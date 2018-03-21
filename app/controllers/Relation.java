
package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.Transient;

import models.item.ItemPlay;
import models.relation.RelationModel;
import models.relation.RelationPlan;
import models.relation.RelationStaticModel;
import models.relation.RelationedItems;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import result.TMResult;
import transaction.JDBCBuilder;
import actions.DiagAction.BatchResultMsg;
import actions.RelationAction;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.HtmlUtil;
import com.ciaosir.client.utils.JsonUtil;

import dao.UserDao;
import dao.item.ItemDao;

public class Relation extends TMController {
    @Transient
    private static final Logger log = LoggerFactory.getLogger(Relation.class);
    
    public static void relationOp(String sid) {
        render();
    }

    public static void relationOper(String sid) {
        render("/Relation/relationOp2.html");
    }
    
    public static void myRelation() {
        render("/Relation/MyRelation.html");
    }

    public static void relation() {
        render();
    }

    public static void initStep1() {
        User user = getUser();
        //List<RelationPlan> rps = RelationPlan.find("userId = ?", user.getId()).fetch();
        List<RelationPlan> rps = RelationPlan.nativeQuery("userId = ?", user.getId());
        if (CommonUtils.isEmpty(rps)) {
            renderJSON("{}");
        }
        renderJSON(JsonUtil.getJson(rps));
    }

    public static void initStep2() {
        //new RelationModel(2l,"默认模板","http://qcdn.maijiatang.net/glyx/t.big_list.png",true,2,3).jdbcSave();
        //List<RelationModel> rms = RelationModel.findAll();
    	List<RelationModel> rms = RelationModel.findAllModel();
        if (CommonUtils.isEmpty(rms)) {
            RelationModel rm = new RelationModel(1l, "默认模板", "http://qcdn.maijiatang.net/glyx/t.big_list.png", true, 2,
                    3);
            if (rm.jdbcSave()) {
                rms.add(rm);
            } else {
                renderJSON("{}");
            }
        }
        renderJSON(JsonUtil.getJson(rms));
    }
    
    public static void queryItems(String title, String cid, String sellerCid, double itemPriceMin
    		, double itemPriceMax, int itemState, int relateState, int pn, int ps){
    	
    	User user = getUser();
    	
    	PageOffset po=new PageOffset(pn, ps);

    	//List<ItemPlay> itemList = ItemDao.findRelationByCondition(user.getId(), title, cid, sellerCid, itemPriceMin, itemPriceMax, itemState, relateState, po);
    	
    	//long count =ItemDao.countRelationByCondition(user.getId(), title, cid, sellerCid, itemPriceMin, itemPriceMax, itemState, relateState);
    	List<Long> relationed = RelationedItems.findAllRelationedNumIids();
    	List<ItemPlay> itemList = ItemDao.searchRelationed(user.getId(), po.getOffset(), po.getPs(), 
    			title, 0, relateState, sellerCid, cid, itemState, itemPriceMin, itemPriceMax);
    	if(CommonUtils.isEmpty(itemList)) {
    		renderJSON(new TMResult(new ArrayList<ItemPlay>(), 0, po));
    	}
    	long count = ItemDao.countRelationed(user.getId(), title, 1, sellerCid, cid, relateState,
    			itemState, itemPriceMin, itemPriceMax);
    	if(!CommonUtils.isEmpty(relationed)) {
    		for(ItemPlay itemPlay : itemList) {
    			itemPlay.setUnRelated();
    			if(relationed.contains(itemPlay.getNumIid())) {
    				itemPlay.setRelated();
    			}
    		}
    	}
    	renderJSON(new TMResult(itemList, (int)count, po));
    	
    }

    public static void getItemCount(QueryItemDao queryRule) {
        log.info("[dao : ]" + queryRule);
        User user = getUser();
        if (user == null || queryRule == null)
            renderText(0);
        long totalCount = queryRule.getTotalCount(user.getId());
        renderText(totalCount);
    }

    public static void getUnRelatedItems(int pn, int ps, String s) {
        User user = getUser();
        Set<Long> ids1 = RelationPlan.findByUser(user.getId());

        PageOffset po = new PageOffset(pn, ps);
        TMResult tmRes = ItemDao.findByUserAndSearchWithExcluded(user.getId(), s, po, ids1);
        renderJSON(JsonUtil.getJson(tmRes));
    }

