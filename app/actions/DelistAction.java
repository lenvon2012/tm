
package actions;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;
import com.taobao.api.domain.Item;

public class DelistAction {

    private static final Logger log = LoggerFactory.getLogger(DelistAction.class);

    public static final String TAG = "DelistAction";

    public static class DelistDiag {
        /**
         * 方差
         */
        int variance;

        int inBadTimeCount;

        int[] weekDistributed = new int[7];

        public int getVariance() {
            return variance;
        }

        public void setVariance(int variance) {
            this.variance = variance;
        }

        public int[] getWeekDistributed() {
            return weekDistributed;
        }

        public void setWeekDistributed(int[] weekDistributed) {
            this.weekDistributed = weekDistributed;
        }

        public int getInBadTimeCount() {
            return inBadTimeCount;
        }

        public void setInBadTimeCount(int inBadTimeCount) {
            this.inBadTimeCount = inBadTimeCount;
        }

    }

    public static DelistDiag compute(List<Item> items) {
        if (CommonUtils.isEmpty(items)) {
            return null;
        }
        int size = items.size();
        long[] tsArr = new long[size];
        int count = 0;
        for (Item item : items) {
            if(item == null){
                continue;
            }
            tsArr[count++] = item.getDelistTime().getTime();
        }

        return compute(tsArr);
    }

    /**
     * keep no transaction...
     * @param tsList
     * @return 
     */
    public static DelistDiag compute(long[] tsList) {
        if (ArrayUtils.isEmpty(tsList)) {
            return null;
        }

        int itemNum = tsList.length;
        DelistDiag diagRes = new DelistDiag();
//        int[] distributedWeek = new int[7];

        for (Long delistTs : tsList) {
            int hourOfDay = DateUtil.getHourOfDay(delistTs);
            int minute = getMinuteOfHour(delistTs);

//            log.info("[date ]" + new Date(delistTs));
            if (!isGoodTime(hourOfDay, minute)) {
                diagRes.inBadTimeCount++;
            }

            /**
             * here, Sunday is the first of the week, we need to transfer it to 6...
             */
            int dayOfWeek = DateUtil.getDayOfWeek(delistTs);

//            log.info("[day of week :]" + dayOfWeek);

            diagRes.weekDistributed[(dayOfWeek + 6) % 7]++;
        }

        for (int count = 0; count < diagRes.weekDistributed.length; count++) {
            int i = diagRes.weekDistributed[count];
            int diff = ((i * 100) / itemNum) - 14;
            diagRes.variance += diff * diff;
        }

        log.error("[ week :]" + ArrayUtils.toString(diagRes.weekDistributed));
        log.info("variance : " + diagRes.variance);

        return diagRes;
    }

    public static boolean isGoodTime(int hour, int minute) {
        int value = hour * 100 + minute;
//        log.info("[value :]" + value);
        //log.error("11111111111111111111111111111111");
        //return (value >= 930 && value < 1130) || (value >= 1430 && value < 1730) || (value >= 1930 && value < 2300);
    
        //新的判断算法
        return (value >= 900 && value < 2400);
        
    }

//    public static void computeAsync(User user) {
//        new UserDiag.ComputeUserDelistTime(user).now();
//    }

    public static int getMinuteOfHour(long day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(day));
        return calendar.get(Calendar.MINUTE);
    }
}
