
package bustbapi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import models.user.User;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;
import com.ciaosir.client.utils.NumberUtil;
import com.ciaosir.client.utils.SplitUtils;
import com.taobao.api.ApiException;
import com.taobao.api.ApiRuleException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.TaobaoRequest;
import com.taobao.api.TaobaoResponse;
import com.taobao.api.domain.DescModuleInfo;
import com.taobao.api.domain.INWordBase;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.ItemImg;
import com.taobao.api.domain.Location;
import com.taobao.api.domain.Order;
import com.taobao.api.domain.Picture;
import com.taobao.api.domain.PictureCategory;
import com.taobao.api.domain.PropImg;
import com.taobao.api.domain.Shop;
import com.taobao.api.domain.Sku;
import com.taobao.api.domain.Trade;
import com.taobao.api.domain.TradeRate;
import com.taobao.api.domain.Video;
import com.taobao.api.request.SimbaInsightWordsbaseGetRequest;
import com.taobao.api.response.ItemGetResponse;
import com.taobao.api.response.ItemSellerGetResponse;
import com.taobao.api.response.ItemsListGetResponse;
import com.taobao.api.response.ItemsOnsaleGetResponse;
import com.taobao.api.response.ItemsSellerListGetResponse;
import com.taobao.api.response.PictureCategoryGetResponse;
import com.taobao.api.response.PictureGetResponse;
import com.taobao.api.response.ShopRemainshowcaseGetResponse;
import com.taobao.api.response.SimbaInsightWordsbaseGetResponse;

import configs.TMConfigs.App;
import controllers.APIConfig;
import dao.UserDao;

public abstract class TBApi<K extends TaobaoRequest<V>, V extends TaobaoResponse, W> implements Callable<W> {

    private static final Logger log = LoggerFactory.getLogger(TBApi.class);

    protected String TAG = "TBApi";

    protected int retryTime = 1;

    protected String sid;

    protected String appKey = APIConfig.get().getApiKey();

    protected String appSecret = APIConfig.get().getSecret();

    protected long sleepInterval = 1000L;

    protected boolean isApiSuccess = true;

    protected int iteratorTime = 1;

    protected String subErrorCode = null;

    protected String subErrorMsg = null;

    protected TaobaoClient client = TBApi.genClient();

    protected User user;

    public TaobaoClient getClient() {
        return client;
    }

    public void setClient(TaobaoClient client) {
        this.client = client;
    }

    public enum TimeSpan {
        DAY(1, "DAY"), WEEK(7, "WEEK"), MONTH(30, "MONTH"), TRIPPLE_MONGH(90, "3MONTH");

        private int day;

        private String key;

        private TimeSpan(int day, String key) {
            this.day = day;
            this.key = key;
        }

        public int getDay() {
            return day;
        }

        public void setDay(int day) {
            this.day = day;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }

    public TBApi(String sid) {
        this.sid = sid;
    }

    public TBApi() {

    }

    public TBApi(User user) {
        if (user != null) {
            this.sid = user.getSessionKey();
            this.user = user;
        }
    }

    public abstract K prepareRequest();

    public abstract W validResponse(V resp);

    public abstract W applyResult(W res);

    @Override
    public W call() {
        return callApi();
    }

    K req = null;
    
    public void setRetryTime(int retryTime) {
        this.retryTime = retryTime;
    }

    protected W callApi() {

        W res = null;

        while (iteratorTime-- > 0) {

            V resp = null;

            req = prepareRequest();
            if (req == null) {
                return res;
            }

            boolean doneForThisTime = false;
            int count = 0;

            while (count++ < retryTime && !doneForThisTime) {
//                if (sid != null && !UserDao.isVaild(sid)) {
//                    log.error("Sid is not vaild!!!");
//                    return null;
//                }

                if (count > 1) {
                    log.warn("[Current Retry Time]" + count + "  for class:" + this.getClass());
                    CommonUtils.sleepQuietly(sleepInterval);
                }

                try {

                    resp = execProcess();

                    ApiUtil.apiCount++;

                    if (resp != null && !resp.isSuccess()) {
                        String subCode = resp.getSubCode();
                        
                        if("invalid-sessionkey".equals(subCode)) {
                        	User user = UserDao.findBySessionKey(this.sid);
                        	user.updateIsVaild(false);
                        }
                        
                        if ("session-expired".equals(subCode)) {
                            this.retryTime = 0;
                        }
                        
                        String errorCode = resp.getErrorCode();
                        // 授权出错
                        if ("27".equals(errorCode)) {
                            this.retryTime = 0;
                        }
                    }

                    res = validResponse(resp);

                    /**
                     * no results got and exception, continue retry
                     */
                    if (res == null) {
//                        log.warn("No Validate Res , retry:" + this.getClass());
                        continue;
                    }
                    if (count > 1) {
                        log.error("this is the " + count + " times to try for class:" 
                                + this.getClass() + ", and success!!!!!!!!!!!!!!-----------------");
                    }

                    res = applyResult(res);
                    doneForThisTime = true;
                    // log.error("This is Done");
                } catch (ApiException e) {
                    log.warn(e.getMessage(), e);
                } catch (Exception e) {
//                    if (e instanceof PolicyResult) {
////                        PolicyResult result = (PolicyResult) e;
////                        if (result.code == ReturnCode.API_CALL_LIMIT) {
////                            // ApiUtil.setApiCallLimited(true);
////                            return null;
////                        } else if (result.code == ReturnCode.INVALID_SESSION) {
////                            UserDao.updateVaild(sid, false);
////                            log.warn("The session is expired!!!");
////                            return null;
////                        } else if (result.code == ItemApi.ItemGet.ITEM_GET_530) {
////                            log.warn("Item get error:" + result.getMsg());
////                            return null;
////                        }
//                    if (result.code == ItemApi.ItemGet.ITEM_GET_530) {
//                        log.warn("Item get error:" + result.getM);
//                        return null;
//                    }
//                    } else {
                    if (e.getMessage() != null && e.getMessage().startsWith("expected string at column")) {
                        log.warn("client.execute exception!!!-------------");
                    } else {
                        log.warn(e.getMessage(), e);
//                        }
                    }
                    continue;
                }
            }

            if (count >= retryTime && !doneForThisTime) {
                this.isApiSuccess = false;
                return null;
            }
        }

        return res;
    }

    protected V execProcess() throws ApiException {
        V resp;
        if (sid == null) {
            resp = client.execute(req);
        } else {
            resp = client.execute(req, sid);
        }
        return resp;
    }

    public boolean isApiSuccess() {
        return this.isApiSuccess;
    }

    public boolean isConcurrentControlled() {
        return false;
    }

    public String getSubErrorCode() {
        return subErrorCode;
    }

    public void setSubErrorCode(String subErrorCode) {
        this.subErrorCode = subErrorCode;
    }

    public static TaobaoClient genBusClient() {
        return new DefaultTaobaoClient(App.API_TAOBAO_URL, "21371613", "f78597dcd98face1d79157003fce7f68");
//        return new DefaultTaobaoClient(App.API_TAOBAO_URL, "21076547", "e3b20302ce41fd67dfba6128ad59ea91");
//        return new DefaultTaobaoClient(App.API_TAOBAO_URL, "21419552", "7c3be024edce76544f6cc1a6736298aa");
    }

