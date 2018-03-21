package controllers;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import jdp.ApiJdpAdapter;
import job.apiget.AsyncSimpleTradeJob;
import job.apiget.ItemUpdateJob;
import job.apiget.TradeRateDeleteSyncJob;
import job.apiget.TradeRateUpdateJob;
import job.apiget.TradeUpdateJob;
import job.item.UpdateItemPropSaleJob;
import models.UserDiag;
import models.fenxiao.Fenxiao;
import models.item.FenxiaoItem;
import models.item.ItemCatHotProps;
import models.item.ItemCatPlay;
import models.item.ItemPlay;
import models.item.ItemPropSale;
import models.item.NoPropsItemCat;
import models.mysql.fengxiao.HotWordCount;
import models.mysql.fengxiao.HotWordCount.WordCount;
import models.updatetimestamp.updates.ItemUpdateTs;
import models.updatetimestamp.updates.TradeRateUpdateTs;
import models.updatetimestamp.updates.TradeUpdateTs;
import models.updatetimestamp.updatestatus.ItemDailyUpdateTask;
import models.user.User;
import onlinefix.FixMonitorInstall.ReinstallUserJob;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
//import org.bouncycastle.asn1.cms.Time;
//import underup.frame.industry.Time;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.CacheFor;
import pojo.webpage.top.ItemStatusCount;
import result.TMPaginger;
import result.TMResult;
import spider.DSRSpider;
import transaction.JDBCBuilder;
import underup.frame.industry.CatTopSaleItemSQL;
import underup.frame.industry.CatTopSaleItemSQL.TopSaleItem;
import underup.frame.industry.HotShop;
import underup.frame.industry.HotShop.HotShopInfo;
import underup.frame.industry.ItemCatLevel1;
import underup.frame.industry.ItemCatLevel2;
import underup.frame.industry.ItemPropsArrange;
import underup.frame.industry.ItemPropsArrange.PInfo;
import underup.frame.industry.ItemsCatArrange;
import underup.frame.industry.ListTimeRange;
import underup.frame.industry.MonthInfo;
import underup.frame.industry.PriceDistribution;
import underup.frame.industry.PriceDistribution.PriceRange;
import underup.frame.industry.YearAndMonth;
import underup.frame.industry.YearInfo;
import utils.PlayUtil;
import utils.intervalTran;
import actions.DiagAction.BatchResultMsg;
import actions.SubcribeAction;
import actions.TemplateAction;
import actions.UserAction;
import actions.WordsAction;
import autotitle.ItemPropAction;
import bustbapi.FenxiaoApi.FengxiaoRecommender;
import bustbapi.ItemApi;
import bustbapi.ItemApi.ItemUpdate;
import bustbapi.ItemApi.ItemsInventoryCount;
import bustbapi.ItemApi.MultiItemsListGet;
import bustbapi.ItemCatApi;
import bustbapi.JDPApi;
import bustbapi.JDPApi.JuShiTaCancelApi;
import bustbapi.OperateItemApi.ItemsOnWindowInit;
import bustbapi.ShowWindowApi;
import bustbapi.TMTradeApi;
import cache.CountItemCatCache;
import cache.CountItemCatStatusCache;
import cache.CountSellerCatCache;
import cache.CountSellerCatStatusCache;
import cache.UserHasTradeItemCache;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.IWordBase;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.pojo.StringPair;
import com.ciaosir.client.utils.DateUtil;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.MapIterator;
import com.ciaosir.client.utils.NumberUtil;
import com.ciaosir.commons.ClientException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.ItemCat;
import com.taobao.api.domain.SellerCat;

import configs.TMConfigs;
import dao.UserDao;
import dao.item.ItemDao;
import dao.trade.OrderDisplayDao;
import dao.trade.TradeDisplayDao;

public class Items extends TMController {

    public static final Logger log = LoggerFactory.getLogger(Items.class);

    public static final String TAG = "Items";

    public static void index() {
        ok();
    }
    
    public static final List<String> VIP_USER_NICK = new ArrayList<String>();
    
