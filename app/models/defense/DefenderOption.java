/**
 * 
 */
package models.defense;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

/**
 * @author navins
 * @date 2013-6-2 下午1:05:37
 */
@Entity(name = DefenderOption.TABLE_NAME)
public class DefenderOption extends Model implements PolicySQLGenerator {
    
    @Transient
    public static final DefenderOption EMPTY = new DefenderOption();
    
    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.QUOTA, EMPTY);
    
    @Transient
    private static final Logger log = LoggerFactory.getLogger(DefenderOption.class);

    @Transient
    public static final String TABLE_NAME = "defender_option";

    @Transient
    public static final String DEFAULT_CLOSEREASON = "系统错误，请联系客服！";

    // 卖家Id
    @Index(name = "userId")
    private long userId;

    // 到目前为止注册时间长，单位天
    private int regDays;

    // 设置最近购物次数，和下面两项结合设置
    private int recentTimes;

    // 最近半年，给过中差评数量
    private int recentChapingCount;
    // 最近半年，给过我中差评数量
    private int recentMeChapingCount;

    // 买家申请退货次数
    private int refundNum;

    private int vipLevel;

    // 买家信誉限制，低于此值判断为差评师
    private int buyerCreditLimt;
    
    // 被别的卖家加入黑名单次数超过，拦截
    private int addBlackListTimes;

    // 对买家获得的好评率限制，低于此值判断为差评师
    private double goodCreditRateLimit;

    // 买家给出的好评限制，低于此值判断为差评师
    private double positiveRate;

    // 一个月内总够购买次数不超过monthTotalNum，给中评差评数量高于monthNonPositiveNum不能购买
    private int monthTotalNum;
    private int monthNonPositiveNum;

    // 半年内总够购买次数不超过halfYearTotalNum时，给中差评数量高于halfYearNonPositiveNum不能购买
    private int halfYearTotalNum;
    private int halfYearNonPositiveNum;

    // 最近一个月的成交占最近半年的成交的比例，占比高于多少视为差评师
    private double monthTradePercent;

    // 价格低于该值，关闭订单
    private double priceLimit;

    // 设置这些，只允许认证用户购买
    private boolean isVerifyLimit;

    // 在本店买过，并给好评的买家，直接通过
    private boolean hasBuyProductWithGoodComment;

    // 是否允许信誉为0的买家
    private boolean allowNoCreditBuyer;

    // 是否允许卖家购买
    private boolean allowSeller;
    
    // 买家收到的信用， 周不过3
    private int buyerWeekCreditLimit;
    
    // 买家收到的信用， 月不过7
    private int buyerMonthCreditLimit;
    
    // 买家收到的信用， 半年不过15
    private int buyerHalfYearCreditLimit;
    
    // 排除的地域，用逗号隔开； 只排除省份
    private String excludeAreas;
    
    private String closeReason;

    public static final DefenderOption DEFAULT_OPTION;

    static {
        DEFAULT_OPTION = new DefenderOption();
        DEFAULT_OPTION.setRegDays(5);
        DEFAULT_OPTION.setVerifyLimit(false);
        DEFAULT_OPTION.setBuyerCreditLimt(5);
        DEFAULT_OPTION.setGoodCreditRateLimit(80);
        DEFAULT_OPTION.setPositiveRate(80);
        DEFAULT_OPTION.setRecentTimes(40);
        DEFAULT_OPTION.setRecentChapingCount(3);
        DEFAULT_OPTION.setRecentMeChapingCount(2);
        DEFAULT_OPTION.setRefundNum(2);
        DEFAULT_OPTION.setMonthNonPositiveNum(2);
        DEFAULT_OPTION.setMonthTotalNum(30);
        DEFAULT_OPTION.setHalfYearTotalNum(0);
        DEFAULT_OPTION.setHalfYearNonPositiveNum(0);
        DEFAULT_OPTION.setCloseReason(DEFAULT_CLOSEREASON);
        DEFAULT_OPTION.setAddBlackListTimes(3);
        DEFAULT_OPTION.setAllowSeller(true);
        DEFAULT_OPTION.setBuyerWeekCreditLimit(0);
        DEFAULT_OPTION.setBuyerMonthCreditLimit(0);
        DEFAULT_OPTION.setBuyerHalfYearCreditLimit(0);
        DEFAULT_OPTION.setExcludeAreas("");
    }

    public DefenderOption() {

    }

    public DefenderOption(ResultSet rs) throws SQLException {
        this.userId = rs.getLong(1);
        this.regDays = rs.getInt(2);
        this.recentTimes = rs.getInt(3);
        this.recentChapingCount = rs.getInt(4);
        this.recentMeChapingCount = rs.getInt(5);
        this.refundNum = rs.getInt(6);
        this.vipLevel = rs.getInt(7);
        this.buyerCreditLimt = rs.getInt(8);
        this.addBlackListTimes = rs.getInt(9);
        this.goodCreditRateLimit = rs.getDouble(10);
        this.positiveRate = rs.getDouble(11);
        this.monthTotalNum = rs.getInt(12);
        this.monthNonPositiveNum = rs.getInt(13);
        this.halfYearTotalNum = rs.getInt(14);
        this.halfYearNonPositiveNum = rs.getInt(15);
        this.monthTradePercent = rs.getDouble(16);
        this.priceLimit = rs.getDouble(17);
        this.isVerifyLimit = rs.getBoolean(18);
        this.hasBuyProductWithGoodComment = rs.getBoolean(19);
        this.allowNoCreditBuyer = rs.getBoolean(20);
        this.closeReason = rs.getString(21);
        this.allowSeller = rs.getBoolean(22);
        this.buyerWeekCreditLimit = rs.getInt(23);
        this.buyerMonthCreditLimit = rs.getInt(24);
        this.buyerHalfYearCreditLimit = rs.getInt(25);
        this.excludeAreas = rs.getString(26);
    }

	public boolean isAllowSeller() {
		return allowSeller;
	}

	public void setAllowSeller(boolean allowSeller) {
		this.allowSeller = allowSeller;
	}

	public String getExcludeAreas() {
		return excludeAreas;
	}

	public void setExcludeAreas(String excludeAreas) {
		this.excludeAreas = excludeAreas;
	}

	public int getBuyerWeekCreditLimit() {
		return buyerWeekCreditLimit;
	}

	public void setBuyerWeekCreditLimit(int buyerWeekCreditLimit) {
		this.buyerWeekCreditLimit = buyerWeekCreditLimit;
	}

	public int getBuyerMonthCreditLimit() {
		return buyerMonthCreditLimit;
	}

	public void setBuyerMonthCreditLimit(int buyerMonthCreditLimit) {
		this.buyerMonthCreditLimit = buyerMonthCreditLimit;
	}

	public int getBuyerHalfYearCreditLimit() {
		return buyerHalfYearCreditLimit;
	}

	public void setBuyerHalfYearCreditLimit(int buyerHalfYearCreditLimit) {
		this.buyerHalfYearCreditLimit = buyerHalfYearCreditLimit;
	}

	public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getRegDays() {
        return regDays;
    }

    public void setRegDays(int regDays) {
        this.regDays = regDays;
    }

    public int getRecentTimes() {
        return recentTimes;
    }

    public void setRecentTimes(int recentTimes) {
        this.recentTimes = recentTimes;
    }

    public int getRecentChapingCount() {
        return recentChapingCount;
    }

    public void setRecentChapingCount(int recentChapingCount) {
        this.recentChapingCount = recentChapingCount;
    }

    public int getRecentMeChapingCount() {
        return recentMeChapingCount;
    }

    public void setRecentMeChapingCount(int recentMeChapingCount) {
        this.recentMeChapingCount = recentMeChapingCount;
    }

    public int getRefundNum() {
        return refundNum;
    }

    public void setRefundNum(int refundNum) {
        this.refundNum = refundNum;
    }

    public int getVipLevel() {
        return vipLevel;
    }

    public void setVipLevel(int vipLevel) {
        this.vipLevel = vipLevel;
    }

    public int getBuyerCreditLimt() {
        return buyerCreditLimt;
    }

    public void setBuyerCreditLimt(int buyerCreditLimt) {
        this.buyerCreditLimt = buyerCreditLimt;
    }

    public int getAddBlackListTimes() {
        return addBlackListTimes;
    }

    public void setAddBlackListTimes(int addBlackListTimes) {
        this.addBlackListTimes = addBlackListTimes;
    }

    public double getGoodCreditRateLimit() {
        return goodCreditRateLimit;
    }

    public void setGoodCreditRateLimit(double goodCreditRateLimit) {
        this.goodCreditRateLimit = goodCreditRateLimit;
    }

    public double getPositiveRate() {
        return positiveRate;
    }

    public void setPositiveRate(double positiveRate) {
        this.positiveRate = positiveRate;
    }

    public int getMonthTotalNum() {
        return monthTotalNum;
    }

    public void setMonthTotalNum(int monthTotalNum) {
        this.monthTotalNum = monthTotalNum;
    }

    public int getMonthNonPositiveNum() {
        return monthNonPositiveNum;
    }

    public void setMonthNonPositiveNum(int monthNonPositiveNum) {
        this.monthNonPositiveNum = monthNonPositiveNum;
    }

    public int getHalfYearTotalNum() {
        return halfYearTotalNum;
    }

    public void setHalfYearTotalNum(int halfYearTotalNum) {
        this.halfYearTotalNum = halfYearTotalNum;
    }

    public int getHalfYearNonPositiveNum() {
        return halfYearNonPositiveNum;
    }

    public void setHalfYearNonPositiveNum(int halfYearNonPositiveNum) {
        this.halfYearNonPositiveNum = halfYearNonPositiveNum;
    }

    public double getMonthTradePercent() {
        return monthTradePercent;
    }

    public void setMonthTradePercent(double monthTradePercent) {
        this.monthTradePercent = monthTradePercent;
    }

    public double getPriceLimit() {
        return priceLimit;
    }

    public void setPriceLimit(double priceLimit) {
        this.priceLimit = priceLimit;
    }

    public boolean isVerifyLimit() {
        return isVerifyLimit;
    }

    public void setVerifyLimit(boolean isVerifyLimit) {
        this.isVerifyLimit = isVerifyLimit;
    }

    public boolean isHasBuyProductWithGoodComment() {
        return hasBuyProductWithGoodComment;
    }

    public void setHasBuyProductWithGoodComment(boolean hasBuyProductWithGoodComment) {
        this.hasBuyProductWithGoodComment = hasBuyProductWithGoodComment;
    }

    public boolean isAllowNoCreditBuyer() {
        return allowNoCreditBuyer;
    }

    public void setAllowNoCreditBuyer(boolean allowNoCreditBuyer) {
        this.allowNoCreditBuyer = allowNoCreditBuyer;
    }

    public String getCloseReason() {
        return closeReason;
    }

    public void setCloseReason(String closeReason) {
        this.closeReason = closeReason;
    }

    @Override
    public String getTableName() {
        // TODO Auto-generated method stub
        return TABLE_NAME;
    }

    @Override
    public String getTableHashKey() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getIdColumn() {
        // TODO Auto-generated method stub
        return "id";
    }

    @Override
    public void setId(Long id) {
        // TODO Auto-generated method stub
        this.id = id;
    }

    static String EXIST_ID_QUERY = "select id from " + TABLE_NAME + " where userId = ? ";

    private static long findExistId(Long userId) {
        return dp.singleLongQuery(EXIST_ID_QUERY, userId);
    }

    @Override
    public boolean jdbcSave() {
        try {
            long existdId = findExistId(this.userId);

            if (existdId == 0L) {
                return this.rawInsert();
            } else {
                id = existdId;
                return this.rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getIdName() {
        // TODO Auto-generated method stub
        return "id";
    }

    public boolean rawInsert() {
        long id = dp.insert("insert into `defender_option`(`userId`,`regDays`,`recentTimes`," +
        		"`recentChapingCount`,`recentMeChapingCount`,`refundNum`,`vipLevel`," +
        		"`buyerCreditLimt`,`addBlackListTimes`,`goodCreditRateLimit`,`positiveRate`," +
        		"`monthTotalNum`,`monthNonPositiveNum`,`halfYearTotalNum`," +
        		"`halfYearNonPositiveNum`,`monthTradePercent`,`priceLimit`,`isVerifyLimit`," +
        		"`hasBuyProductWithGoodComment`,`allowNoCreditBuyer`,`closeReason`," +
        		"`allowSeller`,`buyerWeekCreditLimit`,`buyerMonthCreditLimit`," +
        		"`buyerHalfYearCreditLimit`,`excludeAreas`)" +
        		" values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
        		this.userId ,this.regDays ,this.recentTimes ,this.recentChapingCount ,
        		this.recentMeChapingCount ,this.refundNum ,this.vipLevel ,
        		this.buyerCreditLimt ,this.addBlackListTimes ,this.goodCreditRateLimit ,
        		this.positiveRate ,this.monthTotalNum ,this.monthNonPositiveNum ,
        		this.halfYearTotalNum ,this.halfYearNonPositiveNum ,this.monthTradePercent ,
        		this.priceLimit ,this.isVerifyLimit ,this.hasBuyProductWithGoodComment ,
        		this.allowNoCreditBuyer ,this.closeReason, this.allowSeller, 
        		this.buyerWeekCreditLimit, this.buyerMonthCreditLimit, 
        		this.buyerHalfYearCreditLimit, this.excludeAreas);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.userId);
            return false;
        }

    }

    public boolean rawUpdate() {
        long updateNum = dp.insert("update `defender_option` set  `userId` = ?, `regDays` = ?," +
        		" `recentTimes` = ?, `recentChapingCount` = ?, `recentMeChapingCount` = ?," +
        		" `refundNum` = ?, `vipLevel` = ?, `buyerCreditLimt` = ?, " +
        		"`addBlackListTimes` = ?, `goodCreditRateLimit` = ?, `positiveRate` = ?," +
        		" `monthTotalNum` = ?, `monthNonPositiveNum` = ?, `halfYearTotalNum` = ?," +
        		" `halfYearNonPositiveNum` = ?, `monthTradePercent` = ?, `priceLimit` = ?," +
        		" `isVerifyLimit` = ?, `hasBuyProductWithGoodComment` = ?," +
        		" `allowNoCreditBuyer` = ?, `closeReason` = ?, `allowSeller` = ?," +
        		" `buyerWeekCreditLimit` = ?, `buyerMonthCreditLimit` = ?," +
        		" `buyerHalfYearCreditLimit` = ?, `excludeAreas` = ?" +
        		"  where `id` = ? ",
        		this.userId ,this.regDays ,this.recentTimes ,this.recentChapingCount ,
        		this.recentMeChapingCount ,this.refundNum ,this.vipLevel ,
        		this.buyerCreditLimt ,this.addBlackListTimes ,this.goodCreditRateLimit ,
        		this.positiveRate ,this.monthTotalNum ,this.monthNonPositiveNum ,
        		this.halfYearTotalNum ,this.halfYearNonPositiveNum ,this.monthTradePercent ,
        		this.priceLimit ,this.isVerifyLimit ,this.hasBuyProductWithGoodComment ,
        		this.allowNoCreditBuyer ,this.closeReason ,this.allowSeller, 
        		this.buyerWeekCreditLimit, this.buyerMonthCreditLimit, 
        		this.buyerHalfYearCreditLimit, this.excludeAreas,
        		this.getId());

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for :" + this.id + "[userId : ]" + this.userId);

            return false;
        }
    }

}
