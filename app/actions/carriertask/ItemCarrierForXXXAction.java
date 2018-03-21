package actions.carriertask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.carrierTask.CarrierTaskForXXX;
import models.carrierTask.CarrierTaskForXXX.CarrierTaskForXXXType;
import models.carrierTask.ItemImgForXXX;
import models.carrierTask.ItemImgForXXX.batchOpType;
import models.carrierTask.SubCarrierTaskForXXX;
import models.carrierTask.SubCarrierTaskForXXX.SubCarrierTaskForXXXStatus;
import models.user.User;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import result.TMResult;
import actions.ItemGetAction;
import bustbapi.ItemApi.ItemImgPictureAdd;
import bustbapi.ItemApi.ItemPropPictureAdd;
import bustbapi.ItemCarryApiForXXX;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.ciaosir.client.CommonUtils;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.ItemImg;
import com.taobao.api.domain.PropImg;

import configs.TMConfigs;
import dao.UserDao;

public class ItemCarrierForXXXAction {
	
	private static final Logger log = LoggerFactory.getLogger(ItemCarrierForXXXAction.class);
	
	/**
	 * 商品复制-解析任务创建参数
	 */
	public static CarryParam parseCarryJson(String paramJson) {
		CarryParam param = new CarryParam();
		
		if(StringUtils.isEmpty(paramJson)) {
			return param;
		}
		
		try {
			JSONObject jsonObject = JSON.parseObject(paramJson);
			
			String sid = jsonObject.getString("sid");
			if(StringUtils.isEmpty(sid)) {
				return param;
			}
			int type = jsonObject.getInteger("type");
			if(type != CarrierTaskForXXXType.SINGLE && type != CarrierTaskForXXXType.SHOP && type != CarrierTaskForXXXType.BATCH) {
				return param;
			}
			String ww = jsonObject.getString("ww");
			
			List<CarryItem> items = new ArrayList<CarryItem>();
			
			JSONArray jsonArray = jsonObject.getJSONArray("items");
			for (int i = 0, l = jsonArray.size(); i < l; i++) {
				JSONObject object = jsonArray.getJSONObject(i);
				Long numIid = object.getLong("numIid");
				int source = object.getInteger("source");
				int target = object.getInteger("target");
				if(numIid <= 0 || source <= 0 || target <= 0) {
					continue;
				}
				items.add(new CarryItem(numIid, source, target));
			}
			
			if(CommonUtils.isEmpty(items)) {
				return param;
			}
			
			param = new CarryParam(sid, type, ww, items);
			
			return param;
			
		} catch (JSONException e) {
			log.error(e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 商品复制-解析图片回传参数
	 */
	public static List<ItemImgForXXX> parseImgsJson(String imgsJson) {
		
		List<ItemImgForXXX> imgs = new ArrayList<ItemImgForXXX>();
		
		if(StringUtils.isEmpty(imgsJson)) {
			return imgs;
		}
		
		Long updateTs = System.currentTimeMillis();
		
		try {
			JSONArray jsonArray = JSON.parseArray(imgsJson);
			
			for (int i = 0, l = jsonArray.size(); i < l; i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				
				Long id = jsonObject.getLong("id");
				Long subTaskId = jsonObject.getLong("subTaskId");
				String oldUrl = jsonObject.getString("oldUrl");
				String newUrl = jsonObject.getString("newUrl");
				Long createTs = jsonObject.getLong("createTs");
				
				ItemImgForXXX img = new ItemImgForXXX(subTaskId, oldUrl, newUrl, createTs, updateTs);
				img.setId(id);
				
				imgs.add(img);
			}
			
			if(CommonUtils.isEmpty(imgs)) {
				return imgs;
			}
			
			return imgs;
			
		} catch (JSONException e) {
			log.error(e.getMessage());
			e.printStackTrace();
			return null;
		}
		
	}
	
	/**
	 * 商品复制-数据准备
	 */
	public static void doItemPrePare(final Long taskId) {
		List<SubCarrierTaskForXXX> subTasks = SubCarrierTaskForXXX.findByTaskId(taskId);
		if(CommonUtils.isEmpty(subTasks)) {
			log.error("任务【" + taskId + "】查询不到相关子任务！！！");
			return;
		}
		
		for (SubCarrierTaskForXXX subTask : subTasks) {
			final Long subTaskId = subTask.getId();
			
			TMConfigs.getItemInfoForXXXPool().submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					Boolean success = getItemInfo(subTaskId);
					if(!success) {
						CarrierTaskForXXX.addOneFinishCount(taskId, success);
					}
					return null;
				}
			});
		}
	}
	
