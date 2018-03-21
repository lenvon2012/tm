
package result.pojo.rpt;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

import models.campaign.CampaignPlay;
import models.campaign.CampaignPlay.CampaignAreaUtil;
import models.campaign.CampaignPlay.DemographicUtil;
import models.rpt.response.RptCampaignBase;
import models.updatetimestamp.updatets.RptCampaignUpdateTs;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder.JDBCExecutor;
import utils.DateUtil;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.NumberUtil;

@JsonAutoDetect
@Entity(name = RptCampaignResultPojo.TABLE_NAME)
public class RptCampaignResultPojo extends Model implements PolicySQLGenerator, IRptResultPojo<RptCampaignResultPojo> {

    private static final Logger log = LoggerFactory.getLogger(RptCampaignResultPojo.class);

    public static final String TAG = "RptCampaignResultPojo";

    public static final String TABLE_NAME = "RptCampaignResultPojoNew_";

    public static RptCampaignResultPojo EMPTY = new RptCampaignResultPojo();

    public static DBDispatcher rptCampaignResultPojoDispatcher = new DBDispatcher(DataSrc.BASIC, EMPTY);

    public RptCampaignResultPojo() {
    }

    public final static int CAMPAIGN_TITLE_LENGTH = 60;

    /**
     * used for timeLength
     */
    @Index(name = "timeLength")
    public Long timeLength = 0L;

    @JsonProperty(value = "campaignId")
    @Index(name = "campaignId")
    public Long campaignId;

    public static class DAY {
        public static Long SUM = -1L;

        public static Long AVG = -2L;
    }

    public Long userId;

    public Long day;

    /**
     * 总花费
     */
    public double cost;

    /**
     * 总收入
     */
    public double indirectpayamount;

    public double directpayamount;

    /**
     * 总展现量
     */
    @JsonProperty(value = "impressions")
    public long impressions;

    /**
     * 总点击数
     */
    @JsonProperty(value = "aclick")
    public long aclick;

    /**
     * 总成交笔数
     */
    public long indirectpaycount;

    public long directpaycount;

    /**
     * 店铺收藏次数
     */
    @JsonProperty(value = "favshopcount")
    public int favshopcount;

    /**
     * 宝贝收藏次数
     */
    @JsonProperty(value = "favitemcount")
    public int favitemcount;

    /**
     * campaign info
     */
    /**
     * 推广计划标题
     */
    @JsonProperty(value = "title")
    // @Column(columnDefinition = "varchar(" + CAMPAIGN_TITLE_LENGTH + ")",
    // length = CAMPAIGN_TITLE_LENGTH, nullable = false)
    @Transient
    public String title;

    /**
     * 上下线状态
     */
    @Transient
    public int onlineStatus;

    /**
     * 结算状态
     */
    @Transient
    public int settleStatus;

    /**
     * 创建时间
     */
    @Transient
    public long createTime;

    /**
     * 最后修改时间
     */
    @Transient
    public long modifiedTime;

    /**
     * 日限额
     */
    @JsonProperty(value = "budget")
    @Transient
    public double budget;

    /**
     * 投放平台
     */
    @Transient
    public int platForm;

    /**
     * 关键词匹配方式
     */
    @Transient
    public int keywordMatchType;

    /**
     * 地域
     */
    @Transient
    public String areaNames;

    /**
     * 地域
     */
    @Transient
    public String demographicNames;

    @JsonProperty(value = "adgroupCnt")
    @Transient
    public int adgroupCnt = 0;

    /**
     * calculate when get
     */
    /**
     * 点击率
     */
    @JsonProperty(value = "ctr")
    @Transient
    public double ctr;

    /**
     * 点击转化率 click conversion rate
     */
    @JsonProperty(value = "ccr")
    @Transient
    public double ccr;

    /**
     * 平均点击花费
     */
    @JsonProperty(value = "cpc")
    @Transient
    public double cpc;

    /**
     * roi
     */
    @JsonProperty(value = "roi")
    @Transient
    public double roi;

    @Transient
    public boolean addInBus;

    @Transient
    private int campaignType;//推广计划类型

