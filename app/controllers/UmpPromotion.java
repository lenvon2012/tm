package controllers;

import actions.promotion.PromotionAction;
import actions.sku.SkuPriceEditAction;
import actions.ump.PromotionResult;
import actions.ump.PromotionSearchAction;
import actions.ump.UmpPromotionAction;
import autotitle.ItemPropAction;
import autotitle.ItemPropAction.PropUnit;
import bustbapi.FenxiaoApi;
import bustbapi.FenxiaoApi.FenxiaoProductBean;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.NumberUtil;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.Sku;

import configs.TMConfigs.App;
import dao.item.ItemDao;
import dao.ump.PromotionDao;
import models.item.ItemPlay;
import models.phoneDetailed.PhoneDetailed;
import models.promotion.TMProActivity;
import models.ump.PromotionPlay;
import models.ump.PromotionPlay.ItemPromoteType;
import models.ump.PromotionPlay.PromotionParams;
import models.user.User;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.cache.Cache;
import result.TMResult;
import utils.TBItemUtil;
import utils.TaobaoUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

public class UmpPromotion extends TMController {


	private static final Logger log = LoggerFactory
			.getLogger(UmpPromotion.class);

	private static final int ExpireSeconds = 120;

	private static final boolean IsNewPromotion = true;
	
	private static final Long FIRST_LOGIN_TIME_LIMIT = 1496246400000L; // 2017/06/01 0:0:0

	public static void addPromotion(Long activityId) {
		render("ump/promotionadd.html", activityId);
	}

	public static void modifyPromotion(Long activityId) {
		render("ump/promotionmodify.html", activityId);
	}

	public static void restartPromotion(Long activityId) {
	    render("ump/promotionrestart.html", activityId);
	}
	
	
	/**
	 * TODO ensure for the sku prices with multiple prices there....
	 * @param tmActivityId
	 * @param title
	 * @param cid
	 * @param sellerCid
	 * @param order
	 * @param isDis
	 * @param pn
	 * @param ps
	 */
	public static void searchPromotionAddItems(Long tmActivityId, String title,
			String cid, String sellerCid, int itemStatus, String order, String isDis, int pn,
			int ps) {

		User user = getUser();

		PageOffset po = new PageOffset(pn, ps);

		TMProActivity tmActivity = checkDazheActivity(tmActivityId);

		List<ItemPlay> itemList = ItemDao.findItemByConditionAndOrder(
				user.getId(), title, cid, sellerCid, order, isDis, po,
				IsNewPromotion, itemStatus);
		long count = ItemDao.countItemByConditionAndOrder(user.getId(), title,
				cid, sellerCid, isDis, IsNewPromotion, itemStatus);


		final ItemPromoteType defaultPromotionType = ItemPromoteType.discount;
		
		Map<Long, PromotionPlay> promotionMap = findAllActivePromotionNumIidMap(
				user, itemList);
		
		Set<Long> oldPromotionedNumIidSet = PromotionSearchAction
		        .findOldPromotionedNumIidSet(user, itemList);

		List<ItemPromotionBean> itemPromotionList = new ArrayList<ItemPromotionBean>();
		for (ItemPlay item : itemList) {
			PromotionPlay promotion = promotionMap.get(item.getNumIid());

			ItemPromotionBean itemPromotion = null;
			
			if (promotion != null || oldPromotionedNumIidSet.contains(item.getNumIid()) == false) {
			    itemPromotion = new ItemPromotionBean(item,
	                    promotion, tmActivityId, defaultPromotionType);
			} else {
			    itemPromotion = ItemPromotionBean.createByOldPromotion(item);
			}
			

			itemPromotionList.add(itemPromotion);
		}

		//ItemPromotionBean.ensureSkuAndFenxiao(user, itemPromotionList);
        //商品3天内是否生成过手机详情页
//        System.out.println("================================");
//        System.out.println(itemPromotionList.size());
        itemPromotionList=isCreate(itemPromotionList);

		TMResult tmRes = new TMResult(itemPromotionList, (int) count, po);

        //测试数据
//        tmRes.setCount(100);
		renderJSON(JsonUtil.getJson(tmRes));

	}

    /**
     * 商品3天内是否生成过手机详情页
     * 生成过    true
     * 未生成    false
     * */
    public static List<ItemPromotionBean> isCreate(List<ItemPromotionBean> itemPromotionList){
        for(ItemPromotionBean item:itemPromotionList){
            Long itemId=item.getNumIid();
            int id= PhoneDetailed.countById(itemId,"Success");
            if(id>0){
                item.setCreate(true);
            }else{
                item.setCreate(false);
            }
        }
        return itemPromotionList;
    }

