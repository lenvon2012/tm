package models.itemCopy.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.taobao.api.domain.ItemImg;
import com.taobao.api.domain.ItemProp;

import controllers.newAutoTitle;

/**
 * 销售属性dto
 * @author oyster
 *
 */
public class SalePropDto {
	
	public SalePropDto() {
		super();
	}


	public SalePropDto(List<SalePropModel> colorProps,
			List<SalePropModel> otherProps) {
		super();
		this.colorProps = colorProps;
		this.otherProps = otherProps;
	}


	/**
	 * 颜色销售属性
	 */
	private List<SalePropModel> colorProps=new ArrayList<SalePropModel>();
	
	/**
	 * 其他销售属性
	 */
	private List<SalePropModel> otherProps=new ArrayList<SalePropModel>();


	public List<SalePropModel> getColorProps() {
		return colorProps;
	}


	public void setColorProps(List<SalePropModel> colorProps) {
		this.colorProps = colorProps;
	}


	public List<SalePropModel> getOtherProps() {
		return otherProps;
	}


	public void setOtherProps(List<SalePropModel> otherProps) {
		this.otherProps = otherProps;
	}


	@Override
	public String toString() {
		return "SalePropDto [colorProps=" + colorProps + ", otherProps="
				+ otherProps + "]";
	}

	
	
	
	
}
