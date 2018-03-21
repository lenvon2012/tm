package models.op;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.pojo.PageOffset;

import play.db.jpa.Model;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = CHEctocystLog.TABLE_NAME)
public class CHEctocystLog extends Model implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(CHEctocystLog.class);

    public static final String TAG = "CHEctocystLog";

    public static final String TABLE_NAME = "ch_ectocyst_log";

    private String name;

    private String requestUrl;

    private String requestAction;

    private String ip;

    private long creatTime;

    public CHEctocystLog(String name, String requestUrl, String requestAction, String ip) {

        this.name = name;

        this.requestUrl = requestUrl;

        this.requestAction = requestAction;

        this.creatTime = System.currentTimeMillis();

        this.ip = ip;

        if (this.requestUrl != null && this.requestUrl.length() > 254) {
            this.requestUrl = this.requestUrl.substring(0, 254);
        }

    }

    public CHEctocystLog(ResultSet rs) throws SQLException {
        this.id = rs.getLong(1);
        this.name = rs.getString(2);
        this.requestUrl = rs.getString(3);
        this.requestAction = rs.getString(4);
        this.ip = rs.getString(5);
        this.creatTime = rs.getLong(6);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getRequestAction() {
        return requestAction;
    }

    public void setRequestAction(String requestAction) {
        this.requestAction = requestAction;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public long getCreatTime() {
        return creatTime;
    }

    public void setCreatTime(long creatTime) {
        this.creatTime = creatTime;
    }

    @Override
    public String toString() {
        return "CHEctocystLog [name=" + name + ", requestUrl=" + requestUrl
                + ", requestAction=" + requestAction + ", ip=" + ip
                + ", creatTime=" + creatTime + "]";
    }

    @Override
    public String getTableName() {
        // TODO Auto-generated method stub
        return TABLE_NAME;
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

    public static String insertSQL = "insert into " + TABLE_NAME
            + "(`name`,`requestUrl`,`requestAction`,`ip`,`creatTime`)" +
            " values(?,?,?,?,?)";

    @Override
    public boolean jdbcSave() {
        long id = JDBCBuilder.insert(false, insertSQL, this.name, this.requestUrl, this.requestAction, this.ip,
                this.creatTime);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert oplogs Fails.....");
            return false;
        }
    }

    @Override
    public String getIdName() {
        // TODO Auto-generated method stub
        return null;
    }

    public static final String select_SQL = " select id,name,requestUrl,requestAction,ip,creatTime from " + TABLE_NAME;

    public static List<CHEctocystLog> findlogAll() {
        String sql = select_SQL;

        return new JDBCExecutor<List<CHEctocystLog>>(sql) {

            @Override
            public List<CHEctocystLog> doWithResultSet(ResultSet rs) throws SQLException {
                List<CHEctocystLog> logList = new ArrayList<CHEctocystLog>();
                while (rs.next()) {
                    logList.add(new CHEctocystLog(rs));
                }
                return logList;
            }
        }.call();
    }

    public static List<CHEctocystLog> findByTime(long startTime, long endTime, PageOffset po) {

        String sql = select_SQL;

        sql += " where creatTime > ? ";

        if (endTime != 0) {
            sql += " and creatTime < " + endTime;
        }

        sql += " limit ?,?";

        return new JDBCExecutor<List<CHEctocystLog>>(sql, startTime, po.getOffset(), po.getPs()) {

            @Override
            public List<CHEctocystLog> doWithResultSet(ResultSet rs) throws SQLException {
                List<CHEctocystLog> logList = new ArrayList<CHEctocystLog>();
                while (rs.next()) {
                    logList.add(new CHEctocystLog(rs));
                }
                return logList;
            }
        }.call();
    }

    public static long countByTime(long startTime, long endTime) {
        String sql = "  select count(*) from " + TABLE_NAME;

        sql += " where creatTime > ? ";

        if (endTime != 0) {
            sql += " and creatTime < " + endTime;
        }

        return JDBCBuilder.singleLongQuery(sql, startTime);
    }

}
