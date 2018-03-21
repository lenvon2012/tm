package actions.jms;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.jms.JMSMsgLog;
import models.jms.MsgContent;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import utils.DateUtil;
import bustbapi.ItemApi;
import bustbapi.TMTradeApi;
import bustbapi.TradeRateApi;
import bustbapi.request.JushitaJmsTopicsGetRequest;
import bustbapi.request.JushitaJmsUserAddRequest;
import bustbapi.request.JushitaJmsUserDeleteRequest;
import bustbapi.request.JushitaJmsUserGetRequest;
import bustbapi.request.PromotionmiscCommonItemActivityListGetRequest;
import bustbapi.response.JushitaJmsTopicsGetResponse;
import bustbapi.response.JushitaJmsUserAddResponse;
import bustbapi.response.JushitaJmsUserDeleteResponse;
import bustbapi.response.JushitaJmsUserGetResponse;
import bustbapi.response.PromotionmiscCommonItemActivityListGetResponse;
import bustbapi.result.CommonItemActivity;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Consumer;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.PropertyValueConst;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.SecretException;
import com.taobao.api.TaobaoClient;
import com.taobao.api.domain.TradeRate;
import com.taobao.api.request.ItemRecommendAddRequest;
import com.taobao.api.request.ItemSellerGetRequest;
import com.taobao.api.request.ItemSkusGetRequest;
import com.taobao.api.request.ItemcatsGetRequest;
import com.taobao.api.request.ItempropsGetRequest;
import com.taobao.api.request.ItempropvaluesGetRequest;
import com.taobao.api.request.LogisticsOrdersDetailGetRequest;
import com.taobao.api.request.PictureCategoryGetRequest;
import com.taobao.api.request.PictureGetRequest;
import com.taobao.api.request.SellercenterSubusersGetRequest;
import com.taobao.api.request.ShopGetRequest;
import com.taobao.api.request.ShopRemainshowcaseGetRequest;
import com.taobao.api.request.TbkAdzoneCreateRequest;
import com.taobao.api.request.TbkDgItemCouponGetRequest;
import com.taobao.api.request.TbkDgNewuserOrderGetRequest;
import com.taobao.api.request.TmallItemIncrementUpdateSchemaGetRequest;
import com.taobao.api.request.TmallItemSchemaIncrementUpdateRequest;
import com.taobao.api.request.TradeFullinfoGetRequest;
import com.taobao.api.request.TradeGetRequest;
import com.taobao.api.request.TraderateListAddRequest;
import com.taobao.api.request.TraderatesGetRequest;
import com.taobao.api.request.TradesSoldGetRequest;
import com.taobao.api.request.UserSellerGetRequest;
import com.taobao.api.response.ItemRecommendAddResponse;
import com.taobao.api.response.ItemSellerGetResponse;
import com.taobao.api.response.ItemSkusGetResponse;
import com.taobao.api.response.ItemcatsGetResponse;
import com.taobao.api.response.ItempropsGetResponse;
import com.taobao.api.response.ItempropvaluesGetResponse;
import com.taobao.api.response.LogisticsOrdersDetailGetResponse;
import com.taobao.api.response.PictureCategoryGetResponse;
import com.taobao.api.response.PictureGetResponse;
import com.taobao.api.response.SellercenterSubusersGetResponse;
import com.taobao.api.response.ShopGetResponse;
import com.taobao.api.response.ShopRemainshowcaseGetResponse;
import com.taobao.api.response.TbkAdzoneCreateResponse;
import com.taobao.api.response.TbkDgItemCouponGetResponse;
import com.taobao.api.response.TbkDgNewuserOrderGetResponse;
import com.taobao.api.response.TmallItemIncrementUpdateSchemaGetResponse;
import com.taobao.api.response.TmallItemSchemaIncrementUpdateResponse;
import com.taobao.api.response.TradeFullinfoGetResponse;
import com.taobao.api.response.TradeGetResponse;
import com.taobao.api.response.TraderateListAddResponse;
import com.taobao.api.response.TraderatesGetResponse;
import com.taobao.api.response.TradesSoldGetResponse;
import com.taobao.api.response.UserSellerGetResponse;
import com.taobao.top.schema.Util.XmlUtils;
import com.taobao.top.schema.exception.TopSchemaException;
import com.taobao.top.schema.field.Field;

import controllers.TmSecurity;
import controllers.TmSecurity.SecurityType;

public class JMSTest {
	
