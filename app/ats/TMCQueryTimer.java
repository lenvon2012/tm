
package ats;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import job.MessagesDealer;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.jobs.Every;
import play.jobs.Job;
import bustbapi.TBApi;

import com.taobao.api.TaobaoClient;
import com.taobao.api.domain.TmcMessage;
import com.taobao.api.request.TmcMessagesConfirmRequest;
import com.taobao.api.request.TmcMessagesConsumeRequest;
import com.taobao.api.response.TmcMessagesConfirmResponse;
import com.taobao.api.response.TmcMessagesConsumeResponse;

import configs.TMConfigs;
import controllers.APIConfig;
import controllers.APIConfig.Platform;

//@Every("5s")
public class TMCQueryTimer extends Job {

    private static final Logger log = LoggerFactory.getLogger(TMCQueryTimer.class);

    public static final String TAG = "TMCWorker";

    public void doJob() {

        if (Play.mode.isDev()) {
            return;
        }

        if (!TMConfigs.App.ENABLE_TMHttpServlet) {
            return;
        }

        if (APIConfig.get().getPlatform() != Platform.taobao) {
            return;
        }
        if (APIConfig.get().getApp() != APIConfig.defender.getApp()) {
            if (new Random().nextInt(32) > 5) {
                return;
            }
        }

//        log.info(" do for tm query timer...");
        TaobaoClient client = TBApi.genClient();
        try {
//            do {
            long quantity = 100L;
            TmcMessagesConsumeResponse rsp = null;
            do {
                TmcMessagesConsumeRequest req = new TmcMessagesConsumeRequest();
                req.setQuantity(quantity);
                req.setGroupName("default");
                rsp = client.execute(req);
                if (rsp.isSuccess() && rsp.getMessages() != null) {
                    Set<Long> okMsgIds = new HashSet<Long>();

                    for (TmcMessage msg : rsp.getMessages()) {
                        // handle message  
//                        log.info("[" + msg.getId() + "]:" + msg.getContent());
//                        log.info(msg.getTopic());
                        // confirm message  
//                        MessagesDealer.onReceiveMessage(msg.getContent());
                        MessagesDealer.onReceiveTmcMessage(msg);
                        okMsgIds.add(msg.getId());
                    }

                    TmcMessagesConfirmRequest cReq = new TmcMessagesConfirmRequest();
                    cReq.setGroupName("default");
                    cReq.setsMessageIds(StringUtils.join(okMsgIds, ','));
                    TmcMessagesConfirmResponse cRsp = client.execute(cReq);
                    log.info(cRsp.getBody());
                }
//                log.info(rsp.getBody());
            } while (rsp != null && rsp.isSuccess() && rsp.getMessages() != null
                    && rsp.getMessages().size() == quantity);
//            Thread.sleep(1000L);
//            } while (true);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

}
