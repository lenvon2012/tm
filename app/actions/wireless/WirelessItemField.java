
package actions.wireless;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bustbapi.TMTradeApi.GetNumIidOfTradeOid;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.NumberUtil;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.taobao.api.domain.ItemImg;
import com.taobao.api.domain.Location;
import com.taobao.api.domain.PropImg;
import com.taobao.api.domain.Sku;
import com.taobao.api.domain.Video;

public abstract class WirelessItemField {

    private static final Logger log = LoggerFactory.getLogger(WirelessItemField.class);

    public static final String TAG = "WirelessItemField";

    protected WirelessFieldLoader loader = null;

    public WirelessFieldLoader getLoader() {
        return loader;
    }

    public void setLoader(WirelessFieldLoader loader) {
        this.loader = loader;
    }

    public abstract String getFieldName();

    public abstract String getFieldChnName();

    public void appendSb(WirelessItemAssistant item, StringBuilder sb, Gson gson) {
    }

    public void fillField(WirelessItemAssistant item, String res, JsonParser parser) {
    }

    protected int index;

    public static class AuctionIncreField extends WirelessItemField {

        public String getFieldName() {
            return "auction_increment";
        }

        public String getFieldChnName() {
            return "加价幅度";
        }

        public void appendSb(WirelessItemAssistant item, StringBuilder sb, Gson gson) {
            try {
                sb.append(gson.toJson(new DecimalFormat("######0.00").parseObject(item.getAutoFill())));
            } catch (ParseException e) {
                WirelessItemAssistant.log.warn(e.getMessage(), e);
            }
        }

        public void fillField(WirelessItemAssistant item, String res, JsonParser parser) {
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            return buildStringField(item.getIncrement());
        }

    };

    public static class PriceField extends WirelessItemField {

        public String getFieldName() {
            return "price";
        }

        public String getFieldChnName() {
            return "宝贝价格";
        }

        public void appendSb(WirelessItemAssistant item, StringBuilder sb, Gson gson) {
            try {
                sb.append(gson.toJson(new DecimalFormat("######0.00").parseObject(item.getPrice())));
            } catch (ParseException e) {
                WirelessItemAssistant.log.warn(e.getMessage(), e);
            }
        }

        public void fillField(WirelessItemAssistant item, String res, JsonParser parser) {
            if (StringUtils.isEmpty(res)) {
                return;
            }
            item.setPrice(String.valueOf(parser.parse(res).getAsDouble()));
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            return buildDoubleField(item.getPrice());
        }
    };

    public static class ItemTypeField extends WirelessItemField {

        public String getFieldName() {
            return "item_type";
        }

        public String getFieldChnName() {
            return "出售方式";
        }

        public void appendSb(WirelessItemAssistant item, StringBuilder sb, Gson gson) {
            sb.append(gson.toJson(item.getItemType()));
        }

        public void fillField(WirelessItemAssistant item, String res, JsonParser parser) {
            if (StringUtils.isEmpty(res)) {
                return;
            }
            item.setItemType(parser.parse(res).getAsInt());
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            Long value = 1L;
            if ("fixed".equals(item.getType())) {
                value = 1L;
            } else if ("auction".equals(item.getType())) {
                value = 2L;
            }
            return buildLongField(value);
        }
    };

    public static class LocationCityField extends WirelessItemField {
        public String getFieldName() {
            return "location_city";
        }

        public String getFieldChnName() {
            return "城市";
        }

        public void appendSb(WirelessItemAssistant item, StringBuilder sb, Gson gson) {
            Location loc = item.getLocation();
            if (loc == null) {

            } else {
                sb.append(gson.toJson(loc.getCity()));
            }

        }

        public void fillField(WirelessItemAssistant item, String res, JsonParser parser) {
            if (StringUtils.isEmpty(res)) {
                return;
            }

            Location loc = item.getLocation();
            if (loc == null) {
                loc = new Location();
                item.setLocation(loc);
            }
            loc.setCity(parser.parse(res).getAsString());
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            return buildStringField(item.getLocation().getCity());
        }
    };

    public static class LocationStateField extends WirelessItemField {
        public String getFieldName() {
            return "location_state";
        }

        public String getFieldChnName() {
            return "省";
        }

        public void appendSb(WirelessItemAssistant item, StringBuilder sb, Gson gson) {
            Location loc = item.getLocation();
            if (loc == null) {

            } else {
                sb.append(gson.toJson(loc.getState()));
            }

        }

        public void fillField(WirelessItemAssistant item, String res, JsonParser parser) {
            if (StringUtils.isEmpty(res)) {
                return;
            }

            Location loc = item.getLocation();
            if (loc == null) {
                loc = new Location();
                item.setLocation(loc);
            }
            loc.setState(parser.parse(res).getAsString());
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
//            log.info("[item.getlocation]" + new Gson().toJson(item.getLocation()));
            return buildStringField(item.getLocation().getState());
        }

    };

    public static class StuffStatusField extends WirelessItemField {
        public String getFieldName() {
            return "stuff_status";
        }

        public String getFieldChnName() {
            return "新旧程度";
        }

