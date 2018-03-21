
package job.click;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import models.popularized.Popularized;

import org.elasticsearch.common.util.concurrent.jsr166y.ConcurrentLinkedDeque;

import play.cache.Cache;
import dao.popularized.PopularizedDao;

public class toClickItems {

    public static Queue<Popularized> genItemsToClick() {

        Queue<Popularized> items = new ConcurrentLinkedDeque<Popularized>();

        // get all Popularized items with cache
        List<Popularized> popularizeds = new ArrayList<Popularized>();
        popularizeds = (List<Popularized>) Cache.get("popularizeds");
        if (popularizeds == null || popularizeds.size() == 0) {
            popularizeds = Popularized.findAllPopularizeds();
            Cache.set("popularizeds", popularizeds, "24h");
        }
        // (numIid + random)%7 == 0?推广:不推广 
        // 假设一天推广的时间段为早上8点到晚上12点，共16小时； 每个被推广的宝贝一天平均点击30次，则平均每小时点击两次，半小时点击一次
        // job每个5分钟执行一次，则对于一个指定的宝贝numIid，平均6次job执行中能点击到该宝贝即可
        // 每一次job执行的时候，随机选择七分之一的宝贝进行推广    
        for (Popularized p : popularizeds) {
            if ((p.getNumIid() + (Math.floor((Math.random() * 13)))) % 3 == 0) {
                items.add(p);
            }
        }
        return items;
    }

    public static Queue<ItemNum> getItemNums(int offset, int limit, int status) {
//        Queue<ItemNum> items = new ConcurrentLinkedDeque<ItemNum>();
        Queue<ItemNum> items  = PopularizedDao.getItemNums(limit, offset, status);
        return items;
    }
    
    public static Queue<String> moreClickNums(int offset, int limit) {
    	Queue<String> nicks  = PopularizedDao.moreItemNums(limit, offset);
        return nicks;
    }
}
