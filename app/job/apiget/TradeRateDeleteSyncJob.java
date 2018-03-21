/**
 * 
 */

package job.apiget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import models.traderate.TradeRatePlay;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transaction.MapIterator;
import utils.DateUtil;
import bustbapi.TradeRateApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.PYFutureTaskPool;
import com.ciaosir.client.pojo.PageOffset;
import com.taobao.api.domain.TradeRate;

import controllers.APIConfig;
import dao.trade.TradeRatePlayDao;

/**
 * @author navins
 * @date: Nov 12, 2013 3:36:19 PM
 */
public class TradeRateDeleteSyncJob {

    public static final Logger log = LoggerFactory.getLogger(TradeRateDeleteSyncJob.class);

    static PYFutureTaskPool<Void> pool = new PYFutureTaskPool<Void>(16);

    public static void doWithDeleteTradeRate(final User user, boolean status) {
        if (!APIConfig.get().enableSyncTradeRate()) {
            return;
        }
        
        long end = System.currentTimeMillis();
        long start = end - DateUtil.THIRTY_DAYS;
//        PageOffset po = new PageOffset(1, 3000);

//        List<TradeRatePlay> list = TradeRatePlayDao.searchWithArgs(user.getId(), null, null, 128,
//                start, null, null, null, po);
//        if (CommonUtils.isEmpty(list)) {
//            return;
//        }
        List<TradeRatePlay> list = new ArrayList<TradeRatePlay>();
        int currCount = 0;
        int pn = 1;
        do {
        	PageOffset po = new PageOffset(pn, 3000);
        	List<TradeRatePlay> currList = TradeRatePlayDao.searchWithArgs(user.getId(), null, null, 4,
        			start, null, null, null, po);
        	if(!CommonUtils.isEmpty(currList)) {
        		list.addAll(currList);
        		currCount = currList.size();
        		pn ++;
        	} else {
        		currCount = 0;
        	}
		} while (currCount == 3000);
        
        if(CommonUtils.isEmpty(list)) {
        	return;
        }

        HashMap<Long, TradeRatePlay> rateMap = new HashMap<Long, TradeRatePlay>();
        for (TradeRatePlay tradeRatePlay : list) {
            rateMap.put(tradeRatePlay.getOid(), tradeRatePlay);
//            pool.submit(new SyncTradeRateCaller(user, tradeRatePlay));
        }

        List<TradeRate> rateList = TradeRateApi.fetchBadTradeRate(user, start, end);
        
//        List<TradeRate> rateList = new ArrayList<TradeRate>();
//        TMResult<List<TradeRate>> result = TradeRateApi.fetchBadTradeRateNew(user, start, end);
//        if(result.isOk) {
//        	rateList = result.getRes();
//        } else {
//        	TMResult<List<TradeRate>> newResult = TradeRateApi.fetchBadTradeRateNew(user, start, end);
//        	if(!newResult.isOk) {
//        		return;
//        	}
//        	rateList = newResult.getRes();
//		}
        
        if (!CommonUtils.isEmpty(rateList)) {
            for (TradeRate tradeRate : rateList) {
                TradeRatePlay tradeRatePlay = rateMap.get(tradeRate.getOid());
                if (tradeRatePlay != null) {
                    // 找到该记录对应的TradeRatePlay，对比处理
                    int newRate = TradeRatePlay.parseRate(tradeRate.getResult());
                    if (tradeRatePlay.getLatestRate() != newRate) {
                        Boolean toWriteDB = tradeRatePlay.putTradeRate(tradeRate);
                        if (toWriteDB == Boolean.TRUE) {
                            tradeRatePlay.rawUpdate();
                        }
                    }
                    rateMap.remove(tradeRate.getOid());
                } else {
                    // 数据库里没有这个的记录，保存下来
                    new TradeRatePlay(tradeRate, user.getId()).rawInsert();
                }
            }
            rateList.clear();
        }
        

        // 剩余没找到评价的，表示评价意见被删除。
//        final long userId = user.getId();
        if(status) {
	        new MapIterator<Long, TradeRatePlay>(rateMap) {
	            @Override
	            public void execute(Entry<Long, TradeRatePlay> entry) {
	                TradeRatePlay tradeRatePlay = entry.getValue();
	                log.info("[deleteRate]prepare tid=" + tradeRatePlay.getTid() + ", oid=" + tradeRatePlay.getOid()
	                        + ", userId=" + tradeRatePlay.getUserId() + ", nick=" + tradeRatePlay.getNick());
	
	                pool.submit(new SyncTradeRateCaller(user, tradeRatePlay));

	            }
	        }.call();
        } else {
        	List<TradeRate> goodRateList = TradeRateApi.fetchGoodTradeRate(user, start, end);
        	final HashMap<Long, TradeRate> goodRateMap = new HashMap<Long, TradeRate>();
        	if(!CommonUtils.isEmpty(goodRateList)) {
        		for (TradeRate tradeRate : goodRateList) {
        			goodRateMap.put(tradeRate.getOid(), tradeRate);
        		}
        	}

        	new MapIterator<Long, TradeRatePlay>(rateMap) {
	            @Override
	            public void execute(Entry<Long, TradeRatePlay> entry) {
	                TradeRatePlay tradeRatePlay = entry.getValue();
	                log.info("[deleteRate]prepare tid=" + tradeRatePlay.getTid() + ", oid=" + tradeRatePlay.getOid()
	                        + ", userId=" + tradeRatePlay.getUserId() + ", nick=" + tradeRatePlay.getNick());
	                
	                pool.submit(new TradeRateSyncCaller(user, goodRateMap, tradeRatePlay));
	                
	            }
	        }.call();
		}
    }