	/**
	 * 商品复制-获取商品图片
	 */
	public static Boolean getItemInfo(Long subTaskId) {
		SubCarrierTaskForXXX subTask = SubCarrierTaskForXXX.findById(subTaskId);
		Long numIid = subTask.getNumIid();
		
		try{
			String wapUrl = "http://hws.m.taobao.com/cache/wdetail/5.0/?id=" + numIid + "";
			JSONObject data = ItemGetAction.getDataObject(wapUrl, "http://hws.m.taobao.com");

			if (data == null) {
				SubCarrierTaskForXXX.updateSubTask(StringUtils.EMPTY, StringUtils.EMPTY, SubCarrierTaskForXXXStatus.FAIL, "代理异常，获取宝贝相关信息失败", subTask);
				return false;
			}
			
			List<ItemImgForXXX> imgs = new ArrayList<ItemImgForXXX>();
			
			JSONObject itemInfoModel = data.getJSONObject("itemInfoModel");
			String title = itemInfoModel.getString("title");
			String picUrl = itemInfoModel.getJSONArray("picsPath").getString(0);

			JSONArray picsPath = itemInfoModel.getJSONArray("picsPath");
			for (int i = 0, l = picsPath.size(); i < l; i++) {
				String imgUrl = picsPath.getString(i);
				if(!StringUtils.isEmpty(imgUrl)) {
					ItemImgForXXX img = new ItemImgForXXX();
					img.setOldUrl(imgUrl);
					imgs.add(img);
				}
			}

			boolean hasSku = itemInfoModel.getBoolean("sku");
			JSONArray skuProps = null;
			if (hasSku) {
				JSONObject skuModel = data.getJSONObject("skuModel");
				skuProps = skuModel.getJSONArray("skuProps");
			}
			if (skuProps != null && !skuProps.isEmpty()) {
				for (int i = 0, sl = skuProps.size(); i < sl; i++) {
					JSONObject propJsonObj = skuProps.getJSONObject(i);
					JSONArray propDetails = propJsonObj.getJSONArray("values");

					for (int j = 0, pl = propDetails.size(); j < pl; j++) {
						JSONObject detail = propDetails.getJSONObject(j);
						String imgUrl = detail.getString("imgUrl");
						if (!StringUtils.isEmpty(imgUrl)) {
							ItemImgForXXX img = new ItemImgForXXX();
							img.setOldUrl(imgUrl);
							imgs.add(img);
						}
					}
				}
			}

			JSONObject descInfo = data.getJSONObject("descInfo");
			String fullDescUrl = descInfo.getString("fullDescUrl");
			
			JSONObject jsonObject = ItemGetAction.getDataObject(fullDescUrl, "http://hws.m.taobao.com");
			if (jsonObject == null) {
				SubCarrierTaskForXXX.updateSubTask(StringUtils.EMPTY, StringUtils.EMPTY, SubCarrierTaskForXXXStatus.FAIL, "代理异常，未查询到宝贝的描述信息", subTask);
				return false;
			}

			String desc = jsonObject.getString("desc");
			desc = desc.replaceAll("</?(html|body|head)>", "").trim();
			
			HashSet<String> descImgsUrl = getDescImgsUrl(desc);
			for (String url : descImgsUrl) {
				ItemImgForXXX img = new ItemImgForXXX();
				img.setOldUrl(url);
				imgs.add(img);
			}
			
			if(CommonUtils.isEmpty(imgs)) {
				SubCarrierTaskForXXX.updateSubTask(StringUtils.EMPTY, StringUtils.EMPTY, SubCarrierTaskForXXXStatus.FAIL, "图片获取异常", subTask);
				return false;
			}
			
			SubCarrierTaskForXXX.updateSubTask(title, picUrl, SubCarrierTaskForXXXStatus.PREPARED, StringUtils.EMPTY, subTask);
			
			Long now = System.currentTimeMillis();
			Long subtaskId = subTask.getId();
			for (ItemImgForXXX i : imgs) {
				i.setSubTaskId(subtaskId);
				i.setCreateTs(now);
				i.setUpdateTs(0L);
			}
			boolean batchInsert = ItemImgForXXX.batchOp(imgs, batchOpType.INSERT);
			if(!batchInsert) {
				SubCarrierTaskForXXX.updateSubTask(StringUtils.EMPTY, StringUtils.EMPTY, SubCarrierTaskForXXXStatus.FAIL, "数据库异常，图片批量插入失败", subTask);
				return false;
			}
			return true;
		}catch(Exception e){
			log.warn(e.getMessage(), e);
			SubCarrierTaskForXXX.updateSubTask(StringUtils.EMPTY, StringUtils.EMPTY, SubCarrierTaskForXXXStatus.FAIL, e.getMessage(), subTask);
			return false;
		}
	}
	
