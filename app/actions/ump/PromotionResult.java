package actions.ump;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.item.ItemPlay;
import models.user.User;

import org.apache.commons.lang3.StringUtils;

import bustbapi.TBApi;

import com.ciaosir.client.CommonUtils;

import dao.item.ItemDao;

public class PromotionResult {

    private boolean isSuccess;
    
    private String message;
    
    private int successNum;
    
    private Set<Long> successNumIidSet;
    
    //尚未执行的宝贝，比如前面有太多宝贝失败，后面的就不执行了
    private int notExecuteNum;
    
    private int realErrorNum;
    
    private List<PromotionErrorMsg> errorList;
    
    private int mainErrorType = 0;
    
    private String mainErrorMessage;

    public PromotionResult(boolean isSuccess, String message) {
        super();
        this.isSuccess = isSuccess;
        this.message = message;
    }
    
    public PromotionResult(TBApi api, String prevMessage) {
        super();
        this.isSuccess = true;
        Long numIid = 0L;
        PromotionErrorMsg promotionErrorMsg = createPromotionError(api, numIid);
        
        String errorMessage = checkIsMainError(promotionErrorMsg);
        
        if (mainErrorType <= 0) {
            //没找到mainErrorType，但事实上是错误的
            mainErrorType = PromotionErrorType.OtherErrorType;
            errorMessage = promotionErrorMsg.getErrorMsg();
        }
        if (StringUtils.isEmpty(errorMessage)) {
            errorMessage = "请联系我们！";
        }
        this.message = prevMessage + "，" + errorMessage;
    }
    
    public PromotionResult(User user, int promotionActionType,
            Set<Long> successNumIidSet, int notExecuteNum, List<PromotionErrorMsg> errorList) {
        this(user, promotionActionType, successNumIidSet, notExecuteNum, errorList, "");
    }

