
package jdp;

import java.util.HashSet;
import java.util.Set;

import models.user.User;

import org.apache.commons.collections.SetUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import bustbapi.JDPApi;
import bustbapi.JDPApi.JuShiTaCancelApi;
import bustbapi.JDPApi.JuShiTaGetUsers;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;

import dao.UserDao;
import dao.UserDao.UserBatchOper;

public class JdpReRegisterAllUserJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(JdpReRegisterAllUserJob.class);

    public static final String TAG = "JdpRegisterJob";

    Set<String> registeredNicks = null;

    Set<String> newRegisterNicks = new HashSet<String>();

    int allRegisteredNick = 0;

    int alreadyContainNum = 0;

    int newRegisterNum = 0;

    int totalUserNum = 0;

    int validUserNum = 0;

    int offset = 0;

    public JdpReRegisterAllUserJob() {
        super();
    }

    public JdpReRegisterAllUserJob(int offset) {
        super();
        this.offset = offset;
    }

    public void doJob() {

        registeredNicks = new JuShiTaGetUsers().call();
        if (CommonUtils.isEmpty(registeredNicks)) {
            registeredNicks = SetUtils.EMPTY_SET;
        }
        log.error("[fetch registered nicks :]" + registeredNicks.size());
        this.allRegisteredNick = registeredNicks.size();

        new UserBatchOper(offset, 128) {
            @Override
            public void doForEachUser(User user) {
                this.sleepTime = 1L;
                totalUserNum++;
                if (!UserDao.doValid(user)) {
                    return;
                }
                validUserNum++;
                addJdpRegister(user);
                log.info("jdp register  offset :" + offset + "  tostring:" +
                        JdpReRegisterAllUserJob.this.toString());

            }

        }.call();
    }

    private void addJdpRegister(User user) {
        boolean alreadyContains = registeredNicks.contains(user.getUserNick());
        if (alreadyContains) {
            alreadyContainNum++;
            Boolean res = new JuShiTaCancelApi(user).call();
            if (!res) {
                return;
            }
        }

//        log.info(" try register to jdp:" + user);
        newRegisterNum++;
        newRegisterNicks.add(user.getUserNick() + ":" + DateUtil.formDateForLog(user.getFirstLoginTime()));
        new JDPApi.JuShiTaAddUserApi(user).call();
        CommonUtils.sleepQuietly(10L);
    }

    @Override
    public String toString() {
        int newRegisterNum = newRegisterNicks.size();
        return "JdpRegisterAllUserJob [registeredNicks=" + registeredNicks.size() + ", newRegisterNicks="
                + ((newRegisterNum < 30) ? StringUtils.join(newRegisterNicks, ',') : newRegisterNum)
                + ", allRegisteredNick=" + allRegisteredNick + ", alreadyContainNum=" + alreadyContainNum
                + ", newRegisterNum=" + newRegisterNum + ", totalUserNum=" + totalUserNum + ", validUserNum="
                + validUserNum + "]";
    }

}
