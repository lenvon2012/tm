package controllers;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jdp.ApiJdpAdapter;
import job.ItemCatOrderPayTimeDisTributeJob;
import models.item.ItemPlay;
import models.mainsearch.MainSearchHistory;
import models.op.RawId;
import models.search.UserSearchWordLog;
import models.search.UserSearchWordLog.UserSearchWordType;
import models.user.User;
import models.word.top.NoMatchTopURLBaseCid;
import models.word.top.TopKey;
import models.word.top.TopURLBase;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.cache.Cache;
import play.db.jpa.NoTransaction;
import result.TMResult;
import spider.mainsearch.MainSearchApi.MainSearchParams;
import spider.mainsearch.MainSearchApi.TBSearchRes;
import spider.mainsearch.MainSearchKeywordsUpdater;
import spider.mainsearch.MainSearchKeywordsUpdater.ForSearchAreaCaller;
import spider.mainsearch.MainSearchKeywordsUpdater.MainSearchCache;
import spider.mainsearch.MainSearchKeywordsUpdater.MainSearchItemRank;
import spider.mainsearch.MainSearchKeywordsUpdater.MainSearchPageCaller;
import transaction.DBBuilder.DataSrc;
import utils.PolicyDBUtil;
import actions.NewUvPvDiagAction;
import actions.UserAction;
import actions.UserLoginAction;
import actions.wireless.MobileRankAction;
import bustbapi.ItemApi;
import bustbapi.JDPApi.JuShiTaGetUsers;
import bustbapi.SubUserApi.SellercenterSubusersGet;
import bustbapi.TBApi;
import bustbapi.UserAPIs;
import bustbapi.request.FuwuScoresGetRequest;
import bustbapi.response.FuwuScoresGetResponse;
import cache.CountSellerCatCache;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.JsonUtil;
import com.taobao.api.ApiException;
import com.taobao.api.TaobaoClient;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.SellerCat;
import com.taobao.api.domain.SubUserInfo;

import configs.TMConfigs;
import configs.TMConfigs.App;
import configs.TMConfigs.WebParams;
import dao.UserDao;
import dao.item.ItemDao;

public class Home extends TMController {

    private static final Logger log = LoggerFactory.getLogger(Home.class);

    public static final String TAG = "Home";

    static String PARAM_COME_ON = "_come_on";

    public static void QNTbtIndex(boolean isFirst, String sid) {
    	render("QianNiu/Tbt/QianNiuTbtIndex.html");
    }
    
    public static void testuser() {
    	com.taobao.api.domain.User calledUser = new UserAPIs.UserGetApi("6202b2258dff85f62dff5b721125e9ZZ92aZZb8824602a52909728998", null).call();
    	renderJSON(JsonUtil.getJson(calledUser));
    }
    
    /**
     * http://z.tobti.com/in/login?top_appkey=21255577&top_parameters=
     * ZXhwaXJlc19pbj04NjQwMCZpZnJhbWU9MSZyMV9leHBpcmVzX2luPTg2NDAwJnIyX2V4cGlyZXNfaW49ODY0MDAmcmVfZXhwaXJlc19pbj04NjQwMCZyZWZyZXNoX3Rva2VuPTYxMDE0MDIyZWIxYjk0NzljMmQ4NTFmMDdjYWMyYjUxYzg0YzJmYmYwMTNjZDJlMTAzOTYyNjM4MiZ0cz0xMzUyNDM1NDQzMzY4JnZpc2l0b3JfaWQ9MTAzOTYyNjM4MiZ2aXNpdG9yX25pY2s95qWa5LmL5bCP5Y2XJncxX2V4cGlyZXNfaW49ODY0MDAmdzJfZXhwaXJlc19pbj0xODAw
     * &top_session=6101a02cfa14998f669530d37b232b99516dfab9981ac141039626382&
     * encode=utf-8&agreement=true&agreementsign=21255577-22540590-1039626382-
     * 40294EDA8EE670AD5052F9237D4DA8BF&top_sign=W4voC0xtgqkKsS8p4f38EA%3D%3D
     */
    public static void index(boolean isFirst, String sid) {
        if ("jd".equals(Play.id)) {
            redirect("http://www.jd.com");
        }

        if (!StringUtils.isEmpty(App.HOME_TARGET)) {
            if ("jurenqi".equals(App.HOME_TARGET)) {
                promote();
            }
        }

        if (TMConfigs.App.ENABLE_HELICOPTER == false) {
            // String comeon = session.get(PARAM_COME_ON);
            // if (comeon == null && Operate.REPAY_FOR_FREE) {
            // session.put(PARAM_COME_ON, Boolean.FALSE.toString());
            // } else {
            // session.put(PARAM_COME_ON, Boolean.TRUE.toString());
            // }

            // putUser(getUser());
            // render("Application/newindex.html", isFirst);
            render("Application/tbtwait.html");
            // tbtIndex(isFirst);
        } else {

            // render("/Application/correlativeRecom.html");
            putUser(getUser());
            shopDiag();
        }
    }