        public void appendSb(WirelessItemAssistant item, StringBuilder sb, Gson gson) {
            sb.append(gson.toJson(item.getStuffStatus()));
        }

        public void fillField(WirelessItemAssistant item, String res, JsonParser parser) {
            if (StringUtils.isEmpty(res)) {
                return;
            }
            item.setStuffStatus(parser.parse(res).getAsString());
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            /**
             * stuff_status     String  否   new     
             * 商品新旧程度(全新:new，闲置:unused，二手：second)
             * 淘宝助理里面是0 1 2
             */
            Long value = null;
            String status = item.getStuffStatus();
            if ("new".equals(status)) {
                value = 1L;
            } else if ("unused".equals(status)) {
                value = 2L;
            } else if ("second".equals(status)) {
                value = 3L;
            }
            return buildLongField(value);
        }
    };

    public static class SellerCidsField extends WirelessItemField {

        public String getFieldName() {
            return "seller_cids";
        }

        public String getFieldChnName() {
            return "店铺类目";
        }

        public void appendSb(WirelessItemAssistant item, StringBuilder sb, Gson gson) {
            sb.append(gson.toJson(item.getSellerCids()));
        }

        public void fillField(WirelessItemAssistant item, String res, JsonParser parser) {
            if (StringUtils.isEmpty(res)) {
                return;
            }
            item.setSellerCids(parser.parse(res).getAsString());
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            return buildStringField(item.getSellerCids());
        }
    };

    public static class NumField extends WirelessItemField {
        @Override
        public String getFieldName() {
            return "num";
        }

        @Override
        public String getFieldChnName() {
            return "宝贝数量";
        }

        @Override
        public void appendSb(WirelessItemAssistant item, StringBuilder sb, Gson gson) {
            if (item.getNum() == null) {
                return;
            }
            sb.append(item.getNum());
        }

        @Override
        public void fillField(WirelessItemAssistant item, String res, JsonParser parser) {
            if (StringUtils.isNumeric(res)) {
                item.setNum(NumberUtil.parserLong(res, 0L));
            }
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            return buildLongField(item.getNum());
        }
    };

    public static class ValidThru extends WirelessItemField {
        @Override
        public String getFieldName() {
            return "valid_thru";
        }

        @Override
        public String getFieldChnName() {
            return "有效期";
        }

        @Override
        public void fillField(WirelessItemAssistant item, String res, JsonParser parser) {
        }

        @Override
        public void appendSb(WirelessItemAssistant item, StringBuilder sb, Gson gson) {
            // TODO Auto-generated method stub
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            return buildLongField(item.getValidThru());
        }
    };

    public static class FreightPayerField extends WirelessItemField {
        @Override
        public String getFieldName() {
            return "freight_payer";
        }

        @Override
        public String getFieldChnName() {
            return "运费承担";
        }

        /*
         * freight_payer    String  否   seller  运费承担方式,seller（卖家承担），buyer(买家承担）
         * @see actions.wireless.WirelessItemField#buildField(actions.wireless.WirelessItemAssistant)
         */
        @Override
        public String buildField(WirelessItemAssistant item) {
            Long value = null;
            String fp = item.getFreightPayer();
            if ("seller".equals(fp)) {
                value = 1L;
            } else if ("buyer".equals(fp)) {
                value = 2L;
            }
            return buildLongField(value);
        }

    };

    public static class PostFeeField extends WirelessItemField {
        @Override
        public String getFieldName() {
            return "post_fee";
        }

        @Override
        public String getFieldChnName() {
            return "平邮";
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            return buildValueField(item.getPostFee());
        }
    };

    public static class EmsFeeField extends WirelessItemField {
        @Override
        public String getFieldName() {
            return "ems_fee";
        }

        @Override
        public String getFieldChnName() {
            return "EMS";
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            return buildValueField(item.getEmsFee());
        }
    };

    public static class ExpressFeeField extends WirelessItemField {

        @Override
        public String getFieldName() {
            return "express_fee";
        }

        @Override
        public String getFieldChnName() {
            return "快递";
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            return buildValueField(item.getExpressFee());
        }

    };

    public static class HasInvoiceField extends WirelessItemField {

        @Override
        public String getFieldName() {
            return "has_invoice";
        }

        @Override
        public String getFieldChnName() {
            return "发票";
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            return buildBooleanField(item.getHasInvoice());
        }
    };

//    has_warranty
    public static class HasWarrantyField extends WirelessItemField {

        @Override
        public String getFieldName() {
            return "has_warranty";
        }

        @Override
        public String getFieldChnName() {
            return "保修";
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            return buildBooleanField(item.getHasWarranty());
        }
    };

    /**
     * 这个到底是1还是2, 我也没把握
     */
    public static class ApproveStatusField extends WirelessItemField {
        @Override
        public String getFieldName() {
            return "approve_status";
        }

        @Override
        public String getFieldChnName() {
            return "放入仓库";
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            Long value = null;
            if ("onsale".equals(item.getApproveStatus())) {
                value = 1L;
            } else if ("instock".equals(item.getApproveStatus())) {
                value = 2L;
            }
            return buildLongField(value);
        }
    };

