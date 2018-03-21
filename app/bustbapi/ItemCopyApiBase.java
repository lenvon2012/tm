package bustbapi;

import bustbapi.request.ItemAddRequest;
import carrier.FileCarryUtils;
import com.taobao.api.ApiException;
import com.taobao.api.TaobaoClient;
import com.taobao.api.domain.FoodSecurity;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.Location;
import com.taobao.api.domain.Picture;
import com.taobao.api.request.ItemUpdateRequest;
import com.taobao.api.response.ItemAddResponse;
import com.taobao.api.response.ItemUpdateResponse;
import models.itemCopy.ItemExt;
import models.user.User;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import result.TMResult;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ItemCopyApiBase {

    private static final Logger log = LoggerFactory.getLogger(ItemCopyApiBase.class);

    public static DecimalFormat decimalFormat = new DecimalFormat("#0.00");

    protected User user;
    protected Item item;
    protected ItemAddRequest req = new ItemAddRequest();
    protected TaobaoClient client = TBApi.genClient();
    protected ItemAddResponse resp;

    public ItemCopyApiBase(User user, Item item) {
        this.user = user;
        this.item = item;
    }

    public TMResult<Item> itemCopy() {
        try {
            check();
            reqSetNum();
            reqSetType();
            reqSetStuffStatus();
            reqSetTitle();
            reqSetLocationState();
            reqSetLocationCity();
            reqSetPostageId();
            reqSetApproveStatus();
            reqSetCid();
            reqSetProps();
            reqSetPropertyAlias();
            reqSetInputPids();
            reqSetInputStr();
            reqSetInputCustomCpv();
            reqSetSkuOuterIds();
            reqSetSkuPrices();
            reqSetSkuQuantities();
            reqSetSkuProperties();
            reqSetPrice();
            reqSetSomethingUseless();
            reqSetFee();
            reqSetAuctionPoint();
            reqSetFoodSecurity();
            reqSetPicPath();
            reqSetDesc();
            reqSetLang();
            reqSetOuterId();
            reqSetSellerCids();
            reqSetQualification();
            beforeReqExecute();
            reqExecute();
            afterReqExecute();
        } catch (ItemCopyApiException e) {
            return new TMResult<Item>(false, e.getMessage()+"---cid:"+req.getCid()+"---numIid:" + item.getNumIid(),null);
        } catch (ApiException e) {
            e.printStackTrace();
            return new TMResult(false, e.toString(), null);
        } catch (Exception e) {
            e.printStackTrace();
            return new TMResult(false, e.toString(), null);
        } finally {
            beforeReturn();
        }

     
        return new TMResult<Item>(true, "复制成功", resp.getItem());
    }

    protected void beforeReturn() {

    }

    protected void itemUpdate() {
        if (!resp.isSuccess()) return;
        ItemUpdateRequest itemUpdateRequest = new ItemUpdateRequest();
        itemUpdateRequest.setNumIid(resp.getItem().getNumIid());
        reqSetDesc();
        itemUpdateRequest.setDesc(req.getDesc());
        Integer retry = 3;
        ItemUpdateResponse itemUpdateResponse = null;
        do {
            try {
                itemUpdateResponse = client.execute(itemUpdateRequest, user.getSessionKey());
            } catch (Exception e) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ee) {
                    ee.printStackTrace();
                }
            }
        } while (itemUpdateRequest == null && retry-- > 0);

        if (itemUpdateResponse == null) throw new ItemCopyApiException("调用taobao.item.update接口失败", null);

        if (!itemUpdateResponse.isSuccess()) {
            throw new ItemCopyApiException("更新宝贝描述失败-" + itemUpdateResponse.getSubMsg(), null);
        }
    }

    protected void reqSetSellerCids() {

    }

    protected void reqSetOuterId() {

    }

    protected void reqSetPostageId() {

    }

    protected void beforeReqExecute() {
    }

    protected void reqSetQualification() {
        /*try {
            req.setQualification(URLEncoder.encode("证书编号： 2016010702896858", "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }*/
    }

    protected void reqSetProps() {
        String props = item.getProps();
        req.setProps(props);
    }

    protected void afterReqExecute() {

    }

    protected void reqExecute() throws ApiException, InterruptedException {
        reqCall(3);
        if (resp == null) throw new ItemCopyApiException("请求taobao.item.add接口失败", null);

        if (!resp.isSuccess()) retryReq();

        if (!resp.isSuccess()) throw new ItemCopyApiException(StringUtils.isNotEmpty(resp.getSubMsg()) ? resp.getSubMsg() : resp.getMsg(), null);
    }

    private void retryReq() {
        if (resp.isSuccess()) return;

        if ("isv.error-item-ifd-error".equals(resp.getSubCode()) //
                && resp.getSubMsg().startsWith("由于您发布的商品信息中存在品牌滥用如品牌不一致、堆砌等情况")) {
            req.setTitle(fixAbuseTitle());
            reqCall(3);
        } else if ("isv.invalid-parameter".equals(resp.getSubCode()) //
                && resp.getSubMsg().contains("最多支持传入属性值")) {
            req.setProps(fixNumLimitProps());
            reqCall(3);
        } else if ("isv.error-food-security-required".equals(resp.getSubCode())
                && resp.getSubMsg().contains("食品安全信息必须填写")) {
            setDefaultFoodSecurity(item.getFoodSecurity());
            reqCall(3);
        }
    }

    // 修复标题品牌滥用
    private String fixAbuseTitle() {
        String title = resp.getParams().get("title");
        String subMsg = resp.getSubMsg();
        // （包含但不限于k2summit/凯图巅峰、vibram）
        Pattern p = Pattern.compile("（(.+)）");
        Matcher matcher = p.matcher(subMsg);
        matcher.find();
        String brandsStr = matcher.group(1).replace("包含但不限于", "");
        String[] brands = brandsStr.split("、");
        String propsNames = item.getPropsName();
        String correctBrand = null;
        for (String propsName : propsNames.split(";")) {
            String[] strings = propsName.split(":");
            if (strings[0].equals("20000")) {
                correctBrand = strings[3];
            }
        }
        int i;
        if (correctBrand == null) return title;
        String newTitle = title;
        for (i = 0; i < brands.length; i++) {
            String brand = brands[i];
            if (brand.contains(correctBrand) || correctBrand.contains(brand)) continue;
            newTitle = removeBrandWord(newTitle, brand);
        }

        // 处理后的标题没有变化 与可能是音似或者形似 carkoci/卡古驰、Gucci/古奇
        // 直接去除标题中当前商品品牌的关键字
        if (newTitle.equals(title)) {
            for (int j = 0; j < brands.length; j++) {
                String brand = brands[j];
                if (brand.contains(correctBrand) || correctBrand.contains(brand))
                    newTitle = removeBrandWord(title, brand);
            }
        }

        return newTitle;
    }

    // 删除标题有关品牌的关键字
    private String removeBrandWord(String title, String brand) {
        String regexBrand = "";
        do {
            // vibram      K2summit凯图 Ed04女式登山鞋徒步户外越野跑鞋Vibram大底
            regexBrand = brand;
            if (Pattern.matches(".*((?i)" + regexBrand + ").*", title)) break;

            // 汉鼎（户外）
            regexBrand = brand.replaceAll("[\\(（].*?[\\)）]", "");
            if (Pattern.matches(".*((?i)" + regexBrand + ").*", title)) break;

            // k2summit/凯图巅峰
            if (brand.contains("/")) regexBrand = brand.replace("/", "|");
            if (Pattern.matches(".*((?i)" + regexBrand + ").*", title)) break;

            // UNOONN/优尼.充、S·GP    SGP iPhone6 plus手机壳保护套边框硅胶套超薄大黄蜂苹果6外壳
            if (brand.contains("·")) regexBrand = brand.replace("·", "");
            if (Pattern.matches(".*((?i)" + regexBrand + ").*", title)) break;

            return title;
        } while (false);
        Pattern pattern = Pattern.compile(regexBrand, Pattern.CASE_INSENSITIVE);

        return pattern.matcher(title).replaceAll("");
    }

    // 修复各别非销售属性属性值个数限制
    private String fixNumLimitProps() {
        String props = req.getProps();
        if (StringUtils.isEmpty(props)) return props;
        // 宝贝[561949608366]的属性[122216515]:[适用场景]最多支持传入属性值[2]个，实际传入了[4]个
        String regex = "^宝贝\\[(\\d+)\\]的属性\\[(\\d+)\\]:\\[(.+)\\]最多支持传入属性值\\[(\\d+)\\]个，实际传入了\\[(\\d+)\\]个$";
        Matcher matcher = Pattern.compile(regex).matcher(resp.getSubMsg());
        if (matcher.find()) {
            // 需要限制的pid
            String pidLimit = matcher.group(2);
            // 限制属性个数
            Integer numLimit = Integer.valueOf(matcher.group(4));
            String regexForPidLimit = pidLimit + ":\\d+;?" ;
            Matcher matcherForPidLimit = Pattern.compile(regexForPidLimit).matcher(props);
            StringBuffer propsStringBuffer = new StringBuffer();
            // 保留前numLimit个属性 去除后面的属性
            while (matcherForPidLimit.find()) {
                if (numLimit-- > 0) continue;
                matcherForPidLimit.appendReplacement(propsStringBuffer, "");
            }
            matcherForPidLimit.appendTail(propsStringBuffer);
            return propsStringBuffer.toString();
        }

        return props;
    }

    private void reqCall(Integer retry) {
        if (retry < 0) return;
        try {
            resp = client.execute(req, user.getSessionKey());
        } catch (Exception e) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            reqCall(--retry);
        }
    }

    protected void reqSetLang() {
        req.setLang("zh_CN");
    }

    protected void reqSetDesc() {
        TMResult descResult = FileCarryUtils.newFilterDesc(user, item.getDesc(), null);
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

        req.setDesc(desc);
    }

    protected void reqSetPicPath() {
        TMResult<Picture> picUrlResult = FileCarryUtils.newUploadPicFromOnline(user, item.getPicUrl());
        if (!picUrlResult.isOk) {
            throw new ItemCopyApiException(picUrlResult.msg, null);
        }
        req.setPicPath(picUrlResult.getRes() == null ? StringUtils.EMPTY : picUrlResult.getRes().getPicturePath());
    }

    protected void reqSetFoodSecurity() {
        if (item.getFoodSecurity() != null) {
            // 对食品添加food_security
            FoodSecurity foodSecurity = item.getFoodSecurity();
            setDefaultFoodSecurity(foodSecurity);
        }
        //判断食品安全资格用户
        if (user.getUserNick().equals("楚之小南") || user.getUserNick().equals("开心摇一摇00")
                || user.getUserNick().equals("suchangyu520")
                || user.getUserNick().equals("wang8862")
                || user.getUserNick().equals("yaqiu1979")
                || user.getUserNick().equals("wang8862")
                || user.getUserNick().equals("yinchun450")
                || user.getUserNick().equals("梅子情")
                || user.getUserNick().equals("wang8862")
                || user.getUserNick().equals("hnyltxx")
                || user.getUserNick().equals("喜阁尔保健食品专营店")
                || user.getUserNick().equals("时煜琳")) {
            req.setFoodSecurityPrdLicenseNo("");
            req.setFoodSecurityDesignCode("");
            req.setFoodSecurityFactory("秘鲁赫赛尔公司");
            req.setFoodSecurityFactorySite("AV.LOS FRUTALES NO.22");
            req.setFoodSecurityContact("4008200976");
            req.setFoodSecurityMix("365");
            req.setFoodSecurityPlanStorage("置阴凉干燥处");
            req.setFoodSecurityPeriod("365");
            req.setFoodSecurityFoodAdditive("无");
            req.setFoodSecuritySupplier("通用");
            req.setFoodSecurityProductDateStart("2017-02-21");
            req.setFoodSecurityProductDateEnd("2017-02-22");
            req.setFoodSecurityStockDateStart("2017-02-22");
            req.setFoodSecurityStockDateEnd("2017-06-22");
            req.setFoodSecurityHealthProductNo("国食健字J20050014");
        }
    }

    private void setDefaultFoodSecurity(FoodSecurity foodSecurity) {
        if (foodSecurity == null) foodSecurity = new FoodSecurity();
        req.setFoodSecurityPrdLicenseNo(foodSecurity.getPrdLicenseNo() == null ? "" : foodSecurity.getPrdLicenseNo());
        req.setFoodSecurityDesignCode(foodSecurity.getDesignCode() == null ? "" : foodSecurity.getDesignCode());
        req.setFoodSecurityFactory(foodSecurity.getFactory() == null ? "秘鲁赫赛尔公司" : foodSecurity.getFactory());
        req.setFoodSecurityFactorySite(foodSecurity.getFactorySite() == null ? "AV.LOS FRUTALES NO.22" : foodSecurity.getFactorySite());
        req.setFoodSecurityContact(foodSecurity.getContact() == null ? "4008200976" : foodSecurity.getContact());
        req.setFoodSecurityMix(foodSecurity.getMix() == null ? "365" : foodSecurity.getMix());
        req.setFoodSecurityPlanStorage(foodSecurity.getPlanStorage() == null ? "置阴凉干燥处" : foodSecurity.getPlanStorage());
        req.setFoodSecurityPeriod(foodSecurity.getPeriod() == null ? "365" : foodSecurity.getPeriod());
        req.setFoodSecurityFoodAdditive(foodSecurity.getFoodAdditive() == null ? "无" : foodSecurity.getFoodAdditive());
        req.setFoodSecuritySupplier(foodSecurity.getSupplier() == null ? "通用" : foodSecurity.getSupplier());
        req.setFoodSecurityProductDateStart(foodSecurity.getProductDateStart() == null ? "2017-02-21" : foodSecurity.getProductDateStart());
        req.setFoodSecurityProductDateEnd(foodSecurity.getProductDateEnd() == null ? "2017-02-22" : foodSecurity.getProductDateEnd());
        req.setFoodSecurityStockDateStart(foodSecurity.getStockDateStart() == null ? "2017-06-22" : foodSecurity.getStockDateStart());
        req.setFoodSecurityStockDateEnd(foodSecurity.getStockDateEnd() == null ? "2017-10-22" : foodSecurity.getStockDateEnd());
        req.setFoodSecurityHealthProductNo("国食健字J20050014");
    }

    protected void reqSetAuctionPoint() {
        if (user != null && user.isTmall()) {
            if (item.getAuctionPoint() == null) {
                req.setAuctionPoint(0L);
            } else {
                req.setAuctionPoint(item.getAuctionPoint());
            }
        }
    }

    protected void reqSetFee() {
        if ("0.00".equals(item.getPostFee())) {
            req.setPostFee("20.00");
        } else {
            req.setPostFee(item.getPostFee());
        }
        if ("0.00".equals(item.getExpressFee())) {
            req.setExpressFee("20.00");
        } else {
            req.setExpressFee(item.getExpressFee());
        }
        if ("0.00".equals(item.getEmsFee())) {
            req.setEmsFee("20.00");
        } else {
            req.setEmsFee(item.getEmsFee());
        }
    }

    protected void reqSetSomethingUseless() {
        req.setFreightPayer(item.getFreightPayer());
        req.setValidThru(item.getValidThru());
        req.setHasInvoice(true);
        req.setHasWarranty(item.getHasWarranty());
        req.setHasShowcase(item.getHasShowcase());
        req.setHasDiscount(item.getHasDiscount());
        req.setIncrement(item.getIncrement());
    }

    protected void reqSetPrice() {
        ItemExt itemExt = (ItemExt) item;
        String price = item.getPrice();
        String skuPrices = itemExt.getSkuPrices();
        String skuQuantities = itemExt.getSkuQuantities();
        if (StringUtils.isEmpty(skuPrices)) {
            req.setPrice(price);
        } else {
            Double maxPrice = 0d;
            String[] splitOfSkuPrice = skuPrices.split(",");
            String[] splitOfSkuQuantities = skuQuantities.split(",");
            for (int i = 0; i < splitOfSkuPrice.length; i++) {
                String skuPrice = splitOfSkuPrice[i];
                String skuQuantity = splitOfSkuQuantities[i];
                // 库存为0的sku的价格 不能作为一口价
                if (Integer.valueOf(skuQuantity) > 0 && Double.valueOf(skuPrice) > maxPrice)
                    maxPrice = Double.valueOf(skuPrice);
            }
            if (maxPrice == 0d) maxPrice = Double.valueOf(price);
            req.setPrice(decimalFormat.format(maxPrice));
        }
    }

    protected void reqSetSkuProperties() {
        ItemExt itemExt = (ItemExt) item;
        req.setSkuProperties(itemExt.getSkuProperties());
    }

    protected void reqSetSkuQuantities() {
        ItemExt itemExt = (ItemExt) item;
        req.setSkuQuantities(itemExt.getSkuQuantities());
    }

    protected void reqSetSkuPrices() {
        ItemExt itemExt = (ItemExt) item;
        req.setSkuPrices(itemExt.getSkuPrices());
    }

    protected void reqSetSkuOuterIds() {
        ItemExt itemExt = (ItemExt) item;
        req.setSkuOuterIds(itemExt.getSkuOuterIds());
    }

    protected void reqSetInputCustomCpv() {
        ItemExt itemExt = (ItemExt) item;
        req.setInputCustomCpv(itemExt.getInputCustomCpv());
    }

    protected void reqSetInputStr() {
        String inputStr = item.getInputStr();
        req.setInputStr(inputStr);
    }

    protected void reqSetInputPids() {
        String inputPids = item.getInputPids();
        req.setInputPids(inputPids);
    }

    protected void reqSetPropertyAlias() {
        String propertyAlias = item.getPropertyAlias();
        int maxLength = 250;
        if (StringUtils.isNotEmpty(propertyAlias) && propertyAlias.length() > maxLength) {
            propertyAlias = propertyAlias.substring(0, maxLength);
            int index = propertyAlias.lastIndexOf(";");
            if (index > 0) {
                propertyAlias = propertyAlias.substring(0, index + 1);
            } else {
                propertyAlias = "";
            }
        }
        req.setPropertyAlias(propertyAlias);
    }

    protected void reqSetCid() {
        Long cid = item.getCid();
        req.setCid(cid);
    }

    protected void reqSetApproveStatus() {
        String approveStatus = item.getApproveStatus();
        req.setApproveStatus(approveStatus);
    }

    protected void reqSetLocationCity() {
        Location location = item.getLocation();
        if (location == null) return;
        req.setLocationCity(location.getCity());
    }

    protected void reqSetLocationState() {
        Location location = item.getLocation();
        if (location == null) return;
        req.setLocationState(location.getState());
    }

    protected void reqSetTitle() {
        String title = item.getTitle();
        // 去除关键字"代购" title中含有关键字“代购”，发布商品时会变成发布代购商品，需要填写global_stock_type，global_stock_country，global_stock_delivery_place，直接去除就能正常发布。
        // 去除关键字"养颜"【重要】您发布的宝贝信息中【宝贝标题】存在“养颜”等违规描述，可能会违反《广告法》等相关法律规定，为维护广大消费者的合法权益，同时避免给您带来法律风险，请修改后重新发布，感谢您的配合！
        // 去除关键字"批发" 建议您在编辑发布商品信息时，请勿在供销平台外发布批发、代理、求购类广告商品或信息。具体详见：https://shangpin.bbs.taobao.com/detail.html?spm=0.0.0.0.4cHXv3&postId=6530072。
        // 去除关键字"预售" 您的商品符合预售类商品特征，请选择您需要的预售类型后再发布（通过网页版在一口价下方的“预售设置”中选择对应类型）
        // 去除关键字"中药" 【重要】您发布的宝贝信息中【宝贝标题】存在“中药”等违规描述，可能会违反《广告法》等相关法律规定，为维护广大消费者的合法权益，同时避免给您带来法律风险，请修改后重新发布，感谢您的配合！
        // 去除关键字"排毒" 【重要】您发布的宝贝信息中【宝贝标题】存在“排毒”等违规描述，可能会违反《广告法》等相关法律规定，为维护广大消费者的合法权益，同时避免给您带来法律风险，请修改后重新发布，感谢您的配合！
        title = title.replaceAll("代购|养颜|批发|预售|求购|置换|回收|代理|招商|中药|排毒|医用|点痣|祛痣|颈椎病|腰间盘突出|直邮|乳腺增生|胃炎", "");
        
        req.setTitle(title);
    }

    protected void reqSetStuffStatus() {
        String stuffStatus = item.getStuffStatus();
        req.setStuffStatus(stuffStatus);
    }

    protected void reqSetType() {
        String type = item.getType();
        req.setType(type);
    }

    protected void reqSetNum() {
        Long num = item.getNum();
        if (num < 999999L && num > 0L) {
            req.setNum(num);
        } else {
            req.setNum(99999L);
        }
    }

    protected void check() {
        if (item == null) throw new ItemCopyApiException("获取宝贝信息失败，请联系我们！", null);
    }

}
