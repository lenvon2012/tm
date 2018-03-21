
package controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import models.task.AutoTitleTask;
import models.task.AutoTitleTask.UserTaskStatus;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.NoTransaction;
import result.TMResult;
import actions.task.TaskProgressAction.AutoTitleProgressAction;
import actions.task.TaskProgressAction.TaskProgress;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.NumberUtil;

public class TitleTaskOp extends TMController {

    private static final Logger log = LoggerFactory.getLogger(TMController.class);

    public static void index() {
        render("autoTitle/titletask.html");
    }

    public static void fenxiao() {
        render("fenxiao/titletask.html");
    }
    
    @NoTransaction
    public static void findTasks() {

    	int pn = NumberUtil.parserInt(params.get("pn"), 0);
        int ps = NumberUtil.parserInt(params.get("ps"), 10);

        PageOffset po = new PageOffset(pn, ps);

        User user = getUser();

        List<AutoTitleTask> taskList =
                AutoTitleTask.queryUserTask(user.getId(), po.getOffset(), po.getPs());

        long count = AutoTitleTask.countUserTask(user.getId());
        long unFinishedCount = AutoTitleTask.countUserUnFinishedTask(user.getId());
        
        for (AutoTitleTask task : taskList) {
            if (task.getStatus() == UserTaskStatus.Doing) {

                TaskProgress progress = AutoTitleProgressAction.getTaskProgress(task.getTaskId());

                if (progress == null) {
                    task.setProgress(0);
                    continue;
                } else {
                    int totalNum = progress.getTotalNum();
                    if (totalNum <= 0) {
                        task.setProgress(0);
                        continue;
                    } else {
                        int finishedNum = progress.getFinishedNum();

                        if (finishedNum >= totalNum) {
                            task.setProgress(100);
                        } else {
                            task.setProgress(finishedNum * 100 / totalNum);
                        }
                    }


                }

            }

        }
        
        TMResult res = new TMResult(taskList, (int) count, po, String.valueOf(unFinishedCount));
        renderJSON(JsonUtil.getJson(res));

    }

    @NoTransaction
    public static void findUnFinishedTasks() {

        User user = getUser();

        List<AutoTitleTask> taskList = AutoTitleTask.queryUserUnFinishedTask(user.getId());

        if (CommonUtils.isEmpty(taskList)) {
            taskList = new ArrayList<AutoTitleTask>();
        }

        for (AutoTitleTask task : taskList) {
            if (task.getStatus() == UserTaskStatus.Doing) {

                TaskProgress progress = AutoTitleProgressAction.getTaskProgress(task.getTaskId());

                if (progress == null) {
                    task.setProgress(0);
                    continue;
                } else {
                    int totalNum = progress.getTotalNum();
                    if (totalNum <= 0) {
                        task.setProgress(0);
                        continue;
                    } else {
                        int finishedNum = progress.getFinishedNum();

                        if (finishedNum >= totalNum) {
                            task.setProgress(100);
                        } else {
                            task.setProgress(finishedNum * 100 / totalNum);
                        }
                    }

                }

            }

        }

        renderJSON(JsonUtil.getJson(taskList));

    }

    @NoTransaction
    public static void findFinishedTasks() throws IOException {
        
//        renderMockFileInJsonIfDev("finishedtasks.json");

        int pn = NumberUtil.parserInt(params.get("pn"), 0);
        int ps = NumberUtil.parserInt(params.get("ps"), 10);

        PageOffset po = new PageOffset(pn, ps);

        User user = getUser();

        List<AutoTitleTask> taskList =
                AutoTitleTask.queryUserFinishedTask(user.getId(), po.getOffset(), po.getPs());

        long count = AutoTitleTask.countUserFinishedTask(user.getId());

        TMResult res = new TMResult(taskList, (int) count, po);
        renderJSON(JsonUtil.getJson(res));

    }

    @NoTransaction
    public static void countUnFinishedTask() {

        User user = getUser();

        long count = AutoTitleTask.countUserUnFinishedTask(user.getId());

        renderText(count);
    }

    public static PageJsCaller jsCaller = new PageJsCaller();

    public static class PageJsCaller implements Callable<Boolean> {

        public long countUnfinished(User user) {
            return AutoTitleTask.countUserUnFinishedTask(user.getId());
        }

        @Override
        public Boolean call() {
            return null;
        }
    };

}
