/**
 * 
 */
package bustbapi;

import models.user.User;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.api.domain.Item;
import com.taobao.api.request.ItemUpdateDelistingRequest;
import com.taobao.api.request.ItemUpdateListingRequest;
import com.taobao.api.response.ItemUpdateDelistingResponse;
import com.taobao.api.response.ItemUpdateListingResponse;


public class ItemListingApi {
    public static final Logger log = LoggerFactory.getLogger(ItemListingApi.class);

    public static class ItemUpdateListing extends TBApi<ItemUpdateListingRequest, ItemUpdateListingResponse, Item> {

    	public User user;
    	public Long numIid;
    	public Long num;
    	
    	private String errorMsg;

        /**
         * @param sid
         */
        public ItemUpdateListing(String sid) {
            super(sid);
            // TODO Auto-generated constructor stub
        }

        public ItemUpdateListing(User user, Long numIid, Long num) {
            super(user.getSessionKey());
            this.numIid = numIid;
            this.num = num;
            this.user = user;
        }


        public String getErrorMsg() {
			return errorMsg;
		}

		public void setErrorMsg(String errorMsg) {
			this.errorMsg = errorMsg;
		}

        
        /*
         * (non-Javadoc)
         * 
         * @see bustbapi.TBApi#applyResult(java.lang.Object)
         */
        @Override
        public Item applyResult(Item res) {
            // TODO Auto-generated method stub
            return res;
        }

        /*
         * (non-Javadoc)
         * 
         * @see bustbapi.TBApi#prepareRequest()
         */
        @Override
        public ItemUpdateListingRequest prepareRequest() {
            // TODO Auto-generated method stub
            ItemUpdateListingRequest request = new ItemUpdateListingRequest();
            request.setNumIid(numIid);
            request.setNum(num);
            return request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see bustbapi.TBApi#validResponse(com.taobao.api.TaobaoResponse)
         */
        @Override
        public Item validResponse(ItemUpdateListingResponse response) {
            // TODO Auto-generated method stub

            if (response.isSuccess()) {
//                log.info("上架成功！！for item:" + numIid);
                // log.info("get item numIid : " +
                // response.getItem().getNumIid());

                return response.getItem();
            } else {
                int errorCode = Integer.parseInt(response.getErrorCode());
                String Msg = response.getMsg();
                String subErrorCode = response.getSubCode();
                String subMsg = response.getSubMsg();
                errorMsg = response.getSubMsg();
                
                if (StringUtils.isEmpty(errorMsg)) {
                    errorMsg = response.getErrorCode() + ", " + response.getSubCode()
                            + ", " + response.getMsg();
                }

                log.info("item: " + numIid + " ,Error Code: " + errorCode);
                log.info("item: " + numIid + ",Msg: " + Msg);
                log.info("item: " + numIid + ",Sub Error Code: " + subErrorCode);
                log.info("item: " + numIid + ",Sub Msg: " + subMsg);

                // 空subMsg
                if (subMsg == null || subMsg.trim().equals("")) {
                    subMsg = response.getErrorCode() + "," + Msg;
                }

                 //有些错误不用重试
                 if ((errorCode > 100 || errorCode == 15)
                 && subErrorCode.startsWith("isv")) {
                 retryTime = 1;
                 log.info("listing set retryTime=1 for item:" + numIid);
                 }
                 
                if(errorCode==50&&subErrorCode.equals("isv.item-listing-service-error:ITEM_PROPERTIES_ERROR")){
                    //不重试
                    retryTime = 1;
                    log.info("属性出错，listing set retryTime=1 for item:" + numIid);
                }
                
                // 如果是auto_fill为空的虚拟商品，发现此类错误，则删除该卖家的所有计划。
                if ((errorCode == 50)
                        && subErrorCode.equals("isv.item-listing-service-error:IC_PERMISSION_FOR_TBCP_ONLY")) {
                    //不重试
                    retryTime = 1;
                    log.info("listing set retryTime=1 for item:" + numIid);

                    
                    return null;
                }

                // 违规商品
                else if ((errorCode == 50)
                        && subErrorCode.equals("isv.item-listing-service-error:IC_CHECKSTEP_NO_PERMISSION")) {
                    //不重试
                    retryTime = 1;
                    log.info("listing set retryTime=1 for item:" + numIid);

                    return null;

                }
                
                // 如果是session过期，删除剩余计划
                //Body:{"error_response":{"code":27,"msg":"Invalid session:session-expired","sub_code":"session-expired"}}
                else if ((errorCode == 27)
                        && subErrorCode.equals("session-expired")) {
                    
                    //不重试
                    retryTime = 1;
                    log.info("listing set retryTime=1 for item:" + numIid);
                    

                    return null;
                }
                
                
                

                // 商家失败，可以重试成功种类
                // 27,Invalid session:session-not-exist
                // "code":26,"msg":"Missing session"}
                // "code":530,"sub_code":"isv.item-listing-service-error:GENERIC_FAILURE","sub_msg":"系统错误，请稍后再试:20120929-0D2E593A1F416A29"}
                // {"error_response":{"code":530,"msg":"Remote service error","sub_code":"isv.item-listing-service-error:GENERIC_FAILURE-tmall","sub_msg":"系统错误，请稍后再试:20121001-E8FD26627B62FB3B"}}

                // 这种不要了：重试不成功Body:{"error_response":{"code":530,"msg":"Remote service error","sub_code":"isv.item-listing-service-error","sub_msg":"上架商品失败"}
                else if (errorCode == 27
                        && Msg.equals("Invalid session:session-not-exist")
                        || (errorCode == 26 && Msg.equals("Missing session"))
                        || (errorCode == 530 && subErrorCode.equals("isv.item-listing-service-error:GENERIC_FAILURE"))
                        || (errorCode == 530 && subErrorCode
                                .equals("isv.item-listing-service-error:GENERIC_FAILURE-tmall"))) {

                    
                }

                // 上架属性错误等卖家不操作无法上架的商品,
                // 属性错误：
                // errorCode==50&&subErrorCode.equals("isv.item-listing-service-error:ITEM_PROPERTIES_ERROR")
                else {

                    
                }

                return null;
            }

        }

    }

