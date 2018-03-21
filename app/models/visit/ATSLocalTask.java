
package models.visit;

import java.io.File;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;

import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.data.validation.Unique;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import transaction.DBBuilder;
import transaction.JDBCBuilder;
import transaction.JPATransactionManager;
import transaction.TransactionSecurity;
import ats.ATSTaskUpdate;
import ats.TaskManager;
import ats.TaskManager.Status;

import com.ciaosir.client.utils.DateUtil;
import com.ciaosir.client.utils.NumberUtil;
import com.google.gson.Gson;
import com.taobao.api.domain.Task;

import configs.TMConfigs.ATS;

@Entity(name = "async_log_task")
public class ATSLocalTask extends Model {
    public static final String TABLE_NAME = "async_log_task";

    private static final Logger log = LoggerFactory.getLogger(ATSLocalTask.class);

    private static final String TAG = "ATSLocalTask";

    @Index(name = "userId")
    public Long userId;

//    @Index(name = "ts")
    public Long ts;

    /**
     * Reversed....
     */
    public Long end;

    @Index(name = "tid")
    @Column(unique = true)
    @Unique
    public long taskId;

//    @Index(name = "st")
    @Column(length = 16, nullable = false)
    public String status;

    @Column(name = "url")
    public String downloadUrl;

    @Column(length = 64, name = "md5")
    public String checkCode;

    public enum CompressType {
        ZIP, GZIP, NONE
    }

    public enum TaskType {
        ATSTradeSold, ATSTradeHistory, VisitLog,
        /*
         * 只有oid num numIid payTime这几个字段，其他全部都不要
         */
        ATSSimpleTrade
    }

    @Enumerated(EnumType.STRING)
    public TaskType type;

    @Column(columnDefinition = "int default -1")
    public int mode;

    public boolean isDownloaded;

    public long created;

    @Transient
    public File file;

    public ATSLocalTask(Task task, Long userId, Long ts, TaskType type) {
        this.userId = userId;
        this.ts = ts;
        this.taskId = task.getTaskId();
        this.downloadUrl = task.getDownloadUrl();
        this.status = task.getStatus();
        this.type = type;
        this.checkCode = task.getCheckCode();
        this.created = task.getCreated().getTime();
        this.setMode();
    }

    public ATSLocalTask(Task task, Long userId, Long start, Long end, TaskType type) {
        this.userId = userId;
        this.ts = start;
        this.end = end;
        this.taskId = task.getTaskId();
        this.downloadUrl = task.getDownloadUrl();
        this.status = task.getStatus();
        this.type = type;
        this.checkCode = task.getCheckCode();
        this.created = task.getCreated().getTime();
        this.setMode();
    }

