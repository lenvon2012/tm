
package actions.promotion;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import models.promotion.Promotion;
import models.promotion.TMProActivity;
import models.promotion.UserTag;
import models.promotion.VipConfig;
import models.user.User;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import bustbapi.TBApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.taobao.api.ApiException;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.MarketingPromotionAddRequest;
import com.taobao.api.request.MarketingPromotionDeleteRequest;
import com.taobao.api.request.MarketingPromotionUpdateRequest;
import com.taobao.api.request.MarketingPromotionsGetRequest;
import com.taobao.api.request.MarketingTagAddRequest;
import com.taobao.api.request.MarketingTagDeleteRequest;
import com.taobao.api.request.MarketingTagsGetRequest;
import com.taobao.api.request.MarketingTaguserAddRequest;
import com.taobao.api.request.MarketingTaguserDeleteRequest;
import com.taobao.api.response.MarketingPromotionAddResponse;
import com.taobao.api.response.MarketingPromotionDeleteResponse;
import com.taobao.api.response.MarketingPromotionUpdateResponse;
import com.taobao.api.response.MarketingPromotionsGetResponse;
import com.taobao.api.response.MarketingTagAddResponse;
import com.taobao.api.response.MarketingTagDeleteResponse;
import com.taobao.api.response.MarketingTagsGetResponse;
import com.taobao.api.response.MarketingTaguserAddResponse;
import com.taobao.api.response.MarketingTaguserDeleteResponse;

import configs.TMConfigs;
import dao.UserDao;
import dao.item.ItemDao;

;
public class PromotionAction {
    public static final Logger log = LoggerFactory.getLogger(PromotionAction.class);

    public static final String TAG = "PromotionAction";

    public static final String Promotion_SQL = " select id, userId, activityId, decreaseNum, discountType, discountValue, startDate, "
            +
            "endDate, numIid, promotionDesc, promotionTitle, userTagId, status from " + Promotion.TABLE_NAME;

    public static final String Count_SQL = " select count(*) from " + Promotion.TABLE_NAME;

    public static boolean checkDiscountTypeParam(String discountType) {
        if (!discountType.equals(Promotion.Type.PRICE)
                && !discountType.equals(Promotion.Type.DISCOUNT)) {
            return false;
        }
        return true;
    }

