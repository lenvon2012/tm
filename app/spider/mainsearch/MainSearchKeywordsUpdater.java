
package spider.mainsearch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import play.jobs.Job;
import spider.ItemThumbSecond;
import spider.mainsearch.MainSearchApi.MainSearchParams;
import spider.mainsearch.MainSearchApi.TBSearchRes;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.ItemThumb;

public class MainSearchKeywordsUpdater extends Job {

    private static final Logger log = LoggerFactory.getLogger(MainSearchKeywordsUpdater.class);

    public static final String TAG = "MainSearchKeywordsUpdater";

    public static TBSearchRes queryForPage(MainSearchParams params) {
        TBSearchRes res = MainSearchApi.search(params);
        if (CommonUtils.isEmpty(res.getItems())) {
            // no items..
            log.error("NO RESULT: " + params.toString() + " with res :" + res);
            return null;
        }
        return res;
    }

    public static class MainSearchPageCaller implements Callable<TBSearchRes> {


        MainSearchParams params;

        public MainSearchPageCaller(MainSearchParams params) {
            super();
            this.params = params;
        }

        @Override
        public TBSearchRes call() throws Exception {
            // log.error("ms doing: " + word);
            TBSearchRes res = queryForPage(params);
            return res;
        }

        @Override
        public String toString() {
            return "MainSearchPageCaller [params=" + params + "]";
        }

    }

    public static class MainSearchItemRank implements Serializable {




        @JsonProperty
        protected String picPath;

        // 在该关键词的排名名次
        @JsonProperty
        protected int rank;

        // 直通车推广关键词
        @JsonProperty
        protected long keywordId;

        // 地区
        @JsonProperty
        protected String area;

        // 物品id
        @JsonProperty
        protected long numIid;

        // 时间戳
        @JsonProperty
        protected long ts;

        // 创意标题
        @JsonProperty
        protected String title;

        @JsonProperty
        protected long sellerId;

        @JsonProperty
        protected String wangwangId;

        @JsonProperty
        protected int salesCount;//销量

        @JsonProperty
        protected String keyword;

        @JsonProperty
        protected int price;
        
        @JsonProperty
        protected String mobileUrl;
        
        @JsonProperty
        protected String nick;

        private String dt;

        private String delistTimestamp;

        public MainSearchItemRank(ItemThumb thumb, int rank, String word) {
            this.numIid = thumb.getId();
            this.sellerId = thumb.getSellerId();
            this.rank = rank;
            this.keyword = word;
            this.title = thumb.getFullTitle();
            this.picPath = thumb.getPicPath();
            this.salesCount = thumb.getTradeNum();
            this.price = thumb.getPrice();
            this.ts = System.currentTimeMillis();
        }
        public MainSearchItemRank(ItemThumb thumb, int rank, String word, String area) {
            this.numIid = thumb.getId();
            this.sellerId = thumb.getSellerId();
            this.rank = rank;
            this.keyword = word;
            this.title = thumb.getFullTitle();
            this.picPath = thumb.getPicPath();
            this.salesCount = thumb.getTradeNum();
            this.price = thumb.getPrice();
            this.ts = System.currentTimeMillis();
            this.area = area;
        }

        public MainSearchItemRank(String picPath, int rank, long keywordId,
				String area, long numIid, long ts, String title, long sellerId,
				String wangwangId, int salesCount, String keyword, int price) {
			super();
			this.picPath = picPath;
			this.rank = rank;
			this.keywordId = keywordId;
			this.area = area;
			this.numIid = numIid;
			this.ts = ts;
			this.title = title;
			this.sellerId = sellerId;
			this.wangwangId = wangwangId;
			this.salesCount = salesCount;
			this.keyword = keyword;
			this.price = price;
		}
        
        public MainSearchItemRank(String picPath, int rank, String mobileUrl,
				long numIid, String title, String keyword) {
			super();
			this.picPath = picPath;
			this.rank = rank;
			this.mobileUrl = mobileUrl;
			this.numIid = numIid;
			this.title = title;
			this.keyword = keyword;
		}
        
