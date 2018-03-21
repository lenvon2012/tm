
package models.autolist;

import javax.persistence.Entity;
import javax.persistence.Transient;

import models.item.ItemPlay;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;
import dao.item.ItemDao;

@Entity(name = AutoListTime.TABLE_NAME)
public class AutoListTime extends Model implements PolicySQLGenerator {

    public static class DelistState {
        public static final int Success = 0;

        public static final int NotFoundUser = 1;

        public static final int DeListFail = 2;

        public static final int ListFail = 4;
        
        public static final int AttrError = 8;//属性词检测出错

        
        public static final int DelistSuccess = 16;//下架成功，但还没有上架
    }

    @Transient
    public static final Logger log = LoggerFactory.getLogger(AutoListTime.class);

    @Transient
    public static final String TABLE_NAME = "auto_list_time";

    @Transient
    public static AutoListTime EMPTY = new AutoListTime();
    
    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    @Index(name = "userId")
    private long userId;

    private long numIid;

    private int status = DelistState.Success;//状态，0表示正常，1表示找不到用户，2表示下架失败，4表示上架失败

    //相对于周日0点的时间
    @Index(name = "relativeListTime")
    private long relativeListTime;

    private long listTime;
    
    @Index(name = "planId")
    private long planId = 0;

    @Transient
    private String title;

    @Transient
    private String picPath;

    @Transient
    private double price;
    
    @Transient
    private double realDelistTime;

    public Long getUserId() {
        return userId;
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
    
    

    public long getPlanId() {
        return planId;
    }

    public void setPlanId(long planId) {
        this.planId = planId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public long getListTime() {
        return listTime;
    }

    public void setListTime(long listTime) {
        this.listTime = listTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getRelativeListTime() {
        return relativeListTime;
    }

    public void setRelativeListTime(long relativeListTime) {
        this.relativeListTime = relativeListTime;
    }
    
    

    public double getRealDelistTime() {
        return realDelistTime;
    }

    public void setRealDelistTime(double realDelistTime) {
        this.realDelistTime = realDelistTime;
    }



    public static final long DefaultPlanId = 0L;
    
    public static AutoListTime createAutoListTime(Long userId, Long numIid, Long relativeListTime, long planId) {
        AutoListTime autoListTime = new AutoListTime();
        autoListTime.setRelativeListTime(relativeListTime);
        autoListTime.setNumIid(numIid);
        autoListTime.setUserId(userId);
        autoListTime.status = DelistState.Success;
        autoListTime.listTime = 0;
        autoListTime.planId = planId;
        return autoListTime;
    }

    public void initItemProp() {
        ItemPlay item = ItemDao.findByNumIid(userId, numIid);
        if (item != null) {
            price = item.getPrice();
            title = item.getTitle();
            picPath = item.getPicURL();
            realDelistTime = item.getDeListTime();
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

    static String EXIST_ID_QUERY = "select id from " + TABLE_NAME + " where userId=? and numIid = ? ";

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

        //       log.info("rawInsert: userId: " + userId + " , appKey: " + appKey + " ,numIid: " + numIid);

        long id = dp
                .insert("insert into `auto_list_time`(`userId`,`numIid`,`status`,`relativeListTime`,`listTime`,`planId`) values(?,?,?,?,?,?)",
                        this.userId, this.numIid, this.status, this.relativeListTime, this.listTime, this.planId);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[numIid : ]" + this.numIid);

            return false;
        }

    }

    public boolean rawUpdate() {
//        log.info("rawUpdate: userId: " + userId + " , appKey: " + appKey + " ,numIid: " + numIid);

        long updateNum = dp
                .insert("update `auto_list_time` set  `userId` = ?, `numIid` = ?, `status` = ?, `relativeListTime` = ? ,`listTime` = ?,`planId` = ? where `id` = ? ",
                        this.userId, this.numIid, this.status, this.relativeListTime, this.listTime, this.planId, this.id);

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for :" + this.id + "[numIid : ]" + this.numIid);

            return false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#setId(java.lang.Long)
     */
    @Override
    public void setId(Long id) {
        // TODO Auto-generated method stub
        this.id = id;
    }
    
    public static long countByUserIdWithListTime(String query, long startRelative, long endRelative, long userId, long planId) {
    	return dp.singleLongQuery(query, startRelative, endRelative, userId, planId);
    }

}