	public static class SyncTradeRateCaller implements Callable<Void> {

		public User user;

		public TradeRatePlay tradeRatePlay;

		public SyncTradeRateCaller(User user, TradeRatePlay tradeRatePlay) {
			this.user = user;
			this.tradeRatePlay = tradeRatePlay;
		}

		@Override
		public Void call() throws Exception {
			List<TradeRate> rateList = new TradeRateApi.TraderatesGet(user, tradeRatePlay.getTid()).call();

			boolean found = false;
			if (!CommonUtils.isEmpty(rateList)) {
				for (TradeRate tradeRate : rateList) {
					if (tradeRate.getOid() == tradeRatePlay.getOid()) {
						int newRate = TradeRatePlay.parseRate(tradeRate.getResult());
						if (tradeRatePlay.getLatestRate() != newRate) {
							Boolean toWriteDB = tradeRatePlay.putTradeRate(tradeRate);
							if (toWriteDB == Boolean.TRUE) {
								tradeRatePlay.rawUpdate();
							}
						}
						found = true;
						break;
					}
				}
			}
			
			Thread.sleep(1000);
			// 以防万一，再调用一次api确认评价是被删除状态
			if (CommonUtils.isEmpty(rateList) || found == false) {
				rateList = new TradeRateApi.TraderatesGet(user, tradeRatePlay.getTid()).call();
			}

			// rateList为null 表示接口请求出错  不执行删除操作 直接返回
			if (rateList == null) return null;

			// rateList大小为0 表示请求成功 但没有数据  执行删除评价操作
			if (rateList != null && rateList.isEmpty() || found == false) {
				if (tradeRatePlay.getLatestRate() > 0) {
					log.warn(String.format("[delete comment] userId=%d, tid=%d, oid=%d", user.getId(),
							tradeRatePlay.getTid(), tradeRatePlay.getOid()));
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
			}
			if(!CommonUtils.isEmpty(rateList)) {
				rateList.clear();
			}
			return null;
		}

	}


	public static class TradeRateSyncCaller implements Callable<Void> {
		
		public User user;
		
		public HashMap<Long, TradeRate> goodRateMap;

		public TradeRatePlay tradeRatePlay;

		public TradeRateSyncCaller(User user, HashMap<Long, TradeRate> goodRateMap, TradeRatePlay tradeRatePlay) {
			this.user = user;
			this.goodRateMap = goodRateMap;
			this.tradeRatePlay = tradeRatePlay;
		}

		@Override
		public Void call() throws Exception {
			boolean found = false;
			Long oid = tradeRatePlay.getOid();
			if(goodRateMap.containsKey(oid)){
				TradeRate tradeRate = goodRateMap.get(oid);
				int newRate = TradeRatePlay.parseRate(tradeRate.getResult());
				if (tradeRatePlay.getLatestRate() != newRate) {
					Boolean toWriteDB = tradeRatePlay.putTradeRate(tradeRate);
					if (toWriteDB == Boolean.TRUE) {
						tradeRatePlay.rawUpdate();
					}
				}
				found = true;
			} else {
				// 以防万一，再调用一次api确认评价状态
				List<TradeRate> rateList = new TradeRateApi.TraderatesGet(user, tradeRatePlay.getTid()).call();

				if (!CommonUtils.isEmpty(rateList)) {
					for (TradeRate tradeRate : rateList) {
						if (tradeRate.getOid() == tradeRatePlay.getOid()) {
							int newRate = TradeRatePlay.parseRate(tradeRate.getResult());
							if (tradeRatePlay.getLatestRate() != newRate) {
								Boolean toWriteDB = tradeRatePlay.putTradeRate(tradeRate);
								if (toWriteDB == Boolean.TRUE) {
									tradeRatePlay.rawUpdate();
								}
							}
							found = true;
							break;
						}
					}
				}
			}
			
			
			if (CommonUtils.isEmpty(goodRateMap) || found == false) {
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
			}
			return null;
		}
	}

}
