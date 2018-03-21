package controllers;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import models.Txg20WWords.Txg20WWordsDayDetail;
import models.hotitem.CatTopWordPlay;
import models.item.ItemCatPlay;
import models.mysql.fengxiao.HotWordCount;
import models.mysql.fengxiao.HotWordCount.WordCount;
import models.mysql.word.TMCWordBase;
import models.word.top.TopKey;
import models.word.top.TopURLBase;
import models.word.top.TopKey.QueryType;
import net.sf.oval.constraint.NotBlank;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.ivy.ant.IvyPublish.PublishArtifact;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.mvel2.util.FastList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import play.cache.CacheFor;
import play.db.jpa.NoTransaction;
import play.mvc.Controller;
import pojo.webpage.top.TMWordBase;
import result.TMPaginger;
import result.TMResult;
import search.SearchManager;
import sug.api.QuerySugAPI;
import underup.frame.industry.CatTopSaleItemSQL;
import underup.frame.industry.HotShop;
import underup.frame.industry.ItemCatLevel1;
import underup.frame.industry.ItemCatLevel2;
import underup.frame.industry.ItemPropsArrange;
import underup.frame.industry.ItemsCatArrange;
import underup.frame.industry.ListTimeRange;
import underup.frame.industry.MonthInfo;
import underup.frame.industry.PriceDistribution;
import underup.frame.industry.YearAndMonth;
import underup.frame.industry.YearInfo;
import underup.frame.industry.CatTopSaleItemSQL.TopSaleItem;
import underup.frame.industry.HotShop.HotShopInfo;
import underup.frame.industry.ItemPropsArrange.PInfo;
import underup.frame.industry.PriceDistribution.PriceRange;
import autotitle.AutoSplit;
import bustbapi.BusAPI;
import bustbapi.TMApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.api.SearchAPIs;
import com.ciaosir.client.api.WidAPIs;
import com.ciaosir.client.api.SearchAPIs.SearchParams;
import com.ciaosir.client.api.SearchAPIs.SearchRes;
import com.ciaosir.client.pojo.IWordBase;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.pojo.WordBaseBean;
import com.ciaosir.client.utils.DateUtil;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.commons.ClientException;
import com.taobao.api.ApiException;

import controllers.TMController.BusUIResult;

public class ToolInterface extends Controller {
	private static final Logger log = LoggerFactory
			.getLogger(ToolInterface.class);

	public static final String TAG = "ToolInterface";

	public static String topByUrlCachePre = "topByUrlCachePre_";

	public static SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyyMMdd");
	
	protected static void renderBusJson(Object json) {
        renderJSON(JsonUtil.getJson(new BusUIResult(json)));
    }

	public static void findAllLevel2() {
        List<TopURLBase> allLevel2 = TopURLBase.findAllByLevel(2);
        renderJSON(allLevel2);
    }
	
	public static final int WireLessPageSize = 20;
	@NoTransaction
	public static void receiveWordRankFromClient(String word, String ranks, int pn) {
		if(StringUtils.isEmpty(word)) {
			renderText("传入的word为空");
		}
		if(StringUtils.isEmpty(ranks)) {
			renderText("传入的ranks为空");
		}
		if(pn < 1) {
			renderText("传入的页码不合法");
		}
		String[] rankArr = ranks.split(",");
		if(rankArr == null || rankArr.length > 20) {
			renderText("传入的ranks不合法");
		}
		int offset = WireLessPageSize * pn;
		// 此处应用word查询wordId，测试暂时用随机数
		Long wordId = Math.abs(new Random().nextLong());
		String day = sdf.format(System.currentTimeMillis());
		if(Txg20WWordsDayDetail.isExisted(wordId, day) <= 0) {
			boolean isSuccess = Txg20WWordsDayDetail.batchInsert(wordId, rankArr, day, offset);
			if(isSuccess) {
				renderText("同步成功");
			} else {
				renderText("同步失败");
			}
		}
		renderText("该关键词当天已同步");
	}
	
