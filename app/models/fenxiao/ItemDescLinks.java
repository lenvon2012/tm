/**
 * 
 */
package models.fenxiao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import result.TMResult;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import utils.TBItemUtil;

import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;

/**
 * @author navins
 * @date: Nov 24, 2013 4:16:46 PM
 */
@Entity(name = ItemDescLinks.TABLE_NAME)
@JsonIgnoreProperties(value = { "entityId", "tableHashKey", "persistent", "tableName", "idName", "idColumn",
        "propsName" })
public class ItemDescLinks extends Model implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(ItemDescLinks.class);

    public static final String TABLE_NAME = "item_desc_links";
    
    @Transient
    public static ItemDescLinks _instance = new ItemDescLinks();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.QUOTA, _instance);

    
    @Index(name = "user_id")
    private Long userId;

    private String link;

    //@Index(name = "action")
    private Long action;

    private Long status;

    private Long ts;

    public static class ActionType {
        public static final long DO_NOTHING = 0;

        public static final long REMOVE_IT = 1;
    }
    
    public ItemDescLinks() {
        super();
    }

    public ItemDescLinks(Long userId, String link, Long action) {
        super();
        this.userId = userId;
        this.link = link;
        this.action = action;
        this.status = 0L;
        this.ts = System.currentTimeMillis();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Long getAction() {
        return action;
    }

    public void setAction(Long action) {
        this.action = action;
    }

    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }
    

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getTableHashKey() {
        return null;
    }

    @Override
    public String getIdColumn() {
        return "id";
    }

    @Override
    public String getIdName() {
        return "id";
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }
    

    public static long findExistId(Long userId, String link) {
        String query = " select id from " + TABLE_NAME + " where userId = ? and link = ? ";
        return dp.singleLongQuery(query, userId, link);
    }

    @Override
    public boolean jdbcSave() {

        try {
            long existId = findExistId(this.userId, this.link);

            if (existId <= 0L) {
                return rawInsert();
            } else {
                this.setId(existId);
                return rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }

    }
    
    public boolean rawDelete() {
        String deleteSql = "delete from " + TABLE_NAME + " where id = ? ";
        
        
        long deleteNum = dp.update(deleteSql, this.id);
        
        return deleteNum > 0;
    }

    public boolean rawInsert() {
        
        String insertSQL = "insert into `" + TABLE_NAME + "`(`userId`," +
        		"`link`,`action`,`status`,`ts`) " +
        		" values(?,?,?,?,?)";
        
        this.ts = System.currentTimeMillis();
        
        
        long id = dp.insert(insertSQL, this.userId,
                this.link, this.action, this.status, this.ts);

        if (id >= 0L) {
            this.setId(id);
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.userId);
            return false;
        }

    }

    public boolean rawUpdate() {

        String updateSQL = "update `" + "` set  `userId` = ?, " +
        		"`link` = ?, `action` = ?, `status` = ?, `ts` = ? " +
        		" where `id` = ? ";

        this.ts = System.currentTimeMillis();
        
        long updateNum = dp.update(updateSQL, this.userId,
                this.link, this.action, this.status, this.ts, 
                this.getId());

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for :" + this.getId() + "[userId : ]" + this.userId);
            return false;
        }
    }
    

    @Override
    public String toString() {
        return "ItemDescLinks [userId=" + userId + ", link=" + link + ", action=" + action + ", status=" + status
                + ", ts=" + ts + "]";
    }

    public static List<ItemDescLinks> findLinksByUserId(Long userId) {
        /*List<ItemDescLinks> list = ItemDescLinks.find("userId = ?", userId).fetch();
        return list;*/
        
        String query = " select " + SelectAllProperty + " from " + TABLE_NAME 
                + " where userId = ? ";
        
        List<ItemDescLinks> descLinkList = findListByJDBC(query, userId);
        
        return descLinkList;
        
    }
    
    public static TMResult findLinksLike(Long userId, String link, PageOffset po) {
        
        String searchRule = " from " + TABLE_NAME 
                + " where userId = ? ";
        
        if (StringUtils.isEmpty(link) == false) {
            link = CommonUtils.escapeSQL(link);
            searchRule += " and link like '%" + link + "%' ";
        }
        
        
        List<ItemDescLinks> descLinkList = findListByJDBC(" select " + SelectAllProperty + " "
                + searchRule + " limit ?, ? ", 
                userId, po.getOffset(), po.getPs());
        
        long count = dp.singleLongQuery(" select count(*) " + searchRule, userId);
        
        return new TMResult(descLinkList, (int) count, po);
        
    }
    /*
    public static List<ItemDescLinks> findAllLinksLike(Long userId, String link) {
        
        String query = " select " + SelectAllProperty + " from " + TABLE_NAME 
                + " where userId = ? ";
        
        if (StringUtils.isEmpty(link) == false) {
            link = CommonUtils.escapeSQL(link);
            query += " and link like '%" + link + "%' ";
        }
        
        
        List<ItemDescLinks> descLinkList = findListByJDBC(query, userId);
        
        return descLinkList;
        
    }
*/
    public static List<ItemDescLinks> findLinksByUserIdAction(Long userId, Long action) {
        /*List<ItemDescLinks> list = ItemDescLinks.find("userId = ? and action = ?", userId, action).fetch();
        return list;*/
        
        String query = " select " + SelectAllProperty + " from " + TABLE_NAME 
                + " where userId = ? and action = ? ";
        
        List<ItemDescLinks> descLinkList = findListByJDBC(query, userId, action);
        
        return descLinkList;
    }
    
    public static Set<String> findLinkSet(Long userId) {
        /*List<ItemDescLinks> list = ItemDescLinks.find("userId = ? ", userId).fetch();
        HashSet<String> linkSet = new HashSet<String>();
        if (CommonUtils.isEmpty(list)) {
            return linkSet;
        }
        for (ItemDescLinks item : list) {
            linkSet.add(item.getLink());
        }
        return linkSet;*/
        
        String query = " select link from " + TABLE_NAME + " where userId = ? ";
        
        return new JDBCBuilder.JDBCSetStringExecutor(dp, query, userId).call();
        
    }

    public static void deleteLinksByUserId(Long userId) {
        /*ItemDescLinks.delete("userId = ?", userId);*/
        
        String query = " delete from " + TABLE_NAME + " where userId = ? ";
        
        dp.update(query, userId);
        
    }
    
    public static boolean updateAllLinkAction(Long userId, String link, Long action) {
        
        
        
        String sql = " update " + TABLE_NAME + " set action = " + action + " where "
                + " userId = ? ";
        if (StringUtils.isEmpty(link) == false) {
            link = CommonUtils.escapeSQL(link);
            sql += " and link like '%" + link + "%' ";
        }
        
        long updateNum = dp.update(sql, userId);
        
        if (updateNum > 0) {
            return true;
        } else {
            return false;
        }
        
    }

    public static Boolean updateLinkAction(Long userId, String ids, Long action) {
        /*List<ItemDescLinks> descLinks = ItemDescLinks.find("userId = ? and id in (" + ids + ")", userId).fetch();
        if (CommonUtils.isEmpty(descLinks)) {
            return Boolean.FALSE;
        }
        for (ItemDescLinks itemDescLinks : descLinks) {
            itemDescLinks.setAction(action);
            itemDescLinks.save();
        }
        return Boolean.TRUE;*/
        
        
        Set<Long> idSet = TBItemUtil.parseIdsToSet(ids);
        
        if (CommonUtils.isEmpty(idSet)) {
            return false;
        }
        
        String idRules = StringUtils.join(idSet, ",");
        idRules = CommonUtils.escapeSQL(idRules);
        
        String sql = " update " + TABLE_NAME + " set action = " + action + " where "
                + " userId = ? and id in (" + idRules + ") ";
        
        long updateNum = dp.update(sql, userId);
        
        if (updateNum > 0) {
            return true;
        } else {
            return false;
        }
        
    }
    
    public static Boolean updateLinkActionSingle(Long userId, Long id, Long action) {
        /*ItemDescLinks itemLink = ItemDescLinks.find("userId = ? and id = ?", userId, id).first();
        if (itemLink == null) {
            return Boolean.FALSE;
        }
        itemLink.setAction(action);
        itemLink.save();
        return Boolean.TRUE;*/
        
        String sql = " update " + TABLE_NAME + " set action = " + action + " where "
                + " userId = ? and id = ? ";
        
        long updateNum = dp.update(sql, userId, id);
        
        if (updateNum > 0) {
            return true;
        } else {
            return false;
        }
        
    }
    
    
    private static List<ItemDescLinks> findListByJDBC(String query, Object...params) {
        
        
        return new JDBCBuilder.JDBCExecutor<List<ItemDescLinks>>(dp, query, params) {

            @Override
            public List<ItemDescLinks> doWithResultSet(ResultSet rs)
                    throws SQLException {
                List<ItemDescLinks> descLinkList = new ArrayList<ItemDescLinks>();
                
                while (rs.next()) {
                    ItemDescLinks descLink = parseItemDescLinks(rs);
                    
                    if (descLink != null) {
                        descLinkList.add(descLink);
                    }
                }
                
                return descLinkList;
                
            }
            
            
            
        }.call();
        
    }
    
    
    private static final String SelectAllProperty = " id,userId," +
                "link,action,status,ts ";
    
    private static ItemDescLinks parseItemDescLinks(ResultSet rs) {
        
        try {
            
            ItemDescLinks descLink = new ItemDescLinks();
            
            descLink.setId(rs.getLong(1));
            descLink.setUserId(rs.getLong(2));
            descLink.setLink(rs.getString(3));
            descLink.setAction(rs.getLong(4));
            descLink.setStatus(rs.getLong(5));
            descLink.setTs(rs.getLong(6));
            
            return descLink;
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
        
    }

}
