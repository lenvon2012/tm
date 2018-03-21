package actions.catunion;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.util.concurrent.jsr166y.ConcurrentLinkedDeque;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;

import com.ciaosir.client.CommonUtils;

public class VisitedCacheAction {
    private static final Logger log = LoggerFactory.getLogger(VisitedCacheAction.class);
    
    private static final String VisitedCacheKey = "VisitedCacheKey_";
    private static final String Expired = "24h";
    public static final int MaxQueueSize = 10;

    public static List<UserCacheInfo> getVisitedCache(int searchType) {
        try {
            String key = VisitedCacheKey + searchType;
            Queue<UserCacheInfo> visitedQueue = (Queue<UserCacheInfo>)Cache.get(key);
            List<UserCacheInfo> visitedList = new ArrayList<UserCacheInfo>();
            if (CommonUtils.isEmpty(visitedQueue))
                return visitedList; 
            
            //顺序要反过来，越后面的是越近查询的
            UserCacheInfo[] visitedArray = visitedQueue.toArray(new UserCacheInfo[0]);
            int size = visitedArray.length;
            for (int i = size - 1; i >= 0; i--) {
                visitedList.add(visitedArray[i]);
            }
            return visitedList;
        } catch (Exception ex ) {
            log.error(ex.getMessage(), ex);
            return new ArrayList<UserCacheInfo>();
        }
    }
    
    public static void addToCache(long userId, String nick, int searchType) {
        try {
            if (userId <= 0 || StringUtils.isEmpty(nick))
                return;
            String key = VisitedCacheKey + searchType;
            Queue<UserCacheInfo> visitedQueue = (Queue<UserCacheInfo>)Cache.get(key);
            if (visitedQueue == null) {
                visitedQueue = new ConcurrentLinkedDeque<UserCacheInfo>();
            } 
            List<UserCacheInfo> deleteList = new ArrayList<UserCacheInfo>();
            for (UserCacheInfo userInfo : visitedQueue) {
                if (userInfo == null)
                    continue;
                if (userInfo.getUserId() == userId && nick.equals(userInfo.getNick())) {
                    deleteList.add(userInfo);
                }
            }
            for (UserCacheInfo deleteUser : deleteList) {
                visitedQueue.remove(deleteUser);
            }
            if (visitedQueue.size() >= MaxQueueSize)
                visitedQueue.poll();
            UserCacheInfo userInfo = new UserCacheInfo(userId, nick);
            visitedQueue.offer(userInfo);
            
            Cache.set(key, visitedQueue, Expired);
        } catch (Exception ex ) {
            log.error(ex.getMessage(), ex);
        }
    }

    public static class UserCacheInfo implements Serializable {
        private static final long serialVersionUID = -1L; 
        
        private long userId;
        private String nick;
        public long getUserId() {
            return userId;
        }
        public void setUserId(long userId) {
            this.userId = userId;
        }
        public String getNick() {
            return nick;
        }
        public void setNick(String nick) {
            this.nick = nick;
        }
        public UserCacheInfo(long userId, String nick) {
            super();
            this.userId = userId;
            this.nick = nick;
        }
        
        
    }
    
}
