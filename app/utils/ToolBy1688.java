package utils;

import actions.ItemGetAction;
import bustbapi.LogisticsApi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.ocean.rawsdk.ApiExecutor;
import com.alibaba.product.param.AlibabaAgentProductGetParam;
import com.alibaba.product.param.AlibabaAgentProductGetResult;
import com.alibaba.product.param.AlibabaProductProductInfo;
import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.api.API;
import com.ciaosir.client.utils.NumberUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.taobao.api.ApiException;
import com.taobao.api.domain.AddressResult;
import com.taobao.api.domain.Feature;
import com.taobao.api.domain.ItemProp;
import com.taobao.api.domain.ItemTaoSirElDO;
import com.taobao.api.domain.ItemTaosirDO;
import com.taobao.api.domain.Location;
import com.taobao.api.domain.PropImg;
import com.taobao.api.domain.PropValue;
import com.taobao.api.domain.Sku;
import com.taobao.api.response.ItemAddResponse;

import configs.TMConfigs.Sale;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.item.ItemCatPlay;
import models.itemCopy.APiConfig1688;
import models.itemCopy.ItemCatProps;
import models.itemCopy.NotCustomizableProp;
import models.itemCopy.PriceUnit;
import models.itemCopy.SkuInfo;
import models.itemCopy.SkuProps;
import models.itemCopy.dto.PropDto;
import models.itemCopy.dto.SalePropDto;
import models.itemCopy.dto.SalePropModel;
import models.itemCopy.dto.SkuDto;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import proxy.NewProxyTools;
import result.TMResult;
import utils.oyster.Levenshtein;

/**
 * @author Oyster
 */
public class ToolBy1688 {

	/* 设置网页抓取响应时间 */
	private static final int TIMEOUT = 10000;

	private Document document;

	private Gson gson;

	private int index = 0;

	// 自定义属性的index,-1开始，自减1
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public static final Logger log = LoggerFactory.getLogger(ToolBy1688.class);

	public static final String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36";

	public String getUrl() {
		return url;
	}

	public ToolBy1688 setUrl(String url) {
		this.url = url;
		return this;
	}

	private String url;

	/**
	 * 商品主图
	 * 
	 * @return
	 */
	public String getPicPath() {
		return picPath;
	}

	private String picPath;

	private JSONObject itemInfo;

