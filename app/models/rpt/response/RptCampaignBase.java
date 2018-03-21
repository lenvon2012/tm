package models.rpt.response;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.DBBuilder.DataSrc;
import utils.DateUtil;
import actions.rpt.RptCampaignAction;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.utils.NumberUtil;

@Entity(name = RptCampaignBase.TABLE_NAME)
@JsonAutoDetect
public class RptCampaignBase extends GenericModel implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(RptCampaignBase.class);

    public static final String TAG = "RptCampaignBase";

    public static final String TABLE_NAME = "RptCampaignBaseNew_";

    public static RptCampaignBase EMPTY = new RptCampaignBase();

    public static DBDispatcher rptCampaignBaseDispatcher = new DBDispatcher(DataSrc.BASIC, EMPTY);

    @Id
    @GeneratedValue
    @JsonIgnore
    @PolicySQLGenerator.CodeNoUpdate
    public Long id;

    public Long getId() {
        return id;
    }

    @Override
    public Object _key() {
        return getId();
    }

    @JsonProperty
    @PolicySQLGenerator.CodeNoUpdate
    @Index(name = "nick")
    @Column(columnDefinition = "varchar(63) default '' NOT NULL")
    String nick = StringUtils.EMPTY;

    @JsonProperty
    @Transient
    String date = StringUtils.EMPTY;

    @JsonIgnore
    Long dateTime = NumberUtil.DEFAULT_LONG;

    @JsonProperty
    Long campaignId = NumberUtil.DEFAULT_LONG;

    @JsonProperty
    Long impressions = NumberUtil.DEFAULT_LONG;

    @JsonProperty
    @Transient
    String source = StringUtils.EMPTY;

    @JsonProperty
    Integer sourceId = NumberUtil.DEFAULT_ZERO;

    @JsonProperty
    Integer click = NumberUtil.DEFAULT_ZERO;

    @JsonProperty
    Integer aclick = NumberUtil.DEFAULT_ZERO;

    @JsonProperty
    Integer cost = NumberUtil.DEFAULT_ZERO;

    @JsonProperty
    Double ctr = NumberUtil.DEFAULT_DOUBLE;

    @JsonProperty
    Double cpc = NumberUtil.DEFAULT_DOUBLE;

    @JsonProperty
    Double cpm = NumberUtil.DEFAULT_DOUBLE;

    @JsonProperty
    @Transient
    String searchtype = StringUtils.EMPTY;

    @JsonProperty
    Integer searchtypeId = NumberUtil.DEFAULT_ZERO;

    @JsonProperty
    Integer avgpos = NumberUtil.DEFAULT_ZERO;

    @JsonProperty
    Integer directpay = NumberUtil.DEFAULT_ZERO;

    @JsonProperty
    Integer indirectpay = NumberUtil.DEFAULT_ZERO;

    @JsonProperty
    Integer directpaycount = NumberUtil.DEFAULT_ZERO;

    @JsonProperty
    Integer indirectpaycount = NumberUtil.DEFAULT_ZERO;

    @JsonProperty
    Integer favItemCount = NumberUtil.DEFAULT_ZERO;

    @JsonProperty
    Integer favShopCount = NumberUtil.DEFAULT_ZERO;

    public RptCampaignBase() {

    }

    public RptCampaignBase(Long campaignId, Long day, int cost, long impresssions, int click, int indirectpay,
            int directpay, int indirectpaycount, int directpaycount, int favShopCount, int favItemCount) {

        this.campaignId = campaignId;
        this.dateTime = day;
        this.cost = cost;
        this.directpay = directpay;
        this.indirectpay = indirectpay;
        this.impressions = impresssions;
        this.click = click;
        this.indirectpaycount = indirectpaycount;
        this.directpaycount = directpaycount;
        this.favShopCount = favShopCount;
        this.favItemCount = favItemCount;
    }

    static String EXIST_ID_QUERY = "select id from " + RptCampaignBase.TABLE_NAME
            + " where campaignId = ?  and dateTime = ? and sourceId = ? and searchtypeId = ? ";

    public static long findExistId(Long campaignId, Long dateTime, int sourceId, int searchtypeId) {
        return rptCampaignBaseDispatcher.singleLongQuery(EXIST_ID_QUERY, campaignId, dateTime, sourceId, searchtypeId);
    }

    @Override
    public String getTableName() {
        return this.TABLE_NAME;
    }

    @Override
    public String getIdColumn() {
        return "id";
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean jdbcSave() {

        try {
            if (!RptUtils.checkDateTime(this.getDateTime())) {
                return false;
            }

            long existdId = findExistId(this.campaignId, this.getDateTime(), this.getSourceId(), this.getSearchtypeId());

            if (existdId == 0L) {
                return this.rawInsert();
            } else {
                setId(existdId);
                return this.rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }

    }

    @Override
    public String getIdName() {
        return "id";
    }

    static String insertSQL = "insert into `RptCampaignBaseNew_`(`nick`,`dateTime`,`campaignId`,`impressions`,`sourceId`,`click`,`aclick`,`cost`,`ctr`,`cpc`,`cpm`,`searchtypeId`,`avgpos`,`directpay`,"
            + "`indirectpay`,`directpaycount`,`indirectpaycount`,`favitemcount`,`favshopcount`) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public boolean rawInsert() {
        long id = rptCampaignBaseDispatcher.insert(insertSQL, this.nick, this.getDateTime(), this.campaignId,
                this.impressions, this.getSourceId(), this.click, this.aclick, this.cost, this.ctr, this.cpc, this.cpm,
                this.getSearchtypeId(), this.avgpos, this.directpay, this.indirectpay, this.directpaycount,
                this.indirectpaycount, this.favItemCount, this.favItemCount);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[nick : ]" + this.nick);

            return false;
        }

    }

    static String updateSQL = "update `RptCampaignBaseNew_` set  `dateTime` = ?,  `impressions` = ?, `sourceId` = ?, `click` = ?, `aclick` = ?, `cost` = ?, `ctr` = ?, `cpc` = ?, `cpm` = ?, `searchtypeId` = ?, `avgpos` = ? where `id` = ? ";

    public boolean rawUpdate() {

//        log.info(format("campaignId, dateTime, source, searchType".replaceAll(", ", "=%s, ") + "=%s", this.campaignId,
//                this.getDateTime(), this.getSourceId(), this.getSearchtypeId()));

        long updateNum = rptCampaignBaseDispatcher.insert(updateSQL, this.getDateTime(), this.impressions,
                this.getSourceId(), this.click, this.aclick, this.cost, this.ctr, this.cpc, this.cpm,
                this.getSearchtypeId(), this.avgpos, this.getId());

        if (updateNum == 1) {

            return true;
        } else {
            log.error("update failed...for :" + this.getId() + "[nick : ]" + this.nick);

            return false;
        }
    }

    static String updateEffectSQL = "update `RptCampaignBaseNew_` set `directpay` = ?, `indirectpay` = ?, `directpaycount` = ?, `indirectpaycount` = ?, `favitemcount` = ?, `favshopcount` = ? where `id` = ? ";

    public static boolean rawUpdateEffect(Long userId, String sid, String userNick, String subwayToken,
            RptCampaignEffect effect) {

        if (!RptUtils.checkDateTime(effect.getDateTime())) {
            return false;
        }

        long existdId = findExistId(effect.getCampaignId(), effect.getDateTime(), effect.getSourceId(),
                effect.getSearchtypeId());

        if (existdId == 0) {
            try {
                if (effect.getDateTime() > DateUtil.formYestadyMillis()) {
                    log.error("RptCampaignEffect dateTime is latter than yestoday!!! userNick:" + userNick
                            + ", campaignId:" + effect.getCampaignId());
                    return false;
                }
                new RptCampaignAction.SyncCampaignBase(userId, sid, userNick, subwayToken, effect.getCampaignId(),
                        effect.getDateTime(), effect.getDateTime(), RptUtils.Source.SUMMARY,
                        RptUtils.SearchType.SUMMARY).call();

                existdId = findExistId(effect.getCampaignId(), effect.getDateTime(), effect.getSourceId(),
                        effect.getSearchtypeId());

                if (existdId == 0) {
                    return rawInsertEffect(effect);
                }
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
                return rawInsertEffect(effect);
            }
        }

        long updateNum = rptCampaignBaseDispatcher.insert(updateEffectSQL, effect.getDirectpay(),
                effect.getIndirectpay(), effect.getDirectpaycount(), effect.getIndirectpaycount(),
                effect.getFavItemCount(), effect.getFavShopCount(), existdId);

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for :" + effect.getCampaignId() + "[nick : ]" + effect.getNick());
            return false;
        }
    }

    static String insertEffectSQL = "insert into `RptCampaignBaseNew_`(`nick`,`dateTime`,`campaignId`,`directpay`,`indirectpay`,`directpaycount`,`indirectpaycount`,`favItemCount`,`favShopCount`,`searchtypeId`,`sourceId`) values(?,?,?,?,?,?,?,?,?,?,?)";

    public static boolean rawInsertEffect(RptCampaignEffect effect) {
        long id = rptCampaignBaseDispatcher.insert(insertEffectSQL, effect.nick, effect.getDateTime(),
                effect.campaignId, effect.directpay, effect.indirectpay, effect.directpaycount,
                effect.indirectpaycount, effect.favItemCount, effect.favShopCount, effect.getSearchtypeId(),
                effect.getSourceId());

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[nick : ]" + effect.nick);

            return false;
        }

    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Long getDateTime() {
        return RptUtils.getDateTime(this.dateTime, date);
    }

    public void setDateTime(Long dateTime) {
        this.dateTime = dateTime;
    }

    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }

    public Long getImpressions() {
        return impressions;
    }

    public void setImpressions(Long impressions) {
        this.impressions = impressions;
    }

    public Integer getClick() {
        return click;
    }

    public void setClick(Integer click) {
        this.click = click;
    }

    public Integer getAclick() {
        return aclick;
    }

    public void setAclick(Integer aclick) {
        this.aclick = aclick;
    }

    public Integer getCost() {
        return cost;
    }

    public void setCost(Integer cost) {
        this.cost = cost;
    }

    public Double getCtr() {
        return ctr;
    }

    public void setCtr(Double ctr) {
        this.ctr = ctr;
    }

    public Double getCpc() {
        return cpc;
    }

    public void setCpc(Double cpc) {
        this.cpc = cpc;
    }

    public Double getCpm() {
        return cpm;
    }

    public void setCpm(Double cpm) {
        this.cpm = cpm;
    }

    public String getSearchtype() {
        return searchtype;
    }

    public void setSearchtype(String searchtype) {
        this.searchtype = searchtype;
    }

    public Integer getAvgpos() {
        return avgpos;
    }

    public void setAvgpos(Integer avgpos) {
        this.avgpos = avgpos;
    }

    @Override
    public String getTableHashKey() {
        // TODO Auto-generated method stub
        return null;
    }

    public Integer getSourceId() {
        return RptUtils.getSourceId(source);
    }

    public void setSourceId(Integer sourceId) {
        this.sourceId = sourceId;
    }

    public Integer getSearchtypeId() {
        return RptUtils.getSearchTypeId(searchtype);
    }

    public void setSearchtypeId(Integer searchtypeId) {
        this.searchtypeId = searchtypeId;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "RptCampaignBase [id=" + id + ", nick=" + nick + ", date=" + date + ", dateTime=" + dateTime
                + ", campaignId=" + campaignId + ", impressions=" + impressions + ", source=" + source + ", sourceId="
                + sourceId + ", click=" + click + ", aclick=" + aclick + ", cost=" + cost + ", ctr=" + ctr + ", cpc="
                + cpc + ", cpm=" + cpm + ", searchtype=" + searchtype + ", searchtypeId=" + searchtypeId + ", avgpos="
                + avgpos + "]";
    }

    public Integer getDirectpay() {
        return directpay;
    }

    public void setDirectpay(Integer directpay) {
        this.directpay = directpay;
    }

    public Integer getIndirectpay() {
        return indirectpay;
    }

    public void setIndirectpay(Integer indirectpay) {
        this.indirectpay = indirectpay;
    }

    public Integer getDirectpaycount() {
        return directpaycount;
    }

    public void setDirectpaycount(Integer directpaycount) {
        this.directpaycount = directpaycount;
    }

    public Integer getIndirectpaycount() {
        return indirectpaycount;
    }

    public void setIndirectpaycount(Integer indirectpaycount) {
        this.indirectpaycount = indirectpaycount;
    }

    public Integer getFavItemCount() {
        return favItemCount;
    }

    public void setFavItemCount(Integer favItemCount) {
        this.favItemCount = favItemCount;
    }

    public Integer getFavShopCount() {
        return favShopCount;
    }

    public void setFavShopCount(Integer favShopCount) {
        this.favShopCount = favShopCount;
    }
}
