
package onlinefix;

import java.util.concurrent.Callable;

import job.showwindow.WindowRemoteJob;
import models.user.User;
import play.jobs.Job;
import actions.TemplateAction.ItemMonitorInstaller;
import bustbapi.UserAPIs;

import com.ciaosir.client.CommonUtils;

import dao.UserDao.UserBatchOper;

public class FixMonitorInstall extends Job {

    @Override
    public void doJob() {

        new UserBatchOper(32) {

            @Override
            public void doForEachUser(final User user) {
                this.sleepTime = 50L;

                WindowRemoteJob.getPool().submit(new Callable<Boolean>() {

                    @Override
                    public Boolean call() throws Exception {
                        if (new UserAPIs.UserGetApi(user.getSessionKey(), null).call() == null) {
                            return Boolean.FALSE;
                        }

                        new ReinstallUserJob(user).doJob();
                        return Boolean.TRUE;
                    }

                });
                CommonUtils.sleepQuietly(5000L);
            }
        }.call();
    }

    public static class ReinstallUserJob extends Job {
        User user;

        public ReinstallUserJob(User user) {
            super();
            this.user = user;
        }

        public void doJob() {
            //先删除
            ItemMonitorInstaller installer = new ItemMonitorInstaller(user, true);
            installer.doJob();
            //再安装
            installer = new ItemMonitorInstaller(user, false);
            installer.doJob();
        }

    }
}
