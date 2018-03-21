package bustbapi.response;

import com.taobao.api.internal.mapping.ApiField;
import com.taobao.api.domain.TmcUser;

import com.taobao.api.TaobaoResponse;

/**
 * TOP API: taobao.jushita.jms.user.get response.
 * 
 * @author top auto create
 * @since 1.0, null
 */
public class JushitaJmsUserGetResponse extends TaobaoResponse {

	private static final long serialVersionUID = 3419586323452923282L;

	/** 
	 * 同步的用户信息
	 */
	@ApiField("ons_user")
	private TmcUser onsUser;


	public void setOnsUser(TmcUser onsUser) {
		this.onsUser = onsUser;
	}
	public TmcUser getOnsUser( ) {
		return this.onsUser;
	}
	


}
