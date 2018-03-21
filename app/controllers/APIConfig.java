
package controllers;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import jdp.ApiJdpAdapter;
import job.apiget.ItemUpdateJob;
import job.apiget.TradeRateUpdateJob;
import job.apiget.TradeUpdateJob;
import job.click.HourlyCheckerJob;
import job.diagjob.PropDiagJob;
import job.ump.ShopMinDiscountGetJob;
import models.popularized.Popularized;
import models.user.User;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.Scope.Params;
import play.mvc.Scope.Session;
import titleDiag.DiagResult;
import actions.SubcribeAction;
import actions.jd.JDAction;
import bustbapi.ItemApi;
import bustbapi.JDPApi;
import bustbapi.JMSApi.JushitaJmsUserAdd;
import bustbapi.JMSApi.JushitaJmsUserGet;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.NumberUtil;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.TmcUser;

import configs.Subscribe.Version;
import configs.TMConfigs;
import configs.TMConfigs.App;
import configs.TMConfigs.WebParams;
import dao.popularized.PopularizedDao;

public abstract class APIConfig {

    private static final Logger log = LoggerFactory.getLogger(APIConfig.class);

    public static final String TAG = "APIConfig";

    public int app;

    public String apiKey;

    public String secret;

    protected boolean enableSyncTrade = false;

    public APIConfig(int app, String secret) {
        super();
        this.app = app;
        this.secret = secret;
        this.apiKey = String.valueOf(app);
    }

    public static APIConfig me;

    protected Set<String> tryCodesSet = new HashSet<String>();

    protected Set<String> freeCodesSet = new HashSet<String>();

    protected Set<String> baseCodesSet = new HashSet<String>();

    protected Set<String> llCodesSet = new HashSet<String>();
    
    protected Set<String> VIPCodesSet = new HashSet<String>();

    protected Set<String> superCodesSet = new HashSet<String>();

    protected Set<String> hallCodeSet = new HashSet<String>();

    protected Set<String> godCodeSet = new HashSet<String>();

    protected Set<String> sunCodeSet = new HashSet<String>();

    protected Set<String> daweiCodeSet = new HashSet<String>();

    protected Set<String> cuocuoCodeSet = new HashSet<String>();

    protected String subCode = StringUtils.EMPTY;

    public static APIConfig getByApp(int app) {
        for (APIConfig config : apps) {
            if (app == config.app) {
                return config;
            }
        }

        return null;
    }

    public static APIConfig setByApp(int app) {

        log.info(format("setByApp:app".replaceAll(", ", "=%s, ") + "=%s", app));

        me = taobiaoti;

        for (APIConfig config : apps) {
            if (app == config.app) {
                me = config;
                break;
            }
        }

        return me;
    }

    public final static APIConfig tddwm = new AppTaobiaoti(23082368, "52693c7d664eec1f114fc61a3c7dddda");// 淘点点外卖
    
    public final static APIConfig tzgsp = new AppTaobiaoti(23080699, "4e4f61b262365cbdf12fad503de3bda9");// 淘掌柜商品
    
    public final static APIConfig qkcrm = new AppTaobiaoti(21555946, "95c940e05022ef550cb4ac5b8c666140");//千客crm
    
    public final static APIConfig tzgTrade = new AppTaobiaoti(23074070, "d9d88d7c8463101f4f00f74d53f02f1c");//淘掌柜交易
    
    public final static APIConfig taobiaoti = new AppTaobiaoti(21255586, "04eb2b1fa4687fbcdeff12a795f863d4");

    public final static APIConfig chedaotaozhanggui = new AppTaobiaoti(21371613, "f78597dcd98face1d79157003fce7f68");

    public final static AppTaoxuanci taoxuanci = new AppTaoxuanci(21348761, "74854fd22c37b749b7d86b7fafd45a96");

    public final static APIConfig thandboxAutoTitle = new AppTaoxuanci(1021348761, "sandbox5ae2cbe4990e0a0c71bdc1733") {
        @Override
        public void afterLogin(User user, String itemCode, boolean isFirst, Boolean isQianniu) {
            Home.tbtIndex(isFirst);
        }
    };

    public final static APIConfig thandboxTianxiaomao = new AppTaoxuanci(1021568624, "sandbox216e20e5b141327b28af7b65f") {
        @Override
        public void afterLogin(User user, String itemCode, boolean isFirst, Boolean isQianniu) {
            Home.tbtIndex(isFirst);
        }
    };

    public final static APIConfig thandboxDazhe = new AppTaoxuanci(1021766941, "sandbox4394637fefd579ac9586f80e5") {
        @Override
        public void afterLogin(User user, String itemCode, boolean isFirst, Boolean isQianniu) {
            //TaoDiscount.index(user.getSessionKey(), user.getRefreshToken());
            TaoDiscount.index();
        }
    };

    public final static AppFenxiao fenxiao = new AppFenxiao(21348761, "74854fd22c37b749b7d86b7fafd45a96");

    /**
        App Key: 21383873
        App Secret: 9db522509d076d4c12a890d5e598af06
        被改造成 流量推广应用 第一版
     */
    public final static APIConfig relationSale = new AppLiuLiangTuiGuangFirst(21392618,
            "2e38e81a3659da4c11f6c0fac7e1b906");

    /**
        被改造成 爱乐购
        2013-05-16 改造成 淘微购
     */
    final public static APIConfig taovgo = new AppTaoWeigou(21405616, "dc682589d6d625e606ceaef411f5eecc");

    /**
     * 21405608
     * 0cd50b5abe0aefa2f1b6aab4afe077b
     *
     * 被改造成 爱推广
     */
//    public final static APIConfig aituiguang = new AppSkinDelist(21405608, "0cd50b5abe0aefa2f1b6aab49afe077b");
    public final static APIConfig aituiguang = new AppTaoWeigou(21405608, "0cd50b5abe0aefa2f1b6aab49afe077b");

    final static APIConfig skinWindow = new AppSkinWindow(21457299, "44129d75edcde98a3ea5aaa290e74be9");

    /**
     * wk2311帐号下，第一个 标题应用
     * http://liuliang.tobtn.com/in/login
     * 41: 9004--> tomcat-third
     */
    final static APIConfig kittytitle = new AppKittyTitle(21436342, "383b69ee3c62f3f0e0096faf83674721");

    /**
     * wk2311帐号下，第二个 标题应用 还在站外的应用
     * http://auto.tobtn.com/in/login
     */
    //final static APIConfig sheepTitle = new AppYueji(21436223, "8c48b275734e038bec0880219ff49094");

    final static APIConfig dawei880 = new AppSheepTitle(21486571, "7c630c6a980f3f10e8c8445aad711144");

    /**
     * 骑着绵羊帐号下，defender,流量汇
     * 41 --> 9002 tomcat-controller
     * fangyu.tobti.com
     */
    public final static APIConfig defender = new AppDefender(21404171, "724576dc06e80ed8e38d1ad2f6de39da");

    final static APIConfig daweishop = new AppSheepTitle(21502400, "650c5ce6dd978c6a95a37e32ce1ed5f6");

//    final static APIConfig taobiaotiWrapper = new AppTaobiaotiWrapper(21405608, "0cd50b5abe0aefa2f1b6aab49afe077b");

//    final static APIConfig taodiscount = new AppTaoDiscount(12266732, "fd132f04d231934fb83417e00e8d256b");

    final static APIConfig jinnangzhekou = new AppJinNang(12266732, "fd132f04d231934fb83417e00e8d256b");

//    final static APIConfig taobiaotiWrapper = new AppTaobiaotiWrapper(21416664, "04eb2b1fa4687fbcdeff12a795f863d4");

    /**
     * 还价不求人  12266732
     * app secret 00e433f3381ef01ee9842069c81120a4
     *
     * 翻翻乐_店铺插件 前台Appkey 12265023    后台Appkey 1226502
     * 翻翻乐--> 店铺插件
     * 12265022
     * 48b91e94491f57b8d25c8b3833c1b1c7
     *
     * 站外推广的某个流量
     * 12111104
     * 31a2ad364848a501c0c5596e3507b218
     */
    static APIConfig fanfanle = new AppTaoWeigou(12265022, "48b91e94491f57b8d25c8b3833c1b1c7");

    static APIConfig wuxifenxiao = new AppFenxiao(12111104, "31a2ad364848a501c0c5596e3507b218");
    
    
    
    static APIConfig mingDian = new AppMingDian(21436223, "8c48b275734e038bec0880219ff49094");

//
//    static class AppFenxiao extends APIConfig {
//        public AppFenxiao(int app, String secret) {
//            super(app, secret);
//        }
//
//        @Override
//        public void afterLogin(User user, String itemCode, boolean isFirst) {
//            Home.fenxiao();
//        }
//    }

    static class AppDaweiShop extends APIConfig {
        public AppDaweiShop(int app, String secret) {
            super(app, secret);
        }

