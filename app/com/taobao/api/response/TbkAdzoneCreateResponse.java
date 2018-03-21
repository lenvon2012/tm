package com.taobao.api.response;

import com.taobao.api.internal.mapping.ApiField;
import com.taobao.api.TaobaoObject;

import com.taobao.api.TaobaoResponse;

/**
 * TOP API: taobao.tbk.adzone.create response.
 * 
 * @author top auto create
 * @since 1.0, null
 */
public class TbkAdzoneCreateResponse extends TaobaoResponse {

	private static final long serialVersionUID = 7367856495783245325L;

	/** 
	 * MapData
	 */
	@ApiField("data")
	private MapData data;


	public void setData(MapData data) {
		this.data = data;
	}
	public MapData getData( ) {
		return this.data;
	}
	
	/**
 * MapData
 *
 * @author top auto create
 * @since 1.0, null
 */
public static class MapData extends TaobaoObject {

	private static final long serialVersionUID = 7666787911341376473L;

	/**
		 * 完整的pid
		 */
		@ApiField("model")
		private String model;
	

	public String getModel() {
			return this.model;
		}
		public void setModel(String model) {
			this.model = model;
		}

}



}
