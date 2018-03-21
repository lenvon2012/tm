package models.words;

import java.io.Serializable;

import controllers.SupportUtil;
import models.words.ItemDetail;
import models.words.Txg20WWordsDayNoDump;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HotSalesItem implements Serializable{
	private static final Logger log = LoggerFactory.getLogger(HotSalesItem.class);
	
	private static final long serialVersionUID = -1L;
	
	public Long numIid;

	public int rank;

	public int pageNo;

	public String day;

	/*
	 * 30天销量
	 */
	public int sold;
	/**
	 * @Description: Rate of Change
	 */
	public float soldRoc;
	
	public float sales;
	public float salesRoc;

	/*
	 * 宝贝所属类目
	 */
	public Long category;

	/*
	 * 宝贝标题
	 */
	public String title;

	/*
	 * 卖家ID
	 */
	public Long userId;

	/*
	 * 卖家旺旺
	 */
	public String nick;

	/*
	 * 发货地址
	 */
	public String location;

	/*
	 * 主图链接
	 */
	public String picPath;

	/*
	 * 宝贝实际价格
	 */
	public String price;

	public int viewCount;

	public Long createTs;

	public Long updateTs;

	public HotSalesItem() {
		super();
	}

	public HotSalesItem(Txg20WWordsDayNoDump t, String day, ItemDetail i, Txg20WWordsDayNoDump formerT) {
		super();
		this.numIid = t.getNumIid();
//		this.rank = t.getRank();
//		this.pageNo = t.getPageNo();
		this.day = day;
		this.sold = t.getSold();
		this.category = t.getCategory();
		if (i != null) {
			this.title = i.getTitle();
			this.userId = i.getUserId();
			this.nick = i.getNick();
			this.location = i.getLocation();
			this.picPath = i.getPicPath();
			this.price = i.getPrice();
			this.viewCount = i.getViewCount();
		} else {
			log.error("严重！行业查查询时ItemDetail表中未找到宝贝id为" + t.getNumIid() + "的详细信息");
			this.title = "未知宝贝标题";
			this.title = "宝贝id" + this.numIid;
			this.userId = 1L;
			this.nick = "未知卖家";
			this.location = "未知发货地";
			this.picPath = "//img.alicdn.com/tps/i3/T1OjaVFl4dXXa.JOZB-114-114.png";
			this.price = "0.00";
			this.viewCount = 0;
		}
		try {
			this.sales = this.sold * Float.parseFloat(this.price);
		} catch (NumberFormatException e) {
			this.sales = 0F;
		}
		if (formerT != null) {
			this.soldRoc = calcPercentage(this.sold, formerT.getSold());
			this.salesRoc = calcPercentage(this.sales, formerT.getSold()*Float.parseFloat(this.price));
		} else {
			this.soldRoc = 0F;
			this.salesRoc = 0F;
		}
	}
	
	public HotSalesItem(Long numIid, String day, ItemDetail i, Txg20WWordsDayNoDump t, Txg20WWordsDayNoDump formerT) {
		super();
		this.numIid = numIid;
		this.day = day;
		if (i != null) {
			this.title = i.getTitle();
			this.userId = i.getUserId();
			this.nick = i.getNick();
			this.location = i.getLocation();
			this.picPath = i.getPicPath();
			this.price = i.getPrice();
			this.category = i.getCategory();
			this.viewCount = i.getViewCount();
		} else {
			this.title = "未知宝贝标题";
			this.title = "宝贝id" + this.numIid;
			this.userId = 1L;
			this.nick = "未知卖家";
			this.location = "未知发货地";
			this.picPath = "//img.alicdn.com/tps/i3/T1OjaVFl4dXXa.JOZB-114-114.png";
			this.price = "0.00";
			this.category = 0L;
			this.viewCount = 0;
		}
		if( t != null) {
			this.sold = t.getSold();
		} else {
			this.sold = 0;
		}
		try {
			this.sales = this.sold * Float.parseFloat(this.price);
		} catch (NumberFormatException e) {
			this.sales = 0F;
		}
		if (formerT != null) {
			this.soldRoc = calcPercentage(this.sold, formerT.getSold());
			this.salesRoc = calcPercentage(this.sales, formerT.getSold()*Float.parseFloat(this.price));
		} else {
			this.soldRoc = 0F;
			this.salesRoc = 0F;
		}
	}
	
	private static float calcPercentage(float dividen, float divisor) {
		if (dividen <= 0F || divisor <= 0F) {
			return 0F;
		}
		return (dividen/divisor - 1F)*100F;
	}
	
	public HotSalesItem(Long numIid, byte rank, byte pageNo, String day, int sold, Long category,
						String title, Long userId, String nick, String location, String picPath, String price, Long createTs,
						Long updateTs) {
		super();
		this.numIid = numIid;
		this.rank = rank;
		this.pageNo = pageNo;
		this.day = day;
		this.sold = sold;
		this.category = category;
		this.title = title;
		this.userId = userId;
		this.nick = nick;
		this.location = location;
		this.picPath = picPath;
		this.price = price;
		this.createTs = createTs;
		this.updateTs = updateTs;
		try{
			this.sales = sold * Float.parseFloat(price);
		} catch (NumberFormatException e) {
			this.sales = 0F;
		}
		
		//TODO
		this.soldRoc = 0F;
		this.salesRoc = 0F;
	}

	public Long getNumIid() {
		return numIid;
	}

	public void setNumIid(Long numIid) {
		this.numIid = numIid;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public int getSold() {
		return sold;
	}

	public void setSold(int sold) {
		this.sold = sold;
	}

	public Long getCategory() {
		return category;
	}

	public void setCategory(Long category) {
		this.category = category;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getPicPath() {
		return picPath;
	}

	public void setPicPath(String picPath) {
		this.picPath = picPath;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public int getViewCount() {
		return viewCount;
	}

	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
	}

	public Long getCreateTs() {
		return createTs;
	}

	public void setCreateTs(Long createTs) {
		this.createTs = createTs;
	}

	public Long getUpdateTs() {
		return updateTs;
	}

	public void setUpdateTs(Long updateTs) {
		this.updateTs = updateTs;
	}
}
