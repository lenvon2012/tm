
package bustbapi;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import jdp.ApiJdpAdapter.OriginApiImpl;
import job.showwindow.ShowWindowInitJob.ShowCaseInfo;
import models.showwindow.ShowwindowExcludeItem;
import models.showwindow.ShowwindowTmallTotalNumFixedNum;
import models.user.User;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.SetUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import play.jobs.Job;

import com.ciaosir.client.CommonUtils;
import com.google.gson.Gson;
import com.taobao.api.ApiException;
import com.taobao.api.domain.Item;
import com.taobao.api.request.ItemsInventoryGetRequest;
import com.taobao.api.request.ItemsOnsaleGetRequest;
import com.taobao.api.response.ItemsInventoryGetResponse;
import com.taobao.api.response.ItemsOnsaleGetResponse;

import configs.TMConfigs.PageSize;
import dao.UserDao;
import dao.item.ItemDao;

public class OperateItemApi {

    public final static Logger log = LoggerFactory.getLogger(ItemApi.class);

    public static final String DEFAULT_SORT = "delist_time:asc";

    public static final String DELIST_DESC_SORT = "delist_time:desc";

    public static final String SIMPLE_FIELDS_WITH_SHOWCASE = "num_iid,list_time," +
            "delist_time,has_showcase,is_virtual,auto_fill,num";

//            "delist_time,is_virtual,auto_fill,num";

    public static final String SIMPLE_FIELDS = "num_iid,list_time,delist_time,num";

    public static final String ITEM_OPRATE_FIELDS = "seller_cids,detail_url,approve_status,num_iid,"
            +
            "has_showcase,title,nick,cid,pic_url,num,price,list_time,delist_time,outer_id,has_showcase,is_virtual,auto_fill";

//            "title,nick,cid,pic_url,num,price,list_time,delist_time,outer_id,has_showcase,is_virtual,auto_fill";

    public static class ItemsOnsale extends TBApi<ItemsOnsaleGetRequest, ItemsOnsaleGetResponse, List<Item>> {

        public User user;

        public Date startModified;

        public Date endModified;

        public String orderBy = DEFAULT_SORT;

        public boolean hasInit = false;

        public long pageNo = 1;

        public List<Item> resList = new ArrayList<Item>();

        int max = Integer.MAX_VALUE;

        boolean onlyNumIidAndDelistNeeded = false;

        long pageSize = PageSize.API_ITEM_PAGE_SIZE;

        public ItemsOnsale(User user, int max) {
            super(user.getSessionKey());
            this.user = user;
            this.resList = new ArrayList<Item>();
            this.max = max;
            if (max < pageSize) {
                pageSize = max;
            }
        }

        public ItemsOnsale(User user, int max, boolean onlyNumIidAndDelist) {
            this(user, max);
            this.onlyNumIidAndDelistNeeded = onlyNumIidAndDelist;
        }

        public ItemsOnsale(User user, int max, boolean onlyNumIidAndDelist, long start, long end) {
            this(user, max);
            this.onlyNumIidAndDelistNeeded = onlyNumIidAndDelist;
            this.startModified = new Date(start);
            this.endModified = new Date(end);
        }

