package job.word;

import models.word.top.TopURLBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.jobs.Job;
import play.jobs.On;
import pojo.webpage.top.NewTopHotKeyParser;

/**
 * Created by hao on 15-5-4.
 */
//@On("0 30 1 * * ?")
public class NewTopKeyNavSpider extends Job {
    private static final Logger log = LoggerFactory.getLogger(NewTopKeyNavSpider.class);
    public static final String TAG = "NewTopKeyNavSpider";

    @Override
    public void doJob() throws Exception {

        long count = TopURLBase.count();
        if(count == 0) {
            NewTopHotKeyParser hotKeyParser = NewTopHotKeyParser.getInstance();
            hotKeyParser.getUrls();
        }
    }
}
