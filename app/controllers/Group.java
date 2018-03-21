package controllers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import models.group.FavoriteModel;
import models.group.GroupModel;
import models.group.GroupPlan;
import models.group.GroupQueue;
import models.group.GroupedItems;
import models.item.ItemPlay;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.CacheFor;

import result.TMResult;

import actions.GroupAction;
import actions.TemplateAction;
import actions.DiagAction.BatchResultMsg;

import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.NumberUtil;
import com.jd.open.api.sdk.internal.util.StringUtil;

import dao.item.ItemDao;

public class Group extends TMController {

    private static final Logger log = LoggerFactory.getLogger(Associate.class);

    private static String TAG = "GROUP";
   
    public static void listModels(String showFlag, int pn, int ps) {
        User user = getUser();
        if (user == null) {
            renderJSON("{}");
        }

        if ("favorite=true".equals(showFlag)) {
            listFavModels(pn, ps);
        }
        if ("showAll".equals(showFlag)) {
            listAllModels(pn, ps);
        }
    }
    
    public static void listFavModels(int pn,int ps){
        User user = getUser();
        PageOffset po = new PageOffset(pn, ps);
        
        List<Map<String, GroupModel>> list = GroupAction.listFavoriteModels(user, po.getOffset(), po.getPs());

        int count = FavoriteModel.getFavoriteCount(user.getId());

        TMResult tmRes = new TMResult(list, count, po);

        renderJSON(JsonUtil.getJson(tmRes));
        
    }
    
    public static void listAllModels(int pn,int ps){
        User user = getUser();
        
        PageOffset po = new PageOffset(pn, ps);
        
        List<Map<String, GroupModel>> list = GroupAction.listModels(user, po.getOffset(), po.getPs());

        int count = GroupModel.getModelCount();

        TMResult tmRes = new TMResult(list, count, po);

        renderJSON(JsonUtil.getJson(tmRes));
    }

    public static void favorite(Long modelId) {
        User user = getUser();
        if (NumberUtil.isNullOrZero(modelId)) {
            renderJSON("{}");
        }
        boolean flag = GroupAction.doFavorite(modelId, user);

        renderJSON(JsonUtil.getJson(flag));
    }
    
    public static void favoriteCancel(Long modelId){
        User user = getUser();
        if (NumberUtil.isNullOrZero(modelId)) {
            renderJSON("{}");
        }
        boolean flag = GroupAction.favoriteCancel(modelId, user);

        renderJSON(JsonUtil.getJson(flag));
    }

    public static void selectModel(Long modelId, String type) {
        User user = getUser();

        if (NumberUtil.isNullOrZero(modelId) || user == null || StringUtils.isEmpty(type)) {
            renderJSON("{}");
        }
        Map<String, GroupModel> modelMap = GroupAction.selectModelAction(modelId, type);
        renderJSON(JsonUtil.getJson(modelMap));
    }

    public static void autoInsert() {
        User user = getUser();
        if (user == null) {
            renderJSON("{]");
        }
        ItemPlay item = GroupAction.getRecommendItem(user);
        renderJSON(JsonUtil.getJson(item));
    }

    public static void listItems(int pn, int ps, String searchText, Long sellerCid, String sort) {
        User user = getUser();

        if (!GroupAction.priceAsc.equals(sort) && !GroupAction.priceDesc.equals(sort)
                && !GroupAction.sellAsc.equals(sort) && !GroupAction.sellDesc.equals(sort)
                && !GroupAction.sortNormal.equals(sort)) {
            renderJSON("{}");
        }

        PageOffset po = new PageOffset(pn, ps);

        List<ItemPlay> itemsList = GroupAction.listItems(user, po.getOffset(), po.getPs(), searchText, sellerCid, sort);

        // about size;
        long size = ItemDao.countOnlineBySellerCid(user.getId(), searchText, sellerCid);

        TMResult tmRes = new TMResult(itemsList, (int) size, po);

        renderJSON(JsonUtil.getJson(tmRes));
    }

    public static void savePlan(Long planId, String planName, Long modelId, String type, String color,
            String activityTitle, String label,String btnName, String originalPriceName, String currentPriceName, int days,
            int hours, int minutes) {
        User user = getUser();

        if (user == null || modelId == null || planName == null) {
            renderJSON("{}");
        }
        Long id = GroupAction.savePlan(planId, planName, user.getId(), modelId, type, activityTitle,label,  btnName,
                originalPriceName, currentPriceName, days, hours, minutes);

        renderJSON(JsonUtil.getJson(id));
    }

    public static void getOneItem(Long numIid) {
        User user = getUser();
        if (user == null || NumberUtil.isNullOrZero(numIid)) {
            renderJSON("{}");
        }

        ItemPlay item = GroupAction.getItem(numIid, user);

        renderJSON(JsonUtil.getJson(item));
    }

