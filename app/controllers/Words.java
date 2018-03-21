
package controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import models.item.ItemPlay;
import models.mysql.word.MyLexicon;
import models.mysql.word.TMCWordBase;
import models.mysql.word.WordBase;
import models.user.User;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.CacheFor;
import play.db.jpa.NoTransaction;
import pojo.webpage.top.TMWordBase;
import result.TMPaginger;
import result.TMResult;
import search.SearchManager;
import autotitle.AutoSplit;
import bustbapi.TMApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.api.SearchAPIs;
import com.ciaosir.client.api.SearchAPIs.SearchParams;
import com.ciaosir.client.api.SearchAPIs.SearchRes;
import com.ciaosir.client.pojo.IWordBase;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.pojo.WordBaseBean;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.NumberUtil;
import com.ciaosir.commons.ClientException;

import configs.TMConfigs;
import dao.item.ItemDao;

public class Words extends TMController {

    private static final Logger log = LoggerFactory.getLogger(Words.class);

    public static final String TAG = "Words";

    @NoTransaction
    public static void search(String s, String title, long numIid, int pn, int ps) throws ClientException {
//        log.info("[search word:]>>>" + s);

        s = tryGetSearchKey(s, title, numIid);

        if (StringUtils.isBlank(s)) {
            renderJSON(TMPaginger.makeEmptyFail("亲，请输入搜索关键词哦"));
        }
        pn = pn < 1 ? 1 : pn;
//        ps = ps < 10 ? 30 : ps;
        ps = 60;

        String[] keys = new AutoSplit(s, ListUtils.EMPTY_LIST, true).execute().toArray(new String[] {});

        if (keys.length > 2) {
            keys = (String[]) ArrayUtils.subarray(keys, 0, 2);
        }

        log.info("[query keys :]" + ArrayUtils.toString(keys));
        SearchRes call = new SearchAPIs.TermSearchApi(getSClient(), keys, pn, ps, SearchParams.MustBooleanPVNeededQuery)
                .call();
//        log.info("[res num :]" + call.getList().size());

        List<IWordBase> bases = call.getList();
        SearchManager.removeDumpElem(bases);

//        log.info("[ basesa num]" + bases.size());

//        @SuppressWarnings("deprecation")
        TMPaginger paginger = new TMPaginger(pn, ps, (int) call.getTotalHits(), bases);
        //TMResult res = new TMResult(bases, (int) call.getTotalHits(), new PageOffset(pn, ps));
        renderJSON(JsonUtil.getJson(paginger));
    }

    public static void searchKeywords(String s, String title, long numIid, int pn, int ps) throws ClientException {
//        log.info("[search word:]>>>" + s);

        s = tryGetSearchKey(s, title, numIid);

        if (StringUtils.isBlank(s)) {
            renderJSON(TMPaginger.makeEmptyFail("亲，请输入搜索关键词哦"));
        }
        pn = pn < 1 ? 1 : pn;
//        ps = ps < 10 ? 30 : ps;
        ps = 60;

        String[] keys = new AutoSplit(s, ListUtils.EMPTY_LIST, true).execute().toArray(new String[] {});

        if (keys.length > 2) {
            keys = (String[]) ArrayUtils.subarray(keys, 0, 2);
        }

        SearchRes call = new SearchAPIs.TermSearchApi(getSClient(), keys, pn, ps, SearchParams.MustBooleanPVNeededQuery)
                .call();
//        log.info("[res num :]" + call.getList().size());

        List<IWordBase> bases = SearchAPIs.buildWordBase(call);
//        log.info("[ basesa num]" + bases.size());

//        @SuppressWarnings("deprecation")
        //TMPaginger paginger = new TMPaginger(pn, ps, (int) call.getTotalHits(), bases);
        TMResult res = new TMResult(bases, (int) call.getTotalHits(), new PageOffset(pn, ps));
        renderJSON(JsonUtil.getJson(res));
    }

    static String tryGetSearchKey(String s, String title, long numIid) {
        if (!StringUtils.isBlank(s)) {
            return s;
        }
        if (!StringUtils.isEmpty(title)) {
            return title;
        }

        User user = getUser();
        if (user == null) {
            return StringUtils.EMPTY;
        }
        ItemPlay item = null;
        if (numIid >= 0) {
            item = ItemDao.findByNumIid(user.getId(), numIid);
            if (item != null) {
                s = item.getACidKey();
                if (!StringUtils.isEmpty(s)) {
                    return s;
                } else {
                    return item.getTitle();
                }
            }
        }

        item = NumberUtil.first(ItemDao.findByUserId(user.getId(), 1));
        if (item == null) {
            return StringUtils.EMPTY;
        }

        s = item.getACidKey();
        return StringUtils.isEmpty(s) ? item.getTitle() : s;

    }

