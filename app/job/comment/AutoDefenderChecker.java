
package job.comment;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;

import jdp.JdpModel;
import job.defense.TradeMsgDealerJob;
import models.item.ItemPlay;
import models.trade.TradeDisplay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;

import com.taobao.api.domain.Trade;

import configs.TMConfigs;
import configs.TMConfigs.Rds;
import configs.TMConfigs.Server;
import controllers.APIConfig;

public class AutoDefenderChecker extends Job {


    @Every("3s")
    public static class AutoDefenderTimer extends Job {
        public void doJob() {
            TMConfigs.getShowwindowPool().submit(new Callable<ItemPlay>() {
                @Override
                public ItemPlay call() throws Exception {
                    if (APIConfig.get().getApp() != APIConfig.defender.getApp()) {
                        return null;
                    }

                    new AutoDefenderChecker().doJob();
                    return null;
                }
            });
        }
    }

    public static final String TAG = "AutoDefenderChecker";

    private static final Logger log = LoggerFactory.getLogger(AutoDefenderChecker.class);

    private static int retry = 5;

    public void doJob() {

        if (!Rds.enableJdpPush) {
            return;
        }

        if (!Server.jobTimerEnable) {
            return;
        }

        Thread.currentThread().setName(TAG);
//        if (APIConfig.get().getApp() != APIConfig.defender.getApp()) {
//            return;
//        }
//        WorkTagUpdateTs workts = WorkTagUpdateTs.findOrCreate(AutoDefenderChecker.TAG);
        long end = System.currentTimeMillis() - 1 * 1000L;
        long start = end - 10 * 1000L;

        //TRADE_FINISHED
        Map<Long, Trade> tidTrades = JdpModel.JdpTradeModel.recentCreated(start, end);
        Collection<Trade> trades = tidTrades.values();
        log.info("[found auto created for defender:]" + tidTrades.keySet());

        for (Trade trade : trades) {
            if (TradeDisplay.isRecentCreated(trade)) {
//                log.info("[check trade new created:]" + trade);
                // TODO prepare for the is recent created job..
                TradeMsgDealerJob.addTrade(trade);
            } else {
                // Next...
//                log.info("[not  trade new created:]" + trade);
            }
        }
    }

}
