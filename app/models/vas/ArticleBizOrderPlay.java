
package models.vas;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import utils.DateUtil;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.NumberUtil;
import com.taobao.api.domain.ArticleBizOrder;

@Entity(name = ArticleBizOrderPlay.TABLE_NAME)
public class ArticleBizOrderPlay extends GenericModel implements PolicySQLGenerator {

    private static final long serialVersionUID = 1L;

    @Transient
    private static final Logger log = LoggerFactory.getLogger(ArticleBizOrderPlay.class);

    @Transient
    public static final String TABLE_NAME = "article_biz_order";

    @Transient
    public static ArticleBizOrderPlay EMPTY = new ArticleBizOrderPlay();

    public long bizOrderId;// 订单号

    @Id
    @PolicySQLGenerator.CodeNoUpdate
    public long orderId;// 子订单号

    @Index(name = "nickname")
    public String nick;// 淘宝会员名

    public String articleName;// 应用名称

    public String articleCode;// 应用收费代码

    public String itemCode;// 收费项目代码

    @Index(name = "createTime")
    public long createTime;// 订单创建时间（订购时间）

    public String orderCycle;// 订购周期

    @Index(name = "orderCycleStart")
    public long orderCycleStart;// 订购周期开始时间

    @Index(name = "orderCycleEnd")
    public long orderCycleEnd;// 订购周期结束时间

    @Index(name = "bizType")
    public long bizType;// 订单类型，1=新订 2=续订 3=升级 4=后台赠送 5=后台自动续订

    public String getBizTypeStr() {
        switch ((int) this.bizType) {
            case 1:
                return "新订 ++";
            case 2:
                return "续订 --";
            case 3:
                return "升级 ||";
            default:
                return "4=后台赠送 5=后台自动续订";
        }

    }
    
    // 6=订单审核后生成订购关系（暂时用不到）

    public long fee;// 原价（单位为分）

    public long promFee;// 优惠（单位为分）

    public long refundFee;// 退款（单位为分；升级时，系统会将升级前老版本按照剩余订购天数退还剩余金额）

    @Index(name = "totalPayFee")
    public long totalPayFee;// 实付（单位为分）

    @Index(name = "level")
    public Long level;// 用户订购时的等级

    public int hour;

    int monthCycle;

    public ArticleBizOrderPlay() {
    }

    public ArticleBizOrderPlay(ArticleBizOrder order, long level) {
        this.bizOrderId = order.getBizOrderId();
        this.orderId = order.getOrderId();
        this.nick = order.getNick();
        this.articleName = order.getArticleName();
        this.articleCode = order.getArticleCode();
        this.itemCode = order.getItemCode();
        this.createTime = order.getCreate().getTime();
        this.orderCycle = order.getOrderCycle();
        this.orderCycleStart = order.getOrderCycleStart().getTime();
        this.orderCycleEnd = order.getOrderCycleEnd().getTime();
        this.bizType = order.getBizType();
        this.fee = CommonUtils.String2long(order.getFee());
        this.promFee = CommonUtils.String2long(order.getPromFee());
        this.refundFee = CommonUtils.String2long(order.getRefundFee());
        this.totalPayFee = CommonUtils.String2long(order.getTotalPayFee());
        this.hour = DateUtil.getHourOfDay(this.createTime);
        this.level = level;

        if (orderCycle.startsWith("1个月")) {
            this.monthCycle = 1;
        } else if (orderCycle.startsWith("3个月")) {
            this.monthCycle = 3;
        } else if (orderCycle.startsWith("6个月")) {
            this.monthCycle = 6;
        } else if (orderCycle.startsWith("12个月")) {
            this.monthCycle = 12;
        } else {
            this.monthCycle = 0;
        }
    }

    public ArticleBizOrderPlay(ResultSet rs) throws SQLException {
        this.bizOrderId = rs.getLong(1);
        this.orderId = rs.getLong(2);
        this.nick = rs.getString(3);
        this.articleName = rs.getString(4);
        this.articleCode = rs.getString(5);
        this.itemCode = rs.getString(6);
        this.createTime = rs.getLong(7);
        this.orderCycle = rs.getString(8);
        this.orderCycleStart = rs.getLong(9);
        this.orderCycleEnd = rs.getLong(10);
        this.bizType = rs.getLong(11);
        this.fee = rs.getLong(12);
        this.promFee = rs.getLong(13);
        this.refundFee = rs.getLong(14);
        this.totalPayFee = rs.getLong(15);
        this.level = rs.getLong(16);
        this.hour = rs.getInt(17);
        this.monthCycle = rs.getInt(18);
    }

    public long getBizOrderId() {
        return bizOrderId;
    }

