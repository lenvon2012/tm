package bustbapi;

import models.user.User;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.api.domain.Sku;
import com.taobao.api.request.ItemSkuPriceUpdateRequest;
import com.taobao.api.response.ItemSkuPriceUpdateResponse;

public class ItemSkuApi {
    
    private static final Logger log = LoggerFactory.getLogger(ItemSkuApi.class);
    
    private static final String SKU_FIELDS = "sku_spec_id,sku_id,num_iid,quantity,price,status,created,modified";
    private static final int MAX_NUMIID_LENGTH = 30;
    
    
    /*public static class ItemsSkuGetApi extends TBApi<ItemSkusGetRequest, ItemSkusGetResponse, List<Sku>> {
        private User user;
        
        private List<List<Long>> splitToSubLongList;
        private List<Sku> resSkuList = new ArrayList<Sku>();
        
        public ItemsSkuGetApi(String sid, User user, Collection<Long> numIidCollection) {
            super(sid);
            try {
                this.user = user;
                this.splitToSubLongList = SplitUtils.splitToSubList(numIidCollection, MAX_NUMIID_LENGTH);
                
                this.iteratorTime = this.splitToSubLongList.size();
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }

        @Override
        public ItemSkusGetRequest prepareRequest() {
            
            if (user == null) {
                log.error("sku api: user is null, return!!!");
                return null;
            }
            if (CommonUtils.isEmpty(splitToSubLongList.get(iteratorTime))) {
                log.error("sku api: numIidList is null, return!!!!");
                return null;
            }
            String numIids = StringUtils.join(splitToSubLongList.get(iteratorTime), ",");
            if (StringUtils.isEmpty(numIids)) {
                return null;
            }
            
            ItemSkusGetRequest req = new ItemSkusGetRequest();
            req.setFields(SKU_FIELDS);
            req.setNumIids(numIids);
            
            return req;
        }

        @Override
        public List<Sku> validResponse(ItemSkusGetResponse resp) {
            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }
            
            if (!resp.isSuccess()) {
                log.error("skus get api error");
                return null;
            }
            
            return resp.getSkus();
        }

        @Override
        public List<Sku> applyResult(List<Sku> res) {
            if (res == null) {
                return resSkuList;
            }

            resSkuList.addAll(res);
            return resSkuList;
        }
        
    }*/
    
    
    
    public static class SkuPriceUpdateApi extends TBApi<ItemSkuPriceUpdateRequest, ItemSkuPriceUpdateResponse, Sku> {

        private Long numIid;
        private String properties;
        private String skuPrice;
        private String itemPrice;
        private boolean isSetItemPrice;
        
        public SkuPriceUpdateApi(User user, Long numIid, String properties, String skuPrice) {
            this(user, numIid, properties, skuPrice, "", false);
        }

        public SkuPriceUpdateApi(User user, Long numIid, String properties, String skuPrice, 
                String itemPrice, boolean isSetItemPrice) {
            super(user.getSessionKey());
            this.numIid = numIid;
            this.properties = properties;
            this.skuPrice = skuPrice;
            this.itemPrice = itemPrice;
            this.isSetItemPrice = isSetItemPrice;
            this.retryTime = 2;
        }

        @Override
        public ItemSkuPriceUpdateRequest prepareRequest() {
            ItemSkuPriceUpdateRequest req = new ItemSkuPriceUpdateRequest();

            req.setNumIid(numIid);
            req.setProperties(properties);
            req.setPrice(skuPrice);
            
            if (isSetItemPrice == true && StringUtils.isEmpty(itemPrice) == false) {
                req.setItemPrice(itemPrice);
            }
            
            
            return req;
        }

        @Override
        public Sku validResponse(ItemSkuPriceUpdateResponse resp) {

            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }
            
            errorMsg = resp.getSubMsg();
            
            ErrorHandler.validTaoBaoResp(resp);
            if (!resp.isSuccess()) {
                return null;
            }
            return resp.getSku();
        }

        @Override
        public Sku applyResult(Sku res) {
            return res;
        }

    }
    
    
}
