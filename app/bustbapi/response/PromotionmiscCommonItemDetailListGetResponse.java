package bustbapi.response;

import java.util.List;

import bustbapi.result.CommonItemDetail;

import com.taobao.api.TaobaoResponse;
import com.taobao.api.internal.mapping.ApiField;
import com.taobao.api.internal.mapping.ApiListField;

/**
 * TOP API: taobao.promotionmisc.common.item.detail.list.get response.
 * 
 * @author top auto create
 * @since 1.0, null
 */
public class PromotionmiscCommonItemDetailListGetResponse extends TaobaoResponse {

	private static final long serialVersionUID = 5136488948239516998L;

	/** 
	 * 活动详情列表
	 */
	@ApiListField("detail_list")
	@ApiField("common_item_detail")
	private List<CommonItemDetail> detailList;

	/** 
	 * 是否查询成功
	 */
	@ApiField("is_success")
	private Boolean isSuccess;

	/** 
	 * 数据总数量
	 */
	@ApiField("total_count")
	private Long totalCount;


	public void setDetailList(List<CommonItemDetail> detailList) {
		this.detailList = detailList;
	}
	public List<CommonItemDetail> getDetailList( ) {
		return this.detailList;
	}

	public void setIsSuccess(Boolean isSuccess) {
		this.isSuccess = isSuccess;
	}
	public Boolean getIsSuccess( ) {
		return this.isSuccess;
	}

	public void setTotalCount(Long totalCount) {
		this.totalCount = totalCount;
	}
	public Long getTotalCount( ) {
		return this.totalCount;
	}
	


}
