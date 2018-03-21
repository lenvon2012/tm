
package bustbapi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.lucene.util.ArrayUtil;
import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.cache.Cache;
import bustbapi.TBApi.WordBaseGet;
import bustbapi.TMApi.BusWordBaseGetApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.Validator;
import com.ciaosir.client.api.API;
import com.ciaosir.client.api.BusAPIs;
import com.ciaosir.client.pojo.IWordBase;
import com.ciaosir.client.pojo.WordBaseBean;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.NumberUtil;
import com.ciaosir.commons.ClientException;
import com.google.gson.Gson;
import com.mchange.v1.util.ArrayUtils;
import com.taobao.api.ApiException;
import com.taobao.api.TaobaoClient;
import com.taobao.api.domain.INRecordBase;
import com.taobao.api.domain.INWordBase;
import com.taobao.api.domain.INWordCategory;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.Shop;
import com.taobao.api.request.SimbaInsightWordscatsGetRequest;
import com.taobao.api.response.SimbaInsightWordscatsGetResponse;

public class BusAPI {

    private static final Logger log = LoggerFactory.getLogger(DirectBusApi.class);

    public static final String TAG = "DirectBusApi";

    static String DURATION_CACHE_HOUR = "168h";

    // static String TEMP_SID =
    // "6100b2281aa7770f85b0f2d8b593da1926fad1e3723e13279742176";
    // static String TEMP_SID =
    // "610161250cdaa1a3015b1a0a8a4fd31efa28054768bdc1479742176";
    /**
     * just_001
     */
    static String TEMP_SID = "6102b083ea1411ec108c93ecfedf35fc111be0f670e9d9641353865";

    /**
     * {"simba_insight_catsforecast_get_response":{"in_category_tops":{
     * "i_n_category_top"
     * :[{"category_child_top_list":{"i_n_category_child_top":[{"category_desc":
     * "女装\/女士精品>连衣裙","category_id":50010850,"category_name":"女装\/女士精品>连衣裙","category_properties_list":{}},{"category_desc":"女装\/女士精品","category_id":16,"category_name":"女装\/女士精品","category_properties_list":{}},{"category_desc":"童装\/童鞋\/亲子装","category_id":50008165,"category_name":"童装\/童鞋\/亲子装","category_properties_list":{}},{"category_desc":"女士内衣\/男士内衣\/家居服","category_id":1625,"category_name":"女士内衣\/男士内衣\/家居服","category_properties_list":{}}]}}]}
     * } }
     * 
     * @param word
     * @return
     */
    // public static Map<String, Long> getCatPvMap(String word) {
    // TaobaoClient client = TBApi.genBusClient();
    // try {
    // Map<String, Long> catPv = new HashMap<String, Long>();
    // Map<Long, String> cidName = new HashMap<Long, String>();
    // buildCatIdNameMap(word, client, cidName);
    // buildCatPv(word, client, catPv, cidName);
    // return catPv;
    // } catch (ApiException e) {
    // log.warn(e.getMessage(), e);
    // }
    // return MapUtils.EMPTY_MAP;
    // }

    static int MAX_WID_NUM = 200;

    static String urlBase = "http://chedao.taovgo.com/";

    static String BUS_CACHE_KEY = "BusAPIWordPV";

    public static Map<String, WordBaseBean> wordPv(Collection<String> words) throws ApiException, ClientException {

        if (CommonUtils.isEmpty(words)) {
            return MapUtils.EMPTY_MAP;
        }

        int cache = 7 + (int) (System.currentTimeMillis() % 8L);
        return new BusWordBaseGetApi(words, 7, cache, false).execute();

        // final Set<String> todoWord = new HashSet<String>();
        // Map<String, WordBaseBean> finalRes = new HashMap<String,
        // WordBaseBean>();
        // for (String string : words) {
        // try {
        // String cacheKey = BUS_CACHE_KEY + string;
        // log.info("[cache key:]" + cacheKey);
        // WordBaseBean pv = null;
        // Object cacheObj = Cache.get(cacheKey);
        //
        // if (cacheObj instanceof Long) {
        // pv = new WordBaseBean(string, -1);
        // finalRes.put(string, pv);
        // continue;
        // }
        //
        // pv = (WordBaseBean) cacheObj;
        // if (pv != null) {
        // finalRes.put(string, pv);
        // } else {
        // todoWord.add(string);
        // }
        // } catch (Exception e) {
        // log.warn(e.getMessage(), e);
        //
        // }
        // }
        //
        // log.warn("[cache not hit:]" + StringUtils.join(todoWord, ','));
        //
        // Map<String, WordBaseBean> toDoWordPv = bases(todoWord);
        // new MapIterator<String, WordBaseBean>(toDoWordPv) {
        // @Override
        // public void execute(Entry<String, WordBaseBean> entry) {
        // String cacheKey = BUS_CACHE_KEY + entry.getKey();
        // WordBaseBean pv = (WordBaseBean) Cache.get(cacheKey);
        // if (pv != null) {
        // Cache.set(BUS_CACHE_KEY + entry.getKey(), pv, DURATION_CACHE_HOUR);
        // }
        // todoWord.remove(entry.getKey());
        // }
        // }.call();
        // finalRes.putAll(toDoWordPv);
        // for (String noResWord : todoWord) {
        // String cacheKey = BUS_CACHE_KEY + noResWord;
        // Cache.set(cacheKey, new WordBaseBean(noResWord, -1),
        // DURATION_CACHE_HOUR);
        // }
        //
        // return finalRes;
    }

