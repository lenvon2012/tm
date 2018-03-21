
package actions.wireless;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.util.ArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.libs.Codec;
import actions.wireless.WirelessItemField.ApproveStatusField;
import actions.wireless.WirelessItemField.AuctionIncreField;
import actions.wireless.WirelessItemField.AuctionPoint;
import actions.wireless.WirelessItemField.BarCodeField;
import actions.wireless.WirelessItemField.BuyareatypeField;
import actions.wireless.WirelessItemField.CatePropsField;
import actions.wireless.WirelessItemField.CidItemField;
import actions.wireless.WirelessItemField.CustomDesignFlag;
import actions.wireless.WirelessItemField.DescriptionField;
import actions.wireless.WirelessItemField.EmsFeeField;
import actions.wireless.WirelessItemField.ExpressFeeField;
import actions.wireless.WirelessItemField.FeaturesField;
import actions.wireless.WirelessItemField.FoodParame;
import actions.wireless.WirelessItemField.FreightPayerField;
import actions.wireless.WirelessItemField.GlobalStockCountryField;
import actions.wireless.WirelessItemField.GlobalStockTypeField;
import actions.wireless.WirelessItemField.HasDiscountField;
import actions.wireless.WirelessItemField.HasInvoiceField;
import actions.wireless.WirelessItemField.HasShowCaseField;
import actions.wireless.WirelessItemField.HasWarrantyField;
import actions.wireless.WirelessItemField.InputPidsField;
import actions.wireless.WirelessItemField.InputValuesField;
import actions.wireless.WirelessItemField.IsLightingConsigmentField;
import actions.wireless.WirelessItemField.IsXinPinField;
import actions.wireless.WirelessItemField.ItemSize;
import actions.wireless.WirelessItemField.ItemTypeField;
import actions.wireless.WirelessItemField.ItemWeight;
import actions.wireless.WirelessItemField.ListTimeField;
import actions.wireless.WirelessItemField.LocalCidField;
import actions.wireless.WirelessItemField.LocationCityField;
import actions.wireless.WirelessItemField.LocationStateField;
import actions.wireless.WirelessItemField.ModifiedField;
import actions.wireless.WirelessItemField.NavigationTypeField;
import actions.wireless.WirelessItemField.NewPrepayField;
import actions.wireless.WirelessItemField.NumField;
import actions.wireless.WirelessItemField.NumIidField;
import actions.wireless.WirelessItemField.OuterIdField;
import actions.wireless.WirelessItemField.PictureField;
import actions.wireless.WirelessItemField.PictureStatusField;
import actions.wireless.WirelessItemField.PostFeeField;
import actions.wireless.WirelessItemField.PostageId;
import actions.wireless.WirelessItemField.PriceField;
import actions.wireless.WirelessItemField.PropAliasField;
import actions.wireless.WirelessItemField.SellPromise;
import actions.wireless.WirelessItemField.SellerCidsField;
import actions.wireless.WirelessItemField.SkuBarCodeField;
import actions.wireless.WirelessItemField.SkuPropsField;
import actions.wireless.WirelessItemField.StuffStatusField;
import actions.wireless.WirelessItemField.SubStockType;
import actions.wireless.WirelessItemField.SubTitleField;
import actions.wireless.WirelessItemField.SyncStatusField;
import actions.wireless.WirelessItemField.TitleField;
import actions.wireless.WirelessItemField.UploadFailMsgField;
import actions.wireless.WirelessItemField.UserNameField;
import actions.wireless.WirelessItemField.ValidThru;
import actions.wireless.WirelessItemField.VideoField;
import actions.wireless.WirelessItemField.WirelessDescField;
import actions.wireless.WirelessItemField.autoFillField;

import com.ciaosir.client.utils.DateUtil;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.ItemImg;
import com.taobao.api.domain.PropImg;

public class WirelessFieldLoader {

    private static final Logger log = LoggerFactory.getLogger(WirelessFieldLoader.class);

    public static final String TAG = "WirelessFieldLoader";

    private static WirelessFieldLoader _instance;

