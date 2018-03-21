package models.CPEctocyst;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import models.associate.AssociatedItems;
import models.item.ItemCatPlay;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.pojo.PageOffset;

import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;
import play.db.jpa.GenericModel;
import transaction.JDBCBuilder;
import transaction.DBBuilder.DataSrc;

@Entity(name = SellerToStaff.TABLE_NAME)
@JsonIgnoreProperties(value = {})
public class SellerToStaff  extends GenericModel implements PolicySQLGenerator{
	
	@Transient
    public static final String TABLE_NAME = "seller_to_staff";

    @Transient
    public static final Logger log = LoggerFactory.getLogger(SellerToStaff.class);

    @Transient
    public static SellerToStaff EMPTY = new SellerToStaff();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    @Id
    @PolicySQLGenerator.CodeNoUpdate
    public Long userId;
    
    public String userNick;
    
    /**
     *  订购了好评助手的卖家， 如果想要处理差评，会选择一家差评外包商 CPStaff
     *  CPStaff类 role字段表示  是外包商的老板（SUPERADMIN） 还是 客服(SWEETY)
     *  好评助手的卖家，先选择对应的外包商，这时候指定的是 chiefId（chiefId对应的是role为SUPERADMIN的某个CPStaff的ID）
     *  即 new SellerToStaff(userId, -1, chiefId);
     *  这时候，在chiefId的后台管理页面，可以看到该卖家
     *  chiefId手动将该卖家分配给某个role为SWEETY的CPStaff, 即客服
     *  即sellerToStaff.setStaffId(subStaffId)
     *  这样，在客服的卖家管理页面上，就会看到该卖家
     */
    @Index(name="staffId")
    public Long subStaffId;
    
    @Index(name="subStaffName")
    public String subStaffName;
    
    @Index(name="chiefId")
    public Long chiefId;

    @Index(name="chiefName")
    public String chiefName;
    
    public Long created;
    
    public Long updated;
    
    public SellerToStaff() {
    	super();
    }
    
	public SellerToStaff(Long userId, String userNick, Long subStaffId, String subStaffName, Long chiefId, String chiefName) {
		super();
		this.userId = userId;
		this.userNick = userNick;
		this.subStaffId = subStaffId;
		this.chiefId = chiefId;
		this.subStaffName = subStaffName;
		this.chiefName = chiefName;
	}

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

	public Long getSubStaffId() {
		return subStaffId;
	}

	public void setSubStaffId(Long subStaffId) {
		this.subStaffId = subStaffId;
	}

	public Long getChiefId() {
		return chiefId;
	}

	public void setChiefId(Long chiefId) {
		this.chiefId = chiefId;
	}
    
	public String getSubStaffName() {
		return subStaffName;
	}

	public void setSubStaffName(String subStaffName) {
		this.subStaffName = subStaffName;
	}

	public String getChiefName() {
		return chiefName;
	}

	public void setChiefName(String chiefName) {
		this.chiefName = chiefName;
	}

	public Long getCreated() {
		return created;
	}

	public void setCreated(Long created) {
		this.created = created;
	}

	public Long getUpdated() {
		return updated;
	}

	public void setUpdated(Long updated) {
		this.updated = updated;
	}

	@Override
    @JsonIgnore
    public String getTableHashKey() {
        return null;
    }
	
	@Override
    @JsonIgnore
    public String getTableName() {
        return this.TABLE_NAME;
    }

    @Override
    @JsonIgnore
    public String getIdColumn() {
        return "userId";
    }

    @Override
    public void setId(Long userId) {
        this.userId = userId;
    }

    @Override
    @JsonIgnore
    public String getIdName() {
        return "userId";
    }

    @Override
    public Long getId() {
        return userId;
    }

    @Transient
    static String EXIST_ID_QUERY = "select userId from " + SellerToStaff.TABLE_NAME + " where userId = ? ";

    public static Long findExistId(Long userId) {
        return dp.singleLongQuery(EXIST_ID_QUERY, userId);
    }

