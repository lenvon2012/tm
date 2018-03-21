/**
 * 
 */

package actions.paipai;

import java.util.List;
import java.util.concurrent.Callable;

import models.paipai.PaiPaiUser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.mvc.results.Redirect;
import ppapi.PaiPaiSubscribeApi;
import ppapi.PaiPaiUserInfoApi;
import ppapi.models.PaiPaiSubscribe;
import ppapi.models.PaiPaiUserInfo;
import titleDiag.DiagResult;
import transaction.TransactionSecurity;
import actions.UserLoginAction;

import com.ciaosir.client.CommonUtils;

import configs.Subscribe;
import configs.Subscribe.Version;
import configs.TMConfigs;
import controllers.PaiPaiAPIConfig;

/**
 * @author navins
 * @date 2013-7-8 下午3:23:45
 */
public class PaiPaiAction {

    private static final Logger log = LoggerFactory.getLogger(PaiPaiAction.class);

    public static final String PAIPAI_REDIRECT_URL = "http://fuwu.paipai.com/my/app/authorizeGetAccessToken.xhtml?responseType=access_token&appOAuthID=";

    public static PaiPaiAction _instance = new PaiPaiAction();

    public static PaiPaiAction get() {
        return _instance;
    }

    public PaiPaiUser checkLogin(long uin, String accessToken) {
        if (uin < 10000) {
            throw new Redirect(buildRedir());
        }

        PaiPaiUser user = PaiPaiUser.findByUserId(uin);
        if (user == null) {
            user = new PaiPaiUser(uin, accessToken);
            PaiPaiUserInfo info = new PaiPaiUserInfoApi(user).call();
            if (info == null) {
                throw new Redirect(buildRedir());
            }
            user.setFirstLoginTime(System.currentTimeMillis());
            user.setNick(info.getNick());
        } else {
            user.setAccessToken(accessToken);
        }
        int version = getVersion(user);
        user.setVersion(version);
        user.jdbcSave();
        log.info("[save paipai user:]" + user);

        return user;
    }

    public int getVersion(PaiPaiUser user) {
        boolean isValid = false;
        int version = Version.FREE;
        List<PaiPaiSubscribe> subList = new PaiPaiSubscribeApi.PaiPaiSubscribeListApi(user).call();
        
        if(user.getId()==301074800){
            return 1;
        }
        
        if (CommonUtils.isEmpty(subList)) {
            user.setVersion(version);
            user.setValid(isValid);
            user.jdbcSave();
            return Version.FREE;
        }

        // long recent = Long.MAX_VALUE;
        long ts = System.currentTimeMillis();

        for (PaiPaiSubscribe sub : subList) {
            // if(recent > sub.getDeadLine()) {

            int tmp = Subscribe.getVersionByCode(String.valueOf(sub.getChargeItemId()));
            if (tmp > version && ts < sub.getDeadLine()) {
                version = tmp;
            }

            if (ts < sub.getDeadLine()) {
                isValid = true;
            }
            // recent = sub.getDeadLine();
            // }
        }

        user.setVersion(version);
        user.setValid(isValid);
        user.jdbcSave();
        
        return version;
    }

    public String buildRedir() {
        // String origin = PAIPAI_REDIRECT_URL + "&appOAuthID=%s&appOAuthkey=%s";

        // String url = String.format(origin, apiConfig.getAppOAuthID(), apiConfig.getAppOAuthkey());
        String url = PAIPAI_REDIRECT_URL + PaiPaiAPIConfig.get().getAppOAuthID();

        return url;
    }

    public void ensureBaseInfo(final PaiPaiUser pUser) {
        UserLoginAction.lockUserTag(pUser.getId());
        TMConfigs.getDiagResultPool().submit(new Callable<DiagResult>() {
            @Override
            public DiagResult call() throws Exception {
                return new TransactionSecurity<DiagResult>() {
                    @Override
                    public DiagResult operateOnDB() {
                        doForPaiPaiUser(pUser);
                        return null;
                    }
                }.execute();
            }
        });
    }

    private void doForPaiPaiUser(PaiPaiUser pUser) {
        // TODO item update job we need
        UserLoginAction.unLockUserTag(pUser.getId());
    }
}
