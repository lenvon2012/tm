package bustbapi.request;

import java.util.Map;

import bustbapi.response.JushitaJmsTopicsGetResponse;

import com.taobao.api.ApiRuleException;
import com.taobao.api.BaseTaobaoRequest;
import com.taobao.api.internal.util.RequestCheckUtils;
import com.taobao.api.internal.util.TaobaoHashMap;

/**
 * TOP API: taobao.jushita.jms.topics.get request
 * 
 * @author top auto create
 * @since 1.0, 2016.03.03
 */
public class JushitaJmsTopicsGetRequest extends BaseTaobaoRequest<JushitaJmsTopicsGetResponse> {
	
	

	/** 
	* 卖家nick
	 */
	private String nick;

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getNick() {
		return this.nick;
	}

	public String getApiMethodName() {
		return "taobao.jushita.jms.topics.get";
	}

	public Map<String, String> getTextParams() {		
		TaobaoHashMap txtParams = new TaobaoHashMap();
		txtParams.put("nick", this.nick);
		if(this.udfParams != null) {
			txtParams.putAll(this.udfParams);
		}
		return txtParams;
	}

	public Class<JushitaJmsTopicsGetResponse> getResponseClass() {
		return JushitaJmsTopicsGetResponse.class;
	}

	public void check() throws ApiRuleException {
		RequestCheckUtils.checkNotEmpty(nick, "nick");
	}
	

}