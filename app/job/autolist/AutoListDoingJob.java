
package job.autolist;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import job.ApplicationStopJob;
import job.autolist.service.ItemService;
import job.autolist.service.ItemService.DelistOpStatus;
import job.showwindow.ShowWindowExecutor;
import models.autolist.AutoListJobTs;
import models.autolist.AutoListLog;
import models.autolist.AutoListTime;
import models.autolist.AutoListTime.DelistState;
import models.autolist.NoAutoListItem;
import models.autolist.plan.UserDelistPlan;
import models.item.ItemPlay;
import models.showwindow.OnWindowItemCache;
import models.user.User;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;
import service.WindowsService;
import utils.DateUtil;
import utils.PlayUtil;
import bustbapi.TmallItem.TmallItemTitleUpdater;
import cache.UserHasTradeItemCache;

import com.ciaosir.client.PYFutureTaskPool;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.ItemTmall;

import configs.TMConfigs;
import dao.UserDao;
import dao.autolist.AutoListJobTsDao;
import dao.autolist.AutoListLogDao;
import dao.autolist.AutoListTimeDao;
import dao.item.ItemDao;

/**
 * 处理宝贝上下架
 * @author Administrator
 *
 */
@Every("10s")
public class AutoListDoingJob extends Job {
    static PYFutureTaskPool<Boolean> pool = new PYFutureTaskPool<Boolean>(32);

    private static final Logger log = LoggerFactory.getLogger(AutoListDoingJob.class);

    private static final String JOB_NAME = AutoListDoingJob.class.getSimpleName();

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static {
        ApplicationStopJob.addShutdownPool(pool);
    }

    public static boolean isUseDistribution = false;
    
    private static boolean isFirstStartJob = true;

    public void doJob() {
        if (!TMConfigs.Server.jobTimerEnable) {
            return;
        }

        long startTime = System.currentTimeMillis();
//        log.info("job: " + JOB_NAME + " start: " + sdf.format(new Date()));

        doListJob();

        long endTime = System.currentTimeMillis();
//        log.info("job: " + JOB_NAME + " end: " + sdf.format(new Date()));
//        log.info("job: " + JOB_NAME + " used: " + (endTime - startTime) / 1000 + "秒");
    }

    private void doListJob() {
        long oldTs = 0;
        long newTs = System.currentTimeMillis();
        AutoListJobTs jobTs = AutoListJobTsDao.queryByJobId(JOB_NAME);
        if (jobTs == null) {
            oldTs = newTs - 3 * DateUtil.TEN_SECONDS_MILLIS;
            jobTs = AutoListJobTs.createAutoListJobTs(JOB_NAME, newTs);
        } else {
            //这里服务器一段时间崩溃了怎么办，oldTs和newTs之间超过了5分钟
            oldTs = jobTs.getTimestamp();
            jobTs.setTimestamp(newTs);
        }

        if (newTs < oldTs) {
            log.error("newTs比oldTs小，系统时间有误！退出job");
            return;
        }
        // 比较新老时间戳的差异，如果差距大于15分钟，说明可能有down机情况，只处理newTs前5分钟的上下架，其余下周按计划上架
        if (newTs - oldTs > 15 * utils.DateUtil.ONE_MINUTE_MILLIS) {
            log.error("新老时间戳差距大于15分钟，可能有down机情况");
            oldTs = newTs - 5 * DateUtil.ONE_MINUTE_MILLIS;
        }

        AutoListJobTsDao.saveOrUpdateAutoJobTs(jobTs);

        //获取要上架的商品
        long startQueryTime = System.currentTimeMillis();
        List<AutoListTime> resultList = AutoListTimeDao.queryAutoListTimeByTime(oldTs, newTs, isFirstStartJob);
        long endQueryTime = System.currentTimeMillis();

        log.info("query list used " + (endQueryTime - startQueryTime) / 1000.0 + "秒！！！！");
        if (resultList != null) {
            log.info("要自动上下架" + resultList.size() + "个宝贝");
        }

        int totalNum = 0;
        int delistSuccessNum = 0;
        for (AutoListTime autoListTime : resultList) {
            User user = UserDao.findById(autoListTime.getUserId());
            if (user == null || !user.isVaild()) {
                //AutoListTimeDao.deleteAutoListTime(autoListTime);
                continue;
            }

            //DelistUpdateAction.checkDefaultDelistPlan(user);
            if (autoListTime.getPlanId() <= 0) {
                autoListTime = AutoListTimeDao.queryByNumIidWithJDBC(user.getId(), autoListTime.getNumIid());
            }
            if (autoListTime.getStatus() == DelistState.DelistSuccess) {
                delistSuccessNum++;
            }

            totalNum++;
            if (isUseDistribution == false) {
                pool.submit(new ListSingleItem(autoListTime, user, totalNum));
            } else {
                //使用分布式
                boolean isSuccess = WindowsService.addDelist(user.getId(), autoListTime.getNumIid(), totalNum);
                if (isSuccess == false) {
                    //设置上下架失败
                    //还是下周再来
                    /*AutoListLog listLog = AutoListLog.createAutoListJobTs(user.getId(),
                            autoListTime.getNumIid(), System.currentTimeMillis());

                    AutoListTimeDao.setListFail(autoListTime);
                    listLog.setStatus(DelistState.ListFail);
                    listLog.setOpMsg("消息发送失败！！");
                    AutoListLogDao.saveOrUpdateAutoListLog(listLog);*/
                }
            }

        }

        isFirstStartJob = false;
        
        log.warn("真实要自动上下架的宝贝数：" + totalNum + ", 而下架成功却还没有上架的宝贝数：" + delistSuccessNum
                + "--------------------------------");
    }

