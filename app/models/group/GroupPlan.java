package models.group;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.persistence.Id;

import models.associate.AssociatePlan;
import models.user.User;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actions.GroupAction;

import play.db.jpa.Model;

import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;
import transaction.JDBCBuilder;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder.JDBCExecutor;

@Entity(name = GroupPlan.TABLE_NAME)
@JsonIgnoreProperties(value = {"tableName", "idColumn", "idName", "tableHashKey", "persistent", "entityId" })
public class GroupPlan extends Model implements PolicySQLGenerator{
    @Transient
    private static final Logger log = LoggerFactory.getLogger(GroupPlan.class);
    
    @Transient
    public static final String TABLE_NAME = "group_Plan";
    
    @Transient
    public static GroupPlan EMPTY = new GroupPlan();
    
    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    /**
     * 成功個數
     */
    @Transient
    private int success;
    
    /**
     * 失败个数
     */
    @Transient
    private int fail;
    
    @Transient
    private int wait;
    
    /**
     * 计划名称
     */
    private String planName;

    /**
     * 卖家Id
     */
    private Long userId;

    /**
     * 模板Id
     */
    private Long modelId;

    /**
     * 模板计划的类型 width or color
     */
    private String type;

    /**
     * 活动标题
     */
    private String activityTitle;
    
    /**
     * 促销标签
     */
    private String label;
    
    /**
     * 按钮名称
     */
    private String btnName;
    
    /**
     * 原价名称
     */
    private String originalPriceName;
    
    /**
     * 现价名称
     */
    private String currentPriceName; 
    
    /**
     * 活动时间
     */
    private int days;
    private int hours;
    private int minutes;
    
    /**
     * 宝贝属性
     */
    private String itemString;
    
    /**
     * 1 投放 ; 2 未投放 ; 3 已結束 ; 4 刪除 ; 5 正在投放... ; 6 正在刪除...
     */
    private int status;
    
    public GroupPlan(){
        super();
    }
    
    public GroupPlan(String planName, Long userId, Long modelId, String type,String activityTitle,String label,
            String btnName, String originalPriceName, String currentPriceName, int days, int hours, int minutes,String itemString,int status) {
        this.planName = planName;
        this.userId = userId;
        this.modelId = modelId;
        this.type = type;
        this.activityTitle = activityTitle;
        this.label = label;
        this.btnName = btnName;
        this.originalPriceName = originalPriceName;
        this.currentPriceName = currentPriceName;
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.itemString = itemString;
        this.status = status;
    }
    
    public GroupPlan(Long planId,Long usrId,String itemString){
        this.id = planId;
        this.userId = usrId;
        this.itemString = itemString;
    }
    
    public static GroupPlan getGroupPlan(Long planId,User user){
        GroupPlan plan = GroupPlan.singleQuery("id = ? and userId = ?", planId,user.getId());
        return plan;
    }
    
    public static List<GroupPlan> getGroupPlanByStatus(User user,int status,int offset,int limit){
        List<GroupPlan> planList = new ArrayList<GroupPlan>();
        if(status == GroupAction.ONE){
            //status 1,5,6
            planList = GroupPlan.nativeQuery("userId = ? and status in (1,5,6) order by id desc limit ?,? ",user.getId(),offset,limit);
        }else{
            planList = GroupPlan.nativeQuery("userId = ? and status = ? order by id desc limit ?,? ",user.getId(),status,offset,limit);
        }
        return planList;
    }
    
    public static int getPlanCount(Long userId,int status){
        String sql = "select count(*) from " + GroupPlan.TABLE_NAME + " where userId = ?  and status = ?";
        int count = (int) dp.singleLongQuery(sql, userId, status);
        return count;
    }
   
    public static List<GroupPlan> nativeQuery(String query, Object... params) {
        return new JDBCExecutor <List<GroupPlan>> (GROUP_PLAN_QUERY + query, params) {

            @Override
            public List<GroupPlan> doWithResultSet(ResultSet rs) throws SQLException {
                final List<GroupPlan> planList = new ArrayList<GroupPlan>();
                while (rs.next()) {
                    GroupPlan plan = new GroupPlan(rs.getString(2), rs.getLong(3), rs.getLong(4),rs.getString(5),
                             rs.getString(6), rs.getString(7),rs.getString(8), rs.getString(9),rs.getString(10), rs.getInt(11),
                            rs.getInt(12), rs.getInt(13), rs.getString(14),rs.getInt(15));
                    plan.setId(rs.getLong(1));
                    planList.add(plan);
                }
                return planList;
            }
        }.call();
    }
    
    
    
    private static String GROUP_PLAN_QUERY = "select id,planName,userId,modelId,type,activityTitle,label,btnName,originalPriceName," +
		        "currentPriceName,days,hours,minutes,itemString,status from " + GroupPlan.TABLE_NAME + " where ";
    
