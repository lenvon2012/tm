package dao.jd.crmmember;

import java.sql.ResultSet;
import java.sql.SQLException;

import models.jd.JDGradeConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transaction.JDBCBuilder.JDBCExecutor;

public class JDGradeConfigDao {
    private static final Logger log = LoggerFactory.getLogger(JDGradeConfigDao.class);
    
    public static JDGradeConfig findBySellerId(Long sellerId) {
        if (sellerId == null || sellerId <= 0) {
            return null;
        }
        
        String sql = " select " + GradeConfigProperties + " from " + JDGradeConfig.TABLE_NAME + " where sellerId = ? ";
        
        return new JDBCExecutor<JDGradeConfig>(JDGradeConfig.GradeConfigDp, sql, sellerId) {
            @Override
            public JDGradeConfig doWithResultSet(ResultSet rs) throws SQLException {
                
                if (rs.next()) {
                    JDGradeConfig gradeConfig = parseJDGradeConfig(rs);
                    return gradeConfig;
                }
                return null;
            }
        }.call();
        
    }
    
    
    
    private static final String GradeConfigProperties = " sellerId, normalTradeAmount, normalTradeCount, " +
            " advanceTradeAmount, advanceTradeCount, vipTradeAmount, vipTradeCount, " +
            " godTradeAmount, godTradeCount, createTs, updateTs ";


    
    
    
    private static JDGradeConfig parseJDGradeConfig(ResultSet rs) {
        try {
            JDGradeConfig gradeConfig = new JDGradeConfig();
            
            gradeConfig.setSellerId(rs.getLong(1));
            gradeConfig.setNormalTradeAmount(rs.getDouble(2));
            gradeConfig.setNormalTradeCount(rs.getInt(3));
            gradeConfig.setAdvanceTradeAmount(rs.getDouble(4));
            gradeConfig.setAdvanceTradeCount(rs.getInt(5));
            gradeConfig.setVipTradeAmount(rs.getDouble(6));
            gradeConfig.setVipTradeCount(rs.getInt(7));
            gradeConfig.setGodTradeAmount(rs.getDouble(8));
            gradeConfig.setGodTradeCount(rs.getInt(9));
            gradeConfig.setCreateTs(rs.getLong(10));
            gradeConfig.setUpdateTs(rs.getLong(11));
            
            
            return gradeConfig;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }
    
    
}
