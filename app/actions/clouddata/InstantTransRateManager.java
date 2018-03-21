
package actions.clouddata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.item.ItemPlay;
import models.order.OrderDisplay;
import models.trade.TradeDisplay;
import models.user.User;
import models.visit.LinezingRecord;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import secure.SimulateRequestUtil;
import controllers.InstantTransRate.ItemBaseSaleWebResult;
import controllers.InstantTransRate.PluginBaseInfo;
import controllers.TMController;
import dao.item.ItemDao;
import dao.trade.OrderDisplayDao;
import dao.trade.TradeDisplayDao;

public class InstantTransRateManager {

    private static final Logger log = LoggerFactory.getLogger(InstantTransRateManager.class);

    public static final String TAG = "InstantTransRateManager";

    public static InstantTransRateManager _instance = new InstantTransRateManager();

    public InstantTransRateManager() {
    }

    public static InstantTransRateManager get() {
        return _instance;
    }

    public void triggerNewLizingRecords(User user) {
        resetShopSale(user);
    }

    public void triggerNewTrades(User user) {
        resetShopTodayUv(user);
    }

    static String uvKeyTag = "_instant_uv_day_key";

    static String saleKeyTag = "_instant_sale_day_key";

    @JsonAutoDetect
    public static class UvBaseData {
        @JsonProperty
        int pv;

        @JsonProperty
        int uv;

        public int getPv() {
            return pv;
        }

        public void setPv(int pv) {
            this.pv = pv;
        }

        public int getUv() {
            return uv;
        }

        public void setUv(int uv) {
            this.uv = uv;
        }

        public UvBaseData(int pv, int uv) {
            super();
            this.pv = pv;
            this.uv = uv;
        }

        public UvBaseData() {

        }

        public UvBaseData(UvBaseData data) {
            super();
            this.pv = data.pv;
            this.uv = data.uv;
        }

    }

    public static class ItemUvBaseData extends UvBaseData {

        private ItemUvBaseData(long numIid) {
            super();
            this.numIid = numIid;
        }

        public ItemUvBaseData() {
            // TODO Auto-generated constructor stub
        }

        @JsonProperty
        long numIid;

        public long getNumIid() {
            return numIid;
        }

        public void setNumIid(long numIid) {
            this.numIid = numIid;
        }

    }

    @JsonAutoDetect
    public static class SaleBaseData {
        @JsonProperty
        int amount;

        @JsonProperty
        int num;

        @JsonProperty
        int sale;

        public SaleBaseData() {
            super();
        }

        public SaleBaseData(SaleBaseData todayTrade) {
            this.amount = todayTrade.amount;
            this.num = todayTrade.num;
            this.sale = todayTrade.sale;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public int getNum() {
            return num;
        }

        public void setNum(int num) {
            this.num = num;
        }

        public int getSale() {
            return sale;
        }

        public void setSale(int sale) {
            this.sale = sale;
        }

    }

    @JsonAutoDetect
    public static class ItemBaesSaleData extends SaleBaseData {

        public ItemBaesSaleData() {
            super();
        }

        public ItemBaesSaleData(ShopSaleBaseData todayTrade) {
            super(todayTrade);
        }

        private ItemBaesSaleData(ShopSaleBaseData todayTrade, long numIid) {
            super(todayTrade);
            this.numIid = numIid;
        }

        public ItemBaesSaleData(Long numiid2) {
            this.numIid = numiid2;
        }

        @JsonProperty
        long numIid;

        public long getNumIid() {
            return numIid;
        }

        public void setNumIid(long numIid) {
            this.numIid = numIid;
        }

    }

    @JsonAutoDetect
    public static class ShopSaleBaseData extends SaleBaseData {

        private ShopSaleBaseData() {
            super();
        }

        @JsonProperty
        Long userId;

        @JsonProperty
        Map<Long, ItemBaesSaleData> map = new HashMap<Long, ItemBaesSaleData>();

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Map<Long, ItemBaesSaleData> getMap() {
            return map;
        }

        public void setMap(Map<Long, ItemBaesSaleData> map) {
            this.map = map;
        }

    }

    @JsonAutoDetect
    public static class ShopUvBaseData extends UvBaseData implements Serializable {

        private ShopUvBaseData() {
            super();
        }

        private static final long serialVersionUID = -1679458253208555786L;

        @JsonProperty
        Long userId;

        @JsonProperty
        Map<Long, ItemUvBaseData> list;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Map<Long, ItemUvBaseData> getMap() {
            return list;
        }

        public void setMap(Map<Long, ItemUvBaseData> list) {
            this.list = list;
        }

    }

    private String genUvKey(User user) {
        return uvKeyTag + user.getId();
    }

    private String genSaleKey(User user) {
        return saleKeyTag + user.getId();
    }

    public ShopUvBaseData getTodayUv(User user, boolean mustRefresh) {
        String key = genUvKey(user);
        ShopUvBaseData shopUv = (ShopUvBaseData) Cache.get(key);
        if (shopUv != null && !mustRefresh) {
            return shopUv;
        }

        shopUv = resetShopTodayUv(user);
        return shopUv;
    }

