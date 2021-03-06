package com.taobao.api.response;

import com.taobao.api.internal.mapping.ApiField;
import com.taobao.api.domain.Item;

import com.taobao.api.TaobaoResponse;

/**
 * TOP API: taobao.item.seller.get response.
 * 
 * @author auto create
 * @since 1.0, null
 */
public class ItemSellerGetResponse extends TaobaoResponse {

	private static final long serialVersionUID = 5484618593798978997L;

	/** 
	 * 商品详细信息
	 */
	@ApiField("item")
	private Item item;

	public void setItem(Item item) {
		this.item = item;
	}
	public Item getItem( ) {
		return this.item;
	}

}
