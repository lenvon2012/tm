package ppapi;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import models.paipai.PaiPaiUser;
import models.ppdazhe.PPDazheActive;
import models.ppdazhe.PPLtdItem;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.JsonUtil;

public class PaiPaiYingxiaoApi {
	
    private static final Logger log = LoggerFactory.getLogger(PaiPaiYingxiaoApi.class);
    
    public static final String TAG ="PaiPaiYingxiaoApi";
    
    public static class  PPaddLtdActiveApi extends PaiPaiApi<Rerror>{
    	    	
    	public String beginTime;
    	
    	public String endTime;
    	
    	public String activityName;
    	
    	Rerror res=new Rerror();
    	
    	public PPaddLtdActiveApi(PaiPaiUser user){
    		super(user);
    	}
    	
    	public PPaddLtdActiveApi(PaiPaiUser user,String beginTime,String endTime,String activityName){
    		super(user);
    		this.beginTime=beginTime;
    		this.endTime=endTime;
    		Date now=new Date();
    		this.activityName=String.valueOf(CommonUtils.Date2long(now));
    	}

		@Override
		public String getApiPath() {
			return ("/yingxiao/addLtdActive.xhtml");
		}

		@Override
		public boolean prepareRequest(HashMap<String, Object> params) {
			if(!StringUtils.isEmpty(beginTime)){
				params.put("beginTime", beginTime);
			}
		    if(!StringUtils.isEmpty(endTime)){
				params.put("endTime", endTime);
			}
            if (!StringUtils.isEmpty(activityName)){
            	params.put("activityName", activityName);
            }
			
			return false;
		}

		@Override
		public Rerror validResponse(String resp) {
	        if (StringUtils.isEmpty(resp)) {
	            return null;
	        }
	        JsonNode node = JsonUtil.readJsonResult(resp);
	        if (node == null || node.isMissingNode()) {
	            return null;
	        }
	        int errorCode = node.findValue("errorCode").getIntValue();
	        if (errorCode != 0) {
	            log.error("resp error: " + resp);
	       	    String errorMessage=node.findValue("errorMessage").getTextValue();     
	            res.errorMessage= errorMessage;
	            return res;
	        }

	        
	        res.activityId =node.findValue("activityId").getTextValue();
			
	        return res;
		}

		@Override
		public Rerror applyResult(Rerror res) {
			// TODO Auto-generated method stub
			return res;
		}
    	
    }
    
    public static class Rerror{
    	public String errorMessage;
    	public String activityId;
    }
    
    public static class PPmodifyLtdActiveApi extends  PaiPaiApi<String> {
    	
    	public String activityId;
    	
    	public String beginTime;
    	
    	public String endTime;
    	
    	public String activityName;
    	  	
    	public PPmodifyLtdActiveApi(PaiPaiUser user,String activityId,String beginTime,String endTime,String activityName){
    		super(user);
    		this.activityId=activityId;
    		this.beginTime=beginTime;
    		this.endTime=endTime;
    		this.activityName=activityName;
    	}
    	
    	

		@Override
		public String getApiPath() {
			
			return ("/yingxiao/modifyLtdActive.xhtml");
		}

		@Override
		public boolean prepareRequest(HashMap<String, Object> params) {
			
			if(!StringUtils.isEmpty(activityId)){
				params.put("activityId", activityId);
			}
			if(!StringUtils.isEmpty(beginTime)){
				params.put("beginTime", beginTime);
			}
			if(!StringUtils.isEmpty(endTime)){
				params.put("endTime", endTime);
			}
            if (!StringUtils.isEmpty(activityName)){
            	params.put("activityName", activityName);
            }
			
			return false;
		}

		@Override
		public String validResponse(String resp) {
	        if (StringUtils.isEmpty(resp)) {
	            return null;
	        }
	        JsonNode node = JsonUtil.readJsonResult(resp);
	        if (node == null || node.isMissingNode()) {
	            return null;
	        }
	        int errorCode = node.findValue("errorCode").getIntValue();
	        if (errorCode != 0) {
	            log.error("resp error: " + resp);
	       	    String errorMessage=node.findValue("errorMessage").getTextValue();     
	            return errorMessage;
	        }
	        
	        return null;
		}

		@Override
		public String applyResult(String res) {
			return res;
		}
    	    	
    }
    
    public static class PPdelLtdActiveApi extends PaiPaiApi<String>{
    	
    	public String activityId;
    	
    	public PPdelLtdActiveApi(PaiPaiUser user,String activityId){
    		super(user);
    		this.activityId=activityId;
    	}

		@Override
		public String getApiPath() {

			return ("/yingxiao/delLtdActive.xhtml");
		}

