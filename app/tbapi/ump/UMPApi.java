
package tbapi.ump;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import models.promotion.TMProActivity;
import models.ump.PromotionPlay.ItemPromoteType;
import models.ump.UMPTool;
import models.user.User;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import result.TMResult;
import bustbapi.ErrorHandler;
import bustbapi.TBApi;

import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.SplitUtils;
import com.google.gson.Gson;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import com.taobao.api.ApiException;
import com.taobao.api.TaobaoRequest;
import com.taobao.api.TaobaoResponse;
import com.taobao.api.domain.ItemPromotion;
import com.taobao.api.domain.MjsPromotion;
import com.taobao.api.request.PromotionmiscActivityRangeAddRequest;
import com.taobao.api.request.PromotionmiscActivityRangeRemoveRequest;
import com.taobao.api.request.PromotionmiscItemActivityAddRequest;
import com.taobao.api.request.PromotionmiscItemActivityDeleteRequest;
import com.taobao.api.request.PromotionmiscItemActivityGetRequest;
import com.taobao.api.request.PromotionmiscItemActivityListGetRequest;
import com.taobao.api.request.PromotionmiscItemActivityUpdateRequest;
import com.taobao.api.request.PromotionmiscMjsActivityAddRequest;
import com.taobao.api.request.PromotionmiscMjsActivityDeleteRequest;
import com.taobao.api.request.PromotionmiscMjsActivityListGetRequest;
import com.taobao.api.request.PromotionmiscMjsActivityUpdateRequest;
import com.taobao.api.request.UmpActivitiesGetRequest;
import com.taobao.api.request.UmpActivityAddRequest;
import com.taobao.api.response.PromotionmiscActivityRangeAddResponse;
import com.taobao.api.response.PromotionmiscActivityRangeRemoveResponse;
import com.taobao.api.response.PromotionmiscItemActivityAddResponse;
import com.taobao.api.response.PromotionmiscItemActivityDeleteResponse;
import com.taobao.api.response.PromotionmiscItemActivityGetResponse;
import com.taobao.api.response.PromotionmiscItemActivityListGetResponse;
import com.taobao.api.response.PromotionmiscItemActivityUpdateResponse;
import com.taobao.api.response.PromotionmiscMjsActivityAddResponse;
import com.taobao.api.response.PromotionmiscMjsActivityDeleteResponse;
import com.taobao.api.response.PromotionmiscMjsActivityListGetResponse;
import com.taobao.api.response.PromotionmiscMjsActivityUpdateResponse;
import com.taobao.api.response.UmpActivitiesGetResponse;
import com.taobao.api.response.UmpActivityAddResponse;
import com.taobao.ump.marketing.MarketingActivity;
import com.taobao.ump.marketing.MarketingActivity.ParticipateRange;
import com.taobao.ump.marketing.MarketingBuilder;
import com.taobao.ump.marketing.MarketingTool;

import controllers.TaoDiscount;

/**
 * Wait for the ump tool api sdk...
 * @author zrb
 *
 * @param <K>
 * @param <V>
 * @param <W>
 */
public abstract class UMPApi<K extends TaobaoRequest<V>, V extends TaobaoResponse, W> extends TBApi<K, V, W> {

    public UMPApi(String sid) {
        super(sid);
    }

    @Override
    public W validResponse(V resp) {
        log.error(" response : :" + new Gson().toJson(resp.getBody()));
        if (resp.isSuccess()) {
            return null;
        }

        if ("isv.w2-security-authorize-invalid".equals(resp.getSubCode())) {
            this.retryTime = 0;
        } else {

        }

        return null;
    }

    private static final Logger log = LoggerFactory.getLogger(UMPApi.class);

    public static final String TAG = "UMPApi";

//
    public static class ListActivitiesApi extends UMPApi<UmpActivitiesGetRequest, UmpActivitiesGetResponse, String> {

        final Long pageSize = 20L;

        Long totalNum = 0L;

        Long pageNum = 1L;

        Long toolId = null;

        int finishNum = 0;

        public ListActivitiesApi(User user, Long toolId) {
            super(user.getSessionKey());
            this.toolId = toolId;
            this.retryTime = 2;
        }

        @Override
        public UmpActivitiesGetRequest prepareRequest() {
            UmpActivitiesGetRequest req = new UmpActivitiesGetRequest();
            req.setToolId(this.toolId);
            req.setPageNo(this.pageNum);
            req.setPageSize(this.pageSize);
            return req;
        }

        @Override
        public String validResponse(UmpActivitiesGetResponse resp) {
            super.validResponse(resp);
            if (!resp.isSuccess()) {
                return null;
            }

            List<String> contents = resp.getContents();

            this.totalNum = resp.getTotalCount();
            this.finishNum += (contents == null ? 0 : contents.size());

            /*
             * At most 50 activities for a seller
             */
            if (finishNum < this.totalNum && pageNum <= 3) {
                this.iteratorTime = 1;
                pageNum = pageNum + 1;
            }

            return StringUtils.join(contents, ",");
        }

