package actions.fenxiao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.item.ItemPlay;
import models.user.User;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.TBItemUtil;
import actions.batch.BatchEditResult;
import actions.batch.BatchEditResult.BatchEditErrorMsg;
import actions.batch.BatchEditResult.BatchEditResStatus;
import actions.fenxiao.FenxiaoPriceConfig.FenxiaoDecimalType;
import actions.fenxiao.FenxiaoPriceConfig.FenxiaoModifyPriceType;
import actions.fenxiao.FenxiaoPriceConfig.FenxiaoPriceOverflowType;
import actions.fenxiao.FenxiaoPriceConfig.FenxiaoSkuEditType;
import actions.sku.SkuPriceEditAction;
import actions.sku.SkuPriceEditAction.SkuPriceBean;
import actions.sku.SkuPriceEditAction.SkuPriceEditResult;
import autotitle.ItemPropAction;
import autotitle.ItemPropAction.PropUnit;

import com.ciaosir.client.CommonUtils;
import com.taobao.api.domain.FenxiaoProduct;
import com.taobao.api.domain.FenxiaoSku;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.Sku;

public class FenxiaoPriceEditAction {

    private static final Logger log = LoggerFactory.getLogger(FenxiaoPriceEditAction.class);
    
    private static final int MaxModifyItemNum = 300;
    
    //参数：user, itemList, config: modifyType, modifyParameter, 
    
