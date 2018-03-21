package controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import jdp.JdpModel.JdpTradeModel;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.mvc.Controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.JsonUtil;
import com.taobao.api.domain.Trade;

import controllers.TMController.TMResultMsg;

public class TradeStatusForMohe extends Controller {
	
	private static final Logger log = LoggerFactory.getLogger(TradeStatusForMohe.class);
	
	private static String SHOP_SEARCH_URL = "http://shop.m.taobao.com/shop/shopsearch/search_page_json.do";
	
	/**
	 * 根据旺旺查询店铺信息
	 */
	public static void getShopInfo(String nick) {
		if (StringUtils.isEmpty(nick)) {
			renderError("入参nick为空！！！");
		}
		
		nick = nick.trim();
		
		// 参数拼接
		StringBuffer param = new StringBuffer();
		param.append("isb=").append("");
		param.append("&sort=").append("default");
		param.append("&type=").append("all");
		param.append("&loc=").append("");
		param.append("&fx=").append("0");
		param.append("&lp=").append("0");
		param.append("&jf=").append("0");
		param.append("&my=").append("0");
		param.append("&paytype=").append("");
		param.append("&_input_charset=").append("utf-8");
		param.append("&base64=").append("0");
		param.append("&shop_type=").append("");
		param.append("&olu=").append("");
		param.append("&q=").append(nick);
		
		String json = sendPost(SHOP_SEARCH_URL, param.toString(), "utf-8").trim();
		json = StringEscapeUtils.unescapeHtml4(json);
		
		List<ShopInfo> shopInfo = parseShopInfo(json);
		if(CommonUtils.isEmpty(shopInfo)) {
			renderError("未匹配到相关店铺数据");
		}
		
		renderSuccess("", json);
	}
	
	/**
	 * 魔盒-获取订单状态
	 */
	public static void getTradeStatus(String json) {
		if (StringUtils.isEmpty(json)) {
			renderError("入参json为空！！！");
		}
		
		List<TradeStatus> result = parseJson(json);
		if (result == null) {
			renderError("入参解析异常！！！json:" + json);
		}
		
		for (TradeStatus tradeStatus : result) {
			Trade trade = JdpTradeModel.fetchTrade(tradeStatus.getTid());
			if(trade != null) {
				tradeStatus.setStatus(trade.getStatus());
				tradeStatus.setBuyerRate(trade.getBuyerRate());
			}
		}
		
		renderSuccess("", result);
	}
	
