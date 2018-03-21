package models.relation;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RelationStaticModel {
	
	private static final Logger log = LoggerFactory.getLogger(RelationStaticModel.class);
	
    public static final String TAG= "RelationStaticModel";
    
    public static List<String> getModelList(String numIid,String picURL,String title,String price,String salesCount,double px){
    	List<String> ModelList = new ArrayList<String>();
    	
    	String M=M1(numIid, picURL, title, price,salesCount, px);    	
    	ModelList.add(M);
    	
    	M=M2(numIid, picURL, title, price,salesCount, px);
    	ModelList.add(M);
    	M=M3(numIid, picURL, title, price,salesCount, px);
    	ModelList.add(M);
    	M=M4(numIid, picURL, title, price,salesCount, px);
    	ModelList.add(M);
    	
    	return ModelList;
    }
        
    public static String M1(String numIid,String picURL,String title,String price,String salesCount,double px){
    	
    	StringBuilder htmlBuilder = new StringBuilder();
    	int w1=(int)(180*px);
    	int w2=(int)(180*px);
    	int h2=(int)(180*px);
    	int h3=(int)(30*px);
    	int w3=(int)(30*px);
    	int h4=(int)(20*px);
    	int fz=(int)(12*px);
    	
    	htmlBuilder.append("<div style='width:"+w1+"px;background:#FFFFFF;margin: 0 auto;'>");
    	htmlBuilder.append("<table style='width:"+w2+"px;' cellspacing='0' cellpadding='0' border='0'>");
    	htmlBuilder.append("<tbody><tr>");
    	
    	String itemHtml="" +
    			"<td style='text-align:center;width:"+w2+"px; height:"+h2+"px;' colspan='3' valign='middle'>" +
    			"<a target='_blank' href='http://item.taobao.com/item.htm?id="+numIid+"'>" +
    				"<span style='width:"+w2+"px;height:"+h2+"px;border: 1px solid #CCCCCC;display:inline-block;'>" +
    					"<img src='" +picURL+"' style='width:"+w2+"px;height:"+h2+"px;'/>" +
    				"</span>" +
    			"</a></td>";
    	htmlBuilder.append(itemHtml);
    	htmlBuilder.append("</tr><tr>");
    	htmlBuilder.append("<td  colspan='3' align='left'><div style='width:"+w2+"px;background:#ffffff;padding-left:2px;font-size:12px;height:"+h3+"px;line-height:"+h3+"px;overflow:hidden; color:#3E3E3E;'>");
    	htmlBuilder.append(title+"</div></td></tr>");
    	htmlBuilder.append("<tr>");

    	String bottomHtml=""+
    			"<td align='left' style=' padding-bottom:5px;paading-right:2px;'>" +
    			"<a  target='_blank' href='#' style='display:block;width:100%;height:20px;overflow:hidden;'>" +
    			"<span style=' background-color:#7DD7BB;float:left;width:"+w3+"px; height:20px; line-height:20px; color:#FFF; font-size:"+fz+"px; font-weight:100;display:block; text-align:center;'>热销</span>" +
    			"<span style='display:block; width:0px; height:0px; font-size:0px; line-height:0px; border-bottom:10px solid transparent; border-top:10px solid transparent;border-left:5px solid #7DD7BB; float:left;'> </span>" +
    			"</a>" +
    			"</td>"+
    			
    			"<td align='left' style='color:#7DD7BB;font-weight:bold;'><span style=' font-size:"+fz+"px;'>￥</span>" +
    			"<span style='font-family:Georgia; font-size:"+fz+"px;'>"+price+"</span>" +
    			"</td>" +
    			"<td align='right' style=' font-size:"+fz+"px;'>" +
    			"<span style=' padding-bottom:5px;font-family: 宋体; color:#7DD7BB; overflow:hidden;text-align:right;'>销量：<b style='color:#59C4CC;'>"+salesCount+"</b>件</span>" +
    			"</td>"
    	                  ;
    	htmlBuilder.append(bottomHtml);
    	htmlBuilder.append("</tr></tbody></table></div>");
    	
    	return htmlBuilder.toString();
    	
    } 
    
    public static String M2(String numIid,String picURL,String title,String price,String salesCount,double px){
    	
    	StringBuilder htmlBuilder = new StringBuilder();
    	int w1=(int)(180*px);
    	int w2=(int)(180*px);
    	int h2=(int)(180*px);
    	int h3=(int)(30*px);
    	int w3=(int)(30*px);
    	int h4=(int)(20*px);
    	int fz=(int)(12*px);
    	
    	htmlBuilder.append("<div style='width:"+w1+"px;background:#FFFFFF:5px;margin: 0 auto;'>");
    	htmlBuilder.append("<table style='width:"+w2+"px;' cellspacing='0' cellpadding='0' border='0'>");
    	htmlBuilder.append("<tbody><tr>");
    	
    	String itemHtml="" +
    			"<td style='text-align:center;width:"+w2+"px; height:"+h2+"px;' colspan='2' valign='middle'>" +
    			"<a target='_blank' href='http://item.taobao.com/item.htm?id="+numIid+"'>" +
    				"<span style='width:"+w2+"px;height:"+h2+"px;'>" +
    					"<img src='" +picURL+"' style='width:"+w2+"px;height:"+h2+"px;'/>" +
    				"</span>" +
    			"</a></td>";
    	htmlBuilder.append(itemHtml);
    	htmlBuilder.append("</tr><tr>");
    	htmlBuilder.append("<td  colspan='2' align='left'><div style='width:"+w2+"px;background:#ffffff;padding-left:2px;font-size:12px;height:"+h3+"px;line-height:"+h3+"px;overflow:hidden; color:#3E3E3E;'>");
    	htmlBuilder.append(title+"</div></td></tr>");
    	htmlBuilder.append("<tr>");

    	String bottomHtml=""+
    			"<td height='20' width='50%' align='left' style='height:20px;line-height:20px;text-align:center;overflow:hidden;'>" +
    			"<span style='color:#000000;font-family:Arial;font-style:normal;font-size:19px;line-height:18px;font-weight:700;float:left;'><sub style='font-size:12px;float:left;'>RMB</sub> "+price+"</span>" +
    			"</td>" +
    			"<td height='20' width='50%' align='right' style='height:20px;line-height:20px;text-align:center;overflow:hidden;'>" +
    			"<span style='font-family:Arial; font-weight:bold; font-size:13px;float:right;'><span>|</span>SHOP NOW</span>" +
    			"" +    			
    			"</td> ";
    	htmlBuilder.append(bottomHtml);
    	htmlBuilder.append("</tr></tbody></table></div>");
    	
    	return htmlBuilder.toString();
    	
    } 
    
   public static String M3(String numIid,String picURL,String title,String price,String salesCount,double px){
    	
    	StringBuilder htmlBuilder = new StringBuilder();
    	int w1=(int)(180*px);
    	int w2=(int)(180*px);
    	int h2=(int)(180*px);
    	int h3=(int)(30*px);
    	int w3=(int)(30*px);
    	int h4=(int)(20*px);
    	int fz=(int)(12*px);
    	
    	htmlBuilder.append("<div style='width:"+w1+"px;background:#FFFFFF;margin: 0 auto;'>");
    	htmlBuilder.append("<table style='width:"+w2+"px;' cellspacing='0' cellpadding='0' border='0'>");
    	htmlBuilder.append("<tbody><tr>");
    	
    	String itemHtml="" +
    			"<td style='text-align:center;width:"+w2+"px; height:"+h2+"px;' colspan='3' valign='middle'>" +
    			"<a target='_blank' href='http://item.taobao.com/item.htm?id="+numIid+"'>" +
    				"<span style='width:"+w2+"px;height:"+h2+"px;'>" +
    					"<img src='" +picURL+"' style='width:"+w2+"px;height:"+h2+"px;'/>" +
    				"</span>" +
    			"</a></td>";
    	
    	String titleHtml="<td  align='left'><div style='width:"+w2+"px;background:#ffffff;padding-left:2px;font-size:12px;height:"+h3+"px;line-height:"+h3+"px;overflow:hidden; color:#3E3E3E;'>" +
		          title+"</div></td>";
    	
    	String bottomHtml=""+   			
    			"<td height='20' width='120' align='left' style='font-size:14px;height:20px;line-height:20px;text-align:center;overflow:hidden;'>" +
    			"<span style='color:#000000;font-family:Arial;font-style:normal;font-size:14px;line-height:18px;font-weight:700'>RMB   " +
    			price +"</span>" +
    			"</td>";
    	
    	
    	htmlBuilder.append(itemHtml);
    	
    	htmlBuilder.append("</tr><tr>");
    	
   	    htmlBuilder.append(bottomHtml);

    	htmlBuilder.append("</tr><tr>");

    	htmlBuilder.append(titleHtml);
    	
    	htmlBuilder.append("</tr></tbody></table></div>");
    	
    	return htmlBuilder.toString();
    	
    } 
   
   public static String M4(String numIid,String picURL,String title,String price,String salesCount,double px){
   	
   	StringBuilder htmlBuilder = new StringBuilder();
   	int w1=(int)(180*px);
   	int w2=(int)(180*px);
   	int h2=(int)(180*px);
   	int h3=(int)(30*px);
   	int w3=(int)(30*px);
   	int h4=(int)(20*px);
   	int fz=(int)(12*px);
   	
   	htmlBuilder.append("<div style='width:"+w1+"px;background:#FFFFFF;margin: 0 auto;'>");
   	htmlBuilder.append("<table style='width:"+w2+"px;' cellspacing='0' cellpadding='0' border='0'>");
   	htmlBuilder.append("<tbody><tr>");
   	
   	String itemHtml="" +
   			"<td style='text-align:center;width:"+w2+"px; height:"+h2+"px;' colspan='3' valign='middle'>" +
   			"<a target='_blank' href='http://item.taobao.com/item.htm?id="+numIid+"'>" +
   				"<span style='width:"+w2+"px;height:"+h2+"px;border: 1px solid #4B4C47;display:inline-block;'>" +
   					"<img src='" +picURL+"' style='width:"+w2+"px;height:"+h2+"px;'/>" +
   				"</span>" +
   			"</a></td>";
   	htmlBuilder.append(itemHtml);
   	htmlBuilder.append("</tr><tr>");
   	htmlBuilder.append("<td  colspan='3' align='left'><div style='margin-bottom:3px;width:182px;background:#333333;color:#FEFEFE;font-size:13px;height:25px;line-height:25px;overflow:hidden;'>");
   	htmlBuilder.append(title+"</div></td></tr>");
   	htmlBuilder.append("<tr>");


   	String bottomHtml="" +
   			"<td width='35' height='13' align='left' style='font-size:12px;'>" +
   			"<span style='background-color:#ED1614; color:#ffffff; padding: 3px 3px; border-radius: 3px 3px 3px 3px;float:left;width:35px; height:13px; line-height:13px; text-align:center;'>热销</span>" +
   			"</td>" +
   			"<td height='18' align='left'>" +
   			"<div style='width:40px; padding-left:5px;color:#AAAAAA;height:20px;overflow:hidden;line-height:20px;font-size:14px;font-weight:100;font-family:Arial;'><sup style='font-size:12px;'>"+salesCount+"件</sup></div>" +
   			"</td>" +
   			"<td height='18' align='right'>" +
   			"<div style='width:96px;color:#0F0F0F;height:20px;overflow:hidden;line-height:20px;font-size:14px;font-weight:700; font-family:Arial;'><sup style='font-size:12px;'>￥" +
   			price+"</sup></div>" +
   			"</td>";

   	htmlBuilder.append(bottomHtml);
   	htmlBuilder.append("</tr></tbody></table></div>");
   	
   	return htmlBuilder.toString();
   	
   } 

}
