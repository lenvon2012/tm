
package models.oplog;

import javax.persistence.Column;
import javax.persistence.Entity;

import models.CreatedUpdatedModel;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transaction.DBBuilder.DataSrc;

/**
 * http://item.taobao.com/item.htm?id=35280757270
 * http://item.taobao.com/item.htm?id=35018140257
 * @author zrb
 *
 */
@Entity(name = TMUserWindowInfo.TABLE_NAME)
public class TMUserWindowInfo extends CreatedUpdatedModel {

    private static final Logger log = LoggerFactory.getLogger(TMUserWindowInfo.class);

    public static final String TAG = "TMJdpFailRecord";

    public static final String TABLE_NAME = "tm_user_window_info";

    static DataSrc src = DataSrc.BASIC;

    String type;

    @Index(name = "userId")
    Long userId;

    @Column(columnDefinition = "varchar(8190) default null")
    String msg;

//    
//
//    public TMUserWindowInfo(String type, Long userId, String msg, int failCount) {
//        super();
//        this.type = type;
//        this.userId = userId;
//        this.msg = msg;
//        this.failCount = failCount;
//    }
//
//    static String insertFields = " (`userId`,`msg`,`type`,`failCount`,`created`,`updated`) ";
//
//    public static String insertSQL = "insert into " + TABLE_NAME + insertFields + " values(?,?,?,?,?,?)";
//
//    @Override
//    public boolean jdbcSave() {
//        long id = JDBCBuilder.insert(false, insertSQL, this.userId, this.msg, this.type.toString(), this.failCount,
//                this.created, this.updated);
//
//        if (id > 0L) {
//            return true;
//        } else {
//            log.error("Insert tm jdp fail record Fails.....");
//            return false;
//        }
//    }
//
//    @Override
//    public String getIdName() {
//        return "id";
//    }
//
//    public static void clearOld() {
//        int maxCount = 2048;
//        int count = 0;
//        long start = System.currentTimeMillis() - DateUtil.THIRTY_DAYS;
//
//        while (count++ < maxCount) {
//            long updateNum = JDBCBuilder.update(false, src, "delete from " + TABLE_NAME
//                    + "  where created < ? order by id ", start);
//            //long updateNum = TMJdpFailRecord.delete("created < ? order by id", start);
//            if (updateNum <= 0) {
//                return;
//            }
//        }
//    }
//
//    static String selectField = " `id`,`userId`,`msg`,`type`,`failCount`,`created`,`updated` ";
//
//    public static List<TMUserWindowInfo> fetch(String whereQuery, Object... args) {
//        return new JDBCExecutor<List<TMUserWindowInfo>>(
//                " select " + selectField + "  from " + TABLE_NAME + " where " + whereQuery, args) {
//
//            @Override
//            public List<TMUserWindowInfo> doWithResultSet(ResultSet rs) throws SQLException {
//                List<TMUserWindowInfo> list = new ArrayList<TMUserWindowInfo>();
//                while (rs.next()) {
//                    list.add(new TMUserWindowInfo(rs));
//                }
//                return list;
//            }
//
//        }.call();
//    }
//
//    public TMUserWindowInfo(ResultSet rs) throws SQLException {
//        this.id = rs.getLong(1);
//        this.userId = rs.getLong(2);
//        this.msg = rs.getString(3);
//        this.type = rs.getString(4);
//        this.failCount = rs.getInt(5);
//        this.created = rs.getLong(6);
//        this.updated = rs.getLong(7);
//    }
//
//    public static void batchWrite(List<TMUserWindowInfo> records) {
//        if (CommonUtils.isEmpty(records)) {
//            return;
//        }
//
//        StringBuilder sb = new StringBuilder("insert into " + TABLE_NAME + insertFields + " values");
//
//        Iterator<TMUserWindowInfo> it = records.iterator();
//        while (it.hasNext()) {
//            TMUserWindowInfo TMJdpFailRecord = it.next();
//
//            sb.append('(');
//            sb.append(TMJdpFailRecord.userId);
//            sb.append(",'");
//            sb.append(CommonUtils.escapeSQL(TMJdpFailRecord.msg));
//            sb.append("',");
//            sb.append(",'");
//            sb.append(CommonUtils.escapeSQL(TMJdpFailRecord.type.toString()));
//            sb.append("',");
//            sb.append(TMJdpFailRecord.failCount);
//            sb.append(",");
//            sb.append(TMJdpFailRecord.created);
//            sb.append(",");
//            sb.append(TMJdpFailRecord.updated);
//            sb.append(")");
//
//            if (it.hasNext()) {
//                sb.append(',');
//            }
//        }
//
//        JDBCBuilder.insert(false, true, sb.toString());
//    }
//
//    @JsonAutoDetect
//    public static class TMWorkMsg implements Serializable {
//        @JsonProperty
//        String type;
//
//        @JsonProperty
//        String msg;
//
//        @JsonProperty
//        Long userId;
//
//        public TMWorkMsg(Long userId, String type, String msg) {
//            super();
//            this.type = type;
//            this.msg = msg;
//            this.userId = userId;
//        }
//
//        public String getType() {
//            return type;
//        }
//
//        public void setType(String type) {
//            this.type = type;
//        }
//
//        public String getMsg() {
//            return msg;
//        }
//
//        public void setMsg(String msg) {
//            this.msg = msg;
//        }
//
//        public Long getUserId() {
//            return userId;
//        }
//
//        public void setUserId(Long userId) {
//            this.userId = userId;
//        }
//
//    }
//
//    @Every("20s")
//    public static class TMJdpFailWritter extends Job {
//
//        static Queue<TMUserWindowInfo> queue = new ConcurrentLinkedQueue<TMUserWindowInfo>();
//
//        public void doJob() {
//            TMUserWindowInfo msg = null;
//            while ((msg = queue.poll()) != null) {
//                msg.save();
//            }
//        }
//
//        public static Queue<TMUserWindowInfo> getQueue() {
//            return queue;
//        }
//
//        public static void addMsg(User user, Set<Long> failIds) {
//            int size = failIds.size();
//            String msg = StringUtils.join(failIds, ',');
//
//            if (msg.length() > 8000) {
//                msg = msg.substring(0, 8000);
//            }
//            queue.add(new TMUserWindowInfo(StringUtils.EMPTY, user.getId(), msg, size));
//        }
//
//    }
//
//    @Override
//    public String getTableName() {
//        return TABLE_NAME;
//    }
//
//    @Override
//    public String getTableHashKey() {
//        return null;
//    }
//
//    @Override
//    public String getIdColumn() {
//        return "id";
//    }
//
//    @Override
//    public void setId(Long id) {
//        this.id = id;
//    }
}
