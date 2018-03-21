
package dao.word;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.word.ElasticRawWord;
import models.word.top.CatTopWord;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import utils.DateUtil;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.api.WidAPIs;
import com.ciaosir.client.pojo.IWordBase;
import com.ciaosir.client.pojo.IWordBase.SortMode;
import com.ciaosir.client.pojo.IWordBase.WordStatus;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.NumberUtil;
import com.ciaosir.commons.ClientException;

public class WordDao {

    private static final Logger log = LoggerFactory.getLogger(WordDao.class);

    public static final String TAG = "WordDao";

    public static String QUERY_BY_CID_ORDER_BY_PV = "select w.* from " + ElasticRawWord.TABLE_NAME + " as w, "
            + CatTopWord.TABLE_NAME + " as c where c.wid = w.id and c.cid = ? order by pv desc";

    public static String QUERY_BY_CID_ORDER_BY_CLICK = "select w.* from " + ElasticRawWord.TABLE_NAME + " as w, "
            + CatTopWord.TABLE_NAME + " as c where c.wid = w.id and c.cid = ? order by click desc";

    public static String QUERY_BY_CID_ORDER_BY_COMPETITION = "select w.* from " + ElasticRawWord.TABLE_NAME + " as w, "
            + CatTopWord.TABLE_NAME + " as c where c.wid = w.id and c.cid = ? order by competition desc";

    public static List<ElasticRawWord> findByCid(Long cid, int pn, int ps, SortMode sortMode) {
        if (NumberUtil.isNullOrZero(cid)) {
            return ListUtils.EMPTY_LIST;
        }

        PageOffset po = new PageOffset(pn, ps, 20);

        String query = null;
        switch (sortMode) {
            case PV:
                query = "select w.id,  w.word,  w.price,  w.click,  w.competition,  w.pv,  w.strikeFocus, "
                        + " w.searchFocus,  w.lastINWordUpdate,  w.score , w.status,  w.scount,  w.cid from "
                        + ElasticRawWord.TABLE_NAME + " as w, " + CatTopWord.TABLE_NAME
                        + " as c where c.wid = w.id and c.cid = ? order by pv desc limit ? offset ? ";
                break;
            case COMPETITION:
                query = "select w.id,  w.word,  w.price,  w.click,  w.competition,  w.pv,  w.strikeFocus, "
                        + " w.searchFocus,  w.lastINWordUpdate,  w.score , w.status,  w.scount,  w.cid from "
                        + ElasticRawWord.TABLE_NAME + " as w, " + CatTopWord.TABLE_NAME
                        + " as c where c.wid = w.id and c.cid = ? order by competition desc limit ? offset ? ";
            case CLICK:
                query = "select w.id,  w.word,  w.price,  w.click,  w.competition,  w.pv,  w.strikeFocus, "
                        + " w.searchFocus,  w.lastINWordUpdate,  w.score , w.status,  w.scount,  w.cid from "
                        + ElasticRawWord.TABLE_NAME + " as w, " + CatTopWord.TABLE_NAME
                        + " as c where c.wid = w.id and c.cid = ? order by click desc limit ? offset ? ";
                break;
            default:
                query = "select w.id,  w.word,  w.price,  w.click,  w.competition,  w.pv,  w.strikeFocus, "
                        + " w.searchFocus,  w.lastINWordUpdate,  w.score , w.status,  w.scount,  w.cid from "
                        + ElasticRawWord.TABLE_NAME + " as w, " + CatTopWord.TABLE_NAME
                        + " as c where c.wid = w.id and c.cid = ? order by pv desc limit ? offset ? ";

                break;
        }

        return new ListElasticRawWordFetcher(false, query, cid, po.getPs(), po.getPn()).call();
//        return JPA.em().createNativeQuery(query, ElasticRawWord.class).setParameter(1, cid)
//                .setFirstResult((pn - 1) * ps).setMaxResults(ps).getResultList();
    }

    public static void findPvUpdateWords(String[] splits) {

    }

