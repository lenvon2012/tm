
package actions.listTaoBao;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TBUrlManager {

    private static final Logger log = LoggerFactory.getLogger(TBUrlManager.class);

    public static final String TAG = "TBUrlManager";

    public static TBUrlManager _instance = new TBUrlManager();

    public TBUrlManager() {
    }

    public static TBUrlManager get() {
        return _instance;
    }

    public boolean isTBUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return false;
        }
        return url.indexOf(".tmall.com") > 0 || url.indexOf(".taobao.com") > 0;

    }

    public String filterForSpm(String targetUrl) {
        return filterFor(targetUrl, "spm");
    }

    public String filterFor(String targetUrl, String param) {
        if (StringUtils.isEmpty(targetUrl)) {
            return null;
        }

        int paramIndex = targetUrl.indexOf(param + "=");
        if (paramIndex < 0) {
            return targetUrl;
        }
        int endIndex = targetUrl.indexOf('&', paramIndex);
        if (endIndex < 0) {
            endIndex = targetUrl.length();
        } else {
            endIndex++;
        }
        targetUrl = targetUrl.substring(0, paramIndex) + targetUrl.substring(endIndex);
        if (targetUrl.endsWith("?") || targetUrl.endsWith("&")) {
            targetUrl = targetUrl.substring(0, targetUrl.length() - 1);
        }

        return targetUrl;
    }

    static List<String> tbUrlPrefixs = new ArrayList<String>();

    public static void loadTBPrefixUrls(File file) {
        try {
            List<String> lines = FileUtils.readLines(file);
            for (String string : lines) {
                if (StringUtils.isBlank(string)) {
                    continue;
                }

                if (!string.startsWith("http://")) {
                    string = "http://" + string;
                }
                tbUrlPrefixs.add(string);
            }
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }
    }

    public boolean isTBUrlOfficialPrefix(String url) {

        for (String tbUrl : tbUrlPrefixs) {
            if (url.startsWith(tbUrl)) {
//                log.info("[prefix : tburl :]" + tbUrl);
                return true;
            }
        }
        if (url.startsWith("https://")) {
            return true;
        }
        return false;
    }

    static String[] args = new String[] {
            "spm", "tracelog", "scm"
    };

    public String filterForUselessParams(String url) {
        if (StringUtils.isBlank(url)) {
            return url;
        }

        for (String arg : args) {
            url = filterFor(url, arg);
        }

        return url;
    }

    public final long findItemId(String url) {
        String itemIdString = findItemIdString(url);
        if (StringUtils.isEmpty(itemIdString)) {
            return -1L;
        }

        try {
            return Long.valueOf(itemIdString);
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

    public final String findItemIdString(String url) {
        if (StringUtils.isEmpty(url)) {
            return null;
        }
        if (url.indexOf('?') < 0 || !isTaobaoItemUrl(url)) {
            return null;
        }

        int idIndex = url.indexOf("id=");
        int offset = 3;
        if (idIndex < 0) {
            idIndex = url.indexOf("default_item_id=");
            if (idIndex < 0) {
                idIndex = url.indexOf("item_num_id=");
                if (idIndex < 0) {
                    return null;
                } else {
                    offset = 12;
                }
            } else {
                offset = 16;
            }
        }

        int nextStop = url.indexOf('&', idIndex);
        if (nextStop < 0) {
            nextStop = url.length();
        }

        return url.substring(idIndex + offset, nextStop);
    }

    public final boolean isTaobaoItemUrl(String src) {
        if (StringUtils.isEmpty(src)) {
            return false;
        }
        if (src.startsWith("http://")) {
            src = src.substring("http://".length());
        }

        return src.startsWith("item.taobao.com") || src.startsWith("detail.tmall.com")
                || src.startsWith("meal.taobao.com") || src.startsWith("item.tmall.com")
                || src.startsWith("detail.taobao.com") || src.startsWith("meal.tmall.com")
                || src.startsWith("shuziitem.taobao.com") || src.startsWith("ju.taobao.com/tg/home")
                || src.startsWith("ju.taobao.com/tg/life_home");

    }
}
