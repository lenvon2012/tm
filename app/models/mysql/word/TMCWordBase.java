
package models.mysql.word;

import static java.lang.String.format;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import play.data.validation.Unique;
import play.db.jpa.GenericModel;
import result.TMResult;
import search.SearchManager;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.api.SearchAPIs.SearchParams;
import com.ciaosir.client.api.SearchAPIs.SearchRes;
import com.ciaosir.client.api.SearchAPIs.TermSearchApi;
import com.ciaosir.client.api.WidAPIs;
import com.ciaosir.client.pojo.IWordBase;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.MapIterator;
import com.ciaosir.client.utils.NumberUtil;
import com.ciaosir.commons.ClientException;
import com.taobao.api.domain.INRecordBase;

@Entity(name = TMCWordBase.TABLE_NAME)
@JsonIgnoreProperties(value = {
        "persistent", "entityId", "dataSrc", "tableName", "idName", "idColumn", "dataSrc",
        "hashColumnName", "hashed", "tableHashKey"
})
public class TMCWordBase extends GenericModel implements IWordBase, PolicySQLGenerator, Serializable {

    private static final long serialVersionUID = -1L;

    private static final Logger log = LoggerFactory.getLogger(TMCWordBase.class);

    public static final String TAG = "WordBase";

    public static final String TABLE_NAME = "word_base";

    static DataSrc src = DataSrc.BASIC;

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

    /**
     * 点击率strikeFocus = click * 10000L / this.pv
     */
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

    /**
     * pv/关键词对应宝贝数量 = pv * 10000L/scount
     */
    @Column(columnDefinition = "int default -1")
    public Integer score = NumberUtil.DEFAULT_ZERO;

    /**
     * 关键词对应宝贝数量
     */
    @Column(columnDefinition = "int default -1")
    public Integer scount = NumberUtil.DEFAULT_ZERO;

    @Column(columnDefinition = "int default 0")
    public Integer status = NumberUtil.DEFAULT_ZERO;

    public TMCWordBase() {
        super();
    }

    public TMCWordBase(String word, Integer scount) {
        super();
        this.word = word;
        this.scount = scount;
    }

    public TMCWordBase(IWordBase base) {
        super();
        this.word = base.getWord();
        this.price = base.getPrice();
        this.click = base.getClick();
        this.competition = base.getCompetition();
        this.cid = base.getCid();
        this.pv = base.getPv();
        // this.strikeFocus = base.getStrikeFocus();
        if (this.pv > 0) {
            this.strikeFocus = (int) (this.click * 10000L / this.pv);
        }
        this.searchFocus = base.getSearchFocus();
        // this.totalPayed = ...
        // this.lastINWordUpdate = ...
        if (base.getScount() > 0) {
            this.scount = base.getScount();
            this.score = (int) (base.getPv() * 10000L / this.scount);
        }
        this.status = base.getStatus();
    }

    public TMCWordBase(Long id, String word, Integer price, Integer click, Integer competition, Integer cid,
            Integer pv,
            Integer strikeFocus, Integer searchFocus, Integer ctr, Long totalPayed, Long lastINWordUpdate,
            Integer score, Integer scount, Integer status) {
        super();
        this.id = id;
        this.word = word;
        this.price = price;
        this.click = click;
        this.competition = competition;
        this.cid = cid;
        this.pv = pv;
        this.strikeFocus = strikeFocus;
        this.searchFocus = searchFocus;
        this.ctr = ctr;
        this.totalPayed = totalPayed;
        this.lastINWordUpdate = lastINWordUpdate;
        this.score = score;
        this.scount = scount;
        this.status = status;
    }

    public static TMCWordBase _instance = new TMCWordBase();

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

    public static String INSERT_SQL = "insert into `" + TABLE_NAME + "` (`word`) values(?);";

    static String INSERT_ONE_SQL = "insert into `"
            + TABLE_NAME
            + "`(`id`, `word`,`price`,`click`,`competition`,"
            + "`pv`,`strikeFocus`,`searchFocus`,`lastINWordUpdate`,`score`,`status`,`scount`,`cid`) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";

