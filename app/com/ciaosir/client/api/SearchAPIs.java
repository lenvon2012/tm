
package com.ciaosir.client.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.SetUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.CustomScoreQueryBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.ScriptFilterBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.PYFutureTaskPool;
import com.ciaosir.client.PYThreadPool;
import com.ciaosir.client.ReturnCode;
import com.ciaosir.client.pojo.IWordBase;
import com.ciaosir.client.pojo.StringTripple;
import com.ciaosir.client.pojo.WordBaseBean;
import com.ciaosir.client.utils.CiaoStringUtil;
import com.ciaosir.client.utils.MixHelpers;
import com.ciaosir.client.word.PaodingSpliter;
import com.ciaosir.client.word.PaodingSpliter.SplitMode;
import com.ciaosir.commons.ClientException;

public abstract class SearchAPIs<T> implements Callable<T> {

    public static final Logger log = LoggerFactory.getLogger(SearchAPIs.class);

    public static final String TAG = "SearchAPIs";

    protected Client client;

    public static String indexName = "devsearch";

    public static String indexType = "devsearchtype";

    public static String bakIndexName = "tm_center";

    public static String bakIndexType = "tm_centertype";

    protected String index;

    protected boolean clearHits = true;

    public SearchAPIs(Client client) {
        super();
        this.client = client;
        this.index = indexName;
    }

    public SearchAPIs<T> backIndex() {
        this.index = bakIndexName;
        return this;
    }

    public static class SearchParams {
        boolean isPvNeeded = true;

        boolean isBlankNeeded = true;

        boolean isMust = true;

        String sortField = null;

        // DESC or ASC
        String sortOrder = null;

        String indexName = null;

        int pn = 0;

        int ps = 0;

        public SearchParams(boolean isPvNeeded, boolean isBlankNeeded, boolean isMust) {
            super();
            this.isPvNeeded = isPvNeeded;
            this.isBlankNeeded = isBlankNeeded;
            this.isMust = isMust;
        }

        public SearchParams(boolean isPvNeeded, boolean isBlankNeeded, boolean isMust, String sortField,
                            String sortOrder, String indexName) {
            super();
            this.isPvNeeded = isPvNeeded;
            this.isBlankNeeded = isBlankNeeded;
            this.isMust = isMust;
            this.sortField = sortField;
            this.sortOrder = sortOrder;
            this.indexName = indexName;
        }

        public SearchParams(boolean isPvNeeded, boolean isBlankNeeded, boolean isMust, String sortField,
                            String sortOrder, String indexName, int pn, int ps) {
            super();
            this.isPvNeeded = isPvNeeded;
            this.isBlankNeeded = isBlankNeeded;
            this.isMust = isMust;
            this.sortField = sortField;
            this.sortOrder = sortOrder;
            this.indexName = indexName;
            this.pn = pn;
            this.ps = ps;
        }

        public static SearchParams MustBooleanPVNeededQuery = new SearchParams(true, true, true);

        public static SearchParams ShouldBooleanPVNeededQuery = new SearchParams(true, true, false);

        public boolean isPvNeeded() {
            return isPvNeeded;
        }

        public void setPvNeeded(boolean isPvNeeded) {
            this.isPvNeeded = isPvNeeded;
        }

        public boolean isBlankNeeded() {
            return isBlankNeeded;
        }

        public void setBlankNeeded(boolean isBlankNeeded) {
            this.isBlankNeeded = isBlankNeeded;
        }

        public boolean isMust() {
            return isMust;
        }

        public void setMust(boolean isMust) {
            this.isMust = isMust;
        }

        public String getSortField() {
            return sortField;
        }

        public void setSortField(String sortField) {
            this.sortField = sortField;
        }

        public String getSortOrder() {
            return sortOrder;
        }

        public void setSortOrder(String sortOrder) {
            this.sortOrder = sortOrder;
        }

        public String getIndexName() {
            return indexName;
        }

        public void setIndexName(String indexName) {
            this.indexName = indexName;
        }

        public int getPn() {
            return pn;
        }

        public void setPn(int pn) {
            this.pn = pn;
        }

        public int getPs() {
            return ps;
        }

        public void setPs(int ps) {
            this.ps = ps;
        }

        public static SearchParams getMustBooleanPVNeededQuery() {
            return MustBooleanPVNeededQuery;
        }

