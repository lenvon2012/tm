package com.taobao.api.response;

import java.util.List;
import java.util.Date;
import com.taobao.api.internal.mapping.ApiField;
import com.taobao.api.TaobaoObject;
import com.taobao.api.internal.mapping.ApiListField;

import com.taobao.api.TaobaoResponse;

/**
 * TOP API: taobao.tbk.dg.newuser.order.get response.
 * 
 * @author top auto create
 * @since 1.0, null
 */
public class TbkDgNewuserOrderGetResponse extends TaobaoResponse {

	private static final long serialVersionUID = 5427374297263536138L;

	/** 
	 * data
	 */
	@ApiField("results")
	private Data results;


	public void setResults(Data results) {
		this.results = results;
	}
	public Data getResults( ) {
		return this.results;
	}
	
	/**
 * result
 *
 * @author top auto create
 * @since 1.0, null
 */
public static class MapData extends TaobaoObject {

	private static final long serialVersionUID = 5638412313427873146L;

	/**
		 * 来源广告位ID(pid中mm_1_2_3)中第3位
		 */
		@ApiField("adzone_id")
		private Long adzoneId;
		/**
		 * 来源广告位名称
		 */
		@ApiField("adzone_name")
		private String adzoneName;
		/**
		 * 新激活时间
		 */
		@ApiField("bind_time")
		private Date bindTime;
		/**
		 * 首购时间
		 */
		@ApiField("buy_time")
		private Date buyTime;
		/**
		 * 来源媒体ID(pid中mm_1_2_3)中第1位
		 */
		@ApiField("member_id")
		private Long memberId;
		/**
		 * 来源媒体名称
		 */
		@ApiField("member_nick")
		private String memberNick;
		/**
		 * 新人手机号
		 */
		@ApiField("mobile")
		private String mobile;
		/**
		 * 订单淘客类型:1.淘客订单；2.非淘客订单
		 */
		@ApiField("order_tk_type")
		private Long orderTkType;
		/**
		 * 新注册时间
		 */
		@ApiField("register_time")
		private Date registerTime;
		/**
		 * 来源站点ID(pid中mm_1_2_3)中第2位
		 */
		@ApiField("site_id")
		private Long siteId;
		/**
		 * 来源站点名称
		 */
		@ApiField("site_name")
		private String siteName;
		/**
		 * 新人状态 1:新注册，2:激活，3:首购
		 */
		@ApiField("status")
		private Long status;
		/**
		 * 淘宝订单id
		 */
		@ApiField("tb_trade_parent_id")
		private Long tbTradeParentId;
		/**
		 * 分享用户(unionid)
		 */
		@ApiField("union_id")
		private String unionId;
	

	public Long getAdzoneId() {
			return this.adzoneId;
		}
		public void setAdzoneId(Long adzoneId) {
			this.adzoneId = adzoneId;
		}
		public String getAdzoneName() {
			return this.adzoneName;
		}
		public void setAdzoneName(String adzoneName) {
			this.adzoneName = adzoneName;
		}
		public Date getBindTime() {
			return this.bindTime;
		}
		public void setBindTime(Date bindTime) {
			this.bindTime = bindTime;
		}
		public Date getBuyTime() {
			return this.buyTime;
		}
		public void setBuyTime(Date buyTime) {
			this.buyTime = buyTime;
		}
		public Long getMemberId() {
			return this.memberId;
		}
		public void setMemberId(Long memberId) {
			this.memberId = memberId;
		}
		public String getMemberNick() {
			return this.memberNick;
		}
		public void setMemberNick(String memberNick) {
			this.memberNick = memberNick;
		}
		public String getMobile() {
			return this.mobile;
		}
		public void setMobile(String mobile) {
			this.mobile = mobile;
		}
		public Long getOrderTkType() {
			return this.orderTkType;
		}
		public void setOrderTkType(Long orderTkType) {
			this.orderTkType = orderTkType;
		}
		public Date getRegisterTime() {
			return this.registerTime;
		}
		public void setRegisterTime(Date registerTime) {
			this.registerTime = registerTime;
		}
		public Long getSiteId() {
			return this.siteId;
		}
		public void setSiteId(Long siteId) {
			this.siteId = siteId;
		}
		public String getSiteName() {
			return this.siteName;
		}
		public void setSiteName(String siteName) {
			this.siteName = siteName;
		}
		public Long getStatus() {
			return this.status;
		}
		public void setStatus(Long status) {
			this.status = status;
		}
		public Long getTbTradeParentId() {
			return this.tbTradeParentId;
		}
		public void setTbTradeParentId(Long tbTradeParentId) {
			this.tbTradeParentId = tbTradeParentId;
		}
		public String getUnionId() {
			return this.unionId;
		}
		public void setUnionId(String unionId) {
			this.unionId = unionId;
		}

}

	/**
 * data
 *
 * @author top auto create
 * @since 1.0, null
 */
public static class Data extends TaobaoObject {

	private static final long serialVersionUID = 6158391377868779112L;

	/**
		 * 是否有下一页
		 */
		@ApiField("has_next")
		private Boolean hasNext;
		/**
		 * 页码
		 */
		@ApiField("page_no")
		private Long pageNo;
		/**
		 * 每页大小
		 */
		@ApiField("page_size")
		private Long pageSize;
		/**
		 * result
		 */
		@ApiListField("results")
		@ApiField("map_data")
		private List<MapData> results;
	

	public Boolean getHasNext() {
			return this.hasNext;
		}
		public void setHasNext(Boolean hasNext) {
			this.hasNext = hasNext;
		}
		public Long getPageNo() {
			return this.pageNo;
		}
		public void setPageNo(Long pageNo) {
			this.pageNo = pageNo;
		}
		public Long getPageSize() {
			return this.pageSize;
		}
		public void setPageSize(Long pageSize) {
			this.pageSize = pageSize;
		}
		public List<MapData> getResults() {
			return this.results;
		}
		public void setResults(List<MapData> results) {
			this.results = results;
		}

}



}
