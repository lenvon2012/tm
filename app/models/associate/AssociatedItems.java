package models.associate;

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

import actions.AssociateAction;

import play.db.jpa.Model;
import transaction.JDBCBuilder;
import transaction.DBBuilder.DataSrc;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;
import dao.item.ItemDao;

/**
 * 被关联的宝贝
 * 
 * @author hyg 2014-4-3下午7:56:21
 */
@Entity(name=AssociatedItems.TABLE_NAME)
public class AssociatedItems extends Model implements PolicySQLGenerator {

    public static AssociatedItems EMPTY = new AssociatedItems();

    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

    @Transient
    private static final Logger log = LoggerFactory.getLogger(AssociatedItems.class);

    @Transient
    public static final String TABLE_NAME = "associated_items";

    /**
     * 卖家Id
     */
    @Index(name = "userId")
    private Long userId;

    /**
     * 宝贝Id
     */
    private Long numIid;

    /**
     * 模板计划
     */
    private Long planId;
    
    public AssociatedItems() {
        super();
    }

    public AssociatedItems(Long userId, Long numIid, Long planId) {
        this.userId = userId;
        this.numIid = numIid;
        this.planId = planId;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getNumIid() {
        return numIid;
    }

    public void setNumIid(Long numIid) {
        this.numIid = numIid;
    }

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }
    
    /**
     * 通过planId查找关联宝贝
     * @param planId
     * @param userId
     * @return
     * Long userId, Long numIid, Long planId
     */
    public static List<AssociatedItems> findByPlanId(Long planId,Long userId) {
        String query = "select id, userId, numIid, planId from " + AssociatedItems.TABLE_NAME + " where planId=" + planId
                + " and userId=" + userId;

        return new JDBCBuilder.JDBCExecutor<List<AssociatedItems>>(dp, query) {

            @Override
            public List<AssociatedItems> doWithResultSet(ResultSet rs) throws SQLException {
                List<AssociatedItems> list = new ArrayList<AssociatedItems>();
                while (rs.next()) {
                    AssociatedItems items = new AssociatedItems(rs.getLong(2),rs.getLong(3),rs.getLong(4));
                    items.setId(rs.getLong(1));
                    list.add(items);
                }
                return list;
            }
        }.call();
    }
    
    public static int getPlanCountByNumIid(Long numIid,Long userId){
        
        String sql = "select count(*) from " + AssociatedItems.TABLE_NAME +" where numIid = ? and userId = ?";
        
        int count = AssociateAction.ZERO;
        
        count = (int)dp.singleLongQuery(sql, numIid,userId);
        
        return count;
    }
    
    /**
     * 
     * @param numIid
     * @param userId
     * @param planId
     * @return
     */
    public static AssociatedItems findByNumIidAndPlanId(Long numIid,Long userId,Long planId){
        String query ="select id, numIid, planId, userId from " + AssociatedItems.TABLE_NAME + " where planId=" + planId 
                + " and userId=" + userId + " and numIid=" + numIid;
        return new JDBCBuilder.JDBCExecutor<AssociatedItems>(dp,query){

            @Override
            public AssociatedItems doWithResultSet(ResultSet rs) throws SQLException {
                if(rs.next()){
                    AssociatedItems item = new AssociatedItems(rs.getLong(2),rs.getLong(3),rs.getLong(4));
                    item.setId(rs.getLong(1));
                    return item;
                }else{
                    return null;
                }
            }
        }.call();
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public void setId(Long id) {
        this.id = id;
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

    static String EXIST_ID_QUERY = "select id from " + TABLE_NAME + " where userId = ? and numIid = ? and planId = ?";

    private static long findExistId(Long userId, Long numIid,Long planId) {
        return dp.singleLongQuery(EXIST_ID_QUERY, userId, numIid,planId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#jdbcSave()
     */
    @Override
    public boolean jdbcSave() {
        try {
            long existdId = findExistId(userId, numIid,planId);
            // if (existdId != 0)
            // log.info("find existed Id: " + existdId);

            if (existdId == 0L) {
                return rawInsert(userId,numIid,planId);
            } else {
                id = existdId;
                return rawUpdate(userId,numIid,planId,id);
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }
    }
    
    public boolean rawInsert(Long useId,Long numIid,Long planId) {
        long id = dp.insert("insert into `associated_items`(`userId`,`numIid`,`planId`) values(?,?,?)", userId, numIid,planId);
        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Failed....." + "[userId : ]" + userId + "[numIid : ]" + numIid + "[planId : ]" + planId);
            return false;
        }

    }

    public boolean rawUpdate(Long userId,Long numIid,Long planId,Long id) {
        long updateNum = dp.insert("update `associated_items` set  `userId` = ?, `numIid` = ? ,`planId` = ?  where `id` = ? ",
                userId, numIid, planId, id);
        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for :" + id + "[userId : ]" + userId + "[numIid : ]" + numIid);
            return false;
        }
    }
    
    
    public boolean rawDelete(Long userId,Long numIid,Long planId) {
        
        String deleteSql = "delete from " + AssociatedItems.TABLE_NAME + " where planId = ? and userId = ? and numIid = ?";
        
        long deleteNum = dp.update(deleteSql, planId, userId, numIid);
        
        if (deleteNum > 0) {
            return true;
        } else {
            return false;
        }
    }

}
