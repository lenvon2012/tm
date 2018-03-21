
package models.word.top;

import static java.lang.String.format;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import play.jobs.Job;
import pojo.webpage.top.TopHotKeyItem;
import result.TMPaginger;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.api.WidAPIs;
import com.ciaosir.client.pojo.IWordBase;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.NumberUtil;
import com.ciaosir.commons.ClientException;

/**
 * 暂时不更新Topkey表，使用topkey_bak的数据
 * @author lzl
 *
 */
@Entity(name = TopKey.TABLE_NAME)
@JsonIgnoreProperties(value = {
        "lastUpdateTime", "persistent", "entityId", "id", "score", "cid3", "cid2", "topUrlId", "topUrlBaseId"
})
public class TopKey extends Model implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(TopKey.class);

    public static final String TAG = "TopKey";

    public static final String TABLE_NAME = "topkey_bak";

    private static TopKey _instance = new TopKey();

    static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, _instance);

    public static DBDispatcher getDp() {
        return dp;
    }

    public static void setDp(DBDispatcher dp) {
        TopKey.dp = dp;
    }

    public static TopKey getInstance() {
        return _instance;
    }

    //    @EmbeddedId
//    public TextUrlId id;
    @Index(name = "text")
    public String text;

//    @Index(name = "topUrlId")
    public Long topUrlId;

//    @Id
//    public String text;

    /**
     * 上升名次
     */
    public int upRateRank = 0;

    /**
     * 搜索排名
     */
    public int searchRank = 0;

    /**
     * 搜索指数
     */
    public int focusIndex = 0;

    /**
     * 提升率
     */
    public double rateChange = 0;

    /**
     * 排名变化
     */
    public int rankChange = 0;

    public int cid3 = -1;

    public int cid2 = -1;

    public int score = -1;

    @Index(name = "topUrlBaseId")
    public long topUrlBaseId = 0L;

    public long lastUpdateTime = 0L;

    /**e
     * 展现量
     */
    public int pv;

    /**
     * 点击量
     */
    public int click;

    /**
     * 点击率
     */
//    @Index(name = "ctr")
    public int ctr;

    /**
     * 直通车竞争度
     */
    public int competition;

    public TopKey(String text, Long id) {
        this.text = text;
        this.topUrlBaseId = id;
//        this.id = new TextUrlId(text, urlBase.getId());
    }

