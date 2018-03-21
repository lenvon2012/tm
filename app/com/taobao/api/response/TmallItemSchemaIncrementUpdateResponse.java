package com.taobao.api.response;

import com.taobao.api.internal.mapping.ApiField;

import com.taobao.api.TaobaoResponse;

/**
 * TOP API: tmall.item.schema.increment.update response.
 * 
 * @author auto create
 * @since 1.0, null
 */
public class TmallItemSchemaIncrementUpdateResponse extends TaobaoResponse {

	private static final long serialVersionUID = 7753836437746399564L;

	/** 
	 * 返回商品发布结果
	 */
	@ApiField("update_item_result")
	private String updateItemResult;

	public void setUpdateItemResult(String updateItemResult) {
		this.updateItemResult = updateItemResult;
	}
	public String getUpdateItemResult( ) {
		return this.updateItemResult;
	}

}
