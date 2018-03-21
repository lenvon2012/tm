
package actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bustbapi.FuwuApis;

public class SaleLinkGenAction {

    private final static Logger log = LoggerFactory.getLogger(SaleLinkGenAction.class);

    public static String POLICY_APP_KEY = "12442245";

    public static String POLICY_APP_SECRET = "80663ff0052dad65fa4bd4cc4aba071f";

    public static class POLICY_PARAM_STR {

        public static String ZUAN_1_1 = "{\"param\":{\"aCode\":\"734131476_121024102944\",\"itemList\":[\"ts-24135-11:1*2\"],\"promIds\":[10014469],\"type\":1},\"sign\":\"7FA6C2EB01E6B65665BEC1D719776991\"}";

        public static String ZUAN_1_3 = "{\"param\":{\"aCode\":\"734131476_121024102944\",\"itemList\":[\"ts-24135-11:3*2\"],\"promIds\":[10014469],\"type\":1},\"sign\":\"41D448E6791F81B7A9B705C22372C80C\"}";

        public static String ZUAN_1_6 = "{\"param\":{\"aCode\":\"734131476_121024102944\",\"itemList\":[\"ts-24135-11:6*2\"],\"promIds\":[10014469],\"type\":1},\"sign\":\"BBF3041EA95508EF9595BB983CC60216\"}";

        public static String ZUAN_1_12 = "{\"param\":{\"aCode\":\"734131476_121024102944\",\"itemList\":[\"ts-24135-11:12*2\"],\"promIds\":[10014469],\"type\":1},\"sign\":\"D308A5AF5A28596F3740B89AAF9F0418\"}";

        public static String BA_1_1 = "{\"param\":{\"aCode\":\"734131476_121024102944\",\"itemList\":[\"ts-24135-15:1*2\"],\"promIds\":[10014472],\"type\":1},\"sign\":\"00973C023EF2CAF437EA235004798C76\"}";

        public static String BA_1_3 = "{\"param\":{\"aCode\":\"734131476_121024102944\",\"itemList\":[\"ts-24135-15:3*2\"],\"promIds\":[10014472],\"type\":1},\"sign\":\"5BA5CF1FDEC1118012033756DC84CE37\"}";

        public static String BA_1_6 = "{\"param\":{\"aCode\":\"734131476_121024102944\",\"itemList\":[\"ts-24135-15:6*2\"],\"promIds\":[10014472],\"type\":1},\"sign\":\"89D5FA14FB690E70BEDDA9ABAC5AEAE5\"}";

        public static String BA_1_12 = "{\"param\":{\"aCode\":\"734131476_121024102944\",\"itemList\":[\"ts-24135-15:12*2\"],\"promIds\":[10014472],\"type\":1},\"sign\":\"EFDD4478FD52BD69FC00BC2B4B302C14\"}";

        public static String GUAN_1_1 = "{\"param\":{\"aCode\":\"734131476_121024102944\",\"itemList\":[\"ts-24135-8:1*2\"],\"promIds\":[10014466],\"type\":1},\"sign\":\"AFCCDC9D769AB1D6927385BE84E928A6\"}";

        public static String GUAN_1_3 = "{\"param\":{\"aCode\":\"734131476_121024102944\",\"itemList\":[\"ts-24135-8:3*2\"],\"promIds\":[10014466],\"type\":1},\"sign\":\"D885DE833F84C552102DDAA139134C32\"}";

        public static String GUAN_1_6 = "{\"param\":{\"aCode\":\"734131476_121024102944\",\"itemList\":[\"ts-24135-8:6*2\"],\"promIds\":[10014466],\"type\":1},\"sign\":\"A0DFC41EA28FD10B7B4E8DCD75D8BB0E\"}";

        public static String GUAN_1_12 = "{\"param\":{\"aCode\":\"734131476_121024102944\",\"itemList\":[\"ts-24135-8:12*2\"],\"promIds\":[10014466],\"type\":1},\"sign\":\"21CEB48A83B7FA8097A20F4001AF9C2D\"}";

