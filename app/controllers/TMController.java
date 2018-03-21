package controllers;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import models.order.OrderDisplay;
import models.trade.TradeDisplay;
import models.user.User;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.cache.Cache;
import play.libs.Crypto;
import play.mvc.After;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;
import result.TMPaginger;
import result.TMResult;
import search.SearchManager;
import secure.SimulateRrequest;
import secure.SimulateRrequest.LogType;
import secure.SimulateRrequest.Param;
import secure.SimulateRrequest.ResultRisk;
import utils.DateUtil;
import utils.UserCache;
import actions.SubcribeAction;
import bustbapi.ItemApi;
import bustbapi.UserAPIs;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.PYFutureTaskPool;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.NetworkUtil;
import com.ciaosir.client.utils.NumberUtil;
import com.google.gson.Gson;

import configs.TMConfigs;
import configs.TMConfigs.App;
import configs.TMConfigs.WebParams;
import dao.UserDao;

public class TMController extends Controller {

    public static final Logger log = LoggerFactory.getLogger(TMController.class);

    public static final String TAG = "TMController";

    private static final int THREAD_SIZE = 32;

    private static PYFutureTaskPool<Boolean> threadPool = new PYFutureTaskPool<Boolean>(THREAD_SIZE);

    @Before
    public static void startTime() {
        //        log.info("Request For " + request.url + ":" + request.action + " Starts");
        request.args.put("_ts", System.currentTimeMillis());
    }

    @After
    public static void endTime() {
        log.info("Action [" + request.url + "] took "
                + (System.currentTimeMillis() - (Long) request.current().args.get("_ts")) + " ms");
    }

    public static UserCache checkEndTimeCache(long userId) {
        boolean flag = false;

        UserCache usercache = (UserCache) Cache.get(Long.toString(userId));
        
        if(usercache == null) {
            usercache = new UserCache();
            usercache.setFlag(flag);
            usercache.setRunningEndTime(0);
        }
        else {
            usercache.setRunningEndTime(usercache.getRunningEndTime());
            usercache.setFlag(usercache.isFlag());
        }
        
        return usercache;
    }

    @Before(unless = { "EXFactory.setAllContents", "EXFactory.setExactCatId", "Words.base", "Words.id", "Words.ids",
            "Words.word", "Words.words", "Words.getWordBaseList", "Words.fetch", "Words.fetchFt", "Words.equal",
            "Words.query", "Words.queryFt", "Words.fetchAll" , "Diag.pcItem", "Diag.appItem"})
    protected static void check() {
        APIConfig.get().beforeReq(request, response, session, params);

        String sid = StringUtils.EMPTY;
        String paramSid = params.get("sid");
        if (Play.mode.isDev() || "6101b134157a708ebf2751c62e85c9d8e56f11026c9e50879742174".equals(paramSid)) {
            sid = params.get("_tms");
            if (StringUtils.isEmpty(sid)) {
                sid = params.get("sid");
            }
        }
        if (request.url.indexOf("_eu=831712c4e3feea8321edbdebf1eeb4df") >= 0) {
            redirect(App.CONTAINER_TAOBAO_URL);
        }

        if (StringUtils.isEmpty(sid)) {
            sid = session.get(WebParams.SESSION_USER_KEY);
        }
        if (StringUtils.isEmpty(sid)) {
        	sid = CommonUtils.getCookieString(request, WebParams.SESSION_USER_KEY);
        }

        String encodedUid = null;
        //        encodedUid = params.get(WebParams.COOKIE_ENCODE_USER_ID);
        if (StringUtils.isEmpty(encodedUid)) {
            encodedUid = session.get(WebParams.COOKIE_ENCODE_USER_ID);
        }

        if (StringUtils.isEmpty(encodedUid)) {
            encodedUid = CommonUtils.getCookieString(request, WebParams.COOKIE_ENCODE_USER_ID);
        }

        //        if (StringUtils.isEmpty(sid)) {
        //            sid = CommonUtils.getCookieString(request, WebParams.SESSION_USER_KEY);
        //        }
        //        log.error(" action :" + request.actionMethod);

        //        log.info(" action :" + request.action + " with sid :" + sid + " encodeed uid :" + encodedUid);
        if (!APIConfig.get().isAllow(request.action)) {
            forbidden();
        }
        User user = UserDao.findBySessionKey(sid);
//        if (user != null && user.isVaild()) {
        if (user != null) {
            putUser(user);
            return;
        }

        //      MixHelpers.infoAll(request, response);

        if (!StringUtils.isEmpty(sid)) {
            com.taobao.api.domain.User calledUser = new UserAPIs.UserGetApi(sid, null).call();
            if (calledUser != null) {
                user = UserDao.findById(calledUser.getUserId());
                if (user == null) {
                    user = UserDao
                            .addUser(calledUser, calledUser.getNick(), calledUser.getUserId(), sid, true, 0, null);
                } else {
                    user.setSessionKey(sid);
                }
                //user = UserDao.findById(user.getId());
            } else {
                //log.warn("No User Info :" + request.url);
                //redirect(App.CONTAINER_TAOBAO_URL);
            }
        }

        //        else {
        //            if (Play.mode.isDev()) {
        //                user = User.find(" 1 = 1 ").first();
        //            } else {
        //                log.warn("No User Info :" + request.url);
        //                redirect(App.TAOBAO_URL);
        //            }
        //        }

        if (user != null) {
            //            session.clear();
            putUser(user);
            return;
        }

        //        log.warn(" encode uid :" + encodedUid);

        if (!StringUtils.isEmpty(encodedUid)) {
            try {
                Long uid = NumberUtil.parserLong(Crypto.decryptAES(encodedUid), -1L);
                user = UserDao.findById(uid);
//                if (user != null && user.isVaild()) {
                if (user != null) {
                    putUser(user);
                    return;
                }
            } catch (Exception e) {
                log.warn(e.getMessage(), e);

            }
        }

        redirect(App.CONTAINER_TAOBAO_URL);
    }

