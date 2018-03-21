package models.words;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import play.db.jpa.Model;
import transaction.CodeGenerator.DBDispatcher;
import transaction.CodeGenerator.PolicySQLGenerator;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;


@Entity(name = ItemDetail.TABLE_NAME)
@JsonIgnoreProperties(value = { "entityId", "tableHashKey", "persistent",
		"tableName", "idName", "idColumn", "id" })
public class ItemDetail extends Model implements PolicySQLGenerator<Long>, Serializable {

	private static final long serialVersionUID = -1L;
	
	@Transient
	private static final Logger log = LoggerFactory
			.getLogger(ItemDetail.class);

	@Transient
	public static final String TABLE_NAME = "item_detail";

	@Transient
	public static ItemDetail EMPTY = new ItemDetail();

	@Transient
	public static DBDispatcher dp = new DBDispatcher(DataSrc.QUOTA, EMPTY);

	@Transient
	private static int viewCount = 0;

	/**
	 * @Description: 宝贝当前在线浏览人数有效期 4m
	 */
	@Transient
	public static final String CAHCHE_ITEMDETAIL_VIEWCOUNT_PREFIX = "ITMDT_VC";
	@Transient
	public static final String DEFAULT_CAHCHE_ITEMDETAIL_VIEWCOUNT_LIFETIME = "4mn";

	@Index(name = "numIid")
	public Long numIid;

	/*
	 * 宝贝标题
	 */
	@Column(columnDefinition = "varchar(63) default '' ")
	public String title;
		
	/*
	 * 卖家ID
	 */
	@Index(name = "userId")
	public Long userId;
	
	/*
	 * 卖家旺旺
	 */
	@Index(name = "nick")
	@Column(columnDefinition = "varchar(63) default '' ")
	public String nick;
	
	/*
	 * 发货地址
	 */
	@Column(columnDefinition = "varchar(31) default '' ")
	public String location;

	/*
	 * 主图链接
	 */
	@Column(columnDefinition = "varchar(127) default '' ")
	public String picPath;
	
	/*
	 * 宝贝实际价格
	 */
	@Column(columnDefinition = "varchar(15) default '' ")
	public String price;

	/*
	 * 宝贝所属类目
	 */
	public Long category;

	public Long createTs;
	
	public Long updateTs;
	
	public ItemDetail() {
		super();
	}

	public ItemDetail(Long numIid, String title, Long userId, String nick,
			String location, String picPath, String price, Long category, Long createTs, Long updateTs) {
		super();
		this.numIid = numIid;
		this.title = title;
		this.userId = userId;
		this.nick = nick;
		this.location = location;
		this.picPath = picPath;
		this.price = price;
		this.category = category;
		this.createTs = createTs;
		this.updateTs = updateTs;
	}



	public Long getNumIid() {
		return numIid;
	}

