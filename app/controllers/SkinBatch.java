
package controllers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.item.ItemPlay;
import models.item.ItemPlay.Status;
import models.user.User;

import org.apache.commons.lang3.StringUtils;

import result.TMResult;
import utils.TaobaoUtil;
import bustbapi.ItemApi;
import bustbapi.ItemApi.MultiItemsListGet;
import bustbapi.ItemListingApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.DateUtil;
import com.dbt.cred.utils.JsonUtil;
import com.taobao.api.FileItem;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.ItemImg;

import dao.item.ItemDao;

public class SkinBatch extends TMController {

    public static void index() {
        render("skinbatch/skinbatch.html");
    }

    public static void badComment() {
        render("skinbatch/skinbadcomment.html");
    }
    
    public static void batchDelist() {
        render("skinbatch/batchdelist.html");
    }
    
    public static void batchPrice() {
        render("skinbatch/batchprice.html");
    }
    
    public static void batchposition() {
    	render("skinbatch/batchposition.html");
    }

    /**
     * 查询宝贝
     * @param title 标题
     * @param catId 分类
     * @param state 状态
     * @param orderProp 排序的属性
     * @param orderType 是升序还是降序
     */
    public static void queryItems(String title, Long catId, String state,
            String orderProp, String orderType, int pn, int ps) {
        User user = getUser();

        boolean isOrderAsc = true;
        if (!StringUtils.isEmpty(orderType) && orderType.toLowerCase().equals("desc"))
            isOrderAsc = false;
        else
            isOrderAsc = true;

        String catIdStr = "";
        if (catId != null && catId.longValue() > 0) {
            catIdStr = String.valueOf(catId);
        }

        PageOffset po = new PageOffset(pn, ps, 10);

        int status = 2;//表示全部
        if (StringUtils.isEmpty(state)) {
            status = 2;
        }
        else if (state.toLowerCase().equals("all")) {
            status = 2;
        }
        else if (state.toLowerCase().equals("onsale")) {
            status = 0;
        }
        else if (state.toLowerCase().equals("instock")) {
            status = 1;
        }

        TMResult result = ItemDao.findWithOrder(user, title, status, catIdStr, orderProp, isOrderAsc, po);

        renderJSON(JsonUtil.getJson(result));
    }

    /**
     * 批量上架
     * @param numIids
     */
    public static void doBatchListing(List<Long> numIidList) {
        User user = getUser();
        if (numIidList == null || numIidList.isEmpty()) {
            renderError("亲，请先选择要上架的宝贝");
        }

        List<Item> itemList = loadItems(user, numIidList);

        List<Long> successList = new ArrayList<Long>();
        Map<Long, String> errorMap = new HashMap<Long, String>();
        Map<Long, Long> newDelistTimeMap = new HashMap<Long, Long>();

        for (Item item : itemList) {
            //在架上的
            if (item.getApproveStatus().equals("onsale")) {
                successList.add(item.getNumIid());
                continue;
            }
            ItemListingApi.ItemUpdateListing listingApi = new ItemListingApi.ItemUpdateListing(user, item.getNumIid(),
                    item.getNum());
            Item resItem = listingApi.call();
            boolean isSuccess = listingApi.isApiSuccess();
            if (isSuccess == true && resItem != null) {
                successList.add(item.getNumIid());
                if (resItem.getModified() != null) {
                    newDelistTimeMap.put(item.getNumIid(), 
                            resItem.getModified().getTime() + 7 * DateUtil.DAY_MILLIS);
                }
                
                //errorList.add(item.getNumIid());//测试错误
            } else {
                String errorMsg = listingApi.getErrorMsg();
                errorMap.put(item.getNumIid(), errorMsg);
            }
        }

        //更新itemPlay
        List<ItemPlay> successItemList = ItemDao.findByNumIids(user.getId(), successList);
        for (ItemPlay itemPlay : successItemList) {
            Long newDelistTime = newDelistTimeMap.get(itemPlay.getNumIid());
            if (newDelistTime != null) {
                itemPlay.setDeListTime(newDelistTime);
            }
            itemPlay.setStatus(Status.ONSALE);
            itemPlay.jdbcSave();
        }

        String message = "成功上架" + successItemList.size() + "个宝贝";
        if (errorMap.size() > 0) {
            message += "，失败" + errorMap.size() + "个，点击确定后查看详情";
        }
        List<ItemPlay> errorItemList = ItemDao.findByNumIids(user.getId(), errorMap.keySet());
        List<BatchOpMessage> batchOpMsgList = new ArrayList<BatchOpMessage>();
        for (ItemPlay itemPlay : errorItemList) {
            BatchOpMessage batchOpMsg = new BatchOpMessage(itemPlay, "上架失败，" + errorMap.get(itemPlay.getNumIid()));
            batchOpMsgList.add(batchOpMsg);
        }

        renderSuccess(message, batchOpMsgList);
    }

