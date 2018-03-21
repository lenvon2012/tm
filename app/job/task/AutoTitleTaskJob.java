
package job.task;

import static java.lang.String.format;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import jdp.ApiJdpAdapter;
import job.ApplicationStopJob;
import job.writter.TitleLogWritter;
import models.item.ItemPlay;
import models.oplog.TitleOpRecord;
import models.task.AutoTitleTask;
import models.task.AutoTitleTask.UserTaskStatus;
import models.task.AutoTitleTask.UserTaskType;
import models.task.AutoTitleTask.WireLessDetailConfig;
import models.user.User;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.netty.util.internal.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.db.jpa.NoTransaction;
import play.jobs.Every;
import play.jobs.Job;
import result.TMResult;
import utils.PlayUtil;
import actions.DiagAction;
import actions.DiagAction.BatchReplacer;
import actions.DiagAction.BatchResultMsg;
import actions.task.TaskProgressAction.AutoTitleProgressAction;
import actions.wireless.WireLessUtil;
import actions.wireless.WirelessItemAssistant.WireLessDescWritter;
import actions.wireless.WirelessItemWorker;
import autotitle.AutoTitleOption.BatchPageOption;
import bustbapi.FenxiaoApi;
import bustbapi.FenxiaoApi.BatchAutoTitleRecommend;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.PYFutureTaskPool;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.NumberUtil;
import com.sun.swing.internal.plaf.synth.resources.synth;
import com.taobao.api.domain.Item;

import controllers.APIConfig;
import controllers.BatchOp.BatchOpResult;
import dao.UserDao;
import dao.item.ItemDao;

@Every("15s")
@NoTransaction
public class AutoTitleTaskJob extends Job {

    private final static Logger log = LoggerFactory.getLogger(AutoTitleTaskJob.class);

    //是否是系统刚启动，刚启动的时候需要重启任务的
    private static boolean isOnBootStrap = true;

    static PYFutureTaskPool<Void> pool = null;

    static int taskTheadNum = Play.mode.isDev() ? 1 : NumberUtil.parserInt(Play.configuration.get("autotask.num"), 0);

    static int freeNum = taskTheadNum;

    private synchronized static void changeWorkingNum(boolean isAdd) {
        if (isAdd) {
            freeNum--;
        } else {
            freeNum++;
        }
    }

    static PYFutureTaskPool<Void> getPool() {
        if (pool == null) {
            pool = new PYFutureTaskPool<Void>(taskTheadNum);
            ApplicationStopJob.addShutdownPool(pool);
        }
        return pool;
    }

    private static boolean checkIsNeedReTry(AutoTitleTask task) {
        if (task == null || task.getStatus() != UserTaskStatus.Doing) {
            return false;
        }

        if (task.isBuildAutoTitleTask() == true) {
            return true;
        } else {
            return false;
        }

    }

    private static void addToPool(AutoTitleTask task, List<FutureTask<Void>> promises) {
        User user = UserDao.findById(task.getUserId());

        if (user == null) {
            setTaskFailStatus(task, "找不到User");
            return;
        }

        task.setStatus(UserTaskStatus.InRunPool);
        task.jdbcSave();

        log.info("start AutoTitleTask for user: " + user.getUserNick()
                + " -----------------------------------");
        
        FutureTask<Void> promise = getPool().submit(new SubmitTitleTask(user, task));
        promises.add(promise);
    }

    private static void setTaskFailStatus(AutoTitleTask task, String message) {
        if (task == null) {
            return;
        }

        long finishedTime = System.currentTimeMillis();

        task.updateTaskResult(finishedTime, 0, UserTaskStatus.Failed, 0, 0, message);

        task.jdbcSave();
    }

    private static void setTaskSuccessStatus(AutoTitleTask task, long startTime,
            int totalNum, int successNum) {
        if (task == null) {
            return;
        }

        long finishedTime = System.currentTimeMillis();

        String message = task.getMessage();
        if (StringUtils.isEmpty(message)) {
            message = "";
        }

        task.updateTaskResult(finishedTime, finishedTime - startTime,
                UserTaskStatus.Finished, totalNum, successNum, message);

        task.jdbcSave();
    }

