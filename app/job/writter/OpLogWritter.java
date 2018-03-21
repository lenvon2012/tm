
package job.writter;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import models.oplog.OpLog;
import models.oplog.OpLog.LogType;
import play.jobs.Every;
import play.jobs.Job;

@Every("15s")
public class OpLogWritter extends Job {

    static Queue<OpLog> queue = new ConcurrentLinkedQueue<OpLog>();

    @Override
    public void doJob() {
        OpLog pLog = null;
        List<OpLog> logs = new ArrayList<OpLog>();

        while ((pLog = queue.poll()) != null) {
            logs.add(pLog);
        }

        OpLog.batchWrite(logs);
    }

    public static void addMsg(Long userId, String content, Long numIid, LogType type, boolean isError) {
//        switch (type) {
//            case ShowWindow:
//                if (ShowWindowConfig.enableRemoteWindow) {
//                    WindowsService.addLog(userId, numIid);
//                    return;
//                }
//
//                break;
//
//            default:
//                break;
//        }

        queue.add(new OpLog(userId, content, numIid, type, isError));
    }
}
