package models.jms;

public class MsgContent {
	// 买家昵称
	private String buyer_nick;
	
	// 订单实付金额
	private String payment;
	
	// 订单状态
	private String status;
	
	// 子订单ID
	private Long oid;
	
	// 评价者：枚举buyer,seller,unknown
	private String rater;
	
	// 主订单ID
	private Long tid;
	
	// 交易类型列表
	private String type;
	
	// 卖家昵称
	private String seller_nick;
	
	public String getBuyer_nick() {
		return buyer_nick;
	}
	public void setBuyer_nick(String buyer_nick) {
		this.buyer_nick = buyer_nick;
	}
	public String getPayment() {
		return payment;
	}
	public void setPayment(String payment) {
		this.payment = payment;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Long getOid() {
		return oid;
	}
	public void setOid(Long oid) {
		this.oid = oid;
	}
	public String getRater() {
		return rater;
	}
	public void setRater(String rater) {
		this.rater = rater;
	}
	public Long getTid() {
		return tid;
	}
	public void setTid(Long tid) {
		this.tid = tid;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getSeller_nick() {
		return seller_nick;
	}
	public void setSeller_nick(String seller_nick) {
		this.seller_nick = seller_nick;
	}
	
	public MsgContent() {
		
	}
	
	public MsgContent(String buyer_nick, String payment, String status, Long oid, String rater, Long tid, String type, String seller_nick) {
		super();
		this.buyer_nick = buyer_nick;
		this.payment = payment;
		this.status = status;
		this.oid = oid;
		this.rater = rater;
		this.tid = tid;
		this.type = type;
		this.seller_nick = seller_nick;
	}
	
	public String toString() {
		return "MsgContent [buyer_nick=" + buyer_nick + ", payment=" + payment + ", status=" + status + ", oid=" + oid
				+ ", rater=" + rater + ", tid=" + tid + ", type=" + type + ", seller_nick=" + seller_nick + "]";
	}
	
}
