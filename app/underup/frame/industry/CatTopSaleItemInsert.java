package underup.frame.industry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.FutureTask;

import job.ApplicationStopJob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spider.mainsearch.MainSearchApi.TBSearchRes;
import actions.ItemGetAction;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.PYFutureTaskPool;
import com.ciaosir.client.pojo.ItemThumb;
import com.taobao.api.domain.Item;

public class CatTopSaleItemInsert implements Runnable {
    
    private static final Logger log = LoggerFactory.getLogger(CatTopSaleItemInsert.class);
    
    private static Queue<List<TBSearchRes>> searchResQueue = new ConcurrentLinkedQueue<List<TBSearchRes>>();
    
    static PYFutureTaskPool<Item> diagResultPool;
    
    @Override
    public void run() {
    	Long lastFrontCid = FrontCats.getLastFrontCid();
		List<TBSearchRes> res = null;
		while(true){
			if((res = searchResQueue.poll()) != null){
				Long frontCid = getItem(res);
				if (frontCid == lastFrontCid) {
					new UpdateItemsCatJob().doJob();
					break;
				}	
			}
			continue;
		}
    }

    private Long getItem(List<TBSearchRes> res){
        Long frontCid = -1L;
        if(CommonUtils.isEmpty(res)){
            return frontCid;
        }
        for (TBSearchRes searchRes : res) {
            frontCid = searchRes.getFrontCid();
            List<ItemThumb> itemThumbs = searchRes.getItems();
            if (CommonUtils.isEmpty(itemThumbs)) {
                log.info("------------------------------------ItemThumb获取出错");
                continue;
            }
            Map<Long, Item> map = new HashMap<Long, Item>();

            List<FutureTask<Item>> list = new ArrayList<FutureTask<Item>>(itemThumbs.size());
            for (ItemThumb itemThumb : itemThumbs) {
                if(itemThumb == null) {
                    continue;
                }
                final Long id = itemThumb.getId();
                CommonUtils.sleepQuietly(95);
                list.add(getDiagResultPool().submit(new Callable<Item>() {

                    @Override
                    public Item call() throws Exception {
//                      Item item = ItemGetAction.getSimpleItem(id);
                        Item item = ItemGetAction.getSimpleItemNew(id);
//                        log.info("getSimpleItem OK for id = " + id);
                        return item;
                    }

                }));
            }

            for (FutureTask<Item> task : list) {
                try {
                    Item diagResult = task.get();
                    if (diagResult != null) {
                        final Long id = diagResult.getNumIid();
                        Item item = map.get(id);
                        if(item != null) {
                            continue;
                        }
                        map.put(id, diagResult);
                    }
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                }
            }
            if (map == null || map.size() == 0 || map.size() != itemThumbs.size()) {
                log.error("------------------------------------服务器获取数据失败");
                continue;
            }
            CatTopSaleItemSQL.insertPatch(frontCid, itemThumbs, map);
            log.info("------------------------------------frontCid is " + frontCid);
        }
        return frontCid;
    }
    
    public PYFutureTaskPool<Item> getDiagResultPool() {
        if (diagResultPool == null) {
            diagResultPool = new PYFutureTaskPool<Item>(256);
            ApplicationStopJob.addShutdownPool(diagResultPool);
        }
        return diagResultPool;
    }
    
    public static void addObject(List<TBSearchRes> searchRes){
        searchResQueue.add(searchRes); 
    }

}
