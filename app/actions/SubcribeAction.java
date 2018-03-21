package actions;

import java.util.Date;
import java.util.List;

import models.sms.SmsSendCount;
import models.user.User;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bustbapi.VasApis;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;
import com.google.gson.Gson;
import com.taobao.api.domain.ArticleUserSubscribe;

import configs.Subscribe;
import configs.Subscribe.Version;
import controllers.APIConfig;

public class SubcribeAction {

    public static final Logger log = LoggerFactory.getLogger(SubcribeAction.class);

    public static final int getSubscribeInfo(User user) {

        List<ArticleUserSubscribe> list = new VasApis.SubscribeGet(user.getUserNick(), APIConfig.get().getSubCode())
                .call();
        // log.info("[Sub :]" + list);

        if (CommonUtils.isEmpty(list)) {
            log.error("Found No Subcribe Returned...." + user);
            return user.getVersion();
        }

        int length = list.size();

        removeExpiredArticles(list, length);
        int max = getMaxVersion(list);

        return max;
    }

    private static int getMaxVersion(List<ArticleUserSubscribe> list) {
        int max = Version.BLACK;
        for (ArticleUserSubscribe sub : list) {
            String code = sub.getItemCode();
            int ver = Subscribe.getVersionByCode(code);
            if (ver > max) {
                max = ver;
            }
        }
        return max;
    }

    public static final SubscribeStatus getSubscribeStatus(User user) {
        SubscribeStatus status = new SubscribeStatus();
        long lastPayedDeadLine = Long.MIN_VALUE;

        List<ArticleUserSubscribe> list = new VasApis.SubscribeGet(user.getUserNick(), APIConfig.get().getSubCode())
                .call();
        log.info("[Sub :]" + list);
        if (CommonUtils.isEmpty(list)) {
            log.error("Found No Subcribe Returned...." + user);
            return status;
        }
        int payedVersionCount = 0;

        int max = Version.BLACK;
        String maxCode = null;
        for (ArticleUserSubscribe sub : list) {
            String code = sub.getItemCode();
            int ver = Subscribe.getVersionByCode(code);
            if (lastPayedDeadLine < 0L) {
                lastPayedDeadLine = sub.getDeadline().getTime();
            }
            if (ver <= Version.BLACK) {
                continue;
            }

            payedVersionCount++;
            max = ver;
            maxCode = code;

            if (sub.getDeadline() == null) {
                continue;
            }

            long deadLine = sub.getDeadline().getTime();
            if (deadLine > lastPayedDeadLine) {
                lastPayedDeadLine = deadLine;
            }
        }

        if (lastPayedDeadLine > 0) {
            status.payedDayLeftDay = (int) (((lastPayedDeadLine - System.currentTimeMillis()) / DateUtil.DAY_MILLIS) + 1);
        }

        // if ("ts-24135-11".equals(maxCode) || "ts-24135-12".equals(maxCode) || "ts-24135-13".equals(maxCode)
        // || "ts-24135-14".equals(maxCode)) {
        // status.isPriceHalf = true;
        // }
        // if ("ts-24135-15".equals(maxCode) && payedVersionCount == 1) {
        // status.isJuwubaOnly = true;
        // }
        status.setVersion(max);

        return status;
    }

    private static void removeExpiredArticles(List<ArticleUserSubscribe> list, int length) {
        Date curr = new Date();
        for (int i = length - 1; i >= 0; i--) {
            ArticleUserSubscribe subsribe = list.get(i);
            Date deadline = subsribe.getDeadline();
            if (curr.getTime() > deadline.getTime()) {
                list.remove(i);
            }
        }
    }

    @JsonAutoDetect
    public static class SubscribeInfo {

        @JsonProperty
        int ver;

        @JsonProperty
        int left;

        public SubscribeInfo(int version, int left) {
            super();
            this.ver = version;
            this.left = left;
        }

        public static SubscribeInfo empty(User user) {
            return new SubscribeInfo(user.getVersion(), -1);
        }

        public int getVersion() {
            return ver;
        }

        public void setVersion(int version) {
            this.ver = version;
        }

        public int getLeft() {
            return left;
        }

        public void setLeft(int left) {
            this.left = left;
        }

    }

    public static class SubscribeStatus {

        public int version = Version.BLACK;

        public int payedDayLeftDay = 0;

        public boolean isPriceHalf = false;

        public boolean isJuwubaOnly = false;

        public SubscribeStatus(int version, int payedDayLeftDay) {
            super();
            this.version = version;
            this.payedDayLeftDay = payedDayLeftDay;
        }

        @Override
        public String toString() {
            return "SubscribeStatus [version=" + version + ", payedDayLeftDay=" + payedDayLeftDay + ", isPriceHalf="
                    + isPriceHalf + "]";
        }

