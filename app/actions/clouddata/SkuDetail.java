package actions.clouddata;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.taobao.api.domain.QueryRow;
import com.taobao.api.domain.Sku;

public class SkuDetail implements Serializable{
	
	public static final String DEFAULT_VALUE = "0";
	
	private String skuId;
	
	// sku所对应的销售属性的中文名字串
	private String propertiesName;
	
	// 价格
	private String price;
	
	// sku在线库存数
	private String skuStock;

	// 添加购物车的用户数
	private String addCartUserNum;
	
	// 商品被拍下的总件数
	private String gmvAuctionNum;
	
	// 成功拍下的人数。所选时间段内同一用户拍下多笔订单会进行去重计算。
	private String gmvWinnerNum;
	
	// 通过支付宝付款的商品总件数
	private String alipayAuctionNum;
	
	// 成功拍下并完成支付宝付款的人数
	private String alipayWinnerNum;
	
	public SkuDetail(){
		
	}

	public SkuDetail(Sku sku){
		this.skuId = String.valueOf(sku.getSkuId());
		String propertiesName = StringUtils.EMPTY;
		String name = sku.getPropertiesName();
		String[] split = name.split(";");
		for (String s : split) {
			String[] split2 = s.split(":");
			propertiesName += split2[2] + ":" + split2[3] + ";";
		}
		propertiesName = propertiesName.substring(0, propertiesName.length() - 1);
		this.propertiesName = propertiesName;
		this.price = sku.getPrice();
		this.skuStock = String.valueOf(sku.getQuantity());
		this.addCartUserNum = DEFAULT_VALUE;
		this.gmvAuctionNum = DEFAULT_VALUE;
		this.gmvWinnerNum = DEFAULT_VALUE;
		this.alipayAuctionNum = DEFAULT_VALUE;
		this.alipayWinnerNum = DEFAULT_VALUE;
	}
	
	public void addProps(QueryRow row) {
		if(row == null) {
			return;
		}
		List<String> value = row.getValues();
		if(this.skuId.equalsIgnoreCase(value.get(1))) {
			this.addCartUserNum = String.valueOf(value.get(2));
			this.gmvAuctionNum = String.valueOf(value.get(3));
			this.gmvWinnerNum = String.valueOf(value.get(4));
			this.alipayAuctionNum = String.valueOf(value.get(5));
			this.alipayWinnerNum = String.valueOf(value.get(6));
		}
	}
	
	public static String[] skuDetail2String(SkuDetail sku){
		String[] skuStr = new String[8];
		
		skuStr[0] = sku.propertiesName;
		skuStr[1] = sku.price;
		skuStr[2] = sku.skuStock;
		skuStr[3] = sku.addCartUserNum;
		skuStr[4] = sku.gmvAuctionNum;
		skuStr[5] = sku.gmvWinnerNum;
		skuStr[6] = sku.alipayAuctionNum;
		skuStr[7] = sku.alipayWinnerNum;
		
		return skuStr;
	}

	public String getSkuId() {
		return skuId;
	}

	public void setSkuId(String skuId) {
		this.skuId = skuId;
	}

	public String getPropertiesName() {
		return propertiesName;
	}

	public void setPropertiesName(String propertiesName) {
		this.propertiesName = propertiesName;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getSkuStock() {
		return skuStock;
	}

	public void setSkuStock(String skuStock) {
		this.skuStock = skuStock;
	}

	public String getAddCartUserNum() {
		return addCartUserNum;
	}

	public void setAddCartUserNum(String addCartUserNum) {
		this.addCartUserNum = addCartUserNum;
	}

	public String getGmvAuctionNum() {
		return gmvAuctionNum;
	}

	public void setGmvAuctionNum(String gmvAuctionNum) {
		this.gmvAuctionNum = gmvAuctionNum;
	}

	public String getGmvWinnerNum() {
		return gmvWinnerNum;
	}

	public void setGmvWinnerNum(String gmvWinnerNum) {
		this.gmvWinnerNum = gmvWinnerNum;
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

}