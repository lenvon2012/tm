/**
 * 
 */
package ppapi;

import java.util.HashMap;
import java.util.concurrent.Callable;

import models.paipai.PaiPaiUser;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;
import com.paipai.api.OpenApiException;
import com.paipai.api.PaiPaiOpenApiOauth;

import controllers.PaiPaiAPIConfig;

/**
 * @author navins
 * @date 2013-7-8 下午3:42:30
 */
public abstract class PaiPaiApi<W> implements Callable<W> {

    private static final Logger log = LoggerFactory.getLogger(PaiPaiApi.class);

    String appOAuthID = PaiPaiAPIConfig.get().getAppOAuthID();
    String appOAuthkey = PaiPaiAPIConfig.get().getAppOAuthkey();

    String accessToken = "";
    long uin = 1;

    String method = "get";

    protected int iteratorTime = 1;

    protected int retryTime = 3;

    protected long sleepInterval = 1000L;

    public PaiPaiApi(PaiPaiUser user) {
        this.accessToken = user.getAccessToken();
        this.uin = user.getId();
    }

    public PaiPaiApi(PaiPaiUser user, String method) {
        this.accessToken = user.getAccessToken();
        this.uin = user.getId();
        this.method = method;
    }

    @Override
    public W call() {
        W res = null;

        PaiPaiOpenApiOauth sdk = new PaiPaiOpenApiOauth(appOAuthID, appOAuthkey, accessToken, uin);

        while (iteratorTime-- > 0) {
            sdk.setMethod(method);
            sdk.setCharset("gbk");
            sdk.setFormat("json");

            HashMap<String, Object> params = sdk.getParams(getApiPath());
            params.put("sellerUin", "" + uin);
            boolean flag = prepareRequest(params);
            if (flag == true) {
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
                    String resp = sdk.invoke();
                    //log.info("\n------------接口调用结果-------------" + resp);
                    // resp = FileUtils.readFileToString(new File(TMConfigs.mockDir, "itemapi.json"));
                    // resp = FileUtils.readFileToString(new File(TMConfigs.mockDir, "tradeitemapi.json"));

                    // resp = filter(resp);
                    // if (resp == null) {
                    // log.info("return value for [" + res + "] for Class: " + this.getClass());
                    // continue;
                    // }

                    res = validResponse(resp);

                    /**
                     * no results got and exception, continue retry
                     */
                    if (res == null) {
                        log.info("return value for [" + res + "] for Class: " + this.getClass());
                    } else {
                        doneForThisTime = true;
                    }
                    
                    res = applyResult(res);

                } catch (OpenApiException e) {
                    log.error(String.format("Request Failed. code:%d, msg:%s\n", e.getErrorCode(), e.getMessage()));
                    log.error(e.getMessage(), e);
                }

            }
        }
        return res;
    }

    /**
     * 过滤返回结构中的try catch
     * 
     * @param resp
     * @return
     */
    public String filter(String resp) {
        if (StringUtils.isEmpty(resp)) {
            return null;
        }
        if (resp.trim().startsWith("try")) {
            int equalpos = resp.indexOf('=');

            if (equalpos > 0) {
                return resp.substring(equalpos + 1, resp.indexOf(';'));
            } else {
                int left = resp.indexOf('(');
                int right = resp.indexOf(')');
                if (left > 0 && right > left) {
                    return resp.substring(left + 1, right);
                }
            }
            return null;
        }
        return resp;
    }

    /**
     * 获取请求的apiPath
     * 
     * @return apiPath 如 “/user/getUserInfo.xhtml”
     */
    public abstract String getApiPath();

    /**
     * 准备参数，params
     * 
     * @return false 表示正常；true表示参数处理有误
     */
    public abstract boolean prepareRequest(HashMap<String, Object> params);

    /**
     * 验证返回字符串
     * 
     * @param resp
     * @return false 表示正常；true表示返回结果有误
     */
    public abstract W validResponse(String resp);

    /**
     * 将返回内容解析成对应的类
     * 
     * @param res
     * @return
     */
    public abstract W applyResult(W res);

}