    //
    // public static DBDispatcher rawwordDispatcher = new DBDispatcher(DataSrc.ITEMBUSES, _instance) {
    // };

    public boolean rawInsert() {
        long id = JDBCBuilder.insert(false, false, src, INSERT_ONE_SQL, this.getId(), this.word, this.price,
                this.click, this.competition, this.pv, this.strikeFocus, this.searchFocus, this.lastINWordUpdate,
                this.score, this.status, this.scount, this.cid);

        if (id >= 0L) {
            // this.setId(id);
            return true;
        } else {
            log.warn("Insert Failes.....");
            return false;
        }
    }

    public static final String SELECT_QUERY = "select id, word, price, click, competition, pv, strikeFocus, searchFocus,"
            + " lastINWordUpdate, score ,status, scount, cid from " + TABLE_NAME + " where ";

    public static final String SELECT_SQL = "select id, word, price, click, competition, pv, strikeFocus, searchFocus,"
            + " lastINWordUpdate, score ,status, scount, cid from ";

    public static final String SELECT_COUNT_QUERY = "select count(*) from " + TABLE_NAME + " where ";

    static String UPDATE_QUERY_SQL = "update `" + TABLE_NAME + "` set  `price` = ?, `click` = ?, "
            + "`competition` = ?, `pv` = ?, `strikeFocus` = ?, `searchFocus` = ?, "
            + "`lastINWordUpdate` = ?, `score` = ?, `status` = ?, `scount` = ?, `cid` = ? where `id` = ? ";

    public boolean rawUpdate() {
        long updateNum = JDBCBuilder.update(src, UPDATE_QUERY_SQL, this.price, this.click, this.competition, this.pv,
                this.strikeFocus, this.searchFocus, this.lastINWordUpdate, this.score, this.status, this.scount,
                this.cid, this.getId());

        return updateNum > 0L;
    }

    static String UPDATE_WITHOUT_ITEMCOUNT_QUERY_SQL = "update `" + TABLE_NAME + "` set  `price` = ?, `click` = ?, "
            + "`competition` = ?, `pv` = ?, `strikeFocus` = ?, `searchFocus` = ?, "
            + "`lastINWordUpdate` = ?, `status` = ?, `cid` = ? where `id` = ? ";

    public boolean rawUpdateWithOutItemCount() {
        long updateNum = JDBCBuilder.update(src, UPDATE_WITHOUT_ITEMCOUNT_QUERY_SQL, this.price, this.click,
                this.competition, this.pv,
                this.strikeFocus, this.searchFocus, this.lastINWordUpdate, this.status, this.cid, this.getId());

        return updateNum > 0L;
    }

    public static void rawUpdateItemCount(String word, int itemCount) {

        JDBCBuilder.update(src, "update " + TABLE_NAME + " set scount = ? where word = ? ", itemCount,
                StringEscapeUtils.escapeSql(word));
    }

    public static List<TMCWordBase> fetch(String whereQuery, Object... args) {
        return new ListElasticRawWordFetcher(false, TMCWordBase.SELECT_QUERY + whereQuery, args).call();
    }

    public static List<TMCWordBase> search(String whereQuery, int pn, int ps) {
        // return new ListElasticRawWordFetcher(false, WordBase.SELECT_SQL + whereQuery+"limit "+pn+","+ps+"").call();

        PageOffset po = new PageOffset(pn, ps);

        return new ListElasticRawWordFetcher(false, TMCWordBase.SELECT_QUERY + whereQuery + "  limit ? offset ?",
                po.getPs(), po.getOffset()).call();

    }