    public static List<com.taobao.api.domain.Promotion> getPromotions(String sid, Long numiids[]) throws ApiException {
        //User user = User.find("bySessionKey", sid).first();
        User user = UserDao.findBySessionKey(sid);
        if (user == null) {
            return null;
        }
        List<com.taobao.api.domain.Promotion> promotionList = new ArrayList<com.taobao.api.domain.Promotion>();
        TaobaoClient client = TBApi.genClient();
        MarketingPromotionsGetRequest req = new MarketingPromotionsGetRequest();
        String numIIdsStr = "";
        // log.info("Add promotion numIIds is:"+numIIdsStr);
        Set<String> itemSet = new HashSet<String>();
        for (int looper = 0; looper < numiids.length; ++looper) {
            itemSet.add(String.valueOf(numiids[looper]));
        }
        numIIdsStr = StringUtils.join(itemSet, ",");

        req.setNumIid(numIIdsStr);
        req.setFields("promotion_id, promotion_title, num_iid");

        log.info("MarketingPromotionsGetRequest with sid=" + sid);
        MarketingPromotionsGetResponse response = client.execute(req, sid);
        log.info("MarketingPromotionsGetResponse:" + response.getBody());
        try {
            if (response.isSuccess()) {
                JSONObject obj = new JSONObject(CommonUtils.stringReplaceNewLineWithSpace(response.getBody()));
                if (obj != null) {
                    JSONObject promotions = obj.getJSONObject(
                            "marketing_promotions_get_response").getJSONObject(
                            "promotions");
                    JSONArray promotion_array = promotions
                            .getJSONArray("promotion");
                    Long p_id = -1L;
                    for (int i = 0; i < promotion_array.length(); i++) {
                        JSONObject promotion_obj = (JSONObject) promotion_array
                                .get(i);

                        p_id = promotion_obj.getLong("promotion_id");
                        Long p_num_iid = promotion_obj.getLong("num_iid");

                        String p_title = promotion_obj.getString("promotion_title");

                        com.taobao.api.domain.Promotion promotion = new com.taobao.api.domain.Promotion();
                        promotion.setPromotionTitle(p_title);
                        promotion.setNumIid(p_num_iid);
                        promotion.setPromotionId(p_id);
                        promotionList.add(promotion);
                    }
                }
            } else {
                JSONObject obj = new JSONObject(CommonUtils.stringReplaceNewLineWithSpace(response.getBody()));
                JSONObject error_response = obj.getJSONObject("error_response");
                log.error("TOP MarketingPromotionsGetResponse call error,error code="
                        + error_response.getInt("code"));
                if (error_response.getInt("code") == 53) {
                    return null;
                    //return -2L;
                    // W2-security
                    /*
                     * //w2-security, should refresh token
                     * log.info("find user with sid="+sid); User user =
                     * User.find("bySessionKey",sid).first();
                     * log.info("refresh user sessionkey with refresh token:"
                     * +user
                     * .getRefreshToken()+",sessionKey:"+user.getSessionKey());
                     * UserDao.refreshToken(user); sid = user.sessionKey;
                     */
                }
                //return -1L;
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void removeAllPromotion(String sid) throws ApiException {

    }

    public static class PromotionAddApi extends
            TBApi<MarketingPromotionAddRequest, MarketingPromotionAddResponse, List<com.taobao.api.domain.Promotion>> {

        public PromotionAddApi(User user, Long activityId, String numiids,
                String discountType, String discountValue, Date startDate,
                Date endDate, String title, Long tagId, String description,
                Long decreaseNumber) {
            super(user.getSessionKey());
            // TODO Auto-generated constructor stub
            this.user = user;
            this.activityId = activityId;
            this.numiids = numiids;
            this.discountType = discountType;
            this.discountValue = discountValue;
            this.startDate = startDate;
            this.endDate = endDate;
            this.title = title;
            this.tagId = tagId;
            this.decreaseNumber = decreaseNumber;
            this.description = description;
        }

        public User user;

        public Long activityId;

        public String numiids;

        public String discountType;

        public String discountValue;

        public Date startDate;

        public Date endDate;

        public String title;

        public Long tagId;

        public Long decreaseNumber;

        public String description;

        public String errorMsg;

        public int code;
        
        private String subErrorCode;

        public String getErrorMsg() {
            return errorMsg;
        }

        public void setErrorMsg(String errorMsg) {
            this.errorMsg = errorMsg;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getSubErrorCode() {
            return subErrorCode;
        }

        @Override
        public MarketingPromotionAddRequest prepareRequest() {
            MarketingPromotionAddRequest req = new MarketingPromotionAddRequest();

            req.setNumIids(numiids);
            req.setDiscountType(discountType);
            req.setDiscountValue(discountValue);
            req.setStartDate(startDate);
            req.setEndDate(endDate);
            req.setPromotionTitle(title);
            req.setTagId(tagId);
            if (decreaseNumber != 0 && decreaseNumber != 1) {
                decreaseNumber = 0L;
            }
            req.setDecreaseNum(decreaseNumber);
            req.setPromotionDesc(description);

            return req;
        }

        @Override
        public List<com.taobao.api.domain.Promotion> validResponse(MarketingPromotionAddResponse resp) {

            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }

            if (!resp.isSuccess()) {
                log.error("resp submsg" + resp.getSubMsg());
                log.error("resp error code " + resp.getErrorCode());
                log.error("resp Mesg " + resp.getMsg());

                int errorCode = Integer.parseInt(resp.getErrorCode());
                String Msg = resp.getMsg();
                String subErrorCode = resp.getSubCode();
                String subMsg = resp.getSubMsg();

                this.subErrorCode = resp.getSubCode();
                errorMsg = resp.getSubMsg();
                code = Integer.parseInt(resp.getErrorCode());

                // 空subMsg
                if (subMsg == null || subMsg.trim().equals("")) {
                    subMsg = resp.getErrorCode() + "," + Msg;
                }

                if (errorCode == 7) {
                    retryTime = 5;

                    log.info("listing set retryTime=5 ...");

                    return null;
                }

                if (StringUtils.equals(subErrorCode, "isp.null-pointer-exception")) {
                    retryTime = 5;

                    log.info("listing set retryTime=5 ...");

                    return null;
                }

                return null;
            }
            else {
                List<com.taobao.api.domain.Promotion> promotionList = resp.getPromotions();

                return promotionList;
            }

        }

        @Override
        public List<com.taobao.api.domain.Promotion> applyResult(List<com.taobao.api.domain.Promotion> res) {
            return res;
        }

    }

    public static ItemOpStatus addPromotion(String sid, String numiids,
            String discountType, String discountValue, Date startDate,
            Date endDate, String title, Long tagId, String description,
            Long decreaseNumber, Long activityId) throws ApiException {
        if (!checkDiscountTypeParam(discountType)) {
            log.error("Promotion: parameter error,discountType is [%s],",
                    discountType);
            throw new IllegalArgumentException(
                    "discountType is not properly set");
        }

        User user = null;

        user = UserDao.findBySessionKey(sid);

        if (user == null) {
            log.error(String.format("can't find user using sid [%s]", sid));
        }

        PromotionAddApi api = new PromotionAddApi(user, activityId, numiids, discountType, discountValue, startDate,
                endDate, title, tagId, description, decreaseNumber);

        List<com.taobao.api.domain.Promotion> tbaoProtionList = api.call();

        if (!api.isApiSuccess()) {

            log.error("add promotion error!" + numiids);

            if (api.getCode() == 53) {
                return new ItemOpStatus(false, numiids, "W2");
            }

            if (api.getCode() == 7) {

                return new ItemOpStatus(false, numiids, "applimit");
            }

            if (StringUtils.equals(api.getSubErrorCode(), "isp.null-pointer-exception")) {

                return new ItemOpStatus(false, numiids, "淘宝接收时遗漏!");
            }
            return new ItemOpStatus(false, numiids, api.getErrorMsg());
        }

        else {
            log.info("Successful added promotion");
            for (com.taobao.api.domain.Promotion tbaopromotion : tbaoProtionList) {
                new Promotion(tbaopromotion.getPromotionId(), user.id, activityId, decreaseNumber,
                        discountType, discountValue,
                        startDate.getTime(), endDate.getTime(),
                        tbaopromotion.getNumIid(), title, description,
                        tagId, "ACTIVE")
                        .jdbcSave();
                log.info("create promotion successful with p_id="
                        + tbaopromotion.getPromotionId());
            }

            return null;
        }

//        TaobaoClient client = new DefaultTaobaoClient(
//                TMConfigs.App.API_TAOBAO_URL, TMConfigs.App.APP_KEY,
//                TMConfigs.App.APP_SECRET);
//        MarketingPromotionAddRequest req = new MarketingPromotionAddRequest();
//        log.info("Add promotion numIIds is:" + numiids);
//        req.setNumIids(numiids);
//        req.setDiscountType(discountType);
//        req.setDiscountValue(discountValue);
//        req.setStartDate(startDate);
//        req.setEndDate(endDate);
//        req.setPromotionTitle(title);
//        req.setTagId(tagId);
//        req.setDecreaseNum(decreaseNumber);
//        req.setPromotionDesc(description);
//
//        log.info("MarketingPromotionAddRequest with sid=" + sid);
//        MarketingPromotionAddResponse response = client.execute(req, sid);
//        log.info("MarketingPromotionAddResponse:" + response.getBody());
//        try {
//            if (response.isSuccess()) {
//                JSONObject obj = new JSONObject(CommonUtils.stringReplaceNewLineWithSpace(response.getBody()));
//                if (obj != null) {
//                    JSONObject promotions = obj.getJSONObject(
//                            "marketing_promotion_add_response").getJSONObject(
//                            "promotions");
//                    JSONArray promotion_array = promotions
//                            .getJSONArray("promotion");
//                    Long p_id = -1L;
//                    for (int i = 0; i < promotion_array.length(); i++) {
//                        JSONObject promotion_obj = (JSONObject) promotion_array
//                                .get(i);
//
//                        p_id = promotion_obj.getLong("promotion_id");
//                        promotion_obj.getString("item_detail_url");
//                        Long p_num_iid = promotion_obj.getLong("num_iid");
//
//                        new Promotion(p_id, user.id,activityId, decreaseNumber,
//                                discountType, discountValue,
//                                startDate.getTime(), endDate.getTime(),
//                                p_num_iid, title, description,
//                                tagId, "ACTIVE")
//                                .jdbcSave();
//                        log.info("create promotion successful with p_id="
//                                + p_id);
//                    }
//                    return null;
//                }
//            } else {
//                JSONObject obj = new JSONObject(CommonUtils.stringReplaceNewLineWithSpace(response.getBody()));
//                JSONObject error_response = obj.getJSONObject("error_response");
//                log.error("TOP MarketingPromotionAddRequest call error,error code="
//                        + error_response.getInt("code"));
//                if (error_response.getInt("code") == 53) {
//                	return "W2";
//                }
//                if(error_response.getInt("code") == 7){
//                    return "十分抱歉，淘宝服务器忙，请过2分钟重试！";
//                }
//                return error_response.getString("sub_msg");
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return null;
    }

    public static class PromotionUpdateApi extends
            TBApi<MarketingPromotionUpdateRequest, MarketingPromotionUpdateResponse, com.taobao.api.domain.Promotion> {

        public PromotionUpdateApi(User user, Long promotionId,
                String discountType, String discountValue, Date startDate,
                Date endDate, Long tagId, String promotionDescription, String promotionTitle
                , Long decreaseNum, Long activityId) {
            super(user.getSessionKey());
            // TODO Auto-generated constructor stub
            this.user = user;
            this.promotionId = promotionId;
            this.discountType = discountType;
            this.discountValue = discountValue;
            this.startDate = startDate;
            this.endDate = endDate;
            this.title = promotionTitle;
            this.tagId = tagId;
            this.decreaseNumber = decreaseNum;
            this.description = promotionDescription;
        }

        public User user;

        public Long promotionId;

        public String numiids;

        public String discountType;

        public String discountValue;

        public Date startDate;

        public Date endDate;

        public String title;

        public Long tagId;

        public Long decreaseNumber;

        public String description;

        public String errorMsg;

        public int code;

        public String getErrorMsg() {
            return errorMsg;
        }

        public void setErrorMsg(String errorMsg) {
            this.errorMsg = errorMsg;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        @Override
        public MarketingPromotionUpdateRequest prepareRequest() {
            MarketingPromotionUpdateRequest req = new MarketingPromotionUpdateRequest();

            req.setPromotionId(promotionId);
            req.setDiscountType(discountType);
            req.setDiscountValue(discountValue);
            req.setStartDate(startDate);
            req.setEndDate(endDate);
            req.setTagId(tagId);
            if (!StringUtils.isEmpty(description)) {
                req.setPromotionDesc(description);
            }
            if (!StringUtils.isEmpty(title)) {
                req.setPromotionDesc(title);
            }
            req.setDecreaseNum(decreaseNumber);

            return req;
        }

        @Override
        public com.taobao.api.domain.Promotion validResponse(MarketingPromotionUpdateResponse resp) {

            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }

            if (!resp.isSuccess()) {
                log.error("resp submsg" + resp.getSubMsg());
                log.error("resp error code " + resp.getErrorCode());
                log.error("resp Mesg " + resp.getMsg());

                int errorCode = Integer.parseInt(resp.getErrorCode());
                String Msg = resp.getMsg();
                String subErrorCode = resp.getSubCode();
                String subMsg = resp.getSubMsg();

                errorMsg = resp.getSubMsg();
                code = Integer.parseInt(resp.getErrorCode());

                // 空subMsg
                if (subMsg == null || subMsg.trim().equals("")) {
                    subMsg = resp.getErrorCode() + "," + Msg;
                }

                if (errorCode == 7) {
                    retryTime = 5;

                    log.info("listing set retryTime=5 ...");

                    return null;
                }

                if (StringUtils.equals(subErrorCode, "isp.null-pointer-exception")) {
                    retryTime = 5;

                    log.info("listing set retryTime=5 ...");

                    return null;
                }

                return null;
            }
            else {
                com.taobao.api.domain.Promotion promotion = resp.getPromotion();

                return promotion;
            }

        }

        @Override
        public com.taobao.api.domain.Promotion applyResult(com.taobao.api.domain.Promotion res) {
            return res;
        }

    }

    public static ItemOpStatus updatePromotion(String sid, Long promotionId,
            String discountType, String discountValue, Date startDate,
            Date endDate, Long tagId, String promotionDescription, String promotionTitle
            , Long decreaseNum, Long activityId) throws ApiException,
            ParseException {

        User user = null;

        user = UserDao.findBySessionKey(sid);

        if (user == null) {
            log.error(String.format("can't find user using sid [%s]", sid));
        }

        PromotionUpdateApi api = new PromotionUpdateApi(user, promotionId, discountType, discountValue, startDate,
                endDate, tagId, promotionDescription, promotionTitle, decreaseNum, activityId);

        com.taobao.api.domain.Promotion tbaoPromotion = api.call();

        if (!api.isApiSuccess()) {

            Promotion promotion = PromotionAction.findPromotionById(promotionId);
            String numiids = String.valueOf(promotion.getNumIid());
            log.error("add promotion error!" + promotionId);

            if (api.getCode() == 53) {
                return new ItemOpStatus(false, numiids, "W2");
            }

            if (api.getCode() == 7) {

                return new ItemOpStatus(false, numiids, "applimit");
            }

            if (StringUtils.equals(api.getSubErrorCode(), "isp.null-pointer-exception")) {

                return new ItemOpStatus(false, numiids, "淘宝接收时遗漏!");
            }

//            if(api.getCode()==15){
//                
//                return new DelistOpStatus(false,numiids,"HaveDeleteItem");
//            }

            return new ItemOpStatus(false, numiids, api.getErrorMsg());
        }

        else {
            log.info("Successful update promotion");
            Promotion promotion = PromotionAction.findPromotionById(promotionId);
            if (promotion != null) {
                promotion.setDiscountType(discountType);
                promotion.setDiscountValue(discountValue);
                promotion.setStartDate(startDate.getTime());
                promotion.setEndDate(endDate.getTime());
                promotion.setUserTagId(tagId);
                if (!StringUtils.isEmpty(promotionDescription)) {
                    promotion.setPromotionDesc(promotionDescription);
                }
                if (!StringUtils.isEmpty(promotionTitle)) {
                    promotion.setPromotionDesc(promotionTitle);
                }
                if (decreaseNum != 0) {
                    promotion.setDecreaseNum(decreaseNum);
                }
                promotion.setNumIid(tbaoPromotion.getNumIid());

                promotion.jdbcSave();

            }
            return null;
        }

//            try {
//            	if (response.isSuccess()) {
//                JSONObject obj = new JSONObject(response.getBody());
//                if (obj != null) {
//                    JSONObject promotionObj = obj.getJSONObject(
//                            "marketing_promotion_update_response")
//                            .getJSONObject("promotion");
//                    Long promotion_id = promotionObj.getLong("promotion_id");
//                    Long num_iid = promotionObj.getLong("num_iid");
//                    Promotion promotion = PromotionAction.findPromotionById(promotion_id);
//                    if (promotion != null) {
//                        promotion.setDiscountType(discountType);
//                        promotion.setDiscountValue(discountValue);
//                        promotion.setStartDate(startDate.getTime());
//                        promotion.setEndDate(endDate.getTime());
//                        promotion.setUserTagId(tagId);
//                        if(!StringUtils.isEmpty(promotionDescription)){
//                        	promotion.setPromotionDesc(promotionDescription);
//                        }
//                        if(!StringUtils.isEmpty(promotionTitle)){
//                        	promotion.setPromotionDesc(promotionTitle);
//                        }
//                        if(decreaseNum!=0){
//                        	promotion.setDecreaseNum(decreaseNum);
//                        }
//                        promotion.setNumIid(num_iid);
//                        
//                        promotion.jdbcSave();
//                        return null;
//                    }
//                }
//            }else {
//                JSONObject obj = new JSONObject(CommonUtils.stringReplaceNewLineWithSpace(response.getBody()));
//                JSONObject error_response = obj.getJSONObject("error_response");
//                log.error("TOP MarketingPromotionAddRequest call error,error code="
//                        + error_response.getInt("code"));
//                if (error_response.getInt("code") == 53) {
////                    return -2L;
//                	return "W2";
//                    // W2-security
//                }
//                if(error_response.getInt("code") == 7){
//                    return "十分抱歉，淘宝服务器忙，请过2分钟重试!";
//                }
//                return error_response.getString("sub_msg");
////                return -1L;
//            } 
//            	
//            }catch (JSONException e) {
//                e.printStackTrace();
//            }
//        
//        return null;
    }

    public static boolean deletePromotion(String sid, Long promotionId)
            throws ApiException {
        TaobaoClient client = TBApi.genClient();
        MarketingPromotionDeleteRequest req = new MarketingPromotionDeleteRequest();
        req.setPromotionId(promotionId);
        MarketingPromotionDeleteResponse response = client.execute(req, sid);
        //log.info("MarketingPromotionDeleteResponse:" + response.getBody());
        if (response.isSuccess() || "删除优惠失败，根据优惠ID查询不到对应的优惠".equals(response.getSubMsg()) 
                || "Invalid method".equalsIgnoreCase(response.getMsg())) {
            log.info("delete promotion success: " + sid);
            //删除自己数据库
            PromotionAction.deletePromotionById(promotionId);
            log.info("delete datebase promotion success");
            return true;
        }
        return false;
    }

    public static class ItemOpStatus {
        public boolean isSuccess;

        public String numIid;

        public String opMsg;

        public boolean isSuccess() {
            return isSuccess;
        }

        public void setSuccess(boolean isSuccess) {
            this.isSuccess = isSuccess;
        }

        public String getOpMsg() {
            return opMsg;
        }

        public void setOpMsg(String opMsg) {
            this.opMsg = opMsg;
        }

        public String getNumIid() {
            return numIid;
        }

        public void setNumIid(String numIid) {
            this.numIid = numIid;
        }

        public ItemOpStatus(boolean isSuccess, String numIid, String opMsg) {
            this.isSuccess = isSuccess;
            this.numIid = numIid;
            this.opMsg = opMsg;
        }

    }

    public static boolean addUserTag(String sid, String tagName,
            String description) throws ApiException {
        TaobaoClient client = TBApi.genClient();
        System.out.println("app key:" + TMConfigs.App.APP_KEY);
        System.out.println("app secret:" + TMConfigs.App.APP_SECRET);
        MarketingTagAddRequest req = new MarketingTagAddRequest();
        req.setTagName(tagName);
        req.setDescription(description);
        MarketingTagAddResponse response = client.execute(req, sid);
        log.info("MarketingTagAddResponse:" + response.getBody());
        if (response.isSuccess()) {
            // 示例返回:{"marketing_tag_add_response":{"user_tag":{"description":"这是一个标签描述","create_date":"2013-05-28 00:20:10","tag_id":1489015,"tag_name":"生日特价人群"}}}
            try {
                JSONObject obj = new JSONObject(response.getBody());
                if (obj != null) {
                    JSONObject user_tag = obj.getJSONObject(
                            "marketing_tag_add_response").getJSONObject(
                            "user_tag");
                    Long tag_id = user_tag.getLong("tag_id");
                    String create_date_str = user_tag.getString("create_date");
                    Date create_date = CommonUtils.String2Date(create_date_str);
                    create_date.getTime();
                    UserTag userTag = new UserTag(tag_id,
                            create_date.getTime(), tagName, description);
                    userTag.save();
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public static boolean deleteUserTag(String sid, Long tagId)
            throws ApiException {
        TaobaoClient client = TBApi.genClient();
        MarketingTagDeleteRequest req = new MarketingTagDeleteRequest();
        req.setTagId(tagId);
        MarketingTagDeleteResponse response = client.execute(req, sid);
        System.out.println("result:" + response.getBody());
        if (response.isSuccess()) {
            UserTag.<UserTag> findById(tagId).delete();
            return true;
        }
        return false;
    }

    public static boolean updateUserTag(String sid, Long id, String tagName,
            String description) throws ApiException {
        // try delete and update
        boolean updateResult = false;
        boolean delResult = PromotionAction.deleteUserTag(sid, id);
        if (delResult) {
            updateResult = PromotionAction
                    .addUserTag(sid, tagName, description);
        }
        return updateResult;
    }

    public static boolean tagAddUser(String sid, Long tagId, String userNick)
            throws ApiException {
        TaobaoClient client = TBApi.genClient();
        MarketingTaguserAddRequest req = new MarketingTaguserAddRequest();
        req.setTagId(tagId);
        req.setNick(userNick);
        MarketingTaguserAddResponse response = client.execute(req, sid);
        log.info("MarketingTaguserAddResponse:" + response.getBody());
        if (response.isSuccess()) {
            UserTag userTag = UserTag.<UserTag> findById(tagId);
            userTag.addUser(userNick);
            userTag.save();
        }
        return true;
    }

    public static boolean tagDeleteUser(String sid, Long tagId, String userNick)
            throws ApiException {
        TaobaoClient client = TBApi.genClient();
        MarketingTaguserDeleteRequest req = new MarketingTaguserDeleteRequest();
        req.setTagId(tagId);
        req.setNick(userNick);
        MarketingTaguserDeleteResponse response = client.execute(req, sid);
        log.info("MarketingTaguserDeleteResponse:" + response.getBody());
        if (response.isSuccess()) {
            UserTag userTag = UserTag.<UserTag> findById(tagId);
            userTag.deleteUser(userNick);
            userTag.save();
        }
        return false;
    }

    public static UserTag[] getUserTags(String sid, String fieldList)
            throws ApiException {
        List<UserTag> userTags = new ArrayList<UserTag>();
        TaobaoClient client = TBApi.genClient();
        MarketingTagsGetRequest req = new MarketingTagsGetRequest();
        req.setFields("tag_id,tag_name,create_date,description");
        MarketingTagsGetResponse response = client.execute(req, sid);
        log.info("MarketingTagsGetResponse:" + response.getBody());
        if (response.isSuccess()) {
            JSONObject obj;
            try {
                obj = new JSONObject(response.getBody());
                if (obj != null) {
                    JSONArray user_tags = obj.getJSONObject(
                            "marketing_tags_get_response").getJSONArray(
                            "user_tags");
                    for (int i = 0; i < user_tags.length(); ++i) {
                        JSONObject item = (JSONObject) user_tags.get(i);
                        Long tagId = item.getLong("tag_id");
                        String tagName = item.getString("tag_name");
                        Date createDate = CommonUtils.String2Date(item
                                .getString("create_date"));
                        String desp = item.getString("description");
                        UserTag userTag = new UserTag(tagId,
                                createDate.getTime(), tagName, desp);
                        userTags.add(userTag);
                    }
                    return (UserTag[]) userTags.toArray();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    // Internal actions
    public static ItemOpStatus addActivity(Long userId, String title,
            String description, Long createTime, Long startTime, Long endTime,
            String promotionType, String discountValue, Long decreaseNum,
            Long userTagId, String items, Long activityId) {
        log.info("start addActivity......");
        User user = User.findByUserId(userId);
        log.warn(String
                .format("try to add items [%s] to promotion type [%s] with discount value [%s] or decrease number [%s]",
                        items, promotionType, discountValue, decreaseNum));
        try {

            ItemOpStatus opStatus = addPromotion(user.sessionKey, items,
                    promotionType, discountValue, new Date(startTime),
                    new Date(endTime), title, userTagId, description,
                    decreaseNum, activityId);

            return opStatus;

        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ApiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;

    }

    public static Long addUserTag(Long userId, String tagName,
            String description) {
        Long currentTime = CommonUtils.Date2long(new Date());
        UserTag userTag = new UserTag(userId, currentTime, tagName, description);
        userTag.save();
        return userTag.getId();
    }

    public static void addPromotionToActivity(Long id, Promotion promotion) {
        /*
         * Activity activity = Activity.findById(id);
         * activity.addPromotion(promotion); promotion.save();
         */
    }

    public static void removePromotionFromActivity(Long id, Promotion promotion) {
        /*
         * Activity activity = Activity.findById(id);
         * activity.removePromotion(promotion); activity.save();
         */
    }

    public static void updatePromotionForActiivty(Long id, Promotion promotion) {

    }

    public static void saveVipConfig(Long userId, String type, Long[] quantity,
            Long[] discount) {
        VipConfig vipConfig = VipConfig.find("byUserId", userId).first();
        if (vipConfig == null) {
            vipConfig = new VipConfig(userId, type, quantity, discount);
        } else {
            vipConfig.setType(type);
            vipConfig.setQuantity(quantity);
            vipConfig.setDiscount(discount);
        }
        vipConfig.save();
    }

    public static void editActivity(Long activityId, Long userId, String title,
            String description, Long createTime, Long startTime, Long endTime,
            String activityType, String discountValue, String decreaseNum,
            Long userTagId) {
        //Activity activity = Activity.findById(activityId);
        TMProActivity activity = TMProActivity.findByActivityId(userId, activityId);
        activity.setActivityTitle(title);
        activity.setActivityDescription(description);
        activity.setActivityStartTime(startTime);
        activity.setActivityEndTime(endTime);
//        activity.setPromotionType(activityType);
//        activity.setDiscountValue(discountValue);
//        activity.setDecreaseNum(decreaseNum);
        activity.jdbcSave();
    }

    public static Promotion selectBynumiid(long numiid) {

        String sql = Promotion_SQL + " where numIid = ? and status = ? ";

        String status = "ACTIVE";
        //最多只能找到一个
        Promotion promotion = new JDBCExecutor<Promotion>(sql, numiid, status
                ) {
                    @Override
                    public Promotion doWithResultSet(ResultSet rs) throws SQLException {
                        Promotion list = null;
                        if (rs.next()) {
                            Promotion promotionList = parsePromotionList(rs);
                            if (promotionList != null)
                                list = promotionList;
                        }
                        return list;
                    }
                }.call();

        return promotion;
    }

    public static long countBynumiid(long numiid) {
        String countSql = Count_SQL + " where numIid = ? and status = ? ";

        String status = "ACTIVE";

        long count = JDBCBuilder.singleLongQuery(countSql, numiid, status);

        return count;
    }
    
    public static Set<Long> findPromotionedNumIids(Long userId, Set<Long> numIidSet) {
        
        if (CommonUtils.isEmpty(numIidSet)) {
            return new HashSet<Long>();
        }
        
        String numIids = StringUtils.join(numIidSet, ",");
        
        numIids = CommonUtils.escapeSQL(numIids);
        
        String query = " select numIid from " + Promotion.TABLE_NAME + " where userId = ? " +
        		" and numIid in (" + numIids + ") and status = ? ";
        

        final String status = "ACTIVE";

        return new JDBCBuilder.JDBCLongSetExecutor(query, userId, status).call();

    }

    public static Promotion selectById_numiid(Long id, long numiid) {

        String sql = Promotion_SQL + " where numIid = ? and id = ? ";

        //最多只能找到一个
        Promotion promotion = new JDBCExecutor<Promotion>(sql, numiid, id
                ) {
                    @Override
                    public Promotion doWithResultSet(ResultSet rs) throws SQLException {
                        Promotion list = new Promotion();
                        if (rs.next()) {
                            Promotion promotionList = parsePromotionList(rs);
                            if (promotionList != null)
                                list = promotionList;
                        }
                        return list;
                    }
                }.call();

        return promotion;
    }

    public static long countById_numiid(Long id, long numiid) {
        String countSql = Count_SQL + " where numIid = ? and id = ? ";

        long count = JDBCBuilder.singleLongQuery(countSql, numiid, id);

        return count;
    }

    public static Promotion findPromotionById(Long promotionId) {
        String sql = Promotion_SQL + " where id = ? ";

        Promotion promotion = new JDBCExecutor<Promotion>(sql, promotionId
                ) {
                    @Override
                    public Promotion doWithResultSet(ResultSet rs) throws SQLException {
                        Promotion list = new Promotion();
                        if (rs.next()) {
                            Promotion promotionList = parsePromotionList(rs);
                            if (promotionList != null)
                                list = promotionList;
                        }
                        return list;
                    }
                }.call();

        return promotion;
    }

    public static Promotion parsePromotionList(ResultSet rs) {
        try {
            Long promotionId = rs.getLong(1);
            Long userId = rs.getLong(2);
            Long activityId = rs.getLong(3);
            Long decreaseNum = rs.getLong(4);
            String discountType = rs.getString(5);
            String discountValue = rs.getString(6);
            Long startDate = rs.getLong(7);
            Long endDate = rs.getLong(8);
            Long numIid = rs.getLong(9);
            String promotionDesc = rs.getString(10);
            String promotionTitle = rs.getString(11);
            Long userTagId = rs.getLong(12);
            String status = rs.getString(13);

            Promotion promotion = new Promotion(promotionId, userId, activityId, decreaseNum, discountType,
                    discountValue, startDate, endDate, numIid, promotionTitle, promotionDesc, userTagId, status);

            return promotion;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }


    public static final String Delete_Promotion_SQL = " delete from " + Promotion.TABLE_NAME;

    public static void deletePromotionById(Long id) {

        String sql = Delete_Promotion_SQL + " where id = ? ";

        JDBCBuilder.update(false, sql, id);
    }

    public static Promotion findPromotionByNumIid(Long userId, Long numIid) {
        String sql = Promotion_SQL + " where userId = ? and numIid = ? ";

        return new JDBCExecutor<Promotion>(sql, userId, numIid) {
            @Override
            public Promotion doWithResultSet(ResultSet rs) throws SQLException {
                Promotion promotion = null;
                if (rs.next()) {
                    Promotion promotionList = parsePromotionList(rs);
                    if (promotionList != null)
                        promotion = promotionList;
                }
                return promotion;
            }
        }.call();
    }

    public static List<Promotion> findPromotionByNumIids(Long userId, String numIids, PageOffset po) {

        if (StringUtils.isEmpty(numIids)) {
            log.error("empty numIids");
            return ListUtils.EMPTY_LIST;
        }

        String sql = Promotion_SQL + " where userId = ? and numIid in (" + numIids + ")" + " limit ? , ? ";

        return new JDBCExecutor<List<Promotion>>(sql, userId, po.getOffset(), po.getPs()) {
            @Override
            public List<Promotion> doWithResultSet(ResultSet rs) throws SQLException {
                List<Promotion> list = new ArrayList<Promotion>();
                while (rs.next()) {
                    Promotion promotionList = parsePromotionList(rs);
                    if (promotionList != null)
                        list.add(promotionList);
                }
                return list;
            }
        }.call();
    }

    public static long countPromotionList(Long userId, String numIids) {
        String countSql = Count_SQL + " where userId = ? and numIid in (" + numIids + ")";
        long count = JDBCBuilder.singleLongQuery(countSql, userId);
        return count;

    }

    public static List<Promotion> findPromotionAllByUserId(Long userId) {

        String sql = Promotion_SQL + " where userId = ? ";

        return new JDBCExecutor<List<Promotion>>(sql, userId) {
            @Override
            public List<Promotion> doWithResultSet(ResultSet rs) throws SQLException {
                List<Promotion> list = new ArrayList<Promotion>();
                while (rs.next()) {
                    Promotion promotionList = parsePromotionList(rs);
                    if (promotionList != null)
                        list.add(promotionList);
                }
                return list;
            }
        }.call();
    }

    public static List<Promotion> findPromotionByActivityId(Long activityId) {

        String sql = Promotion_SQL + " where activityId = ? ";

        return new JDBCExecutor<List<Promotion>>(sql, activityId) {
            @Override
            public List<Promotion> doWithResultSet(ResultSet rs) throws SQLException {
                List<Promotion> list = new ArrayList<Promotion>();
                while (rs.next()) {
                    Promotion promotionList = parsePromotionList(rs);
                    if (promotionList != null)
                        list.add(promotionList);
                }
                return list;
            }
        }.call();
    }

    public static List<Promotion> findPromotionByActivityId(Long activityId, PageOffset po) {

        String sql = Promotion_SQL + " where activityId = ? limit ?, ? ";

        return new JDBCExecutor<List<Promotion>>(sql, activityId, po.getOffset(), po.getPs()) {
            @Override
            public List<Promotion> doWithResultSet(ResultSet rs) throws SQLException {
                List<Promotion> list = new ArrayList<Promotion>();
                while (rs.next()) {
                    Promotion promotionList = parsePromotionList(rs);
                    if (promotionList != null)
                        list.add(promotionList);
                }
                return list;
            }
        }.call();
    }

    public static long countPromotionByActivityId(Long userId, Long activityId) {
        String countSql = Count_SQL + " where userId = ? and activityId = ? ";
        long count = JDBCBuilder.singleLongQuery(countSql, userId, activityId);
        return count;

    }

    public static Promotion findPromotionByActivityIdLimit1(Long activityId) {

        String sql = Promotion_SQL + " where activityId = ? order by decreaseNum desc limit 1 ";

        return new JDBCExecutor<Promotion>(sql, activityId) {
            @Override
            public Promotion doWithResultSet(ResultSet rs) throws SQLException {
                Promotion list = null;
                if (rs.next()) {
                    list = parsePromotionList(rs);
                }
                return list;
            }
        }.call();
    }

    public static List<Promotion> findPromotionByCondition(Long userId, Long activityId, String title, String cid,
            String sellerCid, PageOffset po) {

        String sql = Promotion_SQL
                + " where activityId = ? and numIid in ( select numIid from item%s where userId = ? ";

        sql = checkItemSQL(sql, userId, title, cid, sellerCid);

        sql += " ) limit ? , ? ";

        return new JDBCExecutor<List<Promotion>>(sql, activityId, userId, po.getOffset(), po.getPs()) {
            @Override
            public List<Promotion> doWithResultSet(ResultSet rs) throws SQLException {
                List<Promotion> list = new ArrayList<Promotion>();
                while (rs.next()) {
                    Promotion promotionList = parsePromotionList(rs);
                    if (promotionList != null)
                        list.add(promotionList);
                }
                return list;
            }
        }.call();
    }

    public static long countPromotionByCondition(Long userId, Long activityId, String title, String cid,
            String sellerCid) {

        String sql = Count_SQL + " where activityId = ? and numIid in ( select numIid from item%s where userId = ? ";

        sql = checkItemSQL(sql, userId, title, cid, sellerCid);

        sql += " )";

        long count = JDBCBuilder.singleLongQuery(sql, activityId, userId);
        return count;

    }

    public static String checkItemSQL(String sql, Long userId, String title, String cid, String sellerCid) {
        sql = ItemDao.genShardQuery(sql, userId);
        log.info("[sql]" + sql);

        if (!StringUtils.isEmpty(title)) {
            String like = appendKeywordsLike(title);
            sql += " and " + like;
        }
        if (!StringUtils.isEmpty(cid)) {
            String like = appendItemCidsLike(cid);
            sql += " and " + like;
        }
        if (!StringUtils.isEmpty(sellerCid)) {
            String like = appendSellerCidsLike(sellerCid);
            sql += " and " + like;
        }

        return sql;
    }

    static String appendSellerCidsLike(String catId) {

        StringBuilder sb = new StringBuilder("(( 0 = 1)");
        String[] keys = catId.split("\\s");

        for (String split : keys) {
            if (StringUtils.isBlank(split)) {
                continue;
            }

            sb.append(" or (sellerCids like '%");
            sb.append(CommonUtils.escapeSQL(split));
            sb.append("%')");
        }
        sb.append(")");

        return sb.toString();
    }

    static String appendKeywordsLike(String Keywords) {

        StringBuilder sb = new StringBuilder("(( 0 = 1)");
        String[] keys = Keywords.split("\\s");

        for (String split : keys) {
            if (StringUtils.isBlank(split)) {
                continue;
            }

            sb.append(" or (title like '%");
            sb.append(CommonUtils.escapeSQL(split));
            sb.append("%')");
        }
        sb.append(")");

        return sb.toString();
    }

    static String appendItemCidsLike(String catId) {

        StringBuilder sb = new StringBuilder("(( 0 = 1)");
        String[] keys = catId.split("\\s");

        for (String split : keys) {
            if (StringUtils.isBlank(split)) {
                continue;
            }

            sb.append(" or (cid like '%");
            sb.append(CommonUtils.escapeSQL(split));
            sb.append("%')");
        }
        sb.append(")");

        return sb.toString();
    }

}
