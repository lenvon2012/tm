
package ats;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import utils.TopCometStreamFactory;

import com.taobao.api.internal.stream.Configuration;
import com.taobao.api.internal.stream.TopCometStream;
import com.taobao.api.internal.stream.message.StreamMsgConsumeFactory;

import configs.TMConfigs;

public class TMHttpServlet {

    private static final Logger log = LoggerFactory
            .getLogger(TMHttpServlet.class);

    public static final String TAG = "CiaoSirHttpServlet";

    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public void start() {
        if (TMConfigs.App.APP_KEY == null || TMConfigs.App.APP_SECRET == null) {
            log.info("APP_KEY APP_SERCET未初始化");
            return;
        }
        /**
         * 一个连接接收所有用户的消息数据的配置方式
         */
        Configuration conf = new Configuration(TMConfigs.App.APP_KEY, TMConfigs.App.APP_SECRET, null, Play.id);
        /**
         * 一个连接只接收指定userid的消息数据的配置方式
         */
        // Configuration conf = new Configuration(appkey,secret,userid);
        /**
         * 一个连接接收所有用户的指定一个类型的消息
         */
        // Configuration conf = new Configuration(appkey,secret,null,"item");
        /**
         * 一个连接接收所有用户的指定多个类型的消息
         */
        // Configuration conf = new
        // Configuration(appkey,secret,null,"item,trade");
        /**
         * 一个连接接收指定用户的，指定一个类型的消息
         */
        // Configuration conf = new Configuration(appkey,secret,userid,"item");
        /**
         * 一个连接接收指定用户的，指定多个类型的消息
         */
        // Configuration conf = new
        // Configuration(appkey,secret,userid,"item,trade");

        /**
         * 请注意minThread,maxThread,queueSize请按照你的消息量的情况设置一个合理的值，线程池用的是jdk自带的线程池。
         */
        conf.setMinThreads(30);// 处理消息的线程池中线程最小个数
        conf.setMaxThreads(100);// 处理消息的线程池中线程最大个数
        conf.setQueueSize(500);// 处理消息的线程池中队列大小

        /**
         * 处理消息线程池
         */
        TMConfigs.msgConsumeFactory = new StreamMsgConsumeFactory(conf.getMinThreads(), conf.getMaxThreads(),
                conf.getQueueSize(), new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable arg0, ThreadPoolExecutor arg1) {
                    }
                }
                );

        TopCometStream stream = new TopCometStreamFactory(conf).getInstance();
        stream.setConnectionListener(new ConnectionListener());
        stream.setMessageListener(new TopCometMessageListenerImpl());
        stream.start();
//
//        final TaobaoClient client = new AutoRetryTaobaoClient(TMConfigs.App.API_TAOBAO_URL, TMConfigs.App.APP_KEY,
//                TMConfigs.App.APP_SECRET);
//        final TopApiService topApiService = new TopApiService(client);
//        Configuration conf = new Configuration(APP_KEY, APP_SECRET, null);
//        TopCometStream stream = new TopCometStreamFactory(conf).getInstance();
//        stream.setConnectionListener(new ConnectionLifeCycleListenerImpl());
//        stream.setMessageListener(new TopCometMessageListenerImpl(topApiService));
//        stream.start();
        log.info("client started...");
    }

    private String getLoggerInfo(String functionName, String loggerInfo) {
        return (new Date().toString()).concat(" TMHttpServlet.")
                .concat(functionName).concat(":").concat(loggerInfo);
    }
}