    static User tryFindUser() {

        User user = null;
        String sid = session.get(WebParams.SESSION_USER_KEY);
        if (StringUtils.isEmpty(sid)) {
            sid = params.get("sid");
        }
        if (StringUtils.isEmpty(sid)) {
            sid = CommonUtils.getCookieString(request, WebParams.SESSION_USER_KEY);
            user = UserDao.findBySessionKey(sid);
        }
        if (user != null) {
            return user;
        }

        String encodedUid = params.get(WebParams.COOKIE_ENCODE_USER_ID);
        if (StringUtils.isEmpty(encodedUid)) {
            encodedUid = session.get(WebParams.COOKIE_ENCODE_USER_ID);
        }

        log.error(" find :" + encodedUid);
        if (StringUtils.isEmpty(encodedUid)) {
            return null;
        }

        Long uid = NumberUtil.parserLong(Crypto.decryptAES(encodedUid), -1L);
        user = UserDao.findById(uid);
//        if (user == null || !user.isVaild()) {
        if (user == null) {
            return null;
        }

        return user;
    }

    protected static void putUser(models.user.User user) {
        // TODO Auto-generated method stub
        //        session.put(Params.SESSION_USER_ID, String.valueOf(user.getId()));
        //        session.put(Params.SESSION_USER_NICK, String.valueOf(user.getUserNick()));
        //        session.put(Params.SESSION_USER_LEVEL, String.valueOf(user.getLevel()));

        //        session.put(WebParams.SESSION_USER_KEY, String.valueOf(user.getSessionKey()));
        response.setCookie(WebParams.SESSION_USER_KEY, user.getSessionKey(), "120h");
        response.setCookie(WebParams.SESSION_USER_VERSION, String.valueOf(user.getVersion()), "120h");
        try {
            String encodeUid = Crypto.encryptAES(user.getId().toString());
            response.setCookie(WebParams.COOKIE_ENCODE_USER_ID, encodeUid, "120h");
            response.setCookie(WebParams.SESSION_USER_NICK, URLEncoder.encode(user.getUserNick(), "utf-8"), "120h");
            session.put(WebParams.COOKIE_ENCODE_USER_ID, encodeUid);
            session.put(WebParams.SESSION_USER_KEY, String.valueOf(user.getSessionKey()));

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }

        //        session.put(Params.CREATED, String.valueOf(user.getFirstLoginTime()));
        //        session.put(Params.SHIELD, String.valueOf(Datacheckaction.Isinblacklist(user.getUserNick())));
        request.args.put(WebParams.ARGS_USER, user);
        //        response.setCookie(WebParams.SESSION_USER_KEY, user.getSessionKey(), "7d");
        log.info("[User has been put..]" + user);
    }