    /**
     * 增加一个方法用来获取商品集合不rander  只是return
     * */
    public static TMResult getItems(Long tmActivityId, String title,
                                               String cid, String sellerCid, int itemStatus, String order, String isDis, int pn,
                                               int ps) {

        User user = getUser();

        PageOffset po = new PageOffset(pn, ps);

        TMProActivity tmActivity = checkDazheActivity(tmActivityId);

        List<ItemPlay> itemList = ItemDao.findItemByConditionAndOrder(
                user.getId(), title, cid, sellerCid, order, isDis, po,
                IsNewPromotion, itemStatus);
        long count = ItemDao.countItemByConditionAndOrder(user.getId(), title,
                cid, sellerCid, isDis, IsNewPromotion, itemStatus);


        final ItemPromoteType defaultPromotionType = ItemPromoteType.discount;

        Map<Long, PromotionPlay> promotionMap = findAllActivePromotionNumIidMap(
                user, itemList);

        Set<Long> oldPromotionedNumIidSet = PromotionSearchAction
                .findOldPromotionedNumIidSet(user, itemList);

        List<ItemPromotionBean> itemPromotionList = new ArrayList<ItemPromotionBean>();

        for (ItemPlay item : itemList) {
            PromotionPlay promotion = promotionMap.get(item.getNumIid());

            ItemPromotionBean itemPromotion = null;

            if (promotion != null || oldPromotionedNumIidSet.contains(item.getNumIid()) == false) {
                itemPromotion = new ItemPromotionBean(item,
                        promotion, tmActivityId, defaultPromotionType);
            } else {
                itemPromotion = ItemPromotionBean.createByOldPromotion(item);
            }


            itemPromotionList.add(itemPromotion);
        }

        //ItemPromotionBean.ensureSkuAndFenxiao(user, itemPromotionList);

        TMResult tmRes = new TMResult(itemPromotionList, (int) count, po);

        return tmRes;

    }

	public static void queryPromotionRestartNumIids(Long tmActivityId) {
	    
	    TMProActivity tmActivity = checkDazheActivity(tmActivityId);
	    
	    User user = getUser();
	    
	    Set<Long> noActiveNumIidSet = PromotionDao.findUnActiveNumIidsByTMActivityId(user.getId(),
	            tmActivityId);
	    
	    if (CommonUtils.isEmpty(noActiveNumIidSet) == false) {
	        //再排除加在其他活动里的宝贝
	        List<PromotionPlay> promotionList = PromotionDao
	                .findAllOnActiveByNumIids(user.getId(), noActiveNumIidSet);
	        
	        for (PromotionPlay promotion : promotionList) {
	            noActiveNumIidSet.remove(promotion.getNumIid());
	        }
	        
	        //还有老的活动
	        Set<Long> oldPromotionNumIidSet = PromotionAction.findPromotionedNumIids(user.getId(),
	                noActiveNumIidSet);
	        
	        for (Long numIid : oldPromotionNumIidSet) {
	            noActiveNumIidSet.remove(numIid);
	        }
	    }
	    
	    
	    renderResultJson(noActiveNumIidSet);
	    
	}
	
	
	public static void findDazheActivity(Long tmActivityId) {
	    TMProActivity tmActivity = checkDazheActivity(tmActivityId);
	    
	    renderResultJson(tmActivity);
	}
	