	private static List<ShopInfo> parseShopInfo(String json) {
		
		List<ShopInfo> info = new ArrayList<ShopInfo>();
		
		if(StringUtils.isEmpty(json)) {
			return info;
		}
		
		try {
			JSONObject jsonObject = JSON.parseObject(json);
			Boolean result = jsonObject.getBoolean("result");
			
			if(!result) {
				return info;
			}
			
			JSONArray jsonArray = jsonObject.getJSONArray("listItem");
			
			for (int i = 0, l = jsonArray.size(); i < l; i++) {
				JSONObject object = jsonArray.getJSONObject(i);
				
				JSONObject shopObject = object.getJSONObject("shop");
				
				String name = shopObject.getString("name");
				String logo = shopObject.getString("logo");
				String keyBiz = shopObject.getString("keyBiz");
				Long id = shopObject.getLong("id");
				Boolean isMall = shopObject.getBoolean("isMall");
				String url = shopObject.getString("url");
				int totalSold = shopObject.getInteger("totalSold");
				
				info.add(new ShopInfo(name, logo, keyBiz, id, isMall, url, totalSold));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return info;
		
	}
	
	private static List<TradeStatus> parseJson(String json) {
		
		List<TradeStatus> result = new ArrayList<TradeStatus>();
		
		if(StringUtils.isEmpty(json)) {
			return result;
		}
		
		try {
			JSONArray jsonArray = JSON.parseArray(json);
			
			for (int i = 0, l = jsonArray.size(); i < l; i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				
				Long missionId = jsonObject.getLong("missionId");
				Long tid = jsonObject.getLong("tid");
				
				if(missionId >= 0 && tid >= 0) {
					result.add(new TradeStatus(missionId, tid, StringUtils.EMPTY, false));
				}
			}
			
			return result;
			
		} catch (JSONException e) {
			e.printStackTrace();
			return ListUtils.EMPTY_LIST;
		}
		
	}
	
	/**
	 * 向指定URL发送POST方法的请求
	 * 获取店铺信息专用
	 */
	public static String sendPost(String url, String param, String encode) {
		PrintWriter out = null;
		BufferedReader in = null;
		String result = StringUtils.EMPTY;
		if(StringUtils.isEmpty(encode)) {
			encode = "GBK";
		}
		try {
			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();
			// 设置通用的请求属性
			conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
//			conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
			conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
			conn.setRequestProperty("Cookie", "");
			conn.setRequestProperty("Host", "shop.m.taobao.com");
//			conn.setRequestProperty("Upgrade-Insecure-Requests", "1");
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");
//			conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
			// 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setDoInput(true);
			// 获取URLConnection对象对应的输出流
			out = new PrintWriter(conn.getOutputStream());
			// 发送请求参数
			out.print(param);
			// flush输出流的缓冲
			out.flush();
			// 定义BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(conn.getInputStream(), encode));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
				result += "\n";
			}
			return result;
		} catch (MalformedURLException e) {
			log.error(e.toString());;
		} catch (IOException e) {
			log.error(e.toString());
		} finally {
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(in);
		}
		return result;
	}
	
	private static class ShopInfo{
		
		private String name;
		
		private String logo;
		
		private String keyBiz;
		
		private Long id;
		
		private boolean isMall;
		
		private String url;
		
		private int totalSold;
		
		public ShopInfo() {
			super();
		}

		public ShopInfo(String name, String logo, String keyBiz, Long id,
				boolean isMall, String url, int totalSold) {
			super();
			this.name = name;
			this.logo = logo;
			this.keyBiz = keyBiz;
			this.id = id;
			this.isMall = isMall;
			this.url = url;
			this.totalSold = totalSold;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getLogo() {
			return logo;
		}

		public void setLogo(String logo) {
			this.logo = logo;
		}

		public String getKeyBiz() {
			return keyBiz;
		}

		public void setKeyBiz(String keyBiz) {
			this.keyBiz = keyBiz;
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public boolean isMall() {
			return isMall;
		}

		public void setMall(boolean isMall) {
			this.isMall = isMall;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public int getTotalSold() {
			return totalSold;
		}

		public void setTotalSold(int totalSold) {
			this.totalSold = totalSold;
		}
		
	}
	
	private static class TradeStatus{
		
		private Long missionId;
		
		private Long tid;
		
		private String status;
		
		private Boolean buyerRate;
		
		public TradeStatus(Long missionId, Long tid, String status, Boolean buyerRate) {
			super();
			this.missionId = missionId;
			this.tid = tid;
			this.status = status;
			this.buyerRate = buyerRate;
		}

		public Long getMissionId() {
			return missionId;
		}

		public void setMissionId(Long missionId) {
			this.missionId = missionId;
		}

		public Long getTid() {
			return tid;
		}

		public void setTid(Long tid) {
			this.tid = tid;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public Boolean getBuyerRate() {
			return buyerRate;
		}

		public void setBuyerRate(Boolean buyerRate) {
			this.buyerRate = buyerRate;
		}
		
	}
	
	private static void renderError(String msg) {
		TMResultMsg wmMsg = new TMResultMsg();
		wmMsg.setSuccess(false);
		wmMsg.setMessage(msg);
		renderJSON(JsonUtil.getJson(wmMsg));
	}
	
	private static void renderSuccess(String msg, Object res) {
		TMResultMsg wmMsg = new TMResultMsg();
		wmMsg.setSuccess(true);
		wmMsg.setMessage(msg);
		wmMsg.setRes(res);
		renderJSON(JsonUtil.getJson(wmMsg));
	}
	
}
