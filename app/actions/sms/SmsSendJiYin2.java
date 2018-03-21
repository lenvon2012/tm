package actions.sms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import smsprovider.SendInfo;
import smsprovider.SmsStatus;
import utils.PlayUtil;

import com.ciaosir.client.CommonUtils;

/**
 * 吉因科技  发送短信类
 */
public class SmsSendJiYin2 {
	
	private static final Logger log = LoggerFactory.getLogger(SmsSendJiYin2.class);
	
	/**
	 * 企业ID
	 */
	private static final String INDUSTRY_ENTERPRISE_ID = "102";
	
	/**
	 * 用户帐号
	 */
	private static final String INDUSTRY_USER_NAME = "bgxbyx";
	
	/**
	 * 帐号密码
	 */
	private static final String INDUSTRY_PASSWORD = "Kky41D13";
	
	public static SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyy-MM-dd hh:mm:ss");
	
	public enum JIYINAction {
		send,    // 发送短信
		overage, // 查询短信余额
		query    // 获取状态报告
	}
	
	/** 
	 * 自定义签名行业短信
	 * */
	private static final String INDUSTRY_URL_ANAY_SIGN = "http://101.201.37.87:8868//smsGBK.aspx";
	
	/** 
	 * 自定义签名行业短信
	 * 状态报告接口
	 * */
	private static final String INDUSTRY_REPORT_ANAY_SIGN = "http://101.201.37.87:8868//statusApi.aspx";
	
	private static final String ENCODE = "GB2312";

