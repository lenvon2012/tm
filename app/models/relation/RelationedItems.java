
package models.relation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

import models.user.User;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = RelationedItems.TABLE_NAME)
public class RelationedItems extends Model implements PolicySQLGenerator {

    public static RelationedItems EMPTY = new RelationedItems();

    public static DBDispatcher dp = new DBDispatcher(DataSrc.QUOTA, EMPTY);

    @Transient
    private static final Logger log = LoggerFactory.getLogger(RelationedItems.class);

    @Transient
    public static final String TABLE_NAME = "relationed_items";

    @Index(name = "numIid")
    private Long numIid;

    @Index(name = "userId")
    private Long userId;

    public RelationedItems() {
        super();
    }

    public RelationedItems(Long userId, Long numIid) {
        this.userId = userId;
        this.numIid = numIid;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setNumIid(Long numIid) {
        this.numIid = numIid;
    }

    public Long getNumIid() {
        return this.numIid;
    }

    public static void add(User user, long numIid2) {
        //Popularized item = Popularized.find("userId = ? and numIid = ?", user.getId(), numIid2).first();
    	RelationedItems item = RelationedItems.findByNumIid(user.getId(), numIid2);
        if (item == null) {
            new RelationedItems(user.getId(), numIid2).jdbcSave();
        }
    }

    public static void remove(User user, long numIid) {
        dp.update(true, " delete from " + RelationedItems.TABLE_NAME + " where numIid = ? and userId = ? ",
                numIid, user.getId());
    }

    public static void removeAll(User user) {
        //Popularized.delete("userId = ?", user.getId());
    	dp.update(true, " delete from " + RelationedItems.TABLE_NAME + " where userId = ? ",
                 user.getId());
    }

    public static RelationedItems findByNumIid(Long userId, Long numIid) {

        String query = "select " + SelectAllProperties + " from " + RelationedItems.TABLE_NAME
                + " where userId = ? and numIid = ?";

        return new JDBCBuilder.JDBCExecutor<RelationedItems>(dp, query, userId, numIid) {

            @Override
            public RelationedItems doWithResultSet(ResultSet rs)
                    throws SQLException {

                if (rs.next()) {

                    return parseRelationedItems(rs);

                } else {
                    return null;
                }

            }

        }.call();
    }
    
    public static RelationedItems findFirstByNumIid(Long numIid) {

        String query = "select " + SelectAllProperties + " from " + RelationedItems.TABLE_NAME
                + " where numIid = ? limit 1";

        return new JDBCBuilder.JDBCExecutor<RelationedItems>(dp, query, numIid) {

            @Override
            public RelationedItems doWithResultSet(ResultSet rs)
                    throws SQLException {

                if (rs.next()) {

                    return parseRelationedItems(rs);

                } else {
                    return null;
                }

            }

        }.call();
    }
    
    public static RelationedItems findFirstByUserId(Long userId) {

        String query = "select " + SelectAllProperties + " from " + RelationedItems.TABLE_NAME
                + " where userId = ? limit 1";

        return new JDBCBuilder.JDBCExecutor<RelationedItems>(dp, query, userId) {

            @Override
            public RelationedItems doWithResultSet(ResultSet rs)
                    throws SQLException {

                if (rs.next()) {

                    return parseRelationedItems(rs);

                } else {
                    return null;
                }

            }

        }.call();
    }
    
    public static List<RelationedItems> findAllRelationed() {

        String query = "select " + SelectAllProperties + " from " + RelationedItems.TABLE_NAME;

        return new JDBCBuilder.JDBCExecutor<List<RelationedItems>>(dp, query) {

            @Override
            public List<RelationedItems> doWithResultSet(ResultSet rs)
                    throws SQLException {
            	List<RelationedItems> list = new ArrayList<RelationedItems>();
                while (rs.next()) {
                    list.add(parseRelationedItems(rs));
                } 
                return list;
            }

        }.call();
    }
    
    public static List<Long> findAllRelationedNumIids() {

        String query = "select numIid from " + RelationedItems.TABLE_NAME;

        return new JDBCBuilder.JDBCExecutor<List<Long>>(dp, query) {

            @Override
            public List<Long> doWithResultSet(ResultSet rs)
                    throws SQLException {
            	List<Long> list = new ArrayList<Long>();
                while (rs.next()) {
                    list.add(rs.getLong(1));
                } 
                return list;
            }

        }.call();
    }
    
    private static final String SelectAllProperties = " userId,numIid";

    private static RelationedItems parseRelationedItems(ResultSet rs) {
        try {
        	RelationedItems listCfg = new RelationedItems(rs.getLong(1),rs.getLong(2));
            return listCfg;

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    } 

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getId()
     */
    @Override
    public Long getId() {
        // TODO Auto-generated method stub
        return id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getIdColumn()
     */
    @Override
    public String getIdColumn() {
        // TODO Auto-generated method stub
        return "id";
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getIdName()
     */
    @Override
    public String getIdName() {
        // TODO Auto-generated method stub
        return "id";
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getTableHashKey()
     */
    @Override
    public String getTableHashKey() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getTableName()
     */
    @Override
    public String getTableName() {
        // TODO Auto-generated method stub
        return TABLE_NAME;
    }

    static String EXIST_ID_QUERY = "select id from " + TABLE_NAME + " where userId = ? and numIid = ?";

    private static long findExistId(Long userId, Long numIid) {
        return dp.singleLongQuery(EXIST_ID_QUERY, userId, numIid);
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#jdbcSave()
     */
    @Override
    public boolean jdbcSave() {

        try {
            long existdId = findExistId(this.userId, this.numIid);
//            if (existdId != 0)
//                log.info("find existed Id: " + existdId);

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

    public boolean rawInsert() {
        long id = dp
                .insert("insert into `relationed_items`(`userId`,`numIid`) values(?,?)",
                        this.userId, this.numIid);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.userId + "[numIid : ]" + this.numIid);
            return false;
        }

    }

    public boolean rawUpdate() {
        long updateNum = dp
                .insert("update `relationed_items` set  `userId` = ?, `numIid` = ? where `id` = ? ",
                        this.userId, this.numIid, this.id);
        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for :" + this.id + "[userId : ]" + this.userId + "[numIid : ]" + this.numIid);

            return false;
        }
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public void setId(Long id) {
        this.id = id;
    }
}