        public MainSearchItemRank(ItemThumbSecond thumb, int rank) {
            this.numIid = thumb.getId();
            this.rank = rank;
            this.title = thumb.getFullTitle();
            this.picPath = thumb.getPicPath();
            this.salesCount = thumb.getTradeNum();
            this.price = thumb.getPrice();
            this.ts = System.currentTimeMillis();
            this.dt = thumb.getdelistTimes();
            this.delistTimestamp = thumb.getdelistTimestamp();
        }
        
        public MainSearchItemRank(String picPath, int rank, String mobileUrl,
				long numIid, String title, String keyword, String nick) {
			super();
			this.picPath = picPath;
			this.rank = rank;
			this.mobileUrl = mobileUrl;
			this.numIid = numIid;
			this.title = title;
			this.keyword = keyword;
			this.nick = nick;
		}
        
        public String getDt() {
            return dt;
        }
        public void setDt(String dt) {
            this.dt = dt;
        }
        public String getdelistTimestamp() {
            return delistTimestamp;
        }
        public void setdelistTimestamp(String delistTimestamp) {
            this.delistTimestamp = delistTimestamp;
        }

		public String getPicPath() {
            return picPath;
        }

        public void setPicPath(String picPath) {
            this.picPath = picPath;
        }

        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }

        public String getNick() {
			return nick;
		}
		public void setNick(String nick) {
			this.nick = nick;
		}
		public long getKeywordId() {
            return keywordId;
        }

        public void setKeywordId(long keywordId) {
            this.keywordId = keywordId;
        }

        public String getArea() {
            return area;
        }

        public void setArea(String area) {
            this.area = area;
        }

        public long getNumIid() {
            return numIid;
        }

        public void setNumIid(long numIid) {
            this.numIid = numIid;
        }

        public long getTs() {
            return ts;
        }

        public void setTs(long ts) {
            this.ts = ts;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public long getSellerId() {
            return sellerId;
        }

        public void setSellerId(long sellerId) {
            this.sellerId = sellerId;
        }

        public String getWangwangId() {
            return wangwangId;
        }

        public void setWangwangId(String wangwangId) {
            this.wangwangId = wangwangId;
        }

        public String getKeyword() {
            return keyword;
        }

        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }

        public int getSalesCount() {
            return salesCount;
        }

        public void setSalesCount(int salesCount) {
            this.salesCount = salesCount;
        }

        public int getPrice() {
            return price;
        }

