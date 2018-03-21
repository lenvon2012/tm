package com.taobao.api.request;

import com.taobao.api.internal.util.RequestCheckUtils;
import java.util.Map;

import com.taobao.api.ApiRuleException;
import com.taobao.api.BaseTaobaoRequest;
import com.taobao.api.internal.util.TaobaoHashMap;

import com.taobao.api.response.TbkAdzoneCreateResponse;

/**
 * TOP API: taobao.tbk.adzone.create request
 * 
 * @author top auto create
 * @since 1.0, 2017.09.07
 */
public class TbkAdzoneCreateRequest extends BaseTaobaoRequest<TbkAdzoneCreateResponse> {
	
	

	/** 
	* 广告位名称，最大长度64字符
	 */
	private String adzoneName;

	/** 
	* 网站ID
	 */
	private Long siteId;

	public void setAdzoneName(String adzoneName) {
		this.adzoneName = adzoneName;
	}

	public String getAdzoneName() {
		return this.adzoneName;
	}

	public void setSiteId(Long siteId) {
		this.siteId = siteId;
	}

	public Long getSiteId() {
		return this.siteId;
	}

	public String getApiMethodName() {
		return "taobao.tbk.adzone.create";
	}

	public Map<String, String> getTextParams() {		
		TaobaoHashMap txtParams = new TaobaoHashMap();
		txtParams.put("adzone_name", this.adzoneName);
		txtParams.put("site_id", this.siteId);
		if(this.udfParams != null) {
			txtParams.putAll(this.udfParams);
		}
		return txtParams;
	}

	public Class<TbkAdzoneCreateResponse> getResponseClass() {
		return TbkAdzoneCreateResponse.class;
	}

	public void check() throws ApiRuleException {
		RequestCheckUtils.checkNotEmpty(adzoneName, "adzoneName");
		RequestCheckUtils.checkMaxLength(adzoneName, 64, "adzoneName");
		RequestCheckUtils.checkNotEmpty(siteId, "siteId");
	}
	

}