        @Override
        public String applyResult(String res) {
            log.info("[apply res :]" + res);
            return res;
        }

    }

//
//    public static class ModifyActivity extends UMPApi {
//
//    }
//
//    public static class TurnOFFActivity extends UMPApi {
//
//    }
//
//    public static class TurnOnActivity extends UMPApi {
//
//    }
//
    public static class CreateDiscountActivityApi extends
            UMPApi<UmpActivityAddRequest, UmpActivityAddResponse, Long> {

        private Long toolId = UMPTool.getBase().getId();

        String name;

        Date start;

        Date end;

        String desc;

        String tag;

        public CreateDiscountActivityApi(String sid) {
            super(sid);
        }

        public CreateDiscountActivityApi(User user, Long toolId, String name, Date start, Date end, String desc,
                String tag) {
            super(user.getSessionKey());
            this.toolId = toolId;
            this.name = name;
            this.start = start;
            this.end = end;
            this.desc = desc;
            this.tag = tag;
        }

//        SimpleDateFormat yyyymmddFormat = new SimpleDateFormat("yyyy-mm-dd");

        @Override
        public UmpActivityAddRequest prepareRequest() {
            MarketingTool marketingTool;
            try {
                marketingTool = UMPManager.get().getMarketingTool(toolId);
                MarketingBuilder builder = new MarketingBuilder();
                builder.setMetaDefProvider(UMPManager.get().getProvider());

                MarketingActivity marketingActivity = builder.createMarketingActivity(marketingTool);

//                Date startTime = yyyymmddFormat.parse("2013-01-31");
//                Date endTime = yyyymmddFormat.parse("2013-02-08");
//                marketingActivity.setName("减10元");
//                marketingActivity.setDescription("春节专享-减10元");
                marketingActivity.setName(name);
                marketingActivity.setDescription(desc);
                marketingActivity.setStartTime(start);
                marketingActivity.setEndTime(end);

                marketingActivity.setParticipateRange(ParticipateRange.PART);
                marketingActivity.setMultipleLayerPromotion(true);

                String actContent = builder.build(marketingActivity);
                UmpActivityAddRequest actRequest = new UmpActivityAddRequest();
                actRequest.setContent(actContent);
                actRequest.setToolId(toolId);

                return actRequest;
            } catch (ApiException e) {
                log.warn(e.getMessage(), e);
                this.errorMsg = e.getMessage();
            }

            return null;
        }

        @Override
        public Long validResponse(UmpActivityAddResponse resp) {
            super.validResponse(resp);
            if (!resp.isSuccess()) {
                return null;
            }

            return resp.getActId();

        }

        @Override
        public Long applyResult(Long res) {
            return res;
        }
    }

//
//    public static class ModifyActivityItem extends UMPApi {
//
//    }
//
//    public static class RemoveActivityItem extends UMPApi {
//
//    }
//
//    public static class ModifyItemDiscount extends UMPApi {
//

    //    }

    static final int MAX_NUMIID_LENGTH = 50;
    public static class UmpRangeAdd extends
            TBApi<PromotionmiscActivityRangeAddRequest, PromotionmiscActivityRangeAddResponse, TMResult> {

    	List<List<Long>> splitToSubLongList = ListUtils.EMPTY_LIST;

        Long umpActivityId;

        public UmpRangeAdd(User user, Long umpActivityId, Collection<Long> numIids) {
            super(user.getSessionKey());
            this.umpActivityId = umpActivityId;
            this.splitToSubLongList = SplitUtils.splitToSubLongList(numIids, MAX_NUMIID_LENGTH);
            this.iteratorTime = splitToSubLongList.size();
        }

        public UmpRangeAdd(User user, Long umpActivityId, Long numIid) {
            super(user.getSessionKey());
            this.umpActivityId = umpActivityId;
            List<Long> ids = new ArrayList<Long>();
            ids.add(numIid);
            List<List<Long>> idSplit = new ArrayList<List<Long>>();
            idSplit.add(ids);
            this.splitToSubLongList = idSplit;
        }

        @Override
        public PromotionmiscActivityRangeAddRequest prepareRequest() {
            // TODO Auto-generated method stub
            PromotionmiscActivityRangeAddRequest req = new PromotionmiscActivityRangeAddRequest();
            req.setActivityId(umpActivityId);
            req.setIds(StringUtils.join(splitToSubLongList.get(iteratorTime), ','));

            return req;
        }

        @Override
        public TMResult validResponse(PromotionmiscActivityRangeAddResponse resp) {
            if (resp == null) {
                return TMResult.failMsg("null response");
            }
            ErrorHandler.validTaoBaoResp(this, resp);
            
            if (resp.isSuccess() == false) {
                return null;
            }
            
            if (resp.isSuccess() && resp.getIsSuccess()) {
                return TMResult.OK;
            }

            return TMResult.failMsg(resp.getSubCode());
        }

        @Override
        public TMResult applyResult(TMResult res) {
            if (res == null) {
                return TMResult.failMsg("淘宝接口异常！");
            }
            return res;
        }

    }
    
    
    public static class UmpRangeDelete extends
            TBApi<PromotionmiscActivityRangeRemoveRequest, PromotionmiscActivityRangeRemoveResponse, Boolean> {
        
        Collection<Long> numIids;
        
        Long umpActivityId;
        
        public UmpRangeDelete(User user, Long umpActivityId, Collection<Long> numIids) {
            super(user.getSessionKey());
            this.umpActivityId = umpActivityId;
            this.numIids = numIids;
        }
        
        public UmpRangeDelete(User user, Long umpActivityId, Long numIid) {
            super(user.getSessionKey());
            this.umpActivityId = umpActivityId;
            List<Long> ids = new ArrayList<Long>();
            ids.add(numIid);
            this.numIids = ids;
        }
        
        @Override
        public PromotionmiscActivityRangeRemoveRequest prepareRequest() {
            // TODO Auto-generated method stub
            PromotionmiscActivityRangeRemoveRequest req = new PromotionmiscActivityRangeRemoveRequest();
            req.setActivityId(umpActivityId);
            req.setIds(StringUtils.join(numIids, ','));
        
            return req;
        }
        
        @Override
        public Boolean validResponse(PromotionmiscActivityRangeRemoveResponse resp) {
            if (resp == null) {
                return false;
            }
            ErrorHandler.validTaoBaoResp(this, resp);
            if (resp.isSuccess() && resp.getIsSuccess()) {
                return true;
            }
        
            return false;
        }
        
        @Override
        public Boolean applyResult(Boolean res) {
            if (res == null) {
                return false;
            }
            return res;
        }
        
    }
            

