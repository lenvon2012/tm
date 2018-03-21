
package controllers;

import static java.lang.String.format;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import job.AutoCommentCrontabJob;
import job.click.HourlyCheckerJob;
import job.hotitems.SpiderHotItemThread;
import job.showwindow.DropCacheInitJob;
import job.showwindow.RecentDownCacheJob;
import job.showwindow.ShowWindowCrontabJob;
import job.showwindow.WindowDownAfterItemDownChecker;
import job.showwindow.WindowsQueueJob;
import job.writter.TradeRateWritter;
import job.writter.TradeWritter;
import jxl.write.WriteException;
import models.CPEctocyst.SellerToStaff;
import models.defense.DefenseLog;
import models.item.ItemPlay;
import models.mysql.word.WordBase;
import models.op.CommentDeal;
import models.op.TraceLogClick;
import models.op.TraceLogInvite;
import models.paipai.PaiPaiUser;
import models.updatetimestamp.updates.ItemUpdateTs;
import models.user.User;
import models.vas.ArticleBizOrderPlay;
import onlinefix.UpdateAllItemsJob;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.db.jpa.NoTransaction;
import play.jobs.JobsPlugin;
import play.mvc.Controller;
import play.mvc.With;
import result.TMResult;
import transaction.DBBuilder;
import actions.UserAction;
import actions.UserLoginAction;
import bustbapi.TMApi;
import bustbapi.UserAPIs;

import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.DateUtil;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.MapIterator;
import com.ciaosir.commons.ClientException;

import configs.TMConfigs;
import configs.TMConfigs.Operate;
import controllers.Windows.ShowWindowMustRefresh;
import dao.UserDao;
import dao.defense.DefenseLogDao;
import dao.paipai.PaiPaiUserDao;
import dao.vas.ArticleBizOrderDao;

@With(Secure.class)
public class TMAdmin extends Controller {

    private static final Logger log = LoggerFactory.getLogger(TMAdmin.class);

    public static final String TAG = "Admin";

    public static void index() {
        render("Application/crud.html");
    }

    public static void userlist() {
        render("Application/userlist.html");
    }

    public static void links() {
        render("Application/crudlinks.html");
    }

    public static void tasks() {
        render("Application/crudtasks.html");
    }

    public static void deleteHPZSWangwang(String nick) {
    	if(StringUtils.isEmpty(nick)) {
    		renderText("请输入旺旺号");
    	}
    	User user = UserDao.findByUserNick(nick);
    	if(user == null) {
    		renderText("没有这个用户");
    	}
    	SellerToStaff sellerToStaff = SellerToStaff.findByUserId(user.getId());
    	if(sellerToStaff == null) {
    		renderText("该用户未曾授权");
    	}
    	boolean isSuccess = sellerToStaff.rawDelete(user.getId());
    	if(isSuccess) {
    		renderText("取消成功");
    	}
    	renderText("取消失败");
    }
    
    public static void updateUserStatus(int status, String nick) {
    	if(status < 0) {
    		renderText("status值不对");
    	}
    	if(StringUtils.isEmpty(nick)) {
    		renderText("nick为空");
    	}
    	User user = UserDao.findByUserNick(nick);
    	if(user == null) {
    		renderText("没有这个用户");
    	}
    	user.setType(status);
    	user.jdbcSave();
    	TMController.clearUser();
        TMController.putUser(user);
        renderText("更新成功"); 
    }
    
    public static void list(String startYMS, String endYMS, int pn, int ps, String nick) {
        if (nick != null) {
            nick = StringUtils.trim(nick);
        }

        PageOffset po = new PageOffset(pn, ps, 20);
        long startMillis = DateUtil.simpleParse(DateUtil.genYMS(), startYMS, 0L).getTime();
        long endMillis = DateUtil.simpleParse(DateUtil.genYMS(), startYMS, Long.MAX_VALUE >> 4).getTime();

        List<ArticleBizOrderPlay> list = ArticleBizOrderDao.fetchDurationList(nick, startMillis, endMillis, po, false);
        int count = ArticleBizOrderDao.countDurationList(startMillis, endMillis, po);
        TMResult res = new TMResult(list, count, po);
        renderJSON(JsonUtil.getJson(res));
    }

