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
import actions.rpt.RptCustAction;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.utils.NumberUtil;

@Entity(name = RptCustBase.TABLE_NAME)
@JsonAutoDetect
public class RptCustBase extends GenericModel implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(RptCustBase.class);

    public static final String TAG = "RptCustBase";

    public static final String TABLE_NAME = "rptcustbase_";

    public static RptCustBase EMPTY = new RptCustBase();

    public static DBDispatcher rptCustBaseDispatcher = new DBDispatcher(DataSrc.BASIC, EMPTY);

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
    @Transient
    String source = StringUtils.EMPTY;

    @JsonProperty
    Integer sourceId = NumberUtil.DEFAULT_ZERO;

    /**
     * 展现量
     */
    @JsonProperty
    Integer impressions = NumberUtil.DEFAULT_ZERO;

    /**
     * 点击量
     */
    @JsonProperty
    Integer click = NumberUtil.DEFAULT_ZERO;

    /**
     * 花费
     */
    @JsonProperty
    Integer cost = NumberUtil.DEFAULT_ZERO;

    /**
     * 总点击量
     */
    @JsonProperty
    Integer aclick = NumberUtil.DEFAULT_ZERO;

    /**
     * 点击率
     */
    @JsonProperty
    Double ctr = NumberUtil.DEFAULT_DOUBLE;

    /**
     * 平均点击花费
     */
    @JsonProperty
    Double cpc = NumberUtil.DEFAULT_DOUBLE;

    /**
     * 千次展现花费
     */
    @JsonProperty
    Double cpm = NumberUtil.DEFAULT_DOUBLE;

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

    public RptCustBase() {

    }

    public RptCustBase(String nick, Long day, int impresssions, int click, int cost, int aclick, double ctr,
            double cpc, double cpm, int directpay, int indirectpay, int directpaycount, int indirectpaycount,
            int favItemCount, int favShopCount) {

        this.nick = nick;
        this.dateTime = day;
        this.impressions = impresssions;
        this.click = click;
        this.cost = cost;
        this.aclick = aclick;
        this.ctr = ctr;
        this.cpc = cpc;
        this.cpm = cpm;
        this.directpay = directpay;
        this.indirectpay = indirectpay;
        this.indirectpaycount = indirectpaycount;
        this.directpaycount = directpaycount;
        this.favshopcount = favShopCount;
        this.favitemcount = favItemCount;
    }

    static String EXIST_ID_QUERY = "select id from " + RptCustBase.TABLE_NAME
            + " where nick = ?  and dateTime = ? and sourceId = ? ";

    public static long findExistId(String nick, Long dateTime, int sourceId) {
        return rptCustBaseDispatcher.singleLongQuery(EXIST_ID_QUERY, nick, dateTime, sourceId);
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

    static String insertSQL = "insert into `rptcustbase_`(`nick`,`dateTime`,`sourceId`,`impressions`,`click`,`cost`,`aclick`,`ctr`,`cpc`,`cpm`,`directpay`,"
            + "`indirectpay`,`directpaycount`,`indirectpaycount`,`favitemcount`,`favshopcount`) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public boolean rawInsert() {
        long id = rptCustBaseDispatcher.insert(insertSQL, this.nick, this.getDateTime(), this.getSourceId(),
                this.impressions, this.click, this.cost, this.aclick, this.ctr, this.cpc, this.cpm, this.directpay,
                this.indirectpay, this.directpaycount, this.indirectpaycount, this.favshopcount, this.favitemcount);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[nick : ]" + this.nick);

            return false;
        }

    }

    static String updateSQL = "update `rptcustbase_` set  `dateTime` = ?, `sourceId` = ?, `impressions` = ?, `click` = ?, `cost` = ?, `aclick` = ?, `ctr` = ?, `cpc` = ?, `cpm` = ? where `id` = ? ";

    public boolean rawUpdate() {

        long updateNum = rptCustBaseDispatcher.insert(updateSQL, this.getDateTime(), this.getSourceId(),
                this.impressions, this.click, this.cost, this.aclick, this.ctr, this.cpc, this.cpm, this.getId());

        if (updateNum == 1) {

            return true;
        } else {
            log.error("update failed...for :" + this.getId() + "[nick : ]" + this.nick);

            return false;
        }
    }

    static String updateEffectSQL = "update `rptcustbase_` set `directpay` = ?, `indirectpay` = ?, `directpaycount` = ?, `indirectpaycount` = ?, `favitemcount` = ?, `favshopcount` = ? where `id` = ? ";

    public static boolean rawUpdateEffect(Long userId, String sid, String userNick, String subwayToken,
            RptCustEffect effect) {

        if (!RptUtils.checkDateTime(effect.getDateTime())) {
            return false;
        }

        long existdId = findExistId(effect.getNick(), effect.getDateTime(), effect.getSourceId());

        if (existdId == 0) {
            try {
                if (effect.getDateTime() > DateUtil.formYestadyMillis()) {
                    log.error("RptCustEffect dateTime is latter than yestoday!!! userNick:" + userNick);
                    return false;
                }
                new RptCustAction.SyncCustBase(userId, sid, userNick, subwayToken, effect.getDateTime(),
                        effect.getDateTime(), RptUtils.Source.SUMMARY).call();

                existdId = findExistId(effect.getNick(), effect.getDateTime(), effect.getSourceId());

                if (existdId == 0) {
                    return rawInsertEffect(effect);
                }
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
                return rawInsertEffect(effect);
            }
        }

        long updateNum = rptCustBaseDispatcher.insert(updateEffectSQL, effect.getDirectpay(), effect.getIndirectpay(),
                effect.getDirectpaycount(), effect.getIndirectpaycount(), effect.getFavitemcount(),
                effect.getFavshopcount(), existdId);

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for :" + effect.getNick() + "[nick : ]" + effect.getNick());
            return false;
        }
    }

    static String insertEffectSQL = "insert into `rptcustbase_`(`nick`,`dateTime`,`sourceId`,`directpay`,`indirectpay`,`directpaycount`,`indirectpaycount`,`favitemcount`,`favshopcount`) values(?,?,?,?,?,?,?,?,?)";

    public static boolean rawInsertEffect(RptCustEffect effect) {
        long id = rptCustBaseDispatcher.insert(insertEffectSQL, effect.nick, effect.getDateTime(),
                effect.getSourceId(), effect.directpay, effect.indirectpay, effect.directpaycount,
                effect.indirectpaycount, effect.favitemcount, effect.favshopcount);

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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Integer getImpressions() {
        return impressions;
    }

    public void setImpressions(Integer impressions) {
        this.impressions = impressions;
    }

    public Integer getClick() {
        return click;
    }

    public void setClick(Integer click) {
        this.click = click;
    }

    public Integer getCost() {
        return cost;
    }

    public void setCost(Integer cost) {
        this.cost = cost;
    }

    public Integer getAclick() {
        return aclick;
    }

    public void setAclick(Integer aclick) {
        this.aclick = aclick;
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

}
