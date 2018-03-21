package utils;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import result.TMResult;
import autotitle.AutoSplit;
import bustbapi.MBPApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.commons.ClientException;
import com.taobao.api.domain.QueryRow;

public class ClouddateUtil {

	private static final Logger log = LoggerFactory
			.getLogger(ClouddateUtil.class);

	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

	public static String SEVEN_DAY_ITEM_COMEIN_WORD_CLICK = "seven_day_item_comein_word_click_";

	public static String SEVEN_DAY_ITEM_COMEIN_WIRELESS_WORD_CLICK = "seven_day_item_comein_ireless_word_click_";

	public static Map<String, Integer> get7DayComeInWordsMap(User user,
			Long numIid, Long interval) {
		if(interval == null) {
			interval = 14L;
		}
		if (user == null) {
			return null;
		}
		if (numIid == null) {
			return null;
		}
		Map<String, Integer> map = new HashMap<String, Integer>();
		map = (Map<String, Integer>) Cache.get(SEVEN_DAY_ITEM_COMEIN_WORD_CLICK
				+ user.getId() + numIid);
		if (map != null) {
			return map;
		}
		Long endTime = System.currentTimeMillis();
		Long startTime = endTime - (interval * DateUtil.DAY_MILLIS);
		TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(2887L, "startdate="
				+ sdf.format(new Date(startTime)) + ",sellerId=" + user.getId()
				+ ",enddate=" + sdf.format(new Date(endTime)) + ",numIid="
				+ numIid, user.getSessionKey()).call();
		if(!res.isOk()) {
			return null;
		}
		List<QueryRow> wordsRows = res.getRes();
		if (CommonUtils.isEmpty(wordsRows)) {
			return null;
		}
		map = getComeInWordsClickMap(wordsRows);
		Cache.set(SEVEN_DAY_ITEM_COMEIN_WORD_CLICK + user.getId() + numIid, map, "72h");
		return map;
	}
    
    public static Map<String, Integer> get7DayComeInWirelessWord(User user, Long numIid, Long interval){
        if(interval == null) {
            interval = 14L;
        }
        if (user == null) {
            return null;
        }
        if (numIid == null) {
            return null;
        }
        Map<String, Integer> map = new HashMap<String, Integer>();
        map = (Map<String, Integer>) Cache.get(SEVEN_DAY_ITEM_COMEIN_WIRELESS_WORD_CLICK + user.getId() + numIid);
        if (map != null) {
            return map;
        }
        Long endTime = System.currentTimeMillis();
        Long startTime = endTime - (interval * DateUtil.DAY_MILLIS);
        TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(105129L, "startdate="
                + sdf.format(new Date(startTime)) + ",sellerId=" + user.getId()
                + ",enddate=" + sdf.format(new Date(endTime)) + ",numIid="
                + numIid, user.getSessionKey()).call();
        if(!res.isOk()) {
        	return null;
        }
        List<QueryRow> wirelessWordsRows = res.getRes();
        if (CommonUtils.isEmpty(wirelessWordsRows)) {
            return null;
        }
        map = getComeInWordsClickMap(wirelessWordsRows);
        Cache.set(SEVEN_DAY_ITEM_COMEIN_WIRELESS_WORD_CLICK + user.getId() + numIid, map, "72h");
        return map;
    }

	public static Map<String, Integer> getComeInWordsClickMap(List<QueryRow> wordsRows) {
		if (CommonUtils.isEmpty(wordsRows)) {
			return null;
		}
		Map<String, Integer> map = new HashMap<String, Integer>();
		try {
			for (QueryRow row : wordsRows) {
				List<String> value = row.getValues();
				if (CommonUtils.isEmpty(value)) {
					continue;
				}
				String word = value.get(1);
				if (StringUtils.isEmpty(word)) {
					continue;
				}
				Integer click = Integer.valueOf(value.get(2));
				if (click <= 0) {
					continue;
				}

				List<String> splits = new AutoSplit(word, false).execute();
				if(CommonUtils.isEmpty(splits)) {
					continue;
				}
				for(String subWord : splits) {
					String tmp = subWord.toLowerCase();
					if (map.get(tmp) == null) {
						map.put(tmp, click);
					} else {
						map.put(tmp, Integer.valueOf(map.get(tmp)) + click);
					}
				}
				
			}
		} catch (ClientException e) {
			e.printStackTrace();
		}
		return map;
	}

	public static List<QueryRow> getComeInWords(Long startTime, Long endTime,
			User user, int offset, int limit) {
		TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(2887L, "startdate="
				+ sdf.format(new Date(startTime)) + ",sellerId=" + user.getId()
				+ ",enddate=" + sdf.format(new Date(endTime)) + ",sub_offset="
				+ offset + ",sub_limit=" + limit + ",sub_order_by=impression",
				user.getSessionKey()).call();
		List<QueryRow> wordsRows = res.getRes();
		return wordsRows;
	}
}
