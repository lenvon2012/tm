package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import models.associate.AssociateModel;
import models.associate.AssociatePlan;
import models.associate.AssociatedItems;
import models.item.ItemPlay;
import models.user.User;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bustbapi.ItemApi;
import bustbapi.ItemApi.ItemDescModulesUpdater;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.NumberUtil;

import dao.UserDao;
import dao.item.ItemDao;
import actions.AssociateAction;
import actions.DiagAction.BatchResultMsg;
import play.cache.CacheFor;
import result.TMResult;

public class Associate extends TMController {

    private static final Logger log = LoggerFactory.getLogger(Associate.class);

    public static String TAG = "ASSOCIATE";

    /**
     * recommend
     */
    @CacheFor("1h")
    public static void listModel(int width, int type, int maxNum, int pn, int ps) {
        User user = getUser();
        if (user == null) {
            renderJSON("{}");
        }
        
        PageOffset po = new PageOffset(pn, ps);

        List<AssociateModel> list = AssociateAction.listAssociateModel(user, width, maxNum, type, po.getOffset(),
                po.getPs());
        int modelCount = AssociateModel.getModelCount(width, maxNum, type);
        
        TMResult tmRes = new TMResult(list, modelCount, po);
        
        renderJSON(JsonUtil.getJson(tmRes));
    }

    public static void selectModel(Long modelId) {
        if(NumberUtil.isNullOrZero(modelId)){
            renderJSON("{}");
        }
        List<Object> list = new ArrayList<Object>();

        String html = AssociateAction.selectModel(modelId);
        
        AssociateModel model = AssociateAction.findAssociateModel(modelId);
        if(model == null){
            renderJSON("{}");
        }
        else{
            int maxNum = model.getMaxNum();
            list.add(maxNum);
            list.add(html);
            renderJSON(JsonUtil.getJson(list));
        }
    }

    public static void getRelatedRecommends() {
        User user = getUser();
        Long numIid = ItemDao.findBestSales(user.getId());
        List<ItemPlay> list = AssociateAction.getRecommend(numIid, user);
        renderJSON(JsonUtil.getJson(list));
    }
    
    public static void findModelById(Long modelId){
        if(NumberUtil.isNullOrZero(modelId)){
            renderJSON("{}");
        }
        AssociateModel  model = AssociateAction.findAssociateModel(modelId);
        
        renderJSON(JsonUtil.getJson(model));
    }

    /**
     * @param -1 <= sort <= 3
     */
    public static void listItems(int pn, int ps, String searchText, Long sellerCid, int sort) {
        
        User user = getUser();
        
        PageOffset po = new PageOffset(pn, ps);

        List<ItemPlay> items = AssociateAction.listItems(user, po.getOffset(), po.getPs(), searchText, sellerCid, sort);

        // about size;
        long size = ItemDao.countOnlineBySellerCid(user.getId(), searchText, sellerCid);

        TMResult tmRes = new TMResult(items, (int) size, po);
        
        renderJSON(JsonUtil.getJson(tmRes));
    }
    
    public static void listItemsNoMinPrice(int pn, int ps, String searchText, Long sellerCid, int sort) {

        User user = getUser();

        PageOffset po = new PageOffset(pn, ps);

        List<ItemPlay> items = AssociateAction.listItemsNoMinPrice(user, po.getOffset(), po.getPs(), searchText, sellerCid, sort);

        // about size;
        long size = ItemDao.countOnlineBySellerCid(user.getId(), searchText, sellerCid);

        TMResult tmRes = new TMResult(items, (int) size, po);
        
        renderJSON(JsonUtil.getJson(tmRes));
    }
    
    

    //save or update
    public static void savePlanId(String itemIds, Long modelId,String planName,String borderColor,String activityTitle,
            double activityPrice,double counterPrice,String activityNameChinese,String activityNameEnglish,int planWidth,double originalPrice,
            String fontColor,Long planId,String backgroundColor,int days,int hours,int minutes) {
        
        if (StringUtils.isEmpty(itemIds) || NumberUtil.isNullOrZero(modelId)) {
            renderJSON("{}");
        }
        User user = getUser();
        
        if (user == null) {
            renderJSON("{}");
        }
        Long id = AssociateAction.saveAssociatePlan(itemIds, modelId, planName, user, borderColor, activityTitle,
                activityPrice, counterPrice, activityNameChinese, activityNameEnglish, planWidth, originalPrice,
                fontColor,planId,backgroundColor,days,hours,minutes);
        renderJSON(JsonUtil.getJson(id));
    }
    
