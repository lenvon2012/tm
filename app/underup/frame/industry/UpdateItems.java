package underup.frame.industry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import spider.mainsearch.MainSearchApi;
import spider.mainsearch.MainSearchApi.TBSearchRes;
import configs.TMConfigs;

/*
 * 从网页上根据前台cid按照销量爬取100页宝贝并存入数据库，以供使用
 * 每天中午更新数据
 */
public class UpdateItems extends Job{
    
    private static final Logger log = LoggerFactory.getLogger(UpdateItems.class);
    
    @Override
    public void doJob(){
        List<Long> frontCids = FrontCats.getAllCats();
        List<Long> rmCids = CatTopSaleItemSQL.getFrontCid();
        if(rmCids != null){
            for(long rmCid: rmCids){
                frontCids.remove(rmCid);
            }
        }
        log.info("------------------------------------front cid 的个数是---" + frontCids.size());
        int count = 0;
        for (Long frontCid : frontCids) {
            log.info("------------------------------------现在爬取的frontcid是:" + frontCid);
            long tempEnd = System.currentTimeMillis();
            // 多线程爬取数据
//          List<TBSearchRes> res = getTBSearchRes(100, "sale-desc", frontCid);
            List<TBSearchRes> res = getTBSearchResNew(100, "sale-desc", frontCid);
            long tempStart = System.currentTimeMillis();
            log.info("------------------------------------爬取100个页面所需要的时间：" + (tempStart - tempEnd));
            CatTopSaleItemInsert.addObject(res);
            // 该job 第一次运行  来启动CatTopSaleItemInsert 这个job 
            if(count == 0){
            	ExecutorService exec = Executors.newFixedThreadPool(1);  
            	exec.execute(new CatTopSaleItemInsert());  
            }
            count++;
        }
    }
    
    private List<TBSearchRes> getTBSearchResNew(int pageNum, String orderType, Long frontCid) {
        List<TBSearchRes> res = new ArrayList<TBSearchRes>();
        for (int i = 1; i <= pageNum; i++) {
            TBSearchRes search = MainSearchApi.search(StringUtils.EMPTY, i, orderType, frontCid);
            if(search == null){
                continue;
            }
            search.setFrontCid(frontCid);
            res.add(search);
        }
        return res;
    }

    //根据前端cid得到满足条件（综合、人气、销量、信用、最新、价格）个数（需要几页,一页=40）的宝贝
//    BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<Runnable>(128);
//
//    RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();
//
//    ThreadPoolExecutor executorService = new ThreadPoolExecutor(10, 32, 0L, TimeUnit.MILLISECONDS, blockingQueue, rejectedExecutionHandler);
   
    public List<TBSearchRes> getTBSearchRes(int pageNum, String orderType, Long frontCid) {
    	ThreadPoolExecutor pageSearchPool = TMConfigs.getPageSearchPool();
    	
        List<TBSearchRes> res = new ArrayList<TBSearchRes>();
        List<Future<TBSearchRes>> fus = new ArrayList<Future<TBSearchRes>>();
        for (int i = 1; i <= pageNum; i++) {
            Future<TBSearchRes> fu = pageSearchPool.submit(new SearchPages(i, orderType, frontCid));
            if(fu != null){
                fus.add(fu);
            }
        }
        for(Future<TBSearchRes> fu:fus){
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
    
    public List<TBSearchRes> getTBSearchRes(String host,int pageNum, String orderType, Long frontCid) {
    	ThreadPoolExecutor pageSearchPool = TMConfigs.getPageSearchPool();
    	
        List<TBSearchRes> res = new ArrayList<TBSearchRes>();
        List<Future<TBSearchRes>> fus = new ArrayList<Future<TBSearchRes>>();
        for (int i = 1; i <= pageNum; i++) {
            Future<TBSearchRes> fu = pageSearchPool.submit(new SearchPages(host,i, orderType, frontCid));
            if(fu != null){
                fus.add(fu);
            }
        }
        for(Future<TBSearchRes> fu:fus){
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
