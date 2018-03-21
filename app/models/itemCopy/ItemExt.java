package models.itemCopy;

import com.taobao.api.domain.Item;

import java.util.List;
import java.util.Map;

import models.itemCopy.dto.SalePropDto;

/**
 * Created by User on 2017/11/1.
 */
public class ItemExt extends Item {

    String skuProperties;
    String inputCustomCpv;
    String skuPrices;
    String skuQuantities;
    String skuOuterIds;
    SalePropDto salePropDto;  //scheme体系的销售属性
    private Long brand; //天猫品牌ID
    PriceUnit[] priceUnitsForPrice; // 如果数组PriceUnit长度为2 则索引0是折扣价 索引1是原价
    List<PriceUnit[]> priceUnitsForSkuPrice; // 如果数组PriceUnit长度为2 则索引0是折扣价 索引1是原价

    public String getSkuProperties() {
        return skuProperties;
    }

    public ItemExt setSkuProperties(String skuProperties) {
        this.skuProperties = skuProperties;
        return this;
    }

    public String getInputCustomCpv() {
        return inputCustomCpv;
    }

    public ItemExt setInputCustomCpv(String inputCustomCpv) {
        this.inputCustomCpv = inputCustomCpv;
        return this;
    }

    public String getSkuPrices() {
        return skuPrices;
    }

    public ItemExt setSkuPrices(String skuPrices) {
        this.skuPrices = skuPrices;
        return this;
    }

    public String getSkuQuantities() {
        return skuQuantities;
    }

    public ItemExt setSkuQuantities(String skuQuantities) {
        this.skuQuantities = skuQuantities;
        return this;
    }

    public String getSkuOuterIds() {
        return skuOuterIds;
    }

    public ItemExt setSkuOuterIds(String skuOuterIds) {
        this.skuOuterIds = skuOuterIds;
        return this;
    }

    public PriceUnit[] getPriceUnitsForPrice() {
        return priceUnitsForPrice;
    }

    public ItemExt setPriceUnitsForPrice(PriceUnit[] priceUnitsForPrice) {
        this.priceUnitsForPrice = priceUnitsForPrice;
        return this;
    }

    public List<PriceUnit[]> getPriceUnitsForSkuPrice() {
        return priceUnitsForSkuPrice;
    }

    public ItemExt setPriceUnitsForSkuPrice(List<PriceUnit[]> priceUnitsForSkuPrice) {
        this.priceUnitsForSkuPrice = priceUnitsForSkuPrice;
        return this;
    }

	public SalePropDto getSalePropDto() {
		return salePropDto;
	}

	public void setSalePropDto(SalePropDto salePropDto) {
		this.salePropDto = salePropDto;
	}

	public Long getBrand() {
		return brand;
	}

	public void setBrand(Long brand) {
		this.brand = brand;
	}
    
    
}