    public static WirelessFieldLoader get() {
        if (_instance == null) {
            _instance = new WirelessFieldLoader();
        }
        return _instance;
    }

    public WirelessFieldLoader() {
    }

    protected WirelessItemField[] fields = null;

    protected void initFields() {
//        log.info("[load fields:]" + this.fields.length);
        for (WirelessItemField field : fields) {
            field.setLoader(this);
        }
    }

    /**
     * e9edd2c170d5e4bbfc7ecd1e3280efdc:1:4:|http://img03.taobaocdn.com/bao/uploaded/i3/1039626382/T2gMCTXkxaXXXXXXXX_!!1039626382.jpg
     * @param itemImg
     * @return
     */
    public String appendPictureField(ItemImg itemImg) {
        String md5 = Codec.hexMD5(itemImg.getUrl());
        Long position = itemImg.getPosition();
        String url = itemImg.getUrl();
        return String.format("%s:1:%s:|%s", md5, position, url);
    }

    /**
     * 232eeed1bb918b934dd41d482d390196:2:0:1627207:3232480|http://img04.taobaocdn.com/bao/uploaded/i4/587251099/T2wzjtXyNaXXXXXXXX_!!587251099.jpg;
     * decb6addfb8fc0e8f937bdf9bd603a96:2:0:1627207:132069|http://img01.taobaocdn.com/bao/uploaded/i1/587251099/T2RMORXNdaXXXXXXXX_!!587251099.jpg;
     * @param img
     * @return
     */
    public String appendPropImgField(PropImg img) {
        String md5 = Codec.hexMD5(img.getUrl());
        String url = img.getUrl();
        String prop = img.getProperties();
        return String.format("%s:2:0:%s|%s", md5, prop, url);
    }

    public String appendPictureField(String url) {
        String md5 = Codec.hexMD5(url);
        Long position = 0L;
        return String.format("%s:1:%s:|%s", md5, position, url);
    }

    protected String buildValueField(String value) {
        if (value == null) {
            return StringUtils.EMPTY;
        }
        return value.replaceAll("\t", "");
    }

    protected String buildDateField(Date date) {
        if (date == null) {
            return StringUtils.EMPTY;
        }

        return "\"" + DateUtil.genYMSHms(0L).format(date) + "\"";
    }

    protected String buildLongField(Long value) {
        if (value == null) {
            return StringUtils.EMPTY;
        }
        return value.toString();
    }

    protected String buildStringField(String value) {
        if (value == null) {
            return StringUtils.EMPTY;
        }

        return "\"" + value.replaceAll("\"", "\"\"").replaceAll("\t", " ") + "\"";
    }

    protected String buildBooleanField(Boolean value) {
        if (value == null) {
            return StringUtils.EMPTY;
        }

        if (value.booleanValue()) {
            return "1";
        } else {
            return "0";
        }
    }

    protected String ver;

    protected String header;

    public WirelessFieldLoader(String ver, String header) {
        super();
        this.ver = ver;
        this.header = header;
    }

    public static class Assistent550 extends WirelessFieldLoader {
        static String headerStr = "version 1.00\n" +
                "title\tcid\tseller_cids\tstuff_status\tlocation_state\tlocation_city\t" +
                "item_type\tprice\tauction_increment\tnum\tvalid_thru\tfreight_payer\t" +
                "post_fee\tems_fee\texpress_fee\thas_invoice\thas_warranty\tapprove_status\t" +
                "has_showcase\tlist_time\tdescription\tcateProps\tpostage_id\thas_discount\t" +
                "modified\tupload_fail_msg\tpicture_status\tauction_point\tpicture\tvideo\t" +
                "skuProps\tinputPids\tinputValues\touter_id\tpropAlias\tauto_fill\tnum_id\t" +
                "local_cid\tnavigation_type\tuser_name\tsyncStatus\tis_lighting_consigment\t" +
                "is_xinpin\tfoodparame\tfeatures\tbuyareatype\tglobal_stock_type\tglobal_stock_country\t" +
                "sub_stock_type\titem_size\titem_weight\tsell_promise\tcustom_design_flag\t" +
                "wireless_desc\tbarcode\tsku_barcode\tnewprepay\n" +
                "宝贝名称\t宝贝类目\t店铺类目\t新旧程度\t省\t城市\t出售方式\t宝贝价格\t加价幅度\t" +
                "宝贝数量\t有效期\t运费承担\t平邮\tEMS\t快递\t发票\t保修\t放入仓库\t橱窗推荐\t开始时间\t" +
                "宝贝描述\t宝贝属性\t邮费模版ID\t会员打折\t修改时间\t上传状态\t图片状态\t返点比例\t" +
                "新图片\t视频\t销售属性组合\t用户输入ID串\t用户输入名-值对\t商家编码\t销售属性别名\t" +
                "代充类型\t数字ID\t本地ID\t宝贝分类\t用户名称\t宝贝状态\t闪电发货\t新品\t食品专项\t" +
                "尺码库\t采购地\t库存类型\t国家地区\t库存计数\t物流体积\t物流重量\t退换货承诺\t定制工具\t" +
                "无线详情\t商品条形码\tsku\t条形码\t7天退货\n";

