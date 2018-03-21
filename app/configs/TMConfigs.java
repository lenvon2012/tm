
package configs;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import job.ApplicationStopJob;
import models.item.ItemPlay;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;

import play.Play;
import titleDiag.DiagResult;
import uvpvdiag.NewUvPvDiagResult;
import uvpvdiag.UvPvDiagResult;
import actions.DiagAction.BatchResultMsg;

import com.ciaosir.client.PYFutureTaskPool;
import com.ciaosir.client.utils.NumberUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.taobao.api.domain.Trade;
import com.taobao.api.domain.TradeRate;
import com.taobao.api.internal.stream.message.StreamMsgConsumeFactory;

import controllers.APIConfig;

public class TMConfigs {

    @JsonAutoDetect
    public static class TMSWitch implements Serializable {

        public TMSWitch(String configKey, boolean value) {
            super();
            this.configKey = configKey;
            this.value = value;
        }

        @JsonProperty
        String configKey;

        @JsonProperty
        boolean value;

        @JsonProperty
        String name;

        @JsonProperty
        String comment;
    }

    public static class App {

        public static boolean ENABLE_HELICOPTER = Boolean.parseBoolean(Play.configuration.getProperty(
                "enable.helicopter", "false"));

        public static String HOME_TARGET = (Play.configuration.getProperty("home.target", ""));

        public static boolean ENABLE_SHIELD = Boolean.parseBoolean(Play.configuration.getProperty("enable.shield",
                "false"));
        
        public static boolean ENABLE_USERS_GET_API = Boolean.parseBoolean(Play.configuration.getProperty("users.api.get",
                "false"));

        public static boolean ENABLE_TMHttpServlet = Boolean.parseBoolean(Play.configuration.getProperty(
                "enable.TMHttpServlet", "false"));

        public static String APP_KEY = Play.configuration.getProperty("app.key", "12442245");

        public static String APP_SECRET = Play.configuration.getProperty("app.secret",
                "80663ff0052dad65fa4bd4cc4aba071f");

        public static boolean THANDBOX_ENABLE = Boolean.parseBoolean(Play.configuration.getProperty(
                "sandbox.enable", "false"));

        public static final String TOKEN_URL = THANDBOX_ENABLE ? "https://oauth.tbsandbox.com/token"
                : "https://oauth.taobao.com/token";

//        public static String ORIGIN_TAOBAO_URL = "http://container.api.taobao.com/container?encode=utf-8&appkey=";

        public static String CONTAINER_TAOBAO_URL = (THANDBOX_ENABLE ? "http://container.api.tbsandbox.com/container?encode=utf-8&appkey="
                : "http://container.api.taobao.com/container?encode=utf-8&appkey=")
                + APP_KEY;

        public static String TAOBAO_AUTH_URL = (THANDBOX_ENABLE ? "https://oauth.tbsandbox.com/authorize?response_type=code&client_id="
                : "https://oauth.taobao.com/authorize?response_type=code&client_id=")
                + APP_KEY;

//        public static String THAND_TAOBAO_URL = "http://container.api.tbsandbox.com/container?encode=utf-8&appkey="
//                + APP_KEY;

        public static String REFRESH_URL = (THANDBOX_ENABLE ? "http://container.api.tbsandbox.com/container/refresh"
                : "http://container.api.taobao.com/container/refresh");

        public static String API_TAOBAO_URL = THANDBOX_ENABLE ? "http://gw.api.tbsandbox.com/router/rest"
                : "http://gw.api.taobao.com/router/rest";

        public static String TAOBAO_ITEM_URL = "http://item.taobao.com/item.htm?id=";

        public static boolean isUdpEnable = Boolean.parseBoolean(Play.configuration.getProperty("udp.enable", "false"));

        public static boolean isDevMock = Boolean.parseBoolean(Play.configuration
                .getProperty("enable.dev.mock", "true"));

        public static Map<String, String> keySecrets = new HashMap<String, String>();

        static {
            keySecrets.put("21255586", "31d9c374ff99e6cd6d50e6b22daca68a");
        }

        public static boolean USE_DETAIL_DIAG = false;

        public static boolean IS_TRADE_ALLOW = Boolean.parseBoolean(Play.configuration.getProperty("enable.trade.api",
                "false"));

//        public static boolean IS_ADD_ITEM_ALLOW = Boolean.parseBoolean(Play.configuration.getProperty(
//                "enable.additem.message", "false"));
        public static boolean IS_ADD_ITEM_ALLOW = true;

        // jd
        public static String API_JD_URL = "http://gw.api.360buy.com/routerjson";

//        public static String API_JD_URL = "http://gw.api.sandbox.360buy.com/routerjson";

        public static String JD_APP_KEY = Play.configuration.getProperty("app.key",
                "7172A253F87CE1C102CC92942989951B");

        public static String JD_APP_SECRET = Play.configuration.getProperty("app.secret",
                "41978b4bc8e04bfb87a272742b4f19ae");
    }

    public static class version {
        public static int VERSION_DEFAULT = 45;
    }

    public static class CPStaff {

        public static String[] cpstaffs = new String[] {
            "xiaoyang", "xiaojian", "baihe", "xinhong", "xinsheng", "songsir", "longtai", "linluo"
        };
        
    }
    
    public static class YINGXIAO {

        // 爱推广新的5元链接
        public static String AITUIGUANG_FIVE_YAUN_ONE_SHOWCASE = "{\"param\":{\"aCode\":\"ACT_333336410_140317112727\",\"itemList\":[\"FW_GOODS-1848326-1:1*2\"],\"promIds\":[10259037],\"type\":1},\"sign\":\"35DF4D0F214DEFA90101542722D79481\"}";

