package paipai;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import play.test.UnitTest;

import com.ciaosir.client.api.API;
import com.ciaosir.client.utils.JsonUtil;

public class paipaiDownId extends UnitTest{

    public static void main(String[] args){  
    	
    	String name="new微博营销2";
    	String appId="233891";
    	int firstPage=6;
    	int lastPage=20;
    	
    	String url="http://fuwu.paipai.com/appstore/ui/my/app/appdetail/records.xhtml?appId="+appId+"&pageNumber=";
    	String fs="D:\\QQID\\"+name+".txt";

		try {
			   
			File f = new File(fs);
			   
			if(f.exists()){
			   System.out.println("文件存在");			
			}			
			else{
			   System.out.println("文件不存在");
			   f.createNewFile();//不存在则创建			   
			}

			BufferedWriter output = new BufferedWriter(new FileWriter(f));
			Set<String> userset=new HashSet<String>();
			int count=0;
	    	for (int i = firstPage; i < lastPage; i++) {
	            String content = API.directGet(
	                    url + i,
	                    "http://fuwu.paipai.com", null);

//	            System.out.println(content);
	            if(StringUtils.isEmpty(content)){
	            	System.out.println("content empty"); 
	            	return ;
	            }

	            try {
		            JSONObject obj =new JSONObject(content);
		            
		            JSONArray json =obj.getJSONArray("records");
		            
		            if (json == null ) {
		            	System.out.println("--records empty--" );
		            }
					
					for (int j = 0; j < json.length(); j++) {
						JSONObject user = json.getJSONObject(j);
						String userUin=user.getString("userUin");
						userset.add(userUin);
			    		System.out.println(userUin);
						count++;
					}
				} catch (JSONException e) {
					System.out.println(e); 
				}
	        }
	    	
	    	for(String ID :userset){
	    		output.write(ID);//写文件 
				output.newLine();
	    	}
	    	System.out.println(count);
	    	System.out.println(userset.size());
	    	output.close();
		} catch (IOException e1) {
			System.out.println(e1);
		} 
		
		System.out.println("success");

    }
	

}
