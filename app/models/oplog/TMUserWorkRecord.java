
package models.oplog;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.persistence.Column;
import javax.persistence.Entity;

import job.diagjob.PropDiagJob;
import models.CreatedUpdatedModel;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;

/**
 * http://item.taobao.com/item.htm?id=35280757270
 * http://item.taobao.com/item.htm?id=35018140257
 * @author zrb
 *
 */
@Entity(name = TMUserWorkRecord.TABLE_NAME)
public class TMUserWorkRecord extends CreatedUpdatedModel implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(TMUserWorkRecord.class);

    public static final String TAG = "TMJdpFailRecord";

    public static final String TABLE_NAME = "tm_jdp_fail_log";

    static DataSrc src = DataSrc.BASIC;

    public static DataSrc getSrc() {
        return src;
    }

    public static void setSrc(DataSrc src) {
        TMUserWorkRecord.src = src;
    }

    String type;

    @Index(name = "userId")
    Long userId;

    @Column(columnDefinition = "varchar(8190) default null")
    String msg;

    int failCount;

    public TMUserWorkRecord(String type, Long userId, String msg, int failCount) {
        super();
        this.type = type;
        this.userId = userId;
        this.msg = msg;
        this.failCount = failCount;
    }

    static String insertFields = " (`userId`,`msg`,`type`,`failCount`,`created`,`updated`) ";

    public static String insertSQL = "insert into " + TABLE_NAME + insertFields + " values(?,?,?,?,?,?)";

    public static boolean recentFails(User user) {
        long end = System.currentTimeMillis() - DateUtil.DAY_MILLIS * 4;
        return JDBCBuilder.singleIntQuery(src, " select failCount from " + TABLE_NAME
                + "  where userId = ? and type = ? and created  > ?",
                user.getId(), PropDiagJob.TAG, end) > 0;
    }

    public static boolean exists(Long id, String tag) {
        return JDBCBuilder.singleLongQuery(src, " select userId from " + TABLE_NAME + " where userId = ? and type = ?",
                id, tag) > 0L;
    }

    @Override
    public boolean jdbcSave() {
        long id = JDBCBuilder.insert(false, insertSQL, this.userId, this.msg, this.type.toString(), this.failCount,
                this.created, this.updated);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert tm jdp fail record Fails.....");
            return false;
        }
    }

    @Override
    public String getIdName() {
        return "id";
    }

    public static void clearOld() {
        int maxCount = 2048;
        int count = 0;
        long start = System.currentTimeMillis() - DateUtil.THIRTY_DAYS;

        while (count++ < maxCount) {
            long updateNum = JDBCBuilder.update(false, src, "delete from " + TABLE_NAME
                    + "  where created < ? order by id ", start);
            //long updateNum = TMJdpFailRecord.delete("created < ? order by id", start);
            if (updateNum <= 0) {
                return;
            }
        }
    }

    static String selectField = " `id`,`userId`,`msg`,`type`,`failCount`,`created`,`updated` ";

    public static List<TMUserWorkRecord> fetch(String whereQuery, Object... args) {
        return new JDBCExecutor<List<TMUserWorkRecord>>(
                " select " + selectField + "  from " + TABLE_NAME + " where " + whereQuery, args) {

            @Override
            public List<TMUserWorkRecord> doWithResultSet(ResultSet rs) throws SQLException {
                List<TMUserWorkRecord> list = new ArrayList<TMUserWorkRecord>();
                while (rs.next()) {
                    list.add(new TMUserWorkRecord(rs));
                }
                return list;
            }

        }.call();
    }

    public TMUserWorkRecord(ResultSet rs) throws SQLException {
        this.id = rs.getLong(1);
        this.userId = rs.getLong(2);
        this.msg = rs.getString(3);
        this.type = rs.getString(4);
        this.failCount = rs.getInt(5);
        this.created = rs.getLong(6);
        this.updated = rs.getLong(7);
    }

    public static void batchWrite(List<TMUserWorkRecord> records) {
        if (CommonUtils.isEmpty(records)) {
            return;
        }

        StringBuilder sb = new StringBuilder("insert into " + TABLE_NAME + insertFields + " values");

        Iterator<TMUserWorkRecord> it = records.iterator();
        while (it.hasNext()) {
            TMUserWorkRecord TMJdpFailRecord = it.next();

            sb.append('(');
            sb.append(TMJdpFailRecord.userId);
            sb.append(",'");
            sb.append(CommonUtils.escapeSQL(TMJdpFailRecord.msg));
            sb.append("',");
            sb.append(",'");
            sb.append(CommonUtils.escapeSQL(TMJdpFailRecord.type.toString()));
            sb.append("',");
            sb.append(TMJdpFailRecord.failCount);
            sb.append(",");
            sb.append(TMJdpFailRecord.created);
            sb.append(",");
            sb.append(TMJdpFailRecord.updated);
            sb.append(")");

            if (it.hasNext()) {
                sb.append(',');
            }
        }

        JDBCBuilder.insert(false, true, sb.toString());
    }

    @JsonAutoDetect
    public static class TMWorkMsg implements Serializable {
        @JsonProperty
        String type;

        @JsonProperty
        String msg;

        @JsonProperty
        Long userId;

        public TMWorkMsg(Long userId, String type, String msg) {
            super();
            this.type = type;
            this.msg = msg;
            this.userId = userId;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

    }

    @Every("20s")
    public static class TMJdpFailWritter extends Job {

        static Queue<TMUserWorkRecord> queue = new ConcurrentLinkedQueue<TMUserWorkRecord>();

        public void doJob() {
            TMUserWorkRecord msg = null;
            while ((msg = queue.poll()) != null) {
                msg.jdbcSave();
            }
        }

        public static Queue<TMUserWorkRecord> getQueue() {
            return queue;
        }

        public static void addMsg(String tag, User user, Set<Long> failIds) {
            if (CommonUtils.isEmpty(failIds)) {
                log.warn("no fail no write..." + user);
                return;
            }
            int size = failIds.size();
            String msg = StringUtils.join(failIds, ',');

            if (msg.length() > 8000) {
                msg = msg.substring(0, 8000);
            }
            queue.add(new TMUserWorkRecord(tag, user.getId(), msg, size));
        }

        public static void addMsg(User user, String tag) {
            queue.add(new TMUserWorkRecord(tag, user.getId(), StringUtils.EMPTY, 0));
        }

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
    public void setId(Long id) {
        this.id = id;
    }

}
