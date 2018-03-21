package actions.clouddata;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.ciaosir.client.CommonUtils;

public class AreaViews implements Serializable{
    
    public static final String DEFAULT_VALUE = "0";
    
    public static final String DEFAULT_VALUE_PRRCENT = "0.00%";
    
    private String dataTime;
    
    // 浏览量  页面被查看的次数。用户多次打开或刷新同一个页面，该指标值累加。
    private String pv;

    // 访问量  页面的独立访问人数。所选时间段内，同一访客多次访问会进行去重计算。
    private String uv;
    
    // 销售额  成功完成支付宝支付的金额(元)
    private String alipayTradeAmt;
    
    // 销量  通过支付宝付款的商品总件数
    private String alipayAuctionNum;
    
    // 订单数  统计周期内，用户成功完成（支付宝）支付的子订单数（一笔订单，按照商品分拆为多个子订单）
    private String alipayTradeNum;
    
    // 成功拍下并完成支付宝付款的人数（这也是总的） 计算成功转换率使用
    private String alipayWinnerNum;
    
    // 成交转化率  成功拍下并完成支付宝付款的人数(alipay_winner_num) / uv
    private String tradeRate;
    
    // 入口数   pc + 无线入店关键词的总数量
    private String entranceNum;
    
    // 跳失率   bounceCount / pv
    private String bounceRate;
    
    // 收藏数
    private String itemCollectNum;
    
    // 购物车
    private String itemCartNum;
    
    // 搜索uv
    private String searchUv;
    
    // pc类目UV  pc 端的  u + v
    private String pcUv;
    
    // 展现量
    private String impression;
    
    public AreaViews(){   }
    
    public AreaViews(String dataTime){
        this.pv = DEFAULT_VALUE;
        this.uv = DEFAULT_VALUE;
        this.alipayTradeAmt = DEFAULT_VALUE;
        this.alipayAuctionNum = DEFAULT_VALUE;
        this.alipayTradeNum = DEFAULT_VALUE;
        this.alipayWinnerNum = DEFAULT_VALUE;
        this.tradeRate = DEFAULT_VALUE_PRRCENT;
        this.entranceNum = DEFAULT_VALUE;
        this.bounceRate = DEFAULT_VALUE_PRRCENT;
        this.itemCollectNum = DEFAULT_VALUE;
        this.itemCartNum = DEFAULT_VALUE;
        this.searchUv = DEFAULT_VALUE;
        this.pcUv = DEFAULT_VALUE;
        this.dataTime = dataTime;
    }
    
    // ipv, iuv, alipay_trade_num, alipay_auction_num,alipay_trade_amt, tradeRate
    public void addProp(String pv, String uv, String alipayTradeNum, String alipayAuctionNum, String alipayTradeAmt, String alipayWinnerNum){
        this.pv = pv;
        this.uv = uv;
        this.alipayTradeNum = alipayTradeNum;
        this.alipayAuctionNum = alipayAuctionNum;
        this.alipayTradeAmt = alipayTradeAmt;
        this.alipayWinnerNum = alipayWinnerNum;
        NumberFormat nf  = NumberFormat.getPercentInstance();
        nf.setMinimumFractionDigits(2);
        double tradeRateNum = Double.valueOf(alipayWinnerNum) / Double.valueOf(uv);
        this.tradeRate = tradeRateNum == 0D ? DEFAULT_VALUE_PRRCENT : nf.format(tradeRateNum);
    }
    
    public void addProp(AreaViews tmp) {
        this.pv = String.valueOf(Integer.valueOf(this.pv) + Integer.valueOf(tmp.pv));
        int uvSum =  Integer.valueOf(this.uv) + Integer.valueOf(tmp.uv);
        this.uv = String.valueOf(uvSum);
        this.alipayTradeNum = String.valueOf(Integer.valueOf(this.alipayTradeNum) + Integer.valueOf(tmp.alipayTradeNum));
        this.alipayAuctionNum = String.valueOf(Integer.valueOf(this.alipayAuctionNum) + Integer.valueOf(tmp.alipayAuctionNum));
        this.alipayTradeAmt = String.valueOf(Double.valueOf(this.alipayTradeAmt) + Double.valueOf(tmp.alipayTradeAmt));
        int alipayWinnerNumSum = Integer.valueOf(this.alipayWinnerNum) + Integer.valueOf(tmp.alipayWinnerNum);
        this.alipayWinnerNum = String.valueOf(alipayWinnerNumSum);
        NumberFormat nf  = NumberFormat.getPercentInstance();
        nf.setMinimumFractionDigits(2);
        double tradeRateNum = Double.valueOf(alipayWinnerNumSum) / Double.valueOf(uvSum);
        this.tradeRate = tradeRateNum == 0D ? DEFAULT_VALUE_PRRCENT : nf.format(tradeRateNum);
    }

    public void addSearchUv(String searchUvNew) {
        this.searchUv = String.valueOf(Integer.valueOf(this.searchUv) + Integer.valueOf(searchUvNew));
    }
    
