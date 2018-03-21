
package bustbapi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.hotitem.CatTopWordPlay;
import models.jms.MsgContent;
import models.mysql.word.WordBase;
import models.words.ALResult;
import models.words.HotSalesItem;
import models.words.SearchHeat;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import pojo.webpage.top.TMWordBase;
import result.TMResult;
import search.SearchManager;
import utils.TaobaoUtil;

import com.alibaba.fastjson.JSON;
import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.Validator;
import com.ciaosir.client.api.BusAPIs;
import com.ciaosir.client.pojo.IWordBase;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.pojo.WordBaseBean;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.commons.ClientException;
import com.taobao.api.domain.INWordCategory;

import dto.eslexicon.ESSearchResult;

public class TMApi {

    private static final Logger log = LoggerFactory.getLogger(TMApi.class);

    public static final String TAG = "TMApi";

/*    static String TIAN_XIAO_MAO_URL = Play.mode.isProd() ? "http://10.128.1.58:9771"
            : "http://42.121.137.197:9002";*/

    public static String TMC_WORD_URL = Play.mode.isProd() ? "http://jbt:9002" :
            ("zrb".equals(Play.id) ? "http://localhost:9999" : "http://z.taovgo.com");

    private static final String ChedaoUrl = "http://chedao.taovgo.com";
    private static final String ESLexiconURL = "http://bbn29:9090/api";

    //private static final String ChedaoUrl = "http://localhost:9000";
    /*static String TIAN_XIAO_MAO_URL = "http://10.128.1.58:9771";*/

    public enum TMOrder {
        pv, click, scount, score, strikeFocus,
//        pv, click_desc, scount_desc, score_desc, strikeFocus_desc
    }

    public static class TMWordEquelApi extends BusAPIs<List<WordBase>> {
        public TMWordEquelApi(String words) {
            super(TMC_WORD_URL);
            this.urlBuilder.appendParam(PARAM_WORDS, words);
        }

        public TMWordEquelApi(Collection<String> words) {
            super(TMC_WORD_URL);
            this.urlBuilder.appendParam(PARAM_WORDS, StringUtils.join(words, ','));
        }

        @Override
        public Validator<List<WordBase>> getValidator() {
            return WordBaseValidator;
        }

        @Override
        public String getActions() {
            return "/Words/equal";
        }

        @Override
        public boolean isMethodGet() {
            return false;
        }

    }

    /**
     * TODO we need total count....
     * http://bbn04:9002/words/fetch?words=%E5%A5%B3%E8%A3%85
     * @author zrb
     */
    public static class TMWordBaseApi extends BusAPIs<TMResult> {

        /**
         * 用, 拼起来
         * @param words
         * @param po
         * @param order
         * @param desc
         */
        public TMWordBaseApi(String words, PageOffset po, String order, String desc) {
            super(TMC_WORD_URL);
            urlBuilder.appendParam(PARAM_WORDS, words);
            urlBuilder.appendParam(PARAM_PAGE_NUMBER, po.getPn());
            urlBuilder.appendParam(PARAM_PAGE_SIZE, po.getPs());
            urlBuilder.appendParam(PARAM_ORDER, order);
            if (!StringUtils.isEmpty(desc)) {
                urlBuilder.appendParam("desc", desc);
            }

//            this.urlBuilder = new CiaoURLBuilder(TIAN_XIAO_MAO_URL, getActions(), isMethodGet());
            log.info("[builder: ]" + this.urlBuilder.genRequest().getURI());
            defaultTimeout = 6000;
        }

        public TMWordBaseApi(List<String> words, PageOffset po, String order, String desc) {
            this(StringUtils.join(words, ','), po, order, desc);
        }

        public TMWordBaseApi(String word, PageOffset po, String order) {
            this(word, po, order, null);
        }

        @Override
        public Validator<TMResult> getValidator() {
            return TMResultListValidator;
        }

        @Override
        public String getActions() {
            return "/words/fetch";
        }

