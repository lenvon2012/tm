package utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by oyster on 2017/3/29.
 */
public class CopyUtil {

    public static Pattern Alibaba_URL_PATTERN = Pattern
            .compile("offer/(\\d+).html");

	// public static Pattern Tmall_URL_PATTERN =
	// Pattern.compile("detail\\.tmall\\.com/item.htm\\?id=(\\d+)");
	public static Pattern TX_URL_PATTERN = Pattern.compile("[\\?&]id=(\\d+)");

	/**
	 * 根据url获取商品ID
	 * 
	 * @param url
	 * @return
	 */
	public static String parseItemId(String url) {
		Matcher matcher = Alibaba_URL_PATTERN.matcher(url);
        String itemId;
		if (matcher.find()) {
			itemId = matcher.group(1);
		} else {
			throw new RuntimeException("阿里巴巴宝贝链接不正确");
		}

		return itemId;
	}

	/**
	 * 将产品ID转换成对应的Url
	 * 
	 * @param itemId
	 * @return
	 */
	public static String changeToUrl(String itemId) {
		return "https://m.1688.com/offer/" + itemId + ".html";
	}
	
	/**
	 * 将产品ID转换成对应的Url
	 * 
	 * @param itemId
	 * @return
	 */
	public static String changeToUrl(Long itemId) {
		return "https://m.1688.com/offer/" + itemId + ".html";
	}

	/**
	 * 判断是否是alibaba的有效链接
	 * 
	 * @param url
	 * @return
	 */
	public static Boolean isAlibabaUrl(String url) {
		if (Alibaba_URL_PATTERN.matcher(url).find())
			return true;
		return false;

	}

	/**
	 * 根据url获取商品ID
	 * 
	 * @param url
	 * @return
	 */
	public static String parseTbAndTmItemId(String url) {
		Matcher matcher = TX_URL_PATTERN.matcher(url);

		String itemId = null;
		if (matcher.find()) {
			itemId = matcher.group(1);
		} else {
			throw new RuntimeException("宝贝链接不正确");

		}
		return itemId;
	}

}
