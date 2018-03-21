package smsprovider.QTL;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import smsprovider.ISmsProvider;
import smsprovider.SendInfo;
import smsprovider.SmsProvider;
import smsprovider.SmsStatus;
import smsprovider.SmsUtil;
import smsprovider.XMLParserUtil;

public class QLTProvider extends SmsProvider<QLTResponseMsg> implements ISmsProvider {

    public final static Logger log = LoggerFactory.getLogger(QLTProvider.class);

//    public String sendMsgUrl = "http://221.179.180.158:9002/QxtSms/QxtFirewall";
    public String sendMsgUrl = "http://221.179.180.158:9007/QxtSms/QxtFirewall";
    public String checkBalanceUrl = "http://221.179.180.158:9001/QxtSms/surplus";
//    public String userName = "qiaosh";
//    public String password = "lw7yv1hb";
    public String userName = "tzhgui";
    public String password = "feng0609";
    
    public static int NORMAL_SMS_LENGHT = 67;

    public static int BATCH_MOBILE_NUM = 100;

    private static final QLTProvider INSTANCE = new QLTProvider();

    private QLTProvider() {
    }

    public static QLTProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public SendInfo sendNormalMsg(Long userId, Long ts, String mobile, String content) {

        log.info("Send Normal Msg for " + mobile + ", content:" + content);
        if (content.length() > NORMAL_SMS_LENGHT) {
            return sendLongMsg(userId, ts, mobile, content);

        }

        return sendMsg(userId, ts, mobile, content, SmsUtil.SHORT_SMS);
    }

    @Override
    public SendInfo sendLongMsg(Long userId, Long ts, String mobile, String content) {

        log.info("Send Long Msg for " + mobile + ", content:" + content);
        if (content.length() <= NORMAL_SMS_LENGHT) {
            return sendNormalMsg(userId, ts, mobile, content);
        }

        return sendMsg(userId, ts, mobile, content, SmsUtil.LONG_SMS);
    }

    @Override
    public List<SendInfo> sendNormalMsgBatch(Long userId, Long ts, String mobiles, String content) {

        log.info("Send Normal Msg Batch for " + mobiles + ", content:" + content);
        if (content.length() > NORMAL_SMS_LENGHT) {
            return sendLongMsgBatch(userId, ts, mobiles, content);
        }

        return sendMsgBatch(userId, ts, mobiles, content, SmsUtil.SHORT_SMS);
    }

    @Override
    public List<SendInfo> sendLongMsgBatch(Long userId, Long ts, String mobiles, String content) {

        log.info("Send Long Msg Batch for " + mobiles + ", content:" + content);
        if (content.length() <= NORMAL_SMS_LENGHT) {
            return sendNormalMsgBatch(userId, ts, mobiles, content);

        }

        return sendMsgBatch(userId, ts, mobiles, content, SmsUtil.LONG_SMS);
    }

    @Override
    public int checkBalance() {

        String sendUrl = appendURL("", "", SmsUtil.BALANCE_SMS);
        // log.info(sendUrl);

        String responseMsg = SmsUtil.httpClientExecutor(sendUrl.toString());

        log.info(responseMsg);
        return parseBalanceResponseMsg(responseMsg);

    }

    @Override
    public SendInfo sendMsg(Long userId, Long ts, String mobile, String content, int smsType) {

        mobile = SmsUtil.filterUnNumber(mobile);

        if (!SmsUtil.vaildMobileNum(mobile)) {
            log.info("Mobile Num is not vaild ! " + mobile);
            return new SendInfo(userId, ts, mobile, content, SmsStatus.MOBILENUM_UNVAILD);
        }

        try {
            content = SmsUtil.URLEncodeContent(content);
        } catch (UnsupportedEncodingException e) {
            log.error("URLEncoder content exception:" + content);
            log.error(e.getMessage(), e);
            return new SendInfo(userId, ts, mobile, content, SmsStatus.URLENCODER_EXCEPTION);
        }

        String sendUrl = appendURL(mobile, content, smsType);

        String responseMsg = SmsUtil.httpClientExecutor(sendUrl.toString());
        // String responseMsg =
        // "<?xml version=\"1.0\" encoding=\"gbk\" ?><response><code>03</code><message><desmobile>13656656493</desmobile><msgid>20120522100427227040</msgid></message><message><desmobile>13656656493</desmobile><msgid>20120522100427227041</msgid></message></response>";

        log.info("Response message:" + responseMsg);

        QLTResponseMsg msg = parseXML(responseMsg);

        if (msg == null) {
            return new SendInfo(userId, ts, mobile, content, SmsStatus.RESPONSE_PARSE_EXCEPTION);
        }

        int status = parseResponseMsg(msg);

        return new SendInfo(userId, ts, mobile, content, status, msg.messages.size());
    }

