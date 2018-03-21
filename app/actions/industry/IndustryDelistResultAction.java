package actions.industry;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import result.TMResult;
import utils.DateUtil;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;

public class IndustryDelistResultAction {
    
    private static final Logger log = LoggerFactory.getLogger(IndustryDelistResultAction.class);
    
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 统计在每个小时上下架的宝贝数
     * @param user
     * @param searchKey
     * @param searchPages
     * @return
     */
    public static int[] countTaobaoItemHourlyDelist(String searchKey, String itemOrderType, int searchPages) {
    
        int[] hourDelistArray = new int [7 * 24];
        
        for (int i = 0; i < hourDelistArray.length; i++) {
            hourDelistArray[i] = 0;
        }
        
        List<DelistItemInfo> delistItemList = IndustryDelistGetAction.doSearchTaobaoRankItems(searchKey, 
                itemOrderType, searchPages);
        
        if (CommonUtils.isEmpty(delistItemList)) {
            return hourDelistArray;
        }
        
        for (DelistItemInfo delistItem : delistItemList) {
            if (delistItem == null) {
                continue;
            }
            //这个是从周一开始的
            long relativeTime = delistItem.getRelativeTime();
            
            int delistHour = getItemDelistHour(relativeTime);
            if (delistHour >= 0) {
                hourDelistArray[delistHour]++;
            }
        }
        
        return hourDelistArray;
    }
    
    public static int[] countTaobaoItemHourlyDelist(String searchKey,
            String itemOrderType, int searchPages, String searchPlace, User user) {

        int[] hourDelistArray = new int[7 * 24];

        for (int i = 0; i < hourDelistArray.length; i++) {
            hourDelistArray[i] = 0;
        }
        
        PageOffset po = new PageOffset(1, 10);

        List<DelistItemInfo> delistItemList = IndustryDelistGetAction
                .doSearchTaobaoRankItems(searchKey, itemOrderType, searchPages,
                        searchPlace, po, user);

        if (CommonUtils.isEmpty(delistItemList)) {
            return null;
        }

        for (DelistItemInfo delistItem : delistItemList) {
            if (delistItem == null) {
                continue;
            }
            // 相对时间，这个是从周一开始的
            long relativeTime = getRelativeTime(delistItem.getdelistTimestamp());

            int delistHour = getItemDelistHour(relativeTime);
            if (delistHour >= 0 && delistHour < hourDelistArray.length) {
                hourDelistArray[delistHour]++;
            }
        }

        return hourDelistArray;
    }
    
    //得到相对于周一0点起的小时数
    private static int getItemDelistHour(long relativeTime) {

        //relativeTime是可能为0的。。。
        if (relativeTime < 0) {
            return -1;
        }
        
        int relativeHour = (int) (relativeTime / DateUtil.ONE_HOUR_MILLIS);
        return relativeHour;
        
    }
    
    public static TMResult findTaobaoItemsWithPaging(String searchKey, String itemOrderType, int searchPages, 
            String orderBy, boolean isDesc, PageOffset po) {
        
        List<DelistItemInfo> delistItemList = findDelistItems(searchKey, itemOrderType, searchPages);
        
        if (CommonUtils.isEmpty(delistItemList)) {
            return new TMResult(new ArrayList<DelistItemInfo>(), 0, po);
        }
        
        delistItemList = orderDelistItems(delistItemList, orderBy, isDesc);
        
        int totalCount = delistItemList.size();
        int startOffset = po.getOffset();
        int endOffset = startOffset + po.getPs();
        
        if (startOffset >= totalCount) {
            return new TMResult(new ArrayList<DelistItemInfo>(), totalCount, po); 
        }
        
        List<DelistItemInfo> resultList = new ArrayList<DelistItemInfo>();
        
        if (totalCount > endOffset) {
            resultList = delistItemList.subList(startOffset, endOffset);
        } else {
            resultList = delistItemList.subList(startOffset, totalCount);
        }
        
        return new TMResult(resultList, totalCount, po); 
    }
    
