
package models.word;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

import models.mysql.word.WordBase;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.data.validation.Unique;
import play.db.jpa.GenericModel;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder.JDBCExecutor;
import utils.DateUtil;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.IWordBase;
import com.ciaosir.client.utils.NumberUtil;
import com.taobao.api.domain.INRecordBase;
import com.taobao.api.domain.INWordBase;

/**
 * {avgPrice=47, click=23, competition=435, ctr=0.19, date=1343318400000, pv=11076}
 *
 * @author zhourunbo
 *
 */

@Entity(name = ElasticRawWord.TABLE_NAME)
@JsonIgnoreProperties(value = {
        "userId", "entityId", "idColumn", "idName", "persistent", "tableHashKey", "tableName", "lastINWordUpdate",
        "ctr", "totalPayed"
})
public class ElasticRawWord extends GenericModel implements PolicySQLGenerator, IWordBase {

    public static final Logger log = LoggerFactory.getLogger(ElasticRawWord.class);

    @JsonIgnore
    public static final String TAG = "ElasticRawWord";

    @JsonIgnore
    public static String indexName = "devsearch";

    @JsonIgnore
    public static String indexType = "devsearchtype";

//    @ElasticSearchIgnore
    @Id
    @GeneratedValue
    public Long id;

    public Long getId() {
        return id;
    }

    public ElasticRawWord() {
    }

//    @ElasticSearchIgnore
    @JsonIgnore
    public static final String TABLE_NAME = "searchkey_";

    @Index(name = "word")
    @Column(columnDefinition = "varchar(127) default '' not null")
    @Unique
    @CodeNoUpdate
    public String word;

    @Column(columnDefinition = "int default -1")
    public Integer price;

    @Column(columnDefinition = "int default -1")
    public Integer click;

    @Column(columnDefinition = "int default -1")
    public Integer competition;

    @Column(columnDefinition = "int default -1")
    public Integer cid;

    @Column(columnDefinition = "int default -1")
    public Integer pv;

    /**
     * 成交指数
     */
    @Column(columnDefinition = "int default -1")
    public Integer strikeFocus;

    /**
     * 搜索指数
     */
    @Column(columnDefinition = "int default -1")
    public Integer searchFocus;

    @JsonIgnore
    @Column(columnDefinition = "int default -1")
    public Integer ctr;

    @Column(columnDefinition = "int default -1")
    public Long totalPayed;

    @Column(columnDefinition = "bigint default -1")
    public Long lastINWordUpdate;

    @Column(columnDefinition = "bigint default -1")
    public long lastRelatedUpdate = 0L;

    /**
     * 转化率
     */
    @Column(columnDefinition = "int default -1")
    public Integer score = NumberUtil.DEFAULT_ZERO;

    @Column(columnDefinition = "int default -1")
    public Integer scount;

    @Column(columnDefinition = "int default 0")
    public Integer status;

    static Map<String, ElasticRawWord> wordCache = new HashMap<String, ElasticRawWord>();

    static Map<Long, ElasticRawWord> idCache = new HashMap<Long, ElasticRawWord>();

    public ElasticRawWord(String word) {
        super();
        this.word = word;
    }

    public ElasticRawWord(WordBase base) {
        super();
        this.id = base.getId();
        this.word = base.getWord();
        this.price = base.getPrice();
        this.click = base.getClick();
        this.competition = base.getCompetition();
        this.cid = base.getCid();
        this.pv = base.getPv();
        // this.strikeFocus = base.getStrikeFocus();
        if (this.pv > 0) {
            this.strikeFocus = this.click * 10000 / this.pv;
        }
        this.searchFocus = base.getSearchFocus();
        // this.totalPayed = ...
        // this.lastINWordUpdate = ...
        if (base.getScount() > 0) {
            this.scount = base.getScount();
            this.score = base.getPv() * 10000 / this.scount;
        }
        this.status = base.getStatus();
    }