    static void clearUser() {
        session.remove(WebParams.SESSION_USER_ID, WebParams.SESSION_USER_NICK, WebParams.SESSION_USER_LEVEL,
                WebParams.SESSION_USER_KEY);
        response.removeCookie(WebParams.SESSION_USER_KEY);
        response.removeCookie(WebParams.SESSION_USER_VERSION);
        response.removeCookie(WebParams.SESSION_USER_ID);
        response.removeCookie(WebParams.SESSION_USER_NICK);
    }

    protected static User getUser() {
        User user = (User) request.args.get(WebParams.ARGS_USER);
        return user;
    }

    protected static String getPrefUserNick() {
        User user = getUser();
        return user.getUserNick();
    }

    static void renderMockFileInJsonIfDev(String filename) throws IOException {
        if (Play.mode.isProd() || !App.isDevMock) {
            return;
        }

        String str = FileUtils.readFileToString(new File(TMConfigs.mockDir, filename));
        //        log.info("[str:]" + str);
        renderJSON(str);
    }

    protected static Client getSClient() {
        return SearchManager.getIntance().getClient();
    }

    protected static void renderUISuccess() {
        renderJSON(JsonUtil.getJson(TMPaginger.makeEmptyOk()));
    }

    protected static void renderUIErrorMessage(String msg) {
        renderJSON(JsonUtil.getJson(TMPaginger.makeEmptyFail(msg)));
    }

    static void renderJs(String s) {
        response.setContentTypeIfNotSet("application/x-javascript;charset=" + Http.Response.current().encoding);
        renderText(s);
    }

    static byte[] imgBytes = null;

    static byte[] ensureImgBytes() {
        if (imgBytes != null) {
            return imgBytes;
        }

        try {
            imgBytes = FileUtils.readFileToByteArray(new File(new File(new File(Play.applicationPath, "public"),
                    "images"), "favicon.png"));

        } catch (IOException e) {
            log.warn(e.getMessage(), e);

        }

        return imgBytes;
    }

    static void renderEmptyImg() {
        response.setContentTypeIfNotSet("image/png");
        try {
            byte[] bs = ensureImgBytes();
            response.out.write(bs);
            response.setHeader("Content-Length", bs.length + "");
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }
        ok();
    }

    protected static void checkIsZhizun() {
        if (!SubcribeAction.isVIP(getUser())) {
            renderJSON(JsonUtil.getJson(new TMResult(false, "亲，要升级到至尊版才能使用该功能哟", false)));
        }
    }

    protected static void renderError(String msg) {
        TMResultMsg wmMsg = new TMResultMsg();
        wmMsg.setSuccess(false);
        wmMsg.setMessage(msg);
        renderJSON(JsonUtil.getJson(wmMsg));
    }

    protected static void renderAuthorizedError(String msg, String authorizeUrl) {
        TMResultMsg wmMsg = new TMResultMsg();
        wmMsg.setSuccess(false);
        wmMsg.setMessage(msg);
        wmMsg.setTaobaoAuthorizeUrl(authorizeUrl);
        renderJSON(JsonUtil.getJson(wmMsg));
    }

    protected static void renderSuccess(String msg, Object res) {
        TMResultMsg wmMsg = new TMResultMsg();
        wmMsg.setSuccess(true);
        wmMsg.setMessage(msg);
        wmMsg.setRes(res);
        renderJSON(JsonUtil.getJson(wmMsg));
    }

    protected static void renderTMSuccess(String msg) {
        TMResultMsg wmMsg = new TMResultMsg();
        wmMsg.setSuccess(true);
        wmMsg.setMessage(msg);
        wmMsg.setRes(null);
        renderJSON(JsonUtil.getJson(wmMsg));
    }

    protected static void renderResultJson(Object res) {
        TMResultMsg wmMsg = new TMResultMsg();
        wmMsg.setSuccess(true);
        wmMsg.setRes(res);
        renderJSON(JsonUtil.getJson(wmMsg));
    }

    /**
     * 消息
     * @author Administrator
     *
     */
    public static class TMResultMsg {
        private boolean success = false;

        private String message;

        private String taobaoAuthorizeUrl;

        private Object res;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Object getRes() {
            return res;
        }

        public void setRes(Object res) {
            this.res = res;
        }

        public String getTaobaoAuthorizeUrl() {
            return taobaoAuthorizeUrl;
        }

        public void setTaobaoAuthorizeUrl(String taobaoAuthorizeUrl) {
            this.taobaoAuthorizeUrl = taobaoAuthorizeUrl;
        }

    }

    static String getParam(Map<String, String[]> data, String key) {
        if (data.containsKey(key)) {
            return data.get(key)[0];
        }
        return null;
    }