    /**
     * 批量下架
     * @param numIids
     */
    public static void doBatchDeListing(List<Long> numIidList) {
        User user = getUser();
        if (numIidList == null || numIidList.isEmpty()) {
            renderError("亲，请先选择要下架的宝贝");
        }
        List<Item> itemList = loadItems(user, numIidList);

        List<Long> successList = new ArrayList<Long>();
        Map<Long, String> errorMap = new HashMap<Long, String>();
        Map<Long, Long> newDelistTimeMap = new HashMap<Long, Long>();

        for (Item item : itemList) {
            //在架下的
            if (item.getApproveStatus().equals("onsale") == false) {
                successList.add(item.getNumIid());
                continue;
            }
            ItemListingApi.ItemUpdateDelisting delistingApi = new ItemListingApi.ItemUpdateDelisting(user,
                    item.getNumIid());
            Item resItem = delistingApi.call();
            boolean isSuccess = delistingApi.isApiSuccess();
            if (isSuccess == true && resItem != null) {
                successList.add(item.getNumIid());
                if (resItem.getModified() != null) {
                    newDelistTimeMap.put(item.getNumIid(), resItem.getModified().getTime());
                }
                //errorList.add(item.getNumIid());//测试错误
            } else {
                String errorMsg = delistingApi.getErrorMsg();
                errorMap.put(item.getNumIid(), errorMsg);
            }
        }

        //更新itemPlay
        List<ItemPlay> successItemList = ItemDao.findByNumIids(user.getId(), successList);
        for (ItemPlay itemPlay : successItemList) {
            Long newDelistTime = newDelistTimeMap.get(itemPlay.getNumIid());
            if (newDelistTime != null) {
                itemPlay.setDeListTime(newDelistTime);
            }
            itemPlay.setStatus(Status.INSTOCK);
            itemPlay.jdbcSave();
        }

        String message = "成功下架" + successItemList.size() + "个宝贝";
        if (errorMap.size() > 0) {
            message += "，失败" + errorMap.size() + "个，点击确定后查看详情";
        }
        List<ItemPlay> errorItemList = ItemDao.findByNumIids(user.getId(), errorMap.keySet());
        List<BatchOpMessage> batchOpMsgList = new ArrayList<BatchOpMessage>();
        for (ItemPlay itemPlay : errorItemList) {
            BatchOpMessage batchOpMsg = new BatchOpMessage(itemPlay, "下架失败，" + errorMap.get(itemPlay.getNumIid()));
            batchOpMsgList.add(batchOpMsg);
        }

        renderSuccess(message, batchOpMsgList);
    }
    