    public static void tbtWait(String isFirst) {
        render("Application/tbtwait.html");
    }

    public static void tbtIndex(boolean isFirst) {
        render("Application/newindex.html", isFirst);
    }
    
    public static void newTbtIndex(boolean isFirst) {
    	User user =  getUser();
    	boolean needRedirect = NewUvPvDiagAction.isNeedRedirect(user);
    	if(needRedirect) {
    		redirect(App.CONTAINER_TAOBAO_URL);
    	}
        render("Application/tbtIndex.html", isFirst);
    }

    public static void relation() {
        render("autoTitle/relation.html");
    }

    public static void classes() {
        render("Application/classes.html");
    }

    public static void catClickRate() {
        render("Application/catClickRate.html");
    }

    public static void tbtDazheItems() {
        render("Application/tbtDazheItems.html", false);
    }

    public static void tbtDazheItemPromote() {
        render("Application/promoteItem.html", false);
    }

    public static void getSellerCat(Long userId) {
        Map<SellerCat, Integer> map = CountSellerCatCache.get().getByUser(getUser());
        renderJSON(JsonUtil.getJson(map.keySet()));
    }

    public static void newIndex() {
        boolean isFirst = false;
        render("Application/newindex.html", isFirst);
    }

    public static void commsTab() {
        render("Application/commsTab.html");
    }

    public static void newkeywords() {
        render("keywords/keywords.html");
    }

    public static void autoIndex(String sid, boolean isFirst) {
        render("autoTitle/autoIndex.html");
    }

    public static void autoTitle() {
        render("autoTitle/autoTitle.html");
    }

    public static void mylexicon() {
        render("keywords/mylexicon.html");
    }

    public static void shopDiag() {
        render("Application/shopDiag.html");
    }

    public static void taozhanguiDiag() {
        render("Application/taozhangguiDiag.html");
    }

    public static void mainsearch() {
        render("txm/mainsearch.html");
    }

    public static void instantsearch() {
        render("txm/instantsearch.html");
    }

    public static void searchhistory() {
        render("txm/searchhistory.html");
    }
    
    public static void mobilerank() {
        render("txm/mobileRank.html");
    }

    public static void queryLatestSearchWords(int searchType) {
        User user = getUser();

        int limit = 10;

        List<String> wordList = UserSearchWordLog.findLatestWordsByUserIdAndType(user.getId(), searchType, limit);

        renderBusJson(wordList);
    }

//    public static void doSearchNow(String word, String sort, int pages) {
//    @NoTransaction

