package controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import models.paipai.PaiPaiUser;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ppapi.PaiPaiOrderFormApi.evaluateDealApi;
import ppapi.models.PaiPaiTradeDisplay;
import ppapi.models.PaiPaiTradeItem;
import result.TMResult;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;

import dao.paipai.PaiPaiTradeDao;

/**
 * @author haoyongzh
 *
 */
public class PaiPaiOrderForm extends PaiPaiController{
	
    private static final Logger log = LoggerFactory.getLogger(PaiPaiOrderForm.class);
    
    public static final String TAG = "PaiPaiOrderForm";
    
    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public static void index(){
    	render("paipaiorder/orderform.html");
    }
    
    public static void queryOrders(String buyerName,String dealState,String createTime,String itemName
    		,String dealRateState,String dealCode,String buyerUin,int pn,int ps){
    	
    	PaiPaiUser user=getUser();
    	
    	PageOffset po=new PageOffset(pn, ps);
    	
    	List<PaiPaiTradeDisplay> TradeDisplayList = PaiPaiTradeDao.findByOrderCondition(user.getId(), buyerName, dealState, createTime, itemName, dealRateState, dealCode,buyerUin,po);
    	
    	long count =PaiPaiTradeDao.countByOrderCondition(user.getId(), buyerName, dealState, createTime, itemName, dealRateState, dealCode,buyerUin);
    	
    	if(CommonUtils.isEmpty(TradeDisplayList)){
    		return ;
    	}
    	
    	for(PaiPaiTradeDisplay TradeDisplay:TradeDisplayList){
    		List<PaiPaiTradeItem> TradeItemList = PaiPaiTradeDao.findTradeItemByDealCode(user.getId(),TradeDisplay.getDealCode());
    		TradeDisplay.setItemList(TradeItemList);
    	}
  
    	renderJSON(new TMResult(TradeDisplayList,(int) count, po));
    }
    
    public static void doEvaluate(List<String> evaluateList,String evaluatecontent){
    	
        PaiPaiUser user = getUser();
        
        if (evaluateList == null || evaluateList.isEmpty()) {
            renderError("亲，请先选择要评价的订单!");
        }
        
        List<evalResult> resultList=new ArrayList<evalResult>();
        
        for(String dealCode : evaluateList){
        	String errorMessage = new evaluateDealApi(user, dealCode,evaluatecontent).call();
        	if(errorMessage!=null){
        		evalResult eva=new evalResult(dealCode,errorMessage);
        		resultList.add(eva);
        	}
        	else{
        		PaiPaiTradeDisplay order = PaiPaiTradeDao.findTradeDisplayByDealCode(user.getId(), dealCode);
        		
        		if(StringUtils.equals(order.getDealRateState(),"DEAL_RATE_BUYER_NO_SELLER_NO")){
        			order.setDealRateState("DEAL_RATE_BUYER_NO_SELLER_DONE");
        			order.jdbcSave();
        		}
        		else if(StringUtils.equals(order.getDealRateState(),"DEAL_RATE_BUYER_DONE_SELLER_NO")){
        			order.setDealRateState("DEAL_RATE_BUYER_DONE_SELLER_DONE");
        			order.jdbcSave();
        		}
        		else{
            		evalResult eva=new evalResult(dealCode,"未知评价状态，不能评价!");
            		resultList.add(eva);
        		}
        	}
        	
        }
        
        renderJSON(new TMResult(resultList));
    }
    
    public static class evalResult{
    	public String dealCode;
    	public String errorMessage;
    	
    	public evalResult(String dealCode,String errorMessage){
    		this.dealCode=dealCode;
    		this.errorMessage=errorMessage;
    	}
    }
}