    public PromotionResult(User user, int promotionActionType,
            Set<Long> successNumIidSet, int notExecuteNum, List<PromotionErrorMsg> errorList,
            String minShopDiscountMessage) {
        super();
        this.isSuccess = true;
        
        if (CommonUtils.isEmpty(successNumIidSet)) {
            successNumIidSet = new HashSet<Long>();
        }
        if (CommonUtils.isEmpty(errorList)) {
            errorList = new ArrayList<PromotionErrorMsg>();
        }
        
        this.successNum = successNumIidSet.size();
        this.realErrorNum = errorList.size();
        this.notExecuteNum = notExecuteNum;
        this.errorList = errorList;
        
        
        this.message = genPromotionResultMessage(user, promotionActionType, minShopDiscountMessage);
        
        //得到每个宝贝的具体错误信息
        final int maxShowErrorNum = UmpPromotionAction.MaxPromotionNum;
        if (errorList.size() > maxShowErrorNum) {
            errorList = errorList.subList(0, maxShowErrorNum);
        }
        fetchErrorItems(user, errorList);
        
        this.errorList = errorList;
    }
    
    
    private String genPromotionResultMessage(User user, int promotionActionType, String minShopDiscountMessage) {
        
        String message = genSuccessMessage(promotionActionType, successNum);
        
        if (notExecuteNum <= 0 && CommonUtils.isEmpty(errorList)) {

            if (successNum > 0 && StringUtils.isEmpty(minShopDiscountMessage) == false) {
                message += "，" + minShopDiscountMessage;
            }
            
            return message + "！";
        } 
        if (successNum <= 0) {
            message = "";
        } 
        
        if (CommonUtils.isEmpty(errorList) == false) {
            if (StringUtils.isEmpty(message) == false) {
                message += ",";
            }
            message += "失败" + errorList.size() + "个";
        }
        
        if (notExecuteNum > 0) {
            if (StringUtils.isEmpty(message) == false) {
                message += ",";
            }
            message += notExecuteNum + "个宝贝尚未执行";
        }
        
        String mainErrorMessage = getMainErrorMessage(user);
        if (StringUtils.isEmpty(mainErrorMessage) == false) {
            if (StringUtils.isEmpty(message) == false) {
                message += ",";
            }
            message += mainErrorMessage;
        }
        
        
        
        return message;
    }
    
    
    private String getMainErrorMessage(User user) {
        
        if (CommonUtils.isEmpty(errorList)) {
            return "";
        }
        
        String errorMessage = "";
        
        for (PromotionErrorMsg error : errorList) {
            
            errorMessage = checkIsMainError(error);
            if (mainErrorType > 0) {
                break;
            }
            
        }
        
        if (StringUtils.isEmpty(errorMessage) == false) {
            return errorMessage;
        } else {
            return "点击确定后，请查看错误列表！";
        }
        
    }
    
    
    private String checkIsMainError(PromotionErrorMsg error) {
        
        String errorMessage = "";
        
        if (error.getErrorType() == PromotionErrorType.DataBaseSaveError) {
            mainErrorType = error.getErrorType();
            mainErrorMessage = error.getErrorMsg();
            errorMessage = "失败原因是数据存储失败，请联系我们！";
            
        } else if (error.getErrorType() == PromotionErrorType.MinDiscountRateError) {
            errorMessage = "失败原因是店铺最低折扣出错，请设置店铺最低折扣，然后重新执行！";
            mainErrorType = error.getErrorType();
            mainErrorMessage = error.getErrorMsg();
            
        } else if (error.getErrorType() == PromotionErrorType.W2AuthError) {
            errorMessage = "失败原因是您的授权已过期，请重新授权后，再重新执行！";
            mainErrorType = error.getErrorType();
            mainErrorMessage = error.getErrorMsg();
            
        }
        
        return errorMessage;
        
    }
    
    
    //根据numIid，获取失败的宝贝
    private static void fetchErrorItems(User user, List<PromotionErrorMsg> errorList) {
        
        if (CommonUtils.isEmpty(errorList)) {
            return;
        }
        Set<Long> numIidSet = new HashSet<Long>();
        
        for (PromotionErrorMsg promotionError : errorList) {
            if (promotionError == null) {
                continue;
            }
            numIidSet.add(promotionError.getNumIid());
        }
        
        if (CommonUtils.isEmpty(numIidSet)) {
            return;
        }
        
        List<ItemPlay> itemList = ItemDao.findByIds(user.getId(), StringUtils.join(numIidSet, ","));
        
        if (CommonUtils.isEmpty(itemList)) {
            return;
        }
        
        Map<Long, ItemPlay> itemMap = new HashMap<Long, ItemPlay>();
        for (ItemPlay item : itemList) {
            if (item == null) {
                continue;
            }
            itemMap.put(item.getNumIid(), item);
        }
        
        for (PromotionErrorMsg promotionError : errorList) {
            if (promotionError == null) {
                continue;
            }
            ItemPlay item = itemMap.get(promotionError.getNumIid());
            
            promotionError.setItem(item);
        }
        
        
        
        
    }
    
    private static String genSuccessMessage(int promotionActionType, int successNum) {
        
        if (promotionActionType == PromotionActionType.Normal) {
            return "成功" + successNum + "个宝贝";
        } else if (promotionActionType == PromotionActionType.AddPromotion) {
            return "成功添加" + successNum + "个宝贝到促销活动中";
        } else if (promotionActionType == PromotionActionType.ModifyPromotion) {
            return "成功修改" + successNum + "个宝贝的促销活动信息";
        } else if (promotionActionType == PromotionActionType.DeletePromotion) {
            return "成功删除" + successNum + "个宝贝的促销活动";
        } else if (promotionActionType == PromotionActionType.AddMjsItems) {
            return "成功添加" + successNum + "个宝贝到满就送活动中";
        } else if (promotionActionType == PromotionActionType.DeleteMjsItems) {
            return "成功删除" + successNum + "个宝贝的满就送活动";
        } else if (promotionActionType == PromotionActionType.CancelPromotionActivity) {
            return "成功结束" + successNum + "个宝贝促销活动";
        } else if (promotionActionType == PromotionActionType.CancelMjsActivity) {
            return "成功结束" + successNum + "个宝贝的满就送活动";
        } else {
            return "成功" + successNum + "个宝贝";
        }
        
        
    }
    
    

