package bustbapi.response;

import com.taobao.api.internal.mapping.ApiField;

import com.taobao.api.TaobaoResponse;

/**
 * TOP API: taobao.promotionmisc.common.item.detail.add response.
 * 
 * @author top auto create
 * @since 1.0, null
 */
public class PromotionmiscCommonItemDetailAddResponse extends TaobaoResponse {

	private static final long serialVersionUID = 3293656754148744663L;

	/** 
	 * 优惠详情ID
	 */
	@ApiField("detail_id")
	private Long detailId;

	/** 
	 * 是否创建成功
	 */
	@ApiField("is_success")
	private Boolean isSuccess;


	public void setDetailId(Long detailId) {
		this.detailId = detailId;
	}
	public Long getDetailId( ) {
		return this.detailId;
	}

	public void setIsSuccess(Boolean isSuccess) {
		this.isSuccess = isSuccess;
	}
	public Boolean getIsSuccess( ) {
		return this.isSuccess;
	}
	


}