        public static String AITUIGUANG_FIVE_YAUN_THREE_SHOWCASE = "{\"param\":{\"aCode\":\"ACT_333336410_140317112727\",\"itemList\":[\"FW_GOODS-1848326-v3:1*2\"],\"promIds\":[10259036],\"type\":1},\"sign\":\"BCDDF88A8E300EE93E1156B4F1EBFBCD\"}";

        public static String AITUIGUANG_FIVE_YAUN_FIVE_SHOWCASE = "{\"param\":{\"aCode\":\"ACT_333336410_140317112727\",\"itemList\":[\"FW_GOODS-1848326-v4:1*2\"],\"promIds\":[10259035],\"type\":1},\"sign\":\"0814AF7CF4C767C7AE430B892ABAA5A1\"}";

        public static String AITUIGUANG_FIVE_YAUN_TEN_SHOWCASE = "{\"param\":{\"aCode\":\"ACT_333336410_140317112727\",\"itemList\":[\"FW_GOODS-1848326-v5:1*2\"],\"promIds\":[10259034],\"type\":1},\"sign\":\"363C05DEE8AE8746DE182C39B6020D74\"}";

        public static String AITUIGUANG_FIVE_YAUN_TWENTY_SHOWCASE = "{\"param\":{\"aCode\":\"ACT_333336410_140317112727\",\"itemList\":[\"FW_GOODS-1848326-v6:1*2\"],\"promIds\":[10259033],\"type\":1},\"sign\":\"FDA3D50661F991AF82B56898CB10D3C8\"}";

        public static String AITUIGUANG_FIVE_YAUN_THIRTY_SHOWCASE = "{\"param\":{\"aCode\":\"ACT_333336410_140317112727\",\"itemList\":[\"FW_GOODS-1848326-v7:1*2\"],\"promIds\":[10259032],\"type\":1},\"sign\":\"9787E6CB3FB56A64A814C14931AC6D0B\"}";

        // 淘掌柜后台5元链接
        public static String TAOZHANGGUI_FIVE_YUAN_MONTH_VERSION_20 = "{\"param\":{\"aCode\":\"ACT_22902351_131005215247\",\"itemList\":[\"ts-1820059-3:1*2\"],\"promIds\":[10129889],\"type\":1},\"sign\":\"8C852D9CEE68FE96D587E99EB6F0FF35\"}";

        public static String TAOZHANGGUI_FIVE_YUAN_MONTH_VERSION_30 = "{\"param\":{\"aCode\":\"ACT_22902351_131005215247\",\"itemList\":[\"ts-1820059-6:1*2\"],\"promIds\":[10129888],\"type\":1},\"sign\":\"09D23720295439FFF6C46845CAE725C5\"}";

        public static String TAOZHANGGUI_FIVE_YUAN_MONTH_VERSION_40 = "{\"param\":{\"aCode\":\"ACT_22902351_131005215247\",\"itemList\":[\"ts-1820059-v8:1*2\"],\"promIds\":[10129887],\"type\":1},\"sign\":\"4D1AC0AF44CF2634D734508DAD0E85E1\"}";

        // 自动标题后台5元链接
        public static String TAOXUANCI_FIVE_YUAN_MONTH_VERSION_20 = "{\"param\":{\"aCode\":\"ACT_22902351_131005215315\",\"itemList\":[\"FW_GOODS-1835721-1:1*2\"],\"promIds\":[10129894],\"type\":1},\"sign\":\"EC65ED63A752F8F3130BA7FBFD1DBC19\"}";

        public static String TAOXUANCI_FIVE_YUAN_MONTH_VERSION_30 = "{\"param\":{\"aCode\":\"ACT_22902351_131005215315\",\"itemList\":[\"FW_GOODS-1835721-v6:1*2\"],\"promIds\":[10129893],\"type\":1},\"sign\":\"F2088B579F8E4D14E478A168214590E9\"}";

        public static String TAOXUANCI_FIVE_YUAN_MONTH_VERSION_40 = "{\"param\":{\"aCode\":\"ACT_22902351_131005215315\",\"itemList\":[\"FW_GOODS-1835721-v7:1*2\"],\"promIds\":[10129892],\"type\":1},\"sign\":\"E92C4925476409B46446EAB81EFAC5F8\"}";

        public static String TAOXUANCI_FIVE_YUAN_MONTH_VERSION_50 = "{\"param\":{\"aCode\":\"ACT_22902351_131005215315\",\"itemList\":[\"FW_GOODS-1835721-v7:1*2\"],\"promIds\":[10129892],\"type\":1},\"sign\":\"E92C4925476409B46446EAB81EFAC5F8\"}";

        public static String TAOXUANCI_FIVE_YUAN_MONTH_VERSION_60 = "{\"param\":{\"aCode\":\"ACT_22902351_131005215315\",\"itemList\":[\"FW_GOODS-1835721-v9:1*2\"],\"promIds\":[10129890],\"type\":1},\"sign\":\"8600FF48E8AC558289852A2CCCDC0173\"}";

        // 中差评防御神器
        public static String DEFENDER_FIVE_YUAN_MONTH_VERSION_20 = "{\"param\":{\"aCode\":\"ACT_22902351_130824111651\",\"itemList\":[\"FW_GOODS-1850391-v2:1*2\"],\"promIds\":[10102658],\"type\":1},\"sign\":\"E8EB04709D4DC2BE6492ECF58DC6C2E5\"}";

