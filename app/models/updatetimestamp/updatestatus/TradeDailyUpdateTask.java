package models.updatetimestamp.updatestatus;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Entity;

import models.updatetimestamp.UserDailyUpdateStatus;
import transaction.JDBCBuilder;

@Entity(name = TradeDailyUpdateTask.TABLE_NAME)
public class TradeDailyUpdateTask extends UserDailyUpdateStatus {

    public final static String TABLE_NAME = "trade_daily_update_task";

    public TradeDailyUpdateTask(Long userId, Long ts) {
        super(userId, ts);
    }

    public TradeDailyUpdateTask(ResultSet rs) throws SQLException {
        super(rs.getLong(2), rs.getLong(3));
        this.id = rs.getLong(1);
        this.userId = rs.getLong(2);
        this.ts = rs.getLong(3);
        this.createAt = rs.getLong(4);
        this.updateAt = rs.getLong(5);
        this.status = rs.getInt(6);
    }

    public static TradeDailyUpdateTask findByUserIdAndTs(Long userId, Long ts) {

        String sql = SELECT_SQL + " where userId = ? and ts = ?";
        TradeDailyUpdateTask task = new JDBCBuilder.JDBCExecutor<TradeDailyUpdateTask>(sql, userId, ts) {
            @Override
            public TradeDailyUpdateTask doWithResultSet(ResultSet rs) throws SQLException {
                // TODO Auto-generated method stub
                if (rs.next()) {
                    return new TradeDailyUpdateTask(rs);
                }
                return null;
            }
        }.call();

        return task;
        // return TradeDailyUpdateTask.find("userId= ?  and ts = ? ", userId, ts).first();

    }

    public static TradeDailyUpdateTask findOrCreate(Long userId, Long ts) {
        TradeDailyUpdateTask task = findByUserIdAndTs(userId, ts);
        if (task == null) {
            // task = new TradeDailyUpdateTask(userId, ts).save();
            task = new TradeDailyUpdateTask(userId, ts).rawInsert();
        }
        return task;
    }

    static String SELECT_SQL = "select id,userId,ts,createAt,updateAt,status from " + TABLE_NAME;

    public TradeDailyUpdateTask rawInsert() {
        this.updateAt = System.currentTimeMillis();
        if (this.createAt <= 0L) {

            this.createAt = System.currentTimeMillis();
        }
        
        long id = JDBCBuilder
                .insert("insert into `trade_daily_update_task`(`userId`,`ts`,`createAt`,`updateAt`,`status`) values(?,?,?,?,?)",
                        this.userId, this.ts, this.createAt, this.updateAt, this.status);

        if (id > 0) {
            this.id = id;
            return this;
        }
        return null;
    }

    public boolean rawUpdate() {
        this.updateAt = System.currentTimeMillis();
        if (this.createAt <= 0L) {

            this.createAt = System.currentTimeMillis();
        }
        
        long updateNum = JDBCBuilder
                .insert("update `trade_daily_update_task` set  `userId` = ?, `ts` = ?, `createAt` = ?, `updateAt` = ?, `status` = ? where `id` = ? ",
                        this.userId, this.ts, this.createAt, this.updateAt, this.status, this.getId());

        if (updateNum < 0) {
            return false;
        }
        return true;
    }
}