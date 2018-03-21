
package job.writter;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import models.oplog.TitleOptimiseLog;
import models.user.TitleOptimised;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;

import com.ciaosir.client.CommonUtils;
import com.taobao.api.domain.Item;

@Every("3s")
public class TitleOptimisedWritter extends Job {

    private static final Logger log = LoggerFactory.getLogger(TitleOptimisedWritter.class);

    public static final String TAG = "TitleOptimisedWritter";

    static Queue<TitleOptimised> msgs = new ConcurrentLinkedQueue<TitleOptimised>();

    public void doJob() {
        TitleOptimised msg = null;
        while ((msg = msgs.poll()) != null) {
            // -1 代表是更新数据完之后检查TitleOptimised表是否有对应记录
            if (msg.ts == -1) {

                long count = TitleOptimiseLog.count("userId = ? and numIid = ?",
                        msg.getUserId(), msg.getNumIid());
                if (count <= 0) {
                    new TitleOptimised(msg.getUserId(), msg.getNumIid(), false,
                            System.currentTimeMillis()).jdbcSave();
                } else {
                    new TitleOptimised(msg.getUserId(), msg.getNumIid(), true,
                            System.currentTimeMillis()).jdbcSave();
                }

            } else {
                msg.jdbcSave();
            }
        }
    }

    public static void addMsg(Long userId, Long numIid, boolean isOptimised) {
        addMsg(new TitleOptimised(userId, numIid, isOptimised, System.currentTimeMillis()));
    }

    public static void addMsg(Long userId, Long numIid, boolean isOptimised, Long ts) {
        addMsg(new TitleOptimised(userId, numIid, isOptimised, ts));
    }

    public static void addMsg(TitleOptimised msg) {
        msgs.add(msg);
    }

    // -1 代表是更新数据完之后检查TitleOptimised表是否有对应记录
    public static void addMsg(Long userId, List<Item> itemsGet) {
        if (CommonUtils.isEmpty(itemsGet)) {
            return;
        }
        for (Item item : itemsGet) {
            addMsg(userId, item.getNumIid(), false, -1L);
        }
    }
}
