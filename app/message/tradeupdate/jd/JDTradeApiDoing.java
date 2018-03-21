package message.tradeupdate.jd;

import models.updatetimestamp.updatestatus.JDTradeDailyUpdateTask;
import transaction.TransactionSecurity;

public class JDTradeApiDoing extends JDTradeDailyUpdateMsg {

	public JDTradeApiDoing(Long userId, Long ts) {
		super(userId, ts);
	}

	@Override
	public void applyFor(final JDTradeDailyUpdateTask t) {
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
