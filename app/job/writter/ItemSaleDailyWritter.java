//package job.writter;
//
//import java.util.List;
//import java.util.Queue;
//import java.util.concurrent.ConcurrentLinkedQueue;
//
//import message.itemsaledaily.IItemSaleDailyMessage;
//import message.itemsaledaily.ItemSaleDailyMessage;
//import models.item.ItemSaleDaily;
//import models.updatetimestamp.updatets.process.RefundProcessUpdateTs;
//import models.updatetimestamp.updatets.process.TradeProcessUpdateTs;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import play.jobs.Every;
//import play.jobs.Job;
//import transaction.JDBCBuilder;
//import utils.DateUtil;
//
//import com.taobao.api.domain.Item;
//
//import dao.ItemSaleDailyDao;
//
//@Every("15s")
//public class ItemSaleDailyWritter extends Job {
//
//	private static final Logger log = LoggerFactory
//			.getLogger(ItemSaleDailyWritter.class);
//
//	public static final String TAG = "ItemSaleDailyWritter";
//
//	public static final Queue<IItemSaleDailyMessage> msgToWritten = new ConcurrentLinkedQueue<IItemSaleDailyMessage>();
//
//	public static String statusMessage;
//
//	public static void addObject(IItemSaleDailyMessage itemSaleDailyMsg) {
//		msgToWritten.add(itemSaleDailyMsg);
//	}
//
//	@Override
//	public void doJob() {
//
//		Thread.currentThread().setName(TAG);
//
//		IItemSaleDailyMessage msg = null;
//
//		while ((msg = msgToWritten.poll()) != null) {
//			doInsert(msg);
//		}
//	}
//
//	public void doInsert(IItemSaleDailyMessage msg) {
//
//		if (msg.isFinished()) {
//			afterFinished(msg);
//		} else {
//			ItemSaleDailyDao.jdbcSaveMessage(msg);
//		}
//	}
//
//	public void afterFinished(IItemSaleDailyMessage msg) {
//		switch (msg.getTag()) {
//		case ItemSaleDailyMessage.TAG.ALIPAY:
//			TradeProcessUpdateTs.updateLastModifedTime(msg.getUserId(),
//					msg.getTs());
//			break;
//		case ItemSaleDailyMessage.TAG.REFUND:
//			RefundProcessUpdateTs.updateLastModifedTime(msg.getUserId(),
//					msg.getTs());
//			break;
//		}
//	}
//
//}
