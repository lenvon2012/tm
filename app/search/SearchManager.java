
package search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import models.word.ElasticRawWord;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;

import com.ciaosir.client.api.SearchAPIs;
import com.ciaosir.client.pojo.IWordBase;
import com.ciaosir.client.utils.ChsCharsUtil;
import com.ciaosir.client.utils.NumberUtil;

import dao.word.WordSearch;

public class SearchManager {

    private static final Logger log = LoggerFactory.getLogger(SearchManager.class);

    public static final String TAG = "SearchManager";

    static Set<String> goodCharSet = new HashSet<String>();

    static {
        goodCharSet.add(".");
        goodCharSet.add("-");
        goodCharSet.add("=");
        goodCharSet.add(":");
        goodCharSet.add("%");
        goodCharSet.add("*");
    }

    static SearchManager _instance = null;

    private Client client;

    public static String CLUSTER_NAME = Play.configuration.getProperty("elasticsearch.cluster.name", "elasticsearch");

    public void create() {
        WordSearch.createMapping(client);
    }

    public static synchronized SearchManager getIntance() {
        if (_instance != null) {
            return _instance;
        }

        Properties prop = Play.configuration;
        String url = prop.getProperty("elasticsearch.url", "jbt11");
        String urls = prop.getProperty("search.urls");

        int port = NumberUtil.parserInt(prop.getProperty("elasticsearch.port"), 80);

        if (StringUtils.isBlank(urls)) {
            _instance = new SearchManager(url, port);
        } else {
            _instance = new SearchManager(urls.split(","), port);
        }

        return _instance;
    }

    public SearchManager(String url, int port) {
        this.client = new TransportClient(ImmutableSettings.settingsBuilder().put("cluster.name",
                CLUSTER_NAME)).addTransportAddress(new InetSocketTransportAddress(url, port));
    }

    public SearchManager(String[] split, int port) {
        TransportClient tcpClient = new TransportClient(ImmutableSettings.settingsBuilder().put("cluster.name",
                CLUSTER_NAME));
        for (String string : split) {
            log.warn(" add search ip:" + string);
            tcpClient.addTransportAddress(new InetSocketTransportAddress(string, port));
        }

        this.client = tcpClient;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public static void removeDumpElem(List<? extends IWordBase> value) {

        Map<String, IWordBase> wordBaseMap = new HashMap<String, IWordBase>();
        for (IWordBase base : value) {
            String word = base.getWord();
            String baseKey = trimWordBaseKey(word);
//            log.info("[base key ]" + baseKey);
            IWordBase exist = wordBaseMap.get(baseKey);
            /**
             * If the word is same, use the shortest one....
             */
            if (exist == null) {
                wordBaseMap.put(baseKey, base);
            } else {
                if (base.getWord().length() < exist.getWord().length()) {
                    wordBaseMap.put(baseKey, base);
                }
            }
        }

        Set<Long> toKeepWords = new HashSet<Long>();
        Collection<IWordBase> values = wordBaseMap.values();
        for (IWordBase iWordBase : values) {
            if (excludedWords.contains(iWordBase.getWord())) {
                continue;
            }
            Long id = iWordBase.getId();
            toKeepWords.add(id);
        }

//        log.info("[tokeywords:]" + toKeepWords);

        Iterator<? extends IWordBase> it = value.iterator();
        while (it.hasNext()) {
            IWordBase next = it.next();
            Long word = next.getId();

//            log.info("[word:]" + word);
//            if (excludedWords.contains(word)) {
//                it.remove();
//                continue;
//            }

            if (toKeepWords.contains(word)) {
            } else {
                it.remove();
            }
        }
    }

    static Set<String> excludedWords = new HashSet<String>();
    static {
        excludedWords.add("雪紡连衣裙");
        excludedWords.add("女性连衣裙");
        excludedWords.add("女人的连衣裙");
        excludedWords.add("新款的连衣裙");
        excludedWords.add("雪紡 连衣裙");
        excludedWords.add("女人长袖连衣裙");
        excludedWords.add("连衣裙 大号");
        excludedWords.add("孕妇的连衣裙");
        excludedWords.add("t恤女的");
        excludedWords.add("t恤的男人");
        excludedWords.add("女子t恤");
        excludedWords.add("长袖女性t恤");
        excludedWords.add("連衣裙");

    }

    public static String trimWordBaseKey(String word) {
        int length = word.length();
        List<String> bases = new ArrayList<String>();
        for (int i = 0; i < length; i++) {
            String str = word.substring(i, i + 1);
            if (StringUtils.isAlphanumeric(str) || goodCharSet.contains(str)) {
//                    log.info("[is alpah]" + str);
                bases.add(str);
            } else if (str.matches(ChsCharsUtil.chsReg)) {
//                    log.info("[is chs]" + str);
                bases.add(str);
            }
        }
//            log.info("[bases : ]" + StringUtils.join(bases, ','));

        /**
         * Do remember to make sort....
         */
        Collections.sort(bases);
//            log.info("[bases : ]" + StringUtils.join(bases, ','));
        return StringUtils.join(bases, StringUtils.EMPTY);
    }

    public static void resetParams() {
        SearchAPIs.indexName = "tm_center";
        ElasticRawWord.indexName = SearchAPIs.indexName;
        SearchAPIs.indexType = "tm_centertype";
        ElasticRawWord.indexType = SearchAPIs.indexType;
    }

}
