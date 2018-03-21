package uvpvdiag;

import java.io.Serializable;
import java.util.List;

import models.item.ItemPlay;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.api.domain.QueryRow;

@JsonAutoDetect
public class UvPvDiagResult implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(UvPvDiagResult.class);

    public static final String TAG = "UvPvDiagResult";

    private static final long serialVersionUID = 1L;

    @JsonProperty
    long numIid = 0L;

    @JsonProperty
    long cid = 0L;

    @JsonProperty
    double price = 0d;

    @JsonProperty
    String picPath = StringUtils.EMPTY;
    
    @JsonProperty
    public String word;
	
    @JsonProperty
	public int pv;
	
    @JsonProperty
	public int click = 0;
	
    @JsonProperty
	public int uv;
	
    @JsonProperty
	public int alipay_winner_num;
	
    @JsonProperty
	public int alipay_trade_num;
	
    @JsonProperty
	public double alipay_trade_amt;
	
	@JsonProperty
	public int alipay_auction_num;
	
	@JsonProperty
	public String thedate;   
    
    @JsonProperty
    String title = StringUtils.EMPTY;

    @JsonProperty
    long impression = 0L;
    
    public UvPvDiagResult(String title) {
        super();
        this.title = title;
    }

    public UvPvDiagResult() {
		
	}
    
    public UvPvDiagResult(long numIid, long cid, double price, String picPath,
			int pv, int uv, String title) {
		super();
		this.numIid = numIid;
		this.cid = cid;
		this.price = price;
		this.picPath = picPath;
		this.pv = pv;
		this.uv = uv;
		this.title = title;
	}

	public UvPvDiagResult(long numIid, long cid, double price, String picPath,
			String word, int pv, int click, int uv, int alipay_winner_num,
			int alipay_trade_num, int alipay_trade_amt, int alipay_auction_num,
			String thedate, String title) {
		super();
		this.numIid = numIid;
		this.cid = cid;
		this.price = price;
		this.picPath = picPath;
		this.word = word;
		this.pv = pv;
		this.click = click;
		this.uv = uv;
		this.alipay_winner_num = alipay_winner_num;
		this.alipay_trade_num = alipay_trade_num;
		this.alipay_trade_amt = alipay_trade_amt;
		this.alipay_auction_num = alipay_auction_num;
		this.thedate = thedate;
		this.title = title;
	}

	public UvPvDiagResult(ItemPlay item) {
		super();
		this.numIid = item.getNumIid();
		this.cid = item.getCid();
		this.price = item.getPrice();
		this.picPath = item.getPicURL();
		this.title = item.getTitle();
	}
	
	public int getPv() {
        return pv;
    }

    public void setPv(int pv) {
        this.pv = pv;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    public long getNumIid() {
        return numIid;
    }

    public void setNumIid(long numIid) {
        this.numIid = numIid;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;

    }

    public long getCid() {
        return cid;
    }

    public void setCid(long cid) {
        this.cid = cid;
    }

    public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public int getClick() {
		return click;
	}

	public void setClick(int click) {
		this.click = click;
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

	public double getAlipay_trade_amt() {
		return alipay_trade_amt;
	}

	public void setAlipay_trade_amt(double alipay_trade_amt) {
		this.alipay_trade_amt = alipay_trade_amt;
	}

	public int getAlipay_auction_num() {
		return alipay_auction_num;
	}

	public void setAlipay_auction_num(int alipay_auction_num) {
		this.alipay_auction_num = alipay_auction_num;
	}

	public String getThedate() {
		return thedate;
	}

	public void setThedate(String thedate) {
		this.thedate = thedate;
	}

	public long getImpression() {
		return impression;
	}

	public void setImpression(long impression) {
		this.impression = impression;
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
    
}
