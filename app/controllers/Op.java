
package controllers;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import models.op.AdBanner;
import models.op.CommentDeal;
import models.op.MixPlanRecommend;
import models.op.RecommendFeedBack;
import models.op.TaozhangguiFeedBack;
import models.op.TraceLogInvite;
import models.op.UserInvite;
import models.oplog.TMErrorLog;
import models.user.User;
import models.vas.ArticleBizOrderPlay;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.CacheFor;
import play.jobs.Job;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;
import play.mvc.Http.Cookie;
import play.mvc.Http.Request;
import result.TMResult.TMListResult;
import bustbapi.ItemApi;
import bustbapi.VasApis;

import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.MapIterator;
import com.ciaosir.client.utils.NetworkUtil;
import com.ciaosir.client.utils.NumberUtil;
import com.taobao.api.domain.ArticleSub;

import configs.TMConfigs;
import dao.vas.ArticleBizOrderDao;

public class Op extends TMController {

    static final Logger log = LoggerFactory.getLogger(Op.class);

    public static final String TAG = "Op";

    @JsonAutoDetect
    public static class InviteInfo {
        @JsonProperty
        String textInput;

        @JsonProperty
        String url;

        public InviteInfo() {
            super();
        }

        public InviteInfo(String textInput, String url) {
            super();
            this.textInput = textInput;
            this.url = url;
        }

        public String getTextInput() {
            return textInput;
        }

        public void setTextInput(String textInput) {
            this.textInput = textInput;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

    }

    static String INVITE_COOKIE_FROMUID = "_i";

    static String INVITE_COOKIE_TRACELOG = "_l";

    static String INVITE_COOKIE_FROMUNAME = "_in";

    static String INVITE_COOKIE_CREATED = "_its";

    public static void genInviteUrl() {
        User user = getUser();
        InviteInfo info = new InviteInfo();
        log.info("[user : ]" + user);
        String url = "http://" + request.host + "/f/" + user.getId();
        info.setUrl(url);
        renderJSON(JsonUtil.getJson(info));
    }

    /**
     * 验证用户是否是被人邀请，是返回邀请用户id；不是返回0。controllers包里可用
     * @param req
     * @return 邀请用户id，不是邀请的返回0
     */
    static long checkInvite() {
        Request req = request;
        Cookie fromUidc = req.cookies.get(INVITE_COOKIE_FROMUID);
        Cookie createdc = req.cookies.get(INVITE_COOKIE_CREATED);
        Cookie tracelogc = req.cookies.get(INVITE_COOKIE_TRACELOG);

        if (createdc == null) {
            return 0L;
        }


        
        
        final User user = getUser();
        if (user == null) {
            return 0L;
        }
        final String ip = NetworkUtil.getRemoteIPForNginx(request);
        log.info(" from uid:" + fromUidc + "  -- created dc :" + createdc + " --  tradelog :" + tracelogc);
        log.info(" invite ip :" + ip + " for user :" + user);

        final long ts = NumberUtil.parserLong(createdc.value, 0L);
        if (user.getFirstLoginTime() < ts) {
            String content = " user " + user + "has logined bofore :" + ts;
            log.error(content);
            new TMErrorLog(content).save();
            return 0L;
        }

        checkTraceLog(tracelogc, ip, user);

        if (fromUidc == null) {
            return 0L;
        }
        final long srcUid = Long.parseLong(fromUidc.value);
        if (srcUid <= 0L) {
            log.error(" no src uid :" + srcUid);
            return 0L;
        }

//        MixHelpers.infoAll(request, response);

        if (user.getId().longValue() == srcUid) {
            String content = " user " + user + "has logined bofore :" + ts + " with srcUid :" + srcUid;
            log.error(content);
            new TMErrorLog(content).save();
            return 0L;
        }

        new Job() {
            public void doJob() {
                UserInvite.ensure(user, srcUid, ip);
            }
        }.now();

        return srcUid;
    }

