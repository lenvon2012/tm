
package onlinefix;

import java.util.List;

import models.industry.TopShop;
import models.industry.TopShopItem;
import models.industry.TopUrlToShop;
import models.word.top.TopURLBase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import transaction.JPATransactionManager;

import com.ciaosir.client.api.API.PYSpiderOption;
import com.ciaosir.client.api.SellerAPI;
import com.ciaosir.client.api.ShopSearchAPI;
import com.ciaosir.client.item.ShopInfo;
import com.ciaosir.client.pojo.ItemThumb;

public class IndustryJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(IndustryJob.class);

    public static final String TAG = "IndustryJobs";

    int max = Integer.MAX_VALUE;

    int offset = 0;

    public IndustryJob() {
        super();
    }

    public IndustryJob(int max, int offset) {
        super();
        this.max = max;
        this.offset = offset;
    }

    public void doJob() {
        List<TopURLBase> bases = TopURLBase.findAllLevel3WithCid(offset);
        int count = 0;
        for (TopURLBase base : bases) {
            try {
                log.info("[ do with base]" + base);
                new TopUrlIndustryJob(base).doJob();
                if (count++ > max) {
                    log.warn("max reached....");
                    return;
                }
                JPATransactionManager.clearEntities();
            } catch (Exception e) {
                log.warn(e.getMessage(), e);

            }
        }
    }

    public static class TopUrlIndustryJob extends Job {
        TopURLBase base;

        public TopUrlIndustryJob(TopURLBase base) {
            super();
            this.base = base;
        }

        public void doJob() {
            String tag = base.getTag();
            List<ShopInfo> shopInfos = ShopSearchAPI.getShopInfo(tag, null, new PYSpiderOption(false, 2));
            log.info("[shops :]" + shopInfos);
            for (ShopInfo info : shopInfos) {
                try {
                    TopUrlToShop.ensureRelation(base, info);
                    new TopShopJob(info).doJob();
                    JPATransactionManager.clearEntities();
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);

                }
            }
        }
    }

    public static class TopShopJob extends Job {
        ShopInfo info;

        public TopShopJob(ShopInfo info) {
            this.info = info;
        }

        public void doJob() {
            log.info("[ do for shop ]" + info);
            TopShop.ensureInfo(info);
            try {
                List<ItemThumb> itemArray = SellerAPI.getItemArray(info.getShopnick(), null, 19, null, true);
                log.info("[found itemds :]" + itemArray);
                for (ItemThumb itemThumb : itemArray) {
                    new TopShopItemJob(info, itemThumb).doJob();
                }
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }
    }

    public static class TopShopItemJob extends Job {
        ShopInfo sInfo;

        ItemThumb thumb;

        public TopShopItemJob(ShopInfo sInfo, ItemThumb thumb) {
            super();
            this.sInfo = sInfo;
            this.thumb = thumb;
        }

        public void doJob() {
            log.info("[sinfo:]" + sInfo + " [ thumb]:" + thumb);
            TopShopItem.ensure(sInfo, thumb);
        }
    }
}