    private static void doForDoingTasks(List<FutureTask<Void>> promises) {
        if (promises == null) {
            return;
        }

        if (isOnBootStrap == false) {
            return;
        }

        List<AutoTitleTask> doingTaskList = AutoTitleTask.queryDoingTasks();

        if (CommonUtils.isEmpty(doingTaskList)) {
            doingTaskList = new ArrayList<AutoTitleTask>();
        }

        /**
         * 出现这种情况的，都是服务器失败了的，比如服务器重启
         */
        for (AutoTitleTask task : doingTaskList) {

            if (task == null) {
                continue;
            }

            //检查，如果需要重新执行的
            if (checkIsNeedReTry(task) == true) {
                log.warn("重试自动标题的任务： AutoTitleTask: " + task.toString()
                        + " -----------------------------------");

                task.setMessage("重新尝试执行Doing任务！");

                addToPool(task, promises);
            } else {

//                log.warn("结束Doing状态的任务： AutoTitleTask: " + task.toString() + " -----------------------------------");
//                setTaskFailStatus(task, "服务器出现异常！");

            }
        }

    }

    private static void doForInRunPoolTasks(List<FutureTask<Void>> promises) {
        if (promises == null) {
            return;
        }

        if (isOnBootStrap == false) {
            return;
        }

        List<AutoTitleTask> inRunPoolTasks = AutoTitleTask.queryInRunPoolTasks();

        if (CommonUtils.isEmpty(inRunPoolTasks)) {
            inRunPoolTasks = new ArrayList<AutoTitleTask>();
        }

        /**
         * 出现这种情况的，都是服务器失败了的，比如服务器重启
         */
        for (AutoTitleTask task : inRunPoolTasks) {
            log.warn("重试自动标题的任务： AutoTitleTask: " + task
                    + " -----------------------------------");

            task.setMessage("重新尝试in run pool任务！");

            addToPool(task, promises);
        }
    }

    @Override
    public void doJob() {

        Thread.currentThread().setName(AutoTitleTaskJob.class.getName());
        log.error(">>>>>>>>>>>curr task num:" + taskTheadNum + "curr free num :" + freeNum);

        if (taskTheadNum <= 0) {
            return;
        }

        List<FutureTask<Void>> promises = new ArrayList<FutureTask<Void>>();

        if (isOnBootStrap == true) {

            //这两个顺序不能换，doForDoingTasks中会将状态设置成InRuleStatus的
            doForInRunPoolTasks(promises);

            doForDoingTasks(promises);

        }

        isOnBootStrap = false;

        List<AutoTitleTask> newTaskList = AutoTitleTask.queryNewTasks(taskTheadNum);
        if (CommonUtils.isEmpty(newTaskList)) {
            newTaskList = new ArrayList<AutoTitleTask>();
        }

        for (AutoTitleTask task : newTaskList) {

            log.error("do for new task:" + task);
            if (task == null) {
                continue;
            }

            log.info("[currrent free num ]" + freeNum);
            if (freeNum <= 0) {
                continue;
            }
            addToPool(task, promises);

        }

        int count = promises.size();

        if (count <= 0) {
            log.warn("no auto title task-------------");
        } else {
            log.info("there are " + count + " auto title tasks-----------");
        }

        /*
        for (FutureTask<Void> promise : promises) {
            try {
                promise.get();
            } catch (InterruptedException e) {
                log.warn(e.getMessage(), e);
            } catch (ExecutionException e) {
                log.warn(e.getMessage(), e);
            }
        }
        */

    }

    public static class SubmitTitleTask implements Callable<Void> {

        private User user;

        private AutoTitleTask task;

        public SubmitTitleTask(User user, AutoTitleTask task) {
            super();
            this.user = user;
            this.task = task;
        }

