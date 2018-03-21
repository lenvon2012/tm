
package controllers;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import models.CloudDataRegion;
import models.DayOverThousandUvItem;
import models.diag.RecentlyDiagedItem;
import models.item.ItemPlay;
import models.mysql.word.DiagWordInfo;
import models.user.User;
import models.user.UserContact;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.cache.Cache;
import play.db.jpa.NoTransaction;
import result.TMResult;
import utils.ClouddateUtil;
import utils.CommonUtil;
import utils.ExcelUtil;
import uvpvdiag.NewUvPvDiagResult;
import uvpvdiag.UvPvDiagResult;
import actions.DiagAction;
import actions.ItemGetAction;
import actions.NewUvPvDiagAction;
import actions.UvPvDiagAction;
import actions.clouddata.AreaViews;
import actions.clouddata.AreaViewsAndTrades;
import actions.clouddata.CPCUvPv;
import actions.clouddata.CloudDataAction;
import actions.clouddata.EntranceResult;
import actions.clouddata.ItemRelativeAccess;
import actions.clouddata.PCSrcUvPv;
import actions.clouddata.ShopHourViewAndTrade;
import actions.clouddata.SkuDetail;
import actions.clouddata.SrcResult;
import actions.clouddata.TitleAnalysisResult;
import actions.clouddata.UserDailyUvPv;
import actions.clouddata.WirelessSrcUvPv;
import actions.clouddata.WordInfo;
import bustbapi.ItemApi;
import bustbapi.MBPApi;
import bustbapi.TMApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.DateUtil;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.commons.ClientException;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.QueryRow;
import com.taobao.api.domain.Sku;

import configs.TMConfigs;
import configs.TMConfigs.App;
import configs.TMConfigs.PageSize;
import dao.UserDao;
import dao.item.ItemDao;
import dto.eslexicon.ESSearchResult;
import dto.eslexicon.WordHit;

public class Diag extends TMController {

    public static SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyyMMdd");

    private static final Logger log = LoggerFactory.getLogger(Diag.class);

    public static final String TAG = "Diag";
    
    public static final String CACHE_TIME = "20h";

    /**
     * {"id":83753542,"variance":0,"inBadTimeCount":0,"weekDistributed":"1,1,1,1,1,1,1",
     * "titleScore":0,"remainWindowCount":0,"windowUsage":0,
     * "updateTs":1358012565910,"tradeCount":2,
     * "reverse":0,"badTitleCount":0,
     * "goodItemCount":0,
     * "potentialGoodItemCount":2,"conversionRate":0,
     * "favRate":0,"burstItems":[],
     * "potentionItems":[{"id":20778652347,"fullTitle":"伊暖儿usb豪华版超大暖手鼠标垫 冬季保暖必备 卡拉 正品特价","tradeNum":1,"periodSoldQuantity":0,"price":2390,"sellerId":83753542,"picPath":"http://img04.taobaocdn.com/bao/uploaded/i8/T1XFnYXaNlXXc26R6a_121918.jpg","score":80},{"id":20697520284,"fullTitle":"特价正品 伊暖儿USB远红外暖手鼠标垫 冬季保暖必备神器 黄色维尼","tradeNum":1,"periodSoldQuantity":0,"price":1590,"sellerId":83753542,"picPath":"http://img04.taobaocdn.com/bao/uploaded/i4/T1GKj1XjlhXXXIGBs._113251.jpg",
     * "score":222}]}
     * 1 --> titleScore 标题得分， batTitleCount 标题得分较差的数量
     * 2 --> goodItemCount 爆款数量,burstItems 爆款宝贝列表
     * 3 -->potentialGoodItemCount，潜在爆款数量, potentionItems :  潜在爆款宝贝
     * 4 --> conversionRate 爆款转化率 
     * 5 --> inBadTimeCount 上下架分布不合理宝贝数目, weekDistributed 一周内上下架分布
     * 6 --> windowsUsage 橱窗利用率, remainWindowCount 剩余橱窗总数
     * 7 --> tradeCount 最近一个月订单量
     * @throws IOException
     */
    public static void shop() throws IOException {
        renderMockFileInJsonIfDev("diag.shop.json");
        User user = getUser();
        String diagJson = DiagAction.buildUserShopDiag(user, null, null);
        renderJSON(diagJson);
    }

    @NoTransaction
    public static void getAllDayOverThousandUvItem(String day, int threhold) {
    	day = StringUtils.trim(day);
    	if(StringUtils.isEmpty(day)) {
    		day = sdf.format(System.currentTimeMillis() - DateUtil.DAY_MILLIS);
    	}
    	if(threhold <= 0) {
    		threhold = 1000;
    	}
    	User user = UserDao.findByUserNick("clorest510");
    	int offset = 0, limit = 100;
    	List<QueryRow> wordsRows = new ArrayList<QueryRow>();
    	List<QueryRow> tmpRows = new ArrayList<QueryRow>();
    	TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(108417L,"startdate=" + day + ",enddate=" + day + ",threhold=" + threhold + ",sub_offset=" + offset +",sub_limit=" + limit, user.getSessionKey()).call();
    	tmpRows = res.getRes();
    	while(!CommonUtils.isEmpty(tmpRows)) {
    		offset += limit;
    		wordsRows.addAll(tmpRows);
    	}
    	for(QueryRow row : wordsRows) {
    		List<String> values = row.getValues();
    		if(CommonUtils.isEmpty(values)) {
    			continue;
    		}
    		Long userId = 0L, numIid = 0L, iuv =0L;
    		if(values.size() == 5) {
    			userId = Long.valueOf(values.get(0));
	    		numIid = Long.valueOf(values.get(1));
	    		iuv = Long.valueOf(values.get(3));
    		} else if(values.size() == 6){
	    		userId = Long.valueOf(values.get(1));
	    		numIid = Long.valueOf(values.get(2));
	    		iuv = Long.valueOf(values.get(4));
    		}
    		User tmpUser = UserDao.findById(userId);
    		if(tmpUser == null) {
    			continue;
    		}
    		Item item = new ItemApi.ItemFullGet(tmpUser, numIid).call();
    		if(item == null) {
    			continue;
    		}
    		if(item.getCreated().getTime() < System.currentTimeMillis() - DateUtil.DAY_MILLIS * 30) {
    			continue;
    		}
    		new DayOverThousandUvItem(numIid, userId, iuv, day, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(item.getCreated().getTime())).rawInsert();
    	}
    	renderText("chenggong");
    }
    
    /**
     * 参考生意经和量子为主
     */
    public static void sources() {

    }

    /**
     * 宝贝流量诊断
     */
    public static void itemSource() {
    }

    public static void searchDiag() {
    	User user =  getUser();
    	boolean needRedirect = NewUvPvDiagAction.isNeedRedirect(user);
    	if(needRedirect) {
    		redirect(App.CONTAINER_TAOBAO_URL);
    	}
        render("searchDiag/searchDiag.html");
    }

    public static void shopSearchWords() {
    	User user =  getUser();
    	boolean needRedirect = NewUvPvDiagAction.isNeedRedirect(user);
    	if(needRedirect) {
    		redirect(App.CONTAINER_TAOBAO_URL);
    	}
        render("searchDiag/shopSearchWords.html");
    }
    
    public static void appShopSearchWords() {
    	User user =  getUser();
    	boolean needRedirect = NewUvPvDiagAction.isNeedRedirect(user);
    	if(needRedirect) {
    		redirect(App.CONTAINER_TAOBAO_URL);
    	}
        render("searchDiag/appShopSearchWords.html");
    }
    
    public static void shopViewTrade() {
    	User user =  getUser();
    	boolean needRedirect = NewUvPvDiagAction.isNeedRedirect(user);
    	if(needRedirect) {
    		redirect(App.CONTAINER_TAOBAO_URL);
    	}
        render("searchDiag/shopViewTrade.html");
    }

    public static void areaDiag() {
    	User user =  getUser();
    	boolean needRedirect = NewUvPvDiagAction.isNeedRedirect(user);
    	if(needRedirect) {
    		redirect(App.CONTAINER_TAOBAO_URL);
    	}
        render("searchDiag/areaDiag.html");
    }

    public static void commodityDiag() {
    	User user =  getUser();
    	boolean needRedirect = NewUvPvDiagAction.isNeedRedirect(user);
    	if(needRedirect) {
    		redirect(App.CONTAINER_TAOBAO_URL);
    	}
        render("diag/commodityDiag.html");
    }

    public static void shopSrcDiag() {
    	User user =  getUser();
    	boolean needRedirect = NewUvPvDiagAction.isNeedRedirect(user);
    	if(needRedirect) {
    		redirect(App.CONTAINER_TAOBAO_URL);
    	}
        render("diag/shopSrcDiag.html");
    }

    public static void shopEvent() {
        render("searchDiag/shopEvent.html");
    }

    public static void shopHourDiag() {
        render("searchDiag/shopHourDiag.html");
    }
    
    public static void viewItemShow(){
        render("searchDiag/viewItemShow.html");
    }

    public static void getWordDiagIndos() {
        List<DiagWordInfo> infos = new ArrayList<DiagWordInfo>();
        infos.add(new DiagWordInfo("女装", 244, 43, 2, 34, 20, 2, 0.12, 6, 8, 1256, 0.12));
        infos.add(new DiagWordInfo("男装", 34, 433, 24, 343, 80, 4, 0.12, 53, 76, 124567, 0.56));
        infos.add(new DiagWordInfo("袜子", 45, 643, 8, 534, 420, 542, 0.54, 566, 745, 5454324, 0.52));
        PageOffset po = new PageOffset(1, 10, 15);
        renderJSON(JsonUtil.getJson(new TMResult(infos, 34, po)));
    }

    public static void testInState() {
    	User user =  getUser();
    	TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(3299L, "startdate=20140225,enddate=20140303,sellerId=" + user.getId(),
                user.getSessionKey())
                .call();
    	List<QueryRow> rows = res.getRes();
    	renderJSON(JsonUtil.getJson(rows));
    }
    
    public static void testMBKApi() {
        User user = UserDao.findByUserNick("clorest510");
        if (user == null) {
            renderText("用户不存在");
        }
        TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(2536L, "thedate=20131215,sellerId=" + user.getId(),
                user.getSessionKey())
                .call();
        List<QueryRow> rows = res.getRes();
        if (rows == null) {
            renderText("返回结果为null");
        } else if (CommonUtils.isEmpty(rows)) {
            renderText("返回结果为空");
        } else {
            Map<Integer, Integer> map = new HashMap<Integer, Integer>();
            for (QueryRow row : rows) {
                Integer srcId = Integer.valueOf(row.getValues().get(3));
                Integer uv = Integer.valueOf(row.getValues().get(6));
                //log.info("" + Integer.valueOf(row.getValues().get(5)));
                if (Integer.valueOf(row.getValues().get(5)) == 8) {
                    if (map.get(srcId) == null) {
                        map.put(srcId, uv);
                    } else {
                        map.put(srcId, uv + map.get(srcId));
                    }

                }

            }
            renderJSON(JsonUtil.getJson(map));
        }

    }

    public static void testMBKApiWithParams(String params) {
        User user = UserDao.findByUserNick("楚之小南");
        if (user == null) {
            renderText("用户不存在");
        }
        TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(2523L, params, user.getSessionKey())
                .call();
        List<QueryRow> rows = res.getRes();
        if (rows == null) {
            renderText("返回结果为null");
        } else if (CommonUtils.isEmpty(rows)) {
            renderText("返回结果为空");
        } else {
            List<UserDailyUvPv> results = new ArrayList<UserDailyUvPv>();
            for (QueryRow row : rows) {
                results.add(new UserDailyUvPv(row));
            }
            renderJSON(JsonUtil.getJson(results));
        }

    }

    public static void lastWeekUV(int interval, Long endTime) {
        if (interval < 1) {
            interval = 7;
        }
        if (endTime == null) {
            endTime = System.currentTimeMillis();
        }
        User user = getUser();
        if (user == null) {
            new HashMap<Integer, Integer>();
        }
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        for (int i = 1; i <= interval; i++) {
            String day = sdf.format(new Date(endTime - i * DateUtil.DAY_MILLIS));
            TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(2537L, "thedate=" + day + ",sellerId=" + user.getId(),
                    user.getSessionKey())
                    .call();
            List<QueryRow> rows = res.getRes();
            Integer uv = 0;
            if (CommonUtils.isEmpty(rows)) {
                map.put(i, null);
                continue;
            }
            for (QueryRow row : rows) {
                uv += getPCSearchUv(row);
            }
            map.put(i, uv);
            /*if(i == 1) {
            	int allUv = 0;
            	for(QueryRow row : rows) {
            		allUv += getPCUv(row);
            	}
            	map.put(8, allUv);
            }*/
        }
        renderJSON(JsonUtil.getJson(map));
    }

    public static void lastWeekTrade(int interval, Long endTime) {
        if (interval < 1) {
            interval = 7;
        }
        if (endTime == null) {
            endTime = System.currentTimeMillis();
            ;
        }
        User user = getUser();
        if (user == null) {
            new HashMap<Integer, Integer>();
        }
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        for (int i = 1; i <= interval; i++) {
            String day = sdf.format(new Date(endTime - i * DateUtil.DAY_MILLIS));
            TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(2536L, "thedate=" + day + ",sellerId=" + user.getId(),
                    user.getSessionKey())
                    .call();
            List<QueryRow> rows = res.getRes();
            Integer trade = 0;
            if (CommonUtils.isEmpty(rows)) {
                map.put(i, null);
                continue;
            }
            for (QueryRow row : rows) {
                trade += getTradeUv(row);
            }
            map.put(i, trade);
        }
        renderJSON(JsonUtil.getJson(map));
    }

    public static Integer getPCSearchUv(QueryRow row) {

        if (Integer.valueOf(row.getValues().get(4)) == 1) {
            return 0;
        }
        if (Integer.valueOf(row.getValues().get(3)) != 20) {
            return 0;
        }
        return Integer.valueOf(row.getValues().get(5));
    }

//    public static Integer getPCUv(QueryRow row) {
//
//        if (Integer.valueOf(row.getValues().get(4)) == 1) {
//            return 0;
//        }
//        return Integer.valueOf(row.getValues().get(5));
//    }

    public static Integer getTradeUv(QueryRow row) {
        if (Integer.valueOf(row.getValues().get(3)) != 20) {
            return 0;
        }
        return Integer.valueOf(row.getValues().get(11));
    }

    public static void shopWordsDiag(int pn, int ps, int interval, Long endTime, String orderBy, boolean isDesc) {
        if (interval < 1) {
            interval = 7;
        }
        if (endTime == null) {
            endTime = System.currentTimeMillis();
            ;
        }
        orderBy = ensureOrderBy(orderBy);
        User user = getUser();
        if (user == null) {
            renderFailedJson("用户不存在");
        }
        daysBetween(user, interval);
        if (needReduceEndTime(endTime)) {
            endTime -= DateUtil.DAY_MILLIS;
            interval--;
        }
        PageOffset po = new PageOffset(pn, ps, 10);
        Long startTime = endTime - (interval - 1) * DateUtil.DAY_MILLIS;
        int count = 0;
        List<WordInfo> wordInfos = new ArrayList<WordInfo>();

        TMResult<List<QueryRow>> res = new TMResult<List<QueryRow>>();
        if(isDesc){
            res = new MBPApi.MBPDataGet(3337L,
                    "startdate=" + sdf.format(new Date(startTime)) + ",sellerId=" + user.getId() +
                    ",enddate=" + sdf.format(new Date(endTime)) + ",sub_offset=" + po.getOffset() +
                    ",sub_limit=" + po.getPs() + ",sub_order_by=" + orderBy, user.getSessionKey()).call();
        } else {
            res = new MBPApi.MBPDataGet(106801L,
                    "startdate=" + sdf.format(new Date(startTime)) + ",sellerId=" + user.getId() +
                    ",enddate=" + sdf.format(new Date(endTime)) + ",sub_offset=" + po.getOffset() +
                    ",sub_limit=" + po.getPs() + ",sub_order_by=" + orderBy, user.getSessionKey()).call();
        }
        List<QueryRow> wordsRows = res.getRes();
        if (CommonUtils.isEmpty(wordsRows)) {
            renderJSON(JsonUtil.getJson(new TMResult(true, po.getPn(), po.getPs(),
                    count, "", wordInfos)));
        }
        for (QueryRow row : wordsRows) {
            wordInfos.add(WordInfo.buildWithNoDate(row));
        }
        TMResult<List<QueryRow>> wordsCountRes = new MBPApi.MBPDataGet(2854L,
                "startdate=" + sdf.format(new Date(startTime)) + ",sellerId=" + user.getId() +
                        ",enddate=" + sdf.format(new Date(endTime)), user.getSessionKey())
                .call();
        List<QueryRow> wordsCountRows = wordsCountRes.getRes();
        if (CommonUtils.isEmpty(wordsCountRows)) {
            count = 0;
        } else {
            count = wordsCountRows.size();
        }

        renderJSON(JsonUtil.getJson(new TMResult(true, po.getPn(), po.getPs(),
                count, "", wordInfos)));
    }
    
