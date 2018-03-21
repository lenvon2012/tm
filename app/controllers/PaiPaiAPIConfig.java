
package controllers;

import java.util.HashMap;
import java.util.Map;

import job.click.HourlyCheckerJob;
import models.user.User;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import configs.Subscribe.Version;

public abstract class PaiPaiAPIConfig extends APIConfig {

    private static final Logger log = LoggerFactory.getLogger(PaiPaiAPIConfig.class);

    public static final String TAG = "PaiPaiAPIConfig";

    public PaiPaiAPIConfig(int app, String secret) {
        super(app, secret);

    }

    public String getAppOAuthID() {
        return this.apiKey;
    }

    public String getAppOAuthkey() {
        return this.secret;
    }

    /*
    private String appOAuthID;

    private String appOAuthkey;

    public PaiPaiAPIConfig(String appOAuthID, String appOAuthkey) {
        super();
        this.appOAuthID = appOAuthID;
        this.appOAuthkey = appOAuthkey;
    }

    public String getAppOAuthID() {
        return appOAuthID;
    }

    public void setAppOAuthID(String appOAuthID) {
        this.appOAuthID = appOAuthID;
    }

    public String getAppOAuthkey() {
        return appOAuthkey;
    }

    public void setAppOAuthkey(String appOAuthkey) {
        this.appOAuthkey = appOAuthkey;
    }
    */
//    public static PaiPaiAPIConfig apiConfig = new PaiPaiAPIConfig("700132927", "9NA7Jjl21ORnJniT");

    public Platform getPlatform() {
        return Platform.paipai;
    }

    static PaiPaiAPIConfig pConfig = null;

    public static PaiPaiAPIConfig get() {
        if (pConfig != null) {
            return pConfig;
        }

        APIConfig config = APIConfig.get();
        log.info("[current config : ]" + config);
        if (config instanceof PaiPaiAPIConfig) {
            pConfig = (PaiPaiAPIConfig) config;
        } else {
            pConfig = null;
        }

        return pConfig;

    }

    public static APIConfig paipaiweigou = new PaiPaiAPPWeigou(700132427, "Bt0qp5KsIHFAvuoT");

    static class PaiPaiAPPWeigou extends PaiPaiAPIConfig {

        public PaiPaiAPPWeigou(int app, String secret) {
            super(app, secret);

            this.subCode = "";
            /*this.tryCodesSet.add("");
            this.freeCodesSet.add("");
            this.baseCodesSet.add("");
            this.VIPCodesSet.add("");
            this.superCodesSet.add("");
            this.hallCodeSet.add("");
            this.godCodeSet.add("");
            this.sunCodeSet.add("");
            this.daweiCodeSet.add("");
            this.cuocuoCodeSet.add("");*/

            /* 拍拍微购 */
            this.tryCodesSet.add("2746");
            this.freeCodesSet.add("2756");
            this.VIPCodesSet.add("2749");
            this.superCodesSet.add("2940");
            this.hallCodeSet.add("2941");
            this.godCodeSet.add("2947");
            this.sunCodeSet.add("2950");
            this.daweiCodeSet.add("2978");
        }

        @Override
        public void afterLogin(User user, String itemCode, boolean isFirst, Boolean isQianniu) {
            PaiPaiWeigou.index();
        }

        @Override
        public void beforeLogin() {
            PaiPaiPromoteSite.goIndex();
        }

        final static String[] referers = new String[] {
                "http://www.youmiguang.com"
        };

        public String[] getReferes() {
            return referers;
        }

        @Override
        public void doOnStartUpAsync() {
            HourlyCheckerJob.HOUR_JOB_ENABLE = true;

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
            vernameMap.put(Version.VIP, "3个优质位+1个热销位");
            vernameMap.put(Version.SUPER, "5个优质位+2个热销位");
            vernameMap.put(Version.HALL, "10个优质位+3个热销位");
            vernameMap.put(Version.GOD, "20个优质位+8个热销位");
            vernameMap.put(Version.SUN, "30个优质位+10个热销位");
            vernameMap.put(Version.DAWEI, "旗舰版-无限推广位");
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
            /*if (StringUtils.isEmpty(action)) {
                return true;
            }
            String target = action.toLowerCase();
            for (String prefix : allowedPrefix) {
                if (target.startsWith(prefix)) {
                    return true;
                }
            }
            return false;*/

            return true;
        }