	private static final Logger log = LoggerFactory.getLogger(JMSTest.class);
	
	private static String URL = "http://gw.api.taobao.com/router/rest";
	
	private static String APP_KEY = "21404171";
	
	private static String APP_SECRET = "724576dc06e80ed8e38d1ad2f6de39da";
	
	private static String TZG_APP_KEY = "21255586";
	
	private static String TZG_APP_SECRET = "04eb2b1fa4687fbcdeff12a795f863d4";
	
	private static String JIAOYI_APP_KEY = "23256680";
	
	private static String JIAOYI_APP_SECRET = "06291fbd4cd886ab5723204bc399855a";
	
//	private static String USER_NICK = "贝乐星商城2号";
//	
//	private static String SESSIONKEY = "61006207a47496fbdb81b036f3ca17723ZZff28c59365962209509168";
	
	private static String USER_NICK = "liu405607850";
	
	private static String SESSIONKEY = "61020252ea187f57b2dc72603fd40714d14b58egdb83d252881213009";
	
	private static Long TID = 41347369313696290L;
	
	private static String CONSUMERID = "CID_test_lzl";

	private static String TOPIC_NAMES = "taobao_trade_TradeRated";
	
	private static String fields = "user_id,uid,nick,seller_credit,status,has_shop,type";
	
	public static void tmallItemIncrementUpdateSchemaGet() {
		TaobaoClient client = new DefaultTaobaoClient(URL, TZG_APP_KEY, TZG_APP_SECRET);
		TmallItemIncrementUpdateSchemaGetRequest req = new TmallItemIncrementUpdateSchemaGetRequest();
		req.setItemId(561400693383L);
		req.setXmlData("<?xml version=\"1.0\" encoding=\"UTF-8\"?><itemParam><field id=\"update_fields\" name=\"更新字段列表\" type=\"multiCheck\"><values><value>description</value></values></field></itemParam>");
		TmallItemIncrementUpdateSchemaGetResponse rsp = null;
		try {
			rsp = client.execute(req, "6100b2668442fe9eb21259afa1df5be8fe197a0ZZ92da7f1758965127");
		} catch (ApiException e) {
			e.printStackTrace();
		}
		System.out.println(rsp.getUpdateItemResult());
	}
	
	public static void tmallItemSchemaIncrementUpdate() {
		TaobaoClient client = new DefaultTaobaoClient(URL, TZG_APP_KEY, TZG_APP_SECRET);
		try {
			TmallItemIncrementUpdateSchemaGetRequest req1 = new TmallItemIncrementUpdateSchemaGetRequest();
			req1.setItemId(561400693383L);
			req1.setXmlData("<?xml version=\"1.0\" encoding=\"UTF-8\"?><itemParam><field id=\"update_fields\" name=\"更新字段列表\" type=\"multiCheck\"><values><value>description</value></values></field></itemParam>");
			TmallItemIncrementUpdateSchemaGetResponse rsp1 = client.execute(req1, "6100b2668442fe9eb21259afa1df5be8fe197a0ZZ92da7f1758965127");
			String updateItemResult = rsp1.getUpdateItemResult();
			if(StringUtils.isEmpty(updateItemResult)) {
				return;
			}
			
			String xml = StringUtils.EMPTY;
			Pattern p = Pattern.compile("<default-complex-values>(.*)</default-complex-values>", Pattern.DOTALL);
			Matcher matcher = p.matcher(updateItemResult);
			if (matcher.find()) {
				xml = matcher.group(1);
			}
			
			String xmlDataBefore = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
					"<itemRule>" +
						"<field id=\"update_fields\" name=\"更新字段列表\" type=\"multiCheck\">" +
							"<values>" +
								"<value>description</value>" +
							"</values>" +
						"</field>" +
						"<field id=\"description\" name=\"商品描述\" type=\"complex\">" +
							"<complex-values>";
	
			String xmlDateAfter = "</complex-values></field></itemRule>";
			
			
			TmallItemSchemaIncrementUpdateRequest req = new TmallItemSchemaIncrementUpdateRequest();
			req.setItemId(561400693383L);
			req.setXmlData(xmlDataBefore + xml + xmlDateAfter);
			TmallItemSchemaIncrementUpdateResponse rsp = client.execute(req, "6100b2668442fe9eb21259afa1df5be8fe197a0ZZ92da7f1758965127");
			System.out.println(rsp.getUpdateItemResult());
		} catch (ApiException e) {
			e.printStackTrace();
		}
		
//		String xmlDataBefore = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
//				"<itemRule>" +
//					"<field id=\"update_fields\" name=\"更新字段列表\" type=\"multiCheck\">" +
//						"<values>" +
//							"<value>description</value>" +
//						"</values>" +
//					"</field>" +
//					"<field id=\"description\" name=\"商品描述\" type=\"complex\">" +
//						"<complex-values>" +
//							"<field id=\"desc_module_user_mods\" type=\"multiComplex\">" +
//								"<complex-values>" +
//									"<field id=\"desc_module_user_mod_name\" type=\"input\">" +
//										"<value>团购模板</value>" +
//									"</field>" +
//									"<field id=\"desc_module_user_mod_order\" type=\"input\">" +
//										"<value>1</value>" +
//									"</field>" +
//									"<field id=\"desc_module_user_mod_content\" type=\"input\">" +
//										"<value>";
//
//		String xmlDateAfter = "</value></field></complex-values></field></complex-values></field></itemRule>";
//		
//		String desc = "aaa";
	}
	
