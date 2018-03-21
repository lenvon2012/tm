package com.taobao.api.request;

import com.taobao.api.internal.util.RequestCheckUtils;
import java.util.Map;

import com.taobao.api.ApiRuleException;
import com.taobao.api.BaseTaobaoRequest;
import com.taobao.api.internal.util.TaobaoHashMap;

import com.taobao.api.response.TbkDgItemCouponGetResponse;

/**
 * TOP API: taobao.tbk.dg.item.coupon.get request
 * 
 * @author top auto create
 * @since 1.0, 2017.06.21
 */
public class TbkDgItemCouponGetRequest extends BaseTaobaoRequest<TbkDgItemCouponGetResponse> {
	
	

	/** 
	* mm_xxx_xxx_xxx的第三位
	 */
	private Long adzoneId;

	/** 
	* 后台类目ID，用,分割，最大10个，该ID可以通过taobao.itemcats.get接口获取到
	 */
	private String cat;

	/** 
	* 第几页，默认：1（当后台类目和查询词均不指定的时候，最多出10000个结果，即page_no*page_size不能超过200；当指定类目或关键词的时候，则最多出100个结果）
	 */
	private Long pageNo;

	/** 
	* 页大小，默认20，1~100
	 */
	private Long pageSize;

	/** 
	* 1：PC，2：无线，默认：1
	 */
	private Long platform;

	/** 
	* 查询词
	 */
	private String q;

	public void setAdzoneId(Long adzoneId) {
		this.adzoneId = adzoneId;
	}

	public Long getAdzoneId() {
		return this.adzoneId;
	}

	public void setCat(String cat) {
		this.cat = cat;
	}

	public String getCat() {
		return this.cat;
	}

	public void setPageNo(Long pageNo) {
		this.pageNo = pageNo;
	}

	public Long getPageNo() {
		return this.pageNo;
	}

	public void setPageSize(Long pageSize) {
		this.pageSize = pageSize;
	}

	public Long getPageSize() {
		return this.pageSize;
	}

	public void setPlatform(Long platform) {
		this.platform = platform;
	}

	public Long getPlatform() {
		return this.platform;
	}

	public void setQ(String q) {
		this.q = q;
	}

	public String getQ() {
		return this.q;
	}

	public String getApiMethodName() {
		return "taobao.tbk.dg.item.coupon.get";
	}

	public Map<String, String> getTextParams() {		
		TaobaoHashMap txtParams = new TaobaoHashMap();
		txtParams.put("adzone_id", this.adzoneId);
		txtParams.put("cat", this.cat);
		txtParams.put("page_no", this.pageNo);
		txtParams.put("page_size", this.pageSize);
		txtParams.put("platform", this.platform);
		txtParams.put("q", this.q);
		if(this.udfParams != null) {
			txtParams.putAll(this.udfParams);
		}
		return txtParams;
	}

	public Class<TbkDgItemCouponGetResponse> getResponseClass() {
		return TbkDgItemCouponGetResponse.class;
	}

	public void check() throws ApiRuleException {
		RequestCheckUtils.checkNotEmpty(adzoneId, "adzoneId");
		RequestCheckUtils.checkMaxLength(cat, 10, "cat");
	}
	

}