    @JsonAutoDetect
    static class BusUIResult {
        @JsonProperty
        int curPage = 1;

        @JsonProperty
        int pageCount = 1;

        @JsonProperty
        boolean success = true;

        @JsonProperty
        String message = StringUtils.EMPTY;

        @JsonProperty
        boolean hasRecords = true;

        @JsonProperty
        Object results;

        public BusUIResult(Object res) {
            this.results = res;
        }

        public BusUIResult(Boolean success, Object res, String message) {
            this.success = success;
            this.results = res;
            this.message = message;
        }

        public BusUIResult(int curPage, int pageCount, Object res) {
            this.results = res;
            this.curPage = curPage;
            this.pageCount = pageCount;
        }

        public BusUIResult(int curPage, int pageCount, Object res, String message) {
            this.results = res;
            this.curPage = curPage;
            this.pageCount = pageCount;
            this.message = message;
        }

        public BusUIResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public BusUIResult(boolean success, boolean hasRecords, String message) {
            this.success = success;
            this.hasRecords = hasRecords;
            this.message = message;
        }
    }

    protected static void renderBusJson(Object json) {
        renderJSON(JsonUtil.getJson(new BusUIResult(json)));
    }

    protected static void renderFailedJson(String message) {
        renderJSON(JsonUtil.getJson(new BusUIResult(false, message)));
    }

    protected static void renderSuccessJson() {
        renderJSON(JsonUtil.getJson(new BusUIResult(true, "")));
    }

    protected static void renderSuccessJson(String msg) {
        renderJSON(JsonUtil.getJson(new BusUIResult(true, msg)));
    }

    static boolean isAutoOnekeyOK() {
        // return isFirstLoginTime < 昨天  || itemNum < 当前的版本
        User user = getUser();
        long onsaleCount = new ItemApi.ItemsOnsaleCount(user.getSessionKey(), null, null).call();
        //long inventoryCount = new ItemApi.ItemsInventoryCount(user, null, null).call();
        //long totalCount = onsaleCount + inventoryCount;
        long totalCount = onsaleCount;
        int userVersionCount = APIConfig.get().getMaxAvailable(user);
        return (user.firstLoginTime < (System.currentTimeMillis() - DateUtil.DAY_MILLIS))
                || (totalCount <= userVersionCount);

    }

    static long totalCount() {
        User user = getUser();
        long onsaleCount = new ItemApi.ItemsOnsaleCount(user.getSessionKey(), null, null).call();
        //long inventoryCount = new ItemApi.ItemsInventoryCount(user, null, null).call();
        //return onsaleCount + inventoryCount;
        return onsaleCount;
    }

    static int userVersionCount() {
        User user = getUser();
        return APIConfig.get().getMaxAvailable(user);
    }

    /**
     * 御城河日志接入之top调用日志
     */
    static void sendTopLog(Long userId, String topApiName, String apiParameter) {
        if (!APIConfig.get().isRisk()) {
            return;
        }
        if (request == null) {
            return;
        }

        String userIp = NetworkUtil.getRemoteIPForNginx(request);
        String ati = CommonUtils.getCookieString(request, WebParams.ATI);
        String url = topApiName + apiParameter;
        String topAppKey = APIConfig.get().getApiKey();
        String appName = APIConfig.get().getAppName();
        Param param = new SimulateRrequest.Param(ati, userId, userIp, topAppKey, appName, url);
        threadPool.submit(new SimulateRrequest(param, LogType.TOP));
    }

    /**
     * 御城河日志接入之数据库访问日志
     */
    static void sendSqlLog(Long userId, String db, String sql) {
        if (!APIConfig.get().isRisk()) {
            return;
        }
        String userIp = NetworkUtil.getRemoteIPForNginx(request);
        String ati = CommonUtils.getCookieString(request, WebParams.ATI);
        String url = request.url;
        String topAppKey = APIConfig.get().getApiKey();
        String appName = APIConfig.get().getAppName();
        Param param = new SimulateRrequest.Param(ati, userId, userIp, topAppKey, appName, url, db, sql);
        threadPool.submit(new SimulateRrequest(param, LogType.SQL));
    }