        public static String GUAN_2_1 = "{\"param\":{\"aCode\":\"734131476_121024102944\",\"itemList\":[\"ts-24135-12:1*2\"],\"promIds\":[10014470],\"type\":1},\"sign\":\"D564890DF94F94AB5F4EE06C21D386D4\"}";

        public static String GUAN_2_3 = "{\"param\":{\"aCode\":\"734131476_121024102944\",\"itemList\":[\"ts-24135-12:3*2\"],\"promIds\":[10014470],\"type\":1},\"sign\":\"A62A5D80B0D5EBEEB6077DEA26AF492E\"}";

        public static String GUAN_2_6 = "{\"param\":{\"aCode\":\"734131476_121024102944\",\"itemList\":[\"ts-24135-12:6*2\"],\"promIds\":[10014470],\"type\":1},\"sign\":\"FB7261051E29AA40DAD6CFED09243ECD\"}";

        public static String GUAN_2_12 = "{\"param\":{\"aCode\":\"734131476_121024102944\",\"itemList\":[\"ts-24135-12:12*2\"],\"promIds\":[10014470],\"type\":1},\"sign\":\"AF531C2C0434C9C2927BEBB8BE6B779D\"}";

        public static String JINGUAN_1_1 = "{\"param\":{\"aCode\":\"734131476_121024102944\",\"itemList\":[\"ts-24135-9:1*2\"],\"promIds\":[10014467],\"type\":1},\"sign\":\"B7AAFDD6141A1E55742F2FBFBC489B87\"}";

        public static String JINGUAN_1_3 = "{\"param\":{\"aCode\":\"734131476_121024102944\",\"itemList\":[\"ts-24135-9:3*2\"],\"promIds\":[10014467],\"type\":1},\"sign\":\"F046A57AC9E3F77DEE1113B51381FEC8\"}";

        public static String JINGUAN_1_6 = "{\"param\":{\"aCode\":\"734131476_121024102944\",\"itemList\":[\"ts-24135-9:6*2\"],\"promIds\":[10014467],\"type\":1},\"sign\":\"87E11BC9CC5683BBF8DC01EA2BA8E83F\"}";

        public static String JINGUAN_1_12 = "{\"param\":{\"aCode\":\"734131476_121024102944\",\"itemList\":[\"ts-24135-9:12*2\"],\"promIds\":[10014467],\"type\":1},\"sign\":\"F875793AC4477B20962C5296E08B676E\"}";

        public static String JINGUAN_2_1 = "{\"param\":{\"aCode\":\"734131476_121024102944\",\"itemList\":[\"ts-24135-13:1*2\"],\"promIds\":[10014471],\"type\":1},\"sign\":\"556B468D8E0564EDFD2ED8BE106B9E56\"}";

        public static String JINGUAN_2_3 = "{\"param\":{\"aCode\":\"734131476_121024102944\",\"itemList\":[\"ts-24135-13:3*2\"],\"promIds\":[10014471],\"type\":1},\"sign\":\"4E441EA8A968D965796FF9DFF856BE11\"}";

        public static String JINGUAN_2_6 = "{\"param\":{\"aCode\":\"734131476_121024102944\",\"itemList\":[\"ts-24135-13:6*2\"],\"promIds\":[10014471],\"type\":1},\"sign\":\"81C3FB16B9A27A3F75B191B961C13083\"}";

        public static String JINGUAN_2_12 = "{\"param\":{\"aCode\":\"734131476_121024102944\",\"itemList\":[\"ts-24135-13:12*2\"],\"promIds\":[10014471],\"type\":1},\"sign\":\"94B7506CD6AEA3380725436CC76D50F8\"}";

        // 运营一点通_数据化运营推广专家 - 全新升级_金冠版_ (ts-24135-14)
        public static String SJ_JINGUAN__1 = "{\"param\":{\"aCode\":\"ACT_734131476_130504140144\",\"itemList\":[\"ts-24135-14:1*2\"],\"promIds\":[10053262],\"type\":1},\"sign\":\"0C4B83ADC249EC68EB83160DC6925758\"}";

