
package bustbapi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import jdp.ApiJdpAdapter;
import models.item.ItemPlay;
import models.user.User;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import actions.DiagAction.BatchResultMsg;
import actions.task.TaskProgressAction.AutoTitleProgressAction;
import autotitle.AutoTitleAction;
import autotitle.AutoTitleOption.BatchPageOption;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.NumberUtil;
import com.ciaosir.client.utils.SplitUtils;
import com.dbt.cred.utils.JsonUtil;
import com.google.gson.Gson;
import com.taobao.api.domain.Cooperation;
import com.taobao.api.domain.FenxiaoProduct;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.LoginUser;
import com.taobao.api.request.FenxiaoCooperationGetRequest;
import com.taobao.api.request.FenxiaoDistributorProductsGetRequest;
import com.taobao.api.request.FenxiaoLoginUserGetRequest;
import com.taobao.api.request.FenxiaoProductsGetRequest;
import com.taobao.api.request.ScitemGetRequest;
import com.taobao.api.request.ScitemMapQueryRequest;
import com.taobao.api.request.ScitemOutercodeGetRequest;
import com.taobao.api.response.FenxiaoCooperationGetResponse;
import com.taobao.api.response.FenxiaoDistributorProductsGetResponse;
import com.taobao.api.response.FenxiaoLoginUserGetResponse;
import com.taobao.api.response.FenxiaoProductsGetResponse;
import com.taobao.api.response.ScitemGetResponse;
import com.taobao.api.response.ScitemMapQueryResponse;
import com.taobao.api.response.ScitemOutercodeGetResponse;

import configs.TMConfigs;
import controllers.Titles;
import dao.UserDao;

public class FenxiaoApi {

    static FenxiaoApi _instance = new FenxiaoApi();

    public FenxiaoApi get() {
        return _instance;
    }

    private static final Logger log = LoggerFactory.getLogger(FenxiaoApi.class);

    public static final String TAG = "FenxiaoApi";

    public static class FXUserGet extends TBApi<FenxiaoLoginUserGetRequest, FenxiaoLoginUserGetResponse, Integer> {

        public FXUserGet(User user) {
            super(user.getSessionKey());
        }

        public FXUserGet(String sid) {
            super(sid);
        }

        @Override
        public FenxiaoLoginUserGetRequest prepareRequest() {
            FenxiaoLoginUserGetRequest req = new FenxiaoLoginUserGetRequest();
            return req;
        }

        @Override
        public Integer validResponse(FenxiaoLoginUserGetResponse resp) {
            LoginUser loginUser = resp.getLoginUser();
            if (loginUser == null) {
                return 0;
            }
            String userType = loginUser.getUserType();
            if ("1".equals(userType)) {
                return User.Type.IS_FENGXIAO;
            }
            if ("2".equals(userType)) {
                return User.Type.IS_GONGHUO;
            }

            return 0;
        }

        @Override
        public Integer applyResult(Integer res) {
            return res;
        }
    }