    public static Map<String, ElasticRawWord> findPvUpdateWords(String words) throws ClientException {
        if (StringUtils.isBlank(words)) {
            return MapUtils.EMPTY_MAP;
        }

//        ElasticRawWord.log.info("Find Pv Up Info:" + words);

        String[] splits = StringUtils.split(words, ',');
//        log.warn("Target splits size " + splits.length);

        Map<String, ElasticRawWord> res = WordDao.findPVUpdatedWords(splits);

//        log.warn("We Have Such words in database :" + res.size());

        WordDao.appendPvNoUpdateWord(res, splits);
//        ElasticRawWord.log.warn("After append, res size:" + res.size());

        return res;
    }

    public static String QUERY_FIND_PV_UPDATE_YET = "select * from " + ElasticRawWord.TABLE_NAME + " where status & "
            + WordStatus.PV_UPDATE_YET + " > 0 and word in :names";

    public static Map<String, ElasticRawWord> findPVUpdatedWords(String[] splits) {
        if (ArrayUtils.isEmpty(splits)) {
            return MapUtils.EMPTY_MAP;
        }
        int length = splits.length;
        List<String> wordList = new ArrayList<String>();
        for (int i = 0; i < length; i++) {
            wordList.add("'" + StringEscapeUtils.escapeSql(splits[i]) + "'");
        }

//        List<String> rawWrods = new ArrayList<String>();
//        for (String string : splits) {
//            rawWrods.add(string);
//        }
        Map<String, ElasticRawWord> res = new HashMap<String, ElasticRawWord>();

        String words = StringUtils.join(wordList, ',');
        List<ElasticRawWord> resultList = fetch("  status & " + WordStatus.PV_UPDATE_YET + " > 0 and word in (" + words
                + ")");

//        List<ElasticRawWord> resultList = JPA.em().createNativeQuery(QUERY_FIND_PV_UPDATE_YET, ElasticRawWord.class)
//                .setParameter("names", rawWrods).getResultList();

        for (ElasticRawWord elasticRawWord : resultList) {
            res.put(elasticRawWord.getWord(), elasticRawWord);
        }
        return res;
    }

    public static void appendPvNoUpdateWord(Map<String, ElasticRawWord> res, String[] splits) throws ClientException {
        if (res.size() == splits.length) {
            return;
        }

        List<String> todoList = new ArrayList<String>();

        for (String split : splits) {
            ElasticRawWord exist = res.get(split);
            if (exist == null) {
                todoList.add(split);
            }
        }

//        log.warn("[add word]" + todoList);

        ElasticRawWord instance = null;
        Map<String, IWordBase> resMap = new WidAPIs.WordBaseAPI(todoList).execute();
        if (CommonUtils.isEmpty(resMap)) {
            return;
        }

        for (String todo : todoList) {
            IWordBase inWordBase = resMap.get(todo);
            //            log.info("[Foudn Word:(" + todo + ")]" + inWordBase);
            if (inWordBase != null) {
                instance = new ElasticRawWord(inWordBase);
            } else {
                instance = new ElasticRawWord(todo);
                instance.setINInfoNotProvided();
            }

            res.put(todo, instance);
            // TODO No Update Now..
//            ElasticRawWordUpdater.addQueue(instance);
        }
    }

    public static String findName() {

        return null;
    }

    public static List<ElasticRawWord> findToDelete(int offset, int limit) {
        return fetch(" status & " + WordStatus.TO_DELETE + " > 0 limit ? offset ?", limit, offset);
//        List<ElasticRawWord> list = JPA
//                .em()
//                .createNativeQuery(
//                        "select * from " + ElasticRawWord.TABLE_NAME + " where status & " + WordStatus.TO_DELETE
//                                + " > 0", ElasticRawWord.class).setFirstResult(offset).setMaxResults(limit)
//                .getResultList();
//        return list;
    }

    public static void deleteList(List<ElasticRawWord> list) {
        List<Long> idList = WordDao.toIdList(list);
        JDBCBuilder.update(false,
                "delete from " + ElasticRawWord.TABLE_NAME + " where id in (" + StringUtils.join(idList, ',') + ");");
    }

    public static List<String> toWordList(List<ElasticRawWord> rawWords) {
        if (CommonUtils.isEmpty(rawWords)) {
            return ListUtils.EMPTY_LIST;
        }
        List<String> words = new ArrayList<String>();
        for (ElasticRawWord elasticRawWord : rawWords) {
            words.add(elasticRawWord.getWord());
        }
        return words;
    }

