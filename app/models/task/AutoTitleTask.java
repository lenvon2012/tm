
package models.task;

import static java.lang.String.format;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import models.oplog.TitleOpRecord;
import models.user.User;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.annotations.Index;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import utils.DateUtil;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.NumberUtil;

@Entity(name = AutoTitleTask.TABLE_NAME)
@JsonIgnoreProperties(value = {
        "dataSrc", "persistent", "entityId",
        "entityId", "ts", "numIid", "detailURL", "sellerCids", "tableHashKey", "persistent", "tableName",
        "idName", "idColumn",
})
public class AutoTitleTask extends Model implements PolicySQLGenerator {

    private final static Logger log = LoggerFactory.getLogger(AutoTitleTask.class);

    public final static String TABLE_NAME = "auto_title_task_";

    public static AutoTitleTask EMPTY = new AutoTitleTask();

    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

    @Index(name = "userId")
    private Long userId;

    @Column(columnDefinition = "varchar(2000) default '' ")
    private String configJson;

    /**
     * {"5.6":"xxxxx"}
     */
    @Column(columnDefinition = "varchar(2046) default '' ")
    private String results;

    private int status = UserTaskStatus.New;

    public static class UserTaskStatus {

        public static final int New = 1;

        public static final int InRunPool = 2;

        public static final int Doing = 4;

        public static final int Finished = 8;

        public static final int Failed = 16;

        public static final int Suspend = 32;

        public static final int Deleted = 64;
    }

    private int type;

    public static class UserTaskType {

        public static final int BuildAutoTitle = 1;//生成标题

        public static final int BuildPhoneDetailByTaobaoZhuli = 2;//生成手机详情页

        public static final int BuildPhoneDetailByNumIids = 3;//生成手机详情页
    }

    private String message;

    private long createTime;

    private long finishedTime;

    private long usedTime;

    private int totalNum;

    private int successNum;

    private int runCount;//运行次数

    @Transient
    private int progress; // 任务进度

    @Column(columnDefinition = "bigint(20) default 0")
    private Long titleOpId = NumberUtil.DEFAULT_LONG;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getConfigJson() {
        return configJson;
    }