        public void setPrice(int price) {
            this.price = price;
        }

    }
    
    public static class ForSearchAreaCaller implements Callable<HashMap<Long, MainSearchItemRank>> {

        MainSearchParams params;

        public ForSearchAreaCaller(MainSearchParams params) {
            super();
            this.params = params;
        }
        
        @Override
        public HashMap<Long, MainSearchItemRank> call() throws Exception {
            // log.error("ms doing: " + word);
            HashMap<Long, MainSearchItemRank> map = MainSearchKeywordsUpdater.doSearch(params);
            return map;
        }
        
        @Override
        public String toString() {
            return "forSearchAreaCaller  [params=" + params + "]";
        }

    }
    
    public static HashMap<Long, MainSearchItemRank> doSearch(MainSearchParams params) {


        String key = MainSearchCache.genCacheKey(params);
        HashMap<Long, MainSearchItemRank> map = MainSearchCache.getMainSearchFromCache(key);
        if (!CommonUtils.isEmpty(map)) {
            return map;
        }

        int pages = params.getPageNum();
        String word = params.getWord();
        String queryArea = params.getQueryArea();

        ExecutorService threadPool = Executors.newCachedThreadPool();
        CompletionService<TBSearchRes> cs = new ExecutorCompletionService<TBSearchRes>(threadPool);
        for (int i = 1; i <= pages; i++) {
            cs.submit(new MainSearchPageCaller(new MainSearchParams(params, i)));
        }

        map = new HashMap<Long, MainSearchItemRank>();

        for (int i = 1; i <= pages; i++) {
            try {
                TBSearchRes res = cs.take().get();

                if (res == null || CommonUtils.isEmpty(res.getItems())) {
                    continue;
                }
                List<ItemThumb> items = res.getItems();
                int size = items.size();
                for (int j = 0; j < size; j++) {
                    int rank = (res.getCurPage() - 1) * res.getPageSize() + j + 1;
                    ItemThumb thumb = items.get(j);
                    MainSearchItemRank rankbase = new MainSearchItemRank(thumb, rank, word,queryArea);
                    map.put(thumb.getId(), rankbase);
                }
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            } catch (ExecutionException e) {
                log.error(e.getMessage(), e);
            }
        }

        MainSearchCache.putIntoCache(key, map);
        return map;

    }

    public static class MainSearchCache {


        public static String genCacheKey(MainSearchParams params) {


            String prefix = "_MainSearch_";
            String word = params.getWord();
            if (StringUtils.isBlank(word)) {
                word = StringUtils.EMPTY;
            } else {
                word = word.replaceAll("\\s", "_");
            }
            String queryArea = params.getQueryArea();
            if (StringUtils.isEmpty(queryArea) || "默认".equals(queryArea) || "北京".equals(queryArea)) {
                queryArea = "默认";
            }

            String tag = prefix + word + "_" + params.getOrder() + "_" + params.getPageNum() + "_"
                    + params.getMinPrice() + "_" + params.getMaxPrice() +"_" + queryArea;
            log.info("[MainSearchCache key:]" + tag);

            return tag;
        }
        
        public static String genMobileCacheKey(MainSearchParams params) {
            String prefix = "_MobileRank_";
            String word = params.getWord();
            if (StringUtils.isBlank(word)) {
                word = StringUtils.EMPTY;
            } else {
                word = word.replaceAll("\\s", "_");
            }

            String tag = prefix + word + "_" + params.getPageNum() + "_";
            log.info("[genMobileCacheKey key:]" + tag);

            return tag;
        }

        public static void putIntoMobileCache(String key, List<MainSearchItemRank> ranks) {
            if (StringUtils.isEmpty(key)) {
                return;
            }
            if (CommonUtils.isEmpty(ranks)) {
                return;
            }
            try {
                Cache.set(key, ranks, "1min");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
     
        
        
        public static void putIntoCache(String key, HashMap<Long, MainSearchItemRank> map) {
            if (StringUtils.isEmpty(key)) {
                return;
            }
            if (CommonUtils.isEmpty(map)) {
                return;
            }
            try {
                Cache.set(key, map, "1h");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        
        public static void putIntoCache(String key, Map<Long, MainSearchItemRank> map) {
            if (StringUtils.isEmpty(key)) {
                return;
            }
            if (CommonUtils.isEmpty(map)) {
                return;
            }
            try {
                Cache.set(key, map, "1h");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        public static HashMap<Long, MainSearchItemRank> getMainSearchFromCache(String key) {
            if (StringUtils.isEmpty(key)) {
                return null;
            }
            try {
                Object cached = Cache.get(key);
                if (cached == null) {
                    return null;
                }
                HashMap<Long, MainSearchItemRank> map = (HashMap<Long, MainSearchItemRank>) cached;
                return map;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            return null;
        }
        
        public static List<MainSearchItemRank> getMobileRankFromCache(String key) {
            if (StringUtils.isEmpty(key)) {
                return null;
            }
            try {
                Object cached = Cache.get(key);
                if (cached == null) {
                    return null;
                }
                List<MainSearchItemRank> ranks = (List<MainSearchItemRank>) cached;
                return ranks;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            return null;
        }
    }

    public static void main(String[] args) throws Exception {
        MainSearchParams params = new MainSearchParams("女装 毛衣", 1, "sale-desc");
        TBSearchRes res = new MainSearchPageCaller(params).call();
        System.out.println(res);
        System.out.println(res.getItems());
    }
}