    static {
    	VIP_USER_NICK.add("liu405607850");
    	VIP_USER_NICK.add("0o嗯嗯o0");
    	VIP_USER_NICK.add("东阿特产店");
    	VIP_USER_NICK.add("liuguoliang0305");
    	VIP_USER_NICK.add("q592026354");
    	VIP_USER_NICK.add("汉库克丶小衫");
    	VIP_USER_NICK.add("阿高睡衣行");
    	VIP_USER_NICK.add("七天无理由退换2014");
    	VIP_USER_NICK.add("天铁天天");
    	VIP_USER_NICK.add("曼森数码科技");
    	VIP_USER_NICK.add("江苏华泰金属制品直销店");
    	VIP_USER_NICK.add("zhuguang9011");
    	VIP_USER_NICK.add("深圳市龙腾服饰有限公司");
    	VIP_USER_NICK.add("zhangjie7554898");
    	VIP_USER_NICK.add("美之选数码城");
    	VIP_USER_NICK.add("中国风奇异梦");
    	VIP_USER_NICK.add("甜果飘香");
    	VIP_USER_NICK.add("zhonghaijun1983");
    	VIP_USER_NICK.add("韩都衣舍家居服");
    	VIP_USER_NICK.add("幸福一家笑");
    	VIP_USER_NICK.add("贝乐星商城2号");
    	VIP_USER_NICK.add("clorest510");
    }
    
    public static final List<Long> VIP_USER_ID= new ArrayList<Long>();
    
    static {
    	VIP_USER_ID.add(2469531684L);
    	VIP_USER_ID.add(499826673L);
    	VIP_USER_ID.add(1599904401L);
    	VIP_USER_ID.add(136593762L);
    	VIP_USER_ID.add(684777283L);
    	VIP_USER_ID.add(2570700643L);
    	VIP_USER_ID.add(2284770973L);
    	VIP_USER_ID.add(2286446466L);
    	VIP_USER_ID.add(10322294L);
    	VIP_USER_ID.add(2972837704L);
    	VIP_USER_ID.add(2780046749L);
    	VIP_USER_ID.add(1013540350L);
    	VIP_USER_ID.add(2888476359L);
    	VIP_USER_ID.add(168889575L);
    	VIP_USER_ID.add(1066473463L);
    	VIP_USER_ID.add(1105695531L);
    	VIP_USER_ID.add(2954543177L);
    	VIP_USER_ID.add(2296124895L);
    	VIP_USER_ID.add(1637637178L);
    	VIP_USER_ID.add(2394418789L);
    	VIP_USER_ID.add(2209509168L);
    	VIP_USER_ID.add(79742176L);
    }

    public static void sync(boolean install) {
        User user = getUser();
        log.info("[get user]" + getUser());
        boolean first = false;

        // if (ts != null) {
        // ts.setLastUpdateTime(System.currentTimeMillis() -
        // DateUtil.DAY_MILLIS);
        // ts.save();
        // }

        ItemDailyUpdateTask taskTs = ItemDailyUpdateTask.findByUserIdAndTs(user.getId(), DateUtil.formCurrDate());
        if (taskTs != null) {
            ItemDailyUpdateTask.deleteOne(taskTs);
        }

        /*
         * WindowConcigs wc = WindowsService.getConfig(user.getId()); Long[]
         * musts = wc.getMustIds(); for(Long numIid : musts){ new
         * ShowwindowMustDoItem(user.getId(),numIid).save(); } Long[] excludes =
         * wc.getExcludeIds(); for(Long numIid : excludes){ new
         * ShowwindowExcludeItem(user.getId(),numIid).jdbcSave(); }
         */

        ItemUpdateTs ts = ItemUpdateTs.fetchByUser(user);
        if (ts != null) {
            ts.jdbcDelete(user.getId());
        }
        // if (APIConfig.get().deleteAllItems()) {
        // ItemDao.deleteAll(user.getId());
        // }

        new ItemUpdateJob(user.getId(), first).doJob();

        if (APIConfig.get().enableSyncTrade(user.getId())) {
            // TODO try sync trades and comments...
            new TradeUpdateJob(user.getId(), System.currentTimeMillis()).doJob();
        }

        if (APIConfig.get().enableSyncTradeRate()) {
        	// TODO: sync trade rate..
            new TradeRateUpdateJob(user.getId(), true).doJob();

            // 检查是否有删除评价
            // TradeRateUpdateJob.doWithDeleteTradeRate(user);
            
            TradeRateDeleteSyncJob.doWithDeleteTradeRate(user, true);

            DSRSpider.spiderSellerGoodRate(user.getId());
        }

        if (install) {
            TemplateAction.doInstallItemMonitor(user);
        }
    }

    public static void invent() {
        // User user = getUser();
        // List<Item> call = new ItemsInventory(user, null, null).call();
        // for (Item item : call) {
        // log.info("[item]" + item.getNumIid());
        // if (item.getNumIid().longValue() == 16422361655L) {
        // log.error(" ids ;got....");
        // }
        // }
        // Item call2 = new ItemGet(user, 16422361655L).call();
        // log.info("[Call2]" + new Gson().toJson(call2));
    }

