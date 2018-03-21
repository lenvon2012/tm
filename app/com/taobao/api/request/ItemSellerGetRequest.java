package com.taobao.api.request;

import com.taobao.api.internal.util.RequestCheckUtils;
import java.util.Map;

import com.taobao.api.TaobaoRequest;
import com.taobao.api.internal.util.TaobaoHashMap;

import com.taobao.api.response.ItemSellerGetResponse;
import com.taobao.api.ApiRuleException;

/**
 * TOP API: taobao.item.seller.get request
 * 
 * @author auto create
 * @since 1.0, 2015.04.01
 */
public class ItemSellerGetRequest implements TaobaoRequest<ItemSellerGetResponse> {

	private Map<String, String> headerMap = new TaobaoHashMap();
	private TaobaoHashMap udfParams; // add user-defined text parameters
	
	private Long timestamp;

	/** 
	* 需要返回的商品字段列表。可选值：Item商品结构体中所有字段均可返回，多个字段用“,”分隔。
	 */
	private String fields;

	/** 
	* 商品数字ID
	 */
	private Long numIid;

	public void setFields(String fields) {
		this.fields = fields;
	}
	public String getFields() {
		return this.fields;
	}

	public void setNumIid(Long numIid) {
		this.numIid = numIid;
	}
	public Long getNumIid() {
		return this.numIid;
	}
	
	public Long getTimestamp() {
		return this.timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public String getApiMethodName() {
		return "taobao.item.seller.get";
	}

	public Map<String, String> getTextParams() {		
		TaobaoHashMap txtParams = new TaobaoHashMap();
		txtParams.put("fields", this.fields);
		txtParams.put("num_iid", this.numIid);
		if(this.udfParams != null) {
			txtParams.putAll(this.udfParams);
		}
		return txtParams;
	}

	public void putOtherTextParam(String key, String value) {
		if(this.udfParams == null) {
			this.udfParams = new TaobaoHashMap();
		}
		this.udfParams.put(key, value);
	}

	public Class<ItemSellerGetResponse> getResponseClass() {
		return ItemSellerGetResponse.class;
	}

	public void check() throws ApiRuleException {
		RequestCheckUtils.checkNotEmpty(fields, "fields");
		RequestCheckUtils.checkNotEmpty(numIid, "numIid");
		RequestCheckUtils.checkMinValue(numIid, 1L, "numIid");
	}

	public Map<String, String> getHeaderMap() {
		return headerMap;
	}
}