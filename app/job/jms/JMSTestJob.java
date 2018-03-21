package job.jms;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import models.jms.JMSMsgLog;
import models.jms.MsgContent;
import models.traderate.TradeRatePlay;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import actions.jms.TradeRateSyncAction;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Consumer;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.PropertyValueConst;
import com.ciaosir.client.PYFutureTaskPool;
import com.taobao.api.domain.TmcUser;

import configs.TMConfigs.Server;
import controllers.APIConfig;

//@OnApplicationStart(async = true)
public class JMSTestJob extends Job{
	
	private static final int APP = 21404171;
	
	private static String APP_KEY = "21404171";
	
	private static String APP_SECRET = "724576dc06e80ed8e38d1ad2f6de39da";
	
	private static String CONSUMERID = "CID_lzl";

	private static final Logger log = LoggerFactory.getLogger(JMSTestJob.class);
	
	static PYFutureTaskPool<Boolean> pool = new PYFutureTaskPool<Boolean>(32);
	
	@Override
	public void doJob() throws Exception {
		log.info("JMSTestJob launch!!");
		
		if (!Server.jobTimerEnable) {
			return;
		}
		// 好评助手
		if (APIConfig.get().getApp() != APP) {
			return;
		}
		doJMSTestJob();
	}

	private void doJMSTestJob() {
		Properties properties = new Properties();
		properties.put(PropertyKeyConst.ConsumerId, CONSUMERID);// ons控制台订阅管理中获取ConsumerID
		properties.put(PropertyKeyConst.AccessKey, APP_KEY);// 应用appkey，根据用户实际参数修改
		properties.put(PropertyKeyConst.SecretKey, APP_SECRET);// 应用密钥，根据用户实际参数修改
//		properties.put(PropertyKeyConst.OnsChannel, "CLOUD");// cloud为聚石塔标识
		properties.put(PropertyKeyConst.MessageModel, PropertyValueConst.CLUSTERING);//集群消费，默认为集群消费模式
//		properties.put(PropertyKeyConst.MessageModel, PropertyValueConst.BROADCASTING);广播消费模式
		final Consumer consumer = ONSFactory.createConsumer(properties); 
		consumer.subscribe("rmq_sys_jst_21404171", "*", new MessageListener() {// 消息队列名称，根据用户实际参数修改
			public Action consume(Message message, ConsumeContext context) {
				
				String cacheKey = "jms_count_";
				try {
					int count = Cache.get(cacheKey) == null? 0 : (Integer)Cache.get(cacheKey);
					count += 1;
					Cache.set(cacheKey, count, "24h");
					log.info("~~~~~当前jms_count_次数：" + count + "~~~~~");
				} catch(Exception e) {
					log.error("key " + cacheKey + " 没有对应的缓存");
				}
				
				String msg_id = message.getMsgID();
				String msg_body = StringUtils.EMPTY;
				try {
					msg_body = new String(message.getBody(), "utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				log.info("~~~~~Receive: " + message + "~~~~~msg_body: " + msg_body + "~~~~~");
				
				MsgContent msgContent = getMsgContent(msg_body);
				
				if(msgContent == null) {
					log.error("~~~~~解析msgContent失败！msg_body为： " + msg_body + "~~~~~");
					
					String errKey = "jms_error_";
					try {
						int count = Cache.get(errKey) == null? 0 : (Integer)Cache.get(errKey);
						count += 1;
						Cache.set(cacheKey, count, "72h");
						log.info("~~~~~当前jms_error_次数：" + count + "~~~~~");
					} catch(Exception e) {
						log.error("key " + cacheKey + " 没有对应的缓存");
					}
					
					return Action.ReconsumeLater;
				}
				
				if("buyer".equalsIgnoreCase(msgContent.getRater())) {
					// 储存jms消息日志
					JMSMsgLog jmsMsgLog = new JMSMsgLog(msg_id, msgContent);
					jmsMsgLog.jdbcSave();
					
					TradeRateSyncAction.doTradeRateSync(msgContent);
				}
				
				return Action.CommitMessage;
				
			}
		});
		consumer.start();
		log.info("~~~~~Consumer Started~~~~~");
	}
	
	private static class TradeRateSync implements Callable<Boolean> {

		public Message message;

		public TradeRateSync(Message message) {
			this.message = message;
		}

		@Override
		public Boolean call() throws Exception {
			String cacheKey = "jms_count_";
			try {
				int count = Cache.get(cacheKey) == null? 0 : (Integer)Cache.get(cacheKey);
				count += 1;
				Cache.set(cacheKey, count, "24h");
				log.info("~~~~~当前jms_count_次数：" + count + "~~~~~");
			} catch(Exception e) {
				log.error("key " + cacheKey + " 没有对应的缓存");
			}
			
			String msg_id = message.getMsgID();
			String msg_body = StringUtils.EMPTY;
			try {
				msg_body = new String(message.getBody(), "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			log.info("~~~~~Receive: " + message + "~~~~~msg_body: " + msg_body + "~~~~~");
			
			MsgContent msgContent = getMsgContent(msg_body);
			
			if(msgContent == null) {
				log.error("~~~~~解析msgContent失败！msg_body为： " + msg_body + "~~~~~");
				
				String errKey = "jms_error_";
				try {
					int count = Cache.get(errKey) == null? 0 : (Integer)Cache.get(errKey);
					count += 1;
					Cache.set(cacheKey, count, "72h");
					log.info("~~~~~当前jms_error_次数：" + count + "~~~~~");
				} catch(Exception e) {
					log.error("key " + cacheKey + " 没有对应的缓存");
				}
				
				return false;
			}
			
			// 储存jms消息日志
			JMSMsgLog jmsMsgLog = new JMSMsgLog(msg_id, msgContent);
			jmsMsgLog.jdbcSave();
			
			if("buyer".equalsIgnoreCase(msgContent.getRater())) {
				TradeRateSyncAction.doTradeRateSync(msgContent);
			}
			
			return true;
		}

	}
	
	private static MsgContent getMsgContent(String msg_body) {
		MsgContent msgContent = new MsgContent();
		try {
			JSONObject jsonObject = new JSONObject(msg_body);
			String contentString = jsonObject.getString("content");
			JSONObject content = new JSONObject(contentString);
			String buyerNick = content.getString("buyer_nick");
			String payment = content.getString("payment");
			String status = content.getString("status");
			Long oid = Long.parseLong(content.getString("oid"));
			String rater = content.getString("rater");
			Long tid = Long.parseLong(content.getString("tid"));
			String type = content.getString("type");
			String sellerNick = content.getString("seller_nick");
			
			msgContent = new MsgContent(buyerNick, payment, status, oid, rater, tid, type, sellerNick);
			
			return msgContent;
		} catch (JSONException e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
}
