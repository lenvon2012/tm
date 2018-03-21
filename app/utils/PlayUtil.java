
package utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.mvc.Http.Cookie;
import play.mvc.Http.Request;

import com.ciaosir.client.utils.NumberUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PlayUtil {

    public static Object EMPTY_OBJ = new Object();

    public static final void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    public static String getCookieString(Request request, String key) {
        Cookie cookie = request.cookies.get(key);
        return cookie == null ? null : cookie.value;
    }

    public enum OS {
        LINUX, WINDOWS
    }

    public static OS os = null;

    public static OS getOS() {
        if (os != null) {
            return os;
        }

        if (StringUtils.startsWith(System.getProperty("os.name").toLowerCase(), "linux")) {
            os = OS.LINUX;
        } else {
            os = OS.WINDOWS;
        }
        return os;
    }

    public static Gson genPrettyGson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson;
    }

    public static List<Long> parseIdsList(String ids) {
        if (ids == null) {
            return ListUtils.EMPTY_LIST;
        }
        String[] splits = ids.split(",");
        List<Long> idsList = new ArrayList<Long>();
        for (String split : splits) {
            final Long numIid = NumberUtil.parserLong(split, 0L);
            if (numIid <= 0L) {
                continue;
            }

            idsList.add(numIid);
        }

        return idsList;
    }

    public static String trimToShow(String str) {
        if (str == null) {
            return null;
        }
        if (str.length() < 511) {
            return str;
        }
        return str.substring(0, 511);

    }

    private static final Logger log = LoggerFactory.getLogger(PlayUtil.class);

    public static final String TAG = "PlayUtil";

    public static void infoListStringArr(List<String[]> s) {
        if (s == null) {
            log.info(" empty list :" + s);
        }
        StringBuilder sb = new StringBuilder();
        for (String[] strings : s) {
            sb.append(StringUtils.join(strings, ' '));
            sb.append('\n');
        }

        log.info("csv :" + sb.toString());
    }

    public static void printStack(StackTraceElement[] stackElements) {
        if (stackElements == null) {
            return;
        }
        for (int i = 0; i < stackElements.length; i++) {
            StackTraceElement elem = stackElements[i];
            StringBuilder sb = new StringBuilder();
            sb.append(elem.getClassName());
            sb.append("/t");
            sb.append(elem.getFileName());
            sb.append("/t");
            sb.append(elem.getLineNumber());
            sb.append("/t");
            sb.append(elem.getMethodName());
            log.info(sb.toString());
        }
    }
    
    public static String trimValue(String value) {
        if (StringUtils.isEmpty(value)) {
            return "";
        }
        return value.trim();
    }
}
