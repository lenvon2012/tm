package job.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.FutureTask;

import models.group.GroupQueue;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actions.GroupAction;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.PYFutureTaskPool;
import com.ciaosir.client.utils.NumberUtil;

import play.db.jpa.NoTransaction;
import play.jobs.Every;
import play.jobs.Job;

@Every("5s")
@NoTransaction
public class UpdateTmplJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(UpdateTmplJob.class);

    private static PYFutureTaskPool<Void> pool = new PYFutureTaskPool<Void>(3);

    private static Queue<GroupQueue> planQueue = new ConcurrentLinkedQueue<GroupQueue>();

    public static void addPlanId(GroupQueue gq ) {
        if (gq.getNumIids() == null || NumberUtil.isNullOrZero(gq.getPlanId()) || gq.getUser() == null) {
            return;
        }
        if (planQueue.contains(gq)) {
            return;
        }
        planQueue.add(gq);
    }

    @Override
    public void doJob() throws Exception {
        if (CommonUtils.isEmpty(planQueue)) {
            return;
        }

        long startTime = System.currentTimeMillis();

        List<FutureTask<Void>> promises = new ArrayList<FutureTask<Void>>();

        int planCount = 0;
        GroupQueue gq = null;
        while ((gq = planQueue.poll()) != null) {
            planCount++;
            planDelistUpdateSubmit call = new planDelistUpdateSubmit(gq);
            promises.add(pool.submit(call));
        }

        for (FutureTask<Void> promise : promises) {
            try {
                promise.get();

            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);

            }
        }

        long endTime = System.currentTimeMillis();

        long usedTime = endTime - startTime;

        log.error("end do UpdateTmplJob started with queue size = " + planCount + ", used " + usedTime
                + " ms------------------");

    }

    public static class planDelistUpdateSubmit implements Callable<Void> {

        private GroupQueue gq;

        public planDelistUpdateSubmit(GroupQueue gq) {
            super();
            this.gq = gq;
        }
        

        @Override
        public Void call() throws Exception {
            if (gq == null) {
                return null;
            }
            if(gq.getType() == GroupAction.ONE){
                GroupAction.insertOnePlan(gq);
            }
            if(gq.getType() == GroupAction.TWO){
                GroupAction.deleteOnePlan(gq);
            }
            return null;
        }
    }
}
