package bustbapi.response;

import com.taobao.api.internal.mapping.ApiField;

import com.taobao.api.TaobaoResponse;

/**
 * TOP API: taobao.wireless.share.tpwd.create response.
 * 
 * @author top auto create
 * @since 1.0, null
 */
public class WirelessShareTpwdCreateResponse extends TaobaoResponse {

	private static final long serialVersionUID = 7553992552533538546L;

	/** 
	 * 口令内容，用于口令宣传组织
	 */
	@ApiField("model")
	private String model;


	public void setModel(String model) {
		this.model = model;
	}
	public String getModel( ) {
		return this.model;
	}
	


}
