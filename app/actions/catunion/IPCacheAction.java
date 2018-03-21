package actions.catunion;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;

public class IPCacheAction {

    private static final Logger log = LoggerFactory.getLogger(IPCacheAction.class);
    
    private static final int MaxVisitTimes = 1000;
    
    private static final String Expired = "1h";//1小时过期
    private static final String IPCacheKey = "IPCache_";
    
    public static boolean judgeIp(String ip) {
        try {
            String key = IPCacheKey + ip;
            Object obj = Cache.get(key);
            IPVisitedInfo visitedInfo = null;
            if (obj != null) {
                visitedInfo = (IPVisitedInfo)obj;
            } 
            if (visitedInfo == null) {
                visitedInfo = new IPVisitedInfo(System.currentTimeMillis(), 0);
                Cache.set(key, visitedInfo, Expired);
            }
            if (visitedInfo.visitedCount < 0)
                visitedInfo.visitedCount = 0;
            
            visitedInfo.visitedCount++;
            
            //log.error(ip + "   " + JsonUtil.getJson(visitedInfo));
            if (visitedInfo.visitedCount > MaxVisitTimes)
                return false;
            else
                return true;
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            
            return true;
        }
    }
    
    public static class IPVisitedInfo implements Serializable {
        private static final long serialVersionUID = -1L;
        
        private long firstTime;
        private int visitedCount;
        
        
        public long getFirstTime() {
            return firstTime;
        }


        public void setFirstTime(long firstTime) {
            this.firstTime = firstTime;
        }


        public int getVisitedCount() {
            return visitedCount;
        }


        public void setVisitedCount(int visitedCount) {
            this.visitedCount = visitedCount;
        }


        public IPVisitedInfo(long firstTime, int visitedCount) {
            super();
            this.firstTime = firstTime;
            this.visitedCount = visitedCount;
        }
        
        
    }
    
}