    public void addPcUv(int pcUvNew) {
        this.pcUv = String.valueOf(Integer.valueOf(this.pcUv) + pcUvNew);
    }
    
    public void addProp(Integer pv, Integer uv, Integer alipayTradeNum, Integer alipayAuctionNum, Double alipayTradeAmt, Integer alipayWinnerNum){
        this.pv = String.valueOf(Integer.valueOf(this.pv) + pv);
        this.uv = String.valueOf(Integer.valueOf(this.uv) + uv);
        this.alipayTradeNum = String.valueOf(Integer.valueOf(this.alipayTradeNum) + alipayTradeNum);
        this.alipayAuctionNum = String.valueOf(Integer.valueOf(this.alipayAuctionNum) + alipayAuctionNum);;
        this.alipayTradeAmt = String.valueOf(Double.valueOf(this.alipayTradeAmt) + alipayTradeAmt);
        this.alipayWinnerNum = String.valueOf(Integer.valueOf(this.alipayWinnerNum) + alipayWinnerNum);
        NumberFormat nf  = NumberFormat.getPercentInstance();
        nf.setMinimumFractionDigits(2);
        double tradeRateNum = Double.valueOf(alipayWinnerNum) / Double.valueOf(uv);
        this.tradeRate = tradeRateNum == 0D ? DEFAULT_VALUE_PRRCENT : nf.format(tradeRateNum);
    }
    
    public void addItemCollectNum(HashMap<Long, Integer> itemCollectNumMap) {
        if(CommonUtils.isEmpty(itemCollectNumMap)){
            return;
        }
        Collection<Integer> values = itemCollectNumMap.values();
        if(CommonUtils.isEmpty(values)){
            return;
        }
        int itemCollectionSum = 0;
        Iterator<Integer> iterator = values.iterator();
        while(iterator.hasNext()){
            itemCollectionSum += iterator.next();
        }
        this.itemCollectNum = String.valueOf(itemCollectionSum);
    }
    
    public void addItemCartNum(HashMap<Long, Integer> skuIdCatNumMap) {
        if(CommonUtils.isEmpty(skuIdCatNumMap)){
            return;
        }
        Collection<Integer> values = skuIdCatNumMap.values();
        if(CommonUtils.isEmpty(values)){
            return;
        }
        int itemCartSum = 0;
        Iterator<Integer> iterator = values.iterator();
        while(iterator.hasNext()){
            itemCartSum += iterator.next();
        }
        this.itemCartNum = String.valueOf(itemCartSum);
    }
    
    public String getPv() {
        return pv;
    }

    public void setPv(String pv) {
        this.pv = pv;
    }

    public String getUv() {
        return uv;
    }

    public void setUv(String uv) {
        this.uv = uv;
    }

    public String getAlipayTradeAmt() {
        return alipayTradeAmt;
    }

    public void setAlipayTradeAmt(String alipayTradeAmt) {
        this.alipayTradeAmt = alipayTradeAmt;
    }

    public String getAlipayAuctionNum() {
        return alipayAuctionNum;
    }

    public void setAlipayAuctionNum(String alipayAuctionNum) {
        this.alipayAuctionNum = alipayAuctionNum;
    }

    public String getAlipayTradeNum() {
        return alipayTradeNum;
    }

    public String getAlipayWinnerNum() {
		return alipayWinnerNum;
	}

	public void setAlipayWinnerNum(String alipayWinnerNum) {
		this.alipayWinnerNum = alipayWinnerNum;
	}

	public void setAlipayTradeNum(String alipayTradeNum) {
        this.alipayTradeNum = alipayTradeNum;
    }

    public String getTradeRate() {
        return tradeRate;
    }

    public void setTradeRate(String tradeRate) {
        this.tradeRate = tradeRate;
    }

    public String getBounceRate() {
		return bounceRate;
	}

	public void setBounceRate(String bounceRate) {
		this.bounceRate = bounceRate;
	}

	public String getItemCollectNum() {
        return itemCollectNum;
    }

    public void setItemCollectNum(String itemCollectNum) {
        this.itemCollectNum = itemCollectNum;
    }

    public String getItemCartNum() {
        return itemCartNum;
    }

    public void setItemCartNum(String itemCartNum) {
        this.itemCartNum = itemCartNum;
    }

    public String getSearchUv() {
        return searchUv;
    }

    public void setSearchUv(String searchUv) {
        this.searchUv = searchUv;
    }

    public String getPcUv() {
        return pcUv;
    }

    public void setPcUv(String pcUv) {
        this.pcUv = pcUv;
    }

    public String getEntranceNum() {
        return entranceNum;
    }

    public void setEntranceNum(String entranceNum) {
        this.entranceNum = entranceNum;
    }

    public String getDataTime() {
        return dataTime;
    }

    public void setDataTime(String dataTime) {
        this.dataTime = dataTime;
    }

    public String getImpression() {
        return impression;
    }

    public void setImpression(String impression) {
        this.impression = impression;
    }

}
