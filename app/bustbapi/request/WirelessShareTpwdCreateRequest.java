package bustbapi.request;

import com.taobao.api.internal.mapping.ApiField;
import com.taobao.api.TaobaoObject;
import java.util.Map;

import com.taobao.api.ApiRuleException;
import com.taobao.api.BaseTaobaoRequest;
import com.taobao.api.internal.util.TaobaoHashMap;
import com.taobao.api.internal.utils.json.JSONWriter;
import bustbapi.response.WirelessShareTpwdCreateResponse;

/**
 * TOP API: taobao.wireless.share.tpwd.create request
 * 
 * @author top auto create
 * @since 1.0, 2017.04.18
 */
public class WirelessShareTpwdCreateRequest extends BaseTaobaoRequest<WirelessShareTpwdCreateResponse> {
	
	

	/** 
	* 口令参数
	 */
	private String tpwdParam;

	public void setTpwdParam(String tpwdParam) {
		this.tpwdParam = tpwdParam;
	}

	public void setTpwdParam(GenPwdIsvParamDto tpwdParam) {
		this.tpwdParam = new JSONWriter(false,true).write(tpwdParam);
	}

	public String getTpwdParam() {
		return this.tpwdParam;
	}

	public String getApiMethodName() {
		return "taobao.wireless.share.tpwd.create";
	}

	public Map<String, String> getTextParams() {		
		TaobaoHashMap txtParams = new TaobaoHashMap();
		txtParams.put("tpwd_param", this.tpwdParam);
		if(this.udfParams != null) {
			txtParams.putAll(this.udfParams);
		}
		return txtParams;
	}

	public Class<WirelessShareTpwdCreateResponse> getResponseClass() {
		return WirelessShareTpwdCreateResponse.class;
	}

	public void check() throws ApiRuleException {
	}
	
	/**
 * 口令参数
 *
 * @author top auto create
 * @since 1.0, null
 */
public static class GenPwdIsvParamDto extends TaobaoObject {

	private static final long serialVersionUID = 1166973553834453393L;

	/**
		 * 扩展字段JSON格式
		 */
		@ApiField("ext")
		private String ext;
		/**
		 * 口令弹框logoURL
		 */
		@ApiField("logo")
		private String logo;
		/**
		 * 口令弹框内容
		 */
		@ApiField("text")
		private String text;
		/**
		 * 口令跳转url
		 */
		@ApiField("url")
		private String url;
		/**
		 * 生成口令的淘宝用户ID
		 */
		@ApiField("user_id")
		private Long userId;
	

	public String getExt() {
			return this.ext;
		}
		public void setExt(String ext) {
			this.ext = ext;
		}
		public String getLogo() {
			return this.logo;
		}
		public void setLogo(String logo) {
			this.logo = logo;
		}
		public String getText() {
			return this.text;
		}
		public void setText(String text) {
			this.text = text;
		}
		public String getUrl() {
			return this.url;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		public Long getUserId() {
			return this.userId;
		}
		public void setUserId(Long userId) {
			this.userId = userId;
		}

}


}