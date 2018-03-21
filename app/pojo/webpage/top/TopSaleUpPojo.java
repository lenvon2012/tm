
package pojo.webpage.top;

import java.util.List;

/**
 * Only level 3
 * @author zhourunbo
 *
 */
public class TopSaleUpPojo extends WebPojo {

    public TopSaleUpPojo() {
    }

    @XpathFieldForm(possiblePatterns = {
        "//div[@id='content']/div[1][@class='col-main']/div[@class='main-wrap']/div[4][@class='bangbox']/div[2][@class='bd']/div[@class='itemlist clearfix']/div[1][@class='items imagelists']/ol/li"
    }, pager = TopSalePager.class)
    public List<TopSaleUpItemPojo> items;

    @Override
    public String toString() {
        return "TopSaleUpPojo [items=" + items + "]";
    }

    public static class TopSaleUpItemPojo extends WebPojo {
        public TopSaleUpItemPojo() {

        }

        @XpathFieldForm(possiblePatterns = {
            "//li/a[2][@class='name']"
        }, attr = "href")
        public String href;

        @XpathFieldForm(possiblePatterns = {
            "//li/div/span[@class='upgrade']"
        })
        public String upgrade;

        @XpathFieldForm(possiblePatterns = {
            "//li/span[1]"
        })
        public String price;

        @XpathFieldForm(possiblePatterns = {
            "//li/span[2]"
        })
        public String weekSaleCount;

        @XpathFieldForm(possiblePatterns = {
            "//li/a[2][@class='name']"
        })
        public String title;

        @Override
        public String toString() {
            return "TopSaleUpItemPojo [href=" + href + ", upgrade=" + upgrade + ", price=" + price + ", weekSaleCount="
                    + weekSaleCount + ", title=" + title + "]";
        }

    }

}
