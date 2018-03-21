package bustbapi.response;

import com.taobao.api.internal.mapping.ApiField;

import com.taobao.api.TaobaoResponse;

/**
 * TOP API: taobao.jushita.jms.user.delete response.
 * 
 * @author top auto create
 * @since 1.0, null
 */
public class JushitaJmsUserDeleteResponse extends TaobaoResponse {

	private static final long serialVersionUID = 2378972724787792995L;

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
