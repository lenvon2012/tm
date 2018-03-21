package dao.spread;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.spread.SpreadItemPlay;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import result.TMResult;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;

import dao.item.ItemDao;

public class SpreadItemDao {
    private static final Logger log = LoggerFactory.getLogger(SpreadItemDao.class);
    
    public static class SpreadStatusType {
        public static final int All = 0;
        public static final int Spreaded = 1;
        public static final int UnSpread = 2;
    }
    
    public static class ItemSortType {
        public static final int Default = 0;
        public static final int TradeDesc = 1;
        public static final int TradeAsc = 2;
    }
    
    static String appendFieldLike(String field, String key) {

        StringBuilder sb = new StringBuilder("(( 0 = 1)");
        String[] keys = key.split("\\s");

        for (String split : keys) {
            if (StringUtils.isBlank(split)) {
                continue;
            }

            sb.append(" or (" + field + " like '%");
            sb.append(CommonUtils.escapeSQL(split));
            sb.append("%')");
        }
        sb.append(")");

        return sb.toString();
    }
    
    
    /*public static TMResult queryNotSpreadItems(Long userId, String catId, 
            String title, int level, int sort, PageOffset po) {
        String sql = " select " + ItemDao.ITEM_SQL + " from item%s where userId=? ";
        
        return null;
    }*/
    
    public static TMResult queryWithItemInfo(Long userId, String catId, int status, 
            String title, int level, int sort, PageOffset po) {
        
        if (level <= 0)
            return new TMResult(new ArrayList<SpreadItemPlay>(), 0, po);
        
        String sql = " from item%s as item left join " +
        		SpreadItemPlay.TABLE_NAME + " as s on item.numIid = s.numIid and s.spreadLevel & " + level + " > 0 where item.userId=? " +
        		" and (item.cid <> 50023725 and item.cid <> 50023728) ";
        
        sql = ItemDao.genShardQuery(sql, userId);
        
        if (!StringUtils.isEmpty(title)) {
            String like = appendFieldLike("item.title", title);
            sql += " and " + like;
        }
        if (!StringUtils.isEmpty(catId)) {
            String like = appendFieldLike("item.sellerCids", catId);
            sql += " and " + like;
        }
        if (status == SpreadStatusType.UnSpread) {
            if (level > 0) {
                sql += " and (s.spreadLevel & " + level + " <= 0 or s.spreadLevel is null) ";
            } else {
                sql += " and (s.spreadLevel <= 0 or s.spreadLevel is null) ";
            }
        } else if (status == SpreadStatusType.Spreaded) {
            if (level > 0) {
                sql += " and s.spreadLevel & " + level + " > 0 ";
            } else {
                sql += " and s.spreadLevel > 0 ";
            }
        }
        
        
        String querySql = " select item.numIid, item.userId, s.spreadUrl, s.spreadStatus, s.spreadLevel, s.createTs, s.updateTs, " +
                " item.picURL, item.title, item.price, item.salesCount ";
        querySql += sql;
        
        if (sort == ItemSortType.Default) {
            querySql += " order by s.createTs desc, item.salesCount desc, item.numIid asc ";
        } else if (sort == ItemSortType.TradeDesc) {
            querySql += " order by item.salesCount desc, s.createTs desc, item.numIid asc ";
        } else if (sort == ItemSortType.TradeAsc) {
            querySql += " order by item.salesCount asc, s.createTs desc, item.numIid asc ";
        }
        
        querySql += " limit ?, ? ";
        
        log.error(querySql);
        
        List<SpreadItemPlay> itemList = new JDBCExecutor<List<SpreadItemPlay>>(querySql, userId, po.getOffset(), po.getPs()) {
            @Override
            public List<SpreadItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<SpreadItemPlay> tempList = new ArrayList<SpreadItemPlay>();
                while (rs.next()) {
                    SpreadItemPlay tempObj = new SpreadItemPlay();
                    try {
                        tempObj.setNumIid(rs.getLong(1));
                        tempObj.setUserId(rs.getLong(2));
                        tempObj.setSpreadUrl(rs.getString(3));
                        tempObj.setSpreadStatus(rs.getInt(4));
                        tempObj.setSpreadLevel(rs.getInt(5));
                        tempObj.setCreateTs(rs.getLong(6));
                        tempObj.setUpdateTs(rs.getLong(7));
                        
                        tempObj.setPicURL(rs.getString(8));
                        tempObj.setTitle(rs.getString(9));
                        tempObj.setPrice(rs.getDouble(10));
                        tempObj.setSalesCount(rs.getInt(11));
                        tempList.add(tempObj);
                    } catch (Exception ex) {
                        log.error(ex.getMessage(), ex);
                        continue;
                    }
                    
                } 
                return tempList;
            }
        }.call();
        
        
        String countSql = " select count(*) " + sql;
        long count = JDBCBuilder.singleLongQuery(countSql, userId);
        
