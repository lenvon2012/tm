package utils;

import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.SimpleHttpRetryUtil;

public class JsoupUtil {
	
    public static final String Default_UA = "Mozilla/4.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)";
    
    
	private static final Logger log = LoggerFactory.getLogger(JsoupUtil.class);
	
	//private static final int TimeOut = 30000;
	
	public static Document loadHtmlDoc(String url, String refer) {
		
		/*Document doc = null;
		for (int i = 0; i < RetryTime; i++) {
			long startTime = System.currentTimeMillis();
			try {
				Connection conn = Jsoup.connect(url);
				conn.userAgent(Default_UA);
	            conn.timeout(TimeOut);
	            conn.referrer(refer);
	            doc = conn.get();
				if (doc != null) {
	            	return doc;
	            }
			} catch (Exception ex) {
				if (ex instanceof org.jsoup.UnsupportedMimeTypeException) {
					
				} else {
					log.error(ex.getMessage(), ex);
					long endTime = System.currentTimeMillis();
					double second = ((double)(endTime - startTime)) / 1000.0;
					log.error("Jsoup获取页面失败：重试" + (i + 1) + ", 花费" + second + "秒" + ":" + url);
					try {
	                    Thread.sleep(500);
	                } catch (Exception e) {
	                    log.error(e.getMessage(), e);
	                }
				}
			}
		}
		if (doc == null) {
			log.error("can not load url: " + url); 
		}
		return doc;*/
	    
	    
	    String html = SimpleHttpRetryUtil.retryGetWebContent(url, refer);
	    if (StringUtils.isEmpty(html)) {
            return null;
        }
	    return parseJsoupDocument(html);
	}
	
	public static Document parseJsoupDocument(String html) {
	    if (StringUtils.isEmpty(html))
            return null;
        try {
            Document doc = Jsoup.parse(html);
            return doc;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            log.error("fail to change html to Jsoup Document!!!!");
        }
        return null;
	}
	
	/**
	 * 把相对路径转换成绝对路径
	 * @param baseUrl
	 * @param relativeUrl
	 * @return
	 */
	private static String dealWithRelativeUrl(String baseUrl, String relativeUrl) {
		try {
			URL baseURL = new URL(baseUrl);  
			URL absoluteURL = new URL(baseURL, relativeUrl); 
			String resultUrl = absoluteURL.toString().trim();
	
			return resultUrl;
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			
			return relativeUrl;
		}
	}
	
	
	public static boolean hasElementByCssQuery(Document doc, String cssQuery) {
		Elements eles = getElesByCssQuery(doc, cssQuery);
		if (CommonUtils.isEmpty(eles))
			return false;
		if (eles.size() > 1) {
			log.error("find more than 1 element by cssQuery: " + cssQuery + "!!!!!!!!!!!");
		}
		return true;
	}
	
	private static Element getUniqueEleByCssQuery(Document doc, String cssQuery) {
		Elements eles = getElesByCssQuery(doc, cssQuery);
		if (CommonUtils.isEmpty(eles)) {
			log.error("can not find element by cssQuery: " + cssQuery + "!!!!!!!!!!!");
			return null;
		}
		if (eles.size() > 1) {
			log.error("find more than 1 element by cssQuery: " + cssQuery + "!!!!!!!!!!!");
			//return null;
		}
		return eles.get(0);
	}
	
	//jsoup解析返回的如果有&nbsp，是替换成160，而不是32。。。
	private static String dealWithJsoupText(String text) {
		if (StringUtils.isEmpty(text))
			text = "";
		return text.replace((char)160, (char)32).trim();
	}
	
	public static String getTextByCssQuery(Document doc, String cssQuery) {
		Element ele = getUniqueEleByCssQuery(doc, cssQuery);
		if (ele == null)
			return "";
		String html = ele.text();
		if (StringUtils.isEmpty(html))
			return "";
		html = dealWithJsoupText(html);
		html = html.trim();
		return html;
	}
	
	/*public static String getValueByCssQuery(Document doc, String cssQuery) {
		Element ele = getFirstEleByCssQuery(doc, cssQuery);
		if (ele == null)
			return "";
		String value = ele.val();
		if (StringUtils.isEmpty(value))
			return "";
		return value.trim();
	}
	*/
	public static String getAttrByCssQuery(Document doc, String cssQuery, String attr) {
		Element ele = getUniqueEleByCssQuery(doc, cssQuery);
		if (ele == null)
			return "";
		String value = ele.attr(attr);
		if (StringUtils.isEmpty(value))
			return "";
		return value.trim();
	}
	
	private static Elements getElesByCssQuery(Document doc, String cssQuery) {
		Elements eles = doc.select(cssQuery);
		if (CommonUtils.isEmpty(eles)) {
			
		}
		return eles;
	}
}
