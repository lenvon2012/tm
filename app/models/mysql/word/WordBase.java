
package models.mysql.word;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import job.writter.RawWordBaseUpdateWritter;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.data.validation.Unique;
import play.db.jpa.GenericModel;
import play.jobs.Job;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.PolicySQLGenerator.CodeNoUpdate;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.api.WidAPIs;
import com.ciaosir.client.pojo.IWordBase;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.MapIterator;
import com.ciaosir.client.utils.NumberUtil;
import com.ciaosir.commons.ClientException;
import com.taobao.api.domain.INRecordBase;

@Entity(name = WordBase.TABLE_NAME)
@JsonIgnoreProperties(value = {
        "persistent", "entityId", "dataSrc", "tableName", "idName", "idColumn", "dataSrc", "hashColumnName", "hashed",
        "tableHashKey"
})
public class WordBase extends GenericModel implements IWordBase {

    private static final Logger log = LoggerFactory.getLogger(WordBase.class);

    public static final String TAG = "WordBase";

    public static final String TABLE_NAME = "sp_word";

    @Id
    @GeneratedValue
    public Long id;

    public Long getId() {
        return id;
    }

    @Index(name = "word")
    @Column(columnDefinition = "varchar(127) default '' not null")
    @Unique
    @CodeNoUpdate
    public String word;

    @Column(columnDefinition = "int default -1")
    public Integer price = NumberUtil.NEVER_START;

    @Column(columnDefinition = "int default -1")
    public Integer click = NumberUtil.NEVER_START;

    @Column(columnDefinition = "int default -1")
    public Integer competition = NumberUtil.NEVER_START;

    @Column(columnDefinition = "int default -1")
    public Integer cid = NumberUtil.NEVER_START;

    @Column(columnDefinition = "int default -1")
    public Integer pv = NumberUtil.NEVER_START;

    @Column(columnDefinition = "int default -1")
    public Integer strikeFocus = NumberUtil.NEVER_START;

    @Column(columnDefinition = "int default -1")
    public Integer searchFocus = NumberUtil.NEVER_START;

    @JsonIgnore
    @Column(columnDefinition = "int default -1")
    public Integer ctr = NumberUtil.NEVER_START;

    @Column(columnDefinition = "int default -1")
    public Long totalPayed = Long.MIN_VALUE;

    @Column(columnDefinition = "bigint default -1")
    public Long lastINWordUpdate = NumberUtil.DEFAULT_LONG;

    @Column(columnDefinition = "int default -1")
    public Integer score = NumberUtil.DEFAULT_ZERO;

    @Column(columnDefinition = "int default -1")
    public Integer scount = NumberUtil.DEFAULT_ZERO;

    @Column(columnDefinition = "int default 0")
    public Integer status = NumberUtil.DEFAULT_ZERO;

    public WordBase() {
        super();
    }

    public static WordBase _instance = new WordBase();

    public static String INSERT_SQL = "insert into `" + TABLE_NAME + "` (`word`) values(?);";

    static String INSERT_ONE_SQL = "insert into `"
            + TABLE_NAME
            + "`(`id`, `word`,`price`,`click`,`competition`,"
            + "`pv`,`strikeFocus`,`searchFocus`,`lastINWordUpdate`,`score`,`status`,`scount`,`cid`) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";

//
//    public static DBDispatcher rawwordDispatcher = new DBDispatcher(DataSrc.ITEMBUSES, _instance) {
//    };

    public boolean rawInsert() {
        long id = JDBCBuilder.insert(false, false, DataSrc.BASIC, INSERT_ONE_SQL, this.getId(), this.word, this.price,
                this.click, this.competition, this.pv, this.strikeFocus, this.searchFocus, this.lastINWordUpdate,
                this.score, this.status, this.scount, this.cid);

        if (id >= 0L) {
//            this.setId(id);
            return true;
        } else {
            log.warn("Insert Failes.....");
            return false;
        }
    }

    public void fetchFromResultSet(ResultSet rs) throws SQLException {
        this.id = rs.getLong(1);
        this.word = rs.getString(2);
        this.price = rs.getInt(3);
        this.click = rs.getInt(4);
        this.competition = rs.getInt(5);
        this.pv = rs.getInt(6);
        this.strikeFocus = rs.getInt(7);
        this.searchFocus = rs.getInt(8);
        this.lastINWordUpdate = rs.getLong(9);
        this.score = rs.getInt(10);
        this.status = rs.getInt(11);
        this.scount = rs.getInt(12);
        this.cid = rs.getInt(13);
    }

    public static final String SELECT_QUERY = "select id, word, price, click, competition, pv, strikeFocus, searchFocus,"
            + " lastINWordUpdate, score ,status, scount, cid from " + TABLE_NAME + " where ";

