
package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import job.apiget.TradeUpdateJob;
import models.item.ItemPlay;
import models.trade.TradeDisplay;
import models.updatetimestamp.updates.TradeUpdateTs;
import models.user.User;
import models.visit.LinezingRecord.LinezingWritter;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.NoTransaction;
import play.mvc.Controller;
import result.TMResult;
import secure.SimulateRequestUtil;
import actions.clouddata.InstantTransRateManager;
import actions.clouddata.InstantTransRateManager.ItemBaesSaleData;
import actions.clouddata.InstantTransRateManager.ItemUvBaseData;
import actions.clouddata.InstantTransRateManager.SaleBaseData;
import actions.clouddata.InstantTransRateManager.UvBaseData;

import com.ciaosir.client.utils.DateUtil;
import com.ciaosir.client.utils.JsonUtil;

import dao.trade.TradeDisplayDao;

public class InstantTransRate extends TMController {

    private static final Logger log = LoggerFactory.getLogger(InstantTransRate.class);

    public static final String TAG = "InstantTransRate";

    @JsonAutoDetect
    public static class PluginBaseInfo {
        public PluginBaseInfo() {
            this.tsMillis = System.currentTimeMillis();
            this.tsYMS = DateUtil.genYMS().format(new Date(tsMillis));
        }

        public PluginBaseInfo(User user) {
            this();
            userId = user.getId();
        }

        @JsonProperty
        Long userId;

        @JsonProperty
        int pcTransRate;

        @JsonProperty
        UvBaseData uvBase;

        @JsonProperty
        SaleBaseData saleBase;

        @JsonProperty
        long tsMillis;

        @JsonProperty
        String tsYMS;

        @JsonProperty
        List<ItemBaseSaleWebResult> itemSales = null;

        @JsonProperty
        List<RefBaseSale> refSales = null;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public int getPcTransRate() {
            return pcTransRate;
        }

        public void setPcTransRate(int pcTransRate) {
            this.pcTransRate = pcTransRate;
        }

        public UvBaseData getUvBase() {
            return uvBase;
        }

        public void setUvBase(UvBaseData uvBase) {
            this.uvBase = uvBase;
        }

        public SaleBaseData getSaleBase() {
            return saleBase;
        }

        public void setSaleBase(SaleBaseData saleBase) {
            this.saleBase = saleBase;
        }

        public long getTsMillis() {
            return tsMillis;
        }

        public void setTsMillis(long tsMillis) {
            this.tsMillis = tsMillis;
        }

        public String getTsYMS() {
            return tsYMS;
        }

        public void setTsYMS(String tsYMS) {
            this.tsYMS = tsYMS;
        }

        public List<ItemBaseSaleWebResult> getItemSales() {
            return itemSales;
        }

        public void setItemSales(List<ItemBaseSaleWebResult> itemSales) {
            this.itemSales = itemSales;
        }

        public List<RefBaseSale> getRefSales() {
            return refSales;
        }

        public void setRefSales(List<RefBaseSale> refSales) {
            this.refSales = refSales;
        }

    }

    @JsonAutoDetect
    public static class RefBaseSale {
        @JsonProperty
        String refType;

        @JsonProperty
        int transRate;

        @JsonProperty
        int sale;

        @JsonProperty
        int uv;

        @JsonProperty
        int pv;

        private RefBaseSale(String refType, int transRate, int sale, int uv, int pv) {
            super();
            this.refType = refType;
            this.transRate = transRate;
            this.sale = sale;
            this.uv = uv;
            this.pv = pv;
        }

    }

    @JsonAutoDetect
    public static class ItemBaseSaleWebResult {
        @JsonProperty
        long numIid;

        @JsonProperty
        String picPath;

        @JsonProperty
        String title;

        @JsonProperty
        int transRate;

        @JsonProperty
        int sale;

        @JsonProperty
        int uv;

        @JsonProperty
        int pv;

        @JsonProperty
        UvBaseData uvData;

        @JsonProperty
        SaleBaseData saleData;

        public ItemBaseSaleWebResult(long numIid) {
            super();
            this.numIid = numIid;
        }

        private ItemBaseSaleWebResult(long numIid, String picPath, String title, int transRate, int sale, int uv, int pv) {
            super();
            this.numIid = numIid;
            this.picPath = picPath;
            this.title = title;
            this.transRate = transRate;
            this.sale = sale;
            this.uv = uv;
            this.pv = pv;
        }

        public void buildData(ItemPlay localItem, ItemUvBaseData itemUvBase, ItemBaesSaleData itemBaseSale) {
            // TODO Auto-generated method stub
        }

        public long getNumIid() {
            return numIid;
        }

