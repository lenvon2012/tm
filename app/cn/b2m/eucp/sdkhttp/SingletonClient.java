package cn.b2m.eucp.sdkhttp;


import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import cn.b2m.eucp.sdkhttp.EMClient;




public class SingletonClient {
	private static EMClient client=null;
	private SingletonClient(){
	}
	public synchronized static EMClient getClient(String softwareSerialNo,String key){
		if(client==null){
			try {
				client=new EMClient(softwareSerialNo,key);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return client;
	}
	public synchronized static EMClient getClient(){
		ResourceBundle bundle=PropertyResourceBundle.getBundle("config");
		if(client==null){
			try {
				client=new EMClient(bundle.getString("softwareSerialNo"),bundle.getString("key"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return client;
	}
	
	
}
