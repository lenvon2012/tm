package models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = FuwuScoreResult.TABLE_NAME)
public class FuwuScoreResult extends GenericModel implements PolicySQLGenerator {
    
    @Transient
    private static final Logger log = LoggerFactory.getLogger(FuwuScoreResult.class);
    
    @Transient
    public static final String TABLE_NAME = "fuwu_score_result";
    
    @Transient
    public static FuwuScoreResult EMPTY = new FuwuScoreResult();
    
    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    // 评价id
    @Id
    private Long id;
    
    // 平均分
    private String avg_score;
    
    // 评论neir
    private String suggestion;
    
    // 服务code
    private String service_code;
    
    // 评价人用户昵称
    private String user_nick;
    
    // 评价时间
    private Date gmt_create;
    
    // 是否为有效评分  1表示有效，2表示无效
    private int is_valid;
    
    // 服务规格code
    private String item_code;
    
    // 服务规格名称
    private String item_name;
    
    // 是否实际付费  1表示实际付费，2表示实际未付费
    private int is_pay;

	// 服务态度评分
    private String attitude_score;
    
    // 稳定性评分
    private String stability_score;
    
    // 易用性评分
    private String easyuse_score;
    
    // 专业性评分
    private String prof_score;
    
    // 交片速度
    private String rapid_score;
    
    // 描述相符
    private String matched_score;

    public String getAvg_score() {
		return avg_score;
	}

	public void setAvg_score(String avg_score) {
		this.avg_score = avg_score;
	}

	public String getSuggestion() {
		return suggestion;
	}

	public void setSuggestion(String suggestion) {
		this.suggestion = suggestion;
	}

	public String getService_code() {
		return service_code;
	}

	public void setService_code(String service_code) {
		this.service_code = service_code;
	}

	public String getUser_nick() {
		return user_nick;
	}

	public void setUser_nick(String user_nick) {
		this.user_nick = user_nick;
	}

	public Date getGmt_create() {
		return gmt_create;
	}

	public void setGmt_create(Date gmt_create) {
		this.gmt_create = gmt_create;
	}

	public int getIs_valid() {
		return is_valid;
	}

	public void setIs_valid(int is_valid) {
		this.is_valid = is_valid;
	}

	public String getItem_code() {
		return item_code;
	}

	public void setItem_code(String item_code) {
		this.item_code = item_code;
	}

	public String getItem_name() {
		return item_name;
	}

	public void setItem_name(String item_name) {
		this.item_name = item_name;
	}

	public int getIs_pay() {
		return is_pay;
	}

	public void setIs_pay(int is_pay) {
		this.is_pay = is_pay;
	}

	public String getAttitude_score() {
		return attitude_score;
	}

	public void setAttitude_score(String attitude_score) {
		this.attitude_score = attitude_score;
	}

	public String getStability_score() {
		return stability_score;
	}

	public void setStability_score(String stability_score) {
		this.stability_score = stability_score;
	}

	public String getEasyuse_score() {
		return easyuse_score;
	}

	public void setEasyuse_score(String easyuse_score) {
		this.easyuse_score = easyuse_score;
	}

	public String getProf_score() {
		return prof_score;
	}

	public void setProf_score(String prof_score) {
		this.prof_score = prof_score;
	}

	public String getRapid_score() {
		return rapid_score;
	}

	public void setRapid_score(String rapid_score) {
		this.rapid_score = rapid_score;
	}

	public String getMatched_score() {
		return matched_score;
	}

	public void setMatched_score(String matched_score) {
		this.matched_score = matched_score;
	}
	
    public FuwuScoreResult() {
        super();
    }

    public FuwuScoreResult(Long id, String avg_score, String suggestion,
			String service_code, String user_nick, Date gmt_create,
			int is_valid, String item_code, String item_name, int is_pay,
			String attitude_score, String stability_score,
			String easyuse_score, String prof_score, String rapid_score,
			String matched_score) {
		super();
		this.id = id;
		this.avg_score = avg_score;
		this.suggestion = suggestion;
		this.service_code = service_code;
		this.user_nick = user_nick;
		this.gmt_create = gmt_create;
		this.is_valid = is_valid;
		this.item_code = item_code;
		this.item_name = item_name;
		this.is_pay = is_pay;
		this.attitude_score = attitude_score;
		this.stability_score = stability_score;
		this.easyuse_score = easyuse_score;
		this.prof_score = prof_score;
		this.rapid_score = rapid_score;
		this.matched_score = matched_score;
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
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }
    