    public static List<TMCWordBase> queryList(String[] wordArr, int minPv, int maxPv, int minClick, int maxClick,
            int minPrice, int maxPrice, int minCompetition, int maxCompetition, PageOffset po) {
        if (wordArr.length <= 0) {
            return null;
        }
        String wordLike = "word like '%" + StringEscapeUtils.escapeSql(wordArr[0]) + "%' ";
        for (int i = 1; i < wordArr.length; i++) {
            wordLike += " and word like '%" + StringEscapeUtils.escapeSql(wordArr[i]) + "%' ";
        }
        String whereQuery = "("
                + wordLike
                + ") and pv >= ? and pv <= ? and click >= ? and click <= ? and price >= ? and price <= ? and competition >= ? and competition <= ? limit ? offset ?";

        return fetch(whereQuery, minPv, maxPv, minClick, maxClick, minPrice, maxPrice, minCompetition, maxCompetition,
                po.getPs(), po.getOffset());
    }

    public static long countQueryList(String[] wordArr, int minPv, int maxPv, int minClick, int maxClick, int minPrice,
            int maxPrice, int minCompetition, int maxCompetition) {
        if (wordArr.length <= 0) {
            return 0;
        }

        String wordLike = "word like '%" + StringEscapeUtils.escapeSql(wordArr[0]) + "%' ";
        for (int i = 1; i < wordArr.length; i++) {
            wordLike += " and word like '%" + StringEscapeUtils.escapeSql(wordArr[i]) + "%' ";
        }
        String whereQuery = "("
                + wordLike
                + ") and pv >= ? and pv <= ? and click >= ? and click <= ? and price >= ? and price <= ? and competition >= ? and competition <= ? ";

        return JDBCBuilder.singleLongQuery(src, TMCWordBase.SELECT_COUNT_QUERY + whereQuery, minPv, maxPv, minClick,
                maxClick, minPrice, maxPrice, minCompetition, maxCompetition);
    }

    public static List<TMCWordBase> queryListFullText(String[] wordArr, int minPv, int maxPv, int minClick,
            int maxClick,
            int minPrice, int maxPrice, int minCompetition, int maxCompetition, PageOffset po) {
        if (wordArr.length <= 0) {
            return null;
        }
        String wordLike = " match(word) against ('" + StringEscapeUtils.escapeSql(wordArr[0]) + "') ";
        for (int i = 1; i < wordArr.length; i++) {
            wordLike += " and match(word) against ('" + StringEscapeUtils.escapeSql(wordArr[i]) + "') ";
        }
        String whereQuery = "("
                + wordLike
                + ") and pv >= ? and pv <= ? and click >= ? and click <= ? and price >= ? and price <= ? and competition >= ? and competition <= ? limit ? offset ?";

        return fetch(whereQuery, minPv, maxPv, minClick, maxClick, minPrice, maxPrice, minCompetition, maxCompetition,
                po.getPs(), po.getOffset());
    }

    public static long countQueryListFullText(String[] wordArr, int minPv, int maxPv, int minClick, int maxClick,
            int minPrice,
            int maxPrice, int minCompetition, int maxCompetition) {
        if (wordArr.length <= 0) {
            return 0;
        }

        String wordLike = " match(word) against ('" + StringEscapeUtils.escapeSql(wordArr[0]) + "') ";
        for (int i = 1; i < wordArr.length; i++) {
            wordLike += " and match(word) against ('" + StringEscapeUtils.escapeSql(wordArr[i]) + "') ";
        }

        String whereQuery = "("
                + wordLike
                + ") and pv >= ? and pv <= ? and click >= ? and click <= ? and price >= ? and price <= ? and competition >= ? and competition <= ? ";

        return JDBCBuilder.singleLongQuery(src, TMCWordBase.SELECT_COUNT_QUERY + whereQuery, minPv, maxPv, minClick,
                maxClick, minPrice, maxPrice, minCompetition, maxCompetition);
    }

    public static List<TMCWordBase> fetchAll(PageOffset po) {
        String sql = TMCWordBase.SELECT_SQL + TMCWordBase.TABLE_NAME + " limit ? offset ?";
        return new ListElasticRawWordFetcher(false, sql, po.getPs(), po.getOffset()).call();
    }

    public static class SearchConfigParms {
        String order;

        String desc;

        int pn;

        int ps;

        String wordList;

        public SearchConfigParms(String order, String desc, int pn, int ps, String wordList) {
            super();
            this.order = order;
            this.desc = desc;
            this.pn = pn;
            this.ps = ps;
            this.wordList = wordList;
        }