	/**
	 * 全店上架
	 */
	public static void doShopListing() {
		User user = getUser();
		
		List<Long> numIidList = new ArrayList<Long>();
		
		List<ItemPlay> items = ItemDao.findByUserId(user.getId());
		for (ItemPlay item : items) {
			Long numIid = item.getNumIid();
			if(numIid == null || numIid <= 0) {
				continue;
			}
			numIidList.add(item.getNumIid());
		}
		
		if (numIidList == null || numIidList.isEmpty()) {
			renderError("暂无需要上架的宝贝");
		}

		List<Item> itemList = loadItems(user, numIidList);

		List<Long> successList = new ArrayList<Long>();
		Map<Long, String> errorMap = new HashMap<Long, String>();
		Map<Long, Long> newDelistTimeMap = new HashMap<Long, Long>();

		for (Item item : itemList) {
			//在架上的
			if (item.getApproveStatus().equals("onsale")) {
				successList.add(item.getNumIid());
				continue;
			}
			ItemListingApi.ItemUpdateListing listingApi = new ItemListingApi.ItemUpdateListing(user, item.getNumIid(),
					item.getNum());
			Item resItem = listingApi.call();
			boolean isSuccess = listingApi.isApiSuccess();
			if (isSuccess == true && resItem != null) {
				successList.add(item.getNumIid());
				if (resItem.getModified() != null) {
					newDelistTimeMap.put(item.getNumIid(), 
							resItem.getModified().getTime() + 7 * DateUtil.DAY_MILLIS);
				}
			} else {
				String errorMsg = listingApi.getErrorMsg();
				errorMap.put(item.getNumIid(), errorMsg);
			}
		}

		//更新itemPlay
		List<ItemPlay> successItemList = ItemDao.findByNumIids(user.getId(), successList);
		for (ItemPlay itemPlay : successItemList) {
			Long newDelistTime = newDelistTimeMap.get(itemPlay.getNumIid());
			if (newDelistTime != null) {
				itemPlay.setDeListTime(newDelistTime);
			}
			itemPlay.setStatus(Status.ONSALE);
			itemPlay.jdbcSave();
		}

		String message = "成功上架" + successItemList.size() + "个宝贝";
		if (errorMap.size() > 0) {
			message += "，失败" + errorMap.size() + "个，点击确定后查看详情";
		}
		List<ItemPlay> errorItemList = ItemDao.findByNumIids(user.getId(), errorMap.keySet());
		List<BatchOpMessage> batchOpMsgList = new ArrayList<BatchOpMessage>();
		for (ItemPlay itemPlay : errorItemList) {
			BatchOpMessage batchOpMsg = new BatchOpMessage(itemPlay, "上架失败，" + errorMap.get(itemPlay.getNumIid()));
			batchOpMsgList.add(batchOpMsg);
		}

		renderSuccess(message, batchOpMsgList);
	}
	
	/**
	 * 全店下架
	 */
	public static void doShopDeListing() {
		User user = getUser();
		
		List<Long> numIidList = new ArrayList<Long>();
		
		List<ItemPlay> items = ItemDao.findByUserId(user.getId());
		for (ItemPlay item : items) {
			Long numIid = item.getNumIid();
			if(numIid == null || numIid <= 0) {
				continue;
			}
			numIidList.add(item.getNumIid());
		}
		
		if (numIidList == null || numIidList.isEmpty()) {
			renderError("暂无需要下架的宝贝");
		}

		List<Item> itemList = loadItems(user, numIidList);

		List<Long> successList = new ArrayList<Long>();
		Map<Long, String> errorMap = new HashMap<Long, String>();
		Map<Long, Long> newDelistTimeMap = new HashMap<Long, Long>();

		for (Item item : itemList) {
			//在架下的
			if (item.getApproveStatus().equals("onsale") == false) {
				successList.add(item.getNumIid());
				continue;
			}
			ItemListingApi.ItemUpdateDelisting delistingApi = new ItemListingApi.ItemUpdateDelisting(user,
					item.getNumIid());
			Item resItem = delistingApi.call();
			boolean isSuccess = delistingApi.isApiSuccess();
			if (isSuccess == true && resItem != null) {
				successList.add(item.getNumIid());
				if (resItem.getModified() != null) {
					newDelistTimeMap.put(item.getNumIid(), resItem.getModified().getTime());
				}
				//errorList.add(item.getNumIid());//测试错误
			} else {
				String errorMsg = delistingApi.getErrorMsg();
				errorMap.put(item.getNumIid(), errorMsg);
			}
		}

		//更新itemPlay
		List<ItemPlay> successItemList = ItemDao.findByNumIids(user.getId(), successList);
		for (ItemPlay itemPlay : successItemList) {
			Long newDelistTime = newDelistTimeMap.get(itemPlay.getNumIid());
			if (newDelistTime != null) {
				itemPlay.setDeListTime(newDelistTime);
			}
			itemPlay.setStatus(Status.INSTOCK);
			itemPlay.jdbcSave();
		}

		String message = "成功下架" + successItemList.size() + "个宝贝";
		if (errorMap.size() > 0) {
			message += "，失败" + errorMap.size() + "个，点击确定后查看详情";
		}
		List<ItemPlay> errorItemList = ItemDao.findByNumIids(user.getId(), errorMap.keySet());
		List<BatchOpMessage> batchOpMsgList = new ArrayList<BatchOpMessage>();
		for (ItemPlay itemPlay : errorItemList) {
			BatchOpMessage batchOpMsg = new BatchOpMessage(itemPlay, "下架失败，" + errorMap.get(itemPlay.getNumIid()));
			batchOpMsgList.add(batchOpMsg);
		}

		renderSuccess(message, batchOpMsgList);
	}

