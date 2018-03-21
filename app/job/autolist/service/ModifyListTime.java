package job.autolist.service;

import java.util.List;
import java.util.Random;

import job.autolist.AutoListRefreshItemJob.RefreshOneUserAutoList;
import job.autolist.BackToOldDelistAction;
import models.autolist.AutoListConfig;
import models.autolist.AutoListRecord;
import models.autolist.AutoListTime;
import models.item.ItemPlay;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.DateUtil;

import com.ciaosir.client.utils.NumberUtil;

import dao.UserDao;
import dao.autolist.AutoListRecordDao;
import dao.autolist.AutoListTimeDao;
import dao.item.ItemDao;

public class ModifyListTime {
	public final static Logger log = LoggerFactory.getLogger(ModifyListTime.class);
	
	public static void setSchedule(AutoListRecord record, List<AutoListTime> timeList) {
		int[] nowDistri = RefreshOneUserAutoList.getNowDistri(timeList);
		String schedule = "";
		for (int i = 0; i < nowDistri.length; i++) {
			if (!StringUtils.isEmpty(schedule))
				schedule += ",";
			schedule += nowDistri[i];
		}
		record.setAutoListSchedule(schedule);
	}
	
	public static void deleteFromSchedule(long userId, int[] deleteHourNumArray) {
		AutoListRecord record = AutoListRecordDao.findAutoListRecordByUserId(userId);
		if (record == null)
			return;
		String schedule = record.getAutoListSchedule();
		if (StringUtils.isEmpty(schedule)) {
			List<AutoListTime> timeList = AutoListTimeDao.queryAllAutoListTime(userId);
			ModifyListTime.setSchedule(record, timeList);
			schedule = record.getAutoListSchedule();
			AutoListRecordDao.saveOrUpdateAutoListRecord(record);
			return;
		}
		
		String[] nowDistri = schedule.split(",");
		if (nowDistri.length != 7 * 24)
			log.error("上下架计划竟然不是 7 * 24小时的");
		
		for (int i = 0; i < deleteHourNumArray.length; i++) {
			if (deleteHourNumArray[i] <= 0)
				continue;
			int num = NumberUtil.parserInt(nowDistri[i], 0);
			num = num - deleteHourNumArray[i];
			nowDistri[i] = num + "";
		}
		
		schedule = "";
		for (int i = 0; i < nowDistri.length; i++) {
			if (!StringUtils.isEmpty(schedule))
				schedule += ",";
			schedule += nowDistri[i];
		}
		record.setAutoListSchedule(schedule);
		AutoListRecordDao.saveOrUpdateAutoListRecord(record);
	}
	
