package actions.emay;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import smsprovider.SendInfo;
import smsprovider.SmsStatus;

import cn.b2m.eucp.sdkhttp.SingletonClient;

public class EMaySmsSendAction {
	private static final Logger log = LoggerFactory
			.getLogger(EMaySmsSendAction.class);

	public static SendInfo send(String phone, String msg) {
		if(StringUtils.isEmpty(phone)) {
			return new SendInfo(0L, System.currentTimeMillis(), phone, msg, SmsStatus.EMayErr);
		}
		if(StringUtils.isEmpty(msg)) {
			return new SendInfo(0L, System.currentTimeMillis(), phone, msg, SmsStatus.EMayErr);
		}
		if(msg.startsWith("【速推科技】") == false) {
			msg  = "【速推科技】" + msg;
		}
		/*if(msg.endsWith("回复td退订原因") == false) {
            msg  = msg + "回复td退订原因";
        }*/
		try {
			int i = SingletonClient.getClient().sendSMS(
					new String[] { phone }, msg,
					"", 5);// 带扩展码
			if(i == 0) {
				return new SendInfo(0L, System.currentTimeMillis(), phone, msg, SmsStatus.SUCCESS);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new SendInfo(0L, System.currentTimeMillis(), phone, msg, SmsStatus.EMayErr);
	}
}
