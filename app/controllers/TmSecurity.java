package controllers;

import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.SecretException;
import com.taobao.api.security.SecurityClient;

/**
 * 好评助手数据加、解密
 * 
 * @author ww
 * @since 2017年02月09日 下午20:15:17
 */
public class TmSecurity {
	
	private static final Logger log = LoggerFactory.getLogger(TmSecurity.class);
	
	private static final String serverUrl = "https://eco.taobao.com/router/rest";
	
	private static final String appkey = "21404171";
	
	private static final String appSecret = "724576dc06e80ed8e38d1ad2f6de39da";
	// 令牌
	private static final String RandomNumber = "hJCk63umEmr9ZippDJ+P0/RD7Q3QqNSV6jqneyBaVAA=";
	
	private static final SecurityClient securityClient = new SecurityClient(new DefaultTaobaoClient(serverUrl, appkey, appSecret), RandomNumber);
	
	public static class SecurityType {
		public static String PHONE = "phone";
		public static String SIMPLE = "simple";
		public static String SEARCH = "search";
	}
	
	// 判断是否是加密数据
	public static boolean isEncryptData(String data, String type) throws SecretException {
		
		return securityClient.isEncryptData(data, type);
		
	}
	
	// 加密
	public static String encrypt(String value, String type, User user) throws SecretException {
		
		if(securityClient.isEncryptData(value, type)) {
			return value;
		}
		
		return securityClient.encrypt(value, type, user.getSessionKey());
		
	}
	
	// 解密
	public static String decrypt(String encryptValue, String type, User user) throws SecretException {
		
		String Value = encryptValue;
		if(securityClient.isEncryptData(encryptValue, type)) {
			Value = securityClient.decrypt(encryptValue, type, user.getSessionKey());
		}
		
		// 确认是否已经解密成功(有些phone类型解密不了的用simple类型再解密一次)
		if(securityClient.isEncryptData(Value, SecurityType.SIMPLE)) {
			Value = securityClient.decrypt(Value, SecurityType.SIMPLE, user.getSessionKey());
		}
		
		return Value;
		
	}
	
}