        @Override
        public void afterLogin(User user, String itemCode, boolean isFirst, Boolean isQianniu) {
            Home.relation();
        }
    }

    static class AppDefender extends APIConfig {
    	
    	private String logAppkey = "68756511";
    	
    	private String logAppSecret = "w4jRdbsPUCL6zV5ji22n";
    	
    	private String rdsHostAddress = "jconnzgkrku5u.mysql.rds.aliyuncs.com";
    	
    	private String appName = "好评助手_中差评外包修改神器";
    	
    	// 是否需要接入御城河日志
    	private boolean isRisk = true;

        public String getRdsName() {
            return "jrdsygmnuupn";
        }

        public String[] getTmcTopics() {
            return defenderTmcTopic;
        }
        
        @Override
        public String getRedirURL(){
        	return "http://chaping.taovgo.com/";
        }

        public AppDefender(int app, String secret) {
            super(app, secret);
            this.subCode = "FW_GOODS-1850391";
            /**
             *   06 标准版_年付省40    20 元/月  否   可订购     06  修改
            FW_GOODS-1850391-v2     测试  1 元/月   否   不可订购    06  修改
            FW_GOODS-1850391-v4     07 皇冠/商城    400 元/月     否   可订购     07  修改
            FW_GOODS-1850391-v5     08 至尊版  500 元/月     否   可订购     08  修改
            FW_GOODS-1850391-1  默认收费项目  100 元/月     否   不可订购    10  修改
             */
            this.VIPCodesSet.add("FW_GOODS-1850391-v2");

            this.superCodesSet.add("FW_GOODS-1850391-1");
            this.superCodesSet.add("FW_GOODS-1850391-v3");

            this.hallCodeSet.add("FW_GOODS-1850391-v4");
            this.godCodeSet.add("FW_GOODS-1850391-v5");
            this.sunCodeSet.add("FW_GOODS-1850391-v6");

            this.enableSyncTrade = true;
        }

        @Override
        public void afterLogin(User user, String itemCode, boolean isFirst, Boolean isQianniu) {
            //SkinDefender.config(user.getSessionKey());
            SkinDefender.index();
        }

        @Override
        public boolean enableSyncTrade(Long userId) {
            return this.enableSyncTrade;
        }

        @Override
        public boolean enableSyncTradeRate() {
            return true;
        }

        @Override
        public void doForInstall(User user) {
            new TradeUpdateJob(user.getId(), System.currentTimeMillis()).doJob();
            new TradeRateUpdateJob(user.getId(), true).doJob();
            jdpInstall(user);
            onsAdd(user);
        }

        @Override
        public boolean doCloseTrade() {
            return true;
        }
        
        @Override
        public String getLogAppkey(){
        	return logAppkey;
        }
        
        @Override
        public String getLogAppSecret(){
        	return logAppSecret;
        }
        
        @Override
        public String getAppName(){
        	return appName;
        }
        
        @Override
        public String getRdsHostAddress(){
        	return rdsHostAddress;
        }
        
        @Override
        public boolean isRisk(){
        	return isRisk;
        }
    }

    /**
     * now, this should be the 
     * @author zrb
     *
     */
    static class AppSkinDelist extends APIConfig {

        public AppSkinDelist(int app, String secret) {
            super(app, secret);
            this.subCode = "FW_GOODS-1848326";

            this.tryCodesSet.add("FW_GOODS-1848326-v10");
            this.freeCodesSet.add("FW_GOODS-1848326-1");
            this.baseCodesSet.add("FW_GOODS-1848326-v2");
            this.VIPCodesSet.add("FW_GOODS-1848326-v3");
            this.superCodesSet.add("FW_GOODS-1848326-v4");
            this.hallCodeSet.add("FW_GOODS-1848326-v5");
            this.godCodeSet.add("FW_GOODS-1848326-v6");
            this.sunCodeSet.add("FW_GOODS-1848326-v7");
            this.daweiCodeSet.add("FW_GOODS-1848326-v8");
            this.cuocuoCodeSet.add("FW_GOODS-1848326-v9");

        }

//        static String[] aituiguangServers = new String[] {
//                "http://bbn09:9092/go/reClick", "http://bbn10:9092/go/reClick",
//        };

        public String[] getClickServers() {
            return defaultClickServers;
//            return aituiguangServers;
        }

        @Override
        public void beforeLogin() {
            Shopping.home();
//            render("shopping/shopping.html");
        }

        @Override
        public void afterLogin(User user, String itemCode, boolean isFirst, Boolean isQianniu) {
            if (user.getFirstLoginTime() != null && user.getFirstLoginTime() < 1370335747601L) {
                Aituiguang.index(user.getSessionKey());
            } else {
                Dianputuiguang.index(user.getSessionKey());
            }

//            SkinDelist.index(user.getSessionKey());
        }

        String[] allowedPrefix = new String[] {
                "in", "popularize", "items", "aituiguang", "boyvon", "home", "status", "application", "tryshare",
                "share", "op", "titles"
        };

        @Override
        public boolean isItemScoreRelated() {
            return false;
        }

        @Override
        public boolean isAllow(String action) {
            if (StringUtils.isEmpty(action)) {
                return true;
            }
            String target = action.toLowerCase();
            for (String prefix : allowedPrefix) {
                if (target.startsWith(prefix)) {
                    return true;
                }
            }
            return false;
        }

        final static String[] referers = new String[] {
                "http://www.taovgo.com"
        };

        public String[] getReferes() {
            return referers;
        }

        public String[] getReferes(long numIid) {
            return new String[] {
                    "http://www.taovgo.com/item/" + numIid + ".html?ll=2185480046"
            };
        }

        final static Map<Integer, String> vernameMap = new HashMap<Integer, String>();

        static {
//            vernameMap.put(1, "体验版1个推广位");

            vernameMap.put(Version.BLACK, "体验版1个宝贝");
            vernameMap.put(Version.FREE, "VIP版_1个优质推广位");
            vernameMap.put(10, "VIP版_2个优质推广位");
            vernameMap.put(20, "VIP版_5个优质推广位");
            vernameMap.put(30, "VIP版_10个优质推广位");
            vernameMap.put(40, "VIP版_20个优质推广位");
            vernameMap.put(50, "VIP版_30个优质推广位");
            vernameMap.put(60, "VIP版_40个优质推广位");
            vernameMap.put(70, "VIP版_50个优质推广位");
            vernameMap.put(80, "VIP版_60个优质推广位");
        }

        public Map<Integer, String> getVersionNameMap() {
            return vernameMap;
        }

        final static Map<Integer, Integer> verCountMap = new HashMap<Integer, Integer>();
        static {
            verCountMap.put(Version.BLACK, 1);
            verCountMap.put(1, 1);
            verCountMap.put(10, 2);
            verCountMap.put(20, 5);
            verCountMap.put(30, 10);
            verCountMap.put(40, 20);
            verCountMap.put(50, 30);
            verCountMap.put(60, 40);
            verCountMap.put(70, 50);
            verCountMap.put(80, 60);
        }

        public Map<Integer, Integer> getTuiguangCountMap() {
            return verCountMap;
        }

        public void doOnStartUpAsync() {
            HourlyCheckerJob.HOUR_JOB_ENABLE = false;
        }

        @Override
        public String getName() {
            return "爱推广";
        }

    }

//    static class AppTaoDiscount extends APIConfig {
//
//        public AppTaoDiscount(int app, String secret) {
//            super(app, secret);
//            this.subCode = "ts-11477";
//        }
//
//        @Override
//        public void afterLogin(User user, String itemCode, boolean isFirst) {
//            TaoDiscount.index(user.getSessionKey(), user.getRefreshToken());
//
//        }
//
//        @Override
//        public void beforeReq(Request req, Response resp, Session session, Params params) {
//            session.put(WebParams.HIDE_NAV, true);
//        }
//    }

    static class AppJinNang extends APIConfig {

        public AppJinNang(int app, String secret) {
            super(app, secret);
            this.subCode = "ts-11477";

            this.VIPCodesSet.add("ts-11477-1");
            this.superCodesSet.add("ts-11477-v2");

        }

        @Override
        public void afterLogin(User user, String itemCode, boolean isFirst, Boolean isQianniu) {

            ShopMinDiscountGetJob.addUser(user);

            //好牛的登录，如果是第一次登录，会在doForInstall中同步数据，所以这里就不同步了
            //不然batchInsert item时会报错，id重复
            if (isFirst == false) {
                new ItemUpdateJob(user.getId()).now();
            } else {
                log.info("user: " + user.getId() + ", nick: " + user.getUserNick()
                        + " is first login in haoniu-----");
            }

            //TaoDiscount.index(user.getSessionKey(), user.getRefreshToken());
            TaoDiscount.index();
        }

        @Override
        public boolean isNeedToUpdateMjsTmpl() {
            return true;
        }

        @Override
        public void beforeReq(Request req, Response resp, Session session, Params params) {
            session.put(WebParams.HIDE_NAV, true);
        }

