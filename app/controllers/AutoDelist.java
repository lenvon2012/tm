
package controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import job.autolist.BackToOldDelistAction;
import job.autolist.service.ModifyListTime;
import models.autolist.AutoListConfig;
import models.autolist.AutoListConfig.DelistConfig;
import models.autolist.AutoListLog;
import models.autolist.AutoListRecord;
import models.autolist.AutoListTime;
import models.autolist.NoAutoListItem;
import models.item.ItemPlay;
import models.user.User;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import result.TMResult;
import utils.DateUtil;
import bustbapi.ItemApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.NumberUtil;
import com.taobao.api.domain.Item;

import dao.autolist.AutoListLogDao;
import dao.autolist.AutoListRecordDao;
import dao.autolist.AutoListTimeDao;
import dao.item.ItemDao;

public class AutoDelist extends TMController {
    private static final Logger log = LoggerFactory.getLogger(AutoDelist.class);

    public static void turnOn(int distriType, String[] distriTimeArray, String distriHours) {
        User user = getUser();
        Long userId = user.getId();

        checkIsZhizun();

        AutoListRecord record = AutoListRecordDao.findAutoListRecordByUserId(userId);
        boolean isSuccess = true;
        //第一次，要新建，并进行初始计算
        if (record == null) {

            setDelistConfig(DelistConfig.Smart_Distri);

            record = AutoListRecord.createAutoListRecord(userId);
            record.setIsCalcuComplete(false);
            record.setDistriType(distriType);
            record.setDistriHours(distriHours);
            if (distriTimeArray != null) {
                String distriTime = "";
                for (int i = 0; i < distriTimeArray.length; i++) {
                    if (i > 0)
                        distriTime += ",";
                    distriTime += distriTimeArray[i];

                }
                record.setDistriTime(distriTime);
            }
            isSuccess = AutoListRecordDao.saveOrUpdateAutoListRecord(record);
            if (isSuccess == false) {
                renderJSON(JsonUtil.getJson(new TMResult(false, null, false)));
            }
            //这个要在save之后
            //new AutoListInitJob(record).doJob();
        }
        //打开
        record.setIsTurnOn(true);
        user.setAutoDelistOn(true);
        isSuccess = user.jdbcSave();
        isSuccess = isSuccess && AutoListRecordDao.saveOrUpdateAutoListRecord(record);
        renderJSON(JsonUtil.getJson(new TMResult(isSuccess, null, isSuccess)));
    }
    
    public static void simpleTurnOn() {
        User user = getUser();
        user.setAutoDelistOn(true);
        
        user.jdbcSave();
        
        renderBusJson(true);
    }
    
    public static void simpleTurnOff() {
        User user = getUser();
        user.setAutoDelistOn(false);
        
        user.jdbcSave();
        
        renderBusJson(true);
    }

    public static void chooseItems(int pn, int ps, String s) {
        User user = getUser();
        Set<Long> ids1 = NoAutoListItem.findNumIidsByUser(user.getId(), AutoListTime.DefaultPlanId);

        PageOffset po = new PageOffset(pn, ps);
        TMResult tmRes = ItemDao.findByUserAndSearchWithExcluded(user.getId(), s, po, ids1);
        renderJSON(JsonUtil.getJson(tmRes));
    }

    public static void listNoAutoListItems(int pn, int ps) {
        User user = getUser();
        TMResult res = NoAutoListItem.findItemByUserId(user.getId(), AutoListTime.DefaultPlanId, pn, ps);
        renderJSON(JsonUtil.getJson(res));
    }
    
    public static void showChooseItem(String numIids) {
        final User user = getUser();
        List<ItemPlay> res = ItemDao.findByNumIidList(user, numIids);
        renderJSON(JsonUtil.getJson(res));
    }

    public static void removeAllNoAutoListItems() {
        User user = getUser();
        //
        Set<Long> numIidSet = NoAutoListItem.findNumIidsByUser(user.getId(), AutoListTime.DefaultPlanId);
        boolean isSuccess = NoAutoListItem.removeAll(user, AutoListTime.DefaultPlanId);
        if (isSuccess == true) {
            for (Long numIid : numIidSet) {
                //AutoListTime listTime = AutoListTime.find("numIid=? and userId=?", item.getNumIid(), user.getId())
                //        .first();
                AutoListTime listTime = AutoListTimeDao.queryByNumIidWithJDBC(user.getId(), numIid);
                if (listTime != null)
                    ;
                else
                    ModifyListTime.addToSchedule(user.getId(), numIid);
            }
        }

        renderJSON(JsonUtil.getJson(new TMResult(isSuccess)));
    }

