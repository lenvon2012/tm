package models.mysql.fengxiao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

import play.Play;
import play.db.jpa.GenericModel;

import models.item.ItemCatPlay;
import models.item.ItemPropSale;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.pojo.ItemThumb;
import com.taobao.api.domain.Item;

import transaction.JDBCBuilder;
import transaction.DBBuilder.DataSrc;
import underup.frame.industry.CatTopSaleItemSQL;
import underup.frame.industry.ItemsCatArrange;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

public class HotWordCount extends GenericModel implements PolicySQLGenerator {
    private static final Logger log = LoggerFactory.getLogger(HotWordCount.class);

    public static final String NAME = "hot_word_count";

    Long id;

    String hotWord;

    Long count;

    Long cid;

    long year;

    long month;

    public static HotWordCount EMPTY = new HotWordCount();

    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

    public HotWordCount() {
    }

    public HotWordCount(String hotWord, Long count, Long cid, long year, long month) {
        this.hotWord = hotWord;
        this.count = count;
        this.cid = cid;
        this.year = year;
        this.month = month;
    }

    @Override
    public String getTableName() {
        return NAME;
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
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "HotWordCount [id=" + id + ", hotWord=" + hotWord + ", count=" + count + ", cid=" + cid + "]";
    }

    @Override
    public String getIdName() {
        return "id";
    }

