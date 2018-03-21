
package controllers;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import job.click.HourlyCheckerJob;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;
import com.ciaosir.client.utils.JsonUtil;

public class Dianputuiguang extends TMController {

    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private static final Logger log = LoggerFactory.getLogger(Dianputuiguang.class);

    public static void index(String sid) {
        render("dianputuiguang/firstpage.html");
    }

    public static void queryUserNick() {
        User user = getUser();
        if (user == null) {
            renderText("");
        }
        renderText(user.getUserNick());
    }

    public static void tuiguang() {
        render("dianputuiguang/items.html");
    }

    public static void fenxiang() {
        render("dianputuiguang/fenxiang.html");
    }

    public static void hotRecommend() {
        render("dianputuiguang/hotrecommend.html");
    }

    public static void shortlink() {
        render("dianputuiguang/shortlink.html");
    }

    public static void analysis() {
        render("dianputuiguang/analysis.html");
    }

    public static void help() {
        render("dianputuiguang/help.html");
    }

    public static void tryhelp() {
        render("dianputuiguang/tryhelp.html");
    }

    public static void award() {
        render("dianputuiguang/award.html");
    }

    public static void shoucang() {
        render("dianputuiguang/shoucang.html");
    }

    public static void usites() {
        render("dianputuiguang/usites.html");
    }

    public static void upgrade() {
        render("/dianputuiguang/upgrade.html");
    }
    
    public static void accountAdmin() {
        render("dianputuiguang/accountadmin.html");
    }
    
    public static void manualTask() {
        render("dianputuiguang/manualTask.html");
    }
    
    public static void analysisItem(Long numIid) throws IOException {
        User user = getUser();
        long userId = user.getId();
        int interval = 7;
        long now = System.currentTimeMillis();
        long days = now / DateUtil.DAY_MILLIS;
        if (now - user.firstLoginTime < DateUtil.WEEK_MILLIS) {
            interval = (int) Math.floor((now - user.firstLoginTime) / DateUtil.WEEK_MILLIS);
        }
        List<UserLiuliang> liuliangs = new ArrayList<UserLiuliang>();
        for (int i = 0; i < interval; i++) {
            int pv = 0, uv = 0;
            if (numIid % 23 > 0) {
                uv = (int) (((days - i) * numIid) % 23) + 5;
            } else if (numIid % 19 > 0) {
                uv = (int) (((days - i) * numIid) % 19) + 6;
            } else {
                uv = (int) (((days - i) * numIid) % 29) + 3;
            }

            if (userId % 23 > 0) {
                uv = (int) ((uv * userId) % 23) + 5;
            } else if (numIid % 19 > 0) {
                uv = (int) ((uv * userId) % 19) + 6;
            } else {
                uv = (int) ((uv * userId) % 29) + 3;
            }
            String date = sdf.format(now - DateUtil.DAY_MILLIS * (i + 1));
            UserLiuliang liuliang = new UserLiuliang(pv, uv, date);
            liuliangs.add(liuliang);
        }
        renderJSON(JsonUtil.getJson(liuliangs));
        renderMockFileInJsonIfDev("dianputuiguang.analysis.json");
    }

    public static class UserLiuliang {

        private int pv = 0;

        private int uv = 0;

        private String date = StringUtils.EMPTY;

        public UserLiuliang(int pv, int uv, String date) {
            super();
            this.pv = pv;
            this.uv = uv;
            this.date = date;
        }

        public int getPv() {
            return pv;
        }

        public void setPv(int pv) {
            this.pv = pv;
        }

        public int getUv() {
            return uv;
        }

        public void setUv(int uv) {
            this.uv = uv;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

    }
    
    // interval以毫秒为单位
    public static void submitManualTask(Long numIid, String referer, Long interval, Long count) {
    	if(numIid == null || numIid <= 0) {
    		renderFailedJson("宝贝ID需为数字且大于0，请修正~");
    	}
    	if(StringUtils.isEmpty(referer)) {
    		renderFailedJson("请输入referer");
    	}
    	if(interval == null || interval <= 0) {
    		renderFailedJson("时间间隔需为数字且大于0，请修正~");
    	}
    	if(count == null || count <= 0) {
    		renderFailedJson("流量个数需为数字且大于0，请修正~");
    	}
    	String url = "http://item.taobao.com/item.htm?id=" + numIid;
    	Long sleepMills = interval / count;
    	for(int i = 0; i < count; i++) {
    		HourlyCheckerJob.getPool().submit(new HourlyCheckerJob.manualClickItem(url, referer, 0));
    		CommonUtils.sleepQuietly(sleepMills);
    	}
    	renderSuccessJson("任务已提交");
    }
}
