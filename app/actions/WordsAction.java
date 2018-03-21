
package actions;

import java.util.ArrayList;
import java.util.List;

import models.item.ItemPlay;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import search.SearchManager;
import autotitle.AutoSplit;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.api.API;
import com.ciaosir.client.api.SearchAPIs;
import com.ciaosir.client.api.SearchAPIs.SearchRes;
import com.ciaosir.client.api.SearchAPIs.TermSearchApi;
import com.ciaosir.client.pojo.IWordBase;
import com.ciaosir.client.titlediag.DiagResult;
import com.ciaosir.client.utils.CiaoStringUtil;
import com.ciaosir.commons.ClientException;

import dao.item.ItemDao;

public class WordsAction {

    private static final Logger log = LoggerFactory.getLogger(WordsAction.class);

    public static final String TAG = "WordsAction";

    public static List<String> genWords(List<String> words) {
        List<String[]> splits = appendStringSplits(words, 6);
        List<String> res = new ArrayList<String>();

        for (String[] strings : splits) {
            res.add(StringUtils.join(strings, StringUtils.EMPTY));
        }

        return res;
    }

    public static List<IWordBase> buildRecommend(long numIid, long userId) {
        // ItemPlay item = ItemPlay.findById(numIid);
        ItemPlay item = ItemDao.findByNumIid(userId, numIid);
        if (item == null) {
            log.error("No Item found for numIid:" + numIid);
            return ListUtils.EMPTY_LIST;
        }

        String title = item.getTitle();
        if (StringUtils.isEmpty(title)) {
            log.error("No Title for :" + item);
            return ListUtils.EMPTY_LIST;
        }

        try {
            return buildByTitle(title);

        } catch (ClientException e) {
            log.warn(e.getMessage(), e);
            return ListUtils.EMPTY_LIST;
        }
    }

    public static List<IWordBase> buildByTitle(String title) throws ClientException {
        List<String> segs = new AutoSplit(title, ListUtils.EMPTY_LIST, true).execute();
        List<SearchRes> sRess = new ArrayList<SearchAPIs.SearchRes>();
        List<String[]> splits = appendStringSplits(segs, 5);
        for (String[] strings : splits) {
//            log.info("[for strings :]" + StringUtils.join(strings, ','));
            SearchRes call = new SCountSearch(SearchManager.getIntance().getClient(), strings, 1, 100).call();
//            log.info("[call hits:]" + call.getTotalHits());
            sRess.add(call);
        }
        List<IWordBase> buildWordBase = SearchAPIs.buildWordBase(sRess);
        return buildWordBase;
    }

    public static List<String[]> appendStringSplits(List<String> baseList, int gap) {
        if (CommonUtils.isEmpty(baseList)) {
            return ListUtils.EMPTY_LIST;
        }

        int size = baseList.size();

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
                if (j - i > gap) {
                    continue;
                }

                if (DiagResult.promotes.contains(level1String) && DiagResult.promotes.contains(level2String)) {
                    continue;
                }

                targetList.add(new String[] {
                        level1String, level2String
                });
            }
        }

        return targetList;
    }

    public static class SCountSearch extends TermSearchApi {

        public SCountSearch(Client client, String[] keys, int pageNum, int pageSize) {
            super(client, keys, pageNum, pageSize, SearchParams.MustBooleanPVNeededQuery);
        }

        @Override
        protected BoolQueryBuilder buildMore(BoolQueryBuilder req) {
            return req.must(QueryBuilders.rangeQuery(API.PARAM_CLICK).from(5));
        }

        @Override
        protected SearchRequestBuilder buildMore(SearchRequestBuilder req) {
            return req.addSort(API.PARAM_SCOUNT, SortOrder.DESC);
        }
    }
}
