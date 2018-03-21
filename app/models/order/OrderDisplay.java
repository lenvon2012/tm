
package models.order;

/**
 * 订单结构
 *
 */

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import models.Status;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.DBBuilder;
import transaction.DBBuilder.DataSrc;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;

@Entity(name = OrderDisplay.TABLE_NAME)
public class OrderDisplay extends GenericModel implements PolicySQLGenerator {

    @Transient
    public static final String TABLE_NAME = "order_display_";

    @Transient
    private static final Logger log = LoggerFactory.getLogger(OrderDisplay.class);

    @Transient
    public static final PolicySQLGenerator EMPTY = new OrderDisplay();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.QUOTA, EMPTY);

    public OrderDisplay() {
        super();
    }

    /**
     * Order对应的User
     */
    @Index(name = "userId")
    @CodeNoUpdate
    public Long userId;

    @CodeNoUpdate
    public Long ts;

    /**
     * Order的tid
     */

    @Index(name = "tid")
    @CodeNoUpdate
    public Long tid;

    /**
     * 子订单编号
     */
    @Id
    @Index(name = "oid")
    @CodeNoUpdate
    public Long oid;

    /**
     * 订单状态
     */
    public int status;

    /**
     * 商品数字ID
     */
    @CodeNoUpdate
    public Long numIid;

    /**
     * 交易商品对应的类目ID
     */
    @CodeNoUpdate
    public Long cid;

    /**
     * 买家昵称
     */
    public String buyerNick;

    /**
     * 商品标题
     */
//    @Transient
    public String title;

    /**
     * 购买数量。取值范围:大于零的整数
     */
    @CodeNoUpdate
    @Column(columnDefinition = "int default -1")
    public Integer num;

    /**
     * 商品图片的绝对路径
     */
