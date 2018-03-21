package models.mysql.word;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonAutoDetect
public class DiagWordInfo {
	@JsonProperty
    String word = StringUtils.EMPTY;
	
	// 平均搜索排名
	@JsonProperty
	int searchRank;
	
	// 展现量
	@JsonProperty
	int impression = 0;
	
	// 点击量
	@JsonProperty
	int aclick = 0;
	
	// 点击率
	@JsonProperty
	double ctr;
	
	// 入店次数（浏览量）
	@JsonProperty
	int pv;
	
	// 访客数(UV)
	@JsonProperty
	int uv;
	
	// 访问深度
	@JsonProperty
	double deep;
	
	// 跳失率
	@JsonProperty
	double bounceRate;
	
	// 成交用户数
	@JsonProperty
	int tradeUserCount;
	
	// 成交件数
	@JsonProperty
	int tradeCount;
	
	// 成交金额
	@JsonProperty
	int tradeAmount;
	
	// 成交转化率
	@JsonProperty
	double tranrate;
	
	public DiagWordInfo(String word, int searchRank, int impression, int aclick, 
			int pv, int uv, double deep, double bounceRate, int tradeUserCount,
			int tradeCount, int tradeAmount, double tranrate) {
		super();
		this.word = word;
		this.searchRank = searchRank;
		this.impression = impression;
		this.aclick = aclick;
		this.ctr = (aclick * 1.0) / impression;
		this.pv = pv;
		this.uv = uv;
		this.deep = deep;
		this.bounceRate = bounceRate;
		this.tradeUserCount = tradeUserCount;
		this.tradeCount = tradeCount;
		this.tradeAmount = tradeAmount;
		this.tranrate = tranrate;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public int getSearchRank() {
		return searchRank;
	}

	public void setSearchRank(int searchRank) {
		this.searchRank = searchRank;
	}

	public int getImpression() {
		return impression;
	}

	public void setImpression(int impression) {
		this.impression = impression;
	}

	public int getAclick() {
		return aclick;
	}

	public void setAclick(int aclick) {
		this.aclick = aclick;
	}

	public double getCtr() {
		return ctr;
	}

	public void setCtr(double ctr) {
		this.ctr = ctr;
	}

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

	public double getDeep() {
		return deep;
	}

	public void setDeep(double deep) {
		this.deep = deep;
	}

	public double getBounceRate() {
		return bounceRate;
	}

	public void setBounceRate(double bounceRate) {
		this.bounceRate = bounceRate;
	}

	public int getTradeUserCount() {
		return tradeUserCount;
	}

	public void setTradeUserCount(int tradeUserCount) {
		this.tradeUserCount = tradeUserCount;
	}

	public int getTradeCount() {
		return tradeCount;
	}

	public void setTradeCount(int tradeCount) {
		this.tradeCount = tradeCount;
	}

	public int getTradeAmount() {
		return tradeAmount;
	}

	public void setTradeAmount(int tradeAmount) {
		this.tradeAmount = tradeAmount;
	}

	public double getTranrate() {
		return tranrate;
	}

	public void setTranrate(double tranrate) {
		this.tranrate = tranrate;
	}
	
	
}