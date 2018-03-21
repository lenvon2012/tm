package controllers;

import actions.batch.BatchEditResult;
import actions.batch.RemoveLinksAction;
import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.JsonUtil;
import dao.fenxiao.ItemDescDao;
import models.fenxiao.ItemDescLinks;
import models.fenxiao.ItemDescPlay;
import models.user.User;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import result.TMResult;
import utils.TBItemUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class RemoveLinks extends TMController {

    private static final Logger log = LoggerFactory.getLogger(RemoveLinks.class);
    
    
    public static void index() {
        render("newAutoTitle/removelinks.html");
    }
    
    
    public static void searchLinkItems(String title, Long catId, Long sellerCatId, int status,
            int pn, int ps) {
        
        User user = getUser();
        PageOffset po = new PageOffset(pn, ps);
        
        List<ItemDescLinks> descLinkList = ItemDescLinks.findLinksByUserId(user.getId());
        if (CommonUtils.isEmpty(descLinkList)) {
            renderJSON(JsonUtil.getJson(new TMResult(descLinkList, 0, po)));
        }

        HashMap<String, Long> linkActionMap = new HashMap<String, Long>();
        for (ItemDescLinks itemDescLink : descLinkList) {
            linkActionMap.put(itemDescLink.getLink(), itemDescLink.getAction());
        }
        
        
        String catIdStr = "";
        if (catId != null && catId > 0) {
            catIdStr = catId + "";
        }
        String sellerCatIdStr = "";
        if (sellerCatId != null && sellerCatId > 0) {
            sellerCatIdStr = sellerCatId + "";
        }
        
        TMResult tmRes = ItemDescDao.findByItemRulesWithPaging(user.getId(), title, catIdStr, sellerCatIdStr, 
                status, po);
        
        List<ItemDescPlay> itemDescList = (List<ItemDescPlay>) tmRes.getRes();

        if (!CommonUtils.isEmpty(itemDescList)) {
            for (ItemDescPlay itemDescPlay : itemDescList) {
                itemDescPlay.checkLinkActionMap(linkActionMap);
            }
        }

        
        renderJSON(JsonUtil.getJson(tmRes));
        
    }
    
    public static void getTMControllerUser() {
    	User user = getUser();
    	renderJSON(JsonUtil.getJson(user));
    }
    
    public static void searchAllLinks(String link, int pn, int ps) {
        
        User user = getUser();
        PageOffset po = new PageOffset(pn, ps);
        
        TMResult tmRes = ItemDescLinks.findLinksLike(user.getId(), link, po);
        
        renderJSON(JsonUtil.getJson(tmRes));
                
        
    }
    
    
    
    public static void updateLinkAction(String ids, Long actionType) {
        User user = getUser();
        
        Set<Long> idSet = TBItemUtil.parseIdsToSet(ids);
        
        if (CommonUtils.isEmpty(idSet)) {
            renderError("请先选择要修改的链接！");
        }
        ids = StringUtils.join(idSet, ",");
        Boolean isSuccess = ItemDescLinks.updateLinkAction(user.getId(), ids, actionType);
        if (Boolean.FALSE.equals(isSuccess)) {
            renderError("更新出错，请刷新重试，或联系我们！");
        }
        renderTMSuccess("链接状态配置成功！");
    }

    public static void updateAllLinkAction(String link, Long actionType) {
        User user = getUser();
        
        boolean isSuccess = ItemDescLinks.updateAllLinkAction(user.getId(), link, actionType);
        if (isSuccess == false) {
            renderError("没有修改任何链接的配置！");
        }
        
        renderTMSuccess("链接状态配置成功！");
    }
    
    
    public static void doRemoveSelectItemLinks(String numIids) {
        
        User user = getUser();
        
        Set<Long> numIidSet = TBItemUtil.parseIdsToSet(numIids);
        
        if (CommonUtils.isEmpty(numIidSet)) {
            renderError("请先选择要删除外链的宝贝！");
        }
        List<ItemDescPlay> itemDescList = ItemDescDao.findByUserIdNumIids(user.getId(), numIids);
        
        BatchEditResult removeRes = RemoveLinksAction.doRemoveItemLinks(user, itemDescList);
        
        if (removeRes.isSuccess() == false) {
            renderError(removeRes.getMessage());
        }
        
        renderResultJson(removeRes);
                
    }
    
    
    public static void doRemoveAllItemLinks(String title, Long catId, Long sellerCatId, int status) {
        
        User user = getUser();
        
        String catIdStr = "";
        if (catId != null && catId > 0) {
            catIdStr = catId + "";
        }
        String sellerCatIdStr = "";
        if (sellerCatId != null && sellerCatId > 0) {
            sellerCatIdStr = sellerCatId + "";
        }
        
        
        List<ItemDescPlay> itemDescList = ItemDescDao.findByItemRules(user.getId(), title, 
                catIdStr, sellerCatIdStr, status);
        
        BatchEditResult removeRes = RemoveLinksAction.doRemoveItemLinks(user, itemDescList);
        
        if (removeRes.isSuccess() == false) {
            renderError(removeRes.getMessage());
        }
        
        renderResultJson(removeRes);
                
    }


}
