
package controllers;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import job.dazhe.UpdateSalesCountJob;
import models.item.ItemPlay;
import models.promotion.EveryDaySalesCount;
import models.promotion.Promotion;
import models.promotion.TMProActivity;
import models.promotion.TMProActivity.ActivityStatus;
import models.ump.PromotionPlay;
import models.ump.PromotionPlay.ItemPromoteType;
import models.user.User;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.mvc.Before;
import result.TMResult;
import tbapi.ump.UMPApi;
import tbapi.ump.UMPApi.MjsParams;
import tbapi.ump.UMPApi.UmpMjsActivityAdd;
import tbapi.ump.UMPApi.UmpMjsActivityUpdate;
import utils.TaobaoUtil;
import actions.promotion.PromotionAction;
import actions.promotion.PromotionAction.ItemOpStatus;
import actions.ump.UmpMjsAction;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.HtmlUtil;
import com.ciaosir.client.utils.JsonUtil;
import com.taobao.api.ApiException;
import com.taobao.api.domain.ItemPromotion;

import configs.TMConfigs;
import dao.item.ItemDao;
import dao.ump.PromotionDao;

/**
 * https://oauth.taobao.com/authorize?response_type=code&client_id=12266732&redirect_uri=http://223.4.51.164/in/login
 * https://oauth.taobao.com/authorize?redirect_uri=http%3A%2F%2F223.4.51.164%2Fin%2Flogin&client_id=12266732&response_type=code&timestamp=2013-08-11+04%3A19%3A39&sign=GDYBj14apb%2BWMM2cwNgvXw%3D%3D
 *  Now, it's going to warn...........
 *  这个类是所有打折活动的源泉 --   打折减钱 ，  满减、满包邮
 *
 */
public class TaoDiscount extends TMController {

	public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
    private static final Logger log = LoggerFactory.getLogger(TaoDiscount.class);

    public static final String TAG = "TaoDiscount";

    public static final String DOMAIN = "http://dazhe.taovgo.com/";

    public static final String TESTDOMAIN = "http://dazhe.taovgo.com/";
    
    
    private static final boolean IsNewPromotion = false;

    @Before
    public static void logAppKey() {
        log.info("appkey=" + TMConfigs.App.APP_KEY + " " + "appsecret="
                + TMConfigs.App.APP_SECRET);
    }

    public static void index() {

//    	User user=getUser();
//    	TaobaoUtil.refreshToken(user);

        render("tdazhe/index.html");
    }

    public static void itemSelect(String activityString) {
        render("tdazhe/itemSelect.html", activityString);
    }

    public static void revise_item(String activityId) {
        render("tdazhe/reviseItem.html", activityId);
    }

    public static void add_Item(String activityId) {
        render("tdazhe/addItem.html", activityId);
    }

    public static void revise_Act(String activityId) {
        render("tdazhe/reviseAct.html", activityId);
    }

    public static void addSuccess() {
        render("tdazhe/addsuccess.html");
    }

    public static void item_alladd() {
        render("tdazhe/itemalladd.html");
    }

    public static void award() {
        render("tdazhe/award.html");
    }

    public static void auto_relation() {
        render("tdazhe/dz_relationOp.html");
    }

    public static void waterMarker() {
        render("tdazhe/dz_waterMarker.html");
    }

    public static void mjsAddActivity() {
        render("ump/mjsAddActivity.html");
    }

    public static void mjsRestartActivity(Long activityId) {
    	render("ump/restartMjsActivity.html", activityId);
    }
    
    public static void mjsRestartShopActivity(Long activityId) {
    	render("ump/restartMjsShopActivity.html", activityId);
    }
    public static void mjsItemSelect(Long activityId) {
        render("ump/mjsItemSelect.html", activityId);
    }

    //---------做重新授权------------------------------------
    protected static boolean checkToken() {
        User user = getUser();

        JsonNode refreshObj = TaobaoUtil.refreshTokenAndGetJsonObject(user);

        if (refreshObj == null) {
//            return true;//refreshtoken的次数用完，全部最后判断
            return false;//refreshtoken的次数用完，全部最后判断            
        }

        Long w2_expires_in = Long.parseLong(refreshObj.findValue("w2_expires_in").getTextValue());

        if (w2_expires_in < 300) {

            return false;
//        In.forwardToOAuth(state, DOMAIN + "In/Tdiscount");
        }
        return true;

    }

    public static void reShouquan() {
        String state = TESTDOMAIN + "taodiscount/index";
        In.forwardToOAuth(state, DOMAIN + "In/Tdiscount");
    }
    
    
    // 添加活动
    public static void activity_add() {

        //boolean success = checkToken();
        boolean isAuthorized = UmpPromotion.isHasAuthorized(300);
        
        if (isAuthorized == false) {
            String state = HtmlUtil.domain + "/taodiscount/activity_add";
            In.forwardToOAuth(state, HtmlUtil.domain + "/In/Tdiscount");
        }

        render("tdazhe/activity_add.html");
    }

    //展示商品列表
    public static void searchItems(String title, String cid, String sellerCid, String order, String isDis, int pn,
            int ps) {
        
        final int itemStatus = 1;
        
        User user = getUser();
        PageOffset po = new PageOffset(pn, ps);

        List<ItemPlay> items = ItemDao.findItemByConditionAndOrder(user.getId(), title, cid, sellerCid, order, isDis,
                po, IsNewPromotion, itemStatus);
        long count = ItemDao.countItemByConditionAndOrder(user.getId(), title, cid, sellerCid, 
                isDis, IsNewPromotion, itemStatus);

        if (CommonUtils.isEmpty(items)) {
            renderJSON(new TMResult("亲， 您还没有上架宝贝哟！！！！！"));
        }

        List<Item_Activity> item_acts = new ArrayList<TaoDiscount.Item_Activity>();

        for (ItemPlay item : items) {
            Item_Activity item_act = new Item_Activity();
            long test = PromotionAction.countBynumiid(item.getNumIid());
            if (test != 0) {
                Promotion promotion = PromotionAction.selectBynumiid(item.getNumIid());
                item_act.numiid = item.getNumIid();
                item_act.title = item.getTitle();
                item_act.picURL = item.getPicURL();
                item_act.price = item.getPrice();
                item_act.promotionId = promotion.getId();
                if (StringUtils.equals(promotion.getDiscountType(), "DISCOUNT")) {
                    item_act.discountType = 0;
                }
                else {
                    item_act.discountType = 1;
                }
                item_act.discountValue = promotion.getDiscountValue();

            }
            else {
                PromotionPlay promotion = PromotionDao.findFirstActivePromotionByNumIid(user.getId(), 
                        item.getNumIid());
                
                item_act.numiid = item.getNumIid();
                item_act.title = item.getTitle();
                item_act.picURL = item.getPicURL();
                item_act.price = item.getPrice();
                if (promotion != null) {
                    if (ItemPromoteType.discount.equals(promotion.getPromotionType())) {
                        item_act.discountType = 0;
                        item_act.discountValue = (double) promotion.getDiscountRate() / 100 + "";
                    } else {
                        item_act.discountType = 1;
                        item_act.discountValue = (double) promotion.getDecreaseAmount() / 100 + "";
                    }
                } else {
                    item_act.discountValue = "0";//表示该商品没有参加打折活动
                }
                
            }
            if (item_act != null) {
                item_acts.add(item_act);
            }

        }

        TMResult tmResult = new TMResult(item_acts, (int) count, po);
        renderJSON(tmResult);

    }