    public static void makeDev(long id) {
        Long uid = id;

        User user = UserDao.findById(uid);

        if (user == null) {
            notFound();
        }

        log.error("[Found Make Dev User]" + user);

        log.error("set dev user :" + user);

//        List<ItemPlay> items = ItemDao.findByUserId(user.getId(), 1);
        //ItemUpdateTs ts = ItemUpdateTs.findById(user.getId());
        ItemUpdateTs ts = ItemUpdateTs.fetchByUser(user.getId());
        log.error("ts :" + ts);
        if (ts == null) {
            UserLoginAction.basicInfoCheck(user, user.getUserNick());
        }

        TMController.clearUser();
        setDevUser(user);
    }

    public static void makeDevName(String name) {
        if (StringUtils.isBlank(name)) {
            notFound();
        }

        User user = UserDao.findByUserNick(name.trim());
        if (user == null) {
            notFound();
        }

        //ItemUpdateTs ts = ItemUpdateTs.findById(user.getId());
        ItemUpdateTs ts = ItemUpdateTs.fetchByUser(user.getId());
        log.error("ts :" + ts);
        if (ts == null) {
            UserLoginAction.basicInfoCheck(user, user.getUserNick());
        }

        TMController.clearUser();
        setDevUser(user);
    }

    protected static void setDevUser(User user) {

        UserAction.updateUser(user);
        TMController.putUser(user);

//        BusController.checkForward(user, user.getUserNick());
//
//        BusController.putUser(user);
//        BusController.resetNick(user.getUserNick());

        // Application.syncImmediate(user, user.getUserNick());
        // new UserUpdateJob(user, user.getUserNick()).doJob();
//        UserLoginAction.basicInfoCheck(user, user.getUserNick());

        // Application.index();
//        render("Application/redirecting.html");
//        Items.index();

//        Home.index(false);

        APIConfig.get().afterLogin(user, null, false, false);

    }

    @NoTransaction
    public static void status() {
        StringBuilder sb = new StringBuilder();

//        BusMonitor.appendReport(sb);

//        sb.append("User Update Thread count :" + UpdateThread.threadCount);

        sb.append(" database conn status :" + DBBuilder.getStatus() + " \n");

        sb.append(ShowWindowCrontabJob.getStatus());
        sb.append('\n');
        sb.append(DropCacheInitJob.getStatus());
        sb.append('\n');
        sb.append(UpdateAllItemsJob.getStatus());
        sb.append('\n');
        sb.append("recommend queue size :" + WindowsQueueJob.queueDeleteMsg.size());
        sb.append('\n');

        sb.append('\n');
        sb.append("down shelf size :" + WindowDownAfterItemDownChecker.queue.size());
        sb.append('\n');

        sb.append('\n');
        sb.append("TradeWritter queue size :" + TradeWritter.tradeListToWritten.size());
        sb.append("\nTradeWritter statusMsg: " + TradeWritter.statusMessage);
        sb.append('\n');

        sb.append('\n');
        sb.append("TradeRateWritter queue size :" + TradeRateWritter.tradeRateListToWritten.size());
        sb.append("\nTradeRateWritter statusMsg: " + TradeRateWritter.statusMessage);
        sb.append("\n\n");

        JobsPlugin jobPlugin = (JobsPlugin) Play.pluginCollection.getPluginInstance(JobsPlugin.class);
        sb.append(jobPlugin.getStatus());

        renderText(sb.toString());
    }

    public static void updateAll() {
        //List<User> users = User.findAll();
        List<User> users = UserDao.fetchAllUser();
        for (User user : users) {
            com.taobao.api.domain.User call = new UserAPIs.UserGetApi(user.getSessionKey(), null).call();
            if (call == null) {
                continue;
            }
            user.update(call).jdbcSave();
        }
    }

    public static void updateWords(int offset) throws ClientException {
        WordBase.update(offset, 256);
    }

    public static void updateItems() {
        TMConfigs.getShowwindowPool().submit(new Callable<ItemPlay>() {

            @Override
            public ItemPlay call() throws Exception {
                new UpdateAllItemsJob().doJob();
                return null;
            }
        });
    }

    public static void updateUserSale() {
        TMConfigs.getShowwindowPool().submit(new Callable<ItemPlay>() {
            @Override
            public ItemPlay call() throws Exception {
                new ShowWindowMustRefresh().call();
                return null;
            }
        });
    }

    public static void startWindow() {
        new WindowsQueueJob(true).now();
    }

