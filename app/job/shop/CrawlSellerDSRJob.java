/**
 * 
 */
package job.shop;

import models.user.SellerDSR;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import play.jobs.On;
import spider.DSRSpider;

import com.ciaosir.client.CommonUtils;

import controllers.APIConfig;
import dao.UserDao.UserBatchOper;

/**
 * @author navins
 * @date: 2013年10月20日 下午2:06:03
 */
@On("0 0 4 * * ?")
public class CrawlSellerDSRJob extends Job {

    private final static Logger log = LoggerFactory.getLogger(CrawlSellerDSRJob.class);

    @Override
    public void doJob() throws Exception {
        
        if (APIConfig.get().getApp() != APIConfig.defender.getApp()) {
            return;
        }
        
        new UserBatchOper(256) {
            @Override
            public void doForEachUser(User user) {
                try {
                    if (user == null || !user.isVaild()) {
                        return;
                    }
                    
                    int retry = 1;
                    double goodRate = DSRSpider.getSellerGoodRate(user.getId());
                    while (goodRate < 0 && ++retry < 3) {
                        log.error("spider error retry: uesrId = " + user.getId());
                        goodRate = DSRSpider.getSellerGoodRate(user.getId());
                    }
                    
                    if (goodRate >= 0) {
                        new SellerDSR(user.getId(), goodRate).jdbcSave();
                        CommonUtils.sleepQuietly(50);
                    }

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }.call();
    }

}