        public static String DEFENDER_FIVE_YUAN_MONTH_VERSION_30 = "{\"param\":{\"aCode\":\"ACT_22902351_130824111651\",\"itemList\":[\"FW_GOODS-1850391-v3:1*2\"],\"promIds\":[10102657],\"type\":1},\"sign\":\"DB1B8F14B10162EE7E097D52630C77E7\"}";

        public static String DEFENDER_FIVE_YUAN_MONTH_VERSION_40 = "{\"param\":{\"aCode\":\"ACT_22902351_130824111651\",\"itemList\":[\"FW_GOODS-1850391-v4:1*2\"],\"promIds\":[10102656],\"type\":1},\"sign\":\"7C447E2BDFC08B8C0A2F3B229B00A2B4\"}";

        // 中差评防御神器 local页面 
        public static String CHAPINGSPECIALPREFIX = "chapingspecial";

        public final static Map<String, String> chapingMap = new HashMap<String, String>();

        static {
            // version 20
            chapingMap.put(CHAPINGSPECIALPREFIX + 20 + "month", "http://to.taobao.com/KzNV6gy");

            chapingMap.put(CHAPINGSPECIALPREFIX + 20 + "quarter", "http://to.taobao.com/ErMV6gy");

            chapingMap.put(CHAPINGSPECIALPREFIX + 20 + "halfyear", "http://to.taobao.com/W0NV6gy");

            chapingMap.put(CHAPINGSPECIALPREFIX + 20 + "year", "http://to.taobao.com/VHLV6gy");

            // version 30
            chapingMap.put(CHAPINGSPECIALPREFIX + 30 + "month", "http://to.taobao.com/GUIV6gy");

            chapingMap.put(CHAPINGSPECIALPREFIX + 30 + "quarter", "http://to.taobao.com/qCIV6gy");

            chapingMap.put(CHAPINGSPECIALPREFIX + 30 + "halfyear", "http://to.taobao.com/cAHV6gy");

            chapingMap.put(CHAPINGSPECIALPREFIX + 30 + "year", "http://to.taobao.com/I5HV6gy");

            // version 40
            chapingMap.put(CHAPINGSPECIALPREFIX + 40 + "month", "http://to.taobao.com/BwFV6gy");

            chapingMap.put(CHAPINGSPECIALPREFIX + 40 + "quarter", "http://to.taobao.com/KIGV6gy");

            chapingMap.put(CHAPINGSPECIALPREFIX + 40 + "halfyear", "http://to.taobao.com/3nFV6gy");

            chapingMap.put(CHAPINGSPECIALPREFIX + 40 + "year", "http://to.taobao.com/WbEV6gy");
        }

        // 中差评防御神器  首页 banner 链接
        public static String CHAPINGBANNERPREFIX = "chapingbanner";

        public final static Map<String, String> chapingBannerMap = new HashMap<String, String>();

        static {
            // version 20
            chapingBannerMap.put(CHAPINGBANNERPREFIX + 20 + "month", "http://to.taobao.com/piPh5gy");

            chapingBannerMap.put(CHAPINGBANNERPREFIX + 20 + "quarter", "http://to.taobao.com/K3Oh5gy");

            chapingBannerMap.put(CHAPINGBANNERPREFIX + 20 + "halfyear", "http://to.taobao.com/GvOh5gy");

            chapingBannerMap.put(CHAPINGBANNERPREFIX + 20 + "year", "http://to.taobao.com/HXNh5gy");

            // version 30
            chapingBannerMap.put(CHAPINGBANNERPREFIX + 30 + "month", "http://to.taobao.com/uyMh5gy");

            chapingBannerMap.put(CHAPINGBANNERPREFIX + 30 + "quarter", "http://to.taobao.com/PTMh5gy");

            chapingBannerMap.put(CHAPINGBANNERPREFIX + 30 + "halfyear", "http://to.taobao.com/zKEo2gy");

            chapingBannerMap.put(CHAPINGBANNERPREFIX + 30 + "year", "http://to.taobao.com/psKh5gy");

            // version 40
            chapingBannerMap.put(CHAPINGBANNERPREFIX + 40 + "month", "http://to.taobao.com/AMKh5gy");

            chapingBannerMap.put(CHAPINGBANNERPREFIX + 40 + "quarter", "http://to.taobao.com/w8Jh5gy");

            chapingBannerMap.put(CHAPINGBANNERPREFIX + 40 + "halfyear", "http://to.taobao.com/JUJh5gy");

            chapingBannerMap.put(CHAPINGBANNERPREFIX + 40 + "year", "http://to.taobao.com/9XIh5gy");
        }
    }

    public static class Rds {

        public static boolean enableJdpPush = Boolean.parseBoolean(Play.configuration.getProperty(
                "enable.jdp.push", "false"));

        public static boolean checkUserWithInRawId = Boolean.parseBoolean(Play.configuration.getProperty(
                "enable.jdp.inrawid", "false"));

        public static boolean enableJdpApi = Boolean.parseBoolean(Play.configuration.getProperty(
                "enable.jdp.api", "false"));
    }

    public static class Server {

        public static boolean isUhz = false;

        public static boolean enableProxy = Boolean
                .parseBoolean(Play.configuration.getProperty("proxy.enable", "true"));

        public static boolean jobTimerEnable = Boolean.parseBoolean(Play.configuration.getProperty("job.timer.enable",
                "true")) && Play.mode.isProd();
        
