package models.autolist;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

/**
 * 记录上一次job的时间
 * @author Administrator
 *
 */
@Entity(name=AutoListJobTs.TABLE_NAME)
public class AutoListJobTs extends Model implements PolicySQLGenerator {
	@Transient
    private static final Logger log = LoggerFactory.getLogger(AutoListJobTs.class);

    @Transient
    public static final String TABLE_NAME = "auto_list_job_ts"; 
    
    @Transient
    public static AutoListJobTs EMPTY = new AutoListJobTs();
    
    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    
    private String jobId;

    private long timestamp;

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
    
	public AutoListJobTs(String jobId, long timestamp) {
		super();
		this.jobId = jobId;
		this.timestamp = timestamp;
	}
	
	public AutoListJobTs() {
		super();
	}
	
    public static AutoListJobTs createAutoListJobTs(String jobId, long timestamp) {
    	AutoListJobTs jobTs = new AutoListJobTs();
    	jobTs.jobId = jobId;
    	jobTs.timestamp = timestamp;
    	
    	return jobTs;
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

    static String EXIST_ID_QUERY = "select id from " + TABLE_NAME + " where  jobId = ? ";

    public static long findExistId(String jobId) {
        return dp.singleLongQuery(EXIST_ID_QUERY, jobId);
    }

    @Override
    public boolean jdbcSave() {
        try {
            long existdId = findExistId(this.jobId);

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
        long id = dp.insert("insert into `auto_list_job_ts`(`jobId`,`timestamp`) values(?,?)",
                this.jobId, this.timestamp);

        if (id > 0L) {
            log.info("insert ts for the first time !" + timestamp);
            return true;
        } else {
            log.error("Insert Fails....." + "[Id : ]" + this.id);

            return false;
        }
    }

    public boolean rawUpdate() {
        long updateNum = dp.insert(
                "update `auto_list_job_ts` set  `jobId` = ?, `timestamp` = ? where `id` = ? ", this.jobId,
                this.timestamp, this.getId());

        if (updateNum > 0L) {
            log.info("update ts success! " + timestamp);
            return true;
        } else {
            log.error("update Fails....." + "[Id : ]" + this.id);

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

    public static AutoListJobTs findByJobId(String jobId) {

        String query = "select jobId, timestamp from " + AutoListJobTs.TABLE_NAME
                + " where jobId=? order by id desc";

        return new JDBCBuilder.JDBCExecutor<AutoListJobTs>(dp, query, jobId) {

            @Override
            public AutoListJobTs doWithResultSet(ResultSet rs)
                    throws SQLException {

                if (rs.next()) {
                	return new AutoListJobTs(rs.getString(1), rs.getLong(2));
                } else {
                    return null;
                }
            }

        }.call();
    }
}