    private ShopUvBaseData resetShopTodayUv(User user) {
        int pv = LinezingRecord.findTodayRecords(user);
        int uv = LinezingRecord.findTodayUv(user);
        Map<Long, Integer> itemPvMap = LinezingRecord.findTodayItemPv(user);
        Map<Long, Integer> itemUvMap = LinezingRecord.findTodayItemUv(user);

        ShopUvBaseData base = new ShopUvBaseData();
        base.setUserId(user.getId());
        base.setPv(pv);
        base.setUv(uv);
//        List<ItemUvBaseData> list = new ArrayList<ItemUvBaseData>();
        Map<Long, ItemUvBaseData> map = new HashMap<Long, ItemUvBaseData>();
        base.setMap(map);

        Set<Long> keySet = itemUvMap.keySet();
        for (Long numIid : keySet) {
            int itemPv = itemPvMap.get(numIid);
            int itemUv = itemPvMap.get(numIid);
            ItemUvBaseData itemBase = new ItemUvBaseData();
            itemBase.setNumIid(numIid);
            itemBase.setPv(itemPv);
            itemBase.setUv(itemUv);

            map.put(numIid, itemBase);
        }

        String key = genUvKey(user);
        Cache.set(key, base, "15min");
        return base;

    }

    public ShopSaleBaseData getTodayTrades(User user, boolean mustRefresh) {
        String key = genSaleKey(user);
        ShopSaleBaseData data = (ShopSaleBaseData) Cache.get(key);
        if (data != null && !mustRefresh) {
            return data;
        }

        data = resetShopSale(user);
        return data;
    }

    private ShopSaleBaseData resetShopSale(User user) {
        List<TradeDisplay> list = TradeDisplayDao.findTodayCreated(user);
        // 御城河日志
        SimulateRequestUtil.sendOrderLog("订单详情", SimulateRequestUtil.getTradeTid(list));
        ShopSaleBaseData data = new ShopSaleBaseData();
        data.setUserId(user.getId());
        /*
         * 可能后续需要过滤掉手机订单
         */
        for (TradeDisplay tradeDisplay : list) {
            data.amount += tradeDisplay.getPayment() * 100d;
            data.num += tradeDisplay.getNum();
            data.sale++;
        }

        for (TradeDisplay model : list) {
            List<OrderDisplay> orders = OrderDisplayDao.findByUserIdTid(user.getId(), model.getTid());
            for (OrderDisplay order : orders) {
                Long numiid = order.getNumIid();
                ItemBaesSaleData itemData = data.map.get(numiid);
                if (itemData == null) {
                    itemData = new ItemBaesSaleData(numiid);
                    data.map.put(numiid, itemData);
                }

                itemData.amount += order.getPayment() * 100d;
                itemData.num += order.getNum();
                itemData.sale++;
            }
        }

        return data;
    }

    public PluginBaseInfo buildUserBase(User user) {
        PluginBaseInfo pluginBase = new PluginBaseInfo(user);

        ShopUvBaseData uvData = getTodayUv(user, false);
        ShopSaleBaseData todayTrade = getTodayTrades(user, false);

        pluginBase.setUvBase(new UvBaseData(uvData));
        pluginBase.setSaleBase(new SaleBaseData(todayTrade));

        List<ItemBaseSaleWebResult> resList = new ArrayList<ItemBaseSaleWebResult>();
        pluginBase.setItemSales(resList);

        Map<Long, ItemBaesSaleData> map = todayTrade.getMap();
        Collection<ItemBaesSaleData> values = map.values();

        Set<Long> numIids = map.keySet();
//        Set<Long> allNumIids = new HashSet<Long>();
        List<ItemPlay> items = ItemDao.findByNumIids(user.getId(), numIids);

        Map<Long, ItemPlay> mapItems = new HashMap<Long, ItemPlay>();
        for (ItemPlay itemPlay : items) {
            mapItems.put(itemPlay.getNumIid(), itemPlay);
        }

        for (ItemBaesSaleData bean : values) {
            long numIid = bean.getNumIid();

            ItemPlay item = mapItems.get(numIid);
            if (item == null) {
                log.warn("fails for no item base data:" + bean);
                continue;
            }
            ItemBaesSaleData itemBaseSale = todayTrade.getMap().get(numIid);
            ItemUvBaseData itemUvBase = uvData.getMap().get(numIid);
            ItemPlay localItem = mapItems.get(numIid);

            ItemBaseSaleWebResult res = buildData(localItem, itemUvBase, itemBaseSale);
            resList.add(res);
        }

        return pluginBase;
    }

    private ItemBaseSaleWebResult buildData(ItemPlay localItem, ItemUvBaseData itemUvBase, ItemBaesSaleData itemBaseSale) {

        ItemBaseSaleWebResult oneResult = new ItemBaseSaleWebResult(localItem.getNumIid());

        oneResult.updateWrapper(localItem);
        oneResult.updateWrapper(itemUvBase);
        oneResult.updateWrapper(itemBaseSale);
        oneResult.buildTransRate();

        return oneResult;
    }

}
