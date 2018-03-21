package actions.clouddata;

import java.util.List;

import models.CloudDataRegion;

import com.taobao.api.domain.QueryRow;

public class AreaViewsAndTrades {
	
	public String regionId;
	
	public String regionName;
	
	public String provinceId;
	
	public String provinceName;
	
	public String thedate;
	
	// 页面被查看的次数。用户多次打开或刷新同一个页面，该指标值累加。
	public String pv;

	// 页面的独立访问人数。所选时间段内，同一访客多次访问会进行去重计算。
	public String uv;

	// 统计周期内，宝贝页面被浏览的次数。
	public String ipv;

	// 统计周期内，浏览宝贝页的独立访客数（这是总的）
	public String iuv;
	
	// 统计周期内，站内搜索uv
	public String searchUv;

	// 浏览回头客人数
	public String view_repeat_num;

	// 统计周期内，用户成功完成（支付宝）支付的子订单数（一笔订单，按照商品分拆为多个子订单）
	public String alipay_trade_num;

	// 通过支付宝付款的商品总件数
	public String alipay_auction_num;

	// 成功完成支付宝支付的金额(元)
	public String alipay_trade_amt;

	// 成功拍下并完成支付宝付款的人数（这也是总的）
	public String alipay_winner_num;
	
	// 站内搜索 成功拍下并完成支付宝付款的人数
	public String search_alipay_winner_num;

	// 商品被拍下的总件数
	public String gmv_auction_num;
	
	// 统计周期内，用户成功拍下的金额（以宝贝商品标价做计算）。
	public String gmv_trade_amt;

	// 统计周期内，用户成功拍下的子订单数（一笔订单，按照商品分拆为多个子订单）。
	public String gmv_trade_num;

	// 成功拍下的人数。所选时间段内同一用户拍下多笔订单会进行去重计算。
	public String gmv_winner_num;

	// 成交回头客
	public String trade_repeat_num;
	
	/*
	 * thedate, seller_id, region_id, pv, uv, ipv, iuv, view_repeat_num, alipay_trade_num, alipay_auction_num, alipay_trade_amt,
     * alipay_winner_num, gmv_auction_num, gmv_trade_amt, gmv_trade_num, gmv_winner_num  
	 *
	 */
	
	public AreaViewsAndTrades() {
		this.pv = "0";
		this.uv = "0";
		this.searchUv = "0";
		this.view_repeat_num = "0";
		this.trade_repeat_num = "0";
		this.alipay_trade_num = "0";
		this.alipay_auction_num = "0";
		this.alipay_trade_amt = "0";
		this.alipay_winner_num = "0";
		this.search_alipay_winner_num = "0";
	}

	public AreaViewsAndTrades(String pv, String uv, String view_repeat_num, String trade_repeat_num,
			String alipay_trade_num, String alipay_auction_num, String alipay_trade_amt, String alipay_winner_num) {
		this.pv = pv;
		this.uv = uv;
		this.view_repeat_num = view_repeat_num;
		this.trade_repeat_num = trade_repeat_num;
		this.alipay_trade_num = alipay_trade_num;
		this.alipay_auction_num = alipay_auction_num;
		this.alipay_trade_amt = alipay_trade_amt;
		this.alipay_winner_num = alipay_winner_num;
	}
	
	public void AddProp(Integer pv, Integer uv, Integer view_repeat_num, Integer trade_repeat_num,
			Integer alipay_trade_num, Integer alipay_auction_num, 
			float alipay_trade_amt, Integer alipay_winner_num) {
		this.pv = String.valueOf(Integer.valueOf(this.pv) + pv);
		this.uv = String.valueOf(Integer.valueOf(this.uv) + uv);
		this.view_repeat_num = String.valueOf(Integer.valueOf(this.view_repeat_num) + view_repeat_num);
		this.trade_repeat_num = String.valueOf(Integer.valueOf(this.trade_repeat_num) + trade_repeat_num);
		this.alipay_trade_num = String.valueOf(Integer.valueOf(this.alipay_trade_num) + alipay_trade_num);
		this.alipay_auction_num = String.valueOf(Integer.valueOf(this.alipay_auction_num) + alipay_auction_num);
		this.alipay_trade_amt = String.valueOf(Float.valueOf(this.alipay_trade_amt) + alipay_trade_amt);
		this.alipay_winner_num = String.valueOf(Integer.valueOf(this.alipay_winner_num) + alipay_winner_num);
	}
	
