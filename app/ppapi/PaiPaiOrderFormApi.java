package ppapi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import models.comment.Comments;
import models.paipai.PaiPaiUser;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.utils.DateUtil;
import com.ciaosir.client.utils.JsonUtil;

public class PaiPaiOrderFormApi {

    private static final Logger log = LoggerFactory.getLogger(PaiPaiOrderFormApi.class);
    
    public static final String TAG ="PaiPaiOrderFormApi";
    
    public static final int ITEM_PAGE_SIZE = 20;
    
    //同步30天内的成功订单   
    public static class getUnCommentOrderApi extends PaiPaiApi<List<Comments>>{
    	
    	public PaiPaiUser user;
    	
    	public int pn =1;
    	        
        public List<Comments> commentList = new ArrayList<Comments>();
        
        public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        public Date end = new Date();
        public Date start = new Date(end.getTime() - DateUtil.THIRTY_DAYS);
        
        String timeBegin=sdf.format(start);
        String timeEnd=sdf.format(end);
    	
    	public getUnCommentOrderApi(PaiPaiUser user){
    		super(user);
    		this.user=user;
    	}

		@Override
		public String getApiPath() {
			return ("/deal/sellerSearchDealList.xhtml");
		}

		@Override
		public boolean prepareRequest(HashMap<String, Object> params) {
//			params.put("dealRateState", dealRateState);
//			String dealState="DS_DEAL_END_NORMAL";
//			params.put("dealState", dealState);
            params.put("pageIndex", String.valueOf(pn));
            params.put("pageSize", String.valueOf(ITEM_PAGE_SIZE));
			params.put("timeType", "CREATE");
			params.put("timeBegin", timeBegin);
			params.put("timeEnd", timeEnd);
			params.put("dealRateState", "101");

			return false;
		}

		@Override
		public List<Comments> validResponse(String resp) {
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
	        }
	        
            int countTotal = node.findValue("countTotal").getIntValue();
            if (countTotal > 0 && pn < (countTotal + ITEM_PAGE_SIZE - 1) / ITEM_PAGE_SIZE) {
                this.iteratorTime = 1;
                this.pn++;
            }
	        JsonNode dealList= node.findValue("dealList");
            if (dealList == null || dealList.isArray() == false) {
                log.info("--dealList empty--" + resp);
            }
            
            try {
				JSONArray json = new JSONArray(dealList.toString());
				 for (int i = 0; i < json.length(); i++) {
	                    JSONObject obj = json.getJSONObject(i);
	                    
        	        	Long userId=user.getId();
        	        	
        	        	String nick = user.getNick();
        	        	
        	        	String result = obj.getString("dealCode");//把这个字段当dealCode存
        	        	
        	        	String buyerName=obj.getString("buyerName");
        	        	
        	        	Comments comment=new Comments(userId, 0L, 0L, result,"", nick, buyerName);
        	        	
        	        	commentList.add(comment);
	                    
//	                    String dealRateState=obj.getString("dealRateState");
//	                    if(StringUtils.equals(dealRateState, "DEAL_RATE_BUYER_NO_SELLER_NO")||
//	        	        		StringUtils.equals(dealRateState, "DEAL_RATE_BUYER_DONE_SELLER_NO")){
//	        	        	
//	        	        	Long userId=user.getId();
//	        	        	
//	        	        	String nick = user.getNick();
//	        	        	
//	        	        	String result = obj.getString("dealCode");//把这个字段当dealCode存
//	        	        	
//	        	        	String buyerName=obj.getString("buyerName");
//	        	        	
//	        	        	Comments comment=new Comments(userId, 0L, 0L, result,"", nick, buyerName);
//	        	        	
//	        	        	commentList.add(comment);
//	                    }
	                    
				 }
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				log.warn(e.getMessage(), e);
			}

			return commentList;
		}

		@Override
		public List<Comments> applyResult(List<Comments> res) {
			// TODO Auto-generated method stub
			return res;
		}

    }
    
    public static class evaluateDealApi extends PaiPaiApi<String>{

    	String dealList;
    	
    	String evalContent;
    	
    	public evaluateDealApi(PaiPaiUser user,String dealList,String evalContent){
    		super(user);
    		this.dealList=dealList;
    		this.evalContent=evalContent;
    	}
    	
		@Override
		public String getApiPath() {
			return ("/evaluation/evaluateDeal.xhtml");
		}

		@Override
		public boolean prepareRequest(HashMap<String, Object> params) {
			if(!StringUtils.isEmpty(dealList)){
				params.put("dealList", dealList);
			}
			if(!StringUtils.isEmpty(evalContent)){
				params.put("evalContent", evalContent);
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
    
}