	public void setNumIid(Long numIid) {
		this.numIid = numIid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

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

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getPicPath() {
		return picPath;
	}

	public void setPicPath(String picPath) {
		this.picPath = picPath;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public Long getCategory() {
		return category;
	}

	public void setCategory(Long category) {
		this.category = category;
	}

	public Long getCreateTs() {
		return createTs;
	}

	public void setCreateTs(Long createTs) {
		this.createTs = createTs;
	}

	public Long getUpdateTs() {
		return updateTs;
	}

	public void setUpdateTs(Long updateTs) {
		this.updateTs = updateTs;
	}

	public static int getViewCount() {
		return viewCount;
	}

	public static void setViewCount(int viewCount) {
		ItemDetail.viewCount = viewCount;
	}

	@Override
	public String getTableName() {
		return TABLE_NAME;
	}

	@Override
	public String getIdColumn() {
		return "id";
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public static long findExistId(Long numIid) {

		String query = "select id from " + TABLE_NAME
				+ " where numIid = ?";

		return dp.singleLongQuery(query, numIid);
	}

	@Override
	public boolean jdbcSave() {
		try {
    		long existdId = findExistId(this.numIid);

            if (existdId <= 0L) {
                return this.rawInsert();
            } else {
            	this.setId(existdId);
                return this.rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }
        
	}
	
	public static void createOrUpdateItemDetail(Long numIid, String title, Long userId, String nick,
			String location, String picPath, String price, Long category) {
		if(numIid == null || numIid <= 0L) {
			return;
		}
		if(StringUtils.isEmpty(picPath) == false && picPath.length() > 125) {
			picPath = picPath.substring(0,125);
		}
		if(StringUtils.isEmpty(title) == false && title.length() > 60) {
			title = title.substring(0,60);
		}
		ItemDetail detail = ItemDetail.findByNumIid(numIid);
		if(detail == null) {
			Long ts = System.currentTimeMillis();
			new ItemDetail(numIid, title, userId, nick, location, picPath, price, category, ts, ts).rawInsert();
		} else {
			boolean changed = false;
			if (!(StringUtils.isEmpty(price) || price.equals(detail.getPrice()))) {//价格不为空且前后不一致时跟新
				changed = true;
				detail.setPrice(price);
			}
			if (!picPath.equals(detail.getPicPath())) {
				changed = true;
				detail.setPicPath(picPath);;
			}
			if (!title.equals(detail.getTitle())) {
				changed = true;
				detail.setTitle(title);
			}
			if (!(category == null || category.equals(detail.getCategory()))) {
				changed = true;
				detail.setCategory(category);
			}
			if (changed) {
				detail.rawUpdate();
			}
//			if((System.currentTimeMillis() - detail.getUpdateTs()) > DateUtil.WEEK_MILLIS) {
//				detail.rawUpdate();
//			}
		}
	}
	
	public static void createOrUpdateItemDetail(ItemDetail oldDetail) {
		if(oldDetail == null) {
			return;
		}
		Long numIid = oldDetail.getNumIid();
		ItemDetail detail = ItemDetail.findByNumIid(numIid);
		if(detail == null) {
			Long ts = System.currentTimeMillis();
			new ItemDetail(numIid, oldDetail.getTitle(), oldDetail.getUserId(), oldDetail.getNick(), oldDetail.getLocation(),
					oldDetail.getPicPath(), oldDetail.getPrice(), oldDetail.getCategory(), ts, ts).jdbcSave();
		} else {
			if((oldDetail.getTitle().equals(detail.getTitle()) == false) || (oldDetail.getUserId() != detail.getUserId()) ||
			    (oldDetail.getNick().equals(detail.getNick()) == false) || (oldDetail.getLocation().equals(detail.getLocation()) == false) ||
			    (oldDetail.getPicPath().equals(detail.getLocation()) == false) || (oldDetail.getPrice().equals(detail.getPrice()) == false) ||
			    (oldDetail.getCategory() != detail.getCategory())) {
				detail.rawUpdate();
			}
		}
	}
	
	public boolean rawUpdate() {
	        String updateSQL = "update `" + TABLE_NAME + "` set " +
	                "`numIid` = ?, `title` = ?, `userId` = ?, `nick` = ?, `location` = ?, `picPath` = ?, `price` = ?, `category` = ?, " +
	                "`createTs` = ?, `updateTs` = ? " +
	                " where `id` = ? ";

	        long updateNum = dp.update(updateSQL, 
	        		this.numIid, this.title, this.userId, this.nick, this.location, this.picPath, this.price, this.category,
	        		this.createTs, System.currentTimeMillis(), 
	                this.id);

	        if (updateNum >= 1) {
	            return true;
	        } else {
	            log.error("update failed...for id : ]" + this.id + "[numIid : ]" + this.numIid);
	            return false;
	        }
	    }
	
	
	private static List<ItemDetail> findListByJDBC(String query, Object...params) {
	        
	        return new JDBCBuilder.JDBCExecutor<List<ItemDetail>>(dp, query, params) {

	            @Override
	            public List<ItemDetail> doWithResultSet(ResultSet rs)
	                    throws SQLException {
	            	List<ItemDetail> contents = new ArrayList<ItemDetail>();
	                while (rs.next()) {
	                	contents.add(parseItemDetail(rs));
	                }
	                return contents;
	            }
	        }.call();
	        
	    }

	@Override
	public String getIdName() {
		return "id";
	}

	public boolean rawInsert() {
		try {
			String insertSQL = "insert into `" + TABLE_NAME
					+ "` (`numIid`,`title`,`userId`,`nick`,`location`,`picPath`,`price`,`category`,`createTs`,`updateTs`)" + 
					" values(?,?,?,?,?,?,?,?,?,?)";

			long id = dp.insert(insertSQL, this.numIid, this.title, this.userId,
					this.nick, this.location, this.picPath, this.price, this.category, this.createTs, this.updateTs);

			if (id > 0L) {
				return true;
			} else {
				return false;
			}

		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);

			return false;
		}
	}
	
	public static List<ItemDetail> findByUserId(Long userId) {
		if (userId == null || userId <= 0L) {
			return null;
		}
		String query = "select " + SelectAllProperties + " from " + TABLE_NAME
				+ " where userId = ?";

		return new JDBCBuilder.JDBCExecutor<List<ItemDetail>>(dp,
				query, userId) {
			@Override
			public List<ItemDetail> doWithResultSet(ResultSet rs)
					throws SQLException {
				List<ItemDetail> result = new ArrayList<ItemDetail>();
				while (rs.next()) {
					result.add(parseItemDetail(rs));
				}
				return result;
			}
		}.call();
	}
	
	public static ItemDetail findByNumIid(Long numIid) {
		if (numIid == null || numIid <= 0L) {
			return null;
		}
		String query = "select " + SelectAllProperties + " from " + TABLE_NAME
				+ " where numIid = ? LIMIT 1";

		ItemDetail itemDetail =  new JDBCBuilder.JDBCExecutor<ItemDetail>(dp,
				query, numIid) {
			@Override
			public ItemDetail doWithResultSet(ResultSet rs)
					throws SQLException {
				if (rs.next()) {
					return parseItemDetail(rs);
				}
				return null;
			}
		}.call();
		return itemDetail;
	}

	private static final String SelectAllProperties = " id, numIid, title, userId,  nick ,location, picPath, price, category, createTs, updateTs ";
	private static ItemDetail parseItemDetail(ResultSet rs) {
		try {
			ItemDetail detail = new ItemDetail(
					rs.getLong(2), rs.getString(3), rs.getLong(4), rs.getString(5), rs.getString(6), 
					rs.getString(7), rs.getString(8), rs.getLong(9), rs.getLong(10), rs.getLong(11));
			detail.setId(rs.getLong(1));
			return detail;

		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return null;
		}
	}

	@Override
	public String getTableHashKey(Long t) {
		return null;
	}

}
