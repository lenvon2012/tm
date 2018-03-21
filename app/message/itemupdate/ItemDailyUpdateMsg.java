package message.itemupdate;

import job.checker.dailyupdate.ItemDailyUpdateChecker;
import message.EntityWorkerMessage;
import message.Publisher;
import message.UserTimestampMsg;
import models.updatetimestamp.updatestatus.ItemDailyUpdateTask;
import transaction.TransactionSecurity;

public abstract class ItemDailyUpdateMsg extends UserTimestampMsg implements
		Publisher, EntityWorkerMessage<ItemDailyUpdateTask> {

	public ItemDailyUpdateMsg(Long userId, Long ts) {
		super(userId, ts);
	}

	@Override
	public ItemDailyUpdateTask findEntity() {
		return new TransactionSecurity<ItemDailyUpdateTask>() {
			@Override
			public ItemDailyUpdateTask operateOnDB() {
				return ItemDailyUpdateTask.findOrCreate(userId, ts);
			}
		}.execute();
	}

	public abstract void applyFor(final ItemDailyUpdateTask t);

	@Override
	public void publish() {
		ItemDailyUpdateChecker.addMessage(this);
	}

	@Override
	public String toString() {
		return "ItemDailyUpdateMsg [userId=" + userId + ", ts=" + ts + "]";
	}

}
