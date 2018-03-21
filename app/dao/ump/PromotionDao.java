package dao.ump;

import codegen.CodeGenerator.DBDispatcher;
import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import controllers.PhoneItem;
import controllers.UmpPromotion.ItemPromotionBean;
import dao.item.ItemDao;
import jdbcexecutorwrapper.JDBCLongSetExecutor;
import models.item.ItemPlay;
import models.ump.PromotionPlay;
import models.ump.PromotionPlay.ItemPromoteType;
import models.ump.PromotionPlay.TMPromotionStatus;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import transaction.DBBuilder;
import transaction.JDBCBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PromotionDao {

    private static final Logger log = LoggerFactory.getLogger(PromotionDao.class);
    
    private static DBDispatcher dp = PromotionPlay.dp;
    
    
    public static PromotionPlay findFirstActivePromotionByNumIid(Long userId, Long numIid) {
        
        String query = "select " + SelectAllProperty + " from " + PromotionPlay.TABLE_NAME + "%s where "
                + " userId = ? and numIid = ? ";
        query = appendActiveStatusRule(query);
        
        query = genShardQuery(query, userId);
        
        
        return queryByJDBC(query, userId, numIid);
    }
    
    public static void unActiveActivityPromotions(Long userId, Long tmActivityId) {
        
        String sql = " update " + PromotionPlay.TABLE_NAME + "%s set tmStatus = ?, updateTs = ? where " +
        		" userId = ? and tmActivityId = ? ";
        
        sql = genShardQuery(sql, userId);
        
        dp.update(sql, TMPromotionStatus.UnActive, System.currentTimeMillis(), 
                userId, tmActivityId);
        
    }
    
    public static boolean deleteByTMActivityIdAndNumIid(Long userId, Long tmActivityId, Long numIid) {
        
        String deleteSql = " delete from " + PromotionPlay.TABLE_NAME + "%s where "
                + " userId = ? and tmActivityId = ? and numIid = ? ";
        
        deleteSql = genShardQuery(deleteSql, userId);
        
        dp.update(deleteSql, userId, tmActivityId, numIid);
        
        return true;
        
    }
    
    public static Set<Long> findActiveNumIidsByTMActivityId(Long userId, Long tmActivityId) {
        String query = " select numIid from " + PromotionPlay.TABLE_NAME + "%s where "
                + " userId = ? and tmActivityId = ? ";
        
        query = appendActiveStatusRule(query);
        
        query = genShardQuery(query, userId);
        
        return new JDBCLongSetExecutor(dp, query, userId, tmActivityId).call();
    }
    

    public static Set<Long> findUnActiveNumIidsByTMActivityId(Long userId, Long tmActivityId) {
        
        String query = " select numIid from " + PromotionPlay.TABLE_NAME + "%s where "
                + " userId = ? and tmActivityId = ? ";
        
        query = appendUnActiveStatusRule(query);
        
        query = genShardQuery(query, userId);
        
        return new JDBCLongSetExecutor(dp, query, userId, tmActivityId).call();
        
    }
    
    public static long countByPromotionType(Long userId, Long tmActivityId, 
            ItemPromoteType promotionType) {
        
        String query = " select count(*) from " + PromotionPlay.TABLE_NAME + "%s where "
                + " userId = ? and tmActivityId = ? and promotionType = ? ";
        
        query = genShardQuery(query, userId);
        
        long count = dp.singleLongQuery(query, userId, tmActivityId, promotionType.toString());
        
        return count;
        
    }
    
    
    
    public static long countActivePromotionsByTMActivityId(Long userId, Long tmActivityId) {
        String query = " select count(*) from " + PromotionPlay.TABLE_NAME + "%s where "
                + " userId = ? and tmActivityId = ? ";
        
        query = genShardQuery(query, userId);
        
        query = appendActiveStatusRule(query);
        
        long count = dp.singleLongQuery(query, userId, tmActivityId);
        
        return count;
        
        
    }
    
    public static long countActivePromotionsByTMActivityIdWithItemExist(Long userId, Long tmActivityId) {
        long count = countActivePromotionsByRules(userId,
                tmActivityId, "", "", "", false, new HashSet<Long>());
        
        return count;
    }
    
    
    public static void deleteByTMActivityId(Long userId, Long tmActivityId) {
        
        String deleteSql = " delete from " + PromotionPlay.TABLE_NAME + "%s where "
                + " userId = ? and tmActivityId = ? ";
        
        deleteSql = genShardQuery(deleteSql, userId);
        
        dp.update(deleteSql, userId, tmActivityId);
        
        return;
    }
    
    
    
    public static List<PromotionPlay> findAllOnActiveByNumIids(Long userId, Set<Long> numIidSet) {
        if (CommonUtils.isEmpty(numIidSet)) {
            return new ArrayList<PromotionPlay>();
        }
        
        String numIids = StringUtils.join(numIidSet, ",");
        numIids = CommonUtils.escapeSQL(numIids);
        
        if (StringUtils.isEmpty(numIids)) {
            return new ArrayList<PromotionPlay>();
        }
        
        String query = "select " + SelectAllProperty + " from " + PromotionPlay.TABLE_NAME + "%s where "
                + " userId = ?  ";
        query = appendActiveStatusRule(query);
        
        query = genShardQuery(query, userId);
        
        StringBuilder sb = new StringBuilder();
        sb.append(query);
        sb.append(" and numIid in (");
        sb.append(numIids);
        sb.append(")");
        
        
        query = sb.toString();
        
        return queryListByJDBC(query, userId);
    }
    
    
    public static List<PromotionPlay> findByNumIidsInOneActivity(Long userId, Long tmActivityId,
            Set<Long> numIidSet) {
        
        if (CommonUtils.isEmpty(numIidSet)) {
            return new ArrayList<PromotionPlay>();
        }
        
        String numIids = StringUtils.join(numIidSet, ",");
        numIids = CommonUtils.escapeSQL(numIids);
        
        if (StringUtils.isEmpty(numIids)) {
            return new ArrayList<PromotionPlay>();
        }
        
        String query = "select " + SelectAllProperty + " from " + PromotionPlay.TABLE_NAME + "%s where "
                + " userId = ? and tmActivityId = ? ";
        
        query = genShardQuery(query, userId);
        
        StringBuilder sb = new StringBuilder();
        sb.append(query);
        sb.append(" and numIid in (");
        sb.append(numIids);
        sb.append(")");
        
        
        query = sb.toString();
        
        return queryListByJDBC(query, userId, tmActivityId);
    }
    
    
    public static List<PromotionPlay> findByTMActivityId(Long userId, Long tmActivityId) {
        
        
        String query = "select " + SelectAllProperty + " from " + PromotionPlay.TABLE_NAME + "%s where "
                + " userId = ? and tmActivityId = ? ";
        
        query = genShardQuery(query, userId);
        
        
        return queryListByJDBC(query, userId, tmActivityId);
    }
    
    
    
    public static List<ItemPromotionBean> findItemActivePromotionsByRules(Long userId, Long tmActivityId,
            String itemTitle, String cid, String sellerCid, 
            boolean isMustInNumIids, Set<Long> targetNumIidSet,
            String order, PageOffset po) {
        
        if (isMustInNumIids == true && CommonUtils.isEmpty(targetNumIidSet)) {
            return new ArrayList<ItemPromotionBean>();
        }
        
        String query = " select " + SelectPropertyWithItem + " from " 
                + PromotionPlay.TABLE_NAME + "%s as p, " + ItemPlay.TABLE_NAME + "%s as i where "
                + " p.numIid = i.numIid and p.userId = ? and i.userId = ? and p.tmActivityId = ? ";
        
        query = genShardQueryTwoTable(query, userId);
        
        List<Object> paramList = new ArrayList<Object>();
        paramList.add(userId);
        paramList.add(userId);
        paramList.add(tmActivityId);
        
        query = addActivePromotionsByRulesSql(query, itemTitle, cid, sellerCid, 
                isMustInNumIids, targetNumIidSet, paramList);
        
        if ("pu".equals(order)) {
            query += " order by i.price asc ";
        } else if ("pd".equals(order)) {
            query += " order by i.price desc ";
        } else if ("su".equals(order)) {
            query += " order by i.deListTime asc ";
        } else if ("sd".equals(order)) {
            query += " order by i.deListTime desc ";
        }
        
        
        query += " limit ?, ? ";
        
        paramList.add(po.getOffset());
        paramList.add(po.getPs());
        
        Object[] paramArray = paramList.toArray();
        
        
        return new JDBCBuilder.JDBCExecutor<List<ItemPromotionBean>>(dp, query, paramArray) {

            @Override
            public List<ItemPromotionBean> doWithResultSet(ResultSet rs)
                    throws SQLException {
                
                List<ItemPromotionBean> promotionList = new ArrayList<ItemPromotionBean>();
                
                while (rs.next()) {
                    ItemPromotionBean promotion = parsePromotionPlayWithItemInfo(rs);
                    if (promotion != null) {
                        promotionList.add(promotion);
                    }
                }
                
                return promotionList;
            }
            
            
        }.call();
    }


    public static List<PhoneItem.ItemPromotionBean> findItemByRules(Long userId, Long tmActivityId,
                                                                          String itemTitle, String cid, String sellerCid,
                                                                          boolean isMustInNumIids, Set<Long> targetNumIidSet,
                                                                          String order, PageOffset po) {

        if (isMustInNumIids == true && CommonUtils.isEmpty(targetNumIidSet)) {
            return new ArrayList<PhoneItem.ItemPromotionBean>();
        }

        String query = " select " + SelectPropertyWithItem + " from "
                + PromotionPlay.TABLE_NAME + "%s as p, " + ItemPlay.TABLE_NAME + "%s as i where "
                + " p.numIid = i.numIid and p.userId = ? and i.userId = ? and p.tmActivityId = ? ";

        query = genShardQueryTwoTable(query, userId);

        List<Object> paramList = new ArrayList<Object>();
        paramList.add(userId);
        paramList.add(userId);
        paramList.add(tmActivityId);

        query = addActivePromotionsByRulesSql(query, itemTitle, cid, sellerCid,
                isMustInNumIids, targetNumIidSet, paramList);

        if ("pu".equals(order)) {
            query += " order by i.price asc ";
        } else if ("pd".equals(order)) {
            query += " order by i.price desc ";
        } else if ("su".equals(order)) {
            query += " order by i.deListTime asc ";
        } else if ("sd".equals(order)) {
            query += " order by i.deListTime desc ";
        }


        query += " limit ?, ? ";

        paramList.add(po.getOffset());
        paramList.add(po.getPs());

        Object[] paramArray = paramList.toArray();


        return new JDBCBuilder.JDBCExecutor<List<PhoneItem.ItemPromotionBean>>(dp, query, paramArray) {

            @Override
            public List<PhoneItem.ItemPromotionBean> doWithResultSet(ResultSet rs)
                    throws SQLException {

                List<PhoneItem.ItemPromotionBean> promotionList = new ArrayList<PhoneItem.ItemPromotionBean>();

                while (rs.next()) {
                    PhoneItem.ItemPromotionBean promotion = parseWithItemInfo(rs);
                    if (promotion != null) {
                        promotionList.add(promotion);
                    }
                }

                return promotionList;
            }


        }.call();
    }

    
    public static long countActivePromotionsByRules(Long userId, Long tmActivityId,
            String itemTitle, String cid, String sellerCid,
            boolean isMustInNumIids, Set<Long> targetNumIidSet) {
        
        if (isMustInNumIids == true && CommonUtils.isEmpty(targetNumIidSet)) {
            return 0;
        }
        
        String query = " select count(*) from " 
                + PromotionPlay.TABLE_NAME + "%s as p, " + ItemPlay.TABLE_NAME + "%s as i where "
                + " p.numIid = i.numIid and p.userId = ? and i.userId = ? and p.tmActivityId = ? ";
                
        
        query = genShardQueryTwoTable(query, userId);
        
        List<Object> paramList = new ArrayList<Object>();
        paramList.add(userId);
        paramList.add(userId);
        paramList.add(tmActivityId);
        
        query = addActivePromotionsByRulesSql(query, itemTitle, cid, sellerCid, 
                isMustInNumIids, targetNumIidSet, paramList);
        
        Object[] paramArray = paramList.toArray();
        
        long count = dp.singleLongQuery(query, paramArray);
        
        return count;
    }
    
    
    private static String addActivePromotionsByRulesSql(String query, 
            String itemTitle, String cid, String sellerCid, 
            boolean isMustInNumIids, Set<Long> targetNumIidSet,
            List<Object> paramList) {
        
        query += " and p.tmStatus & " + TMPromotionStatus.Active + " > 0 ";
        
        if (StringUtils.isEmpty(itemTitle) == false) {
            String like = appendTitleLikeWithAnd(itemTitle);
            query += " and " + like + " ";
        }
        if (StringUtils.isEmpty(cid) == false) {
            String like = appendItemCidsLike(cid);
            query += " and " + like + " ";
        }
        if (StringUtils.isEmpty(sellerCid) == false) {
            String like = appendSellerCidsLike(sellerCid);
            query += " and " + like + " ";
        }
        
        if (isMustInNumIids == false) {
            return query;
        }
        
        if (CommonUtils.isEmpty(targetNumIidSet)) {
            query += " and 1 != 1 ";
            return query;
        }
        String targetNumIids = StringUtils.join(targetNumIidSet, ",");
        targetNumIids = CommonUtils.escapeSQL(targetNumIids);
        if (StringUtils.isEmpty(targetNumIids)) {
            query += " and 1 != 1 ";
            return query;
        }
        
        query += " and p.numIid in (" + targetNumIids + ") ";
        
        
        return query;
        
    }
    
    public static String appendActiveStatusRule(String query) {
        query += " and tmStatus & " + TMPromotionStatus.Active + " > 0 ";
        
        return query;
    }
    
    public static String appendUnActiveStatusRule(String query) {
        query += " and tmStatus & " + TMPromotionStatus.Active + " <= 0 ";
        
        return query;
    }
    
    private static String appendItemCidsLike(String catId) {

        StringBuilder sb = new StringBuilder("(( 0 = 1)");
        String[] keys = catId.split("\\s");

        for (String split : keys) {
            if (StringUtils.isBlank(split)) {
                continue;
            }

            sb.append(" or (i.cid like '%");
            sb.append(CommonUtils.escapeSQL(split));
            sb.append("%')");
        }
        sb.append(")");

        return sb.toString();
    }

    
    private static String appendSellerCidsLike(String catId) {

        StringBuilder sb = new StringBuilder("(( 0 = 1)");
        String[] keys = catId.split("\\s");

        for (String split : keys) {
            if (StringUtils.isBlank(split)) {
                continue;
            }

            sb.append(" or (i.sellerCids like '%");
            sb.append(CommonUtils.escapeSQL(split));
            sb.append("%')");
        }
        sb.append(")");

        return sb.toString();
    }
    
    
    private static String appendTitleLikeWithAnd(String key) {

        StringBuilder sb = new StringBuilder("(( 1 = 1)");
        String[] keys = key.split("\\s");

        for (String split : keys) {
            if (StringUtils.isBlank(split)) {
                continue;
            }

            sb.append(" and (i.title like '%");
            sb.append(CommonUtils.escapeSQL(split));
            sb.append("%')");
        }
        sb.append(")");

        return sb.toString();
    }
    
    
    public static String genShardQueryTwoTable(String query, Long userId) {
        return String.format(query, DBBuilder.genUserIdHashKey(userId), DBBuilder.genUserIdHashKey(userId));
    }
    
    public static String genShardQuery(String query, Long userId) {
        query = ItemDao.genShardQuery(query, userId);
        
        return query;
    }

    private static PromotionPlay queryByJDBC(String query, Object...params) {
        
        return new JDBCBuilder.JDBCExecutor<PromotionPlay>(dp, query, params) {

            @Override
            public PromotionPlay doWithResultSet(ResultSet rs)
                    throws SQLException {
                if (rs.next()) {
                    return parsePromotionPlay(rs);
                } else {
                    return null;
                }
            }
            
            
        }.call();
        
    }
    
    
    private static List<PromotionPlay> queryListByJDBC(String query, Object...params) {
        
        return new JDBCBuilder.JDBCExecutor<List<PromotionPlay>>(dp, query, params) {

            @Override
            public List<PromotionPlay> doWithResultSet(ResultSet rs)
                    throws SQLException {
                
                List<PromotionPlay> promotionList = new ArrayList<PromotionPlay>();
                
                while (rs.next()) {
                    PromotionPlay promotion = parsePromotionPlay(rs);
                    if (promotion != null) {
                        promotionList.add(promotion);
                    }
                }
                
                return promotionList;
            }
            
            
        }.call();
        
    }
    
    
    private static final String SelectAllProperty = " promotionId,userId,"
                + "tmActivityId,numIid,"
                + "isUserTag,userTagValue,promotionType,"
                + "decreaseAmount,discountRate,tmStatus,createTs,updateTs ";
    
    
    
    private static PromotionPlay parsePromotionPlay(ResultSet rs) {
        
        try {
            
            PromotionPlay promotion = new PromotionPlay();
            
            promotion.setPromotionId(rs.getLong(1));
            promotion.setUserId(rs.getLong(2));
            promotion.setTmActivityId(rs.getLong(3));
            promotion.setNumIid(rs.getLong(4));
            promotion.setUserTag(rs.getBoolean(5));
            promotion.setUserTagValue(rs.getString(6));
            promotion.setPromotionType(ItemPromoteType.valueOf(rs.getString(7)));
            promotion.setDecreaseAmount(rs.getLong(8));
            promotion.setDiscountRate(rs.getLong(9));
            promotion.setTmStatus(rs.getInt(10));
            promotion.setCreateTs(rs.getLong(11));
            promotion.setUpdateTs(rs.getLong(12));
            
            return promotion;
            
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
        
    }
    
    
    private static final String SelectPropertyWithItem = " p.promotionId,p.numIid,"
            + "p.promotionType,"
            + "p.decreaseAmount,p.discountRate, i.picURL,i.title,i.price ";
    
    
    private static ItemPromotionBean parsePromotionPlayWithItemInfo(ResultSet rs) {
        
        try {
            
            Long promotionId = rs.getLong(1);
            Long numIid = rs.getLong(2);
            ItemPromoteType promotionType = ItemPromoteType.valueOf(rs.getString(3));
            long decreaseAmount = rs.getLong(4);
            long discountRate = rs.getLong(5);
            String picURL = rs.getString(6);
            String title = rs.getString(7);
            double price =rs.getDouble(8);
            
            ItemPromotionBean itemPromotion = new ItemPromotionBean(numIid, title, picURL, 
                    price, true, promotionId, promotionType, decreaseAmount, 
                    discountRate, true);
            
            return itemPromotion;
            
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
        
    }

    private static PhoneItem.ItemPromotionBean parseWithItemInfo(ResultSet rs) {

        try {

            Long promotionId = rs.getLong(1);
            Long numIid = rs.getLong(2);
            ItemPromoteType promotionType = ItemPromoteType.valueOf(rs.getString(3));
            long decreaseAmount = rs.getLong(4);
            long discountRate = rs.getLong(5);
            String picURL = rs.getString(6);
            String title = rs.getString(7);
            double price =rs.getDouble(8);

            PhoneItem.ItemPromotionBean itemPromotion = new PhoneItem.ItemPromotionBean(numIid, title, picURL,
                    price, true, promotionId, promotionType, decreaseAmount,
                    discountRate, true);

            return itemPromotion;


        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }

    }
    
}
