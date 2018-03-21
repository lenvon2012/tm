package models.rpt.response;

import static java.lang.String.format;

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
import transaction.JDBCBuilder;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.utils.NumberUtil;

@Entity(name = RptCampaignEffect.TABLE_NAME)
@JsonAutoDetect
public class RptCampaignEffect extends GenericModel implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(RptCampaignEffect.class);

    public static final String TAG = "RptCampaignEffect";

    public static final String TABLE_NAME = "rptcampaigneffect_";

    public static RptCampaignEffect EMPTY = new RptCampaignEffect();

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

    @JsonProperty
    @Transient
    String searchtype = StringUtils.EMPTY;

    @JsonProperty
    Integer searchtypeId = NumberUtil.DEFAULT_ZERO;

    @JsonProperty
    @Transient
    String source = StringUtils.EMPTY;

    @JsonProperty
    Integer sourceId = NumberUtil.DEFAULT_ZERO;

    public RptCampaignEffect() {

    }

    static String EXIST_ID_QUERY = "select id from " + RptCampaignEffect.TABLE_NAME
            + " where campaignId = ?  and dateTime = ? and sourceId = ? and searchtypeId = ? ";

    public static long findExistId(Long campaignId, Long dateTime, int sourceId, int searchtypeId) {
        return JDBCBuilder.singleLongQuery(EXIST_ID_QUERY, campaignId, dateTime, sourceId, searchtypeId);
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

    static String insertSQL = "insert into `rptcampaigneffect_`(`nick`,`dateTime`,`campaignId`,`directpay`,`indirectpay`,`directpaycount`,`indirectpaycount`,`favItemCount`,`favShopCount`,`searchtypeId`,`sourceId`) values(?,?,?,?,?,?,?,?,?,?,?)";

    public boolean rawInsert() {
        long id = JDBCBuilder.insert(false, insertSQL, this.nick, this.getDateTime(), this.campaignId, this.directpay,
                this.indirectpay, this.directpaycount, this.indirectpaycount, this.favItemCount, this.favShopCount,
                this.getSearchtypeId(), this.getSourceId());

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[nick : ]" + this.nick);

            return false;
        }

    }

    static String updateSQL = "update `rptcampaigneffect_` set  `dateTime` = ?, `campaignId` = ?, `directpay` = ?, `indirectpay` = ?, `directpaycount` = ?, `indirectpaycount` = ?, `favItemCount` = ?, `favShopCount` = ?, `searchtypeId` = ?, `sourceId` = ? where `id` = ? ";

    public boolean rawUpdate() {

        log.info(format("campaignId, dateTime, source, searchType".replaceAll(", ", "=%s, ") + "=%s", this.campaignId,
                this.getDateTime(), this.getSourceId(), this.getSearchtypeId()));

        long updateNum = JDBCBuilder.insert(false, updateSQL, this.getDateTime(), this.campaignId, this.directpay,
                this.indirectpay, this.directpaycount, this.indirectpaycount, this.favItemCount, this.favShopCount,
                this.getSearchtypeId(), this.getSourceId(), this.getId());

        if (updateNum == 1) {

            return true;
        } else {
            log.error("update failed...for :" + this.getId() + "[nick : ]" + this.nick);

            return false;
        }
    }

    public Long getDateTime() {
        return RptUtils.getDateTime(this.dateTime, date);
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

    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
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

    public String getSearchtype() {
        return searchtype;
    }

    public void setSearchtype(String searchtype) {
        this.searchtype = searchtype;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setDateTime(Long dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public String getTableHashKey() {
        // TODO Auto-generated method stub
        return null;
    }

    public Integer getSearchtypeId() {
        return RptUtils.getSearchTypeId(searchtype);
    }

    public void setSearchtypeId(Integer searchtypeId) {
        this.searchtypeId = searchtypeId;
    }

    public Integer getSourceId() {
        return RptUtils.getSourceId(source);
    }

    public void setSourceId(Integer sourceId) {
        this.sourceId = sourceId;
    }

    @Override
    public String toString() {
        return "RptCampaignEffect [id=" + id + ", nick=" + nick + ", date=" + date + ", dateTime=" + dateTime
                + ", campaignId=" + campaignId + ", directpay=" + directpay + ", indirectpay=" + indirectpay
                + ", directpaycount=" + directpaycount + ", indirectpaycount=" + indirectpaycount + ", favItemCount="
                + favItemCount + ", favShopCount=" + favShopCount + ", searchtype=" + searchtype + ", searchtypeId="
                + searchtypeId + ", source=" + source + ", sourceId=" + sourceId + "]";
    }
}