    public static TaobaoClient genPYClient() {
        return new DefaultTaobaoClient(App.API_TAOBAO_URL, "12442245", "80663ff0052dad65fa4bd4cc4aba071f");
    }

    public static TaobaoClient genClient() {
        return new DefaultTaobaoClient(App.API_TAOBAO_URL, APIConfig.get().getApiKey(), APIConfig.get().getSecret());
    }

    public static TaobaoClient genClient(APIConfig config) {
        return new DefaultTaobaoClient(App.API_TAOBAO_URL, config.getApiKey(), config.getSecret());
    }

    protected String errorMsg;

    private static long currentTimeMillis;

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public static class WordBaseGet extends
            TBApi<SimbaInsightWordsbaseGetRequest, SimbaInsightWordsbaseGetResponse, List<INWordBase>> {

        static final int MAX_WORD_LENGTH = 160;

        List<List<String>> splitToSubStrList = ListUtils.EMPTY_LIST;

        String filter;

        List<INWordBase> resList;

        public static String MINING_FITER = "PV,CLICK,CTR,COMPETITION,AVGCPC";

        Collection<String> wordList;

        public WordBaseGet(String sid, Collection<String> wordList) {
            this(sid, wordList, MINING_FITER);
        }

        public WordBaseGet(String sid, Collection<String> wordList, String filter) {
            super(sid);
            if (CommonUtils.isEmpty(wordList)) {
                return;
            }

            this.wordList = wordList;
            this.splitToSubStrList = SplitUtils.splitToSubStrList(wordList, MAX_WORD_LENGTH);
            this.iteratorTime = splitToSubStrList.size();
            log.error("Repeat Size:" + iteratorTime + " with words length:" + wordList.size());

            this.filter = filter;
            resList = new ArrayList<INWordBase>();
            this.client = genBusClient();
        }

        @Override
        public SimbaInsightWordsbaseGetRequest prepareRequest() {
            if (CommonUtils.isEmpty(this.splitToSubStrList)) {
                return null;
            }

            SimbaInsightWordsbaseGetRequest req = new SimbaInsightWordsbaseGetRequest();

            String words = StringUtils.join(splitToSubStrList.get(iteratorTime), ',');
            req.setTime(TimeSpan.WEEK.getKey());
            req.setWords(words);
            req.setFilter(filter);
            return req;
        }

        @Override
        public List<INWordBase> validResponse(SimbaInsightWordsbaseGetResponse resp) {

            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }

            // ErrorHandler.validTaoBaoResp(resp);
            if (!resp.isSuccess()) {
                if ("数组内容中有重复值".equals(resp.getSubMsg())) {
                    this.splitToSubStrList = SplitUtils.splitToSubStrList(wordList, 1);
                    this.iteratorTime = splitToSubStrList.size();
                    return ListUtils.EMPTY_LIST;
                }
                // throw new PolicyResult(ReturnCode.API_CALL_LIMIT,
                // StringUtils.EMPTY);
            }

            return resp.getInWordBases() == null ? ListUtils.EMPTY_LIST : resp.getInWordBases();
        }

        @Override
        public List<INWordBase> applyResult(List<INWordBase> res) {
            if (res == null) {
                return resList;
            }

            // log.info("[Before Res List Size]" + resList.size());
            resList.addAll(res);
            // log.info("[After Res List Size]" + resList.size());

            return resList;
        }

    }

    protected ItemSellerGetResponse validItemGetResp() throws ApiException {
        try {
            req.check();//if check failed,will throw ApiRuleException.
        } catch (ApiRuleException e) {
        	ItemSellerGetResponse localResponse = null;
            try {
                localResponse = new ItemSellerGetResponse();
            } catch (Exception e2) {
                throw new ApiException(e2);
            }
            localResponse.setErrorCode(e.getErrCode());
            localResponse.setMsg(e.getErrMsg());
            //localResponse.setBody("this.");
            return localResponse;
        }

        ItemSellerGetResponse localResponse = new ItemSellerGetResponse();
        Map<String, Object> rt = ((DefaultTaobaoClient) client).doPost(req, sid);
//        List<Item> items = new ArrayList<Item>();
//        localResponse.setItems(items);
//        Item item = new Item();
//        localResponse.setItem(item);
        try {
            JSONObject first = new JSONObject(rt.get("rsp").toString());
            if (!first.has("item_seller_get_response")) {
                log.warn("no resp????:" + rt);
                if (first.has("error_response")) {
                    parseErrorCode(localResponse, first);
                }

                return localResponse;
            }
            JSONObject respObj = first.getJSONObject("item_seller_get_response");
            if (!respObj.has("item")) {
                return localResponse;
            }

            JSONObject itemJson = respObj.getJSONObject("item");
            Item item = parseItemRespJson(itemJson);
            localResponse.setItem(item);

//            extractItems(items, respObj);

        } catch (JSONException e) {
            log.warn(e.getMessage(), e);
        } catch (ParseException e) {
            log.warn(e.getMessage(), e);
        }
        return localResponse;
    }
    
    protected ItemGetResponse validOldItemGetResp() throws ApiException {
        try {
            req.check();//if check failed,will throw ApiRuleException.
        } catch (ApiRuleException e) {
        	ItemGetResponse localResponse = null;
            try {
                localResponse = new ItemGetResponse();
            } catch (Exception e2) {
                throw new ApiException(e2);
            }
            localResponse.setErrorCode(e.getErrCode());
            localResponse.setMsg(e.getErrMsg());
            //localResponse.setBody("this.");
            return localResponse;
        }

        ItemGetResponse localResponse = new ItemGetResponse();
        Map<String, Object> rt = ((DefaultTaobaoClient) client).doPost(req, sid);
//        List<Item> items = new ArrayList<Item>();
//        localResponse.setItems(items);
//        Item item = new Item();
//        localResponse.setItem(item);
        try {
            JSONObject first = new JSONObject(rt.get("rsp").toString());
            if (!first.has("item_get_response")) {
                log.warn("no resp????:" + rt);
                if (first.has("error_response")) {
                    parseErrorCode(localResponse, first);
                }

                return localResponse;
            }
            JSONObject respObj = first.getJSONObject("item_get_response");
            if (!respObj.has("item")) {
                return localResponse;
            }

            JSONObject itemJson = respObj.getJSONObject("item");
            Item item = parseItemRespJson(itemJson);
            localResponse.setItem(item);

//            extractItems(items, respObj);

        } catch (JSONException e) {
            log.warn(e.getMessage(), e);
        } catch (ParseException e) {
            log.warn(e.getMessage(), e);
        }
        return localResponse;
    }

