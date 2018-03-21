package job.carriertask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.carrierTask.CarrierItemPlay;
import models.carrierTask.CarrierItemPlay.CarrierItemBatchOper;
import models.carrierTask.CarrierItemPlay.CarrierItemPlatform;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import play.jobs.On;
import bustbapi.CarrierItemApi.itemSkuGet;
import bustbapi.CarrierItemApi.skusQuantityUpdate;

import com.taobao.api.domain.Item;
import com.taobao.api.domain.Sku;

import configs.TMConfigs.Server;
import controllers.APIConfig;
import dao.UserDao;

@On("0 0 2 * * ?")
//@Every ("30s")
public class CarrierItemSyncJob extends Job {
	
	private static final int APP = 21255586;

	private static final Logger log = LoggerFactory.getLogger(CarrierItemSyncJob.class);

	public void doJob() {
		if (!Server.jobTimerEnable) {
			return;
		}
		// 淘掌柜
		if (APIConfig.get().getApp() != APP) {
			return;
		}
		
		log.info("CarrierItemSyncJob launch!!");
		
		doItemSync();
	}
	
	private void doItemSync() {
		new CarrierItemBatchOper(32) {
			@Override
			public void doForEachCarrierItem(CarrierItemPlay item) {
				User user = UserDao.findById(item.getUserId());
				if(user == null) {
					log.error("CarrierItemSyncJob.doItemSync: user is null! userId:" + item.getUserId());
					return;
				}
				// 原商品历史库存
				String OriginSkuQuantities = item.getOriginSkuQuantities();
				// 原商品当前库存
				String currOriginSkuQuantities = getSkuQuantitiesStr(item.getOriginNumIid(), item.getOriginPlatform(), user);
				// 更新后的原商品库存
				String newOriginSkuQuantities = syncOriginSkuQuantities(OriginSkuQuantities, currOriginSkuQuantities);
				// 现商品库存
				String skuQuantities = item.getSkuQuantities();
				// 更新后的现商品库存
				String currSkuQuantities = syncSkuQuantities(skuQuantities, newOriginSkuQuantities);
				
				if(skuQuantities.equalsIgnoreCase(currSkuQuantities)) {
					log.info("CarrierItemSyncJob.doItemSync: item 【" + item.getNumIid() + "】 not need to update sku quantities");
					return;
				}
				String skuidQuantities = formatSkuidQuantities(currSkuQuantities);
				// 更新库存
				Boolean success = updateSkuQuantities(item.getNumIid(), item.getPlatform(), skuidQuantities, user);
				if(!success) {
					return;
				}
				item.setSkuQuantities(currSkuQuantities);
				item.setOriginSkuQuantities(newOriginSkuQuantities);
				item.jdbcSave();
			}
		}.call();
	}
	
	/**
	 * 将currSkuQuantities中的outerId去掉（更新库存接口中不需要）
	 */
	private String formatSkuidQuantities(String currSkuQuantities) {
		String skuidQuantities = StringUtils.EMPTY;
		
		String[] currSplit = currSkuQuantities.split(";");
		for (String s : currSplit) {
			String[] split = s.split(":");
			skuidQuantities += split[0] + ":" + split[1] + ";";
		}
		
		return skuidQuantities;
	}

	/**
	 * 更新原商品sku库存信息到最新
	 */
	private String syncOriginSkuQuantities(String OriginSkuQuantities, String currOriginSkuQuantities) {
		String skuQuantities = StringUtils.EMPTY;
		
		Map<String, String> skuMap = new HashMap<String, String>();
		String[] newSplit = currOriginSkuQuantities.split(";");
		for (String s : newSplit) {
			String[] split = s.split(":");
			skuMap.put(split[0], split[1]);
		}
		
		String[] oldSplit = OriginSkuQuantities.split(";");
		for (String s : oldSplit) {
			String[] split = s.split(":");
			String Quantities = skuMap.get(split[0]);
			if(StringUtils.isEmpty(Quantities)) {
				// sku未匹配到 库存设为0
				skuQuantities += split[0] + ":0" + ";";
			} else {
				// 同步匹配到的sku库存
				skuQuantities += split[0] + ":" + Quantities + ";";
			}
		}
		
		if(!StringUtils.isEmpty(skuQuantities)) {
			skuQuantities = skuQuantities.substring(0, skuQuantities.length() - 1);
		}
		
		return skuQuantities;
	}
	
