
package pojo.webpage.top;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;

public abstract class MiningPager<T extends WebPojo, V extends WebPojo> extends Pager<T, V> {

    private static final Logger log = LoggerFactory.getLogger(MiningPager.class);

    public static final String TAG = "Pager";

    public static class PageResult {
        int pageNum;

        List<HtmlElement> elems;

        public PageResult(int pageNum, List<HtmlElement> elems) {
            super();
            this.pageNum = pageNum;
            this.elems = elems;
        }
    }

//    protected int maxPage;

//
//    public List<V> getAll(DomNode node, String currHref, String listPath) {
//        return ListUtils.EMPTY_LIST;
//    }
    @Override
    public int getMaxPage() {
        return Integer.MAX_VALUE;
    }

    @Override
    public List<V> getAll(DomNode node, String currHref, String listPath) {

        List<V> pojos = new ArrayList<V>();

        int currPageNum = 1;

        do {
            log.info("Do for the page :" + currPageNum);

            List<HtmlElement> elems = (List<HtmlElement>) node.getByXPath(listPath);

            log.info("[Element Size :]" + elems.size());

            for (HtmlElement elem : elems) {
//                log.info("[For Page Item]" + elem.asXml());
                V pojo = createPojo();
//                log.info("Path :" + elem.getByXPath("//tr/td/span[4][@class='grow']/i"));
//                log.info("tr :" + elem.getByXPath("/tr"));
//                log.info("td :" + elem.getByXPath("/td"));
//                log.info("td :" + elem.getByXPath("./tr"));
//                log.info("//td :" + elem.getByXPath("//td"));

//                log.info("Path :" + elem.getByXPath("/tr/td/span[4][@class='grow']/i"));
//                log.info("Path :" + elem.getByXPath("//td/span[4][@class='grow']/i"));
//                log.info("Path :" + elem.getByXPath("/"));
//                log.info("Child :" + elem.getChildNodes());
//                log.info("Name :" + elem.getNodeName());
//                log.info("Text :" + elem.getTextContent());
//                log.info("Page :" + elem.getPage());
//                elem.get
//                log.info(" First Child :" + elem.getFirstChild());

                pojo.parse(elem, currHref);
//                log.info("[Set Pojo]" + pojo);
                pojos.add(pojo);

            }

            node = clickForTheNextPage(node, currHref);
            if (node == null) {
                return pojos;
            }
        } while (currPageNum++ < getMaxPage());
        return pojos;
    }

    abstract protected V createPojo();

    protected DomNode clickForTheNextPage(DomNode node, String currHref) {
        log.info("[this class ]" + this.getClass() + " with use direct :" + useDirectPage());
        List<HtmlElement> elems = (List<HtmlElement>) node.getByXPath(getNextPagePath());
        log.info("[Click Items]" + elems);
        if (CommonUtils.isEmpty(elems)) {
            log.warn("No Next Button found.....:" + currHref);
            return null;
        }

        HtmlAnchor anchor = (HtmlAnchor) elems.get(0);
        String nextUrl = anchor.getHrefAttribute();
        log.info("[next url]" + nextUrl);

        CommonUtils.sleepQuietly(500L);
        if (useDirectPage()) {
            return WebClientPool.getDirrectPage(nextUrl);
        } else {
            return WebClientPool.getPage(nextUrl);
        }
    }

    public static Map<String, MiningPager> instances = new HashMap<String, MiningPager>();

    abstract protected String getNextPagePath();

//
//    static {
//        instances.put(MiningPager.class.getName(), MiningPager.worker);
//    }

    protected boolean useDirectPage() {
        return false;
    }

}
