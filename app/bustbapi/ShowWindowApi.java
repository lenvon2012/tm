/**
 * 
 */

package bustbapi;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import job.showwindow.ShowWindowInitJob.ShowCaseInfo;
import models.user.User;

import org.apache.commons.collections.SetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import result.TMResult;

import com.ciaosir.client.CommonUtils;
import com.google.gson.Gson;
import com.taobao.api.ApiException;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.Shop;
import com.taobao.api.request.ItemRecommendAddRequest;
import com.taobao.api.request.ItemRecommendDeleteRequest;
import com.taobao.api.request.ShopRemainshowcaseGetRequest;
import com.taobao.api.response.ItemRecommendAddResponse;
import com.taobao.api.response.ItemRecommendDeleteResponse;
import com.taobao.api.response.ShopRemainshowcaseGetResponse;

/**
 * @author jd 创建于 2012-8-28 上午10:32:47
 * 
 */
public class ShowWindowApi {
    public static final Logger log = LoggerFactory.getLogger(ShowWindowApi.class);

    public static class GetRemainShowcase extends
            TBApi<ShopRemainshowcaseGetRequest, ShopRemainshowcaseGetResponse, Long> {
        /**
         * @param sid
         */
        public GetRemainShowcase(String sid) {
            super(sid);
        }

        @Override
        public ShopRemainshowcaseGetRequest prepareRequest() {
            ShopRemainshowcaseGetRequest req = new ShopRemainshowcaseGetRequest();
            return req;
        }

        @Override
        public Long validResponse(ShopRemainshowcaseGetResponse resp) {

            boolean res = ErrorHandler.validResponseBoolean(resp);
            if (res) {
                return resp.getShop() == null ? 0L : resp.getShop().getRemainCount();
            } else {
                return 0L;
            }

        }

        @Override
        public Long applyResult(Long res) {
            return res;
        }

        @Override
        protected ShopRemainshowcaseGetResponse execProcess() throws ApiException {
            return super.validateWindowCase();
        }
    }

    public static class GetShopShowcase extends
            TBApi<ShopRemainshowcaseGetRequest, ShopRemainshowcaseGetResponse, ShowCaseInfo> {
        /**
         * @param sid
         */
        public GetShopShowcase(String sid) {
            super(sid);
        }

        @Override
        public ShopRemainshowcaseGetRequest prepareRequest() {
            ShopRemainshowcaseGetRequest req = new ShopRemainshowcaseGetRequest();
            return req;
        }

        @Override
        public ShowCaseInfo validResponse(ShopRemainshowcaseGetResponse resp) {

            boolean res = ErrorHandler.validResponseBoolean(resp);

            Shop shop = resp.getShop();
            if (shop == null) {
//                log.error("no shop???:::" + new Gson().toJson(resp));
                return null;
            }

            Long usedCount = shop.getUsedCount();
            if (usedCount < 0L) {
//                log.error("no userd count ???:::" + new Gson().toJson(resp));
                return null;
            }

            ShowCaseInfo info = new ShowCaseInfo();
            info.setOnShowItemCount(shop.getUsedCount().intValue());
            info.setTotalWindowCount(shop.getAllCount().intValue());
            info.setRemainWindowCount(shop.getRemainCount().intValue());
            return info;
        }

        @Override
        public ShowCaseInfo applyResult(ShowCaseInfo res) {
            return res;
        }

        @Override
        protected ShopRemainshowcaseGetResponse execProcess() throws ApiException {
            return super.validateWindowCase();
        }
    }

    public static class AddRecommend extends TBApi<ItemRecommendAddRequest, ItemRecommendAddResponse, TMResult<Item>> {

        public long numIid;

        /**
         * @param cid
         */
        public AddRecommend(User user, long numIid) {
            super(user.getSessionKey());
            this.numIid = numIid;

//            this.appKey = appKey;
//            this.appSecret = appSecret;
        }

        @Override
        public TMResult<Item> applyResult(TMResult<Item> res) {
            return res;
        }

        @Override
        public ItemRecommendAddRequest prepareRequest() {
            ItemRecommendAddRequest req = new ItemRecommendAddRequest();
            req.setNumIid(numIid);
            return req;
        }

