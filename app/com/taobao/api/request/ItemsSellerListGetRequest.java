package com.taobao.api.request;

import com.taobao.api.internal.util.RequestCheckUtils;
import java.util.Map;

import com.taobao.api.TaobaoRequest;
import com.taobao.api.internal.util.TaobaoHashMap;

import com.taobao.api.response.ItemsSellerListGetResponse;
import com.taobao.api.ApiRuleException;

/**
 * TOP API: taobao.items.seller.list.get request
 * 
 * @author auto create
 * @since 1.0, 2015.04.01
 */
public class ItemsSellerListGetRequest implements TaobaoRequest<ItemsSellerListGetResponse> {

	private Map<String, String> headerMap = new TaobaoHashMap();
	private TaobaoHashMap udfParams; // add user-defined text parameters
	
	private Long timestamp;

	/** 
	* 需要返回的商品字段列表。可选值：点击返回结果中的Item结构体中能展示出来的所有字段，多个字段用“,”分隔。注：返回所有sku信息的字段名称是sku而不是skus。
	 */
	private String fields;

	/** 
	* 商品ID列表，多个ID用半角逗号隔开，一次最多不超过20个。注：获取不存在的商品ID或获取别人的商品都不会报错，但没有商品数据返回。
	 */
	private String numIids;

	public void setFields(String fields) {
		this.fields = fields;
	}
	public String getFields() {
		return this.fields;
	}

	public void setNumIids(String numIids) {
		this.numIids = numIids;
	}
	public String getNumIids() {
		return this.numIids;
	}
	
	public Long getTimestamp() {
		return this.timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public String getApiMethodName() {
		return "taobao.items.seller.list.get";
	}

	public Map<String, String> getTextParams() {		
		TaobaoHashMap txtParams = new TaobaoHashMap();
		txtParams.put("fields", this.fields);
		txtParams.put("num_iids", this.numIids);
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

	public Class<ItemsSellerListGetResponse> getResponseClass() {
		return ItemsSellerListGetResponse.class;
	}

	public void check() throws ApiRuleException {
		RequestCheckUtils.checkNotEmpty(fields, "fields");
		RequestCheckUtils.checkNotEmpty(numIids, "numIids");
		RequestCheckUtils.checkMaxListSize(numIids, 20, "numIids");
	}

	public Map<String, String> getHeaderMap() {
		return headerMap;
	}
}