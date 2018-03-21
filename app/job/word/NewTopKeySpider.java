package job.word;

import models.word.top.TopKey;
import models.word.top.TopURLBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.jobs.Job;
import play.jobs.On;
import play.jobs.OnApplicationStart;
import pojo.webpage.top.NewTopHotKeyParser;

import java.util.List;

/**
 * Created by hao on 15-4-30.
 */
//@On("0 0 2 * * ?")
public class NewTopKeySpider extends Job {
    private static final Logger log = LoggerFactory.getLogger(NewTopKeySpider.class);

    public static final String TAG = "NewTopkeySpider";

    @Override
    public void doJob() throws Exception {
        String TYPE = "hot";
        String RANK = "search";

        NewTopHotKeyParser hotKeyParser = NewTopHotKeyParser.getInstance();
//        截断表
        TopKey.truncateTable();

        List<TopURLBase> topURLBases = TopURLBase.findAllLevel3WithCid();
        for (TopURLBase topURLBase : topURLBases) {
            for (int j = 0; j < 100; j+=20) {
                String url = NewTopHotKeyParser.TOPURL + topURLBase.getUrl() + "&rank=" + RANK + "&type=" + TYPE + "&s=" + j;
                hotKeyParser.getTopKeys(url, topURLBase.getId());
            }
        }
    }
}
