package bustbapi.request;

import java.util.Map;

import bustbapi.response.JushitaJmsUserGetResponse;

import com.taobao.api.ApiRuleException;
import com.taobao.api.BaseTaobaoRequest;
import com.taobao.api.internal.util.RequestCheckUtils;
import com.taobao.api.internal.util.TaobaoHashMap;

/**
 * TOP API: taobao.jushita.jms.user.get request
 * 
 * @author top auto create
 * @since 1.0, 2016.01.13
 */
public class JushitaJmsUserGetRequest extends BaseTaobaoRequest<JushitaJmsUserGetResponse> {
	
	

	/** 
	* 需要查询的用户名
	 */
	private String userNick;

	public void setUserNick(String userNick) {
		this.userNick = userNick;
	}

	public String getUserNick() {
		return this.userNick;
	}

	public String getApiMethodName() {
		return "taobao.jushita.jms.user.get";
	}

	public Map<String, String> getTextParams() {		
		TaobaoHashMap txtParams = new TaobaoHashMap();
		txtParams.put("user_nick", this.userNick);
		if(this.udfParams != null) {
			txtParams.putAll(this.udfParams);
		}
		return txtParams;
	}

	public Class<JushitaJmsUserGetResponse> getResponseClass() {
		return JushitaJmsUserGetResponse.class;
	}

	public void check() throws ApiRuleException {
		RequestCheckUtils.checkNotEmpty(userNick, "userNick");
	}
	

}