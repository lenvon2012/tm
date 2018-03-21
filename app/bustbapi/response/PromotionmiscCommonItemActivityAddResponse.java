package bustbapi.response;

import com.taobao.api.internal.mapping.ApiField;

import com.taobao.api.TaobaoResponse;

/**
 * TOP API: taobao.promotionmisc.common.item.activity.add response.
 * 
 * @author top auto create
 * @since 1.0, null
 */
public class PromotionmiscCommonItemActivityAddResponse extends TaobaoResponse {

	private static final long serialVersionUID = 5649232424585661517L;

	/** 
	 * 优惠活动ID
	 */
	@ApiField("activity_id")
	private Long activityId;

	/** 
	 * 是否创建成功
	 */
	@ApiField("is_success")
	private Boolean isSuccess;


	public void setActivityId(Long activityId) {
		this.activityId = activityId;
	}
	public Long getActivityId( ) {
		return this.activityId;
	}

	public void setIsSuccess(Boolean isSuccess) {
		this.isSuccess = isSuccess;
	}
	public Boolean getIsSuccess( ) {
		return this.isSuccess;
	}
	


}
