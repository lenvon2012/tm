
package pojo.webpage.top;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlElement;

public class Pager<T extends WebPojo, V extends WebPojo> {

    private static final Logger log = LoggerFactory.getLogger(Pager.class);

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

    protected int maxPage;

    protected int getMaxPage() {
        return this.maxPage;
    }

//
//    public List<V> getAll(DomNode node, String currHref, String listPath) {
//        return ListUtils.EMPTY_LIST;
//    }

    public List<V> getAll(DomNode node, String currHref, String listPath) {

        List<V> pojos = new ArrayList<V>();

        int currPageNum = 1;

        do {
            log.info("Do for the page :" + currPageNum);

            List<HtmlElement> elems = (List<HtmlElement>) node.getByXPath(listPath);

            log.info("[Element Size :]" + elems.size());

            for (HtmlElement elem : elems) {
                log.info("[For Page Item]" + elem.asXml());
                V pojo = createPojo();
                pojo.parse(elem, currHref);
                log.info("[Set Pojo]" + pojo);
                pojos.add(pojo);
            }

            node = clickForTheNextPage(node, currHref);
            if (node == null) {
                return pojos;
            }
        } while (currPageNum++ < getMaxPage());
        return pojos;
    }

    protected V createPojo() {
        return null;
    }

    protected DomNode clickForTheNextPage(DomNode node, String currHref) {
        return null;
    }

    public static Map<String, Pager> instances = new HashMap<String, Pager>();

    static {
        instances.put(TopSalePager.class.getName(), TopSalePager.worker);
        instances.put(TopHotKeyParger.class.getName(), new TopHotKeyParger());
    }

}
