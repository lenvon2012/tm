package actions.pdd;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import models.itemCopy.PddGallery;
import models.itemCopy.dto.Gallery;
import models.itemCopy.dto.Groups;
import models.itemCopy.dto.PddItemDto;
import models.itemCopy.dto.PddLoginDto;
import models.itemCopy.dto.SkuDto;
import models.itemCopy.dto.Skus;
import models.itemCopy.dto.Spec;
import models.words.ALResult;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import carrier.FileCarryUtils;
import controllers.Application;
import result.TMResult;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import utils.CommonUtil;
import utils.CopyUtil;
import utils.intervalTran;
import utils.oyster.DateUtil;
import utils.oyster.Pic;
import actions.dama.DamaAction;
import actions.dama.DamaAction.ImageResult;

@SuppressWarnings("deprecation")
public class CopyToPddAction {

	private static final Logger log = LoggerFactory
			.getLogger(CopyToPddAction.class);

	// 验证码
	public static final String CaptchaCode_URL = "http://mms.pinduoduo.com/captchaCode/getCaptchaCode";
	// 登录验证
	public static final String Auth_URL = "http://mms.pinduoduo.com/auth";
	// 运费模板
	public static final String SHIP_TEMPLATE = "http://mms.pinduoduo.com/express_base/cost_template/get_list";
	// referer
	public static final String Referer_URL = "http://mms.pinduoduo.com/Pdd.html";
	// 获取规格信息
	public static final String GET_SPEC_INFO = "http://mms.pinduoduo.com/glide/v2/mms/query/spec/by/name";
	// 获取签名
	public static final String GET_UPLOAD_SIGN = "http://mms.pinduoduo.com/earth/api/upload/getSignature";
	// 创建新商品
	public static final String CREATE_NEW = "http://mms.pinduoduo.com/glide/v2/mms/edit/commit/create_new";
	// 更新新商品
	public static final String UPDATE = "http://mms.pinduoduo.com/glide/v2/mms/edit/commit/update";
	// 提交商品
	public static final String COMMIT_URL = "http://mms.pinduoduo.com/glide/v2/mms/edit/commit/submit";
	// 上传图片
	public static final String STORE_IMAGE = "http://mms.pinduoduo.com/Pdd.html";
	// UA
	public static final String UA = "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)";
	// JSON contentType
	public static final String REQ_TYPE_JSON = "application/json; charset=UTF-8";

	// contentType
	public static final String REQ_TYPE_FROM = "application/x-www-form-urlencoded";

	public static TMResult login(String username, String password,
			String verificationCode) throws Exception {

		TMResult rtnResult = new TMResult();
		String token = StringUtils.EMPTY, imageBase64 = StringUtils.EMPTY, code = StringUtils.EMPTY;
		// 获取登陆图片验证码
		String content = sendGet(CaptchaCode_URL, REQ_TYPE_JSON, Referer_URL,
				UA, "");// EntityUtils.toString(entity);
		JSONObject object = new JSONObject(content);
		Boolean isOk = object.getBoolean("result");
		if (isOk) {
			token = object.getJSONObject("data").getString("token");
			imageBase64 = object.getJSONObject("data").getString("image");
		}
		// 在线打码
		DamaAction dama = new DamaAction();
		// String pathString = GenerateImage(imageBase64);
		TMResult result = dama.updateImage("3040", GenerateImage(imageBase64));
		if (result.isOk()) {
			ImageResult img = (ImageResult) result.getRes();
			code = img.getResult();
		}

		rtnResult = auth(username, password, token, code, verificationCode);

		return rtnResult;

	}

