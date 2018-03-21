package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * Created by Echo on 2017-03-08.
 */
public class CommonUtil {

	public static final List<String> States = Arrays.asList("西藏", "湖南", "内蒙古",
			"浙江", "河南", "江苏", "香港", "河北", "澳门", "四川", "黑龙江", "湖北", "云南", "台湾",
			"青海", "宁夏", "新疆", "广西", "海南", "山西", "福建", "陕西", "安徽", "贵州", "山东",
			"甘肃", "江西", "辽宁", "广东", "吉林", "上海", "北京", "重庆", "天津");

	/**
	 * 分两步，先反序列化出ArrayList<JsonObject>，然后在一个个的把JsonObject转成classOfT类型的对象。
	 */
	public static <T> ArrayList<T> jsonToList(String json, Class<T> classOfT) {
		Type type = new TypeToken<ArrayList<JsonObject>>() {
		}.getType();
		ArrayList<JsonObject> jsonObjs = new Gson().fromJson(json, type);

		ArrayList<T> listOfT = new ArrayList<T>();
		for (JsonObject jsonObj : jsonObjs) {
			listOfT.add(new Gson().fromJson(jsonObj, classOfT));
		}

		return listOfT;
	}

	/**
	 * 判断集合是否为null或者长度为0
	 * 
	 * @param c
	 * @return 有元素返回false,否则返回true
	 */
	public static Boolean isNullOrSizeZero(Collection c) {
		return c == null || c.isEmpty();
	}

	/**
	 * 判断字符串是否为null或者""
	 */
	public static Boolean isNullOrEmpty(Object str) {
		return str == null || str.toString().length() == 0;
	}

	/**
	 * 判断字符串是否为数字
	 * 
	 * @param str
	 * @return
	 */
	public static Boolean isNumber(String str) {
		try {
			@SuppressWarnings("unused")
			Double num = Double.valueOf(str);// 把字符串强制转换为数字
			return true;// 如果是数字，返回True
		} catch (Exception e) {
			return false;// 如果抛出异常，返回False
		}
	}
	
	/**
	 * 判断字符串是否为手机号码
	 * 
	 * @param str
	 * @return
	 */
	public static Boolean isMobile(String str) {
		/** 验证手机号的正则表达式 */
		Pattern MOBILE_REGEX = Pattern.compile("^1[0-9][0-9][\\d]{8}$");
		try {
			return MOBILE_REGEX.matcher(str).matches();
		} catch (Exception e) {
			return false;// 如果抛出异常，返回False
		}
	}

	/**
	 * 判断字符串是否包含中文字符
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isContainChinese(String str) {
		Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
		Matcher m = p.matcher(str);
		if (m.find()) {
			return true;
		}
		return false;
	}

	/**
	 * 去除字符串中包含的中文字符
	 * 
	 * @param str
	 * @return
	 */
	public static String delChineseWord(String str) {
		if (!isContainChinese(str)) {
			return str;
		}
		return str.replaceAll("[\u4e00-\u9fa5]", "").replace("(", "")
				.replace(")", "");

	}

	/**
	 * 根据inputPids生成对应的inputStr
	 * 
	 * @param inputPids
	 * @return
	 */
	public static String genratorInputStr(String inputPids) {
		if (CommonUtil.isNullOrEmpty(inputPids)) {
			return null;
		}

		String[] pidArray = inputPids.split(",");

		int size = pidArray.length;

		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < size; i++) {
			String init = "11";
			if (pidArray[i].equalsIgnoreCase("149422948")) {
				//材质成分
				init = "其他100%";
			}else if (pidArray[i].equalsIgnoreCase("20000")) {
				//品牌
				init = "琪她";
			}
			if (i == size - 1) {
				sb.append(init);
				break;
			}
			sb.append(init + ",");
		}

		return sb.toString();
	}

	/**
	 * 获取数据整合后的list对象
	 */
	public static Collection<String> mergeList(Collection<String>... lists) {
		Collection<String> newList = new ArrayList<String>();
		int size = lists.length;
		if (size == 0)
			return null;
		if (size > 0) {
			for (int i = 0; i < size; i++) {
				newList = mergeTwoList(newList, lists[i]);
			}
		}
		return newList;

	}

	/**
	 * 获取数据整合后的list对象
	 */
	public static Collection<String> mergeList(List<Collection<String>> lists) {
		Collection<String> newList = new ArrayList<String>();
		int size = lists.size();
		if (size == 0)
			return null;
		if (size > 0) {
			for (int i = 0; i < size; i++) {
				newList = mergeTwoList(newList, lists.get(i));
			}
		}
		return newList;

	}

	/**
	 * 获取数据整合后的list对象
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	private static Collection<String> mergeTwoList(Collection<String> a,
			Collection<String> b) {
		List<String> newList = new ArrayList<String>();
		if (isNullOrSizeZero(a))
			return b;
		if (isNullOrSizeZero(b))
			return a;
		for (String s : a) {
			for (String s1 : b) {
				newList.add(s + ";" + s1);
			}
		}
		return newList;
	}

	public static String decodeUnicode(final String dataStr) {
		int start = 0;
		int end = 0;
		final StringBuffer buffer = new StringBuffer();
		while (start > -1) {
			end = dataStr.indexOf("\\u", start + 2);
			String charStr = "";
			if (end == -1) {
				charStr = dataStr.substring(start + 2, dataStr.length());
			} else {
				charStr = dataStr.substring(start + 2, end);
			}
			char letter = (char) Integer.parseInt(charStr, 16); // 16进制parse整形字符串。
			buffer.append(new Character(letter).toString());
			start = end;
		}
		return buffer.toString();
	}

	public static void main(String[] args) {
		Set<String> a = new HashSet<String>();
		a.add("a");
		a.add("b");
		Set<String> b = new HashSet<String>();
		b.add("1");
		b.add("2");

		List<String> c = (List<String>) CommonUtil.mergeList(a, b);

		for (String string : c) {
			System.out.println(string);
		}

	}
	
	 /**
     * 获取Url中对应的数据
     * @param urlPath
     * @param charset 如果为null,则默认采用gbk的编码
     * @return
     * @throws Exception
     */
    public static String sendGet(String urlPath,String charset) throws Exception {
        if (charset==null){
            charset="gbk";
        }
        URL url = new URL(urlPath);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        InputStream inputStream = connection.getInputStream();
        //对应的字符编码转换
        Reader reader = new InputStreamReader(inputStream, charset);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String str = null;
        StringBuffer sb = new StringBuffer();
        while ((str = bufferedReader.readLine()) != null) {
            sb.append(str);
        }
        reader.close();
        connection.disconnect();
        return sb.toString();
    }
    /**
     * 向指定 URL 发送POST方法的请求
     * 
     * @param url
     *            发送请求的 URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
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
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
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
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！"+e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result;
    }


	

}