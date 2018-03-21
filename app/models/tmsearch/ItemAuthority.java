package models.tmsearch;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonProperty;


public class ItemAuthority implements Serializable {
    private static final long serialVersionUID = -1L;
    
    @JsonProperty(value = "numIid")
    protected Long id;
    @JsonProperty(value = "itemTitle")
    protected String fullTitle;
    @JsonProperty(value = "saleCount")
    protected int tradeNum;
    @JsonProperty(value = "monthSaleCount")
    protected int periodSoldQuantity;
    @JsonProperty(value = "itemPrice")
    protected int price;
    @JsonProperty(value = "itemImgPath")
    protected String picPath;
    @JsonProperty(value = "diff")
    protected int diff;
	
	public ItemAuthority(){
	    
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

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    public int getDiff() {
        return diff;
    }

    public void setDiff(int diff) {
        this.diff = diff;
    }
	
	
	
}