    public static void appShopWordsDiag(int pn, int ps, int interval, Long endTime, String orderBy, boolean isDesc) {
        if (interval < 1) {
            interval = 7;
        }
        if (endTime == null) {
            endTime = System.currentTimeMillis();
        }
        orderBy = ensureAppOrderBy(orderBy);
        User user = getUser();
        if (user == null) {
            renderFailedJson("用户不存在");
        }
        daysBetween(user, interval);
        if (needReduceEndTime(endTime)) {
            endTime -= DateUtil.DAY_MILLIS;
            interval--;
        }

        PageOffset po = new PageOffset(pn, ps, 10);
        Long startTime = endTime - (interval - 1) * DateUtil.DAY_MILLIS;
        int count = 0;
        List<WordInfo> wordInfos = new ArrayList<WordInfo>();
        TMResult<List<QueryRow>> res = new TMResult<List<QueryRow>>();
        if(isDesc){
            res = new MBPApi.MBPDataGet(100496L,
                    "startdate=" + sdf.format(new Date(startTime)) + ",sellerId=" + user.getId() +
                            ",enddate=" + sdf.format(new Date(endTime)) + ",sub_offset=" + po.getOffset() +
                            ",sub_limit=" + po.getPs() + ",sub_order_by=" + orderBy, user.getSessionKey()).call();
        } else {
            res = new MBPApi.MBPDataGet(106800L,
                    "startdate=" + sdf.format(new Date(startTime)) + ",sellerId=" + user.getId() +
                            ",enddate=" + sdf.format(new Date(endTime)) + ",sub_offset=" + po.getOffset() +
                            ",sub_limit=" + po.getPs() + ",sub_order_by=" + orderBy, user.getSessionKey()).call();
        }
        List<QueryRow> wordsRows = res.getRes();
        if (CommonUtils.isEmpty(wordsRows)) {
            renderJSON(JsonUtil.getJson(new TMResult(true, po.getPn(), po.getPs(),
                    count, "", wordInfos)));
        }
        for (QueryRow row : wordsRows) {
            wordInfos.add(WordInfo.buildAppWithNoDate(row));
        }
        TMResult<List<QueryRow>> wordsCountRes = new MBPApi.MBPDataGet(100530L,
                "startdate=" + sdf.format(new Date(startTime)) + ",sellerId=" + user.getId() +
                        ",enddate=" + sdf.format(new Date(endTime)), user.getSessionKey())
                .call();
        List<QueryRow> wordsCountRows = wordsCountRes.getRes();
        if (CommonUtils.isEmpty(wordsCountRows)) {
            count = 0;
        } else {
        	List<String> values = wordsCountRows.get(0).getValues();
        	if(CommonUtils.isEmpty(values) == false) {
        		count = StringUtils.isEmpty(values.get(0)) ? 0 : Integer.valueOf(values.get(0));
        	}
        }

        renderJSON(JsonUtil.getJson(new TMResult(true, po.getPn(), po.getPs(),
                count, "", wordInfos)));
    }

    public static void shopWordTrend(int interval, Long endTime, String word) {
        if (StringUtils.isEmpty(word)) {
            renderFailedJson("要查询的关键词为空");
        }
        if (interval < 1) {
            interval = 7;
        }
        if (endTime == null) {
            endTime = System.currentTimeMillis();
        }
        if (needReduceEndTime(endTime)) {
            endTime -= DateUtil.DAY_MILLIS;
            interval--;
        }
        User user = getUser();
        if (user == null) {
            renderFailedJson("用户不存在");
        }
        Long startTime = endTime - (interval - 1) * DateUtil.DAY_MILLIS;

        TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(2835L,
                "startdate=" + sdf.format(new Date(startTime)) + ",sellerId=" + user.getId() +
                        ",enddate=" + sdf.format(new Date(endTime)) + ",word=" + word, user.getSessionKey()).call();
        List<QueryRow> wordsRows = res.getRes();
        if (CommonUtils.isEmpty(wordsRows)) {
            renderFailedJson("查不到该关键词数据");
        }
        Map<String, String> results = new HashMap<String, String>();
        for (QueryRow row : wordsRows) {
            List<String> values = row.getValues();
            results.put(values.get(0), values.get(3));
        }
        renderJSON(JsonUtil.getJson(results));

    }

    public static void appShopWordTrend(int interval, Long endTime, String word) {
        if (StringUtils.isEmpty(word)) {
            renderFailedJson("要查询的关键词为空");
        }
        if (interval < 1) {
            interval = 7;
        }
        if (endTime == null) {
            endTime = System.currentTimeMillis();
        }
        if (needReduceEndTime(endTime)) {
            endTime -= DateUtil.DAY_MILLIS;
            interval--;
        }

        User user = getUser();
        if (user == null) {
            renderFailedJson("用户不存在");
        }
        Long startTime = endTime - (interval - 1) * DateUtil.DAY_MILLIS;

        TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(100532L,
                "startdate=" + sdf.format(new Date(startTime)) + ",sellerId=" + user.getId() +
                        ",enddate=" + sdf.format(new Date(endTime)) + ",word=" + word, user.getSessionKey()).call();
        List<QueryRow> wordsRows = res.getRes();
        if (CommonUtils.isEmpty(wordsRows)) {
            renderFailedJson("查不到该关键词数据");
        }
        Map<String, String> results = new HashMap<String, String>();
        for (QueryRow row : wordsRows) {
            List<String> values = row.getValues();
            results.put(values.get(0), values.get(3));
        }
        renderJSON(JsonUtil.getJson(results));

    }
    
    public static class WordDayPv {
        public String thedate;

        public String pv;

        public String word;

        public String getThedate() {
            return thedate;
        }

        public void setThedate(String thedate) {
            this.thedate = thedate;
        }

        public String getPv() {
            return pv;
        }

        public void setPv(String pv) {
            this.pv = pv;
        }

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public WordDayPv(String thedate, String pv, String word) {
            super();
            this.thedate = thedate;
            this.pv = pv;
            this.word = word;
        }

        public WordDayPv(QueryRow row) {
            super();
            if (row != null) {
                List<String> values = row.getValues();
                this.thedate = values.get(0);
                this.word = values.get(2);
                this.pv = values.get(3);
            }
        }

    }

    public static String ensureOrderBy(String orderBy) {
        if (StringUtils.isEmpty(orderBy)) {
            return "impression";
        }
        if (orderBy.equals("impression") || orderBy.equals("click") || orderBy.equals("uv")
                || orderBy.equals("alipay_winner_num") || orderBy.equals("alipay_trade_num")
                || orderBy.equals("alipay_trade_amt") || orderBy.equals("alipay_auction_num")) {
            return orderBy;
        }
        return "impression";
    }
    
    public static String ensureAppOrderBy(String orderBy) {
        if (StringUtils.isEmpty(orderBy)) {
            return "pv";
        }
        if (orderBy.equals("pv") || orderBy.equals("uv") || orderBy.equals("direct_alipay_winner_num")
                || orderBy.equals("direct_alipay_trade_num") || orderBy.equals("direct_alipay_trade_amt")
                || orderBy.equals("direct_alipay_auction_num")) {
            return orderBy;
        }
        return "pv";
    }

    public static void relativeAccessByNumIid(Long numIid, int interval, Long endTime) {
        if (interval < 1) {
            interval = 7;
        }
        if (endTime == null) {
            endTime = System.currentTimeMillis();
        }
        if (numIid == null) {
            renderFailedJson("宝贝ID为空");
        }
        User user = getUser();
        if (user == null) {
            renderFailedJson("用户不存在");
        }
        daysBetween(user, interval);
        if (needReduceEndTime(endTime)) {
            endTime -= DateUtil.DAY_MILLIS;
            interval--;
        }

        Long startTime = endTime - (interval - 1) * DateUtil.DAY_MILLIS;
        TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(3050L,
                "startdate=" + sdf.format(new Date(startTime)) + ",numIid=" + numIid +
                        ",sellerId=" + user.getId() +
                        ",enddate=" + sdf.format(new Date(endTime)), user.getSessionKey()).call();
        List<QueryRow> rows = res.getRes();
        if (CommonUtils.isEmpty(rows)) {
            renderFailedJson("御膳房返回数据为空");
        }
        QueryRow row = rows.get(0);
        ItemRelativeAccess access = new ItemRelativeAccess(row);
        renderJSON(JsonUtil.getJson(access));
    }

    public static void diagItem(Long numIid, int pn, int ps, int interval, Long endTime,
            String orderBy) {
        if (interval < 1) {
            interval = 7;
        }
        if (endTime == null) {
            endTime = System.currentTimeMillis();
        }
        orderBy = ensureOrderBy(orderBy);
        User user = getUser();
        daysBetween(user, interval);
        if (needReduceEndTime(endTime)) {
            endTime -= DateUtil.DAY_MILLIS;
            interval--;
        }
        if (user == null) {
            renderFailedJson("用户不存在");
        }
        if (numIid == null) {
            RecentlyDiagedItem recently = RecentlyDiagedItem.findMostRecently(user.getId());
            if (recently != null) {
                numIid = recently.getNumIid();
            } else {
                ItemPlay firstItemPlay = ItemDao.findFirstItemByUserId(user.getId());
                if (firstItemPlay == null) {
                    renderFailedJson("当前用户未存在宝贝");
                }
                numIid = firstItemPlay.getNumIid();
            }

        }
        ItemPlay itemPlay = ItemDao.findByNumIid(user.getId(), numIid);
        new RecentlyDiagedItem(numIid, user.getId(), System.currentTimeMillis(),
                itemPlay.getPicURL()).jdbcSave();
        PageOffset po = new PageOffset(pn, ps, 10);
        TMResult<List<QueryRow>> res = new TMResult<List<QueryRow>>();
        /*for(int i = 1; i<= interval; i++) {
        	String day = sdf.format(new Date(endTime - i * DateUtil.DAY_MILLIS));
        	List<QueryRow> tmprows = new MBPApi.MBPDataGet(2544L, 
        		"thedate="+day+",sellerId="+user.getId()+",numIid=" + numIid, user.getSessionKey())
        		.call();
        	if(CommonUtils.isEmpty(tmprows)) {
        		continue;
        	}
        	rows.addAll(tmprows);
        }*/
        Long startTime = endTime - (interval - 1) * DateUtil.DAY_MILLIS;
        res = new MBPApi.MBPDataGet(2725L,
                "startdate=" + sdf.format(new Date(startTime)) + ",numIid=" + numIid +
                        ",sellerId=" + user.getId() +
                        ",enddate=" + sdf.format(new Date(endTime)) + ",sub_offset=" + po.getOffset() +
                        ",sub_limit=" + po.getPs() + ",sub_order_by=" + orderBy, user.getSessionKey()).call();

        List<QueryRow> rows = res.getRes();
        List<WordInfo> wordInfos = new ArrayList<WordInfo>();
        if (CommonUtils.isEmpty(rows)) {
            renderJSON(JsonUtil.getJson(new TMResult(wordInfos, 0, po)));
        }
        // 计算该宝贝时间段内所有入店关键词总数
        TMResult<List<QueryRow>> wordsCountRes = new MBPApi.MBPDataGet(2853L,
                "startdate=" + sdf.format(new Date(startTime)) + ",sellerId=" + user.getId() +
                        ",enddate=" + sdf.format(new Date(endTime)) + ",numIid=" + numIid, user.getSessionKey())
                .call();
        List<QueryRow> wordsCountRows = wordsCountRes.getRes();
        int count = 0;
        if (!CommonUtils.isEmpty(wordsCountRows)) {
            count = wordsCountRows.size();
        }

        // 计算该宝贝时间段内的uv总数和支付宝成功付款人数
        TMResult<List<QueryRow>> uvpvRes = new MBPApi.MBPDataGet(2673L,
                "startdate=" + sdf.format(new Date(startTime)) + ",sellerId=" + user.getId() +
                        ",enddate=" + sdf.format(new Date(endTime)) + ",numIid=" + numIid, user.getSessionKey())
                .call();
        List<QueryRow> uvpvRows = uvpvRes.getRes();
        QueryRow uvpvRow = uvpvRows.get(0);
        int totalUv = Integer.valueOf(uvpvRow.getValues().get(0));
        int totalTrade = Integer.valueOf(uvpvRow.getValues().get(1));

        for (QueryRow row : rows) {
            wordInfos.add(WordInfo.buildWithNoDate(row));
        }
        renderJSON(JsonUtil.getJson(new TMResult(true, po.getPn(), po.getPs(),
                count, totalUv + "," + totalTrade, wordInfos)));

    }
    
    public static void diagAppItem(Long numIid, int pn, int ps, int interval, Long endTime,
            String orderBy) {
        if (interval < 1) {
            interval = 7;
        }
        if (endTime == null) {
            endTime = System.currentTimeMillis();
        }
        orderBy = ensureAppOrderBy(orderBy);
        User user = getUser();
        if (user == null) {
            renderFailedJson("用户不存在");
        }
        if (numIid == null) {
            renderFailedJson("请输入正确的宝贝ID");
        }
        daysBetween(user, interval);
        if (needReduceEndTime(endTime)) {
            endTime -= DateUtil.DAY_MILLIS;
            interval--;
        }

        ItemPlay itemPlay = ItemDao.findByNumIid(user.getId(), numIid);
        new RecentlyDiagedItem(numIid, user.getId(), System.currentTimeMillis(),
                itemPlay.getPicURL()).jdbcSave();
        PageOffset po = new PageOffset(pn, ps, 10);
        TMResult<List<QueryRow>> res = new TMResult<List<QueryRow>>();
        Long startTime = endTime - (interval - 1) * DateUtil.DAY_MILLIS;
        res = new MBPApi.MBPDataGet(100495L,
                "startdate=" + sdf.format(new Date(startTime)) + ",numIid=" + numIid +
                        ",sellerId=" + user.getId() +
                        ",enddate=" + sdf.format(new Date(endTime)) + ",sub_offset=" + po.getOffset() +
                        ",sub_limit=" + po.getPs() + ",sub_order_by=" + orderBy, user.getSessionKey()).call();

        List<QueryRow> rows = res.getRes();
        List<WordInfo> wordInfos = new ArrayList<WordInfo>();
        if (CommonUtils.isEmpty(rows)) {
            renderJSON(JsonUtil.getJson(new TMResult(wordInfos, 0, po)));
        }
        // 计算该宝贝时间段内所有入店关键词总数
        TMResult<List<QueryRow>> wordsCountRes = new MBPApi.MBPDataGet(100531L,
                "startdate=" + sdf.format(new Date(startTime)) + ",sellerId=" + user.getId() +
                        ",enddate=" + sdf.format(new Date(endTime)) + ",numIid=" + numIid, user.getSessionKey())
                .call();
        List<QueryRow> wordsCountRows = wordsCountRes.getRes();
        int count = 0;
        if (!CommonUtils.isEmpty(wordsCountRows)) {
        	List<String> values = wordsCountRows.get(0).getValues();
        	if(CommonUtils.isEmpty(values) == false) {
        		count = StringUtils.isEmpty(values.get(0)) ? 0 : Integer.valueOf(values.get(0));
        	}
        }

        for (QueryRow row : rows) {
            wordInfos.add(WordInfo.buildAppWithNoDate(row));
        }
        renderJSON(JsonUtil.getJson(new TMResult(true, po.getPn(), po.getPs(),
                count, "", wordInfos)));

    }

    public static void shopTranrate(int platform, int interval, Long endTime) {
        User user = getUser();
        if (user == null) {
            renderFailedJson("用户不存在");
        }
        if (platform != 0 && platform != 1 && platform != 2) {
            platform = 0;
        }
        if (endTime == null) {
            endTime = System.currentTimeMillis();
            ;
        }
        Long startTime = endTime - (interval - 1) * DateUtil.DAY_MILLIS;
        TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(3049L,
                "startdate=" + sdf.format(new Date(startTime)) + ",sellerId=" + user.getId() +
                        ",enddate=" + sdf.format(new Date(endTime)) + ",platform=" + platform +
                        ",@replace_null=NULL", user.getSessionKey())
                .call();
        List<QueryRow> viewTrade = res.getRes();
        if (CommonUtils.isEmpty(viewTrade)) {
            renderFailedJson("找不到对应的流量成交数据");
        }
        Map<String, AreaViewsAndTrades> map = new HashMap<String, AreaViewsAndTrades>();
        for (QueryRow row : viewTrade) {
            List<String> value = row.getValues();
            // 这是计算每日数据
            AreaViewsAndTrades tmp = new AreaViewsAndTrades();
            tmp.AddProp(value.get(3).equals("NULL") ? 0 : Integer.valueOf(value.get(3)),
                    value.get(4).equals("NULL") ? 0 : Integer.valueOf(value.get(4)),
                    value.get(7).equals("NULL") ? 0 : Integer.valueOf(value.get(7)),
                    value.get(12).equals("NULL") ? 0 : Integer.valueOf(value.get(12)),
                    value.get(8).equals("NULL") ? 0 : Integer.valueOf(value.get(8)),
                    value.get(9).equals("NULL") ? 0 : Integer.valueOf(value.get(9)),
                    value.get(10).equals("NULL") ? 0 : Float.valueOf(value.get(10)),
                    value.get(11).equals("NULL") ? 0 : Integer.valueOf(value.get(11)));
            if (map.get(value.get(13)) == null) {
                map.put(value.get(13), tmp);
            } else {
                AreaViewsAndTrades old = (AreaViewsAndTrades) map.get(value.get(13));
                old.AddProp(tmp);
                map.put(value.get(13), old);
            }

        }
        // 这个是用0填充没有数据的日期
        for (int i = 0; i < interval; i++) {
            String dateStr = sdf.format(new Date(endTime - (interval - i) * DateUtil.DAY_MILLIS));
            if (map.get(dateStr) == null) {
                map.put(dateStr, new AreaViewsAndTrades());
            }
        }
        renderJSON(JsonUtil.getJson(map));
    }

