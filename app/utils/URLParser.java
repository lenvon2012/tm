package utils;

import org.apache.commons.lang.StringUtils;

public class URLParser {

    public static boolean isFromWWAd(String url) {
        if (StringUtils.isEmpty(url)) {
            return false;
        }

        if (url.contains("&ali_trackid=10")) {
            return true;
        }

        return false;
    }

    public static boolean isFromHardPromotiohn(String url) {
        if (StringUtils.isEmpty(url)) {
            return false;
        }

        if (url.contains("&ali_trackid=9") || url.contains("&ali_trackid=12")) {
            return true;
        }

        return false;
    }

    public static boolean isFromStone(String url) {
        if (StringUtils.isEmpty(url)) {
            return false;
        }

        if (url.contains("&ali_trackid=3")) {
            return true;
        }

        return false;
    }

    public static boolean isFromTaobaoKe(String url) {
        if (StringUtils.isEmpty(url)) {
            return false;
        }

        if (url.contains("&ali_trackid=2:mm") || url.contains("&ali_trackid=2_")) {
            return true;
        }

        return false;
    }

    public static boolean isFromSimba(String url) {
        if (StringUtils.isEmpty(url)) {
            return false;
        }

        if (url.contains("&ali_trackid=1")) {
            return true;
        }

        return false;
    }

    public static final boolean isTaobaoItemUrl(String src) {
        if (StringUtils.isEmpty(src)) {
            return false;
        }
        return src.startsWith("item.taobao.com") || src.startsWith("detail.tmall.com")
                || src.startsWith("meal.taobao.com") || src.startsWith("item.tmall.com")
                || src.startsWith("detail.taobao.com") || src.startsWith("meal.tmall.com");

    }

    public static final String findItemIdString(String url) {
        if (StringUtils.isEmpty(url)) {
            return null;
        }
        if (url.indexOf('?') < 0 || !isTaobaoItemUrl(url)) {
            return null;
        }

        int idIndex = url.indexOf("?id=");
        if(idIndex < 0) {
        	idIndex = url.indexOf("&id=");
        }
        int offset = 4;
        if (idIndex < 0) {
            idIndex = url.indexOf("default_item_id=");
            if (idIndex < 0) {
                idIndex = url.indexOf("item_num_id=");
                if (idIndex < 0) {
                    return null;
                } else {
                    offset = 12;
                }
            } else {
                offset = 16;
            }
        }

        int nextStop = url.indexOf('&', idIndex + 1);
        if (nextStop < 0) {
            nextStop = url.length();
        }

        return url.substring(idIndex + offset, nextStop);
    }

    public static final long findItemId(String url) {
        String itemIdString = findItemIdString(url);
        if (StringUtils.isEmpty(itemIdString)) {
            return -1L;
        }

        try {
            return Long.valueOf(itemIdString);
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

    public static final boolean isItemId(String url) {
        return findItemId(url) > 0L;
    }

    public static String findBusClickKey(String url) {
        String keywords = null;
        if (url.contains("&ali_trackid=10")) {
            String trackid = url.substring(url.indexOf("&ali_trackid=10") + "&ali_trackid=10".length());
            String[] parts = trackid.split(":");
            if (parts.length >= 4) {
                keywords = parts[3];
            }
        }
        return keywords;
    }
}