	public static void oldDoMobileRank(MainSearchParams params) {


		log.info("[find params :]" + params);
		String word = params.getWord();
		int pn = params.getPageNum();
		User user = getUser();
		UserSearchWordLog searchLog = new UserSearchWordLog(user.getId(), word,
				UserSearchWordType.MobileRank, System.currentTimeMillis());

		searchLog.jdbcSave();
		String cacheKey = MainSearchCache.genMobileCacheKey(params);
		List<MainSearchItemRank> ranks = MainSearchCache
				.getMobileRankFromCache(cacheKey);
		if (CommonUtils.isEmpty(ranks)) {
			ranks = MobileRankAction.MobileSearchRank(word, pn, user);
		}
		if (CommonUtils.isEmpty(ranks)) {
			TMResult res = new TMResult(true, "",
					new ArrayList<MainSearchItemRank>());
			new MainSearchHistory(user.getIdlong(), word, "手机查排名").jdbcSave();
			renderJSON(JsonUtil.getJson(res));
		}
		MainSearchCache.putIntoMobileCache(cacheKey, ranks);
		for (MainSearchItemRank itemRank : ranks) {
            new MainSearchHistory(user.getId(), itemRank, "手机查排名").jdbcSave();
        }
		TMResult res = new TMResult(true, "", ranks);
		renderJSON(JsonUtil.getJson(res));
	}
    
	public static void doMobileRank(MainSearchParams params) {

		log.info("[find params :]" + params);
		String word = params.getWord();
		int pn = params.getPageNum();
		User user = getUser();
		UserSearchWordLog searchLog = new UserSearchWordLog(user.getId(), word,
				UserSearchWordType.MobileRank, System.currentTimeMillis());

		searchLog.jdbcSave();
		String cacheKey = MainSearchCache.genMobileCacheKey(params);
		List<MainSearchItemRank> ranks = MainSearchCache
				.getMobileRankFromCache(cacheKey);
		if (CommonUtils.isEmpty(ranks)) {
			ranks = MobileRankAction.MobileSearchRank(word, pn, user);
		}
		if (CommonUtils.isEmpty(ranks)) {
			TMResult res = new TMResult(true, "",
					new ArrayList<MainSearchItemRank>());
			new MainSearchHistory(user.getIdlong(), word, "手机查排名").jdbcSave();
			renderJSON(JsonUtil.getJson(res));
		}
		MainSearchCache.putIntoMobileCache(cacheKey, ranks);
		String userNick = user.getUserNick();
		List<MainSearchItemRank> myRanks = new ArrayList<MainSearchKeywordsUpdater.MainSearchItemRank>();
		for (MainSearchItemRank itemRank : ranks) {
			if(userNick.equals(itemRank.getNick())) {
				new MainSearchHistory(user.getId(), itemRank, "手机查排名").jdbcSave();
				myRanks.add(itemRank);
			}
        }
		TMResult res = new TMResult(true, "", myRanks);
		renderJSON(JsonUtil.getJson(res));
	}
	
