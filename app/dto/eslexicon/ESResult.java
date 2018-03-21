package dto.eslexicon;

/**
 * @author:Young
 * @date:2016年7月14日下午7:21:13
 * @Description:
 */
public class ESResult {
	
	boolean success;
	
	String msg;

	public ESResult() {
	}

	public ESResult(Boolean success, String msg) {
		this.success = success;
		this.msg = msg;
	}
	
	public boolean isSuccess() {
		return success;
	}
	
	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	public String getMsg() {
		return msg;
	}
	
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
}
