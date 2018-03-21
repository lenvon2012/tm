
package actions.delist;

import java.util.ArrayList;
import java.util.List;

import job.autolist.AutoListRefreshItemJob.RefreshOneUserAutoList;
import job.autolist.service.ModifyListTime;
import models.autolist.AutoListConfig;
import models.autolist.AutoListLog;
import models.autolist.AutoListRecord;
import models.autolist.AutoListTime;
import models.autolist.NoAutoListItem;
import models.autolist.plan.UserDelistPlan;
import models.autolist.plan.UserDelistPlan.DelistItemStatusRule;
import models.autolist.plan.UserDelistPlan.DelistPlanStatus;
import models.autolist.plan.UserDelistPlan.DelistSalesNumRule;
import models.autolist.plan.UserDelistPlan.DelistTemplate;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actions.delist.DelistScheduleAction.DelistScheduleLog;

import com.ciaosir.client.CommonUtils;
import com.dbt.cred.utils.JsonUtil;

import dao.autolist.AutoListRecordDao;
import dao.autolist.AutoListTimeDao;

/**
 * 更新上下架分布
 * @author ying
 *
 */
public class DelistUpdateAction {

    private static final Logger log = LoggerFactory.getLogger(DelistUpdateAction.class);

    public static void doUpdateUserDelist(User user) {
        try {

            if (user == null || user.isVaild() == false) {
                return;
            }

            List<UserDelistPlan> delistPlanList = UserDelistPlan.findByUserId(user.getId());

            if (CommonUtils.isEmpty(delistPlanList)) {
                delistPlanList = new ArrayList<UserDelistPlan>();
            }

            for (UserDelistPlan delistPlan : delistPlanList) {
                if (delistPlan == null) {
                    continue;
                }

                doUpdateOneDelistPlan(user, delistPlan);

            }

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    public static void doUpdateOneDelistPlan(User user, UserDelistPlan delistPlan) {

        try {

            if (delistPlan.isPlanTurnOn() == false) {
                return;
            }

            if (delistPlan.isAutoAddNewItem() == false) {
                return;
            }

            DelistScheduleAction.doSchedule(user, delistPlan);

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

    }

    public static void updateDelistPlan(User user, UserDelistPlan delistPlan){
        try{
            if(delistPlan.isPlanTurnOn() == false){
                return;
            }
            DelistScheduleAction.doSchedule(user, delistPlan);
        }catch(Exception ex){
            log.error(ex.getMessage(), ex);
        }
    }
    /**
     * 检查是否存在默认的上下架计划
     * @param user
     */
    public static DelistScheduleLog checkDefaultDelistPlan(User user) {

        long startTime = System.currentTimeMillis();

        if (user == null) {
            return new DelistScheduleLog(false, "找不到用户！");
        }

        long delistCount = AutoListTimeDao.countByPlanId(0L, user.getId());

        long planCount = UserDelistPlan.countByUserId(user.getId());
        if (planCount > 0) {

            //AutoListTimeDao.deleteNoPlanIdItems(user.getId());

            if (delistCount > 0) {
                List<UserDelistPlan> delistPlanList = UserDelistPlan.findByUserId(user.getId());
                if (CommonUtils.isEmpty(delistPlanList)) {

                } else {
                    UserDelistPlan delistPlan = delistPlanList.get(0);

                    AutoListTimeDao.updateZeroPlanId(user.getId(), delistPlan.getPlanId());
                    NoAutoListItem.updateZeroPlanId(user.getId(), delistPlan.getPlanId());
                    AutoListLog.updateZeroPlanId(user.getId(), delistPlan.getPlanId());

                    long endTime = System.currentTimeMillis();
                    log.info("end checkDefaultDelistPlan for user: " + user.getUserNick() + ", used time "
                            + (endTime - startTime) + "ms------");

                    return new DelistScheduleLog(true);
                }

            } else {
                return new DelistScheduleLog(true);
            }

        }

        if (delistCount > 0) {
            UserDelistPlan delistPlan = new UserDelistPlan(user.getId(), "默认上下架计划");
            delistPlan.jdbcSave();
            delistPlan.setTemplateType(DelistTemplate.Default);
            int planStatus = 0;
            if (user.isAutoDelistOn()) {
                planStatus = DelistPlanStatus.ON;
            } else {
                planStatus = DelistPlanStatus.OFF;
            }
            delistPlan.setStatus(planStatus);
            delistPlan.setItemStatusRule(DelistItemStatusRule.OnSaleItems);
            delistPlan.setSalesNumRule(DelistSalesNumRule.AllItems);
            delistPlan.setDelistCateIds(UserDelistPlan.AllCateIds);
            delistPlan.setSelfCateIds(UserDelistPlan.AllCateIds);

            if (AutoListConfig.isRemainDelist(user.getId()) == false) {
                delistPlan.setAutoAddNewItem();
            } else {

            }

            delistPlan.setFilterGoodSalesItem();

            AutoListRecord record = AutoListRecordDao.findAutoListRecordByUserId(user.getId());

            if (record != null) {
                String schedule = record.getAutoListSchedule();
                if (StringUtils.isEmpty(schedule)) {
                    List<AutoListTime> timeList = AutoListTimeDao.findListTimeByPlanId(0L, user.getId());
                    ModifyListTime.setSchedule(record, timeList);
                    schedule = record.getAutoListSchedule();
                    AutoListRecordDao.saveOrUpdateAutoListRecord(record);

                }
                delistPlan.setDistriNums(record.getAutoListSchedule());
                delistPlan.setHourRates(record.getAutoListSchedule());
            } else {

                List<AutoListTime> timeList = AutoListTimeDao.findListTimeByPlanId(0L, user.getId());
                int[] nowDistri = RefreshOneUserAutoList.getNowDistri(timeList);
                String schedule = "";
                for (int i = 0; i < nowDistri.length; i++) {
                    if (!StringUtils.isEmpty(schedule))
                        schedule += ",";
                    schedule += nowDistri[i];
                }

                delistPlan.setDistriNums(schedule);
                delistPlan.setHourRates(schedule);
            }

            boolean isSuccess = delistPlan.jdbcSave();
            if (isSuccess == false) {
                return new DelistScheduleLog(false, "默认上下架计划创建失败！请联系我们！");
            }

            AutoListTimeDao.updateZeroPlanId(user.getId(), delistPlan.getPlanId());
            NoAutoListItem.updateZeroPlanId(user.getId(), delistPlan.getPlanId());
            AutoListLog.updateZeroPlanId(user.getId(), delistPlan.getPlanId());

            long endTime = System.currentTimeMillis();

            log.info("end checkDefaultDelistPlan for user: " + user.getUserNick() + ", used time "
                    + (endTime - startTime) + "ms------");

            return new DelistScheduleLog(true);

        } else {
            return new DelistScheduleLog(true);
        }

    }

    public static void main(String[] args) {
        UserDelistPlan delistPlan = new UserDelistPlan(222L, "默认上下架计划");
        delistPlan.setTemplateType(DelistTemplate.Default);
        int planStatus = 0;
        if (true) {
            planStatus = DelistPlanStatus.ON;
        } else {
            planStatus = DelistPlanStatus.OFF;
        }
        delistPlan.setStatus(planStatus);
        delistPlan.setItemStatusRule(DelistItemStatusRule.OnSaleItems);
        delistPlan.setSalesNumRule(DelistSalesNumRule.AllItems);
        delistPlan.setDelistCateIds(UserDelistPlan.AllCateIds);
        delistPlan.setSelfCateIds(UserDelistPlan.AllCateIds);
        delistPlan.setAutoAddNewItem();

        log.error(JsonUtil.getJson(delistPlan));

    }

}