    public static void turnEnableRemote(boolean on) {
        TMConfigs.ShowWindowParams.enableRemoteWindow = on;
    }

    public static void turnEnableShelf(boolean on) {
        TMConfigs.ShowWindowParams.enableItemShelfDownMesssage = on;
    }

    public static void updateWord(int offset, int limit) throws ClientException {
        WordBase.update(offset, limit);
    }

    public static void getUser(Long userId) {
        renderJSON(JsonUtil.getJson(UserDao.findById(userId)));
    }

    public static void turnPayForFree(boolean on) {
        Operate.REPAY_FOR_FREE = on;
    }

    public static void isop(boolean on) {
        TMConfigs.IS_OP = on;
    }

    public static void setClick(boolean on) {
        log.info(format("setClick:on".replaceAll(", ", "=%s, ") + "=%s", on));
        HourlyCheckerJob.HOUR_JOB_ENABLE = on;
    }

    public static void setReduce(boolean on) {
        log.info(format("setClick:on".replaceAll(", ", "=%s, ") + "=%s", on));
        TMConfigs.Referers.reduceornot = on;
    }
    
    public static void getReduce() {
        renderText(TMConfigs.Referers.reduceornot);
    }

    public static void doHourly() {
        new HourlyCheckerJob().now();
    }

    public static void setNickAward(String wangwang) {
        String target = StringUtils.trim(wangwang);
        User user = UserDao.findByUserNick(target);
        if (user == null) {
            renderText("555555555555 亲，没有这个 旺旺诶[" + wangwang + "]");
        } else {
            user.setPopularAward(true);
            user.jdbcSave();
            renderText("推广位 赠送成功 , 旺旺[" + wangwang + "]");
        }
    }

    public static void setPaiPaiAward(Long qq) {
        PaiPaiUser user = PaiPaiUserDao.findById(qq);
        if (user == null) {
            renderText("55555555555 亲，没有这个 QQ诶[" + qq + "]");
        } else {
            user.setPopularAward(true);
            user.jdbcSave();
            renderText("推广位 赠送成功 , QQ[" + qq + "]");
        }
    }

    public static void fixComment(long id) {
        User user = UserDao.findById(id);
        if (user == null) {
            return;
        }
        log.info("[fix comment for user]" + user);
        try {
            new AutoCommentCrontabJob.UserCaller(user).call();
        } catch (Exception e) {
            log.warn(e.getMessage(), e);

        }
    }

    public static void getYingxiaoLink(String userNick, String key) {
        if (userNick.isEmpty()) {
            renderText("用户名为空");
        }
        Map<String, String> paramStrs = APIConfig.get().getSellLinkParamStr();
        if (paramStrs == null) {
            renderText("没有对应的营销链接");
        }
        String paramStr = paramStrs.get(key);
        if (paramStr == null || paramStr.isEmpty()) {
            renderText("没有对应的营销链接");
        }
        String link = new bustbapi.FuwuApis.SaleLinkGenApi(APIConfig.get().getApiKey(), APIConfig.get().getSecret(),
                userNick, paramStr).call();
        if (link == null || link.isEmpty()) {
            renderText("获取营销链接出错");
        }
        renderText(link);
    }

    public static void appInfo() {
        APIConfig config = APIConfig.get();
        String appKey = config.getApiKey();
        renderText(appKey);
    }

    public static void vasorder(String nick) {
        List<ArticleBizOrderPlay> list = ArticleBizOrderDao.findVasOrderByNick(nick.trim());
        renderJSON(list);
    }

    public static void tracelogmoney(String start, String end) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = df.parse(start);
        Date endDate = df.parse(end);

        Map<String, Long> invite = TraceLogInvite.durationMap(startDate.getTime(), endDate.getTime());

        Map<String, Long> clickPv = TraceLogClick.durationMapPv(startDate.getTime(), endDate.getTime());
        Map<String, Long> clickUv = TraceLogClick.durationMapUv(startDate.getTime(), endDate.getTime());

        List<Entry<String, Long>> list = new ArrayList<Map.Entry<String, Long>>(invite.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Long>>() {
            @Override
            public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {
                return (int) (o1.getValue() - o2.getValue());
            }
        });

