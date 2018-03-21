/**
 * 
 */
package job.defense;

import java.util.List;

import models.order.OrderDisplay;
import models.trade.TradeDisplay;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import play.jobs.On;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;

import configs.TMConfigs;
import dao.UserDao;
import dao.UserDao.UserBatchOper;

/**
 * @author navins
 * @date 2014-1-5 上午1:26:54
 */
@On("0 0 5 * * ? *")
public class DailyTradeCleanJob extends Job {
    private static final Logger log = LoggerFactory.getLogger(DailyTradeCleanJob.class);

    private static final String Delete_Trade_Sql = "delete from trade_display_%s where userId = ? and created < ?";

    private static final String Delete_Order_Sql = "delete from order_display_%s where userId = ? and created < ?";

    private static final String Delete_TradeRate_Sql = "delete from trade_rate_%s where userId = ? and created < ?";

    @Override
    public void doJob() throws Exception {

        if (!TMConfigs.App.IS_TRADE_ALLOW) {
            return;
        }

        log.warn("[DailyTradeCleanJob] clean start!!!!");

        final Long ts = System.currentTimeMillis() - 45L * DateUtil.DAY_MILLIS;

        new UserBatchOper(32) {
            @Override
            public List<User> findNext() {
                return UserDao.findAllUserList(offset, limit);
            }

            @Override
            public void doForEachUser(User user) {
                if (user == null || user.isVaild()) {
                    return;
                }

                // 当前不是valid的用户才清理。
                String sql = TradeDisplay.genShardQuery(Delete_Trade_Sql, user.getId());
                TradeDisplay.dp.update(sql, user.getId(), ts);

                sql = OrderDisplay.genShardQuery(Delete_Order_Sql, user.getId());
                OrderDisplay.dp.update(sql, user.getId(), ts);

//                sql = TradeRatePlay.genShardQuery(Delete_TradeRate_Sql, user.getId());
//                TradeRatePlay.dp.update(sql, user.getId(), ts);

                CommonUtils.sleepQuietly(300);
            }
        }.call();

        log.warn("[DailyTradeCleanJob] data clean complete!");
    }
}