    public static class UmpSingleItemActivityDelete extends
            TBApi<PromotionmiscItemActivityDeleteRequest, PromotionmiscItemActivityDeleteResponse, Boolean> {

        User user;

        Long umpActivityId;

        public UmpSingleItemActivityDelete(User user, Long umpActivityId) {
            super(user.getSessionKey());
            this.user = user;
            this.umpActivityId = umpActivityId;
        }

        @Override
        public PromotionmiscItemActivityDeleteRequest prepareRequest() {
            PromotionmiscItemActivityDeleteRequest req = new PromotionmiscItemActivityDeleteRequest();
            req.setActivityId(umpActivityId);
            return req;
        }

        @Override
        public Boolean validResponse(PromotionmiscItemActivityDeleteResponse resp) {

            if (resp == null) {
                return null;
            }
            ErrorHandler.validTaoBaoResp(this, resp);
            
            if (resp.isSuccess() == false) {
                return null;
            }
            
            return resp.getIsSuccess();
        }

        @Override
        public Boolean applyResult(Boolean res) {
            return res;
        }

    }

    /**
     * This api will be a little complex...
     * @author zrb
     *
     */
    public static class UmpSingleItemActivityUpdate extends AbstractDiscountActivityUpdate {

        public UmpSingleItemActivityUpdate(User user, TMProActivity tmActivity, Long promotionId,
                ItemPromoteType promotionType, long discountRate,
                long decreaseAmount) {
            
            super(user, tmActivity, promotionId, promotionType, discountRate, decreaseAmount,
                    false);
        }
        
    }
    
    public static class ShopDiscountActivityUpdate extends AbstractDiscountActivityUpdate {

        public ShopDiscountActivityUpdate(User user, TMProActivity tmActivity, Long promotionId,
                long discountRate) {
            
            super(user, tmActivity, promotionId, ItemPromoteType.discount, discountRate, 0L,
                    true);
        }
        
    }
    
    
    
