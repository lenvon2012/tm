package actions.clouddata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import models.user.User;
import actions.UvPvDiagAction;

import com.taobao.api.domain.QueryRow;

public class SrcResult implements Serializable{
	
	public String thedate;
	
	public String sellerId;
	
	public String itemId;
	
	public String srcId;
	
	public String srcLevel;
	
	public String srcParentId;
	
	/** 访客数 iuv */
	public String uv;
	
	/** 浏览次数  ipv */
	public String pv;
	
	/** 下单买家数  crt_ord_byr_cnt_holotree_lastbe_guide */
	private String gmvWinnerNum;
	
	/** 下单金额  crt_ord_amt_holotree_lastbe_guide */
	private String gmvTradeAmt;
	
	/** 下单商品件数  crt_ord_item_qty_holotree_lastbe_guide */
	private String gmvAuctionNum;
	
	/** 下单子订单数 crt_ord_cnt_holotree_lastbe_guide */
	private String gmvTradeNum;
	
	/** 支付的子订单数  pay_ord_cnt_holotree_lastbe_guide */
    private String alipayTradeNum;
    
    /** 支付商品件数  pay_ord_item_qty_holotree_lastbe_guide */
    private String alipayAuctionNum;
    
    /** 支付买家数  pay_ord_byr_cnt_holotree_lastbe_guide */
    private String alipayWinnerNum;
    
    /** 支付金额  pay_ord_amt_holotree_lastbe_guide */
    private String alipayTradeAmt; 
    
    /** 商品添加收藏次数  item_clt_cnt */
//    private String itemCltCnt;
    
    /** 添加购物车的商品件数  cart_item_qty */
//    private String cartItemQty;
    
	public SrcResult(QueryRow row, User user) {
		super();
		if(row != null) {
			List<String> values = row.getValues();
			this.thedate = values.get(0);
			this.sellerId = values.get(1);
			this.itemId = values.get(2);
			this.srcId = values.get(3);
			this.srcLevel = values.get(4);
			this.srcParentId = values.get(5);
			this.uv = values.get(6);
			this.pv = values.get(7);
			this.gmvWinnerNum = values.get(8);
			this.gmvTradeAmt = values.get(9);
			this.gmvAuctionNum = values.get(10);
			this.gmvTradeNum = values.get(11);
			this.alipayWinnerNum = values.get(12);
			this.alipayTradeAmt = values.get(13);
			this.alipayAuctionNum = values.get(14);
			this.alipayTradeNum = values.get(15);
//			this.itemCltCnt = values.get(16);
//			this.cartItemQty = values.get(17);
		}
	}
	
	public SrcResult(String dataTime, User user){
		super();
		this.thedate = dataTime;
		this.uv = "0";
		this.pv = "0";
		this.gmvWinnerNum = "0";
		this.gmvTradeAmt = "0";
		this.gmvAuctionNum = "0";
		this.gmvTradeNum = "0";
		this.alipayWinnerNum = "0";
		this.alipayTradeAmt = "0";
		this.alipayAuctionNum = "0";
		this.alipayTradeNum = "0";
//		this.itemCltCnt = "0";
//		this.cartItemQty = "0";
	}

	public String getThedate() {
		return thedate;
	}

	public void setThedate(String thedate) {
		this.thedate = thedate;
	}

	public String getSellerId() {
		return sellerId;
	}

	public void setSellerId(String sellerId) {
		this.sellerId = sellerId;
	}

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public String getSrcId() {
		return srcId;
	}

	public void setSrcId(String srcId) {
		this.srcId = srcId;
	}

	public String getSrcLevel() {
		return srcLevel;
	}

	public void setSrcLevel(String srcLevel) {
		this.srcLevel = srcLevel;
	}

	public String getSrcParentId() {
		return srcParentId;
	}

	public void setSrcParentId(String srcParentId) {
		this.srcParentId = srcParentId;
	}

	public String getUv() {
		return uv;
	}

	public void setUv(String uv) {
		this.uv = uv;
	}

	public String getPv() {
		return pv;
	}

	public void setPv(String pv) {
		this.pv = pv;
	}

	public String getGmvWinnerNum() {
		return gmvWinnerNum;
	}

	public void setGmvWinnerNum(String gmvWinnerNum) {
		this.gmvWinnerNum = gmvWinnerNum;
	}

	public String getGmvTradeAmt() {
		return gmvTradeAmt;
	}

	public void setGmvTradeAmt(String gmvTradeAmt) {
		this.gmvTradeAmt = gmvTradeAmt;
	}

	public String getGmvAuctionNum() {
		return gmvAuctionNum;
	}

	public void setGmvAuctionNum(String gmvAuctionNum) {
		this.gmvAuctionNum = gmvAuctionNum;
	}

	public String getGmvTradeNum() {
		return gmvTradeNum;
	}

	public void setGmvTradeNum(String gmvTradeNum) {
		this.gmvTradeNum = gmvTradeNum;
	}

	public String getAlipayTradeNum() {
		return alipayTradeNum;
	}

	public void setAlipayTradeNum(String alipayTradeNum) {
		this.alipayTradeNum = alipayTradeNum;
	}

	public String getAlipayAuctionNum() {
		return alipayAuctionNum;
	}

	public void setAlipayAuctionNum(String alipayAuctionNum) {
		this.alipayAuctionNum = alipayAuctionNum;
	}

	public String getAlipayWinnerNum() {
		return alipayWinnerNum;
	}

	public void setAlipayWinnerNum(String alipayWinnerNum) {
		this.alipayWinnerNum = alipayWinnerNum;
	}

	public String getAlipayTradeAmt() {
		return alipayTradeAmt;
	}

	public void setAlipayTradeAmt(String alipayTradeAmt) {
		this.alipayTradeAmt = alipayTradeAmt;
	}

//	public String getItemCltCnt() {
//		return itemCltCnt;
//	}
//
//	public void setItemCltCnt(String itemCltCnt) {
//		this.itemCltCnt = itemCltCnt;
//	}
//
//	public String getCartItemQty() {
//		return cartItemQty;
//	}
//
//	public void setCartItemQty(String cartItemQty) {
//		this.cartItemQty = cartItemQty;
//	}
	
}
