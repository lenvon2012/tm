package actions;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import play.cache.Cache;
import result.TMResult;
import models.CPEctocyst.SellerToStaff;
import models.item.ItemPlay;
import models.user.User;
import uvpvdiag.NewUvPvDiagResult;
import uvpvdiag.UvPvDiagResult;
import actions.clouddata.AreaViews;
import actions.clouddata.CloudDataAction;
import bustbapi.MBPApi;
import bustbapi.SubUserApi.SellercenterSubusersGet;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.commons.ClientException;
import com.taobao.api.domain.QueryRow;
import com.taobao.api.domain.SubUserInfo;

import configs.TMConfigs.App;
import controllers.Items;

public class NewUvPvDiagAction {
	
	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	
	public static Map<Integer, String> srcIdNameMap = new HashMap<Integer, String>();
	
	public static Map<String, String> wirelessSrcIdNameMap = new HashMap<String, String>();
	
	// 测试用户session是否有效
	public static boolean isNeedRedirect(User user) {
		SellercenterSubusersGet subUserApi = new SellercenterSubusersGet(user);
		TMResult<List<SubUserInfo>> res = subUserApi.call();
		if(!res.isOk() && "Invalid session".equalsIgnoreCase(res.getMsg())) {
			return true;
		}
		return false;
	}
	
	// 判断是否要走新的同步接口
	public static boolean isNew(User user) {
		if(Items.VIP_USER_NICK.contains(user.getUserNick())) {
			return true;
		}
		
		// 判断是否是帮帮淘网络
		String chiefName = "帮帮淘网络";
		SellerToStaff res = SellerToStaff.findByChiefNameAndUserNick(chiefName, user.getUserNick());
		if(res != null) {
			return true;
		}
		
		return false;
	}
	
	public static NewUvPvDiagResult doDiag(User user, ItemPlay itemPlay, int interval, Long endTime) throws ClientException {
		if(itemPlay == null) {
			return new NewUvPvDiagResult();
		}
		NewUvPvDiagResult result = new NewUvPvDiagResult(itemPlay);
		if(user == null) {
			return result;
		}
		Long startTime = endTime - (interval - 1) * DateUtil.DAY_MILLIS;
		String startDate = sdf.format(new Date(startTime));
		String endDate = sdf.format(new Date(endTime));
		
		// 从缓存中读取数据
		String key = user.getId() + "_" +  itemPlay.getNumIid() + "_" + startDate + "_" + endDate + "_NewUvPvDiagResult";
		NewUvPvDiagResult cache = Cache.get(key, NewUvPvDiagResult.class);
		if(cache != null) {
			return cache;
		}
		
		boolean collectFlag = false;
		boolean cartFlag = false;
		
		TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(2948L, 
				"startdate=" + startDate + ",numIid=" + itemPlay.getNumIid() +
				",sellerId=" + user.getId() + 
				",enddate=" + endDate + ",platform=0", user.getSessionKey()).call();
		if(res.isOk()) {
			List<QueryRow> rows = res.getRes();
			if(!CommonUtils.isEmpty(rows)) {
				for(QueryRow row : rows) {
					result.addProps(row);
				}
			}
		}
		
		for (int i = 1; i <= interval; i++) {
			// 将时间转成yyyyMMdd格式的字符串
			long tempTime = endTime - (interval - i) * DateUtil.DAY_MILLIS;
			String dataTime = sdf.format(new Date(tempTime));
			
			// 收藏
			String collectKey = user.getId() + "_" +  itemPlay.getNumIid() + "_" + dataTime + "_NewUvPvDiagResult_collect";
			Object collectCache = Cache.get(collectKey);
			int collectCount = 0;
			if(collectCache == null) {
				TMResult<List<QueryRow>> collectRes = new MBPApi.MBPDataGet(108451L, "startdate=" + 
						dataTime + ",enddate=" + dataTime + ",sellerId=" + user.getId() + ",numIid=" + itemPlay.getNumIid(),
						user.getSessionKey()).call();
				if(collectRes.isOk()) {
					List<QueryRow> collectRows = collectRes.getRes();
					if(!CommonUtils.isEmpty(collectRows)) {
						for(QueryRow row : collectRows) {
							if(row == null) {
								continue;
							}
							List<String> value = row.getValues();
							collectCount += Integer.valueOf(value.get(4));
						}
					}
					Cache.set(collectKey, collectCount, "20h");
					collectFlag = true;
				}
			} else {
				collectCount = (Integer) collectCache;
			}
			result.addCollect(collectCount);
			
			// 加购
			String cartKey = user.getId() + "_" +  itemPlay.getNumIid() + "_" + dataTime + "_NewUvPvDiagResult_cart";
			Object cartCache = Cache.get(cartKey);
			int cartCount = 0;
			if(cartCache == null) {
				TMResult<List<QueryRow>> cartRes = new MBPApi.MBPDataGet(108452L, "startdate=" + 
						dataTime + ",enddate=" + dataTime + ",sellerId=" + user.getId() + ",numIid=" + itemPlay.getNumIid(),
						user.getSessionKey()).call();
				if(cartRes.isOk()) {
					List<QueryRow> cartRows = cartRes.getRes();
					if(!CommonUtils.isEmpty(cartRows)) {
						for(QueryRow row : cartRows) {
							if(row == null) {
								continue;
							}
							List<String> value = row.getValues();
							cartCount += Integer.valueOf(value.get(4));
						}
					}
					Cache.set(cartKey, cartCount, "20h");
					cartFlag = true;
				}
			} else {
				cartCount = (Integer) cartCache;
			}
			result.addCart(cartCount);
		}
		
		if(res.isOk() && collectFlag && cartFlag) {
			Cache.set(key, result, "20h");
		}
		
		return result;
	}
	
}
