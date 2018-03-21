package bustbapi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jdapi.JDApi;
import models.jd.JDUser;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.utils.DateUtil;
import com.jd.open.api.sdk.domain.order.OrderSearchInfo;
import com.jd.open.api.sdk.request.order.OrderSearchRequest;
import com.jd.open.api.sdk.response.order.OrderSearchResponse;

public class JDTradeApi {

    public final static Logger log = LoggerFactory.getLogger(JDTradeApi.class);

    public static final String OPTIONAL_FIELDS = "vender_remark,pin";

    public static final String TRADE_FIELDS = "buyer_area,tid,status,seller_nick,buyer_nick,num";
    public final static String TRADE_ACOOKIE_FIELDS = "acookie_id,status,buyer_nick,num,payment";

    static String RECENT_TRADE_FIELD = "tid,pay_time,orders.num_iid,orders.num";

    static String TRADE_OID_FIELD = "tid,orders.oid,orders.num_iid,orders.num";

    public final static String TRADE_GET_FIELDS = "consign_time";

    public static final int TRADE_PAGE_SIZE = 100;

    public static String ALL_ORDER_STATE = "WAIT_SELLER_STOCK_OUT,SEND_TO_DISTRIBUTION_CENER,DISTRIBUTION_CENTER_RECEIVED,WAIT_GOODS_RECEIVE_CONFIRM,RECEIPTS_CONFIRM,FINISHED_L,TRADE_CANCELED";

    public static class JDTradesSold extends JDApi<OrderSearchRequest, OrderSearchResponse, List<OrderSearchInfo>> {

        public JDUser user;

        public long ts;

        public Date startCreated;

        public Date endCreated;

        public String orderState;

        public List<OrderSearchInfo> resList;

        public int pn = 1;

        public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // public JDTradesSold(JDUser user, long ts, Date startCreated, Date endCreated) {
        // this(user, ts, startCreated, endCreated);
        // }

        public JDTradesSold(JDUser user, long ts, Date startCreated, Date endCreated) {

            super(user.getAccessToken());
            this.user = user;
            this.ts = ts;
            this.startCreated = startCreated;
            this.endCreated = endCreated;

            this.resList = new ArrayList<OrderSearchInfo>();
        }

        public JDTradesSold(JDUser user, long ts, Date startCreated, Date endCreated, String orderState) {

            super(user.getAccessToken());
            this.user = user;
            this.ts = ts;
            this.startCreated = startCreated;
            this.endCreated = endCreated;
            this.orderState = orderState;

            this.resList = new ArrayList<OrderSearchInfo>();
        }

        public JDTradesSold(JDUser user, long ts, Date endCreated) {

            super(user.getAccessToken());
            this.user = user;
            this.ts = ts;
            this.startCreated = new Date(endCreated.getTime() - DateUtil.DAY_MILLIS);
            this.endCreated = endCreated;

            this.resList = new ArrayList<OrderSearchInfo>();
        }

        @Override
        public OrderSearchRequest prepareRequest() {

            OrderSearchRequest request = new OrderSearchRequest();
            if (startCreated != null) {
                request.setStartDate(sdf.format(startCreated));
            }
            if (endCreated != null) {
                request.setEndDate(sdf.format(endCreated));
            }
            if (StringUtils.isEmpty(orderState)) {
                request.setOrderState(ALL_ORDER_STATE);
            } else {
                request.setOrderState(orderState);
            }
            request.setPage(String.valueOf(pn));
            request.setPageSize(String.valueOf(TRADE_PAGE_SIZE));
            request.setOptionalFields("vender_id,order_id,pay_type");

            request.setOptionalFields(OPTIONAL_FIELDS);

            return request;
        }

        @Override
        public List<OrderSearchInfo> validResponse(OrderSearchResponse resp) {

            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }

            if (!"0".equals(resp.getCode())) {
                log.error("resp Error msg:" + resp.getMsg());
                return null;
            }

            if (resp.getOrderInfoResult().getOrderTotal() == TRADE_PAGE_SIZE) {
                this.iteratorTime = 1;
                this.pn++;
            }
            List<OrderSearchInfo> ll = resp.getOrderInfoResult().getOrderInfoList();
//            ll.get(0).getItemInfoList().get(0).getSkuName()
            System.out.println(ll);
            return resp.getOrderInfoResult().getOrderInfoList();
        }

        @Override
        public List<OrderSearchInfo> applyResult(List<OrderSearchInfo> res) {

            if (res == null) {
                return resList;
            }
            resList.addAll(res);
            // TradeWritter.addTradeList(user.getId(), ts, res);
            return resList;

        }
    }

}