        @Override
        public ItemsOnsaleGetRequest prepareRequest() {
            ItemsOnsaleGetRequest req = new ItemsOnsaleGetRequest();

            String fields = this.onlyNumIidAndDelistNeeded ? SIMPLE_FIELDS : ITEM_OPRATE_FIELDS;
//            log.info("[fields:]" + fields);
            req.setPageNo(pageNo++);
            req.setFields(fields);
            req.setOrderBy(orderBy);
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
        protected ItemsOnsaleGetResponse execProcess() throws ApiException {
            return super.validItemOnSaleResp();
        }

        @Override
        public List<Item> validResponse(ItemsOnsaleGetResponse resp) {

            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }

            if (!resp.isSuccess()) {

                int errorCode = Integer.parseInt(resp.getErrorCode());
                String Msg = resp.getMsg();
                String subErrorCode = resp.getSubCode();
                String subMsg = resp.getSubMsg();

                log.info("user: " + user.id + " ,Error Code: " + errorCode);
                log.info("user: " + user.id + ",Msg: " + Msg);
                log.info("user: " + user.id + ",Sub Error Code: " + subErrorCode);
                log.info("user: " + user.id + ",Sub Msg: " + subMsg);

                // 空subMsg
                if (subMsg == null || subMsg.trim().equals("")) {
                    subMsg = resp.getErrorCode() + "," + Msg;
                }

                // 有些错误不要重试
                if ((errorCode > 100 || errorCode == 15) && subErrorCode.startsWith("isv")) {
                    retryTime = 1;
                    log.info("item.get set retryTime=1 for user onsale.get:" + user.id + " ,errorCode: " + errorCode);
                }

                if ((errorCode == 27) && subErrorCode.equals("session-expired")) {
                    // 不重试
                    retryTime = 1;
                    log.info("onsale.get set retryTime=1 for user:" + user.id);
                    log.info("delete all item plan of user: " + user.id);

                    log.info("user:" + user.id + ",过期用户！ delete all item plan of user: " + user.id);

                    return null;
                }

                else {

//                        AutoListingDao.addAutoListingLog(user.id, null, "onsale.get error:" + subMsg, true, appKey);

                    return null;
                }

            }

            if (!hasInit) {
                long totalResult = resp.getTotalResults() == null ? 0 : resp.getTotalResults();

                this.iteratorTime = (int) CommonUtils.calculatePageCount(totalResult, PageSize.API_ITEM_PAGE_SIZE) - 1;
                this.hasInit = true;
            }
            List<Item> tmp = resp.getItems();
            return tmp == null ? ListUtils.EMPTY_LIST : tmp;

        }

        @Override
        public List<Item> applyResult(List<Item> res) {

            if (res == null) {
                log.info("res is null!");
                return resList;
            }

            resList.addAll(res);
            // ItemWritter.addItemList(user.getId(), res);

//            log.info("Apply result :" + user.id + ":Return size:" + res.size());
            if (resList.size() >= max) {
                this.iteratorTime = 0;
            }
            return resList;

        }

    }

    public static class ItemOnShowcaseCount extends TBApi<ItemsOnsaleGetRequest, ItemsOnsaleGetResponse, Integer> {

        public ItemOnShowcaseCount(User user) {
            super(user.getSessionKey());
            this.count = 0;
            this.user = user;
        }

        public User user;

        public boolean hasInit = false;

        public long pageNo = 1;

        public int count = 0;

        @Override
        public ItemsOnsaleGetRequest prepareRequest() {
            ItemsOnsaleGetRequest req = new ItemsOnsaleGetRequest();

            req.setPageNo(pageNo++);
            req.setFields("num_iid,has_showcase");
            req.setPageSize(PageSize.API_ITEM_PAGE_SIZE);
            req.setHasShowcase(true);

            return req;
        }

        @Override
        public Integer validResponse(ItemsOnsaleGetResponse resp) {
            if (!ErrorHandler.validResponseBoolean(resp)) {
                return null;
            }

//            log.error(" back item on window resp :" + new Gson().toJson(resp));

            return resp.getTotalResults() == null ? 0 : resp.getTotalResults().intValue();
        }

        @Override
        public Integer applyResult(Integer res) {
            return res;
        }

        @Override
        protected ItemsOnsaleGetResponse execProcess() throws ApiException {
            return validItemOnSaleResp();
        }

    }

    public static class OnWindowItemsDelistDesc extends
            TBApi<ItemsOnsaleGetRequest, ItemsOnsaleGetResponse, List<Item>> {

        public User user;

        public long max = 1L;

//        public OnWindowItemsDelistDesc(User user) {
//            super(user.getSessionKey());
//            this.user = user;
//        }

        public OnWindowItemsDelistDesc(User user, long max) {
            super(user.getSessionKey());
            this.user = user;
            this.max = max;
        }

        @Override
        public ItemsOnsaleGetRequest prepareRequest() {
            ItemsOnsaleGetRequest req = new ItemsOnsaleGetRequest();
            req.setPageNo(1L);
            req.setFields(SIMPLE_FIELDS_WITH_SHOWCASE);
            req.setOrderBy(DELIST_DESC_SORT);
            req.setPageSize(max);
            req.setHasShowcase(true);
            return req;
        }

        @Override
        public List<Item> validResponse(ItemsOnsaleGetResponse resp) {
            this.retryTime = 1;

            if (!resp.isSuccess()) {

                int errorCode = Integer.parseInt(resp.getErrorCode());
                // 有些错误不要重试
                if ((errorCode > 100 || errorCode == 15) && subErrorCode.startsWith("isv")) {
                    retryTime = 1;
                    log.info("item.get set retryTime=1 for user onsale.get:" + user.id + " ,errorCode: " + errorCode);
                }
                return null;

            }
            List<Item> items = resp.getItems();

            if (CommonUtils.isEmpty(items)) {
                log.error("no resp for on sale..????::" + new Gson().toJson(resp) + "  with delist req: "
                        + new Gson().toJson(req));
                items = ListUtils.EMPTY_LIST;
            }

            return items;
        }

        @Override
        public List<Item> applyResult(List<Item> res) {
            return res;
        }

        @Override
        protected ItemsOnsaleGetResponse execProcess() throws ApiException {
            return validItemOnSaleResp();
        }

    }

