package controllers;

import java.util.List;

import models.jd.JDUser;
import models.popularized.Popularized;
import models.popularized.Popularized.PopularizedStatus;

import org.apache.commons.lang.StringUtils;

import play.libs.Crypto;
import play.mvc.Controller;
import result.TMResult;
import actions.shopping.RandomShareAction;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.NumberUtil;

import configs.TMConfigs.WebParams;
import dao.popularized.PopularizedDao;
import dao.popularized.PopularizedDao.PopularizedStatusSqlUtil;
import dao.popularized.ShoppingDao;

public class JDPromoteSite extends Controller {

    /**
     * 推广网站
     */
    public static void weigou() {
        goIndex();
    }

    static void goIndex() {
        render("jdtuiguangsite/itemlist.html");
    }

    public static void detail() {
        render("jdtuiguangsite/itemdetail.html");
    }

    /**
     * 爱推广
     */
    public static void aituiguang() {
        goTuiguangIndex();
    }

    static void goTuiguangIndex() {
        render("jdtuiguangsite/shopping.html");
    }
    
    public static final String sql = PopularizedDao.SelectPopularizedSql + " 1=1 order by rand() limit 1";
    
    public static void view() {
        Popularized pop = PopularizedDao.queryByJDBC(sql);
        
        redirect("http://item.jd.com/" + pop.getNumIid() + ".html");
    }

    public static void randomUserItems(Long numIid, Long topCatId, String title, int status) {
        topCatId = 10L;
        
        JDUser pUser = null;

        String enscryptUserId = CommonUtils.getCookieString(request, WebParams.SESSION_USER_ID);
        if (!StringUtils.isBlank(enscryptUserId)) {
            Long userId = NumberUtil.parserLong(Crypto.decryptAES(enscryptUserId), 0L);
            pUser = JDUser.findByUserId(userId);
        }

        status = PopularizedStatusSqlUtil.checkStatus(status);

        Long userId = null;
//        if (pUser != null) {
//            userId = pUser.getId();
//        }
        
        List<Popularized> items = RandomShareAction.randomWithUser(numIid, userId, topCatId, title, status);

        renderSuccess(items);

    }

    public static void randomItems(Long topCatId, String title, int status) {
        status = PopularizedStatusSqlUtil.checkStatus(status);

        List<Popularized> items = RandomShareAction.randomWithTitle(title, topCatId, status);

        renderSuccess(items);
    }

    public static void queryPopularized(Long userId, Long numIid) {
        if (numIid == null || numIid <= 0) {
            renderError("系统出现错误啦~~");
        }
        Popularized item = ShoppingDao.findByNumIid(numIid, "", "", PopularizedStatus.Normal
                + PopularizedStatus.HotSale);
        if (item == null) {
            renderError("找不到对应的宝贝~~");
        }

//        String itemCode = PaiNumIidToItemCode.fetchItemCode(numIid);
//        if (StringUtils.isEmpty(itemCode)) {
//            renderError("系统出现错误啦~~");
//        }

//        item.setItemCode(itemCode);
        
//        JDItemPlay item = JDItemDao.queryJDItem(userId, numIid);
        
        renderSuccess(item);
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