    public static void setUserFenxiao(User user) {
        try {
            Integer res = new FXUserGet(user).call();
            if (res == null) {
                return;
            }
            switch (res) {
                case User.Type.IS_FENGXIAO:
                    user.type &= ~(User.Type.IS_GONGHUO);
                    user.type |= User.Type.IS_FENGXIAO;
                    break;
                case User.Type.IS_GONGHUO:
                    user.type &= ~(User.Type.IS_FENGXIAO);
                    user.type |= User.Type.IS_GONGHUO;
                    break;
                case 0:
                    user.type &= ~(User.Type.IS_FENGXIAO | User.Type.IS_GONGHUO);
                    break;
                default:
                    return;
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }

        // now, it's the time to update...
    }

    public static class FixAllUserFengxiaoJob extends Job {

        public void doJob() {
            new UserDao.UserBatchOper(128) {
                @Override
                public void doForEachUser(User user) {
                    setUserFenxiao(user);
                    user.jdbcSave();
                }
            }.call();
        }
    }

    public static class FXScItemOutCodeGet extends
            TBApi<ScitemOutercodeGetRequest, ScitemOutercodeGetResponse, Integer> {

        String code;

        public FXScItemOutCodeGet(User user, String code) {
            super(user.getSessionKey());
            this.code = code;
        }

        @Override
        public ScitemOutercodeGetRequest prepareRequest() {
            ScitemOutercodeGetRequest req = new ScitemOutercodeGetRequest();
            req.setOuterCode(code);
            return req;
        }

        @Override
        public Integer validResponse(ScitemOutercodeGetResponse resp) {
            System.out.println(new Gson().toJson(resp));
            return 1;
        }

        @Override
        public Integer applyResult(Integer res) {
            return res;
        }

    }

    public static class FXItemGet extends TBApi<ScitemGetRequest, ScitemGetResponse, Integer> {

        long numIid = 0L;

        public FXItemGet(User user, long numIid) {
            super(user.getSessionKey());
            this.numIid = numIid;
        }

        @Override
        public ScitemGetRequest prepareRequest() {
            ScitemGetRequest req = new ScitemGetRequest();
            req.setItemId(numIid);
            return req;
        }

        @Override
        public Integer validResponse(ScitemGetResponse resp) {
            System.out.println(new Gson().toJson(resp));
            System.out.println(new Gson().toJson(resp.getScItem()));
            return 1;
        }

        @Override
        public Integer applyResult(Integer res) {
            return res;
        }

    }

//    TaobaoClient client=new DefaultTaobaoClient(url, appkey, secret);
//    ScitemMapQueryRequest req=new ScitemMapQueryRequest();
//    req.setItemId(128L);
//    req.setSkuId(129L);
//    ScitemMapQueryResponse response = client.execute(req , sessionKey);
    public static class FXScItemMapQueryApi extends TBApi<ScitemMapQueryRequest, ScitemMapQueryResponse, Integer> {

        public FXScItemMapQueryApi(User user, long numIid) {
            super(user.getSessionKey());
            this.numIid = numIid;
        }

        long numIid = 0L;

        @Override
        public ScitemMapQueryRequest prepareRequest() {
            ScitemMapQueryRequest req = new ScitemMapQueryRequest();
            req.setItemId(numIid);
            return req;
        }

        @Override
        public Integer applyResult(Integer res) {
            return 1;
        }

        @Override
        public Integer validResponse(ScitemMapQueryResponse resp) {
            System.out.println(new Gson().toJson(resp));
            return 1;
        }

    }

    public static void hello() {
//        TaobaoClient client = new DefaultTaobaoClient(url, appkey, secret);
//        FenxiaoDistributorProductsGetRequest req = new FenxiaoDistributorProductsGetRequest();
//        req.setSupplierNick("测试账号");
//        req.setTradeType("AGENT");
//        req.setDownloadStatus("UNDOWNLOAD");
//        req.setProductcatId(3232312L);
//        req.setFields("skus,images");
//        Date dateTime = SimpleDateFormat.getDateTimeInstance().parse("2000-01-01 00:00:00");
//        req.setStartTime(dateTime);
//        Date dateTime = SimpleDateFormat.getDateTimeInstance().parse("2000-01-01 00:00:00");
//        req.setEndTime(dateTime);
//        req.setPageNo(1L);
//        req.setPageSize(20L);
//        req.setTimeType("MODIFIED");
//        req.setOrderBy("QUANTITY_DESC");
//        req.setItemIds("1001,1002,1003,1004,1005");
//        req.setPids("1001,1002,1003,1004,1005");
//        FenxiaoDistributorProductsGetResponse response = client.execute(req, sessionKey);
    }

    public static class FXScItemMapApi
            extends
            TBApi<FenxiaoDistributorProductsGetRequest, FenxiaoDistributorProductsGetResponse, Map<Long, FenxiaoProduct>> {
        Collection<Long> ids = ListUtils.EMPTY_LIST;

        Map<Long, FenxiaoProduct> res = new HashMap<Long, FenxiaoProduct>();

        public FXScItemMapApi(User user, Collection<Long> ids) {
            super(user.getSessionKey());
            this.ids = ids;
        }

        @Override
        public FenxiaoDistributorProductsGetRequest prepareRequest() {
            FenxiaoDistributorProductsGetRequest req = new FenxiaoDistributorProductsGetRequest();
            String join = StringUtils.join(ids, ',');

            req.setItemIds(join);
//            req.setSupplierNick("嘟宝");
            return req;
        }

        @Override
        public Map<Long, FenxiaoProduct> validResponse(FenxiaoDistributorProductsGetResponse resp) {
            List<FenxiaoProduct> products = resp.getProducts();
            for (FenxiaoProduct fenxiaoProduct : products) {
                System.out.println(" title :" + fenxiaoProduct.getName());
                System.out.println(" item id :" + fenxiaoProduct.getItemId());
                System.out.println(" item id :" + fenxiaoProduct.getAlarmNumber());
                System.out.println(" item id :" + fenxiaoProduct.getAlarmNumber());
                System.out.println(" item id :" + fenxiaoProduct.getProperties());
                System.out.println(" item id :" + fenxiaoProduct.getPropertyAlias());

            }
            return res;
        }

        @Override
        public Map<Long, FenxiaoProduct> applyResult(Map<Long, FenxiaoProduct> res) {
            return res;
        }
    }

    public static class FXScItemApi extends
            TBApi<FenxiaoDistributorProductsGetRequest, FenxiaoDistributorProductsGetResponse, FenxiaoProduct> {

        Map<Long, FenxiaoProduct> res = new HashMap<Long, FenxiaoProduct>();

        Long id;

        User user;

        public FXScItemApi(User user, Long id) {
            super(user.getSessionKey());
            this.id = id;
            this.user = user;
            this.retryTime = 1;
        }

        @Override
        public FenxiaoDistributorProductsGetRequest prepareRequest() {
            FenxiaoDistributorProductsGetRequest req = new FenxiaoDistributorProductsGetRequest();
            req.setItemIds(String.valueOf(id));
            return req;
        }

        @Override
        public FenxiaoProduct validResponse(FenxiaoDistributorProductsGetResponse resp) {
            if (!ErrorHandler.validResponseBoolean(resp)) {
                if (":isv.invalid-parameter:user_id_num".equals(resp.getSubCode())) {
                    user.setFenxiaoOn(false);
                    user.jdbcSave();
                }
                return null;
            }

            FenxiaoProduct product = NumberUtil.first(resp.getProducts());
            return product;
        }

        @Override
        public FenxiaoProduct applyResult(FenxiaoProduct res) {
            return res;
        }
    }

    public static class FXScItemSupplierApi extends
            TBApi<FenxiaoDistributorProductsGetRequest, FenxiaoDistributorProductsGetResponse, FenxiaoProduct> {

        Long id;

        String supplierNick;

        public FXScItemSupplierApi(User user, Long id, String supplierNick) {
            super(user.getSessionKey());
            this.id = id;
            this.supplierNick = supplierNick;
            this.retryTime = 1;
        }

        @Override
        public FenxiaoDistributorProductsGetRequest prepareRequest() {
            FenxiaoDistributorProductsGetRequest req = new FenxiaoDistributorProductsGetRequest();
            req.setItemIds(String.valueOf(id));
            req.setSupplierNick(supplierNick);
            return req;
        }

        @Override
        public FenxiaoProduct validResponse(FenxiaoDistributorProductsGetResponse resp) {
            if (!ErrorHandler.validResponseBoolean(resp)) {
                return null;
            }

            FenxiaoProduct product = NumberUtil.first(resp.getProducts());
            return product;
        }

        @Override
        public FenxiaoProduct applyResult(FenxiaoProduct res) {
            return res;
        }
    }

    public static class FengxiaoRecommender implements Callable<BatchResultMsg> {
        User user;

        Long numIid;

        BatchPageOption opt;

        public FengxiaoRecommender(User user, Long numIid) {
            super();
            this.user = user;
            this.numIid = numIid;
            this.opt = new BatchPageOption();
        }

        public FengxiaoRecommender(User user, Long numIid, BatchPageOption opt) {
            super();
            this.user = user;
            this.numIid = numIid;
            this.opt = opt;
        }

        Item tbItem;

        public FengxiaoRecommender(User user, Item item) {
            super();
            this.user = user;
            this.numIid = item.getNumIid();
            this.tbItem = item;
            this.opt = new BatchPageOption();
        }

        public FengxiaoRecommender(User user, Item tbItem, BatchPageOption opt) {
            super();
            this.user = user;
            this.opt = opt;
            this.tbItem = tbItem;
            this.numIid = tbItem.getNumIid();
        }

        @Override
        public BatchResultMsg call() throws Exception {

            FenxiaoProduct product = new FXScItemApi(user, numIid).call();
//            Items.log.info("[propd :]" + product);
            if (product == null) {
                return null;
            }

            Item itemWithProps = tbItem == null ? ApiJdpAdapter.tryFetchSingleItem(user, numIid) : tbItem;
            String providerTitle = product.getName();
            itemWithProps.setTitle(providerTitle);
            String recommendTitle = AutoTitleAction.autoRecommend(itemWithProps, this.opt);
            BatchResultMsg msg = new BatchResultMsg(numIid, recommendTitle, StringUtils.EMPTY, null);
            msg.setOriginTitle(providerTitle);

            return msg;
        }
    }

    public static void buildResult(final Map<String, String> newTitleMap, final int recMode, final User user,
            List<Item> itemsAll, BatchPageOption opt, Long taskId) {

        List<FutureTask<String>> tasks = new ArrayList<FutureTask<String>>();
        
        for (Item item : itemsAll) {
            BatchAutoTitleRecommend caller = new BatchAutoTitleRecommend(item, user, opt, recMode, newTitleMap);
            FutureTask<String> task = TMConfigs.getStrPool().submit(caller);
            tasks.add(task);
        }

        for (FutureTask<String> futureTask : tasks) {
            try {
                String newTitle = futureTask.get();
//                log.info("newtitle = " + newTitle);
                //newTitleMap.put(arg0, arg1)

                //设置异步任务的进度
                if (taskId != null && taskId > 0L) {
                    AutoTitleProgressAction.stepOneTaskProgress(taskId);
                }

            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }
    }

    public static class BatchAutoTitleRecommend implements Callable<String> {

        ItemPlay item;

        User user;

        BatchPageOption opt;

        int mode;

        Map<String, String> newTitleMap;

        public BatchAutoTitleRecommend(ItemPlay item, User user, BatchPageOption opt, int mode,
                Map<String, String> newTitleMap) {
            super();
            this.item = item;
            this.user = user;
            this.opt = opt;
            this.mode = mode;
            this.newTitleMap = newTitleMap;
        }

        Item tbItem;

        public BatchAutoTitleRecommend(Item tbItem, User user, BatchPageOption opt, int mode,
                Map<String, String> newTitleMap) {
            super();
            this.tbItem = tbItem;
            this.user = user;
            this.opt = opt;
            this.mode = mode;
            this.newTitleMap = newTitleMap;
        }

        @Override
        public String call() throws Exception {
        	
            String newTitle = null;
            BatchResultMsg msg = null;
            Long numIid = null;
            if (tbItem != null) {
                numIid = tbItem.getNumIid();
            }
            if (item != null) {
                numIid = item.getNumIid();
            }

            try {
                switch (mode) {
                    case Titles.TITLE_MODE_DEFAULT_RECOMMEND:
                        if (tbItem == null) {
                            newTitle = AutoTitleAction.autoRecommend(user, numIid, opt);
                        } else {
                            newTitle = AutoTitleAction.autoRecommend(tbItem, opt);
                        }

                        newTitleMap.put(numIid.toString(), newTitle);
                        return newTitle;
                    default:
                        if (tbItem == null) {
                            msg = new FengxiaoRecommender(user, numIid, opt).call();
                        } else {
                            msg = new FengxiaoRecommender(user, tbItem, opt).call();
                        }
                        break;
                }
            } catch (Exception e) {
                Gson gson = new Gson();
                log.warn(" bad item :" + gson.toJson(item) + "  and tbitem :" + gson.toJson(tbItem));
                log.warn(e.getMessage(), e);
            }

            if (msg == null) {
                return null;
            }

            switch (mode) {
                case Titles.TITLE_MODE_OFFICIAL_ORIGIN:
                    newTitle = msg.getOriginTitle();
                    break;
                case Titles.TITLE_MODE_OFFICIAL_RECOMMEND:
                    newTitle = msg.getTitle();
                    break;
            }

            newTitleMap.put(numIid.toString(), newTitle);
            return newTitle;
        }

    }

//    TaobaoClient client=new DefaultTaobaoClient(url, appkey, secret);
//    FenxiaoProductsGetRequest req=new FenxiaoProductsGetRequest();
//    req.setOuterId("a154552");
//    req.setProductcatId(3232312L);
//    req.setStatus("up");
//    req.setPids("1001,1002,1003,1004,1005");
//    req.setFields("skus,images");
//    Date dateTime = SimpleDateFormat.getDateTimeInstance().parse("2000-01-01 00:00:00");
//    req.setStartModified(dateTime);
//    Date dateTime = SimpleDateFormat.getDateTimeInstance().parse("2000-01-01 00:00:00");
//    req.setEndModified(dateTime);
//    req.setPageNo(1L);
//    req.setPageSize(20L);
//    req.setSkuNumber("sku商家编码");
//    req.setIsAuthz("yes");
//    req.setItemIds("1001,1002,1003,1004,1005");
//    FenxiaoProductsGetResponse response = client.execute(req , sessionKey);

    public static class FenxiaoProductGet extends
            TBApi<FenxiaoProductsGetRequest, FenxiaoProductsGetResponse, List<FenxiaoProduct>> {

        Long pn = 1L;

        static Long ps = 20L;

        Collection<Long> numIids = null;

        List<FenxiaoProduct> res = new ArrayList<FenxiaoProduct>();

        User user;

        public FenxiaoProductGet(User user, Collection<Long> numIids) {
            super(user.getSessionKey());
            this.numIids = numIids;
            this.user = user;
        }

        @Override
        public FenxiaoProductsGetRequest prepareRequest() {
            FenxiaoProductsGetRequest req = new FenxiaoProductsGetRequest();
            if (!CommonUtils.isEmpty(numIids)) {
                req.setItemIds(StringUtils.join(numIids, ','));
            }

            req.setPageNo(pn);
            req.setPageSize(ps);
            req.setFields("");
            log.info("[do for pn :]" + pn);

            return req;
        }

        @Override
        public List<FenxiaoProduct> validResponse(FenxiaoProductsGetResponse resp) {
            Long totalRes = resp.getTotalResults();
            List<FenxiaoProduct> products = resp.getProducts();

            ErrorHandler.validTaoBaoResp(this, resp);
            if (totalRes != null && totalRes > pn * ps) {
                this.iteratorTime = 1;
                pn++;
            }
            return products;
        }

        @Override
        public List<FenxiaoProduct> applyResult(List<FenxiaoProduct> res) {
            this.res.addAll(res);
            return this.res;
        }

    }
    
    
    public static class FXDistriProductsGetApi extends
            TBApi<FenxiaoDistributorProductsGetRequest, FenxiaoDistributorProductsGetResponse, List<FenxiaoProduct>> {
        
        private static final int MaxProductsPageSize = 20;

        private Collection<Long> numIidColl;

        private List<List<Long>> splitToSubLongList = ListUtils.EMPTY_LIST;

        private List<FenxiaoProduct> resList = new ArrayList<FenxiaoProduct>();

        
        public FXDistriProductsGetApi(User user, Collection<Long> numIidColl) {
            super(user.getSessionKey());
            this.numIidColl = numIidColl;
            this.splitToSubLongList = SplitUtils.splitToSubLongList(numIidColl, MaxProductsPageSize);
            this.iteratorTime = splitToSubLongList.size();
        }

        @Override
        public FenxiaoDistributorProductsGetRequest prepareRequest() {
            FenxiaoDistributorProductsGetRequest req = new FenxiaoDistributorProductsGetRequest();
            
            String numIids = StringUtils.join(splitToSubLongList.get(iteratorTime), ',');
            req.setItemIds(numIids);
            
            return req;
        }
        

        @Override
        public List<FenxiaoProduct> validResponse(FenxiaoDistributorProductsGetResponse resp) {
            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }


            return resp.getProducts();
        }

        @Override
        public List<FenxiaoProduct> applyResult(List<FenxiaoProduct> res) {
            if (res == null) {
                return resList;
            }

            resList.addAll(res);
            return resList;
        }
    }
    
    

    public static class CooperationGetApi extends
            TBApi<FenxiaoCooperationGetRequest, FenxiaoCooperationGetResponse, List<Cooperation>> {

        Long pn;

        Long ps = 30L;

        List<Cooperation> resList = new ArrayList<Cooperation>();

        public CooperationGetApi(User user) {
            super(user.getSessionKey());
        }

        @Override
        public FenxiaoCooperationGetRequest prepareRequest() {
            FenxiaoCooperationGetRequest req = new FenxiaoCooperationGetRequest();
            req.setPageNo(pn);
            req.setPageSize(ps);
            return req;
        }

        @Override
        public List<Cooperation> validResponse(FenxiaoCooperationGetResponse resp) {
            if (!ErrorHandler.validResponseBoolean(resp)) {
                return null;
            }

            List<Cooperation> cooperations = resp.getCooperations();
            if (!CommonUtils.isEmpty(cooperations) && cooperations.size() >= ps) {
                this.iteratorTime = 1;
                pn++;
            }
            return cooperations;
        }

        @Override
        public List<Cooperation> applyResult(List<Cooperation> res) {
            if (CommonUtils.isEmpty(res)) {
                return res;
            }
            resList.addAll(res);
            return resList;
        }
    }

    @JsonAutoDetect
    public static class FenxiaoProductBean implements Serializable {
        @JsonProperty
        private Long queryNumIid;

        @JsonProperty
        private Long cid;

        @JsonProperty
        private Long discountId;

        @JsonProperty
        private Long pid;

        @JsonProperty
        private Long quantity;

        /**
         * 采购价格，单位：分。
         */
        @JsonProperty
        private int costPrice;

        /**
         * 经销采购价
         */
        @JsonProperty
        private int dealerCostPrice;

        /**
        * 最高零售价，单位：分。
         */
        @JsonProperty
        private int retailPriceHigh;

        @JsonProperty
        private int retailPriceLow;

        @JsonProperty
        private int standardPrice;

        @JsonProperty
        private String status;

        public Long getQueryNumIid() {
            return queryNumIid;
        }

        public void setQueryNumIid(Long queryNumIid) {
            this.queryNumIid = queryNumIid;
        }

        public Long getCid() {
            return cid;
        }

        public void setCid(Long cid) {
            this.cid = cid;
        }

        public Long getDiscountId() {
            return discountId;
        }

        public void setDiscountId(Long discountId) {
            this.discountId = discountId;
        }

        public Long getPid() {
            return pid;
        }

        public void setPid(Long pid) {
            this.pid = pid;
        }

        public Long getQuantity() {
            return quantity;
        }

        public void setQuantity(Long quantity) {
            this.quantity = quantity;
        }

        public int getCostPrice() {
            return costPrice;
        }

        public void setCostPrice(int costPrice) {
            this.costPrice = costPrice;
        }

        public int getDealerCostPrice() {
            return dealerCostPrice;
        }

        public void setDealerCostPrice(int dealerCostPrice) {
            this.dealerCostPrice = dealerCostPrice;
        }

        public int getRetailPriceHigh() {
            return retailPriceHigh;
        }

        public void setRetailPriceHigh(int retailPriceHigh) {
            this.retailPriceHigh = retailPriceHigh;
        }

        public int getRetailPriceLow() {
            return retailPriceLow;
        }

        public void setRetailPriceLow(int retailPriceLow) {
            this.retailPriceLow = retailPriceLow;
        }

        public int getStandardPrice() {
            return standardPrice;
        }

        public void setStandardPrice(int standardPrice) {
            this.standardPrice = standardPrice;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public FenxiaoProductBean(FenxiaoProduct product) {
            this.queryNumIid = product.getQueryItemId();
            this.cid = NumberUtil.parserLong(product.getCategoryId(), 0L);
            this.discountId = product.getDiscountId();
            this.pid = product.getPid();
            this.quantity = product.getQuantity();
            this.costPrice = NumberUtil.getIntFromPrice(product.getCostPrice());
            this.dealerCostPrice = NumberUtil.getIntFromPrice(product.getDealerCostPrice());
            this.retailPriceLow = NumberUtil.getIntFromPrice(product.getRetailPriceLow());
            this.retailPriceHigh = NumberUtil.getIntFromPrice(product.getRetailPriceHigh());
            this.standardPrice = NumberUtil.getIntFromPrice(product.getStandardPrice());
            this.status = product.getStatus();
        }
    }

    /*
     * {
    "fenxiao_distributor_products_get_response": {
    "has_next": false,
    "products": {
      "fenxiao_product": [
        {
          "alarm_number": 0,
          "category_id": "50009032",
          "city": "南京",
          "cost_price": "15.00",
          "dealer_cost_price": "15.00",
          "desc_path": "http:\/\/img05.taobaocdn.com\/bao\/uploaded\/i5\/T16OLLFipaXXbospjX",
          "description": "<p><img align=\"absmiddle\" src=\"http:\/\/img03.taobaocdn.com\/imgextra\/i3\/1798657831\/T2uD0wXBtaXXXXXXXX_!!1798657831.jpg\"><img align=\"absmiddle\" style=\"line-height: 1.5;\" src=\"http:\/\/img01.taobaocdn.com\/imgextra\/i1\/1798657831\/T2uBtxXy4aXXXXXXXX_!!1798657831.jpg\"><img align=\"absmiddle\" style=\"line-height: 1.5;\" src=\"http:\/\/img03.taobaocdn.com\/imgextra\/i3\/1798657831\/T28JFBXxXXXXXXXXXX_!!1798657831.jpg\"><img align=\"absmiddle\" style=\"line-height: 1.5;\" src=\"http:\/\/img03.taobaocdn.com\/imgextra\/i3\/1798657831\/T2q0hxXv4aXXXXXXXX_!!1798657831.jpg\"><img align=\"absmiddle\" style=\"line-height: 1.5;\" src=\"http:\/\/img02.taobaocdn.com\/imgextra\/i2\/1798657831\/T2jH0AXDVXXXXXXXXX_!!1798657831.jpg\"><img align=\"absmiddle\" style=\"line-height: 1.5;\" src=\"http:\/\/img03.taobaocdn.com\/imgextra\/i3\/1798657831\/T2IT8wXxNaXXXXXXXX_!!1798657831.jpg\"><img align=\"absmiddle\" style=\"line-height: 1.5;\" src=\"http:\/\/img02.taobaocdn.com\/imgextra\/i2\/1798657831\/T2UjhAXxXXXXXXXXXX_!!1798657831.jpg\"><img align=\"absmiddle\" style=\"line-height: 1.5;\" src=\"http:\/\/img02.taobaocdn.com\/imgextra\/i2\/1798657831\/T2JXqBXaNdXXXXXXXX_!!1798657831.jpg\"><img align=\"absmiddle\" style=\"line-height: 1.5;\" src=\"http:\/\/img03.taobaocdn.com\/imgextra\/i3\/1798657831\/T2LONBXtdXXXXXXXXX_!!1798657831.jpg\"><img align=\"absmiddle\" style=\"line-height: 1.5;\" src=\"http:\/\/img01.taobaocdn.com\/imgextra\/i1\/1798657831\/T2pDBjXXlcXXXXXXXX_!!1798657831.jpg\"><img align=\"absmiddle\" style=\"line-height: 1.5;\" src=\"http:\/\/img04.taobaocdn.com\/imgextra\/i4\/1798657831\/T2XMpzXvdaXXXXXXXX_!!1798657831.jpg\"><img align=\"absmiddle\" style=\"line-height: 1.5;\" src=\"http:\/\/img01.taobaocdn.com\/imgextra\/i1\/1798657831\/T2BYBBXxtXXXXXXXXX_!!1798657831.jpg\"><img align=\"absmiddle\" style=\"line-height: 1.5;\" src=\"http:\/\/img01.taobaocdn.com\/imgextra\/i1\/1798657831\/T2eityXvpXXXXXXXXX_!!1798657831.jpg\"><img align=\"absmiddle\" style=\"line-height: 1.5;\" src=\"http:\/\/img03.taobaocdn.com\/imgextra\/i3\/1798657831\/T2xhtzXstaXXXXXXXX_!!1798657831.jpg\"><img align=\"absmiddle\" style=\"line-height: 1.5;\" src=\"http:\/\/img01.taobaocdn.com\/imgextra\/i1\/1798657831\/T21XhzXvNaXXXXXXXX_!!1798657831.jpg\"><img align=\"absmiddle\" style=\"line-height: 1.5;\" src=\"http:\/\/img04.taobaocdn.com\/imgextra\/i4\/1798657831\/T2NcxvXC0aXXXXXXXX_!!1798657831.jpg\"><img align=\"absmiddle\" style=\"line-height: 1.5;\" src=\"http:\/\/img01.taobaocdn.com\/imgextra\/i1\/1798657831\/T2GwxxXEVXXXXXXXXX_!!1798657831.jpg\"><img align=\"absmiddle\" style=\"line-height: 1.5;\" src=\"http:\/\/img04.taobaocdn.com\/imgextra\/i4\/1798657831\/T229hwXD4XXXXXXXXX_!!1798657831.jpg\"><\/p>",
          "discount_id": 0,
          "created": "2013-10-06 19:39:49",
          "modified": "2014-04-04 22:35:48",
          "have_invoice": false,
          "have_guarantee": false,
          "input_properties": "13021751:Y;20000:柯仕道;",
          "is_authz": "no",
          "items_count": 202,
          "pictures": "http:\/\/img05.taobaocdn.com\/bao\/uploaded\/i5\/T1v1nfFjlaXXazCko9_073943.jpg",
          "postage_id": 964280370,
          "postage_type": "buyer",
          "pid": 381946952389,
          "images": {
            
          },
          "productcat_id": 2404173,
          "outer_id": "Y",
          "properties": "20021:28397;31713:48182;122216608:3247842;122216608:3267960;122216608:3267959;122216608:101181;24477:20532;122216904:493280173;1627207:3232483;2123830:48182;122216624:29568;2124210:107668;122216625:12461073;18653767:42752;20608:3308302;20000:107671159;13021751:110675540",
          "property_alias": "1627207:3232483:款式随机",
          "prov": "江苏",
          "quantity": 227,
          "query_item_id": 37851011455,
          "retail_price_high": "109.00",
          "retail_price_low": "19.00",
          "skus": {
            
          },
          "spu_id": 0,
          "standard_price": "15.00",
          "standard_retail_price": "19.00",
          "status": "up",
          "name": "腰带",
          "orders_count": 55,
          "trade_type": "ALL",
          "upshelf_time": "2013-10-06 19:39:49"
        },
        {
          "alarm_number": 0,
          "category_id": "50010167",
          "city": "南京",
          "cost_price": "52.00",
          "dealer_cost_price": "52.00",
          "desc_path": "http:\/\/img03.taobaocdn.com\/bao\/uploaded\/i3\/T1XsP8Fw8dXXbospjX",
          "description": "<p><img align=\"absmiddle\" src=\"http:\/\/img02.taobaocdn.com\/imgextra\/i2\/1798657831\/T2owxaXJpaXXXXXXXX-1798657831.jpg\"><img align=\"absmiddle\" src=\"http:\/\/img04.taobaocdn.com\/imgextra\/i4\/1798657831\/T2ei8fXJ0XXXXXXXXX-1798657831.jpg\"><img align=\"absmiddle\" src=\"http:\/\/img01.taobaocdn.com\/imgextra\/i1\/1798657831\/T2oLhdXTJXXXXXXXXX-1798657831.jpg\"><img align=\"absmiddle\" src=\"http:\/\/img03.taobaocdn.com\/imgextra\/i3\/1798657831\/T2NS4fXI4XXXXXXXXX-1798657831.jpg\"><img align=\"absmiddle\" src=\"http:\/\/img03.taobaocdn.com\/imgextra\/i3\/1798657831\/T2v7tcXFVaXXXXXXXX-1798657831.jpg\"><img align=\"absmiddle\" src=\"http:\/\/img04.taobaocdn.com\/imgextra\/i4\/1798657831\/T2oqhdXUdXXXXXXXXX-1798657831.jpg\"><img align=\"absmiddle\" src=\"http:\/\/img02.taobaocdn.com\/imgextra\/i2\/1798657831\/T2YvpcXGFaXXXXXXXX-1798657831.jpg\"><img align=\"absmiddle\" src=\"http:\/\/img01.taobaocdn.com\/imgextra\/i1\/1798657831\/T2nSpfXIFXXXXXXXXX-1798657831.jpg\"><img align=\"absmiddle\" src=\"http:\/\/img02.taobaocdn.com\/imgextra\/i2\/1798657831\/T2mvxXXMBaXXXXXXXX-1798657831.jpg\"><img align=\"absmiddle\" src=\"http:\/\/img02.taobaocdn.com\/imgextra\/i2\/1798657831\/T2iklfXJhXXXXXXXXX-1798657831.jpg\"><img align=\"absmiddle\" src=\"http:\/\/img02.taobaocdn.com\/imgextra\/i2\/1798657831\/T2SBBgXFtXXXXXXXXX-1798657831.jpg\"><img align=\"absmiddle\" src=\"http:\/\/img03.taobaocdn.com\/imgextra\/i3\/1798657831\/T2cN8dXSRXXXXXXXXX-1798657831.jpg\"><img align=\"absmiddle\" src=\"http:\/\/img01.taobaocdn.com\/imgextra\/i1\/1798657831\/T2zPFaXHxaXXXXXXXX-1798657831.jpg\"><\/p>",
          "discount_id": 0,
          "created": "2014-03-31 00:19:02",
          "modified": "2014-04-06 10:49:05",
          "have_invoice": true,
          "have_guarantee": false,
          "input_properties": "13021751:8160;20000:柯仕道;",
          "is_authz": "no",
          "items_count": 99,
          "pictures": "http:\/\/img04.taobaocdn.com\/bao\/uploaded\/i4\/T1n.H6FBVdXXXXXXXX_!!0-item_pic.jpg",
          "postage_id": 1070957300,
          "postage_type": "buyer",
          "pid": 420863672389,
          "images": {
            
          },
          "productcat_id": 2404173,
          "outer_id": "8160",
          "properties": "20518:3217387;20518:3217386;20518:3217389;20518:3217388;20518:3271768;20518:3217382;20518:3217383;20518:3217384;20518:3217385;20551:3267795;20677:29954;1627207:28338;2047670:115481;8560225:30724429;36228475:174934690;42718685:178914558;42722636:248572013;122216345:29457;122216507:3216783;122216586:3267162;122216608:3267959;122276111:20525;13021751:3383667;20000:107671159",
          "property_alias": "20518:3217382:29（2.23尺）;20518:3271768:38（2.92尺）;20518:3217383:30（2.31尺）;20518:3217384:31（2.39尺）;20518:3217385:32（2.46尺）;20518:3217386:33（2.54尺）;20518:3217387:34（2.62尺）;20518:3217388:35（2.69尺）;20518:3217389:36（2.77尺）",
          "prov": "江苏",
          "quantity": 1799,
          "query_item_id": 38364712256,
          "retail_price_high": "179.00",
          "retail_price_low": "59.00",
          "skus": {
            
          },
          "spu_id": 267634888,
          "standard_price": "52.00",
          "standard_retail_price": "59.00",
          "status": "up",
          "name": "柯仕道 男式夏季薄款修身青年牛仔裤 潮中腰薄长裤子",
          "orders_count": 1,
          "trade_type": "ALL",
          "upshelf_time": "2014-03-31 00:19:44"
        }
      ]
    }
    }
    }
     */

    public static void ensureFenxiaoInfo(User user, List<ItemPlay> items) {
        Map<Long, ItemPlay> fenxiaoItemMap = new HashMap<Long, ItemPlay>();
        for (ItemPlay itemPlay : items) {
            if (!itemPlay.isFenxiao()) {
                // Nothing to do for the common items..
                continue;
            }
            // Deal with the fenxiao item...
            fenxiaoItemMap.put(itemPlay.getNumIid(), itemPlay);
        }
        
        if (CommonUtils.isEmpty(fenxiaoItemMap)) {
            return;
        }
        
        
        //List<FenxiaoProduct> products = new FenxiaoProductGet(user, fenxiaoItemMap.keySet()).call();
        List<FenxiaoProduct> products = new FXDistriProductsGetApi(user, fenxiaoItemMap.keySet()).call();
        
        if (CommonUtils.isEmpty(products)) {
            return;
        }
        
        log.info("there " + fenxiaoItemMap.size() + " fenxiao items need fetch for user: " 
                + user.getUserNick() + ", and result get " + products.size() + " products------");
        
        
        //log.info(JsonUtil.getJson(products.get(0)));
        
        for (FenxiaoProduct fenxiaoProduct : products) {
            FenxiaoProductBean bean = new FenxiaoProductBean(fenxiaoProduct);
            ItemPlay item = fenxiaoItemMap.get(bean.getQueryNumIid());
            if (item == null) {
                // no found for the item numiid :
                log.warn("no found for the fenxiao numiid :" + bean);
                continue;
            }

            // ensure the bean...
            item.setFenxiaoProductBean(bean);
        }
    }
}
