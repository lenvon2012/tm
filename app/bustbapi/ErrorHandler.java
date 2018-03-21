
package bustbapi;

import models.showwindow.ShowwindowTmallTotalNumFixedNum;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import result.PolicyResult;
import result.TMResult;
import bustbapi.UserAPIs.UserGetApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.ReturnCode;
import com.ciaosir.client.utils.NumberUtil;
import com.google.gson.Gson;
import com.taobao.api.ApiException;
import com.taobao.api.TaobaoClient;
import com.taobao.api.TaobaoResponse;
import com.taobao.api.domain.Task;
import com.taobao.api.request.TopatsResultGetRequest;
import com.taobao.api.response.TopatsResultGetResponse;
import com.taobao.api.response.TopatsSimbaCampkeywordbaseGetResponse;
import com.taobao.api.response.TopatsSimbaCampkeywordeffectGetResponse;
import com.taobao.api.response.TopatsTradesSoldGetResponse;
import com.taobao.api.response.TopatsVisitlogGetResponse;

import dao.UserDao;

public class ErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);

    public static final String TAG = "ErrorHandler";

//    static Gson gson = new Gson();

    public static void validTaoBaoResp(TaobaoResponse resp) {
        validTaoBaoResp(null, resp);
    }

    public static void validTaoBaoResp(TBApi api, TaobaoResponse resp) {
        if (resp == null) {
            log.error("No Response ???:" + resp);
            return;
        }

        if (resp.isSuccess()) {
            return;
        }

        Gson gson = new Gson();
        log.error("Error:" + gson.toJson(resp));

        log.error("Error Resp ErrorCode Msg :" + resp.getErrorCode());
        log.error("Error Resp Msg :" + resp.getMsg());
        log.error("Error Resp SubCode Msg :" + resp.getSubCode());
        log.error("Error Resp SubMsg :" + resp.getSubMsg());

        if (api != null) {
            api.setSubErrorCode(resp.getSubCode());
            api.setSubErrorMsg(resp.getSubMsg());
        }

        String errorCode = resp.getErrorCode();
        if ("7".equals(errorCode)) {
            /**
             * api调用频率超。。。
             */
            if (resp.getSubMsg().equals("This ban will last for 1 more seconds")) {
                if (api != null) {
                    api.setRetryTime(5);
                }
                
                CommonUtils.sleepQuietly(500L);
                return;
            }
            throw new PolicyResult(ReturnCode.API_CALL_LIMIT, StringUtils.EMPTY);
        } else if ("15".equals(errorCode)) {
            if ((resp.getSubMsg() != null)
                    && (resp.getSubMsg().contains("账户memeberid不能为空") || resp.getSubMsg().contains("直通车主账号不存在"))) {
                return;
            }

            // throw new PolicyResult(ReturnCode.INNER_ERROR,
            // StringUtils.EMPTY);
        } else if ("40".equals(errorCode)) {
            // throw new PolicyResult(ReturnCode.INNER_ERROR,
            // StringUtils.EMPTY);
        } else if ("27".equals(errorCode)) {
//            throw new PolicyResult(ReturnCode.INVALID_SESSION, StringUtils.EMPTY);
        } else if ("530".equals(errorCode)) {
//            throw new PolicyResult(ItemApi.ItemGet.ITEM_GET_530, resp.getSubMsg());
        }

        validATSDumpResponse(resp);
        return;

    }

    public static class CommonTaobaoHandler {
        private static ErrorHandler.CommonTaobaoHandler _instance = new CommonTaobaoHandler();;

        public static CommonTaobaoHandler getInstance() {
            return _instance;
        }

        public void validResp(TaobaoResponse resp) {
            ErrorHandler.validTaoBaoResp(resp);
        }

        public String getErrorMsg(TaobaoResponse resp) {
            String errorMsg = resp.getSubMsg();

            if (errorMsg == null || StringUtils.isBlank(errorMsg)) {
                if ("40".equals(resp.getErrorCode()) || "530".equals(resp.getErrorCode())) {
                    errorMsg = "亲，淘宝后台正忙，请您过10分钟刷新页面再试试哦！非常感谢 o(∩∩)o";
                } else {
                    errorMsg = "亲，出了点小意外，请联系客服哦！非常感谢 o(∩∩)o";
                }
            }

            return errorMsg;
        }
    }

    public static boolean validResponseBoolean(TaobaoResponse resp) {
        if (resp == null) {
            log.error("No Response ???:" + resp);
            return false;
        }

        if (resp.isSuccess()) {
            return true;
        }

//        log.error("Error:" + gson.toJson(resp));
        log.error("Error Resp ErrorCode Msg :" + resp.getErrorCode());
        log.error("Error Resp Msg :" + resp.getMsg());
        log.error("Error Resp SubMsg :" + resp.getSubMsg());
        log.error("Error Resp SubCode :" + resp.getSubCode());
        return false;
    }

    public static boolean isRecommendMaxReached(User user, TBApi api) {
        if (api == null) {
            return false;
        }
        return isRecommendMaxReached(user, api.getSubErrorCode(), api.getSubErrorMsg());
    }

    public static boolean isRecommendMaxReached(User user, TMResult res) {
        if (res == null) {
            return false;
        }
        return isRecommendMaxReached(user, res.getCode(), res.getMsg());
    }

    public static boolean isRecommendMaxReached(User user, String subErrorCode, String subErrorMsg) {

        if (user == null) {
            return false;
        }

        if ("isv.item-recommend-service-error:ERROR_MORE_THAN_ALLOWED_RECOMMEND_NUM".equals(subErrorCode)) {
            return true;
        }
        if ("isv.item-recommend-service-error:isv.item-recommend-service-over-limit-tmall"
                .equals(subErrorCode)) {
            /**
             * 您最大的橱窗推荐数为: 100, 已橱窗推荐的商品数为: 100, 所以不能再把商品设为推荐橱窗状态了 
             */
            resetMaxWindowNum(user, subErrorMsg);
            return true;
        }
        return false;
        //isv.item-recommend-service-error:isv.item-recommend-service-over-limit-tmall
    }

    public static void resetMaxWindowNum(User user, String errorMsg) {
        if (StringUtils.isEmpty(errorMsg)) {
            return;
        }
        String target = "您最大的橱窗推荐数为: ";
        int maxWindowNumStartIndex = errorMsg.indexOf(target);
        if (maxWindowNumStartIndex < 0) {
            return;
        }
        int maxWindowNumEndIndex = errorMsg.indexOf(",", maxWindowNumStartIndex);
        if (maxWindowNumEndIndex <= 0) {
            return;
        }

        String str = errorMsg.substring(maxWindowNumStartIndex + target.length(), maxWindowNumEndIndex);
        if (!StringUtils.isNumeric(str)) {
            return;
        }

        int maxNum = NumberUtil.parserInt(str, -1);
        OperateItemApi.setUserTotalNum(user, maxNum);
        ShowwindowTmallTotalNumFixedNum.updateUserTotalNum(user.getId(), maxNum);
    }

    /**
     * Never call in the user get api..
     * @param userId
     * @param sid
     * @param subCode
     * @return
     */
    public static boolean fuckWithTheErrorCode(Long userId, String sid, String subCode) {
        if (userId == null) {
            return false;
        }

        log.info("[sub code :]" + subCode);
        if ("session-expired".equals(subCode) || "session-not-exist".equals(subCode)
                || "isv.shop-not-exist:invalid-shop".equals(subCode) || "invalid-sessionkey".equals(subCode)) {
            if (new UserGetApi(sid, null).call() == null) {
                UserDao.updateVaild(userId, false);
            }
            return false;
        }

        return true;
    }

    /**
     * TODO This ban will last for 18 more seconds
     * @param subMsg
     * @return
     */
    public static int extractBanSeconds(String subMsg) {
    	if(StringUtils.isEmpty(subMsg)) {
    		return -1;
    	}
        int banStart = subMsg.indexOf("This ban will last for");
        if (banStart < 0) {
            return -1;
        }
        banStart += "This ban will last for".length();
        int banEnd = subMsg.indexOf("more seconds");
        if (banEnd < 0) {
            return -1;
        }

        int seconds = NumberUtil.parserInt(subMsg.substring(banStart, banEnd).trim(), -1);
        return seconds;
    }

    public static void validATSDumpResponse(TaobaoResponse resp) {
        try {

            if ("isv.task-duplicate".equals(resp.getSubCode())) {
                // *****：TaskId=11642745
                String subMsg = resp.getSubMsg();
                int index = subMsg.indexOf("TaskId=");
                Long taskId = Long.valueOf(subMsg.substring(index + "TaskId=".length()));
                log.error("refix for the task id :" + taskId);
//                log.info("[we found dump task:]"+taskId);

                if (taskId == null) {
                    return;
                }

                Task task = getTaskResult(taskId);
                log.info("[fix task:]" + new Gson().toJson(task));
                if (task == null) {
                    return;
                }

                if (resp instanceof TopatsResultGetResponse) {
                    ((TopatsResultGetResponse) resp).setTask(task);
                } else if (resp instanceof TopatsSimbaCampkeywordbaseGetResponse) {
                    ((TopatsSimbaCampkeywordbaseGetResponse) resp).setTask(task);
                } else if (resp instanceof TopatsSimbaCampkeywordeffectGetResponse) {
                    ((TopatsSimbaCampkeywordeffectGetResponse) resp).setTask(task);
                } else if (resp instanceof TopatsTradesSoldGetResponse) {
                    ((TopatsTradesSoldGetResponse) resp).setTask(task);
                } else if (resp instanceof TopatsVisitlogGetResponse) {
                    ((TopatsVisitlogGetResponse) resp).setTask(task);
                }

                resp.setSubCode(null);
                resp.setErrorCode(null);
                log.info("[fixed resp;]" + new Gson().toJson(resp));
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }

        return;
    }

    public static Task getTaskResult(final Long taskId) {
        Task task = null;
        if (taskId == null) {
            log.error("taskId is NULL");
            return null;
        }

        TaobaoClient client = TBApi.genClient();
        TopatsResultGetRequest req = new TopatsResultGetRequest();

        req.setTaskId(taskId);
        try {
            TopatsResultGetResponse response = client.execute(req);
            if (response == null) {
                log.warn("Null Response");
                return null;
            }

            if (!response.isSuccess()) {
                return null;
            }

            task = response.getTask();
            if (task == null) {
                log.warn("Null task returned ....");
                return null;
            }
            // TODO makes it debug...

        } catch (ApiException e) {
            log.warn(e.getMessage(), e);
        }

        return task;
    }

}