    private static void checkTraceLog(Cookie tracelogc, final String ip, final User user) {
        if (tracelogc == null) {
            return;
        }
        try {
            final String tracelogvalue = tracelogc.value;
            log.info(" check trace log:" + tracelogc.value);

            new Job() {
                public void doJob() {
                    TraceLogInvite.ensure(user, tracelogvalue, ip);
                }
            }.now();
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    /**
     * list invites...
     * @param pn
     * @param ps
     */
    public static void li(int pn, int ps) {
        PageOffset po = new PageOffset(pn, ps);
        User user = getUser();
        TMListResult res = UserInvite.findBySrcUid(user.getId(), po);
        renderJSON(JsonUtil.getJson(res));
    }

    @CacheFor("5min")
    public static void adbanner(String name) {

        log.info(format("adbanner:name".replaceAll(", ", "=%s, ") + "=%s", name));
        AdBanner banner = AdBanner.findbyName(name);
        log.info("banner :" + banner);
        renderJSON(JsonUtil.getJson(banner));
    }

    public static void commitRecommenFeedBack(RecommendFeedBack feed) {
        User user = getUser();
        feed.setUserId(user.getId());
        log.info("[param :]" + feed);
        feed.save();
    }

    public static void commitTaozhangguiFeedBack(String feed) {
        if (feed == null || feed.isEmpty()) {
            renderText("意见内容出错");
        }
        User user = getUser();
        new TaozhangguiFeedBack(user.getId(), feed).save();
        renderText("您的意见已接受，淘掌柜用心为您服务");
    }

    public static void tracelogmoney(String start, String end) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = df.parse(start);
        Date endDate = df.parse(end);

        Map<String, Long> map = TraceLogInvite.durationMap(startDate.getTime(), endDate.getTime());
        final StringBuilder sb = appendMap(map, false);
        renderText(sb.toString());
    }

    public static void invitemoney(String start, String end) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = df.parse(start);
        Date endDate = df.parse(end);

//        Map<String>
        Map<String, Long> map = new HashMap<String, Long>();

        List<UserInvite> find = UserInvite.find("created >= ? and created  <= ?", startDate.getTime(),
                endDate.getTime()).fetch();
        for (UserInvite userInvite : find) {
            String srcNick = userInvite.getSrcUserNick();
            if (StringUtils.isEmpty(srcNick)) {
                continue;
            }
            map.put(srcNick, 0L);
        }

        for (UserInvite userInvite : find) {
            //List<ArticleBizOrderPlay> list = ArticleBizOrderPlay.find("nick = ? ", userInvite.getNick()).fetch();
            List<ArticleBizOrderPlay> list = ArticleBizOrderPlay.nativeQuery("nick = ? ", userInvite.getNick());
            String srcNick = userInvite.getSrcUserNick();
            if (StringUtils.isEmpty(srcNick)) {
                continue;
            }
            String thisNick = userInvite.getNick();
            if (refundNames.contains(thisNick)) {
                continue;
            }
            log.info("[nick : ]" + srcNick);
            for (ArticleBizOrderPlay instance : list) {
                if (instance.getRefundFee() > 0L) {
                    Long curr = map.get(srcNick);
                    log.info("[curr : ]" + curr);
                    map.put(srcNick, curr - instance.getRefundFee());
                } else {
                    Long curr = map.get(srcNick);
                    log.info("[curr : ]" + curr);
                    map.put(srcNick, curr + instance.getTotalPayFee());
                }
            }
        }

        final StringBuilder sb = appendMap(map, true);

        log.info("[map]" + map);

        Collection<Long> res = map.values();
        int sum = 0;
        for (Long r : res) {
            sum += r.intValue();
        }
        log.error("sum :" + sum);
        renderText(sb.toString());

    }

    private static StringBuilder appendMap(Map<String, Long> map, final boolean toDouble) {
        final StringBuilder sb = new StringBuilder();
        new MapIterator<String, Long>(map) {
            @Override
            public void execute(Entry<String, Long> entry) {
                sb.append(entry.getKey());
                sb.append("\t");
                if (toDouble) {
                    sb.append(entry.getValue().doubleValue() * 0.3d / 100d);
                } else {
                    sb.append(entry.getValue());
                }
                sb.append("\n");
            }
        }.call();
        return sb;
    }

    static Set<String> refundNames = new HashSet<String>();
    static {
        try {
            List<String> lines = org.apache.commons.io.FileUtils.readLines(new File(TMConfigs.mockDir,
                    "refundnicks.txt"));
            for (String string : lines) {
                if (StringUtils.isBlank(string)) {
                    continue;
                }
                refundNames.add(string);
            }
        } catch (IOException e) {
            log.warn(e.getMessage(), e);

        }
    }

    public static void mixplans(String tag) throws IOException {
//        renderMockFileInJsonIfDev("op.mixplans.json");
        renderJSON(JsonUtil.getJson(MixPlanRecommend.findAllTagShown(APIConfig.get().getName())));
    }

    public static void artile(String wangwang) {
        wangwang = StringUtils.trim(wangwang);
        List<ArticleSub> subs = new VasApis.SubscSearch(APIConfig.get().getSubCode(), wangwang).call();
        renderJSON(subs);

    }

    public static void getArticleVasOrder(String wangwang) {
        wangwang = StringUtils.trim(wangwang);
        //new ArticleBizOrderPlay().jdbcSave();
        List<ArticleBizOrderPlay> subs = ArticleBizOrderDao.findVasOrderByNick(wangwang);
        renderJSON(subs);
    }

    public static void fillQQ(String qq) {
        User user = getUser();
        new CommentDeal(user.getId(), user.getUserNick(), qq).save();
        renderText("已成功提交，我们会尽快联系您！");
    }

    /**
     * {"tinyurl":"http:\/\/dwz.cn\/aR3ou","status":0,"longurl":"http://item.taobao.com/item.htm?id=123456","err_msg":""}
     * @param numIid
     */
    public static void tbShortUrl(long numIid) {
        String url = null;
        if (numIid <= 0L) {
            User user = getUser();
            url = "http://store.taobao.com/shop/view_shop.htm?user_number_id=" + user.getId();
        } else {
            url = "http://item.taobao.com/item.htm?id=" + numIid;
        }

        String target = "http://dwz.cn/create.php";
        WSRequest req = WS.url(target);
        HttpResponse resp = req.setParameter("url", url).post();
        String respString = resp.getString();
        log.warn("back result :" + respString);
        renderJSON(respString);
    }

    public static void tbShopShortUrl() {
        String url = null;
        User user = getUser();
        url = "http://store.taobao.com/shop/view_shop.htm?user_number_id=" + user.getId();

        String target = "http://dwz.cn/create.php";
        WSRequest req = WS.url(target);
        HttpResponse resp = req.setParameter("url", url).post();
        String respString = resp.getString();
        log.warn("back result :" + respString);
        renderJSON(respString);
    }

    public static void getUserItemsCount() {
        User user = getUser();
        long onsaleCount = new ItemApi.ItemsOnsaleCount(user.getSessionKey(), null, null).call();
        long inventoryCount = new ItemApi.ItemsInventoryCount(user, null, null).call();
        long totalCount = onsaleCount + inventoryCount;
        renderSuccessJson("" + totalCount);
    }
}