    public static void doDelistTask(Long userId, Long numIid, int delistIndex) {
        User user = UserDao.findById(userId);
        if (user == null || !user.isVaild()) {
            //AutoListTimeDao.deleteAutoListTime(autoListTime);
            return;
        }

        AutoListTime autoListTime = AutoListTimeDao.queryByNumIidWithJDBC(userId, numIid);

        if (autoListTime == null) {
            log.warn("error!!!!! cannot find AutoListTime for nick: " + user.getUserNick() + "--");
            return;
        }

        pool.submit(new ListSingleItem(autoListTime, user, delistIndex));

    }

    static class ListSingleItem implements Callable<Boolean> {
        private AutoListTime autoListTime;

        private User user;

        private int delistIndex;

        public ListSingleItem(AutoListTime autoListTime, User user, int delistIndex) {
            super();
            this.autoListTime = autoListTime;
            this.user = user;
            this.delistIndex = delistIndex;
        }

        private static boolean judgeDelistTime(long relativeTime) {
            long now = System.currentTimeMillis();

            long nowRelativeTime = now - DateUtil.findThisWeekStart(now);

            long startRelativeTime = nowRelativeTime - 20 * DateUtil.ONE_MINUTE_MILLIS;

            long endRelativeTime = nowRelativeTime + 20 * DateUtil.ONE_MINUTE_MILLIS;

            return true;
        }