        @Override
        public String getRedirURL() {
            return "http://dazhe.taovgo.com";
        }

        @Override
        public boolean isNeedMoreFieldsWhenSyncItem() {
            return true;
        }
    }

    static class AppSkinWindow extends APIConfig {
        public AppSkinWindow(int app, String secret) {
            super(app, secret);
        }

        @Override
        public void afterLogin(User user, String itemCode, boolean isFirst, Boolean isQianniu) {
            skinWindows.index(user.getSessionKey());
        }
    }

//    static class AppTaobiaotiWrapper extends APIConfig {
//
//        public AppTaobiaotiWrapper(int app, String secret) {
//            super(app, secret);
//        }
//
//        @Override
//        public void afterLogin(User user, String itemCode, boolean isFirst) {
//            Home.index(true, user.getSessionKey());
//            if (user.getFirstLoginTime() > 1364556641629L) {
//                WindowMustEnsueId.ensure(user.getId());
//            }
//        }
//
//        public void beforeReq(Request req, Response resp, Session session, Params params) {
//            session.put(WebParams.HIDE_NAV, true);
//        }
//    }

    static class AppTaoWeigou extends APIConfig {

        public AppTaoWeigou(int app, String secret) {
            super(app, secret);

//            this.subCode = "FW_GOODS-1848325";
//            this.tryCodesSet.add("FW_GOODS-1848325-1");
//            this.freeCodesSet.add("FW_GOODS-1848325-v2");
//            this.baseCodesSet.add("FW_GOODS-1848325-v3");
//            this.VIPCodesSet.add("FW_GOODS-1848325-v4");
//            this.superCodesSet.add("FW_GOODS-1848325-v5");
//            this.hallCodeSet.add("FW_GOODS-1848325-v6");
//            this.godCodeSet.add("FW_GOODS-1848325-v7");
//            this.sunCodeSet.add("FW_GOODS-1848325-v8");
//            this.daweiCodeSet.add("FW_GOODS-1848325-v9");

            /*----------TODO， taoweigo已经说bye bye了--- app 爱推广 ------------*/
            this.subCode = "FW_GOODS-1848326";
            this.tryCodesSet.add("FW_GOODS-1848326-v10");
            this.freeCodesSet.add("FW_GOODS-1848326-1");
            this.baseCodesSet.add("FW_GOODS-1848326-v2");
            this.VIPCodesSet.add("FW_GOODS-1848326-v3");
            this.superCodesSet.add("FW_GOODS-1848326-v4");
            this.hallCodeSet.add("FW_GOODS-1848326-v5");
            this.godCodeSet.add("FW_GOODS-1848326-v6");
            this.sunCodeSet.add("FW_GOODS-1848326-v7");
            this.daweiCodeSet.add("FW_GOODS-1848326-v8");
            this.cuocuoCodeSet.add("FW_GOODS-1848326-v9");

            /* 拍拍微购 */
            this.freeCodesSet.add("2756");
            this.VIPCodesSet.add("2749");

//            vernameMap.put(Version.BASE, "3个优质位+1个热销位");
//            vernameMap.put(Version.VIP, "5个优质位+2个热销位");
//            vernameMap.put(Version.SUPER, "10个优质位+3个热销位");
//            vernameMap.put(Version.HALL, "15个优质位+5个热销位");
//            vernameMap.put(Version.GOD, "20个优质位+8个热销位");
//            vernameMap.put(Version.SUN, "30个优质位+10个热销位");
//            vernameMap.put(Version.DAWEI, "至尊不限量版");

            log.error(" codes :" + this.baseCodesSet);
        }

        @Override
        public void afterLogin(User user, String itemCode, boolean isFirst, Boolean isQianniu) {
            Dianputuiguang.index(user.getSessionKey());
        }

        @Override
        public void beforeLogin() {
            TaoWeiGou.goIndex();
        }

        final static String[] referers = new String[] {
                "http://www.taovgo.com"
        };

        public String[] getReferes() {
            return referers;
        }

        public String[] getReferes(long numIid) {
            if (System.currentTimeMillis() % 2 == 0) {
                return new String[] {
                        "http://www.taovgo.com/item/" + numIid + ".html?ll=2185480046"
                };
            } else {
                return new String[] {
                        "http://www.taovgo.com/item/" + numIid + ".html?ll=2185480046"
                };
            }

        }

        @Override
        public String getClickUrl(Long numIid) {
            return "http://item.taobao.com/item.htm?id=" + numIid + "&ll=2185480046";
        }

        @Override
        public void doOnStartUpAsync() {
            HourlyCheckerJob.HOUR_JOB_ENABLE = true;

        }

        protected String[] allowedPrefix = new String[] {
                "in", "popularize", "items", "dianputuiguang", "home", "status", "op", "dianputuiguang", "tryshare",
                "share", "aituiguang", "skindefenselog", "xiaoqingxin", "yueji", "accountadmin"
        };

        final static Map<Integer, String> vernameMap = new HashMap<Integer, String>();

        @Override
        public Map<Integer, String> getVersionNameMap() {
            if (!vernameMap.isEmpty()) {
                return vernameMap;
            }

//            vernameMap.put(Version.BLACK, "体验版");
//            vernameMap.put(Version.FREE, "1个优质推广位");
//            vernameMap.put(Version.BASE, "3个优质位+1个热销位");
//            vernameMap.put(Version.VIP, "5个优质位+2个热销位");
//            vernameMap.put(Version.SUPER, "10个优质位+3个热销位");
//            vernameMap.put(Version.HALL, "15个优质位+5个热销位");
//            vernameMap.put(Version.GOD, "20个优质位+8个热销位");
//            vernameMap.put(Version.SUN, "30个优质位+10个热销位");
//            vernameMap.put(Version.DAWEI, "至尊不限量版");

            vernameMap.put(Version.BLACK, "体验版");
            vernameMap.put(Version.FREE, "1个优质推广位");
            vernameMap.put(Version.VIP, "3个优质位+1个热销位");
            vernameMap.put(Version.SUPER, "5个优质位+2个热销位");
            vernameMap.put(Version.HALL, "10个优质位+3个热销位");
            vernameMap.put(Version.GOD, "20个优质位+8个热销位");
            vernameMap.put(Version.SUN, "30个优质位+10个热销位");
//            "至尊不限量版"

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
            verHotCountMap.put(Version.BASE, 0);
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
            if (StringUtils.isEmpty(action)) {
                return true;
            }
            String target = action.toLowerCase();
            for (String prefix : allowedPrefix) {
                if (target.startsWith(prefix)) {
                    return true;
                }
            }
            return false;
        }

        public String getName() {
            return "微淘";
        }

        final static Map<String, String> paramStrs = new HashMap<String, String>();

        @Override
        public Map<String, String> getSellLinkParamStr() {
            if (!paramStrs.isEmpty()) {
                return paramStrs;
            }
            paramStrs
                    .put("350版本",
                            "{\"param\":{\"aCode\":\"ACT_333336410_130604231711\",\"itemList\":[\"FW_GOODS-1848326-v6:3*2\"],\"promIds\":[10065575],\"type\":1},\"sign\":\"76F03633A11B283E3F7B89474054B87D\"}");
            return paramStrs;
        }

        final static Map<String, String> newParamStrs = new HashMap<String, String>();

        // 一个月的营销链接
        /*@Override
        public Map<String, String> getNewParamStr() {
            if (!newParamStrs.isEmpty()) {
                return newParamStrs;
            }
            newParamStrs
                    .put("体验版",
                            "{\"param\":{\"aCode\":\"ACT_333336410_130714150450\",\"itemList\":[\"FW_GOODS-1848326-v10:1*2\"],\"promIds\":[10081157],\"type\":1},\"sign\":\"4E581AE757F7CD4110DDEBFC0CA25267\"}");
            newParamStrs
                    .put("1个优质推广位",
                            "{\"param\":{\"aCode\":\"ACT_333336410_130714150450\",\"itemList\":[\"FW_GOODS-1848326-1:1*2\"],\"promIds\":[10081163],\"type\":1},\"sign\":\"7D7D4B197E486C5D63E969206E4E182B\"}");
            newParamStrs
                    .put("3个优质位+1个热销位",
                            "{\"param\":{\"aCode\":\"ACT_333336410_130714150450\",\"itemList\":[\"FW_GOODS-1848326-v3:1*2\"],\"promIds\":[10081160],\"type\":1},\"sign\":\"8F3A2843DF71EE7281BD6697C43F21A6\"}");
            newParamStrs
                    .put("5个优质位+2个热销位",
                            "{\"param\":{\"aCode\":\"ACT_333336410_130714150450\",\"itemList\":[\"FW_GOODS-1848326-v4:1*2\"],\"promIds\":[10081159],\"type\":1},\"sign\":\"8AC18EE1FA48F5D61B9AA82A51930820\"}");
            newParamStrs
                    .put("10个优质位+3个热销位",
                            "{\"param\":{\"aCode\":\"ACT_333336410_130714150450\",\"itemList\":[\"FW_GOODS-1848326-v5:1*2\"],\"promIds\":[10081158],\"type\":1},\"sign\":\"4A3636EB9020D14D66C80DE0B27C72DE\"}");
            newParamStrs
                    .put("20个优质位+8个热销位",
                            "{\"param\":{\"aCode\":\"ACT_333336410_130714150450\",\"itemList\":[\"FW_GOODS-1848326-v6:1*2\"],\"promIds\":[10081162],\"type\":1},\"sign\":\"742B1568023EA36B7FBBE49CA02AAAF5\"}");
            newParamStrs
                    .put("30个优质位+10个热销位",
                            "{\"param\":{\"aCode\":\"ACT_333336410_130714150450\",\"itemList\":[\"FW_GOODS-1848326-v7:1*2\"],\"promIds\":[10081161],\"type\":1},\"sign\":\"E7B03988A1EF7862951BD5CEE7BA99A9\"}");

            return newParamStrs;
        }*/

