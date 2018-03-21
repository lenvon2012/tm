package security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actions.ronghe.SMSSendRongHe;

import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.SecretException;
import com.taobao.api.security.SecurityClient;

public class SecurityTest {
	
	private static final Logger log = LoggerFactory.getLogger(SecurityTest.class);
	
	private static final String serverUrl = "https://eco.taobao.com/router/rest";
	
	private static final String appkey = "21404171";
	
	private static final String appSecret = "724576dc06e80ed8e38d1ad2f6de39da";
	
	private static final String sessionkey = "6200022e7adf2a650a982516532e66ZZc6ff839f208915d79742176";
	
	private static final String RandomNumber = "hJCk63umEmr9ZippDJ+P0/RD7Q3QqNSV6jqneyBaVAA=";
	
	public static void testPrjvateSecret() throws ApiException, SecretException {
		
		SecurityClient securityClient = new SecurityClient(new DefaultTaobaoClient(serverUrl, appkey, appSecret), RandomNumber);
		
		String type = "phone";
		String value = "18814887685";
		
		// 加密手机号码
		String encryptValue = securityClient.encrypt(value, type, sessionkey);
		System.out.println("手机号码明文：" + value + " ->密文：" + encryptValue);
		
		// 判断是否为加密手机号数据
		if(securityClient.isEncryptData(encryptValue, type)) {
			String originValue = securityClient.decrypt(encryptValue, type, sessionkey);
			System.out.println("手机号码密文：" + encryptValue + " ->明文：" + originValue);
		}
		
		String[] typeArr = { "normal", "nick", "receiver_name","simple","search" };
		value = "taobaoTest";
		for(String typeValue : typeArr) {
			System.out.println("===========================================================");
			// 加密nick
			encryptValue = securityClient.encrypt(value, typeValue, sessionkey);
			System.out.println("nick明文：" + value + " ->密文：" + encryptValue);
			
			// 判断是否为加密手机号数据
			if(securityClient.isEncryptData(encryptValue, typeValue)) {
				String originValue = securityClient.decrypt(encryptValue, typeValue, sessionkey);
				System.out.println("nick密文：" + encryptValue + " ->明文：" + originValue);
			}
		}
		
	}
	
	public static void main(String[] args) {
		try {
			testPrjvateSecret();
		} catch (ApiException e) {
			e.printStackTrace();
		} catch (SecretException e) {
			e.printStackTrace();
		}
	}
	
}
