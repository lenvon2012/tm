package models.carrierTask;

import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;
import com.ciaosir.client.pojo.PageOffset;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.db.jpa.Model;
import transaction.DBBuilder;
import transaction.JDBCBuilder;
import utils.DateUtil;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/3/7.
 */
@JsonIgnoreProperties(value = { "dataSrc", "persistent", "entityId", "entityId", "ts",
        "tableHashKey", "persistent", "tableName", "idName", "idColumn" })
@Entity(name = CarrierTask.TABLE_NAME)
public class CarrierTask extends Model implements PolicySQLGenerator {

    @Transient
    public static final String TABLE_NAME = "carrier_task";

    @Transient
    public static final Logger log = LoggerFactory.getLogger(CarrierTask.class);

    @Transient
    public static CarrierTask EMPTY = new CarrierTask();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DBBuilder.DataSrc.BASIC, EMPTY);

    @Transient
    private static final String ALL_PROPERTIES = " id, ww, publisher, babyCnt, finishCnt, sid, pullTs, finishTs, status, createTs, taskType, itemCarryCustomId";

    @Index(name = "ww")
    private String ww;

    @Index(name = "publisher")
    private String publisher;

    private int babyCnt;

    private int finishCnt;

    private long sid;

    @Index(name = "pullTs")
    private long pullTs;

    @Index(name = "finishTs")
    private long finishTs;

    @Index(name = "status")
    private int status;

    private long createTs;

    @Index(name = "taskType")
    private int taskType;

    private long itemCarryCustomId;

    public static void finishTask(long id) {
        String query = "update `" + TABLE_NAME + "` set `status` = ? where id = ?";
        dp.update(query, CarrierTaskStatus.finished, id);
    }

    public static boolean findByWW(String ww, String publisher) {
        long ts = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000;
        String query = "select id from " + TABLE_NAME + " where ww = ? and publisher = ? and createTs > ?";
        return dp.singleLongQuery(query, ww, publisher, ts) > 0;
    }

    public static void reduceFinishCnt(Long id) {

        String update = "update " +TABLE_NAME + " set finishCnt = finishCnt - 1 where id = ?";

        dp.update(update, id);
    }

    public static class CarrierTaskStatus {
        public static final int preparing = 0; //还未被客户端取走任务
        public static final int prepared = 1;
        public static final int running = 2;
        public static final int finished = 4;
    }

    public static class CarrierTaskType {
        public static final int shop = 1; //店铺复制任务
        public static final int batch = 2;//批量复制任务
        public static final int batch1688 = 3;//批量复制任务1688
    }

    public String getTaskTypeStr() {
        if (taskType == CarrierTaskType.shop) {
            return "全店复制宝贝";
        } else if (taskType == CarrierTaskType.batch) {
            return "批量复制宝贝";
        } else if (taskType == CarrierTaskType.batch1688) {
            return "批量复制宝贝(1688)";
        }
        return "";
    }

    public String getStatusStr() {
        if (status == CarrierTaskStatus.preparing) {
            return "正在查询店铺信息";
        } else if (status == CarrierTaskStatus.prepared) {
            return "已获取宝贝信息，等待复制宝贝";
        } else if (status == CarrierTaskStatus.running) {
            return "正在复制宝贝(" + this.finishCnt + "/" + this.babyCnt + ")";
        } else if (status == CarrierTaskStatus.finished) {
            return "任务已完成";
        }
        return "";
    }

    public String getCreateTsStr() {
        return DateUtil.sdf.format(new Date(this.createTs));
    }

    public long saveBatchTask() {
        String insertSQL = "insert into `" + TABLE_NAME + "`(`ww`,`sid`,`babyCnt`,`publisher`," +
                "`pullTs`,`finishTs`,`status`,`createTs`,`finishCnt`,`taskType`,`itemCarryCustomId`) values(?,?,?,?,?,?,?,?,?,?,?)";

        log.info("[Insert carrier_task Id:]" + this.id);
        this.createTs = System.currentTimeMillis();
        return dp.insert(insertSQL, this.ww, this.sid, this.babyCnt, this.publisher,
                this.pullTs, this.finishTs, this.status, this.createTs, this.finishCnt, this.taskType, this.itemCarryCustomId);
    }

    public static int countCurrentTaskByNick(String nick) {
        String query = "select count(*) from " + TABLE_NAME + " where publisher = ? and status <= ?";
        return (int)dp.singleLongQuery(query, nick, CarrierTaskStatus.running);
    }

    public static int countHistoryTaskByNick(String nick) {
        String query = "select count(*) from " + TABLE_NAME + " where publisher = ? and status = ?";
        return (int)dp.singleLongQuery(query, nick, CarrierTaskStatus.finished);
    }

    public static CarrierTask findByTaskId(long taskId) {
        String query = "select " + ALL_PROPERTIES + " from " + TABLE_NAME + " where id = ?";
        return findByJDBC(query, taskId);
    }

    public static List<CarrierTask> findTaskShopCarryToday(String publisher) {
        long start = DateUtil.formDailyTimestamp(System.currentTimeMillis());
        long end = start + DateUtil.DAY_MILLIS;

        String sql = "select " + ALL_PROPERTIES + " from " + TABLE_NAME +" where publisher = ? and taskType = ? and createTs >= ? and createTs < ?";

        return findListByJDBC(sql, publisher, CarrierTaskType.shop, start, end);
    }

    public static Boolean rebootTask(Long taskId) {
        Long createTs = System.currentTimeMillis();
        String update = "update `" + TABLE_NAME + "` set `status` = " + CarrierTaskStatus.preparing + ", `createTs` = ?, pullTs = 0, finishTs = 0, babyCnt = 0,finishCnt = 0 " +
                "where id = ? and status in (" + CarrierTaskStatus.preparing + ", " + CarrierTaskStatus.prepared + ", " + CarrierTaskStatus.running + ")";

        return dp.update(update, createTs, taskId) > 0;
    }
    
    public static Boolean cancelTask(Long taskId) {
        Long finishTs = System.currentTimeMillis();
        String update = "update `" + TABLE_NAME + "` set `status` = " + CarrierTaskStatus.finished + ", finishTs = ? " +
                "where id = ? and status in (" + CarrierTaskStatus.preparing + ", " + CarrierTaskStatus.prepared + ", " + CarrierTaskStatus.running + ")";

        return dp.update(update, finishTs, taskId) > 0;
    }

    public static Boolean deleteTask(Long taskId) {
        String update = "delete from " + TABLE_NAME + " where id = ? and status in (" + CarrierTaskStatus.preparing + ", " + CarrierTaskStatus.prepared + ", " + CarrierTaskStatus.running + ")";

        return dp.update(update, taskId) > 0;
    }

    public static boolean finishShopInfo(long sid) {
        long finishTs = System.currentTimeMillis();
        String query = "update `" + TABLE_NAME + "` set `status` = 1, `finishTs` = ? where id = ?";
        return dp.update(query, finishTs, sid) > 0;
    }

    public static List<CarrierTask> fetchUnfinishedListByUserNick(String publisher, PageOffset po, boolean isDesc) {
        String query = "select " + ALL_PROPERTIES + " from " + TABLE_NAME + " where publisher = ? and status < ? order by createTs";
        if (isDesc == true) {
            query += " desc";
        }
        query += " limit ?, ?";
        return findListByJDBC(query, publisher, CarrierTaskStatus.finished, po.getOffset(), po.getPs());
    }

    public static List<CarrierTask> fetchFinishedListByUserNick(String publisher, PageOffset po, boolean isDesc) {
        String query = "select " + ALL_PROPERTIES + " from " + TABLE_NAME + " where publisher = ? and status = ? order by createTs";
        if (isDesc == true) {
            query += " desc";
        }
        query += " limit ?, ?";
        return findListByJDBC(query, publisher, CarrierTaskStatus.finished, po.getOffset(), po.getPs());
    }

    public CarrierTask() {
    }

    public CarrierTask(long sid, String ww, String publisher, int taskType, long itemCarryCustomId) {
        this.sid = sid;
        this.ww = ww;
        this.publisher = publisher;
        this.taskType = taskType;
        this.itemCarryCustomId = itemCarryCustomId;
    }

    public CarrierTask(String publisher, int taskType, int babyCnt, int status, long itemCarryCustomId) {
        this.publisher = publisher;
        this.taskType = taskType;
        this.babyCnt = babyCnt;
        this.status = status;
        this.itemCarryCustomId = itemCarryCustomId;
    }

    public CarrierTask(String ww, String publisher, int taskType, int babyCnt, int status, long itemCarryCustomId) {
        this.ww = ww;
        this.publisher = publisher;
        this.taskType = taskType;
        this.babyCnt = babyCnt;
        this.status = status;
        this.itemCarryCustomId = itemCarryCustomId;
    }



    public CarrierTask(String ww, String publisher, int babyCnt, long sid, long pullTs, long finishTs, int status) {
        this.ww = ww;
        this.publisher = publisher;
        this.babyCnt = babyCnt;
        this.sid = sid;
        this.pullTs = pullTs;
        this.finishTs = finishTs;
        this.status = status;
    }

    public int getFinishCnt() {
        return finishCnt;
    }

    public void setFinishCnt(int finishCnt) {
        this.finishCnt = finishCnt;
    }

    public long getCreateTs() {
        return createTs;
    }

    public void setCreateTs(long createTs) {
        this.createTs = createTs;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getWw() {
        return ww;
    }

    public void setWw(String ww) {
        this.ww = ww;
    }

    public int getBabyCnt() {
        return babyCnt;
    }

    public void setBabyCnt(int babyCnt) {
        this.babyCnt = babyCnt;
    }

    public long getSid() {
        return sid;
    }

    public void setSid(long sid) {
        this.sid = sid;
    }

    public long getPullTs() {
        return pullTs;
    }

    public void setPullTs(long pullTs) {
        this.pullTs = pullTs;
    }

    public long getFinishTs() {
        return finishTs;
    }

    public void setFinishTs(long finishTs) {
        this.finishTs = finishTs;
    }

    public int getTaskType() {
        return taskType;
    }

    public void setTaskType(int taskType) {
        this.taskType = taskType;
    }

    public long getItemCarryCustomId() {
        return itemCarryCustomId;
    }

    public CarrierTask setItemCarryCustomId(long itemCarryCustomId) {
        this.itemCarryCustomId = itemCarryCustomId;
        return this;
    }

    @Override
    public String getTableName() {
        return null;
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

    public static void addBabyCnt(long taskId, int count) {
        String query = "update " + TABLE_NAME + " set babyCnt = babyCnt + ? where id = ?";
        dp.update(query, count, taskId);
    }

    // 完成数量添加count个 并状态更新为已经完成
    public static void addFinishCnt(long taskId, long count) {
        String query = "update " + TABLE_NAME + " set finishCnt = finishCnt + ?, status = ? where id = ?";
        dp.update(query, count, CarrierTaskStatus.finished, taskId);
    }

    public static List<CarrierTask> findSidList() {
    	//不爬取 1688批量复制任务
        long ts = System.currentTimeMillis() - 5 * 60 * 1000;
        String query = "select " + ALL_PROPERTIES + " from `" + TABLE_NAME + "` where pullTs < ?" +
        		" and babyCnt = 0 and taskType != ? order by id desc limit 5";
        return findListByJDBC(query, ts,CarrierTaskType.batch1688);
    }
    
    public static List<CarrierTask> find1688BatchTasks() {
    	//不爬取 1688批量复制任务
//        long ts = System.currentTimeMillis() - 5 * 60 * 1000;
        String query = "select " + ALL_PROPERTIES + " from `" + TABLE_NAME + "` where " +
        		"  babyCnt > 0 and taskType = ? and status = ? order by id desc limit 5";
        return findListByJDBC(query,CarrierTaskType.batch1688,CarrierTaskStatus.prepared);
    }

    private static CarrierTask findByJDBC(String query, Object...params) {

        return new JDBCBuilder.JDBCExecutor<CarrierTask>(dp, query, params) {

            @Override
            public CarrierTask doWithResultSet(ResultSet rs) throws SQLException {

                if (rs.next()) {
                    return parseCarrierTask(rs);
                }

                return null;
            }
        }.call();
    }

    private static List<CarrierTask> findListByJDBC(String query, Object...params) {

        return new JDBCBuilder.JDBCExecutor<List<CarrierTask>>(dp, query, params) {

            @Override
            public List<CarrierTask> doWithResultSet(ResultSet rs) throws SQLException {

                List<CarrierTask> carrierTasks = new ArrayList<CarrierTask>();

                while (rs.next()) {
                    CarrierTask carrierTask = parseCarrierTask(rs);
                    if (carrierTask != null) {
                        carrierTasks.add(carrierTask);
                    }
                }

                return carrierTasks;
            }
        }.call();
    }

    private static CarrierTask parseCarrierTask(ResultSet rs) {
        try {
            CarrierTask carrierTask = new CarrierTask();
            carrierTask.setId(rs.getLong(1));
            carrierTask.setWw(rs.getString(2));
            carrierTask.setPublisher(rs.getString(3));
            carrierTask.setBabyCnt(rs.getInt(4));
            carrierTask.setFinishCnt(rs.getInt(5));
            carrierTask.setSid(rs.getLong(6));
            carrierTask.setPullTs(rs.getLong(7));
            carrierTask.setFinishTs(rs.getLong(8));
            carrierTask.setStatus(rs.getInt(9));
            carrierTask.setCreateTs(rs.getLong(10));
            carrierTask.setTaskType(rs.getInt(11));
            carrierTask.setItemCarryCustomId(rs.getLong(12));
            return carrierTask;
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public static long findExistId(Long id) {
        String sql = "select id from " + TABLE_NAME + " where id = ?";
        return dp.singleLongQuery(sql, id);
    }

    @Override
    public boolean jdbcSave() {

        try {
            long existdId = findExistId(this.id);

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
        String insertSQL = "insert into `" + TABLE_NAME + "`(`ww`,`sid`,`babyCnt`,`publisher`," +
                "`pullTs`,`finishTs`,`status`,`createTs`,`finishCnt`,`taskType`,`itemCarryCustomId`) values(?,?,?,?,?,?,?,?,?,?,?)";

        this.createTs = System.currentTimeMillis();
        long id = dp.insert(insertSQL, this.ww, this.sid, this.babyCnt, this.publisher,
                this.pullTs, this.finishTs, this.status, this.createTs, this.finishCnt, this.taskType, this.itemCarryCustomId);

        log.info("[Insert carrier_task Id:]" + this.id);

        if (id > 0L) {
            setId(id);
            return true;
        } else {
            log.error("Insert Fails....." + this.id);
            return false;
        }

    }

    public static boolean deleteByPublisherAndww(String publisher, String ww) {
    	if(StringUtils.isEmpty(publisher) || StringUtils.isEmpty(ww)) {
    		return false;
    	}
        String query = "delete from " + TABLE_NAME + " where ww = ? and publisher = ?";
        return dp.update(query, ww, publisher) > 0;
    }
    
    public boolean rawUpdate() {
        String updateSQL = "update `" + TABLE_NAME + "` set `ww`=?,`sid`=?,`babyCnt`=?,`pullTs`=?," +
                "`finishTs`=?,`status`=?,`createTs`=?,`finishCnt`=?,`publisher`=?,`taskType`=?,`itemCarryCustomId`=? where `id`=? ";
        long updateNum = dp.update(updateSQL, this.ww, this.sid, this.babyCnt, this.pullTs, this.finishTs,
                this.status, this.createTs, this.finishCnt, this.publisher, this.taskType, this.itemCarryCustomId, this.id);

        if (updateNum == 1) {
            log.info("update ok for :" + this.id);
            return true;
        } else {
            log.error("update failed...for :" + this.id);
            return false;
        }
    }

    @Override
    public String getIdName() {
        return null;
    }

}