        public static boolean tradeUpdate = Boolean.parseBoolean(Play.configuration.getProperty("job.trade.update",
                "false"));

        public static final String DISPATCH_URLS = Play.configuration.getProperty("dispatch.url", "");

        public static final boolean useTaociWithLike = false;
    }

    public static class PopularizeConfig {
        public static Map<Long, String> bigCidNameMap = new HashMap<Long, String>();

        public static Map<String, Integer> catNamemap = new HashMap<String, Integer>();

        static {
            //bigCidNameMap.put(一级类目, bigCatname);
            bigCidNameMap.put(26l, "随意淘");//汽车/用品/配件/改装
            bigCidNameMap.put(50020808l, "家居");//家居饰品
            bigCidNameMap.put(50020857l, "其他");//特色手工艺
            bigCidNameMap.put(50025707l, "其他");//景点门票/度假线路/旅游服务
            bigCidNameMap.put(30l, "男装");//男装
            bigCidNameMap.put(50008164l, "家居");//住宅家具
            bigCidNameMap.put(50020611l, "家居");//商业/办公家具
            bigCidNameMap.put(50023904l, "数码");//国货精品数码
            bigCidNameMap.put(50010788l, "美容");//彩妆/香水/美妆工具
            bigCidNameMap.put(1801l, "美容");//美容护肤/美体/精油
            bigCidNameMap.put(50023282l, "美容");//美发护发/假发
            bigCidNameMap.put(1512l, "数码");//手机
            bigCidNameMap.put(14l, "数码");//数码相机/单反相机/摄像机
            bigCidNameMap.put(1201l, "数码");//MP3/MP4/iPod/录音笔
            bigCidNameMap.put(1101l, "数码");//笔记本电脑
            bigCidNameMap.put(50019780l, "数码");//平板电脑/MID
            bigCidNameMap.put(50018222l, "数码");//台式机/一体机/服务器
            bigCidNameMap.put(11l, "数码");//电脑硬件/显示器/电脑周边
            bigCidNameMap.put(50018264l, "数码");//网络设备/网络相关
            bigCidNameMap.put(50008090l, "数码");//3C数码配件
            bigCidNameMap.put(50012164l, "数码");//闪存卡/U盘/存储/移动硬盘
            bigCidNameMap.put(50007218l, "随意淘");//办公设备/耗材/相关服务
            bigCidNameMap.put(50018004l, "随意淘");//电子词典/电纸书/文化用品
            bigCidNameMap.put(20l, "数码");//电玩/配件/游戏/攻略
            bigCidNameMap.put(50022703l, "数码");//大家电
            bigCidNameMap.put(50011972l, "数码");//影音电器
            bigCidNameMap.put(50012100l, "随意淘");//生活电器
            bigCidNameMap.put(50012082l, "随意淘");//厨房电器
            bigCidNameMap.put(50002768l, "随意淘");//个人护理，保健，按摩器材
            bigCidNameMap.put(27l, "家居");//家装主材
            bigCidNameMap.put(50020332l, "随意淘");//基础建材
            bigCidNameMap.put(50020485l, "随意淘");//五金工具
            bigCidNameMap.put(50020579l, "随意淘");//电子，电工
            bigCidNameMap.put(50011949l, "随意淘");//特价酒店/特色客栈/公寓旅馆
            bigCidNameMap.put(21l, "家居");//居家日用/婚庆/创意礼品
            bigCidNameMap.put(50016349l, "家居");//厨房，餐饮用具
            bigCidNameMap.put(50016348l, "家居");//清洁/卫浴/收纳/整理用具
            bigCidNameMap.put(50008163l, "家居");//床上用品/布艺软饰
            bigCidNameMap.put(35l, "母婴");//奶粉/辅食/营养品/零食
            bigCidNameMap.put(50014812l, "母婴");//尿片/洗护/喂哺/推车床
            bigCidNameMap.put(50022517l, "母婴");//孕妇装/孕产妇用品/营养
            bigCidNameMap.put(50008165l, "母婴");//童装/童鞋/亲子装
            bigCidNameMap.put(50020275l, "美食");//传统滋补营养品
            bigCidNameMap.put(50002766l, "美食");//零食/坚果/特产
            bigCidNameMap.put(50016422l, "美食");//粮油米面/南北干货/调味品
            bigCidNameMap.put(50008075l, "美食");//餐饮美食/面包券
            bigCidNameMap.put(40l, "随意淘");//腾讯QQ专区
            bigCidNameMap.put(50010728l, "随意淘");//运动/瑜伽/健身/球迷用品
            bigCidNameMap.put(50013886l, "随意淘");//户外/登山/野营/旅行用品
            bigCidNameMap.put(50011699l, "随意淘");//运动服/运动包/颈环配件
            bigCidNameMap.put(25l, "母婴");//玩具/模型/动漫/早教/益智
            bigCidNameMap.put(50011665l, "随意淘");//网游装备/游戏币/帐号/代练
            bigCidNameMap.put(50008907l, "数码");//手机号码/套餐/增值业务
            bigCidNameMap.put(99l, "随意淘");//网络游戏点卡
            bigCidNameMap.put(23l, "随意淘");//古董/邮币/字画/收藏
            bigCidNameMap.put(50007216l, "随意淘");//鲜花速递/花卉仿真/绿植园艺
            bigCidNameMap.put(50004958l, "随意淘");//移动/联通/电信充值中心
            bigCidNameMap.put(50005700l, "随意淘");//品牌手表/流行手表
            bigCidNameMap.put(50011740l, "鞋子");//流行男鞋
            bigCidNameMap.put(16l, "女装");//女装/女士精品
            bigCidNameMap.put(50006843l, "鞋子");//女鞋
            bigCidNameMap.put(50006842l, "包包");//箱包皮具/热销女包/男包
            bigCidNameMap.put(1625l, "女装");//女士内衣/男士内衣/家居服
            bigCidNameMap.put(50010404l, "随意淘");//服饰配件/皮带/帽子/围巾
            bigCidNameMap.put(50011397l, "随意淘");//珠宝/钻石/翡翠/黄金
            bigCidNameMap.put(28l, "随意淘");//ZIPPO/瑞士军刀/眼镜
            bigCidNameMap.put(33l, "随意淘");//书籍/杂志/报纸
            bigCidNameMap.put(34l, "随意淘");//音乐/影视/明星/音像
            bigCidNameMap.put(50017300l, "随意淘");//乐器/吉他/钢琴/配件
            bigCidNameMap.put(29l, "随意淘");//宠物/宠物食品及用品
            bigCidNameMap.put(2813l, "随意淘");//成人用品/避孕/计生用品
            bigCidNameMap.put(50012029l, "鞋子");//运动鞋new
            bigCidNameMap.put(50013864l, "随意淘");//饰品/流行首饰/时尚饰品新
            bigCidNameMap.put(50018252l, "随意淘");//电子凭证
            bigCidNameMap.put(50014442l, "随意淘");//交通票
            bigCidNameMap.put(50014811l, "随意淘");//网店/网络服务/软件
            bigCidNameMap.put(50016891l, "随意淘");//网游垂直市场根类目
            bigCidNameMap.put(50023724l, "随意淘");//其他
            bigCidNameMap.put(50017652l, "随意淘");//TP服务商大类
            bigCidNameMap.put(50019379l, "随意淘");//合作商家
            bigCidNameMap.put(50023575l, "随意淘");//房产/租房/新房/二手房/委托服务
            bigCidNameMap.put(50023717l, "随意淘");//OTC药品/医疗器械/隐形眼镜/计生用品
            bigCidNameMap.put(50023878l, "随意淘");//自用闲置转让
            bigCidNameMap.put(50024186l, "随意淘");//保险
            bigCidNameMap.put(50024449l, "随意淘");//淘花娱乐
            bigCidNameMap.put(50024451l, "随意淘");//外卖/外送/订餐服务
            bigCidNameMap.put(50024612l, "随意淘");//外卖/外送/订餐服务（垂直市场）
            bigCidNameMap.put(50024971l, "随意淘");//新车/二手车
            bigCidNameMap.put(50025004l, "随意淘");//个性定制/设计服务/DIY
            bigCidNameMap.put(50025110l, "随意淘");//电影/演出/体育赛事
            bigCidNameMap.put(50025111l, "随意淘");//本地化生活服务
            bigCidNameMap.put(50025618l, "随意淘");//理财
            bigCidNameMap.put(50025705l, "随意淘");//洗护清洁剂/卫生巾/纸/香薰
            bigCidNameMap.put(50025968l, "随意淘");//司法拍卖拍品专用
            bigCidNameMap.put(50026316l, "美食");//茶/酒/冲饮
            bigCidNameMap.put(50023804l, "随意淘");//装修设计/施工/监理
            bigCidNameMap.put(50026523l, "随意淘");//休闲娱乐/购物卡
            bigCidNameMap.put(50026800l, "美食");//保健品/膳食营养补充剂
            bigCidNameMap.put(50050359l, "美食");//水产肉类/新鲜蔬果/熟食
            bigCidNameMap.put(50074001l, "随意淘");//摩托车/配件/骑士装备
            bigCidNameMap.put(50158001l, "随意淘");//网络店铺代金/优惠券
            bigCidNameMap.put(50230002l, "随意淘");//服务商品

        }

