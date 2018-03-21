
package pojo.webpage.top;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pojo.webpage.top.TopSaleUpPojo.TopSaleUpItemPojo;

import com.ciaosir.client.CommonUtils;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;

public class TopSalePager extends Pager<TopSaleUpPojo, TopSaleUpItemPojo> {

    private static final Logger log = LoggerFactory.getLogger(TopSalePager.class);

    public static final String TAG = "TopSalePager";

    public static final TopSalePager worker = new TopSalePager();

    int maxPage = 1;

    String path = "//div[@id='content']/div[1][@class='col-main']/div[@class='main-wrap']/div[4][@class='bangbox']/div[2][@class='bd']/div[@class='itemlist']/div[1][@class='items']/ol/li";

    ////div[@id='content' and @class='grid-c2']/div[1][@class='col-main']/div[@class='main-wrap']/div[4][@class='bangbox']/div[2][@class='bd']/div[@class='itemlist clearfix']/div[1][@class='items imagelists']/div[2][@class='pagination']/div[@class='page-bottom']/a[10][@class='page-next']/span
    String nextBtnPath = "//div[@id='content']//a[@class='page-next']";

    public List<TopSaleUpItemPojo> getAll(DomNode node, String currHref, String listPath) {

        log.info(format("getAll:node, currHref, listPath".replaceAll(", ", "=%s, ") + "=%s", node, currHref, listPath));

        List<TopSaleUpItemPojo> pojos = new ArrayList<TopSaleUpItemPojo>();

        int currPageNum = 1;

        do {
            log.info("Do for the page :" + currPageNum);

            List<HtmlElement> elems = (List<HtmlElement>) node.getByXPath(listPath);

            log.info("[Element Size :]" + elems.size());

            for (HtmlElement elem : elems) {
                log.info("[For Page Item]" + elem.asXml());
                TopSaleUpItemPojo pojo = new TopSaleUpItemPojo();
                pojo.parse(elem, currHref);
                log.info("[Set Pojo]" + pojo);
                pojos.add(pojo);
            }

            node = clickForTheNextPage(node, currHref);
            if (node == null) {
                return pojos;
            }
        } while (currPageNum++ < maxPage);
        return pojos;
    }

    protected DomNode clickForTheNextPage(DomNode node, String currHref) {
        List<HtmlElement> elems = (List<HtmlElement>) node.getByXPath(nextBtnPath);
        log.info("[Click Items]" + elems);
        if (CommonUtils.isEmpty(elems)) {
            return null;
        }

        HtmlAnchor anchor = (HtmlAnchor) elems.get(0);
        String nextUrl = anchor.getHrefAttribute();
        log.info("[next url]" + nextUrl);
        return WebClientPool.getPage(nextUrl);
    }

    @Override
    public String toString() {
        return "TopSalePager [maxPage=" + maxPage + ", path=" + path + ", nextBtnPath=" + nextBtnPath + "]";
    }

}
