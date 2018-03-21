package bustbapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.api.domain.Shop;
import com.taobao.api.request.ShopGetRequest;
import com.taobao.api.response.ShopGetResponse;

public class ShopApi {

    public final static Logger log = LoggerFactory.getLogger(ShopApi.class);

    public final static String FIELDS = "sid,cid,title,nick,desc,bulletin,pic_path,created,modified,shop_score";

    public static class ShopGet extends TBApi<ShopGetRequest, ShopGetResponse, Shop> {

        public String userNick;

        public ShopGet(String userNick) {
            super();
            this.userNick = userNick;
        }

        @Override
        public ShopGetRequest prepareRequest() {
            ShopGetRequest req = new ShopGetRequest();
            req.setFields(FIELDS);
            req.setNick(userNick);
            return req;
        }

        @Override
        public Shop validResponse(ShopGetResponse resp) {
            if (resp == null) {
                log.error("No results return !!!");
            }

            ErrorHandler.validTaoBaoResp(resp);

            if (resp.isSuccess()) {
                return resp.getShop();
            }

            return null;
        }

        @Override
        public Shop applyResult(Shop res) {
            return res;
        }

    }

}