    @Override
    public List<SendInfo> sendMsgBatch(Long userId, Long ts, String mobiles, String content, int smsType) {

        String[] mobileArray = mobiles.split(",");
        /**
         * num of mobiles overflow
         */
        int mobileNum = mobileArray.length;
        if (mobileNum > BATCH_MOBILE_NUM) {
            List<SendInfo> sendInfoList = new ArrayList<SendInfo>();
            sendInfoList.add(new SendInfo(userId, ts, mobiles, content, SmsStatus.BATCH_MOBILE_NUM_OVERFLOW));
            return sendInfoList;
        }

        /**
         * content is not vaild;
         */
        try {
            content = SmsUtil.URLEncodeContent(content);
        } catch (UnsupportedEncodingException e) {
            log.error("URLEncoder content exception:" + content);
            log.error(e.getMessage(), e);
            List<SendInfo> sendInfoList = new ArrayList<SendInfo>();
            sendInfoList.add(new SendInfo(userId, ts, mobiles, content, SmsStatus.URLENCODER_EXCEPTION));
            return sendInfoList;

        }

        List<SendInfo> sendInfoList = new ArrayList<SendInfo>();

        /**
         * check for every mobile num;
         */
        StringBuffer vaildMobiles = new StringBuffer();
        List<String> vaildMobileList = new ArrayList<String>();

        for (int i = 0; i < mobileNum; i++) {
            String mobile = mobileArray[i];

            mobile = SmsUtil.filterUnNumber(mobile);
            if (!SmsUtil.vaildMobileNum(mobile)) {
                log.info("Mobile Num is not vaild ! " + mobile);
                sendInfoList.add(new SendInfo(userId, ts, mobile, content, SmsStatus.MOBILENUM_UNVAILD));
                continue;
            }

            vaildMobiles.append(mobile);
            vaildMobileList.add(mobile);

            if (i != mobileNum - 1) {
                vaildMobiles.append(",");
            }

        }

        String sendUrl = appendURL(vaildMobiles.toString(), content, smsType);

        String responseMsg = SmsUtil.httpClientExecutor(sendUrl.toString());

        log.info("Response message:" + responseMsg);

        QLTResponseMsg msg = parseXML(responseMsg);

        if (msg == null) {
            sendInfoList.clear();
            sendInfoList.add(new SendInfo(userId, ts, mobiles, content, SmsStatus.RESPONSE_PARSE_EXCEPTION));

            return sendInfoList;
        }

        List<QLTResponseMsg.Message> messages = msg.messages;

        int statusCode = parseResponseMsg(msg);

        for (QLTResponseMsg.Message message : messages) {
            sendInfoList.add(new SendInfo(userId, ts, message.desmobile, content, statusCode));
        }

        return sendInfoList;

    }

    @Override
    public int parseResponseMsg(QLTResponseMsg msg) {

        if (msg.code.equals("00")) {
            return SmsStatus.BATCH_SUBMIT_SUCCESS_EXAM;
        } else if (msg.code.equals("01")) {
            return SmsStatus.BATCH_SUBMIT_SUCCESS;
        } else if (msg.code.equals("03")) {
            return SmsStatus.SINGLE_SUBMIT_SUCCESS;
        } else if (msg.code.equals("04")) {
            return SmsStatus.USERNAME_ERROR;
        } else if (msg.code.equals("05")) {
            return SmsStatus.PASSWORD_ERROR;
        } else if (msg.code.equals("06")) {
            return SmsStatus.BALANCE_NOT_ENOUGH;
        } else if (msg.code.equals("07")) {
            return SmsStatus.CONTENT_WORD_CENSOR;
        } else if (msg.code.equals("08")) {
            return SmsStatus.CONTENT_CENSOR;
        } else if (msg.code.equals("09")) {
            return SmsStatus.CONTENT_REPEATE;
        }

        return SmsStatus.OTHERS;
    }

    @Override
    public QLTResponseMsg parseXML(String responseMsg) {

        Document document = XMLParserUtil.init(responseMsg);
        if (document == null) {
            return null;
        }

        Element rsp = (Element) document.getElementsByTagName("response").item(0);

        QLTResponseMsg msg = new QLTResponseMsg();

        msg.setCode(XMLParserUtil.getNodeValue(rsp, "code"));

        NodeList messageNodes = rsp.getElementsByTagName("message");

        List<QLTResponseMsg.Message> messages = new ArrayList<QLTResponseMsg.Message>();

        for (int j = 0; j < messageNodes.getLength(); j++) {
            Element message = (Element) messageNodes.item(j);

            String desmobile = XMLParserUtil.getNodeValue(message, "desmobile");
            String msgid = XMLParserUtil.getNodeValue(message, "msgid");

            messages.add(new QLTResponseMsg.Message(desmobile, msgid));
        }

        msg.messages = messages;
        log.info("Parse xml response finished!");

        return msg;
    }

    @Override
    public String appendURL(String mobile, String content, int msgType) {

        StringBuffer sendUrl = new StringBuffer();

        if (msgType == SmsUtil.BALANCE_SMS) {
            sendUrl.append(checkBalanceUrl);
            sendUrl.append("?OperID=").append(userName);
            sendUrl.append("&OperPass=").append(password);

            return sendUrl.toString();
        }

        sendUrl.append(sendMsgUrl);
        sendUrl.append("?OperID=").append(userName);
        sendUrl.append("&OperPass=").append(password);
        sendUrl.append("&SendTime=");
        sendUrl.append("&ValidTime=");
        sendUrl.append("&AppendID=");
        sendUrl.append("&DesMobile=").append(mobile);
        sendUrl.append("&Content=").append(content);
        if (msgType == SmsUtil.SHORT_SMS) {
            sendUrl.append("&ContentType=15");
        } else if (msgType == SmsUtil.LONG_SMS) {
            sendUrl.append("&ContentType=8");
        }

        return sendUrl.toString();
    }

    public int parseBalanceResponseMsg(String responseMsg) {

        Document document = XMLParserUtil.init(responseMsg);
        if (document == null) {
            return -1;
        }

        Element rsp = (Element) document.getElementsByTagName("resRoot").item(0);

        String balanceNum = XMLParserUtil.getNodeValue(rsp, "rcode");

        return Integer.parseInt(balanceNum);

    }

}
