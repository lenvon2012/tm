
package models.campaign;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import play.db.jpa.GenericModel;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.PolicySQLGenerator;
import codegen.TaobaoObjWrapper;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.NumberUtil;
import com.taobao.api.domain.Campaign;
import com.taobao.api.domain.CampaignBudget;

import dao.campaign.CampaignDao;
import dao.industry.AreaOptionDao;

/**
 * 推广计划
 * 
 * @author LY
 * 
 */

@Entity(name = CampaignPlay.TABLE_NAME)
@JsonIgnoreProperties(value = {
        "tableHashKey", "persistent", "entityId", "active", "tableName", "idColumn"
})
public class CampaignPlay extends GenericModel implements PolicySQLGenerator, TaobaoObjWrapper<Campaign> {

    private static final Logger log = LoggerFactory.getLogger(CampaignPlay.class);

    public static final String TAG = "CampaignPlay";

    public static final String TABLE_NAME = "campaign";

    public static CampaignPlay EMPTY = new CampaignPlay();

    public CampaignPlay() {
    };

    /**
     * 推广计划ID
     */
    @Id
    @PolicySQLGenerator.CodeNoUpdate
    // @JsonProperty(value = "id")
    public Long campaignId;

    /**
     * 对应的用户
     */
    @PolicySQLGenerator.CodeNoUpdate
    @JsonIgnore
    public Long userId;

    /**
     * 多账号时切换
     */
    @Index(name = "nick")
    // @JsonIgnore
    @Column(columnDefinition = "varchar(63) default '' NOT NULL")
    public String nick;

    /**
     * 推广计划名称，不能多余20个汉字
     */
    @Column(columnDefinition = "varchar(50) DEFAULT null")
    public String title;

    public double budget = NumberUtil.DEFAULT_DOUBLE;

    public static class SettleStatus {
        public static final int OFF_LINE = 1;

        public static final int ON_LINE = 2;

        public static final String ON_STRING = "online";

        public static final String OFF_STRING = "offline";

        public static String getSettleStatus(int settleStatus) {
            return settleStatus == 1 ? OFF_STRING : ON_STRING;
        }
    }

    /**
     * 在我们应用中推广计划的状态
     */
    public int campaignStatus = 0;

    public static class CampaignStatus {
        public static final int Has_Exact_Throw = 1;//已经精准化投放过了
    }

    @Column(columnDefinition = "int default 0")
    private int campaignType = 0;

    public static class CampaignType {
        public static final int Low_Price = 1;//低价引流

        public static final int Factory_Rebirth = 2;//爆款重生

        public static final int Human_Computer = 4;//人机结合
    }

    private boolean smooth = false;

    /**
     * 推广计划结算状态，1 : offline-下线；2 : online-上线，
     */
    // @JsonIgnore
    public int settleStatus = 0;

    public static class OnlineStatus {
        public static final int OFF_LINE = 1;

        public static final int ON_LINE = 2;

        public static final int DELETE = 4;

        // add in bus
        public static final int ADD_IN_BUS = 8;

        public static final int ADD_IN_BUS_SETTED = 16;

        public static final int IS_SMOOTH = 32;

        public static final String ON_STRING = "online";

        public static final String OFF_STRING = "offline";

        public static String getOnlineStatus(int onlineStatus) {

            if ((onlineStatus & OFF_LINE) > 0) {
                return "OFF";
            } else if ((onlineStatus & ON_LINE) > 0) {
                return "ON";
            } else {
                return "OFF";
            }
        }
    }

    /**
     * 用户设置的上下限状态；1 : offline-下线；2 :　online-上线；3 : delete-被用户暂时删除
     */
    // @JsonIgnore
    public int onlineStatus = 0;

    public static class SettleReason {
        public static final int NORMAL = 0;

        public static final int BALANCE_NOT_ENOUGH = 1;

        public static final int OVER_BUDGET = 2;
    }

    /**
     * 推广计划结算下线原因，1-余额不足；2-超过日限额，以分号分隔多个下线原因 1 余额不足 3 超过日限额
     */
    @JsonIgnore
    public int settleReason;

    /**
     * 创建时间
     */
    @JsonIgnore
    public long createTime;

    /**
     * 最后修改时间
     */
    @JsonIgnore
    public long modifiedTime;

    public static class MatchType {

        public static int Exact_Match = 1;

