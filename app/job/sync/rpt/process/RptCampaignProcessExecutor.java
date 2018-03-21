package job.sync.rpt.process;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import models.campaign.CampaignPlay;
import monitor.StatusReporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;

import com.ciaosir.client.CommonUtils;

import dao.rpt.RptCampaignDao;

@Every("5s")
public class RptCampaignProcessExecutor extends
        RptProcessJobExecutor<RptCampaignProcessExecutor.RptCampaignProcessInfo, String> {

    private static final Logger log = LoggerFactory.getLogger(RptCampaignProcessExecutor.class);

    public static Queue<RptCampaignProcessExecutor.RptCampaignProcessInfo> queue = new ConcurrentLinkedQueue<RptCampaignProcessExecutor.RptCampaignProcessInfo>();

    public static Queue<String> nickQueue = new ConcurrentLinkedQueue<String>();

    public static RptCampaignProcessExecutorStatusReport reporter = new RptCampaignProcessExecutorStatusReport();

    public static long totalAdded = 0;

    public static class RptCampaignProcessInfo {

        long userId;
        String userNick;
        List<CampaignPlay> campaignList;
        long endTs;

        public RptCampaignProcessInfo(long userId, String userNick, List<CampaignPlay> campaignList, long endTs) {
            this.userId = userId;
            this.userNick = userNick;
            this.campaignList = campaignList;
            this.endTs = endTs;
        }

    }

    public static void addObject(long userId, String userNick, List<CampaignPlay> campaignList, long endTs) {

        if (nickQueue.contains(userNick)) {
            log.warn("The user is alreay in queue!!! userNick :" + userNick);
            return;
        }

        queue.add(new RptCampaignProcessInfo(userId, userNick, campaignList, endTs));
        nickQueue.add(userNick);

        totalAdded++;
        reporter.totalAdded = totalAdded;
//        log.warn("RptCampaignProcessExecutor for [" + userNick + "], current queue size [" + queue.size() + "]");

    }

    @Override
    public Queue<RptCampaignProcessInfo> getQueue() {
        return queue;
    }

    @Override
    public Queue<String> getIdQueue() {
        return nickQueue;
    }

    @Override
    public void doProcess(RptCampaignProcessInfo t) {

        if (CommonUtils.isEmpty(t.campaignList)) {
            log.error("The campaign list is empty, userId:" + t.userId + ", userNick:" + t.userNick);
            return;
        }

        for (CampaignPlay campaign : t.campaignList) {
            RptCampaignDao.calculatePojoByCampaignId(t.userId, t.userNick, campaign, t.endTs, 1);
            RptCampaignDao.calculatePojoByCampaignId(t.userId, t.userNick, campaign, t.endTs, 3);
            RptCampaignDao.calculatePojoByCampaignId(t.userId, t.userNick, campaign, t.endTs, 7);
        }

    }

    public static class RptCampaignProcessExecutorStatusReport implements StatusReporter {

        public long queueSize;

        public long totalAdded;

        @Override
        public void appendReport(StringBuilder sb) {
            sb.append(String.format("RptCampaignProcessExecutor!!!Queue size: [%d], total added size: [%d]\n",
                    nickQueue.size(), totalAdded));
        }
    }

}
