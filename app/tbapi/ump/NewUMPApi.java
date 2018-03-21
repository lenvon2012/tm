package tbapi.ump;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.user.User;

import org.apache.commons.collections.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bustbapi.ErrorHandler;
import bustbapi.TBApi;
import bustbapi.request.PromotionmiscCommonItemActivityAddRequest;
import bustbapi.request.PromotionmiscCommonItemActivityDeleteRequest;
import bustbapi.request.PromotionmiscCommonItemActivityUpdateRequest;
import bustbapi.request.PromotionmiscCommonItemDetailAddRequest;
import bustbapi.request.PromotionmiscCommonItemDetailDeleteRequest;
import bustbapi.request.PromotionmiscCommonItemDetailListGetRequest;
import bustbapi.request.PromotionmiscCommonItemDetailUpdateRequest;
import bustbapi.response.PromotionmiscCommonItemActivityAddResponse;
import bustbapi.response.PromotionmiscCommonItemActivityDeleteResponse;
import bustbapi.response.PromotionmiscCommonItemActivityUpdateResponse;
import bustbapi.response.PromotionmiscCommonItemDetailAddResponse;
import bustbapi.response.PromotionmiscCommonItemDetailDeleteResponse;
import bustbapi.response.PromotionmiscCommonItemDetailListGetResponse;
import bustbapi.response.PromotionmiscCommonItemDetailUpdateResponse;
import bustbapi.result.CommonItemDetail;

import com.ciaosir.client.CommonUtils;
import com.google.gson.Gson;
import com.taobao.api.TaobaoRequest;
import com.taobao.api.TaobaoResponse;

public abstract class NewUMPApi<K extends TaobaoRequest<V>, V extends TaobaoResponse, W> extends TBApi<K, V, W> {