    static void checkW2Expires(User user, int expireTime) {
        
        boolean isAuthorized = UmpPromotion.isHasAuthorized(expireTime);
        
        if (isAuthorized == false) {

            String authUrl = UmpPromotion.genReshouquanUrl();

            renderAuthorizedError("", authUrl);
        }
    }

    
    public static void checkAuth() {
        User user = getUser();
        final int expireTime = 180;

        checkW2Expires(user, expireTime);
    }

    /**
     * 批量改价
     * @param toCancelNumIid
     * @param newPriceStr
     */
    public static void doModifyPrice(List<Long> numIidList, String newPriceStr) {
        BigDecimal newPrice = new BigDecimal(0);
        try {
            newPrice = new BigDecimal(newPriceStr);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            renderError("请先输入正确的价格格式");
        }
        if (newPrice.compareTo(new BigDecimal(0)) <= 0) {
            renderError("请先输入正确的价格");
        }
        if (newPrice.compareTo(new BigDecimal(100000000)) > 0) {
            renderError("价格不得大于100000000元");
        }
        //转换成小数的两位
        newPrice = newPrice.multiply(new BigDecimal(100));
        int integerPrice = newPrice.intValue();
        newPrice = new BigDecimal(integerPrice).divide(new BigDecimal(100));

        User user = getUser();
        if (numIidList == null || numIidList.isEmpty()) {
            renderError("亲，请先选择要修改价格的宝贝");
        }

        //检查w2权限
        checkW2Expires(user, 60);

        List<Long> successList = new ArrayList<Long>();
        Map<Long, String> errorMap = new HashMap<Long, String>();

        for (Long numIid : numIidList) {
            ItemApi.ItemPriceUpdater updateApi = new ItemApi.ItemPriceUpdater(user.getSessionKey(), numIid,
                    newPrice.toString());
            updateApi.call();
            boolean isSuccess = updateApi.isApiSuccess();
            if (isSuccess == true) {
                successList.add(numIid);
                //errorList.add(numIid);//测试错误
            } else {
                String errorMsg = updateApi.getErrorMsg();
                errorMap.put(numIid, errorMsg);
            }

        }

        //更新itemPlay
        List<ItemPlay> successItemList = ItemDao.findByNumIids(user.getId(), successList);
        for (ItemPlay itemPlay : successItemList) {
            itemPlay.setPrice(newPrice.doubleValue());
            itemPlay.jdbcSave();
        }

        String message = "成功修改" + successItemList.size() + "个宝贝的价格";
        if (errorMap.size() > 0) {
            message += "，失败" + errorMap.size() + "个，点击确定后查看详情";
        }
        List<ItemPlay> errorItemList = ItemDao.findByNumIids(user.getId(), errorMap.keySet());
        List<BatchOpMessage> batchOpMsgList = new ArrayList<BatchOpMessage>();
        for (ItemPlay itemPlay : errorItemList) {
            BatchOpMessage batchOpMsg = new BatchOpMessage(itemPlay, "修改价格失败，" + errorMap.get(itemPlay.getNumIid()));
            batchOpMsgList.add(batchOpMsg);
        }

        renderSuccess(message, batchOpMsgList);
    }

