package actions.clouddata;

import java.util.List;

import com.taobao.api.domain.QueryRow;

// 用户一段时间之内的DSR评分
public class UserPeriodDSR {
	
	// 起始时间
	public String startdate;

	// 结束时间
	public String enddate;

	// 卖家Id
	public String userId;

	// 店铺类型： 0是集市店 ，1是天猫店
	public String shop_type;
	
	// 系统默认好评子订单数
	public String sys_def_pos_comt_tra_num;
	
	// 用户评价为好评的子订单笔数
	public String pos_comt_trade_num;
	
	// 用户评价为中评的子订单笔数
	public String mid_comt_trade_num;
	
	// 用户评价为差评的子订单笔数
	public String neg_comt_trade_num;
	
	// 用户评价的子订单总笔数
	public String comt_trade_num;
	
	// 订单的商品质量----“商品与描述相符”的dsr评分值
	public String dsr_qual_score;
	
	// 订单的商品质量----“商品与描述相符”的dsr评分总次数
	public String dsr_qual_comt_cnt;
	
	// 订单产生的服务质量的dsr评分值
	public String dsr_serv_score;
	
	// 订单产生的服务质量的dsr评分总次数
	public String dsr_serv_comt_cnt;
	
	// 订单产生的卖家发货质量的dsr评分值
	public String dsr_deli_score;
	
	// 订单产生的卖家发货质量的dsr评分总次数
	public String dsr_deli_comt_cnt;
	
	// 订单产生的物流服务质量的dsr评分值
	public String dsr_logi_score;
	
	// 订单产生的物流服务质量的dsr评分总次数
	public String dsr_logi_comt_cnt;

	public UserPeriodDSR(QueryRow row, String startdate, String enddate) {
		if(row != null) {
			this.startdate = startdate;
			this.enddate = enddate;
			List<String> values = row.getValues();
			this.userId = values.get(1);
			this.shop_type = values.get(2);
			this.sys_def_pos_comt_tra_num = values.get(3);
			this.pos_comt_trade_num = values.get(4);
			this.mid_comt_trade_num = values.get(5);
			this.neg_comt_trade_num = values.get(6);
			this.comt_trade_num = values.get(7);
			this.dsr_qual_score = values.get(8);
			this.dsr_qual_comt_cnt = values.get(9);
			this.dsr_serv_score = values.get(10);
			this.dsr_serv_comt_cnt = values.get(11);
			this.dsr_deli_score = values.get(12);
			this.dsr_logi_comt_cnt = values.get(13);
			this.dsr_logi_score = values.get(14);
			this.dsr_logi_comt_cnt = values.get(15);
		}
	}
	
	public UserPeriodDSR(String startdate, String enddate, String userId,
			String shop_type, String sys_def_pos_comt_tra_num,
			String pos_comt_trade_num, String mid_comt_trade_num,
			String neg_comt_trade_num, String comt_trade_num,
			String dsr_qual_score, String dsr_qual_comt_cnt,
			String dsr_serv_score, String dsr_serv_comt_cnt,
			String dsr_deli_score, String dsr_deli_comt_cnt,
			String dsr_logi_score, String dsr_logi_comt_cnt) {
		super();
		this.startdate = startdate;
		this.enddate = enddate;
		this.userId = userId;
		this.shop_type = shop_type;
		this.sys_def_pos_comt_tra_num = sys_def_pos_comt_tra_num;
		this.pos_comt_trade_num = pos_comt_trade_num;
		this.mid_comt_trade_num = mid_comt_trade_num;
		this.neg_comt_trade_num = neg_comt_trade_num;
		this.comt_trade_num = comt_trade_num;
		this.dsr_qual_score = dsr_qual_score;
		this.dsr_qual_comt_cnt = dsr_qual_comt_cnt;
		this.dsr_serv_score = dsr_serv_score;
		this.dsr_serv_comt_cnt = dsr_serv_comt_cnt;
		this.dsr_deli_score = dsr_deli_score;
		this.dsr_deli_comt_cnt = dsr_deli_comt_cnt;
		this.dsr_logi_score = dsr_logi_score;
		this.dsr_logi_comt_cnt = dsr_logi_comt_cnt;
	}

	public String getStartdate() {
		return startdate;
	}

	public void setStartdate(String startdate) {
		this.startdate = startdate;
	}

	public String getEnddate() {
		return enddate;
	}

	public void setEnddate(String enddate) {
		this.enddate = enddate;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getShop_type() {
		return shop_type;
	}

	public void setShop_type(String shop_type) {
		this.shop_type = shop_type;
	}

	public String getSys_def_pos_comt_tra_num() {
		return sys_def_pos_comt_tra_num;
	}

	public void setSys_def_pos_comt_tra_num(String sys_def_pos_comt_tra_num) {
		this.sys_def_pos_comt_tra_num = sys_def_pos_comt_tra_num;
	}

	public String getPos_comt_trade_num() {
		return pos_comt_trade_num;
	}

	public void setPos_comt_trade_num(String pos_comt_trade_num) {
		this.pos_comt_trade_num = pos_comt_trade_num;
	}

	public String getMid_comt_trade_num() {
		return mid_comt_trade_num;
	}

	public void setMid_comt_trade_num(String mid_comt_trade_num) {
		this.mid_comt_trade_num = mid_comt_trade_num;
	}

	public String getNeg_comt_trade_num() {
		return neg_comt_trade_num;
	}

	public void setNeg_comt_trade_num(String neg_comt_trade_num) {
		this.neg_comt_trade_num = neg_comt_trade_num;
	}

	public String getComt_trade_num() {
		return comt_trade_num;
	}

	public void setComt_trade_num(String comt_trade_num) {
		this.comt_trade_num = comt_trade_num;
	}

	public String getDsr_qual_score() {
		return dsr_qual_score;
	}

	public void setDsr_qual_score(String dsr_qual_score) {
		this.dsr_qual_score = dsr_qual_score;
	}

	public String getDsr_qual_comt_cnt() {
		return dsr_qual_comt_cnt;
	}

	public void setDsr_qual_comt_cnt(String dsr_qual_comt_cnt) {
		this.dsr_qual_comt_cnt = dsr_qual_comt_cnt;
	}

	public String getDsr_serv_score() {
		return dsr_serv_score;
	}

	public void setDsr_serv_score(String dsr_serv_score) {
		this.dsr_serv_score = dsr_serv_score;
	}

	public String getDsr_serv_comt_cnt() {
		return dsr_serv_comt_cnt;
	}

	public void setDsr_serv_comt_cnt(String dsr_serv_comt_cnt) {
		this.dsr_serv_comt_cnt = dsr_serv_comt_cnt;
	}

	public String getDsr_deli_score() {
		return dsr_deli_score;
	}

	public void setDsr_deli_score(String dsr_deli_score) {
		this.dsr_deli_score = dsr_deli_score;
	}

	public String getDsr_deli_comt_cnt() {
		return dsr_deli_comt_cnt;
	}

	public void setDsr_deli_comt_cnt(String dsr_deli_comt_cnt) {
		this.dsr_deli_comt_cnt = dsr_deli_comt_cnt;
	}

	public String getDsr_logi_score() {
		return dsr_logi_score;
	}

	public void setDsr_logi_score(String dsr_logi_score) {
		this.dsr_logi_score = dsr_logi_score;
	}

	public String getDsr_logi_comt_cnt() {
		return dsr_logi_comt_cnt;
	}

	public void setDsr_logi_comt_cnt(String dsr_logi_comt_cnt) {
		this.dsr_logi_comt_cnt = dsr_logi_comt_cnt;
	}
	
	
}
