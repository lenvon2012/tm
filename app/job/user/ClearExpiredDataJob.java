
package job.user;

import play.jobs.Job;
import dao.trade.OrderDisplayDao;
import dao.trade.TradeDisplayDao;

public class ClearExpiredDataJob extends Job {

    public void doJob() {
        TradeDisplayDao.clearOld();
        OrderDisplayDao.clearOld();
    }
}
