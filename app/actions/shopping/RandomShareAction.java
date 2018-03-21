package actions.shopping;

import java.util.ArrayList;
import java.util.List;

import job.InnerRandomLogWritter;

import models.oplog.InnerRandomLog;
import models.popularized.Popularized;
import models.popularized.Popularized.PopularizedStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.TMCatUtil;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.JsonUtil;

import controllers.newAutoTitle;

import dao.popularized.PopularizedDao;
import dao.popularized.ShoppingDao;

public class RandomShareAction {
    private static final Logger log = LoggerFactory.getLogger(RandomShareAction.class);

    public static final int EachReturnNum = 20;
    
    public static List<Popularized> randomWithUser(Long numIid, Long userId, Long topCatId, 
            String title, int status) {
        
        return randomWithUser(numIid, userId, topCatId, title, status, EachReturnNum);
    }
    
    public static List<Popularized> randomWithUser(Long numIid, Long userId, Long topCatId, 
            String title, int status, int limitNum) {
        List<Popularized> resultItemList = new ArrayList<Popularized>();
        
        try {
            String catName = TMCatUtil.getFirstLevelCatName(topCatId);
            
            //根据numIid搜索
            Popularized targetItem = null;
            if (numIid != null && numIid > 0) {
                targetItem = ShoppingDao.findByNumIid(numIid, catName, title, status);
            }
            if (targetItem != null)
                resultItemList.add(targetItem);
            
            //根据user搜索
            if (userId != null && userId > 0L) {
                List<Popularized> userItemList = ShoppingDao.findByUserAndTitle(userId, catName, title, status);
                userItemList = PopularizedDao.randomItems(userItemList);
                if (targetItem != null && userItemList.contains(targetItem)) {
                    userItemList.remove(targetItem);
                }
                
                resultItemList.addAll(userItemList);
            }
            if (resultItemList.size() >= limitNum) {
                resultItemList = resultItemList.subList(0, limitNum);
                return resultItemList;
            } else {
                List<Popularized> itemList = randomWithTitleByCatName(title, catName, status, limitNum);
                for (Popularized temp : itemList) {
                    if (resultItemList.contains(temp))
                        continue;
                    resultItemList.add(temp);
                    if (resultItemList.size() >= limitNum)
                        break;
                }
                
                return resultItemList;
            }
            
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        
        return resultItemList;
        
    }
    
    public static List<Popularized> randomWithTitle(String title, Long topCatId, int status) {
        List<Popularized> resultItemList = new ArrayList<Popularized>();
        
        try {
            String catName = TMCatUtil.getFirstLevelCatName(topCatId);
            resultItemList = randomWithTitleByCatName(title, catName, status);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        
        return resultItemList;
    }
    
    public static Popularized randomOne(Long userId) {
        Popularized item = new Popularized();
        long startId = allPreviousMaxId;
        try {
            item = ShoppingDao.findRandomOne(startId, userId);
            if(item == null) {
            	item = ShoppingDao.findRandomOne(allPreviousMaxId, userId);
            }
            if(item != null && item.getUserId().equals(79742176L)) {
            	InnerRandomLogWritter.addMsg(new InnerRandomLog(item.getUserId(), item.getNumIid(), System.currentTimeMillis()));
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        
        return item;
    }
    
    
    private static long normalPreviousMaxId = 0;
    private static long hotPreviousMaxId = 0;
    public static long allPreviousMaxId = 0;
    
    private static final int PerFetchNum = 40;
    
    public static List<Popularized> randomWithTitleByCatName(String title, String catName, int status) {
        return randomWithTitleByCatName(title, catName, status, EachReturnNum);
    }
    
    private static List<Popularized> randomWithTitleByCatName(String title, String catName, int status, int limitNum) {
        List<Popularized> itemList = findByTitle(title, catName, status, PerFetchNum);
        if (itemList.size() < PerFetchNum) {
            if (status <= PopularizedStatus.Normal) {
                normalPreviousMaxId = 0;
            } else if (status <= PopularizedStatus.HotSale) {
                hotPreviousMaxId = 0;
            } else {
                return itemList;
            }
            //ywj add, fulfill items
            log.error("the items size: " + itemList.size() + ", small than " + PerFetchNum + ", so fulfill items");
            int needNum = PerFetchNum - itemList.size();
            List<Popularized> tempList = findByTitle(title, catName, status, needNum);
            for (Popularized temp : tempList) {
                if (!itemList.contains(temp))
                    itemList.add(temp);
            }
        }
        
        itemList = PopularizedDao.randomItems(itemList, limitNum);
        
        return itemList;
    }
    
    private static List<Popularized> findByTitle(String title, String catName, int status, int fetchNum) {
        long startId = 0;
        if (status <= PopularizedStatus.Normal) {
            startId = normalPreviousMaxId;
        } else if (status <= PopularizedStatus.HotSale) {
            startId = hotPreviousMaxId;
        } else {
            return new ArrayList<Popularized>();
        }
        
        List<Popularized> itemList = ShoppingDao.findByTitle(startId, title, catName, status, fetchNum);
        if (CommonUtils.isEmpty(itemList))
            return new ArrayList<Popularized>();
        else {
            int size = itemList.size();
            long maxId = itemList.get(size - 1).getId();
            if (status <= PopularizedStatus.Normal) {
                normalPreviousMaxId = maxId;
            } else if (status <= PopularizedStatus.HotSale) {
                hotPreviousMaxId = maxId;
            }
            
            return itemList;
        }
    }
    
    public static boolean isInnerIP(String ipAddress){   
        boolean isInnerIp = false;   
        long ipNum = getIpNum(ipAddress);   
        /**  
        私有IP：A类  10.0.0.0-10.255.255.255  
               B类  172.16.0.0-172.31.255.255  
               C类  192.168.0.0-192.168.255.255  
        当然，还有127这个网段是环回地址  
        **/  
        long aBegin = getIpNum("10.0.0.0");   
        long aEnd = getIpNum("10.255.255.255");   
        long bBegin = getIpNum("172.16.0.0");   
        long bEnd = getIpNum("172.31.255.255");   
        long cBegin = getIpNum("192.168.0.0");   
        long cEnd = getIpNum("192.168.255.255");   
        isInnerIp = isInner(ipNum,aBegin,aEnd) || isInner(ipNum,bBegin,bEnd) 
        		|| isInner(ipNum,cBegin,cEnd) || ipAddress.equals("127.0.0.1");   
        return isInnerIp;              
    }  
    
    private static long getIpNum(String ipAddress) {   
        String [] ip = ipAddress.split("\\.");   
        long a = Integer.parseInt(ip[0]);   
        long b = Integer.parseInt(ip[1]);   
        long c = Integer.parseInt(ip[2]);   
        long d = Integer.parseInt(ip[3]);   
      
        long ipNum = a * 256 * 256 * 256 + b * 256 * 256 + c * 256 + d;   
        return ipNum;   
    }  
    
    private static boolean isInner(long userIp,long begin,long end){   
        return (userIp>=begin) && (userIp<=end);   
    }  
    
    public static String jsonpFormat(List<Popularized> res, String callback) {
    	String json = JsonUtil.getJson(res);

        StringBuilder sb = new StringBuilder();
        sb.append(callback);
        sb.append('(');
        sb.append(json);
        sb.append(')');

        json = sb.toString();
        return json;
    }
}
