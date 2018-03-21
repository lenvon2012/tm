package itemcarrier;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.user.User;

import org.apache.commons.lang3.StringUtils;

import proxy.NewProxyTools;
import carrier.FileCarryUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class ImgTest {
	
    public static void ItemCarrier(){
//    	String description = "<img align=\"absmiddle\" src=\"http://img.alicdn.com/imgextra/i2/2208278932/TB2mG5BpVXXXXcYXpXXXXXXXXXX_!!2208278932.jpg\" />";
//
//    	Pattern pattern = Pattern.compile("(?:src=\"?)(.*?)\"?\\s");
//		Matcher m = pattern.matcher(description);
//		while (m.find()) {
//			String oldDescPictureUrl = m.group(1);
//			String newDescPictureUrl = "www.baidu.com";
//			description = description.replaceAll(oldDescPictureUrl, newDescPictureUrl);
//		}
//    	System.out.println(description);
    	Map item = new HashMap<String, String>();
    	item.put("规格（粒/袋/ml/g）", "13g*10袋");
    	String name = "规格（粒/袋/ml/g）";
    	System.out.println(item.get("规格（粒/袋/ml/g）"));
    }

    public static void main(String[] args) {
    	ImgTest.ItemCarrier();
    }

}