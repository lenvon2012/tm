package models.defense;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.DBBuilder.DataSrc;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = ItemBuyLimit.TABLE_NAME)
public class ItemBuyLimit extends GenericModel implements PolicySQLGenerator {
    @Transient
    private static final Logger log = LoggerFactory.getLogger(ItemBuyLimit.class);

    @Transient
    public static final String TABLE_NAME = "item_buy_limit";

    @Transient
    public static final String DEFAULT_CLOSEREASON = "系统错误，请联系客服！";

    @Transient
    public static ItemBuyLimit EMPTY = new ItemBuyLimit();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.QUOTA, EMPTY);

    public static class VipLevelCode {
        public static final int ALL = 0;
        public static final int VIP0 = 1;
        public static final int VIP1 = 2;
        public static final int VIP2 = 4;
        public static final int VIP3 = 8;
        public static final int VIP4 = 16;
        public static final int VIP5 = 32;
        public static final int VIP6 = 64;
        public static final int VIP7 = 128;
    }

    @Id
    private Long numIid;

    @Index(name = "userId")
    private Long userId;

    // daysLimit天内限制购买次数tradeNum，最少购买商品个数itemMinNum，最多购买商品个数itemMaxNum
    private int daysLimit;
    private Long tradeNum;
    private Long itemMinNum;
    private Long itemMaxNum;

    private int vipLevel;

    private Long ts;

    private int status;

    private String buyerName;

    private String closeReason;

    public ItemBuyLimit() {

    }

    public ItemBuyLimit(Long userId, Long tradeId, Long numIid, int daysLimit, Long tradeNum, Long itemMinNum,
            Long itemMaxNum, int vipLevel, String buyerName, String closeReason) {
        super();
        this.userId = userId;
        this.numIid = numIid;
        this.daysLimit = daysLimit;
        this.tradeNum = tradeNum;
        this.itemMinNum = itemMinNum;
        this.itemMaxNum = itemMaxNum;
        this.vipLevel = vipLevel;
        this.buyerName = buyerName;
        this.closeReason = closeReason;
    }

    public ItemBuyLimit(ResultSet rs) throws SQLException {
        this.userId = rs.getLong(1);
        this.numIid = rs.getLong(2);
        this.daysLimit = rs.getInt(3);
        this.tradeNum = rs.getLong(4);
        this.itemMinNum = rs.getLong(5);
        this.itemMaxNum = rs.getLong(6);
        this.vipLevel = rs.getInt(7);
        this.ts = rs.getLong(8);
        this.status = rs.getInt(9);
        this.buyerName = rs.getString(10);
        this.closeReason = rs.getString(11);
    }

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

    public int getDaysLimit() {
        return daysLimit;
    }

    public void setDaysLimit(int daysLimit) {
        this.daysLimit = daysLimit;
    }

    public Long getTradeNum() {
        return tradeNum;
    }

    public void setTradeNum(Long tradeNum) {
        this.tradeNum = tradeNum;
    }

    public Long getItemMinNum() {
        return itemMinNum;
    }

    public void setItemMinNum(Long itemMinNum) {
        this.itemMinNum = itemMinNum;
    }

    public Long getItemMaxNum() {
        return itemMaxNum;
    }

    public void setItemMaxNum(Long itemMaxNum) {
        this.itemMaxNum = itemMaxNum;
    }

    public int getVipLevel() {
        return vipLevel;
    }

    public void setVipLevel(int vipLevel) {
        this.vipLevel = vipLevel;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getCloseReason() {
        return closeReason;
    }

    public void setCloseReason(String closeReason) {
        this.closeReason = closeReason;
    }

    @Override
    public String toString() {
        return "ItemBuyLimit [userId=" + userId + ", numIid=" + numIid + ", daysLimit=" + daysLimit + ", tradeNum="
                + tradeNum + ", itemMinNum=" + itemMinNum + ", itemMaxNum=" + itemMaxNum + ", vipLevel=" + vipLevel
                + ", ts=" + ts + ", status=" + status + ", buyerName=" + buyerName + ", closeReason=" + closeReason
                + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getId()
     */
    @Override
    public Long getId() {
        // TODO Auto-generated method stub
        return numIid;
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getIdColumn()
     */
    @Override
    public String getIdColumn() {
        // TODO Auto-generated method stub
        return "id";
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getIdName()
     */
    @Override
    public String getIdName() {
        // TODO Auto-generated method stub
        return "id";
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getTableHashKey()
     */
    @Override
    public String getTableHashKey() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getTableName()
     */
    @Override
    public String getTableName() {
        // TODO Auto-generated method stub
        return TABLE_NAME;
    }

    static String EXIST_ID_QUERY = "select numIid from " + TABLE_NAME + " where userId = ? and numIid = ? ";

    private static long findExistId(Long userId, Long numIid) {
        return dp.singleLongQuery(EXIST_ID_QUERY, userId, numIid);
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#jdbcSave()
     */
    @Override
    public boolean jdbcSave() {

        try {
            // long existdId = findExistId(this.userId, this.numIid);
            // if (existdId != 0)
            // log.info("find existed Id: " + existdId);

            // if (existdId == 0L) {
            // return this.rawInsert();
            // } else {
            // return this.rawUpdate();
            // }

            return rawInsertOnDupKeyUpdate();

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }

    }

    public boolean rawInsert() {
        long id = dp
                .insert("insert into `item_buy_limit`(`userId`,`numIid`,`daysLimit`,`tradeNum`,`itemMinNum`,`itemMaxNum`,`vipLevel`,`ts`,`status`,`buyerName`,`closeReason`) values(?,?,?,?,?,?,?,?,?,?,?)",
                        this.userId, this.numIid, this.daysLimit, this.tradeNum, this.itemMinNum, this.itemMaxNum,
                        this.vipLevel, this.ts, this.status, this.buyerName, this.closeReason);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.userId);
            return false;
        }

    }

    public boolean rawUpdate() {
        long updateNum = dp
                .insert("update `item_buy_limit` set  `userId` = ?, `daysLimit` = ?, `tradeNum` = ?, `itemMinNum` = ?, `itemMaxNum` = ?, `vipLevel` = ?, `ts` = ?, `status` = ?, `buyerName` = ?, `closeReason` = ? where `numIid` = ? ",
                        this.userId, this.daysLimit, this.tradeNum, this.itemMinNum, this.itemMaxNum, this.vipLevel,
                        this.ts, this.status, this.buyerName, this.closeReason, this.getId());

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for :" + this.numIid + "[userId : ]" + this.userId);

            return false;
        }
    }

    public boolean rawInsertOnDupKeyUpdate() {
        long id = dp
                .insert("insert into `item_buy_limit`(`userId`,`numIid`,`daysLimit`,`tradeNum`,`itemMinNum`,`itemMaxNum`,`vipLevel`,`ts`,`status`,`buyerName`,`closeReason`) values(?,?,?,?,?,?,?,?,?,?,?) on duplicate key update `userId` = ?, `daysLimit` = ?, `tradeNum` = ?, `itemMinNum` = ?, `itemMaxNum` = ?, `vipLevel` = ?, `ts` = ?, `status` = ?, `buyerName` = ?, `closeReason` = ? ",
                        this.userId, this.numIid, this.daysLimit, this.tradeNum, this.itemMinNum, this.itemMaxNum,
                        this.vipLevel, this.ts, this.status, this.buyerName, this.closeReason, this.userId,
                        this.daysLimit, this.tradeNum, this.itemMinNum, this.itemMaxNum, this.vipLevel, this.ts,
                        this.status, this.buyerName, this.closeReason);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.userId);
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#setId(java.lang.Long)
     */
    @Override
    public void setId(Long id) {
        // TODO Auto-generated method stub
        this.numIid = id;
    }

}
