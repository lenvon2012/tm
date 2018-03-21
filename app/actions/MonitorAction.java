
package actions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import utils.TBIpApi;

import com.ciaosir.client.utils.JsonUtil;

public class MonitorAction {
    private static final String MONITOR_KEY = "MonitorCollection";

    private static final Logger log = LoggerFactory.getLogger(MonitorAction.class);

    //加入一条访问记录
    public static void addUserMonitor(String ip, Long userId, Long numIid, String cookieId) {
        Queue<VisitorInfo> visitorInfoList = Cache.get(MONITOR_KEY + userId, Queue.class);
        if (visitorInfoList == null) {
            visitorInfoList = new ConcurrentLinkedQueue<VisitorInfo>();
        } else {

        }

        long currentTime = System.currentTimeMillis();
        //先删掉过期的
        deleteOutDateVisitor(visitorInfoList, currentTime);

        VisitorInfo targetVisitor = null;
        for (VisitorInfo visitorInfo : visitorInfoList) {
            if (cookieId != null && cookieId.equals(visitorInfo.getCookieId())) {
                targetVisitor = visitorInfo;
                break;
            }
        }
        if (targetVisitor == null) {
            targetVisitor = new VisitorInfo(cookieId, ip, currentTime);
            visitorInfoList.add(targetVisitor);
        }
        targetVisitor.setLastTime(currentTime);

        VisitItem visitItem = new VisitItem(numIid, currentTime);
        targetVisitor.addVisitItem(visitItem);

        Cache.set(MONITOR_KEY + userId, visitorInfoList, "1h");
    }

    //找到某个店铺的监视结果
    public static String getMonitorResult(Long userId, String userIp) {
        Queue<VisitorInfo> visitorInfoList = Cache.get(MONITOR_KEY + userId, Queue.class);
        if (visitorInfoList == null)
            return "[]";

        if (visitorInfoList != null) {
            //先删掉过期的
            long currentTime = System.currentTimeMillis();
            deleteOutDateVisitor(visitorInfoList, currentTime);

            for (VisitorInfo visitorInfo : visitorInfoList) {
                String ip = visitorInfo.getIp();
                String location = "";
                if (userIp != null && userIp.equals(ip))
                    location = "掌柜";
                else
                    location = TBIpApi.getLocation(ip);
                visitorInfo.setLocation(location);
            }
        }
        String json = JsonUtil.getJson(visitorInfoList);
        return json;

    }

    //在加入新的买家，或者查找某个店铺的监视结果时，删除过期的买家
    private static void deleteOutDateVisitor(Queue<VisitorInfo> visitorInfoList, long currentTime) {
        List<VisitorInfo> deleteList = new ArrayList<VisitorInfo>();
        for (VisitorInfo visitor : visitorInfoList) {
            if (visitor.judgeOutOfDate(currentTime)) {
                deleteList.add(visitor);
            }
        }

        for (VisitorInfo visitor : deleteList) {
            visitorInfoList.remove(visitor);
            //log.info("delete visitor:" + visitor.toString()) ;
        }
    }

    public static class VisitorInfo implements Serializable {
        private static final long serialVersionUID = -4232305298240916427L;

        private static final long TimeLimit = 1000 * 150;//150秒

        private final String cookieId;

        private final String ip;

        private String location;

        private final long startTime;

        private long lastTime;

        private Queue<VisitItem> visitItemList = new ConcurrentLinkedQueue<VisitItem>();

        public VisitorInfo(String cookieId, String ip, long startTime) {
            this.cookieId = cookieId;
            this.ip = ip;
            this.startTime = startTime;
        }

        public String getCookieId() {
            return cookieId;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getLocation() {
            return location;
        }

        public String getIp() {
            return ip;
        }

        public long getStartTime() {
            return startTime;
        }

        public void addVisitItem(VisitItem visitItem) {
            visitItemList.add(visitItem);
        }

        public Queue<VisitItem> getVisitItemList() {
            return visitItemList;
        }

        public void setLastTime(long lastTime) {
            this.lastTime = lastTime;
        }

        public long getLastTime() {
            return lastTime;
        }

        public boolean judgeOutOfDate(long currentTime) {
            if (lastTime + TimeLimit < currentTime)
                return true;
            return false;
        }

        public String toString() {
            return "ip: " + ip + "  startTime: " + startTime + "   lastTime: " + lastTime;
        }

    };

    public static class VisitItem implements Serializable {

        private static final long serialVersionUID = -4292305298243336427L;

        private final Long numIid;

        private final long visitTime;

        public VisitItem(Long numIid, long visitTime) {
            this.numIid = numIid;
            this.visitTime = visitTime;
        }

        public Long getNumIid() {
            return numIid;
        }

        public long getVisitTime() {
            return visitTime;
        }
    }
}
