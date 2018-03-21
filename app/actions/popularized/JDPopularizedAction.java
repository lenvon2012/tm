package actions.popularized;

import java.util.List;

import models.jd.JDItemPlay;
import models.jd.JDUser;
import models.popularized.Popularized;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.TaobaoUtil;

import com.ciaosir.client.CommonUtils;

import controllers.APIConfig;
import dao.popularized.PopularizedDao;
import dao.popularized.VGItemDao;

public class JDPopularizedAction {
    private static final Logger log = LoggerFactory.getLogger(JDPopularizedAction.class);

    public static void addPopularized(JDUser user, List<JDItemPlay> itemList, int status) {
        if (user == null)
            return;
        if (CommonUtils.isEmpty(itemList))
            return;

        for (JDItemPlay itemPlay : itemList) {
            // itemPlay.setPopularized();
            // itemPlay.jdbcSave();
            // Popularized popularized = Popularized.find("userId = ? and numIid = ?", user.getId(), itemPlay.getId())
            // .first();
            Popularized popularized = Popularized.findByNumIid(user.getId(), itemPlay.getId());
            if (popularized == null) {
                Long firstCid = TaobaoUtil.getFirstLevel(0L);
                if (firstCid == null) {
                    firstCid = 0l;
                }
                // save as popularized
                Popularized p = new Popularized(user.getId(), itemPlay.getSkuId(), itemPlay.getTitle(),
                        itemPlay.getPicURL(), itemPlay.getPrice(), 0, 0L, user.getVersion(), user.getFirstLoginTime(),
                        firstCid);

                p.setSkuMinPrice(itemPlay.getPrice());
                // 设置推广状态
                p.setStatus(status);
                p.jdbcSave();
            } else {
                if (popularized.hasStatus(status) == false) {
                    popularized.addStatus(status);
                    popularized.jdbcSave();
                }

            }
        }
    }

    public static void removeAllPopularized(Long userId, int status) {
        if (userId == null)
            return;

        List<Popularized> popuList = PopularizedDao.queryPopularizedsByUserIdAndStatus(userId, status);
        if (CommonUtils.isEmpty(popuList))
            return;

        for (Popularized popu : popuList) {
            removePopularized(userId, popu, status);// 删除
        }

        // 删除VGouItem
        if (APIConfig.get().vgouSave()) {
            VGItemDao.deleteVGouItemByUserId(userId);
        }

    }

    public static void removePopularized(Long userId, Popularized popu, int status) {
        if (userId == null)
            return;
        if (popu == null)
            return;
        if (popu.getUserId() == null || !popu.getUserId().equals(userId))
            return;
        popu.removeStatus(status);
        if (popu.getStatus() <= 0) {
            PopularizedDao.deletePopularize(popu);
        } else {
            popu.jdbcSave();
        }
    }

    public static void removePopularizedById(Long userId, Long numIid) {
        PopularizedDao.deletePopularizeById(userId, numIid);
    }

}
