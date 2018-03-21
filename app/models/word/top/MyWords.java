package models.word.top;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = MyWords.TABLE_NAME)
public class MyWords extends Model implements PolicySQLGenerator {
    @Transient
    private static final Logger log = LoggerFactory.getLogger(MyWords.class);

    @Transient
    public static final String TABLE_NAME = "my_words";

    private String word;
    
    private Long userId;

    public MyWords() {
        super();
    }

    public MyWords(String word, Long userId) {
        this.userId = userId;
        this.word = word;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return this.userId;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public static List<MyWords> search(Long userId, int offset, int limit){
        String sql = "select userId, word from my_words where userId = ? limit ?, ?";
        return new JDBCExecutor<List<MyWords>>(true, sql, userId, offset, limit) {

            @Override
            public List<MyWords> doWithResultSet(ResultSet rs) throws SQLException {
                List<MyWords> itemList = new ArrayList<MyWords>();
                while (rs.next()) {
                    itemList.add(new MyWords(rs.getString(2),rs.getLong(1)));
                }
                return itemList;
            }
        }.call();
    }
    
    public static MyWords singleSearch(Long userId, String word){
        String sql = "select userId, word from my_words where userId = ? and word = ?";
        return new JDBCExecutor<MyWords>(true, sql, userId, word) {
            @Override
            public MyWords doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return new MyWords(rs.getString(2),rs.getLong(1));
                } else {
                	return null;
                }
            }
        }.call();
    }
    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getId()
     */
    @Override
    public Long getId() {
        // TODO Auto-generated method stub
        return id;
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

    static String EXIST_ID_QUERY = "select id from " + TABLE_NAME + " where userId = ? and word = ? ";

    private static long findExistId(Long userId, String word) {
        return JDBCBuilder.singleLongQuery(EXIST_ID_QUERY, userId,word);
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#jdbcSave()
     */
    @Override
    public boolean jdbcSave() {

        try {
            long existdId = findExistId(this.userId, this.word);
//            if (existdId != 0)
//                log.info("find existed Id: " + existdId);

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

    public boolean rawInsert() {
        long id = JDBCBuilder
                .insert("insert into `my_words`(`word`,`userId`) values(?,?)",
                        this.word, this.userId);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.userId);
            return false;
        }

    }

    public boolean rawUpdate() {
        long updateNum = JDBCBuilder
                .insert("update `my_words` set  `word` = ?, `userId` = ? where `id` = ? ",
                        this.word, this.userId, this.id);

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for :" + this.id + "[userId : ]" + this.userId);

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

    @Override
    public void _save() {
        this.jdbcSave();
    }
}
