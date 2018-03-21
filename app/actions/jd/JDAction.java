
package actions.jd;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.Map;

import job.click.HourlyCheckerJob;
import models.jd.JDUser;
import models.user.User;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.libs.WS;
import play.mvc.results.Redirect;

import com.ciaosir.client.utils.NumberUtil;
import com.google.gson.JsonObject;
import com.jd.open.api.sdk.DefaultJdClient;
import com.jd.open.api.sdk.JdClient;

import configs.Subscribe.Version;
import controllers.APIConfig;
import controllers.JDPromoteSite;
import controllers.JDTuiguang;

public class JDAction {

    private static final Logger log = LoggerFactory.getLogger(JDAction.class);

    public static final String TAG = "JDLoginAction";

    public static JDAction _instance = new JDAction();

    public JDAction() {
    }

    public static JDAction get() {
        return _instance;
    }

    private static JDAPIConfig jdLiuliangConfig = new JDAPIConfig("D46E44FFCE509817D2B6D9FB9106BE05",
            "8eea4cfb5abf4a46b520ca45e9748496", "http://www.jujintuan.com/in/jd",
            "http://auth.360buy.com/oauth/authorize?response_type=code",
            "http://auth.360buy.com/oauth/token?grant_type=authorization_code&client_id=");

    private static JDAPIConfig jdWangkeCOnfig = new JDAPIConfig("185825C619AEC828FF3A5B900F189269",
            "32e5ec214c1b4779b7f0683223418cc2", "http://wangke.taovgo.com/in/jd",
            "http://auth.360buy.com/oauth/authorize?response_type=code",
            "http://auth.360buy.com/oauth/token?grant_type=authorization_code&client_id=");

    private static JDAPIConfig jdShoucangConfig = new JDAPIConfig("E080F22459AD987CBB932B3EBC456EA0",
            "84525061396d465ca5fcb494ab8e1768", "http://wangkeshoucang.taovgo.com/in/jd",
            "http://auth.360buy.com/oauth/authorize?response_type=code",
            "http://auth.360buy.com/oauth/token?grant_type=authorization_code&client_id=");

    /**
     * 沙箱环境地址：Mini沙箱环境首页：http://mini.sandbox.360buy.com 沙箱开发者访问地址：http://dev.sandbox.360buy.com
     * 沙箱接口访问地址：http://gw.api.sandbox.360buy.com/routerjson 沙箱授权地址：http://auth.sandbox.360buy.com App
     * Key：713069A60D93B4FD45F334C444C1ECE1 App Secret：e737e39524ae460e961f1314f82f77f1
     */

    private static JDAPIConfig jdSandBoxConfig = new JDAPIConfig("B08700091DAF25FD08329DA311B212BC",
            "6435497c3ff64f789f430f72be6402dd", "http://127.0.0.1:9000/in/jd",
            "http://auth.sandbox.360buy.com/oauth/authorize?response_type=code",
            "http://auth.sandbox.360buy.com/oauth/token?grant_type=authorization_code&client_id=");

    public static JDAPIConfig jdApiConfig = jdLiuliangConfig;
    static {
        String newKey = Play.configuration.get("app.key").toString();
        if (jdWangkeCOnfig.getApiKey().equals(newKey)) {
            jdApiConfig = jdWangkeCOnfig;
        } else if (jdLiuliangConfig.getApiKey().equals(newKey)) {
            jdApiConfig = jdLiuliangConfig;
        } else if (jdShoucangConfig.getApiKey().equals(newKey)) {
            jdApiConfig = jdShoucangConfig;
        }

        log.info("[jd api config:]" + jdApiConfig.toString());
    }

    public static class JDAPIConfig extends APIConfig {
        String appKey;

        String appSecret;

        String callback;

        String redirLoginUrl;

        String verifyCodeUrl;

        public JDAPIConfig(String appKey, String appSecret, String callback, String redirLoginUrl, String verifyCodeUrl) {
            super(appKey.hashCode(), appSecret);
            this.appKey = appKey;
            this.appSecret = appSecret;
            this.callback = callback;
            this.redirLoginUrl = redirLoginUrl;
            this.verifyCodeUrl = verifyCodeUrl;

            this.tryCodesSet.add("FW_GOODS-31802-1");
            this.freeCodesSet.add("FW_GOODS-31802-2");
            this.VIPCodesSet.add("FW_GOODS-31802-3");
            this.superCodesSet.add("FW_GOODS-31802-4");
            this.hallCodeSet.add("FW_GOODS-31802-5");
            this.godCodeSet.add("FW_GOODS-31802-6");
            this.sunCodeSet.add("FW_GOODS-31802-7");
        }