        @Override
        public Void call() throws Exception {
        	
            log.error("exec for the task:" + task);
            Thread.currentThread().setName(SubmitTitleTask.class.getName());
            changeWorkingNum(true);
            try {
                if (task == null) {
                    log.warn("auto title task is null !!!!!!!!!!");
                    return null;
                }

                if (user == null || user.isVaild() == false) {
                    log.warn("cannot find user!!!!!!!!!!!");

                    setTaskFailStatus(task, "找不到用户，或用户过期！");

                    return null;
                }

                try {

                    task = AutoTitleTask.queryByTaskId(user.getId(), task.getTaskId());
                    
                    if (task == null) {
                        log.warn("can not find task for taskId: " + task.getTaskId() + "!!!!!!!!!!");
                        return null;
                    }

                    if (task.getStatus() >= UserTaskStatus.Finished) {
                        log.warn("the task has been doned before!!!!!!!" + task);

                        return null;
                    }
                    if (task.getStatus() >= UserTaskStatus.Doing) {
                        log.warn("the task is doing now!!!!!!!" + task);

                        return null;
                    }

                    //设置状态
                    task.addRunCount();

                    task.setStatus(UserTaskStatus.Doing);
                    task.jdbcSave();
                    
                    boolean isSuccess = false;

                    try {
                        switch (task.getType()) {
                            case UserTaskType.BuildAutoTitle:
                                isSuccess = doBuildAutoTitle(user, task);
                                break;
                            case UserTaskType.BuildPhoneDetailByTaobaoZhuli:
                                isSuccess = doBuildPhoneDetailByTaobaoAssistant(user, task);
                                break;
                            case UserTaskType.BuildPhoneDetailByNumIids:
                                isSuccess = doBuildPhoneDetailByNumIids(user, task);
                                break;
                            default:
                                isSuccess = false;
                                setTaskFailStatus(task, "找不到任务类型！");
                                return null;
                        }
                    } catch (Exception e) {
                        log.warn(e.getMessage(), e);
                    }

                    log.error("back title op id:" + task.getTitleOpId() + " with result :" + isSuccess);
                    if (isSuccess == false) {
                        task.setStatus(UserTaskStatus.Failed);
                        task.jdbcSave();
                    } else {

                        task.setStatus(UserTaskStatus.Finished);
                        task.jdbcSave();
                    }

                    return null;

                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                    log.warn("自动标题异常： autoTitle for userNick: " + user.getUserNick()
                            + " -----------------------------------");
                    setTaskFailStatus(task, "任务执行出错！");
                }
            } finally {
                changeWorkingNum(false);
            }

            log.info("[finish task]" + task);
            return null;
        }

        private boolean doBuildPhoneDetailByTaobaoAssistant(User user, AutoTitleTask task) {

            WireLessDetailConfig config = task.genWirelessConfig();
            File file = new File(config.getFilePath());
            if (!file.exists()) {
                setTaskFailStatus(task, "文件不存在:[" + file.getName() + "]！");
                return false;
            }

            TMResult<List<String[]>> wordResult = new WirelessItemWorker(user, file, task, config).call();
            log.info("[word result:]" + wordResult);
            AutoTitleProgressAction.closeTaskProgress(task.getTaskId());

            if (wordResult != null && wordResult.isOk()) {
                File dir = new File(WireLessUtil.genWirelessOutPutDir(), String.valueOf(user.getId() % 1000));
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File output = new File(dir, user.getUserNick()
                        + new SimpleDateFormat("MMdd_HH点mm分ss秒").format(new Date())
                        + ".csv");

                List<String[]> listRow = wordResult.getRes();
                WireLessDescWritter.writeToCsv(listRow, output);

                task.setMessage(output.getAbsolutePath());
                task.setFinishedTime(System.currentTimeMillis());
                log.info("[task:]" + task);
                task.jdbcSave();

                return true;
            } else {
                task.setFinishedTime(System.currentTimeMillis());
                if (wordResult != null) {
                    setTaskFailStatus(task, wordResult.getMsg());
                } else {
                    setTaskFailStatus(task, "任务失败");
                }
                return false;
            }
        }

