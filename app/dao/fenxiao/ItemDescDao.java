/**
 * 
 */
package dao.fenxiao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.fenxiao.ItemDescPlay;
import models.item.ItemPlay.Type;

import org.apache.commons.lang.StringUtils;

import result.TMResult;
import transaction.DBBuilder;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.DBDispatcher;

import com.ciaosir.client.pojo.PageOffset;

import dao.item.ItemDao;

/**
 * @author navins
 * @date: Nov 24, 2013 5:53:28 PM
 */
public class ItemDescDao {

    public static DBDispatcher dp = ItemDescPlay.dp;
    
    static String Select_Sql = "select numIid,userId,price,title,detail,picUrl,links,ts,status from item_desc_";

    public static class ListFetcher extends JDBCExecutor<List<ItemDescPlay>> {
        public ListFetcher(Long userId, String whereQuery, Object... params) {
            super(false, dp, whereQuery, params);
            StringBuilder sb = new StringBuilder();
            sb.append(Select_Sql);
            sb.append(DBBuilder.genUserIdHashKey(userId));
            sb.append(" where userId = ");
            sb.append(userId);
            if (!StringUtils.isBlank(whereQuery)) {
                sb.append(" and ");
                sb.append(whereQuery);
            }
            this.src = dp.getDataSrc(userId);
            this.query = sb.toString();
        }

        @Override
        public List<ItemDescPlay> doWithResultSet(ResultSet rs) throws SQLException {
            List<ItemDescPlay> list = new ArrayList<ItemDescPlay>();
            while (rs.next()) {
                list.add(new ItemDescPlay(rs));
            }
            return list;
        }
    }

    public static int count(Long userId, String whereQuery, Object... params) {
        StringBuilder sb = new StringBuilder();
        sb.append("select count(*) from item_desc_");
        sb.append(DBBuilder.genUserIdHashKey(userId));
        sb.append(" where userId = ");
        sb.append(userId);
        if (!StringUtils.isBlank(whereQuery)) {
            sb.append(" and ");
            sb.append(whereQuery);
        }
        return (int) JDBCBuilder.singleLongQuery(dp.getDataSrc(userId), sb.toString(), params);
    }
    
    public static List<ItemDescPlay> findByUserId(Long userId) {
        List<ItemDescPlay> list = new ListFetcher(userId, "").call();
        return list;
    }
    
    public static List<ItemDescPlay> findByUserIdNumIids(Long userId, String numIids) {
        List<ItemDescPlay> list = new ListFetcher(userId, "numIid in (" + numIids + ")").call();
        return list;
    }
    
    public static TMResult findTmResult(Long userId, PageOffset po) {
        List<ItemDescPlay> list = new ListFetcher(userId, "userId = ? and links is not null limit ? offset ?", userId, po.getPs(), po.getOffset()).call();
        int count = count(userId, "userId = ? and links is not null", userId);
        return new TMResult(list, count, po);
    }
    
    static String Delete_Sql = "delete from item_desc_%s where userId = ?"; 
    public static void deleteItemDesc(Long userId) {
        String sql = ItemDescPlay.genShardQuery(Delete_Sql, userId);
        dp.update(sql, userId);
    }
    
    
    
    public static TMResult findByItemRulesWithPaging(Long userId, String title, String catId, String sellerCatId, 
            int status, PageOffset po) {
        
        String query = genItemRulesQuery(userId, title, catId, sellerCatId, status);
        
        List<ItemDescPlay> list = new ListFetcher(userId, query + " limit ?, ? ", 
                userId, status, po.getOffset(), po.getPs()).call();
        int count = count(userId, query, userId, status);
        
        return new TMResult(list, count, po);
        
    }
    
    public static List<ItemDescPlay> findByItemRules(Long userId, String title, 
            String catId, String sellerCatId, 
            int status) {
        String query = genItemRulesQuery(userId, title, catId, sellerCatId, status);
        
        List<ItemDescPlay> list = new ListFetcher(userId, query, 
                userId, status).call();
        
        return list;
    }
    
    
    private static String genItemRulesQuery(Long userId, String title, String catId, String sellerCatId, 
            int status) {
        
        String query = " numIid in (select numIid from item%s where userId = ? and status <> ? " +
                "  ";

        query = ItemDao.genShardQuery(query, userId);

        if (!StringUtils.isEmpty(title)) {
            String like = ItemDao.appendTitleLike(title);
            query += " and " + like;
        }
        if (!StringUtils.isEmpty(catId)) {
            String like = ItemDao.appendItemCidsLike(catId);
            query += " and " + like;
        }

        if (!StringUtils.isEmpty(sellerCatId)) {
            String like = ItemDao.appendSellerCidsLike(catId);
            query += " and " + like;
        }
        
        query += ") ";
        
        return query;
        
    }
}
