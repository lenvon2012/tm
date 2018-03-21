package models.promotion;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.PolicySQLGenerator;


@Entity(name = Promotion.TABLE_NAME)
public class Promotion extends GenericModel implements PolicySQLGenerator{
	@Transient
	public static final String TABLE_NAME = "promotion";
	private static final Logger log = LoggerFactory.getLogger(Promotion.class);

	public static class Type {
		public static final String PRICE = "PRICE";
		public static final String DISCOUNT = "DISCOUNT";
	}

	public static class Status {
		public static final String ACTIVE = "ACTIVE";
		public static final String UNACTIVE = "UNACTIVE";
	}
	
	public Promotion(){
		
	}

	public Promotion(Long promotionId,Long userId, Long decreaseNum, String discountType,
			String discountValue, Long startDate, Long endDate,
			Long numIid, String promotionTitle,
			String promotionDesc,Long userTagId,String status) {
		this.id = promotionId;
		this.userId = userId;
		this.decreaseNum = decreaseNum;
		this.discountType = discountType;
		this.discountValue = discountValue;
		this.startDate = startDate;
		this.endDate = endDate;
		this.numIid = numIid;
		this.promotionTitle = promotionTitle;
		this.promotionDesc = promotionDesc;
		this.userTagId = userTagId;
		this.status = status;
	}
	
	public Promotion(Long promotionId,Long userId,Long activityId, Long decreaseNum, String discountType,
	        String discountValue, Long startDate, Long endDate,
	        Long numIid, String promotionTitle,
	        String promotionDesc,Long userTagId,String status) {
	    this.id = promotionId;
	    this.userId = userId;
	    this.activityId=activityId;
	    this.decreaseNum = decreaseNum;
	    this.discountType = discountType;
	    this.discountValue = discountValue;
	    this.startDate = startDate;
	    this.endDate = endDate;
	    this.numIid = numIid;
	    this.promotionTitle = promotionTitle;
	    this.promotionDesc = promotionDesc;
	    this.userTagId = userTagId;
	    this.status = status;
	}
	/**
	 * 关联用户
	 */
	@Index(name="userId")
	Long userId;
	
	/**
	 * 关联活动
	 */
    @Index(name="activityId")
	Long activityId;
	
	/**
	 * 优惠ID (当优惠有效是此值有效)
	 */
	@Id
	public Long id;
	/**
	 * 减价件数，1只减一件，0表示多件
	 */
	public Long decreaseNum;

	/**
	 * 优惠类型，PRICE表示按价格优惠，DISCOUNT表示按折扣优惠
	 */
	public String discountType;

	/**
	 * 优惠额度 (例：100.00)
	 */
	public String discountValue;

	/**
	 * 优惠开始日期
	 */
	public Long startDate;

	/**
	 * 优惠结束日期
	 */
	public Long endDate;

	/**
	 * 商品数字ID
	 */
	@Index(name="numIid")
	private Long numIid;

	/**
	 * 优惠描述
	 */
	public String promotionDesc;

	

	/**
	 * 优惠标题，显示在宝贝详情页面的优惠图标的tip。
	 */
	public String promotionTitle;

	/**
	 * 优惠策略状态，ACTIVE表示有效，UNACTIVE表示无效
	 */
	public String status;

	/**
	 * 对应的人群标签
	 */
	public Long userTagId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getDecreaseNum() {
		return decreaseNum;
	}

	public void setDecreaseNum(Long decreaseNum) {
		this.decreaseNum = decreaseNum;
	}

	public String getDiscountType() {
		return discountType;
	}

	public void setDiscountType(String discountType) {
		this.discountType = discountType;
	}

	public String getDiscountValue() {
		return discountValue;
	}

	public void setDiscountValue(String discountValue) {
		this.discountValue = discountValue;
	}

	public Long getStartDate() {
		return startDate;
	}

	public void setStartDate(Long startDate) {
		this.startDate = startDate;
	}

	public Long getEndDate() {
		return endDate;
	}

	public void setEndDate(Long endDate) {
		this.endDate = endDate;
	}

	public Long getNumIid() {
		return numIid;
	}

	public void setNumIid(Long numIid) {
		this.numIid = numIid;
	}

	public String getPromotionDesc() {
		return promotionDesc;
	}

	public void setPromotionDesc(String promotionDesc) {
		this.promotionDesc = promotionDesc;
	}

	public String getPromotionTitle() {
		return promotionTitle;
	}

	public void setPromotionTitle(String promotionTitle) {
		this.promotionTitle = promotionTitle;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getUserTagId(){
		return this.userTagId;
	}
	
	public void setUserTagId(Long userTagId){
		this.userTagId = userTagId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getActivityId() {
		return activityId;
	}

	public void setActivityId(Long activityId) {
		this.activityId = activityId;
	}
	
	@Override
	public String toString() {
		return "Promotion [userId=" + userId + ", activityId=" + activityId
				+ ", id=" + id + ", decreaseNum=" + decreaseNum
				+ ", discountType=" + discountType + ", discountValue="
				+ discountValue + ", startDate=" + startDate + ", endDate="
				+ endDate + ", numIid=" + numIid + ", promotionDesc="
				+ promotionDesc + ", promotionTitle=" + promotionTitle
				+ ", status=" + status + ", userTagId=" + userTagId + "]";
	}
	
    @Transient
    static String EXIST_ID_QUERY = "select id from " + Promotion.TABLE_NAME + " where id = ? ";

    public static long findExistId(Long promotionId) {
        return JDBCBuilder.singleLongQuery(EXIST_ID_QUERY, promotionId);
    }
	
    @Transient
    static String updateSQL = "update `Promotion` set  `userId` = ?, `activityId` = ?, `decreaseNum` = ?,"
            +
            "`discountType` = ?, `discountValue` = ?, `startDate` = ?, `endDate` = ?, `numIid` = ?" +
            ", `promotionDesc` = ?, `promotionTitle` = ?, `status` = ?, `userTagId` = ? where `id` = ? ";

    public boolean rawUpdate() {

        long updateNum = JDBCBuilder.update(false, updateSQL, this.userId, this.activityId, this.decreaseNum,
                this.discountType, this.discountValue, this.startDate, this.endDate, this.numIid, this.promotionDesc,
                this.promotionTitle,this.status,this.userTagId,this.getId());

        return true;
    }
    
    @Transient
    static String insertSQL = "insert into `Promotion`(`id`,`userId`,`activityId`,`decreaseNum`,`discountType`,"
            + "`discountValue`,`startDate`,`endDate`,`numIid`,`promotionDesc`,`promotionTitle`" +
            ",`status`,`userTagId`) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
    
    public boolean rawInsert() {
        long id = JDBCBuilder.insert(false, insertSQL, this.id,this.userId, this.activityId, this.decreaseNum,
                this.discountType, this.discountValue, this.startDate, this.endDate, this.numIid, this.promotionDesc,
                this.promotionTitle,this.status,this.userTagId);

        if (id > 0L) {
            return true;
        } else {
            return false;
        }

    }
    @Override
    public boolean jdbcSave() {

        try {
            long existdId = findExistId(this.id);

            if (existdId <= 0L) {
                this.rawInsert();
            } else {
                this.rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }
        
        return true;
    }

	@Override
	public String getTableName() {
		// TODO Auto-generated method stub
		return Promotion.TABLE_NAME;
	}

	@Override
	public String getTableHashKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getIdColumn() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getIdName() {
		// TODO Auto-generated method stub
		return null;
	}
    

}
