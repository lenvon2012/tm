package actions.clouddata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import sun.awt.SunHints.Value;
import models.user.User;
import actions.UvPvDiagAction;

import com.sun.org.apache.xpath.internal.operations.Bool;
import com.taobao.api.domain.QueryRow;

public class EntranceResult implements Serializable{
	
	public String thedate;
	
	public String sellerId;
	
	public String itemId;
	
	public String word;
	
	public String pv;
	
	public String uv;
	
	public boolean isPC;
	
	public EntranceResult(QueryRow row, Boolean isPC, User user) {
		super();
		if(row != null) {
			List<String> values = row.getValues();
			this.thedate = values.get(0);
			this.sellerId = values.get(1);
			this.itemId = values.get(2);
			this.word = values.get(3);
			this.pv = values.get(4);
			this.uv = values.get(5);
			this.isPC = isPC;
		}
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

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
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

	public boolean isPC() {
		return isPC;
	}

	public void setPC(boolean isPC) {
		this.isPC = isPC;
	}

}
