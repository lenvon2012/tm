
package controllers;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.Callable;

import job.autolist.AutoListDoingJob;
import job.showwindow.CheckNoDownShelfJob;
import job.showwindow.LightWeightQueueJob;
import job.showwindow.WindowRemoteJob;
import job.showwindow.WindowsQueueJob;
import job.writter.OpLogWritter;
import models.oplog.OpLog.LogType;
import models.showwindow.DropWindowTodayCache.CheckRecentToDropCacheJob;
import models.showwindow.ShowwindowExcludeItem;
import models.showwindow.ShowwindowMustDoItem;
import models.user.User;
import onlinefix.UpdateAllItemsJob;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.CacheFor;
import play.db.jpa.NoTransaction;
import play.mvc.Controller;
import bustbapi.PicApi;
import bustbapi.PicApi.AddPicture;
import bustbapi.TBApi;

import com.ciaosir.client.utils.DateUtil;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.NumberUtil;
import com.ning.http.util.Base64;
import com.taobao.api.domain.Picture;

import dao.UserDao;

public class SWindows extends Controller {

    private static final Logger log = LoggerFactory.getLogger(SWindows.class);

    public static final String TAG = "SWindows";

//    @Before
//    public static void startTime() {
//        log.info("Request For " + request.url + ":" + request.action + " Starts");
//        request.args.put("_ts", System.currentTimeMillis());
//    }
//
//    @After
//    public static void endTime() {
//        log.info("Action [" + request.url + "] took "
//                + (System.currentTimeMillis() - (Long) request.current().args.get("_ts")) + " ms");
//    }

    @NoTransaction
    public static void addWindowIds() {
        long id = NumberUtil.parserLong(params.get("id"), 0L);
        if (id <= 0L) {
            return;
        }
        WindowsQueueJob.addUserIdOfDeleteMsg(id);
    }

    static long lastCheck = 0L;