        @Override
        public boolean isMethodGet() {
            return true;
        }
    }

    public static final Validator<List<WordBase>> WordBaseValidator = new Validator<List<WordBase>>() {
        @Override
        public List<WordBase> validate(HttpResponse resp) throws ClientException {
            String res = Validator.StringValidator.validate(resp);
            WordBase[] beans = JsonUtil.toObject(res, WordBase[].class);
            List<WordBase> list = new ArrayList<WordBase>();
            for (WordBase wordBase : beans) {
                list.add(wordBase);
            }
            return list;
        }
    };

    public static final Validator<TMResult> TMResultListValidator = new Validator<TMResult>() {
        @Override
        public TMResult validate(HttpResponse resp) throws ClientException {
            String string = null;
            try {
                string = EntityUtils.toString(resp.getEntity());
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
//            log.info("[string:]" + string);
            if (StringUtils.isEmpty(string)) {
                return new TMResult(true, 0, 10, 0, StringUtils.EMPTY, ListUtils.EMPTY_LIST);
            }
            JsonNode node = JsonUtil.readJsonResult(string);
            int count = node.get("count").getIntValue();
            int pn = node.get("pn").getIntValue();
            int ps = node.get("ps").getIntValue();
//            log.info("[node]" + node);
            WordBase[] beans = JsonUtil.toObject(node.get("res"), WordBase[].class);
            List<TMWordBase> bases = new ArrayList<TMWordBase>();
            if (ArrayUtils.isEmpty(beans)) {
                return new TMResult(true, pn, ps, count, StringUtils.EMPTY, ListUtils.EMPTY_LIST);
            }
            for (WordBase bean : beans) {
                bases.add(new TMWordBase(bean));
            }

            SearchManager.getIntance().removeDumpElem(bases);
            return new TMResult(true, pn, ps, count, StringUtils.EMPTY, bases);
        }

    };

    public static class TMWangkeWordBaseApi extends BusAPIs<List<WordBase>> {

        /**
         * 用, 拼起来
         * @param words
         * @param po
         * @param order
         * @param desc
         */
        public TMWangkeWordBaseApi(String words, PageOffset po, String order, String desc) {
            super(TMC_WORD_URL);
            urlBuilder.appendParam(PARAM_WORDS, words);
            urlBuilder.appendParam(PARAM_PAGE_NUMBER, po.getPn());

            urlBuilder.appendParam(PARAM_PAGE_SIZE, po.getPs());
            urlBuilder.appendParam(PARAM_ORDER, order);
            if (!StringUtils.isEmpty(desc)) {
                urlBuilder.appendParam("desc", desc);
            }

//            this.urlBuilder = new CiaoURLBuilder(TIAN_XIAO_MAO_URL, getActions(), isMethodGet());
            log.info("[builder: ]" + this.urlBuilder.genRequest().getURI());

        }

        public TMWangkeWordBaseApi(List<String> words, PageOffset po, String order, String desc) {
            this(StringUtils.join(words, ','), po, order, desc);
        }

        public TMWangkeWordBaseApi(String word, PageOffset po, String order) {
            this(word, po, order, null);
        }

        @Override
        public Validator<List<WordBase>> getValidator() {
            return TMWangkeListValidator;
        }

        @Override
        public String getActions() {
            return "/words/fetch";
        }

        @Override
        public boolean isMethodGet() {
            return true;
        }
    }

    public static final Validator<List<WordBase>> TMWangkeListValidator = new Validator<List<WordBase>>() {
        @Override
        public List<WordBase> validate(HttpResponse resp) throws ClientException {
            String string = null;
            try {
                string = EntityUtils.toString(resp.getEntity());
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
//            log.info("[string:]" + string);
            if (StringUtils.isEmpty(string)) {
                return ListUtils.EMPTY_LIST;
            }
            JsonNode node = JsonUtil.readJsonResult(string);
//            int count = node.get("count").getIntValue();
//            int pn = node.get("pn").getIntValue();
//            int ps = node.get("ps").getIntValue();
//            log.info("[node]" + node);
            WordBase[] beans = JsonUtil.toObject(node.get("res"), WordBase[].class);
            List<WordBase> bases = new ArrayList<WordBase>();
            if (ArrayUtils.isEmpty(beans)) {
                return ListUtils.EMPTY_LIST;
            }
            for (WordBase bean : beans) {
                bases.add(bean);
            }
            return bases;
        }

    };

    public static List<TMWordBase> mergeDumpWordBase(List<TMWordBase> bases) {
        if (CommonUtils.isEmpty(bases)) {
            return ListUtils.EMPTY_LIST;
        }

        Map<String, TMWordBase> map = new HashMap<String, TMWordBase>();
        List<TMWordBase> list = bases;
        for (TMWordBase iWordBase : list) {
            //if (iWordBase.getClick() < 30) {
            //    continue;
            //}

            String trimmed = TaobaoUtil.fastRemove(iWordBase.getWord(), " ");

            if (map.containsKey(trimmed)) {
                continue;
            }
            iWordBase.setWord(trimmed);
            map.put(trimmed, iWordBase);
        }

        return new ArrayList<TMWordBase>(map.values());
    }

    static class IndexWordBase implements Comparable<IndexWordBase> {
        int index;

        String word;

        IWordBase base;

        public IndexWordBase(int index, IWordBase base) {
            super();
            this.index = index;
            this.base = base;
            this.word = base.getWord();
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public IWordBase getBase() {
            return base;
        }

        public void setBase(IWordBase base) {
            this.base = base;
        }

        @Override
        public int compareTo(IndexWordBase o) {
            return this.index - o.index;
        }

    }

    public static class TMCatTopWordGetApi extends BusAPIs<List<CatTopWordPlay>> {

        public TMCatTopWordGetApi(long firstCid, long secondCid, long thirdCid, int pn, int ps,
                String orderBy, boolean isDesc) {
            super(ChedaoUrl);

            this.defaultTimeout = 20000;

            this.urlBuilder.appendParam("firstCid", firstCid + "");
            this.urlBuilder.appendParam("secondCid", secondCid + "");
            this.urlBuilder.appendParam("thirdCid", thirdCid + "");
            this.urlBuilder.appendParam("pn", pn + "");
            this.urlBuilder.appendParam("ps", ps + "");
            this.urlBuilder.appendParam("orderBy", orderBy);
            this.urlBuilder.appendParam("isDesc", isDesc + "");
        }

        @Override
        public Validator<List<CatTopWordPlay>> getValidator() {
            return new Validator<List<CatTopWordPlay>>() {
                @Override
                public List<CatTopWordPlay> validate(HttpResponse resp) throws ClientException {
                    String topWordJson = Validator.StringValidator.validate(resp);

                    if (StringUtils.isEmpty(topWordJson)) {
                        log.error("error: the result is empty from action: " + getActions() + "--------------");
                        return new ArrayList<CatTopWordPlay>();
                    }

                    CatTopWordPlay[] topWordArray = JsonUtil.toObject(topWordJson, CatTopWordPlay[].class);

                    if (topWordArray == null) {
                        log.error("fail to parse CatTopWordPlay json: " + topWordJson + "--------------");
                        return new ArrayList<CatTopWordPlay>();
                    }

                    List<CatTopWordPlay> topWordList = new ArrayList<CatTopWordPlay>();

                    for (CatTopWordPlay topWord : topWordArray) {
                        if (topWord == null) {
                            continue;
                        }
                        topWordList.add(topWord);
                    }
                    return topWordList;
                }
            };
        }

        @Override
        public String getActions() {
            return "/commons/findCatTopWordByCids";
        }

        @Override
        public boolean isMethodGet() {
            return false;
        }

    }

    public static class TMCatTopWordCountApi extends BusAPIs<Long> {

        public TMCatTopWordCountApi(long firstCid, long secondCid, long thirdCid) {
            super(ChedaoUrl);

            this.defaultTimeout = 20000;

            this.urlBuilder.appendParam("firstCid", firstCid + "");
            this.urlBuilder.appendParam("secondCid", secondCid + "");
            this.urlBuilder.appendParam("thirdCid", thirdCid + "");
        }

        @Override
        public Validator<Long> getValidator() {
            return new Validator<Long>() {
                @Override
                public Long validate(HttpResponse resp) throws ClientException {
                    String countStr = Validator.StringValidator.validate(resp);

                    if (StringUtils.isEmpty(countStr)) {
                        log.error("error: the countStr is empty from action: " + getActions() + "--------------");
                        return 0L;
                    }

                    try {
                        long count = Long.parseLong(countStr);

                        return count;

                    } catch (Exception ex) {
                        log.error(ex.getMessage(), ex);
                        log.error("error to parse " + countStr + " to long from action: " + getActions()
                                + "--------------");
                        return 0L;
                    }
                }
            };
        }

        @Override
        public String getActions() {
            return "/commons/countCatTopWordByCids";
        }

        @Override
        public boolean isMethodGet() {
            return false;
        }

    }

    public static class UpdateTaociApi extends BusAPIs<Long> {

        public UpdateTaociApi(String keyword, int count) {
            super(ChedaoUrl);

            this.defaultTimeout = 20000;

            this.urlBuilder.appendParam("keyword", keyword);
            this.urlBuilder.appendParam("count", count + "");
        }

        @Override
        public Validator<Long> getValidator() {
            return new Validator<Long>() {
                @Override
                public Long validate(HttpResponse resp) throws ClientException {
                    String countStr = Validator.StringValidator.validate(resp);

                    if (StringUtils.isEmpty(countStr)) {
                        log.error("error: the countStr is empty from action: " + getActions() + "--------------");
                        return 0L;
                    }

                    try {
                        long count = Long.parseLong(countStr);

                        return count;

                    } catch (Exception ex) {
                        log.error(ex.getMessage(), ex);
                        log.error("error to parse " + countStr + " to long from action: " + getActions()
                                + "--------------");
                        return 0L;
                    }
                }
            };
        }

        @Override
        public String getActions() {
            return "/TMApi/updateTaoci";
        }

        @Override
        public boolean isMethodGet() {
            return false;
        }

    }

    public static class BatchUpdateTaociApi extends BusAPIs<Long> {

        public BatchUpdateTaociApi(String keyword, String count) {
            super(ChedaoUrl);

            this.defaultTimeout = 20000;

            this.urlBuilder.appendParam("keyword", keyword);
            this.urlBuilder.appendParam("count", count);
        }

        @Override
        public Validator<Long> getValidator() {
            return new Validator<Long>() {
                @Override
                public Long validate(HttpResponse resp) throws ClientException {
                    String countStr = Validator.StringValidator.validate(resp);

                    if (StringUtils.isEmpty(countStr)) {
                        log.error("error: the countStr is empty from action: " + getActions() + "--------------");
                        return 0L;
                    }

                    try {
                        long count = Long.parseLong(countStr);

                        return count;

                    } catch (Exception ex) {
                        log.error(ex.getMessage(), ex);
                        log.error("error to parse " + countStr + " to long from action: " + getActions()
                                + "--------------");
                        return 0L;
                    }
                }
            };
        }

        @Override
        public String getActions() {
            return "/TMApi/BatchUpdateTaoci";
        }

        @Override
        public boolean isMethodGet() {
            return false;
        }

    }

//    static class WordDealer {
//        public WordDealer() {
//        }
//
//        public List<? extends IWordBase> mergeDumpKeepingSort(List<? extends IWordBase> list) {
//            if (CommonUtils.isEmpty(list)) {
//                return ListUtils.EMPTY_LIST;
//            }
//
//            int originLength = list.size();
//            Map<String, IndexWordBase> originBases = new HashMap<String, IndexWordBase>();
//            for (int i = 0; i < originLength; i++) {
////                originBase.add(new IndexWordBase(i, ));
//                IWordBase iWordBase = list.get(i);
//                originBases.put(iWordBase.getWord(), new IndexWordBase(i, iWordBase));
//            }
//
//            SearchManager.getIntance().removeDumpElem(list);
////
////            List<IndexWordBase> toSortList = new ArrayList<IndexWordBase>();
////
////            for (IWordBase left : list) {
////                IndexWordBase indexWordBase = originBases.get(left.getWord());
////                if (indexWordBase != null) {
////                    toSortList.add(indexWordBase);
////                }
////            }
////            
////            Collections.sort(toSortList);
////            list.clear();
////            for (IndexWordBase indexWordBase : toSortList) {
////                IWordBase base = indexWordBase.getBase();
////                list.addAll(base);
////            }
//
//            // Now, this list is no order...
//            return list;
//        }
//    }

//    public static WordDealer wordDealer = new WordDealer();

    // TODO, 可以试试 更新所有的  商品的库存，观察是否有变化
    private static final String FourDriverUrl = Play.mode.isProd() ? "http://jbt09:9000" : "http://driver.tobti.com";

    public static class BusWordBaseGetApi extends BusAPIs<Map<String, WordBaseBean>> {
        public BusWordBaseGetApi(Collection<String> wordColl, long timeLength,
                int cacheDays, boolean isDirectUpdateBase) {
            this(StringUtils.join(wordColl, ','), timeLength, cacheDays, isDirectUpdateBase);
        }

        public BusWordBaseGetApi(String wordColls, long timeLength,
                int cacheDays, boolean isDirectUpdateBase) {
            super(FourDriverUrl);

            this.defaultTimeout = 400000;

            this.urlBuilder.appendParam("words", wordColls);
            this.urlBuilder.appendParam("timeLength", String.valueOf(timeLength));
            this.urlBuilder.appendParam("cacheDays", String.valueOf(cacheDays));
            this.urlBuilder.appendParam("isDirectUpdateBase", String.valueOf(isDirectUpdateBase));
        }

        @Override
        public Validator<Map<String, WordBaseBean>> getValidator() {
            return new Validator<Map<String, WordBaseBean>>() {
                @Override
                public Map<String, WordBaseBean> validate(HttpResponse resp) throws ClientException {
                    try {
                        String res = Validator.StringValidator.validate(resp);
                        if (StringUtils.isEmpty(res)) {
                            log.error("BusWordBaseGetApi error res: " + res + "-------------------");
                            return new HashMap<String, WordBaseBean>();
                        }
                        WordBaseBean[] wordBaseArray = JsonUtil.toObject(res, WordBaseBean[].class);
                        if (wordBaseArray == null) {
                            log.error("fail to parse json of wordBaseMap: " + res + "-------------------------");
                            return new HashMap<String, WordBaseBean>();
                        }
                        List<WordBaseBean> wordBaseList = Arrays.asList(wordBaseArray);
                        if (CommonUtils.isEmpty(wordBaseList)) {
                            log.error("return none of wordBaseMap: " + res + "-------------------------");
                            return new HashMap<String, WordBaseBean>();
                        }

                        Map<String, WordBaseBean> wordBaseMap = new HashMap<String, WordBaseBean>();
                        for (WordBaseBean wordBase : wordBaseList) {
                            if (wordBase == null) {
                                continue;
                            }
                            String word = wordBase.getWord();
                            if (StringUtils.isEmpty(word)) {
                                continue;
                            }
                            wordBaseMap.put(word, wordBase);
                        }

                        return wordBaseMap;
                    } catch (Exception ex) {
                        log.error(ex.getMessage(), ex);
                        return new HashMap<String, WordBaseBean>();
                    }

                }
            };
        }

        @Override
        public String getActions() {
            return "/Words/getWordBaseList";
        }

        @Override
        public boolean isMethodGet() {
            return false;
        }

    }

    public static class BusCategoryWordBaseGetApi extends BusAPIs<Map<String, INWordCategory>> {
        public BusCategoryWordBaseGetApi(Long cid, Collection<String> wordColl,
                int cacheDays, int maxCacheDays, boolean isReturnWhileApiFail) {

            super(FourDriverUrl);

            this.defaultTimeout = 400000;

            String words = StringUtils.join(wordColl, ",");
            this.urlBuilder.appendParam("cid", cid + "");
            this.urlBuilder.appendParam("words", words);
            this.urlBuilder.appendParam("cacheDays", cacheDays + "");
            this.urlBuilder.appendParam("maxCacheDays", maxCacheDays + "");
            this.urlBuilder.appendParam("isReturnWhileApiFail", isReturnWhileApiFail + "");
        }

        @Override
        public Validator<Map<String, INWordCategory>> getValidator() {
            return new Validator<Map<String, INWordCategory>>() {
                @Override
                public Map<String, INWordCategory> validate(HttpResponse resp) throws ClientException {
                    String res = Validator.StringValidator.validate(resp);
                    if (StringUtils.isEmpty(res)) {
//                        log.error("BusCategoryWordBaseGetApi error res: " + res + "-------------------");
                        return null;
                    }
                    INWordCategory[] inWordCategoryArr = JsonUtil.toObject(res, INWordCategory[].class);
                    if (inWordCategoryArr == null) {
//                        log.error("fail to parse json of inWordCategoryArr: " + res + "-------------------------");
                        return new HashMap<String, INWordCategory>();
                    }
                    List<INWordCategory> inWordCategoryList = Arrays.asList(inWordCategoryArr);
                    if (CommonUtils.isEmpty(inWordCategoryList)) {
//                        log.error("return none of inWordCategoryList: " + res + "-------------------------");
                        return new HashMap<String, INWordCategory>();
                    }

                    Map<String, INWordCategory> catBaseMap = new HashMap<String, INWordCategory>();
                    for (INWordCategory catBase : inWordCategoryList) {
                        if (catBase == null) {
                            continue;
                        }
                        String word = catBase.getWord();
                        if (StringUtils.isEmpty(word)) {
                            continue;
                        }
                        catBaseMap.put(word, catBase);
                    }

                    return catBaseMap;
                }
            };
        }

        @Override
        public String getActions() {
            return "/Words/getCategoryBaseList";
        }

        @Override
        public boolean isMethodGet() {
            return false;
        }

    }
    
    public static class HotKeywordSearchApi extends BusAPIs<ESSearchResult> {
    	
    	/**
    	 * @Title:SearchHotKeyword
    	 * @Description:
    	 * @param itemTitle		not null
    	 */ 	
    	public HotKeywordSearchApi(String itemTitle){
    		this(itemTitle, null, 0, 0, "impressions", true);
    	}
    	/**
    	 * @Title:SearchHotKeyword
    	 * @Description:
    	 * @param itemTitle		not null
    	 * @param minScore		0.0~10,default 0.2
    	 */
    	public HotKeywordSearchApi(String itemTitle, String minScore){
    		this(itemTitle, minScore, 0, 0, "impressions", true);
    	}
    	/**
    	 * @Title:SearchHotKeyword
    	 * @Description:
    	 * @param itemTitle		not null
    	 * @param pn		page number
    	 */
    	public HotKeywordSearchApi(String itemTitle, int pn){
    		this(itemTitle, null, pn, 0, "impressions", true);
    	}
    	/**
    	 * @Title:SearchHotKeyword
    	 * @Description:
    	 * @param itemTitle		not null
    	 * @param minScore		0.0~10,default 0.2
    	 * @param pn		page number
    	 */
    	public HotKeywordSearchApi(String itemTitle, String minScore, int pn){
    		this(itemTitle, minScore, pn, 0, "impressions", true);
    	}

    	/**
    	 * @Title:SearchHotKeyword
    	 * @Description: 
    	 * @param itemTitle		not null
    	 * @param minScore		0.0~10,default 0.2
    	 * @param pn		page number
    	 * @param ps		page size,default 1000
    	 * @param sortBy		default score
    	 * @param isDesc
    	 */
    	public HotKeywordSearchApi(String itemTitle, String minScore, int pn, int ps, String sortBy, boolean isDesc){
    		super(ESLexiconURL);
    		this.defaultTimeout = 5000;
    		if (StringUtils.isEmpty(itemTitle)) {
				//error, How to handle this error?
    			//先交给ESLexicon处理，抛出
			}else {
				this.urlBuilder.appendParam("itemTitle", itemTitle);
			}
    		if (!StringUtils.isEmpty(minScore)) {
    			this.urlBuilder.appendParam("minScore", minScore);
			}
    		if (pn > 0) {
    			this.urlBuilder.appendParam("pn", pn);
			}
    		if (ps >= 10) {
    			this.urlBuilder.appendParam("ps", ps);
			}
    		if ("impressions".equals(sortBy)) {
				this.urlBuilder.appendParam("sortBy", "impressions");
			}else {
				this.urlBuilder.appendParam("sortBy", "_score");
			}
    		if (isDesc) {
    			this.urlBuilder.appendParam("sortOrder", "desc");
			} else {
				this.urlBuilder.appendParam("sortOrder", "asc");
			}
    	}
    	
		@Override
		protected Validator<ESSearchResult> getValidator() {
			return new Validator<ESSearchResult>() {
				@Override
				public ESSearchResult validate(HttpResponse resp) {
					String res = "";
					try {
						res = Validator.StringValidator.validate(resp);
						if (StringUtils.isEmpty(res)) {
							return new ESSearchResult(false, "未知错误，空响应");
						}
					} catch (ClientException e) {
						log.error("访问ES Search服务时出错：本地http连接错误"+e.getLocalizedMessage());
						return new ESSearchResult(false, "本地http连接错误"+e.getLocalizedMessage());
					}
                    //相应于ESLexicon也使用fastjson进行反序列化
					return JSON.parseObject(res, ESSearchResult.class);
				}
			};
		}

		@Override
		protected String getActions() {
			return "/HotKeyWord/search";
		}

		@Override
		protected boolean isMethodGet() {
			return true;
		}
    }
    
	private static final String TXGURL = Play.mode.isDev() ? "http://vip.tianxiaogou.com" : "http://bbn31:9006";
	
	public static class CategoryDataApi extends BusAPIs<ALResult> {
		public CategoryDataApi(long cid, String day, int pn, String sort) {
			super(TXGURL);
			this.urlBuilder.appendParam("cid", cid + "");
			this.urlBuilder.appendParam("day", day + "");
			this.urlBuilder.appendParam("pn", pn + "");
			this.urlBuilder.appendParam("sort", sort + "");
		}

		@Override
		public Validator<ALResult> getValidator() {
			return CategoryDataValidator;
		}

		@Override
		public String getActions() {
			return "/TradeQueryForTM/hotSalesRank";
		}

		@Override
		public boolean isMethodGet() {
			return false;
		}

	}
	
	public static final Validator<ALResult> CategoryDataValidator = new Validator<ALResult>() {
		@Override
		public ALResult validate(HttpResponse resp) throws ClientException {
			String res = Validator.StringValidator.validate(resp);
			if (StringUtils.isEmpty(res)) {
				return null;
			}
			ALResult result;
			try {
				JSONObject jsonObject = new JSONObject(res);
				Boolean isOk = jsonObject.getBoolean("isOk");
				if(!isOk) {
					String msg = jsonObject.getString("msg");
					result = new ALResult(isOk, msg);
					return result;
				}
				
				int pn = jsonObject.getInt("pn");
				int ps = jsonObject.getInt("ps");
				int count = jsonObject.getInt("count");
				String content = jsonObject.getString("res");
				HotSalesItem[] beans = JsonUtil.toObject(content, HotSalesItem[].class);
				List<HotSalesItem> list = new ArrayList<HotSalesItem>();
				for (HotSalesItem hotSalesItem : beans) {
					list.add(hotSalesItem);
				}
				result = new ALResult(isOk, pn, ps, count, "", list);
				
				return result;
			} catch (JSONException e) {
				log.error(e.getMessage(), e);
			}
			return null;
		}
	};
	
	public static class hotSalesItemRankApi extends BusAPIs<ALResult> {
		public hotSalesItemRankApi(long numIid, String day, int pn, long wordId, int track, int rankType) {
			super(TXGURL);
			this.urlBuilder.appendParam("numIid", numIid + "");
			this.urlBuilder.appendParam("day", day + "");
			this.urlBuilder.appendParam("pn", pn + "");
			this.urlBuilder.appendParam("wordId", wordId + "");
			this.urlBuilder.appendParam("track", track + "");
			this.urlBuilder.appendParam("rankType", rankType + "");
		}

		@Override
		public Validator<ALResult> getValidator() {
			return HotSalesItemRankValidator;
		}

		@Override
		public String getActions() {
			return "/TradeQueryForTM/hotSalesItemRank";
		}

		@Override
		public boolean isMethodGet() {
			return false;
		}

	}
	
	public static final Validator<ALResult> HotSalesItemRankValidator = new Validator<ALResult>() {
		@Override
		public ALResult validate(HttpResponse resp) throws ClientException {
			String res = Validator.StringValidator.validate(resp);
			if (StringUtils.isEmpty(res)) {
				return null;
			}
			ALResult result;
			try {
				JSONObject jsonObject = new JSONObject(res);
				Boolean isOk = jsonObject.getBoolean("isOk");
				if(!isOk) {
					String msg = jsonObject.getString("msg");
					result = new ALResult(isOk, msg);
					return result;
				}
				
				int pn = jsonObject.getInt("pn");
				int ps = jsonObject.getInt("ps");
				int count = jsonObject.getInt("count");
				String content = jsonObject.getString("res");

				com.alibaba.fastjson.JSONObject parseObject = JSON.parseObject(content);
				
				if(count == 0) {
					result = new ALResult(parseObject);
				} else {
					result = new ALResult(isOk, pn, ps, count, "", parseObject);
				}
				
				return result;
			} catch (JSONException e) {
				log.error(e.getMessage(), e);
			}
			return null;
		}
	};
	
	public static class searchHeatDataApi extends BusAPIs<ALResult> {
		public searchHeatDataApi(Long numIid, String startDate, String endDate) {
			super(TXGURL);
			this.urlBuilder.appendParam("numIid", numIid + "");
		}

		@Override
		public Validator<ALResult> getValidator() {
			return SearchHeatDataValidator;
		}

		@Override
		public String getActions() {
			return "/TradeQueryForTM/getSearchHeatData";
		}

		@Override
		public boolean isMethodGet() {
			return false;
		}

	}
	
	public static final Validator<ALResult> SearchHeatDataValidator = new Validator<ALResult>() {
		@Override
		public ALResult validate(HttpResponse resp) throws ClientException {
			String res = Validator.StringValidator.validate(resp);
			if (StringUtils.isEmpty(res)) {
				return null;
			}
			ALResult result;
			try {
				JSONObject jsonObject = new JSONObject(res);
				Boolean isOk = jsonObject.getBoolean("isOk");
				if(!isOk) {
					String msg = jsonObject.getString("msg");
					result = new ALResult(isOk, msg);
					return result;
				}
				
				String content = jsonObject.getString("res");
				SearchHeat[] beans = JsonUtil.toObject(content, SearchHeat[].class);
				List<SearchHeat> list = new ArrayList<SearchHeat>();
				for (SearchHeat searchHeat : beans) {
					list.add(searchHeat);
				}
				result = new ALResult(isOk, "", list);
				
				return result;
			} catch (JSONException e) {
				log.error(e.getMessage(), e);
			}
			return null;
		}
	};
	
}
