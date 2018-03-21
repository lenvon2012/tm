package bustbapi;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.carrierTask.ItemCarryCustom;
import models.itemCopy.ItemExt;
import models.itemCopy.PriceUnit;
import models.user.User;

import org.apache.commons.lang3.StringUtils;

import result.TMResult;
import carrier.FileCarryUtils;

import com.alibaba.fastjson.JSON;
import com.ciaosir.client.CommonUtils;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.ItemImg;
import com.taobao.api.domain.Picture;
import com.taobao.api.domain.PropImg;

/**
 * Created by User on 2017/11/10.
 */
public class ItemCopyApiForCustom extends ItemCopyApi {

    // 用户的自定义设置数据
    private ItemCarryCustom itemCarryCustom;

    // 价格自定义中使用
    private Map<String, String> priceConvert;

    public ItemCopyApiForCustom(User user, Item item, ItemCarryCustom itemCarryCustom) {
        super(user, item);
        this.itemCarryCustom = itemCarryCustom;
    }

    @Override
    protected void check() {
        super.check();
        if (itemCarryCustom == null) throw new ItemCopyApiException("获取配置信息失败，请联系我们！", null);

        ItemExt itemExt = (ItemExt) item;
        if (itemCarryCustom.getCalPriceBaseNumType() != null) {
            List<PriceUnit[]> priceUnitsList = new ArrayList<PriceUnit[]>();
            if (itemExt.getPriceUnitsForPrice() != null) {
                priceUnitsList.add(itemExt.getPriceUnitsForPrice());
            }
            if (!CommonUtils.isEmpty(itemExt.getPriceUnitsForSkuPrice())) {
                priceUnitsList.addAll(itemExt.getPriceUnitsForSkuPrice());
            }

            if (!CommonUtils.isEmpty(priceUnitsList)) {
                this.priceConvert = getPriceConvert(priceUnitsList);
            }
        }
    }

    @Override
    protected void reqSetLocationState() {
        String locationState = itemCarryCustom.getLocationState();
        String locationCity = itemCarryCustom.getLocationCity();
        if (StringUtils.isNotEmpty(locationState) && StringUtils.isNotEmpty(locationCity)) {
            req.setLocationState(locationState);
        } else {
            super.reqSetLocationState();
        }
    }

    @Override
    protected void reqSetLocationCity() {
        String locationState = itemCarryCustom.getLocationState();
        String locationCity = itemCarryCustom.getLocationCity();
        if (StringUtils.isNotEmpty(locationState) && StringUtils.isNotEmpty(locationCity)) {
            req.setLocationCity(locationCity);
        } else {
            super.reqSetLocationCity();
        }

    }

    @Override
    protected void reqSetPostageId() {
        Long postageId = itemCarryCustom.getPostageId();
        if (postageId != null) {
            req.setPostageId(postageId);
        } else {
            super.reqSetPostageId();
        }
    }

    @Override
    protected void reqSetStuffStatus() {
        ItemCarryCustom.StuffStatus stuffStatus = itemCarryCustom.getStuffStatus();
        if (stuffStatus != null) {
            switch (stuffStatus) {
                case 二手:
                    req.setStuffStatus("second");
                    break;
                case 新品:
                    req.setStuffStatus("new");
                    break;
            }
        } else {
            super.reqSetStuffStatus();
        }
    }

    @Override
    protected void reqSetApproveStatus() {
        ItemCarryCustom.ApproveStatus approveStatus = itemCarryCustom.getApproveStatus();
        if (approveStatus != null) {
            req.setApproveStatus(approveStatus.name());
        } else {
            super.reqSetApproveStatus();
        }
    }

