package smsprovider;

import java.util.List;

public interface ISmsProvider {

    public SendInfo sendNormalMsg(Long userId, Long ts, String mobile, String content);

    public SendInfo sendLongMsg(Long userId, Long ts, String mobile, String content);

    public List<SendInfo> sendNormalMsgBatch(Long userId, Long ts, String mobiles, String content);

    public List<SendInfo> sendLongMsgBatch(Long userId, Long ts, String mobiles, String content);

    public int checkBalance();

}