    /**
     * 御城河日志接入之订单访问日志
     */
    static void sendOrderLog(Long userId, String operation, List<Long> tradeIds) {
        if (!APIConfig.get().isRisk()) {
            return;
        }
        if(CommonUtils.isEmpty(tradeIds)) {
        	return;
        }
        String userIp = NetworkUtil.getRemoteIPForNginx(request);
        String ati = CommonUtils.getCookieString(request, WebParams.ATI);
        String url = request.url;
        String topAppKey = APIConfig.get().getApiKey();
        String appName = APIConfig.get().getAppName();
        Param param = new SimulateRrequest.Param(ati, userId, userIp, topAppKey, appName, url, tradeIds, operation);
        threadPool.submit(new SimulateRrequest(param, LogType.ORDER));
    }

    /**
     * 御城河日志接入之账号系统风险控制登录日志
     */
    static void sendLoginLog(String userId, String tid, boolean loginResult, String loginMessage) {
        String userIp = NetworkUtil.getRemoteIPForNginx(request);
        String ati = CommonUtils.getCookieString(request, WebParams.ATI);
        String topAppKey = APIConfig.get().getApiKey();
        String appName = APIConfig.get().getAppName();
        Param param = new SimulateRrequest.Param(ati, userId, userIp, topAppKey, appName, tid, loginResult,
                loginMessage);
        threadPool.submit(new SimulateRrequest(param, LogType.ACCOUNT_LOGIN));
    }

    /**
     * 风险计算(computeRisk)接口
     */
    static ResultRisk computeRisk(String userId) {
        String userIp = NetworkUtil.getRemoteIPForNginx(request);
        String ati = CommonUtils.getCookieString(request, WebParams.ATI);
        String topAppKey = APIConfig.get().getApiKey();
        String appName = APIConfig.get().getAppName();
        Param param = new SimulateRrequest.Param(ati, userId, userIp, topAppKey, appName);
        String resultPost = new SimulateRrequest().sendLog(param, LogType.COMPUTE_RISK);
        return new Gson().fromJson(resultPost, SimulateRrequest.ResultRisk.class);
    }

    /**
     * 获取二次验证地址
     */
    static ResultRisk getVerifyUrl(String userName, String sessionId, String mobile, String redirectURL) {
        String userIp = NetworkUtil.getRemoteIPForNginx(request);
        String ati = CommonUtils.getCookieString(request, WebParams.ATI);
        String topAppKey = APIConfig.get().getApiKey();
        String appName = APIConfig.get().getAppName();
        Param param = new SimulateRrequest.Param(ati, userName, userIp, topAppKey, appName, sessionId, mobile,
                redirectURL);
        String resultPost = new SimulateRrequest().sendLog(param, LogType.GET_VERIFY_URL);
        return new Gson().fromJson(resultPost, SimulateRrequest.ResultRisk.class);
    }

    /**
     * 判断验证是否通过
     */
    static ResultRisk isVerifyPassed(String token) {
        String resultPost = new SimulateRrequest().sendLog(new SimulateRrequest.Param(token), LogType.IS_VERIFY_PASSED);
        return new Gson().fromJson(resultPost, SimulateRrequest.ResultRisk.class);
    }

    /**
     * 风险重置
     */
    static ResultRisk resetRisk(String userName) {
        String userIp = NetworkUtil.getRemoteIPForNginx(request);
        String ati = CommonUtils.getCookieString(request, WebParams.ATI);
        String topAppKey = APIConfig.get().getApiKey();
        String appName = APIConfig.get().getAppName();
        Param param = new SimulateRrequest.Param(ati, userName, userIp, topAppKey, appName);
        String resultPost = new SimulateRrequest().sendLog(param, LogType.RESET_RISK);
        return new Gson().fromJson(resultPost, SimulateRrequest.ResultRisk.class);
    }
    
    static void daysBetween(User user, int interval){
    	Long nowTime = System.currentTimeMillis();
        Long firstLoginTime = user.getFirstLoginTime();
        long limitTime = DateUtil.formNextDate(firstLoginTime) + 8 * DateUtil.ONE_HOUR_MILLIS;
        
        int daysBetween = 0;
        if(nowTime >= limitTime) {
        	daysBetween = (int) ((nowTime - limitTime) / DateUtil.DAY_MILLIS) + 1;
        }
        if(daysBetween < interval){
        	if(daysBetween == 0) {
        		renderFailedJson("亲才刚开始使用软件不久，请于订购第二天8点后再来看看吧");
        	} else {
        		renderFailedJson("亲开始使用软件时间不到 "+ (daysBetween + 1) +" 天，只能查看 "+ daysBetween +" 天的数据，请重试");
        	}
        }
    }

}
