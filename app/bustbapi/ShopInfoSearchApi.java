
package bustbapi;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.api.SimpleHttpApi.WebContentApi;
import com.ciaosir.client.item.ShopInfo;
import com.ciaosir.client.utils.NumberUtil;

public class ShopInfoSearchApi {
    private static final Logger log = LoggerFactory.getLogger(ShopInfoSearchApi.class);

    private static final String ShopInfoUrl = "http://shopsearch.taobao.com/search?";

    private static final String DefaultRefer = "http://www.taobao.com";

    public static List<ShopInfo> doSearch(String query, int startIndex) {
        List<ShopInfo> shopList = new ArrayList<ShopInfo>();
        try {
            String encodeQuery = URLEncoder.encode(query, "GBK");
            String resultUrl = ShopInfoUrl + "q=" + encodeQuery + "&s=" + startIndex;

            String content = JsoupSpiderUtil.retryGetWebContent(resultUrl, DefaultRefer);
            if (StringUtils.isEmpty(content)) {
                return shopList;
            }
            Document doc = JsoupSpiderUtil.parseJsoupDocument(content);
            if (doc == null)
                return shopList;
            Elements shopEleList = JsoupSpiderUtil.getElesByCssQuery(doc,
                    "div#list-content ul#list-container li.list-item");
            if (CommonUtils.isEmpty(shopEleList))
                return shopList;

            for (Element shopEle : shopEleList) {
                ShopInfo shopInfo = getShopInfo(shopEle);
                shopList.add(shopInfo);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

        return shopList;
    }

    private static ShopInfo getShopInfo(Element shopEle) {
        ShopInfo shopInfo = new ShopInfo();
        try {
            //宝贝数
            String itemCountStr = JsoupSpiderUtil
                    .getTextByCssQuery(shopEle, "p.item-bottom span.pro-sale-num em:eq(0)");
            shopInfo.setItemCount(NumberUtil.parserInt(itemCountStr, 0));

            //月销量
            String latestTradeCountStr = JsoupSpiderUtil.getTextByCssQuery(shopEle,
                    "p.item-bottom span.pro-sale-num em:eq(1)");
            shopInfo.setLatestTradeCount(NumberUtil.parserInt(latestTradeCountStr, 0));

            //level
            boolean isTmall = JsoupSpiderUtil.hasElementByCssQuery(shopEle, "ul li.list-info h4 a.mall-icon");
            int level = 0;
            if (isTmall == true) {
                level = -1;//-1表示天猫
                shopInfo.setBShop(true);
            } else {
                String levelCss = JsoupSpiderUtil.getAttrByCssQuery(shopEle, "ul li.list-info h4 a.rank", "class");

                if (!StringUtils.isEmpty(levelCss)) {
                    //rank seller-rank-12
                    int index = levelCss.lastIndexOf("-");
                    if (index >= 0) {
                        levelCss = levelCss.substring(index + 1);
                        level = NumberUtil.parserInt(levelCss, 0);
                    }
                }
                shopInfo.setBShop(false);
            }

            shopInfo.setLevel(level);

            //userId
            String userIdStr = JsoupSpiderUtil.getAttrByCssQuery(shopEle, "ul li.list-info h4 a.rank", "href");
            if (StringUtils.isEmpty(userIdStr)) {
                userIdStr = JsoupSpiderUtil.getAttrByCssQuery(shopEle, "ul li.list-info h4 a.mall-icon", "href");
            }
            //http://rate.taobao.com/user-rate-409789068.htm
            int userIdStart = userIdStr.lastIndexOf("rate-");
            if (userIdStart >= 0) {
                int end = userIdStr.indexOf(".htm", userIdStart);
                if (end >= 0) {
                    userIdStr = userIdStr.substring(userIdStart + "rate-".length(), end);
                }
            }
            shopInfo.setUserId(NumberUtil.parserLong(userIdStr, 0L));

            //图片
            String picPath = JsoupSpiderUtil.getAttrByCssQuery(shopEle, "ul li.list-img a img", "src");
            shopInfo.setPicPath(picPath);

            //nick
            String wangwang = JsoupSpiderUtil.getTextByCssQuery(shopEle,
                    "ul li.list-info p.shop-info span.shop-info-list > a");
            shopInfo.setShopnick(wangwang);

            //shopId
            String shopIdStr = JsoupSpiderUtil.getAttrByCssQuery(shopEle,
                    "ul li.list-info p.shop-info span.shop-info-list > a", "href");
            if (!StringUtils.isEmpty(shopIdStr)) {
                //http://shop60528226.taobao.com
                int start = shopIdStr.indexOf("shop");
                if (start >= 0) {
                    int end = shopIdStr.indexOf(".taobao", start);
                    if (end >= 0) {
                        shopIdStr = shopIdStr.substring(start + "shop".length(), end);
                    }
                }

            }
            shopInfo.setShopId(NumberUtil.parserLong(shopIdStr, 0L));

            //地区
            String area = JsoupSpiderUtil.getTextByCssQuery(shopEle, "ul li.list-info p.shop-info span.shop-address");
            shopInfo.setArea(area);
            //log.error(area + "");
            //log.error(shopInfo.toString());
            //log.error("");
            //log.error("");
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

        return shopInfo;
    }

    public static void main(String[] args) {
        doSearch("雷", 0);
    }

    public static class JsoupSpiderUtil {

        private static final int DefaultRetryTime = 3;

        private static final String Default_UA = "Mozilla/4.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)";

        public static String retryGetWebContent(String url, String refer) {
            return retryGetWebContent(url, refer, DefaultRetryTime);
        }

        public static String retryGetWebContent(String url, String refer, int retryTime) {
            do {
                long startTime = System.currentTimeMillis();
                int index = 0;
                try {
                    WebContentApi api = new WebContentApi(url, refer, Default_UA);
                    String content = api.execute();

                    if (!StringUtils.isEmpty(content))
                        return content;
                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                    long endTime = System.currentTimeMillis();
                    double second = ((double) (endTime - startTime)) / 1000.0;
                    log.error("WebContentApi获取页面失败：重试" + (index + 1) + ", 花费" + second + "秒" + ":" + url);
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }

            } while ((retryTime--) > 0);

            return null;
        }

        public static Document parseJsoupDocument(String html) {
            if (StringUtils.isEmpty(html))
                return null;
            try {
                Document doc = Jsoup.parse(html);
                return doc;
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                log.error("fail to change html to Jsoup Document!!!!");
            }
            return null;
        }

        public static Elements getElesByCssQuery(Document doc, String cssQuery) {
            Elements eles = doc.select(cssQuery);
            if (CommonUtils.isEmpty(eles)) {

            }
            return eles;
        }

        private static Element getUniqueEleByCssQuery(Element fatherEle, String cssQuery) {
            if (fatherEle == null) {
                log.error("fatherEle is null!!!!");
                return null;
            }
            Elements eles = fatherEle.select(cssQuery);
            if (CommonUtils.isEmpty(eles)) {
                //log.error("can not find element by cssQuery: " + cssQuery + "!!!!!!!!!!!");
                return null;
            }
            if (eles.size() > 1) {
                //log.error("find more than 1 element by cssQuery: " + cssQuery + "!!!!!!!!!!!");
                //return null;
            }
            return eles.get(0);
        }

        public static String getTextByCssQuery(Element fatherEle, String cssQuery) {
            Element ele = getUniqueEleByCssQuery(fatherEle, cssQuery);
            if (ele == null)
                return "";
            String html = ele.text();
            if (StringUtils.isEmpty(html))
                return "";
            html = dealWithJsoupText(html);
            html = html.trim();
            return html;
        }

        public static String getAttrByCssQuery(Element fatherEle, String cssQuery, String attr) {
            Element ele = getUniqueEleByCssQuery(fatherEle, cssQuery);
            if (ele == null)
                return "";
            String value = ele.attr(attr);
            if (StringUtils.isEmpty(value))
                return "";
            return value.trim();
        }

        //jsoup解析返回的如果有&nbsp，是替换成160，而不是32。。。
        private static String dealWithJsoupText(String text) {
            if (StringUtils.isEmpty(text))
                text = "";
            return text.replace((char) 160, (char) 32).trim();
        }

        public static boolean hasElementByCssQuery(Element fatherEle, String cssQuery) {
            if (fatherEle == null) {
                log.error("fatherEle is null!!!!");
                return false;
            }
            Elements eles = fatherEle.select(cssQuery);
            if (CommonUtils.isEmpty(eles)) {
                return false;
            }
            if (eles.size() > 1) {
                log.error("find more than 1 element by cssQuery: " + cssQuery + "!!!!!!!!!!!");
            }
            return true;
        }
    }

}
