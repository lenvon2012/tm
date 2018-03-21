package bustbapi.response;

import java.util.List;
import com.taobao.api.internal.mapping.ApiField;
import com.taobao.api.internal.mapping.ApiListField;

import com.taobao.api.TaobaoResponse;

/**
 * TOP API: taobao.jushita.jms.topics.get response.
 * 
 * @author top auto create
 * @since 1.0, null
 */
public class JushitaJmsTopicsGetResponse extends TaobaoResponse {

	private static final long serialVersionUID = 8342222349939414393L;

	/** 
	 * 错误码
	 */
	@ApiField("result_code")
	private String resultCode;

	/** 
	 * 错误信息
	 */
	@ApiField("result_message")
	private String resultMessage;

	/** 
	 * topic列表
	 */
	@ApiListField("results")
	@ApiField("string")
	private List<String> results;


	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}
	public String getResultCode( ) {
		return this.resultCode;
	}

	public void setResultMessage(String resultMessage) {
		this.resultMessage = resultMessage;
	}
	public String getResultMessage( ) {
		return this.resultMessage;
	}

	public void setResults(List<String> results) {
		this.results = results;
	}
	public List<String> getResults( ) {
		return this.results;
	}
	


}