    public static Map<String, WordBaseBean> bases(Collection<String> words) throws ApiException {
        List<INWordBase> inWordBases = new WordBaseGet(TEMP_SID, words).call();
        if (CommonUtils.isEmpty(inWordBases)) {
            return MapUtils.EMPTY_MAP;
        }

        Map<String, WordBaseBean> map = new HashMap<String, WordBaseBean>();
        for (INWordBase inWordBase : inWordBases) {
            String word = inWordBase.getWord();
            List<INRecordBase> inBases = inWordBase.getInRecordBaseList();
            WordBaseBean bean = new WordBaseBean();
            bean.setWord(word);
            bean.updateByINInfo(NumberUtil.sum(inBases));
            log.info("[bean:]" + bean);
            map.put(word, bean);
        }

        log.info("[map :]" + map);

        return map;
    }

    static String CatClickTAG = "_CatClick";

    @Deprecated
    public static Map<String, Long> buildCatClickCached(Collection<String> colls, Long cid) throws ApiException {
        String thisTag = CatClickTAG;
        if (CommonUtils.isEmpty(colls)) {
            return MapUtils.EMPTY_MAP;
        }

        Map<String, Long> finalRes = new HashMap<String, Long>();
        Set<String> todoWords = new HashSet<String>();

        for (String word : colls) {
            if (StringUtils.isBlank(word)) {
                continue;
            }

            String trimed = word.replace(" ", StringUtils.EMPTY);
            String cacheKey = String.format("%s%d%s", thisTag, cid, trimed);
            Long pv = (Long) Cache.get(cacheKey);
            if (pv == null) {
                // log.info("[add todo word:]" + word);
                if (todoWords.contains(" ")) {
                    // continue;
                } else {
                    todoWords.add(word);
                }
            } else {
                finalRes.put(word, pv);
            }
        }

        if (CommonUtils.isEmpty(todoWords)) {
            return finalRes;
        }
        log.warn("not hit words :" + StringUtils.join(todoWords, ','));

        TaobaoClient client = TBApi.genBusClient();
        SimbaInsightWordscatsGetRequest req = new SimbaInsightWordscatsGetRequest();
        List<String> wordCat = new ArrayList<String>();
        for (String string : todoWords) {
            wordCat.add(string + "^^" + cid);
        }
        req.setWordCategories(StringUtils.join(wordCat, ','));
        req.setFilter("PV");
        SimbaInsightWordscatsGetResponse response = client.execute(req, TEMP_SID);
        List<INWordCategory> list = response.getInWordCategories();
        if (CommonUtils.isEmpty(list)) {
            return finalRes;
        }

        // Map<String, Long> res = new HashMap<String, Long>();
        for (INWordCategory catInfo : list) {
            Long pv = catInfo.getPv();
            if (NumberUtil.isNullOrZero(pv)) {
                pv = -1L;
            }
            String trimed = catInfo.getWord().replace(" ", StringUtils.EMPTY);
            String cacheKey = String.format("%s%d%s", thisTag, cid, trimed);
            // log.info("set cache:" + cacheKey);
            Cache.set(cacheKey, pv, DURATION_CACHE_HOUR);
            todoWords.remove(trimed);
        }

        for (String noResWrod : todoWords) {
            String cacheKey = String.format("%s%d%s", thisTag, cid, noResWrod);
            // log.info("set cache:" + cacheKey);
            Cache.set(cacheKey, -1L, DURATION_CACHE_HOUR);
        }

        return finalRes;
    }

    public static class WordBusInfoApi extends BusAPIs<Map<String, IWordBase>> {

        static String CHEDAO_URL = "http://chedao.taovgo.com";

