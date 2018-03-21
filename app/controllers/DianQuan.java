package controllers;

import actions.dianquan.DianquanUtils;
import actions.dianquan.ShihuizhuDianquanService;
import models.dianquan.DianQuanItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.db.jpa.NoTransaction;
import play.libs.Codec;
import play.mvc.Controller;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author lyl
 * @date 2017/11/01
 */
public class DianQuan extends Controller {
    private static final Logger LOGGER = LoggerFactory.getLogger(DianQuan.class);

    @NoTransaction
    public static void index() {
        render("dianquan/index.html");
    }

    @NoTransaction
    public static void runShihuizhuDianquanJob() {
        boolean clearSuccess = DianQuanItem.clearExpiredDianquan();
        if (clearSuccess) {
            boolean success = new ShihuizhuDianquanService.BatchGetDianquanInfo().getInfo();
            if (success) {
                LOGGER.info("ShihuizhuDianquanJob Success, Time: {}", new Date());
            }
        }
    }

    @NoTransaction
    public static void searchDianquanList(
            int type, String activityFilters, String siteFilters, String typeFilters,
            BigDecimal lowPrice, BigDecimal highPrice, BigDecimal ratio, BigDecimal sales, String searchText,
            int curr, int pageSize, int order) {
        if (curr < 1) {
            curr = 1;
        }
        if (pageSize < 1) {
            pageSize = 15;
        }
        String result = DianquanUtils.getDianquanListJson(type, activityFilters, siteFilters, typeFilters, lowPrice, highPrice, ratio, sales, searchText, curr, pageSize, order);
        renderJSON(result);
    }

    @NoTransaction
    public static void getAllGid(int type, String activityFilters, String siteFilters, String typeFilters,
                                 BigDecimal lowPrice, BigDecimal highPrice, BigDecimal ratio, BigDecimal sales, String searchText) {
        String queryStrMd5 = Codec.hexMD5(request.querystring);
        String actionResult = (String) play.cache.Cache.get(queryStrMd5);
        if (actionResult != null) {
            renderJSON(actionResult);
        }
        String result = DianquanUtils.getDianquanGidListJson(type, activityFilters, siteFilters, typeFilters, lowPrice, highPrice, ratio, sales, searchText);
        play.cache.Cache.safeAdd(queryStrMd5, result, "1h");
        renderJSON(result);
    }
}
