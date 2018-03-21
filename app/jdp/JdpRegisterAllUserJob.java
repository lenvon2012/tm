
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
import bustbapi.JDPApi.JuShiTaGetUsers;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;

import configs.Subscribe.Version;
import dao.UserDao;
import dao.UserDao.UserBatchOper;

public class JdpRegisterAllUserJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(JdpRegisterAllUserJob.class);

    public static final String TAG = "JdpRegisterJob";

    Set<String> registeredNicks = null;

    Set<String> newRegisterNicks = new HashSet<String>();

    int allRegisteredNick = 0;

    int alreadyContainNum = 0;

    int newRegisterNum = 0;

    int totalUserNum = 0;

    int validUserNum = 0;

    public void doJob() {

        registeredNicks = new JuShiTaGetUsers().call();
        if (CommonUtils.isEmpty(registeredNicks)) {
            registeredNicks = SetUtils.EMPTY_SET;
        }
        log.error("[fetch registered nicks :]" + registeredNicks.size());
        this.allRegisteredNick = registeredNicks.size();

        new UserBatchOper(32) {
            @Override
            public void doForEachUser(User user) {
                this.sleepTime = 1L;
                totalUserNum++;
                if (!UserDao.doValid(user)) {
                    return;
                }
                if(Version.LL == user.getVersion()) {
                	return;
                }
                validUserNum++;
                addJdpRegister(user);
                log.info("jdp register  offset :" + offset + "  tostring:" +
                        JdpRegisterAllUserJob.this.toString());

            }

        }.call();
    }

    private void addJdpRegister(User user) {
        boolean alreadyContains = registeredNicks.contains(user.getUserNick());
        if (alreadyContains) {

//            log.info(" already contain:" + user);
            alreadyContainNum++;
            return;
        }

//        log.info(" try register to jdp:" + user);
        newRegisterNum++;
        newRegisterNicks.add(user.getUserNick() + ":" + DateUtil.formDateForLog(user.getFirstLoginTime()));
        new JDPApi.JuShiTaAddUserApi(user).call();
        CommonUtils.sleepQuietly(50L);

    }

    // registeredNicks.size()报空指针错
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
