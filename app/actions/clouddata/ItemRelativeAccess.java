package actions.clouddata;

import java.util.List;

import com.taobao.api.domain.QueryRow;

public class ItemRelativeAccess {

	public String thedate;
	
	public String shop_id;
	
	public String seller_id;
	
	public String auction_id;
	
	public String asso_access_num;
	
	public String asso_access_user_num;
	
	public String asso_alipay_num;
	
	public String asso_alipay_user_num;
	
	public String asso_alipay_amt;
	
	public String asso_alipay_auction_num;
	
	public String asso_alipay_trade_num;

	public ItemRelativeAccess(QueryRow row) {
		if(row != null) {
			List<String> values = row.getValues();
			this.thedate = values.get(0);
			this.shop_id = values.get(1);
			this.seller_id = values.get(2);
			this.auction_id = values.get(3);
			this.asso_access_num = values.get(4);
			this.asso_access_user_num = values.get(5);
			this.asso_alipay_num = values.get(6);
			this.asso_alipay_user_num = values.get(7);
			this.asso_alipay_amt = values.get(8);
			this.asso_alipay_auction_num = values.get(9);
			this.asso_alipay_trade_num = values.get(10);
		}
	}
	
	public ItemRelativeAccess(String asso_access_num,
			String asso_access_user_num, String asso_alipay_num,
			String asso_alipay_user_num, String asso_alipay_amt,
			String asso_alipay_auction_num, String asso_alipay_trade_num) {
		super();
		this.asso_access_num = asso_access_num;
		this.asso_access_user_num = asso_access_user_num;
		this.asso_alipay_num = asso_alipay_num;
		this.asso_alipay_user_num = asso_alipay_user_num;
		this.asso_alipay_amt = asso_alipay_amt;
		this.asso_alipay_auction_num = asso_alipay_auction_num;
		this.asso_alipay_trade_num = asso_alipay_trade_num;
	}

	public ItemRelativeAccess(String thedate, String shop_id, String seller_id,
			String auction_id, String asso_access_num,
			String asso_access_user_num, String asso_alipay_num,
			String asso_alipay_user_num, String asso_alipay_amt,
			String asso_alipay_auction_num, String asso_alipay_trade_num) {
		super();
		this.thedate = thedate;
		this.shop_id = shop_id;
		this.seller_id = seller_id;
		this.auction_id = auction_id;
		this.asso_access_num = asso_access_num;
		this.asso_access_user_num = asso_access_user_num;
		this.asso_alipay_num = asso_alipay_num;
		this.asso_alipay_user_num = asso_alipay_user_num;
		this.asso_alipay_amt = asso_alipay_amt;
		this.asso_alipay_auction_num = asso_alipay_auction_num;
		this.asso_alipay_trade_num = asso_alipay_trade_num;
	}

	public String getThedate() {
		return thedate;
	}

	public void setThedate(String thedate) {
		this.thedate = thedate;
	}

	public String getShop_id() {
		return shop_id;
	}

	public void setShop_id(String shop_id) {
		this.shop_id = shop_id;
	}

	public String getSeller_id() {
		return seller_id;
	}

	public void setSeller_id(String seller_id) {
		this.seller_id = seller_id;
	}

	public String getAuction_id() {
		return auction_id;
	}

	public void setAuction_id(String auction_id) {
		this.auction_id = auction_id;
	}

	public String getAsso_access_num() {
		return asso_access_num;
	}

	public void setAsso_access_num(String asso_access_num) {
		this.asso_access_num = asso_access_num;
	}

	public String getAsso_access_user_num() {
		return asso_access_user_num;
	}

	public void setAsso_access_user_num(String asso_access_user_num) {
		this.asso_access_user_num = asso_access_user_num;
	}

	public String getAsso_alipay_num() {
		return asso_alipay_num;
	}

	public void setAsso_alipay_num(String asso_alipay_num) {
		this.asso_alipay_num = asso_alipay_num;
	}

	public String getAsso_alipay_user_num() {
		return asso_alipay_user_num;
	}

	public void setAsso_alipay_user_num(String asso_alipay_user_num) {
		this.asso_alipay_user_num = asso_alipay_user_num;
	}

	public String getAsso_alipay_amt() {
		return asso_alipay_amt;
	}

	public void setAsso_alipay_amt(String asso_alipay_amt) {
		this.asso_alipay_amt = asso_alipay_amt;
	}

	public String getAsso_alipay_auction_num() {
		return asso_alipay_auction_num;
	}

	public void setAsso_alipay_auction_num(String asso_alipay_auction_num) {
		this.asso_alipay_auction_num = asso_alipay_auction_num;
	}

	public String getAsso_alipay_trade_num() {
		return asso_alipay_trade_num;
	}

	public void setAsso_alipay_trade_num(String asso_alipay_trade_num) {
		this.asso_alipay_trade_num = asso_alipay_trade_num;
	}
	
	
}