    public static List<Long> toIdList(List<ElasticRawWord> rawWords) {
        if (CommonUtils.isEmpty(rawWords)) {
            return ListUtils.EMPTY_LIST;
        }
        List<Long> words = new ArrayList<Long>();
        for (ElasticRawWord elasticRawWord : rawWords) {
            words.add(elasticRawWord.getId());
        }
        return words;
    }

    static String FIND_DIRTY_WORDS = "status & " + WordStatus.NOT_DIRTY + " = 0 limit ? offset ? ";

    public static List<ElasticRawWord> findDirty(int offset, int limit) {
        return WordDao.fetch(FIND_DIRTY_WORDS, limit, offset);
        //        ElasticRawWord.find("status ")
//        List<ElasticRawWord> list = JPA
//                .em()
//                .createNativeQuery(
//                        "select * from " + ElasticRawWord.TABLE_NAME + " where status & "
//                                + WordStatus.NOT_DIRTY + " = 0", ElasticRawWord.class)
//                .setFirstResult(offset).setMaxResults(limit).getResultList();
//        return list;
    }

    public static void cleanDirty(Collection<ElasticRawWord> words) {
        if (CommonUtils.isEmpty(words)) {
            return;
        }
        List<Long> ids = new ArrayList<Long>();
        for (ElasticRawWord elasticRawWord : words) {
            ids.add(elasticRawWord.getId());
        }

        long updateNum = ElasticRawWord.rawwordDispatcher.update("update " + ElasticRawWord.TABLE_NAME
                + " set status = status | " + WordStatus.NOT_DIRTY + " where id in (" + StringUtils.join(ids, ',')
                + ");");

        ElasticRawWord.log.info("clean raw word number :" + updateNum);
    }

    public static List<ElasticRawWord> fetch(String whereQuery, Object... args) {
        return new ListElasticRawWordFetcher(false, ElasticRawWord.SELECT_QUERY + whereQuery, args).call();
    }

    static class ListElasticRawWordFetcher extends JDBCExecutor<List<ElasticRawWord>> {

        public ListElasticRawWordFetcher(boolean debug, String query, Object... params) {
            super(debug, query, params);
            this.src = ElasticRawWord.rawwordDispatcher.getSrc();
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

    public static List<ElasticRawWord> fetchUpdateNeeded(int offset, int limit) {
        long curr = utils.DateUtil.formCurrDate();
        long weekAgo = curr - (utils.DateUtil.WEEK_MILLIS << 1);

        return fetch("lastINWordUpdate is NULL or lastINWordUpdate <= ? limit ? offset ?", weekAgo, limit, offset);
    }

    public static void markNeedupdate(String word) {
        ElasticRawWord findByWord = findByWord(word);
        if (findByWord == null) {
            return;
        }
        WordStatus.markDirty(findByWord);
        WordStatus.markNotDirty(findByWord);
        findByWord.rawUpdate();
    }

    public static void markClean(String word) {
        ElasticRawWord findByWord = findByWord(word);
        if (findByWord == null) {
            return;
        }

        findByWord.setStatus(0);
        log.info("[findby word ]" + findByWord);
        findByWord.rawUpdate();
    }

    public static void markDirtyForContain(String word) {

        List<ElasticRawWord> list = fetch("word like ?", "%" + StringEscapeUtils.escapeSql(word) + "%");
        log.info("update :" + word);
        for (ElasticRawWord elasticRawWord : list) {
            WordStatus.markDirty(elasticRawWord);
            elasticRawWord.rawUpdate();
        }
    }

    public static ElasticRawWord findByWord(String word) {
        if (StringUtils.isEmpty(word)) {
            return null;
        }

        return NumberUtil.first(fetch(" word = ? ", word));
//        return ElasticRawWord.find("word = ?", word).first();

    }

    public static List<ElasticRawWord> findRawWordByMinPv(int minPv, int offset, int limit) {
        return fetch("pv >= ? limit ? offset ?", minPv, limit, offset);
    }

    public static List<ElasticRawWord> findLongTimeNoUpdate(int offset, int limit) {
        long curr = System.currentTimeMillis();
        curr = curr - (3 * DateUtil.WEEK_MILLIS);
        return fetch("lastINWordUpdate < ? limit ? offset ? ", curr, limit, offset);

    }
}
