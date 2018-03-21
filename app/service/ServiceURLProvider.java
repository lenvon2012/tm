
package service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.NumberUtil;

import configs.TMConfigs.Server;

public class ServiceURLProvider {

    private static final Logger log = LoggerFactory.getLogger(ServiceURLProvider.class);

    public static final String TAG = "ServiceURLProvider";

    List<TMURL> urls;

    static int count;

    int totalWeight;

    String[] urlString;

    public ServiceURLProvider(List<TMURL> urls) {
        if (CommonUtils.isEmpty(urls)) {
            throw new IllegalArgumentException(" no urls...");
        }

        for (TMURL tmurl : urls) {
            totalWeight += tmurl.weight;
        }
        urlString = new String[totalWeight];
        int count = 0;
        for (TMURL tmurl : urls) {
            int currWeight = tmurl.weight;
            while (currWeight-- > 0) {
                urlString[count++] = tmurl.url;
            }
        }
        log.error("final url string :" + ArrayUtils.toString(urlString));
    }

    public String getNext() {
        int now = count++;
        return urlString[now % urlString.length];
    }

    public static class TMURL {
        String url;

        int weight;

        public TMURL(String url, int weight) {
            super();
            this.url = url;
            this.weight = weight;
        }

        @Override
        public String toString() {
            return "TMURL [url=" + url + ", weight=" + weight + "]";
        }

    }

    public static ServiceURLProvider tbtProvider = null;

    public static ServiceURLProvider instantWindowProvider = null;

    public static void init() {
        if (StringUtils.isBlank(Server.DISPATCH_URLS)) {
            return;
        }
        String rawUrls = Server.DISPATCH_URLS;
        tbtProvider = rebuildRawUrls(rawUrls);
        instantWindowProvider = rebuildRawUrls(Play.configuration.getProperty("instant.dispatch.url", ""));
        if (instantWindowProvider == null) {
            instantWindowProvider = tbtProvider;
        }
    }

    private static ServiceURLProvider rebuildRawUrls(String rawUrls) {
        if (StringUtils.isBlank(rawUrls)) {
            return null;
        }
        List<TMURL> res = new ArrayList<TMURL>();
        String[] url = rawUrls.split(";");
        for (String string : url) {
            String[] splits = string.split(",");
            String host = splits[0];
            int weight = NumberUtil.parserInt(splits[1], 1);
            TMURL tmUrl = new TMURL(host, weight);
            res.add(tmUrl);
        }

        log.error("init url :" + res);
        return new ServiceURLProvider(res);
    }

}
