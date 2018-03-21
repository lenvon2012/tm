
package models.autolist;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

import models.item.ItemPlay;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;
import dao.item.ItemDao;

@Entity(name = AutoListLog.TABLE_NAME)
public class AutoListLog extends Model implements PolicySQLGenerator {
    @Transient
    private static final Logger log = LoggerFactory.getLogger(AutoListLog.class);

    @Transient
    public static final String TABLE_NAME = "auto_list_log";

    @Transient
    public static AutoListLog EMPTY = new AutoListLog();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

    @Index(name = "userId")
    private long userId;

    @Index(name = "numIid")
    private long numIid;

    @Index(name = "planId")
    private long planId;

    private long listTime;

    private int status;

    private String opMsg;

    @Transient
    private String title;

    @Transient
    private String picPath;

    @Transient
    private double price;

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

    public long getNumIid() {
        return numIid;
    }

    public void setNumIid(long numIid) {
        this.numIid = numIid;
    }

    public String getOpMsg() {
        return opMsg;
    }

    public void setOpMsg(String opMsg) {
        this.opMsg = opMsg;
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

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getPlanId() {
        return planId;
    }

    public void setPlanId(long planId) {
        this.planId = planId;
    }

    public static AutoListLog createAutoListJobTs(long userId, long planId, long numIid, long listTime) {
        AutoListLog listLog = new AutoListLog();
        listLog.userId = userId;
        listLog.planId = planId;
        listLog.numIid = numIid;
        listLog.listTime = listTime;

        return listLog;
    }

    public void initItemProp() {
        ItemPlay item = ItemDao.findByNumIid(userId, numIid);
        if (item != null) {
            price = item.getPrice();
            title = item.getTitle();
            picPath = item.getPicURL();
        }
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

    //static String EXIST_ID_QUERY = "select id from " + TABLE_NAME + " where  id = ? ";

    public static long findExistId() {
        //return dp.singleLongQuery(EXIST_ID_QUERY, id);
        return 0L;
    }

    @Override
    public boolean jdbcSave() {
        try {
            long existdId = findExistId();

            if (existdId == 0L) {
                return this.rawInsert();
            } else {
                setId(existdId);
                return this.rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }
    }

    public boolean rawInsert() {
        if (StringUtils.isEmpty(opMsg) == false && opMsg.length() > 255) {
            opMsg = opMsg.substring(0, 255);
        }
        // TODO Auto-generated method stub
        long id = dp
                .insert(
                        "insert into `auto_list_log`(`userId`,`planId`,`numIid`,`listTime`,`status`,`opMsg`) values(?,?,?,?,?,?)",
                        this.userId,
                        this.planId, this.numIid, this.listTime, this.status, this.opMsg);

        if (id > 0L) {

            return true;
        } else {
            log.error("Insert Fails....." + "[Id : ]" + this.id);

            return false;
        }
    }

    public boolean rawUpdate() {
        long updateNum = dp
                .insert(
                        "update `auto_list_log` set  `userId` = ?, `planId` = ?, `numIid` = ?, `listTime` = ?, `status` = ?, `opMsg` = ? where `id` = ? ",
                        this.userId, this.planId, this.numIid, this.listTime, this.status, this.opMsg, this.getId());

        if (updateNum > 0L) {

            return true;
        } else {
            log.error("update Fails....." + "[Id : ]" + this.id);

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

    public static long countByUserId(Long userId, long planId, Collection<Long> numIidColl) {
        String query = "select count(*) from " + TABLE_NAME + " where userId = ? and planId = ? and listTime > 0";//好像差一点
        
        if (CommonUtils.isEmpty(numIidColl) == false) {
            query += " and numIid in (" + StringUtils.join(numIidColl, ",") + ") ";
        }
        
        
        return dp.singleLongQuery(query, userId, planId);
    }

    public static void updateZeroPlanId(Long userId, long planId) {
        String sql = " update " + TABLE_NAME + " set planId = ? where userId = ? and planId = 0";
        dp.update(sql, planId, userId);
    }

    public static void deleteByPlanId(Long userId, long planId) {
        String sql = "delete from " + TABLE_NAME + " where userId = ? and planId = ?";

        dp.update(sql, userId, planId);
    }

    public static List<AutoListLog> findByUserId(long userId, long planId, Collection<Long> numIidColl,
            int startPage, int pageSize) {

        int offset = 0;
        if (startPage > 0) {
            offset = (startPage - 1) * pageSize;
        }
        String query = "select id, userId, planId, numIid,listTime,status,opMsg from " + TABLE_NAME
                + " where userId = ? and planId = ? ";
        
        if (CommonUtils.isEmpty(numIidColl) == false) {
            query += " and numIid in (" + StringUtils.join(numIidColl, ",") + ") ";
        }
        
        query += " order by listTime desc limit ?,? ";

        return new JDBCBuilder.JDBCExecutor<List<AutoListLog>>(dp, query, userId, planId, offset, pageSize) {

            @Override
            public List<AutoListLog> doWithResultSet(ResultSet rs)
                    throws SQLException {
                List<AutoListLog> list = new ArrayList<AutoListLog>();
                while (rs.next()) {
                    AutoListLog autoListLog = new AutoListLog();
                    autoListLog.setId(rs.getLong(1));
                    autoListLog.setUserId(rs.getLong(2));
                    autoListLog.setPlanId(rs.getLong(3));
                    autoListLog.setNumIid(rs.getLong(4));
                    autoListLog.setListTime(rs.getLong(5));
                    autoListLog.setStatus(rs.getInt(6));
                    autoListLog.setOpMsg(rs.getString(7));

                    list.add(autoListLog);
                }
                return list;
            }

        }.call();
    }
}