    //展示商品列表
    public static void searchMjsItems(String title, String cid, String sellerCid, String order, String isDis, int pn,
            int ps, Long tmProActivityId) {
        
        final int itemStatus = 1;
        
        User user = getUser();
        PageOffset po = new PageOffset(pn, ps);

        List<ItemPlay> items = ItemDao.findItemByConditionAndOrder(user.getId(), title, cid, sellerCid, order, isDis,
                po, IsNewPromotion, itemStatus);
        long count = ItemDao.countItemByConditionAndOrder(user.getId(), title, cid, sellerCid, 
                isDis, IsNewPromotion, itemStatus);

        if (CommonUtils.isEmpty(items)) {
            new TMResult(new ArrayList<TaoDiscount.Item_Activity>(), 0, po);
        }

        TMProActivity activity = TMProActivity.findByActivityId(user.getId(), tmProActivityId);
        
        List<Item_Activity> item_acts = new ArrayList<TaoDiscount.Item_Activity>();

        for (ItemPlay item : items) {
            Item_Activity item_act = new Item_Activity();
            long test = PromotionAction.countBynumiid(item.getNumIid());
            if (test != 0) {
                Promotion promotion = PromotionAction.selectBynumiid(item.getNumIid());
                item_act.numiid = item.getNumIid();
                item_act.title = item.getTitle();
                item_act.picURL = item.getPicURL();
                item_act.price = item.getPrice();
                item_act.promotionId = promotion.getId();
                if (StringUtils.equals(promotion.getDiscountType(), "DISCOUNT")) {
                    item_act.discountType = 0;
                }
                else {
                    item_act.discountType = 1;
                }
                item_act.discountValue = promotion.getDiscountValue();

            }
            else {
                item_act.numiid = item.getNumIid();
                item_act.title = item.getTitle();
                item_act.picURL = item.getPicURL();
                item_act.price = item.getPrice();
                item_act.discountValue = "0";//表示该商品没有参加打折活动
            }
            if(activity != null) {
            	if(!StringUtils.isEmpty(activity.getItems())) {
            		if(activity.getItems().indexOf(item.getNumIid().toString()) >= 0) {
            			item_act.setIsthisActivity(true);
            		}
            	}
            	
            }
            if (item_act != null) {
                item_acts.add(item_act);
            }

        }

        TMResult tmResult = new TMResult(item_acts, (int) count, po);
        renderJSON(tmResult);

    }

    //展示商品列表
    public static void searchMjsItemsWithParams(String title, String cid, String sellerCid, String order,
    		String isDis, String isOnsale, int pn, int ps, Long tmProActivityId) {
        User user = getUser();
        PageOffset po = new PageOffset(pn, ps);

        String itemsInMjs = "";
        TMProActivity activity = TMProActivity.findByActivityId(user.getId(), tmProActivityId);
        if(activity != null) {
        	itemsInMjs = activity.getItems();
        }
        List<ItemPlay> items = ItemDao.findMjsItemByConditionAndOrder(user.getId(), title, cid, sellerCid, 
        		order, isDis, isOnsale, po, itemsInMjs);
        long count = ItemDao.countMjsItemByConditionAndOrder(user.getId(), title, cid, sellerCid, 
                isDis, isOnsale, itemsInMjs);

        if (CommonUtils.isEmpty(items)) {
        	renderJSON(new TMResult(new ArrayList<TaoDiscount.Item_Activity>(), 0, po));
        }

        
        List<Item_Activity> item_acts = new ArrayList<TaoDiscount.Item_Activity>();

        for (ItemPlay item : items) {
            Item_Activity item_act = new Item_Activity();
            long test = PromotionAction.countBynumiid(item.getNumIid());
            if (test != 0) {
                Promotion promotion = PromotionAction.selectBynumiid(item.getNumIid());
                item_act.numiid = item.getNumIid();
                item_act.title = item.getTitle();
                item_act.picURL = item.getPicURL();
                item_act.price = item.getPrice();
                item_act.promotionId = promotion.getId();
                if (StringUtils.equals(promotion.getDiscountType(), "DISCOUNT")) {
                    item_act.discountType = 0;
                }
                else {
                    item_act.discountType = 1;
                }
                item_act.discountValue = promotion.getDiscountValue();

            }
            else {
                item_act.numiid = item.getNumIid();
                item_act.title = item.getTitle();
                item_act.picURL = item.getPicURL();
                item_act.price = item.getPrice();
                item_act.discountValue = "0";//表示该商品没有参加打折活动
            }
            if(activity != null) {
            	if(!StringUtils.isEmpty(activity.getItems())) {
            		if(activity.getItems().indexOf(item.getNumIid().toString()) >= 0) {
            			item_act.setIsthisActivity(true);
            		}
            	}
            	
            }
            if (item_act != null) {
                item_acts.add(item_act);
            }

        }
        TMResult tmResult = new TMResult(item_acts, (int) count, po);
        renderJSON(JsonUtil.getJson(tmResult));
        

    }
    
    public static void searchReviseItems(Long id, String title, String cid, String sellerCid, String order, int pn,
            int ps) {

        User user = getUser();

        PageOffset po = new PageOffset(pn, ps);

        TMProActivity activity = TMProActivity.findByActivityId(user.getId(), id);
        List<ItemPlay> itemList = ItemDao.findPromotionByConditionAndOrder(user.getId(), id, title, cid, sellerCid,
                order, po);

        if (CommonUtils.isEmpty(itemList)) {
            renderJSON(new TMResult("亲， 没有相应宝贝哟！！！！！"));
        }

        //新类型打折的搜索
        if (StringUtils.isEmpty(activity.getItems())) {
            long count = ItemDao.countPromotionByConditionAndOrder(user.getId(), id, title, cid, sellerCid);
            List<Item_Activity> item_acts = new ArrayList<TaoDiscount.Item_Activity>();

            for (ItemPlay item : itemList) {
                Promotion promotion = PromotionAction.selectBynumiid(item.getNumIid());
                if (promotion == null) {
                    continue;
                }
                Item_Activity item_act = new Item_Activity();

                item_act.numiid = item.getNumIid();
                item_act.title = item.getTitle();
                item_act.picURL = item.getPicURL();
                item_act.price = item.getPrice();
                item_act.promotionId = promotion.getId();
                if (StringUtils.equals(promotion.getDiscountType(), "DISCOUNT")) {
                    item_act.discountType = 0;
                }
                else {
                    item_act.discountType = 1;
                }
                item_act.discountValue = promotion.getDiscountValue();

                item_acts.add(item_act);

            }

            TMResult tmResult = new TMResult(item_acts, (int) count, po);
            renderJSON(tmResult);
        }

        String[] numIids = StringUtils.split(activity.getItems(), ",");

        String newitems = null;

        for (String numIid : numIids) {
            for (ItemPlay item : itemList) {
                String itemIdString = String.valueOf(item.getNumIid());
                if (StringUtils.equals(numIid, itemIdString)) {

                    if (StringUtils.isEmpty(newitems)) {
                        newitems = numIid;
                    }
                    else {
                        newitems += "," + numIid;
                    }
                }
            }
        }

        if (newitems == null)
            return;

        List<Promotion> promotionList = PromotionAction.findPromotionByNumIids(user.getId(), newitems, po);

        long count = PromotionAction.countPromotionList(user.getId(), newitems);

        List<Item_Activity> item_acts = new ArrayList<TaoDiscount.Item_Activity>();

        if (CommonUtils.isEmpty(promotionList)) {
            log.error("promotionList is empty,check numIids~~~");
            return;
        }

        log.info("start to select items in the activity.....");
        for (Promotion promotion : promotionList) {
            Item_Activity item_act = new Item_Activity();

            ItemPlay item = ItemDao.findByNumIid(user.getId(), promotion.getNumIid());
            if (item == null) {
                continue;
            }

            item_act.numiid = item.getNumIid();
            item_act.title = item.getTitle();
            item_act.picURL = item.getPicURL();
            item_act.price = item.getPrice();
            item_act.promotionId = promotion.getId();
            if (StringUtils.equals(promotion.getDiscountType(), "DISCOUNT")) {
                item_act.discountType = 0;
            }
            else {
                item_act.discountType = 1;
            }
            item_act.discountValue = promotion.getDiscountValue();

            item_acts.add(item_act);

        }

        TMResult tmResult = new TMResult(item_acts, (int) count, po);
        renderJSON(tmResult);

    }