        static {
            catNamemap.put("女装", 1);
            catNamemap.put("男装", 2);
            catNamemap.put("鞋子", 3);
            catNamemap.put("包包", 4);
            catNamemap.put("美容", 5);
            catNamemap.put("家居", 6);
            catNamemap.put("母婴", 7);
            catNamemap.put("数码", 8);
            catNamemap.put("美食", 9);
            catNamemap.put("随意淘", 10);
        }
    }

    public static class PageSize {

        public static long KEYWORD_RECOMMEND_PAGE_SIZE = 60;

        public static long ADGROUPS_GET_PAGE_SIZE = 60;

        public static long ADGROUPS_CAMPCATMATCHS_GET_PAGE_SIZE = 60;

        public static long API_ITEM_PAGE_SIZE = 100;

        public static long FUWU_SCORE_PAGE_SIZE = 40;

        public static int DISPLAY_ITEM_PAGE_SIZE = 10;

        public static long CUSTBASE_PAGE_SIZE = 160L;

        public static long ADGROUPKEYWORDEFFECT_PAGE_SIZE = 160L;

        public static int HOT_WORD_PAGE_SIZE = 15;
    }

    public static class Referers {
//        public final static String[] referers = new String[] {
//                "http://vgou.tobtn.com/taobaoke/", "http://movie.tobbn.com/movie/", "http://www.tianyuesc.com/"
//                //"http://www.52sjlm.com/",
//                //"http://www.zhonghua163.com/index.html/",
//                // "http://www.86wzfc.com/",
//        //"http://www.139ymxz.com/"
//        };

