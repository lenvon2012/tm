
package jdp;

import java.util.HashSet;
import java.util.Set;

import models.user.User;

import org.apache.commons.collections.SetUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bustbapi.JDPApi;
import bustbapi.JDPApi.JuShiTaGetUsers;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;

import dao.UserDao;
import dao.UserDao.UserBatchOper;

public class JdpCancelAllJob {

    private static final Logger log = LoggerFactory.getLogger(JdpCancelAllJob.class);

    public static final String TAG = "JdpCancelAllJob";

    Set<String> registeredNicks = null;

    Set<String> newCancelNicks = new HashSet<String>();

    int allRegisteredNick = 0;

    int alreadyContainNum = 0;

    int cancelNum = 0;

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
                validUserNum++;
                addJdpRegister(user);
                log.info("jdp cancel offset :" + offset + "  tostring:" +
                        JdpCancelAllJob.this.toString());

            }

        }.call();
    }

    private void addJdpRegister(User user) {
        boolean alreadyContains = registeredNicks.contains(user.getUserNick());
        if (!alreadyContains) {
            return;
        }
        alreadyContainNum++;

//        log.info(" try register to jdp:" + user);
        cancelNum++;
        newCancelNicks.add(user.getUserNick() + ":" + DateUtil.formDateForLog(user.getFirstLoginTime()));
        new JDPApi.JuShiTaCancelApi(user).call();
        CommonUtils.sleepQuietly(10L);
    }

    // registeredNicks.size()报空指针错
    @Override
    public String toString() {
        int newCancel = newCancelNicks.size();
        return "JdpCancelAllJob  [registeredNicks=" + registeredNicks.size() + ", newRegisterNicks="
                + ((newCancel < 30) ? StringUtils.join(newCancelNicks, ',') : newCancel)
                + ", allRegisteredNick=" + allRegisteredNick + ", alreadyContainNum=" + alreadyContainNum
                + ", newRegisterNum=" + newCancel + ", totalUserNum=" + totalUserNum + ", validUserNum="
                + validUserNum + "]";
    }

}
