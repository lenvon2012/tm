package bustbapi.request;

import java.util.Map;

import bustbapi.response.JushitaJmsUserAddResponse;

import com.taobao.api.ApiRuleException;
import com.taobao.api.BaseTaobaoRequest;
import com.taobao.api.internal.util.RequestCheckUtils;
import com.taobao.api.internal.util.TaobaoHashMap;

/**
 * TOP API: taobao.jushita.jms.user.add request
 * 
 * @author top auto create
 * @since 1.0, 2016.01.29
 */
public class JushitaJmsUserAddRequest extends BaseTaobaoRequest<JushitaJmsUserAddResponse> {
	
	

	/** 
	* topic列表,不填则继承appkey所订阅的topic
	 */
	private String topicNames;

	public void setTopicNames(String topicNames) {
		this.topicNames = topicNames;
	}

	public String getTopicNames() {
		return this.topicNames;
	}

	public String getApiMethodName() {
		return "taobao.jushita.jms.user.add";
	}

	public Map<String, String> getTextParams() {		
		TaobaoHashMap txtParams = new TaobaoHashMap();
		txtParams.put("topic_names", this.topicNames);
		if(this.udfParams != null) {
			txtParams.putAll(this.udfParams);
		}
		return txtParams;
	}

	public Class<JushitaJmsUserAddResponse> getResponseClass() {
		return JushitaJmsUserAddResponse.class;
	}

	public void check() throws ApiRuleException {
		RequestCheckUtils.checkMaxListSize(topicNames, 20, "topicNames");
	}
	

}