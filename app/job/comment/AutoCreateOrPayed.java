
package job.comment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jdp.JdpModel;
import job.writter.TradeWritter;
import models.op.WhiteTransRateId;
import models.updatetimestamp.updates.WorkTagUpdateTs;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import play.jobs.Every;
import play.jobs.Job;

import com.ciaosir.client.utils.DateUtil;
import com.taobao.api.domain.Trade;

import configs.Subscribe.Version;
import configs.TMConfigs.Rds;
import configs.TMConfigs.Server;
import controllers.APIConfig;
import dao.UserDao;

//@Every("10s")
public class AutoCreateOrPayed extends Job {

    private static final Logger log = LoggerFactory.getLogger(AutoCreateOrPayed.class);

    public static final String TAG = "AutoCreateOrPayed";

    private static int retry = 5;

    private String genTradeKey(Trade trade) {
        return TAG + trade.getStatus() + trade.getTid();
    }

    public void doJob() {
        if (!Rds.enableJdpPush) {
            return;
        }
        if (!Server.jobTimerEnable) {
            return;
        }
        if (!APIConfig.get().enableInstantTradeSync()) {
            return;
        }

//        long end = System.currentTimeMillis() - 10 * 1000L;
//        long start = end - 25 * 1000L;
        WorkTagUpdateTs workts = WorkTagUpdateTs.findOrCreate(AutoCreateOrPayed.TAG);
        long start = workts.getLastUpdateTime() - 15 * 1000L;
        long end = System.currentTimeMillis() - 10 * 1000L;
        long maxEnd = start + DateUtil.ONE_HOUR;
        if (end > maxEnd) {
            end = maxEnd;
        }
        log.info("jdp auto comment [start ]" + workts);
        log.info("jdp auto comment [start ]" + DateUtil.formDateForLog(start) + "  with end :"
                + DateUtil.formDateForLog(end));

        //TRADE_FINISHED
//        Map<Long, Trade> tidTrades = JdpModel.JdpTradeModel.recentCreateOrPayed(start, end);
        Map<Long, Trade> tidTrades = JdpModel.JdpTradeModel.recentStatus(" true ", start, end);

        Collection<Trade> trades = tidTrades.values();
        Set<String> tradeKeys = new HashSet<String>();

        for (Trade trade : trades) {
            tradeKeys.add(genTradeKey(trade));
        }

        log.info("[found auto comment trade create or payed.. :]" + tidTrades.keySet().size());
        Iterator<Trade> it = trades.iterator();

        while (it.hasNext()) {
            Trade trade = it.next();

            if (Cache.get(genTradeKey(trade)) != null) {
                // Recently received...
                it.remove();
                continue;
            }
            String sellerNick = trade.getSellerNick();
            User user = UserDao.findByUserNick(sellerNick);

            if (user == null) {
                it.remove();
                continue;
            }

            if (user.getVersion() > Version.VIP || WhiteTransRateId.hasId(user.getId())) {
                // Now, we need to record...
                List<Trade> list = new ArrayList<Trade>();
                list.add(trade);
                TradeWritter.addTradeList(user.getId(), System.currentTimeMillis(), list);
            } else {
                it.remove();
                continue;
            }

        }
        for (String string : tradeKeys) {
            Cache.set(string, Boolean.TRUE, "3min");
        }

        WorkTagUpdateTs.updateLastModifedTime(TAG, end);
    }
}
