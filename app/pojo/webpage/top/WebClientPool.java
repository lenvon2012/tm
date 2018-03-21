
package pojo.webpage.top;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.StackObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.libs.WS;

import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

//import org.apache.xml.utils.ObjectPool;

public class WebClientPool {

    private static final Logger log = LoggerFactory.getLogger(WebClientPool.class);

    public static final String TAG = "WebClientPool";

    public static ObjectPool<WebClient> clientPool = new StackObjectPool<WebClient>(new WebClientFactory());

    public static HtmlPage getPage(String url) {
        return getPage(url, null);
    }

    public static abstract class SingleClientCaller implements Callable<Void> {

        @Override
        public Void call() {
            WebClient client = null;
            try {
                try {
                    client = clientPool.borrowObject();
                    doWithClient(client);
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                }

            } finally {
                returnClientQuietly(client);
            }
            return null;
        }

        public abstract void doWithClient(WebClient client) throws Exception;

    }

    public static HtmlPage getDirrectPage(String url) {
        try {
            /**
             * 暂时先直接用gbk
             */
            String content = WS.url(url).get().getString("gbk");
//            log.info("[content]" + content);
            StringWebResponse response = new StringWebResponse(content, "gbk", new URL(url));
            WebClient client = new WebClient();
            client.setCssEnabled(false);
            client.setJavaScriptEnabled(false);
            HtmlPage page = HTMLParser.parseHtml(response, client.getCurrentWindow());
            return page;
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }
        return null;
    }

    public static HtmlPage getPage(String url, String refer) {
        long start = System.currentTimeMillis();
        WebClient client = null;

        try {
            client = clientPool.borrowObject();

            if (refer != null) {
                client.addRequestHeader("Refer", refer);
            }

//            client.setJavaScriptEnabled(enableJs);
            HtmlPage page = client.getPage(url);

            long end = System.currentTimeMillis();
            log.info("Get Page :[" + url + "] took " + (end - start) + " ms");

            return page;
        } catch (Exception e) {
            log.warn(e.getMessage(), e);

        } finally {
            returnClientQuietly(client);
        }

        return null;
    }

    public static void returnClientQuietly(WebClient client) {
        if (client == null) {
            return;
        }

        try {
            clientPool.returnObject(client);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }

    }
}