	public ToolBy1688(String url) {
		this.url = url;
		gson = new Gson();
		String referer = url.substring(0, url.indexOf("offer"));
		try {
			String content = "";

			if (Play.id.equalsIgnoreCase("oyster") == true) {
//				content = NewProxyTools.proxyGet(url, referer, "", 10000, 5);
				content=Jsoup.connect(url).userAgent(userAgent)
						.referrer(referer).timeout(TIMEOUT).get().html();
			} else {
				content = SquidProxyUtils.get(url, referer, null, 10000, 5, "");
			}

			// ;url, referrer);
			if (isBadContent(content) == true) {
				content = NewProxyTools.proxyGet(url, referer, "", 10000, 5);
				if (isBadContent(content)) {
					document = Jsoup.connect(url).userAgent(userAgent)
							.referrer(referer).timeout(TIMEOUT).get();
				} else {
					document = JsoupUtil.parseJsoupDocument(content);
				}
			} else {
				document = JsoupUtil.parseJsoupDocument(content);
			}
			setItemInfo();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean isBadContent(String content) {
		if (StringUtils.isEmpty(content)) {
			log.error("ToolBy1688 isBadContent empty");
			return true;
		}

		if (content.indexOf("Bad Request") > 0
				|| content.indexOf("Your requested URL was not found") > 0
				|| content.indexOf("hr noshade size=") > 0
				|| content.indexOf("Moved Permanently") > 0
				|| content.indexOf("502 Bad Gateway") > 0
				|| content.contains("404 Not Found")
				|| content.indexOf("Maximum number") > 0
				|| content.indexOf("403 Forbidden") > 0
				|| content.indexOf("Connect request not authorized") > 0
				|| content
						.indexOf("You are not allowed to access the document") > 0
				|| content.indexOf("Access Denied") > 0
				|| content
						.indexOf("The web server software is running but no content has been added") > 0
						||content.indexOf("The requested URL could not be retrieved")>0) {
			log.error("ToolBy1688 isBadContent true");
			return true;
		}
		return false;
	}

	/**
	 * 获取商品展示图信息,同时给主图赋值
	 * 
	 * @return 返回封装所有获取到的展示图的集合
	 */
	public List<String> getImgUrl() {
		List<String> imgList = new ArrayList<String>();
		// 获取展示图
		Elements imgSrc = document.select("li.tab-trigger");
		for (int i = 0; i < imgSrc.size(); i++) {
			String src = imgSrc.get(i).attr("data-imgs").toString();
			Map<String, String> imgSrcMap = new HashMap<String, String>();
			imgSrcMap = gson.fromJson(src,
					new TypeToken<Map<String, String>>() {
					}.getType());
			String url = imgSrcMap.get("original");
			if (i == 0) {
				picPath = url;
				continue;
			}
			// System.out.println("次图地址："+url);
			imgList.add(url);

		}

		return imgList;
	}

	// /**
	// * 获取价格信息
	// *
	// * @return
	// * @throws IOException
	// */
	// public Double getSkuPriceInfo() {
	// Elements pricesE = document.select(".price .value");
	//
	// List<Double> prices = new ArrayList<Double>();
	// log.info("get price element size:" + pricesE.size());
	// for (Element e : pricesE) {
	// // 获取价格区间信息
	// String price = e.html();
	// if (CommonUtil.isNumber(price)) {
	// prices.add(Double.valueOf(price));
	// log.info("price:" + price);
	// }
	// }
	//
	// return Collections.max(prices);
	// }
	//

	/**
	 * 获取详细信息
	 * 
	 * @return
	 */
	public String getDetailInfo() {
		Elements props = document.select("td.de-feature");
		// Map<String,String> maps=new HashMap<>();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < props.size(); i++) {
			Element e = props.get(i);
			String key = e.html();
			if (key == null || key.replace(" ", "").equals(""))
				continue;
			String value = e.nextElementSibling().html();
			sb.append(key + ":" + value + ";");
			// maps.put(key,value);
		}
		return sb.toString().substring(0, sb.length() - 1);
	}

	/**
	 * 获取宝贝标题
	 * 
	 * @return
	 */
	public String getTitle() {
		Elements title = document.select("h1[class=d-title]");
		log.info("宝贝标题：" + title.html());
		return title.html();
	}

	/**
	 * 获取网页keywords
	 * 
	 * @return
	 */
	public String getPageTitle() {
		Elements title = document.getElementsByAttributeValue("name",
				"keywords");
		log.info("网页keywords：" + title.attr("content"));
		return title.attr("content");
	}

	/**
	 * 获取发货地址
	 * 
	 * @return 第一个元素省份,第二个元素城市
	 */
	public String[] getLocation() {
		Elements address = document.select("span.delivery-addr");
		return address.html().split(" ");
	}

	/**
	 * 获取Url中对应的数据
	 * 
	 * @param urlPath
	 * @param charset
	 *            如果为null,则默认采用gbk的编码
	 * @return
	 * @throws Exception
	 */
	public static String getJsonString(String urlPath, String charset)
			throws Exception {
		if (charset == null) {
			charset = "gbk";
		}
		if (CommonUtil.isNullOrEmpty(urlPath)) {
			return null;
		}
		if (urlPath.startsWith("http") == false) {
			urlPath = "https:" + urlPath;
		}
		URL url = new URL(urlPath);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.connect();
		InputStream inputStream = connection.getInputStream();
		// 对应的字符编码转换
		Reader reader = new InputStreamReader(inputStream, charset);
		BufferedReader bufferedReader = new BufferedReader(reader);
		String str = null;
		StringBuffer sb = new StringBuffer();
		while ((str = bufferedReader.readLine()) != null) {
			sb.append(str);
		}
		reader.close();
		connection.disconnect();
		String result = "";
		if (sb.toString().contains("offer_details")) {
			result = sb.toString()
					.substring(30, sb.toString().lastIndexOf("\""))
					.replace("\\", "");
		} else {
			result = sb.toString()
					.substring(sb.indexOf("'") + 1, sb.lastIndexOf("'"))
					.replace("\\", "");
		}
		return result;
	}

	/**
	 * 获取商品描述详情信息
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getItemDesc(String charset) {

		try {
			String descUrl = document.getElementById("desc-lazyload-container")
					.attr("data-tfs-url");
			return getJsonString(descUrl, charset);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 无sku情况下获取商品数量
	 * 
	 * @return 获取不到直接返回1000
	 */
	public Long getNumsWithoutSku() {
		Matcher m;
		try {
			Elements numE = document.select(".total");
			String numSpan = numE.get(0).html();
			String regEx = "[^0-9]";
			Pattern p = Pattern.compile(regEx);
			m = p.matcher(numSpan);
			return Long.valueOf(m.replaceAll("").trim());
		} catch (Exception e) {
			log.warn("get amount error,So what?");
		}
		return 1000L;

	}

	/**
	 * 获取对应淘宝信息的SKU
	 * 
	 * @param cid
	 *            指定类目
	 */
	public SkuDto getSkus(Long cid) throws IOException {
		/* 用來封装要保存的参数 */
		Document document = Jsoup.connect(url).timeout(TIMEOUT).get();
		/* 取得script下面的JS变量 */
		Elements e = document.getElementsByTag("script").eq(10);
		Pattern pattern = Pattern.compile("var");
		Matcher matcher = pattern.matcher(e.get(0).data());
		int i = 0;
		while (matcher.find()) {
			i++;
		}
		// 检验是否匹配正确的script
		if (i != 2) {
			e = document.getElementsByTag("script").eq(9);
		}
		/* 循环遍历script下面的JS变量 */
		for (Element element : e) {
			/* 取得JS变量数组 */
			String[] data = element.data().replace("\n", "").replace("\t", "")
					.replace(";", "").replace(" ", "").toString().split("var");
			/* 取得单个JS变量 */
			for (String variable : data) {
				/* 过滤variable为空的数据 */
				if (variable.contains("=")) {
					String kvp = null;
					// 判断是否是sku相关的json串，如果是，截取到最后一个}
					if (variable.contains("iDetailData")) {
						variable = variable.substring(0,
								variable.lastIndexOf("}") + 1);
						kvp = variable.split("=")[1];
						// System.out.println(kvp);
						JSONObject jb = JSON.parseObject(kvp);

						JSONObject skuMap = jb.getJSONObject("sku")
								.getJSONObject("skuMap");

						List<String> properties = new ArrayList<String>();
						// 获取数量串
						List<String> quantities = new ArrayList<String>();
						// 获取价格串
						// List<String> prices=new ArrayList<String>();

						for (Map.Entry<String, Object> entry : skuMap
								.entrySet()) {
							JSONObject val = JSON.parseObject(entry.getValue()
									.toString());
							// 避免部分规格属性造成的SKU价格与一口价不一致，不取价格
							// 判断是否是折扣价格还是正价，优先折扣价
							/*
							 * String price=""; if
							 * (CommonUtil.isNullOrEmpty(val.
							 * get("discountPrice"))){ if
							 * (!CommonUtil.isNullOrEmpty(val.get("price"))){
							 * price=val.get("price").toString(); } }else {
							 * price=val.get("discountPrice").toString(); }
							 * prices.add(price);
							 */
							quantities.add(val.get("canBookCount").toString());
							properties.add(entry.getKey());
						}
						// 获取SKU图片
						List<PropImg> propImgs = new ArrayList<PropImg>();

						//
						Map<String, List<String>> skuPropsMap = new HashMap<String, List<String>>();

						// 存在多种规格的情况
						JSONArray skuProps = jb.getJSONObject("sku")
								.getJSONArray("skuProps");
						// .getJSONArray("value");

						// 遍历对应的规格（例如颜色，尺码，容量等）
						for (int j = 0; j < skuProps.size(); j++) {
							// 取得当前遍历的规格
							JSONObject skuProp = skuProps.getJSONObject(j);
							// 当前规格名称
							String propName = skuProp.getString("prop");
							// 取得当前规格的值的集合
							JSONArray spVal = skuProp.getJSONArray("value");

							List<String> skuPropVal = new ArrayList<String>();
							// 当前遍历的规格对应SkuProp属性的值的集合
							for (int k = 0; k < spVal.size(); k++) {
								JSONObject urlAndName = spVal.getJSONObject(k);
								String imgUrl = urlAndName
										.getString("imageUrl");
								String name = urlAndName.getString("name");
								if (!CommonUtil.isNullOrEmpty(imgUrl)) {
									PropImg propImg = new PropImg();
									propImg.setPosition((long) (k + 1));
									propImg.setUrl(imgUrl);
									String pandv = genratorPandV(cid, name,
											propName);
									propImg.setProperties(pandv);
									propImgs.add(propImg);
								}
								skuPropVal.add(urlAndName.getString("name"));
							}
							skuPropsMap.put(propName, skuPropVal);
						}
						// 自定义销售属性
						StringBuffer inputCustomCpvs = new StringBuffer();
						// 转换成真实的PandV数据
						for (Entry<String, List<String>> entry : skuPropsMap
								.entrySet()) {
							String propName = entry.getKey();
							List<String> names = entry.getValue();
							List<String> pandvs = new ArrayList<String>();
							// 将集合中的数据转换成PID:VID的形式
							for (String name : names) {
								String pandvStr = genratorPandV(cid, name,
										propName);
								if (pandvStr != null
										&& !pandvStr.contains("null")) {
									pandvs.add(pandvStr);
								}
								// 如果有负数时自定义销售属性
								if (!CommonUtil.isNullOrEmpty(pandvStr)
										&& pandvStr.contains("-")) {
									inputCustomCpvs.append(pandvStr + ":"
											+ name + ";");
								}
							}
							skuPropsMap.put(propName, pandvs);
						}

						List<Sku> skus = new ArrayList<Sku>();

						List<String> skuProptiesStr = new ArrayList<String>();
						// 生成真实数量的SKU
						for (Entry<String, List<String>> entry : skuPropsMap
								.entrySet()) {
							// String propName=entry.getKey();
							List<String> names = entry.getValue();
							skuProptiesStr = (List<String>) CommonUtil
									.mergeList(names, skuProptiesStr);
						}
						for (int j = 0; j < skuProptiesStr.size(); j++) {
							try {
								Sku sku = new Sku();
								// 避免部分规格属性造成的SKU价格与一口价不一致，不取价格
								/*
								 * if (!CommonUtil.isNullOrEmpty(prices.get(j)))
								 * { sku.setPrice(prices.get(j)); }
								 */
								sku.setQuantity(Long.valueOf(quantities.get(j)));
								sku.setProperties(skuProptiesStr.get(j));
								skus.add(sku);
							} catch (Exception e2) {
								log.error(e2.getMessage());
								continue;
							}

						}
						return new SkuDto(skus, inputCustomCpvs.toString(),
								propImgs);
					}
				}
			}
		}
		return null;

	}

	/**
	 * 获取销售属性信息(含图片)
	 * 
	 * @return
	 */
	public SalePropDto getSaleProp() throws IOException {
		/* 用來封装要保存的参数 */
		Document document = Jsoup.connect(url).timeout(TIMEOUT).get();
		/* 取得script下面的JS变量 */
		Elements e = document.getElementsByTag("script").eq(10);
		Pattern pattern = Pattern.compile("var");
		Matcher matcher = pattern.matcher(e.get(0).data());
		int i = 0;
		while (matcher.find()) {
			i++;
		}
		// 检验是否匹配正确的script
		if (i != 2) {
			e = document.getElementsByTag("script").eq(9);
		}
		/* 循环遍历script下面的JS变量 */
		for (Element element : e) {
			/* 取得JS变量数组 */
			String[] data = element.data().replace("\n", "").replace("\t", "")
					.replace(";", "").replace(" ", "").toString().split("var");
			/* 取得单个JS变量 */
			for (String variable : data) {
				/* 过滤variable为空的数据 */
				if (variable.contains("=")) {
					String kvp = null;
					// 判断是否是sku相关的json串，如果是，截取到最后一个}
					if (variable.contains("iDetailData")) {
						variable = variable.substring(0,
								variable.lastIndexOf("}") + 1);
						kvp = variable.split("=")[1];
						JSONObject jb = JSON.parseObject(kvp);
						// 存在多种规格的情况
						JSONArray skuProps = jb.getJSONObject("sku")
								.getJSONArray("skuProps");
						List<SalePropModel> colors = new ArrayList<SalePropModel>();
						List<SalePropModel> others = new ArrayList<SalePropModel>();
						// 遍历对应的规格（例如颜色，尺码，容量等）
						for (int j = 0; j < skuProps.size(); j++) {
							// 取得当前遍历的规格
							JSONObject skuProp = skuProps.getJSONObject(j);
							// 当前规格名称
							String propName = skuProp.getString("prop");
							// 取得当前规格的值的集合
							JSONArray spVal = skuProp.getJSONArray("value");
							// 当前遍历的规格对应SkuProp属性的值的集合
							for (int k = 0; k < spVal.size(); k++) {
								SalePropModel spm = new SalePropModel();
								JSONObject urlAndName = spVal.getJSONObject(k);
								// 属性别名
								String aliasName = urlAndName.getString("name");
								spm.setAliasName(aliasName);
								// 属性图片
								String imgUrl = urlAndName
										.getString("imageUrl");
								spm.setImgUrl(imgUrl);
								if (propName.contains("颜色")) {
									colors.add(spm);
								} else {
									others.add(spm);
								}
							}
						}

						return new SalePropDto(colors, others);
					}
				}
			}
		}
		return null;

	}

	/**
	 * 根据cid,prop的中文字符串生成对应PID:VID格式的对应数据
	 * 
	 * @param cid
	 *            类目
	 * @param name
	 *            对应的属性值名称
	 * @param propname
	 *            对应的属性名称
	 * @return
	 */
	public String genratorPandV(Long cid, String name, String propname) {
		Long pid = null;
		// 查询数据库中有无对应标准SKU
		SkuProps props = SkuProps.getSkuProps(cid, name, false);
		if (props != null) {
			return GenPav(props);
		} else {
			// 查询表中是否有对应记录
			Boolean exsit = SkuProps.checkExsitCid(cid);
			if (exsit) {
				// 模糊匹配查询是否存在记录
				props = SkuProps.getSkuProps(cid, name, true);
				if (props == null) {
					// 根据属性名称查询PID
					pid = SkuProps.getPidByCidAndPropName(cid, propname);
					//剔除部分类目无法自定义销售属性的sku
					//童装-参考身高 cid==124216006&&pid==122216343
//					if (cid==124216006&&pid==122216343) {
//						
//					}
					if (pid != null) {
						NotCustomizableProp prop=NotCustomizableProp.getPropCidAndPid(cid, pid);
						if (prop!=null) {
							return null;
						}
						index--;
						return pid + ":" + index;
					} else {
						// 将尺码转换成身高属性
						if (propname.contains("尺码")) {
							pid = SkuProps.getPidByCidAndPropName(cid, "身高");
							if (pid == null) {
								return null;
							}
							index--;
							return pid + ":" + index;
						}
						return null;
					}
				} else {
					return GenPav(props);
				}
			} else {
				// 调用Api获取所有的销售属性信息并填充到数据库
				Integer addRows = ApiUtil.getAndSaveSkuProp(cid);
				if (addRows != null && addRows > 0) {
					props = SkuProps.getSkuProps(cid, name, false);
					if (props != null) {
						// 查询该PID是否属于该类目
						return GenPav(props);
					} else {
						// 模糊匹配查询是否存在记录
						props = SkuProps.getSkuProps(cid, name, true);
						if (props == null) {
							// 根据属性名称查询PID
							pid = SkuProps
									.getPidByCidAndPropName(cid, propname);
							if (pid != null) {
								index--;
								return pid + ":" + index;
							} else {
								return null;
							}
						}
					}
				}
			}
		}
		return null;

	}

	private String GenPav(SkuProps props) {
		return props.getPid() + ":" + props.getVid();
	}

	/**
	 * 根据cid获取props
	 * 
	 * @param cid
	 * @return
	 */
	public PropDto getPropsByCid(Long cid) {
		PropDto propDto = new PropDto();
		// 1.查询数据库中是否有对应的记录
		ItemCatProps icp = ItemCatProps.getPropStrByCid(cid);
		if (icp != null) {
			propDto.setProps(icp.getProps());
			propDto.setInputPids(icp.getInputPids());
			propDto.setInputStr(CommonUtil.genratorInputStr(propDto
					.getInputPids()));
		} else {
			// 2.不存在记录则进行Api查询，查询成功后保存到数据库
			propDto = ApiUtil.getPropsByCid(cid);
			if (propDto != null) {
				ItemCatProps itemCatProps = new ItemCatProps(cid,
						propDto.getProps(), propDto.getInputPids());
				if (!itemCatProps.jdbcSave()) {
					log.error("添加ItemCatProps没有成功，" + itemCatProps);
				}
			}
		}
		return propDto;
	}

	public JSONObject getItemInfo() {
		return itemInfo;
	}

	/**
	 * 获取相关信息
	 * 
	 * @return
	 * @throws Exception
	 */
	public void setItemInfo() throws Exception {
		Elements scripts = document.getElementsByTag("script");
		if (scripts.size() == 0) {
			log.error("===========>>>>>.未取得数据源。url:" + url);
			return;
		}
		Elements e = scripts.eq(6);
		for (int i = 7; i < scripts.size(); i++) {
			if (e.html().indexOf("wingxViewData[0]") != -1) {
				break;
			}
			e = document.getElementsByTag("script").eq(i);
		}
		String itemJOStr = e.html();
		itemJOStr = itemJOStr.substring(itemJOStr.indexOf("={") + 1);
		this.itemInfo = JSONObject.parseObject(itemJOStr);

	}

	/**
	 * 获取宝贝标题
	 * 
	 * @return
	 */
	public String getMTitle() {
		return itemInfo.getString("subject");
	}

	/**
	 * 获取宝贝价格
	 * 
	 * @return
	 */
	public Double getMPrice() {
		JSONArray priceArr = itemInfo.getJSONArray("priceRanges");
		List<Double> prices = new ArrayList<Double>();
		log.info("get price element size:" + priceArr.size());
		for (int i = 0; i < priceArr.size(); i++) {
			JSONObject priceJo = priceArr.getJSONObject(i);
			String price = priceJo.getString("price");
			if (CommonUtil.isNumber(price)) {
				prices.add(Double.valueOf(price));
				log.info("price:" + price);
			}
		}

		return Collections.max(prices);
	}

	/**
	 * 获取宝贝价格与优惠价
	 * 
	 * @return
	 */
	public PriceUnit[] getDiscountPrice() {
		JSONArray priceArr = itemInfo.getJSONArray("priceRanges");
		PriceUnit[] priceUnits = new PriceUnit[1];
		log.info("get price element size:" + priceArr.size());
		// int size= 1688默认不取优惠价
		for (int i = 0; i < 1; i++) {
			JSONObject priceJo = priceArr.getJSONObject(i);
			String price = priceJo.getString("price");
			String convertPrice = priceJo.getString("convertPrice");
			priceUnits[i] = new PriceUnit().setPrice(price).setDisplay(
					convertPrice);
		}

		return priceUnits;
	}

	/**
	 * 获取宝贝数量
	 * 
	 * @return
	 */
	public Long getMNums() {
		return itemInfo.getLong("canBookedAmount");
	}

	/**
	 * 获取发货地址
	 * 
	 * @return
	 */
	public String getMLocation() {
		return itemInfo.getJSONObject("freightInfo").getString("location");
	}

	/**
	 * 获取发货地址
	 * 
	 * @return
	 */
	public Location getObjLocation() {
		List<String> zxs = Arrays.asList("北京", "上海", "重庆", "天津");
		String location = itemInfo.getJSONObject("freightInfo").getString(
				"location");
		if (StringUtils.isEmpty(location)) {
			return null;
		}
		Location local = new Location();
		if (location.startsWith("内蒙古") || location.startsWith("黑龙江")) {
			local.setState(location.substring(0, 3));
			local.setCity(location.substring(3));
		} else {
			if (zxs.contains(location)) {
				local.setState(location);
				local.setCity(location);
			} else {
				local.setState(location.substring(0, 2));
				local.setCity(location.substring(2));
			}

		}
		return local;
	}

	public List<String> getMImgUrl() {
		List<String> imgList = new ArrayList<String>();
		// 获取展示图
		JSONArray imageArr = itemInfo.getJSONArray("imageList");
		for (int i = 0; i < imageArr.size(); i++) {
			String url = imageArr.getJSONObject(i)
					.getString("originalImageURI");
			imgList.add(url);
			if (i == 0) {
				picPath = url;
			}

		}

		return imgList;
	}

	public String getMDesc() {
		try {
			String descUrl = itemInfo.getString("detailUrl");
			return getJsonString(descUrl, "gbk");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "描述信息获取失败，请尝试手动复制";
	}

	public Map<String, String> getMProps() {
		Map<String, String> propMap = new HashMap<String, String>();
		JSONArray propArray = itemInfo.getJSONArray("productFeatureList");

		for (int i = 0; i < propArray.size(); i++) {
			JSONObject data = propArray.getJSONObject(i);
			propMap.put(data.getString("name"), data.getString("value"));
		}
		return propMap;
	}

	/**
	 * 获取对应淘宝信息的SKU
	 * 
	 * @param cid
	 *            指定类目
	 */
	public SkuDto getMSkus(Long cid) throws IOException {

		JSONObject skuMap = itemInfo.getJSONObject("skuMap");

		List<String> properties = new ArrayList<String>();
		// 获取数量串
		List<String> quantities = new ArrayList<String>();
		// 获取价格串
		// List<String> prices=new ArrayList<String>();
		if (skuMap != null)
			for (Map.Entry<String, Object> entry : skuMap.entrySet()) {
				JSONObject val = JSON.parseObject(entry.getValue().toString());
				// 避免部分规格属性造成的SKU价格与一口价不一致，不取价格
				quantities.add(val.get("canBookCount").toString());
				properties.add(entry.getKey());
			}
		// 获取SKU图片
		List<PropImg> propImgs = new ArrayList<PropImg>();

		//
		Map<String, List<String>> skuPropsMap = new HashMap<String, List<String>>();

		// 存在多种规格的情况
		JSONArray skuProps = itemInfo.getJSONArray("skuProps");
		// .getJSONArray("value");

		// 遍历对应的规格（例如颜色，尺码，容量等）
		if (skuProps != null)
			for (int j = 0; j < skuProps.size(); j++) {
				// 取得当前遍历的规格
				JSONObject skuProp = skuProps.getJSONObject(j);
				// 当前规格名称
				String propName = skuProp.getString("prop");
				// 取得当前规格的值的集合
				JSONArray spVal = skuProp.getJSONArray("value");

				List<String> skuPropVal = new ArrayList<String>();
				int size = spVal.size() > 24 ? 24 : spVal.size();
				// 当前遍历的规格对应SkuProp属性的值的集合
				for (int k = 0; k < size; k++) {
					JSONObject urlAndName = spVal.getJSONObject(k);
					String imgUrl = urlAndName.getString("imageUrl");
					String name = urlAndName.getString("name");
					if (!CommonUtil.isNullOrEmpty(imgUrl)) {
						PropImg propImg = new PropImg();
						propImg.setPosition((long) (k + 1));
						propImg.setUrl(imgUrl);
						String pandv = genratorPandV(cid, name, propName);
						propImg.setProperties(pandv);
						propImgs.add(propImg);
					}
					skuPropVal.add(urlAndName.getString("name"));
				}
				skuPropsMap.put(propName, skuPropVal);
			}
		// 自定义销售属性
		StringBuffer inputCustomCpvs = new StringBuffer();
		// 转换成真实的PandV数据
		for (Entry<String, List<String>> entry : skuPropsMap.entrySet()) {
			String propName = entry.getKey();
			List<String> names = entry.getValue();
			List<String> pandvs = new ArrayList<String>();
			// 将集合中的数据转换成PID:VID的形式
			for (String name : names) {
				String pandvStr = genratorPandV(cid, name, propName);
				if (pandvStr==null) {
					continue; //不支持自定义的销售属性跳过
				}
				if (pandvStr != null && !pandvStr.contains("null")) {
					pandvs.add(pandvStr);
				}
				// 如果有负数时自定义销售属性
				if (!CommonUtil.isNullOrEmpty(pandvStr)
						&& pandvStr.contains("-")) {
					// 修复自定义属性长度限制 并且去重复项
					String cusName = fixSalePropCustomPvalue(name);
					if (inputCustomCpvs.toString().contains(cusName)) {
						if (inputCustomCpvs.toString().indexOf(cusName + "，") > 1) {
							cusName = cusName + "，，";
						} else {
							cusName = cusName + "，";
						}

					}
					inputCustomCpvs.append(pandvStr + ":" + cusName + ";");
				}
			}
			skuPropsMap.put(propName, pandvs);
		}

		List<Sku> skus = new ArrayList<Sku>();

		List<String> skuProptiesStr = new ArrayList<String>();
		// 生成真实数量的SKU
		for (Entry<String, List<String>> entry : skuPropsMap.entrySet()) {
			List<String> names = entry.getValue();
			skuProptiesStr = (List<String>) CommonUtil.mergeList(names,
					skuProptiesStr);
		}
		for (int j = 0; j < skuProptiesStr.size(); j++) {
			try {
				Sku sku = new Sku();
				// 避免部分规格属性造成的SKU价格与一口价不一致，不取价格
				/*
				 * if (!CommonUtil.isNullOrEmpty(prices.get(j))) {
				 * sku.setPrice(prices.get(j)); }
				 */
				sku.setQuantity(Long.valueOf(quantities.get(j)));
				sku.setProperties(skuProptiesStr.get(j));
				skus.add(sku);
			} catch (Exception e2) {
				log.error(e2.getMessage());
				continue;
			}

		}
		return new SkuDto(skus, inputCustomCpvs.toString(), propImgs);

	}

	public SalePropDto getMSaleProp() throws IOException {
		// 存在多种规格的情况
		JSONArray skuProps = itemInfo.getJSONArray("skuProps");
		List<SalePropModel> colors = new ArrayList<SalePropModel>();
		List<SalePropModel> others = new ArrayList<SalePropModel>();
		// 遍历对应的规格（例如颜色，尺码，容量等）
		for (int j = 0; j < skuProps.size(); j++) {
			// 取得当前遍历的规格
			JSONObject skuProp = skuProps.getJSONObject(j);
			// 当前规格名称
			String propName = skuProp.getString("prop");
			// 取得当前规格的值的集合
			JSONArray spVal = skuProp.getJSONArray("value");
			// 当前遍历的规格对应SkuProp属性的值的集合
			for (int k = 0; k < spVal.size(); k++) {
				SalePropModel spm = new SalePropModel();
				JSONObject urlAndName = spVal.getJSONObject(k);
				// 属性别名
				String aliasName = urlAndName.getString("name");
				spm.setAliasName(aliasName);
				// 属性图片
				String imgUrl = urlAndName.getString("imageUrl");
				spm.setImgUrl(imgUrl);
				if (propName.contains("颜色")) {
					colors.add(spm);
				} else {
					others.add(spm);
				}
			}
		}

		return new SalePropDto(colors, others);

	}

	/**
	 * 根据阿里数据源属性数据生成淘宝属性数据
	 * 
	 * @param cid
	 *            类目
	 * @param propMap
	 *            数据源键值对
	 * @return
	 */
	public PropDto getPropsByCid(Long cid, Map<String, String> propMap) {
		PropDto pd = new PropDto();
		StringBuffer propSb = new StringBuffer();
		List<ItemProp> itemProps = ApiUtil.getItemProps(cid);
		if (CommonUtil.isNullOrSizeZero(itemProps)) {
			return null;
		}
		StringBuffer inputPids = new StringBuffer();

		StringBuffer inputStr = new StringBuffer();
		// 关于度量衡属性的pid与str
		StringBuffer dlhPid = new StringBuffer();
		StringBuffer dlhStr = new StringBuffer();
		// 遍历必备属性
		for (int i = 0; i < itemProps.size(); i++) {
			ItemProp itemProp = itemProps.get(i);
			if (itemProp == null || !itemProp.getMust()
					|| itemProp.getIsSaleProp())
				continue;
			StringBuffer tsdVal = new StringBuffer();
			// 判断是否是度量衡属性
			if (itemProp.getIsTaosir()) {
				ItemTaosirDO tsd = itemProp.getTaosirDo();
				dlhPid.append(itemProp.getPid() + ",");
				String str = "";
				// 判断是否是普通度量衡属性
				if (CommonUtil.isNullOrSizeZero(tsd.getExprElList())) {
					// 单位
					List<Feature> features = tsd.getStdUnitList();
					if (CommonUtil.isNullOrSizeZero(features)) {
						continue;
					}
					str = "1" + tsd.getStdUnitList().get(0).getAttrValue();
					tsdVal.append("1"
							+ tsd.getStdUnitList().get(0).getAttrValue());
				} else {
					// 表达式度量衡属性
					List<ItemTaoSirElDO> itses = tsd.getExprElList();
					for (int j = 1; j < itses.size(); j++) {
						ItemTaoSirElDO itse = itses.get(j);
						if (itse.getIsInput()) {
							str = str + "1";
							continue;
						}
						if (itse.getIsLabel()) {
							str = str + itse.getText();
						}
					}
					str = str + tsd.getStdUnitList().get(0).getAttrValue();
				}
				// 时间点度量衡暂不做判断
				log.error("cid:" + cid + "中有度量衡属性：" + itemProp.getName());
				dlhStr.append(str + ",");
				continue;
			}
			// 获取对应的PID属性值
			Long pid = itemProp.getPid();
			String pname = itemProp.getName();
			// 度量衡属性没有属性值
			// if (dlhPid.length() > 0) {
			// propSb.append(pid + ":" + tsdVal.toString() + ";");
			// continue;
			// }

			// 判断是否是可输入字段
			if (itemProp.getIsInputProp()) {
				inputPids.append(pid + ",");
				// 从数据源数据匹配
				String value = findMapByName(pname, propMap);
				//
				if (value!=null) {
					if (pid==20000) {
						value=value+";"+"型号;C1231";
					}
					inputStr.append(value.substring(0, value.length() > 30 ? 30
							: value.length())
							+ ",");
					
					continue;
				}else {
					inputStr.append("棋他,");
					continue;
				}
				
			} else {
				// 材质成分
				if (pid == 149422948L) {
					inputPids.append(pid + ",");
					inputStr.append("其他100%" + ",");
					continue;
				}
				// 吊牌价
				if (pid == 6103476L) {
					inputPids.append(pid + ",");
					inputStr.append("others" + ",");
					continue;
				}

				// if (CommonUtil.isNullOrSizeZero(itemProp.getPropValues())) {
				// inputPids.append(pid + ",");
				// }
			}
			// 具有值的集合的属性
			// 获取某个属性的值
			List<PropValue> propValues = itemProp.getPropValues();
			if (CommonUtil.isNullOrSizeZero(propValues))
				continue;
			// 遍历值列表
			Long vid = findMapByName(itemProp, propMap);

			propSb.append(pid + ":" + vid + ";");
			// 获取二级子属性
			String childPidAndVid = ApiUtil
					.getSonItemProp(cid, pid + ":" + vid);
			if (childPidAndVid != null) {
				if (childPidAndVid.contains("-")) {
					inputPids.append(childPidAndVid.substring(0,
							childPidAndVid.indexOf(":"))
							+ ",");
					inputStr.append("others");
				} else {
					propSb.append(childPidAndVid + ";");
				}

			}

		}
		// 判断是否有值
		if (propSb.length() > 0) {
			pd.setProps(propSb.toString()
					.substring(0, propSb.toString().length() - 1)
					.replace(";;", ";"));
		}
		// if (inputPids.length() > 0) {
		// String inputPidWithoutDlh = inputPids.toString().substring(0,
		// inputPids.toString().length() - 1);
		pd.setInputPids(dlhPid.toString() + inputPids);
		pd.setInputStr(dlhStr + inputStr.toString());
		// }
		return pd;
	}

	/**
	 * 从给定键值对中匹配最相关数据
	 * 
	 * @param itemProp
	 * @param propMap
	 * @return
	 */
	private Long findMapByName(ItemProp itemProp, Map<String, String> propMap) {
		List<PropValue> propValues = itemProp.getPropValues();

		Levenshtein ls = new Levenshtein();
		// 匹配度
		float similarityRatio = 0f;
		// 匹配度最高的VID
		Long vid = null;
		long pid = itemProp.getPid();
		// 是否有其他选项
		boolean isHaveOther = false;
		long otherVid = 0l;

		String pname = itemProp.getName();
		// 属性源有相关属性
		if (propMap.containsKey(pname)) {
			String aliValStr = propMap.get(pname);
			for (int i = 0; i < propValues.size(); i++) {
				PropValue propValue = propValues.get(i);
				String valueName = propValue.getName();
				// 判断相似度
				float ratio = ls.getSimilarityRatio(aliValStr, valueName);
				if (ratio > similarityRatio) {
					similarityRatio = ratio;
					vid = propValue.getVid();
					if (ratio >= 1) {
						break;
					}
				}
				if (valueName.contains("其他")) {
					isHaveOther = true;
					otherVid = propValue.getVid();
				}
			}
		} else {
			for (int i = 0; i < propValues.size(); i++) {
				PropValue propValue = propValues.get(i);
				String valueName = propValue.getName();
				if (valueName.contains("其他")) {
					isHaveOther = true;
					otherVid = propValue.getVid();
					break;
				}
			}
		}
		if (vid == null) {
			// 品牌属性
			if (pid == 20000) {
				// 是否有其他选项
				if (isHaveOther) {
					vid = otherVid;
				} else {
					vid = propValues.get(0).getVid();
				}
			} else {
				vid = propValues.get(0).getVid();
			}
		}
		return vid;
	}

	/**
	 * 从给定键值对中匹配最相关数据
	 * 
	 * @param pname
	 * @param propMap
	 * @return
	 */
	private String findMapByName(String pname, Map<String, String> propMap) {
		// 修正属性名
		pname = fixPropName(pname);
		// 精准匹配
		if (propMap.containsKey(pname)) {
			return propMap.get(pname);
		}
		return null;
	}

	// 修正属性名
	private String fixPropName(String pname) {
		return pname;
	}

	public String getCatIdByUrl() throws IOException {
		String content = null;
		url = url.replace("m.", "detail.");
		String referer = url.substring(0, url.indexOf("offer"));
		if (Play.id.equalsIgnoreCase("oyster") == false) {
			content = SquidProxyUtils.get(url, referer, null, 10000, 5, "");

		} else {
			content = NewProxyTools.proxyGet(url, referer, "", 10000, 5);
		}

		if (isBadContent(content) == true) {
			content = NewProxyTools.proxyGet(url, referer, "", 10000, 5);
			if (isBadContent(content)) {
				content = Jsoup.connect(url).userAgent(userAgent)
						.referrer(referer).timeout(TIMEOUT).get().html();
			}
		}

		Pattern pattern = Pattern.compile("'catid':'(\\d+)'");

		Matcher matcher = pattern.matcher(content);
		String catId = null;
		if (matcher.find()) {
			catId = matcher.group(1);
		}
		return catId;// itemId;
	}

	//获取宝贝所属类目
	public static long getCatIdFor1688(String url) {
		if (CopyUtil.isAlibabaUrl(url)==false) {
			return 0;
		}
		long numiId=Long.parseLong(CopyUtil.parseItemId(url));
		AlibabaProductProductInfo result=getCatIdFor1688(numiId);
		if (result==null) {
			return -1;
		}
		return result.getCategoryID();
	}
	
	//获取宝贝所属类目
		public static long getCatIdByNumiId(long numiId) {
			AlibabaProductProductInfo result=getCatIdFor1688(numiId);
			return result.getCategoryID();
		}
	
	/**
	 * 调用接口获取宝贝信息
	 * @param numiId
	 * @return
	 */
	public static AlibabaProductProductInfo getCatIdFor1688(long numiId) {
		APiConfig1688 config = APiConfig1688.getValidApp();
		
		if (config==null) {
			return null;
		}

		ApiExecutor executor = new ApiExecutor(config.getAppkey(),
				config.getAppSecret());
	
		AlibabaAgentProductGetParam param = new AlibabaAgentProductGetParam();
		param.setWebSite("1688");
		param.setProductID(numiId);
		AlibabaAgentProductGetResult result=null;
		log.info(config.getAppkey()+"---------"+config.getAppSecret());
		try {
			result = executor.execute(param, config.getAccessToken());
			config.setUseCount(config.getUseCount()+1);
		} catch (Exception e) {
			ApiUtilFor1688.doRefresh();
			config.setUseCount(config.getUseCount()+1);
			result = executor.execute(param, config.getAccessToken());
		}
		config.rawUpdate();
		
		return result.getProductInfo();
	}

	// 对销售属性自定义属性值中的违规词语进行处理
	private String fixSalePropCustomPvalue(String name) {
		name = fixCustomPvalue(name);
		/** 双字节字符正则表达式 长度不能大于30字节 */
		final Pattern p = Pattern.compile("[^\\x00-\\xff]");
		char ch;
		int count = 0;
		for (int i = 0; i < name.length(); i++) {
			ch = name.charAt(i);
			Matcher m = p.matcher(String.valueOf(ch));
			if (m.find()) {
				count += 2;
			} else {
				count += 1;
			}
			if (count > 30) {
				return name.substring(0, i);
			}
		}

		return name;
	}

	// 对普通属性自定义属性值中的违规词语进行处理
	private String fixCustomPvalue(String name) {
		// 您自定义的属性值 存在违禁词 雾霾，请去掉后重新再提交
		if (name.equals("雾霾蓝"))
			name = "浅艾蓝";
		if (name.contains("毒药"))
			name = name.replace("毒药", "**");
		// 属性值里不能存在英文的冒号|分号|逗号 因为连接属性值的就是用的英文的冒号|分号|逗号
		if (name.contains(":"))
			name = name.replace(":", "：");
		if (name.contains(";"))
			name = name.replace(";", "；");
		if (name.contains(","))
			name = name.replace(",", "，");

		return name;
	}
}
