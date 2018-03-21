package models.pricelog;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = EditItemPriceLog.TABLE_NAME)
public class EditItemPriceLog extends Model implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(EditItemPriceLog.class);

    public static final String TABLE_NAME = "edit_item_price_log";

    public static final EditItemPriceLog EMPTY = new EditItemPriceLog();

    public static final DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    @Index(name = "userId")
    private Long userId;
    
    @Index(name = "numIid")
    private Long numIid;
    
    private String originPrice;
    
    private String newPrice;
    
    
    
    @Lob
    private String skuPriceJson;
    
    public static class EditItemPriceStatus {
        public static final int AllSuccess = 1;
        public static final int SomeSkuFail = 2;
    }
    
    @Column(columnDefinition = "int default 0")
    private int status;
    
    @Column(columnDefinition = "int default 0")
    private int successSkuNum;
    
    @Column(columnDefinition = "int default 0")
    private int failSkuNum;
    
    
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

    public String getOriginPrice() {
        return originPrice;
    }

    public void setOriginPrice(String originPrice) {
        this.originPrice = originPrice;
    }

    public String getNewPrice() {
        return newPrice;
    }

    public void setNewPrice(String newPrice) {
        this.newPrice = newPrice;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getSuccessSkuNum() {
        return successSkuNum;
    }

    public void setSuccessSkuNum(int successSkuNum) {
        this.successSkuNum = successSkuNum;
    }

    public int getFailSkuNum() {
        return failSkuNum;
    }

    public void setFailSkuNum(int failSkuNum) {
        this.failSkuNum = failSkuNum;
    }

    public String getSkuPriceJson() {
        return skuPriceJson;
    }

    public void setSkuPriceJson(String skuPriceJson) {
        this.skuPriceJson = skuPriceJson;
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

    public EditItemPriceLog() {
        super();
    }


    public EditItemPriceLog(Long userId, Long numIid, String originPrice,
            String newPrice, String skuPriceJson, int status,
            int successSkuNum, int failSkuNum) {
        super();
        this.userId = userId;
        this.numIid = numIid;
        this.originPrice = originPrice;
        this.newPrice = newPrice;
        this.skuPriceJson = skuPriceJson;
        this.status = status;
        this.successSkuNum = successSkuNum;
        this.failSkuNum = failSkuNum;
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

    
    public boolean rawInsert() {
        
        String insertSql = "insert into `" + TABLE_NAME + "`(`userId`,`numIid`,"
                + "`originPrice`,`newPrice`,"
                + "`skuPriceJson`,`status`,`successSkuNum`,`failSkuNum`," 
                + "`createTs`,`updateTs`) "
                + "values(?,?,?,?,?,?,?,?,?,?)";
        
        this.createTs = System.currentTimeMillis();
        this.updateTs = System.currentTimeMillis();
        
        long id = dp.insert(true, insertSql, this.userId, this.numIid, 
                this.originPrice, this.newPrice, 
                this.skuPriceJson, this.status, this.successSkuNum, this.failSkuNum,
                this.createTs, this.updateTs);

        // log.info("[Insert Item Id:]" + id + "[userId : ]" + this.userId);

        if (id >= 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.userId);
            return false;
        }

    }

    
    public boolean rawUpdate() {
        
        String updateSql = "update `" + TABLE_NAME + "%s` set `userId` = ?, `numIid` = ?, " 
                + "`originPrice` = ?, `newPrice` = ?, " 
                + "`skuPriceJson` = ?, `status` = ?, `successSkuNum` = ?, `failSkuNum` = ?, " 
                + "`updateTs` = ? " 
                +" where `id` = ?  ";
        
        this.updateTs = System.currentTimeMillis();
        
        
        long updateNum = dp.update(updateSql, this.userId, this.numIid, 
                this.originPrice, this.newPrice, 
                this.skuPriceJson, this.status, this.successSkuNum, this.failSkuNum,
                this.updateTs,
                this.id);

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for :" + this.getId() + "[userId : ]" + this.userId);
            return false;
        }
    }
    
    
    public static class SingleSkuPriceLog {
        private String properties;
        private String originPrice;
        
        private String newPrice;

        public String getProperties() {
            return properties;
        }

        public void setProperties(String properties) {
            this.properties = properties;
        }

        public String getOriginPrice() {
            return originPrice;
        }

        public void setOriginPrice(String originPrice) {
            this.originPrice = originPrice;
        }

        public String getNewPrice() {
            return newPrice;
        }

        public void setNewPrice(String newPrice) {
            this.newPrice = newPrice;
        }

        public SingleSkuPriceLog() {
            super();
        }

        public SingleSkuPriceLog(String properties, String originPrice,
                String newPrice) {
            super();
            this.properties = properties;
            this.originPrice = originPrice;
            this.newPrice = newPrice;
        }
        
        
    }
}