    public static void shopPCWirelessViewTrade(int platform, int interval, Long endTime) {
        User user = getUser();
        if (user == null) {
            renderFailedJson("用户不存在");
        }
        if (platform != 0 && platform != 1 && platform != 2) {
            platform = 0;
        }
        if (endTime == null) {
            endTime = System.currentTimeMillis();
            
        }
        daysBetween(user, interval);
        // 这里，每天早上8点才会显示昨天的数据，否则仍然显示前天的数据
        if (needReduceEndTime(endTime)) {
            endTime -= DateUtil.DAY_MILLIS;
            interval--;
        }

        Long startTime = endTime - (interval - 1) * DateUtil.DAY_MILLIS;
        TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(3049L,
                "startdate=" + sdf.format(new Date(startTime)) + ",sellerId=" + user.getId() +
                        ",enddate=" + sdf.format(new Date(endTime)) + ",platform=" + platform +
                        ",@replace_null=NULL", user.getSessionKey())
                .call();
        List<QueryRow> viewTrade = res.getRes();
        if (CommonUtils.isEmpty(viewTrade)) {
            renderFailedJson("找不到对应的流量成交数据");
        }
        AreaViewsAndTrades result = new AreaViewsAndTrades();
        Map<String, AreaViewsAndTrades> map = new TreeMap<String, AreaViewsAndTrades>();
        for (QueryRow row : viewTrade) {
            List<String> value = row.getValues();
            // 这是计算聚合数据
            result.AddProp(value.get(3).equals("NULL") ? 0 : Integer.valueOf(value.get(3)),
                    value.get(4).equals("NULL") ? 0 : Integer.valueOf(value.get(4)),
                    value.get(7).equals("NULL") ? 0 : Integer.valueOf(value.get(7)),
                    value.get(12).equals("NULL") ? 0 : Integer.valueOf(value.get(12)),
                    value.get(8).equals("NULL") ? 0 : Integer.valueOf(value.get(8)),
                    value.get(9).equals("NULL") ? 0 : Integer.valueOf(value.get(9)),
                    value.get(10).equals("NULL") ? 0 : Float.valueOf(value.get(10)),
                    value.get(11).equals("NULL") ? 0 : Integer.valueOf(value.get(11)));
            // 这是计算每日数据
            AreaViewsAndTrades tmp = new AreaViewsAndTrades();
            tmp.AddProp(value.get(3).equals("NULL") ? 0 : Integer.valueOf(value.get(3)),
                    value.get(4).equals("NULL") ? 0 : Integer.valueOf(value.get(4)),
                    value.get(7).equals("NULL") ? 0 : Integer.valueOf(value.get(7)),
                    value.get(12).equals("NULL") ? 0 : Integer.valueOf(value.get(12)),
                    value.get(8).equals("NULL") ? 0 : Integer.valueOf(value.get(8)),
                    value.get(9).equals("NULL") ? 0 : Integer.valueOf(value.get(9)),
                    value.get(10).equals("NULL") ? 0 : Float.valueOf(value.get(10)),
                    value.get(11).equals("NULL") ? 0 : Integer.valueOf(value.get(11)));
            if (map.get(value.get(13)) == null) {
                map.put(value.get(13), tmp);
            } else {
                AreaViewsAndTrades old = (AreaViewsAndTrades) map.get(value.get(13));
                old.AddProp(tmp);
                map.put(value.get(13), old);
            }

        }
        
        // 无数据的日子，用0补
        for (int i = 0; i < interval; i++) {
            String dateStr = sdf.format(new Date(endTime - (interval - i) * DateUtil.DAY_MILLIS));
            if (map.get(dateStr) == null) {
                map.put(dateStr, new AreaViewsAndTrades());
            }
        }
        
        // 设置宝贝的站内搜索searchUv跟search_alipay_winner_num
        TMResult<List<QueryRow>> searchUvWinnnersRes = new MBPApi.MBPDataGet(3274L,
                "startdate=" + sdf.format(new Date(startTime)) + ",sellerId=" + user.getId() +
                        ",enddate=" + sdf.format(new Date(endTime)) + ",platform=" + platform +
                        ",@replace_null=NULL", user.getSessionKey())
                .call();
        List<QueryRow> searchUvWinnners = searchUvWinnnersRes.getRes();
        if(!CommonUtils.isEmpty(searchUvWinnners)) {
        	for(QueryRow uvWinner : searchUvWinnners) {
        		List<String> values = uvWinner.getValues();
        		if(CommonUtils.isEmpty(values)) {
        			continue;
        		}
        		AreaViewsAndTrades noSearch = map.get(values.get(0));
        		if(noSearch == null) {
        			continue;
        		}
        		noSearch.setSearchUv(values.get(1));
        		noSearch.setSearch_alipay_winner_num(values.get(2));
        	}
        }
        
        Object[] objs = new Object[] {
                result, map
        };
        renderJSON(JsonUtil.getJson(objs));
    }

    private static Boolean needReduceEndTime(Long endTime) {
        if (endTime == null) return false;

        Long currentTimestamp = System.currentTimeMillis();
        // 今天0点的时间戳
        Long currentDateZero = utils.DateUtil.formDailyTimestamp(currentTimestamp);
        // 如果endTime是当前时间的前一天&&今天当前时间小于8点
        if (endTime >= currentDateZero - DateUtil.DAY_MILLIS && endTime < currentDateZero && !utils.DateUtil.isTimestampGTEight(currentTimestamp)) {
            return true;
        } else {
            return false;
        }
    }

    public static void ItemPlayPCWirelessViewTrade(int platform, int interval, Long endTime,
    		Long numIid) throws IOException {
//        renderMockFileInJsonIfDev("itemplaypcwirelessviewtrade.json");

    	if(numIid == null || numIid <= 0) {
    		renderFailedJson("numIid为空或小雨0");
    	}
        User user = getUser();
        if (user == null) {
            renderFailedJson("用户不存在");
        }
        if (platform != 0 && platform != 1 && platform != 2) {
            platform = 0;
        }
        if (endTime == null) {
            endTime = System.currentTimeMillis();
        }
        daysBetween(user, interval);
        if (needReduceEndTime(endTime)) {
            endTime -= DateUtil.DAY_MILLIS;
            interval--;
        }
        Map<String, AreaViewsAndTrades> map = new TreeMap<String, AreaViewsAndTrades>();
        Long startTime = endTime - (interval - 1) * DateUtil.DAY_MILLIS;
        TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(3256L,
                "startdate=" + sdf.format(new Date(startTime)) + ",sellerId=" + user.getId() +
                        ",enddate=" + sdf.format(new Date(endTime)) + ",platform=" + platform +
                        ",numIid=" + numIid + ",@replace_null=NULL", user.getSessionKey())
                .call();
        List<QueryRow> viewTrade = res.getRes();
        // 如果没数据，则默认返回全为0
        if (CommonUtils.isEmpty(viewTrade)) {
        	for (int i = 0; i < interval; i++) {
                String dateStr = sdf.format(new Date(endTime - (interval - i) * DateUtil.DAY_MILLIS));
                map.put(dateStr, new AreaViewsAndTrades());
            }
        	renderJSON(JsonUtil.getJson(map));
        }
        
        for (QueryRow row : viewTrade) {
            List<String> value = row.getValues();

            // 这是计算每日数据
            AreaViewsAndTrades tmp = new AreaViewsAndTrades();
            tmp.AddProp(value.get(2).equals("NULL") ? 0 : Integer.valueOf(value.get(2)),
                    value.get(3).equals("NULL") ? 0 : Integer.valueOf(value.get(3)),
                    value.get(4).equals("NULL") ? 0 : Integer.valueOf(value.get(4)),
                    value.get(5).equals("NULL") ? 0 : Integer.valueOf(value.get(5)),
                    value.get(6).equals("NULL") ? 0 : Float.valueOf(value.get(6)),
                    value.get(7).equals("NULL") ? 0 : Integer.valueOf(value.get(7)));
            if (map.get(value.get(8)) == null) {
                map.put(value.get(8), tmp);
            } else {
                AreaViewsAndTrades old = (AreaViewsAndTrades) map.get(value.get(8));
                old.AddProp(tmp);
                map.put(value.get(8), old);
            }

        }
        
        // 有些日子没数据，map里就没有对应记录，需要用0来填充
        for (int i = 0; i < interval; i++) {
            String dateStr = sdf.format(new Date(endTime - (interval - i) * DateUtil.DAY_MILLIS));
            if (map.get(dateStr) == null) {
                map.put(dateStr, new AreaViewsAndTrades());
            }
        }
        
        // 设置宝贝的站内搜索searchUv跟search_alipay_winner_num
        TMResult<List<QueryRow>> searchUvWinnnersRes = new MBPApi.MBPDataGet(3273L,
                "startdate=" + sdf.format(new Date(startTime)) + ",sellerId=" + user.getId() +
                        ",enddate=" + sdf.format(new Date(endTime)) + ",platform=" + platform +
                        ",numIid=" + numIid + ",@replace_null=NULL", user.getSessionKey())
                .call();
        List<QueryRow> searchUvWinnners = searchUvWinnnersRes.getRes();
        if(!CommonUtils.isEmpty(searchUvWinnners)) {
        	for(QueryRow uvWinner : searchUvWinnners) {
        		List<String> values = uvWinner.getValues();
        		if(CommonUtils.isEmpty(values)) {
        			continue;
        		}
        		AreaViewsAndTrades noSearch = map.get(values.get(0));
        		if(noSearch == null) {
        			continue;
        		}
        		noSearch.setSearchUv(values.get(1));
        		noSearch.setSearch_alipay_winner_num(values.get(2));
        	}
        }
        renderJSON(JsonUtil.getJson(map));
    }
    
    public static void ItemPCWirelessViewTrade(int platform, int interval, Long endTime, Long numIid) {
        if (numIid == null) {
            renderFailedJson("宝贝ID为空");
        }
        User user = getUser();
        if (user == null) {
            renderFailedJson("用户不存在");
        }
        if (platform != 0 && platform != 1 && platform != 2) {
            platform = 0;
        }
        if (endTime == null) {
            endTime = System.currentTimeMillis();
            ;
        }
        Long startTime = endTime - (interval - 1) * DateUtil.DAY_MILLIS;
        TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(2948L,
                "startdate=" + sdf.format(new Date(startTime)) + ",sellerId=" + user.getId() +
                        ",enddate=" + sdf.format(new Date(endTime)) + ",platform=" + platform +
                        ",numIid=" + numIid + ",@replace_null=NULL", user.getSessionKey())
                .call();
        List<QueryRow> viewTrade = res.getRes();
        if (CommonUtils.isEmpty(viewTrade)) {
            renderFailedJson("找不到对应的流量成交数据");
        }
        AreaViewsAndTrades result = new AreaViewsAndTrades();
        for (QueryRow row : viewTrade) {
            List<String> value = row.getValues();
            result.AddItemProp(value.get(4).equals("NULL") ? 0 : Integer.valueOf(value.get(4)),
                    value.get(5).equals("NULL") ? 0 : Integer.valueOf(value.get(5)),
                    value.get(6).equals("NULL") ? 0 : Integer.valueOf(value.get(6)),
                    value.get(7).equals("NULL") ? 0 : Integer.valueOf(value.get(7)),
                    value.get(8).equals("NULL") ? 0 : Float.valueOf(value.get(8)),
                    value.get(9).equals("NULL") ? 0 : Integer.valueOf(value.get(9)));
        }
        renderJSON(JsonUtil.getJson(result));
    }

    /**
     * @Description:	分析与宝贝标题相关的热词
     * @param numIid		Item Id
     * @return: void
     */
    public static void getItemTitleAnalysis(Long numIid, String sortBy, boolean isDesc, int pn, int ps) {
    	
    	User user = getUser();
        if (user == null) {
            renderFailedJson("用户不存在");
        }
        ItemPlay itemPlay;
        if (numIid == null) {
        	renderFailedJson("请传入宝贝ID");
        }
        itemPlay = ItemDao.findByNumIid(user.getId(), numIid);
        if (itemPlay == null) {
        	renderFailedJson("宝贝不存在");
        }
        PageOffset po = new PageOffset(pn, ps, 10);
        List<TitleAnalysisResult> wordInfos = new ArrayList<TitleAnalysisResult>();
        try {
        	ESSearchResult essResult = new TMApi.HotKeywordSearchApi(itemPlay.title, "0.2", po.getPn(), po.getPs(), sortBy, isDesc).execute();
        	if (!essResult.isSuccess()) {
        		renderJSON(JsonUtil.getJson(new TMResult(wordInfos, 0, po)));
        	}
        	List<WordHit> wordHits = essResult.getWordHits();
        	for(WordHit wordHit : wordHits) {
        		String name = wordHit.getName();
        		String word = wordHit.getWord();
        		String hotSearchDegree = String.valueOf(wordHit.getImpressions());
        		String relevancy = String.valueOf(wordHit.getScore());
        		wordInfos.add(new TitleAnalysisResult(name, word, hotSearchDegree, relevancy, user));
        	}
        	renderJSON(JsonUtil.getJson(new TMResult(wordInfos, (int) essResult.getTotal(), po)));
		} catch (ClientException e) {
			// 需返回前端
			renderFailedJson("后台服务错误，错误原因：" + e.getLocalizedMessage());
		}
        
    }
    
    public static void getItemInfo(Long numIid) {
        User user = getUser();
        if (user == null) {
            renderFailedJson("用户不存在");
        }
        if (numIid == null) {
            RecentlyDiagedItem recently = RecentlyDiagedItem.findMostRecently(user.getId());
            if (recently != null) {
                numIid = recently.getNumIid();
            } else {
                ItemPlay firstItemPlay = ItemDao.findFirstItemByUserId(user.getId());
                if (firstItemPlay == null) {
                    renderFailedJson("当前用户未存在宝贝");
                }
                renderJSON(JsonUtil.getJson(firstItemPlay));
            }

        }

        ItemPlay itemPlay = ItemDao.findByNumIid(user.getId(), numIid);
        if (itemPlay == null) {
            renderFailedJson("宝贝不存在");
        }
        renderJSON(JsonUtil.getJson(itemPlay));
    }

    public static void getRecentlyDiagedItems() {
        User user = getUser();
        if (user == null) {
            renderFailedJson("用户不存在");
        }
        List<RecentlyDiagedItem> recentlys = RecentlyDiagedItem.getRecentlyFour(user.getId());
        if (CommonUtils.isEmpty(recentlys)) {
            List<ItemPlay> items = ItemDao.findAllByUser(user.getId(), 0, 4, "", 0);
            if (CommonUtils.isEmpty(items)) {
                renderJSON(JsonUtil.getJson(new ArrayList<RecentlyDiagedItem>()));
            }
            for (ItemPlay itemPlay : items) {
                recentlys.add(new RecentlyDiagedItem(itemPlay));
            }
        }
        renderJSON(JsonUtil.getJson(recentlys));
    }

    public static void chooseItems(int pn, int ps, String s) {
        User user = getUser();

        PageOffset po = new PageOffset(pn, ps);
        TMResult tmRes = ItemDao.findWithOrder(user, s, 0, "", "", true, po);
        renderJSON(JsonUtil.getJson(tmRes));
    }

    static String PARAM_LAST_QUERY = "_last_q";

