package actions.itemcopy.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ZhuQianli on 2018/1/24.
 */
public class ResultBean {
    // 商品标题
    private String title;
    // 商品cid
    private String categoryId;
    // 商品主副图
    private List<String> images = new ArrayList<String>();
    // 卖家昵称
    private String sellerNick;
    // 商品普通属性
    private List<PropInfo> prop = new ArrayList<PropInfo>();
    // 商品销售属性
    private List<SkuPropInfo> skuProp = new ArrayList<SkuPropInfo>();
    // 商品销售细节信息(skuId为0的数据表示商品价格和总库存)
    private Map<String, SkuCoreInfo> skuCore = new HashMap<String, SkuCoreInfo>();

    public String getTitle() {
        return title;
    }

    public ResultBean setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public ResultBean setCategoryId(String categoryId) {
        this.categoryId = categoryId;
        return this;
    }

    public List<String> getImages() {
        return images;
    }

    public ResultBean setImages(List<String> images) {
        this.images = images;
        return this;
    }

    public String getSellerNick() {
        return sellerNick;
    }

    public ResultBean setSellerNick(String sellerNick) {
        this.sellerNick = sellerNick;
        return this;
    }

    public List<PropInfo> getProp() {
        return prop;
    }

    public ResultBean setProp(List<PropInfo> prop) {
        this.prop = prop;
        return this;
    }

    public List<SkuPropInfo> getSkuProp() {
        return skuProp;
    }

    public ResultBean setSkuProp(List<SkuPropInfo> skuProp) {
        this.skuProp = skuProp;
        return this;
    }

    public Map<String, SkuCoreInfo> getSkuCore() {
        return skuCore;
    }

    public ResultBean setSkuCore(Map<String, SkuCoreInfo> skuCore) {
        this.skuCore = skuCore;
        return this;
    }
}
