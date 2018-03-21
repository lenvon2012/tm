package models.itemCopy.dto;

public class PddLoginDto {
	
	private String username;
	
	private String password;
	
	private String passId;
	
	private String mallId;

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getPassId() {
		return passId;
	}

	public String getMallId() {
		return mallId;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setPassId(String passId) {
		this.passId = passId;
	}

	public void setMallId(String mallId) {
		this.mallId = mallId;
	}

	public PddLoginDto() {
		super();
	}

	public PddLoginDto(String username, String password, String passId,
			String mallId) {
		super();
		this.username = username;
		this.password = password;
		this.passId = passId;
		this.mallId = mallId;
	}

	@Override
	public String toString() {
		return "PddLoginDto [username=" + username + ", password=" + password
				+ ", passId=" + passId + ", mallId=" + mallId + "]";
	}
	
	

}