        public static int Center_Word_Match = 2;

        public static int Broad_Match = 4;

        public static String getStr(int type) {
            if (type == Exact_Match) {
                return "精准匹配";
            } else if (type == Center_Word_Match) {
                return "中心词匹配";
            } else if (type == Broad_Match) {
                return "广泛匹配";
            }
            return "精准匹配";
        }
    }

    public static class PlatForm {

        public static int Taobao_Zhan = 1;

        public static int Taobao_Union = 2;

        public static int ETao = 4;

        public static List<String> getStrList(int platForm) {

            List<String> strList = new ArrayList<String>();
            if ((platForm & Taobao_Zhan) > 0) {
                strList.add("淘宝站内");
            }
            if ((platForm & Taobao_Union) > 0) {
                strList.add("淘宝联盟");
            }
            if ((platForm & ETao) > 0) {
                strList.add("一淘");
            }
            return strList;
        }

        public static String getSearchChannel(int platForm) {
            String str = "";

            if ((platForm & Taobao_Zhan) > 0) {
                str += "1";
            }
            if ((platForm & Taobao_Union) > 0) {
                str += ",2";
            }
            if ((platForm & ETao) > 0) {
                str += ",4";
            }
            // default taobao_zhan
            return str == "" ? "1" : str;
        }

        public static String getNonSearchChannel(int platForm) {
            String str = "";

            if ((platForm & Taobao_Zhan) > 0) {
                str += "1";
            }
            if ((platForm & Taobao_Union) > 0) {
                str += ",2";
            }

            // default taobao_zhan
            return str == "" ? "1" : str;
        }

        public static boolean hasNonSearchChannel(int platForm) {
            if ((platForm & Taobao_Zhan) > 0) {
                return true;
            }
            if ((platForm & Taobao_Union) > 0) {
                return true;
            }
            return false;
        }
    }

    public int keywordMatchType = MatchType.Exact_Match;

    public int platform = PlatForm.Taobao_Zhan;

    //地域
    @Column(columnDefinition = "varchar(1000) default '' ")
    private String areaIds;

    //人群
    @Column(columnDefinition = "varchar(255) default '' ")
    private String demographicIds;

    //人群加价，逗号隔开，与人群一一对应
    @Column(columnDefinition = "varchar(255) default '' ")
    private String addPrices;

    public static class CampaignAreaUtil {
        public static final String AllArea = "all";

        public static final String AllAreaStr = "全部";

        /**
         * 在计划管理页面，显示部分地域
         * @param areaIds
         * @return
         */
        public static String showPartAreaNames(String areaIds) {
            if (StringUtils.isEmpty(areaIds))
                return AllAreaStr;
            if (AllArea.equals(areaIds))
                return AllAreaStr;
            String[] areaIdArray = areaIds.split(",");
            int showLength = 3;
            String areaNames = "";
            for (int i = 0; i < areaIdArray.length && i < showLength; i++) {
                long areaId = 0;
                try {
                    areaId = Long.parseLong(areaIdArray[i]);
                    String areaName = AreaOptionDao.queryAreaNameById(areaId);
                    if (StringUtils.isEmpty(areaName))
                        continue;
                    if (!StringUtils.isEmpty(areaNames))
                        areaNames += ",";
                    areaNames += areaName;
                } catch (Exception ex) {
                    continue;
                }
            }
            if (areaIdArray.length > showLength && !StringUtils.isEmpty(areaNames))
                areaNames += "等";

            return areaNames;
        }

    }

    public static class DemographicUtil {
        private static final String AllDemographicStr = "全部";

        private static final String NoDemographicStr = "尚未加价";

        private static final Map<Long, String> demographicMap = new ConcurrentHashMap<Long, String>();
        static {
            demographicMap.put(11L, "男");
            demographicMap.put(12L, "女");
            demographicMap.put(21L, "低收入人群");
            demographicMap.put(22L, "中收入人群");
            demographicMap.put(23L, "高收入人群");
        }

