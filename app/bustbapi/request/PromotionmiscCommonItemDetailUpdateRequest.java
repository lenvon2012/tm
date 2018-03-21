package bustbapi.request;

import java.util.Map;

import bustbapi.response.PromotionmiscCommonItemDetailUpdateResponse;

import com.taobao.api.ApiRuleException;
import com.taobao.api.BaseTaobaoRequest;
import com.taobao.api.internal.util.RequestCheckUtils;
import com.taobao.api.internal.util.TaobaoHashMap;

/**
 * TOP API: taobao.promotionmisc.common.item.detail.update request
 * 
 * @author top auto create
 * @since 1.0, 2015.10.29
 */
public class PromotionmiscCommonItemDetailUpdateRequest extends BaseTaobaoRequest<PromotionmiscCommonItemDetailUpdateResponse> {
	
	

	/** 
	* 优惠活动ID
	 */
	private Long activityId;

	/** 
	* 优惠详情ID
	 */
	private Long detailId;

	/** 
	* 商品ID
	 */
	private Long itemId;

	/** 
	* 优惠类型，只有两种可选值：0-减钱；1-打折
	 */
	private Long promotionType;

	/** 
	* 优惠力度，其值的解释方式由promotion_type定义：当为减钱时解释成减钱数量，如：900表示减9元；当为打折时解释成打折折扣，如：900表示打9折
	 */
	private Long promotionValue;

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

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	public Long getItemId() {
		return this.itemId;
	}

	public void setPromotionType(Long promotionType) {
		this.promotionType = promotionType;
	}

	public Long getPromotionType() {
		return this.promotionType;
	}

	public void setPromotionValue(Long promotionValue) {
		this.promotionValue = promotionValue;
	}

	public Long getPromotionValue() {
		return this.promotionValue;
	}

	public String getApiMethodName() {
		return "taobao.promotionmisc.common.item.detail.update";
	}

	public Map<String, String> getTextParams() {		
		TaobaoHashMap txtParams = new TaobaoHashMap();
		txtParams.put("activity_id", this.activityId);
		txtParams.put("detail_id", this.detailId);
		txtParams.put("item_id", this.itemId);
		txtParams.put("promotion_type", this.promotionType);
		txtParams.put("promotion_value", this.promotionValue);
		if(this.udfParams != null) {
			txtParams.putAll(this.udfParams);
		}
		return txtParams;
	}

	public Class<PromotionmiscCommonItemDetailUpdateResponse> getResponseClass() {
		return PromotionmiscCommonItemDetailUpdateResponse.class;
	}

	public void check() throws ApiRuleException {
		RequestCheckUtils.checkNotEmpty(activityId, "activityId");
		RequestCheckUtils.checkMinValue(activityId, 1L, "activityId");
		RequestCheckUtils.checkNotEmpty(detailId, "detailId");
		RequestCheckUtils.checkMinValue(detailId, 1L, "detailId");
		RequestCheckUtils.checkNotEmpty(itemId, "itemId");
		RequestCheckUtils.checkMinValue(itemId, 1L, "itemId");
		RequestCheckUtils.checkNotEmpty(promotionType, "promotionType");
		RequestCheckUtils.checkMaxValue(promotionType, 1L, "promotionType");
		RequestCheckUtils.checkMinValue(promotionType, 0L, "promotionType");
		RequestCheckUtils.checkNotEmpty(promotionValue, "promotionValue");
		RequestCheckUtils.checkMinValue(promotionValue, 1L, "promotionValue");
	}
	

}