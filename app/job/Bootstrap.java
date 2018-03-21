
package job;

import java.util.concurrent.Callable;

import job.showwindow.WindowRemoteJob;
import models.op.AdBanner;
import models.op.CPStaff;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import service.ServiceURLProvider;
import transaction.DBBuilder;
import actions.jd.JDAction;
import ats.TMHttpServlet;

import com.ciaosir.client.api.SimpleHttpApi;
import com.ciaosir.client.api.WidAPIs;
import com.ciaosir.client.utils.NumberUtil;

import configs.TMConfigs;
import configs.TMConfigs.App;
import controllers.APIConfig;
import controllers.APIConfig.Platform;

@OnApplicationStart
public class Bootstrap extends Job {

    private static final Logger log = LoggerFactory.getLogger(Bootstrap.class);

    public static final String TAG = "Bootstrap";

    @Override
    public void doJob() {

        //        User.deleteAll();
//        log.info("[users : ]" + User.findAll());
        DBBuilder.genUserIdHashKey(0L);
        AdBanner.ensure();

        //同行标题
//        HotTitle.loadData();
        //TaobaoUtil.PermitDemoUser();

        log.error(">>>> Enable tm http servlet:" + App.ENABLE_TMHttpServlet);
        if (TMConfigs.App.ENABLE_TMHttpServlet) {
            WindowRemoteJob.getPool().submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
//                    new FixAllUserTracerJob().doJob();
                    return null;
                }
            });
        }
//
//        WindowRemoteJob.getPool().submit(new Callable<Boolean>() {
//            @Override
//            public Boolean call() throws Exception {
//                CommonUtils.sleepQuietly(30000L);
//                List<Long> userIds = UserDao.findUserIdWindowShowOn();
//                Cache.set("AutoWindowOnUserIds", userIds);
//                return null;
//            }
//        });

        if (TMConfigs.ShowWindowParams.enableItemTradeCache && TMConfigs.Server.jobTimerEnable && Play.mode.isProd()) {
//            WindowRemoteJob.getPool().submit(new ShowWindowSaleCacheEnsure());
        }

//
//        if (Play.mode.isProd()) {
//            new Job() {
//                public void doJob() {
//                    TopShop.ensurePicpath();
//                }
//            }.now();
//        }

        APIConfig.setByApp(NumberUtil.parserInt(TMConfigs.App.APP_KEY, 0));
        ServiceURLProvider.init();

        if ("jdtuiguang".equals(Play.id) || "jdwangke".equals(Play.id) || "jdmiaoshu".equals(Play.id)) { // || "zl".equals(Play.id)) {
            APIConfig.me = JDAction.jdApiConfig;
        }

//        String wsUrl = Play.configuration.getProperty("ws.url");
//        if (!StringUtils.isBlank(wsUrl) || Play.mode.isProd()) {
//            WidAPIs.DEFAULT_WORD_SERVICE_URL = wsUrl;
//        }

        // TODO , delete more old...
//        ATSLocalTask.clearDownloading();

        //new HourlyCheckerJob().doJob();
        // 建立默认全网用户的用户标签
//        UserTag userTag = UserTag.findById(1L);
//        if (userTag == null) {
//            userTag = new UserTag(1L, 0L, "全网用户", "淘宝默认全网用户标签");
//            userTag.save();
//        }

        //TaobaoUtil.customerPermitStop();
        if (TMConfigs.App.ENABLE_TMHttpServlet && APIConfig.get().getPlatform() == Platform.taobao) {
            log.error(" start  tm http servlet");
            new TMHttpServlet().start();
        }
        
        SimpleHttpApi.init(new String[] {

//                "bbn03:9092",
                "bbn04:9092",
                "bbn25:9092",
                "bbn25:9092",
                "bbn26:9092",
                "bbn26:9092",
                "bbn27:9092",
                "bbn27:9092",
                "bbn28:9092",
                "bbn28:9092",
                "bbn29:9092",
                "bbn29:9092",
                "bbn30:9092",
                "bbn30:9092",
                "bbn31:9092",
                "bbn31:9092",
                "bbn32:9092",
                "bbn32:9092",
                "bbn33:9092",
                "bbn33:9092",
        });
        if (Play.mode.isProd()) {

            SimpleHttpApi.init(new String[] {
                    //"py02",
                    //"py04",
                    //"subway01",
                    //"subway02",
                    //"subway03",
                    //"subway04",
//                    "op01",
                    //"sp01",
//                    "sp02",
            		 "jbt02:7777", "jbt02:7777", "jbt02:7777", "jbt02:7777", "jbt02:7777", "jbt02:7777",
            		 "jbt02:7777", "jbt02:7777", "jbt02:7777", "jbt02:7777", "jbt02:7777", "jbt02:7777", "jbt02:7777",
            		 "jbt02:7777", "jbt02:7777", "jbt02:7777", "jbt02:7777", "jbt02:7777", "jbt02:7777", "jbt02:7777",
            		 "jbt02:7777", "jbt02:7777", "jbt02:7777", "jbt02:7777", "jbt02:7777",
            });
        }

        if (Play.mode.isProd()) {
//            WidAPIs.DEFAULT_WORD_SERVICE_URL = "http://223.4.49.55";
//            WidAPIs.DEFAULT_WORD_SERVICE_URL = "http://10.241.51.10:9000";
//            WidAPIs.DEFAULT_WORD_SERVICE_URL = "http://10.128.0.4:9000";
//            WidAPIs.DEFAULT_WORD_SERVICE_URL = "http://driver.tobti.com";
//            WidAPIs.DEFAULT_WORD_SERVICE_URL = "http://10.128.6.151:9002";
            // jbt12
            WidAPIs.DEFAULT_WORD_SERVICE_URL = "http://jbt12:9004";
            // jbt09
            //WidAPIs.DEFAULT_WORD_SERVICE_URL = "http://10.128.0.80:9000";
            WidAPIs.DEFAULT_WORD_SERVICE_URL = "http://jbt09:9000";

        } else {
//            WidAPIs.DEFAULT_WORD_SERVICE_URL = "http://223.4.49.205:30001";
//            WidAPIs.DEFAULT_WORD_SERVICE_URL = "http://chedao.tobti.com";
            WidAPIs.DEFAULT_WORD_SERVICE_URL = "http://driver.tobti.com";
        }
    }
}