		@Override
		public boolean prepareRequest(HashMap<String, Object> params) {
			
			if(!StringUtils.isEmpty(activityId)){
				params.put("activityId", activityId);
			}
			return false;
		}

		@Override
		public String validResponse(String resp) {
			 if (StringUtils.isEmpty(resp)) {
		            return null;
		        }
		        JsonNode node = JsonUtil.readJsonResult(resp);
		        if (node == null || node.isMissingNode()) {
		            return null;
		        }
		        int errorCode = node.findValue("errorCode").getIntValue();
		        if (errorCode != 0) {
		            log.error("resp error: " + resp);
		       	    String errorMessage=node.findValue("errorMessage").getTextValue();     
		            return errorMessage;
		        }
		        
		        return null;
		}

		@Override
		public String applyResult(String res) {
			
			return res;
		}
    	
    }
    
    public static class PPsetLtdItemApi extends PaiPaiApi<String>{
    	
    	public int reqType;
    	
    	public String activityId;
    	
    	public String itemCode;
    	
    	public long buyLimit;
    	
    	public int discount;
    	
    	public PPsetLtdItemApi(PaiPaiUser user,int reqType,String activityId,String itemCode,
    			long buyLimit,int discount){
    		super(user);
    		this.reqType=reqType;
    		this.activityId=activityId;
    		this.itemCode=itemCode;
    		this.buyLimit=buyLimit;
    		this.discount=discount;
    		
    	}
    	public PPsetLtdItemApi(PaiPaiUser user,int reqType,String activityId,String itemCode,
    			int discount){
    		super(user);
    		this.reqType=reqType;
    		this.activityId=activityId;
    		this.itemCode=itemCode;
    		this.discount=discount;
    		
    	}
    	public PPsetLtdItemApi(PaiPaiUser user,int reqType,String activityId,String itemCode){
    		super(user);
    		this.reqType=reqType;
    		this.activityId=activityId;
    		this.itemCode=itemCode;
    		
    	}

		@Override
		public String getApiPath() {
			return ("/yingxiao/setLtdItem.xhtml");
		}

		@Override
		public boolean prepareRequest(HashMap<String, Object> params) {
			
			params.put("reqType", String.valueOf(reqType));

			if(!StringUtils.isEmpty(activityId)){
				params.put("activityId", activityId);
			}
			if(!StringUtils.isEmpty(itemCode)){
				params.put("itemCode", itemCode);
			}
			if(buyLimit!=0){
			params.put("buyLimit", String.valueOf(buyLimit));
			}
			if(discount!=0){
			params.put("discount", String.valueOf(discount));
			}
			
			return false;
		}

		@Override
		public String validResponse(String resp) {
	        if (StringUtils.isEmpty(resp)) {
	            return null;
	        }
	        JsonNode node = JsonUtil.readJsonResult(resp);
	        if (node == null || node.isMissingNode()) {
	            return null;
	        }
	        int errorCode = node.findValue("errorCode").getIntValue();
	        if (errorCode != 0) {
	            log.error("resp error: " + resp);
	       	    String errorMessage=node.findValue("errorMessage").getTextValue();     
	            return errorMessage;
	        }
	        
	        return null;
		}

		@Override
		public String applyResult(String res) {         
                return res;
 		}
    	
    }
    
    public static class PPgetLtdActiveApi extends PaiPaiApi<List<PPDazheActive>>{
    	
    	public String activityId;
    	
    	public List<PPDazheActive> resList;
    	
    	public PPgetLtdActiveApi(PaiPaiUser user) {
			super(user);
		}
    	
    	public PPgetLtdActiveApi(PaiPaiUser user,String activityId){
    		
    		super(user);
    		
    		this.activityId=activityId;
    	}

		@Override
		public String getApiPath() {
			return ("/yingxiao/getLtdActive.xhtml");
		}

		@Override
		public boolean prepareRequest(HashMap<String, Object> params) {
			if(!StringUtils.isEmpty(activityId)){
				params.put("activityId", activityId);
			}
			return false;
		}

