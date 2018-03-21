
package jdp;

import java.util.List;

import models.op.RawId;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import bustbapi.JDPApi;
import bustbapi.JDPApi.JdpItemStatus;
import dao.UserDao;
import dao.UserDao.UserBatchJob;

public class JdpStatusValidJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(JdpStatusValidJob.JdpStatusPrintJob.class);

    public static final String TAG = "JdpStatusValidJob.JdpStatusPrintJob";

    public static class JdpStatusPrintJob extends UserBatchJob {
        @Override
        public void doForUser(User user) {

        }
    }

    public static class AllJdpUserPrintJob extends Job {

        public void doJob() {
            List<RawId> ids = RawId.findAll();
            for (RawId rawId : ids) {
                User user = UserDao.findById(rawId.getId());
                if (user == null || !user.isVaild()) {
                    continue;
                }
                if (!UserDao.doValid(user)) {
                    return;
                }

                JdpItemStatus status = new JDPApi.JdpItemStatus(user);
                StringBuilder str = status.toStrBuilder();
                log.warn(str.toString());
            }
        }
    }

}
