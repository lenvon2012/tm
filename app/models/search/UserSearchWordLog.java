
package models.search;

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
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = UserSearchWordLog.TABLE_NAME)
public class UserSearchWordLog extends Model implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(UserSearchWordLog.class);

    @Transient
    public static final String TABLE_NAME = "user_search_word_log";

    @Transient
    public static UserSearchWordLog EMPTY = new UserSearchWordLog();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

    @Index(name = "userId")
    private Long userId;

    @Index(name = "word")
    private String word;

    private int searchType;

    public static class UserSearchWordType {
        public static final int QueryRank = 1;//查排名
        
        public static final int MobileRank = 2;//手机查排名
    }

    private long searchTs;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getSearchType() {
        return searchType;
    }

    public void setSearchType(int searchType) {
        this.searchType = searchType;
    }

    public long getSearchTs() {
        return searchTs;
    }

    public void setSearchTs(long searchTs) {
        this.searchTs = searchTs;
    }

    public UserSearchWordLog() {
        super();
    }

    public UserSearchWordLog(Long userId, String word, int searchType,
            long searchTs) {
        super();
        this.userId = userId;
        this.word = word;
        this.searchType = searchType;
        this.searchTs = searchTs;
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

        String query = "select id from " + TABLE_NAME + " where id = ? ";

        return dp.singleLongQuery(query, id);
    }

    @Override
    public boolean jdbcSave() {
        try {

            long existdId = findExistId(this.id);

            if (existdId <= 0L) {
                return this.rawInsert();
            } else {
                setId(existdId);
                return this.rawUpdate();
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public boolean rawInsert() {

        String insertSQL = "insert into `" + TABLE_NAME + "`" +
                "(`userId`,`word`,`searchType`,`searchTs`) " +
                " values(?,?,?,?)";

        long id = dp.insert(true, insertSQL,
                this.userId, this.word, this.searchType, this.searchTs);

        if (id > 0L) {
            setId(id);
            return true;
        } else {
            log.error("Insert Fails.....");
            return false;
        }

    }

    public boolean rawUpdate() {

        String updateSQL = "update `" + TABLE_NAME + "` set  " +
                " `userId` = ?,`word` = ?,`searchType` = ?,`searchTs` = ? " +
                " where `id` = ? ";

        long updateNum = dp.update(false, updateSQL,
                this.userId, this.word, this.searchType, this.searchTs);

        if (updateNum == 1) {
            //log.info("update ok for :" + this.getId());
            return true;
        } else {
            log.error("update failed...for :" + this.getId());
            return false;
        }
    }

    public static List<String> findLatestWordsByUserIdAndType(Long userId, int searchType, int limit) {
        String query = " select word, max(id) as mid from " + TABLE_NAME
                + " where userId = ? and searchType = ? group by word order by mid desc limit ?, ? ";

        return new JDBCBuilder.JDBCExecutor<List<String>>(dp, query, userId,
                searchType, 0, limit) {

            @Override
            public List<String> doWithResultSet(ResultSet rs)
                    throws SQLException {
                List<String> wordList = new ArrayList<String>();

                while (rs.next()) {
                    String word = rs.getString(1);
                    wordList.add(word);
                }

                return wordList;

            }

        }.call();
    }

    /*
    public static List<UserSearchWordLog> findLatestByUserIdAndType(Long userId, int searchType, int limit) {
        String query = " select " + SelectAllProperty + " from " + TABLE_NAME 
                + " where userId = ? and searchType = ? order by id desc limit ?, ? ";
        
        return new JDBCBuilder.JDBCExecutor<List<UserSearchWordLog>>(dp, query, userId, 
                searchType, 0, limit) {

                @Override
                public List<UserSearchWordLog> doWithResultSet(ResultSet rs)
                        throws SQLException {
                    List<UserSearchWordLog> searchLogList = new ArrayList<UserSearchWordLog>();
                    
                    while (rs.next()) {
                        UserSearchWordLog searchLog = parseUserSearchWordLog(rs);
                        if (searchLog != null) {
                            searchLogList.add(searchLog);
                        }
                    }
                    
                    return searchLogList;
                    
                }
            
            
        }.call();
    }
    */

    private static final String SelectAllProperty = " id, userId, word, searchType, searchTs ";

    private static UserSearchWordLog parseUserSearchWordLog(ResultSet rs) {
        try {

            UserSearchWordLog searchLog = new UserSearchWordLog();

            searchLog.setId(rs.getLong(1));
            searchLog.setUserId(rs.getLong(2));
            searchLog.setWord(rs.getString(3));
            searchLog.setSearchType(rs.getInt(4));
            searchLog.setSearchTs(rs.getLong(5));

            return searchLog;

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

}
