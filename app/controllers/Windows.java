
package controllers;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import jdp.ApiJdpAdapter;
import jdp.JdpModel.JdpItemModel;
import job.showwindow.CheckNoDownShelfJob;
import job.showwindow.ShowWindowExecutor;
import job.showwindow.ShowWindowInitJob.ShowCaseInfo;
import job.showwindow.ShowWindowInitJob.WindowItemInfo;
import job.showwindow.ShowWindowTimerExecJob.ShowWindowUserBatcher;
import job.showwindow.WaitToAddShowWindowMustJob;
import job.showwindow.WindowValidationJob;
import models.item.ItemPlay;
import models.oplog.OpLog;
import models.oplog.OpLog.LogType;
import models.showwindow.DoubleTwelveOpenUser;
import models.showwindow.DropWindowTodayCache;
import models.showwindow.OnWindowItemCache;
import models.showwindow.ShowWindowConfig;
import models.showwindow.ShowwindowExcludeItem;
import models.showwindow.ShowwindowMustDoItem;
import models.showwindow.ShowwindowTmallTotalNumFixedNum;
import models.showwindow.WindowMoreRecommend;
import models.user.User;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import result.IItemBase.ItemBaseBean;
import result.TMResult;
import utils.TaobaoUtil;
import bustbapi.ErrorHandler;
import bustbapi.ItemApi;
import bustbapi.ItemApi.ItemsInventoryCount;
import bustbapi.ItemApi.ItemsInventoryPage;
import bustbapi.OperateItemApi;
import bustbapi.OperateItemApi.ItemsOnWindowInit;
import bustbapi.ShowWindowApi;
import bustbapi.ShowWindowApi.DeleteRecommend;
import bustbapi.ShowWindowApi.GetShopShowcase;
import bustbapi.UserAPIs;
import bustbapi.UserAPIs.UserGetApi;
import cache.UserHasTradeItemCache;
import cache.UserLoginInfoCache;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.DateUtil;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.NumberUtil;
import com.google.gson.Gson;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.Trade;

import configs.TMConfigs;
import dao.UserDao;
import dao.UserDao.UserBatchOper;
import dao.item.ItemDao;

public class Windows extends TMController {

    private static final Logger log = LoggerFactory.getLogger(Windows.class);

    public static final String TAG = "Windows";

    //public static final String DOUBLE_TWELVE = "double_twelve";

    public static void base() {
        User user = getUser();
//        ShowCaseInfo showCase = ShowCaseInfo.build(user);
        ShowCaseInfo showCase = new GetShopShowcase(user.getSessionKey()).call();
        WindowItemInfo info = null;

        if (showCase == null) {
            info = new WindowItemInfo(user);
        } else {
            // 数据修正,避免出现剩余橱窗位置为负的情况
            if(showCase.remainWindowCount < 0) {
            	showCase.setRemainWindowCount(0);
            	showCase.setTotalWindowCount(showCase.onShowItemCount);
            }
            info = new WindowItemInfo(user, showCase);
        }
        info.setMustCount(ShowwindowMustDoItem.countByUserId(user.getId()));
        info.setExcludeCount(ShowwindowExcludeItem.countByUserId(user.getId()));
        renderJSON(JsonUtil.getJson(info));
    }

    public static void addMustItem(long numIid) {
        final User user = getUser();
        ShowwindowMustDoItem.add(user, numIid);
        ShowWindowExecutor.delayExec(user);
        TMResult.renderMsg(StringUtils.EMPTY);
    }

    public static void addMustItems(String numIids) {
        final User user = getUser();
        if (StringUtils.isEmpty(numIids)) {
            ok();
        }
        String[] idStrings = StringUtils.split(numIids, ",");
        if (ArrayUtils.isEmpty(idStrings)) {
            ok();
        }
        for (String string : idStrings) {
            ShowwindowMustDoItem.add(user, NumberUtil.parserLong(string, 0L));
        }

        ShowWindowExecutor.delayExec(user);

        TMResult.renderMsg(StringUtils.EMPTY);
    }
    
