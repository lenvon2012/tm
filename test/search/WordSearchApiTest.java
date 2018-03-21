
package search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import models.word.ElasticRawWord;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.test.UnitTest;
import sug.api.QuerySugAPI;

import com.ciaosir.client.api.SearchAPIs;
import com.ciaosir.client.api.SearchAPIs.SearchParams;
import com.ciaosir.client.api.SearchAPIs.SearchRes;
import com.ciaosir.client.api.WidAPIs;
import com.ciaosir.client.pojo.IWordBase;

import dao.word.WordSearch;

public class WordSearchApiTest extends UnitTest {

    private static final Logger log = LoggerFactory.getLogger(WordSearchApiTest.class);

    public static final String TAG = "WordSearchApiTest";

    @Before
    public void testResetParams() {
        SearchManager.resetParams();
//        List<String> result = QuerySugAPI.getQuerySugListSimple(s);
    }

    @Test
    public void testAll() {
        try {
            String[] args = new String[] {
                    };
            SearchRes res = new SearchAPIs.TermSearchApi(SearchManager.getIntance().getClient(), args, 1, 200,
                    SearchParams.ShouldBooleanPVNeededQuery).call();

            log.error("should list:" + res.getList());
        } catch (Exception e) {
            log.warn(e.getMessage(), e);

        }

    }

    @Test
    public void testExist() {
        String[] args = new String[] {
                "手机壳", "手机套",
        };
//        
        try {
            SearchRes res = new SearchAPIs.TermSearchApi(SearchManager.getIntance().getClient(), args, 1, 200,
                    SearchParams.MustBooleanPVNeededQuery).call();
            log.error("must list:" + res.getList());
        } catch (Exception e) {
            log.warn(e.getMessage(), e);

        }

    }

    @Test
    public void testSync() {
        String[] words = new String[] {
                "手机壳 手机套", "手机套 手机",
        };
        try {
            Collection<IWordBase> bases = new WidAPIs.WordBaseAPI(words).execute().values();

            List<String> toWords = new ArrayList<String>();
            for (IWordBase iWordBase : bases) {
                toWords.add(iWordBase.getWord());
            }
            Map<String, Long> map = new WidAPIs.GetIdsByWords(toWords).execute();
            for (IWordBase iWordBase : bases) {
                Long id = map.get(iWordBase.getWord());
                if (id == null) {
                    log.info("no id : for word:" + iWordBase.getWord());
                    id = -1L;
                }

                iWordBase.setId(id);
            }

            log.info("[bases:]" + bases);
            List<ElasticRawWord> rawWords = new ArrayList<ElasticRawWord>();
            for (IWordBase iWordBase : bases) {
                rawWords.add(new ElasticRawWord(iWordBase));
            }

            WordSearch.callSync(rawWords);

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

}