		@Override
		public List<PPDazheActive> validResponse(String resp) {
            if (StringUtils.isEmpty(resp)) {
                return null;
            }
            JsonNode node = JsonUtil.readJsonResult(resp);
            if (node == null || node.isMissingNode()) {
                return null;
            }
            int errorCode = node.findValue("errorCode").getIntValue();
            if (errorCode != 0) {
                log.error("resp error: " + resp);
                return null;
            }
            int totalNum=node.findValue("totalNum").getIntValue();
            if(totalNum<=0){
            	return null;
            }
            JsonNode activityList = node.findValue("activityList");
            if (activityList == null || activityList.isArray() == false) {
                log.info("--activityList empty--" + resp);
            }
            List<PPDazheActive> list=new ArrayList<PPDazheActive>();
            
            try {
                JSONArray json = new JSONArray(activityList.toString());
                for (int i = 0; i < json.length(); i++) {
                    JSONObject obj = json.getJSONObject(i);
                    String activityId=obj.getString("activityId");
                    String activityName=obj.getString("activityName");
                    Long sellerUin=obj.getLong("sellerUin");
                    String beginTime=obj.getString("beginTime");
                    String endTime=obj.getString("endTime");
                    //int itemNum=obj.getInt("itemNum");
                    //String status="ACTIVE";
                    
                    PPDazheActive activity=new PPDazheActive(sellerUin,beginTime,endTime,activityName,activityId);
                    
                    list.add(activity);
                }
            
            } catch (JSONException e) {
                 log.error(e.getMessage(), e);      
            }
            return list;

		}

		@Override
		public List<PPDazheActive> applyResult(List<PPDazheActive> res) {

            if (res == null) {
                return resList;
            }
            return res;
        }   	
    }
    
    public static class PPgetLtdItemApi extends PaiPaiApi<List<PPLtdItem>>{
    	
    	public PaiPaiUser user;
    	
    	public String activityId;
    	
    	public List<PPLtdItem> resList = new ArrayList<PPLtdItem>();
    	
    	public PPgetLtdItemApi(PaiPaiUser user,String activityId){
    		
    		super(user);
    		
    		this.user=user;
    		
    		this.activityId=activityId;
    		
    	}

		@Override
		public String getApiPath() {
			return ("/yingxiao/getLtdItem.xhtml");
		}

		@Override
		public boolean prepareRequest(HashMap<String, Object> params) {
			if(!StringUtils.isEmpty(activityId)){
				params.put("activityId", activityId);
			}
			return false;
		}

		@Override
		public List<PPLtdItem> validResponse(String resp) {
            if (StringUtils.isEmpty(resp)) {
                return null;
            }
            JsonNode node = JsonUtil.readJsonResult(resp);
            if (node == null || node.isMissingNode()) {
                return null;
            }
            int errorCode = node.findValue("errorCode").getIntValue();
            if (errorCode != 0) {
                log.error("resp error: " + resp);
                return null;
            }
            int totalNum=node.findValue("totalNum").getIntValue();
            if(totalNum<=0){
            	return null;
            }
            String activityId=node.findValue("activityId").getTextValue();
            {
            	if(StringUtils.isEmpty(activityId)) 
            	return null;
            }
            JsonNode itemList = node.findValue("cBoLtdItems");
            if (itemList == null || itemList.isArray() == false) {
                log.info("--itemList empty--" + resp);
            }
            List<PPLtdItem> list=new ArrayList<PPLtdItem>();
            
            try {
                JSONArray json = new JSONArray(itemList.toString());
                for (int i = 0; i < json.length(); i++) {
                    JSONObject obj = json.getJSONObject(i);
                    String itemCode=obj.getString("sItemId");
//                    Long sellerUin=obj.getLong("sellerUin");
                    int buyLimit=obj.getInt("dwBuyLimit");
//                    int minPrice=obj.getInt("minPrice");
//                    String lastModifyTime=obj.getString("lastModifyTime");
//                    String addTime=obj.getString("addTime");
//                    String itemPreTime=obj.getString("itemPreTime");
//                    String itemBeginTime=obj.getString("itemBeginTime");
//                    String itemEndTime=obj.getString("itemEndTime");
//                    int itemStatus=obj.getInt("itemStatus");
//                    int stockNum=obj.getInt("stockNum");
                    int itemDiscount=obj.getInt("dwItemDiscount");
//                    int soldNum=obj.getInt("soldNum");
//                    int payNum=obj.getInt("payNum");
//                    log.info(activityId+itemCode+buyLimit+itemDiscount+"bbbbbbbbbbbbbbbbb");
                    
                    PPLtdItem item=new PPLtdItem(activityId, itemCode, user.getId(),buyLimit, itemDiscount);

                    list.add(item);
                }
            } catch (JSONException e) {
                log.error(e.getMessage(), e);
            }
			return list;
		}

		@Override
		public List<PPLtdItem> applyResult(List<PPLtdItem> res) {
            if (res == null) {
                return resList;
            }
            resList.addAll(res);
            return resList;
        } 
   	
    }
    
    public static class PPcreateManJianSongApi extends PaiPaiApi<String>{
    	
    	public PaiPaiUser user;
    	
    	public String beginTime;
    	
    	public String endTime;
    	
    	public String activityDesc;
    	
    	public String contentJson;
    	