        public void setNumIid(long numIid) {
            this.numIid = numIid;
        }

        public String getPicPath() {
            return picPath;
        }

        public void setPicPath(String picPath) {
            this.picPath = picPath;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getTransRate() {
            return transRate;
        }

        public void setTransRate(int transRate) {
            this.transRate = transRate;
        }

        public int getSale() {
            return sale;
        }

        public void setSale(int sale) {
            this.sale = sale;
        }

        public int getUv() {
            return uv;
        }

        public void setUv(int uv) {
            this.uv = uv;
        }

        public int getPv() {
            return pv;
        }

        public void setPv(int pv) {
            this.pv = pv;
        }

        public UvBaseData getUvData() {
            return uvData;
        }

        public void setUvData(UvBaseData uvData) {
            this.uvData = uvData;
        }

        public SaleBaseData getSaleData() {
            return saleData;
        }

        public void setSaleData(SaleBaseData saleData) {
            this.saleData = saleData;
        }

        public void updateWrapper(ItemBaesSaleData itemBaseSale) {
            if (itemBaseSale == null) {
                return;
            }
            this.setSaleData(new SaleBaseData(itemBaseSale));
        }

        public void updateWrapper(ItemUvBaseData itemUvBase) {
            if (itemUvBase == null) {
                this.uv = 0;
                this.pv = 0;
                this.transRate = 0;
                return;
            }

            this.uvData = new UvBaseData(itemUvBase);
            this.uv = itemUvBase.getUv();
            this.pv = itemUvBase.getPv();
        }

        public void updateWrapper(ItemPlay localItem) {
            if (localItem == null) {
                return;
            }

            this.picPath = localItem.getPicURL();
            this.title = localItem.getTitle();
        }

        public void buildTransRate() {
            if (this.uv <= 0) {
                this.transRate = 0;
                return;
            }

            this.transRate = this.saleData.getSale() * 10000 / this.uv;
        }

    }

    @NoTransaction
    /**
     * http://localhost:9999/InstantTransRate/userTs
     */
    public static void userTs() {
        PluginBaseInfo base = new PluginBaseInfo();
//        base.pcTransRate = 100; // 1%
//        base.tsMillis = System.currentTimeMillis();
//        base.tsYMS = DateUtil.formDateForLog(base.tsMillis);
//        base.itemSales = new ArrayList<ItemBaseSaleWebResult>();
//        base.itemSales.add(new ItemBaseSaleWebResult(37532643253L,
//                "http://img03.taobaocdn.com/bao/uploaded/i3/T1zgqRFBBbXXXXXXXX_!!0-item_pic.jpg",
//                "时尚女包小包 链条包2014新款欧美复古包单肩斜挎包小流苏菱格包", 100, 100, 10000, 20000));
//        base.refSales = new ArrayList<RefBaseSale>();
//        base.refSales.add(new RefBaseSale("淘宝搜索", 100, 100, 10000, 20000));

        User user = TMController.getUser();
        base = InstantTransRateManager.get().buildUserBase(user);

        TMResult<PluginBaseInfo> res = new TMResult<PluginBaseInfo>(base);

        renderJSON(JsonUtil.getJson(res));
    }

    @NoTransaction
    /**
     * 这里data一定要post上来
     * localhost:9999/InstantTransRate/uploadLinzingVisitLog?
     * data=[{量子的数据},{量子的数据}]
     * @param data
     */
    public static void uploadLinzingVisitLog(String data) {
        log.info("[data.length]" + StringUtils.length(data));
        TMResult<Integer> res = null;
        if (data == null) {
            res = new TMResult<Integer>("No data.....");
            renderJSON(JsonUtil.getJson(res));
        }

        User user = getUser();

        try {

            JSONArray arr = new JSONArray(data);
            res = new TMResult<Integer>(arr.length());
            LinezingWritter.addQueue(user, arr);

        } catch (JSONException e) {
            log.warn(e.getMessage(), e);
            res = new TMResult<Integer>("format not valid...");
        }

        renderJSON(JsonUtil.getJson(res));
    }

    public static void forcrTradeSync() {
        User user = TMController.getUser();
        TradeUpdateTs ts = TradeUpdateTs.findByUser(user);
        if (ts != null) {
            TradeUpdateTs.jdbcDelete(user.getId());
        }
        new TradeUpdateJob(user.getId(), System.currentTimeMillis()).doJob();
    }

    public static void doBind() {
        User user = getUser();
        List<TradeDisplay> doBind = TradeDisplayDao.doBind(user);
        sendOrderLog(user.getId(), "订单详情", SimulateRequestUtil.getTradeTid(doBind));
    }
}
