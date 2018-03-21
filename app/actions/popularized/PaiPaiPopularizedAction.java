package actions.popularized;

import java.util.List;

import models.paipai.PaiPaiUser;
import models.popularized.Popularized;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ppapi.models.PaiPaiItem;
import utils.PaiPaiUtil;

import com.ciaosir.client.CommonUtils;

import dao.popularized.PopularizedDao;

public class PaiPaiPopularizedAction {
    private static final Logger log = LoggerFactory.getLogger(PaiPaiPopularizedAction.class);

    public static void addPaiPaiPopularized(PaiPaiUser user, List<PaiPaiItem> paipaiItemList, int status) {
        if (user == null)
            return;
        if (CommonUtils.isEmpty(paipaiItemList))
            return;
        
        for (PaiPaiItem paipaiItem : paipaiItemList) {

            Long numIid = paipaiItem.initLongNumIid();
            
            Popularized popularized = PopularizedDao.findByNumIid(user.getId(), numIid);
            if (popularized == null) {
                Long catId = (long) paipaiItem.getCategoryId();
                
                Long firstCid = PaiPaiUtil.getPaiPaiFirstCatId(catId);
                
                if (firstCid == null) {
                    firstCid = 0l;
                }
                // save as popularized
                int salesCount = 0;
                Popularized p = new Popularized(user.getId(), numIid, paipaiItem.getTitle(),
                        paipaiItem.getPicPath(), paipaiItem.getItemPrice(), salesCount, (long) paipaiItem.getCategoryId(),
                        user.getVersion(), user.getFirstLoginTime(), firstCid);
                //double skuMinPrice = UpdatePopularizedJob.getItemSkuMinPrice(user, itemPlay);
                //p.setSkuMinPrice(skuMinPrice);
                //设置推广状态
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
    
    
    
}
