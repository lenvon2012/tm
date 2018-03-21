package models.ump;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = ShopMinDiscountPlay.TABLE_NAME)
public class ShopMinDiscountPlay extends GenericModel implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(ShopMinDiscountPlay.class);

    public static final String TABLE_NAME = "shop_min_discount_play";

    public static final ShopMinDiscountPlay EMPTY = new ShopMinDiscountPlay();

    public static final DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    @Id
    private Long userId;
    
    @Index(name = "userNick")
    private String userNick;
    
    @Column(columnDefinition = "int default 0")
    private int minDiscountRate;//
    
    private long createTs;
    
    private long updateTs;
    
    
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserNick() {
        return userNick;
    }

    public void setUserNick(String userNick) {
        this.userNick = userNick;
    }

    public int getMinDiscountRate() {
        return minDiscountRate;
    }

    public void setMinDiscountRate(int minDiscountRate) {
        this.minDiscountRate = minDiscountRate;
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
    
    public ShopMinDiscountPlay() {
        super();
    }

    public ShopMinDiscountPlay(Long userId, String userNick, int minDiscountRate) {
        super();
        this.userId = userId;
        this.userNick = userNick;
        this.minDiscountRate = minDiscountRate;
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
        return "userId";
    }
    
    @Override
    public String getIdName() {
        return "userId";
    }

    @Override
    public Long getId() {
        return userId;
    }

    @Override
    public void setId(Long id) {
        this.userId = id;
    }
    
    public static long findExistId(Long userId) {
        
        String query = "select userId from " + TABLE_NAME + " where userId = ?  ";
        
        
        return dp.singleLongQuery(query, userId);
        
    }

    @Override
    public boolean jdbcSave() {
        long existdId = findExistId(this.userId);
        
        if (existdId <= 0L) {
            return this.rawInsert();
        } else {
            return this.rawUpdate();
        }
    }

    public boolean rawInsert() {
        
        String insertSql = "insert into `" + TABLE_NAME + "`(`userId`,`userNick`,"
                + "`minDiscountRate`,`createTs`,`updateTs`) "
                + "values(?,?,?,?,?)";
        
        this.createTs = System.currentTimeMillis();
        this.updateTs = System.currentTimeMillis();
        
        long id = dp.insert(true, insertSql, this.userId, this.userNick, 
                this.minDiscountRate, this.createTs, this.updateTs);

        // log.info("[Insert Item Id:]" + id + "[userId : ]" + this.userId);

        if (id >= 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.userId);
            return false;
        }

    }

    public boolean rawUpdate() {
        
        String updateSql = "update `" + TABLE_NAME + "` set `userNick` = ?, " 
                + "`minDiscountRate` = ?, `updateTs` = ? " 
                +" where `userId` = ? ";

        
        this.updateTs = System.currentTimeMillis();
        
        
        long updateNum = dp.update(updateSql, this.userNick, 
                this.minDiscountRate, this.updateTs,
                this.userId);

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for :" + this.getId() + "[userId : ]" + this.userId);
            return false;
        }
    }
    
    public static ShopMinDiscountPlay findByUserId(Long userId) {
        
        String query = "select " + SelectAllProperty + " from " + TABLE_NAME 
                + " where userId = ? ";
        
        return new JDBCBuilder.JDBCExecutor<ShopMinDiscountPlay>(dp, query, userId) {

            @Override
            public ShopMinDiscountPlay doWithResultSet(ResultSet rs)
                    throws SQLException {
                if (rs.next()) {
                    return parseShopMinDiscountPlay(rs);
                } else {
                    return null;
                }
            }
            
            
        }.call();
        
    }
    
    private static final String SelectAllProperty = " userId,userNick,"
                + "minDiscountRate,createTs,updateTs ";
    
    private static ShopMinDiscountPlay parseShopMinDiscountPlay(ResultSet rs) {
        
        try {
            
            ShopMinDiscountPlay shopMinDiscount = new ShopMinDiscountPlay();
            
            shopMinDiscount.setUserId(rs.getLong(1));
            shopMinDiscount.setUserNick(rs.getString(2));
            shopMinDiscount.setMinDiscountRate(rs.getInt(3));
            shopMinDiscount.setCreateTs(rs.getLong(4));
            shopMinDiscount.setUpdateTs(rs.getLong(5));
            
            return shopMinDiscount;
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            
            return null;
        }
        
    }
    
}