    public static void addMustItemsWithFilter(String numIids) {
        final User user = getUser();
        
        Set<Long> numIidSet = UmpPromotion.parseIdsToSet(numIids);
        
        Set<Long> excludeNumIidSet = ShowwindowExcludeItem.findIdsByUser(user.getId(), numIidSet);
        
        for (Long numIid : numIidSet) {
            //加入了排除宝贝的，不能加入
            if (excludeNumIidSet.contains(numIid)) {
                continue;
            }
            ShowwindowMustDoItem.add(user, numIid);
        }

        ShowWindowExecutor.delayExec(user);

        renderTMSuccess("");
        
    }
    

    public static void removeMustItem(long numIid) {
        User user = getUser();
        ShowwindowMustDoItem.remove(user.getId(), numIid);
        ShowWindowExecutor.delayExec(user);
        TMResult.renderMsg(StringUtils.EMPTY);
    }
    
    public static void removeMustItems(String numIids) {
        User user = getUser();
        Set<Long> numIidSet = UmpPromotion.parseIdsToSet(numIids);
        
        for (Long numIid : numIidSet) {
            ShowwindowMustDoItem.remove(user.getId(), numIid);
        }
        ShowWindowExecutor.delayExec(user);
        renderTMSuccess("");
    }

    public static void removeAllMustItem() {
        User user = getUser();
        boolean isSuccess = ShowwindowMustDoItem.removeAll(user);
        ShowWindowExecutor.delayExec(user);
        renderJSON(JsonUtil.getJson(new TMResult(isSuccess)));
    }

    public static void addBaoyou() {
        User user = getUser();
        List<Long> baoyousList = ItemDao.findBaoyouIdsByUser(user.getId());
        for (Long id : baoyousList) {
            ShowwindowExcludeItem.add(user, id);
        }
        TMResult.renderMsg(StringUtils.EMPTY);
    }

    public static void addExcludeItems(String numIids) {
        User user = getUser();
        if (StringUtils.isEmpty(numIids)) {
            ok();
        }
        String[] idStrings = StringUtils.split(numIids, ",");
        if (ArrayUtils.isEmpty(idStrings)) {
            ok();
        }
        for (String string : idStrings) {
            ShowwindowExcludeItem.add(user, NumberUtil.parserLong(string, 0L));
        }
        ShowWindowExecutor.delayExec(user);

        TMResult.renderMsg(StringUtils.EMPTY);
    }
    
    public static void addExcludeItemsWithFilter(String numIids) {
        final User user = getUser();
        
        Set<Long> numIidSet = UmpPromotion.parseIdsToSet(numIids);
        
        Set<Long> mustNumIidSet = ShowwindowMustDoItem.findIdsByUser(user.getId(), numIidSet);
        
        for (Long numIid : numIidSet) {
            //加入了排除宝贝的，不能加入
            if (mustNumIidSet.contains(numIid)) {
                continue;
            }
            ShowwindowExcludeItem.add(user, numIid);
        }

        ShowWindowExecutor.delayExec(user);

        renderTMSuccess("");
        
    }

    public static void addExcludeItem(long numIid) {
        User user = getUser();
        ShowwindowExcludeItem.add(user, numIid);
        ShowWindowExecutor.delayExec(user);
        TMResult.renderMsg(StringUtils.EMPTY);
    }

    public static void addExcludeItemFromShow(long numIid) {
        User user = getUser();

        new ShowWindowApi.DeleteRecommend(user, numIid).call();
        ShowwindowExcludeItem.add(user, numIid);
        ShowWindowExecutor.delayExec(user);
        TMResult.renderMsg(StringUtils.EMPTY);
    }

    public static void removeExcludeItem(long numIid) {
        User user = getUser();
        ShowwindowExcludeItem.remove(user, numIid);
        ShowWindowExecutor.delayExec(user);
        TMResult.renderMsg(StringUtils.EMPTY);
    }
    
    public static void removeExcludeItems(String numIids) {
        User user = getUser();
        Set<Long> numIidSet = UmpPromotion.parseIdsToSet(numIids);
        
        for (Long numIid : numIidSet) {
            ShowwindowExcludeItem.remove(user, numIid);
        }
        ShowWindowExecutor.delayExec(user);
        renderTMSuccess("");
    }

