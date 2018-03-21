
package job.subscribe;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import tbapi.subcribe.VasOrderApi;
import utils.DateUtil;

import com.taobao.api.domain.ArticleSub;

public class ExportOrdersJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(ExportOrdersJob.class);

    public static final String TAG = "ExportOrdersJob";

    Mode model = Mode.YESTODAY;

    enum Mode {
        ALL, YESTODAY
    }

    @Override
    public void doJob() {
        long start = 0L;
        long end = 0L;
        switch (model) {
            case YESTODAY:
                start = DateUtil.formCurrDate();
                end = start - 1000L;
                start -= DateUtil.WEEK_MILLIS;
                break;

            default:
                break;
        }

        List<ArticleSub> list = new ArrayList<ArticleSub>();
        boolean res = VasOrderApi.exportAll(start, end, list);
        if (res) {
            applyForOrderList(list);
        }

    }

    private void applyForOrderList(List<ArticleSub> list) {
        for (ArticleSub articleSub : list) {
            log.info("[nick]" + articleSub.getNick());
        }

    }

}
