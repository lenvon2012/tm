
package controllers;

import actions.delist.*;
import actions.delist.DelistScheduleAction.DelistScheduleLog;
import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.NumberUtil;
import dao.autolist.AutoListLogDao;
import dao.autolist.AutoListTimeDao;
import dao.item.ItemDao;
import models.autolist.AutoListLog;
import models.autolist.AutoListTime;
import models.autolist.NoAutoListItem;
import models.autolist.plan.UserDelistPlan;
import models.autolist.plan.UserDelistPlan.DelistItemStatusRule;
import models.autolist.plan.UserDelistPlan.DelistPlanStatus;
import models.autolist.plan.UserDelistPlan.DelistSalesNumRule;
import models.autolist.plan.UserDelistPlan.DelistTemplate;
import models.item.ItemPlay;
import models.user.User;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import result.TMPaginger;
import result.TMResult;
import utils.DateUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DelistPlan extends TMController {

    private static final Logger log = LoggerFactory.getLogger(DelistPlan.class);

    public static void queryDelistPlan(long planId) {
        UserDelistPlan delistPlan = getDelistPlanById(planId);

        renderBusJson(delistPlan);
    }

    public static void queryDelistPlanList() throws IOException {

        User user = getUser();
        DelistUpdateAction.checkDefaultDelistPlan(user);

        //TMController.renderMockFileInJsonIfDev("delistplans.json");

        List<UserDelistPlan> planList = UserDelistPlan.findByUserId(user.getId());
        if (CommonUtils.isEmpty(planList)) {
            planList = ListUtils.EMPTY_LIST;
        }

        renderBusJson(planList);
    }

    private static String trimString(String str) {
        if (StringUtils.isEmpty(str)) {
            return "";
        }
        str = str.trim();

        return str;
    }

    //生成上下架计划
    public static void createDelistPlan(String title, long planId) {
        User user = getUser();

        title = trimString(title);

        if (StringUtils.isEmpty(title)) {
            renderFailedJson("上下架计划标题不能为空！");
        }

        UserDelistPlan delistPlan = null;
        if (planId <= 0) {
            delistPlan = new UserDelistPlan(user.getId(), title);
        } else {
            delistPlan = getDelistPlanById(planId);
            delistPlan.setTitle(title);
        }

        delistPlan.setStatus(DelistPlanStatus.OFF);

        //delistPlan.setAutoAddNewItem();

        boolean isSuccess = delistPlan.jdbcSave();
        if (isSuccess == false) {

            renderFailedJson("上下架计划提交失败，请联系我们！");
        }

        renderBusJson(delistPlan.getPlanId());
    }

    public static void createSimpleDelistPlan() {

        User user = getUser();

        String title = "均匀上架计划" + DateUtil.sdf.format(new Date());

        UserDelistPlan delistPlan = new UserDelistPlan(user.getId(), title);

        delistPlan.setStatus(DelistPlanStatus.OFF);
        delistPlan.setTemplateType(DelistTemplate.Default);

        delistPlan.setItemStatusRule(DelistItemStatusRule.OnSaleItems);

        int salesNumRule = DelistSalesNumRule.AllItems;
        delistPlan.setSalesNumRule(salesNumRule);

        delistPlan.setDelistCateIds(UserDelistPlan.AllCateIds);
        delistPlan.setSelfCateIds(UserDelistPlan.AllCateIds);
        delistPlan.setFilterGoodSalesItem();

        delistPlan.setSelectNumIids("");
        delistPlan.setAutoAddNewItem();

        //分布比例
        int[] hourRateArray = new int[7 * 24];
        for (int i = 0; i < hourRateArray.length; i++) {
            hourRateArray[i] = 0;
        }
        int[] hourArray = new int[] {
                9, 10, 11, 14, 15, 16, 19, 20, 21, 22
        };

        for (int i = 0; i < hourArray.length; i++) {
            int hourIndex = hourArray[i];
            for (int j = 0; j < 7; j++) {
                hourRateArray[j * 24 + hourIndex] = 1;
            }
        }

        String hourRates = DelistCalculateAction.arrToString(hourRateArray);
        delistPlan.setHourRates(hourRates);

        boolean isSuccess = delistPlan.jdbcSave();
        if (isSuccess == false) {

            renderFailedJson("上下架计划提交失败，请联系我们！");
        }

        DelistScheduleLog scheduleLog = DelistScheduleAction.doSchedule(user, delistPlan);
        if (scheduleLog.isSuccess() == false) {
            //计划回滚
            AutoListTimeDao.deleteByPlanId(delistPlan.getPlanId(), user.getId());
            delistPlan.rawDelete();

            renderFailedJson(scheduleLog.getMessage());
        }

        delistPlan.setStatus(DelistPlanStatus.ON);

        delistPlan.jdbcSave();

        renderSuccessJson();
    }

    //提交上下架计划的配置
    public static void setDelistPlanConfig(long planId, boolean isFilterGoodSalesItem, int itemStatusRule,
            String delistCateIds, String selfCateIds, String notDelistNumIids) {

        delistCateIds = trimString(delistCateIds);
        selfCateIds = trimString(selfCateIds);

        if (StringUtils.isEmpty(delistCateIds)) {
            delistCateIds = UserDelistPlan.AllCateIds;
        }
        if (StringUtils.isEmpty(selfCateIds)) {
            selfCateIds = UserDelistPlan.AllCateIds;
        }

        UserDelistPlan delistPlan = getDelistPlanById(planId);

        delistPlan.setTemplateType(DelistTemplate.Default);

        delistPlan.setItemStatusRule(itemStatusRule);

        int salesNumRule = DelistSalesNumRule.AllItems;
        delistPlan.setSalesNumRule(salesNumRule);

        delistPlan.setDelistCateIds(delistCateIds);
        delistPlan.setSelfCateIds(selfCateIds);
        if (isFilterGoodSalesItem == true) {
            delistPlan.setFilterGoodSalesItem();
        } else {
            delistPlan.removeFilterGoodSalesItem();
        }

        delistPlan.setSelectNumIids("");
        delistPlan.setAutoAddNewItem();

        User user = getUser();

        boolean isSuccess = delistPlan.jdbcSave();
        if (isSuccess == false) {

            renderFailedJson("上下架计划提交失败，请联系我们！");
        }

        //先去掉原来设置的排除宝贝
        NoAutoListItem.removeAll(user, delistPlan.getPlanId());
        if (!StringUtils.isEmpty(notDelistNumIids)) {
            Set<Long> numIidSet = toSet(notDelistNumIids);

            DelistScheduleLog scheduleLog = DelistModifyAction.addNoDelistItems(user, delistPlan, numIidSet);

            if (scheduleLog.isSuccess() == false) {
                renderFailedJson(scheduleLog.getMessage());
            }
        }

        renderSuccessJson();
    }

    //提交自定义选择的宝贝id
    public static void setUserSelectNumIids(long planId, String numIids) {
        numIids = trimString(numIids);

        if (StringUtils.isEmpty(numIids)) {
            renderFailedJson("请先选择要自动上下架的宝贝！");
        }

        UserDelistPlan delistPlan = getDelistPlanById(planId);

        delistPlan.setTemplateType(DelistTemplate.UserSelectItems);
        delistPlan.setSelectNumIids(numIids);
        delistPlan.removeAutoAddNewItem();

        delistPlan.removeFilterGoodSalesItem();

        boolean isSuccess = delistPlan.jdbcSave();
        if (isSuccess == false) {

            renderFailedJson("上下架计划提交失败，请联系我们！");
        }

        User user = getUser();

        NoAutoListItem.removeAll(user, delistPlan.getPlanId());

        renderSuccessJson();
    }

    public static void isOn(long planId) {
        UserDelistPlan delistPlan = getDelistPlanById(planId);

        boolean isOn = delistPlan.isPlanTurnOn();

        renderBusJson(isOn);

    }

    public static void turnOnPlan(Long planId) {

        UserDelistPlan delistPlan = getDelistPlanById(planId);

        delistPlan.setStatus(DelistPlanStatus.ON);

        boolean isSuccess = delistPlan.jdbcSave();
        if (isSuccess == false) {
            renderFailedJson("上下架计划开启失败，请联系我们！");
        }
        renderSuccessJson();
    }

    public static void turnOffPlan(Long planId) {

        UserDelistPlan delistPlan = getDelistPlanById(planId);

        delistPlan.setStatus(DelistPlanStatus.OFF);

        boolean isSuccess = delistPlan.jdbcSave();
        if (isSuccess == false) {
            renderFailedJson("上下架计划关闭失败，请联系我们！");
        }

        renderSuccessJson();
    }

    public static void deletePlan(Long planId) {

        User user = getUser();
        UserDelistPlan delistPlan = getDelistPlanById(planId);

        long count = AutoListTimeDao.countByPlanId(delistPlan.getPlanId(), user.getId());
        boolean isSuccess = AutoListTimeDao.deleteByPlanId(delistPlan.getPlanId(), user.getId());
        if (count > 0 && isSuccess == false) {
            renderFailedJson("上下架分布记录删除失败，请联系我们！");
        }

        NoAutoListItem.removeAll(user, delistPlan.getPlanId());
        //AutoListLog.deleteByPlanId(user.getId(), delistPlan.getPlanId());

        isSuccess = delistPlan.rawDelete();
        if (isSuccess == false) {
            renderFailedJson("上下架计划删除失败，请联系我们！");
        }

        renderSuccessJson();
    }

    private static UserDelistPlan getDelistPlanById(Long planId) {

        if (planId == null || planId <= 0) {
            renderFailedJson("系统出现异常，计划ID为空，请联系我们！");
        }

        User user = getUser();

        UserDelistPlan delistPlan = UserDelistPlan.findByPlanId(planId, user.getId());
        if (delistPlan == null) {
            renderFailedJson("系统出现异常，找不到相应的上下架计划，请联系我们！");
        }

        return delistPlan;
    }

    public static void addNoDelistItems(String numIids, long planId) {

        if (StringUtils.isEmpty(numIids)) {
            renderFailedJson("请先选择要排除的宝贝！");
        }

        if (planId <= 0) {
            renderFailedJson("系统出现异常，找不到上下架计划，请联系我们！");
        }

        String[] idStrArray = numIids.split(",");
        if (ArrayUtils.isEmpty(idStrArray)) {
            renderFailedJson("请先选择要排除的宝贝！");
        }

        User user = getUser();

        UserDelistPlan delistPlan = UserDelistPlan.findByPlanId(planId, user.getId());
        if (delistPlan == null) {
            renderFailedJson("系统出现异常，找不到相应的计划，请联系我们！");
        }

        Set<Long> numIidSet = toSet(numIids);

        DelistScheduleLog scheduleLog = DelistModifyAction.addNoDelistItems(user, delistPlan, numIidSet);

        if (scheduleLog.isSuccess() == false) {
            renderFailedJson(scheduleLog.getMessage());
        } else {
            renderSuccessJson();
        }

    }

    public static void removeAllNoDelist(long planId) {

        User user = getUser();

        UserDelistPlan delistPlan = UserDelistPlan.findByPlanId(planId, user.getId());
        if (delistPlan == null) {
            renderFailedJson("系统出现异常，找不到相应的计划，请联系我们！");
        }

        DelistScheduleLog scheduleLog = DelistModifyAction.removeAllNoDelistItems(user, delistPlan);

        if (scheduleLog.isSuccess() == false) {
            renderFailedJson(scheduleLog.getMessage());
        } else {
            renderSuccessJson();
        }

    }

    public static void chooseItemsTMPaginger(String s, String excludeNumIids, int pn, int ps) {
        User user = getUser();

        PageOffset po = new PageOffset(pn, ps);
        Set<Long> numIidSet = new HashSet<Long>();

        Set<Long> excludeNumIidSet = toSet(excludeNumIids);
        numIidSet.addAll(excludeNumIidSet);

        TMResult tmRes = ItemDao.findByUserAndSearchWithExcludedIgnoreStatus(user.getId(), s, po, numIidSet);

        TMPaginger tm = new TMPaginger(po.getPn(), po.getPs(), tmRes.getCount(), (List) tmRes.getRes());

        renderJSON(JsonUtil.getJson(tm));
    }

    public static void chooseItems(long planId, String s, int pn, int ps, String cid, String sellerCid) {
        User user = getUser();
        Set<Long> idSet = NoAutoListItem.findNumIidsByUser(user.getId(), planId);

        PageOffset po = new PageOffset(pn, ps);
        //TMResult tmRes = ItemDao.findByUserAndSearchWithExcludedIgnoreStatus(user.getId(), s, po, idSet);
        TMResult tmRes = ItemDao.findByUserAndSearchWithExcludedInAllcids(user.getId(), s, po, idSet, cid, sellerCid);
        renderJSON(JsonUtil.getJson(tmRes));
    }

    public static void reDistribute(long planId, String hourRates, boolean isTurnOn) {
        if (planId <= 0) {
            renderFailedJson("系统出现异常，上下架计划ID为空！");
        }
        if (StringUtils.isEmpty(hourRates)) {
            renderFailedJson("请先设置宝贝的时间分布！");
        }
        User user = getUser();

        UserDelistPlan delistPlan = UserDelistPlan.findByPlanId(planId, user.getId());
        if (delistPlan == null) {
            renderFailedJson("系统出现异常，找不到相应的计划，请联系我们！");
        }
        delistPlan.setHourRates(hourRates);
        
        boolean isSuccess = delistPlan.jdbcSave();
        if (isSuccess == false) {

            renderFailedJson("上下架计划提交失败，请联系我们！");
        }

        AutoListTimeDao.deleteByPlanId(delistPlan.getPlanId(), user.getId());
        DelistScheduleLog scheduleLog = DelistScheduleAction.doSchedule(user, delistPlan);
        if (scheduleLog.isSuccess() == false) {

            renderFailedJson(scheduleLog.getMessage());
        }

        if (isTurnOn == true) {
            delistPlan.setStatus(DelistPlanStatus.ON);
            delistPlan.jdbcSave();
        }

        renderSuccessJson();
    }

    private static void checkPlanId(long planId) {
        if (planId <= 0) {

        }
    }

    public static void getTodayDelist(Long nowTime, int pn, int ps, long planId) {
        int pageSize = ps;
        int currentPage = pn;
        User user = getUser();

        checkPlanId(planId);

        List<AutoListTime> timeList = AutoListTimeDao.queryTodayList(nowTime, user.getId(),
                planId, currentPage, pageSize);
        long count = AutoListTimeDao.queryTodayListNum(nowTime, user.getId(), planId);
        for (AutoListTime autoListTime : timeList) {
            long relativeTime = autoListTime.getRelativeListTime();
            long weekStart = DateUtil.findThisWeekStart(nowTime);
            long listTime = relativeTime + weekStart;
            autoListTime.setListTime(listTime);
            autoListTime.initItemProp();
        }
        PageOffset po = new PageOffset(pn, ps, 10);
        TMResult res = new TMResult(timeList, (int) count, po);

        renderJSON(JsonUtil.getJson(res));
    }

    public static void getWeekDelist(Long nowTime, long planId, int pn, int ps) {

        checkPlanId(planId);

        int pageSize = ps;
        int currentPage = pn;
        User user = getUser();

        List<AutoListTime> timeList = AutoListTimeDao.queryWeekList(user.getId(), planId,
                currentPage, pageSize);
        for (AutoListTime autoListTime : timeList) {
            long relativeTime = autoListTime.getRelativeListTime();
            long weekStart = DateUtil.findThisWeekStart(nowTime);
            long listTime = relativeTime + weekStart;
            autoListTime.setListTime(listTime);
            autoListTime.initItemProp();
        }
        long count = AutoListTimeDao.queryWeekNum(user.getId(), planId);

        PageOffset po = new PageOffset(pn, ps, 10);
        TMResult res = new TMResult(timeList, (int) count, po);

        renderJSON(JsonUtil.getJson(res));
    }

    private static Set<Long> toSet(String numIids) {
        Set<Long> numIidSet = new HashSet<Long>();

        String[] idStrArray = numIids.split(",");

        for (String idStr : idStrArray) {
            long numIid = NumberUtil.parserLong(idStr, 0L);
            if (numIid < 0) {
                continue;
            }
            numIidSet.add(numIid);

        }

        return numIidSet;

    }

    public static void addNoAutoListItems(String numIids, long planId) {
        if (StringUtils.isEmpty(numIids)) {
            renderFailedJson("请先选择要排除的宝贝！");
        }

        if (planId <= 0) {
            renderFailedJson("系统出现异常，找不到上下架计划，请联系我们！");
        }

        String[] idStrArray = numIids.split(",");
        if (ArrayUtils.isEmpty(idStrArray)) {
            renderFailedJson("请先选择要排除的宝贝！");
        }

        User user = getUser();

        UserDelistPlan delistPlan = UserDelistPlan.findByPlanId(planId, user.getId());
        if (delistPlan == null) {
            renderFailedJson("系统出现异常，找不到相应的计划，请联系我们！");
        }

        Set<Long> numIidSet = toSet(numIids);

        DelistScheduleLog scheduleLog = DelistModifyAction.addNoDelistItems(user, delistPlan, numIidSet);

        if (scheduleLog.isSuccess() == false) {
            renderFailedJson(scheduleLog.getMessage());
        } else {
            renderSuccessJson();
        }
    }

    public static void removeNoDelist(String numIids, long planId) {

        if (StringUtils.isEmpty(numIids)) {
            renderFailedJson("请先选择要取消排除的宝贝！");
        }

        User user = getUser();

        UserDelistPlan delistPlan = UserDelistPlan.findByPlanId(planId, user.getId());
        if (delistPlan == null) {
            renderFailedJson("系统出现异常，找不到相应的计划，请联系我们！");
        }

        Set<Long> numIidSet = toSet(numIids);

        DelistScheduleLog scheduleLog = DelistModifyAction.removeNoDelistItems(user, delistPlan, numIidSet);

        if (scheduleLog.isSuccess() == false) {
            renderFailedJson(scheduleLog.getMessage());
        } else {
            renderSuccessJson();
        }

    }

    public static void getDelistLog(String title, Long nowTime, long planId, int pn, int ps) {
        int pageSize = ps;
        int currentPage = pn;

        checkPlanId(planId);

        PageOffset po = new PageOffset(pn, ps, 10);
        
        User user = getUser();
        
        Set<Long> numIidSet = new HashSet<Long>();
        if (!StringUtils.isEmpty(title)) {
            List<Long> numIidList = ItemDao.findNumIidsByTitle(user.getId(), title);
            if (CommonUtils.isEmpty(numIidList)) {
                TMResult res = new TMResult(new ArrayList<AutoListTime>(), 0, po);

                renderJSON(JsonUtil.getJson(res));
            }
            numIidSet.addAll(numIidList);
        }

        List<AutoListLog> timeList = AutoListLogDao.queryListLog(user.getId(), planId,
                numIidSet, currentPage, pageSize);
        for (AutoListLog listLog : timeList) {
            listLog.initItemProp();
        }
        long count = AutoListLogDao.queryListLogNum(user.getId(), planId, numIidSet);

        TMResult res = new TMResult(timeList, (int) count, po);

        renderJSON(JsonUtil.getJson(res));
    }

    public static void listNoAutoListItems(long planId, int pn, int ps) {
        User user = getUser();
        TMResult res = NoAutoListItem.findItemByUserId(user.getId(), planId, pn, ps);
        renderJSON(JsonUtil.getJson(res));
    }

    //根据标题查询上下架
    public static void queryDelistTimes(Long nowTime, long planId, String title, int pn, int ps) {

        checkPlanId(planId);

        PageOffset po = new PageOffset(pn, ps, 10);
        User user = getUser();

        String numIids = "";
        if (!StringUtils.isEmpty(title)) {
            List<Long> numIidList = ItemDao.findNumIidsByTitle(user.getId(), title);
            if (CommonUtils.isEmpty(numIidList)) {
                TMResult res = new TMResult(new ArrayList<AutoListTime>(), 0, po);

                renderJSON(JsonUtil.getJson(res));
            }
            numIids = StringUtils.join(numIidList, ",");
        }
        List<AutoListTime> timeList = AutoListTimeDao.queryByNumIids(user.getId(), planId, numIids,
                po);
        for (AutoListTime autoListTime : timeList) {
            long relativeTime = autoListTime.getRelativeListTime();
            long weekStart = DateUtil.findThisWeekStart(nowTime);
            long listTime = relativeTime + weekStart;
            autoListTime.setListTime(listTime);
            autoListTime.initItemProp();
        }
        long count = AutoListTimeDao.queryNumByNumIids(user.getId(), planId, numIids);

        TMResult res = new TMResult(timeList, (int) count, po);

        renderJSON(JsonUtil.getJson(res));
    }

    public static void modifyDelistTime(long numIid, long planId, int weekIndex, String timeStr) {
        User user = getUser();

        if (weekIndex < 0 || weekIndex > 6 || StringUtils.isBlank(timeStr)) {
            renderFailedJson("请先设置要修改的时间");
        }
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        int hour = 0;
        int minute = 0;
        int second = 0;
        try {
            Date date = sdf.parse(timeStr);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);
            second = calendar.get(Calendar.SECOND);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            renderFailedJson("修改失败，时间格式错误");
        }

        long newTime = 0L;
        newTime = 1L * weekIndex * DateUtil.DAY_MILLIS;
        newTime += hour * DateUtil.ONE_HOUR_MILLIS + minute * DateUtil.ONE_MINUTE_MILLIS
                + second * 1000L;

        UserDelistPlan delistPlan = UserDelistPlan.findByPlanId(planId, user.getId());
        if (delistPlan == null) {
            renderFailedJson("系统出现异常，找不到相应的计划，请联系我们！");
        }

        DelistScheduleLog scheduleLog = DelistModifyAction.modifyDelistTime(user, delistPlan, numIid, newTime);

        if (scheduleLog.isSuccess() == false) {
            renderFailedJson(scheduleLog.getMessage());
        }

        renderSuccessJson("该宝贝上架时间修改成功");

    }

    //根据选择的宝贝，模拟计算分布结果
    public static void calcuDistribute(long planId, String weeks, String hours) throws IOException {

//    	renderMockFileInJsonIfDev("calcuDistribute.json");
        UserDelistPlan delistPlan = getDelistPlanById(planId);

        if (StringUtils.isEmpty(hours)) {
            //renderFailedJson("请先选择要分布的时间段！");
        }

        Set<Long> hourSet = toSet(hours);
        if (CommonUtils.isEmpty(hourSet)) {
            //renderFailedJson("请先选择要分布的时间段！");
        }

        if (StringUtils.isEmpty(weeks)) {
            //renderFailedJson("请先选择要分布的时间段！");
        }
        Set<Long> weekSet = toSet(weeks);
        if (CommonUtils.isEmpty(weekSet)) {
            //renderFailedJson("请先选择要分布的时间段！");
        }

        User user = getUser();

        List<ItemPlay> itemList = DelistScheduleAction.findResultDelistItems(user, delistPlan);

        if (CommonUtils.isEmpty(itemList)) {
            itemList = new ArrayList<ItemPlay>();
        }

        //过滤已分布的宝贝，注意过滤顺序，这个不能在删除之前
        itemList = DelistFilterAction.filterExistItems(user, itemList);

        int totalSize = itemList.size();

        int[] hourRateArray = getHourRateArray(weekSet, hourSet);

        int[] distriNumArray = GoodDistriAction.genGoodDistriArray(hourRateArray, totalSize);

        int[][] scheduleArray = hourArrayToScheduleArray(distriNumArray);

        renderJSON(JsonUtil.getJson(scheduleArray));
    }

    private static int[][] hourArrayToScheduleArray(int[] distriNumArray) {
        int[][] scheduleArray = new int[25][7];
        for (int i = 0; i < 25; i++) {
            for (int j = 0; j < 7; j++) {
                scheduleArray[i][j] = 0;
            }
        }

        for (int hourIndex = 0; hourIndex < distriNumArray.length; hourIndex++) {
            int dayIndex = hourIndex / 24;
            if (dayIndex <= 0) {
                dayIndex = 7;
            }

            int relativeHourIndex = hourIndex % 24;

            scheduleArray[relativeHourIndex][dayIndex - 1] = distriNumArray[hourIndex];
            scheduleArray[24][dayIndex - 1] += distriNumArray[hourIndex];

        }

        return scheduleArray;
    }

    public static void queryDetailDistribute(long planId) {

        UserDelistPlan delistPlan = getDelistPlanById(planId);

        String distriNums = delistPlan.getDistriNums();

        int[] distriNumArray = DelistCalculateAction.parseToHourArray(distriNums);

        int[][] scheduleArray = hourArrayToScheduleArray(distriNumArray);

        renderJSON(JsonUtil.getJson(scheduleArray));

    }

    public static void findRemainItems(String s, String excludeNumIids, int pn, int ps) {
        User user = getUser();

        PageOffset po = new PageOffset(pn, ps);
        Set<Long> numIidSet = AutoListTimeDao.findNumIidsByUserId(user.getId());

        if (CommonUtils.isEmpty(numIidSet)) {
            numIidSet = new HashSet<Long>();
        }
        Set<Long> excludeNumIidSet = toSet(excludeNumIids);
        numIidSet.addAll(excludeNumIidSet);

        TMResult tmRes = ItemDao.findByUserAndSearchWithExcludedIgnoreStatus(user.getId(), s, po, numIidSet);

        TMPaginger tm = new TMPaginger(po.getPn(), po.getPs(), tmRes.getCount(), (List) tmRes.getRes());

        renderJSON(JsonUtil.getJson(tm));
    }

    public static void modifyPlanConfig(long planId, boolean isAutoAddNewItem, boolean isFilterGoodSalesItem) {

        User user = getUser();

        UserDelistPlan delistPlan = getDelistPlanById(planId);

        if (isAutoAddNewItem == true) {
            delistPlan.setAutoAddNewItem();
        } else {
            delistPlan.removeAutoAddNewItem();
        }

        if (isFilterGoodSalesItem == true) {
            delistPlan.setFilterGoodSalesItem();
        } else {
            delistPlan.removeFilterGoodSalesItem();
        }

        boolean isSuccess = delistPlan.jdbcSave();
        if (isSuccess == false) {
            renderFailedJson("上下架计划配置修改失败，请联系我们！");
        }

        DelistUpdateAction.doUpdateOneDelistPlan(user, delistPlan);

        renderSuccessJson();
    }

    public static void simpleReDistribute(long planId, String weeks, String hours) {

        UserDelistPlan delistPlan = getDelistPlanById(planId);

        if (StringUtils.isEmpty(hours)) {
            renderFailedJson("请先选择要分布的时间段！");
        }

        Set<Long> hourSet = toSet(hours);
        if (CommonUtils.isEmpty(hourSet)) {
            renderFailedJson("请先选择要分布的时间段！");
        }

        if (StringUtils.isEmpty(weeks)) {
            renderFailedJson("请先选择要分布的时间段！");
        }
        Set<Long> weekSet = toSet(weeks);
        if (CommonUtils.isEmpty(weekSet)) {
            renderFailedJson("请先选择要分布的时间段！");
        }

        User user = getUser();

        int[] hourRateArray = getHourRateArray(weekSet, hourSet);
        String hourRates = DelistCalculateAction.arrToString(hourRateArray);

        delistPlan.setHourRates(hourRates);

        boolean isSuccess = delistPlan.jdbcSave();
        if (isSuccess == false) {

            renderFailedJson("上下架计划提交失败，请联系我们！");
        }

        AutoListTimeDao.deleteByPlanId(delistPlan.getPlanId(), user.getId());
        DelistScheduleLog scheduleLog = DelistScheduleAction.doSchedule(user, delistPlan);
        if (scheduleLog.isSuccess() == false) {

            renderFailedJson(scheduleLog.getMessage());
        }

        renderSuccessJson();

    }

    private static int[] getHourRateArray(Set<Long> weekSet, Set<Long> hourSet) {
        int[] hourRateArray = new int[7 * 24];
        for (int i = 0; i < hourRateArray.length; i++) {
            hourRateArray[i] = 0;
        }

        for (Long week : weekSet) {
            int weekIndex = week.intValue();
            if (weekIndex == 7) {
                weekIndex = 0;
            }
            for (Long hour : hourSet) {
                int hourIndex = (int) hour.longValue() + weekIndex * 24;
                hourRateArray[hourIndex] = 1;
            }
        }

        return hourRateArray;
    }

    public static void fixDistriNums(long planId) {
        UserDelistPlan delistPlan = getDelistPlanById(planId);

        User user = getUser();
        List<AutoListTime> delistList = AutoListTimeDao.findListTimeByPlanId(delistPlan.getPlanId(), user.getId());

        String distriNums = DelistCalculateAction.calcuDistriNums(delistList);

        delistPlan.setDistriNums(distriNums);

        delistPlan.jdbcSave();

    }

    public static void fixClearDelist(long planId) {

        UserDelistPlan delistPlan = getDelistPlanById(planId);

        User user = getUser();

        AutoListTimeDao.deleteByPlanId(delistPlan.getPlanId(), user.getId());

    }

    public static void findRemainItemsNew(String s, String excludeNumIids, int pn, int ps) {
        User user = getUser();

        PageOffset po = new PageOffset(pn, ps);
        Set<Long> numIidSet = AutoListTimeDao.findNumIidsByUserId(user.getId());

        if (CommonUtils.isEmpty(numIidSet)) {
            numIidSet = new HashSet<Long>();
        }
        Set<Long> excludeNumIidSet = toSet(excludeNumIids);
        numIidSet.addAll(excludeNumIidSet);

        TMResult tmRes = ItemDao.findByUserAndSearchWithExcludedIgnoreStatus(user.getId(), s, po, numIidSet);

        renderJSON(JsonUtil.getJson(tmRes));
    }

    public static void chooseItemsTMPagingerNew(String s, String excludeNumIids, int pn, int ps) {
        User user = getUser();

        PageOffset po = new PageOffset(pn, ps);
        Set<Long> numIidSet = new HashSet<Long>();

        Set<Long> excludeNumIidSet = toSet(excludeNumIids);
        numIidSet.addAll(excludeNumIidSet);

        TMResult tmRes = ItemDao.findByUserAndSearchWithExcludedIgnoreStatus(user.getId(), s, po, numIidSet);

        renderJSON(JsonUtil.getJson(tmRes));
    }


    public static void setDelistPlanConfigAutoAddNew(long planId, boolean isFilterGoodSalesItem, boolean isAutoAddNewItem, int itemStatusRule,
                                           String delistCateIds, String selfCateIds, String notDelistNumIids) {

        delistCateIds = trimString(delistCateIds);
        selfCateIds = trimString(selfCateIds);

        if (StringUtils.isEmpty(delistCateIds)) {
            delistCateIds = UserDelistPlan.AllCateIds;
        }
        if (StringUtils.isEmpty(selfCateIds)) {
            selfCateIds = UserDelistPlan.AllCateIds;
        }

        UserDelistPlan delistPlan = getDelistPlanById(planId);

        delistPlan.setTemplateType(DelistTemplate.Default);

        delistPlan.setItemStatusRule(itemStatusRule);

        int salesNumRule = DelistSalesNumRule.AllItems;
        delistPlan.setSalesNumRule(salesNumRule);

        delistPlan.setDelistCateIds(delistCateIds);
        delistPlan.setSelfCateIds(selfCateIds);
        if (isFilterGoodSalesItem == true) {
            delistPlan.setFilterGoodSalesItem();
        } else {
            delistPlan.removeFilterGoodSalesItem();
        }

        delistPlan.setSelectNumIids("");
        if(isAutoAddNewItem == true)
            delistPlan.setAutoAddNewItem();

        User user = getUser();

        boolean isSuccess = delistPlan.jdbcSave();
        if (isSuccess == false) {

            renderFailedJson("上下架计划提交失败，请联系我们！");
        }

        //先去掉原来设置的排除宝贝
        NoAutoListItem.removeAll(user, delistPlan.getPlanId());
        if (!StringUtils.isEmpty(notDelistNumIids)) {
            Set<Long> numIidSet = toSet(notDelistNumIids);

            DelistScheduleLog scheduleLog = DelistModifyAction.addNoDelistItems(user, delistPlan, numIidSet);

            if (scheduleLog.isSuccess() == false) {
                renderFailedJson(scheduleLog.getMessage());
            }
        }

        renderSuccessJson();
    }
    
    public static void chooseItemsCategory(String s, String itemCats, String sellerCats, int pn, int ps) {
        User user = getUser();

        PageOffset po = new PageOffset(pn, ps);

        TMResult tmRes = ItemDao.findByUserSearchCats(user.getId(),itemCats,sellerCats, s, po);

        renderJSON(JsonUtil.getJson(tmRes));
    }

    public static void setCateConfig(boolean isFilterGoodSalesItem, boolean isAutoAddNewItem,String title, String delistCateIds, String selfCateIds){
        log.info("--------------------------------------------------------------------------setCateConfig delistCateIds:" + delistCateIds + " selfCateIds:" + selfCateIds);
        User user = getUser();

        title = trimString(title);

        if (StringUtils.isEmpty(title)) {
            renderFailedJson("上下架计划标题不能为空！");
        }

        UserDelistPlan delistPlan = new UserDelistPlan(user.getId(), title);

        delistPlan.setStatus(DelistPlanStatus.OFF);

        if(StringUtils.isNotEmpty(delistCateIds)){
            delistPlan.setDelistCateIds(delistCateIds);
        }else{
            delistPlan.setDelistCateIds(UserDelistPlan.AllCateIds);
        }

        if(StringUtils.isNotEmpty(selfCateIds)){
            delistPlan.setSelfCateIds(selfCateIds);
        }else{
            delistPlan.setSelfCateIds(UserDelistPlan.AllCateIds);
        }

        if (isFilterGoodSalesItem == true) {
            delistPlan.setFilterGoodSalesItem();
        } else {
            delistPlan.removeFilterGoodSalesItem();
        }

        if(isAutoAddNewItem == true)
            delistPlan.setAutoAddNewItem();
        else{
            delistPlan.removeAutoAddNewItem();
        }
        boolean isSuccess = delistPlan.jdbcSave();

        if(isSuccess == false){
            renderFailedJson("上下架计划提交失败，请联系我们！");
        }
        renderBusJson(delistPlan.getPlanId());
    }

    public static void setDelistConfig(String title, boolean isFilterGoodSalesItem, boolean isAutoAddNewItem){
        User user = getUser();

        title = trimString(title);

        if (StringUtils.isEmpty(title)) {
            renderFailedJson("上下架计划标题不能为空！");
        }

        UserDelistPlan delistPlan = new UserDelistPlan(user.getId(), title);

        delistPlan.setStatus(DelistPlanStatus.OFF);


        if (isFilterGoodSalesItem == true) {
            delistPlan.setFilterGoodSalesItem();
        } else {
            delistPlan.removeFilterGoodSalesItem();
        }

        if(isAutoAddNewItem == true)
            delistPlan.setAutoAddNewItem();
        else{
            delistPlan.removeAutoAddNewItem();
        }

        boolean isSuccess = delistPlan.jdbcSave();

        if(isSuccess == false){
            renderFailedJson("上下架计划提交失败，请联系我们！");
        }
        renderBusJson(delistPlan.getPlanId());

    }


    public static void getAllItems(long planId, String s, int pn, int ps, String cid, String sellerCid){
        User user = getUser();
        Set<Long> idSet = NoAutoListItem.findNumIidsByUser(user.getId(), planId);
        PageOffset po = new PageOffset(pn, ps);
        TMResult tmRes = ItemDao.findByUserAndSearchWithExcludedItems(user.getId(), s, po, idSet, cid, sellerCid);
        renderJSON(JsonUtil.getJson(tmRes));
    }

    public static void getNoDelistItems(long planId){
        User user = getUser();

        Set<Long> noDelistItems = NoAutoListItem.findNumIidsByUser(user.getId(), planId);

        renderBusJson(noDelistItems);

    }

   public static void addNoDelistItem(long planId, long numIid){
       User user = getUser();

       boolean isSuccess = new NoAutoListItem(user.getId(), numIid, planId).jdbcSave();

       renderJSON(isSuccess);

   }

   public static void removeNoDelistItems(long planId, long numIid){
       User user = getUser();

       NoAutoListItem.remove(user, numIid, planId);
       renderSuccessJson();
   }

    public static void configExcludeItems(long planId){
        User user = getUser();
        UserDelistPlan delistPlan = getDelistPlanById(planId);

        Set<Long> numIidSet = NoAutoListItem.findNumIidsByUser(user.getId(), planId);
        if(CommonUtils.isEmpty(numIidSet)){
            return;
        }

        DelistScheduleLog scheduleLog = DelistModifyAction.deleteDelists(user, delistPlan, numIidSet);
        if (scheduleLog.isSuccess() == false) {
            renderFailedJson(scheduleLog.getMessage());
        }
        renderSuccessJson();
    }
}