    public static class HasShowCaseField extends WirelessItemField {

        @Override
        public String getFieldName() {
            return "has_showcase";
        }

        @Override
        public String getFieldChnName() {
            return "橱窗推荐";
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            return buildBooleanField(item.getHasShowcase());
        }
    };

    public static class ListTimeField extends WirelessItemField {

        @Override
        public String getFieldName() {
            return "list_time";
        }

        @Override
        public String getFieldChnName() {
            return "开始时间";
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            return buildDateField(item.getListTime());
        }
    };

    public static class CatePropsField extends WirelessItemField {
        @Override
        public String getFieldName() {
            return "cateProps";
        }

        @Override
        public String getFieldChnName() {
            return "宝贝属性";
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            return buildStringField(item.getProps());
        }
    };

    public static class PostageId extends WirelessItemField {
        @Override
        public String getFieldName() {
            return "postage_id";
        }

        @Override
        public String getFieldChnName() {
            return "邮费模版ID";
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            return buildLongField(item.getPostageId());
        }
    };

    public static class HasDiscountField extends WirelessItemField {
        @Override
        public String getFieldName() {
            return "has_discount";
        }

        @Override
        public String getFieldChnName() {
            return "会员打折";
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            return buildBooleanField(item.getHasDiscount());
        }
    };

    //modified
    public static class ModifiedField extends WirelessItemField {
        @Override
        public String getFieldName() {
            return "modified";
        }

        @Override
        public String getFieldChnName() {
            return "修改时间";
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            return buildDateField(item.getModified());
        }
    };

    public static class UploadFailMsgField extends WirelessItemField {
        @Override
        public String getFieldName() {
            return "upload_fail_msg";
        }

        @Override
        public String getFieldChnName() {
            return "上传状态";
        }

        /**
         * 200 应该是正常的意思
         */
        @Override
        public String buildField(WirelessItemAssistant item) {
            return buildStringField("200");
        }
    };

    public static class PictureStatusField extends WirelessItemField {
        @Override
        public String getFieldName() {
            return "picture_status";
        }

        @Override
        public String getFieldChnName() {
            return "图片状态";
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            List<ItemImg> imgs = item.getItemImgs();

            /**
             * 这个比较复杂
             */
            if (CommonUtils.isEmpty(imgs)) {
                return buildValueField("1");
            } else {
                List<Long> ids = new ArrayList<Long>();
                for (ItemImg itemImg : imgs) {
                    ids.add(1L);
                }
                return buildStringField(StringUtils.join(ids, ';') + ";");
//                return buildStringField()
            }
        }
    };

    //auction_point
    public static class AuctionPoint extends WirelessItemField {

        @Override
        public String getFieldName() {
            return "auction_point";
        }

        @Override
        public String getFieldChnName() {
            return "返点比例";
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            return buildLongField(item.getAuctionPoint());
        }
    };


    /**
     *
     * http://localhost:9999/wireless/onekey?sid=6100b17bd6df2dbb65989258319e5chj37c52c82857ecf51981410042
     * @author zrb
     *
     */
    public static class PictureField extends WirelessItemField {

        @Override
        public String getFieldName() {
            return "picture";
        }

        @Override
        public String getFieldChnName() {
            return "新图片";
        }

