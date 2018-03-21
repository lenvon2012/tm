/**
 * 
 */
package utils;

import com.ciaosir.client.api.SimpleHttpApi.WebContentApi;

/**
 * @author navins
 * @date 2013-5-28 下午9:46:35
 */
public class WebContentSimpleApi extends WebContentApi {
    
    public WebContentSimpleApi(String url, String referer, String ua) {
        super(url, referer, ua);
    }

    public WebContentSimpleApi(String url, String referer, String ua, String cookie) {
        super(url, referer, ua);
        this.urlBuilder.appendParam(PARAM_COOKIE, cookie);
    }

}