        private boolean doBuildPhoneDetailByNumIids(User user, AutoTitleTask task) {

            log.info(format("doBuildPhoneDetailByNumIids:user, task".replaceAll(", ", "=%s, ") + "=%s", user, task));

            Set<Long> targetIds = new HashSet<Long>();

            WireLessDetailConfig config = task.genWirelessConfig();

            log.info("[numiids:]" + config.getNumIids());
            /**
             * 取哪些id
             */
            log.info("[let's parse]");
            List<Long> ids = PlayUtil.parseIdsList(config.getNumIids());
            if (CommonUtils.isEmpty(ids)) {
                log.info("[try query]");
                Set<Long> set = ItemDao.findNumIdsByCondition(user.getId(), config.getNotStatus(), config.getItemCat(),
                        config.getSellerCid());
                targetIds.addAll(set);
            }
            targetIds.addAll(ids);
            task.setTotalNum(targetIds.size());
            AutoTitleProgressAction.createTaskProgress(task.getTaskId(), targetIds.size());

            WirelessItemWorker worker = new WirelessItemWorker(user, null, task, config);

            TMResult<Map<String, String>> res = worker.doForAllNumIids(new ArrayList(targetIds));
            task.setFinishedTime(System.currentTimeMillis());

            if (res.isOk()) {
                task.setResults(JsonUtil.getJson(res.getRes()));
                log.info("[task:]" + task);
                task.jdbcSave();
                AutoTitleProgressAction.closeTaskProgress(task.getId());
                return true;
            } else {
                if (res.getMsg() != null) {
                    task.setMessage(res.getMsg());
                    setTaskFailStatus(task, res.getMsg());
                } else {
                    setTaskFailStatus(task, "任务失败");
                }
                task.jdbcSave();
                return false;
            }
        }

        private static boolean doBuildAutoTitle(User user, AutoTitleTask task) {

            long startTime = System.currentTimeMillis();
            
            String configJson = task.getConfigJson();
            log.info("[config json:]" + configJson);
            if (StringUtils.isEmpty(configJson)) {
                setTaskFailStatus(task, "configJson is empty!!");
                return false;
            }

            BatchPageOption opt = BatchPageOption.parseByJson(configJson);
            log.info("[opt:]" + opt);

            if (opt == null) {
                setTaskFailStatus(task, "任务解析出错!!");
                return false;
            }

            List<ItemPlay> items = ItemDao.findForBatchTitleOptimise(user, opt.getSellerCatId(), opt.getItemCatId(),
                    opt.getStatus(), opt.isAllSale(), 
                    opt.getTitle(), opt.getStartScore(), opt.getEndScore(), opt.isNewSearchRule());
            
            log.info(" item size:" + items.size());

            if (CommonUtils.isEmpty(items)) {
                items = new ArrayList<ItemPlay>();
            }

            int limit = APIConfig.taoxuanci.getMaxAvailable(user);
//            if (items.size() > 3000 && !"jinhua101012".equals(user.getUserNick())) {
            if (items.size() > limit) {
                log.warn("there are too many items for auto title, kill it");
                setTaskFailStatus(task, "宝贝数太多，有" + items.size() + "个宝贝,而当前版本最多支持" + limit + "个宝贝,不支持自动标题操作！");
                return false;
            }
            log.error(" to optimise item num:" + items.size());

//            int totalNum = 0;
//            webResult.getSuccessNum() + webResult.getFailNum();
//            int successNum = 0;
//            webResult.getSuccessNum();
            //items的两倍，包括生成推荐标题，提交推荐标题
//            List<BatchOpResult> list = new ArrayList<BatchOpResult>();

            BatchOpResult batchResult = new BatchOpResult();
            AutoTitleProgressAction.createTaskProgress(task.getTaskId(), items.size() * 2);

            int size = items.size();
            log.error("itme size :" + size);
            for (int start = 0; start < size;) {
                int end = start + 50;
                if (end > size) {
                    end = size;
                }

                log.error(String.format("[start: %d  and  end : %d]", start, end));
                List<ItemPlay> subItems = items.subList(start, end);
                doWork(user, task, opt, subItems, batchResult);
                log.error("[curr batch result :]" + batchResult);
                start = end;
            }
            
            List<BatchResultMsg> resList = new ArrayList<BatchResultMsg>();
            resList.addAll(batchResult.getOkList());
            resList.addAll(batchResult.getErrorList());

            TitleOpRecord logRecord = TitleOpRecord.build(user, resList, batchResult);
            task.setTitleOpRecord(logRecord);

            AutoTitleProgressAction.closeTaskProgress(task.getTaskId());
            setTaskSuccessStatus(task, startTime, batchResult.getSuccessNum() + batchResult.getFailNum(),
                    batchResult.getSuccessNum());

            return true;
        }

