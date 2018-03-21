package job.autolist.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;

import models.autolist.AutoListTime;
import models.autolist.NoAutoListItem;
import models.item.ItemPlay;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.DateUtil;

import com.taobao.api.domain.Item;

import dao.item.ItemDao;

/**
 * 第一次的时候，计算宝贝上下架计划
 * @author Administrator
 *
 */
public class InitCalcuListTime {
	private static final Logger log = LoggerFactory.getLogger(InitCalcuListTime.class);
	
	private User user;
	public InitCalcuListTime(User user) {
		this.user = user;
	}
	
	
	public List<AutoListTime> calcuListTime(int distriType, String distriTime, String distriHours) {
		
		//先得到所有上线宝贝
		List<Item> itemList = ItemService.getAllOnSaleItems(user);
		Set<Long> noAutoListItems = NoAutoListItem.findIdsByUser(user.getId(), AutoListTime.DefaultPlanId);
		List<Item> toRemove = new ArrayList<Item>();
		for (Item item : itemList){
			if(noAutoListItems.contains(item.getNumIid())){
				toRemove.add(item);
			}
		}
		for (Item item : toRemove){
			itemList.remove(item);
		}
		
		List<AutoListTime> resultList = new ArrayList<AutoListTime>(); 
		//找到不需要重新分布的宝贝
		List<ItemPlay> itemPlayList = ItemDao.findByUserId(user.getId());
		List<ItemPlay> remainList = findNotReDistriItems(itemPlayList);
		//从宝贝中排除
		for (ItemPlay itemPlay : remainList) {
			long numIid = itemPlay.getNumIid();
			Item targetItem = null;
			for (Item item : itemList) {
				if (item.getNumIid().equals(numIid)) {
					targetItem = item;
					break;
				}
			}
			if (targetItem == null)
				continue;
			//如果找不到上架时间，还是要重新分布
			Date date = targetItem.getListTime();
            if (date == null)
                continue;
            //
            long time = date.getTime();
            int relativeHour = DateUtil.findEachHourInOneWeek(time);
            if (relativeHour >= 7 * 24) {
            	log.error("错误！！！小时数超出了7*24！！！！");
            	continue;
            }
            /*if (modifyDistri[relativeHour] > 0) {
            	//这个上架时间是不需要修改的
            	modifyDistri[relativeHour] = modifyDistri[relativeHour] - 1;
            }*/
            //新建一个AutoListTime
            long relativeListTime = findRelativeTime(time);
        	AutoListTime autoListTime = AutoListTime.createAutoListTime(user.getId(),
        			targetItem.getNumIid(), relativeListTime, AutoListTime.DefaultPlanId);
        	resultList.add(autoListTime);
        	itemList.remove(targetItem);
		}

		//在去除排名前10的宝贝后，得到好的分布安排
		GoodListDistriCalcu goodListDistriCalcu = new GoodListDistriCalcu(distriType, distriTime, distriHours);
		int[] goodDistri = goodListDistriCalcu.newGetGoodDistribute(itemList.size());
		
		
		//要被修改上架时间的宝贝
		List<Item> modifyItemList = new ArrayList<Item>();
		//要修改的时间段
		int[] modifyDistri = new int[7 * 24];
		for (int i = 0; i < 7 * 24; i++) {
			modifyDistri[i] = goodDistri[i];
		}
		
		
		
		//遍历所有宝贝
		for (Item item : itemList) {
			Date date = item.getListTime();
            long time = 0L;
            if (date != null)
                time = date.getTime();
            else {
            	modifyItemList.add(item);
            	continue;
            }
            int relativeHour = DateUtil.findEachHourInOneWeek(time);
            if (relativeHour >= 7 * 24) {
            	log.error("错误！！！小时数超出了7*24！！！！");
            	continue;
            }
            if (modifyDistri[relativeHour] > 0) {
            	//这个上架时间是不需要修改的
            	modifyDistri[relativeHour] = modifyDistri[relativeHour] - 1;
            	long relativeListTime = findRelativeTime(time);
            	AutoListTime autoListTime = AutoListTime.createAutoListTime(user.getId(),
            			item.getNumIid(), relativeListTime, AutoListTime.DefaultPlanId);
            	resultList.add(autoListTime);
            } else {
            	modifyItemList.add(item);
            }
		}
		//
		//int sum = 0;
		//for (int i = 0; i < 7 * 24; i++) {
		//	sum += modifyDistri[i];
		//}
		//if (sum != modifyItemList.size()) {
		//	log.error("错误！！！数量不一致！！！");
		//}
		//
		List<AutoListTime> modifyList = reDistributeListTime(modifyDistri, modifyItemList);
		
		resultList.addAll(modifyList);
		return resultList;
	}
	
