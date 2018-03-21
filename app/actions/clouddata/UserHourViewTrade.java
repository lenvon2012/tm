package actions.clouddata;

import java.util.List;

import javax.persistence.Entity;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.JDBCBuilder;

import com.taobao.api.domain.QueryRow;

@Entity(name=UserHourViewTrade.TABLE_NAME)
public class UserHourViewTrade extends Model {

	private final static Logger log = LoggerFactory
			.getLogger(UserHourViewTrade.class);

	public final static String TABLE_NAME = "user_hour_view_trade";

	public Long userId;

	public String thedate;

	@Index(name = "thehour")
	public int thehour;	

	@Index(name = "catId")
	public int catId;
	
	public int visit_platform;

	public Long pv;

	public Long uv;

	public Long ipv;

	public Long iuv;

	public Long view_repeat_num;

	public Long alipay_trade_num;

	public Long alipay_auction_num;

	public double alipay_trade_amt;

	public Long alipay_winner_num;

	public Long gmv_auction_num;

	public Long gmv_trade_num;

	public Long gmv_winner_num;

	public double gmv_trade_amt;

	/*  row : 
	 *  thehour, seller_id,visit_platform, sum(pv), sum(uv), sum(ipv), sum(iuv), sum(view_repeat_num),
	 *  sum(alipay_trade_num), sum(alipay_auction_num), sum(alipay_trade_amt), 
	 *  sum(alipay_winner_num), sum(gmv_trade_num), sum(gmv_auction_num), sum(gmv_trade_amt), 
	 *  sum(gmv_winner_num)*/ 
	
	/*  对应的值 : "0","376767139","1","70","40","58","40","5","0","0","0","0","0","0","0","0"*/
	public UserHourViewTrade(QueryRow row, int catId, String thedate) {
		super();
		this.catId = catId;
		if(row != null) {
			List<String> values = row.getValues();
			this.userId = Long.valueOf(values.get(1));
			this.thedate = thedate;
			this.thehour = Integer.valueOf(values.get(0));
			this.visit_platform = Integer.valueOf(values.get(2));
			this.pv = Long.valueOf(values.get(3));
			this.uv = Long.valueOf(values.get(4));
			this.ipv = Long.valueOf(values.get(5));
			this.iuv = Long.valueOf(values.get(6));
			this.view_repeat_num = Long.valueOf(values.get(7));
			if(values.size() > 8) {
				this.alipay_trade_num = Long.valueOf(values.get(8));
				this.alipay_auction_num = Long.valueOf(values.get(9));
				this.alipay_trade_amt = Double.valueOf(values.get(10));
				this.alipay_winner_num = Long.valueOf(values.get(11));
				this.gmv_auction_num = Long.valueOf(values.get(13));
				this.gmv_trade_num = Long.valueOf(values.get(12));
				this.gmv_winner_num = Long.valueOf(values.get(15));
				this.gmv_trade_amt = Double.valueOf(values.get(14));
			}
			
		}
	}
	
	public UserHourViewTrade(Long userId, String thedate, int thehour, int catId, 
			int visit_platform, Long pv, Long uv, Long ipv, Long iuv,
			Long view_repeat_num, Long alipay_trade_num,
			Long alipay_auction_num, double alipay_trade_amt,
			Long alipay_winner_num, Long gmv_auction_num, Long gmv_trade_num,
			Long gmv_winner_num, double gmv_trade_amt) {
		super();
		this.userId = userId;
		this.thedate = thedate;
		this.thehour = thehour;
		this.catId = catId;
		this.visit_platform = visit_platform;
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
		this.gmv_trade_num = gmv_trade_num;
		this.gmv_winner_num = gmv_winner_num;
		this.gmv_trade_amt = gmv_trade_amt;
	}

	static String EXIST_ID_QUERY = "select userId from " + TABLE_NAME
            + " where  userId = ? and thehour = ? and visit_platform = ?";

    public static long findExistId(long userId, int thehour, int visit_platform) {
        return JDBCBuilder.singleLongQuery(EXIST_ID_QUERY, userId, thehour, visit_platform);
    }

