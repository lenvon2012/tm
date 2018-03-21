package dao.defense;

import java.sql.ResultSet;
import java.sql.SQLException;

import models.defense.DefenderOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transaction.JDBCBuilder.JDBCExecutor;


public class DefenderOptionDao {
	private static final Logger log = LoggerFactory.getLogger(DefenderOptionDao.class);
	
    private static final String Defender_Option_Sql = " select userId,regDays," +
    		"recentTimes,recentChapingCount,recentMeChapingCount,refundNum,vipLevel," +
    		"buyerCreditLimt,addBlackListTimes,goodCreditRateLimit,positiveRate,monthTotalNum," +
    		"monthNonPositiveNum,halfYearTotalNum,halfYearNonPositiveNum,monthTradePercent," +
    		"priceLimit,isVerifyLimit,hasBuyProductWithGoodComment,allowNoCreditBuyer," +
    		"closeReason,allowSeller,buyerWeekCreditLimit,buyerMonthCreditLimit," +
    		"buyerHalfYearCreditLimit,excludeAreas from "
            + DefenderOption.TABLE_NAME;
    private static final String Defender_Option_Count_Sql = " select count(*) from " + DefenderOption.TABLE_NAME;
	
    public static DefenderOption findByUserId(Long userId) {
        String sql = Defender_Option_Sql + " where userId=?";
        return new JDBCExecutor<DefenderOption>(DefenderOption.dp, sql, userId) {
            @Override
            public DefenderOption doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return new DefenderOption(rs);
                }
                return null;
            }
        }.call();
    }
    
    public static DefenderOption findOrUseDefaultOption(Long userId) {
        DefenderOption option = DefenderOptionDao.findByUserId(userId);
        if (option == null) {
            option = DefenderOption.DEFAULT_OPTION;
            option.setUserId(userId);
            option.jdbcSave();
        }
        return option;
    }
    
    public static void updateDefenderOption(DefenderOption option) {
        
        option.jdbcSave();
    }
	
}