    public static final String SELECT_SQL = "select id, word, price, click, competition, pv, strikeFocus, searchFocus,"
            + " lastINWordUpdate, score ,status, scount, cid from ";

    static String UPDATE_QUERY_SQL = "update `" + TABLE_NAME + "` set  `price` = ?, `click` = ?, "
            + "`competition` = ?, `pv` = ?, `strikeFocus` = ?, `searchFocus` = ?, "
            + "`lastINWordUpdate` = ?, `score` = ?, `status` = ?, `scount` = ?, `cid` = ? where `id` = ? ";

    public boolean rawUpdate() {
        long updateNum = JDBCBuilder.update(false, UPDATE_QUERY_SQL, this.price, this.click, this.competition, this.pv,
                this.strikeFocus, this.searchFocus, this.lastINWordUpdate, this.score, this.status, this.scount,
                this.cid, this.getId());

        return updateNum > 0L;
    }

    public static List<WordBase> fetch(String whereQuery, Object... args) {
        return new ListElasticRawWordFetcher(false, WordBase.SELECT_QUERY + whereQuery, args).call();
    }

    public static List<WordBase> search(String whereQuery, int pn, int ps) {
//        return new ListElasticRawWordFetcher(false, WordBase.SELECT_SQL + whereQuery+"limit "+pn+","+ps+"").call();

        PageOffset po = new PageOffset(pn, ps);

        return new ListElasticRawWordFetcher(false, WordBase.SELECT_QUERY + whereQuery + "  limit ?,?",
                po.getOffset(), po.getPs()).call();

    }

    static class ListElasticRawWordFetcher extends JDBCExecutor<List<WordBase>> {

        public ListElasticRawWordFetcher(boolean debug, String query, Object... params) {
            super(query, params);
        }

        @Override
        public List<WordBase> doWithResultSet(ResultSet rs) throws SQLException {
            final List<WordBase> list = new ArrayList<WordBase>();
            while (rs.next()) {
                WordBase model = new WordBase();
                model.fetchFromResultSet(rs);
                list.add(model);
            }
            return list;
        }
    }

    public void updateByWordBaseBean(IWordBase record) {
        if (record == null) {
            return;
        }

        this.click = NumberUtil.parserInt(record.getClick(), NumberUtil.NONE_EXIST);
        this.pv = NumberUtil.parserInt(record.getPv(), NumberUtil.NONE_EXIST);
        this.competition = NumberUtil.parserInt(record.getCompetition(), NumberUtil.NONE_EXIST);
        this.price = NumberUtil.parserInt(record.getPrice(), NumberUtil.NONE_EXIST);
    }

    public static WordBase findOrCreateByWord(String word) throws ClientException {
        if (StringUtils.isBlank(word)) {
            return null;
        }

        WordBase first = NumberUtil.first(fetch(" word = ?", word));
        if (first != null) {
            return first;
        }

        Long execute = new WidAPIs.GetIdByWord(word).execute();
        first = new WordBase();
        first.setId(execute);
        first.setWord(word);
        log.info("[first : ]" + first);
        first.rawInsert();
        return first;
    }

