
package pojo.webpage.top;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopHotKeyParger extends MiningPager<TopSaleUpPojo, TopHotKeyItem> {

//    protected boolean useDirectPage = true;

    private static final Logger log = LoggerFactory.getLogger(TopHotKeyParger.class);

    public static final String TAG = "TopHotKeyParger";

    public static final TopHotKeyParger worker = new TopHotKeyParger();

    protected int maxPage = Integer.MAX_VALUE;

    String nextBtnPath = "//div[@id='content']//a[@class='page-next']";

    @Override
    protected TopHotKeyItem createPojo() {
        return new TopHotKeyItem();
    }

    @Override
    public String toString() {
        return "TopSalePager [maxPage=" + maxPage + ", nextBtnPath=" + nextBtnPath + "]";
    }

    @Override
    protected String getNextPagePath() {
        return nextBtnPath;
    }


    public  boolean useDirectPage(){
        return true;
    }
}