    public static void getRelatedItems(int pn, int ps, String s, Long planId) {
        User user = getUser();
        Set<Long> ids1 = RelationPlan.findByUserAndPlanId(user.getId(), planId);
        log.info("related items: " + ids1);
        PageOffset po = new PageOffset(pn, ps);
        TMResult tmRes = ItemDao.findByUserAndSearchWithId(user.getId(), s, po, ids1);
        renderJSON(JsonUtil.getJson(tmRes));
    }

    public static void removeRelatedItems(String numIids, Long planId) {
        User user = getUser();
        if (StringUtils.isEmpty(numIids)) {
            ok();
        }

        String[] numIidsList = numIids.split(",");
        //RelationPlan rp = RelationPlan.find("userId = ? and id = ?", user.getId(), planId).first();
        RelationPlan rp = RelationPlan.singleQuery("userId = ? and id = ?", user.getId(), planId);
        if (rp != null) {
            for (String numIid : numIidsList) {
                rp.deleteNumIid(Long.parseLong(numIid));
                RelationAction.removeRelationItems(user, Long.parseLong(numIid));
            }
        }
        boolean isSuccess = rp.jdbcSave();

        TMResult.renderMsg(StringUtils.EMPTY);
    }

    public static void getItemList(QueryItemDao queryRule) {
        User user = getUser();
        if (user == null || queryRule == null)
            renderJSON("[]");
        List<ItemPlay> itemList = queryRule.getItemList(user.getId());
        String json = JsonUtil.getJson(itemList);
        renderJSON(json);
    }
    
    public static void getModel(String numIid,String picURL,String title,String price,String salesCount,double px,int index){

    	List<String> ModelList=RelationStaticModel.getModelList(numIid, picURL, title, price, salesCount,px);
    	
    	if(index>ModelList.size()){
    		index=0;
    		log.error("超过模板选择的最大值！");
    	}
    	
    	String ModelTable=ModelList.get(index);
    	
    	renderText(ModelTable);
    }
    
    public static void getModelList(){
    	String numIid="16150274657";
    	String picURL="http://img04.taobaocdn.com/imgextra/i4/73328087/T2ntJlXuhaXXXXXXXX_!!73328087.jpg";
    	String title="修身长款顶级皮草羽绒服";
    	String price="88.8";
    	String salesCount="66";
    	double px=1;
    	
    	List<String> ModelList=RelationStaticModel.getModelList(numIid, picURL, title, price,salesCount, px);
    	
    	List<String> List=new ArrayList<String>();
    	
    	for(String ModelTable :ModelList)
    	{
        	ModelTable=HtmlUtil.deleteTargetHref(ModelTable);
        	List.add(ModelTable);
    	}
    	
    	renderJSON(new TMResult(List));
    }

    public static void addRelationWithModel(String numIids, Long planId, String newName, Long modelId) {
        User user = getUser();
        if (StringUtils.isEmpty(numIids)) {
            ok();
        }

        String numIidsFormat = numIids.replaceAll(",", "!@#");
        //RelationPlan rp = RelationPlan.find("userId = ? and id = ?", user.getId(), planId).first();
        RelationPlan rp = RelationPlan.singleQuery("userId = ? and id = ?", user.getId(), planId);
        if (rp == null) {
            rp = new RelationPlan(newName, modelId, user.getId(), numIidsFormat);
        } else {
            if (modelId > 0) {
                rp.setModelId(modelId);
            }
            rp.setPlanName(newName);
            rp.addNumIids(numIidsFormat);
        }
        String[] ids = numIids.split(",");
        for (String numIid : ids) {
            System.out.println(numIid);
//            RelationAction.insertRelationItems(user, Long.parseLong(numIid));
        }
        boolean isSuccess = rp.jdbcSave();

        TMResult.renderMsg(StringUtils.EMPTY);
    }

