
package controllers;

import models.jd.JDUser;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.libs.Crypto;
import play.mvc.After;
import play.mvc.Before;
import play.mvc.Controller;
import result.TMResult;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.NumberUtil;

import configs.TMConfigs.WebParams;

public class JDController extends Controller {

    private static final Logger log = LoggerFactory.getLogger(JDController.class);

    public static final String TAG = "JDController";

    @Before
    public static void startTime() {
        log.info("Request For " + request.url + ":" + request.action + " Starts");
        request.args.put("_ts", System.currentTimeMillis());
    }

    @After
    public static void endTime() {
        log.info("Action [" + request.url + "] took "
                + (System.currentTimeMillis() - (Long) request.current().args.get("_ts")) + " ms");
    }

    @Before
    protected static void check() {
        JDUser pUser = null;

        String enscryptUserId = null;
        String sid = null;

        enscryptUserId = CommonUtils.getCookieString(request, WebParams.SESSION_USER_ID);

        if (!StringUtils.isBlank(enscryptUserId)) {

            log.info("[cookie : userId : ]" + enscryptUserId);
            Long userId = NumberUtil.parserLong(Crypto.decryptAES(enscryptUserId), 0L);
            pUser = JDUser.findByUserId(userId);

        } else {
            sid = session.get(WebParams.SESSION_USER_KEY);
            if (StringUtils.isEmpty(sid)) {
                sid = params.get("_tms");
            }
            if (StringUtils.isEmpty(sid)) {
                sid = params.get("sid");
            }
            if (StringUtils.isEmpty(sid)) {
                sid = session.get(WebParams.SESSION_USER_KEY);
            }
            if (!StringUtils.isEmpty(sid)) {
                pUser = JDUser.findByAccessToken(sid);
            }
        }

        if (pUser == null) {
            pUser = tryRenderMockPaiPaiUser();
        }

        if (pUser == null) {
            // TODO try redirect to some place to get oauth
            //redirect("");
        } else {
            putUser(pUser);
            return;
        }
    }

    static JDUser tryRenderMockPaiPaiUser() {
        if (Play.mode.isProd()) {
            return null;
        }

        // Now, it's dev mode...
        JDUser pUser = null;
        pUser = getUser();
        pUser = JDUser.findByUserId(26883L);
        if (pUser != null) {
            putUser(pUser);
            return pUser;
        }

        pUser = new JDUser(1L, "testtoken", "testrefreshtoken");
        pUser.setNick("tester");
        pUser.setFirstLoginTime(System.currentTimeMillis());
        pUser.setRefreshToken("testrefreshtoken");
        pUser.jdbcSave();
        putUser(pUser);
        return pUser;
    }

    protected static JDUser getUser() {
        // TODO
        JDUser user = (JDUser) request.args.get(WebParams.ARGS_USER);
        return user;
    }

    protected static void putUser(JDUser user) {
        log.info("[put user:]" + user);
        if (user == null || user.getId() == null) {
            clearAndRedir();
        }
        // TODO Auto-generated method stub
        String enscryptId = Crypto.encryptAES(user.getId().toString());
        response.setCookie(WebParams.SESSION_USER_ID, enscryptId, "120h");
        request.args.put(WebParams.ARGS_USER, user);
        log.info("[JD User has been put..]" + user);
    }

    protected static void clearAndRedir() {
        response.removeCookie(WebParams.SESSION_USER_ID);
        redirect("http://www.jd.com");
    }
    
    static void clearUser() {
        // session.remove(Params.SESSION_USER_ID, Params.SESSION_USER_NICK, Params.SESSION_USER_LEVEL,
        // Params.SESSION_USER_KEY, Params.CREATED);
        response.removeCookie(WebParams.COOKIE_ENCODE_USER_ID);
        response.removeCookie(WebParams.SESSION_USER_KEY);
        request.args.remove(WebParams.ARGS_USER);
        session.remove(WebParams.SESSION_USER_ID, WebParams.SESSION_USER_NICK, WebParams.SESSION_USER_LEVEL,
                WebParams.SESSION_USER_KEY);
        // session.clear();
    }

    protected static void renderError(String message) {
        TMResult res = new TMResult(false, message, null);
        renderJSON(JsonUtil.getJson(res));
    }

    protected static void renderSuccess(String message) {
        TMResult res = new TMResult(true, message, null);
        renderJSON(JsonUtil.getJson(res));
    }

    protected static void renderJDJson(Object result) {
        TMResult res = new TMResult(true, "", result);
        renderJSON(JsonUtil.getJson(res));
    }

}
