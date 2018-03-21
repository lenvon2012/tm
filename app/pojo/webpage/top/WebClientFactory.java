
package pojo.webpage.top;

import org.apache.commons.pool.BasePoolableObjectFactory;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;

public class WebClientFactory extends BasePoolableObjectFactory<WebClient> {

    @Override
    public WebClient makeObject() throws Exception {

        WebClient webClient = new WebClient(BrowserVersion.FIREFOX_3_6);
        webClient.setThrowExceptionOnFailingStatusCode(false);
        webClient.setThrowExceptionOnScriptError(false);
        webClient.setCssEnabled(false);
        webClient.setRedirectEnabled(true);

        webClient.setJavaScriptEnabled(true);
        webClient.setTimeout(20000);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());

        webClient.waitForBackgroundJavaScript(5000L);
        webClient.waitForBackgroundJavaScriptStartingBefore(5000L);

        return webClient;
    }

}
