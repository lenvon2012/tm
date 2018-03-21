package bustbapi.request;

import com.taobao.api.internal.util.RequestCheckUtils;
import java.util.Map;

import com.taobao.api.ApiRuleException;
import com.taobao.api.BaseTaobaoRequest;
import com.taobao.api.internal.util.TaobaoHashMap;

import bustbapi.response.PromotionmiscCommonItemActivityDeleteResponse;

/**
 * TOP API: taobao.promotionmisc.common.item.activity.delete request
 * 
 * @author top auto create
 * @since 1.0, 2015.10.29
 */
public class PromotionmiscCommonItemActivityDeleteRequest extends BaseTaobaoRequest<PromotionmiscCommonItemActivityDeleteResponse> {
	
	

	/** 
	* 优惠活动ID
	 */
	private Long activityId;

	public void setActivityId(Long activityId) {
		this.activityId = activityId;
	}

	public Long getActivityId() {
		return this.activityId;
	}

	public String getApiMethodName() {
		return "taobao.promotionmisc.common.item.activity.delete";
	}

	public Map<String, String> getTextParams() {		
		TaobaoHashMap txtParams = new TaobaoHashMap();
		txtParams.put("activity_id", this.activityId);
		if(this.udfParams != null) {
			txtParams.putAll(this.udfParams);
		}
		return txtParams;
	}

	public Class<PromotionmiscCommonItemActivityDeleteResponse> getResponseClass() {
		return PromotionmiscCommonItemActivityDeleteResponse.class;
	}

	public void check() throws ApiRuleException {
		RequestCheckUtils.checkNotEmpty(activityId, "activityId");
		RequestCheckUtils.checkMinValue(activityId, 1L, "activityId");
	}
	

}