    public boolean jdbcSave() {
        try {
            long existdId = findExistId(this.userId, this.thehour, this.visit_platform);

            if (existdId == 0L) {
                return this.rawInsert();
            } else {
                return this.rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }
    }

    /*
	 * p.thedate,p.thehour, p.seller_id,p.visit_platform, p.pv, p.uv, p.ipv,
	 * p.iuv, p.view_repeat_num, alipay_trade_num, alipay_auction_num,
	 * alipay_trade_amt, alipay_winner_num, gmv_auction_num, gmv_trade_amt,
	 * gmv_trade_num, gmv_winner_num
	 */
    
    public boolean rawInsert() {
        // TODO Auto-generated method stub
        long id = JDBCBuilder.insert("insert into `" + TABLE_NAME
                + "`(`thedate`,`thehour`,`catId`,`userId`,`visit_platform`,`pv`,`uv`" +
                ",`ipv`,`iuv`,`view_repeat_num`,`alipay_trade_num`,`alipay_auction_num`,`alipay_trade_amt`" +
                ",`alipay_winner_num`,`gmv_auction_num`,`gmv_trade_num`,`gmv_winner_num`,`gmv_trade_amt`) " +
                "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", this.thedate, this.thehour, this.catId,
                this.userId, this.visit_platform, this.pv, this.uv, this.ipv, this.iuv, 
                this.view_repeat_num, this.alipay_trade_num, this.alipay_auction_num,
                this.alipay_trade_amt, this.alipay_winner_num, this.gmv_auction_num, this.gmv_trade_num,
                this.gmv_winner_num, this.gmv_trade_amt);

        if (id > 0L) {
            return true;
        } else {
            return false;
        }
    }

    public boolean rawUpdate() {
        long updateNum = JDBCBuilder.insert("update `" + TABLE_NAME
                + "` set  `thedate` = ?, `thehour` = ?,  `catId` = ?," +
                "`visit_platform` = ?, `pv` = ?, `uv` = ?, `ipv` = ?, `iuv` = ?," +
                "`view_repeat_num` = ?, `alipay_trade_num` = ?, `alipay_auction_num` = ?, " +
                "`alipay_trade_amt` = ?, `alipay_winner_num` = ?, `gmv_auction_num` = ?, " +
                "`gmv_trade_num` = ?, `gmv_winner_num` = ?, `gmv_trade_amt` = ? where userId = ? ",
                this.thedate, this.thehour, this.catId, this.visit_platform, this.pv,
                this.uv, this.ipv, this.iuv, this.view_repeat_num,
                this.alipay_trade_num, this.alipay_auction_num, this.alipay_trade_amt, this.alipay_winner_num,
                this.gmv_auction_num, this.gmv_trade_num, this.gmv_winner_num, this.gmv_trade_amt,
                this.userId);

        if (updateNum > 0L) {
            return true;
        } else {
            return false;
        }
    }
    
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getThedate() {
		return thedate;
	}

	public void setThedate(String thedate) {
		this.thedate = thedate;
	}

	public int getThehour() {
		return thehour;
	}

	public void setThehour(int thehour) {
		this.thehour = thehour;
	}

	public int getCatId() {
		return catId;
	}

	public void setCatId(int catId) {
		this.catId = catId;
	}

	public int getVisit_platform() {
		return visit_platform;
	}

	public void setVisit_platform(int visit_platform) {
		this.visit_platform = visit_platform;
	}

	public Long getPv() {
		return pv;
	}

	public void setPv(Long pv) {
		this.pv = pv;
	}

	public Long getUv() {
		return uv;
	}

	public void setUv(Long uv) {
		this.uv = uv;
	}

	public Long getIpv() {
		return ipv;
	}

	public void setIpv(Long ipv) {
		this.ipv = ipv;
	}

	public Long getIuv() {
		return iuv;
	}

	public void setIuv(Long iuv) {
		this.iuv = iuv;
	}

	public Long getView_repeat_num() {
		return view_repeat_num;
	}

	public void setView_repeat_num(Long view_repeat_num) {
		this.view_repeat_num = view_repeat_num;
	}

	public Long getAlipay_trade_num() {
		return alipay_trade_num;
	}

	public void setAlipay_trade_num(Long alipay_trade_num) {
		this.alipay_trade_num = alipay_trade_num;
	}

	public Long getAlipay_auction_num() {
		return alipay_auction_num;
	}

	public void setAlipay_auction_num(Long alipay_auction_num) {
		this.alipay_auction_num = alipay_auction_num;
	}

	public double getAlipay_trade_amt() {
		return alipay_trade_amt;
	}

	public void setAlipay_trade_amt(double alipay_trade_amt) {
		this.alipay_trade_amt = alipay_trade_amt;
	}

	public Long getAlipay_winner_num() {
		return alipay_winner_num;
	}

	public void setAlipay_winner_num(Long alipay_winner_num) {
		this.alipay_winner_num = alipay_winner_num;
	}

	public Long getGmv_auction_num() {
		return gmv_auction_num;
	}

	public void setGmv_auction_num(Long gmv_auction_num) {
		this.gmv_auction_num = gmv_auction_num;
	}

	public Long getGmv_trade_num() {
		return gmv_trade_num;
	}

	public void setGmv_trade_num(Long gmv_trade_num) {
		this.gmv_trade_num = gmv_trade_num;
	}

	public Long getGmv_winner_num() {
		return gmv_winner_num;
	}

	public void setGmv_winner_num(Long gmv_winner_num) {
		this.gmv_winner_num = gmv_winner_num;
	}

	public double getGmv_trade_amt() {
		return gmv_trade_amt;
	}

	public void setGmv_trade_amt(double gmv_trade_amt) {
		this.gmv_trade_amt = gmv_trade_amt;
	}

}