        // 半年的营销链接
        @Override
        public Map<String, String> getNewParamStr() {
            if (!newParamStrs.isEmpty()) {
                return newParamStrs;
            }
            // 体验版直接升级成一年的一个优质推广位
            newParamStrs
                    .put("体验版",
                            "{\"param\":{\"aCode\":\"ACT_333336410_130714150450\",\"itemList\":[\"FW_GOODS-1848326-1:12*2\"],\"promIds\":[10081163],\"type\":1},\"sign\":\"F22D4BD6FE404ADD04D89080BD471265\"}");
            newParamStrs
                    .put("1个优质推广位",
                            "{\"param\":{\"aCode\":\"ACT_333336410_130714150450\",\"itemList\":[\"FW_GOODS-1848326-1:6*2\"],\"promIds\":[10081163],\"type\":1},\"sign\":\"DE6FC1EC1337636AB756A79CFE916CBE\"}");
            newParamStrs
                    .put("3个优质位+1个热销位",
                            "{\"param\":{\"aCode\":\"ACT_333336410_130714150450\",\"itemList\":[\"FW_GOODS-1848326-v3:6*2\"],\"promIds\":[10081160],\"type\":1},\"sign\":\"C23AAD355EB741FF76F59CD83369874F\"}");
            newParamStrs
                    .put("5个优质位+2个热销位",
                            "{\"param\":{\"aCode\":\"ACT_333336410_130714150450\",\"itemList\":[\"FW_GOODS-1848326-v4:6*2\"],\"promIds\":[10081159],\"type\":1},\"sign\":\"65AB986EDC384ABC159D26DA846D7DA2\"}");
            newParamStrs
                    .put("10个优质位+3个热销位",
                            "{\"param\":{\"aCode\":\"ACT_333336410_130714150450\",\"itemList\":[\"FW_GOODS-1848326-v5:6*2\"],\"promIds\":[10081158],\"type\":1},\"sign\":\"76AFCEAD46EF2515AE3896E20972D6FA\"}");
            newParamStrs
                    .put("20个优质位+8个热销位",
                            "{\"param\":{\"aCode\":\"ACT_333336410_130714150450\",\"itemList\":[\"FW_GOODS-1848326-v6:6*2\"],\"promIds\":[10081162],\"type\":1},\"sign\":\"C03D78A09D4BD4CE7900B685BC7E7A0B\"}");
            newParamStrs
                    .put("30个优质位+10个热销位",
                            "{\"param\":{\"aCode\":\"ACT_333336410_130714150450\",\"itemList\":[\"FW_GOODS-1848326-v7:6*2\"],\"promIds\":[10081161],\"type\":1},\"sign\":\"BE58DAC69DF639FCF9494E35B87266A4\"}");

            return newParamStrs;
        }
    }

    /**
     * 改造成百度搜藏
     * @author zrb
     */
    static class AppYueji extends AppTaoWeigou {

        public AppYueji(int app, String secret) {
            super(app, secret);
            this.subCode = "FW_GOODS-1855967";

//            vernameMap.put(Version.BLACK, "体验版");
//            vernameMap.put(Version.FREE, "1个优质推广位");
//            vernameMap.put(Version.BASE, "3个优质位+1个热销位");
//            vernameMap.put(Version.VIP, "5个优质位+2个热销位");

            this.freeCodesSet.add("FW_GOODS-1855967-v2");
            this.baseCodesSet.add("FW_GOODS-1855967-1");
            this.VIPCodesSet.add("FW_GOODS-1855967-v3");
            this.superCodesSet.add("FW_GOODS-1855967-v4");
            this.hallCodeSet.add("FW_GOODS-1855967-v5");
            this.godCodeSet.add("FW_GOODS-1855967-v6");
            this.sunCodeSet.add("FW_GOODS-1855967-v7");

            this.tryCodesSet.add("FW_GOODS-1855967-v8");

            log.error(" codes :" + this.baseCodesSet);
        }

        public String getName() {
            return "V淘";
        }

        @Override
        public void afterLogin(User user, String itemCode, boolean isFirst, Boolean isQianniu) {
            //Yueji.index(user.getSessionKey());
            LeShouCang.baidushoucang(user.getSessionKey());
        }

        @Override
        public void beforeLogin() {

        }

        @Override
        public boolean isAllow(String action) {

            return true;
        }

        @Override
        public String getClickUrl(Long numIid) {
            return "http://item.taobao.com/item.htm?id=" + numIid + "";
        }

    }

    static class AppLiuLiangTuiGuangFirst extends APIConfig {

        public AppLiuLiangTuiGuangFirst(int app, String secret) {
            super(app, secret);

            this.subCode = "FW_GOODS-1845420";

            //this.tryCodesSet.add("");

            this.freeCodesSet.add("FW_GOODS-1845420-1");
            this.baseCodesSet.add("FW_GOODS-1845420-v2");
            this.VIPCodesSet.add("FW_GOODS-1845420-v4");
            this.superCodesSet.add("FW_GOODS-1845420-v3");
            this.hallCodeSet.add("FW_GOODS-1845420-v5");
            this.godCodeSet.add("FW_GOODS-1845420-v6");
            this.sunCodeSet.add("FW_GOODS-1845420-v7");
        }

        @Override
        public void afterLogin(User user, String itemCode, boolean isFirst, Boolean isQianniu) {
//            Relation.relationOp();
            Popularize.popularize(user.getSessionKey());
        }

//        @Override
//        public boolean deleteAllItems() {
//            return true;
//        }

        @Override
        public boolean isItemScoreRelated() {
            return false;
        }

        final static String[] referers = new String[] {
                "http://vgou.tobtn.com/taobaoke/", "http://movie.tobbn.com/movie/", "http://www.tianyuesc.com/"
        };

        public String[] getReferes() {
            return referers;
        }

//        static String[] relationClickServers = new String[] {
//                "http://bbn06:9092/go/reClick", "http://bbn07:9092/go/reClick", "http://bbn08:9092/go/reClick",
//        };

        public String[] getClickServers() {
//            return relationClickServers;
            return defaultClickServers;
        }

        final static Map<Integer, String> vernameMap = new HashMap<Integer, String>();

        public Map<Integer, String> getVersionNameMap() {
            if (!vernameMap.isEmpty()) {
                return vernameMap;
            }
            vernameMap.put(Version.BLACK, "体验版");
            vernameMap.put(1, "1个推广位");
            vernameMap.put(10, "5个推广位");
            vernameMap.put(20, "10个推广位");
            vernameMap.put(30, "20个推广位");
            vernameMap.put(40, "40个推广位");
            vernameMap.put(50, "60个推广位");
            vernameMap.put(60, "120个推广位");
            return vernameMap;
        }

        final static Map<Integer, Integer> verCountMap = new HashMap<Integer, Integer>();

        public Map<Integer, Integer> getTuiguangCountMap() {
            if (!verCountMap.isEmpty()) {
                return verCountMap;
            }
            verCountMap.put(Version.BLACK, 1);
            verCountMap.put(1, 1);
            verCountMap.put(10, 5);
            verCountMap.put(20, 10);
            verCountMap.put(30, 20);
            verCountMap.put(40, 40);
            verCountMap.put(50, 60);
            verCountMap.put(60, 120);
            return verCountMap;
        }

        @Override
        public void doOnStartUpAsync() {
            HourlyCheckerJob.HOUR_JOB_ENABLE = true;
        }

        @Override
        public String getName() {
            return "流量推广";
        }
    }

    static class AppBaobeidashi extends APIConfig {

        public AppBaobeidashi(int app, String secret) {
            super(app, secret);
            this.subCode = "ts-10452";
        }

        @Override
        public void afterLogin(User user, String itemCode, boolean isFirst, Boolean isQianniu) {
//            Home.relation();
            Relation.relationOper(user.getSessionKey());
        }

        @Override
        public void doForInstall(User user) {
            new ItemUpdateJob(user.getId(), System.currentTimeMillis(), 20L, true).doJob();
        }

