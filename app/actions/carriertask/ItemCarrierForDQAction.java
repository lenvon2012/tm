package actions.carriertask;

import java.util.ArrayList;
import java.util.List;

import models.carrierTask.CarrierTaskForDQ;
import models.carrierTask.CarrierTaskForDQ.CarrierTaskForDQStatus;
import models.carrierTask.CarrierTaskForDQ.CarrierTaskForDQType;
import models.carrierTask.ItemCarryCustom;
import models.user.User;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import weibo4j.org.json.JSONArray;
import weibo4j.org.json.JSONException;
import weibo4j.org.json.JSONObject;
import bustbapi.UserAPIs;
import bustbapi.UserAPIs.UserGetApi;

import com.alibaba.fastjson.JSON;

public class ItemCarrierForDQAction {
	
	private static final Logger log = LoggerFactory.getLogger(ItemCarrierForDQAction.class);
	
	public static itemCarryResult doItemCarry(String sid, Long numIid, int type, Long cid) {
		User user = getUserForDQ(sid);
		if(user == null || !user.isVaild) {
			return new itemCarryResult(sid, numIid, false, "无效的用户秘钥");
		}
		
		CarrierTaskForDQ task = new CarrierTaskForDQ(numIid, CarrierTaskForDQStatus.WAITING, user.getId(), type, cid);
		boolean success = task.jdbcSave();
		if(!success) {
			return new itemCarryResult(sid, numIid, false, "数据库异常，任务创建失败！");
		}
		
		return new itemCarryResult(sid, numIid, true, String.valueOf(task.getId()));
	}
	
	public static User getUserForDQ(String sid) {
		if(StringUtils.isEmpty(sid)) {
			return null;
		}
		sid = sid.trim();
		
		String key = "DQ_User_Valid_" + sid;
		User user = (User) Cache.get(key);
		if(user != null) {
			return user;
		}
		
		UserGetApi api = new UserAPIs.UserGetApi(sid, null);
		com.taobao.api.domain.User tbUser = api.call();
		if (tbUser == null) {
			user = new User(StringUtils.EMPTY, 0L, sid, false);
		} else {
			user = new User(tbUser.getNick(), tbUser.getUserId(), sid, true);
		}
		Cache.set(key, user, "3mn");
		return user;
	}
	
	public static List<CarryParam> parseCarrierJson(String paramJson) {
		List<CarryParam> list = new ArrayList<CarryParam>();
		
		if(StringUtils.isEmpty(paramJson)) {
			return list;
		}
		
		try {
			JSONArray jsonArray = new JSONArray(paramJson);
			for (int i = 0, l = jsonArray.length(); i < l; i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				
				String sid = jsonObject.getString("sid");
				Long numIid = jsonObject.getLong("numIid");
				int type = jsonObject.getInt("type");
				if(type != CarrierTaskForDQType.TB && type != CarrierTaskForDQType.ALIBABA) {
					continue;
				}
				Long cid = jsonObject.getLong("cid");
				
				if(StringUtils.isEmpty(sid) || numIid <= 0 || type <= 0) {
					continue;
				}
				
				CarryParam carrierParam = new CarryParam(sid, numIid, type, cid);
				list.add(carrierParam);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return ListUtils.EMPTY_LIST;
		}
		
		return list;
	}

	public static ItemCarryCustom parseItemCarryCustom(String paramJson) {
		try {
			String sid = JSON.parseObject(paramJson).getString("sid");
			User user = getUserForDQ(sid);
			if (user == null || user.getId() == 0L) {
				return null;
			}
			ItemCarryCustom itemCarryCustom = JSON.parseObject(paramJson, ItemCarryCustom.class);
			itemCarryCustom.setUserId(user.getId());
			
			return itemCarryCustom;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static List<RecommendItemCat> parseItemCatJson(String json) {
		List<RecommendItemCat> list = new ArrayList<RecommendItemCat>();
		
		if(StringUtils.isEmpty(json)) {
			return list;
		}
		
		try {
			JSONObject jsonObject = new JSONObject(json);
			boolean success = jsonObject.getBoolean("success");
			if(!success) {
				return list;
			}
			String msg = jsonObject.getString("msg");
			if(StringUtils.isEmpty(msg)) {
				return list;
			}
			
			JSONArray jsonArray = new JSONArray(msg);
			
			for (int i = 0, l = jsonArray.length(); i < l; i++) {
				JSONObject object = jsonArray.getJSONObject(i);
				
				Long cid = object.getLong("cid");
				String name = object.getString("name");
				Long parentCid = object.getLong("parentCid");
				String parentName = object.getString("parentName");
				String score = object.getString("score");
				
				RecommendItemCat itemCat = new RecommendItemCat(cid, name, parentCid, parentName, score);
				list.add(itemCat);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return ListUtils.EMPTY_LIST;
		}
		
		return list;
	}

	public static class CarryParam{
		
		private String sid;
		
		private Long numIid;
		
		private int type;
		
		private Long cid;
		
		public CarryParam() {
			super();
		}

		public CarryParam(String sid, Long numIid, int type, Long cid) {
			super();
			this.sid = sid;
			this.numIid = numIid;
			this.type = type;
			this.cid = cid;
		}

		public String getSid() {
			return sid;
		}

		public void setSid(String sid) {
			this.sid = sid;
		}

		public Long getNumIid() {
			return numIid;
		}

		public void setNumIid(Long numIid) {
			this.numIid = numIid;
		}

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public Long getCid() {
			return cid;
		}

		public void setCid(Long cid) {
			this.cid = cid;
		}
		
	}
	
	public static class itemCarryResult{
		
		private String sid;
		
		private Long numIid;
		
		private Boolean success;
		
		private String message;
		
		public itemCarryResult() {
			super();
		}

		public itemCarryResult(String sid, Long numIid, Boolean success,
				String message) {
			super();
			this.sid = sid;
			this.numIid = numIid;
			this.success = success;
			this.message = message;
		}

		public String getSid() {
			return sid;
		}

		public void setSid(String sid) {
			this.sid = sid;
		}

		public Long getNumIid() {
			return numIid;
		}

		public void setNumIid(Long numIid) {
			this.numIid = numIid;
		}

		public Boolean getSuccess() {
			return success;
		}

		public void setSuccess(Boolean success) {
			this.success = success;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
		
	}
	
	public static class RecommendItemCat{
		
		private Long cid;
		
		private String name;
		
		private Long parentCid;
		
		private String parentName;
		
		private String score;
		
		public RecommendItemCat() {
			super();
		}

		public RecommendItemCat(Long cid, String name, Long parentCid,
				String parentName, String score) {
			super();
			this.cid = cid;
			this.name = name;
			this.parentCid = parentCid;
			this.parentName = parentName;
			this.score = score;
		}

		public Long getCid() {
			return cid;
		}

		public void setCid(Long cid) {
			this.cid = cid;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Long getParentCid() {
			return parentCid;
		}

		public void setParentCid(Long parentCid) {
			this.parentCid = parentCid;
		}

		public String getParentName() {
			return parentName;
		}

		public void setParentName(String parentName) {
			this.parentName = parentName;
		}

		public String getScore() {
			return score;
		}

		public void setScore(String score) {
			this.score = score;
		}

	}
	
}
