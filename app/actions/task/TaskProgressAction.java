
package actions.task;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;

public class TaskProgressAction {
    private final static Logger log = LoggerFactory.getLogger(TaskProgressAction.class);

    private static String genKey(Long taskId, String taskTag) {
        return taskTag + taskId;
    }

    private static void createProgress(Long taskId, String taskTag, int totalNum) {

        if (taskId == null || taskId <= 0L) {
            return;
        }

        TaskProgress progress = new TaskProgress(taskId, taskTag);

        progress.setTotalNum(totalNum);

        String key = genKey(taskId, taskTag);

        TaskProgressCache.putToCache(key, progress);
    }

    private static void stepOneProgress(Long taskId, String taskTag) {
        try {
            if (taskId == null || taskId <= 0L) {
                return;
            }

            String key = genKey(taskId, taskTag);
            TaskProgress progress = TaskProgressCache.getFromCache(key);

            if (progress == null) {
                log.warn("cannot find TaskProgress for key: " + key + "-----------------");
                return;
            }

            int finishedNum = progress.getFinishedNum();
            finishedNum++;

            log.warn("for TaskProgress: " + key + ", finishedNum: " + finishedNum
                    + ", totalNum: " + progress.getTotalNum() + "-----------------");

            progress.setFinishedNum(finishedNum);

            TaskProgressCache.putToCache(key, progress);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

    }

    private static void closeProgress(Long taskId, String taskTag) {

        try {
            if (taskId == null || taskId <= 0L) {
                return;
            }

            String key = genKey(taskId, taskTag);

            TaskProgress progress = TaskProgressCache.getFromCache(key);

            if (progress == null) {
                log.warn("cannot find TaskProgress for key: " + key + "-----------------");
                return;
            }

            progress.setFinishedNum(progress.getTotalNum());

            TaskProgressCache.putToCache(key, progress);

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    public static class AutoTitleProgressAction {

        private static final String TASK_TAG = "Task_AutoTitleTask_";

        public static void createTaskProgress(Long taskId, int totalNum) {

            if (taskId == null || taskId <= 0L) {
                return;
            }

            createProgress(taskId, TASK_TAG, totalNum);
        }

        public static void stepOneTaskProgress(Long taskId) {
            try {
                if (taskId == null || taskId <= 0L) {
                    return;
                }

                stepOneProgress(taskId, TASK_TAG);

            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }

        }

        public static void closeTaskProgress(Long taskId) {

            if (taskId == null || taskId <= 0L) {
                return;
            }

            closeProgress(taskId, TASK_TAG);
        }

        public static TaskProgress getTaskProgress(Long taskId) {
            if (taskId == null || taskId <= 0L) {
                return null;
            }

            String key = genKey(taskId, TASK_TAG);

            return TaskProgressCache.getFromCache(key);
        }

    }

    public static class TaskProgressCache {

        public static TaskProgress getFromCache(String key) {

            TaskProgress progress = (TaskProgress) Cache.get(key);

            return progress;
        }

        public static void putToCache(String key, TaskProgress progress) {

            Cache.set(key, progress, "5h");
        }
    }

    public static class TaskProgress implements Serializable {

        private static final long serialVersionUID = 1L;

        private long taskId;

        private String taskTag;//区分任务类型

        private int totalNum;

        private int finishedNum;

        public long getTaskId() {
            return taskId;
        }

        public void setTaskId(long taskId) {
            this.taskId = taskId;
        }

        public String getTaskTag() {
            return taskTag;
        }

        public void setTaskTag(String taskTag) {
            this.taskTag = taskTag;
        }

        public int getTotalNum() {
            return totalNum;
        }

        public void setTotalNum(int totalNum) {
            this.totalNum = totalNum;
        }

        public int getFinishedNum() {
            return finishedNum;
        }

        public void setFinishedNum(int finishedNum) {
            this.finishedNum = finishedNum;
        }

        public TaskProgress(long taskId, String taskTag) {
            super();
            this.taskId = taskId;
            this.taskTag = taskTag;
        }

    }
}
