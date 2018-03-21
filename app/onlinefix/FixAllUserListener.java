
package onlinefix;

import java.util.concurrent.Callable;

import job.showwindow.WindowRemoteJob;
import models.user.User;
import play.Play;
import play.jobs.Job;
import utils.TaobaoUtil;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.PYFutureTaskPool;
import com.ciaosir.client.utils.DateUtil;

import configs.TMConfigs;
import controllers.APIConfig;
import controllers.APIConfig.Platform;
import dao.UserDao.UserBatchOper;

public class FixAllUserListener extends Job {

    static PYFutureTaskPool<Boolean> pool = new PYFutureTaskPool<Boolean>(16);

    @Override
    public void doJob() {

        if (!TMConfigs.App.ENABLE_TMHttpServlet) {
            return;
        }

        if (APIConfig.get().getPlatform() != Platform.taobao) {
            return;
        }
        if (Play.mode.isDev()) {
            return;
        }
        if (true) {
            return;
        }

        new UserBatchOper(64) {

            @Override
            public void doForEachUser(final User user) {
                // this.sleepTime = 100L;

                if (APIConfig.get().getApp() == APIConfig.defender.getApp()) {

                    pool.submit(new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            // 现在每个用户都默认开启主动通知
                            /*if (!user.isMsgOn()) {
                                return Boolean.FALSE;
                            }*/

                            int retry = 3;
                            while (retry-- > 0) {
                                boolean res = TaobaoUtil.permitByUser(user);
                                if (res) {
                                    return Boolean.TRUE;
                                }
                                CommonUtils.sleepQuietly(3000L + ((long) Math.floor(Math.random() * 15000)));
                            }
                            return Boolean.FALSE;
                        }
                    });

                    CommonUtils.sleepQuietly(100L);
                } else {

                    WindowRemoteJob.getPool().submit(new Callable<Boolean>() {

                        @Override
                        public Boolean call() throws Exception {
                            // 现在每个用户都默认开启主动通知
                            /*if (!user.isMsgOn()) {
                                return Boolean.FALSE;
                            }*/

                            int retry = 3;
                            while (retry-- > 0) {
                                boolean res = TaobaoUtil.permitByUser(user);
                                if (res) {
                                    return Boolean.TRUE;
                                }
                                CommonUtils.sleepQuietly(3000L + ((long) Math.floor(Math.random() * 15000)));
                            }
                            return Boolean.FALSE;
                        }
                    });
                }
            }
        }.call();

        CommonUtils.sleepQuietly(DateUtil.ONE_MINUTE_MILLIS * 60 * 6);
    }

    public static PYFutureTaskPool<Boolean> getPool() {
        return pool;
    }

    public static void setPool(PYFutureTaskPool<Boolean> pool) {
        FixAllUserListener.pool = pool;
    }

}