        public static String showPartDemographicNames(String demographicIds) {
            if (StringUtils.isEmpty(demographicIds))
                return NoDemographicStr;
            String[] demographicIdArray = demographicIds.split(",");
            List<String> demographicIdList = new ArrayList<String>();
            for (int i = 0; i < demographicIdArray.length; i++) {
                demographicIdList.add(demographicIdArray[i]);
            }
            //男和女
            if (demographicIdList.contains("11") && demographicIdList.contains("12")) {
                demographicIdList.remove("11");
                demographicIdList.remove("12");
                demographicIdList.remove("13");
            }
            if (demographicIdList.contains("21") && demographicIdList.contains("22")
                    && demographicIdList.contains("23")) {
                demographicIdList.remove("21");
                demographicIdList.remove("22");
                demographicIdList.remove("23");
                demographicIdList.remove("24");
            }

            int showLength = 3;
            String demographicNames = "";
            for (int i = 0; i < demographicIdList.size() && i < showLength; i++) {
                long demographicId = 0;
                try {
                    demographicId = Long.parseLong(demographicIdList.get(i));
                    String demographicName = demographicMap.get(demographicId);
                    if (StringUtils.isEmpty(demographicName))
                        continue;
                    if (!StringUtils.isEmpty(demographicNames))
                        demographicNames += ",";
                    demographicNames += demographicName;
                } catch (Exception ex) {
                    continue;
                }
            }
            if (StringUtils.isEmpty(demographicNames)) {
                return AllDemographicStr;
            } else if (demographicIdList.size() > showLength)
                demographicNames += "等";
            return demographicNames;
        }
    }

    public boolean isSmooth() {
        return smooth;
    }

    public void setSmooth(boolean smooth) {
        this.smooth = smooth;
    }

    public int getCampaignType() {
        return campaignType;
    }

    public void setCampaignType(int campaignType) {
        this.campaignType = campaignType;
    }

    public String getAreaIds() {
        return areaIds;
    }

    public void setAreaIds(String areaIds) {
        this.areaIds = areaIds;
    }

    public boolean hasExactThrowStatus() {
        return ((campaignStatus & CampaignStatus.Has_Exact_Throw) > 0);
    }

    public void setExactThrowStatus() {
        campaignStatus |= CampaignStatus.Has_Exact_Throw;
    }

    public void clearExactThrowStatus() {
        campaignStatus &= (~CampaignStatus.Has_Exact_Throw);
    }

    public int getCampaignStatus() {
        return campaignStatus;
    }

    public void setCampaignStatus(int campaignStatus) {
        this.campaignStatus = campaignStatus;
    }

    public String getDemographicIds() {
        return demographicIds;
    }

    public void setDemographicIds(String demographicIds) {
        this.demographicIds = demographicIds;
    }