    public static void toPut(String associateIds,Long planId){
        if(NumberUtil.isNullOrZero(planId) || StringUtils.isEmpty(associateIds)){
            renderJSON(JsonUtil.getJson("{}")); 
        }
        User user = getUser();
        List<BatchResultMsg> resultMsgs = AssociateAction.putPlanCheched(associateIds,planId, user);
        renderJSON(JsonUtil.getJson(resultMsgs));
    }
    
    
    public static void toPutToAll(Long planId){
        if(NumberUtil.isNullOrZero(planId) ){
           renderJSON(JsonUtil.getJson("{}")); 
        }
        User user = getUser();
        List<BatchResultMsg> resultMsgs = AssociateAction.putPlanAll(planId, user);
        renderJSON(JsonUtil.getJson(resultMsgs));
    }
    

    public static void listAssociatePlan(int type, int pn, int ps) {
        if(type > AssociateAction.TYPETHREE){
            renderJSON("{}");
        }
        
        List<Map<String, AssociatePlan>> list = new ArrayList<Map<String, AssociatePlan>>();
        User user = getUser();
        
        if (type > AssociateAction.TYPETHREE || user == null ) {
            renderJSON("{}");
        }
        
        PageOffset po = new PageOffset(pn, ps);
        
        list =  AssociateAction.getPlanHtmls(user, type, po.getOffset(), po.getPs());
        
        int planCount = AssociatePlan.getPlanCount(user.getId(), type);
        
        TMResult tmRes = new TMResult(list, planCount, po);

        renderJSON(JsonUtil.getJson(tmRes));
    }
    
//    public static void associatedCount(Long planId){
//        User user = getUser();
//        if(user == null || NumberUtil.isNullOrZero(planId)){
//            renderJSON("{}");
//        }
//        List<Long> listCount = AssociateAction.getAssociatedCount(planId, user);
//        
//        if(listCount == null){
//            //TODO
//            AssociatePlan plan = AssociatePlan.findByUserAndPlanId(user.getId(), planId);
//            plan.setType(AssociateAction.TYPETHREE);
//            plan.setId(planId);
//            plan.jdbcSave();
//            listCount = new ArrayList<Long>();
//        }
//        renderJSON(JsonUtil.getJson(listCount.size()));
//    }
    

    public static void findPlanById(Long planId) {
        User user = getUser();
        if (user == null) {
            renderJSON("{}");
        }

        AssociatePlan plan = AssociatePlan.findByUserAndPlanId(user.getId(), planId);
        renderJSON(plan);
    }

    public static void getPlanIdHtml(Long planId) {
        User user = getUser();
        if (NumberUtil.isNullOrZero(planId) || user == null) {
            renderJSON("{}");
        }
        String html = AssociateAction.getPlanHtml(user, planId);

        renderJSON(JsonUtil.getJson(html));
    }

    public static void deletePlanId(Long planId) {
        User user = getUser();
        if (user == null || NumberUtil.isNullOrZero(planId)) {
            renderJSON("{}");
        }

        if (NumberUtil.isNullOrZero(planId)) {
            renderJSON("{}");
        }

        AssociatePlan plan = AssociatePlan.findByUserAndPlanId(user.getId(), planId);
        
        plan.setType(AssociateAction.TYPEFOUR);
        plan.setId(planId);
        boolean flag = plan.jdbcSave();
        if (flag) {
            renderJSON(JsonUtil.getJson(flag));
        } else {
            log.error("[ can not delete planId ] : planId=" + planId);
            renderJSON(JsonUtil.getJson(flag));
        }
    }

    public static void stopPlanId(Long planId) {
        User user = getUser();

        if (user == null || NumberUtil.isNullOrZero(planId)) {
            renderJSON("{}");
        }
        List<BatchResultMsg> list = AssociateAction.stopPlanId(user, planId);

        renderJSON(JsonUtil.getJson(list));
    }
    
    public static void addNewAssociatedItems(String itemIids,Long planId,Long modelId ){
         if(StringUtils.isEmpty(itemIids) || NumberUtil.isNullOrZero(planId)){
             renderJSON("{}");
         }
         User user = getUser();
         List<BatchResultMsg> list = AssociateAction.addAssociatedItems(user, itemIids, planId, modelId);
         renderJSON(JsonUtil.getJson(list));
    }

    public static void AssociatedItems(Long planId) {
        User user = getUser();

        if (user == null || NumberUtil.isNullOrZero(planId)) {
            renderJSON("{}");
        }
        List<ItemPlay> itemList = AssociateAction.getAssociatedItemsByPlanId(planId, user);
        renderJSON(JsonUtil.getJson(itemList));
    }