    public static void downloadWords(String numIid) {
        long userId = getUser().getId();
        String[] numIidL = numIid.split(",");
        intervalTran.createAllExcel(numIidL, userId);
        File excel = new File("word.xls");
        renderBinary(excel);
    }

    public static void getWords(String numIid) {
        long userId = getUser().getId();
        String[] numIidL = numIid.split(",");
        List<IWordBase> wordBase = WordsAction.buildRecommend(Long.parseLong(numIid), userId);
        renderJSON(JsonUtil.getJson(wordBase));
    }

    public static void base(long numIid) {
        if (numIid <= 0L) {
            notFound();
        }
        User user = getUser();
        ItemPlay item = ItemDao.findByNumIid(user.getId(), numIid);
        if (item == null) {
            notFound();
        }
        renderJSON(JsonUtil.getJson(item));
    }

    public static void listAll() {
        renderJSON(JsonUtil.getJson(ItemDao.findByUserId(getUser().getId())));
    }

    public static void list(String s, int pn, int ps) throws IOException {
        User user = getUser();
        List<ItemPlay> list = ItemDao.findAllByUser(user.getId(), (pn - 1) * ps, ps, s, 0);
        if (CommonUtils.isEmpty(list)) {
            TMPaginger.makeEmptyFail("亲， 您还没有上架宝贝哟！！！！！");
        }

        TMPaginger tm = new TMPaginger(pn, ps, (int) ItemDao.countAllByUser(user.getId(), s), list);
        renderJSON(JsonUtil.getJson(tm));

    }

    /**
     * 获取某个宝贝的属性列表
     * 
     * @param numIid
     */
    public static void prop(long numIid) {
        User user = getUser();
        List<StringPair> props = ItemApi.getProps(user, numIid);
        renderJSON(JsonUtil.getJson(props));
    }

    public static void getMonitorItems(Long[] numIidArr) {
        // String json = "[{\"numIid\":1,\"title\": \"test\"}]";
        // renderJSON(json);
        if (numIidArr == null || numIidArr.length == 0) {
            renderJSON("[]");
        }

        User user = TMController.getUser();
        Long userId = user.getId();
        String ids = "";
        for (int i = 0; i < numIidArr.length; i++) {
            if (i > 0)
                ids += ",";
            ids += numIidArr[i];
        }
        List<ItemPlay> itemList = ItemDao.findByIds(userId, ids);
        if (itemList == null) {
            renderJSON("[]");
        }
        renderJSON(JsonUtil.getJson(itemList));
    }

    public static void tradeNum() throws InterruptedException, ExecutionException {
        User user = getUser();
        Map<Long, Integer> itemsSaleCount = TMTradeApi.go(user);
        renderJSON(itemsSaleCount);
    }

    public static void testAllSub() {
        // List<User> users = User.findAll();
        List<User> users = UserDao.fetchAllUser();
        for (User user : users) {
            int ver = SubcribeAction.getSubscribeInfo(user);
            log.info("[ver:]" + ver + " for user:" + user);
        }
    }

    public static void dropAllOnShow() {
        User user = getUser();
        List<Item> tbItems = new ItemsOnWindowInit(user).call();
        List<Long> ids = new ArrayList<Long>();
        for (Item item : tbItems) {
            ids.add(item.getNumIid());
        }
        ShowWindowApi.DeleteRecomendAll(ids, user.getSessionKey());

    }

    public static void all() {
        User user = getUser();
        List<ItemPlay> findByUserId = ItemDao.findByUserId(user.getId());
        for (ItemPlay itemPlay : findByUserId) {
            log.info("[itemPlay:]" + new Date(itemPlay.getDeListTime()));
        }
    }

    public static void syncAllHasSaleItem() {
    }

    public static void reinstall() {
        new ReinstallUserJob(getUser()).now();
    }

    public static void dailyOrder() {
        User user = getUser();
        Map<String, String> map = OrderDisplayDao.queryOrderDay(user.getId());
        String json = new GsonBuilder().setPrettyPrinting().create().toJson(map);
        renderText(json);
    }

    public static void clear() {
        User user = getUser();
        ItemUpdateTs ts = ItemUpdateTs.fetchByUser(user.getId());
        if (ts != null) {
            ts.jdbcDelete(user.getId());
        }

        ItemDailyUpdateTask taskTs = ItemDailyUpdateTask.findByUserIdAndTs(user.getId(), DateUtil.formCurrDate());
        if (taskTs != null) {
            taskTs.delete();
        }

        ItemDao.deleteAll(user.getId());
        UserDiag diag = UserDiag.findByUserId(user.getId());
        if (diag != null) {
            // diag.delete();
            JDBCBuilder.update(false, "delete from user_diag where id = ?", diag.getId());
        }
        TradeDisplayDao.removeUser(user.getId());
        long removeOrderNum = OrderDisplayDao.removeUser(user.getId());
        log.error(" remove order num[" + removeOrderNum + "] for user:" + user);
    }