    public static void searchAddItems(Long activityId, String title, String cid, String sellerCid, String order,
            String isDis, int pn, int ps) {
        
        final int itemStatus = 1;
        
        User user = getUser();

        PageOffset po = new PageOffset(pn, ps);
//        log.info("searchAddItemsTime : " +System.currentTimeMillis()+"11111111111111111111111111111111111111");
        List<ItemPlay> items = ItemDao.findItemByConditionAndOrder(user.getId(), title, cid, sellerCid, order, isDis,
                po, IsNewPromotion, itemStatus);
        long count = ItemDao.countItemByConditionAndOrder(user.getId(), title, cid, sellerCid, 
                isDis, IsNewPromotion, itemStatus);

        if (CommonUtils.isEmpty(items)) {
            renderJSON(new TMResult(items));
        }

        TMProActivity activity = TMProActivity.findByActivityId(user.getId(), activityId);

        Long decreaseNum = 2L;
        int discountType = 2;

        /*******************************************新类型的打折**************************************************/
        if (StringUtils.isEmpty(activity.getItems())) {

            Promotion profordes = PromotionAction.findPromotionByActivityIdLimit1(activityId);
            if (profordes == null) {
                renderJSON("该活动没有商品，请删除");
            }
            List<Item_Activity> item_acts = new ArrayList<TaoDiscount.Item_Activity>();
            decreaseNum = profordes.getDecreaseNum();

            if (StringUtils.equals(profordes.getDiscountType(), "DISCOUNT")) {
                discountType = 0;
            } else {
                discountType = 1;
            }
//            
//            log.info("searchAddItemsTime : " +System.currentTimeMillis()+"2222222222222222222222222222222222222");

            for (ItemPlay item : items) {
                Item_Activity item_act = new Item_Activity();
                long test = PromotionAction.countBynumiid(item.getNumIid());
                if (test != 0) {
                    Promotion promotion = PromotionAction.selectBynumiid(item.getNumIid());
                    item_act.numiid = item.getNumIid();
                    item_act.title = item.getTitle();
                    item_act.picURL = item.getPicURL();
                    item_act.price = item.getPrice();
                    item_act.promotionId = promotion.getId();
                    if (StringUtils.equals(promotion.getDiscountType(), "DISCOUNT")) {
                        item_act.discountType = 0;
                    }
                    else {
                        item_act.discountType = 1;
                    }
                    item_act.discountValue = promotion.getDiscountValue();

                    item_act.decreaseNum = promotion.getDecreaseNum();

                    if (promotion.getActivityId().longValue() == activityId.longValue()) {
                        item_act.isthisActivity = true;
                    }

                }
                else {
                    PromotionPlay promotion = PromotionDao.findFirstActivePromotionByNumIid(user.getId(), 
                            item.getNumIid());
                    
                    item_act.numiid = item.getNumIid();
                    item_act.title = item.getTitle();
                    item_act.picURL = item.getPicURL();
                    item_act.price = item.getPrice();
                    if (promotion != null) {
                        if (ItemPromoteType.discount.equals(promotion.getPromotionType())) {
                            item_act.discountType = 0;
                            item_act.discountValue = (double) promotion.getDiscountRate() / 100 + "";
                        } else {
                            item_act.discountType = 1;
                            item_act.discountValue = (double) promotion.getDecreaseAmount() / 100 + "";
                        }
                        item_act.decreaseNum = decreaseNum;
                    } else {
                        item_act.discountValue = "0";//表示该商品没有参加打折活动
                        item_act.decreaseNum = decreaseNum;
                        item_act.discountType = discountType;
                    }
                    
                }
                if (item_act != null) {
                    item_acts.add(item_act);
                }

            }
//            log.info("searchAddItemsTime : " +System.currentTimeMillis()+"3333333333333333333333333333333333333333333333333");
            TMResult tmResult = new TMResult(item_acts, (int) count, po);
            renderJSON(tmResult);
        }

        else {
            String[] numiids = StringUtils.split(activity.getItems(), ",");

            if (numiids == null) {
                renderJSON("该活动没有商品，请删除");
            }

            List<Item_Activity> item_acts = new ArrayList<TaoDiscount.Item_Activity>();

            Promotion profordes = PromotionAction.findPromotionByNumIid(user.getId(), Long.valueOf(numiids[0]));

            decreaseNum = profordes.getDecreaseNum();

            if (StringUtils.equals(profordes.getDiscountType(), "DISCOUNT")) {
                discountType = 0;
            }
            else {
                discountType = 1;
            }

            for (ItemPlay item : items) {
                Item_Activity item_act = new Item_Activity();
                long test = PromotionAction.countBynumiid(item.getNumIid());
                if (test != 0) {
                    Promotion promotion = PromotionAction.selectBynumiid(item.getNumIid());
                    item_act.numiid = item.getNumIid();
                    item_act.title = item.getTitle();
                    item_act.picURL = item.getPicURL();
                    item_act.price = item.getPrice();
                    item_act.promotionId = promotion.getId();
                    if (StringUtils.equals(promotion.getDiscountType(), "DISCOUNT")) {
                        item_act.discountType = 0;
                    }
                    else {
                        item_act.discountType = 1;
                    }
                    item_act.discountValue = promotion.getDiscountValue();

                    item_act.decreaseNum = promotion.getDecreaseNum();

                    String itemNumiid = String.valueOf(item.getNumIid());

                    for (String numiid : numiids) {
                        if (StringUtils.equals(numiid, itemNumiid)) {
                            item_act.isthisActivity = true;
                        }

                    }

                }
                else {
                    item_act.numiid = item.getNumIid();
                    item_act.title = item.getTitle();
                    item_act.picURL = item.getPicURL();
                    item_act.price = item.getPrice();
                    item_act.discountValue = "0";//表示该商品没有参加打折活动
                    item_act.decreaseNum = decreaseNum;
                    item_act.discountType = discountType;
                }
                if (item_act != null) {
                    item_acts.add(item_act);
                }

            }

            TMResult tmResult = new TMResult(item_acts, (int) count, po);
            renderJSON(tmResult);
        }

    }

    public static class Item_Activity {

        public long numiid;

        public String title;

        public String picURL;

        public double price;

        public long promotionId;

        public int discountType;

        public String discountValue;

        public Long decreaseNum;

        public boolean isthisActivity = false;

        public String order;

        public long getNumiid() {
            return numiid;
        }

