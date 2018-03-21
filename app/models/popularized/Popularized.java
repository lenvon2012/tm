
package models.popularized;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import models.user.User;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;
import configs.TMConfigs;

@Entity(name = Popularized.TABLE_NAME)
public class Popularized extends Model implements PolicySQLGenerator {

    public static Popularized EMPTY = new Popularized();

    public static DBDispatcher dp = new DBDispatcher(DataSrc.QUOTA, EMPTY);

    @Transient
    private static final Logger log = LoggerFactory.getLogger(Popularized.class);

    @Transient
    public static final String TABLE_NAME = "popularized";

    @Index(name = "numIid")
    private Long numIid;

    @Index(name = "userId")
    private Long userId;

    private int userVersion;

    private Long firstLoginTime;

    private String picPath;

    private double price;

    private String title;

    private int salesCount;

    private Long cid;

    private Long firstCid;

    // catgory name in our sites
    private String bigCatName;

    @Column(columnDefinition = "decimal(10,2) DEFAULT 0")
    private double skuMinPrice = 0;//打折价

    @Transient
    private String itemCode;

    public static class PopularizedStatus {
        public static final int Normal = 1;

        public static final int HotSale = 2;

        public static final int Try = 4;//体验版 
    }

    @Column(columnDefinition = "int DEFAULT 1")
    private int status = PopularizedStatus.Normal;

    public Popularized() {
        super();
    }

    public Popularized(Long userId, Long numIid, String title, String picPath, double price, int salesCount,
            int userVersion, Long firstLoginTime, Long firstCid) {
        this.userId = userId;
        this.numIid = numIid;
        this.picPath = picPath;
        this.price = price;
        this.title = title;
        this.userVersion = userVersion;
        this.firstLoginTime = firstLoginTime;
        this.firstCid = firstCid;
        this.salesCount = salesCount;
        this.bigCatName = TMConfigs.PopularizeConfig.bigCidNameMap.get(firstCid) == null ? "随意淘"
                : TMConfigs.PopularizeConfig.bigCidNameMap.get(firstCid);
    }

    public Popularized(Long userId, Long numIid, String title, String picPath, double price, int salesCount, Long cid,
            int userVersion, Long firstLoginTime, Long firstCid) {
        this.userId = userId;
        this.numIid = numIid;
        this.picPath = picPath;
        this.price = price;
        this.title = title;
        this.salesCount = salesCount;
        this.userVersion = userVersion;
        this.firstLoginTime = firstLoginTime;
        this.firstCid = firstCid;
        this.cid = cid;
        this.bigCatName = TMConfigs.PopularizeConfig.bigCidNameMap.get(firstCid) == null ? "随意淘"
                : TMConfigs.PopularizeConfig.bigCidNameMap.get(firstCid);
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return this.userId;
    }

    public Long getFirstCid() {
        return firstCid;
    }

    public void setFirstCid(Long firstCid) {
        this.firstCid = firstCid;
    }

    public void setBigCatName(String name) {
        this.bigCatName = name;
    }

    public String getBigCatName() {
        return this.bigCatName;
    }

    public void setNumIid(Long numIid) {
        this.numIid = numIid;
    }

