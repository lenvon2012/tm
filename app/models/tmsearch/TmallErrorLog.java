package models.tmsearch;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = TmallErrorLog.TABLE_NAME)
public class TmallErrorLog extends Model implements PolicySQLGenerator {
    @Transient
    private static final Logger log = LoggerFactory.getLogger(TmallErrorLog.class);

    @Transient
    public static final String TABLE_NAME = "tmall_error_log";

    @Index(name = "tmallSearchType")
    private int tmallSearchType = 0;
    private long visitedTs = 0L;
    private String errorReason;
    @Index(name = "searchedNick")
    private String searchedNick;
    

    public int getTmallSearchType() {
        return tmallSearchType;
    }

    public void setTmallSearchType(int tmallSearchType) {
        this.tmallSearchType = tmallSearchType;
    }

    public long getVisitedTs() {
        return visitedTs;
    }

    public void setVisitedTs(long visitedTs) {
        this.visitedTs = visitedTs;
    }

    public String getErrorReason() {
        return errorReason;
    }

    public void setErrorReason(String errorReason) {
        this.errorReason = errorReason;
    }

    public String getSearchedNick() {
        return searchedNick;
    }

    public void setSearchedNick(String searchedNick) {
        this.searchedNick = searchedNick;
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

    static String EXIST_ID_QUERY = "select id from " + TABLE_NAME + " where  id = ? ";

    public static long findExistId(Long id) {
        return JDBCBuilder.singleLongQuery(EXIST_ID_QUERY, id);
        //return 0L;
    }

    @Override
    public boolean jdbcSave() {
        try {
            long existdId = findExistId(this.id);

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
        long id = JDBCBuilder.insert(
                "insert into `" + TABLE_NAME + "`(`tmallSearchType`,`visitedTs`,`errorReason`,`searchedNick`) values(?,?,?,?)", this.tmallSearchType,
                this.visitedTs, this.errorReason, this.searchedNick);

        if (id > 0L) {
            log.error("Insert success....." + "[Id : ]" + this.id);
            return true;
        } else {
            log.error("Insert Fails....." + "[Id : ]" + this.id);

            return false;
        }
    }

    public boolean rawUpdate() {
        long updateNum = JDBCBuilder.insert(
                "update `" + TABLE_NAME + "` set  `tmallSearchType` = ?, `visitedTs` = ?, `errorReason` = ?, `searchedNick` = ? where `id` = ? ",
                this.tmallSearchType, this.visitedTs, this.errorReason, this.searchedNick, this.getId());

        if (updateNum > 0L) {

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

}
