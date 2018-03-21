package bustbapi;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author navins
 * @date 2013-1-2 下午1:21:39
 */
public class IdNickApi {

    private static final Logger log = LoggerFactory.getLogger(IdNickApi.class);

    public static final String REQUEST_URL = "http://rate.taobao.com/user-rate-";

    public static final String DEFAULT_REFER = "http://shopsearch.taobao.com/search?v=shopsearch&q=";

    public static String getUserInfo(long userid, int retry) {
        while(retry-- > 0) {
            String nick = getUserInfo(userid);
            if(nick == null) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
                continue;
            }
            return nick;
        }
        return null;
    }
    
    public static String getUserInfo(long userid) {
        String result = null;
        HttpClient httpclient = null;

        HttpResponse rsp = null;

        try {
            httpclient = new DefaultHttpClient();
            HttpConnectionParams.setSoTimeout(httpclient.getParams(), 5000);
            HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), 5000);
            HttpGet httpGet = new HttpGet(REQUEST_URL + userid + ".htm");
            httpGet.addHeader("Referer", DEFAULT_REFER);
            rsp = httpclient.execute(httpGet);

//            log.info("return status code :" + rsp.getStatusLine());
            if (rsp != null) {
                if(rsp.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    return "302";
                }
                HttpEntity entity = rsp.getEntity();
    
                if (entity != null) {
                    String content = EntityUtils.toString(entity);
                    result = parseContent(userid, content);
                }
                
                EntityUtils.consume(entity);
            }
        } catch (IllegalStateException e) {
            log.warn(e.getMessage(), e);
        } catch (Exception e) {
            log.warn(e.getMessage());
        } finally {
            if(httpclient != null) {
                httpclient.getConnectionManager().shutdown();
            }
        }

        return result;
    }
    
    private static String parseContent(long userid, String content) throws UnsupportedEncodingException {
        if (StringUtils.isEmpty(content)) {
            return null;
        }
        int start = content.indexOf("data-nick=");
        if (start == -1) {
            if(content.indexOf("class=\"attention\">") > 0) {
                return StringUtils.EMPTY;
            }
            return null;
        }
        content = content.substring(start + 11, start + 200);
        String nick = content.substring(0, content.indexOf('"')).trim();
        nick = URLDecoder.decode(nick, "UTF-8");
        if (StringUtils.isEmpty(nick)) {
            return null;
        }

        return nick;
    }
}