    public static BatchEditResult doEditFenxiaoPrice(User user, List<ItemPlay> itemList, 
            FenxiaoPriceConfig config) {
        
        if (CommonUtils.isEmpty(itemList)) {
            return new BatchEditResult(false, "请先选择宝贝，当前没有需要改价的宝贝！");
        }
        if (itemList.size() > MaxModifyItemNum) {
            return new BatchEditResult(false, "为防止您过久等待，一次最多只能提交" + MaxModifyItemNum + "个宝贝！");
        }
        
        BatchEditResult checkRes = checkFenxiaoPriceConfig(config);
        if (checkRes.isSuccess() == false) {
            return new BatchEditResult(false, checkRes.getMessage());
        }
        Set<Long> numIidSet = new HashSet<Long>();
        for (ItemPlay item : itemList) {
            if (item == null) {
                continue;
            }
            numIidSet.add(item.getNumIid());
        }
        
        Map<Long, Item> tbItemMap = TBItemUtil.findTaobaoItemMap(user, numIidSet);
        Map<Long, FenxiaoProduct> fenxiaoProductMap = TBItemUtil.findFenxiaoProductMap(user, itemList);
        
        Set<Long> successNumIidSet = new HashSet<Long>();
        Set<Long> skipNumIidSet = new HashSet<Long>();
        List<BatchEditErrorMsg> errorMsgList = new ArrayList<BatchEditErrorMsg>();
        
        for (ItemPlay itemPlay : itemList) {
            if (itemPlay == null) {
                continue;
            }
            if (BatchEditResult.checkIsContinueExecute(errorMsgList) == false) {
                break;
            }
            
            Item tbItem = tbItemMap.get(itemPlay.getNumIid());
            FenxiaoProduct fenxiaoProduct = fenxiaoProductMap.get(itemPlay.getNumIid());
            
            BatchEditErrorMsg errorMsg = doEditOneItemPrice(user, tbItem, itemPlay, 
                    fenxiaoProduct, config);
            
            if (BatchEditResStatus.Success.equals(errorMsg.getStatus())) {
                successNumIidSet.add(itemPlay.getNumIid());
            } else if (BatchEditResStatus.SkipItem.equals(errorMsg.getStatus())) {
                skipNumIidSet.add(itemPlay.getNumIid());
            } else {
                errorMsgList.add(errorMsg);
            }
            
        }
        
        int notExecuteNum = itemList.size() - successNumIidSet.size() - skipNumIidSet.size()
                - errorMsgList.size();
        
        String prevMessage = "成功修改" + successNumIidSet.size() + "个宝贝价格";
        
        if (CommonUtils.isEmpty(skipNumIidSet) == false) {
            prevMessage += "，跳过了" + skipNumIidSet.size() + "个宝贝";
        }
        if (CommonUtils.isEmpty(errorMsgList) == false) {
            prevMessage += "，失败了" + errorMsgList.size() + "个宝贝";
        }
        if (notExecuteNum > 0) {
            prevMessage += "，有" + notExecuteNum + "个宝贝尚未执行";
        }
        
        BatchEditResult priceRes = new BatchEditResult(prevMessage, errorMsgList);
        
        return priceRes;
    }
    
    
    private static BatchEditErrorMsg doEditOneItemPrice(User user, Item tbItem, 
            ItemPlay itemPlay, FenxiaoProduct fenxiaoProduct,
            FenxiaoPriceConfig config) {
        
        try {
            
            if (tbItem == null) {
                BatchEditErrorMsg errorMsg = new BatchEditErrorMsg(BatchEditResStatus.OtherError,
                        "从淘宝找不到该宝贝！", itemPlay);
                return errorMsg;
            }
            if (fenxiaoProduct == null) {
                BatchEditErrorMsg errorMsg = new BatchEditErrorMsg(BatchEditResStatus.OtherError,
                        "找不到该宝贝的分销数据！", itemPlay);
                return errorMsg;
            }
            
            final double lowRetailPrice = parseYuanStrToYuan(fenxiaoProduct.getRetailPriceLow());
            final double highRetailPrice = parseYuanStrToYuan(fenxiaoProduct.getRetailPriceHigh());
            final double productCostPrice = parseYuanStrToYuan(fenxiaoProduct.getCostPrice());
            final double itemOriginPrice = parseYuanStrToYuan(tbItem.getPrice());
            
            
            
            List<Sku> skuList = tbItem.getSkus();
            List<SkuPriceBean> skuBeanList = new ArrayList<SkuPriceBean>();
            
            if (CommonUtils.isEmpty(skuList) == false) {
                
                final FenxiaoSkuEditType skuEditType = config.getSkuEditType();
                final List<FenxiaoSku> fenxiaoSkuList = fenxiaoProduct.getSkus();
                
                if (FenxiaoSkuEditType.skipSkuItems.equals(skuEditType)) {
                    BatchEditErrorMsg errorMsg = new BatchEditErrorMsg(BatchEditResStatus.SkipItem,
                            "", itemPlay);
                    return errorMsg;
                    
                } else if (FenxiaoSkuEditType.doEditToSku.equals(skuEditType)) {
                    
                    skuBeanList = calcuSkuResultPrice(lowRetailPrice, highRetailPrice, 
                            productCostPrice,
                            tbItem, skuList, fenxiaoSkuList, config);
                    
                } else {
                    BatchEditErrorMsg errorMsg = new BatchEditErrorMsg(BatchEditResStatus.OtherError,
                            "属性价格编辑配置错误，请联系我们！", itemPlay);
                    return errorMsg;
                }
            } 
            
            //剩下都是要改价的
            BigDecimal itemResultPrice = calcuResultPrice(lowRetailPrice, highRetailPrice, 
                    productCostPrice, itemOriginPrice, config);
            
            //要根据sku和宝贝一口价，调整宝贝一口价，使一口价在sku价格内，防止改价失败
            itemResultPrice = adjustItemResultPriceBySkuResultPrice(itemResultPrice, skuBeanList);
            
            //真正改价
            SkuPriceEditResult skuEditRes = SkuPriceEditAction.doEditSkuPrice(user, itemPlay, 
                    itemResultPrice, skuBeanList);
                    
            if (skuEditRes.isSuccess()) {
                return new BatchEditErrorMsg(BatchEditResStatus.Success, "", itemPlay);
            } else {
                String message = skuEditRes.getMessage();
                if (StringUtils.isEmpty(message)) {
                    message = "改价失败，请联系我们！";
                }
                return new BatchEditErrorMsg(BatchEditResStatus.CallApiError, message, 
                        itemPlay);
            }
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            String errorMsg = ex.getMessage();
            if (StringUtils.isEmpty(errorMsg)) {
                errorMsg = "系统出现异常，请联系我们！";
            }
            return new BatchEditErrorMsg(BatchEditResStatus.OtherError,
                    errorMsg, itemPlay);
        }
        
        
    }
    
    
    private static BigDecimal adjustItemResultPriceBySkuResultPrice(BigDecimal itemResultPrice,
            List<SkuPriceBean> skuBeanList) {
        
        if (CommonUtils.isEmpty(skuBeanList)) {
            return itemResultPrice;
        }
        
        BigDecimal minSkuPrice = null;
        BigDecimal maxSkuPrice = null;
        
        for (SkuPriceBean skuBean : skuBeanList) {
            BigDecimal skuPrice = skuBean.getSkuPrice();
            if (skuBean.getQuantity() != null && skuBean.getQuantity() > 0L) {
                if (minSkuPrice == null || minSkuPrice.compareTo(skuPrice) > 0) {
                    minSkuPrice = skuPrice;
                }
                if (maxSkuPrice == null || maxSkuPrice.compareTo(skuPrice) < 0) {
                    maxSkuPrice = skuPrice;
                }
            }
        }
        
        if (minSkuPrice != null && minSkuPrice.compareTo(itemResultPrice) > 0) {
            itemResultPrice = new BigDecimal(minSkuPrice.toString());
        } else if (maxSkuPrice != null && maxSkuPrice.compareTo(itemResultPrice) < 0) {
            itemResultPrice = new BigDecimal(maxSkuPrice.toString());
        }
        
        return itemResultPrice;
    }
    
    
    //注意宝贝的sku和分销product的sku可能不一致的
    //如果宝贝有sku，但在分销的sku中找不到，那么就用宝贝的采购价
    //
    private static List<SkuPriceBean> calcuSkuResultPrice(double lowRetailPrice, double highRetailPrice,
            double productCostPrice, 
            Item tbItem, List<Sku> itemSkuList, List<FenxiaoSku> fenxiaoSkuList, 
            FenxiaoPriceConfig config) throws Exception {
     
        if (CommonUtils.isEmpty(itemSkuList)) {
            return new ArrayList<SkuPriceBean>();
        }
        if (CommonUtils.isEmpty(fenxiaoSkuList)) {
            //throw new Exception("分销的sku属性为空，而宝贝的sku属性却不为空！");
            fenxiaoSkuList = new ArrayList<FenxiaoSku>();
        }
        
        Map<String, FenxiaoSku> fenxiaoSkuMap = new HashMap<String, FenxiaoSku>();
        
        for (FenxiaoSku fenxiaoSku : fenxiaoSkuList) {
            if (fenxiaoSku == null) {
                continue;
            }
            fenxiaoSkuMap.put(fenxiaoSku.getProperties(), fenxiaoSku);
        }
        
        List<SkuPriceBean> skuBeanList = new ArrayList<SkuPriceBean>();
        
        List<PropUnit> propList = ItemPropAction.mergePropAlis(tbItem);
        
        for (Sku itemSku : itemSkuList) {
            if (itemSku == null) {
                continue;
            }
            
            final String properties = itemSku.getProperties();
            final String propertyNames = SkuPriceEditAction.getSkuPropertyNames(itemSku, propList);
            
            FenxiaoSku fenxiaoSku = fenxiaoSkuMap.get(properties);
            double skuCostPrice = 0;
            if (fenxiaoSku == null) {
                //throw new Exception("分销的sku属性中找不到\"" + propertyNames + "\"这个sku！");
                skuCostPrice = productCostPrice;
            } else {
                skuCostPrice = parseYuanStrToYuan(fenxiaoSku.getCostPrice());
            }
            
            final double skuOriginPrice = parseYuanStrToYuan(itemSku.getPrice());
            
            BigDecimal skuResultPrice = calcuResultPrice(lowRetailPrice, highRetailPrice, 
                    skuCostPrice, skuOriginPrice, config);
            
            SkuPriceBean skuBean = new SkuPriceBean(properties, propertyNames, skuResultPrice.toString(), 
                    skuResultPrice, itemSku.getQuantity(), itemSku.getPrice());
            
            skuBeanList.add(skuBean);
        }
        
        return skuBeanList;
        
    }
    
    
    
