
package job.apiget;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import job.ApplicationStopJob;
import job.writter.VasOrderWritter;
import models.updatetimestamp.updates.VasOrderUpdateTs;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;
import utils.DateUtil;
import bustbapi.ApiUtil;
import bustbapi.VasApis;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.PYFutureTaskPool;
import com.taobao.api.domain.ArticleBizOrder;

import configs.TMConfigs.Server;
import controllers.APIConfig;

//@Every("180min")
//@Every("12s")
@Every("3min")
public class VasOrderUpdateJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(VasOrderUpdateJob.class);

    private static final String TAG = "VasOrderUpdateJob";

    static PYFutureTaskPool<List<ArticleBizOrder>> pool = new PYFutureTaskPool<List<ArticleBizOrder>>(8);

    static {
        ApplicationStopJob.addShutdownPool(pool);
    }

    protected String articleCode = StringUtils.EMPTY;

    protected long start = 0L;

    protected long end = 0L;

    protected long maxUpdateTs = 0L;

    protected long now = 0L;

    public VasOrderUpdateJob() {
        APIConfig config = APIConfig.get();
        if (config == null) {
            return;
        }
        this.articleCode = config.getSubCode();
        this.now = DateUtil.formCurrDate();
    }

//
//    public VasOrderUpdateJob(String articleCode) {
//        this.articleCode = articleCode;
//        this.now = DateUtil.formCurrDate();
////        log.warn("[new vasorder update job]" + articleCode);
//    }
//
//    public VasOrderUpdateJob(String articleCode, Long ts) {
//        this.articleCode = articleCode;
//        this.now = DateUtil.formDailyTimestamp(ts);
////        log.warn("[new item update job]" + articleCode);
//    }

    @Override
    public void doJob() {
        if (!Server.jobTimerEnable) {
            return;
        }

        log.warn("[do for ] vas order");

        if (!prepare()) {
            return;
        }

        if (ApiUtil.isApiCallLimited()) {
            log.error("the api call limited!");
            return;
        }

        requestUpdate(getStart(), getEnd());

    }

    protected boolean prepare() {

        maxUpdateTs = getMaxUserUpdateVersion();
        if (maxUpdateTs == 0) {
            try {
                maxUpdateTs = new SimpleDateFormat("yyyy-MM-dd").parse("2013-05-01").getTime();
            } catch (ParseException e) {
                log.info(e.getMessage(), e);
                maxUpdateTs = 0L;
            }
        }

//        log.info("Current Max Info:" + new Date(maxUpdateTs));

        start = maxUpdateTs;
        end = now;

        if (start > end) {
            return false;
        }

//        log.info("Set new Start and End[" + new Date(start) + "," + new Date(end) + "]");
        return true;
    }

    public void requestUpdate(long start, long end) {

//        log.warn(String.format("VasOrderUpdateJOb for %s, startTs %s, endTs %s ", this.articleCode, start, end));

        long startTs = start;
        long endTs = start + getInterval();
        if (endTs > end) {
            endTs = end;
        }

        while (endTs <= end) {
            log.info(String.format("Doing for Vas Order for %s, startTs:%s, endTs:%s", this.articleCode, new Date(
                    startTs), new Date(endTs)));

            List<ArticleBizOrder> ordersGet = getOrders(startTs, endTs);
            VasOrderWritter.addList(endTs, ordersGet);
            if (endTs == end) {
                break;
            }
            startTs += getInterval();
            endTs += getInterval();
            if (endTs > end) {
                endTs = end;
            }
        }
    }

    public List<ArticleBizOrder> getOrders(Long startCreated, Long endCreated) {

        Long totalNum = new VasApis.OrderSearchDayTotalNum(articleCode, startCreated, endCreated).call();

        if (totalNum == null || totalNum <= 0) {
            log.error("Order size for " + new Date(startCreated) + " is 0!!!");
            return null;
        }

        log.info(String.format("Vas Order Get articleCode, totalNum".replaceAll(", ", "=%s, ") + "=%s ", articleCode,
                totalNum));

        long totalPageCount = CommonUtils.calculatePageCount(totalNum, VasApis.PAGE_SIZE);

        List<FutureTask<List<ArticleBizOrder>>> promises = new ArrayList<FutureTask<List<ArticleBizOrder>>>();

        List<ArticleBizOrder> resList = new ArrayList<ArticleBizOrder>();
        for (Long pageNo = 1L; pageNo < totalPageCount + 1; pageNo++) {
            FutureTask<List<ArticleBizOrder>> promise = pool.submit(new VasApis.OrderSearchPage(articleCode,
                    startCreated, endCreated, pageNo));
            promises.add(promise);
            CommonUtils.sleepQuietly(500L);
        }

        for (FutureTask<List<ArticleBizOrder>> promise : promises) {

            List<ArticleBizOrder> ordersGet = ListUtils.EMPTY_LIST;
            try {
                ordersGet = promise.get();
            } catch (InterruptedException e) {
                log.info(e.getMessage(), e);
            } catch (ExecutionException e) {
                log.info(e.getMessage(), e);
            }

            if (!CommonUtils.isEmpty(ordersGet)) {
                resList.addAll(ordersGet);
            }
        }

        return resList;
    }

    private long getEnd() {
        return end;
    }

    private long getStart() {
        return start;
    }

    public long getMaxUserUpdateVersion() {
        VasOrderUpdateTs orderTs = VasOrderUpdateTs.findByArticleCode(articleCode);
        log.info("[Found Current Version]" + orderTs);
        return orderTs == null ? 0L : orderTs.getLastUpdateTime();
    }

    protected long getInterval() {
        return DateUtil.DAY_MILLIS;
    }
}