    public RptCampaignResultPojo(RptCampaignBase base, long timeLength) {

        this.campaignId = base.getCampaignId();
        // this.timeLength =
        // UserNick.findOrCreatetimeLengthIdByNick(base.getNick());
        this.timeLength = timeLength;
        this.campaignId = base.getCampaignId();
        this.day = base.getDateTime();

        this.cost = base.getCost() == null ? 0 : base.getCost();
        this.impressions = base.getImpressions() == null ? 0 : base.getImpressions();
        this.aclick = base.getClick() == null ? 0 : base.getClick();

        this.directpayamount += (base.getDirectpay() == null ? 0 : base.getDirectpay());
        this.indirectpayamount += (base.getIndirectpay() == null ? 0 : base.getIndirectpay());

        this.directpaycount += (base.getDirectpaycount() == null ? 0 : base.getDirectpaycount());
        this.indirectpaycount += (base.getIndirectpaycount() == null ? 0 : base.getIndirectpaycount());

        this.favitemcount = (base.getFavItemCount() == null ? 0 : base.getFavItemCount());
        this.favshopcount = (base.getFavShopCount() == null ? 0 : base.getFavShopCount());

    }

    public void setBasicInfo(CampaignPlay campaign) {
        this.userId = campaign.getUserId();
        this.campaignId = campaign.getCampaignId();
        this.title = campaign.getTitle();
        this.onlineStatus = campaign.getOnlineStatus();
        this.settleStatus = campaign.getSettleStatus();
        this.createTime = campaign.getCreateTime();
        this.modifiedTime = campaign.getModifiedTime();
        this.budget = campaign.getBudget();
        this.platForm = campaign.getPlatform();
        this.keywordMatchType = campaign.getKeywordMatchType();
        this.addInBus = campaign.addInBus();
        //this.adgroupCnt = (int) ADGroupDao.countByCampaignId(userId, campaignId);
        this.campaignType = campaign.getCampaignType();
        //地域
        areaNames = CampaignAreaUtil.showPartAreaNames(campaign.getAreaIds());
        demographicNames = DemographicUtil.showPartDemographicNames(campaign.getDemographicIds());
    }

    public RptCampaignResultPojo(String userNick, Long campaignId, Long day, long timeLength, double cost,
            double indirectpayamount, double directpayamount, long indirectpaycount, long directpaycount,
            long impresssions, long aclick, int favshopcount, int favItemcount) {

        // this.timeLength = UserNick.findOrCreatetimeLengthIdByNick(userNick);
        this.timeLength = timeLength;
        this.campaignId = campaignId;
        this.day = day;
        this.cost = cost;
        this.directpayamount = directpayamount;
        this.indirectpayamount = indirectpayamount;
        this.impressions = impresssions;
        this.aclick = aclick;
        this.indirectpaycount = indirectpaycount;
        this.directpaycount = directpaycount;
        this.favshopcount = favshopcount;
        this.favitemcount = favItemcount;
    }

    public RptCampaignResultPojo add(RptCampaignResultPojo pojo) {

        this.cost += pojo.getCost();
        this.impressions += pojo.getImpressions();
        this.aclick += pojo.getAclick();
        this.indirectpayamount += pojo.getIndirectpayamount();
        this.directpayamount += pojo.getDirectpayamount();
        this.indirectpaycount += pojo.getIndirectpaycount();
        this.directpaycount += pojo.getDirectpaycount();
        this.favitemcount += pojo.getFavitemcount();
        this.favshopcount += pojo.getFavshopcount();

        return this;
    }

    public RptCampaignResultPojo divide(int divider) {

        this.cost /= divider;
        this.impressions /= divider;
        this.aclick /= divider;
        this.indirectpayamount /= divider;
        this.directpayamount /= divider;
        this.indirectpaycount /= divider;
        this.directpaycount /= divider;
        this.favitemcount /= divider;
        this.favshopcount /= divider;

        return this;
    }

    public RptCampaignResultPojo(Long day) {
        this("", day, 0L);
    }

    public RptCampaignResultPojo(String nick, Long day, long timeLength) {

        // this.timeLength = UserNick.findOrCreatetimeLengthIdByNick(nick);
        this.timeLength = timeLength;
        this.day = day;
        this.cost = 0.0d;
        this.indirectpayamount = 0.0d;
        this.directpayamount = 0.0d;
        this.impressions = 0;
        this.aclick = 0;
        this.indirectpaycount = 0;
        this.directpaycount = 0;
        this.ctr = 0.0d;
        this.ccr = 0.0d;
        this.cpc = 0.0d;
        this.favitemcount = 0;
        this.favshopcount = 0;
        this.roi = 0.0;

    }

