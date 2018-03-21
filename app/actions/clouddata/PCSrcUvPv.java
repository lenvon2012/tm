package actions.clouddata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import models.user.User;
import actions.UvPvDiagAction;

import com.taobao.api.domain.QueryRow;

public class PCSrcUvPv implements Serializable{
	
	public String thedate;
	
	public String srcId;
	
	public String srcName;
	
	public String parentSrcId;
	
	public String parentSrcName;
	
	public String uv;
	
	public String pv;
	
	public String alipay_trade_num;
	
	public String alipay_auction_num;
	
	public String alipay_trade_amt;
	
	public String alipay_winner_num;
	
	public String bounce_count;

	public PCSrcUvPv(String thedate, String srcId, String parentSrcId, String uv, String pv, User user) {
		super();
		this.thedate = thedate;
		this.srcId = srcId;
		this.srcName = UvPvDiagAction.getPCSrcNameById(srcId, user);
		this.parentSrcId = parentSrcId;
		this.parentSrcName = UvPvDiagAction.getPCSrcNameById(parentSrcId, user);
		this.uv = uv;
		this.pv = pv;
	}
	
	public PCSrcUvPv(String srcId, String parentSrcId, String uv, String pv, String alipay_trade_num,
			String alipay_auction_num, String alipay_winner_num, String alipay_trade_amt, User user) {
		super();
		this.srcId = srcId;
		this.srcName = UvPvDiagAction.getPCSrcNameById(srcId, user);
		this.parentSrcId = parentSrcId;
		this.parentSrcName = UvPvDiagAction.getPCSrcNameById(parentSrcId, user);
		this.uv = uv;
		this.pv = pv;
		this.alipay_trade_num = alipay_trade_num;
		this.alipay_auction_num = alipay_auction_num;
		this.alipay_winner_num = alipay_winner_num;
		this.alipay_trade_amt = alipay_trade_amt;
	}
	
	public PCSrcUvPv(QueryRow row, User user) {
		super();
		if(row != null) {
			List<String> values = row.getValues();
			this.thedate = values.get(0);
			this.pv = values.get(7);
			this.srcId = values.get(3);
			this.uv = values.get(6);
			this.parentSrcId = values.get(5);
			this.srcName = UvPvDiagAction.getPCSrcNameById(srcId, user);
			this.parentSrcName = UvPvDiagAction.getPCSrcNameById(parentSrcId, user);
			this.alipay_trade_num = values.get(8);
			this.alipay_auction_num = values.get(9);
			this.alipay_winner_num = values.get(14);
			this.alipay_trade_amt = values.get(15);
		}
		
	}
	
	public static List<PCSrcUvPv> sort(List<PCSrcUvPv> srcLevelOne, List<PCSrcUvPv> srcLevelTwo){
	    List<PCSrcUvPv> srcUvPvs = new ArrayList<PCSrcUvPv>();
        
        for (PCSrcUvPv w : srcLevelOne) {
            srcUvPvs.add(w);
            String parentSrcId = w.getSrcId();
            List<PCSrcUvPv> temp = new ArrayList<PCSrcUvPv>();
            for (PCSrcUvPv srcUvPv : srcLevelTwo) {
                if(parentSrcId.equals(srcUvPv.getParentSrcId())){
                    temp.add(srcUvPv);
                }
            }
            // 根据访问数进行排序
            Collections.sort(temp, new Comparator<PCSrcUvPv>() {

                @Override
                public int compare(PCSrcUvPv o1, PCSrcUvPv o2) {
                    String uv1 = o1.getUv();
                    String uv2 = o2.getUv();
                    return Integer.valueOf(uv1) > Integer.valueOf(uv2) ? -1 : 1;
                }  
                
            });
            for (PCSrcUvPv srcUvPv : temp) {
                srcUvPvs.add(srcUvPv);
            }
        }
        return srcUvPvs;
	}

	public String getThedate() {
		return thedate;
	}

	public void setThedate(String thedate) {
		this.thedate = thedate;
	}

	public String getSrcId() {
		return srcId;
	}

	public void setSrcId(String srcId) {
		this.srcId = srcId;
	}

	public String getBounce_count() {
		return bounce_count;
	}

	public void setBounce_count(String bounce_count) {
		this.bounce_count = bounce_count;
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

	public String getSrcName() {
		return srcName;
	}

	public void setSrcName(String srcName) {
		this.srcName = srcName;
	}

	public String getParentSrcId() {
		return parentSrcId;
	}

	public void setParentSrcId(String parentSrcId) {
		this.parentSrcId = parentSrcId;
	}

	public String getParentSrcName() {
		return parentSrcName;
	}

	public void setParentSrcName(String parentSrcName) {
		this.parentSrcName = parentSrcName;
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
	
	
}