        public static String SJ_JINGUAN__3 = "{\"param\":{\"aCode\":\"ACT_734131476_130504140144\",\"itemList\":[\"ts-24135-14:3*2\"],\"promIds\":[10053262],\"type\":1},\"sign\":\"ECF2BEFFDD90674DBC1E5C3B7A9FD090\"}";

        public static String SJ_JINGUAN__6 = "{\"param\":{\"aCode\":\"ACT_734131476_130504140144\",\"itemList\":[\"ts-24135-14:6*2\"],\"promIds\":[10053262],\"type\":1},\"sign\":\"A26B162192710BE0C74584C0B3A91CFD\"}";

        public static String SJ_JINGUAN__12 = "{\"param\":{\"aCode\":\"ACT_734131476_130504140144\",\"itemList\":[\"ts-24135-14:12*2\"],\"promIds\":[10053262],\"type\":1},\"sign\":\"3F108C02C8E32514AD12788A8B5A7419\"}";

        // 运营一点通_数据化运营推广专家 - 全新升级_升金冠版_ (ts-24135-13)
        public static String SJ_SHENJINQUAN__1 = "{\"param\":{\"aCode\":\"ACT_734131476_130504140144\",\"itemList\":[\"ts-24135-13:1*2\"],\"promIds\":[10053263],\"type\":1},\"sign\":\"59B651DAF495EA66FBA00A76ACAE5819\"}";

        public static String SJ_SHENJINQUAN__3 = "{\"param\":{\"aCode\":\"ACT_734131476_130504140144\",\"itemList\":[\"ts-24135-13:3*2\"],\"promIds\":[10053263],\"type\":1},\"sign\":\"853DB758D1A24003C6507B89B278563B\"}";

        public static String SJ_SHENJINQUAN__6 = "{\"param\":{\"aCode\":\"ACT_734131476_130504140144\",\"itemList\":[\"ts-24135-13:6*2\"],\"promIds\":[10053263],\"type\":1},\"sign\":\"4AF38F492AB4BE3E2BB118BE46BFA5E0\"}";

        public static String SJ_SHENJINQUAN__12 = "{\"param\":{\"aCode\":\"ACT_734131476_130504140144\",\"itemList\":[\"ts-24135-13:12*2\"],\"promIds\":[10053263],\"type\":1},\"sign\":\"BF3C3E51750F0815B15E019616D916E6\"}";

        // 运营一点通_数据化运营推广专家 - 全新升级_升冠版_ (ts-24135-12)
        public static String SJ_SHENGUAN__1 = "{\"param\":{\"aCode\":\"ACT_734131476_130504140144\",\"itemList\":[\"ts-24135-12:1*2\"],\"promIds\":[10053264],\"type\":1},\"sign\":\"19793F8FA354E2342F286A008021222D\"}";

        public static String SJ_SHENGUAN__3 = "{\"param\":{\"aCode\":\"ACT_734131476_130504140144\",\"itemList\":[\"ts-24135-12:3*2\"],\"promIds\":[10053264],\"type\":1},\"sign\":\"21E504B391C7655B19317C06A0DC3550\"}";

        public static String SJ_SHENGUAN__6 = "{\"param\":{\"aCode\":\"ACT_734131476_130504140144\",\"itemList\":[\"ts-24135-12:6*2\"],\"promIds\":[10053264],\"type\":1},\"sign\":\"AB0E2B2786B46EE441F94A59C1E4C931\"}";

        public static String SJ_SHENGUAN__12 = "{\"param\":{\"aCode\":\"ACT_734131476_130504140144\",\"itemList\":[\"ts-24135-12:12*2\"],\"promIds\":[10053264],\"type\":1},\"sign\":\"9740B2094D2BDA54976B701159E885AE\"}";

        // 运营一点通_数据化运营推广专家 - 全新升级_升钻版_ (ts-24135-11)
        public static String SJ_SHENZUAN__1 = "{\"param\":{\"aCode\":\"ACT_734131476_130504140144\",\"itemList\":[\"ts-24135-11:1*2\"],\"promIds\":[10053265],\"type\":1},\"sign\":\"F64EE8B5ADD1D1F89E0952BCC96917B7\"}";