//    @Transient
    public String picPath;

    /**
     * 买家实付金额（单笔子订单时包含物流费用，多笔子订单时不包含物流费用
     * ，付款后这个价格不会变化）。精确到2位小数，单位:元。如:200.07，表示:200元7分。规则为：total_fee*num -
     * audjust_fee - discount_fee - refund_fee
     */
    @Column(columnDefinition = "double default -1")
    public double payment;

    /**
     * 商品价格。精确到2位小数;单位:元。如:200.07，表示:200元7分
     */
    @Column(columnDefinition = "double default -1")
    public double price;

    /**
     * 应付金额（商品价格 * 商品数量 + 手工调整金额 - 订单优惠金额）。精确到2位小数;单位:元。如:200.07，表示:200元7分
     */
    @Column(columnDefinition = "double default -1")
    public double totalFee;

    /********************************* 新增相关时间字段 *********************/

    /**
     * 交易创建时间。格式: yyyy- MM -dd HH:mm:ss
     */
    @Index(name = "created")
    public Long created;

    /**
     * 付款时间。格式: yyyy- MM -dd HH:mm:ss
     */
    public Long payTime;

    /**
     * 卖家发货时间。格式: yyyy- MM -dd HH:mm:ss
     */
    public Long consignTime;

    /**
     * 交易结束时间。交易成功时间(更新交易状态为成功的同时更新)/确认收货时间或者交易关闭时间 。格式:yyyy -MM - dd HH:mm:ss
     */
    public Long endTime;

    /**
     * 交易修改时间(用户对订单的任何修改都会更新此字段)。格式: yyyy- MM -dd HH:mm:ss
     */
    @Column(columnDefinition = "bigint default 0")
    public Long modified;

    @Index(name = "created_day")
    @Column(columnDefinition = "bigint default 0")
    public Long createdDay;

    @Index(name = "paytime_day")
    @Column(columnDefinition = "bigint default 0")
    public Long payTimeDay;

    @Column(columnDefinition = "bigint default 0")
    public Long consignTimeDay;

    @Index(name = "endtime_day")
    @Column(columnDefinition = "bigint default 0")
    public Long endTimeDay;

    public Long modifiedDay;

    /**
     * 买家是否评价
     */
    public Boolean buyerRate;

    /**
     * 卖家是否评价
     */
    public Boolean sellerRate;

    /**
     * 联系电话
     */
    public String phone;

    /**
     * 买家姓名
     */
    @Column(columnDefinition = "varchar(63)")
    public String receiverName;

    @Transient
    protected String buyerAlipayNo;

    @Transient
    public Long dispatchId;//催评分配专员ID

    @Transient
    public String groupName;//专员名称

    @Transient
    public String remark;//催评备注

    @Transient
    private String tidStr;
    
    @Transient
    private String oidStr;

    public OrderDisplay(Long userId, Long ts, com.taobao.api.domain.Trade trade, com.taobao.api.domain.Order order) {
        this(userId, ts, trade, order, true);
    }

    public OrderDisplay(Long userId, com.taobao.api.domain.Trade trade, com.taobao.api.domain.Order order) {
        this(userId, DateUtil.formCurrDate(), trade, order, true);
    }

    public OrderDisplay(Long userId, com.taobao.api.domain.Trade trade, com.taobao.api.domain.Order order,
            boolean isOldCustomer) {
        this(userId, DateUtil.formCurrDate(), trade, order, isOldCustomer);
    }

    public OrderDisplay(Long userId, Long ts, com.taobao.api.domain.Trade trade, com.taobao.api.domain.Order order,
            boolean isOldCustomer) {

        this.userId = userId;
        this.ts = ts;

        this.tid = trade.getTid();
        this.oid = order.getOid();
        this.status = Status.tradestatus2int(order.getStatus());

        this.numIid = order.getNumIid();
        this.cid = order.getCid();
        this.title = order.getTitle();
        this.num = order.getNum().intValue();
        this.picPath = order.getPicPath();

        this.payment = CommonUtils.String2Double(order.getPayment());
        this.price = CommonUtils.String2Double(order.getPrice());
        this.totalFee = CommonUtils.String2Double(order.getTotalFee());

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

        this.buyerRate = order.getBuyerRate();
        this.sellerRate = order.getSellerRate();
        this.buyerNick = trade.getBuyerNick();
        this.phone = trade.getReceiverMobile();
        if (StringUtils.isEmpty(this.phone)) {
            this.phone = trade.getReceiverPhone();
        }
        this.buyerAlipayNo = trade.getBuyerAlipayNo();
        this.receiverName = trade.getReceiverName();
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getIdColumn() {
        return "oid";
    }

    @Override
    public Long getId() {
        return oid;
    }

    @Override
    public void setId(Long id) {
        this.oid = id;

    }

    public static String genShardQuery(String query, String key) {
        query = query.replaceAll("%s", "~~");
        query = query.replaceAll("%", "##");
        query = query.replaceAll("~~", "%s");

        String formQuery = String.format(query, key);
        return formQuery.replaceAll("##", "%");
    }

    public static String genShardQuery(String query, Long userId) {
        return genShardQuery(query, String.valueOf(DBBuilder.genUserIdHashKey(userId)));
    }

    static String OID_QUERY = "select oid from order_display_%s where oid = ?";

    public static long findExistId(Long userId, long oid) {
        return dp.singleLongQuery(genShardQuery(OID_QUERY, userId), oid);
    }

    @Override
    public boolean jdbcSave() {

        try {

            return insertOnDupKeyUpdate();
//            long exist = dp.singleLongQuery(genShardQuery(OID_QUERY, userId), oid);
//
//            if (exist == 0L) {
//                return rawInsert();
//            } else {
//                return rawUpdate();
//            }

        } catch (Exception e) {
            log.warn(e.getMessage());
            return false;
        }
    }

    public boolean rawUpdate() {
        long updateNum = dp
                .insert(genShardQuery(
                        "update `order_display_%s` set `status` = ?, `buyerNick` = ?, `title` = ?, `num` = ?, `picPath` = ?, `payment` = ?, `price` = ?, `totalFee` = ?, `created` = ?, `payTime` = ?, `consignTime` = ?, `endTime` = ?, `modified` = ?, `createdDay` = ?, `payTimeDay` = ?, `consignTimeDay` = ?, `endTimeDay` = ?, `modifiedDay` = ?, `buyerRate` = ?, `sellerRate` = ?, `phone` = ?, `receiverName` = ? where `oid` = ? ",
                        this.userId), this.status, this.buyerNick, this.title, this.num, this.picPath, this.payment,
                        this.price, this.totalFee,
                        this.created, this.payTime, this.consignTime, this.endTime, this.modified, this.createdDay,
                        this.payTimeDay, this.consignTimeDay, this.endTimeDay, this.modifiedDay, this.buyerRate,
                        this.sellerRate, this.phone, this.receiverName, this.getId());

        if (updateNum > 0L) {
            // log.info("Update Order Display OK:" + oid);
            return true;
        } else {
            log.warn("Update Fails... for :" + oid);

            return false;
        }

    }

    public boolean rawInsert() {
        long id = dp
                .insert(genShardQuery(
                        "insert into `order_display_%s`(`userId`,`ts`,`tid`,`oid`,`status`,`numIid`,`cid`,`buyerNick`,`title`,`num`,`picPath`,"
                                + "`payment`,`price`,`totalFee`,`created`,`payTime`,`consignTime`,`endTime`,`modified`,`createdDay`,`payTimeDay`,"
                                + "`consignTimeDay`,`endTimeDay`,`modifiedDay`,`buyerRate`,`sellerRate`, `phone`, `receiverName`) "
                                + "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                        this.userId), this.userId, this.ts, this.tid, this.oid, this.status, this.numIid, this.cid,
                        this.buyerNick, this.title, this.num, this.picPath, this.payment, this.price, this.totalFee,
                        this.created, this.payTime,
                        this.consignTime, this.endTime, this.modified, this.createdDay, this.payTimeDay,
                        this.consignTimeDay, this.endTimeDay, this.modifiedDay, this.buyerRate, this.sellerRate,
                        this.phone, this.receiverName);

        if (id != 0L) {
            new OrderItem(this).rawInsertOnDupUpdate();
            // log.info("Raw Insert Order Display OK:" + oid);
            return true;
        } else {
            log.warn("Raw insert Fails... for :" + oid);

            return false;
        }

    }

    static String insertOnDupKeyUpdateSQL = "insert into `order_display_%s`(`userId`,`ts`,`tid`,`oid`,`status`,`numIid`,`cid`,`buyerNick`,`title`,`num`,`picPath`,`payment`,`price`,`totalFee`,`created`,`payTime`,`consignTime`,`endTime`,`modified`,`createdDay`,`payTimeDay`,`consignTimeDay`,`endTimeDay`,`modifiedDay`,`buyerRate`,`sellerRate`, `phone`, `receiverName`) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) on duplicate key"
            +
            " update `status` = ?, `buyerNick` = ?, `title` = ?, `num` = ?, `picPath` = ?, `payment` = ?, `price` = ?, `totalFee` = ?, `created` = ?, `payTime` = ?, `consignTime` = ?, `endTime` = ?, `modified` = ?, `createdDay` = ?, `payTimeDay` = ?, `consignTimeDay` = ?, `endTimeDay` = ?, `modifiedDay` = ?, `buyerRate` = ?, `sellerRate` = ?, `phone` = ?, `receiverName` = ?";

    public boolean insertOnDupKeyUpdate() {
        long id = dp.insert(genShardQuery(insertOnDupKeyUpdateSQL, this.userId), this.userId, this.ts, this.tid,
                this.oid, this.status, this.numIid, this.cid, this.buyerNick, this.title, this.num, this.picPath,
                this.payment, this.price, this.totalFee, this.created, this.payTime, this.consignTime, this.endTime,
                this.modified, this.createdDay, this.payTimeDay, this.consignTimeDay, this.endTimeDay,
                this.modifiedDay, this.buyerRate, this.sellerRate, this.phone, this.receiverName, this.status,
                this.buyerNick, this.title,
                this.num, this.picPath, this.payment, this.price, this.totalFee, this.created, this.payTime,
                this.consignTime, this.endTime, this.modified, this.createdDay, this.payTimeDay, this.consignTimeDay,
                this.endTimeDay, this.modifiedDay, this.buyerRate, this.sellerRate, this.phone, this.receiverName);

        if (id != 0L) {
            new OrderItem(this).rawInsertOnDupUpdate();
            // log.info("Raw Insert Order Display OK:" + oid);
            return true;
        } else {
            log.warn("Raw insert Fails... for :" + oid);

            return false;
        }
    }

    @Override
    public String getIdName() {
        return "oid";
    }

    @Override
    public void _save() {
        throw new UnsupportedOperationException("No Save Method for this model...");
    }

    public Long getNumIid() {
        return numIid;
    }

    public void setNumIid(Long numIid) {
        this.numIid = numIid;
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

    public Long getConsignTimeDay() {
        return consignTimeDay;
    }

    public void setConsignTimeDay(Long consignTimeDay) {
        this.consignTimeDay = consignTimeDay;
    }

    @Override
    public String getTableHashKey() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String toString() {
        return "OrderDisplay [userId=" + userId + ", tid=" + tid + ", oid=" + oid + ", status=" + status + ", numIid="
                + numIid + ", cid=" + cid + ", buyerNick=" + buyerNick + ", title=" + title + ", num=" + num
                + ", picPath=" + picPath + ", payment=" + payment + ", price=" + price + ", totalFee=" + totalFee
                + ", created=" + DateUtil.formDateForLog(created) + ", payTime=" + DateUtil.formDateForLog(payTime)
                + ", consignTime=" + DateUtil.formDateForLog(consignTime) + ", endTime=" + endTime + ", modified="
                + DateUtil.formDateForLog(modified) + ", createdDay=" + DateUtil.formDateForLog(createdDay)
                + ", payTimeDay=" + DateUtil.formDateForLog(payTimeDay) + ", consignTimeDay="
                + DateUtil.formDateForLog(consignTimeDay) + ", endTimeDay=" + DateUtil.formDateForLog(endTimeDay)
                + ", modifiedDay=" + modifiedDay + ", buyerRate=" + buyerRate + ", sellerRate=" + sellerRate
                + ", phone=" + phone + ", receiverName=" + receiverName + ", buyerAlipayNo=" + buyerAlipayNo + "]";
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

    public Long getTid() {
        return tid;
    }

    public void setTid(Long tid) {
        this.tid = tid;
    }

    public Long getOid() {
        return oid;
    }

    public void setOid(Long oid) {
        this.oid = oid;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Long getCid() {
        return cid;
    }

    public void setCid(Long cid) {
        this.cid = cid;
    }

    public String getBuyerNick() {
        return buyerNick;
    }

    public void setBuyerNick(String buyerNick) {
        this.buyerNick = buyerNick;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    public double getPayment() {
        return payment;
    }

    public void setPayment(double payment) {
        this.payment = payment;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(double totalFee) {
        this.totalFee = totalFee;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
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

    public Boolean getBuyerRate() {
        return buyerRate;
    }

    public void setBuyerRate(Boolean buyerRate) {
        this.buyerRate = buyerRate;
    }

    public Boolean getSellerRate() {
        return sellerRate;
    }

    public void setSellerRate(Boolean sellerRate) {
        this.sellerRate = sellerRate;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

	public String getTidStr() {
		return tidStr;
	}

	public void setTidStr(String tidStr) {
		this.tidStr = tidStr;
	}

	public String getOidStr() {
		return oidStr;
	}

	public void setOidStr(String oidStr) {
		this.oidStr = oidStr;
	}

	public OrderDisplay(ResultSet rs) throws SQLException {
        this.userId = rs.getLong(1);
        this.ts = rs.getLong(2);
        this.tid = rs.getLong(3);
        this.oid = rs.getLong(4);
        this.status = rs.getInt(5);
        this.numIid = rs.getLong(6);
        this.cid = rs.getLong(7);
        this.buyerNick = rs.getString(8);
        this.title = rs.getString(9);
        this.num = rs.getInt(10);
        this.picPath = rs.getString(11);
        this.payment = rs.getDouble(12);
        this.price = rs.getDouble(13);
        this.totalFee = rs.getDouble(14);
        this.created = rs.getLong(15);
        this.payTime = rs.getLong(16);
        this.consignTime = rs.getLong(17);
        this.endTime = rs.getLong(18);
        this.modified = rs.getLong(19);
        this.createdDay = rs.getLong(20);
        this.payTimeDay = rs.getLong(21);
        this.consignTimeDay = rs.getLong(22);
        this.endTimeDay = rs.getLong(23);
        this.modifiedDay = rs.getLong(24);
        this.buyerRate = rs.getBoolean(25);
        this.sellerRate = rs.getBoolean(26);
        this.phone = rs.getString(27);
        this.receiverName = rs.getString(28);
    }

    public void appendTradeLine(StringBuilder sb) {
        sb.append(this.userId);
        sb.append(',');
        sb.append(this.ts);
        sb.append(',');
        sb.append(this.tid);
        sb.append(',');
        sb.append(this.oid);
        sb.append(',');
        sb.append(this.status);
        sb.append(',');
        sb.append(this.numIid);
        sb.append(',');
        sb.append(this.cid);
        sb.append(',');
        sb.append(this.buyerNick);
        sb.append(',');
        sb.append(this.title);
        sb.append(',');
        sb.append(this.num);
        sb.append(',');
        sb.append(this.picPath);
        sb.append(',');
        sb.append(this.payment);
        sb.append(',');
        sb.append(this.price);
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
        sb.append(this.buyerRate);
        sb.append(',');
        sb.append(this.sellerRate);
        sb.append(',');
        sb.append(this.phone);
        sb.append(',');
        sb.append(this.receiverName);
        sb.append("\n");

    }

    public void appendInsertLine(StringBuilder sb) {
        sb.append('(');
        sb.append(this.userId);
        sb.append(',');
        sb.append(this.ts);
        sb.append(',');
        sb.append(this.tid);
        sb.append(',');
        sb.append(this.oid);
        sb.append(',');
        sb.append(this.status);
        sb.append(',');
        sb.append(this.numIid);
        sb.append(',');
        sb.append(this.cid);
        sb.append(',');
        sb.append("'" + CommonUtils.escapeSQL(this.buyerNick) + "'");
        sb.append(',');
        sb.append("'" + CommonUtils.escapeSQL(this.title) + "'");
        sb.append(',');
        sb.append(this.num);
        sb.append(',');
        sb.append("'" + CommonUtils.escapeSQL(this.picPath) + "'");
        sb.append(',');
        sb.append(this.payment);
        sb.append(',');
        sb.append(this.price);
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
        sb.append(this.buyerRate);
        sb.append(',');
        sb.append(this.sellerRate);
        sb.append(',');
        sb.append("'" + CommonUtils.escapeSQL(this.phone) + "'");
        sb.append(',');
        sb.append("'" + CommonUtils.escapeSQL(this.receiverName) + "'");
        sb.append(')');
    }

    public String getBuyerAlipayNo() {
        return buyerAlipayNo;
    }

    public void setBuyerAlipayNo(String buyerAlipayNo) {
        this.buyerAlipayNo = buyerAlipayNo;
    }





}