    @Override
    protected void reqSetTitle() {
        String title = item.getTitle();

        String titleKeywordMapper = itemCarryCustom.getTitleKeywordMapper();
        if (StringUtils.isNotEmpty(titleKeywordMapper)) {
            try {
                Map<String, String> mapper = JSON.parseObject(itemCarryCustom.getTitleKeywordMapper(), Map.class);
                for (Map.Entry<String, String> kv : mapper.entrySet()) {
                    String keyword = kv.getKey();
                    String replacement = kv.getValue();
                    title = title.replace(keyword, replacement);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String prefixTitleString = itemCarryCustom.getPrefixTitleString();
        if (StringUtils.isNotEmpty(prefixTitleString)) {
            title = prefixTitleString + title;
        }

        String suffixTitleString = itemCarryCustom.getSuffixTitleString();
        if (StringUtils.isNotEmpty(suffixTitleString)) {
            title = title + suffixTitleString;
        }

        int titleSize = 0;
        try {
            titleSize = title.getBytes("GBK").length;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (titleSize > 60) {
            throw new ItemCopyApiException("【" + title + "】自定义后的标题字节数为" + titleSize + "超过60个字节,字母符号等于一字节,汉字等于两字节", null);
        }

        req.setTitle(title);
    }

    @Override
    protected void reqSetPrice() {
        // 使用ItemCopyAPiBase中处理过的Price
        super.reqSetPrice();
        String price = req.getPrice();
        // 获取自定义类型的价格数值
        if (priceConvert != null) {
            String newPrice = priceConvert.get(decimalFormat(price));
            price = newPrice != null ? newPrice : price;
        }

        req.setPrice(price);
    }

    @Override
    protected void reqSetSkuPrices() {
        // 使用ItemCopyAPiBase中处理过的skuPrices
        super.reqSetSkuPrices();
        if (StringUtils.isEmpty(req.getSkuPrices())) return;

        String[] skuPrices = req.getSkuPrices().split(",");
        StringBuilder newSkuPrices = new StringBuilder();
        for (String skuPrice : skuPrices) {
            // 获取自定义类型的价格数值
            if (priceConvert != null) {
                String skuPriceConvert = priceConvert.get(decimalFormat(skuPrice));
                if (StringUtils.isNotEmpty(skuPriceConvert)) skuPrice = skuPriceConvert;
            }
            newSkuPrices.append(",").append(skuPrice);
        }

        req.setSkuPrices(newSkuPrices.substring(1));
    }

    // 根据自定义的价格类型来 建立Map(key：原价数值  value：自定义类型的价格数值)
    private Map<String, String> getPriceConvert(List<PriceUnit[]> priceUnitsList) {
        Integer length = priceUnitsList.get(0).length;
        Integer sourceIndex; // 原价索引
        Integer targetIndex; // calPriceBaseNumType对应的价格索引
        if (length == 1) {
            sourceIndex = 0;
            targetIndex = 0;
        } else {
            sourceIndex = 1;
            switch (itemCarryCustom.getCalPriceBaseNumType()) {
                case 原价:
                    targetIndex = 1;
                    break;
                case 折扣价:
                    targetIndex = 0;
                    break;
                default:
                    targetIndex = 11;
            }
        }

        Map<String, String> priceConvert = new HashMap<String, String>();
        for (PriceUnit[] priceUnits : priceUnitsList) {
            try {
                String sourcePrice = priceUnits[sourceIndex].getPrice();
                String targetPrice = priceUnits[targetIndex].getPrice();
                targetPrice = calculatePrice(targetPrice);
                priceConvert.put(decimalFormat(sourcePrice), decimalFormat(targetPrice));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return priceConvert;
    }

    // 根据公式计算价格
    private String calculatePrice(String skuPrice) {
        BigDecimal basePrice = new BigDecimal(skuPrice);

        BigDecimal result = basePrice;
        Double multiplyNum = itemCarryCustom.getMultiplyNum();
        if (multiplyNum != null) {
            result = result.multiply(new BigDecimal(multiplyNum));
        }

        Double addNum = itemCarryCustom.getAddNum();
        if (addNum!= null) {
            result = result.add(new BigDecimal(addNum));
        }

        return result.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

    public String decimalFormat(String str) {
        return decimalFormat.format(Double.valueOf(str));
    }

    @Override
    protected void reqSetOuterId() {
        try {
            Boolean whetherSaveOldNumiid = itemCarryCustom.getWhetherSaveOldNumiid();
            if (whetherSaveOldNumiid != null && whetherSaveOldNumiid) {
                Long numIid = item.getNumIid();
                if (numIid != null) req.setOuterId(numIid.toString());
            } else {
                ;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void reqSetPicPath() {
        Long mainPicIndex = itemCarryCustom.getMainPicIndex();
        if (mainPicIndex != null) {
            Long index;
            // 随机主图逻辑
            if (mainPicIndex > 0L && mainPicIndex < 6L) index = mainPicIndex;
            else index = (long) (new Random().nextInt(5) + 1);
            // 获取主图url
            List<ItemImg> itemImgs = item.getItemImgs();
            ItemImg itemImg = null;
            for (int i = 0; i < itemImgs.size(); i++) {
                if (itemImgs.get(i).getPosition() == index) {
                    itemImg = itemImgs.get(i);
                    break;
                }
            }
            // 如果找不到自定义设置的主图 默认第一张
            if (itemImg == null) {
                for (int i = 0; i < itemImgs.size(); i++) {
                    if (itemImgs.get(i).getPosition() == 1L) {
                        itemImg = itemImgs.get(i);
                        break;
                    }
                }
            }
            if (itemImg == null) throw new ItemCopyApiException("未找到原宝贝主图", null);
            // 当随机主图时,将随机的结果返回去 上传附图使用
            itemCarryCustom.setMainPicIndex(index);
            // 上传主图
            TMResult<Picture> picUrlResult = FileCarryUtils.newUploadPicFromOnline(user, itemImg.getUrl(), itemCarryCustom.getLongPictureCategoryId());
            // 上传失败 抛出异常
            if(!picUrlResult.isOk) throw new ItemCopyApiException(picUrlResult.msg, null);
            req.setPicPath(picUrlResult.getRes() == null? StringUtils.EMPTY : picUrlResult.getRes().getPicturePath());
        } else {
            super.reqSetPicPath();
        }

    }

    @Override
    protected void reqSetDesc() {
        TMResult descResult = FileCarryUtils.newFilterDesc(user, item.getDesc(), itemCarryCustom.getLongPictureCategoryId());
        if (!descResult.isOk) {
            throw new ItemCopyApiException(descResult.msg, null);
        }
        String desc = descResult.msg + "<p style=\"height:0px;margin:0px;color:#ffffff;\">~~~~~</p>";

        // 过滤特殊关键字  替换成“*”
        Pattern p = Pattern.compile("顶级|第一|治愈|绝对");
        Matcher m = p.matcher(desc);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String group = m.group();
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < group.length(); i++, s.append("*")) ;
            m.appendReplacement(sb, s.toString());
        }
        m.appendTail(sb);
        desc = sb.toString();

        Boolean whetherSaveOldUrl = itemCarryCustom.getWhetherSaveOldUrl();
        if (whetherSaveOldUrl != null && whetherSaveOldUrl) {
            // 约定这里面放原宝贝的链接
            String itemUrl = item.getOuterId();
            if (!StringUtils.isEmpty(itemUrl)) {
                desc = "<div style=\"height:20px;color: #ffffff;\">" + itemUrl + "</div>" + desc;
            }
        }

        req.setDesc(desc);
    }

    @Override
    protected void reqSetSellerCids() {
        String sellerCids = itemCarryCustom.getSellerCidsLastOne();
        if (sellerCids != null) {
            req.setSellerCids(sellerCids);
        } else {
            super.reqSetSellerCids();
        }

    }

    @Override
    protected void afterReqExecute() {
        if (resp == null) return;
        // 宝贝上传成功  添加副图
        if (resp.isSuccess()) addPic();
    }

    // 添加副图
    private void addPic() {
        Long ignoreImgIndex = 1L;
        if (itemCarryCustom.getMainPicIndex() != null) {
            ignoreImgIndex = itemCarryCustom.getMainPicIndex();
        }

        //添加副图
        Item itemNow = resp.getItem();
        List<ItemImg> imgs = item.getItemImgs();
        if (!CommonUtils.isEmpty(imgs)) {
            for (ItemImg itemImg : imgs) {
                if (itemImg.getPosition() != null && itemImg.getPosition().longValue() == ignoreImgIndex) continue;
                new ItemApi.ItemImgPictureAdd(user, itemNow.getNumIid(), itemImg).call();
            }
        }

        //添加属性图片
        List<PropImg> propImgs = item.getPropImgs();
        if (!CommonUtils.isEmpty(propImgs)) {
            for (PropImg propImg : propImgs) new ItemApi.ItemPropPictureAdd(user, itemNow.getNumIid(), propImg).call();
        }
    }
}