    public static void listDiagTMpage(String s, int pn, int ps, int sort,
            int status, String catId) throws IOException {
        if ("输入您的关键字".equals(s)) {
            s = null;
        }

        final User user = getUser();
        pn = pn < 1 ? 1 : pn;
        ps = ps < 1 ? PageSize.DISPLAY_ITEM_PAGE_SIZE : ps;
        session.put(PARAM_LAST_QUERY, s);

        List<ItemPlay> list = ItemDao.findOnlineByUserWithscoreAndCatid(user.getId(), (pn - 1) * ps, ps, s, 0L,
                0, 100, sort, status, catId, StringUtils.EMPTY);

        // List<ItemPlay> list = ItemDao.findOnlineByUser(user.getId(), (pn - 1) * ps, ps, s, sort);

        if (CommonUtils.isEmpty(list)) {
            renderFailedJson("宝贝列表为空");
        }

        List<UvPvDiagResult> res = new ArrayList<UvPvDiagResult>();
        List<FutureTask<UvPvDiagResult>> tasks = new ArrayList<FutureTask<UvPvDiagResult>>();

        // try {
        for (final ItemPlay item : list) {
            FutureTask<UvPvDiagResult> task = TMConfigs.getUvPvDiagResultPool().submit(new Callable<UvPvDiagResult>() {
                @Override
                public UvPvDiagResult call() throws Exception {
                    return UvPvDiagAction.doDiag(user, item, null);
                }
            });
            tasks.add(task);

        }

        for (FutureTask<UvPvDiagResult> task : tasks) {
            UvPvDiagResult doItem;
            try {
                doItem = task.get();
                res.add(doItem);
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }

        PageOffset po = new PageOffset(pn, ps, 5);

        TMResult tmRes = new TMResult(res, (int) ItemDao.countOnlineByUserWithArgs(user.getId(), 0,
                100, s, 0L,
                status, catId, null), po);
        renderJSON(JsonUtil.getJson(tmRes));
    }
	
	// 琳琅秀宝贝搜索
	public static void singleItemInfo(Long numIid) {
		if(numIid == null) {
			renderJSON(JsonUtil.getJson(new TMResult("宝贝Id为空！")));
		}

		final User user = getUser();
		
		final ItemPlay item = ItemDao.findByNumIid(user.getId(), numIid);
		
		if(item == null) {
			renderJSON(JsonUtil.getJson(new TMResult("找不到指定的宝贝，请重试或者联系我们！")));
		}
		
		UvPvDiagResult res = new UvPvDiagResult();
		
		FutureTask<UvPvDiagResult> task = TMConfigs.getUvPvDiagResultPool().submit(new Callable<UvPvDiagResult>() {
			@Override
			public UvPvDiagResult call() throws Exception {
				return UvPvDiagAction.doDiag(user, item, null);
			}
		});
		
		try {
			res = task.get();
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}

		renderJSON(JsonUtil.getJson(new TMResult(res)));
	}

	public static void newListDiagTMpage(String s, Long numIid, int pn, int ps, int sort, final int order, final boolean isDesc,
			int status, String catId, String taobaoCatId, int interval, Long endTime) throws IOException {
		if ("输入您的关键字".equals(s)) {
			s = null;
		}
		
		if (endTime == null) {
			endTime = DateUtil.formCurrDate() - 1;
		}
		
		if (needReduceEndTime(endTime)) {
			endTime -= DateUtil.DAY_MILLIS;
			interval--;
		}
		
		if (interval < 0) {
			interval = 1;
		}
		
		final User user = getUser();
		final Long end = endTime;
		final int day = interval;
		
		pn = pn < 1 ? 1 : pn;
		ps = ps < 1 ? PageSize.DISPLAY_ITEM_PAGE_SIZE : ps;
		session.put(PARAM_LAST_QUERY, s);
		
		int count = (int) ItemDao.countOnlineByUserWithArgs(user.getId(), 0, 100, s, numIid, status, catId, StringUtils.isEmpty(taobaoCatId)? null : Long.valueOf(taobaoCatId));
		List<ItemPlay> list = ItemDao.findOnlineByUserWithscoreAndCatid(user.getId(), (pn - 1) * ps, ps, s, numIid,
								0, 100, sort, status, catId, taobaoCatId);

		if (CommonUtils.isEmpty(list)) {
			renderFailedJson("宝贝列表为空");
		}

		List<NewUvPvDiagResult> res = new ArrayList<NewUvPvDiagResult>();
		List<FutureTask<NewUvPvDiagResult>> tasks = new ArrayList<FutureTask<NewUvPvDiagResult>>();

		for (final ItemPlay item : list) {
			FutureTask<NewUvPvDiagResult> task = TMConfigs.getNewUvPvDiagResultPool().submit(new Callable<NewUvPvDiagResult>() {
				@Override
				public NewUvPvDiagResult call() throws Exception {
					return NewUvPvDiagAction.doDiag(user, item, day, end);
				}
			});
			tasks.add(task);
		}

		for (FutureTask<NewUvPvDiagResult> task : tasks) {
			NewUvPvDiagResult doItem;
			try {
				doItem = task.get();
				res.add(doItem);
			} catch (Exception e) {
				log.warn(e.getMessage(), e);
			}
		}
		
		// 加购率 订单数排序
		if(order > 0) {
			Collections.sort(res, new Comparator<NewUvPvDiagResult>() {
				@Override
				public int compare(NewUvPvDiagResult o1, NewUvPvDiagResult o2) {
					if(order == 1) {
						// 加购率排序
						double itemCartNumPer1 = o1.uv == 0 ? 0 : (double)o1.itemCartNum / (double)o1.uv;
						double itemCartNumPer2 = o2.uv == 0 ? 0 : (double)o2.itemCartNum / (double)o2.uv;
						if(isDesc) {
							return itemCartNumPer1 > itemCartNumPer2 ? -1 : 1;
						} else {
							return itemCartNumPer1 > itemCartNumPer2 ? 1 : -1;
						}
					} else {
						// 订单数排序
						if(isDesc) {
							return o1.alipay_winner_num > o2.alipay_winner_num ? -1 : 1;
						} else {
							return o1.alipay_winner_num > o2.alipay_winner_num ? 1 : -1;
						}
					}
				}
			});
		}

		PageOffset po = new PageOffset(pn, ps, 5);

		TMResult tmRes = new TMResult(res, count, po);
		renderJSON(JsonUtil.getJson(tmRes));
	}

    public static void getPCItemSource(int interval, Long endTime, Long numIid, int pn, int ps) {
        if (numIid == null) {
            renderFailedJson("宝贝id为空");
        }
        if (endTime == null) {
            endTime = System.currentTimeMillis();
        }
        if (interval < 0) {
            interval = 7;
        }
        User user = getUser();
        Long userId = user.getId();
        daysBetween(user, interval);
        if (needReduceEndTime(endTime)) {
            endTime -= DateUtil.DAY_MILLIS;
            interval--;
        }
        Long startTime = endTime - (interval - 1) * DateUtil.DAY_MILLIS;
        String cacheKey = userId + "_" + numIid + "_" + interval + "_" + sdf.format(new Date(endTime)) + "_" + "getPCItemSource";
        try {
        	List<PCSrcUvPv> list = (List<PCSrcUvPv>) Cache.get(cacheKey);
        	if (list != null) {
        		renderJSON(JsonUtil.getJson(list));
        	}
        } catch(Exception e) {
        	log.error("key " + cacheKey + " 没有对应的缓存");
        }
        // 获取时间段内的流量数据
        List<PCSrcUvPv> results = new ArrayList<PCSrcUvPv>();
        TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(106768L,
                "startDate=" + sdf.format(new Date(startTime)) + ",sellerId=" + userId +
                ",endDate=" + sdf.format(new Date(endTime)) + ",numIid=" + numIid, user.getSessionKey()).call();
        List<QueryRow> rows = res.getRes();
        if (CommonUtils.isEmpty(rows)) {
            renderFailedJson("此时段暂无数据");
        }
        List<PCSrcUvPv> srcLevelOne = new ArrayList<PCSrcUvPv>();
        List<PCSrcUvPv> srcLevelTwo = new ArrayList<PCSrcUvPv>();
        for (QueryRow row : rows) {
            PCSrcUvPv src = new PCSrcUvPv(row, user);
            if("0".equals(src.getParentSrcId())){
                srcLevelOne.add(src);
                continue;
            }
            srcLevelTwo.add(src);
        }
        results = PCSrcUvPv.sort(srcLevelOne, srcLevelTwo);
        Cache.set(cacheKey, results, "20h");
        renderJSON(JsonUtil.getJson(results));
    }
    
    public static void showPCDetail(String numIid, String srcId, int days) {
    	if (StringUtils.isEmpty(numIid)) {
            renderFailedJson("宝贝id为空");
        }
        if (StringUtils.isEmpty(srcId)) {
        	renderFailedJson("来源id为空");
        }
        if (days < 0) {
        	days = 30;
        }
        Long endTime = System.currentTimeMillis() - DateUtil.DAY_MILLIS;
        Long startTime = endTime - (days - 1) * DateUtil.DAY_MILLIS;
        User user = getUser();
        Long userId = user.getId();
        // 时间间隔修正
        startTime = startTime > user.getFirstLoginTime()? startTime : user.getFirstLoginTime();
        days = (int) ((endTime - startTime) / DateUtil.DAY_MILLIS) + 1;
        
        String cacheKey = userId + "_" + numIid + "_" + srcId +  "_" + days + "_" + sdf.format(new Date(endTime)) + "_" + "showPCDetail";
        try {
        	List<SrcResult> list = (List<SrcResult>) Cache.get(cacheKey);
        	if (list != null) {
        		renderSuccess("", list);
        	}
        } catch(Exception e) {
        	log.error("key [" + cacheKey + "] 没有对应的缓存");
        }
        
        List<SrcResult> results = new ArrayList<SrcResult>();
        TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(107938L,
                "startDate=" + sdf.format(new Date(startTime)) + ",sellerId=" + userId +
                ",endDate=" + sdf.format(new Date(endTime)) + ",numIid=" + numIid + ",srcId=" + srcId, user.getSessionKey()).call();
        List<QueryRow> rows = res.getRes();
        if (CommonUtils.isEmpty(rows)) {
            renderFailedJson("此时段暂无数据");
        }
        for (int i = 1; i <= days; i++) {
            String dateStr = sdf.format(new Date(endTime - (days - i) * DateUtil.DAY_MILLIS));
            boolean flag = false;
            for (QueryRow row : rows) {
            	if(dateStr.equalsIgnoreCase(row.getValues().get(0))) {
            		results.add(new SrcResult(row, user));
            		flag = true;
            		break;
            	}
            }
        	// 如果没数据，则默认返回全为0
            if(!flag) {
            	results.add(new SrcResult(dateStr, user));
            }
        }
        Cache.set(cacheKey, results, "20h");
        renderSuccess("", results);
    }
    
    public static void showWirelessDetail(String numIid, String srcId, int days) {
    	if (StringUtils.isEmpty(numIid)) {
            renderFailedJson("宝贝id为空");
        }
        if (StringUtils.isEmpty(srcId)) {
        	renderFailedJson("来源id为空");
        }
        if (days < 0) {
        	days = 30;
        }
        Long endTime = System.currentTimeMillis()- DateUtil.DAY_MILLIS;
        Long startTime = endTime - (days - 1) * DateUtil.DAY_MILLIS;
        User user = getUser();
        Long userId = user.getId();
        // 时间间隔修正
        startTime = startTime > user.getFirstLoginTime()? startTime : user.getFirstLoginTime();
        days = (int) ((endTime - startTime) / DateUtil.DAY_MILLIS) + 1;
        
        String cacheKey = userId + "_" + numIid + "_" + srcId +  "_" + days + "_" + sdf.format(new Date(endTime)) + "_" + "showWirelessDetail";
        try {
        	List<SrcResult> list = (List<SrcResult>) Cache.get(cacheKey);
        	if (list != null) {
        		renderSuccess("", list);
        	}
        } catch(Exception e) {
        	log.error("key [" + cacheKey + "] 没有对应的缓存");
        }
        
        List<SrcResult> results = new ArrayList<SrcResult>();
        TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(107941L,
                "startDate=" + sdf.format(new Date(startTime)) + ",sellerId=" + userId +
                ",endDate=" + sdf.format(new Date(endTime)) + ",numIid=" + numIid + ",srcId=" + srcId, user.getSessionKey()).call();
        List<QueryRow> rows = res.getRes();
        if (CommonUtils.isEmpty(rows)) {
            renderFailedJson("此时段暂无数据");
        }
        for (int i = 1; i <= days; i++) {
            String dateStr = sdf.format(new Date(endTime - (days - i) * DateUtil.DAY_MILLIS));
            boolean flag = false;
            for (QueryRow row : rows) {
            	if(dateStr.equalsIgnoreCase(row.getValues().get(0))) {
            		results.add(new SrcResult(row, user));
            		flag = true;
            		break;
            	}
            }
        	// 如果没数据，则默认返回全为0
            if(!flag) {
            	results.add(new SrcResult(dateStr, user));
            }
        }
        Cache.set(cacheKey, results, "20h");
        renderSuccess("", results);
    }

    public static void getShopHourViewAndTrade(Long thedate, String orderBy) {

        if (thedate == null) {
            thedate = System.currentTimeMillis();
        }
        if (StringUtils.isEmpty(orderBy)) {
            orderBy = "thehour";
        }

        User user = getUser();
        // 获取时间段内的流量数据
        List<ShopHourViewAndTrade> result = new ArrayList<ShopHourViewAndTrade>();
        TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(3089L,
                "theDate=" + sdf.format(new Date(thedate)) + ",sellerId=" + user.getId() +
                        ",sub_order_by=" + orderBy,
                user.getSessionKey()).call();
        List<QueryRow> rows = res.getRes();
        if (CommonUtils.isEmpty(rows)) {
            renderJSON(JsonUtil.getJson(new ArrayList<PCSrcUvPv>()));
        } else {
            for (QueryRow row : rows) {
                result.add(new ShopHourViewAndTrade(row));
            }
        }

        renderJSON(JsonUtil.getJson(result));
    }

    public static void getShopAllHourViewAndTrade(Long thedate) {

        if (thedate == null) {
            thedate = System.currentTimeMillis();
        }

        User user = getUser();
        // 获取时间段内的流量数据
        Map<String, ShopHourViewAndTrade> map = new HashMap<String, ShopHourViewAndTrade>();
        TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(3092L,
                "theDate=" + sdf.format(new Date(thedate)) + ",sellerId=" + user.getId(),
                user.getSessionKey()).call();
        List<QueryRow> rows = res.getRes();
        if (CommonUtils.isEmpty(rows)) {
            renderJSON(JsonUtil.getJson(new ArrayList<PCSrcUvPv>()));
        } else {
            for (QueryRow row : rows) {
                String thehour = row.getValues().get(1);
                map.put(thehour, new ShopHourViewAndTrade(row));
            }
            for (int i = 0; i < 24; i++) {
                if (map.get(String.valueOf(i)) == null) {
                    ShopHourViewAndTrade s = new ShopHourViewAndTrade(i);
                    map.put(String.valueOf(i), s);
                }
            }
        }

        renderJSON(JsonUtil.getJson(map));
    }

    public static void getPCShopSource(int interval, Long endTime) {

        if (endTime == null) {
            endTime = System.currentTimeMillis();
        }
        if (interval < 0) {
            interval = 7;
        }


        User user = getUser();
        daysBetween(user, interval);
        if (needReduceEndTime(endTime)) {
            endTime -= DateUtil.DAY_MILLIS;
            interval--;
        }
        Long startTime = endTime - (interval - 1) * DateUtil.DAY_MILLIS;
        // 获取时间段内的流量数据
        List<PCSrcUvPv> results = new ArrayList<PCSrcUvPv>();
        TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(110122L,
                "startDate=" + sdf.format(new Date(startTime)) + ",sellerId=" + user.getId() +
                        ",endDate=" + sdf.format(new Date(endTime)), user.getSessionKey())
                .call();
        List<QueryRow> rows = res.getRes();
        if (CommonUtils.isEmpty(rows)) {
            renderJSON(JsonUtil.getJson(new ArrayList<PCSrcUvPv>()));
        } else {
            for (QueryRow row : rows) {
                List<String> values = row.getValues();
                PCSrcUvPv src = new PCSrcUvPv(values.get(1), values.get(3), values.get(4),
                        values.get(5), values.get(6), values.get(7), values.get(8), values.get(13), user);
                src.setBounce_count(getBounceCount(startTime, endTime, user, src.getSrcId(), "pv"));
                results.add(src);
            }

        }

        renderJSON(JsonUtil.getJson(results));
    }

    public static String getBounceCount(Long startTime, Long endTime, User user,
            String srcId, String type) {
        if (StringUtils.isEmpty(srcId)) {
            return StringUtils.EMPTY;
        }
        if (StringUtils.isEmpty(type)) {
            return StringUtils.EMPTY;
        }
        TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(2804L,
                "startDate=" + sdf.format(new Date(startTime)) + ",sellerId=" + user.getId() +
                        ",endDate=" + sdf.format(new Date(endTime)) + ",srcId=" + srcId, user.getSessionKey())
                .call();
        List<QueryRow> rows = res.getRes();
        if (CommonUtils.isEmpty(rows)) {
            return StringUtils.EMPTY;
        }
        QueryRow row = rows.get(0);
        List<String> values = row.getValues();
        if (values.size() <= 0) {
            return "~";
        }
        if (type.equals("pv")) {
            return values.get(1);
        } else if (type.equals("ipv")) {
            return values.get(2);
        } else if (type.equals("uv")) {
            return values.get(3);
        } else if (type.equals("iuv")) {
            return values.get(4);
        } else {
            return values.get(1);
        }
    }

    public static void getAreasViewsAndTrades(int interval, Long endTime) {

        if (endTime == null) {
            endTime = System.currentTimeMillis();
        }
        if (interval < 1) {
            interval = 7;
        }
        Long startTime = endTime - (interval - 1) * DateUtil.DAY_MILLIS;
        User user = getUser();
        TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(2747L,
                "startdate=" + sdf.format(new Date(startTime)) + ",sellerId=" + user.getId() +
                        ",enddate=" + sdf.format(new Date(endTime)), user.getSessionKey()).call();
        List<QueryRow> rows = res.getRes();
        List<AreaViewsAndTrades> results = new ArrayList<AreaViewsAndTrades>();
        if (CommonUtils.isEmpty(rows)) {
            renderJSON(JsonUtil.getJson(results));
        }
        for (QueryRow row : rows) {
            results.add(new AreaViewsAndTrades(row));
        }
        renderJSON(JsonUtil.getJson(results));
    }

    public static void getAreasViewsAndTradesPaging(int interval, Long endTime, int pn, int ps) {

        if (endTime == null) {
            endTime = System.currentTimeMillis();
        }
        if (interval < 1) {
            interval = 7;
        }
        PageOffset po = new PageOffset(pn, ps, 10);
        User user = getUser();
        daysBetween(user, interval);
        if (needReduceEndTime(endTime)) {
            endTime -= DateUtil.DAY_MILLIS;
            interval--;
        }
        Long startTime = endTime - (interval - 1) * DateUtil.DAY_MILLIS;
        TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(2979L,
                "startdate=" + sdf.format(new Date(startTime)) + ",sellerId=" + user.getId() +
                        ",enddate=" + sdf.format(new Date(endTime)) + ",sub_offset=" + po.getOffset() +
                        ",sub_limit=" + po.getPs(), user.getSessionKey()).call();
        List<QueryRow> rows = res.getRes();
        List<AreaViewsAndTrades> results = new ArrayList<AreaViewsAndTrades>();
        if (CommonUtils.isEmpty(rows)) {
            renderJSON(JsonUtil.getJson(new TMResult(results)));
        }
        int count = 0;
        TMResult<List<QueryRow>> countRes = new MBPApi.MBPDataGet(2980L,
                "startdate=" + sdf.format(new Date(startTime)) + ",sellerId=" + user.getId() +
                        ",enddate=" + sdf.format(new Date(endTime)), user.getSessionKey()).call();
        List<QueryRow> countRows = countRes.getRes();
        if (!CommonUtils.isEmpty(countRows)) {
            count = countRows.size();
        }
        for (QueryRow row : rows) {
            results.add(new AreaViewsAndTrades(row));
        }
        renderJSON(JsonUtil.getJson(new TMResult(results, count, po)));
    }

    public static void getAllAreasViewsAndTrades(int interval, Long endTime) {

        if (endTime == null) {
            endTime = System.currentTimeMillis();
        }
        if (interval < 1) {
            interval = 7;
        }
        User user = getUser();
        daysBetween(user, interval);
        if (needReduceEndTime(endTime)) {
            endTime -= DateUtil.DAY_MILLIS;
            interval--;
        }
        Long startTime = endTime - (interval - 1) * DateUtil.DAY_MILLIS;
        TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(2985L,
                "startdate=" + sdf.format(new Date(startTime)) + ",sellerId=" + user.getId() +
                        ",enddate=" + sdf.format(new Date(endTime)), user.getSessionKey()).call();
        List<QueryRow> rows = res.getRes();
        if (CommonUtils.isEmpty(rows)) {
            renderJSON(JsonUtil.getJson(new HashMap<Long, AreaViewsAndTrades>()));
        }
        Map<Long, AreaViewsAndTrades> result = genProvinceMap(rows);
        renderJSON(JsonUtil.getJson(result));
    }

    public static Map<Long, AreaViewsAndTrades> genProvinceMap(List<QueryRow> areaRows) {
        Map<Long, AreaViewsAndTrades> result = new HashMap<Long, AreaViewsAndTrades>();
        if (CommonUtils.isEmpty(areaRows)) {
            return result;
        }
        for (QueryRow row : areaRows) {
            List<String> value = row.getValues();
            Long regionId = Long.valueOf(value.get(2));
            CloudDataRegion region = CloudDataRegion.findByRegionId(regionId);
            if (region == null) {
                continue;
            }
            Long provinceId = region.getProvince_id();
            if (provinceId == null) {
                continue;
            }
            if (provinceId <= 0) {
                continue;
            }
            AreaViewsAndTrades provinceViewsAndTrades = result.get(provinceId);
            if (provinceViewsAndTrades == null) {
                provinceViewsAndTrades = new AreaViewsAndTrades(row);
                provinceViewsAndTrades.setProvinceId(provinceId.toString());
                provinceViewsAndTrades.setProvinceName(region.getProvince_name());
                result.put(provinceId, provinceViewsAndTrades);
            } else {
                provinceViewsAndTrades.AddProp(value.get(3).equals("NULL") ? 0 : Integer.valueOf(value.get(3)),
                        value.get(4).equals("NULL") ? 0 : Integer.valueOf(value.get(4)),
                        value.get(7).equals("NULL") ? 0 : Integer.valueOf(value.get(7)),
                        value.get(12).equals("NULL") ? 0 : Integer.valueOf(value.get(12)),
                        value.get(8).equals("NULL") ? 0 : Integer.valueOf(value.get(8)),
                        value.get(9).equals("NULL") ? 0 : Integer.valueOf(value.get(9)),
                        value.get(10).equals("NULL") ? 0 : Float.valueOf(value.get(10)),
                        value.get(11).equals("NULL") ? 0 : Integer.valueOf(value.get(11)));
                provinceViewsAndTrades.setProvinceId(provinceId.toString());
                provinceViewsAndTrades.setProvinceName(region.getProvince_name());
                result.put(provinceId, provinceViewsAndTrades);
            }

        }
        return result;
    }

    public static void testSevenDayComeInWordsClick(Long numIid) {
        User user = getUser();
        Map<String, Integer> result = ClouddateUtil.get7DayComeInWordsMap(user, numIid, 14L);
        renderJSON(JsonUtil.getJson(result));
    }

    public static void getShopPCBounceCount(int interval, Long endTime) {
    	if (endTime == null) {
            endTime = System.currentTimeMillis();
        }
    	// 这里，每天早上8点才会显示当天的数据，否则仍然显示前天的数据
        if (needReduceEndTime(endTime)) {
    	    endTime -= DateUtil.DAY_MILLIS;
    	    interval--;
        }
        // endTime = endTime - 8 * DateUtil.ONE_HOUR;
        if (interval < 0) {
            interval = 7;
        }
        Long startTime = endTime - (interval - 1) * DateUtil.DAY_MILLIS;
    	User user = getUser();
    	if(user == null) {
    		renderFailedJson("用户不存在");
    	}
    	TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(3136L,
                "startdate=" + sdf.format(new Date(startTime)) + ",sellerId=" + user.getId() +
                        ",enddate=" + sdf.format(new Date(endTime)), user.getSessionKey()).call();
    	List<QueryRow> rows = res.getRes();
    	if (CommonUtils.isEmpty(rows)) {
            renderFailedJson("御膳房返回数据为空");
        }
        List<String> values = rows.get(0).getValues();
        if(CommonUtils.isEmpty(values)) {
        	renderFailedJson("御膳房返回数据为空");
        }
        renderJSON(JsonUtil.getJson(values.get(1)));
    }
    
    public static void getUserDSR(int interval, Long endTime) {
    	if (endTime == null) {
            endTime = System.currentTimeMillis();
        }
        if (interval < 1) {
            interval = 7;
        }
        Long startTime = endTime - (interval - 1) * DateUtil.DAY_MILLIS;
    	User user = getUser();
    	if(user == null) {
    		renderFailedJson("用户不存在");
    	}
    	TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(3185L,
                "startdate=" + sdf.format(new Date(startTime)) + ",sellerId=" + user.getId() +
                        ",enddate=" + sdf.format(new Date(endTime)), user.getSessionKey()).call();
    	List<QueryRow> rows = res.getRes();
    	if (CommonUtils.isEmpty(rows)) {
            renderFailedJson("御膳房返回数据为空");
        }
    }
    
    /**
     * 下载某个宝贝的无线入店关键词
     */
    public static int YSFLimit = 5000;
    public static void exportExcel(Long numIid, int interval, Long endTime, String content){
        if (needReduceEndTime(endTime)) {
            endTime -= DateUtil.DAY_MILLIS;
            interval--;
        }
        User user = getUser();
        String[] wordArr = null;
        if(!StringUtils.isEmpty(content)) {
        	content = content.replaceAll("，", ",");
        	if (content.contains("\r\n")) {
        		wordArr = content.split("\r\n");
            } else {
            	wordArr = content.split("\n");
            }
        }
        Map<String, Long> wordMap = new HashMap<String, Long>();
        if (wordArr != null) {
            for (String string : wordArr) {
                String[] split = string.split(",");
                if(split.length != 2) {
                	continue;
                }
                String word = split[0].trim();
                boolean matches = split[1].trim().matches("^[-\\+]?[\\d]*$");
                if(!matches) {
                	continue;
                }
                Long count = Long.valueOf(split[1].trim());
                wordMap.put(word, count);
            }
        }
        
        List<String[]> wordInfos = new ArrayList<String[]>();
        if (numIid == null) {
            generateExcel(wordInfos, user);
        }
        Long startTime = endTime - (interval - 1) * DateUtil.DAY_MILLIS;
        
        // 计算该宝贝时间段内所有入店关键词总数
        TMResult<List<QueryRow>> wordsCountRes = new MBPApi.MBPDataGet(100531L,
                "startdate=" + sdf.format(new Date(startTime)) + ",sellerId=" + user.getId() +
                ",enddate=" + sdf.format(new Date(endTime)) + ",numIid=" + numIid, user.getSessionKey())
                .call();
        List<QueryRow> wordsCountRows = wordsCountRes.getRes();
        if(CommonUtils.isEmpty(wordsCountRows)){
            generateExcel(wordInfos, user);
        }
        List<String> values = wordsCountRows.get(0).getValues();
        if(CommonUtils.isEmpty(values)) {
            generateExcel(wordInfos, user);
        }
        String countStr = values.get(0);
        int count = StringUtils.isEmpty(countStr) ? 0 : Integer.valueOf(countStr);
        List<QueryRow> rows = new ArrayList<QueryRow>();
        int loops = 0;
        while(loops * YSFLimit <= count) {
            TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(100495L,
                     "startdate=" + sdf.format(new Date(startTime)) +
                     ",numIid=" + numIid +
                     ",sellerId=" + user.getId() +
                     ",enddate=" + sdf.format(new Date(endTime)) + 
                     ",sub_offset=" + loops * YSFLimit + 
                     ",sub_limit=" + YSFLimit + 
                     ",sub_order_by=pv", user.getSessionKey()).call();
            rows.addAll(res.getRes());
            loops++;
        }
        if(CommonUtils.isEmpty(rows)){
            generateExcel(wordInfos, user);
        }
        
        Iterator<QueryRow> iterator = rows.iterator();
        while(iterator.hasNext()){
            WordInfo wordInfo = WordInfo.buildAppWithNoDate(iterator.next());
            if(wordMap.containsKey(wordInfo.word)) {
            	wordInfo.pv = String.valueOf(Long.valueOf(wordInfo.pv) - wordMap.get(wordInfo.word));
            	wordInfo.uv = String.valueOf(Long.valueOf(wordInfo.uv) - wordMap.get(wordInfo.word));
            }
            wordInfos.add(WordInfo.wordInfo2String(wordInfo));
        }
        
        generateExcel(wordInfos, user);
    }
    
    /**
     * 下载该店铺所有的无线入店关键词
     */
    public static void exportExcelAll(int interval, Long endTime){
        if (needReduceEndTime(endTime)) {
            endTime -= DateUtil.DAY_MILLIS;
            interval --;
        }
        User user = getUser();
        List<String[]> wordInfos = new ArrayList<String[]>();
        Long startTime = endTime - (interval - 1) * DateUtil.DAY_MILLIS;
        
        // 计算该宝贝时间段内所有入店关键词总数
        TMResult<List<QueryRow>> wordsCountRes = new MBPApi.MBPDataGet(100530L,
                "startdate=" + sdf.format(new Date(startTime)) + 
                ",sellerId=" + user.getId() +
                ",enddate=" + sdf.format(new Date(endTime)), user.getSessionKey()).call();
        List<QueryRow> wordsCountRows = wordsCountRes.getRes();
        if(CommonUtils.isEmpty(wordsCountRows)){
            generateExcel(wordInfos, user);
        }
        List<String> values = wordsCountRows.get(0).getValues();
        if(CommonUtils.isEmpty(values)) {
            generateExcel(wordInfos, user);
        }
        int count = StringUtils.isEmpty(values.get(0)) ? 0 : Integer.valueOf(values.get(0));
        if(count > 1000){
            pageOffsetExcel(count, startTime, endTime, user, wordInfos);
            generateExcel(wordInfos, user);
        }
        TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(100496L,
                "startdate=" + sdf.format(new Date(startTime)) +
                ",sellerId=" + user.getId() +
                ",enddate=" + sdf.format(new Date(endTime)) + 
                ",sub_offset=0" +
                ",sub_limit=" + count + 
                ",sub_order_by=pv", user.getSessionKey()).call();
        List<QueryRow> rows = res.getRes();
        if(CommonUtils.isEmpty(rows)){
            generateExcel(wordInfos, user);
        }
        
        Iterator<QueryRow> iterator = rows.iterator();
        while(iterator.hasNext()){
            WordInfo wordInfo = WordInfo.buildAppWithNoDate(iterator.next());
            wordInfos.add(WordInfo.wordInfo2String(wordInfo));
        }
        generateExcel(wordInfos, user);
    }
    
    private static void pageOffsetExcel(int count, Long startTime, Long endTime, User user, List<String[]> wordInfos){
        int begin = 0;
        List<QueryRow> allRows = new ArrayList<QueryRow>();
        while(true){
            if(begin > count){
                break;
            }
            TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(100496L,
                    "startdate=" + sdf.format(new Date(startTime)) +
                    ",sellerId=" + user.getId() +
                    ",enddate=" + sdf.format(new Date(endTime)) + 
                    ",sub_offset=" + begin +
                    ",sub_limit=" + 1000 + 
                    ",sub_order_by=pv", user.getSessionKey()).call();
            List<QueryRow> rows = res.getRes();
            begin += 1000;
            if(CommonUtils.isEmpty(rows)){
                continue;
            }
            allRows.addAll(rows);
        }
        Iterator<QueryRow> iterator = allRows.iterator();
        while(iterator.hasNext()){
            WordInfo wordInfo = WordInfo.buildAppWithNoDate(iterator.next());
            wordInfos.add(WordInfo.wordInfo2String(wordInfo));
        }
    }

    private static void generateExcel(List<String[]> wordInfos, User user) {
        String fileName = Play.tmpDir.getPath() + "/[无线入店关键词]" + user.getUserNick() + ".xls";
        String sheetName = "无线入店关键词";
        String fields = "关键词,浏览量,访客数,成交人数,成交件数,成交金额,成交转化率";
        ExcelUtil.writeToExcel(wordInfos, fields, sheetName, fileName);
        File file = new File(fileName);
        renderBinary(file);
    }
    
    private static long itemPlayViewTrade(int interval, Long endTime, Long numIid, User user) {
        if(numIid == null || numIid <= 0) {
            renderFailedJson("numIid为空或小于0");
        }
        if (user == null) {
            renderFailedJson("用户不存在");
        }
        if (endTime == null) {
            endTime = DateUtil.formDailyTimestamp(System.currentTimeMillis());
        }
        if (needReduceEndTime(endTime)) {
            endTime -= DateUtil.DAY_MILLIS;
            interval--;
        }

//        daysBetween(user, interval);
        endTime = DateUtil.formDailyTimestamp(endTime);
        return endTime - (interval - 1) * DateUtil.DAY_MILLIS;
    }
    
    private static void getCache(String key){
        try {
            Map<String, AreaViews> map = Cache.get(key, Map.class);
            if (map != null) {
                renderJSON(JsonUtil.getJson(map));
            }
        } catch (Exception e) {
            return;
        }
    }
    
    private static void getCache(int interval, Long endTime, Long numIid, Long userId){
    	Map<String, AreaViews> map = new TreeMap<String, AreaViews>();
    	try {
        	for (int i = interval; i >= 1; i--) {
            	String dateStr = sdf.format(new Date(endTime - (interval - i) * DateUtil.DAY_MILLIS));
            	String dayKey = "getItemPlayViewTrade_" + userId + dateStr + "_" + numIid;
            	Object obj = Cache.get(dayKey);
            	if(obj == null){
            		return;
            	}
            	map.put(dateStr, (AreaViews) obj);
            }
        	renderJSON(JsonUtil.getJson(map));
		} catch (Exception e) {
			return;
		}
    }
    
    public static void getItemPlayViewTrade(int interval, Long endTime, Long numIid){
        User user = getUser();
        long startTime = itemPlayViewTrade(interval, endTime, numIid, user);
        Long userId = user.getId();
        // 从缓存中读取数据
        getCache(interval, endTime, numIid, userId);
        // 如果没数据，则默认返回全为0
        Map<String, AreaViews> map = new TreeMap<String, AreaViews>();
        // 设置pv uv等
        boolean isExitData = CloudDataAction.getViewTrade(endTime, numIid, user.getSessionKey(), userId, map, startTime);
        // 加入缓存中
        Map<String, AreaViews> returnMap = CloudDataAction.setMapDate(interval, endTime);
        if(isExitData){
        	for (int i = interval; i >= 1; i--) {
                String dateStr = sdf.format(new Date(endTime - (interval - i) * DateUtil.DAY_MILLIS));
                String dayKey = "getItemPlayViewTrade_" + userId + dateStr + "_" + numIid;
                if(map.get(dateStr) != null) {
                	Cache.set(dayKey, map.get(dateStr), CACHE_TIME);
                	returnMap.put(dateStr, map.get(dateStr));
                }
            }
        }
        renderJSON(JsonUtil.getJson(returnMap));
    }
    
    public static void getUV(int interval, Long endTime, Long numIid){
        User user = getUser();
        long startTime = itemPlayViewTrade(interval, endTime, numIid, user);
        Long userId = user.getId();
        // 从缓存中读取数据
        String key = userId + "_" + interval+ "_" + sdf.format(new Date(endTime)) + "_" + numIid + "_getUV";
        getCache(key);
        Map<String, AreaViews> map = CloudDataAction.setMapDate(interval, endTime);
        CloudDataAction.setSearchUV(endTime, numIid, user.getSessionKey(), userId, map, startTime);
        // 加入缓存中
        Cache.set(key, map, CACHE_TIME);
        renderJSON(JsonUtil.getJson(map));
    }
    
    public static void getPCUV(int interval, Long endTime, Long numIid){
        User user = getUser();
        long startTime = itemPlayViewTrade(interval, endTime, numIid, user);
        Long userId = user.getId();
        // 从缓存中读取数据
        String key = userId + "_" + interval+ "_" + sdf.format(new Date(endTime)) + "_" + numIid + "_getPCUV";
        getCache(key);
        Map<String, AreaViews> map = CloudDataAction.setMapDate(interval, endTime);
        CloudDataAction.setPCUV(endTime, numIid, user.getSessionKey(), userId, map, startTime);
        // 加入缓存中
        Cache.set(key, map, CACHE_TIME);
        renderJSON(JsonUtil.getJson(map));
    }
    
    public static void getSearchUV(int interval, Long endTime, Long numIid){
        User user = getUser();
        long startTime = itemPlayViewTrade(interval, endTime, numIid, user);
        Long userId = user.getId();
        // 从缓存中读取数据
        String key = userId + "_" + interval+ "_" + sdf.format(new Date(endTime)) + "_" + numIid + "_getSearchUV";
        getCache(key);
        Map<String, AreaViews> map = CloudDataAction.setMapDate(interval, endTime);
        // 宝贝PC端searchUv
        TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(3273L,
                "startdate=" + sdf.format(new Date(startTime)) + ",sellerId=" + userId +
                ",enddate=" + sdf.format(new Date(endTime)) + ",platform=0,numIid=" + numIid + ",@replace_null=NULL", user.getSessionKey()).call();
        List<QueryRow> pcSearchUv = res.getRes();
        if(!CommonUtils.isEmpty(pcSearchUv)) {
            for(QueryRow uvWinner : pcSearchUv) {
                List<String> values = uvWinner.getValues();
                if(CommonUtils.isEmpty(values)) {
                    continue;
                }
                AreaViews noSearch = map.get(values.get(0));
                if(noSearch == null) {
                    continue;
                }
                noSearch.addSearchUv(values.get(1));
            }
        }
        // 宝贝无线端searchUv
        TMResult<List<QueryRow>> appres = new MBPApi.MBPDataGet(111539L,
                "startdate=" + sdf.format(new Date(startTime)) + ",sellerId=" + userId +
                ",enddate=" + sdf.format(new Date(endTime)) + ",platform=0,numIid=" + numIid + ",@replace_null=NULL", user.getSessionKey()).call();
        List<QueryRow> appSearchUv = appres.getRes();
        if(!CommonUtils.isEmpty(appSearchUv)) {
            for(QueryRow uvWinner : appSearchUv) {
                List<String> values = uvWinner.getValues();
                if(CommonUtils.isEmpty(values)) {
                    continue;
                }
                AreaViews noSearch = map.get(values.get(0));
                if(noSearch == null) {
                    continue;
                }
                noSearch.addSearchUv(values.get(1));
            }
        }
        
        if(!CommonUtils.isEmpty(pcSearchUv) && !CommonUtils.isEmpty(appSearchUv)) {
        	// 加入缓存中
        	Cache.set(key, map, CACHE_TIME);
        }
        
        renderJSON(JsonUtil.getJson(map));
    }
	
	public static void getEntranceNum(int interval, Long endTime, Long numIid){
		User user = getUser();
		Long userId = user.getId();
		String sessionKey = user.getSessionKey();
		itemPlayViewTrade(interval, endTime, numIid, user);
		// 从缓存中读取数据
		String cacheKey = userId + "_" + interval+ "_" + sdf.format(new Date(endTime)) + "_" + numIid + "_getEntranceNum";
		try {
			Map<String, EntranceNum> map = Cache.get(cacheKey, Map.class);
			if (map != null) {
				renderJSON(JsonUtil.getJson(map));
			}
		} catch(Exception e) {
			log.error("key [" + cacheKey + "] 没有对应的缓存");
		}
		
		List<EntranceResult> totalEntrance = new ArrayList<EntranceResult>();
		// 时间跨度多了1天 用来计算【流失/新增入口】
		long startTime = endTime - interval * DateUtil.DAY_MILLIS;
		String startDate = sdf.format(new Date(startTime));
		String endDate = sdf.format(new Date(endTime));
		boolean pcFlag = true;
		
		TMResult<List<QueryRow>> pcRes = new MBPApi.MBPDataGet(108380L,
				"startdate=" + startDate + ",enddate=" + endDate + ",sellerId=" + userId + ",numIid=" + numIid, sessionKey).call();
		if(!pcRes.isOk()) {
			pcFlag = false;
		}
		List<QueryRow> pcRows = pcRes.getRes();
		if (!CommonUtils.isEmpty(pcRows)) {
			for (QueryRow row : pcRows) {
				totalEntrance.add(new EntranceResult(row, true, user));
			}
		}
		// 多于5000时循环获取
		int loops = 0;
		int limit = 5000;
		List<QueryRow> wxRows = new ArrayList<QueryRow>();
		boolean wxFlag = true;
		do {
			TMResult<List<QueryRow>> wxRes = new MBPApi.MBPDataGet(108401L,
					"startdate=" + startDate + ",enddate=" + endDate + ",sellerId=" + userId + ",numIid=" + numIid + ",sub_offset=" + loops * limit + ",sub_limit=" + limit, sessionKey).call();
			if(!wxRes.isOk()) {
				wxFlag = false;
			}
			wxRows = wxRes.getRes();
			if(!CommonUtils.isEmpty(wxRows)) {
				for (QueryRow row : wxRows) {
					totalEntrance.add(new EntranceResult(row, false, user));
				}
				loops ++;
			}
		} while (!CommonUtils.isEmpty(wxRows));
		
		Map<String, List<EntranceResult>> listMap = new TreeMap<String, List<EntranceResult>>();
		for(int i = 0; i <= interval; i++) {
			long tempTime = endTime - (interval - i) * DateUtil.DAY_MILLIS;
			String dateTime = sdf.format(new Date(tempTime));
			List<EntranceResult> entranceList = new ArrayList<EntranceResult>();
			for (EntranceResult entrance : totalEntrance) {
				if(entrance.getThedate().equalsIgnoreCase(dateTime)) {
					entranceList.add(entrance);
				}
			}
			listMap.put(dateTime, entranceList);
		}
		
		Map<String, EntranceNum> map = CloudDataAction.setEntranceMap(interval, endTime);
		for (int i = 1; i <= interval; i++) {
			long tempTime = endTime - (interval - i) * DateUtil.DAY_MILLIS;
			String curr = sdf.format(new Date(tempTime));
			String ytd = getYesterdayOf(curr);
			EntranceNum result = map.get(curr);
			List<EntranceResult> repeat = new ArrayList<EntranceResult>();
			List<EntranceResult> ytdEntrance = listMap.get(ytd);
			List<EntranceResult> currEntrance = listMap.get(curr);
			for (EntranceResult currData : currEntrance) {
				String word = currData.getWord();
				for(EntranceResult ytdDate : ytdEntrance) {
					if(ytdDate.getWord().equalsIgnoreCase(word)) {
						repeat.add(currData);
					}
				}
			}
			int entranceNum = currEntrance.size();
			int reduceNum = ytdEntrance.size() - repeat.size();
			int increaseNum = currEntrance.size() - repeat.size();
			result.setEntranceNum(entranceNum);
			result.setReduceNum(reduceNum);
			result.setIncreaseNum(increaseNum);
		}
		
		if(pcFlag && wxFlag) {
			Cache.set(cacheKey, map, "20h");
		}
		renderJSON(JsonUtil.getJson(map));
	}
	
	public static class EntranceNum implements Serializable{
		private static final long serialVersionUID = 1L;
		public static final int DEFAULT_VALUE = 0;
		
		public int entranceNum;
		
		public int reduceNum;

		public int increaseNum;
		
		public EntranceNum(){
			this.entranceNum = DEFAULT_VALUE;
			this.reduceNum = DEFAULT_VALUE;
			this.increaseNum = DEFAULT_VALUE;
		}
		
		public EntranceNum(int entranceNum, int reduceNum, int increaseNum) {
			super();
			this.entranceNum = entranceNum;
			this.reduceNum = reduceNum;
			this.increaseNum = increaseNum;
		}

		public int getEntranceNum() {
			return entranceNum;
		}

		public void setEntranceNum(int entranceNum) {
			this.entranceNum = entranceNum;
		}

		public int getReduceNum() {
			return reduceNum;
		}

		public void setReduceNum(int reduceNum) {
			this.reduceNum = reduceNum;
		}

		public int getIncreaseNum() {
			return increaseNum;
		}

		public void setIncreaseNum(int increaseNum) {
			this.increaseNum = increaseNum;
		}
	}
	
	public static void showEntranceDetail(String date, String numIid) {
		if (StringUtils.isEmpty(numIid)) {
			renderFailedJson("宝贝id为空");
		}
		if (StringUtils.isEmpty(date)) {
			renderFailedJson("入口时间为空");
		}
		User user = getUser();
		Long userId = user.getId();
		
		String cacheKey = userId + "_" + numIid + "_" + date + "showEntranceDetail";
		try {
			List<EntranceDetail> list = (List<EntranceDetail>) Cache.get(cacheKey);
			if (list != null) {
				renderSuccess("", list);
			}
		} catch(Exception e) {
			log.error("key [" + cacheKey + "] 没有对应的缓存");
		}
		
		List<EntranceDetail> results = new ArrayList<EntranceDetail>();
		List<EntranceResult> ytdEntrance = new ArrayList<EntranceResult>();
		List<EntranceResult> currEntrance = new ArrayList<EntranceResult>();
		
		String ytd = getYesterdayOf(date);
		TMResult<List<QueryRow>> ytdPcRes = new MBPApi.MBPDataGet(108380L,
				"startdate=" + ytd + ",enddate=" + ytd +
				",sellerId=" + userId + ",numIid=" + numIid, user.getSessionKey()).call();
		List<QueryRow> ytdPcRows = ytdPcRes.getRes();
		if (!CommonUtils.isEmpty(ytdPcRows)) {
			for (QueryRow row : ytdPcRows) {
				ytdEntrance.add(new EntranceResult(row, true, user));
			}
		}
		TMResult<List<QueryRow>> ytdWirelessRes = new MBPApi.MBPDataGet(108381L,
				"startdate=" + ytd + ",enddate=" + ytd +
				",sellerId=" + userId + ",numIid=" + numIid, user.getSessionKey()).call();
		List<QueryRow> ytdWirelessRows = ytdWirelessRes.getRes();
		if (!CommonUtils.isEmpty(ytdWirelessRows)) {
			for (QueryRow row : ytdWirelessRows) {
				ytdEntrance.add(new EntranceResult(row, false, user));
			}
		}
		
		TMResult<List<QueryRow>> pcRes = new MBPApi.MBPDataGet(108380L,
				"startdate=" + date + ",enddate=" + date +
				",sellerId=" + userId + ",numIid=" + numIid, user.getSessionKey()).call();
		List<QueryRow> pcRows = pcRes.getRes();
		if (!CommonUtils.isEmpty(pcRows)) {
			for (QueryRow row : pcRows) {
				currEntrance.add(new EntranceResult(row, true, user));
			}
		}
		TMResult<List<QueryRow>> wirelessRes = new MBPApi.MBPDataGet(108381L,
				"startdate=" + date + ",enddate=" + date +
				",sellerId=" + userId + ",numIid=" + numIid, user.getSessionKey()).call();
		List<QueryRow> wirelessRows = wirelessRes.getRes();
		if (!CommonUtils.isEmpty(wirelessRows)) {
			for (QueryRow row : wirelessRows) {
				currEntrance.add(new EntranceResult(row, false, user));
			}
		}
		
		for (int i = 0; i < ytdEntrance.size(); i++) {
			String word = ytdEntrance.get(i).getWord();
			for (int j = 0; j < currEntrance.size(); j++) {
				if(currEntrance.get(j).getWord().equalsIgnoreCase(word)) {
					currEntrance.remove(j);
					ytdEntrance.remove(i);
					i--;
					break;
				}
			}
		}
		
		if(ytdEntrance.size() >= currEntrance.size()) {
			for (int i = 0; i < ytdEntrance.size(); i++) {
				String reduceWord = ytdEntrance.get(i).getWord();
				boolean reduceWordPC = ytdEntrance.get(i).isPC();
				String increaseWord = StringUtils.EMPTY;
				boolean increaseWordPC = true;
				if(i < currEntrance.size()) {
					increaseWord = currEntrance.get(i).getWord();
					increaseWordPC = currEntrance.get(i).isPC();
				}
				EntranceDetail result = new EntranceDetail(reduceWord, reduceWordPC, increaseWord, increaseWordPC);
				results.add(result);
			}
		} else {
			for (int i = 0; i < currEntrance.size(); i++) {
				String reduceWord = StringUtils.EMPTY;
				boolean reduceWordPC = true;
				String increaseWord = currEntrance.get(i).getWord();
				boolean increaseWordPC = currEntrance.get(i).isPC();
				if(i < ytdEntrance.size()) {
					reduceWord = ytdEntrance.get(i).getWord();
					reduceWordPC = ytdEntrance.get(i).isPC();
				}
				EntranceDetail result = new EntranceDetail(reduceWord, reduceWordPC, increaseWord, increaseWordPC);
				results.add(result);
			}
		}
		
		String latest = sdf.format(new Date(System.currentTimeMillis() - DateUtil.DAY_MILLIS));
		if(CommonUtils.isEmpty(currEntrance) && date.equalsIgnoreCase(latest)) {
			renderSuccess("", results);
		}
		
		if(ytdWirelessRes.isOk() && ytdPcRes.isOk() && wirelessRes.isOk() && pcRes.isOk()) {
			Cache.set(cacheKey, results, "20h");
		}
		renderSuccess("", results);
	}
	
	public static class EntranceDetail implements Serializable{
		private static final long serialVersionUID = 1L;
		public String reduceWord;
		
		public boolean reduceWordPC;

		public String increaseWord;
		
		public boolean increaseWordPC;
		
		public EntranceDetail(String reduceWord, Boolean reduceWordPC,
				String increaseWord, Boolean increaseWordPC) {
			super();
			this.reduceWord = reduceWord;
			this.reduceWordPC = reduceWordPC;
			this.increaseWord = increaseWord;
			this.increaseWordPC = increaseWordPC;
		}

		public String getReduceWord() {
			return reduceWord;
		}

		public void setReduceWord(String reduceWord) {
			this.reduceWord = reduceWord;
		}

		public boolean isReduceWordPC() {
			return reduceWordPC;
		}

		public void setReduceWordPC(boolean reduceWordPC) {
			this.reduceWordPC = reduceWordPC;
		}

		public String getIncreaseWord() {
			return increaseWord;
		}

		public void setIncreaseWord(String increaseWord) {
			this.increaseWord = increaseWord;
		}

		public boolean isIncreaseWordPC() {
			return increaseWordPC;
		}

		public void setIncreaseWordPC(boolean increaseWordPC) {
			this.increaseWordPC = increaseWordPC;
		}
	}
	
	private static String getYesterdayOf(String day) {
		DateFormat fmt =new SimpleDateFormat("yyyyMMdd");
		if (StringUtils.isEmpty(day)) {
			day = fmt.format(System.currentTimeMillis() - DateUtil.DAY_MILLIS);
		} else {
			try {
				day = fmt.format(fmt.parse(day).getTime() - DateUtil.DAY_MILLIS);
			} catch (ParseException e) {
				//传入参数时已进行检查，此处不会throw ParseException
			}
		}
		return day;
	}
	
	/*
	 * 跳失率
	 */
	public static void getBounceRate(int interval, Long endTime, Long numIid) {
		User user = getUser();
		itemPlayViewTrade(interval, endTime, numIid, user);
		// 从缓存中读取数据
		String key = user.getId() + "_" + interval+ "_" + sdf.format(new Date(endTime)) + "_" + numIid + "_getBounceRate";
		getCache(key);
		Map<String, AreaViews> map = CloudDataAction.setMapDate(interval, endTime);
		// 计算每天的数量
		int hasDateDayNum = 0;
		for (int i = 1; i <= interval; i++) {
			// 将时间转成yyyyMMdd格式的字符串
			long tempTime = endTime - (interval - i) * DateUtil.DAY_MILLIS;
			String dataTime = sdf.format(new Date(tempTime));
			AreaViews areaViews = map.get(dataTime);
			// 跳失率  boucceRate
			String boucceRate = CloudDataAction.getBounceRate(user, numIid, dataTime);
			if(StringUtils.isEmpty(boucceRate)) {
				continue;
			}
			hasDateDayNum++;
			areaViews.setBounceRate(boucceRate);
		}
		// 加入缓存中
		if(hasDateDayNum == interval){
			Cache.set(key, map, CACHE_TIME);
		}
		renderJSON(JsonUtil.getJson(map));
	}
	
    public static void getItemCollectNum(int interval, Long endTime, Long numIid){
        User user = getUser();
        itemPlayViewTrade(interval, endTime, numIid, user);
        // 从缓存中读取数据
        String key = user.getId() + "_" + interval+ "_" + sdf.format(new Date(endTime)) + "_" + numIid + "_getItemCollectNum";
        getCache(key);
        Map<String, AreaViews> map = CloudDataAction.setMapDate(interval, endTime);
        // 计算每天的数量
        int hasDateDayNum = 0;
        for (int i = 1; i <= interval; i++) {
            // 将时间转成yyyyMMdd格式的字符串
            long tempTime = endTime - (interval - i) * DateUtil.DAY_MILLIS;
            String dataTime = sdf.format(new Date(tempTime));
            AreaViews areaViews = map.get(dataTime);
            // 收藏数  itemCollectNum
            int itemCollectNum = CloudDataAction.getItemCollectNumMap(user, numIid, dataTime, dataTime);
            if(itemCollectNum == -1){
                areaViews.setItemCollectNum("0");
                continue;
            }
            hasDateDayNum++;
            areaViews.setItemCollectNum(String.valueOf(itemCollectNum));
        }
        // 加入缓存中
        if(hasDateDayNum == interval){
            Cache.set(key, map, CACHE_TIME);
        }
        renderJSON(JsonUtil.getJson(map));
    }
    
    public static void getItemCartNum(int interval, Long endTime, Long numIid){
        User user = getUser();
        itemPlayViewTrade(interval, endTime, numIid, user);
        // 从缓存中读取数据
        String key = user.getId() + "_" + interval+ "_" + sdf.format(new Date(endTime)) + "_" + numIid + "_getItemCartNum";
        getCache(key);
        Map<String, AreaViews> map = CloudDataAction.setMapDate(interval, endTime);
        // 计算每天的数量
        int hasDateDayNum = 0;
        for (int i = 1; i <= interval; i++) {
            // 将时间转成yyyyMMdd格式的字符串
            long tempTime = endTime - (interval - i) * DateUtil.DAY_MILLIS;
            String dataTime = sdf.format(new Date(tempTime));
            AreaViews areaViews = map.get(dataTime);
            // 购物车，使用 宝贝加购人数（不是件数）
            Integer itemCartBuyers = CloudDataAction.getItemCartBuyers(user, dataTime, numIid);
            if(itemCartBuyers == -1){
                areaViews.setItemCartNum("0");
                continue;
            }
            hasDateDayNum++;
            areaViews.setItemCartNum(String.valueOf(itemCartBuyers));
        }
        // 加入缓存中
        if(hasDateDayNum == interval){
            Cache.set(key, map, CACHE_TIME);    
        }
        renderJSON(JsonUtil.getJson(map));
    }
    
    public static void testCart(Long numIid, String startdate, String enddate) {
        User user = getUser();
        TMResult<HashMap<Long, Integer>> res = CloudDataAction.getItemCartNumMap(user, numIid, startdate, enddate);
        renderJSON(JsonUtil.getJson(res));
    }

    public static void testCollect(Long numIid, String startdate, String enddate) {
        User user = getUser();
        renderJSON(JsonUtil.getJson(CloudDataAction.getItemCollectNumMap(user, numIid, startdate, enddate)));
    }
    
    public static void getItemByNumIid(Long numIid) {
    	User user = getUser();
    	Item item = new ItemApi.ItemGet(user, numIid, true).call();
    	renderJSON(JsonUtil.getJson(item));
    }
    
    public static void deleteCache(int interval, Long endTime, Long numIid, String userNick) {
        User user = UserDao.findByUserNick(userNick);
        if (user == null) {
            renderError("没有该用户信息");
        }
        String key = user.getId() + "_" + interval + "-" + endTime + "-" + numIid;
        Cache.delete(key);
        renderTMSuccess("删除cache成功");
    }
    
    public static void showKey(int interval) {
    	User user = getUser();
    	Long userId = user.getId();
    	Long endTime = System.currentTimeMillis() - DateUtil.DAY_MILLIS;
    	String endTimeStr = sdf.format(new Date(endTime));
    	String key = userId + endTimeStr + interval;
    	renderText(key);
    }
    
    public static void shopView(Long endTime, int interval){
        User user = getUser();
        if (user == null) {
            renderFailedJson("用户不存在");
        }
        if(endTime == null || endTime == 0){
            endTime = System.currentTimeMillis() - DateUtil.DAY_MILLIS;
        }
        if(interval == 0){
            interval = 7;
        }
        daysBetween(user, interval);
        if (needReduceEndTime(endTime)) {
            endTime -= DateUtil.DAY_MILLIS;
            interval--;
        }
        long startTime = endTime - (interval - 1) * DateUtil.DAY_MILLIS;
        String startTimeStr = sdf.format(new Date(startTime));
        String endTimeStr = sdf.format(new Date(endTime));
        Long userId = user.getId();
        String sessionKey = user.getSessionKey();
        Map<String, AreaViews> map = new TreeMap<String, AreaViews>();
        // 从缓存中读取
        String key = userId + endTimeStr + interval;
        try {
        	for (int i = interval; i >= 1; i--) {
            	String dateStr = sdf.format(new Date(endTime - (interval - i) * DateUtil.DAY_MILLIS));
            	String dayKey = "shopView_" + userId + dateStr;
            	Object obj = Cache.get(dayKey);
            	if(obj != null){
            		map.put(dateStr, (AreaViews) obj);
            	} else {
            		getMBPData(user, endTime, interval, startTimeStr, endTimeStr, userId, sessionKey, key);
            	}
            }
        	renderJSON(JsonUtil.getJson(map));
		} catch (Exception e) {
			getMBPData(user, endTime, interval, startTimeStr, endTimeStr, userId, sessionKey, key);
		}
    }

    private static void getMBPData(User user, long endTime, int interval, String startTimeStr, String endTimeStr, Long userId,
            String sessionKey, String key) {
    	Map<String, AreaViews> map = new HashMap<String, AreaViews>();
        // 如果没数据，则默认返回全为0
//        for (int i = interval; i >= 1; i--) {
//            String dateStr = sdf.format(new Date(endTime - (interval - i) * DateUtil.DAY_MILLIS));
//            map.put(dateStr, new AreaViews(dateStr));
//        }
        // pv, uv, alipay_trade_num, alipay_auction_num,alipay_trade_amt, tradeRate
        TMResult<List<QueryRow>> viewTradeRes = new MBPApi.MBPDataGet(3049L,
                "startdate=" + startTimeStr + ",sellerId=" + userId + ",enddate=" + endTimeStr + ",platform= 0"+ ",@replace_null=NULL", sessionKey).call();
        List<QueryRow> viewTrade = viewTradeRes.getRes();
        if (CommonUtils.isEmpty(viewTrade)) {
            renderFailedJson("找不到对应的流量成交数据");
        }
        for (QueryRow row : viewTrade) {
            List<String> value = row.getValues();
            // 这是计算每日数据
            String dataTime = value.get(13);
            AreaViews tmp = new AreaViews(dataTime);
            tmp.addProp(value.get(3).equals("NULL") ? 0 : Integer.valueOf(value.get(3)),
                    value.get(4).equals("NULL") ? 0 : Integer.valueOf(value.get(4)),
                    value.get(11).equals("NULL") ? 0 : Integer.valueOf(value.get(11)),
                    value.get(9).equals("NULL") ? 0 : Integer.valueOf(value.get(9)),
                    value.get(10).equals("NULL") ? 0 : Double.valueOf(value.get(10)),
                    value.get(11).equals("NULL") ? 0 : Integer.valueOf(value.get(11)));
            if (map.get(dataTime) == null) {
                map.put(dataTime, tmp);
            } else {
                AreaViews old = map.get(dataTime);
                old.addProp(tmp);
                map.put(dataTime, old);
            }
        }
        // 设置站内搜索searchUv  会返回dt
        CloudDataAction.setSearchUVValue(startTimeStr, endTimeStr, userId, sessionKey, map);
        // 设置pc类目UV
        TMResult<List<QueryRow>> viewTradePCRes = new MBPApi.MBPDataGet(3049L,
                "startdate=" + startTimeStr + ",sellerId=" + userId + ",enddate=" + endTimeStr + ",platform=1"+ ",@replace_null=NULL", sessionKey).call();
        List<QueryRow> viewTradePC = viewTradePCRes.getRes();
        if(!CommonUtils.isEmpty(viewTradePC)){
            for (QueryRow view : viewTradePC) {
                List<String> values = view.getValues();
                if(CommonUtils.isEmpty(values)){
                    continue;
                }
                AreaViews areaViews = map.get(values.get(13));
                if(areaViews == null){
                    continue;
                }
                int pcPv = values.get(3).equals("NULL") ? 0 : Integer.parseInt(values.get(3));
                int pcUv = values.get(4).equals("NULL") ? 0 : Integer.parseInt(values.get(4));
                areaViews.addPcUv(pcUv + pcPv);
            }
        }
        
        for (int i = 1; i <= interval; i++) {
            // 将时间转成yyyyMMdd格式的字符串
            long tempTime = endTime - (interval - i) * DateUtil.DAY_MILLIS;
            String dataTime = sdf.format(new Date(tempTime));
            AreaViews areaViews = map.get(dataTime);
            if(areaViews == null) {
            	areaViews = new AreaViews(dataTime);
            }
            // pc入店关键词数量
            TMResult<List<QueryRow>> wordsCountRowsPCRes = new MBPApi.MBPDataGet(2854L,
                    "startdate=" + dataTime + ",sellerId=" + userId + ",enddate=" + dataTime, sessionKey).call();
            List<QueryRow> wordsCountRowsPC = wordsCountRowsPCRes.getRes();
            int countPC = 0;
            if (!CommonUtils.isEmpty(wordsCountRowsPC)) {
                countPC =  wordsCountRowsPC.size();
            }
            // 无线入店关键词的数量
            TMResult<List<QueryRow>> wordsCountRowsWirelessRes = new MBPApi.MBPDataGet(100530L,
                    "startdate=" + dataTime + ",sellerId=" + userId + ",enddate=" + dataTime, sessionKey).call();
            List<QueryRow> wordsCountRowsWireless = wordsCountRowsWirelessRes.getRes();
            int countWireless = 0;
            if (!CommonUtils.isEmpty(wordsCountRowsWireless)) {
                List<String> values = wordsCountRowsWireless.get(0).getValues();
                if(!CommonUtils.isEmpty(values)) {
                    countWireless = StringUtils.isEmpty(values.get(0)) ? 0 : Integer.valueOf(values.get(0));
                }
            }
            areaViews.setEntranceNum(String.valueOf(countWireless + countPC));
            // 收藏数  itemCollectNum
            HashMap<Long, Integer> itemCollectNum = CloudDataAction.getItemCollectNumMap(user, dataTime, dataTime);
            areaViews.addItemCollectNum(itemCollectNum);
            // 购物车 使用sku 加购宝贝数，不准，只有PC
            //HashMap<Long, Integer> skuIdCatNumMap = CloudDataAction.getSkuIdCatNumMap(user, dataTime, dataTime);
            //areaViews.addItemCartNum(skuIdCatNumMap);
            // 购物车， 改用宝贝加购人数（不是加购件数）
            areaViews.setItemCartNum(String.valueOf(CloudDataAction.getShopCartBuyers(user, dataTime)));
        }
        Map<String, AreaViews> returnMap = CloudDataAction.setMapDate(interval, endTime);
        // 放入缓存中
        for (int i = interval; i >= 1; i--) {
            String dateStr = sdf.format(new Date(endTime - (interval - i) * DateUtil.DAY_MILLIS));
            String dayKey = "shopView_" + userId + dateStr;
            if(map.get(dateStr) != null) {
            	Cache.set(dayKey, map.get(dateStr), CACHE_TIME);
            	returnMap.put(dateStr, map.get(dateStr));
            }
        }
        renderJSON(JsonUtil.getJson(returnMap));
    }

    /**
     * 宝贝数据分析    无线端流量来源
     */
    public static void getWireelessItemSource(Long numIid, int interval, Long endTime){
        User user = getUser();
        if (user == null) {
            renderFailedJson("用户不存在");
        }
        if(endTime == null || endTime == 0){
            endTime = System.currentTimeMillis() - DateUtil.DAY_MILLIS;
        }
        if(interval == 0){
            interval = 7;
        }
        if (needReduceEndTime(endTime)) {
            endTime -= DateUtil.DAY_MILLIS;
            interval--;
        }
        Long userId = user.getId();
        String cacheKey = userId + "_" + numIid + "_" + interval + "_" + sdf.format(new Date(endTime)) + "_" + "getWireelessItemSource";
        try {
            List<WirelessSrcUvPv> list = (List<WirelessSrcUvPv>) Cache.get(cacheKey);
            if (list != null) {
                renderSuccess(StringUtils.EMPTY, list);
            }
        } catch(Exception e) {
            log.error("key " + cacheKey + " 没有对应的缓存");
        }
        daysBetween(user, interval);
        long startTime = endTime - (interval - 1) * DateUtil.DAY_MILLIS;
        TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(105181L,
                "startDate=" + sdf.format(new Date(startTime)) + ",sellerId=" + user.getId() + 
                ",endDate=" + sdf.format(new Date(endTime)) + ",numIid=" + numIid, user.getSessionKey()).call();
        List<QueryRow> wirelessWordsCountRows = res.getRes();
        if(CommonUtils.isEmpty(wirelessWordsCountRows)){
            renderError("没有数据");
        }
        List<WirelessSrcUvPv> srcLevelThree = new ArrayList<WirelessSrcUvPv>();
        // 记录src_level相同的WirelessSrcUvPv
        List<WirelessSrcUvPv> srcLevelTwo = new ArrayList<WirelessSrcUvPv>();
        for (QueryRow row : wirelessWordsCountRows) {
            WirelessSrcUvPv src = new WirelessSrcUvPv(row, user);
            if(src.getSrcLevel() == 2){
                srcLevelTwo.add(src);
                continue;
            }
            srcLevelThree.add(src);
        }
        List<WirelessSrcUvPv> list = WirelessSrcUvPv.sort(srcLevelTwo, srcLevelThree);
        Cache.set(cacheKey, list, "20h");
        renderSuccess(StringUtils.EMPTY, list);
    }
    
    // 清除无线流量来源数据缓存
    public static void cleanCache(String nick) {
    	if(StringUtils.isEmpty(nick)) {
    		renderText("请输入旺旺号");
    	}
    	User user = UserDao.findByUserNick(nick);
    	if(user == null) {
    		renderText("没有这个用户");
    	}
    	Long userId = user.getId();
    	Long endTime = System.currentTimeMillis() - DateUtil.DAY_MILLIS;
    	
    	List<ItemPlay> itemList = ItemDao.findByUserId(userId);
    	if(CommonUtils.isEmpty(itemList)) {
    		renderText("宝贝列表为空");
    	}
    	for (int interval = 1; interval <= 14; interval++) {
    		for (ItemPlay item : itemList) {
    			String cacheKey = userId + "_" + item.getNumIid() + "_" + interval + "_" + sdf.format(new Date(endTime)) + "_" + "getWireelessItemSource";
    			Cache.delete(cacheKey);
			}
		}
    	renderText("操作成功");
    }
    
    public static void getImpression(int interval, Long endTime, Long numIid){
        User user = getUser();
        itemPlayViewTrade(interval, endTime, numIid, user);
        Long userId = user.getId();
        // 从缓存中读取数据
        String key = userId + "_" + interval+ "_" + sdf.format(new Date(endTime)) + "_" + numIid + "_getImpression";
        getCache(key);
        Map<String, AreaViews> map = CloudDataAction.setMapDate(interval, endTime);
        // 计算每天的数量
        int hasDateDayNum = 0;
        for (int i = 1; i <= interval; i++) {
            // 将时间转成yyyyMMdd格式的字符串
            long tempTime = endTime - (interval - i) * DateUtil.DAY_MILLIS;
            String dataTime = sdf.format(new Date(tempTime));
            AreaViews areaViews = map.get(dataTime);
            // 展现量
            String impression = CloudDataAction.getImpression(dataTime, userId, numIid, user.getSessionKey());
            if("-1".equals(impression)){
                areaViews.setImpression("0");
                continue;
            }
            hasDateDayNum++;
            areaViews.setImpression(impression);
        }
        // 加入缓存中
        if(hasDateDayNum == interval){
            Cache.set(key, map, CACHE_TIME);
        }
        renderJSON(JsonUtil.getJson(map));
    }
    
    public static void clearCache(String key){
        if(StringUtils.isEmpty(key)){
            renderError("请输入key！");
        }
        Cache.delete(key);
        renderTMSuccess("删除成功。");
    }
    
	// 删除宝贝流量分析缓存
	public static void clearCacheForUser(String nick, int interval, String end){
		if(StringUtils.isEmpty(nick)) {
			renderText("请输入旺旺号!");
		}
		if(interval <= 0) {
			renderText("请输入时间间隔！");
		}
		if(StringUtils.isEmpty(end)) {
			renderText("请输入结束时间！");
		}
		User user = UserDao.findByUserNick(nick);
		if(user == null) {
			renderText("没有这个用户");
		}
		Long userId = user.getId();
		
		List<ItemPlay> itemList = ItemDao.findByUserId(userId);
		if(CommonUtils.isEmpty(itemList)) {
			renderText("宝贝列表为空");
		}
		
		Long endTime = 0L;
		try {
			endTime = sdf.parse(end).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Long startTime = endTime - (interval - 1) * DateUtil.DAY_MILLIS;
		String startDate = sdf.format(new Date(startTime));
		String endDate = sdf.format(new Date(endTime));
		
		for (ItemPlay item : itemList) {
			Long numIid = item.getNumIid();
			String key = userId + "_" +  numIid + "_" + startDate + "_" + endDate + "_NewUvPvDiagResult";
			Cache.delete(key);
			for(int i = 1; i <= interval; i++) {
				long tempTime = endTime - (interval - i) * DateUtil.DAY_MILLIS;
				String dataTime = sdf.format(new Date(tempTime));
				String collectKey = userId + "_" +  numIid + "_" + dataTime + "_NewUvPvDiagResult_collect";
				Cache.delete(collectKey);
				String cartKey = userId + "_" +  numIid + "_" + dataTime + "_NewUvPvDiagResult_cart";
				Cache.delete(cartKey);
			}
		}
		renderText("操作成功");
	}
    
    public static void appItem(String sellerNick, Long numIid, int pn, int ps, int interval, Long endTime,
            String orderBy, boolean isDesc, String coreWord){
        if (interval < 1) {
            interval = 7;
        }
        if (endTime == null) {
            endTime = System.currentTimeMillis();
        }
        orderBy = ensureAppOrderBy(orderBy);
        User user = UserDao.findByUserNick(sellerNick);
        if (user == null) {
            renderFailedJson("用户不存在");
        }
        if (numIid == null) {
            renderFailedJson("请输入正确的宝贝ID");
        }
        daysBetween(user, interval);
        
        ItemPlay itemPlay = ItemDao.findByNumIid(user.getId(), numIid);
        new RecentlyDiagedItem(numIid, user.getId(), System.currentTimeMillis(),
                itemPlay.getPicURL()).jdbcSave();
        PageOffset po = new PageOffset(pn, ps, 10);
        List<QueryRow> rows = new ArrayList<QueryRow>();
        Long startTime = endTime - (interval - 1) * DateUtil.DAY_MILLIS;
        // 核心关键词不为空时
        boolean empty = !StringUtils.isEmpty(coreWord);
        if(empty){
            // 计算该宝贝时间段内所有入店关键词总数
            TMResult<List<QueryRow>> wordsCountRes = new MBPApi.MBPDataGet(100531L,
                    "startdate=" + sdf.format(new Date(startTime)) + ",sellerId=" + user.getId() +
                    ",enddate=" + sdf.format(new Date(endTime)) + ",numIid=" + numIid, user.getSessionKey()).call();
            List<QueryRow> wordsCountRows = wordsCountRes.getRes();
            int count = 0;
            if (!CommonUtils.isEmpty(wordsCountRows)) {
                List<String> values = wordsCountRows.get(0).getValues();
                if(CommonUtils.isEmpty(values) == false) {
                    count = StringUtils.isEmpty(values.get(0)) ? 0 : Integer.valueOf(values.get(0));
                }
            }
            po.setPs(count);
        }
        if(isDesc){
            TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(100495L,
                    "startdate=" + sdf.format(new Date(startTime)) + ",numIid=" + numIid +
                    ",sellerId=" + user.getId() + ",enddate=" + sdf.format(new Date(endTime)) + ",sub_offset=" + po.getOffset() +
                    ",sub_limit=" + po.getPs() + ",sub_order_by=" + orderBy, user.getSessionKey()).call();
            rows = res.getRes();
        } else {
            TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(107054L,
                    "startdate=" + sdf.format(new Date(startTime)) + ",numIid=" + numIid +
                    ",sellerId=" + user.getId() + ",enddate=" + sdf.format(new Date(endTime)) + ",sub_offset=" + po.getOffset() +
                    ",sub_limit=" + po.getPs() + ",sub_order_by=" + orderBy, user.getSessionKey()).call();
            rows = res.getRes();
        }
        List<WordInfo> wordInfos = new ArrayList<WordInfo>();
        if (CommonUtils.isEmpty(rows)) {
            renderJSON(JsonUtil.getJson(new TMResult(wordInfos, 0, po)));
        }
        for (QueryRow row : rows) {
            wordInfos.add(WordInfo.buildAppWithNoDate(row));
        }
        if(empty){
            String[] coreWords = coreWord.split(",");
            wordInfos = queryCoreWord(wordInfos, coreWords, ps);
        }
        renderJSON(JsonUtil.getJson(new TMResult(true, po.getPn(), po.getPs(), 0, StringUtils.EMPTY, wordInfos)));
    }
    
    private static List<WordInfo> queryCoreWord(List<WordInfo> wordInfos, String[] coreWords, int ps){
        Iterator<WordInfo> iterator = wordInfos.iterator();
        List<WordInfo> wordInfosResult = new ArrayList<WordInfo>();
        while (iterator.hasNext()){
            WordInfo wordInfo = iterator.next();
            for (int i = 0; i < coreWords.length; i++) {
                boolean contains = wordInfo.word.contains(coreWords[i]);
                if(contains){
                    wordInfosResult.add(wordInfo);
                    break;
                }
            }
            if(wordInfosResult.size() == ps){
                return wordInfosResult;
            }
        }
        return wordInfosResult;
    }

    public static void pcItem(Long numIid, int pn, int ps, int interval, Long endTime, String sellerNick,
            String orderBy, boolean isDesc, String coreWord){
        if (interval < 1) {
            interval = 7;
        }
        if (endTime == null) {
            endTime = System.currentTimeMillis();
        }
        orderBy = ensureOrderBy(orderBy);
        User user = UserDao.findByUserNick(sellerNick);
        if (user == null) {
            renderFailedJson("用户不存在");
        }
        daysBetween(user, interval);
        if (numIid == null) {
            RecentlyDiagedItem recently = RecentlyDiagedItem.findMostRecently(user.getId());
            if (recently != null) {
                numIid = recently.getNumIid();
            } else {
                ItemPlay firstItemPlay = ItemDao.findFirstItemByUserId(user.getId());
                if (firstItemPlay == null) {
                    renderFailedJson("当前用户未存在宝贝");
                }
                numIid = firstItemPlay.getNumIid();
            }

        }
        ItemPlay itemPlay = ItemDao.findByNumIid(user.getId(), numIid);
        new RecentlyDiagedItem(numIid, user.getId(), System.currentTimeMillis(),
                itemPlay.getPicURL()).jdbcSave();
        PageOffset po = new PageOffset(pn, ps, 10);
        List<QueryRow> rows = new ArrayList<QueryRow>();
        Long startTime = endTime - (interval - 1) * DateUtil.DAY_MILLIS;
        boolean empty = !StringUtils.isEmpty(coreWord);
        if(empty){
            // 计算该宝贝时间段内所有入店关键词总数
            TMResult<List<QueryRow>> wordsCountRes = new MBPApi.MBPDataGet(2853L,
                    "startdate=" + sdf.format(new Date(startTime)) + ",sellerId=" + user.getId() +
                    ",enddate=" + sdf.format(new Date(endTime)) + ",numIid=" + numIid, user.getSessionKey()).call();
            List<QueryRow> wordsCountRows = wordsCountRes.getRes();
            int count = 0;
            if (!CommonUtils.isEmpty(wordsCountRows)) {
                count = wordsCountRows.size();
            }
            po.setPs(count);
        }
        if(isDesc){
            TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(2725L, "startdate=" + sdf.format(new Date(startTime)) + ",numIid=" + numIid +
                    ",sellerId=" + user.getId() + ",enddate=" + sdf.format(new Date(endTime)) + ",sub_offset=" + po.getOffset() +
                    ",sub_limit=" + po.getPs() + ",sub_order_by=" + orderBy, user.getSessionKey()).call();
            rows = res.getRes();
        } else {
            TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(107055L, "startdate=" + sdf.format(new Date(startTime)) + ",numIid=" + numIid +
                    ",sellerId=" + user.getId() + ",enddate=" + sdf.format(new Date(endTime)) + ",sub_offset=" + po.getOffset() +
                    ",sub_limit=" + po.getPs() + ",sub_order_by=" + orderBy, user.getSessionKey()).call();
            rows = res.getRes();
        }
        List<WordInfo> wordInfos = new ArrayList<WordInfo>();
        if (CommonUtils.isEmpty(rows)) {
            renderJSON(JsonUtil.getJson(new TMResult(wordInfos, 0, po)));
        }
        for (QueryRow row : rows) {
            wordInfos.add(WordInfo.buildWithNoDate(row));
        }
        if(empty){
            String[] coreWords = coreWord.split(",");
            wordInfos = queryCoreWord(wordInfos, coreWords, ps);
        }
        renderJSON(JsonUtil.getJson(new TMResult(true, po.getPn(), po.getPs(), 0, StringUtils.EMPTY, wordInfos)));
    }
    
    public static void getSkuDetail(int interval, Long endTime, Long numIid){
        User user = getUser();
        long startTime = itemPlayViewTrade(interval, endTime, numIid, user);
        Long userId = user.getId();
        
        String cacheKey = userId + "_" + numIid + "_" + interval + "_" + sdf.format(new Date(endTime)) + "_" + "getSkuDetail";
        try {
        	List<SkuDetail> list = (List<SkuDetail>) Cache.get(cacheKey);
        	if (list != null) {
        		renderSuccess("", list);
        	}
        } catch(Exception e) {
        	log.error("key " + cacheKey + " 没有对应的缓存");
        }
        
        List<SkuDetail> result = new ArrayList<SkuDetail>();
        List<Sku> skus = ItemGetAction.getSkus(String.valueOf(numIid));
        if(CommonUtils.isEmpty(skus)) {
        	renderError("sku获取失败！");
        }
        
        for (Sku sku : skus) {
            SkuDetail skuDetail = new SkuDetail(sku);
            result.add(skuDetail);
        }
        
        TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(111016L, "startdate=" + sdf.format(new Date(startTime)) +
                ",enddate=" + sdf.format(new Date(endTime)) + ",sellerId=" + userId, user.getSessionKey()).call();
        List<QueryRow> skuRows = res.getRes();
        
        if (CommonUtils.isEmpty(skuRows)) {
            renderJSON(JsonUtil.getJson(result));
        }
        
        for (SkuDetail sku : result) {
        	for (QueryRow row : skuRows) {
        		sku.addProps(row);
        	}
        }
        
        Cache.set(cacheKey, result, "20h");
        renderSuccess("", result);
    }
    
    public static void exportSkuExcel(int interval, Long endTime, Long numIid){
        User user = getUser();
        long startTime = itemPlayViewTrade(interval, endTime, numIid, user);
        Long userId = user.getId();
        
        List<String[]> skuDetails = new ArrayList<String[]>();
        
        List<SkuDetail> result = new ArrayList<SkuDetail>();
        List<Sku> skus = ItemGetAction.getSkus(String.valueOf(numIid));
        if(CommonUtils.isEmpty(skus)) {
        	generateSkuExcel(skuDetails, interval, numIid, user);
        }
        
        for (Sku sku : skus) {
            SkuDetail skuDetail = new SkuDetail(sku);
            result.add(skuDetail);
        }
        
        TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(111016L, "startdate=" + sdf.format(new Date(startTime)) +
                ",enddate=" + sdf.format(new Date(endTime)) + ",sellerId=" + userId, user.getSessionKey()).call();
        List<QueryRow> skuRows = res.getRes();
        
        if (!CommonUtils.isEmpty(skuRows)) {
        	for (SkuDetail sku : result) {
        		for (QueryRow row : skuRows) {
        			sku.addProps(row);
        		}
        	}
        }
        
        for (SkuDetail sku : result) {
        	skuDetails.add(SkuDetail.skuDetail2String(sku));
        }
        
        generateSkuExcel(skuDetails, interval, numIid, user);
    }
    
    private static void generateSkuExcel(List<String[]> skuDetails, int interval, Long numIid, User user) {
        String fileName = Play.tmpDir.getPath() + "/[SKU销售详情]" + numIid + "_" + interval + "天" + ".xls";
        String sheetName = "SKU销售详情";
        String fields = "SKU信息,价格,当前库存,新增加购人数,下单件数,下单买家数,支付件数,支付买家数";
        ExcelUtil.writeToExcel(skuDetails, fields, sheetName, fileName);
        File file = new File(fileName);
        renderBinary(file);
    }
    
	// 直通车搜索UV
	public static void getCPCUV(Long numIid, int interval, Long endTime) {
		User user = getUser();
		long startTime = itemPlayViewTrade(interval, endTime, numIid, user);
		Long userId = user.getId();
		// 从缓存中读取数据
		String key = userId + "_" + interval+ "_" + sdf.format(new Date(endTime)) + "_" + numIid + "_getCPCUV";
		getCache(key);
		Map<String, CPCUvPv> map = CloudDataAction.setCPCMap(interval, endTime);
		// 直通车PC端uv
		TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(111541L,
				"startDate=" + sdf.format(new Date(startTime)) + ",endDate=" + sdf.format(new Date(endTime)) +
				",sellerId=" + userId + ",numIid=" + numIid, user.getSessionKey()).call();
		List<QueryRow> pcUV = res.getRes();
		if(!CommonUtils.isEmpty(pcUV)) {
			for(QueryRow pc : pcUV) {
				List<String> values = pc.getValues();
				if(CommonUtils.isEmpty(values)) {
					continue;
				}
				CPCUvPv search = map.get(values.get(0));
				if(search == null) {
					continue;
				}
				search.addSearchUv(values.get(6));
			}
		}
		// 直通车无线端searchUv
		TMResult<List<QueryRow>> appRes = new MBPApi.MBPDataGet(111540L,
				"startDate=" + sdf.format(new Date(startTime)) + ",endDate=" + sdf.format(new Date(endTime)) +
				",sellerId=" + userId + ",numIid=" + numIid, user.getSessionKey()).call();
		List<QueryRow> appUV = appRes.getRes();
		if(!CommonUtils.isEmpty(appUV)) {
			for(QueryRow app : appUV) {
				List<String> values = app.getValues();
				if(CommonUtils.isEmpty(values)) {
					continue;
				}
				CPCUvPv search = map.get(values.get(0));
				if(search == null) {
					continue;
				}
				search.addSearchUv(values.get(7));
			}
		}
		
		if(!CommonUtils.isEmpty(pcUV) || !CommonUtils.isEmpty(appUV)) {
			// 加入缓存中
			Cache.set(key, map, CACHE_TIME);
		}
		
		renderJSON(JsonUtil.getJson(map));
	}
	
	// 清除用户所有缓存（流量分析相关）
	public static void cleanAllCacheForUser(String nick) {
		if(StringUtils.isEmpty(nick)) {
			renderText("请输入旺旺号");
		}
		User user = UserDao.findByUserNick(nick);
		if(user == null) {
			renderText("该用户不存在");
		}
		Long userId = user.getId();
		
		List<ItemPlay> list = ItemDao.findByUserId(userId);
		if (CommonUtils.isEmpty(list)) {
			renderText("宝贝列表为空");
		}
		
		Long endTime = System.currentTimeMillis() - DateUtil.DAY_MILLIS;
		for (int interval = 1; interval <= 14; interval++) {
			for (ItemPlay item : list) {
				Long startTime = endTime - (interval - 1) * DateUtil.DAY_MILLIS;
				String startDate = sdf.format(new Date(startTime));
				String endDate = sdf.format(new Date(endTime));
				
				// 宝贝基本数据
				String key = userId + "_" +  item.getNumIid() + "_" + startDate + "_" + endDate + "_NewUvPvDiagResult";
				Cache.delete(key);
				
				for (int i = 1; i <= interval; i++) {
					long tempTime = endTime - (interval - i) * DateUtil.DAY_MILLIS;
					String dataTime = sdf.format(new Date(tempTime));
					// 首页数据
					String shopViewKey = "shopView_" + userId + dataTime;
					Cache.delete(shopViewKey);
					
					// 收藏
					String collectKey = userId + "_" +  item.getNumIid() + "_" + dataTime + "_NewUvPvDiagResult_collect";
					Cache.delete(collectKey);
					
					String collect = "ItemCollectNumMapPre_" + userId + dataTime + "_" + dataTime;
					Cache.delete(collect);
					
					// 加购
					String cartKey = userId + "_" +  item.getNumIid() + "_" + dataTime + "_NewUvPvDiagResult_cart";
					Cache.delete(cartKey);
					
					String cart = "ItemCartBuyerPre_" + userId + dataTime + item.getNumIid();
					Cache.delete(cart);
				}
				
				// 效果分析-入口数
				String entranceKey = userId + "_" + interval + "_" + endDate + "_" + item.getNumIid() + "_getEntranceNum";
				Cache.delete(entranceKey);
				// 效果分析-收藏
				String itemCollectNumKey = userId + "_" + interval+ "_" + endDate + "_" + item.getNumIid() + "_getItemCollectNum";
				Cache.delete(itemCollectNumKey);
				// 效果分析-加购
				String itemCartNumKey = userId + "_" + interval+ "_" + endDate + "_" + item.getNumIid() + "_getItemCartNum";
				Cache.delete(itemCartNumKey);
				// 效果分析-搜索UV
				String searchUvKey = userId + "_" + interval+ "_" + endDate + "_" + item.getNumIid() + "_getSearchUV";
				Cache.delete(searchUvKey);
				// 效果分析-直通车UV
				String cpcUvKey = userId + "_" + interval+ "_" + endDate + "_" + item.getNumIid() + "_getCPCUV";
				Cache.delete(cpcUvKey);
			}
		}
		renderText("操作成功！");
	}
	
	// 检查淘掌柜首页运营联系方式
	public static void checkUserContact() {
		User user = getUser();
		
		UserContact userContact = UserContact.findByUserId(user.getId());
		if(userContact == null) {
			renderError("尚未保存运营联系方式！");
		}
		
		renderSuccess("运营联系方式已保存！", "");
	}
	
	// 淘掌柜首页运营联系方式
	public static void saveUserContact(String mobile) {
		if(StringUtils.isEmpty(mobile)) {
			renderError("请先填写手机号码！");
		}
		
		Boolean isMobile = CommonUtil.isMobile(mobile);
		if(!isMobile) {
			renderError("请输入正确的手机号码！");
		}
		
		User user = getUser();
		
		UserContact exist = UserContact.findByUserId(user.getId());
		if(exist != null) {
			renderError("运营联系方式已成功保存，请勿重复提交！");
		}
		
		UserContact userContact = new UserContact(mobile, user.getUserNick(), user.getId());
		boolean success = userContact.jdbcSave();
		if(!success) {
			renderError("运营联系方式保存失败，数据库异常！");
		}
		
		renderSuccess("运营联系方式保存成功，感谢您对我们的支持！", "");
	}
    
    // 琳琅秀数据页面
    public static void itemPvUv() {
    	render("/linlangxiu/itemPvUv.html");
    }
    
    // 琳琅秀欢迎页面
    public static void welcome() {
    	render("/linlangxiu/welcome.html");
    }
	
}