    private static void setDelistConfig(int config) {

        User user = getUser();

        AutoListConfig listCfg = AutoListConfig.findByUserId(user.getId());
        if (listCfg == null) {
            listCfg = new AutoListConfig(user.getId(), config);
        } else {
            listCfg.setConfig(config);
        }
        listCfg.jdbcSave();
    }

    public static void reDistribute(int distriType, String[] distriTimeArray, String distriHours) {
        User user = getUser();
        Long userId = user.getId();

        AutoListRecord record = AutoListRecordDao.findAutoListRecordByUserId(userId);
        boolean isSuccess = true;
        //第一次，要新建，并进行初始计算
        if (record == null) {
            record = AutoListRecord.createAutoListRecord(userId);
        }

        setDelistConfig(DelistConfig.Smart_Distri);

        if (distriTimeArray != null) {
            String distriTime = "";
            for (int i = 0; i < distriTimeArray.length; i++) {
                if (i > 0)
                    distriTime += ",";
                distriTime += distriTimeArray[i];

            }
            record.setDistriTime(distriTime);
        }
        record.setIsCalcuComplete(false);
        record.setDistriType(distriType);
        record.setDistriHours(distriHours);
        isSuccess = AutoListRecordDao.saveOrUpdateAutoListRecord(record);
        if (isSuccess == false) {
            renderJSON(JsonUtil.getJson(new TMResult(false, null, false)));
        }
        //这个要在save之后
        //new AutoListInitJob(record).doJob();
    }

    public static void backToOldDelist() {
        User user = getUser();

        AutoListRecord record = AutoListRecordDao.findAutoListRecordByUserId(user.getId());

        if (record == null) {
            renderError("您尚未开启自动上下架功能！");
        }

        setDelistConfig(DelistConfig.Remain_Old);

        BackToOldDelistAction.doBackToOldDelist(user);

        record.setUpdateTime(System.currentTimeMillis());

        List<AutoListTime> timeList = AutoListTimeDao.queryAllAutoListTime(user.getId());
        ModifyListTime.setSchedule(record, timeList);

        AutoListRecordDao.saveOrUpdateAutoListRecord(record);

        renderSuccess("计划清空成功，目前计划中展现宝贝真实上下架时间！", null);
    }

    public static void hasSetDelist() {
        User user = getUser();

        AutoListRecord record = AutoListRecordDao.findAutoListRecordByUserId(user.getId());

        if (record == null) {
            renderSuccess("", false);
        } else {
            renderSuccess("", true);
        }
    }

    public static void getDistriType() {
        User user = getUser();
        Long userId = user.getId();

        AutoListRecord record = AutoListRecordDao.findAutoListRecordByUserId(userId);
        //第一次，要新建，并进行初始计算
        if (record == null) {
            renderJSON(new DistriInfo(0, false, null, ""));
        } else if (record.getIsCalcuComplete() == false) {
            renderJSON(new DistriInfo(record.getDistriType(), false, record.getDistriTime(), record.getDistriHours()));
        }
        renderJSON(new DistriInfo(record.getDistriType(), true, record.getDistriTime(), record.getDistriHours()));
    }

    public static class DistriInfo {
        private int state;

        private String distriHours;

        private boolean isShowBtn;

        private boolean[] isDistriTime = new boolean[7];//一周中哪几天上架

        public DistriInfo(int state, boolean isShowBtn, String distriTime, String distriHours) {
            super();
            this.state = state;
            this.isShowBtn = isShowBtn;
            if (!StringUtils.isEmpty(distriTime)) {
                String[] distriTimeArray = StringUtils.split(distriTime, ",");
                for (int i = 0; i < isDistriTime.length; i++) {
                    int isDistri = 1;
                    if (i < distriTimeArray.length)
                        isDistri = NumberUtil.parserInt(distriTimeArray[i], 0);
                    if (isDistri > 0)
                        isDistriTime[i] = true;
                    else
                        isDistriTime[i] = false;
                }
            } else {
                for (int i = 0; i < isDistriTime.length; i++)
                    isDistriTime[i] = true;
            }

            this.distriHours = distriHours;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }

