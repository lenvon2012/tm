
package jdapi;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bustbapi.ApiUtil;

import com.ciaosir.client.CommonUtils;
import com.jd.open.api.sdk.DefaultJdClient;
import com.jd.open.api.sdk.JdClient;
import com.jd.open.api.sdk.request.JdRequest;
import com.jd.open.api.sdk.response.AbstractResponse;

import configs.TMConfigs;
import configs.TMConfigs.App;

public abstract class JDApi<K extends JdRequest<V>, V extends AbstractResponse, W> implements Callable<W> {

    private static final Logger log = LoggerFactory.getLogger(JDApi.class);

    protected String TAG = "JDApi";

    protected int retryTime = 1;

    protected String sid;

    protected String appKey = TMConfigs.App.JD_APP_KEY;
    protected String appSecret = TMConfigs.App.JD_APP_SECRET;
    
//    protected String appKey = JDAction.jdApiConfig.getAppKey();
//
//    protected String appSecret = JDAction.jdApiConfig.getAppSecret();

    protected long sleepInterval = 1000L;

    protected boolean isSuccess = true;

    protected int iteratorTime = 1;

    protected String subErrorCode = null;

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

    public JDApi(String sid) {
        this.sid = sid;
    }

    public abstract K prepareRequest();

    public abstract W validResponse(V resp);

    public abstract W applyResult(W res);

    @Override
    public W call() {

//        log.info(TAG + " class:" + this.getClass());
//        Thread.currentThread().setName(TAG);

        // if (ApiUtil.isApiCallLimited()) {
        // return null;
        // }

        return callApi();

    }

    protected W callApi() {

        W res = null;
        JdClient client = new DefaultJdClient(App.API_JD_URL, sid, appKey, appSecret);
        while (iteratorTime-- > 0) {

            V resp = null;
            K req = null;

            req = prepareRequest();
            if (req == null) {
                return res;
            }

            boolean doneForThisTime = false;
            int count = 0;

            while (count++ < retryTime && !doneForThisTime) {

                if (count > 1) {
                    log.warn("[Current Retry Time]" + count + "  for class:" + this.getClass());
                    CommonUtils.sleepQuietly(sleepInterval);
                }

                try {
                    resp = client.execute(req);

                    ApiUtil.apiCount++;
                    
//                    log.warn(resp.getMsg());

                    res = validResponse(resp);
                    // log.info("return value for [" + res + "] for Class " +
                    // this.getClass());

                    /**
                     * no results got and exception, continue retry
                     */
                    if (res == null) {
                        log.warn("No Validate Res , retry:" + this.getClass());
                        continue;
                    }

                    res = applyResult(res);
                    doneForThisTime = true;
                    // log.error("This is Done");
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
                this.isSuccess = false;
                return null;
            }
        }

        return res;
    }

    public boolean isSuccess() {
        return this.isSuccess;
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

    public static JdClient genXXClient() {
        return new DefaultJdClient(App.API_JD_URL, "", "", "");
    }

    protected String errorMsg;

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

}