    public static TMResult findTaobaoItemsWithPaging(String searchKey, String itemOrderType, int searchPages, 
            String searchPlace, String orderBy, boolean isDesc, PageOffset po, User user) {
        
        List<DelistItemInfo> delistItemList = IndustryDelistGetAction.doSearchTaobaoRankItems(searchKey, 
                itemOrderType, searchPages, searchPlace, po, user);
        
        if (CommonUtils.isEmpty(delistItemList)) {
            return new TMResult(new ArrayList<DelistItemInfo>(), 0, po);
        }
        
        delistItemList = orderDelistItems(delistItemList, orderBy, isDesc);
        
        int totalCount = delistItemList.size();
        int startOffset = po.getOffset();
        int endOffset = startOffset + po.getPs();
        
        if (startOffset >= totalCount) {
            return new TMResult(new ArrayList<DelistItemInfo>(), totalCount, po); 
        }
        
        List<DelistItemInfo> resultList = new ArrayList<DelistItemInfo>();
        
        if (totalCount > endOffset) {
            resultList = delistItemList.subList(startOffset, endOffset);
        } else {
            resultList = delistItemList.subList(startOffset, totalCount);
        }
        
        return new TMResult(resultList, totalCount, po); 
    }
    
    private static List<DelistItemInfo> findDelistItems(String searchKey, 
            String itemOrderType, int searchPages) {
        
        List<DelistItemInfo> delistItemList = IndustryDelistGetAction.doSearchTaobaoRankItems(searchKey, 
                itemOrderType, searchPages);
        
        return delistItemList;
    }
    
    private static List<DelistItemInfo> orderDelistItems(List<DelistItemInfo> delistItemList, 
            final String orderBy, final boolean isDesc) {
        
        if (CommonUtils.isEmpty(delistItemList)) {
            return new ArrayList<DelistItemInfo>();
        }
        
        Collections.sort(delistItemList, new Comparator<DelistItemInfo>() {
            @Override
            public int compare(DelistItemInfo o1, DelistItemInfo o2) {
                String tempOrderBy = orderBy;
                
                if (StringUtils.isEmpty(tempOrderBy)) {
                    tempOrderBy = "orderIndex";
                }
                
                int ascResult = 0;
                
                if ("orderIndex".equals(orderBy)) {
                    ascResult = o1.getOrderIndex() - o2.getOrderIndex();
                } else if ("relativeDelistTime".equals(orderBy)) {
                    if (o1.getRelativeTime() > o2.getRelativeTime()) {
                        ascResult = 1;
                    } else if (o1.getRelativeTime() < o2.getRelativeTime()) {
                        ascResult = -1;
                    } else {
                        ascResult = 0;
                    }
                } else if ("leftTime".equals(orderBy)) {
                    if (o1.getLeftTime() > o2.getLeftTime()) {
                        ascResult = 1;
                    } else if (o1.getLeftTime() < o2.getLeftTime()) {
                        ascResult = -1;
                    } else {
                        ascResult = 0;
                    }
                } else if ("salesCount".equals(orderBy)) {
                    ascResult = o1.getSalesCount() - o2.getSalesCount();
                } else if ("createTime".equals(orderBy)) {
                    if (o1.getCreateTime() > o2.getCreateTime()) {
                        ascResult = 1;
                    } else if (o1.getCreateTime() < o2.getCreateTime()) {
                        ascResult = -1;
                    } else {
                        ascResult = 0;
                    }
                } else {
                    ascResult = 0;
                }
                
                if (isDesc == true) {
                    ascResult = ascResult * -1;
                }
                
                return ascResult;
            }
            
        });
        
        
        return delistItemList;
        
        
    }
    
    
    public static class DelistItemInfo {
        private Long numIid;
        private String title;
        private String dt;
        private String delistTimestamp;
        
        private String picUrl;
        private int orderIndex;
        private long relativeTime;//相对时间，这里从周一开始的。不是周日，因为要排序
        
        private long leftTime;//下架剩余时间
        
        private long createTime;//宝贝创建时间
        
        private int salesCount;
        
