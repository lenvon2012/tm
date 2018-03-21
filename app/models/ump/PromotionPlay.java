package models.ump;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

import models.promotion.TMProActivity;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.DBBuilder.DataSrc;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;
import dao.ump.PromotionDao;

@Entity(name = PromotionPlay.TABLE_NAME)
public class PromotionPlay extends GenericModel implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(PromotionPlay.class);

    public static final String TABLE_NAME = "promotionplay";

    public static final PromotionPlay EMPTY = new PromotionPlay();

    public static final DBDispatcher dp = new DBDispatcher(DataSrc.QUOTA, EMPTY);
    
    @Id
    private Long promotionId;//即淘宝api ItemPromotion中的activity_id
    
    @Index(name = "userId")
    private Long userId;
    
    @Index(name = "tmActivityId")
    private Long tmActivityId;//我们应用自己的活动ID
    
    @Index(name = "numIid")
    private Long numIid;
    
    private boolean isUserTag;
    
    private String userTagValue;
    
    public enum ItemPromoteType {
        decrease, discount
    }
    
    @Enumerated(EnumType.STRING)
    private ItemPromoteType promotionType;
    
    
    /**
     * 减多少钱。当is_decrease_money为true时，该值才有意义。注意：该值单位为分，即100表示1元。
     */
    private long decreaseAmount;
    

    /**
     * 折扣值。当is_discount为true时，该值才有意义。注意：800表示8折。
     */
    private long discountRate;
    
    
    public static class TMPromotionStatus {
        public static final int Active = 1;
        
        public static final int UnActive = 2;
    }
    
    
    private int tmStatus;
    
    
    
    private long createTs;
    
    //活动修改的ts
    private long updateTs;
    
    
    
    public static class PromotionParams {
        
        
        private Long numIid;
        
        private ItemPromoteType promotionType;
        
        private long decreaseAmount;
        
        private long discountRate;


        public Long getNumIid() {
            return numIid;
        }

        public void setNumIid(Long numIid) {
            this.numIid = numIid;
        }

        public ItemPromoteType getPromotionType() {
            return promotionType;
        }

        public void setPromotionType(ItemPromoteType promotionType) {
            this.promotionType = promotionType;
        }

        public long getDecreaseAmount() {
            return decreaseAmount;
        }

        public void setDecreaseAmount(long decreaseAmount) {
            this.decreaseAmount = decreaseAmount;
        }

        public long getDiscountRate() {
            return discountRate;
        }

        public void setDiscountRate(long discountRate) {
            this.discountRate = discountRate;
        }
        
        
    }
    
    
    public boolean isActive() {
        return (this.tmStatus & TMPromotionStatus.Active) > 0;
    }
    

    public Long getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(Long promotionId) {
        this.promotionId = promotionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTmActivityId() {
        return tmActivityId;
    }

    public void setTmActivityId(Long tmActivityId) {
        this.tmActivityId = tmActivityId;
    }

    public Long getNumIid() {
        return numIid;
    }

    public void setNumIid(Long numIid) {
        this.numIid = numIid;
    }

    public boolean isUserTag() {
        return isUserTag;
    }

    public void setUserTag(boolean isUserTag) {
        this.isUserTag = isUserTag;
    }

    public String getUserTagValue() {
        return userTagValue;
    }

    public void setUserTagValue(String userTagValue) {
        this.userTagValue = userTagValue;
    }

    public ItemPromoteType getPromotionType() {
        return promotionType;
    }

    public void setPromotionType(ItemPromoteType promotionType) {
        this.promotionType = promotionType;
    }

    public long getDecreaseAmount() {
        return decreaseAmount;
    }

    public void setDecreaseAmount(long decreaseAmount) {
        this.decreaseAmount = decreaseAmount;
    }

    public long getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(long discountRate) {
        this.discountRate = discountRate;
    }

    public int getTmStatus() {
        return tmStatus;
    }

    public void setTmStatus(int tmStatus) {
        this.tmStatus = tmStatus;
    }

    public long getCreateTs() {
        return createTs;
    }

    public void setCreateTs(long createTs) {
        this.createTs = createTs;
    }

    public long getUpdateTs() {
        return updateTs;
    }

    public void setUpdateTs(long updateTs) {
        this.updateTs = updateTs;
    }

    public PromotionPlay() {
        super();
    }
    
    public PromotionPlay(Long userId, Long promotionId, 
            TMProActivity tmActivity, PromotionParams params, int tmStatus) {
        
        super();
        this.userId = userId;
        this.promotionId = promotionId;
        this.tmStatus = tmStatus;
        
        updateFromPromotionParams(tmActivity, params);
    }
    
    private void updateFromActivity(TMProActivity tmActivity) {
        this.tmActivityId = tmActivity.getTMActivityId();
    }
    
    public void updateFromPromotionParams(TMProActivity tmActivity, PromotionParams params) {
        updateFromActivity(tmActivity);

        this.promotionType = params.promotionType;
        
        if (ItemPromoteType.decrease.equals(params.getPromotionType())) {
            this.decreaseAmount = params.getDecreaseAmount();
            this.discountRate = 0;
        } else if (ItemPromoteType.discount.equals(params.getPromotionType())) {
            this.decreaseAmount = 0;
            this.discountRate = params.getDiscountRate();
        } else {
            this.decreaseAmount = 0;
            this.discountRate = 0;
        }
        
        
        this.numIid = params.getNumIid();
    }
    
    public PromotionParams genPromotionParams() {
        PromotionParams params = new PromotionParams();
        params.setDecreaseAmount(this.decreaseAmount);
        params.setDiscountRate(this.discountRate);
        params.setNumIid(this.numIid);
        params.setPromotionType(this.promotionType);
        
        return params;
    }
    
    
    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getTableHashKey() {
        return null;
    }

    @Override
    public String getIdColumn() {
        return "promotionId";
    }
    
    @Override
    public String getIdName() {
        return "promotionId";
    }

    @Override
    public Long getId() {
        return promotionId;
    }

    @Override
    public void setId(Long id) {
        this.promotionId = id;
    }

    
    
    public static long findExistId(Long userId, Long promotionId) {
        
        String query = "select promotionId from " + TABLE_NAME + "%s where userId = ? and promotionId = ? ";
        
        query = PromotionDao.genShardQuery(query, userId);
        
        return dp.singleLongQuery(query, userId, promotionId);
        
    }
    
    
    @Override
    public boolean jdbcSave() {
        
        long existdId = findExistId(this.userId, this.promotionId);
        
        if (existdId <= 0L) {
            return this.rawInsert();
        } else {
            return this.rawUpdate();
        }
    }
    
    
    public boolean rawDelete() {
        
        String deleteSql = "delete from " + TABLE_NAME + "%s where userId = ? and promotionId = ? ";
        
        deleteSql = PromotionDao.genShardQuery(deleteSql, userId);
        
        long deleteNum = dp.update(deleteSql, userId, promotionId);
        
        return true;
    }
    
    
    public boolean rawInsert() {
        
        String insertSql = "insert into `" + TABLE_NAME + "%s`(`promotionId`,`userId`,"
                + "`tmActivityId`,`numIid`,"
                + "`isUserTag`,`userTagValue`,`promotionType`,"
                + "`decreaseAmount`,`discountRate`,`tmStatus`,`createTs`,`updateTs`) "
                + "values(?,?,?,?,?,?,?,?,?,?,?,?)";
        
        insertSql = PromotionDao.genShardQuery(insertSql, userId);
        
        this.createTs = System.currentTimeMillis();
        this.updateTs = System.currentTimeMillis();
        
        long id = dp.insert(true, insertSql, this.promotionId, this.userId, 
                this.tmActivityId, this.numIid, 
                this.isUserTag, this.userTagValue, getPromotionTypeStr(),
                this.decreaseAmount, this.discountRate, this.tmStatus, this.createTs, this.updateTs);

        // log.info("[Insert Item Id:]" + id + "[userId : ]" + this.userId);

        if (id >= 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.userId);
            return false;
        }

    }

    
    private String getPromotionTypeStr() {
        if (this.promotionType == null) {
            return "";
        } else {
            return this.promotionType.toString();
        }
    }
    
    public boolean rawUpdate() {
        
        String updateSql = "update `" + TABLE_NAME + "%s` set `tmActivityId` = ?, `numIid` = ?, " 
                + "`isUserTag` = ?, `userTagValue` = ?, `promotionType` = ?," 
                + "`decreaseAmount` = ?,`discountRate` = ?, `tmStatus` = ?, `updateTs` = ? " 
                +" where `userId` = ? and `promotionId` = ? ";

        updateSql = PromotionDao.genShardQuery(updateSql, userId);
        
        this.updateTs = System.currentTimeMillis();
        
        
        long updateNum = dp.update(updateSql, this.tmActivityId, this.numIid, 
                this.isUserTag, this.userTagValue, getPromotionTypeStr(),
                this.decreaseAmount, this.discountRate, this.tmStatus, this.updateTs,
                this.userId, this.promotionId);

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for :" + this.getId() + "[userId : ]" + this.userId);
            return false;
        }
    }
    
    
    
    
}   