    public static void addRelationWithNoNumIids(Long planId, String newPlanName, Long modelId) {
        User user = getUser();
        boolean isSuccess = true;
        //RelationPlan rp = RelationPlan.find("userId = ? and id = ?", user.getId(), planId).first();
        RelationPlan rp = RelationPlan.singleQuery("userId = ? and id = ?", user.getId(), planId);
        if (rp == null) {
            renderJSON(JsonUtil.getJson(new TMResult(false)));
        } else {
            if (modelId > 0) {
                rp.setModelId(modelId);
            }
            rp.setPlanName(newPlanName);
        }
        isSuccess = rp.jdbcSave();

        renderJSON(JsonUtil.getJson(new TMResult(isSuccess)));
    }

    public static void deletePlan(Long planId) {
        User user = getUser();
        int deleteNum = 0;
        //RelationPlan rp = RelationPlan.find("userId = ? and id = ?", user.getId(), planId).first();
        RelationPlan rp = RelationPlan.singleQuery("userId = ? and id = ?", user.getId(), planId);
        if (rp != null) {
            List<Long> numIids = rp.getNumIdList();
            if (!CommonUtils.isEmpty(numIids)) {
                for (Long numIid : numIids) {
                    RelationAction.removeRelationItems(user, numIid);
                }
            }
            //deleteNum = RelationPlan.delete("userId = ? and id = ?", user.getId(), planId);
            deleteNum = (int) JDBCBuilder.update(false, "delete from relation_plan where userId = ? and id = ?", user.getId(), planId);
        }

        if (deleteNum > 0) {
            renderJSON(JsonUtil.getJson(new TMResult(true)));
        } else {
            renderJSON(JsonUtil.getJson(new TMResult(false)));
        }
    }

    public static void addRelation(Long[] numIidArr,int index) {
        User user = getUser();
        if (numIidArr == null || numIidArr.length == 0)
            renderJSON(new TMResult("没有需要添加的宝贝！"));
        
        List<BatchResultMsg> msgList=new ArrayList<BatchResultMsg>();
        
        for (int i = 0; i < numIidArr.length; i++) {
            Long numIid = numIidArr[i];
            BatchResultMsg msg=RelationAction.insertRelationItems(user, numIid,index);
            if(msg!=null){
            	msgList.add(msg);
            }
        }
        if(!CommonUtils.isEmpty(msgList)){
        	renderJSON(new TMResult(msgList));
        }

    }

    public static void removeRelation(Long[] numIidArr) {
        User user = getUser();
        if (numIidArr == null || numIidArr.length == 0)
            renderJSON(new TMResult("没有需要添加的宝贝！"));
        
        List<BatchResultMsg> msgList=new ArrayList<BatchResultMsg>();
        
        for (int i = 0; i < numIidArr.length; i++) {
            Long numIid = numIidArr[i];
            BatchResultMsg msg=RelationAction.removeRelationItems(user, numIid);
            if(msg!=null){
            	msgList.add(msg);
            }
        }
        if(!CommonUtils.isEmpty(msgList)){
        	renderJSON(new TMResult(msgList));
        }
    }

//    @NoTransaction
    public static void getRelatedRecommends(long numIid) {
        if (numIid <= 0L) {
            renderJSON("[]");
        }

        User user = getUser();
        List<ItemPlay> list = RelationAction.recommendItems(user, numIid);
        renderJSON(JsonUtil.getJson(list));
    }

    public static void previewRelatedRecommends(long numIid) {
        User user = getUser();
        List<ItemPlay> list = RelationAction.recommendItems(user, numIid);
//        String generateHtml = RelationHtml.generateHtml(list);
//        renderHtml(generateHtml);
    }

    //查询宝贝，比如已关联，未关联，宝贝标题等条件
    public static class QueryItemDao {
        private String title;

        private String relationState;//关联的状态，有all,related,unrelated

        private int currentPage;//从1开始

        private int pageSize;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getRelationState() {
            return relationState;
        }

        public void setRelationState(String relationState) {
            this.relationState = relationState;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public void setCurrentPage(int currentPage) {
            this.currentPage = currentPage;
        }

        public long getTotalCount(Long userId) {
            long totalCount = 0;
            if ("all".equals(relationState)) {
                totalCount = ItemDao.countOnlineByUser(userId, title);
            } else if ("unrelated".equals(relationState)) {
                totalCount = ItemDao.countUnAdItemByUser(userId, title);
            }
            return totalCount;
        }

        public List<ItemPlay> getItemList(Long userId) {
            log.info("[page size : ]" + pageSize);
            List<ItemPlay> itemList = null;
            int startIndex = (currentPage - 1) * pageSize;
            if ("all".equals(relationState)) {
                itemList = ItemDao.findOnlineByUser(userId, startIndex, pageSize, title, 0);
            } else if ("unrelated".equals(relationState)) {
                itemList = ItemDao.findUnAdItemByUser(userId, startIndex, pageSize, title);
            } else
                itemList = new ArrayList<ItemPlay>();
            return itemList;
        }

        @Override
        public String toString() {
            return "QueryItemDao [title=" + title + ", relationState=" + relationState + ", currentPage=" + currentPage
                    + ", pageSize=" + pageSize + "]";
        }

    };