    protected ItemsSellerListGetResponse validItemListResp() throws ApiException {
        try {
            req.check();//if check failed,will throw ApiRuleException.
        } catch (ApiRuleException e) {
            ItemsSellerListGetResponse localResponse = null;
            try {
                localResponse = new ItemsSellerListGetResponse();
            } catch (Exception e2) {
                throw new ApiException(e2);
            }
            localResponse.setErrorCode(e.getErrCode());
            localResponse.setMsg(e.getErrMsg());
            //localResponse.setBody("this.");
            return localResponse;
        }
        ItemsSellerListGetResponse localResponse = new ItemsSellerListGetResponse();
        Map<String, Object> rt = ((DefaultTaobaoClient) client).doPost(req, sid);
        List<Item> items = new ArrayList<Item>();
        localResponse.setItems(items);
        try {
            JSONObject first = new JSONObject(rt.get("rsp").toString());
            if (!first.has("items_seller_list_get_response")) {
                log.warn("no resp????:" + rt);
                if (first.has("error_response")) {
                    parseErrorCode(localResponse, first);
                }

                return localResponse;
            }
            JSONObject respObj = first.getJSONObject("items_seller_list_get_response");
            extractItems(items, respObj);

        } catch (JSONException e) {
            log.warn(e.getMessage(), e);
        } catch (ParseException e) {
            log.warn(e.getMessage(), e);
        }
        return localResponse;
    }

    protected ItemsListGetResponse validOldItemListResp() throws ApiException {
        try {
            req.check();//if check failed,will throw ApiRuleException.
        } catch (ApiRuleException e) {
            ItemsListGetResponse localResponse = null;
            try {
                localResponse = new ItemsListGetResponse();
            } catch (Exception e2) {
                throw new ApiException(e2);
            }
            localResponse.setErrorCode(e.getErrCode());
            localResponse.setMsg(e.getErrMsg());
            //localResponse.setBody("this.");
            return localResponse;
        }
        ItemsListGetResponse localResponse = new ItemsListGetResponse();
        Map<String, Object> rt = ((DefaultTaobaoClient) client).doPost(req, sid);
        List<Item> items = new ArrayList<Item>();
        localResponse.setItems(items);
        try {
            JSONObject first = new JSONObject(rt.get("rsp").toString());
            if (!first.has("items_seller_list_get_response")) {
                log.warn("no resp????:" + rt);
                if (first.has("error_response")) {
                    parseErrorCode(localResponse, first);
                }

                return localResponse;
            }
            JSONObject respObj = first.getJSONObject("items_seller_list_get_response");
            extractItems(items, respObj);

        } catch (JSONException e) {
            log.warn(e.getMessage(), e);
        } catch (ParseException e) {
            log.warn(e.getMessage(), e);
        }
        return localResponse;
    }
    
    protected PictureGetResponse validGetPicResp() throws ApiException {
        try {
            req.check();//if check failed,will throw ApiRuleException.
        } catch (ApiRuleException e) {
            PictureGetResponse localResponse = null;
            try {
                localResponse = new PictureGetResponse();
            } catch (Exception e2) {
                throw new ApiException(e2);
            }
            localResponse.setErrorCode(e.getErrCode());
            localResponse.setMsg(e.getErrMsg());
            //localResponse.setBody("this.");
            return localResponse;
        }
        PictureGetResponse localResponse = new PictureGetResponse();
        Map<String, Object> rt = ((DefaultTaobaoClient) client).doPost(req, sid);
        List<Picture> pictures = new ArrayList<Picture>();
        localResponse.setPictures(pictures);

        /**
         * {
        "picture_get_response": {
        "totalResults": 100,
        "pictures": {
            "picture": [{
                "picture_id": 123,
                "picture_category_id": 12,
                "picture_path": "i3/156447112/Tdsxsiixxx.jpg",
                "title": "title",
                "sizes": 100,
                "uid": 1234567,
                "pixel": "450x150",
                "status": "unfroze",
                "gmt_create": "2000-01-01 00:00:00",
                "gmt_modified": "2000-01-01 00:00:00",
                "success": true
            }]
        }
        }
        }
         */
//        taobao.picture.get
        try {
            JSONObject first = new JSONObject(rt.get("rsp").toString());
//            log.info("[first:]" + first);
            if (!first.has("picture_get_response")) {
                log.warn("no resp????:" + rt);
                if (first.has("error_response")) {
                    parseErrorCode(localResponse, first);
                }

                return localResponse;
            }
            JSONObject respObj = first.getJSONObject("picture_get_response");
            if (respObj.has("totalResults")) {
                localResponse.setTotalResults(respObj.getLong("totalResults"));
            } else {
                localResponse.setTotalResults(null);
            }
//            log.warn(" json :" + respObj);
            if (respObj.has("pictures")) {
                JSONObject pictureObj = respObj.getJSONObject("pictures");
                if (pictureObj.has("picture")) {
                    JSONArray jsonArray = pictureObj.getJSONArray("picture");
                    int length = jsonArray.length();
                    for (int i = 0; i < length; i++) {
                        JSONObject picObj = jsonArray.getJSONObject(i);
                        Picture pic = new Picture();
                        parsePicJson(pic, picObj);
                        pictures.add(pic);
                    }
                }

            }

        } catch (JSONException e) {
            log.warn(e.getMessage(), e);
        } catch (ParseException e) {
            log.warn(e.getMessage(), e);

        }
        return localResponse;
    }

    protected PictureCategoryGetResponse validPictureCatResp() throws ApiException {
        try {
            req.check();//if check failed,will throw ApiRuleException.
        } catch (ApiRuleException e) {
            PictureCategoryGetResponse localResponse = null;
            try {
                localResponse = new PictureCategoryGetResponse();
            } catch (Exception e2) {
                throw new ApiException(e2);
            }
            localResponse.setErrorCode(e.getErrCode());
            localResponse.setMsg(e.getErrMsg());
            //localResponse.setBody("this.");
            return localResponse;
        }
        PictureCategoryGetResponse localResponse = new PictureCategoryGetResponse();
        Map<String, Object> rt = ((DefaultTaobaoClient) client).doPost(req, sid);
        List<PictureCategory> categories = new ArrayList<PictureCategory>();
        localResponse.setPictureCategories(categories);

        try {
            JSONObject first = new JSONObject(rt.get("rsp").toString());
//            log.info("[first:]" + first);
            if (!first.has("picture_category_get_response")) {
                log.warn("no resp????:" + rt);
                if (first.has("error_response")) {
                    parseErrorCode(localResponse, first);
                }

                return localResponse;
            }
            JSONObject respObj = first.getJSONObject("picture_category_get_response");

            if (respObj.has("picture_categories")) {
                JSONObject pictureObj = respObj.getJSONObject("picture_categories");
                if (pictureObj.has("picture_category")) {
                    JSONArray jsonArray = pictureObj.getJSONArray("picture_category");
                    int length = jsonArray.length();
                    for (int i = 0; i < length; i++) {
                        JSONObject picObj = jsonArray.getJSONObject(i);
                        categories.add(parsePicCatRespJson(picObj));
                    }
                }

            }

        } catch (JSONException e) {
            log.warn(e.getMessage(), e);
        } catch (ParseException e) {
            log.warn(e.getMessage(), e);

        }
        return localResponse;
    }