    public static class AbstractDiscountActivityUpdate extends
            TBApi<PromotionmiscItemActivityUpdateRequest, PromotionmiscItemActivityUpdateResponse, Boolean> {
        
        User user;
        
        Long promotionId;

        long startTime;

        long endTime;

        String acitivityName;

        ItemPromoteType promotionType;
        long discountRate;
        long decreaseAmount;
        
        private boolean isShopDiscount = false;

        public AbstractDiscountActivityUpdate(User user, TMProActivity tmActivity, Long promotionId,
                ItemPromoteType promotionType, long discountRate, long decreaseAmount,
                boolean isShopDiscount) {
            super(user.getSessionKey());
            this.user = user;
            this.promotionId = promotionId;
            
            this.startTime = tmActivity.getActivityStartTime() == null ? 0 : tmActivity.getActivityStartTime();
            this.endTime = tmActivity.getActivityEndTime() == null ? 0 : tmActivity.getActivityEndTime();
            this.acitivityName = tmActivity.getActivityTitle();
            
            this.promotionType = promotionType;
            this.discountRate = discountRate;
            this.decreaseAmount = decreaseAmount;
            
            this.isShopDiscount = isShopDiscount;
        }

        @Override
        public PromotionmiscItemActivityUpdateRequest prepareRequest() {
            PromotionmiscItemActivityUpdateRequest req = new PromotionmiscItemActivityUpdateRequest();
            req.setActivityId(promotionId);
            req.setName(acitivityName);
            
            if (isShopDiscount == false) {
                req.setParticipateRange(1L);
            } else {
                req.setParticipateRange(0L);
            }
            Date startDate = new Date(startTime);
            req.setStartTime(startDate);
            Date endDate = new Date(endTime);
            req.setEndTime(endDate);

            if (ItemPromoteType.discount.equals(promotionType)) {
                req.setIsDiscount(Boolean.TRUE);
                req.setDiscountRate(discountRate);
            } else if (ItemPromoteType.decrease.equals(promotionType)) {
                req.setIsDecreaseMoney(Boolean.TRUE);
                req.setDecreaseAmount(decreaseAmount);
            }

            /**
             * UserTag 是什么意思？
             */
//            req.setIsUserTag(true);
            req.setUserTag("1");
            return req;
        }

        @Override
        public Boolean validResponse(PromotionmiscItemActivityUpdateResponse resp) {

            if (resp == null) {
                return null;
            }
            ErrorHandler.validTaoBaoResp(this, resp);
            
            if (resp.isSuccess() == false) {
                return null;
            }
            
            if (resp.isSuccess()) {
                return resp.getIsSuccess();
            }
            
            return false;
        }

        @Override
        public Boolean applyResult(Boolean res) {
            if (res == null) {
                return false;
            }
            return res;
        }

        
        
    }
    

    /**
     * 添加商品的单个打折活动
     * @author zrb
     */
    public static class UmpSingleItemActivityAdd extends AbstractDiscountActivityAdd {

        public UmpSingleItemActivityAdd(User user, TMProActivity tmActivity,
                ItemPromoteType promotionType, long discountRate,
                long decreaseAmount) {
            
            super(user, tmActivity, promotionType, discountRate, decreaseAmount,
                    false);
        }
        
    }
    
    public static class ShopDiscountActivityAdd extends AbstractDiscountActivityAdd {

        public ShopDiscountActivityAdd(User user, TMProActivity tmActivity,
                long discountRate) {
            
            super(user, tmActivity, ItemPromoteType.discount, discountRate, 0L,
                    true);
        }
        
    }
    
    
    public static class AbstractDiscountActivityAdd extends
            TBApi<PromotionmiscItemActivityAddRequest, PromotionmiscItemActivityAddResponse, Long> {

        User user;

        long startTime;

        long endTime;

        String acitivityName;

        ItemPromoteType promotionType;
        long discountRate;
        long decreaseAmount;
        
        private boolean isShopDiscount = false;

        public AbstractDiscountActivityAdd(User user, TMProActivity tmActivity, 
                ItemPromoteType promotionType, long discountRate, long decreaseAmount,
                boolean isShopDiscount) {
            
            super(user.getSessionKey());
            this.user = user;
            this.startTime = tmActivity.getActivityStartTime() == null ? 0 : tmActivity.getActivityStartTime();
            this.endTime = tmActivity.getActivityEndTime() == null ? 0 : tmActivity.getActivityEndTime();
            this.acitivityName = tmActivity.getActivityTitle();
            
            this.promotionType = promotionType;
            this.discountRate = discountRate;
            this.decreaseAmount = decreaseAmount;
            
            this.isShopDiscount = isShopDiscount;
            
        }

        @Override
        public PromotionmiscItemActivityAddRequest prepareRequest() {
            PromotionmiscItemActivityAddRequest req = new PromotionmiscItemActivityAddRequest();
            req.setName(acitivityName);
            
            if (isShopDiscount == false) {
                req.setParticipateRange(1L);
            } else {
                req.setParticipateRange(0L);
            }
            Date startDate = new Date(startTime);
            req.setStartTime(startDate);
            Date endDate = new Date(endTime);
            req.setEndTime(endDate);

            if (ItemPromoteType.discount.equals(promotionType)) {
                req.setIsDiscount(Boolean.TRUE);
                req.setDiscountRate(discountRate);
            } else if (ItemPromoteType.decrease.equals(promotionType)) {
                req.setIsDecreaseMoney(Boolean.TRUE);
                req.setDecreaseAmount(decreaseAmount);
            }

            /**
             * UserTag 是什么意思？
             */
//            req.setIsUserTag(true);
            req.setUserTag("1");
            return req;
        }

        @Override
        public Long validResponse(PromotionmiscItemActivityAddResponse resp) {

            if (resp == null) {
                return null;
            }
            ErrorHandler.validTaoBaoResp(this, resp);
            
            if (resp.isSuccess() == false) {
                return null;
            }
            
            if (resp.isSuccess()) {
                return resp.getActivityId();
            }
            return null;
        }

        @Override
        public Long applyResult(Long res) {
            
            return res;
        }

    }