    public static PromotionErrorMsg createPromotionError(TBApi api, Long numIid) {
        
        String errorMsg = "";
        int errorType = PromotionErrorType.NotKnown;
        
        if (api == null) {
            return new PromotionErrorMsg(numIid, errorMsg, errorType);
        }
        
        String subCode = api.getSubErrorCode();
        errorMsg = api.getSubErrorMsg();
        

        if (StringUtils.isEmpty(subCode) == false 
                && subCode.contains("isv.w2-security-authorize-invalid")) {
            errorMsg = "授权过期，请重新授权后再试！";
            errorType = PromotionErrorType.W2AuthError;
            return new PromotionErrorMsg(numIid, errorMsg, errorType);
            
        } else if (StringUtils.isEmpty(subCode) == false 
                && subCode.contains("isv.activity-not-exist:activityId")) {
            errorMsg = "该宝贝活动之前已被删除！";
            errorType = PromotionErrorType.PromotionDeletedInTaobao;
            return new PromotionErrorMsg(numIid, errorMsg, errorType);
            
        } else if (StringUtils.isEmpty(errorMsg) == false 
                && errorMsg.contains("商品折扣幅度不能低于设置的店铺最低折扣")) {
            errorType = PromotionErrorType.MinDiscountRateError;
            return new PromotionErrorMsg(numIid, errorMsg, errorType);
            
        }
        
        return new PromotionErrorMsg(numIid, errorMsg, errorType);
    }

    
    
    
    
    
    
    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getSuccessNum() {
        return successNum;
    }

    public void setSuccessNum(int successNum) {
        this.successNum = successNum;
    }

    public Set<Long> getSuccessNumIidSet() {
        return successNumIidSet;
    }

    public void setSuccessNumIidSet(Set<Long> successNumIidSet) {
        this.successNumIidSet = successNumIidSet;
    }

    public int getNotExecuteNum() {
        return notExecuteNum;
    }

    public void setNotExecuteNum(int notExecuteNum) {
        this.notExecuteNum = notExecuteNum;
    }

    public int getRealErrorNum() {
        return realErrorNum;
    }

    public void setRealErrorNum(int realErrorNum) {
        this.realErrorNum = realErrorNum;
    }

    public List<PromotionErrorMsg> getErrorList() {
        return errorList;
    }

    public void setErrorList(List<PromotionErrorMsg> errorList) {
        this.errorList = errorList;
    }

    public int getMainErrorType() {
        return mainErrorType;
    }

    public void setMainErrorType(int mainErrorType) {
        this.mainErrorType = mainErrorType;
    }

    public String getMainErrorMessage() {
        return mainErrorMessage;
    }

    public void setMainErrorMessage(String mainErrorMessage) {
        this.mainErrorMessage = mainErrorMessage;
    }














    public static class PromotionActionType {
        
        public static final int Normal = 0;
        
        public static final int AddPromotion = 1;
        
        public static final int ModifyPromotion = 2;
        
        public static final int DeletePromotion = 4;
        
        public static final int CancelPromotionActivity = 8;
        
        public static final int AddMjsItems = 16;
        
        public static final int DeleteMjsItems = 32;
        
        public static final int CancelMjsActivity = 64;
        
        //public static final int RestartDiscountActivity = 128;
    }
    
    
    public static class PromotionErrorType {
        
        public static final int NotKnown = 0;
        
        public static final int DataBaseSaveError = 1;//数据库中存储失败
        
        public static final int W2AuthError = 2;//授权错误
        
        public static final int MinDiscountRateError = 4;//最低折扣
        
        public static final int CannotFindPromotionInDB = 8;//数据库中找不到这个活动了
        
        public static final int ItemAlreadyPromotioned = 16;//宝贝已经在活动中打折了
        
        public static final int PromotionDeletedInTaobao = 32;//活动在淘宝删除了
        
        public static final int OtherErrorType = 64;
        
    }
    
    
    
    public static class PromotionErrorMsg {
        
        private Long numIid;
        
        private ItemPlay item;
        
        private String errorMsg;
        
        private int errorType;

        public PromotionErrorMsg(Long numIid, String errorMsg, int errorType) {
            super();
            this.numIid = numIid;
            this.errorMsg = errorMsg;
            this.errorType = errorType;
        }

        public ItemPlay getItem() {
            return item;
        }

        public void setItem(ItemPlay item) {
            this.item = item;
        }

        public String getErrorMsg() {
            return errorMsg;
        }

        public void setErrorMsg(String errorMsg) {
            this.errorMsg = errorMsg;
        }

        public Long getNumIid() {
            return numIid;
        }

        public void setNumIid(Long numIid) {
            this.numIid = numIid;
        }

        public int getErrorType() {
            return errorType;
        }

        public void setErrorType(int errorType) {
            this.errorType = errorType;
        }
        
        
    }
    
    
}
