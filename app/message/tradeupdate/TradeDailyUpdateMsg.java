package message.tradeupdate;

import job.checker.dailyupdate.TradeDailyUpdateChecker;
import message.EntityWorkerMessage;
import message.Publisher;
import message.UserTimestampMsg;
import models.updatetimestamp.updatestatus.TradeDailyUpdateTask;
import transaction.TransactionSecurity;

public abstract class TradeDailyUpdateMsg extends UserTimestampMsg implements Publisher,
        EntityWorkerMessage<TradeDailyUpdateTask> {

    public TradeDailyUpdateMsg(Long userId, Long ts) {
        super(userId, ts);
    }

    @Override
    public TradeDailyUpdateTask findEntity() {
        return new TransactionSecurity<TradeDailyUpdateTask>() {
            @Override
            public TradeDailyUpdateTask operateOnDB() {
                return TradeDailyUpdateTask.findOrCreate(userId, ts);
            }
        }.execute();
    }

    public abstract void applyFor(final TradeDailyUpdateTask t);

    @Override
    public void publish() {
    	TradeDailyUpdateChecker.addMessage(this);
    }

    @Override
    public String toString() {
        return "TradeUpdateMsg [userId=" + userId + ", ts=" + ts + "]";
    }
    
    

}
