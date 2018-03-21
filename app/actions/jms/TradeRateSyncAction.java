package actions.jms;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import models.jms.MsgContent;
import models.traderate.TradeRatePlay;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bustbapi.TradeRateApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.PYFutureTaskPool;
import com.taobao.api.domain.TradeRate;

import controllers.APIConfig;
import dao.UserDao;
import dao.trade.TradeRatePlayDao;

public class TradeRateSyncAction {

	public static final Logger log = LoggerFactory.getLogger(TradeRateSyncAction.class);

	static PYFutureTaskPool<Void> pool = new PYFutureTaskPool<Void>(16);


	public static void doTradeRateSync(MsgContent msgContent) {
		if (!APIConfig.get().enableSyncTradeRate()) {
			return;
		}
		
		if(msgContent == null) {
			log.error("~~~~~msgContent为空！~~~~~");
			return;
		}
		
		String userNick = msgContent.getSeller_nick();
		if(StringUtils.isEmpty(userNick)) {
			log.error("~~~~~msgContent中Seller_nick为空！~~~~~");
			return;
		}
		
		User user = UserDao.findByUserNick(userNick);
		if(user == null) {
			log.error("~~~~~数据库中未匹配到用户！userNick： " + userNick + "~~~~~");
			return;
		}
		
		Long oid = msgContent.getOid();
		if(oid <= 0) {
			log.error("~~~~~msgContent中oid为空！~~~~~");
			return;
		}
		
		TradeRatePlay tradeRatePlay = TradeRatePlayDao.findByUserIOid(user.getId(), oid);
		List<TradeRate> rateList = new TradeRateApi.TraderatesGet(user, oid).call();

		// 数据库中评价不存在
		if(tradeRatePlay == null) {
			if(!CommonUtils.isEmpty(rateList)) {
				TradeRate tradeRate = rateList.get(0);
				int newRate = TradeRatePlay.parseRate(tradeRate.getResult());
				// 中差评
				if(newRate != 1) {
					new TradeRatePlay(tradeRate, user.getId()).rawInsert();
					log.info("~~~~~保存新增的中差评~~~~~tradeRate： " + String.valueOf(tradeRate) + "~~~~~");
				} else {
					log.info("~~~~~一开始就是好评~~~~~tradeRate： " + String.valueOf(tradeRate) + "~~~~~");
				}
			}
		// 数据库中评价存在
		} else {
			if(CommonUtils.isEmpty(rateList)) {
				if (tradeRatePlay.getLatestRate() > 0) {
					tradeRatePlay.setNewRate(0);
					tradeRatePlay.setUpdated(System.currentTimeMillis());
					tradeRatePlay.rawUpdate();
				} else if (tradeRatePlay.getLatestRate() == 0) {
					int rate = tradeRatePlay.getRate();
					int lastRate = 0;
					while (rate > 0) {
						lastRate = rate;
						rate = (rate >> 2);
					}

					int newRate = (lastRate << 2);
					if (newRate != tradeRatePlay.getRate()) {
						tradeRatePlay.setRate(newRate);
						tradeRatePlay.rawUpdate();
					}
				}
				log.info("~~~~~删除已删除的评价~~~~~tradeRatePlay： " + String.valueOf(tradeRatePlay) + "~~~~~");
			} else {
				TradeRate tradeRate = rateList.get(0);
				int newRate = TradeRatePlay.parseRate(tradeRate.getResult());
				if (tradeRatePlay.getLatestRate() != newRate) {
					Boolean toWriteDB = tradeRatePlay.putTradeRate(tradeRate);
					if (toWriteDB == Boolean.TRUE) {
						tradeRatePlay.rawUpdate();
					}
					log.info("~~~~~更新已有的评价~~~~~tradeRatePlay： " + String.valueOf(tradeRatePlay) + "~~~~~");
				} else {
					log.info("~~~~~更新已有的评价(已经被messagedealer处理)~~~~~tradeRatePlay： " + String.valueOf(tradeRatePlay) + "~~~~~");
				}
			}
		}
	}
	
}