	/**
	 * 更新现商品sku库存信息到最新
	 */
	private String syncSkuQuantities(String skuQuantities, String newOriginSkuQuantities) {
		String currSkuQuantities = StringUtils.EMPTY;
		
		Map<String, String> skuMap = new HashMap<String, String>();
		String[] newSplit = newOriginSkuQuantities.split(";");
		for (String s : newSplit) {
			String[] split = s.split(":");
			skuMap.put(split[0], split[1]);
		}
		
		String[] currSplit = skuQuantities.split(";");
		for (String s : currSplit) {
			String[] split = s.split(":");
			String Quantities = skuMap.get(split[2]);
			if(StringUtils.isEmpty(Quantities)) {
				// sku未匹配到 库存设为0
				currSkuQuantities += split[0] + ":0:" + split[2] + ";";
			} else {
				// 同步匹配到的sku库存
				currSkuQuantities += split[0] + ":" + Quantities + ":" + split[2] + ";";
			}
		}
		
		// 下面的是依据sku位置进行匹配同步 暂时弃用 改为outerId关联
		/*String[] split1 = skuQuantities.split(";");
		String[] split2 = newOriginSkuQuantities.split(";");
		
		for (int i = 0, l = split1.length; i < l; i++) {
			String[] s1 = split1[i].split(":");
			String[] s2 = split2[i].split(":");
			currSkuQuantities += s1[0] + ":" + s2[1] + ";";
		}*/
		
		if(!StringUtils.isEmpty(currSkuQuantities)) {
			currSkuQuantities = currSkuQuantities.substring(0, currSkuQuantities.length() - 1);
		}
		
		return currSkuQuantities;
	}
	
	/**
	 * 获取商品sku库存字符串
	 */
	private String getSkuQuantitiesStr(Long numIid, int platform, User user) {
		String skuQuantities = StringUtils.EMPTY;
		
		switch (platform) {
			// 淘宝
			case CarrierItemPlatform.TB:
				skuQuantities = getSkuQuantitiesStrForTB(numIid, user);
				break;
			// 京东
			case CarrierItemPlatform.JD:
				break;
			// 1688
			case CarrierItemPlatform.ALIBABA:
				break;
			// 拼多多
			case CarrierItemPlatform.PDD:
				break;
			// 淘宝
			default:
				skuQuantities = getSkuQuantitiesStrForTB(numIid, user);
				break;
		}
		
		return skuQuantities;
	}
	
	private String getSkuQuantitiesStrForTB(Long numIid, User user) {
		String skuQuantities = StringUtils.EMPTY;
		
		itemSkuGet itemSkuGet = new itemSkuGet(String.valueOf(numIid), user.getSessionKey());
		List<Sku> skus = itemSkuGet.call();
		if(skus == null) {
			return skuQuantities;
		}
		for (Sku sku : skus) {
			skuQuantities += sku.getSkuId() + ":" + sku.getQuantity() + ";";
		}
		if(!StringUtils.isEmpty(skuQuantities)) {
			skuQuantities = skuQuantities.substring(0, skuQuantities.length() - 1);
		}
		
		return skuQuantities;
	}
	
	/**
	 * 更新商品sku库存信息
	 */
	private Boolean updateSkuQuantities(Long numIid, int platform, String skuidQuantities, User user) {
		Boolean success = false;
		
		switch (platform) {
			// 淘宝
			case CarrierItemPlatform.TB:
				success = updateSkuQuantitiesForTB(numIid, skuidQuantities, user);
				break;
			// 京东
			case CarrierItemPlatform.JD:
				break;
			// 1688
			case CarrierItemPlatform.ALIBABA:
				break;
			// 拼多多
			case CarrierItemPlatform.PDD:
				break;
			// 淘宝
			default:
				success = updateSkuQuantitiesForTB(numIid, skuidQuantities, user);
				break;
		}
		
		return success;
	}
	
	private Boolean updateSkuQuantitiesForTB(Long numIid, String skuidQuantities, User user) {
		skusQuantityUpdate skusQuantityUpdate = new skusQuantityUpdate(numIid, skuidQuantities, user.getSessionKey());
		Item newItem = skusQuantityUpdate.call();
		if(newItem == null) {
			log.error("CarrierItemSyncJob.doItemSync: quantities update fail for TB item 【" + numIid + "】 error message:" + skusQuantityUpdate.getSubErrorMsg());
			return false;
		}
		log.info("CarrierItemSyncJob.doItemSync: quantities update success for TB item 【" + numIid + "】 skuidQuantities:" + skuidQuantities);
		return true;
	}
	
}
