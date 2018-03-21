
package models.trade;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import models.Status;
import models.Status.TRADE_FROM;
import models.Status.TRADE_STATUS;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.db.jpa.GenericModel;
import transaction.DBBuilder;
import transaction.DBBuilder.DataSrc;
import utils.TBIpApi;
import utils.TBIpApi.IpDataBean;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;
import com.ciaosir.client.utils.NumberUtil;
import com.taobao.api.domain.Trade;

import dao.trade.OrderDisplayDao;

/**
 * 交易结构
 * 
 */
@JsonIgnoreProperties(value = {
        "tableHashKey", "persistent", "tableName", "idName", "idColumn", "entityId"
})
@Entity(name = TradeDisplay.TABLE_NAME)
public class TradeDisplay extends GenericModel implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(TradeDisplay.class);

    public static final String TAG = "TradeDisplay";

    public static final String TABLE_NAME = "trade_display_";

    public static final TradeDisplay EMPTY = new TradeDisplay();

    public static DBDispatcher dp = new DBDispatcher(DataSrc.QUOTA, EMPTY);

    /**
     * 交易编号 (父订单的交易编号) 该变量有可能为负，但不为0
     */
    @Id
    @Index(name = "idx_tid")
    @CodeNoUpdate
    public Long tid;

    /**
     * Trade对应的卖家
     */
    @CodeNoUpdate
    @Index(name = "user_id")
    public Long userId;

    @CodeNoUpdate
    public Long ts;

    /**
     * 交易状态。可选值:
     * TRADE_NO_CREATE_PAY(没有创建支付宝交易)
     * WAIT_BUYER_PAY(等待买家付款)
     * SELLER_CONSIGNED_PART(卖家部分发货)
     * WAIT_SELLER_SEND_GOODS(等待卖家发货,即:买家已付款)
     * WAIT_BUYER_CONFIRM_GOODS(等待买家确认收货,即:卖家已发货)
     * TRADE_BUYER_SIGNED(买家已签收,货到付款专用)
     * TRADE_FINISHED(交易成功)
     * TRADE_CLOSED(付款以后用户退款成功，交易自动关闭)
     * TRADE_CLOSED_BY_TAOBAO(付款以前，卖家或买家主动关闭交易)
     */
    public int status;

    /**
     * 买家昵称buyerMessage
     */
    @Index(name = "idx_buyer_nick")
    @CodeNoUpdate
    public String buyerNick = null;

    @Column(columnDefinition = "varchar(63) default null")
    String buyerAlipayNo = null;

    /**
     * 商品购买数量。取值范围：大于零的整数
     */
    @CodeNoUpdate
    public Integer num = 0;

    /**
     * 交易来源。 WAP(手机);HITAO(嗨淘);TOP(TOP平台);TAOBAO(普通淘宝);JHS(聚划算)
     */
    @CodeNoUpdate
    public int tradeFrom = 1;

    /**
     * 实付金额。精确到2位小数;单位:元。如:200.07，表示:200元7分
     */
    public double payment = -1d;

    /**
     * 邮费。精确到2位小数;单位:元。如:200.07，表示:200元7分
     */
    public double postFee = -1d;

    /**
     * 商品价格。精确到2位小数；单位：元。如：200.07，表示：200元7分
     */
    public double price = -1d;

    /**
     * 卖家实际收到的支付宝打款金额（由于子订单可以部分确认收货，这个金额会随着子订单的确认收货而不断增加，交易成功后等于买家实付款减去退款金额）。 精确到2位小数;单位:元。如:200.07，表示:200元7分
     */
    public double receivedPayment = -1d;

    /**
     * 商品金额（商品价格乘以数量的总金额）。精确到2位小数;单位:元。如:200.07，表示:200元7分
     */
    public double totalFee = -1d;

    /**
     * 交易创建时间。格式:yyyy-MM-dd HH:mm:ss
     */
    @Index(name = "created")
    public Long created = NumberUtil.DEFAULT_LONG;

    /**
     * 付款时间。格式:yyyy-MM-dd HH:mm:ss
     */
    public Long payTime;

    /**
     * 卖家发货时间。格式:yyyy-MM-dd HH:mm:ss
     */
    public Long consignTime;

    /**
     * 交易结束时间。交易成功时间(更新交易状态为成功的同时更新)/确认收货时间或者交易关闭时间 。格式:yyyy-MM-dd HH:mm:ss
     */
    public Long endTime;

    /**
     * 交易修改时间(用户对订单的任何修改都会更新此字段)。格式:yyyy-MM-dd HH:mm:ss
     */
    public Long modified;

    @Index(name = "created_day")
    public Long createdDay;

    @Index(name = "paytime_day")
    public Long payTimeDay;

    public Long consignTimeDay;

    @Index(name = "endtime_day")
    public Long endTimeDay;

    public Long modifiedDay;

    /**
     * 收货人的详细地址
     */
    // @Transient
    public String receiverAddress;

    /**
     * 收货人的所在城市
     */
    @Transient
    public String receiverCity;

    /**
     * 收货人的所在地区
     */
    @Transient
    public String receiverDistrict;

    /**
     * 收货人的手机号码， （如receiverMobile为空，取receiverPhone内容）
     */
    // @Transient
    @Index(name = "mobile")
    public String receiverMobile;

    /**
     * 收货人的姓名
     */
    @Transient
    public String receiverName;

    /**
     * 收货人的所在省份
     */
    @Transient
    public String receiverState;

    /**
     * 收货人的邮编
     */
    @Transient
    public String receiverZip;

    /**
     * 5.15 新增两个字段
     */
    @CodeNoUpdate
