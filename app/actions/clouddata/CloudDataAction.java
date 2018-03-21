package actions.clouddata;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import models.item.ItemPlay;
import models.user.RecentListItemTwoWeekPv;
import models.user.User;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.cache.Cache;
import result.TMResult;
import transaction.JDBCBuilder.JDBCExecutor;
import bustbapi.ItemApi;
import bustbapi.MBPApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.QueryRow;
import com.taobao.api.domain.Sku;

import controllers.Diag;
import controllers.Diag.EntranceNum;
import dao.item.ItemDao;

public class CloudDataAction {
public static final String TAG = "CloudDataAction";
	
	private static final Logger log = LoggerFactory.getLogger(CloudDataAction.class);
	
	static String QueryForFindByUserIdWithPageOffset = "select userId, numIid, cid, picURL, price, title, type, quantity, salesCount, status, score, deListTime, listTime, sellerCids "
            + " from item%s where userId = ? and listTime > ? and listTime < ? and status = 1";
	public static Long startListTs = 1463055095000L - 30 * DateUtil.DAY_MILLIS;
	public static Long endListTs = Play.mode.isDev() ? 1463055095000L : 1463055095000L - 15 * DateUtil.DAY_MILLIS;
	public static void checkEachUser(User user) {
		SimpleDateFormat sdf = new SimpleDateFormat(
                "yyyyMMdd");
		List<ItemPlay> itemPlays = new JDBCExecutor<List<ItemPlay>>(ItemPlay.dp, 
				ItemDao.genShardQuery(QueryForFindByUserIdWithPageOffset, user.getId()), user.getId(), 
				startListTs, endListTs) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(ItemDao.parseItem(rs));
                }
                return itemList;
            }
        }.call();
        if(CommonUtils.isEmpty(itemPlays)) {
        	return;
        }
        log.info("checkEachUser return itemPlay size is " + itemPlays.size());
        for(ItemPlay itemPlay : itemPlays) {
        	if(RecentListItemTwoWeekPv.findExistId(user.getId(), itemPlay.getNumIid()) > 0) {
        		continue;
        	}
        	Map<String, Integer> map = new HashMap<String, Integer>();
        	Long listTime = itemPlay.getListTime();
        	String startTime = sdf.format(new Date(listTime));
        	String endTime = sdf.format(new Date(listTime + 22 * DateUtil.DAY_MILLIS));
     
        	TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(106975L, "startdate=" + startTime + ",enddate=" + endTime
        			+ ",numIid=" + itemPlay.getNumIid() + ",platform=0" + ",sellerId=" + user.getId(), user.getSessionKey())
            .call();
        	List<QueryRow> rows = res.getRes();
        	if(CommonUtils.isEmpty(rows)) {
        		log.info("yushangfang return size is 0 for user: " + user.getUserNick() + " and numIid = " + itemPlay.getNumIid());
        		continue;
        	}
        	log.info("yushangfang return size is "+rows.size()+" for user: " + user.getUserNick() + " and numIid = " + itemPlay.getNumIid());
        	for(QueryRow uvWinner : rows) {
                List<String> values = uvWinner.getValues();
                if(CommonUtils.isEmpty(values)) {
                    continue;
                }
                Integer pv = map.get(values.get(4));
                if(pv == null || pv < 0) {
                    pv = 0;
                }
                pv += Integer.valueOf(values.get(3));
                map.put(values.get(4), pv);
            }
        	int day1 = map.get(sdf.format(new Date(listTime + 0 * DateUtil.DAY_MILLIS))) == null ? 0 : map.get(sdf.format(new Date(listTime + 0 * DateUtil.DAY_MILLIS)));
        	int day2 = map.get(sdf.format(new Date(listTime + 1 * DateUtil.DAY_MILLIS))) == null ? 0 : map.get(sdf.format(new Date(listTime + 1 * DateUtil.DAY_MILLIS)));
        	int day3 = map.get(sdf.format(new Date(listTime + 2 * DateUtil.DAY_MILLIS))) == null ? 0 : map.get(sdf.format(new Date(listTime + 2 * DateUtil.DAY_MILLIS)));
        	int day4 = map.get(sdf.format(new Date(listTime + 3 * DateUtil.DAY_MILLIS))) == null ? 0 : map.get(sdf.format(new Date(listTime + 3 * DateUtil.DAY_MILLIS)));
        	int day5 = map.get(sdf.format(new Date(listTime + 4 * DateUtil.DAY_MILLIS))) == null ? 0 : map.get(sdf.format(new Date(listTime + 4 * DateUtil.DAY_MILLIS)));
        	int day6 = map.get(sdf.format(new Date(listTime + 5 * DateUtil.DAY_MILLIS))) == null ? 0 : map.get(sdf.format(new Date(listTime + 5 * DateUtil.DAY_MILLIS)));
        	int day7 = map.get(sdf.format(new Date(listTime + 6 * DateUtil.DAY_MILLIS))) == null ? 0 : map.get(sdf.format(new Date(listTime + 6 * DateUtil.DAY_MILLIS)));
        	int day8 = map.get(sdf.format(new Date(listTime + 7 * DateUtil.DAY_MILLIS))) == null ? 0 : map.get(sdf.format(new Date(listTime + 7 * DateUtil.DAY_MILLIS)));
        	int day9 = map.get(sdf.format(new Date(listTime + 8 * DateUtil.DAY_MILLIS))) == null ? 0 : map.get(sdf.format(new Date(listTime + 8 * DateUtil.DAY_MILLIS)));
        	int day10 = map.get(sdf.format(new Date(listTime + 9 * DateUtil.DAY_MILLIS))) == null ? 0 : map.get(sdf.format(new Date(listTime + 9 * DateUtil.DAY_MILLIS)));
        	int day11 = map.get(sdf.format(new Date(listTime + 10 * DateUtil.DAY_MILLIS))) == null ? 0 : map.get(sdf.format(new Date(listTime + 10 * DateUtil.DAY_MILLIS)));
        	int day12 = map.get(sdf.format(new Date(listTime + 11 * DateUtil.DAY_MILLIS))) == null ? 0 : map.get(sdf.format(new Date(listTime + 11 * DateUtil.DAY_MILLIS)));
        	int day13 = map.get(sdf.format(new Date(listTime + 12 * DateUtil.DAY_MILLIS))) == null ? 0 : map.get(sdf.format(new Date(listTime + 12 * DateUtil.DAY_MILLIS)));
        	int day14 = map.get(sdf.format(new Date(listTime + 13 * DateUtil.DAY_MILLIS))) == null ? 0 : map.get(sdf.format(new Date(listTime + 13 * DateUtil.DAY_MILLIS)));
        	int day15 = map.get(sdf.format(new Date(listTime + 14 * DateUtil.DAY_MILLIS))) == null ? 0 : map.get(sdf.format(new Date(listTime + 14 * DateUtil.DAY_MILLIS)));
        	int day16 = map.get(sdf.format(new Date(listTime + 15 * DateUtil.DAY_MILLIS))) == null ? 0 : map.get(sdf.format(new Date(listTime + 15 * DateUtil.DAY_MILLIS)));
        	int day17 = map.get(sdf.format(new Date(listTime + 16 * DateUtil.DAY_MILLIS))) == null ? 0 : map.get(sdf.format(new Date(listTime + 16 * DateUtil.DAY_MILLIS)));
        	int day18 = map.get(sdf.format(new Date(listTime + 17 * DateUtil.DAY_MILLIS))) == null ? 0 : map.get(sdf.format(new Date(listTime + 17 * DateUtil.DAY_MILLIS)));
        	int day19 = map.get(sdf.format(new Date(listTime + 18 * DateUtil.DAY_MILLIS))) == null ? 0 : map.get(sdf.format(new Date(listTime + 18 * DateUtil.DAY_MILLIS)));
        	int day20 = map.get(sdf.format(new Date(listTime + 19 * DateUtil.DAY_MILLIS))) == null ? 0 : map.get(sdf.format(new Date(listTime + 19 * DateUtil.DAY_MILLIS)));
        	int day21 = map.get(sdf.format(new Date(listTime + 20 * DateUtil.DAY_MILLIS))) == null ? 0 : map.get(sdf.format(new Date(listTime + 20 * DateUtil.DAY_MILLIS)));
        	
        	new RecentListItemTwoWeekPv(user.getId(), itemPlay.getNumIid(),
        			day1, day2, day3, day4, day5, day6, day7, 
        			day8, day9, day10, day11, day12, day13, day14,
        			day15, day16, day17, day18, day19, day20, day21).jdbcSave();
        	/*if(map.size() < 10) {
        		continue;
        	}
        	
        	List<Map.Entry<String, Integer>> infoIds =
        		    new ArrayList<Map.Entry<String, Integer>>(map.entrySet());
        	Collections.sort(infoIds, new Comparator<Map.Entry<String, Integer>>() {   
        	    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {      
        	        //return (o2.getValue() - o1.getValue()); 
        	        return (o1.getKey()).toString().compareTo(o2.getKey());
        	    }
        	}); 
        	int count = 0;
        	for (int i = 0; i < infoIds.size(); i++) {
        	    if(i < infoIds.size() - 1) {
        	    	if(infoIds.get(i + 1).getValue() - infoIds.get(i).getValue() >= 0) {
        	    		count++;
        	    	}
        	    }
        	}
        	if(count == infoIds.size() -1) {
        		return itemPlay.getNumIid();
        	}*/
        }
        return;
	}
	
	/*
	 * 根据用户，宝贝ID 获取该宝贝 对应时间段内的  加购物车信息
	 * startdate: 20160118
	 * endDate: 20160118
	 */
	public static TMResult<HashMap<Long, Integer>> getItemCartNumMap(User user, Long numIid, String startdate, String endDate) {
		HashMap<Long, Integer> numIidCartNumMap = new HashMap<Long, Integer>();
		if(user == null || numIid == null || numIid <= 0L || StringUtils.isEmpty(startdate) || StringUtils.isEmpty(endDate)) {
			return new TMResult(false, "请传入用户信息,宝贝信息,查询时间段", numIidCartNumMap);
		}

    	HashMap<Long, Integer> skuIdCatNumMap = CloudDataAction.getSkuIdCatNumMap(user, startdate, endDate);
    	if(CommonUtils.isEmpty(skuIdCatNumMap)) {
    		return new TMResult(false, "该卖家时间段内无加购物车数据", numIidCartNumMap);
    	}
    	numIidCartNumMap = CloudDataAction.getNumIidCartNumMap(user, numIid, skuIdCatNumMap);
		return new TMResult(true, "", numIidCartNumMap);
	}
	
	public static String ShopCartBuyerPre = "ShopCartBuyerPre_";
	public static Integer getShopCartBuyers(User user, String startdate) {
		Integer shopCartBuyers = (Integer) Cache.get(ShopCartBuyerPre + user.getId() + startdate);
		if(shopCartBuyers != null) {
			return shopCartBuyers;
		}
		TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(105114L, "thedate="+startdate+",sellerId=" + user.getId(),
				user.getSessionKey()).call();
		if(!res.isOk()) {
			return 0;
		}
		
		List<QueryRow> rows = res.getRes();
		
		if(CommonUtils.isEmpty(rows)) {
//			Cache.set(ShopCartBuyerPre + user.getId() + startdate, 0);
			return 0;
		}
		List<String> values = rows.get(0).getValues();
		if(CommonUtils.isEmpty(values)) {
			Cache.set(ShopCartBuyerPre + user.getId() + startdate, 0);
			return 0;
		}
		try {
			shopCartBuyers = Integer.valueOf(values.get(0));
		} catch (Exception e) {
			Cache.set(ShopCartBuyerPre + user.getId() + startdate, 0);
			return 0;
			// TODO: handle exception
		}
		Cache.set(ShopCartBuyerPre + user.getId() + startdate, shopCartBuyers);
		return shopCartBuyers;
	}
	
	public static String ItemCartBuyerPre = "ItemCartBuyerPre_";
	public static Integer getItemCartBuyers(User user, String startdate, Long numIid) {
		Integer itemCartBuyers = (Integer) Cache.get(ItemCartBuyerPre + user.getId() + startdate + numIid);
		if(itemCartBuyers != null) {
			return itemCartBuyers;
		}
		
		// 最近一天（昨日）的数据，只有当前时间大于8点时，才放入缓存
		Boolean setToCache = true;
		String dataString = Diag.sdf.format(new Date(DateUtil.formYestadyMillis()));
		if(dataString.equalsIgnoreCase(startdate)) {
			Long nowTime = System.currentTimeMillis();
			Long limitTime = DateUtil.formCurrDate() + 8 * DateUtil.ONE_HOUR;
			
			if(nowTime < limitTime) {
				setToCache = false;
			}
		}
		
		TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(105115L, "thedate="+startdate+",sellerId=" + user.getId() + ",numIid=" + numIid,
				user.getSessionKey()).call();
		if(!res.isOk()) {
			return -1;
		}
		List<QueryRow> rows = res.getRes();
		if(CommonUtils.isEmpty(rows)) {
			return -1;
		}
		List<String> values = rows.get(0).getValues();
		if(CommonUtils.isEmpty(values)) {
			return 0;
		}
		try {
			itemCartBuyers = Integer.valueOf(values.get(0));
		} catch (Exception e) {
			return 0;
		}
		if(setToCache) {
			Cache.set(ItemCartBuyerPre + user.getId() + startdate + numIid, itemCartBuyers);
		}
		return itemCartBuyers;
	}
	
	public static String skuIdCartNumMapPre = "skuIdCartNumMapPre_";
	public static HashMap<Long, Integer> getSkuIdCatNumMap(User user, String startdate, String endDate) {
		HashMap<Long, Integer> skuIdCatNumMap = new HashMap<Long, Integer>();
		skuIdCatNumMap = (HashMap<Long, Integer>) Cache.get(skuIdCartNumMapPre + user.getId() + startdate + "_" + endDate);
		if(skuIdCatNumMap != null) {
			return skuIdCatNumMap;
		}
		TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(105001L, "startdate="+startdate+",enddate="+endDate+",sellerId=" + user.getId(),
				user.getSessionKey()).call();
		skuIdCatNumMap = new HashMap<Long, Integer>();
		if(!res.isOk()) {
			return skuIdCatNumMap;
		}
		List<QueryRow> rows = res.getRes();
		if(CommonUtils.isEmpty(rows)) {
			return skuIdCatNumMap;
		}
    	for(QueryRow row : rows) {
    		if(row == null) {
    			continue;
    		}
    		List<String> value = row.getValues();
    		try {
    			Long skuId = Long.valueOf(value.get(4));
    			Integer addCartUserNum = Integer.valueOf(value.get(5));
    			if(skuIdCatNumMap.get(skuId) == null) {
    				skuIdCatNumMap.put(skuId, addCartUserNum);
    			} else {
    				int oldAddCartUserNum = skuIdCatNumMap.get(skuId);
    				skuIdCatNumMap.put(skuId, oldAddCartUserNum + addCartUserNum);
    			}
			} catch (Exception e) {
				// TODO: handle exception
			}
    		
    	}
    	Cache.set(skuIdCartNumMapPre + user.getId() + startdate + "_" + endDate, skuIdCatNumMap, "24h");
    	return skuIdCatNumMap;
	}
	
	public static HashMap<Long, Integer> getNumIidCartNumMap(User user, Long numIid, HashMap<Long, Integer> skuIdCatNumMap) {
		HashMap<Long, Integer> numIidCartNumMap = new HashMap<Long, Integer>();
		if(numIid == null || numIid <= 0L) {
			return numIidCartNumMap;
		}
		Item item = new ItemApi.ItemGet(user, numIid, true).call();
		List<Sku> skus = item == null ? null : item.getSkus();
		if(CommonUtils.isEmpty(skus) == false) {
			int total = 0;
			for(Sku sku : skus) {
				Integer skuCartNum = skuIdCatNumMap.get(sku.getSkuId());
				if(skuCartNum != null && skuCartNum > 0) {
					total += skuCartNum;
				}
			}
			numIidCartNumMap.put(numIid, total);
		}
		return numIidCartNumMap;
	}
	
	public static int getItemCollectNumMap(User user, Long numIid, String startdate, String endDate) {
		HashMap<Long, Integer> numIidCollectNumMap = new HashMap<Long, Integer>();
		if(user == null || numIid == null || numIid <= 0L || StringUtils.isEmpty(startdate) || StringUtils.isEmpty(endDate)) {
			return 0;
		}
		numIidCollectNumMap = getItemCollectNumMap(user, startdate, endDate);
		if(CommonUtils.isEmpty(numIidCollectNumMap)){
		    return -1;
		}
		Set<Long> keySet = numIidCollectNumMap.keySet();
		if(!keySet.contains(numIid)){
		    return 0;
		}
		return numIidCollectNumMap.get(numIid);
	}
	
	public static String ItemCollectNumMapPre = "ItemCollectNumMapPre_";
	public static HashMap<Long, Integer> getItemCollectNumMap(User user, String startdate, String endDate) {
		HashMap<Long, Integer> itemCollectNumMap = new HashMap<Long, Integer>();
		itemCollectNumMap = (HashMap<Long, Integer>) Cache.get(ItemCollectNumMapPre + user.getId() + startdate + "_" + endDate);
		if(itemCollectNumMap != null) {
			return itemCollectNumMap;
		}
		TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(105005L, "startdate="+startdate+",enddate="+endDate+",sellerId=" + user.getId(),
				user.getSessionKey()).call();
		itemCollectNumMap = new HashMap<Long, Integer>();
		if(!res.isOk()) {
			return itemCollectNumMap;
		}
		List<QueryRow> rows = res.getRes();
		if(CommonUtils.isEmpty(rows)) {
			return itemCollectNumMap;
		}
    	for(QueryRow row : rows) {
    		if(row == null) {
    			continue;
    		}
    		List<String> value = row.getValues();
    		try {
    			Long skuId = Long.valueOf(value.get(3));
    			Integer addCartUserNum = Integer.valueOf(value.get(4));
    			if(itemCollectNumMap.get(skuId) == null) {
    				itemCollectNumMap.put(skuId, addCartUserNum);
    			} else {
    				int oldAddCartUserNum = itemCollectNumMap.get(skuId);
    				itemCollectNumMap.put(skuId, oldAddCartUserNum + addCartUserNum);
    			}
			} catch (Exception e) {
				// TODO: handle exception
			}
    		
    	}
    	Cache.set(ItemCollectNumMapPre + user.getId() + startdate + "_" + endDate, itemCollectNumMap, "24h");
    	return itemCollectNumMap;
    }

    public static boolean getViewTrade(Long endTime, Long numIid, String sessionKey, Long userId, Map<String, AreaViews> map, Long startTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        // 会返回dt
        TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(106838L, "startdate="
                + sdf.format(startTime) + ",sellerId=" + userId + ",enddate="
                + sdf.format(endTime) + ",platform=0" + ",numIid=" + numIid
                + ",@replace_null=NULL", sessionKey).call();
        List<QueryRow> viewTrade = res.getRes();
        if (!CommonUtils.isEmpty(viewTrade)) {
            for (QueryRow row : viewTrade) {
                List<String> value = row.getValues();
                AreaViews tmp = new AreaViews();
                tmp.addProp(value.get(2).equals("NULL") ? AreaViews.DEFAULT_VALUE : value.get(2),
                        value.get(3).equals("NULL") ? AreaViews.DEFAULT_VALUE : value.get(3),
                        value.get(4).equals("NULL") ? AreaViews.DEFAULT_VALUE : value.get(4),
                        value.get(5).equals("NULL") ? AreaViews.DEFAULT_VALUE : value.get(5),
                        value.get(6).equals("NULL") ? AreaViews.DEFAULT_VALUE : value.get(6),
                        value.get(7).equals("NULL") ? AreaViews.DEFAULT_VALUE : value.get(7));
                String dt = value.get(8);
                if (map.get(dt) == null) {
                    map.put(dt, tmp);
                    continue;
                }
                AreaViews old = (AreaViews) map.get(dt);
                old.addProp(tmp);
                map.put(dt, old);
            }
            return true;
        }
        return false;
    }

    public static void getViewTradeDay(Long endTime, Long numIid, String sessionKey, Long userId, Map<String, AreaViews> map, int interval) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        // 计算每天的数量
        for (int i = 1; i <= interval; i++) {
            // 将时间转成yyyyMMdd格式的字符串
            long tempTime = endTime - (interval - i) * DateUtil.DAY_MILLIS;
            String dataTime = sdf.format(new Date(tempTime));
            AreaViews areaViews = map.get(dataTime);
            TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(3256L, "startdate="+ dataTime + ",sellerId=" + userId + ",enddate="
                    + dataTime + ",platform=0" + ",numIid=" + numIid + ",@replace_null=NULL", sessionKey).call();
            List<QueryRow> viewTrade = res.getRes();
            if(CommonUtils.isEmpty(viewTrade)){
               continue; 
            }
            for (QueryRow row : viewTrade) {
                List<String> value = row.getValues();
                if(CommonUtils.isEmpty(value)){
                    continue;
                }
                AreaViews tmp = new AreaViews();
                tmp.addProp(value.get(2).equals("NULL") ? AreaViews.DEFAULT_VALUE : value.get(2),
                        value.get(3).equals("NULL") ? AreaViews.DEFAULT_VALUE : value.get(3),
                        value.get(4).equals("NULL") ? AreaViews.DEFAULT_VALUE : value.get(4),
                        value.get(5).equals("NULL") ? AreaViews.DEFAULT_VALUE : value.get(5),
                        value.get(6).equals("NULL") ? AreaViews.DEFAULT_VALUE : value.get(6),
                        value.get(7).equals("NULL") ? AreaViews.DEFAULT_VALUE : value.get(7));
                areaViews.addProp(tmp);
            }
        }
    }
    
    public static void setSearchUV(Long endTime, Long numIid, String sessionKey, Long userId, Map<String, AreaViews> map, Long startTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        // 设置宝贝的pc类目UV
        TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(3256L, "startdate=" + sdf.format(new Date(startTime)) + ",sellerId=" + userId +
                        ",enddate=" + sdf.format(new Date(endTime)) + ",platform=1" + ",numIid=" + numIid + ",@replace_null=NULL", sessionKey).call();
        List<QueryRow> viewTradePCs = res.getRes();
        if(!CommonUtils.isEmpty(viewTradePCs)){
            for (QueryRow viewTradePC : viewTradePCs) {
                List<String> values = viewTradePC.getValues();
                if(CommonUtils.isEmpty(values)){
                    continue;
                }
                AreaViews areaViews = map.get(values.get(8));
                if(areaViews == null){
                    continue;
                }
                int pcPv = values.get(2).equals("NULL") ? 0 : Integer.parseInt(values.get(2));
                int pcUv = values.get(3).equals("NULL") ? 0 : Integer.parseInt(values.get(3));
                areaViews.addPcUv(pcUv + pcPv);
            }
        }
    }
    
    public static void setPCUV(Long endTime, Long numIid, String sessionKey, Long userId, Map<String, AreaViews> map, Long startTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        // 设置宝贝的pc类目UV
        TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(112788L, "startdate=" + sdf.format(new Date(startTime)) + ",sellerId=" + userId +
                        ",enddate=" + sdf.format(new Date(endTime)) + ",platform=1" + ",numIid=" + numIid + ",@replace_null=NULL", sessionKey).call();
        List<QueryRow> rows = res.getRes();
        if(!CommonUtils.isEmpty(rows)){
            for (QueryRow row : rows) {
                List<String> values = row.getValues();
                if(CommonUtils.isEmpty(values)){
                    continue;
                }
                AreaViews areaViews = map.get(values.get(0));
                if(areaViews == null){
                    continue;
                }
                int pcUv = values.get(5).equals("NULL") ? 0 : Integer.parseInt(values.get(5));
                areaViews.addPcUv(pcUv);
            }
        }
    }

    public static void setSearchUVDay(Long endTime, Long numIid, String sessionKey, Long userId, Map<String, AreaViews> map, int interval) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        // 计算每天的数量
        for (int i = 1; i <= interval; i++) {
            // 将时间转成yyyyMMdd格式的字符串
            long tempTime = endTime - (interval - i) * DateUtil.DAY_MILLIS;
            String dataTime = sdf.format(new Date(tempTime));
            AreaViews areaViews = map.get(dataTime);
            TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(3256L, "startdate=" + dataTime + ",sellerId=" + userId +
                    ",enddate=" + dataTime + ",platform=1" + ",numIid=" + numIid + ",@replace_null=NULL", sessionKey).call();
            List<QueryRow> viewTradePCs = res.getRes();
            if(CommonUtils.isEmpty(viewTradePCs)){
                continue;
            }
            for (QueryRow queryRow : viewTradePCs) {
                List<String> values = queryRow.getValues();
                if(CommonUtils.isEmpty(values)){
                    continue;
                }
                int pcPv = values.get(2).equals("NULL") ? 0 : Integer.parseInt(values.get(2));
                int pcUv = values.get(3).equals("NULL") ? 0 : Integer.parseInt(values.get(3));
                areaViews.addPcUv(pcUv + pcPv);
            }
        }
    }
    
    public static void setSearchUVValue(String startTimeStr, String endTimeStr, Long userId, String sessionKey, Map<String, AreaViews> map) {
        // PC端的
        setSearchUVPC(startTimeStr, endTimeStr, userId, sessionKey, map);
        // 无线端端的
        setSearchUVWireless(startTimeStr, endTimeStr, userId, sessionKey, map);
    }

    private static void setSearchUVPC(String startTimeStr, String endTimeStr, Long userId, String sessionKey, Map<String, AreaViews> map){
        TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(3274L,
                "startdate=" + startTimeStr + ",sellerId=" + userId + ",enddate=" + endTimeStr + ",@replace_null=NULL", sessionKey).call();
        List<QueryRow> searchUvWinnnersPC = res.getRes();
        if(CommonUtils.isEmpty(searchUvWinnnersPC)) {
            return;
        }
        addSearchUV(map, searchUvWinnnersPC);
    }

    private static void setSearchUVWireless(String startTimeStr, String endTimeStr, Long userId, String sessionKey, Map<String, AreaViews> map){
        TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(105047L,
                "startdate=" + startTimeStr + ",sellerId=" + userId + ",enddate=" + endTimeStr + ",@replace_null=NULL", sessionKey).call();
        List<QueryRow> searchUvWinnnersWireless = res.getRes();
        if(CommonUtils.isEmpty(searchUvWinnnersWireless)){
            return;
        }
        addSearchUV(map, searchUvWinnnersWireless);
    }
    
    private static void addSearchUV(Map<String, AreaViews> map, List<QueryRow> searchUvWinnnersPC) {
        for(QueryRow uvWinner : searchUvWinnnersPC) {
            List<String> values = uvWinner.getValues();
            if(CommonUtils.isEmpty(values)) {
                continue;
            }
            AreaViews noSearch = map.get(values.get(0));
            if(noSearch == null) {
                continue;
            }
            noSearch.addSearchUv(values.get(1));
        }
    }
    
    public static Map<String, AreaViews> setMapDate(int interval, Long endTime){
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Map<String, AreaViews> map = new TreeMap<String, AreaViews>();
        for (int i = 1; i <= interval; i++) {
            String dateStr = format.format(new Date(endTime - (interval - i) * DateUtil.DAY_MILLIS));
            map.put(dateStr, new AreaViews(dateStr));
        }
        return map;
    }
    
    public static Map<String, EntranceNum> setEntranceMap(int interval, Long endTime){
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Map<String, EntranceNum> map = new TreeMap<String, EntranceNum>();
        for (int i = 1; i <= interval; i++) {
            String dateStr = format.format(new Date(endTime - (interval - i) * DateUtil.DAY_MILLIS));
            map.put(dateStr, new EntranceNum());
        }
        return map;
    }
    
    public static Map<String, CPCUvPv> setCPCMap(int interval, Long endTime){
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Map<String, CPCUvPv> map = new TreeMap<String, CPCUvPv>();
        for (int i = 1; i <= interval; i++) {
            String dateStr = format.format(new Date(endTime - (interval - i) * DateUtil.DAY_MILLIS));
            map.put(dateStr, new CPCUvPv(dateStr));
        }
        return map;
    }
    
    public static String getImpression(String startTimeStr, Long userId, Long numIid, String sessionKey){
        TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(106778L,
                "startdate=" + startTimeStr +",sellerId=" + userId + ",enddate=" + startTimeStr + ",numIid=" + numIid, sessionKey).call();
        List<QueryRow> impression = res.getRes();
        if(CommonUtils.isEmpty(impression)) {
            return "-1";
        }
        List<String> values = impression.get(0).getValues();
        if(CommonUtils.isEmpty(values)){
            return "0";
        }
        return values.get(0);
    }
    
	public static String getBounceRate(User user, Long numIid, String dataTime) {
		String bounceRate = StringUtils.EMPTY;
		
		TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(112359L,
				"sellerId=" + user.getId() +",actionId=" + numIid + ",startDate=" + dataTime + ",endDate=" + dataTime, user.getSessionKey()).call();
		List<QueryRow> traffic = res.getRes();
		
		if(CommonUtils.isEmpty(traffic)) {
			return bounceRate;
		}
		List<String> values = traffic.get(0).getValues();
		if(CommonUtils.isEmpty(values)){
			return bounceRate;
		}
		return values.get(6);
	}
	
}