    public void setConfigJson(String configJson) {
        this.configJson = configJson;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public Long getTaskId() {
        return this.id;
    }

    public void addRunCount() {
        this.runCount++;
    }

    public long getFinishedTime() {
        return finishedTime;
    }

    public void setFinishedTime(long finishedTime) {
        this.finishedTime = finishedTime;
    }

    public long getUsedTime() {
        return usedTime;
    }

    public void setUsedTime(long usedTime) {
        this.usedTime = usedTime;
    }

    public int getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(int totalNum) {
        this.totalNum = totalNum;
    }

    public int getSuccessNum() {
        return successNum;
    }

    public void setSuccessNum(int successNum) {
        this.successNum = successNum;
    }

    public int getRunCount() {
        return runCount;
    }

    public void setRunCount(int runCount) {
        this.runCount = runCount;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public AutoTitleTask() {
        super();
    }

    public AutoTitleTask(Long userId, String configJson, int status, int type) {
        super();

        log.info(format("AutoTitleTask:userId, configJson, status, type".replaceAll(", ", "=%s, ") + "=%s", userId,
                configJson, status, type));

        this.userId = userId;
        this.configJson = configJson;
        this.status = status;
        this.type = type;
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

    public void updateTaskResult(long finishedTime, long usedTime, int status,
            int totalNum, int successNum, String message) {

        log.error(format(
                ">>>>>>>>>>>>>>>updateTaskResult:finishedTime, usedTime, status, totalNum, successNum, message"
                        .replaceAll(", ",
                                "=%s, ") + "=%s", finishedTime, usedTime, status, totalNum, successNum, message));

        this.finishedTime = finishedTime;
        this.usedTime = usedTime;
        this.status = status;
        this.totalNum = totalNum;
        this.successNum = successNum;
        this.message = message;
    }

    public boolean isBuildAutoTitleTask() {
        return type == UserTaskType.BuildAutoTitle;
    }

    public boolean isPhoneDetail() {
        return this.type == UserTaskType.BuildPhoneDetailByTaobaoZhuli;
    }

    public static long findExistId(Long id, Long userId) {

        String query = "select id from " + TABLE_NAME + " where id = ? and userId = ?";

        return dp.singleLongQuery(query, id, userId);
    }

    @Override
    public boolean jdbcSave() {

        try {
            long existId = findExistId(this.id, userId);

            if (existId <= 0) {
                return this.rawInsert();
            } else {
                return this.rawUpdate();
            }

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return false;
        }

    }

    @Override
    public String getIdName() {
        return "id";
    }

    public boolean rawInsert() {
        try {

            String insertSQL = "insert into `" + TABLE_NAME +
                    "`(`userId`,`configJson`,`status`,`type`," +
                    "`message`,`createTime`,`finishedTime`,`usedTime`,`totalNum`,`successNum`," +
                    "`runCount`,`titleOpId`,`results`) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";

            this.createTime = System.currentTimeMillis();

            long id = dp.insert(insertSQL, this.userId, this.configJson, this.status, this.type,
                    this.message, this.createTime, this.finishedTime, this.usedTime, this.totalNum, this.successNum,
                    this.runCount, this.titleOpId, this.results);

            if (id > 0L) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {

            log.error(e.getMessage(), e);
            return false;

        }

    }

    public boolean rawUpdate() {

        String updateSQL = "update `" + TABLE_NAME +
                "` set `userId` = ?, `configJson` = ?, `status` = ?, " +
                "`type` = ?, `message` = ?, `createTime` = ?, `finishedTime` = ?, " +
                "`usedTime` = ?, `totalNum` = ?, `successNum` = ?, " +
                "`runCount` = ?, `titleOpId` = ?, `results` = ?  where `id` = ? and userId = ? ";

        long updateNum = dp.insert(updateSQL, this.userId, this.configJson, this.status,
                this.type, this.message, this.createTime, this.finishedTime, this.usedTime, this.totalNum,
                this.successNum, this.runCount, this.titleOpId, this.results, this.id, this.userId);

        if (updateNum == 1) {

            return true;
        } else {

            return false;
        }
    }

    public static boolean checkHasTheSameTask(Long userId, int taskType) {

        if (userId == null || userId <= 0L) {
            log.error("campaignId is null!!!!!!!!!!!!!!");
            return true;
        }

        String query = "select id from " + TABLE_NAME + " where userId = ? and status < ? and type = ?";
        long id = dp.singleLongQuery(query, userId, UserTaskStatus.Finished, taskType);
        if (id > 0L) {
            return true;
        } else {
            return false;
        }
    }

    public static void hasRecentSame() {

    }

    public static List<AutoTitleTask> queryDoingTasks() {
        String query = "select " + SelectAllProperties + " from " + TABLE_NAME + " where status = ? order by id asc ";

        return new JDBCBuilder.JDBCExecutor<List<AutoTitleTask>>(dp, query, UserTaskStatus.Doing) {

            @Override
            public List<AutoTitleTask> doWithResultSet(ResultSet rs)
                    throws SQLException {

                return queryListByJDBC(rs);
            }

        }.call();

    }

    public static List<AutoTitleTask> queryInRunPoolTasks() {
        String query = "select " + SelectAllProperties + " from " + TABLE_NAME + " where status = ? order by id asc ";

        return new JDBCBuilder.JDBCExecutor<List<AutoTitleTask>>(dp, query, UserTaskStatus.InRunPool) {

            @Override
            public List<AutoTitleTask> doWithResultSet(ResultSet rs)
                    throws SQLException {

                return queryListByJDBC(rs);
            }

        }.call();

    }

    public static List<AutoTitleTask> queryNewTasks(int taskTheadNum) {
        String query = "select " + SelectAllProperties + " from " + TABLE_NAME
                + " where status = ? order by id asc limit ?,? ";

        return new JDBCExecutor<List<AutoTitleTask>>(dp, query, UserTaskStatus.New, 0, taskTheadNum) {

            @Override
            public List<AutoTitleTask> doWithResultSet(ResultSet rs)
                    throws SQLException {

                return queryListByJDBC(rs);
            }

        }.call();

    }

    public static AutoTitleTask queryByTaskId(Long userId, Long taskId) {
        String query = "select " + SelectAllProperties + " from " + TABLE_NAME
                + " where userId = ? and id = ?  ";

        return new JDBCBuilder.JDBCExecutor<AutoTitleTask>(dp, query, userId, taskId) {

            @Override
            public AutoTitleTask doWithResultSet(ResultSet rs)
                    throws SQLException {

                return queryByJDBC(rs);
            }

        }.call();
    }

    public static List<AutoTitleTask> queryUserUnFinishedTask(Long userId) {
        String query = "select " + SelectAllProperties + " from " + TABLE_NAME
                + " where userId = ? and status < ? order by id asc ";

        return new JDBCBuilder.JDBCExecutor<List<AutoTitleTask>>(dp, query, userId,
                UserTaskStatus.Finished) {

            @Override
            public List<AutoTitleTask> doWithResultSet(ResultSet rs)
                    throws SQLException {

                return queryListByJDBC(rs);
            }

        }.call();

    }
    
    public static List<AutoTitleTask> queryUserTask(Long userId,
    		int offset, int limit) {
        String query = "select " + SelectAllProperties + " from " + TABLE_NAME
                + " where userId = ? order by id desc limit ?,?";

        return new JDBCBuilder.JDBCExecutor<List<AutoTitleTask>>(dp, query, userId,
        		offset, limit) {

            @Override
            public List<AutoTitleTask> doWithResultSet(ResultSet rs)
                    throws SQLException {

                return queryListByJDBC(rs);
            }

        }.call();

    }

    public static List<AutoTitleTask> queryUserFinishedTask(Long userId, int offset, int limit) {
        String query = "select " + SelectAllProperties + " from " + TABLE_NAME
                + " where userId = ? and status >= ? order by id desc limit ?,?  ";

        return new JDBCBuilder.JDBCExecutor<List<AutoTitleTask>>(dp, query, userId,
                UserTaskStatus.Finished, offset, limit) {

            @Override
            public List<AutoTitleTask> doWithResultSet(ResultSet rs)
                    throws SQLException {

                return queryListByJDBC(rs);
            }

        }.call();

    }

    public static long countUserFinishedTask(Long userId) {
        String query = "select count(*) from " + TABLE_NAME + " where userId = ? and status >= ?  ";

        return dp.singleLongQuery(query, userId, UserTaskStatus.Finished);

    }
    
    public static long countUserTask(Long userId) {
        String query = "select count(*) from " + TABLE_NAME + " where userId = ? ";

        return dp.singleLongQuery(query, userId);

    }

    public static long countUserUnFinishedTask(Long userId) {
        String query = "select count(*) from " + TABLE_NAME + " where userId = ?  and status < ?  ";

        return dp.singleLongQuery(query, userId, UserTaskStatus.Finished);

    }

    private static AutoTitleTask queryByJDBC(ResultSet rs) throws SQLException {

        if (rs.next()) {

            return parseAutoTitleTask(rs);

        } else {
            return null;
        }

    }

    private static List<AutoTitleTask> queryListByJDBC(ResultSet rs) throws SQLException {
        List<AutoTitleTask> taskList = new ArrayList<AutoTitleTask>();
        while (rs.next()) {
            AutoTitleTask task = parseAutoTitleTask(rs);

            if (task == null) {
                continue;
            }
            taskList.add(task);
        }

        return taskList;
    }

    private static final String SelectAllProperties = "id,userId,configJson,status,type," +
            "message,createTime,finishedTime,usedTime,totalNum,successNum," +
            "runCount,titleOpId,results";

    private static AutoTitleTask parseAutoTitleTask(ResultSet rs) {
        try {
            AutoTitleTask task = new AutoTitleTask();
            task.setId(rs.getLong(1));
            task.setUserId(rs.getLong(2));
            task.setConfigJson(rs.getString(3));
            task.setStatus(rs.getInt(4));
            task.setType(rs.getInt(5));
            task.setMessage(rs.getString(6));
            task.setCreateTime(rs.getLong(7));
            task.setFinishedTime(rs.getLong(8));
            task.setUsedTime(rs.getLong(9));
            task.setTotalNum(rs.getInt(10));
            task.setSuccessNum(rs.getInt(11));
            task.setRunCount(rs.getInt(12));
            task.setTitleOpId(rs.getLong(13));
            task.setResults(rs.getString(14));
            return task;

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

    public Long getTitleOpId() {
        return titleOpId;
    }

    public void setTitleOpId(Long titleOpId) {
        this.titleOpId = titleOpId;
    }

    public void setTitleOpRecord(TitleOpRecord logRecord) {
        if (logRecord == null) {
            return;
        }
        log.error("[logrecord id :]" + logRecord.getId());
        setTitleOpId(logRecord.getId());
    }

    @JsonAutoDetect
    @JsonIgnoreProperties(value = {
            "dataSrc", "persistent", "entityId",
            "entityId", "ts", "numIid", "detailURL", "sellerCids", "tableHashKey", "persistent", "tableName",
            "idName", "idColumn",
    })
    public static class WireLessDetailConfig {

        public static WireLessDetailConfig gen(String src) {
            WireLessDetailConfig config = new WireLessDetailConfig();
            try {
                JSONObject json = new JSONObject(src);
                if (json.has("numIids")) {
                    config.numIids = json.getString("numIids");
                }
                if (json.has("autoSplit")) {
                    config.autoSplit = json.getBoolean("autoSplit");
                }
                if (json.has("autoProp")) {
                    config.autoProp = json.getBoolean("autoProp");
                }
                if (json.has("skipExist")) {
                    config.skipExist = json.getBoolean("skipExist");
                }
                if (json.has("filePath")) {
                    config.filePath = json.getString("filePath");
                }
                if (json.has("sellerCid")) {
                    config.sellerCid = json.getLong("sellerCid");
                }
                if (json.has("itemCat")) {
                    config.itemCat = json.getLong("itemCat");
                }
                if (json.has("notStatus")) {
                    config.notStatus = json.getInt("notStatus");
                }
            } catch (JSONException e) {
                log.warn(e.getMessage(), e);
            }

            return config;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (autoProp ? 1231 : 1237);
            result = prime * result + (autoSplit ? 1231 : 1237);
            result = prime * result + ((filePath == null) ? 0 : filePath.hashCode());
            result = prime * result + ((itemCat == null) ? 0 : itemCat.hashCode());
            result = prime * result + notStatus;
            result = prime * result + ((numIids == null) ? 0 : numIids.hashCode());
            result = prime * result + ((sellerCid == null) ? 0 : sellerCid.hashCode());
            result = prime * result + (skipExist ? 1231 : 1237);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            WireLessDetailConfig other = (WireLessDetailConfig) obj;
            if (autoProp != other.autoProp)
                return false;
            if (autoSplit != other.autoSplit)
                return false;
            if (filePath == null) {
                if (other.filePath != null)
                    return false;
            } else if (!filePath.equals(other.filePath))
                return false;
            if (itemCat == null) {
                if (other.itemCat != null)
                    return false;
            } else if (!itemCat.equals(other.itemCat))
                return false;
            if (notStatus != other.notStatus)
                return false;
            if (numIids == null) {
                if (other.numIids != null)
                    return false;
            } else if (!numIids.equals(other.numIids))
                return false;
            if (sellerCid == null) {
                if (other.sellerCid != null)
                    return false;
            } else if (!sellerCid.equals(other.sellerCid))
                return false;
            if (skipExist != other.skipExist)
                return false;
            return true;
        }

        @JsonProperty
        String numIids;

        @JsonProperty
        boolean autoSplit = true;

        @JsonProperty
        boolean autoProp = true;

        @JsonProperty
        boolean skipExist = true;

        @JsonProperty
        String filePath = null;

        @JsonProperty
        Long sellerCid = 0L;

        @JsonProperty
        Long itemCat = 0L;

        @JsonProperty
        int notStatus = -1;

        public Long getSellerCid() {
            return sellerCid;
        }

        public void setSellerCid(Long sellerCid) {
            this.sellerCid = sellerCid;
        }

        public Long getItemCat() {
            return itemCat;
        }

        public void setItemCat(Long itemCat) {
            this.itemCat = itemCat;
        }

        public int getNotStatus() {
            return notStatus;
        }

        public void setNotStatus(int notStatus) {
            this.notStatus = notStatus;
        }

        public String getNumIids() {
            return numIids;
        }

        public void setNumIids(String numIids) {
            this.numIids = numIids;
        }

        public boolean isAutoSplit() {
            return autoSplit;
        }

        public void setAutoSplit(boolean autoSplit) {
            this.autoSplit = autoSplit;
        }

        public boolean isAutoProp() {
            return autoProp;
        }

        public void setAutoProp(boolean autoProp) {
            this.autoProp = autoProp;
        }

        public boolean isSkipExist() {
            return skipExist;
        }

        public void setSkipExist(boolean skipExist) {
            this.skipExist = skipExist;
        }

        public WireLessDetailConfig() {
            super();
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public String toString() {
            return "WireLessDetailConfig [numIids=" + numIids + ", autoSplit=" + autoSplit + ", autoProp=" + autoProp
                    + ", skipExist=" + skipExist + ", filePath=" + filePath + "]";
        }

    }

    public static boolean hasRecentSame(User user, String configJson, int taskType) {
        Long userId = user.getId();

        String query = "select id from " + TABLE_NAME
                + " where userId = ? and status < ? and type = ? and createTime > ? and configJson = ?";
        long id = dp.singleLongQuery(query, userId, UserTaskStatus.Finished, taskType, System.currentTimeMillis()
                - (DateUtil.ONE_MINUTE_MILLIS * 5), configJson);

        if (id > 0L) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean hasRecentSame(User user, int taskType) {
        Long userId = user.getId();

        String query = "select id from " + TABLE_NAME
                + " where userId = ? and status < ? and type = ? and createTime > ? ";
        long id = dp.singleLongQuery(query, userId, UserTaskStatus.Finished, taskType, System.currentTimeMillis()
                - DateUtil.ONE_MINUTE_MILLIS);

        if (id > 0L) {
            return true;
        } else {
            return false;
        }
    }

    public WireLessDetailConfig genWirelessConfig() {
        return WireLessDetailConfig.gen(this.configJson);
    }

    public Map genPageRes() {
        HashMap map = JsonUtil.toObject(this.results, HashMap.class);
        return map;
    }

    public String getResults() {
        return results;
    }

    public void setResults(String results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "AutoTitleTask [userId=" + userId + ", configJson=" + configJson + ", results=" + results + ", status="
                + status + ", type=" + type + ", message=" + message + ", createTime=" + createTime + ", finishedTime="
                + finishedTime + ", usedTime=" + usedTime + ", totalNum=" + totalNum + ", successNum=" + successNum
                + ", runCount=" + runCount + ", progress=" + progress + ", titleOpId=" + titleOpId + "]";
    }

}