        public Assistent550() {
            this.ver = "5.5.0";
            this.header = headerStr;
            fields = new WirelessItemField[] {
                    new TitleField(), new CidItemField(), new SellerCidsField(), new StuffStatusField(),
                    new LocationStateField(), new LocationCityField(),
                    new ItemTypeField(), new PriceField(), new AuctionIncreField(), new NumField(), new ValidThru(),
                    new FreightPayerField(), new PostFeeField(), new EmsFeeField(),
                    new ExpressFeeField(), new HasInvoiceField(), new HasWarrantyField(), new ApproveStatusField(),
                    new HasShowCaseField(),
                    new ListTimeField(), new DescriptionField()
                    , new CatePropsField(), new PostageId(), new HasDiscountField(), new ModifiedField(),
                    new UploadFailMsgField(),
                    new PictureStatusField(), new AuctionPoint(),
                    new PictureField(), new VideoField(), new SkuPropsField(), new InputPidsField(),
                    new InputValuesField(),
                    new OuterIdField(), new PropAliasField(),
                    new autoFillField(), new NumIidField(), new LocalCidField(), new NavigationTypeField(),
                    new UserNameField(),
                    new SyncStatusField(), new IsLightingConsigmentField(),
                    new IsXinPinField(), new FoodParame(), new FeaturesField(), new BuyareatypeField(),
                    new GlobalStockTypeField(), new GlobalStockCountryField(),
                    new SubStockType(), new ItemSize(), new ItemWeight(), new SellPromise(), new CustomDesignFlag(),
                    new WirelessDescField(), new BarCodeField(),
                    new SkuBarCodeField(), new NewPrepayField()
            };
            initFields();
        }
    };

    public static class Assistent560 extends WirelessFieldLoader {
        static String headerStr = "version 1.00\n" +
                "title\tcid\tseller_cids\tstuff_status\tlocation_state\tlocation_city\titem_type\t" +
                "price\tauction_increment\tnum\tvalid_thru\tfreight_payer\tpost_fee\tems_fee\t" +
                "express_fee\thas_invoice\thas_warranty\tapprove_status\thas_showcase\tlist_time\t" +
                "description\tcateProps\tpostage_id\thas_discount\tmodified\tupload_fail_msg\t" +
                "picture_status\tauction_point\tpicture\tvideo\tskuProps\tinputPids\tinputValues\t" +
                "outer_id\tpropAlias\tauto_fill\tnum_id\tlocal_cid\tnavigation_type\tuser_name\t" +
                "syncStatus\tis_lighting_consigment\tis_xinpin\tfoodparame\tfeatures\tbuyareatype\t" +
                "global_stock_type\tglobal_stock_country\tsub_stock_type\titem_size\titem_weight\t" +
                "sell_promise\tcustom_design_flag\twireless_desc\tbarcode\tsku_barcode\tnewprepay\t" +
                "subtitle\n" +
                "宝贝名称\t宝贝类目\t店铺类目\t新旧程度\t省\t城市\t出售方式\t宝贝价格\t加价幅度\t宝贝数量\t" +
                "有效期\t运费承担\t平邮\tEMS\t快递\t发票\t保修\t放入仓库\t橱窗推荐\t开始时间\t宝贝描述\t宝贝属性\t" +
                "邮费模版ID\t会员打折\t修改时间\t上传状态\t图片状态\t返点比例\t新图片\t视频\t销售属性组合\t" +
                "用户输入ID串\t用户输入名-值对\t商家编码\t销售属性别名\t代充类型\t数字ID\t本地ID\t宝贝分类\t" +
                "用户名称\t宝贝状态\t闪电发货\t新品\t食品专项\t尺码库\t采购地\t库存类型\t国家地区\t库存计数\t" +
                "物流体积\t物流重量\t退换货承诺\t定制工具\t无线详情\t商品条形码\tsku\t条形码\t7天退货\t宝贝卖点\n";

