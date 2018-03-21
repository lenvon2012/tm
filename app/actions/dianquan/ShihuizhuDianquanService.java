package actions.dianquan;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ThreadPoolExecutor;

import models.dianquan.DianQuanItem;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import configs.TMConfigs;

/**
 * @author lyl
 * @date 2017/11/03
 */
public class ShihuizhuDianquanService {
    private static final String APPID = "1711011712442183";
    private static final String APPKEY = "2e59459876678c25a43a562c1ec05865";
    private static final String GET_CATEGORY_URL = "http://gateway.shihuizhu.net/open/cates";
    private static final String GET_DIANQUAN_URL = "http://gateway.shihuizhu.net/open/goods/%s/%s";

    public static class BatchGetDianquanInfo {
        private final static Logger LOGGER = LoggerFactory.getLogger(BatchGetDianquanInfo.class);
//        private static final int PROCESSORS_NUM = Runtime.getRuntime().availableProcessors();
        private static final long SLEEP_TIME = 1000L;

//        private static final ThreadFactory FACTORY
//                = new ThreadFactoryBuilder()
//                .setNameFormat("ShihuizhuDianquan-%d").build();
//        private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR
//                = new ThreadPoolExecutor(PROCESSORS_NUM * 2, PROCESSORS_NUM * 2
//                , 0L, TimeUnit.MICROSECONDS
//                , new LinkedBlockingDeque<Runnable>(), FACTORY);

        public boolean getInfo() {
            LOGGER.info("Start Get Shihuizhu Dianquan Job, Time: {}", new Object[]{new Date()});
            
            ThreadPoolExecutor THREAD_POOL_EXECUTOR = TMConfigs.getCheckAndUpdateDianQuanItemPool();
            
            String resultString = getResponse(getHttpGet(GET_CATEGORY_URL));
            if (resultString.trim().length() == 0) {
                return false;
            }
            JsonObject categoryJson = new JsonParser().parse(resultString).getAsJsonObject();
            JsonArray categories = categoryJson.getAsJsonArray("result");
            for (JsonElement e : categories) {
                int page = 1;
                JsonObject category = e.getAsJsonObject();
                String id = category.get("id").getAsString();
                String url = String.format(GET_DIANQUAN_URL, id, page++);
                String dianquanResult = getResponse(getHttpGet(url));
                JsonObject dianquanJson = new JsonParser().parse(dianquanResult).getAsJsonObject();
                JsonArray info = dianquanJson.getAsJsonArray("result");
                int pagecount = dianquanJson.get("pagecount").getAsInt();
                THREAD_POOL_EXECUTOR.execute(new CheckAndUpdateDianQuanItem(info));
                for (; page <= pagecount; page++) {
                    LOGGER.info("CateId: {}, Page: {}, Time: {}", id, page, new Date());
                    url = String.format(GET_DIANQUAN_URL, id, page);
                    dianquanResult = getResponse(getHttpGet(url));
                    dianquanJson = new JsonParser().parse(dianquanResult).getAsJsonObject();
                    info = dianquanJson.getAsJsonArray("result");
                    THREAD_POOL_EXECUTOR.execute(new CheckAndUpdateDianQuanItem(info));
                }
            }
            while (true) {
                if (THREAD_POOL_EXECUTOR.getActiveCount() == 0) {
                    LOGGER.info("End Get Shihuizhu Dianquan Job, Time: {}", new Object[]{new Date()});
                    return true;
                }
                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }

        private HttpGet getHttpGet(String url) {
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("APPID", APPID);
            httpGet.setHeader("APPKEY", APPKEY);
            httpGet.setHeader(HttpHeaders.HOST, "gateway.shihuizhu.net");
            return httpGet;
        }

        private String getResponse(HttpGet httpGet) {
            HttpClient client = new DefaultHttpClient();
            String resultString = "";
            try {
                HttpResponse response = client.execute(httpGet);
                resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
                httpGet.abort();
                client.getConnectionManager().shutdown();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return resultString;
        }
    }

    public static class CheckAndUpdateDianQuanItem extends Thread {
        public final static Logger LOGGER = LoggerFactory.getLogger(CheckAndUpdateDianQuanItem.class);

        private JsonArray resultArray;

        public CheckAndUpdateDianQuanItem(JsonArray resultArray) {
            this.resultArray = resultArray;
        }

        @Override
        public void run() {
            for (JsonElement e : resultArray) {
                JsonObject item = e.getAsJsonObject();
                String gid = item.get("gid").getAsString();
                DianQuanItem dianQuanItem = DianQuanItem.findDianQuanItemByGid(gid);
                try {
                    if (dianQuanItem == null) {
                        dianQuanItem = new DianQuanItem(item);
                        dianQuanItem.rawInsert();
                    } else {
                        if (DianquanUtils.checkIsNeedUpdate(dianQuanItem, item)) {
                            dianQuanItem = DianquanUtils.updateDianquanItem(dianQuanItem, item);
                            dianQuanItem.update();
                        }
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                    LOGGER.error("Gid: {}, Update Error",gid);
                }

            }
        }
    }
}