    /*
     * 普通查询
     */
    public static void norsearch(String s, String title, long numIid, int pn, int ps) throws ClientException {

        s = tryGetSearchKey(s, title, numIid);

        if (StringUtils.isBlank(s)) {
            renderJSON(TMPaginger.makeEmptyFail("亲，请输入搜索关键词哦"));
        }
        pn = pn < 1 ? 1 : pn;
        ps = ps < 10 ? 30 : ps;
//        ps = 30;

        String[] keys = new AutoSplit(s, ListUtils.EMPTY_LIST, true).execute().toArray(new String[] {});

        if (keys.length > 2) {
            keys = (String[]) ArrayUtils.subarray(keys, 0, 2);
        }
        //得到了切好的词并且放到了数组中下面写查找的方法
        List<WordBase> bases = WordBase.norsearch(keys, pn, ps);

        TMResult res = new TMResult(bases, WordBase.getCountOfNormal(keys), new PageOffset(pn, ps));
        renderJSON(JsonUtil.getJson(res));
    }

    /*
     * 高级查询
     */
    public static void supersearch(String s, int pn, int ps) {

        List<WordBase> words = new ArrayList<WordBase>();
        words = null;
        List<List<String>> conditions = tryGetSearchcondition(s);
        words = WordBase.superSearch(conditions, pn, ps);
//		TMPaginger paginger = new TMPaginger(pn, ps,words.size(), words);
        TMResult res = new TMResult(words, (int) WordBase.getTotalHits(conditions), new PageOffset(pn, ps));
        renderJSON(JsonUtil.getJson(res));
    }

    static List<List<String>> tryGetSearchcondition(String s) {
        String[] conditions = s.split("!");
        List<String> baohanall = new ArrayList<String>();
        baohanall.add(conditions[0]);
        List<List<String>> searcondition = new ArrayList<List<String>>();

        //这里还有处理空条件的方法
        List<String> baohanrengyi = new ArrayList<String>();
        List<String> buhanrengyi = new ArrayList<String>();
        for (int i = 1; i <= 3; i++) {
            if (!conditions[i].equals("~")) {
                baohanrengyi.add(conditions[i]);
            }
        }
        for (int i = 4; i <= 6; i++) {
            if (!conditions[i].equals("~")) {
                buhanrengyi.add(conditions[i]);
            }
        }
        searcondition.add(baohanall);
        searcondition.add(baohanrengyi);
        searcondition.add(buhanrengyi);
        return searcondition;
    }

    /*
     * 添加到我的词库
     */
    public static void addmylexicon(Long wordId) {
        User user = getUser();
        boolean flag = MyLexicon.isIn(wordId, user.getId());
        if (flag) {
            renderText("亲！您已经添加该词了，请查看您的词库！");
        } else {
            try {
                MyLexicon myowrd = new MyLexicon();
                myowrd.setMyWordId(wordId);
                myowrd.setUserId(user.id);
                myowrd.save();
                renderText("亲，添加成功！");
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
                renderText("亲，添加失败！");
            }

        }

    }

    /*
     * 查找词
     */
    public static void searchAll(int pn, int ps) {
        User user = getUser();
        Long userId = user.id;
        List<WordBase> myLexicon = MyLexicon.searchAll(userId);

        int count = MyLexicon.getTotalHits(userId);
        TMResult res = new TMResult(myLexicon, count, new PageOffset(pn, ps));

        renderJSON(JsonUtil.getJson(res));
    }

    public static void removeWord(Long wid) {
        MyLexicon myword = MyLexicon.findById(wid);
        myword.delete("myWordId = ?", wid);
        renderJSON(TMResult.OK);
    }