        private Boolean doWord() {
            long numIid = 0L;
            try {
                //if (!user.isVaild())
                //    return true;

                UserDelistPlan delistPlan = UserDelistPlan.findByPlanId(autoListTime.getPlanId(), user.getId());
                if (delistPlan == null) {
                    log.error("找不到上下架计划！我是第" + delistIndex + "号宝贝, " + user.getUserNick() + "------------");
                    return false;
                }
                if (delistPlan.isPlanTurnOn() == false) {
                    log.error("上下架计划未开启！我是第" + delistIndex + "号宝贝, user:" + user.getUserNick() + "------------");
                    return false;
                }

                //判断
                if (judgeDelistTime(autoListTime.getRelativeListTime()) == false
                        && autoListTime.getStatus() != DelistState.DelistSuccess) {
                    log.error("当前时间与计划上架时间差距过大，等待下次上架！我是第" + delistIndex + "号宝贝, user:" + user.getUserNick()
                            + "------------");
                    return false;
                }

                boolean isDelistAllTheTime = false;
                if (autoListTime.getPlanId() > 0) {

                    if (delistPlan != null && delistPlan.isDelistAllTheTime()) {
                        isDelistAllTheTime = true;
                    }
                }

                long startTime = System.currentTimeMillis();
                long beginTime = System.currentTimeMillis();

                numIid = autoListTime.getNumIid();

                Set<Long> noListItemList = NoAutoListItem.findByUserAndNumIid(user.getId(), numIid,
                        autoListTime.getPlanId());

                long endTime = System.currentTimeMillis();
                if (endTime - beginTime > 1000) {
                    log.info("查找NoAutoListItem花了" + (endTime - beginTime) / 1000.0 + "秒！！！！" + ", user:"
                            + user.getUserNick() + "---------------");
                }
                beginTime = System.currentTimeMillis();

                if (noListItemList != null && !noListItemList.isEmpty())
                    return false;
                if (user == null) {
                    log.error("找不到用户：" + autoListTime.getUserId());
                    AutoListTimeDao.setNotFoundUser(autoListTime);
                    return false;
                }
                Item item = ItemService.getSingleItem(user, numIid);

                endTime = System.currentTimeMillis();
                if (endTime - beginTime > 1000) {
                    log.info("获取item花了" + (endTime - beginTime) / 1000.0 + "秒！！！！---------------");
                }

                beginTime = System.currentTimeMillis();

                if (item == null) {
                    log.error("宝贝" + numIid + "不存在，从上下架计划中删除！我是第" + delistIndex + "号宝贝------------");
                    AutoListTimeDao.deleteAutoListTime(autoListTime);
                    return false;
                }

                String approveStatus = item.getApproveStatus();
                // 如果商品在仓库，不操作，继续执行下个item

                boolean isInstock = false;
                if ("instock".equals(approveStatus)) {
                    isInstock = true;
                    if (delistPlan.isDelistInstockItems() == false) {

                        if (autoListTime.getStatus() != DelistState.DelistSuccess) {
                            log.error("宝贝" + numIid + "在仓库，不执行！我是第" + delistIndex + "号宝贝, user:" + user.getUserNick()
                                    + "------------");
                            AutoListTimeDao.deleteAutoListTime(autoListTime);
                            return false;
                        } else {
                            log.error("宝贝" + numIid + "之前被下架了，但没有执行上架动作，所以再来一次！我是第" + delistIndex + "号宝贝, user:"
                                    + user.getUserNick() + "------------");
                        }

                    }

                }
                //虚拟的
                if (ItemService.checkIsVirtual(item) == true) {
                    autoListTime.setListTime(item.getListTime().getTime());
                    autoListTime.setStatus(DelistState.Success);
                    log.warn("宝贝" + numIid + "是一个虚拟的宝贝, user:" + user.getUserNick() + "-----------");
                    autoListTime.jdbcSave();
                    return false;
                }

                long listTime = System.currentTimeMillis();
                AutoListLog listLog = AutoListLog.createAutoListJobTs(user.getId(), autoListTime.getPlanId(), numIid,
                        listTime);

                //检测宝贝属性
//                DelistOpStatus checkStatus = ItemService.checkItemAttr(user, item);
                DelistOpStatus checkStatus = null;
                if (user.isTmall()) {
                	TmallItemTitleUpdater updater = new TmallItemTitleUpdater(user.getSessionKey(), numIid, item.getTitle());
                	ItemTmall call = updater.call();
                	checkStatus = call == null ? new DelistOpStatus(false, updater.getErrorMsg()) : new DelistOpStatus(true, "");
				} else {
					checkStatus = ItemService.checkItemAttr(user, item);
				}
                if (checkStatus.isSuccess() == false) {
                    log.error("宝贝" + numIid + "属性词检查失败, 我是第" + delistIndex + "号宝贝------------");
                    autoListTime.setStatus(DelistState.AttrError);
                    listLog.setStatus(DelistState.AttrError);
                    listLog.setOpMsg(checkStatus.getOpMsg());
                    AutoListLogDao.saveOrUpdateAutoListLog(listLog);

                    autoListTime.setListTime(listTime);
                    autoListTime.jdbcSave();
                    return false;
                }

                endTime = System.currentTimeMillis();
                if (endTime - beginTime > 1000) {
                    log.info("检测属性花了" + (endTime - beginTime) / 1000.0 + "秒！！！！" + "---------------");
                }

                beginTime = System.currentTimeMillis();

                DelistOpStatus delistStatus = null;

                // 商品在架，要先下架,再上架
                if (isInstock == false) {
                    delistStatus = ItemService.delistItem(user, numIid);
                    if (delistStatus.isSuccess() == false) {//下架失败
                        log.error("宝贝" + numIid + "下架失败, 我是第" + delistIndex + "号宝贝------------");
                        AutoListTimeDao.setDelistFail(autoListTime);
                        listLog.setStatus(DelistState.DeListFail);
                        listLog.setOpMsg(delistStatus.getOpMsg());
                        AutoListLogDao.saveOrUpdateAutoListLog(listLog);

                        autoListTime.setListTime(listTime);
                        autoListTime.jdbcSave();
                        return false;
                    }

                    autoListTime.setStatus(DelistState.DelistSuccess);
                    autoListTime.jdbcSave();

                    endTime = System.currentTimeMillis();
                    if (endTime - beginTime > 1000) {
                        log.info("宝贝下架花了" + (endTime - beginTime) / 1000.0 + "秒！！！！" + "---------------");
                    }
                }

                DelistOpStatus listStatus = null;

                for (int i = 0; i < 3; i++) {
                    listStatus = ItemService.listItem(user, numIid, item.getNum());
                    if (listStatus.isSuccess() == true) {
                        break;
                    }
                    String opMsg = listStatus.getOpMsg();
                    if (!StringUtils.isEmpty(opMsg) && opMsg.contains("系统繁忙")) {
                        try {
                            PlayUtil.sleepQuietly(1000L);
                        } catch (Exception ex) {
                            log.error(ex.getMessage(), ex);
                        }

                    } else {
                        break;
                    }
                }

                final boolean isListSuccess = listStatus.isSuccess();
                final long modifyTime = getModifyTime(delistStatus, listStatus);
                if (modifyTime > 0) {

                }

                /*
                 * check for the new delist time...
                 */
                final long finalDelistNumIid = numIid;
                if (finalDelistNumIid > 0L) {
                    TMConfigs.getShowwindowPool().submit(
                            new WindowFixerAfterDelist(user, numIid, isListSuccess, modifyTime));
                }

                if (listStatus.isSuccess() == false) {
                    log.error("宝贝" + numIid + "上架失败, 我是第" + delistIndex + "号宝贝, user:" + user.getUserNick()
                            + "------------");
                    AutoListTimeDao.setListFail(autoListTime);
                    listLog.setStatus(DelistState.ListFail);
                    listLog.setOpMsg(listStatus.getOpMsg());
                    AutoListLogDao.saveOrUpdateAutoListLog(listLog);
                    autoListTime.setListTime(listTime);
                    autoListTime.jdbcSave();
                    return false;
                }
                autoListTime.setListTime(listTime);
                //上架成功状态
                AutoListTimeDao.setListSuccess(autoListTime);
                listLog.setStatus(DelistState.Success);
                listLog.setOpMsg("");
                listLog.setListTime(System.currentTimeMillis());
                AutoListLogDao.saveOrUpdateAutoListLog(listLog);

                endTime = System.currentTimeMillis();
//                log.info("宝贝" + numIid + "上架成功, 我是第" + delistIndex + "号宝贝--used " + (endTime - startTime) / 1000.0
//                        + "秒, user:"  + user.getUserNick() + "！！！！-------------");

//                log.info("auto list a item " + numIid + " used " + (endTime - startTime) / 1000.0 + "秒！！！！");

                return true;
            } catch (Exception ex) {
                log.error("宝贝" + numIid + "上下架失败");
                log.error(ex.getMessage(), ex);
                return false;
            }

        }

