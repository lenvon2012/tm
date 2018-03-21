package dao.autolist;

import java.util.Collection;
import java.util.List;

import models.autolist.AutoListLog;

public class AutoListLogDao {

	public static void saveOrUpdateAutoListLog(AutoListLog listLog) {
		listLog.jdbcSave();
	}
	
	public static long queryListLogNum(long userId, long planId, Collection<Long> numIidColl) {
		//String query = "userId = ? and listTime > 0";//好像差一点
		//return AutoListLog.count(query, userId);
		return AutoListLog.countByUserId(userId, planId, numIidColl);
	}
	
	public static List<AutoListLog> queryListLog(long userId, long planId, Collection<Long> numIidColl,
	        int startPage, int pageSize) {
		//String query = "userId = ? and listTime > 0 order by listTime desc";
		//return AutoListLog.find(query, userId).fetch(startPage, pageSize);
		return AutoListLog.findByUserId(userId, planId, numIidColl, startPage, pageSize);
	}
}
