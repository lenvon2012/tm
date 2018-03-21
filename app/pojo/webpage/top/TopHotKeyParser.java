
package pojo.webpage.top;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class TopHotKeyParser extends DBTParser<TopHotUpKeyPojo> {

    public TopHotUpKeyPojo parse(String url) {
        TopHotUpKeyPojo item = new TopHotUpKeyPojo();
        HtmlPage page = WebClientPool.getDirrectPage(url);
        if (page == null) {
            return null;
        }
        item.parse(page, "");
        return item;
    }
}
