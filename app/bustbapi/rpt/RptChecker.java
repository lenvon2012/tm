package bustbapi.rpt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RptChecker {

    private final static Logger log = LoggerFactory.getLogger(RptChecker.class);

    private static RptChecker instance = new RptChecker();

    public static class Type {
        public static int CUST_BASE = 1;
        public static int CUST_EFFECT = 2;
        public static int ADGROUPWORD_BASE = 3;
        public static int ADGROUPWORD_EFFECT = 4;
        public static int CAMPAIGNADGROUP_BASE = 5;
        public static int CAMPAIGNADGROUP_EFFECT = 6;
        public static int CAMPAIGN_BASE = 7;
        public static int CAMPAIGN_EFFECT = 8;
    }

    public static class TypeStr {
        public static String CUST_BASE = "{\"simba_rpt_custbase_get_response\":{\"rpt_cust_base_list\":{}}}";
        public static String CUST_EFFECT = "{\"simba_rpt_custeffect_get_response\":{\"rpt_cust_effect_list\":{}}}";
        public static String ADGROUPWORD_BASE = "{\"simba_rpt_adgroupkeywordbase_get_response\":{\"rpt_adgroupkeyword_base_list\":{}}}";
        public static String ADGROUPWORD_EFFECT = "{\"simba_rpt_adgroupkeywordeffect_get_response\":{\"rpt_adgroupkeyword_effect_list\":{}}}";
        public static String CAMPAIGNADGROUP_BASE = "{\"simba_rpt_campadgroupbase_get_response\":{\"rpt_campadgroup_base_list\":{}}}";
        public static String CAMPAIGNADGROUP_EFFECT = "{\"simba_rpt_campadgroupeffect_get_response\":{\"rpt_campadgroup_effect_list\":{}}}";
        public static String CAMPAIGN_BASE = "{\"simba_rpt_campaignbase_get_response\":{\"rpt_campaign_base_list\":{}}}";
        public static String CAMPAIGN_EFFECT = "{\"simba_rpt_campaigneffect_get_response\":{\"rpt_campaign_effect_list\":{}}}";

    }

    public static RptChecker getInstance() {
        return instance;
    }

    public static boolean checkVaild(String str, int type) {
        if (type == Type.CUST_BASE && str.equals(TypeStr.CUST_BASE)) {
            return false;
        } else if (type == Type.CUST_EFFECT && str.equals(TypeStr.CUST_EFFECT)) {
            return false;
        } else if (type == Type.ADGROUPWORD_BASE && str.equals(TypeStr.ADGROUPWORD_BASE)) {
            return false;
        } else if (type == Type.ADGROUPWORD_EFFECT && str.equals(TypeStr.ADGROUPWORD_EFFECT)) {
            return false;
        } else if (type == Type.CAMPAIGNADGROUP_BASE && str.equals(TypeStr.CAMPAIGNADGROUP_BASE)) {
            return false;
        } else if (type == Type.CAMPAIGNADGROUP_EFFECT && str.equals(TypeStr.CAMPAIGNADGROUP_EFFECT)) {
            return false;
        } else if (type == Type.CAMPAIGN_BASE && str.equals(TypeStr.CAMPAIGN_BASE)) {
            return false;
        } else if (type == Type.CAMPAIGN_EFFECT && str.equals(TypeStr.CAMPAIGN_EFFECT)) {
            return false;
        }

        return true;
    }
}
