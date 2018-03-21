package bustbapi.request;

import java.util.Map;

import bustbapi.response.PromotionmiscCommonItemDetailDeleteResponse;

import com.taobao.api.ApiRuleException;
import com.taobao.api.BaseTaobaoRequest;
import com.taobao.api.internal.util.RequestCheckUtils;
import com.taobao.api.internal.util.TaobaoHashMap;

/**
 * TOP API: taobao.promotionmisc.common.item.detail.delete request
 * 
 * @author top auto create
 * @since 1.0, 2015.10.29
 */
public class PromotionmiscCommonItemDetailDeleteRequest extends BaseTaobaoRequest<PromotionmiscCommonItemDetailDeleteResponse> {
	
	

	/** 
	* 优惠活动ID
	 */
	private Long activityId;

	/** 
	* 优惠详情ID
	 */
	private Long detailId;

	public void setActivityId(Long activityId) {
		this.activityId = activityId;
	}

	public Long getActivityId() {
		return this.activityId;
	}

	public void setDetailId(Long detailId) {
		this.detailId = detailId;
	}

	public Long getDetailId() {
		return this.detailId;
	}

	public String getApiMethodName() {
		return "taobao.promotionmisc.common.item.detail.delete";
	}

	public Map<String, String> getTextParams() {		
		TaobaoHashMap txtParams = new TaobaoHashMap();
		txtParams.put("activity_id", this.activityId);
		txtParams.put("detail_id", this.detailId);
		if(this.udfParams != null) {
			txtParams.putAll(this.udfParams);
		}
		return txtParams;
	}

	public Class<PromotionmiscCommonItemDetailDeleteResponse> getResponseClass() {
		return PromotionmiscCommonItemDetailDeleteResponse.class;
	}

	public void check() throws ApiRuleException {
		RequestCheckUtils.checkNotEmpty(activityId, "activityId");
		RequestCheckUtils.checkMinValue(activityId, 1L, "activityId");
		RequestCheckUtils.checkNotEmpty(detailId, "detailId");
		RequestCheckUtils.checkMinValue(detailId, 1L, "detailId");
	}
	

}