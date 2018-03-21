
package controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.item.ItemPlay;
import models.promotion.TMProActivity;
import models.promotion.UserTag;
import models.promotion.VipConfig;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.mvc.Before;
import result.TMPaginger;
import result.TMResult;
import transaction.JDBCBuilder;
import utils.TaobaoUtil;
import actions.promotion.PromotionAction;
import bustbapi.TBApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.JsonUtil;
import com.taobao.api.ApiException;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.MarketingTagAddRequest;
import com.taobao.api.request.MarketingTagDeleteRequest;
import com.taobao.api.request.MarketingTagsGetRequest;
import com.taobao.api.response.MarketingTagAddResponse;
import com.taobao.api.response.MarketingTagDeleteResponse;

import configs.TMConfigs;
import configs.TMConfigs.PageSize;
import configs.TMConfigs.WebParams;
import dao.UserDao;
import dao.item.ItemDao;
import dao.popularized.PopularizedDao.PopularizedStatusSqlUtil;

/**
 * https://oauth.taobao.com/authorize?response_type=code&client_id=12266732&redirect_uri=http://223.4.51.164/in/login
 * @author zrb
 *
 */
public class JinNangZheKou extends TMController {
    private static final Logger log = LoggerFactory.getLogger(JinNangZheKou.class);

    public static final String TAG = "JinNangZheKou";

    public static final String DOMAIN = "http://huanjia.fanfanle.com/";

    @Before
    public static void logAppKey() {
        log.info("appkey=" + TMConfigs.App.APP_KEY + " " + "appsecret="
                + TMConfigs.App.APP_SECRET);
    }

    public static void index(String sid, String token) {
        TaobaoUtil.refreshToken(getUser());
        render("jinnang/index.html");
    }

