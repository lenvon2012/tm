package smsprovider;

public class SmsStatus {
    /**
     * 发送成功
     */
    public static int SUCCESS = 1;
    
    /**
     * 批量短信提交成功（批量短信待审批）
     */
    public static int BATCH_SUBMIT_SUCCESS_EXAM = 2;
    /**
     * 批量短信提交成功（批量短信跳过审批环节）
     */
    public static int BATCH_SUBMIT_SUCCESS = 3;
    /**
     * 单条短信提交成功
     */
    public static int SINGLE_SUBMIT_SUCCESS = 4;
    
    public static int SMS_SUCCESS = 10;
    /**
     * 手机号码异常
     */
    /**
     * 手机号码有问题，不符合要求
     */
    public static int MOBILENUM_UNVAILD = 11;
    
    /**
     * 批量发送时，手机号码异常
     */
    public static int BATCH_MOBILENUM_UNVAILD = 12;
    /**
     * 批量发送时，手机号码数量超过限制
     */
    public static int BATCH_MOBILE_NUM_OVERFLOW = 13;
    
    
    public static int MOBILENUM_ERROR = 100;
    /**
     * 账户相关错误
     */
    /**
     * 用户名错误
     */
    public static int USERNAME_ERROR = 101;
    /**
     * 密码错误
     */
    public static int PASSWORD_ERROR = 102;
    /**
     * 短信账号余额不足
     */
    public static int BALANCE_NOT_ENOUGH = 103;
    
    
    public static int ACCOUNT_ERROR = 1000;
    /**
     * 内容错误
     */
    
    /**
     * 信息内容中含有限制词(违禁词)
     */
    public static int CONTENT_WORD_CENSOR = 1001;
    /**
     * 信息内容为黑内容
     */
    public static int CONTENT_CENSOR = 1002;
    /**
     * 该用户的该内容 受同天内内容不能重复发 限制
     */
    public static int CONTENT_REPEATE = 1003;
    
    /**
     * 短信内容URLEncoder时出现异常
     */
    public static int URLENCODER_EXCEPTION = 1004;
    
    public static int CONTENT_ERROR = 10000;
    /**
     * parse error
     */
    
    /**
     * 解析返回结果出现异常
     */
    public static int RESPONSE_PARSE_EXCEPTION = 10001;

   
    /**
     * 其他异常错误
     */
    public static int OTHERS = 100000;
    
    /**
     * SZYaoJia
     */
    public static int SZYaoJiaErr = 100001;

    /**
     * EMay
     */
    public static int EMayErr = 100002;
}