    /**
     * 批量比例加价
     * @param toCancelNumIid
     * @param newPriceStr
     */
    public static void doModifyPriceByScale(List<Long> numIidList, String priceScaleStr) {
        BigDecimal priceScale = new BigDecimal(0);
        try {
            priceScale = new BigDecimal(priceScaleStr);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            renderError("请先输入正确的加价比例");
        }

        User user = getUser();
        if (numIidList == null || numIidList.isEmpty()) {
            renderError("亲，请先选择要修改价格的宝贝");
        }

        //检查w2权限
        checkW2Expires(user, 60);

        Map<Long, BigDecimal> successPriceMap = new HashMap<Long, BigDecimal>();
        Map<Long, String> errorMap = new HashMap<Long, String>();

        List<Item> itemList = loadItems(user, numIidList);

        for (Item item : itemList) {
            Long numIid = item.getNumIid();
            BigDecimal oldPrice = new BigDecimal(0);
            try {
                oldPrice = new BigDecimal(item.getPrice());
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                errorMap.put(numIid, "获取原价格失败");
                continue;
            }
            //Decimal
            BigDecimal multiTime = new BigDecimal(1);
            multiTime = multiTime.add(priceScale);
            BigDecimal newPrice = oldPrice.multiply(multiTime);

            String errorMsg = "";
            if (newPrice.compareTo(new BigDecimal(0)) <= 0) {
                errorMsg = "价格不得小于等于0";
            }
            if (newPrice.compareTo(new BigDecimal(100000000)) > 0) {
                errorMsg = "价格不得大于100000000元";
            }
            if (!StringUtils.isEmpty(errorMsg)) {
                errorMap.put(numIid, errorMsg);
                continue;
            }
            //转换成小数的两位
            newPrice = newPrice.multiply(new BigDecimal(100));
            int integerPrice = newPrice.intValue();
            newPrice = new BigDecimal(integerPrice).divide(new BigDecimal(100));

            ItemApi.ItemPriceUpdater updateApi = new ItemApi.ItemPriceUpdater(user.getSessionKey(), numIid,
                    newPrice.toString());
            updateApi.call();
            boolean isSuccess = updateApi.isApiSuccess();
            if (isSuccess == true) {
                successPriceMap.put(numIid, newPrice);
                //errorList.add(numIid);//测试错误
            } else {
                errorMsg = updateApi.getErrorMsg();
                errorMap.put(numIid, errorMsg);
            }

        }

        //更新itemPlay
        List<ItemPlay> successItemList = ItemDao.findByNumIids(user.getId(), successPriceMap.keySet());
        for (ItemPlay itemPlay : successItemList) {
            itemPlay.setPrice(successPriceMap.get(itemPlay.getNumIid()).doubleValue());
            itemPlay.jdbcSave();
        }

        String message = "成功修改" + successItemList.size() + "个宝贝的价格";
        if (errorMap.size() > 0) {
            message += "，失败" + errorMap.size() + "个，点击确定后查看详情";
        }
        List<ItemPlay> errorItemList = ItemDao.findByNumIids(user.getId(), errorMap.keySet());
        List<BatchOpMessage> batchOpMsgList = new ArrayList<BatchOpMessage>();
        for (ItemPlay itemPlay : errorItemList) {
            BatchOpMessage batchOpMsg = new BatchOpMessage(itemPlay, "修改价格失败，" + errorMap.get(itemPlay.getNumIid()));
            batchOpMsgList.add(batchOpMsg);
        }

        renderSuccess(message, batchOpMsgList);
    }

    private static List<Item> loadItems(User user, List<Long> numIidList) {
//        ItemApi.ItemsListGet getApi = new ItemApi.ItemsListGet(numIidList, false);
//        List<Item> itemList = getApi.call();
        MultiItemsListGet getApi = new MultiItemsListGet(user.getSessionKey(), numIidList, ItemApi.FIELDS);
        List<Item> itemList = getApi.call();
        if (getApi.isApiSuccess() == false) {
            renderError("亲，获取宝贝时出错，请联系我们。");
        }
        if (itemList == null || itemList.isEmpty()) {
            renderError("亲，找不到相应的宝贝，请联系我们。");
        }
        return itemList;
    }