        @Override
        /**
         * da4696a2be9654097864d9511e055dc6:1:0:|http://img01.taobaocdn.com/bao/uploaded/i1/1039626382/T22799XjhbXXXXXXXX-1039626382.jpg;
         * m5 (1这个到底是啥??)position path
         * 估计不是引用就是删除
         * 1a47080ce020975854fa833836240b16:1:0:|http://img03.taobaocdn.com/bao/uploaded/i3/16382019946892756/T1BT0xXspaXXXXXXXX_!!0-item_pic.jpg
         * ;
         * 7036575b0f752f8b6612c14b9336dc6b:1:1:|http://img03.taobaocdn.com/bao/uploaded/i3/1039626382/T2uMqTXopaXXXXXXXX_!!1039626382.jpg
         * ;
         * 12474ce46e74436060923d130218d868:1:2:|http://img02.taobaocdn.com/bao/uploaded/i2/1039626382/T2LKucXaJcXXXXXXXX_!!1039626382.jpg
         * ;
         * 04114693383b9c26225f8b29745a6095:1:3:|http://img04.taobaocdn.com/bao/uploaded/i4/1039626382/T2SWiUXd4aXXXXXXXX_!!1039626382.jpg
         * ;
         * e9edd2c170d5e4bbfc7ecd1e3280efdc:1:4:|http://img03.taobaocdn.com/bao/uploaded/i3/1039626382/T2gMCTXkxaXXXXXXXX_!!1039626382.jpg
         * ;
         *
         *
         * 773ee7080bff1d8722c741c885fbc6d1:1:0:|http://img03.taobaocdn.com/bao/uploaded/i3/T138hPFUNbXXXXXXXX_!!0-item_pic.jpg;
         * 65af0f9ac543a5f2a0bd8dc1f6cd7f2e:1:1:|http://img01.taobaocdn.com/bao/uploaded/i1/587251099/T2LBtQXh0eXXXXXXXX_!!587251099.jpg;
         * 232eeed1bb918b934dd41d482d390196:1:2:|http://img04.taobaocdn.com/bao/uploaded/i4/587251099/T2wzjtXyNaXXXXXXXX_!!587251099.jpg;
         * b8e56fd22e34a78cb4e1ba8d86a6de23:1:3:|http://img03.taobaocdn.com/bao/uploaded/i3/587251099/T2O61nXrRbXXXXXXXX_!!587251099.jpg;
         * 257e3493e37de76eeb9031e96937df3c:1:4:|http://img04.taobaocdn.com/bao/uploaded/i4/587251099/T26c9TXGRaXXXXXXXX_!!587251099.jpg;
         * b8e56fd22e34a78cb4e1ba8d86a6de23:2:0:1627207:3232484|http://img03.taobaocdn.com/bao/uploaded/i3/587251099/T2O61nXrRbXXXXXXXX_!!587251099.jpg;
         * 65af0f9ac543a5f2a0bd8dc1f6cd7f2e:2:0:1627207:28320|http://img01.taobaocdn.com/bao/uploaded/i1/587251099/T2LBtQXh0eXXXXXXXX_!!587251099.jpg;
         * 232eeed1bb918b934dd41d482d390196:2:0:1627207:3232480|http://img04.taobaocdn.com/bao/uploaded/i4/587251099/T2wzjtXyNaXXXXXXXX_!!587251099.jpg;
         * decb6addfb8fc0e8f937bdf9bd603a96:2:0:1627207:132069|http://img01.taobaocdn.com/bao/uploaded/i1/587251099/T2RMORXNdaXXXXXXXX_!!587251099.jpg;
         * b8e56fd22e34a78cb4e1ba8d86a6de23:2:0:1627207:3232484|http://img03.taobaocdn.com/bao/uploaded/i3/587251099/T2O61nXrRbXXXXXXXX_!!587251099.jpg;
         * 65af0f9ac543a5f2a0bd8dc1f6cd7f2e:2:0:1627207:28320|http://img01.taobaocdn.com/bao/uploaded/i1/587251099/T2LBtQXh0eXXXXXXXX_!!587251099.jpg;
         * 232eeed1bb918b934dd41d482d390196:2:0:1627207:3232480|http://img04.taobaocdn.com/bao/uploaded/i4/587251099/T2wzjtXyNaXXXXXXXX_!!587251099.jpg;
         * decb6addfb8fc0e8f937bdf9bd603a96:2:0:1627207:132069|http://img01.taobaocdn.com/bao/uploaded/i1/587251099/T2RMORXNdaXXXXXXXX_!!587251099.jpg;
         *
         *
         * 30cd4fab76fd3ce76537dd990a4546c8:1:0:|http://img01.taobaocdn.com/bao/uploaded/i1/T1QFNiFsVeXXXXXXXX_!!0-item_pic.jpg;
         * f1b19f4367824844d32eaf851a6cfe7a:2:0:1627207:28340|http://img04.taobaocdn.com/bao/uploaded/i4/1039626382/T2FxPJXJXaXXXXXXXX-1039626382.jpg;
         * 2df5006ca421ea02adaaa85d76ae7478:2:0:1627207:107121|http://img01.taobaocdn.com/bao/uploaded/i1/1039626382/T2pCbMXN8XXXXXXXXX-1039626382.jpg;
         * 2df5006ca421ea02adaaa85d76ae7478:2:0:1627207:28341|http://img01.taobaocdn.com/bao/uploaded/i1/1039626382/T2pCbMXN8XXXXXXXXX-1039626382.jpg;
         */
        public String buildField(WirelessItemAssistant item) {
            List<ItemImg> itemImgs = item.getItemImgs();
            List<String> args = new ArrayList<String>();
            if (CommonUtils.isEmpty(itemImgs)) {
                String field = WirelessFieldLoader.get().appendPictureField(item.getPicUrl());
                args.add(field);
            } else {
                for (ItemImg itemImg : itemImgs) {
                    String field = WirelessFieldLoader.get().appendPictureField(itemImg);
                    args.add(field);
                }
            }

            List<PropImg> propImgs = item.getPropImgs();
            if (CommonUtils.isEmpty(propImgs)) {
                // Nothing to do...
            } else {
                for (PropImg propImg : propImgs) {
                    String field = WirelessFieldLoader.get().appendPropImgField(propImg);
                    args.add(field);
                }
            }

            String parts = StringUtils.join(args, ';');
            if (parts.endsWith(";")) {

            } else {
                parts += ";";
            }
            return buildStringField(parts);

            //da4696a2be9654097864d9511e055dc6:1:0:|http://img01.taobaocdn.com/bao/uploaded/i1/1039626382/T22799XjhbXXXXXXXX-1039626382.jpg;
//            return buildLongField(item.getAuctionPoint());
            // 这个好像还是比较难搞的
        }
    };

    /**
     * @deprecated
     * 现在还不能这么干...
     * @author zrb
     *
     */
    public static class VideoField extends WirelessItemField {

