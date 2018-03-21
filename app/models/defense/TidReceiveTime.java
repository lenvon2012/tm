
package models.defense;

import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import play.jobs.Every;
import play.jobs.Job;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;

import com.taobao.api.domain.Trade;

import controllers.APIConfig;

@Entity(name = TidReceiveTime.TABLE_NAME)
public class TidReceiveTime extends GenericModel {

    private static final Logger log = LoggerFactory.getLogger(TidReceiveTime.class);

    public static final String TAG = "TidReceiveTime";

    public static final String TABLE_NAME = "tid_receive_time";

    public static DataSrc src = DataSrc.BASIC;

    @Id
    Long tid;

    @Index(name = "userId")
    Long userId;

    String buyerNick;

    Long receiveTime;

    Long created;

    private static class TradeRecieveParam {
        Long received;

        Long userId;

        Trade trade;

        public TradeRecieveParam(Long created, Long userId, Trade trade) {
            super();
            this.received = created;
            this.userId = userId;
            this.trade = trade;
        }

    }

    @Every("5s")
    public static class TidReceiverWritter extends Job {

        public TidReceiverWritter() {

        }

        static Queue<TradeRecieveParam> queue = new ConcurrentLinkedQueue<TradeRecieveParam>();

        public void doJob() {
            if (APIConfig.get().getApp() != APIConfig.defender.getApp()) {
                return;
            }

            try {
                TradeRecieveParam param = null;
                while ((param = queue.poll()) != null) {
                    doInsert(param);
                }
            } catch (Exception e) {
                log.warn(e.getMessage(), e);

            }
        }

        private void doInsert(TradeRecieveParam param) {
            Date created = param.trade.getCreated();
            JDBCBuilder.insert(false, false, src, sql, param.trade.getTid(), param.userId, param.received,
                    created == null ? 0L : created.getTime(), param.trade.getBuyerNick());
        }

        static String sql = " insert into " + TABLE_NAME
                + " (tid,userId,receiveTime,created,buyerNick) values(?,?,?,?,?)";
    }

    public static void addMsg(Long userId, Trade trade) {
        TidReceiverWritter.queue.add(new TradeRecieveParam(System.currentTimeMillis(), userId, trade));
    }

    public static void ensureSQL() {

    }
}