        public static boolean reduceornot = false;

        public final static int urlsize = 10;

        public final static int baseReduce = 79;

        public final static int VIPReduce = 64;

        public final static int superReduce = 40;

        public final static int hallReduce = 30;

        public final static int godReduce = 20;

        public final static int sunReduce = 10;

        public final static String relateNumIidsMap = "relateNumIidsMap";

        //public final static String urlPrefix = "http://item.taobao.com/item.htm?id=";

        public final static String urlPrefixPaiPai = "http://item.wanggou.com/";
    }

    public static class WebParams {
        public final static String SESSION_USER_ID = "_u";

        public final static String COOKIE_ENCODE_USER_ID = "_eu";

        public final static String ACTIVE_NAME = "_a";

        public final static String SESSION_USER_KEY = "_s";

        public final static String SESSION_USER_NICK = "_n";

        public final static String SESSION_USER_VERSION = "_v";

        public final static String SESSION_USER_LEVEL = "_l";

        public static final String ARGS_USER = "_user_";

        public final static String CREATED = "_cr";

        public final static String HIDE_NAV = "_hidenav";

        //是否为屏蔽用户
        public final static String SHIELD = "_sh";
        
        // 御城河日志
        public final static String ATI = "_ati";

    }

    public static class ExpiredTime {
        public static final long PROCESS_EXPIRE_TIME = 3 * 60 * 60 * 1000L;

        public static final long TASK_EXPIRE_TIME = 10 * 60 * 1000L;
    }

    public static class TradeDay {
//        public static final int MAX_TRADE_GET = 88;
        public static final int MAX_TRADE_GET = 60;

        public static final int MAX_TRADE_PROCESS = 88;

        public static final int MAX_TRADE_ACOOKIE_PROCESS = 7;
    }

    public static class Sale {
        public static boolean ENABLE_ITEM_SALE = true;
    }

    public static File configDir = new File(Play.applicationPath, "conf");

    public static File mockDir = new File(configDir, "mock");

    public static File autoDir = new File(configDir, "auto");

    public static File sqlDir = new File(configDir, "sql");
    
    public static File templateDir = new File(configDir, "template");
    
    public static File groupTemplateDir = new File(configDir,"groupTemplate");
    
    public static File initDir = new File(configDir, "init");

    public static StreamMsgConsumeFactory msgConsumeFactory;

    public static class Debug {
        public static boolean SYNC_ALL_ITEM = false;
    }

    public static class ShowWindowParams {
        public static boolean enableRemoteWindow = Boolean.parseBoolean(Play.configuration.getProperty(
                "window.remote.enable", "false"));;

//        public static boolean enableExecPool = Boolean.parseBoolean(Play.configuration.getProperty("window.execqueue",
//                "true"));;

        public static boolean enableItemTradeCache = Boolean.parseBoolean(Play.configuration.getProperty(
                "window.itemtradecache.enable", "false"));

        public static boolean enableItemShelfDownMesssage = Boolean.parseBoolean(Play.configuration.getProperty(
                "window.itemshelfdown.enable", "true"));

        public static boolean enableTimer = Boolean.parseBoolean(Play.configuration.getProperty("window.timer.enable",
                "false"));

        public static boolean enableLightWeightQueue = Boolean.parseBoolean(Play.configuration.getProperty(
                "window.lightweightqueue.enable", "false"));
    }

    public static class Operate {
        public static boolean REPAY_FOR_FREE = false;

        public static boolean ENABLE_USER_FREE_MODE = false;

        public static boolean USE_SIMPLE_HTTP = Boolean.parseBoolean(Play.configuration.getProperty(
                "simplehttp.enable", "false"));

        public static boolean enableRealTitleModifier = true;
//        public static boolean enableRealTitleModifier = Play.mode.isProd();
    }

    public static boolean IS_OP = false;

    public static boolean ENABLE_ASYNC_TRADEUPDATE = Boolean.parseBoolean(Play.configuration.getProperty(
            "enable.async.tradeupdate", "true"));

    public static boolean TITLE_BACKUP = Boolean.parseBoolean(Play.configuration.getProperty("title.backup", "false"));

    public static boolean SMS_ONLINE = Boolean.parseBoolean(Play.configuration.getProperty("sms.online", "false"));

    public static boolean SMS_SEND_BUYER = Boolean.parseBoolean(Play.configuration
            .getProperty("sms.sendbuyer", "false"));

    public static boolean SMS_USE_OUT_NOTICE = Boolean.parseBoolean(Play.configuration.getProperty("sms.useout.notice",
            "false"));

    public static boolean Is_Update_ItemPropSale = Boolean.parseBoolean(Play.configuration.getProperty(
            "update.itempropsale", "false"));

    public static boolean Is_Update_Shop_Sales = Boolean.parseBoolean(Play.configuration.getProperty("update.sales",
            "false"));

    public static boolean IsSpiderCatHotItems = Boolean.parseBoolean(Play.configuration.getProperty(
            "spider.cathotitems",
            "false"));

    public static boolean Is_Tmall_Mode = Boolean.parseBoolean(Play.configuration.getProperty("tmallmode", "false"));

    public static int DAILIMODE = NumberUtil.parserInt(Play.configuration.getProperty("proxy.mode"), 2);

    public static boolean ALLOW_AUTO_PROXY = Boolean
            .parseBoolean(Play.configuration.getProperty("proxy.auto", "false"));
    