        @Override
        public String getFieldName() {
            return "video";
        }

        @Override
        public String getFieldChnName() {
            return "视频";
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            List<Video> videos = item.getVideos();
            // TODO 这个也比较难搞
            return StringUtils.EMPTY;
        }
    };

    public static class SkuPropsField extends WirelessItemField {

        @Override
        public String getFieldName() {
            return "skuProps";
        }

        @Override
        public String getFieldChnName() {
            return "销售属性组合";
        }

        /**
         * 48.43:100::1627207:3232478;20509:28383;
         * 48.58:100::1627207:28340;20509:28383;
         * 48.41:95:EAN-8:1627207:132069;20509:28383;
         * 48.58:100::1627207:28341;20509:28383;
         * {
        "created": "2012-10-12 23:17:53",
        "modified": "2014-04-30 01:07:20",
        "outerId": "EAN-8",
        "price": "48.41",
        "properties": "1627207:132069;20509:28383",
        "propertiesName": "1627207:132069:颜色分类:褐色;20509:28383:尺码:均码",
        "quantity": 95,
        "skuId": 22972337865
        },
        {
        "created": "2012-10-12 23:17:53",
        "modified": "2014-04-13 19:15:08",
        "price": "48.58",
        "properties": "1627207:28341;20509:28383",
        "propertiesName": "1627207:28341:颜色分类:黑色;20509:28383:尺码:均码",
        "quantity": 100,
        "skuId": 22972337866
        
        SkuPropsField
        48.43:100::1627207:3232478;20509:28383;48.58:100::1627207:28340;20509:28383;48.41:95:EAN-8:1627207:132069;20509:28383;48.58:100::1627207:28341;20509:28383;
        48.43:100:1627207:3232478;20509:28383;48.58:100:1627207:28340;20509:28383;48.41:95:1627207:132069;20509:28383;48.58:100:1627207:28341;20509:28383
        }

         */
        @Override
        public String buildField(WirelessItemAssistant item) {
            List<Sku> skus = item.getSkus();
            if (CommonUtils.isEmpty(skus)) {
                return buildStringField(StringUtils.EMPTY);
            }
            List<String> allParts = new ArrayList<String>();
            for (Sku sku : skus) {
                List<String> singleParts = new ArrayList<String>();
                singleParts.add(sku.getPrice());
                Long quantity = sku.getQuantity();
                if (quantity == null) {
                    quantity = 0L;
                } else if (quantity > 999999L) {
                    quantity = 999999L;
                }
                singleParts.add(quantity.toString());
                singleParts.add(sku.getBarcode() == null ? "" : sku.getBarcode());
                singleParts.add(sku.getProperties());
                allParts.add(StringUtils.join(singleParts, ':'));
            }
            String strValue = StringUtils.join(allParts, ';');
            if (!strValue.endsWith(";")) {
                strValue += ";";
            }
            return buildStringField(strValue);
        }
    };

    public static class InputPidsField extends WirelessItemField {

        @Override
        public String getFieldName() {
            return "inputPids";
        }

        @Override
        public String getFieldChnName() {
            return "用户输入ID串";
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            return buildStringField(item.getInputPids());
        }
    };

    public static class InputValuesField extends WirelessItemField {

        @Override
        public String getFieldName() {
            return "inputValues";
        }

        @Override
        public String getFieldChnName() {
            return "用户输入名-值对";
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            return buildStringField(item.getInputStr());
        }

    }

    public static class OuterIdField extends WirelessItemField {

        @Override
        public String getFieldName() {
            return "outer_id";
        }

        @Override
        public String getFieldChnName() {
            return "商家编码";
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            return buildStringField(item.getOuterId());
        }
    };

    /*
     * "propAlias";
     */
    public static class PropAliasField extends WirelessItemField {

        @Override
        public String getFieldName() {
            return "propAlias";
        }

        @Override
        public String getFieldChnName() {
            return "销售属性别名";
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            return buildStringField(item.getPropertyAlias());
        }
    };

    public static class autoFillField extends WirelessItemField {

        @Override
        public String getFieldName() {
            return "auto_fill";
        }

        @Override
        public String getFieldChnName() {
            return "代充类型";
        }

        /**
         * TODO, 这个目前还是猜的
         * 代充商品类型。在代充商品的类目下，不传表示不标记商品类型（交易搜索中就不能通过标记搜到相关的交易了）。可选类型： 
         * no_mark(不做类型标记) time_card(点卡软件代充) fee_card(话费软件代充)
         */
        @Override
        public String buildField(WirelessItemAssistant item) {
            Long value = 0L;
            String autoFill = item.getAutoFill();
            if ("no_mark".equals(autoFill)) {
                value = 1L;
            } else if ("time_card".equals(autoFill)) {
                value = 2L;
            } else if ("fee_card".equals(autoFill)) {
                value = 3L;
            }
            return buildLongField(value);
        }
    };

    /*
     * local_cid
     */
    public static class LocalCidField extends WirelessItemField {

        @Override
        public String getFieldName() {
            return "local_cid";
        }