        public WordBusInfoApi(Collection<String> words) {
            this(CHEDAO_URL, words);
        }

        public WordBusInfoApi(String[] words) {
            this(CHEDAO_URL, words);
        }

        public WordBusInfoApi(String url, String[] words) {
            super(url);
            urlBuilder.appendParam(PARAM_WORDS, StringUtils.join(words, ','));
        }

        public WordBusInfoApi(String url, Collection<String> words) {
            super(url);
            urlBuilder.appendParam(PARAM_WORDS, StringUtils.join(words, ','));
        }

        @Override
        public Validator<Map<String, IWordBase>> getValidator() {
            return BaseInfoToWordBaseValidator;
        }

        @Override
        public String getActions() {
            return "/SubwayWord/base";
        }

        @Override
        public boolean isMethodGet() {
            return false;
        }
    }

    public static Validator<Map<String, IWordBase>> BaseInfoToWordBaseValidator = new Validator<Map<String, IWordBase>>() {
        @Override
        public Map<String, IWordBase> validate(HttpResponse resp) throws ClientException {
            super.validate(resp);

            JsonNode node = JsonNodeValidator.validate(resp);

            Map<String, IWordBase> result = new HashMap<String, IWordBase>();
            Iterator<Entry<String, JsonNode>> fields = node.getFields();
            while (fields.hasNext()) {
                Entry<String, JsonNode> entry = (Entry<String, JsonNode>) fields.next();

                INWordBase base = JsonUtil.toObject(entry.getValue(), INWordBase.class);
                if (base == null) {
                    continue;
                }

                List<INRecordBase> list = base.getInRecordBaseList();
                if (CommonUtils.isEmpty(list)) {
                    continue;
                }
                INRecordBase sum = NumberUtil.sum(list);
                WordBaseBean bean = new WordBaseBean(entry.getKey(), sum.getPv().intValue());
                bean.updateByINInfo(sum);
                result.put(entry.getKey(), bean);
            }

            return result;
        }
    };

    static String autoTitleHost = Play.mode.isDev() ? "http://x.taovgo.com" : "http://10.128.1.83:9002";

    public static class SingleShopApi extends BusAPIs<Shop> {
        public SingleShopApi(String wangwang) {
            super(autoTitleHost);
            urlBuilder.appendParam(API.PARAM_USERNAME, wangwang);
        }

        Validator<Shop> validator = null;

        @Override
        public Validator<Shop> getValidator() {
            if (validator != null) {
                return validator;
            }
            validator = new Validator<Shop>() {
                @Override
                public Shop validate(HttpResponse resp) throws ClientException {
                    String node = StringValidator.validate(resp);
                    return new Gson().fromJson(node, Shop.class);
                }

            };
            return validator;
        }

        @Override
        public String getActions() {
            return "/free/singleShop";
        }

        @Override
        public boolean isMethodGet() {
            return false;
        }
    }

    public static class MultiItemApi extends BusAPIs<Map<Long, Item>> {
        public MultiItemApi(Collection<Long> ids) {
            super(autoTitleHost);
            urlBuilder.appendParam("ids", StringUtils.join(ids, ','));
        }

        Validator<Map<Long, Item>> validator = null;

        @Override
        public Validator<Map<Long, Item>> getValidator() {
            if (validator != null) {
                return validator;
            }
            validator = new Validator<Map<Long, Item>>() {
                @Override
                public Map<Long, Item> validate(HttpResponse resp) throws ClientException {
                    Map<Long, Item> idItemMap = new HashMap<Long, Item>();
                    String node = StringValidator.validate(resp);
                    Item[] items = new Gson().fromJson(node, Item[].class);
                    for (Item item : items) {
                        idItemMap.put(item.getNumIid(), item);
                    }
                    return idItemMap;
                }

            };
            return validator;
        }

        @Override
        public String getActions() {
            return "/free/multipleTbItems";
        }

        @Override
        public boolean isMethodGet() {
            return true;
        }

    }

    public static class SingleItemApi extends BusAPIs<Item> {
        public SingleItemApi(Long id) {
            super(autoTitleHost);
            urlBuilder.appendParam("id", id.toString());
        }

        @Override
        public Validator<Item> getValidator() {
            return new Validator<Item>() {
                @Override
                public Item validate(HttpResponse resp) throws ClientException {

                    Gson gson = new Gson();
                    Item item = gson.fromJson(StringValidator.validate(resp), Item.class);
                    return item;
                }

            };
        }

        @Override
        public String getActions() {
            return "/free/singleItem";
        }

        @Override
        public boolean isMethodGet() {
            return true;
        }
    }
}
