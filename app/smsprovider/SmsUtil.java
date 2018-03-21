package smsprovider;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmsUtil {

    public static Logger log = LoggerFactory.getLogger(SmsUtil.class);

    public final static int SHORT_SMS = 1;
    public final static int LONG_SMS = 2;
    public final static int BALANCE_SMS = 3;

    public static boolean vaildMobileNum(String mobileNum) {

        if (mobileNum.length() != 11 && mobileNum.substring(0, 1) != "1") {
            return false;
        }
        return true;
    }

    public static String filterUnNumber(String str) {
        // 只允数字
        String regEx = "[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        // 替换与模式匹配的所有字符（即非数字的字符将被""替换）
        return m.replaceAll("").trim();
    }

    public static String URLEncodeContent(String content) throws UnsupportedEncodingException {
        return URLEncoder.encode(content, "GBK");
    }

    public static String httpClientExecutor(String url) {

        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(url);

        // 使用系统提供的默认的恢复策略
        getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
        httpClient.getHttpConnectionManager().getParams().setSoTimeout(10000);
        
        int statusCode;
        try {
            statusCode = httpClient.executeMethod(getMethod);
            if (statusCode != HttpStatus.SC_OK) {
                log.error("HttpClient executor url error:" + url + ", msg:" + getMethod.getStatusLine());
            }
            byte[] responseBody = getMethod.getResponseBody();
            if (responseBody == null) {
                return "";
            }

            return new String(responseBody);

        } catch (HttpException e) {
            log.error(e.getMessage(), e);
            return null;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        } finally {
            getMethod.releaseConnection();
        }

    }
}
