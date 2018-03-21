package smsprovider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class SZYaoJiaProvider {
	
	public final static Logger log = LoggerFactory.getLogger(SZYaoJiaProvider.class);
	
	public static final String username = "bigexibo";
	public static final String password = "bigexibo!@#";
	public static SendInfo SZYaojiaSmsSend(Long userId, Long ts, String mobile, String content) {
		try {
			HttpClient client = new HttpClient();

			client.getParams().setParameter(
					HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");

			PostMethod post = new PostMethod(
					"http://www.szyaojia.com/sms/smsInterface.do");

			NameValuePair usernamepair = new NameValuePair("username", username);
			NameValuePair passwordpair = new NameValuePair("password", password);
			NameValuePair mobilepair = new NameValuePair("mobile",
					mobile);
			NameValuePair contentpair = new NameValuePair("content",
					content);

			post.setRequestBody(new NameValuePair[] { usernamepair, passwordpair,
					mobilepair, contentpair });
			client.executeMethod(post);
			String result = post.getResponseBodyAsString();
			post.releaseConnection();
			return new SendInfo(userId, ts, mobile, content, parseResult(result), 0);
		} catch (Exception e) {
			e.printStackTrace();
			return new SendInfo(userId, ts, mobile, content, SmsStatus.SZYaoJiaErr);
		}
	}
	
	public static int parseResult(String result) {
		if(result.indexOf("<resultcode>0</resultcode>") > 0) {
			return SmsStatus.SUCCESS;
		}
		return SmsStatus.SZYaoJiaErr;
	}
	
	public static String getSZYaoJiaBanlance() {
		try {
			HttpClient client = new HttpClient();

			client.getParams().setParameter(
					HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");

			PostMethod post = new PostMethod(
					"http://www.szyaojia.com/sms/smsBalance.do");

			NameValuePair usernamepair = new NameValuePair("username", username);
			NameValuePair passwordpair = new NameValuePair("password", password);

			post.setRequestBody(new NameValuePair[] { usernamepair, passwordpair });
			client.executeMethod(post);
			String result = post.getResponseBodyAsString();
			post.releaseConnection();
			return result;
		} catch (Exception e) {
			return e.getMessage();
		}
	} 
	
	public static String getSZYaoJiaReply() {
		try {
			HttpClient client = new HttpClient();

			client.getParams().setParameter(
					HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");

			PostMethod post = new PostMethod(
					"http://www.szyaojia.com/sms/smsReply.do");

			NameValuePair usernamepair = new NameValuePair("username", username);
			NameValuePair passwordpair = new NameValuePair("password", password);

			post.setRequestBody(new NameValuePair[] { usernamepair, passwordpair });
			client.executeMethod(post);
			String result = post.getResponseBodyAsString();
			post.releaseConnection();
			return result;
		} catch (Exception e) {
			return e.getMessage();
		}
	} 
}