	private static String getDefaultValue(Field field) throws TopSchemaException {
		Element fieldElm = field.toElement();
		Element defaultValueEle = XmlUtils.getChildElement(fieldElm, "default-values");
		String dvalue = StringUtils.EMPTY;
		if(defaultValueEle != null){
			dvalue = defaultValueEle.getText();
		}
		return dvalue;
	}
	
	public static void updateItemTitle() {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
				"<itemRule>" +
					"<field id=\"title\" name=\"商品标题\" type=\"input\">" +
						"<value>2016秋季新款5女童装8纯棉4中大童&gt;袖6春秋装&lt;儿童韩版</value>" +
					"</field>" +
					"<field id=\"update_fields\" name=\"更新字段列表\" type=\"multiCheck\">" +
						"<values>" +
							"<value>title</value>" +
						"</values>" +
						"</field>" +
				"</itemRule>";
		TaobaoClient client = new DefaultTaobaoClient(URL, TZG_APP_KEY, TZG_APP_SECRET);
		TmallItemSchemaIncrementUpdateRequest req = new TmallItemSchemaIncrementUpdateRequest();
		req.setItemId(561400693383L);
		req.setXmlData(xml);
		TmallItemSchemaIncrementUpdateResponse rsp = null;
		try {
			rsp = client.execute(req, "6100b2668442fe9eb21259afa1df5be8fe197a0ZZ92da7f1758965127");
		} catch (ApiException e) {
			e.printStackTrace();
		}
		System.out.println(rsp.getUpdateItemResult());
	}
	
	public static void pictureCategoryGet() {
		TaobaoClient client = new DefaultTaobaoClient(URL, TZG_APP_KEY, TZG_APP_SECRET);
		PictureCategoryGetRequest req = new PictureCategoryGetRequest();
		PictureCategoryGetResponse rsp = null;
		try {
			rsp = client.execute(req, "610022944f53a1b3f51b83d1337d4f011baa41ad009a28479742176");
		} catch (ApiException e) {
			e.printStackTrace();
		}
		System.out.println(rsp.getPictureCategories());
	}
	
	public static void pictureGet() {
		TaobaoClient client = new DefaultTaobaoClient(URL, TZG_APP_KEY, TZG_APP_SECRET);
		PictureGetRequest req = new PictureGetRequest();
		req.setDeleted("unfroze");
		req.setPictureCategoryId(1217616171789846796L);
		req.setTitle("TB1..HGkPihSKJjy0FeXXbJtpXa_!!1-item_pic.gif");
		PictureGetResponse rsp = null;
		try {
			rsp = client.execute(req, "610022944f53a1b3f51b83d1337d4f011baa41ad009a28479742176");
		} catch (ApiException e) {
			e.printStackTrace();
		}
		System.out.println(rsp.getBody());
	}
	
	public static void itemSellerGet() {
		TaobaoClient client = new DefaultTaobaoClient(URL, TZG_APP_KEY, TZG_APP_SECRET);
		ItemSellerGetRequest req = new ItemSellerGetRequest();
		req.setFields(ItemApi.FIELDS_ALL);
		req.setNumIid(562385533623L);
		ItemSellerGetResponse rsp = null;
		try {
			rsp = client.execute(req, "610022944f53a1b3f51b83d1337d4f011baa41ad009a28479742176");
		} catch (ApiException e) {
			e.printStackTrace();
		}
		System.out.println(rsp.getBody());
	}
	
