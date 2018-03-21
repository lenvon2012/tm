package actions.clouddata;

import java.util.List;

import com.taobao.api.domain.QueryRow;

public class UserDailyUvPv {
	
	public String theDate;
	
	public String shopId; 
	
	public String sellerId;
	
	public String numIid;
	
	public String uv;
	
	public String pv;
	
	public UserDailyUvPv() {
		super();
	}
	
	public UserDailyUvPv(QueryRow row) {
		if(row != null) {
			List<String> values = row.getValues();
			this.theDate = values.get(0);
			this.shopId = values.get(1);
			this.sellerId = values.get(2);
			this.numIid = values.get(3);
			this.uv = values.get(4);
			this.pv = values.get(5);
		}
	}
}