        @Override
        public String getFieldChnName() {
            return "本地ID";
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
//            return buildStringField(item.getloca);
            return buildLongField(0L);
        }
    };

    public static class NavigationTypeField extends WirelessItemField {
        @Override
        public String getFieldName() {
            return "navigation_type";
        }

        @Override
        public String getFieldChnName() {
            return "宝贝分类";
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            Long value = 3L;
            if ("instock".equals(item.getApproveStatus())) {
                value = 3L;
            } else if ("onsale".equals(item.getApproveStatus())) {
                value = 2L;
            }
            return buildLongField(value);
        }
    }

    public static class SyncStatusField extends WirelessItemField {
        @Override
        public String getFieldName() {
            return "syncStatus";
        }

        @Override
        public String getFieldChnName() {
            return "宝贝状态";
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            /**
             * 默认好像都是1
             */
            return buildLongField(1L);
        }
    }

    public static class IsLightingConsigmentField extends WirelessItemField {
        @Override
        public String getFieldName() {
            return "is_lighting_consigment";
        }

        @Override
        public String getFieldChnName() {
            return "闪电发货";
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
//            return item.getIsLightningConsignment();
            // TODO 这是什么东西????
            return "204";
        }

    }

    public static class IsXinPinField extends WirelessItemField {
        @Override
        public String getFieldName() {
            return "is_xinpin";
        }

        @Override
        public String getFieldChnName() {
            return "新品";
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            // TODO 245 is what????
            item.getIsXinpin();
            return "245";
        }
    }

    public static class FoodParame extends WirelessItemField {

        @Override
        public String getFieldName() {
            return "foodparame";
        }

        @Override
        public String getFieldChnName() {
            return "食品专项";
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            //return item.getFoodSecurity();
            /*
             * TODO 这里也有点问题
             */
            return StringUtils.EMPTY;
        }

    }

    public static class FeaturesField extends WirelessItemField {

        @Override
        public String getFieldName() {
            return "features";
        }

        @Override
        public String getFieldChnName() {
            return "尺码库";
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            //return buildStringField(item.getSkus());
            /*
             * TODO 这个字段也要确认一下
             */
            return StringUtils.EMPTY;
        }

    }

    /**
     * buyareatype
     * 采购地
     * 应该是国内和国外两种
     * @author zrb
     *
     */
    public static class BuyareatypeField extends WirelessItemField {

        @Override
        public String getFieldName() {
            return "buyareatype";
        }

        @Override
        public String getFieldChnName() {
            return "采购地";
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            Long value = 0L;
            if ("1".equals(item.getGlobalStockType()) || "2".equals(item.getGlobalStockType())) {
                value = 1L;
            }

            /**
             * TODO nothing??
             */
            return buildLongField(value);
        }
    }

    //库存类型
    public static class GlobalStockTypeField extends WirelessItemField {
        @Override
        public String getFieldName() {
            return "global_stock_type";
        }

        @Override
        public String getFieldChnName() {
            return "库存类型";
        }

        @Override
        public String buildField(WirelessItemAssistant item) {

            /*
             * lobal_stock_type     String      可选  
             * 1       针对全球购卖家的库存类型业务， 
             * 有两种库存类型：现货和代购 参数值为1时代表现货，值为2时代表代购 
             * 如果传值为这两个值之外的值，会报错; 如果不是全球购卖家，这两个值即使设置也不会处理 
             */
            Long value = -1L;
            if ("1".equals(item.getGlobalStockType())) {
                value = 1L;
            } else if ("2".equals(item.getGlobalStockType())) {
                value = 2L;
            }
            return buildLongField(value);
        }
    }

    public static class GlobalStockCountryField extends WirelessItemField {
        @Override
        public String getFieldName() {
            return "global_stock_country";
        }

        @Override
        public String getFieldChnName() {
            return "国家地区";
        }

        /**
         * TODO 默认是0
         * global_stock_country  String  否  美国  全球购商品采购地信息（地区/国家），代表全球购商品的产地信息。
         */
        @Override
        public String buildField(WirelessItemAssistant item) {
            /**
             * TODO 默认是0
             * global_stock_country  String  否  美国  全球购商品采购地信息（地区/国家），代表全球购商品的产地信息。
             * 5.6沒有雙引號...
             */
            return buildValueField(item.getGlobalStockCountry());
        }
    }

    /*
     * sub_stock_type
     */
    public static class SubStockType extends WirelessItemField {

        @Override
        public String getFieldName() {
            return "sub_stock_type";
        }

        @Override
        public String getFieldChnName() {
            return "库存计数";
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            /**
             * 标识商品减库存的方式
             * 值含义：1-拍下减库存，2-付款减库存。
             */
            return buildLongField(item.getSubStock());
        }

    }

    public static class ItemSize extends WirelessItemField {

        @Override
        public String getFieldName() {
            return "item_size";
        }

        @Override
        public String getFieldChnName() {
            return "物流体积";
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            return buildValueField(item.getItemSize());
        }

    }

    /**
     * item_weight
     * @return
     */
    public static class ItemWeight extends WirelessItemField {

        @Override
        public String getFieldName() {
            return "item_weight";
        }

