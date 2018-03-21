
package controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import models.paipai.PaiPaiUser;
import models.ppdazhe.ManJianSongActivity;
import models.ppdazhe.PPDazheActive;
import models.ppdazhe.PPLtdItem;
import models.ppdazhe.PPhongbao;
import models.updatetimestamp.updates.ItemUpdateTs;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ppapi.PaiPaiYingxiaoApi.PPaddLtdActiveApi;
import ppapi.PaiPaiYingxiaoApi.PPcreateManJianSongApi;
import ppapi.PaiPaiYingxiaoApi.PPdeleteManJianSongApi;
import ppapi.PaiPaiYingxiaoApi.PPsetLtdItemApi;
import ppapi.PaiPaiYingxiaoApi.Rerror;
import ppapi.models.PaiPaiItem;
import ppapi.models.PaiPaiItemCatPlay;
import result.TMResult;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;

import dao.paipai.PaiPaiItemDao;
import dao.paipai.PaiPaiUserDao;
import dao.ppdazhe.ManJianSongDao;
import dao.ppdazhe.PPgetLtdItemDao;


/**
 * 拍拍下面的 打折应用
 */
/**
 * @author haoyongzh
 *
 */
public class PaiPaiDiscount extends PaiPaiController {

    private static final Logger log = LoggerFactory.getLogger(PaiPaiDiscount.class);

    public static final String TAG = "PaiPaiDiscount";
    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public static void queryUserNick() {
        PaiPaiUser user = getUser();
        if (user == null) {
            renderText("");
        }
        renderText(user.getNick());
    }
    
    public static void isfirstlogin(){
        PaiPaiUser user = getUser();
        log.info("[get user]" + getUser());
        boolean result = false;
        ItemUpdateTs ts = ItemUpdateTs.fetchByUser(user.getId());
        if (ts != null) {
            renderJDJson(result);
        }
        else{
        	result=true;
        	renderJDJson(result);
        }
    }
    
    public static void Base_Index(){
    	render("dazhe/base_index.html");
    }

    public static void Index() {
    	render("dazhe/index.html");
    }
    
    public static void zhekou_1(){
    	render("dazhe/zhekou_1.html");
    }
    public static void zhekou_2(String activityId){
    	render("dazhe/zhekou_2.html", activityId);
    }
    public static void reviseItem(String activityId){
    	render("dazhe/reviseItem.html",activityId);
    }
    public static void addItem(String activityId){
    	render("dazhe/addItem.html",activityId);
    }
    public static void reviseAct(String activityId){
    	render("dazhe/reviseAct.html",activityId);
    }
    public static void addSuccess(){
    	render("dazhe/addSuccess.html");
    }
    public static void item_alladd(){
    	render("dazhe/itemalladd.html");
    }
    public static void award() {
    	render("dazhe/award.html");
    }
    
    public static void creatmanjiansong(){
    	render("dazhe/creatmanjiansong.html");
    }

    public static void manjiansongdetail(String activityString){
    	render("dazhe/manjiansongdetail.html",activityString);
    }
    
    public static void addLtdActive(long beginTime,long endTime,String activityName){
    	String b=sdf.format(new Date(beginTime));
    	String e=sdf.format(new Date(endTime));       
    	PaiPaiUser user=getUser();
    	Rerror result=new PPaddLtdActiveApi(user,b,e,activityName).call();

    	String status ="ACTIVE";
    	
    	if(result.activityId !=null){
    		PPDazheActive activity=new PPDazheActive(user.getId(), b, e, activityName,result.activityId,status);
    		boolean success = activity.jdbcSave();
    		if(!success){
    			String message="数据库存储出错";
      	      	renderError(message);
    		}
    	}
    	renderJDJson(result);
    }
    
    public static void modifyLtdActive(String activityId,long beginTime,long endTime,String activityName){
    	
    	String b=sdf.format(new Date(beginTime));
    	String e=sdf.format(new Date(endTime));       
    	PaiPaiUser user=getUser();

//    	String errorMessage=new PPmodifyLtdActiveApi(user,activityId,b,e,activityName).call();
//    	
//    	if(errorMessage!=null) renderError(errorMessage);
    	
    	PPDazheActive active =PPgetLtdItemDao.findActivityByActivityId(activityId);
    	
    	active.setActivityName(activityName);
    	active.setBeginTime(b);
    	active.setEndTime(e);
    	active.jdbcSave();
    }
    
