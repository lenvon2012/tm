
package job;

import java.util.concurrent.Callable;

import jdp.ApiJdpAdapter.JdpDelistTimeFixer;
import job.apiget.VasOrderUpdateJob;
import job.showwindow.DropCacheInitJob;
import job.showwindow.ShowWindowCrontabJob;
import models.item.ItemPlay;
import models.oplog.OpLog;
import models.oplog.TitleOpRecord;
import onlinefix.ClearAllSaleCacheJob;
import onlinefix.FixAllUserValid;
import onlinefix.UpdateAllItemsJob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.jobs.Job;
import play.jobs.On;
import ats.TMCAddAllListener;
import ats.TMCSdkListener;
import bustbapi.JDPApi;

import com.ciaosir.client.CommonUtils;

import configs.TMConfigs;
import configs.TMConfigs.Rds;
import configs.TMConfigs.Server;
import controllers.APIConfig;
import controllers.APIConfig.Platform;
import dao.UserDao;
import dao.trade.OrderDisplayDao;
import dao.trade.TradeDisplayDao;

@On("0 0 0 * * ?")
/**
 * Something we should care...
 * @author zrb
 *
 */
public class MidNightJob extends Job {

    public void doJob() {
        if (!Server.jobTimerEnable || Play.mode.isDev()) {
            return;
        }

        if (APIConfig.get().getPlatform() != Platform.taobao) {
            return;
        }
        if (APIConfig.get().getApp() == APIConfig.taoxuanci.getApp()) {
            CommonUtils.sleepQuietly(3 * 3600 * 1000L);
        }

        final Logger log = LoggerFactory.getLogger(MidNightJob.class);
//        log.info("[delete old :]" + OpLog.deleteOld(1024 << 5));
//        new DropCacheInitJob().doJob();

        new FixAllUserValid().now();
        new RefreshTokenJob().now();
        new VasOrderUpdateJob().now();
        new ClearAllSaleCacheJob().now();

//        new RebuildTopKeyJob().doJob();

        if (Rds.enableJdpPush) {
            TMConfigs.getShowwindowPool().submit(new Callable<ItemPlay>() {
                @Override
                public ItemPlay call() throws Exception {
                    try {
                        // Clear old order display

                        UserDao.clearOld();

                    } catch (Exception e) {
                        log.warn(e.getMessage(), e);
                    }
                    return null;
                }
            });

        }

        TMConfigs.getShowwindowPool().submit(new Callable<ItemPlay>() {
            @Override
            public ItemPlay call() throws Exception {
                TitleOpRecord.clearOld();
                TradeDisplayDao.clearOld();
                OrderDisplayDao.clearOld();
                OpLog.clearOld();
                return null;
            }
        });

        new UpdateAllItemsJob(false).doJob();
        new ShowWindowCrontabJob().now();

        new JdpDelistTimeFixer(false).doJob();
        new DropCacheInitJob().now();

        if (APIConfig.get().getApp() == APIConfig.defender.getApp()) {
            new TMCSdkListener().doJob();
            new TMCAddAllListener().now();
        }
        if (APIConfig.get().getApp() == APIConfig.taobiaoti.getApp()) {
        }

        // TODO Fix user item delist time whitch is behind today..

        if (Rds.enableJdpPush) {
            /*
             * TODO check the jdp status, especially for the jdp item num fixer...
             */

            JDPApi.get().clearOldTradeData();
        }

    }

}
