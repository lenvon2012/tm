package utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class TimeUtil {
	private static Logger log = LoggerFactory.getLogger(TimeUtil.class);
	
	/**
	 * 返回20121212格式的时间
	 * @param date
	 * @return
	 */
	public static int getDay(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		String dayStr = format.format(date);
        int day = Integer.parseInt(dayStr);
        return day;
	}
	
	public static int getHour(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("HH");
		String hourStr = format.format(date);
        int hour = Integer.parseInt(hourStr);
        return hour;
	}
	
	public static int addDay(int startDay, int dayNum) {
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
			Date date = format.parse(startDay + " 00:00:00");
			Calendar now = Calendar.getInstance();
			now.setTime(date);
			now.add(Calendar.DATE, dayNum);
			return getDay(now.getTime());
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return startDay;
		}
	}
	
	
	public static void main(String[] args) {
		long time = 1353730741000L;
		Date date = new Date(time);
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		String str = format.format(date);
		System.out.println(str);
		
		time = 1353125941000L;
		date = new Date(time);
		str = format.format(date);
		System.out.println(str);
	}
}
