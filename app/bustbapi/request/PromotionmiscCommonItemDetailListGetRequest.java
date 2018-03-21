package bustbapi.request;

import java.util.Map;

import bustbapi.response.PromotionmiscCommonItemDetailListGetResponse;

import com.taobao.api.ApiRuleException;
import com.taobao.api.BaseTaobaoRequest;
import com.taobao.api.internal.util.RequestCheckUtils;
import com.taobao.api.internal.util.TaobaoHashMap;

/**
 * TOP API: taobao.promotionmisc.common.item.detail.list.get request
 * 
 * @author top auto create
 * @since 1.0, 2015.12.03
 */
public class PromotionmiscCommonItemDetailListGetRequest extends BaseTaobaoRequest<PromotionmiscCommonItemDetailListGetResponse> {
	
	

	/** 
	* 优惠活动ID
	 */
	private Long activityId;

	/** 
	* 分页页码，页码从1开始
	 */
	private Long pageNo;

	/** 
	* 分页大小，不能超过50
	 */
	private Long pageSize;

	public void setActivityId(Long activityId) {
		this.activityId = activityId;
	}

	public Long getActivityId() {
		return this.activityId;
	}

	public void setPageNo(Long pageNo) {
		this.pageNo = pageNo;
	}

	public Long getPageNo() {
		return this.pageNo;
	}

	public void setPageSize(Long pageSize) {
		this.pageSize = pageSize;
	}

	public Long getPageSize() {
		return this.pageSize;
	}

	public String getApiMethodName() {
		return "taobao.promotionmisc.common.item.detail.list.get";
	}

	public Map<String, String> getTextParams() {		
		TaobaoHashMap txtParams = new TaobaoHashMap();
		txtParams.put("activity_id", this.activityId);
		txtParams.put("page_no", this.pageNo);
		txtParams.put("page_size", this.pageSize);
		if(this.udfParams != null) {
			txtParams.putAll(this.udfParams);
		}
		return txtParams;
	}

	public Class<PromotionmiscCommonItemDetailListGetResponse> getResponseClass() {
		return PromotionmiscCommonItemDetailListGetResponse.class;
	}

	public void check() throws ApiRuleException {
		RequestCheckUtils.checkNotEmpty(activityId, "activityId");
		RequestCheckUtils.checkMinValue(activityId, 1L, "activityId");
		RequestCheckUtils.checkNotEmpty(pageNo, "pageNo");
		RequestCheckUtils.checkMinValue(pageNo, 1L, "pageNo");
		RequestCheckUtils.checkNotEmpty(pageSize, "pageSize");
		RequestCheckUtils.checkMaxValue(pageSize, 50L, "pageSize");
		RequestCheckUtils.checkMinValue(pageSize, 1L, "pageSize");
	}
	

}