	public void AddProp(Integer pv, Integer uv, Integer alipay_trade_num, Integer alipay_auction_num, 
			float alipay_trade_amt, Integer alipay_winner_num) {
		this.pv = String.valueOf(Integer.valueOf(this.pv) + pv);
		this.uv = String.valueOf(Integer.valueOf(this.uv) + uv);
		this.alipay_trade_num = String.valueOf(Integer.valueOf(this.alipay_trade_num) + alipay_trade_num);
		this.alipay_auction_num = String.valueOf(Integer.valueOf(this.alipay_auction_num) + alipay_auction_num);
		this.alipay_trade_amt = String.valueOf(Float.valueOf(this.alipay_trade_amt) + alipay_trade_amt);
		this.alipay_winner_num = String.valueOf(Integer.valueOf(this.alipay_winner_num) + alipay_winner_num);
	}
	
	public void AddProp(AreaViewsAndTrades area) {
		if(area != null) {
			this.pv = String.valueOf(Integer.valueOf(this.pv) + Integer.valueOf(area.pv));
			this.uv = String.valueOf(Integer.valueOf(this.uv) + Integer.valueOf(area.uv));
			this.view_repeat_num = String.valueOf(Integer.valueOf(this.view_repeat_num) + Integer.valueOf(area.view_repeat_num));
			this.trade_repeat_num = String.valueOf(Integer.valueOf(this.trade_repeat_num) + Integer.valueOf(area.trade_repeat_num));
			this.alipay_trade_num = String.valueOf(Integer.valueOf(this.alipay_trade_num) + Integer.valueOf(area.alipay_trade_num));
			this.alipay_auction_num = String.valueOf(Integer.valueOf(this.alipay_auction_num) + Integer.valueOf(area.alipay_auction_num));
			this.alipay_trade_amt = String.valueOf(Float.valueOf(this.alipay_trade_amt) + Float.valueOf(area.alipay_trade_amt));
			this.alipay_winner_num = String.valueOf(Integer.valueOf(this.alipay_winner_num) + Integer.valueOf(area.alipay_winner_num));
		}
		
	}
	
	public void AddItemProp(Integer pv, Integer uv,	Integer alipay_trade_num,
			Integer alipay_auction_num, float alipay_trade_amt, Integer alipay_winner_num) {
		this.pv = String.valueOf(Integer.valueOf(this.pv) + pv);
		this.uv = String.valueOf(Integer.valueOf(this.uv) + uv);
		this.alipay_trade_num = String.valueOf(Integer.valueOf(this.alipay_trade_num) + alipay_trade_num);
		this.alipay_auction_num = String.valueOf(Integer.valueOf(this.alipay_auction_num) + alipay_auction_num);
		this.alipay_trade_amt = String.valueOf(Float.valueOf(this.alipay_trade_amt) + alipay_trade_amt);
		this.alipay_winner_num = String.valueOf(Integer.valueOf(this.alipay_winner_num) + alipay_winner_num);
	}
	
	public AreaViewsAndTrades(Integer pv, Integer uv, Integer view_repeat_num, Integer trade_repeat_num,
			Integer alipay_trade_num, Integer alipay_auction_num, Integer alipay_trade_amt, Integer alipay_winner_num) {
		this.pv = String.valueOf(pv);
		this.uv = String.valueOf(uv);
		this.view_repeat_num = String.valueOf(view_repeat_num);
		this.trade_repeat_num = String.valueOf(trade_repeat_num);
		this.alipay_trade_num = String.valueOf(alipay_trade_num);
		this.alipay_auction_num = String.valueOf(alipay_auction_num);
		this.alipay_trade_amt = String.valueOf(alipay_trade_amt);
		this.alipay_winner_num = String.valueOf(alipay_winner_num);
	}
	
	public AreaViewsAndTrades(QueryRow row) {
		super();
		if(row != null) {
			List<String> values = row.getValues();
			this.thedate = values.get(0);
			this.regionId = values.get(2);
			CloudDataRegion region = CloudDataRegion.findByRegionId(Long.valueOf(this.regionId));
			if(region != null) {
				regionName = region.getRegion_name();
			}
			this.pv = values.get(3);
			this.uv = values.get(4);
			this.ipv = values.get(5);
			this.iuv = values.get(6);
			this.view_repeat_num = values.get(7);
			if(values.size() > 8) {
				this.alipay_trade_num = values.get(8);
				this.alipay_auction_num = values.get(9);
				this.alipay_trade_amt = values.get(10);
				this.alipay_winner_num = values.get(11);
				this.gmv_auction_num = values.get(12);
				this.gmv_trade_amt = values.get(13);
				this.gmv_trade_num = values.get(14);
				this.gmv_winner_num = values.get(15);
			} else {
				this.alipay_trade_num = "0";
				this.alipay_auction_num = "0";
				this.alipay_trade_amt = "0";
				this.alipay_winner_num = "0";
				this.gmv_auction_num = "0";
				this.gmv_trade_amt = "0";
				this.gmv_trade_num = "0";
				this.gmv_winner_num = "0";
			}
			
		}
	}
	
