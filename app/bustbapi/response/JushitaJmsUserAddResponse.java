package bustbapi.response;

import com.taobao.api.internal.mapping.ApiField;

import com.taobao.api.TaobaoResponse;

/**
 * TOP API: taobao.jushita.jms.user.add response.
 * 
 * @author top auto create
 * @since 1.0, null
 */
public class JushitaJmsUserAddResponse extends TaobaoResponse {

	private static final long serialVersionUID = 1389966883864253686L;

	/** 
	 * 是否成功，如果失败请看错误信息
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
