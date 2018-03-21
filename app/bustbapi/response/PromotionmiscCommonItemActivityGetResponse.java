package bustbapi.response;

import com.taobao.api.internal.mapping.ApiField;
import bustbapi.result.CommonItemActivity;

import com.taobao.api.TaobaoResponse;

/**
 * TOP API: taobao.promotionmisc.common.item.activity.get response.
 * 
 * @author top auto create
 * @since 1.0, null
 */
public class PromotionmiscCommonItemActivityGetResponse extends TaobaoResponse {

	private static final long serialVersionUID = 5327637968371299331L;

	/** 
	 * 优惠活动
	 */
	@ApiField("activity")
	private CommonItemActivity activity;

	/** 
	 * 是否查询成功
	 */
	@ApiField("is_success")
	private Boolean isSuccess;


	public void setActivity(CommonItemActivity activity) {
		this.activity = activity;
	}
	public CommonItemActivity getActivity( ) {
		return this.activity;
	}

	public void setIsSuccess(Boolean isSuccess) {
		this.isSuccess = isSuccess;
	}
	public Boolean getIsSuccess( ) {
		return this.isSuccess;
	}
	


}
