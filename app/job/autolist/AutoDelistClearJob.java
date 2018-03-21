
package job.autolist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;

import com.ciaosir.client.utils.DateUtil;

import dao.autolist.AutoListTimeDao;

public class AutoDelistClearJob extends Job {

    int maxDeleteNum = 50000;

    int deleteBatchNum = 128;

    int deleteCount = 0;

    public AutoDelistClearJob() {
        super();
    }

    public AutoDelistClearJob(int maxDeleteNum) {
        super();
        this.maxDeleteNum = maxDeleteNum;
    }

    public void doJob() {
        Logger log = LoggerFactory.getLogger(AutoDelistClearJob.class);
        long endTs = DateUtil.formCurrDate() - DateUtil.TWO_WEEK_SPAN;
        int currDeleteNum = 0;
        do {
            currDeleteNum = (int) AutoListTimeDao.deleteOld(endTs, deleteBatchNum);

            deleteCount += deleteBatchNum;
            log.info("curr delete num:" + currDeleteNum + "  with delete count :" + deleteCount);
            if (deleteCount > maxDeleteNum) {
                break;
            }
        } while (currDeleteNum == deleteBatchNum);
    }

}
