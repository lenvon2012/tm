
package tbapi.subcribe;

import static java.lang.String.format;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.DateUtil;
import bustbapi.TBApi;

import com.ciaosir.client.CommonUtils;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.domain.ArticleBizOrder;
import com.taobao.api.domain.ArticleSub;
import com.taobao.api.request.VasOrderSearchRequest;
import com.taobao.api.request.VasSubscSearchRequest;
import com.taobao.api.response.VasOrderSearchResponse;
import com.taobao.api.response.VasSubscSearchResponse;

import configs.Subscribe;
import configs.TMConfigs.App;
import controllers.APIConfig;

public class VasOrderApi {

    private static final Logger log = LoggerFactory.getLogger(VasOrderApi.class);

    public static final String TAG = "VasOrderApi";

    public static final long PAGE_SIZE = 16L;

    /**
     * Add retry later...
     * 
     * @param start
     * @param end
     * @param list
     * @return
     */
    public static boolean exportAll(long start, long end, List<ArticleSub> list) {

        log.info(format("exportAll:start, end, list".replaceAll(", ", "=%s, ") + "=%s", start, end, list));
        try {

            for (String itemCode : Subscribe.ALL_ITEM_CODE) {
                export(APIConfig.get().getSubCode(), itemCode, start, end, list);
            }

            return true;
        } catch (ApiException e) {
            return false;
        } finally {
        }

    }

    public static void export(String articleCode, String itemCode, long start, long end, List<ArticleSub> list)
            throws ApiException {

        log.info(format("export:articleCode, itemCode, start, end, list".replaceAll(", ", "=%s, ") + "=%s",
                articleCode, itemCode, start, end, list));

        TaobaoClient client = TBApi.genClient();
        VasSubscSearchRequest req = new VasSubscSearchRequest();
        req.setArticleCode(articleCode);
        req.setItemCode(itemCode);
        // req.setArticleCode("ts-1234");
        // req.setItemCode("ts-1234-1");
        Date dateTime = new Date(start);
        req.setStartDeadline(dateTime);
        Date endTime = new Date(end);
        req.setEndDeadline(endTime);
        log.info("[start:]" + dateTime);
        log.info("[end:]" + endTime);
        // req.setStatus(1L);
        // req.setAutosub(true);
        // req.setExpireNotice(true);
        req.setPageSize(PAGE_SIZE);
        long currPage = 1L;
        long totalNum = 0L;
        do {
            req.setPageNo(currPage);
            VasSubscSearchResponse resp = client.execute(req);
            if (resp == null) {
                log.error("Resp Empty");
                return;
            }
            totalNum = resp.getTotalItem();

            log.info("[Return List:]" + resp.getArticleSubs());
            if (!CommonUtils.isEmpty(resp.getArticleSubs())) {
                list.addAll(resp.getArticleSubs());
            }
        } while (((currPage++) * PAGE_SIZE) < totalNum);

    }

    public static void varOrderSearch(String articleCode, long start, long end, List<ArticleBizOrder> list)
            throws ApiException {
        varOrderSearch(articleCode, null, start, end, -1, list);
    }

    public static void varOrderSearch(String articleCode, String itemCode, long start, long end, long bizType,
            List<ArticleBizOrder> list) throws ApiException {

        long currPage = 1L;
        long totalNum = 0L;
        do {
            totalNum = varOrderSearch(articleCode, itemCode, start, end, bizType, currPage, list, 0);
            CommonUtils.sleepQuietly(2000L);
        } while (((currPage++) * PAGE_SIZE) < totalNum);

    }

    public static long varOrderSearch(String articleCode, String itemCode, long start, long end, long bizType,
            long pageNo, List<ArticleBizOrder> list, int retryTime) throws ApiException {

        if (retryTime++ == 5) {
//            throw new ApiException(ErrorCode.API_LIMIT);
            return 0L;
        }
        CommonUtils.sleepQuietly(2000L);

        log.info(format("export:articleCode, itemCode, start, end, list".replaceAll(", ", "=%s, ") + "=%s",
                articleCode, itemCode, start, end, list.size()));

        TaobaoClient client = new DefaultTaobaoClient(App.API_TAOBAO_URL, App.APP_KEY, App.APP_SECRET);
        VasOrderSearchRequest req = new VasOrderSearchRequest();

        req.setArticleCode(articleCode);
        if (itemCode != null) {
            req.setItemCode(itemCode);
        }
        req.setStartCreated(new Date(start));
        req.setEndCreated(new Date(end));
        if (bizType > 0) {
            req.setBizType(bizType);
        }
        req.setPageSize(PAGE_SIZE);

        req.setPageNo(pageNo);
        VasOrderSearchResponse resp = client.execute(req);

        long totalNum = -1;
        if (resp.isSuccess()) {
            totalNum = resp.getTotalItem();
            log.info("[Return List:]" + resp.getArticleBizOrders().size());
            if (!CommonUtils.isEmpty(resp.getArticleBizOrders())) {
                list.addAll(resp.getArticleBizOrders());
            }
            return totalNum;
        } else {
            try {
                Thread.sleep(DateUtil.THREE_SECONDS_MILLIS);
            } catch (InterruptedException e) {
                log.warn(e.getMessage(), e);
            }
            log.warn("Sleep Three Seconds...");
            return varOrderSearch(articleCode, itemCode, start, end, bizType, pageNo, list, retryTime);
        }
    }
}
