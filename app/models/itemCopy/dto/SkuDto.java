package models.itemCopy.dto;

import java.util.List;

import com.taobao.api.domain.PropImg;
import com.taobao.api.domain.Sku;

/**
 * Sku相关dto
 * @author oyster
 *
 */
public class SkuDto {
	
	private List<Sku> skus;
	
	private String inputCustomCpvs;
	
	private List<PropImg> propImgs;

	

	public SkuDto(List<Sku> skus, String inputCustomCpvs, List<PropImg> propImgs) {
		super();
		this.skus = skus;
		this.inputCustomCpvs = inputCustomCpvs;
		this.setPropImgs(propImgs);
	}

	public SkuDto() {
		super();
	}

	public List<Sku> getSkus() {
		return skus;
	}

	public void setSkus(List<Sku> skus) {
		this.skus = skus;
	}

	public String getInputCustomCpvs() {
		return inputCustomCpvs;
	}

	public void setInputCustomCpvs(String inputCustomCpvs) {
		this.inputCustomCpvs = inputCustomCpvs;
	}

	public List<PropImg> getPropImgs() {
		return propImgs;
	}

	public void setPropImgs(List<PropImg> propImgs) {
		this.propImgs = propImgs;
	}
	
	
	
	

}
