package job.shop;

import java.util.Date;
import java.util.List;

import models.tmsearch.UpdateShopSalesLog;
import models.tmsearch.UserShopPlay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import play.jobs.On;
import utils.DateUtil;
import actions.catunion.UserShopSalesAction;
import actions.catunion.UserShopSalesAction.ShopSalesResult;

import com.ciaosir.client.CommonUtils;

import configs.TMConfigs;

@On("0 30 0 * * ?")
public class UpdateShopSalesJob extends Job {
    private static final Logger log = LoggerFactory.getLogger(UpdateShopSalesJob.class);
    
    private static final long IntervalTime = DateUtil.DAY_MILLIS;
    
    public void doJob() {
        if (TMConfigs.Is_Update_Shop_Sales == false) {
            log.error("update shop sales is not opened!!!!");
            return;
        }
        
        long startTs = System.currentTimeMillis();
        String startTime = DateUtil.sdf.format(new Date());
        log.info("start UpdateShopSales job at: " + startTime);
        
        long visitedTime = System.currentTimeMillis() - IntervalTime;
        //long visitedTime = DateUtil.formYestadyMillis();
        log.info("visitedTime: " + DateUtil.sdf.format(new Date(visitedTime)));
        List<UserShopPlay> userShopList = UserShopPlay.findByVisitedTs(visitedTime);
        if (!CommonUtils.isEmpty(userShopList)) {
            log.error("shop size: " + userShopList.size());
            for (UserShopPlay userShop : userShopList) {
                updateOneShop(userShop);
            }
        } else {
            log.error("shop need to update is empty!!!!!! ");
        }
        
        long endTs = System.currentTimeMillis();
        String endTime = DateUtil.sdf.format(new Date());
        log.info("end UpdateShopSales job at: " + endTime 
                + ", used time of " + (endTs - startTs) * 1.0 /1000 + "s");
        
        int shopSize = 0;
        if (!CommonUtils.isEmpty(userShopList)) {
            shopSize = userShopList.size();
            log.error("shop size: " + userShopList.size());
        }
        
        
        UpdateShopSalesLog updateLog = new UpdateShopSalesLog();
        updateLog.setStartTs(startTs);
        updateLog.setEndTs(endTs);
        updateLog.setUsedTime(endTs - startTs);
        updateLog.setShopNum(shopSize);
        
        updateLog.jdbcSave();
    }
    
    
    public static boolean updateOneShop(UserShopPlay userShop) {
        try {
            long startTs = System.currentTimeMillis();
            ShopSalesResult result = UserShopSalesAction.updateUserShopSales(userShop);
            long endTs = System.currentTimeMillis();
            log.error("update shop: " + userShop.getNick() + " used time: " + (endTs - startTs) * 1.0 /1000 + "s");
            
            return result.isOk();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return false;
        }
    }
    
}
