
package ats;

import models.visit.ATSLocalTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transaction.TransactionSecurity;
import ats.TaskManager.Status;
import bustbapi.TBApi;

import com.taobao.api.ApiException;
import com.taobao.api.TaobaoClient;
import com.taobao.api.domain.Task;
import com.taobao.api.request.TopatsResultGetRequest;
import com.taobao.api.response.TopatsResultGetResponse;

public class ATSResultGetAPI {

    private static final Logger log = LoggerFactory.getLogger(ATSResultGetAPI.class);

    private static final String TAG = "ATSResultGetAPI";

    /**
     * 根据TaskId获取任务结果
     * 
     * @param taskId
     */
    public static Task getTaskResult(final Long taskId) {
        Task task = null;
        if (taskId == null) {
            log.error("taskId is NULL");
            return null;
        }
//        log.info("Get Task With ID:" + taskId);

        TaobaoClient client = TBApi.genClient();
        TopatsResultGetRequest req = new TopatsResultGetRequest();

        req.setTaskId(taskId);
        try {
            TopatsResultGetResponse response = client.execute(req);
            if (response == null) {
                log.warn("Null Response");
                return null;
            }

            if (!response.isSuccess()) {
                checkForTheUnSuccessResponse(taskId, response);
            }

            task = response.getTask();
            if (task == null) {
                log.warn("Null task returned ....");
                return null;
            }
            // TODO makes it debug...
            TaskManager.infoTaobaoTask(task);

        } catch (ApiException e) {
            log.warn(e.getMessage(), e);
        }

        return task;
    }

    private static void checkForTheUnSuccessResponse(final Long taskId,
            TopatsResultGetResponse response) {
        log.error(response.getErrorCode() + ":" + response.getMsg() + "," + response.getSubCode()
                + ":" + response.getSubMsg());
        // 600:Remote service error,isv.task-not-exist:该任务不存在
        // TODO we need to check...
        // [2012-03-29 01:04:07,246] ERROR [jobs-thread-6]
        // tbapi.ATSResultGetAPI.getTaskResult(ATSResultGetAPI.java:58)
        // - 600:Remote service error,isv.task-result-empty:异步任务结果为空
        if ("600".equals(response.getErrorCode()) || "700".equals(response.getErrorCode())) {
            if ("isv.task-not-exist".equals(response.getSubCode())) {
                ATSTaskReceiver.onNotExists(taskId);
                return;
            } else if ("isv.task-result-empty".equals(response.getSubCode())) {
                log.warn("resutl empty:" + taskId);
                Task returnTBtask = new Task();
                returnTBtask.setTaskId(taskId);
                returnTBtask.setStatus(Status.OVER);
                response.setTask(returnTBtask);

                new TransactionSecurity<Void>() {

                    @Override
                    public Void operateOnDB() {
                        ATSLocalTask localTask = ATSLocalTask.findByTask(taskId);
                        if (localTask == null) {
                            return null;
                        }

/*                        new VisitLogDoneMsg(localTask.getUserId(), localTask.getTs()).publish();
//                        localTask.setStatus(Status.OVER);
//                        localTask.save();
                        VisitLogUpdateTs.updateLastVisitlogModifedTime(localTask.getUserId(),
                                localTask.getTs());*/
                        return null;
                    }
                }.execute();
            }
        }
    }
}