    public static void doSearchNow(MainSearchParams params) {


        log.info("[find params :]" + params);
        String word = params.getWord();
        String sort = params.getOrder();
        String allAreas = params.getQueryArea();
        if (StringUtils.isEmpty(allAreas)) {
            allAreas = "默认";
        }
        String areas[] = allAreas.split("-");      
        List<MainSearchItemRank> list = new ArrayList<MainSearchItemRank>();
        
        if (StringUtils.isBlank(word)) {
            renderError("请输入关键词后，点击查询！");
        }
//        word = word.trim();
        User user = getUser();
        List<ItemPlay> items = ItemDao.findByUserId(user.getId());
        if (CommonUtils.isEmpty(items)) {
                renderError("亲，你还没有宝贝信息哦~点击右上角【更新数据】");
            }
        
        //最近查询
        UserSearchWordLog searchLog = new UserSearchWordLog(user.getId(), word, UserSearchWordType.QueryRank, System.currentTimeMillis());       
        searchLog.jdbcSave();
         
        //存放查询结果
        if (areas.length > 1) {
            ExecutorService threadPool = Executors.newCachedThreadPool();
            CompletionService<HashMap<Long, MainSearchItemRank> > cs = new ExecutorCompletionService<HashMap<Long, MainSearchItemRank>>(threadPool);
            for(String queryArea : areas){  
                    params.setQueryArea(queryArea);     
                    log.info("doSearchNow for userNick: " + user.getUserNick() + " with queryArea = " + queryArea);
                    cs.submit(new ForSearchAreaCaller(new MainSearchParams(params)));
            }
          
            for(String queryArea : areas){
                    try {
                        HashMap<Long, MainSearchItemRank> map = cs.take().get();
                        /*if (CommonUtils.isEmpty(map)) {
                                renderError("淘宝搜索失败，请稍后再试！");
                              }*/
                        if(!CommonUtils.isEmpty(map)) {
                        	for (ItemPlay itemPlay : items) {
                                MainSearchItemRank itemRank = map.get(itemPlay.getNumIid());
                                if (itemRank != null) {
                                          itemRank.setPicPath(itemPlay.getPicURL());
                                          list.add(itemRank);
                                                }
                              }
                        }
                        
                              
                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                    } catch (ExecutionException e) {
                        log.error(e.getMessage(), e);
                    }
            }  
        } else {
            try {
                params.setQueryArea(allAreas);
                Map<Long, MainSearchItemRank> map = new ForSearchAreaCaller(new MainSearchParams(params)).call();
                
                /*if (CommonUtils.isEmpty(map)) {
                    renderError("淘宝搜索失败，请稍后再试！");
                }*/
                if(!CommonUtils.isEmpty(map)) {
                	for (ItemPlay itemPlay : items) {
	                    MainSearchItemRank itemRank = map.get(itemPlay.getNumIid());
	                    if (itemRank != null) {
	                        itemRank.setPicPath(itemPlay.getPicURL());
	                        list.add(itemRank);
	                    }
	                }
                }
               
                      
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            
        }
        
        // save log
        if (CommonUtils.isEmpty(list)) {
                new MainSearchHistory(user.getIdlong(), word, sort).jdbcSave();
         } else {
                for (MainSearchItemRank itemRank : list) {
                          new MainSearchHistory(itemRank, sort).jdbcSave();
                     }
         }
        
        TMResult res = new TMResult(true, sort, list);
        renderJSON(JsonUtil.getJson(res));
   
    }

//    @NoTransaction
    public static void querySearchHistory(int pn, int ps) throws IOException {
        //renderMockFileInJsonIfDev("search.history.json");
        User user = getUser();
        PageOffset po = new PageOffset(pn, ps);
        TMResult res = MainSearchHistory.querySearchHistory(user.getId(), po);
        renderJSON(JsonUtil.getJson(res));
    }

    /**
     * 8.00 http://to.taobao.com/FV9tzgy 24.00 http://to.taobao.com/nIbfzgy
     * 39.00 http://to.taobao.com/uRbfzgy 69.00 http://to.taobao.com/1Abfzgy
     * 
     * @param pn
     * @param start
     * @param end
     * @param sort
     * @param status
     */
    public static void commodityDiag(Integer pn, Integer start, Integer end, Integer sort, Integer status) {
        if (pn == null || pn < 1)
            pn = 1;
        if (start == null || start < 0)
            start = 0;
        if (end == null || end > 100)
            end = 100;
        if (sort == null || sort > 1 || sort < -1)
            sort = 1;
        if (status == null || !status.equals(0) || !status.equals(1) || !status.equals(2)) {
            status = 0;
        }

        render("Application/commodityDiag.html", pn, start, end, sort, status);
    }

    public static void topKey() {
        render("topKey/topkey.html");
    }

    public static void bustopkey() {
        // render("topBus/topbus.html");
        render("topBus/topbusnew.html");
    }

    public static void bustopkeyold() {
        render("topBus/topbus.html");
    }

    public static void bustopkeynew() {
        render("topBus/topbusnew.html");
    }

    public static void bussearchkey() {
        render("topBus/bussearch.html");
    }

    public static void dazhe() {
        render("dazhe/Dazhe.html");
    }

    public static void zhekou_1() {
        render("dazhe/zhekou_1.html");
    }

    // 购买版本通过参数选择页面，如果传入参数>5则是新的收费规则，小于5则是旧版本的收费规则,如果>10则使用第三套版本的收费规则
    public static void buyVersion() {
        int a = TMConfigs.version.VERSION_DEFAULT;
        if (a < 5) {
            render("Application/buyversion.html");
        } else if (a < 10 && a > 5) {
            render("Application/newbuyversion.html");
        } else if (a < 20 && a > 10) {
            render("Application/thirdbuyversion.html");
        } else if (a < 30 && a > 20) {
            render("Application/fourthbuyversion.html");
        } else if (a < 40 && a > 30) {
            render("Application/fivebuyversion.html");
        } else if (a < 50 && a > 40) {
            render("Application/sixbuyversion.html");
        } else if (a < 60 && a > 50) {
            render("Application/sevenversion.html");
        }

    }

    public static void buyzhizun() {
        int a = TMConfigs.version.VERSION_DEFAULT;

        if (a < 5) {
            render("Application/buyzhizun.html");
        } else if (a < 10 && a > 5) {
            render("Application/newbuyzhizun.html");
        } else if (a < 20 && a > 10) {
            render("Application/thirdbuyzhizun.html");
        } else if (a < 30 && a > 20) {
            render("Application/fourthbuyzhizun.html");
        } else if (a < 40 && a > 30) {
            render("Application/fivebuyzhizun.html");
        } else if (a < 50 && a > 40) {
            render("Application/sixbuyzhizun.html");
        } else if (a < 60 && a > 50) {
            render("Application/sevenzhizun.html");
        }

    }

    public static void multiModify() {
        render("Application/multiModify.html");
    }

    public static void autoMultimodify() {
        render("autoTitle/multiModify.html");
    }

    public static void correlativeRecom() {
        render("Application/correlativeRecom.html");
    }

    public static void introduce() {
        render("Nepal/new.html");
    }

    public static void auto_relation() {
        render("Application/tzg_relationOp.html");
    }

    public static void waterMarker() {
        render("Application/tzg_waterMarker.html");
    }

    static int WAIT_COUNT = 30;

    public static void firstSync() {
        Thread.currentThread().setName("WaitLock");

        int count = 0;
        Long userId = UserAction.getSessionUserId();
        if (userId == null) {
            log.error(" No User???????????????How does it come here???????");
        }

        do {
            log.info("[Start to wait lock]");
            boolean isLocked = UserLoginAction.isUserTagLocked(userId);
            log.error("Is Lock for user:" + userId + " with:" + isLocked);
            if (!isLocked) {
                log.info("[Wait Over, Let's Turn Over]");
                ok();
            }

            await(2000);
        } while (count++ < WAIT_COUNT);

        log.warn("Wait Time Over..............");
        renderJSON("{}");
    }

    public static void topshop() {
        render("topshop/topshop.html");
    }

    public static void kits() {
        render("autoTitle/kitIndex.html");
    }

    public static void other(String s, String app) {
        String secret = App.keySecrets.get(app);
        if (StringUtils.isEmpty(secret)) {
            // notFound("no app key :" + app);
            // TODO show go to order index.....
        }

        com.taobao.api.domain.User tbUser = new UserAPIs.UserGetApi(s, null, app, secret).call();
        if (tbUser == null) {
            // TODO show go to order index.....
        }

        Long userId = tbUser.getUserId();
        User user = UserDao.findById(userId);
        putUser(user);

        // TODO Go.....
    }

    public static void promote() {
        render("Popularize/autocomment.html");
    }

    public static void autocomment() {
        render("Popularize/autocomment.html");
    }

    public static void promotehelp() {
        render("promoteHelp/promotHelp.html");
    }

    public static void taoxuanciversion() {
        render("Application/taoxuanciversion.html");
    }

    public static void buyxuanci() {
        render("diag/buyxuanci.html");
    }

    public static void myWords() {
        render("keywords/mywords.html");
    }

    public static void seaWords() {
        render("keywords/seawords.html");
    }

    public static void freeUp() {
        render("freeUp.html");
    }

    public static void testWriteToDB() {
        User user = getUser();
        try {
            List<Item> itemsList = new ItemApi.ItemsOnsale(user, 0l, 0l).call();
            // writeToDB(user.getId(), System.currentTimeMillis(), itemsGet);
            if (!CommonUtils.isEmpty(itemsList)) {
                Set<Long> numIids = ItemDao.toIdsSet(itemsList);
                // ItemDao.deleteAll(userId, numIids);
                // List<ItemPlay> itemPlays =
                // ItemDao.findByNumIidListAndUserId(userId,StringUtils.join(numIids,","));
                Map<Long, ItemPlay> numIidMap = ItemDao.findMapByNumIids(user.getId(), numIids);
                log.info(">>>>>>>>>>>>>writeToDB map size in numIids ========== " + numIidMap.size());
                Set<Long> to_deleteItems = ItemDao.findModifiedItems(numIidMap, itemsList);
                ItemDao.deleteAll(user.getId(), to_deleteItems);
                List<Item> toInsertItems = ItemDao.findToInsertItems(numIidMap, itemsList);
                ItemDao.batchInsert(user.getId(), 1111l, toInsertItems);
                itemsList.clear();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void textFuwuScoreApi() {
        Date dateTime = new Date();
        try {
            dateTime = SimpleDateFormat.getDateTimeInstance().parse("2013-08-03 00:00:00");
        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        TaobaoClient client = TBApi.genClient();
        FuwuScoresGetRequest req = new FuwuScoresGetRequest();
        req.setCurrentPage(1L);
        req.setPageSize(40L);
        req.setDate(dateTime);
        try {
            FuwuScoresGetResponse response = client.execute(req,
                    "62009241420e8cb445230a561d751ad8ZZ8c269ef28fa481782852588");
            log.info("d");
        } catch (ApiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void fenxiao() {
        render("fenxiao/index.html");
    }

    /**
     * 行业销售属性分析
     */
    public static void catAnalysis() {
        render("autoTitle/catAnalysis.html");
    }

    public static void propAnalysis() {
        render("autoTitle/propAnalysis.html");
    }

    public static void addDev(long id) {
        RawId.addId(id);
    }

    public static void addThisDev() {
        User user = getUser();
        RawId.addId(user.getId());
    }

    public static void clearAllRawCache() {
        new UserDao.UserBatchOper(128) {
            @Override
            public void doForEachUser(User user) {
                this.sleepTime = 10L;
                log.info("[remove user:]" + user);
                RawId.removeId(user.getId());
            }
        }.call();
    }

    public static void removeDev(long id) {
        if (id <= 0L) {
            id = getUser().getId().longValue();
        }
        RawId.removeId(id);
    }

    public static void hasThisId() {
        User user = getUser();
        renderText(RawId.hasId(user.getId()));
    }

    public static void allDev() {
        List<RawId> ids = RawId.findAll();
        // StringBuilder sb = new StringBuilder();
        Set<String> set = new HashSet<String>();
        for (RawId rawId : ids) {
            // set.add(rawId.getId());
            User user = UserDao.findById(rawId.getId());
            if (user == null) {
                continue;
            }
            set.add(rawId.getId() + "\t" + ApiJdpAdapter.isUserJdpAvailable(user));
        }
        renderText(StringUtils.join(set, '\n'));
    }

    // public static void allDevInWork(){
    // List<RawId> ids = RawId.findAll();
    // }

    public static void getBaseIdFromCid(Long numIid) {
        if (numIid == null) {
            renderFailedJson("宝贝ID为空");
        }
        User user = getUser();
        if (user == null) {
            renderFailedJson("用户不存在");
        }
        ItemPlay itemPlay = ItemDao.findByNumIid(user.getId(), numIid);
        if (itemPlay == null) {
            renderFailedJson("找不到宝贝");
        }
        Long cid = itemPlay.getCid();
        if (cid == null) {
            renderFailedJson("cid为空");
        }
        List<TopURLBase> bases = TopURLBase.find("itemCidString like '%" + cid + "%'").fetch();
        if (CommonUtils.isEmpty(bases)) {
            new NoMatchTopURLBaseCid(cid).jdbcSave();
            renderFailedJson("找不到该宝贝对应的TopURLBase");
        } else {
            TopURLBase base = bases.get(0);
            Long baseId = base.getId();
            renderSuccessJson(baseId.toString());
        }
    }

    public static void getBaseIdByUser() {
        User user = getUser();
        if (user == null) {
            renderFailedJson("用户不存在");
        }
        Long cid = (Long) Cache.get(CidTopKeyPre + user.getId());
        Long baseId = -1L;
        if (cid == null) {
            cid = ItemDao.findMaxCid(user.getId());
            Cache.set(CidTopKeyPre + user.getId(), cid, "3d");
        }
        if (cid == null) {
            renderFailedJson("cid为空");
        }
        List<TopURLBase> bases = TopURLBase.find("itemCidString like '%" + cid + "%'").fetch();
        if (CommonUtils.isEmpty(bases)) {
            new NoMatchTopURLBaseCid(cid).jdbcSave();
            renderFailedJson("找不到该宝贝对应的TopURLBase");
        } else {
            TopURLBase base = bases.get(0);
            baseId = base.getId();
            renderSuccessJson(baseId.toString());
        }
    }

    public static String CidTopKeyPre = "CidTopKeyPre";

    public static void getCidTopKey() {

        User user = getUser();
        if (user == null) {
            renderText("用户不存在");
        }
        Long cid = (Long) Cache.get(CidTopKeyPre + user.getId());
        Long baseId = -1L;
        if (cid == null) {
            cid = ItemDao.findMaxCid(user.getId());
            Cache.set(CidTopKeyPre + user.getId(), cid, "3d");
        }
        List<TopKey> keys = new ArrayList<TopKey>();
        if (cid < 0) {
            log.info("找不多包含最多宝贝的cid");
        } else {
            List<TopURLBase> bases = TopURLBase.find("itemCidString like '%" + cid + "%'").fetch();
            if (CommonUtils.isEmpty(bases)) {
                new NoMatchTopURLBaseCid(cid).jdbcSave();
            } else {
                TopURLBase base = bases.get(0);
                baseId = base.getId();
                keys = TopKey
                        .fetch("topUrlBaseId = ? and  rankChange > 0 and LENGTH(text + 0) != LENGTH(text) order by rankChange desc limit ? offset ? ",
                                baseId, 10, 1);
            }
        }

        if (CommonUtils.isEmpty(keys)) {
            keys = TopKey.fetch(
                    "rankChange > 0 and LENGTH(text + 0) != LENGTH(text) order by rankChange desc limit ? offset ? ",
                    10, 1);
        }
        renderJSON(JsonUtil.getJson(new TMResult(true, baseId + "", keys)));
    }

    public static void getMaxCidByUser() {
        User user = getUser();
        if (user == null) {
            renderText("用户不存在");
        }
        Long cid = ItemDao.findMaxCid(user.getId());
        renderText(cid);
    }

    public static void jdpUsers() {
        Set<String> registeredNicks = new JuShiTaGetUsers().call();
        renderText(StringUtils.join(registeredNicks, '\n'));
    }

    public static void updateUserFirstLoginTime(Long firstLoginTime) {
        if (firstLoginTime == null || firstLoginTime <= 0) {
            renderFailedJson("请输入firstLoginTime");
        }
        User user = getUser();
        user.setFirstLoginTime(firstLoginTime);
        boolean isSuccess = user.jdbcSave();
        if (isSuccess) {
            renderSuccessJson("更新成功");
        } else {
            renderFailedJson("更新失败，请重试");
        }

    }

    public static void updateUserFirstLoginTimeByUserNick(Long firstLoginTime, String userNick) {
        if (StringUtils.isEmpty(userNick)) {
            renderFailedJson("用户名为空，请输入用户名");
        }
        if (firstLoginTime == null || firstLoginTime <= 0) {
            renderFailedJson("请输入firstLoginTime");
        }
        User user = UserDao.findByUserNick(userNick);
        if (user == null) {
            renderFailedJson("用户名为空，请输入正确的用户名");
        }

        user.setFirstLoginTime(firstLoginTime);
        boolean isSuccess = user.jdbcSave();
        if (isSuccess) {
            renderSuccessJson("更新成功");
        } else {
            renderFailedJson("更新失败，请重试");
        }

    }

    public static void getCatPayHourMap() {
        renderJSON(JsonUtil.getJson(ItemCatOrderPayTimeDisTributeJob.cidPayTimeMap.size()));
    }

}