        @Override
        public String getFieldChnName() {
            return "物流重量";
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            return buildValueField(item.getItemWeight());
        }

    }

    /*
     * sell_promise
     */
    public static class SellPromise extends WirelessItemField {
        @Override
        public String getFieldName() {
            return "sell_promise";
        }

        @Override
        public String getFieldChnName() {
            return "退换货承诺";
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            return buildBooleanField(item.getSellPromise());
        }

    }

    /*
     * custom_design_flag
     */
    public static class CustomDesignFlag extends WirelessItemField {

        @Override
        public String getFieldName() {
            return "custom_design_flag";
        }

        @Override
        public String getFieldChnName() {
            return "定制工具";
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            return buildValueField(item.getCustomMadeTypeid());
        }
    }

    public static class WirelessDescField extends WirelessItemField {

        static WirelessDescField _instance = null;

        public static WirelessDescField getOne() {
            if (_instance != null) {
                return _instance;
            }
            _instance = new WirelessDescField();
            return _instance;
        }

        public String getFieldName() {
            return "wireless_desc";
        }

        public String getFieldChnName() {
            return "无线详情";
        }

        public void appendSb(WirelessItemAssistant item, StringBuilder sb, Gson gson) {
            sb.append(gson.toJson(item.getDesc()));
        }

        public void fillField(WirelessItemAssistant item, String res, JsonParser parser) {
            if (StringUtils.isEmpty(res)) {
                return;
            }
            item.setDesc(parser.parse(res).getAsString());
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            return buildStringField(item.getWirelessDesc());
        }
    }

    public static class BarCodeField extends WirelessItemField {

        @Override
        public String getFieldName() {
            return "barcode";
        }

        @Override
        public String getFieldChnName() {
            return "商品条形码";
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            return buildValueField(item.getBarcode());
        }

    }

    public static class UserNameField extends WirelessItemField {
        public String getFieldName() {
            return "user_name";
        }

        @Override
        public String getFieldChnName() {
            return "用户名称";
        }

        @Override
        public void appendSb(WirelessItemAssistant item, StringBuilder sb, Gson gson) {
            sb.append(gson.toJson(item.getNick()));

        }

        @Override
        public void fillField(WirelessItemAssistant item, String res, JsonParser parser) {
            if (StringUtils.isEmpty(res)) {
                return;
            }
            item.setNick(parser.parse(res).getAsString());
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            return buildValueField(item.getNick());
        }

    }

    public static class SubTitleField extends WirelessItemField {

        @Override
        public String getFieldName() {
            return "subtitle";
        }

        @Override
        public String getFieldChnName() {
            return "宝贝卖点";
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
//            return (item.get);
            log.info("[build sub title>>>>>>>>>>>>>>>>>>>]" + item.getSellPoint());
            return buildStringField(item.getSellPoint());
        }
    }

    public static class NumIidField extends WirelessItemField {
        public String getFieldName() {
            return "num_id";
        }

        public String getFieldChnName() {
            return "数字ID";
        }

        public void appendSb(WirelessItemAssistant item, StringBuilder sb, Gson gson) {
            sb.append(gson.toJson(item.getNumIid()));
        }

        public void fillField(WirelessItemAssistant item, String res, JsonParser parser) {
            if (StringUtils.isEmpty(res)) {
                return;
            }
            item.setNumIid(parser.parse(res).getAsLong());
        }

        static NumIidField _instance = null;

        public static NumIidField getOne() {
            if (_instance != null) {
                return _instance;
            }
            _instance = new NumIidField();
            return _instance;
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            return buildLongField(item.getNumIid());
        }
    }

    public static class CidItemField extends WirelessItemField {

        public String getFieldName() {
            return "cid";
        }

        public String getFieldChnName() {
            return "宝贝类目";
        }

        public void appendSb(WirelessItemAssistant item, StringBuilder sb, Gson gson) {
            sb.append(item.getCid());
        }

        public void fillField(WirelessItemAssistant item, String res, JsonParser parser) {
            if (StringUtils.isEmpty(res)) {
                return;
            }
            item.setCid(parser.parse(res).getAsLong());
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
//            return item.getCid() == null ? StringUtils.EMPTY : item.getCid().toString();
            return buildLongField(item.getCid());
        }
    }

    public static class DescriptionField extends WirelessItemField {
        public String getFieldName() {
            return "description";
        }

        public String getFieldChnName() {
            return "宝贝描述";
        }

        public void appendSb(WirelessItemAssistant item, StringBuilder sb, Gson gson) {
            sb.append(gson.toJson(item.getDesc()));
        }

        public void fillField(WirelessItemAssistant item, String res, JsonParser parser) {
            if (StringUtils.isEmpty(res)) {
                return;
            }
            item.setDesc(parser.parse(res).getAsString());
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            return buildStringField(item.getDesc());
        }
    }

    public static class TitleField extends WirelessItemField {

        public String getFieldName() {
            return "title";
        }

        public String getFieldChnName() {
            return "宝贝名称";
        }

