package dao.jd.crmmember;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.jd.JDCrmMember;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transaction.JDBCBuilder.JDBCExecutor;

public class JDCrmMemberDao {
    private static final Logger log = LoggerFactory.getLogger(JDCrmMemberDao.class);
    
    
    public static List<JDCrmMember> findCrmMemberListBySql(String sql, Object[] paramArray) {
        
        log.info("query crm member sql: " + sql + " ------------------");
        
        return queryListByJDBC(sql, paramArray);
        
    }
    
    public static long countCrmMemberBySql(String sql, Object[] paramArray) {
        
        log.info("count crm member sql: " + sql + " ------------------");
        
        
        long count = JDCrmMember.CrmMemberDp.singleLongQuery(sql, paramArray);
        
        return count;
    }
    
    private static List<JDCrmMember> queryListByJDBC(String sql, Object... params) {
        
        return new JDBCExecutor<List<JDCrmMember>>(JDCrmMember.CrmMemberDp, sql, params) {
            @Override
            public List<JDCrmMember> doWithResultSet(ResultSet rs) throws SQLException {
                List<JDCrmMember> crmMemberList = new ArrayList<JDCrmMember>();
                while (rs.next()) {
                    JDCrmMember crmMember = parseJDCrmMember(rs);
                    if (crmMember != null) {
                        crmMemberList.add(crmMember);
                    }
                }
                return crmMemberList;
            }
        }.call();
    }
    
    
    static final String CrmMemberProperties = " id, sellerId, buyerNick, " +
            " telephone, avgPrice, bizOrderId, area, closeTradeAmount, closeTradeCount, " +
            " grade, groupIds, itemCloseCount, itemNum, lastTradeTime, relationSource, " +
            " status, tradeAmount, tradeCount, updateTs ";


    
    
    private static JDCrmMember parseJDCrmMember(ResultSet rs) {
        try {
            JDCrmMember crmMember = new JDCrmMember();
            crmMember.setId(rs.getLong(1));
            crmMember.setSellerId(rs.getLong(2));
            crmMember.setBuyerNick(rs.getString(3));
            crmMember.setTelephone(rs.getString(4));
            crmMember.setAvgPrice(rs.getDouble(5));
            crmMember.setBizOrderId(rs.getLong(6));
            crmMember.setArea(rs.getString(7));
            crmMember.setCloseTradeAmount(rs.getDouble(8));
            crmMember.setCloseTradeCount(rs.getLong(9));
            crmMember.setGrade(rs.getLong(10));
            crmMember.setGroupIds(rs.getString(11));
            crmMember.setItemCloseCount(rs.getLong(12));
            crmMember.setItemNum(rs.getLong(13));
            crmMember.setLastTradeTime(rs.getLong(14));
            crmMember.setRelationSource(rs.getLong(15));
            crmMember.setStatus(rs.getInt(16));
            crmMember.setTradeAmount(rs.getDouble(17));
            crmMember.setTradeCount(rs.getLong(18));
            crmMember.setUpdateTs(rs.getLong(19));
            
            return crmMember;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }
    
    
    
}