        Map<String, String> ret = new HashMap<String, String>();
        for (Entry<String, Long> entry : list) {
            String key = entry.getKey();
            Long pv = clickPv.get(key);
            if (pv == null) {
                pv = 0L;
            }
            Long uv = clickUv.get(key);
            if (uv == null) {
                uv = 0L;
            }
            ret.put(key, entry.getValue() + " : " + uv + "/" + pv);
        }

        // final StringBuilder sb = appendMap(invite, false);
        final StringBuilder sb = appendMap(ret);

        renderText(sb.toString());
    }

    private static StringBuilder appendMap(Map<String, String> map) {
        final StringBuilder sb = new StringBuilder();
        new MapIterator<String, String>(map) {
            @Override
            public void execute(Entry<String, String> entry) {
                sb.append(entry.getKey());
                sb.append("\t\t");
                sb.append(entry.getValue());
                sb.append("\n");
            }
        }.call();
        return sb;
    }

    public static void tracelogclean(String date) {
        TraceLogClick.clean();
        renderText("clean two week before tracelog ...");
    }

    public static void sevenDaysFreeUser() {
        render("Application/sevendayfreeuser.html");
    }

    public static void getSevenFreeUserInfos(int pn, int ps) {
        List<ArticleBizOrderPlay> users = ArticleBizOrderDao.sevenDaysFreeUserNicks((pn - 1) * ps, ps);
        PageOffset po = new PageOffset(pn, ps, 5);
        TMResult tmRes = new TMResult(users, (int) ArticleBizOrderDao.countSevenDaysFreeUserNicks(), po);
        renderJSON(JsonUtil.getJson(tmRes));
    }

    /**
     * 需要处理差评的
     */
    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public static void getCommentDeal(String date) {
        long ts = 0;
        try {
            ts = sdf.parse(date).getTime();
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
            renderText("日期格式错误: yyyy-MM-dd");
        }
        List<CommentDeal> list = CommentDeal.find("created >= ? and created < ?", ts, ts + DateUtil.DAY_MILLIS).fetch();
        renderJSON(list);
    }

    public static void allItemJob(int offset) {
        new UpdateAllItemsJob(offset).now();
    }

    public static void setHourlyJob(boolean on) {
        HourlyCheckerJob.HOUR_JOB_ENABLE = on;
    }

    public static void listWillExpired(String start, String end, int pn, int ps)

            throws ParseException, WriteException, IOException {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = df.parse(start);
        Date endDate = df.parse(end);
        log.info("[start and end :]" + startDate + " ---- " + endDate);
        PageOffset po = new PageOffset(pn, ps, 10);

        int count = 0;

        Map<String, Long> listWillExpire = MapUtils.EMPTY_MAP;
        if (Play.mode.isDev()) {
//        if (Play.mode.isProd()) {
            listWillExpire = new HashMap<String, Long>();
            listWillExpire.put("just_001", System.currentTimeMillis() + DateUtil.DAY_MILLIS);
            count = 1;
        } else {
            count = ArticleBizOrderDao.countWillExpire(startDate.getTime(), endDate.getTime());
            listWillExpire = ArticleBizOrderDao.listWillExpire(startDate.getTime(), endDate.getTime(), po);
        }

        final List<User> users = new ArrayList<User>();
//        final List<UserTimestampMsg> list = new ArrayList<UserTimestampMsg>();
        new MapIterator<String, Long>(listWillExpire) {
            @Override
            public void execute(Entry<String, Long> entry) {
//                list.add(new UserTimestampMsg(0L, entry.getValue(), entry.getKey()));
                String nick = entry.getKey();
                User user = UserDao.findByUserNick(nick);
                if (user == null) {
                    return;
                }
                if (user.isBlackList()) {
                    return;
                }
                user.setTs(entry.getValue());
                users.add(user);
            }
        }.call();

        TMResult res = new TMResult(users, (int) count, po);
        renderJSON(JsonUtil.getJson(res));
    }

    public static void expire() {
        render("op/userexpired.html");
    }

    public static void fiveYuanCRUDLink(String wangwang) {
        if (StringUtils.isEmpty(wangwang)) {
            renderText("请输出用户旺旺");
        }
        User user = UserDao.findByUserNick(wangwang);
        if (user == null) {
            renderText("不存在对应用户");
        }
        String link = StringUtils.EMPTY;
        String paramStr = genFiveYuanParamStr(user.getVersion());
        if (paramStr.isEmpty()) {
            renderText("获取5元链接出错");
        }
        link = new bustbapi.FuwuApis.SaleLinkGenApi(APIConfig.get().getApiKey(), APIConfig.get().getSecret(),
                wangwang, paramStr).call();
        renderText(link);
    }

    public static String genFiveYuanParamStr(int version) {
        String paramStr = StringUtils.EMPTY;
        String appkey = APIConfig.get().getApiKey();
        // 淘掌柜
        if (appkey.equals("21255586")) {
            if (version <= 20) {
                paramStr = TMConfigs.YINGXIAO.TAOZHANGGUI_FIVE_YUAN_MONTH_VERSION_20;
            } else if (version <= 30) {
                paramStr = TMConfigs.YINGXIAO.TAOZHANGGUI_FIVE_YUAN_MONTH_VERSION_30;
            } else if (version <= 40) {
                paramStr = TMConfigs.YINGXIAO.TAOZHANGGUI_FIVE_YUAN_MONTH_VERSION_40;
            }
        }
        // 自动标题
        else if (appkey.equals("21348761")) {
            if (version <= 20) {
                paramStr = TMConfigs.YINGXIAO.TAOXUANCI_FIVE_YUAN_MONTH_VERSION_20;
            } else if (version <= 30) {
                paramStr = TMConfigs.YINGXIAO.TAOXUANCI_FIVE_YUAN_MONTH_VERSION_30;
            } else if (version <= 40) {
                paramStr = TMConfigs.YINGXIAO.TAOXUANCI_FIVE_YUAN_MONTH_VERSION_40;
            } else if (version <= 50) {
                paramStr = TMConfigs.YINGXIAO.TAOXUANCI_FIVE_YUAN_MONTH_VERSION_50;
            } else if (version <= 60) {
                paramStr = TMConfigs.YINGXIAO.TAOXUANCI_FIVE_YUAN_MONTH_VERSION_60;
            }
        }
        // 差评防御师
        else if (appkey.equals("21404171")) {
            if (version <= 20) {
                paramStr = TMConfigs.YINGXIAO.DEFENDER_FIVE_YUAN_MONTH_VERSION_20;
            } else if (version <= 30) {
                paramStr = TMConfigs.YINGXIAO.DEFENDER_FIVE_YUAN_MONTH_VERSION_30;
            } else if (version <= 40) {
                paramStr = TMConfigs.YINGXIAO.DEFENDER_FIVE_YUAN_MONTH_VERSION_40;
            }
        }
        return paramStr;
    }

    public static void getChapingSpecialLink(String userNick, String interval) {
        if (userNick.isEmpty()) {
            renderText("用户名为空");
        }
        User user = UserDao.findByUserNick(userNick);
        if (user == null) {
            renderText("找不到对应用户");
        }
        String key = TMConfigs.YINGXIAO.CHAPINGSPECIALPREFIX + (user.getVersion() == -1 ? 20 : user.getVersion())
                + interval;
        String link = TMConfigs.YINGXIAO.chapingMap.get(key);

        if (link == null || link.isEmpty()) {
            renderText("获取营销链接出错");
        }
        renderText(link);
    }

    public static void initCacheWindow(int offset) {
        new DropCacheInitJob(offset).now();
    }

    public static void setTuiguangInterval(long millis) {
        HourlyCheckerJob.intervalMillis = millis;
    }

    public static void testSpiderHotItems() {
        SpiderHotItemThread spiderJob = new SpiderHotItemThread();
        spiderJob.now();
    }

    public static void setEnableRawIds(boolean enable) {
        TMConfigs.Rds.checkUserWithInRawId = enable;
    }

    public static void setEnableJdpApi(boolean enable) {
        TMConfigs.Rds.enableJdpApi = enable;
    }

    public static void queryDefenseLogs(String wangwang) {
        if (StringUtils.isBlank(wangwang)) {
            renderJSON("");
        }
        User user = UserDao.findByUserNick(wangwang.trim());
        if (user == null) {
            renderJSON("");
        }

        List<DefenseLog> list = DefenseLogDao.findDefenseLogByUserId(user.getId());
        renderJSON(JsonUtil.getJson(list));
    }

    public static void setTMCUrl(String url) {
        TMApi.TMC_WORD_URL = url;
    }
    public static void setRecentDownCheck(boolean enable) {
        RecentDownCacheJob.recentDownCheck = enable;
    }
}
