package com.taobao.api.domain;

import com.taobao.api.internal.mapping.ApiField;
import com.taobao.api.TaobaoObject;


/**
 * item对应的素材文件info
 *
 * @author top auto create
 * @since 1.0, null
 */
public class ItemMaterialInfo extends TaobaoObject {

	private static final long serialVersionUID = 3578694252126499724L;

	/**
	 * 素材id
	 */
	@ApiField("material_id")
	private String materialId;

	/**
	 * itemId_skuId
	 */
	@ApiField("source_id")
	private String sourceId;


	public String getMaterialId() {
		return this.materialId;
	}
	public void setMaterialId(String materialId) {
		this.materialId = materialId;
	}

	public String getSourceId() {
		return this.sourceId;
	}
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

}