    public static String getTableName(long year, long month) {
        Date date = new Date(year + month);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        String y = sdf.format(date);
        sdf = new SimpleDateFormat("MM");
        String m = sdf.format(date);
        return "hot_word_count_" + y + "_" + m;
    }
    
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public boolean jdbcSave() {
        try {
            long existedId = isExisted(this.hotWord, this.cid);
            if (existedId == 0L) {
                return rawInsert();
            } else {
                return rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }
    }

    
    public static String getIsExistedQuery(){
        long[] ym = CatTopSaleItemSQL.getTime();
        String tableName = getTableName(ym[0], ym[1]);
        String isExistedQuery = "select id from "+ tableName +" where hotWord=? and cid=?";
        return isExistedQuery;
    }
    
    public static long isExisted(String hotWord, long cid) {
        String isExistedQuery = getIsExistedQuery();
        return dp.singleLongQuery(isExistedQuery, hotWord, cid);
    }

    public static String getInsertQuery(){
        long[] ym = CatTopSaleItemSQL.getTime();
        String insertQuery = "insert into "+ getTableName(ym[0], ym[1]) +" (hotWord, count, cid, year, month) values(?,?,?,?,?)";
        return insertQuery;
    }
    
    public boolean rawInsert() {
        String insertQuery = getInsertQuery();
        long id = dp.insert(false, insertQuery, this.hotWord, this.count, this.cid, this.year, this.month);
        if (id > 0L) {
            return true;
        } else {
            log.error("insert fail...");
            return false;
        }
    }

    public static String getUpdateQuery(){
        long[] ym = CatTopSaleItemSQL.getTime();
        String updateQuery = "update "+getTableName(ym[0], ym[1]) +" set count=? where hotWord=? and cid=? and year=? and month=?";
        return updateQuery;
    }
    
    public boolean rawUpdate() {
        String updateQuery = getUpdateQuery();
        long updateNum = dp.insert(false, updateQuery, this.count, this.hotWord, this.cid, this.year, this.month);
        if (updateNum == 1L) {
            log.info("update ok!");
            return true;
        } else {
            log.info("update fail .....");
            return false;
        }

    }

    @JsonAutoDetect
    public static class WordCount implements Comparable {
        @JsonProperty
        public String word;

        @JsonProperty
        public Long count;

        public WordCount(String word, Long count) {
            this.word = word;
            this.count = count;
        }

        @Override
        public int compareTo(Object o) {
            WordCount o1 = (WordCount)o;
            return (int)(o1.count - this.count);
        }
        
        @Override
        public String toString(){
            return " word:" + word +" count:" + count;
        }

    }

    // 得到爆款词前20
    public static List<WordCount> getTopWord20(Long cid, Long year, long month) {
        String tableName = getTableName(year, month);
        String query = "select hotWord, count from " + tableName +" where cid=? order by count desc limit 20";
        return new JDBCBuilder.JDBCExecutor<List<WordCount>>(dp, query, cid) {
            @Override
            public List<WordCount> doWithResultSet(ResultSet rs) throws SQLException {
                List<WordCount> topWord = new ArrayList<WordCount>();
                while (rs.next()) {
                    String word = rs.getString(1);
                    Long count = rs.getLong(2);
                    topWord.add(new WordCount(word, count));
                }
                return topWord;
            }
        }.call();
    }

    // 得到所有爆款词的个数
    public static int getTopWordSize(Long cid, Long year, long month) {
        String tableName = getTableName(year, month);
        ItemCatPlay itemCatPlay = ItemCatPlay.findByCid(cid);
        String cidString = StringUtils.EMPTY;
        if (itemCatPlay.isParent) {
            List<Long> cids = ItemsCatArrange.getChildrenCids(cid, year, month);
            if (cids != null) {
                cidString += "cid=" + cids.get(0);
                for (int i = 1; i < cids.size(); ++i) {
                    cidString += " or cid=" + cids.get(i);
                }
            }
        } else
            cidString = "cid=" + cid;
        String query = "select count(1) from " + tableName +" where " + cidString;
        return new JDBCBuilder.JDBCExecutor<Integer>(dp, query) {
            @Override
            public Integer doWithResultSet(ResultSet rs) throws SQLException {
                int count;
                rs.next();
                count = rs.getInt(1);
                return count;
            }
        }.call();
    }

    // 得到爆页面款词
    public static List<WordCount> getTopWord(long cid, long year, long month, int offsize, int ps) {
        String tableName = getTableName(year, month);
        List<WordCount> wordCounts = new ArrayList<WordCount>();
        ItemCatPlay itemCatPlay = ItemCatPlay.findByCid(cid);
        String cidString = StringUtils.EMPTY;
        if (itemCatPlay.isParent) {
            List<Long> cids = ItemsCatArrange.getChildrenCids(cid, year, month);
            if (cids != null) {
                cidString += "cid=" + cids.get(0);
                for (int i = 1; i < cids.size(); ++i) {
                    cidString += " or cid=" + cids.get(i);
                }
            }
        } else {
            cidString = "cid=" + cid;
        }
        String query = "select hotWord, count from " + tableName +" where " + cidString
                + " order by count desc limit ? offset ?";
        wordCounts =  new JDBCBuilder.JDBCExecutor<List<WordCount>>(dp, query, ps, offsize) {
            @Override
            public List<WordCount> doWithResultSet(ResultSet rs) throws SQLException {
                List<WordCount> topWord = new ArrayList<WordCount>();
                Map<String, Long> topWordsMap = new HashMap<String, Long>();
                while (rs.next()) {
                    String word = rs.getString(1);
                    Long count = rs.getLong(2);
                    if (topWordsMap.containsKey(word)) {
                        long c = topWordsMap.get(word) + count;
                        topWordsMap.remove(word);
                        topWordsMap.put(word, c);
                    }else{
                        topWordsMap.put(word, count);
                    }
                }
                List<String> keys = new ArrayList<String>(topWordsMap.keySet());
                if (keys != null) {
                    for (String key : keys) {
                        WordCount wc = new WordCount(key, topWordsMap.get(key));
                        topWord.add(wc);
                    }
                }
                return topWord;
            }
        }.call();
        
        Collections.sort(wordCounts);
        log.info("-----------------wordCounts  " + wordCounts);
        return wordCounts;
    }

    public static void insertPatch(Map<String, Long> hotWords, long cid, long year, long month) {
        String tableName = getTableName(year, month);
        Properties prop = Play.configuration;
        Connection conn = null;

        String url = prop.getProperty("base.db.url");
        if (StringUtils.isEmpty(url)) {
            url = prop.getProperty("db.url");
        }

        String user = prop.getProperty("base.db.user");
        if (StringUtils.isEmpty(user)) {
            user = prop.getProperty("db.user");
        }

        String pwd = prop.getProperty("base.db.pass");
        if (StringUtils.isEmpty(pwd)) {
            pwd = prop.getProperty("db.pass");
        }
        try {
            conn = DriverManager.getConnection(url, user, pwd);
            ResultSet rs = null;
            conn.setAutoCommit(false);
            String insertQuery = getInsertQuery();
            PreparedStatement prest = conn.prepareStatement(insertQuery);
            PreparedStatement up = conn
                    .prepareStatement("select id from " + tableName +" where cid=? and hotWord = ?");
            List<Map.Entry<String, Long>> keyWords = new ArrayList<Map.Entry<String, Long>>(hotWords.entrySet());
            for (Map.Entry<String, Long> keyWord : keyWords) {
                up.setLong(1, cid);
                up.setString(2, keyWord.getKey());
                rs = up.executeQuery();
                long flag = 0L;
                if (rs.next()) {
                    flag = rs.getLong(1);
                    if (flag >= 0L)
                        log.info("-------------------------------------------------------------------------------------need to update numiid");
                }
                if (flag == 0L) {
                    prest.setString(1, keyWord.getKey());
                    prest.setLong(2, keyWord.getValue());
                    prest.setLong(3, cid);
                    prest.setLong(4, year);
                    prest.setLong(5, month);
                    prest.addBatch();
                }
            }
            prest.executeBatch();
            conn.commit();
            conn.close();
        } catch (SQLException e) {
            log.error("connect to database fial........");
        }
    }

//    public static List<Long> getBackcid1() {
//        
//        String query = "select cid from hot_word_count group by cid";
//        return new JDBCBuilder.JDBCExecutor<List<Long>>(dp, query) {
//            @Override
//            public List<Long> doWithResultSet(ResultSet rs) throws SQLException {
//                List<Long> catList = new ArrayList<Long>();
//                while (rs.next()) {
//                    long cid = rs.getLong(1);
//                    catList.add(cid);
//                }
//                return catList;
//            }
//
//        }.call();
//    }

}
