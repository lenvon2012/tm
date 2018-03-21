package job.log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import jdp.JdpModel.JdpTradeModel;

import com.ciaosir.client.CommonUtils;
import com.taobao.api.domain.Trade;

import models.trade.TradeDisplay;
import models.user.User;
import play.Play;
import play.jobs.Job;
import play.jobs.On;
import secure.SimulateRequestUtil;
import configs.TMConfigs.Server;
import controllers.APIConfig;
import dao.UserDao.UserBatchOper;
import dao.trade.TradeDisplayDao;

//@Every("1min")
@On("0 0 4 * * ?")
public class SimulateRequestJob extends Job {

	protected static final Logger log = Logger.getLogger(SimulateRequestJob.class);
    @Override
    public void doJob() throws Exception {

        if (!APIConfig.get().isRisk()) {
            return;
        }
        if (!Server.jobTimerEnable || Play.mode.isDev()) {
            return;
        }
        // 仅限淘掌柜
        if (!"21255586".equals(APIConfig.get().getApiKey())) {
            return;
        }

        sendLog();
    }

    public static Boolean sendLog() {

        return new UserBatchOper(64) {
            @Override
            public void doForEachUser(final User user) {
                // SQL
                Long userId = user.getId();
                String query = "select count(tid) from trade_display_%s where userId = " + userId;
                query = TradeDisplay.genShardQuery(query, userId);

                SimulateRequestUtil.sendSqlLog("订单数量查询", APIConfig.get().getRdsHostAddress(), query);
                // top
                SimulateRequestUtil.sendTopLog("taobao.trades.sold.get");
                // order
                List<Long> tradeIds = new ArrayList<Long>();
                Map<Long, Trade> tradeMap = JdpTradeModel.queryRecentModifiedTrades(user.getUserNick());
                for (Long key : tradeMap.keySet()) {  
                	tradeIds.add(key); 
                }
                log.error("sendOrderLog size is " + tradeIds.size());
                SimulateRequestUtil.sendOrderLog("订单详情查询", tradeIds);
            }
        }.call();

    }
}