    public static class UmpMjsActivityAdd extends
            TBApi<PromotionmiscMjsActivityAddRequest, PromotionmiscMjsActivityAddResponse, Long> {

        User user;

        MjsParams mjsParams;
        
        Boolean isShopMjs = false;
        
        public UmpMjsActivityAdd(User user) {
            super(user.getSessionKey());
            this.user = user;
        }

        public UmpMjsActivityAdd(User user, MjsParams mjsParams) {
            super(user.getSessionKey());
            this.user = user;
            this.mjsParams = mjsParams;
        }
        
        public UmpMjsActivityAdd(User user, MjsParams mjsParams, boolean isShopMjs) {
            super(user.getSessionKey());
            this.user = user;
            this.mjsParams = mjsParams;
            this.isShopMjs = isShopMjs;
        }
        
        @Override
        public PromotionmiscMjsActivityAddRequest prepareRequest() {
        	PromotionmiscMjsActivityAddRequest req=new PromotionmiscMjsActivityAddRequest();
        	if(mjsParams != null) {
        		req.setName(mjsParams.getActivityName());

        		// 全部活动都是店铺级别
        		if(isShopMjs) {
        			// 1表示商品级别的活动；2表示店铺级别的活动
                	req.setType(2L);
                	
                	// 0表示全部参与； 1表示部分商品参与。
                	req.setParticipateRange(0L);
        		} else {
        			// 1表示商品级别的活动；2表示店铺级别的活动
	            	req.setType(2L);
	            	
	            	// 0表示全部参与； 1表示部分商品参与。
	            	req.setParticipateRange(1L);
        		}
        		

            	// 设置活动起始时间
            	req.setStartTime(new Date(mjsParams.getStartLong()));
            	
            	// 设置活动结束时间
            	req.setEndTime(new Date(mjsParams.getEndLong()));
            	
            	// 如果有满元条件
            	if(mjsParams.isAmountOver()) {
            		req.setIsAmountOver(true);
            		req.setTotalPrice(mjsParams.getTotalPrice());            		
            	}
            	
            	// 是否有满件条件
            	if(mjsParams.isItemCountOver()) {
            		req.setIsItemCountOver(true);
                	req.setItemCount(mjsParams.getItemCount());                
            	}
            	
            	// 是否有减价行为
            	if(mjsParams.isDecrease()) {
            		req.setIsDecreaseMoney(true);
            		// 以分为单位
            		req.setDecreaseAmount(mjsParams.getDecreaseValue());
            		
            		// 满件上不封顶
                	if(mjsParams.isItemMultiple()) {
                		req.setIsItemMultiple(true);
                	}
                	// 满元上不封顶
                	if(mjsParams.isAmountMultiple()) {
                		req.setIsAmountMultiple(true);
                	}
            	} 

            	// 是否有打折行为
            	if(mjsParams.isDiscount()) {
            		req.setIsDiscount(true);
            		req.setDiscountRate(mjsParams.getDiscountValue());
            	}
            	
            	// 是否送礼物
            	if(mjsParams.isSentGift()) {
            		req.setIsSendGift(true);
            		req.setGiftName(mjsParams.getGiftName());
            	}
            	
            	// 是否有包邮行为
            	if(mjsParams.isFreePost()) {
            		req.setIsFreePost(true);
            		req.setExcludeArea(mjsParams.getExcludedCodes() + "*990000");
            	}
            	
        	}
        	
        	return req;
        }

        @Override
        public Long validResponse(PromotionmiscMjsActivityAddResponse resp) {
        	 if (resp == null) {
                 return null;
             }
             ErrorHandler.validTaoBaoResp(this, resp);
             
             if (resp.isSuccess() == false) {
                 return null;
             }
             
             if (resp.isSuccess()) {
                 return resp.getActivityId();
             }
             return null;
        }

        @Override
        public Long applyResult(Long res) {
            return res;
        }

    }
    