        public SubscribeStatus() {
            super();
        }

        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        public int getPayedDayLeftDay() {
            return payedDayLeftDay;
        }

        public void setPayedDayLeftDay(int payedDayLeftDay) {
            this.payedDayLeftDay = payedDayLeftDay;
        }

        public boolean isPriceHalf() {
            return isPriceHalf;
        }

        public void setPriceHalf(boolean isPriceHalf) {
            this.isPriceHalf = isPriceHalf;
        }

    }

    public static SmsSendCount updateSmsCount(User user) {
        int verMax = Version.BLACK;
        long maxDeadLine = 0;
        int currLeft = -1;
        List<ArticleUserSubscribe> list = new VasApis.SubscribeGet(user.getUserNick(), APIConfig.get().getSubCode())
                .call();

        log.info("[list:]" + new Gson().toJson(list));
        if (CommonUtils.isEmpty(list)) {
            // 未查到订单记录
            SmsSendCount sms = new SmsSendCount(user.getId());
            sms.setTotal(SmsSendCount.DEFAULT_SMS_COUNT);
            sms.rawInsert();
            return sms;
        }

        long curr = System.currentTimeMillis();
        for (ArticleUserSubscribe sub : list) {
            if (sub.getDeadline().getTime() < curr) {
                continue;
            }

            if (sub.getDeadline().getTime() > maxDeadLine) {
                maxDeadLine = sub.getDeadline().getTime();
            }

            int ver = Subscribe.getVersionByCode(sub.getItemCode());

            if (ver > verMax) {
                int left = (int) ((sub.getDeadline().getTime() - curr) / DateUtil.DAY_MILLIS) + 1;
                if (verMax > Version.BLACK) {
                    currLeft = Math.max(currLeft, left);
                } else {
                    currLeft = left;
                }
                verMax = ver;

            } else if (ver == verMax) {
                int left = (int) ((sub.getDeadline().getTime() - curr) / DateUtil.DAY_MILLIS) + 1;
                currLeft = left > currLeft ? left : currLeft;
            }
        }

        SmsSendCount sms = SmsSendCount.findByUserId(user.getId());
        if (sms != null && sms.getDeadline() >= maxDeadLine) {
            // 不用重新更新
            return sms;
        }
        Long count = SmsSendCount.Version_MonthSmsCount_Map.get(user.getVersion());
        if (count == null || count <= 0) {
            count = 10L;
        }

        if (sms != null) {
            // 存在的，追加天数
            currLeft = (int) ((maxDeadLine - sms.getDeadline()) / DateUtil.DAY_MILLIS) + 1;
        }

        int month = currLeft / 31;
        if (currLeft % 31 > 0) {
            month += 1;
        }
        long total = count * month;
        if (month == 12 && user.getVersion() >= Version.HALL) {
            total = 500;
        }
        
        if (sms == null) {
            sms = new SmsSendCount(user.getId(), user.getUserNick(), total, maxDeadLine);
        } else {
            if (sms.getDeadline() > 0) {
                sms.setTotal(sms.getTotal() + total);
            }
            sms.setDeadline(maxDeadLine);
        }
        sms.jdbcSave();
        return sms;
    }

    public static SubscribeInfo getMax(User user) {
        int currMax = Version.BLACK;
        long maxDeadLine;
        int currLeft = -1;
        List<ArticleUserSubscribe> list = new VasApis.SubscribeGet(user.getUserNick(), APIConfig.get().getSubCode())
                .call();

        log.info("[list:]" + new Gson().toJson(list));
        if (CommonUtils.isEmpty(list)) {
            return new SubscribeInfo(Version.VIP, 1);
        }

        // log.error("[ sub ]" + new Gson().toJson(list));

        // log.error("[curr max : ]" + currMax);

        long curr = System.currentTimeMillis();
        for (ArticleUserSubscribe sub : list) {

            if (sub.getDeadline().getTime() < curr) {
                continue;
            }

            int ver = Subscribe.getVersionByCode(sub.getItemCode());

            if (ver > currMax) {
                int left = (int) ((sub.getDeadline().getTime() - curr) / DateUtil.DAY_MILLIS) + 1;
                if (currMax > Version.BLACK) {
                    currLeft = Math.max(currLeft, left);
                } else {
                    currLeft = left;
                }
                currMax = ver;

            } else if (ver == currMax) {
                int left = (int) ((sub.getDeadline().getTime() - curr) / DateUtil.DAY_MILLIS) + 1;
                currLeft = left > currLeft ? left : currLeft;
            }
        }

        // log.error("[final max : :]" + currMax);
        return new SubscribeInfo(currMax, currLeft);
    }

    public static boolean isVIP(User user) {
        return SubcribeAction.getMax(user).getVersion() > Version.BASE;
    }
}
