package models.tmsearch;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.item.ShopInfo;

@Entity(name = UserShopPlay.TABLE_NAME)
public class UserShopPlay extends GenericModel implements PolicySQLGenerator {
    @Transient
    private static final Logger log = LoggerFactory.getLogger(UserShopPlay.class);

    @Transient
    public static final String TABLE_NAME = "usershopplay";

    @Id
    @PolicySQLGenerator.CodeNoUpdate
    private Long userId;
    
    @Index(name = "nick")
    private String nick;

    private Long shopId;
    
    @Column(columnDefinition = " varchar(255) default null")
    private String shopName;

    @Column(columnDefinition = " varchar(255) default null")
    private String shopImgPath;
    
    private int itemNum = 0;
    private int level = 0;// 用户等级 -1表示 天猫
    //private int renqi;
    //private int quality;
    //private String area;
    
    private double saleAmount = 0;
    private int saleNum = 0;
    
    @Column(columnDefinition = " varchar(2048) default null")
    private String priceRangeJson;
    

    private long updateTs = 0L;
    
    private long visitedTs = 0L;
    
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public int getItemNum() {
        return itemNum;
    }

    public void setItemNum(int itemNum) {
        this.itemNum = itemNum;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public double getSaleAmount() {
        return saleAmount;
    }

    public void setSaleAmount(double saleAmount) {
        this.saleAmount = saleAmount;
    }

    

    public String getPriceRangeJson() {
        return priceRangeJson;
    }

    public void setPriceRangeJson(String priceRangeJson) {
        this.priceRangeJson = priceRangeJson;
    }

    public int getSaleNum() {
        return saleNum;
    }

    public void setSaleNum(int saleNum) {
        this.saleNum = saleNum;
    }

    public String getShopImgPath() {
        return shopImgPath;
    }

    public void setShopImgPath(String shopImgPath) {
        this.shopImgPath = shopImgPath;
    }

 
    public long getUpdateTs() {
        return updateTs;
    }

    public void setUpdateTs(long updateTs) {
        this.updateTs = updateTs;
    }

    public long getVisitedTs() {
        return visitedTs;
    }

    public void setVisitedTs(long visitedTs) {
        this.visitedTs = visitedTs;
    }

    public UserShopPlay() {
        
    }
    
    
    public void updateShopInfo(ShopInfo shopInfo, double saleAmount) {
        setItemNum(shopInfo.getItemCount());
        setSaleAmount(saleAmount);
        setSaleNum(shopInfo.getLatestTradeCount());
        setShopId(shopInfo.getShopId());
        setShopImgPath(shopInfo.getPicPath());
        setLevel(shopInfo.getLevel());
        
        long ts = System.currentTimeMillis();
        setUpdateTs(ts);
    }
    
    
    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getIdColumn()
     */
    @Override
    public String getIdColumn() {
        // TODO Auto-generated method stub
        return "userId";
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getIdName()
     */
    @Override
    public String getIdName() {
        // TODO Auto-generated method stub
        return "userId";
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

    static String EXIST_ID_QUERY = "select userId from " + TABLE_NAME + " where  userId = ? ";

    public static long findExistId(long userId) {
        return JDBCBuilder.singleLongQuery(EXIST_ID_QUERY, userId);
    }

    @Override
    public boolean jdbcSave() {
        try {
            long existdId = findExistId(this.userId);

            
            if (existdId == 0L) {
                return this.rawInsert();
            } else {
                setId(existdId);
                return this.rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }
    }

    public boolean rawInsert() {
        
        // TODO Auto-generated method stub
        long id = JDBCBuilder.insert("insert into `" + TABLE_NAME + "`(`userId`,`nick`,`shopId`,`shopName`,`shopImgPath`,`itemNum`,`level`,`saleAmount`,`saleNum`,`priceRangeJson`,`updateTs`,`visitedTs`) values(?,?,?,?,?,?,?,?,?,?,?,?)",
                this.userId, this.nick, this.shopId, this.shopName, this.shopImgPath, this.itemNum, this.level, this.saleAmount, this.saleNum, this.priceRangeJson, this.updateTs, this.visitedTs);

        if (id > 0L) {
            log.info("insert ts for the first time !" + userId);
            return true;
        } else {
            log.error("Insert Fails....." + "[Id : ]" + this.userId);

            return false;
        }
    }

    public boolean rawUpdate() {
        
        long updateNum = JDBCBuilder.insert(
                "update `" + TABLE_NAME + "` set  `nick` = ?, `shopId` = ?, `shopName` = ?, `shopImgPath` = ?, `itemNum` = ?, `level` = ?, `saleAmount` = ?, `saleNum` = ?, `priceRangeJson` = ?, `updateTs` = ?, `visitedTs` = ? where `userId` = ? ", 
                this.nick, this.shopId, this.shopName, this.shopImgPath, this.itemNum, this.level, this.saleAmount, this.saleNum, this.priceRangeJson, this.updateTs, this.visitedTs, 
                this.userId);

        if (updateNum > 0L) {
            log.info("update ts success! " + userId);
            return true;
        } else {
            log.error("update Fails....." + "[Id : ]" + this.userId);

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
        this.userId = id;
    }

    @Override
    public Long getId() {
        // TODO Auto-generated method stub
        return userId;
    }

    
    private static final String UserShopProperties = " userid, nick, shopId, shopName, shopImgPath, itemNum, level, saleAmount, saleNum, priceRangeJson, updateTs, visitedTs ";
    public static UserShopPlay findByNick(String nick) {
        String sql = "select " + UserShopProperties + " from " + TABLE_NAME + " where nick = ?";
        
        UserShopPlay userShop = new JDBCExecutor<UserShopPlay>(sql, nick) {
            @Override
            public UserShopPlay doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    UserShopPlay tempObj = parseUserShop(rs);
                    return tempObj;
                } else
                    return null;
            }
        }.call();
        
        return userShop;
    }
    
    
    public static List<UserShopPlay> findByVisitedTs(long visitedTime) {
        String sql = "select " + UserShopProperties + " from " + TABLE_NAME + " where visitedTs >= ?";
        
        List<UserShopPlay> userShopList = new JDBCExecutor<List<UserShopPlay>>(sql, visitedTime) {
            @Override
            public List<UserShopPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<UserShopPlay> tempList = new ArrayList<UserShopPlay>();
                while (rs.next()) {
                    UserShopPlay tempObj = parseUserShop(rs);
                    if (tempObj == null)
                        continue;
                    tempList.add(tempObj);
                }
                return tempList;
            }
        }.call();
        
        return userShopList;
    }
    
    
    private static UserShopPlay parseUserShop(ResultSet rs) {
        try {
            
            UserShopPlay userShop = new UserShopPlay();
            userShop.setId(rs.getLong(1));
            userShop.setNick(rs.getString(2));
            userShop.setShopId(rs.getLong(3));
            userShop.setShopName(rs.getString(4));
            userShop.setShopImgPath(rs.getString(5));
            userShop.setItemNum(rs.getInt(6));
            userShop.setLevel(rs.getInt(7));
            userShop.setSaleAmount(rs.getDouble(8));
            userShop.setSaleNum(rs.getInt(9));
            userShop.setPriceRangeJson(rs.getString(10));
            userShop.setUpdateTs(rs.getLong(11));
            userShop.setVisitedTs(rs.getLong(12));
            
            return userShop;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }
    
}