	public static void countActivityActivePromotions(Long tmActivityId) {
	    
	    if (tmActivityId == null) {
	        tmActivityId = 0L;
	    }
	    
	    User user = getUser();
	    
	    long count = PromotionDao.countActivePromotionsByTMActivityIdWithItemExist(user.getId(), tmActivityId);
	    
	    renderResultJson(count);
	    
	}
	
	
	public static void queryPromotionSelectedItems(Long tmActivityId, String title, 
	        int itemType,
	        String selectNumIids, int pn, int ps, boolean isRestartActivity) {
	    
	    User user = getUser();
	    
	    String vipNick = "柠檬绿茶运动天下";

        PageOffset po = new PageOffset(pn, ps);

        TMProActivity tmActivity = checkDazheActivity(tmActivityId);
        
        Set<Long> allNumIidSet = new HashSet<Long>();
        
        //加入选中的numIid
        if (itemType != PromotionItemType.ExistActivityItem) {
            Set<Long> selectNumIidSet = parseIdsToSet(selectNumIids);
            allNumIidSet.addAll(selectNumIidSet);
        }
        
        //加入活动中原有的numIid
        if (itemType == PromotionItemType.AllActivityItem 
                || itemType == PromotionItemType.ExistActivityItem) {
            Set<Long> promotionNumIidSet = PromotionDao.findActiveNumIidsByTMActivityId(user.getId(), 
                    tmActivityId);
        
            allNumIidSet.addAll(promotionNumIidSet);
        }
        
        
        List<ItemPlay> itemList = new ArrayList<ItemPlay>();
        if (CommonUtils.isEmpty(allNumIidSet) == false) {
            
            itemList = ItemDao.findByTitleAndNumIids(user.getId(), title, allNumIidSet, po);
            
        }
        
        long count = ItemDao.countByTitleAndNumIids(user.getId(), title, allNumIidSet);

        /*long decreaseCount = PromotionDao.countByPromotionType(user.getId(),
                tmActivityId, ItemPromoteType.decrease);
        long discountCount = PromotionDao.countByPromotionType(user.getId(),
                tmActivityId, ItemPromoteType.discount);
*/
        ItemPromoteType defaultPromotionType = ItemPromoteType.discount;
        if(user.getFirstLoginTime() > FIRST_LOGIN_TIME_LIMIT) {
        	defaultPromotionType = ItemPromoteType.decrease;
        }
        /*if (decreaseCount > discountCount) {
            defaultPromotionType = ItemPromoteType.decrease;
        } else {
            defaultPromotionType = ItemPromoteType.discount;
        }*/

        Map<Long, PromotionPlay> activePromotionMap = vipNick.equalsIgnoreCase(user.getUserNick())? new HashMap<Long, PromotionPlay>() : findAllActivePromotionNumIidMap(
                user, itemList);
        
        Map<Long, PromotionPlay> slavePromotionMap = new HashMap<Long, PromotionPlay>();
        if (isRestartActivity == true) {
            slavePromotionMap = PromotionSearchAction.findActivityPromotionNumIidMap(user, 
                    tmActivityId, itemList);
        }

        List<ItemPromotionBean> itemPromotionList = new ArrayList<ItemPromotionBean>();

        for (ItemPlay item : itemList) {
            PromotionPlay promotion = activePromotionMap.get(item.getNumIid());
            
            if (promotion == null) {
                promotion = slavePromotionMap.get(item.getNumIid());
            }

            ItemPromotionBean itemPromotion = new ItemPromotionBean(item,
                    promotion, tmActivityId, defaultPromotionType);

            itemPromotionList.add(itemPromotion);
        }

        ItemPromotionBean.ensureSkuAndFenxiao(user, itemPromotionList);
        
        TMResult tmRes = new TMResult(itemPromotionList, (int) count, po);

        renderJSON(JsonUtil.getJson(tmRes));
	    
	}

	
	private static Map<Long, PromotionPlay> findAllActivePromotionNumIidMap(
			User user, List<ItemPlay> itemList) {

		if (CommonUtils.isEmpty(itemList)) {
			return new HashMap<Long, PromotionPlay>();
		}

		Set<Long> numIidSet = new HashSet<Long>();
		for (ItemPlay item : itemList) {
			numIidSet.add(item.getNumIid());
		}

		List<PromotionPlay> promotionList = PromotionDao
				.findAllOnActiveByNumIids (user.getId(), numIidSet);

		Map<Long, PromotionPlay> promotionMap = new HashMap<Long, PromotionPlay>();

		for (PromotionPlay promotion : promotionList) {
			promotionMap.put(promotion.getNumIid(), promotion);
		}

		return promotionMap;
	}

	
	
	
	
	public static void findExistPromotions(Long tmActivityId, String title,
			String cid, String sellerCid, String order, int itemType, String targetNumIids, 
			int pn, int ps) {

		User user = getUser();

		PageOffset po = new PageOffset(pn, ps);

		TMProActivity tmActivity = checkDazheActivity(tmActivityId);
		
		Set<Long> targetNumIidSet = new HashSet<Long>();
		
		//比如查询错误的宝贝的时候，就要为true
		boolean isMustInNumIids = false;
		if (itemType == PromotionItemType.ErrorSettingItem) {
		    isMustInNumIids = true;
		    targetNumIidSet = parseIdsToSet(targetNumIids);
		} else {
		    isMustInNumIids = false;
		}

		List<ItemPromotionBean> promotionList = PromotionDao
				.findItemActivePromotionsByRules(user.getId(), tmActivityId, title,
						cid, sellerCid, isMustInNumIids, targetNumIidSet, order, po);

		long count = PromotionDao.countActivePromotionsByRules(user.getId(),
				tmActivityId, title, cid, sellerCid, isMustInNumIids, targetNumIidSet);
		
		ItemPromotionBean.ensureSkuAndFenxiao(user, promotionList);

		TMResult tmResult = new TMResult(promotionList, (int) count, po);
		renderJSON(JsonUtil.getJson(tmResult));

	}
	
	
	
	
	