        private static BatchOpResult doWork(User user, AutoTitleTask task, BatchPageOption opt, List<ItemPlay> items,
                BatchOpResult mainResult) {
        	
            log.error("page opt : " + opt);

            List<Long> idsList = ItemDao.toIdsList(items);
            if (CommonUtils.isEmpty(idsList)) {
                return mainResult;
            }

            List<Item> remoteItems = ApiJdpAdapter.multiItemList(user, idsList);
            
            if (CommonUtils.isEmpty(remoteItems)) {
                return mainResult;
            }
            
            final Map<String, String> newTitleMap = new ConcurrentHashMap<String, String>();
            FenxiaoApi.buildResult(newTitleMap, opt.getRecMode(), user, remoteItems, opt, task.getTaskId());
            List<BatchResultMsg> msgs = new BatchReplacer(user, items, newTitleMap, task.getTaskId(), opt).call();

            List<BatchResultMsg> toAddMsgs = new ArrayList<BatchResultMsg>();
            Iterator<BatchResultMsg> it = msgs.iterator();
            while (it.hasNext()) {
                BatchResultMsg msg = it.next();
                if (msg.isOk()) {
                    TitleLogWritter.addMsg(user.getId(), msg.getNumIid(), msg.getOriginTitle(), msg.getTitle());
                    continue;
                }

                BatchResultMsg newMsg = tryNewTitle(user, msg, items, opt, newTitleMap);
                if (newMsg == null) {
                    // Nothing happens...

                } else {
                    toAddMsgs.add(newMsg);
                    it.remove();

                }
            }

            msgs.addAll(toAddMsgs);

            DiagAction.refreshByUpdateMsgs(user, msgs);
            mainResult.addAllList(msgs);
            newTitleMap.clear();
            return mainResult;
        }

        private static BatchResultMsg tryNewTitle(User user, BatchResultMsg msg, List<ItemPlay> items,
                BatchPageOption opt, Map<String, String> newTitleMap) {
            String errorMsg = msg.getMsg();
            if (StringUtils.isEmpty(errorMsg)) {
                return null;
            }

            if (errorMsg.indexOf("宝贝标题中必须要含有") < 0 || errorMsg.indexOf("为产品标题") < 0) {
                return null;
            }

            ItemPlay item = null;
            for (ItemPlay i : items) {
                if (i.getNumIid().longValue() == msg.getNumIid().longValue()) {
                    item = i;
                }
            }

            if (item == null) {
                return null;
            }

            BatchResultMsg newMsg = null;
            int startIndex = errorMsg.indexOf("宝贝标题中必须要含有") + "宝贝标题中必须要含有".length();
            int endIndex = errorMsg.indexOf("。其中");
            String mustContains = errorMsg.substring(startIndex, endIndex);
            log.error("must containers:" + mustContains);
//            opt.setFixedStart(mustContains);
            String originTitle = item.getTitle();

            item.setTitle(mustContains);
            opt.setRepalceOrigin(false);

            BatchAutoTitleRecommend caller = new BatchAutoTitleRecommend(item, user, opt, opt.getRecMode(),
                    newTitleMap);
            try {
                String newTitle = caller.call();
                if (StringUtils.isEmpty(newTitle)) {
                    return null;
                }
                newMsg = DiagAction.executeUpdate(user, item, newTitle);
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
            newMsg.setOriginTitle(originTitle);
            return newMsg;
        }
    }
}
