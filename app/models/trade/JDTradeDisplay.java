package models.trade;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.DBBuilder;
import transaction.DBBuilder.DataSrc;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.NumberUtil;
import com.jd.open.api.sdk.domain.order.OrderSearchInfo;
import com.jd.open.api.sdk.domain.order.UserInfo;

/**
 * 交易结构
 * 
 */
@Entity(name = JDTradeDisplay.TABLE_NAME)
public class JDTradeDisplay extends GenericModel implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(JDTradeDisplay.class);

    public static final String TAG = "JDTradeDisplay";

    public static final String TABLE_NAME = "jd_trade_display_";

    public static final JDTradeDisplay EMPTY = new JDTradeDisplay();

    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static DBDispatcher dp = new DBDispatcher(DataSrc.QUOTA, EMPTY);

    /**
     * 交易编号 (父订单的交易编号) 该变量有可能为负，但不为0
     */
    @Id
    @Index(name = "tid")
    @CodeNoUpdate
    public Long tid;

    /**
     * Trade对应的卖家
     */
    @CodeNoUpdate
    @Index(name = "vender_id")
    public Long venderId;

    /**
     * 订单总金额
     */
    public Double totalPrice;

    /**
     * 用户应付金额
     */
    public Double payment;

    public Double freightPrice;

    public Double sellerDiscount;

    @CodeNoUpdate
    public Long ts;

    /**
     * 订单状态（英文）
     * 多订单状态可以用逗号隔开( WAIT_SELLER_STOCK_OUT 等待出库 ,
     * SEND_TO_DISTRIBUTION_CENER 发往配送中心(只适用于LBP,SOPL商家),
     * DISTRIBUTION_CENTER_RECEIVED 配送中心已收货(只适用于LBP,SOPL商家)，
     * WAIT_GOODS_RECEIVE_CONFIRM 等待确认收货,
     * RECEIPTS_CONFIRM 收款确认(服务完成)(只适用于LBP,SOPL商家)
     * FINISHED_L 完成,
     * TRADE_CANCELED 取消)
     * LOCKED  已锁定（锁定的订单不返回订单详情）
     */
    public enum StatusType {
        WAIT_SELLER_STOCK_OUT, SEND_TO_DISTRIBUTION_CENER, DISTRIBUTION_CENTER_RECEIVED, WAIT_GOODS_RECEIVE_CONFIRM, RECEIPTS_CONFIRM, FINISHED_L, TRADE_CANCELED, LOCKED
    }

    @Enumerated(EnumType.STRING)
    public StatusType status;

    /**
     * 订单状态说明（中文）
     */
    public String stateRemark;

    /**
     * 送货（日期）类型（1-只工作日送货(双休日、假日不用送);2-只双休日、假日送货(工作日不用送);3-工作日、双休日与假日均可送货;其他值-返回“任意时间”）
     */
    public String deliveryType;

    /**
     * 发票信息 “invoice_info: 不需要开具发票”下无需开具发票；其它返回值请正常开具发票
     */
    public String invoiceInfo;

    /**
     * 买家下单时订单备注
     */
    public String remark;

    /**
     * 下单时间
     */
    @Index(name = "created")
    public Long startTime = NumberUtil.DEFAULT_LONG;

    /**
     * 付款时间。jd没有该字段
     */
    public Long payTime;

    /**
     * 结单时间  如返回信息为“0001-01-01 00:00:00”和“1970-01-01 00:00:00”，可认为此订单为未完成状态。
     */
    public Long endTime;

    /**
     * UserInfo 里收货人信息
     */
    public String fullname;

    public String full_address;

    public String telephone;

    public String mobile;
    
    /**
     * 买家账号，对应jd的pin字段
     */
    public String buyer;

    /**
     * 换货订单标识 0:不是换货订单,1:换货订单(默认不返回)
     */
    @Transient
    public int returnOrder;

    public JDTradeDisplay() {

    }

    public JDTradeDisplay(Long venderId, Long ts, OrderSearchInfo trade) {
        this(venderId, ts, trade, true);
    }

    public JDTradeDisplay(Long venderId, Long ts, OrderSearchInfo trade, boolean isOldCustomer) {
        this.venderId = venderId;
        this.ts = ts;
        this.tid = CommonUtils.String2Long(trade.getOrderId());

        this.totalPrice = CommonUtils.String2Double(trade.getOrderTotalPrice());
        this.payment = CommonUtils.String2Double(trade.getOrderPayment());
        this.freightPrice = CommonUtils.String2Double(trade.getFreightPrice());
        this.sellerDiscount = CommonUtils.String2Double(trade.getSellerDiscount());
        this.status = StatusType.valueOf(trade.getOrderState());
        this.stateRemark = trade.getOrderStateRemark();
        this.deliveryType = trade.getDeliveryType();

        this.invoiceInfo = trade.getInvoiceInfo();
        this.remark = trade.getOrderRemark();
//        this.buyer = trade.get
        Date startDate = CommonUtils.String2Date(trade.getOrderStartTime());
        if (startDate != null) {
            this.startTime = startDate.getTime();
        }
        Date endDate = CommonUtils.String2Date(trade.getOrderEndTime());
        if (endDate != null) {
            this.endTime = endDate.getTime();
        }

        UserInfo info = trade.getConsigneeInfo();
        if (info != null) {
            this.full_address = info.getProvince() + "-" + info.getCity() + "-" + info.getCounty() + "-"
                    + info.getFullAddress();
            this.fullname = info.getFullname();
            this.telephone = info.getTelephone();
            this.mobile = info.getMobile();
        }
        this.ts = System.currentTimeMillis();
    }

    public JDTradeDisplay(Long tid, Long venderId, Double totalPrice, Double payment, Double freightPrice,
            Double sellerDiscount, StatusType status, String stateRemark, String deliveryType, String invoiceInfo,
            String remark, Long startTime, Long payTime, Long endTime, String fullname, String full_address,
            String telephone, String mobile, String buyer) {
        super();
        this.tid = tid;
        this.venderId = venderId;
        this.totalPrice = totalPrice;
        this.payment = payment;
        this.freightPrice = freightPrice;
        this.sellerDiscount = sellerDiscount;
        this.status = status;
        this.stateRemark = stateRemark;
        this.deliveryType = deliveryType;
        this.invoiceInfo = invoiceInfo;
        this.remark = remark;
        this.startTime = startTime;
        this.payTime = payTime;
        this.endTime = endTime;
        this.fullname = fullname;
        this.full_address = full_address;
        this.telephone = telephone;
        this.mobile = mobile;
        this.buyer = buyer;
        this.ts = System.currentTimeMillis();
    }

    public JDTradeDisplay(ResultSet rs) throws SQLException {
        this.tid = rs.getLong(1);
        this.venderId = rs.getLong(2);
        this.totalPrice = rs.getDouble(3);
        this.payment = rs.getDouble(4);
        this.freightPrice = rs.getDouble(5);
        this.sellerDiscount = rs.getDouble(6);
        this.ts = rs.getLong(7);
        this.status = StatusType.valueOf(rs.getString(8));
        this.stateRemark = rs.getString(9);
        this.deliveryType = rs.getString(10);
        this.invoiceInfo = rs.getString(11);
        this.remark = rs.getString(12);
        this.startTime = rs.getLong(13);
        this.payTime = rs.getLong(14);
        this.endTime = rs.getLong(15);
        this.fullname = rs.getString(16);
        this.full_address = rs.getString(17);
        this.telephone = rs.getString(18);
        this.mobile = rs.getString(19);
        this.buyer = rs.getString(20);
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

    public static String genShardQuery(String query, Long venderId) {
        return genShardQuery(query, String.valueOf(DBBuilder.genUserIdHashKey(venderId)));
    }

    static String EXIST_ID_QUERY = "select tid from `jd_trade_display_%s` where tid  = ?";

    public static long findExistId(Long userId, Long tid) {
        return dp.singleLongQuery(genShardQuery(EXIST_ID_QUERY, userId), tid);
    }

    static String MIN_TID_QUERY = "select min(tid) from `jd_trade_display_%s`";

    static String insertSQL = "insert into `jd_trade_display_%s`(`tid`,`venderId`,`totalPrice`,`payment`,`freightPrice`,`sellerDiscount`,`ts`,`status`,`stateRemark`,`deliveryType`,`invoiceInfo`,`remark`,`startTime`,`payTime`,`endTime`,`fullname`,`full_address`,`telephone`,`mobile`, `buyer`) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public boolean rawInsert() {

        long id = dp.insert(genShardQuery(insertSQL, venderId), this.tid, this.venderId, this.totalPrice, this.payment,
                this.freightPrice, this.sellerDiscount, this.ts, this.status, this.stateRemark, this.deliveryType,
                this.invoiceInfo, this.remark, this.startTime, this.payTime, this.endTime, this.fullname,
                this.full_address, this.telephone, this.mobile, this.buyer);

        if (id != 0L) {
            // new TradeReceiver(this).jdbcSave();
            return true;
        } else {
            return false;
        }
    }

    static String insertOnDupKeyUpdateSQL = insertSQL
            + " on duplicate key update `totalPrice` = ?, `payment` = ?, `freightPrice` = ?, `sellerDiscount` = ?, `status` = ?, `stateRemark` = ?, `deliveryType` = ?, `invoiceInfo` = ?, `remark` = ?, `startTime` = ?, `payTime` = ?, `endTime` = ?, `fullname` = ?, `full_address` = ?, `telephone` = ?, `mobile` = ?, `buyer` = ? ";

    public boolean insertOnDupKeyUpdate() {
        long id = dp.insert(genShardQuery(insertSQL, venderId), this.tid, this.venderId, this.totalPrice, this.payment,
                this.freightPrice, this.sellerDiscount, this.ts, this.status, this.stateRemark, this.deliveryType,
                this.invoiceInfo, this.remark, this.startTime, this.payTime, this.endTime, this.fullname,
                this.full_address, this.telephone, this.mobile, this.buyer, this.totalPrice, this.payment, this.freightPrice,
                this.sellerDiscount, this.status, this.stateRemark, this.deliveryType, this.invoiceInfo, this.remark,
                this.startTime, this.payTime, this.endTime, this.fullname, this.full_address, this.telephone,
                this.mobile, this.buyer);

        if (id != 0L) {
            // new TradeReceiver(this).jdbcSave();
            return true;
        } else {
            return false;
        }
    }

    public static final String updateSQL = "update `jd_trade_display_%s` set `totalPrice` = ?, `payment` = ?, `freightPrice` = ?, `sellerDiscount` = ?, `status` = ?, `stateRemark` = ?, `deliveryType` = ?, `invoiceInfo` = ?, `remark` = ?, `startTime` = ?, `payTime` = ?, `endTime` = ?, `fullname` = ?, `full_address` = ?, `telephone` = ?, `mobile` = ?, `buyer` = ? where `tid` = ? ";

    public boolean rawUpdate() {
        long updateNum = dp.insert(genShardQuery(updateSQL, this.venderId), this.totalPrice, this.payment,
                this.freightPrice, this.sellerDiscount, this.status, this.stateRemark, this.deliveryType,
                this.invoiceInfo, this.remark, this.startTime, this.payTime, this.endTime, this.fullname,
                this.full_address, this.telephone, this.mobile, this.buyer, this.getId());

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

    @Override
    public String getTableHashKey() {
        // TODO Auto-generated method stub
        return null;
    }

}
