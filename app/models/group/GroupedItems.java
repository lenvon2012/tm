package models.group;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import controllers.Items;

import play.db.jpa.Model;

import transaction.JDBCBuilder;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;


@Entity(name = GroupedItems.TABLE_NAME)
@JsonIgnoreProperties(value = {"tableName", "idColumn", "idName", "tableHashKey", "persistent", "entityId" })
public class GroupedItems extends Model implements PolicySQLGenerator {

    @Transient
    public static GroupedItems EMPTY = new GroupedItems();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

    @Transient
    private static Logger log = LoggerFactory.getLogger(GroupedItems.class);

    @Transient
    public static final String TABLE_NAME = "grouped_Items";

    @Transient
    private String picURL;
    
    @Transient
    private double price;
    
    @Transient
    private String title;
    
    private Long planId;

    private Long numIid;

    private Long userId;

    /**
     * 1 未投放 2 投放成功 3 投放失敗 4 退出投放失敗
     */
    private int status;

    private String errorMsg;

    public GroupedItems() {
        super();
    }

    /**
     * 保存
     */
    public GroupedItems(Long planId, Long numIid, Long userId, int status) {
        this.planId = planId;
        this.numIid = numIid;
        this.userId = userId;
        this.status = status;
    }
    
    public GroupedItems(Long planId,Long numIid,Long userId,int status,String errorMsg){
        this.planId = planId;
        this.numIid = numIid;
        this.userId = userId;
        this.status = status;
        this.errorMsg = errorMsg;
    } 
    
    
    public static int getItemCountByStatus(Long userId,Long planId,int status){
        if(status == 0){
            String sql = "select count(*) from " + GroupedItems.TABLE_NAME + " where planId = ? and userId = ? and status in (3,4)";
            int count = (int) dp.singleLongQuery(sql, planId,userId);
            return count;
        }
        else{
            String sql = "select count(*) from " + GroupedItems.TABLE_NAME + " where planId = ?  and userId = ? and status = ?";
            int count = (int) dp.singleLongQuery(sql, planId, userId,status);
            return count;
        }
        
    }
    
    public static int getPlanCountByNumIids(Long numIid,Long userId){
        String sql = "select count(*) from " + GroupedItems.TABLE_NAME + " where userId = ? and numIid = ? and status in (2,4)";
        int count = (int) dp.singleLongQuery(sql, userId,numIid); 
        return count;
        
    }
    
    public static int getGrouedItemsCountByPlanId(Long planId,Long userId){
        String sql = "select count(*) from " + GroupedItems.TABLE_NAME + " where planId = ? and userId = ?";
        int count = (int) dp.singleLongQuery(sql, planId,userId);
        return count;
    }
    
    public static List<Long> findNumIidsByPlanId(Long userId,Long planId){
        
        String sql = "select numIid from " + GroupedItems.TABLE_NAME + " where userId = ? and planId = ? ";
        
        return new JDBCExecutor<List<Long>>(dp, sql, userId,planId) {

            @Override
            public List<Long> doWithResultSet(ResultSet rs) throws SQLException {

                List<Long> numIidsList = new ArrayList<Long>();

                while (rs.next()) {
                    numIidsList.add(rs.getLong(1));
                }

                return numIidsList;
            }
        }.call();
    }
    
    
    public static List<Long> findNumIidsByPlanIdAndStatus(Long userId,Long planId,int status){
       
        String sql = "select numIid from " + GroupedItems.TABLE_NAME + " where userId = ? and planId = ? and status = ?";
        
        return new JDBCExecutor<List<Long>>(dp, sql, userId,planId,status) {

            @Override
            public List<Long> doWithResultSet(ResultSet rs) throws SQLException {

                List<Long> numIidsList = new ArrayList<Long>();

                while (rs.next()) {
                    numIidsList.add(rs.getLong(1));
                }

                return numIidsList;
            }
        }.call();
    }
    
