package job.dazhe;

import java.util.List;

import models.item.ItemPlay;
import models.promotion.EveryDaySalesCount;
import models.promotion.Promotion;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import play.jobs.On;
import actions.promotion.PromotionAction;
import controllers.APIConfig;
import dao.UserDao.UserBatchOper;
import dao.item.ItemDao;

@On("0 0 4 * * ?")
public class UpdateSalesCountJob extends Job {
    
    private final static Logger log = LoggerFactory.getLogger(UpdateSalesCountJob.class);

    @Override
    public void doJob() throws Exception {
        
        log.info("start UpdateSalesCountJob ............"+APIConfig.get().getApp());
        if (APIConfig.get().getApp() != 12266732) {
            return;
        }
 
        new UserBatchOper(256) {
            @Override
            public void doForEachUser(User user) {
                try {
                    if (user == null || !user.isVaild()) {
                        return;
                    }
                    
                    if(!user.getUserNick().contains("测试")&&!user.getUserNick().contains("淘宝")&&!user.getUserNick().contains("开放平台")
                            &&!user.getUserNick().contains("support")&&!user.getUserNick().contains("包u吧")&&!user.getUserNick().contains("包邮吧")
                            &&!user.getUserNick().contains("楚之小南")&&!user.getUserNick().contains("jy87771107")){
                        return;
                    }
                    
                    List<Promotion> promotionList=PromotionAction.findPromotionAllByUserId(user.getId());
                    
                    for(Promotion promotion : promotionList){
                        ItemPlay item = ItemDao.findByNumIid(user.getId(),promotion.getNumIid());
                        long salesCount = item.getSalesCount();
                        new EveryDaySalesCount(item.getNumIid(),user.getId(),salesCount).jdbcSave();
                    }

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }.call();
    }


}
