package controllers;

import job.jd.JDItemUpdateJob;
import models.jd.JDUser;
import models.updatetimestamp.updates.ItemUpdateTs;
import models.updatetimestamp.updatestatus.ItemDailyUpdateTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.utils.DateUtil;

public class JDItems extends JDController {

    public static final Logger log = LoggerFactory.getLogger(JDItems.class);

    /**
     * 同步数据
     */
    public static void sync() {
        JDUser user = getUser();
        log.info("[get user]" + getUser());
        try {
            boolean first = true;
            ItemUpdateTs ts = ItemUpdateTs.fetchByUser(user.getId());
            if (ts != null) {
                ts.jdbcDelete(user.getId());
            }
            ItemDailyUpdateTask taskTs = ItemDailyUpdateTask.findByUserIdAndTs(user.getId(), DateUtil.formCurrDate());
            if (taskTs != null) {
                ItemDailyUpdateTask.deleteOne(taskTs);
            }

            new JDItemUpdateJob(user).doJob();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void firstSync() {
        renderJSON("{}");
    }

    public static void sellerCatCount() {

    }

    public static void itemCatCount() {

    }

}
