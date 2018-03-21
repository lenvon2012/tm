package actions.clouddata;

import java.util.List;

import com.taobao.api.domain.QueryRow;

public class ShopHourViewAndTrade {
	
	public String thedate;
	
	public String pv = "0";
	
	public String thehour;
	
	public String visit_platform;
	
	public String uv = "0";
	
	public String view_repeat_num;
	
	public String alipay_winner_num = "0";
	
	public String alipay_trade_num = "0";
	
	public String alipay_trade_amt = "0.0";
	
	public String alipay_auction_num = "0";

	public ShopHourViewAndTrade() {
		super();
	}

	public ShopHourViewAndTrade(Integer thehour) {
		super();
		this.thehour = String.valueOf(thehour);
	}

	public ShopHourViewAndTrade(QueryRow row) {
		if(row != null) {
			List<String> values = row.getValues();
			this.thedate = values.get(0);
			this.thehour = values.get(1);
			this.visit_platform = values.get(3);
			this.pv = values.get(4);
			this.uv = values.get(5);
			this.view_repeat_num = values.get(8);
			if(values.size() > 9) {
				this.alipay_trade_num = values.get(9);
				this.alipay_auction_num = values.get(10);
				this.alipay_trade_amt = values.get(11);
				this.alipay_winner_num = values.get(12);
			}
		}
	}
	
	
}
