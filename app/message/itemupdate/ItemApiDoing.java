package message.itemupdate;

import models.updatetimestamp.updatestatus.ItemDailyUpdateTask;
import transaction.TransactionSecurity;

public class ItemApiDoing extends ItemDailyUpdateMsg {

	public ItemApiDoing(Long userId, Long ts) {
		super(userId, ts);
	}

	@Override
	public void applyFor(final ItemDailyUpdateTask t) {
		new TransactionSecurity<Void>() {

			@Override
			public Void operateOnDB() {
				t.setApiDoing();
				t.save();

				return null;
			}
		}.execute();
	}

}
