
package job.writter;

import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import models.updatetimestamp.updates.VasOrderUpdateTs;
import models.vas.ArticleBizOrderPlay;
import monitor.StatusReporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;

import com.ciaosir.client.CommonUtils;
import com.taobao.api.domain.ArticleBizOrder;

import controllers.APIConfig;

@Every("5s")
public class VasOrderWritter extends Job {

    private static final Logger log = LoggerFactory.getLogger(VasOrderWritter.class);

    public static final String TAG = "VasOrderWritter";

    public static final Queue<VasOrderList> orderListToWritten = new ConcurrentLinkedQueue<VasOrderList>();

    public static StatusReporter reporter = new VasOrderWritterStatusReport();

    public static void addList(Long ts, List<ArticleBizOrder> ordersGet) {
        orderListToWritten.add(new VasOrderList(ts, ordersGet));
        log.error("Add ArticleBizOrder for " + new Date(ts) + ", size:"
                + (CommonUtils.isEmpty(ordersGet) ? 0 : ordersGet.size()));
    }

    @Override
    public void doJob() {

        Thread.currentThread().setName(TAG);

        VasOrderList orderList = null;

        while ((orderList = orderListToWritten.poll()) != null) {
            doInsert(orderList);
        }
    }

    public static void doInsert(VasOrderList orderList) {
//        log.info("[insert order list size:]"+orderList = null ? 0 : orderList.orderList);
        if (CommonUtils.isEmpty(orderList.orderList)) {
            return;
        }

        for (ArticleBizOrder order : orderList.orderList) {
            String nick = order.getNick();
//                User tbUser = new UserGetApi(null, nick).call();
//            models.user.User tbUser = UserDao.findByUserNick(nick);
//            new ArticleBizOrderPlay(order, tbUser != null ? tbUser.getLevel() : 0L).jdbcSave();
            new ArticleBizOrderPlay(order, 0L).jdbcSave();
        }

        afterFinished(orderList.ts);
    }

    private static void afterFinished(Long ts) {
        VasOrderUpdateTs.updateLastModifedTime(APIConfig.get().getSubCode(), ts);
    }

    public static class VasOrderList {

        Long ts;

        List<ArticleBizOrder> orderList;

        public VasOrderList(Long ts, List<ArticleBizOrder> orderList) {
            super();
            this.ts = ts;
            this.orderList = orderList;
        }
    }

    public static class VasOrderWritterStatusReport implements StatusReporter {

        @Override
        public void appendReport(StringBuilder sb) {
            sb.append(String.format("VasOrderWritter !!!Queue size: [%d]\n", orderListToWritten.size()));
        }
    }
}