    public static void saveItemProp(Long planId, String itemString) {
        User user = getUser();
        if (NumberUtil.isNullOrZero(planId) || user == null) {
            renderJSON("{}");
        }

        Long id = GroupAction.saveItemProp(planId, user, itemString);

        renderJSON(JsonUtil.getJson(id));
    }

    public static void putAll(Long planId) {
        User user = getUser();
        if (NumberUtil.isNullOrZero(planId) || user == null) {
            renderJSON("{}");
        }
        boolean flag = GroupAction.putAll(planId, user);
        renderJSON(JsonUtil.getJson(flag));
    }

    public static void putAllNoQueue(Long planId) {
        User user = getUser();
        if (NumberUtil.isNullOrZero(planId) || user == null) {
            renderJSON("{}");
        }
        List<BatchResultMsg> results = GroupAction.putAllNoQueue(planId, user);
        renderJSON(JsonUtil.getJson(results));
    }

    public static void putChecked(Long planId, String numIidsString) {
        User user = getUser();
        if (NumberUtil.isNullOrZero(planId) || user == null || StringUtils.isEmpty(numIidsString)) {
            renderJSON("{}");
        }
        boolean flag = GroupAction.putChecked(planId, numIidsString, user);
        renderJSON(JsonUtil.getJson(flag));
    }

    public static void putCheckedNoQueue(Long planId, String numIidsString) {
        User user = getUser();
        if (NumberUtil.isNullOrZero(planId) || user == null || StringUtils.isEmpty(numIidsString)) {
            renderJSON("{}");
        }
        List<BatchResultMsg> results = GroupAction.putCheckedNoQueue(planId, numIidsString, user);
        renderJSON(JsonUtil.getJson(results));
    }

    public static void showPlans(int pn, int ps, int status) {
        User user = getUser();
        if (user == null) {
            renderJSON("{}");
        }
        PageOffset po = new PageOffset(pn, ps);

        List<Map<String, GroupPlan>> mapList = GroupAction.showPlans(user, po.getOffset(), po.getPs(), status);
        
        int planCount = GroupAction.getPlanCount(user, status);

        TMResult tmRes = new TMResult(mapList, planCount, po);

        renderJSON(JsonUtil.getJson(tmRes));
    }

    public static void stopOnePlan(Long planId) {
        User user = getUser();
        if (NumberUtil.isNullOrZero(planId) || user == null) {
            renderJSON("{}");
        }
        boolean flag = GroupAction.stopOnePlan(planId, user);
        renderJSON(JsonUtil.getJson(flag));
    }

    public static void showGroupedItems(Long planId,int status) {
        User user = getUser();
        if (NumberUtil.isNullOrZero(planId) || user == null) {
            renderJSON("{}");
        }

        List<GroupedItems> items = GroupAction.getGroupedItems(planId, user,status);

        renderJSON(JsonUtil.getJson(items));
    }
    
    public static void showGroupedItemsPage(Long planId,int status,int pn, int ps) {
        User user = getUser();
        if (NumberUtil.isNullOrZero(planId) || user == null) {
            renderJSON("{}");
        }
        PageOffset po = new PageOffset(pn, ps);
        
        List<GroupedItems> items = GroupAction.findGroupedItemsPage(planId, user,status,po.getOffset(), po.getPs());
        
        int itemCount = GroupAction.getGroupedItemsCount(user, status,planId);

        TMResult tmRes = new TMResult(items, itemCount, po);

        renderJSON(JsonUtil.getJson(tmRes));

        renderJSON(JsonUtil.getJson(items));
    }
    
    public static void delOneItemOnePlan(Long numIid, Long planId) {
        User user = getUser();
        if (NumberUtil.isNullOrZero(numIid) || NumberUtil.isNullOrZero(planId)) {
            renderJSON("{}");
        }
        BatchResultMsg resultMsg = GroupAction.deleteOneTmp(numIid, planId, user);
        renderJSON(JsonUtil.getJson(resultMsg));
    }
    
    public static void addOneItemPlan(Long numIid,Long planId){
        User user = getUser();
        if (NumberUtil.isNullOrZero(numIid) || NumberUtil.isNullOrZero(planId)) {
            renderJSON("{}");
        }
        BatchResultMsg resultMsg = GroupAction.addOneItem(planId, numIid, user);
        renderJSON(JsonUtil.getJson(resultMsg));
    }
    

    public static void getItemCount() {
        User user = getUser();
        if (user == null) {
            renderJSON("{}");
        }
        Set<Long> count = ItemDao.findNumIidsByUserStatus(user.getId());
        renderJSON(JsonUtil.getJson(count.size()));
    }

    public static void getPlan(Long planId) {
        User user = getUser();
        if (NumberUtil.isNullOrZero(planId) || user == null) {
            renderJSON("{}");
        }
        boolean flag = GroupAction.findPlanById(planId, user);
        renderJSON(JsonUtil.getJson(flag));
    }

