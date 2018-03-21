package message.itemupdate;

import models.updatetimestamp.updatestatus.ItemDailyUpdateTask;
import transaction.TransactionSecurity;

public class ItemDBDone extends ItemDailyUpdateMsg {

	public ItemDBDone(Long userId, Long ts) {
		super(userId, ts);
	}

	@Override
	public void applyFor(final ItemDailyUpdateTask t) {
		new TransactionSecurity<Void>() {

			@Override
			public Void operateOnDB() {
				t.setDbDone();
				t.save();

				return null;
			}
		}.execute();
	}
}
