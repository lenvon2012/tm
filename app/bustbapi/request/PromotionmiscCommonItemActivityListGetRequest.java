package bustbapi.request;

import com.taobao.api.internal.util.RequestCheckUtils;
import java.util.Map;

import com.taobao.api.ApiRuleException;
import com.taobao.api.BaseTaobaoRequest;
import com.taobao.api.internal.util.TaobaoHashMap;

import bustbapi.response.PromotionmiscCommonItemActivityListGetResponse;

/**
 * TOP API: taobao.promotionmisc.common.item.activity.list.get request
 * 
 * @author top auto create
 * @since 1.0, 2016.10.20
 */
public class PromotionmiscCommonItemActivityListGetRequest extends BaseTaobaoRequest<PromotionmiscCommonItemActivityListGetResponse> {
	
	

	/** 
	* 分页页码，页码从1开始
	 */
	private Long pageNo;

	/** 
	* 分页大小，不能超过50
	 */
	private Long pageSize;

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

	public String getApiMethodName() {
		return "taobao.promotionmisc.common.item.activity.list.get";
	}

	public Map<String, String> getTextParams() {		
		TaobaoHashMap txtParams = new TaobaoHashMap();
		txtParams.put("page_no", this.pageNo);
		txtParams.put("page_size", this.pageSize);
		if(this.udfParams != null) {
			txtParams.putAll(this.udfParams);
		}
		return txtParams;
	}

	public Class<PromotionmiscCommonItemActivityListGetResponse> getResponseClass() {
		return PromotionmiscCommonItemActivityListGetResponse.class;
	}

	public void check() throws ApiRuleException {
		RequestCheckUtils.checkNotEmpty(pageNo, "pageNo");
		RequestCheckUtils.checkMinValue(pageNo, 1L, "pageNo");
		RequestCheckUtils.checkNotEmpty(pageSize, "pageSize");
		RequestCheckUtils.checkMaxValue(pageSize, 50L, "pageSize");
		RequestCheckUtils.checkMinValue(pageSize, 1L, "pageSize");
	}
	

}