	public static void submitAddPromotions(Long tmActivityId, String paramsJson) {

		User user = getUser();
		// 先检查w2权限
		checkW2Expires(user);

		if (StringUtils.isEmpty(paramsJson)) {
			renderError("请先设置要加入活动的宝贝！");
		}
		List<PromotionParams> paramsList = checkPromotionParams(paramsJson);

		if (CommonUtils.isEmpty(paramsList)) {
			renderError("请先设置要加入活动的宝贝！");
		}

		TMProActivity tmActivity = checkDazheActivity(tmActivityId);

		PromotionResult promotionRes = UmpPromotionAction.doAddPromotions(user,
				tmActivity, paramsList);

		if (promotionRes.isSuccess() == false) {
			renderError(promotionRes.getMessage());
		}

		renderResultJson(promotionRes);
	}

	public static void submitRestartPrmotions(Long tmActivityId, String paramsJson, String selectNumIids) {
	    
	    TMProActivity tmActivity = checkDazheActivity(tmActivityId);
	    
	    User user = getUser();
        // 先检查w2权限
        checkW2Expires(user);
	    
        if (StringUtils.isEmpty(selectNumIids)) {
            renderError("请先选择要重启的宝贝！");
        }
        
        Set<Long> selectNumIidSet = parseIdsToSet(selectNumIids);
        if (CommonUtils.isEmpty(selectNumIidSet)) {
            renderError("请先选择要重启的宝贝！");
        }
        
        Map<Long, PromotionPlay> slavePromotionMap = PromotionSearchAction.findActivityPromotionNumIidMap(user, 
                tmActivityId, selectNumIidSet);

        
        List<PromotionParams> paramsList = checkPromotionParams(paramsJson);
        
        Map<Long, PromotionParams> paramsMap = new HashMap<Long, PromotionParams>();
        
        for (PromotionParams params : paramsList) {
            paramsMap.put(params.getNumIid(), params);
        }
        
        for (Long numIid : selectNumIidSet) {
            if (paramsMap.containsKey(numIid)) {
                continue;
            }
            PromotionPlay promotion = slavePromotionMap.get(numIid);
            if (promotion == null || promotion.isActive() == true) {
                continue;
            }
            
            ItemPlay item = ItemDao.findByNumIid(user.getId(), numIid);
            if (item == null) {
                log.error("the item: " + numIid + ", userId: " + user.getId() + " has been deleted-------");
                continue;
            }
            
            PromotionParams params = new PromotionParams();
            params.setNumIid(promotion.getNumIid());
            params.setDecreaseAmount(promotion.getDecreaseAmount());
            params.setDiscountRate(promotion.getDiscountRate());
            params.setPromotionType(promotion.getPromotionType());
            
            paramsList.add(params);
            
        }
        
        
        PromotionResult promotionRes = UmpPromotionAction.doAddPromotions(user,
                tmActivity, paramsList);

        if (promotionRes.isSuccess() == false) {
            renderError(promotionRes.getMessage());
        }

        renderResultJson(promotionRes);
	}
	
	
	
	public static void submitUpdatePromotions(Long tmActivityId,
			String paramsJson) {
		User user = getUser();
		// 先检查w2权限
		checkW2Expires(user);

		if (StringUtils.isEmpty(paramsJson)) {
			renderError("请先设置要修改的宝贝！");
		}

		List<PromotionParams> paramsList = checkPromotionParams(paramsJson);

		if (CommonUtils.isEmpty(paramsList)) {
			renderError("请先设置要修改的宝贝！");
		}

		TMProActivity tmActivity = checkDazheActivity(tmActivityId);

		PromotionResult promotionRes = UmpPromotionAction.updateSomePromotions(
				user, tmActivity, paramsList);

		if (promotionRes.isSuccess() == false) {
			renderError(promotionRes.getMessage());
		}

		renderResultJson(promotionRes);
	}

	public static void deletePromotions(Long tmActivityId, String numIids) {

		User user = getUser();
		// 先检查w2权限
		checkW2Expires(user);

		if (StringUtils.isEmpty(numIids)) {
			renderError("请先选择要从活动中删除的宝贝！");
		}
		TMProActivity tmActivity = checkDazheActivity(tmActivityId);

		Set<Long> numIidSet = parseIdsToSet(numIids);

		if (CommonUtils.isEmpty(numIidSet)) {
			renderError("请先选择要从活动中删除的宝贝！");
		}

		PromotionResult promotionRes = UmpPromotionAction.deleteSomePromotionsTotally(
				user, tmActivity, numIidSet);

		if (promotionRes.isSuccess() == false) {
			renderError(promotionRes.getMessage());
		}

		renderResultJson(promotionRes);

	}