        public static String SJ_SHENZUAN__3 = "{\"param\":{\"aCode\":\"ACT_734131476_130504140144\",\"itemList\":[\"ts-24135-11:3*2\"],\"promIds\":[10053265],\"type\":1},\"sign\":\"D75FF7D1EDD57ADEAD2A66A2D6072B22\"}";

        public static String SJ_SHENZUAN__6 = "{\"param\":{\"aCode\":\"ACT_734131476_130504140144\",\"itemList\":[\"ts-24135-11:6*2\"],\"promIds\":[10053265],\"type\":1},\"sign\":\"CE04CC8C6547FE868915FF059BCA9FC2\"}";

        public static String SJ_SHENZUAN__12 = "{\"param\":{\"aCode\":\"ACT_734131476_130504140144\",\"itemList\":[\"ts-24135-11:12*2\"],\"promIds\":[10053265],\"type\":1},\"sign\":\"821F27DFDBA56C6BBC2F35A859D44DD2\"}";

        // 运营一点通_数据化运营推广专家 - 全新升级_金冠版 (ts-24135-10)
        public static String SJ_JINGUAN_1 = "{\"param\":{\"aCode\":\"ACT_734131476_130504140144\",\"itemList\":[\"ts-24135-10:1*2\"],\"promIds\":[10053266],\"type\":1},\"sign\":\"63437456A371F5A702ACC137B482C865\"}";

        public static String SJ_JINGUAN_3 = "{\"param\":{\"aCode\":\"ACT_734131476_130504140144\",\"itemList\":[\"ts-24135-10:3*2\"],\"promIds\":[10053266],\"type\":1},\"sign\":\"6C184F52399772D67B35B9DEDBA3C1F2\"}";

        public static String SJ_JINGUAN_6 = "{\"param\":{\"aCode\":\"ACT_734131476_130504140144\",\"itemList\":[\"ts-24135-10:6*2\"],\"promIds\":[10053266],\"type\":1},\"sign\":\"0C9E639DC8AEC247199963D528401284\"}";

        public static String SJ_JINGUAN_12 = "{\"param\":{\"aCode\":\"ACT_734131476_130504140144\",\"itemList\":[\"ts-24135-10:12*2\"],\"promIds\":[10053266],\"type\":1},\"sign\":\"AFADC706C420428FF5AA156898225E4B\"}";

        // 运营一点通_数据化运营推广专家 - 全新升级_升金冠版 (ts-24135-9)
        public static String SJ_SHENJINGUAN_1 = "{\"param\":{\"aCode\":\"ACT_734131476_130504140144\",\"itemList\":[\"ts-24135-9:1*2\"],\"promIds\":[10053267],\"type\":1},\"sign\":\"12F4430D2FEE3DCC24EDC155AC611DB8\"}";

        public static String SJ_SHENJINGUAN_3 = "{\"param\":{\"aCode\":\"ACT_734131476_130504140144\",\"itemList\":[\"ts-24135-9:3*2\"],\"promIds\":[10053267],\"type\":1},\"sign\":\"AA886CC191DEC929380755D6D222370C\"}";

        public static String SJ_SHENJINGUAN_6 = "{\"param\":{\"aCode\":\"ACT_734131476_130504140144\",\"itemList\":[\"ts-24135-9:6*2\"],\"promIds\":[10053267],\"type\":1},\"sign\":\"BC4086BED90303083B83AFF697D6D109\"}";

        public static String SJ_SHENJINGUAN_12 = "{\"param\":{\"aCode\":\"ACT_734131476_130504140144\",\"itemList\":[\"ts-24135-9:12*2\"],\"promIds\":[10053267],\"type\":1},\"sign\":\"5D74DA98DB562F623516594C6A04350A\"}";

        // 运营一点通_数据化运营推广专家 - 全新升级_升冠版 (ts-24135-8)
        public static String SJ_SHENGUAN_1 = "{\"param\":{\"aCode\":\"ACT_734131476_130504140144\",\"itemList\":[\"ts-24135-8:1*2\"],\"promIds\":[10053268],\"type\":1},\"sign\":\"6CD5FFD1C8EDC7F2515E77CF5C45F08D\"}";

