package models.ump;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = ShopMinDiscountGetLog.TABLE_NAME)
public class ShopMinDiscountGetLog extends Model implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(ShopMinDiscountGetLog.class);

    public static final String TABLE_NAME = "shop_min_discount_get_log";

    public static final ShopMinDiscountGetLog EMPTY = new ShopMinDiscountGetLog();

    public static final DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    
    @Index(name = "userId")
    private Long userId;
    
    @Index(name = "numIid")
    private Long numIid;
    
    private Long promotionId;
    
    @Column(columnDefinition = "int default 0")
    private int deleteTimes;
    
    public enum ShopMinDiscountTMStatus {
        /*public static final int Started = 1;//开始调用api来打折
        
        public static final int Promotioned = 2;//促销成功
        
        public static final int GetShopDiscountFail = 4;
        
        public static final int Finished = 8;
        
        public static final int DeletePromotionError = 16;*/
        
        None, Started, Promotioned, GetShopDiscountFail, Finished, DeletePromotionError
        
    }
    
    @Enumerated(EnumType.STRING)
    @Column(length = 255, nullable = false)
    private ShopMinDiscountTMStatus tmStatus = ShopMinDiscountTMStatus.None;
    
    public enum ShopMinDiscountApiStatus {
        /*public static final int PromotionAuthOutDate = 1;//授权过期
        public static final int PromotionSuccess = 2;
        public static final int PromotionMinDiscountFail = 4;
        public static final int PromotionOtherFail = 8;
        
        public static final int DeleteSuccess = 16;
        public static final int DeleteFail = 32;*/
        
        None,
        PromotionAuthOutDate, PromotionSuccess, PromotionMinDiscountFail,
        PromotionOtherFail, DeleteSuccess, DeleteFail
        
    }
    
    @Enumerated(EnumType.STRING)
    @Column(length = 255, nullable = false)
    private ShopMinDiscountApiStatus apiStatus = ShopMinDiscountApiStatus.None; 
    
    private long usedTime;
    
    private long createTs;
    
    private long updateTs;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getNumIid() {
        return numIid;
    }

    public void setNumIid(Long numIid) {
        this.numIid = numIid;
    }

    public Long getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(Long promotionId) {
        this.promotionId = promotionId;
    }

    
    public int getDeleteTimes() {
        return deleteTimes;
    }

    public void setDeleteTimes(int deleteTimes) {
        this.deleteTimes = deleteTimes;
    }

    public ShopMinDiscountTMStatus getTmStatus() {
        return tmStatus;
    }

    public void setTmStatus(ShopMinDiscountTMStatus tmStatus) {
        this.tmStatus = tmStatus;
    }

    public ShopMinDiscountApiStatus getApiStatus() {
        return apiStatus;
    }

    public void setApiStatus(ShopMinDiscountApiStatus apiStatus) {
        this.apiStatus = apiStatus;
    }

    public long getUsedTime() {
        return usedTime;
    }

    public void setUsedTime(long usedTime) {
        this.usedTime = usedTime;
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

    public ShopMinDiscountGetLog() {
        super();
    }

    public ShopMinDiscountGetLog(Long userId, Long numIid, ShopMinDiscountTMStatus tmStatus) {
        super();
        this.userId = userId;
        this.numIid = numIid;
        this.tmStatus = tmStatus;
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
        return "id";
    }

    @Override
    public String getIdName() {
        return "id";
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public static long findExistId(Long id) {
        
        String query = "select id from " + TABLE_NAME + " where id = ?  ";
        
        
        return dp.singleLongQuery(query, id);
        
    }

    @Override
    public boolean jdbcSave() {
        long existdId = findExistId(this.id);
        
        if (existdId <= 0L) {
            return this.rawInsert();
        } else {
            return this.rawUpdate();
        }
    }
    
    private void enSureStatus() {
        if (this.tmStatus == null) {
            this.tmStatus = ShopMinDiscountTMStatus.None;
        }
        if (this.apiStatus == null) {
            this.apiStatus = ShopMinDiscountApiStatus.None;
        }
    }

    public boolean rawInsert() {
        
        String insertSql = "insert into `" + TABLE_NAME + "`(`userId`,`numIid`,"
                + "`promotionId`,`deleteTimes`,`tmStatus`,`apiStatus`,"
                + "`usedTime`,`createTs`,`updateTs`) "
                + "values(?,?,?,?,?,?,?,?,?)";
        
        this.createTs = System.currentTimeMillis();
        this.updateTs = System.currentTimeMillis();
        
        enSureStatus();
        
        long id = dp.insert(true, insertSql, this.userId, this.numIid, 
                this.promotionId, this.deleteTimes, this.tmStatus.toString(), this.apiStatus.toString(),
                this.usedTime, this.createTs, this.updateTs);

        // log.info("[Insert Item Id:]" + id + "[userId : ]" + this.userId);

        if (id >= 0L) {
            setId(id);
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.userId);
            return false;
        }

    }

    public boolean rawUpdate() {
        
        String updateSql = "update `" + TABLE_NAME + "` set `userId` = ?, `numIid` = ?, " 
                + "`promotionId` = ?, `deleteTimes` = ?, `tmStatus` = ?, `apiStatus` = ?, " 
                + "`usedTime` = ?, `updateTs` = ? " 
                +" where `id` = ? ";

        
        this.updateTs = System.currentTimeMillis();
        this.usedTime = this.updateTs - this.createTs;
        
        enSureStatus();
        
        long updateNum = dp.update(updateSql, this.userId, this.numIid,
                this.promotionId, this.deleteTimes, this.tmStatus.toString(), this.apiStatus.toString(),
                this.usedTime, this.updateTs,
                this.id);

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for :" + this.getId() + "[userId : ]" + this.userId);
            return false;
        }
    }
    
    
    
    
    
}