    	public PPcreateManJianSongApi(PaiPaiUser user,String beginTime,String endTime,
    			String activityDesc,String contentJson){
    		super(user);
    		this.beginTime=beginTime;
    		this.endTime=endTime;
    		this.activityDesc=activityDesc;
    		this.contentJson=contentJson;
    	}

		@Override
		public String getApiPath() {
			return ("/yingxiao/createManJianSong.xhtml");
		}

		@Override
		public boolean prepareRequest(HashMap<String, Object> params) {
			if(!StringUtils.isEmpty(beginTime)){
				params.put("beginTime", beginTime);
			}
			if(!StringUtils.isEmpty(endTime)){
				params.put("endTime", endTime);
			}
			if(!StringUtils.isEmpty(activityDesc)){
				params.put("activityDesc", activityDesc);
			}
			if(!StringUtils.isEmpty(contentJson)){
				params.put("contentJson", contentJson);
			}
			return false;
		}

		@Override
		public String validResponse(String resp) {
	        if (StringUtils.isEmpty(resp)) {
	            return null;
	        }
	        JsonNode node = JsonUtil.readJsonResult(resp);
	        if (node == null || node.isMissingNode()) {
	            return null;
	        }
	        int errorCode = node.findValue("errorCode").getIntValue();
	        if (errorCode != 0) {
	            log.error("resp error: " + resp);
	       	    String errorMessage=node.findValue("errorMessage").getTextValue();     
	            return errorMessage;
	        }
	        
	        return null;
		}

		@Override
		public String applyResult(String res) {
			// TODO Auto-generated method stub
			return res;
		}
   	
    } 
    
    public static class PPupdateManJianSongApi extends PaiPaiApi<String>{
    	
    	public PaiPaiUser user;
    	
    	public String beginTime;
    	
    	public String endTime;
    	
    	public String activityDesc;
    	
    	public String contentJson;
    	
    	public PPupdateManJianSongApi(PaiPaiUser user,String beginTime,String endTime,
    			String activityDesc,String contentJson){
    		super(user);
    		this.beginTime=beginTime;
    		this.endTime=endTime;
    		this.activityDesc=activityDesc;
    		this.contentJson=contentJson;
    	}

		@Override
		public String getApiPath() {
			return ("/yingxiao/updateManJianSong.xhtml");
		}

		@Override
		public boolean prepareRequest(HashMap<String, Object> params) {
			if(!StringUtils.isEmpty(beginTime)){
				params.put("beginTime", beginTime);
			}
			if(!StringUtils.isEmpty(endTime)){
				params.put("endTime", endTime);
			}
			if(!StringUtils.isEmpty(activityDesc)){
				params.put("activityDesc", activityDesc);
			}
			if(!StringUtils.isEmpty(contentJson)){
				params.put("contentJson", contentJson);
			}
			return false;
		}

		@Override
		public String validResponse(String resp) {
	        if (StringUtils.isEmpty(resp)) {
	            return null;
	        }
	        JsonNode node = JsonUtil.readJsonResult(resp);
	        if (node == null || node.isMissingNode()) {
	            return null;
	        }
	        int errorCode = node.findValue("errorCode").getIntValue();
	        if (errorCode != 0) {
	            log.error("resp error: " + resp);
	       	    String errorMessage=node.findValue("errorMessage").getTextValue();     
	            return errorMessage;
	        }
	        
	        return null;
		}

		@Override
		public String applyResult(String res) {
			// TODO Auto-generated method stub
			return res;
		}   	
    }
    
    public static class PPdeleteManJianSongApi extends PaiPaiApi<String>{
    	
    	public PaiPaiUser user;
    	    	
    	public PPdeleteManJianSongApi(PaiPaiUser user){
    		super(user);
    	}

		@Override
		public String getApiPath() {
			return ("/yingxiao/deleteManJianSong.xhtml");
		}

		@Override
		public boolean prepareRequest(HashMap<String, Object> params) {

			return false;
		}

		@Override
		public String validResponse(String resp) {
	        if (StringUtils.isEmpty(resp)) {
	            return null;
	        }
	        JsonNode node = JsonUtil.readJsonResult(resp);
	        if (node == null || node.isMissingNode()) {
	            return null;
	        }
	        int errorCode = node.findValue("errorCode").getIntValue();
	        if (errorCode != 0) {
	            log.error("resp error: " + resp);
	       	    String errorMessage=node.findValue("errorMessage").getTextValue();     
	            return errorMessage;
	        }
	        
	        return null;
		}

		@Override
		public String applyResult(String res) {
			// TODO Auto-generated method stub
			return res;
		}
    	
    } 
    
    
    

}
