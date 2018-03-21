
package job.word;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import models.word.ElasticRawWord;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import sug.api.QuerySugAPI;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.api.WidAPIs;
import com.ciaosir.client.utils.MapIterator;
import com.ciaosir.commons.ClientException;

import configs.TMConfigs;
import dao.word.WordDao;

public class ElasticWordSyncTailJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(ElasticWordSyncTailJob.class);

    public static final String TAG = "ElasticWordSyncTailJob";

    File originFile = new File(new File(TMConfigs.configDir, "words"), "tb_hot.txt");

    int limit = 128;

    int offset = 0;

    public ElasticWordSyncTailJob() {
        super();
    }

    public ElasticWordSyncTailJob(int limit, int offset) {
        super();
        this.limit = limit;
        this.offset = offset;
    }

    @Override
    public void doJob() {

        while (true) {
            List<ElasticRawWord> words = WordDao.fetchUpdateNeeded(offset, limit);
            if (CommonUtils.isEmpty(words)) {
                break;
            }
            for (ElasticRawWord elasticRawWord : words) {
                doForLine(elasticRawWord);
            }

            offset += limit;
        }
    }

    private void doForLine(ElasticRawWord wordModel) {
        log.info("[do for word :]" + wordModel);
        if (wordModel == null) {
            return;
        }
        String line = wordModel.getWord();
        if (StringUtils.isBlank(line)) {
            return;
        }

        List<String> result = QuerySugAPI.getQuerySugListSimple(line);
        log.info("[back result {" + line + "}]:" + result);
        if (result == null) {
            log.error("failed for the line :" + line);
            return;
        }

        try {
//            ElasticRawWord word = ElasticRawWord.findByWord(line);
//            if (wordModel != null) {
            wordModel.setLastINWordUpdate(System.currentTimeMillis());
            wordModel.rawUpdate();
//            }

            Map<String, Long> map = new WidAPIs.GetIdsByWords(result).execute();
            new MapIterator<String, Long>(map) {
                @Override
                public void execute(Entry<String, Long> entry) {
                    new ElasticRawWord(entry.getValue(), entry.getKey()).jdbcSave();
                }
            }.call();

        } catch (ClientException e) {
            log.warn(e.getMessage(), e);
        }

    }

    int eachNum = 128;

    public void initIds() {

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
                int count = 3;

                Map<String, Long> map = null;
                while (count-- > 0) {
                    try {
                        map = new WidAPIs.GetIdsByWords(subline).execute();
                        new MapIterator<String, Long>(map) {
                            @Override
                            public void execute(Entry<String, Long> entry) {
                                new ElasticRawWord(entry.getValue(), entry.getKey()).jdbcSave();
                            }
                        }.call();

                        break;
                    } catch (ClientException e) {
                        log.warn(e.getMessage(), e);
                        log.warn("error left :" + count);
                    }
                }

                log.info("[back map :]" + map);
                CommonUtils.sleepQuietly(100L);

                start += eachNum;
            }

        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }

    }

}
