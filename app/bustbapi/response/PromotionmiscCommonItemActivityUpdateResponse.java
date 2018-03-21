package bustbapi.response;

import com.taobao.api.internal.mapping.ApiField;

import com.taobao.api.TaobaoResponse;

/**
 * TOP API: taobao.promotionmisc.common.item.activity.update response.
 * 
 * @author top auto create
 * @since 1.0, null
 */
public class PromotionmiscCommonItemActivityUpdateResponse extends TaobaoResponse {

	private static final long serialVersionUID = 3162962279917856416L;

	/** 
	 * 是否修改成功
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
