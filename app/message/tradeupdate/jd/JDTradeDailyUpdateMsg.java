package message.tradeupdate.jd;

import job.checker.dailyupdate.JDTradeDailyUpdateChecker;
import message.EntityWorkerMessage;
import message.Publisher;
import message.UserTimestampMsg;
import models.updatetimestamp.updatestatus.JDTradeDailyUpdateTask;
import transaction.TransactionSecurity;

public abstract class JDTradeDailyUpdateMsg extends UserTimestampMsg implements Publisher,
        EntityWorkerMessage<JDTradeDailyUpdateTask> {

    public JDTradeDailyUpdateMsg(Long userId, Long ts) {
        super(userId, ts);
    }

    @Override
    public JDTradeDailyUpdateTask findEntity() {
        return new TransactionSecurity<JDTradeDailyUpdateTask>() {
            @Override
            public JDTradeDailyUpdateTask operateOnDB() {
                return JDTradeDailyUpdateTask.findOrCreate(userId, ts);
            }
        }.execute();
    }

    public abstract void applyFor(final JDTradeDailyUpdateTask t);

    @Override
    public void publish() {
    	JDTradeDailyUpdateChecker.addMessage(this);
    }

    @Override
    public String toString() {
        return "TradeUpdateMsg [userId=" + userId + ", ts=" + ts + "]";
    }

}