        public static void setMustBooleanPVNeededQuery(SearchParams mustBooleanPVNeededQuery) {
            MustBooleanPVNeededQuery = mustBooleanPVNeededQuery;
        }

        public static SearchParams getShouldBooleanPVNeededQuery() {
            return ShouldBooleanPVNeededQuery;
        }

        public static void setShouldBooleanPVNeededQuery(SearchParams shouldBooleanPVNeededQuery) {
            ShouldBooleanPVNeededQuery = shouldBooleanPVNeededQuery;
        }
    }

    static TimeValue timeout = TimeValue.timeValueMillis(15000L);

    public static class RecommendProKeyHavingGetApi extends SearchAPIs<SearchRes> {

        protected String key;

        public RecommendProKeyHavingGetApi(Client client, String key) {
            super(client);
            this.key = key;
        }

        public SearchRes call() {

            QueryBuilder query = QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery("pv").from(1))
                    .must(QueryBuilders.termQuery("word", key));

            SearchResponse actionGet = this.client.prepareSearch(index).setQuery(query).setTimeout(timeout).execute()
                    .actionGet();
            SearchHits hits = actionGet.getHits();
            List<IWordBase> buildFromHits = WordBaseBean.buildFromHits(hits, clearHits);
            return new SearchRes(key, buildFromHits);
        }
    }

    public static abstract class ConditionSearch extends SearchAPIs<SearchRes> {

        protected String[] keys;

        protected int pageNum;

        protected int pageSize;

        protected boolean isPvNeeded;

        protected SearchParams params;

        public ConditionSearch(Client client, String[] keys, int pageNum, int pageSize, boolean isPvNeeded) {
            super(client);
            this.keys = keys;
            this.pageNum = pageNum;
            this.pageSize = pageSize;
            this.isPvNeeded = isPvNeeded;
            this.params = new SearchParams(isPvNeeded, true, true);
        }

        public ConditionSearch(Client client, String[] keys, int pageNum, int pageSize) {
            super(client);
            this.keys = keys;
            this.pageNum = pageNum;
            this.pageSize = pageSize;
            this.params = SearchParams.MustBooleanPVNeededQuery;
            if (params.getIndexName() != null) {
                this.index = params.getIndexName();
            } else {
                this.index = indexName;
            }
            this.isPvNeeded = params.isPvNeeded();
        }

        public ConditionSearch(Client client, String[] keys, int pageNum, int pageSize, SearchParams params) {
            super(client);
            this.keys = keys;
            this.pageNum = pageNum;
            this.pageSize = pageSize;
            this.params = params;
            if (params.getIndexName() != null) {
                this.index = params.getIndexName();
            } else {
                this.index = indexName;
            }
            this.isPvNeeded = params.isPvNeeded();
        }

        protected abstract void buildQuery(BoolQueryBuilder queryBuilder, String[] keys);

        protected int retryTime = 3;

        protected SearchRequestBuilder buildMore(SearchRequestBuilder req) {
            return req;
        }

        protected BoolQueryBuilder buildMore(BoolQueryBuilder req) {
            return req;
        }

        protected QueryBuilder buildQueryScoreRule(QueryBuilder target) {
            return target;
        }

        @SuppressWarnings("unchecked")
        public SearchRes call() throws ClientException {
            if (ArrayUtils.isEmpty(keys)) {
                return new SearchRes(StringUtils.EMPTY, ListUtils.EMPTY_LIST);
            }

            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            buildQuery(queryBuilder, keys);

            if (isPvNeeded) {
                queryBuilder.must(QueryBuilders.rangeQuery(API.PARAM_PV).from(10).to(Integer.MAX_VALUE));
            }

            String sortField = params.getSortField();
            String sortOrder = params.getSortOrder();
            while (--retryTime >= 0) {
                try {

                    SearchRequestBuilder builder = this.client.prepareSearch(index).setQuery(queryBuilder)
                            .setTimeout(timeout)
                            .setFrom(pageSize * (pageNum - 1))
                            .setSize(pageSize);
                    if (StringUtils.isNotEmpty(sortField) && StringUtils.isNotEmpty(sortOrder)) {
                        builder = builder.addSort(sortField, SortOrder.valueOf(sortOrder.toUpperCase()));
                    }

                    ListenableActionFuture<SearchResponse> resp = builder.execute();
                    SearchResponse searchResp = resp.actionGet();

                    List<IWordBase> buildFromHits = WordBaseBean.buildFromHits(searchResp.getHits(), clearHits);
                    return new SearchRes(StringUtils.join(keys, ','), buildFromHits, searchResp.getHits()
                            .getTotalHits());
                } catch (NoNodeAvailableException e) {
                    log.error(e.getMessage());
                    log.error("retry for retryTime:" + retryTime);
                    MixHelpers.sleepQuietly(1500L);
                }
            }

            throw new ClientException(ReturnCode.INNER_ERROR, "search node not available...");
        }
    }

    public static class SimpleSearchApi extends ConditionSearch {

        public SimpleSearchApi(Client client, String[] keys, int pageNum, int pageSize) {
            super(client, keys, pageNum, pageSize, true);
        }

        @Override
        protected void buildQuery(BoolQueryBuilder queryBuilder, String[] keys) {
            for (String string : keys) {
                queryBuilder.must(QueryBuilders.fieldQuery(API.PARAM_WORD, string));
            }
        }
    }

    public static class TermSearchApi extends ConditionSearch {

        public TermSearchApi(Client client, String[] keys, int pageNum, int pageSize) {
            super(client, keys, pageNum, pageSize, true);
        }

        public TermSearchApi(Client client, String[] keys, int pageNum, int pageSize, SearchParams params) {
            super(client, keys, pageNum, pageSize, params);
        }

        @Override
        protected void buildQuery(BoolQueryBuilder queryBuilder, String[] keys) {
            for (String string : keys) {

                TermQueryBuilder termQuery = QueryBuilders.termQuery(API.PARAM_WORD, string);

                if (params.isMust) {
                    queryBuilder.must(termQuery);
                } else {
                    queryBuilder.should(termQuery);
                }

            }
        }
    }

    public static class RecommendProKeyGetApi extends SearchAPIs<SearchRes> {

        protected String key;

        public RecommendProKeyGetApi(Client client, String key) {
            super(client);
            this.key = key;

        }

        public SearchRes call() {
            TermQueryBuilder termQuery = QueryBuilders.termQuery(API.PARAM_WORD, key);

            SearchResponse actionGet = this.client.prepareSearch(index).setQuery(termQuery).execute().actionGet();
            List<IWordBase> buildFromHits = WordBaseBean.buildFromHits(actionGet.getHits(), clearHits);
            return new SearchRes(key, buildFromHits);
        }
    }

    public static class AllRecommendWordApi extends SearchAPIs<Map<String, List<IWordBase>>> {

        PYThreadPool<SearchRes> pool;

        Collection<String> coll;

        public AllRecommendWordApi(Client client, Collection<String> coll) {
            super(client);
            this.coll = coll;
        }

        @SuppressWarnings("unchecked")
        public Map<String, List<IWordBase>> call() throws ClientException {

            int size = CollectionUtils.size(coll);
            if (size <= 0) {
                return MapUtils.EMPTY_MAP;
            }

            pool = new PYThreadPool<SearchRes>(coll.size());
            Map<String, List<IWordBase>> res = new HashMap<String, List<IWordBase>>();
            for (String elem : coll) {
                pool.submit(new RecommendProKeyGetApi(client, elem));
            }

            try {
                for (int i = 0; i < size; i++) {
                    SearchRes searchRes = pool.take();
                    res.put(searchRes.getQueryKey(), searchRes.getList());

                }
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
                throw new ClientException(ReturnCode.INNER_ERROR, e.getMessage());
            } finally {
                if (pool != null) {
                    pool.shutdown();
                }
            }

            return res;
        }
    }

    public static class ItemsTitleMatchSearch extends SearchAPIs<List<SearchRes>> {
        String title;

        PYThreadPool<SearchRes> pool;

        boolean mergeDump = false;

        SplitMode mode = SplitMode.NEIBER_COMB;

        public ItemsTitleMatchSearch(Client client, String title) {
            super(client);
            this.title = title;
        }

        public ItemsTitleMatchSearch(Client client, String title, boolean mergeDump) {
            super(client);
            this.title = title;
            this.mergeDump = mergeDump;
        }

        public ItemsTitleMatchSearch(Client client, String title,
                                     PYThreadPool<com.ciaosir.client.api.SearchAPIs.SearchRes> pool, boolean mergeDump, SplitMode mode) {
            super(client);
            this.title = title;
            this.pool = pool;
            this.mergeDump = mergeDump;
            this.mode = mode;
        }

        @SuppressWarnings("unchecked")
        public List<SearchRes> call() throws ClientException {
            if (StringUtils.isEmpty(title)) {
                return ListUtils.EMPTY_LIST;
            }

            List<SearchRes> res = new ArrayList<SearchAPIs.SearchRes>();
            List<String> words = PaodingSpliter.split(title, mode, mergeDump);

            if (CommonUtils.isEmpty(words)) {
                return ListUtils.EMPTY_LIST;
            }
            int size = words.size();
            pool = new PYThreadPool<SearchRes>(size);
            log.info("Submit size :" + size);
            try {

                for (String string : words) {
                    pool.submit(new RecommendProKeyGetApi(client, string));
                }
                for (int i = 0; i < size; i++) {
                    res.add(pool.take());

                }
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
                throw new ClientException(ReturnCode.INNER_ERROR, e.getMessage());
            } finally {
                if (pool != null) {
                    pool.shutdown();
                }
            }
            return res;
        }
    }

    public static class SearchRes {
        String queryKey;

        long totalHits = 0L;

        List<IWordBase> list;

        public SearchRes(String queryKey, List<IWordBase> list) {
            super();
            this.queryKey = queryKey;
            this.list = list;
        }

        public SearchRes(String queryKey, List<IWordBase> list, long totalHits) {
            super();
            this.queryKey = queryKey;
            this.list = list;
            this.totalHits = totalHits;
        }

        public String getQueryKey() {
            return queryKey;
        }

        public void setQueryKey(String queryKey) {
            this.queryKey = queryKey;
        }

        public List<IWordBase> getList() {
            return list;
        }

        public void setList(List<IWordBase> list) {
            this.list = list;
        }

        public long getTotalHits() {
            return totalHits;
        }

        public void setTotalHits(long totalHits) {
            this.totalHits = totalHits;
        }

        @Override
        public String toString() {
            return "SearchRes [queryKey=" + queryKey + ", totalHits=" + totalHits + ", list=" + list + "]";
        }

    }

    public static class ReocommendWordsSearch extends SearchAPIs<SearchRes> {

        private String title;

        private boolean isPvNeeded;

        private int pageSize = 200;

        private int pageNum = 1;

        public ReocommendWordsSearch(Client client, String title) {
            super(client);
        }

        public ReocommendWordsSearch(Client client, String title, String[] props, boolean isPVNeeded, int pageNum,
                                     int pageSize) {
            super(client);
            this.title = title;
            this.isPvNeeded = isPVNeeded;
            this.pageNum = pageNum;
            this.pageSize = pageSize;
        }

        public SearchRes call() {
            List<String> words = PaodingSpliter.split(title, SplitMode.BASE, true);

            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            for (String string : words) {
                queryBuilder.should(QueryBuilders.termQuery(API.PARAM_WORD, string));
            }

            if (isPvNeeded) {
                QueryBuilders.rangeQuery(API.PARAM_PV).from(2);
            }

            SearchResponse actionGet = this.client.prepareSearch(index).setQuery(queryBuilder)
                    .setFrom(pageSize * (pageNum - 1)).setSize(pageSize).execute().actionGet();

            List<IWordBase> buildFromHits = WordBaseBean.buildFromHits(actionGet.getHits(), clearHits);
            return new SearchRes(title, buildFromHits, actionGet.getHits().getTotalHits());

        }
    }

    public static class TitleRecommendSearch extends SearchAPIs<Map<String, IWordBase>> {

        static PYFutureTaskPool<SearchRes> pool = new PYFutureTaskPool<SearchAPIs.SearchRes>(32);

        private String title;

        private String[] props;

        Map<String, IWordBase> res;

        int pageSize;

        private boolean isWild = false;

        private SearchParams params;

        private List<String> baseList;

        public TitleRecommendSearch(Client client, String title, String[] props, boolean isWild) {
            super(client);
            this.title = title;
            this.props = props;
            this.isWild = isWild;
            this.params = SearchParams.ShouldBooleanPVNeededQuery;
        }

        public TitleRecommendSearch(Client client, String title, String[] props, boolean isWild, SearchParams params) {
            super(client);
            this.title = title;
            this.props = props;
            this.isWild = isWild;
            this.params = params;
        }

        public TitleRecommendSearch(Client client, List<String> baseList, boolean isWild, SearchParams params) {
            super(client);
            this.isWild = isWild;
            this.params = params;
            this.baseList = baseList;
        }

        @SuppressWarnings("unchecked")
        public Map<String, IWordBase> call() throws ClientException {

            //            if (StringUtils.isEmpty(title)) {
            //                return MapUtils.EMPTY_MAP;
            //            }
            if (CommonUtils.isEmpty(baseList)) {
                this.baseList = new WidAPIs.SplitAPI(title, SplitMode.BASE, false).execute();
            }

            int size = CollectionUtils.size(baseList);
            if (size < 3) {
                return MapUtils.EMPTY_MAP;
            }

            log.info("[Found Base list :]" + baseList.size());

            this.res = new HashMap<String, IWordBase>();
            callForEachWordArray(baseList, size, params);
            return res;
        }

        private void callForEachWordArray(List<String> baseList, int size, final SearchParams params) {
            //          List<String[]> splitsList = appendTargetStringSplits(baseList, size);
            List<String[]> splitsList = appendTargetStringSplits(baseList, size, 4);

            log.info("[Append split list:]" + splitsList.size());

            List<FutureTask<SearchRes>> list = new ArrayList<FutureTask<SearchRes>>();

            for (final String[] strings : splitsList) {
                FutureTask<SearchRes> submit = pool.submit(new Callable<SearchAPIs.SearchRes>() {

                    public SearchRes call() {
                        try {
                            return new TermSearchApi(client, strings, 1, isWild ? 40 : 600, params).call();
                        } catch (ClientException e) {
                            log.warn(e.getMessage(), e);
                            return null;
                        }
                    }
                });
                list.add(submit);
                //              log.info("[Count ++");
            }

            try {
                for (FutureTask<SearchRes> futureTask : list) {
                    SearchRes searchRes = futureTask.get();
                    if (searchRes == null) {
                        continue;
                    }

                    for (IWordBase iWordBase : searchRes.getList()) {
                        if (StringUtils.isEmpty(iWordBase.getWord())) {
                            continue;
                        }

                        if (iWordBase.getWord().length() > 16) {
                            continue;
                        }

                        res.put(iWordBase.getWord(), iWordBase);
                    }
                }
            } catch (InterruptedException e) {
                log.warn(e.getMessage(), e);
            } catch (ExecutionException e) {
                log.warn(e.getMessage(), e);
            }
        }

        private void callForEachWordArray(List<String> baseList, int size) {
            callForEachWordArray(baseList, size, SearchParams.ShouldBooleanPVNeededQuery);
        }

        private List<String[]> appendSimpleSplits(List<String> baseList, int size) {
            List<String[]> targetList = new ArrayList<String[]>();
            targetList.add(baseList.toArray(new String[] {}));
            return targetList;
        }

        public static List<String[]> appendTargetStringSplits(List<String> baseList, int size) {
            return appendTargetStringSplits(baseList, size, Integer.MAX_VALUE);
        }

        public static List<String[]> appendTargetStringSplits(List<String> baseList, int size, int gap) {

            List<String[]> targetList = new ArrayList<String[]>();

            for (int i = 0; i < size; i++) {
                String level1String = baseList.get(i);
                if (level1String.length() == 1) {
                    continue;
                }
                for (int j = i + 1; j < size; j++) {
                    String level2String = baseList.get(j);
                    if (level2String.length() == 1) {
                        continue;
                    }
                    if (CiaoStringUtil.containsOrContained(level1String, level2String)) {
                        continue;
                    }
                    for (int k = j + 1; k < size; k++) {
                        String level3String = baseList.get(k);
                        if (level3String.length() == 1) {
                            continue;
                        }
                        if (k - i > gap) {
                            continue;
                        }

                        if (CiaoStringUtil.containsOrContained(level3String, level1String)
                                || CiaoStringUtil.containsOrContained(level3String, level2String)) {
                            continue;
                        }

                        targetList.add(new String[] {
                                level1String, level2String, level3String
                        });
                    }
                }
            }

            for (int i = size - 1; i >= 0; i--) {
                String level1String = baseList.get(i);
                if (level1String.length() == 1) {
                    continue;
                }
                for (int j = i - 1; j >= 0; j--) {
                    String level2String = baseList.get(j);
                    if (level2String.length() == 1) {
                        continue;
                    }
                    if (CiaoStringUtil.containsOrContained(level1String, level2String)) {
                        continue;
                    }
                    for (int k = j - 1; k >= 0; k--) {
                        String level3String = baseList.get(k);
                        if (level3String.length() == 1) {
                            continue;
                        }

                        if (i - k > gap) {
                            continue;
                        }

                        if (CiaoStringUtil.containsOrContained(level3String, level1String)
                                || CiaoStringUtil.containsOrContained(level3String, level2String)) {
                            continue;
                        }

                        targetList.add(new String[] {
                                level1String, level2String, level3String
                        });
                    }
                }
            }

            return targetList;
        }

        @SuppressWarnings("unchecked")
        public static Set<StringTripple> appendStringSplits(List<String> baseList, int gap) {
            if (CommonUtils.isEmpty(baseList)) {
                return SetUtils.EMPTY_SET;
            }

            int size = baseList.size();

            Set<StringTripple> targetList = new HashSet<StringTripple>();

            for (int i = 0; i < size; i++) {
                String level1String = baseList.get(i);
                if (level1String.length() == 1) {
                    continue;
                }
                for (int j = i + 1; j < size; j++) {
                    String level2String = baseList.get(j);
                    if (level2String.length() == 1) {
                        continue;
                    }
                    if (CiaoStringUtil.containsOrContained(level1String, level2String)) {
                        continue;
                    }
                    for (int k = j + 1; k < size; k++) {
                        String level3String = baseList.get(k);
                        if (level3String.length() == 1) {
                            continue;
                        }
                        if (k - i > gap) {
                            continue;
                        }

                        if (CiaoStringUtil.containsOrContained(level3String, level1String)
                                || CiaoStringUtil.containsOrContained(level3String, level2String)) {
                            continue;
                        }

                        targetList.add(new StringTripple(level1String, level2String, level3String));
                    }
                }
            }

            for (int i = size - 1; i >= 0; i--) {
                String level1String = baseList.get(i);
                if (level1String.length() == 1) {
                    continue;
                }
                for (int j = i - 1; j >= 0; j--) {
                    String level2String = baseList.get(j);
                    if (level2String.length() == 1) {
                        continue;
                    }
                    if (CiaoStringUtil.containsOrContained(level1String, level2String)) {
                        continue;
                    }
                    for (int k = j - 1; k >= 0; k--) {
                        String level3String = baseList.get(k);
                        if (level3String.length() == 1) {
                            continue;
                        }

                        if (i - k > gap) {
                            continue;
                        }

                        if (CiaoStringUtil.containsOrContained(level3String, level1String)
                                || CiaoStringUtil.containsOrContained(level3String, level2String)) {
                            continue;
                        }

                        targetList.add(new StringTripple(level1String, level2String, level3String));
                    }
                }
            }

            return targetList;
        }

    }

    @SuppressWarnings("unchecked")
    public static List<IWordBase> buildWordBase(Collection<SearchRes> ress) {
        if (CommonUtils.isEmpty(ress)) {
            return ListUtils.EMPTY_LIST;
        }

        Map<String, IWordBase> map = new HashMap<String, IWordBase>();
        for (SearchRes searchRes : ress) {
            List<IWordBase> list = searchRes.getList();
            for (IWordBase iWordBase : list) {
                String trimmed = iWordBase.getWord();
                if (map.containsKey(trimmed)) {
                    continue;
                }
                iWordBase.setWord(trimmed);
                map.put(trimmed, iWordBase);
            }
        }

        return new ArrayList<IWordBase>(map.values());
    }

    public static List<IWordBase> buildWordBase(SearchRes searchRes) {

        Map<String, IWordBase> map = new HashMap<String, IWordBase>();
        List<IWordBase> list = searchRes.getList();
        for (IWordBase iWordBase : list) {
            if (iWordBase.getClick() < 30) {
                continue;
            }

            String trimmed = iWordBase.getWord().replaceAll(" ", StringUtils.EMPTY);

            if (map.containsKey(trimmed)) {
                continue;
            }
            iWordBase.setWord(trimmed);
            map.put(trimmed, iWordBase);
        }

        return new ArrayList<IWordBase>(map.values());
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public static String getIndexName() {
        return indexName;
    }

    public static void setIndexName(String indexName) {
        SearchAPIs.indexName = indexName;
    }

    public static String getIndexType() {
        return indexType;
    }

    public static void setIndexType(String indexType) {
        SearchAPIs.indexType = indexType;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public boolean isClearHits() {
        return clearHits;
    }

    public void setClearHits(boolean clearHits) {
        this.clearHits = clearHits;
    }

}
