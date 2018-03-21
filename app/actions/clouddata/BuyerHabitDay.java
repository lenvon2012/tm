package actions.clouddata;

import java.util.List;

import com.taobao.api.domain.QueryRow;

// 按天统计，店铺买家购买习惯分析日表
public class BuyerHabitDay {
	
	// 日期
	public String thedate;
	
	// 卖家ID
	public String userId;
	
	// 最近30天，有购买的客户中(支付宝支付的客户)，购买天数大于等于2天的客户比例
	public String retrade_rate_30d;
	
	// 购买（支付）两种及以上商品的用户占购买（支付）用户的比例
	public String related_trade_buyer_rate;
	
	// 交易成功的订单，从拍下到交易成功的平均时间跨度（单位：天）
	public String avg_trade_cycle;
	
	// 统计周期内，成功完成（支付宝）支付的订单，从拍下到（支付宝）支付的平均时间跨度（单位：秒）
	public String avg_alipay_duration;
	
	public BuyerHabitDay(QueryRow row) {
		super();
		if(row != null) {
			List<String> values = row.getValues();
			this.thedate = values.get(0);
			this.userId = values.get(1);
			this.retrade_rate_30d = values.get(2);
			this.related_trade_buyer_rate = values.get(3);
			this.avg_trade_cycle = values.get(4);
			this.avg_alipay_duration = values.get(5);
		}
	}
	
	public BuyerHabitDay() {
		super();
	}
	
	public BuyerHabitDay(String thedate, String userId,
			String retrade_rate_30d, String related_trade_buyer_rate,
			String avg_trade_cycle, String avg_alipay_duration) {
		super();
		this.thedate = thedate;
		this.userId = userId;
		this.retrade_rate_30d = retrade_rate_30d;
		this.related_trade_buyer_rate = related_trade_buyer_rate;
		this.avg_trade_cycle = avg_trade_cycle;
		this.avg_alipay_duration = avg_alipay_duration;
	}

	public String getThedate() {
		return thedate;
	}

	public void setThedate(String thedate) {
		this.thedate = thedate;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getRetrade_rate_30d() {
		return retrade_rate_30d;
	}

	public void setRetrade_rate_30d(String retrade_rate_30d) {
		this.retrade_rate_30d = retrade_rate_30d;
	}

	public String getRelated_trade_buyer_rate() {
		return related_trade_buyer_rate;
	}

	public void setRelated_trade_buyer_rate(String related_trade_buyer_rate) {
		this.related_trade_buyer_rate = related_trade_buyer_rate;
	}

	public String getAvg_trade_cycle() {
		return avg_trade_cycle;
	}

	public void setAvg_trade_cycle(String avg_trade_cycle) {
		this.avg_trade_cycle = avg_trade_cycle;
	}

	public String getAvg_alipay_duration() {
		return avg_alipay_duration;
	}

	public void setAvg_alipay_duration(String avg_alipay_duration) {
		this.avg_alipay_duration = avg_alipay_duration;
	}
	
	
	
}
