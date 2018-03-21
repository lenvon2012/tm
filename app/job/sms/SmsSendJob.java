package job.sms;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import models.defense.DefenseWarn;
import models.sms.SmsSendCount;
import models.sms.SmsSendLog;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import play.jobs.Every;
import play.jobs.Job;
import utils.DateUtil;
import actions.sms.SmsSendLaiqt.SmsResult;
import actions.sms.SmsSendLiangY;
import bustbapi.ShopApi;

import com.ciaosir.client.CommonUtils;
import com.taobao.api.domain.Shop;

import configs.TMConfigs;
import dao.defense.DefenseWarnDao;

@Every("5s")
public class SmsSendJob extends Job {

    public final static Logger log = LoggerFactory.getLogger(SmsSendJob.class);

    public static Queue<SmsInfo> queue = new ConcurrentLinkedQueue<SmsInfo>();

    public final static int SMS_NOTICE_LIMIT = 7;
    
    public final static String SMS_NOTICE_CONTENT = "您好，感谢您使用我们的软件【好评助手】，您的短信余额还剩%d条，为了不影响亲正常使用我们的软件，请及时联系我们客服充值。";
    
    public static void addQueue(Long userId, String userNick, String phone, String content, int type, long tid) {
        if (!TMConfigs.SMS_ONLINE) {
            log.warn("The sms config is not open!!!");
            return;
        }
        content = content.replaceAll("\\(", "");
        content = content.replaceAll("\\)", "");
        content = content.replaceAll("\\[", "");
        content = content.replaceAll("\\]", "");
        content = content.replaceAll("【差评防御师】", "");
        content = content.replaceAll("【好评助手】", "");
//        if(!content.startsWith("【")) {
//        	content = "【淘宝】" + content;
//        }
        if(!content.endsWith("退订回t")) {
        	content.concat("退订回t");
        }
        log.info("add sms send job: userId=" + userId + ",nick:" + userNick + ",phone:" + phone + ",tid:" + tid
                + ",type:" + type);
        queue.add(new SmsInfo(userId, userNick, phone, content, type, tid));
    }

    @Override
    public void doJob() {
        if (!TMConfigs.SMS_ONLINE) {
            //log.warn("The sms config is not open!!!");
            return;
        }
        
        log.info("[SmsSendJob] doing with queue size: " + queue.size());

        SmsInfo smsInfo = null;
        while ((smsInfo = queue.poll()) != null) {
            log.info("send sms:" + smsInfo.userNick + ",phone:" + smsInfo.phone + ",content:" + smsInfo.content
                    + ",type:" + smsInfo.type);

            try {
                List<SmsSendLog> list = SmsSendLog.findSameMsgSendLogs(smsInfo.userId, smsInfo.phone, smsInfo.type,
                        smsInfo.tid, null, null);
                if (!CommonUtils.isEmpty(list)) {
                    log.warn("message already send!! msg: " + smsInfo);
                    continue;
                }

                long remainCount = SmsSendCount.countSmsSendUsed(smsInfo.userId);
                if (remainCount > 0) {
                    log.info("Send Msg: " + smsInfo);
                    sendSmsImmediate(smsInfo.userId, smsInfo.userNick, smsInfo.phone, smsInfo.content, smsInfo.type,
                            smsInfo.tid);
                } else {
                    // 没有免费量
                    log.warn("SMS USE OUT! userId: " + smsInfo.userId + " [want send msg:] " + smsInfo);
                }

                if (TMConfigs.SMS_USE_OUT_NOTICE && remainCount < SMS_NOTICE_LIMIT) {
                    List<DefenseWarn> defenseWarns = DefenseWarnDao.findByUserId(smsInfo.userId);
                    for (DefenseWarn warn : defenseWarns) {
                        long startTs = System.currentTimeMillis() - DateUtil.WEEK_MILLIS;
                        List<SmsSendLog> sendLogs = SmsSendLog.findSameMsgSendLogs(smsInfo.userId, warn.getTelephone(),
                                SmsSendLog.TYPE.NOTICE_SELLER_SMS_OUT, 0L, startTs, null);
                        if (!CommonUtils.isEmpty(sendLogs)) {
                            log.warn("notice already send! Msg:userId=" + smsInfo.userId + ", phone="
                                    + warn.getTelephone());
                            continue;
                        }

                        log.info("Send SMS_USE_OUT notice Msg: userId=" + smsInfo.userId + ", phone="
                                + warn.getTelephone());
                        sendSmsImmediate(smsInfo.userId, smsInfo.userNick, warn.getTelephone(),
                                String.format(SMS_NOTICE_CONTENT, remainCount), SmsSendLog.TYPE.NOTICE_SELLER_SMS_OUT,
                                0L);
                    }
                }

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private static boolean sendSmsImmediate(Long userId, String userNick, String phone, String content, int type, long tid) {

        if (!TMConfigs.SMS_ONLINE) {
            log.warn("The sms config is not open!!!");
            return false;
        }
        if (StringUtils.isEmpty(phone.trim())) {
            log.error("Phone of user :" + userNick + " is empty!!!");
            return false;
        }
        
        if (TMConfigs.SMS_SEND_BUYER == false && type == SmsSendLog.TYPE.BADCOMMENT_BUYER_SMS) {
            new SmsSendLog(userId, userNick, phone, content, type, tid, false).jdbcSave();
            return false;
        }
        
        SmsResult result = new SmsResult();
        if(SmsSendLog.TYPE.BADCOMMENT_BUYER_SMS != type) {
//        	result = SmsSendLaiqt.sendLaiqtSms(phone.trim(), content, getUserShopName(userId));
        	result = SmsSendLiangY.jiaQunSmsSend(phone.trim(), content, getUserShopName(userId));
        }
        if(result != null && result.getIsSubmited() && result.getFailCount() ==0) {
        	new SmsSendLog(userId, userNick, phone, content, type, tid, true).jdbcSave();

            // 短信使用条数++
            if (type != SmsSendLog.TYPE.NOTICE_SELLER_SMS_OUT) {
                SmsSendCount.incrementUsedCount(userId);
            }
            return true;
        } else {
        	new SmsSendLog(userId, userNick, phone, content, type, tid, false).jdbcSave();
            return false;
        }
    }

    public static class SmsInfo {

        public Long userId;
        public String userNick;
        public String phone;
        public String content;
        public int type;
        public long tid;

        public SmsInfo(Long userId, String userNick, String phone, String content, int type, long tid) {
            this.userId = userId;
            this.userNick = userNick;
            this.phone = phone;
            this.content = content;
            this.type = type;
            this.tid = tid;
        }

        @Override
        public String toString() {
            return "SmsInfo [userId=" + userId + ", userNick=" + userNick + ", phone=" + phone + ", content=" + content
                    + ", type=" + type + ", tid=" + tid + "]";
        }
    }
    
    public static String userShopNamePre = "userShopNamePre_";
    public static String getUserShopName(Long userId){
        String userIdStr = String.valueOf(userId);
        try {
            Object obj = Cache.get(userShopNamePre + userIdStr);
            if(obj != null){
                return (String) obj;
            }
            return tbApiGetShopName(userId, userIdStr);
        } catch (Exception e) {
            return tbApiGetShopName(userId, userIdStr);
        }
    }
    
    private static String tbApiGetShopName(Long userId, String userIdStr){
        User user = User.findByUserId(userId);
        Shop shop = new ShopApi.ShopGet(user.getUserNick()).call();
        if(shop == null){
            return null;
        }
        String shopName = shop.getTitle();
        Cache.set(userShopNamePre + userIdStr, shopName, "48h");
        return shopName;
    }
    
}
