/**
 * 
 */
package paipai;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import transaction.MapIterator;

import com.ciaosir.client.api.API;

/**
 * @author navins
 * @date: Nov 6, 2013 11:19:45 PM
 */
public class GenPaiPaiOnlineCode {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        String name = "dianzhang";
        String appId = "262400";
        int firstPage = 1;
        int lastPage = 2;

        String url = "http://fuwu.paipai.com/appstore/ui/my/app/appdetail/records.xhtml?appId=" + appId
                + "&pageNumber=";
        String fs = "/Users/navins/Code/" + name + ".html";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {

            File f = new File(fs);

            if (f.exists()) {
                System.out.println("文件存在");
            } else {
                System.out.println("文件不存在");
                f.createNewFile();// 不存在则创建
            }

            final BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fs), "UTF-8"));
            HashMap<String, String> subscribMap = new HashMap<String, String>();
            int count = 0;
            for (int i = firstPage; i < lastPage; i++) {
                String content = API.directGet(url + i, "http://fuwu.paipai.com", null);

                // System.out.println(content);
                if (StringUtils.isEmpty(content)) {
                    System.out.println("content empty");
                    return;
                }

                try {
                    JSONObject obj = new JSONObject(content);

                    JSONArray json = obj.getJSONArray("records");

                    if (json == null) {
                        System.out.println("--records empty--");
                    }

                    for (int j = 0; j < json.length(); j++) {
                        JSONObject user = json.getJSONObject(j);
                        String userUin = user.getString("userUin");
                        Long paySuccessTime = user.getLong("paySuccessTime");
                        String time = sdf.format(new Date(paySuccessTime));
                        subscribMap.put(userUin, time);
                        // output.write(userUin + "\n");
                        count++;
                    }

                } catch (JSONException e) {
                    System.out.println(e);
                }
            }

            output.write("<html><head><meta http-equiv='Content-Type' content='text/html; charset=utf-8'>");
            output.write("<style>table tr td {text-align: center;}table {border: 1px solid #aaa;}table tr td, th {border-right: 1px solid #ccc;border-bottom: 1px solid #ccc;}table tr:hover{background:#eee}</style></head><body>");
            output.write("<table style='width:700px;margin: 0 auto;border-collapse: collapse;'><tr><th style='width:40%'>订购时间</th><th style='width:30%'>QQ</th><th style='width:30%'>点击联系</th></tr>");
            output.newLine();

            new MapIterator<String, String>(subscribMap) {

                @Override
                public void execute(Entry<String, String> entry) {
                    String key = entry.getKey();
                    String val = entry.getValue();

                    try {
                        output.write("<tr><td>" + val + "</td><td>" + key
                                + "</td><td><a target='_blank' href='http://wpa.qq.com/msgrd?v=3&uin=" + key
                                + "&site=qq&menu=yes'><img border='0' src='http://wpa.qq.com/pa?p=2:" + key
                                + ":51' alt='点击这里给我发消息' title='点击这里给我发消息'/></a></td></tr>");
                        output.newLine();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

            }.call();
            output.write("</table></body></html>");
            output.newLine();
            output.close();
        } catch (IOException e1) {
            System.out.println(e1);
        }
    }

}