    public static void delLtdActive(String activityId){
    	
    	PaiPaiUser user=getUser();
    	
//    	String errorMessage=new PPdelLtdActiveApi(user,activityId).call();
//    	
//    	if(errorMessage!=null) renderError(errorMessage);
    	
    	PPDazheActive active =PPgetLtdItemDao.findActivityByActivityId(activityId);
    	
    	if(StringUtils.isEmpty(active.getItemStrings())){
    		PPgetLtdItemDao.deleteActivityByActivityId(activityId);
    		return;
    	}
    	
    	List<PPLtdItem> LtdItemList=PPgetLtdItemDao.findLtdItemByactivityId(activityId);
    	
    	if(CommonUtils.isEmpty(LtdItemList)){
        	PPgetLtdItemDao.deleteActivityByActivityId(activityId);
        	return ;
    	}
    	
    	for(PPLtdItem ltdItem : LtdItemList){
    		int reqType=2;    		
    		String itemCode=ltdItem.getItemCode();
    		
    		String errorMessage=new PPsetLtdItemApi(user,reqType,activityId,itemCode).call();
    		
    		if(errorMessage!=null){
    			renderError(errorMessage);
    		}
    	}

    	PPgetLtdItemDao.deleteLtdItemByactivityId(activityId);
    	PPgetLtdItemDao.deleteActivityByActivityId(activityId);
    }
    
    public static void delUnActive(String activityId){
    	
    	PPgetLtdItemDao.deleteActivityByActivityId(activityId);
    	
    }
    
    
    public static void setLtdItem(String itemString){    	    	

    	PaiPaiUser user=getUser();
    	
        if (StringUtils.isEmpty(itemString)) {
            ok();
        }
        String[] idStrings = StringUtils.split(itemString, "!");
        if (ArrayUtils.isEmpty(idStrings)) {
            ok();
        }
    	for(String idString:idStrings){
    		
    		String[] item=StringUtils.split(idString, ",");
    		int reqType=Integer.valueOf(item[0]).intValue();
    		String activityId=item[1];
    		String itemCode=item[2];
    		int discount=Integer.valueOf(item[3]).intValue();
    		int buyLimit=0;//0表示没有限购
    		
    		String errorMessage=new PPsetLtdItemApi(user,reqType,activityId,itemCode,discount).call();
    		
    		if(errorMessage!=null){
    			renderError(errorMessage);
    		}
    		
    		if(reqType==1){

    			PPDazheActive activity =PPgetLtdItemDao.findActivityByActivityId(activityId);
    			activity.addItemStrings(itemCode);
    			activity.jdbcSave();
    			
    			PPLtdItem LtdItem =new PPLtdItem(itemCode, activityId, user.getId(), buyLimit, activity.getBeginTime(), activity.getEndTime(), discount);
    			
    			LtdItem.jdbcSave();
    		}
    		if(reqType==2){    			
    			PPDazheActive activity =PPgetLtdItemDao.findActivityByActivityId(activityId);
    			String[] itemStrings = StringUtils.split(activity.getItemStrings(), ",");

    			if(itemStrings==null) return;
    			String newItemStrings=null;
    			for(String  newItem:itemStrings){
    				if(!StringUtils.equals(newItem,itemCode)){
            			if(StringUtils.isEmpty(newItemStrings)) newItemStrings=newItem;
            			else newItemStrings+=","+newItem;
    				}
    			}
    			
    			activity.setItemStrings(newItemStrings);
    			activity.jdbcSave();    			
    			// 删除ltd数据库
    			PPgetLtdItemDao.deleteLtdItemByItemCode(itemCode);
    		}
    		if(reqType==3){
   			
    			PPLtdItem LtdItem = PPgetLtdItemDao.findByItemCode(itemCode);
    			LtdItem.setItemDiscount(discount);   			
    			boolean success=LtdItem.jdbcSave();
    			log.info("jdbcSave :"+success);
   			
    		}

    		    		
    	}
    	
    }
    
