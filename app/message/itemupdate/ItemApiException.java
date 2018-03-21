package message.itemupdate;

import models.updatetimestamp.updatestatus.ItemDailyUpdateTask;
import transaction.TransactionSecurity;

public class ItemApiException extends ItemDailyUpdateMsg {

	public ItemApiException(Long userId, Long ts) {
		super(userId, ts);
	}

	@Override
	public void applyFor(final ItemDailyUpdateTask t) {
		new TransactionSecurity<Void>() {

			@Override
			public Void operateOnDB() {
				t.setApiException();
				t.save();

				return null;
			}
		}.execute();
	}
}