	/**
	 * 商品复制-复制
	 */
	public static Boolean ItemCarry(Long subTaskId) {
		SubCarrierTaskForXXX subTask = SubCarrierTaskForXXX.findById(subTaskId);
		Long numIid = subTask.getNumIid();
		Long userId = subTask.getUserId();
		
		Map<String, String> imgMap = new HashedMap();
		List<ItemImgForXXX> itemImgs = ItemImgForXXX.findBySubTaskId(subTaskId);
		for (ItemImgForXXX img : itemImgs) {
			imgMap.put(img.getOldUrl(), img.getNewUrl());
		}
		
		User user = UserDao.findById(userId);
		if(user == null) {
			SubCarrierTaskForXXX.updateSubTask(StringUtils.EMPTY, StringUtils.EMPTY, SubCarrierTaskForXXXStatus.FAIL, "用户【" + userId + "】不存在", subTask);
			return false;
		}
		// 任务开始
		SubCarrierTaskForXXX.updateSubTask(StringUtils.EMPTY, StringUtils.EMPTY, SubCarrierTaskForXXXStatus.RUNNING, StringUtils.EMPTY, subTask);

		if (user.isTmall()) {
			TMResult tmResult = CarrierTaskAction.doCarryForTmall(numIid, user);
			if (tmResult.isOk()) {
				SubCarrierTaskForXXX.updateSubTask(StringUtils.EMPTY, StringUtils.EMPTY, SubCarrierTaskForXXXStatus.SUCCESS, tmResult.getMsg(), subTask);
				return true;
			} else {
				SubCarrierTaskForXXX.updateSubTask(StringUtils.EMPTY, StringUtils.EMPTY, SubCarrierTaskForXXXStatus.FAIL, tmResult.getMsg(), subTask);
				return false;
			}
		}

		Item itemOrigin = null;
		try {
			itemOrigin = ItemGetAction.getItemExt(user.getSessionKey(), numIid);
		} catch (Exception e) {
			e.printStackTrace();
			SubCarrierTaskForXXX.updateSubTask(StringUtils.EMPTY, StringUtils.EMPTY, SubCarrierTaskForXXXStatus.FAIL, e.toString(), subTask);
			return false;
		}
		if (itemOrigin == null) {
			SubCarrierTaskForXXX.updateSubTask(StringUtils.EMPTY, StringUtils.EMPTY, SubCarrierTaskForXXXStatus.FAIL, "未查到宝贝【" + numIid + "】的相关信息", subTask);
			return false;
		}
		// 执行复制操作
		TMResult<Item> result = ItemCarryApiForXXX.ItemCarrier(user, itemOrigin, imgMap);
		if (!result.isOk) {
			String errMsg = StringUtils.isEmpty(result.msg) ? "接口返回结果为空" : result.msg;
			SubCarrierTaskForXXX.updateSubTask(StringUtils.EMPTY, StringUtils.EMPTY, SubCarrierTaskForXXXStatus.FAIL, errMsg, subTask);
			return false;
		}
		Item itemNow = result.getRes();
		// 添加副图
		List<ItemImg> imgs = itemOrigin.getItemImgs();
		if (!CommonUtils.isEmpty(imgs)) {
			for (ItemImg itemImg : imgs) {
				if (itemImg.getPosition() != null && itemImg.getPosition().longValue() == 1L) {
					continue;
				}
				// 图片替换
				itemImg.setUrl(imgMap.get(itemImg.getUrl()));
				new ItemImgPictureAdd(user, itemNow.getNumIid(), itemImg).call();
			}
		}
		// 添加属性图片
		List<PropImg> propImgs = itemOrigin.getPropImgs();
		if (!CommonUtils.isEmpty(propImgs)) {
			for (PropImg propImg : propImgs) {
				// 图片替换
				propImg.setUrl(imgMap.get(propImg.getUrl()));
				new ItemPropPictureAdd(user, itemNow.getNumIid(), propImg).call();
			}
		}

		log.info("success copy from [numIid: " + numIid + "] -> to [" + user.getUserNick() + "]~~~ [new numIid: " + itemNow.getNumIid() + "]");

		SubCarrierTaskForXXX.updateSubTask(StringUtils.EMPTY, StringUtils.EMPTY, SubCarrierTaskForXXXStatus.SUCCESS, String.valueOf(itemNow.getNumIid()), subTask);
		return true;

	}
	
