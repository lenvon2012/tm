package actions.weibo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import models.user.User;
import models.weibo.SocialAccountPlay;
import models.weibo.SocialAccountPlay.SocialAccountFunction;
import models.weibo.WeiboMsgPlay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;

public class WeiboMsgAction {

    private static final Logger log = LoggerFactory.getLogger(WeiboMsgAction.class);
    
    //上一次微博的offset
    private static long PrevWeiboOffset = 0L;
    protected static final int PageSize = 50;
    protected static final int RandNum = 20;
    protected static final int LoopCount = 10;
    
    public static List<WeiboMsgPlay> queryRandomWeiboList(User user, int accountType) {
        
        List<WeiboMsgPlay> allWeiboList = new ArrayList<WeiboMsgPlay>();
        
        long tempOffset = PrevWeiboOffset;
        boolean hasBackToFront = false;//数据库查询返回到了开头
        
        SocialAccountPlay slaveAccount = SocialAccountPlay.findByFunction(user.getId(), accountType, 
                SocialAccountFunction.SlaveAccount);
        
        for (int i = 0; i < LoopCount; i++) {
            //所有的记录都查询过了
            if (hasBackToFront == true && PrevWeiboOffset >= tempOffset) {
                break;
            }
            
            List<WeiboMsgPlay> tempList = WeiboMsgPlay.findByAccountType(accountType, PrevWeiboOffset, PageSize);
            if (CommonUtils.isEmpty(tempList)) {
                tempList = new ArrayList<WeiboMsgPlay>();
            }
            if (CommonUtils.isEmpty(tempList) || tempList.size() < PageSize) {
                PrevWeiboOffset = 0;
                hasBackToFront = true;
            }
            
            tempList = WeiboMsgValidAction.getValidWeiboList(user, accountType, slaveAccount, tempList);
            if (CommonUtils.isEmpty(tempList)) {
                continue;
            }
            addUniqueWeiboList(allWeiboList, tempList);
            if (allWeiboList.size() >= RandNum) {
                break;
            }
        }
        
        allWeiboList = getRandomWeiboList(allWeiboList);
        
        return allWeiboList;
    }
    
    
    
    //加入到allWeiboList，去掉重复的weibo，userId先可以相同
    private static void addUniqueWeiboList(List<WeiboMsgPlay> allWeiboList, List<WeiboMsgPlay> tempList) {
        if (CommonUtils.isEmpty(tempList)) {
            return;
        }
        Set<String> weiboIdSet = new HashSet<String>();
        for (WeiboMsgPlay weibo : allWeiboList) {
            weiboIdSet.add(weibo.getWeiboId());
        }
        
        for (WeiboMsgPlay weibo : tempList) {
            if (weiboIdSet.contains(weibo.getWeiboId())) {
                continue;
            }
            allWeiboList.add(weibo);
            weiboIdSet.add(weibo.getWeiboId());
        }
        
    }
    
    
    //从allWeiboList中随即获取几个微博
    private static List<WeiboMsgPlay> getRandomWeiboList(List<WeiboMsgPlay> allWeiboList) {
        
        if (CommonUtils.isEmpty(allWeiboList)) {
            return new ArrayList<WeiboMsgPlay>(); 
        }
        if (allWeiboList.size() <= RandNum) {
            return allWeiboList;
        }
        List<WeiboMsgPlay> randomWeiboList = new ArrayList<WeiboMsgPlay>();
        
        for (int i = 0; i < RandNum; i++) {
            if (CommonUtils.isEmpty(allWeiboList)) {
                break;
            }
            int randomIndex = (int) (Math.random() * allWeiboList.size());
            if (randomIndex >= allWeiboList.size()) {
                randomIndex = allWeiboList.size() - 1;
            }
            WeiboMsgPlay weibo = allWeiboList.get(randomIndex);
            randomWeiboList.add(weibo);
            allWeiboList.remove(randomIndex);
        }
        
        return randomWeiboList;
    }
}
