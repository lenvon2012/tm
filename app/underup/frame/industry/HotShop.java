package underup.frame.industry;

import models.item.ItemCatPlay;
import underup.frame.industry.CatTopSaleItemSQL;
import underup.frame.industry.CatTopSaleItemSQL.ShopSimpleInfo;
import models.shop.TBShopPlay;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.*;

import underup.frame.industry.LevelPicShopInfo.ShopLevelPicInfos;

import java.util.*;

import bustbapi.BusAPI;
import bustbapi.ShopInfoSearchApi;

import com.ciaosir.client.item.ShopInfo;
import com.ciaosir.commons.ClientException;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.domain.Shop;
import com.taobao.api.domain.ShopScore;
import com.taobao.api.request.UserGetRequest;
import com.taobao.api.request.UserSellerGetRequest;
import com.taobao.api.response.UserGetResponse;
import com.taobao.api.response.UserSellerGetResponse;

public class HotShop {
    private static final Logger log = LoggerFactory.getLogger(HotShop.class);
    private static final String TAG = "HotShop";

    private Long cid;
    private long year;
    private long month;
    private List<HotShopInfo> hotShops = new ArrayList<HotShopInfo>();

    public List<HotShopInfo> getHotShops() {
        return this.hotShops;
    }

    public HotShop(Long cid, long year, long month) {
        this.cid = cid;
        this.year = year;
        this.month = month;
    }

    @JsonAutoDetect
    public static class HotShopInfo implements Comparable{
        @JsonProperty
        private String picPath; // 主图

        @JsonProperty
        private String nick; // 店铺

        @JsonProperty
        private Long sellerId; // shop id

        @JsonProperty
        private int sale; // 店铺销售

        @JsonProperty
        private int level = 0; // 店铺分数
        
        @JsonProperty
        private boolean isTM;

        public HotShopInfo(String picPath, String nick, Long sellerId, int sale, int level) {
            this.picPath = picPath;
            this.nick = nick;
            this.sellerId = sellerId;
            this.sale = sale;
            this.level = level;
            this.isTM = false;
        }
        
        public boolean getIsTM(){
            return this.isTM;
        }
        
        public HotShopInfo(){
        }
       
        public void setNick(String nick){
            this.nick = nick;
        }
        
        public void setSellerId(long sellerId){
            this.sellerId = sellerId;
        }
        
        public void setSale(int sale) {
            this.sale = sale;
        }

        public int getSale() {
            return this.sale;
        }
        
        public void setIsTM(boolean isTM){
            this.isTM = isTM;
        }
        public void setLevel(int level) {
            this.level = level;
        }

        public String getNick() {
            return this.nick;
        }
        
        public void setPicPath(String picPath){
            this.picPath = picPath;
        }

        @Override
        public int compareTo(Object o) {
            HotShopInfo hotShopInfo = (HotShopInfo)o;
            return hotShopInfo.sale - this.sale;
        }
    }

    public void execute() throws ClientException {
        long t1, t2;
        t1 = System.currentTimeMillis();
        List<ShopSimpleInfo> shopSimpleInfos = CatTopSaleItemSQL.findShopInfo(this.cid, this.year, this.month);
        Map<String, HotShopInfo> shopInfosMap = new HashMap<String, HotShopInfo>();
        t2 = System.currentTimeMillis();
        log.info("------------get item need the time is " + (t2 - t1));
        if (shopSimpleInfos != null) {
            for (ShopSimpleInfo shopSimpleInfo : shopSimpleInfos) {
                String nick = shopSimpleInfo.getWangWang();
                Long sellerId = shopSimpleInfo.getSellerId();
                int sale = shopSimpleInfo.getTradeNum();
                if(shopInfosMap.containsKey(nick)){
                    HotShopInfo tempShopInfo = shopInfosMap.get(nick);
                    tempShopInfo.setSale(tempShopInfo.getSale() + sale);
                }else{
                    HotShopInfo hotShopInfo = new HotShopInfo();
                    hotShopInfo.setNick(nick);
                    hotShopInfo.setSellerId(sellerId);
                    hotShopInfo.setSale(sale);
                    shopInfosMap.put(nick, hotShopInfo);
                }
                
            }
            t1 = System.currentTimeMillis();
            log.info("----get the item need the time is " + (t1 - t2));
        } else {
            log.error("no CatTopSaleItem");
        }
        List<String> hotShopNames = new ArrayList<String>(shopInfosMap.keySet());
        for(String hotShopName : hotShopNames){
            hotShops.add(shopInfosMap.get(hotShopName));
        }
        Collections.sort(hotShops);
    }

    public List<HotShopInfo> getShopInfo(int offset, int ps) throws ClientException {
        List<HotShopInfo> hotShopInfos = new ArrayList<HotShopInfo>();
        long t1,t2;
        t1 = System.currentTimeMillis();
        List<HotShopInfo> hotShopInfosAll = this.getHotShops();
        int size = hotShopInfosAll.size();
        for (int i = offset; i < offset + ps && i < size; ++i) {
            if (hotShopInfosAll.get(i) == null)
                continue;
            String wangwang = hotShopInfosAll.get(i).getNick();
            ShopLevelPicInfos levelPicInfos = LevelPicShopInfo.getShoLevelPic(wangwang);
            int level = levelPicInfos.getLevel();
            hotShopInfosAll.get(i).setLevel(level);
            String picPath = levelPicInfos.getPicPath();
            if(picPath.equals("http://logo.taobao.com/shop-logo")){
                picPath = "http://img03.taobaocdn.com/tps/i3/T1DbPGXldcXXXH8R6X-140-42.png";
            }
            hotShopInfosAll.get(i).setPicPath(picPath);
            if(wangwang.contains("旗舰店") || wangwang.contains("专买店") || wangwang.contains("专营店") || wangwang.contains("官方网店")){
                hotShopInfosAll.get(i).setIsTM(true);
            }
            
            hotShopInfos.add(hotShopInfosAll.get(i));
        }
        t2 = System.currentTimeMillis();
        log.info("--------------------get the info from remot server need the time is " + (t2 - t1));
        return hotShopInfos;
    }
}
