package models.baidufav;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Transient;

import jdbcexecutorwrapper.JDBCLongSetExecutor;
import models.item.ItemPlay;
import models.user.User;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = BaiduFaved.TABLE_NAME)
public class BaiduFaved extends Model implements PolicySQLGenerator {

	@Transient
	private static final Logger log = LoggerFactory.getLogger(BaiduFaved.class);

	@Transient
	public static final String TABLE_NAME = "baidufav";
	
	@Transient
    public static BaiduFaved EMPTY = new BaiduFaved();
    
    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
	private Long numIid;

    @Index(name = "userId")
    private Long userId;
    
    private String picPath;
    
    private double price;
    
    private String title;
    
    private int salesCount;
    
    private Long cid;
    
    public BaiduFaved() {
        super();
    }

    public BaiduFaved(Long userId, Long numIid, String title, String picPath, double price, int salesCount) {
        this.userId = userId;
        this.numIid = numIid;
        this.picPath = picPath;
        this.price = price;
        this.title = title;
        this.salesCount = salesCount;
    }
    
    public BaiduFaved(Long userId, Long numIid, String title, String picPath, double price, int salesCount, Long cid) {
        this.userId = userId;
        this.numIid = numIid;
        this.picPath = picPath;
        this.price = price;
        this.title = title;
        this.salesCount = salesCount;
        this.cid = cid;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return this.userId;
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
    
    public void setPicpath(String picPath) {
    	this.picPath = picPath;
    }
    
    public String getPicPath() {
    	return this.picPath;
    }
    
    public void setCid(Long cid) {
    	this.cid = cid;
    }
    
    public Long getCid() {
    	return this.cid;
    }
   

    public static void remove(User user, String[] numIids) {
    	for(String numIid : numIids){
    		BaiduFaved item = findByUserIdAndNumIid(user.getId(),Long.valueOf(numIid));
	        if (item != null) {
	            //item.delete();
	        	dp.update("delete from "+TABLE_NAME+" where `userId` = ? and numIid = ?", user.getId(), numIid);
	        }
    	}
    }
    
    private static final String SelectAllProperties = " userId, numIid, picPath, price, title, salesCount, cid ";

    public static BaiduFaved findByUserIdAndNumIid(Long userId, Long numIid) {

        String query = "select " + SelectAllProperties + " from " + TABLE_NAME
                + " where userId = ? and numIid = ?";

        return new JDBCBuilder.JDBCExecutor<BaiduFaved>(dp, query, userId, numIid) {

            @Override
            public BaiduFaved doWithResultSet(ResultSet rs)
                    throws SQLException {

                if (rs.next()) {

                    return parseBaiduFaved(rs);

                } else {
                    return null;
                }

            }

        }.call();
    }
    
    private static BaiduFaved parseBaiduFaved(ResultSet rs) {
        try {
            BaiduFaved listCfg = new BaiduFaved();

            listCfg.setUserId(rs.getLong(1));
            listCfg.setNumIid(rs.getLong(2));
            listCfg.setPicpath(rs.getString(3));
            listCfg.setPrice(rs.getDouble(4));
            listCfg.setTitle(rs.getString(5));
            listCfg.setSalesCount(rs.getInt(6));
            listCfg.setCid(rs.getLong(7));
            return listCfg;

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    } 
    
    public static boolean removeAll(User user) {
        return dp.update("delete from "+TABLE_NAME+" where `userId` = ? ", user.getId()) > 0L;
    }
    
    public static Set<Long> findIdsByUser(Long userId){
    	Set<Long> ids = new JDBCLongSetExecutor("select numIid from " + TABLE_NAME + " where userId = ?", userId).call();
        return ids;
    }
    
    public static void add(User user, List<ItemPlay> items) {
    	for(ItemPlay item : items){
    		BaiduFaved fav = findByUserIdAndNumIid(user.getId(),item.getNumIid());
    		if (fav == null) {
                new BaiduFaved(user.getId(), item.getId(),item.title, item.picURL,item.price,item.salesCount).jdbcSave();
            }
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
                .insert("insert into `baidufav`(`userId`,`numIid`,`title`,`picPath`,`price`,`salesCount`,`cid`) values(?,?,?,?,?,?,?)",
                        this.userId, this.numIid, this.title, this.picPath, this.price, this.salesCount, this.cid);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.userId +"[numIid : ]" + this.numIid);
            return false;
        }

    }

    public boolean rawUpdate() {
        long updateNum = dp
                .insert("update `baidufav` set  `userId` = ?, `numIid` = ?, `title` = ?, `picPath` = ?, `price` = ?, `salesCount` = ? , `cid` = ?  where `id` = ? ",
                        this.userId, this.numIid, this.title, this.picPath, this.price, this.salesCount, this.cid, this.id);

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for :" + this.id + "[userId : ]" + this.userId +"[numIid : ]" + this.numIid);

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
        this.id = id;
    }
}