	public static void getSku() {
		TaobaoClient client = new DefaultTaobaoClient(URL, TZG_APP_KEY, TZG_APP_SECRET);
		ItemSkusGetRequest req = new ItemSkusGetRequest();
		req.setFields("sku_id,quantity,outer_id");
		req.setNumIids("560387391215");
		ItemSkusGetResponse rsp = null;
		try {
			rsp = client.execute(req, "610022944f53a1b3f51b83d1337d4f011baa41ad009a28479742176");
		} catch (ApiException e) {
			e.printStackTrace();
		}
		System.out.println(rsp.getBody());
	}
	
	public static void getItempropvalues() {
		TaobaoClient client = new DefaultTaobaoClient(URL, TZG_APP_KEY, TZG_APP_SECRET);
		ItempropvaluesGetRequest req = new ItempropvaluesGetRequest();
		req.setFields("cid,pid,prop_name,vid,name,name_alias,status,sort_order");
		req.setCid(123216004L);
		req.setPvs("20000:29534");
		ItempropvaluesGetResponse rsp = null;
		try {
			rsp = client.execute(req, "610022944f53a1b3f51b83d1337d4f011baa41ad009a28479742176");
		} catch (ApiException e) {
			e.printStackTrace();
		}
		System.out.println(rsp.getBody());
	}
	
	public static void getItemprops() {
		TaobaoClient client = new DefaultTaobaoClient(URL, TZG_APP_KEY, TZG_APP_SECRET);
		ItempropsGetRequest req = new ItempropsGetRequest();
		req.setFields("pid,name,is_sale_prop,is_color_prop,is_allow_alias,is_enum_prop,is_input_prop,prop_values,multi,must");
		req.setCid(50011979L);
//		req.setChildPath("20000:29534");
		ItempropsGetResponse rsp = null;
		try {
			rsp = client.execute(req, "610022944f53a1b3f51b83d1337d4f011baa41ad009a28479742176");
		} catch (ApiException e) {
			e.printStackTrace();
		}
		System.out.println(rsp.getBody());
	}
	
	public static void getlogisticsOrdersDetail() {
		TaobaoClient client = new DefaultTaobaoClient(URL, JIAOYI_APP_KEY, JIAOYI_APP_SECRET);
		LogisticsOrdersDetailGetRequest req = new LogisticsOrdersDetailGetRequest();
		req.setFields("tid,order_code,out_sid,seller_nick,buyer_nick,item_title,receiver_location,status,type,company_name,created,modified,is_quick_cod_order,sub_tids,is_split");
		req.setTid(52824545437592305L);
		LogisticsOrdersDetailGetResponse rsp = null;
		try {
			rsp = client.execute(req, "50008400e36jkiwnPeD0fYciydwSf6DAR7OUtCfFSLVDQxFj1003989f73XxxJ5xZp");
		} catch (ApiException e) {
			e.printStackTrace();
		}
		System.out.println(rsp.getBody());
	}
	
	public static void getPromotionList() {
		TaobaoClient client = new DefaultTaobaoClient(URL, "12266732", "fd132f04d231934fb83417e00e8d256b");
		PromotionmiscCommonItemActivityListGetRequest req = new PromotionmiscCommonItemActivityListGetRequest();
		req.setPageNo(1L);
		req.setPageSize(50L);
		PromotionmiscCommonItemActivityListGetResponse rsp = null;
		try {
			rsp = client.execute(req, "61013246056679c5f67bcffe05dadf621914560d657ab722905457684");
		} catch (ApiException e) {
			e.printStackTrace();
		}
		List<CommonItemActivity> lists = rsp.getActivityList();
		for (CommonItemActivity list : lists) {
			System.out.println(list.getActivityId() + "~~~" + list.getName() + "~~~" + list.getDescription());
		}
	}
	
	public static void testSellerGet() {
		TaobaoClient client = new DefaultTaobaoClient(URL, APP_KEY, APP_SECRET);
		UserSellerGetRequest req = new UserSellerGetRequest();
		req.setFields(fields);
		UserSellerGetResponse response = null;
		try {
			response = client.execute(req, SESSIONKEY);
		} catch (ApiException e) {
			e.printStackTrace();
		}
		System.out.println(response.getBody());
	}
	
