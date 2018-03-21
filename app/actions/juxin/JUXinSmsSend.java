package actions.juxin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 聚信科技  发送短信类
 */
public class JUXinSmsSend {
	
	private static final Logger log = Logger.getLogger(JUXinSmsSend.class);
	
	private static final String USER_NAME = "bgxb";
	
	private static final String PWD = "dENsEduE";
	
	private static final String USER_NAME_CMCC = "bgxb1";
	
	private static final String PWD_CMCC = "W69PUjx6";
	
	static final String URL = "http://api.app2e.com/smsBigSend.api.php";
	
	static final String CHARSET = "utf";
	
	/** 一条短信的字数 */
    public static final int SINGLE_SMS_COUNT = 70;
    
    /** 短信签名所占的字数 */
    public static final int SMS_SIGN_COUNT = 8;
	
	// 移动用户号段
	private static List<Integer> cmccSegment = Arrays.asList(134, 135, 136, 137, 138, 139, 150, 151, 157, 158, 159, 182, 183, 184, 187, 188, 147, 152, 178, 1705);
	
	// 联通  电信号段
//	private List<Integer> unicomAndTelecomSegment = Arrays.asList(130, 131, 132, 155, 156, 185, 186, 144, 145, 176, 1709, 1707, 1708, 133, 153, 177, 180, 181, 189, 1700);
	
	/**
	 * 发送短信
	 * 
	 * @param mobile 发送的手机号码
	 * @param message	短信的内容
	 * @param signatures 【淘宝】 或者 【京东】
	 * @return	{"status":100, "count":5, "list":[{"p":"15111111111","mid":46b991364d938571}, {"p":"15222222222","mid":46b991364d938572},]}
	 */
	public static ResultInfo sendSms(String mobile, String message, String shopName){
		// 参数拼接
		StringBuffer param = new StringBuffer();
	    if(StringUtils.isEmpty(shopName)){
            log.warn("------------ shopName is null ------------");
            return new ResultInfo(101, 0);
        }
	    message = "【" + shopName + "】" + message;
		if (!message.endsWith("退订回T")) {
			message = message.concat("退订回T");
		}
		// 判断联通  || 移动  || 电信
		if(isCmccSegment(mobile)){
			param.append("username=").append(USER_NAME_CMCC);
			param.append("&pwd=").append(string2MD5(PWD_CMCC));
			log.info("cmcc message :" + message);
		} else {
			param.append("username=").append(USER_NAME);
			param.append("&pwd=").append(string2MD5(PWD));
		}
		param.append("&p=").append(mobile);
		param.append("&isUrlEncode=").append("no");
		param.append("&msg=").append(message);
		param.append("&charSetStr=").append(CHARSET);
		// 发送短信
		String sendPost = sendPost(URL,param.toString());
		log.info(sendPost);
		return parseJson(sendPost);
	}
	
	static ResultInfo parseJson(String string){
		try {
			JSONObject jsonObject = new JSONObject(string);
			return new ResultInfo(jsonObject.getInt("status"), jsonObject.getInt("count"));
		} catch (JSONException e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	/**
	 * 向指定 URL 发送POST方法的请求
	 * 
	 * @param url 发送请求的 URL
	 * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
	 * @return 所代表远程资源的响应结果
	 */
	public static String sendPost(String url, String param) {
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		try {
			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();
			// 设置通用的请求属性
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			// 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setDoInput(true);
			// 获取URLConnection对象对应的输出流
			out = new PrintWriter(conn.getOutputStream());
			// 发送请求参数
			out.print(param);
			// flush输出流的缓冲
			out.flush();
			// 定义BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
				result += "\n";
			}
			return result;
		} catch (MalformedURLException e) {
			log.error(e);
		} catch (IOException e) {
			log.error(e);
		} finally {
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(in);
		}
		return result;
	}
	
	/** 
	 * 判断号码是哪家运营商的
	 * @param mobile	手机号码
	 * @return	移动 true  电信联通  false
	 */
	private static boolean isCmccSegment(String mobile){
		int num = Integer.parseInt(mobile.substring(0,3));
		int forth = Integer.parseInt(mobile.substring(0,4));
		return forth == 1705 ? true : cmccSegment.contains(num);
	}
	
	
	static String string2MD5(String inStr){  
        MessageDigest md5 = null;  
        try{  
            md5 = MessageDigest.getInstance("MD5");  
        }catch (Exception e){  
        	log.error("MD5 error:", e);
            return "";  
        }  
        char[] charArray = inStr.toCharArray();  
        byte[] byteArray = new byte[charArray.length];  
  
        for (int i = 0; i < charArray.length; i++){
        	byteArray[i] = (byte) charArray[i];  
        }
        byte[] md5Bytes = md5.digest(byteArray);  
        StringBuffer hexValue = new StringBuffer();  
        for (int i = 0; i < md5Bytes.length; i++){  
            int val = ((int) md5Bytes[i]) & 0xff;  
            if (val < 16){
            	hexValue.append("0");  
            }
            hexValue.append(Integer.toHexString(val));  
        }  
        return hexValue.toString();  
    }  
	
	/**
     * 向指定URL发送GET方法的请求
     * 
     * @param url 发送请求的URL
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendGet(String url, String param) {
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url + "?" + param;
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : map.keySet()) {
                log.info(key + "--->" + map.get(key));
            }
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (MalformedURLException e) {
			log.error(e);
		} catch (IOException e) {
			log.error(e);
        } finally {
        	// 使用finally块来关闭输入流
        	IOUtils.closeQuietly(in);
        }
        return result;
    }
    
    public static class ResultInfo {
    	
    	/**
    	 * 提交状态，即是否全部接收手机号码以及内容提交成功。100：全部成功
    	 */
    	private int status;
    	
    	/**
    	 * 提交成功的接收号码个数。
    	 */
    	private int count;
    	
    	public ResultInfo(int status, int count){
    		this.status = status;
    		this.count = count;
    	}
    	
		public int getCount() {
			return count;
		}

		public boolean isSuccess() {
			return this.status == 100;
		}

    } 
    
    public static enum Signatures {
    	
    	TAO_BAO {
    		public String getValue(){
    			return "【淘宝】";
    		}
    	}, 
    	JING_DONG {
    		public String getValue(){
    			return "【京东】";
    		}
    	};
    	
    	public abstract String getValue();
    	
    }
    
}
