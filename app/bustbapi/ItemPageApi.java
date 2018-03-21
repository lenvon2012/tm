
package bustbapi;

import static java.lang.String.format;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import models.item.ItemPlay.ItemPageBean;
import models.user.UserIdNick;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.api.API;
import com.ciaosir.client.api.SimpleHttpApi.WebContentApi;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.NumberUtil;
import com.ciaosir.commons.ClientException;

import configs.TMConfigs.Operate;

/*
 * 销量和价格最好还是不要靠这个，外围更新之。。。
 */
public class ItemPageApi implements Callable<ItemPageBean> {

    static final Logger log = LoggerFactory.getLogger(ItemPageApi.class);

    public static final String TAG = "ItemPageAPIs";

    protected boolean success = false;

    protected Long numIid;

    protected Long sellerId;

    protected ItemPageBean bean = null;

    public ItemPageApi(ItemPageBean bean) {
        this.numIid = bean.getNumIid();
        this.sellerId = bean.getSellerId();
        this.bean = bean;

    }

    public ItemPageApi(Long numIid, Long sellerId, ItemPageBean bean) {
        super();
        this.numIid = numIid;
        this.sellerId = sellerId;
        this.bean = bean;
    }

    public ItemPageBean call() {
        ItemPageBean bean = adapterToPage(numIid);
        if (bean == null) {
            log.warn("no result for :" + this);
            return null;
        }
//        log.warn(" fetch bean :" + bean);
        bean.setSellerId(sellerId);
        return bean;
    }

    /**
     * http://item.taobao.com/item.htm?spm=0.0.0.385.hQY77G&id=18112347158
     * g_config = {
    startTime:+new Date,
    prefetch:[],
    asyncCss:[],
    t:"20121227",
    st:"20121228",
    shopVer:1,
    appId: 1 ,
    itemId:"18112347158",
    shopId:"58730116",
    pageId:"18112347158",
    assetsHost:"http://a.tbcdn.cn",
    enable:true,
    p:1.0,
    type:"cex"  ,counterApi:"http://count.tbcdn.cn/counter3?inc=ICVT_7_18112347158&sign=9072351f9040ba77d4e237d69d6faedd2032f&keys=DFX_200_1_18112347158,ICVT_7_18112347158,ICCP_1_18112347158,SCCP_2_58730116"
    ,rateCounterApi:"http://ratecount.tbcdn.cn/counter7?keys=SM_101_sm-90725881,IR_103_ir-18112347158"
    ,toolbar:{delay:30}  ,
    lazyload:'#J_DivItemDesc' };
     * @param numIid
     * @return 
     */
    public ItemPageBean adapterToPage(Long numIid) {
        String itemUrl = "http://item.taobao.com/item.htm?id=" + numIid;
        String fetchWebPage = fetchWebPage(itemUrl, "http://www.taobao.com", true, 2);
        if (StringUtils.isEmpty(fetchWebPage)) {
            log.warn(" no content back for :" + this);
            return null;
        }
        checkReturnPageWebpage(fetchWebPage);
        int countIndex = fetchWebPage.indexOf("http://count.tbcdn.cn/counter");
        if (countIndex < 0) {
//            log.warn("do with b shop with numIid:" + this);
            bean.setBShop(true);
            return doForBShop(itemUrl, fetchWebPage, numIid);
        } else {
//            log.warn("do with c shop with numIid:" + this);
            bean.setBShop(false);
            return DoForCShop(itemUrl, fetchWebPage, countIndex, numIid);
        }
//        return this.bean;
    }

    private static void checkReturnPageWebpage(String fetchWebPage) {
    }

    /**
     * http://mdskip.taobao.com/core/initItemDetail.htm?ump=false&trialErrNum=0&isSpu=false&isIFC=false&sellerUserTag4=134218115&notAllowOriginPrice=false&sellerUserTag2=18020085046181888&sellerUserTag3=50364544&isAreaSell=false&isForbidBuyItem=false&isMeizTry=false&tmallBuySupport=true&itemTags=72,1478,2049,3974,4166,6146,8258,8578&household=false&sellerUserTag=576262176&tgTag=false&itemId=8578223469&isUseInventoryCenter=true&isSecKill=false&isApparel=false&service3C=false&cartEnable=true&callback=jsonp1357392818965_0&ip=&campaignId=&key=&abt=&cat_id=&q=&u_channel=&ref=
     * ttp://count.tbcdn.cn/counter3?keys=ICCP_1_8578223469&t=1357392818965&callback=TShop.mods.SKU.Stat.setCollectCount
     *    "sellCountDO": {
            "sellCount": 459
        },
     * @param numIid 
     * @param fetchWebPage 
     * @param itemUrl 
     * @return 
     */
    ItemPageBean doForBShop(String itemUrl, String fetchWebPage, Long numIid) {
        //http://count.tbcdn.cn/counter3?keys=ICCP_1_20463352648&t=1357448453790&callback=TShop.mods.SKU.Stat.setCollectCount
        long now = System.currentTimeMillis();

        String url = String.format(
                "http://count.tbcdn.cn/counter3?keys=ICCP_1_%s&t=%s&callback=TShop.mods.SKU.Stat.setCollectCount",
                numIid.toString(), String.valueOf(now));
//        String content = HttpExecutor.useCommonHttpClient(url, itemUrl,
//                DBTConfigs.USE_PROXY ? HttpExecutor.getRandHost(DBTConfigs.listTaobaoPool) : null);
        String content = fetchWebPage(url, itemUrl, false, 2);
//        this.success = !StringUtils.isEmpty(content);

//        if(content =  null){
//            this.success = false;
//        }
        parserItemPageItem(content, bean);
        return bean;
    }