        /*
         * (non-Javadoc)
         * 
         * @see bustbapi.TBApi#validResponse(com.taobao.api.TaobaoResponse)
         */
        @Override
        public TMResult<Item> validResponse(ItemRecommendAddResponse resp) {
            this.isApiSuccess = false;

            if (resp == null) {
                log.error("Null Resp Returned");
                return new TMResult<Item>("no res", "no res");
            }
            
            ErrorHandler.validTaoBaoResp(this, resp);

//            log.warn(" add item back:" + new Gson().toJson(resp.getItem()));

            Item rawItem = resp.getItem();
//            if (rawItem == null) {
//                log.info("add item back nullll for:" + user.toIdNick() + " with numiid :" + numIid);
//            } else {
//                log.info("add item ok for back res :" + rawItem.getNumIid() + " with modified time :"
//                        + DateUtil.formDateForLog(rawItem.getModified() == null ? 0L : rawItem.getModified().getTime()));
//            }

            if (!resp.isSuccess()) {
                String Msg = resp.getMsg();
                String subErrorCode = resp.getSubCode();
                String subMsg = resp.getSubMsg();
//                log.warn("Sub Error Code: " + subErrorCode);
//                log.warn("Sub Msg: " + subMsg);
                this.subErrorCode = resp.getSubCode();
                this.subErrorMsg = resp.getSubMsg();

                return new TMResult<Item>(this.subErrorCode, this.subErrorMsg);
            }

            this.isApiSuccess = true;
            return new TMResult<Item>(resp.getItem());
        }

    }

    public static class DeleteRecommend extends
            TBApi<ItemRecommendDeleteRequest, ItemRecommendDeleteResponse, TMResult<Item>> {

        long numIid;

        String subMsg = null;

        public DeleteRecommend(String sid, long numIid) {
            super(sid);
            this.numIid = numIid;
            this.retryTime = 5;
        }

        public DeleteRecommend(User user, long numIid) {
            super(user.getSessionKey());
            this.numIid = numIid;
            this.retryTime = 5;
        }

//        public DeleteRecommend(String appKey, String appSecret, String sid, long numIid) {
//            super(sid);
//            this.numIid = numIid;
//            this.appKey = appKey;
//            this.appSecret = appSecret;
//            this.retryTime = 5;
//        }

        /*
         * (non-Javadoc)
         * 
         * @see bustbapi.TBApi#applyResult(java.lang.Object)
         */
        @Override
        public TMResult<Item> applyResult(TMResult<Item> res) {
            return res;
        }

        /*
         * (non-Javadoc)
         * 
         * @see bustbapi.TBApi#prepareRequest()
         */
        @Override
        public ItemRecommendDeleteRequest prepareRequest() {
            // TODO Auto-generated method stub
            ItemRecommendDeleteRequest req = new ItemRecommendDeleteRequest();
            req.setNumIid(numIid);
            return req;
        }

        /*
         * (non-Javadoc)
         * 
         * @see bustbapi.TBApi#validResponse(com.taobao.api.TaobaoResponse)
         */
        @Override
        public TMResult<Item> validResponse(ItemRecommendDeleteResponse resp) {
            this.isApiSuccess = false;

            if (resp == null) {
                log.error("Null Resp Returned");
                return new TMResult<Item>(" no result code", " no result msg");
            }
            
            ErrorHandler.validTaoBaoResp(this, resp);

            Item rawItem = resp.getItem();
//            if (rawItem == null) {
//                log.info("cancel item back nullll for:" + user.toIdNick() + " with numiid :" + numIid);
//            } else {
//                log.info("cancel item ok for back res :" + rawItem.getNumIid() + " with modified time :"
//                        + DateUtil.formDateForLog(rawItem.getModified() == null ? 0L : rawItem.getModified().getTime()));
//            }

            if (!resp.isSuccess()) {
                int errorCode = Integer.parseInt(resp.getErrorCode());
                String Msg = resp.getMsg();
                String subErrorCode = resp.getSubCode();
                String subMsg = resp.getSubMsg();

                this.subErrorCode = resp.getSubCode();

                log.warn("Error Code: " + errorCode);
                log.warn("Msg: " + Msg);
                log.warn("Sub Error Code: " + subErrorCode);
                log.warn("Sub Msg: " + subMsg);
                this.subMsg = resp.getSubMsg();

                if ("This ban will last for 1 more seconds".equals(subMsg)) {
                    log.info("sleep for the window for 1 second");
                    return new TMResult<Item>(this.subErrorCode, this.subErrorMsg);
                }

                return new TMResult<Item>(this.subErrorCode, this.subErrorMsg);
            }

            this.isApiSuccess = true;
            return new TMResult<Item>(rawItem);
        }

    }

    public static Set<Long> toNumIids(List<Item> items) {
        if (CommonUtils.isEmpty(items)) {
            return SetUtils.EMPTY_SET;
        }
        Set<Long> res = new HashSet<Long>();
        for (Item item : items) {
            res.add(item.getNumIid());
        }
        return res;
    }

    public static void DeleteRecomendAll(Collection<Long> numIids, String sid) {
        for (Long numIid : numIids) {
            new ShowWindowApi.DeleteRecommend(sid, numIid.longValue()).call();
        }

    }

}