    private void parsePicJson(Picture pic, JSONObject picObj) throws JSONException, ParseException {

//        log.info("[json:]" + picObj);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (picObj.has("picture_id")) {
            pic.setPictureId(picObj.getLong("picture_id"));
        }
        if (picObj.has("picture_category_id")) {
            pic.setPictureCategoryId(picObj.getLong("picture_category_id"));
        }
        if (picObj.has("picture_path")) {
            pic.setPicturePath(picObj.getString("picture_path"));
        }
        if (picObj.has("title")) {
            pic.setTitle(picObj.getString("title"));
        }
        if (picObj.has("sizes")) {
            pic.setSizes(picObj.getLong("sizes"));
        }
        if (picObj.has("uid")) {
            pic.setUid(picObj.getLong("uid"));
        }
        if (picObj.has("pixel")) {
            pic.setPixel(picObj.getString("pixel"));
        }
        if (picObj.has("status")) {
            pic.setStatus(picObj.getString("status"));
        }
        if (picObj.has("gmt_create")) {
            pic.setCreated(sdf.parse(picObj.getString("gmt_create")));
        }
        if (picObj.has("gmt_modified")) {
            pic.setModified(sdf.parse(picObj.getString("gmt_modified")));
        }

        if (picObj.has("created")) {
            pic.setCreated(sdf.parse(picObj.getString("created")));
        }
        if (picObj.has("modified")) {
            pic.setModified(sdf.parse(picObj.getString("modified")));
        }
        if (picObj.has("referenced")) {
            pic.setReferenced(picObj.getBoolean("referenced"));
        }
        if (picObj.has("md5")) {
            pic.setMd5(picObj.getString("md5"));
        }

        if (picObj.has("deleted")) {
            pic.setDeleted(picObj.getString("deleted"));
        }