        public static String SJ_SHENGUAN_3 = "{\"param\":{\"aCode\":\"ACT_734131476_130504140144\",\"itemList\":[\"ts-24135-8:3*2\"],\"promIds\":[10053268],\"type\":1},\"sign\":\"D246C52A72BAAE087365375005488B5B\"}";

        public static String SJ_SHENGUAN_6 = "{\"param\":{\"aCode\":\"ACT_734131476_130504140144\",\"itemList\":[\"ts-24135-8:6*2\"],\"promIds\":[10053268],\"type\":1},\"sign\":\"43C54A6AFF0CCD604BEB0C52E12F470B\"}";

        public static String SJ_SHENGUAN_12 = "{\"param\":{\"aCode\":\"ACT_734131476_130504140144\",\"itemList\":[\"ts-24135-8:12*2\"],\"promIds\":[10053268],\"type\":1},\"sign\":\"FB18066B3E7CFFA8F577E307AF5793EF\"}";

        // 运营一点通_数据化运营推广专家 - 巨无霸版_开春特惠 (ts-24135-15)
        public static String SJ_JU_1 = "{\"param\":{\"aCode\":\"ACT_734131476_130504140144\",\"itemList\":[\"ts-24135-15:1*2\"],\"promIds\":[10053269],\"type\":1},\"sign\":\"96336DD1A9F6789C9656E1BE7DABC5A2\"}";

        public static String SJ_JU_3 = "{\"param\":{\"aCode\":\"ACT_734131476_130504140144\",\"itemList\":[\"ts-24135-15:3*2\"],\"promIds\":[10053269],\"type\":1},\"sign\":\"59C5D023AC3B3BFD5BC4C8B6540011E2\"}";

        public static String SJ_JU_6 = "{\"param\":{\"aCode\":\"ACT_734131476_130504140144\",\"itemList\":[\"ts-24135-15:6*2\"],\"promIds\":[10053269],\"type\":1},\"sign\":\"1E6618CDC9E00F4A9B841626ABF3E5E7\"}";

        public static String SJ_JU_12 = "{\"param\":{\"aCode\":\"ACT_734131476_130504140144\",\"itemList\":[\"ts-24135-15:12*2\"],\"promIds\":[10053269],\"type\":1},\"sign\":\"AFD581B9B800F173FDD223B8425E689F\"}";

        public static final int SJ_JINGUAN__1_VERSION = 101;

        public static final int SJ_JINGUAN__3_VERSION = 102;

        public static final int SJ_JINGUAN__6_VERSION = 103;

        public static final int SJ_JINGUAN__12_VERSION = 104;

        public static final int SJ_SHENJINQUAN__1_VERSION = 105;

        public static final int SJ_SHENJINQUAN__3_VERSION = 106;

        public static final int SJ_SHENJINQUAN__6_VERSION = 107;

        public static final int SJ_SHENJINQUAN__12_VERSION = 108;

        public static final int SJ_SHENGUAN__1_VERSION = 109;

        public static final int SJ_SHENGUAN__3_VERSION = 110;

        public static final int SJ_SHENGUAN__6_VERSION = 111;

        public static final int SJ_SHENGUAN__12_VERSION = 112;

        public static final int SJ_SHENZUAN__1_VERSION = 113;

        public static final int SJ_SHENZUAN__3_VERSION = 114;

        public static final int SJ_SHENZUAN__6_VERSION = 115;

        public static final int SJ_SHENZUAN__12_VERSION = 116;

        public static final int SJ_JINGUAN_1_VERSION = 117;

        public static final int SJ_JINGUAN_3_VERSION = 118;

        public static final int SJ_JINGUAN_6_VERSION = 119;

        public static final int SJ_JINGUAN_12_VERSION = 120;

        public static final int SJ_SHENJINGUAN_1_VERSION = 121;

        public static final int SJ_SHENJINGUAN_3_VERSION = 122;

        public static final int SJ_SHENJINGUAN_6_VERSION = 123;

        public static final int SJ_SHENJINGUAN_12_VERSION = 124;

