package bustbapi.response;

import java.util.List;
import com.taobao.api.internal.mapping.ApiField;
import bustbapi.result.CommonItemActivity;
import com.taobao.api.internal.mapping.ApiListField;

import com.taobao.api.TaobaoResponse;

/**
 * TOP API: taobao.promotionmisc.common.item.activity.list.get response.
 * 
 * @author top auto create
 * @since 1.0, null
 */
public class PromotionmiscCommonItemActivityListGetResponse extends TaobaoResponse {

	private static final long serialVersionUID = 4761822351368674556L;

	/** 
	 * 营销活动列表
	 */
	@ApiListField("activity_list")
	@ApiField("common_item_activity")
	private List<CommonItemActivity> activityList;

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


	public void setActivityList(List<CommonItemActivity> activityList) {
		this.activityList = activityList;
	}
	public List<CommonItemActivity> getActivityList( ) {
		return this.activityList;
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
