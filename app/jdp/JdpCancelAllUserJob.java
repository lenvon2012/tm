
package jdp;

import java.util.Set;

import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import bustbapi.JDPApi;
import bustbapi.JDPApi.JuShiTaGetUsers;
import dao.UserDao.UserBatchOper;

public class JdpCancelAllUserJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(JdpCancelAllUserJob.class);

    public static final String TAG = "JdpRegisterJob";

    Set<String> registeredNicks = null;

    public void doJob() {

        registeredNicks = new JuShiTaGetUsers().call();
//
//        if (CommonUtils.isEmpty(registeredNicks)) {
//            registeredNicks = SetUtils.EMPTY_SET;
//        }
        log.info("[fetch registered nicks :]" + registeredNicks.size());

        new UserBatchOper(256) {
            @Override
            public void doForEachUser(User user) {
                if (user == null || !user.isVaild()) {
                    return;
                }
                cancelJdpRegister(user);
            }

        }.call();
    }

    private void cancelJdpRegister(User user) {
        boolean alreadyContains = registeredNicks.contains(user.getUserNick());
        if (alreadyContains) {
            log.info(" try cancel to jdp:" + user);
            new JDPApi.JuShiTaCancelApi(user).call();
        } else {
            log.info(" No cancel for not register:" + user);
        }

    }

}
