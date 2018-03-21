package bustbapi.response;

import com.taobao.api.internal.mapping.ApiField;

import com.taobao.api.TaobaoResponse;

/**
 * TOP API: taobao.promotionmisc.common.item.detail.delete response.
 * 
 * @author top auto create
 * @since 1.0, null
 */
public class PromotionmiscCommonItemDetailDeleteResponse extends TaobaoResponse {

	private static final long serialVersionUID = 3266836719496356243L;

	/** 
	 * 是否删除成功
	 */
	@ApiField("is_success")
	private Boolean isSuccess;


	public void setIsSuccess(Boolean isSuccess) {
		this.isSuccess = isSuccess;
	}
	public Boolean getIsSuccess( ) {
		return this.isSuccess;
	}
	


}
