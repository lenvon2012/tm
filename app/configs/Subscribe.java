
package configs;

import java.util.HashMap;
import java.util.Map;

import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import controllers.APIConfig;

public class Subscribe {

    private static final Logger log = LoggerFactory.getLogger(Subscribe.class);

    public static final String TAG = "Subscribe";

//    public static String ARTICAL_CODE = Play.configuration.getProperty("artical.code", "ts-1820059");

    public static final String ITEM_CODE_FREE = "ts-1820059-1";

    public static final String ITEM_CODE_BASE = "ts-1820059-5";

    public static final String ITEM_CODE_VIP = "ts-1820059-3";

    public static final String[] ALL_ITEM_CODE = new String[] {
            ITEM_CODE_FREE, ITEM_CODE_BASE, ITEM_CODE_VIP
    };

    public static class Version {

        public static final int BLACK = -1;

        public static final int FREE = 1;

        public static final int BASE = 10;
        
        public static final int LL = 15; //琳琅

        public static final int VIP = 20;

        public static final int SUPER = 30;

        public static final int HALL = 40;

        public static final int GOD = 50;

        public static final int SUN = 60;

        public static final int DAWEI = 70;

        public static final int CUOCUO = 80;
    }

    public static boolean ownsSmallVersion(User user) {
        return user.getVersion() >= Version.BASE;
    }

    public static Map<String, Integer> subsricbeCache = new HashMap<String, Integer>();

    static {
        subsricbeCache.put("ts-1820059-1", Version.FREE);
//        subsricbeCache.put("ts-1820059-2", Version.FREE);
//        subsricbeCache.put("ts-1820059-5", Version.FREE);
        subsricbeCache.put("ts-1820059-5", Version.BASE);
//        subsricbeCache.put("ts-1820059-1", Version.BASE);
        subsricbeCache.put("ts-1820059-3", Version.VIP);

//        subsricbeCache.put("ts-1820059-4", Version.VIP);
//        subsricbeCache.put("ts-1820059-4", Version.VIP);
    }

    public static final int getVersionByCode(String code) {

        if (StringUtils.isEmpty(code)) {
            return Version.FREE;
        }

        APIConfig config = APIConfig.get();
        if (config.getTryCodesSet().contains(code)) {
            return Version.BLACK;
        } else if (config.getFreeCodesSet().contains(code)) {
            return Version.FREE;
        } else if (config.getBaseCodesSet().contains(code)) {
            return Version.BASE;
        } else if (config.getLLCodesSet().contains(code)) {
            return Version.LL;
        } else if (config.getVIPCodesSet().contains(code)) {
            return Version.VIP;
        } else if (config.getSuperCodesSet().contains(code)) {
            return Version.SUPER;
        } else if (config.getHallCodesSet().contains(code)) {
            return Version.HALL;
        } else if (config.getGodCodeSet().contains(code)) {
            return Version.GOD;
        } else if (config.getSunCodeSet().contains(code)) {
            return Version.SUN;
        } else if (config.getDaweiCodeSet().contains(code)) {
            return Version.DAWEI;
        } else if (config.getCuocuoCodeSet().contains(code)) {
            return Version.CUOCUO;
        }

        if (ITEM_CODE_FREE.equals(code)) {
            return Version.FREE;
        } else if (ITEM_CODE_BASE.equals(code)) {
            return Version.BASE;
        } else if (ITEM_CODE_VIP.equals(code)) {
            return Version.VIP;
        }

        Integer versionCode = subsricbeCache.get(code);
        if (versionCode != null) {
            return versionCode.intValue();
        }

        return Version.FREE;
    }

}
