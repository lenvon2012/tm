package actions.popularized;

import java.util.List;

import job.apiget.UpdatePopularizedJob;
import models.item.ItemPlay;
import models.popularized.Popularized;
import models.user.User;
import models.vgouitem.VGouItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.TaobaoUtil;

import com.ciaosir.client.CommonUtils;

import controllers.APIConfig;
import dao.popularized.PopularizedDao;
import dao.popularized.VGItemDao;

public class PopularizedAction {
    private static final Logger log = LoggerFactory.getLogger(PopularizedAction.class);

    public static void addPopularized(User user, List<ItemPlay> itemList, int status) {
        if (user == null)
            return;
        if (CommonUtils.isEmpty(itemList))
            return;
        
        
        for (ItemPlay itemPlay : itemList) {
            //     itemPlay.setPopularized();
            //     itemPlay.jdbcSave();
            //Popularized popularized = Popularized.find("userId = ? and numIid = ?", user.getId(), itemPlay.getId())
            //        .first();
        	Popularized popularized = Popularized.findByNumIid(user.getId(), itemPlay.getId());
            if (popularized == null) {
                Long firstCid = TaobaoUtil.getFirstLevel(itemPlay.getCid());
                if (firstCid == null) {
                    firstCid = 0l;
                }
                // save as popularized
                Popularized p = new Popularized(user.getId(), itemPlay.getNumIid(), itemPlay.getTitle(),
                        itemPlay.getPicURL(), itemPlay.getPrice(), itemPlay.getSalesCount(), itemPlay.getCid(),
                        user.getVersion(), user.getFirstLoginTime(), firstCid);
                double skuMinPrice = UpdatePopularizedJob.getItemSkuMinPrice(user, itemPlay);
                p.setSkuMinPrice(skuMinPrice);
                //设置推广状态
                p.setStatus(status);
                p.jdbcSave();
                
                // save as vgouitem
                if (APIConfig.get().vgouSave()) {
                     String img = itemPlay.getPicURL() + "_210x210.jpg";
                     VGouItem v = new VGouItem(user.getId(), itemPlay.getNumIid(), itemPlay.getTitle(),
                             img, itemPlay.getPrice(), itemPlay.getCid());
                     v.setSkuMinPrice(skuMinPrice);
                     v.setSimg(itemPlay.getPicURL() + "_210x210.jpg");
                     v.setBimg(itemPlay.getPicURL() + "_480x480.jpg");
                     v.jdbcSave();
                }
               
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
            removePopularized(userId, popu, status);//删除
        }
        
        //删除VGouItem
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
    
    
}