    private static BigDecimal calcuResultPrice(double lowRetailPrice, double highRetailPrice,
            double costPrice, double originPrice, FenxiaoPriceConfig config) throws Exception {
        
        final FenxiaoModifyPriceType modifyType = config.getModifyType();
        final double modifyParameter = config.getModifyParameter();
        
        final double minProfit = config.getMinProfit();
        final FenxiaoPriceOverflowType overflowType = config.getOverType();
        final FenxiaoDecimalType decimalType = config.getDecimalType();
        
        
        double resultPrice = 0;
        
        if (FenxiaoModifyPriceType.lowRetailPercentAdd.equals(modifyType)) {
            resultPrice = lowRetailPrice + lowRetailPrice * modifyParameter / 100;
        } else if (FenxiaoModifyPriceType.lowRetailFixedAdd.equals(modifyType)) {
            resultPrice = lowRetailPrice + modifyParameter;
        } else if (FenxiaoModifyPriceType.highRetailPercentMinus.equals(modifyType)) {
            resultPrice = highRetailPrice - highRetailPrice * modifyParameter / 100;
        } else if (FenxiaoModifyPriceType.highRetailFixedMinus.equals(modifyType)) {
            resultPrice = highRetailPrice - modifyParameter;
        } else if (FenxiaoModifyPriceType.costPricePercentAdd.equals(modifyType)) {
            resultPrice = costPrice + costPrice * modifyParameter / 100;
        } else if (FenxiaoModifyPriceType.costPriceFixedAdd.equals(modifyType)) {
            resultPrice = costPrice + modifyParameter;
        } else if (FenxiaoModifyPriceType.originPricePercentAdd.equals(modifyType)) {
            resultPrice = originPrice + originPrice * modifyParameter / 100;
        } else if (FenxiaoModifyPriceType.originPriceFixedAdd.equals(modifyType)) {
            resultPrice = originPrice + modifyParameter;
        } else if (FenxiaoModifyPriceType.originPricePercentMinus.equals(modifyType)) {
            resultPrice = originPrice - originPrice * modifyParameter / 100;
        } else if (FenxiaoModifyPriceType.originPriceFixedMinus.equals(modifyType)) {
            resultPrice = originPrice - modifyParameter;
        } else {
            
            throw new Exception("改价类型配置错误，请联系我们！");
        }
        
        //先判断利润
        if (minProfit != 0) {
            if (resultPrice - costPrice < minProfit) {
                resultPrice = costPrice + minProfit;
            }
        }
        
        //再是小数点
        double formatHighRetailPrice = highRetailPrice;
        double formatLowRetailPrice = lowRetailPrice;
        double formatResultPrice = resultPrice;
        
        int formatRate = 1;
        if (FenxiaoDecimalType.noDecimal.equals(decimalType)) {
            formatRate = 1;
        } else if (FenxiaoDecimalType.oneDecimal.equals(decimalType)) {
            formatRate = 10;
        } else if (FenxiaoDecimalType.twoDecimal.equals(decimalType)) {
            formatRate = 100;
        } else {
            throw new Exception("改价小数点位数配置错误，请联系我们！");
        }
        
        formatHighRetailPrice = Math.floor(highRetailPrice * formatRate) / formatRate;
        formatLowRetailPrice = Math.ceil(lowRetailPrice * formatRate) / formatRate;
        formatResultPrice = Math.ceil(resultPrice * formatRate) / formatRate;
        
        //格式化后，最高价反而低于最低价了
        if (formatHighRetailPrice < formatLowRetailPrice) {
            formatHighRetailPrice = highRetailPrice;
            formatLowRetailPrice = lowRetailPrice;
        }
        
        
        //但比如采购价加价的时候，可能加价结果还低于最低零售价的。。。。。
        //最后是检查最低价和最高价溢出
        if (formatResultPrice > highRetailPrice) {
            formatResultPrice = formatHighRetailPrice;
        } else if (formatResultPrice < lowRetailPrice) {
            formatResultPrice = formatLowRetailPrice;
        }
        
        /*
        if (formatResultPrice > highRetailPrice || formatResultPrice < lowRetailPrice) {
            if (FenxiaoPriceOverflowType.useLowRetailPrice.equals(overflowType)) {
                formatResultPrice = formatLowRetailPrice;
            } else if (FenxiaoPriceOverflowType.useHighRetailPrice.equals(overflowType)) {
                formatResultPrice = formatHighRetailPrice;
            } else {
                throw new Exception("改价溢出配置错误，请联系我们！");
            }
        }*/
        
        BigDecimal resultDecimal = new BigDecimal(String.valueOf(formatResultPrice));
        
        return resultDecimal;
    }
    
    
    