	public static void testSubuserGet() {
		TaobaoClient client = new DefaultTaobaoClient(URL, APP_KEY, APP_SECRET);
		SellercenterSubusersGetRequest req = new SellercenterSubusersGetRequest();
		req.setNick("beauty鞋柜1号店");
		SellercenterSubusersGetResponse rsp = null;
		try {
			rsp = client.execute(req, SESSIONKEY);
		} catch (ApiException e) {
			e.printStackTrace();
		}
		System.out.println(rsp.getBody());
	}
	
	public static void testJmsUserAdd() {
		TaobaoClient client = new DefaultTaobaoClient(URL, APP_KEY, APP_SECRET);
		JushitaJmsUserAddRequest req = new JushitaJmsUserAddRequest();
		req.setTopicNames(TOPIC_NAMES);
		JushitaJmsUserAddResponse response = null;
		try {
			response = client.execute(req, SESSIONKEY);
		} catch (ApiException e) {
			e.printStackTrace();
		}
		System.out.println(response.getBody());
	}
	
	public static void testJmsUserGet() {
		TaobaoClient client = new DefaultTaobaoClient(URL, APP_KEY, APP_SECRET);
		JushitaJmsUserGetRequest req = new JushitaJmsUserGetRequest();
		req.setUserNick(USER_NICK);
		JushitaJmsUserGetResponse response = null;
		try {
			response = client.execute(req, SESSIONKEY);
		} catch (ApiException e) {
			e.printStackTrace();
		}
		System.out.println(response.getBody());
	}
	
	public static void testJmsUserDelete() {
		TaobaoClient client = new DefaultTaobaoClient(URL, APP_KEY, APP_SECRET);
		JushitaJmsUserDeleteRequest req = new JushitaJmsUserDeleteRequest ();
		req.setUserNick(USER_NICK);
		JushitaJmsUserDeleteResponse response = null;
		try {
			response = client.execute(req, SESSIONKEY);
		} catch (ApiException e) {
			e.printStackTrace();
		}
		System.out.println(response.getBody());
	}
	
	public static void testJmsTopicsGet() {
		TaobaoClient client = new DefaultTaobaoClient(URL, APP_KEY, APP_SECRET);
		JushitaJmsTopicsGetRequest req = new JushitaJmsTopicsGetRequest();
		req.setNick(USER_NICK);
		JushitaJmsTopicsGetResponse response = null;
		try {
			response = client.execute(req, SESSIONKEY);
		} catch (ApiException e) {
			e.printStackTrace();
		}
		System.out.println(response.getBody());
	}
	
	public static void testItemCatGet() {
		TaobaoClient client = new DefaultTaobaoClient(URL, APP_KEY, APP_SECRET);
		ItemcatsGetRequest req = new ItemcatsGetRequest();
		req.setParentCid(0L);
		ItemcatsGetResponse response = null;
		try {
			response = client.execute(req, SESSIONKEY);
		} catch (ApiException e) {
			e.printStackTrace();
		}
		System.out.println(response.getBody());
	}
	
	public static void testShopGet() {
		TaobaoClient client = new DefaultTaobaoClient(URL, APP_KEY, APP_SECRET);
		ShopGetRequest req = new ShopGetRequest();
		req.setFields("nick,shop_score");
		req.setNick(USER_NICK);;
		ShopGetResponse response = null;
		try {
			response = client.execute(req, SESSIONKEY);
		} catch (ApiException e) {
			e.printStackTrace();
		}
		System.out.println(response.getBody());
	}
	
	public static void testTradeGet() {
		TaobaoClient client = new DefaultTaobaoClient(URL, APP_KEY, APP_SECRET);
		TradeGetRequest req = new TradeGetRequest();
		req.setFields(TMTradeApi.TRADE_FIELDS);
		req.setTid(131509039318471768L);
		TradeGetResponse response = null;
		try {
			response = client.execute(req, "6202a21f0a3883ca1006bca8b8e58ZZ279ZZ8b27c72eec42120935684");
		} catch (ApiException e) {
			e.printStackTrace();
		}
		System.out.println(response.getBody());
	}
	
	public static void testTradesSoldGet() {
		TaobaoClient client = new DefaultTaobaoClient(URL, APP_KEY, APP_SECRET);
		TradesSoldGetRequest req = new TradesSoldGetRequest();
		req.setFields("tid,orders,buyer_nick,seller_can_rate");
		req.setStartCreated(new Date(new Date().getTime() - DateUtil.DAY_MILLIS * 30));
		req.setEndCreated(new Date());
		req.setStatus("TRADE_FINISHED");
		req.setRateStatus("RATE_UNSELLER");
		req.setUseHasNext(true);
		req.setPageNo(1L);
		req.setPageSize(4L);
		TradesSoldGetResponse response = null;
		try {
			response = client.execute(req, "6200406ca2c5a0ZZ663ZZ7c9224c791ee813353fdbd3f193247089122");
		} catch (ApiException e) {
			e.printStackTrace();
		}
		System.out.println(response.getBody());
	}
	