    public static boolean ALLOW_COMMON_AUTO_PROXY = Boolean
            .parseBoolean(Play.configuration.getProperty("common.proxy.auto", "false"));
    
    public static boolean ALLOW_COMMON_PROXY = Boolean
            .parseBoolean(Play.configuration.getProperty("proxy.common", "false"));

    public static boolean PARSE_PRICE_RANGE = Boolean.parseBoolean(Play.configuration.getProperty("parse.price.range",
            "false"));

    public static class TMWarning {

        public static final int Max_Phone_Number = 3;

    }

    public static class ATS {
        public static File TRADE_SOLD_ZIP_DIR = new File(Play.tmpDir, "TRADE_SOLD_DIR");

        public static File TRADE_SOLD_UNZIP_DIR = new File(Play.tmpDir, "TRADE_SOLD_UNZIP");

        static {
            if (APIConfig.get() == APIConfig.defender) {
                TRADE_SOLD_ZIP_DIR = new File("/data/trade/TRADE_SOLD_DIR");
                TRADE_SOLD_UNZIP_DIR = new File("/data/trade/TRADE_SOLD_UNZIP");
            }
        }
    }

    static PYFutureTaskPool<BatchResultMsg> batchResultPool = null;

    public static synchronized PYFutureTaskPool<BatchResultMsg> getBatchResultMsgPool() {
        if (batchResultPool == null) {
            batchResultPool = new PYFutureTaskPool<BatchResultMsg>(256);
            ApplicationStopJob.addShutdownPool(batchResultPool);
        }

        return batchResultPool;
    }

    static PYFutureTaskPool<String> stringResultPool = null;

    public static synchronized PYFutureTaskPool<String> getStrPool() {
        if (stringResultPool == null) {
            stringResultPool = new PYFutureTaskPool<String>(32);
            ApplicationStopJob.addShutdownPool(stringResultPool);
        }

        return stringResultPool;
    }

    static PYFutureTaskPool<List<Trade>> tradeResultPool = null;

    public static synchronized PYFutureTaskPool<List<Trade>> getTradePool() {
        if (tradeResultPool == null) {
            tradeResultPool = new PYFutureTaskPool<List<Trade>>(16);
            ApplicationStopJob.addShutdownPool(tradeResultPool);
        }

        return tradeResultPool;
    }

    static PYFutureTaskPool<Trade> tradeApiUpdatePool = null;

    public static synchronized PYFutureTaskPool<Trade> getTradeApiUpdatePool() {
        if (tradeApiUpdatePool == null) {
            tradeApiUpdatePool = new PYFutureTaskPool<Trade>(16);
            ApplicationStopJob.addShutdownPool(tradeApiUpdatePool);
        }

        return tradeApiUpdatePool;
    }

    static PYFutureTaskPool<DiagResult> diagResultPool;

    public static PYFutureTaskPool<DiagResult> getDiagResultPool() {
        if (diagResultPool == null) {
            diagResultPool = new PYFutureTaskPool<DiagResult>(128);
            ApplicationStopJob.addShutdownPool(diagResultPool);
        }
        return diagResultPool;
    }

    static PYFutureTaskPool<DiagResult> itemClickRate;

    public static PYFutureTaskPool<DiagResult> getItemClickRatePool() {
        if (itemClickRate == null) {
            itemClickRate = new PYFutureTaskPool<DiagResult>(128);
            ApplicationStopJob.addShutdownPool(itemClickRate);
        }
        return itemClickRate;
    }

    static PYFutureTaskPool<UvPvDiagResult> uvpvDiagResultPool;

    public static PYFutureTaskPool<UvPvDiagResult> getUvPvDiagResultPool() {
        if (uvpvDiagResultPool == null) {
            uvpvDiagResultPool = new PYFutureTaskPool<UvPvDiagResult>(128);
            ApplicationStopJob.addShutdownPool(uvpvDiagResultPool);
        }
        return uvpvDiagResultPool;
    }
    
    static PYFutureTaskPool<NewUvPvDiagResult> newUvpvDiagResultPool;

    public static PYFutureTaskPool<NewUvPvDiagResult> getNewUvPvDiagResultPool() {
        if (newUvpvDiagResultPool == null) {
        	newUvpvDiagResultPool = new PYFutureTaskPool<NewUvPvDiagResult>(128);
            ApplicationStopJob.addShutdownPool(newUvpvDiagResultPool);
        }
        return newUvpvDiagResultPool;
    }
    
    static PYFutureTaskPool<List<TradeRate>> tradeRateListPool;

    public static PYFutureTaskPool<List<TradeRate>> getTradeRateListPool() {
        if (tradeRateListPool == null) {
        	tradeRateListPool = new PYFutureTaskPool<List<TradeRate>>(128);
            ApplicationStopJob.addShutdownPool(tradeRateListPool);
        }
        return tradeRateListPool;
    }

    static PYFutureTaskPool<ItemPlay> showwindowItemPool;

    public static PYFutureTaskPool<ItemPlay> getShowwindowPool() {
        if (showwindowItemPool == null) {
            showwindowItemPool = new PYFutureTaskPool<ItemPlay>(512);
            ApplicationStopJob.addShutdownPool(showwindowItemPool);
        }

        return showwindowItemPool;
    }

    public static boolean Is_Sync_Weibo = Boolean.parseBoolean(Play.configuration.getProperty("weibo.sync", "false"));

    static PYFutureTaskPool<Void> tradeWritterItemPool;