    public boolean isLowPriceCampaign() {
        if (this.campaignType == CampaignType.Low_Price) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 包括爆款重生和人机结合
     * @return
     */
    public boolean isRebirthCampaign() {
        if (this.campaignType == CampaignType.Factory_Rebirth ||
                this.campaignType == CampaignType.Human_Computer) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean isChedaoCampaign() {
        if (isLowPriceCampaign() == true || isRebirthCampaign() == true) {
            return true;
        } else {
            return false;
        }
    }
    

    public String getAddPrices() {
        return addPrices;
    }

    public void setAddPrices(String addPrices) {
        this.addPrices = addPrices;
    }

    public CampaignPlay(Long userId, String title) {
        this.userId = userId;
        this.title = title;
    }

    public CampaignPlay(Long campaignId, Long userId, String nick, String title, int settleStatus, int onlineStatus,
            int settleReason, Long createTime, Long modifiedTime, int budget, int keywordMatchType, int platForm) {
        this.campaignId = campaignId;
        this.userId = userId;
        this.nick = nick;
        this.title = title;
        this.settleStatus = settleStatus;
        this.onlineStatus = onlineStatus;
        this.settleReason = settleReason;
        this.createTime = createTime;
        this.modifiedTime = modifiedTime;
        this.budget = budget;
        this.keywordMatchType = keywordMatchType;
        this.platform = platForm;

    }

    public CampaignPlay(Long campaignId, Long userId, String nick, String title, int settleStatus, int onlineStatus,
            int settleReason, Long createTime, Long modifiedTime, int budget, int keywordMatchType, int platForm,
            String areaIds, String demographicIds, String addPrices, int campaignStatus, int campaignType,
            boolean smooth) {
        this.campaignId = campaignId;
        this.userId = userId;
        this.nick = nick;
        this.title = title;
        this.settleStatus = settleStatus;
        this.onlineStatus = onlineStatus;
        this.settleReason = settleReason;
        this.createTime = createTime;
        this.modifiedTime = modifiedTime;
        this.budget = budget;
        this.keywordMatchType = keywordMatchType;
        this.platform = platForm;

        this.areaIds = areaIds;
        this.demographicIds = demographicIds;
        this.addPrices = addPrices;

        this.campaignStatus = campaignStatus;
        this.campaignType = campaignType;

        this.smooth = smooth;

    }

    public CampaignPlay(Long userId, Campaign campaign) {
        this(userId, campaign, false);
    }

    public CampaignPlay(Long userId, Campaign campaign, boolean addInBus) {
        this.userId = userId;
        this.campaignId = campaign.getCampaignId();
        this.nick = campaign.getNick();

        if (addInBus) {
            this.onlineStatus = (OnlineStatus.ADD_IN_BUS | OnlineStatus.ADD_IN_BUS_SETTED);
        }

        updateWrapper(campaign);
    }

    public CampaignPlay(Long userId, int budget, int platForm, int matchType, Campaign campaign) {
        this.userId = userId;
        this.campaignId = campaign.getCampaignId();
        this.nick = campaign.getNick();
        this.budget = budget;
        this.platform = platForm;
        this.keywordMatchType = matchType;
        this.onlineStatus = OnlineStatus.ADD_IN_BUS;

        updateWrapper(campaign);
    }

    public CampaignPlay(Long userId, int budget, int platForm, int matchType, String areaIds, String demographicIds,
            String addPrices,
            Campaign campaign) {
        this.userId = userId;
        this.campaignId = campaign.getCampaignId();
        this.nick = campaign.getNick();
        this.budget = budget;
        this.platform = platForm;
        this.keywordMatchType = matchType;
        this.onlineStatus = OnlineStatus.ADD_IN_BUS;

        //地域和人群
        this.areaIds = areaIds;
        this.demographicIds = demographicIds;
        this.addPrices = addPrices;

        updateWrapper(campaign);
    }

    public void updateCampaign(Campaign campaign, int budget, int platForm, int matchType, boolean addInBus) {
        this.budget = budget;
        this.platform = platForm;
        this.keywordMatchType = matchType;
        this.onlineStatus = OnlineStatus.ADD_IN_BUS;

        updateWrapper(campaign);
    }

    public void updateCampaign(Campaign campaign, int budget, int platForm, int matchType, String areaIds,
            String demographicIds, String addPrices, boolean addInBus) {
        this.budget = budget;
        this.platform = platForm;
        this.keywordMatchType = matchType;
        if (addInBus == true)
            this.onlineStatus = OnlineStatus.ADD_IN_BUS;

        //地域和人群
        this.areaIds = areaIds;
        this.demographicIds = demographicIds;
        this.addPrices = addPrices;

        updateWrapper(campaign);
    }

    public void updateCampaign(Campaign campaign, String areaIds, String demographicIds, String addPrices,
            boolean addInBus) {
        //地域和人群
        this.areaIds = areaIds;
        this.demographicIds = demographicIds;
        this.addPrices = addPrices;
        if (addInBus == true)
            this.onlineStatus = OnlineStatus.ADD_IN_BUS;

        updateWrapper(campaign);
    }

    public void updateCampaign(Campaign campaign, int budget, boolean addInBus) {
        this.budget = budget;
        this.onlineStatus = OnlineStatus.ADD_IN_BUS;

        updateWrapper(campaign);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getIdColumn() {
        return "campaignId";
    }

    @Override
    public Long getId() {
        return campaignId;
    }

    @Override
    public void setId(Long id) {
        this.campaignId = id;
    }

    static String EXIST_ID_QUERY = "select campaignId from " + CampaignPlay.TABLE_NAME + " where campaignId = ? ";

    public static long findExistId(Long numIid) {
        return JDBCBuilder.singleLongQuery(EXIST_ID_QUERY, numIid);
    }

    @Override
    public boolean jdbcSave() {
        try {
            long existdId = findExistId(this.campaignId);

            if (existdId == 0L) {
                return this.rawInsert();
            } else {
                return this.rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }

    }

    @JsonIgnore
    static String insertSQL = "insert into `campaign`(`campaignId`,`userId`,`nick`,`title`,`settleStatus`,`onlineStatus`,`settleReason`,`createTime`,`modifiedTime`,`budget`,`keywordMatchType`,`platForm`,`areaIds`,`demographicIds`,`addPrices`,`campaignStatus`,`campaignType`,`smooth`) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public boolean rawInsert() {

        long ts = System.currentTimeMillis();
        long id = JDBCBuilder.insert(false, insertSQL, this.campaignId, this.userId, this.nick, this.title,
                this.settleStatus, this.onlineStatus, this.settleReason, this.createTime, this.modifiedTime,
                this.budget, this.keywordMatchType, this.platform, this.areaIds, this.demographicIds, this.addPrices,
                this.campaignStatus, this.campaignType, this.smooth);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.userId);
            return false;
        }

    }

    @JsonIgnore
    static String updateSQL = "update `campaign` set `nick` = ?, `title` = ?, `settleStatus` = ?, `onlineStatus` = ?, `settleReason` = ?, `createTime` = ?, `modifiedTime` = ?, `budget` = ?, `keywordMatchType` = ? , `platForm` = ?  , `areaIds` = ? , `demographicIds` = ?, `addPrices` = ?, `campaignStatus` = ?, `campaignType` = ?, `smooth` = ?  where `campaignId` = ? ";

    public boolean rawUpdate() {

        long updateNum = JDBCBuilder.insert(false, updateSQL, this.nick, this.title, this.settleStatus,
                this.onlineStatus, this.settleReason, this.createTime, this.modifiedTime, this.budget,
                this.keywordMatchType, this.platform, this.areaIds, this.demographicIds, this.addPrices,
                this.campaignStatus, this.campaignType, this.smooth,
                this.getId());

        String cacheKey = getCacheKey(campaignId);
        Cache.delete(cacheKey);

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for :" + this.getId() + "[campaignId : ]" + this.campaignId);
            return false;
        }
    }