    public ElasticRawWord(IWordBase base) {
        super();
        //this.id = base.getId();
        this.word = base.getWord();
        this.price = base.getPrice();
        this.click = base.getClick();
        this.competition = base.getCompetition();
        this.cid = base.getCid();
        this.pv = base.getPv();
        // this.strikeFocus = base.getStrikeFocus();
        if (this.pv > 0) {
            this.strikeFocus = this.click * 10000 / this.pv;
        }
        this.searchFocus = base.getSearchFocus();
        // this.totalPayed = ...
        // this.lastINWordUpdate = ...
        if (base.getScount() > 0) {
            this.scount = base.getScount();
            this.score = base.getPv() * 10000 / this.scount;
        }
        this.status = base.getStatus();
        if (base.getId() != null) {
            this.id = base.getId();
        }
    }

    public ElasticRawWord(Long value, String key) {
        this.id = value;
        this.word = key;
    }

    @Override
    public String toString() {
        return "ElasticRawWord [id=" + id + ", word=" + word + ", price=" + price + ", click=" + click
                + ", competition=" + competition + ", cid=" + cid + ", pv=" + pv + ", ctr=" + ctr + ", score=" + score
                + ", status=" + status + "]";
    }

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

    public static final String SELECT_QUERY = "select id, word, price, click, competition, pv, strikeFocus, searchFocus,"
            + " lastINWordUpdate, score ,status, scount, cid from " + TABLE_NAME + " where ";

    public static ElasticRawWord EMPTY = new ElasticRawWord();

    static String INSERT_ONE_SQL = "insert into `searchkey_`(`word`,`price`,`click`,`competition`,"
            + "`pv`,`strikeFocus`,`searchFocus`,`lastINWordUpdate`,`score`,`status`,`scount`,`cid`)" +
            " values(?,?,?,?,?,?,?,?,?,?,?,?)";

    @Transient
    public static DBDispatcher rawwordDispatcher = new DBDispatcher(DataSrc.BASIC, EMPTY);

    public boolean rawInsert() {
        long id = rawwordDispatcher.insert(INSERT_ONE_SQL, this.word, this.price, this.click, this.competition,
                this.pv, this.strikeFocus, this.searchFocus, this.lastINWordUpdate, this.score, this.status,
                this.scount, this.cid);

        if (id >= 0L) {
            this.setId(id);
            return true;
        } else {
            log.warn("Insert Failes.....");
            return false;
        }
    }

    static String UPDATE_QUERY_SQL = "update `searchkey_` set  `price` = ?, `click` = ?, "
            + "`competition` = ?, `pv` = ?, `strikeFocus` = ?, `searchFocus` = ?, "
            + "`lastINWordUpdate` = ?, `score` = ?, `status` = ?, `scount` = ?, `cid` = ?,`word`=? where `id` = ? ";

    public boolean rawUpdate() {
        long updateNum = rawwordDispatcher.insert(UPDATE_QUERY_SQL, this.price, this.click, this.competition, this.pv,
                this.strikeFocus, this.searchFocus, this.lastINWordUpdate, this.score, this.status, this.scount,
                this.cid, this.word, this.getId());

        return updateNum > 0L;
    }

    /**
     * No Update, You know it...
     */
    @Override
    public boolean jdbcSave() {
        long existId = rawwordDispatcher.singleLongQuery(" select id from " + TABLE_NAME + " where id = ?", id);

        if (existId <= 0L) {
            return rawInsert();
        } else {
            return rawUpdate();
        }
    }

//
//    @Override
//    public void _save() {
//        this.jdbcSave();
//    }

    @Override
    public String getIdName() {
        return "id";
    }

