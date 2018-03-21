
package configs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;

public class BusConfigs {

    static final Logger log = LoggerFactory.getLogger(BusConfigs.class);

    public static final String TAG = "BusConfigs";

    public static boolean RPT_ENABLE = Boolean.parseBoolean(Play.configuration.getProperty("rpt.enable", "true"));


    public static class RptConfig {

        public static long ONE_DAY_RPT_SHOW = 1;
        
        public static long RPT_TRIPPLE_DAY = 3;

        public static long RPT_DAY_SHOW = 7;

        public static long MAX_RPT_GET = 7;

        public static long MAX_CUST_RPT_GET = 30;
    }

    public static class PageSize {

        public static long KEYWORD_RECOMMEND_PAGE_SIZE = 60;

        public static long ADGROUPS_GET_PAGE_SIZE = 60;

        public static long ADGROUPS_CAMPCATMATCHS_GET_PAGE_SIZE = 60;

        public static long ITEM_PAGE_SIZE = 200L;

        public static long CUSTBASE_PAGE_SIZE = 160L;

        public static long ADGROUPKEYWORDEFFECT_PAGE_SIZE = 160L;

        public static int HOT_WORD_PAGE_SIZE = 15;
        
        
        public static long API_ITEM_PAGE_SIZE = 50L;

        public static int DISPLAY_ITEM_PAGE_SIZE = 10;

    }
}
