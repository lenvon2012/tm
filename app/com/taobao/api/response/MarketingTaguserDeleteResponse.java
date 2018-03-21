package com.taobao.api.response;

import com.taobao.api.TaobaoResponse;
import com.taobao.api.internal.mapping.ApiField;

/**
 * TOP API: taobao.marketing.taguser.delete response.
 * 
 * @author auto create
 * @since 1.0, null
 */
public class MarketingTaguserDeleteResponse extends TaobaoResponse {

	private static final long serialVersionUID = 4274235553613473394L;

	/** 
	 * 是否成功
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