    /**
     * TShop.mods.SKU.Stat.setCollectCount({"ICCP_1_20463352648":562});
     * @param bean 
     * @return 
     */
    public static ItemPageBean parserItemPageItem(String content, ItemPageBean bean) {
        JsonNode node = JsonUtil.parserJSONP(content);
        if (node == null) {
            log.error(" no item page content :" + bean);
            return null;
        }
        Iterator<Entry<String, JsonNode>> fields = node.getFields();
        while (fields.hasNext()) {
            Entry<String, JsonNode> next = fields.next();
            String key = next.getKey();
            if (key.contains("ICVT")) {
                bean.setPv(NumberUtil.parserInt(next.getValue(), -1));
            } else if (key.contains("ICCP")) {
                bean.setShare((NumberUtil.parserInt(next.getValue(), -1)));
            }
        }
        return bean;
    }

    /**
     * DT.mods.SKU.CountCenter.saveCounts({"ICCP_1_14886457770":811,"ICVT_7_14886457770":54898,"SCCP_2_65129401":25645,"DFX_200_1_14886457770":50});
     * ICVT_7_14886457770 --> pv
     * ICCP_1_14886457770 --> share
     * @return 
     */
    ItemPageBean DoForCShop(String itemUrl, String fetchWebPage, int countIndex, Long numIid) {
        int end = fetchWebPage.indexOf('"', countIndex + 1);
        String countUrl = fetchWebPage.substring(countIndex, end).replaceAll("amp;", StringUtils.EMPTY)
                + "&callback=DT.mods.SKU.CountCenter.saveCounts";
//        log.info("[coutn url ]:" + countUrl);
        if (StringUtils.isBlank(countUrl)) {
            return null;
        }

        String content = fetchWebPage(countUrl, itemUrl, false, 2);
        this.success = !StringUtils.isEmpty(content);
        parserItemPageItem(content, bean);
        return bean;
    }

    static int retry = 2;

    public static String fetchWebPage(final String url, final String referer, boolean useAsync, int retry) {
        if (!Operate.USE_SIMPLE_HTTP) {
            return API.directGet(url, referer, null);
        }

        if (retry < 0) {
            return null;
        }

        String content = null;
//        if (useAsync) {
//            content = HttpExecutor.useAsyncHttpClient(url, referer,
//                    DBTConfigs.USE_PROXY ? HttpExecutor.getRandHost(DBTConfigs.listTaobaoPool) : null);
//        } else {
//            content = HttpExecutor.useCommonHttpClient(url, referer,
//                    DBTConfigs.USE_PROXY ? HttpExecutor.getRandHost(DBTConfigs.listTaobaoPool) : null);
//        }

        WebContentApi api = null;
        try {
            api = new WebContentApi(url, referer, null);
            content = api.execute();
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            log.error(" current api :" + ((api == null) ? " no host..." : api.getHost()));
            content = null;
        }
        if (content == null && retry > 0) {
            return fetchWebPage(url, referer, useAsync, --retry);
        }
        return content;
    }

    @Override
    public String toString() {
        return "ItemPageAPIs [numIid=" + numIid + ", sellerId=" + sellerId + ", bean=" + bean + "]";
    }
    
    public static UserIdNick fetchUserId(String url) {

        log.info(format("fetchUserId:url".replaceAll(", ", "=%s, ") + "=%s", url));
        try {
            String call = new WebContentApi(url, "", API.DEFAULT_UA).call();
            int index = call.indexOf("userid=");
            if (index < 0) {
                log.error(" no id :");
                return null;
            }
            // log.info("[content ;]" + call);
            int endIndex = call.indexOf(";", index + 1);
            long id = NumberUtil.parserLong(call.substring(index + "userid=".length(), endIndex), 0L);
            log.error(" id :" + id);
            if (id < 0L) {
                return null;
            }

            index = call.indexOf("data-nick=\"");
            if (index < 0) {
                return null;
            }
            index += "data-nick=\"".length();
            endIndex = call.indexOf("\"", index + 1);
            String nick = call.substring(index, endIndex);
            log.error(" nick :" + nick);
            if (StringUtils.isEmpty(nick)) {
                return null;
            }
            return new UserIdNick(id, nick);

        } catch (ClientException e) {
            log.warn(e.getMessage(), e);
        }
        return null;
    }

}