    public static class UmpMjsActivityUpdate extends
    	TBApi<PromotionmiscMjsActivityUpdateRequest, PromotionmiscMjsActivityUpdateResponse, Boolean> {
		
		User user;
		
		MjsParams mjsParams;
		
		Long activityId;
		
		Boolean isShop = false;
		
		public UmpMjsActivityUpdate(User user) {
		    super(user.getSessionKey());
		    this.user = user;
		}
		
		public UmpMjsActivityUpdate(User user, MjsParams mjsParams, Long activityId, Boolean isShop) {
		    super(user.getSessionKey());
		    this.user = user;
		    this.mjsParams = mjsParams;
		    this.activityId = activityId;
		    this.isShop = isShop;
		}
		
		@Override
		public PromotionmiscMjsActivityUpdateRequest prepareRequest() {
			PromotionmiscMjsActivityUpdateRequest req=new PromotionmiscMjsActivityUpdateRequest();
			if(mjsParams != null) {
				req.setName(mjsParams.getActivityName());
		    	req.setActivityId(activityId);
		    	if(isShop) {
		    		req.setParticipateRange(0L);
		    	} else {
		    		req.setParticipateRange(1L);
		    	}
		    	
		    	// 设置活动起始时间
		    	req.setStartTime(new Date(mjsParams.getStartLong()));
		    	
		    	// 设置活动结束时间
		    	req.setEndTime(new Date(mjsParams.getEndLong()));
		    	
		    	// 如果有满元条件
		    	if(mjsParams.isAmountOver()) {
		    		req.setIsAmountOver(true);
		    		req.setTotalPrice(mjsParams.getTotalPrice());            		
		    	}
		    	
		    	// 是否有满件条件
		    	if(mjsParams.isItemCountOver()) {
		    		req.setIsItemCountOver(true);
		        	req.setItemCount(mjsParams.getItemCount());                
		    	}
		    	
		    	// 是否有减价行为
		    	if(mjsParams.isDecrease()) {
		    		req.setIsDecreaseMoney(true);
		    		// 以分为单位
		    		req.setDecreaseAmount(mjsParams.getDecreaseValue());
		    		
		    		// 满件上不封顶
		        	if(mjsParams.isItemMultiple()) {
		        		req.setIsItemMultiple(true);
		        	}
		        	// 满元上不封顶
		        	if(mjsParams.isAmountMultiple()) {
		        		req.setIsAmountMultiple(true);
		        	}
		    	} 
		
		    	// 是否有打折行为
		    	if(mjsParams.isDiscount()) {
		    		req.setIsDiscount(true);
		    		req.setDiscountRate(mjsParams.getDiscountValue());
		    	}
		    	
		    	// 是否送礼物
		    	if(mjsParams.isSentGift()) {
		    		req.setIsSendGift(true);
		    		req.setGiftName(mjsParams.getGiftName());
		    	}
		    	
		    	// 是否有包邮行为
		    	if(mjsParams.isFreePost()) {
		    		req.setIsFreePost(true);
		    		req.setExcludeArea(mjsParams.getExcludedCodes() + "*990000");
		    	}
		    	
			}
			
			return req;
		}
		
		@Override
		public Boolean validResponse(PromotionmiscMjsActivityUpdateResponse resp) {
			 if (resp == null) {
		         return false;
		     }
		     ErrorHandler.validTaoBaoResp(this, resp);
		     
		     if (resp.isSuccess() == false) {
	                return false;
	         }
		     
		     if (resp.isSuccess()) {
		         return true;
		     }
		     return false;
		}
		
		@Override
		public Boolean applyResult(Boolean res) {
		    if (res == null) {
                return false;
            }
		    return res;
		}
		
	}
    
    public static class UmpMjsActivityListGet extends
	    TBApi<PromotionmiscMjsActivityListGetRequest, PromotionmiscMjsActivityListGetResponse, List<MjsPromotion>> {
	
		User user;
		
		// 1表示商品级别的活动；2表示店铺级别的活动。
		Long mjsType = 1L;
		
		public UmpMjsActivityListGet(User user) {
		    super(user.getSessionKey());
		    this.user = user;
		}
		
		public UmpMjsActivityListGet(User user, Long mjsType) {
		    super(user.getSessionKey());
		    this.user = user;
		    this.mjsType = mjsType;
		}
		
		@Override
		public PromotionmiscMjsActivityListGetRequest prepareRequest() {
			PromotionmiscMjsActivityListGetRequest req=new PromotionmiscMjsActivityListGetRequest();
			req.setActivityType(mjsType);
			req.setPageNo(1L);
			req.setPageSize(20L);
		    
		    return req;
		}
		
		@Override
		public List<MjsPromotion> validResponse(PromotionmiscMjsActivityListGetResponse resp) {
		    if (resp == null) {
		        return null;
		    }
		    ErrorHandler.validTaoBaoResp(this, resp);
		    
		    if (resp.isSuccess() == false) {
                return null;
		    }
		    
		    if (resp.isSuccess()) {
		        return resp.getMjsPromotionList();
		    }
		    
		    return null;
		}
		
		@Override
		public List<MjsPromotion> applyResult(List<MjsPromotion> res) {
		    return res;
		}
	
	}
    
    public static class UmpMjsActivityDelete extends
            TBApi<PromotionmiscMjsActivityDeleteRequest, PromotionmiscMjsActivityDeleteResponse, Boolean> {
        
        User user;
        Long mjsActivityId;
        
        public UmpMjsActivityDelete(User user, Long mjsActivityId) {
            super(user.getSessionKey());
            this.user = user;
            this.mjsActivityId = mjsActivityId;
        }
        
        @Override
        public PromotionmiscMjsActivityDeleteRequest prepareRequest() {
            PromotionmiscMjsActivityDeleteRequest req=new PromotionmiscMjsActivityDeleteRequest();
            req.setActivityId(mjsActivityId);
            
            return req;
        }
        
        @Override
        public Boolean validResponse(PromotionmiscMjsActivityDeleteResponse resp) {
            if (resp == null) {
                return null;
            }
            ErrorHandler.validTaoBaoResp(this, resp);
            
            if (resp.isSuccess() == false) {
                return null;
            }
            
            if (resp.isSuccess()) {
                return resp.getIsSuccess();
            }
            
            return false;
        }
        
        @Override
        public Boolean applyResult(Boolean res) {
            if (res == null) {
                return false;
            }
            return res;
        }
        
    }
    