    public void setBizOrderId(long bizOrderId) {
        this.bizOrderId = bizOrderId;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getArticleName() {
        return articleName;
    }

    public void setArticleName(String articleName) {
        this.articleName = articleName;
    }

    public String getArticleCode() {
        return articleCode;
    }

    public void setArticleCode(String articleCode) {
        this.articleCode = articleCode;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public long getCreate() {
        return createTime;
    }

    public void setCreate(long create) {
        this.createTime = create;
    }

    public String getOrderCycle() {
        return orderCycle;
    }

    public void setOrderCycle(String orderCycle) {
        this.orderCycle = orderCycle;
    }

    public long getOrderCycleStart() {
        return orderCycleStart;
    }

    public void setOrderCycleStart(long orderCycleStart) {
        this.orderCycleStart = orderCycleStart;
    }

    public long getOrderCycleEnd() {
        return orderCycleEnd;
    }

    public void setOrderCycleEnd(long orderCycleEnd) {
        this.orderCycleEnd = orderCycleEnd;
    }

    public long getBizType() {
        return bizType;
    }

    public void setBizType(long bizType) {
        this.bizType = bizType;
    }

    public long getFee() {
        return fee;
    }

    public void setFee(long fee) {
        this.fee = fee;
    }

    public long getPromFee() {
        return promFee;
    }

    public void setPromFee(long promFee) {
        this.promFee = promFee;
    }

    public long getRefundFee() {
        return refundFee;
    }

    public void setRefundFee(long refundFee) {
        this.refundFee = refundFee;
    }

    public long getTotalPayFee() {
        return totalPayFee;
    }

    public void setTotalPayFee(long totalPayFee) {
        this.totalPayFee = totalPayFee;
    }

    @Override
    public String getIdColumn() {
        // TODO Auto-generated method stub
        return "orderId";
    }

    @Override
    public String getIdName() {
        // TODO Auto-generated method stub
        return "orderId";
    }

    @Override
    public String getTableHashKey() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getTableName() {
        // TODO Auto-generated method stub
        return this.TABLE_NAME;
    }

    @Override
    public boolean jdbcSave() {
        long existedId = findExistId(this.orderId);

        if (existedId == 0) {
            return rawInsert();
        }
        return true;
        // TODO Auto-generated method stub

    }

    public boolean rawInsert() {
        long id = JDBCBuilder
                .insert("insert into `article_biz_order`(`bizOrderId`,`orderId`,`nick`,`articleName`,`articleCode`,`itemCode`,`createTime`,`orderCycle`,`orderCycleStart`,`orderCycleEnd`,`bizType`,`fee`,`promFee`,`refundFee`,`totalPayFee`,`level`, `hour`"
                        +
                        ",`monthCycle`) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                        this.bizOrderId, this.orderId, this.nick, this.articleName, this.articleCode, this.itemCode,
                        this.createTime, this.orderCycle, this.orderCycleStart, this.orderCycleEnd, this.bizType,
                        this.fee, this.promFee, this.refundFee, this.totalPayFee, this.level, this.hour,
                        this.monthCycle);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[orderId : ]" + this.orderId);

            return false;
        }

    }

    static String EXIST_ID_QUERY = "select orderId from " + ArticleBizOrderPlay.TABLE_NAME + " where orderId = ? ";

    public static long findExistId(Long orderId) {
        return JDBCBuilder.singleLongQuery(EXIST_ID_QUERY, orderId);
    }

    public static long jdbcCount() {
        return JDBCBuilder.singleLongQuery("select count(*) from  " + ArticleBizOrderPlay.TABLE_NAME);

    }

    public static long jdbcCountBetween(long start, long end) {

        return JDBCBuilder.singleLongQuery("select count(*) from  " + ArticleBizOrderPlay.TABLE_NAME
                + " where createTime >=? and createTime<=?", start, end);
    }

    @Override
    public void setId(Long id) {
        this.orderId = id;
    }

    @Override
    public Long getId() {
        return this.orderId;
    }

    public static String ARTICLE_BIZ_ORDER_PLAy_QUERY = "select orderId,bizOrderId,nick,articleName,articleCode,itemCode," +
    		"createTime,orderCycle,orderCycleStart,orderCycleEnd,bizType,fee,promFee,refundFee,totalPayFee,level,hour,monthCycle from " + TABLE_NAME
            + " where ";
	public static List<ArticleBizOrderPlay> nativeQuery(String query, Object...params) {
		return new JDBCExecutor<List<ArticleBizOrderPlay>>(ARTICLE_BIZ_ORDER_PLAy_QUERY+query, params) {

            @Override
            public List<ArticleBizOrderPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ArticleBizOrderPlay> resulteList = new ArrayList<ArticleBizOrderPlay>();
                while (rs.next()) {
                	ArticleBizOrderPlay order = new ArticleBizOrderPlay();
                	order.setOrderId(rs.getLong(1));
                	order.setBizOrderId(rs.getLong(2));
                	order.setNick(rs.getString(3));
                	order.setArticleName(rs.getString(4));
                	order.setArticleCode(rs.getString(5));
                	order.setItemCode(rs.getString(6));
                	order.setCreate(rs.getLong(7));
                	order.setOrderCycle(rs.getString(8));
                	order.setOrderCycleStart(rs.getLong(9));
                	order.setOrderCycleEnd(rs.getLong(10));
                	order.setBizType(rs.getLong(11));
                	order.setFee(rs.getLong(12));
                	order.setPromFee(rs.getLong(13));
                	order.setRefundFee(rs.getLong(14));
                	order.setTotalPayFee(rs.getLong(15));
                	order.setLevel(rs.getLong(16));
                	order.setHour(rs.getInt(17));
                	order.setMonthCycle(rs.getInt(18));
                	resulteList.add(order);
                }
                return resulteList;
            }
        }.call();

		
	}

	public Long getLevel() {
		return level;
	}

	public void setLevel(Long level) {
		this.level = level;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	public int getMonthCycle() {
		return monthCycle;
	}

	public void setMonthCycle(int monthCycle) {
		this.monthCycle = monthCycle;
	}
	
	@JsonProperty
    public String getPayed() {
        return NumberUtil.doubleFormatter.format((this.totalPayFee) / 100d);
    }

    public String getCycleStart() {
        return new SimpleDateFormat("yyyy-MM-dd").format(this.orderCycleStart);
    }

    public String getCycleEnd() {
        return new SimpleDateFormat("yyyy-MM-dd").format(this.orderCycleEnd);
    }

    public String getCreatedStr() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(this.createTime);

    }
}
