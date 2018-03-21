package message.itemupdate;

import models.updatetimestamp.updatestatus.ItemDailyUpdateTask;
import transaction.TransactionSecurity;

public class ItemApiDoneDBDoing extends ItemDailyUpdateMsg {

	public ItemApiDoneDBDoing(Long userId, Long ts) {
		super(userId, ts);
	}

	@Override
	public void applyFor(final ItemDailyUpdateTask t) {
		new TransactionSecurity<Void>() {

			@Override
			public Void operateOnDB() {
				t.setApiDone();
				t.setDbDoing();
				t.save();

				return null;
			}
		}.execute();
	}
}