	/**
	 * 获取运费模板信息
	 * 
	 * @param passId
	 * @param mallId
	 */
	public static String getShipTemplate(String passId, String mallId) {
		String content = StringUtils.EMPTY;
		try {
			JSONObject param = new JSONObject();
			param.put("mallId", mallId);
			param.put("pageNo", 1);
			param.put("pageSize", 1000);
			content = sendPost(SHIP_TEMPLATE, REQ_TYPE_JSON, Referer_URL, UA,
					"", param);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return content;
	}

	/**
	 * 创建新商品
	 * 
	 * @param passId
	 * @param mallId
	 */
	public static String createNewGood(String passId) {
		String content = StringUtils.EMPTY;
		content = sendPost(CREATE_NEW, REQ_TYPE_JSON, Referer_URL, UA,
				"PASS_ID=" + passId, new JSONObject());
		return content;
	}

	/**
	 * 保存草稿
	 * 
	 * @param passId
	 * @param mallId
	 */
	public static String updateGood(String passId, PddItemDto itemDto) {
		String content = StringUtils.EMPTY;
		content = sendPost(UPDATE, REQ_TYPE_JSON, Referer_URL, UA, "PASS_ID="
				+ passId, new JSONObject(itemDto));
		return content;
	}

	public static String getMainCat(String mallId, String passId) {
		String content = StringUtils.EMPTY;
		String url = "http://mms.pinduoduo.com/earth/api/mallInfo/catList";
		content = sendGet(url, REQ_TYPE_JSON, Referer_URL, UA, "PASS_ID="
				+ passId);
		return content;
	}

	public static String getSonCat(String mallId, String passId, int level,
			long parentId) {
		String content = StringUtils.EMPTY;
		String url = "http://mms.pinduoduo.com/malls/" + mallId
				+ "/commit/categories?level=" + level + "&parentId=" + parentId;
		content = sendGet(url, REQ_TYPE_JSON, Referer_URL, UA, "PASS_ID="
				+ passId);
		return content;
	}

	/**
	 * 获取规格信息
	 * 
	 * @param name
	 * @return
	 * @throws JSONException
	 */
	public static String getSpecId(String parentId, String name, String passId)
			throws JSONException {
		String content = StringUtils.EMPTY;
		JSONObject param = new JSONObject();
		param.put("parent_id", parentId);
		param.put("name", name);
		content = sendPost(GET_SPEC_INFO, REQ_TYPE_JSON, Referer_URL, UA,
				"PASS_ID=" + passId, param);
		return content;
	}

	public static String commitGood(String passId, String commitId)
			throws JSONException {
		String content = StringUtils.EMPTY;
		JSONObject param = new JSONObject();
		param.put("goods_commit_id", commitId);
		content = sendPost(COMMIT_URL, REQ_TYPE_JSON, Referer_URL, UA,
				"PASS_ID=" + passId, param);
		return content;

	}

	// 登录接口调用
	@SuppressWarnings("resource")
	public static TMResult auth(String username, String password, String token,
			String authCode, String verificationCode) {
		String passId = StringUtils.EMPTY;
		String content = StringUtils.EMPTY;
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(Auth_URL);
		httpPost.addHeader("Referer", Referer_URL);
		httpPost.addHeader("User-Agent", UA);
		httpPost.addHeader("Content-Type", REQ_TYPE_JSON);
		TMResult rtnResult = new TMResult();
		try {
			JSONObject param = new JSONObject();
			param.put("authCode", authCode);
			param.put("username", username);
			param.put("token", token);
			param.put("password", password);
			param.put("verificationCode", verificationCode);
			StringEntity se = new StringEntity(param.toString());
			httpPost.setEntity(se);
			HttpResponse rsp = httpclient.execute(httpPost);
			HttpEntity entity = rsp.getEntity();
			content = EntityUtils.toString(entity);
			EntityUtils.consume(entity);
			JSONObject backResult = new JSONObject(content);
			if (content.indexOf("mallId") < 0) {
				rtnResult.isOk = false;
				// 取得错误码与错误提示
				rtnResult.msg = backResult.getString("error_msg");
				rtnResult.code = backResult.getString("error_code");
				return rtnResult;
			}
			String mallId = backResult.getJSONObject("userInfo").getString(
					"mallId");
			// 多个setcookie情况下取得passid
			Header[] headers = rsp.getHeaders("Set-Cookie");
			for (Header header : headers) {
				String value = header.getValue();
				if (value.contains("PASS_ID")) {
					passId = value.substring("PASS_ID=".length(),
							value.indexOf(";"));
					break;
				}
			}

			rtnResult.isOk = true;
			rtnResult
					.setRes(new PddLoginDto(username, password, passId, mallId));
			return rtnResult;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;

	}

	@SuppressWarnings({ "resource" })
	public static String sendPost(String url, String contentType,
			String referer, String ua, String cookie, JSONObject param) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		httpPost.addHeader("Referer", referer);
		httpPost.addHeader("User-Agent", ua);
		httpPost.addHeader("Content-Type", contentType);
		httpPost.addHeader("Cookie", cookie);
		String content = StringUtils.EMPTY;
		try {
			if (param != null) {
				StringEntity se = new StringEntity(param.toString(), "utf-8");
				httpPost.setEntity(se);
			}

			HttpResponse rsp = httpclient.execute(httpPost);
			HttpEntity entity = rsp.getEntity();
			content = EntityUtils.toString(entity);
			log.info("cookie:" + cookie);
			log.info("param:" + param);
			log.info("content:" + content);
			EntityUtils.consume(entity);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return content;
	}

	@SuppressWarnings({ "resource" })
	public static String sendGet(String url, String contentType,
			String referer, String ua, String cookie) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		httpGet.addHeader("Referer", referer);
		httpGet.addHeader("User-Agent", ua);
		httpGet.addHeader("Content-Type", contentType);
		httpGet.addHeader("Cookie", cookie);
		String content = StringUtils.EMPTY;
		try {
			HttpResponse rsp = httpclient.execute(httpGet);
			HttpEntity entity = rsp.getEntity();
			content = EntityUtils.toString(entity);
			EntityUtils.consume(entity);
			log.info("url:" + url);
			log.info("cookie:" + cookie);
			log.info("content:" + content);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}

	public static byte[] GenerateImage(String imgStr) { // 对字节数组字符串进行Base64解码并生成图片
		if (imgStr == null) {
			// return StringUtils.EMPTY;
		}
		if (imgStr.indexOf("base64,") <= 0) {
			// return StringUtils.EMPTY;
		}
		imgStr = imgStr.substring(imgStr.indexOf("base64,")
				+ "base64,".length());
		BASE64Decoder decoder = new BASE64Decoder();
		try {
			// Base64解码
			byte[] b = decoder.decodeBuffer(imgStr);
			for (int i = 0; i < b.length; ++i) {
				if (b[i] < 0) {// 调整异常数据
					b[i] += 256;
				}
			}
			return b;
		} catch (Exception e) {
			log.error(e.getMessage());
			// return StringUtils.EMPTY;
		}
		return null;
	}

	/**
	 * @Title: GetImageStrFromUrl
	 * @Description: TODO(将一张网络图片转化成Base64字符串)
	 * @param imgURL
	 *            网络资源位置
	 * @return Base64字符串
	 */
	public static String GetImageStrFromUrl(String imgURL) {
		byte[] data = null;
		try {
			// 创建URL
			URL url = new URL(imgURL);
			// 创建链接
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5 * 1000);
			data = readInputStream(conn.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 对字节数组Base64编码
		BASE64Encoder encoder = new BASE64Encoder();
		// 返回Base64编码过的字节数组字符串
		return encoder.encode(data);
	}

	/**
	 * @Title: GetImageStrFromPath
	 * @Description: TODO(将一张本地图片转化成Base64字符串)
	 * @param imgPath
	 * @return
	 */
	public static String GetImageStrFromPath(String imgPath) {
		InputStream in = null;
		byte[] data = null;
		// 读取图片字节数组
		try {
			in = new FileInputStream(imgPath);
			data = new byte[in.available()];
			in.read(data);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 对字节数组Base64编码
		BASE64Encoder encoder = new BASE64Encoder();
		// 返回Base64编码过的字节数组字符串
		return encoder.encode(data);
	}

	/**
	 * 从输入流中获取字节数组
	 * 
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	public static byte[] readInputStream(InputStream inputStream)
			throws IOException {
		byte[] buffer = new byte[1024];
		int len = 0;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		while ((len = inputStream.read(buffer)) != -1) {
			bos.write(buffer, 0, len);
		}
		bos.close();
		return bos.toByteArray();
	}

	/**
	 * 拼多多上传图片
	 * 
	 * @param picUrl
	 *            图片链接
	 * @param height
	 *            高度
	 * @param width
	 *            宽度
	 * @param passId
	 * @return
	 */
	public static Gallery uploadUrlToPdd(String picUrl, int height, int width,
			String passId, int type) {

		Gallery gallery = null;
		String filePath = StringUtils.EMPTY;

		String md5 = getFileMD5(picUrl);
		try {
			// 判断数据库是否有该图片
			PddGallery isExsit = PddGallery.findByMd5(md5, width, height);
			if (isExsit != null) {
				gallery = new Gallery();
				gallery.setUrl(isExsit.getUrl());
				gallery.setWidth(isExsit.getWidth());
				gallery.setHeight(isExsit.getHeight());
			} else {
				// 获取签名
				String content = sendPost(GET_UPLOAD_SIGN, REQ_TYPE_JSON,
						Referer_URL, UA, "PASS_ID=" + passId, null);
				JSONObject signJo = new JSONObject(content);
				String sign = signJo.getJSONObject("result").getString(
						"signature");
				// 上传图片
				String storeUrl = signJo.getJSONObject("result").getString(
						"url");
				JSONObject param = new JSONObject();
				param.put("upload_sign", sign);
				String image = StringUtils.EMPTY;
				if (height == 0 || width == 0) {
					// 不裁剪 需判断图片是否符合要求
					// 未完成：详情图尺寸超标处理
					image = GetImageStrFromUrl(picUrl).replace("\r", "")
							.replace("\n", "");
				} else {
					filePath = FileCarryUtils.zoomImage(picUrl, width, height);
					// .downOnlineDealSize(picUrl, width,
					// height);
					// 判断是否是主图裁剪
					if (type== ImageTypePdd.MAIN_IMG) {
						Pic tt = new Pic();
						BufferedImage base = tt
								.loadImageLocal(Play.applicationPath
										+ File.separator + "public"
										+ File.separator + "images"
										+ File.separator + "base.png");

						BufferedImage img = tt.loadImageLocal(filePath);
						// 往图片上写文件
						BufferedImage newImg = tt.modifyImagetogeter(img, base);
						File outputfile = new File(filePath);
						ImageIO.write(newImg, "jpg", outputfile);
					}

					image = CopyToPddAction.GetImageStrFromPath(filePath)
							.replace("\r", "").replace("\n", "");
				}
				// 取图片格式
				String ext = picUrl.substring(picUrl.lastIndexOf(".") + 1);
				if (ext.equalsIgnoreCase("ss2")) {
					ext = "jpg";
				}
				param.put("image", "data:image/" + ext + ";base64," + image);
				String result = sendPost(storeUrl, REQ_TYPE_JSON, Referer_URL,
						UA, "PASS_ID=" + passId, param);
				gallery = new Gallery();
				gallery.setUrl(new JSONObject(result).getString("url"));
				gallery.setWidth(new JSONObject(result).getInt("width"));
				gallery.setHeight(new JSONObject(result).getInt("height"));

				PddGallery saveGallery = new PddGallery(gallery, md5);
				// 保存到数据库
				if (saveGallery.rawInsert() == false) {
					log.error("==============》》》》》》》》》》图片保存失败：url:"
							+ gallery.getUrl());
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return gallery;

	}

	// 执行复制操作
	public static TMResult doCarrier(PddItemDto item, String url,
			String oldTitle, String newTitle, Integer priceWay,
			Double priceVal, String passId, Integer isPublish) {
		TMResult tmResult = new TMResult();
		String itemId = StringUtils.EMPTY;
		try {
			itemId = CopyUtil.parseTbAndTmItemId(url);
		} catch (Exception e) {
			tmResult.setOk(false);
			tmResult.setMsg(e.getMessage());
			return tmResult;
		}

		String dataUrl = "http://hws.m.taobao.com/cache/wdetail/5.0/?id="
				+ itemId;
		// + CopyUtil.parseTbAndTmItemId(url);
		// 标题
		try {
			String dataString = sendGet(dataUrl, REQ_TYPE_JSON, "", UA, "");
			if (dataString.contains("宝贝不存在")) {
				tmResult.setOk(false);
				tmResult.setMsg("要复制的源宝贝不存在，请检查网址是否正确！");
				return tmResult;
			}
			JSONObject data = new JSONObject(dataString);
			String title = data.getJSONObject("data")
					.getJSONObject("itemInfoModel").getString("title");
			// 是否替换标题关键字
			if (!CommonUtil.isNullOrEmpty(oldTitle) && newTitle != null) {
				title = title.replace(oldTitle, newTitle);
			}
			// 过滤淘宝不被允许的关键字（批发、代理、招商、回收、置换、求购）
			title = title.replaceAll("求购|批发|置换|回收|代理|招商", "");
			item.setGoods_name(title.length() > 60 ? title.substring(60)
					: title);
			// 设置市场价
			String price = getPrice(data);
			// 是否有价格改变的值
			if (!CommonUtil.isNullOrEmpty(priceVal)) {
				// 价格变更方式
				if (priceWay == 1) {
					price = String.valueOf(Double.valueOf(price)
							+ Double.valueOf(priceVal));
				} else if (priceWay == 2) {
					price = String
							.valueOf(priceVal * Double.parseDouble(price));
				}
			}
			item.setMarket_price((int) (Double.parseDouble(price) * 100 * 3));

			JSONArray imgUrls = data.getJSONObject("data")
					.getJSONObject("itemInfoModel").getJSONArray("picsPath");

			// 主图
			String image_url = uploadUrlToPdd(imgUrls.get(0).toString(), 352,
					352, passId, ImageTypePdd.MAIN_IMG).getUrl();
			// uploadUrlToPdd(, 352, 352,
			// passId).getUrl();
			item.setImage_url(image_url);

			// 轮播图 gallery type=1
			List<Gallery> galleries = new ArrayList();
			for (int i = 0; i < imgUrls.length(); i++) {
				String imgUrl = imgUrls.get(i).toString();
				Gallery gallery = uploadUrlToPdd(imgUrl, 0, 0, passId, ImageTypePdd.MAIN_IMG);
				gallery.setType("1");
				galleries.add(gallery);
				if (i == 0) {
					Gallery thumb_url = uploadUrlToPdd(imgUrl, 200, 200,
							passId, ImageTypePdd.LB_IMG);
					// 主轮播图上传时生成缩略图和高清缩略图
					item.setThumb_url(thumb_url.getUrl());
					Gallery hd_thumb_url = uploadUrlToPdd(imgUrl, 400, 400,
							passId, ImageTypePdd.LB_IMG);
					item.setHd_thumb_url(hd_thumb_url.getUrl());
				}
			}

			// 移动端详情图片
			String descUrl = "http://hws.m.taobao.com/cache/mtop.wdetail.getItemDescx/4.1/?data=%7Bitem_num_id%3A"
					+ itemId + "%7D";

			// PC端详情图片
			String descUrlPc = "http://hws.m.taobao.com/cache/mtop.wdetail.getItemFullDesc/4.1/?data=%7B%22item_num_id%22%3A%"
					+ itemId + "%7D";
			String descUrlContent = sendGet(descUrl, REQ_TYPE_JSON, "", UA, "");
			JSONObject descJO = new JSONObject(descUrlContent);
			JSONArray descImgArray = descJO.getJSONObject("data").getJSONArray(
					"images");
			// 是否有文字描述
			String pages = descJO.getJSONObject("data").getString("pages");
			boolean isHasTxt = pages.contains("<txt>");
			String txt = StringUtils.EMPTY;
			// 取详情描述中的文字
			if (isHasTxt) {
				txt = pages.substring(0, pages.indexOf("<img>"))
						.replace("<txt>", "").replace("</txt>", "");
			}
			
			//商家编码-保存来源宝贝ID
			item.setOut_goods_sn("tb-"+itemId);

			// 商品描述,分享描述
			JSONArray descArray = data.getJSONObject("data").getJSONArray(
					"props");
			StringBuffer desc = new StringBuffer();
			for (int i = 0; i < descArray.length(); i++) {
				desc.append(descArray.getJSONObject(i).getString("name"));
				desc.append(":" + descArray.getJSONObject(i).getString("value"));
			}
			item.setGoods_desc(desc.toString() + txt);
			item.setShare_desc(desc.toString());

			// 详情图 gallery type=2
			int descPicSize = descImgArray.length() > 20 ? 20 : descImgArray
					.length();
			for (int i = 0; i < descPicSize; i++) {
				String imgUrl = descImgArray.get(i).toString();
				Gallery gallery = uploadUrlToPdd(imgUrl, 0, 0, passId, ImageTypePdd.DESC_IMG);
				gallery.setType("2");
				galleries.add(gallery);
			}
			item.setGallery(galleries);
			// SKU
			List<Skus> skus = getSku(data, passId, price, item.getGallery()
					.get(0).getUrl());

			item.setSkus(skus);
			// 食品安全 保质期-生产日期
			item.setShelf_life(365);
			item.setStart_production_date((DateUtil.dateToUnixTimestamp() - DateUtil.DAY_MILLIS)
					/ 1000 + "");
			item.setEnd_production_date((DateUtil.dateToUnixTimestamp()) / 1000
					+ "");
			// 团信息
			Groups groups = new Groups();
			groups.setBuy_limit(999999);
			groups.setOrder_limit(999999);
			groups.setCustomer_num(2);
			item.setGroups(groups);
			String createIdContent = createNewGood(passId);
			JSONObject createJo = new JSONObject(createIdContent);
			if (createJo.getBoolean("success") == false) {
				tmResult = new TMResult(false, "创建商品失败！", null);
				return tmResult;
			}

			String createId = createJo.getJSONObject("result").getString(
					"goods_commit_id");
			item.setGoods_commit_id(createId);
			String updateResult = updateGood(passId, item);
			JSONObject updateJo = new JSONObject(updateResult);
			if (updateJo.getBoolean("success") == false) {
				tmResult = new TMResult(false, "保存草稿失败！", null);
				return tmResult;
			}
			if (isPublish == 1) {
				// 提交校验
				String checkUrl = "http://mms.pinduoduo.com/glide/v2/mms/query/goods/is_commit_available/"
						+ createId;
				String checkResult = sendGet(checkUrl, REQ_TYPE_JSON,
						Referer_URL, UA, "PASS_ID=" + passId);
				JSONObject checkJo = new JSONObject(checkResult);
				if (checkJo.getBoolean("success") == false) {
					tmResult = new TMResult(false, "提交商品失败，请联系客服处理。", null);
					return tmResult;
				}
				// 提交宝贝
				boolean isCommit = false;
				String commitResult = commitGood(passId, createId);

				JSONObject commitJo = new JSONObject(commitResult);

				isCommit = commitJo.getBoolean("success");

				if (isCommit == false) {
					// for (int i = 0; i < 5; i++) {
					// commitResult=commitGood(passId, createId);
					// commitJo=new JSONObject(commitResult);
					// isCommit=commitJo.getBoolean("success");
					// if (isCommit) {
					// break;
					// }
					// }
					tmResult = new TMResult(false,
							commitJo.getString("error_msg"), null);
					return tmResult;
				}
			}

			String itemUrl = "http://mms.pinduoduo.com/Pdd.html#/goods/goods_edit/index?id="
					+ createId;
			tmResult.setMsg("拷贝成功，请在仓库中查看！新宝贝地址：<br>" + itemUrl
					+ "<br/><a style='color:blue' href='" + itemUrl
					+ "' target='_blank'>编辑宝贝</a>");
			return tmResult;

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 生成SKU信息
	 * 
	 * @param data
	 * @param img
	 *            单规格的时候用主图作为sku预览图
	 * @return
	 */
	private static List<Skus> getSku(JSONObject data, String passId,
			String price, String img) {
		List<Skus> skus = new ArrayList();
		try {
			// 判断销售属性组成，无销售属性，单种，两种
			if (data.getJSONObject("data").getJSONObject("skuModel")
					.has("skuProps")) {
				JSONArray skuArray = data.getJSONObject("data")
						.getJSONObject("skuModel").getJSONArray("skuProps");
				// 单种规格
				if (skuArray.length() == 1) {
					JSONObject skujo = skuArray.getJSONObject(0);
					// 取名字
					String propName = skujo.getString("propName");
					// 取值集
					JSONArray values = skujo.getJSONArray("values");
					// 生成Sku
					propName = fixPropName(propName);// 修正属性 如果不存在该规格则使用款式
					if (propName == null) {
						propName = "款式";
					}
					String specContent = getSpecId("0", propName, passId);
					String parent_id = new JSONObject(specContent)
							.getString("result");
					for (int i = 0; i < values.length(); i++) {
						//
						Skus sku = new Skus();
						// 库存增减
						sku.setQuantity_delta(100);
						// 团购价
						sku.setMulti_price((int) (Double.parseDouble(price) * 100));
						// 单卖价
						sku.setPrice((int) (Double.parseDouble(price) * 100 * 2));
						// sku预览图 拼多多所有SKU必须有缩略图

						if (values.getJSONObject(i).has("imgUrl")) {
							String imgUrl = values.getJSONObject(i).getString(
									"imgUrl");
							Gallery gallery = uploadUrlToPdd(imgUrl, 0, 0,
									passId,ImageTypePdd.SKU_VIEW_IMG);
							sku.setThumb_url(gallery.getUrl());
							String spec_name = values.getJSONObject(i)
									.getString("name");
							// 创建子规格
							String sonSpecContent = getSpecId(parent_id,
									spec_name, passId);
							String sonSpecId = new JSONObject(sonSpecContent)
									.getString("result");
							Spec spec = new Spec();
							spec.setParent_id(Integer.parseInt(parent_id));
							spec.setParent_name(propName);
							spec.setSpec_id(Integer.parseInt(sonSpecId));
							spec.setSpec_name(spec_name);
							List<Spec> specs = new ArrayList();
							specs.add(spec);
							sku.setSpec(specs);

							skus.add(sku);
						}

					}
				} else if (skuArray.length() == 2) {
					JSONObject firstProp = skuArray.getJSONObject(0);

					JSONObject secondProp = skuArray.getJSONObject(1);

					// 取名字
					String propName = firstProp.getString("propName");
					// 取值集
					JSONArray values = firstProp.getJSONArray("values");

					// 取名字
					String propNameSecond = secondProp.getString("propName");
					// 取值集
					JSONArray valuesSecond = secondProp.getJSONArray("values");
					// 生成Sku
					propName = fixPropName(propName);// 修正属性
					if (propName == null) {
						propName = "款式";
					}

					propNameSecond = fixPropName(propNameSecond);// 修正属性

					if (propNameSecond == null) {
						propNameSecond = "款式";
					}
					String specContent = getSpecId("0", propName, passId);
					String specContentSecond = getSpecId("0", propNameSecond,
							passId);
					String parent_id = new JSONObject(specContent)
							.getString("result");
					String parent_idSecond = new JSONObject(specContentSecond)
							.getString("result");
					for (int i = 0; i < values.length(); i++) {

						for (int j = 0; j < valuesSecond.length(); j++) {
							Skus sku = new Skus();
							// 库存增减
							sku.setQuantity_delta(100);
							// 团购价
							sku.setMulti_price((int) (Double.parseDouble(price) * 100));
							// 单卖价
							sku.setPrice((int) (Double.parseDouble(price) * 100 * 2));
							// sku预览图

							if (values.getJSONObject(i).has("imgUrl")) {
								String imgUrl = values.getJSONObject(i)
										.getString("imgUrl");
								Gallery gallery = uploadUrlToPdd(imgUrl, 0, 0,
										passId, ImageTypePdd.SKU_VIEW_IMG);
								sku.setThumb_url(gallery.getUrl());
							} else if (valuesSecond.getJSONObject(j).has(
									"imgUrl")) {
								String imgUrl = valuesSecond.getJSONObject(j)
										.getString("imgUrl");
								Gallery gallery = uploadUrlToPdd(imgUrl, 0, 0,
										passId, ImageTypePdd.SKU_VIEW_IMG);
								sku.setThumb_url(gallery.getUrl());
							}
							if (StringUtils.isEmpty(sku.getThumb_url())) {
								continue;
							}

							String spec_name = values.getJSONObject(i)
									.getString("name");
							// 创建子规格
							String sonSpecContent = getSpecId(parent_id,
									spec_name, passId);
							String sonSpecId = new JSONObject(sonSpecContent)
									.getString("result");

							String spec_nameSecond = valuesSecond
									.getJSONObject(j).getString("name");
							// 创建子规格
							String sonSpecContentSecond = getSpecId(
									parent_idSecond, spec_nameSecond, passId);
							String sonSpecIdSecond = new JSONObject(
									sonSpecContentSecond).getString("result");
							Spec spec = new Spec();
							spec.setParent_id(Integer.parseInt(parent_id));
							spec.setParent_name(propName);
							spec.setSpec_id(Integer.parseInt(sonSpecId));
							spec.setSpec_name(spec_name);
							Spec specSecond = new Spec();
							specSecond.setParent_id(Integer
									.parseInt(parent_idSecond));
							specSecond.setParent_name(propNameSecond);
							specSecond.setSpec_id(Integer
									.parseInt(sonSpecIdSecond));
							specSecond.setSpec_name(spec_nameSecond);
							List<Spec> specs = new ArrayList();
							specs.add(spec);
							specs.add(specSecond);
							sku.setSpec(specs);

							skus.add(sku);
						}

					}
				}
			} else {
				// 无SKU规格信息 根据主图创建单SKU
				Skus sku = new Skus();
				// 库存增减
				sku.setQuantity_delta(100);
				// 团购价
				sku.setMulti_price((int) (Double.parseDouble(price) * 100));
				// 单卖价
				sku.setPrice((int) (Double.parseDouble(price) * 100 * 2));
				// sku预览图
				sku.setThumb_url(img);
				skus.add(sku);

			}

			// 双规格

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return skus;
	}

	// 修正属性
	private static String fixPropName(String propName) {
		if (propName.contains("颜色")) {
			return "颜色";
		}
		if (propName.contains("尺码")) {
			return "尺码";
		}
		if (propName.contains("款式")) {
			return "款式";
		}
		if (propName.contains("尺寸")) {
			return "尺寸";
		}
		if (propName.contains("容量")) {
			return "容量";
		}
		if (propName.contains("套餐")) {
			return "套餐";
		}
		if (propName.contains("材质")) {
			return "材质";
		}
		if (propName.contains("版本")) {
			return "版本";
		}
		if (propName.contains("年龄")) {
			return "适用年龄";
		}
		if (propName.contains("重量")) {
			return "重量";
		}
		if (propName.contains("度数")) {
			return "度数";
		}
		if (propName.contains("地区")) {
			return "地区";
		}
		if (propName.contains("地点")) {
			return "地点";
		}
		if (propName.contains("时间")) {
			return "时间";
		}
		if (propName.contains("人群")) {
			return "适用人群";
		}
		return null;
	}

	// 取价格
	public static String getPrice(JSONObject data) throws JSONException {
		JSONObject apiStack = data.getJSONObject("data")
				.getJSONArray("apiStack").getJSONObject(0);

		String value = apiStack.getString("value");

		JSONObject apiStackValue = new JSONObject(value);

		JSONArray priceUnitArr = apiStackValue.getJSONObject("data")
				.getJSONObject("itemInfoModel").getJSONArray("priceUnits");

		if (priceUnitArr.length() == 0) {
			return null;
		}

		Pattern pattern = Pattern.compile("\"price\":\"(.+?)\"");

		String priceUnit;
		if (priceUnitArr.length() == 1) {
			priceUnit = priceUnitArr.getString(0);
		} else {
			priceUnit = priceUnitArr.getString(1);
		}
		log.warn("--------->" + priceUnit);
		Matcher matcher = pattern.matcher(priceUnit);
		if (matcher.find()) {
			String price = matcher.group(1);
			if (price.contains("-")) {
				return price.split("-")[1];
			} else {
				return price;
			}
		}

		return null;
	}

	/**
	 * 根据在线URL计算出文件的MD5
	 * 
	 * @param file
	 * @return
	 */
	public static String getFileMD5(String imgURL) {
		// 创建URL
		MessageDigest digest = null;
		try {
			URL url = new URL(imgURL);
			// 创建链接
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5 * 1000);
			byte buffer[] = new byte[1024];
			InputStream in = null;
			int len;
			in = conn.getInputStream();
			digest = MessageDigest.getInstance("MD5");
			while ((len = in.read(buffer, 0, 1024)) != -1) {
				digest.update(buffer, 0, len);
			}
			in.close();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		BigInteger bigInt = new BigInteger(1, digest.digest());

		return bigInt.toString(16);
	}

	public final static String MD5(String pwd) {
		// 用于加密的字符
		char md5String[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'A', 'B', 'C', 'D', 'E', 'F' };
		try {
			// 使用平台的默认字符集将此 String 编码为 byte序列，并将结果存储到一个新的 byte数组中
			byte[] btInput = pwd.getBytes();

			// 信息摘要是安全的单向哈希函数，它接收任意大小的数据，并输出固定长度的哈希值。
			MessageDigest mdInst = MessageDigest.getInstance("MD5");

			// MessageDigest对象通过使用 update方法处理数据， 使用指定的byte数组更新摘要
			mdInst.update(btInput);

			// 摘要更新之后，通过调用digest（）执行哈希计算，获得密文
			byte[] md = mdInst.digest();

			// 把密文转换成十六进制的字符串形式
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) { // i = 0
				byte byte0 = md[i]; // 95
				str[k++] = md5String[byte0 >>> 4 & 0xf]; // 5
				str[k++] = md5String[byte0 & 0xf]; // F
			}

			// 返回经过加密后的字符串
			return new String(str);

		} catch (Exception e) {
			return null;
		}
	}

}