    public static void debug(String sessionKey) {
        //User user = User.find("bySessionKey", sessionKey).first();
        User user = UserDao.findBySessionKey(sessionKey);
        if (user == null) {
            log.error("find null user");
        } else {
            log.error("find user with session key:" + user.sessionKey
                    + ",refresh token:" + user.getRefreshToken());
            UserDao.refreshToken(user);
            String newSessionKey = user.sessionKey;
            log.error("new session key:" + user.sessionKey);
            //user = User.find("bySessionKey", user.sessionKey).first();
            user = UserDao.findBySessionKey(sessionKey);
            if (user == null) {
                log.error("new user is null");
                //List<User> users = User.findAll();
                List<User> users = UserDao.fetchAllUser();
                for (User item : users) {
                    log.error("user session=[" + item.sessionKey + "]");

                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                log.error("find user by sessison newSessionKey="
                        + newSessionKey);
                log.error("user is "
                        + UserDao.findBySessionKey(sessionKey));
            } else {
                log.error("new user info:sid=" + user.sessionKey
                        + ",refresh token=" + user.getRefreshToken());
            }
        }

        /*
         * List<UserTag> userList = UserTag.findAll(); for (UserTag userTag :
         * userList) { System.out.println(userTag.id); Set<String> users =
         * userTag.getUsers(); String userListStr = StringUtils.join(users,
         * ","); System.out.println(userListStr); }
         */
        render("jinnang/debug.html");
    }

    /*public static void debug2() {
        log.error("debug user:"
                + User.find("bySessionKey",
                        "6101407c6db70801f92eef77ea00b8df375f841ad50ac831039626382")
                        .first());
    }*/

    /*public static void debug3() {
        User user = User.findById(1039626382L);
        log.error("find user by id " + user + ",refresh Token is "
                + user.getRefreshToken());
        User user2 = User.find("bySessionKey",
                "6101407c6db70801f92eef77ea00b8df375f841ad50ac831039626382")
                .first();
        log.error("find user by sessionKey " + user2 + ",refresh Token is "
                + user.getRefreshToken());
    }*/

    public static void removeAllPromotion() {

    }

    public static void re_auth() {

    }

    public static void test_oauth() {
        String appkey = TMConfigs.App.APP_KEY;
        String appSecret = TMConfigs.App.APP_SECRET;
        String client_id = appkey;
        String response_type = "code";
        String scope = "item";
        String state = "local_info";
        String view = "web";
        String redirect_uri = "http://huanjia.fanfanle.com/JinNangZheKou/test_oauth_return";// 应用回调地址
        String url = "https://oauth.taobao.com/authorize?";// code获取地址
        url += "response_type=" + response_type + "&client_id=" + client_id
                + "&redirect_uri=" + redirect_uri + "&scope=" + scope
                + "&view=" + view + "&state=" + state;
        redirect(url);
    }

    public static void test_oauth_return() {
        try {
            log.info(request.url);
            log.info("passed back local params:"
                    + request.params.current().get("state"));
            String code = (String) request.args.get("code");
            // String code = request.getParameter("code");
            if (code != null && !code.equals("")) {
                String appkey = TMConfigs.App.APP_KEY;
                String client_id = appkey;
                String client_secret = TMConfigs.App.APP_SECRET;
                String grant_type = "authorization_code";
                String redirect_uri = "http://huanjia.fanfanle.com/JinNangZheKou/index";// 应用回调地址
                String scope = "item";
                String state = "";
                String view = "web";
                // 用post方法
                Map param = new HashMap();
                String tbPostSessionUrl = "https://oauth.taobao.com/token";// access_token获取地址
                param.put("grant_type", grant_type);
                param.put("code", code);
                param.put("client_id", client_id);
                param.put("client_secret", client_secret);
                param.put("redirect_uri", redirect_uri);
                param.put("scope", scope);
                param.put("view", view);
                param.put("state", state);
                String responseJson;
                responseJson = com.taobao.api.internal.util.WebUtils.doPost(
                        tbPostSessionUrl, param, 3000, 3000);
                log.warn(responseJson);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void vip_mnt() {
        List<VipConfig> vipConfigList = VipConfig.findAll();
        if (vipConfigList.size() == 0) {
            render("jinnang/vip_mnt.html");
        } else {
            VipConfig vipConfig = vipConfigList.get(0);
            render("jinnang/vip_mnt.html", vipConfig);
        }
    }

    public static void vip_edit() {
        render("jinnang/vip_edit.html");
    }

    // 帮助
    public static void help() {
        render("jinnang/help.html");
    }

    // 完成任务后送xx服务说明
    public static void reward() {
        render("jinnang/reward.html");
    }

    // 推荐服务
    public static void recomment_serv() {
        render("jinnang/recommend_serv.html");
    }

    // 添加活动
    public static void activity_add(Long activityId) {
        User user = getUser();

        JsonNode refreshObj = UserDao.refreshTokenAndGetJson(user);
        session.put(WebParams.SESSION_USER_KEY,
                String.valueOf(user.getSessionKey()));

        // TODO should change url back after testing
//        String state = "http://localhost:9000/" + "JinNangZheKou/activity_add";
//
//        if (StringUtils.isEmpty(user.getRefreshToken())) {System.out.println("ddddddddddddddddddd");
//            In.forwardToOAuth(state, DOMAIN + "In/reAuthCallback");
//            return;
//        }
//        Long w2_expires_in = Long.parseLong(refreshObj.get("w2_expires_in")
//                .getTextValue());
//
//        if (w2_expires_in == null || w2_expires_in < 300) {
//            In.forwardToOAuth(state, DOMAIN + "In/reAuthCallback");
//            return;
//        }

        List<UserTag> userTags = UserTag.findAll();
        TMProActivity activity = null;
        if (activityId != null) {
            //activity = Activity.findById(activityId);
            activity = TMProActivity.findByActivityId(user.getId(), activityId);
        }

        render("jinnang/activity_add.html", userTags, activity);
    }

    // 活动方案
    public static void activity_mnt(Integer page, Integer cat) {
        Integer limit = 20;
        if (cat == null) {
            cat = 0;
        }
        if (page == null) {
            page = 1;
        }
        Integer start = (page - 1) * limit;
        //List<Activity> activities = Activity.find("order by id desc")
        //List<TMProActivity> activities = TMProActivity.nativeQuery("order by id desc limit ?,?", start, limit);
        //render("jinnang/activity_list.html", activities);
    }

    // 用户标签
    public static void user_tag_mnt(Long userTagId) {
        if (userTagId == null) {
            render("jinnang/user_tags_mnt.html");
        } else {
            UserTag userTag = UserTag.findById(userTagId);
            render("jinnang/user_tags_mnt.html", userTag);
        }
    }

    public static void user_tag_list() {
        List<UserTag> userTags = UserTag.findAll();
        render("jinnang/tag_lists.html", userTags);
    }

    // 营销计划
    public static void promotion_list() {
        render("jinnang/promotion_mnt.html");
    }

    // 报表
    public static void report() {
        render("jinnang/report.html");
    }

    // 升级
    public static void update() {

    }

    // 推荐
    public static void recommend() {

    }

    // 宝贝营销状态
    public static void items_list() {
        render("jinnang/items_list.html");
    }

    public static void notice() {
        render("jinnang/notice.html");
    }

    public static void list_items() {
        List<ItemPlay> items = ItemPlay.findAll();
        System.out.println("total items:" + items.size());
        for (ItemPlay item : items) {
            System.out.println(item.getNumIid());
        }
    }

    public static void test_promotion_tag_delete() {
        TaobaoClient client = TBApi.genClient();
        MarketingTagDeleteRequest req = new MarketingTagDeleteRequest();
        req.setTagId(1489014L);
        try {
            MarketingTagDeleteResponse response = client
                    .execute(req,
                            "610152690e3cc364450a28c19cd2735ec415ff2dbd4b8b63594267752");
            System.out.println("result:" + response.getBody());
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    public static void test_promotion_tags_get() {
        TaobaoClient client = TBApi.genClient();
        MarketingTagsGetRequest req = new MarketingTagsGetRequest();
        req.setFields("tag_id,tag_name");
        try {
            // MarketingTagsGetResponse response =
            // client.execute(req,"610152690e3cc364450a28c19cd2735ec415ff2dbd4b8b63594267752");
            System.out
                    .println("result:"
                            + client.execute(req,
                                    "610152690e3cc364450a28c19cd2735ec415ff2dbd4b8b63594267752")
                                    .getBody());
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    public static void test_promotion() {

        // taobao.marketing.tag.add
        TaobaoClient client = TBApi.genClient();
        System.out.println("app key:" + TMConfigs.App.APP_KEY);
        System.out.println("app secret:" + TMConfigs.App.APP_SECRET);
        MarketingTagAddRequest req = new MarketingTagAddRequest();
        req.setTagName("生日特价人群");
        req.setDescription("这是一个标签描述");
        try {
            MarketingTagAddResponse response = client
                    .execute(req,
                            "610152690e3cc364450a28c19cd2735ec415ff2dbd4b8b63594267752");
            System.out.println("result:" + response.getBody());
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    @JsonAutoDetect
    public static class UserInfo {

        @JsonProperty
        String username = StringUtils.EMPTY;

        @JsonProperty
        int level = 0;

        @JsonProperty
        boolean award;

        // 好评赠送
        @JsonProperty
        boolean reward = false;

        public UserInfo() {
            super();
        }

        public static UserInfo getUserInfo(int level, Long userId,
                String username) {
            User user = UserDao.findById(userId);
            boolean award = false;
            if (user.isPopularAward()) {
                award = true;
            }
            /*
             * boolean isPopularOn = true; if (user.isPopularOff()) {
             * isPopularOn = false; }
             */

            return new UserInfo(level, award);
        }

        public UserInfo(int level, boolean award) {
            this.level = level;
            this.award = award;
        }
    }

    public static void addUserTag(String name, String description) {
        User user = getUser();
        Long userId = user.getId();
        Long tagId = PromotionAction.addUserTag(userId, name, description);
        renderJSON(new TMResult(tagId));
        /*
         * User user = getUser(); String sid =user.getSessionKey(); try {
         * boolean result = PromotionAction.addUserTag(sid,name, description);
         * if(result==true){ renderJSON(TMResult.renderMsg(StringUtils.EMPTY));
         * }else{ renderJSON(TMResult.failMsg("添加人群标签失败!")); } } catch
         * (ApiException e) { e.printStackTrace();
         * renderJSON(TMResult.failMsg("添加人群标签失败:"+e.getErrMsg())); }
         */
    }

    public static void editActivity(Long activityId, String title,
            String description, String startTimeStr, String endTimeStr,
            String activityType, String discountValue, String decreaseNum,
            Long userTagId) {
        //Activity activity = Activity.findById(activityId);
        
        User user = getUser();
        
        TMProActivity activity = TMProActivity.findByActivityId(user.getId(), activityId);
        if (activity == null) {
            renderJSON(TMResult.failMsg("准备编辑的活动不存在!"));
        }
        if (userTagId == null
                || UserTag.findById(userTagId) == null
                || (!activityType.equals(TMProActivity.Type.DISCOUNT) && !activityType
                        .equals(TMProActivity.Type.PRICE))
                || CommonUtils.String2Double(discountValue) == -1) {
            renderJSON(TMResult.failMsg("对不起，输入参数不正确!"));
        } else {
            
            Long userId = user.getId();
            Long createTime = CommonUtils.Date2long(new Date());
            Long startTime = CommonUtils.Date2long(CommonUtils
                    .String2Date(startTimeStr));
            Long endTime = CommonUtils.Date2long(CommonUtils
                    .String2Date(endTimeStr));
            PromotionAction.editActivity(activityId, userId, title,
                    description, createTime, startTime, endTime, activityType,
                    discountValue, decreaseNum, userTagId);
            renderJSON(JsonUtil.getJson(new TMResult(activityId)));
        }
    }

    public static void deleteActivity(Long activityId) {
        //Activity activity = Activity.findById(activityId);
        
        User user = getUser();
        
        TMProActivity activity = TMProActivity.findByActivityId(user.getId(), activityId);
        if (activity != null) {
            //activity.delete();
            JDBCBuilder.update(false, "delete from activity where id = ?", activity.getId());
            renderJSON(TMResult.OK);
        } else {
            renderJSON(TMResult.failMsg("要删除的活动不存在!"));
        }
    }

    public static void checkUserTagEditable(Long userTagId) {
        if (userTagId == 1L || UserTag.findById(userTagId) == null) {
            renderJSON(TMResult.failMsg("标签ID不存在或不可编辑!"));
        } else {
            renderJSON(TMResult.OK);
        }
    }

    public static void checkUserTagDeletable(Long userTagId) {
        if (userTagId == 1L || UserTag.findById(userTagId) == null) {
            renderJSON(TMResult.failMsg("标签ID不存在或不可删除!"));
        } else {
            renderJSON(TMResult.OK);
        }
    }

    public static void editUserTag(Long userTagId, String name,
            String description) {
        User user = getUser();
        Long userId = user.getId();
        PromotionAction.addUserTag(userId, name, description);
        if (userTagId == null || UserTag.findById(userTagId) == null) {
            renderJSON(TMResult.failMsg("editUserTag error!"));
        } else {
            UserTag userTag = UserTag.findById(userTagId);
            userTag.setTagName(name);
            userTag.setDescription(description);
            userTag.save();
            renderJSON(TMResult.OK);
        }
    }

    public static void removeUserTag(Long userTagId) {
        if (userTagId == 1L || UserTag.findById(userTagId) == null) {
            renderJSON(TMResult.failMsg("标签ID不存在!"));
        } else {
            UserTag userTag = UserTag.findById(userTagId);
            userTag.delete();
            renderJSON(TMResult.OK);
        }
    }

    public static void userTagAddUser(Long userTagId, String userList) {
        if (userTagId == null || UserTag.findById(userTagId) == null) {
            // find UserTag error
        } else {
            UserTag userTag = UserTag.findById(userTagId);
            String[] users = userList.split(",");
            List<String> addedUserList = new ArrayList<String>();
            for (String user : users) {
                if (!userTag.getUsers().contains(user)) {
                    userTag.addUser(user);
                    addedUserList.add(user);
                }
            }
            userTag.save();
            renderJSON(new TMResult(addedUserList));
        }
    }

    // 添加活动接口
//    public static void addActivity(String title, String description,
//            String startTimeStr, String endTimeStr, String activityType,
//            String discountValue, String decreaseNum, Long userTagId,
//            String items) {
//        if (userTagId == null
//                //                || UserTag.findById(userTagId) == null
//                || (!activityType.equals(Activity.Type.DISCOUNT) && !activityType
//                        .equals(Activity.Type.PRICE))
//                || CommonUtils.String2Double(discountValue) == -1
//                || (!decreaseNum.equals("1") && !decreaseNum.equals("0"))) {
//            log.error(String
//                    .format("param error, userTagId = [%d],type=[%s],discountValue=[%s],decreaseNum=[%s]",
//                            userTagId, activityType, discountValue, decreaseNum));
//            renderJSON(TMResult.failMsg("对不起，输入参数不正确!"));
//
//        } else {
//            User user = getUser();
//            Long userId = user.getId();
//            Long createTime = CommonUtils.Date2long(new Date());
//            Long startTime = CommonUtils.Date2long(CommonUtils
//                    .String2Date(startTimeStr));
//            Long endTime = CommonUtils.Date2long(CommonUtils
//                    .String2Date(endTimeStr));
//            log.info("start add activity......");
//            Long activityId = PromotionAction.addActivity(userId, title,
//                    description, createTime, startTime, endTime, activityType,
//                    discountValue, decreaseNum, userTagId, items);
//            renderJSON(JsonUtil.getJson(new TMResult(activityId)));
//        }
//    }

    public static void getUserInfo() {
        User user = getUser();
        int level = user.getVersion();
        renderJSON(JsonUtil.getJson(UserInfo.getUserInfo(level, user.getId(),
                user.getUserNick())));
    }

    // 宝贝列表
    public static void searchItems(String s, int pn, int ps, int sort,
            int polularized, Long catId, int popularizeStatus) {
        final User user = getUser();

        popularizeStatus = PopularizedStatusSqlUtil
                .checkStatus(popularizeStatus);

        pn = pn < 1 ? 1 : pn;
        ps = ps < 1 ? PageSize.DISPLAY_ITEM_PAGE_SIZE : ps;

        // Set<Long> pops =
        // PopularizedDao.findNumIidsByUserIdWithStatus(user.getId(),
        // popularizeStatus);

        if (catId == null || catId <= 0) {
            catId = null;
        }

        List<ItemPlay> list = ItemDao.searchPop(user.getId(), (pn - 1) * ps,
                ps, s, sort, polularized, catId == null ? "" : catId.toString(), popularizeStatus);
        if (CommonUtils.isEmpty(list)) {
            TMPaginger.makeEmptyFail("亲， 您还没有上架宝贝哟！！！！！");
        } else {
            /*
             * for (ItemPlay item : list) { if (item == null) continue; if
             * (pops.contains(item.getNumIid())) { item.setPopularized();
             * 
             * //找到vgItemId if(APIConfig.get().vgouSave()) { long vgItemId =
             * VGItemDao.findIdByNumIid(user.getId(), item.getNumIid()); if
             * (vgItemId < 0) vgItemId = 0; item.setVgItemId(vgItemId); } } }
             */
            /*
             * for(ItemPlay item : list){ if(item == null){ continue; } }
             */
        }
        log.info("Total items get [%d]", list.size());
        PageOffset po = new PageOffset(pn, ps, 10);
        TMResult tmRes = new TMResult(list, (int) ItemDao.countPop(
                user.getId(), s, sort, catId == null ? "" : catId.toString(),
                polularized, popularizeStatus), po);
        renderJSON(JsonUtil.getJson(tmRes));
    }

    // 会员
    public static void saveVipConfig(String type, String quantity[],
            String discount[]) {
        if (quantity.length != 4 || discount.length != 4) {
            renderJSON(TMResult.failMsg("参数不合法!"));
        } else {
            User user = getUser();
            Long quantityLong[] = new Long[4];
            Long discountLong[] = new Long[4];
            // quantityLong[0] = Long.parseLong(quantity[0]);
            quantityLong[1] = Long.parseLong(quantity[1]);
            quantityLong[2] = Long.parseLong(quantity[2]);
            quantityLong[3] = Long.parseLong(quantity[3]);

            discountLong[0] = Long.parseLong(discount[0]);
            discountLong[1] = Long.parseLong(discount[1]);
            discountLong[2] = Long.parseLong(discount[2]);
            discountLong[3] = Long.parseLong(discount[3]);

            PromotionAction.saveVipConfig(user.getId(), type, quantityLong,
                    discountLong);
        }
    }

    public static void viewVipList(Integer vipLevel) {
        User user = getUser();
        Long userId = user.getId();
        VipConfig config = VipConfig.find("byUserId", userId).first();
        if (config == null) {
            log.error("can't find VipConfig for userId [%d],vipLevel [%d]",
                    userId, vipLevel);
            renderJSON(TMResult.failMsg("找不到对应的会员等级配置！"));
        }
        Set<String> users = config.getVips(vipLevel);
        if (users != null) {
            renderJSON(new TMResult(true, "userList", Arrays.asList(users
                    .toArray())));
        } else {
            renderJSON(TMResult.failMsg("wrong vip level"));
        }
    }

    public static void addVipMember(String userNick, Integer vipLevel) {
        User user = getUser();
        Long userId = user.getId();
        VipConfig config = VipConfig.find("byUserId", userId).first();
        if (config == null) {
            log.error("can't find VipConfig for userId [%d],vipLevel [%d]",
                    userId, vipLevel);
            renderJSON(TMResult.failMsg("找不到对应的会员等级配置！"));
        } else {
            config.addVip(userNick, vipLevel);
            config.save();
            renderJSON(TMResult.OK);
        }
    }

}
