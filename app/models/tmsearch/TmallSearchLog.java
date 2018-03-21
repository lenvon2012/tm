package models.tmsearch;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.PolicySQLGenerator;


@Entity(name = TmallSearchLog.TABLE_NAME)
public class TmallSearchLog extends Model implements PolicySQLGenerator {
    @Transient
    private static final Logger log = LoggerFactory.getLogger(TmallSearchLog.class);

    @Transient
    public static final String TABLE_NAME = "tmall_search_log";

    public static class TmallSearchType {
        public static final int Comment = 1;
        public static final int Authority = 2;
        public static final int ShopSales = 3;
    }
    @Index(name = "userIp")
    private String userIp;
    
    @Index(name = "tmallSearchType")
    private int tmallSearchType = 0;
    
    private long visitedTs = 0L;
    private long usedTime = 0L;
    
    @Index(name = "searchedNick")
    private String searchedNick;
    
    public static class SearchStatus {
        public static final int Success = 1;
        public static final int Error = 2;
    }
    
    private int searchStatus = 0;

    public String getUserIp() {
        return userIp;
    }

    public void setUserIp(String userIp) {
        this.userIp = userIp;
    }

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

    

    public long getUsedTime() {
        return usedTime;
    }

    public void setUsedTime(long usedTime) {
        this.usedTime = usedTime;
    }

    public String getSearchedNick() {
        return searchedNick;
    }

    public void setSearchedNick(String searchedNick) {
        this.searchedNick = searchedNick;
    }

    

    public int getSearchStatus() {
        return searchStatus;
    }

    public void setSearchStatus(int searchStatus) {
        this.searchStatus = searchStatus;
    }

    
    public TmallSearchLog(String userIp, int tmallSearchType, long visitedTs,
            long usedTime, String searchedNick, int searchStatus) {
        super();
        this.userIp = userIp;
        this.tmallSearchType = tmallSearchType;
        this.visitedTs = visitedTs;
        this.usedTime = usedTime;
        this.searchedNick = searchedNick;
        this.searchStatus = searchStatus;
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
                "insert into `" + TABLE_NAME + "`(`userIp`,`tmallSearchType`,`visitedTs`,`usedTime`,`searchedNick`,`searchStatus`) values(?,?,?,?,?,?)", this.userIp, this.tmallSearchType,
                this.visitedTs, this.usedTime, this.searchedNick, this.searchStatus);

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
                "update `" + TABLE_NAME + "` set  `userIp` = ?, `tmallSearchType` = ?, `visitedTs` = ?, `usedTime` = ?, `searchedNick` = ?, `searchStatus` = ? where `id` = ? ",
                this.userIp, this.tmallSearchType, this.visitedTs, this.usedTime, this.searchedNick, this.searchStatus, this.getId());

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

    
    
    public static List<String> queryLatestSearchedNick(int searchType) {
        String sql = "select distinct searchedNick from " + TABLE_NAME + " where tmallSearchType = ? and searchStatus = ? " +
        		" and searchedNick <> '' and (searchedNick is not null) " +
        		"  order by id desc limit ?,? ";
        
        List<String> nickList = new JDBCExecutor<List<String>>(sql, searchType, SearchStatus.Success, 0, 20) {
            @Override
            public List<String> doWithResultSet(ResultSet rs) throws SQLException {
                List<String> tempList = new ArrayList<String>();
                while (rs.next()) {
                    try {
                        String temp = rs.getString(1);
                        tempList.add(temp);
                    } catch (Exception ex) {
                        log.error(ex.getMessage(), ex);
                    }
                    
                }
                return tempList;
            }
        }.call();
        
        return nickList;
    }
}