//    @Transient
    @Column(columnDefinition = "varchar(31) default ''")
    public String buyerArea = "";

    /**
     * 收货人的电话号码
     */
//    @Transient
    @Column(columnDefinition = "varchar(63) default ''")
    public String receiverPhone;

    public TradeDisplay() {

    }

    public TradeDisplay(Long userId, Long ts, com.taobao.api.domain.Trade trade) {
        this(userId, ts, trade, true);
    }

    public TradeDisplay(Long userId, Long ts, com.taobao.api.domain.Trade trade, boolean isOldCustomer) {

        this.userId = userId;
        this.ts = ts;
        this.tid = trade.getTid();

//        log.info("[trade status:]" + trade.getStatus());
//        log.info("[trade status:]" + trade.getStatus());
        this.status = Status.tradestatus2int(trade.getStatus());
        this.buyerNick = trade.getBuyerNick();

        this.num = trade.getNum() == null ? 0 : trade.getNum().intValue();
        this.tradeFrom = Status.tradefrom2int(trade.getTradeFrom());

        this.payment = CommonUtils.String2Double(trade.getPayment());
        this.postFee = CommonUtils.String2Double(trade.getPostFee());
        this.price = CommonUtils.String2Double(trade.getPrice());
        this.receivedPayment = CommonUtils.String2Double(trade.getReceivedPayment());
        this.totalFee = CommonUtils.String2Double(trade.getTotalFee());

        this.created = trade.getCreated() == null ? 0 : trade.getCreated().getTime();
        this.payTime = trade.getPayTime() == null ? 0 : trade.getPayTime().getTime();
        this.consignTime = trade.getConsignTime() == null ? 0 : trade.getConsignTime().getTime();
        this.endTime = trade.getEndTime() == null ? 0 : trade.getEndTime().getTime();
        this.modified = trade.getModified() == null ? 0 : trade.getModified().getTime();

        this.createdDay = this.created == 0 ? 0 : DateUtil.formDailyTimestamp(this.created);
        this.payTimeDay = this.payTime == 0 ? 0 : DateUtil.formDailyTimestamp(this.payTime);
        this.consignTimeDay = this.consignTime == 0 ? 0 : DateUtil.formDailyTimestamp(this.consignTime);
        this.endTimeDay = this.endTime == 0 ? 0 : DateUtil.formDailyTimestamp(this.endTime);
        this.modifiedDay = this.modified == 0 ? 0 : DateUtil.formDailyTimestamp(this.modified);

        this.receiverAddress = trade.getReceiverAddress();
        this.receiverCity = trade.getReceiverCity();
        this.receiverDistrict = trade.getReceiverDistrict();
        this.receiverMobile = trade.getReceiverMobile();
        this.receiverName = trade.getReceiverName();
        this.receiverPhone = trade.getReceiverPhone();
        this.receiverState = trade.getReceiverState();
        this.receiverZip = trade.getReceiverZip();

        this.buyerAlipayNo = trade.getBuyerAlipayNo();
        this.buyerArea = trade.getBuyerArea();
        // TODO add the buyer alipay id...
        if (StringUtils.isEmpty(this.receiverMobile)) {
            this.receiverMobile = trade.getReceiverPhone();
        }
        this.receiverPhone = trade.getReceiverPhone();
    }

    public TradeDisplay(ResultSet rs) throws SQLException {
        this.tid = rs.getLong(1);
        this.userId = rs.getLong(2);
        this.ts = rs.getLong(3);
        this.status = rs.getInt(4);
        this.buyerNick = rs.getString(5);
        this.num = rs.getInt(6);
        this.tradeFrom = rs.getInt(7);
        this.payment = rs.getDouble(8);
        this.postFee = rs.getDouble(9);
        this.price = rs.getDouble(10);
        this.receivedPayment = rs.getDouble(11);
        this.totalFee = rs.getDouble(12);
        this.created = rs.getLong(13);
        this.payTime = rs.getLong(14);
        this.consignTime = rs.getLong(15);
        this.endTime = rs.getLong(16);
        this.modified = rs.getLong(17);
        this.createdDay = rs.getLong(18);
        this.payTimeDay = rs.getLong(19);
        this.consignTimeDay = rs.getLong(20);
        this.endTimeDay = rs.getLong(21);
        this.modifiedDay = rs.getLong(22);
        this.receiverAddress = rs.getString(23);
        this.receiverMobile = rs.getString(24);
        this.buyerAlipayNo = rs.getString(25);
        this.buyerArea = rs.getString(26);
        this.receiverPhone = rs.getString(27);
    }

    public void appendTradeLine(StringBuilder sb) {
        sb.append(this.tid);
        sb.append(',');
        sb.append(this.userId);
        sb.append(',');
        sb.append(this.ts);
        sb.append(',');
        sb.append(this.status);
        sb.append(',');
        sb.append(this.buyerNick);
        sb.append(',');
        sb.append(this.num);
        sb.append(',');
        sb.append(this.tradeFrom);
        sb.append(',');
        sb.append(this.payment);
        sb.append(',');
        sb.append(this.postFee);
        sb.append(',');
        sb.append(this.price);
        sb.append(',');
        sb.append(this.receivedPayment);
        sb.append(',');
        sb.append(this.totalFee);
        sb.append(',');
        sb.append(this.created);
        sb.append(',');
        sb.append(this.payTime);
        sb.append(',');
        sb.append(this.consignTime);
        sb.append(',');
        sb.append(this.endTime);
        sb.append(',');
        sb.append(this.modified);
        sb.append(',');
        sb.append(this.createdDay);
        sb.append(',');
        sb.append(this.payTimeDay);
        sb.append(',');
        sb.append(this.consignTimeDay);
        sb.append(',');
        sb.append(this.endTimeDay);
        sb.append(',');
        sb.append(this.modifiedDay);
        sb.append(',');
        sb.append(this.receiverAddress);
        sb.append(',');
        sb.append(this.receiverMobile);
        sb.append(',');
        sb.append(this.buyerAlipayNo);
        sb.append(',');
        sb.append(this.buyerArea);
        sb.append(',');
        sb.append(this.receiverPhone);

        sb.append("\n");

    }

    public void appendInsertLine(StringBuilder sb) {
        sb.append('(');
        sb.append(this.tid);
        sb.append(',');
        sb.append(this.userId);
        sb.append(',');
        sb.append(this.ts);
        sb.append(',');
        sb.append(this.status);
        sb.append(',');
        sb.append("'" + CommonUtils.escapeSQL(this.buyerNick) + "'");
        sb.append(',');
        sb.append(this.num);
        sb.append(',');
        sb.append(this.tradeFrom);
        sb.append(',');
        sb.append(this.payment);
        sb.append(',');
        sb.append(this.postFee);
        sb.append(',');
        sb.append(this.price);
        sb.append(',');
        sb.append(this.receivedPayment);
        sb.append(',');
        sb.append(this.totalFee);
        sb.append(',');
        sb.append(this.created);
        sb.append(',');
        sb.append(this.payTime);
        sb.append(',');
        sb.append(this.consignTime);
        sb.append(',');
        sb.append(this.endTime);
        sb.append(',');
        sb.append(this.modified);
        sb.append(',');
        sb.append(this.createdDay);
        sb.append(',');
        sb.append(this.payTimeDay);
        sb.append(',');
        sb.append(this.consignTimeDay);
        sb.append(',');
        sb.append(this.endTimeDay);
        sb.append(',');
        sb.append(this.modifiedDay);
        sb.append(',');
        sb.append("'" + CommonUtils.escapeSQL(this.receiverAddress) + "'");
        sb.append(',');
        sb.append("'" + CommonUtils.escapeSQL(this.receiverMobile) + "'");
        sb.append(',');
        sb.append("'" + CommonUtils.escapeSQL(this.buyerAlipayNo) + "'");
        sb.append(',');
        sb.append("'" + CommonUtils.escapeSQL(this.buyerArea) + "'");
        sb.append(',');
        sb.append("'" + CommonUtils.escapeSQL(this.receiverPhone) + "'");
        sb.append(')');

    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getIdColumn() {
        return "tid";
    }

    @Override
    public void setId(Long id) {
        this.tid = id;
    }

    public static String genShardQuery(String query, String key) {
        query = query.replaceAll("%s", "~~");
        query = query.replaceAll("%", "##");
        query = query.replaceAll("~~", "%s");

        String formQuery = String.format(query, key);
        return formQuery.replaceAll("##", "%");
    }

    public static String genShardQuery(String query, Long userId) {
        if (Play.mode.isDev()) {
            return StringUtils.EMPTY;
        }

        return genShardQuery(query, String.valueOf(DBBuilder.genUserIdHashKey(userId)));
    }

    static String EXIST_ID_QUERY = "select tid from `trade_display_%s` where tid  = ?";

    public static long findExistId(Long userId, Long tid) {
        return dp.singleLongQuery(genShardQuery(EXIST_ID_QUERY, userId), tid);
    }

    static String MIN_TID_QUERY = "select min(tid) from `trade_display_%s`";

    static String insertSQL = "insert into `trade_display_%s`(`tid`,`userId`,`ts`,`status`,`buyerNick`,`num`,`tradeFrom`"
            +
            ",`payment`,`postFee`,`price`,`receivedPayment`,`totalFee`,`created`,`payTime`,`consignTime`" +
            ",`endTime`,`modified`,`createdDay`,`payTimeDay`,`consignTimeDay`,`endTimeDay`," +
            "`modifiedDay`,`receiverAddress`,`receiverMobile`,`buyerAlipayNo`,`buyerArea`,`receiverPhone`) " +
            "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public boolean rawInsert() {

        if (this.tid == 0L) {
            long minTid = dp.singleLongQuery(genShardQuery(MIN_TID_QUERY, this.userId));
            if (minTid > 0L) {
                tid = -minTid;
            } else {
                tid = minTid - 2L;
            }
        }

        long id = dp.insert(genShardQuery(insertSQL, this.userId), this.tid, this.userId, this.ts, this.status,
                this.buyerNick, this.num, this.tradeFrom, this.payment, this.postFee, this.price, this.receivedPayment,
                this.totalFee, this.created, this.payTime, this.consignTime, this.endTime, this.modified,
                this.createdDay, this.payTimeDay, this.consignTimeDay, this.endTimeDay, this.modifiedDay,
                this.receiverAddress, this.receiverMobile, this.buyerAlipayNo, this.buyerArea, this.receiverPhone);

        if (id != 0L) {
            new TradeReceiver(this).jdbcSave();
            return true;
        } else {
            return false;
        }
    }

    static String insertOnDupKeyUpdateSQL = insertSQL
            + " on duplicate key update `status` = ?, `payment` = ?, `postFee` = ?, `price` = ?, `receivedPayment` = ?, "
            +
            "`totalFee` = ?, `created` = ?, `payTime` = ?, `consignTime` = ?, `endTime` = ?, `modified` = ?, "
            +
            "`createdDay` = ?, `payTimeDay` = ?, `consignTimeDay` = ?, `endTimeDay` = ?, "
            +
            "`modifiedDay` = ?, `receiverAddress` = ?, `receiverMobile` = ?, `buyerAlipayNo` = ?, `buyerArea` = ?,`receiverPhone` = ?";

    public boolean insertOnDupKeyUpdate() {
        long id = dp.insert(false, genShardQuery(insertOnDupKeyUpdateSQL, this.userId), this.tid, this.userId, this.ts,
                this.status, this.buyerNick, this.num, this.tradeFrom, this.payment, this.postFee, this.price,
                this.receivedPayment, this.totalFee, this.created, this.payTime, this.consignTime, this.endTime,
                this.modified, this.createdDay, this.payTimeDay, this.consignTimeDay, this.endTimeDay,
                this.modifiedDay, this.receiverAddress, this.receiverMobile, this.buyerAlipayNo, this.buyerArea,
                this.receiverPhone, this.status, this.payment, this.postFee, this.price, this.receivedPayment,
                this.totalFee, this.created, this.payTime, this.consignTime, this.endTime, this.modified,
                this.createdDay, this.payTimeDay, this.consignTimeDay, this.endTimeDay, this.modifiedDay,
                this.receiverAddress, this.receiverMobile, this.buyerAlipayNo, this.buyerArea, this.receiverPhone);
        if (id != 0L) {
            new TradeReceiver(this).jdbcSave();
            return true;
        } else {
            return false;
        }
    }

    public static final String updateSQL = "update `trade_display_%s` set `status` = ?, `payment` = ?, "
            +
            "`postFee` = ?,`price` = ?, `receivedPayment` = ?, `totalFee` = ?, `created` = ?, `payTime` = ?,"
            +
            "`consignTime` = ?,"
            + "`endTime` = ?, `modified` = ?, `createdDay` = ?, `payTimeDay` = ?,`consignTimeDay` = ?, "
            +
            "`endTimeDay` = ?,`modifiedDay` = ?, `receiverAddress` = ?, `receiverMobile` = ?, `buyerAlipayNo` = ?, `buyerArea` = ?,`receiverPhone` = ?  where `tid` = ? ";

    public boolean rawUpdate() {
        long updateNum = dp.insert(genShardQuery(updateSQL, this.userId), this.status, this.payment, this.postFee,
                this.price, this.receivedPayment, this.totalFee, this.created, this.payTime, this.consignTime,
                this.endTime, this.modified, this.createdDay, this.payTimeDay, this.consignTimeDay, this.endTimeDay,
                this.modifiedDay, this.receiverAddress, this.receiverMobile, this.buyerAlipayNo, this.buyerArea,
                this.receiverPhone, this.getId());

        if (updateNum > 0L) {
            // log.info("Update Order Display OK:" + tid);
            return true;
        } else {
            // log.warn("Update Fails... for :" + tid);
            return false;
        }
    }

    @Override
    public boolean jdbcSave() {

        try {

            return insertOnDupKeyUpdate();

            // long existdId = findExistId(this.tid);
            //
            // if (existdId == 0L) {
            // return this.rawInsert();
            // } else {
            // return this.rawUpdate();
            // }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getIdName() {
        return "tid";
    }

    @Override
    public Long getId() {
        return tid;
    }

    @Override
    public void _save() {
        throw new UnsupportedOperationException("No Save Method for this model...");
    }

    public Long getPayTime() {
        return payTime;
    }

    public void setPayTime(Long payTime) {
        this.payTime = payTime;
    }

    public Long getPayTimeDay() {
        return payTimeDay;
    }

    public void setPayTimeDay(Long payTimeDay) {
        this.payTimeDay = payTimeDay;
    }

    @Override
    public String getTableHashKey() {
        // TODO Auto-generated method stub
        return null;
    }

    public static boolean isRecentCreated(Trade trade) {
        if (trade == null) {
            return false;
        }

//        log.info("not create status ");

        if (!isCreateStatus(trade)) {
            return false;
        }

//        log.info("[created time :]"+DateUtil.formDateForLog(create2.getti)));

        Date created2 = trade.getCreated();
        if (created2 == null || System.currentTimeMillis() - created2.getTime() > 5 * DateUtil.ONE_MINUTE_MILLIS) {
            return false;
        }

        return true;
    }

    public static boolean isCreateStatus(Trade trade) {
        if (trade == null) {
            return false;
        }
        if (TRADE_STATUS.TRADE_NO_CREATE_PAY.name().equals(trade.getStatus())) {
            return true;
        }
        if (TRADE_STATUS.WAIT_BUYER_PAY.name().equals(trade.getStatus())) {
            return true;
        }
        return false;
    }

    public int getTradeFrom() {
        return tradeFrom;
    }

    public void setTradeFrom(int tradeFrom) {
        this.tradeFrom = tradeFrom;
    }

    public TRADE_FROM genFromPlatfrom() {
        return TRADE_FROM.values()[this.tradeFrom];
    }

    public String getBuyerArea() {
        return buyerArea;
    }

    public void setBuyerArea(String buyerArea) {
        this.buyerArea = buyerArea;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public String getReceiverCity() {
        return receiverCity;
    }

    public void setReceiverCity(String receiverCity) {
        this.receiverCity = receiverCity;
    }

    public Long getTid() {
        return tid;
    }

    public void setTid(Long tid) {
        this.tid = tid;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getBuyerNick() {
        return buyerNick;
    }

    public void setBuyerNick(String buyerNick) {
        this.buyerNick = buyerNick;
    }

    public String getBuyerAlipayNo() {
        return buyerAlipayNo;
    }

    public void setBuyerAlipayNo(String buyerAlipayNo) {
        this.buyerAlipayNo = buyerAlipayNo;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public double getPayment() {
        return payment;
    }

    public void setPayment(double payment) {
        this.payment = payment;
    }

    public double getPostFee() {
        return postFee;
    }

    public void setPostFee(double postFee) {
        this.postFee = postFee;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getReceivedPayment() {
        return receivedPayment;
    }

    public void setReceivedPayment(double receivedPayment) {
        this.receivedPayment = receivedPayment;
    }

    public double getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(double totalFee) {
        this.totalFee = totalFee;
    }

    public Long getConsignTime() {
        return consignTime;
    }

    public void setConsignTime(Long consignTime) {
        this.consignTime = consignTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Long getModified() {
        return modified;
    }

    public void setModified(Long modified) {
        this.modified = modified;
    }

    public Long getCreatedDay() {
        return createdDay;
    }

    public void setCreatedDay(Long createdDay) {
        this.createdDay = createdDay;
    }

    public Long getConsignTimeDay() {
        return consignTimeDay;
    }

    public void setConsignTimeDay(Long consignTimeDay) {
        this.consignTimeDay = consignTimeDay;
    }

    public Long getEndTimeDay() {
        return endTimeDay;
    }

    public void setEndTimeDay(Long endTimeDay) {
        this.endTimeDay = endTimeDay;
    }

    public Long getModifiedDay() {
        return modifiedDay;
    }

    public void setModifiedDay(Long modifiedDay) {
        this.modifiedDay = modifiedDay;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public String getReceiverDistrict() {
        return receiverDistrict;
    }

    public void setReceiverDistrict(String receiverDistrict) {
        this.receiverDistrict = receiverDistrict;
    }

    public String getReceiverMobile() {
        return receiverMobile;
    }

    public void setReceiverMobile(String receiverMobile) {
        this.receiverMobile = receiverMobile;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverState() {
        return receiverState;
    }

    public void setReceiverState(String receiverState) {
        this.receiverState = receiverState;
    }

    public String getReceiverZip() {
        return receiverZip;
    }

    public void setReceiverZip(String receiverZip) {
        this.receiverZip = receiverZip;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public IpDataBean genIpDataBean() {
        String area = this.buyerArea;
        if (StringUtils.isEmpty(area)) {
            area = this.receiverCity + this.receiverAddress;
        }
        String country = TBIpApi.trimCountry(area);
        String province = TBIpApi.trimProvince(area);
        String city = TBIpApi.trimCity(area);
        String isp = TBIpApi.trimIsp(area);
        IpDataBean bean = new IpDataBean(country, city, province, null, isp);
        return bean;
    }

    @Transient
    Set<Long> numIids = null;

    public Set<Long> ensureNumIids() {
        if (numIids != null) {
            return numIids;
        }
        numIids = OrderDisplayDao.findNumIidsByTid(this);
        return numIids;
    }

}