        public static final int SJ_SHENGUAN_1_VERSION = 125;

        public static final int SJ_SHENGUAN_3_VERSION = 126;

        public static final int SJ_SHENGUAN_6_VERSION = 127;

        public static final int SJ_SHENGUAN_12_VERSION = 128;

        public static final int SJ_JU_1_VERSION = 129;

        public static final int SJ_JU_3_VERSION = 130;

        public static final int SJ_JU_6_VERSION = 131;

        public static final int SJ_JU_12_VERSION = 132;

        public static final int ZUAN_1_1_VERSION = 1;

        public static final int ZUAN_1_3_VERSION = 2;

        public static final int ZUAN_1_6_VERSION = 3;

        public static final int ZUAN_1_12_VERSION = 4;

        public static final int BA_1_1_VERSION = 5;

        public static final int BA_1_3_VERSION = 6;

        public static final int BA_1_6_VERSION = 7;

        public static final int BA_1_12_VERSION = 8;

        public static final int GUAN_1_1_VERSION = 9;

        public static final int GUAN_1_3_VERSION = 10;

        public static final int GUAN_1_6_VERSION = 11;

        public static final int GUAN_1_12_VERSION = 12;

        public static final int GUAN_2_1_VERSION = 13;

        public static final int GUAN_2_3_VERSION = 14;

        public static final int GUAN_2_6_VERSION = 15;

        public static final int GUAN_2_12_VERSION = 16;

        public static final int JINGUAN_1_1_VERSION = 17;

        public static final int JINGUAN_1_3_VERSION = 18;

        public static final int JINGUAN_1_6_VERSION = 19;

        public static final int JINGUAN_1_12_VERSION = 20;

        public static final int JINGUAN_2_1_VERSION = 21;

        public static final int JINGUAN_2_3_VERSION = 22;

        public static final int JINGUAN_2_6_VERSION = 23;

        public static final int JINGUAN_2_12_VERSION = 24;

    }