	public static void testTradeRateListAdd() {
		TaobaoClient client = new DefaultTaobaoClient(URL, APP_KEY, APP_SECRET);
		TraderateListAddRequest req = new TraderateListAddRequest();
		req.setTid(128752116962955125L);
		req.setResult("good");
		req.setRole("seller");
		req.setContent("好评！");
		req.setAnony(false);
		TraderateListAddResponse response = null;
		try {
			response = client.execute(req, "61020243c273e2af4406fae471bf3d6617f3bikf15b6427666019952");
		} catch (ApiException e) {
			e.printStackTrace();
		}
		System.out.println(response.getBody());
	}
	
	public static void testTradeFullGet() {
		TaobaoClient client = new DefaultTaobaoClient(URL, JIAOYI_APP_KEY, JIAOYI_APP_SECRET);
		TradeFullinfoGetRequest req = new TradeFullinfoGetRequest();
		req.setFields("tid,promotion_details");
		req.setTid(TID);
		TradeFullinfoGetResponse response = null;
		try {
			response = client.execute(req, SESSIONKEY);
		} catch (ApiException e) {
			e.printStackTrace();
		}
		System.out.println(response.getBody());
	}
	
	public static void testTradeRateGet() {
		TaobaoClient client = new DefaultTaobaoClient(URL, APP_KEY, APP_SECRET);
		TraderatesGetRequest req = new TraderatesGetRequest();
		req.setFields(TradeRateApi.FIELDS);
		req.setRateType("get");
		req.setRole("buyer");
//		req.setResult("neutral");
//		req.setStartDate(new Date(1491807198403L));
//		req.setEndDate(new Date(1492671198403L));
		req.setTid(TID);
		TraderatesGetResponse response = null;
		try {
			response = client.execute(req, SESSIONKEY);
		} catch (ApiException e) {
			e.printStackTrace();
		}
		System.out.println(response.getBody());
		List<TradeRate> tradeRates = response.getTradeRates();
		for (TradeRate t : tradeRates) {
			System.out.println(t.getTid());
		}
		TradeRate tradeRate = response.getTradeRates().get(0);
		User user = new User();
		user.setSessionKey(SESSIONKEY);
		try {
			System.out.println(TmSecurity.decrypt(tradeRate.getNick(), SecurityType.SIMPLE, user));
		} catch (SecretException e) {
			e.printStackTrace();
		}
	}
	
	public static void testShopRemainshowcaseGet() {
		TaobaoClient client = new DefaultTaobaoClient(URL, TZG_APP_KEY, TZG_APP_SECRET);
		ShopRemainshowcaseGetRequest req = new ShopRemainshowcaseGetRequest();
		ShopRemainshowcaseGetResponse response = null;
		try {
			response = client.execute(req, SESSIONKEY);
		} catch (ApiException e) {
			e.printStackTrace();
		}
		System.out.println(response.getBody());
	}
	
	public static void testItemRecommendAdd() {
		TaobaoClient client = new DefaultTaobaoClient(URL, TZG_APP_KEY, TZG_APP_SECRET);
		ItemRecommendAddRequest req = new ItemRecommendAddRequest();
		req.setNumIid(542349556364L);
		
		ItemRecommendAddResponse response = null;
		try {
			response = client.execute(req, SESSIONKEY);
		} catch (ApiException e) {
			e.printStackTrace();
		}
		System.out.println(response.getBody());
	}
	
