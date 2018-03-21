package bustbapi.response;

import bustbapi.result.CommonItemDetail;

import com.taobao.api.TaobaoResponse;
import com.taobao.api.internal.mapping.ApiField;

/**
 * TOP API: taobao.promotionmisc.common.item.detail.get response.
 * 
 * @author top auto create
 * @since 1.0, null
 */
public class PromotionmiscCommonItemDetailGetResponse extends TaobaoResponse {

	private static final long serialVersionUID = 8452762541712526234L;

	/** 
	 * 优惠详情
	 */
	@ApiField("detail")
	private CommonItemDetail detail;

	/** 
	 * 是否查询成功
	 */
	@ApiField("is_success")
	private Boolean isSuccess;


	public void setDetail(CommonItemDetail detail) {
		this.detail = detail;
	}
	public CommonItemDetail getDetail( ) {
		return this.detail;
	}

	public void setIsSuccess(Boolean isSuccess) {
		this.isSuccess = isSuccess;
	}
	public Boolean getIsSuccess( ) {
		return this.isSuccess;
	}
	


}
