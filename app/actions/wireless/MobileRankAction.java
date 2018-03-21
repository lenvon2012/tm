package actions.wireless;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.api.API;

import spider.mainsearch.MainSearchKeywordsUpdater.MainSearchItemRank;

public class MobileRankAction {
	private static final Logger log = LoggerFactory.getLogger(MobileRankAction.class);

    public static final String TAG = "MobileRankAction";
    
    public static String searchPrefix = "http://s.m.taobao.com/search?&q=";
    public static String searchMidString = "&event_submit_do_new_search_auction=1&_input_charset=utf-8&topSearch=1&atype=b&searchfrom=1&action=home%3Aredirect_app_action&from=1&sst=1&n=20&buying=buyitnow&m=api4h5&abtest=8&wlsort=8";
    public static String searchSuffix = "&callback=jsonp";
    
    public static List<MainSearchItemRank> MobileSearchRank(String word, int pn, User user) {
    	if(StringUtils.isEmpty(word)) {
    		return null;
    	}
    	if(pn < 10) {
    		pn = 10;
    	}
    	if(user == null) {
    		return null;
    	}
    	List<MainSearchItemRank> ranks = new ArrayList<MainSearchItemRank>();
    	int rank = 0;
    	try {
			
			for(int i = 1; i <= pn; i++) {
				String searchUrl = searchPrefix + URLEncoder.encode(word, "utf-8") + searchMidString + "&page=" + i + searchSuffix;
	    		String res = API.directGet(searchUrl,
	    			"", null);
		    	if(StringUtils.isEmpty(res)) {
		    		continue;
		    	}
		    	res = res.replace("jsonp(", "");
				res = res.substring(0, res.length() - 1);
				res = res.trim();
				res = res.replace("\\", "\\\\");
				JSONObject obj = new JSONObject(res);
				
				JSONArray listItem = obj.getJSONArray("listItem");
				for(int j = 0; j < listItem.length(); j++) {
					rank++;
					JSONObject object = (JSONObject) listItem.get(j);
					/*String nick = object.getString("nick");
					log.info("Mobile Rank find item " + object.getLong("itemNumId") + ", title = " +
							"" + object.getString("name") + " for seller " + nick + " with rank = " +
							"" + rank);
					if(StringUtils.isEmpty(nick)) {
						continue;
					}*/
					//if(nick.equals(user.getUserNick())) {
						// 判断是否是直通车
						if(object.getString("url").indexOf("mclick.simba.taobao.com") > 0) {
							ranks.add(new MainSearchItemRank(object.getString("img2"), rank + 100000, object.getString("url"),
									object.getLong("itemNumId"), object.getString("name"), word, object.getString("nick")));
						} else {
							ranks.add(new MainSearchItemRank(object.getString("img2"), rank, object.getString("url"),
									object.getLong("itemNumId"), object.getString("name"), word, object.getString("nick")));
						}
					//}
				}
	    	}
		} 
    	catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	return ranks;
    }
}