    public static class ItemRecentDownGet extends ItemsOnWindowInit {

        public ItemRecentDownGet(User user) {
            super(user, 200, false);
            this.hasShowcase = Boolean.FALSE;
        }

        public ItemRecentDownGet(User user, int maxFetchNum) {
            super(user, maxFetchNum, false);
            this.hasShowcase = Boolean.FALSE;
            this.max = maxFetchNum;
        }

    }

    public static class OnWindowNumIids extends
            TBApi<ItemsOnsaleGetRequest, ItemsOnsaleGetResponse, Set<Long>> {
        protected boolean hasInit = false;

        protected long pageNo = 1;

        static Long pageSize = 200L;

        private Set<Long> finalRes = new HashSet<Long>();

        private User user;

        public OnWindowNumIids(User user) {
            super(user.getSessionKey());
            this.user = user;
        }

        @Override
        public ItemsOnsaleGetRequest prepareRequest() {
            ItemsOnsaleGetRequest req = new ItemsOnsaleGetRequest();
            req.setPageNo(pageNo++);
            req.setFields("num_iid");
            req.setPageSize(pageSize);
            req.setHasShowcase(true);
            return req;
        }

        @Override
        public Set<Long> validResponse(ItemsOnsaleGetResponse resp) {

            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }

            if (!resp.isSuccess()) {

                ErrorHandler.validTaoBaoResp(this, resp);

                // 有些错误不要重试
                if (subErrorCode != null && subErrorCode.startsWith("isv")) {
//                    retryTime = 1;
                    log.info("item.get set retryTime=1 for user onsale.get:" + user.id + " ,errorCode: " + subErrorCode);
                    CommonUtils.sleepQuietly(200L);
                }

                return SetUtils.EMPTY_SET;

            }

            if (!hasInit) {
                long totalResult = resp.getTotalResults() == null ? 0L : resp.getTotalResults();
                this.iteratorTime = (int) CommonUtils.calculatePageCount(totalResult, pageSize) - 1;
                this.hasInit = true;
            }
            List<Item> tmp = resp.getItems();
            Set<Long> ids = new HashSet<Long>();
            if (!CommonUtils.isEmpty(tmp)) {
                for (Item item : tmp) {
                    ids.add(item.getNumIid());
                }
            }

            return ids;
        }

        @Override
        public Set<Long> applyResult(Set<Long> res) {
            finalRes.addAll(res);
            return finalRes;
        }