    @Override
    public boolean jdbcSave() {

        try {
            Long existdId = findExistId(this.userId);

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

    @Transient
    static String insertSQL = "insert into `seller_to_staff` (`userId`,`userNick`,`subStaffId`,`subStaffName`,`chiefId`,`chiefName`,`created`,`updated`) values(?,?,?,?,?,?,?,?)";

    public boolean rawInsert() {
    	Long now = System.currentTimeMillis();
        Long id = dp.insert(false, insertSQL, this.userId, this.userNick, this.subStaffId, this.subStaffName, this.chiefId, this.chiefName, now, now);

        if (id > 0L) {
            setId(id);
            return true;
        } else {
            log.error("Insert Fails.....");
            return false;
        }

    }

    @Transient
    static String updateSQL = "update `seller_to_staff` set  `userNick` = ?, `subStaffId` = ?, `chiefId` = ?, `subStaffName` = ?, `chiefName` = ?, `updated` = ? where `userId` = ? ";

    public boolean rawUpdate() {
    	Long now = System.currentTimeMillis();
        Long updateNum = dp.insert(false, updateSQL, this.userNick, this.subStaffId, this.chiefId, this.subStaffName, this.chiefName, now, this.userId);

        if (updateNum == 1) {
            log.info("update ok for :" + this.getId());
            return true;
        } else {
            log.error("update failed...for :" + this.getId());
            return false;
        }
    }
    
    private static final String SelectAllProperties = " userId,subStaffId,chiefId,subStaffName,chiefName,created,updated,userNick ";
    public static SellerToStaff findByUserId(Long userId) {

        String query = "select " + SelectAllProperties + " from " + TABLE_NAME
                + " where userId = ? ";

        return new JDBCBuilder.JDBCExecutor<SellerToStaff>(dp, query, userId) {

            @Override
            public SellerToStaff doWithResultSet(ResultSet rs)
                    throws SQLException {

                if (rs.next()) {

                    return parseSellerToStaff(rs);

                } else {
                    return null;
                }

            }

        }.call();
    }
    
    public static List<SellerToStaff> findByChiefId(PageOffset po, Long chiefId, String searchText, String staffName) {

        String query = "select " + SelectAllProperties + " from " + TABLE_NAME
                + " where chiefId = ?";
        if(!StringUtils.isEmpty(searchText)) {
        	query += " and userNick = '" + searchText + "'";
        }
        if(!StringUtils.isEmpty(staffName)) {
        	query += " and subStaffName = '" + staffName + "'";
        }
        query += " order by updated desc limit ? offset ?";

        return new JDBCBuilder.JDBCExecutor<List<SellerToStaff>>(dp, query, chiefId, po.getPs(), po.getOffset()) {

            @Override
            public List<SellerToStaff> doWithResultSet(ResultSet rs) throws SQLException {
                List<SellerToStaff> catList = new ArrayList<SellerToStaff>();
                while (rs.next()) {
                	SellerToStaff catPlay = parseSellerToStaff(rs);
                    if (catPlay != null) {
                        catList.add(catPlay);
                    }
                }

                return catList;
            }

        }.call();
    }
    
	public static SellerToStaff findByChiefNameAndUserNick(String chiefName, String userNick) {
		if(StringUtils.isEmpty(chiefName)) {
			return null;
		}

		String query = "select " + SelectAllProperties + " from " + TABLE_NAME
				+ " where chiefName = ? and userNick = ?";

		return new JDBCBuilder.JDBCExecutor<SellerToStaff>(dp, query, chiefName, userNick) {

			@Override
			public SellerToStaff doWithResultSet(ResultSet rs)
					throws SQLException {
				if (rs.next()) {
					return parseSellerToStaff(rs);
				} else {
					return null;
				}
			}

		}.call();
	}
	
	public static SellerToStaff findByChiefIdAndSubStaffName(Long chiefId, String subStaffName) {
		if(chiefId <= 0) {
			return null;
		}
		
		if(StringUtils.isEmpty(subStaffName)) {
			return null;
		}
		
		String query = "select " + SelectAllProperties + " from " + TABLE_NAME
				+ " where chiefId = ? and subStaffName = ?";

		return new JDBCBuilder.JDBCExecutor<SellerToStaff>(dp, query, chiefId, subStaffName) {

			@Override
			public SellerToStaff doWithResultSet(ResultSet rs)
					throws SQLException {
				if (rs.next()) {
					return parseSellerToStaff(rs);
				} else {
					return null;
				}
			}

		}.call();
	}
    
    public static long countByChiefId(Long chiefId, String searchText, String staffName) {
        String query = "select count(*) from " + TABLE_NAME + " where chiefId = ? ";
        if(!StringUtils.isEmpty(searchText)) {
        	query += " and userNick = '" + searchText + "'";
        }
        if(!StringUtils.isEmpty(staffName)) {
        	query += " and subStaffName = '" + staffName + "'";
        }
        return dp.singleLongQuery(query, chiefId);
    }
    
    public static long countByChiefName(String chiefName, String searchText, String staffName) {
        String query = "select count(*) from " + TABLE_NAME + " where chiefName = ? ";
        if(!StringUtils.isEmpty(searchText)) {
        	query += " and userNick = '" + searchText + "'";
        }
        if(!StringUtils.isEmpty(staffName)) {
        	query += " and subStaffName = '" + staffName + "'";
        }
        return dp.singleLongQuery(query, chiefName);
    }
    
    public static long countBySubStaffId(Long subStaffId, String searchText) {
        String query = "select count(*) from " + TABLE_NAME + " where subStaffId = ? ";
        if(!StringUtils.isEmpty(searchText)) {
        	query += " and userNick = '" + searchText + "'";
        }
        return dp.singleLongQuery(query, subStaffId);
    }
    
    public static List<SellerToStaff> findBySubStaffId(PageOffset po, Long subStaffId, String searchText) {

        String query = "select " + SelectAllProperties + " from " + TABLE_NAME
                + " where subStaffId = ?";
        if(!StringUtils.isEmpty(searchText)) {
        	query += " and userNick = '" + searchText + "'";
        }
        query += " order by updated desc limit ? offset ?";

        return new JDBCBuilder.JDBCExecutor<List<SellerToStaff>>(dp, query, subStaffId, po.getPs(), po.getOffset()) {

            @Override
            public List<SellerToStaff> doWithResultSet(ResultSet rs) throws SQLException {
                List<SellerToStaff> catList = new ArrayList<SellerToStaff>();
                while (rs.next()) {
                	SellerToStaff catPlay = parseSellerToStaff(rs);
                    if (catPlay != null) {
                        catList.add(catPlay);
                    }
                }

                return catList;
            }

        }.call();
    }
    
    public static List<SellerToStaff> findBySubStaffId(Long subStaffId) {

        String query = "select " + SelectAllProperties + " from " + TABLE_NAME
                + " where subStaffId = ?";

        return new JDBCBuilder.JDBCExecutor<List<SellerToStaff>>(dp, query, subStaffId) {

            @Override
            public List<SellerToStaff> doWithResultSet(ResultSet rs) throws SQLException {
                List<SellerToStaff> catList = new ArrayList<SellerToStaff>();
                while (rs.next()) {
                	SellerToStaff catPlay = parseSellerToStaff(rs);
                    if (catPlay != null) {
                        catList.add(catPlay);
                    }
                }

                return catList;
            }

        }.call();
    }
    
    private static SellerToStaff parseSellerToStaff(ResultSet rs) {
        try {
        	SellerToStaff listCfg = new SellerToStaff();
            listCfg.setUserId(rs.getLong(1));
            listCfg.setSubStaffId(rs.getLong(2));
            listCfg.setChiefId(rs.getLong(3));
            listCfg.setSubStaffName(rs.getString(4));
            listCfg.setChiefName(rs.getString(5));
            listCfg.setCreated(rs.getLong(6));
            listCfg.setUpdated(rs.getLong(7));
            listCfg.setUserNick(rs.getString(8));
            return listCfg;

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }
    
    public boolean rawDelete(Long userId) {
        
        String deleteSql = "delete from " + TABLE_NAME + " where userId = ?";
        
        long deleteNum = dp.update(deleteSql, userId);
        
        if (deleteNum > 0) {
            return true;
        } else {
            return false;
        }
    }
}
