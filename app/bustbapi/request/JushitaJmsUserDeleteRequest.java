package bustbapi.request;

import java.util.Map;

import bustbapi.response.JushitaJmsUserDeleteResponse;

import com.taobao.api.ApiRuleException;
import com.taobao.api.BaseTaobaoRequest;
import com.taobao.api.internal.util.RequestCheckUtils;
import com.taobao.api.internal.util.TaobaoHashMap;

/**
 * TOP API: taobao.jushita.jms.user.delete request
 * 
 * @author top auto create
 * @since 1.0, 2016.01.13
 */
public class JushitaJmsUserDeleteRequest extends BaseTaobaoRequest<JushitaJmsUserDeleteResponse> {
	
	

	/** 
	* 需要停止同步消息的用户nick
	 */
	private String userNick;

	public void setUserNick(String userNick) {
		this.userNick = userNick;
	}

	public String getUserNick() {
		return this.userNick;
	}

	public String getApiMethodName() {
		return "taobao.jushita.jms.user.delete";
	}

	public Map<String, String> getTextParams() {		
		TaobaoHashMap txtParams = new TaobaoHashMap();
		txtParams.put("user_nick", this.userNick);
		if(this.udfParams != null) {
			txtParams.putAll(this.udfParams);
		}
		return txtParams;
	}

	public Class<JushitaJmsUserDeleteResponse> getResponseClass() {
		return JushitaJmsUserDeleteResponse.class;
	}

	public void check() throws ApiRuleException {
		RequestCheckUtils.checkNotEmpty(userNick, "userNick");
	}
	

}