    public static void getAssociatePlan(Long planId) {
        User user = getUser();
        if (user == null || NumberUtil.isNullOrZero(planId)) {
            renderJSON("{}");
        }
        AssociatePlan plan = new AssociatePlan().findByUserAndPlanId(user.getId(), planId);
        if (plan != null) {
            renderJSON(JsonUtil.getJson(plan));
        }
    }
    
    public static void deleteByNumId(Long planId,Long itemId){
        User user = getUser();
        BatchResultMsg resultMsg = AssociateAction.delPlanIdInOneItem(itemId, user, planId);
        renderJSON(resultMsg);
    }
    
    public static void deleteBatch(Long planId,String numIids){
        User user = getUser();
        if(user == null){
            renderJSON("{}");
        }
        List<BatchResultMsg> results = AssociateAction.batchDelPlanId(numIids, user, planId);
        
        renderJSON(JsonUtil.getJson(results));
    }
    
    
    /**
     * 刪除店鋪中所有寶貝中所有模板  
     * attention：尽量把详情更新到本地 谨慎使用 
     */
    public static void removeAllPlanInOneShop(String userNick){
        if(StringUtils.isEmpty(userNick)) {
            renderFailedJson("用戶名爲空");
        }
        User user = UserDao.findByUserNick(userNick);
        if(user == null) {
            renderFailedJson("用戶不存在");
        }
        List<BatchResultMsg> msgs = AssociateAction.removeAllPlanInOneShop(user);
        renderJSON(JsonUtil.getJson(msgs));
    }
    
    /**
     *  刪除店鋪內某一個模板
     *  attention：尽量把详情更新到本地 谨慎使用 
     */
    public static void removeOnePlanAllShop(String userNick,Long planId){
        if(StringUtils.isEmpty(userNick)) {
            renderFailedJson("用戶名为空");
        }
        if(NumberUtil.isNullOrZero(planId)){
            renderFailedJson("计划ID为空");
        }
        User user = UserDao.findByUserNick(userNick);
        if(user == null) {
            renderFailedJson("用戶不存在");
        }
        AssociatePlan plan = AssociatePlan.findByUserAndPlanId(user.getId(), planId);
        if(plan == null){
            renderFailedJson("计划不存在");
        }
        
        List<BatchResultMsg> msgs = AssociateAction.removeOnePlanAllShop(user,planId);
        
        renderJSON(JsonUtil.getJson(msgs));
    }
    
    /**
     * 刪除某個寶貝中某個模板
     * attention：尽量把详情更新到本地 谨慎使用 
     */
    public static void removeOnePlanOneItem(String userNick,Long planId,Long numIid){
        if(StringUtils.isEmpty(userNick)) {
            renderFailedJson("用戶名为空");
        }
        if(NumberUtil.isNullOrZero(planId)){
            renderFailedJson("计划ID为空");
        }
        if(NumberUtil.isNullOrZero(numIid)){
            renderFailedJson("宝贝ID为空");
        }
        User user = UserDao.findByUserNick(userNick);
        if(user == null) {
            renderFailedJson("用戶不存在");
        }
        AssociatePlan plan = AssociatePlan.findByUserAndPlanId(user.getId(), planId);
        if(plan == null){
            renderFailedJson("计划不存在");
        }
        ItemPlay item = ItemDao.findByNumIid(user.getId(), numIid);
        if(item == null){
            renderFailedJson("宝贝不存在");
        }
        BatchResultMsg resultMsg = AssociateAction.removeOnePlanOneItem(user, planId, numIid);
        renderJSON(JsonUtil.getJson(resultMsg));
    }
    
    /**
     * 发现 有些用户模版下面竟然没有tag标签....（zjm330993）
     */
    public static void removeOnePlanOneItemNoTag(Long numIid){
        User user = getUser();
        BatchResultMsg resultMsg = AssociateAction.removeOnePlanOneItemNoTag(user, numIid);
        renderJSON(JsonUtil.getJson(resultMsg));
    }
    
    /**
     * zjm330993  这个用户很奇怪，往他的宝贝中投放模版，都会把a标签忽略...
     */
    public static void removeAllPlanAllItems(){
        User user = getUser();
        if(user == null){
            renderFailedJson("用戶不存在");
        }
        List<BatchResultMsg> msgs = AssociateAction.removeAllPlanAllItem(user);
        renderJSON(JsonUtil.getJson(msgs));
    }

    public static void testAssociateIndex() {
        render("/associate/associateIndex.html");
    }

    public static void associate() {
        render("/associate/associate.html");
    }

    public static void myassociate() {
        render("/associate/myAssociate.html");
    }
}










