package job.comment;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;

import jdp.JdpModel;
import job.writter.CommentsWritter;
import models.comment.CommentConf;
import models.updatetimestamp.updates.WorkTagUpdateTs;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;
import result.TMResult;
import bustbapi.TradeRateApi;
import bustbapi.TradeRateApi.TraderateListAdd;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.PYFutureTaskPool;
import com.ciaosir.client.utils.DateUtil;
import com.taobao.api.domain.Trade;
import com.taobao.api.domain.TradeRate;

import configs.TMConfigs.Rds;
import configs.TMConfigs.Server;
import dao.UserDao;
import dao.defense.BlackListBuyerDao;

@Every("10s")
public class AutoCommentTimer extends Job {

    public static final String TAG = "AutoCommentTimer";

    private static final Logger log = LoggerFactory.getLogger(AutoCommentTimer.class);

    private static int retry = 5;

    static PYFutureTaskPool<Boolean> pool = null;

    public void doJob() {
        if (!Rds.enableJdpPush) {
            return;
        }
        if (!Server.jobTimerEnable) {
            return;
        }
        if (pool == null) {
            pool = new PYFutureTaskPool<Boolean>(16);
        }

//        long end = System.currentTimeMillis() - 10 * 1000L;
//        long start = end - 25 * 1000L;
        WorkTagUpdateTs workts = WorkTagUpdateTs.findOrCreate(AutoCommentTimer.TAG);
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
        Map<Long, Trade> tidTrades = JdpModel.JdpTradeModel.recentFinished(start, end);
        Collection<Trade> trades = tidTrades.values();
        log.info("[found auto comment trade finished :]" + tidTrades.keySet().size());

        for (Trade trade : trades) {
            if (trade.getSellerRate()) {
                continue;
            }

            Long tid = trade.getTid();
            if (tid == null) {
                continue;
            }

            String sellerNick = trade.getSellerNick();
            User user = UserDao.findByUserNick(sellerNick);

            if (user == null || !user.isAutoCommentOn() || user.isTmall()) {
                continue;
            }

            String buyerNick = trade.getBuyerNick();

            // 如果是抢评的，那么暂时不评价
            CommentConf conf = CommentConf.findByUserId(user.getId());
            if (conf != null && conf.getCommentType() > 0) {
                /*log.info("AutoCommentTimer : 发现需要抢评的订单，订单号为[" + tid + "], " + "卖家为[" + user.getUserNick() + "], 买家为["
                        + buyerNick + "]");*/
                continue;
            }

            doForUser(user, tid, buyerNick, sellerNick);
        }
        if(trades!=null && trades.size() > 0){
            trades.clear();
        }
        WorkTagUpdateTs.updateLastModifedTime(TAG, end);
    }

    private static void doForUser(final User user, final Long tid, final String buyerNick, final String sellerNick) {

        pool.submit(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                TMResult res = commentByOrder(user, tid, buyerNick);

                if (!res.isOk()) {
                    log.info("tmc auto comment failed for sellerNick [" + sellerNick + "] and tid[" + tid
                            + "] and buyerNick[" + buyerNick + "]");
                }
                return null;
            }
        });
        CommonUtils.sleepQuietly(10L);
    }

	public static TMResult commentByOrder(User user, Long tid, String buyerNick) {
//		log.info("commentByOrder : userNick = [" + user.getUserNick() + "], tid = [" + tid + "], buyserNick = [" + buyerNick + "]");

		if (user == null) {
			return new TMResult(false, "user为空", null);
		}

		if (tid == null) {
			return new TMResult(false, "tid为空", null);
		}

		if (StringUtils.isEmpty(buyerNick)) {
			return new TMResult(false, "buyerNick为空", null);
		}
		
		try {
			if (user.isTmall()) {
//				log.info("tmall seller, can not rate trade, return now!!!");
				return new TMResult(false, "该用户为天猫用户", null);
			}
			
			if (!user.isAutoCommentOn()) {
//				log.info("auto comment is not open!!! for user:" + user);
				return new TMResult(false, "该用户自动评价已关闭", null);
			}
			
			if (BlackListBuyerDao.findByBuyerName(user.getId(), buyerNick) != null) {
//				log.info("黑名单用户，不评价");
				return new TMResult(false, "该用户是黑名单用户", null);
			}
			
			CommentConf commentConf = CommentConf.findByUserId(user.getId());
			String content = StringUtils.EMPTY;
			if (commentConf != null) {
				content = commentConf.getRandomComment();
			} else {
				content = "很好的买家，欢迎下次再来！";
			}
			
			int count = 0;
			while (count++ < retry) {
				String res = doComemntContent(user, tid, buyerNick, content);
				if (!StringUtils.isEmpty(res)) {
					return new TMResult(true, res, null);
				}
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}

		return new TMResult(false, "评价接口调用失败", null);
	}

	private static String doComemntContent(User user, Long tid, String buyerNick, String content) {
		TraderateListAdd api = new TradeRateApi.TraderateListAdd(user, tid, "good", "seller", content);
		TradeRate tradeRate = api.call();
		if (tradeRate == null) {
			String msg = api.getSubErrorMsg();
//			log.info("~~~自动评价：接口异常：【" + msg + "】~~~");
			if(StringUtils.isEmpty(msg) || !msg.contains("主订单不可以评价")) {
				// 如果不成功 并且 错误信息不包含【主订单不可以评价】，休眠一秒
				CommonUtils.sleepQuietly(1000L);
				return null;
			}
//			log.info("~~~自动评价：该订单已被评价，跳过~~~");
		}
		String realContent = StringUtils.EMPTY;
		if (content.length() > 255) {
			realContent = content.substring(0, 250).concat("...");
		} else {
			realContent = content;
		}
		CommentsWritter.addMsg(user.getId(), tid, tid, "good", realContent, user.getUserNick(), buyerNick);
		tradeRate = null;
		return realContent;
	}
	
}