    //public static void search(String s, String title, long numIid, int pn, int ps) throws ClientException {
    /**
     * @param word
     * @param pn
     * @param ps

     * @param order: pv, click, scount, score, strikeFocus
     * scount --> itemCount 宝贝数
     * score --> pv / itemCount  性价比
     * strikeFocus --> ctr 点击率 -- 转化率
     * @throws IOException

     * @param order: pv desc, click desc, scount desc, score desc, strikeFocus desc,pv asc, click asc, scount asc, score asc, strikeFocus asc
     * scount --> itemCount
     * score --> pv / itemCount
     * strikeFocus --> ctr
     * @throws IOException

     */
    public static void busSearch(int pn, int ps, long numIid, String order, String sort, String word)
            throws IOException {
        if (order == null || order.isEmpty()) {
            order = "pv";
        }
        if (sort == null || sort.isEmpty()) {
            sort = "desc";
        }

//        renderMockFileInJsonIfDev("words.bussearch.json");
        //String word = params.get("s");
        String title = params.get("title");
        //String order = params.get("order");
        //String sort = params.get("sort");

        word = tryGetSearchKey(word, title, numIid);
        word = CommonUtils.escapeSQL(word);
//        word = StringUtils.replace(word, " ", StringUtils.EMPTY);

        int minPageSize = 30;
        if (ps < minPageSize) {
            ps = minPageSize;
        }
        PageOffset po = new PageOffset(pn, ps, minPageSize);
        try {
            if (StringUtils.isBlank(order)) {
                order = "pv";
            }
            if (StringUtils.isEmpty(sort)) {
                sort = "asc";
            }
            final String realSort = sort;
            final String realOrder = order;
            List<String> list = new AutoSplit(word, false).execute();
//            TMResult tmresult = new TMApi.TMWordBaseApi(list, po, order, sort).execute();

            TMResult tmresult = TMCWordBase.doESSearch(list, pn, ps, order, sort);

//            log.info("[result:]" + new Gson().toJson(tmresult));

            if (tmresult == null) {
                tmresult = new TMResult();
            }
            List<WordBaseBean> newlistBases = (List<WordBaseBean>) tmresult.getRes();
            if (newlistBases == null) {
                newlistBases = ListUtils.EMPTY_LIST;
            }
            List<TMWordBase> formedRes = new ArrayList<TMWordBase>();
            for (IWordBase tmcWordBase : newlistBases) {
                formedRes.add(new TMWordBase(tmcWordBase));
            }

            SearchManager.removeDumpElem(formedRes);
            tmresult.setRes(formedRes);
//            Collections.sort(newlistBases, new Comparator<TMWordBase>() {
//                public int compare(TMWordBase arg0, TMWordBase arg1) {
//                    if (realSort.equals("desc")) {
//                        return arg1.getByProp(realOrder) - arg0.getByProp(realOrder);
//                    } else {
//                        return arg0.getByProp(realOrder) - arg1.getByProp(realOrder);
//                    }
//                }
//            });

            renderJSON(JsonUtil.getJson(tmresult));
        } catch (ClientException e) {
            log.warn(e.getMessage(), e);
        }
        renderJSON(TMResult.failMsg("亲,词库数据暂时有点问题哟,可以联系客服呢"));
    }

    public static void wangkeSearch(int pn, int ps, long numIid, String order, String sort, String word,
            String contain, String exclude) throws IOException {
        String[] contains = null;
        String[] excludes = null;
        if (order == null || order.isEmpty()) {
            order = "pv";
        }
        if (sort == null || sort.isEmpty()) {
            sort = "desc";
        }

        if (contain != null && !contain.isEmpty()) {
            contains = contain.split(",");
        }
        if (exclude != null && !exclude.isEmpty()) {
            excludes = exclude.split(",");
        }
        String title = params.get("title");

        word = tryGetSearchKey(word, title, numIid);
        word = CommonUtils.escapeSQL(word);

        PageOffset po = new PageOffset(pn, ps, 15);
        try {
            if (StringUtils.isBlank(order)) {
                order = "score";
            }
            if (StringUtils.isEmpty(sort)) {
                sort = "asc";
            }

            List<String> list = new AutoSplit(word, false).execute();
            log.info("[list]" + list);
            TMResult tmresult = new TMApi.TMWordBaseApi(list, po, order, sort).execute();
            List<TMWordBase> bases = (List<TMWordBase>) tmresult.getRes();
            int count = tmresult.getCount();
            List<TMWordBase> todelete = new ArrayList<TMWordBase>();
            if (bases.size() > 0) {
                for (TMWordBase base : bases) {

                    if (contains != null && contains.length > 0) {
                        //if(contains)
                    }
                    // exclude
                    if (excludes != null && excludes.length > 0) {
                        for (int i = 0; i < excludes.length; i++) {
                            if (base.word.indexOf(excludes[i]) >= 0) {
                                todelete.add(base);
                            }
                        }
                    }
                }

            }
            if (todelete.size() > 0) {
                for (TMWordBase delete : todelete) {
                    bases.remove(delete);
                }
            }

            TMResult newtmresult = new TMResult(true, pn, ps, count, StringUtils.EMPTY, bases);

            renderJSON(JsonUtil.getJson(newtmresult));
        } catch (ClientException e) {
            log.warn(e.getMessage(), e);
        }
        renderJSON(TMResult.failMsg("亲,词库数据暂时有点问题哟,可以联系客服呢"));
    }