        public Assistent560() {
            this.ver = "5.6.0";
            this.header = headerStr;
            this.fields = new WirelessItemField[] {
                    new TitleField(), new CidItemField(), new SellerCidsField(), new StuffStatusField(),
                    new LocationStateField(), new LocationCityField(),
                    new ItemTypeField(), new PriceField(), new AuctionIncreField(), new NumField(), new ValidThru(),
                    new FreightPayerField(), new PostFeeField(), new EmsFeeField(),
                    new ExpressFeeField(), new HasInvoiceField(), new HasWarrantyField(), new ApproveStatusField(),
                    new HasShowCaseField(),
                    new ListTimeField(), new DescriptionField(), new CatePropsField(), new PostageId(),
                    new HasDiscountField(), new ModifiedField(),
                    new UploadFailMsgField(),
                    new PictureStatusField(), new AuctionPoint(),
                    new PictureField(), new VideoField(), new SkuPropsField(), new InputPidsField(),
                    new InputValuesField(),
                    new OuterIdField(), new PropAliasField(),
                    new autoFillField(), new NumIidField(), new LocalCidField(), new NavigationTypeField(),
                    new UserNameField(),
                    new SyncStatusField(), new IsLightingConsigmentField(),
                    new IsXinPinField(), new FoodParame(), new FeaturesField(), new BuyareatypeField(),
                    new GlobalStockTypeField(), new GlobalStockCountryField(),
                    new SubStockType(), new ItemSize(), new ItemWeight(), new SellPromise(), new CustomDesignFlag(),
                    new WirelessDescField(), new BarCodeField(),
                    new SkuBarCodeField(), new NewPrepayField(), new SubTitleField()
            };
            initFields();
        }
    }

    public static class Assistent561 extends WirelessFieldLoader {

        static String headerStr = "version 1.00\n" +
                "title\tcid\tseller_cids\tstuff_status\tlocation_state\tlocation_city\titem_type\t" +
                "price\tauction_increment\tnum\tvalid_thru\tfreight_payer\tpost_fee\tems_fee\t" +
                "express_fee\thas_invoice\thas_warranty\tapprove_status\thas_showcase\tlist_time\t" +
                "description\tcateProps\tpostage_id\thas_discount\tmodified\tupload_fail_msg\t" +
                "picture_status\tauction_point\tpicture\tvideo\tskuProps\tinputPids\tinputValues\t" +
                "outer_id\tpropAlias\tauto_fill\tnum_id\tlocal_cid\tnavigation_type\tuser_name\t" +
                "syncStatus\tis_lighting_consigment\tis_xinpin\tfoodparame\tfeatures\tbuyareatype\t" +
                "global_stock_type\tglobal_stock_country\tsub_stock_type\titem_size\titem_weight\t" +
                "sell_promise\tcustom_design_flag\twireless_desc\tbarcode\tsku_barcode\tnewprepay\t" +
                "subtitle\n" +
                "宝贝名称\t宝贝类目\t店铺类目\t新旧程度\t省\t城市\t出售方式\t宝贝价格\t加价幅度\t宝贝数量\t" +
                "有效期\t运费承担\t平邮\tEMS\t快递\t发票\t保修\t放入仓库\t橱窗推荐\t开始时间\t宝贝描述\t宝贝属性\t" +
                "邮费模版ID\t会员打折\t修改时间\t上传状态\t图片状态\t返点比例\t新图片\t视频\t销售属性组合\t" +
                "用户输入ID串\t用户输入名-值对\t商家编码\t销售属性别名\t代充类型\t数字ID\t本地ID\t宝贝分类\t" +
                "用户名称\t宝贝状态\t闪电发货\t新品\t食品专项\t尺码库\t采购地\t库存类型\t国家地区\t库存计数\t" +
                "物流体积\t物流重量\t退换货承诺\t定制工具\t无线详情\t商品条形码\tsku\t条形码\t7天退货\t宝贝卖点\n";

