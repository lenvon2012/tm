package job.carriertask;

import actions.carriertask.CarrierTaskAction;
import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import configs.TMConfigs;
import configs.TMConfigs.Server;
import models.carrierTask.SubCarrierTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.jobs.Every;
import play.jobs.Job;
import result.TMResult;
import titleDiag.DiagResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Created by Administrator on 2016/3/8.
 */
@Every("1h")
public class CarrierTaskJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(CarrierTaskJob.class);

    private static final long TIME_SPAN = 60 * 60 * 1000;

    //这个job用来清理客户端取到任务没有成功上传的任务
    @Override
    public void doJob() {
    	 if (!Server.jobTimerEnable || Play.mode.isDev()) {
             return;
         }
        int pn = 1;
        int ps = 128;
        while (true) {
            long ts = System.currentTimeMillis() - TIME_SPAN;
            PageOffset po = new PageOffset(pn, ps, 10);
            List<SubCarrierTask> tasks = SubCarrierTask.findClientFailedTask(ts, po);
            if (CommonUtils.isEmpty(tasks) == true) {
                return;
            }
            for (SubCarrierTask task : tasks) {
                task.resetPullTs();
            }
            pn++;
        }
    }
}