//
    public TopKey(TextUrlId textModel) {
        this(textModel.text, textModel.urlModelId);
    }

    public TopKey() {
    }

    public int getFocusIndex() {
        return focusIndex;
    }

    public void setFocusIndex(int focusIndex) {
        this.focusIndex = focusIndex;
    }

    public int getUpRateRank() {
        return upRateRank;
    }

    public void setUpRateRank(int upRateRank) {
        this.upRateRank = upRateRank;
    }

    public int getSearchRank() {
        return searchRank;
    }

    public void setSearchRank(int searchRank) {
        this.searchRank = searchRank;
    }

    public double getRateChange() {
        return rateChange;
    }

    public void setRateChange(double rateChange) {
        this.rateChange = rateChange;
    }

    public int getRankChange() {
        return rankChange;
    }

    public void setRankChange(int rankChange) {
        this.rankChange = rankChange;
    }

    public int getCid3() {
        return cid3;
    }

    public void setCid3(int cid3) {
        this.cid3 = cid3;
    }

    public int getCid2() {
        return cid2;
    }

    public void setCid2(int cid2) {
        this.cid2 = cid2;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Long getTopUrlBaseId() {
        return topUrlBaseId;
    }

    public void setTopUrlBaseId(Long topUrlBaseId) {
        this.topUrlBaseId = topUrlBaseId;
    }

    public Long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getTopUrlId() {
        return topUrlId;
    }

    public void setTopUrlId(Long topUrlId) {
        this.topUrlId = topUrlId;
    }

    public int getPv() {
        return pv;
    }

    public void setPv(int pv) {
        this.pv = pv;
    }

    public int getClick() {
        return click;
    }

    public void setClick(int click) {
        this.click = click;
    }

    public int getCtr() {
        return ctr;
    }

    public void setCtr(int ctr) {
        this.ctr = ctr;
    }

    public int getCompetition() {
        return competition;
    }

    public void setCompetition(int competition) {
        this.competition = competition;
    }

    static Pattern p = Pattern.compile("[^0-9]");

    public static final int extractInt(String src) {
        Matcher matcher = p.matcher(src);
        int value = Integer.parseInt(matcher.replaceAll("").trim());
        return value;
    }

    public static final Integer getIntByClass(String rawValue, String className) {
        int value = extractInt(rawValue);
        if ("rate down".equals(className)) {
            value = -value;
        }

        return value;
    }

    @Embeddable
    public static class TextUrlId implements Serializable {

        @Column(name = "text", nullable = false)
        public String text;

        @Column(name = "urlModelId", nullable = false)
        public Long urlModelId;

        public TextUrlId(String text, Long urlModelId) {
            super();
            this.text = text;
            this.urlModelId = urlModelId;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((text == null) ? 0 : text.hashCode());
            result = prime * result + ((urlModelId == null) ? 0 : urlModelId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            TextUrlId other = (TextUrlId) obj;
            if (text == null) {
                if (other.text != null)
                    return false;
            } else if (!text.equals(other.text))
                return false;
            if (urlModelId == null) {
                if (other.urlModelId != null)
                    return false;
            } else if (!urlModelId.equals(other.urlModelId))
                return false;
            return true;
        }

    }

    public void setCtr() {
        if (this.click <= 0 || this.pv <= 0) {
            return;
        }
        if (this.pv > 1000000) {
            this.ctr = this.click / (this.pv / 10000);
        } else {
            this.ctr = this.click * 10000 / this.pv;
        }
    }

    public static TopKey findOne(Long topUrlBaseId) {
        return NumberUtil.first(fetch("topUrlBaseId = ? ", topUrlBaseId));
    }

    public static TMPaginger findByUrlBaseId(Long topUrlBaseId, int pn, int ps, QueryType qType) {
        PageOffset po = new PageOffset(pn, ps);

        String condition = null;
        switch (qType) {
            case searchhot:
                condition = " topUrlBaseId = ? order by focusIndex desc, rankChange desc";
                break;
            case searchup:
                condition = " topUrlBaseId = ? order by rankChange desc, focusIndex desc";
                break;
            case ctrhot:
            case categoryhot:
                condition = " topUrlBaseId = ? and  searchRank > 0 order by ctr desc ";
                break;
            default:
                break;
        }

        List<TopKey> list = fetch(condition + " limit ? offset ? ", topUrlBaseId, po.getPs(), po.getOffset());
        int count = (int) countJdbc(condition, topUrlBaseId);

        return new TMPaginger(po.getPn(), po.getPs(), count, list);
    }

    public static TMPaginger findByUrlBase(String cat, String cid, int pn, int ps, QueryType qType) {
        TopURLBase base = TopURLBase.findbyCatAndCid(cat, cid);
        if (base == null) {
            return TMPaginger.makeEmptyFail("No List");
        }

        return findByUrlBaseId(base.getId(), pn, ps, qType);
    }

    public enum QueryType {
        searchup, searchhot, categoryhot, ctrhot;

        public static QueryType getType(String text) {
            if ("searchup".equals(text)) {
                return searchup;
            } else if ("searchot".equals(text)) {
                return searchhot;
            } else if ("categoryhot".equals(text)) {
                return categoryhot;
            } else if ("ctrhot".equals(text)) {
                return ctrhot;
            } else {
                return searchhot;
            }
        }
    }

    public static TopKey ensure(TopHotKeyItem item, TopURLBase urlModel, int index, boolean isSearchRank) {

        log.info(format("ensure:item, urlModel, nav, isSearchRank".replaceAll(", ", "=%s, ") + "=%s", item, urlModel,
                index, isSearchRank));

        String text = StringUtils.trim(item.title);
        if (StringUtils.isBlank(text)) {
            return null;
        }

        TextUrlId textModel = new TextUrlId(text, urlModel.getId());
        TopKey key = NumberUtil.first(fetch("text = ? and topUrlBaseId = ?", text, urlModel.getId()));
        if (key == null) {
            key = new TopKey(textModel);
        }

        key.setLastUpdateTime(System.currentTimeMillis());

        key.setRankChange(getIntByClass(item.upGrowRank, item.upGrowRankClass));
        key.setRateChange(getIntByClass(item.upGrowRate, item.upGrowRateClass));

        int focus = extractInt(item.focus);
        key.setFocusIndex(focus);

        if (isSearchRank) {
            key.setSearchRank(index);
        } else {
            key.setUpRateRank(index);
        }

        key.jdbcSave();
        return key;
    }

    public static class TopKeyWordBaseUpdateJob extends Job {
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
        List<TopKey> next = null;
        while (!CommonUtils.isEmpty((next = fetch(" 1 = 1 limit ? offset ? ", limit, offset)))) {
            log.info("[current offset :]" + offset);
            updateWordList(next);

            offset += limit;
        }
    }

    public static void update(long topUrlBaseId, int offset, int limit) throws ClientException {
        List<TopKey> next = null;
        while (!CommonUtils
                .isEmpty((next = fetch(" topUrlBaseId = ? limit ?  offset ? ", topUrlBaseId, offset, limit)))) {
            log.info("[current offset :]" + offset);
            updateWordList(next);

            offset += limit;
        }
    }

    private static void updateWordList(List<TopKey> next) throws ClientException {
        List<String> words = new ArrayList<String>();
        for (TopKey wordBase : next) {
            words.add(wordBase.getWord());
        }
        Map<String, IWordBase> execute = new WidAPIs.WordBaseAPI(words).execute();
        for (TopKey wordBase : next) {
            IWordBase iWordBase = execute.get(wordBase.getWord());
            if (iWordBase != null) {
                wordBase.updateByWordBaseBean(iWordBase);
            }

//            TopKeyUpdateWritter.addMsg(wordBase);
            wordBase.jdbcSave();
        }
    }

    public void updateByWordBaseBean(IWordBase iWordBase) {
        this.pv = iWordBase.getPv();
        this.click = iWordBase.getClick();
        this.competition = iWordBase.getCompetition();
        this.ctr = (int) Math.floor((this.click * 10000.0D / this.pv));
    }

    @JsonProperty
    public String getWord() {
        return this.text;
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getTableHashKey() {
        // TODO Auto-generated method stub
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
    public boolean jdbcSave() {
        long res = 0L;
        long existId = existId(text, topUrlBaseId);

        if (existId > 0L) {
            this.id = existId;
            res = update();
        } else {
            res = insert();
        }
        return res > 0;
    }

    @Override
    public String getIdName() {
        return "id";
    }

    public static long existId(String text, Long topUrlBaseId) {
//        return !CommonUtils.isEmpty(fetch("text = ? and topUrlBaseId = ?", text, topUrlBaseId));
        return dp.singleLongQuery(" select id from " + TABLE_NAME + "  where topUrlBaseId = ? and text = ? ",
                topUrlBaseId, text);
    }

    public static List<TopKey> fetch(String sql, Object... objs) {
        return new ListFetcher(null, sql, objs).call();
    }

    static String selectSql = "select text,topUrlId,upRateRank,searchRank,"
            + "focusIndex,rateChange,rankChange,cid3,cid2,score,topUrlBaseId,lastUpdateTime,pv,click,ctr,competition,id from " + TABLE_NAME;

    public long insert() {
        long id = dp
                .insert(true,
                        "insert into `topkey_`(`text`,`topUrlId`,`upRateRank`,`searchRank`,`focusIndex`,`rateChange`,`rankChange`,`cid3`,`cid2`,`score`,`topUrlBaseId`,`lastUpdateTime`,`pv`,`click`,`ctr`,`competition`) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                        this.text, this.topUrlId, this.upRateRank, this.searchRank, this.focusIndex, this.rateChange,
                        this.rankChange, this.cid3, this.cid2, this.score, this.topUrlBaseId, this.lastUpdateTime,
                        this.pv, this.click, this.ctr, this.competition);
        this.id = id;
        return id;
    }

    public long update() {
        long updateNum = dp
                .insert("update `topkey_` set  `topUrlId` = ?, `upRateRank` = ?, `searchRank` = ?, `focusIndex` = ?, `rateChange` = ?, `rankChange` = ?, `cid3` = ?, `cid2` = ?, "
                        +
                        "`score` = ?, `lastUpdateTime` = ?, `pv` = ?, `click` = ?, `ctr` = ?, `competition` = ?"
                        +
                        //                        " where `topUrlBaseId` = ? and `text` = ?  ",
                        " where id = ?  ",
                        this.topUrlId, this.upRateRank, this.searchRank, this.focusIndex, this.rateChange,
                        this.rankChange, this.cid3, this.cid2, this.score, this.lastUpdateTime,
                        this.pv, this.click, this.ctr, this.competition, this.id);
        return updateNum;
    }

    public static void truncateTable() {
        String sql = "TRUNCATE " + TopKey.TABLE_NAME;
        dp.update(sql);
    }

    public TopKey(ResultSet rs) throws SQLException {
        this.text = rs.getString(1);
        this.topUrlId = rs.getLong(2);
        this.upRateRank = rs.getInt(3);
        this.searchRank = rs.getInt(4);
        this.focusIndex = rs.getInt(5);
        this.rateChange = rs.getDouble(6);
        this.rankChange = rs.getInt(7);
        this.cid3 = rs.getInt(8);
        this.cid2 = rs.getInt(9);
        this.score = rs.getInt(10);
        this.topUrlBaseId = rs.getLong(11);
        this.lastUpdateTime = rs.getLong(12);
        this.pv = rs.getInt(13);
        this.click = rs.getInt(14);
        this.ctr = rs.getInt(15);
        this.competition = rs.getInt(16);
        this.id = rs.getLong(17);
    }

    public static class ListFetcher extends JDBCExecutor<List<TopKey>> {
        public ListFetcher(Long hashKeyId, String whereQuery, Object... params) {
            super(false, whereQuery, params);
            StringBuilder sb = new StringBuilder();
            sb.append(selectSql);
            sb.append(" where   true  ");
            if (!StringUtils.isBlank(whereQuery)) {
                sb.append(" and ");
                sb.append(whereQuery);
            }

            this.src = dp.getSrc();
            this.query = sb.toString();

        }

        @Override
        public List<TopKey> doWithResultSet(ResultSet rs) throws SQLException {
            List<TopKey> list = new ArrayList<TopKey>();
            while (rs.next()) {
                list.add(new TopKey(rs));
            }
            return list;
        }
    }

    public static long countJdbc(String whereQuery, Object... params) {
        StringBuilder sb = new StringBuilder();
        sb.append("select count(*) from ");
        sb.append(TABLE_NAME);
        sb.append(" where  true ");
        if (!StringUtils.isBlank(whereQuery)) {
            sb.append(" and ");
            sb.append(whereQuery);
            return dp.singleLongQuery(sb.toString(), params);
        }
        return dp.singleLongQuery(sb.toString());
    }

    @Override
    public void _save() {
        this.jdbcSave();
    }
}