    public static class ItemUpdateDelisting extends
            TBApi<ItemUpdateDelistingRequest, ItemUpdateDelistingResponse, Item> {

    	private String errorMsg;
        Long numIid = null;
        User user = null;

        /**
         * @param sid
         */
        public ItemUpdateDelisting(String sid) {
            super(sid);
            // TODO Auto-generated constructor stub
        }

        public ItemUpdateDelisting(User user, Long numIid) {
            super(user.getSessionKey());
            this.numIid = numIid;
            this.user = user;
            this.numIid = numIid;
        }

        
        
        public String getErrorMsg() {
			return errorMsg;
		}

		public void setErrorMsg(String errorMsg) {
			this.errorMsg = errorMsg;
		}

		/*
         * (non-Javadoc)
         * 
         * @see bustbapi.TBApi#applyResult(java.lang.Object)
         */
        @Override
        public Item applyResult(Item res) {
            // TODO Auto-generated method stub
            return res;
        }

        /*
         * (non-Javadoc)
         * 
         * @see bustbapi.TBApi#prepareRequest()
         */
        @Override
        public ItemUpdateDelistingRequest prepareRequest() {
            // TODO Auto-generated method stub
            ItemUpdateDelistingRequest req = new ItemUpdateDelistingRequest();
            req.setNumIid(numIid);
            return req;
        }

        /*
         * (non-Javadoc)
         * 
         * @see bustbapi.TBApi#validResponse(com.taobao.api.TaobaoResponse)
         */
        @Override
        public Item validResponse(ItemUpdateDelistingResponse response) {
            // TODO Auto-generated method stub
            if (response.isSuccess()) {
//                log.info("下架成功！！for item:" + numIid);
                // log.info("get item numIid: " +
                // response.getItem().getNumIid());
                return response.getItem();
            } else {
                int errorCode = Integer.parseInt(response.getErrorCode());
                String Msg = response.getMsg();
                String subErrorCode = response.getSubCode();
                String subMsg = response.getSubMsg();
                errorMsg = response.getSubMsg();

                log.info("item: " + numIid + " ,Error Code: " + errorCode);
                log.info("item: " + numIid + ",Msg: " + Msg);
                log.info("item: " + numIid + ",Sub Error Code: " + subErrorCode);
                log.info("item: " + numIid + ",Sub Msg: " + subMsg);

                // 空subMsg
                if (subMsg == null || subMsg.trim().equals("")) {
                    subMsg = response.getErrorCode() + "," + Msg;
                }

                // 有些错误不用重试
                if ((errorCode > 100 || errorCode == 15) && subErrorCode.startsWith("isv")) {
                    retryTime = 1;
                    log.info("delisting set retryTime=1 for item:" + numIid);
                }

                // 如果是auto_fill为空的虚拟商品，发现此类错误，则删除该卖家的所有剩余计划。
                if ((errorCode == 50)
                        && subErrorCode.equals("isv.item-listing-service-error:IC_PERMISSION_FOR_TBCP_ONLY")) {
                    
                    //不重试
                    retryTime = 1;
                    log.info("delisting set retryTime=1 for item:" + numIid);
                    
                    
                    return null;
                }

                // 如果是违规商品，发现此类错误，则删除该卖家的所有剩余计划。
                else if ((errorCode == 50)
                        && subErrorCode.equals("isv.item-listing-service-error:IC_CHECKSTEP_NO_PERMISSION")) {
                    //不重试
                    retryTime = 1;
                    log.info("delisting set retryTime=1 for item:" + numIid);
                   
                    return null;
                }
                
                // 如果是session过期，删除剩余计划
                //Body:{"error_response":{"code":27,"msg":"Invalid session:session-expired","sub_code":"session-expired"}}
                else if ((errorCode == 27)
                        && subErrorCode.equals("session-expired")) {
                    //不重试
                    retryTime = 1;
                    
                    
                    return null;
                }
                
                

                // 其他情况，都推迟一周
                else {

                    
                }
                return null;
            }
        }
    }

}