    public static void getManJianSongActivity(){
    	PaiPaiUser user=getUser();
    	ManJianSongActivity activity=ManJianSongDao.findBySellerUin(user.getId());
    	
    	if(activity==null){
    		return ;
    	}
    	
        long nowTime=CommonUtils.Date2long(new Date());
        long endTime=0;
    	try {
			endTime=sdf.parse(activity.getEndTime()).getTime();
		} catch (ParseException e) {
			log.info(e.toString());
		}
    	
    	if(endTime<=nowTime){
    		ManJianSongDao.deleteBySellerUin(user.getId());
    		return ;
    	}
    	
    	if(activity!=null){
    		renderJSON(new TMResult(activity));	
    	}
    }
    
    public static void getXiaoMan(){
    	PaiPaiUser user=getUser();
    	ManJianSongActivity activity=ManJianSongDao.findBySellerUin(user.getId());
    	
    	if(activity==null){
    		return ;
    	}
    	
        long nowTime=CommonUtils.Date2long(new Date());
        long endTime=0;
    	try {
			endTime=sdf.parse(activity.getEndTime()).getTime();
		} catch (ParseException e) {
			log.info(e.toString());
		}
    	
    	if(endTime<=nowTime){
    		ManJianSongDao.deleteBySellerUin(user.getId());
    		return ;
    	}
    	  	
    	try {
    		String Condition="每笔订单";
			JSONArray json = new JSONArray(activity.getContentJson());
            if (json == null ) {
            	log.info("--json empty--" );
            }
			
			for (int i = 0; i < json.length(); i++) {
				JSONObject ConditionJSON = json.getJSONObject(i);
				
				int costFlag=ConditionJSON.getInt("costFlag");
				
				if(costFlag==1){
					int costMoney = ConditionJSON.getInt("costMoney")/100; 
					Condition += "满"+costMoney+"元";
				}
				else{
					int costMoney = ConditionJSON.getInt("costMoney");
					Condition += "满"+costMoney+"件";
				}
				
				int favorableFlag=ConditionJSON.getInt("favorableFlag");
				
				if(favorableFlag==1){
					int freeMoney = ConditionJSON.getInt("freeMoney")/100;
					Condition += "减"+freeMoney+"元!";
				}
				if(favorableFlag==2){
					int freeRebate = ConditionJSON.getInt("freeRebate")/10;
					Condition += "打"+freeRebate+"折!";
				}
				if(favorableFlag==4){
					Condition += "送礼物!"; 
				}
				if(favorableFlag==16){
					Condition += "包邮!";
				}

			}
			
			renderJSON(new TMResult(Condition));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			log.error(e.toString());
		} 
    }
    
    public static void getLtdActive(int isactive,int pn,int ps){//***************************** 
    	PaiPaiUser user=getUser();
        PageOffset po = new PageOffset(pn, ps, 10);

        List<PPDazheActive> active=PPgetLtdItemDao.findOnActive(user.getId());
        
        long nowTime=CommonUtils.Date2long(new Date());
             
        if(!CommonUtils.isEmpty(active)){
        	 for(PPDazheActive act : active){
             	long endTime=0;
     			try {
     				endTime = sdf.parse(act.getEndTime()).getTime();
     			} catch (ParseException e) {
     			}
             	
             	if(endTime<=nowTime){
             		act.setStatus("UNACTIVE");
             		PPgetLtdItemDao.deleteLtdItemByactivityId(act.getActivityId());
             		act.jdbcSave();
             	}
             }
        	
        }
     
        if(isactive==1){
        	List<PPDazheActive> act_active=PPgetLtdItemDao.findListActivityOnActive(user.getId(), po);
        	long count =PPgetLtdItemDao.countActivityAllOnActive(user.getId());
         	TMResult tmResult =new TMResult(act_active,(int) count,po);
         	renderJSON(tmResult);
        }
        else {
        	List<PPDazheActive> unact_active=PPgetLtdItemDao.findListActivityUnActive(user.getId(), po);
        	long count =PPgetLtdItemDao.countActivityAllUnActive(user.getId());
         	TMResult tmResult =new TMResult(unact_active,(int) count,po);
         	renderJSON(tmResult);
        }
//        List<PPDazheActive> active=new PaiPaiYingxiaoApi.PPgetLtdActiveApi(user).call();
//
//        int count=active.size();
//        TMResult tmResult =new TMResult(active,(int) count,po);
//     	renderJSON(tmResult);
    }
    
