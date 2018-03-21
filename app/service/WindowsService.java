
package service;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import models.item.ItemPlay;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.mvc.Http;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.api.API;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.NumberUtil;

import configs.TMConfigs;
import controllers.SWindows.WindowConcigs;

public class WindowsService {

    static String TIMEOUT_STR = "3s";

    static int TIMEOUT_MILLIS = 10000;

    private static final Logger log = LoggerFactory.getLogger(WindowsService.class);

    public static final String TAG = "WindowsService";

    public static WindowConcigs getConfig(Long userId) {
        String url = ServiceURLProvider.tbtProvider.getNext() + "/SWindows/sConfigs?userId=" + userId;
        String res = null;
        try {
            res = API.directPost(url, null, TIMEOUT_MILLIS, MapUtils.EMPTY_MAP);
        } catch (Exception e) {
            log.error(" error url :" + url);
            log.warn(e.getMessage(), e);
        }
        if (StringUtils.isEmpty(res)) {
            return null;
        }
        WindowConcigs config = JsonUtil.toObject(res, WindowConcigs.class);
        return config;
    }

    public static Long[] getCurrentReadyIds() {

        String url = ServiceURLProvider.tbtProvider.getNext() + "/SWindows/pollOutSet";

        String res = null;
        try {
            res = API.directPost(url, null, TIMEOUT_MILLIS, null);
        } catch (Exception e) {
            log.error(" error url:" + url);
            log.warn(e.getMessage(), e);
        }

        if (StringUtils.isBlank(res)) {
            log.info("[no res ]");
            return NumberUtil.EMPTY_LONG_ARRAY;
        }
        log.info("[res :]" + res);

        return JsonUtil.toObject(res, Long[].class);
    }

    //public static void addLog(Long userId, Long numIid, String msg, boolean isError) {
    public static void addLog(Long userId, Long numIid) {
        String url = null;
        try {
            String msg = URLEncoder.encode("橱窗推荐成功", "utf-8");
            url = ServiceURLProvider.tbtProvider.getNext() + "/SWindows/addLog?userId=" + userId + "&numIid="
                    + numIid + "&msg=" + msg;

            API.directPost(url, null, TIMEOUT_MILLIS, MapUtils.EMPTY_MAP);

        } catch (Exception e) {
            log.warn(" error url :" + url);
            log.warn(e.getMessage(), e);
        }
    }

    public static void addQueue(Long userId) {
//        String url = ServiceURLProvider.tbtProvider.getNext() + "/SWindows/addQueue?userId=" + userId;
//        WS.url(url).getAsync();
        addLightWeightCommon(userId);
    }

    public static void addLightWeight(final String host, final Long userId) {
        TMConfigs.getShowwindowPool().submit(new Callable<ItemPlay>() {
            @Override
            public ItemPlay call() throws Exception {
                CommonUtils.sleepQuietly(5000L);
                String url = host + "/SWindows/addLightWeight?userId=" + userId;
                try {
                    API.directPost(url, null, 3000, MapUtils.EMPTY_MAP);
                } catch (Exception e) {
                    log.error(" fail at url" + url);
                    log.warn(e.getMessage(), e);
                }
                return null;
            }
        });
    }

    public static void addDropWindowCheck() {

        TMConfigs.getShowwindowPool().submit(new Callable<ItemPlay>() {
            @Override
            public ItemPlay call() throws Exception {
                int retry = 5;
                while (retry-- > 0) {
                    String host = ServiceURLProvider.tbtProvider.getNext();
                    log.info("[ws for host:]" + host);
                    if (host == null) {
                        return null;
                    }

                    String url = host + "/SWindows/checkDropCache";
                    try {
                        API.directPost(url, null, 3000, MapUtils.EMPTY_MAP);
                        return null;
                    } catch (Exception e) {
                        log.error(" fail at url" + url);
                        log.warn(e.getMessage(), e);
                    }
                    CommonUtils.sleepQuietly(300L);
                }

                return null;
            }
        });
    }

    public static void addLightWeightInstant(final Long userId) {
        final String commonHost = ServiceURLProvider.instantWindowProvider.getNext();
//        addLightWeight(commonHost, userId);
        TMConfigs.getShowwindowPool().submit(new Callable<ItemPlay>() {
            @Override
            public ItemPlay call() throws Exception {
                String url = commonHost + "/SWindows/addLightWeightInstant?userId=" + userId;
                int count = 2;
                while (count-- > 0) {
                    try {
                        API.directPost(url, null, 3000, MapUtils.EMPTY_MAP);
                        return null;
                    } catch (Exception e) {
                        log.error(" fail at url" + url);
                        log.warn(e.getMessage(), e);
                    }
                    CommonUtils.sleepQuietly(3000L);
                }

                return null;
            }
        });
    }

    public static void addItemUpdateJob(final Long userId) {
        int count = 0;
        while (count++ < 3) {
            String commonHost = ServiceURLProvider.instantWindowProvider.getNext();
            String url = commonHost + "/SWindows/addItemUpdateJob?userId=" + userId;
            try {
                API.directPost(url, null, 3000, MapUtils.EMPTY_MAP);
                return;
            } catch (Exception e) {
                log.error(" fail at url" + url);
                log.warn(e.getMessage(), e);
            }
        }
    }

    public static void addLightWeightCancelInstant(final Long userId, final Long numIid) {
        final String commonHost = ServiceURLProvider.instantWindowProvider.getNext();
//        addLightWeight(commonHost, userId);
        TMConfigs.getShowwindowPool().submit(new Callable<ItemPlay>() {
            @Override
            public ItemPlay call() throws Exception {
                String url = commonHost + "/SWindows/addLightWeightCancelInstant?userId=" + userId + "&numIid="
                        + numIid;
                try {
                    API.directPost(url, null, 3000, MapUtils.EMPTY_MAP);
                } catch (Exception e) {
                    log.error(" fail at url" + url);
                    log.warn(e.getMessage(), e);
                }
                return null;
            }
        });
    }

    public static void addLightWeightCommon(final Long userId) {
        final String commonHost = ServiceURLProvider.tbtProvider.getNext();
        addLightWeight(commonHost, userId);
    }

    public static void addLightWeightIds(Set<Long> ids) {
        log.info("[add size:]" + ids.size());
        String host = ServiceURLProvider.tbtProvider.getNext();
        String url = host + "/SWindows/addQueueIds";
        Map<String, String> params = new HashMap<String, String>();
        params.put("ids", StringUtils.join(ids, ','));
        try {
            API.directPost(url, null, TIMEOUT_MILLIS, params);
        } catch (Exception e) {
            log.error(" url:" + url);
            log.warn(e.getMessage(), e);
        }
    }

    public static boolean addDelist(Long userId, Long numIid, int delistIndex) {
        int count = 0;
        do {
            String host = ServiceURLProvider.tbtProvider.getNext();
            //host = "http://localhost:9002";
            String url = host + "/SWindows/doDelist?userId=" + userId + "&numIid=" + numIid + "&delistIndex="
                    + delistIndex;
            HttpResponse resp = WS.url(url).timeout(TIMEOUT_STR).get();
            /**
             * 是否有人接收到。。。
             */
            if (resp.getStatus() == Http.StatusCode.OK) {
                return true;
            }
        } while (count++ < 4);

        return false;
    }
}
