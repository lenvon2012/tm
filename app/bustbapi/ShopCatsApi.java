
package bustbapi;

import java.util.List;

import models.shop.ShopCatPlay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;
import com.taobao.api.domain.ShopCat;
import com.taobao.api.request.ShopcatsListGetRequest;
import com.taobao.api.response.ShopcatsListGetResponse;

public class ShopCatsApi {

    public final static Logger log = LoggerFactory.getLogger(ShopCatsApi.class);

    public final static String FIELDS = "cid,parent_cid,name,is_parent";

    public static class ShopCatsListGet extends TBApi<ShopcatsListGetRequest, ShopcatsListGetResponse, List<ShopCat>> {

        public ShopCatsListGet() {
            super();
        }

        @Override
        public ShopcatsListGetRequest prepareRequest() {
            ShopcatsListGetRequest req = new ShopcatsListGetRequest();
            req.setFields(FIELDS);
            return req;
        }

        @Override
        public List<ShopCat> validResponse(ShopcatsListGetResponse resp) {

            if (resp == null) {
                log.error("No results return !!!");
            }
            ErrorHandler.validTaoBaoResp(resp);

            if (resp.isSuccess()) {
                return resp.getShopCats();
            }

            return null;
        }

        @Override
        public List<ShopCat> applyResult(List<ShopCat> res) {
            if (!CommonUtils.isEmpty(res)) {
                for (ShopCat shopCat : res) {
                    new ShopCatPlay(shopCat).jdbcSave();
                }
            }
            return res;
        }
    }

}