	private static List<PromotionParams> checkPromotionParams(String paramsJson) {

		PromotionParams[] paramsArray = JsonUtil.toObject(paramsJson,
				PromotionParams[].class);

		if (paramsArray == null) {
			renderError("系统出现一些异常，宝贝解析出错，请联系我们！");
		}

		List<PromotionParams> tempList = Arrays.asList(paramsArray);
		
		List<PromotionParams> paramsList = new ArrayList<PromotionParams>();
		
		paramsList.addAll(tempList);

		for (PromotionParams params : paramsList) {
		    if (params == null) {
		        renderError("系统出现一些异常，促销解析出错，请联系我们！");
		    }
		    ItemPromoteType promotionType = params.getPromotionType();
			if (promotionType == null) {
				renderError("系统出现一些异常，促销类型出错，请联系我们！");
			}
			if (ItemPromoteType.decrease.equals(promotionType) == false 
			        && ItemPromoteType.discount.equals(promotionType) == false) {
			    renderError("系统出现一些异常，促销类型出错，请联系我们！");
			}
			if (params.getDecreaseAmount() <= 0
					&& params.getDiscountRate() <= 0) {
				renderError("系统出现一些异常，促销参数出错，请联系我们！");
			}
			
			if (ItemPromoteType.discount.equals(promotionType)) {
			    if (params.getDiscountRate() <= 0 || params.getDiscountRate() >= 1000) {
			        renderError("系统出现一些异常，促销折扣出错，请联系我们！");
			    }
			}
			
		}

		return paramsList;

	}

	static TMProActivity checkDazheActivity(Long tmActivityId) {

		User user = getUser();

		if (tmActivityId == null || tmActivityId <= 0) {
			renderError("系统出现异常，打折活动ID为空，请联系我们！");
		}

		TMProActivity tmActivity = TMProActivity.findByActivityId(user.getId(),
				tmActivityId);
		if (tmActivity == null) {
			renderError("系统出现异常，找不到要指定的打折活动，请检查是否已删除该活动，或联系我们！");
		}

		if (!tmActivity.isDiscountActivity() && !tmActivity.isNewDiscountActivity()) {
			renderError("系统出现异常，这不是一个打折活动，请联系我们！");
		}

		return tmActivity;
	}

	public static Set<Long> parseIdsToSet(String ids) {
		if (StringUtils.isEmpty(ids)) {
			return new HashSet<Long>();
		}
		String[] idArray = ids.split(",");
		Set<Long> idSet = new HashSet<Long>();

		for (String idStr : idArray) {
			if (StringUtils.isEmpty(idStr)) {
				continue;
			}
			Long id = NumberUtil.parserLong(idStr, 0L);
			if (id == null || id <= 0L) {
				continue;
			}
			idSet.add(id);
		}

		return idSet;
	}

	static void checkW2Expires(User user) {
		
	    checkW2Expires(user, ShouQuanUrl);
	}
	
	private static final String ShouQuanUrl = "/in/authDazhe";
	

	public static void newReShouquan() {
        
	    String authUrl = genAuthUrl(ShouQuanUrl);
	    
	    redirect(authUrl);
    }
	
	static String genReshouquanUrl() {
	    String authUrl = genAuthUrl(ShouQuanUrl);
	    return authUrl;
	}

	
	static void checkW2Expires(User user, String url) {
		if (StringUtils.isEmpty(url)) {
			url = ShouQuanUrl;
		}
		
		boolean isAuthorized = isHasAuthorized(ExpireSeconds);
		
		if (isAuthorized == false) {

			String authUrl = genAuthUrl(url);

			renderAuthorizedError("", authUrl);
		}
	}
	
	
	static boolean isHasAuthorized(final int expireSeconds) {
	    
	    User user = getUser();
	    
	    Long expireTime = W2ExpireTimeCache.getFromCache(user.getId());
        if (expireTime == null) {
            expireTime = 0L;
        }
        
        //expireTime > 0 且 当前时间还没到过期时间
        if (System.currentTimeMillis() + expireSeconds * 1000 <= expireTime) {
            long remainSeconds = (expireTime - System.currentTimeMillis()) / 1000;
            log.info("hit w2 expire time in cache for user: " + user.getUserNick() 
                    + ", remain time: " + remainSeconds + " s-----");
            return true;
        } 
        
        //否则，不管是没在cache中找到，或者cache中认为已经过期了，都要调用一次接口
        
        
        boolean isAuthorized = true;
        
        //没有从Cache获得授权信息，调用接口获取（虽然一般这时就是没有授权的）
        String w2TimeStr = TaobaoUtil.getRefreshResponseProperty(user,
                TaobaoUtil.W2_EXPIRES_IN);
        
        if (StringUtils.isEmpty(w2TimeStr)) {
            isAuthorized = false;
        } else {
            long w2Time = NumberUtil.parserInt(w2TimeStr, 0);
            if (w2Time <= ExpireSeconds) {// 小于120秒
                isAuthorized = false;
            }
            
            W2ExpireTimeCache.putToCache(user.getId(), w2Time * 1000 + System.currentTimeMillis());
            
        }
        
        return isAuthorized;
        
	}
	
	
	private static String genAuthUrl(String url) {
	    String authUrl = App.TAOBAO_AUTH_URL;

        String redirectUrl = APIConfig.get().getRedirURL() + url;

        try {
            redirectUrl = URLEncoder.encode(redirectUrl, "utf-8");
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
            renderError("系统出现一些异常，请联系我们！");
        }

        authUrl += "&redirect_uri=" + redirectUrl;
        
        return authUrl;
	}
	
	
	//w2授权缓存
	public static class W2ExpireTimeCache {
	    
