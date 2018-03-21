
package actions;

import java.util.concurrent.Callable;

import models.oplog.TMUserWorkRecord;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import titleDiag.DiagResult;
import transaction.TransactionSecurity;
import bustbapi.ShopApi;
import bustbapi.UserAPIs;
import cache.CacheKeyGenerator;
import cache.CacheVisitor;

import com.taobao.api.domain.Shop;

import configs.TMConfigs;
import controllers.APIConfig;

/**
 * TODO Add the job...
 * 
 * @author zhourunbo
 * 
 */
public class UserLoginAction implements CacheVisitor<Long> {

    private static final Logger log = LoggerFactory.getLogger(UserLoginAction.class);

    public static final String TAG = "UserLoginAction";

    private static UserLoginAction EMPTY_INSTANCE = new UserLoginAction();

    private static Object EMPTY_OBJ = StringUtils.EMPTY;

    private static Long ITEM_API_PAGE_SIZE = 10L;

    public static UserLoginAction getInstance() {
        return EMPTY_INSTANCE;
    }

    public static void basicInfoCheck(final User user, final String userNick) {

        if (isUserTagLocked(user.getId())) {
            log.warn("User Login Tag is Locked....");
            return;
        }

        lockUserTag(user.getId());
        log.error("Lock User :" + user);

        TMConfigs.getDiagResultPool().submit(new Callable<DiagResult>() {
            public DiagResult call() throws Exception {
                new TransactionSecurity<Void>() {

                    @Override
                    public Void operateOnDB() {
                        long start = System.currentTimeMillis();

                        // new UdpRequestJob(user.getId(), true, true).doJob();
                        log.error("in stall for user");

                        String tag = "doForInstall";
//                        TMWorkWritter.addToWritter(user.getId(), , "--");
                        if (TMUserWorkRecord.exists(user.getId(), tag)) {

                        } else {
                            TMUserWorkRecord.TMJdpFailWritter.addMsg(user, tag);
                            APIConfig.get().doForInstall(user);
                        }

//                        new ItemUpdateJob(user.getId(), System.currentTimeMillis(), ITEM_API_PAGE_SIZE, true).doJob();
//                        TemplateAction.doInstallItemMonitor(user);
                        TMConfigs.getDiagResultPool().submit(new Callable<DiagResult>() {

                            @Override
                            public DiagResult call() throws Exception {
                                Shop tbShop = new ShopApi.ShopGet(userNick).call();
                                if (tbShop == null) {
                                    return null;
                                }
                                user.setCid(tbShop.getCid().intValue());
                                user.jdbcSave();
                                return null;
                            }
                        });

                        unLockUserTag(user.getId());

                        long end = System.currentTimeMillis();
                        log.warn("[End User Login Check Takes ]:" + (end - start) + "ms");
                        return null;
                    }
                }.execute();

                return null;
            }
        });

    }

    public static void lockUserTag(Long userId) {
        if (userId == null || userId.longValue() <= 0L) {
            return;
        }

        String key = CacheKeyGenerator.get(getInstance(), userId.toString());
        Cache.safeSet(key, EMPTY_OBJ, getInstance().expired());
    }

    public static void unLockUserTag(Long userId) {
        if (userId == null || userId.longValue() <= 0L) {
            return;
        }

        String key = CacheKeyGenerator.get(getInstance(), userId.toString());
        Cache.safeDelete(key);
    }

    public static boolean isUserTagLocked(Long userId) {

        if (userId == null || userId.longValue() <= 0L) {
            log.warn("User null????:" + userId);
            return false;
        }

        String key = getInstance().genKey(userId);
        return Cache.get(key) != null;
    }

    @Override
    public String prefixKey() {
        return TAG;
    }

    @Override
    public String expired() {
        // TODO set to 10 seconds....
        return "20s";
    }

    @Override
    public String genKey(Long t) {
        return CacheKeyGenerator.get(getInstance(), t.toString());
    }

    public static boolean checkUserVaild(models.user.User userPlay) {

        com.taobao.api.domain.User userTB = new UserAPIs.UserGetApi(userPlay.sessionKey, userPlay.getUserNick()).call();

        if (userTB == null) {
            return false;
        }
        return true;
    }

    public static void doAfterLogin(final models.user.User user) {
        TMConfigs.getDiagResultPool().submit(new Callable<DiagResult>() {
            @Override
            public DiagResult call() throws Exception {
                UserAction.updateUser(user);
                return null;
            }
        });
    }
}