    public static void relateAllItems(int index) {
        User user = getUser();
        //List<ItemPlay> items = ItemDao.findRelationByAll(user.getId(), 0, 300, null, 0);
        // 查找未关联的宝贝
        List<ItemPlay> items = ItemDao.searchRelationedAll(user.getId(), 1);
        if(CommonUtils.isEmpty(items)){
        	return;
        }
        for (ItemPlay item : items) {
            RelationAction.insertRelationItems(user, item.getNumIid(),index);
        }
    }
    
    public static void removeAllItemRelation() {
    	User user = getUser();
        //List<ItemPlay> items = ItemDao.findUnRelationByAll(user.getId(), 0, 300, null, 0);
    	// 查找已关联的宝贝
        List<ItemPlay> items = ItemDao.searchRelationedAll(user.getId(), 0);
        if(CommonUtils.isEmpty(items)){
        	return;
        }
        for (ItemPlay item : items) {
            RelationAction.removeRelationItems(user, item.getNumIid());
        }
    }
    
    public static void relateLiebiaoItems(String title,String cid,String sellerCid,double itemPriceMin
    		,double itemPriceMax,int itemState,int relateState,int index){
    	
    	User user = getUser();   	

    	List<ItemPlay> itemList = ItemDao.findRelationByCondition(user.getId(), title, cid, sellerCid, itemPriceMin, itemPriceMax, itemState, relateState);
    	
        if(CommonUtils.isEmpty(itemList)){
        	return;
        }
        
        for(ItemPlay item:itemList){
        	if(!item.isRelated()){
        		RelationAction.insertRelationItems(user, item.getNumIid(),index);
        	}        	 
        }
    	
    }
    
    public static void removeLiebiaoItems(String title,String cid,String sellerCid,double itemPriceMin
    		,double itemPriceMax,int itemState,int relateState){
    	
    	User user = getUser();    	

    	List<ItemPlay> itemList = ItemDao.findRelationByCondition(user.getId(), title, cid, sellerCid, itemPriceMin, itemPriceMax, itemState, relateState);
    	
        if(CommonUtils.isEmpty(itemList)){
        	return;
        }
        
        for(ItemPlay item:itemList){
        	if(item.isRelated()){
        		RelationAction.removeRelationItems(user, item.getNumIid());
        	}      	 
        }
    	
    }
    
    /**
     * 删除某个宝贝所有的旧的关联模板
     */
    public static void removeItemAllRelation(String userNick, Long numIid){
        if (numIid == null || numIid == 0){
            renderJSON(new TMResult("没有numIid！"));
        }
        User user = UserDao.findByUserNick(userNick);
        if(user == null){
            renderJSON(new TMResult("没有该用户！"));
        }
        BatchResultMsg msg = RelationAction.removeRelationItemsAll(user, numIid);
        if(msg == null){
            renderJSON(new TMResult("删除失败"));
        }
        renderJSON(new TMResult("删除成功"));
    }
    
    /**
     * 删除店铺所有的旧的关联模板
     */
    public static void removeShopItemAllRelation(String userNick){
        User user = UserDao.findByUserNick(userNick);
        if(user == null){
            renderJSON(new TMResult("没有该用户！"));
        }
        // 查找已关联的宝贝
        List<ItemPlay> items = ItemDao.findByUserId(user.getId());
        if(CommonUtils.isEmpty(items)){
            renderJSON(new TMResult("没有找到宝贝！"));
        }
        for (ItemPlay item : items) {
            RelationAction.removeRelationItemsAll(user, item.getNumIid());
        }
        renderJSON(new TMResult("删除成功！"));
    }
    
}