        @Override
        public String goOptimiseTemplateName() {
            return null;
        }

        @Override
        public int getVersion(User user) {
            return 0;
        }
    }
    
    public static class AppTaobiaoti extends APIConfig {
    	
    	private String logAppkey = "68756511";
    	
    	private String logAppSecret = "w4jRdbsPUCL6zV5ji22n";
    	
    	private String rdsHostAddress = "jconn2twrtwas.mysql.rds.aliyuncs.com";
    	
    	private String appName = " 淘掌柜_一键站内引流_提升排名";
    	
    	// 是否需要接入御城河日志
    	private boolean isRisk = true;

        public boolean enableInstantTradeSync() {
            return true;
        }

        public String[] getTmcTopics() {
            return allTmcTopics;
        }

        public boolean isToBuildDiagInfo() {
            return true;
        }

        public String getRdsName() {
//            return "jrds2c648b2x";
//            return "ins_0648b6n4";
            return "jrdsk47wxb6f";
        }

        public AppTaobiaoti(int app, String secret) {
            super(app, secret);
            this.subCode = "ts-1820059";

            this.freeCodesSet.add("ts-1820059-1");
            this.baseCodesSet.add("ts-1820059-5");
            this.llCodesSet.add("ts-1820059-4");
            /**
             *
             */
            this.VIPCodesSet.add("ts-1820059-3");

            // verion number : 30
            // ts-1820059-6
            this.superCodesSet.add("ts-1820059-6");

            // Version number : 40
            this.hallCodeSet.add("ts-1820059-v8");
        }

        @Override
        public void doForInstall(final User user) {
            jdpInstall(user);
        }

        @Override
        public boolean isNeedToUpdateMjsTmpl() {
            return true;
        }
        
        @Override
        public String getRedirURL() {
//            return "http://t.tobti.com";
            return "http://t.taovgo.com";
        }

        @Override
        public int getVersion(User user) {
            return 0;
        }

        public boolean useAsyncTrade() {
            return true;
        }

        @Override
        public String goOptimiseTemplateName() {
            return "diag/buytaobiaoti.html";
        }

        @Override
        public boolean isNeedMoreFieldsWhenSyncItem() {
            return true;
        }
        
        @Override
        public void afterLogin(User user, String itemCode, boolean isFirst, Boolean isQianniu) {
            if(isQianniu == null) {
            	isQianniu = false;
            }
            if(isQianniu) {
//            	Home.QNTbtIndex(isFirst, user.getSessionKey());
            	Home.index(isFirst, user.getSessionKey());
            } else {
            	log.info("" + System.currentTimeMillis());
            	if(user.getVersion() == Version.LL) {
            		ItemCarrierForDQ.TZGItemCarrier();
            	} else {
            		Home.index(isFirst, user.getSessionKey());
            	}
            	
            }
        }

        public void beforeReq(Request req, Response resp, Session session, Params params) {
            session.remove(WebParams.HIDE_NAV);
        }

        final static Map<Integer, String> vernameMap = new HashMap<Integer, String>();

        public boolean vgouSave() {
            return true;
        }

        @Override
        public boolean isItemScoreRelated() {
            return true;
        }

        final static Map<String, String> paramStrs = new HashMap<String, String>();

        @Override
        public Map<String, String> getSellLinkParamStr() {
            if (!paramStrs.isEmpty()) {
                return paramStrs;
            }
            paramStrs
                    .put("0.1元至尊版",
                            "{\"param\":{\"aCode\":\"ACT_22902351_130801170220\",\"itemList\":[\"ts-1820059-3:1*2\"],\"promIds\":[10088729],\"type\":1},\"sign\":\"EE8667D4DE830A3E360997A1FAEB5C5B\"}");
            return paramStrs;
        }

        final static Map<String, String> newParamStrs = new HashMap<String, String>();

        @Override
        public Map<String, String> getNewParamStr() {
            if (!newParamStrs.isEmpty()) {
                return newParamStrs;
            }
            newParamStrs
                    .put("诊断版",
                            "{\"param\":{\"aCode\":\"ACT_22902351_130714211138\",\"itemList\":[\"ts-1820059-1:1*2\"],\"promIds\":[10081230],\"type\":1},\"sign\":\"452B1F4F56CAA6D4D911A89453F6D612\"}");
            newParamStrs
                    .put("尊享版",
                            "{\"param\":{\"aCode\":\"ACT_22902351_130714211138\",\"itemList\":[\"ts-1820059-3:1*2\"],\"promIds\":[10081229],\"type\":1},\"sign\":\"3861C32F514DD42EE3EEEAB54510B813\"}");
            return newParamStrs;
        }

