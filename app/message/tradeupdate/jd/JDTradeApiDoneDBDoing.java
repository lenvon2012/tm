package message.tradeupdate.jd;

import models.updatetimestamp.updatestatus.JDTradeDailyUpdateTask;
import transaction.TransactionSecurity;

public class JDTradeApiDoneDBDoing  extends JDTradeDailyUpdateMsg {

	public JDTradeApiDoneDBDoing(Long userId, Long ts) {
		super(userId, ts);
	}

	@Override
	public void applyFor(final JDTradeDailyUpdateTask t) {
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