	public AreaViewsAndTrades(String regionId, String pv, String uv,
			String ipv, String iuv, String view_repeat_num,
			String alipay_trade_num, String alipay_auction_num,
			String alipay_trade_amt, String alipay_winner_num,
			String gmv_auction_num, String gmv_trade_amt, String gmv_trade_num,
			String gmv_winner_num) {
		super();
		this.regionId = regionId;
		this.pv = pv;
		this.uv = uv;
		this.ipv = ipv;
		this.iuv = iuv;
		this.view_repeat_num = view_repeat_num;
		this.alipay_trade_num = alipay_trade_num;
		this.alipay_auction_num = alipay_auction_num;
		this.alipay_trade_amt = alipay_trade_amt;
		this.alipay_winner_num = alipay_winner_num;
		this.gmv_auction_num = gmv_auction_num;
		this.gmv_trade_amt = gmv_trade_amt;
		this.gmv_trade_num = gmv_trade_num;
		this.gmv_winner_num = gmv_winner_num;
	}

	public String getRegionId() {
		return regionId;
	}

	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}

	public String getRegionName() {
		return regionName;
	}

	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}

	public String getProvinceId() {
		return provinceId;
	}

	public void setProvinceId(String provinceId) {
		this.provinceId = provinceId;
	}

	public String getProvinceName() {
		return provinceName;
	}

	public void setProvinceName(String provinceName) {
		this.provinceName = provinceName;
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

	public String getIpv() {
		return ipv;
	}

	public void setIpv(String ipv) {
		this.ipv = ipv;
	}

	public String getIuv() {
		return iuv;
	}

	public void setIuv(String iuv) {
		this.iuv = iuv;
	}

	public String getSearchUv() {
		return searchUv;
	}

	public void setSearchUv(String searchUv) {
		this.searchUv = searchUv;
	}

	public String getSearch_alipay_winner_num() {
		return search_alipay_winner_num;
	}

	public void setSearch_alipay_winner_num(String search_alipay_winner_num) {
		this.search_alipay_winner_num = search_alipay_winner_num;
	}

	public String getView_repeat_num() {
		return view_repeat_num;
	}

	public void setView_repeat_num(String view_repeat_num) {
		this.view_repeat_num = view_repeat_num;
	}

	public String getAlipay_trade_num() {
		return alipay_trade_num;
	}

	public void setAlipay_trade_num(String alipay_trade_num) {
		this.alipay_trade_num = alipay_trade_num;
	}

	public String getAlipay_auction_num() {
		return alipay_auction_num;
	}

	public void setAlipay_auction_num(String alipay_auction_num) {
		this.alipay_auction_num = alipay_auction_num;
	}

	public String getAlipay_trade_amt() {
		return alipay_trade_amt;
	}

	public void setAlipay_trade_amt(String alipay_trade_amt) {
		this.alipay_trade_amt = alipay_trade_amt;
	}

	public String getAlipay_winner_num() {
		return alipay_winner_num;
	}

	public void setAlipay_winner_num(String alipay_winner_num) {
		this.alipay_winner_num = alipay_winner_num;
	}

	public String getGmv_auction_num() {
		return gmv_auction_num;
	}

	public void setGmv_auction_num(String gmv_auction_num) {
		this.gmv_auction_num = gmv_auction_num;
	}

	public String getGmv_trade_amt() {
		return gmv_trade_amt;
	}

	public void setGmv_trade_amt(String gmv_trade_amt) {
		this.gmv_trade_amt = gmv_trade_amt;
	}

	public String getGmv_trade_num() {
		return gmv_trade_num;
	}

	public void setGmv_trade_num(String gmv_trade_num) {
		this.gmv_trade_num = gmv_trade_num;
	}

	public String getGmv_winner_num() {
		return gmv_winner_num;
	}

	public void setGmv_winner_num(String gmv_winner_num) {
		this.gmv_winner_num = gmv_winner_num;
	}

	public String getThedate() {
		return thedate;
	}

	public void setThedate(String thedate) {
		this.thedate = thedate;
	}

	public String getTrade_repeat_num() {
		return trade_repeat_num;
	}

	public void setTrade_repeat_num(String trade_repeat_num) {
		this.trade_repeat_num = trade_repeat_num;
	}
	
	
}
