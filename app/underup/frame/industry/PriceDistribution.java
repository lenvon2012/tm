package underup.frame.industry;

import java.util.*;

import models.item.ItemCatPlay;
//import models.mysql.fengxiao.CatTopSaleItem;
//mport models.mysql.fengxiao.CatTopSaleItem.PriceTradenum;
import underup.frame.industry.CatTopSaleItemSQL;
import underup.frame.industry.CatTopSaleItemSQL.PriceTradenum;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;

public class PriceDistribution {
    private static final Logger log = LoggerFactory.getLogger(PriceDistribution.class);

    private static final String TAG = "PriceDistribution";

    private static final String[] priceRangeStr = { "0元-10元", "10元-20元", "20元-50元", "50元-100元", "100元-150元",
            "150元-200元", "200元-300元", "300元-400元", "400元-500元", "500元-600元", "600元-700元", "700元-800元", "800元-1000元",
            "1000元-1500元", "1500元-2000元", "2000元-3000元", "3000元-4000元", "4000元-5000元", "5000元-6000元", "6000元-8000元",
            "8000元-10000元", "10000元-15000元", "15000元-20000元", "20000元-元", };

    private static final double[] priceRangeDou = { 10, 20, 50, 100, 150, 200, 300, 400, 500, 600, 700, 800, 1000,
            1500, 2000, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 20000 };

    private int getPriceIndex(double price) {
        int firstIndex = 0;
        int lastIndex = 22;
        while (firstIndex <= lastIndex) {
            int midIndex = (firstIndex + lastIndex) / 2;
            if (price > priceRangeDou[midIndex]) {
                firstIndex = midIndex + 1;
            } else if (price < priceRangeDou[midIndex]) {
                lastIndex = midIndex - 1;
            } else
                return midIndex;
        }
        if (lastIndex == -1)
            lastIndex = 0;
        if (price > priceRangeDou[lastIndex])
            return lastIndex + 1;
        else
            return lastIndex;
    }

    public PriceDistribution(Long cid, long year, long month) {
        this.cid = cid;
        this.year = year;
        this.month = month;
    }

    @JsonAutoDetect
    public static class PriceRange {
        @JsonProperty
        private String priceRange;

        @JsonProperty
        private int totleTradeNum;

        @JsonProperty
        private double totlePrice;

        public PriceRange(String priceRange) {
            this.priceRange = priceRange;
            this.totlePrice = 0.0;
            this.totleTradeNum = 0;
        }

        public PriceRange(String priceRange, int totleTradeNum, double totlePrice) {
            this.priceRange = priceRange;
            this.totleTradeNum = totleTradeNum;
            this.totlePrice = totlePrice;
        }

        public String getPriceRange() {
            return priceRange;
        }

        public void setPriceRange(String priceRange) {
            this.priceRange = priceRange;
        }

        public int getTradeNum() {
            return totleTradeNum;
        }

        public void setTradeNum(int tradeNum) {
            this.totleTradeNum = tradeNum;
        }

        public double getTotlePrice() {
            return this.totlePrice;
        }

        public void setTotlePrice(double totlePrice) {
            this.totlePrice = totlePrice;
        }
    }

    private Long cid;
    private long year;
    private long month;
    private Map<String, PriceRange> priceRangeMap = new LinkedHashMap<String, PriceRange>();

    private void initPriceRangeMap() {
        for (final String priceRange : priceRangeStr) {
            PriceRange temp = new PriceRange(priceRange);
            this.priceRangeMap.put(priceRange, temp);
        }
    }

    public void call(List<PriceTradenum> topItems) {
        if (topItems == null)
            return;
        for (PriceTradenum topItem : topItems) {
            double itemPrice = topItem.getPrice();
            int tradeNum = topItem.getTradeNum();
            String priceRange = priceRangeStr[getPriceIndex(itemPrice)];
            if (priceRangeMap.containsKey(priceRange)) {
                PriceRange temp = priceRangeMap.get(priceRange);
                temp.setTotlePrice(temp.getTotlePrice() + itemPrice);
                temp.setTradeNum(temp.getTradeNum() + tradeNum);
            } else {
                PriceRange temp = new PriceRange(priceRange, tradeNum, itemPrice);
                priceRangeMap.put(priceRange, temp);
            }
        }
    }

    public void exec() {
        initPriceRangeMap();
        ItemCatPlay itemCatPlay = ItemCatPlay.findByCid(this.cid);
        if (!itemCatPlay.isParent) {
            List<Long> childrenCids = new ArrayList<Long>();
            childrenCids.add(this.cid);
            List<PriceTradenum> topItems = CatTopSaleItemSQL.getPriceTradenum(childrenCids, this.year, this.month);
            call(topItems);
        } else {
            List<Long> childrenCids = ItemsCatArrange.getChildrenCids(this.cid, this.year, this.month);
            if (childrenCids == null)
                return;
            List<PriceTradenum> topItems = CatTopSaleItemSQL.getPriceTradenum(childrenCids, year, month);
            call(topItems);
        }
    }

    public Map<String, PriceRange> getPriceRange() {
        return priceRangeMap;
    }

}