        public void setNumiid(long numiid) {
            this.numiid = numiid;
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

        public int getDiscountType() {
            return discountType;
        }

        public void setDiscountType(int discountType) {
            this.discountType = discountType;
        }

        public String getDiscountValue() {
            return discountValue;
        }

        public void setDiscountValue(String discountValue) {
            this.discountValue = discountValue;
        }

        public Long getDecreaseNum() {
            return decreaseNum;
        }

        public void setDecreaseNum(Long decreaseNum) {
            this.decreaseNum = decreaseNum;
        }

        public boolean isIsthisActivity() {
            return isthisActivity;
        }

        public void setIsthisActivity(boolean isthisActivity) {
            this.isthisActivity = isthisActivity;
        }

        public String getOrder() {
            return order;
        }

        public void setOrder(String order) {
            this.order = order;
        }

        
        
    }
    
// 添加活动接口
//    public static void addActivity(String title, String description,
//            String startTimeStr, String endTimeStr, String activityType,
//            String discountValue, String decreaseNum, String items) {
    public static void addActivity(String itemString, int distype) {

        User user = getUser();
        if (StringUtils.isEmpty(itemString))
            return;
        String[] idStrings = StringUtils.split(itemString, "!");

        if (ArrayUtils.isEmpty(idStrings)) {
            ok();
        }
        String[] item_first = StringUtils.split(idStrings[0], ",");

        Long createTime = CommonUtils.Date2long(new Date());
        String status = "ACTIVE";
        
        long startTime = 0;
        long endTime = 0;
        try {
            startTime = sdf.parse(item_first[1]).getTime();
            endTime = sdf.parse(item_first[2]).getTime();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            renderJSON(new TMResult("活动时间错误，请联系我们！"));
        }

        TMProActivity activity = new TMProActivity(user.getId(), item_first[3], item_first[0], createTime,
                startTime, endTime, status);

        boolean success3 = activity.jdbcSave();

        List<TMProActivity> activityList = TMProActivity.findOnActiveActivitys(user.getId());

        if (!success3) {
            renderJSON(new TMResult("创建活动失败，请重新创建！"));
        }

        List<ErrorItems> errorItemList = new ArrayList<TaoDiscount.ErrorItems>();

        for (String idString : idStrings) {
            String[] item = StringUtils.split(idString, ",");
            String description = item[0];
            String title = item[3];
            int type = Integer.valueOf(item[8]).intValue();
            Long decreaseNum = Long.valueOf(item[5]);
            String promotionType = "";
            if (type == 0) {
                promotionType = "DISCOUNT";
            }
            else {
                promotionType = "PRICE";
            }
            String items = item[6];
            String discountValue = item[7];
            Long userTagId = 1L;

            if (promotionType == ""
                    || (decreaseNum != 0L && decreaseNum != 1L)) {
                log.error("param error," + promotionType + "param error" + decreaseNum);

                renderJSON(TMResult.failMsg("对不起，输入参数不正确!"));

            } else {
                log.info("start add activity......");
                ItemOpStatus opStatus = PromotionAction.addActivity(user.getId(), title,
                        description, createTime, startTime, endTime, promotionType,
                        discountValue, decreaseNum, userTagId, items, activity.getId());

                if (opStatus == null) {
                    continue;
                }

                String W2 = "W2";
                if (StringUtils.equals(opStatus.getOpMsg(), W2)) {
                    renderJSON(JsonUtil.getJson(new TMResult("0")));
                }
                else if (StringUtils.equals(opStatus.getOpMsg(), "applimit")) {
                    renderJSON(JsonUtil.getJson(new TMResult("-1")));
                }
                else if (opStatus.getOpMsg().contains("商品折扣幅度不能低于设置的店铺最低折扣")) {
                    String limitDis = opStatus.getOpMsg().substring(19, 23);
                    renderJSON(JsonUtil.getJson(new TMResult(limitDis)));
                }
                ErrorItems errorItem = new ErrorItems(opStatus, ItemDao.findByNumIid(user.getId(), Long.valueOf(items)));
                errorItemList.add(errorItem);
            }
        }

        renderJSON(JsonUtil.getJson(new TMResult(errorItemList)));

    }
    
    public static void listActivity(int isactive, int pn, int ps) throws IOException {
        /*if(isactive == 1) {
        	renderMockFileInJsonIfDev("listactivity.json");
        }*/
    	
        User user = getUser();

        PageOffset po = new PageOffset(pn, ps);
		try {
	        List<TMProActivity> activity = TMProActivity.findOnActiveActivitys(user.getId());
	        if(user.getUserNick().equals("森易格旗舰店")) {
	        	log.error("1");
	        }
	        long nowTime = CommonUtils.Date2long(new Date());
	
	        for (TMProActivity act : activity) {
	        	 if(user.getUserNick().equals("for")) {
	 	        	log.error("1");
	 	        }
	            //旧的活动
	            if (act.isOldActivity() == true) {
	            	 if(user.getUserNick().equals("森易格旗舰店")) {
	     	        	log.error("old");
	     	        }
	                if (StringUtils.isEmpty(act.getItems())) {
	
	                    long test = PromotionAction.countPromotionByActivityId(user.getId(), act.getId());
	                    if(user.getUserNick().equals("森易格旗舰店")) {
	        	        	log.error("2");
	        	        }
	                    if (test == 0) {
	                        /*
	                         * 没有宝贝就直接删除活动。。。。
	                        TMProActivity.deleteActivityById(user.getId(), act.getId());
	                        continue;
	                        */
	                        act.setActivityEndTime(System.currentTimeMillis());
	                        act.setStatus("UNACTIVE");
	                        act.jdbcSave();
	                        continue;
	                    }
	                    if(user.getUserNick().equals("森易格旗舰店")) {
	        	        	log.error("3");
	        	        }
	                    long endTime = act.getActivityEndTime();
	                    if (endTime <= nowTime) {
	
	                        boolean isdeleteAct = true;
	                        List<Promotion> promotionList = PromotionAction
	                                .findPromotionByActivityId(act.getId());
	                        if(user.getUserNick().equals("森易格旗舰店")) {
	            	        	log.error("4");
	            	        }
	                        if (!CommonUtils.isEmpty(promotionList)) {
	                            for (Promotion promotion : promotionList) {
	                                try {
	                                    boolean success = PromotionAction
	                                            .deletePromotion(user.sessionKey,
	                                                    promotion.getId());
	                                    if (success != true) {
	                                        isdeleteAct = false;
	                                    }
	                                } catch (ApiException e) {
	                                    log.warn(e.getMessage());
	                                }
	                            }
	                        }
	                        if(user.getUserNick().equals("森易格旗舰店")) {
	            	        	log.error("5");
	            	        }
	                        if (isdeleteAct == true) {
	                        	 if(user.getUserNick().equals("森易格旗舰店")) {
	                 	        	log.error("6");
	                 	        }
	                            act.setStatus("UNACTIVE");
	                            act.jdbcSave();
	                        }
	                    }
	                }
	            } else {
	            	 if(user.getUserNick().equals("森易格旗舰店")) {
	     	        	log.error("7");
	     	        }
	                long endTime = act.getActivityEndTime();
	                if (endTime <= nowTime) {
	                    
	                    if (act.isDiscountActivity() || act.isNewDiscountActivity()) {
	                        PromotionDao.unActiveActivityPromotions(user.getId(), act.getTMActivityId());
	                    } else if(act.isMjsActivity()){
	                    	UmpMjsAction.removeItemsMjsTmpl(user, act.getItems(), act.getId());
	                    } else if(act.isShopMjsActivity()) {
	                    	UmpMjsAction.removeItemsMjsTmpl(user, ItemDao.findNumIidWithUser(user.getId()), act.getId());
	                    }
	                    act.setStatus(ActivityStatus.UNACTIVE);
	                    act.jdbcSave();
	                    
	                }
	            }
	            
	
	        }
	        if(user.getUserNick().equals("森易格旗舰店")) {
	        	log.error("8");
	        }
	        if (isactive == 1) {
	            List<TMProActivity> act_activity = TMProActivity.findOnActiveActivitys(user.getId());
	            long count = TMProActivity.countOnActiveActivity(user.getId());
	            TMResult tmResult = new TMResult(act_activity, (int) count, po);
	            renderJSON(JsonUtil.getJson(tmResult));
	        }
	        else {
	        	 if(user.getUserNick().equals("森易格旗舰店")) {
	 	        	log.error("9");
	 	        }
	            List<TMProActivity> unact_activity = TMProActivity.findUnActiveActivitys(user.getId());
	            if(user.getUserNick().equals("森易格旗舰店")) {
		        	log.error("10");
		        }
	            long count = TMProActivity.countUnActiveActivity(user.getId());
	            if(user.getUserNick().equals("森易格旗舰店")) {
		        	log.error("11");
		        }
	            TMResult tmResult = new TMResult(unact_activity, (int) count, po);
	            renderJSON(JsonUtil.getJson(tmResult));
	        }
    	} catch (Exception e) {
    		log.error(e.getMessage());
    		 if(user.getUserNick().equals("森易格旗舰店")) {
 	        	log.error("12");
 	        }
    		renderJSON(JsonUtil.getJson(new TMResult(new ArrayList<TMProActivity>(), 0, po)));
		}
    }

