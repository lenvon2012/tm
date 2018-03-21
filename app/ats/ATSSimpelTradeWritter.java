
package ats;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import job.writter.TradeWritter;
import models.user.User;
import models.visit.ATSLocalTask;
import models.visit.ATSLocalTask.TaskType;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import transaction.JPATransactionManager;
import transaction.TransactionSecurity;
import ats.TaskManager.Status;

import com.ciaosir.client.CommonUtils;
import com.taobao.api.ApiException;
import com.taobao.api.domain.Trade;
import com.taobao.api.internal.util.TaobaoUtils;
import com.taobao.api.response.TradeFullinfoGetResponse;

import configs.TMConfigs;
import dao.UserDao;

@Every("30s")
@OnApplicationStart(async = true)
public class ATSSimpelTradeWritter extends Job {

    private static final Logger log = LoggerFactory.getLogger(ATSSimpelTradeWritter.class);

    public static final String TAG = "ATSSimpelTradeWritter";

    private static TaskType taskType = TaskType.ATSSimpleTrade;

    public final Queue<ATSLocalTask> writeQueue = new ConcurrentLinkedQueue<ATSLocalTask>();

    public void doJob() {
        Thread.currentThread().setName(TAG);
        doWithTasks();
    }

    public void doWithTasks() {
        if (!TMConfigs.App.ENABLE_TMHttpServlet && !Play.mode.isDev()) {
            return;
        }

        log.error("do for the ats trade sold writter");

//        do {
        final List<ATSLocalTask> tasks = ATSLocalTask.findAllDownloaded(taskType, 128);
        if (CommonUtils.isEmpty(tasks)) {
            return;
        }

        JPATransactionManager.clearEntities();
        for (ATSLocalTask task : tasks) {
            final Long taskId = task.getTaskId();
            new TransactionSecurity<Boolean>() {
                @Override
                public Boolean operateOnDB() {
                    doForTask(taskId);
                    return null;
                }

            }.execute();
            JPATransactionManager.clearEntities();
        }

        ATSLocalTask peerTask = null;
        while ((peerTask = writeQueue.poll()) != null) {
            final long tId = peerTask.getId();

        }
    }

//    private Boolean doForTask(ATSLocalTask task) {
    private Boolean doForTask(Long taskId) {
        Boolean success = Boolean.FALSE;
        ATSLocalTask task = ATSLocalTask.findByTask(taskId);

        User user = UserDao.findById(task.getUserId());
        if (user == null) {
            ATSLocalTask.updateStatusInDelay(task, Status.FAIL);
            return Boolean.FALSE;
        }

        try {
            success = writeTrades(task.getFile(), user, task.getTs());
            return Boolean.TRUE;
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return Boolean.FALSE;

        } finally {
            // log.info("Resunt[" + success + "] for task:" + task);
            if (success) {
//                DailyLogJobExecutor.addObject(new UserTimestampMsg(task.getUserId(), task.getTs()));
                ATSLocalTask.updateStatusInDelay(task, Status.OVER);
//                new VisitLogDoneMsg(task.getUserId(), task.getTs()).publish();
            } else {
                // TODO how can this fails???? we can see this later...
                ATSLocalTask.updateStatusInDelay(task, Status.NEW);
            }
        }
//        return Boolean.FALSE;
    }

    private Boolean writeTrades(File file, User user, Long ts) {

        log.info(format("writeTrades:file, user, ts".replaceAll(", ", "=%s, ") + "=%s", file, user, ts));
        try {
            List<String> readLines = FileUtils.readLines(file);
            List<Trade> trades = new ArrayList<Trade>();
            for (String line : readLines) {
                TradeFullinfoGetResponse rsp = TaobaoUtils.parseResponse(line, TradeFullinfoGetResponse.class);
                if (rsp.getTrade() != null) {
                    trades.add(rsp.getTrade());
                }

            }

            TradeWritter.addTradeList(user.getId(), ts, trades, false);
            TradeWritter.addFinishedMarkMsg(user.getId(), ts);

            return Boolean.TRUE;
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        } catch (ApiException e) {
            log.warn(e.getMessage(), e);
        }
        return Boolean.FALSE;
    }

}
