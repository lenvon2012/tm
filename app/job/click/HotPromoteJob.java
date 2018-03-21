
package job.click;

import java.util.Queue;

import models.popularized.Popularized.PopularizedStatus;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;
import utils.DateUtil;

import com.ciaosir.client.CommonUtils;

@Every("100s")
public class HotPromoteJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(HotPromoteJob.class);

    public void doJob() {
        if (!HourlyCheckerJob.HOUR_JOB_ENABLE) {
            return;
        }

        // TODO daweige ..come on... 参照 hourlycheck
        int hour = DateUtil.getCurrHour();
        // no fake click during 1:00 and 8:00
        if (hour >= 0 && hour < 8) {
            return;
        }

        doHotPromoteJob();
    }

    private static int hotLimit = HourlyCheckerJob.limit;

    private void doHotPromoteJob() {
        try {
            log.warn("do for the promote  job");
            long sleepInterval = HourlyCheckerJob.getSleepInterval(HourlyCheckerJob.intervalMillis);

            int offset = 0;
//            int limit = 128;
            while (true) {
                Queue<ItemNum> items = toClickItems.getItemNums(offset, hotLimit, PopularizedStatus.HotSale);
                log.info("[fetch promote size :]" + CollectionUtils.size(items));

                if (CommonUtils.isEmpty(items)) {
                    break;
                }

                offset += hotLimit;

                HourlyCheckerJob.doItemClick(items);
                CommonUtils.sleepQuietly(sleepInterval);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

}