    public static List<Long> findNumIidsSucceed(Long userId,Long planId){
        
        String sql = "select numIid from " + GroupedItems.TABLE_NAME + " where userId = ? and planId = ? and status in (2,4)"; 
        
        return new JDBCExecutor<List<Long>>(dp, sql, userId,planId) {

            @Override
            public List<Long> doWithResultSet(ResultSet rs) throws SQLException {

                List<Long> numIidsList = new ArrayList<Long>();

                while (rs.next()) {
                    numIidsList.add(rs.getLong(1));
                }

                return numIidsList;
            }
        }.call();
    }
        
    public static List<Long> findNumIidsFailed(Long userId,Long planId){
        
        String sql = "select numIid from " + GroupedItems.TABLE_NAME + " where userId = ? and planId = ? and status <> 2"; 
        
        return new JDBCExecutor<List<Long>>(dp, sql, userId,planId) {

            @Override
            public List<Long> doWithResultSet(ResultSet rs) throws SQLException {

                List<Long> numIidsList = new ArrayList<Long>();

                while (rs.next()) {
                    numIidsList.add(rs.getLong(1));
                }

                return numIidsList;
            }
        }.call();
    }
    
    public static List<Long> findNumIidsByPlanId(Long userId,Long planId,int status){
        
        String sql = "select numIid from " + GroupedItems.TABLE_NAME + " where userId = ? and planId = ? and status <> ?";
        
        return new JDBCExecutor<List<Long>>(dp, sql, userId,planId,status) {

            @Override
            public List<Long> doWithResultSet(ResultSet rs) throws SQLException {

                List<Long> numIidsList = new ArrayList<Long>();

                while (rs.next()) {
                    numIidsList.add(rs.getLong(1));
                }

                return numIidsList;
            }
        }.call();
    }
    
    static String ALL_QUERY = "select id,planId,numIid,userId,status,errorMsg from " + GroupedItems.TABLE_NAME;
    
