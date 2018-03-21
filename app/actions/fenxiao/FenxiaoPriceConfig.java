package actions.fenxiao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FenxiaoPriceConfig {
    
    private static final Logger log = LoggerFactory.getLogger(FenxiaoPriceConfig.class);

    private FenxiaoModifyPriceType modifyType;
    private double modifyParameter;
    
    private FenxiaoSkuEditType skuEditType;
    
    private double minProfit;
    
    private FenxiaoPriceOverflowType overType;
    private FenxiaoDecimalType decimalType;
    
    
    
    
    public FenxiaoModifyPriceType getModifyType() {
        return modifyType;
    }

    public void setModifyType(FenxiaoModifyPriceType modifyType) {
        this.modifyType = modifyType;
    }

    public double getModifyParameter() {
        return modifyParameter;
    }

    public void setModifyParameter(double modifyParameter) {
        this.modifyParameter = modifyParameter;
    }

    public FenxiaoSkuEditType getSkuEditType() {
        return skuEditType;
    }

    public void setSkuEditType(FenxiaoSkuEditType skuEditType) {
        this.skuEditType = skuEditType;
    }

    public double getMinProfit() {
        return minProfit;
    }

    public void setMinProfit(double minProfit) {
        this.minProfit = minProfit;
    }

    public FenxiaoPriceOverflowType getOverType() {
        return overType;
    }

    public void setOverType(FenxiaoPriceOverflowType overType) {
        this.overType = overType;
    }

    public FenxiaoDecimalType getDecimalType() {
        return decimalType;
    }

    public void setDecimalType(FenxiaoDecimalType decimalType) {
        this.decimalType = decimalType;
    }

    
    
    
    
    
    
    
    public enum FenxiaoModifyPriceType {
        lowRetailPercentAdd, lowRetailFixedAdd,
        highRetailPercentMinus, highRetailFixedMinus,
        costPricePercentAdd, costPriceFixedAdd,
        originPricePercentAdd, originPriceFixedAdd,
        originPricePercentMinus, originPriceFixedMinus
    }
    
    public enum FenxiaoSkuEditType {
        skipSkuItems, //跳过这个sku宝贝
        //sameWithPrice, //和一口价一样
        doEditToSku //sku也根据配置的幅度进行改价
    }
    
    public enum FenxiaoPriceOverflowType {
        //notModifyPrice,
        useHighRetailPrice, useLowRetailPrice
    }
    
    public enum FenxiaoDecimalType {
        noDecimal, //取整
        oneDecimal, //一位小数
        twoDecimal, //两位小数
    }

 
    public enum FenxiaoSubmitItemType {
        selectedItems, allSearchItem
    }
    
}
