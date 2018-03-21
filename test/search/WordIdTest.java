
package search;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import job.word.ElasticWordSyncTailJob;
import models.word.ElasticRawWord;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.test.UnitTest;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.api.WidAPIs;
import com.ciaosir.client.utils.MapIterator;
import com.ciaosir.commons.ClientException;

import configs.TMConfigs;

public class WordIdTest extends UnitTest {

    File originFile = new File(new File(TMConfigs.configDir, "words"), "tb_hot.txt");

    static final Logger log = LoggerFactory.getLogger(WordIdTest.class);

    static final String TAG = "WordIdTest";

    @Before
    public void testResetParams() {
        SearchManager.resetParams();
//        List<String> result = QuerySugAPI.getQuerySugListSimple(s);
    }

    int eachNum = 128;

    @Test
    public void testUpdateElasticRawWord() {
        new ElasticWordSyncTailJob().doJob();
    }


    public void testAll() {

    }

    public void ensureTail() {
        try {
            List<String> lines = FileUtils.readLines(originFile);
            int lineNum = lines.size();
            int start = 0;
            int end = 0;
            while (start < lineNum) {
                end = start + eachNum;
                if (end > lineNum) {
                    end = lineNum;
                }

                log.info("[start ]:" + start);
                List<String> subline = lines.subList(start, end);
//                int count = 3;
                for (String line : subline) {
                    doForSingleWordTail(line);
                }

                CommonUtils.sleepQuietly(100L);

                start += eachNum;
            }

        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }

    }

    public void doForSingleWordTail(String line) {
    }
}