    public static void removeAllExcludeItem() {
        User user = getUser();
        boolean isSuccess = ShowwindowExcludeItem.removeAll(user);
        ShowWindowExecutor.delayExec(user);
        renderJSON(JsonUtil.getJson(new TMResult(isSuccess)));
    }

    public static void listOnItems() {
        User user = getUser();
        List<Item> tbItems = new ItemsOnWindowInit(user).call();
        List<ItemBaseBean> buildFromTBItem = ItemBaseBean.buildFromTBItem(user, tbItems, true);
        if (CommonUtils.isEmpty(buildFromTBItem)) {
            renderJSON(JsonUtil.getJson(new ArrayList<ItemBaseBean>()));
        }
        TaobaoUtil.setOnWindowItemReason(user, buildFromTBItem);
        Collections.sort(buildFromTBItem);
        log.info("[find back item base:]" + buildFromTBItem.size());
        renderJSON(JsonUtil.getJson(buildFromTBItem));
    }

    public static void listMustItems(int pn, int ps) {
        User user = getUser();
        TMResult res = ShowwindowMustDoItem.findByUser(user.getId(), pn, ps, true);
        renderJSON(JsonUtil.getJson(res));
    }

    public static void chooseMustItems(int pn, int ps) {
        User user = getUser();
        Set<Long> ids = ShowwindowMustDoItem.findIdsByUser(user.getId());

        PageOffset po = new PageOffset(pn, ps);
        TMResult tmRes = ItemDao.findByUserWithExcluded(user.getId(), po, ids);
        renderJSON(JsonUtil.getJson(tmRes));
    }

    public static void chooseExcludedItems(int pn, int ps) {
        User user = getUser();
        Set<Long> ids = ShowwindowExcludeItem.findIdsByUser(user.getId());

        PageOffset po = new PageOffset(pn, ps);
        TMResult tmRes = ItemDao.findByUserWithExcluded(user.getId(), po, ids);
        renderJSON(JsonUtil.getJson(tmRes));
    }

    public static void chooseItems(int pn, int ps, String s, String cid, String sellerCid) {
        User user = getUser();
        Set<Long> ids1 = ShowwindowExcludeItem.findIdsByUser(user.getId());
        Set<Long> ids2 = ShowwindowMustDoItem.findIdsByUser(user.getId());
        if (!ids1.isEmpty())
            ids1.addAll(ids2);
        else
            ids1 = ids2;

        PageOffset po = new PageOffset(pn, ps);
        TMResult tmRes = ItemDao.findByUserAndSearchWithExcludedInAllcids(user.getId(), s, po, ids1, cid, sellerCid);
        renderJSON(JsonUtil.getJson(tmRes));
    }

    public static void chooseBaoyouItems(int pn, int ps, String s) {
        User user = getUser();
        Set<Long> ids1 = ShowwindowExcludeItem.findIdsByUser(user.getId());
        Set<Long> ids2 = ShowwindowMustDoItem.findIdsByUser(user.getId());
        if (!ids1.isEmpty())
            ids1.addAll(ids2);
        else
            ids1 = ids2;

        PageOffset po = new PageOffset(pn, ps);
        TMResult tmRes = ItemDao.findBaoyouByUser(user.getId(), s, po, ids1);
        renderJSON(JsonUtil.getJson(tmRes));
    }

    public static void listExcludeItems(int pn, int ps) {
        User user = getUser();
        TMResult res = ShowwindowExcludeItem.findByUser(user, pn, ps, true);
        renderJSON(JsonUtil.getJson(res));
    }

    public static void listOpLogs(int pn, int ps) {
        User user = getUser();
        PageOffset po = new PageOffset(pn, ps);
        TMResult res = OpLog.findByTypeAndUser(true, user, LogType.ShowWindow, po);
        renderJSON(JsonUtil.getJson(res));
    }

