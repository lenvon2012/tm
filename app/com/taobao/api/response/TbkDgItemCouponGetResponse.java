package com.taobao.api.response;

import java.util.List;
import com.taobao.api.internal.mapping.ApiField;
import com.taobao.api.TaobaoObject;
import com.taobao.api.internal.mapping.ApiListField;

import com.taobao.api.TaobaoResponse;

/**
 * TOP API: taobao.tbk.dg.item.coupon.get response.
 * 
 * @author top auto create
 * @since 1.0, null
 */
public class TbkDgItemCouponGetResponse extends TaobaoResponse {

	private static final long serialVersionUID = 6824714993162784588L;

	/** 
	 * TbkCoupon
	 */
	@ApiListField("results")
	@ApiField("tbk_coupon")
	private List<TbkCoupon> results;

	/** 
	 * 总请求数
	 */
	@ApiField("total_results")
	private Long totalResults;


	public void setResults(List<TbkCoupon> results) {
		this.results = results;
	}
	public List<TbkCoupon> getResults( ) {
		return this.results;
	}

	public void setTotalResults(Long totalResults) {
		this.totalResults = totalResults;
	}
	public Long getTotalResults( ) {
		return this.totalResults;
	}
	
	/**
 * TbkCoupon
 *
 * @author top auto create
 * @since 1.0, null
 */
public static class TbkCoupon extends TaobaoObject {

	private static final long serialVersionUID = 7587875791135796526L;

	/**
		 * 后台一级类目
		 */
		@ApiField("category")
		private Long category;
		/**
		 * 佣金比率(%)
		 */
		@ApiField("commission_rate")
		private String commissionRate;
		/**
		 * 商品优惠券推广链接
		 */
		@ApiField("coupon_click_url")
		private String couponClickUrl;
		/**
		 * 优惠券结束时间
		 */
		@ApiField("coupon_end_time")
		private String couponEndTime;
		/**
		 * 优惠券面额
		 */
		@ApiField("coupon_info")
		private String couponInfo;
		/**
		 * 优惠券剩余量
		 */
		@ApiField("coupon_remain_count")
		private Long couponRemainCount;
		/**
		 * 优惠券开始时间
		 */
		@ApiField("coupon_start_time")
		private String couponStartTime;
		/**
		 * 优惠券总量
		 */
		@ApiField("coupon_total_count")
		private Long couponTotalCount;
		/**
		 * 宝贝描述（推荐理由）
		 */
		@ApiField("item_description")
		private String itemDescription;
		/**
		 * 商品详情页链接地址
		 */
		@ApiField("item_url")
		private String itemUrl;
		/**
		 * 卖家昵称
		 */
		@ApiField("nick")
		private String nick;
		/**
		 * itemId
		 */
		@ApiField("num_iid")
		private Long numIid;
		/**
		 * 商品主图
		 */
		@ApiField("pict_url")
		private String pictUrl;
		/**
		 * 卖家id
		 */
		@ApiField("seller_id")
		private Long sellerId;
		/**
		 * 店铺名称
		 */
		@ApiField("shop_title")
		private String shopTitle;
		/**
		 * 商品小图列表
		 */
		@ApiListField("small_images")
		@ApiField("string")
		private List<String> smallImages;
		/**
		 * 商品标题
		 */
		@ApiField("title")
		private String title;
		/**
		 * 卖家类型，0表示集市，1表示商城
		 */
		@ApiField("user_type")
		private Long userType;
		/**
		 * 30天销量
		 */
		@ApiField("volume")
		private Long volume;
		/**
		 * 折扣价
		 */
		@ApiField("zk_final_price")
		private String zkFinalPrice;
	

	public Long getCategory() {
			return this.category;
		}
		public void setCategory(Long category) {
			this.category = category;
		}
		public String getCommissionRate() {
			return this.commissionRate;
		}
		public void setCommissionRate(String commissionRate) {
			this.commissionRate = commissionRate;
		}
		public String getCouponClickUrl() {
			return this.couponClickUrl;
		}
		public void setCouponClickUrl(String couponClickUrl) {
			this.couponClickUrl = couponClickUrl;
		}
		public String getCouponEndTime() {
			return this.couponEndTime;
		}
		public void setCouponEndTime(String couponEndTime) {
			this.couponEndTime = couponEndTime;
		}
		public String getCouponInfo() {
			return this.couponInfo;
		}
		public void setCouponInfo(String couponInfo) {
			this.couponInfo = couponInfo;
		}
		public Long getCouponRemainCount() {
			return this.couponRemainCount;
		}
		public void setCouponRemainCount(Long couponRemainCount) {
			this.couponRemainCount = couponRemainCount;
		}
		public String getCouponStartTime() {
			return this.couponStartTime;
		}
		public void setCouponStartTime(String couponStartTime) {
			this.couponStartTime = couponStartTime;
		}
		public Long getCouponTotalCount() {
			return this.couponTotalCount;
		}
		public void setCouponTotalCount(Long couponTotalCount) {
			this.couponTotalCount = couponTotalCount;
		}
		public String getItemDescription() {
			return this.itemDescription;
		}
		public void setItemDescription(String itemDescription) {
			this.itemDescription = itemDescription;
		}
		public String getItemUrl() {
			return this.itemUrl;
		}
		public void setItemUrl(String itemUrl) {
			this.itemUrl = itemUrl;
		}
		public String getNick() {
			return this.nick;
		}
		public void setNick(String nick) {
			this.nick = nick;
		}
		public Long getNumIid() {
			return this.numIid;
		}
		public void setNumIid(Long numIid) {
			this.numIid = numIid;
		}
		public String getPictUrl() {
			return this.pictUrl;
		}
		public void setPictUrl(String pictUrl) {
			this.pictUrl = pictUrl;
		}
		public Long getSellerId() {
			return this.sellerId;
		}
		public void setSellerId(Long sellerId) {
			this.sellerId = sellerId;
		}
		public String getShopTitle() {
			return this.shopTitle;
		}
		public void setShopTitle(String shopTitle) {
			this.shopTitle = shopTitle;
		}
		public List<String> getSmallImages() {
			return this.smallImages;
		}
		public void setSmallImages(List<String> smallImages) {
			this.smallImages = smallImages;
		}
		public String getTitle() {
			return this.title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public Long getUserType() {
			return this.userType;
		}
		public void setUserType(Long userType) {
			this.userType = userType;
		}
		public Long getVolume() {
			return this.volume;
		}
		public void setVolume(Long volume) {
			this.volume = volume;
		}
		public String getZkFinalPrice() {
			return this.zkFinalPrice;
		}
		public void setZkFinalPrice(String zkFinalPrice) {
			this.zkFinalPrice = zkFinalPrice;
		}

}



}