        if (picObj.has("client_type")) {
            pic.setClientType(picObj.getString("client_type"));
        }
    }

    protected ItemsOnsaleGetResponse validItemOnSaleResp() throws ApiException {
        try {
            req.check();//if check failed,will throw ApiRuleException.
        } catch (ApiRuleException e) {
            ItemsOnsaleGetResponse localResponse = null;
            try {
                localResponse = new ItemsOnsaleGetResponse();
            } catch (Exception e2) {
                throw new ApiException(e2);
            }
            localResponse.setErrorCode(e.getErrCode());
            localResponse.setMsg(e.getErrMsg());
            //localResponse.setBody("this.");
            return localResponse;
        }
        ItemsOnsaleGetResponse localResponse = new ItemsOnsaleGetResponse();
        Map<String, Object> rt = ((DefaultTaobaoClient) client).doPost(req, sid);
        List<Item> items = new ArrayList<Item>();
        localResponse.setItems(items);
        try {
            JSONObject first = new JSONObject(rt.get("rsp").toString());
            if (!first.has("items_onsale_get_response")) {
                log.warn("no resp????:" + rt);
                localResponse.setTotalResults(-1L);
                log.error(" start to parse error resp:" + first);
                if (first.has("error_response")) {
                    parseErrorCode(localResponse, first);
                }

                return localResponse;
            }
            JSONObject respObj = first.getJSONObject("items_onsale_get_response");

            if (respObj.has("total_results")) {
                Long totalNum = respObj.getLong("total_results");
                localResponse.setTotalResults(totalNum);
            } else {
                localResponse.setTotalResults(NumberUtil.DEFAULT_LONG);
            }

            extractItems(items, respObj);

        } catch (JSONException e) {
            log.warn(e.getMessage(), e);
        } catch (ParseException e) {
            log.warn(e.getMessage(), e);
        }
        return localResponse;
    }

    private void extractItems(List<Item> items, JSONObject obj) throws JSONException, ParseException {
        if (!obj.has("items")) {
            return;
        }

        JSONArray arr = obj.getJSONObject("items").getJSONArray("item");
        int size = arr.length();
        for (int i = 0; i < size; i++) {
            JSONObject itemJson = arr.getJSONObject(i);
            Item item = parseItemRespJson(itemJson);
            items.add(item);
        }
    }

    private void parseErrorCode(TaobaoResponse localResponse, JSONObject first) throws JSONException {
        JSONObject obj = first.getJSONObject("error_response");
        if (obj.has("msg")) {
            localResponse.setMsg(obj.getString("msg"));
        }
        if (obj.has("sub_code")) {
            localResponse.setSubCode(obj.getString("sub_code"));
        }
        if (obj.has("sub_msg")) {
            localResponse.setSubMsg(obj.getString("sub_msg"));
        }
        if (obj.has("code")) {
            localResponse.setErrorCode(obj.get("code").toString());
        }
    }

    public static Trade parseTradeRespJson(JSONObject tradeJson) throws JSONException, ParseException {
        Trade trade = new Trade();
//        log.info("[trade]" + tradeJson);
        if (tradeJson.has("trade_fullinfo_get_response")) {
            tradeJson = tradeJson.getJSONObject("trade_fullinfo_get_response");
        }
        if (tradeJson.has("trade")) {
            tradeJson = tradeJson.getJSONObject("trade");
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (tradeJson.has("alipay_id")) {
            trade.setAlipayId(tradeJson.getLong("alipay_id"));
        }
        if (tradeJson.has("buyer_alipay_no")) {
            trade.setBuyerAlipayNo(tradeJson.getString("buyer_alipay_no"));
        }
        if (tradeJson.has("buyer_area")) {
            trade.setBuyerArea(tradeJson.getString("buyer_area"));
        }
        if (tradeJson.has("buyer_cod_fee")) {
            trade.setBuyerCodFee(tradeJson.getString("buyer_cod_fee"));
        }
        if (tradeJson.has("buyer_email")) {
            trade.setBuyerEmail(tradeJson.getString("buyer_email"));
        }
        if (tradeJson.has("buyer_nick")) {
            trade.setBuyerNick(tradeJson.getString("buyer_nick"));
        }
        if (tradeJson.has("buyer_rate")) {
            trade.setBuyerRate(tradeJson.getBoolean("buyer_rate"));
        }
        if (tradeJson.has("created")) {
            Date createdDate = sdf.parse(tradeJson.getString("created"));
            trade.setCreated(createdDate);
        }
        if (tradeJson.has("consign_time")) {
            Date endDate = sdf.parse(tradeJson.getString("consign_time"));
            trade.setConsignTime(endDate);
        }
        if (tradeJson.has("end_time")) {
            Date endDate = sdf.parse(tradeJson.getString("end_time"));
            trade.setEndTime(endDate);
        }
        if (tradeJson.has("modified")) {
            Date modifiedDate = sdf.parse(tradeJson.getString("modified"));
            trade.setModified(modifiedDate);
        }
        if (tradeJson.has("num")) {
            trade.setNum(tradeJson.getLong("num"));
        }
        if (tradeJson.has("num_iid")) {
            trade.setNumIid(tradeJson.getLong("num_iid"));
        }

        if (tradeJson.has("orders")) {
            JSONObject ordersObj = tradeJson.getJSONObject("orders");
            if (ordersObj.has("order")) {

                JSONArray orderArr = ordersObj.getJSONArray("order");
                int orderSize = orderArr.length();
                List<Order> orderList = new ArrayList<Order>(orderSize);
                trade.setOrders(orderList);

                for (int i = 0; i < orderSize; i++) {
                    JSONObject orderJson = orderArr.getJSONObject(i);
                    Order order = new Order();
                    orderList.add(order);

                    if (orderJson.has("oid")) {
                        order.setOid(Long.parseLong(orderJson.getString("oid")));
                    }
                    if (orderJson.has("buyer_rate")) {
                        order.setBuyerRate(orderJson.getBoolean("buyer_rate"));
                    }
                    if (orderJson.has("buyer_nick")) {
                        order.setBuyerNick(orderJson.getString("buyer_nick"));
                    }
                    if (orderJson.has("cid")) {
                        order.setNum(orderJson.getLong("cid"));
                    }
                    if (orderJson.has("end_time")) {
                        Date endDate = sdf.parse(orderJson.getString("end_time"));
                        order.setEndTime(endDate);
                    }
                    if (orderJson.has("num")) {
                        order.setNum(orderJson.getLong("num"));
                    }
                    if (orderJson.has("num_iid")) {
                        order.setNumIid(orderJson.getLong("num_iid"));
                    }
                    if (orderJson.has("payment")) {
                        order.setPayment(orderJson.getString("payment"));
                    }

                    if (orderJson.has("pic_path")) {
                        order.setPicPath(orderJson.getString("pic_path"));
                    }

                    if (orderJson.has("price")) {
                        order.setPrice(orderJson.getString("price"));
                    }

                    if (orderJson.has("seller_rate")) {
                        order.setSellerRate(orderJson.getBoolean("seller_rate"));
                    }
                    if (orderJson.has("seller_nick")) {
                        order.setSellerNick(orderJson.getString("seller_nick"));
                    }

                    if (orderJson.has("status")) {
                        order.setStatus(orderJson.getString("status"));
                    }

                    if (orderJson.has("title")) {
                        order.setTitle(orderJson.getString("title"));
                    }

                    if (orderJson.has("total_fee")) {
                        order.setTotalFee(orderJson.getString("total_fee"));
                    }

                }
            }
        }

        if (tradeJson.has("pay_time")) {
            Date endDate = sdf.parse(tradeJson.getString("pay_time"));
            trade.setPayTime(endDate);
        }

        if (tradeJson.has("payment")) {
            trade.setPayment(tradeJson.getString("payment"));
        }
        if (tradeJson.has("post_fee")) {
            trade.setPostFee(tradeJson.getString("post_fee"));
        }

        if (tradeJson.has("pic_path")) {
            trade.setPicPath(tradeJson.getString("pic_path"));
        }
        if (tradeJson.has("price")) {
            trade.setPrice(tradeJson.getString("price"));
        }
        if (tradeJson.has("receiver_address")) {
            trade.setReceiverAddress(tradeJson.getString("receiver_address"));
        }
        if (tradeJson.has("receiver_city")) {
            trade.setReceiverCity(tradeJson.getString("receiver_city"));
        }
        if (tradeJson.has("receiver_district")) {
            trade.setReceiverDistrict(tradeJson.getString("receiver_district"));
        }
        if (tradeJson.has("receiver_mobile")) {
            trade.setReceiverMobile(tradeJson.getString("receiver_mobile"));
        }

        if (tradeJson.has("receiver_name")) {
            trade.setReceiverName(tradeJson.getString("receiver_name"));
        }
        if (tradeJson.has("receiver_phone")) {
            trade.setReceiverPhone(tradeJson.getString("receiver_phone"));
        }
        if (tradeJson.has("receiver_state")) {
            trade.setReceiverState(tradeJson.getString("receiver_state"));
        }
        if (tradeJson.has("receiver_zip")) {
            trade.setReceiverZip(tradeJson.getString("receiver_zip"));
        }
        if (tradeJson.has("received_payment")) {
            trade.setReceivedPayment(tradeJson.getString("received_payment"));
        }
        ;
        if (tradeJson.has("seller_name")) {
            trade.setSellerName(tradeJson.getString("seller_name"));
        }

        if (tradeJson.has("seller_nick")) {
            trade.setSellerNick(tradeJson.getString("seller_nick"));
        }

        if (tradeJson.has("seller_rate")) {
            trade.setSellerRate(tradeJson.getBoolean("seller_rate"));
        }
        if (tradeJson.has("status")) {
            trade.setStatus(tradeJson.getString("status"));
        }
        if (tradeJson.has("tid")) {
            trade.setTid(Long.parseLong(tradeJson.getString("tid")));
        }
        if (tradeJson.has("type")) {
            trade.setType(tradeJson.getString("type"));
        }
        if (tradeJson.has("type")) {
            trade.setType(tradeJson.getString("type"));
        }
        if (tradeJson.has("trade_from")) {
            trade.setTradeFrom(tradeJson.getString("trade_from"));
        }
        if (tradeJson.has("total_fee")) {
            trade.setTotalFee(tradeJson.getString("total_fee"));
        }

        return trade;
    }

    /**
    {
    "traderates_get_response": {
        "trade_rates": {
            "trade_rate": [{
                "content": "好评！",
                "created": "2010-05-20 22:00:37",
                "item_price": "1.2",
                "item_title": "【24小时自动电脑充值】海南移动快充1元67.la#AAA",
                "nick": "easesou",
                "oid": 37750250222274,
                "rated_nick": "匿名",
                "result": "good",
                "role": "seller",
                "tid": 37750250222274,
                "num_iid": 1234
            },
            {
                "content": "好评！",
                "created": "2010-05-20 22:00:37",
                "item_price": "1.2",
                "item_title": "【24小时自动电脑充值】湖北移动快充1元67.la#AAA",
                "nick": "easesou",
                "oid": 37749751692274,
                "rated_nick": "匿名",
                "result": "good",
                "role": "seller",
                "tid": 37749751692274,
                "num_iid": 5678
            }]
        },
        "total_results": 12
    }
    }
     * @param itemJson
     * @return
     * @throws JSONException
     * @throws ParseException
     */
    public static TradeRate parseTradeRateRespJson(JSONObject itemJson) throws JSONException, ParseException {
        TradeRate rate = new TradeRate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (itemJson.has("content")) {
            rate.setContent(itemJson.getString("content"));
        }
        if (itemJson.has("created")) {
            Date created = sdf.parse(itemJson.getString("created"));
            rate.setCreated(created);
        }
        if (itemJson.has("item_price")) {
            rate.setItemPrice(itemJson.getString("item_price"));
        }
        if (itemJson.has("item_title")) {
            rate.setItemTitle(itemJson.getString("item_title"));
        }
        if (itemJson.has("nick")) {
            rate.setNick(itemJson.getString("nick"));
        }
        if (itemJson.has("oid")) {
            rate.setOid(Long.parseLong(itemJson.getString("oid")));

        }
        if (itemJson.has("rated_nick")) {
            rate.setRatedNick(itemJson.getString("rated_nick"));
        }
        if (itemJson.has("result")) {
            rate.setResult(itemJson.getString("result"));
        }
        if (itemJson.has("role")) {
            rate.setRole(itemJson.getString("role"));
        }

        if (itemJson.has("tid")) {
            rate.setTid(Long.parseLong(itemJson.getString("tid")));

        }
        if (itemJson.has("num_iid")) {
            Long numIid = itemJson.getLong("num_iid");
            rate.setNumIid(numIid);
        }

        return rate;
    }

    /**
     * {
    05                  "picture_category_id": 1234,
    06                  "picture_category_name": "名称",
    07                  "uid": 12345678,
    08                  "sorts": 1,
    09                  "type": "sys-fixture",
    10                  "total": 100,
    11                  "gmt_create": "2000-01-01 00:00:00",
    12                  "gmt_modified": "2000-01-01 00:00:00",
    13                  "parent_id": 0
    14              }
     * @param json
     * @return
     * @throws JSONException
     * @throws ParseException
     */
    public static PictureCategory parsePicCatRespJson(JSONObject json) throws JSONException, ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        PictureCategory cat = new PictureCategory();
        if (json.has("picture_category_id")) {
            cat.setPictureCategoryId(json.getLong("picture_category_id"));
        }
        if (json.has("picture_category_name")) {
            cat.setPictureCategoryName(json.getString("picture_category_name"));
        }
        if (json.has("created")) {
            Date created = sdf.parse(json.getString("created"));
            cat.setCreated(created);
        }
        if (json.has("modified")) {
            Date modified = sdf.parse(json.getString("modified"));
            cat.setModified(modified);
        }
        if (json.has("picture_category_name")) {
            cat.setPictureCategoryName(json.getString("picture_category_name"));
        }
        if (json.has("type")) {
            cat.setType(json.getString("type"));
        }
        if (json.has("position")) {
            cat.setPosition(json.getLong("position"));
        }
        if (json.has("parent_id")) {
            cat.setParentId(json.getLong("parent_id"));
        }

        return cat;
    }

    public static Item parseItemRespJson(JSONObject json) throws JSONException, ParseException {
//        log.info("[parse json]" + json);
        Item item = new Item();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Long numIid = json.getLong("num_iid");
        item.setNumIid(numIid);
//        log.info("[gson :]" + json);

        if (json.has("after_sale_id")) {
            item.setAfterSaleId(json.getLong("after_sale_id"));
        }

        if (json.has("approve_status")) {
            item.setApproveStatus(json.getString("approve_status"));
        }

        if (json.has("auction_point")) {
            item.setAuctionPoint(json.getLong("auction_point"));
        }

        if (json.has("auto_fill")) {
            item.setAutoFill(json.getString("auto_fill"));
        }

        if (json.has("cid")) {
            item.setCid(json.getLong("cid"));
        }

        if (json.has("cod_postage_id")) {
            item.setCodPostageId(json.getLong("cod_postage_id"));
        }

        if (json.has("created")) {
            Date delistDate = sdf.parse(json.getString("created"));
            item.setCreated(delistDate);
        }

        if (json.has("delist_time")) {
            Date delistDate = sdf.parse(json.getString("delist_time"));
            item.setDelistTime(delistDate);
        }

        if (json.has("desc")) {
            item.setDesc(json.getString("desc"));
        }
        if (json.has("detail_url")) {
            item.setDetailUrl(json.getString("detail_url"));
        }

        if (json.has("desc_module_info")) {
            DescModuleInfo module = new DescModuleInfo();
            item.setDescModuleInfo(module);

            JSONObject info = json.getJSONObject("desc_module_info");
            if (info.has("type")) {
                module.setType(info.getLong("type"));
            }
            if (info.has("anchor_module_ids")) {
                module.setAnchorModuleIds(info.getString("anchor_module_ids"));
            }
        }
        if (json.has("desc_modules")) {
            item.setDescModules(json.getString("desc_modules"));
        }

        if (json.has("ems_fee")) {
            item.setEmsFee(json.getString("ems_fee"));
        }

        if (json.has("express_fee")) {
            item.setExpressFee(json.getString("express_fee"));
        }

        if (json.has("freight_payer")) {
            item.setFreightPayer(json.getString("freight_payer"));
        }

        if (json.has("has_discount")) {
            item.setHasDiscount(json.getBoolean("has_discount"));
        }

        if (json.has("has_invoice")) {
            item.setHasInvoice(json.getBoolean("has_invoice"));
        }

        /*
         * 
         * 食品安全信息，包括：生产许可证编号、产品标准号、厂名、厂址等
         * @ApiField("food_security")
         * private FoodSecurity foodSecurity;
         * 这个字段还没有启用
         */
        if (json.has("has_showcase")) {
            item.setHasShowcase(json.getBoolean("has_showcase"));
        }

        if (json.has("has_warranty")) {
            item.setHasWarranty(json.getBoolean("has_warranty"));
        }

        if (json.has("increment")) {
            item.setIncrement(json.getString("increment"));
        }

        if (json.has("inner_shop_auction_template_id")) {
            item.setInnerShopAuctionTemplateId(json.getLong("inner_shop_auction_template_id"));
        }

        if (json.has("input_pids")) {
            item.setInputPids(json.getString("input_pids"));
        }

        if (json.has("input_str")) {
            item.setInputStr(json.getString("input_str"));
        }

        if (json.has("is_3D")) {
            item.setIs3D(json.getBoolean("is_3D"));
        }
        if (json.has("is_ex")) {
            item.setIsEx(json.getBoolean("is_ex"));
        }

        /**
         * 非分销商品：0，代销：1，经销：2
         */
        if (json.has("is_fenxiao")) {
            item.setIsFenxiao(json.getLong("is_fenxiao"));
        }

        if (json.has("is_lightning_consignment")) {
            item.setIsLightningConsignment(json.getBoolean("is_lightning_consignment"));
        }
        if (json.has("is_prepay")) {
            item.setIsPrepay(json.getBoolean("is_prepay"));
        }

        if (json.has("is_taobao")) {
            item.setIsTaobao(json.getBoolean("is_taobao"));
        }

        if (json.has("is_timing")) {
            item.setIsTiming(json.getBoolean("is_timing"));
        }

        if (json.has("is_virtual")) {
            item.setIsVirtual(json.getBoolean("is_virtual"));
        }
        if (json.has("is_xinpin")) {
            item.setIsXinpin(json.getBoolean("is_xinpin"));
        }

        if (json.has("item_imgs")) {
            log.info("[item imgs:]" + json.get("item_imgs"));
        }
        if (json.has("item_img")) {
            log.info("[item img:]" + json.get("item_img"));
        }
        if (json.has("item_imgs")) {
//            log.info("[json]" + json.getJSONObject("item_imgs"));
            JSONObject imgJsons = json.getJSONObject("item_imgs");

            List<ItemImg> imgs = new ArrayList<ItemImg>();
            item.setItemImgs(imgs);

            if (imgJsons.has("item_img")) {
                JSONArray skuArr = imgJsons.getJSONArray("item_img");
                int length = skuArr.length();
                for (int i = 0; i < length; i++) {
                    JSONObject imgjson = skuArr.getJSONObject(i);
                    ItemImg img = new ItemImg();
                    imgs.add(img);

                    if (imgjson.has("created")) {
                        Date created = sdf.parse(imgjson.getString("created"));
                        img.setCreated(created);
                    }
                    if (imgjson.has("id")) {
                        img.setId(imgjson.getLong("id"));
                    }
                    if (imgjson.has("position")) {
                        img.setPosition(imgjson.getLong("position"));
                    }
                    if (imgjson.has("url")) {
                        img.setUrl(imgjson.getString("url"));
                    }
                }

            }
        }

        if (json.has("list_time")) {
            Date listTime = sdf.parse(json.getString("list_time"));
            item.setListTime(listTime);
        }
        if (json.has("location")) {
            Location loc = new Location();
            item.setLocation(loc);

            JSONObject locJson = json.getJSONObject("location");
//            log.info("[loca json:]" + locJson);
            if (locJson.has("address")) {
                loc.setAddress(locJson.getString("address"));
            }
            if (locJson.has("city")) {
                loc.setCity(locJson.getString("city"));
            }
            if (locJson.has("country")) {
                loc.setCountry(locJson.getString("country"));
            }
            if (locJson.has("district")) {
                loc.setDistrict(locJson.getString("district"));
            }
            if (locJson.has("state")) {
                loc.setState(locJson.getString("state"));
            }
            if (locJson.has("zip")) {
                loc.setZip(locJson.getString("zip"));
            }

//            log.info("[location:]" + new Gson().toJson(loc));
        }

        if (json.has("modified")) {
            Date modified = sdf.parse(json.getString("modified"));
            item.setModified(modified);
        }

        if (json.has("nick")) {
            item.setNick(json.getString("nick"));
        }
        if (json.has("num")) {
            long num = json.getLong("num");
            item.setNum(num);
        }

        if (json.has("one_station")) {
            item.setOneStation(json.getBoolean("one_station"));
        }

        if (json.has("outer_id")) {
            item.setOuterId(json.getString("outer_id"));
        }

        if (json.has("outer_shop_auction_template_id")) {
            item.setOuterShopAuctionTemplateId(json.getLong("outer_shop_auction_template_id"));
        }

        if (json.has("pic_url")) {
            item.setPicUrl(json.getString("pic_url"));
        }

        if (json.has("post_fee")) {
            item.setPostFee(json.getString("post_fee"));
        }
        if (json.has("postage_id")) {
            item.setPostageId(json.getLong("postage_id"));
        }

        if (json.has("price")) {
            item.setPrice(json.getString("price"));
        }

        if (json.has("product_id")) {
            item.setProductId(json.getLong("product_id"));
        }
        if (json.has("promoted_service")) {
            item.setPromotedService(json.getString("promoted_service"));
        }

        if (json.has("prop_imgs")) {
            List<PropImg> imgs = new ArrayList<PropImg>();
            item.setPropImgs(imgs);

            log.info("[prop imgs:]" + json.getJSONObject("prop_imgs"));
            JSONObject propJsonImgs = json.getJSONObject("prop_imgs");
            if (propJsonImgs.has("prop_img")) {
                JSONArray propImgArr = propJsonImgs.getJSONArray("prop_img");
                int length = propImgArr.length();
                for (int i = 0; i < length; i++) {
                    JSONObject imgjson = propImgArr.getJSONObject(i);
                    PropImg img = new PropImg();
                    imgs.add(img);

                    if (imgjson.has("created")) {
                        Date created = sdf.parse(imgjson.getString("created"));
                        img.setCreated(created);
                    }
                    if (imgjson.has("id")) {
                        img.setId(imgjson.getLong("id"));
                    }
                    if (imgjson.has("position")) {
                        img.setPosition(imgjson.getLong("position"));
                    }
                    if (imgjson.has("properties")) {
                        img.setProperties(imgjson.getString("properties"));
                    }
                    if (imgjson.has("url")) {
                        img.setUrl(imgjson.getString("url"));
                    }
                }
            }
        }

        if (json.has("property_alias")) {
            String propAlias = json.getString("property_alias");
            item.setPropertyAlias(propAlias);
        }
        if (json.has("props")) {
            item.setProps(json.getString("props"));
        }
        if (json.has("props_name")) {
            item.setPropsName(json.getString("props_name"));
        }
        /*
         * score
         * second_kill
         */
        if (json.has("sell_promise")) {
            item.setSellPromise(json.getBoolean("sell_promise"));
        }
        if (json.has("seller_cids")) {
            item.setSellerCids(json.getString("seller_cids"));
        }
//        log.info("[skus:]" + json.get("skus"));

        if (json.has("skus")) {
            List<Sku> skus = new ArrayList<Sku>();
            item.setSkus(skus);
            try {
                JSONObject skusJsonObj = json.getJSONObject("skus");
//                log.info("[sku :]" + skusJsonObj);
                if (skusJsonObj.has("sku")) {
                    JSONArray skuArr = skusJsonObj.getJSONArray("sku");
                    int length = skuArr.length();
                    for (int i = 0; i < length; i++) {
                        JSONObject skuJson = skuArr.getJSONObject(i);
                        Sku sku = new Sku();
                        skus.add(sku);
                        if (skuJson.has("properties")) {
                            sku.setProperties(skuJson.getString("properties"));
                        }
                        if (skuJson.has("properties_name")) {
                            sku.setPropertiesName(skuJson.getString("properties_name"));
                        }
                        if (skuJson.has("quantity")) {
                            sku.setQuantity(skuJson.getLong("quantity"));
                        }
                        if (skuJson.has("outer_id")) {
                            sku.setOuterId(skuJson.getString("outer_id"));
                        }
                        if (skuJson.has("sku_id")) {
                            sku.setSkuId(skuJson.getLong("sku_id"));
                        }
                        if (skuJson.has("num_iid")) {
                            sku.setNumIid(skuJson.getLong("num_iid"));
                        }
                        if (skuJson.has("price")) {
                            sku.setPrice(skuJson.getString("price"));
                        }
                        if (skuJson.has("created")) {
                            sku.setCreated(skuJson.getString("created"));
                        }
                        if (skuJson.has("modified")) {
                            sku.setModified(skuJson.getString("modified"));
                        }
                        if (skuJson.has("skuSpecId")) {
                            sku.setSkuSpecId(skuJson.getLong("skuSpecId"));
                        }
                        if (skuJson.has("change_prop")) {
                            sku.setChangeProp(skuJson.getString("change_prop"));
                        }
                        //"outer_id":"EAN-8","created":"2012-10-12 23:17:53","barcode":"69012341"
                        if (skuJson.has("barcode")) {
                            sku.setBarcode(skuJson.getString("barcode"));
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("bad sku :" + json.get("skus"));
                log.warn(e.getMessage(), e);

            }
        }
        if (json.has("stuff_status")) {
            item.setStuffStatus(json.getString("stuff_status"));
        }
        if (json.has("sub_stock")) {
            item.setSubStock(json.getLong("sub_stock"));
        }
//        sell_point
        if (json.has("sell_point")) {
            item.setSellPoint(json.getString("sell_point"));
        }

        if (json.has("template_id")) {
            item.setTemplateId(json.getString("template_id"));
        }

        if (json.has("title")) {
            item.setTitle(json.getString("title"));
        }

        if (json.has("type")) {
            item.setType(json.getString("type"));
        }
        if (json.has("valid_thru")) {
            item.setValidThru(json.getLong("valid_thru"));
        }
        if (json.has("videos")) {
            JSONArray videoJsonArr = json.getJSONObject("videos").getJSONArray("video");
            int length = videoJsonArr.length();
            List<Video> videos = new ArrayList<Video>(length);
            item.setVideos(videos);
            for (int i = 0; i < length; i++) {
                JSONObject imgjson = videoJsonArr.getJSONObject(i);
                Video img = new Video();
                videos.add(img);

                if (imgjson.has("created")) {
                    Date created = sdf.parse(imgjson.getString("created"));
                    img.setCreated(created);
                }
                if (imgjson.has("modified")) {
                    Date modified = sdf.parse(imgjson.getString("modified"));
                    img.setModified(modified);
                }
                if (imgjson.has("id")) {
                    img.setId(imgjson.getLong("id"));
                }
                if (imgjson.has("num_iid")) {
                    img.setNumIid(imgjson.getLong("num_iid"));
                }
                if (imgjson.has("video_id")) {
                    img.setVideoId(imgjson.getLong("video_id"));
                }
                if (imgjson.has("url")) {
                    img.setUrl(imgjson.getString("url"));
                }
            }
        }

        if (json.has("violation")) {
            item.setViolation(json.getBoolean("violation"));
        }

        if (json.has("wireless_desc")) {
            item.setWirelessDesc(json.getString("wireless_desc"));
        }

        if (json.has("global_stock_country")) {
            item.setGlobalStockCountry(json.getString("global_stock_country"));
        }
        if (json.has("global_stock_type")) {
            item.setGlobalStockType(json.getString("global_stock_type"));
        }
        if (json.has("features")) {
            item.setFeatures(json.getString("features"));
        }
        if (json.has("item_weight")) {
            item.setItemWeight(json.getString("item_weight"));
        }
        if (json.has("item_size")) {
            item.setItemSize(json.getString("item_size"));
        }
        if (json.has("custom_made_type_id")) {
            item.setCustomMadeTypeid(json.getString("custom_made_type_id"));
        }

        if (json.has("barcode")) {
            item.setBarcode(json.getString("barcode"));
        }

        if (json.has("newprepay")) {
            log.error(" new pre pay:" + json.getString("newprepay"));
            item.setNewprepay(json.getString("newprepay"));
        }

        long curr = System.currentTimeMillis();
        if (item.getDelistTime() != null && item.getDelistTime().getTime() < curr
                && (item.getIsTiming() == null || !item.getIsTiming()) && "onsale".equals(item.getApproveStatus())) {

            long oldDelistTime = item.getDelistTime().getTime();
            long weekmillis = DateUtil.WEEK_MILLIS;
            long newDelistTime = curr / weekmillis * weekmillis + oldDelistTime % weekmillis;
            if (newDelistTime < curr) {
                newDelistTime += weekmillis;
            }
            item.setDelistTime(new Date(newDelistTime));
//            log.info("[parsed old delist time :[" + oldDelistTime + "]new delist time : [" + newDelistTime + "]");
//            log.info("[parsed old delist time :[" + DateUtil.formDateForLog(oldDelistTime) + "]new delist time : ["
//                    + DateUtil.formDateForLog(newDelistTime) + "]");
        }

        return item;
    }

    public enum TMValueType {
        String, Boolean, Integer, Long, Date
    }

    public enum JdpApiField {
        modified("modified", TMValueType.Date),
        hasShowcase("has_showcase", TMValueType.Boolean),
        isTiming("is_timing", TMValueType.Boolean),
        isFenxiao("is_fenxiao", TMValueType.Long);

        JdpApiField(String name, TMValueType vType) {
            this.name = name;
            this.vType = vType;

        }

        private String name;

        private TMValueType vType;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public TMValueType getvType() {
            return vType;
        }

        public void setvType(TMValueType vType) {
            this.vType = vType;
        }

    }

//    static enum API_FIELD_

//    @Override
//    protected ShopRemainshowcaseGetResponse execProcess() throws ApiException {
    protected ShopRemainshowcaseGetResponse validateWindowCase() throws ApiException {

        try {
            req.check();//if check failed,will throw ApiRuleException.
        } catch (ApiRuleException e) {
            ShopRemainshowcaseGetResponse localResponse = null;
            try {
                localResponse = new ShopRemainshowcaseGetResponse();
            } catch (Exception e2) {
                throw new ApiException(e2);
            }
            localResponse.setErrorCode(e.getErrCode());
            localResponse.setMsg(e.getErrMsg());
            //localResponse.setBody("this.");
            return localResponse;
        }
        ShopRemainshowcaseGetResponse localResponse = new ShopRemainshowcaseGetResponse();
        Map<String, Object> rt = ((DefaultTaobaoClient) client).doPost(req, sid);
//        log.error("back res:" + rt.get("rsp"));
        try {
            JSONObject first = new JSONObject(rt.get("rsp").toString());

            if ((!first.has("shop_remainshowcase_get_response")) && first.has("error_response")) {
                JSONObject obj = first.getJSONObject("error_response");
                if (obj.has("msg")) {
                    localResponse.setMsg(obj.getString("msg"));
                }
                if (obj.has("sub_code")) {
                    localResponse.setSubCode(obj.getString("sub_code"));
                }
                if (obj.has("sub_msg")) {
                    localResponse.setSubMsg(obj.getString("sub_msg"));
                }
                if (obj.has("code")) {
                    localResponse.setErrorCode(obj.get("code").toString());
                }
                return localResponse;
            }

            Shop shop = new Shop();
            localResponse.setShop(shop);
            shop.setAllCount(NumberUtil.DEFAULT_LONG);
            shop.setRemainCount(NumberUtil.DEFAULT_LONG);
            shop.setUsedCount(NumberUtil.DEFAULT_LONG);
            if (!first.has("shop_remainshowcase_get_response")) {
                return localResponse;
            }

            JSONObject obj = first.getJSONObject("shop_remainshowcase_get_response");
            JSONObject shopObj = obj.getJSONObject("shop");

            if (shopObj.has("all_count")) {
                shop.setAllCount(shopObj.getLong("all_count"));
            } else {

            }
            if (shopObj.has("remain_count")) {
                shop.setRemainCount(shopObj.getLong("remain_count"));
            } else {

            }
            if (shopObj.has("used_count")) {
                shop.setUsedCount(shopObj.getLong("used_count"));
            } else {

            }

        } catch (JSONException e) {
            log.warn(e.getMessage(), e);
        }
        return localResponse;
    }

    public void stopRetry() {
        this.retryTime = 0;
    }

    public String getSubErrorMsg() {
        return subErrorMsg;
    }

    public void setSubErrorMsg(String subErrorMsg) {
        this.subErrorMsg = subErrorMsg;
    }

}
