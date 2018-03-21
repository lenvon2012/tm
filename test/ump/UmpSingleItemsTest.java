
package ump;

import java.util.ArrayList;
import java.util.List;

import models.item.ItemPlay;
import models.user.User;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.test.UnitTest;
import result.TMResult;
import tbapi.ump.UMPApi.UmpSingleItemActivityDelete;

import com.ciaosir.client.utils.DateUtil;

import configs.TMConfigs;

import dao.UserDao;
import dao.item.ItemDao;

public class UmpSingleItemsTest extends UnitTest {

    private static final Logger log = LoggerFactory.getLogger(UmpSingleItemsTest.class);

    public static final String TAG = "UmpSingleItemsTest";

    String appkey = "1021348761";

    String sandboxNick = "sandbox_autotitle";

    String sandboxPasd = "autotitle";

    String containerUrl = "http://container.api.tbsandbox.com/container?encode=utf-8&appkey=1021348761";

    String testSid = "";

    @Test
    public void testUmpCreate() {
        User user = UserDao.findByUserNick(sandboxNick);
        UserDao.refreshToken(user);

        long start = System.currentTimeMillis();
        long end = start + DateUtil.DAY_MILLIS;
        String activityName = "新春活动走起";
        String comment = null;
        String tagnName = "盛夏聚会";
/*
        TMPromotionActivity tmActivity = new TMPromotionActivity(user.getId(), start, end, activityName, comment,
                tagnName, ActivityType.SimpleDiscount);
        if (tmActivity.doValid()) {
            tmActivity.save();
        } else {
            // TODO return;
            log.error("fail for no activity created...");
            return;
        }
        */
        /*
        List<TMSingleItemPromotion> activities = new ArrayList<TMSingleItemPromotion>();

        List<ItemPlay> items = ItemDao.findByUserId(user.getId());
        int size = items.size();
        for (int i = 0; i < size; i++) {
            if (i > 5) {
                break;
            }

            ItemPlay item = items.get(i);
            SingleItemPromotionParams params = new SingleItemPromotionParams(tmActivity.getId(), item.getNumIid(),
                    ItemPromoteType.discount.name(), 800L, 800L);

            TMResult<TMSingleItemPromotion> res = TMSingleItemPromotion.doPromote(params);
            if (res.isOk()) {
                activities.add(res.getRes());
            } else {
                log.error("res : " + res.getMsg());
                if ("isv.w2-security-authorize-invalid".equals(res.getMsg())) {
                    log.warn(" w2 need refresh : go resresh:\n" + TMConfigs.App.CONTAINER_TAOBAO_URL);
                    break;
                }
            }
        }

        log.info("[created : activities]" + activities);
        for (TMSingleItemPromotion promotion : activities) {
            UmpSingleItemActivityDelete api = new UmpSingleItemActivityDelete(user, promotion.getId());
            Boolean res = api.call();
            if (res != null && res.booleanValue()) {
                log.info("delete success for :" + promotion);
                promotion.markDeleted();
            } else {
                log.error(" deletion fails :" + promotion);
            }
        }

        log.error(" remain : local " + tmActivity);*/
    }

}