    public static void blank() {
        User user = getUser();
        List<Item> call = new ItemApi.ItemsOnsale(user, null, null).call();
        for (Item item : call) {
            Item temp = new ItemApi.ItemDescGet(user, item.getNumIid()).call();
            String newhtml = temp.getDesc().replaceAll("<p>&nbsp;</p>", StringUtils.EMPTY);
            newhtml = newhtml.replaceAll("<div>&nbsp;</div>", StringUtils.EMPTY);
            ItemUpdate api = new ItemApi.ItemUpdate(user.getSessionKey(), item.getNumIid(), newhtml);
            api.call();
        }
    }

    @JsonAutoDetect
    static class CatIdNameCount {
        @JsonProperty
        int id;

        @JsonProperty
        String name;

        @JsonProperty
        int count;

        public CatIdNameCount(int id, String name, int count) {
            super();
            this.id = id;
            this.name = name;
            this.count = count;
        }

        static List<CatIdNameCount> buildSellerCats(Map<SellerCat, Integer> map) {
            final List<CatIdNameCount> models = new ArrayList<CatIdNameCount>();
            new MapIterator<SellerCat, Integer>(map) {
                @Override
                public void execute(Entry<SellerCat, Integer> entry) {
                    SellerCat cat = entry.getKey();
                    models.add(new CatIdNameCount(cat.getCid().intValue(), cat.getName(), entry.getValue()));
                }
            }.call();
            return models;
        }

        static List<CatIdNameCount> buildItemCats(Map<ItemCatPlay, Integer> map) {
            final List<CatIdNameCount> models = new ArrayList<CatIdNameCount>();
            new MapIterator<ItemCatPlay, Integer>(map) {
                @Override
                public void execute(Entry<ItemCatPlay, Integer> entry) {
                    ItemCatPlay cat = entry.getKey();
                    models.add(new CatIdNameCount(cat.getCid().intValue(), cat.getName(), entry.getValue()));
                }
            }.call();
            return models;
        }
    }
    
    public static void sellerCatCount() {
        User user = getUser();
        Map<SellerCat, Integer> map = CountSellerCatCache.get().getByUser(user);
        renderJSON(JsonUtil.getJson(CatIdNameCount.buildSellerCats(map)));
    }
    
    public static void onSaleSellerCatCount() {
        User user = getUser();
        Map<SellerCat, Integer> map = CountSellerCatCache.get().getOnSaleByUser(user);
        renderJSON(JsonUtil.getJson(CatIdNameCount.buildSellerCats(map)));
    }

    public static void itemCatCount() {
        User user = getUser();
        Map<ItemCatPlay, Integer> map = CountItemCatCache.get().getByUser(user);
        renderJSON(JsonUtil.getJson(CatIdNameCount.buildItemCats(map)));
    }

    public static void onSaleItemCatCount() {
        User user = getUser();
        Map<ItemCatPlay, Integer> map = CountItemCatCache.get().getOnSaleByUser(user);
        renderJSON(JsonUtil.getJson(CatIdNameCount.buildItemCats(map)));
    }

	public static void sellerCatStatusCount() {
		User user = getUser();
		List<ItemStatusCount> result = new ArrayList<ItemStatusCount>();
		
		Map<SellerCat, ItemStatusCount> res = CountSellerCatStatusCache.get().getByUser(user);
		result.addAll(res.values());
		
		// 按照在售倒序排列
		Collections.sort(result, new Comparator<ItemStatusCount>() {
			@Override
			public int compare(ItemStatusCount c1, ItemStatusCount c2) {
				int a = -1;
				
				if(c1.getOnsaleCount() > c2.getOnsaleCount()) {
					a = -1;
				}
				if(c1.getOnsaleCount() == c2.getOnsaleCount()) {
					a = c1.getInstockCount() > c2.getInstockCount() ? -1 : 1;
				}
				if(c1.getOnsaleCount() < c2.getOnsaleCount()) {
					a = 1;
				}
				
				return a;
			}
		});
		
		renderJSON(JsonUtil.getJson(result));
	}

