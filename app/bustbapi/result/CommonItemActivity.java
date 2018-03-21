package bustbapi.result;

import java.util.Date;
import com.taobao.api.internal.mapping.ApiField;
import com.taobao.api.TaobaoObject;


/**
 * 通用单品优惠活动
 *
 * @author top auto create
 * @since 1.0, null
 */
public class CommonItemActivity extends TaobaoObject {

	private static final long serialVersionUID = 8132935457547741623L;

	/**
	 * 优惠活动ID
	 */
	@ApiField("activity_id")
	private Long activityId;

	/**
	 * 活动描述，不能超过100字符
	 */
	@ApiField("description")
	private String description;

	/**
	 * 活动结束时间
	 */
	@ApiField("end_time")
	private Date endTime;

	/**
	 * 是否指定人群标签
	 */
	@ApiField("is_user_tag")
	private Boolean isUserTag;

	/**
	 * 活动名称，不能超过32字符
	 */
	@ApiField("name")
	private String name;

	/**
	 * 提供者标识
	 */
	@ApiField("provider_key")
	private String providerKey;

	/**
	 * 卖家ID
	 */
	@ApiField("seller_id")
	private Long sellerId;

	/**
	 * 活动开始时间
	 */
	@ApiField("start_time")
	private Date startTime;

	/**
	 * 人群标签值
	 */
	@ApiField("user_tag")
	private String userTag;


	public Long getActivityId() {
		return this.activityId;
	}
	public void setActivityId(Long activityId) {
		this.activityId = activityId;
	}

	public String getDescription() {
		return this.description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public Date getEndTime() {
		return this.endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Boolean getIsUserTag() {
		return this.isUserTag;
	}
	public void setIsUserTag(Boolean isUserTag) {
		this.isUserTag = isUserTag;
	}

	public String getName() {
		return this.name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getProviderKey() {
		return this.providerKey;
	}
	public void setProviderKey(String providerKey) {
		this.providerKey = providerKey;
	}

	public Long getSellerId() {
		return this.sellerId;
	}
	public void setSellerId(Long sellerId) {
		this.sellerId = sellerId;
	}

	public Date getStartTime() {
		return this.startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public String getUserTag() {
		return this.userTag;
	}
	public void setUserTag(String userTag) {
		this.userTag = userTag;
	}

}