    public enum MsjType {
    	
    }

    @JsonAutoDetect
    public static class MjsParams extends Model implements Serializable {

    	public MjsParams() {
    		super();
    	}
    	
        @JsonProperty
        String activityName;

        @JsonProperty
        int type;

        @JsonProperty
        String start;

        @JsonProperty
        String end;

        @JsonProperty
        int participageRange = 1;

        /**
         * 满元
         */
        @JsonProperty
        boolean isAmountOver = false;

        @JsonProperty
        boolean isAmountMultiple = false;

        @JsonProperty
        long totalPrice;

        /**
         * 满件
         */
        @JsonProperty
        boolean isItemCountOver = false;

        /**
         * 满件是否上不封顶
         */
        @JsonProperty
        boolean isItemMultiple = false;

        @JsonProperty
        Long itemCount;

        /**
         * 是否店铺会员
         */
        @JsonProperty
        boolean isShopMember = false;

        @JsonProperty
        int shopMemberLevel;

        /**
         * 是否用户标签
         */
        @JsonProperty
        boolean isUserTag = false;

        @JsonProperty
        String userTagStr;

        /**
         * 是否减钱
         */
        @JsonProperty
        boolean isDecrease = false;

        @JsonProperty
        long decreaseValue;

        /**
         * 是否打折
         */
        @JsonProperty
        boolean isDiscount = false;

        @JsonProperty
        long discountValue;

        /**
         * 赠品行为
         */
        @JsonProperty
        boolean isSentGift = false;

        @JsonProperty
        String giftName;
        
        @JsonProperty
        long giftId;

        @JsonProperty
        String giftUrl;

        /**
         * 免邮行为
         */
        @JsonProperty
        boolean isFreePost = false;

        // 免邮地区名
        @JsonProperty
        String excludedAreas;
        
        // 免邮地区代码
        @JsonProperty
        String excludedCodes;
        
        @JsonIgnore
        String toDisplay;
        
        @JsonIgnore
        String toServerParams;
        
        @JsonIgnore
        String buildDisplay;
        
        @JsonIgnore
        String createOne;
        
        public static MjsParams createByJson(String json) {
        	return JsonUtil.toObject(json, MjsParams.class);
        }

		public String getToDisplay() {
			return toDisplay;
		}

		public void setToDisplay(String toDisplay) {
			this.toDisplay = toDisplay;
		}

		public String getToServerParams() {
			return toServerParams;
		}

		public void setToServerParams(String toServerParams) {
			this.toServerParams = toServerParams;
		}

		public String getBuildDisplay() {
			return buildDisplay;
		}

		public void setBuildDisplay(String buildDisplay) {
			this.buildDisplay = buildDisplay;
		}

		public String getCreateOne() {
			return createOne;
		}

		public void setCreateOne(String createOne) {
			this.createOne = createOne;
		}

		public String getActivityName() {
			return activityName;
		}

		public void setActivityName(String activityName) {
			this.activityName = activityName;
		}

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public String getStart() {
			return start;
		}

