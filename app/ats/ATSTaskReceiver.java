
package ats;

import models.visit.ATSLocalTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * @TODO This class is for the old time...rewrite it later....
 * @author zhourunbo
 * 
 */
public class ATSTaskReceiver {

    private static final Logger log = LoggerFactory.getLogger(ATSTaskReceiver.class);

    private static final String TAG = "VisitLogTaskReceiver";

    public static class PYTask {

        public String timestamp = null;

        public Long task_id = Long.valueOf(-1L);

        public String status = null;

        @Override
        public String toString() {
            return "Task [timestamp=" + timestamp + ", task_id=" + task_id
                    + ", status=" + status + "]";
        }

        public static PYTask getFromAsyncMessage(String msg) {
            Gson gson = new Gson();
            JsonObject jsonTask = gson.toJsonTree(msg).getAsJsonObject().get("task").getAsJsonObject();
            PYTask task = gson.fromJson(jsonTask, PYTask.class);
            return task;
        }
    }

    public static final void onReceiveMessage(String msg) {

        Gson gson = new Gson();
        JsonObject jsonTask = gson.toJsonTree(msg).getAsJsonObject().get("task").getAsJsonObject();
        PYTask message = gson.fromJson(jsonTask, PYTask.class);
        onReceiveMessage(message);
    }

    public static final void onReceiveMessage(PYTask message) {
        onReceiveMessage(message.task_id);
    }

    public static final void onReceiveMessage(Long taskId) {
        // VisitLogTaskReceiver.onReceiveMessage(message);

        if (taskId == null) {
            log.warn("null py task received..." + taskId);
            return;
        }

//        log.info("Receive Message Task:" + message);

//        Task taobaoTask = ATSResultGetAPI.getTaskResult(taskId);

        ATSLocalTask task = ATSLocalTask.findByTask(taskId);
        if (task == null) {
//            log.error("Null Task Received... with message: " + taobaoTask);
            return;
        }

        onReceiveMessage(task);
    }

    public static final void onReceiveMessage(ATSLocalTask task) {
        if (task == null) {
            log.warn("Null Task Sent....");
            return;
        }

        if (!ATSLocalTask.isTaskDoneByTaobao(task)) {
            log.warn("Not done task....:" + task);
            return;
        }

//        if (!TMConfigs.App.ENABLE_TMHttpServlet) {
//            return;
//        }

        Boolean downResult = new ATSDownloader(task).call();
        log.info("[Download Result:]" + downResult + "\twith task:" + task);
    }

    public static final void onNotExists(final long taskId) {
        ATSLocalTask task = ATSLocalTask.findByTask(taskId);
        if (task == null) {
            log.error("No Found for Task :" + task);
        }
        task.setUserId(task.getUserId());
        task.setStatus(TaskManager.Status.FAIL);
        ATSTaskUpdate.addObject(task);
    }
}