        public SearchConfigParms() {
            super();
        }

        public static SearchConfigParms fromLine(String line) {
            SearchConfigParms params = new SearchConfigParms();
            String[] split = StringUtils.split(line, "_");
//            log.info("[splits]"+ArrayUtils.toString(split));
            params.order = split[0];
            params.desc = split[1];
            params.pn = NumberUtil.parserInt(split[2], 1);
            params.ps = NumberUtil.parserInt(split[3], 20);
            params.wordList = split[4];
            return params;
        }

        public void doMustRefresh() {
            List<String> splits = new ArrayList(Arrays.asList(StringUtils.split(wordList, ',')));
            PageOffset po = new PageOffset(pn, ps);
            doCacheWord(splits, order, desc, po, true);
        }
    }

    public static String genFetchkey(String wordList, String order, String desc, int pn, int ps) {
        String key = order + "_" + desc + "_" + pn + "_" + ps + "_" + wordList + "_tmccache";
        if (key.length() > 255) {
            key = key.substring(0, 255);
        }
        return key;
    }

    @JsonIgnore
    public static String indexName = "txmtaocinew";

    @JsonIgnore
    public static String indexType = "txmtaocitypenew";

    public static TMResult doESSearch(List<String> wordList, int pn, int ps, String order, String desc) {
        return doESSearch(wordList, pn, ps, order, desc, false);
    }

    public static TMResult doESSearch(List<String> wordList, int pn, int ps, String order, String desc,
            boolean mustRefresh) {

        log.info(format("doESSearch:wordList, pn, ps, order, desc".replaceAll(", ", "=%s, ") + "=%s", wordList, pn, ps,
                order, desc));

        String key = genFetchkey(StringUtils.join(wordList, ','), order, desc, pn, ps);
        log.info("[key:]" + key);

        TMResult res = null;
        if (!mustRefresh) {
            res = (TMResult) Cache.get(key);
            if (res != null) {
                return res;
            }
        }

        try {
            String[] arr = wordList.toArray(new String[] {});
            log.info("[keys :]" + wordList + " with arr:" + arr);

            SearchParams params = new SearchParams(true, true, true, order, desc, TMCWordBase.indexName);

            TermSearchApi api = new TermSearchApi(SearchManager.getIntance().getClient(), arr, pn, ps, params);

            SearchRes call = api.call();
            res = new TMResult(call.getList(), (int) call.getTotalHits(), new PageOffset(pn, ps));
//            log.info("[call list:]"+call.getList());
            if (res != null && res.isOk) {
                Cache.set(key, res, "1d");
            }
            return res;
        } catch (ClientException e) {
            log.warn(e.getMessage(), e);
        }
        return null;
    }

    public static TMResult doCacheWord(List<String> wordList, String order, String desc, PageOffset po,
            boolean mustRefresh) {
//            log.info(format("doCacheWord:wordList, order, desc, po".replaceAll(", ", "=%s, ") + "=%s", wordList, order,
//                    desc, po));

        String key = genFetchkey(StringUtils.join(wordList, ','), order, desc, po.getPn(), po.getPs());

        TMResult res = null;
        if (!mustRefresh) {
            res = (TMResult) Cache.get(key);
            if (res != null) {
                return res;
            }
        }

        res = searchWord(wordList, order, desc, po);
        if (res != null && res.isOk) {
            Cache.set(key, res, "1d");
        }

        return res;
    }

    private static TMResult searchWord(List<String> wordList, String order, String desc, PageOffset po) {
        if (CommonUtils.isEmpty(wordList)) {
            return TMResult.failMsg("wordList null");
        }

        StringBuilder sb = new StringBuilder();
        sb.append(TMCWordBase.SELECT_QUERY);
        sb.append(" (word like '%" + StringEscapeUtils.escapeSql(wordList.get(0)) + "%') ");
        for (int i = 1; i < wordList.size(); i++) {
            if (!StringUtils.isBlank(wordList.get(i))) {
                sb.append(" and (word like '%" + StringEscapeUtils.escapeSql(wordList.get(i)) + "%') ");
            }
        }
        sb.append(" order by " + StringEscapeUtils.escapeSql(order) + " " + StringEscapeUtils.escapeSql(desc));
        sb.append(" limit ? offset ?");
        List<TMCWordBase> list = new ListElasticRawWordFetcher(false, sb.toString(), po.getPs(), po.getOffset()).call();
        int count = (int) countWord(wordList);
        return new TMResult(list, count, po);
    }