    @NoTransaction
    public static void checkDropCache() {
        long now = System.currentTimeMillis();
        log.info("[start to check drop cache : ]" + DateUtil.formDateForLog(System.currentTimeMillis()));
        if (now - lastCheck < 10000L) {
            return;
        }
        lastCheck = now;

        WindowRemoteJob.getPool().submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                new CheckRecentToDropCacheJob().doJob();
                return null;
            }
        });
    }

    @NoTransaction
    public static void pollOutSet() {
        Set<Long> ids = WindowsQueueJob.pollOutSet();
        renderJSON(JsonUtil.getJson(ids.toArray(NumberUtil.EMPTY_LONG_ARRAY)));
    }

    @NoTransaction
    public static void addLog() {
        Long userId = NumberUtil.parserLong(params.get("userId"), 0L);
        Long numIid = NumberUtil.parserLong(params.get("numIid"), 0L);
//        String msg = params.get("msg");
//        boolean isError = Boolean.parseBoolean("isError");

//        log.info(format("addLog:userId, numIid, msg, isError".replaceAll(", ", "=%s, ") + "=%s", userId, numIid, msg,
//                isError));
        String msg = "橱窗推荐成功";
        OpLogWritter.addMsg(userId, msg, numIid, LogType.ShowWindow, true);
    }

    @NoTransaction
    public static void enableUser() {
        Long userId = NumberUtil.parserLong(params.get("userId"), 0L);
        renderJSON(JsonUtil.getJson(UserDao.findById(userId)));
    }

    @NoTransaction
    public static void sMustIds() {
        Long userId = NumberUtil.parserLong(params.get("userId"), 0L);
        Set<Long> ids = ShowwindowMustDoItem.findIdsByUser(userId);
        renderJSON(JsonUtil.getJson(ids));
    }

    @NoTransaction
    public static void sExcludedIds() {
        Long userId = NumberUtil.parserLong(params.get("userId"), 0L);
        Set<Long> ids = ShowwindowExcludeItem.findIdsByUser(userId);
        renderJSON(JsonUtil.getJson(ids));
    }

    @NoTransaction
    public static void finish() {
        Long userId = NumberUtil.parserLong(params.get("userId"), 0L);
        User user = UserDao.findById(userId);
        notFoundIfNull(user);
    }

    @NoTransaction
    public static void addQueue() {
        Long userId = NumberUtil.parserLong(params.get("userId"), 0L);
//        WindowsQueueJob.addUserIdOfDeleteMsg(userId);
        LightWeightQueueJob.add(userId);
    }

    @NoTransaction
    public static void addQueueIds() {
        String ids = params.get("ids");
        String[] splits = StringUtils.split(ids, ',');
        if (splits == null) {
            return;
        }
        for (String string : splits) {
            Long userId = NumberUtil.parserLong(string, 0L);
            if (NumberUtil.isNullOrZero(userId)) {
                continue;
            }
            LightWeightQueueJob.add(userId);
        }
    }

    @NoTransaction
    public static void addLightWeight() {
        Long userId = NumberUtil.parserLong(params.get("userId"), 0L);
        LightWeightQueueJob.add(userId);
    }

    @NoTransaction
    public static void addItemUpdateJob(final Long userId) {

        UpdateAllItemsJob.getPool().submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                User user = UserDao.findById(userId);
                UpdateAllItemsJob.doForUser(user);
                return null;
            }
        });
    }

    /**
     * This is the main part
     */
    @NoTransaction
    public static void addLightWeightInstant() {
        Long userId = NumberUtil.parserLong(params.get("userId"), 0L);
        LightWeightQueueJob.submitUserId(userId);
    }

    @NoTransaction
    public static void addLightWeightCancelInstant() {
        final Long userId = NumberUtil.parserLong(params.get("userId"), 0L);
        final Long numIid = NumberUtil.parserLong(params.get("numIid"), 0L);

        WindowRemoteJob.getPool().submit(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                User user = UserDao.findById(userId);
                if (user != null) {
                    new CheckNoDownShelfJob(user, numIid).call();
                }
                return null;
            }
        });

    }

    @NoTransaction
    public static void doDelist(Long userId, Long numIid, int delistIndex) {
        // queue.add(userId, numIid);)

        AutoListDoingJob.doDelistTask(userId, numIid, delistIndex);
        ok();
    }

    @CacheFor("5min")
    public static void sConfigs(Long userId) {
//        log.info(format("sConfigs:userId".replaceAll(", ", "=%s, ") + "=%s", userId));

        User user = UserDao.findById(userId);

        notFoundIfNull(user);

        WindowConcigs config = WindowConcigs.build(user);
        renderJSON(JsonUtil.getJson(config));
    }

    @JsonAutoDetect
    public static class WindowConcigs {
        @JsonProperty
        Long[] mustIds;

        @JsonProperty
        Long[] excludeIds;

        @JsonProperty
        int type;

        @JsonProperty
        Long userId;

        @JsonProperty
        String sid;

        public WindowConcigs(Long[] mustIds, Long[] excludeIds, int type, Long userId, String sid) {
            super();
            this.mustIds = mustIds;
            this.excludeIds = excludeIds;
            this.type = type;
            this.userId = userId;
            this.sid = sid;

            log.info("[excluded id :]" + ArrayUtils.toString(excludeIds) + " for userId :" + userId);
        }

        public Long[] getMustIds() {
            return mustIds;
        }

        public void setMustIds(Long[] mustIds) {
            this.mustIds = mustIds;
        }

        public Long[] getExcludeIds() {
            return excludeIds;
        }

        public void setExcludeIds(Long[] excludeIds) {
            this.excludeIds = excludeIds;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getSid() {
            return sid;
        }

        public void setSid(String sid) {
            this.sid = sid;
        }

        @Override
        public String toString() {
            return "WindowConcigs [mustIds=" + Arrays.toString(mustIds) + ", excludeIds=" + Arrays.toString(excludeIds)
                    + ", type=" + type + ", userId=" + userId + ", sid=" + sid + "]";
        }

        public static WindowConcigs build(User user) {

            Long userId = user.getId();

            Set<Long> mustIds = ShowwindowMustDoItem.findIdsByUser(userId);
            Set<Long> excludeIds = ShowwindowExcludeItem.findIdsByUser(userId);

            WindowConcigs config = new WindowConcigs(mustIds.toArray(NumberUtil.EMPTY_LONG_ARRAY),
                    excludeIds.toArray(NumberUtil.EMPTY_LONG_ARRAY), user.getType(), userId, user.getSessionKey());

            return config;
        }
    }

    public static void postPicApi(String sid, int appKey, String clientType, String title, Long catId) {

        String content = params.get("content");
        byte[] bytes = Base64.decode(content);

        APIConfig app = APIConfig.getByApp(appKey);
        if (app == null) {
            badRequest();
        }
        if (catId == null) {
            badRequest();
        }

        AddPicture api = new PicApi.AddPicture(sid, bytes, title, catId, clientType);
        api.setClient(TBApi.genClient(app));
        Picture pic = api.call();
        renderJSON(JsonUtil.getJson(pic));

    }
}