    public static GroupPlan singleQuery(String query, Object... params) {
        return new JDBCExecutor <GroupPlan> (GROUP_PLAN_QUERY + query, params) {

            @Override
            public GroupPlan doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    GroupPlan plan = new GroupPlan(rs.getString(2), rs.getLong(3), rs.getLong(4),rs.getString(5),rs.getString(6),
                             rs.getString(7), rs.getString(8), rs.getString(9),rs.getString(10), rs.getInt(11),
                            rs.getInt(12), rs.getInt(13), rs.getString(14),rs.getInt(15));
                    plan.setId(rs.getLong(1));
                    return plan;
                }else{
                    return null;
                }
            }
        }.call();
    }
    
    
    
    
    public boolean itemPropUpdate() {
        long updateNum = JDBCBuilder.insert("update `group_Plan` set `itemString` = ? where `id` = ? and `userId` = ?",
                this.itemString, this.id, this.userId);
        if (updateNum == 1) {
            return true;
        } else {
            log.error("updateItemProp failed...for :[id:]" + this.id + "[userId : ]" + this.userId);
            return false;
        }
    }
    
    public boolean statusUpdate(){
        long updateNum = JDBCBuilder.insert("update `group_Plan` set `status` = ? where `id` = ? and `userId` = ?", this.status,this.id,this.userId);
        if(updateNum == 1){
            return true;
        }else {
            log.error("updateStatus failed...for :[id:]" + this.id + "[userId : ]" + this.userId);
            return false;
        }
    }

    /**
     * 插入 默认status 为2
     * @return
     */
    public boolean rawInsert() {
        long id = JDBCBuilder
                .insert("insert into `group_Plan`(`planName`, `userId`,`modelId`,`type`,`activityTitle`,`label`,`btnName`,"
                        + "`originalPriceName`,`currentPriceName`,`days`,`hours`,`minutes`,`status`) values(?,?,?,?,?,?,?,?,?,?,?,?,?)",
                        this.planName, this.userId, this.modelId, this.type, this.activityTitle,this.label,
                        this.btnName, this.originalPriceName, this.currentPriceName, this.days, this.hours, this.minutes,GroupAction.TWO);
                       
        if (id > 0L) {
            setId(id);
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + userId);
            return false;
        }
    }

    /**
     * update status 状态不会改变
     * @return
     */
    public boolean rawUpdate() {
        long updateNum = JDBCBuilder
                .insert("update `group_Plan` set `planName` = ?, `userId` = ?, `modelId` = ? ,`type` = ?,`activityTitle` = ?,`label` = ?,"
                        + "`btnName` = ?, `originalPriceName` = ?,`currentPriceName` = ?,`days` = ?,`hours` = ?,`minutes` = ? ,`itemString` = ?," +
                        "`status` = ? where id = ?" ,
                        this.planName,this.userId,this.modelId,this.type,this.activityTitle,this.label,this.btnName,this.originalPriceName,
                        this.currentPriceName,this.days,this.hours,this.minutes,this.itemString,this.status,this.id);
        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for :" + id + "[userId : ]" + userId);
            return false;
        }
    }
    
    public boolean rawDelete(Long userId,Long planId) {
        
        String deleteSql = "delete from " + TABLE_NAME + " where id = ? and userId = ? ";
        
        long deleteNum = dp.update(deleteSql, id, userId);
        
        if (deleteNum > 0) {
            return true;
        } else {
            return false;
        }
    }
    
    private static String EXIST_ID_QUERY = "select id from " + TABLE_NAME + " where userId = ? and id = ?";
    
    private static long findExistId(Long userId,Long id){
        return dp.singleLongQuery(EXIST_ID_QUERY, userId, id);
    }
    
    @Override
    public boolean jdbcSave() {
        try{
            long existId = findExistId(this.userId, this.id);
            if(existId == 0L){
                return this.rawInsert();
            }else{
                id = existId;
                return this.rawUpdate();
            }
        }catch(Exception e){
            log.error(e.getMessage(),e);
            return false;
        }
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getActivityTitle() {
        return activityTitle;
    }

    public void setActivityTitle(String activityTitle) {
        this.activityTitle = activityTitle;
    }

    public String getBtnName() {
        return btnName;
    }

    public void setBtnName(String btnName) {
        this.btnName = btnName;
    }

    public String getOriginalPriceName() {
        return originalPriceName;
    }

    public void setOriginalPriceName(String originalPriceName) {
        this.originalPriceName = originalPriceName;
    }

    public String getCurrentPriceName() {
        return currentPriceName;
    }

    public void setCurrentPriceName(String currentPriceName) {
        this.currentPriceName = currentPriceName;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }
    
    public String getItemString() {
        return itemString;
    }

    public void setItemString(String itemString) {
        this.itemString = itemString;
    }
    
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
    
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    
    public int getWait() {
        return wait;
    }

    public void setWait(int wait) {
        this.wait = wait;
    }

    public int getFail() {
        return fail;
    }

    public void setFail(int fail) {
        this.fail = fail;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
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
    
    @Override
    public String getTableName() {
        // TODO Auto-generated method stub
        return "id";
    }

    @Override
    public String getTableHashKey() {
        // TODO Auto-generated method stub
        return null;
    }

}
