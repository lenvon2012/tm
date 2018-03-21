
package controllers;

import java.util.ArrayList;
import java.util.List;

import models.tmsearch.TmallSearchLog;
import models.tmsearch.TmallSearchLog.SearchStatus;
import models.tmsearch.TmallSearchLog.TmallSearchType;
import models.user.UserIdNick;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.mvc.After;
import play.mvc.Before;
import play.mvc.Controller;
import result.TMResult;
import actions.catunion.IPCacheAction;
import actions.catunion.UserIdNickAction;
import actions.catunion.VisitedCacheAction;
import actions.catunion.VisitedCacheAction.UserCacheInfo;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.NetworkUtil;
import com.ciaosir.client.utils.NumberUtil;
import com.dbt.cred.utils.JsonUtil;

public abstract class CatUnionBase extends Controller {

    private static final Logger log = LoggerFactory.getLogger(CatUnionBase.class);

    private static final String RequestTsKey = "_ts";

    private static final String RequestIpKey = "_ip";

    protected static final String MetaKeywords = "天小猫,淘宝信誉查询,买家信誉查询,淘宝,淘宝工具,淘宝卖家工具,宝贝降权,宝贝隐形降权查询,降权查询,淘宝宝贝降权,店铺宝贝降权,销量查询,淘宝销量查询,淘宝店铺,店铺销量查询,卖家销量查询";

    protected static final String MetaDescription = "天小猫_淘宝卖家中心,免费提供买家信誉、宝贝隐形降权和店铺销量等查询功能，是淘宝卖家必备的工具箱。";

    @Before
    public static void startTime() {
        log.info("Request For " + request.url + ":" + request.action + " Starts");
        request.args.put(RequestTsKey, System.currentTimeMillis());
    }

