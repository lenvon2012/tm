package models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import models.user.User;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import play.jobs.Job;
import result.TMResult;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import bustbapi.MBPApi;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.CommonUtils;
import com.taobao.api.domain.QueryRow;

import dao.UserDao;

@Entity(name = CloudDataRegion.TABLE_NAME)
public class CloudDataRegion extends GenericModel implements PolicySQLGenerator {
    
    @Transient
    private static final Logger log = LoggerFactory.getLogger(CloudDataRegion.class);
    
    @Transient
    public static final String TABLE_NAME = "cloud_data_region";
    
    @Transient
    public static CloudDataRegion EMPTY = new CloudDataRegion();
    
    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    public Long region_level;
	
    @Id
    @Index(name="regionId")
    public Long region_id;
    
    public String region_name;
    
    public Long country_id;
    
    public String country_name;
    
    public Long province_id;
    
    public String province_name;
    
    public Long city_id;
    
    public String city_name;
    
    // 维度， 国家标识 4 中国 0 未知 -4 海外
    public Long country_tag;
    
    public CloudDataRegion() {
        super();
    }

	public CloudDataRegion(Long region_level, Long region_id,
			String region_name, Long country_id, String country_name,
			Long province_id, String province_name, Long city_id,
			String city_name, Long country_tag) {
		super();
		this.region_level = region_level;
		this.region_id = region_id;
		this.region_name = region_name;
		this.country_id = country_id;
		this.country_name = country_name;
		this.province_id = province_id;
		this.province_name = province_name;
		this.city_id = city_id;
		this.city_name = city_name;
		this.country_tag = country_tag;
	}

	public CloudDataRegion(List<String> values) {
		super();
		if(!CommonUtils.isEmpty(values)) {
			this.region_level = Long.valueOf(values.get(0));
			this.region_id = Long.valueOf(values.get(1));
			this.region_name = values.get(2);
			this.country_id = Long.valueOf(values.get(3));
			this.country_name = values.get(4);
			this.province_id = Long.valueOf(values.get(5));
			this.province_name = values.get(6);
			this.city_id = Long.valueOf(values.get(7));
			this.city_name = values.get(8);
			this.country_tag = Long.valueOf(values.get(9));
		}
	}
	
	public Long getRegion_level() {
		return region_level;
	}

	public void setRegion_level(Long region_level) {
		this.region_level = region_level;
	}

	public Long getRegion_id() {
		return region_id;
	}

	public void setRegion_id(Long region_id) {
		this.region_id = region_id;
	}

	public String getRegion_name() {
		return region_name;
	}

	public void setRegion_name(String region_name) {
		this.region_name = region_name;
	}

	public Long getCountry_id() {
		return country_id;
	}

	public void setCountry_id(Long country_id) {
		this.country_id = country_id;
	}

	public String getCountry_name() {
		return country_name;
	}

	public void setCountry_name(String country_name) {
		this.country_name = country_name;
	}

	public Long getProvince_id() {
		return province_id;
	}

	public void setProvince_id(Long province_id) {
		this.province_id = province_id;
	}

	public String getProvince_name() {
		return province_name;
	}

	public void setProvince_name(String province_name) {
		this.province_name = province_name;
	}

	public Long getCity_id() {
		return city_id;
	}

	public void setCity_id(Long city_id) {
		this.city_id = city_id;
	}

	public String getCity_name() {
		return city_name;
	}

	public void setCity_name(String city_name) {
		this.city_name = city_name;
	}

	public Long getCountry_tag() {
		return country_tag;
	}

	public void setCountry_tag(Long country_tag) {
		this.country_tag = country_tag;
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
        return "region_id";
    }

    @Override
    public Long getId() {
        return region_id;
    }

    @Override
    public void setId(Long region_id) {
        this.region_id = region_id;
    }
    
    public static long findExistId(Long id) {

        String query = "select region_id from " + TABLE_NAME + " where region_id = ? ";

        return dp.singleLongQuery(query, id);
    }


    @Override
    public boolean jdbcSave() {
        try {
            
            long existId = findExistId(this.region_id);
            
            if (existId <= 0) {
                return this.rawInsert();
            } else {
                return this.rawUpdate();
            }
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return false;
        }
    }

    @Override
    public String getIdName() {
        return "id";
    }
    
    public boolean rawInsert() {
        try {
            String insertSQL = "insert into `"
                    + TABLE_NAME
                    + "`(`region_id`,`region_level`,`region_name`,`country_id`,`country_name`," +
                    "`province_id`,`province_name`,`city_id`,`city_name`,`country_tag`) values(?,?,?,?,?,?,?,?,?,?)";
            
            long id = dp.insert(insertSQL, this.region_id, this.region_level, this.region_name,
            		this.country_id, this.country_name, this.province_id, this.province_name, this.city_id, this.city_name,
            		this.country_tag);
            
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
    
    
    public boolean rawUpdate() {

        String updateSQL = "update `"
                + TABLE_NAME
                + "` set `region_level` = ?, `region_name` = ?, `country_id` = ?, " +
                "`country_name` = ?, `province_id` = ?, `province_name` = ?, `city_id` = ?, `city_name` = ?," +
                "`country_tag` = ? where `region_id` = ?  ";

        
        long updateNum = dp.insert(updateSQL, this.region_level, this.region_name, this.country_id,
        		this.country_name, this.province_id, this.province_name, this.city_id, this.city_name,
        		this.country_tag, this.region_id);

        if (updateNum == 1) {

            return true;
        } else {

            return false;
        }
    }
    
    
    public static CloudDataRegion findByRegionId(Long regionId) {
    	if(regionId == null) {
    		return null;
    	}
        String query = "select " + SelectAllProperties + " from " + TABLE_NAME
                + " where region_id = ? ";

        return new JDBCBuilder.JDBCExecutor<CloudDataRegion>(dp, query, regionId) {
            @Override
            public CloudDataRegion doWithResultSet(ResultSet rs)
                    throws SQLException {
                if (rs.next()) {
                    return parseCloudDataRegion(rs);
                } else {
                    return null;
                }
            }
        }.call();
    }    
    
    private static final String SelectAllProperties = " region_level, region_id, region_name,  " +
    		"country_id, country_name, province_id, province_name, city_id, city_name, country_tag ";

    private static CloudDataRegion parseCloudDataRegion(ResultSet rs) {
        try {
        	CloudDataRegion result = new CloudDataRegion();
        	result.setRegion_level(rs.getLong(1));
        	result.setRegion_id(rs.getLong(2));
        	result.setRegion_name(rs.getString(3));
        	result.setCountry_id(rs.getLong(4));
        	result.setCountry_name(rs.getString(5));
        	result.setProvince_id(rs.getLong(6));
        	result.setProvince_name(rs.getString(7));
        	result.setCity_id(rs.getLong(8));
        	result.setCity_name(rs.getString(9));
        	result.setCountry_tag(rs.getLong(10));
            return result;

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    } 
    
    public static class CloudDataRegionUpdateJob extends Job {
        @Override
        public void doJob() {
        	User user = UserDao.findByUserNick("clorest510");
        	dp.update("delete from " + TABLE_NAME);
        	TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(2961L, "thedate=20140111", user.getSessionKey())
    		.call();
        	List<QueryRow> rows = res.getRes();
        	if(CommonUtils.isEmpty(rows)) {
        		return;
        	}
        	for(QueryRow row : rows) {
        		List<String> values = row.getValues();
        		if(CommonUtils.isEmpty(values)) {
        			continue;
        		}
        		log.info(values.toString());
        		new CloudDataRegion(values).jdbcSave();
        				
        	}
        }
    }
}

