package actions.itemcopy.model;

/**
 * Created by ZhuQianli on 2018/1/24.
 */
public class SkuCoreInfo {
    String skuId;
    String originalPrice;
    String salePrice;
    String quantity;
    String propPath;

    public SkuCoreInfo(String skuId, String originalPrice, String salePrice, String quantity) {
        this.skuId = skuId;
        this.originalPrice = originalPrice;
        this.salePrice = salePrice;
        this.quantity = quantity;
    }

    public SkuCoreInfo(String skuId, String propPath) {
        this.skuId = skuId;
        this.propPath = propPath;
    }

    public String getSkuId() {
        return skuId;
    }

    public SkuCoreInfo setSkuId(String skuId) {
        this.skuId = skuId;
        return this;
    }

    public String getOriginalPrice() {
        return originalPrice;
    }

    public SkuCoreInfo setOriginalPrice(String originalPrice) {
        this.originalPrice = originalPrice;
        return this;
    }

    public String getSalePrice() {
        return salePrice;
    }

    public SkuCoreInfo setSalePrice(String salePrice) {
        this.salePrice = salePrice;
        return this;
    }

    public String getQuantity() {
        return quantity;
    }

    public SkuCoreInfo setQuantity(String quantity) {
        this.quantity = quantity;
        return this;
    }

    public String getPropPath() {
        return propPath;
    }

    public SkuCoreInfo setPropPath(String propPath) {
        this.propPath = propPath;
        return this;
    }
}
