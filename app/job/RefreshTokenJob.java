/**
 * 
 */

package job;

import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;

import com.ciaosir.client.CommonUtils;

import dao.UserDao;
import dao.UserDao.UserBatchOper;

/**
 * @author navins
 * @date 2013-6-20 下午5:02:19
 */
//@On("0 0 4 * * ? *")
public class RefreshTokenJob extends Job {

    public final static Logger log = LoggerFactory.getLogger(RefreshTokenJob.class);

    @Override
    public void doJob() {
        new UserBatchOper(256) {
            @Override
            public void doForEachUser(User user) {
                if (user == null || !user.isVaild()) {
                    return;
                }
                UserDao.refreshTokenNow(user);
                CommonUtils.sleepQuietly(10L);
            }
        }.call();
    }

}
