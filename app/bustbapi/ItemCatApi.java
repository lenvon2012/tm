
package bustbapi;

import java.util.List;

import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.taobao.api.domain.ItemCat;
import com.taobao.api.request.ItemcatsGetRequest;
import com.taobao.api.response.ItemcatsGetResponse;

public class ItemCatApi {

    public final static Logger log = LoggerFactory.getLogger(ItemCatApi.class);

    public static String ITEMCAT_FIELDS = "cid,parent_cid,name,is_parent,status,sort_order";

    public static class ItemcatsGet extends TBApi<ItemcatsGetRequest, ItemcatsGetResponse, List<ItemCat>> {

        public Long parentCid;

        public Long cid = null;

        public ItemcatsGet(User user, Long parentCid) {
            super(user.getSessionKey());
            this.parentCid = parentCid;
        }

        public ItemcatsGet(Long parentCid, Long cid) {
            super();
            this.parentCid = parentCid;
            this.cid = cid;
        }

        @Override
        public ItemcatsGetRequest prepareRequest() {
            ItemcatsGetRequest req = new ItemcatsGetRequest();
            req.setFields(ITEMCAT_FIELDS);
            if (parentCid != null && parentCid >= 0L) {
                req.setParentCid(parentCid);
            }
            if (cid != null && cid > 0L) {
                req.setCids(String.valueOf(cid));
            }
            return req;
        }

        @Override
        public List<ItemCat> validResponse(ItemcatsGetResponse resp) {

            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }

            if (!resp.isSuccess()) {
                log.error("resp submsg" + resp.getSubMsg());
                log.error("resp error code " + resp.getErrorCode());
                log.error("resp Mesg " + resp.getMsg());
                return null;
            }

            return resp.getItemCats();
        }

        @Override
        public List<ItemCat> applyResult(List<ItemCat> res) {
            return res;
        }
    }

    public static class ItemcatsListGet extends TBApi<ItemcatsGetRequest, ItemcatsGetResponse, List<ItemCat>> {

        public Long parentCid;

        public String cids = StringUtils.EMPTY;

        public ItemcatsListGet(User user, Long parentCid) {
            super(user.getSessionKey());
            this.parentCid = parentCid;
        }

        public ItemcatsListGet(User user, String cids) {
            super(user.getSessionKey());
            this.cids = cids;
        }
        @Override
        public ItemcatsGetRequest prepareRequest() {
            ItemcatsGetRequest req = new ItemcatsGetRequest();
            req.setFields(ITEMCAT_FIELDS);
            if (parentCid != null && parentCid > 0L) {
                req.setParentCid(parentCid);
            }
            if (!StringUtils.isEmpty(cids)) {
                req.setCids(String.valueOf(cids));
            }
            return req;
        }

        @Override
        public List<ItemCat> validResponse(ItemcatsGetResponse resp) {

            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }

            if (!resp.isSuccess()) {
                log.error("resp submsg" + resp.getSubMsg());
                log.error("resp error code " + resp.getErrorCode());
                log.error("resp Mesg " + resp.getMsg());
                return null;
            }

            return resp.getItemCats();
        }

        @Override
        public List<ItemCat> applyResult(List<ItemCat> res) {
            return res;
        }
    }
    
}
