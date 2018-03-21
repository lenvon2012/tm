package controllers;

import java.util.HashSet;
import java.util.Set;

import models.topscoreid.TopScoreId;
import play.mvc.Controller;

import com.ciaosir.client.utils.JsonUtil;

public class TopScore extends Controller {

    static final int DEFAULT_PAGE_SIZE = 999;
    static final String RET_FORMAT = "%s(%s)";

    static Long[] forbiddenArr = new Long[] { 2934713L, 2934713L, 3114881L, 2575948L, 2679236L, 2827746L, 2709325L,
            2679018L, 2994098L, 2800574L, 3126235L, 3077597L, 3185795L, 3213129L, 3076679L, 3151614L, 2700517L,
            3198625L, 3184495L, 2780815L, 3120654L, 2730103L, 2735407L, 2907409L, 2650112L, 2837585L, 2740808L,
            2721555L, 2694367L, 2680166L, 2959613L, 2908569L, 2737478L, 2670793L, 2636543L, 2862514L, 2866988L,
            2850714L, 2849355L, 2679018L, 2944130L, 2772574L, 2719440L, 3125373L, 2727811L, 2700517L, 3041548L,
            3129996L, 2814862L, 2800574L, 2733520L, 2710207L, 2708542L, 2688899L, 3151104L, 3077597L, 2807365L,
            2802052L, 2669280L, 3216294L, 3155949L, 3152708L, 2932369L, 2834687L, 2806591L, 2660024L, 2646396L,
            3199802L, 3185795L, 3183900L, 3157933L, 2966238L, 2950036L, 2946838L, 2944649L, 2934795L, 2928749L,
            2918763L, 2903394L, 2857318L, 2821454L, 2815916L, 2719470L, 2711084L, 3186696L, 3151614L, 3150556L,
            3088648L, 3060101L, 3013066L, 2930385L, 2929802L, 2929711L, 2928407L, 2868754L, 2830066L, 2828861L,
            2825933L, 3217805L, 3217698L, 3215990L, 3214711L, 3048607L, 3207219L, 3158938L, 3150802L, 3048607L,
            2975482L, 2944364L, 2940612L, 2938133L, 2935812L, 2928934L, 2926012L, 2920636L, 2920390L, 2918937L,
            2914603L, 2717907L, 2643874L, 3218112L, 3217672L, 3217565L, 3211118L, 3014043L, 2881267L, 3153988L,
            2503368L, 2818945L, 2978181L, 2968836L, 2963749L, 2639780L, 2558351L, 2503986L, 2909463L, 2642016L,
            2855497L, 2773729L, 3193121L, 3038233L, 3036353L, 2790500L, 2882523L, 2777034L, 2963250L, 2958685L,
            2933864L, 2826155L, 2730898L, 2891194L, 2847513L, 2745422L, 2715161L, 3219944L, 2889983L, 2605354L,
            2963749L, 3193121L, 2619297L, 2512316L, 2765544L, 2933864L, 2516525L, 2714965L, 3014043L, 2889983L,
            3216294L, 3215990L, 3214711L, 3212813L, 3211118L, 3209610L, 3209363L, 3208414L, 3207701L, 3207252L,
            3204773L, 3201363L, 3200375L, 3199802L, 3199317L, 3197490L, 3196873L, 3196042L, 3194999L, 3192034L,
            3192001L, 3191628L, 3187081L, 3186696L, 3186321L, 3185795L, 3184838L, 3184118L, 3183900L, 3182208L };
    static HashSet<Long> forbidden = new HashSet<Long>();
    static {
        for (int i = 0; i < forbiddenArr.length; i++) {
            forbidden.add(forbiddenArr[i]);
        }
    }

    public static void sendGood(String serviceCode, String ids) {
        String[] idArr = ids.split(",");
        for (int i = 0; i < idArr.length; i++) {
            Long scoreId = Long.valueOf(idArr[i].trim());
            if (!forbidden.contains(scoreId)) {
                TopScoreId.saveOrUpdate(serviceCode, scoreId, true);
            }
        }
    }

    public static void sendOthersBad(String serviceCode, String ids) {
        String[] idArr = ids.split(",");
        for (int i = 0; i < idArr.length; i++) {
            Long scoreId = Long.valueOf(idArr[i].trim());
            if (!forbidden.contains(scoreId)) {
                TopScoreId.saveOrUpdate(serviceCode, scoreId, true);
            }
        }
    }

    public static void clickGood(int pn, int ps, String callback) {
        // if (StringUtils.isEmpty(callback)) {
        callback = "TM.click";
        // }
        if (ps <= 0) {
            ps = DEFAULT_PAGE_SIZE;
        }

        Set<Long> set = TopScoreId.findTopScoreIdLongList(true, pn, ps);
        Set<Long> ret = new HashSet<Long>();
        if (set != null) {
            for (Long id : set) {
                if (!forbidden.contains(id)) {
                    ret.add(id);
                }
            }
        }
        String json = JsonUtil.getJson(ret);
        renderJSON(String.format(RET_FORMAT, callback, json));
    }

    public static void clickOthersBad(int pn, int ps, String callback) {
        // if (StringUtils.isEmpty(callback)) {
        callback = "TM.click";
        // }
        if (ps <= 0) {
            ps = DEFAULT_PAGE_SIZE;
        }
        Set<Long> set = TopScoreId.findTopScoreIdLongList(false, pn, ps);
        Set<Long> ret = new HashSet<Long>();
        if (set != null) {
            for (Long id : set) {
                if (!forbidden.contains(id)) {
                    ret.add(id);
                }
            }
        }
        String json = JsonUtil.getJson(ret);
        renderJSON(String.format(RET_FORMAT, callback, json));
    }

    public static void clickTest(String serviceCode, String ids, String callback) {
        // if (StringUtils.isEmpty(callback)) {
        callback = "TM.click";
        // }
        String[] idArr = ids.split(",");
        for (int i = 0; i < idArr.length; i++) {
            Long scoreId = Long.valueOf(idArr[i].trim());
            TopScoreId.saveOrUpdate(serviceCode, scoreId, true);
        }
        String json = JsonUtil.getJson(idArr);
        renderJSON(String.format(RET_FORMAT, callback, json));
    }

}
