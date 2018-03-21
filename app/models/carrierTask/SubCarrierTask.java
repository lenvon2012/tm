package models.carrierTask;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder;
import transaction.JDBCBuilder;
import utils.PlayUtil;
import actions.carriertask.BabyInfo;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;

/**
 * Created by Administrator on 2016/3/8.
 */
@JsonIgnoreProperties(value = {
        "dataSrc", "persistent", "entityId",
        "entityId", "ts", "detailURL", "sellerCids", "tableHashKey", "persistent", "tableName",
        "idName", "idColumn"
})
@Entity(name = SubCarrierTask.TABLE_NAME)
public class SubCarrierTask extends Model implements PolicySQLGenerator {

    @Transient
    public static final String TABLE_NAME = "sub_carrier_task";

    @Transient
    public static final Logger log = LoggerFactory.getLogger(SubCarrierTask.class);

    @Transient
    public static SubCarrierTask EMPTY = new SubCarrierTask();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DBBuilder.DataSrc.BASIC, EMPTY);

    @Transient
    private static final String ALL_PROPERTIES = " id, taskId, url, publisher, pn, finishTs, " +
            "createTs, pullTs, errorMsg, status, babyTitle, picUrl, subTaskType, cid, brand";

    @Transient
    static String PARAMS_NUM = "(?,?,?,?,?,?,?,?,?,?,?,?)";
    
    @Transient
    static String PARAMS_NUM_1688 = "(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    @Transient
    private static String INSERT_BASE = "insert into `" + TABLE_NAME + "`(`taskId`,`url`,`publisher`," +
            "`pn`,`finishTs`,`createTs`,`pullTs`,`errorMsg`,`status`, `babyTitle`, `picUrl`, `subTaskType`) values ";
    
    @Transient
    private static String INSERT_BASE_1688 = "insert into `" + TABLE_NAME + "`(`taskId`,`url`,`publisher`," +
            "`pn`,`finishTs`,`createTs`,`pullTs`,`errorMsg`,`status`, `babyTitle`, `picUrl`, `subTaskType`,`cid`,`brand`) values ";

    @Index(name = "taskId")
    private long taskId;

    private String url;

    @Index(name = "pn")
    private int pn;

    private String publisher;

    private String errorMsg;

    @Index(name = "status")
    private int status;

    @Index(name = "finishTs")
    private long finishTs;

    @Index(name = "babyTitle")
    private String babyTitle;

    private String picUrl;

    @Index(name = "pullTs")
    private long pullTs;

    private long createTs;

    private SubTaskType subTaskType;
    
    @Column(nullable=true)
    private long cid; //要复制的类目ID只针对1688复制
    @Column(nullable=true)
    private long brand; //要复制的类目ID只针对1688复制

    public enum SubTaskType {
        淘宝复制,$1688复制,天猫复制;
        @Override
        public String toString() {
            if (this.name().startsWith("$")) return this.name().substring(1);
            return this.name();
        }
    }

    public static class SubCarrierTaskStatus {
        public static final int waiting = 0;
        public static final int success = 1;
        public static final int failure = 2;
        public static final int cancel = 4;
    }

    public SubCarrierTask() {
    }

    public String getBabyTitle() {
        return babyTitle;
    }

    public void setBabyTitle(String babyTitle) {
        this.babyTitle = babyTitle;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public int getPn() {
        return pn;
    }

    public void setPn(int pn) {
        this.pn = pn;
    }

    public String getUrl() {
        return url;

    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getFinishTs() {
        return finishTs;
    }

    public void setFinishTs(long finishTs) {
        this.finishTs = finishTs;
    }

    public long getCreateTs() {
        return createTs;
    }

    public void setCreateTs(long createTs) {
        this.createTs = createTs;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public long getPullTs() {
        return pullTs;
    }

    public void setPullTs(long pullTs) {
        this.pullTs = pullTs;
    }

    public SubTaskType getSubTaskType() {
        return this.subTaskType;
    }

    public SubCarrierTask setSubTaskType(SubTaskType subTaskType) {
        this.subTaskType = subTaskType;
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

    public static int countByTaskId(long taskId) {
        String query = "select count(*) from " + TABLE_NAME + " where taskId = ?";
        return (int)dp.singleLongQuery(query, taskId);
    }

    public static int countByPublisherDay(String publisher) {
    	if(StringUtils.isEmpty(publisher)) {
    		return 0;
    	}
        String query = "select count(id) from " + TABLE_NAME + " where publisher = ? and pullTs = 0";
        return (int)dp.singleLongQuery(query, publisher);
    }

    public static List<SubCarrierTask> findListByTaskId(long taskId, PageOffset po) {
        log.info(po.getOffset() + " " + po.getPs() + " " + taskId);
        String query = "select " + ALL_PROPERTIES + " from " + TABLE_NAME + " where taskId = ? limit ?, ?";
        return findListByJDBC(query, taskId, po.getOffset(), po.getPs());
    }
    
    public static List<SubCarrierTask> findListByTaskId(long taskId) {
        log.info(" " + taskId);
        String query = "select " + ALL_PROPERTIES + " from " + TABLE_NAME + " where taskId = ? ";
        return findListByJDBC(query, taskId);
    }

    public void resetPullTs() {
        String query = "update " + TABLE_NAME + " set pullTs = 0 where id = ?";
        dp.update(query, this.id);
    }

    public static boolean updateBySubId(long id, int status) {
        return updateBySubId(id, status, null, null, null);
    }

    public static boolean updateBySubId(long id, int status, String errorMsg, String babyTitle, String picUrl) {
        String statusQuery = "select status from `" + TABLE_NAME + "` where id = ?";
        if (dp.singleLongQuery(statusQuery, id) == SubCarrierTaskStatus.success) {
            log.error("updateBySubId: duplicatePullTask");
            return false;
        }
        log.error("updateBySubId: " + id);
        long ts = System.currentTimeMillis();
        String query = "update `" + TABLE_NAME + "` set finishTs=?, status=?, errorMsg=?, babyTitle=?, picUrl=? where id = ?";
        log.error("picUrl: " + picUrl + " babyTitle: " + babyTitle);
        return dp.update(query, ts, status, errorMsg, babyTitle, picUrl, id) > 0;
    }

    public static List<SubCarrierTask> findClientFailedTask(long timeSpan, PageOffset po) {
        String query = "select " + ALL_PROPERTIES + " from " + TABLE_NAME + " where finishTs = 0 and pullTs > 0 and pullTs < ? and (subTaskType = 0 or subTaskType = 2) and status = 0 limit ?, ?";
        return findListByJDBC(query, timeSpan, po.getOffset(), po.getPs());
    }

    public static SubCarrierTask fetchSubTask() {
        String query = "select " + ALL_PROPERTIES + " from " + TABLE_NAME + " where finishTs = 0 limit 1";
        return findByJDBC(query);
    }

    public static List<SubCarrierTask> fetchTaskList(int pn, int ps) {
        int offset = (pn - 1) * ps;
        String sql = "select " + ALL_PROPERTIES + " from " + TABLE_NAME + " where finishTs = 0 limit ?, ?";
        return findListByJDBC(sql, offset, ps);
    }

    public static boolean isUploadedByPageAndTaskId(long taskId, int pn) {
        String query = "select id from `" + TABLE_NAME + "` where taskId = ? and pn = ?";
        return dp.singleLongQuery(query, taskId, pn) > 0;
    }

    public String getStatusStr() {
        if (this.status == SubCarrierTaskStatus.waiting) {
            return "等待执行";
        } else if (this.status == SubCarrierTaskStatus.success) {
            return "已完成";
        } else if (this.status == SubCarrierTaskStatus.failure) {
            return "任务失败";
        } else if (this.status == SubCarrierTaskStatus.cancel) {
            return "任务取消";
        }
        return "";
    }

    public String getImgStr() {
        if (StringUtils.isEmpty(picUrl)) {
            return "https://img.alicdn.com/imgextra/i4/2253471304/TB26sL5lFXXXXarXXXXXXXXXXXX_!!2253471304.png";
        } else {
            return picUrl;
        }
    }

    public static int finishedCountByTaskId(long id) {
        String query = "select count(*) from `" + TABLE_NAME + "` where taskId = ? and finishTs > 0";
        return (int)dp.singleLongQuery(query, id);
    }

    public static void updatePullTs(long id) {
        long ts = System.currentTimeMillis();
        String query = "update `" + TABLE_NAME + "` set `pullTs` = ? where id = ?";
        dp.update(query, ts, id);
    }

    public static int batchInsert(List<BabyInfo> list, long taskId, int pn, String publisher, SubTaskType subTaskType) {
        log.warn("batchInsert--->start" + new Date(System.currentTimeMillis()));
        if (CommonUtils.isEmpty(list)) {
            return 0;
        }

        Object[] res = new Object[] {};
        List<String> paramList = new ArrayList<String>();

        for (BabyInfo object : list) {
            paramList.add(PARAMS_NUM);
            res = ArrayUtils.addAll(res, new Object[]{taskId, object.getUrl(), publisher, pn, 0,
                    System.currentTimeMillis(), 0, "", SubCarrierTaskStatus.waiting, object.getBabyTitle(), object.getPicUrl(), subTaskType.ordinal()});
        }
        StringBuilder sb = new StringBuilder(INSERT_BASE);
        sb.append(StringUtils.join(paramList, ','));
        String sql = sb.toString();

        long num = dp.insert(sql, res);
        log.info(" batch insert sub_carrier_task num ;" + num);
        log.warn("batchInsert--->end" + new Date(System.currentTimeMillis()));
        return (int)num;
    }
    
    public static int batchInsert(List<BabyInfo> list, long taskId, int pn, String publisher, SubTaskType subTaskType, Long cid, Long brand) {
    	log.warn("batchInsert--->start" + new Date(System.currentTimeMillis()));
        if (CommonUtils.isEmpty(list)) {
            return 0;
        }

        Object[] res = new Object[] {};
        List<String> paramList = new ArrayList<String>();

        
        for (BabyInfo object : list) {
            paramList.add(PARAMS_NUM_1688);
            res = ArrayUtils.addAll(res, new Object[]{taskId, object.getUrl(), publisher, pn, 0,
                    System.currentTimeMillis(), System.currentTimeMillis(), "", SubCarrierTaskStatus.waiting,
                    object.getBabyTitle()==null?"":object.getBabyTitle(), object.getPicUrl()==null?"":object.getPicUrl(), subTaskType.ordinal(),cid==null?0:cid,brand==null?0:brand});
        }
        StringBuilder sb = new StringBuilder(INSERT_BASE_1688);
        sb.append(StringUtils.join(paramList, ','));
        String sql = sb.toString();

        long num = dp.insert(sql, res);
        log.info(" batch insert sub_carrier_task num ;" + num);
        log.warn("batchInsert--->end" + new Date(System.currentTimeMillis()));
        return (int)num;
		
	}


    public static long findExistId(Long id) {
        String sql = "select id from " + TABLE_NAME + " where id = ?";
        return dp.singleLongQuery(sql, id);
    }

    public static SubCarrierTask findById(Long id) {
        String sql = "select "+ALL_PROPERTIES+" from " + TABLE_NAME + " where id = ?";
        return findByJDBC(sql, id);
    }
    
	// 查询失败的子任务
	public static List<SubCarrierTask> findBySearchRules(Long startTime,
			Long endTime, String errorInfoKey, Long taskId, String goodUrl,
			String publisher, int taskStatus, int subTaskType, PageOffset po) {
		
		List<Object> paramList = new ArrayList<Object>();
		String whereSql = genWhereSqlByRules(startTime, endTime, errorInfoKey, taskId, goodUrl, publisher, taskStatus, subTaskType, paramList);

		String query = "select " + ALL_PROPERTIES + " from "
				+ TABLE_NAME + " where " + whereSql;

		query += " order by createTs desc";

		if(po != null) {
			query += " limit ?, ? ";
			paramList.add(po.getOffset());
			paramList.add(po.getPs());
		}
		Object[] paramArray = paramList.toArray();
		return findListByJDBC(query, paramArray);
	}
	
	private static String genWhereSqlByRules(Long startTime,
			Long endTime, String errorInfoKey, Long taskId, String goodUrl,
			String publisher, int taskStatus, Integer subTaskType, List<Object> paramList) {

		String whereSql = " 1=1 "; // 规范sql语句
		
		if(startTime != null && startTime > 0) {
			whereSql += " and createTs >= ?";
			paramList.add(startTime);
		}
		
		if(endTime != null && endTime > 0) {
			whereSql += " and createTs <= ?";
			paramList.add(endTime);
		}
		
		if(taskId != null && taskId > 0) {
			whereSql += " and taskId = ?";
			paramList.add(taskId);
		}
		
		if(taskStatus >= 0) {
			whereSql += " and status = ?";
			paramList.add(taskStatus);
		}
		
		publisher = PlayUtil.trimValue(publisher);
		if (!StringUtils.isEmpty(publisher)) {
			publisher = CommonUtils.escapeSQL(publisher);
			whereSql += " and publisher = ?";
			paramList.add(publisher);
		}
		
		goodUrl = PlayUtil.trimValue(goodUrl);
		if (!StringUtils.isEmpty(goodUrl)) {
			goodUrl = CommonUtils.escapeSQL(goodUrl);
			whereSql += " and url like '%" + goodUrl + "%'";
		}
		
		errorInfoKey = PlayUtil.trimValue(errorInfoKey);
		if (!StringUtils.isEmpty(errorInfoKey)) {
			errorInfoKey = CommonUtils.escapeSQL(errorInfoKey);
			whereSql += " and errorMsg like '%" + errorInfoKey + "%' ";
		}

		if (subTaskType >= 0) {
			whereSql += " and subTaskType = ?";
			paramList.add(subTaskType);
		}

		
		return whereSql;

	}
	
	public static int countBySearchRules(Long startTime,
			Long endTime, String errorInfoKey, Long taskId, String goodUrl,
			String publisher, Integer subTaskType, int taskStatus) {

		List<Object> paramList = new ArrayList<Object>();

		String whereSql = genWhereSqlByRules(startTime, endTime, errorInfoKey, taskId, goodUrl, publisher, taskStatus, subTaskType, paramList);

		String query = " select count(*) from " + TABLE_NAME + " where " + whereSql;

		Object[] paramArray = paramList.toArray();

		return (int) dp.singleLongQuery(query, paramArray);
	}

    public static Boolean rebootById(Long id) {
        long createTs = System.currentTimeMillis();
        String sql = "update " + TABLE_NAME + " set finishTs = 0, pullTs = 0, status = " + SubCarrierTaskStatus.waiting + ", errorMsg = null, createTs = ? " +
                "where id = ?";

        return dp.update(sql, createTs, id) == 1;
    }
    
    public static Boolean cancelSubTaskByTaskId(Long taskId) {
        long finishTs = System.currentTimeMillis();
        String msg = "任务被取消";
        String sql = "update " + TABLE_NAME + " set finishTs = ?, status = " + SubCarrierTaskStatus.cancel + ", errorMsg = ? " +
                "where taskId = ? and status = " + SubCarrierTaskStatus.waiting;

        return dp.update(sql, finishTs, msg, taskId) == 1;
    }

    public static long clearSubTaskByTaskId(long taskId) {
        String sql = "delete from " + TABLE_NAME + " where taskId = ?";

        return dp.update(sql, taskId);
    }

    private static SubCarrierTask findByJDBC(String query, Object...params) {

        return new JDBCBuilder.JDBCExecutor<SubCarrierTask>(dp, query, params) {

            @Override
            public SubCarrierTask doWithResultSet(ResultSet rs) throws SQLException {

                if (rs.next()) {
                    return parseSubCarrierTask(rs);
                }

                return null;
            }
        }.call();
    }

    private static List<SubCarrierTask> findListByJDBC(String query, Object...params) {

        return new JDBCBuilder.JDBCExecutor<List<SubCarrierTask>>(dp, query, params) {

            @Override
            public List<SubCarrierTask> doWithResultSet(ResultSet rs) throws SQLException {

                List<SubCarrierTask> subCarrierTasks = new ArrayList<SubCarrierTask>();

                while (rs.next()) {
                    SubCarrierTask carrierTask = parseSubCarrierTask(rs);
                    if (carrierTask != null) {
                        subCarrierTasks.add(carrierTask);
                    }
                }

                return subCarrierTasks;
            }
        }.call();
    }

    private static SubCarrierTask parseSubCarrierTask(ResultSet rs) {
        try {
            SubCarrierTask carrierTask = new SubCarrierTask();
            carrierTask.setId(rs.getLong(1));
            carrierTask.setTaskId(rs.getLong(2));
            carrierTask.setUrl(rs.getString(3));
            carrierTask.setPublisher(rs.getString(4));
            carrierTask.setPn(rs.getInt(5));
            carrierTask.setFinishTs(rs.getLong(6));
            carrierTask.setCreateTs(rs.getLong(7));
            carrierTask.setPullTs(rs.getLong(8));
            carrierTask.setErrorMsg(rs.getString(9));
            carrierTask.setStatus(rs.getInt(10));
            carrierTask.setBabyTitle(rs.getString(11));
            carrierTask.setPicUrl(rs.getString(12));
            carrierTask.setCid(rs.getLong("cid"));
            carrierTask.setBrand(rs.getLong("brand"));
            Object subTaskType = rs.getObject("subTaskType");
            if (subTaskType == null) carrierTask.setSubTaskType(null);
            else carrierTask.setSubTaskType(SubTaskType.values()[(Integer) subTaskType]);

            return carrierTask;
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public static void recordError(String numiid, String publisher, String errorMsg, SubTaskType subTaskType) {
        record(false, numiid, null, publisher, errorMsg, subTaskType, StringUtils.EMPTY);
    }

    public static void recordSuccess(String numiid, String title, String publisher, String newNumiid, SubTaskType subTaskType) {
        record(true, numiid, title, publisher, newNumiid, subTaskType, StringUtils.EMPTY);
    }
    
    public static void recordSuccess(String numiid, String title, String publisher, String newNumiid, SubTaskType subTaskType, String picUrl) {
        record(true, numiid, title, publisher, newNumiid, subTaskType, picUrl);
    }

    private static void record(Boolean success, String numiid, String title, String publisher, String errorMsg, SubTaskType subTaskType, String picUrl) {
        SubCarrierTask subCarrierTask = new SubCarrierTask();
        Long currentTime = System.currentTimeMillis();
        subCarrierTask.setCreateTs(currentTime);
        subCarrierTask.setFinishTs(currentTime);
        subCarrierTask.setPullTs(currentTime);
        subCarrierTask.setUrl(numiid);
        subCarrierTask.setBabyTitle(title);
        subCarrierTask.setPublisher(publisher);
        if (success) subCarrierTask.setStatus(SubCarrierTaskStatus.success);
        else subCarrierTask.setStatus(SubCarrierTaskStatus.failure);
        subCarrierTask.setErrorMsg(errorMsg);
        subCarrierTask.setTaskId(0L);
        subCarrierTask.setSubTaskType(subTaskType);
        subCarrierTask.setPicUrl(picUrl);

        subCarrierTask.jdbcSave();
    }

    // 更新主任务id下所有未操作过的子任务
    public static Long finishFailSubTaskByTaskId(long taskId, String errorMsg) {
        String sql = "update " + TABLE_NAME + " set finishTs=?, status=?, errorMsg=? where taskId = ? and status = ? and finishTs = ? and pullTs = ?";

        return dp.update(sql, System.currentTimeMillis(), SubCarrierTaskStatus.failure, errorMsg, taskId, SubCarrierTaskStatus.waiting, 0, 0);
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
        String insertSQL = "insert into `" + TABLE_NAME + "`(`taskId`,`url`,`pn`,`finishTs`,`createTs`," +
                "`publisher`,`errorMsg`,`pullTs`,`status`,`babyTitle`,`picUrl`,`subTaskType`,`cid`,`brand`) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        long id = dp.insert(insertSQL, this.taskId, this.url, this.pn, this.finishTs, this.createTs,
                this.publisher, this.errorMsg, this.pullTs, this.status, this.babyTitle, this.picUrl, this.subTaskType.ordinal(),this.cid,this.brand);

        log.info("[Insert sub_carrier_task Id:]" + id);

        if (id > 0L) {
            setId(id);
            return true;
        } else {
            log.error("Insert Fails.....");
            return false;
        }

    }

    public boolean rawUpdate() {
        String updateSQL = "update `" + TABLE_NAME + "` set `taskId`=?,`url`=?,`pn`=?,`finishTs`=?,`createTs`=?," +
                "`publisher`=?,`errorMsg`=?,`status`=?,`pullTs`=?,`babyTitle`=?,`picUrl`=?,`subTaskType`=?,`cid` = ?,`brand` = ?  where `id`=? ";
        long updateNum = dp.update(updateSQL, this.taskId, this.url, this.pn, this.finishTs, this.createTs, this.publisher,
                this.errorMsg, this.status, this.pullTs, this.babyTitle, this.picUrl, this.subTaskType.ordinal(),this.cid,this.brand, this.id);

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

    public static List<SubTaskInfo> fetchSubTaskInfo() {

        String query = "select publisher, taskId, url, id from sub_carrier_task where status = " 
        			+ SubCarrierTaskStatus.waiting + "  and finishTs = 0 and pullTs = 0 limit 5";

        return new JDBCBuilder.JDBCExecutor<List<SubTaskInfo>>(dp, query) {

            @Override
            public List<SubTaskInfo> doWithResultSet(ResultSet rs) throws SQLException {
                List<SubTaskInfo> subTaskInfos = new ArrayList<SubTaskInfo>();

                while (rs.next()) {
                    SubTaskInfo subTaskInfo = praseSubTaskInfo(rs);
                    if (subTaskInfo != null) {
                        subTaskInfos.add(subTaskInfo);
                    }
                }

                return subTaskInfos;
            }

            private SubTaskInfo praseSubTaskInfo(ResultSet rs) {
                try {
                    SubTaskInfo subTaskInfo = new SubTaskInfo();
                    subTaskInfo.setPublisher(rs.getString(1));
                    subTaskInfo.setTaskId(rs.getLong(2));
                    subTaskInfo.setUrl(rs.getString(3));
                    subTaskInfo.setSubTaskId(rs.getLong(4));
                    return subTaskInfo;
                } catch (Exception e) {
                    log.error("parseSubTaskInfo: " + e.getMessage());
                    return null;
                }
            }
        }.call();
    }

    public long getCid() {
		return cid;
	}

	public void setCid(long cid) {
		this.cid = cid;
	}

	public long getBrand() {
		return brand;
	}

	public void setBrand(long brand) {
		this.brand = brand;
	}

	public static class SubTaskInfo {
        private long subTaskId;
        private long taskId;
        private String publisher;
        private String url;

        public long getTaskId() {
            return taskId;
        }

        public void setTaskId(long taskId) {
            this.taskId = taskId;
        }

        public long getSubTaskId() {
            return subTaskId;
        }

        public void setSubTaskId(long subTaskId) {
            this.subTaskId = subTaskId;
        }

        public String getPublisher() {
            return publisher;
        }

        public void setPublisher(String publisher) {
            this.publisher = publisher;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

	

}
