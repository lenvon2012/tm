package actions.batch;

import java.util.List;

import models.item.ItemPlay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;

public class BatchEditResult {

    private static final Logger log = LoggerFactory.getLogger(BatchEditResult.class);
    
    private static final int MaxShowErrorNum = 100;
    
    private boolean isSuccess;
    
    private String message;
    
    private List<BatchEditErrorMsg> errorMsgList;
    
    private BatchEditResStatus mainErrorType = null;

    public BatchEditResult(boolean isSuccess, String message) {
        super();
        this.isSuccess = isSuccess;
        this.message = message;
    }
    
    
    public BatchEditResult(String message, List<BatchEditErrorMsg> errorMsgList) {
        super();
        this.isSuccess = true;
        this.message = message;
        this.errorMsgList = errorMsgList;
        
        findMainErrorType();
    }

    
    private void findMainErrorType() {
        
        if (CommonUtils.isEmpty(errorMsgList)) {
            this.message += "!";
            return;
        }
        
        for (BatchEditErrorMsg errorMsg : errorMsgList) {
            if (BatchEditResStatus.AuthOutDate.equals(errorMsg.getStatus())) {
                this.message += "，主要原因是淘宝授权过期！";
                this.mainErrorType = errorMsg.getStatus();
                break;
            } else if (BatchEditResStatus.DBError.equals(errorMsg.getStatus())) {
                this.message += "，主要原因是数据库出现异常，请联系我们！";
                this.mainErrorType = errorMsg.getStatus();
                break;
            }
        }
        
        if (errorMsgList.size() > MaxShowErrorNum) {
            errorMsgList = errorMsgList.subList(0, MaxShowErrorNum);
        }
    }
    


    public BatchEditResStatus getMainErrorType() {
        return mainErrorType;
    }


    public void setMainErrorType(BatchEditResStatus mainErrorType) {
        this.mainErrorType = mainErrorType;
    }


    public List<BatchEditErrorMsg> getErrorMsgList() {
        return errorMsgList;
    }

    public void setErrorMsgList(List<BatchEditErrorMsg> errorMsgList) {
        this.errorMsgList = errorMsgList;
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
    
    
    public static boolean checkIsContinueExecute(List<BatchEditErrorMsg> errorMsgList) {
        
        if (CommonUtils.isEmpty(errorMsgList)) {
            return true;
        }
        
        for (BatchEditErrorMsg errorMsg : errorMsgList) {
            if (errorMsg == null) {
                continue;
            }
            
            if (BatchEditResStatus.AuthOutDate.equals(errorMsg.getStatus())) {
                return false;
            } else if (BatchEditResStatus.DBError.equals(errorMsg.getStatus())) {
                return false;
            }
        }
        
        return true;
    }
    
    
    
    
    public static class BatchEditErrorMsg {

        private BatchEditResStatus status;
        
        private String errorMsg;
        
        private ItemPlay item;

        public BatchEditErrorMsg(BatchEditResStatus status,
                String errorMsg, ItemPlay item) {
            super();
            this.status = status;
            this.errorMsg = errorMsg;
            this.item = item;
        }

        
        public BatchEditResStatus getStatus() {
            return status;
        }

        public void setStatus(BatchEditResStatus status) {
            this.status = status;
        }

        public String getErrorMsg() {
            return errorMsg;
        }

        public void setErrorMsg(String errorMsg) {
            this.errorMsg = errorMsg;
        }

        public ItemPlay getItem() {
            return item;
        }

        public void setItem(ItemPlay item) {
            this.item = item;
        }
        
        
        
    }
    
    
    public enum BatchEditResStatus {
        
        AuthOutDate, DBError, SkipItem, Success, OtherError, CallApiError
        
    }
}