	public static void itemCatStatusCount() {
		User user = getUser();
		List<ItemStatusCount> result = new ArrayList<ItemStatusCount>();
		
		Map<ItemCatPlay, ItemStatusCount> res = CountItemCatStatusCache.get().getByUser(user);
		result.addAll(res.values());

		// 按照在售倒序排列
		Collections.sort(result, new Comparator<ItemStatusCount>() {
			@Override
			public int compare(ItemStatusCount c1, ItemStatusCount c2) {
				int a = -1;
				
				if(c1.getOnsaleCount() > c2.getOnsaleCount()) {
					a = -1;
				}
				if(c1.getOnsaleCount() == c2.getOnsaleCount()) {
					a = c1.getInstockCount() > c2.getInstockCount() ? -1 : 1;
				}
				if(c1.getOnsaleCount() < c2.getOnsaleCount()) {
					a = 1;
				}
				
				return a;
			}
		});
		
		renderJSON(JsonUtil.getJson(result));
	}

    // @CacheFor("24h")
    public static void catProps(long numIid) {
        if (numIid <= 0L) {
            renderJSON("[]");
        }

        Long cid = 0L;
        User user = getUser();
        ItemPlay item = ItemDao.findByNumIid(user.getId(), numIid);
        if (item == null) {
            Item call = ApiJdpAdapter.get(user).findItem(user, numIid);
            cid = call.getCid();
        } else {
            cid = item.getCid();
        }
        log.info("[target cid :]" + cid);

        String res = ItemCatHotProps.getCachedRecent(cid);
        if (StringUtils.isEmpty(res)) {
            renderJSON("[]");
        } else {
            renderJSON(res);
        }
    }

    public static void catHotProps(long cid) {
        if (cid <= 0L) {
            renderJSON("[]");
        }

        String res = ItemCatHotProps.getCachedRecent(cid);
        if (StringUtils.isEmpty(res)) {
            renderJSON("[]");
        } else {
            renderJSON(res);
        }
    }

    public static void catPropGroup(long cid) throws ClientException {
        if (cid <= 0L) {
            renderJSON("[]");
        }

        List<ItemPropSale> groupList = ItemPropSale.findCidPropGroup(cid);
        if (CommonUtils.isEmpty(groupList)) {
            // retry fetch data
            ItemCatPlay itemCat = ItemCatPlay.findByCid(cid);
            UpdateItemPropSaleJob.updateEachCat(itemCat);
        }

        groupList = ItemPropSale.findCidPropGroup(cid);
        if (CommonUtils.isEmpty(groupList)) {
            renderJSON("[]");
        }
        renderJSON(JsonUtil.getJson(groupList));
    }

    public static void catPropSale(long cid, long pid, int pn, int ps) {
        if (cid <= 0L) {
            renderJSON("[]");
        }

        if (ps < 10) {
            ps = 50;
        }
        PageOffset po = new PageOffset(pn, ps);
        List<ItemPropSale> propList = ItemPropSale.findCidTopProp(cid, pid, po);
        log.info("[prop list:]" + propList);
        if (CommonUtils.isEmpty(propList)) {
            renderJSON("[]");
        }
        renderJSON(JsonUtil.getJson(propList));
    }

    public static void spiderCat(long cid) throws ClientException {
        ItemCatPlay itemCat = ItemCatPlay.findByCid(cid);
        UpdateItemPropSaleJob.updateEachCat(itemCat);
    }

    public static void tc() {
        ItemPropSale.truncateTable();
    }

    public static void fenxiaoTitles(String numIids) {

        User user = getUser();

        log.warn("[is fengxiao :]" + user.isFengxiao());
        if (!user.isFengxiao()) {
            renderJSON(JsonUtil.getJson(new TMResult(ListUtils.EMPTY_LIST)));
        }

        Set<Long> ids = NumberUtil.splitLongSet(numIids);
        log.info("[ids :]" + ids);

        List<BatchResultMsg> msgs = new Vector<BatchResultMsg>();

        batchFengxiaoRecommend(user, ids, msgs);

        renderJSON(JsonUtil.getJson(new TMResult(msgs)));
    }