        @Override
        protected ItemsOnsaleGetResponse execProcess() throws ApiException {
            return super.validItemOnSaleResp();
        }

    }

    public static class ItemsOnWindowInit extends
            TBApi<ItemsOnsaleGetRequest, ItemsOnsaleGetResponse, List<Item>> {

        public User user;

        public Date startModified;

        public Date endModified;

        public String orderBy = DEFAULT_SORT;

        public Boolean hasShowcase = Boolean.FALSE;

        public boolean hasInit = false;

        public long pageNo = 1;

        public List<Item> resList = new ArrayList<Item>();

        protected boolean onlyNumIidDelistNeeded = false;

        long pageSize = PageSize.API_ITEM_PAGE_SIZE;

        int max = Integer.MAX_VALUE;

        public ItemsOnWindowInit(User user) {
            super(user.getSessionKey());
            this.user = user;
            this.resList = new ArrayList<Item>();
            this.hasShowcase = Boolean.TRUE;
        }

        public ItemsOnWindowInit(User user, int max, boolean onlyNumIidAndDelistNeeded) {
            super(user.getSessionKey());
            this.user = user;
            this.resList = new ArrayList<Item>();
            this.hasShowcase = Boolean.TRUE;
            this.onlyNumIidDelistNeeded = onlyNumIidAndDelistNeeded;
            if (max < pageSize) {
                pageSize = max;
            }
            this.max = max;
        }

        public ItemsOnWindowInit(User user, boolean onlyNumIidAndDelistNeeded) {
            super(user.getSessionKey());
            this.user = user;
            this.resList = new ArrayList<Item>();
            this.hasShowcase = Boolean.TRUE;
            this.onlyNumIidDelistNeeded = onlyNumIidAndDelistNeeded;
        }

        @Override
        public ItemsOnsaleGetRequest prepareRequest() {
            ItemsOnsaleGetRequest req = new ItemsOnsaleGetRequest();

            String field = onlyNumIidDelistNeeded ? SIMPLE_FIELDS_WITH_SHOWCASE : ITEM_OPRATE_FIELDS;
            req.setPageNo(pageNo++);
            req.setFields(field);
            req.setOrderBy(orderBy);
            req.setPageSize(pageSize);

            if (startModified != null) {
                req.setStartModified(startModified);
            }
            if (endModified != null) {
                req.setEndModified(endModified);
            }

            if (this.hasShowcase) {
                req.setHasShowcase(hasShowcase);
            }

            return req;
        }

        @Override
        protected ItemsOnsaleGetResponse execProcess() throws ApiException {
            return validItemOnSaleResp();
        }

        @Override
        public List<Item> validResponse(ItemsOnsaleGetResponse resp) {

            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }

            if (!resp.isSuccess()) {

                int errorCode = Integer.parseInt(resp.getErrorCode());
                String Msg = resp.getMsg();
                String subErrorCode = resp.getSubCode();
                String subMsg = resp.getSubMsg();

//                log.info("user: " + user.id + " ,Error Code: " + errorCode);
//                log.info("user: " + user.id + ",Msg: " + Msg);
//                log.info("user: " + user.id + ",Sub Error Code: " + subErrorCode);
//                log.info("user: " + user.id + ",Sub Msg: " + subMsg);

                // 空subMsg
                if (subMsg == null || subMsg.trim().equals("")) {
                    subMsg = resp.getErrorCode() + "," + Msg;
                }
//                log.info("subMsg for user:" + user.id + ", " + subMsg);

                // 有些错误不要重试
                if ((errorCode > 100 || errorCode == 15) && subErrorCode.startsWith("isv")) {
                    retryTime = 1;
//                    log.info("item.get set retryTime=1 for user onsale.get:" + user.id + " ,errorCode: " + errorCode);
                }
                return null;

            }

            if (!hasInit) {
                long totalResult = resp.getTotalResults() == null ? 0L : resp.getTotalResults();

                this.iteratorTime = (int) CommonUtils.calculatePageCount(totalResult, PageSize.API_ITEM_PAGE_SIZE) - 1;
                this.hasInit = true;
            }
            List<Item> tmp = resp.getItems();
            return tmp == null ? ListUtils.EMPTY_LIST : tmp;

        }

        @Override
        public List<Item> applyResult(List<Item> res) {

            if (res == null) {
                log.info("res is null!");
                return resList;
            }

            resList.addAll(res);
            if (resList.size() >= max) {
                resList = resList.subList(0, max);
                this.iteratorTime = 0;
            }
            // ItemWritter.addItemList(user.getId(), res);

            return resList;

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

        @Override
        public ItemsInventoryGetRequest prepareRequest() {
            ItemsInventoryGetRequest req = new ItemsInventoryGetRequest();

            req.setFields(ITEM_OPRATE_FIELDS);
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

            if (!resp.isSuccess()) {
                log.error("resp submsg" + resp.getSubMsg());
                log.error("resp error code " + resp.getErrorCode());
                log.error("resp Mesg " + resp.getMsg());
                return null;
            }

            if (!hasInit) {
                long totalResult = resp.getTotalResults();

                this.iteratorTime = (int) CommonUtils.calculatePageCount(totalResult, PageSize.API_ITEM_PAGE_SIZE) - 1;
                this.hasInit = true;
            }
            return resp.getItems();
        }

        @Override
        public List<Item> applyResult(List<Item> res) {

            if (res == null) {
                return resList;
            }

            resList.addAll(res);
            // ItemWritter.addItemList(user.getId(), res);
            log.info("Return size:" + res.size());

            return resList;

        }

    }

    public static boolean setUserTotalNum(User user, int totalNum) {

        if (totalNum < 0 || user == null) {
            return false;
        }
        String cacheKey = totoalNumCacheKey + user.getId();
        Cache.set(cacheKey, totalNum, (4 + (user.getId().longValue() / 64L % 4)) + "d");

        return true;
    }

    public static int getUserTotalWindowNum(User user) {
        if (user.isTmall()) {
            return ShowwindowTmallTotalNumFixedNum.findOrCreate(user);
        }

        String cacheKey = totoalNumCacheKey + user.getId();
        Integer num = (Integer) Cache.get(cacheKey);
        if (num != null) {
            return num.intValue();
        }

        ShowCaseInfo info = new ShowCaseInfo(user);
//        if (info == null) {
//            return 0;
//        }

        num = new Integer(info.getTotalWindowCount());
        Cache.set(cacheKey, num, "5d");

        return num.intValue();
    }

    static String TMALL_USER_WINDOW_COUNT_TAG = "_tmall_user_window_count";

    public static void setTmallUserWindowCount(User user, int count) {
        if (count <= 0) {
            return;
        }
        String tag = TMALL_USER_WINDOW_COUNT_TAG + user.getId();

        Cache.set(tag, count, "30d");
    }

    public static int getTmallUserWindowCount(User user) {
        String tag = TMALL_USER_WINDOW_COUNT_TAG + user.getId();
        Integer count = (Integer) Cache.get(tag);
        if (count != null) {
            return count.intValue();
        } else {
            return -1;
        }
    }

    static String NO_MORE_WINDOW_CANDIDATES_TAG = "_no_more_window_candidates";

    public static void clearNoMoreCandidatesCached(User user) {
        String key = NO_MORE_WINDOW_CANDIDATES_TAG + user.getId();
        Cache.delete(key);
    }

    public static boolean isNoMoreCandidatesCached(User user) {
        String key = NO_MORE_WINDOW_CANDIDATES_TAG + user.getId();
        Boolean res = (Boolean) Cache.get(key);
        if (res != null) {
            return res;
        }

        int windowTotalNum = getUserTotalWindowNum(user);
        int onSale = (int) ItemDao.countOnsaleItemByuserId(user.getId());
        if (onSale <= windowTotalNum) {
            res = Boolean.TRUE;
        } else {
            res = Boolean.FALSE;
        }

        Cache.set(key, res, (user.getId().intValue() % 8 + 5) + "d");
        return res.booleanValue();
    }

    private static String totoalNumCacheKey = "ShowWindowTotalNum_";

    @JsonAutoDetect
    public static class WirelessItemStatus implements Serializable {
        boolean isWindowNeeded = true;

        User user;

        public WirelessItemStatus(User user) {
            super();
            this.user = user;
        }

        public boolean isNoMoreCandidates() {

            return false;
        }

        public void build() {
            List<Item> items = OriginApiImpl.get().findRecentDownItems(user, 100);

            Set<Long> set = ShowwindowExcludeItem.findIdsByUser(user.getId());
            Iterator<Item> it = items.iterator();
            while (it.hasNext()) {
                Item item = it.next();
                if (set.contains(item.getNumIid())) {
                    it.remove();
                }
            }
            if (CommonUtils.isEmpty(items)) {
                isWindowNeeded = false;
            }

            Long saleCount = OriginApiImpl.get().onSaleItemNum(user);
            Long inventoryCountLong = OriginApiImpl.get().inventoryItemNum(user);

            ShowCaseInfo.build(user);
        }
    }

    public static class PrintUserNotToDoWindowStatus extends Job {
        int count = 0;

        public void doJob() {

            new UserDao.UserBatchOper(0, 16, 10L) {

                public List<User> findNext() {
                    List<User> list = UserDao.findWindowShowOn(offset, limit);
                    log.info("[list:]" + list);
                    return list;
                }

                @Override
                public void doForEachUser(User user) {
                    List<Item> items = OriginApiImpl.get().findRecentDownItems(user, 100);
                    log.info("[to show size:]" + items.size());
                    if (items.size() == 0) {
                        count++;
                    }
                    log.info("offset :" + offset + "   --- count :" + count);
                }
            }.call();
        }
    }

}
