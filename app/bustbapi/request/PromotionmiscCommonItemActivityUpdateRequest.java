package bustbapi.request;

import java.util.Date;
import com.taobao.api.internal.util.RequestCheckUtils;
import java.util.Map;

import com.taobao.api.ApiRuleException;
import com.taobao.api.BaseTaobaoRequest;
import com.taobao.api.internal.util.TaobaoHashMap;

import bustbapi.response.PromotionmiscCommonItemActivityUpdateResponse;

/**
 * TOP API: taobao.promotionmisc.common.item.activity.update request
 * 
 * @author top auto create
 * @since 1.0, 2016.10.20
 */
public class PromotionmiscCommonItemActivityUpdateRequest extends BaseTaobaoRequest<PromotionmiscCommonItemActivityUpdateResponse> {
	
	

	/** 
	* 优惠活动ID
	 */
	private Long activityId;

	/** 
	* 活动描述，不能超过100字符
	 */
	private String description;

	/** 
	* 活动结束时间
	 */
	private Date endTime;

	/** 
	* 是否指定人群标签
	 */
	private Boolean isUserTag;

	/** 
	* 活动名称，不能超过32字符
	 */
	private String name;

	/** 
	* 活动开始时间
	 */
	private Date startTime;

	/** 
	* 用户标签。当is_user_tag为true时，该值才有意义。
	 */
	private String userTag;

	public void setActivityId(Long activityId) {
		this.activityId = activityId;
	}

	public Long getActivityId() {
		return this.activityId;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return this.description;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Date getEndTime() {
		return this.endTime;
	}

	public void setIsUserTag(Boolean isUserTag) {
		this.isUserTag = isUserTag;
	}

	public Boolean getIsUserTag() {
		return this.isUserTag;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getStartTime() {
		return this.startTime;
	}

	public void setUserTag(String userTag) {
		this.userTag = userTag;
	}

	public String getUserTag() {
		return this.userTag;
	}

	public String getApiMethodName() {
		return "taobao.promotionmisc.common.item.activity.update";
	}

	public Map<String, String> getTextParams() {		
		TaobaoHashMap txtParams = new TaobaoHashMap();
		txtParams.put("activity_id", this.activityId);
		txtParams.put("description", this.description);
		txtParams.put("end_time", this.endTime);
		txtParams.put("is_user_tag", this.isUserTag);
		txtParams.put("name", this.name);
		txtParams.put("start_time", this.startTime);
		txtParams.put("user_tag", this.userTag);
		if(this.udfParams != null) {
			txtParams.putAll(this.udfParams);
		}
		return txtParams;
	}

	public Class<PromotionmiscCommonItemActivityUpdateResponse> getResponseClass() {
		return PromotionmiscCommonItemActivityUpdateResponse.class;
	}

	public void check() throws ApiRuleException {
		RequestCheckUtils.checkNotEmpty(activityId, "activityId");
		RequestCheckUtils.checkMinValue(activityId, 1L, "activityId");
		RequestCheckUtils.checkNotEmpty(description, "description");
		RequestCheckUtils.checkMaxLength(description, 100, "description");
		RequestCheckUtils.checkNotEmpty(endTime, "endTime");
		RequestCheckUtils.checkNotEmpty(name, "name");
		RequestCheckUtils.checkMaxLength(name, 32, "name");
		RequestCheckUtils.checkNotEmpty(startTime, "startTime");
	}
	

}