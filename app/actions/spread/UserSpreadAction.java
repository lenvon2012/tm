package actions.spread;

import java.util.HashMap;
import java.util.Map;

import models.spread.SpreadItemPlay.SpreadLevelType;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import configs.Subscribe.Version;
import dao.spread.SpreadItemDao;

public class UserSpreadAction {
private static final Logger log = LoggerFactory.getLogger(UserSpreadAction.class);
    
    private static final Map<Integer, Integer> FreeLevelMap = new HashMap<Integer, Integer>();
    static {
        FreeLevelMap.put(SpreadLevelType.Level1, 1);
    }
    private static final Map<Integer, Integer> BasicLevelMap = new HashMap<Integer, Integer>();
    static {
        BasicLevelMap.put(SpreadLevelType.Level1, 5);
    }
    private static final Map<Integer, Integer> VIPLevelMap = new HashMap<Integer, Integer>();
    static {
        VIPLevelMap.put(SpreadLevelType.Level1, 10);
    }
    private static final Map<Integer, Integer> SuperLevelMap = new HashMap<Integer, Integer>();
    static {
        SuperLevelMap.put(SpreadLevelType.Level1, 20);
    }
    private static final Map<Integer, Integer> HallLevelMap = new HashMap<Integer, Integer>();
    static {
        HallLevelMap.put(SpreadLevelType.Level1, 40);
    }
    private static final Map<Integer, Integer> GodLevelMap = new HashMap<Integer, Integer>();
    static {
        GodLevelMap.put(SpreadLevelType.Level1, 60);
    }
    private static final Map<Integer, Integer> SunLevelMap = new HashMap<Integer, Integer>();
    static {
        SunLevelMap.put(SpreadLevelType.Level1, 120);
    }
    
    
    
    private static Map<Integer, Integer> getUserSpreadMap(User user) {
        int level = user.getVersion();
        
        if (level <= Version.FREE) {
            return FreeLevelMap;
        } else if (level <= Version.BASE) {
            return BasicLevelMap;
        } else if (level <= Version.VIP) {
            return VIPLevelMap;
        } else if (level <= Version.SUPER){
            return SuperLevelMap;
        } else if (level <= Version.HALL){
            return HallLevelMap;
        } else if (level <= Version.GOD){
            return GodLevelMap;
        } else {
            return SunLevelMap;
        }
    }
    
    public static int getUserSpreadNum(User user, int level) {
        Map<Integer, Integer> levelMap = getUserSpreadMap(user);
        if (levelMap == null) {
            log.error("can not find levelMap for version: " + user.getVersion());
            return 0;
        }
        Integer spreadNum = levelMap.get(level);
        if (spreadNum == null || spreadNum <= 0)
            return 0;
        
        return spreadNum;
    }
    
    public static LevelSpreadInfo queryLevelSpreadInfo(User user, int level) {
        
        int usedNum = (int)SpreadItemDao.countSpreadNumByLevel(user.getId(), level);

        int totalNum = getUserSpreadNum(user, level);
        
        LevelSpreadInfo levelSpreadInfo = new LevelSpreadInfo(totalNum, usedNum);
        return levelSpreadInfo;
    }
    
    
    /*public static class UsedSpreadInfo {
        private Map<Integer, Integer> usedMap = new HashMap<Integer, Integer>();
        public UsedSpreadInfo(String json) {
            try {
                if (StringUtils.isEmpty(json)) {
                    usedMap = new HashMap<Integer, Integer>();
                    return;
                }
                usedMap = JsonUtil.toObject(json, Map.class);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                usedMap = new HashMap<Integer, Integer>();
            }
        }
        
        public int getLevelUsedNum(int level) {
            Integer num = usedMap.get(level);
            if (num == null)
                num = 0;
            return num;
        }
        
        public void addLevelSpreadNum(int level) {
            Integer num = usedMap.get(level);
            if (num == null)
                num = 0;
            num++;
            usedMap.put(level, num);
        }
        
        public void decreaseLevelSpreadNum(int level) {
            Integer num = usedMap.get(level);
            if (num == null || num <= 0) {
                num = 0;
                log.error("there may be something wrong!");
            } else
                num--;
            usedMap.put(level, num);
        }
        
        public String toJson() {
            try {
                String json = JsonUtil.getJson(usedMap);
                return json;
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                return "";
            }
        }
    }*/
    
    
    public static class LevelSpreadInfo {
        private int totalNum;
        private int usedNum;
        public int getTotalNum() {
            return totalNum;
        }
        public void setTotalNum(int totalNum) {
            this.totalNum = totalNum;
        }
        public int getUsedNum() {
            return usedNum;
        }
        public void setUsedNum(int usedNum) {
            this.usedNum = usedNum;
        }
        public LevelSpreadInfo(int totalNum, int usedNum) {
            super();
            this.totalNum = totalNum;
            this.usedNum = usedNum;
        }
        
        
    }
}
