
package utils;

import static java.lang.String.format;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import job.writter.CommentsWritter;
import job.writter.OpLogWritter;
import job.writter.UserTradeCommentLogWritter;
import models.comment.CommentConf;
import models.comment.Comments;
import models.comment.CommentsFailed;
import models.item.ItemCatPlay;
import models.oplog.OpLog.LogType;
import models.showwindow.ShowWindowConfig;
import models.showwindow.ShowwindowMustDoItem;
import models.user.User;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import result.IItemBase.ItemBaseBean;
import secure.SimulateRequestUtil;
import spider.mainsearch.MainSearchApi;
import spider.mainsearch.MainSearchApi.PriceRangeLike;
import sun.misc.BASE64Encoder;
import bustbapi.ErrorHandler;
import bustbapi.TBApi;
import bustbapi.TMTradeApi.TradesSoldUnComment;
import bustbapi.TradeRateApi;
import bustbapi.UserAPIs;
import cache.UserHasTradeItemCache;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.NumberUtil;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.domain.Order;
import com.taobao.api.domain.SellerCat;
import com.taobao.api.domain.Trade;
import com.taobao.api.domain.TradeRate;
import com.taobao.api.internal.util.WebUtils;
import com.taobao.api.internal.util.codec.Base64;
import com.taobao.api.request.IncrementCustomerPermitRequest;
import com.taobao.api.request.IncrementCustomerStopRequest;
import com.taobao.api.request.IncrementCustomersGetRequest;
import com.taobao.api.request.SellercatsListGetRequest;
import com.taobao.api.request.TmcUserPermitRequest;
import com.taobao.api.request.TraderateAddRequest;
import com.taobao.api.request.TradesSoldGetRequest;
import com.taobao.api.response.IncrementCustomerPermitResponse;
import com.taobao.api.response.IncrementCustomerStopResponse;
import com.taobao.api.response.IncrementCustomersGetResponse;
import com.taobao.api.response.SellercatsListGetResponse;
import com.taobao.api.response.TmcUserPermitResponse;
import com.taobao.api.response.TraderateAddResponse;
import com.taobao.api.response.TradesSoldGetResponse;

import configs.TMConfigs;
import configs.TMConfigs.App;
import controllers.APIConfig;
import controllers.TmSecurity;
import controllers.TmSecurity.SecurityType;
import dao.UserDao;
import dao.defense.BlackListBuyerDao;
import exceptions.EncryptException;

/**
 * 淘宝相关
 * 
 * @author cst
 */
public class TaobaoUtil {

    private static final Logger log = LoggerFactory.getLogger(TaobaoUtil.class);

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public static final SimpleDateFormat desdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static final String DEFAULT_CHAR_SET = "gbk";

    public static final Long VISITLOG_MAX_RECHABLE = 7L;

    public static final double MAX_CONVERSION = 0.9;

    public static final double BENCHMARK_OVERFLOW = 1.3;

    /******************************** 用户的登录签名验证部分 **********************************/
    /***********************************************************************************/
    /**
     * 验证签名
     * 
     * @param sign
     * @param parameter
     *            注意，这个parameter并不就是上面的top_paramater，而是指的待签名验证的参数，即上面的top_appkey +top_parameter+top_session
     * @param secret
     * @return
     * @throws EncryptException
     */
    public static boolean validateSign(String sign, String parameter, String secret) throws EncryptException {
        return sign != null && parameter != null && secret != null && sign.equals(sign(parameter, secret));
    }

    /**
     * 验证时间戳是否在应用允许的误差范围；然后验证时间戳是否在允许的范围内（官方建议误差在5分钟以内，最长不超过30分钟）
     * 
     * @param top_parameters
     * @param minMins
     *            最小时长（分钟）
     * @param maxMins
     *            最大时长（分钟）
     * @return
     */
    public static boolean validateTimestamp(String top_parameters, int minMins, int maxMins) {
        Map<String, String> params = convertBase64StringtoMap(top_parameters);
        // 单位纳秒
        long ts = Long.parseLong((String) (params.get("ts")));
        // 一分钟60秒，一秒钟1000毫秒，一毫秒等于1000微妙，一微秒等于1000纳秒
        return ts >= (minMins * 60L * 1000L * 1000L * 1000L) && ts <= (maxMins * 60L * 1000L * 1000L * 1000L);
    }

