
package ats;

import java.util.List;

import models.visit.ATSLocalTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import ats.TaskManager.Status;

import com.ciaosir.client.CommonUtils;
import com.google.gson.Gson;
import com.taobao.api.domain.Task;

import configs.TMConfigs;

/**
 * We need to find those jobs that have been deprecated for too long a time....
 *
 * @author zhourunbo
 *
 */
@Every("60s")
@OnApplicationStart(async = true)
public class ATSCheckerJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(ATSCheckerJob.class);

    private static final String TAG = "VisitLogCheckerJob";

    @Override
    public void doJob() {
        Thread.currentThread().setName(TAG);

        if (!TMConfigs.App.ENABLE_TMHttpServlet && (Play.mode.isDev() && !"zrb".equals(Play.id))) {
            return;
        }
        log.error("[ATS Job Checker Job]");

        List<ATSLocalTask> tasks = null;
        while (ATSTaskUpdate.tasksToWritten.size() > 512) {
            CommonUtils.sleepQuietly(1000L);
        }

        tasks = ATSLocalTask.findAllDoneByTaobao();
        log.info("[Done By Taobao Size:]" + tasks.size());
        for (ATSLocalTask task : tasks) {
            task.setStatus(Status.NEW);
        }

        tasks.addAll(ATSLocalTask.findAllNotReady());

        log.error("[Not Ready Tasks Number:]" + tasks.size());
        for (final ATSLocalTask task : tasks) {

            final Task taobaoTask = ATSResultGetAPI.getTaskResult(task.getTaskId());
            log.info("[find taobao task:]" + new Gson().toJson(taobaoTask));
            if (taobaoTask == null) {
                continue;
            }

            task.update(taobaoTask);
            if (task.isOver()) {
                ATSTaskUpdate.addObject(task);
            } else if (task.isDoneByTaobao()) {
                ATSTaskReceiver.onReceiveMessage(task);
            }

        }
    }

}
