
package ats;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.persistence.EntityManager;

import job.ScheduledWritterJob;
import models.visit.ATSLocalTask;
import monitor.StatusReporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;

@Every("5s")
public class ATSTaskUpdate extends ScheduledWritterJob<ATSLocalTask> {
    public static final Queue<ATSLocalTask> tasksToWritten = new ConcurrentLinkedQueue<ATSLocalTask>();

    private static final Logger log = LoggerFactory.getLogger(ATSTaskUpdate.class);

    public static final String TAG = "VisitLogLocalTaskUpdate";

    public ATSTaskUpdate() {
        super();
        this.tryMerge = true;
    }

    public static class VisitLogLocalTaskUpdateStatusReport implements StatusReporter {
        public void appendReport(StringBuilder sb) {
            sb.append(String.format("%s current queue size : %s\n", TAG, tasksToWritten.size()));
        }
    }

    public static VisitLogLocalTaskUpdateStatusReport reporter = new VisitLogLocalTaskUpdateStatusReport();

    @Override
    public Queue<ATSLocalTask> getQueue() {
        return tasksToWritten;
    }

    @Override
    protected ATSLocalTask doMerge(EntityManager manager, ATSLocalTask t) {
        return manager.merge(t);
    }

    public void doUpdate(ATSLocalTask t) {
        ATSLocalTask exist = ATSLocalTask.findByTask(t.getTaskId());


        if (exist != null) {
            exist.setUserId(t.getUserId());
            exist.setDownloadUrl(t.getDownloadUrl());
            exist.setStatus(t.getStatus());
            exist.setTs(t.getTs());
            exist.setTaskType(t.getTaskType());
            exist.setCheckCode(t.getCheckCode());
            exist.setEnd(t.getEnd());

            exist.save();
            log.info("[save exist:]" + exist);
        } else {

            log.info("[save t:]" + t);
            t._save();
        }
    }

    public static void addObject(ATSLocalTask task) {
        tasksToWritten.add(task);
    }
}
