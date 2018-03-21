package models.oplog;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.JDBCBuilder;

@Entity(name = UnCommentedTradeJobLog.TABLE_NAME)
public class UnCommentedTradeJobLog extends Model{
    public static final String TABLE_NAME = "un_comment_trade_job_log";
    
    private static final Logger log = LoggerFactory.getLogger(UnCommentedTradeJobLog.class);
    
    public static final String TAG = "UnCommentedTradeJobLog";
    
    public static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public String ts;
    
    public Long userCount;
    
    public UnCommentedTradeJobLog() {
        this.ts = df.format(new Date());
    }
    
    public UnCommentedTradeJobLog(Long userCount) {
        this.ts = df.format(new Date());
        this.userCount = userCount;
    }
    
    public Long getUserCount() {
        return userCount;
    }

    public void setUserCount(Long userCount) {
        this.userCount = userCount;
    }
    
    public String getTs() {
        return ts;
    }
    public void setTs(String ts) {
        this.ts = ts;
    }

    public static String insertSQL = "insert into " + TABLE_NAME + "(`ts`,`userCount`) values(?,?)";;
    public boolean saveLog() {
        long id = JDBCBuilder.insert(false, insertSQL, this.ts,this.userCount);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert seller Fails.....");
            return false;
        }
    }
}
