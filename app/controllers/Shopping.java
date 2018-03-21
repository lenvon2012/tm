
package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import models.popularized.Popularized;
import models.popularized.Popularized.PopularizedStatus;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.mvc.Controller;
import result.TMResult;
import actions.shopping.RandomShareAction;

import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.NetworkUtil;

import configs.TMConfigs.WebParams;
import dao.UserDao;
import dao.popularized.PopularizedDao;
import dao.popularized.PopularizedDao.PopularizedStatusSqlUtil;
import dao.popularized.ShoppingDao;

public class Shopping extends Controller {
    private static final Logger log = LoggerFactory.getLogger(Shopping.class);

    public static void index() {
        render("shopping/shopping.html");
    }

    static void home() {
        render("shopping/shopping.html");
    }

    private static final int EachFetchNum = 20;

    public static void randomUserItems(Long numIid, Long topCatId, String title, int status) {
        

        status = PopularizedStatusSqlUtil.checkStatus(status);

        Long userId = getVisitUserId();
        
        List<Popularized> items = RandomShareAction.randomWithUser(numIid, userId, topCatId, title, status);

        renderSuccess(items);

    }
    
    static Long getVisitUserId() {
        User user = null;
        String sid = session.get(WebParams.SESSION_USER_KEY);
        if (StringUtils.isEmpty(sid)) {
            sid = params.get("sid");
        }

        if (!StringUtils.isEmpty(sid)) {
            user = UserDao.findBySessionKey(sid);
        }
        Long userId = 0L;
        if (user != null) {
            userId = user.getId();
        }
        
        return userId;
    }

    /*public static void randomItems(Long topCatId, String title, int status) {
        status = PopularizedStatusSqlUtil.checkStatus(status);

        List<Popularized> items = RandomShareAction.randomWithTitle(title, topCatId, status);

        renderSuccess(items);
    }*/

    public static void innerRandomItems(Long topCatId, String title, int status, String callback) {
    	List<Popularized> items = new ArrayList<Popularized>();
    	String ip = NetworkUtil.getRemoteIPForNginx(request);
    	
    	// 不是内网IP 就不可以用
    	if(StringUtils.isEmpty(ip) || !RandomShareAction.isInnerIP(ip)) {
    		renderJSON(RandomShareAction.jsonpFormat(items, callback));
    	}
        status = PopularizedStatusSqlUtil.checkStatus(status);

        items = RandomShareAction.randomWithTitle(title, topCatId, status);

        renderJSON(RandomShareAction.jsonpFormat(items, callback));
    }
    
    public static Random random = new Random();
    public static void innerRandomOne(Long userId) {
    	Popularized item = new Popularized();
    	String ip = NetworkUtil.getRemoteIPForNginx(request);
    	
    	// 不是内网IP 就不可以用
    	if(StringUtils.isEmpty(ip) || !RandomShareAction.isInnerIP(ip)) {
    		if(Play.mode.isProd()) {
    			renderError("只能内网IP可以调用");
    		}
    	}
        item = RandomShareAction.randomOne(userId);

        renderJSON(JsonUtil.getJson(item));
    }
    
    public static void getPreviousMaxId() {
    	renderText("allPreviousMaxId = " + RandomShareAction.allPreviousMaxId);
    }
    
    public static void queryPopularized(Long numIid) {
        if (numIid == null || numIid <= 0) {
            renderError("系统出现错误啦~~");
        }
        Popularized item = ShoppingDao.findByNumIid(numIid, "", "", PopularizedStatus.Normal + PopularizedStatus.HotSale);
        if (item == null) {
            renderError("找不到对应的宝贝~~");
        }

        renderSuccess(item);
    }

    public static void guessYoulike() {
    	Long count = PopularizedDao.countAllHot();
    	int start = (int) Math.floor(Math.random() * (count - 10));
    	List<Popularized> guesses = PopularizedDao.getRandomHotItems(start, 10);
    	renderJSON(JsonUtil.getJson(guesses));
    }
    
    protected static void renderSuccess(Object res) {
        TMResult tmRes = new TMResult(true, "", res);

        renderJSON(JsonUtil.getJson(tmRes));
    }

    protected static void renderError(String message) {
        TMResult tmRes = new TMResult(false, message, null);

        renderJSON(JsonUtil.getJson(tmRes));
    }

}