	@CacheFor(value = "2h")
    public static void findLevel3(String level2) {
        List<TopURLBase> list = TopURLBase.findByLevel2(level2);
        if (StringUtils.isEmpty(level2)) {
            renderJSON("[]");
        }

        Iterator<TopURLBase> it = list.iterator();
        while (it.hasNext()) {
            TopURLBase next = it.next();
            TopKey topkey = TopKey.findOne(next.getId());
//            log.info("[found :]" + topkey);
            if (topkey == null) {
                it.remove();
            }
        }

        if (list.isEmpty()) {
            list.add((TopURLBase) TopURLBase.findById(Long.parseLong(level2)));
        }
        renderJSON(JsonUtil.getJson(list));
    }
	
	@CacheFor("3h")
    public static void findLevel1() {
        List<ItemCatPlay> catList = ItemCatPlay.findAllFirstLevelCats();
        renderBusJson(catList);
    }
    
    @CacheFor("3h")
    public static void findLevel2or3(Long parentCid) {
        if (parentCid == null || parentCid <= 0L) {
            parentCid = 0L;
            //renderFailedJson("请先选择一个类目！");
        }
        List<ItemCatPlay> catList = ItemCatPlay.findByParentCid(parentCid);
        renderBusJson(catList);
    }
    
    @NoTransaction
	public static void topByUrl(String type, long urlId, int pn, int ps) {

		ps = ps < 5 ? 20 : ps;
		TMPaginger paginer = TopKey.findByUrlBaseId(urlId, pn, ps,
				QueryType.getType(type));

		// 更新topkey词的pv, click, ctr数据
		if (paginer != null) {
			List<TopKey> keys = (List<TopKey>) paginer.getRes();
			if (!CommonUtils.isEmpty(keys)) {
				// 首先应该检查Cache是否有数据
				String key = topByUrlCachePre + type.replace(" ", "") + "_"
						+ urlId + "_" + pn + "_" + ps;
				Map<String, IWordBase> execute = (Map<String, IWordBase>) Cache
						.get(key);
				if (execute == null) {
					try {
						List<String> words = new ArrayList<String>();
						for (TopKey wordBase : keys) {
							words.add(wordBase.getWord());
						}
						execute = new WidAPIs.WordBaseAPI(words).execute();
						Cache.set(key, execute);
					} catch (ClientException e) {
						// TODO Auto-generated catch block
						renderJSON(JsonUtil.getJson(paginer));
						e.printStackTrace();
					}
				}

				for (TopKey wordBase : keys) {
					IWordBase iWordBase = execute.get(wordBase.getWord());
					if (iWordBase != null) {
						wordBase.updateByWordBaseBean(iWordBase);
					}

				}

			}
		}
		renderJSON(JsonUtil.getJson(paginer));
	}

