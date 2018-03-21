package controllers;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import models.paipai.PaiPaiUser;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.libs.Crypto;
import play.mvc.After;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.results.Redirect;
import result.TMPaginger;
import result.TMResult;
import search.SearchManager;
import actions.paipai.PaiPaiAction;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.NumberUtil;

import configs.TMConfigs;
import configs.TMConfigs.App;
import configs.TMConfigs.WebParams;

public class PaiPaiController extends Controller {

    private static final Logger log = LoggerFactory.getLogger(PaiPaiController.class);

    public static final String TAG = "PaipaiController";

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
        PaiPaiUser pUser = null;

        String enscryptUserId = null;
        String sid = null;

        sid = params.get("_tms");
        if (StringUtils.isEmpty(sid)) {
            sid = params.get("sid");
        }
        if (StringUtils.isEmpty(sid)) {
            sid = session.get(WebParams.SESSION_USER_KEY);
        }
        if (!StringUtils.isEmpty(sid)) {
            pUser = PaiPaiUser.findBySessionKey(sid);
        }

        if (pUser == null) {
            enscryptUserId = CommonUtils.getCookieString(request, WebParams.COOKIE_ENCODE_USER_ID);
            if (!StringUtils.isBlank(enscryptUserId)) {
                log.info("[cookie : userId : ]" + enscryptUserId);
                Long userId = NumberUtil.parserLong(Crypto.decryptAES(enscryptUserId), 0L);
                pUser = PaiPaiUser.findByUserId(userId);
            }
        }

        // if (pUser == null) {
        // pUser = tryRenderMockPaiPaiUser();
        // }

        if (pUser == null) {
            // TODO try reset to some place...
            // redirect("");
            throw new Redirect(PaiPaiAction.get().buildRedir());
        } else {
            int version = PaiPaiAction.get().getVersion(pUser);
            pUser.setVersion(version);
            putUser(pUser);
            return;
        }

    }
    
    protected static Client getSClient() {
        return SearchManager.getIntance().getClient();
    }

    protected static void renderUISuccess() {
        renderJSON(JsonUtil.getJson(TMPaginger.makeEmptyOk()));
    }

    protected static void renderUIErrorMessage(String msg) {
        renderJSON(JsonUtil.getJson(TMPaginger.makeEmptyFail(msg)));
    }
    
    protected static void putUser(PaiPaiUser user) {
        // TODO Auto-generated method stub

        String enscryptId = Crypto.encryptAES(user.getId().toString());
        response.setCookie(WebParams.COOKIE_ENCODE_USER_ID, enscryptId, "120h");
        response.setCookie(WebParams.SESSION_USER_KEY, user.getAccessToken(), "120h");
        response.setCookie(WebParams.SESSION_USER_VERSION, String.valueOf(user.getVersion()), "120h");
        response.setCookie(WebParams.SESSION_USER_ID, enscryptId, "120h");
        
        request.args.put(WebParams.ARGS_USER, user);
        
        session.put(WebParams.SESSION_USER_KEY, user.getAccessToken());
        try {
            response.setCookie(WebParams.SESSION_USER_NICK, URLEncoder.encode(user.getNick(), "utf-8"), "120h");
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
        }
        log.info("[PaiPai User has been put..]" + user);
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

    protected static PaiPaiUser getUser() {
        PaiPaiUser user = (PaiPaiUser) request.args.get(WebParams.ARGS_USER);
        return user;
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

    static void renderMockFileInJsonIfDev(String filename) throws IOException {
        if (Play.mode.isProd() || !App.isDevMock) {
            return;
        }

        String str = FileUtils.readFileToString(new File(TMConfigs.mockDir, filename));
        // log.info("[str:]" + str);
        renderJSON(str);
    }

    static PaiPaiUser tryRenderMockPaiPaiUser() {
        if (Play.mode.isProd()) {
            return null;
        }

        // Now, it's dev mode...
        PaiPaiUser pUser = null;
        pUser = getUser();
        pUser = PaiPaiUser.findByUserId(1L);
        if (pUser != null) {
            putUser(pUser);
            return pUser;
        }

        pUser = new PaiPaiUser(1L, "testtoken");
        pUser.setNick("tester");
        pUser.setFirstLoginTime(System.currentTimeMillis());
        pUser.setRefreshToken("testrefreshtoken");
        pUser.save();
        putUser(pUser);
        return pUser;
    }
}