    public static void getLtdItem(String activityId,int pn,int ps){
    	PaiPaiUser user=getUser();
    	
    	PageOffset po =new PageOffset(pn, ps);
    	
    	List<PPLtdItem> LtdItemList=PPgetLtdItemDao.findLtdItemByactivityId(activityId, po);
    	
    	long count = PPgetLtdItemDao.countLtdItemByactivityId(activityId);
    	
    	List<Item_Activity> item_acts=new ArrayList<PaiPaiDiscount.Item_Activity>();
    	
    	if(CommonUtils.isEmpty(LtdItemList)){
    		return ;
    	}
    	
    	for(PPLtdItem LtdItem :LtdItemList){
    		
    		Item_Activity item_act = new Item_Activity();
    		
    		PaiPaiItem item = PaiPaiItemDao.findByItemCode(user.getId(), LtdItem.getItemCode());
    		
			item_act.itemCode=item.getItemCode();
			item_act.itemName=item.getItemName();
			item_act.picLink=item.getPicLink();
			item_act.itemPrice=item.getItemPrice();
			item_act.activityId=LtdItem.getActivityId();
			item_act.itemDiscount=LtdItem.getItemDiscount();    
			
    		if(item_act!=null){
    			item_acts.add(item_act);
    		}
    		
    	}

            TMResult tmResult = new TMResult(item_acts,(int) count, po);
            renderJSON(tmResult);
    		
    	  	
    }
    
    public static void searchItems(int status,String catId,int pn,int ps){

    	PaiPaiUser user=getUser();

    	long count=0;
    	PageOffset po = new PageOffset(pn, ps);    	
    	
    	List<PaiPaiItem> items=null;
        if (status == 0 || catId == null) {
    		items=PaiPaiItemDao.findOnSaleByUserId(user.getId(),po);    	       	
        	count =PaiPaiItemDao.countOnSaleByUserId(user.getId());  
        }
        else if(status==1){
           	items = PaiPaiItemDao.findOnSaleByUserCatId(user.getId(), Long.valueOf(catId),po);
          	count=PaiPaiItemDao.countOnSaleByUserCatId(user.getId(), Long.valueOf(catId));
        }
        else if(status==2){
        	items = PaiPaiItemDao.findOnSaleByKeywords(user.getId(), catId, po);
        	count = PaiPaiItemDao.countOnSaleByKeywords(user.getId(), catId);
        }


        if (CommonUtils.isEmpty(items)) {
        	renderJSON( new TMResult("亲， 您还没有上架宝贝哟！！！！！"));
        }
    	
    	List<Item_Activity> item_acts=new ArrayList<PaiPaiDiscount.Item_Activity>();

    	for(PaiPaiItem item : items) {
    		Item_Activity item_act = new Item_Activity();
    		long test =PPgetLtdItemDao.countOnSaleByItemCode(item.getItemCode());
    		if(test!=0){
    			PPLtdItem LtdItem=PPgetLtdItemDao.findByItemCode(item.getItemCode());
    			item_act.itemCode=item.getItemCode();
    			item_act.itemName=item.getItemName();
    			item_act.picLink=item.getPicLink();
    			item_act.itemPrice=item.getItemPrice();
    			item_act.activityId=LtdItem.getActivityId();
    			item_act.itemDiscount=LtdItem.getItemDiscount();
    			
    		}
    		else{
    			item_act.itemCode=item.getItemCode();
    			item_act.itemName=item.getItemName();
    			item_act.picLink=item.getPicLink();
    			item_act.itemPrice=item.getItemPrice();
    			item_act.itemDiscount=0;//表示该商品没有参加打折活动
    		}
    		if(item_act!=null){
    			item_acts.add(item_act);
    		}
    		
    	}
    	
        TMResult tmResult = new TMResult(item_acts,(int) count, po);
        renderJSON(tmResult);
        
    }
    