	public static void ConsumerTest() {
		Properties properties = new Properties();
		properties.put(PropertyKeyConst.ConsumerId, CONSUMERID);// ons控制台订阅管理中获取ConsumerID
		properties.put(PropertyKeyConst.AccessKey, APP_KEY);// 应用appkey，根据用户实际参数修改
		properties.put(PropertyKeyConst.SecretKey, APP_SECRET);// 应用密钥，根据用户实际参数修改
//		properties.put(PropertyKeyConst.OnsChannel, "CLOUD");// cloud为聚石塔标识
		properties.put(PropertyKeyConst.MessageModel, PropertyValueConst.CLUSTERING);//集群消费，默认为集群消费模式
//		properties.put(PropertyKeyConst.MessageModel, PropertyValueConst.BROADCASTING);广播消费模式
		Consumer consumer = ONSFactory.createConsumer(properties); 
		consumer.subscribe("defender_test", "*", new MessageListener() {// 消息队列名称，根据用户实际参数修改
			public Action consume(Message message, ConsumeContext context) {
				String msg_body = StringUtils.EMPTY;
				try {
					msg_body = new String(message.getBody(), "utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				log.info("~~~~~Receive: " + message + "~~~~~msg_body: " + msg_body + "~~~~~");
				
//				MsgContent msgContent = getMsgContent(msg_body);
//				
//				if(msgContent == null) {
//					log.error("解析msgContent失败！msg_body为： " + msg_body);
//					return Action.ReconsumeLater;
//				}
//				
//				TradeRateSyncAction.doTradeRateSync(msgContent);
//				message.setKey("aaaaa");
				
				return Action.CommitMessage;
			}
		});
		consumer.start();
		log.info("~~~~~Consumer Started~~~~~");
	}
	
	private static class TradeRateSync implements Callable<Boolean> {

		public Message message;

		public TradeRateSync(Message message) {
			this.message = message;
		}

		@Override
		public Boolean call() throws Exception {
			String cacheKey = "jms_count_";
			try {
				int count = Cache.get(cacheKey) == null? 0 : (Integer)Cache.get(cacheKey);
				count += 1;
				Cache.set(cacheKey, count, "24h");
				log.info("~~~~~当前jms_count_次数：" + count + "~~~~~");
			} catch(Exception e) {
				log.error("key " + cacheKey + " 没有对应的缓存");
			}
			
			String msg_id = message.getMsgID();
			String msg_body = StringUtils.EMPTY;
			try {
				msg_body = new String(message.getBody(), "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			log.info("~~~~~Receive: " + message + "~~~~~msg_body: " + msg_body + "~~~~~");
			
			MsgContent msgContent = getMsgContent(msg_body);
			
			if(msgContent == null) {
				log.error("~~~~~解析msgContent失败！msg_body为： " + msg_body + "~~~~~");
				
				String errKey = "jms_error_";
				try {
					int count = Cache.get(errKey) == null? 0 : (Integer)Cache.get(errKey);
					count += 1;
					Cache.set(cacheKey, count, "72h");
					log.info("~~~~~当前jms_error_次数：" + count + "~~~~~");
				} catch(Exception e) {
					log.error("key " + cacheKey + " 没有对应的缓存");
				}
				
				return false;
			}
			
			// 储存jms消息日志
			JMSMsgLog jmsMsgLog = new JMSMsgLog(msg_id, msgContent);
			jmsMsgLog.jdbcSave();
			
			if("buyer".equalsIgnoreCase(msgContent.getRater())) {
				TradeRateSyncAction.doTradeRateSync(msgContent);
			}
			
			return true;
		}

	}
	
	private static MsgContent getMsgContent(String msg_body) {
		MsgContent msgContent = new MsgContent();
		try {
			JSONObject jsonObject = new JSONObject(msg_body);
			JSONObject content = jsonObject.getJSONObject("content");
			String buyerNick = content.getString("buyer_nick");
			String payment = content.getString("payment");
			String status = content.getString("status");
			Long oid = Long.parseLong(content.getString("oid"));
			String rater = content.getString("rater");
			Long tid = Long.parseLong(content.getString("tid"));
			String type = content.getString("type");
			String sellerNick = content.getString("seller_nick");
			
			msgContent = new MsgContent(buyerNick, payment, status, oid, rater, tid, type, sellerNick);
			
			return msgContent;
		} catch (JSONException e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	public static void test() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		 //获取当前月第一天：
		Calendar c = Calendar.getInstance();    
		c.add(Calendar.MONTH, 0);
		c.set(Calendar.DAY_OF_MONTH,1);  //设置为1号,当前日期既为本月第一天 
		String first = sdf.format(DateUtil.formDailyTimestamp(c.getTime()));
		System.out.println("===============first:"+first);
		
		//获取当前月最后一天
		Calendar ca = Calendar.getInstance();    
		ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));  
		String last = sdf.format(DateUtil.formDailyTimestamp(ca.getTime()));
		System.out.println("===============last:"+last);
	}

	public static void testTbkDgNewuserOrderGet() {
		TaobaoClient client = new DefaultTaobaoClient(URL, "24800017", "202810c2bca0a91cfbd55a25c0390a1b");
		TbkDgNewuserOrderGetRequest req = new TbkDgNewuserOrderGetRequest();
		req.setPageNo(1L);
		req.setPageSize(20L);
		req.setAdzoneId(242092024L);
		TbkDgNewuserOrderGetResponse response = null;
		try {
			response = client.execute(req, "");
		} catch (ApiException e) {
			e.printStackTrace();
		}
		System.out.println(response.getBody());
	}

	public static String testTbkAdzoneCreate(String name) throws InterruptedException {
		TaobaoClient client = new DefaultTaobaoClient(URL, "24800017", "202810c2bca0a91cfbd55a25c0390a1b");
		TbkAdzoneCreateRequest req = new TbkAdzoneCreateRequest();
		req.setSiteId(42608322L);
		req.setAdzoneName(name);
		TbkAdzoneCreateResponse response = null;
		try {
			response = client.execute(req, "");
		} catch (Exception e) {
			System.out.println("retry: " + name);
			Thread.sleep(500L);
			return testTbkAdzoneCreate(name);
		}
		return response.getData().getModel() + "~~~~~~" + name;
	}

	public static void TestTbkAdzoneCreate() throws FileNotFoundException {
		ExecutorService pool = Executors.newFixedThreadPool(10);
		int size = 1000;
		for (int i = 0; i < 6000 / size; i++) {
			final int start = size * i;
			final int end = size * (i + 1);
			final int page = i;
			pool.execute(new Runnable() {
				@Override
				public void run() {
					FileOutputStream out = null;
					try {
						out = new FileOutputStream("C:/tbk" + page + ".txt");
						for (int j = start; j < end; j++) {
							String name = "api推广" + j;
							String result = testTbkAdzoneCreate(name);
							out.write((result + "\r\n").getBytes());
							out.flush();
						}
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} finally {
						try {
							out.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			});
		}
	}
	
	public static void testTbkDgItemCouponGet() {
		TaobaoClient client = new DefaultTaobaoClient(URL, "24800017", "202810c2bca0a91cfbd55a25c0390a1b");
		TbkDgItemCouponGetRequest req = new TbkDgItemCouponGetRequest();
		req.setAdzoneId(269058539L);
		req.setQ("南极人童装男童女童纯棉内衣套装儿童秋衣秋裤宝宝保暖睡衣中大童");
		TbkDgItemCouponGetResponse response = null;
		try {
			response = client.execute(req, "");
		} catch (ApiException e) {
			e.printStackTrace();
		}
		System.out.println(response.getBody());
	}
	
	public static void aaa() {
		ExecutorService pool = Executors.newFixedThreadPool(3);
		
		for (int i = 0; i < 5; i++) {
			pool.execute(new Runnable() {
				@Override
				public void run() {
					System.out.println("当前线程【" + Thread.currentThread().getName() + "】正在执行中...");
				}
			});
		}
	}

	public static void main(String[] args) throws Exception {
//		JMSTest.tmallItemIncrementUpdateSchemaGet();
//		JMSTest.tmallItemSchemaIncrementUpdate();
//		JMSTest.updateItemTitle();
//		JMSTest.pictureCategoryGet();
//		JMSTest.pictureGet();
//		JMSTest.itemSellerGet();
//		JMSTest.getSku();
//		JMSTest.getItempropvalues();
//		JMSTest.getItemprops();
//		JMSTest.getlogisticsOrdersDetail();
//		JMSTest.getPromotionList();
//		JMSTest.testSellerGet();
//		JMSTest.testSubuserGet();
//		JMSTest.testJmsUserAdd();
//		JMSTest.testJmsUserGet();
//		JMSTest.testJmsUserDelete();
//		JMSTest.testJmsTopicsGet();
//		JMSTest.testItemCatGet();
//		JMSTest.testShopGet();
//		JMSTest.testTradeGet();
		JMSTest.testTradesSoldGet();
//		JMSTest.testTradeRateListAdd();
//		JMSTest.testTradeFullGet();
//		JMSTest.testTradeRateGet();
//		JMSTest.testShopRemainshowcaseGet();
//		JMSTest.testItemRecommendAdd();
//		JMSTest.ConsumerTest();
//		JMSTest.test();
//		testTbkDgNewuserOrderGet();
//		JMSTest.TestTbkAdzoneCreate();
//		testTbkDgItemCouponGet();
//		aaa();
	}

}
