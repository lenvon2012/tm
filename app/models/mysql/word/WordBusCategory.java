
package models.mysql.word;

import static java.lang.String.format;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.utils.NumberUtil;

@Entity(name = WordBusCategory.TABLE_NAME)
public class WordBusCategory extends Model implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(WordBusCategory.class);

    public static final String TAG = "WordBusCategory";

    public static WordBusCategory _instance = new WordBusCategory();

    public WordBusCategory() {
    }

    public static final String TABLE_NAME = "word_bus_category";

    public String word;

    public Long wid;

    public Long cid1;

    public Long cid2;

    public Long cid3;

    @Override
    public String getTableName() {
        return TABLE_NAME;
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
    public String getIdName() {
        return "id";
    }

    public long rawInsert() {
        long id = JDBCBuilder.insert(
                "insert into `word_bus_category`(`word`,`wid`,`cid1`,`cid2`,`cid3`) values(?,?,?,?,?)", this.word,
                this.wid, this.cid1, this.cid2, this.cid3);
        if (id >= 0) {
            return id;
        } else {
            log.error("insert fails....");
            return -1L;
        }
    }

    public long rawUpdate() {
        long updateNum = JDBCBuilder
                .update(false,
                        "update `word_bus_category`( `word` = ?, `wid` = ?, `cid1` = ?, `cid2` = ?, `cid3` = ? where `id` = ? ",
                        this.word, this.wid, this.cid1, this.cid2, this.cid3, this.getId());
        if (updateNum > 0) {
            return updateNum;
        } else {
            log.error("update fails....");
            return -1L;
        }
    }

    public WordBusCategory(ResultSet rs) throws SQLException {
        this.word = rs.getString(1);
        this.wid = rs.getLong(2);
        this.cid1 = rs.getLong(3);
        this.cid2 = rs.getLong(4);
        this.cid3 = rs.getLong(5);

    }

    public long findExistId() {
        long existId = JDBCBuilder.singleLongQuery("select id  from `word_bus_category` where id = ?", this.getId());
        return existId;
    }

    public boolean jdbcSave() {
        long existId = findExistId();
        if (existId > 0L) {
            return this.rawUpdate() > 0L;
        } else {
            return this.rawInsert() >= 0L;
        }

    }

    public static class ListFetcher extends JDBCExecutor<List<WordBusCategory>> {

        public ListFetcher(String whereQuery, Object... params) {
            super(whereQuery, params);
            StringBuilder sb = new StringBuilder();
            sb.append("select word,wid,cid1,cid2,cid3 from word_bus_category");
            sb.append(" where  1= 1 ");
            if (!StringUtils.isBlank(whereQuery)) {
                sb.append(" and ");
                sb.append(whereQuery);

            }
            this.query = sb.toString();

        }

        @Override
        public List<WordBusCategory> doWithResultSet(ResultSet rs) throws SQLException {
            List<WordBusCategory> list = new ArrayList<WordBusCategory>();
            while (rs.next()) {
                list.add(new WordBusCategory(rs));
            }
            return list;
        }
    }

    public static int count(Long hashKeyId, String whereQuery, Object... params) {
        StringBuilder sb = new StringBuilder();
        sb.append("select count(*) from ");
        sb.append(TABLE_NAME);
        sb.append(" where  1 = 1");
        if (!StringUtils.isBlank(whereQuery)) {
            sb.append(" and ");
            sb.append(whereQuery);
        }
        return (int) JDBCBuilder.singleLongQuery(sb.toString(), params);

    }

    static Map<String, WordBusCategory> cache = new HashMap<String, WordBusCategory>();

    public static WordBusCategory ensure(BusCategory cat1, BusCategory cat2, BusCategory cat3, String text) {

        log.info(format("ensure:cat1, cat2, cat3, text".replaceAll(", ", "=%s, ") + "=%s", cat1, cat2, cat3, text));

        if (StringUtils.isBlank(text)) {
            return null;
        }

        Long cid1 = cat1 == null ? 0L : cat1.getId();
        Long cid2 = cat2 == null ? 0L : cat2.getId();
        Long cid3 = cat3 == null ? 0L : cat3.getId();
        String key = "+" + cid1 + cid2 + cid3;

        WordBusCategory first = cache.get(key);
        if (first != null) {
            return first;
        }
        first = NumberUtil.first(new ListFetcher(null, "word = ? and cid1 = ? and cid2 = ? and cid3 = ? ", cid1, cid2,
                cid3, text).call());

        if (first != null) {
            cache.put(key, first);
            return first;
        }

        first = new WordBusCategory();
        first.setCid1(cid1);
        first.setCid2(cid2);
        first.setCid3(cid3);
        first.setWord(text);
        first.jdbcSave();
        cache.put(key, first);
        return first;

    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Long getWid() {
        return wid;
    }

    public void setWid(Long wid) {
        this.wid = wid;
    }

    public Long getCid1() {
        return cid1;
    }

    public void setCid1(Long cid1) {
        this.cid1 = cid1;
    }

    public Long getCid2() {
        return cid2;
    }

    public void setCid2(Long cid2) {
        this.cid2 = cid2;
    }

    public Long getCid3() {
        return cid3;
    }

    public void setCid3(Long cid3) {
        this.cid3 = cid3;
    }

    @Override
    public String getTableHashKey() {
        return null;
    }

}
