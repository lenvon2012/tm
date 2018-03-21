package dao.popularized;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.vgouitem.VGouItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;

public class VGItemDao {
    private static final Logger log = LoggerFactory.getLogger(VGItemDao.class);
 
    
    public static long findIdByNumIid(Long userId, Long numIid) {
        if (userId == null || numIid <= 0)
            return 0;
        if (numIid == null || numIid <= 0)
            return 0;
        String sql = " select id from " + VGouItem.TABLE_NAME + " where uid = ? and numIid = ? ";
        long vgItemId = JDBCBuilder.singleLongQuery(sql, userId, numIid);
        
        return vgItemId;
    }
    
    public static boolean deleteVGouItemByUserId(Long userId) {
        String sql = "delete from " + VGouItem.TABLE_NAME + " where uid=?";
        
        long res = JDBCBuilder.insert(sql, userId);
        
        if (res == 1) {
            return true;
        } else {
            log.error("delete failed...for VGouItem: [userId : ]" + userId);
            return false;
        }
    }
    
    
    public static List<VGouItem> queryVGouItemsByUserId(Long userId) {
        if (userId == null || userId <= 0)
            return new ArrayList<VGouItem>();
        String sql = SelectVGItemSql + " uid = ? ";
        List<VGouItem> itemList = queryListByJDBC(sql, userId);
        
        return itemList;
    }
    
    private static final String UpdatePricePropertiesSql = " update " + VGouItem.TABLE_NAME + " " +
    		" set `title` = ?, `img` = ?, `price` = ?, `skuMinPrice` = ? where `id` = ? and numIid= ? ";
    
    /**
     * 在更新数据时，更新宝贝后，更新VGItem中标题、价格、图片等信息
     * @param item
     */
    public static boolean updateVGItemPriceProperties(VGouItem item) {
        if (item == null)
            return false;
        
        long res = JDBCBuilder.insert(UpdatePricePropertiesSql, item.getTitle(), item.getImg(), 
                item.getPrice(), item.getSkuMinPrice(), item.getId(), item.getNumIid());
        
        if (res == 1) {
            return true;
        } else {
            log.error("update failed...for VGouItem: [numIid : ]" + item.getNumIid());
            return false;
        }
    }
    
    
    /**
     * 使用JDBC查询
     * 
     */
    private static final String VGItemProperties = " id, cid, item_key, " +
    		" title, img, simg, bimg, price, url, sid, hits, likes, browse_num, haves, " +
    		" comments, comments_last, is_index, status, add_time, uid, seo_title, seo_keys, " +
    		" sort_order, sort_desc, cash_back_rate, seller_name, remark, remark_status, id_collect_comments, " +
    		" numIid, skuMinPrice  ";
    
    private static final String SelectVGItemSql = " select " + VGItemProperties + " from " + VGouItem.TABLE_NAME + " where ";

    private static VGouItem parseVGItem(ResultSet rs) {
        try {
            VGouItem item = new VGouItem();
            item.setId(rs.getLong(1));
            item.setCid(rs.getInt(2));
            item.setItem_key(rs.getString(3));
            item.setTitle(rs.getString(4));
            item.setImg(rs.getString(5));
            item.setSimg(rs.getString(6));
            item.setBimg(rs.getString(7));
            item.setPrice(rs.getDouble(8));
            item.setUrl(rs.getString(9));
            item.setSid(rs.getInt(10));
            item.setHits(rs.getInt(11));
            item.setLikes(rs.getInt(12));
            item.setBrowse_num(rs.getInt(13));
            item.setHaves(rs.getInt(14));
            item.setComments(rs.getInt(15));
            item.setComments_last(rs.getString(16));
            item.setIs_index(rs.getInt(17));
            item.setStatus(rs.getInt(18));
            item.setAdd_time(rs.getInt(19));
            item.setUid(rs.getLong(20));
            item.setSeo_title(rs.getString(21));
            item.setSeo_keys(rs.getString(22));
            item.setSort_order(rs.getInt(23));
            item.setSort_desc(rs.getString(24));
            item.setCash_back_rate(rs.getString(25));
            item.setSeller_name(rs.getString(26));
            item.setRemark(rs.getString(27));
            item.setRemark_status(rs.getInt(28));
            item.setId_collect_cooments(rs.getInt(29));
            item.setNumIid(rs.getLong(30));
            item.setSkuMinPrice(rs.getDouble(31));
            
            return item;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }
    
    

    
    private static VGouItem queryByJDBC(String sql, Object...prams) {
        return new JDBCExecutor<VGouItem>(sql, prams) {
            @Override
            public VGouItem doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return parseVGItem(rs);
                }
                return null;
            }
        }.call();
    }
    
    private static List<VGouItem> queryListByJDBC(String sql, Object...prams) {
        return new JDBCExecutor<List<VGouItem>>(sql, prams) {
            @Override
            public List<VGouItem> doWithResultSet(ResultSet rs) throws SQLException {
                List<VGouItem> itemList = new ArrayList<VGouItem>();
                while (rs.next()) {
                    VGouItem item = parseVGItem(rs);
                    if (item != null)
                        itemList.add(item);
                }
                return itemList;
            }
        }.call();
    }
    
    
}