    public static void isOn() {
        User user = getUser();
//        UserShowWindowConfigs config = UserShowWindowConfigs.findOrCreateByUser(user);
        TMResult res = new TMResult(user.isShowWindowOn());
        renderJSON(JsonUtil.getJson(res));
    }

    public static void turn(boolean isOn, boolean isSalesCountFirst) {
        User user = getUser();

        System.out.println("看一下参数");
        System.out.println(isSalesCountFirst);
        System.out.println(isOn);


        checkIsZhizun();
        UserLoginInfoCache.get().doClearUser(user);

        user.setShowWindowOn(isOn);
        user.setSalesCountOn(isSalesCountFirst);
        boolean isSuccess = user.jdbcSave();

        if (isOn) {
//            List<Long> userIds = (List<Long>) Cache.get("AutoWindowOnUserIds");
//            if (!CommonUtils.isEmpty(userIds) && !userIds.contains(user.getId())) {
//                userIds.add(user.getId());
//                Cache.safeSet("AutoWindowOnUserIds", userIds, "300d");
//            }
            new ShowWindowExecutor(user).doJob();
            //new LightWeightRecommend(user).doJob();
//            TaobaoUtil.permitByUser(user);
            TaobaoUtil.permitTMCUser(user);
            int count = DropWindowTodayCache.addCacheForUser(user, 99);
            OnWindowItemCache.get().refresh(user);

        } else {

//            List<Long> userIds = (List<Long>) Cache.get("AutoWindowOnUserIds");
//            if (!CommonUtils.isEmpty(userIds) && userIds.contains(user.getId())) {
//                userIds.remove(user.getId());
//                Cache.safeSet("AutoWindowOnUserIds", userIds, "300d");
//            }

        }

        renderJSON(JsonUtil.getJson(new TMResult(isSuccess)));

    }

    public static void gogogo(Long userId) {
        User user = getUser();
        new ShowWindowExecutor(user).doJob();
    }

    public static void week() {
        renderText(DateUtil.getDayOfWeek(System.currentTimeMillis()));
    }

    public static void setWindowCache(boolean on) {
        TMConfigs.ShowWindowParams.enableItemTradeCache = on;
    }

    public static void ensure() {
        new ShowWindowSaleCacheEnsure().call();
    }

    public static class ShowWindowSaleCacheEnsure extends ShowWindowUserBatcher {

        @Override
        public void doForEachUser(final User user) {
            TMConfigs.getShowwindowPool().submit(new Callable<ItemPlay>() {

                @Override
                public ItemPlay call() throws Exception {
//                    log.info("[sync has sale item;]" + user);
                    UserGetApi api = new UserAPIs.UserGetApi(user.getSessionKey(), null);
                    com.taobao.api.domain.User tbUser = api.call();
                    if (tbUser == null) {
                        ErrorHandler.fuckWithTheErrorCode(user.getId(), user.getSessionKey(), api.getSubErrorCode());
                        return null;
                    }
                    UserHasTradeItemCache.clear(user);
                    //UserHasTradeItemCache.getByUser(user, ShowWindowExecutor.MUST_RECOMMEND_BY_TRADE_ORDER_NUM);
                    int prior_num = ShowWindowConfig.findOrCreate(user.getId()).checkPrioSaleNum();
                    UserHasTradeItemCache.getByUser(user, prior_num);
                    return null;
                }

            });

            CommonUtils.sleepQuietly(1000L);
        }

    };

    public static class ShowWindowMustRefresh extends ShowWindowUserBatcher {

        @Override
        public void doForEachUser(User user) {
            int prior_num = ShowWindowConfig.findOrCreate(user.getId()).checkPrioSaleNum();
            UserHasTradeItemCache.getByUser(user, prior_num, true);
            //UserHasTradeItemCache.getByUser(user, ShowWindowExecutor.MUST_RECOMMEND_BY_TRADE_ORDER_NUM, true);
            CommonUtils.sleepQuietly(1000L);
        }
    }

    public static void down() {
        new CheckNoDownShelfJob(getUser()).call();
    }

    public static void light() {
//        new LightWeightRecommend(getUser()).doJob();
        User user = getUser();
        new ShowWindowExecutor(user, true).call();
    }

