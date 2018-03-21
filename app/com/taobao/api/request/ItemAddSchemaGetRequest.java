package com.taobao.api.request;

import com.taobao.api.internal.util.RequestCheckUtils;
import java.util.Map;

import com.taobao.api.ApiRuleException;
import com.taobao.api.BaseTaobaoRequest;
import com.taobao.api.internal.util.TaobaoHashMap;

import com.taobao.api.response.ItemAddSchemaGetResponse;

/**
 * TOP API: taobao.item.add.schema.get request
 * 
 * @author top auto create
 * @since 1.0, 2016.03.04
 */
public class ItemAddSchemaGetRequest extends BaseTaobaoRequest<ItemAddSchemaGetResponse> {
	
	

	/** 
	* 发布宝贝的叶子类目id
	 */
	private Long categoryId;

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}

	public Long getCategoryId() {
		return this.categoryId;
	}

	public String getApiMethodName() {
		return "taobao.item.add.schema.get";
	}

	public Map<String, String> getTextParams() {		
		TaobaoHashMap txtParams = new TaobaoHashMap();
		txtParams.put("category_id", this.categoryId);
		if(this.udfParams != null) {
			txtParams.putAll(this.udfParams);
		}
		return txtParams;
	}

	public Class<ItemAddSchemaGetResponse> getResponseClass() {
		return ItemAddSchemaGetResponse.class;
	}

	public void check() throws ApiRuleException {
		RequestCheckUtils.checkNotEmpty(categoryId, "categoryId");
	}
	

}