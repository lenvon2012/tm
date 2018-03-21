package job;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import models.newCatPayHourDistribute;
import models.item.ItemPlay;
import models.order.OrderDisplay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import titleDiag.DiagResult;

import com.ciaosir.client.utils.DateUtil;
import com.ciaosir.client.utils.MapIterator;

import configs.TMConfigs;
import dao.item.ItemDao;
import dao.trade.OrderDisplayDao;

public class ItemCatOrderPayTimeDisTributeJob extends Job{

	static final Logger log = LoggerFactory.getLogger(ItemCatOrderPayTimeDisTributeJob.class);

	public static String TAG = "ItemCatOrderPayTimeDisTributeJob";
	
	public static int OrderDisplayIndex = 0;
	
	public static int count = 0;
	
    public static SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyyMMdd");
    
    public static Map<Long, newCatPayHourDistribute> cidPayTimeMap = new HashMap<Long, newCatPayHourDistribute>();
    
    public void doJob() {
        
        Thread.currentThread().setName(TAG);
         
        
        if (!TMConfigs.Server.jobTimerEnable) {
            return;
        }
        
        cidPayTimeMap.clear();
        
        new OrderDisplayDao.OrderDisplayBatchOper(256) {
            public List<OrderDisplay> findNext() {
                return OrderDisplayDao.findByOffsetLimit(offset, limit);
            }

            @Override
            public void doForEachOrder(final OrderDisplay order) {
                TMConfigs.getDiagResultPool().submit(new OrderCaller(order));
            }
        }.call();
        SaveCidPayHourDistributeMap(cidPayTimeMap);
    }
    
    public static void SaveCidPayHourDistributeMap(Map<Long, newCatPayHourDistribute> map) {
    	new MapIterator<Long, newCatPayHourDistribute>(map) {
            @Override
            public void execute(Entry<Long, newCatPayHourDistribute> entry) {
            	entry.getValue().jdbcSave();
            }
        }.call();
    }
    
    public static class OrderCaller implements Callable<DiagResult> {
    	OrderDisplay order;

        public OrderCaller(OrderDisplay order) {
            super();
            this.order = order;
        }

        @Override
        public DiagResult call() {
            try {
                doWithOrder(order);
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
            return null;
        }
        
        public void doWithOrder(final OrderDisplay order) {
        	Long numIid = order.getNumIid();
        	if(numIid == null || numIid <= 0) {
        		return;
        	}
        	
        	Long userId = order.getUserId();
        	if(userId == null || userId <= 0) {
        		return;
        	}
        	
        	Long payTime = order.getPayTime();
        	if(payTime == null || payTime <= 0) {
        		return;
        	}
        	
        	ItemPlay itemPlay = ItemDao.findByNumIid(userId, numIid);
        	if(itemPlay == null) {
        		return;
        	}
        	
        	Long cid = itemPlay.getCid();
        	if(cid == null || cid <= 0) {
        		return;
        	}
        	
        	int hour = (int) ((Math.floor(payTime / DateUtil.ONE_HOUR) + 8) % 24);
        	
        	newCatPayHourDistribute catDis = (newCatPayHourDistribute) cidPayTimeMap.get(cid);
        	if(catDis == null) {
        		catDis = newCatPayHourDistribute.findByCid(cid);
        		if(catDis == null) {
        			catDis = new newCatPayHourDistribute(cid);
        		}
        		cidPayTimeMap.put(cid, catDis);
        	}
        	Integer count = catDis.addByHour(hour);
        	log.info("cid = " + cid + " and hour = " + hour + " and count = " + count);
        }
    }
}

