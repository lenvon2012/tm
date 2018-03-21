package models.sms;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import models.user.User;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder.JDBCExecutor;
import actions.SubcribeAction;
import actions.SubcribeAction.SubscribeInfo;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.CommonUtils;

import configs.Subscribe.Version;
import dao.UserDao;

@Entity(name = SmsSendCount.TABLE_NAME)
public class SmsSendCount extends GenericModel implements PolicySQLGenerator {

    @Transient
    private static final Logger log = LoggerFactory.getLogger(SmsSendCount.class);

    @Transient
    public static final String TABLE_NAME = "SmsSendCount_";

    @Transient
    public static SmsSendCount _instance = new SmsSendCount();

    @Transient
    public static final int DEFAULT_SMS_COUNT = 10;

    @Transient
    public static HashMap<Integer, Long> Version_MonthSmsCount_Map = new HashMap<Integer, Long>();
    
    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, _instance);

    static {
        Version_MonthSmsCount_Map.put(Version.FREE, 1L);
        Version_MonthSmsCount_Map.put(Version.BASE, 10L);
        Version_MonthSmsCount_Map.put(Version.VIP, 10L);
        Version_MonthSmsCount_Map.put(Version.SUPER, 20L);
        Version_MonthSmsCount_Map.put(Version.HALL, 30L);
        Version_MonthSmsCount_Map.put(Version.GOD, 30L);
    }

    @Id
    public Long userId;

    public String nick;

    public long total;

    public long used;

    public long ts;

    public long deadline;

    public SmsSendCount() {
    }

    public SmsSendCount(Long userId) {
        this.userId = userId;
        this.ts = System.currentTimeMillis();
    }

    public SmsSendCount(Long userId, String nick, long total, long deadline) {
        super();
        this.userId = userId;
        this.nick = nick;
        this.total = total;
        this.deadline = deadline;
    }

    public SmsSendCount(ResultSet rs) throws SQLException {
        this.userId = rs.getLong(1);
        this.nick = rs.getString(2);
        this.total = rs.getLong(3);
        this.used = rs.getLong(4);
        this.ts = rs.getLong(5);
        this.deadline = rs.getLong(6);
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getUsed() {
        return used;
    }

    public void setUsed(long used) {
        this.used = used;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public long getDeadline() {
        return deadline;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    @Override
    public String toString() {
        return "SmsSendCount [userId=" + userId + ", nick=" + nick + ", total=" + total + ", used=" + used + ", ts="
                + ts + ", deadline=" + deadline + "]";
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
        this.userId = id;
    }

    @Override
    public Long getId() {
        return userId;
    }

    @Override
    public String getIdName() {
        return "userId";
    }

    @Override
    public void _save() {
        this.jdbcSave();
    }

    @Override
    public boolean jdbcSave() {
        long id = findExistId(userId);
        try {
            if (id > 0) {
                return this.rawUpdate();
            }
            return this.rawInsert();
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }
    }

    public static long findExistId(long userId) {
        return dp.singleLongQuery("select userId from SmsSendCount_ where userId = ?", userId);
    }

    public boolean rawInsert() {
        long id = dp.insert(
                "insert into `SmsSendCount_`(`userId`,`nick`,`total`,`used`,`ts`, `deadline`) values(?,?,?,?,?,?)",
                this.userId, this.nick, this.total, this.used, this.ts, this.deadline);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails..... [userId : ]" + this.userId);

            return false;
        }
    }

    public boolean rawUpdate() {
        long updateNum = dp
                .insert("update `SmsSendCount_` set  `userId` = ?, `nick` = ?, `total` = ?, `used` = ?, `ts` = ?, `deadline` = ? where `userId` = ? ",
                        this.userId, this.nick, this.total, this.used, this.ts, this.deadline, this.getId());

        if (updateNum > 0L) {
            return true;
        } else {
            log.error("update Fails...[userId : ]" + this.userId);
            return false;
        }
    }

    public static SmsSendCount findByUserId(Long userId) {
        List<SmsSendCount> list = new ListFetcher("userId = ?", userId).call();
        if (CommonUtils.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    public static long findUsedCount(Long userId) {
        if (userId == null || userId < 0) {
            return -1;
        }

        return dp.singleLongQuery("select used from SmsSendCount_ where userId = ?", userId);
    }

    public static SmsSendCount findOrInitSmsSendCount(User user) {
        SmsSendCount exist = findByUserId(user.getIdlong());
        if (exist != null) {
            return exist;
        }

        SubscribeInfo subscribe = SubcribeAction.getMax(user);
        if (user.getVersion() <= 0) {
            user.setVersion(subscribe.getVersion());
        }
        long count = Version_MonthSmsCount_Map.get(user.getVersion());
        if (count <= 0) {
            count = 10;
        }

        int month = subscribe.getLeft() / 31;
        if (subscribe.getLeft() % 31 > 0) {
            month += 1;
        }

        long total = count * month;
        if (month == 12 && user.getVersion() >= Version.HALL) {
            total = 500;
        }
        SmsSendCount sms = new SmsSendCount(user.getId());
        sms.setNick(user.getUserNick());
        sms.setTotal(total);
        sms.setUsed(0);
        sms.rawInsert();
        return sms;
    }
    
    public static long countSmsSendUsed(Long userId) {
        SmsSendCount sms = findByUserId(userId);
        if (sms == null) {
            User user = UserDao.findById(userId);
            sms = findOrInitSmsSendCount(user);
        }
        return (sms.getTotal() - sms.getUsed());
    }

    public static long updateSmsSendUsed(Long userId) {
        SmsSendCount sms = findByUserId(userId);
        if (sms == null) {
            User user = UserDao.findById(userId);
            findOrInitSmsSendCount(user);
        }
        if (sms.getTotal() > sms.getUsed()) {
            sms.incrementUsedCount(userId);
        }
        return (sms.getTotal() - sms.getUsed());
    }

    public static boolean incrementUsedCount(Long userId) {
        if (userId == null || userId < 0) {
            return false;
        }

        long id = dp.update(false, "update SmsSendCount_ set used = used + 1 where userId = ?", userId);
        if (id > 0) {
            return true;
        }
        return false;
    }

    public static boolean addUsedCount(Long userId, int count) {
        if (userId == null || userId < 0) {
            return false;
        }
        if(count <= 0) {
        	return false;
        }
        long id = dp.update(false, "update SmsSendCount_ set used = used + " + count + " where userId = ?", userId);
        if (id > 0) {
            return true;
        }
        return false;
    }
    
    public static boolean addTotalSmsCount(Long userId, long increment) {
        if (userId == null || userId < 0) {
            return false;
        }

        long id = dp.update(false, "update SmsSendCount_ set total = total + ? where userId = ?", increment,
                userId);
        if (id > 0) {
            return true;
        }
        return false;
    }

    public static String COUNT_SMS_SEND_LOG_QUERY = "select count(*) from SmsSendCount_ ";

    public static long countSmsSendLogByRules(Long userId, String buyerNick, Long startTs, Long endTs) {
        String query = formatQuery(userId, buyerNick, startTs, endTs);

        String sql = COUNT_SMS_SEND_LOG_QUERY + " where " + query;
        log.info(sql);

        return dp.singleLongQuery(sql, userId);
    }

    private static String formatQuery(Long userId, String nick, Long startTs, Long endTs) {
        String query = " userId=? ";
        if (!StringUtils.isEmpty(nick)) {
            query += " and " + formatBuyerNickLike(nick);
        }
        if (startTs != null && startTs.longValue() > 0L) {
            query += " and ts >= " + startTs + " ";
        }
        if (endTs != null && endTs.longValue() > 0L) {
            query += " and ts < " + endTs + " ";
        }
        return query;
    }

    private static String formatBuyerNickLike(String buyerNick) {
        String like = " nick like '%" + buyerNick + "%' ";
        return like;
    }

    public static class ListFetcher extends JDBCExecutor<List<SmsSendCount>> {
        public ListFetcher(String whereQuery, Object... params) {
            super(whereQuery, params);
            StringBuilder sb = new StringBuilder();
            sb.append("select userId,nick,total,used,ts,deadline from SmsSendCount_ ");
            if (!StringUtils.isBlank(whereQuery)) {
                sb.append(" where ");
                sb.append(whereQuery);
            }
            this.query = sb.toString();
        }

        @Override
        public List<SmsSendCount> doWithResultSet(ResultSet rs) throws SQLException {
            List<SmsSendCount> list = new ArrayList<SmsSendCount>();
            while (rs.next()) {
                list.add(new SmsSendCount(rs));
            }
            return list;
        }
    }

    public static int count(Long hashKeyId, String whereQuery, Object... params) {
        StringBuilder sb = new StringBuilder();
        sb.append("select count(*) from SmsSendCount_ ");
        if (!StringUtils.isBlank(whereQuery)) {
            sb.append(" where ");
            sb.append(whereQuery);
        }
        return (int) dp.singleLongQuery(sb.toString(), params);
    }

}
