package bustbapi;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jdp.ApiJdpAdapter;
import job.message.DeleteItemJob;
import models.item.ItemPlay;
import models.user.User;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import autotitle.ItemPropAction;
import bustbapi.request.ItemAddRequest;
import carrier.FileCarryUtils;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.StringPair;
import com.ciaosir.client.utils.CiaoStringUtil;
import com.ciaosir.client.utils.NumberUtil;
import com.ciaosir.client.utils.SplitUtils;
import com.google.gson.Gson;
import com.taobao.api.ApiException;
import com.taobao.api.ApiRuleException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.FileItem;
import com.taobao.api.domain.FoodSecurity;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.ItemImg;
import com.taobao.api.domain.NotifyItem;
import com.taobao.api.domain.PropImg;
import com.taobao.api.domain.Sku;
import com.taobao.api.request.IncrementItemsGetRequest;
import com.taobao.api.request.ItemGetRequest;
import com.taobao.api.request.ItemImgUploadRequest;
import com.taobao.api.request.ItemPropimgUploadRequest;
import com.taobao.api.request.ItemUpdateRequest;
import com.taobao.api.request.ItemsInventoryGetRequest;
import com.taobao.api.request.ItemsListGetRequest;
import com.taobao.api.request.ItemsOnsaleGetRequest;
import com.taobao.api.response.IncrementItemsGetResponse;
import com.taobao.api.response.ItemAddResponse;
import com.taobao.api.response.ItemGetResponse;
import com.taobao.api.response.ItemImgUploadResponse;
import com.taobao.api.response.ItemPropimgUploadResponse;
import com.taobao.api.response.ItemUpdateResponse;
import com.taobao.api.response.ItemsInventoryGetResponse;
import com.taobao.api.response.ItemsListGetResponse;
import com.taobao.api.response.ItemsOnsaleGetResponse;

import configs.TMConfigs.PageSize;
import controllers.APIConfig;

public class OldItemApi {

    public final static Logger log = LoggerFactory.getLogger(ItemApi.class);

    public static final String FIELDS = "seller_cids,detail_url,approve_status,num_iid,title,nick,"
            + "cid,pic_url,num,price,list_time,delist_time,outer_id,modified,has_showcase,created,is_fenxiao,is_virtual";

    public static final String FIELDS_WITH_DESC_MODULES = "seller_cids,detail_url,approve_status,num_iid,title,nick,"
            + "cid,pic_url,num,price,desc_modules,list_time,delist_time,outer_id,modified,has_showcase,created,is_fenxiao,is_virtual";

    
    public static final String FIELDS_WITH_SKU = "seller_cids,detail_url,approve_status,num_iid,title,nick,"
            + "cid,pic_url,num,sku,price,list_time,delist_time,outer_id,modified,has_showcase,is_fenxiao,is_virtual";

    public static final String FIELDS_WITH_DESC = "seller_cids,detail_url,approve_status,num_iid,title,wireless_desc"
            + "nick,cid,pic_url,num,price,list_time,delist_time,outer_id,desc,modified,is_fenxiao,is_virtual";

    public static final String FIELDS_WITH_PROPS = "property_alias,props,props_name,seller_cids,detail_url,created,"
            + "approve_status,num_iid,title,nick,cid,pic_url,num,price,list_time,delist_time,outer_id,modified," +
            "has_showcase,is_fenxiao,is_virtual,sku,wireless_desc";

    public static final String FIELDS_ALL = "property_alias,props,props_name,seller_cids,detail_url,wireless_desc,"
            + "approve_status,num_iid,title,nick,cid,sku,pic_url,num,price,list_time,delist_time,outer_id," +
            "desc,modified,has_showcase,location,stuff,stuff_status,type,input_pids,input_str,is_fenxiao,is_virtual";

    public static final String FIELDS_ALL_WITH_FOODSECURITY = "property_alias,props,props_name,seller_cids,detail_url,"
            + "approve_status,num_iid,title,nick,cid,sku,pic_url,num,price,list_time,delist_time,outer_id,desc,modified,"
            + "has_showcase,location,stuff,stuff_status,type,input_pids,input_str,food_security,is_fenxiao,is_virtual,wireless_desc";

    public static String[] wirelessFields = new String[] {
            "detail_url", "num_iid", "title", "nick", "type", "desc", "sku", "props_name", "created",
            "promoted_service", "is_lightning_consignment", "is_fenxiao", "auction_point", "property_alias",
            "template_id", "after_sale_id", "is_xinpin", "sub_stock", "inner_shop_auction_template_id",
            "outer_shop_auction_template_id", "food_security", "features",
            "desc_module_info", "desc_module",
            //            "locality_life", "wap_desc", "wap_detail_url",
            "item_weight", "item_size", "with_hold_quantity", "change_prop", "delivery_time", "paimai_info",
            "sell_point", "valid_thru", "outer_id", "auto_fill", "custom_made_type_id",
            "wireless_desc", "is_offline", "barcode", "is_cspu", "newprepay", "global_stock_type",
            "global_stock_country", "cid", "seller_cids", "props", "input_pids", "input_str", "pic_url", "num",
            "list_time", "delist_time", "stuff_status", "location", "price", "post_fee", "express_fee", "ems_fee",
            "has_discount", "freight_payer", "has_invoice", "has_warranty", "has_showcase", "modified", "increment",
            "approve_status", "postage_id", "product_id", "item_img", "item_img", "prop_img", "is_virtual",
            "is_taobao", "is_ex",
            "is_timing", "video", "is_3D", "score", "one_station", "second_kill", "violation", "is_prepay",
            "ww_status", "cod_postage_id", "sell_promise"

    };

    public static final String WIRELESS_FIELDS = StringUtils.join(wirelessFields, ',');

    public static final String FIELD_WIRELESS_NEEDED = "property_alias,props_name,wireless_desc,num_iid,title,cid,desc,is_fenxiao,is_virtual";

    public static final String FIELDS_ONLY_ID_DESC = "num_iid,desc";

    public static final String FIELDS_ONLY_PROPS = "property_alias,props,props_name,num_iid,title,nick,cid";

    public static class ItemsOnsale extends TBApi<ItemsOnsaleGetRequest, ItemsOnsaleGetResponse, List<Item>> {

        public User user;

        public Long startModified;

        public Long endModified;

        public boolean hasInit = false;

        public long pageNo = 1;

        public List<Item> resList;

        private String field = FIELDS;

        public ItemsOnsale(User user, String field) {
            super(user.getSessionKey());
            this.user = user;
            this.resList = new ArrayList<Item>();
            this.field = field;
        }

        public ItemsOnsale(User user, Long startModified, Long endModified) {
            super(user.getSessionKey());
            this.user = user;
            this.startModified = startModified;
            this.endModified = endModified;
            this.resList = new ArrayList<Item>();
        }