        public boolean isNeedSyncUserSimba(User user) {
            if (user == null) {
                return false;
            } else if (user.getVersion() >= Version.SUPER) {//开车版
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String getLogAppkey(){
        	return logAppkey;
        }
        
        @Override
        public String getLogAppSecret(){
        	return logAppSecret;
        }
        
        @Override
        public String getAppName(){
        	return appName;
        }
        
        @Override
        public String getRdsHostAddress(){
        	return rdsHostAddress;
        }
        
        @Override
        public boolean isRisk(){
        	return isRisk;
        }
    };

    static class AppSheepTitle extends AppTaoxuanci {

        public AppSheepTitle(int app, String secret) {
            super(app, secret);
            this.subCode = "";
        }

        @Override
        public void afterLogin(User user, String itemCode, boolean isFirst, Boolean isQianniu) {
            SheepTitle.sheeptitle();
        }
    }

    public static class AppTaoxuanci extends APIConfig {

        public String getRdsName() {
            return "jrds2c648b2x";
//            return "ins_0648b6n4";
        }

        @Override
        public String[] getTmcTopics() {
            return allTmcTopics;
        }

        @Override
        public void doOnStartUpAsync() {
        }

        Set<String> exlucdedUserNicks = new HashSet<String>();

        public AppTaoxuanci(int app, String secret) {
            super(app, secret);
            this.subCode = "FW_GOODS-1835721";
            /**
             * 诊断版本
             */
            this.freeCodesSet.add("FW_GOODS-1835721-2");

            /**
             * 99包年大促 基础版本
             */
            this.VIPCodesSet.add("FW_GOODS-1835721-1");
            /**
             * 500宝贝以上版本
             */
            this.superCodesSet.add("FW_GOODS-1835721-v6");
            /**
             * >= 1000
             */
            this.hallCodeSet.add("FW_GOODS-1835721-v7");
            /**
             * >= 2k
             */
            this.godCodeSet.add("FW_GOODS-1835721-v8");

            /**
             * >= 5k
             */
            this.sunCodeSet.add("FW_GOODS-1835721-v9");

            /**
             * 无限宝贝订购数
             */
            this.cuocuoCodeSet.add("FW_GOODS-1835721-v5");

            exlucdedUserNicks.add("最嗨时尚女鞋");
            exlucdedUserNicks.add("金华小店2");
            exlucdedUserNicks.add("jinhua101012");
        }

        public int getMaxAvailable(User user) {

            if (exlucdedUserNicks.contains(user.getUserNick())) {
                return 50000;
            }
            if (user.getFirstLoginTime() == null || user.getFirstLoginTime() < 1376079162380L) {
                return 50000;
            }

            if (!TMConfigs.Operate.enableRealTitleModifier) {
                return 99999;
            }

            int version = user.getVersion();

            switch (version) {
                case Version.VIP:
                    return 499;
                case Version.SUPER:
                    return 999;
                case Version.HALL:
                    return 1999;
                case Version.GOD:
                    return 4999;
                case Version.SUN:
                    return 49999;
                default:
                    return 50000;
            }
        }

        public boolean useAsyncTrade() {
            return true;
        }

        @Override
        public void doForInstall(final User user) {
            jdpInstall(user);
        }

        @Override
        public boolean isUserSycnTrade(User user) {
            return user.getFirstLoginTime() < 1364546851308L;
        }

        @Override
        public String goOptimiseTemplateName() {
            return "diag/buyxuanci.html";
        }

        @Override
        public int getVersion(User user) {
            return 0;
        }

        @Override
        public String getRedirURL() {
            return App.THANDBOX_ENABLE ? "http://localhost:9999" : "http://x.taovgo.com";
        }

        @Override
        public void afterLogin(User user, String itemCode, boolean isFirst, Boolean isQianniu) {
//            if (!StringUtils.isEmpty(itemCode)) {
//                if (this.freeCodesSet.contains(itemCode)) {
//                    In.immediateXuanciDiag();
//                } else {
//                    Home.autoTitle();
//                }
//            }

            int version = SubcribeAction.getSubscribeInfo(user);
            if (version > Version.FREE) {
                Home.autoIndex(user.getSessionKey(), isFirst);
            } else {
                In.immediateXuanciDiag();
            }

        }

        @Override
        public boolean isItemScoreRelated() {
            return true;
        }

        final static Map<String, String> paramStrs = new HashMap<String, String>();

        @Override
        public Map<String, String> getSellLinkParamStr() {
            if (!paramStrs.isEmpty()) {
                return paramStrs;
            }
            paramStrs
                    .put("25一个月",
                            "{\"param\":{\"aCode\":\"ACT_22902351_130626144056\",\"itemList\":[\"FW_GOODS-1835721-1:1*2\"],\"promIds\":[10074061],\"type\":1},\"sign\":\"EFBC10649941BC44A8FD38A7949CA088\"}");
            paramStrs
                    .put("35一个月",
                            "{\"param\":{\"aCode\":\"ACT_22902351_130626144056\",\"itemList\":[\"FW_GOODS-1835721-v6:1*2\"],\"promIds\":[10074062],\"type\":1},\"sign\":\"DE3ED7D1A3CDDDADC088C021D95C8BD2\"}");
            return paramStrs;
        }

        final static Map<String, String> newParamStrs = new HashMap<String, String>();

        @Override
        public Map<String, String> getNewParamStr() {
            if (!newParamStrs.isEmpty()) {
                return newParamStrs;
            }
            newParamStrs
                    .put("500以下版本",
                            "{\"param\":{\"aCode\":\"ACT_22902351_130720212522\",\"itemList\":[\"FW_GOODS-1835721-1:6*2\"],\"promIds\":[10083703],\"type\":1},\"sign\":\"421C9B27C1E0D73DB27FFCF07EB49EB9\"}");
            newParamStrs
                    .put("500以上版本",
                            "{\"param\":{\"aCode\":\"ACT_22902351_130720212522\",\"itemList\":[\"FW_GOODS-1835721-v6:6*2\"],\"promIds\":[10083702],\"type\":1},\"sign\":\"61D32104251636A80C6AFAA51740FC0A\"}");
            return newParamStrs;
        }

    }
    
    
    public static class AppFenxiao extends APIConfig {

        public AppFenxiao(int app, String secret) {
            super(app, secret);
            this.subCode = "service-0-22735";
            /**
             * 诊断版本
             */
            this.freeCodesSet.add("service-0-22735-1");

            /**
             * 99包年大促 基础版本
             */
            this.VIPCodesSet.add("service-0-22735-2");
            /**
             * 500宝贝以上版本
             */
            this.superCodesSet.add("service-0-22735-v3");
            /**
             * >= 1000
             */
            this.hallCodeSet.add("service-0-22735-v3");
            /**
             * >= 2k
             */
            this.godCodeSet.add("service-0-22735-v3");

            /**
             * >= 5k
             */
            this.sunCodeSet.add("FW_GOODS-1835721-v9");

            /**
             * 无限宝贝订购数
             */
            this.cuocuoCodeSet.add("FW_GOODS-1835721-v5");
        }

        public int getMaxAvailable(User user) {

            if (user.getFirstLoginTime() == null || user.getFirstLoginTime() < 1376079162380L) {
                return 50000;
            }

            if (!TMConfigs.Operate.enableRealTitleModifier) {
                return 99999;
            }

            int version = user.getVersion();

            switch (version) {
                case Version.VIP:
                    return 499;
                case Version.SUPER:
                    return 999;
                case Version.HALL:
                    return 1999;
                case Version.GOD:
                    return 4999;
                case Version.SUN:
                    return 49999;
                default:
                    return 50000;
            }
        }

        @Override
        public void doForInstall(User user) {
            new ItemUpdateJob(user.getId(), System.currentTimeMillis(), 20L, true).doJob();
            if (user.isNew()) {
                // TODO...
            }
        }

        @Override
        public boolean isUserSycnTrade(User user) {
            return user.getFirstLoginTime() < 1364546851308L;
        }

        @Override
        public int getVersion(User user) {
            return 0;
        }

        @Override
        public String getRedirURL() {
            return "http://fenxiao.youmiguang.com";
        }

        @Override
        public void afterLogin(User user, String itemCode, boolean isFirst, Boolean isQianniu) {

            if (isFirst == false) {
                new ItemUpdateJob(user.getId()).now();
            } else {
                log.info("user: " + user.getId() + ", nick: " + user.getUserNick()
                        + " is first login in mingdian-----");
            }

            Wireless.onekey();

        }

        /*@Override
        public boolean isNeedMoreFieldsWhenSyncItem() {
            return true;
        }

        @Override
        public String getItemFieldsWhenSyncItem() {
            return ItemApi.FIELDS_WITH_DESC;
        }

        @Override
        public boolean isGetItemOutLinkFromDesc() {
            return true;
        }

        @Override
        public boolean isItemScoreRelated() {
            return true;
        }

        final static Map<String, String> paramStrs = new HashMap<String, String>();

        @Override
        public Map<String, String> getSellLinkParamStr() {
            if (!paramStrs.isEmpty()) {
                return paramStrs;
            }
            paramStrs
                    .put("25一个月",
                            "{\"param\":{\"aCode\":\"ACT_22902351_130626144056\",\"itemList\":[\"FW_GOODS-1835721-1:1*2\"],\"promIds\":[10074061],\"type\":1},\"sign\":\"EFBC10649941BC44A8FD38A7949CA088\"}");
            paramStrs
                    .put("35一个月",
                            "{\"param\":{\"aCode\":\"ACT_22902351_130626144056\",\"itemList\":[\"FW_GOODS-1835721-v6:1*2\"],\"promIds\":[10074062],\"type\":1},\"sign\":\"DE3ED7D1A3CDDDADC088C021D95C8BD2\"}");
            return paramStrs;
        }

        final static Map<String, String> newParamStrs = new HashMap<String, String>();

        @Override
        public Map<String, String> getNewParamStr() {
            if (!newParamStrs.isEmpty()) {
                return newParamStrs;
            }
            newParamStrs
                    .put("500以下版本",
                            "{\"param\":{\"aCode\":\"ACT_22902351_130720212522\",\"itemList\":[\"FW_GOODS-1835721-1:6*2\"],\"promIds\":[10083703],\"type\":1},\"sign\":\"421C9B27C1E0D73DB27FFCF07EB49EB9\"}");
            newParamStrs
                    .put("500以上版本",
                            "{\"param\":{\"aCode\":\"ACT_22902351_130720212522\",\"itemList\":[\"FW_GOODS-1835721-v6:6*2\"],\"promIds\":[10083702],\"type\":1},\"sign\":\"61D32104251636A80C6AFAA51740FC0A\"}");
            return newParamStrs;
        }
*/
    }
    

    public static class AppMingDian extends APIConfig {

        public AppMingDian(int app, String secret) {
            super(app, secret);
            //this.subCode = "service-0-22735";
            
        }

        public int getMaxAvailable(User user) {
            
            return 500;

            /*if (user.getFirstLoginTime() == null || user.getFirstLoginTime() < 1376079162380L) {
                return 50000;
            }

            if (!TMConfigs.Operate.enableRealTitleModifier) {
                return 99999;
            }

            int version = user.getVersion();

            switch (version) {
                case Version.VIP:
                    return 499;
                case Version.SUPER:
                    return 999;
                case Version.HALL:
                    return 1999;
                case Version.GOD:
                    return 4999;
                case Version.SUN:
                    return 49999;
                default:
                    return 50000;
            }*/
        }

        @Override
        public void doForInstall(User user) {
            new ItemUpdateJob(user.getId(), System.currentTimeMillis(), 20L, true).doJob();
            if (user.isNew()) {
                // TODO...
            }
            
          
        }


        @Override
        public int getVersion(User user) {
            return 0;
        }

        @Override
        public String getRedirURL() {
            //return "http://fenxiao.youmiguang.com";
            return "";
        }

        @Override
        public void afterLogin(User user, String itemCode, boolean isFirst, Boolean isQianniu) {
//            if (!StringUtils.isEmpty(itemCode)) {
//                if (this.freeCodesSet.contains(itemCode)) {
//                    In.immediateXuanciDiag();
//                } else {
//                    Home.autoTitle();
//                }
//            }
/*
            int version = SubcribeAction.getSubscribeInfo(user);
            if (version > Version.FREE) {
                FenXiao.diag();
            } else {
                FenXiao.diag();
            }*/
            
            //如果是第一次登录，会在doForInstall中同步数据，所以这里就不同步了
            //不然batchInsert item时会报错，id重复
            if (isFirst == false) {
                //new ItemUpdateJob(user.getId()).now();
            } else {
                log.info("user: " + user.getId() + ", nick: " + user.getUserNick()
                        + " is first login in mingdian-----");
            }

            OneKey.indexNew();

        }

        @Override
        public boolean isNeedMoreFieldsWhenSyncItem() {
            return true;
        }

        @Override
        public String getItemFieldsWhenSyncItem() {
            return ItemApi.FIELDS_WITH_DESC;
        }

        @Override
        public boolean isGetItemOutLinkFromDesc() {
            return true;
        }

        @Override
        public boolean isItemScoreRelated() {
            return true;
        }

        /*final static Map<String, String> paramStrs = new HashMap<String, String>();

        @Override
        public Map<String, String> getSellLinkParamStr() {
            if (!paramStrs.isEmpty()) {
                return paramStrs;
            }
            paramStrs
                    .put("25一个月",
                            "{\"param\":{\"aCode\":\"ACT_22902351_130626144056\",\"itemList\":[\"FW_GOODS-1835721-1:1*2\"],\"promIds\":[10074061],\"type\":1},\"sign\":\"EFBC10649941BC44A8FD38A7949CA088\"}");
            paramStrs
                    .put("35一个月",
                            "{\"param\":{\"aCode\":\"ACT_22902351_130626144056\",\"itemList\":[\"FW_GOODS-1835721-v6:1*2\"],\"promIds\":[10074062],\"type\":1},\"sign\":\"DE3ED7D1A3CDDDADC088C021D95C8BD2\"}");
            return paramStrs;
        }

        final static Map<String, String> newParamStrs = new HashMap<String, String>();

        @Override
        public Map<String, String> getNewParamStr() {
            if (!newParamStrs.isEmpty()) {
                return newParamStrs;
            }
            newParamStrs
                    .put("500以下版本",
                            "{\"param\":{\"aCode\":\"ACT_22902351_130720212522\",\"itemList\":[\"FW_GOODS-1835721-1:6*2\"],\"promIds\":[10083703],\"type\":1},\"sign\":\"421C9B27C1E0D73DB27FFCF07EB49EB9\"}");
            newParamStrs
                    .put("500以上版本",
                            "{\"param\":{\"aCode\":\"ACT_22902351_130720212522\",\"itemList\":[\"FW_GOODS-1835721-v6:6*2\"],\"promIds\":[10083702],\"type\":1},\"sign\":\"61D32104251636A80C6AFAA51740FC0A\"}");
            return newParamStrs;
        }
*/
    }

    public int getApp() {
        return app;
    }

    public void setApp(int app) {
        this.app = app;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public static APIConfig get() {
        return me;
    }

    public abstract void afterLogin(User user, String itemCode, boolean isFirst, Boolean isQianniu);

    public void doForInstall(User user) {
        if (APIConfig.get().enableSyncTrade(user.getId())) {
            // TODO, sync the trades and the comments... while, make it async...
            new TradeUpdateJob(user.getId(), System.currentTimeMillis()).doJob();
        }
        if (APIConfig.get().enableSyncTradeRate()) {
            // TODO: sync trade rate..
            new TradeRateUpdateJob(user.getId(), true).doJob();
        }
        new ItemUpdateJob(user.getId(), System.currentTimeMillis(), 20L, true).doJob();
    }

    public String goOptimiseTemplateName() {
        return null;
    }

    public int getVersion(User user) {
        return 0;
    }

    public Set<String> getFreeCodesSet() {
        return freeCodesSet;
    }

    public void setFreeCodesSet(Set<String> freeCodesSet) {
        this.freeCodesSet = freeCodesSet;
    }

    public Set<String> getVIPCodesSet() {
        return VIPCodesSet;
    }

    public void setVIPCodesSet(Set<String> vIPCodesSet) {
        VIPCodesSet = vIPCodesSet;
    }

    public Set<String> getBaseCodesSet() {
        return baseCodesSet;
    }

    public void setBaseCodesSet(Set<String> baseCodesSet) {
        this.baseCodesSet = baseCodesSet;
    }

    public Set<String> getLLCodesSet() {
        return llCodesSet;
    }

    public void setLLCodesSet(Set<String> llCodesSet) {
        this.llCodesSet = llCodesSet;
    }
    
    public Set<String> getSuperCodesSet() {
        return superCodesSet;
    }

    public void setHallCodesSet(Set<String> hallCodesSet) {
        this.hallCodeSet = hallCodesSet;
    }

    public Set<String> getHallCodesSet() {
        return hallCodeSet;
    }

    public Set<String> getTryCodesSet() {
        return tryCodesSet;
    }

    public void setTryCodesSet(Set<String> tryCodesSet) {
        this.tryCodesSet = tryCodesSet;
    }

    public void setSuperCodesSet(Set<String> superCodesSet) {
        this.superCodesSet = superCodesSet;
    }

    public String getSubCode() {
        return subCode;
    }

    public void setSubCode(String subCode) {
        this.subCode = subCode;
    }

    public String getServiceUrl() {
        return "http://fuwu.taobao.com/ser/detail.htm?tracelog=yy&service_code=" + APIConfig.get().getSubCode();
    }

    public void beforeReq(Request request, Response resp, Session session, Params params) {
    }

    public void doOnStartUpAsync() {
    }

    public static class AppKittyTitle extends APIConfig {
        public AppKittyTitle(int app, String secret) {
            super(app, secret);
            this.subCode = "FW_GOODS-1854059";
            this.freeCodesSet.add("FW_GOODS-1854059-1");
            this.baseCodesSet.add("FW_GOODS-1854059-2");
        }

        /*@Override
        public void afterLogin(User user, String itemCode, boolean isFirst, Boolean isQianniu) {
            new ItemUpdateJob(user.getId(), System.currentTimeMillis(), 20L, true).now();
            //Wireless.onekey();
            OneKey.indexNew();
        }*/
        

        public int getMaxAvailable(User user) {
            
            return 500;

        }

        @Override
        public void doForInstall(User user) {
            new ItemUpdateJob(user.getId(), System.currentTimeMillis(), 20L, true).doJob();
            
        }


        @Override
        public int getVersion(User user) {
            return 0;
        }

        @Override
        public String getRedirURL() {
            //return "http://zhaoliuliang.youmiguang.com";
            //return "http://mingdian.tmallm.com";
            return "http://121.196.131.221";
        }

        @Override
        public void afterLogin(User user, String itemCode, boolean isFirst, Boolean isQianniu) {

            //如果是第一次登录，会在doForInstall中同步数据，所以这里就不同步了
            //不然batchInsert item时会报错，id重复
            if (isFirst == false) {
                new ItemUpdateJob(user.getId()).now();
            } else {
                log.info("user: " + user.getId() + ", nick: " + user.getUserNick()
                        + " is first login in mingdian-----");
            }

            OneKey.indexNew();

        }

        @Override
        public boolean isNeedMoreFieldsWhenSyncItem() {
            return true;
        }

        @Override
        public String getItemFieldsWhenSyncItem() {
            //return ItemApi.FIELDS_WITH_DESC;
            return "";
        }

        @Override
        public boolean isGetItemOutLinkFromDesc() {
            return false;
        }

        @Override
        public boolean isItemScoreRelated() {
            return true;
        }
    }

    public boolean deleteAllItems() {
        return true;
    }

    public boolean isItemScoreRelated() {
        return false;
    }

    public boolean isNeedToUpdateMjsTmpl() {
        return false;
    }

    public Set<String> getHallCodeSet() {
        return hallCodeSet;
    }

    public void setHallCodeSet(Set<String> hallCodeSet) {
        this.hallCodeSet = hallCodeSet;
    }

    public Set<String> getGodCodeSet() {
        return godCodeSet;
    }

    public void setGodCodeSet(Set<String> godCodeSet) {
        this.godCodeSet = godCodeSet;
    }

    public void setSunCodeSet(Set<String> sunCodeSet) {
        this.sunCodeSet = sunCodeSet;
    }

    public Set<String> getSunCodeSet() {
        return sunCodeSet;
    }

    public Set<String> getDaweiCodeSet() {
        return daweiCodeSet;
    }

    public void setDaweiCodeSet(Set<String> daweiCodeSet) {
        this.daweiCodeSet = daweiCodeSet;
    }

    public Set<String> getCuocuoCodeSet() {
        return cuocuoCodeSet;
    }

    public void setCuocuoCodeSet(Set<String> cuocuoCodeSet) {
        this.cuocuoCodeSet = cuocuoCodeSet;
    }

    public void afterItemUpdateJob(User user, List<Item> onSaleItemList, List<Item> inventoryItemList) {
        // TODO 有可能有几个宝贝没有拖到，删除之前最好做一次ItemGet来确认
        Set<Long> onSaleItemSet = new HashSet<Long>();
        Set<Long> inventoryItemSet = new HashSet<Long>();
        if (onSaleItemList != null && !onSaleItemList.isEmpty()) {
            for (Item item : onSaleItemList) {
                onSaleItemSet.add(item.getNumIid());
            }
        }
        if (inventoryItemList != null) {
            for (Item item : inventoryItemList) {
                inventoryItemSet.add(item.getNumIid());
            }
        }
        Set<Long> numIids = PopularizedDao.findNumIidsByUserId(user.getId());
        if (numIids != null && !numIids.isEmpty()) {
            for (Long numIid : numIids) {
                if (onSaleItemSet.contains(numIid)) {
                    continue;
                } else if (inventoryItemSet.contains(numIid)) {
                    Popularized.remove(user, numIid);
                } else {
                    Item call = ApiJdpAdapter.get(user).findItem(user, numIid);
                    if (call == null) {
                        Popularized.remove(user, numIid);
                    }
                }
            }
        }
    }

    public void beforeLogin() {
        if (TMConfigs.Is_Tmall_Mode == true) {
            CatSearchComment.home();
        }
    }

    public Map<Integer, String> getVersionNameMap() {
        return MapUtils.EMPTY_MAP;
    }

    public Map<Integer, Integer> getTuiguangCountMap() {
//        return MapUtils.EMPTY_MAP;
        return aituiguang.getTuiguangCountMap();
    }

    public Map<Integer, Integer> getHotCountMap() {
        return MapUtils.EMPTY_MAP;
    }

    public String[] getReferes() {
        return NumberUtil.EMPTY_STRING_ARRAY;
    }

    public String[] getReferes(long numIid) {
        return NumberUtil.EMPTY_STRING_ARRAY;
    }

    public boolean isAllow(String action) {
        return true;
    }

    public boolean vgouSave() {
        return false;
    }

    static String[] defaultClickServers = new String[] {
            "http://bbn06:9092/go/reClick",
            "http://bbn09:9092/go/reClick",
            "http://bbn08:9092/go/reClick",
            "http://bbn10:9092/go/reClick",
    };

    public String[] getClickServers() {
        return defaultClickServers;
    }

    public String getName() {
        return StringUtils.EMPTY;
    }

    public boolean enableSyncTrade(Long userId) {
        return false;
    }

    public boolean enableSyncTradeRate() {
        return enableSyncTrade;
    }

    public Map<String, String> getSellLinkParamStr() {
        return MapUtils.EMPTY_MAP;
    }

    public Map<String, String> getNewParamStr() {
        return MapUtils.EMPTY_MAP;
    }

    public Platform getPlatform() {
        return Platform.taobao;
    }

    public enum Platform {
        jingdong, paipai, taobao
    }

    final static APIConfig taobaobeidashi = new AppBaobeidashi(12265022, "48b91e94491f57b8d25c8b3833c1b1c7");

    /**
     * 
     * 还价不求人 前台Appkey 12266733    后台Appkey 12266732
     * 还价不求人
     * 12266732
     * 00e433f3381ef01ee9842069c81120a4
     * 
     * 翻翻乐_店铺插件 前台Appkey 12265023    后台Appkey 12265022
     */
    public static APIConfig dazhe = new AppJinNang(12266732, "fd132f04d231934fb83417e00e8d256b");

    static APIConfig[] apps = new APIConfig[] {
            taobiaoti, taoxuanci, taobaobeidashi, relationSale, taovgo, aituiguang, skinWindow,
            thandboxAutoTitle, thandboxTianxiaomao,
            defender, kittytitle, mingDian, dawei880, daweishop, fanfanle, wuxifenxiao, jinnangzhekou, dazhe,
            fenxiao, thandboxDazhe,
            chedaotaozhanggui, tzgTrade, qkcrm, tzgsp, tddwm,

            PaiPaiAPIConfig.paipaiweigou, PaiPaiAPIConfig.paipaidiscount, PaiPaiAPIConfig.paipailetuiguang,
            PaiPaiAPIConfig.paipaibiaoti,

            JDAction.jdApiConfig
    };

    @Override
    public String toString() {
        return "APIConfig [app=" + app + ", apiKey=" + apiKey + ", secret=" + secret + ", subCode=" + subCode + "]";
    }

    public boolean isToBuildDiagInfo() {
        return false;
    }

    public boolean isUserSycnTrade(User user) {
        return false;
    }

    public String getRedirURL() {
        // return "http://t.tobti.com";
        return "http://t.taovgo.com";
    }

    /**
     * 同步直通车数据
     * @return
     */
    public boolean isNeedSyncUserSimba(User user) {
        return false;
    }

    public boolean installMonitor() {
        return false;
    }

    public int getMaxAvailable(User user) {
        return 500;
    }

    static String[] allTmcTopics = new String[] {
            "taobao_trade_TradeSuccess",
            "taobao_trade_TradeSuccess",
            "taobao_trade_TradeTimeoutRemind",
            "taobao_trade_TradeRated",
            "taobao_trade_TradeMemoModified",
            "taobao_trade_TradeLogisticsAddressChanged",
            "taobao_trade_TradeChanged",
            "taobao_trade_TradeCreate",
            "taobao_item_ItemAdd",
            "taobao_item_ItemUpshelf",
            "taobao_item_ItemDownshelf",
            "taobao_item_ItemDelete",
            "taobao_item_ItemUpdate",
            //            "taobao_item_ItemRecommendAdd",
            "taobao_item_ItemRecommendDelete",
            "taobao_item_ItemZeroStock",
            "taobao_item_ItemPunishDelete"
    };

    static String[] baseTmcTopic = new String[] {
            "taobao_item_ItemAdd",
            "taobao_item_ItemUpshelf",
            "taobao_item_ItemDownshelf",
            "taobao_item_ItemDelete",
            "taobao_item_ItemUpdate",
            "taobao_item_ItemPunishDelete"
    };

    static String[] defenderTmcTopic = new String[] {
            "taobao_trade_TradeSuccess",
            "taobao_trade_TradeSuccess",
            "taobao_trade_TradeTimeoutRemind",
            "taobao_trade_TradeRated",
            "taobao_trade_TradeMemoModified",
            "taobao_trade_TradeLogisticsAddressChanged",
            "taobao_trade_TradeChanged",
            "taobao_trade_TradeCreate",
            "taobao_item_ItemAdd",
            "taobao_item_ItemUpshelf",
            "taobao_item_ItemDownshelf",
            "taobao_item_ItemDelete",
            "taobao_item_ItemUpdate",
            "taobao_item_ItemZeroStock",
            "taobao_item_ItemPunishDelete"
    };

    public String[] getTmcTopics() {
        return baseTmcTopic;
    }

    public boolean doCloseTrade() {
        return false;
    }

    public boolean isSimpleTradeToLocal() {
        return !doCloseTrade() && TMConfigs.App.IS_TRADE_ALLOW;
    }

    /**
     * 异步订单，基本上每天同步一次，同步前三天的订单为主
     * @return
     */
    public boolean useAsyncTrade() {
        return false;
    }

    public String getRdsName() {
        return null;
    }

    public String getClickUrl(Long numIid) {
        return "http://item.taobao.com/item.htm?id=" + numIid;
    }

    public void jdpInstall(final User user) {
        TMConfigs.getDiagResultPool().submit(new Callable<DiagResult>() {
            @Override
            public DiagResult call() throws Exception {
                new JDPApi.JuShiTaAddUserApi(user).call();
                CommonUtils.sleepQuietly(5000L);
                if (TMConfigs.Rds.enableJdpPush) {
                    new PropDiagJob(user, true).doJob();
                }
                return null;
            }
        });

        new ItemUpdateJob(user.getId(), System.currentTimeMillis(), 20L, true).doJob();

        if (user.isNew()) {
            // TODO...
        }
    }
    
	public void onsAdd(final User user) {
		// 查询某个用户是否同步消息
		JushitaJmsUserGet jmsGet = new JushitaJmsUserGet(user);
		TmcUser onsUser = jmsGet.call();
		if (onsUser == null) {
			// 添加ONS消息同步用户
			JushitaJmsUserAdd jmsAdd = new JushitaJmsUserAdd(user);
			Boolean success = jmsAdd.call();
			if (!success) {
				log.error("添加ONS消息同步用户失败！" + user.toString() + "~~~错误： "
						+ jmsAdd.getSubErrorMsg());
			} else {
				log.info("添加ONS消息同步用户成功！" + user.toString());
			}
		}
	}

    public void ensureHelpBase() {
    }

    /**
     * 同步的时候，ItemOnSale这个接口只能得到一部分属性，如果需要其他属性，比如是否是分销
     * @return
     */
    public boolean isNeedMoreFieldsWhenSyncItem() {
        return false;
    }

    /**
     * 当需要获取Item其他属性的时候，返回需要获取的属性
     * @return
     */
    public String getItemFieldsWhenSyncItem() {
        return "";
    }

    /**
     * 是否获取宝贝外链
     * @return
     */
    public boolean isGetItemOutLinkFromDesc() {
        return false;
    }

    public boolean enableInstantTradeSync() {
        return false;
    }
    
    public String getLogAppkey(){
    	return StringUtils.EMPTY;
    }
    
    public String getLogAppSecret(){
    	return StringUtils.EMPTY;
    }
    
    public String getAppName(){
    	return StringUtils.EMPTY;
    }
    
    public String getRdsHostAddress(){
    	return StringUtils.EMPTY;
    }
    
    public boolean isRisk(){
    	return false;
    }

    public boolean isEnableSyncTrade() {
        return enableSyncTrade;
    }

    public void setEnableSyncTrade(boolean enableSyncTrade) {
        this.enableSyncTrade = enableSyncTrade;
    }

}
