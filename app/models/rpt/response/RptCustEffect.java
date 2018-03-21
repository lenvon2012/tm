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
import transaction.JDBCBuilder;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.utils.NumberUtil;

@Entity(name = RptCustEffect.TABLE_NAME)
@JsonAutoDetect
public class RptCustEffect extends GenericModel implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(RptCustEffect.class);

    public static final String TAG = "RptCustEffect";

    public static final String TABLE_NAME = "rptcusteffect_";

    public static RptCustEffect EMPTY = new RptCustEffect();

    @Id
    @GeneratedValue
    @JsonIgnore
    public Long id;

    public Long getId() {
        return id;
    }

    @Override
    public Object _key() {
        return getId();
    }

    @JsonProperty
    @Index(name = "nick")
    @Column(columnDefinition = "varchar(63) default '' NOT NULL")
    String nick = StringUtils.EMPTY;

    @JsonProperty
    @Transient
    String date = StringUtils.EMPTY;

    @JsonIgnore
    Long dateTime = NumberUtil.DEFAULT_LONG;

    @JsonProperty
    @Transient
    String source = StringUtils.EMPTY;

    @JsonProperty
    Integer sourceId = NumberUtil.DEFAULT_ZERO;

    /**
     * 直接成交金额
     */
    @JsonProperty
    Integer directpay = NumberUtil.DEFAULT_ZERO;

    /**
     * 间接成交金额
     */
    @JsonProperty
    Integer indirectpay = NumberUtil.DEFAULT_ZERO;

    /**
     * 直接成交笔数
     */
    @JsonProperty
    Integer directpaycount = NumberUtil.DEFAULT_ZERO;

    /**
     * 间接成交笔数
     */
    @JsonProperty
    Integer indirectpaycount = NumberUtil.DEFAULT_ZERO;

    /**
     * 宝贝收藏数
     */
    @JsonProperty
    Integer favitemcount = NumberUtil.DEFAULT_ZERO;

    /**
     * 店铺收藏数
     */
    @JsonProperty
    Integer favshopcount = NumberUtil.DEFAULT_ZERO;

    public RptCustEffect() {

    }

    @Override
    public String toString() {
        return "RptCustEffect [id=" + id + ", nick=" + nick + ", date=" + date + ", dateTime=" + dateTime + ", source="
                + source + ", directpay=" + directpay + ", indirectpay=" + indirectpay + ", directpaycount="
                + directpaycount + ", indirectpaycount=" + indirectpaycount + ", favitemcount=" + favitemcount
                + ", favshopcount=" + favshopcount + "]";
    }

    static String EXIST_ID_QUERY = "select id from " + RptCustEffect.TABLE_NAME
            + " where nick = ?  and dateTime = ? and sourceId = ?";

    public static long findExistId(String nick, Long dateTime, Integer sourceId) {
        return JDBCBuilder.singleLongQuery(EXIST_ID_QUERY, nick, dateTime, sourceId);
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
            long existdId = findExistId(this.nick, this.getDateTime(), this.getSourceId());

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

    static String insertSQL = "insert into `rptcusteffect_`(`nick`,`dateTime`,`sourceId`,`directpay`,`indirectpay`,`directpaycount`,`indirectpaycount`,`favitemcount`,`favshopcount`) values(?,?,?,?,?,?,?,?,?)";

    public boolean rawInsert() {
        long id = JDBCBuilder.insert(false, insertSQL, this.nick, this.getDateTime(), this.getSourceId(),
                this.directpay, this.indirectpay, this.directpaycount, this.indirectpaycount, this.favitemcount,
                this.favshopcount);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[nick : ]" + this.nick);

            return false;
        }

    }

    static String updateSQL = "update `rptcusteffect_` set  `sourceId` = ?, `directpay` = ?, `indirectpay` = ?, `directpaycount` = ?, `indirectpaycount` = ?, `favitemcount` = ?, `favshopcount` = ? where `id` = ? ";

    public boolean rawUpdate() {

        long updateNum = JDBCBuilder.insert(false, updateSQL, this.getSourceId(), this.directpay, this.indirectpay,
                this.directpaycount, this.indirectpaycount, this.favitemcount, this.favshopcount, this.getId());

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for :" + this.getId() + "[nick : ]" + this.nick);

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

    public Integer getSource() {
        return RptUtils.getSourceId(source);
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

    public Integer getFavitemcount() {
        return favitemcount;
    }

    public void setFavitemcount(Integer favitemcount) {
        this.favitemcount = favitemcount;
    }

    public Integer getFavshopcount() {
        return favshopcount;
    }

    public void setFavshopcount(Integer favshopcount) {
        this.favshopcount = favshopcount;

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

    public void setSource(String source) {
        this.source = source;
    }

}
