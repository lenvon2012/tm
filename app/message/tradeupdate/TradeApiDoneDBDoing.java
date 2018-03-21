package message.tradeupdate;

import models.updatetimestamp.updatestatus.TradeDailyUpdateTask;
import transaction.TransactionSecurity;

public class TradeApiDoneDBDoing  extends TradeDailyUpdateMsg {

	public TradeApiDoneDBDoing(Long userId, Long ts) {
		super(userId, ts);
	}

	@Override
	public void applyFor(final TradeDailyUpdateTask t) {
		new TransactionSecurity<Void>() {

			@Override
			public Void operateOnDB() {
				t.setApiDone();
				t.setDbDoing();
//				t.save();
				t.rawUpdate();

				return null;
			}
		}.execute();
	}
}