		public Long getStartLong() {
			try {
				return TaoDiscount.sdf.parse(start).getTime();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return -1L;
		}
		
		public void setStart(String start) {
			this.start = start;
		}

		public String getEnd() {
			return end;
		}
		
		public Long getEndLong() {
			try {
				return TaoDiscount.sdf.parse(end).getTime();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return -1L;
		}

		public void setEnd(String end) {
			this.end = end;
		}

		public int getParticipageRange() {
			return participageRange;
		}

		public void setParticipageRange(int participageRange) {
			this.participageRange = participageRange;
		}

		public boolean isAmountOver() {
			return isAmountOver;
		}

		public void setAmountOver(boolean isAmountOver) {
			this.isAmountOver = isAmountOver;
		}

		public boolean isAmountMultiple() {
			return isAmountMultiple;
		}

		public void setAmountMultiple(boolean isAmountMultiple) {
			this.isAmountMultiple = isAmountMultiple;
		}

		public long getTotalPrice() {
			return totalPrice;
		}

		public void setTotalPrice(long totalPrice) {
			this.totalPrice = totalPrice;
		}

		public boolean isItemCountOver() {
			return isItemCountOver;
		}

		public void setItemCountOver(boolean isItemCountOver) {
			this.isItemCountOver = isItemCountOver;
		}

		public boolean isItemMultiple() {
			return isItemMultiple;
		}

		public void setItemMultiple(boolean isItemMultiple) {
			this.isItemMultiple = isItemMultiple;
		}

		public Long getItemCount() {
			return itemCount;
		}

		public void setItemCount(Long itemCount) {
			this.itemCount = itemCount;
		}

		public boolean isShopMember() {
			return isShopMember;
		}

		public void setShopMember(boolean isShopMember) {
			this.isShopMember = isShopMember;
		}

		public int getShopMemberLevel() {
			return shopMemberLevel;
		}

		public void setShopMemberLevel(int shopMemberLevel) {
			this.shopMemberLevel = shopMemberLevel;
		}

		public boolean isUserTag() {
			return isUserTag;
		}

		public void setUserTag(boolean isUserTag) {
			this.isUserTag = isUserTag;
		}

		public String getUserTagStr() {
			return userTagStr;
		}

		public void setUserTagStr(String userTagStr) {
			this.userTagStr = userTagStr;
		}

		public boolean isDecrease() {
			return isDecrease;
		}

		public void setDecrease(boolean isDecrease) {
			this.isDecrease = isDecrease;
		}

		public long getDecreaseValue() {
			return decreaseValue;
		}

		public void setDecreaseValue(long decreaseValue) {
			this.decreaseValue = decreaseValue;
		}

		public boolean isDiscount() {
			return isDiscount;
		}

		public void setDiscount(boolean isDiscount) {
			this.isDiscount = isDiscount;
		}

		public long getDiscountValue() {
			return discountValue;
		}

		public void setDiscountValue(long discountValue) {
			this.discountValue = discountValue;
		}

		public boolean isSentGift() {
			return isSentGift;
		}

		public void setSentGift(boolean isSentGift) {
			this.isSentGift = isSentGift;
		}

		public long getGiftId() {
			return giftId;
		}

		public void setGiftId(long giftId) {
			this.giftId = giftId;
		}

		public String getGiftUrl() {
			return giftUrl;
		}

		public String getGiftName() {
			return giftName;
		}

		public void setGiftName(String giftName) {
			this.giftName = giftName;
		}

		public void setGiftUrl(String giftUrl) {
			this.giftUrl = giftUrl;
		}

		public boolean isFreePost() {
			return isFreePost;
		}

		public void setFreePost(boolean isFreePost) {
			this.isFreePost = isFreePost;
		}

		public String getExcludedAreas() {
			return excludedAreas;
		}

		public void setExcludedAreas(String excludedAreas) {
			this.excludedAreas = excludedAreas;
		}

		public String getExcludedCodes() {
			return excludedCodes;
		}

		public void setExcludedCodes(String excludedCodes) {
			this.excludedCodes = excludedCodes;
		}
        
        
    }
    
    public static class PromotionMiscListAPI extends UMPApi<PromotionmiscItemActivityListGetRequest, PromotionmiscItemActivityListGetResponse, List<ItemPromotion>> {

        final Long pageSize = 20L;

        Long pageNum = 1L;

        List<ItemPromotion> res;
        
        public PromotionMiscListAPI(User user) {
            super(user.getSessionKey());
            this.retryTime = 2;
        }

        public PromotionMiscListAPI(String sid) {
            super(sid);
            this.retryTime = 2;
        }
        
        @Override
        public PromotionmiscItemActivityListGetRequest prepareRequest() {
        	PromotionmiscItemActivityListGetRequest req = new PromotionmiscItemActivityListGetRequest();
            req.setPageNo(this.pageNum);
            req.setPageSize(this.pageSize);
            return req;
        }

        @Override
        public List<ItemPromotion> validResponse(PromotionmiscItemActivityListGetResponse resp) {
            super.validResponse(resp);
            if (!resp.isSuccess()) {
                return null;
            }

            List<ItemPromotion> contents = resp.getItemPromotionList();

            /*
             * At most 50 activities for a seller
             */
            if (contents != null && contents.size() == 20) {
                this.iteratorTime = 1;
                pageNum = pageNum + 1;
            }

            return contents;
        }

        @Override
        public List<ItemPromotion> applyResult(List<ItemPromotion> res) {
            return res;
        }

    }

    public static class PromotionMiscItemsAPI extends UMPApi<PromotionmiscItemActivityGetRequest, PromotionmiscItemActivityGetResponse, ItemPromotion> {
        
        Long activityId;
    	
        public PromotionMiscItemsAPI(User user, Long activityId) {
            super(user.getSessionKey());
            this.activityId = activityId;
            this.retryTime = 2;
        }

        public PromotionMiscItemsAPI(String sid, Long activityId) {
            super(sid);
            this.activityId = activityId;
            this.retryTime = 2;
        }
        
        @Override
        public PromotionmiscItemActivityGetRequest prepareRequest() {
        	PromotionmiscItemActivityGetRequest req = new PromotionmiscItemActivityGetRequest();
        	req.setActivityId(activityId);
            return req;
        }

        @Override
        public ItemPromotion validResponse(PromotionmiscItemActivityGetResponse resp) {
            super.validResponse(resp);
            if (!resp.isSuccess()) {
                return null;
            }

            ItemPromotion contents = resp.getItemPromotion();

            return contents;
        }

        @Override
        public ItemPromotion applyResult(ItemPromotion res) {
            return res;
        }

    }
}