    @JsonProperty(value = "day")
    public String getDayPojo() {
        if (this.day == DAY.SUM) {
            return "总和";
        } else if (this.day == DAY.AVG) {
            return "平均";
        }
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date(day));
    }

    @JsonProperty(value = "cost")
    public double getCostPojo() {
        return NumberUtil.doubleFormatter(cost / 100);
    }

    @JsonProperty(value = "payamount")
    public double getPayamountPojo() {
        return NumberUtil.doubleFormatter((this.indirectpayamount + this.directpayamount) / 100);
    }

    @JsonProperty(value = "paycount")
    public long getPaycountPojo() {
        return (this.directpaycount + this.indirectpaycount);
    }

    public long getImpressions() {
        return impressions;
    }

    public long getAclick() {
        return aclick;
    }

    public double getCtr() {
        return impressions == 0 ? 0 : (double) aclick / impressions;
    }

    public double getCcr() {
        return aclick == 0 ? 0 : (double) (directpaycount + indirectpaycount) / aclick;
    }

    public double getCpc() {
        return aclick == 0 ? 0 : NumberUtil.doubleFormatter(getCostPojo() / aclick);
    }

    public int getFavshopcount() {
        return favshopcount;
    }

    public int getFavitemcount() {
        return favitemcount;
    }

    public int getCampaignType() {
        return campaignType;
    }

    public void setCampaignType(int campaignType) {
        this.campaignType = campaignType;
    }

    public double getRoi() {
        return cost == 0 ? 0 : NumberUtil.double4Formatter(getPayamount() / cost);
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public double getPayamount() {
        return this.indirectpayamount + this.directpayamount;
    }

    public long getPaycount() {
        return (this.directpaycount + this.indirectpaycount);
    }

    public void setImpressions(long impressions) {
        this.impressions = impressions;
    }

    public void setAclick(long aclick) {
        this.aclick = aclick;
    }

    public void setCtr(double ctr) {
        this.ctr = ctr;
    }

    public void setCcr(double ccr) {
        this.ccr = ccr;
    }

    public void setCpc(double cpc) {
        this.cpc = cpc;
    }

    public void setFavshopcount(int favshopcount) {
        this.favshopcount = favshopcount;
    }

    public void setFavitemcount(int favitemcount) {
        this.favitemcount = favitemcount;
    }

    public void setRoi(double roi) {
        this.roi = roi;

    }

    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty(value = "onlineStatus")
    public String getOnlineStatusStr() {
        return CampaignPlay.OnlineStatus.getOnlineStatus(onlineStatus);
    }

    @JsonProperty(value = "isSmooth")
    public boolean getIsSmooth() {
        return (this.onlineStatus & CampaignPlay.OnlineStatus.IS_SMOOTH) > 0;
    }

    @JsonProperty(value = "settleStatus")
    public String getSettleStatusStr() {
        return CampaignPlay.SettleStatus.getSettleStatus(settleStatus);
    }

    @JsonProperty(value = "createTime")
    public String getCreateTimeStr() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(createTime));
    }

    @JsonProperty(value = "modifiedTime")
    public String getModifiedTimeStr() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(modifiedTime));
    }

    @JsonProperty(value = "matchType")
    public String getKeywordMatchType() {
        return CampaignPlay.MatchType.getStr(keywordMatchType);
    }

    @JsonProperty(value = "platForm")
    public List<String> getPlatForm() {
        return CampaignPlay.PlatForm.getStrList(platForm);
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public String getAreaNames() {
        return areaNames;
    }

    public void setAreaNames(String areaNames) {
        this.areaNames = areaNames;
    }

    public String getDemographicNames() {
        return demographicNames;
    }

    public void setDemographicNames(String demographicNames) {
        this.demographicNames = demographicNames;
    }

    @Override
    public String getTableName() {
        return this.TABLE_NAME;
    }

    @Override
    public String getTableHashKey() {
        return null;
    }

    @Override
    public String getIdColumn() {
        return "id";
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getIdName() {
        return "id";
    }

    static String QueryForDayByCampaignId = "select  id, day from " + TABLE_NAME
            + " where campaignId = ? and timeLength = ? ";

    public long findExistId(Long campaignId, long timeLength) {

        List<Long> result = new JDBCExecutor<List<Long>>(true, rptCampaignResultPojoDispatcher,
                QueryForDayByCampaignId, campaignId, timeLength) {
            @Override
            public List<Long> doWithResultSet(ResultSet rs) throws SQLException {
                List<Long> result = new ArrayList<Long>();
                if (rs.next()) {
                    result.add(rs.getLong(1));
                    result.add(rs.getLong(2));
                }
                return result;
            }
        }.call();

        if (CommonUtils.isEmpty(result)) {
            return 0L;
        } else {
            this.setId(result.get(0));
            return result.get(1);
        }

        // return JDBCBuilder.singleLongQuery(QueryForDayByCampaignId,
        // campaignId, timeLength);
    }

    @Override
    public boolean jdbcSave(Long userId) {
        RptCampaignUpdateTs updateTs = RptCampaignUpdateTs.findByUserId(userId);

        if (updateTs == null || updateTs.getLastUpdateTime() + DateUtil.DAY_MILLIS < this.day) {
            return false;
        }

        return jdbcSave();
    }

    @Override
    public boolean jdbcSave() {

        try {
            long day = findExistId(this.campaignId, this.timeLength);

            if (day == 0L) {
                return this.rawInsert();
            } else if (day <= this.day) {
                return this.rawUpdate();
            }

            return true;

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }

    }

    static String insertSQL = "insert into `RptCampaignResultPojoNew_`(`timeLength`,`campaignId`,`day`,`cost`,`indirectpayamount`,"
            + "`directpayamount`,`impressions`,`aclick`,`indirectpaycount`,`directpaycount`,`favshopcount`,`favitemcount`) values(?,?,?,?,?,?,?,?,?,?,?,?)";

    public boolean rawInsert() {

        long id = rptCampaignResultPojoDispatcher.insert(insertSQL, this.timeLength, this.campaignId, this.day,
                this.cost, this.indirectpayamount, this.directpayamount, this.impressions, this.aclick,
                this.indirectpaycount, this.directpaycount, this.favshopcount, this.favitemcount);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[campaignId : ]" + this.campaignId);

            return false;
        }
    }

    static String updateSQL = "update `RptCampaignResultPojoNew_` set  `timeLength` = ?, `day` = ?, `cost` = ?, `indirectpayamount` = ?, `directpayamount` = ?, `impressions` = ?, `aclick` = ?, `indirectpaycount` = ?, `directpaycount` = ?, `favshopcount` = ?, `favitemcount` = ? where `id` = ? ";

    public boolean rawUpdate() {

        long updateNum = rptCampaignResultPojoDispatcher.insert(updateSQL, this.timeLength, this.day, this.cost,
                this.indirectpayamount, this.directpayamount, this.impressions, this.aclick, this.indirectpaycount,
                this.directpaycount, this.favshopcount, this.favitemcount, this.getId());

        if (updateNum == 1) {

            return true;
        } else {
            log.error("update failed...for :" + this.getId() + "[campaignId : ]" + this.campaignId);

            return false;
        }
    }

    public Long getTimeLength() {
        return timeLength;
    }

    public void setTimeLength(Long timeLength) {
        this.timeLength = timeLength;
    }

    public Long getDay() {
        return day;
    }

    public void setDay(Long day) {
        this.day = day;
    }

    public int getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(int onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public int getSettleStatus() {
        return settleStatus;
    }

    public void setSettleStatus(int settleStatus) {
        this.settleStatus = settleStatus;
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

    // public static RptCampaignResultPojo findByCampaignIdAndTimeLength(Long
    // campaignId, long timeLength) {
    // return RptCampaignResultPojo.find("campaignId = ?  and timeLength = ?",
    // campaignId, timeLength).first();
    // }

    public double getIndirectpayamount() {
        return indirectpayamount;
    }

    public void setIndirectpayamount(double indirectpayamount) {
        this.indirectpayamount = indirectpayamount;
    }

    public double getDirectpayamount() {
        return directpayamount;
    }

    public void setDirectpayamount(double directpayamount) {
        this.directpayamount = directpayamount;
    }

    public long getIndirectpaycount() {
        return indirectpaycount;
    }

    public void setIndirectpaycount(long indirectpaycount) {
        this.indirectpaycount = indirectpaycount;
    }

    public long getDirectpaycount() {
        return directpaycount;
    }

    public void setDirectpaycount(long directpaycount) {
        this.directpaycount = directpaycount;
    }

    public double getCost() {
        return cost;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public boolean isAddInBus() {
        return addInBus;
    }

    public void setAddInBus(boolean addInBus) {
        this.addInBus = addInBus;
    }

    public void setPlatForm(int platForm) {
        this.platForm = platForm;
    }

    public void setKeywordMatchType(int keywordMatchType) {
        this.keywordMatchType = keywordMatchType;
    }

    public void setAdgroupCnt(int adgroupCnt) {
        this.adgroupCnt = adgroupCnt;
    }

    public int getAdgroupCnt() {
        return adgroupCnt;
    }
}