    @NoTransaction
	public static void busSearch(int pn, int ps, long numIid, String order,
			String sort, String word) throws IOException {
		if (order == null || order.isEmpty()) {
			order = "pv";
		}
		if (sort == null || sort.isEmpty()) {
			sort = "desc";
		}

		// renderMockFileInJsonIfDev("words.bussearch.json");
		// String word = params.get("s");
		String title = params.get("title");
		// String order = params.get("order");
		// String sort = params.get("sort");

		word = Words.tryGetSearchKey(word, title, numIid);
		word = CommonUtils.escapeSQL(word);
		// word = StringUtils.replace(word, " ", StringUtils.EMPTY);

		int minPageSize = 30;
		if (ps < minPageSize) {
			ps = minPageSize;
		}
		PageOffset po = new PageOffset(pn, ps, minPageSize);
		try {
			if (StringUtils.isBlank(order)) {
				order = "pv";
			}
			if (StringUtils.isEmpty(sort)) {
				sort = "asc";
			}
			final String realSort = sort;
			final String realOrder = order;
			List<String> list = new AutoSplit(word, false).execute();
			// TMResult tmresult = new TMApi.TMWordBaseApi(list, po, order,
			// sort).execute();

			TMResult tmresult = TMCWordBase.doESSearch(list, pn, ps, order,
					sort, true);

			// log.info("[result:]" + new Gson().toJson(tmresult));

			if (tmresult == null) {
				tmresult = new TMResult();
			}
			List<WordBaseBean> newlistBases = (List<WordBaseBean>) tmresult
					.getRes();
			if (newlistBases == null) {
				newlistBases = ListUtils.EMPTY_LIST;
			}
			List<TMWordBase> formedRes = new ArrayList<TMWordBase>();
			for (IWordBase tmcWordBase : newlistBases) {
				formedRes.add(new TMWordBase(tmcWordBase));
			}

			SearchManager.removeDumpElem(formedRes);
			tmresult.setRes(formedRes);
			// Collections.sort(newlistBases, new Comparator<TMWordBase>() {
			// public int compare(TMWordBase arg0, TMWordBase arg1) {
			// if (realSort.equals("desc")) {
			// return arg1.getByProp(realOrder) - arg0.getByProp(realOrder);
			// } else {
			// return arg0.getByProp(realOrder) - arg1.getByProp(realOrder);
			// }
			// }
			// });

			renderJSON(JsonUtil.getJson(tmresult));
		} catch (ClientException e) {
			log.warn(e.getMessage(), e);
		}
		renderJSON(TMResult.failMsg("亲,词库数据暂时有点问题哟,可以联系客服呢"));
	}

