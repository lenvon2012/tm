package controllers;

import java.io.IOException;

import job.paipai.PaiPaiItemUpdateJob;
import job.paipai.PaiPaiTradeUpdateJob;
import models.paipai.PaiPaiUser;
import models.updatetimestamp.updates.ItemUpdateTs;
import models.updatetimestamp.updatestatus.ItemDailyUpdateTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ppapi.models.PaiPaiItem;
import result.TMResult;

import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.DateUtil;
import com.ciaosir.client.utils.JsonUtil;

public class PaiPaiItems extends PaiPaiController {

    public static final Logger log = LoggerFactory.getLogger(PaiPaiItems.class);

    /**
     * 同步数据
     */
    public static void sync() {
        PaiPaiUser user = getUser();
        log.info("[get user]" + getUser());
        boolean first = true;
        ItemUpdateTs ts = ItemUpdateTs.fetchByUser(user.getId());
        if (ts != null) {
            ts.jdbcDelete(user.getId());
        }
        ItemDailyUpdateTask taskTs = ItemDailyUpdateTask.findByUserIdAndTs(user.getId(), DateUtil.formCurrDate());
        if (taskTs != null) {
            ItemDailyUpdateTask.deleteOne(taskTs);
        }
        new PaiPaiItemUpdateJob(user.getId(), first).doJob();
    }

    public static void syncSale() {
        PaiPaiUser user = getUser();
        log.info("[get user]" + getUser());
        boolean first = true;
        ItemUpdateTs ts = ItemUpdateTs.fetchByUser(user.getId());
        if (ts != null) {
            ts.jdbcDelete(user.getId());
        }
        ItemDailyUpdateTask taskTs = ItemDailyUpdateTask.findByUserIdAndTs(user.getId(), DateUtil.formCurrDate());
        if (taskTs != null) {
            ItemDailyUpdateTask.deleteOne(taskTs);
        }
        new PaiPaiItemUpdateJob(user.getId(), first, true).doJob();
    }

    public static void tradeSync() {
        PaiPaiUser user = getUser();
        log.info("[get user]" + getUser());
        boolean first = true;
        ItemUpdateTs ts = ItemUpdateTs.fetchByUser(user.getId());
        if (ts != null) {
            ts.jdbcDelete(user.getId());
        }
        ItemDailyUpdateTask taskTs = ItemDailyUpdateTask.findByUserIdAndTs(user.getId(), DateUtil.formCurrDate());
        if (taskTs != null) {
            ItemDailyUpdateTask.deleteOne(taskTs);
        }
        try {
            Long end = System.currentTimeMillis();
            Long start = end - DateUtil.THIRTY_DAYS;
            new PaiPaiTradeUpdateJob(user.getId(), start, end).doJob();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            log.info(e.getMessage());
        }
    }

    public static void firstSync() {
        renderJSON("{}");
    }

    public static void sellerCatCount() {

    }

    public static void itemCatCount() {

    }

    public static void list(int pn, int ps, String s) throws IOException {
        renderMockFileInJsonIfDev("paipaiitems.list.json");
        PaiPaiUser pUser = getUser();
        PageOffset po = new PageOffset(pn, ps);
        TMResult res = PaiPaiItem.fetchTMResult(pUser.getId(), s, po);
        renderJSON(JsonUtil.getJson(res));
    }

}