    public Long getNumIid() {
        return this.numIid;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPrice() {
        return this.price;
    }

    public void setSalesCount(int salesCount) {
        this.salesCount = salesCount;
    }

    public int getSalesCount() {
        return this.salesCount;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    public String getPicPath() {
        return this.picPath;
    }

    public void setFirstLoginTime(Long firstLoginTime) {
        this.firstLoginTime = firstLoginTime;
    }

    public int getUserVersion() {
        return userVersion;
    }

    public Long getFirstLoginTime() {
        return firstLoginTime;
    }

    public void setUserVersion() {
        this.userVersion = userVersion;
    }

    public void setUserVersion(int userVersion) {
        this.userVersion = userVersion;
    }

    public void setCid(Long cid) {
        this.cid = cid;
    }

    public Long getCid() {
        return this.cid;
    }

    public double getSkuMinPrice() {
        return skuMinPrice;
    }

    public void setSkuMinPrice(double skuMinPrice) {
        this.skuMinPrice = skuMinPrice;
    }

    //每次更新时都重置一下skuMinPrice
    public void resetSkuMinPrice() {
        this.skuMinPrice = 0;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean hasStatus(int status) {
        boolean flag = (this.status & status) > 0;
        return flag;
    }

    public void addStatus(int status) {
        this.status = this.status | status;
    }

    public void removeStatus(int status) {
        this.status = this.status & (~status);
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public static void add(User user, long numIid2, String title, String picPath, double price, int salesCount,
            int userVersion, Long firstLoginTime, Long firstCid) {
        //Popularized item = Popularized.find("userId = ? and numIid = ?", user.getId(), numIid2).first();
    	Popularized item = Popularized.findByNumIid(user.getId(), numIid2);
        if (item == null) {
            new Popularized(user.getId(), numIid2, title, picPath, price, salesCount, item.getCid(), userVersion,
                    firstLoginTime, firstCid).jdbcSave();
        }
    }

    public static void remove(User user, long numIid) {
        dp.update(true, " delete from " + Popularized.TABLE_NAME + " where numIid = ? and userId = ? ",
                numIid, user.getId());
    }

    public static void removeAll(User user) {
        //Popularized.delete("userId = ?", user.getId());
    	dp.update(true, " delete from " + Popularized.TABLE_NAME + " where userId = ? ",
                 user.getId());
    }

    public static Popularized findByNumIid(Long userId, Long numIid) {

        String query = "select " + SelectAllProperties + " from " + Popularized.TABLE_NAME
                + " where userId = ? and numIid = ?";

        return new JDBCBuilder.JDBCExecutor<Popularized>(dp, query, userId, numIid) {

            @Override
            public Popularized doWithResultSet(ResultSet rs)
                    throws SQLException {

                if (rs.next()) {

                    return parsePopularized(rs);

                } else {
                    return null;
                }

            }

        }.call();
    }
    
    public static Popularized findFirstByNumIid(Long numIid) {

        String query = "select " + SelectAllProperties + " from " + Popularized.TABLE_NAME
                + " where numIid = ? limit 1";

        return new JDBCBuilder.JDBCExecutor<Popularized>(dp, query, numIid) {

            @Override
            public Popularized doWithResultSet(ResultSet rs)
                    throws SQLException {

                if (rs.next()) {

                    return parsePopularized(rs);

                } else {
                    return null;
                }

            }

        }.call();
    }
    
    public static Popularized findFirstByUserId(Long userId) {

        String query = "select " + SelectAllProperties + " from " + Popularized.TABLE_NAME
                + " where userId = ? limit 1";

        return new JDBCBuilder.JDBCExecutor<Popularized>(dp, query, userId) {

            @Override
            public Popularized doWithResultSet(ResultSet rs)
                    throws SQLException {

                if (rs.next()) {

                    return parsePopularized(rs);

                } else {
                    return null;
                }

            }

        }.call();
    }
    
    public static List<Popularized> findAllPopularizeds() {

        String query = "select " + SelectAllProperties + " from " + Popularized.TABLE_NAME;

        return new JDBCBuilder.JDBCExecutor<List<Popularized>>(dp, query) {

            @Override
            public List<Popularized> doWithResultSet(ResultSet rs)
                    throws SQLException {
            	List<Popularized> list = new ArrayList<Popularized>();
                while (rs.next()) {
                    list.add(parsePopularized(rs));
                } 
                return list;
            }

        }.call();
    }
    
    private static final String SelectAllProperties = " userId,numIid,userVersion,firstLoginTime,picPath,price,title,salesCount,cid,firstCid,bigCatName,skuMinPrice,status ";

    private static Popularized parsePopularized(ResultSet rs) {
        try {
        	Popularized listCfg = new Popularized(rs.getLong(1),rs.getLong(2),rs.getString(7),rs.getString(5),rs.getDouble(6),
        			rs.getInt(8),rs.getLong(9),rs.getInt(3),rs.getLong(4),rs.getLong(10));
        	listCfg.setBigCatName(rs.getString(11));
        	listCfg.setSkuMinPrice(rs.getDouble(12));
        	//listCfg.setItemCode(rs.getString(13));
        	listCfg.setStatus(rs.getInt(13));
            return listCfg;

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    } 

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getId()
     */
    @Override
    public Long getId() {
        // TODO Auto-generated method stub
        return id;
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

    static String EXIST_ID_QUERY = "select id from " + TABLE_NAME + " where userId = ? and numIid = ?";

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
            long existdId = findExistId(this.userId, this.numIid);
//            if (existdId != 0)
//                log.info("find existed Id: " + existdId);

            if (existdId == 0L) {
                return this.rawInsert();
            } else {
                id = existdId;
                return this.rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }

    }

    public boolean rawInsert() {
        long id = dp
                .insert("insert into `popularized`(`userId`,`numIid`,`title`,`picPath`,`price`,`salesCount`,`cid`,`userVersion`,`firstLoginTime`,`firstCid`, `bigCatName`, `skuMinPrice`, `status`) values(?,?,?,?,?,?,?,?,?,?,?,?,?)",
                        this.userId, this.numIid, this.title, this.picPath, this.price, this.salesCount, this.cid,
                        this.userVersion, this.firstLoginTime, this.firstCid, this.bigCatName, this.skuMinPrice,
                        this.status);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.userId + "[numIid : ]" + this.numIid);
            return false;
        }

    }

    public boolean rawUpdate() {
        long updateNum = dp
                .insert("update `popularized` set  `userId` = ?, `numIid` = ?,"
                        + "`title` = ?, `picPath` = ?, `price` = ?, `salesCount` = ? ,"
                        + "`cid` = ?, `userVersion` = ?, `firstLoginTime` = ?,`firstCid` = ?,"
                        + "`bigCatName` = ?,`skuMinPrice` = ?,`status` = ? where `id` = ? ",
                        this.userId, this.numIid, this.title, this.picPath, this.price, this.salesCount, this.cid,
                        this.userVersion, this.firstLoginTime, this.firstCid, this.bigCatName, this.skuMinPrice,
                        this.status,
                        this.id);
        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for :" + this.id + "[userId : ]" + this.userId + "[numIid : ]" + this.numIid);

            return false;
        }
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public void setId(Long id) {
        this.id = id;
    }
}
