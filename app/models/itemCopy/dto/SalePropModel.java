package models.itemCopy.dto;

import com.taobao.api.domain.ItemImg;
import com.taobao.api.domain.PropImg;

/**
 * 销售属性
 * @author oyster
 *
 */
public class SalePropModel {

	private String id;
	
	private String value;
	
	private String aliasName;
	
	private String imgUrl;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getAliasName() {
		return aliasName;
	}

	public void setAliasName(String aliasName) {
		this.aliasName = aliasName;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}

	@Override
	public String toString() {
		return "SalePropModel [id=" + id + ", value=" + value + ", aliasName="
				+ aliasName + ", imgUrl=" + imgUrl + "]";
	}

	
	
	
}