        public boolean isShowBtn() {
            return isShowBtn;
        }

        public void setShowBtn(boolean isShowBtn) {
            this.isShowBtn = isShowBtn;
        }

        public boolean[] getIsDistriTime() {
            return isDistriTime;
        }

        public void setIsDistriTime(boolean[] isDistriTime) {
            this.isDistriTime = isDistriTime;
        }

        public String getDistriHours() {
            return distriHours;
        }

        public void setDistriHours(String distriHours) {
            this.distriHours = distriHours;
        }

    }

    public static void turnOff() {
        User user = getUser();
        Long userId = user.getId();
        AutoListRecord record = AutoListRecordDao.findAutoListRecordByUserId(userId);
        boolean isSuccess = true;
        if (record != null) {
            record.setIsTurnOn(false);
            isSuccess = isSuccess && AutoListRecordDao.saveOrUpdateAutoListRecord(record);
        }
        user.setAutoDelistOn(false);
        isSuccess = isSuccess && user.jdbcSave();

        renderJSON(JsonUtil.getJson(new TMResult(isSuccess, null, isSuccess)));
    }

    private static boolean isAutoDelistOn(User user) {

        boolean isOn = user.isAutoDelistOn();
//		AutoListRecord record = AutoListRecordDao.findAutoListRecordByUserId(user.getId());
//		if (record == null)
//			return false;
        return isOn;
    }

    public static void isOn() {
        User user = getUser();
        renderJSON(JsonUtil.getJson(new TMResult(true, null, isAutoDelistOn(user))));
    }

    public static void getAutoDelistInfo(long nowTime) {
        User user = getUser();

        AutoDelistInfo listInfo = new AutoDelistInfo(user);
        listInfo.calcuInfo(nowTime);

        //log.info(JsonUtil.getJson(listInfo));

        renderJSON(JsonUtil.getJson(listInfo));
    }