    public static TMResult searchWordFullText(List<String> wordList, String order, String desc, PageOffset po) {
        if (CommonUtils.isEmpty(wordList)) {
            return TMResult.failMsg("wordList null");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(TMCWordBase.SELECT_QUERY);
        sb.append(" match(word) against ('" + StringEscapeUtils.escapeSql(wordList.get(0)) + "') ");
        for (int i = 1; i < wordList.size(); i++) {
            if (!StringUtils.isBlank(wordList.get(i))) {
                sb.append(" and match(word) against ('" + StringEscapeUtils.escapeSql(wordList.get(i)) + "') ");
            }
        }
        sb.append(" order by " + StringEscapeUtils.escapeSql(order) + " " + StringEscapeUtils.escapeSql(desc));
        sb.append(" limit ? offset ?");
        List<TMCWordBase> list = new ListElasticRawWordFetcher(false, sb.toString(), po.getPs(), po.getOffset()).call();
        int count = (int) countWordFullText(wordList);
        return new TMResult(list, count, po);
    }

    public static long countWord(List<String> wordList) {
        if (CommonUtils.isEmpty(wordList)) {
            return 0;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(TMCWordBase.SELECT_COUNT_QUERY);
        sb.append(" (word like '%" + StringEscapeUtils.escapeSql(wordList.get(0)) + "%') ");
        for (int i = 1; i < wordList.size(); i++) {
            sb.append(" and (word like '%" + StringEscapeUtils.escapeSql(wordList.get(i)) + "%') ");
        }
        return JDBCBuilder.singleLongQuery(src, sb.toString());
    }

    public static long countWordFullText(List<String> wordList) {
        if (CommonUtils.isEmpty(wordList)) {
            return 0;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(TMCWordBase.SELECT_COUNT_QUERY);
        sb.append(" match(word) against ('" + StringEscapeUtils.escapeSql(wordList.get(0)) + "') ");
        for (int i = 1; i < wordList.size(); i++) {
            if (!StringUtils.isBlank(wordList.get(i))) {
                sb.append(" and match(word) against ('" + StringEscapeUtils.escapeSql(wordList.get(i)) + "') ");
            }
        }
        return JDBCBuilder.singleLongQuery(src, sb.toString());
    }

    public static List<TMCWordBase> fetchEqualWord(List<String> wordList) {
        if (CommonUtils.isEmpty(wordList)) {
            return null;
        }
        List<String> escapList = new ArrayList<String>();
        for (String word : wordList) {
            if (!StringUtils.isBlank(word)) {
                escapList.add(StringEscapeUtils.escapeSql(word.trim()));
            }
        }
        String words = StringUtils.join(escapList, "', '");
        StringBuilder sb = new StringBuilder();
        sb.append(TMCWordBase.SELECT_QUERY);
        sb.append(" word in ('" + words + "') ");
        List<TMCWordBase> list = new ListElasticRawWordFetcher(false, sb.toString()).call();
        return list;
    }

    static class ListElasticRawWordFetcher extends JDBCExecutor<List<TMCWordBase>> {

        public ListElasticRawWordFetcher(boolean debug, String query, Object... params) {
            super(query, params);
            this.src = TMCWordBase.src;
        }

        @Override
        public List<TMCWordBase> doWithResultSet(ResultSet rs) throws SQLException {
            final List<TMCWordBase> list = new ArrayList<TMCWordBase>();
            while (rs.next()) {
                TMCWordBase model = new TMCWordBase();
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

    public static TMCWordBase findByWord(String word) {
        if (StringUtils.isBlank(word)) {
            return null;
        }
        return NumberUtil.first(fetch(" word = ?", StringEscapeUtils.escapeSql(word)));
    }

    public static TMCWordBase findOrCreateByWord(String word) throws ClientException {
        if (StringUtils.isBlank(word)) {
            return null;
        }

        TMCWordBase first = NumberUtil.first(fetch(" word = ?", word));
        if (first != null) {
            return first;
        }

        Long execute = new WidAPIs.GetIdByWord(word).execute();
        first = new TMCWordBase();
        first.setId(execute);
        first.setWord(word);
        log.info("[first : ]" + first);
        first.rawInsert();
        return first;
    }

    public static TMCWordBase findOrCreateByWord(Long id, String word) throws ClientException {
        if (StringUtils.isBlank(word)) {
            return null;
        }

        TMCWordBase first = NumberUtil.first(fetch(" word = ?", word));
        if (first != null) {
            return first;
        }

        first = new TMCWordBase();
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
                    TMCWordBase.findOrCreateByWord(entry.getValue(), entry.getKey());
                } catch (ClientException e) {
                    log.warn(e.getMessage(), e);

                }
            }
        }.call();

    }

    public static void update(int offset, int limit) throws ClientException {
        List<TMCWordBase> next = null;
        while (!CommonUtils.isEmpty((next = fetch(" 1 = 1 limit ? offset ?", limit, offset)))) {
            log.info("[current offset :]" + offset);
            List<String> words = new ArrayList<String>();
            for (TMCWordBase wordBase : next) {
                words.add(wordBase.getWord());
            }
            Map<String, IWordBase> execute = new WidAPIs.WordBaseAPI(words).execute();
            for (TMCWordBase wordBase : next) {
                IWordBase iWordBase = execute.get(wordBase.getWord());
                if (iWordBase == null) {
                    continue;
                }

                wordBase.updateByWordBaseBean(iWordBase);
                wordBase.rawUpdate();
            }

            offset += limit;
            log.info("[offset :]" + offset);
        }
    }

    /**
     * 普通查询
     */
    public static List<TMCWordBase> norsearch(String[] args, int pn, int ps) {
        String condition = dealNormalCondition(args);

        return search(condition, pn, ps);
    }

    public static String dealNormalCondition(String[] args) {
        StringBuilder sb = new StringBuilder();
        if (args.length == 1) {
            sb.append(" word like '%" + StringEscapeUtils.escapeSql(args[0]) + "%' ");
        } else if (args.length >= 2) {
            sb.append(" ( ");
            for (String string : args) {
                sb.append(" word like '%" + StringEscapeUtils.escapeSql(args[0]) + "%' ");
                if (!string.equals(args[args.length - 1])) {
                    sb.append(" or ");
                }
            }
            sb.append(" )");
        }
        return sb.toString();
    }

    public static int getCountOfNormal(String[] args) {
        String sql = dealNormalCondition(args);
        return (int) TMCWordBase.count(sql);
    }

    /**
     * 查找一个词通过id
     */
    public static TMCWordBase searchAWord(Long wordId) {
        StringBuilder sb = new StringBuilder();
        sb.append("id = ");
        sb.append(wordId);
        List<TMCWordBase> word = TMCWordBase.fetch(sb.toString());

        return word.get(0);
    }

    public static List<TMCWordBase> fetchMultipleAndLike(String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append(" 1 = 1");
        for (String string : args) {
            sb.append(" and ");
            sb.append(" word like '%");
            sb.append(StringEscapeUtils.escapeSql(string));
            sb.append("%'");
        }
        return fetch(sb.toString());
    }

    public static int getTotalHits(List<List<String>> conditions) {
        int count = 0;
        count = fetch(dealCondition(conditions)).size();
        return count;
    }

    public static List<TMCWordBase> superSearch(List<List<String>> conditions, int pn, int ps) {
        return search(dealCondition(conditions), pn, ps);
    }

    public static String dealCondition(List<List<String>> conditions) {
        List<String> baohanall = conditions.get(0);
        List<String> baohanrengyi = conditions.get(1);
        List<String> buhanrengyi = conditions.get(2);
        StringBuilder sb = new StringBuilder();
        if (baohanall.isEmpty() == false) {
            sb.append("(word like '%" + StringEscapeUtils.escapeSql(baohanall.get(0)) + "%')");
            if (baohanrengyi.isEmpty() == false && buhanrengyi.isEmpty() == true) {
                if (baohanrengyi.size() == 1) {
                    sb.append("and ( word like '%" + StringEscapeUtils.escapeSql(baohanrengyi.get(0)) + "%')");
                } else if (baohanrengyi.size() == 2) {
                    sb.append("and ( word like '%" + StringEscapeUtils.escapeSql(baohanrengyi.get(0)) + "%' or ");
                    sb.append(" word like '%" + StringEscapeUtils.escapeSql(baohanrengyi.get(1)) + "%') ");
                } else if (baohanrengyi.size() == 3) {
                    sb.append("and ( word like '%" + StringEscapeUtils.escapeSql(baohanrengyi.get(0)) + "%' or ");
                    sb.append(" word like '%" + StringEscapeUtils.escapeSql(baohanrengyi.get(1)) + "%'or ");
                    sb.append(" word like '%" + StringEscapeUtils.escapeSql(baohanrengyi.get(2)) + "%')");
                }
            } else if (baohanrengyi.isEmpty() == true && buhanrengyi.isEmpty() == false) {
                if (buhanrengyi.size() == 1) {
                    sb.append("and ( word not like '%" + StringEscapeUtils.escapeSql(buhanrengyi.get(0)) + "%')");
                } else if (buhanrengyi.size() == 2) {
                    sb.append("and ( word not like '%" + StringEscapeUtils.escapeSql(buhanrengyi.get(0)) + "%' and ");
                    sb.append(" word not like '%" + StringEscapeUtils.escapeSql(buhanrengyi.get(1)) + "%') ");
                } else if (buhanrengyi.size() == 3) {
                    sb.append("and ( word not like '%" + StringEscapeUtils.escapeSql(buhanrengyi.get(0)) + "%' and ");
                    sb.append(" word not like '%" + StringEscapeUtils.escapeSql(buhanrengyi.get(1)) + "%'and ");
                    sb.append(" word not like '%" + StringEscapeUtils.escapeSql(buhanrengyi.get(2)) + "%')");
                }
            } else if (baohanrengyi.isEmpty() == false && buhanrengyi.isEmpty() == false) {
                if (baohanrengyi.size() == 1) {
                    sb.append("and ( word like '%" + StringEscapeUtils.escapeSql(baohanrengyi.get(0)) + "%')");
                } else if (baohanrengyi.size() == 2) {
                    sb.append("and ( word like '%" + StringEscapeUtils.escapeSql(baohanrengyi.get(0)) + "%' and ");
                    sb.append("word like '%" + StringEscapeUtils.escapeSql(baohanrengyi.get(1)) + "%')");
                } else if (baohanrengyi.size() == 3) {
                    sb.append("and ( word like '%" + StringEscapeUtils.escapeSql(baohanrengyi.get(0)) + "%' and ");
                    sb.append(" word like '%" + StringEscapeUtils.escapeSql(baohanrengyi.get(1)) + "%' and ");
                    sb.append(" word like '%" + StringEscapeUtils.escapeSql(baohanrengyi.get(2)) + "%' )");
                }
                if (buhanrengyi.size() == 1) {
                    sb.append("and (word not like '%" + StringEscapeUtils.escapeSql(buhanrengyi.get(0)) + "%')");
                } else if (buhanrengyi.size() == 2) {
                    sb.append("and ( word not like '%" + StringEscapeUtils.escapeSql(buhanrengyi.get(0)) + "%' and");
                    sb.append(" word not like '%" + StringEscapeUtils.escapeSql(buhanrengyi.get(1)) + "%' )");
                } else if (buhanrengyi.size() == 3) {
                    sb.append("and ( word not like '%" + StringEscapeUtils.escapeSql(buhanrengyi.get(0)) + "%' and");
                    sb.append(" word not like '%" + StringEscapeUtils.escapeSql(buhanrengyi.get(1)) + "%' ");
                    sb.append(" word not like '%" + StringEscapeUtils.escapeSql(buhanrengyi.get(2)) + "%' )");
                }
            }

        }
        return sb.toString();
    }

    @Override
    public String getTableName() {
        // TODO Auto-generated method stub
        return TABLE_NAME;
    }

    @Override
    public String getIdColumn() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getIdName() {
        // TODO Auto-generated method stub
        return "id";
    }

    @Override
    public boolean jdbcSave() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public float getMatch() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setMatch(float match) {
    }

    @Override
    public void updateByINInfo(INRecordBase record) {
    }

    @Override
    public String toString() {
        return "WordBase [id=" + id + ", word=" + word + ", price=" + price + ", click=" + click + ", competition="
                + competition + ", cid=" + cid + ", pv=" + pv + ", strikeFocus=" + strikeFocus + ", searchFocus="
                + searchFocus + ", ctr=" + ctr + ", totalPayed=" + totalPayed + ", lastINWordUpdate="
                + lastINWordUpdate + ", score=" + score + ", scount=" + scount + ", status=" + status + "]";
    }

    @Override
    public String getTableHashKey() {
        return null;
    }

//    public static void main(String[] args){
//        readFetchLineParams(new File("/home/zrb/code/wordtook.txt"));
//    }

    /**
     * [2014-03-20 08:04:02,618] INFO  [WaitLock]  controllers.TMController.endTime(TMController.java:56) - 
     * Action [/words/fetch?words=%E8%B7%AF%E7%94%B1%E5%99%A8&pn=1&ps=20&order=pv&desc=desc] took 1 ms
     */
    public static void readFetchLineParams(File inputFile, File outputFile) {
        Set<String> keys = new HashSet<String>();
        String start = "words=";
        String end = "] took";
        try {
            List<String> lines = FileUtils.readLines(inputFile);
            for (String line : lines) {
                int startIndex = line.indexOf(start);
                if (startIndex < 0) {
                    continue;
                }
                int endIndex = line.indexOf(end, startIndex);
                if (endIndex < 0) {
                    continue;
                }

                String paramStr = line.substring(startIndex, endIndex);
//                log.info("[line:]" + paramStr);
//                Map<String, String[]> map = new TextParser().parse(new ByteArrayInputStream(paramStr
//                        .getBytes("utf-8")));
                List<NameValuePair> params = URLEncodedUtils.parse(paramStr, Charset.forName("utf-8"));
                Map<String, String> p = new HashMap<String, String>();

                for (NameValuePair nameValuePair : params) {
                    p.put(nameValuePair.getName(), nameValuePair.getValue());
                }

                String words = p.get("words");
                String order = p.get("order");
                String desc = p.get("desc");
                int pn = NumberUtil.parserInt(p.get("pn"), 1);
                int ps = NumberUtil.parserInt(p.get("ps"), 1);

                if (StringUtils.isBlank(words)) {
                    continue;
                }

                String key = genFetchkey(words, order, desc, pn, ps);
//                log.info("[form key:]" + key + " from line :" + line);
                keys.add(key);
//                return;
            }
            lines.clear();
            FileUtils.writeLines(outputFile, keys);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    public static void updatePriceAndPv(String word, int pv, int price){
        TMCWordBase wordBase = TMCWordBase.findByWord(word);
        if(wordBase == null){
            wordBase = new TMCWordBase();
            wordBase.setWord(word);
            wordBase.setPv(pv);
            wordBase.setPrice(price);
            wordBase.setCompetition(-1);
            wordBase.rawInsert();
            return;
        }
        Integer wordBasePrice = wordBase.getPrice();
        Integer wordBasePv = wordBase.getPv();
        if(wordBasePrice == null || wordBasePrice == 0){
            wordBase.setPrice(price);
        }
        if(wordBasePv == null || wordBasePv == 0){
            wordBase.setPv(pv);
        }
        wordBase.rawUpdate();
    }
    
}
