
package models.oplog;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;

import models.item.ItemPlay;
import models.user.User;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import result.TMResult;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.DateUtil;

import dao.item.ItemDao;

@Entity(name = OpLog.TABLE_NAME)
@JsonIgnoreProperties(value = {
        "entityId", "sellerCids", "tableHashKey", "persistent", "tableName", "idName", "idColumn", "hashed",
        "propsName", "dataSrc", "topCate_1", "topCate_2", "topCate_3", "topCate_4", "hashColumnName", "parentCid",
        "isParent", "parent"
})
public class OpLog extends Model implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(OpLog.class);

    public static final String TAG = "OpLog";

    public static final String TABLE_NAME = "oplogs";

    @Transient
    static OpLog EMPTY = new OpLog();

//    @Transient
//    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    static DataSrc src = DataSrc.BASIC;

//    @Index(name = "userId")
    Long userId;

    public String content;

//''    @Index(name = "numIid")
    public Long numIid;

    @Transient
    public String title;

    @Transient
    public String picPath;
    
    @Transient
    private long delistTime;
    
    @Transient
    private int salesCount;

    @Enumerated(EnumType.STRING)
    public LogType type;

    public long created = 0L;

//  @Exclude
//    public Long updated;

    public enum LogType {
        ShowWindow, moditytitle, autocommentfail
    };

    public int status = 0;

    static class Status {
        public static final int IS_ERROR = 1;

    }

    public Long getTs() {
        return this.created;
    }

    /**
     * @param userId
     * @param content
     * @param numIid
     * @param picPath
     * @param type
     * @param isError
     */
    public OpLog(Long userId, String content, Long numIid, LogType type, boolean isError) {
        super();
        this.created = System.currentTimeMillis();
        this.userId = userId;
        this.content = content;
        this.numIid = numIid;
        this.type = type;
        if (isError) {
            this.status |= Status.IS_ERROR;
        }

        if (this.content != null && this.content.length() > 254) {
            this.content = this.content.substring(0, 254);
        }
    }

    public OpLog() {
        this.created = System.currentTimeMillis();
    }

    public OpLog(Long userId, String content, Long numIid, String title, String picPath, LogType type, boolean isError) {
        super();
        this.created = System.currentTimeMillis();
        this.userId = userId;
        this.content = content;
        this.numIid = numIid;
        this.title = title;
        this.picPath = picPath;
        this.type = type;
        if (isError) {
            this.status |= Status.IS_ERROR;
        }
        if (this.content != null && this.content.length() > 254) {
            this.content = this.content.substring(0, 254);
        }
    }

    public void updateByItem(ItemPlay item) {
        if (item == null) {
            this.title = "【该宝贝已经下架或者转移】";
            return;
        }

        this.title = item.getTitle();
        if (title == null) {
            this.title = "【该宝贝已经下架或者转移】";
        }
        this.numIid = item.getNumIid();
        this.picPath = item.getPicURL();
        this.salesCount = item.getSalesCount();
        this.delistTime = item.getDeListTime();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getNumIid() {
        return numIid;
    }

    public void setNumIid(Long numIid) {
        this.numIid = numIid;
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

    public LogType getType() {
        return type;
    }

    public void setType(LogType type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public int getSalesCount() {
        return salesCount;
    }
    
    public String getDelistTimeStr() {
        return DateUtil.formDateForLog(delistTime);
    }
    
    public String getCreateTimeStr() {
        return DateUtil.formDateForLog(created);
    }

    public static long deleteOld(int limit) {
        long ts = (DateUtil.formCurrDate() - DateUtil.TWO_WEEK_SPAN);
        return JDBCBuilder.update(false, src, "delete from " + TABLE_NAME + " where created < " + ts);
        //return OpLog.delete("created < ?", ts);
    }

    public static TMResult findByTypeAndUser(boolean ensureItem, User user, LogType type, PageOffset po) {

        //List<OpLog> list = OpLog.find("userId = ? and type = ? order by id desc", user.getId(), type)
        //        .from(po.getOffset()).fetch(po.getPs());
        List<OpLog> list = fetch("userId = ? and type = ? order by id desc limit ?,?", user.getId(), type.toString(),
                po.getOffset(), po.getPs());
        int count = 0;
        if (!CommonUtils.isEmpty(list)) {
            //count = (int) OpLog.count("userId = ? and type = ? order by id desc", user.getId(), type);
            count = (int) JDBCBuilder.singleLongQuery("select count(*) from " + TABLE_NAME
                    + " where userId = ? and type = ? ", user.getId(), type.toString());
        }
        if (ensureItem) {
            for (OpLog opLog : list) {
                long numIid = opLog.getNumIid();
                opLog.updateByItem(ItemDao.findByNumIid(opLog.getUserId(), numIid));
            }
        }
        return new TMResult(list, count, po);
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
        return null;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public static String insertSQL = "insert into " + TABLE_NAME
            + "(`userId`,`content`,`numIid`,`type`,`created`,`status`)" +
            " values(?,?,?,?,?,?)";;

    @Override
    public boolean jdbcSave() {
        long id = JDBCBuilder.insert(false, insertSQL, this.userId, this.content, this.numIid, this.type.toString(),
                this.created, this.status);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert oplogs Fails.....");
            return false;
        }
    }

    @Override
    public String getIdName() {
        return null;
    }

    public static void clearOld() {
        int maxCount = 2048;
        int count = 0;
        long start = System.currentTimeMillis() - (DateUtil.DAY_MILLIS * 2);

        while (count++ < maxCount) {
            long updateNum = JDBCBuilder.update(false, src, "delete from " + TABLE_NAME
                    + "  where created < ? order by id ", start);
            //long updateNum = OpLog.delete("created < ? order by id", start);
            if (updateNum <= 0) {
                return;
            }
        }

        long showwindowStart = System.currentTimeMillis() - DateUtil.WEEK_MILLIS;
        long windowLogRemoveNum = JDBCBuilder.update(false, src, "delete from " + TABLE_NAME
                + " where created < ? and ( type = ? ) limit 100000",
                showwindowStart, LogType.ShowWindow);
        log.error("remove window log remove num:" + windowLogRemoveNum);
    }

    public static List<OpLog> fetch(String whereQuery, Object... args) {
        return new JDBCExecutor<List<OpLog>>(" select id,userId,content,numIid,type,created,status from " + TABLE_NAME
                + " where "
                + whereQuery, args) {

            @Override
            public List<OpLog> doWithResultSet(ResultSet rs) throws SQLException {
                List<OpLog> list = new ArrayList<OpLog>();
                while (rs.next()) {
                    list.add(new OpLog(rs));
                }
                return list;
            }

        }.call();
    }

    public OpLog(ResultSet rs) throws SQLException {
        this.id = rs.getLong(1);
        this.userId = rs.getLong(2);
        this.content = rs.getString(3);
        this.numIid = rs.getLong(4);
        this.type = LogType.valueOf(rs.getString(5));
        this.created = rs.getLong(6);
        this.status = rs.getInt(7);

    }

    public static void batchWrite(List<OpLog> logs) {
        if (CommonUtils.isEmpty(logs)) {
            return;
        }
        StringBuilder sb = new StringBuilder("insert into " + TABLE_NAME
                + "(`userId`,`content`,`numIid`,`type`,`created`,`status`) values");

        Iterator<OpLog> it = logs.iterator();
        while (it.hasNext()) {
            OpLog opLog = it.next();

            sb.append('(');
            sb.append(opLog.userId);
            sb.append(",'");
            sb.append(CommonUtils.escapeSQL(opLog.content));
            sb.append("',");
            sb.append(opLog.numIid);
            sb.append(",'");
            sb.append(CommonUtils.escapeSQL(opLog.type.toString()));
            sb.append("',");
            sb.append(opLog.created);
            sb.append(",");
            sb.append(opLog.status);
            sb.append(")");
            if (it.hasNext()) {
                sb.append(',');
            }
        }

        JDBCBuilder.insert(false, sb.toString());
    }
}