	/**
	 * 行业/通知	短信发送接口  
	 * @param phoneNumber  手机号码
	 * @param smsContent  短信内容
	 * @param kzm  扩展子号
	 * @param sendTime  发送时间，为空表示立即发送；格式为yyyy-MM-dd hh:mm:ss
	 */
	public static SendInfo sendIndustrySms(String phoneNumber, String smsContent, String userShopName, String kzm, String sendTime) {
		if(StringUtils.isEmpty(phoneNumber) || StringUtils.isEmpty(smsContent)){
			return new SendInfo(0L, System.currentTimeMillis(), phoneNumber, smsContent, SmsStatus.EMayErr);
		}
		if(userShopName.length() > 10){
			userShopName = userShopName.substring(0, 10);
		}
		
		userShopName = userShopName.replaceAll("【", "(");
		userShopName = userShopName.replaceAll("】", ")");
		smsContent = smsContent.replaceAll("【", "(");
		smsContent = smsContent.replaceAll("】", ")");
		
		smsContent = "【" + userShopName + "】" + smsContent;
		// 该通道不需要加退订回T
		if(smsContent.endsWith("退订回T")){
			smsContent = smsContent.substring(0, smsContent.length() - 4);
//			smsContent = smsContent + " 退订回T";
		}
		//对短信内容做Urlencode编码操作。
		String contentUrlEncode = StringUtils.EMPTY;
		try {
			contentUrlEncode = URLEncoder.encode(smsContent, ENCODE);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		// 参数拼接
		StringBuffer param = new StringBuffer();
		param.append("&userid=").append(INDUSTRY_ENTERPRISE_ID);
		param.append("&account=").append(INDUSTRY_USER_NAME);
		param.append("&password=").append(INDUSTRY_PASSWORD);
		param.append("&mobile=").append(phoneNumber);
		param.append("&content=").append(contentUrlEncode);
		param.append("&action=").append(JIYINAction.send);
		param.append("&sendTime=").append(formatSendTime(sendTime));
		if(!StringUtils.isEmpty(kzm)) {
			param.append("&extno=").append(kzm);
		}

		// 发送短信
		String resultStr = sendPost(INDUSTRY_URL_ANAY_SIGN, param.toString());
		if(!StringUtils.isEmpty(resultStr)) {
			resultStr = resultStr.trim();
		}
		JiyinResult result = parseResult(resultStr);
		if("Success".equals(result.getReturnstatus()) || "ok".equals(result.getMessage())) {	
			return new SendInfo(Long.valueOf(result.getTaskID()), System.currentTimeMillis(), phoneNumber, smsContent, SmsStatus.SUCCESS);
		}
		return new SendInfo(0L, System.currentTimeMillis(), phoneNumber, smsContent, SmsStatus.EMayErr);
	}
	
	public static JiyinResult parseResult(String resultStr){
		JiyinResult result = new JiyinResult();
		if(StringUtils.isEmpty(resultStr)) {
			return result;
		}
		int index = resultStr.indexOf("<returnstatus>");
		if(index > 0) {
			result.setReturnstatus(resultStr.substring(index + "<returnstatus>".length(), resultStr.indexOf("</returnstatus>", index)));
		}
		index = resultStr.indexOf("<message>");
		if(index > 0) {
			result.setMessage(resultStr.substring(index + "<message>".length(), resultStr.indexOf("</message>", index)));
		}
		index = resultStr.indexOf("<remainpoint>");
		if(index > 0) {
			result.setRemainpoint(resultStr.substring(index + "<remainpoint>".length(), resultStr.indexOf("</remainpoint>", index)));
		}
		index = resultStr.indexOf("<taskID>");
		if(index > 0) {
			result.setTaskID(resultStr.substring(index + "<taskID>".length(), resultStr.indexOf("</taskID>", index)));
		}
		index = resultStr.indexOf("<successCounts>");
		if(index > 0) {
			result.setSuccessCounts(resultStr.substring(index + "<successCounts>".length(), resultStr.indexOf("</successCounts>", index)));
		}
		return result;
	}
	
	public static String formatSendTime(String sendTime){
		sendTime = PlayUtil.trimValue(sendTime);
		String realSendTime = StringUtils.EMPTY;
		if(StringUtils.isEmpty(sendTime)) {
			return realSendTime;
		}
		Long sendTs = 0L;
		try {
			sendTs = sdf.parse(sendTime).getTime();
		} catch (Exception e) {
			// TODO: handle exception
		}
		if(sendTs <= 0L) {
			return realSendTime;
		}
		return sendTime;
	}
	
	/**
	 * <?xml version="1.0" encoding="gb2312" ?><returnsms>
 	 * <returnstatus>Sucess</returnstatus>
 	 * <message></message>
 	 * <payinfo>预付费</payinfo>  (或 后付费)
 	 * <overage>6</overage>
 	 * <sendTotal>70</sendTotal></returnsms>
	 * 行业/通知	短信余额查询
	 */
	public static String industrySmsBalances(){
		// 参数拼接
		StringBuffer param = new StringBuffer();
		param.append("&userid=").append(INDUSTRY_ENTERPRISE_ID);
		param.append("&account=").append(INDUSTRY_USER_NAME);
		param.append("&password=").append(INDUSTRY_PASSWORD);
		param.append("&action=").append(JIYINAction.overage);
		String result = sendPost(INDUSTRY_URL_ANAY_SIGN, param.toString());
		
		if(!StringUtils.isEmpty(result)) {
			result = result.trim();
		}
		int index = result.indexOf("<overage>");
		if(index > 0) {
			return result.substring(index + "<overage>".length(), result.indexOf("</overage>", index));
		} else {
			return "0";
		}
	}	
	
	public static class JiyinStatusBox {
		String mobile;
		Long taskId;
		int status;
		String receivetime;
		String errorcode;
		public String getMobile() {
			return mobile;
		}
		public void setMobile(String mobile) {
			this.mobile = mobile;
		}
		public Long getTaskId() {
			return taskId;
		}
		public void setTaskId(Long taskId) {
			this.taskId = taskId;
		}
		public int getStatus() {
			return status;
		}
		public void setStatus(int status) {
			this.status = status;
		}
		public String getReceivetime() {
			return receivetime;
		}
		public void setReceivetime(String receivetime) {
			this.receivetime = receivetime;
		}
		public String getErrorcode() {
			return errorcode;
		}
		public void setErrorcode(String errorcode) {
			this.errorcode = errorcode;
		}
		public JiyinStatusBox(String mobile, Long taskId, int status,
				String receivetime, String errorcode) {
			super();
			this.mobile = mobile;
			this.taskId = taskId;
			this.status = status;
			this.receivetime = receivetime;
			this.errorcode = errorcode;
		}
	}
	
	/**
	 * <?xml version="1.0" encoding="utf-8" ?> 
	 * <returnsms>
 	 * <statusbox>
	 * <userid>77</userid >-------------企业代码
	 * <mobile>15023239810</mobile>-------------对应的手机号码
	 * <taskid>1212</taskid>-------------同一批任务ID
	 * <status>10</status>---------状态报告----10：发送成功，20：发送失败
	 * <receivetime>2011-12-02 22:12:11</receivetime>-------------接收时间
	 * <errorcode>MK:0011</errorcode>-------------状态返回值
	 * </statusbox>
	 * <statusbox>
	 * <userid>77</userid >-------------企业代码
	 * <mobile>15023239811</mobile>
	 * <taskid>1212</taskid>
	 * <status>20</status>
	 * <receivetime>2011-12-02 22:12:11</receivetime>
	 * <errorcode>DELIVRD</errorcode>-------------状态返回值
	 * </statusbox>
	 * </returnsms>
	 * 获取状态报告
	 */
	public static List<JiyinStatusBox> getSmsStatus(int num){
		if(num <= 0) {
			num = 10;
		}
		List<JiyinStatusBox> boxs = new ArrayList<SmsSendJiYin2.JiyinStatusBox>();
		// 参数拼接
		StringBuffer param = new StringBuffer();
		param.append("&userid=").append(INDUSTRY_ENTERPRISE_ID);
		param.append("&account=").append(INDUSTRY_USER_NAME);
		param.append("&password=").append(INDUSTRY_PASSWORD);
		param.append("&action=").append(JIYINAction.query);
		param.append("&statusNum=").append(num);
		String result = sendPost(INDUSTRY_REPORT_ANAY_SIGN, param.toString());
		
		if(StringUtils.isEmpty(result)) {
			return boxs;
		}
		result = result.trim();
		return parseReportResult(result);
	}	
	
	public static String s = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>" + 
			 "<returnsms>" + 
		 	 "<statusbox>" + 
			 "<userid>77</userid >" + 
			 "<mobile>15023239810</mobile>" + 
			 "<taskid>1212</taskid>" + 
			 "<status>10</status>" + 
			 "<receivetime>2011-12-02 22:12:11</receivetime>" + 
			 "<errorcode>MK:0011</errorcode>" + 
			 "</statusbox>" + 
			 "<statusbox>" + 
			 "<userid>77</userid >" + 
			 "<mobile>15023239811</mobile>" + 
			 "<taskid>1212</taskid>" + 
			 "<status>20</status>" + 
			 "<receivetime>2011-12-02 22:12:11</receivetime>" + 
			 "<errorcode>DELIVRD</errorcode>" + 
			 "</statusbox>" + 
			 "</returnsms>";
	public static List<JiyinStatusBox> parseReportResult(String result) {
		List<JiyinStatusBox> boxs = new ArrayList<JiyinStatusBox>();
		if(StringUtils.isEmpty(result)) {
			return boxs;
		}
		if(result.indexOf("statusbox") <= 0) {
			return boxs;
		}
		Document document = null;
		try {
			document = DocumentHelper.parseText(result);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(document == null) {
			return boxs;
		}
		Element root = document.getRootElement();
		List<Element> elements = root.elements();
		if(CommonUtils.isEmpty(elements)) {
			return boxs;
		}
		for(Element element : elements) {
			try {
				String mobile = element.elementText("mobile");
				Long taskId = Long.valueOf(element.elementText("taskid"));
				int status = Integer.valueOf(element.elementText("status"));
				String receivetime = element.elementText("receivetime");
				String errorcode = element.elementText("errorcode");
				boxs.add(new JiyinStatusBox(mobile, taskId, status, receivetime, errorcode));
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		return boxs;
	}
	
	/**
	 * <?xml version="1.0" encoding="gb2312" ?><returnsms>
 	 * <returnstatus>Success</returnstatus>
 	 * <message>ok</message>
 	 * <remainpoint>6</remainpoint>
 	 * <taskID>130850</taskID>
 	 * <successCounts>1</successCounts></returnsms>
	 * @author lzl
	 *
	 */
	public static class JiyinResult{
		String returnstatus;
		String message;
		String remainpoint;
		String taskID;
		String successCounts;
		public String getReturnstatus() {
			return returnstatus;
		}
		public void setReturnstatus(String returnstatus) {
			this.returnstatus = returnstatus;
		}
		public String getMessage() {
			return message;
		}
		public void setMessage(String message) {
			this.message = message;
		}
		public String getRemainpoint() {
			return remainpoint;
		}
		public void setRemainpoint(String remainpoint) {
			this.remainpoint = remainpoint;
		}
		public String getTaskID() {
			return taskID;
		}
		public void setTaskID(String taskID) {
			this.taskID = taskID;
		}
		public String getSuccessCounts() {
			return successCounts;
		}
		public void setSuccessCounts(String successCounts) {
			this.successCounts = successCounts;
		}
		public JiyinResult(){
			super();
		}
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
		String result = StringUtils.EMPTY;
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
			in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "GBK"));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
				result += "\n";
			}
			return result;
		} catch (MalformedURLException e) {
			log.error(e.toString());;
		} catch (IOException e) {
			log.error(e.toString());
		} finally {
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(in);
		}
		return result;
	}
	
	public static void main(String[] args) {
		sendIndustrySms("18814887685", "tb7736512_2012: 淘宝买家凝夏千颜给您差评，请及时登录淘宝查看，购买宝贝复古loft工业风3dmax模型单体家装材质材素现代简约写实3d模型库。","家具设计", "", "");
//		getSmsStatus(1);
//		industrySmsBalances();
	}
}
