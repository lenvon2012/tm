
package controllers;

import static java.lang.String.format;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import job.jd.JDItemUpdateJob;
import job.paipai.PaiPaiItemUpdateJob;
import job.user.UserLoginIpJob;
import models.itemCopy.APiConfig1688;
import models.itemCopy.APiConfig1688.ApiConfig1688Status;
import models.jd.JDUser;
import models.op.TraceLogClick;
import models.paipai.PaiPaiUser;
import models.user.SellerMobile;
import models.user.UserLoginLog;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.codehaus.jackson.JsonNode;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.data.parsing.UrlEncodedParser;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.Http.Header;
import utils.CommonUtil;
import utils.TaobaoUtil;
import actions.LoginAction;
import actions.UserLoginAction;
import actions.jd.JDAction;
import actions.paipai.PaiPaiAction;
import bustbapi.ShopApi;
import bustbapi.ShopApi.ShopGet;
import bustbapi.UserAPIs;
import cache.UserLoginInfoCache;

import com.alibaba.ocean.rawsdk.ApiExecutor;
import com.alibaba.ocean.rawsdk.client.entity.AuthorizationToken;
import com.alibaba.ocean.rawsdk.example.RawSdkExample;
import com.alibaba.ocean.rawsdk.example.RawSdkExampleData;
import com.alibaba.ocean.rawsdk.example.param.ExampleFamilyPostParam;
import com.alibaba.ocean.rawsdk.example.param.ExampleFamilyPostResult;
import com.alibaba.product.param.AlibabaAgentProductGetParam;
import com.alibaba.product.param.AlibabaAgentProductGetResult;
import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.NetworkUtil;
import com.google.gson.Gson;
import com.ning.http.util.Base64;
import com.taobao.api.domain.Shop;
import com.taobao.api.domain.User;
import com.taobao.api.internal.util.TaobaoUtils;
import com.taobao.api.internal.util.WebUtils;

import configs.TMConfigs;
import configs.TMConfigs.App;
import configs.TMConfigs.WebParams;
import dao.UserDao;

public class In extends Controller {
    // 重新授权
    public static void forwardToOAuth(String state, String redirectUrl) {
        
        log.info(format("forwardToOAuth:state, redirectUrl".replaceAll(", ", "=%s, ") + "=%s", state, redirectUrl));

        String appkey = TMConfigs.App.APP_KEY;
        String client_id = appkey;
        String response_type = "code";
        String scope = "item";
        String view = "web";
        String redirect_uri = redirectUrl;
        String url = TMConfigs.App.TAOBAO_AUTH_URL;
        try {
            url += "&response_type=" + response_type + "&redirect_uri=" + URLEncoder.encode(redirect_uri, "UTF-8")
                    + "&scope=" + scope + "&view=" + view + "&state=" + URLEncoder.encode(state, "UTF-8");
            log.info("[url:]" + url);
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
        }
        redirect(url);
    }

