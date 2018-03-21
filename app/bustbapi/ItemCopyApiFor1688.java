package bustbapi;

import actions.ItemGetAction;
import com.ciaosir.client.CommonUtils;
import com.taobao.api.ApiException;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.ItemImg;
import com.taobao.api.domain.PropImg;
import models.user.User;
import result.TMResult;
import utils.CommonUtil;

import java.util.List;

/**
 * Created by User on 2017/11/16.
 */
public class ItemCopyApiFor1688 extends ItemCopyApiBase {
    public ItemCopyApiFor1688(User user, Item item) {
        super(user, item);
    }

    public static TMResult<Item> itemCarrier(User user, Item item){
        ItemCopyApiFor1688 itemCopyApi = new ItemCopyApiFor1688(user, item);

        return itemCopyApi.itemCopy();
    }

    @Override
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
            // reqSetDesc(); 移到itemUpdate()方法里执行了  先保证宝贝能成功上传 在上传描述里的图片
            req.setDesc("<p style=\"height:0px;margin:0px;color:#ffffff;\">~~~~~</p>");
            reqSetLang();
            reqSetOuterId();
            reqSetSellerCids();
            reqSetQualification();
            beforeReqExecute();
            reqExecute();
            // 更新宝贝描述
            if (user.getUserNick().equals("clorest510") || user.getUserNick().equals("boyvon")) {
                // 测试使用  这两个用户在进行宝贝复制时，不复制描述信息

                itemUpdate();
            } else {
                itemUpdate();
            }
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
        Item rtnItem=resp.getItem();
        rtnItem.setTitle(item.getTitle());
        rtnItem.setPicUrl(item.getPicUrl());
        return new TMResult<Item>(true, "复制成功",rtnItem);
    }

    @Override
    protected void afterReqExecute() {
        if (resp == null) return;
        if (resp.isSuccess()) addPic();

    }

    // 添加副图
    private void addPic() {
        Long ignoreImgIndex = 1L;

        //添加副图
        Item itemNow = resp.getItem();
        List<ItemImg> imgs = item.getItemImgs();
        if (!CommonUtils.isEmpty(imgs)) {
            for (ItemImg itemImg : imgs) {
                if (itemImg.getPosition() != null && itemImg.getPosition().longValue() == ignoreImgIndex) {
                    continue;
                }
                new ItemApi.ItemImgPictureAdd(user, itemNow.getNumIid(), itemImg).call();
            }
        }
        //添加SKU图片
        //获取要复制的宝贝信息
        List<String> colorProps = ItemGetAction.getColorProperties(user.getSessionKey(), itemNow.getNumIid());
        List<PropImg> propImgs = item.getPropImgs();
        if (!CommonUtil.isNullOrSizeZero(propImgs)&&!CommonUtil.isNullOrSizeZero(colorProps)) {
            for (int i=0;i<propImgs.size();i++) {
                PropImg propImg=propImgs.get(i);
                if (propImg.getProperties().contains("-")) {
                    propImg.setProperties(colorProps.get(i));
                }
                new ItemApi.ItemPropPictureAdd(user, itemNow.getNumIid(), propImg).call();
            }
        }
    }

}
