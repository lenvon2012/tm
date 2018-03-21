package models.promoted;

import java.sql.ResultSet;
import java.sql.SQLException;

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

@Entity(name = Promoted.TABLE_NAME)
public class Promoted extends Model implements PolicySQLGenerator {

	public static Promoted EMPTY = new Promoted();

	public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

	@Transient
	private static final Logger log = LoggerFactory
			.getLogger(Promoted.class);

	@Transient
	public static final String TABLE_NAME = "promoted";

	@Index(name = "numIid")
	private Long numIid;

	@Index(name = "userId")
	private Long userId;

	private int userVersion;

	private String picPath;

	private double origPrice;

	private double price;

	private Long cid;

	private String title;

	public Promoted(Long userId, Long numIid, String title, int userVersion,
			String picPath, double origPrice, double price, Long cid) {
		this.userId = userId;
		this.numIid = numIid;
		this.userVersion = userVersion;
		this.picPath = picPath;
		this.origPrice = origPrice;
		this.price = price;
		this.cid = cid;
		this.title = title;
	}

	public Promoted() {
		super();
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
	public void setId(Long id) {
		this.id = id;
	}

	static String EXIST_ID_QUERY = "select id from " + TABLE_NAME
			+ " where userId = ? and numIid = ?";

	private static long findExistId(Long userId, Long numIid) {
		return dp.singleLongQuery(EXIST_ID_QUERY, userId, numIid);
	}
	
	public static Promoted findByNumIid(Long userId, Long numIid) {

        String query = "select " + SelectAllProperties + " from " + TABLE_NAME
                + " where userId = ? and numIid = ?";

        return new JDBCBuilder.JDBCExecutor<Promoted>(dp, query, userId, numIid) {

            @Override
            public Promoted doWithResultSet(ResultSet rs)
                    throws SQLException {

                if (rs.next()) {

                    return parsePromoted(rs);

                } else {
                    return null;
                }

            }

        }.call();
    }
    
    private static final String SelectAllProperties = " userId,numIid,userVersion,picPath,origPrice,price,cid,title ";

    private static Promoted parsePromoted(ResultSet rs) {
        try {
        	Promoted listCfg = new Promoted(rs.getLong(1),rs.getLong(2),rs.getString(8),rs.getInt(3),rs.getString(4),rs.getDouble(5),
        			rs.getDouble(6),rs.getLong(7));
            return listCfg;

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    } 

	@Override
	public boolean jdbcSave() {

		try {
			long existdId = findExistId(this.userId, this.numIid);

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

	@Override
	public String getIdName() {
		return "id";
	}

	public Long getNumIid() {
		return numIid;
	}

	public void setNumIid(Long numIid) {
		this.numIid = numIid;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public int getUserVersion() {
		return userVersion;
	}

	public void setUserVersion(int userVersion) {
		this.userVersion = userVersion;
	}

	public String getPicPath() {
		return picPath;
	}

	public void setPicPath(String picPath) {
		this.picPath = picPath;
	}

	public double getOrigPrice() {
		return origPrice;
	}

	public void setOrigPrice(double origPrice) {
		this.origPrice = origPrice;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public Long getCid() {
		return cid;
	}

	public void setCid(Long cid) {
		this.cid = cid;
	}

	public static void add(User user, Long numIid, String title,
			int userVersion, String picPath, double origPrice, double price,
			Long cid) {
		//Promoted item = Promoted.find("userId = ? and numIid = ?",
		//		user.getId(), numIid).first();
		Promoted item = Promoted.findByNumIid(user.getId(), numIid);
		if (item == null) {
			new Promoted(user.getId(), numIid, title, userVersion, picPath,
					origPrice, price, cid).jdbcSave();
		}
	}

	public static void remove(User user, long numIid) {
		dp.update(true, " delete from " + Promoted.TABLE_NAME
				+ " where numIid = ? and userId = ? ", numIid, user.getId());
	}

	public static void removeAll(User user) {
		//Promoted.delete("userId = ?", user.getId());
		dp.update(true, " delete from " + Promoted.TABLE_NAME
				+ " where userId = ? ", user.getId());
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean rawInsert() {
		long id = dp
				.insert("insert into `"
						+ TABLE_NAME
						+ "`(`userId`,`numIid`,`title`,`picPath`,`price`,`origPrice`,`cid`,`userVersion`) values(?,?,?,?,?,?,?,?)",
						this.userId, this.numIid, this.title, this.picPath,
						this.price, this.origPrice, this.cid, this.userVersion);

		if (id > 0L) {
			return true;
		} else {
			log.error("Insert Fails....." + "[userId : ]" + this.userId
					+ "[numIid : ]" + this.numIid);
			return false;
		}

	}

	public boolean rawUpdate() {
		long updateNum = dp
				.insert("update `"
						+ TABLE_NAME
						+ "` set  `userId` = ?, `numIid` = ?, `title` = ?, `picPath` = ?, `price` = ?, `origPrice` = ? , `cid` = ?, `userVersion` = ? where `id` = ? ",
						this.userId, this.numIid, this.title, this.picPath,
						this.price, this.origPrice, this.cid, this.userVersion,
						this.id);

		if (updateNum == 1) {
			return true;
		} else {
			log.error("update failed...for :" + this.id + "[userId : ]"
					+ this.userId + "[numIid : ]" + this.numIid);

			return false;
		}
	}
}