    public static void reAuthCallback(String code, String state) {
        try {
            log.info("Reauth code:" + code + ",state:" + state);
            String appkey = TMConfigs.App.APP_KEY;
            String client_id = appkey;
            String client_secret = TMConfigs.App.APP_SECRET;
            String grant_type = "authorization_code";
            String redirect_uri = JinNangZheKou.DOMAIN + state;// 应用回调地址
            String scope = "item";
            String view = "web";
            // 用post方法
            Map param = new HashMap();
            String tbPostSessionUrl = "https://oauth.taobao.com/token";// access_token获取地址
            param.put("grant_type", "authorization_code");
            param.put("code", code);
            param.put("client_id", appkey);
            param.put("client_secret", client_secret);
            param.put("redirect_uri", redirect_uri);
            param.put("scope", "item");
            param.put("view", "web");
            param.put("state", state);
            String responseJsonString;
            responseJsonString = WebUtils.doPost(tbPostSessionUrl, param, 3000, 3000);
            log.info("Got resonse json from taobao; " + responseJsonString);
            JsonNode readJsonResult = JsonUtil.readJsonResult(responseJsonString);
            models.user.User user = (models.user.User) request.args.get(WebParams.ARGS_USER);
            if (user == null) {
                log.error("re auth got null user from WebParams.ARGS_USER");
            }
            String userId = readJsonResult.get("taobao_user_id").getTextValue();
            //user = models.user.User.findById(Long.parseLong(userId));
            user = UserDao.findById(Long.parseLong(userId));
            if (user == null) {
                log.error("re auth got null user from user id:" + userId);
                return;
            }
            String newSessionKey = readJsonResult.get("access_token").getTextValue();
            String refreshToken = readJsonResult.get("refresh_token").getTextValue();
            user.sessionKey = newSessionKey;
            user.setRefreshToken(refreshToken);
            user.jdbcSave();

            session.put(WebParams.SESSION_USER_KEY, String.valueOf(user.getSessionKey()));
            log.info("updated user session key:" + user.getSessionKey());
            String finalRedirectUrl = state;
            log.info("finalRedirectUrl is " + finalRedirectUrl);
            redirect(finalRedirectUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final Logger log = LoggerFactory.getLogger(In.class);

    public static final String TAG = "In";

    static boolean forwardToAli = "true".equals(Play.configuration.get("enable.forwardtoali"));

    /**
     * 先验证W2 是不是到期，不是，那么继续 进行改价等操作,如果已经到期，那么提示用户，先去短授权一下，
     * windows.location.href="https://oauth.taobao.com/authorize?response_type=code&client_id=<appkey>&redirect_uri=http://121.196.128.189/temp?href=xxxxx"
     * @param href
     * @param code
     */
    public static void temp(String href, String code) {
        // TODO verify the code... get new session key and  access token...
        // redirect(href);

        models.user.User oauthUser = TaobaoUtil.token(code, "");

        redirect(href);
    }

	/**
	 * 店群授权
	 */
	public static void authDQ(String code, String state) {
		if (StringUtils.isEmpty(state)) {
			state = "";
		}
		
		models.user.User user = null;
		
		if (!StringUtils.isEmpty(code)) {
			user = TaobaoUtil.token(code, "");
		}
		
		render("/dianqun/dqIndex.html", user);
	}
	
    /**
     * 先验证W2 是不是到期，不是，那么继续 进行改价等操作,如果已经到期，那么提示用户，先去短授权一下，
     * windows.location.href="https://oauth.taobao.com/authorize?response_type=code&client_id=<appkey>&redirect_uri=http://121.196.128.189/in/authPrice"
     * @param href
     * @param code
     */
    public static void authPrice(String code, String state) {
        if (StringUtils.isEmpty(state)) {
            state = "";
        }

        if (!StringUtils.isEmpty(code)) {
            models.user.User oauthUser = TaobaoUtil.token(code, "");
        }

        redirect("/skinbatch/batchPrice");
    }
    
    public static void authDazhe(String code, String state) {
        if (StringUtils.isEmpty(state)) {
            state = "";
        }

        if (!StringUtils.isEmpty(code)) {
            models.user.User oauthUser = TaobaoUtil.token(code, "");
        }

        redirect("/umpactivity/closePage");
    }
    

    /**
     * 先验证W2 是不是到期，不是，那么继续 进行改价等操作,如果已经到期，那么提示用户，先去短授权一下，
     * windows.location.href="https://oauth.taobao.com/authorize?response_type=code&client_id=<appkey>&redirect_uri=http://121.196.128.189/in/authPrice"
     * @param href
     * @param code
     */
    public static void Tdiscount(String code, String state) {
        
        log.info(format("Tdiscount:code, state".replaceAll(", ", "=%s, ") + "=%s", code, state));

        if (StringUtils.isEmpty(state)) {
            state = ""; // or reAuth ?
        }

        if (StringUtils.isEmpty(code)) {
            log.error("token: code empty!!!!!!!!!!!!!!!!!!!");
            redirect(state);
        }
        models.user.User oauthUser = TaobaoUtil.token(code, state);
        if(oauthUser == null) {
        	redirect(state);
        }
        if (state.indexOf("?") >= 0) {
            state = state + "&sid=" + oauthUser.getSessionKey();
        } else {
            state = state + "?sid=" + oauthUser.getSessionKey();
        }

        redirect(state);
    }

    /**
     * url:/in/login?code=mcKkAH9x0uW7vLH7n1yw7AV74296911&
     * state=scope%3Ar1%2Cr2%2Cw1%2Cw2%3Bsign%3ABBF4287C895B7A9ED27B97146DC6CCD1%3BleaseId%3A0%3Btimestamp%3A1370284488490%3BversionNo%3A6%3Bouter_trade_code%3A%3BitemCode%3Ats-1820059-3
     * @param top_appkey
     * @param top_parameters
     * @param top_session
     * @param top_sign
     */
	public static void login(String top_appkey, String top_parameters, String top_session, String top_sign,
			String code, String state, String user_id, String from, String sessionkey, String nick) {
//    	if(!StringUtils.isEmpty(from) && !StringUtils.isEmpty(user_id)) {
//    		Long userId = Long.valueOf(user_id);
//    		if(userId == null || userId <= 0L) {
//    			redirect(App.CONTAINER_TAOBAO_URL);
//    		}
//    		models.user.User user = UserDao.findById(userId);
//    		if(user == null) {
//    			redirect(App.CONTAINER_TAOBAO_URL);
//    		}
//    		APIConfig.get().afterLogin(user, null, false, true);
//    	}
		// 测试千牛回调  user_id from sessionkey nick
		if(!StringUtils.isEmpty(from) && !StringUtils.isEmpty(user_id)) {
			log.info("qianNiuLoginInfo: userId=" + user_id + " sid=" + sessionkey + " userNick=" + nick);
			models.user.User user = UserDao.findById(Long.parseLong(user_id));
			if (user == null) {
				if(nick.indexOf(":") >= 0) {
					log.info("qianNiuLoginInfo 子账号:" + nick);
					com.taobao.api.domain.User qianniuSubUser = new UserAPIs.UserGetApi(sessionkey, null).call(); // 返回的是主帐号
					if(qianniuSubUser == null) {
						log.error("【 local db no found user info 】");
						redirect(App.CONTAINER_TAOBAO_URL);
					} else {
						user = UserDao.findById(qianniuSubUser.getUserId());
						if(user == null) {
							user = UserDao.addUser(qianniuSubUser, qianniuSubUser.getNick(), qianniuSubUser.getUserId(), top_session, true, 0, "");
						} else {
							user.sessionKey = sessionkey;
							user.setSub(true);
							nick = qianniuSubUser.getNick();
						}
					}
				} else {
					log.error("【 local db no found user info 】");
					redirect(App.CONTAINER_TAOBAO_URL);
				}
			}
			log.info("【local db find user info 】: userNick" + user.getUserNick());
			// 检验sessionKey是否过期
			ShopGet shopGet = new ShopApi.ShopGet(nick);
			Shop shop = shopGet.call();
			if (shop == null) {
				log.error("no found user " + shopGet.getSubErrorCode());
				redirect(App.CONTAINER_TAOBAO_URL);
			}
			user.setSessionKey(sessionkey);
			TMController.clearUser();
			TMController.putUser(user);
			APIConfig.get().afterLogin(null, null, false, true);
		}
		
        log.info(format(
                "login:top_appkey, top_parameters, top_session, top_sign, code, status".replaceAll(", ", "=%s, ")
                        + "=%s", top_appkey, top_parameters, top_session, top_sign, code, state));

        Thread.currentThread().setName(TAG);
        log.warn("login url:" + request.url + " and from = " + from);
        if (!StringUtils.isBlank(code) && !StringUtils.isBlank(state)) {
            try {
                models.user.User oauthUser = TaobaoUtil.token(code, state);
                log.info("sub login  [oauth user:]" + oauthUser + " and isSub = " + oauthUser.isSub());
                if (oauthUser != null) {
                    TMController.clearUser();
                    log.info("sub login sid is " + oauthUser.getSessionKey());
                    TMController.putUser(oauthUser);
                    log.info("sub login session sid = " + session.get(WebParams.SESSION_USER_KEY));
					if(oauthUser.isSub()) {
						// 如果是子账号，直接跳转到页面
						log.info("sub login  do afterLogin for user " + oauthUser.getUserNick());
						 UserLoginInfoCache.get().doClearUser(oauthUser);
						APIConfig.get().afterLogin(oauthUser, null, oauthUser.isNew(), false);         		
                	}
//                    boolean isFirst = oauthUser.isNew();
//                    if (System.currentTimeMillis() - oauthUser.getFirstLoginTime().longValue() < DateUtil.TWO_DAY_MILLIS) {
                    doForBaseLogin(oauthUser);
//                    }
                    if (oauthUser.isNew()) {
                        UserLoginAction.basicInfoCheck(oauthUser, oauthUser.getUserNick());
                    }
                    APIConfig.get().afterLogin(oauthUser, null, oauthUser.isNew(), false);
                }
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }
        /**
         * 解析参数
         */
        Map<String, String> decodeParams = null;
        try {
            decodeParams = TaobaoUtils.decodeTopParams(top_parameters);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            log.info("Decode top params occure error !");
//            WebUtils.splitUrlQuery(
            try {
                decodeParams = WebUtils.splitUrlQuery(new String(Base64.decode(top_parameters)));
            } catch (Exception e1) {
                log.warn(e.getMessage(), e);
            }

            log.error("After ... retry use split url query:" + decodeParams);
//            redirect(App.TAOBAO_URL);
        }

        if (decodeParams == null) {
            /**
             * 跳转出错页面，可登录淘宝授权页面
             */
            redirect(App.CONTAINER_TAOBAO_URL);
        }

        String userNick = decodeParams.get("visitor_nick");
        Long userId = CommonUtils.String2Long(decodeParams.get("visitor_id"));
        String itemCode = decodeParams.get("itemCode");
        String refreshToken = decodeParams.get("refresh_token");
        log.info("[decode paramss : ]" + decodeParams);
        log.info("[item code : ]" + itemCode);
        if(!StringUtils.isEmpty(decodeParams.get("sub_taobao_user_id"))
        		|| !StringUtils.isEmpty(decodeParams.get("sub_taobao_user_nick"))) {
        	// 如果是子账号
        	log.info("sub login  find decodeParams sub user for " + decodeParams.get("sub_taobao_user_nick") +
        			" and session = " + top_session);
        	//String subNick = decodeParams.get("sub_taobao_user_nick");
        	//if(subNick.equals("楚之小南:绵羊")) {
        		if(userId == null || userId <= 0L) {
	        		log.info("sub login  decodeParams userId is null when in subbbb!!!");
	        	} else {
	        		models.user.User masterUser = UserDao.findById(userId);
		        	if(masterUser == null) {
		        		log.info("sub login decodeParams userId is "+userId+" but masterUser is null when in subbbb!!!");
		        	} else {
		        		// 设置子账号的session, 并跳转
			        	masterUser.setSessionKey(top_session);
			        	/*try {
							masterUser.setUserNick(URLDecoder.decode(decodeParams.get("sub_taobao_user_nick"), "gbk"));
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}*/
			        	TMController.clearUser();
			            TMController.putUser(masterUser);
			            APIConfig.get().afterLogin(masterUser, itemCode, false, false);
		        	}
	        	}
        	//}
        	

        }
        models.user.User tryFindUser = TMController.tryFindUser();

        if (tryFindUser != null && (userId == null || userId.longValue() == tryFindUser.getIdlong())) {
            User call = new UserAPIs.UserGetApi(tryFindUser.getSessionKey(), null).call();
            tryFindUser.setSub(false);
            if (call != null) {
                UserDao.updateIsVaildAndSessionKey(call, tryFindUser, true, top_session, refreshToken);
                TMController.clearUser();
                TMController.putUser(tryFindUser);
                //APIConfig.get().afterLogin(tryFindUser, itemCode, tryFindUser.isNew());
                APIConfig.get().afterLogin(tryFindUser, itemCode, false, false);
            }
        }

        if (!LoginAction.login(top_appkey, top_parameters, top_session, top_sign)) {
            /**
             * 跳转出错页面，可登录淘宝授权页面
             */
            redirect(App.CONTAINER_TAOBAO_URL);
        }

//        if (userNick == null || userNick == "" || userId == null) {
//            log.warn("参数解析有误!");
//            /**
//             * 跳转出错页面，可登录淘宝授权页面
//             */
//            redirect(App.TAOBAO_URL);
//        }

        /**
         * 重新获取userNick，登陆时解析的UserNick可能乱码
         */
        User tbUser = new UserAPIs.UserGetApi(top_session, null).call();
        log.info("[get tb user:]" + new Gson().toJson(tbUser));

        if (tbUser != null) {
            userNick = tbUser.getNick();
            userId = tbUser.getUserId();
        } else {
            log.error("No user got!!!");
        }

        log.info(String.format("User login:userNick [%s],userId:[%s], session [%s]", userNick, userId, top_session));

//        boolean isUserInfoSyncNeeded = false;

        /**
         * 如果用户存在，更新sessionKey及isVaild
         */
        models.user.User user = UserDao.findById(userId);

        if (user != null) {
            log.info("[Update Old User]" + user);
            user = UserDao.updateIsVaildAndSessionKey(tbUser, user, true, top_session, refreshToken);
        } else {
//            Shop tbShop = new ShopApi.ShopGet(userNick).call();
            user = UserDao.addUser(tbUser, userNick, userId, top_session, true, 0, refreshToken);
            if (TMConfigs.App.IS_TRADE_ALLOW) {
                SellerMobile.ensure(user);
            }
//            isUserInfoSyncNeeded = true;
            log.info("[Create New User]" + user);
        }

//        UserAction.updateUser(user);
        TMController.putUser(user);
        if (user.isNew()) {
            log.warn("Before Check:" + user);
            UserLoginAction.basicInfoCheck(user, user.getUserNick());
        }

        doForBaseLogin(user);

        APIConfig.get().afterLogin(user, itemCode, user.isNew(), false);
    }

    private static void doForBaseLogin(models.user.User user) {
        log.info("[check invite:]" + user);
        Op.checkInvite();
        UserLoginInfoCache.get().doClearUser(user);
        new UserLoginLog(user, NetworkUtil.getRemoteIPForNginx(request)).save();
        UserLoginIpJob.addUserIp(user.getId(), NetworkUtil.getRemoteIPForNginx(request));
        UserLoginAction.doAfterLogin(user);
    }

    public static void logout() {
        TMController.clearUser();
        renderText(App.CONTAINER_TAOBAO_URL);
    }

    public static void immediateXuanciDiag() {
    	render("diag/immediateXuanciDiag.html");
    }

    public static void doDiag() {
        render("diag/listdiag.html");
    }

    public static void goOptimise() {
//        String templateName = APIConfig.get().goOptimiseTemplateName();
//        if ("diag/buyxuanci.html".equals(templateName)) {
        render("diag/buyxuanci.html");
//
//        } else if ("diag/buytaobiaoti.html".equals(templateName)) {
//            render("diag/buytaobiaoti.html");
//        }
//        render("diag/buytaobiaoti.html");
    }

    public static void diagnoseShop() {
        render("diagnoseshop/diagnoseshop.html");
    }

    public static void forward(long uid) {
        long ts = System.currentTimeMillis();
        response.setCookie(Op.INVITE_COOKIE_FROMUID, String.valueOf(uid), "2d");
        response.setCookie(Op.INVITE_COOKIE_CREATED, String.valueOf(ts), "2d");

        Op.log.info("[Invite] invite record: uid = " + uid);

        String ip = NetworkUtil.getRemoteIPForNginx(request);
        Op.log.error("ip :" + ip);

        redirect(APIConfig.get().getServiceUrl());
    }

    public static void img() {
        String ip = NetworkUtil.getRemoteIPForNginx(request);
        log.warn("img :::  ip :" + ip);

//        MixHelpers.infoAll(request, response);
        Header header = request.headers.get("referer");
        if (header == null) {
            header = request.headers.get("Referer");
        }
        if (header == null) {
            header = request.headers.get("Refer");
        }
        if (header == null) {
            header = request.headers.get("refer");
        }
        log.warn("find header:" + header);

        if (header == null) {
            TMController.renderEmptyImg();
        }
        String referer = header.value();
        if (StringUtils.isBlank(referer)) {
            TMController.renderEmptyImg();
        }

        String target = "?";
        int paramIndex = referer.indexOf(target);
        if (paramIndex <= 0) {
            TMController.renderEmptyImg();
        }
        String subParams = referer.substring(paramIndex + 1);
        Map<String, String[]> urlParams = UrlEncodedParser.parse(subParams);

        String inviteStr = TMController.getParam(urlParams, "_i");
        String tracelog = TMController.getParam(urlParams, "tracelog");
        log.info("invite str:" + inviteStr);
        log.info("trace str:" + tracelog);

        if (StringUtils.isBlank(subParams)) {
            TMController.renderEmptyImg();
        }

        if (NumberUtils.isNumber(inviteStr)) {
            long uid = Long.parseLong(inviteStr);
            log.info("[Invite] invite record: uid = " + uid);
            if (uid > 0L) {
                response.setCookie(Op.INVITE_COOKIE_FROMUID, String.valueOf(uid), "2d");
            }
        }

        if (!StringUtils.isEmpty(tracelog)) {
            response.setCookie(Op.INVITE_COOKIE_TRACELOG, tracelog, "2d");
        }

        long ts = System.currentTimeMillis();
        response.setCookie(Op.INVITE_COOKIE_CREATED, String.valueOf(ts), "2d");

        TraceLogClick.click(tracelog, ip);

        TMController.renderEmptyImg();
    }

    public static void bdsitemap() {
        renderText("5JfGANkMpaFmjaQC");
    }

    public static void robot() {
        renderText("User-Agent: Baiduspider\nAllow: /");
    }

    public static void home() {
        APIConfig.get().beforeLogin();
        TMController.check();
        models.user.User user = TMController.getUser();
        APIConfig.get().afterLogin(user, null, false, false);
    }

    public static void tblmforward(String uname) {
        long ts = System.currentTimeMillis();
        response.setCookie(Op.INVITE_COOKIE_FROMUNAME, uname, "2d");
        response.setCookie(Op.INVITE_COOKIE_CREATED, String.valueOf(ts), "2d");

        Op.log.info("[Invite] invite record: uid = " + uname);

        String ip = NetworkUtil.getRemoteIPForNginx(request);
        Op.log.error("ip :" + ip);

        TMSearch.comment();
    }

    public static void dostop(String wangwang) {
        if (StringUtils.isEmpty(wangwang)) {
            return;
        }
        wangwang = StringUtils.trim(wangwang);
        models.user.User user = UserDao.findByUserNick(wangwang);
        if (user == null) {
            return;
        }
        user.setPopularOff(false);
        user.setVaild(false);
    }

    static String[] hosts = new String[] {
            "http://www.taovgo.com", "http://g.tobti.com", "http://www.btuiguang.com"
    };

    public static void setstop(String wangwang) {
        if (StringUtils.isEmpty(wangwang)) {
            return;
        }
        wangwang = StringUtils.trim(wangwang);
        for (String host : hosts) {
            WS.url(host + "/in/dostop").setParameter("wangwang", wangwang).postAsync();
        }
    }

    // 旺客掌柜
    public static void wangkezhangui() {
        render("/wangke/zhanggui.html", "旺客掌柜");
    }

    // 旺客标题
    public static void wangkebiaoti() {
        render("/wangke/biaoti.html");
    }

    // 旺客推广
    public static void wangketuiguang() {
        render("/wangke/tuiguang.html");
    }

    // 旺客收藏
    public static void wangkeshoucang() {
        render("/wangke/shoucang.html");
    }

    // 旺客分享
    public static void wangkefenxiang() {
        render("/wangke/fenxiang.html");
    }

    // 旺客描述
    public static void wangkemiaoshu() {
        render("/wangke/miaoshu.html");
    }

    // 旺客促销
    public static void wangkecuxiao() {
        render("/wangke/cuxiao.html");
    }

    // 旺客联盟
    public static void wangkelianmeng() {
        render("/wangke/lianmeng.html");
    }

    // 旺客帮
    public static void wangkebang() {
        render("/wangke/wangkebang.html");
    }

    // 旺客进销存
    public static void wangkejinxiaocun() {
        render("/wangke/jinxiaocun.html");
    }

    // 旺客关联
    public static void wangkeguanlian() {
        render("/wangke/guanlian.html");
    }

    // 网店推广
    public static void wangdiantuiguang() {
        render("/wangke/shoptuiguang.html");
    }

    // 客服绩效
    public static void kefujixiao() {
        render("/wangke/kefujixiao.html");
    }

    // 旺客SNS
    public static void wangkesns() {
        render("/wangke/wangkeSNS.html");
    }

    // 旺客SEO
    public static void wangkeseo() {
        render("/wangke/wangkeSEO.html");
    }

    // 旺客CRM
    public static void wangkecrm() {
        render("/wangke/wangkeCRM.html");
    }

    // 旺客好评
    public static void wangkehaoping() {
        render("/wangke/wangkehaoping.html");
    }

    /**
     * 京东api的入口
     */
    public static void jd(String code, String state) {

        JDUser user = JDAction.get().doLoginByCode(code);
        JDController.putUser(user);
        if (user != null) {
            new JDItemUpdateJob(user).now();
        }

        JDTuiguang.index();
    }

    public static void kuaiche(String code, String state) {
        JDUser user = JDAction.get().doLoginByCode(code);
        JDController.putUser(user);
        if (user != null) {
            new JDItemUpdateJob(user).now();
        }

        JDTuiguang.index();
    }

    /**
     * QQ api entry...
     * http://127.0.0.1:9000/in/qq?access_token=cfb7eeb66e434c94f10d8c933af5ab9d&useruin=721719&app_oauth_id=700132927&sign=L8EI8zEkqpxA4OBJf2576kxK%2Bzg%3D
     */
    public static void qq(long useruin, String access_token, String app_oauth_id, String sign) {
        PaiPaiUser user = PaiPaiAction.get().checkLogin(useruin, access_token);
        PaiPaiController.putUser(user);
        if (user != null) {
            new PaiPaiItemUpdateJob(user.getId()).now();
        }

        PaiPaiAPIConfig.get().afterLogin(null, StringUtils.EMPTY, false, false);
    }

    /**
     * ppdazhe api entry...
     * http://127.0.0.1:9000/in/qq?access_token=cfb7eeb66e434c94f10d8c933af5ab9d&useruin=721719&app_oauth_id=700132314&sign=L8EI8zEkqpxA4OBJf2576kxK%2Bzg%3D
     */
    public static void ppdazhe(long useruin, String access_token, String app_oauth_id, String sign) {
        PaiPaiUser user = PaiPaiAction.get().checkLogin(useruin, access_token);
        PaiPaiController.putUser(user);
        if (user != null) {
            new PaiPaiItemUpdateJob(user.getId()).now();
        }

        PaiPaiAPIConfig.get().afterLogin(null, StringUtils.EMPTY, false, false);
    }

    /**
     * http://www.32133.com/?openid=4DCF507B9913359A55D79231F2C0B762&openkey=2965E1900CD5F2C4F8BFEDA2691E663D&pf=qzone&pfkey=bb3533e092ef102a5967023eed42570a
     * @param openid
     * @param openkey
     * @param pf
     * @param pfkey
     */
    public static void q(String openid, String openkey, String pf, String pfkey) {
        renderText(openid);
    }

    public static void api() {
        renderText(APIConfig.get().toString());
    }

    public static void clorest510() {
        log.info("[appkey :]" + APIConfig.get().getApp());
        if (APIConfig.get().getApp() == APIConfig.dazhe.getApp()) {
            models.user.User user = UserDao.findByUserNick("clorest510");
            TMController.putUser(user);
            APIConfig.get().afterLogin(user, null, false, false);
        }

    }
    
    public static void for1688Title(){
    	String code=request.params.get("code");
    	 String appKey = "9854521";
         String secKey = "eqFlaaWo1d";
         
//         ApiExecutor executor=new ApiExecutor(appKey, secKey);
    	
    	String url=String.format("https://gw.open.1688.com/openapi/http/1/system.oauth2/getToken/%s?grant_type=authorization_code&need_refresh_token=true&client_id=%s&client_secret=%s&redirect_uri=http://localhost:9000/in/for1688&code=%s", appKey,appKey,secKey,code);
    	String resultString=CommonUtil.sendPost(url, "");
    	String access_token=StringUtils.EMPTY;
    	String refresh_token=StringUtils.EMPTY;
 		try {
 			JSONObject resultJo= new JSONObject(resultString);
 			access_token=resultJo.getString("access_token");
 			refresh_token=resultJo.getString("refresh_token");
 			APiConfig1688 config=new APiConfig1688(appKey, secKey, refresh_token, access_token,100,ApiConfig1688Status.NORMAL);
 			config.jdbcSave();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		AlibabaAgentProductGetParam getParam=new AlibabaAgentProductGetParam();
//		getParam.setProductID(533026912875L);
//		getParam.setWebSite("1688");
//		
//		AlibabaAgentProductGetResult result=executor.execute(getParam,access_token);
//		
//		long cid=result.getProductInfo().getCategoryID();
    
    	render("carrier/test.html");
    }
    
    public static void for1688Templete(){
    	String code=request.params.get("code");
    	 String appKey = "3865211";
         String secKey = "5rHXgKt6ebw";
         
         String url=String.format("https://gw.open.1688.com/openapi/http/1/system.oauth2/getToken/%s?grant_type=authorization_code&need_refresh_token=true&client_id=%s&client_secret=%s&redirect_uri=http://localhost:9000/in/for1688&code=%s", appKey,appKey,secKey,code);
     	String resultString=CommonUtil.sendPost(url, "");
     	String access_token=StringUtils.EMPTY;
     	String refresh_token=StringUtils.EMPTY;
 		try {
 			JSONObject resultJo= new JSONObject(resultString);
 			access_token=resultJo.getString("access_token");
 			refresh_token=resultJo.getString("refresh_token");
 			APiConfig1688 config=new APiConfig1688(appKey, secKey, refresh_token, access_token,100,ApiConfig1688Status.NORMAL);
 			config.jdbcSave();
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		
     	render("carrier/test.html");
    }
    
    //美店
    public static void for1688Meidian(){
    	String code=request.params.get("code");
    	 String appKey = "5580640";
         String secKey = "0vFNAYB52T";
         
         String url=String.format("https://gw.open.1688.com/openapi/http/1/system.oauth2/getToken/%s?grant_type=authorization_code&need_refresh_token=true&client_id=%s&client_secret=%s&redirect_uri=http://localhost:9000/in/for1688&code=%s", appKey,appKey,secKey,code);
     	String resultString=CommonUtil.sendPost(url, "");
     	String access_token=StringUtils.EMPTY;
     	String refresh_token=StringUtils.EMPTY;
 		try {
 			JSONObject resultJo= new JSONObject(resultString);
 			access_token=resultJo.getString("access_token");
 			refresh_token=resultJo.getString("refresh_token");
 			APiConfig1688 config=new APiConfig1688(appKey, secKey, refresh_token, access_token,100,ApiConfig1688Status.NORMAL);
 			config.jdbcSave();
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		
     	render("carrier/test.html");
    }
    
    //流量推广
    public static void for1688Uv(){
    	String code=request.params.get("code");
    	 String appKey = "8373584";
         String secKey = "OJwIKQv26yu";
         
         String url=String.format("https://gw.open.1688.com/openapi/http/1/system.oauth2/getToken/%s?grant_type=authorization_code&need_refresh_token=true&client_id=%s&client_secret=%s&redirect_uri=http://localhost:9000/in/for1688&code=%s", appKey,appKey,secKey,code);
     	String resultString=CommonUtil.sendPost(url, "");
     	String access_token=StringUtils.EMPTY;
     	String refresh_token=StringUtils.EMPTY;
 		try {
 			JSONObject resultJo= new JSONObject(resultString);
 			access_token=resultJo.getString("access_token");
 			refresh_token=resultJo.getString("refresh_token");
 			APiConfig1688 config=new APiConfig1688(appKey, secKey, refresh_token, access_token,100,ApiConfig1688Status.NORMAL);
 			config.jdbcSave();
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		
     	render("carrier/test.html");
    }
    
  //自动橱窗
    public static void for1688AutoWindow(){
    	String code=request.params.get("code");
    	 String appKey = "2465734";
         String secKey = "mQJKMPt8Z9pm";
         
         String url=String.format("https://gw.open.1688.com/openapi/http/1/system.oauth2/getToken/%s?grant_type=authorization_code&need_refresh_token=true&client_id=%s&client_secret=%s&redirect_uri=http://localhost:9000/in/for1688&code=%s", appKey,appKey,secKey,code);
     	String resultString=CommonUtil.sendPost(url, "");
     	String access_token=StringUtils.EMPTY;
     	String refresh_token=StringUtils.EMPTY;
 		try {
 			JSONObject resultJo= new JSONObject(resultString);
 			access_token=resultJo.getString("access_token");
 			refresh_token=resultJo.getString("refresh_token");
 			APiConfig1688 config=new APiConfig1688(appKey, secKey, refresh_token, access_token,100,ApiConfig1688Status.NORMAL);
 			config.jdbcSave();
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		
     	render("carrier/test.html");
    }
    
  //包邮打折
    public static void for1688Discount(){
    	String code=request.params.get("code");
    	 String appKey = "2604252";
         String secKey = "nsBKcTH7rxe0";
         
         String url=String.format("https://gw.open.1688.com/openapi/http/1/system.oauth2/getToken/%s?grant_type=authorization_code&need_refresh_token=true&client_id=%s&client_secret=%s&redirect_uri=http://localhost:9000/in/for1688&code=%s", appKey,appKey,secKey,code);
     	String resultString=CommonUtil.sendPost(url, "");
     	String access_token=StringUtils.EMPTY;
     	String refresh_token=StringUtils.EMPTY;
 		try {
 			JSONObject resultJo= new JSONObject(resultString);
 			access_token=resultJo.getString("access_token");
 			refresh_token=resultJo.getString("refresh_token");
 			APiConfig1688 config=new APiConfig1688(appKey, secKey, refresh_token, access_token,100,ApiConfig1688Status.NORMAL);
 			config.jdbcSave();
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		
     	render("carrier/test.html");
    }
    
  //1688crm
    public static void for1688Crm(){
    	String code=request.params.get("code");
    	 String appKey = "5287093";
         String secKey = "ixlRIguBjKq";
         
         String url=String.format("https://gw.open.1688.com/openapi/http/1/system.oauth2/getToken/%s?grant_type=authorization_code&need_refresh_token=true&client_id=%s&client_secret=%s&redirect_uri=http://localhost:9000/in/for1688&code=%s", appKey,appKey,secKey,code);
     	String resultString=CommonUtil.sendPost(url, "");
     	String access_token=StringUtils.EMPTY;
     	String refresh_token=StringUtils.EMPTY;
 		try {
 			JSONObject resultJo= new JSONObject(resultString);
 			access_token=resultJo.getString("access_token");
 			refresh_token=resultJo.getString("refresh_token");
 			APiConfig1688 config=new APiConfig1688(appKey, secKey, refresh_token, access_token,100,ApiConfig1688Status.NORMAL);
 			config.jdbcSave();
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		
     	render("carrier/test.html");
    }
    
  //数据分析
    public static void for1688Data(){
    	String code=request.params.get("code");
    	 String appKey = "3800274";
         String secKey = "e1wBMPhGdHap";
         
         String url=String.format("https://gw.open.1688.com/openapi/http/1/system.oauth2/getToken/%s?grant_type=authorization_code&need_refresh_token=true&client_id=%s&client_secret=%s&redirect_uri=http://localhost:9000/in/for1688&code=%s", appKey,appKey,secKey,code);
     	String resultString=CommonUtil.sendPost(url, "");
     	String access_token=StringUtils.EMPTY;
     	String refresh_token=StringUtils.EMPTY;
 		try {
 			JSONObject resultJo= new JSONObject(resultString);
 			access_token=resultJo.getString("access_token");
 			refresh_token=resultJo.getString("refresh_token");
 			APiConfig1688 config=new APiConfig1688(appKey, secKey, refresh_token, access_token,100,ApiConfig1688Status.NORMAL);
 			config.jdbcSave();
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		
     	render("carrier/test.html");
    }
    
  //包邮打折
    public static void for1688Order(){
    	String code=request.params.get("code");
    	 String appKey = "9839631";
         String secKey = "etiGsAzUdujV";
         
         String url=String.format("https://gw.open.1688.com/openapi/http/1/system.oauth2/getToken/%s?grant_type=authorization_code&need_refresh_token=true&client_id=%s&client_secret=%s&redirect_uri=http://localhost:9000/in/for1688&code=%s", appKey,appKey,secKey,code);
     	String resultString=CommonUtil.sendPost(url, "");
     	String access_token=StringUtils.EMPTY;
     	String refresh_token=StringUtils.EMPTY;
 		try {
 			JSONObject resultJo= new JSONObject(resultString);
 			access_token=resultJo.getString("access_token");
 			refresh_token=resultJo.getString("refresh_token");
 			APiConfig1688 config=new APiConfig1688(appKey, secKey, refresh_token, access_token,100,ApiConfig1688Status.NORMAL);
 			config.jdbcSave();
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		
     	render("carrier/test.html");
    }
    
    public static void for1688Evaluation(){
    	String code=request.params.get("code");
    	 String appKey = "2325405";
         String secKey = "y6dVv6wMPK";
         
         String url=String.format("https://gw.open.1688.com/openapi/http/1/system.oauth2/getToken/%s?grant_type=authorization_code&need_refresh_token=true&client_id=%s&client_secret=%s&redirect_uri=http://localhost:9000/in/for1688&code=%s", appKey,appKey,secKey,code);
     	String resultString=CommonUtil.sendPost(url, "");
     	String access_token=StringUtils.EMPTY;
     	String refresh_token=StringUtils.EMPTY;
 		try {
 			JSONObject resultJo= new JSONObject(resultString);
 			access_token=resultJo.getString("access_token");
 			refresh_token=resultJo.getString("refresh_token");
 			APiConfig1688 config=new APiConfig1688(appKey, secKey, refresh_token, access_token,100,ApiConfig1688Status.NORMAL);
 			config.jdbcSave();
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		
     	render("carrier/test.html");
    }
    
    public static void test(){
    	String itemId=request.params.get("itemId");
    	
    	APiConfig1688 config=APiConfig1688.getValidApp();
    	
    	ApiExecutor executor=new ApiExecutor(config.getAppkey(),config.getAppSecret());
//    	
//    	executor.
//    	
    	AlibabaAgentProductGetParam param=new AlibabaAgentProductGetParam();
    	param.setWebSite("1688");
    	param.setProductID(Long.parseLong(itemId));
    	AlibabaAgentProductGetResult result= executor.execute(param,config.getAccessToken());
    	
    	renderJSON(result);
    	
    }
}
