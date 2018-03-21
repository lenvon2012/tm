package models.itemCopy.dto;

/**
 * 属性Dto
 * @author oyster
 *
 */
public class PropDto {
	
	private String props;
	
	private String inputPids;
	
	private String inputStr;

	public String getProps() {
		return props;
	}

	public void setProps(String props) {
		this.props = props;
	}

	public String getInputPids() {
		return inputPids;
	}

	public void setInputPids(String inputPids) {
		this.inputPids = inputPids;
	}

	public String getInputStr() {
		return inputStr;
	}

	public void setInputStr(String inputStr) {
		this.inputStr = inputStr;
	}

	public PropDto(String props, String inputPids, String inputStr) {
		super();
		this.props = props;
		this.inputPids = inputPids;
		this.inputStr = inputStr;
	}

	public PropDto() {
		super();
	}
	
	

}