	public NewUMPApi(String sid) {
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

	public static final String TAG = "NewUMPApi";
	
	// 创建通用单品优惠活动
	public static class CommonItemActivityAdd extends
		TBApi<PromotionmiscCommonItemActivityAddRequest, PromotionmiscCommonItemActivityAddResponse, Long> {
		
		User user;
		
		String name;
		
		String description;
		
		long startTime;
		
		long endTime;
		
		public CommonItemActivityAdd(User user, String name, String description,
				long startTime, long endTime) {
			super(user.getSessionKey());
			this.user = user;
			this.name = name;
			this.description = description;
			this.startTime = startTime;
			this.endTime = endTime;
		}
		
		@Override
		public PromotionmiscCommonItemActivityAddRequest prepareRequest() {
			PromotionmiscCommonItemActivityAddRequest req = new PromotionmiscCommonItemActivityAddRequest ();
			req.setName(name);;
			req.setDescription(description);
			Date startDate = new Date(startTime);
			req.setStartTime(startDate);
			Date endDate = new Date(endTime);
			req.setEndTime(endDate);
		
			return req;
		}
		
		@Override
		public Long validResponse(PromotionmiscCommonItemActivityAddResponse resp) {
			if (resp == null) {
				return null;
			}
			ErrorHandler.validTaoBaoResp(this, resp);
			
			if (resp.getIsSuccess()) {
				return resp.getActivityId();
			}
			return null;
		}
		
		@Override
		public Long applyResult(Long res) {
			return res;
		}
		
	}
	
	// 修改通用单品优惠活动
	public static class CommonItemActivityUpdate extends
		TBApi<PromotionmiscCommonItemActivityUpdateRequest, PromotionmiscCommonItemActivityUpdateResponse, Boolean> {
		
		User user;
		
		long activityId;
		
		String name;
		
		String description;
		
		long startTime;
		
		long endTime;
		
		public CommonItemActivityUpdate(User user, long activityId, String name,
				String description, long startTime, long endTime) {
			super(user.getSessionKey());
			this.user = user;
			this.activityId = activityId;
			this.name = name;
			this.description = description;
			this.startTime = startTime;
			this.endTime = endTime;
		}
		
		@Override
		public PromotionmiscCommonItemActivityUpdateRequest prepareRequest() {
			PromotionmiscCommonItemActivityUpdateRequest req = new PromotionmiscCommonItemActivityUpdateRequest ();
			req.setActivityId(activityId);
			req.setName(name);;
			req.setDescription(description);
			Date startDate = new Date(startTime);
			req.setStartTime(startDate);
			Date endDate = new Date(endTime);
			req.setEndTime(endDate);
		
			return req;
		}
		
		@Override
		public Boolean validResponse(PromotionmiscCommonItemActivityUpdateResponse resp) {
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
			return res;
		}
		
	}
	
	// 删除通用单品优惠活动
	public static class CommonItemActivityDelete extends
		TBApi<PromotionmiscCommonItemActivityDeleteRequest, PromotionmiscCommonItemActivityDeleteResponse, Boolean> {
		
		User user;
		
		long activityId;
		
		public CommonItemActivityDelete(User user, long activityId) {
			super(user.getSessionKey());
			this.user = user;
			this.activityId = activityId;
		}
		
		@Override
		public PromotionmiscCommonItemActivityDeleteRequest prepareRequest() {
			PromotionmiscCommonItemActivityDeleteRequest req = new PromotionmiscCommonItemActivityDeleteRequest ();
			req.setActivityId(activityId);
		
			return req;
		}
		
		@Override
		public Boolean validResponse(PromotionmiscCommonItemActivityDeleteResponse resp) {
			if (resp == null) {
				return false;
			}
			ErrorHandler.validTaoBaoResp(this, resp);
			
			if (resp.isSuccess() && resp.getIsSuccess()) {
				return true;
			}
			
			// 活动在官方后台已被删除
			if("优惠活动不存在".equalsIgnoreCase(resp.getSubMsg())) {
				return true;
			}
			
			return false;
		}
		
		@Override
		public Boolean applyResult(Boolean res) {
			return res;
		}
		
	}
	
	// 创建通用单品优惠详情
	public static class CommonItemDetailAdd extends
		TBApi<PromotionmiscCommonItemDetailAddRequest, PromotionmiscCommonItemDetailAddResponse, Long> {
		
		User user;
		
		long activityId;
		
		long itemId;
		
		long promotionType;
		
		long promotionValue;
		
		public CommonItemDetailAdd(User user, long activityId, long itemId,
				long promotionType, long promotionValue) {
			super(user.getSessionKey());
			this.user = user;
			this.activityId = activityId;
			this.itemId = itemId;
			this.promotionType = promotionType;
			this.promotionValue = promotionValue;
		}
		
		@Override
		public PromotionmiscCommonItemDetailAddRequest prepareRequest() {
			PromotionmiscCommonItemDetailAddRequest req = new PromotionmiscCommonItemDetailAddRequest ();
			req.setActivityId(activityId);
			req.setItemId(itemId);
			req.setPromotionType(promotionType);
			req.setPromotionValue(promotionValue);
		
			return req;
		}
		
		@Override
		public Long validResponse(PromotionmiscCommonItemDetailAddResponse resp) {
			if (resp == null) {
				return null;
			}
			ErrorHandler.validTaoBaoResp(this, resp);
			
			if(resp.getIsSuccess()) {
				return resp.getDetailId();
			}
			
			return null;
		}
		
		@Override
		public Long applyResult(Long res) {
			return res;
		}
		
	}
	
	// 修改通用单品优惠详情
	public static class CommonItemDetailUpdate extends
		TBApi<PromotionmiscCommonItemDetailUpdateRequest, PromotionmiscCommonItemDetailUpdateResponse, Boolean> {
		
		User user;
		
		long activityId;
		
		long detailId;
		
		long itemId;
		
		long promotionType;
		
		long promotionValue;
		
		public CommonItemDetailUpdate(User user, long activityId, long detailId, long itemId,
				long promotionType, long promotionValue) {
			super(user.getSessionKey());
			this.user = user;
			this.activityId = activityId;
			this.detailId = detailId;
			this.itemId = itemId;
			this.promotionType = promotionType;
			this.promotionValue = promotionValue;
		}
		
		@Override
		public PromotionmiscCommonItemDetailUpdateRequest prepareRequest() {
			PromotionmiscCommonItemDetailUpdateRequest req = new PromotionmiscCommonItemDetailUpdateRequest ();
			req.setActivityId(activityId);
			req.setDetailId(detailId);
			req.setItemId(itemId);
			req.setPromotionType(promotionType);
			req.setPromotionValue(promotionValue);
		
			return req;
		}
		
		@Override
		public Boolean validResponse(PromotionmiscCommonItemDetailUpdateResponse resp) {
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
			return res;
		}
		
	}
	
	// 删除通用单品优惠详情
	public static class CommonItemDetailDelete extends
		TBApi<PromotionmiscCommonItemDetailDeleteRequest, PromotionmiscCommonItemDetailDeleteResponse, Boolean> {
		
		User user;
		
		long activityId;
		
		long detailId;
		
		public CommonItemDetailDelete(User user, long activityId, long detailId) {
			super(user.getSessionKey());
			this.user = user;
			this.activityId = activityId;
			this.detailId = detailId;
		}
		
		@Override
		public PromotionmiscCommonItemDetailDeleteRequest prepareRequest() {
			PromotionmiscCommonItemDetailDeleteRequest req = new PromotionmiscCommonItemDetailDeleteRequest ();
			req.setActivityId(activityId);
			req.setDetailId(detailId);
		
			return req;
		}
		
		@Override
		public Boolean validResponse(PromotionmiscCommonItemDetailDeleteResponse resp) {
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
			return res;
		}
		
	}
	
	// 查询通用单品优惠详情列表
	public static class CommonItemDetailListGet extends
		TBApi<PromotionmiscCommonItemDetailListGetRequest, PromotionmiscCommonItemDetailListGetResponse, List<CommonItemDetail>> {
		
		User user;
		
		long activityId;
		
		long pageNo = 1L;
		
		long pageSize = 50L;
		
		public List<CommonItemDetail> resList = new ArrayList<CommonItemDetail>();
		
		public CommonItemDetailListGet(User user, long activityId) {
			super(user.getSessionKey());
			this.user = user;
			this.activityId = activityId;
			this.resList = new ArrayList<CommonItemDetail>();
		}
		
		@Override
		public PromotionmiscCommonItemDetailListGetRequest prepareRequest() {
			PromotionmiscCommonItemDetailListGetRequest req = new PromotionmiscCommonItemDetailListGetRequest ();
			req.setActivityId(activityId);
			req.setPageNo(pageNo);
			req.setPageSize(pageSize);
		
			return req;
		}
		
		@Override
		public List<CommonItemDetail> validResponse(PromotionmiscCommonItemDetailListGetResponse resp) {
			if (resp == null) {
				return null;
			}
			ErrorHandler.validTaoBaoResp(this, resp);
			
			List<CommonItemDetail> detailList = resp.getDetailList();
			if(CommonUtils.isEmpty(detailList)) {
				return ListUtils.EMPTY_LIST;
			}
			
			log.info("[CommonItemDetailListGet]get detailList : userId=" + user.getId() + " pn=" + pageNo);
			
			iteratorTime = 1;
			pageNo++;
			
			return detailList;
		}
		
		@Override
		public List<CommonItemDetail> applyResult(List<CommonItemDetail> res) {
			if (res == null) {
				return resList;
			}
			resList.addAll(res);
			return resList;
		}
		
	}
	
}
