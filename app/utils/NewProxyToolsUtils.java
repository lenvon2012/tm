package utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import proxy.CommonProxyPools;
import proxy.NewProxyTools;

/**
 * @author create by yaoyuan
 * @date 2017年9月13日 下午5:20:10
 * @carrier wangw 2017年11月1日 下午9:11:00
 */

public class NewProxyToolsUtils {

	public static final Logger log = LoggerFactory.getLogger(NewProxyToolsUtils.class);

	public static String proxyGet(String url, String hostUrl) {
		return proxyGet(url, hostUrl, StringUtils.EMPTY, StringUtils.EMPTY);
	}
	
	public static String proxyGet(String url, String hostUrl, String userAgent) {
		return proxyGet(url, hostUrl, userAgent, StringUtils.EMPTY);
	}

	/**
	 * @param url 请求链接,hostUrl主站地址,resultHead文件头
	 */
	public static String proxyGet(String url, String hostUrl, String userAgent, String resultHead) {
		String result = StringUtils.EMPTY;
		
		if (!url.startsWith("http://hws.m.taobao.com/cache/wdetail/5.0/?id=")) {
			result = SquidProxyUtils.get(url, hostUrl, StringUtils.EMPTY, 15000, 3, userAgent);
		}

		if (isValid(result, resultHead)) {
			return result;
		}
		
		log.info("进入旧代理模式！");
		result = NewProxyTools.proxyGet(url, hostUrl, userAgent);
		
		if (isValid(result, resultHead)) {
			return result;
		}
		
		log.info("进入本地模式！");
		result = CommonProxyPools.directGet(url, hostUrl, userAgent, null, StringUtils.EMPTY);
		
		if (isValid(result, resultHead)) {
			return result;
		}
		
		return StringUtils.EMPTY;
	}
	
	private static Boolean isValid(String result, String resultHead) {
		if(StringUtils.isEmpty(result)) {
			return false;
		}
		
		if(StringUtils.isEmpty(resultHead)) {
			return true;
		}
		
		if(result.startsWith(resultHead)) {
			return true;
		}
			
		return false;
	}
	
}
