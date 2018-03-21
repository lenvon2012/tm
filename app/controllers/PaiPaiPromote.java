
package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.paipai.PaiPaiUser;
import models.popularized.Popularized;
import models.popularized.Popularized.PopularizedStatus;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ppapi.models.PaiPaiItem;
import result.TMResult;
import actions.popularized.PaiPaiPopularizedAction;
import actions.popularized.PopularizedAction;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.JsonUtil;

import configs.Subscribe.Version;
import dao.paipai.PaiPaiItemDao;
import dao.popularized.PopularizedDao;
import dao.popularized.PopularizedDao.PopularizedStatusSqlUtil;

/**
 * 拍拍下面的 流量应用都放在这里
 */
public class PaiPaiPromote extends PaiPaiController {

    
    private static final Logger log = LoggerFactory.getLogger(PaiPaiPromote.class);

    
    public static void index() {

    }

    /**
     * 帮推广
     */

    public static void btuiguang() {
        render("paipaipromote/index.html");
    }

    public static void allItems() {
        render("/paipaipromote/allItems.html");
    }

    public static void showedItems() {
        render("/paipaipromote/showedItems.html");
    }

    public static void award() {
        render("/paipaipromote/award.html");
    }

    public static void help() {
        render("/paipaipromote/help.html");
    }

    public static void tryhelp() {
        render("/paipaipromote/tryhelp.html");
    }

    public static void recommend() {
        render("/paipaipromote/forliuliang.html");
    }

    public static void upgrade() {
        render("/paipaipromote/upgrade.html");
    }

    /**
     * 腾讯推广
     */
    public static void qqpromote() {

    }

    /**
     * 分享应用
     */
    public static void share() {

    }

    public static void setPopularOn() {
        PaiPaiUser user = getUser();
        user.setPopularOff(false);
        boolean success = user.jdbcSave();
        renderJSON(new TMResult(success));
    }

    public static void setPopularOff() {
        PaiPaiUser user = getUser();
        user.setPopularOff(true);
        boolean success = user.jdbcSave();
        renderJSON(new TMResult(success));
    }

    public static void addPopularized(String numIids, int status) {
        PaiPaiUser user = getUser();
        
        
        status = PopularizedStatusSqlUtil.checkStatus(status);

        if (StringUtils.isEmpty(numIids)) {
            ok();
        }
        String[] idStrings = StringUtils.split(numIids, ",");
        if (ArrayUtils.isEmpty(idStrings)) {
            ok();
        }
        
        String itemCodes = numIids;

        List<PaiPaiItem> paipaiItemList = PaiPaiItemDao.findByItemCodes(user.getId(), itemCodes);
        if (CommonUtils.isEmpty(paipaiItemList)) {
            TMResult.renderMsg(StringUtils.EMPTY);
        }

        PaiPaiPopularizedAction.addPaiPaiPopularized(user, paipaiItemList, status);

        TMResult.renderMsg(StringUtils.EMPTY);
        
    }

    public static void removePopularized(String numIids, int status) {
        PaiPaiUser user = getUser();
        
        if (StringUtils.isEmpty(numIids)) {
            ok();
        }

        status = PopularizedStatusSqlUtil.checkStatus(status);

        String[] idStrings = StringUtils.split(numIids, ",");
        if (ArrayUtils.isEmpty(idStrings)) {
            ok();
        }

        String itemCodes = numIids;
        
        List<PaiPaiItem> paipaiItemList = PaiPaiItemDao.findByItemCodes(user.getId(), itemCodes);
        if (CommonUtils.isEmpty(paipaiItemList)) {
            TMResult.renderMsg(StringUtils.EMPTY);
        }

        
        for (PaiPaiItem paipaiItem : paipaiItemList) {
            if (paipaiItem == null) {
                continue;
            }
            Long numIid = paipaiItem.initLongNumIid();
            Popularized popularized = PopularizedDao.findByNumIid(user.getId(), numIid);
            
            if (popularized != null) {
                PopularizedAction.removePopularized(user.getId(), popularized, status);
            }
        }
        

        TMResult.renderMsg(StringUtils.EMPTY);
    }

    public static void addPopularizedAll(int status) {
        PaiPaiUser user = getUser();
        
        status = PopularizedStatusSqlUtil.checkStatus(status);

        int remain = PaiPaiUserInfo.remainNumWithPopularAward(user, status);

        if (remain == 0) {
            renderJSON(new TMResult(false));
        } else {

            //找到未推广的宝贝
            List<PaiPaiItem> paipaiItemList = PaiPaiItemDao.searchPop(user.getId(), 0, remain, "", 3, 1, 0L, 1);
            if (CommonUtils.isEmpty(paipaiItemList)) {
                renderJSON(new TMResult(false));
            }

            PaiPaiPopularizedAction.addPaiPaiPopularized(user, paipaiItemList, status);

            renderJSON(new TMResult(true));
        }

    }