    public static void tmEqual(String words) {
        if (StringUtils.isBlank(words)) {
            renderText("no res");
        }
        List<WordBase> res = ListUtils.EMPTY_LIST;
        try {
            res = new TMApi.TMWordEquelApi(words).execute();
        } catch (ClientException e) {
            log.warn(e.getMessage(), e);

        }
        renderJSON(JsonUtil.getJson(res));
    }

//    @CacheFor("12h")
    public static void fetch(String words, String order, String desc, int pn, int ps) throws IOException {
//        renderMockFileInJsonIfDev("words.fetch.json");

        if (pn <= 0) {
            pn = 1;
        }
        if (ps <= 0) {
            ps = 50;
        }

        if (StringUtils.isEmpty(words)) {
            renderJSON(JsonUtil.getJson(new TMResult(ListUtils.EMPTY_LIST)));
        }
        if (StringUtils.isEmpty(order)) {
            order = "pv";
        }
        if (StringUtils.isEmpty(desc)) {
            desc = "asc";
        }

        String[] wordArr = words.split(",");
        List<String> wordList = Arrays.asList(wordArr);
        PageOffset po = new PageOffset(pn, ps);
        TMResult res = null;
        if (TMConfigs.Server.useTaociWithLike) {
            res = TMCWordBase.doCacheWord(wordList, order, desc, po, false);
        } else {
            res = TMCWordBase.doESSearch(wordList, pn, ps, order, desc);
        }

        renderJSON(JsonUtil.getJson(res));
    }

    @CacheFor("12h")
    public static void fetchFt(String words, String order, String desc, int pn, int ps) throws IOException {
        renderMockFileInJsonIfDev("words.fetch.json");

        if (StringUtils.isEmpty(words)) {
            renderJSON(TMResult.failMsg("words null!"));
        }
        if (StringUtils.isEmpty(order)) {
            order = "pv";
        }
        if (StringUtils.isEmpty(desc)) {
            desc = "asc";
        }
        if (pn <= 0) {
            pn = 1;
        }
        if (ps <= 0) {
            ps = 50;
        }
        PageOffset po = new PageOffset(pn, ps);
        String[] wordArr = words.split(",");
        List<String> wordList = Arrays.asList(wordArr);
        TMResult res = TMCWordBase.searchWordFullText(wordList, order, desc, po);

        renderJSON(JsonUtil.getJson(res));
    }

    @CacheFor("12h")
    @NoTransaction
    public static void equal(String words) throws IOException {
        renderMockFileInJsonIfDev("words.fetch.json");

        if (StringUtils.isEmpty(words)) {
            renderJSON("[]");
        }
        String[] wordArr = words.split(",");
        List<String> wordList = Arrays.asList(wordArr);
        List<TMCWordBase> res = TMCWordBase.fetchEqualWord(wordList);
        renderJSON(JsonUtil.getJson(res));
    }

    @CacheFor("12h")
    public static void query(String words, String pv, String click, String price, String competition, int pn, int ps) {
        int minPv = 0;
        int maxPv = Integer.MAX_VALUE;
        int minClick = 0;
        int maxClick = Integer.MAX_VALUE;
        int minPrice = 0;
        int maxPrice = Integer.MAX_VALUE;
        int minCompetition = 0;
        int maxCompetition = Integer.MAX_VALUE;

        if (StringUtils.isEmpty(words)) {
            renderJSON(TMResult.failMsg("words null!"));
        }
        String[] wordArr = words.split(",");

        if (!StringUtils.isEmpty(pv)) {
            String[] pvs = StringUtils.split(pv, ",");
            if (pvs.length > 0 && !StringUtils.isEmpty(pvs[0])) {
                minPv = Integer.valueOf(pvs[0]);
            }
            if (pvs.length == 2 && !StringUtils.isEmpty(pvs[1])) {
                maxPv = Integer.valueOf(pvs[1]);
            }
        }

        if (!StringUtils.isEmpty(click)) {
            String[] clicks = StringUtils.split(click, ",");
            if (clicks.length > 0 && !StringUtils.isEmpty(clicks[0])) {
                minClick = Integer.valueOf(clicks[0]);
            }
            if (clicks.length == 2 && !StringUtils.isEmpty(clicks[1])) {
                maxClick = Integer.valueOf(clicks[1]);
            }
        }

        if (!StringUtils.isEmpty(price)) {
            String[] prices = StringUtils.split(price, ",");
            if (prices.length > 0 && !StringUtils.isEmpty(prices[0])) {
                minPrice = Integer.valueOf(prices[0]);
            }
            if (prices.length == 2 && !StringUtils.isEmpty(prices[1])) {
                maxPrice = Integer.valueOf(prices[1]);
            }
        }

        if (!StringUtils.isEmpty(competition)) {
            String[] competitions = StringUtils.split(competition, ",");
            if (competitions.length > 0 && !StringUtils.isEmpty(competitions[0])) {
                minCompetition = Integer.valueOf(competitions[0]);
            }
            if (competitions.length == 2 && !StringUtils.isEmpty(competitions[1])) {
                maxCompetition = Integer.valueOf(competitions[1]);
            }
        }

        if (pn <= 0) {
            pn = 1;
        }
        if (ps <= 0) {
            ps = 50;
        }
        PageOffset po = new PageOffset(pn, ps);
        List<TMCWordBase> res = TMCWordBase.queryList(wordArr, minPv, maxPv, minClick, maxClick, minPrice, maxPrice,
                minCompetition, maxCompetition, po);
        int count = (int) TMCWordBase.countQueryList(wordArr, minPv, maxPv, minClick, maxClick, minPrice, maxPrice,
                minCompetition, maxCompetition);
        TMResult ret = new TMResult(res, count, po);
        renderJSON(JsonUtil.getJson(ret));
    }