    public static void searchAddItems(String activityId,int status,String catId,int pn,int ps){

    	PaiPaiUser user=getUser();

    	long count=0;
    	PageOffset po = new PageOffset(pn, ps);    	
    	
    	List<PaiPaiItem> items=null;
        if (status == 0 || catId == null) {
    		items=PaiPaiItemDao.findOnSaleByUserId(user.getId(),po);    	       	
        	count =PaiPaiItemDao.countOnSaleByUserId(user.getId());  
        }
        else if(status==1){
           	items = PaiPaiItemDao.findOnSaleByUserCatId(user.getId(), Long.valueOf(catId),po);
          	count=PaiPaiItemDao.countOnSaleByUserCatId(user.getId(), Long.valueOf(catId));
        }
        else if(status==2){
        	items = PaiPaiItemDao.findOnSaleByKeywords(user.getId(), catId, po);
        	count = PaiPaiItemDao.countOnSaleByKeywords(user.getId(), catId);
        }

        if (CommonUtils.isEmpty(items)) {
        	renderJSON( new TMResult("亲， 您还没有上架宝贝哟！！！！！"));
        }
    	
    	List<Item_Activity> item_acts=new ArrayList<PaiPaiDiscount.Item_Activity>();

    	for(PaiPaiItem item : items) {
    		Item_Activity item_act = new Item_Activity();
    		long test =PPgetLtdItemDao.countOnSaleByItemCode(item.getItemCode());
    		if(test!=0){
    			PPLtdItem LtdItem=PPgetLtdItemDao.findByItemCode(item.getItemCode());
    			item_act.itemCode=item.getItemCode();
    			item_act.itemName=item.getItemName();
    			item_act.picLink=item.getPicLink();
    			item_act.itemPrice=item.getItemPrice();
    			item_act.activityId=LtdItem.getActivityId();
    			item_act.itemDiscount=LtdItem.getItemDiscount();
    			if(StringUtils.equals(activityId, LtdItem.getActivityId())){
    				item_act.isthisActivity=true;
    			}
    		}
    		else{
    			item_act.itemCode=item.getItemCode();
    			item_act.itemName=item.getItemName();
    			item_act.picLink=item.getPicLink();
    			item_act.itemPrice=item.getItemPrice();
    			item_act.itemDiscount=0;//表示该商品没有参加打折活动
    		}
    		if(item_act!=null){
    			item_acts.add(item_act);
    		}
    		
    	}
    	
        TMResult tmResult = new TMResult(item_acts,(int) count, po);
        renderJSON(tmResult);
        
    }
    
    public static class Item_Activity{
    	public String itemCode;
    	public String itemName;
    	public String picLink;
    	public int itemPrice;
    	public String activityId;
    	public int itemDiscount;
    	public boolean isthisActivity=false;
    }
    
    public static void sellerCatCount(){
    	PaiPaiUser user =getUser();
    	Set<Long> catset = PaiPaiItemDao.catIdSet(user.getId());
    	List<PaiPaiItemCatPlay> itemCatList=new ArrayList<PaiPaiItemCatPlay>();
    	for(Long cid : catset){
    		PaiPaiItemCatPlay itemCat=PaiPaiItemCatPlay.findCatPlay(user,cid);
    		if(itemCat!=null){
    			itemCatList.add(itemCat);
    		}
    	}
        TMResult tmResult = new TMResult(itemCatList);
        renderJSON(tmResult);
    }
    
    public static void showReviseAct(String activityId){
    	PPDazheActive activity=PPgetLtdItemDao.findActivityByActivityId(activityId);
        TMResult tmResult = new TMResult(activity);
        renderJSON(tmResult);
    }
    
    public static void debugApiDelete(){
    	PaiPaiUser user=getUser();
    	List<PPDazheActive> activityList=PPgetLtdItemDao.findListActivityByUserId(user.getId());
    	if(!CommonUtils.isEmpty(activityList)){
    		for(PPDazheActive activity : activityList){
//            	String errorMessage=new PPdelLtdActiveApi(user,activity.getActivityId()).call();            	
//            	if(errorMessage!=null) renderError(errorMessage);
//            	else {
//            		PPgetLtdItemDao.deleteActivityByActivityId(activity.getActivityId());
//            		PPgetLtdItemDao.deleteLtdItemByactivityId(activity.getActivityId());
//            	}
    	    	List<PPLtdItem> LtdItemList=PPgetLtdItemDao.findLtdItemByactivityId(activity.getActivityId());
    	    	
    	    	for(PPLtdItem ltdItem : LtdItemList){
    	    		int reqType=2;    		
    	    		String itemCode=ltdItem.getItemCode();
    	    		
    	    		String errorMessage=new PPsetLtdItemApi(user,reqType,activity.getActivityId(),itemCode).call();
    	    		
    	    		if(errorMessage!=null){
    	    			renderError(errorMessage);
    	    		}
    	    	}
    	    	PPgetLtdItemDao.deleteLtdItemByactivityId(activity.getActivityId());
    	    	PPgetLtdItemDao.deleteActivityByActivityId(activity.getActivityId());
    		}    		
    	}
    	renderJSON(new TMResult("删除活动成功"));
    }
    
