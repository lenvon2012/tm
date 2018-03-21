package actions.sms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.sms.SmsSendCount;
import models.sms.SmsSendLog;
import models.traderatesms.RateSmsSendLog;
import models.user.User;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import smsprovider.SendInfo;
import smsprovider.SmsStatus;
import actions.emay.EMaySmsSendAction;
import actions.sms.SmsSendLaiqt.SmsResult;

import com.ciaosir.client.CommonUtils;

import configs.TMConfigs;

public class SmsAction {
	private static final Logger log = LoggerFactory.getLogger(SmsAction.class);
	
	public static enum SmsSendRet implements Serializable {
        SUCCESS(true, "发送成功"),
        USER_NULL(false, "登陆卖家为空"), 
        USER_SMS_NOT_ONLINE(false, "登陆卖家未开启发送短信功能"), 
        PHONE_ILLEGAL(false, "电话号码不合法"), 
        CONTENT_EMPTY(false, "发送内容为空"),
        SMS_SEND_FAIL(false, "运营商短信发送失败"),
        NO_REMAIN_COUNT(false, "短信余额不足");

        private boolean success;

        private String msg;

        private SmsSendRet() {

        }

        private SmsSendRet(boolean success, String chapingmsg) {
            this.success = success;
            this.msg = chapingmsg;
        }

        public boolean isSuccess() {
			return success;
		}

		public void setSuccess(boolean success) {
			this.success = success;
		}

		public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }
	
	public static List<SmsSendRet> batchSendSms(User user, List<String> phones, String content) {
		List<SmsSendRet> rets = new ArrayList<SmsAction.SmsSendRet>();
		if(CommonUtils.isEmpty(phones)) {
			return rets;
		}
		int size = phones.size();
		if(user == null) {
			return genFullFailSmsSendRetList(size, SmsSendRet.USER_NULL);
		}
		if(StringUtils.isEmpty(content)) {
			return genFullFailSmsSendRetList(size, SmsSendRet.CONTENT_EMPTY);
		}
		if (!TMConfigs.SMS_ONLINE) {
			return genFullFailSmsSendRetList(size, SmsSendRet.USER_SMS_NOT_ONLINE);
	    }
		
		long remainCount = SmsSendCount.countSmsSendUsed(user.getId());
		if(remainCount <= 0) {
			return genFullFailSmsSendRetList(size, SmsSendRet.NO_REMAIN_COUNT);
		}
		
		// 有足够的短信余额发送这批短信
		int successNum = 0;
		if(remainCount >= size) {
			for(String phone : phones) {
				SmsSendRet ret = sendSms(user, phone, content);
				rets.add(ret);
				if(ret.isSuccess()) {
					successNum++;
				}
			}
			SmsSendCount.addUsedCount(user.getId(), successNum);
		} 
		// 余额只够发送一部分短信
		else {
			for(String phone : phones) {
				if(successNum >= remainCount) {
					rets.add(SmsSendRet.NO_REMAIN_COUNT);
				} else {
					SmsSendRet ret = sendSms(user, phone, content);
					rets.add(ret);
					if(ret.isSuccess()) {
						successNum++;
					}
				}
			}
			SmsSendCount.addUsedCount(user.getId(), successNum);
		}
		
		
		return rets;
	}
	
	public static List<SmsSendRet> batchSendRateSms(User user, Map<Long, String> map, String content, String shopName) {
		List<SmsSendRet> rets = new ArrayList<SmsAction.SmsSendRet>();
		if(CommonUtils.isEmpty(map)) {
			return rets;
		}
		int size = map.size();
		if(user == null) {
			return genFullFailSmsSendRetList(size, SmsSendRet.USER_NULL);
		}
		if(StringUtils.isEmpty(content)) {
			return genFullFailSmsSendRetList(size, SmsSendRet.CONTENT_EMPTY);
		}
		if (!TMConfigs.SMS_ONLINE) {
			return genFullFailSmsSendRetList(size, SmsSendRet.USER_SMS_NOT_ONLINE);
		}
		
		long remainCount = SmsSendCount.countSmsSendUsed(user.getId());
		if(remainCount <= 0) {
			return genFullFailSmsSendRetList(size, SmsSendRet.NO_REMAIN_COUNT);
		}
		
		int smsCount = 1;
		int smsLenth = content.length() + shopName.length() + 3;
		if(smsLenth > 70){
			smsCount = smsLenth / 67 + 1;
		}
		
		// 有足够的短信余额发送这批短信
		int successNum = 0;
		if(remainCount >= size * smsCount) {
			for (Long key : map.keySet()) {
				Long oid = key;
				String phone = map.get(key);
				SmsSendRet ret = sendRateSms(user, oid, phone, content, shopName);
				rets.add(ret);
				if(ret.isSuccess()) {
					successNum ++;
				}
			}
			SmsSendCount.addUsedCount(user.getId(), successNum * smsCount);
		} 
		// 余额只够发送一部分短信
		else {
			for (Long key : map.keySet()) {
				if((successNum + 1) * smsCount > remainCount) {
					rets.add(SmsSendRet.NO_REMAIN_COUNT);
				} else {
					Long oid = key;
					String phone = map.get(key);
					SmsSendRet ret = sendRateSms(user, oid, phone, content, shopName);
					rets.add(ret);
					if(ret.isSuccess()) {
						successNum ++;
					}
				}
			}
			SmsSendCount.addUsedCount(user.getId(), successNum * smsCount);
		}
		
		
		return rets;
	}
	