	//找到不需要重新分布的items
	private List<ItemPlay> findNotReDistriItems(List<ItemPlay> itemPlayList) {
		List<ItemPlay> resultList = new ArrayList<ItemPlay>();
		
		// 按照上架时间排序
        Collections.sort(itemPlayList, new Comparator<ItemPlay>() {

            @Override
            public int compare(ItemPlay o1, ItemPlay o2) {
                // TODO Auto-generated method stub
            	Integer num1 = o1.getTradeItemNum();
                return 0 - num1.compareTo(o2.getTradeItemNum());

            }

        });
        
        for (int i = 0; i < 10; i++) {
        	if (i >= itemPlayList.size())
        		break;
        	ItemPlay itemPlay = itemPlayList.get(i);
        	if (itemPlay.getTradeItemNum() >= 10)
        		resultList.add(itemPlay);
        }
        
        return resultList;
	}
	

	
	private List<AutoListTime> reDistributeListTime(int[] modifyDistri, List<Item> modifyItemList) {
		//排除了一些热卖宝贝不重新分布后，modifyDistri和modifyItemList总数就不一定相同了，可能modifyDistri大于modifyItemList
		List<Long> newTimeList = new ArrayList<Long>();
		for (int i = 0; i < modifyDistri.length; i++) {
			if (modifyDistri[i] <= 0)
				continue;
			for (int j = 0; j < modifyDistri[i]; j++) {
				long relativeHour = i;
				long relativeTime = relativeHour * DateUtil.ONE_HOUR_MILLIS;
				relativeTime = randomOneHourTime(relativeTime);
				newTimeList.add(relativeTime);
			}
		}
		//if (newTimeList.size() != modifyItemList.size()) {
		//	log.error("错误！！！数量不一致！！！");
		//}
		List<AutoListTime> modifyList = new ArrayList<AutoListTime>();
		
		for (int i = 0; i < modifyItemList.size(); i++) {
			Item item = modifyItemList.get(i);
			if (i >= newTimeList.size())
				break;
			Long relativeTime = newTimeList.get(i);
			AutoListTime result = AutoListTime.createAutoListTime(user.getId(), 
					item.getNumIid(), relativeTime, AutoListTime.DefaultPlanId);
			modifyList.add(result);
		} 
		return modifyList;
	}
	
	//随机返回一个小时内的时间
	public static long randomOneHourTime(long start) {
		Random random = new Random();
		return start + random.nextInt(60) * 60 * 1000 + random.nextInt(60) * 1000;
	}
	
	//相对于这一周周日0点的时间
	private long findRelativeTime(long time) {
		long relativeTime = time - DateUtil.findThisWeekStart(time);
		return relativeTime;
	}
	
	
	public static void main(String[] args) {
		List<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < 100; i++) {
			list.add(new Random().nextInt(1000));
		}
		// 按照上架时间排序
        Collections.sort(list, new Comparator<Integer>() {

            @Override
            public int compare(Integer o1, Integer o2) {
                // TODO Auto-generated method stub
                return 0 - o1.compareTo(o2);

            }

        });
        for (Integer i : list) {
        	log.info(i + "");
        }
	}
}