        public String getAppKey() {
            return appKey;
        }

        public void setAppKey(String appKey) {
            this.appKey = appKey;
        }

        public String getAppSecret() {
            return appSecret;
        }

        public void setAppSecret(String appSecret) {
            this.appSecret = appSecret;
        }

        public String getCallback() {
            return callback;
        }

        public void setCallback(String callback) {
            this.callback = callback;
        }

        public String getRedirLoginUrl() {
            return redirLoginUrl;
        }

        public void setRedirLoginUrl(String redirLoginUrl) {
            this.redirLoginUrl = redirLoginUrl;
        }

        public String getVerifyCodeUrl() {
            return verifyCodeUrl;
        }

        public void setVerifyCodeUrl(String verifyCodeUrl) {
            this.verifyCodeUrl = verifyCodeUrl;
        }

        final static String[] referers = new String[] {
                "http://www.jujintuan.com"
        };

        public String[] getReferes() {
            return referers;
        }

        @Override
        public void doOnStartUpAsync() {
            HourlyCheckerJob.HOUR_JOB_ENABLE = false;
            //京东推广这个先关着
        }

        protected String[] allowedPrefix = new String[] {

                };

        final static Map<Integer, String> vernameMap = new HashMap<Integer, String>();

        @Override
        public Map<Integer, String> getVersionNameMap() {
            if (!vernameMap.isEmpty()) {
                return vernameMap;
            }

            vernameMap.put(Version.BLACK, "体验版");
            vernameMap.put(Version.FREE, "1个优质推广位");
            vernameMap.put(Version.BASE, "3个优质位+1个热销位");
            vernameMap.put(Version.VIP, "3个优质位+1个热销位");
            vernameMap.put(Version.SUPER, "5个优质位+2个热销位");
            vernameMap.put(Version.HALL, "10个优质位+3个热销位");
            vernameMap.put(Version.GOD, "20个优质位+8个热销位");
            vernameMap.put(Version.SUN, "30个优质位+10个热销位");
            // "至尊不限量版"

            return vernameMap;
        }

        @Override
        public Map<Integer, Integer> getTuiguangCountMap() {
            if (!verCountMap.isEmpty()) {
                return verCountMap;
            }
            verCountMap.put(Version.BLACK, 1);
            verCountMap.put(Version.FREE, 1);
            verCountMap.put(Version.BASE, 1);
            verCountMap.put(Version.VIP, 3);
            verCountMap.put(Version.SUPER, 5);
            verCountMap.put(Version.HALL, 10);
            verCountMap.put(Version.GOD, 20);
            verCountMap.put(Version.SUN, 30);
            verCountMap.put(Version.DAWEI, 999);
            return verCountMap;
        }

        final static Map<Integer, Integer> verHotCountMap = new HashMap<Integer, Integer>();

        @Override
        public Map<Integer, Integer> getHotCountMap() {
            if (!verHotCountMap.isEmpty()) {
                return verHotCountMap;
            }

            verHotCountMap.put(Version.BLACK, 0);
            verHotCountMap.put(Version.FREE, 0);
            verHotCountMap.put(Version.BASE, 1);
            verHotCountMap.put(Version.VIP, 1);
            verHotCountMap.put(Version.SUPER, 2);
            verHotCountMap.put(Version.HALL, 3);
            verHotCountMap.put(Version.GOD, 8);
            verHotCountMap.put(Version.SUN, 10);
            verHotCountMap.put(Version.DAWEI, 999);

            return MapUtils.EMPTY_MAP;
        }

        final static Map<Integer, Integer> verCountMap = new HashMap<Integer, Integer>();

        @Override
        public boolean isAllow(String action) {
            /*
             * if (StringUtils.isEmpty(action)) { return true; } String target = action.toLowerCase(); for (String
             * prefix : allowedPrefix) { if (target.startsWith(prefix)) { return true; } } return false;
             */

            return true;
        }

        public String getName() {
            return "京东精品";
        }

        @Override
        public void beforeLogin() {
            JDPromoteSite.weigou();
        }

        @Override
        public void afterLogin(User user, String itemCode, boolean isFirst, Boolean isQianniu) {
            // TODO Auto-generated method stub
            JDTuiguang.index();
        }