	public static List<SmsSendRet> genFullFailSmsSendRetList(int size, SmsSendRet ret) {
		List<SmsSendRet> rets = new ArrayList<SmsAction.SmsSendRet>();
		for(int i = 0; i < size; i++) {
			rets.add(ret);
		}
		return rets;
	}
	
	/**
	 * 
	 * @param user 当前登陆的卖家
	 * @param phone 要发送短信的号码
	 * @param content 短信内容
	 * @return
	 */
	public static SmsSendRet sendSms(User user, String phone, String content) {
		// 判断电话号码格式
		String phoneNo = formatMobileNo(phone);
		if(StringUtils.isEmpty(phoneNo)) {
			return SmsSendRet.PHONE_ILLEGAL;
		}
		
		if(sendSmsImmediate(user.getId(), user.getUserNick(), phone, content, SmsSendLog.TYPE.SMS_SEND_BY_WAIBAO, -1L)) {
			return SmsSendRet.SUCCESS;
		} 
		return SmsSendRet.SMS_SEND_FAIL;

	}
	
	public static SmsSendRet sendRateSms(User user, Long oid, String phone, String content, String shopName) {
		// 判断电话号码格式
		String phoneNo = formatMobileNo(phone);
		if(StringUtils.isEmpty(phoneNo)) {
			return SmsSendRet.PHONE_ILLEGAL;
		}
		
		// 插入一条评价短信发送记录（用以获取短信发送时的扩展码）
		RateSmsSendLog rateSmsSendLog = new RateSmsSendLog(oid, phone, content, true, StringUtils.EMPTY, user.getId());
		boolean isSuccess = rateSmsSendLog.jdbcSave();
		if(!isSuccess) {
			return SmsSendRet.SMS_SEND_FAIL;
		}
		
//		SmsResult result = SmsSendLaiqt.sendLaiqtSms(phone, content, shopName);
		SmsResult result = SmsSendLiangY.jiaQunSmsSend(phone.trim(), content, shopName);
		
		if(result != null && result.getIsSubmited() && result.getFailCount() == 0) {
			rateSmsSendLog.setBatchId(result.getBatchId());
			rateSmsSendLog.jdbcSave();
			return SmsSendRet.SUCCESS;
		}
		
		rateSmsSendLog.setSuccess(false);
		rateSmsSendLog.jdbcSave();
		return SmsSendRet.SMS_SEND_FAIL;

	}
	
	private static boolean sendSmsImmediate(Long userId, String userNick, String phone, String content, int type, long tid) {
        
		// 执行发送短信操作
        //SendInfo sendInfo = QLTProvider.getInstance().sendNormalMsg(userId, 0L, phone, content);
        //使用emay
	    SendInfo sendInfo = EMaySmsSendAction.send(phone, content);
	    
	    log.info("send by waibaoshang with phone = " + phone + " and tid = " + tid + " and status = " + sendInfo.getStatus());
        // 发送成功
        if (sendInfo.getStatus() < SmsStatus.SMS_SUCCESS) {
        	// 这是保存原来的短信发送记录，卖家是可以看到的
            new SmsSendLog(userId, userNick, phone, content, type, tid, true).jdbcSave();
            return true;
        } 
        // 发送失败
        else {
        	// 这是保存原来的短信发送记录，卖家是可以看到的
            new SmsSendLog(userId, userNick, phone, content, type, tid, false).jdbcSave();
            return false;
        }
    }
	
	public static final String regExp = "^[1]([0-9]{1}|59|58|88|89)[0-9]{8}$";
	public static String formatMobileNo(String phone) {

		if(StringUtils.isEmpty(phone)) {
			return StringUtils.EMPTY;
		}
		phone = phone.trim();
		Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");  
		Matcher m = p.matcher(phone);  
		if(m.matches()) {
			return phone;
		}  
		return StringUtils.EMPTY;
	}



}
