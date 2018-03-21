
package cache;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import sug.api.QuerySugAPI;

public class WordSuggesutCache implements CacheVisitor<String> {

    private static final Logger log = LoggerFactory.getLogger(WordSuggesutCache.class);

    public static final String TAG = "WordSuggesutCache";

    public static WordSuggesutCache _instance = new WordSuggesutCache();

    public WordSuggesutCache() {
    }

    public static WordSuggesutCache get() {
        return _instance;
    }

    @Override
    public String prefixKey() {
        return TAG;
    }

    @Override
    public String expired() {
        return "24h";
    }

    @Override
    public String genKey(String t) {
        return prefixKey() + t;
    }

    public List<String> getRecommends(String word) {
        if (StringUtils.isEmpty(word)) {
            return ListUtils.EMPTY_LIST;
        }
        String key = genKey(word);
        WordSuggestResult res = (WordSuggestResult) Cache.get(key);
        if (res != null) {
            return res.getList();
        }
        List<String> list = QuerySugAPI.getQuerySugList(word, false);
        res = new WordSuggestResult(list, word);
        Cache.set(key, res, expired());

        return res.getList();
    }

    public static class WordSuggestResult implements Serializable {
        private static final long serialVersionUID = 7879719522342342353L;

        List<String> list = ListUtils.EMPTY_LIST;

        String word;

        public List<String> getList() {
            return list;
        }

        public void setList(List<String> list) {
            this.list = list;
        }

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public WordSuggestResult(List<String> list, String word) {
            super();
            this.list = list;
            this.word = word;
        }

    }
}