    public static WordBase findOrCreateByWord(Long id, String word) throws ClientException {
        if (StringUtils.isBlank(word)) {
            return null;
        }

        WordBase first = NumberUtil.first(fetch(" word = ?", word));
        if (first != null) {
            return first;
        }

        first = new WordBase();
        first.setId(id);
        first.setWord(word);
        log.info("[first : ]" + first);
        first.rawInsert();
        return first;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getClick() {
        return click;
    }

    public void setClick(Integer click) {
        this.click = click;
    }

    public Integer getCompetition() {
        return competition;
    }

    public void setCompetition(Integer competition) {
        this.competition = competition;
    }

    public Integer getCid() {
        return cid;
    }

    public void setCid(Integer cid) {
        this.cid = cid;
    }

    public Integer getPv() {
        return pv;
    }

    public void setPv(Integer pv) {
        this.pv = pv;
    }

    public Integer getStrikeFocus() {
        return strikeFocus;
    }

    public void setStrikeFocus(Integer strikeFocus) {
        this.strikeFocus = strikeFocus;
    }

    public Integer getSearchFocus() {
        return searchFocus;
    }

    public void setSearchFocus(Integer searchFocus) {
        this.searchFocus = searchFocus;
    }

    public Integer getCtr() {
        return ctr;
    }

    public void setCtr(Integer ctr) {
        this.ctr = ctr;
    }

    public Long getTotalPayed() {
        return totalPayed;
    }

    public void setTotalPayed(Long totalPayed) {
        this.totalPayed = totalPayed;
    }

    public Long getLastINWordUpdate() {
        return lastINWordUpdate;
    }

    public void setLastINWordUpdate(Long lastINWordUpdate) {
        this.lastINWordUpdate = lastINWordUpdate;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getScount() {
        return scount;
    }

    public void setScount(Integer scount) {
        this.scount = scount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public static void flush(List<String> res) throws ClientException {
        Map<String, Long> execute = new WidAPIs.GetIdsByWords(res).execute();
        new MapIterator<String, Long>(execute) {

            @Override
            public void execute(Entry<String, Long> entry) {
                try {
                    WordBase.findOrCreateByWord(entry.getValue(), entry.getKey());
                } catch (ClientException e) {
                    log.warn(e.getMessage(), e);

                }
            }
        }.call();

    }

    public static class RawWordBaseUpdateJob extends Job {
        @Override
        public void doJob() {
            try {
                update(0, 128);
            } catch (ClientException e) {
                log.warn(e.getMessage(), e);
            }
        }
    }

    public static void update(int offset, int limit) throws ClientException {
        List<WordBase> next = null;
        while (!CommonUtils.isEmpty((next = fetch(" 1 = 1 limit ? offset ?", limit, offset)))) {
            log.info("[RawWordBaseUpdateJob current offset :]" + offset);
            List<String> words = new ArrayList<String>();
            for (WordBase wordBase : next) {
                words.add(wordBase.getWord());
            }
            Map<String, IWordBase> execute = new WidAPIs.WordBaseAPI(words).execute();
            for (WordBase wordBase : next) {
                IWordBase iWordBase = execute.get(wordBase.getWord());
                if (iWordBase != null) {
                    wordBase.updateByWordBaseBean(iWordBase);
                }
                RawWordBaseUpdateWritter.addMsg(wordBase);
            }

            offset += limit;
        }
    }

    /**
     * 普通查询
     */
    public static List<WordBase> norsearch(String[] args, int pn, int ps) {
        String condition = dealNormalCondition(args);

        return search(condition, pn, ps);
    }

    public static String dealNormalCondition(String[] args) {
        StringBuilder sb = new StringBuilder();
        if (args.length == 1) {
            sb.append(" word like '%" + CommonUtils.escapeSQL(args[0]) + "%' ");
        } else if (args.length >= 2) {
            sb.append(" ( ");
            for (String string : args) {
                sb.append(" word like '%" + CommonUtils.escapeSQL(string) + "%' ");
                if (!string.equals(args[args.length - 1])) {
                    sb.append(" and ");
                }
            }
            sb.append(" )");
        }
        return sb.toString();
    }

    /**
     * 普通查询
     */
    public static List<WordBase> andSearch(List<String> args, PageOffset po) {
        String condition = dealNormalConditionOR(args);

        return search(condition, po.getPn(), po.getPs());
    }

    public static String dealNormalConditionOR(List<String> args) {
        StringBuilder sb = new StringBuilder();
        if (args.size() == 1) {
            sb.append(" word like '%" + CommonUtils.escapeSQL(args.get(0)) + "%' ");
        } else if (args.size() >= 2) {
            sb.append(" ( ");
            for (String string : args) {
                sb.append(" word like '%" + CommonUtils.escapeSQL(string) + "%' ");
                if (!string.equals(args.get(args.size() - 1))) {
                    sb.append(" or ");
                }
            }
            sb.append(" )");
        }
        return sb.toString();
    }

    public static int getCountOfNormal(String[] args) {
        String sql = dealNormalCondition(args);
        //return (int) WordBase.count(sql);
        return (int) JDBCBuilder.singleLongQuery("select count(*) from " + TABLE_NAME + " where 1=1 and " + sql);
    }

    public static int getOrCountOfNormal(List<String> args) {
        String sql = dealNormalConditionOR(args);
        //return (int) WordBase.count(sql);
        return (int) JDBCBuilder.singleLongQuery("select count(*) from " + TABLE_NAME + " where 1=1 and " + sql);
    }

    /**
     * 查找一个词通过id
     */
    public static WordBase searchAWord(Long wordId) {
        StringBuilder sb = new StringBuilder();
        sb.append("id = ");
        sb.append(wordId);
        List<WordBase> word = WordBase.fetch(sb.toString());

        if (word != null && word.size() > 0) {
            return word.get(0);
        }
        return null;
    }

    public static List<WordBase> fetchMultipleAndLike(String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append(" 1 = 1");
        for (String string : args) {
            sb.append(" and ");
            sb.append(" word like '%");
            sb.append(CommonUtils.escapeSQL(string));
            sb.append("%'");
        }
        return fetch(sb.toString());
    }

    public static int getTotalHits(List<List<String>> conditions) {
        int count = 0;
        count = fetch(dealCondition(conditions)).size();
        return count;
    }

    public static List<WordBase> superSearch(List<List<String>> conditions, int pn, int ps) {
        return search(dealCondition(conditions), pn, ps);
    }

    public static String dealCondition(List<List<String>> conditions) {
        List<String> baohanall = conditions.get(0);
        List<String> baohanrengyi = conditions.get(1);
        List<String> buhanrengyi = conditions.get(2);
        StringBuilder sb = new StringBuilder();
        if (baohanall.isEmpty() == false) {
            sb.append("(word like '%" + CommonUtils.escapeSQL(baohanall.get(0)) + "%')");
            if (baohanrengyi.isEmpty() == false && buhanrengyi.isEmpty() == true) {
                if (baohanrengyi.size() == 1) {
                    sb.append("and ( word like '%" + CommonUtils.escapeSQL(baohanrengyi.get(0)) + "%')");
                } else if (baohanrengyi.size() == 2) {
                    sb.append("and ( word like '%" + CommonUtils.escapeSQL(baohanrengyi.get(0)) + "%' or ");
                    sb.append(" word like '%" + CommonUtils.escapeSQL(baohanrengyi.get(1)) + "%') ");
                } else if (baohanrengyi.size() == 3) {
                    sb.append("and ( word like '%" + CommonUtils.escapeSQL(baohanrengyi.get(0)) + "%' or ");
                    sb.append(" word like '%" + CommonUtils.escapeSQL(baohanrengyi.get(1)) + "%'or ");
                    sb.append(" word like '%" + CommonUtils.escapeSQL(baohanrengyi.get(2)) + "%')");
                }
            } else if (baohanrengyi.isEmpty() == true && buhanrengyi.isEmpty() == false) {
                if (buhanrengyi.size() == 1) {
                    sb.append("and ( word not like '%" + CommonUtils.escapeSQL(buhanrengyi.get(0)) + "%')");
                } else if (buhanrengyi.size() == 2) {
                    sb.append("and ( word not like '%" + CommonUtils.escapeSQL(buhanrengyi.get(0)) + "%' and ");
                    sb.append(" word not like '%" + CommonUtils.escapeSQL(buhanrengyi.get(1)) + "%') ");
                } else if (buhanrengyi.size() == 3) {
                    sb.append("and ( word not like '%" + CommonUtils.escapeSQL(buhanrengyi.get(0)) + "%' and ");
                    sb.append(" word not like '%" + CommonUtils.escapeSQL(buhanrengyi.get(1)) + "%'and ");
                    sb.append(" word not like '%" + CommonUtils.escapeSQL(buhanrengyi.get(2)) + "%')");
                }
            } else if (baohanrengyi.isEmpty() == false && buhanrengyi.isEmpty() == false) {
                if (baohanrengyi.size() == 1) {
                    sb.append("and ( word like '%" + CommonUtils.escapeSQL(baohanrengyi.get(0)) + "%')");
                } else if (baohanrengyi.size() == 2) {
                    sb.append("and ( word like '%" + CommonUtils.escapeSQL(baohanrengyi.get(0)) + "%' and ");
                    sb.append("word like '%" + CommonUtils.escapeSQL(baohanrengyi.get(1)) + "%')");
                } else if (baohanrengyi.size() == 3) {
                    sb.append("and ( word like '%" + CommonUtils.escapeSQL(baohanrengyi.get(0)) + "%' and ");
                    sb.append(" word like '%" + CommonUtils.escapeSQL(baohanrengyi.get(1)) + "%' and ");
                    sb.append(" word like '%" + CommonUtils.escapeSQL(baohanrengyi.get(2)) + "%' )");
                }
                if (buhanrengyi.size() == 1) {
                    sb.append("and (word not like '%" + CommonUtils.escapeSQL(buhanrengyi.get(0)) + "%')");
                } else if (buhanrengyi.size() == 2) {
                    sb.append("and ( word not like '%" + CommonUtils.escapeSQL(buhanrengyi.get(0)) + "%' and");
                    sb.append(" word not like '%" + CommonUtils.escapeSQL(buhanrengyi.get(1)) + "%' )");
                } else if (buhanrengyi.size() == 3) {
                    sb.append("and ( word not like '%" + CommonUtils.escapeSQL(buhanrengyi.get(0)) + "%' and");
                    sb.append(" word not like '%" + CommonUtils.escapeSQL(buhanrengyi.get(1)) + "%' ");
                    sb.append(" word not like '%" + CommonUtils.escapeSQL(buhanrengyi.get(2)) + "%' )");
                }
            }

        }
        return sb.toString();
    }

    @Override
    public float getMatch() {
        return 0;
    }

    @Override
    public void setMatch(float match) {
    }

    @Override
    public void updateByINInfo(INRecordBase record) {
    }
}
