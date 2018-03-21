package bustbapi.result;

import com.taobao.api.TaobaoObject;
import com.taobao.api.internal.mapping.ApiField;


/**
 * 通用单品优惠详情
 *
 * @author top auto create
 * @since 1.0, null
 */
public class CommonItemDetail extends TaobaoObject {

	private static final long serialVersionUID = 5199953428515663378L;

	/**
	 * 优惠活动ID
	 */
	@ApiField("activity_id")
	private Long activityId;

	/**
	 * 优惠详情ID
	 */
	@ApiField("detail_id")
	private Long detailId;

	/**
	 * 商品ID
	 */
	@ApiField("item_id")
	private Long itemId;

	/**
	 * 优惠类型，只有两种可选值：0-减钱；1-打折
	 */
	@ApiField("promotion_type")
	private Long promotionType;

	/**
	 * 优惠力度，其值的解释方式由promotion_type定义：当为减钱时解释成减钱数量，如：900表示减9元；当为打折时解释成打折折扣，如：900表示打9折
	 */
	@ApiField("promotion_value")
	private Long promotionValue;

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


	public Long getActivityId() {
		return this.activityId;
	}
	public void setActivityId(Long activityId) {
		this.activityId = activityId;
	}

	public Long getDetailId() {
		return this.detailId;
	}
	public void setDetailId(Long detailId) {
		this.detailId = detailId;
	}

	public Long getItemId() {
		return this.itemId;
	}
	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	public Long getPromotionType() {
		return this.promotionType;
	}
	public void setPromotionType(Long promotionType) {
		this.promotionType = promotionType;
	}

	public Long getPromotionValue() {
		return this.promotionValue;
	}
	public void setPromotionValue(Long promotionValue) {
		this.promotionValue = promotionValue;
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

}
