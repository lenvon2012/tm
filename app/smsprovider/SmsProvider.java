package smsprovider;

import java.util.List;

public abstract class SmsProvider<T> {

    public abstract int parseResponseMsg(T t);

    public abstract SendInfo sendMsg(Long userId, Long ts, String mobile, String content, int smsType);

    public abstract List<SendInfo> sendMsgBatch(Long userId, Long ts, String mobiles, String content, int smsType);

    public abstract String appendURL(String mobile, String content, int msgType);

    public abstract T parseXML(String responseMsg);

}
