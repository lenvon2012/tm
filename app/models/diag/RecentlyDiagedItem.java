
package models.diag;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import models.item.ItemPlay;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;

@Entity(name = RecentlyDiagedItem.TABLE_NAME)
public class RecentlyDiagedItem extends GenericModel {

    private final static Logger log = LoggerFactory
            .getLogger(RecentlyDiagedItem.class);

    public final static String TABLE_NAME = "recently_diaged_item";

    @Id
    public long numIid;

    @Index(name = "userId")
    public long userId;

    public long diagTime;

    public String picPath;

    public long getNumIid() {
        return numIid;
    }

    public void setNumIid(long numIid) {
        this.numIid = numIid;
    }

    public long getDiagTime() {
        return diagTime;
    }

    public void setDiagTime(long diagTime) {
        this.diagTime = diagTime;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    public RecentlyDiagedItem(Long numIid, Long userId, Long diagTime, String picPath) {
        this.numIid = numIid;
        this.userId = userId;
        this.diagTime = diagTime;
        this.picPath = picPath;
    }

    public RecentlyDiagedItem(ItemPlay itemPlay) {
        if (itemPlay != null) {
            this.numIid = itemPlay.getNumIid();
            this.userId = itemPlay.getUserId();
            this.diagTime = System.currentTimeMillis();
            this.picPath = itemPlay.getPicURL();
        }
    }

    static String EXIST_ID_QUERY = "select numIid from " + TABLE_NAME
            + " where  numIid = ? and userId = ?";

    public static long findExistId(long numIid, long userId) {
        return JDBCBuilder.singleLongQuery(EXIST_ID_QUERY, numIid, userId);
    }

    public boolean jdbcSave() {
        try {
            long existdId = findExistId(this.numIid, this.userId);

            if (existdId == 0L) {
                return this.rawInsert();
            } else {
                setNumIid(existdId);
                return this.rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }
    }

    public boolean rawInsert() {
        // TODO Auto-generated method stub
        long id = JDBCBuilder.insert("insert into `" + TABLE_NAME
                + "`(`numIid`,`userId`,`diagTime`,`picPath`) values(?,?,?,?)", this.numIid,
                this.userId, this.diagTime, this.picPath);

        if (id > 0L) {
            log.info("insert ts for the first time !" + numIid);
            return true;
        } else {
            log.error("Insert Fails....." + "[Id : ]" + this.numIid);

            return false;
        }
    }

    public boolean rawUpdate() {
        long updateNum = JDBCBuilder.insert("update `" + TABLE_NAME
                + "` set  `diagTime` = ?, `picPath` = ? where `numIid` = ? and userId = ? ",
                this.diagTime, this.picPath, this.numIid, this.userId);

        if (updateNum > 0L) {
            log.info("update ts success! " + numIid);
            return true;
        } else {
            log.error("update Fails....." + "[Id : ]" + this.numIid);

            return false;
        }
    }

    public static RecentlyDiagedItem findMostRecently(Long userId) {
        String sql = "select numIid, userId, diagTime, picPath from " + TABLE_NAME + " where " +
                "userId = ? order by diagTime desc limit 1";

        return new JDBCExecutor<RecentlyDiagedItem>(sql, userId) {
            @Override
            public RecentlyDiagedItem doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return new RecentlyDiagedItem(rs.getLong(1), rs.getLong(2), rs.getLong(3),
                            rs.getString(4));
                } else
                    return null;
            }
        }.call();
    }

    public static List<RecentlyDiagedItem> getRecentlyFour(Long userId) {
        String sql = "select numIid, userId, diagTime, picPath from " + TABLE_NAME
                + " where userId = ? order by diagTime desc limit 4";

        return new JDBCExecutor<List<RecentlyDiagedItem>>(sql, userId) {
            @Override
            public List<RecentlyDiagedItem> doWithResultSet(ResultSet rs) throws SQLException {
                List<RecentlyDiagedItem> items = new ArrayList<RecentlyDiagedItem>();
                while (rs.next()) {
                    items.add(new RecentlyDiagedItem(rs.getLong(1), rs.getLong(2), rs.getLong(3),
                            rs.getString(4)));
                }
                return items;
            }
        }.call();
    }
}
