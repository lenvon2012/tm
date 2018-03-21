
package controllers;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;

import models.tmsearch.TmallSearchLog.TmallSearchType;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.cache.Cache;
import actions.catunion.UserRateSpiderAction;
import actions.catunion.UserRateSpiderAction.UserRateInfo;

import com.ciaosir.client.utils.JsonUtil;

/**
 * 天小猫
 * @author zrb
 *
 */
public class CatSearchComment extends CatUnionBase {

    private static final Logger log = LoggerFactory.getLogger(CatSearchComment.class);

    static String seotitle = "淘宝信誉查询_提供淘宝买家信用查询_卖家信用查询_淘宝小号查询 - 天小猫 卖家工具箱";

    public static void index() {
        String title = seotitle;
        String keywords = MetaKeywords;
        String description = MetaDescription;
        List<SearchNickInfo> nickInfoList = queryLatestNick(TmallSearchType.Comment);
        render("tmSearch/commentsearch.html", title, keywords, description, nickInfoList);
    }

    static void home() {
        checkVisitedTimesByIp();
//        String title = "淘宝信誉查询";
        String title = seotitle;
        String keywords = MetaKeywords;
        String description = MetaDescription;
        List<SearchNickInfo> nickInfoList = queryLatestNick(TmallSearchType.Comment);

        render("tmSearch/commentsearch.html", title, keywords, description, nickInfoList);
    }

    /**
     * TODO 这里需要准备一下，就是把用户的评价 放到缓存里面输出到页面上，以保证被搜索引擎索引住内容
     */
    public static void userComment(long userId) {
        String userNick = queryNickByUserId(TmallSearchType.Comment);
        if (StringUtils.isEmpty(userNick))
            renderText("");
        //log.error(userNick);
        String title = "" + userNick + " 信誉记录查询_淘宝信誉查询 - 天猫联盟";
        String keywords = userNick + " " + MetaKeywords;
        String description = "淘宝账号：" + userNick + " 淘宝信誉信息。 " + MetaDescription;
        List<CommentInfo> comments = buildSEOList(userId);
        List<SearchNickInfo> nickInfoList = queryLatestNick(TmallSearchType.Comment);
        render("tmSearch/commentsearch.html", title, keywords, description, userNick, nickInfoList, comments);
    }

    public static void doQueryUserId(String userNick) {
        userNick = trimUserNick(userNick);
        long userId = queryUserIdByNick(userNick);
        checkUserId(userId);
        renderResultJson(userId);
    }

    public static void doQueryUserCredit(long userId, String userNick) {
        checkUserId(userId);
        userNick = trimUserNick(userNick);

        UserRateInfo userRateInfo = UserRateSpiderAction.doSpiderUserRate(userId, userNick);

        if (userRateInfo == null) {
            saveSearchLog(TmallSearchType.Comment, userId, userNick, false);
            renderFailJson("网络出现一些异常，请重试一下！");
        }

        //log.error(JsonUtil.getJson(userRateInfo));
        saveSearchLog(TmallSearchType.Comment, userId, userNick, true);
        renderResultJson(userRateInfo);
    }

    public static void doQueryCommentList(Long userId, int pn, int ps, int commentType, boolean hasContent) {
        if (userId == null || userId <= 0) {
            renderFailJson("系统出现一些异常，请稍后重试");
        }

        if (pn <= 0) {
            pn = 1;
        }

        String json = fetctchCommentJSON(userId, pn, ps, commentType, hasContent);

        if (StringUtils.isEmpty(json)) {
            renderFailJson("网络出现一些异常，请重试一下！");
        }

        //这里有个账号，toObject会出错，是clorest510????
        Object obj = JsonUtil.toObject(json, Object.class);

        if (obj == null)
            renderFailJson("系统出现一些异常，请稍后重试！");
        renderResultJson(obj);
    }

    static List<CommentInfo> buildSEOList(Long userId) {
        if (Play.mode.isDev()) {
            return ListUtils.EMPTY_LIST;
        }

        String res = fetctchCommentJSON(userId, 1, 1, 1, true);
        JsonNode node = JsonUtil.readJsonResult(res);
        List<JsonNode> titles = node.findValues("title");
        List<JsonNode> contents = node.findValues("content");
        int size = Math.min(titles.size(), contents.size());
        List<CommentInfo> finalRes = new ArrayList<CommentInfo>();
        for (int i = 0; i < size; i++) {
            finalRes.add(new CommentInfo(titles.get(i).getTextValue(), contents.get(i).getTextValue()));
        }
        return finalRes;
    }

    @JsonAutoDetect
    static class CommentInfo {
        @JsonProperty
        String title;

        @JsonProperty
        String content;

        public CommentInfo(String title, String content) {
            super();
            this.title = title;
            this.content = content;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

    }

    private static String fetctchCommentJSON(Long userId, int pn, int ps, int commentType, boolean hasContent) {
        String cacheKey = String.format("doQueryCommentList%d%d%d%ds", userId, pn, ps, commentType,
                String.valueOf(hasContent));
        String json = (String) Cache.get(cacheKey);
        if (json == null) {
            json = UserRateSpiderAction.doSpiderCommentList(userId, StringUtils.EMPTY, pn, commentType, hasContent);
            Cache.set(cacheKey, json);
        }
        return json;
    }

    /*public static void main(String[] args) {
    doQueryComment("柠檬绿茶");
    doQueryComment("tb_676700");
    doQueryComment("tb676700");
    }*/

    public static void xinyuwangwang(String wangwang) {
        log.info(format("xinyuwangwang:wangwang".replaceAll(", ", "=%s, ") + "=%s", wangwang));
        wangwang = StringUtils.trim(wangwang);
        long userId = queryUserIdByNick(wangwang);
        xinyuid(userId, wangwang);
    }

    public static void xinyubuyerid(long userId) {
        xinyuid(userId, null);
    }

    static void xinyuid(long userId, String userNick) {
        String title = seotitle;
        String keywords = MetaKeywords;
        String description = MetaDescription;

        if (userId <= 0L) {
            render("tmsubway/subwayxinyu.html", title, keywords, description, userNick);
        }
        UserRateInfo info = UserRateSpiderAction.spiderUserRateById(userId);
        if (info == null) {
            render("tmsubway/subwayxinyu.html", title, keywords, description, userNick);
        }

        userNick = info.getUserNick();
        saveSearchLog(TmallSearchType.Comment, userId, userNick, true);
        title = "" + userNick + " 信誉记录查询_淘宝信誉查询 - 天猫联盟";
        keywords = userNick + " " + MetaKeywords;
        description = "淘宝账号：" + userNick + " 淘宝信誉信息。 " + MetaDescription;
        List<CommentInfo> comments = buildSEOList(userId);
        List<SearchNickInfo> nickInfoList = queryLatestNick(TmallSearchType.Comment);
        render("tmsubway/subwayxinyu.html", title, keywords, description, userNick,info, nickInfoList, comments);
    }
}