    public static long findExistId(Long id) {

        String query = "select id from " + TABLE_NAME + " where id = ? ";

        return dp.singleLongQuery(query, id);
    }


    @Override
    public boolean jdbcSave() {
        try {
            
            long existId = findExistId(this.id);
            
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
                    + "`(`id`,`avg_score`,`suggestion`,`service_code`,`user_nick`,`gmt_create`," +
                    "`is_valid`,`item_code`,`item_name`,`is_pay`,`attitude_score`," +
                    "`stability_score`,`easyuse_score`,`prof_score`,`rapid_score`,`matched_score`) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            
            long id = dp.insert(insertSQL, this.id, this.avg_score, this.suggestion, this.service_code,
            		this.user_nick, this.gmt_create, this.is_valid, this.item_code, this.item_name,
            		this.is_pay, this.attitude_score, this.stability_score, this.easyuse_score, 
            		this.prof_score, this.rapid_score, this.matched_score);
            
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
                + "` set `avg_score` = ?, `suggestion` = ?, `service_code` = ?, " +
                "`user_nick` = ?, `gmt_create` = ?, `is_valid` = ?, `item_code` = ?, `item_name` = ?," +
                "`is_pay` = ?, `attitude_score` = ?, `stability_score` = ?, `easyuse_score` = ?, `prof_score` = ?," +
                "`rapid_score` = ?, `matched_score` = ?" +
                " where `id` = ?  ";

        
        long updateNum = dp.insert(updateSQL, this.avg_score, this.suggestion, this.service_code,
        		this.user_nick, this.gmt_create, this.is_valid, this.item_code, this.item_name,
        		this.is_pay, this.attitude_score, this.stability_score, this.easyuse_score, this.prof_score,
        		this.rapid_score, this.matched_score,
                this.id);

        if (updateNum == 1) {

            return true;
        } else {

            return false;
        }
    }
    
    
    public static FuwuScoreResult findByUserId(Long userId) {

        String query = "select " + SelectAllProperties + " from " + TABLE_NAME
                + " where userId = ? ";

        return new JDBCBuilder.JDBCExecutor<FuwuScoreResult>(dp, query, userId) {
            @Override
            public FuwuScoreResult doWithResultSet(ResultSet rs)
                    throws SQLException {
                if (rs.next()) {
                    return parseFuwuScoreResult(rs);
                } else {
                    return null;
                }
            }
        }.call();
    }    
    
    private static final String SelectAllProperties = " id,avg_score,suggestion,service_code,user_nick,gmt_create," +
    		"is_valid,item_code,item_name,is_pay,attitude_score,stability_score,easyuse_score,prof_score,rapid_score," +
    		"matched_score ";

    private static FuwuScoreResult parseFuwuScoreResult(ResultSet rs) {
        try {
        	FuwuScoreResult result = new FuwuScoreResult();
        	result.setId(rs.getLong(1));
        	result.setAvg_score(rs.getString(2));
        	result.setSuggestion(rs.getString(3));
        	result.setService_code(rs.getString(4));
        	result.setUser_nick(rs.getString(5));
        	result.setGmt_create(rs.getDate(6));
        	result.setIs_valid(rs.getInt(7));
        	result.setItem_code(rs.getString(8));
        	result.setItem_name(rs.getString(9));
        	result.setIs_pay(rs.getInt(10));
        	result.setAttitude_score(rs.getString(11));
        	result.setStability_score(rs.getString(12));
        	result.setEasyuse_score(rs.getString(13));
        	result.setProf_score(rs.getString(14));
        	result.setRapid_score(rs.getString(15));
        	result.setMatched_score(rs.getString(16));
            return result;

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    } 
    
}
