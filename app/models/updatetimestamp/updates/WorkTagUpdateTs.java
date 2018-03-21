
package models.updatetimestamp.updates;

import static java.lang.String.format;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import utils.DateUtil;

import com.ciaosir.client.utils.NumberUtil;

/**
 * 对于应用级别的任务的更新，比如当前的jdp数据库扫到什么时间点这样的任务
 * @author zrb
 *
 */
@Entity(name = WorkTagUpdateTs.TABLE_NAME)
public class WorkTagUpdateTs extends GenericModel {

    private static final Logger log = LoggerFactory.getLogger(WorkTagUpdateTs.class);

    public static final String TAG = "WorkTagUpdateTs";

    public static final String TABLE_NAME = "worktag_update_ts";

    static DataSrc src = DataSrc.BASIC;

    static WorkTagUpdateTs _instance = new WorkTagUpdateTs();

    public WorkTagUpdateTs() {
    }

    @Id
    public String worktag;

//    @Column(name = "first_ts")
    public long firstUpdateTime;

//    @Column(name = "last_ts")
    public long lastUpdateTime;

    @Column(name = "comment", columnDefinition = " varchar(2046) DEFAULT NULL")
    String comment;

    public WorkTagUpdateTs(String tag) {
        this.worktag = tag;
        this.firstUpdateTime = System.currentTimeMillis();
    }

    public WorkTagUpdateTs(ResultSet rs) throws SQLException {
        this.worktag = rs.getString(1);
        this.firstUpdateTime = rs.getLong(2);
        this.lastUpdateTime = rs.getLong(3);
        this.comment = rs.getString(4);
    }

    public WorkTagUpdateTs(String tag, long ts) {
        this.worktag = tag;
        this.firstUpdateTime = ts;
        this.lastUpdateTime = ts;
    }

    static String selectSql = " select worktag,firstUpdateTime,lastUpdateTime,comment ";

    public static List<WorkTagUpdateTs> fetch(String whereSql, Object... objects) {
        String sql = selectSql + " from  " + TABLE_NAME + " where 1 = 1  ";
        if (StringUtils.isEmpty(whereSql)) {
        } else {
            sql += " and " + whereSql;
        }
        return new JDBCBuilder.JDBCExecutor<List<WorkTagUpdateTs>>(sql, objects) {
            @Override
            public List<WorkTagUpdateTs> doWithResultSet(ResultSet rs) throws SQLException {
                List<WorkTagUpdateTs> res = new ArrayList<WorkTagUpdateTs>();
                while (rs.next()) {
                    res.add(new WorkTagUpdateTs(rs));
                }
                return res;
            }
        }.call();
    }

    @Override
    public void _save() {
        WorkTagUpdateTs exit = findByTag(this.worktag);
        if (exit == null) {
            rawInsert();
        } else {
            rawUpdate();
        }

        return;
    }

    private void rawUpdate() {
        JDBCBuilder.update(false, "update " + TABLE_NAME + " set lastUpdateTime = ?,comment =? where worktag = ?",
                this.lastUpdateTime, this.comment, this.worktag);
    }

    public void rawInsert() {
        JDBCBuilder.insert(false, false, src, " insert into " + TABLE_NAME
                + " (worktag,firstUpdateTime,lastUpdateTime,comment) values(?,?,?,?)",
                this.worktag, this.firstUpdateTime, this.lastUpdateTime, this.comment);
    }

    public static void updateLastModifedTime(String tag, long ts) {

        log.info(format("updateLastModifedTime:tag, ts".replaceAll(", ", "=%s, ") + "=%s", tag,
                DateUtil.formDateForLog(ts)));

        WorkTagUpdateTs memberTs = findByTag(tag);
        if (memberTs == null) {
            log.warn("No User Found...Create it now for id:" + tag);
            new WorkTagUpdateTs(tag, ts).rawInsert();
            return;
        }

        if (ts < memberTs.lastUpdateTime) {
            log.warn("ts[" + ts + "] is less than [" + memberTs.lastUpdateTime + "], No Update");
            return;
        }

        memberTs.setLastUpdateTime(ts);
        memberTs.rawUpdate();

    }

    public static WorkTagUpdateTs findByTag(final String tag) {
        return NumberUtil.first(fetch("  worktag = ? ", tag));
    }

    public String gettag() {
        return worktag;
    }

    public void settag(String tag) {
        this.worktag = tag;
    }

    public long getFirstUpdateTime() {
        return firstUpdateTime;
    }

    public void setFirstUpdateTime(long firstUpdateTime) {
        this.firstUpdateTime = firstUpdateTime;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getWorktag() {
        return worktag;
    }

    public void setWorktag(String worktag) {
        this.worktag = worktag;
    }

    public static WorkTagUpdateTs findOrCreate(String tag) {
        WorkTagUpdateTs model = findByTag(tag);
        if (model != null) {
            return model;
        }

        model = new WorkTagUpdateTs(tag, System.currentTimeMillis() - 5 * DateUtil.ONE_MINUTE_MILLIS);
        model.rawInsert();
        return model;
    }

    public static DataSrc getSrc() {
        return src;
    }

}
