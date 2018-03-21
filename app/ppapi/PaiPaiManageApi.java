package ppapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import models.paipai.PaiPaiUser;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.utils.JsonUtil;

public class PaiPaiManageApi {

    private static final Logger log = LoggerFactory.getLogger(PaiPaiManageApi.class);
    
    public static final String TAG ="PaiPaiManageApi";
    
    public static class PPmodifyItemStockApi extends PaiPaiApi<String>{
    	
    	public String itemCode;
    	
    	public String stockJsonList;
    	
    	
    	public PPmodifyItemStockApi(PaiPaiUser user,String itemCode,String stockJsonList){
    		super(user);
    		this.itemCode=itemCode;
    		this.stockJsonList=stockJsonList;
    	}

		@Override
		public String getApiPath() {
			return ("/item/editItemStock.xhtml");
		}

		@Override
		public boolean prepareRequest(HashMap<String, Object> params) {
			if(!StringUtils.isEmpty(itemCode)){
				params.put("itemCode", itemCode);
			}
			if(!StringUtils.isEmpty(stockJsonList)){
				params.put("stockJsonList", stockJsonList);
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
    
    public static class PPmodifyItemStateApi extends PaiPaiApi<List<modifyResult>>{

    	public String itemCodeList;
    	
    	public String itemState;
    	
    	List<modifyResult> resList=new ArrayList<modifyResult>(); 
    	
    	public PPmodifyItemStateApi(PaiPaiUser user,String itemCodeList,String itemState){
    		super(user);
    		this.itemCodeList=itemCodeList;
    		this.itemState=itemState;
    	}
    	
		@Override
		public String getApiPath() {
			return ("/item/modifyItemState.xhtml");
		}

		@Override
		public boolean prepareRequest(HashMap<String, Object> params) {
			if(!StringUtils.isEmpty(itemCodeList)){
				params.put("itemCodeList", itemCodeList);
			}
			if(!StringUtils.isEmpty(itemState)){
				params.put("itemState", itemState);
			}
			
			return false;
		}

		@Override
		public List<modifyResult> validResponse(String resp) {
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
	       	    modifyResult result=new modifyResult();
	       	    result.errorMessage=errorMessage;
	         	resList.add(result);
	            return resList;
	        }
            JsonNode modifyResult = node.findValue("modifyResult");
            if (modifyResult == null || modifyResult.isArray() == false) {
                log.info("--modifyResult empty--" + resp);
            }
            
            try{
            	JSONArray json = new JSONArray(modifyResult.toString());
                for (int i = 0; i < json.length(); i++) {
                    JSONObject obj = json.getJSONObject(i);
                    String itemCode =obj.getString("itemCode");
                    int result=obj.getInt("result");
                    String itemState=obj.getString("itemState");
                    String stateDesc=obj.getString("stateDesc");
                    
                    modifyResult Result=new modifyResult(itemCode, result, itemState, stateDesc);
                    
                    resList.add(Result);
                }
            	
            } catch (JSONException e) {
                log.error(e.getMessage(), e);      
           }
	        
	        return resList;
		}

		@Override
		public List<modifyResult> applyResult(List<modifyResult> res) {
			return res;
		}
    	
    }
    
    public static class modifyResult{
    	public String itemCode;
    	public int result;
    	public String itemState;
    	public String stateDesc;
    	public String errorMessage=null;
    	
    	public modifyResult(){
    		
    	}
    	
    	public modifyResult(String itemCode,int result,String itemState,String stateDesc){
    		this.itemCode=itemCode;
    		this.result=result;
    		this.itemState=itemState;
    		this.stateDesc=stateDesc;
    	}
    }
    
    
}
