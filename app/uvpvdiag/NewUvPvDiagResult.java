package uvpvdiag;

import java.io.Serializable;
import java.util.List;

import models.item.ItemPlay;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;

import com.ciaosir.client.CommonUtils;
import com.taobao.api.domain.QueryRow;

@JsonAutoDetect
public class NewUvPvDiagResult implements Serializable {

	private static final Logger log = LoggerFactory.getLogger(NewUvPvDiagResult.class);

	public static final String TAG = "NewUvPvDiagResult";

	private static final long serialVersionUID = 1L;
	
	public static final int DEFAULT_VALUE = 0;
	
	@JsonProperty
	long numIid = 0L;

	@JsonProperty
	long cid = 0L;

	@JsonProperty
	double price = 0d;

	@JsonProperty
	String picPath = StringUtils.EMPTY;
	
	@JsonProperty
	public String title = StringUtils.EMPTY;;
	
	/*
	 * 浏览量
	 */
	@JsonProperty
	public int pv;
	
	/*
	 * 访客量
	 */
	@JsonProperty
	public int uv;
	
	/*
	 * 成交用户
	 */
	@JsonProperty
	public int alipay_winner_num;
	
	/*
	 * 成交笔数
	 */
	@JsonProperty
	public int alipay_trade_num;
	
	/*
	 * 销量
	 */
	@JsonProperty
	public int alipay_auction_num;
	
	/*
	 * 成交金额
	 */
	@JsonProperty
	public double alipay_trade_amt;
	
	/*
	 * 收藏数
	 */
	public int itemCollectNum;
	
	/*
	 * 加购数
	 */
	public int itemCartNum;
	
	public NewUvPvDiagResult() {
		
	}

	public NewUvPvDiagResult(ItemPlay item) {
		super();
		this.numIid = item.getNumIid();
		this.cid = item.getCid();
		this.price = item.getPrice();
		this.picPath = item.getPicURL();
		this.title = item.getTitle();
		this.pv = DEFAULT_VALUE;
		this.uv = DEFAULT_VALUE;
		this.alipay_winner_num = DEFAULT_VALUE;
		this.alipay_trade_num = DEFAULT_VALUE;
		this.alipay_auction_num = DEFAULT_VALUE;
		this.alipay_trade_amt = DEFAULT_VALUE;
		this.itemCollectNum = DEFAULT_VALUE;
		this.itemCartNum = DEFAULT_VALUE;
	}

	public void addProps(QueryRow row) {
		if(row == null) {
			return;
		}
		List<String> value = row.getValues();
		if(value.size() > 5) {
			this.pv += Integer.valueOf(value.get(4));
			this.uv += Integer.valueOf(value.get(5));
			this.alipay_trade_num += Integer.valueOf(value.get(6));
			this.alipay_auction_num += Integer.valueOf(value.get(7));
			this.alipay_trade_amt += Double.valueOf(value.get(8));
			this.alipay_winner_num += Integer.valueOf(value.get(9));
		} else {
			this.pv += Integer.valueOf(value.get(3));
			this.uv += Integer.valueOf(value.get(4));
		}
	}
	
	public void addCollect(int count) {
		this.itemCollectNum += count;
	}
	
	public void addCart(int count) {
		this.itemCartNum += count;
	}

	public long getNumIid() {
		return numIid;
	}

	public void setNumIid(long numIid) {
		this.numIid = numIid;
	}

	public long getCid() {
		return cid;
	}

	public void setCid(long cid) {
		this.cid = cid;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
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

	public int getAlipay_winner_num() {
		return alipay_winner_num;
	}

	public void setAlipay_winner_num(int alipay_winner_num) {
		this.alipay_winner_num = alipay_winner_num;
	}

	public int getAlipay_trade_num() {
		return alipay_trade_num;
	}

	public void setAlipay_trade_num(int alipay_trade_num) {
		this.alipay_trade_num = alipay_trade_num;
	}

	public int getAlipay_auction_num() {
		return alipay_auction_num;
	}

	public void setAlipay_auction_num(int alipay_auction_num) {
		this.alipay_auction_num = alipay_auction_num;
	}

	public double getAlipay_trade_amt() {
		return alipay_trade_amt;
	}

	public void setAlipay_trade_amt(double alipay_trade_amt) {
		this.alipay_trade_amt = alipay_trade_amt;
	}

	public int getItemCollectNum() {
		return itemCollectNum;
	}

	public void setItemCollectNum(int itemCollectNum) {
		this.itemCollectNum = itemCollectNum;
	}

	public int getItemCartNum() {
		return itemCartNum;
	}

	public void setItemCartNum(int itemCartNum) {
		this.itemCartNum = itemCartNum;
	}

}