    public static class BatchOpMessage {
        private ItemPlay itemPlay;

        private String message;

        public ItemPlay getItemPlay() {
            return itemPlay;
        }

        public void setItemPlay(ItemPlay itemPlay) {
            this.itemPlay = itemPlay;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public BatchOpMessage(ItemPlay itemPlay, String message) {
            super();
            this.itemPlay = itemPlay;
            this.message = message;
        }

    }

    public static void getRefreshResponse() {
        renderText(TaobaoUtil.refreshToken(getUser()));
    }
    
	// 批量交换图片位置
	public static void batchChangeImgPosition(List<Long> numIidList, Long i, Long j) {
		if(CommonUtils.isEmpty(numIidList)) {
			renderText("宝贝Id为空");
		}
		if(i == null || i <= 0L) {
			renderText("请传入正确的图片位置");
		}
		if(j == null || j <= 0L) {
			renderText("请传入正确的图片位置");
		}
		if(i == j) {
			renderText("同一张图片不需要调换");
		}
		i = i - 1;
		j = j - 1;
		User user = getUser();
		Map<Long, String> successMap = new HashMap<Long, String>();
		Map<Long, String> errorMap = new HashMap<Long, String>();
		
		for (Long numIid : numIidList) {
			Item item = new ItemApi.ItemImgsGet(user, numIid).call();
			if(item == null) {
				errorMap.put(numIid, "获取宝贝信息失败");
				continue;
			}
			if(CommonUtils.isEmpty(item.getItemImgs())) {
				errorMap.put(numIid, "获取宝贝图片信息失败");
				continue;
			}
			List<ItemImg> imgs = item.getItemImgs();
			int count = imgs.size();
			if(i >= count || j >= count) {
				errorMap.put(numIid, "当前宝贝图片数量小于要调换的图片位置下标");
				continue;
			}
			ItemImg img_ = new ItemImg();
			ItemImg img_i = new ItemImg();
			ItemImg img_j = new ItemImg();
			for(ItemImg img : imgs) {
				if(img.getPosition() == i) {
					img_i = img;
				} else if(img.getPosition() == j) {
					img_j = img;
				}
			}
			// 更新第i张图片
			img_.setId(img_j.getId());
			img_.setPosition(img_j.getPosition());
			img_.setUrl(img_i.getUrl());
			FileItem fItem_i = ItemApi.fetchUrl(img_.getUrl(), img_.getId() + "_" + img_.getPosition());
			if(fItem_i == null){
				errorMap.put(numIid, "存在无效图片，请检查");
				continue;
			}
			log.info("[f item_i:]" + fItem_i.getFileName());
			ItemImg img_i_result = new ItemApi.ItemImgPictureUpdate(user, numIid, img_, fItem_i).call();
			if(img_i_result == null) {
				errorMap.put(numIid, "更新宝贝第" + i + "张图片时出错");
				continue;
			}
			// 更新第j张图片
			img_.setId(img_i.getId());
			img_.setPosition(img_i.getPosition());
			img_.setUrl(img_j.getUrl());
			FileItem fItem_j = ItemApi.fetchUrl(img_.getUrl(), img_.getId() + "_" + img_.getPosition());
			if(fItem_j == null){
				errorMap.put(numIid, "存在无效图片，请检查");
				continue;
			}
			log.info("[f item_j:]" + fItem_j.getFileName());
			ItemImg img_j_result = new ItemApi.ItemImgPictureUpdate(user, numIid, img_, fItem_j).call();
			if(img_j_result == null) {
				errorMap.put(numIid, "更新宝贝第" + j + "张图片时出错");
				continue;
			}
			successMap.put(numIid, "操作成功");
		}
		String message = "成功交换" + successMap.size() + "个宝贝的图片位置";
		if (errorMap.size() > 0) {
			message += "，失败" + errorMap.size() + "个，以下为失败详情：<br />";
			for (Long numIid : errorMap.keySet()) {
				message += "[" + numIid + ": " + errorMap.get(numIid) + "]<br />";
			}
		}
		renderText(message);
	}
	
}
