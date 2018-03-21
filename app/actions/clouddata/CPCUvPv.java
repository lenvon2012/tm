package actions.clouddata;

import java.io.Serializable;

public class CPCUvPv implements Serializable{
	
	public static final String DEFAULT_VALUE = "0";
	
	private String dt;

	private String uv;
	
	public String getDt() {
		return dt;
	}

	public void setDt(String dt) {
		this.dt = dt;
	}

	public String getUv() {
		return uv;
	}

	public void setUv(String uv) {
		this.uv = uv;
	}
	
	public CPCUvPv(){
		
	}
	
	public CPCUvPv(String dataTime){
		this.dt = dataTime;
		this.uv = DEFAULT_VALUE;
	}
	
	public void addSearchUv(String uvNew) {
		this.uv = String.valueOf(Integer.valueOf(this.uv) + Integer.valueOf(uvNew));
	}

}