        public Platform getPlatform() {
            return Platform.jingdong;
        }

    }

    public int getVersion(JDUser user) {
        return Version.HALL;
    }

//    public int getVersion(JDUser user) {
//        boolean isValid = false;
//        int version = Version.FREE;
//        List<PaiPaiSubscribe> subList = new PaiPaiSubscribeApi.PaiPaiSubscribeListApi(user).call();
//        
//        if(user.getId()==301074800){
//            return 1;
//        }
//        
//        if (CommonUtils.isEmpty(subList)) {
//            user.setVersion(version);
//            user.setValid(isValid);
//            user.jdbcSave();
//            return Version.FREE;
//        }
//
//        // long recent = Long.MAX_VALUE;
//        long ts = System.currentTimeMillis();
//
//        for (PaiPaiSubscribe sub : subList) {
//            // if(recent > sub.getDeadLine()) {
//
//            int tmp = Subscribe.getVersionByCode(String.valueOf(sub.getChargeItemId()));
//            if (tmp > version && ts < sub.getDeadLine()) {
//                version = tmp;
//            }
//
//            if (ts < sub.getDeadLine()) {
//                isValid = true;
//            }
//            // recent = sub.getDeadLine();
//            // }
//        }
//
//        user.setVersion(version);
//        user.setValid(isValid);
//        user.jdbcSave();
//        
//        return version;
//    }

    /**
     * 
     * a)Mini沙箱环境首页:http://mini.sandbox.360buy.com b)沙箱接口访问地址:http://gw.api.sandbox.360buy.com/routerjson
     * c)沙箱开发者访问地址:http://dev.sandbox.360buy.com http://gw.api.360buy.com/routerjson
     */
    public static String serverUrl = "http://gw.api.sandbox.360buy.com/routerjson";

    /**
     * App Key： App Secret：8eea4cfb5abf4a46b520ca45e9748496
     * 
     * @return
     */
    public String buildRedir() {
        String origin = jdApiConfig.getRedirLoginUrl() + "&client_id=%s&redirect_uri=%s&state=taovgo&login=false";

        String url = String.format(origin, jdApiConfig.getAppKey(), jdApiConfig.getCallback());

        return url;
    }

    public JDUser doLoginByCode(String code) {

        log.info(format("verifyCode:code".replaceAll(", ", "=%s, ") + "=%s", code));

        if (StringUtils.isEmpty(code)) {
            throw new Redirect(buildRedir());
        }

        String url = jdApiConfig.getVerifyCodeUrl() + jdApiConfig.getAppKey() + "&client_secret="
                + jdApiConfig.getAppSecret() + "&scope=read&redirect_uri=" + jdApiConfig.getCallback() + "&code="
                + code + "&state=1234";

        JsonObject respJson = WS.url(url).post().getJson().getAsJsonObject();
        log.error("back json res:" + respJson);

        int respCode = respJson.get("code").getAsInt();
        if (respCode != 0) {
            log.info("auth failed!! error: " + respJson.get("error_description").getAsString());
            throw new Redirect(buildRedir());
        }

        String accessToken = respJson.get("access_token").getAsString();
        String refreshToken = respJson.get("refresh_token").getAsString();
        Long uid = 0L; // 26883L;
        String userNick = StringUtils.EMPTY;
        if (respJson.get("uid") != null) {
            uid = respJson.get("uid").getAsLong();
            userNick = respJson.get("user_nick").getAsString();
        }

        if (StringUtils.isEmpty(accessToken) || StringUtils.isEmpty(refreshToken) || NumberUtil.isNullOrZero(uid)) {
            // TODO, we have to re-auth..
            throw new Redirect(buildRedir());
        }

        JDUser user = JDUser.findByUserId(uid);
        if (user == null) {
            user = new JDUser(uid, accessToken, refreshToken);
            user.setNick(userNick);
        } else {
            user.setNick(userNick);
            user.setAccessToken(accessToken);
            user.setRefreshToken(refreshToken);
        }

        if (user.getVersion() < Version.HALL) {
            int version = getVersion(user);
            user.setVersion(version);
        }
        user.jdbcSave();
        log.info("[save user:]" + user);

        return user;
    }

    public JdClient genClient(String accessToken) {
        JdClient c1 = new DefaultJdClient(serverUrl, accessToken, jdApiConfig.getAppKey(), jdApiConfig.getAppSecret());
        return c1;
    }
}