    public static void deletePlanId(Long planId) {
        User user = getUser();
        if (NumberUtil.isNullOrZero(planId) || user == null) {
            renderJSON("{}");
        }
        boolean flag = GroupAction.deletPlan(planId, user);
        renderJSON(JsonUtil.getJson(flag));
    }

    public static void showPlansNoHtml(int status, int pn, int ps) {
        User user = getUser();
        if (user == null) {
            renderJSON("{}");
        }
        PageOffset po = new PageOffset(pn, ps);

        List<GroupPlan> plans = GroupAction.getGroupPlans(user, status, po.getOffset(), po.getPs());

        int planCount = GroupAction.getPlanCount(user, status);

        TMResult tmRes = new TMResult(plans, planCount, po);

        renderJSON(JsonUtil.getJson(tmRes));
    }

    public static void showOnePlan(Long planId) {
        User user = getUser();
        if (NumberUtil.isNullOrZero(planId) || user == null) {
            renderJSON("{}");
        }
        String html = GroupAction.showOnePlan(planId,user);
        renderJSON(JsonUtil.getJson(html));
    }
    
    public static void getItemsStatus(Long planId){
        User user = getUser();
        if(NumberUtil.isNullOrZero(planId) || user == null){
            renderJSON("{}");
        }
        GroupPlan plan = GroupAction.getPlanStatus(planId, user);
        renderJSON(JsonUtil.getJson(plan));
    }
    
    public static void stopPlanByNumiids(Long planId,String numIidsStr){
        User user = getUser();
        if(NumberUtil.isNullOrZero(planId) || StringUtil.isEmpty(numIidsStr) || user == null){
            renderJSON("{}");
        }
        List<BatchResultMsg> resultMsgs = GroupAction.stopPlanByNumIids(planId,numIidsStr,user);
        renderJSON(JsonUtil.getJson(resultMsgs));
    }
    
    public static void dealByNumIids(Long planId,String numIidsStr){
        User user = getUser();
        if(NumberUtil.isNullOrZero(planId) || StringUtil.isEmpty(numIidsStr) || user == null){
            renderJSON("{}");
        }
        List<BatchResultMsg> resultMsgs = GroupAction.dealByNumIids(planId,numIidsStr,user);
        renderJSON(JsonUtil.getJson(resultMsgs));
    }
    
    
    public static void getGroupedItems(Long planId){
        User user = getUser();
        if(NumberUtil.isNullOrZero(planId) || user == null){
            renderJSON("{}");
        }
        List<Long> numIids = GroupedItems.findNumIidsByPlanIdAndStatus(user.getId(), planId,GroupAction.TWO);
        renderJSON(JsonUtil.getJson(numIids));
    }
    
    public static void getGroupedItem(Long planId,Long numIid){
        User user = getUser();
        if(NumberUtil.isNullOrZero(planId) || NumberUtil.isNullOrZero(numIid) || user == null){
            renderJSON("{}");
        }
        GroupedItems item = GroupedItems.findOneGroupedItem(user.getId(), planId, numIid);
        renderJSON(JsonUtil.getJson(item));
    }
    
    
    
    //删除“null”字符串 由于之前生成计划出错时返回了一个null 导致插入了null字符（已修复）
    public static void deleteNULLByNumIid(Long numIid){
        User user = getUser();
        BatchResultMsg result = GroupAction.deleteNULLByNumIid(numIid, user);
        renderJSON(JsonUtil.getJson(result));
    }
    
    public static void deleteNULLAllShop(){
        User user = getUser();
        List<BatchResultMsg> results = GroupAction.deleteNULLAllShop(user);
        renderJSON(JsonUtil.getJson(results));
    }
    
    //删除一个宝贝下的某一个团购模版
    public static void deleteOnePlanForOneItem(Long numIid,Long planId){
		User user = getUser();
		BatchResultMsg result = GroupAction.deleteOneTmp(numIid,planId,user);
        renderJSON(result);
    }
    
    //删除一个店铺中某个团购模版
    public static void deleteOnePlanForOneShop(Long planId){
    	User user = getUser();
        List<Long> numIidsList = ItemDao.findNumIidListWithUser(user.getId());
        GroupQueue gq = new GroupQueue();
        gq.setNumIids(numIidsList);
        gq.setPlanId(planId);
        gq.setUser(user);
        List<BatchResultMsg> results = GroupAction.deleteOnePlan(gq);
        renderJSON(JsonUtil.getJson(results));
    }

    public static void groupAll() {
        render("/group/groupAll.html");
    }

    public static void myFavorite() {
        render("/group/myFavorite.html");
    }

    public static void groupInput() {
        render("/group/groupInput.html");
    }

    public static void groupUnPut() {
        render("/group/groupUnput.html");
    }

    public static void groupCancel() {
        render("/group/groupCancel.html");
    }
}