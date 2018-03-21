package spider;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;

import spider.mainsearch.MainSearchApi.TBSearchRes;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.ItemThumb;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.NumberUtil;

@JsonAutoDetect
public class ItemThumbSecond implements Serializable
{
    private static Logger log = Logger.getLogger(ItemThumbSecond.class);
    
    private static final long serialVersionUID = -20529409161351427L;

    @JsonProperty
    protected Long id;

    @JsonProperty(value = "title")
    protected String fullTitle;

    // 交易笔数
    @JsonProperty
    protected int tradeNum;

    // 月交易件数
//    @JsonProperty
    protected int periodSoldQuantity;

    @JsonProperty
    protected int price;

    @JsonProperty
    protected Long sellerId;

    @JsonProperty
    protected String picPath;

    @JsonProperty
    protected String wangwang;
    
    @JsonProperty
    protected String url;
    
    @JsonProperty
    protected String delistTimes;
    
    @JsonProperty
    protected String delistTimestamp;

//    @JsonProperty
//    protected static String href;
    
    public String getdelistTimestamp() {
        return delistTimestamp;
    }

    public void setdelistTimestamp(String delistTimestamp) {
        this.delistTimestamp = delistTimestamp;
    }

    public String getdelistTimes() {
        return delistTimes;
    }

    public void setdelistTimes(String delistTimes) {
        this.delistTimes = delistTimes;
    }

    static int i = 1;
    
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullTitle() {
        return fullTitle;
    }

    public void setFullTitle(String fullTitle) {
        this.fullTitle = fullTitle;
    }

    public int getTradeNum() {
        return tradeNum;
    }

    public void setTradeNum(int tradeNum) {
        this.tradeNum = tradeNum;
    }

    public int getPeriodSoldQuantity() {
        return periodSoldQuantity;
    }

    public void setPeriodSoldQuantity(int periodSoldQuantity) {
        this.periodSoldQuantity = periodSoldQuantity;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    @Override
    public String toString() {
        return "ItemThumbE [id=" + id + ", fullTitle=" + fullTitle + ", delistTimes=" + delistTimes + ", delistTimestamp=" + delistTimestamp + ", tradeNum=" + tradeNum + ", periodSoldQuantity="
                + periodSoldQuantity + ", price=" + price + ", sellerId=" + sellerId + ", picPath=" + picPath + "]";
    }

    public static String replaceHtml(String jsonResult) {
        jsonResult = jsonResult.replaceAll("<span\\sclass=\\\\'h\\\\'>", StringUtils.EMPTY);
        jsonResult = jsonResult.replaceAll("<\\\\/span>", StringUtils.EMPTY);
        jsonResult = jsonResult.replaceAll("&lt;span", StringUtils.EMPTY);
        jsonResult = jsonResult.replaceAll("&gt;", StringUtils.EMPTY);
        jsonResult = jsonResult.replaceAll("&lt;\\span&gt", StringUtils.EMPTY);
        jsonResult = jsonResult.replaceAll("\\\\&quot;", StringUtils.EMPTY);

        return jsonResult;

    }
    
//    public static String geturl(int i) throws Exception
//    {
//        String url = GetURLInfo.GetUrl(TBSearchRes.getHref()).get(i).toString();
//        
//        return url;
//    }

    public static ItemThumbSecond parseItemThumFromItemList(boolean isRelaceHtmlNeeded, JsonNode node, int tradeNum) 
    {
        ItemThumbSecond item = new ItemThumbSecond();
        item.setId(NumberUtil.parserLong(node.path("itemId").getTextValue(), -1L));
        
        String title = node.path(isRelaceHtmlNeeded ? "title" : "fullTitle").getTextValue();
        if (StringUtils.isEmpty(title)) 
        {
            title = node.path("title").getTextValue();
        }
        item.setFullTitle(title);
        
        String url = node.path("url").getTextValue();
        item.setUrl(url);
        
        String delistTimes = node.path("delistTimes").getTextValue();
        item.setdelistTimes(delistTimes);
        
        String delistTimestamp = node.path("delistTimestamp").getTextValue();
        item.setdelistTimestamp(delistTimestamp);
        
        
        String picPath = node.path("originalImage").getTextValue();
        if (StringUtils.isEmpty(picPath)) 
        {
            picPath = node.path("image").getTextValue();
        }
        item.setPicPath(picPath);
        
        item.setPrice(NumberUtil.getIntFromPrice(node.path("price").getTextValue()));
        
        item.setSellerId(NumberUtil.parserLong(node.path("sellerId").getTextValue(), -1L));
        
        item.setWangwang(node.path("nick").getTextValue());

//        System.out.println("itemid :" + item.getId() + ":" + node.path("renqi").getTextValue() + "  with :" + node);

        if (tradeNum > 0) {
            item.setTradeNum(tradeNum);
        } else {
            item.setTradeNum(NumberUtil.parserInt(node.path("tradeNum").getTextValue(), 0));
        }
        return item;
    }

    @SuppressWarnings("unchecked")
    public static Map<Long, ItemThumbSecond> toMap(List<ItemThumbSecond> thumbs) {
        if (CommonUtils.isEmpty(thumbs)) {
            return MapUtils.EMPTY_MAP;
        }

        Map<Long, ItemThumbSecond> res = new HashMap<Long, ItemThumbSecond>();
        for (ItemThumbSecond ItemThumbE : thumbs) {
            res.put(ItemThumbE.getId(), ItemThumbE);
        }
        return res;
    }

    public static List<ItemThumbSecond> parserItemListWithListTaobao(String content) {
        JsonNode rootNode = JsonUtil.readJsonResult(content);
        return parserItemListWithListTaobao(rootNode);
    }

    public static List<ItemThumbSecond> parserItemListWithListTaobao(JsonNode rootNode) {
        List<ItemThumbSecond> items = new ArrayList<ItemThumbSecond>();

        JsonNode itemList = rootNode.get("itemList");
        for (JsonNode node : itemList) {
            ItemThumbSecond item = ItemThumbSecond.parseItemThumFromItemList(false, node, 0);
            items.add(item);
        }

        return items;
    }
    

    public String getWangwang() {
        return wangwang;
    }

    public void setWangwang(String wangwang) {
        this.wangwang = wangwang;
    }

    @SuppressWarnings("unchecked")
    public static Map<Long, Integer> toNumIidSaleMap(List<ItemThumbSecond> thumbs) {
        if (CommonUtils.isEmpty(thumbs)) {
            return MapUtils.EMPTY_MAP;
        }

        Map<Long, Integer> map = new HashMap<Long, Integer>();
        for (ItemThumbSecond ItemThumbE : thumbs) {
            map.put(ItemThumbE.getId(), ItemThumbE.getTradeNum());
        }

        return map;
    }
}
