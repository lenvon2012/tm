package underup.frame.industry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.mysql.fengxiao.HotWordCount;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import underup.frame.industry.CatTopSaleItemSQL.HotWordInfo;
import autotitle.AutoSplit;

import com.ciaosir.commons.ClientException;

//@OnApplicationStart
public class HotSearchWord extends Job {
    private static final Logger log = LoggerFactory.getLogger(HotSearchWord.class);

    private static final String TAG = "HotSearchWord";

    public HotSearchWord() {
    }

    @Override
    public void doJob() {
        long t, t1;
        t = System.currentTimeMillis();
        List<HotWordInfo> hotWordInfos = CatTopSaleItemSQL.getCidInfos();
        for (HotWordInfo hotWordInfo : hotWordInfos) {
            t = System.currentTimeMillis();
            List<String> titles = CatTopSaleItemSQL.getTitleWords(hotWordInfo.getCid(), hotWordInfo.getYear(),
                    hotWordInfo.getMonth());
            t1 = System.currentTimeMillis();
            log.info("----get the titles from cat_top_sale_item need the time is " + (t1 - t));
            Map<String, Long> hotWords = new HashMap<String, Long>();
            try {
                for (String title : titles) {
                    AutoSplit splitTool = new AutoSplit(title, null);
                    List<String> wordsSplit = splitTool.execute();
                    for (String wordSplit : wordsSplit) {
                        if (hotWords.containsKey(wordSplit)) {
                            Long count = hotWords.get(wordSplit);
                            ++count;
                            hotWords.remove(wordSplit);
                            hotWords.put(wordSplit, count);
                        } else {
                            hotWords.put(wordSplit, 1L);
                        }
                    }
                }
                t = System.currentTimeMillis();
                log.info("----split the title need the time is " + (t - t1));
                HotWordCount.insertPatch(hotWords, hotWordInfo.getCid(), hotWordInfo.getYear(), hotWordInfo.getMonth());
                t1 = System.currentTimeMillis();
                log.info("----store the hotword into the database need the time is " + (t1 - t));
            } catch (ClientException e) {
                log.debug("error");
            }
        }
        //触发yearandmonth的job
        new UpdateYearMonthJob().doJob();
    }
}