        public String getName() {
            return "有米逛";
        }

        final static Map<String, String> paramStrs = new HashMap<String, String>();

        @Override
        public Map<String, String> getSellLinkParamStr() {
            if (!paramStrs.isEmpty()) {
                return paramStrs;
            }

            String paramCode = "{\"param\":{\"aCode\":\"ACT_333336410_130604231711\",\"itemList\":[\"FW_GOODS-1848326-v6:3*2\"],\"promIds\":[10065575],\"type\":1},\"sign\":\"76F03633A11B283E3F7B89474054B87D\"}";
            paramStrs.put("350版本", paramCode);
            return paramStrs;
        }

    }

    public static APIConfig paipailetuiguang = new PaiPaiAPPLeTuiguang(700155239, "rXLY7awlIEwB9qLi");

    static class PaiPaiAPPLeTuiguang extends PaiPaiAPIConfig {

        public PaiPaiAPPLeTuiguang(int app, String secret) {
            super(app, secret);

            this.subCode = "";
            /*this.tryCodesSet.add("");
            this.freeCodesSet.add("");
            this.baseCodesSet.add("");
            this.VIPCodesSet.add("");
            this.superCodesSet.add("");
            this.hallCodeSet.add("");
            this.godCodeSet.add("");
            this.sunCodeSet.add("");
            this.daweiCodeSet.add("");
            this.cuocuoCodeSet.add("");*/

            /* 拍拍微购 */
//            this.tryCodesSet.add("");
//            this.freeCodesSet.add("2756");
            this.baseCodesSet.add("3140");
            this.VIPCodesSet.add("3141");
            this.superCodesSet.add("3142");
            this.hallCodeSet.add("3143");
            this.godCodeSet.add("3144");
            this.sunCodeSet.add("3152");
            this.daweiCodeSet.add("3216");
        }

        @Override
        public void afterLogin(User user, String itemCode, boolean isFirst, Boolean isQianniu) {
            PaiPaiLeTuiguang.index();
        }

        @Override
        public void beforeLogin() {
            PaiPaiPromoteSite.goIndex();
        }

        final static String[] referers = new String[] {
                "http://www.letuiguang.com"
        };

        public String[] getReferes() {
            return referers;
        }

        @Override
        public void doOnStartUpAsync() {
            HourlyCheckerJob.HOUR_JOB_ENABLE = true;

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
            vernameMap.put(Version.VIP, "3个优质位+1个热销位");
            vernameMap.put(Version.SUPER, "5个优质位+2个热销位");
            vernameMap.put(Version.HALL, "10个优质位+3个热销位");
            vernameMap.put(Version.GOD, "20个优质位+8个热销位");
            vernameMap.put(Version.SUN, "30个优质位+10个热销位");
            vernameMap.put(Version.DAWEI, "至尊不限广告位");
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
            /*if (StringUtils.isEmpty(action)) {
                return true;
            }
            String target = action.toLowerCase();
            for (String prefix : allowedPrefix) {
                if (target.startsWith(prefix)) {
                    return true;
                }
            }
            return false;*/

            return true;
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

            String paramCode = "{\"param\":{\"aCode\":\"ACT_333336410_130604231711\",\"itemList\":[\"FW_GOODS-1848326-v6:3*2\"],\"promIds\":[10065575],\"type\":1},\"sign\":\"76F03633A11B283E3F7B89474054B87D\"}";
            paramStrs.put("350版本", paramCode);
            return paramStrs;
        }

    }

    public static APIConfig paipaidiscount = new PaiPaiAppDiscount(700132314, "teXTpbS08oqhN4Zi");

    public static APIConfig paipaibiaoti = new PaiPaiAppDiscount(700156441, "vmj3QAWk4HEBswYF");

    static class PaiPaiAppDiscount extends PaiPaiAPIConfig {

        public PaiPaiAppDiscount(int app, String secret) {
            super(app, secret);
            // TODO Auto-generated constructor stub
            this.subCode = "";
        }

        @Override
        public void afterLogin(User user, String itemCode, boolean isFirst, Boolean isQianniu) {
            // TODO Auto-generated method stub
            PaiPaiDiscount.Base_Index();
        }

        @Override
        public boolean enableSyncTrade(Long userId) {
            return true;
        }

    }

}