        @Override
        public ItemsOnsaleGetRequest prepareRequest() {
            ItemsOnsaleGetRequest req = new ItemsOnsaleGetRequest();

            req.setPageNo(pageNo++);
            req.setFields(field);
            req.setPageSize(PageSize.API_ITEM_PAGE_SIZE);

            if (startModified != null && startModified > 0) {
                req.setStartModified(new Date(startModified));
            }
            if (endModified != null && endModified > 0) {
                req.setEndModified(new Date(endModified));
            }

            return req;
        }

        @Override
        public List<Item> validResponse(ItemsOnsaleGetResponse resp) {

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

            if (!hasInit) {
                long totalResult = resp.getTotalResults();
                if (totalResult > 10000) {
                    totalResult = 10000;
                }
                this.iteratorTime = (int) CommonUtils.calculatePageCount(totalResult, PageSize.API_ITEM_PAGE_SIZE) - 1;
                this.hasInit = true;
            }
            return resp.getItems() == null ? ListUtils.EMPTY_LIST : resp.getItems();
        }

        @Override
        public List<Item> applyResult(List<Item> res) {

            if (res == null) {
                return resList;
            }

            resList.addAll(res);
            // ItemWritter.addItemList(user.getId(), res);

            return resList;

        }

    }

    public static class ItemsOnsaleCount extends TBApi<ItemsOnsaleGetRequest, ItemsOnsaleGetResponse, Long> {

        public Long startModified;

        public Long endModified;

        public ItemsOnsaleCount(String sid, Long startModified, Long endModified) {
            super(sid);
            this.startModified = startModified;
            this.endModified = endModified;
        }

        public ItemsOnsaleCount(User user, Long startModified, Long endModified) {
            super(user.getSessionKey());
            this.startModified = startModified;
            this.endModified = endModified;
        }

        @Override
        protected ItemsOnsaleGetResponse execProcess() throws ApiException {
            return super.validItemOnSaleResp();
        }

        @Override
        public ItemsOnsaleGetRequest prepareRequest() {
            ItemsOnsaleGetRequest req = new ItemsOnsaleGetRequest();

            req.setPageNo(1L);
            req.setFields("num_iid");
            req.setPageSize(1L);

            if (startModified != null && startModified > 0) {
                req.setStartModified(new Date(startModified));
            }
            if (endModified != null && endModified > 0) {
                req.setEndModified(new Date(endModified));
            }

            return req;
        }

        @Override
        public Long validResponse(ItemsOnsaleGetResponse resp) {

            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }
            ErrorHandler.validTaoBaoResp(resp);
            if (!resp.isSuccess()) {
                return null;
            }
            return resp.getTotalResults();
        }