    public static void itemAllAdd(long beginTime,long endTime,String activityName,int discount){
    	String b=sdf.format(new Date(beginTime));
    	String e=sdf.format(new Date(endTime));       
    	PaiPaiUser user=getUser();
    	Rerror result=new PPaddLtdActiveApi(user,b,e,activityName).call();

    	String status ="ACTIVE";
    	
    	if(result.activityId !=null){
    		PPDazheActive activity=new PPDazheActive(user.getId(), b, e, activityName,result.activityId,status);
    		boolean success = activity.jdbcSave();
    		if(!success){
    			String message="数据库存储出错";
      	      	renderError(message);
    		}
    	}
    	else{
    		renderJDJson(result);
    	}
    	
    	String itemCodes="";
    	List<PPLtdItem> LtdItemList=PPgetLtdItemDao.findItemListByUserId(user.getId());
    	if(!CommonUtils.isEmpty(LtdItemList)){
        	for(PPLtdItem ltdItem : LtdItemList){
        		if(itemCodes==""){
        			itemCodes="'"+ltdItem.getItemCode()+"'";
        		}
        		else{
        			itemCodes+=","+"'"+ltdItem.getItemCode()+"'";
        		}
        	}
    		
    	}
    	List<PaiPaiItem> itemList=null;
    	
    	if(itemCodes==""){
    		itemList=PaiPaiItemDao.findOnSaleByUserId(user.getId());
    	}
    	else{
    		itemList=PaiPaiItemDao.findOnSaleOutOfItemCode(user.getId(), itemCodes);
    	}
    	
        if(itemList==null){
        	renderJSON(new TMResult("亲，您没有在售的宝贝哦！"));
        }
        
        for(PaiPaiItem item :itemList){
    		int reqType=1;
    		String activityId=result.activityId;
    		String itemCode=item.getItemCode();
    		int dis=discount;
    		int buyLimit=0;//0表示没有限购
    		
    		String errorMessage=new PPsetLtdItemApi(user,reqType,activityId,itemCode,dis).call();
    		
    		if(errorMessage!=null){
    			renderError(errorMessage);
    		}
    		
			PPDazheActive activity =PPgetLtdItemDao.findActivityByActivityId(activityId);
			activity.addItemStrings(itemCode);
			activity.jdbcSave();
			
			PPLtdItem LtdItem =new PPLtdItem(itemCode, activityId, user.getId(), buyLimit, activity.getBeginTime(), activity.getEndTime(), discount);
			
			LtdItem.jdbcSave();
        }
    }
    