    public static void deleteActivity(Long id) {
        User user = getUser();

        TMProActivity activity = TMProActivity.findByActivityId(user.getId(), id);

        if (StringUtils.isEmpty(activity.getItems())) {
            boolean isdeleteAct = true;
            List<Promotion> promotionList = PromotionAction.findPromotionByActivityId(activity.getId());
            if (!CommonUtils.isEmpty(promotionList)) {
                for (Promotion promotion : promotionList) {
                    try {
                        boolean success = PromotionAction.deletePromotion(user.sessionKey, promotion.getId());
                        if (success != true) {
                            isdeleteAct = false;
                        }
                    } catch (ApiException e) {
                        log.warn(e.getMessage());
                    }
                }
            }
            if (isdeleteAct == true) {
                activity.setStatus("UNACTIVE");
                activity.jdbcSave();
            }
            return;
        }

        String[] items = StringUtils.split(activity.getItems(), ",");

        log.info("start to delete activity........");
        //把每个Activity中的Promotion逐个删除
        for (String item : items) {
            Long numIid = CommonUtils.String2Long(item);
            //删除淘宝数据库
            Promotion promotion = PromotionAction.findPromotionByNumIid(user.getId(), numIid);
            if (promotion == null) {
                continue;
            }

            log.info("start to delete promotion........");
            try {
                boolean success2 = PromotionAction.deletePromotion(user.sessionKey, promotion.getId());
                if (!success2) {
                    renderJSON(new TMResult("delete flase"));
                }
            } catch (ApiException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        //最后删除 整个Activity
        TMProActivity.deleteActivityById(user.getId(), id);
    }

    public static void deleteUnactive(Long id) {
        User user = getUser();
        TMProActivity.deleteActivityById(user.getId(), id);
    }

    public static void reviseItem(String subString) {
//    	Long promotionId,String discountValue

        User user = getUser();

        if (StringUtils.isEmpty(subString))
            return;
        String[] idStrings = StringUtils.split(subString, "!");

        if (ArrayUtils.isEmpty(idStrings)) {
            ok();
        }

        List<ErrorItems> errorItemList = new ArrayList<TaoDiscount.ErrorItems>();

        for (String idString : idStrings) {
            String[] item = StringUtils.split(idString, ",");
            Long promotionId = Long.valueOf(item[0]);
            String discountValue = item[1];
            Promotion promotion = PromotionAction.findPromotionById(promotionId);

            Long activityId = promotion.getActivityId();
            String discountType = "DISCOUNT";
            if (StringUtils.equals(item[2], "0")) {
                discountType = "DISCOUNT";
            }
            else {
                discountType = "PRICE";
            }
            Date startDate = new Date(promotion.getStartDate());
            Date endDate = new Date(promotion.getEndDate());
            Long decreaseNum = promotion.getDecreaseNum();

            Long tagId = promotion.getUserTagId();

            try {
                ItemOpStatus opStatus = PromotionAction.updatePromotion(user.getSessionKey(), promotionId,
                        discountType,
                        discountValue, startDate, endDate, tagId
                        , null, null, decreaseNum, activityId);
                if (opStatus == null) {
                    continue;
                }
                String W2 = "W2";
                if (StringUtils.equals(opStatus.getOpMsg(), W2)) {
                    renderJSON(JsonUtil.getJson(new TMResult("0")));
                }
                else if (StringUtils.equals(opStatus.getOpMsg(), "applimit")) {
                    renderJSON(JsonUtil.getJson(new TMResult("-1")));
                }
                else if (opStatus.getOpMsg().contains("商品折扣幅度不能低于设置的店铺最低折扣")) {
                    String limitDis = opStatus.getOpMsg().substring(19, 23);
                    renderJSON(JsonUtil.getJson(new TMResult(limitDis)));
                }
                ErrorItems errorItem = new ErrorItems(opStatus, ItemDao.findByNumIid(user.getId(),
                        promotion.getNumIid()));
                errorItemList.add(errorItem);
                continue;

            } catch (ApiException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        renderJSON(JsonUtil.getJson(new TMResult(errorItemList)));

    }

    public static void showReviseAct(Long activityId) {
        User user = getUser();
        TMProActivity activity = TMProActivity.findByActivityId(user.getId(), activityId);
        TMResult tmResult = new TMResult(activity);
        renderJSON(tmResult);
    }

    private static boolean checkIsValidPromotionNumIid(User user, Long numIid) {
        if (numIid == null || numIid <= 0L) {
            return true;
        }

        ItemPlay item = ItemDao.findByNumIid(user.getId(), numIid);
        if (item == null) {
            return false;
        }
        return true;
    }

    public static void reviseAct(Long activityId, String activityDescription, String activityStartTime,
            String activityEndTime
            , String activityTitle) {
        User user = getUser();

//    	boolean success=checkToken();
//    	if(!success){
//    		renderJSON(JsonUtil.getJson(new TMResult("0")));
//    	}
        
        long startTime = 0;
        long endTime = 0;
        
        try {
            startTime = sdf.parse(activityStartTime).getTime();
            endTime = sdf.parse(activityEndTime).getTime();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            renderJSON(new TMResult("活动时间错误，请联系我们！"));
        }

        TMProActivity activity = TMProActivity.findByActivityId(user.getId(), activityId);
        activity.setActivityDescription(activityDescription);
        activity.setActivityStartTime(startTime);
        activity.setActivityEndTime(endTime);
        activity.setActivityTitle(activityTitle);

        String[] numiids = StringUtils.split(activity.getItems(), ",");

        Date startDate = new Date(startTime);
        Date endDate = new Date(endTime);

        List<ErrorItems> errorItemList = new ArrayList<TaoDiscount.ErrorItems>();

        /************************************************新类型的修改活动***********************************/
        if (StringUtils.isEmpty(activity.getItems())) {
            List<Promotion> promotionList = PromotionAction.findPromotionByActivityId(activityId);

            if (CommonUtils.isEmpty(promotionList)) {
                return;
            }

            for (Promotion promotion : promotionList) {
                try {
                    if (checkIsValidPromotionNumIid(user, promotion.getNumIid()) == false) {
                        continue;
                    }
                    ItemOpStatus opStatus = PromotionAction.updatePromotion(user.getSessionKey(), promotion.getId(),
                            promotion.getDiscountType(), promotion.getDiscountValue(), startDate, endDate,
                            promotion.getUserTagId(),
                            activityDescription, activityTitle, promotion.getDecreaseNum(), activityId);
                    if (opStatus == null) {
                        continue;
                    }
                    String W2 = "W2";
                    if (StringUtils.equals(opStatus.getOpMsg(), W2)) {
                        renderJSON(JsonUtil.getJson(new TMResult("0")));
                    }
                    else if (StringUtils.equals(opStatus.getOpMsg(), "applimit")) {
                        renderJSON(JsonUtil.getJson(new TMResult("-1")));
                    }
                    else if (opStatus.getOpMsg().contains("找不到关联的商品")) {
                        PromotionAction.deletePromotionById(promotion.getId());
                        continue;
                    }

                    renderJSON(new TMResult(opStatus));

                } catch (ApiException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            activity.jdbcSave();
            return;
        }

        /*********************************************************************************************/

        for (String numiid : numiids) {
            Long numIid = Long.valueOf(numiid);
            Promotion promotion = PromotionAction.findPromotionByNumIid(user.getId(), numIid);
            try {

                if (checkIsValidPromotionNumIid(user, numIid) == false) {
                    continue;
                }

                ItemOpStatus opStatus = PromotionAction.updatePromotion(user.getSessionKey(), promotion.getId(),
                        promotion.getDiscountType(), promotion.getDiscountValue(), startDate, endDate,
                        promotion.getUserTagId(),
                        activityDescription, activityTitle, promotion.getDecreaseNum(), activityId);
                if (opStatus == null) {
                    continue;
                }
                String W2 = "W2";
                if (StringUtils.equals(opStatus.getOpMsg(), W2)) {
                    renderJSON(JsonUtil.getJson(new TMResult("0")));
                }
                else if (StringUtils.equals(opStatus.getOpMsg(), "applimit")) {
                    renderJSON(JsonUtil.getJson(new TMResult("-1")));
                }
                renderJSON(new TMResult(opStatus));

            } catch (ApiException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    public static void deleteReviseItem(String subString) {
        User user = getUser();
//    	Long activityId,Long promotionId,String numIid
//    	boolean success=checkToken();
//    	if(!success){
//    		renderJSON(JsonUtil.getJson(new TMResult("0")));
//    	}
//    	

        if (StringUtils.isEmpty(subString))
            return;
        String[] idStrings = StringUtils.split(subString, "!");

        if (ArrayUtils.isEmpty(idStrings)) {
            ok();
        }
        for (String idString : idStrings) {
            String[] delete_item = StringUtils.split(idString, ",");
            Long activityId = Long.valueOf(delete_item[0]);
            Long promotionId = Long.valueOf(delete_item[1]);
            String numIid = delete_item[2];
            TMProActivity activity = TMProActivity.findByActivityId(user.getId(), activityId);

            //新类型的活动
            if (StringUtils.isEmpty(activity.getItems())) {
                try {
                    boolean success2 = PromotionAction.deletePromotion(user.sessionKey, promotionId);
                    if (!success2)
                        renderJSON(new TMResult("delete promotion flase"));
                } catch (ApiException e) {
                    log.warn(e.getMessage(), e);
                }
                continue;
            }

            String[] items = StringUtils.split(activity.getItems(), ",");

            String newitems = null;

            for (String item : items) {
                if (!StringUtils.equals(item, numIid)) {
                    if (StringUtils.isEmpty(newitems))
                        newitems = item;
                    else
                        newitems += "," + item;
                }
            }

            activity.setItems(newitems);
            activity.jdbcSave();

            try {
                boolean success2 = PromotionAction.deletePromotion(user.sessionKey, promotionId);
                if (!success2)
                    renderJSON(new TMResult("delete promotion flase"));
            } catch (ApiException e) {
                log.warn(e.getMessage(), e);
            }
        }

    }

    public static void addItems(Long activityId, String itemString, int distype) {
        User user = getUser();

        if (StringUtils.isEmpty(itemString))
            return;

        String[] idStrings = StringUtils.split(itemString, "!");

        if (ArrayUtils.isEmpty(idStrings)) {
            ok();
        }
        TMProActivity activity = TMProActivity.findByActivityId(user.getId(), activityId);

        Long createTime = CommonUtils.Date2long(new Date());

        String description = activity.getActivityDescription();
        Long startTime = activity.getActivityStartTime();
        Long endTime = activity.getActivityEndTime();
        String title = activity.getActivityTitle();
        Long userTagId = 1L;

        List<ErrorItems> errorItemList = new ArrayList<TaoDiscount.ErrorItems>();
        for (String idString : idStrings) {
            String[] item = StringUtils.split(idString, ",");
            int type = Integer.valueOf(item[0]).intValue();
            Long decreaseNum = Long.parseLong(item[1]);
            String promotionType = "";
            if (type == 0) {
                promotionType = "DISCOUNT";
            }
            else {
                promotionType = "PRICE";
            }
            String items = item[2];
            String discountValue = item[3];

            if (promotionType == ""
                    || (decreaseNum != 1L && decreaseNum != 0L)) {

                log.error(String.format("param error, userTagId = [%d],type=[%s],discountValue=[%s],decreaseNum=[%s]",
                        userTagId, promotionType, discountValue, decreaseNum));

                renderJSON(TMResult.failMsg("对不起，输入参数不正确!"));

            } else {
                log.info("start add activity......");

                /*****************************新类型添加***********************************/
                if (StringUtils.isEmpty(activity.getItems())) {
                    ItemOpStatus opStatus = PromotionAction.addActivity(user.getId(), title,
                            description, createTime, startTime, endTime, promotionType,
                            discountValue, decreaseNum, userTagId, items, activityId);
                    if (opStatus == null) {
                        continue;
                    }
                    String W2 = "W2";
                    if (StringUtils.equals(opStatus.getOpMsg(), W2)) {
                        renderJSON(JsonUtil.getJson(new TMResult("0")));
                    }
                    else if (StringUtils.equals(opStatus.getOpMsg(), "applimit")) {
                        renderJSON(JsonUtil.getJson(new TMResult("-1")));
                    }
                    else if (opStatus.getOpMsg().contains("商品折扣幅度不能低于设置的店铺最低折扣")) {
                        String limitDis = opStatus.getOpMsg().substring(19, 23);
                        renderJSON(JsonUtil.getJson(new TMResult(limitDis)));
                    }
                    ErrorItems errorItem = new ErrorItems(opStatus, ItemDao.findByNumIid(user.getId(),
                            Long.valueOf(items)));
                    errorItemList.add(errorItem);
                    continue;
                }

                activity.addItem(items);
                ItemOpStatus opStatus = PromotionAction.addActivity(user.getId(), title,
                        description, createTime, startTime, endTime, promotionType,
                        discountValue, decreaseNum, userTagId, items, activityId);

                if (opStatus == null) {
                    continue;
                }
                String W2 = "W2";
                if (StringUtils.equals(opStatus.getOpMsg(), W2)) {
                    renderJSON(JsonUtil.getJson(new TMResult("0")));
                }
                else if (StringUtils.equals(opStatus.getOpMsg(), "applimit")) {
                    renderJSON(JsonUtil.getJson(new TMResult("-1")));
                }
                else if (opStatus.getOpMsg().contains("商品折扣幅度不能低于设置的店铺最低折扣")) {
                    String limitDis = opStatus.getOpMsg().substring(19, 23);
                    renderJSON(JsonUtil.getJson(new TMResult(limitDis)));
                }
                ErrorItems errorItem = new ErrorItems(opStatus, ItemDao.findByNumIid(user.getId(), Long.valueOf(items)));
                errorItemList.add(errorItem);
            }
        }
        renderJSON(JsonUtil.getJson(new TMResult(errorItemList)));

        log.info("successful add items" + activity.getItems());
        activity.jdbcSave();

    }

    public static void debugApiDelete() {
        User user = getUser();
        List<Promotion> promotion = PromotionAction.findPromotionAllByUserId(user.getId());

        if (!CommonUtils.isEmpty(promotion)) {
            try {
                for (Promotion pro : promotion) {
                    boolean success2 = PromotionAction.deletePromotion(user.sessionKey, pro.getId());
                    if (!success2) {
                        renderJSON(new TMResult("删除活动失败"));
                    }
                    PromotionAction.deletePromotionById(pro.getId());
                }

            } catch (ApiException e) {
                log.warn(e.getMessage(), e);
            }
        }
        List<TMProActivity> activity = TMProActivity.findOnActiveActivitys(user.getId());
        if (!CommonUtils.isEmpty(activity)) {
            for (TMProActivity act : activity) {
                TMProActivity.deleteActivityById(user.getId(), act.getId());
            }
        }

        renderJSON(new TMResult("删除活动成功"));
    }

    public static void debugApiDeleteLimit(Long promotionId) {
        User user = getUser();

        try {

            boolean success2 = PromotionAction.deletePromotion(user.sessionKey, promotionId);
            if (!success2) {
                renderJSON(new TMResult("删除活动失败"));
            }
            PromotionAction.deletePromotionById(promotionId);

        } catch (ApiException e) {
            log.warn(e.getMessage(), e);
        }

        renderJSON(new TMResult("删除活动成功"));
    }

    public static void debugApiDeleteByActivityId(Long activityId) {
        User user = getUser();

        try {

            List<Promotion> promotionList = PromotionAction.findPromotionByActivityId(activityId);

            if (CommonUtils.isEmpty(promotionList)) {
                return;
            }

            for (Promotion promotion : promotionList) {
                boolean success2 = PromotionAction.deletePromotion(user.sessionKey, promotion.getId());
                if (!success2) {
                    renderJSON(new TMResult("删除活动失败"));
                }
                PromotionAction.deletePromotionById(promotion.getId());
            }

        } catch (ApiException e) {
            log.warn(e.getMessage(), e);
        }

        renderJSON(new TMResult("删除活动成功"));
    }

    public static void itemAllAdd(String activityString) {
        User user = getUser();

        if (StringUtils.isEmpty(activityString))
            return;
        String[] item = StringUtils.split(activityString, ",");

        if (ArrayUtils.isEmpty(item)) {
            ok();
        }

        String description = item[0];
        Long createTime = CommonUtils.Date2long(new Date());
        Long startTime = Long.valueOf(item[1]);
        Long endTime = Long.valueOf(item[2]);
        String title = item[3];
        Long decreaseNum = 0L;
        String promotionType = "DISCOUNT";
        String discountValue = item[4];
        Long userTagId = 1L;
        String status = "ACTIVE";

        String numIids = "";

        List<Promotion> promotionList = PromotionAction.findPromotionAllByUserId(user.getId());

        TMProActivity activity = new TMProActivity(user.getId(), title, description, createTime, startTime, endTime,
                status);

        boolean success3 = activity.jdbcSave();

        List<TMProActivity> activityList = TMProActivity.findOnActiveActivitys(user.getId());

        if (!success3) {
            renderJSON(new TMResult("创建活动失败，请重新创建！"));
        }

        for (Promotion promotion : promotionList) {
            if (numIids == "") {
                numIids = promotion.getNumIid().toString();
            }
            else {
                numIids += "," + promotion.getNumIid().toString();
            }

        }
        List<ItemPlay> itemList = null;
        if (numIids == "") {
            itemList = ItemDao.findOnSaleByUserId(user.getId());
        }
        else {
            itemList = ItemDao.findOnSaleOutOfNumIids(user.getId(), numIids);
        }

        if (itemList == null) {
            renderJSON(new TMResult("亲，您没有在售的宝贝哦！"));
        }

        List<ErrorItems> errorItemList = new ArrayList<TaoDiscount.ErrorItems>();

        for (int i = 0; i < itemList.size(); i = i + 10) {
            String num_iids = "";
            for (int j = 0; (i + j) < itemList.size() && j < 10; j++) {
                if (num_iids == "") {
                    num_iids = itemList.get(i + j).getNumIid().toString();
                }
                else {
                    num_iids += "," + itemList.get(i + j).getNumIid().toString();
                }
            }

            if (num_iids == "") {
                continue;
            }

            ItemOpStatus opStatus = PromotionAction.addActivity(user.getId(), title,
                    description, createTime, startTime, endTime, promotionType,
                    discountValue, decreaseNum, userTagId, num_iids, activity.getId());

            if (opStatus == null) {
                continue;
            }
            String W2 = "W2";
            if (StringUtils.equals(opStatus.getOpMsg(), W2)) {
                renderJSON(JsonUtil.getJson(new TMResult("0")));
            }
            else if (StringUtils.equals(opStatus.getOpMsg(), "applimit")) {
                renderJSON(JsonUtil.getJson(new TMResult("-1")));
            }
            else if (opStatus.getOpMsg().contains("商品折扣幅度不能低于设置的店铺最低折扣")) {
                String limitDis = opStatus.getOpMsg().substring(19, 23);
                renderJSON(JsonUtil.getJson(new TMResult(limitDis)));
            }

            List<ItemPlay> ErrorNumList = ItemDao.findByNumIidListAndUserId(user.getId(), num_iids);
            if (CommonUtils.isEmpty(ErrorNumList)) {
                continue;
            }
            for (ItemPlay ErrorNum : ErrorNumList) {
                ErrorItems errorItem = new ErrorItems(opStatus, ErrorNum);
                errorItemList.add(errorItem);
            }

        }

        renderJSON(JsonUtil.getJson(new TMResult(errorItemList)));

//        for(ItemPlay items :itemList){
//            log.info("start add activity......");
//            String numString=items.getNumIid().toString();
//            if(StringUtils.isEmpty(numString)){
//            	continue;
//            }
////            activity.addItem(numString);
//            String errorMessage = PromotionAction.addActivity(user.getId(), title,
//                    description, createTime, startTime, endTime, promotionType,
//                    discountValue, decreaseNum, userTagId, numString,activity.getId());
//            String W2 = "W2";
//            if (StringUtils.equals(errorMessage, W2)) {
//                renderJSON(JsonUtil.getJson(new TMResult("0")));
//            }
////            if (errorMessage != null && !StringUtils.equals(errorMessage, W2))
////            {
////                activity.jdbcSave();
////            	renderJSON(new TMResult(errorMessage));
////            }    
//
//        }

        activity.jdbcSave();
    }

    public static class ErrorItems {

        public ItemOpStatus opstatus;

        public ItemPlay item;

        public ErrorItems(ItemOpStatus opstatus, ItemPlay item) {
            this.opstatus = opstatus;
            this.item = item;
        }

        public ItemOpStatus getOpstatus() {
            return opstatus;
        }

        public void setOpstatus(ItemOpStatus opstatus) {
            this.opstatus = opstatus;
        }

        public ItemPlay getItem() {
            return item;
        }

        public void setItem(ItemPlay item) {
            this.item = item;
        }

    }

    public static final SimpleDateFormat dateSDF = new SimpleDateFormat("yyyy-MM-dd");

    public static void getQuerySalesCount(Long numIid, String startTime, String endTime) throws ParseException {

        long startTs = dateSDF.parse(startTime).getTime();
        long endTs = dateSDF.parse(endTime).getTime();

        List<EveryDaySalesCount> itemList = EveryDaySalesCount.findByNumIidAndTs(numIid, startTs, endTs);

        renderJSON(new TMResult(itemList));
    }

    public static void cuxiaofenxi() {
        render("tdazhe/cuxiaofenxi.html");
    }

    public static void searchPromotionItems(String title, String cid, String sellerCid, String order, int pn, int ps) {

        User user = getUser();

        PageOffset po = new PageOffset(pn, ps);

        if (user.getUserNick().contains("测试") || user.getUserNick().contains("淘宝")
                || user.getUserNick().contains("开放平台")
                || user.getUserNick().contains("support") || user.getUserNick().contains("包u吧")
                || user.getUserNick().contains("包邮吧")
                || user.getUserNick().contains("楚之小南") || user.getUserNick().contains("jy87771107")) {
            try {
                new UpdateSalesCountJob().doJob();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        List<ItemPlay> itemList = ItemDao.findPromotionByConditionAndOrder(user.getId(), null, title, cid, sellerCid,
                order, po);

        if (CommonUtils.isEmpty(itemList)) {
            renderJSON(new TMResult("亲， 没有相应宝贝哟！！！！！"));
        }

        //新类型打折的搜索
        long count = ItemDao.countPromotionByConditionAndOrder(user.getId(),
                null, title, cid, sellerCid);
        List<Item_Activity> item_acts = new ArrayList<TaoDiscount.Item_Activity>();

        for (ItemPlay item : itemList) {
            Promotion promotion = PromotionAction.selectBynumiid(item
                    .getNumIid());
            if (promotion == null) {
                continue;
            }
            Item_Activity item_act = new Item_Activity();

            item_act.numiid = item.getNumIid();
            item_act.title = item.getTitle();
            item_act.picURL = item.getPicURL();
            item_act.price = item.getPrice();
            item_act.promotionId = promotion.getId();
            if (StringUtils.equals(promotion.getDiscountType(), "DISCOUNT")) {
                item_act.discountType = 0;
            } else {
                item_act.discountType = 1;
            }
            item_act.discountValue = promotion.getDiscountValue();

            item_acts.add(item_act);

        }

        TMResult tmResult = new TMResult(item_acts, (int) count, po);
        renderJSON(tmResult);

    }
    
    public static void addMjsActivity(String startTimeStr, String endTimeStr, String title,
    		String description, String mjsParamStr, String tmplHtml, String remark) {
    	long startTime = 0;
        long endTime = 0;
        
        try {
            startTime = sdf.parse(startTimeStr).getTime();
            endTime = sdf.parse(endTimeStr).getTime();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    	if(StringUtils.isEmpty(description)) {
    		renderFailedJson("请输入活动名称");
    	}
    	if(StringUtils.isEmpty(title)) {
    		renderFailedJson("请输入活动标签");
    	}
    	User user = getUser();
    	if(user == null) {
    		renderFailedJson("用户不存在");
    	}
    	
    	UmpPromotion.checkW2Expires(user, "/taodiscount/mjsAddActivity");
    	
    	MjsParams mjsParams = new MjsParams();
    	if(!StringUtils.isEmpty(mjsParamStr)) {
    		log.info("addMjsActivity for user "+user.getUserNick() + " with mjsParams "
    				+ mjsParamStr);
    		mjsParams = mjsParams.createByJson(mjsParamStr);
    	}
    	if(mjsParams == null) {
    		renderFailedJson("满就送json解析出错");
    	}
    	mjsParams.setActivityName(title);
    	
    	UmpMjsActivityAdd umpMjsActivityAdd = new UMPApi.UmpMjsActivityAdd(user, mjsParams);
    	Long mjsActivityId = umpMjsActivityAdd.call();
    	
    	if(mjsActivityId == null || mjsActivityId <= 0) {
    		renderFailedJson(umpMjsActivityAdd.getSubErrorMsg());
    	}
    	
    	TMProActivity activity = new TMProActivity(user.getId(), startTime,
                endTime, title, description, TMProActivity.ActivityStatus.UNACTIVE, 
                TMProActivity.ActivityType.Manjiusong);
    	activity.setRemark(remark);
    	activity.setMjsActivityId(mjsActivityId);
    	activity.setMjsParams(mjsParamStr);
    	if(!StringUtils.isEmpty(tmplHtml)) {
    		activity.setTmplHtml(tmplHtml.trim());
    	}
    	// 因为暂时还没添加宝贝，所以不需要改宝贝的详情页
    	activity.jdbcSave();
    	
    	renderSuccessJson(activity.getId().toString());
    }
    
    public static void restartMjsActivity(String startTimeStr, String endTimeStr, String title,
    		String description, String mjsParamStr, String tmplHtml, String remark, Long activityId) {
    	if(activityId == null || activityId <= 0) {
    		renderFailedJson("活动Id不合法");
    	}

        long startTime = 0;
        long endTime = 0;
        
        try {
            startTime = sdf.parse(startTimeStr).getTime();
            endTime = sdf.parse(endTimeStr).getTime();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    	if(StringUtils.isEmpty(description)) {
    		renderFailedJson("请输入活动名称");
    	}
    	if(StringUtils.isEmpty(title)) {
    		renderFailedJson("请输入活动标签");
    	}
    	User user = getUser();
    	if(user == null) {
    		renderFailedJson("用户不存在");
    	}
    	TMProActivity activity = TMProActivity.findByActivityId(user.getId(), activityId);
    	if(activity == null) {
    		renderFailedJson("活动不存在");
    	}
    	UmpPromotion.checkW2Expires(user, "/taodiscount/mjsRestartActivity");
    	
    	MjsParams mjsParams = new MjsParams();
    	if(!StringUtils.isEmpty(mjsParamStr)) {
    		log.info("restartMjsActivity for user " +user.getUserNick() + " with mjsParams : "
    				+ mjsParamStr);
    		mjsParams = mjsParams.createByJson(mjsParamStr);
    	}
    	if(mjsParams == null) {
    		renderFailedJson("满就送json解析出错");
    	}
    	mjsParams.setActivityName(title);
    	
    	UmpMjsActivityAdd umpMjsActivityAdd = new UMPApi.UmpMjsActivityAdd(user, mjsParams);
    	Long mjsActivityId = umpMjsActivityAdd.call();
    	
    	if(mjsActivityId == null || mjsActivityId <= 0) {
    		renderFailedJson(umpMjsActivityAdd.getSubErrorMsg());
    	}
    	
    	activity.setUserId(user.getId());
    	activity.setActivityStartTime(startTime);
    	activity.setActivityEndTime(endTime);
    	activity.setActivityTitle(title);
    	activity.setActivityDescription(description);
    	activity.setStatus(TMProActivity.ActivityStatus.UNACTIVE);
    	activity.setActivityType(TMProActivity.ActivityType.Manjiusong);
    	activity.setRemark(remark);
    	activity.setMjsActivityId(mjsActivityId);
    	activity.setMjsParams(mjsParamStr);
    	if(!StringUtils.isEmpty(tmplHtml)) {
    		activity.setTmplHtml(tmplHtml.trim());
    	}
    	// 因为暂时还没添加宝贝，所以不需要改宝贝的详情页
    	activity.jdbcSave();
    	
    	renderSuccessJson(activity.getId().toString());
    }
    
    public static void updateMjsActivity(Long activityId, String startTimeStr, String endTimeStr, String title,
    		String description, String mjsParamStr, Boolean isShop, String tmplHtml, String remark) {

        long startTime = 0;
        long endTime = 0;
        
        try {
            startTime = sdf.parse(startTimeStr).getTime();
            endTime = sdf.parse(endTimeStr).getTime();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    	if(StringUtils.isEmpty(description)) {
    		renderFailedJson("请输入活动名称");
    	}
    	if(StringUtils.isEmpty(title)) {
    		renderFailedJson("请输入活动标签");
    	}
    	User user = getUser();
    	if(user == null) {
    		renderFailedJson("用户不存在");
    	}
    	if(isShop == null) {
    		isShop = false;
    	}
    	UmpPromotion.checkW2Expires(user, "/UmpMjs/mjsUpdateActivity?activityId=" + activityId);
    	
    	MjsParams mjsParams = new MjsParams();
    	if(!StringUtils.isEmpty(mjsParamStr)) {
    		log.info("updateMjsActivity for " + user.getUserNick() + " with mjsPrams : " +
    				mjsParamStr);
    		mjsParams = mjsParams.createByJson(mjsParamStr);
    	}
    	if(mjsParams == null) {
    		renderFailedJson("满就送json解析出错");
    	}
    	mjsParams.setActivityName(title);
    	
    	TMProActivity activity = TMProActivity.findByActivityId(user.getId(), activityId);
    	if(activity == null) {
    		renderFailedJson("对应活动不存在");
    	}
    	Long mjsActivityId = activity.getMjsActivityId();
    	if(mjsActivityId == null || mjsActivityId <= 0) {
    		renderFailedJson("该活动没有对应的mjs 活动Id");
    	}
    	
    	UmpMjsActivityUpdate umpMjsActivityUpdate = new UMPApi.UmpMjsActivityUpdate(user, mjsParams, 
    			activity.getMjsActivityId(), isShop);
    	Boolean isSuccess = umpMjsActivityUpdate.call();
    	if(!isSuccess) {
    		renderFailedJson(umpMjsActivityUpdate.getSubErrorMsg());
    	}
    	activity.setActivityStartTime(startTime);
    	activity.setActivityEndTime(endTime);
    	activity.setActivityTitle(title);
    	activity.setActivityDescription(description);
    	activity.setStatus(TMProActivity.ActivityStatus.ACTIVE);
    	activity.setMjsActivityId(mjsActivityId);
    	activity.setRemark(remark);
    	activity.setMjsParams(mjsParamStr);
    	
    	//if(!StringUtils.isEmpty(tmplHtml)) {
    		activity.setTmplHtml(tmplHtml);
    		// 并且更新每个宝贝的详情页
    		if(activity.isMjsActivity()) {
    			UmpMjsAction.updateItemsMjsTmpl(user, activity.getItems(), tmplHtml, activityId);
    		} else {
    			UmpMjsAction.updateItemsMjsTmpl(user, ItemDao.findNumIidWithUser(user.getId()),
        				tmplHtml, activityId);
    		}
    		
    	//}
    	activity.jdbcSave();
    	
    	renderSuccessJson(activity.getId().toString());
    }
    
    
    public static void findUser() {
        User user = getUser();
        
        renderJSON(JsonUtil.getJson(user));
    }
    
    public static void setShowOldDiscount(boolean isOn) {
        User user = getUser();
        
        user.setShowOldDiscount(isOn);
        
        user.jdbcSave();
        
        renderText("成功！");
    }
    
    public static void getPromotionMiscListByActivityId(String sessionKey) {

    	List<ItemPromotion> res = new UMPApi.PromotionMiscListAPI(sessionKey).call();
    	renderJSON(JsonUtil.getJson(res));
    }
    
    public static void getItemsByActivityId(Long activityId, String sessionKey) {
    	if(activityId == null || activityId <= 0L) {
    		renderFailedJson("活动ID 为空");
    	}
    	ItemPromotion promotion = new UMPApi.PromotionMiscItemsAPI(sessionKey, activityId).call();
    	renderJSON(JsonUtil.getJson(promotion));
    }
}