    public static String genPolicyUrl(String nick, int version) {

        String para = "";

        switch (version) {
            case POLICY_PARAM_STR.ZUAN_1_1_VERSION:
                para = POLICY_PARAM_STR.ZUAN_1_1;
                break;
            case POLICY_PARAM_STR.ZUAN_1_3_VERSION:
                para = POLICY_PARAM_STR.ZUAN_1_3;
                break;
            case POLICY_PARAM_STR.ZUAN_1_6_VERSION:
                para = POLICY_PARAM_STR.ZUAN_1_6;
                break;
            case POLICY_PARAM_STR.ZUAN_1_12_VERSION:
                para = POLICY_PARAM_STR.ZUAN_1_12;
                break;

            case POLICY_PARAM_STR.BA_1_1_VERSION:
                para = POLICY_PARAM_STR.BA_1_1;
                break;
            case POLICY_PARAM_STR.BA_1_3_VERSION:
                para = POLICY_PARAM_STR.BA_1_3;
                break;
            case POLICY_PARAM_STR.BA_1_6_VERSION:
                para = POLICY_PARAM_STR.BA_1_6;
                break;
            case POLICY_PARAM_STR.BA_1_12_VERSION:
                para = POLICY_PARAM_STR.BA_1_12;
                break;

            case POLICY_PARAM_STR.GUAN_1_1_VERSION:
                para = POLICY_PARAM_STR.GUAN_1_1;
                break;
            case POLICY_PARAM_STR.GUAN_1_3_VERSION:
                para = POLICY_PARAM_STR.GUAN_1_3;
                break;
            case POLICY_PARAM_STR.GUAN_1_6_VERSION:
                para = POLICY_PARAM_STR.GUAN_1_6;
                break;
            case POLICY_PARAM_STR.GUAN_1_12_VERSION:
                para = POLICY_PARAM_STR.GUAN_1_12;
                break;

            case POLICY_PARAM_STR.GUAN_2_1_VERSION:
                para = POLICY_PARAM_STR.GUAN_2_1;
                break;
            case POLICY_PARAM_STR.GUAN_2_3_VERSION:
                para = POLICY_PARAM_STR.GUAN_2_3;
                break;
            case POLICY_PARAM_STR.GUAN_2_6_VERSION:
                para = POLICY_PARAM_STR.GUAN_2_6;
                break;
            case POLICY_PARAM_STR.GUAN_2_12_VERSION:
                para = POLICY_PARAM_STR.GUAN_2_12;
                break;

            case POLICY_PARAM_STR.JINGUAN_1_1_VERSION:
                para = POLICY_PARAM_STR.JINGUAN_1_1;
                break;
            case POLICY_PARAM_STR.JINGUAN_1_3_VERSION:
                para = POLICY_PARAM_STR.JINGUAN_1_3;
                break;
            case POLICY_PARAM_STR.JINGUAN_1_6_VERSION:
                para = POLICY_PARAM_STR.JINGUAN_1_6;
                break;
            case POLICY_PARAM_STR.JINGUAN_1_12_VERSION:
                para = POLICY_PARAM_STR.JINGUAN_1_12;
                break;

            case POLICY_PARAM_STR.JINGUAN_2_1_VERSION:
                para = POLICY_PARAM_STR.JINGUAN_2_1;
                break;
            case POLICY_PARAM_STR.JINGUAN_2_3_VERSION:
                para = POLICY_PARAM_STR.JINGUAN_2_3;
                break;
            case POLICY_PARAM_STR.JINGUAN_2_6_VERSION:
                para = POLICY_PARAM_STR.JINGUAN_2_6;
                break;
            case POLICY_PARAM_STR.JINGUAN_2_12_VERSION:
                para = POLICY_PARAM_STR.JINGUAN_2_12;
                break;

            // 升级——金冠
            case POLICY_PARAM_STR.SJ_JINGUAN__1_VERSION:
                para = POLICY_PARAM_STR.SJ_JINGUAN__1;
                break;
            case POLICY_PARAM_STR.SJ_JINGUAN__3_VERSION:
                para = POLICY_PARAM_STR.SJ_JINGUAN__3;
                break;
            case POLICY_PARAM_STR.SJ_JINGUAN__6_VERSION:
                para = POLICY_PARAM_STR.SJ_JINGUAN__6;
                break;
            case POLICY_PARAM_STR.SJ_JINGUAN__12_VERSION:
                para = POLICY_PARAM_STR.SJ_JINGUAN__12;
                break;
            // 升级——升金冠
            case POLICY_PARAM_STR.SJ_SHENJINQUAN__1_VERSION:
                para = POLICY_PARAM_STR.SJ_SHENJINQUAN__1;
                break;
            case POLICY_PARAM_STR.SJ_SHENJINQUAN__3_VERSION:
                para = POLICY_PARAM_STR.SJ_SHENJINQUAN__3;
                break;
            case POLICY_PARAM_STR.SJ_SHENJINQUAN__6_VERSION:
                para = POLICY_PARAM_STR.SJ_SHENJINQUAN__6;
                break;
            case POLICY_PARAM_STR.SJ_SHENJINQUAN__12_VERSION:
                para = POLICY_PARAM_STR.SJ_SHENJINQUAN__12;
                break;

            // 升级——升冠
            case POLICY_PARAM_STR.SJ_SHENGUAN__1_VERSION:
                para = POLICY_PARAM_STR.SJ_SHENGUAN__1;
                break;
            case POLICY_PARAM_STR.SJ_SHENGUAN__3_VERSION:
                para = POLICY_PARAM_STR.SJ_SHENGUAN__3;
                break;
            case POLICY_PARAM_STR.SJ_SHENGUAN__6_VERSION:
                para = POLICY_PARAM_STR.SJ_SHENGUAN__6;
                break;
            case POLICY_PARAM_STR.SJ_SHENGUAN__12_VERSION:
                para = POLICY_PARAM_STR.SJ_SHENGUAN__12;
                break;

            // 升级——升钻
            case POLICY_PARAM_STR.SJ_SHENZUAN__1_VERSION:
                para = POLICY_PARAM_STR.SJ_SHENZUAN__1;
                break;
            case POLICY_PARAM_STR.SJ_SHENZUAN__3_VERSION:
                para = POLICY_PARAM_STR.SJ_SHENZUAN__3;
                break;
            case POLICY_PARAM_STR.SJ_SHENZUAN__6_VERSION:
                para = POLICY_PARAM_STR.SJ_SHENZUAN__6;
                break;
            case POLICY_PARAM_STR.SJ_SHENZUAN__12_VERSION:
                para = POLICY_PARAM_STR.SJ_SHENZUAN__12;
                break;

            // 升级——金冠
            case POLICY_PARAM_STR.SJ_JINGUAN_1_VERSION:
                para = POLICY_PARAM_STR.SJ_JINGUAN_1;
                break;
            case POLICY_PARAM_STR.SJ_JINGUAN_3_VERSION:
                para = POLICY_PARAM_STR.SJ_JINGUAN_3;
                break;
            case POLICY_PARAM_STR.SJ_JINGUAN_6_VERSION:
                para = POLICY_PARAM_STR.SJ_JINGUAN_6;
                break;
            case POLICY_PARAM_STR.SJ_JINGUAN_12_VERSION:
                para = POLICY_PARAM_STR.SJ_JINGUAN_12;
                break;

            // 升级——升金冠
            case POLICY_PARAM_STR.SJ_SHENJINGUAN_1_VERSION:
                para = POLICY_PARAM_STR.SJ_SHENJINGUAN_1;
                break;
            case POLICY_PARAM_STR.SJ_SHENJINGUAN_3_VERSION:
                para = POLICY_PARAM_STR.SJ_SHENJINGUAN_3;
                break;
            case POLICY_PARAM_STR.SJ_SHENJINGUAN_6_VERSION:
                para = POLICY_PARAM_STR.SJ_SHENJINGUAN_6;
                break;
            case POLICY_PARAM_STR.SJ_SHENJINGUAN_12_VERSION:
                para = POLICY_PARAM_STR.SJ_SHENJINGUAN_12;
                break;

            // 升级——升冠
            case POLICY_PARAM_STR.SJ_SHENGUAN_1_VERSION:
                para = POLICY_PARAM_STR.SJ_SHENGUAN_1;
                break;
            case POLICY_PARAM_STR.SJ_SHENGUAN_3_VERSION:
                para = POLICY_PARAM_STR.SJ_SHENGUAN_3;
                break;
            case POLICY_PARAM_STR.SJ_SHENGUAN_6_VERSION:
                para = POLICY_PARAM_STR.SJ_SHENGUAN_6;
                break;
            case POLICY_PARAM_STR.SJ_SHENGUAN_12_VERSION:
                para = POLICY_PARAM_STR.SJ_SHENGUAN_12;
                break;

            // 升级——巨无霸
            case POLICY_PARAM_STR.SJ_JU_1_VERSION:
                para = POLICY_PARAM_STR.SJ_JU_1;
                break;
            case POLICY_PARAM_STR.SJ_JU_3_VERSION:
                para = POLICY_PARAM_STR.SJ_JU_3;
                break;
            case POLICY_PARAM_STR.SJ_JU_6_VERSION:
                para = POLICY_PARAM_STR.SJ_JU_6;
                break;
            case POLICY_PARAM_STR.SJ_JU_12_VERSION:
                para = POLICY_PARAM_STR.SJ_JU_12;
                break;
        }

        FuwuApis.SaleLinkGenApi saleLinkGenApi = new FuwuApis.SaleLinkGenApi(POLICY_APP_KEY, POLICY_APP_SECRET, nick,
                para);
        String url = saleLinkGenApi.call();
        if (url == null) {
            return saleLinkGenApi.getErrorMsg();
        } else {
            return url;
        }
    }

    public static String genUrl(String nick, int type, int version) {

        log.warn("Gen url for nick:" + nick + ",type:" + type + ",version:" + version);
        nick = nick.trim();
        String url = "";
        if (type == 1) {
            url = SaleLinkGenAction.genPolicyUrl(nick, version);
        } else if (type == 2) {
        }

        log.error("url:" + url);
        return url;
    }

}
