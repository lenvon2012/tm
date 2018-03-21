package models.associate;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaticModel {
    
    private static Logger log = LoggerFactory.getLogger(StaticModel.class);
    
    public static String TAG = "STATICMODEL";
    
    /**
     * 模板代码 无宝贝
     * @param modelId
     * @return
     */
    public static String template(Long modelId){
        
        Map<Long,String> map = new HashMap<Long,String>();
        
        StringBuilder temp1 = new StringBuilder();
        
        temp1.append("<div class='template' value='100011' style='font:12px tahoma,arial,宋体b8b\4f53,sans-serif;width:750px;margin:10px auto 0 auto;display:block;'>");
        temp1.append("<a class='tzg_tag_name' ></a>");
        temp1.append("<table class='tmp_table' border='0' cellpadding='0' cellspacing='0' value='123456'> ");
        temp1.append("<tbody>");
        temp1.append("<tr>");
        temp1.append("<td style='padding-bottom:10px;' background='/public/images/associateModel/template1/template_point.png'>");
        temp1.append("<div><img src='/public/images/associateModel/template1/template_top.png'></div>");
        temp1.append("<table  border='0' cellpadding='0' cellspacing='0'> ");
        temp1.append("<tbody>");
        temp1.append("<tr>");
        //1
        temp1.append("<td class='tmp_td_item' style='height:320px;width:250px;padding-bottom:10px;'>");
        temp1.append("<!--有宝贝 -->");
        temp1.append("<div class='tmp_item_show'>" +
                        "<div class='tmp_td_item_id'></div>" +
                        "<table cellspacing='0' cellpadding='0' border='0' style='margin:8px auto 0 auto;height:303px;width:230px;'>" +
                            "<tbody>" +
                                "<!--图片 -->" +
                                "<tr>" +
                                     "<td style='width:230px;height:230px;background-color:#fff;text-align:center;vertical-align:middle;'>" +
                                     "<a class='tmp_td_item_href' target='_blank' style='display:block;'>" +
                                             "<img class='tmp_td_item_img' border='0' style='vertical-align:middle;height:230px;width:230px'></img>" +
                                         "</a>" +
                                     "</td>" +
                                 "</tr>" +
                                 "<!--标题 -->" +
                                 "<tr>" +
                                     "<td style='height:36px;padding:5px;background-color:#f2fafc;'>" +
                                         "<div style='width:220px;height:36px;word-wrap:break-word;word-break:break-all;overflow:hidden;'>" +
                                             "<a target='_blank' style='line-height:18px;text-decoration:none;color:#7a7a7a;'>" +
                                                 "<span class='tmp_td_item_title'>" +
                                                 "</span>" +
                                             "</a>" +
                                         "</div>" +
                                     "</td>" +
                                 "</tr>" +
                                 "<!--价格 -->" +
                                 "<tr>" +
                                     "<td style='width:240px;height:30px;font-size:14px;font-family:Microsoft Yahei;color:#fff;line-height:30px;padding-top:5px;background-color:#7eab15;text-align:center;'>" +
                                         "<a target='_blank' style='color:#fff;text-decoration:none;'>" +
                                             "¥" +
                                             "<span class='tmp_td_item_price' style='font-size:20px;font-family:'corbel';font-weight:bold;'>" +
                                             "</span>" +
                                         "</a>" +
                                     "</td>" +
                                 "</tr>" +
                                 "<!--尾部 -->" +
                                 "<tr>" +
                                     "<td background='http://img02.taobaocdn.com/imgextra/i2/60113414/T2Ge8XXfJPXXXXXXXX-60113414.png' height='7'></td>" +
                                  "</tr>" +
                              "</tbody>" +
                          "</table>" +
                      "</div></td>");
                    
//        temp1.append("<!--没宝贝-->");
//        temp1.append("<div class='tmp_item_hide' style='display: block;'>");  
//        temp1.append("<table style='margin:8px auto 0 auto;height:317px;width:230px;background-color:#fff;' border='0' cellpadding='0' cellspacing='0'>");  
//        temp1.append("<tbody><tr>");  
//        temp1.append("<td class='template_item' style='height:296px;text-align:center;color:#ddd;'>添加宝贝</td>");  
//        temp1.append("</tr>");  
//        temp1.append("<tr>");  
//        temp1.append("<td background='http://img02.taobaocdn.com/imgextra/i2/60113414/T2Ge8XXfJPXXXXXXXX-60113414.png' height='7'></td>");  
//        temp1.append("</tr>");  
//        temp1.append("</tbody></table></div></td>");  
        
        //2
        temp1.append("<td class='tmp_td_item' style='height:320px;width:250px;padding-bottom:10px;'>");
        temp1.append("<!--有宝贝 -->");
        temp1.append("<div class='tmp_item_show'>" +
                        "<div class='tmp_td_item_id'></div>" +
                        "<table cellspacing='0' cellpadding='0' border='0' style='margin:8px auto 0 auto;height:303px;width:230px;'>" +
                            "<tbody>" +
                                "<!--图片 -->" +
                                "<tr>" +
                                     "<td style='width:230px;height:230px;background-color:#fff;text-align:center;vertical-align:middle;'>" +
                                         "<a class='tmp_td_item_href' target='_blank' style='display:block;'>" +
                                             "<img class='tmp_td_item_img' border='0' style='vertical-align:middle;height:230px;width:230px' ></img>" +
                                         "</a>" +
                                     "</td>" +
                                 "</tr>" +
                                 "<!--标题 -->" +
                                 "<tr>" +
                                     "<td style='height:36px;padding:5px;background-color:#f2fafc;'>" +
                                         "<div style='width:220px;height:36px;word-wrap:break-word;word-break:break-all;overflow:hidden;'>" +
                                             "<a target='_blank' style='line-height:18px;text-decoration:none;color:#7a7a7a;'>" +
                                                 "<span class='tmp_td_item_title'>" +
                                                 "</span>" +
                                             "</a>" +
                                         "</div>" +
                                     "</td>" +
                                 "</tr>" +
                                 "<!--价格 -->" +
                                 "<tr>" +
                                     "<td style='width:240px;height:30px;font-size:14px;font-family:Microsoft Yahei;color:#fff;line-height:30px;padding-top:5px;background-color:#7eab15;text-align:center;'>" +
                                         "<a target='_blank' style='color:#fff;text-decoration:none;'>" +
                                             "¥" +
                                             "<span class='tmp_td_item_price' style='font-size:20px;font-family:'corbel';font-weight:bold;'>" +
                                             "</span>" +
                                         "</a>" +
                                     "</td>" +
                                 "</tr>" +
                                 "<!--尾部 -->" +
                                 "<tr>" +
                                     "<td background='http://img02.taobaocdn.com/imgextra/i2/60113414/T2Ge8XXfJPXXXXXXXX-60113414.png' height='7'></td>" +
                                  "</tr>" +
                              "</tbody>" +
                          "</table>" +
                      "</div></td>");
//        temp1.append("<!--没宝贝-->");
//        temp1.append("<div class='tmp_item_hide' style='display: block;'>");  
//        temp1.append("<table style='margin:8px auto 0 auto;height:317px;width:230px;background-color:#fff;' border='0' cellpadding='0' cellspacing='0'>");  
//        temp1.append("<tbody><tr>");  
//        temp1.append("<td class='template_item' style='height:296px;text-align:center;color:#ddd;'>添加宝贝</td>");  
//        temp1.append("</tr>");  
//        temp1.append("<tr>");  
//        temp1.append("<td background='http://img02.taobaocdn.com/imgextra/i2/60113414/T2Ge8XXfJPXXXXXXXX-60113414.png' height='7'></td>");  
//        temp1.append("</tr>");  
//        temp1.append("</tbody></table></div></td>");
        
        //3
        temp1.append("<td class='tmp_td_item' style='height:320px;width:250px;padding-bottom:10px;'>");
        temp1.append("<!--有宝贝 -->");
        temp1.append("<div class='tmp_item_show' >" +
                        "<div class='tmp_td_item_id'></div>" +
                        "<table cellspacing='0' cellpadding='0' border='0' style='margin:8px auto 0 auto;height:303px;width:230px;'>" +
                            "<tbody>" +
                                "<!--图片 -->" +
                                "<tr>" +
                                     "<td style='width:230px;height:230px;background-color:#fff;text-align:center;vertical-align:middle;'>" +
                                         "<a class='tmp_td_item_href' target='_blank' style='display:block;'>" +
                                             "<img class='tmp_td_item_img' border='0' style='vertical-align:middle;height:230px;width:230px' ></img>" +
                                         "</a>" +
                                     "</td>" +
                                 "</tr>" +
                                 "<!--标题 -->" +
                                 "<tr>" +
                                     "<td style='height:36px;padding:5px;background-color:#f2fafc;'>" +
                                         "<div style='width:220px;height:36px;word-wrap:break-word;word-break:break-all;overflow:hidden;'>" +
                                             "<a target='_blank' style='line-height:18px;text-decoration:none;color:#7a7a7a;'>" +
                                                 "<span class='tmp_td_item_title'>" +
                                                 "</span>" +
                                             "</a>" +
                                         "</div>" +
                                     "</td>" +
                                 "</tr>" +
                                 "<!--价格 -->" +
                                 "<tr>" +
                                     "<td style='width:240px;height:30px;font-size:14px;font-family:Microsoft Yahei;color:#fff;line-height:30px;padding-top:5px;background-color:#7eab15;text-align:center;'>" +
                                         "<a target='_blank' style='color:#fff;text-decoration:none;'>" +
                                             "¥" +
                                             "<span  class='tmp_td_item_price' style='font-size:20px;font-family:'corbel';font-weight:bold;'>" +
                                             "</span>" +
                                         "</a>" +
                                     "</td>" +
                                 "</tr>" +
                                 "<!--尾部 -->" +
                                 "<tr>" +
                                     "<td background='http://img02.taobaocdn.com/imgextra/i2/60113414/T2Ge8XXfJPXXXXXXXX-60113414.png' height='7'></td>" +
                                  "</tr>" +
                              "</tbody>" +
                          "</table>" +
                      "</div></td></tr><tr>");
//        temp1.append("<!--没宝贝-->");
//        temp1.append("<div class='tmp_item_hide' style='display: block;'>");  
//        temp1.append("<table style='margin:8px auto 0 auto;height:317px;width:230px;background-color:#fff;' border='0' cellpadding='0' cellspacing='0'>");  
//        temp1.append("<tbody><tr>");  
//        temp1.append("<td class='template_item' style='height:296px;text-align:center;color:#ddd;'>添加宝贝</td>");  
//        temp1.append("</tr>");  
//        temp1.append("<tr>");  
//        temp1.append("<td background='http://img02.taobaocdn.com/imgextra/i2/60113414/T2Ge8XXfJPXXXXXXXX-60113414.png' height='7'></td>");  
//        temp1.append("</tr>");  
//        temp1.append("</tbody></table></div></td></tr><tr>");
        
        //4
        temp1.append("<td class='tmp_td_item' style='height:320px;width:250px;padding-bottom:10px;'>");
        temp1.append("<!--有宝贝 -->");
        temp1.append("<div class='tmp_item_show'>" +
                        "<div class='tmp_td_item_id'></div>" +
                        "<table cellspacing='0' cellpadding='0' border='0' style='margin:8px auto 0 auto;height:303px;width:230px;'>" +
                            "<tbody>" +
                                "<!--图片 -->" +
                                "<tr>" +
                                     "<td style='width:230px;height:230px;background-color:#fff;text-align:center;vertical-align:middle;'>" +
                                         "<a class='tmp_td_item_href' target='_blank' style='display:block;'>" +
                                             "<img class='tmp_td_item_img' border='0' style='vertical-align:middle;height:230px;width:230px' ></img>" +
                                         "</a>" +
                                     "</td>" +
                                 "</tr>" +
                                 "<!--标题 -->" +
                                 "<tr>" +
                                     "<td style='height:36px;padding:5px;background-color:#f2fafc;'>" +
                                         "<div style='width:220px;height:36px;word-wrap:break-word;word-break:break-all;overflow:hidden;'>" +
                                             "<a target='_blank' style='line-height:18px;text-decoration:none;color:#7a7a7a;'>" +
                                                 "<span class='tmp_td_item_title'>" +
                                                 "</span>" +
                                             "</a>" +
                                         "</div>" +
                                     "</td>" +
                                 "</tr>" +
                                 "<!--价格 -->" +
                                 "<tr>" +
                                     "<td style='width:240px;height:30px;font-size:14px;font-family:Microsoft Yahei;color:#fff;line-height:30px;padding-top:5px;background-color:#7eab15;text-align:center;'>" +
                                         "<a target='_blank' style='color:#fff;text-decoration:none;'>" +
                                             "¥" +
                                             "<span class='tmp_td_item_price' style='font-size:20px;font-family:'corbel';font-weight:bold;'>" +
                                             "</span>" +
                                         "</a>" +
                                     "</td>" +
                                 "</tr>" +
                                 "<!--尾部 -->" +
                                 "<tr>" +
                                     "<td background='http://img02.taobaocdn.com/imgextra/i2/60113414/T2Ge8XXfJPXXXXXXXX-60113414.png' height='7'></td>" +
                                  "</tr>" +
                              "</tbody>" +
                          "</table>" +
                      "</div></td>");
//        temp1.append("<!--没宝贝-->");
//        temp1.append("<div class='tmp_item_hide' style='display: block;'>");  
//        temp1.append("<table style='margin:8px auto 0 auto;height:317px;width:230px;background-color:#fff;' border='0' cellpadding='0' cellspacing='0'>");  
//        temp1.append("<tbody><tr>");  
//        temp1.append("<td class='template_item' style='height:296px;text-align:center;color:#ddd;'>添加宝贝</td>");  
//        temp1.append("</tr>");  
//        temp1.append("<tr>");  
//        temp1.append("<td background='http://img02.taobaocdn.com/imgextra/i2/60113414/T2Ge8XXfJPXXXXXXXX-60113414.png' height='7'></td>");  
//        temp1.append("</tr>");  
//        temp1.append("</tbody></table></div></td>");
        
        //5
        temp1.append("<td class='tmp_td_item' style='height:320px;width:250px;padding-bottom:10px;'>");
        temp1.append("<!--有宝贝 -->");
        temp1.append("<div class='tmp_item_show' >" +
                        "<div class='tmp_td_item_id'></div>" +
                        "<table cellspacing='0' cellpadding='0' border='0' style='margin:8px auto 0 auto;height:303px;width:230px;'>" +
                            "<tbody>" +
                                "<!--图片 -->" +
                                "<tr>" +
                                     "<td style='width:230px;height:230px;background-color:#fff;text-align:center;vertical-align:middle;'>" +
                                         "<a class='tmp_td_item_href' target='_blank' style='display:block;'>" +
                                             "<img class='tmp_td_item_img' border='0' style='vertical-align:middle;height:230px;width:230px' ></img>" +
                                         "</a>" +
                                     "</td>" +
                                 "</tr>" +
                                 "<!--标题 -->" +
                                 "<tr>" +
                                     "<td style='height:36px;padding:5px;background-color:#f2fafc;'>" +
                                         "<div style='width:220px;height:36px;word-wrap:break-word;word-break:break-all;overflow:hidden;'>" +
                                             "<a target='_blank' style='line-height:18px;text-decoration:none;color:#7a7a7a;'>" +
                                                 "<span class='tmp_td_item_title'>" +
                                                 "</span>" +
                                             "</a>" +
                                         "</div>" +
                                     "</td>" +
                                 "</tr>" +
                                 "<!--价格 -->" +
                                 "<tr>" +
                                     "<td style='width:240px;height:30px;font-size:14px;font-family:Microsoft Yahei;color:#fff;line-height:30px;padding-top:5px;background-color:#7eab15;text-align:center;'>" +
                                         "<a target='_blank' style='color:#fff;text-decoration:none;'>" +
                                             "¥" +
                                             "<span class='tmp_td_item_price' style='font-size:20px;font-family:'corbel';font-weight:bold;'>" +
                                             "</span>" +
                                         "</a>" +
                                     "</td>" +
                                 "</tr>" +
                                 "<!--尾部 -->" +
                                 "<tr>" +
                                     "<td background='http://img02.taobaocdn.com/imgextra/i2/60113414/T2Ge8XXfJPXXXXXXXX-60113414.png' height='7'></td>" +
                                  "</tr>" +
                              "</tbody>" +
                          "</table>" +
                      "</div></td>");
//        temp1.append("<!--没宝贝-->");
//        temp1.append("<div class='tmp_item_hide' style='display: block;'>");  
//        temp1.append("<table style='margin:8px auto 0 auto;height:317px;width:230px;background-color:#fff;' border='0' cellpadding='0' cellspacing='0'>");  
//        temp1.append("<tbody><tr>");  
//        temp1.append("<td class='template_item' style='height:296px;text-align:center;color:#ddd;'>添加宝贝</td>");  
//        temp1.append("</tr>");  
//        temp1.append("<tr>");  
//        temp1.append("<td background='http://img02.taobaocdn.com/imgextra/i2/60113414/T2Ge8XXfJPXXXXXXXX-60113414.png' height='7'></td>");  
//        temp1.append("</tr>");  
//        temp1.append("</tbody></table></div></td>");
        
        //6
        temp1.append("<td class='tmp_td_item' style='height:320px;width:250px;padding-bottom:10px;'>");
        temp1.append("<!--有宝贝 -->");
        temp1.append("<div class='tmp_item_show' >" +
                        "<div class='tmp_td_item_id'></div>" +
                        "<table cellspacing='0' cellpadding='0' border='0' style='margin:8px auto 0 auto;height:303px;width:230px;'>" +
                            "<tbody>" +
                                "<!--图片 -->" +
                                "<tr>" +
                                     "<td style='width:230px;height:230px;background-color:#fff;text-align:center;vertical-align:middle;'>" +
                                         "<a class='tmp_td_item_href' target='_blank' style='display:block;'>" +
                                             "<img class='tmp_td_item_img' border='0' style='vertical-align:middle;height:230px;width:230px' ></img>" +
                                         "</a>" +
                                     "</td>" +
                                 "</tr>" +
                                 "<!--标题 -->" +
                                 "<tr>" +
                                     "<td style='height:36px;padding:5px;background-color:#f2fafc;'>" +
                                         "<div style='width:220px;height:36px;word-wrap:break-word;word-break:break-all;overflow:hidden;'>" +
                                             "<a target='_blank' style='line-height:18px;text-decoration:none;color:#7a7a7a;'>" +
                                                 "<span class='tmp_td_item_title'>" +
                                                 "</span>" +
                                             "</a>" +
                                         "</div>" +
                                     "</td>" +
                                 "</tr>" +
                                 "<!--价格 -->" +
                                 "<tr>" +
                                     "<td style='width:240px;height:30px;font-size:14px;font-family:Microsoft Yahei;color:#fff;line-height:30px;padding-top:5px;background-color:#7eab15;text-align:center;'>" +
                                         "<a target='_blank' style='color:#fff;text-decoration:none;'>" +
                                             "¥" +
                                             "<span class='tmp_td_item_price' style='font-size:20px;font-family:'corbel';font-weight:bold;'>" +
                                             "</span>" +
                                         "</a>" +
                                     "</td>" +
                                 "</tr>" +
                                 "<!--尾部 -->" +
                                 "<tr>" +
                                     "<td background='http://img02.taobaocdn.com/imgextra/i2/60113414/T2Ge8XXfJPXXXXXXXX-60113414.png' height='7'></td>" +
                                  "</tr>" +
                              "</tbody>" +
                          "</table>" +
                      "</div></td>");
//        temp1.append("<!--没宝贝-->");
//        temp1.append("<div class='tmp_item_hide' style='display: block;'>");  
//        temp1.append("<table style='margin:8px auto 0 auto;height:317px;width:230px;background-color:#fff;' border='0' cellpadding='0' cellspacing='0'>");  
//        temp1.append("<tbody><tr>");  
//        temp1.append("<td class='template_item' style='height:296px;text-align:center;color:#ddd;'>添加宝贝</td>");  
//        temp1.append("</tr>");  
//        temp1.append("<tr>");  
//        temp1.append("<td background='http://img02.taobaocdn.com/imgextra/i2/60113414/T2Ge8XXfJPXXXXXXXX-60113414.png' height='7'></td>");  
//        temp1.append("</tr>");  
//        temp1.append("</tbody></table></div></td>");
        temp1.append("</tr></tbody></table></td></tr></tbody></table></div>");  
        
        map.put(123456L, temp1.toString());
        
        return map.get(modelId);
    }
}
