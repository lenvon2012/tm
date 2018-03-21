package dao.defense;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.defense.BlackListBuyer;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import result.TMResult;
import transaction.JDBCBuilder.JDBCExecutor;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;

public class BlackListBuyerDao {
	
	private static final Logger log = LoggerFactory.getLogger(BlackListBuyerDao.class);
	
	private static final String BlackList_Buyer_Sql = " select id, userId, buyerName, ts, remark from " + BlackListBuyer.TABLE_NAME;
	private static final String BlackList_Buyer_Count_Sql = " select count(*) from " + BlackListBuyer.TABLE_NAME;
	private static final String BlackList_Buyer_Delete_Sql = " delete from " + BlackListBuyer.TABLE_NAME;
	
	private static final String BlackList_Buyer_Count_Distinct_Sql = " select count(distinct userId) from " + BlackListBuyer.TABLE_NAME;
	
	public static TMResult findBlackListBuyersByName(Long userId, String buyerName, PageOffset po) {
		String query = " userId=? ";
		if (!StringUtils.isEmpty(buyerName)) {
			query += " and " + formatBuyerNameLike(buyerName);
		}
		String sql = BlackList_Buyer_Sql + " where " + query + " order by ts desc limit ? offset ? ";
		//log.info(sql);
		List<BlackListBuyer> buyerList = new JDBCExecutor<List<BlackListBuyer>>(BlackListBuyer.dp, sql, userId, po.getPs(), po.getOffset()) {
            @Override
            public List<BlackListBuyer> doWithResultSet(ResultSet rs) throws SQLException {
                List<BlackListBuyer> list = new ArrayList<BlackListBuyer>();
                while (rs.next()) {
                	BlackListBuyer blackListBuyer = parseBlackListBuyer(rs);
                	if (blackListBuyer != null)
                		list.add(blackListBuyer);
                }
                return list;
            }
        }.call();
        
        String countSql = BlackList_Buyer_Count_Sql + " where " + query;
        long count = BlackListBuyer.dp.singleLongQuery(countSql, userId);
        
        TMResult tmResult = new TMResult(buyerList, (int)count, po);
        return tmResult;
	}
	
	public static long countDistinctUser(String buyerName) {
	    String sql = BlackList_Buyer_Count_Distinct_Sql + " where buyerName = ?";
	    return BlackListBuyer.dp.singleLongQuery(sql, buyerName);
	}
	
	public static BlackListBuyer findByBuyerName(Long userId, String buyerName) {
		String sql = BlackList_Buyer_Sql + " where userId=? and buyerName=? ";
		return new JDBCExecutor<BlackListBuyer>(BlackListBuyer.dp, sql, userId, buyerName) {
            @Override
            public BlackListBuyer doWithResultSet(ResultSet rs) throws SQLException {

                if (rs.next()) {
                    return parseBlackListBuyer(rs);
                }
                return null;
            }
        }.call();
	}
	
	public static BlackListBuyer findBlackListBuyersById(Long userId, Long id) {
		String sql = BlackList_Buyer_Sql + " where userId=? and id=? ";
		return new JDBCExecutor<BlackListBuyer>(BlackListBuyer.dp, sql, userId, id) {
            @Override
            public BlackListBuyer doWithResultSet(ResultSet rs) throws SQLException {

                if (rs.next()) {
                    return parseBlackListBuyer(rs);
                }
                return null;
            }
        }.call();
	}
	
	public static List<BlackListBuyer> findBlackListBuyersByIds(Long userId, List<Long> idList) {
		if (CommonUtils.isEmpty(idList))
			return new ArrayList<BlackListBuyer>();
		String idQuery = formatIdQuery(idList);
		
		String sql = BlackList_Buyer_Sql + " where userId=? and " + idQuery;
		List<BlackListBuyer> buyerList = new JDBCExecutor<List<BlackListBuyer>>(BlackListBuyer.dp, sql, userId) {
            @Override
            public List<BlackListBuyer> doWithResultSet(ResultSet rs) throws SQLException {
                List<BlackListBuyer> list = new ArrayList<BlackListBuyer>();
                while (rs.next()) {
                	BlackListBuyer blackListBuyer = parseBlackListBuyer(rs);
                	if (blackListBuyer != null)
                		list.add(blackListBuyer);
                }
                return list;
            }
        }.call();
        
        return buyerList;
	}
	
	public static List<BlackListBuyer> findBlackListBuyers(Long userId) {
        String sql = BlackList_Buyer_Sql + " where userId=? ";
        List<BlackListBuyer> buyerList = new JDBCExecutor<List<BlackListBuyer>>(BlackListBuyer.dp, sql, userId) {
            @Override
            public List<BlackListBuyer> doWithResultSet(ResultSet rs) throws SQLException {
                List<BlackListBuyer> list = new ArrayList<BlackListBuyer>();
                while (rs.next()) {
                    BlackListBuyer blackListBuyer = parseBlackListBuyer(rs);
                    if (blackListBuyer != null)
                        list.add(blackListBuyer);
                }
                return list;
            }
        }.call();
        
        return buyerList;
    }
	
	public static long countBlackListBuyers(Long userId) {
	    String sql = BlackList_Buyer_Count_Sql + " where userId = ?";
	    return BlackListBuyer.dp.singleLongQuery(sql, userId);
	}
	
	public static boolean deleteByIds(Long userId, List<Long> idList) {
		if (CommonUtils.isEmpty(idList))
			return true;
		String idQuery = formatIdQuery(idList);
		String sql = BlackList_Buyer_Delete_Sql + " where userId=? and " + idQuery;
		long result = BlackListBuyer.dp.update(false, sql, userId);
		if (result == 0L)
			return false;
		return true;
	}
	
	private static String formatBuyerNameLike(String buyerName) {
		String like = " buyerName like '%" + buyerName + "%' ";
		return like;
	}

	private static String formatIdQuery(List<Long> idList) {
		String idQuery = "";
		for (Long id : idList) {
			if (!StringUtils.isEmpty(idQuery))
				idQuery += ",";
			idQuery += id;
		}
		idQuery = " id in (" + idQuery + ") ";
		return idQuery;
	}
	
	private static BlackListBuyer parseBlackListBuyer(ResultSet rs) {
		try {
			Long id = rs.getLong(1);
			Long userId = rs.getLong(2);
			String buyerName = rs.getString(3);
			Long ts = rs.getLong(4);
			String remark = rs.getString(5);
			BlackListBuyer blackListBuyer = new BlackListBuyer(userId, buyerName, ts, remark);
			blackListBuyer.setId(id);
			
			return blackListBuyer;
			
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return null;
		}
	}
	
	public static void testArray(List<BlackListBuyer> buyers) {
		for(BlackListBuyer buyer : buyers) {
			buyer =  null;
		}
		if(CommonUtils.isEmpty(buyers)) {
			log.info("no data");
		}
	}
}