        public String getDt() {
            return dt;
        }
        public void setDt(String dt) {
            this.dt = dt;
        }
        public String getdelistTimestamp() {
            return delistTimestamp;
        }
        public void setdelistTimestamp(String delistTimestamp) {
            this.delistTimestamp = delistTimestamp;
        }
        public Long getNumIid() {
            return numIid;
        }
        public void setNumIid(Long numIid) {
            this.numIid = numIid;
        }
        public String getTitle() {
            return title;
        }
        public void setTitle(String title) {
            this.title = title;
        }
        public String getPicUrl() {
            return picUrl;
        }
        public void setPicUrl(String picUrl) {
            this.picUrl = picUrl;
        }
        
        
        public long getLeftTime() {
            return leftTime;
        }
        public void setLeftTime(long leftTime) {
            this.leftTime = leftTime;
        }
        public long getRelativeTime() {
            return relativeTime;
        }
        public void setRelativeTime(long relativeTime) {
            this.relativeTime = relativeTime;
        }
        public int getOrderIndex() {
            return orderIndex;
        }
        public void setOrderIndex(int orderIndex) {
            this.orderIndex = orderIndex;
        }
        public int getSalesCount() {
            return salesCount;
        }
        public void setSalesCount(int salesCount) {
            this.salesCount = salesCount;
        }
        
        public DelistItemInfo(Long numIid, String title, String picUrl,
                int orderIndex, long relativeTime, long leftTime, long createTime, int salesCount) {
            super();
            this.numIid = numIid;
            this.title = title;
            this.picUrl = picUrl;
            this.orderIndex = orderIndex;
            this.relativeTime = relativeTime;
            this.leftTime = leftTime;
            this.createTime = createTime;
            this.salesCount = salesCount;
        }
        
        public DelistItemInfo(long numIid2, String title2, String dt2,
                String picUrl2, String delistTimestamp) {
            this.numIid = numIid2;
            this.title = title2;
            this.dt = dt2;
            this.picUrl = picUrl2;
            this.delistTimestamp = delistTimestamp;
        }
        
        public static String getLeftTimeStrStatic(long leftTime) {
            if (leftTime < 0) {
                return "-";
            }
            
            long tempLeftTime = leftTime;
            
            long leftDay = tempLeftTime / DateUtil.DAY_MILLIS;
            tempLeftTime = tempLeftTime - leftDay * DateUtil.DAY_MILLIS;
            
            
            long leftHour = tempLeftTime / DateUtil.HOUR_MILLS;
            tempLeftTime = tempLeftTime - leftHour * DateUtil.HOUR_MILLS;
            
            long leftMinute = tempLeftTime / DateUtil.ONE_MINUTE_MILLIS;
            tempLeftTime = tempLeftTime - leftMinute * DateUtil.ONE_MINUTE_MILLIS;
            
            long leftSecond = tempLeftTime / 1000;
            
            String timeStr = "";
            
            if (leftDay > 0) {
                timeStr += leftDay + "天 ";
            }
            
            if (leftHour > 0) {
                timeStr += leftHour + "小时 ";
            }
            
            if (leftDay <= 0 && leftMinute > 0) {
                timeStr += leftMinute + "分 ";
            }
            
            if (leftDay <= 0 && leftHour <= 0) {
                timeStr += leftSecond + "秒 ";
            }
            
            return timeStr;
        }
        
        public String getLeftTimeStr() {
            return getLeftTimeStrStatic(leftTime);
        }
        
        public String getCreateTimeStr() {
            return sdf.format(new Date(createTime));
        }
        
        public long getCreateTime() {
            return createTime;
        }
        public void setCreateTime(long createTime) {
            this.createTime = createTime;
        }
    }
    
    public static long getRelativeTime(String delistTimestamp) {

        Calendar calendar = Calendar.getInstance();
        // 获取当前星期几
        int numWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        // 相对星期一的天数
        int day;
        if (numWeek == 0) {
            day = 6;
        } else {
            day = numWeek - 1;
        }

        // 获取相对于此时day天前的日期
        calendar.setTime(new Date());
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - day);

        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            calendar.setTime(sdf.parse(sdf.format(calendar.getTime())));
        } catch (ParseException e) {
            log.warn(e.getMessage(), e);
        }
        // 这个星期星期一的时间戳
        long MondayTimeStamp = calendar.getTimeInMillis();  
        //下架时间相对本周星期一的时间    delistTimestamp变为ms再运算
        long relativeTime = Long.parseLong(delistTimestamp) * 1000 - MondayTimeStamp;

        return relativeTime;
    }
}