        @Override
        public Long applyResult(Long res) {
            return res;
        }

    }

    public static class ItemsInventoryCount extends TBApi<ItemsInventoryGetRequest, ItemsInventoryGetResponse, Long> {

        public User user;

        public Date startModified;

        public Date endModified;

        public boolean hasInit = false;

        public long pageNo = 1;

        public ItemsInventoryCount(User user, Date startModified, Date endModified) {
            super(user.getSessionKey());
            this.user = user;
            this.startModified = startModified;
            this.endModified = endModified;
        }

        public ItemsInventoryCount(User user, long startModified, long endModified) {
            super(user.getSessionKey());
            this.user = user;
            this.startModified = new Date(startModified);
            this.endModified = new Date(endModified);
        }

        @Override
        public ItemsInventoryGetRequest prepareRequest() {
            ItemsInventoryGetRequest req = new ItemsInventoryGetRequest();

            req.setPageNo(1L);
            req.setFields("num_iid");
            req.setPageSize(1L);

            if (startModified != null) {
                req.setStartModified(startModified);
            }
            if (endModified != null) {
                req.setEndModified(endModified);
            }

            return req;
        }

        @Override
        public Long validResponse(ItemsInventoryGetResponse resp) {

            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }
            ErrorHandler.validTaoBaoResp(resp);
            if (!resp.isSuccess()) {
                return -1L;
            }
            return resp.getTotalResults();
        }

        @Override
        protected ItemsInventoryGetResponse execProcess() throws ApiException {

            try {
                req.check();// if check failed,will throw ApiRuleException.
            } catch (ApiRuleException e) {
                ItemsInventoryGetResponse localResponse = null;
                try {
                    localResponse = new ItemsInventoryGetResponse();
                } catch (Exception e2) {
                    throw new ApiException(e2);
                }
                localResponse.setErrorCode(e.getErrCode());
                localResponse.setMsg(e.getErrMsg());
                // localResponse.setBody("this.");
                return localResponse;
            }
            ItemsInventoryGetResponse localResponse = new ItemsInventoryGetResponse();
            Map<String, Object> rt = ((DefaultTaobaoClient) client).doPost(req, sid);
            try {
                JSONObject first = new JSONObject(rt.get("rsp").toString());
                if (!first.has("items_inventory_get_response")) {
                    log.warn("no resp????:" + rt);
                    localResponse.setTotalResults(-1L);
                    return localResponse;
                }

                JSONObject obj = first.getJSONObject("items_inventory_get_response");
                if (obj.has("total_results")) {
                    Long totalNum = obj.getLong("total_results");
                    localResponse.setTotalResults(totalNum);
                } else {
                    localResponse.setTotalResults(NumberUtil.DEFAULT_LONG);
                }
            } catch (JSONException e) {
                log.warn(e.getMessage(), e);
            }
            return localResponse;
        }

        @Override
        public Long applyResult(Long res) {
            return res;
        }

    }

    public static class ItemsOnsalePage extends TBApi<ItemsOnsaleGetRequest, ItemsOnsaleGetResponse, List<Item>> {

        public User user;

        public Long startModified;

        public Long endModified;

        public Long pageSize = PageSize.API_ITEM_PAGE_SIZE;

        public long pageNo;

        public ItemsOnsalePage(User user, Long startModified, Long endModified, Long pageNo) {
            super(user.getSessionKey());
            this.user = user;
            this.startModified = startModified;
            this.endModified = endModified;
            this.pageNo = pageNo;
        }

        public ItemsOnsalePage(User user, Long pageNo, Long pageSize, String field) {
            super(user.getSessionKey());
            this.user = user;
            this.startModified = 0L;
            this.endModified = 0L;
            this.pageNo = pageNo;
            this.pageSize = pageSize;
            this.fields = field;
        }

        public ItemsOnsalePage(User user, Long startModified, Long endModified, Long pageNo, Long pageSize) {
            super(user.getSessionKey());
            this.user = user;
            this.startModified = startModified;
            this.endModified = endModified;
            this.pageNo = pageNo;
            this.pageSize = pageSize;
        }

//        public ItemsOnsalePage(String sid, User user, Long startModified, Long endModified, long pageNo, Long pageSize) {
//            super(sid);
//            this.user = user;
//            this.startModified = startModified;
//            this.endModified = endModified;
//            this.pageNo = pageNo;
//            this.pageSize = pageSize;
//        }

        private String fields = FIELDS_WITH_PROPS;

        @Override
        public ItemsOnsaleGetRequest prepareRequest() {
            this.retryTime = 3;

            ItemsOnsaleGetRequest req = new ItemsOnsaleGetRequest();

            req.setPageNo(pageNo);
            req.setFields(fields);
            req.setPageSize(this.pageSize);

            if (startModified != null && startModified > 0) {
                req.setStartModified(new Date(startModified));
            }
            if (endModified != null && endModified > 0) {
                req.setEndModified(new Date(endModified));
            }

            // log.info("[page size :]" + this.pageSize);
            return req;
        }

        @Override
        public List<Item> validResponse(ItemsOnsaleGetResponse resp) {
            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }
            // log.error("new resp :" + new Gson().toJson(resp.getParams()));
            ErrorHandler.validTaoBaoResp(resp);
            if (!resp.isSuccess()) {
                return null;
            }

            List<Item> items = resp.getItems();
            if (CommonUtils.isEmpty(items)) {
                return ListUtils.EMPTY_LIST;
            }
            if (!APIConfig.get().isItemScoreRelated()) {
                return items;
            }

            return items;
        }

        @Override
        public List<Item> applyResult(List<Item> res) {
            return res;
        }

    }

    public static class MultiItemsListGet extends TBApi<ItemsListGetRequest, ItemsListGetResponse, List<Item>> {

        public static final int MAX_NUMIID_LENGTH = 20;

        List<Long> numIidList;

        List<List<Long>> splitToSubLongList = ListUtils.EMPTY_LIST;

        List<Item> resList = new ArrayList<Item>();

        boolean getProps;

        String currFields = null;

        int pageSize = MAX_NUMIID_LENGTH;

        public MultiItemsListGet(String sid, Collection<Long> numIidList) {
            super(sid);
            this.numIidList = new ArrayList<Long>(numIidList);
            this.splitToSubLongList = SplitUtils.splitToSubLongList(numIidList, pageSize);
            this.iteratorTime = splitToSubLongList.size();
            this.retryTime = 3;
            this.currFields = FIELDS;
        }

        public MultiItemsListGet(String sid, Collection<Long> numIidList, boolean getProps) {
            this(sid, numIidList);
            this.getProps = getProps;
            if (getProps) {
                currFields = FIELDS_WITH_PROPS;
            }
        }

        public MultiItemsListGet(String sid, Collection<Long> numIidList, String apiFields) {
            this(sid, numIidList);
            this.currFields = apiFields;
        }

        public MultiItemsListGet(String sid, Collection<Long> numIidList, String apiFields, int pageSize) {
            this(sid, numIidList, apiFields);
            this.pageSize = pageSize;
            this.currFields = apiFields;
            this.splitToSubLongList = SplitUtils.splitToSubLongList(numIidList, pageSize);
            this.iteratorTime = splitToSubLongList.size();
        }

        public MultiItemsListGet(User user, Collection<Long> numIidList, String apiFields, int pageSize) {
            this(user.getSessionKey(), numIidList, apiFields);
            this.pageSize = pageSize;
            this.splitToSubLongList = SplitUtils.splitToSubLongList(numIidList, pageSize);
            this.iteratorTime = splitToSubLongList.size();
            this.currFields = apiFields;
        }

        @Override
        public ItemsListGetRequest prepareRequest() {
            ItemsListGetRequest req = new ItemsListGetRequest();
            String numIids = StringUtils.join(splitToSubLongList.get(iteratorTime), ',');
            req.setNumIids(numIids);
            req.setFields(currFields);
            log.info("[fields:]" + currFields);

            return req;
        }

        @Override
        protected ItemsListGetResponse execProcess() throws ApiException {
            return validOldItemListResp();
        }

        @Override
        public List<Item> validResponse(ItemsListGetResponse resp) {
            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }

            if (!resp.isSuccess()) {
                if ("isv.item-is-delete:invalid-numIid-or-iid".equals(resp.getSubCode())) {
                    // TODO This one is deleted......
                }
            }

            for (Item item : resp.getItems()) {
//                log.info("[back items:]" + item.getWirelessDesc());
            }
            return resp.getItems();
        }

        @Override
        public List<Item> applyResult(List<Item> res) {
            if (res == null) {
                return resList;
            }

            resList.addAll(res);
            return resList;
        }
    }

    public static void setItemScore(User user, Collection<Item> list) {
        if (CommonUtils.isEmpty(list)) {
            return;
        }
        Map<Long, Item> srcMap = new HashMap<Long, Item>();
        for (Item item : list) {
            srcMap.put(item.getNumIid(), item);
        }
        List<Item> propItems = ApiJdpAdapter.get(user).tryItemList(user, srcMap.keySet());
        for (Item item : propItems) {
            Item src = srcMap.get(item.getNumIid());
            if (src == null) {
                continue;
            }
            src.setProps(item.getProps());
            src.setPropsName(item.getPropsName());
            src.setPropertyAlias(item.getPropertyAlias());
        }
    }

    public static class ItemsInventory extends TBApi<ItemsInventoryGetRequest, ItemsInventoryGetResponse, List<Item>> {

        public User user;

        public Date startModified;

        public Date endModified;

        public boolean hasInit = false;

        public long pageNo = 1;

        public List<Item> resList;

        public ItemsInventory(User user, Date startModified, Date endModified) {
            super(user.getSessionKey());
            this.user = user;
            this.startModified = startModified;
            this.endModified = endModified;

            resList = new ArrayList<Item>();
        }

        public ItemsInventory(User user, String field) {

            super(user.getSessionKey());
            this.resList = new ArrayList<Item>();
            this.field = field;
            this.user = user;
        }

        public ItemsInventory(User user, long startModified, long endModified) {
            super(user.getSessionKey());
            this.user = user;
            this.startModified = new Date(startModified);
            this.endModified = new Date(endModified);
            this.resList = new ArrayList<Item>();
        }

        String field = FIELDS_WITH_PROPS;

        @Override
        public ItemsInventoryGetRequest prepareRequest() {
            ItemsInventoryGetRequest req = new ItemsInventoryGetRequest();

            req.setPageNo(pageNo++);
            req.setFields(field);
            req.setPageSize(PageSize.API_ITEM_PAGE_SIZE);

            if (startModified != null) {
                req.setStartModified(startModified);
            }
            if (endModified != null) {
                req.setEndModified(endModified);
            }

            return req;
        }

        @Override
        public List<Item> validResponse(ItemsInventoryGetResponse resp) {

            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }

            ErrorHandler.validTaoBaoResp(resp);

            if (resp.getTotalResults() == null) {
                return null;
            }

            if (!hasInit) {
                long totalResult = resp.getTotalResults();
                if (totalResult > 10000) {
                    totalResult = 10000;
                }
                this.iteratorTime = (int) CommonUtils.calculatePageCount(totalResult, PageSize.API_ITEM_PAGE_SIZE) - 1;
                this.hasInit = true;
            }

            List<Item> items = resp.getItems();
            if (CommonUtils.isEmpty(items)) {
                return ListUtils.EMPTY_LIST;
            }
            if (!APIConfig.get().isItemScoreRelated()) {
                return items;
            }

            // setItemScore(user, items);
            // for (Item item : items) {
            // int score;
            // try {
            // score = DiagAction.doDiag(user, item, null).getScore();
            // item.setScore(new Long(score));
            // } catch (ClientException e) {
            // log.warn(e.getMessage(), e);
            // item.setScore(80L);
            // }
            // }

            return items;
        }

        @Override
        public List<Item> applyResult(List<Item> res) {

            if (res == null) {
                return resList;
            }
            resList.addAll(res);
            return resList;
        }
    }

    public static class ItemsInventoryPage extends
            TBApi<ItemsInventoryGetRequest, ItemsInventoryGetResponse, List<Item>> {

        public User user;

        public Long startModified;

        public Long endModified;

        public Long pageSize = PageSize.API_ITEM_PAGE_SIZE;

        public long pageNo;

        public ItemsInventoryPage(User user, Long startModified, Long endModified, Long pageNo) {
            super(user.getSessionKey());
            this.user = user;
            this.startModified = startModified;
            this.endModified = endModified;
            this.pageNo = pageNo;
        }

        public ItemsInventoryPage(User user, Long startModified, Long endModified, Long pageNo, Long pageSize) {
            super(user.getSessionKey());
            this.user = user;
            this.startModified = startModified;
            this.endModified = endModified;
            this.pageNo = pageNo;
            this.pageSize = pageSize;
        }

        public ItemsInventoryPage(String sid, User user, Long startModified, Long endModified, long pageNo,
                Long pageSize) {
            super(sid);
            this.user = user;
            this.startModified = startModified;
            this.endModified = endModified;
            this.pageNo = pageNo;
            this.pageSize = pageSize;
        }

        private String fields = FIELDS_WITH_PROPS;

        @Override
        public ItemsInventoryGetRequest prepareRequest() {
            ItemsInventoryGetRequest req = new ItemsInventoryGetRequest();

            req.setPageNo(pageNo);
            req.setFields(FIELDS_WITH_PROPS);
            req.setPageSize(this.pageSize);

            if (startModified != null && startModified > 0) {
                req.setStartModified(new Date(startModified));
            }
            if (endModified != null && endModified > 0) {
                req.setEndModified(new Date(endModified));
            }

            // log.info("[page size :]" + this.pageSize);
            return req;
        }

        @Override
        public List<Item> validResponse(ItemsInventoryGetResponse resp) {
            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }
            ErrorHandler.validTaoBaoResp(resp);

            List<Item> items = resp.getItems();
            if (CommonUtils.isEmpty(items)) {
                return ListUtils.EMPTY_LIST;
            }
            if (!APIConfig.get().isItemScoreRelated()) {
                return items;
            }
            // setItemScore(user, items);
            // for (Item item : items) {
            // int score;
            // try {
            // // log.info("[raw prop:]" + item.getPropsName());
            // DiagResult res = DiagAction.doDiag(user, item, null);
            // item.setScore(res == null ? 60L : res.getScore());
            //
            // } catch (ClientException e) {
            // log.warn(e.getMessage(), e);
            // item.setScore(60L);
            // }
            // }

            return items;
        }

        @Override
        public List<Item> applyResult(List<Item> res) {
            return res;
        }

    }

    public static class ItemFullGet extends ItemGet {
        public ItemFullGet(User user, Long numIid) {
            super(user, numIid);
        }

        public ItemFullGet(Long numIid) {
            super(numIid);
        }

        public ItemFullGet(User user, Long numIid, String apiFields) {
            super(user, numIid);
            this.apiFields = apiFields;
        }

        String apiFields = WIRELESS_FIELDS;

        @Override
        public ItemGetRequest prepareRequest() {
            ItemGetRequest req = new ItemGetRequest();
            req.setFields(apiFields);
            req.setNumIid(numIid);
            return req;
        }
    }

    public static class ItemPropPictureAdd extends TBApi<ItemPropimgUploadRequest, ItemPropimgUploadResponse, PropImg> {
        PropImg origin;

        User user;

        Long numIid;

        public ItemPropPictureAdd(User user, Long numIid, PropImg origin) {
            super(user.getSessionKey());
            this.origin = origin;
            this.user = user;
            this.numIid = numIid;
        }

        @Override
        public ItemPropimgUploadRequest prepareRequest() {
            ItemPropimgUploadRequest req = new ItemPropimgUploadRequest();
            FileItem fItem = fetchUrl(origin.getUrl(), origin.getId() + "_" + origin.getPosition());
            log.info("[f item:]" + fItem);
            req.setNumIid(numIid);
            req.setProperties(origin.getProperties());
            req.setImage(fItem);
//            req.setId(origin.getId());
            req.setPosition(origin.getPosition());

            return req;
        }

        @Override
        public PropImg validResponse(ItemPropimgUploadResponse resp) {
            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }
            ErrorHandler.validTaoBaoResp(this, resp);
            return resp.getPropImg();
        }

        @Override
        public PropImg applyResult(PropImg res) {
            return res;
        }

    }

    private static File copyDir = null;

    private static File genCopyDir() {
        if (copyDir != null) {
            return copyDir;
        }

        copyDir = new File(Play.tmpDir, "copy_dir");
        if (!copyDir.exists()) {
            copyDir.mkdir();
        }
        return copyDir;
    }

    public static FileItem fetchUrl(String url, String title) {

        try {
            String format = null;
            if (url.endsWith("gif")) {
                format = "gif";
            } else if (url.endsWith("jpg")) {
                format = "jpg";
            } else if (url.endsWith("png")) {
                format = "png";
            } else if (url.endsWith("jpeg")) {
                format = "jpeg";
            } else {
                format = "jpg";
            }
            File dest = new File(genCopyDir(), System.currentTimeMillis() + "_" + title + "." + format);
            dest.createNewFile();
            FileUtils.copyURLToFile(new URL(url), dest);
            FileItem fItem = new FileItem(dest);
            log.info("[create file :]" + dest);
            return fItem;
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }

        return null;
    }

    public static class ItemImgPictureAdd extends TBApi<ItemImgUploadRequest, ItemImgUploadResponse, ItemImg> {

        ItemImg itemImg = null;

        User user;

        Long numIid;

        public ItemImgPictureAdd(User user, Long numIid, ItemImg itemImg) {
            super(user.getSessionKey());
            this.itemImg = itemImg;
            this.user = user;
            this.numIid = numIid;
        }

        @Override
        public ItemImgUploadRequest prepareRequest() {
            ItemImgUploadRequest req = new ItemImgUploadRequest();
            FileItem fItem = fetchUrl(itemImg.getUrl(), itemImg.getId() + "_" + itemImg.getPosition());
            if(fItem == null){
            	return null;
            }
            log.info("[f item:]" + fItem.getFileName());
            req.setImage(fItem);
            req.setPosition(itemImg.getPosition());
//            req.setId(itemImg.getId());
            req.setNumIid(numIid);
            return req;

        }

        @Override
        public ItemImg validResponse(ItemImgUploadResponse resp) {
            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }
            ErrorHandler.validTaoBaoResp(this, resp);
            ItemImg itemImg2 = resp.getItemImg();
            return itemImg2;
        }

        @Override
        public ItemImg applyResult(ItemImg res) {
            return res;
        }

    }

    public static class ItemPropsGet extends ItemGet {

        public ItemPropsGet(Long numIid) {
            super(numIid);
            this.numIid = numIid;
        }

        @Override
        public ItemGetRequest prepareRequest() {
            ItemGetRequest req = new ItemGetRequest();
            req.setFields(FIELDS_ONLY_PROPS);
            req.setNumIid(numIid);
            return req;
        }
    }

    public static class ItemIncrementGet extends
            TBApi<IncrementItemsGetRequest, IncrementItemsGetResponse, List<NotifyItem>> {
        Date start = null;

        Date end = null;

        User user;

        public boolean hasInit = false;

        public long pageNo = 1;

        long pageSize = PageSize.API_ITEM_PAGE_SIZE;

        public List<NotifyItem> resList = new ArrayList<NotifyItem>();

        String field = "ItemAdd,ItemUpshelf,ItemDownshelf,ItemDelete,"
                + "ItemUpdate,ItemZeroStock,ItemPunishDelete,ItemPunishDownshelf,ItemPunishCc";

        // String field = "ItemDelete,ItemPunishDelete";

        public ItemIncrementGet(User user, long start, long end, String field) {
            super(user.getSessionKey());
            this.start = new Date(start);
            this.end = new Date(end);
            this.user = user;
            this.field = field;
        }

        /**
         * 商品操作状态，默认查询所有状态的数据，除了默认值外，每次可查询多种状态， 每种状态间用英语逗号分隔。具体类型列表见： ItemAdd（新增商品） ItemUpshelf（上架商品，自动上架商品不能获取到增量信息）
         * ItemDownshelf（下架商品） ItemDelete（删除商品） ItemUpdate（更新商品） ItemRecommendDelete（取消橱窗推荐商品） ItemRecommendAdd（橱窗推荐商品）
         * ItemZeroStock（商品卖空） ItemPunishDelete（小二删除商品） ItemPunishDownshelf（小二下架商品） ItemPunishCc（小二CC商品）
         * ItemSkuZeroStock（商品SKU卖空） ItemStockChanged（修改商品库存）
         */
        @Override
        public IncrementItemsGetRequest prepareRequest() {
            IncrementItemsGetRequest req = new IncrementItemsGetRequest();
            req.setStatus(field);
            req.setPageNo(pageNo++);
            req.setPageSize(pageSize);
            req.setNick(user.getUserNick());
            return req;
        }

        @Override
        public List<NotifyItem> validResponse(IncrementItemsGetResponse resp) {
            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }
            ErrorHandler.validTaoBaoResp(resp);
            if (!resp.isSuccess()) {
                return null;
            }
            List<NotifyItem> nItems = resp.getNotifyItems();
            if (nItems == null || nItems.size() == 0) {
                return ListUtils.EMPTY_LIST;
            }
            return nItems;
        }

        @Override
        public List<NotifyItem> applyResult(List<NotifyItem> res) {
            if (res == null) {
                return resList;
            }

            resList.addAll(res);
            return resList;
        }

    }

    public static class ItemDeleteIncrementGet extends ItemIncrementGet {

        public ItemDeleteIncrementGet(User user, long start, long end) {
            super(user, start, end, "ItemDelete,ItemPunishDelete");
        }

    }

    public static class ItemGet extends TBApi<ItemGetRequest, ItemGetResponse, Item> {

        public static List<Item> fetchOneByOne(User user, Collection<Long> ids, boolean useProps) {
            List<Item> res = new ArrayList<Item>();
            for (Long long1 : ids) {
                Item item = ApiJdpAdapter.get(user).findItem(user, long1);
                res.add(item);
            }
            return res;
        }

        public Long numIid;

        public static int ITEM_GET_530 = 530;

        public ItemGet(User user, Long numIid) {

            super(user == null ? null : user.getSessionKey());
            this.user = user;
            this.numIid = numIid;
            this.retryTime = 1;
        }

        User user = null;

        boolean useProps = false;

        public ItemGet(Long numIid) {
            super();
            this.numIid = numIid;
        }

        public ItemGet(User user, Long numIid, boolean useProps) {
            super(user == null ? null : user.getSessionKey());
            this.user = user;
            this.numIid = numIid;
            this.useProps = useProps;
            this.retryTime = 3;
        }

//        public ItemGet(String sessionKey, Long numIid) {
//            super(sessionKey);
//            this.numIid = numIid;
//            this.retryTime = 1;
//        }

        @Override
        public ItemGetRequest prepareRequest() {
            ItemGetRequest req = new ItemGetRequest();
            req.setFields(useProps ? FIELDS_WITH_PROPS : FIELDS);
            req.setNumIid(numIid);
            return req;
        }

        @Override
        public ItemGetResponse execProcess() throws ApiException {
            return super.validOldItemGetResp();
        }

        @Override
        public Item validResponse(ItemGetResponse resp) {
            if (resp == null) {
                return null;
            }

            if (resp.isSuccess()) {
                return resp.getItem();
            }

            log.error("Error:" + new Gson().toJson(resp));
            log.error("Error Resp ErrorCode Msg :" + resp.getErrorCode());
            log.error("Error Resp Msg :" + resp.getMsg());

            if ("isv.item-is-delete:invalid-numIid-or-iid".equals(resp.getSubCode())) {
                log.warn(" delete  : " + numIid + " for user:" + user);
                if (user != null) {
                    DeleteItemJob.tryDeleteItem(user.getId(), numIid);
                }
                this.retryTime = 1;
            }

            return null;
        }

        @Override
        public Item applyResult(Item res) {
            return res;
        }
    }

    public static class ItemDescGet extends ItemGet {

        public ItemDescGet(User user, Long numIid) {
            super(user, numIid);
            this.retryTime = 2;
        }

        @Override
        public ItemGetRequest prepareRequest() {
            ItemGetRequest req = new ItemGetRequest();
            req.setFields(FIELDS_WITH_DESC);
            req.setNumIid(numIid);
            return req;
        }

    }
    
    
    public static class ItemDescModulesGet extends ItemGet {

        public ItemDescModulesGet(User user, Long numIid) {
            super(user, numIid);
        }

        @Override
        public ItemGetRequest prepareRequest() {
            ItemGetRequest req = new ItemGetRequest();
            req.setFields(FIELDS_WITH_DESC_MODULES);
            req.setNumIid(numIid);
            return req;
        }

    }
    

    public static List<Long> extractNumIidFromItem(List<Item> itemList) {
        if (CommonUtils.isEmpty(itemList)) {
            return null;
        }
        List<Long> numIidList = new ArrayList<Long>();
        for (Item item : itemList) {
            numIidList.add(item.getNumIid());
        }
        return numIidList;
    }

    public static List<Long> extractNumIidFromItemPlay(List<ItemPlay> itemList) {
        if (CommonUtils.isEmpty(itemList)) {
            return null;
        }
        List<Long> numIidList = new ArrayList<Long>();
        for (ItemPlay item : itemList) {
            numIidList.add(item.getNumIid());
        }
        return numIidList;
    }

    public static class ItemsListGet extends TBApi<ItemsListGetRequest, ItemsListGetResponse, List<Item>> {

        public static final int MAX_NUMIID_LENGTH = 18;

        List<Long> numIidList;

        List<List<Long>> splitToSubLongList = ListUtils.EMPTY_LIST;

        List<Item> resList = new ArrayList<Item>();

        // boolean getProps;

        private String field;

        public ItemsListGet(List<Long> numIidList, boolean getProps) {
            this(numIidList, "");
            this.field = getProps == true ? FIELDS_WITH_PROPS : FIELDS;
        }

        public ItemsListGet(List<Long> numIidList, String field) {
            super();
            this.numIidList = numIidList;
            this.splitToSubLongList = SplitUtils.splitToSubLongList(numIidList, MAX_NUMIID_LENGTH);
            this.iteratorTime = splitToSubLongList.size();
            this.field = field;
        }

        @Override
        public ItemsListGetRequest prepareRequest() {
            ItemsListGetRequest req = new ItemsListGetRequest();

            String numIids = StringUtils.join(splitToSubLongList.get(iteratorTime), ',');
            req.setNumIids(numIids);
            if (StringUtils.isEmpty(field)) {
                field = FIELDS;
            }
            req.setFields(field);

            return req;
        }

        @Override
        public List<Item> validResponse(ItemsListGetResponse resp) {
            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }

            ErrorHandler.validTaoBaoResp(resp);

            return resp.getItems();
        }

        @Override
        public List<Item> applyResult(List<Item> res) {
            if (res == null) {
                return resList;
            }

            resList.addAll(res);
            return resList;
        }
    }

    /**
     * 更新商品描述
     * 
     * @param sessionKey
     *            , num_iid, desc
     * @return
     */
    public static class ItemUpdate extends TBApi<ItemUpdateRequest, ItemUpdateResponse, Item> {

        public Long numIid;

        public String desc;

        public String errorMsg = StringUtils.EMPTY;

        public ItemUpdate(String sid, Long numIid, String desc) {
            super(sid);
            this.numIid = numIid;
            this.desc = desc;
        }

        @Override
        public ItemUpdateRequest prepareRequest() {
            ItemUpdateRequest req = new ItemUpdateRequest();

            req.setNumIid(numIid);
            req.setDesc(desc);

            return req;
        }

        
        
        @Override
        public Item validResponse(ItemUpdateResponse resp) {

            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }

            ErrorHandler.validTaoBaoResp(resp);

            if (resp.isSuccess()) {
                return resp.getItem();
            }

            errorMsg = ErrorHandler.CommonTaobaoHandler.getInstance().getErrorMsg(resp);

            return null;
        }

        @Override
        public Item applyResult(Item res) {
            return res;
        }

        public String getErrorMsg() {
            return this.errorMsg;
        }
    }
    
    public static class ItemDescModulesUpdater extends ItemUpdate {
        public String descModules;

        public ItemDescModulesUpdater(String sid, Long numIid, String descModules) {
            super(sid, numIid, null);
            this.descModules = descModules;
        }

        @Override
        public ItemUpdateRequest prepareRequest() {
            ItemUpdateRequest req = new ItemUpdateRequest();

            req.setNumIid(numIid);
            req.setDescModules(descModules);

            return req;
        }
    }

    public static class ItemTitleUpdater extends ItemUpdate {
        public String newTitle;

        public ItemTitleUpdater(String sid, Long numIid, String title) {
            super(sid, numIid, null);
            this.newTitle = title;
        }

        @Override
        public ItemUpdateRequest prepareRequest() {
            ItemUpdateRequest req = new ItemUpdateRequest();

            req.setNumIid(numIid);
            req.setTitle(newTitle);

            return req;
        }
    }
    

    public static class ItemNumUpdater extends ItemUpdate {
        public Long num;

        public ItemNumUpdater(String sid, Long numIid, Long num) {
            super(sid, numIid, null);
            this.num = num;
            this.retryTime = 2;
        }

        @Override
        public ItemUpdateRequest prepareRequest() {
            ItemUpdateRequest req = new ItemUpdateRequest();

            req.setNumIid(numIid);
            req.setNum(num);

            return req;
        }

        @Override
        public Item validResponse(ItemUpdateResponse resp) {

            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }

            ErrorHandler.validTaoBaoResp(resp);
            if (resp.isSuccess()) {
                return resp.getItem();
            }
            if ("isv.error-spu-tmall-disabled-tmall".equals(resp.getSubCode())) {
                this.retryTime = 0;
            }

            errorMsg = resp.getSubMsg();

            return null;
        }
    }

    public static class ItemSellerCidUpdater extends ItemUpdate {
        public String sellerCids;

        public ItemSellerCidUpdater(String sid, Long numIid, String sellerCids) {
            super(sid, numIid, null);
            this.sellerCids = sellerCids;
        }

        @Override
        public ItemUpdateRequest prepareRequest() {
            ItemUpdateRequest req = new ItemUpdateRequest();

            req.setNumIid(numIid);
            req.setSellerCids(sellerCids);

            return req;
        }

        @Override
        public Item validResponse(ItemUpdateResponse resp) {

            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }

            // ErrorHandler.validTaoBaoResp(resp);
            if (resp.isSuccess()) {
                return resp.getItem();
            }

            errorMsg = resp.getSubMsg();

            return null;
        }
    }

    public static class ItemCidUpdater extends ItemUpdate {
        public Long cid;

        public ItemCidUpdater(String sid, Long numIid, Long cid) {
            super(sid, numIid, null);
            this.cid = cid;
        }

        @Override
        public ItemUpdateRequest prepareRequest() {
            ItemUpdateRequest req = new ItemUpdateRequest();

            req.setNumIid(numIid);
            req.setCid(cid);

            return req;
        }

        @Override
        public Item validResponse(ItemUpdateResponse resp) {

            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }

            // ErrorHandler.validTaoBaoResp(resp);
            if (resp.isSuccess()) {
                return resp.getItem();
            }

            errorMsg = resp.getSubMsg();
            if (StringUtils.isEmpty(errorMsg)) {
                errorMsg = resp.getErrorCode() + ", " + resp.getSubCode()
                        + ", " + resp.getMsg();
            }

            return null;
        }
    }

    public static class ItemFullCarrier extends TBApi<ItemAddRequest, ItemAddResponse, Item> {
        public Item item;

        public String errorMsg = StringUtils.EMPTY;

        User user;

        public ItemFullCarrier(User user, Item item) {
            super(user.getSessionKey());
            this.item = item;
            this.user = user;
        }

        @Override
        public ItemAddRequest prepareRequest() {
            ItemAddRequest req = new ItemAddRequest();
            if (item == null) {
                return null;
            }

            if (item.getNum() < 999999L && item.getNum() > 0L) {
                req.setNum(item.getNum());
            } else {
                req.setNum(99999L);
            }
            req.setPrice(item.getPrice());
            req.setType(item.getType()); // fixed(一口价),auction(拍卖)
            req.setStuffStatus(item.getStuffStatus());
            req.setTitle(item.getTitle());

            log.info("[item localtion]" + item.getLocation());
            if (item.getLocation() != null) {
                req.setLocationState(item.getLocation().getState());
                req.setLocationCity(item.getLocation().getCity());
            }
            req.setApproveStatus("instock"); // instock,onsale
            req.setCid(item.getCid());
            if (user.isTmall()) {

                
                 //*  干掉品牌
                 
//                String props = item.getProps();
//                log.warn("oringin props:" + props);
//                log.warn("oringin props:" + item.getPropsName());
//                String brandSpan = ItemPropAction.parseBrandProp(props);
//                if (brandSpan != null) {
//                    String toReplace = ItemPropAction.getOneBrandVidDesc(user, item.getCid());
//                    props = props.replaceAll(brandSpan, toReplace);
//                    item.setProps(props);
//                    item.setInputPids(null);
//                    item.setInputStr(null);
//                }
//                log.warn("end props:" + props);
            }
            req.setProps(item.getProps());
            String propertyAlias = item.getPropertyAlias();
            final int maxPropertyAliasLength = 250;
            if (StringUtils.isEmpty(propertyAlias) == false && propertyAlias.length() > maxPropertyAliasLength) {
                propertyAlias = propertyAlias.substring(0, maxPropertyAliasLength);
                int index = propertyAlias.lastIndexOf(";");
                if (index > 0) {
                    propertyAlias = propertyAlias.substring(0, index + 1);
                } else {
                    propertyAlias = "";
                }
            }
            req.setPropertyAlias(propertyAlias);
            req.setInputPids(item.getInputPids());
            req.setInputStr(item.getInputStr());
            List<Sku> skus = item.getSkus();
            if (!CommonUtils.isEmpty(skus)) {
                List<String> sku_properties = new ArrayList<String>();
                List<Long> sku_quantities = new ArrayList<Long>();
                List<String> sku_prices = new ArrayList<String>();
                List<String> sku_outer_ids = new ArrayList<String>();
                for (Sku sku : skus) {
                    sku_properties.add(sku.getProperties());
                    sku_quantities.add(sku.getQuantity());
                    sku_prices.add(sku.getPrice());
                    sku_outer_ids.add(sku.getOuterId());
                }
                String skuProperties = StringUtils.join(sku_properties, ",");
                req.setSkuProperties(skuProperties);
                String skuQuantities = StringUtils.join(sku_quantities, ",");
                req.setSkuQuantities(skuQuantities);
                String skuPrices = StringUtils.join(sku_prices, ",");
                req.setSkuPrices(skuPrices);
                String skuOuterIds = StringUtils.join(sku_outer_ids, ",");
                req.setSkuOuterIds(skuOuterIds);
            }

            req.setFreightPayer(item.getFreightPayer());
            req.setValidThru(item.getValidThru());
            req.setHasInvoice(true);
            req.setHasWarranty(item.getHasWarranty());
            req.setHasShowcase(item.getHasShowcase());
            // req.setSellerCids(item.getSellerCids());
            req.setHasDiscount(item.getHasDiscount());
            log.warn("post fee:" + item.getPostFee() + " with express fee:" + item.getExpressFee());
            if ("0.00".equals(item.getPostFee())) {
                req.setPostFee("20.00");
            } else {
                req.setPostFee(item.getPostFee());
            }
            if ("0.00".equals(item.getExpressFee())) {
                req.setExpressFee("20.00");
            } else {
                req.setExpressFee(item.getExpressFee());
            }
            if ("0.00".equals(item.getEmsFee())) {
                req.setEmsFee("20.00");
            } else {
                req.setEmsFee(item.getEmsFee());
            }

            req.setIncrement(item.getIncrement());
            if (user != null && user.isTmall()) {
                if (item.getAuctionPoint() == null) {
                    req.setAuctionPoint(0L);
                } else {
                    req.setAuctionPoint(item.getAuctionPoint());
                }
            }
            // FileItem fItem = new FileItem(new File("fileLocation"));
            // req.setImage(fItem);

            if (item.getFoodSecurity() != null) {
                // 对食品添加food_security
                FoodSecurity foodSecurity = item.getFoodSecurity();
                req.setFoodSecurityPrdLicenseNo(foodSecurity.getPrdLicenseNo());
                req.setFoodSecurityDesignCode(foodSecurity.getDesignCode());
                req.setFoodSecurityFactory(foodSecurity.getFactory());
                req.setFoodSecurityFactorySite(foodSecurity.getFactorySite());
                req.setFoodSecurityContact(foodSecurity.getContact());
                req.setFoodSecurityMix(foodSecurity.getMix());
                req.setFoodSecurityPlanStorage(foodSecurity.getPlanStorage());
                req.setFoodSecurityPeriod(foodSecurity.getPeriod());
                req.setFoodSecurityFoodAdditive(foodSecurity.getFoodAdditive());
                req.setFoodSecuritySupplier(foodSecurity.getSupplier());
                req.setFoodSecurityProductDateStart(foodSecurity.getProductDateStart());
                req.setFoodSecurityProductDateEnd(foodSecurity.getProductDateEnd());
                req.setFoodSecurityStockDateStart(foodSecurity.getStockDateStart());
                req.setFoodSecurityStockDateEnd(foodSecurity.getStockDateEnd());
                req.setFoodSecurityHealthProductNo("国食健字J20050014");
            }
            if(user.getUserNick().equals("楚之小南") || user.getUserNick().equals("开心摇一摇00")
            		|| user.getUserNick().equals("suchangyu520") 
            		|| user.getUserNick().equals("wang8862")
            		|| user.getUserNick().equals("yaqiu1979")
            		|| user.getUserNick().equals("wang8862")
            		|| user.getUserNick().equals("yinchun450")
            		|| user.getUserNick().equals("梅子情")
            		|| user.getUserNick().equals("wang8862")
            		|| user.getUserNick().equals("hnyltxx")
            		|| user.getUserNick().equals("时煜琳")) {
            	req.setFoodSecurityPrdLicenseNo("");
                req.setFoodSecurityDesignCode("");
                req.setFoodSecurityFactory("秘鲁赫赛尔公司");
                req.setFoodSecurityFactorySite("AV.LOS FRUTALES NO.22");
                req.setFoodSecurityContact("4008200976");
                req.setFoodSecurityMix("365");
                req.setFoodSecurityPlanStorage("置阴凉干燥处");
                req.setFoodSecurityPeriod("365");
                req.setFoodSecurityFoodAdditive("无");
                req.setFoodSecuritySupplier("通用");
                req.setFoodSecurityProductDateStart("2014-02-21");
                req.setFoodSecurityProductDateEnd("2014-02-22");
                req.setFoodSecurityStockDateStart("2014-02-22");
                req.setFoodSecurityStockDateEnd("2014-06-22");
            	req.setFoodSecurityHealthProductNo("国食健字J20050014");
            }
            
            String picUrl = FileCarryUtils.uploadPicFromOnline(user, item.getPicUrl());
            req.setPicPath(picUrl);
            String desc = FileCarryUtils.filterDesc(user, item.getDesc());
            req.setDesc(desc);

            req.setLang("zh_CN");
            // req.setProductId(123456789L);

            return req;
        }

        @Override
        public Item validResponse(ItemAddResponse resp) {
            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }

            ErrorHandler.validTaoBaoResp(this, resp);
            if (resp.isSuccess()) {
                return resp.getItem();
            }

            log.warn(resp.getBody());
            log.error(resp.getMsg());

            errorMsg = resp.getSubMsg();

            return null;
        }

        @Override
        public Item applyResult(Item res) {
            return res;
        }
    }

    public static List<StringPair> getProps(User user, long numIid) {

        // log.info(format("getProps:numIid".replaceAll(", ", "=%s, ") + "=%s", numIid));
        if (numIid <= 0L) {
            return ListUtils.EMPTY_LIST;
        }

        Item item = ApiJdpAdapter.tryFetchSingleItem(user, numIid);
        // log.info("[back item :]" + new Gson().toJson(item));
        if (item == null) {
            return ListUtils.EMPTY_LIST;
        }

        return CiaoStringUtil.splitProp(item.getPropsName());
    }

    public static class ItemImageUpdater extends ItemUpdate {
        public File imgFile;

        public ItemImageUpdater(String sid, Long numIid, File imgFile) {
            super(sid, numIid, null);
            this.imgFile = imgFile;
        }

        @Override
        public ItemUpdateRequest prepareRequest() {
            ItemUpdateRequest req = new ItemUpdateRequest();

            req.setNumIid(numIid);
            FileItem fileItem = new FileItem(imgFile);
            req.setImage(fileItem);
            return req;
        }

        @Override
        public Item validResponse(ItemUpdateResponse resp) {

            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }

            // ErrorHandler.validTaoBaoResp(resp);
            if (resp.isSuccess()) {
                return resp.getItem();
            }

            errorMsg = resp.getSubMsg();

            return null;
        }
    }

    public static class ItemPriceUpdater extends ItemUpdate {
        public String priceStr;// 有些浮点数double不能精确表示

        public ItemPriceUpdater(String sid, Long numIid, String priceStr) {
            super(sid, numIid, null);
            this.priceStr = priceStr;
        }

        @Override
        public ItemUpdateRequest prepareRequest() {
            ItemUpdateRequest req = new ItemUpdateRequest();

            req.setNumIid(numIid);
            req.setPrice(priceStr);

            log.info("modify price for numIid: " + numIid + ", price: " + priceStr + "--------");
            return req;
        }

        @Override
        public Item validResponse(ItemUpdateResponse resp) {

            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }

            if (resp.isSuccess()) {
                return resp.getItem();
            }

            errorMsg = resp.getSubMsg();

            return null;

        }
    }

    //不能用。。。。。
    /*@Deprecated
    public static class ItemSkuPriceUpdater extends ItemUpdate {
        
        private String priceStr;// 有些浮点数double不能精确表示
        
        
        private String allSkuProperty;
        
        private String allSkuPrice;
        
        private String allSkuOutId;

        public ItemSkuPriceUpdater(String sid, Long numIid, String priceStr, List<SkuPriceBean> skuBeanList) {
            super(sid, numIid, null);
            this.priceStr = priceStr;
            
            if (CommonUtils.isEmpty(skuBeanList) == false) {
                List<String> skuPropertyList = new ArrayList<String>();
                List<String> skuPriceStrList = new ArrayList<String>();
                List<String> skuOutIdList = new ArrayList<String>();
                
                for (SkuPriceBean skuBean : skuBeanList) {
                    if (skuBean == null) {
                        continue;
                    }
                    skuPropertyList.add(skuBean.getProperties());
                    skuPriceStrList.add(skuBean.getSkuPrice().toString());
                    skuOutIdList.add("");
                }
                allSkuProperty = StringUtils.join(skuPropertyList, ",");
                allSkuPrice = StringUtils.join(skuPriceStrList, ",");
                allSkuOutId = StringUtils.join(skuOutIdList, ",");
            }
        }

        @Override
        public ItemUpdateRequest prepareRequest() {
            ItemUpdateRequest req = new ItemUpdateRequest();

            req.setNumIid(numIid);
            req.setPrice(priceStr);
            
            if (StringUtils.isEmpty(allSkuProperty) == false && StringUtils.isEmpty(allSkuPrice) == false) {
                req.setSkuProperties(allSkuProperty);
                req.setSkuPrices(allSkuPrice);
                req.setSkuOuterIds(allSkuOutId);
            }
            

            log.info("modify price for numIid: " + numIid + ", price: " + priceStr + "--------");
            return req;
        }

        @Override
        public Item validResponse(ItemUpdateResponse resp) {

            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }

            if (resp.isSuccess()) {
                return resp.getItem();
            }

            errorMsg = resp.getSubMsg();

            return null;

        }
    }*/
    

    public static List<Item> tryItemList(User user, Collection<Long> idsList, boolean useProps) {

        if (CommonUtils.isEmpty(idsList)) {
            return ListUtils.EMPTY_LIST;
        }

        List<Item> remoteItems = null;
        remoteItems = new MultiItemsListGet(user == null ? null : user.getSessionKey(), idsList, useProps).call();
        if (CommonUtils.isEmpty(remoteItems)) {
            remoteItems = ItemApi.ItemGet.fetchOneByOne(user, idsList, useProps);
        }

        return remoteItems;
    }

    public static Comparator<Item> ItemDelistAscComparator = new Comparator<Item>() {

        @Override
        public int compare(Item o1, Item o2) {
            long value = o2.getDelistTime().getTime() - o1.getDelistTime().getTime();
            if (value > 0L) {
                return -1;
            }
            if (value == 0L) {
                return 0;
            }
            return 1;
        }

    };

    public static Comparator<Item> ItemDelistDescComparator = new Comparator<Item>() {

        @Override
        public int compare(Item o1, Item o2) {
            long value = o1.getDelistTime().getTime() - o2.getDelistTime().getTime();
            if (value > 0L) {
                return -1;
            }
            if (value == 0L) {
                return 0;
            }
            return 1;
        }

    };

    public static Set<Long> allNumIids(User user) {

        Set<Long> ids = new HashSet<Long>();

        String field = "num_iid";
        List<Item> items = new ItemsInventory(user, field).call();
        if (!CommonUtils.isEmpty(items)) {
            for (Item item : items) {
                ids.add(item.getNumIid());
            }
        }
        items = new ItemsOnsale(user, field).call();
        if (!CommonUtils.isEmpty(items)) {
            for (Item item : items) {
                ids.add(item.getNumIid());
            }
        }
        log.info("[user item api num:]" + ids.size());
        return ids;
    }

}
