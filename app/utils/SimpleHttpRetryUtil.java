
package utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.api.API;

public class SimpleHttpRetryUtil {

    private static final Logger log = LoggerFactory.getLogger(SimpleHttpRetryUtil.class);

    private static final int MinRetryTime = 2;

    public static final int DefaultRetryTime = 3;

    public static String retryGetWebContent(String url, String refer) {
        return retryGetWebContent(url, refer, null, DefaultRetryTime);
    }

    //因为SimpleHttpApi.webContent(url, referer, option.getRetryTime());
    //webContent接口本质上没有重试的，因为它捕获ClientException异常，没有捕获read time out
    public static String retryGetWebContent(String url, String refer, String cookie, int retryTime) {
        if (retryTime < MinRetryTime)
            retryTime = MinRetryTime;
        do {
            long startTime = System.currentTimeMillis();
            int index = 0;
            WebContentSimpleApi api;
            if (StringUtils.isEmpty(cookie)) {
                api = new WebContentSimpleApi(url, refer, API.DEFAULT_UA);
            } else {
                api = new WebContentSimpleApi(url, refer, API.DEFAULT_UA, cookie);
            }
            try {
                String content = api.execute();

                if (!StringUtils.isEmpty(content)) {
                    return content;
                }
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                long endTime = System.currentTimeMillis();
                double second = ((double) (endTime - startTime)) / 1000.0;
                log.error(">>>>>>using host error: "  + api.getHost());
                log.error("WebContentApi获取页面失败：重试" + (index + 1) + ", 花费" + second + "秒" + ":" + url);
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }

        } while ((retryTime--) > 0);

        return null;
    }

}
