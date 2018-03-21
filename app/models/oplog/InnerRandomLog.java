package models.oplog;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = InnerRandomLog.TABLE_NAME)
public class InnerRandomLog extends Model implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(InnerRandomLog.class);

    public static final String TABLE_NAME = "inner_random_log";

    public static final InnerRandomLog EMPTY = new InnerRandomLog();

    public static final DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    @Index(name = "userId")
    private Long userId;
    
    @Index(name = "numIid")
    private Long numIid;
    
    private long createTs;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getNumIid() {
        return numIid;
    }

    public void setNumIid(Long numIid) {
        this.numIid = numIid;
    }

    public long getCreateTs() {
        return createTs;
    }

    public void setCreateTs(long createTs) {
        this.createTs = createTs;
    }

    public InnerRandomLog() {
        super();
    }

    public InnerRandomLog(Long userId, Long numIid, long createTs) {
		super();
		this.userId = userId;
		this.numIid = numIid;
		this.createTs = createTs;
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
    public String getIdName() {
        return "id";
    }
    
    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public static long findExistId(Long id) {
        if(id == null || id <= 0L) {
        	return -1;
        }
        String query = "select id from " + TABLE_NAME + " where id = ?  ";
        
        
        return dp.singleLongQuery(query, id);
        
    }
    
    @Override
    public boolean jdbcSave() {
    	return this.rawInsert();
    }

    
    public boolean rawInsert() {
        
        String insertSql = "insert into `" + TABLE_NAME + "`(`userId`,`numIid`,"
                + "`createTs`) "
                + "values(?,?,?)";

        long id = dp.insert(true, insertSql, this.userId, this.numIid, 
                this.createTs);

        // log.info("[Insert Item Id:]" + id + "[userId : ]" + this.userId);

        if (id >= 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.userId);
            return false;
        }

    }

}