    public static void clear() {
        UserHasTradeItemCache.clear(getUser());
    }

    public static void immediateRecommend() {
        User user = getUser();
        checkIsZhizun();
        //Cache.set(DOUBLE_TWELVE + user.getId(), false);
        new ShowWindowExecutor(user).doJob();
        /*Boolean isDoubleTwelveDeShowFail = (Boolean) Cache.get(DOUBLE_TWELVE + user.getId());
        if(isDoubleTwelveDeShowFail) {
        	renderText("双十二活动商品暂时不能取消橱窗推荐！");
        }*/
    }

    public static void toRecommend() {
        User user = getUser();
        WaitToAddShowWindowMustJob.addUser(user.getId());
    }

    public static void allCancel() {
        User user = getUser();
        List<Item> tbItems = new ItemsOnWindowInit(user).call();
        if (CommonUtils.isEmpty(tbItems)) {
            return;
        }
        for (Item item : tbItems) {
            try {
                DeleteRecommend api = new ShowWindowApi.DeleteRecommend(user, item.getNumIid());
                api.call();
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }
    }

    public static void submitNewConfig(ShowWindowConfig config) {
//        MixHelpers.infoAll(request, response);
        log.info("[conf:]" + config);
        User user = getUser();

        if (config == null) {
            // TODO????
            renderJSON(JsonUtil.getJson(TMResult.failMsg("亲，输入有误,您可以联系客服..")));
        }

        config.setUserId(user.getId());
        config.jdbcSave();
        UserHasTradeItemCache.clear(user);
        log.info("[salved config:]" + config);
        WaitToAddShowWindowMustJob.addUser(user.getId());
        renderJSON(JsonUtil.getJson(TMResult.OK));

    }
    
    public static void getManualWindowNum() {
    	final User user = getUser();
    	if(user == null) {
    		renderFailedJson("用户不存在");
    	}
    	ShowwindowTmallTotalNumFixedNum model = ShowwindowTmallTotalNumFixedNum.findById(user.getId());
        if (model != null) {
        	renderJSON(JsonUtil.getJson(model));
        }
        model = new ShowwindowTmallTotalNumFixedNum(user.getId(), -1);
        model.save();
        renderJSON(JsonUtil.getJson(model));
    }
    
    public static void submitManualWindowNum(Boolean enableManualWindowNum, int newManualWindowNum) {
    	if(enableManualWindowNum == null) {
    		enableManualWindowNum = Boolean.FALSE;
    	}
    	if(newManualWindowNum < 0) {
    		newManualWindowNum = 0;
    	}
    	final User user = getUser();
    	if(user == null) {
    		renderFailedJson("用户不存在");
    	}
    	final String key = ShowwindowTmallTotalNumFixedNum.KEY_TAG + user.getId();
    	ShowwindowTmallTotalNumFixedNum model = ShowwindowTmallTotalNumFixedNum.findById(user.getId());
        if (model != null) {
        	model.setEnableManualWindowNum(enableManualWindowNum);
        	model.setFixedNum(newManualWindowNum);
        	model.save();
        	if(enableManualWindowNum) {
            	Cache.safeSet(key, newManualWindowNum, "7d");
            } else {
            	Cache.safeSet(key, -1, "7d");
            }
        	renderSuccessJson("设置成功");
        }
        model = new ShowwindowTmallTotalNumFixedNum(user.getId(), newManualWindowNum, enableManualWindowNum);
        model.save();
        if(enableManualWindowNum) {
        	Cache.safeSet(key, newManualWindowNum, "7d");
        } else {
        	Cache.safeSet(key, -1, "7d");
        }
       
        renderSuccessJson("设置成功");
    }

    public static void setPriorNum(int priorNum, boolean enableSaleNum) {
        User user = getUser();
        if (priorNum < 0) {
            renderText("数目不能小于0哦亲");
        }
        boolean isSuccess = new ShowWindowConfig(user.getId(), priorNum, enableSaleNum).jdbcSave();
        if (isSuccess) {
            renderText("设置成功");
        } else {
            renderText("设置失败");
        }
    }

    public static void getConfig() {
        User user = getUser();
        ShowWindowConfig windowConfig = ShowWindowConfig.findOrCreate(user.getId());
        if (windowConfig != null) {
            renderJSON(JsonUtil.getJson(windowConfig));
        }

        windowConfig = new ShowWindowConfig(user.getId());
        boolean isSuccess = windowConfig.jdbcSave();
        if (isSuccess) {
            renderJSON(JsonUtil.getJson(windowConfig));
        } else {
            renderText("获取优先推荐销量数出错，请联系客服");
        }

    }

    public static void checkPriorSetting() {
        User user = getUser();
        int prior_num = ShowWindowConfig.findOrCreate(user.getId()).checkPrioSaleNum();
        List<ItemPlay> items = UserHasTradeItemCache.getByUser(user, prior_num, false);
        Iterator<ItemPlay> it = items.iterator();
        while (it.hasNext()) {
            ItemPlay item = it.next();
            if (item.getSalesCount() <= 0) {
                it.remove();
            }
        }
    }

    public static void rebuildCache() {
        User user = getUser();
        int count = DropWindowTodayCache.addCacheForUser(user, 159);
        renderText(" count :" + count);
    }

    public static void recentDown() {
        User user = getUser();
        List<DropWindowTodayCache> list = DropWindowTodayCache.userTodayCache(user.getId());
        StringBuilder sb = new StringBuilder();
//        log.info("[list:]" + list);
//        sb.append(list.toString());
//        sb.append('\n');

        for (DropWindowTodayCache dropWindowTodayCache : list) {
            sb.append(dropWindowTodayCache.toString());
            sb.append('\n');
        }

        renderText(sb.toString());
    }

    public static void setDoubleTwelveWindowOpen() {
        User user = getUser();
        if (user == null) {
            renderFailedJson("用户不存在或已过期");
        }

        long openId = DoubleTwelveOpenUser.findExistId(user.getId());
        if (openId > 0) {
            boolean isSuccess = DoubleTwelveOpenUser.deleteByUserId(openId);
            if (isSuccess) {
                renderSuccessJson("取消成功");
            } else {
                renderFailedJson("系统出现异常，请重试或联系客服哦亲");
            }
        }

        boolean isSuccess = new DoubleTwelveOpenUser(user.getId(), user.getUserNick()).jdbcSave();
        if (isSuccess) {
            renderSuccessJson("启动成功");
        } else {
            renderFailedJson("系统出现异常，请重试或联系客服哦亲");
        }
    }

    public static void isDoubleTwelveOpen() {
        User user = getUser();
        if (user == null) {
            renderFailedJson("用户不存在或已过期");
        }
        long openId = DoubleTwelveOpenUser.findExistId(user.getId());
        if (openId > 0) {
            renderSuccessJson("已开启");
        } else {
            renderFailedJson("未开启");
        }

    }

    public static void dropOnShow(long numIid) {
        User user = getUser();
        TMResult<Item> tmRes = new ShowWindowApi.DeleteRecommend(user, numIid).call();
//        TMResult res = TMResult.renderMsg(tmR);
        renderJSON(JsonUtil.getJson(tmRes));
    }


	/**
	 * 一键取消仓库中宝贝的橱窗推荐
	 * 先清除仓库中所有被推荐的宝贝 再清除所有被推荐中仓库的宝贝
	 */
	public static void batchDropInstockOnShow() {
		User user = getUser();
		
		Long pageSize = 200L;
		Long maxPageNum = 100L;
		
		List<Item> inventoryItemList = new ArrayList<Item>();
		
		Long itemInventory = new ItemsInventoryCount(user, null, null).call();
		long totalInventoryPageCount = CommonUtils.calculatePageCount(itemInventory, pageSize);
		if (totalInventoryPageCount > maxPageNum) {
			totalInventoryPageCount = maxPageNum;
		}
		
		for (Long pageNo = 1L; pageNo < totalInventoryPageCount + 1; pageNo++) {
			ItemsInventoryPage api = new ItemApi.ItemsInventoryPage(user, null, null, pageNo, pageSize);
			List<Item> items = api.call();
			if(!CommonUtils.isEmpty(items)) {
				inventoryItemList.addAll(items);
			}
		}
		
		if(CommonUtils.isEmpty(inventoryItemList)) {
			renderJSON(new TMResult(false, "未查询到任何仓库中的宝贝", null));
		}
		
		TMResult<List<TMResult>> tmResult = new TMResult();
		tmResult.setOk(true);
		tmResult.setRes(new ArrayList<TMResult>());
		
		// 清除仓库中所有被推荐的宝贝
		for (Item item : inventoryItemList) {
			if(item.getHasShowcase()) {
				TMResult<Item> result = new DeleteRecommend(user, item.getNumIid()).call();
				if (!result.isOk()) {
					tmResult.setOk(false);
				}
				tmResult.getRes().add(result);
			}
		}
		
		// 清除所有被推荐中仓库的宝贝
		List<Item> tbItems = new ItemsOnWindowInit(user).call();
		if (!CommonUtils.isEmpty(tbItems)) {
			for (Item item : tbItems) {
				String approveStatus = item.getApproveStatus();
				if (approveStatus.equalsIgnoreCase("instock")) {
					TMResult<Item> result = new DeleteRecommend(user, item.getNumIid()).call();
					if (!result.isOk()) {
						tmResult.setOk(false);
					}
					tmResult.getRes().add(result);
				}
			}
		}
		
		// 展示信息里面 不详细说明取消了多少个。因为取消的个数可能超过橱窗总数量
		List<TMResult> res = tmResult.getRes();
		if (tmResult.isOk()) {
			if (res.size() == 0) {
				tmResult.setMsg("未查询到推荐橱窗中有仓库里的宝贝，或仓库里有被橱窗推荐的宝贝");
			} else {
//				tmResult.setMsg("成功取消推荐" + res.size() + "个仓库中的宝贝");
				tmResult.setMsg("操作成功，已取消所有橱窗推荐中仓库里的宝贝");
			}
		} else {
			int success = 0, fail = 0;
			for (TMResult tmR : res) {
				if (tmR.isOk()) {
					success++;
				} else {
					fail++;
				}
			}
//			tmResult.setMsg("计划取消推荐" + res.size() + "个仓库中的宝贝,成功" + success + "个,失败" + fail + "个");
			tmResult.setMsg("操作成功，但是失败了" + fail + "个，请稍后刷新重试或者联系我们");
		}

		renderJSON(JsonUtil.getJson(tmResult));
	}

    public static void simpleCancel(long numIid) {
        User user = getUser();
        TMResult<Item> api = ApiJdpAdapter.doCancel(user, numIid);
        log.info(" apid res ;" + new Gson().toJson(api));
        renderJSON(api);
    }

    public static void simpleRecommend(long numIid) {

        log.info(format("simpleRecommend:numIid".replaceAll(", ", "=%s, ") + "=%s", numIid));
        User user = getUser();

        TMResult<Item> api = ApiJdpAdapter.doRecommend(user, numIid);
        log.info(" apid res ;" + new Gson().toJson(api));
        renderJSON(api);
    }

    public static void textRecent() {
        User user = getUser();
        StringBuilder sb = new StringBuilder();
        sb.append(" default api :");
        WindowValidationJob vJob = new WindowValidationJob(user, false);
        vJob.doJob();
        sb.append(vJob.getSb().toString());

        sb.append(" \n new origin api:\n");
        vJob = new WindowValidationJob(user, true);
        vJob.doJob();
        sb.append(vJob.getSb().toString());

        renderText(sb.toString());
    }

    public static void getNumIidsByUser() {
        User user = getUser();
        List<Long> numIids = new ArrayList<Long>();
        List<ItemPlay> items = UserHasTradeItemCache.getByUser(user, 5000, false);
        Iterator<ItemPlay> it = items.iterator();
        while (it.hasNext()) {
            ItemPlay item = it.next();
            if (item.getSalesCount() > 0) {
                numIids.add(item.getNumIid());
            }
        }
        renderJSON(JsonUtil.getJson(numIids));
    }

    public static void candidates() {
        User user = getUser();
        ShowWindowExecutor worker = new ShowWindowExecutor(user);
        List<Long> list = worker.buildCandidateItemIdList();
        List<ItemPlay> items = new ArrayList<ItemPlay>();
        for (Long long1 : list) {
            items.add(ItemDao.findByNumIid(user.getId(), long1));
        }

        StringBuilder sb = new StringBuilder();

        sb.append(worker.doMatch());
        sb.append('\n');
        worker.buildCandidateItemIdList();

        for (ItemPlay itemPlay : items) {
            sb.append(itemPlay.toString());
            sb.append('\n');
            sb.append(DateUtil.formDateForLog(itemPlay.ggetDelistTime()));
            sb.append('\n');
        }

        renderText(sb.toString());
    }

    public static void dumpRecommend() {
        User user = getUser();

        Set<Long> ids = ApiJdpAdapter.get(user).findCurrOnWindowNumIids(user);
        for (Long long1 : ids) {
            TMResult<Item> item = ApiJdpAdapter.doRecommend(user, long1);
            log.warn(" dump recommend :" + new Gson().toJson(item));
        }
    }

    public static void addWindow(Long userId) {
        new UserBatchOper(128) {
            public List<User> findNext() {
                return UserDao.findWindowShowOn(offset, limit);
            }

            @Override
            public void doForEachUser(final User user) {
                TMConfigs.getTradePool().submit(new Callable<List<Trade>>() {
                    @Override
                    public List<Trade> call() throws Exception {
                        Long onSaleNum = ApiJdpAdapter.get(user).onSaleItemNum(user);
                        if (onSaleNum == null || onSaleNum.intValue() < 500) {
                            return null;
                        }
                        log.info("[add id:]" + user.toIdNick());
                        WindowMoreRecommend.addId(user.getId());
                        return null;
                    }
                });

            }
        }.call();
    }

    public static void checkMustIds() {
        new UserBatchOper(128) {
            public List<User> findNext() {
                return UserDao.findWindowShowOn(offset, limit);
            }

            @Override
            public void doForEachUser(final User user) {
                TMConfigs.getTradePool().submit(new Callable<List<Trade>>() {
                    @Override
                    public List<Trade> call() {
                        checkForuser(user);
                        return null;
                    }
                });

            }

            public void checkForuser(User user) {
                Set<Long> mustids = ShowwindowMustDoItem.findIdsByUser(user.getId());

                List<ItemPlay> items = ItemDao.findByNumIids(user.getId(), mustids);
                for (ItemPlay itemPlay : items) {
                    Long numIid = itemPlay.getNumIid();
                    if (itemPlay.getStatus() == ItemPlay.Status.ONSALE) {
                        mustids.remove(numIid);
                        continue;
                    }

                    Item item = JdpItemModel.findByNumIid(user.getId(), numIid);
                    if ("onsale".equals(item.getApproveStatus())) {

                    } else {
                        log.error("remove id :" + numIid);
                        ShowwindowMustDoItem.remove(user.getId(), numIid);
                    }

                    mustids.remove(numIid);
                }

                for (Long noDbNumIid : mustids) {
                    Item item = JdpItemModel.findByNumIid(user.getId(), noDbNumIid);
                    if (item == null || "instock".equals(item.getApproveStatus())) {
                        log.error("remove id :" + noDbNumIid);
                        ShowwindowMustDoItem.remove(user.getId(), noDbNumIid);
                    }
                }

            }
        }.call();
    }

    public static void clearTmallMaxNum() {
        User user = getUser();
        ShowwindowTmallTotalNumFixedNum.clear(user);
    }

    public static void setTotalNum(int num) {
        User user = getUser();
        OperateItemApi.setUserTotalNum(user, num);
    }

    public static void fixTmallNoWindow() {
        ShowwindowTmallTotalNumFixedNum.fixNoCountUser();
    }

    public static void fixAllTmallUserCache() {
        ShowwindowTmallTotalNumFixedNum.fixAllUserCache();

    }
}