        public void appendSb(WirelessItemAssistant item, StringBuilder sb, Gson gson) {
            sb.append(gson.toJson(item.getTitle()));
        }

        public void fillField(WirelessItemAssistant item, String res, JsonParser parser) {
            if (StringUtils.isEmpty(res)) {
                return;
            }
            item.setTitle(parser.parse(res).getAsString());
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            return buildStringField(item.getTitle());
        }
    }

    /**
     * promoted_service     String  否   2   消保类型，多个类型以,分割。可取以下值： 2：假一赔三；4：7天无理由退换货；taobao.items.search和taobao.items.vip.search专用
     * @author zrb
     *
     */
    public static class NewPrepayField extends WirelessItemField {
        @Override
        public String getFieldName() {
            return "newprepay";
        }

        @Override
        public String getFieldChnName() {
            return "7天退货";
        }

        /*
         * promoted_service (non-Javadoc)
         * promoted_service     String  否   2   消保类型，多个类型以,分割。可取以下值： 
         * 2：假一赔三；
         * 4：7天无理由退换货；
         * taobao.items.search和taobao.items.vip.search专用
         * @see actions.wireless.WirelessItemField#buildField(actions.wireless.WirelessItemAssistant)
         *   该宝贝是否支持【7天无理由退货】，卖家选择的值只是一个因素，最终以类目和选择的属性条件来确定是否支持7天。填入字符0，表示不支持；未填写或填人字符1，表示支持7天无理由退货；
         */
        @Override
        public String buildField(WirelessItemAssistant item) {
//            log.error(">>>>>[item.get new prepay:]" + item.getNewprepay());
            return buildBooleanField("1".equals(item.getNewprepay()) || item.getNewprepay() == null);
        }
    }

    public static class SkuBarCodeField extends WirelessItemField {
        @Override
        public String getFieldName() {
            return "sku_barcode";
        }

        @Override
        public String getFieldChnName() {
            return "sku 条形码";
        }

        @Override
        public String buildField(WirelessItemAssistant item) {
            List<Sku> skus = item.getSkus();
//            log.error("[>>>>>.skus :]" + new Gson().toJson(item.getSkus()));
            if (CommonUtils.isEmpty(skus)) {
                return buildStringField(StringUtils.EMPTY);
            }
            List<String> list = new ArrayList<String>();
            for (Sku sku : skus) {
                String code = sku.getBarcode();
                list.add(code == null ? StringUtils.EMPTY : code);
            }
            return buildValueField(StringUtils.join(list, ';'));
        }
    }

    protected String buildValueField(String value) {
//        if (value == null) {
//            return StringUtils.EMPTY;
//        }
//        return value;
        return loader.buildValueField(value);
    }

    protected String buildDoubleField(String value) {
        return loader.buildDoubleField(value);
    }

    protected String buildDateField(Date date) {
//        if (date == null) {
//            return StringUtils.EMPTY;
//        }
//        return DateUtil.genYMS().format(date);
        return loader.buildDateField(date);
    }

    protected String buildLongField(Long value) {
//        if (value == null) {
//            return StringUtils.EMPTY;
//        }
        return loader.buildLongField(value);
    }

    protected String buildStringField(String value) {
//        if (value == null) {
//            return StringUtils.EMPTY;
//        }
        return loader.buildStringField(value);
    }

    protected String buildBooleanField(Boolean value) {
//        if (value == null) {
//            return StringUtils.EMPTY;
//        }
//
//        if (value.booleanValue()) {
//            return "1";
//        } else {
//            return "0";
//        }
        return loader.buildBooleanField(value);
    }

    public WirelessItemField() {
        super();
    }

    public WirelessItemField(int index) {
        super();
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public abstract String buildField(WirelessItemAssistant item);

    /**
     *                     "title\tcid\tseller_cids\tstuff_status\tlocation_state\tlocation_city\t" +
                    "item_type\tprice\tauction_increment\tnum\tvalid_thru\tfreight_payer\t" +
                    "post_fee\tems_fee\texpress_fee\thas_invoice\thas_warranty\tapprove_status\t" +
                    "has_showcase\tlist_time\tdescription\tcateProps\tpostage_id\thas_discount\t" +
                    "modified\tupload_fail_msg\tpicture_status\tauction_point\tpicture\tvideo\t" +
                    "skuProps\tinputPids\tinputValues\touter_id\tpropAlias\tauto_fill\tnum_id\t" +
                    "local_cid\tnavigation_type\tuser_name\tsyncStatus\tis_lighting_consigment\t" +
                    "is_xinpin\tfoodparame\tfeatures\tbuyareatype\tglobal_stock_type\tglobal_stock_country\t" +
                    "sub_stock_type\titem_size\titem_weight\tsell_promise\tcustom_design_flag\t" +
                    "wireless_desc\tbarcode\tsku_barcode\tnewprepay\n" +
     */
    public void doWord() {
        /*
         * barcode
         */
    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }

    /**
     * 70e6d735f9ee7304e1e6f9338566929f:2:0:1627207:28335|http://img02.taobaocdn.com/bao/uploaded/i2/1958785004/T2cbW6XQBXXXXXXXXX_
     */
}