    @Override
    public String getTableHashKey() {
        return null;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getScount() {
        return scount;
    }

    public void setScount(Integer scount) {
        this.scount = scount;
    }

    public Integer getCid() {
        return cid;
    }

    public void setCid(Integer cid) {
        this.cid = cid;
    }

    private void setScount() {
        if (this.getClick() > 1) {
            if (this.getPv() > 10000000) {
                this.scount = this.getClick() / (this.getPv() / 10000);
            } else {
                this.scount = this.getClick() * 10000 / (this.getPv());
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((word == null) ? 0 : word.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        ElasticRawWord other = (ElasticRawWord) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (word == null) {
            if (other.word != null)
                return false;
        } else if (!word.equals(other.word))
            return false;
        return true;
    }

    public static Comparator<ElasticRawWord> PVComparator = new Comparator<ElasticRawWord>() {
        @Override
        public int compare(ElasticRawWord o1, ElasticRawWord o2) {
            return o2.pv - o1.pv;
        }
    };

    public static Comparator<ElasticRawWord> ClickComparator = new Comparator<ElasticRawWord>() {
        @Override
        public int compare(ElasticRawWord o1, ElasticRawWord o2) {
            return o2.click - o1.click;
        }
    };

    public static Comparator<ElasticRawWord> CompetitionComparator = new Comparator<ElasticRawWord>() {
        @Override
        public int compare(ElasticRawWord o1, ElasticRawWord o2) {
            return o2.competition - o1.competition;
        }
    };

    public IndexRequestBuilder parepareClientIndex(Client client) throws IOException {
        XContentBuilder builder = XContentFactory.jsonBuilder();
        XContentBuilder obj = builder.startObject();

        obj.field("word", this.getWord());
        obj.field("price", this.getPrice());
        obj.field("click", this.getClick());
        obj.field("competition", this.getCompetition());
        obj.field("pv", this.getPv());
        obj.field("strikeFocus", this.getStrikeFocus());
        obj.field("searchFocus", this.getSearchFocus());
        obj.field("status", this.getStrikeFocus());
        obj.field("score", this.getScore());
        obj.field("scount", this.getScount());
        obj.field("cid", this.getCid());

        obj.endObject();
        log.info("[id:]" + this.getId());
        return client.prepareIndex(indexName, indexType, this.getId().toString()).setSource(obj);
    }

    @Override
    public Integer getCompetition() {
        return this.competition;
    }

    @Override
    public void setCompetition(Integer compettion) {
        this.competition = compettion;
    }

//    static String QUERY_FIND_PV_UPDATE_YET = "select id,word,price,click,competition,cid,pv,"
//            + "strikeFocus,searchFocus,ctr,totalPayed,lastINWordUpdate,score,scount,status from "
//            + TABLE_NAME + "where status & " + WordStatus.PV_UPDATE_YET + " > 0 and ";

    public void fetch(ResultSet rs) throws SQLException {
        this.id = rs.getLong(1);
        this.word = rs.getString(2);
        this.price = rs.getInt(3);
        this.click = rs.getInt(4);
        this.competition = rs.getInt(5);
        this.cid = rs.getInt(6);
        this.pv = rs.getInt(7);
        this.strikeFocus = rs.getInt(8);
        this.searchFocus = rs.getInt(9);
        this.ctr = rs.getInt(10);
        this.totalPayed = rs.getLong(11);
        this.lastINWordUpdate = rs.getLong(12);
        this.score = rs.getInt(13);
        this.scount = rs.getInt(14);
        this.status = rs.getInt(15);
    }

    @Override
    public float getMatch() {
        return 0;
    }

    @Override
    public void setMatch(float match) {
    }

    public void updateByWordBaseBean(IWordBase record) {
        if (record == null) {
            return;
        }

        this.click = NumberUtil.parserInt(record.getClick(), NumberUtil.NONE_EXIST);
        this.pv = NumberUtil.parserInt(record.getPv(), NumberUtil.NONE_EXIST);
        this.competition = NumberUtil.parserInt(record.getCompetition(), NumberUtil.NONE_EXIST);
        this.price = NumberUtil.parserInt(record.getPrice(), NumberUtil.NONE_EXIST);

        setScount();
        if (this.pv > 0) {
            this.ctr = 10000 * this.click / this.pv;
        } else {
            WordStatus.markGarbage(this);
        }
        WordStatus.markPvUpdate(this);
        WordStatus.markDirty(this);
    }

    public void updateFocus(int searchFocus2, int strikeFocus2) {
        this.searchFocus = searchFocus2;
        this.strikeFocus = strikeFocus2;
        this.score = this.strikeFocus == 0 ? 0 : (int) (10000L * (long) this.searchFocus / (long) this.strikeFocus);
        if (this.searchFocus > 0) {
            WordStatus.markDirty(this);
        }
    }

    /**
     *  update searchkey_ set status = status & 31, scount = click * 10000 / pv where scount <= 0 and click > 0 and pv < 10000000 limit 50000;
     *  update searchkey_ set status = status & 31, scount = click / (pv/10000) where scount <= 0 and click > 0 and pv >= 10000000 limit 50000;
     * @param record
     */
    @Override
    public void updateByINInfo(INRecordBase record) {
        if (record == null) {
            return;
        }
        // TODO Auto-generated method stub
        this.pv = NumberUtil.parserInt(record.getPv(), NumberUtil.NONE_EXIST);
        this.click = NumberUtil.parserInt(record.getClick(), NumberUtil.NONE_EXIST);
        this.competition = NumberUtil.parserInt(record.getCompetition(), NumberUtil.NONE_EXIST);
        this.price = NumberUtil.parserInt(record.getAvgPrice(), NumberUtil.NONE_EXIST);
        this.lastINWordUpdate = DateUtil.formCurrDate();
        this.totalPayed = record.getClickPriceSum().longValue();

        if (record.getClick() > 0) {
            this.price = (int) (record.getClickPriceSum() / record.getClick());
        }
        if (this.getPv() != null && this.getPv() > 1) {
            this.ctr = 10000 * this.click / this.pv;
            WordStatus.markNotGarbage(this);
        } else {
            WordStatus.markGarbage(this);
        }

        setScount();

        WordStatus.markPvUpdate(this);
        WordStatus.markDirty(this);
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

    public static ElasticRawWord createByINWordBase(String word, List<INRecordBase> bases) {
        ElasticRawWord elasticWord = new ElasticRawWord(word);
        if (!CommonUtils.isEmpty(bases)) {
            elasticWord.updateByINInfo(NumberUtil.sum(bases));
        }
        return elasticWord;
    }

    public static ElasticRawWord createByINWordBase(INWordBase base) {
        ElasticRawWord elasticWord = new ElasticRawWord(base.getWord());
        INRecordBase summed = NumberUtil.sum(base.getInRecordBaseList());
//        log.info("[sumed :]" + summed);
        elasticWord.updateByINInfo(summed);
        return elasticWord;
    }

    @JsonIgnore
    public void setINInfoNotProvided() {
        this.price = NumberUtil.NONE_EXIST;
        this.pv = NumberUtil.NONE_EXIST;
        this.competition = NumberUtil.NONE_EXIST;
        this.click = NumberUtil.NONE_EXIST;
        this.lastINWordUpdate = utils.DateUtil.formCurrDate();
        WordStatus.markPvUpdate(this);
        WordStatus.markGarbage(this);
        WordStatus.markDirty(this);
    }

    static List<ElasticRawWord> fetch(String whereQuery, Object... args) {
        return new ListElasticRawWordFetcher(false, ElasticRawWord.SELECT_QUERY + whereQuery, args).call();
    }

    static class ListElasticRawWordFetcher extends JDBCExecutor<List<ElasticRawWord>> {

        public ListElasticRawWordFetcher(boolean debug, String query, Object... params) {
            super(rawwordDispatcher, query, params);
        }

        @Override
        public List<ElasticRawWord> doWithResultSet(ResultSet rs) throws SQLException {
            final List<ElasticRawWord> list = new ArrayList<ElasticRawWord>();
            while (rs.next()) {
                ElasticRawWord model = new ElasticRawWord();
                model.fetchFromResultSet(rs);
                list.add(model);
            }
            return list;
        }
    }

    public static ElasticRawWord findOrCreate(String word) {
        ElasticRawWord rawWord = wordCache.get(word);
        if (rawWord != null) {
            return rawWord;
        }

        rawWord = ElasticRawWord.findByWord(word);
        if (rawWord == null) {
            rawWord = new ElasticRawWord(word);
            rawWord.jdbcSave();
        }

        if (wordCache.size() > 1000000) {
            wordCache.clear();
        }

        wordCache.put(word, rawWord);
        idCache.put(rawWord.getId(), rawWord);
        return rawWord;
    }

    public static ElasticRawWord findByWord(String word) {
        if (StringUtils.isEmpty(word)) {
            return null;
        }

        return NumberUtil.first(fetch(" word = ? ", word));
    }

}