    public static void removePopularizedAll(int status) {
        PaiPaiUser user = getUser();
        
        status = PopularizedStatusSqlUtil.checkStatus(status);
        
        PopularizedAction.removeAllPopularized(user.getId(), status);

        TMResult.renderMsg(StringUtils.EMPTY);
    }

    public static void PoluparizedOrNot(int status) {
        
        PaiPaiUser user = getUser();
        
        status = PopularizedStatusSqlUtil.checkStatus(status);
        List<Popularized> popularized = new ArrayList<Popularized>();
        popularized = PopularizedDao.queryPopularizedsByUserIdAndStatus(user.getId(), status);
        renderJSON(JsonUtil.getJson(popularized));
        
    }

    public static void searchItems(String s, int pn, int ps, int sort, int polularized, Long catId, int popularizeStatus) {
    
        PaiPaiUser user = getUser();
        
        popularizeStatus = PopularizedStatusSqlUtil.checkStatus(popularizeStatus);

        PageOffset po = new PageOffset(pn, ps);
        
        Set<Long> pops = PopularizedDao.findNumIidsByUserIdWithStatus(user.getId(), popularizeStatus);
        
        List<PaiPaiItem> paipaiItemList = PaiPaiItemDao.searchPop(user.getId(), po.getOffset(), po.getPs(), s, sort, polularized,
                catId, popularizeStatus);
        if (CommonUtils.isEmpty(paipaiItemList)) {
            paipaiItemList = new ArrayList<PaiPaiItem>();
        }
        
        for (PaiPaiItem paipaiItem : paipaiItemList) {
            if (paipaiItem == null)
                continue;
            Long numIid = paipaiItem.initLongNumIid();
            if (pops.contains(numIid)) {
                paipaiItem.setPopularized();

            }

        }
        
        long count = PaiPaiItemDao.countPop(user.getId(), s, sort, catId, polularized, popularizeStatus);
    
        TMResult tmRes = new TMResult(paipaiItemList, (int) count, po);
        
        renderJSON(JsonUtil.getJson(tmRes));
        
    }

    public static void getUserInfo() {
        PaiPaiUser user = getUser();
        
        int level = user.getVersion();
        renderJSON(JsonUtil.getJson(PaiPaiUserInfo.getPaiPaiUserInfo(level, user.getId(), user.getNick())));
        
    }
    
    
    
    
    
    
    @JsonAutoDetect
    public static class PaiPaiUserInfo {

        @JsonProperty
        String username = StringUtils.EMPTY;

        @JsonProperty
        int level = 0;

        @JsonProperty
        int totalNum = 0;

        @JsonProperty
        int popularizedNum = 0;

        @JsonProperty
        int remainNum = 0;

        @JsonProperty
        boolean award = false;

        @JsonProperty
        boolean isPopularOn = true;

        @JsonProperty
        int hotTotalNum = 0;//热卖推荐总数

        @JsonProperty
        int hotUsedNum = 0;//已热卖推荐数

        @JsonProperty
        int hotRemainNum = 0;//热卖推荐剩余

        public PaiPaiUserInfo() {
            super();
        }

        public String getVersion() {
            Map<Integer, String> versionNameMap = APIConfig.get().getVersionNameMap();
            if (level <= Version.BLACK) {
                return versionNameMap.get(Version.BLACK);
            } else if (level <= Version.FREE) {
                return versionNameMap.get(Version.FREE);
            } else if (level <= Version.BASE) {
                return versionNameMap.get(Version.BASE);
            } else if (level <= Version.VIP) {
                return versionNameMap.get(Version.VIP);
            } else if (level <= Version.SUPER) {
                return versionNameMap.get(Version.SUPER);
            } else if (level <= Version.HALL) {
                return versionNameMap.get(Version.HALL);
            } else if (level <= Version.GOD) {
                return versionNameMap.get(Version.GOD);
            } else if (level <= Version.SUN) {
                return versionNameMap.get(Version.SUN);
            } else if (level <= Version.DAWEI) {
                return versionNameMap.get(Version.DAWEI);
            } else {
                return versionNameMap.get(Version.CUOCUO);
            }
        }

