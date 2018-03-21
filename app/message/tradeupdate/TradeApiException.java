package message.tradeupdate;

import models.updatetimestamp.updatestatus.TradeDailyUpdateTask;
import transaction.TransactionSecurity;

public class TradeApiException extends TradeDailyUpdateMsg {

	public TradeApiException(Long userId, Long ts) {
		super(userId, ts);
	}

	@Override
	public void applyFor(final TradeDailyUpdateTask t) {
		new TransactionSecurity<Void>() {

			@Override
			public Void operateOnDB() {
				t.setApiException();
//				t.save();
				t.rawUpdate();

				return null;
			}
		}.execute();
	}
}