    public void setMode() {
        this.mode = Integer.valueOf(DBBuilder.genVisitLogHashKey(userId, ts));
        if (this.getStatus() == null) {
            this.setStatus(TaskManager.Status.NEW);
        }
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getCheckCode() {
        return checkCode;
    }

    public void setCheckCode(String checkCode) {
        this.checkCode = checkCode;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public static String genModKey(long uid) {
        return String.valueOf(uid % 1024);
    }

    public static String genFileName(long userId, long ts) {
        return String.valueOf(ts);
    }

    public File getFile() {
        if (!isDownloaded) {
            return null;
        }

        if (file == null) {
            switch (type) {
                case ATSTradeSold:
                case ATSSimpleTrade:
                    File dir = new File(ATS.TRADE_SOLD_UNZIP_DIR, genModKey(userId));
                    dir.mkdirs();
                    dir = new File(dir, String.valueOf(userId));
                    dir.mkdirs();
                    return new File(dir, genFileName(userId, ts));
                default:
                    break;
            }
//            file = PolicyUtil.getUserUnzipDir(String.valueOf(userId), ts);
        }
//        log.info("found file:" + file);
        return file;
    }

    public void setFile(File file) {
        this.isDownloaded = true;
    }

    public boolean isNotReady() {
        return StringUtils.isEmpty(status) || Status.NEW.equals(status) || Status.DOING.equals(status);
    }

    public ATSLocalTask update(Task taobaoTask) {
        this.status = taobaoTask.getStatus();
        this.checkCode = taobaoTask.getCheckCode();
        this.downloadUrl = taobaoTask.getDownloadUrl();
        return this;
    }

    public static ATSLocalTask findByUserIdAndTs(Long userId, long ts, String type) {
        return ATSLocalTask.find("userId = ? and ts = ? and type = ?", userId, ts, type).first();
    }

    @Override
    public String toString() {
        return "VisitLogTaobaoTask [id = " + id + ", userId=" + userId + ", ts=" + ts + ", end=" + end + ", taskId="
                + taskId + ", status=" + status + ", downloadUrl=" + downloadUrl + ", checkCode=" + checkCode
                + ", type=" + type + ", file=" + file + "]";
    }

    /**
     * This would ignore the situation when the task object is detached.....
     * findAllDoneByTaobao
     * @param task
     * @param status
     */
    public static final void updateStatusInDelay(final ATSLocalTask task, final String status) {
        task.status = status;
        ATSTaskUpdate.addObject(task);
    }

    public static final ATSLocalTask findByTask(final long taskId) {
        return new TransactionSecurity<ATSLocalTask>() {
            @Override
            public ATSLocalTask operateOnDB() {
                return ATSLocalTask.find("taskId = ?", taskId).first();
            }
        }.execute();
    }

    public static final List<ATSLocalTask> findAllDownloaded(TaskType type) {
        return ATSLocalTask.find("status = ? and type = ? ", Status.DOWNLOADED, type).fetch();
    }

    public static final List<ATSLocalTask> findAllDownloaded(TaskType type, int limit) {
        return ATSLocalTask.find("status = ? and type = ? ", Status.DOWNLOADED, type).fetch(limit);
    }

    public static final List<ATSLocalTask> findAllDownloaded(TaskType type, int hash, int total) {
        return ATSLocalTask.find("status = ? and type = ? and mod(mode,?) = ? ", Status.DOWNLOADED, type, total,
                hash).fetch();
    }

    public static final List<ATSLocalTask> findAllDoneByTaobao() {
        return ATSLocalTask.find("status = ? ", Status.DONE).fetch();
    }

    public static final List<ATSLocalTask> findAllDoneByTaobao(TaskType type) {
        return ATSLocalTask.find("status = ? and type = ? ", Status.DONE, type).fetch();
    }

    public static final List<ATSLocalTask> findAllTradeSoldDone() {
        return findAllDoneByTaobao(TaskType.ATSTradeSold);
    }

    public static final ATSLocalTask findTradeRecentSold(Long userId) {
        return ATSLocalTask.find("userId = ? and type = ? and ts < ? order by id desc ", userId, TaskType.ATSTradeSold,
                DateUtil.formCurrDate() - DateUtil.THIRTY_DAYS).first();
    }

    public static final List<ATSLocalTask> findAllNotReady() {
        return ATSLocalTask.find("(status = ? or status = ?) ", Status.NEW, Status.DOING).fetch();
    }

    public static final List<ATSLocalTask> findAllNotReady(TaskType type) {
        return ATSLocalTask.find("(status = ? or status = ?) and type = ?", Status.NEW, Status.DOING, type)
                .fetch();
    }

    public static final List<ATSLocalTask> findAllNotDoneByTaobao() {
        return ATSLocalTask.find("status = ? ", Status.DONE).fetch();
    }

    public static final boolean isTaskDoneByTaobao(ATSLocalTask task) {
        if (task == null) {
            return false;
        }
        return Status.DONE.equals(task.getStatus());
    }

    public static void writeFilePathInDelay(final ATSLocalTask srcTask, final File dataFile) {
        srcTask.setFile(dataFile);
//        VisitLogLocalTaskUpdate.addObject(srcTask);
    }

    public boolean isDownToLocal() {
        if (Status.DOWNLOADING.equals(this.status)) {
            return true;
        }
        if (Status.DOWNLOADED.equals(this.status)) {
            return true;
        }

        return false;
    }

    public static final int clearDownloading() {

//        VisitLogLocalTask.delete(" ts < " + (DateUtil.formCurrDate() - DateUtil.THIRTY_DAYS));

        List<ATSLocalTask> tasks = ATSLocalTask.find("status =  ? ", Status.DOWNLOADING).fetch();
        log.warn("[Delete Tasks:" + tasks);

        for (ATSLocalTask task : tasks) {
            task.delete();
        }

        return tasks.size();
    }

    public boolean isDoneByTaobao() {
        return Status.DONE.equals(this.status);
    }

    public boolean isOver() {
        return Status.OVER.equals(this.status);
    }

    @Override
    public void _save() {
//        log.warn("Save Async Taobao Task in Thread:" + Thread.currentThread().getName());
        super._save();
    }

    public boolean hasEarlierNotOverTask(String type) {
        return ATSLocalTask.find(" userId = ? and ts < ?  and type = ? and status <> ? ", userId, ts, type,
                Status.OVER).first() != null;
    }

    public static List<ATSLocalTask> findByUser(User user) {
        return ATSLocalTask.find(" userId = ? and type = ? ", user.getId(), TaskType.ATSTradeSold).fetch();
    }

    public static void fix() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                new TransactionSecurity<Void>() {

                    @Override
                    public Void operateOnDB() {
                        doFix();
                        return null;
                    }
                }.execute();

            }

            private void doFix() {

                while (ATSLocalTask.find("mode = -1").first() != null) {
                    List<ATSLocalTask> tasks = ATSLocalTask.find("mode = -1").fetch(32);
                    for (ATSLocalTask task : tasks) {
                        task.setMode();
                        task.save();
                    }
                    JPATransactionManager.clearEntities();
                }
            }
        }).start();
    }

    static String QueryForMinTs = " select min(ts) from " + ATSLocalTask.TABLE_NAME + " where userId = ? ";

    // never used
    public static long findMinTs(Long userId) {
        return JDBCBuilder.singleLongQuery(QueryForMinTs, userId);
    }

    public CompressType getZipType() {

        switch (this.type) {
            case ATSTradeSold:
            case ATSSimpleTrade:
                return CompressType.ZIP;
            case ATSTradeHistory:
                return CompressType.NONE;
            default:
                return CompressType.GZIP;
        }

    }

    public static ATSLocalTask findOrNew(final Task taobaoTask, final Long userId, final long start,
            final long end, final TaskType tag) {
        return new TransactionSecurity<ATSLocalTask>() {

            @Override
            public ATSLocalTask operateOnDB() {

                if (taobaoTask == null) {
                    return null;
                }

                ATSLocalTask localTask = ATSLocalTask.findByTask(taobaoTask.getTaskId());
                log.info("load local task:" + localTask + "[taobao task:]" + new Gson().toJson(taobaoTask));

                if (localTask == null) {
                    TaskManager.infoTaobaoTask(taobaoTask);
                    localTask = new ATSLocalTask(taobaoTask, userId, start, end, tag);
                } else {
                    localTask.update(taobaoTask);
                }

                return localTask;
            }

        }.execute();
    }

    public static ATSLocalTask findMaxEndTask(Long userId, String type) {
        return (ATSLocalTask) NumberUtil.first(JPA
                .em()
                .createNativeQuery("select * from " + TABLE_NAME + "  where userId = ? and type  = ? having max(end)",
                        ATSLocalTask.class).setParameter(1, userId).setParameter(2, type).getResultList());

//        return VisitLogLocalTask.find("userId = ? and type = ? having max(end)", userId, type).first();
    }

    public static ATSLocalTask findOrNew(final Task taobaoTask, final Long userId, final long ts,
            final TaskType type) {
        return findOrNew(taobaoTask, userId, ts, 0L, type);
    }

    public TaskType getTaskType() {
        return type;
    }

    public void setTaskType(TaskType taskType) {
        this.type = taskType;
    }

}
