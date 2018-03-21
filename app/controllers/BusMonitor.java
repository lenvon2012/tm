package controllers;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import models.user.User;
import models.visit.VisitLog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.TimeUtil;

import com.ciaosir.client.utils.JsonUtil;


/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 12-11-11
 * Time: 下午2:29
 * To change this template use File | Settings | File Templates.
 */
public class BusMonitor extends TMController {
	
	private static final Logger log = LoggerFactory.getLogger(BusMonitor.class);
	
    public static void busMonitor() {
        render();
    }

    public static void busHourMonitor(Long numIid, Integer dayNum) {
    	User user = getUser();
        if (dayNum == null || dayNum <= 0)
            renderJSON("{}");
        Map<Integer, int[]> map = new HashMap<Integer, int[]>();
        Date date = new Date();
        int day = TimeUtil.getDay(date);
        int hour = TimeUtil.getHour(date);
        for (int i = 0; i < dayNum; i++) {
        	int[] hourArr = null;
        	if (i == 0) {
        		hourArr = queryHourMonitor(user.getId(), numIid, day - i, hour + 1);
        	} else {
        		hourArr = queryHourMonitor(user.getId(), numIid, day - i, 24);
        	}
        	map.put(i, hourArr);
        }
        String json = JsonUtil.getJson(map);
        renderJSON(json);
    }
    
    public static void busDayMonitor(Long numIid, Integer dayNum) {
    	User user = getUser();
        if (dayNum == null || dayNum <= 0)
            renderJSON("{}");
        Date date = new Date();
        int endDay = TimeUtil.getDay(date);
        int startDay = TimeUtil.addDay(endDay, 0 - dayNum);
        int[] dayArr = queryDayMonitor(user.getId(), numIid, startDay, endDay, dayNum);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("endDay", endDay + "");
        map.put("dayArr", dayArr);
        String json = JsonUtil.getJson(map);
        renderJSON(json);
    }

    private static int[] queryHourMonitor(Long userId, Long numIid, int day, int hourCount) {
        if (hourCount <= 0 || hourCount > 24)
        	return new int[0];
        try {
	    	String sql = "";
	    	if (numIid != null) {
	    		sql = "select count(*), hour from visitlog where  userId = ? and day = ? and curItemId = ? group by hour ";
	    	} else
	    		sql = "select count(*), hour from visitlog where  userId = ? and day = ? group by hour ";
	        
	    	
	    	Query query = null;
	        if (numIid != null) {
	        	query = VisitLog.em().createNativeQuery(sql)
		        		.setParameter(1, userId).setParameter(2, day).setParameter(3, numIid);
	        } else {
	        	query = VisitLog.em().createNativeQuery(sql)
		        		.setParameter(1, userId).setParameter(2, day);
	        }
	        
	        List<Object[]> objs = query.getResultList();

	        int[] hourArr = new int[hourCount];
	        for (int i = 0; i < hourCount; i++) {
	        	hourArr[i] = 0;
	        }
	        for (Object[] objects : objs) {
	            int count = Integer.parseInt(objects[0].toString());
	            int hour = (Integer)objects[1];
	            if (hour >= hourCount)
	            	continue;
	            hourArr[hour] = count;
	        }
	        return hourArr;
        } catch (Exception ex) {
        	log.error(ex.getMessage(), ex);
        	return new int[0];
        }
        
    }
    
    
    private static int[] queryDayMonitor(Long userId, Long numIid, int startDay, int endDay, int dayNum) {
    	try {
	    	String sql = "";
	    	if (numIid != null) {
	    		sql = "select count(*), day from visitlog where  userId = ? and day > ? and day <= ? and curItemId = ? group by hour ";
	    	} else
	    		sql = "select count(*), day from visitlog where  userId = ? and day > ? and day <= ? group by hour ";
	        
	    	
	    	Query query = null;
	        if (numIid != null) {
	        	query = VisitLog.em().createNativeQuery(sql)
		        		.setParameter(1, userId).setParameter(2, startDay).setParameter(3, endDay).setParameter(4, numIid);
	        } else {
	        	query = VisitLog.em().createNativeQuery(sql)
		        		.setParameter(1, userId).setParameter(2, startDay).setParameter(3, endDay);
	        }
	        
	        List<Object[]> objs = query.getResultList();

	        int[] dayArr = new int[dayNum];
	        for (int i = 0; i < dayNum; i++) {
	        	dayArr[i] = 0;
	        }
	        Map<String, Integer> dayMap = new HashMap<String, Integer>();
	        for (Object[] objects : objs) {
	            int count = Integer.parseInt(objects[0].toString());
	            int day = (Integer)objects[1];
	            dayMap.put(day + "", count);
	        }
	        for (int i = 0; i < dayNum; i++) {
	        	int day = TimeUtil.addDay(startDay, i + 1);
	        	Integer count = dayMap.get(day + "");
	        	if (count == null)
	        		dayArr[i] = 0;
	        	else 
	        		dayArr[i] = count;
	        }
	        return dayArr;
        } catch (Exception ex) {
        	log.error(ex.getMessage(), ex);
        	return new int[0];
        }
    }

}