	public static void addToSchedule(long userId, long numIid) {
		AutoListRecord record = AutoListRecordDao.findAutoListRecordByUserId(userId);
		if (record == null)
			return;
		String schedule = record.getAutoListSchedule();
		if (StringUtils.isEmpty(schedule)) {
			List<AutoListTime> timeList = AutoListTimeDao.queryAllAutoListTime(userId);
			ModifyListTime.setSchedule(record, timeList);
			schedule = record.getAutoListSchedule();
		}
		
		int relativeHour = 0;
		String[] nowDistriStr = schedule.split(",");
		if (nowDistriStr.length != 7 * 24)
            log.error("上下架计划竟然不是 7 * 24小时的");
        int[] nowDistri = new int[7 * 24];
		
        int totalSize = 0;
        for (int i = 0; i < nowDistriStr.length; i++) {
            if (i >= 7 * 24)
                break;
            nowDistri[i] = NumberUtil.parserInt(nowDistriStr[i], 0);
            totalSize += nowDistri[i];
        }
        
		if (AutoListConfig.isRemainDelist(userId) == true) {
		    User user = UserDao.findById(userId);
		    if (user == null) {
		        return;
		    }
		    ItemPlay item = ItemDao.findByNumIid(userId, numIid);
		    
		    if (item == null) {
		        return;
		    }
		    
		    AutoListTime newListTime = BackToOldDelistAction.createNewAutoListTime(user, item);
		    if (newListTime == null) {
		        return;
		    }
		    
		    relativeHour = DateUtil.findEachHourInOneWeek(item.getDeListTime());
		    
		} else {
		    
	        //得到好的分布
	        totalSize = totalSize + 1;
	        int distriType = record.getDistriType();
	        GoodListDistriCalcu gdc = new GoodListDistriCalcu(distriType, record.getDistriTime(), record.getDistriHours());
	        int[] goodDistri = gdc.newGetGoodDistribute(totalSize);
	        
	        int[] diffArray = new int[7 * 24];
	        for (int i = 0; i < goodDistri.length; i++) {
	            diffArray[i] = goodDistri[i] - nowDistri[i];
	        }

	        relativeHour = findGoodPosition(diffArray);
	        
	        long relativeTime = relativeHour * DateUtil.ONE_HOUR_MILLIS;
	        relativeTime = InitCalcuListTime.randomOneHourTime(relativeTime);
	        AutoListTime result = AutoListTime.createAutoListTime(userId, 
	                numIid, relativeTime, AutoListTime.DefaultPlanId);
	        AutoListTimeDao.saveOrUpdateAutoListTime(result);
		}
		
		
		
		
		
		schedule = "";
		nowDistri[relativeHour]++;
		for (int i = 0; i < nowDistri.length; i++) {
			if (!StringUtils.isEmpty(schedule))
				schedule += ",";
			schedule += nowDistri[i];
		}
		record.setAutoListSchedule(schedule);
		AutoListRecordDao.saveOrUpdateAutoListRecord(record);
	}
	
	
	public static void modifySchedule(long userId, long oldListTime, long newListTime) throws Exception {
		AutoListRecord record = AutoListRecordDao.findAutoListRecordByUserId(userId);
		if (record == null)
			return;
		String schedule = record.getAutoListSchedule();
		if (StringUtils.isEmpty(schedule)) {
			List<AutoListTime> timeList = AutoListTimeDao.queryAllAutoListTime(userId);
			ModifyListTime.setSchedule(record, timeList);
			schedule = record.getAutoListSchedule();
			AutoListRecordDao.saveOrUpdateAutoListRecord(record);
			return;
		}
		
		String[] nowDistri = schedule.split(",");
		if (nowDistri.length != 7 * 24)
			log.error("上下架计划竟然不是 7 * 24小时的");
		
		//得到小时数
		int oldHour = (int)(oldListTime / DateUtil.ONE_HOUR_MILLIS);
		int newHour = (int)(newListTime / DateUtil.ONE_HOUR_MILLIS); 
		
		int oldHourNum = NumberUtil.parserInt(nowDistri[oldHour], 0);
		if (oldHourNum > 0)
			oldHourNum--;
		nowDistri[oldHour] = String.valueOf(oldHourNum);
		
		int newHourNum = NumberUtil.parserInt(nowDistri[newHour], 0); 
		newHourNum++;
		nowDistri[newHour] = String.valueOf(newHourNum);
		
		
		schedule = "";
		for (int i = 0; i < nowDistri.length; i++) {
			if (!StringUtils.isEmpty(schedule))
				schedule += ",";
			schedule += nowDistri[i];
		}
		record.setAutoListSchedule(schedule);
		AutoListRecordDao.saveOrUpdateAutoListRecord(record);
	}
	
	public static int findGoodPosition(int[] diffArray) {
		if (diffArray == null || diffArray.length != 7 * 24)
			return new Random().nextInt(7 * 24);
		
		int[] dayArray = new int[7];
		int dayMax = 0;
		int maxDayIndex = 0;
		for (int i = 0; i < 7; i++) {
			dayArray[i] = 0;
			for (int j = 0; j < 24; j++) {
				dayArray[i] += diffArray[i * 24 + j];
			}
			if (dayArray[i] > dayMax) {
				dayMax = dayArray[i];
				maxDayIndex = i;
			}
		}
		
		int[] dailyPartArray = new int[3];
		for (int i = 0; i < 3; i++) {
			dailyPartArray[i] = 0;
		}
		for (int i = 0; i < 24; i++) {
			if (i <= GoodListDistriCalcu.dailyPartTime[0]) {
				dailyPartArray[0] += diffArray[maxDayIndex * 24 + i];
			} else if (i <= GoodListDistriCalcu.dailyPartTime[1]) {
				dailyPartArray[1] += diffArray[maxDayIndex * 24 + i];
			} else {
				dailyPartArray[2] += diffArray[maxDayIndex * 24 + i];
			}
		}
		int partMax = 0;
		int partIndex = 0;
		for (int i = 0; i < 3; i++) {
			if (dailyPartArray[i] > partMax) {
				partMax = dailyPartArray[i];
				partIndex = i;
			}
		}
		int startHour = maxDayIndex * 24;
		if (partIndex > 0)
			startHour += GoodListDistriCalcu.dailyPartTime[partIndex - 1] + 1;
		int endHour = maxDayIndex * 24 + GoodListDistriCalcu.dailyPartTime[partIndex] + 1;
		int max = 0;
		int maxHourIndex = startHour;
		for (int i = startHour; i < endHour; i++) {
			if (diffArray[i] > max) {
				max = diffArray[i];
				maxHourIndex = i;
			}
		}
		
		diffArray[maxHourIndex]--;
		return maxHourIndex;
	}
	
}