    public static void setPPhongbao(Long requestId){
    	PaiPaiUser user =getUser();
    	
    	Long userId=user.getId();
    	
    	if(userId==0||requestId==0){
    		renderError("邀请人或者被邀请人为空！");
    	}
    	
    	PaiPaiUser requestUser =PaiPaiUserDao.findById(requestId);
    	
    	if(requestUser==null){
    		renderError("邀请人不是紫金折扣的用户！");
    	}
    	if(requestId.longValue()==userId.longValue()){
    		renderError("邀请人不能是自己！");
    	}
    	
    	int status = 0;
    	
    	PPhongbao userInfo = new PPhongbao(userId, requestId, status);
    	
    	boolean success=userInfo.jdbcSave();
    	if(!success) {
    		renderError("数据库存储失败！请联系客服~");
    		}
    }
    
    
    public static void createManJianSong(String activityString,String DisString,
    		String PreString,String BaoString){
    	
    	PaiPaiUser user = getUser();
    	
    	String JsonString="";
    	JsonString+="[";
    	
    	String[] actStr=StringUtils.split(activityString, ",");
    	String activityDesc=actStr[0];
    	long bT=Long.valueOf(actStr[1]).longValue();
    	long eT=Long.valueOf(actStr[2]).longValue();
    	String beginTime=sdf.format(new Date(bT));
    	String endTime=sdf.format(new Date(eT));
    	
    	if(!StringUtils.isEmpty(DisString)){
    		String[] arraydisStr=StringUtils.split(DisString, "!");
    		for(String disStrs :arraydisStr){
    			String[] disStr=StringUtils.split(disStrs, ",");
    			String costFlag=disStr[0];
    			String costMoney=disStr[1];
    			String favorableFlag=disStr[2];
    			String freeMoney="";
    			String freeRebate="";
    			String JsonDis="";
    			if(Long.valueOf(favorableFlag)==1){
    				freeMoney=disStr[3];
        			JsonDis="{costFlag:\""+costFlag+"\","+"costMoney:\""+costMoney+"\","
        					+"favorableFlag:\""+favorableFlag+"\","+"freeMoney:\""+freeMoney+"\"}";
    				}
    			else{
    				freeRebate=disStr[3];
        			JsonDis="{costFlag:\""+costFlag+"\","+"costMoney:\""+costMoney+"\","
        					+"favorableFlag:\""+favorableFlag+"\","+"freeRebate:\""+freeRebate+"\"}";
    				}

    			if(StringUtils.equals(JsonString, "[")){
    				JsonString+=JsonDis;
    			}
    			else{
    				JsonString+=","+JsonDis;
    			}

    		}
    	}
    	
    	if(!StringUtils.isEmpty(PreString)){
    		String[] arrayPreStr=StringUtils.split(PreString, "!");
    		for(String preStrs :arrayPreStr){
    			String[] preStr=StringUtils.split(preStrs, ",");
    			String costFlag=preStr[0];
    			String costMoney=preStr[1];
    			String favorableFlag=preStr[2];
    			String presentName=preStr[3];
    			String presentUrl=preStr[4];
    			
    			String	JsonPre="{costFlag:\""+costFlag+"\","+"costMoney:\""+costMoney+"\","
        			    +"favorableFlag:\""+favorableFlag+"\","+"presentName:\""+presentName+"\","
    					+"presentUrl:\""+presentUrl+"\"}";


    			if(StringUtils.equals(JsonString, "[")){
    				JsonString+=JsonPre;
    			}
    			else{
    				JsonString+=","+JsonPre;
    			}

    		}
    	}
    	
    	if(!StringUtils.isEmpty(BaoString)){
    		String[] arrayBaoStr=StringUtils.split(BaoString, "!");
    		for(String baoStrs :arrayBaoStr){
    			String[] baoStr=StringUtils.split(baoStrs, ",");
    			String costFlag=baoStr[0];
    			String costMoney=baoStr[1];
    			String favorableFlag=baoStr[2];
    			
    			String	JsonBao="{costFlag:\""+costFlag+"\","+"costMoney:\""+costMoney+"\","
        			    +"favorableFlag:\""+favorableFlag+"\"}";


    			if(StringUtils.equals(JsonString, "[")){
    				JsonString+=JsonBao;
    			}
    			else{
    				JsonString+=","+JsonBao;
    			}

    		}
    	}
    	
    	JsonString+="]";
    	String contentJson=JsonString;

    	String errorMessage=new PPcreateManJianSongApi(user, beginTime, endTime, activityDesc, contentJson).call();
    	
    	if(errorMessage!=null){
    		log.error(errorMessage);
			renderError(errorMessage);
		}
    	
    	ManJianSongActivity activity=new ManJianSongActivity(user.getId(), beginTime, endTime, activityDesc, contentJson);
    	
    	boolean success=activity.jdbcSave();
    	if(!success){
    		renderError("数据库存储失败");
    	}
    		
    }
    
    public static void DeleteManJianSong(){
    	PaiPaiUser user=getUser();
    	
    	String errorMessage=new PPdeleteManJianSongApi(user).call();
    	if(errorMessage!=null){
    		log.error(errorMessage);
			renderError(errorMessage);
		}
    	
    	ManJianSongDao.deleteBySellerUin(user.getId());
    }

}
