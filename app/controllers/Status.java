
package controllers;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

import models.UserDiag;
import models.defense.DefenseWarn;
import models.user.TokenExpiresIn;
import models.user.User;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;

import play.cache.Cache;
import result.TMResult;
import actions.GetTokenExpiresAction;
import actions.SubcribeAction;
import actions.SubcribeAction.SubscribeInfo;
import bustbapi.ItemApi;
import bustbapi.MBPApi;
import bustbapi.TMTradeApi;
import cache.UserLoginInfoCache;
import cache.UserLoginInfoCache.UserCacheCaller;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;
import com.ciaosir.client.utils.JsonUtil;
import com.taobao.api.domain.QueryRow;

import configs.Subscribe.Version;
import dao.defense.DefenseWarnDao;

/**
 * TODO Clear the status when user login or use modify the window status
 * @author zrb
 *
 */
public class Status extends TMController {

    public static void js() {
        User user = getUser();
        String res = statusJsCaller.ensureForUser(user);
        renderJs(res);
    }

    public static void user() {
        User user = getUser();
        StringBuilder sb = new StringBuilder();
        sb.append("TM.name = '");
        sb.append(user.getUserNick());
        sb.append("';\n");
        sb.append("TM.isAutoShow=" + user.isShowWindowOn());
        sb.append(";\n");
        sb.append("TM.isAutoComment=" + user.isAutoCommentOn());
        sb.append(";\n");
        sb.append("TM.isAutoDelist=" + user.isAutoDelistOn());
        sb.append(";\n");
        sb.append("TM.id=" + user.getId());
        sb.append(";\n");
        sb.append("TM.isFenxiao=" + user.isFengxiao());
        sb.append(";\n");
        sb.append("TM.hasShowWindow=" + APIConfig.get().isUserSycnTrade(user));
        sb.append(";\n");
        sb.append("TM.created=" + (user.getFirstLoginTime()));
        sb.append(";\n");
        sb.append("TM.version=" + (user.getVersion()));
        sb.append(";\n");
        sb.append("TM.lastUpdateTime=" + (user.lastUpdateTime));
        sb.append(";\n");
        sb.append("TM.firstLoginTime=" + (user.firstLoginTime));
        sb.append(";\n");
        renderJs(sb.toString());
    }

    public static void discountName() {
        User user = getUser();
        StringBuilder sb = new StringBuilder();
        sb.append("TM.name = '");
        sb.append(user.getUserNick());
        sb.append("';\n");
        renderJs(sb.toString());
    }

    public static UserCacheCaller<String> statusJsCaller = new UserCacheCaller<String>() {

        String TAG = "_statusJsCaller";

        @Override
        public String ensureForUser(User user) {
            String key = TAG + user.getId();
            String value = (String) Cache.get(key);
            if (value != null) {
                return value;
            }

            SubscribeInfo subscribe = SubcribeAction.getMax(user);
            StringBuilder sb = new StringBuilder();
            sb.append("TM.ver = ");
            sb.append(subscribe.getVersion());
//            sb.append("1");
            sb.append(";\n");

            sb.append("TM.name = '");
            sb.append(user.getUserNick());
            sb.append("';\n");

            sb.append("TM.isTmall = '");
            sb.append(user.isTmall());
            sb.append("';\n");

            sb.append("TM.level = '");
            sb.append(user.getLevel());
            sb.append("';\n");

            sb.append("TM.isFree=");
            sb.append(subscribe.getVersion() <= Version.FREE);
            sb.append(";\n");

            sb.append("TM.timeLeft=");
            sb.append(subscribe.getLeft());
            sb.append(";\n");

            sb.append("TM.kitsAvailable=");
//            sb.append(subscribe.getVersion() >= Version.BASE);
            sb.append("false");
            sb.append(";\n");

            sb.append("TM.isAutoShow=" + user.isShowWindowOn());
            sb.append(";\n");
            sb.append("TM.isAutoComment=" + user.isAutoCommentOn());
            sb.append(";\n");
            sb.append("TM.isAutoDelist=" + user.isAutoDelistOn());
            sb.append(";\n");

            sb.append("TM.id=" + user.getId());
            sb.append(";\n");
            sb.append("TM.isFenxiao=" + user.isFengxiao());
            sb.append(";\n");
            sb.append("TM.hasShowWindow=" + APIConfig.get().isUserSycnTrade(user));
            sb.append(";\n");
            sb.append("TM.created=" + (user.getFirstLoginTime()));
            sb.append(";\n");
            sb.append("TM.version=" + (user.getVersion()));
            sb.append(";\n");
            sb.append("TM.lastUpdateTime=" + (user.lastUpdateTime));
            sb.append(";\n");
            sb.append("TM.firstLoginTime=" + (user.firstLoginTime));
            sb.append(";\n");

            value = sb.toString();

            Cache.add(key, value, (20 + user.getId().intValue() % 5) + "min");

            return value;
        }

        @Override
        public void clearForUser(User user) {
            String key = TAG + user.getId();
            Cache.delete(key);
        }
    };

    static {
        UserLoginInfoCache.get().addUserCaller(statusJsCaller);
    }

    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    public static void userdiag() {
        User user = getUser();
        UserDiag userDiag = UserDiag.findOrCreate(user);

        String day = sdf.format(new Date(System.currentTimeMillis() - DateUtil.DAY_MILLIS));
        TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(2789L, "thedate=" + day + ",sellerId=" + user.getId(),
                user.getSessionKey()).call();
        List<QueryRow> rows = res.getRes();
        if (!CommonUtils.isEmpty(rows)) {
            QueryRow row = rows.get(0);
            Integer tradeCount = Integer.valueOf(row.getValues().get(1));
            if (tradeCount > 0) {
                userDiag.setTradeCount(tradeCount);
            }
        }

        Long count = new ItemApi.ItemsOnsaleCount(user.getSessionKey(), null, null).call();
        Object[] objects = {
                count, userDiag
        };
        renderJSON(JsonUtil.getJson(objects));
    }

    public static void diag() {
        User user = getUser();
        TMResult res = new TMResult(true, null, new DiagRes(user));
        //总数res.count;
        renderJSON(JsonUtil.getJson(res));
    }

    @JsonAutoDetect
    static class DiagRes {
        @JsonProperty
        int tradeCount;

        @JsonProperty
        int level;

        public DiagRes(User user) {
            Long tradeNum = new TMTradeApi.TradeNumUpdate(user).call();
            if (tradeNum != null) {
                tradeCount = tradeNum.intValue();
            }
        }
    }

    /**
     * {"w2_expires_in":0,"r2_expires_in":0}
     */
    public static void rwTimeLeft() {
        User user = getUser();
        TokenExpiresIn expireInfo = GetTokenExpiresAction.get(user.getSessionKey(), user.getRefreshToken());
        log.info("[expire:]" + expireInfo);
        renderJSON(JsonUtil.getJson(expireInfo));
    }

    public static void mobile() {
        User user = getUser();
        List<DefenseWarn> warnList = DefenseWarnDao.findByUserId(user.getId());
        renderJSON(JsonUtil.getJson(warnList));
    }

}
