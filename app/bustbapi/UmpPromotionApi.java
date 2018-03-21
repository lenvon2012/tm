package bustbapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.api.domain.PromotionDisplayTop;
import com.taobao.api.request.UmpPromotionGetRequest;
import com.taobao.api.response.UmpPromotionGetResponse;

public class UmpPromotionApi {
    private static final Logger log = LoggerFactory.getLogger(UmpPromotionApi.class);
    
    public static class UmpPromotionGetApi extends TBApi<UmpPromotionGetRequest, UmpPromotionGetResponse, PromotionDisplayTop> {

        private Long numIid;
        
        public UmpPromotionGetApi(String sid, Long numIid) {
            super(sid);
            this.numIid = numIid;
            //retryTime = 10;//这个api成功率有点低。。。
        }

        @Override
        public UmpPromotionGetRequest prepareRequest() {
            if (numIid == null && numIid <= 0)
                return null;
            UmpPromotionGetRequest req = new UmpPromotionGetRequest();
            req.setItemId(numIid);
            return req;
        }

        @Override
        public PromotionDisplayTop validResponse(UmpPromotionGetResponse resp) {
            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }
            
            if (!resp.isSuccess()) {
                log.error("skus get api error");
                return null;
            }
            
            return resp.getPromotions();
            
        }

        @Override
        public PromotionDisplayTop applyResult(PromotionDisplayTop res) {
            return res;
        }
        
    }
}
