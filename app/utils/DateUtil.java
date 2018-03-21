package utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateUtil {
    public static final Logger log= LoggerFactory.getLogger(DateUtil.class);

    public static final long DAY_MILLIS = 24 * 3600L * 1000L;

    // public static final long HOUR_MILLIS = 3600L * 1000L;

    public static final long TWO_DAY_MILLIS = DAY_MILLIS << 1;

    public static final long TIME_ZONE_DIFF = 8 * 3600L * 1000L;

    public static long MAX_INTERVAL_DATA_AVAILABLE = 7 * 24 * 3600 * 1000L;

    public static long WEEK_MILLIS = 7 * 24 * 3600 * 1000L;

    public static long HOUR_MILLS = 3600 * 1000L;
    
    public static long DEFAULT_MILLIS_SPAN = WEEK_MILLIS;

    public static long TRIPPLE_DAY_MILLIS_SPAN = 3 * 24 * 3600 * 1000L;

    public static long TWO_WEEK_SPAN = 14 * DAY_MILLIS;

    public static long FOUR_DAYS = 4 * DAY_MILLIS;
    
    public static long FIFTEEN_DAYS = 15 * DAY_MILLIS;
    
    public static long SIXTEEN_DAYS = 16 * DAY_MILLIS;

    public static long THIRTY_DAYS = 30 * DAY_MILLIS;
    
    public static long NINTY_DAYS = 90 * DAY_MILLIS;

    public static long TEN_SECONDS_MILLIS = 10 * 1000L;

    public static long THREE_SECONDS_MILLIS = 3 * 1000L;

    public static long ONE_MINUTE_MILLIS = 60 * 1000L;

    public static long ONE_HOUR_MILLIS = 60 * 60 * 1000L;
    
    public static long FIVE_HOUR_MILLIS = 5 * 60 * 60 * 1000L;
    
    public static long NINE_HOUR_MILLIS = 9 * 60 * 60 * 1000L;

    public static long ACOOKIE_TIME_ADJUST = 20 * 60 * 1000L;

    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static final SimpleDateFormat ymdsdf = new SimpleDateFormat("yyyy-MM-dd");

    public static String formDateForLog(long time) {
        return sdf.format(time);
    }

    public static long formDailyTimestamp(Date date) {
        return formDailyTimestamp(date.getTime());
    }

    public static long formCurrDate() {
        return formDailyTimestamp(System.currentTimeMillis());
    }

    public static long formYestadyMillis() {
        return formDailyTimestamp(System.currentTimeMillis() - DateUtil.DAY_MILLIS);
    }

    //返回ts时刻当天0点的毫秒数
    public static long formDailyTimestamp(long ts) {
        return ((ts + TIME_ZONE_DIFF) / DAY_MILLIS) * DAY_MILLIS - TIME_ZONE_DIFF;
    }

    // public static long formCurrHour() {
    // return formHourTimestamp(System.currentTimeMillis());
    // }

    // public static long formHourTimestamp(long ts) {
    // return ((ts + TIME_ZONE_DIFF) / HOUR_MILLIS) * HOUR_MILLIS -
    // TIME_ZONE_DIFF;
    // }

    public static long formNextDate(long ts) {
        return formDailyTimestamp(ts) + DAY_MILLIS - 1L;
    }

    public static int getCurrHour() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    public static int getHourOfDay(long day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(day));
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    public static int getDayOfWeek(long day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(day));
        return calendar.get(Calendar.DAY_OF_WEEK) - 1;
    }

    public static int getDayOfMonth(long day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(day));
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    public static long getUdpMaxAvailabe(long today) {
        return today - SIXTEEN_DAYS;
    }

    public static long getTargetUdpMillisForTodayCompute() {
        long curr = System.currentTimeMillis();
        long target = 0L;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(curr);
        if (cal.get(Calendar.HOUR_OF_DAY) < 9) {
            target = DateUtil.formCurrDate() - DateUtil.DAY_MILLIS;
        } else {
            target = DateUtil.formCurrDate();
        }

        return target;
    }

    public static long getTargetUdpMillisForTodayPreview() {
        long curr = System.currentTimeMillis();
        long target = 0L;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(curr);
        if (cal.get(Calendar.HOUR_OF_DAY) < 10) {
            target = DateUtil.formCurrDate() - DateUtil.TWO_DAY_MILLIS;
        } else {
            target = DateUtil.formCurrDate() - DateUtil.DAY_MILLIS;
        }

        return target;
    }

    // public static long getTargetUdpMillisForCurrent() {
    // long curr = System.currentTimeMillis();
    // long target = 0L;
    // Calendar cal = Calendar.getInstance();
    // cal.setTimeInMillis(curr);
    // if (cal.get(Calendar.HOUR_OF_DAY) < 9) {
    // target = DateUtil.formCurrDate() - DateUtil.TWO_DAY_MILLIS;
    // } else {
    // target = DateUtil.formCurrDate() - DateUtil.DAY_MILLIS;
    // }
    //
    // return target;
    // }

    public static boolean isBeforeElevenClock() {
        return false;
    }
    
    
    /**
     * @param start
     * @return 返回start时刻属于一周的第几个小时，周日为一周第一天
     */
    public static int findEachHourInOneWeek(long start) {
        // TODO Auto-generated method stub

        long thisWeekStart = findThisWeekStart(start);

        return (int) ((start - thisWeekStart) / utils.DateUtil.ONE_HOUR_MILLIS);

    }

    /**
     * @param start
     * @return 返回本周第一天（周日为第一天）零点的毫秒数
     */
    public static long findThisWeekStart(long start) {
        
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(DateUtil.formDailyTimestamp(start));
        
   //     log.info("before: "+sdf.format(new Date(cal.getTimeInMillis())));
 
        int dayOfWeek=cal.get(Calendar.DAY_OF_WEEK);
   //     log.info("dayOfWeek: "+dayOfWeek);
        
        

        long thisWeekStart = cal.getTimeInMillis()-(dayOfWeek-1)*DAY_MILLIS;
        

 //       log.info("date: "+sdf.format(new Date(start))+",this weekStart: "+sdf.format(new Date(thisWeekStart)));
        return thisWeekStart;
    }
    
    
    /**
     * @return 返回指定时刻的下个整点时间点
     */
    public static long findNextHour(long startPoint) {
        // TODO Auto-generated method stub
        return startPoint - startPoint % utils.DateUtil.ONE_HOUR_MILLIS + utils.DateUtil.ONE_HOUR_MILLIS;

    }

    /**
     * timestamp是否大于或等于timestamp对应那天早上8点的时间戳
     * @return T|F
     */
    public static Boolean isTimestampGTEight(Long timestamp) {
        long timestampOfEight = formDailyTimestamp(timestamp) + 8 * HOUR_MILLS;
        if (timestamp < timestampOfEight) {
            return false;
        } else {
            return true;
        }
    }
}