    private static void batchFengxiaoRecommend(final User user, Collection<Long> ids, final List<BatchResultMsg> msgs) {
        List<FutureTask<BatchResultMsg>> tasks = new ArrayList<FutureTask<BatchResultMsg>>();

        for (final Long id : ids) {
            tasks.add(TMConfigs.getBatchResultMsgPool().submit(new FengxiaoRecommender(user, id)));
        }

        for (FutureTask<BatchResultMsg> futureTask : tasks) {
            try {
                BatchResultMsg msg = futureTask.get();
                msgs.add(msg);
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }
    }

    private static String appendForItemId(User user, String nameSql, List<BatchResultMsg> msgs, Long id, Item item) {
        String title = StringUtils.EMPTY;
        String fetchSerialNum = ItemPropAction.fetchSerialNum(item);
        log.error(" curr  serial :" + fetchSerialNum);
        if (StringUtils.isEmpty(fetchSerialNum)) {
            msgs.add(new BatchResultMsg(false, "亲,您的宝贝没有货号或者是型号属性哟,这样拿不到官方标题的哟", id));
            return title;
        }

        title = FenxiaoItem.tryFindItem(nameSql, fetchSerialNum);
        if (title == null) {
            log.error(" no numiid for :" + id + " for user:" + user);
            msgs.add(new BatchResultMsg(false, "亲,木有找到供货商的标题55555", id));
        } else {
            msgs.add(new BatchResultMsg(id, title, StringUtils.EMPTY, null));
        }

        return title;
    }

    public static void fenxiaoTitle(Long numIid) {
        User user = getUser();
        // Fenxiao fetch = Fenxiao.find(" userId = ? ", user.getId()).first();
        Fenxiao fetch = Fenxiao.findByUserId(user.getId());
        if (fetch == null || StringUtils.length(fetch.getGonghuo()) < 3) {
            renderJSON(JsonUtil.getJson(new TMResult("亲,请您先在左侧添加供货商哟")));
        }
        log.error("[fetch size :]" + fetch);
        // Item item = new ItemGet(user, numIid, true).call();
        Item item = ApiJdpAdapter.tryFetchSingleItem(user, numIid);

        String fetchSerialNum = ItemPropAction.fetchSerialNum(item);
        if (StringUtils.isEmpty(fetchSerialNum)) {
            renderJSON(JsonUtil.getJson(new TMResult("亲,您的宝贝没有货号或者是型号属性哟,这样拿不到官方标题的哟")));
        }

        String gonghuos = fetch.getGonghuo();
        String[] splits = gonghuos.split(",");
        List<String> names = new ArrayList<String>(splits.length);
        for (String string : splits) {
            names.add(string);
        }
        int length = names.size();
        log.error("[names : ]" + names);
        for (int i = 0; i < length; i++) {
            names.set(i, "'" + CommonUtils.escapeSQL(names.get(i)) + "'");
        }

        String title = FenxiaoItem.tryFindItem(StringUtils.join(names, ','), fetchSerialNum);
        if (title == null) {
            log.error(" no numiid for :" + numIid + " for user:" + user);
            renderJSON(JsonUtil.getJson(new TMResult("亲,木有找到供货商的标题55555")));
        }

        renderJSON(JsonUtil.getJson(new TMResult(true, null, title)));
    }

    public static void getCache() {
        User user = getUser();
        List<ItemPlay> list = UserHasTradeItemCache.getByUser(user, 100);
        Gson gson = PlayUtil.genPrettyGson();
        renderText(gson.toJson(list));
    }

    public static void addDebug() {
        User user = getUser();
        UserAction.debugUserId.add(user.getId());
    }

    public static void removeDebug() {
        User user = getUser();
        UserAction.debugUserId.remove(user.getId());
    }

    public static void syncTrades() {
        User user = getUser();
        if (APIConfig.get().enableSyncTrade(user.getId())) {
            // TradeUpdateTs updateTs = TradeUpdateTs.findById(user.getId());
            // if (updateTs == null && Play.mode.isDev()) {
            // updateTs = new TradeUpdateTs(user.getId(),
            // DateUtil.formCurrDate() - DateUtil.TRIPPLE_DAY_MILLIS_SPAN);
            // updateTs.save();
            // }
            new TradeUpdateJob(user.getId(), System.currentTimeMillis()).doJob();
        }
    }
    
    public static void forceSyncTradeRates() {
        if (!APIConfig.get().enableSyncTradeRate()) {
            return;
        }
        // if (true) {
        User user = getUser();
        
        if (APIConfig.get().enableSyncTrade(user.getId())) {
            new TradeUpdateJob(user.getId(), System.currentTimeMillis()).doJob();
        }
        
        TradeRateUpdateTs.jdbcDelete(user.getId());
        
        new TradeRateUpdateJob(user.getId(), true).doJob();

        // 检查是否有删除评价
        // TradeRateUpdateJob.doWithDeleteTradeRate(user);
        TradeRateDeleteSyncJob.doWithDeleteTradeRate(user, true);

        DSRSpider.spiderSellerGoodRate(user.getId());
        
        renderSuccessJson();
    }
    
	// 测试用
	public static void testTradeRates() {
		if (!APIConfig.get().enableSyncTradeRate()) {
			return;
		}
		User user = getUser();

		TradeRateUpdateTs.jdbcDelete(user.getId());
		
		new TradeRateUpdateJob(user.getId(), true).doJob();

		// 检查是否有删除评价
		TradeRateDeleteSyncJob.doWithDeleteTradeRate(user, true);

		DSRSpider.spiderSellerGoodRate(user.getId());
	}

    public static void syncTradeRates() {
        if (!APIConfig.get().enableSyncTradeRate()) {
            renderError("不允许同步评价！");
        }
        User user = getUser();

//        Long nowTs = System.currentTimeMillis();
//        TradeRateUpdateTs tradeRateTs = TradeRateUpdateTs.findByUserId(user.getId());
//        Long updateTs = tradeRateTs == null ? 0L : tradeRateTs.getLastUpdateTime();
//        if (nowTs - updateTs < DateUtil.ONE_MINUTE_MILLIS * 10) {
//            renderError("同步太频繁啦！请10分钟后再来试试吧~");
//        }
        
        new TradeRateUpdateJob(user.getId(), true).doJob();

        // 检查是否有删除评价
        // TradeRateUpdateJob.doWithDeleteTradeRate(user);
        TradeRateDeleteSyncJob.doWithDeleteTradeRate(user, true);
        
        DSRSpider.spiderSellerGoodRate(user.getId());

        renderSuccessJson();
    }
    
    public static void syncSellerDsr() {
        User user = getUser();
        DSRSpider.spiderSellerGoodRate(user.getId());
    }

    public static void tradeUpdated() {
        User user = getUser();
        // TradeUpdateTs updateTs = TradeUpdateTs.findById(user.getId());
        TradeUpdateTs updateTs = TradeUpdateTs.findByUser(user.getId());
        // if (updateTs == null && Play.mode.isDev()) {
        if (updateTs == null) {
            if (APIConfig.get().enableSyncTrade(user.getId())) {
                new TradeUpdateJob(user.getId(), System.currentTimeMillis()).doJob();
                renderText("亲，好评助手正在为您进行订单同步，这大约需要1小时左右，请耐心等待");
            } else {
                renderText("亲，不需要同步订单，谢谢");
            }
        } else {
            renderText("订单已同步");
        }
    }

    public static void reSync() {

        User user = getUser();
        final Long userId = user.getId();
        ItemUpdateTs ts = ItemUpdateTs.fetchByUser(user.getId());

        log.info("[find ts : ]" + ts);
        if (ts != null) {
            ts.jdbcDelete(user.getId());
        }
        new ItemUpdateJob(userId, false).doJob();
    }

    public static void setEarlyLogin() {
        User user = getUser();
        user.setFirstLoginTime(1354546851308L);
        user.jdbcSave();
    }

    @CacheFor("3h")
    public static void findLevel1(long year, long month) {//TODO
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

    public static void addFilterCat2(Long cat2Id) {
        if (cat2Id == null) {
            renderText("传入的类目id为空");
        }
        ItemCatPlay itemCatPlay = ItemCatPlay.findByCid(cat2Id);
        if (itemCatPlay == null) {
            renderText("找不到对应的类目");
        }
        new NoPropsItemCat(cat2Id, itemCatPlay.getName()).jdbcSave();
        renderText("过滤二级类目成功");
    }

    /**
     */
    public static void asyncTrade() {
        User user = getUser();
        TradeUpdateTs.jdbcDelete(user.getId());

        new AsyncSimpleTradeJob(user.getId(), false).doJob();
    }

    public static void inventoryCount() {
        User user = getUser();
        Long itemInventory = new ItemsInventoryCount(user, null, null).call();
        List<Item> items = new ItemApi.ItemsInventoryPage(user, null, null, 1L, 200L).call();
        renderJSON(items);
    }

    public static void itemApi(long numIid) {

        User user = getUser();
        Item item = new ItemApi.ItemGet(user, numIid).call();
        renderJSON(item);
    }

    public static void noItemApi(long numIid) {
        Item item = new ItemApi.ItemGet(StringUtils.EMPTY, numIid).call();
        renderJSON(item);
    }

    public static void minusItemTs() {
        User user = getUser();
        ItemUpdateTs ts = ItemUpdateTs.fetchByUser(user);
        if (ts == null) {
            renderText(" no ts for user:" + user);
        }
        long newTs = ts.getLastUpdateTime() - DateUtil.DAY_MILLIS;
        ts.updateLastItemModifedTime(user.getId(), newTs, true);
        renderText(" new ts :" + ts);
    }

    public static void findCid(long cid) {
        List<ItemCatPlay> cats = ItemCatPlay.findCidPath(cid);
        List<String> names = new ArrayList<String>(cats.size());
        for (ItemCatPlay itemCatPlay : cats) {
            names.add(itemCatPlay.getName());
        }
        renderText(StringUtils.join(names, "-->"));
    }

    public static void testParentGetApi(Long parent) {
        User user = getUser();
        List<ItemCat> tbCats = new ItemCatApi.ItemcatsGet(user, parent).call();
        System.out.println(new Gson().toJson(tbCats));
    }

    public static void singleItem(String nick, long numiid) {

        User user = StringUtils.isEmpty(nick) ? getUser() : UserDao.findByUserNick(nick);

        if (user == null) {
            badRequest();
        }

        ItemPlay item = ItemDao.findByNumIid(user.getId(), numiid);
        renderJSON(item);
    }

    // uttp
    // 热门属性 直接从数据库item_props里面取
    @CacheFor("3h")
    public static void catHotProps1(long cid, long year, long month) throws IOException {
        List<PInfo> pInfos = new ArrayList<PInfo>();
        pInfos = new ItemPropsArrange(cid, year, month).getPInfos();
        renderJSON(pInfos);
    }

    // 得到top100
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

    // 得到热门爆款词
    public static void hotWords(long cid, long year, long month, int pn, int ps) throws IOException {
        List<WordCount> hotWordCounts = HotWordCount.getTopWord(cid, year, month, (pn - 1) * ps, ps);
        int size = HotWordCount.getTopWordSize(cid, year, month);
        PageOffset po = new PageOffset(pn, ps);
        renderJSON(JsonUtil.getJson(new TMResult<List<WordCount>>(hotWordCounts, size, po)));
    }

    // 宝贝价格分布
    public static void priceRange(long cid, long year, long month) throws IOException {
        PriceDistribution priceDistribution = new PriceDistribution(cid, year, month);
        priceDistribution.exec();
        Map<String, PriceRange> priceRangeMap = priceDistribution.getPriceRange();
        List<PriceRange> priceRange = new ArrayList(priceRangeMap.values());
        renderJSON(priceRange);
    }

    // 上下架时间分布
    public static void delistTimeRange(long cid, long year, long month) throws IOException {
        ListTimeRange timeRange = new ListTimeRange(cid, year, month);
        timeRange.exec();
        int[] hourRange = timeRange.getHourDelistTime();
        renderBusJson(hourRange);
    }

    // 热销店铺
    public static void hotShop(long cid, long year, long month, int pn, int ps) throws IOException, ClientException {
        HotShop hotShop = new HotShop(cid, year, month);
        hotShop.execute();
        List<HotShopInfo> hotShopInfos = hotShop.getShopInfo((pn - 1) * ps, ps);
        int size = hotShop.getHotShops().size();
        PageOffset po = new PageOffset(pn, ps);
        renderJSON(JsonUtil.getJson(new TMResult<List<HotShopInfo>>(hotShopInfos, size, po)));
    }

    // 获取年份
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
    public static void singleItemApi(long id) {
        User user = getUser();
        Item item = new ItemApi.ItemFullGet(user, id).call();
        renderJSON(PlayUtil.genPrettyGson().toJson(item));
    }

    public static void sinlgeItemAll() {
        User user = getUser();
        List<ItemPlay> items = ItemDao.findByUserId(user.getId());
        List<Long> numIids = new ArrayList<Long>();
        for (ItemPlay itemPlay : items) {
            numIids.add(itemPlay.getNumIid());
            Item singleItem = ApiJdpAdapter.singleItem(user, itemPlay.getNumIid());
            System.out.println(singleItem);
        }
        List<Item> tbItems = new MultiItemsListGet(user.getSessionKey(), numIids).call();
        System.out.println(new Gson().toJson(tbItems));

    }
    
    public static void getLevelOneCid(long levelTwoCid, long year, long month){
        long levelOneCid = ItemCatLevel2.getLevelOneCid(levelTwoCid, year, month);
        renderJSON(levelOneCid);
    }
    
	public static void cancelRDS() {
		String result = StringUtils.EMPTY;
		for (String nick : JDPApi.LIMIT_USER_NICK) {
			User user = UserDao.findByUserNick(nick);
			if(user == null) {
				continue;
			}
			Boolean success = new JuShiTaCancelApi(user).call();
			if(success) {
				result += nick + ",";
			}
		}
		renderSuccess("操作成功", result);
	}
}
