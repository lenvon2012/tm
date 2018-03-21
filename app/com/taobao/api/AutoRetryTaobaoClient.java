
package com.taobao.api;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 调用出错自动重试客户端。
 * 
 * @author fengsheng
 * @since 1.0, Sep 4, 2012
 */
public class AutoRetryTaobaoClient extends DefaultTaobaoClient {

    private static final Log log = LogFactory.getLog(AutoRetryTaobaoClient.class);

    private static final ApiException RETRY_FAIL = new ApiException("sdk.retry-call-fail", "API调用重试失败");

    private static final ThreadLocal<Long> RETRY_COUNTER = new ThreadLocal<Long>();

    /**
     * 单次请求的最大重试次数，默认值为3次。
     */
    private int maxRetryCount = 3;

    /**
     * 重试之前休眠时间，默认值为100毫秒。
     */
    private long retryWaitTime = 100L;

    /**
     * 超过最大重试次数时是否抛出异常。
     */
    private boolean throwIfOverMaxRetry = false;

    public AutoRetryTaobaoClient(String serverUrl, String appKey, String appSecret) {
        super(serverUrl, appKey, appSecret);
    }

    public AutoRetryTaobaoClient(String serverUrl, String appKey, String appSecret, String format) {
        super(serverUrl, appKey, appSecret, format);
    }

    public AutoRetryTaobaoClient(String serverUrl, String appKey, String appSecret, String format, int connectTimeout,
            int readTimeout) {
        super(serverUrl, appKey, appSecret, format, connectTimeout, readTimeout);
    }

    public AutoRetryTaobaoClient(String serverUrl, String appKey, String appSecret, String format, int connectTimeout,
            int readTimeout, String signMethod) {
        super(serverUrl, appKey, appSecret, format, connectTimeout, readTimeout, signMethod);
    }

    public <T extends TaobaoResponse> T execute(TaobaoRequest<T> request) throws ApiException {
        return this.execute(request, null);
    }

    public <T extends TaobaoResponse> T execute(TaobaoRequest<T> request, String session) throws ApiException {
        T rsp = null;
        try {
            RETRY_COUNTER.set(getCurrentRetryCount() + 1);
            rsp = super.execute(request, session);
            request.getTextParams();
            if (!rsp.isSuccess()) {
                if (getCurrentRetryCount() < maxRetryCount) {
                    if (rsp.getSubCode() != null && rsp.getSubCode().startsWith("isp.")) {
                        sleepWithoutInterrupt(retryWaitTime);
                        log.warn(buildRetryLog(request.getApiMethodName(), request.getTextParams()));
                        return execute(request, session);
                    }
                } else {
                    if (throwIfOverMaxRetry) {
                        throw RETRY_FAIL;
                    }
                }
            }
        } catch (ApiException e) {
            if (e != RETRY_FAIL && getCurrentRetryCount() < maxRetryCount) {
                sleepWithoutInterrupt(retryWaitTime);
                log.warn(buildRetryLog(request.getApiMethodName(), request.getTextParams()));
                return execute(request, session);
            } else {
                throw e;
            }
        } finally {
            RETRY_COUNTER.set(null);
        }
        return rsp;
    }

    private String buildRetryLog(String apiName, Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        sb.append(apiName).append(" retry call ").append(getCurrentRetryCount() + 1);
        params.remove("fields");
        sb.append(" times, params=").append(params);
        return sb.toString();
    }

    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public void setRetryWaitTime(long retryWaitTime) {
        this.retryWaitTime = retryWaitTime;
    }

    public void setThrowIfOverMaxRetry(boolean throwIfOverMaxRetry) {
        this.throwIfOverMaxRetry = throwIfOverMaxRetry;
    }

    private Long getCurrentRetryCount() {
        if (RETRY_COUNTER.get() == null) {
            return -1L;
        } else {
            return RETRY_COUNTER.get();
        }
    }

    private void sleepWithoutInterrupt(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
