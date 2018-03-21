
package ats;

import job.MessagesDealer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.jobs.Every;
import play.jobs.Job;

import com.taobao.api.internal.tmc.Message;
import com.taobao.api.internal.tmc.MessageHandler;
import com.taobao.api.internal.tmc.MessageStatus;
import com.taobao.api.internal.tmc.TmcClient;
import com.taobao.top.link.LinkException;

import configs.TMConfigs;
import controllers.APIConfig;
import controllers.APIConfig.Platform;

public class TMCSdkListener extends Job {

    private static final Logger log = LoggerFactory.getLogger(TMCSdkListener.class);

    public static final String TAG = "TMCSdkListener";

    static TmcClient client;

    @Override
    public void doJob() {

        if (!TMConfigs.App.ENABLE_TMHttpServlet) {
            return;
        }

        if (APIConfig.get().getPlatform() != Platform.taobao) {
            return;
        }
        /**
         * dev 模式不能监听
         */
        if (Play.mode.isDev() && !"zrb".equals(Play.id)) {
            return;
        }
        log.error("TMCSdkListener getApiKey = " + APIConfig.get().getApiKey() + APIConfig.get().getSecret());
        client = new TmcClient(APIConfig.get().getApiKey(), APIConfig.get().getSecret());
        client.setMessageHandler(new MessageHandler() {
            public void onMessage(Message message, MessageStatus status) {
                try {
                    //if (APIConfig.get().getApp() == APIConfig.defender.getApp() && "taobao_trade_TradeRated".equals(message.getTopic())) {
                        log.info("[taobao_trade_TradeRated~~~~~~~" + message.getId() + "]" + message.getTopic() + " ---- " + message.getContent());
                    //}
                    MessagesDealer.onReceiveTmcMessage(message);

                    // 默认不抛出异常则认为消息处理成功  
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                    status.fail();// 消息处理失败回滚，服务端需要重发  
                }
            }
        });

        try {
            client.connect();
            log.error("connectio nstarts....");
        } catch (LinkException e) {
            log.warn(e.getMessage(), e);

        }
    }

    @Every("15s")
    public static class ShutdownJob extends Job {
        public void doJob() {
            log.info("[current<" + TMConfigs.App.ENABLE_TMHttpServlet + ">tmc status:]"
                    + (client == null ? null : client.isOnline()));
        }
    }
}