        private static long getModifyTime(DelistOpStatus delistStatus, DelistOpStatus listStatus) {

            //上架成功
            if (listStatus != null && listStatus.isSuccess()) {
                Date modifyDate = listStatus.getItem() == null ? null : listStatus.getItem().getModified();
                if (modifyDate != null) {
                    return modifyDate.getTime();
                }
            }

            if (delistStatus == null) {
                //没有下架操作，原来就在下架的
                return 0;
            }

            Date modifyDate = delistStatus.getItem() == null ? null : delistStatus.getItem().getModified();
            if (modifyDate != null) {
                return modifyDate.getTime();
            }

            return 0;
        }

        @Override
        public Boolean call() throws Exception {
            return doWord();
        }

    };

    static class WindowFixerAfterDelist implements Callable<ItemPlay> {
        User user;

        boolean isListSuccess = false;

        long modifyTime = 0L;

        long numIid;

        @Override
        public ItemPlay call() throws Exception {
            if (modifyTime <= 0L) {
                // Nothing to do..
                return null;
            }

            OnWindowItemCache.get().removeItem(user, numIid);

            if (isListSuccess) {
                long newDelistTime = modifyTime;
                ItemDao.updateDelistTime(user, numIid, newDelistTime);

            } else {
                // So, it's down....
                UserHasTradeItemCache.clear(user);
                // TODO modify ?
            }

            if (!user.isShowWindowOn()) {
                return null;
            }

            new ShowWindowExecutor(user).doJob();

            return null;

        }

        public WindowFixerAfterDelist(User user, long numIid, boolean isListSuccess, long modifyTime) {
            super();
            this.user = user;
            this.isListSuccess = isListSuccess;
            this.modifyTime = modifyTime;
            this.numIid = numIid;
        }

    }
}