        return new TMResult(itemList, (int)count, po);
    }
    
    
    public static long countSpreadNumByLevel(Long userId, int level) {
        String countSql = " select count(*) from " + SpreadItemPlay.TABLE_NAME + " where userId = ? and spreadLevel & " + level + " > 0 ";
        
        long count = JDBCBuilder.singleLongQuery(countSql, userId);
        
        return count;
    }
    
    public static List<SpreadItemPlay> querySpreadItemsByIds(Long userId, List<Long> numIidList, int level) {
        if (CommonUtils.isEmpty(numIidList))
            return new ArrayList<SpreadItemPlay>();
        
        String sql = "select " + SpreadItemProperties + " from " + SpreadItemPlay.TABLE_NAME + " where userId = ? and spreadLevel & " + level + " > 0 and ";
        String numIids = StringUtils.join(numIidList, ",");
        sql += " numIid in (" + numIids + ") ";
        
        List<SpreadItemPlay> itemList = new JDBCExecutor<List<SpreadItemPlay>>(sql, userId) {
            @Override
            public List<SpreadItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<SpreadItemPlay> tempList = new ArrayList<SpreadItemPlay>();
                while (rs.next()) {
                    SpreadItemPlay tempObj = parseSpreadItem(rs);
                    if (tempObj != null)
                        tempList.add(tempObj);
                } 
                return tempList;
            }
        }.call();
        
        return itemList;
    }
    
    
    public static boolean deleteSpreadItemByNumIids(Long userId, int level, List<Long> numIidList) {
        if (CommonUtils.isEmpty(numIidList))
            return true;
        
        String sql = " delete from " + SpreadItemPlay.TABLE_NAME + " where userId = ? and spreadLevel & " + level + " > 0 and ";
        String numIids = StringUtils.join(numIidList, ",");
        sql += " numIid in (" + numIids + ") ";
        
        long result = JDBCBuilder.insert(sql, userId);
        
        if (result > 0L)
            return true;
        else
            return false;
    }
    
    public static void deleteAllByLevel(Long userId, int level) {
        String sql = " delete from " + SpreadItemPlay.TABLE_NAME + " where userId = ? and spreadLevel & " + level + " > 0 "; 
        
        JDBCBuilder.insert(sql, userId);
    }
    
    public static void updateSpreadItemStatus(Long userId, int level, int targetStatus) {
        String sql = " update " + SpreadItemPlay.TABLE_NAME + " set spreadStatus=? where userId = ? and spreadLevel & " + level + " > 0 "; 
        
        JDBCBuilder.insert(sql, targetStatus, userId);
    }
    
    
    private static String SpreadItemProperties = " numIid, userId, spreadUrl, spreadStatus, spreadLevel, createTs, updateTs ";
    
    public static SpreadItemPlay parseSpreadItem(ResultSet rs) {
        try {
            
            SpreadItemPlay spreadItem = new SpreadItemPlay();
            spreadItem.setNumIid(rs.getLong(1));
            spreadItem.setUserId(rs.getLong(2));
            spreadItem.setSpreadUrl(rs.getString(3));
            spreadItem.setSpreadStatus(rs.getInt(4));
            spreadItem.setSpreadLevel(rs.getInt(5));
            spreadItem.setCreateTs(rs.getLong(6));
            spreadItem.setUpdateTs(rs.getLong(7));
            
            
            return spreadItem;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }
    
}
