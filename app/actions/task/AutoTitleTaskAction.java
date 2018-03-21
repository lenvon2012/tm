
package actions.task;

import models.task.AutoTitleTask;
import models.task.AutoTitleTask.UserTaskStatus;
import models.task.AutoTitleTask.UserTaskType;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoTitleTaskAction {

    private final static Logger log = LoggerFactory.getLogger(AutoTitleTaskAction.class);

//    public static UserTaskLog addWirelessTask(User user, String configJson) {
//        
//    }

    public static UserTaskLog addAutoTitleTask(User user, String configJson) {
        try {

            boolean isHasSameTask = AutoTitleTask.checkHasTheSameTask(user.getId(), UserTaskType.BuildAutoTitle);

            if (isHasSameTask == true) {
                return new UserTaskLog(false, "当前已存在生成自动标题的任务，您暂时无法提交，请稍后再试！");
            }

            AutoTitleTask task = new AutoTitleTask(user.getId(), configJson,
                    UserTaskStatus.New, UserTaskType.BuildAutoTitle);

            boolean isSuccess = task.jdbcSave();
            log.info("[saved task :]" + task);

            if (isSuccess == false) {
                return new UserTaskLog(false, "提交任务失败，请联系我们！");
            } else {
                return new UserTaskLog(true);
            }

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new UserTaskLog(false, "系统提交任务时失败，请联系我们！");
        }

    }

    public static class UserTaskLog {
        private boolean isSuccess;

        private String message;

        public boolean isSuccess() {
            return isSuccess;
        }

        public void setSuccess(boolean isSuccess) {
            this.isSuccess = isSuccess;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public UserTaskLog(boolean isSuccess, String message) {
            super();
            this.isSuccess = isSuccess;
            this.message = message;
        }

        public UserTaskLog(boolean isSuccess) {
            super();
            this.isSuccess = isSuccess;
        }

    }

}