	    private static final String Prefix = "W2ExpireTimeCache_";
	    
	    private static String genKey(Long userId) {
	        String key = Prefix + Play.id + "_" + userId;
	        
	        return key;
	    }
	    
	    public static void putToCache(Long userId, long w2ExpireTime) {
	        
	        String key = genKey(userId);
	        
	        try {
	            Cache.set(key, w2ExpireTime, "30min");
	        } catch (Exception ex) {
	            log.error(ex.getMessage(), ex);
	        }
	        
	    }
	    
	    public static Long getFromCache(Long userId) {
	        String key = genKey(userId);
            
            try {
                Long expireTime = (Long) Cache.get(key);
                return expireTime;
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
            return null;
	    }
	    
	}
	
	

	public static class ItemPromotionBean {

		private Long numIid;

		private String title;

		private String picURL;

		private double price;

		private boolean hasPromotion = false;

		private long promotionId;

		private ItemPromoteType promotionType;

		private long decreaseAmount;

		private long discountRate;

		private boolean isthisActivity = false;

        //3天内是否生成过手机详情页
        private boolean isCreate=false;
		
		
		private boolean hasSkuPrices;
		
		private double minSkuPrice;
		
		private double maxSkuPrice;
		
		private boolean isFenxiao;
		
		private double fenxiaoPrice;
		
		private String outId;
		
		private List<SkuPriceBean> skuPriceList = new ArrayList<SkuPriceBean>();
		

		public Long getNumIid() {
			return numIid;
		}

