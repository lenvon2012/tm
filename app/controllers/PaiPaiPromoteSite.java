package controllers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import models.paipai.PaiNumIidToItemCode;
import models.paipai.PaiPaiUser;
import models.popularized.Popularized;
import models.popularized.Popularized.PopularizedStatus;

import org.apache.commons.lang.StringUtils;

import play.mvc.Controller;
import result.TMResult;
import actions.shopping.RandomShareAction;

import com.ciaosir.client.utils.JsonUtil;

import dao.popularized.PopularizedDao;
import dao.popularized.PopularizedDao.PopularizedStatusSqlUtil;
import dao.popularized.ShoppingDao;

public class PaiPaiPromoteSite extends Controller {

    /**
     * 推广网站
     */
    public static void weigou() {
        goIndex();
    }

    static void goIndex() {
        if (APIConfig.get().getApp() == PaiPaiAPIConfig.paipaiweigou.getApp()) {
            render("paipaiweigousite/itemlist.html");
        } else if (APIConfig.get().getApp() == PaiPaiAPIConfig.paipailetuiguang.getApp()) {
            render("paipailetuiguangsite/itemlist.html");
        } else {
            render("paipaiweigousite/itemlist.html");
        }
    }

    public static void detail() {
        if (APIConfig.get().getApp() == PaiPaiAPIConfig.paipaiweigou.getApp()) {
            render("paipaiweigousite/itemdetail.html");
        } else if (APIConfig.get().getApp() == PaiPaiAPIConfig.paipailetuiguang.getApp()) {
            render("paipailetuiguangsite/itemdetail.html");
        } else {
            render("paipaiweigousite/itemdetail.html");
        }
    }

    /**
     * 爱推广
     */
    public static void aituiguang() {
        goTuiguangIndex();
    }

    static void goTuiguangIndex() {
        if (APIConfig.get().getApp() == PaiPaiAPIConfig.paipaiweigou.getApp()) {
            render("paipaiweigousite/shopping.html");
        } else if (APIConfig.get().getApp() == PaiPaiAPIConfig.paipailetuiguang.getApp()) {
            render("paipailetuiguangsite/shopping.html");
        } else {
            render("paipaiweigousite/shopping.html");
        }
    }
    
    public static void apply() {
        render("paipaiweigousite/apply.html");
    }
    
    
    public static final String sql = PopularizedDao.SelectPopularizedSql + " 1=1 order by rand() limit 1";
    
    public static void goClick() {
        Popularized pop = PopularizedDao.queryByJDBC(sql);
        
        String code = PaiNumIidToItemCode.fetchItemCode(pop.getNumIid());
        
        redirect("http://item.wanggou.com/" + code);
    }

    public static void randomUserItems(Long numIid, Long topCatId, String title, int status) {

        topCatId = 10L;
        
        PaiPaiUser pUser = null;

//        String enscryptUserId = CommonUtils.getCookieString(request, WebParams.SESSION_USER_ID);
//        if (!StringUtils.isBlank(enscryptUserId)) {
//            Long userId = NumberUtil.parserLong(Crypto.decryptAES(enscryptUserId), 0L);
//            pUser = PaiPaiUser.findByUserId(userId);
//        }

        status = PopularizedStatusSqlUtil.checkStatus(status);

        Long userId = null;
//        if (pUser != null) {
//            userId = pUser.getId();
//        }

        List<Popularized> items = RandomShareAction.randomWithUser(numIid, userId, topCatId, title, status);

        HashSet<Long> numIids = new HashSet<Long>();
        for (Popularized item : items) {
            numIids.add(item.getNumIid());
        }
        
        HashMap<Long, String> itemCodeMap = PaiNumIidToItemCode.fetchItemCodeMap(numIids);
        for (Popularized item : items) {
            item.setItemCode(itemCodeMap.get(item.getNumIid()));
        }
        
        renderSuccess(items);

    }

    public static void randomItems(Long topCatId, String title, int status) {
        status = PopularizedStatusSqlUtil.checkStatus(status);

        List<Popularized> items = RandomShareAction.randomWithTitle(title, topCatId, status);

        for (Popularized item : items) {
            String itemCode = PaiNumIidToItemCode.fetchItemCode(item.getNumIid());
            item.setItemCode(itemCode);
        }

        renderSuccess(items);
    }

    public static void queryPopularized(Long numIid) {
        if (numIid == null || numIid <= 0) {
            renderError("系统出现错误啦~~");
        }
        Popularized item = ShoppingDao.findByNumIid(numIid, "", "", PopularizedStatus.Normal
                + PopularizedStatus.HotSale);
        if (item == null) {
            renderError("找不到对应的宝贝~~");
        }

        String itemCode = PaiNumIidToItemCode.fetchItemCode(numIid);
        if (StringUtils.isEmpty(itemCode)) {
            renderError("系统出现错误啦~~");
        }

        item.setItemCode(itemCode);
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
