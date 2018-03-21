
package bustbapi.sync;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.api.ApiException;
import com.taobao.api.TaobaoClient;
import com.taobao.api.domain.Trade;
import com.taobao.api.internal.util.AtsUtils;
import com.taobao.api.internal.util.TaobaoUtils;
import com.taobao.api.request.IncrementCustomerPermitRequest;
import com.taobao.api.request.TopatsResultGetRequest;
import com.taobao.api.request.TopatsTradesSoldGetRequest;
import com.taobao.api.request.TradeFullinfoGetRequest;
import com.taobao.api.request.TradesSoldGetRequest;
import com.taobao.api.request.TradesSoldIncrementGetRequest;
import com.taobao.api.response.TopatsResultGetResponse;
import com.taobao.api.response.TopatsTradesSoldGetResponse;
import com.taobao.api.response.TradeFullinfoGetResponse;
import com.taobao.api.response.TradesSoldGetResponse;
import com.taobao.api.response.TradesSoldIncrementGetResponse;

public class TopApiService {

    private static final Log log = LogFactory.getLog(TopApiService.class);

    private static final long DEFAULT_PAGE_SIZE = 100L;

    private TaobaoClient client;

    public TopApiService(TaobaoClient client) {
        this.client = client;
    }

    public void syncSoldTrades(Date start, Date end, String sessionKey) throws Exception {
        TradesSoldGetRequest req = new TradesSoldGetRequest();
        req.setFields("tid");
        req.setType("ec,fixed,auction,auto_delivery,cod,independent_shop_trade,independent_simple_trade,shopex_trade,netcn_trade,external_trade,hotel_trade,fenxiao,game_equipment,instant_trade,b2c_cod,super_market_trade,super_market_cod_trade,alipay_movie,taohua,waimai,nopaid");
        req.setStartCreated(start);
        req.setEndCreated(end);
        req.setPageSize(DEFAULT_PAGE_SIZE);
        req.setUseHasNext(true);

        long pageNo = 1;
        TradesSoldGetResponse rsp = null;
        do {
            req.setPageNo(pageNo);
            rsp = client.execute(req, sessionKey);
            if (rsp.isSuccess()) {
                log.info("同步中>>>第" + req.getPageNo() + "页");
                for (Trade t : rsp.getTrades()) {
                    getTradeFullInfo(t.getTid(), sessionKey); // FIXME 保存订单到数据库中
                }
                pageNo++;
            }
        } while (rsp != null && (!rsp.isSuccess() || rsp.getHasNext()));
    }

    public void syncIncrementSoldTrades(Date start, Date end, String sessionKey) throws Exception {
        TradesSoldIncrementGetRequest req = new TradesSoldIncrementGetRequest();
        req.setFields("tid");
        req.setType("ec,fixed,auction,auto_delivery,cod,independent_shop_trade,independent_simple_trade,shopex_trade,netcn_trade,external_trade,hotel_trade,fenxiao,game_equipment,instant_trade,b2c_cod,super_market_trade,super_market_cod_trade,alipay_movie,taohua,waimai,nopaid");
        req.setStartModified(start);
        req.setEndModified(end);
        req.setPageSize(DEFAULT_PAGE_SIZE);
        req.setUseHasNext(false);

        TradesSoldIncrementGetResponse rsp = client.execute(req, sessionKey);
        if (rsp.isSuccess()) {
            long pageCount = (rsp.getTotalResults() + req.getPageSize() - 1) / req.getPageSize();
            while (pageCount > 0) {
                req.setPageNo(pageCount);
                req.setUseHasNext(true); // 终止统计
                rsp = client.execute(req, sessionKey);
                if (rsp.isSuccess()) {
                    log.info("同步中>>>第" + req.getPageNo() + "页");
                    for (Trade t : rsp.getTrades()) {
                        getTradeFullInfo(t.getTid(), sessionKey); // FIXME 保存订单到数据库中
                    }
                    pageCount--;
                }
            }
        }
    }

    public Trade getTradeFullInfo(Long tid, String sessionKey) throws ApiException {
        TradeFullinfoGetRequest req = new TradeFullinfoGetRequest();
        req.setFields("tid,seller_nick,buyer_nick,buyer_message,orders"); // FIXME 增加应用需要的字段
        req.setTid(tid);
        TradeFullinfoGetResponse rsp = client.execute(req, sessionKey);
        if (rsp.isSuccess()) {
            log.info("查询订单详情成功：" + rsp.getBody());
            return rsp.getTrade();
        }
        return null;
    }

    public Long asyncSoldTrades(String start, String end, String sessionKey) throws ApiException {
        TopatsTradesSoldGetRequest req = new TopatsTradesSoldGetRequest();
        req.setFields("tid,seller_nick,buyer_nick,buyer_message,orders");
        req.setStartTime(start);
        req.setEndTime(end);
        TopatsTradesSoldGetResponse rsp = client.execute(req, sessionKey);
        if (rsp.isSuccess()) {
            return rsp.getTask().getTaskId();
        }
        return null;
    }

    public String getTaskResultUrl(Long taskId) throws ApiException {
        TopatsResultGetRequest req = new TopatsResultGetRequest();
        req.setTaskId(taskId);
        TopatsResultGetResponse rsp = client.execute(req);
        if (rsp.isSuccess()) {
            if ("done".equals(rsp.getTask().getStatus())) {
                return rsp.getTask().getDownloadUrl();
            }
        }
        return null;
    }

    public void permitUser(String sessionKey) throws ApiException {
        IncrementCustomerPermitRequest req = new IncrementCustomerPermitRequest();
        req.setType("notify");
        client.execute(req, sessionKey);
    }

    /**
     * 原来一行就是 一个 fullinfogetresponse.....
     * @param url
     * @throws Exception
     */
    public void downloadAndProcess(String url) throws Exception {
        File zip = AtsUtils.download(url, new File("e:/Downloads/Trade/Zip"));
        List<File> files = AtsUtils.unzip(zip, new File("e:/Downloads/Trade/Unzip"));
        for (File file : files) {
            BufferedReader br = null;
            try {
                FileReader fr = new FileReader(file);
                br = new BufferedReader(fr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    TradeFullinfoGetResponse rsp = TaobaoUtils.parseResponse(line, TradeFullinfoGetResponse.class);
                    Trade trade = rsp.getTrade(); // FIXME 保存订单到数据库中
                    System.out.println("处理订单：" + trade.getTid());
                }
            } finally {
                if (br != null) {
                    br.close();
                }
            }
        }
    }

}
