package models.tmsearch;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.persistence.Entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import bustbapi.SellerAPI;

import com.ciaosir.client.api.API.PYSpiderOption;
import com.ciaosir.client.pojo.ItemThumb;

@Entity
public class Grade extends Model{
	
	private static final Logger log = LoggerFactory.getLogger(Grade.class);
	
	public static String getGradeHtml(String nick) throws Exception{
		String gradeHtml = null;
		String url = null;
		
		try{
			PYSpiderOption option = new PYSpiderOption(null, false, 1, false, 2000);
			option.setSort("biz30day");		
			List<ItemThumb> itemArray_biz = SellerAPI.getItemArray(nick, null, option);
			long sellerId = itemArray_biz.get(1).getSellerId();
			url = "http://rate.taobao.com/user-rate-" + sellerId + ".htm";			
		}catch(Exception e){
			log.warn(e.getMessage(), e);
		}		
		
		gradeHtml = getHTML(url, "UTF-8");
		
		return gradeHtml;
	}
	
	private static String getHTML(String pageURL, String encoding) { 
        StringBuilder pageHTML = new StringBuilder(); 
        try { 
            URL url = new URL(pageURL); 
            HttpURLConnection connection = (HttpURLConnection) url.openConnection(); 
            connection.setRequestProperty("User-Agent", "MSIE 7.0"); 
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), encoding)); 
            String line = null; 
            while ((line = br.readLine()) != null) { 
                pageHTML.append(line); 
                pageHTML.append("\r\n"); 
            } 
            connection.disconnect(); 
        } catch (Exception e) { 
            e.printStackTrace(); 
        } 
        return pageHTML.toString(); 
    } 

}
