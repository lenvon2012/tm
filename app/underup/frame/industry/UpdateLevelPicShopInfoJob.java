package underup.frame.industry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;

import com.ciaosir.client.CommonUtils;

import configs.TMConfigs;

//@OnApplicationStart
public class UpdateLevelPicShopInfoJob extends Job {
    private static final Logger log = LoggerFactory.getLogger(UpdateLevelPicShopInfoJob.class);

    @Override
    public void doJob() {
        long t1, t2;
        t1 = System.currentTimeMillis();
        List<String> wangwangs = CatTopSaleItemSQL.getAllShopWangWang();
        if (CommonUtils.isEmpty(wangwangs)) {
            log.error("-------------get the shop wangwang from cat_top_sale_item table failed......");
            return;
        }
        t2 = System.currentTimeMillis();
        log.info("----------------get the wangwang from datbase need the time is " + (t2 - t1)
                + " and the size of wangwangs is " + wangwangs.size());
        //need to delete
        List<String> wangwangTemp = new ArrayList<String>();
        wangwangTemp = LevelPicShopInfo.getAllWangwang();
        for(String wang : wangwangTemp){
            if(wangwangTemp.contains(wang))
                wangwangs.remove(wang);
        }
        t1 = System.currentTimeMillis();
        log.info("需要移除的个数是:" + wangwangTemp.size() + " need the time is " + (t1 - t2) + " and after the remove the size is " + wangwangs.size());
        // 批量插入
        int size = wangwangs.size();
        for (int i = 0; i < size; ) {
            List<LevelPicShopInfo> shopInfos = new ArrayList<LevelPicShopInfo>();
            shopInfos.clear();
            t1 = System.currentTimeMillis();
            int count = 0;
            List<String> wangs = new ArrayList<String>();
            for(; count < 100 && i < size; count++, ++i){
                wangs.add(wangwangs.get(i));
            }
            shopInfos = getShopInfos(wangs);
            t2 = System.currentTimeMillis();
            log.info("从服务器获取100个需要的时间是:" + (t2 - t1));
            if (CommonUtils.isEmpty(shopInfos)) {
                continue;
            }
            LevelPicShopInfo.insertPatch(shopInfos);
            t1 = System.currentTimeMillis();
            log.info("批量插入的时间是:" + (t1 - t2));
        }
    }
    
//    BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<Runnable>(128);
//
//    RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();
//
//    ThreadPoolExecutor executorService = new ThreadPoolExecutor(20, 32, 0L,
//            TimeUnit.MILLISECONDS, blockingQueue, rejectedExecutionHandler);
    
    public List<LevelPicShopInfo>  getShopInfos(List<String> wangwangs){
    	ThreadPoolExecutor shopInfoPool = TMConfigs.getShopInfoPool();
    	
        List<LevelPicShopInfo> res = new ArrayList<LevelPicShopInfo>();
        List<Future<LevelPicShopInfo>> fus = new ArrayList<Future<LevelPicShopInfo>>();
        for (int i = 0; i < wangwangs.size(); i++) {
            Future<LevelPicShopInfo> fu = shopInfoPool.submit(new ShopInfosThread(wangwangs.get(i)));
            if(fu != null){
                fus.add(fu);
            }
        }
        for(Future<LevelPicShopInfo> fu:fus){
            try {
                res.add(fu.get());
            } catch (InterruptedException e) {
                log.info("error1");
            } catch (ExecutionException e) {
                log.info("error2");
            }
        }
        return res;
    }
}
