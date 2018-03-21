package bustbapi.sync;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public abstract class DateUtils {

	public static Date parseDateTime(String str) throws ParseException {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return df.parse(str);
	}

	public static String formatDay(Date date) {
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		return df.format(date);
	}

	public static List<Date[]> splitTimeByDays(Date start, Date end, int days) {
		return splitTimeByHours(start, end, 24 * days);
	}

	public static List<Date[]> splitTimeByHours(Date start, Date end, int hours) {
		List<Date[]> dl = new ArrayList<Date[]>();
		while (start.compareTo(end) < 0) {
			Date _end = addHours(start, hours);
			if (_end.compareTo(end) > 0) {
				_end = end;
			}
			Date[] dates = new Date[] { (Date) start.clone(), (Date) _end.clone() };
			dl.add(dates);

			start = _end;
		}
		return dl;
	}

	public static Date addMinutes(Date date, int amount) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.MINUTE, amount);
		return c.getTime();
	}

	public static Date addHours(Date date, int amount) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.HOUR_OF_DAY, amount);
		return c.getTime();
	}

	public static Date addDays(Date date, int amount) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DAY_OF_MONTH, amount);
		return c.getTime();
	}

	public static Date addMonths(Date date, int amount) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.MONTH, amount);
		return c.getTime();
	}

	/**
	 * 获取今天的开始时刻。
	 */
	public static Date getTodayStartTime() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

}