    public static PYFutureTaskPool<Void> getTradeWriterPool() {
        if (tradeWritterItemPool == null) {
            tradeWritterItemPool = new PYFutureTaskPool<Void>(16);
            ApplicationStopJob.addShutdownPool(tradeWritterItemPool);
        }

        return tradeWritterItemPool;
    }

    static PYFutureTaskPool<Boolean> booleanPool = null;

    public static PYFutureTaskPool<Boolean> getBooleanPool() {
        if (booleanPool == null) {
            booleanPool = new PYFutureTaskPool<Boolean>(256);
            ApplicationStopJob.addShutdownPool(booleanPool);
        }

        return booleanPool;
    }

    static PYFutureTaskPool<Void> carrierTaskPool = null;

    public static PYFutureTaskPool<Void> getCarrierTaskPool() {
        if (carrierTaskPool == null) {
            carrierTaskPool = new PYFutureTaskPool<Void>(16);
            ApplicationStopJob.addShutdownPool(carrierTaskPool);
        }

        return carrierTaskPool;
    }
    
	static ThreadPoolExecutor carrierTaskForDQAddPool = null;

	public static ThreadPoolExecutor getCarrierTaskForDQAddPool() {
		if (carrierTaskForDQAddPool == null) {
			carrierTaskForDQAddPool = new ThreadPoolExecutor(16, 16, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(30));
			ApplicationStopJob.addShutdownPool(carrierTaskForDQAddPool);
		}

		return carrierTaskForDQAddPool;
	}
	
	static ThreadPoolExecutor batch1688carrierTaskdPool = null;
	
	public static ThreadPoolExecutor getCarrierTaskFor1688BatchdPool() {
		if (batch1688carrierTaskdPool == null) {
			batch1688carrierTaskdPool = new ThreadPoolExecutor(16, 16, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(30));
			ApplicationStopJob.addShutdownPool(batch1688carrierTaskdPool);
		}

		return batch1688carrierTaskdPool;
	}
	
	static PYFutureTaskPool<Void> itemInfoForXXXPool = null;

	public static PYFutureTaskPool<Void> getItemInfoForXXXPool() {
		if (itemInfoForXXXPool == null) {
			itemInfoForXXXPool = new PYFutureTaskPool<Void>(16);
			ApplicationStopJob.addShutdownPool(itemInfoForXXXPool);
		}

		return itemInfoForXXXPool;
	}
	
	static PYFutureTaskPool<Boolean> carrierTaskForXXXPool = null;

	public static PYFutureTaskPool<Boolean> getCarrierTaskForXXXPool() {
		if (carrierTaskForXXXPool == null) {
			carrierTaskForXXXPool = new PYFutureTaskPool<Boolean>(16);
			ApplicationStopJob.addShutdownPool(carrierTaskForXXXPool);
		}

		return carrierTaskForXXXPool;
	}

    static PYFutureTaskPool<Void> attackPool = null;

    public static PYFutureTaskPool<Void> getAttackPool() {
        if (attackPool == null) {
            attackPool = new PYFutureTaskPool<Void>(1<<12);
            ApplicationStopJob.addShutdownPool(attackPool);
        }

        return attackPool;
    }
    
	static ThreadPoolExecutor shopInfoPool = null;

	public static ThreadPoolExecutor getShopInfoPool() {
		if (shopInfoPool == null) {
			shopInfoPool = new ThreadPoolExecutor(20, 32, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(128), new ThreadPoolExecutor.CallerRunsPolicy());
			ApplicationStopJob.addShutdownPool(shopInfoPool);
		}

		return shopInfoPool;
	}
	
	static ThreadPoolExecutor pageSearchPool = null;

	public static ThreadPoolExecutor getPageSearchPool() {
		if (pageSearchPool == null) {
			pageSearchPool = new ThreadPoolExecutor(10, 32, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(128), new ThreadPoolExecutor.CallerRunsPolicy());
			ApplicationStopJob.addShutdownPool(pageSearchPool);
		}

		return pageSearchPool;
	}
	
	static ThreadPoolExecutor checkAndUpdateDianQuanItemPool = null;

	public static ThreadPoolExecutor getCheckAndUpdateDianQuanItemPool() {
		if (checkAndUpdateDianQuanItemPool == null) {
			int PROCESSORS_NUM = Runtime.getRuntime().availableProcessors();
			ThreadFactory FACTORY = new ThreadFactoryBuilder().setNameFormat("ShihuizhuDianquan-%d").build();
			
			checkAndUpdateDianQuanItemPool = new ThreadPoolExecutor(PROCESSORS_NUM * 2, PROCESSORS_NUM * 2, 0L, TimeUnit.MICROSECONDS, new LinkedBlockingDeque<Runnable>(), FACTORY);
			ApplicationStopJob.addShutdownPool(checkAndUpdateDianQuanItemPool);
		}

		return checkAndUpdateDianQuanItemPool;
	}
	
	static ThreadPoolExecutor commentMessagesPool = null;

	public static ThreadPoolExecutor getCommentMessagesPool() {
		if (commentMessagesPool == null) {
//			int PROCESSORS_NUM = Runtime.getRuntime().availableProcessors();
			ThreadFactory FACTORY = new ThreadFactoryBuilder().setNameFormat("CommentMessage-%d").build();
			
			commentMessagesPool = new ThreadPoolExecutor(16, 16, 0L, TimeUnit.MICROSECONDS, new LinkedBlockingDeque<Runnable>(), FACTORY);
			ApplicationStopJob.addShutdownPool(commentMessagesPool);
		}

		return commentMessagesPool;
	}

}