    @Before
    public static void checkVisitedTimesByIp() {
        boolean success = true;
        try {
            String ip = getIp();
            if (StringUtils.isEmpty(ip)) {
                success = true;
            } else {
                success = IPCacheAction.judgeIp(ip);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

        if (!success) {
            renderFailJson("too many..");
        }
    }

    @After
    public static void endTime() {
        log.info("Action [" + request.url + "] took "
                + (System.currentTimeMillis() - (Long) request.current().args.get(RequestTsKey)) + " ms");
    }

    protected static void saveSearchLog(int searchType, long userId, String nick, boolean success) {
        try {
            Long startTs = (Long) request.current().args.get(RequestTsKey);
            if (startTs == null)
                startTs = 0L;
            long usedTime = System.currentTimeMillis() - startTs;

            int searchStatus = 0;
            if (success == true)
                searchStatus = SearchStatus.Success;
            else
                searchStatus = SearchStatus.Error;
            new TmallSearchLog(getIp(), searchType, startTs, usedTime, nick, searchStatus).jdbcSave();

            //放入到cache
            if (success == true && userId > 0)
                VisitedCacheAction.addToCache(userId, nick, searchType);

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    protected static String getIp() {
        try {
            String ip = (String) request.current().args.get(RequestIpKey);
            if (StringUtils.isEmpty(ip)) {
                ip = NetworkUtil.getRemoteIPForNginx(request);
                request.args.put(RequestIpKey, ip);
            }
            if (StringUtils.isEmpty(ip)) {
                log.error("ip is null");
                return "";
            } else {
                return ip;
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return "";
        }
    }

    private static void checkUserNick(String nick) {
        if (StringUtils.isEmpty(nick)) {
            renderFailJson("请先输入要查询的卖家账号");
        }

    }

    protected static String trimUserNick(String nick) {
        checkUserNick(nick);
        nick = nick.trim();
        checkUserNick(nick);
        return nick;
    }

    protected static void checkUserId(long userId) {
        if (userId <= 0) {
            renderFailJson("找不到该账号的卖家");
        }
    }

    protected static long queryUserIdByNick(String userNick) {
        userNick = trimUserNick(userNick);
        long userId = UserIdNickAction.findUserIdByNick(userNick);
        checkUserId(userId);

        return userId;
    }

    protected static void renderFailJson(String message) {
        TMResult tmRes = new TMResult(false, message, null);
        renderJSON(JsonUtil.getJson(tmRes));
    }

    protected static void renderSuccessJson() {
        TMResult tmRes = new TMResult(true, "", null);

        renderJSON(JsonUtil.getJson(tmRes));
    }

    protected static void renderResultJson(Object result) {
        TMResult tmRes = new TMResult(true, "", result);

        renderJSON(JsonUtil.getJson(tmRes));
    }

    protected static void renderResultJson(Object result, String message) {
        TMResult tmRes = new TMResult(true, message, result);

        renderJSON(JsonUtil.getJson(tmRes));
    }

    public static class SearchNickInfo {

        private long userId;

        private String nick;

        private String href;

        public String getNick() {
            return nick;
        }

        public void setNick(String nick) {
            this.nick = nick;
        }

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public long getUserId() {
            return userId;
        }

        public void setUserId(long userId) {
            this.userId = userId;
        }

    }

    private static String getUrlPrefix(int searchType) {
        String prefix = "";
        if (searchType == TmallSearchType.Comment)
            prefix = "comment-";
        else if (searchType == TmallSearchType.Authority)
            prefix = "authority-";
        else if (searchType == TmallSearchType.ShopSales)
            prefix = "sales-";
        else
            renderText("系统出现异常");

        return prefix;
    }

    /*protected static List<SearchNickInfo> queryLatestNick(int searchType) {
        List<SearchNickInfo> nickInfoList = new ArrayList<SearchNickInfo>();
        
        List<String> nickList = TmallSearchLog.queryLatestSearchedNick(searchType);
        //log.error(nickList.size() + " -------------");
        String prefix = getUrlPrefix(searchType);
        int maxCount = 10;
        int addedCount = 0;
        List<UserIdNick> userIdNickList = UserIdNick.findByNickList(nickList);
        for (String nick : nickList) {
            if (addedCount >= maxCount)
                break;
            //不用这种方式，而是从数据库查询，忽略数据库中不存在的nick了
            //long userId = UserIdNickAction.findUserIdByNick(nick);
            long userId = 0;
            for (UserIdNick userIdNick : userIdNickList) {
                if (userIdNick == null)
                    continue;
                if (userIdNick.getNick().equals(nick)) {
                    userId = userIdNick.getId();
                    break;
                }
            }
            
            if (userId <= 0)
                continue;
            else {
                SearchNickInfo nickInfo = new SearchNickInfo();
                nickInfo.nick = nick;
                nickInfo.href = "/" + prefix + userId + ".html";
                nickInfoList.add(nickInfo);
                addedCount++;
            }
        }
        
        return nickInfoList;
    }*/

    protected static List<SearchNickInfo> queryLatestNick(int searchType) {
        List<SearchNickInfo> nickInfoList = new ArrayList<SearchNickInfo>();

        List<UserCacheInfo> visitedList = VisitedCacheAction.getVisitedCache(searchType);
        if (CommonUtils.isEmpty(visitedList)) {
            //从数据库取
            List<String> nickList = TmallSearchLog.queryLatestSearchedNick(searchType);
            nickList.remove("");
            log.error(nickList.size() + " -------------");

            int maxCount = VisitedCacheAction.MaxQueueSize;
            int addedCount = 0;
            List<UserIdNick> userIdNickList = UserIdNick.findByNickList(nickList);
            for (String nick : nickList) {
                if (StringUtils.isEmpty(nick))
                    continue;
                if (addedCount >= maxCount)
                    break;
                //不用这种方式，而是从数据库查询，忽略数据库中不存在的nick了
                //long userId = UserIdNickAction.findUserIdByNick(nick);
                long userId = 0;
                for (UserIdNick userIdNick : userIdNickList) {
                    if (userIdNick == null)
                        continue;
                    if (nick.equals(userIdNick.getNick())) {
                        userId = userIdNick.getId();
                        break;
                    }
                }

                if (userId <= 0)
                    continue;
                else {
                    VisitedCacheAction.addToCache(userId, nick, searchType);
                }
            }
        }

        visitedList = VisitedCacheAction.getVisitedCache(searchType);
        if (CommonUtils.isEmpty(visitedList)) {
            return nickInfoList;
        }
        String prefix = getUrlPrefix(searchType);
        for (UserCacheInfo userInfo : visitedList) {
            if (userInfo == null)
                continue;
            if (userInfo.getUserId() <= 0 || StringUtils.isEmpty(userInfo.getNick()))
                continue;
            SearchNickInfo nickInfo = new SearchNickInfo();
            nickInfo.userId = userInfo.getUserId();
            nickInfo.nick = userInfo.getNick();
            nickInfo.href = "/" + prefix + userInfo.getUserId() + ".html";
            nickInfoList.add(nickInfo);
        }

        return nickInfoList;
    }

    protected static String queryNickByUserId(int searchType) {
        try {
            String url = request.path;
            log.error(url);
            if (StringUtils.isEmpty(url)) {
                renderText("系统出现异常");
            }
            String prefix = getUrlPrefix(searchType);

            int start = url.lastIndexOf(prefix);
            if (start < 0)
                renderText("系统出现异常");
            int end = url.lastIndexOf(".html");
            if (end < 0 || end <= start + prefix.length())
                renderText("系统出现异常");
            String idStr = url.substring(start + prefix.length(), end);
            if (StringUtils.isEmpty(idStr))
                renderText("");
            long userId = NumberUtil.parserLong(idStr, 0L);
            if (userId <= 0)
                renderText(idStr + "不是有效的淘宝id");

            String userNick = UserIdNickAction.findNickById(userId);
            if (StringUtils.isEmpty(userNick)) {
                renderText(idStr + "不是有效的淘宝id");
            }
            return userNick;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            renderText("");
        }
        return "";
    }
}