    public static GroupedItems findOneGroupedItem(Long userId,Long planId,Long numIid){
        
        String sql = ALL_QUERY + " where userId = ? and numIid = ? and planId = ?";
        
        return new JDBCExecutor<GroupedItems>(dp, sql, userId,numIid,planId) {

            @Override
            public GroupedItems doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    GroupedItems item = new GroupedItems(rs.getLong(2),rs.getLong(3),rs.getLong(4),rs.getInt(5),rs.getString(6));
                    item.setId(rs.getLong(1));
                    return item;
                }else{
                    return new GroupedItems();
                }
            }
        }.call();
    }
    
    public static GroupedItems findOneGroupedItemStatus(Long userId,Long planId,Long numIid){
        
        String sql = ALL_QUERY + " where userId = ? and numIid = ? and planId = ?";
        
        return new JDBCExecutor<GroupedItems>(dp, sql, userId,numIid,planId) {

            @Override
            public GroupedItems doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    GroupedItems item = new GroupedItems(rs.getLong(2),rs.getLong(3),rs.getLong(4),rs.getInt(5),rs.getString(6));
                    item.setId(rs.getLong(1));
                    return item;
                }else{
                    return new GroupedItems();
                }
            }
        }.call();
    }
    
    public static List<GroupedItems> findGroupedItems(Long userId,Long planId,int status){
        if(status == 0){
            String sql = ALL_QUERY + " where userId = ? and planId = ?";
            return new JDBCExecutor<List<GroupedItems>>(dp, sql, userId,planId) {

                @Override
                public List<GroupedItems> doWithResultSet(ResultSet rs) throws SQLException {
                    final List<GroupedItems> list = new ArrayList<GroupedItems>();
                    while (rs.next()) {
                        GroupedItems item = new GroupedItems(rs.getLong(2),rs.getLong(3),rs.getLong(4),rs.getInt(5),rs.getString(6));
                        item.setId(rs.getLong(1));
                        list.add(item);
                    }
                    return list;
                }
            }.call();
        }else{
            String sql = ALL_QUERY + " where userId = ? and planId = ? and status = ?";
            return new JDBCExecutor<List<GroupedItems>>(dp, sql, userId,planId,status) {

                @Override
                public List<GroupedItems> doWithResultSet(ResultSet rs) throws SQLException {
                    final List<GroupedItems> list = new ArrayList<GroupedItems>();
                    while (rs.next()) {
                        GroupedItems item = new GroupedItems(rs.getLong(2),rs.getLong(3),rs.getLong(4),rs.getInt(5),rs.getString(6));
                        item.setId(rs.getLong(1));
                        list.add(item);
                    }
                    return list;
                }
            }.call();
        }
    }
    
    public static List<GroupedItems> findGroupedItemsPage(Long userId,Long planId,int status,int offset,int limit){
        if(status == 0){
            String sql = ALL_QUERY + " where userId = ? and planId = ? and status in (3,4) limit ?,?";
            return new JDBCExecutor<List<GroupedItems>>(dp, sql, userId,planId,offset,limit) {

                @Override
                public List<GroupedItems> doWithResultSet(ResultSet rs) throws SQLException {
                    final List<GroupedItems> list = new ArrayList<GroupedItems>();
                    while (rs.next()) {
                        GroupedItems item = new GroupedItems(rs.getLong(2),rs.getLong(3),rs.getLong(4),rs.getInt(5),rs.getString(6));
                        item.setId(rs.getLong(1));
                        list.add(item);
                    }
                    return list;
                }
            }.call();
        }else{
            String sql = ALL_QUERY + " where userId = ? and planId = ? and status = ? limit ?,?";
            return new JDBCExecutor<List<GroupedItems>>(dp, sql, userId,planId,status,offset,limit) {

                @Override
                public List<GroupedItems> doWithResultSet(ResultSet rs) throws SQLException {
                    final List<GroupedItems> list = new ArrayList<GroupedItems>();
                    while (rs.next()) {
                        GroupedItems item = new GroupedItems(rs.getLong(2),rs.getLong(3),rs.getLong(4),rs.getInt(5),rs.getString(6));
                        item.setId(rs.getLong(1));
                        list.add(item);
                    }
                    return list;
                }
            }.call();
        }
    }
    
    
    static String EXIST_ID_QUERY = "select id from " + TABLE_NAME + " where userId = ? and numIid = ? and planId = ? and id = ?";

    private static long findExistId(Long userId, Long numIid, Long planId,Long id) {
        return dp.singleLongQuery(EXIST_ID_QUERY, userId, numIid, planId,id);
    }

    public boolean rawInsert() {
        long id = JDBCBuilder.insert("insert into `grouped_Items`(planId, userId,numIid,status) values(?,?,?,?)",
                this.planId, this.userId, this.numIid, this.status);
        
        if (id > 0L) {
            setId(id);
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.userId);
            return false;
        }
    }

    public boolean rawUpdate() {
        long updateNum = JDBCBuilder.insert(
                "update `grouped_Items` set `planId` = ?,`numIid` = ? ,`userId` = ? ,`status` = ?,`errorMsg` = ? where `id` = ?", 
                this.planId,this.numIid,this.userId,this.status,this.errorMsg, this.id);
        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for :" + this.id + "[userId : ]" + this.userId);
            return false;
        }
    }
    
    public boolean rawDelete(Long userId,Long numIid,Long planId) {
        
        String deleteSql = "delete from " + GroupedItems.TABLE_NAME + " where planId = ? and userId = ? and numIid = ?";
        
        long deleteNum = dp.update(deleteSql, planId, userId, numIid);
        
        if (deleteNum > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean jdbcSave() {
        try {
            long existdId = findExistId(this.userId, this.numIid, this.planId,this.id);

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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    
    public String getPicURL() {
        return picURL;
    }

    public void setPicURL(String picURL) {
        this.picURL = picURL;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    @Override
    public String getTableName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getTableHashKey() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getIdColumn() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setId(Long id) {
        // TODO Auto-generated method stub
        this.id = id;
    }

    @Override
    public String getIdName() {
        // TODO Auto-generated method stub
        return null;
    }

}