    public static void getTodayDelist(Long nowTime, int pn, int ps) {
        int pageSize = ps;
        int currentPage = pn;
        User user = getUser();
        AutoListRecord record = AutoListRecordDao.findAutoListRecordByUserId(user.getId());
        if (record == null || record.getIsCalcuComplete() == false) {
            DelistPaging pageInfo = new DelistPaging(0, new ArrayList<AutoListTime>());
            renderJSON(JsonUtil.getJson(pageInfo));
        }
        List<AutoListTime> timeList = AutoListTimeDao.queryTodayList(nowTime, user.getId(),
                record.getCreateTime(), currentPage, pageSize);
        long count = AutoListTimeDao.queryTodayListNum(nowTime, user.getId(), record.getCreateTime());
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

    public static void getWeekDelist(Long nowTime, int pn, int ps) {
        int pageSize = ps;
        int currentPage = pn;
        User user = getUser();
        AutoListRecord record = AutoListRecordDao.findAutoListRecordByUserId(user.getId());
        if (record == null || record.getIsCalcuComplete() == false) {
            DelistPaging pageInfo = new DelistPaging(0, new ArrayList<AutoListTime>());
            renderJSON(JsonUtil.getJson(pageInfo));
        }
        List<AutoListTime> timeList = AutoListTimeDao.queryWeekList(user.getId(), findDefautlPlanId(),
                currentPage, pageSize);
        for (AutoListTime autoListTime : timeList) {
            long relativeTime = autoListTime.getRelativeListTime();
            long weekStart = DateUtil.findThisWeekStart(nowTime);
            long listTime = relativeTime + weekStart;
            autoListTime.setListTime(listTime);
            autoListTime.initItemProp();
        }
        long count = AutoListTimeDao.queryWeekNum(user.getId(), findDefautlPlanId());

        PageOffset po = new PageOffset(pn, ps, 10);
        TMResult res = new TMResult(timeList, (int) count, po);

        renderJSON(JsonUtil.getJson(res));
    }

    public static void addNoAutoListItems(String numIids) {
        User user = getUser();
        if (StringUtils.isEmpty(numIids)) {
            ok();
        }
        String[] idStrings = StringUtils.split(numIids, ",");
        if (ArrayUtils.isEmpty(idStrings)) {
            ok();
        }
        int[] deleteHourNumArray = new int[7 * 24];
        for (int i = 0; i < 7 * 24; i++)
            deleteHourNumArray[i] = 0;
        for (String string : idStrings) {
            NoAutoListItem.add(user, NumberUtil.parserLong(string, 0L), AutoListTime.DefaultPlanId);
            long numIid = NumberUtil.parserLong(string, 0L);
            //List<AutoListTime> autoListTimeList = AutoListTime.find("userId=? and numIid=?", user.getId(), numIid)
            //        .fetch();
            List<AutoListTime> autoListTimeList = AutoListTimeDao.queryListByNumIidWithJDBC(user.getId(), numIid);
            if (autoListTimeList == null || autoListTimeList.isEmpty())
                continue;
            for (AutoListTime autoListTime : autoListTimeList) {
                long relativeTime = autoListTime.getRelativeListTime();
                int hour = (int) (relativeTime / DateUtil.ONE_HOUR_MILLIS);
                deleteHourNumArray[hour]++;
                AutoListTimeDao.deleteAutoListTime(autoListTime);
            }
        }
        ModifyListTime.deleteFromSchedule(user.getId(), deleteHourNumArray);

        TMResult.renderMsg(StringUtils.EMPTY);
    }

    public static void removeNoAutoListItem(long numIid) {
        User user = getUser();
        NoAutoListItem.remove(user, numIid, AutoListTime.DefaultPlanId);
        //AutoListTime listTime = AutoListTime.find("numIid=? and userId=?", numIid, user.getId()).first(); 
        AutoListTime listTime = AutoListTimeDao.queryByNumIidWithJDBC(user.getId(), numIid);
        if (listTime != null)
            TMResult.renderMsg(StringUtils.EMPTY);
        ModifyListTime.addToSchedule(user.getId(), numIid);
        TMResult.renderMsg(StringUtils.EMPTY);
    }

    public static void getDelistLog(String title, Long nowTime, int pn, int ps) {
        int pageSize = ps;
        int currentPage = pn;
        
        PageOffset po = new PageOffset(pn, ps, 10);
        
        User user = getUser();
        AutoListRecord record = AutoListRecordDao.findAutoListRecordByUserId(user.getId());
        if (record == null || record.getIsCalcuComplete() == false) {
            ListLogPaging pageInfo = new ListLogPaging(0, new ArrayList<AutoListLog>());
            renderJSON(JsonUtil.getJson(pageInfo));
        }
        
        Set<Long> numIidSet = new HashSet<Long>();
        if (!StringUtils.isEmpty(title)) {
            List<Long> numIidList = ItemDao.findNumIidsByTitle(user.getId(), title);
            if (CommonUtils.isEmpty(numIidList)) {
                TMResult res = new TMResult(new ArrayList<AutoListTime>(), 0, po);

                renderJSON(JsonUtil.getJson(res));
            }
            numIidSet.addAll(numIidList);
        }

        List<AutoListLog> timeList = AutoListLogDao.queryListLog(user.getId(), findDefautlPlanId(),
                numIidSet, currentPage, pageSize);
        for (AutoListLog listLog : timeList) {
            listLog.initItemProp();
        }
        long count = AutoListLogDao.queryListLogNum(user.getId(), findDefautlPlanId(), numIidSet);

        
        TMResult res = new TMResult(timeList, (int) count, po);

        renderJSON(JsonUtil.getJson(res));
    }

    //判断是否已经进行过上下架计划的分布
    public static void hasInitDelistTimes() {
        User user = getUser();
        AutoListRecord record = AutoListRecordDao.findAutoListRecordByUserId(user.getId());
        if (record == null) {
            renderJSON(false);
        }
        renderJSON(true);
    }

    //根据标题查询上下架
    public static void queryDelistTimes(Long nowTime, String title, int pn, int ps) {
        int pageSize = ps;
        int currentPage = pn;
        PageOffset po = new PageOffset(pn, ps, 10);
        User user = getUser();
        AutoListRecord record = AutoListRecordDao.findAutoListRecordByUserId(user.getId());
        if (record == null || record.getIsCalcuComplete() == false) {
            DelistPaging pageInfo = new DelistPaging(0, new ArrayList<AutoListTime>());
            renderJSON(JsonUtil.getJson(pageInfo));
        }
        String numIids = "";
        if (!StringUtils.isEmpty(title)) {
            List<Long> numIidList = ItemDao.findNumIidsByTitle(user.getId(), title);
            if (CommonUtils.isEmpty(numIidList)) {
                TMResult res = new TMResult(new ArrayList<AutoListTime>(), 0, po);

                renderJSON(JsonUtil.getJson(res));
            }
            numIids = StringUtils.join(numIidList, ",");
        }
        List<AutoListTime> timeList = AutoListTimeDao.queryByNumIids(user.getId(), findDefautlPlanId(), numIids,
                po);
        for (AutoListTime autoListTime : timeList) {
            long relativeTime = autoListTime.getRelativeListTime();
            long weekStart = DateUtil.findThisWeekStart(nowTime);
            long listTime = relativeTime + weekStart;
            autoListTime.setListTime(listTime);
            autoListTime.initItemProp();
        }
        long count = AutoListTimeDao.queryNumByNumIids(user.getId(), findDefautlPlanId(), numIids);

        TMResult res = new TMResult(timeList, (int) count, po);

        renderJSON(JsonUtil.getJson(res));
    }
    
    private static long findDefautlPlanId() {
        return 0;
    }

    public static void modifyDelistTime(long numIid, int weekIndex, String timeStr) {
        User user = getUser();
        AutoListTime autoListTime = AutoListTimeDao.queryByNumIid(user.getId(), findDefautlPlanId(), numIid);
        if (autoListTime == null) {
            renderJSON(JsonUtil.getJson(new DelistMessage(false, "修改失败，找不到对应的上下架计划")));
        }
        if (weekIndex < 0 || weekIndex > 6 || StringUtils.isBlank(timeStr)) {
            renderJSON(JsonUtil.getJson(new DelistMessage(false, "请先设置要修改的时间")));
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
            renderJSON(JsonUtil.getJson(new DelistMessage(false, "修改失败，时间格式错误")));
        }

        long listTime = 0L;
        listTime = 1L * weekIndex * DateUtil.DAY_MILLIS;
        listTime += hour * DateUtil.ONE_HOUR_MILLIS + minute * DateUtil.ONE_MINUTE_MILLIS
                + second * 1000L;

        long oldListTime = autoListTime.getRelativeListTime();

        //再设置autoListSchedule属性
        try {
            ModifyListTime.modifySchedule(user.getId(), oldListTime, listTime);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            renderJSON(JsonUtil.getJson(new DelistMessage(false, "修改时发生了一些错误，请联系我们")));
        }
        autoListTime.setRelativeListTime(listTime);
        autoListTime.setListTime(0L);
        AutoListTimeDao.saveOrUpdateAutoListTime(autoListTime);

        renderJSON(JsonUtil.getJson(new DelistMessage(true, "该宝贝上架时间修改成功")));
    }

    /*public static void newQueryDelistItems(Long nowTime, String title, int pn, int ps) {

    	PageOffset po = new PageOffset(pn, ps, 10);
    	User user = getUser();
    	AutoListRecord record = AutoListRecordDao.findAutoListRecordByUserId(user.getId());	
    	if (record == null || record.getIsCalcuComplete() == false) {
    		DelistPaging pageInfo = new DelistPaging(0, new ArrayList<AutoListTime>());
    		renderJSON(JsonUtil.getJson(pageInfo));
    	}
    	
    	Set<Long> ids1 = NoAutoListItem.findNumIidsByUser(user.getId());
    	TMResult tmResult = ItemDao.findByUserAndSearchWithExcludedOrderbyListTime(user.getId(), title, po, ids1);
    	
    

    	renderJSON(JsonUtil.getJson(tmResult));
    }*/

    
    private static int[] calcuWeekDistributeNum(List<Long> relativeList) {
        if (CommonUtils.isEmpty(relativeList)) {
            relativeList = new ArrayList<Long>();
        }

        int[] distriNumArray = new int[7];
        for (int i = 0; i < 7; i++) {
            distriNumArray[i] = 0;
        }

        for (Long relativeTime : relativeList) {
            if (relativeTime == null) {
                continue;
            }
            int day = (int) (relativeTime / DateUtil.DAY_MILLIS);
            if (day <= 0) {
                day = 7;
            }

            distriNumArray[day - 1]++;
        }
        
        return distriNumArray;
    }
    
    
    public static void queryDelistDistribute() {

        User user = getUser();

        List<Long> relativeList = AutoListTimeDao.queryAllRelativeListTime(user.getId());
        
        int[] scheduleArray = calcuWeekDistributeNum(relativeList);
        
        //真实的上架时间
        List<Long> realDelistTimeList = ItemDao.findOnlineItemDelistTimeListByUserId(user.getId());
        
        if (CommonUtils.isEmpty(realDelistTimeList)) {
            realDelistTimeList = new ArrayList<Long>();
        }
        
        List<Long> realRelativeList = new ArrayList<Long>();
        for (Long realDelistTime : realDelistTimeList) {
            if (realDelistTime == null) {
                realDelistTime = 0L;
            }
            long weekStart = DateUtil.findThisWeekStart(realDelistTime);
            long realRelativeTime = realDelistTime - weekStart;
            
            realRelativeList.add(realRelativeTime);
        }
        
        
        int[] realArray = calcuWeekDistributeNum(realRelativeList);
        
        /*
        for (int i = 0; i < 7; i++) {
            realArray[i] = 0;
        }
        UserDiag userDiag = UserDiag.findOrCreate(user);
        if (userDiag != null) {
            String weekDistribute = userDiag.getWeekDistributed();
            if (StringUtils.isEmpty(weekDistribute)) {

            } else {
                String[] realStrArray = weekDistribute.split(",");
                if (realStrArray == null || realStrArray.length != 7) {

                } else {
                    for (int i = 0; i < 7; i++) {
                        realArray[i] = NumberUtil.parserInt(realStrArray[i], 0);
                    }
                }
            }
        }
        */

        int[][] arr = new int[][] {
                scheduleArray,
                realArray
        };

        renderJSON(JsonUtil.getJson(arr));
    }

    public static void queryDetailDistribute() {

        User user = getUser();

        List<Long> relativeList = AutoListTimeDao.queryAllRelativeListTime(user.getId());
        if (CommonUtils.isEmpty(relativeList)) {
            relativeList = new ArrayList<Long>();
        }

        int[][] scheduleArray = new int[25][7];
        for (int i = 0; i < 25; i++) {
            for (int j = 0; j < 7; j++) {
                scheduleArray[i][j] = 0;
            }
        }

        for (Long relativeTime : relativeList) {
            if (relativeTime == null) {
                continue;
            }
            int day = (int) (relativeTime / DateUtil.DAY_MILLIS);
            if (day <= 0) {
                day = 7;
            }
            int remainMills = (int) (relativeTime % DateUtil.DAY_MILLIS);
            int hour = (int) (remainMills / DateUtil.HOUR_MILLS);
            scheduleArray[hour][day - 1]++;
            scheduleArray[24][day - 1]++;
        }
        renderJSON(JsonUtil.getJson(scheduleArray));
    }

    public static class DelistMessage {
        private boolean success;

        private String message;

        public DelistMessage(boolean success, String message) {
            super();
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

    }

    public static class AutoDelistInfo {

        private int state = 0;//0代表未开启，1表示正在计算，2表示已开启

        private int onSaleCount;

        private int inventoryCount;

        private long todayListCount;

        private long todayAreadyListCount;

        private long todayNeedListCount;

        private User user;

        public AutoDelistInfo(User user) {
            this.user = user;
        }

        public int getOnSaleCount() {
            return onSaleCount;
        }

        public int getInventoryCount() {
            return inventoryCount;
        }

        public long getTodayListCount() {
            return todayListCount;
        }

        public long getTodayAreadyListCount() {
            return todayAreadyListCount;
        }

        public long getTodayNeedListCount() {
            return todayNeedListCount;
        }

        public int getState() {
            return state;
        }

        public void calcuInfo(Long nowTime) {
            boolean isOn = isAutoDelistOn(user);
            if (isOn == false) {
                state = 0;
                return;
            }
            AutoListRecord record = AutoListRecordDao.findAutoListRecordByUserId(user.getId());
            if (record == null) {
                state = 3;//表示第一次
                return;
            }
            if (record.getIsCalcuComplete() == false) {
                state = 1;
                return;
            }
            state = 2;

            Long saleCount = new ItemApi.ItemsOnsaleCount(user.getSessionKey(), null, null).call();
            Long inventoryCountLong = new ItemApi.ItemsInventoryCount(user, null, null).call();
            onSaleCount = saleCount == null ? 0 : saleCount.intValue();
            inventoryCount = inventoryCountLong == null ? 0 : inventoryCountLong.intValue();

            todayListCount = AutoListTimeDao.queryTodayListNum(nowTime, user.getId(), record.getCreateTime());
            todayAreadyListCount = AutoListTimeDao.queryTodayAreadyListNum(nowTime, user.getId());

            todayNeedListCount = todayListCount - todayAreadyListCount;
        }

    };

    public static class DelistPaging {
        private long totalCount;

        private List<AutoListTime> timeList;

        public DelistPaging(long totalCount, List<AutoListTime> timeList) {
            this.timeList = timeList;
            this.totalCount = totalCount;
        }

        public long getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(long totalCount) {
            this.totalCount = totalCount;
        }

        public List<AutoListTime> getTimeList() {
            return timeList;
        }

        public void setTimeList(List<AutoListTime> timeList) {
            this.timeList = timeList;
        }

    }

    public static class ListLogPaging {
        private long totalCount;

        private List<AutoListLog> timeList;

        public ListLogPaging(long totalCount, List<AutoListLog> timeList) {
            this.timeList = timeList;
            this.totalCount = totalCount;
        }

        public long getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(long totalCount) {
            this.totalCount = totalCount;
        }

        public List<AutoListLog> getTimeList() {
            return timeList;
        }

        public void setTimeList(List<AutoListLog> timeList) {
            this.timeList = timeList;
        }

    }

    public static class DelistInfo {
        private String title;

        private String listTime;

        private int salesCount;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getListTime() {
            return listTime;
        }

        public void setListTime(String listTime) {
            this.listTime = listTime;
        }

        public int getSalesCount() {
            return salesCount;
        }

        public void setSalesCount(int salesCount) {
            this.salesCount = salesCount;
        }

    }

    //检查宝贝真实的上架记录
    public static void showItemDelist() {
        User user = getUser();
        ItemApi.ItemsOnsale itemApi = new ItemApi.ItemsOnsale(user, 0L, 0L);

        List<Item> itemList = itemApi.call();
        List<DelistInfo> delistInfoList = new ArrayList<DelistInfo>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (Item item : itemList) {
            DelistInfo delistInfo = new DelistInfo();
            delistInfo.setTitle(item.getTitle());
            delistInfo.setListTime(sdf.format(item.getListTime()));
            ItemPlay itemPlay = ItemDao.findByNumIid(user.getId(), item.getNumIid());
            delistInfo.setSalesCount(itemPlay.getSalesCount());
            delistInfoList.add(delistInfo);
        }

        renderJSON(delistInfoList);
    }

    public static void showItemPlayDelist() {
        User user = getUser();
        List<ItemPlay> itemList = ItemDao.findByUserId(user.getId());

        List<DelistInfo> delistInfoList = new ArrayList<DelistInfo>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (ItemPlay item : itemList) {
            DelistInfo delistInfo = new DelistInfo();
            delistInfo.setTitle(item.getTitle());
            delistInfo.setListTime(sdf.format(item.getDeListTime()));
            delistInfo.setSalesCount(item.getSalesCount());
            delistInfoList.add(delistInfo);
        }

        renderJSON(delistInfoList);
    }

    public static void showAllItemDelist() {
        User user = getUser();
        ItemApi.ItemsOnsale itemApi = new ItemApi.ItemsOnsale(user, 0L, 0L);

        List<Item> itemList = itemApi.call();

        renderJSON(itemList);
    }

    public static void createPlan(String title) {
    }

    public static void removePlan(long planId) {
    }

    public static void turnOnPlan(long planId) {
    }

    public static void turnOffPlan(long planId) {
    }

}
