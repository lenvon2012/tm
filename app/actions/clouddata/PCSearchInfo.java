package actions.clouddata;

import java.util.List;

import com.taobao.api.domain.QueryRow;

public class PCSearchInfo {
	public String theDate;

	public String sellerId;

	public String numIid;

	public String uv;

	public String pv;

	public String srcId;

	public String srcLevel;

	public String src_parent_id; 

	public String alipay_trade_num;

	public String alipay_auction_num;

	public String gmv_winner_num;

	public String gmv_trade_amt;

	public String gmv_auction_num;

	public String gmv_trade_num;

	public String alipay_winner_num;

	public String alipay_trade_amt;

	public PCSearchInfo() {
		super();
	}

	public PCSearchInfo(QueryRow row) {
		if(row != null) {
			List<String> values = row.getValues();
			this.theDate = values.get(0);
			this.sellerId = values.get(1);
			this.numIid = values.get(2);
			this.srcId = values.get(3);
			this.srcLevel = values.get(4);
			this.src_parent_id = values.get(5);
			this.uv = values.get(6);
			this.pv = values.get(7);
			this.alipay_trade_num = values.get(8);
			this.alipay_auction_num = values.get(9);
			this.gmv_winner_num = values.get(10);
			this.gmv_trade_amt = values.get(11);
			this.gmv_auction_num = values.get(12);
			this.gmv_trade_num = values.get(13);
			this.alipay_winner_num = values.get(14);
			this.alipay_trade_amt = values.get(1);
		}
	}
}
