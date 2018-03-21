package job.autolist;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import job.autolist.service.GoodListDistriCalcu;
import job.autolist.service.InitCalcuListTime;
import job.autolist.service.ModifyListTime;
import models.autolist.AutoListConfig;
import models.autolist.AutoListRecord;
import models.autolist.AutoListTime;
import models.autolist.NoAutoListItem;
import models.item.ItemPlay;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import utils.DateUtil;

import com.ciaosir.client.CommonUtils;

import dao.UserDao;
import dao.autolist.AutoListRecordDao;
import dao.autolist.AutoListTimeDao;
import dao.item.ItemDao;

/**
 * 处理用户新增或删除的宝贝的上下架
 * @author Administrator
 *
 */
public class AutoListRefreshItemJob extends Job {
	private static final Logger log = LoggerFactory.getLogger(AutoListRefreshItemJob.class);
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final String JOB_NAME = AutoListRefreshItemJob.class.getSimpleName();

	
	
	private static Set<Long> excludeUserIdSet = new HashSet<Long>();
	
	static {
	    excludeUserIdSet.add(280840037L);//懒懒0落花 用户, 不要更新上下架的
	}
	
	
	public void doJob() {
		long startTime = System.currentTimeMillis();
		log.info("job: " + JOB_NAME + " start: " + sdf.format(new Date()));
		
		/*
		List<AutoListRecord> recordList = AutoListRecordDao.queryAllValidListRecord();
		
		for (AutoListRecord record : recordList) {
			if (record == null)
				continue;
			//RefreshOneUserAutoList refreshOneUser = new RefreshOneUserAutoList(record);
			//AutoListDoingJob.pool.submit(refreshOneUser);
			
		}
		*/
		
		long endTime = System.currentTimeMillis();
		log.info("job: " + JOB_NAME + " end: " + sdf.format(new Date()));
		log.info("job: " + JOB_NAME + " used: " + (endTime - startTime) / 1000 + "秒");
		
	}
	
	public static class RefreshOneUserAutoList {
		private AutoListRecord record;
		//private User user;
		private long userId;
		
		public RefreshOneUserAutoList(AutoListRecord record) {
			this.record = record;
			if (record == null)
				return;
			userId = record.getUserId();
		}
		
		//从itemplay数据库中得到当前所有onsale的宝贝
		private Set<Long> getAllOnSaleItems() {
			return ItemDao.allSaleItemIds(userId);
		}
		
		//从当前计划中删除不存在allItemList中的宝贝
		private List<AutoListTime> deleteItems(List<AutoListTime> autoListTimeList, 
				Set<Long> allItemList) {
			List<AutoListTime> deleteList = new ArrayList<AutoListTime>();
			for (AutoListTime autoListTime : autoListTimeList) {
				boolean isExist = false;
				long numIid = autoListTime.getNumIid();
				for (Long itemNumIid : allItemList) {
					if (itemNumIid.equals(numIid)) {
						isExist = true;
						break;
					}
				}
				if (isExist == true)
					continue;
				else
					deleteList.add(autoListTime);
			}
			for (AutoListTime autoListTime : deleteList) {
				autoListTimeList.remove(autoListTime);
				AutoListTimeDao.deleteAutoListTime(autoListTime);
			}

			return autoListTimeList;
		}
		
		//找到要新增的宝贝
		private List<Long> findNewItems(List<AutoListTime> autoListTimeList,
				Set<Long> allItemList) {
			List<Long> newItemList = new ArrayList<Long>();
			for (Long itemNumIid : allItemList) {
				boolean isExist = false;
				for (AutoListTime autoListTime : autoListTimeList) {
					if (autoListTime.getNumIid().equals(itemNumIid)) {
						isExist = true;
						break;
					}
				}
				if (isExist == true)
					continue;
				newItemList.add(itemNumIid);
			}
			return newItemList;
		}
		
		//获取当前的分布
		public static int[] getNowDistri(List<AutoListTime> autoListTimeList) {
			int[] nowDistri = new int[7 * 24];
			for (int i = 0; i < nowDistri.length; i++) {
				nowDistri[i] = 0;
			}
			for (AutoListTime autoListTime : autoListTimeList) {
				long relativeTime = autoListTime.getRelativeListTime();
				int relativeHour = (int)(relativeTime / DateUtil.ONE_HOUR_MILLIS);
				nowDistri[relativeHour]++;
			}
			return nowDistri;
		}
		