    /**
     * 验证TOP回调地址的签名是否合法。要求所有参数均为已URL反编码的。
     * 
     * @param topParams
     *            TOP私有参数（未经BASE64解密）
     * @param topSession
     *            TOP私有会话码
     * @param topSign
     *            TOP回调签名
     * @param appKey
     *            应用公钥
     * @param appSecret
     *            应用密钥
     * @param appSecret
     *            应用密钥
     * @return 验证成功返回true，否则返回false
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public static boolean verifyTopResponse(String topParams, String topSession, String topSign, String appKey,
            String appSecret) throws NoSuchAlgorithmException, IOException {
        StringBuilder result = new StringBuilder();
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        result.append(appKey).append(topParams).append(topSession).append(appSecret);
        byte[] bytes = md5.digest(result.toString().getBytes("UTF-8"));
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(bytes).equals(topSign);

    }

    /**
     * 签名运算
     * 
     * @param parameter
     * @param secret
     * @return
     * @throws EncryptException
     * 
     */
    public static String sign(String parameter, String secret) throws EncryptException {
        // 对参数+密钥做MD5运算
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            play.Logger.error(e.getMessage(), e);
            throw new EncryptException(e);
        }
        byte[] digest = md.digest((parameter + secret).getBytes());
        // 对运算结果做BASE64运算并加密
        BASE64Encoder encode = new BASE64Encoder();
        return encode.encode(digest);
    }

    // 解析top_paramerters
    public static String ParametersName(String top_parameters) {
        String nick = null;
        Map<String, String> map = convertBase64StringtoMap(top_parameters);
        Iterator keyValuePairs = map.entrySet().iterator();
        for (int i = 0; i < map.size(); i++) {
            Map.Entry entry = (Map.Entry) keyValuePairs.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (key.equals("visitor_nick")) {
                nick = (String) value;
            }
        }
        return nick;
    }

    /**
     * 把经过BASE64编码的字符串转换为Map对象
     * 
     * @param str
     * @return
     * @throws Exception
     */
    public static Map<String, String> convertBase64StringtoMap(String str) {
        if (str == null)
            return null;
        String keyvalues = null;
        try {
            keyvalues = new String(Base64.decodeBase64(URLDecoder.decode(str, "utf-8").getBytes("utf-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String[] keyvalueArray = keyvalues.split("\\&");
        Map<String, String> map = new HashMap<String, String>();
        for (String keyvalue : keyvalueArray) {
            String[] s = keyvalue.split("\\=");
            if (s == null || s.length != 2)
                return null;
            map.put(s[0], s[1]);
        }
        return map;
    }

    /**
     * Trade状态
     * 
     * @author LY
     * 
     */
    public static enum TRADE_STATUS {
        TRADE_NO_CREATE_PAY, WAIT_BUYER_PAY, WAIT_SELLER_SEND_GOODS, WAIT_BUYER_CONFIRM_GOODS, TRADE_BUYER_SIGNED, TRADE_FINISHED, TRADE_CLOSED, TRADE_CLOSED_BY_TAOBAO;
    };

    /**
     * Trade状态String转变为int
     * 
     * @param status
     * @return
     */
    public static int status2int(String status) {
        return TRADE_STATUS.valueOf(status).ordinal();
    }

    // /**
    // * 处理API调用出错
    // *
    // * @param user
    // * @param response
    // */
    // public static boolean APIErrorHandler(User user, TaobaoResponse response) {
    // if (response == null) {
    // log.warn("not response");
    // return false;
    // }
    // if (response.isSuccess()) {
    // return true;
    // }
    //
    // String errorCode = response.getErrorCode();
    // if (errorCode.equals("7")) {
    // log.warn("API limited...");
    // /**
    // * 调用次数受限
    // */
    // TaobaoUtil.App_Call_Limited = true;
    // } else if (errorCode.equals("27")) {
    // log.warn("session key invalid for user :" + user);
    // /**
    // * 无效的SessionKey参数
    // */
    // // UserDao.updateIsVaild(user, false);
    // UserDao.setInvalidStatus(user);
    // } else if (errorCode.equals("isv.invalid-permission:1010")) {
    // log.info("  " + UserDao.updateHasEService(user, false));
    // } else {
    // log.warn(response.getErrorCode() + " body :" + response.getBody());
    // }
    // return false;
    // }
    //
    // /**
    // * 判断是否APP是否调用受限
    // *
    // * @return
    // */
    // public static boolean isAppCallLimited() {
    // /**
    // * 如果调用次数受限，那就什么事都不做。
    // */
    // if (TaobaoUtil.App_Call_Limited) {
    // /**
    // * 如果初始App_Call_Limited = true，调用一次接口来判断是否已经不受限制
    // */
    // TaobaoClient client = new DefaultTaobaoClient(
    // PolicyUtil.API_TAOBAO_URL, PolicyUtil.APP_KEY,
    // PolicyUtil.APP_SECRET);
    // UserGetRequest req = new UserGetRequest();
    // req.setFields("nick");
    // req.setNick("simpleliangy");
    //
    // try {
    // UserGetResponse response = client.execute(req);
    // if (response.isSuccess()) {
    // TaobaoUtil.App_Call_Limited = false;
    //
    // log.warn("App_Call_Limited call UserGet to test is app call limited:"
    // + TaobaoUtil.App_Call_Limited);
    // }
    // } catch (ApiException e) {
    // log.error("isAppCallLimited api occur error!");
    // }
    // }
    // return TaobaoUtil.App_Call_Limited;
    // }

    public static Long getDayTime(Date date) {
        Long timeLong = null;
        try {
            timeLong = sdf.parse(sdf.format(date)).getTime();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return timeLong;
    }

    public static Long getDayTime(Long timeLong) {

        try {
            timeLong = sdf.parse(sdf.format(new Date(timeLong))).getTime();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return timeLong;
    }

    public static String getMapKey(Long key1, Long key2) {
        return String.valueOf(key1).concat(String.valueOf(key2));
    }

    public static String getMapKey(Long key1, Long key2, Long key3) {
        return String.valueOf(key1).concat(String.valueOf(key2)).concat(String.valueOf(key3));
    }

    //
    // public static void customerPermit(String sessionKey) {
    // permitByUser(sessionKey);
    // }

    public static void PermitDemoUser() {
        // permitByUser("6101723e0a979ad087824a129ba66b21354a81836153a2b892086044");//东升嘉利品牌
    }

    public static void customerPermitStop() {
        // List<User> users = User.findAll();
        List<User> users = UserDao.fetchAllUser();
        for (User user : users) {
            stopPermitByUser(user.sessionKey, user.getUserNick());
        }
    }

    public static boolean permitTMCUser(User user) {
        // 自动标题不再使用tmc
        if("21348761".equals(TMConfigs.App.APP_KEY)) {
        	return false;
        }
        
        log.info("[permit tmc user:]" + user);
        TaobaoClient client = TBApi.genClient();

        TmcUserPermitRequest req = new TmcUserPermitRequest();
        req.setTopics(StringUtils.join(APIConfig.get().getTmcTopics(), ','));

        int retryTime = 3;
        while ((retryTime--) > 0) {
            try {
                TmcUserPermitResponse response = client.execute(req, user.getSessionKey());
                if (response.isSuccess()) {
                    return true;
                }
                boolean success = ErrorHandler.fuckWithTheErrorCode(user.getId(), user.getSessionKey(),
                        response.getSubCode());
                if (!success) {
                	break;
                }

                if ("sessionkey-not-generated-by-server".equals(response.getSubCode())) {
                    CommonUtils.sleepQuietly(1000L);
                    break;
                }

                int banSeconds = ErrorHandler.extractBanSeconds(response.getSubMsg());
                log.info("[sleep for :]" + banSeconds);
                if (banSeconds > 0) {
                    CommonUtils.sleepQuietly(banSeconds * 1000L + (System.currentTimeMillis() % 3000L));
                }

            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }
        return false;
    }

    public static boolean permitByUser(User user) {
        TaobaoClient client = TBApi.genClient();
        IncrementCustomerPermitRequest req = new IncrementCustomerPermitRequest();
        req.setType("get,syn,notify");
        req.setTopics("trade;item");
        req.setStatus("TradeCreate,TradeSuccess,TradeChanged,TradeRated;" +
                "ItemAdd,ItemRecommendDelete,ItemRecommendAdd,ItemDownshelf,ItemPunishDownshelf,ItemDelete");
        // req.setTopics("trade;refund;item");
        // req.setStatus("all;all;ItemAdd,ItemUpdate");
        int retryTime = 2;
        while ((retryTime--) > 0) {
            try {
                IncrementCustomerPermitResponse response = client.execute(req, user.getSessionKey());
                if (response.isSuccess()) {
                    return true;
                }
                boolean success = ErrorHandler.fuckWithTheErrorCode(user.getId(), user.getSessionKey(),
                        response.getSubCode());
                if (!success) {
                    return false;
                }
                int banSeconds = ErrorHandler.extractBanSeconds(response.getSubMsg());
                log.info("[sleep for :]" + banSeconds);
                if (banSeconds > 0) {
                    CommonUtils.sleepQuietly(banSeconds * 1000L);
                }

            } catch (ApiException e) {
                log.warn(e.getMessage(), e);
            }
        }

        return false;

    }

    public static List<SellerCat> getSellerCatByUserId(User user) {
        if (user == null) {
            user = UserDao.findByUserNick("楚之小南");
        }
        List<SellerCat> scs = new ArrayList<SellerCat>();
        /*TaobaoClient client = new DefaultTaobaoClient(App.API_TAOBAO_URL, TMConfigs.App.APP_KEY,
                TMConfigs.App.APP_SECRET);*/
        TaobaoClient client = TBApi.genClient();
        SellercatsListGetRequest req = new SellercatsListGetRequest();
        req.setNick(user.getUserNick());
        try {
            SellercatsListGetResponse response = client.execute(req, user.getSessionKey());
            JSONObject obj = new JSONObject(response.getBody());
            if (obj != null) {
                JSONObject sellercats_list_get_response  = obj.getJSONObject("sellercats_list_get_response");
                if (sellercats_list_get_response.isNull("seller_cats")) return scs;
                JSONObject seller_cats = sellercats_list_get_response.getJSONObject("seller_cats");
                JSONArray seller_catArr = seller_cats.getJSONArray("seller_cat");
                for (int i = 0; i < seller_catArr.length(); i++) {
                    JSONObject sellerCat = (JSONObject) seller_catArr.get(i);
                    Long cid = sellerCat.getLong("cid");
                    Long parent_cid = sellerCat.getLong("parent_cid");
                    String name = sellerCat.getString("name");
                    SellerCat sc = new SellerCat();
                    sc.setCid(cid);
                    sc.setName(name);
                    sc.setParentCid(parent_cid);
                    scs.add(sc);
                }
                return scs;
            }
        } catch (ApiException e) {
            log.warn(e.getMessage());
        } catch (JSONException e) {
            log.warn(e.getMessage());
        }
        return null;
    }

    public static boolean stopPermitByUser(String sessionKey, String nick) {
        TaobaoClient client = new DefaultTaobaoClient(App.API_TAOBAO_URL, TMConfigs.App.APP_KEY,
                TMConfigs.App.APP_SECRET);
        IncrementCustomerStopRequest req = new IncrementCustomerStopRequest();
        req.setNick(nick);
        req.setType("get,notify,syn");
        try {
            IncrementCustomerStopResponse response = client.execute(req);
            return response.isSuccess();
        } catch (ApiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    public static Long getFirstLevel(Long cid) {
        if (cid == null || cid == 0) {
            return null;
        }
        Long firstlevel = cid;
        Long parentlevel;
        try {
            while (firstlevel != 0) {
                parentlevel = ItemCatPlay.findByCid(firstlevel).parentCid;
                if (parentlevel == 0) {
                    return firstlevel;
                } else {
                    firstlevel = parentlevel;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return null;
    }

    public static boolean checkUserPermitted(String nick) {
        TaobaoClient client = TBApi.genClient();
        IncrementCustomersGetRequest req = new IncrementCustomersGetRequest();
        req.setNicks(nick);
        req.setPageSize(10L);
        req.setPageNo(1L);
        req.setType("get,syn,notify");
        req.setFields("nick");
        try {
            IncrementCustomersGetResponse response = client.execute(req);
            if (response.getTotalResults() > 0)
                return true;
        } catch (ApiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    public static String formItemDetailURL(Long numIid) {

        return App.TAOBAO_ITEM_URL.concat(String.valueOf(numIid));
    }

    public final static String TRADEINFO_FIELDS = " buyer_nick, tid,status,created,pay_time,consign_time,end_time,modified, orders.oid, "
            + "orders.status,  orders.num_iid, orders.cid, orders.num, orders.payment,orders.price,orders.total_fee, orders.buyer_nick";

    public final static String TRADEINFO_ACOOKIE_FIELDS = "acookie_id,status,buyer_nick,num,payment,"
            + "created,pay_time,consign_time,end_time,modified,"
            + "orders.num_iid,orders.status,orders.cid,orders.num,orders.payment";

    // public final static String TRADEINFO_ACOOKIE_FIELDS =
    // "acookie_id,tid,num,price,status,title,shipping_type,adjust_fee,buyer_obtain_point_fee,cod_fee,cod_status,buyer_area,seller_nick,buyer_nick,type,created,pic_path,payment,discount_fee,point_fee,real_point_fee,total_fee,post_fee,commission_fee,received_payment,num_iid,modified,orders.status,orders.title,orders.price,orders.num_iid,orders.item_meal_id,orders.sku_id,orders.num,orders.outer_sku_id,orders.total_fee,orders.payment,orders.discount_fee,orders.adjust_fee,orders.sku_properties_name,orders.item_meal_name,orders.pic_path,orders.seller_nick,orders.buyer_nick,orders.refund_status,orders.outer_iid,orders.seller_type,orders.cid";

    public final static String ITEM_FIELDS = "desc, seller_cids,detail_url,approve_status,num_iid,title,nick,cid,pic_url,num,price,list_time,delist_time,modified";

    public final static int TABLE_NUM = 25;

    public final static Long TRADE_PAGE_SIZE = 45L;

    public final static Long RATE_PAGE_SIZE = 40L;

    public final static boolean USEHASNEXT = true;

    public static final Long ITEM_PAGE_SIZE = 200L;

    public static boolean App_Call_Limited = false;

    public static double PROFIT_RATE;

    public static String W2_EXPIRES_IN = "w2_expires_in";

    // oauth2
    public static User token(String code, String status) {
        Map<String, String> param = new HashMap<String, String>();

        param.put("grant_type", "authorization_code");
        param.put("code", code);
        param.put("client_id", APIConfig.get().getApiKey());
        param.put("client_secret", APIConfig.get().getSecret());
        param.put("redirect_uri", "urn:ietf:wg:oauth:2.0:oob");
        param.put("scope", "item");
        param.put("view", "web");
        param.put("state", status);

        log.info("[param:]" + param);

        String response;
        try {
            response = WebUtils.doPost(App.TOKEN_URL, param, 3000, 3000);

            log.info("[token result :]" + response);
            JsonNode readJsonResult = JsonUtil.readJsonResult(response);
            if (readJsonResult == null) {
                return null;
            }
            String refreshToken = readJsonResult.get("refresh_token").getTextValue();
            String sid = readJsonResult.get("access_token").getTextValue();
            Long userId = NumberUtil.parserLong(readJsonResult.get("taobao_user_id").getTextValue(), 0L);
            
            if (NumberUtil.isNullOrZero(userId)) {
                log.warn("no res for :" + response);
                return null;
            }
            
            User user = UserDao.findById(userId);
            if(readJsonResult.get("sub_taobao_user_id") != null 
            		|| readJsonResult.get("sub_taobao_user_nick") != null) {
            	log.info("sub login In/Login token with sub user ["+readJsonResult.get("sub_taobao_user_nick").getTextValue()+"] " +
            			" and sub session = " + sid);
            	//String subNick = readJsonResult.get("sub_taobao_user_nick").getTextValue();
            	//if(subNick.equals("楚之小南:绵羊")) {
            		// 如果登陆的是子账号, 那么使用当前的sid，不更新到数据库
	            	if(user == null) {
	            		// 说明主账号都还没有授权，子账号必然失败
	            		return null;
	            	} else {
	            		log.info("sub login " + readJsonResult.get("sub_taobao_user_nick").getTextValue());
	            		log.info("sub login " + URLDecoder.decode(readJsonResult.get("sub_taobao_user_nick").getTextValue(), "gbk"));
	            		//user.setUserNick(readJsonResult.get("sub_taobao_user_nick").getTextValue());
	            		log.info("sub login TMController user : " + user.getUserNick());
	            		user.sessionKey = sid;
	            		user.setSub(true);
	            		return user;
	            	}
            	//}
            	
            }
            // 这是主账号的流程
            if (user == null) {
                log.warn("no res for :" + userId);
                com.taobao.api.domain.User tbUser = new UserAPIs.UserGetApi(sid, null).call();
                if (tbUser == null) {
                    return null;
                }
                log.info("UserAPIs.UserGetApi nick is " + tbUser.getNick());
                user = UserDao.addUser(tbUser, tbUser.getNick(), tbUser.getUserId(), sid, true, 0, refreshToken);
            } else {
            	user.setSub(false);
                UserDao.updateIsVaildAndSessionKey(user, true, sid, refreshToken);
            }

            return user;

        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }
        return null;

    }

    public static String refreshToken(User user) {
        try {

            JsonNode readJsonResult = getRefreshJsonNode(user);
//             log.info("refresh session key result:"+readJsonResult);
            JsonNode sessionNode = readJsonResult.get("top_session");
            if (sessionNode == null) {
                return null;
            }
            String sessionKey = sessionNode.getTextValue();
            user.sessionKey = sessionKey;
            return readJsonResult.get("refresh_token").getTextValue();

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        return null;
    }

    public static JsonNode refreshTokenAndGetJsonObject(User user) {
        try {
            JsonNode readJsonResult = getRefreshJsonNode(user);
            // log.info("refresh session key result:"+readJsonResult);
            String sessionKey = readJsonResult.get("top_session").getTextValue();
            user.sessionKey = sessionKey;
            return readJsonResult;
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        return null;
    }

    public static JsonNode getRefreshJsonNode(User user) throws Exception {
        String appkey = APIConfig.get().getApiKey();
        String secret = APIConfig.get().getSecret();
        String sessionkey = user.getSessionKey();
        String refreshToken = StringUtils.isEmpty(user.getRefreshToken()) ? user.getSessionKey() : user
                .getRefreshToken();

        log.warn("refresh for session:" + sessionkey);
        log.warn("refresh for refreh :" + refreshToken);

        Map<String, String> signParams = new TreeMap<String, String>();
        signParams.put("appkey", appkey);
        signParams.put("refresh_token", refreshToken);
        signParams.put("sessionkey", sessionkey);
        StringBuilder paramsString = new StringBuilder();
        Set<Entry<String, String>> paramsEntry = signParams.entrySet();
        for (Entry paramEntry : paramsEntry) {
            paramsString.append(paramEntry.getKey()).append(paramEntry.getValue());
        }
        String sign = DigestUtils.md5Hex((paramsString.toString() + secret).getBytes("utf-8")).toUpperCase();
        String signEncoder = URLEncoder.encode(sign, "utf-8");
        String appkeyEncoder = URLEncoder.encode(appkey, "utf-8");
        String refreshTokenEncoder = URLEncoder.encode(refreshToken, "utf-8");
        String sessionkeyEncoder = URLEncoder.encode(sessionkey, "utf-8");
        String freshUrl = TMConfigs.App.REFRESH_URL + "?appkey=" + appkeyEncoder + "&refresh_token="
                + refreshTokenEncoder + "&sessionkey=" + sessionkeyEncoder + "&sign=" + signEncoder;

        String response = WebUtils.doPost(freshUrl, null, 30 * 1000 * 60, 30 * 1000 * 60);

        log.info("refresh final freshUrl=" + freshUrl + ",response=" + response);

        JsonNode readJsonResult = JsonUtil.readJsonResult(response);
        // log.info("[refresh resp : ]" + response);

        return readJsonResult;
    }

    public static String getRefreshResponseProperty(User user, String property) {
        try {
            JsonNode readJsonResult = getRefreshJsonNode(user);

            return readJsonResult.get(property).getTextValue();
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        return null;
    }

    public static void getOrdersByUser(User user, Date start, Date end) {

        log.info(format("getOrdersByUser:user, start, end".replaceAll(", ", "=%s, ") + "=%s", user, start, end));
        // User user = UserDao.findByUserNick("0123maixie");
        // Date end = new Date();
        // Date start = new Date(end.getTime() - System.currentTimeMillis());
        log.info("comment check for user : " + user.userNick);
        TaobaoClient client = TBApi.genClient();
        TradesSoldGetRequest req = new TradesSoldGetRequest();
        req.setFields("tid,orders,buyer_nick,seller_can_rate");
        req.setStartCreated(start);
        req.setEndCreated(end);
        req.setStatus("TRADE_FINISHED");
        // req.setType("game_equipment");
        // req.setExtType("service");
        req.setRateStatus("RATE_UNSELLER");
        // req.setTag("time_card");
        // req.setPageNo(1L);
        // req.setPageSize(40L);
        // req.setUseHasNext(true);
        // req.setIsAcookie(false);
        try {
            TradesSoldGetResponse response = client.execute(req, user.sessionKey);
            // 御城河日志接入
            SimulateRequestUtil.sendTopLog(SimulateRequestUtil.TRADES_SOLD_GET);
            if (response.isSuccess()) {
                int orderTotal = 0;
                int successOrderCount = 0;
                int failOrderCount = 0;
                int cannotrateCount = 0;
                JSONObject trades_sold_get_response = new JSONObject(response.getBody())
                        .getJSONObject("trades_sold_get_response");
                JSONObject obj = null;
                if (trades_sold_get_response.has("trades")) {
                    obj = trades_sold_get_response.getJSONObject("trades");
                    JSONArray trades = obj.getJSONArray("trade");
                    if (trades.length() > 0) {
                        int i = 0;
                        while (i++ < trades.length()) {
                            JSONObject trade = (JSONObject) trades.get(i - 1);

                            Long tid = Long.parseLong(trade.getString("tid"));
                            String buyerNick = trade.getString("buyer_nick");
                            JSONObject orderObj = trade.getJSONObject("orders");
                            JSONArray orders = orderObj.getJSONArray("order");
                            // 检测卖家是否可以评价
                            if (!trade.getBoolean("seller_can_rate")) {
                                log.error("seller can not rate this trade!!!");
                                cannotrateCount += orders.length();
                                orderTotal += orders.length();
                                continue;
                            }
                            if (orders.length() > 0) {
                                int j = 0;
                                while (j++ < orders.length()) {
                                    orderTotal++;
                                    JSONObject order = (JSONObject) orders.get(j - 1);
                                    // 如果卖家已评价
                                    if (order.getBoolean("seller_rate")) {
                                        log.info("seller already rated!!!");
                                        cannotrateCount++;
                                        continue;
                                    }
                                    // 如果卖家是商城卖家
                                    if (order.getString("seller_type").equals("B")) {
                                        log.info("tmall seller, can not rate!!!");
                                        cannotrateCount++;
                                        continue;
                                    }
                                    if (order.getString("end_time") == null || order.getString("end_time").isEmpty()) {
                                        log.info("order is 15 days before , can not rate any more!!!");
                                        cannotrateCount++;
                                        continue;
                                    }
                                    // 检测子订单结束时间是否超过15天
                                    Date date = desdf.parse(order.getString(("end_time")));
                                    if (System.currentTimeMillis() - date.getTime() > DateUtil.FIFTEEN_DAYS) {
                                        log.info("order is 15 days before , can not rate any more!!!");
                                        cannotrateCount++;
                                        continue;
                                    }
                                    /*
                                     * //检测cid是否为“订单、赠品、定金、新品预览、邮费”子类目 if(order.getLong("cid")){
                                     * 
                                     * }
                                     */
                                    Long oid = Long.parseLong(order.getString("oid"));
                                    String conf = CommentConf.findConf(user.getId());
                                    String content = StringUtils.EMPTY;
                                    if (conf == null || conf.isEmpty()) {
                                        content = "很好的买家，欢迎下次再来！";
                                    } else {
                                        int length = conf.split("!@#").length;
                                        if (length <= 0) {
                                            content = "很好的买家，欢迎下次再来！";
                                        } else {
                                            int offset = new Random().nextInt(length);
                                            content = conf.split("!@#")[offset];
                                        }

                                    }
                                    if (content.isEmpty()) {
                                        content = "很好的买家，欢迎下次再来！";
                                    }
                                    boolean isSuccess = commentNow(user.userNick, user.getId(), buyerNick, tid, oid,
                                            content);
                                    if (isSuccess) {
                                        successOrderCount++;
                                        log.info("comment success for userNick = " + user.userNick
                                                + " and  buyerNick = " + buyerNick + " and tid = " + tid + " and oid "
                                                + oid);
                                    } else {
                                        failOrderCount++;
                                        log.info("comment failed for userNick = " + user.userNick
                                                + " and  buyerNick = " + buyerNick + " and tid = " + tid + " and oid "
                                                + oid);
                                        OpLogWritter.addMsg(user.getId(), "autoCommentCrontabJob comment failed", oid,
                                                LogType.autocommentfail, true);
                                    }
                                }
                            }
                        }
                    }
                    log.info("orderTotal = " + orderTotal);
                    log.info("successOrderCount = " + successOrderCount);
                    log.info("failOrderCount = " + failOrderCount);
                    log.info("cannotrateCount = " + cannotrateCount);
                } else {
                    log.info("no trades return");
                }

            }
        } catch (ApiException e) {
            log.error("Taobao API TradesSoldGetRequest error!!!!");
            log.warn(e.getMessage(), e);
        } catch (JSONException e) {
            log.error("json error!!!!");
            log.warn(e.getMessage(), e);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static boolean commentNow(String userNick, Long userId, String buyerNick, Long tid, Long oid, String content) {
    	
        log.info(format("doComment:userNick, userId, buyerNick, tid, oid, content".replaceAll(", ", "=%s, ") + "=%s",
                userNick, userId, buyerNick, tid, oid, content));

        boolean doneForThisTime = false;
        int retry = 2;
        log.info("Do autoEvaluate Job with parasm: ");
        String sessionKey = StringUtils.EMPTY;
        User user = UserDao.findById(userId);

        try {
            if (user == null) {
                log.info("no user found in commentNow");
                return false;
            } else {
                sessionKey = user.sessionKey;
            }
            
            buyerNick = TmSecurity.decrypt(buyerNick, SecurityType.SIMPLE, user);

            if (content.isEmpty()) {
                content = "欢迎下次光临";
            }
            if (BlackListBuyerDao.findByBuyerName(user.getId(), buyerNick) != null) {
                log.info("黑名单用户，不评价");
                return false;
            }
            TaobaoClient client = TBApi.genClient();
            TraderateAddRequest req = new TraderateAddRequest();
            req.setTid(tid);
            req.setOid(oid);
            req.setResult("good");
            req.setRole("seller");
            req.setContent(content);
            req.setAnony(false);

            int count = 0;

            while (count++ < retry && !doneForThisTime) {
                TraderateAddResponse response = client.execute(req, sessionKey);
                if (response.isSuccess()) {
                    log.info("comment success for tid = " + tid + " and cid = " + oid);
                    doneForThisTime = true;
                    // sub string when content is too long to save
                    String realContent = StringUtils.EMPTY;
                    if (content.length() > 255) {
                        realContent = content.substring(0, 250).concat("...");
                    } else {
                        realContent = content;
                    }
                    new Comments(user.getId(), tid, oid, "good", realContent, userNick, buyerNick).save();
                    return true;
                }
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }

        if (!doneForThisTime) {
            log.error("[comment failed for user = " + user.userNick + " and tid = " + tid + " and oid = " + oid + " ]");

            // sub string when content is too long to save
            String realContent = StringUtils.EMPTY;
            if (content.length() > 255) {
                realContent = content.substring(0, 250).concat("...");
            } else {
                realContent = content;
            }
            new CommentsFailed(user.getId(), tid, oid, "good", realContent, userNick, buyerNick, "评价失败").save();
            return false;
        }
        return false;
    }

    public static String noUserFoundReasonString = "no user found in commentNowWithReason";

    public static String UsersNotAllowAutoComment = "this users does not allow autocomment";

    public static String commErrorString = "trycatcherror";

    public static String commentNowWithReason(String userNick, Long userId, String buyerNick, String result, Long tid,
            Long oid, String content) {

        log.info(format(
                "commentNowWithReason : userNick, userId, buyerNick, tid, oid, content".replaceAll(", ", "=%s, ")
                        + "=%s", userNick, userId, buyerNick, tid, oid, content));

        boolean doneForThisTime = false;
        int retry = 2;
        // log.info("Do autoEvaluate Job with parasm: ");
        String sessionKey = StringUtils.EMPTY;
        User user = UserDao.findById(userId);
        try {
            if (user == null) {
                log.info(noUserFoundReasonString);
                return noUserFoundReasonString;
            } else {
                sessionKey = user.sessionKey;
            }

            if (StringUtils.isEmpty(content)) {
                content = "欢迎下次光临";
            }
            if (StringUtils.isEmpty(result)) {
                result = "good";
            }
            if (BlackListBuyerDao.findByBuyerName(user.getId(), buyerNick) != null) {
                return "黑名单用户，不评价";
            }
            int count = 0;
            while (count++ < retry && !doneForThisTime) {
                TradeRate tradeRate = new TradeRateApi.TraderateAdd(user, tid, oid, result, "seller", content).call();
                if (tradeRate != null) {
                    doneForThisTime = true;
                    String realContent = StringUtils.EMPTY;
                    if (content.length() > 255) {
                        realContent = content.substring(0, 250).concat("...");
                    } else {
                        realContent = content;
                    }
                    CommentsWritter.addMsg(userId, tid, oid, result, realContent, user.getUserNick(), buyerNick);
                    return StringUtils.EMPTY;
                }
            }
            return "评价出错";
            /*TaobaoClient client = TBApi.genClient();
            TraderateAddRequest req = new TraderateAddRequest();
            req.setTid(tid);
            if (oid != null && oid > 0) {
                req.setOid(oid);
            }
            req.setResult(result);
            req.setRole("seller");
            req.setContent(content);
            req.setAnony(false);

            int count = 0;
            String subMsg = StringUtils.EMPTY;
            while (count++ < retry && !doneForThisTime) {
                TraderateAddResponse response = client.execute(req, sessionKey);
                if (response.isSuccess()) {
                    // log.info("comment success for tid = " + tid + " and cid = " + oid);
                    doneForThisTime = true;
                    // sub string when content is too long to save
                    String realContent = StringUtils.EMPTY;
                    if (content.length() > 255) {
                        realContent = content.substring(0, 250).concat("...");
                    } else {
                        realContent = content;
                    }
                    // new Comments(user.getId(), tid, oid, "good", realContent,
                    // userNick, buyerNick).save();
                    CommentsWritter.addMsg(userId, tid, oid, result, realContent, user.getUserNick(), buyerNick);
                    return StringUtils.EMPTY;
                } else {
                    subMsg = response.getSubMsg();
                }
            }
            return subMsg;*/
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return commErrorString;
        }
    }

    public static void checkOrdersByUser(User user, Date start, Date end, String jobTs) {
        TaobaoClient client = TBApi.genClient();
        TradesSoldGetRequest req = new TradesSoldGetRequest();
        req.setFields("tid,orders,buyer_nick,seller_can_rate");
        req.setStartCreated(start);
        req.setEndCreated(end);
        req.setStatus("TRADE_FINISHED");
        req.setRateStatus("RATE_UNSELLER");
        try {
            TradesSoldGetResponse response = client.execute(req, user.sessionKey);
            // 御城河日志接入
            SimulateRequestUtil.sendTopLog(SimulateRequestUtil.TRADES_SOLD_GET);
            if (response.isSuccess()) {
                int orderTotal = 0;
                int unCommentedOrderCount = 0;
                int cannotrateCount = 0;
                int successCount = 0;
                int failCount = 0;
                StringBuilder failOrderIds = new StringBuilder();
                JSONObject trades_sold_get_response = new JSONObject(response.getBody())
                        .getJSONObject("trades_sold_get_response");
                JSONObject obj = null;
                if (trades_sold_get_response.has("trades")) {
                    obj = trades_sold_get_response.getJSONObject("trades");
                    JSONArray trades = obj.getJSONArray("trade");
                    if (trades.length() > 0) {
                        int i = 0;
                        while (i++ < trades.length()) {
                            JSONObject trade = (JSONObject) trades.get(i - 1);

                            Long tid = Long.parseLong(trade.getString("tid"));
                            String buyerNick = trade.getString("buyer_nick");
                            JSONObject orderObj = trade.getJSONObject("orders");
                            JSONArray orders = orderObj.getJSONArray("order");
                            // 检测卖家是否可以评价
                            if (!trade.getBoolean("seller_can_rate")) {
                                cannotrateCount += orders.length();
                                orderTotal += orders.length();
                                continue;
                            }
                            if (orders.length() > 0) {
                                int j = 0;
                                while (j++ < orders.length()) {
                                    orderTotal++;
                                    JSONObject order = (JSONObject) orders.get(j - 1);
                                    Long oid = Long.parseLong(order.getString("oid"));
                                    // 如果卖家已评价
                                    if (order.getBoolean("seller_rate")) {
                                        cannotrateCount++;
                                        continue;
                                    }
                                    // 如果卖家是商城卖家
                                    if (order.getString("seller_type").equals("B")) {
                                        cannotrateCount++;
                                        continue;
                                    }
                                    // 检测子订单结束时间是否超过15天
                                    if (order.getString("end_time") == null || order.getString("end_time").isEmpty()) {
                                        cannotrateCount++;
                                        continue;
                                    }
                                    Date date = desdf.parse(order.getString(("end_time")));
                                    if (System.currentTimeMillis() - date.getTime() > DateUtil.FIFTEEN_DAYS) {
                                        cannotrateCount++;
                                        continue;
                                    }
                                    if (BlackListBuyerDao.findByBuyerName(user.getId(), buyerNick) != null) {
                                        log.info("黑名单用户，不评价");
                                        continue;
                                    }
                                    unCommentedOrderCount++;
                                    String content = TaobaoUtil.getCommentContent(user);
                                    String reason = TaobaoUtil.commentNowWithReason(user.userNick, user.getId(),
                                            buyerNick, "good", tid, oid, content);
                                    if (reason.isEmpty()) {
                                        successCount++;
                                    } else {
                                        failCount++;
                                        // save failLog
                                        // new CommentsFailed(user.getId(), tid, oid, "good", content,
                                        // user.getUserNick(), buyerNick, reason).jdbcSave();
                                    }
                                }
                            }
                        }
                    }
                } else {
//                    log.error("no trades return for user " + user.userNick);
                }
                // save user trade comment log
                if (orderTotal > 0) {
                    UserTradeCommentLogWritter.addMsg(user.getId(), user.getUserNick(), jobTs, orderTotal,
                            unCommentedOrderCount, cannotrateCount, successCount, failCount, failOrderIds.toString());
                }
            } else {
                // not save no user trade return comment log
                // UserTradeCommentLogWritter.addMsg(user.getId(), user.getUserNick(), jobTs, 0, 0, 0, 0, 0,
                // StringUtils.EMPTY);
            }
        } catch (ApiException e) {
            log.error("Taobao API TradesSoldGetRequest error!!!!");
            // UserTradeCommentLogWritter.addMsg(user.getId(), user.getUserNick(), jobTs, 0, 0, 0, 0, 0,
            // "API call error");
            log.warn(e.getMessage(), e);
        } catch (JSONException e) {
            log.error("json error!!!!");
            // UserTradeCommentLogWritter.addMsg(user.getId(), user.getUserNick(), jobTs, 0, 0, 0, 0, 0, "json error");
            log.warn(e.getMessage(), e);
        } catch (ParseException e) {
            UserTradeCommentLogWritter.addMsg(user.getId(), user.getUserNick(), jobTs, 0, 0, 0, 0, 0, "parse error");
            // log.warn(e.getMessage(), e);
        }
    }

    public static void commentOrdersByConf(User user, Date start, Date end, String jobTs) {

        CommentConf conf = CommentConf.findByUserId(user.getId());
        if (conf == null || conf.getCommentType() <= 0 || conf.getCommentTime() >= DateUtil.FIFTEEN_DAYS
                || conf.getCommentTime() <= 0) {
            // 不抢评，立即评论
            commentUseConf(user, start, end, jobTs, 0);
        } else {
            // 拖一定时间之前的订单，抢评
            commentUseConf(user, start, end, jobTs, conf.getCommentTime());
        }

    }

    /**
     * 评价-抢评
     * 
     * @param user
     * @param start
     * @param end
     * @param jobTs
     * @param commentTime
     *            抢评时间，赶在评价前commentTime毫秒内抢评
     */
    public static void commentUseConf(User user, Date start, Date end, String jobTs, long commentTime) {
    	if (user.isAutoCommentOn() == false) {
            log.error("[AutoComment]user auto comment = false ! userId: " + user.getId());
            return;
        }
        long current = System.currentTimeMillis();
        if (commentTime > 0) {
            // 需要抢评的
            end = new Date(current - (DateUtil.FIFTEEN_DAYS - commentTime));
        }

        List<Trade> list = new TradesSoldUnComment(user, start, end, false).call();
        if (CommonUtils.isEmpty(list)) {
            return;
        }

        int orderTotal = 0;
        int unCommentedOrderCount = 0;
        int cannotrateCount = 0;
        int successCount = 0;
        int failCount = 0;
        StringBuilder failOrderIds = new StringBuilder();
        String content = TaobaoUtil.getCommentContent(user);
        for (Trade trade : list) {
            Long tid = trade.getTid();
            String buyerNick = trade.getBuyerNick();

            List<Order> orderList = trade.getOrders();
            for (Order order : orderList) {
                orderTotal++;
                Long oid = order.getOid();
                // 如果卖家已评价
                if (order.getSellerRate() == true) {
                    cannotrateCount++;
                    continue;
                }
                // 如果卖家是商城卖家
                if ("B".equals(order.getSellerType())) {
                    cannotrateCount++;
                    continue;
                }
                // 检测子订单结束时间是否超过15天
                Date date = order.getEndTime();
                if (current - date.getTime() > DateUtil.FIFTEEN_DAYS) {
                    cannotrateCount++;
                    continue;
                }

                if (BlackListBuyerDao.findByBuyerName(user.getId(), buyerNick) != null) {
                    log.info("黑名单用户，不评价");
                    continue;
                }
                // 检查是否符合抢评时间
                if (commentTime > 0 && current - date.getTime() < DateUtil.FIFTEEN_DAYS - commentTime) {
//                    log.info(format("NO need comment now! userId=%d, tid=%d, oid=%d, comment=%d days!  endTime: %s",
//                            user.getId(), tid, oid, commentTime / DateUtil.DAY_MILLIS, desdf.format(date)));
                    continue;
                } else {
//                    log.info(format("Comment! userId=%d, tid=%d, oid=%d, comment=%d days!  endTime: %s", user.getId(),
//                            tid, oid, commentTime / DateUtil.DAY_MILLIS, desdf.format(date)));
                }

                unCommentedOrderCount++;

                String reason = TaobaoUtil.commentNowWithReason(user.userNick, user.getId(), buyerNick, "good", tid,
                        oid, content);
                if (StringUtils.isEmpty(reason)) {
                    successCount++;
                } else {
                    failCount++;
                    failOrderIds.append(oid + ",");
                }
            }
        }

        // save user trade comment log
        if (orderTotal > 0) {
            UserTradeCommentLogWritter.addMsg(user.getId(), user.getUserNick(), jobTs, orderTotal,
                    unCommentedOrderCount, cannotrateCount, successCount, failCount, failOrderIds.toString());
        }
    }

    public static String getCommentContent(User user) {
        String conf = CommentConf.findConf(user.getId());
        String content = StringUtils.EMPTY;
        if (conf == null || conf.isEmpty()) {
            content = "很好的买家，欢迎下次再来！";
        } else {
            int length = conf.split("!@#").length;
            if (length <= 0) {
                content = "很好的买家，欢迎下次再来！";
            } else {
                int offset = new Random().nextInt(length);
                content = conf.split("!@#")[offset];
            }
        }
        return content;
    }

    public static String formatYingxiaoLink(String link) {
        return link.replace("\\/", "/");
    }

    public static boolean isDouble(String value) {
        try {
            Double.parseDouble(value);
            if (value.contains("."))
                return true;
            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String fastRemove(String strSource, String strFrom) {
        if (strSource == null) {
            return null;
        }
        int i = 0;
        if ((i = strSource.indexOf(strFrom, i)) >= 0) {
            char[] cSrc = strSource.toCharArray();
            // char[] cTo = strTo.toCharArray();
            int len = strFrom.length();
            StringBuilder buf = new StringBuilder(cSrc.length);
            buf.append(cSrc, 0, i);
            i += len;
            int j = i;
            while ((i = strSource.indexOf(strFrom, i)) > 0) {
                buf.append(cSrc, j, i - j);
                i += len;
                j = i;
            }
            buf.append(cSrc, j, cSrc.length - j);
            return buf.toString();
        }
        return strSource;
    }

    public static String fastReplace(String strSource, String strFrom, String strTo) {
        if (strSource == null) {
            return null;
        }
        int i = 0;
        if ((i = strSource.indexOf(strFrom, i)) >= 0) {
            char[] cSrc = strSource.toCharArray();
            char[] cTo = strTo.toCharArray();
            int len = strFrom.length();
            StringBuilder buf = new StringBuilder(cSrc.length);
            buf.append(cSrc, 0, i).append(cTo);
            i += len;
            int j = i;
            while ((i = strSource.indexOf(strFrom, i)) > 0) {
                buf.append(cSrc, j, i - j).append(cTo);
                i += len;
                j = i;
            }
            buf.append(cSrc, j, cSrc.length - j);
            return buf.toString();
        }
        return strSource;
    }

    public static String escapeSQL(String s) {
        int length = s.length();
        int newLength = length;
        // first check for characters that might
        // be dangerous and calculate a length
        // of the string that has escapes.
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\':
                case '\"':
                case '\'':
                case '\0': {
                    newLength += 1;
                }
                    break;
            }
        }
        if (length == newLength) {
            // nothing to escape in the string
            return s;
        }
        StringBuilder sb = new StringBuilder(newLength);
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\': {
                    sb.append("\\\\");
                }
                    break;
                case '\"': {
                    sb.append("\\\"");
                }
                    break;
                case '\'': {
                    sb.append("\\\'");
                }
                    break;
                case '\0': {
                    sb.append("\\0");
                }
                    break;
                default: {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    public static Comparator<String> WordLenghComparitor = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return StringUtils.length(o2) - StringUtils.length(o1);
        }
    };

    public static void main(String[] args) {
//        List<String> toRemoveWords = new ArrayList<String>();
//        Collections.sort(toRemoveWords, TaobaoUtil.WordLenghComparitor);
//        System.out.println(toRemoveWords);
        String msg = "This ban will last for 27 more seconds";
        System.out.println(ErrorHandler.extractBanSeconds(msg));
    }

    public static String WordPriceRangeLikesPre = "WordPriceRangeLikesPre_";

    public static List<PriceRangeLike> getWordPriceRangeLikes(String word) {
        if (StringUtils.isEmpty(word)) {
            return null;
        }
        List<PriceRangeLike> res = (List<PriceRangeLike>) Cache.get(WordPriceRangeLikesPre + word);
        if (CommonUtils.isEmpty(res)) {
            res = MainSearchApi.searchWordPriceRange("男装");
            Cache.set(WordPriceRangeLikesPre + word, res, "7d");
        }
        return res;
    }

    public static String InMust = "必推宝贝";

    public static String priorSale = "销量优先";

    public static String delistTime = "下架时间优先";

    public static void setOnWindowItemReason(User user, List<ItemBaseBean> buildFromTBItem) {
        if (CommonUtils.isEmpty(buildFromTBItem)) {
            return;
        }

        List<Long> mustIds = ShowwindowMustDoItem.findAllIdsByUserId(user.getId());
        ShowWindowConfig windowConfig = ShowWindowConfig.findOrCreate(user.getId());

        if (windowConfig == null) {
            windowConfig = new ShowWindowConfig(user.getId());
        }
        int priorSaleNum = 0;
        if (windowConfig.isEnableSaleNum()) {
            priorSaleNum = windowConfig.getPriorSaleNum();
        }
        List<Long> bigSalesIds = new ArrayList<Long>();
        boolean isSalesFirst = false;
        if (priorSaleNum > 0) {
            bigSalesIds = UserHasTradeItemCache.getNumIidsByUser(user, priorSaleNum);
            isSalesFirst = true;
        }
        for (ItemBaseBean bean : buildFromTBItem) {
            bean.setSalesFirst(isSalesFirst);
            if (!CommonUtils.isEmpty(mustIds)) {
                if (mustIds.contains(bean.getNumIid())) {
                    bean.setOnShowWindowReason(InMust);
                    continue;
                }
            }
            if (!CommonUtils.isEmpty(bigSalesIds)) {
                if (bigSalesIds.contains(bean.getNumIid())) {
                    bean.setOnShowWindowReason(priorSale);
                    continue;
                }
            }
            bean.setOnShowWindowReason(delistTime);
        }
    }

}
