package job.dianquan;

import actions.dianquan.ShihuizhuDianquanService;
import configs.TMConfigs;
import models.dianquan.DianQuanItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Play;
import play.jobs.Job;
import play.jobs.On;

import java.util.Date;

/**
 * 更新实惠猪点券
 *
 * @author lyl
 * @date 2017/11/01
 */
@On("0 0 0 * * ?")
public class ShihuizhuDianquanJob extends Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShihuizhuDianquanJob.class);
    private static final String TZG_APP_KEY = "21255586";
    //private static final String TZG_APP_SECRET = "04eb2b1fa4687fbcdeff12a795f863d4";

    @Override
    public void doJob() throws Exception {
        if (!TMConfigs.Server.jobTimerEnable || Play.mode.isDev()) {
            return;
        }
        if (!TZG_APP_KEY.equals(TMConfigs.App.APP_KEY)) {
            return;
        }
        boolean clearSuccess = DianQuanItem.clearExpiredDianquan();
        if (clearSuccess) {
            boolean success = new ShihuizhuDianquanService.BatchGetDianquanInfo().getInfo();
            if (success) {
                LOGGER.info("ShihuizhuDianquanJob Success, Time: {}", new Date());
            }
        }
        LOGGER.error("ShihuizhuDianquanJob Error, Time: {}", new Date());

    }
}
