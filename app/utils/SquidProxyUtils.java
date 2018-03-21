package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import proxy.NewProxyTools;

public class SquidProxyUtils {

	public static final Logger LOGGER = LoggerFactory.getLogger(SquidProxyUtils.class);

	public static List<HttpHost> squidHosts = new ArrayList<HttpHost>();
	public static int size = 0;
	static Random rand = new Random();
	static {
		// if(Play.mode.isDev()) {
		// squidHosts.add(new HttpHost("192.168.11.105", 3128));
		// } else {
		////squidHosts.add(new HttpHost("182.254.148.89", 3999));// t11
		//squidHosts.add(new HttpHost("115.159.47.42", 3999));// t12
		// squidHosts.add(new HttpHost("115.159.74.104", 3999));// t23
		////squidHosts.add(new HttpHost("182.254.139.155", 3999));// t25
		////squidHosts.add(new HttpHost("115.159.25.42", 3999));// t26
		////squidHosts.add(new HttpHost("182.254.211.199", 3999));// t31
		////squidHosts.add(new HttpHost("182.254.245.96", 3999));// t32
		////squidHosts.add(new HttpHost("182.254.241.216", 3999));// t33
		////squidHosts.add(new HttpHost("115.159.67.233", 3999));// t34
		////squidHosts.add(new HttpHost("115.159.124.154", 3999));// t35
		////squidHosts.add(new HttpHost("115.159.124.90", 3999));// t36
		////squidHosts.add(new HttpHost("182.254.212.247", 3999));// t37
		////squidHosts.add(new HttpHost("182.254.134.184", 3999));// t38
		//squidHosts.add(new HttpHost("115.159.78.227", 3999));// t72
		////squidHosts.add(new HttpHost("115.159.155.138", 3999));// t73
		// squidHosts.add(new HttpHost("182.254.213.161", 3999));//t75
		////squidHosts.add(new HttpHost("115.159.5.147", 3999));// t24
		////squidHosts.add(new HttpHost("115.159.62.49", 3999));// t76
		// squidHosts.add(new HttpHost("115.159.99.11", 3999));//t77

		//squidHosts.add(new HttpHost("123.59.156.160", 3999));// uc02
		//squidHosts.add(new HttpHost("123.59.146.204", 3999));// uc01
		//squidHosts.add(new HttpHost("123.59.132.73", 3999));// uc03
		//squidHosts.add(new HttpHost("123.59.89.146", 3999));// uc04
		//squidHosts.add(new HttpHost("106.75.3.107", 3999));// uc05
		squidHosts.add(new HttpHost("106.75.5.243", 3999));// uc10
		squidHosts.add(new HttpHost("106.75.3.33", 3999));// uc11
		squidHosts.add(new HttpHost("106.75.18.123", 3999));// uc12
		squidHosts.add(new HttpHost("106.75.16.78", 3999));// uc13
		squidHosts.add(new HttpHost("106.75.35.130", 3999));// uc14
		squidHosts.add(new HttpHost("115.29.11.100", 3999));// bbn40
		squidHosts.add(new HttpHost("112.124.18.136", 3999));// bbn38
		squidHosts.add(new HttpHost("115.29.244.183", 3999));// bbn39
		squidHosts.add(new HttpHost("121.199.28.225", 3999));// bbn36
		squidHosts.add(new HttpHost("115.29.244.136", 3999));// bbn34
		squidHosts.add(new HttpHost("115.29.162.137", 3999));// bbn33
		squidHosts.add(new HttpHost("115.29.193.58", 3999));// bbn31
		squidHosts.add(new HttpHost("112.124.43.119", 3999));// bbn32
		squidHosts.add(new HttpHost("115.29.161.96", 3999));// bbn30
		squidHosts.add(new HttpHost("115.29.162.138", 3999));// bbn29
		squidHosts.add(new HttpHost("115.29.175.97", 3999));// bbn26
		// squidHosts.add(new HttpHost("115.29.175.32", 3999));// bbn25
		squidHosts.add(new HttpHost("112.124.6.50", 3999));// bbn24
		squidHosts.add(new HttpHost("121.199.0.70", 3999));// bbn10
		squidHosts.add(new HttpHost("121.40.43.205", 3999));// bbn09
		squidHosts.add(new HttpHost("121.40.42.170", 3999));// bbn08
		squidHosts.add(new HttpHost("121.40.42.202", 3999));// bbn06
		squidHosts.add(new HttpHost("121.40.43.163", 3999));// bbn04
		squidHosts.add(new HttpHost("42.121.136.84", 3999));// bbn03
		squidHosts.add(new HttpHost("42.121.112.102", 3999));// bbn02
		// }
		size = squidHosts.size();
	}

	public static HttpHost getHost() {
		return squidHosts.get(rand.nextInt(size));
	}

	public static String squidGet(String url, String hostUrl, String resultHead, int retryNum) {
		String resultMsg = "";
		for (int i = 0; i < retryNum; i++) {
			resultMsg = NewProxyTools.proxyGet(url, hostUrl);
			if (!StringUtils.isEmpty(resultMsg)) {
				if (resultMsg.startsWith(resultHead)) {
					return resultMsg;
				}
			}
		}
		return null;
	}

	public static String get(String url, String referer, String cookies, int timeout, int retryTime, String userAgent) {
		return get(url, referer, cookies, timeout, retryTime, userAgent, null);
	}

	public static String get(String url, String referer, String cookies, int timeout, int retryTime, String userAgent, HttpHost host) {
		if (timeout < 3000) {
			timeout = 3000;
		}

		if (retryTime < 10) {
			retryTime = 10;
		}

		if (userAgent == null) {
			userAgent = NewProxyTools.DEFAULT_UA;
		}

		HttpClient httpClient = HttpClients.createDefault();

		HttpGet httpGet = new HttpGet(url);
		if (referer != null) {
			httpGet.addHeader(HttpHeaders.REFERER, referer);
		}
		if (cookies != null) {
			httpGet.addHeader("Cookie", cookies);
		}

		httpGet.addHeader(HttpHeaders.USER_AGENT, userAgent);

		HttpHost tmlHost = host;

		for (int i = 0; i < retryTime; i++) {
			if (tmlHost == null) {
				host = getHost();
			}
			LOGGER.error("search by host " + host.getHostName());

			RequestConfig requestConfig = RequestConfig.custom().setMaxRedirects(20).setCircularRedirectsAllowed(true).setConnectTimeout(timeout)
					.setSocketTimeout(timeout).setProxy(host).build();
			httpGet.setConfig(requestConfig);

			String content = NewProxyTools.getContent(httpClient, httpGet, null);
			if (StringUtils.isNotEmpty(content)) {
				return content;
			}
		}
		return null;
	}
}