    @NoTransaction
	public static void findCatTopWords(long firstCid, long secondCid,
			long thirdCid, int pn, int ps, String orderBy, boolean isDesc) {

		PageOffset po = new PageOffset(pn, ps);

		try {
			List<CatTopWordPlay> topWordList = new TMApi.TMCatTopWordGetApi(
					firstCid, secondCid, thirdCid, pn, ps, orderBy, isDesc)
					.execute();

			long count = new TMApi.TMCatTopWordCountApi(firstCid, secondCid,
					thirdCid).execute();

			TMResult tmRes = new TMResult(topWordList, (int) count, po);

			renderJSON(JsonUtil.getJson(tmRes));
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);

			TMResult tmRes = new TMResult(new ArrayList<CatTopWordPlay>(), 0,
					po);

			renderJSON(JsonUtil.getJson(tmRes));
		}

	}

	@NoTransaction
	public static void search(String s, String title, long numIid, int pn,
			int ps) throws ClientException {
		// log.info("[search word:]>>>" + s);

		s = Words.tryGetSearchKey(s, title, numIid);

		if (StringUtils.isBlank(s)) {
			renderJSON(TMPaginger.makeEmptyFail("亲，请输入搜索关键词哦"));
		}
		pn = pn < 1 ? 1 : pn;
		// ps = ps < 10 ? 30 : ps;
		ps = 60;

		String[] keys = new AutoSplit(s, ListUtils.EMPTY_LIST, true).execute()
				.toArray(new String[] {});

		if (keys.length > 2) {
			keys = (String[]) ArrayUtils.subarray(keys, 0, 2);
		}

		log.info("[query keys :]" + ArrayUtils.toString(keys));
		SearchRes call = new SearchAPIs.TermSearchApi(getSClient(), keys, pn,
				ps, SearchParams.MustBooleanPVNeededQuery).call();
		// log.info("[res num :]" + call.getList().size());

		List<IWordBase> bases = call.getList();
		SearchManager.removeDumpElem(bases);

		// log.info("[ basesa num]" + bases.size());

		// @SuppressWarnings("deprecation")
		TMPaginger paginger = new TMPaginger(pn, ps, (int) call.getTotalHits(),
				bases);
		// TMResult res = new TMResult(bases, (int) call.getTotalHits(), new
		// PageOffset(pn, ps));
		renderJSON(JsonUtil.getJson(paginger));
	}

	protected static Client getSClient() {
		return SearchManager.getIntance().getClient();
	}

	@NoTransaction
	public static void newLongTail(String s, long numIid, int pn, int ps)
			throws IOException {
		// renderMockFileInJsonIfDev("TMApi.TMWordBaseApi.json");
		s = Words.tryGetSearchKey(s, null, numIid);
		// List<String> result = QuerySugAPI.getQuerySugList(s, false);
		Map<String, Integer> result = QuerySugAPI.getQuerySugListWordCount(s);
		// log.info("[xx   ]" + result);
		if (CommonUtils.isEmpty(result)) {
			renderJSON(new ArrayList<TMWordBase>());
		}
		Map<String, WordBaseBean> map = MapUtils.EMPTY_MAP;
		try {
			map = BusAPI.wordPv(result.keySet());
			// log.info("[map --<" + map.values().size() + ">:]" + map);
		} catch (ApiException e) {
			log.warn(e.getMessage(), e);
		} catch (ClientException e) {
			log.warn(e.getMessage(), e);
		}
		// PageOffset po = new PageOffset(pn, ps, 10);
		// List<WordBase> bases = WordBase.andSearch(result, po);
		// int count = WordBase.getOrCountOfNormal(result);

		List<WordBaseBean> beans = new ArrayList<WordBaseBean>(map.values());
		int size = beans.size();
		for (WordBaseBean wordBaseBean : beans) {
			wordBaseBean.setCompetition(result.get(wordBaseBean.getWord()));
		}

		// log.info("[back beans:]" + beans);

		renderJSON(JsonUtil.getJson(new TMResult<List<WordBaseBean>>(beans,
				size, new PageOffset(1, size))));

	}
	
	@NoTransaction
	public static void yearLevel() {
        List<Long> years = YearAndMonth.getYearLong();
        List<YearInfo> yearInfos = new ArrayList<YearInfo>();
        if (years != null) {
            for (Long year : years) {
                Date d = new Date(year);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy", Locale.CHINA);
                String y = sdf.format(d);
                yearInfos.add(new YearInfo(year, y));
            }
        }
        renderJSON(yearInfos);
    }
	
	@CacheFor("3h")
    public static void monthLevel(long year) {
        List<Long> months = YearAndMonth.getMonthLong(year);
        List<MonthInfo> monthInfos = new ArrayList<MonthInfo>();
        if (months != null) {
            for (Long month : months) {
                // 判断该月是否有数据
                String tableName = CatTopSaleItemSQL.getTableName(year, month);
                if(!CatTopSaleItemSQL.isExistTable(tableName)){
                    continue;
                }
                Date d = new Date(year + month);
                SimpleDateFormat sdf = new SimpleDateFormat("MM");
                String m = sdf.format(d);
                monthInfos.add(new MonthInfo(month, m));
            }
        }
        renderJSON(monthInfos);
   }
	
	@NoTransaction
	 public static void getLevelOneCid(long levelTwoCid, long year, long month){
        long levelOneCid = ItemCatLevel2.getLevelOneCid(levelTwoCid, year, month);
        renderJSON(levelOneCid);
    }
	
	@CacheFor("3h")
    public static void txgfindLevel1(long year, long month) {//TODO
        int oneMonthMillisecond = 24*3600*1000;
        List<ItemCatLevel1> list = ItemCatLevel1.getLevel1(year, month);
//        while(CommonUtils.isEmpty(list)) {
//            list = ItemCatLevel1.getLevel1(year, month-OneMonthMillisecond);
//        }
        renderJSON(list);
    }
	
	@CacheFor("3h")
    public static void findLevel2(long levelOneCid, long year, long month) {
        List<ItemCatLevel2> list = ItemsCatArrange.level2Arrange(levelOneCid, year, month);
        log.info("------the level2 length is " + list.size());
        renderJSON(list);
    }
	
	@NoTransaction
	public static void topItems(long cid, long year, long month, int pn, int ps) throws IOException {
        ItemCatPlay itemCatPlay = ItemCatPlay.findByCid(cid);
        List<TopSaleItem> topSaleItems = new ArrayList<TopSaleItem>();
        int size;
        if (!itemCatPlay.isParent) {
            long t1, t2;
            t1 = System.currentTimeMillis();
            topSaleItems = CatTopSaleItemSQL.getTopSale100(cid, year, month, (pn - 1) * ps, ps);
            size = CatTopSaleItemSQL.getTopSize(cid, year, month);
            t2 = System.currentTimeMillis();
            log.info("-------------get the hot tiems need the time is no parent" + (t2 - t1));
        } else {
            long t1, t2;
            t1 = System.currentTimeMillis();
            topSaleItems = CatTopSaleItemSQL.getTopSale100Children(cid, year, month, (pn - 1) * ps, ps);
            size = CatTopSaleItemSQL.getTopSizeChildren(cid, year, month);
            t2 = System.currentTimeMillis();
            log.info("-------------get the hot tiems need the time is" + (t2 - t1));
        }
        PageOffset po = new PageOffset(pn, ps);
        renderJSON(JsonUtil.getJson(new TMResult<List<TopSaleItem>>(topSaleItems, size, po)));
    }
	
	@CacheFor("3h")
    public static void catHotProps1(long cid, long year, long month) throws IOException {
        List<PInfo> pInfos = new ArrayList<PInfo>();
        pInfos = new ItemPropsArrange(cid, year, month).getPInfos();
        renderJSON(pInfos);
    }

	@NoTransaction
	public static void hotShop(long cid, long year, long month, int pn, int ps) throws IOException, ClientException {
        HotShop hotShop = new HotShop(cid, year, month);
        hotShop.execute();
        List<HotShopInfo> hotShopInfos = hotShop.getShopInfo((pn - 1) * ps, ps);
        int size = hotShop.getHotShops().size();
        PageOffset po = new PageOffset(pn, ps);
        renderJSON(JsonUtil.getJson(new TMResult<List<HotShopInfo>>(hotShopInfos, size, po)));
    }
	
	@NoTransaction
	public static void delistTimeRange(long cid, long year, long month) throws IOException {
        ListTimeRange timeRange = new ListTimeRange(cid, year, month);
        timeRange.exec();
        int[] hourRange = timeRange.getHourDelistTime();
        renderBusJson(hourRange);
    }
	
	@NoTransaction
	public static void priceRange(long cid, long year, long month) throws IOException {
        PriceDistribution priceDistribution = new PriceDistribution(cid, year, month);
        priceDistribution.exec();
        Map<String, PriceRange> priceRangeMap = priceDistribution.getPriceRange();
        List<PriceRange> priceRange = new ArrayList(priceRangeMap.values());
        renderJSON(priceRange);
    }
	
	@NoTransaction
	public static void hotWords(long cid, long year, long month, int pn, int ps) throws IOException {
        List<WordCount> hotWordCounts = HotWordCount.getTopWord(cid, year, month, (pn - 1) * ps, ps);
        int size = HotWordCount.getTopWordSize(cid, year, month);
        PageOffset po = new PageOffset(pn, ps);
        renderJSON(JsonUtil.getJson(new TMResult<List<WordCount>>(hotWordCounts, size, po)));
    }

	//这个数据更新elasticsearch和表cat_top_word_play
	@NoTransaction
	public static void updateTaoci(String keyword, Integer count) {
		if (keyword == null || count == null) {
			renderJSON(new TMResult(false, "请输入正确参数", null));
		}
		//直接调车道API
		try {
			new TMApi.UpdateTaociApi(keyword, count).execute();

		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			renderJSON(new TMResult(false, "调用失败", null));
		}
		renderJSON(new TMResult());
	}

	@NoTransaction
	public static void batchUpdateTaoci(String keyword, String count) {
		if (keyword == null || count == null) {
			renderJSON(new TMResult(false, "请输入正确参数", null));
		}
		//直接调车道API
		try {
			new TMApi.BatchUpdateTaociApi(keyword, count).execute();

		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			renderJSON(new TMResult(false, "调用失败", null));
		}
		renderJSON(new TMResult());
	}


}