		public void setNumIid(Long numIid) {
			this.numIid = numIid;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getPicURL() {
			return picURL;
		}

		public void setPicURL(String picURL) {
			this.picURL = picURL;
		}

		public double getPrice() {
			return price;
		}

		public void setPrice(double price) {
			this.price = price;
		}

		public long getPromotionId() {
			return promotionId;
		}

		public void setPromotionId(long promotionId) {
			this.promotionId = promotionId;
		}

		public ItemPromoteType getPromotionType() {
			return promotionType;
		}

		public void setPromotionType(ItemPromoteType promotionType) {
			this.promotionType = promotionType;
		}

		public long getDecreaseAmount() {
			return decreaseAmount;
		}

		public void setDecreaseAmount(long decreaseAmount) {
			this.decreaseAmount = decreaseAmount;
		}

		public long getDiscountRate() {
			return discountRate;
		}

		public void setDiscountRate(long discountRate) {
			this.discountRate = discountRate;
		}

		public boolean isIsthisActivity() {
			return isthisActivity;
		}

		public void setIsthisActivity(boolean isthisActivity) {
			this.isthisActivity = isthisActivity;
		}

		public boolean isHasPromotion() {
			return hasPromotion;
		}

		public void setHasPromotion(boolean hasPromotion) {
			this.hasPromotion = hasPromotion;
		}

		public boolean isHasSkuPrices() {
            return hasSkuPrices;
        }

        public void setHasSkuPrices(boolean hasSkuPrices) {
            this.hasSkuPrices = hasSkuPrices;
        }

        public double getMinSkuPrice() {
            return minSkuPrice;
        }

        public void setMinSkuPrice(double minSkuPrice) {
            this.minSkuPrice = minSkuPrice;
        }

        public double getMaxSkuPrice() {
            return maxSkuPrice;
        }

        public void setMaxSkuPrice(double maxSkuPrice) {
            this.maxSkuPrice = maxSkuPrice;
        }

        public boolean isFenxiao() {
            return isFenxiao;
        }

        public void setFenxiao(boolean isFenxiao) {
            this.isFenxiao = isFenxiao;
        }

        public double getFenxiaoPrice() {
            return fenxiaoPrice;
        }

        public void setFenxiaoPrice(double fenxiaoPrice) {
            this.fenxiaoPrice = fenxiaoPrice;
        }

        public boolean isCreate() {
            return isCreate;
        }

        public void setCreate(boolean isCreate) {
            this.isCreate = isCreate;
        }

        public String getOutId() {
            return outId;
        }

        public void setOutId(String outId) {
            this.outId = outId;
        }

        public List<SkuPriceBean> getSkuPriceList() {
            return skuPriceList;
        }

        public void setSkuPriceList(List<SkuPriceBean> skuPriceList) {
            this.skuPriceList = skuPriceList;
        }
        
        public ItemPromotionBean() {
            super();
        }

        public ItemPromotionBean(Long numIid, String title, String picURL,
				double price, boolean hasPromotion, long promotionId,
				ItemPromoteType promotionType, long decreaseAmount,
				long discountRate, boolean isthisActivity) {
			super();
			this.numIid = numIid;
			this.title = title;
			this.picURL = picURL;
			this.price = price;
			this.hasPromotion = hasPromotion;
			this.promotionId = promotionId;
			this.promotionType = promotionType;
			this.decreaseAmount = decreaseAmount;
			this.discountRate = discountRate;
			this.isthisActivity = isthisActivity;
			
			
		}
        
        
		public ItemPromotionBean(ItemPlay item, PromotionPlay promotion,
				Long tmActivityId, ItemPromoteType defaultPromotionType) {

			super();
			this.numIid = item.getNumIid();
			this.title = item.getTitle();
			this.picURL = item.getPicURL();
			this.price = item.getPrice();
			
			User user = getUser();
			
			String vipNick = "柠檬绿茶运动天下";

			if (promotion != null) {
			    if (promotion.isActive() == true) {
			        this.hasPromotion = true;
	                this.promotionId = promotion.getPromotionId();
	                if (tmActivityId != null
	                        && tmActivityId.equals(promotion.getTmActivityId())) {
	                    this.isthisActivity = true;
	                } else {
	                	if(vipNick.equalsIgnoreCase(user.getUserNick())) {
	                		this.hasPromotion = false;
	    	                this.isthisActivity = false;
	                	} else {
	                		this.isthisActivity = false;
	                	}
	                }
			    } else {
			        this.hasPromotion = false;
	                this.isthisActivity = false;
			    }
				
				this.promotionType = promotion.getPromotionType();
				this.decreaseAmount = promotion.getDecreaseAmount();
				this.discountRate = promotion.getDiscountRate();

				

			} else {
				this.hasPromotion = false;
				this.isthisActivity = false;
				this.promotionType = defaultPromotionType;
				this.decreaseAmount = 0;
				this.discountRate = 1000;
			}

			
		}

		public static ItemPromotionBean createByOldPromotion(ItemPlay item) {
		    ItemPromotionBean itemPromotion = new ItemPromotionBean();
		    
		    itemPromotion.numIid = item.getNumIid();
		    itemPromotion.title = item.getTitle();
		    itemPromotion.picURL = item.getPicURL();
		    itemPromotion.price = item.getPrice();
		    
		    itemPromotion.hasPromotion = true;
		    itemPromotion.isthisActivity = false;
		    
		    return itemPromotion;
		}

		
		
		public static void ensureSkuAndFenxiao(User user, List<ItemPromotionBean> itemPromotionList) {
		    
		    ensureSkuPrices(user, itemPromotionList);
		    
		    ensureFenxiao(user, itemPromotionList);
		}
		
		private static void ensureFenxiao(User user, List<ItemPromotionBean> itemPromotionList) {
		    
		    if (CommonUtils.isEmpty(itemPromotionList)) {
		        return;
		    }
		    Set<Long> numIidSet = new HashSet<Long>();
		    for (ItemPromotionBean itemPromotion : itemPromotionList) {
		        if (itemPromotion == null) {
		            continue;
		        }
		        numIidSet.add(itemPromotion.getNumIid());
		    }
		    
		    if (CommonUtils.isEmpty(numIidSet)) {
                return;
            }
		    
		    String numIids = StringUtils.join(numIidSet, ",");
		    List<ItemPlay> itemList = ItemDao.findByIds(user.getId(), numIids);
		    
		    //log.info("find " + itemList.size() + " items, " + numIidSet.size() + " numIids for user: " 
	        //        + user.getUserNick() + "------");
		    
		    FenxiaoApi.ensureFenxiaoInfo(user, itemList);

		    Map<Long, ItemPlay> itemMap = new HashMap<Long, ItemPlay>();
		    
		    for (ItemPlay item : itemList) {
		        if (item == null) {
		            continue;
		        }
		        itemMap.put(item.getNumIid(), item);
		    }
		    
		    for (ItemPromotionBean itemPromotion : itemPromotionList) {
                if (itemPromotion == null) {
                    continue;
                }
                ItemPlay item = itemMap.get(itemPromotion.getNumIid());
                if (item == null) {
                    continue;
                }
                if (item.isFenxiao() == false) {
                    continue;
                }
                FenxiaoProductBean fenxiaoBean = item.getFenxiaoProductBean();
                if (fenxiaoBean == null) {
                    continue;
                }
                
                //log.info("fenxiaoBean: " + JsonUtil.getJson(fenxiaoBean));
                
                itemPromotion.setFenxiao(true);
                double fenxiaoPrice = (double) fenxiaoBean.getCostPrice() * 1.0 / 100;
                itemPromotion.setFenxiaoPrice(fenxiaoPrice);
            }
		    
		}
		
        private static void ensureSkuPrices(User user, List<ItemPromotionBean> itemPromotionList) {
            if (CommonUtils.isEmpty(itemPromotionList)) {
                return;
            }
            
            //先从jdp中取
            Set<Long> numIidSet = new HashSet<Long>();
            for (ItemPromotionBean itemPromotion : itemPromotionList) {
                numIidSet.add(itemPromotion.getNumIid());
            }
            
            Map<Long, Item> tbItemMap = TBItemUtil.findTaobaoItemMap(user, numIidSet);

            
            for (ItemPromotionBean itemPromotion : itemPromotionList) {
                if (itemPromotion == null) {
                    continue;
                }
                Item item = tbItemMap.get(itemPromotion.getNumIid());
                if (item == null) {
                    continue;
                }
                String outIdStr = item.getOuterId();
                if (StringUtils.isEmpty(outIdStr) == false) {
                    itemPromotion.setOutId(outIdStr);
                } else {
                    itemPromotion.setOutId("");
                }
                
                List<Sku> skuList = item.getSkus();
                if (CommonUtils.isEmpty(skuList)) {
                    itemPromotion.setHasSkuPrices(false);
                } else {
                    double minSkuPrice = Double.MAX_VALUE;
                    double maxSkuPrice = 0;
                    
                    List<SkuPriceBean> skuBeanList = new ArrayList<SkuPriceBean>();
                    List<PropUnit> propList = ItemPropAction.mergePropAlis(item);
                    if (CommonUtils.isEmpty(propList)) {
                        propList = new ArrayList<PropUnit>();
                    }
                    
                    for (Sku sku : skuList) {
                        if (sku == null) {
                            continue;
                        }
                        SkuPriceBean skuBean = new SkuPriceBean(sku, propList);
                        skuBeanList.add(skuBean);
                        
                        String priceStr = sku.getPrice();
                        if (StringUtils.isEmpty(priceStr)) {
                            continue;
                        }
                        double skuPrice = CommonUtils.String2Double(priceStr);
                        if (skuPrice < minSkuPrice) {
                            minSkuPrice = skuPrice;
                        }
                        if (skuPrice > maxSkuPrice) {
                            maxSkuPrice = skuPrice;
                        }
                    }
                    if (maxSkuPrice != minSkuPrice) {
                        itemPromotion.setHasSkuPrices(true);
                        itemPromotion.setMinSkuPrice(minSkuPrice);
                        itemPromotion.setMaxSkuPrice(maxSkuPrice);
                        itemPromotion.setSkuPriceList(skuBeanList);
                    } else {
                        itemPromotion.setHasSkuPrices(false);
                    }
                    
                    
                }
            }
        }

	}
	
	
	public static class PromotionItemType {
	    
	    public static final int AllActivityItem = 1;
	    
	    public static final int ExistActivityItem = 2;
	    
	    public static final int NewActivityItem = 4;
	    
	    public static final int ErrorSettingItem = 8;
	    
	}
	
	public static class SkuPriceBean {
	    
	    private String skuProps;
	    
	    private double skuPrice;

        public SkuPriceBean(Sku sku, List<PropUnit> propList) {
            super();
            try {
                if (sku == null) {
                    return;
                }
                skuProps = SkuPriceEditAction.getSkuPropertyNames(sku, propList);
                skuPrice = NumberUtil.parserDouble(sku.getPrice(), 0);
                
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }

        public String getSkuProps() {
            return skuProps;
        }

        public void setSkuProps(String skuProps) {
            this.skuProps = skuProps;
        }

        public double getSkuPrice() {
            return skuPrice;
        }

        public void setSkuPrice(double skuPrice) {
            this.skuPrice = skuPrice;
        }
	    
	    
	    
	}

}
