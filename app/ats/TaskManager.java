
package ats;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import models.visit.ATSLocalTask;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.api.domain.Task;

public class TaskManager {

    private static final Logger log = LoggerFactory.getLogger(TaskManager.Status.class);

    private static final String TAG = "TaskManager.Status";

    public static class Status {

        public static final int FLAG_NEW = 1;

        public static final int FLAG_RUNNING = 2;

        public static final int FLAG_DONE = 3;

        public static final String NEW = "new";

        public static final String DOING = "doing";

        public static final String DONE = "done";

        public static final String DOWNLOADING = "downing";

        public static final String DOWNLOADED = "downloaded";

        public static final String RUNNING = "running";

        public static final String OVER = "over";

        public static final String FAIL = "fail";
    }

    public static final boolean isTaobaoTaskDone(
            com.taobao.api.domain.Task taobaoTask) {
        if (taobaoTask == null) {
            return false;
        }
        if ("done".equals(taobaoTask.getStatus())) {
            return true;
        }
        return false;
    }

    public static final boolean isOver(int status) {
        if (status == Status.FLAG_DONE) {
            return true;
        }
        return false;
    }

    public static final boolean isMD5Equals(ATSLocalTask task, File target) {
        if (target == null || !target.isFile()) {
            return false;
        }

        try {
            String md5 = DigestUtils.md5Hex(new FileInputStream(target));
            if (StringUtils.isEmpty(md5)) {
                return false;
            }

//			log.info("Exists md5:[" + md5 + "] and checkcode:["
//					+ task.getCheckCode() + "]");

            return md5.equals(task.getCheckCode());

        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }

        return false;
    }

    public static final void infoTaobaoTask(Task task) {
        if (task == null) {
//			log.info("Task: null");
        } else {
//			log.info("Task: " + task.getTaskId() + " checkcode:"
//					+ task.getCheckCode() + ", url:" + task.getDownloadUrl()
//					+ ",status:[" + task.getStatus() + "], created:"
//					+ task.getCreated() + "]");
        }
    }

}
