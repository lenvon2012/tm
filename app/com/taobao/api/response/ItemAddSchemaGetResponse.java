package com.taobao.api.response;

import com.taobao.api.internal.mapping.ApiField;

import com.taobao.api.TaobaoResponse;

/**
 * TOP API: taobao.item.add.schema.get response.
 * 
 * @author top auto create
 * @since 1.0, null
 */
public class ItemAddSchemaGetResponse extends TaobaoResponse {

	private static final long serialVersionUID = 1776982566519261995L;

	/** 
	 * 返回结果的集合
	 */
	@ApiField("add_rules")
	private String addRules;


	public void setAddRules(String addRules) {
		this.addRules = addRules;
	}
	public String getAddRules( ) {
		return this.addRules;
	}
	


}