        public static PaiPaiUserInfo getPaiPaiUserInfo(int level, Long userId, String username) {
            PaiPaiUser user = PaiPaiUser.findByUserId(userId);
            boolean award = false;
            if (user.isPopularAward()) {
                award = true;
            }
            boolean isPopularOn = true;
            if (user.isPopularOff()) {
                isPopularOn = false;
            }
            

            int totalNum = getTotal(level, PopularizedStatus.Normal);
            int usedNum = getPopularizedNum(userId, PopularizedStatus.Normal + PopularizedStatus.Try);

            int hotTotalNum = getTotal(level, PopularizedStatus.HotSale);
            int hotUsedNum = getPopularizedNum(userId, PopularizedStatus.HotSale);

            return new PaiPaiUserInfo(level, totalNum, usedNum, totalNum - usedNum, username, award, isPopularOn,
                    hotTotalNum, hotUsedNum, hotTotalNum - hotUsedNum);
        }

        public static int remainNumWithPopularAward(PaiPaiUser user, int status) {
            status = PopularizedStatusSqlUtil.checkStatus(status);

            int total = getTotal(user.getVersion(), status);
            //好评送推广
            if (user.isPopularAward() && status == PopularizedStatus.Normal) {
                total++;
            } else if (user.isPopularAward() && status == PopularizedStatus.Try) {
                total++;
            }

            if (status == PopularizedStatus.Normal || status == PopularizedStatus.Try) {
                status = PopularizedStatus.Normal + PopularizedStatus.Try;
            }
            int count = getPopularizedNum(user.getId(), status);
            return (total - count) > 0 ? (total - count) : 0;
        }

        private static int getPopularizedNum(Long userId, int status) {
            int count = (int) PopularizedDao.countPopularizedByUserIdAndStatus(userId, status);

            return count;
        }

        private static int getTotal(int level, int status) {
            if (status <= PopularizedStatus.Normal || status == PopularizedStatus.Try) {
                Map<Integer, Integer> verCountMap = APIConfig.get().getTuiguangCountMap();
                if (level <= Version.BLACK) {
                    return verCountMap.get(Version.BLACK);
                } else if (level <= Version.FREE) {
                    return verCountMap.get(Version.FREE);
                } else if (level <= Version.BASE) {
                    return verCountMap.get(Version.BASE);
                } else if (level <= Version.VIP) {
                    return verCountMap.get(Version.VIP);
                } else if (level <= Version.SUPER) {
                    return verCountMap.get(Version.SUPER);
                } else if (level <= Version.HALL) {
                    return verCountMap.get(Version.HALL);
                } else if (level <= Version.GOD) {
                    return verCountMap.get(Version.GOD);
                } else if (level <= Version.SUN) {
                    return verCountMap.get(Version.SUN);
                } else if (level <= Version.DAWEI) {
                    return verCountMap.get(Version.DAWEI);
                } else {
                    return verCountMap.get(Version.CUOCUO);
                }
            } else if (status == PopularizedStatus.HotSale) {
                Map<Integer, Integer> hotMap = APIConfig.get().getHotCountMap();
                Integer count = 0;
                if (level <= Version.BLACK) {
                    count = hotMap.get(Version.BLACK);
                } else if (level <= Version.FREE) {
                    count = hotMap.get(Version.FREE);
                } else if (level <= Version.BASE) {
                    count = hotMap.get(Version.BASE);
                } else if (level <= Version.VIP) {
                    count = hotMap.get(Version.VIP);
                } else if (level <= Version.SUPER) {
                    count = hotMap.get(Version.SUPER);
                } else if (level <= Version.HALL) {
                    count = hotMap.get(Version.HALL);
                } else if (level <= Version.GOD) {
                    count = hotMap.get(Version.GOD);
                } else if (level <= Version.SUN) {
                    count = hotMap.get(Version.SUN);
                } else if (level <= Version.DAWEI) {
                    count = hotMap.get(Version.DAWEI);
                } else {
                    count = hotMap.get(Version.CUOCUO);
                }
                if (count == null) {
                    return 0;
                } else {
                    return count;
                }
            } else {
                return 0;
            }

        }

        public PaiPaiUserInfo(int level, int totalNum, int popularizedNum, int remainNum, String username,
                boolean award, boolean isPopularOn, int hotTotalNum, int hotUsedNum, int hotRemainNum) {
            this.level = level;
            this.totalNum = totalNum;
            this.popularizedNum = popularizedNum;
            this.remainNum = remainNum;
            this.username = username;
            this.award = award;
            this.isPopularOn = isPopularOn;

            this.hotTotalNum = hotTotalNum;
            this.hotUsedNum = hotUsedNum;
            this.hotRemainNum = hotRemainNum;
        }

    }
    
    
}
