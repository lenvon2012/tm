package controllers;

import java.util.List;

import models.item.ItemPlay;
import models.user.TitleOptimised;
import models.user.User;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import result.TMResult;

import com.ciaosir.client.pojo.PageOffset;
import com.dbt.cred.utils.JsonUtil;

import dao.item.ItemDao;

public class TitlesUI extends TMController {

    private static final Logger log = LoggerFactory.getLogger(TitlesUI.class);
    
    public static void searchItemsByRules(String title, int itemStatus, long tbCid, long sellerCid,
            int startScore, int endScore,
            String orderBy, boolean isDesc, int pn, int ps) {
        
        User user = getUser();
        PageOffset po = new PageOffset(pn, ps);
        
        String catIdStr = "";
        if (tbCid > 0) {
            catIdStr = tbCid + "";
        }
        String sellerCatIdStr = "";
        if (sellerCid != 0L) {
            sellerCatIdStr = sellerCid + "";
        }
        
        int sort = -1;
        
        if (StringUtils.isEmpty(orderBy) == false) {
            if (isDesc == true) {
                sort = 2;
            } else {
                sort = 1;
            }
        }
        
        
        List<ItemPlay> itemList = ItemDao.findOnlineByUserWithArgs(user.getId(), po.getOffset(), po.getPs(), 
                title, startScore, endScore, 
                sort, itemStatus, sellerCatIdStr, tbCid, false);
        
        for (ItemPlay itemPlay : itemList) {
            TitleOptimised optimised = TitleOptimised.findByUserId(user.getId(), itemPlay.getNumIid());
            if (optimised == null) {
                itemPlay.setOptimised(false);
                continue;
            }
            itemPlay.setOptimised(true);
            itemPlay.setLastOptimiseTs(optimised.getTs());
        }
        
        int count = (int) ItemDao.countOnlineByUserWithArgs(user.getId(), startScore, endScore, title, 0L,
                itemStatus, sellerCatIdStr, tbCid);
        
        TMResult tmRes = new TMResult(itemList, count, po);
                
        renderJSON(JsonUtil.getJson(tmRes));
    }
    
}
