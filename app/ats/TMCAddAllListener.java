
package ats;

import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.cache.Cache;
import play.jobs.Job;
import utils.TaobaoUtil;

import com.ciaosir.client.CommonUtils;

import configs.TMConfigs;
import controllers.APIConfig;
import controllers.APIConfig.Platform;
import dao.UserDao.UserBatchOper;

public class TMCAddAllListener extends Job {

    private static final Logger log = LoggerFactory.getLogger(TMCAddAllListener.class);

    public static final String TAG = "TMCWorker";

    @Override
    public void doJob() {

        if (!TMConfigs.App.ENABLE_TMHttpServlet) {
            return;
        }
        if (!TMConfigs.Server.jobTimerEnable) {
            return;
        }

        if (APIConfig.get().getPlatform() != Platform.taobao) {
            return;
        }
        if (Play.mode.isDev()) {
            return;
        }
        String cacheKey = TAG + APIConfig.get().getApiKey();
        if (Cache.get(cacheKey) != null) {
            return;
        }
        Cache.set(cacheKey, Boolean.TRUE, "20h");

        new UserBatchOper(64) {
            @Override
            public void doForEachUser(final User user) {
                // this.sleepTime = 100L;
                trySubmitUser(user);
            }
        }.call();

//        CommonUtils.sleepQuietly(DateUtil.ONE_MINUTE_MILLIS * 60 * 6);

    }

    public static Boolean trySubmitUser(final User user) {
        // 现在每个用户都默认开启主动通知

        int retry = 3;
        while (retry-- > 0) {
            boolean res = TaobaoUtil.permitTMCUser(user);
            if (res) {
                return Boolean.TRUE;
            }
            CommonUtils.sleepQuietly(3000L + ((long) Math.floor(Math.random() * 15000)));
        }
        return Boolean.FALSE;
    }

}