    /*
    private static double parseFenStrToYuan(String fenStr) {
        
        double price = Double.parseDouble(fenStr);
        
        price = price / 100;
        
        return price;
        
    }
    */
    
    private static double parseYuanStrToYuan(String fenStr) {
        
        double price = Double.parseDouble(fenStr);
        
        return price;
        
    }
    

    
    public static BatchEditResult checkFenxiaoPriceConfig(FenxiaoPriceConfig config) {
        
        if (config == null) {
            return new BatchEditResult(false, "系统出现异常，改价配置为空，请联系我们！");
        }
        
        final FenxiaoModifyPriceType modifyType = config.getModifyType();
        final double modifyParameter = config.getModifyParameter();
        
        final FenxiaoSkuEditType skuEditType = config.getSkuEditType();
        final double minProfit = config.getMinProfit();
        final FenxiaoPriceOverflowType overflowType = config.getOverType();
        final FenxiaoDecimalType decimalType = config.getDecimalType();
        
        
        if (modifyType == null || skuEditType == null || overflowType == null || decimalType == null) {
            return new BatchEditResult(false, "改价配置错误，请重新配置，或联系我们！");
        } 
        
        if (FenxiaoModifyPriceType.lowRetailPercentAdd.equals(modifyType)
                || FenxiaoModifyPriceType.costPricePercentAdd.equals(modifyType)
                || FenxiaoModifyPriceType.originPricePercentAdd.equals(modifyType)) {
            
            if (modifyParameter < 0) {
                return new BatchEditResult(false, "加价百分比不能低于0，请重新设置！");
            }
        } else if (FenxiaoModifyPriceType.lowRetailFixedAdd.equals(modifyType)
                || FenxiaoModifyPriceType.costPriceFixedAdd.equals(modifyType)
                || FenxiaoModifyPriceType.originPriceFixedAdd.equals(modifyType)) {
            
            if (modifyParameter < 0) {
                return new BatchEditResult(false, "固定金额加价不能低于0，请重新设置！");
            }
        } else if (FenxiaoModifyPriceType.highRetailPercentMinus.equals(modifyType)
                || FenxiaoModifyPriceType.originPricePercentMinus.equals(modifyType)) {
            
            if (modifyParameter < 0) {
                return new BatchEditResult(false, "减价百分比不能低于0，请重新设置！");
            }
            if (modifyParameter >= 100) {
                return new BatchEditResult(false, "减价百分比必须小于100，请重新设置！");
            }
            
        } else if (FenxiaoModifyPriceType.highRetailFixedMinus.equals(modifyType)
                || FenxiaoModifyPriceType.originPriceFixedMinus.equals(modifyType)) {
            
            if (modifyParameter < 0) {
                return new BatchEditResult(false, "固定金额减价不能低于0，请重新设置！");
            }
        } else {
            return new BatchEditResult(false, "改价配置错误，请重新配置，或联系我们！");
        }
        
        return new BatchEditResult(true, "");
    }
    
    
    
}