        public Assistent561() {
            this.ver = "5.6.1";
            this.header = headerStr;
            this.fields = new WirelessItemField[] {
                    new TitleField(), new CidItemField(), new SellerCidsField(), new StuffStatusField(),
                    new LocationStateField(), new LocationCityField(),
                    new ItemTypeField(), new PriceField(), new AuctionIncreField(), new NumField(), new ValidThru(),
                    new FreightPayerField(), new PostFeeField(), new EmsFeeField(),
                    new ExpressFeeField(), new HasInvoiceField(), new HasWarrantyField(), new ApproveStatusField(),
                    new HasShowCaseField(),
                    new ListTimeField(), new DescriptionField(), new CatePropsField(), new PostageId(),
                    new HasDiscountField(), new ModifiedField(),
                    new UploadFailMsgField(),
                    new PictureStatusField(), new AuctionPoint(),
                    new PictureField(), new VideoField(), new SkuPropsField(), new InputPidsField(),
                    new InputValuesField(),
                    new OuterIdField(), new PropAliasField(),
                    new autoFillField(), new NumIidField(), new LocalCidField(), new NavigationTypeField(),
                    new UserNameField(),
                    new SyncStatusField(), new IsLightingConsigmentField(),
                    new IsXinPinField(), new FoodParame(), new FeaturesField(), new BuyareatypeField(),
                    new GlobalStockTypeField(), new GlobalStockCountryField(),
                    new SubStockType(), new ItemSize(), new ItemWeight(), new SellPromise(), new CustomDesignFlag(),
                    new WirelessDescField(), new BarCodeField(),
                    new SkuBarCodeField(), new NewPrepayField(), new SubTitleField()
            };
//            this.header = "version 1.00\n" +
//                    "title\tnum_id\tuser_name\twireless_desc\n"
//                    + "宝贝名称\t数字ID\t用户名称\t无线详情\n";
//            ;
//            this.fields = new WirelessItemField[] {
//                    new TitleField(), new NumIidField(), new UserNameField(), new WirelessDescField(),
//            };

            initFields();
        }

        protected String buildStringField(String value) {
            if (value == null) {
                return "\"\"";
            }

            return "\"" + value.replaceAll("\"", "\"\"").replaceAll("\t", " ") + "\"";
        }
    }

    public String buildItemLine(Item item) {
        WirelessItemAssistant assistant = new WirelessItemAssistant(item);
        List<String> values = new ArrayList<String>();
//        log.info("[fields:]" + ArrayUtils.toString(this.fields,","));

        for (WirelessItemField field : fields) {
            String value = field.buildField(assistant);
            values.add(value);
        }
        return StringUtils.join(values, '\t');
    }

    /**
     * 淘宝助理 5.6.1
     */
    public static WirelessFieldLoader loader61 = new Assistent561();

//    /**
//     * 淘宝助理5.6.0
//     */
//    public static WirelessFieldLoader loader60 = new Assistent560();
//
//    /**
//     * 淘宝助理5.5.0
//     */
    public static WirelessFieldLoader loader50 = new Assistent561();

    public WirelessItemField[] getFields() {
        return fields;
    }

    public void setFields(WirelessItemField[] fields) {
        this.fields = fields;
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String buildDoubleField(String value) {
        if (value == null) {
            return "0.0";
        }
        return value;
    }

}