	public static HashSet<String> getDescImgsUrl(String desc) {
		HashSet<String> itemUrlSet = new HashSet<String>();
		
		if (StringUtils.isEmpty(desc)) {
			return itemUrlSet;
		}

		// 匹配 src="http://"
		Pattern pattern = Pattern.compile("src=('|\")?[\\w\\.\\-/:?!_&%=;,]+('|\")?");
		Matcher matcher = pattern.matcher(desc);
		while (matcher.find()) {
			String link = matcher.group();
			if (StringUtils.isBlank(link) || link.startsWith("=")) {
				continue;
			}

			if (link.startsWith("src=")) {
				link = link.replaceFirst("src=('|\")?", StringUtils.EMPTY);
				if (link.endsWith("\"") || link.endsWith("'")) {
					link = link.substring(0, link.length() - 1);
				}
			}
			
			if(link.indexOf("load.js") >= 0) {
				continue;
			}

			itemUrlSet.add(link);
		}

		// 匹配 background:url("http://")
		pattern = Pattern.compile("(background:(\\s)*url\\(('|\")?){1}[\\w\\.\\-/:?!_&%=;,]+('|\")?\\)");
		matcher = pattern.matcher(desc);
		while (matcher.find()) {
			String link = matcher.group();
			if (StringUtils.isBlank(link)) {
				continue;
			}

			link = link.replaceAll("background:(\\s)*url\\(('|\")?", StringUtils.EMPTY);
			if (link.endsWith("\")") || link.endsWith("')")) {
				link = link.substring(0, link.length() - 1);
			}

			itemUrlSet.add(link);
		}

		// 匹配 background="http://..."
		pattern = Pattern.compile("background=('|\")?[\\w\\.\\-/:?!_&%=;,]+('|\")?");
		matcher = pattern.matcher(desc);
		while (matcher.find()) {
			String link = matcher.group();
			if (StringUtils.isBlank(link)) {
				continue;
			}

			link = link.replaceAll("background=('|\")?", "");
			if (link.endsWith("\"") || link.endsWith("'")) {
				link = link.substring(0, link.length() - 1);
			}

			itemUrlSet.add(link);
		}

		return itemUrlSet;
	}
	
	public static class CarryParam{
		
		private String sid;
		
		private int type;
		
		private String ww;
		
		private List<CarryItem> items;
		
		public CarryParam() {
			super();
		}

		public CarryParam(String sid, int type, String ww, List<CarryItem> items) {
			super();
			this.sid = sid;
			this.type = type;
			this.ww = ww;
			this.items = items;
		}

		public String getSid() {
			return sid;
		}

		public void setSid(String sid) {
			this.sid = sid;
		}

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public String getWw() {
			return ww;
		}

		public void setWw(String ww) {
			this.ww = ww;
		}

		public List<CarryItem> getItems() {
			return items;
		}

		public void setItems(List<CarryItem> items) {
			this.items = items;
		}

	}
	
	public static class CarryItem {
		
		private Long numIid;
		
		private int source;
		
		private int target;
		
		public CarryItem() {
			super();
		}

		public CarryItem(Long numIid, int source, int target) {
			super();
			this.numIid = numIid;
			this.source = source;
			this.target = target;
		}

		public Long getNumIid() {
			return numIid;
		}

		public void setNumIid(Long numIid) {
			this.numIid = numIid;
		}

		public int getSource() {
			return source;
		}

		public void setSource(int source) {
			this.source = source;
		}

		public int getTarget() {
			return target;
		}

		public void setTarget(int target) {
			this.target = target;
		}
		
	}

}
