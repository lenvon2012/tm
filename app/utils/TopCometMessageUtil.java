package utils;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class TopCometMessageUtil {
	public static String getUserId(String message) {
		return message.substring(message.indexOf("user_id")+9, message.indexOf("nick")-2);
	}
	
	public static String getUserNick(String message) {
		return message.substring(message.indexOf("nick")+7, message.indexOf("modified")-3);
	}
	
	public static String processJson(String jsonStr,String key)throws Exception{
		  //将要执行的javascript语句
		  String javascript = "function getValue(jsonStr,key){var text=eval('('+jsonStr+')');return text[key];}";
		  //构造脚本引擎工厂
		  ScriptEngineManager factory = new ScriptEngineManager();
		  //获取javascript的脚本引擎
		  ScriptEngine scriptEngine = factory.getEngineByName("javascript");
		  //执行js方法
		  scriptEngine.eval(javascript);
		  //创建执行上下文
		  Invocable invocable = (Invocable)scriptEngine;
		  //调用javascript的getValue方法
		  String value = String.valueOf(invocable.invokeFunction("getValue", new Object[]{jsonStr,key}));
		  return value;
	}
	
	public static String getOid(String message) {
		return message.substring(message.indexOf("oid")+5, message.indexOf("is_3D")-2);
	} 
	
	public static String getTid(String message) {
		return message.substring(message.indexOf("tid")+5, message.indexOf("type")-2);
	} 

	

}
