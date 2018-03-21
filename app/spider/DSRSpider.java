/**
 * 
 */
package spider;

import java.util.concurrent.Callable;

import models.user.SellerDSR;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.PYFutureTaskPool;
import com.ciaosir.client.utils.SimpleHttpRetryUtil;

/**
 * @author navins
 * @date: 2013年10月20日 下午1:49:17
 */
public class DSRSpider {

    private final static Logger log = LoggerFactory.getLogger(DSRSpider.class);

    public static double getSellerGoodRate(Long userId) {
        double goodRate = 0;
        if (userId == null || userId.longValue() < 0L) {
            return -1;
        }
        String key = "SM_368_dsr-" + userId;
        String url = "http://count.tbcdn.cn/counter3?keys=" + key + "&callback=DT.mods.SKU.CountCenter.setReviewCount";
        String refer = "http://www.taobao.com/";
        String content = SimpleHttpRetryUtil.retryGetWebContent(url, refer);

        try {
            JSONObject obj = parseCounterContent(content);
            if (obj == null) {
                return -1;
            }
            if (!"0".equals(obj.getString(key))) {
                JSONObject dsr = obj.getJSONObject(key);
                goodRate = dsr.getDouble("gp");
            }
        } catch (JSONException e) {
            log.error("req err: " + url);
            log.warn(">>>>> " + content);
//            log.error(e.getMessage(), e);
        }
        return goodRate;
    }

    public static JSONObject parseCounterContent(String content) {
        if (StringUtils.isBlank(content)) {
            return null;
        }
        if (content.indexOf('(') < 0 || content.lastIndexOf(')') < 0) {
            return null;
        }
        content = content.substring(content.indexOf('(') + 1, content.lastIndexOf(')'));
        try {
            JSONObject json = new JSONObject(content);
            return json;
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    public static PYFutureTaskPool<Void> pool = new PYFutureTaskPool<Void>(16);

    public static void spiderSellerGoodRate(Long userId) {
        pool.submit(new DoSellerGoodRateCaller(userId));
    }

    public static class DoSellerGoodRateCaller implements Callable<Void> {

        public Long userId;

        public DoSellerGoodRateCaller(Long userId) {
            this.userId = userId;
        }

        @Override
        public Void call() throws Exception {
            int retry = 1;
            double goodRate = DSRSpider.getSellerGoodRate(userId);
            while (goodRate < 0 && ++retry < 3) {
                log.error("spider error retry: uesrId = " + userId);
                goodRate = DSRSpider.getSellerGoodRate(userId);
            }

            if (goodRate >= 0) {
                new SellerDSR(userId, goodRate).jdbcSave();
            }
            return null;
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        System.out.println(DSRSpider.getSellerGoodRate(132496356L));
    }

}