		private void addToNoAutoListItems(List<Long> newItemList) {
		    if (CommonUtils.isEmpty(newItemList)) {
		        return;
		    }
		    
		    for (Long numIid : newItemList) {
		        NoAutoListItem item = new NoAutoListItem(userId, numIid, AutoListTime.DefaultPlanId);
		        item.jdbcSave();
		    }
		    
		}
		
        public Boolean doUpdate() {
			try {
				if (record == null)
					return true;
				
				
				//当前所有在卖宝贝
				//long oldUpdateTime = record.getUpdateTime();
				Set<Long> allItemList = getAllOnSaleItems();
				Set<Long> noAutoListItems = NoAutoListItem.findIdsByUser(userId, AutoListTime.DefaultPlanId);
				for (Long numIid : noAutoListItems){
					if(allItemList.contains(numIid)){
						allItemList.remove(numIid);
					}
				}
				
				//得到当前计划
				List<AutoListTime> autoListTimeList = AutoListTimeDao.queryAllAutoListTime(userId);
				//从当前计划中删除不存在allItemList中的宝贝
				autoListTimeList = deleteItems(autoListTimeList, allItemList);
				//找到要新增的宝贝
				List<Long> newItemList = findNewItems(autoListTimeList, allItemList);
				if (newItemList == null || newItemList.isEmpty()) {
					
				} else {
				    
				    //加入到排除宝贝
				    if (excludeUserIdSet.contains(record.getUserId())) {
	                    addToNoAutoListItems(newItemList);
	                    
	                } else {
	                    //得到好的分布
	                    int totalSize = newItemList.size() + autoListTimeList.size();
	                    int distriType = record.getDistriType();
	                    GoodListDistriCalcu gdc = new GoodListDistriCalcu(distriType, record.getDistriTime(), record.getDistriHours());
	                    int[] goodDistri = gdc.newGetGoodDistribute(totalSize);
	                    int[] nowDistri = getNowDistri(autoListTimeList);
	                    int[] diffArray = new int[7 * 24];
	                    for (int i = 0; i < goodDistri.length; i++) {
	                        diffArray[i] = goodDistri[i] - nowDistri[i];
	                    }
	                    
	                    User user = UserDao.findById(userId);
	                    
	                    //加入新的
	                    if (AutoListConfig.isRemainDelist(userId)) {
	                        for (long itemNumIid : newItemList) {
	                        
	                            if (user == null) {
	                                continue;
	                            }
	                            ItemPlay item = ItemDao.findByNumIid(userId, itemNumIid);
	                            
	                            if (item == null) {
	                                continue;
	                            }
	                            BackToOldDelistAction.createNewAutoListTime(user, item);
	                            
	                        }
	                    } else {
	                        for (long itemNumIid : newItemList) {
	                            int relativeHour = ModifyListTime.findGoodPosition(diffArray);
	                            long relativeTime = relativeHour * DateUtil.ONE_HOUR_MILLIS;
	                            relativeTime = InitCalcuListTime.randomOneHourTime(relativeTime);
	                            AutoListTime result = AutoListTime.createAutoListTime(userId, 
	                                    itemNumIid, relativeTime, AutoListTime.DefaultPlanId);
	                            AutoListTimeDao.saveOrUpdateAutoListTime(result);
	                        }
	                        
	                    }
	                }
	                
					
				}
				
				
				//设置新的更新时间
				record.setUpdateTime(System.currentTimeMillis());
				
				List<AutoListTime> timeList = AutoListTimeDao.queryAllAutoListTime(userId);
				ModifyListTime.setSchedule(record, timeList);
				
				AutoListRecordDao.saveOrUpdateAutoListRecord(record);
			} catch (Exception ex) {
				if (record != null)
					log.error("为用户" + record.getUserId() + "刷新上下架失败");
				log.error(ex.getMessage(), ex);
				return false;
			}
			return true;
		}
		
	};
}