    public boolean rawUpdateBudget() {

        long updateNum = JDBCBuilder.insert(false, "update `campaign` set `budget` = ? where `campaignId` = ? ",
                this.budget, this.getId());

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for :" + this.getId() + "[campaignId : ]" + this.campaignId);
            return false;
        }
    }

    @Override
    @JsonIgnore
    public String getIdName() {
        return "campaignId";
    }

    @JsonIgnore
    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }

    @JsonIgnore
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getSettleStatus() {
        return settleStatus;
    }

    public void setSettleStatus(int settleStatus) {
        this.settleStatus = settleStatus;
    }

    public int getOnlineStatus() {
        return onlineStatus;
    }

    public boolean isOnline() {

        return (this.onlineStatus & OnlineStatus.ON_LINE) > 0;
    }

    public boolean addInBus() {
        return (this.onlineStatus & OnlineStatus.ADD_IN_BUS) > 0;
    }

    public boolean addInBusSetted() {
        return (this.onlineStatus & OnlineStatus.ADD_IN_BUS_SETTED) > 0;
    }

    /**
     * set the campaign as addinbus campaign
     * 
     * @param campaignId
     * @param status
     * @return
     */
    public static boolean changeAddInBus(Long campaignId, boolean status) {
        if (status) {
            return CampaignPlay.setAddInBus(campaignId);
        } else {
            return CampaignPlay.cancelAddInBus(campaignId);
        }
    }

    @JsonIgnore
    static String SET_ADD_IN_BUSSQL = "update `campaign` set `onlineStatus` = (`onlineStatus`|? )  where `campaignId` = ? ";

    public static boolean setAddInBus(Long campaignId) {
        long updateNum = JDBCBuilder.insert(false, SET_ADD_IN_BUSSQL,
                (OnlineStatus.ADD_IN_BUS | OnlineStatus.ADD_IN_BUS_SETTED), campaignId);

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...campaignId : ]" + campaignId);
            return false;
        }
    }

    @JsonIgnore
    static String CANCEL_ADD_IN_BUSSQL = "update `campaign` set  `onlineStatus` = (`onlineStatus` & ?)  where `campaignId` = ? ";

    public static boolean cancelAddInBus(Long campaignId) {
        long updateNum = JDBCBuilder.insert(false, CANCEL_ADD_IN_BUSSQL, (~OnlineStatus.ADD_IN_BUS), campaignId);

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...campaignId : ]" + campaignId);
            return false;
        }
    }

    @JsonIgnore
    static String SET_KEYWORD_MATCH_TYPE_SQL = "update `campaign` set keywordMatchType = ?  where `campaignId` = ? ";

    /**
     * set keyword match type
     * 
     * @param campaignId
     * @param keywordMatchType
     * @return
     */
    public static boolean updateKeywordMatchType(Long campaignId, int keywordMatchType) {

        long updateNum = JDBCBuilder.insert(false, SET_KEYWORD_MATCH_TYPE_SQL, keywordMatchType, campaignId);

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...campaignId : ]" + campaignId);
            return false;
        }
    }

    @JsonIgnore
    static String SET_PLAT_FORM_SQL = "update `campaign` set platForm = ?  where `campaignId` = ? ";

    public static boolean updatePlatForm(Long campaignId, int platForm) {

        long updateNum = JDBCBuilder.insert(false, SET_PLAT_FORM_SQL, platForm, campaignId);

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...campaignId : ]" + campaignId);
            return false;
        }
    }

    @JsonIgnore
    static String UPDATE_BUDGET_SQL = "update `campaign` set budget = ? where `campaignId` = ? ";

    public static boolean updateBudget(Long campaignId, long budget, boolean isSmooth) {

        String sql = "";
        if (isSmooth) {
            sql = "update `campaign` set budget = ? , onlineStatus = onlineStatus |" + OnlineStatus.IS_SMOOTH
                    + " where `campaignId` = ? ";
        } else {
            sql = "update `campaign` set budget = ? , onlineStatus = onlineStatus &" + (~OnlineStatus.IS_SMOOTH)
                    + " where `campaignId` = ? ";
        }
        long updateNum = JDBCBuilder.insert(false, sql, budget, campaignId);

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...campaignId : ]" + campaignId);
            return false;
        }
    }

    public String getOnlineStatusStr() {
        if ((this.onlineStatus & OnlineStatus.ON_LINE) > 0) {
            return "在线";
        } else if ((this.onlineStatus & OnlineStatus.OFF_LINE) > 0) {
            return "下线";
        }

        return "下线";
    }

    public String getOnlineStatusEng() {
        if ((this.onlineStatus & OnlineStatus.ON_LINE) > 0) {
            return "online";
        } else if ((this.onlineStatus & OnlineStatus.OFF_LINE) > 0) {
            return "offline";
        }

        return "下线";
    }

    public void setOnlineStatus(int onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public int getSettleReason() {
        return settleReason;
    }

    public String getSettleStatusStr() {
        if (this.settleStatus == SettleStatus.ON_LINE) {
            return "在线";
        } else if (this.settleStatus == SettleStatus.OFF_LINE) {
            return "下线";
        }

        return "下线";
    }

    public void setSettleReason(int settleReason) {
        this.settleReason = settleReason;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(long modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

//    public static List<CampaignPlay> findbyNick(String userNick) {
//        if (org.apache.commons.lang.StringUtils.isEmpty(userNick)) {
//            return ListUtils.EMPTY_LIST;
//        }
//        // log.info("FindByNick:" + userNick);
//        return CampaignPlay.find("nick = ? ", userNick).fetch();
//    }

    @Override
    public boolean isSameEntity(Campaign t) {
        return t != null && t.getCampaignId().longValue() == this.campaignId.longValue();
    }

    @Override
    public boolean isStatusChaned(Campaign t) {
        // if(t.getOnlineStatus() ！= )
        // return false;
        return true;
    }

    @Override
    public boolean updateWrapper(Campaign campaign) {
        this.title = campaign.getTitle();

        /**
         * keep the ADD_IN_BUS status
         */
        this.nick = campaign.getNick();
        this.onlineStatus = this.onlineStatus & (~(OnlineStatus.ON_LINE | OnlineStatus.OFF_LINE));

        if (campaign.getOnlineStatus().equals("online")) {
            this.onlineStatus |= OnlineStatus.ON_LINE;
        } else {
            this.onlineStatus |= OnlineStatus.OFF_LINE;
        }

        if (campaign.getSettleStatus().equals("online")) {
            this.settleStatus = SettleStatus.ON_LINE;
        } else if (campaign.getSettleStatus().equals("offline")) {
            this.settleStatus = SettleStatus.OFF_LINE;
        }

        String settleReasonGet = campaign.getSettleReason();
        if (settleReasonGet == null || settleReasonGet.isEmpty()) {
            this.settleReason = SettleReason.NORMAL;
        } else {
            String[] reasonArray = settleReasonGet.split(";");
            for (String reason : reasonArray) {
                if (reason.equals("1")) {
                    this.settleReason |= SettleReason.BALANCE_NOT_ENOUGH;
                } else if (reason.equals("2")) {
                    this.settleReason |= SettleReason.OVER_BUDGET;
                }
            }
        }
        if (campaign.getCreateTime() != null) {
            this.createTime = campaign.getCreateTime().getTime();
        }
        if (campaign.getModifiedTime() != null) {
            this.modifiedTime = campaign.getModifiedTime().getTime();
        }

        return true;
    }

    public boolean isActive() {
        return (this.onlineStatus & OnlineStatus.ON_LINE) > 0 && (this.settleStatus & SettleStatus.ON_LINE) > 0;
    }

    @Override
    public String getTableHashKey() {
        // TODO Auto-generated method stub
        return null;
    }

//    public static List<CampaignPlay> all(String nick) {
//        if (StringUtils.isEmpty(nick)) {
//            return ListUtils.EMPTY_LIST;
//        }
//
//        return CampaignPlay.find("nick = ?", nick).fetch();
//    }

    public static List<CampaignPlay> allActive(String nick) {
        List<CampaignPlay> res = new ArrayList<CampaignPlay>();
        List<CampaignPlay> all = CampaignDao.findbyNick(nick);
        for (CampaignPlay campaignPlay : all) {
            if (campaignPlay.isActive()) {
                res.add(campaignPlay);
            }
        }
        return res;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public void updateBudget(CampaignBudget budget) {
        if (budget == null) {
            setBudget(0);
            setIsSmooth(false);
            setSmooth(false);
        } else {
            setIsSmooth(budget.getIsSmooth());
            setSmooth(budget.getIsSmooth());
            setBudget(budget.getBudget());
        }
    }

    public void setIsSmooth(boolean isSmooth) {
        if (isSmooth) {
            this.onlineStatus = this.onlineStatus | OnlineStatus.IS_SMOOTH;
        } else {
            this.onlineStatus = this.onlineStatus & (~OnlineStatus.IS_SMOOTH);
        }
    }

    static String FIND_CAMPAIGNID_WITH_TITILE = "select campaignId, title from " + CampaignPlay.TABLE_NAME
            + " where nick = ? ";

    public static Map<Long, String> findCampaignIdWithTitle(String userNick) {
        return new JDBCBuilder.JDBCMapStringExecutor(FIND_CAMPAIGNID_WITH_TITILE, userNick).call();
    }

    public static boolean campaignAddable(List<CampaignPlay> camps) {
        return CommonUtils.isEmpty(camps) || camps.size() < 4;
    }

    public int getKeywordMatchType() {
        return keywordMatchType == 0 ? 1 : keywordMatchType;
    }

    public int getPlatform() {
        return platform;
    }

    public void setPlatForm(int platform) {
        this.platform = platform;
    }

    public void setKeywordMatchType(int matchType) {
        this.keywordMatchType = matchType;
    }

    public static String QueryForFindByCampaignId = " select " + CampaignDao.CAMPAIGN_SQL + " from "
            + CampaignPlay.TABLE_NAME
            + " as campagin where campagin.campaignId = ? limit 1";

    /**
     * rewrite by zrb, to add the cache info...
     * @param campaignId
     * @return
     */
    public static CampaignPlay findbyCampId(Long campaignId) {
        if (NumberUtil.isNullOrZero(campaignId)) {
            return null;
        }

        String cacheKey = getCacheKey(campaignId);
        CampaignPlay camp = (CampaignPlay) Cache.get(cacheKey);
        if (camp != null) {
            return camp;
        }

        // return CampaignPlay.findById(campaignId);
        camp = new JDBCExecutor<CampaignPlay>(QueryForFindByCampaignId, campaignId) {
            @Override
            public CampaignPlay doWithResultSet(ResultSet rs) throws SQLException {
                return CampaignDao.parseCampaign(rs);
            }
        }.call();
        Cache.set(cacheKey, camp, "15min");
        return camp;
    }

    public static String getCacheKey(Long campaignId) {
        String cacheKey = TAG + campaignId;
        return cacheKey;
    }
}