    @CacheFor("12h")
    public static void queryFt(String words, String pv, String click, String price, String competition, int pn, int ps) {
        int minPv = 0;
        int maxPv = Integer.MAX_VALUE;
        int minClick = 0;
        int maxClick = Integer.MAX_VALUE;
        int minPrice = 0;
        int maxPrice = Integer.MAX_VALUE;
        int minCompetition = 0;
        int maxCompetition = Integer.MAX_VALUE;

        if (StringUtils.isEmpty(words)) {
            renderJSON(JsonUtil.getJson(new TMResult(ListUtils.EMPTY_LIST)));
        }
        String[] wordArr = words.split(",");

        if (!StringUtils.isEmpty(pv)) {
            String[] pvs = StringUtils.split(pv, ",");
            if (pvs.length > 0 && !StringUtils.isEmpty(pvs[0])) {
                minPv = Integer.valueOf(pvs[0]);
            }
            if (pvs.length == 2 && !StringUtils.isEmpty(pvs[1])) {
                maxPv = Integer.valueOf(pvs[1]);
            }
        }

        if (!StringUtils.isEmpty(click)) {
            String[] clicks = StringUtils.split(click, ",");
            if (clicks.length > 0 && !StringUtils.isEmpty(clicks[0])) {
                minClick = Integer.valueOf(clicks[0]);
            }
            if (clicks.length == 2 && !StringUtils.isEmpty(clicks[1])) {
                maxClick = Integer.valueOf(clicks[1]);
            }
        }

        if (!StringUtils.isEmpty(price)) {
            String[] prices = StringUtils.split(price, ",");
            if (prices.length > 0 && !StringUtils.isEmpty(prices[0])) {
                minPrice = Integer.valueOf(prices[0]);
            }
            if (prices.length == 2 && !StringUtils.isEmpty(prices[1])) {
                maxPrice = Integer.valueOf(prices[1]);
            }
        }

        if (!StringUtils.isEmpty(competition)) {
            String[] competitions = StringUtils.split(competition, ",");
            if (competitions.length > 0 && !StringUtils.isEmpty(competitions[0])) {
                minCompetition = Integer.valueOf(competitions[0]);
            }
            if (competitions.length == 2 && !StringUtils.isEmpty(competitions[1])) {
                maxCompetition = Integer.valueOf(competitions[1]);
            }
        }

        if (pn <= 0) {
            pn = 1;
        }
        if (ps <= 0) {
            ps = 50;
        }

        PageOffset po = new PageOffset(pn, ps);
        List<TMCWordBase> res = TMCWordBase.queryListFullText(wordArr, minPv, maxPv, minClick, maxClick, minPrice,
                maxPrice, minCompetition, maxCompetition, po);
        int count = (int) TMCWordBase.countQueryListFullText(wordArr, minPv, maxPv, minClick, maxClick, minPrice,
                maxPrice, minCompetition, maxCompetition);

        TMResult ret = new TMResult(res, count, po);
        renderJSON(JsonUtil.getJson(ret));
    }

    public static void fetchAll(int pn, int ps) throws IOException {
        renderMockFileInJsonIfDev("words.fetch.json");

        if (pn <= 0) {
            pn = 1;
        }
        if (ps <= 0) {
            ps = 50;
        }
        PageOffset po = new PageOffset(pn, ps);
        List<TMCWordBase> res = TMCWordBase.fetchAll(po);

        renderJSON(JsonUtil.getJson(res));
    }

}
