package actions.batch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.fenxiao.ItemDescLinks;
import models.fenxiao.ItemDescLinks.ActionType;
import models.fenxiao.ItemDescPlay;
import models.fenxiao.ItemDescPlay.ItemDescOpStatus;
import models.fenxiao.RemoveDescLinkLog.RemoveDescLinkLogStatus;
import models.fenxiao.RemoveDescLinkLog;
import models.item.ItemPlay;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.TBItemUtil;

import actions.batch.BatchEditResult.BatchEditErrorMsg;
import actions.batch.BatchEditResult.BatchEditResStatus;
import bustbapi.ItemApi;

import com.ciaosir.client.CommonUtils;
import com.taobao.api.domain.Item;

import dao.item.ItemDao;

public class RemoveLinksAction {

    private static final Logger log = LoggerFactory.getLogger(RemoveLinksAction.class);
 
    private static final int MaxRemoveLinkItemNum = 5000;
    
    public static BatchEditResult doRemoveItemLinks(User user, List<ItemDescPlay> itemDescList) {
        
        if (CommonUtils.isEmpty(itemDescList)) {
            return new BatchEditResult(false, "没有需要去除链接的宝贝！");
        }
        
        List<ItemDescLinks> descLinkList = ItemDescLinks.findLinksByUserIdAction(user.getId(), 
                ActionType.REMOVE_IT);
        
        
        return doRemoveItemLinks(user, itemDescList, descLinkList);
        
    }
    
    
    public static BatchEditResult doRemoveItemLinks(User user, 
            List<ItemDescPlay> itemDescList, List<ItemDescLinks> deleteLinkList) {
       
        if (CommonUtils.isEmpty(itemDescList)) {
            return new BatchEditResult(false, "没有需要去除链接的宝贝！");
        }
        if (CommonUtils.isEmpty(deleteLinkList)) {
            return new BatchEditResult(false, "没有需要去除的链接！");
        }
        
        if (itemDescList.size() > MaxRemoveLinkItemNum) {
            return new BatchEditResult(false, "为防止您过久等待，一次最多只能去除" + MaxRemoveLinkItemNum + "个宝贝的外链！");
        }
        
        Set<Long> numIidSet = new HashSet<Long>();
        for (ItemDescPlay itemDesc : itemDescList) {
            if (itemDesc == null) {
                continue;
            }
            numIidSet.add(itemDesc.getNumIid());
        }
        //获取最新的宝贝描述
        Map<Long, Item> tbItemMap = TBItemUtil.findItemDescMap(user, numIidSet);
        Map<Long, ItemPlay> itemPlayMap = findItemPlayMap(user, numIidSet);
        
        Set<Long> successNumIidSet = new HashSet<Long>();
        List<BatchEditErrorMsg> errorMsgList = new ArrayList<BatchEditErrorMsg>();
        
        for (ItemDescPlay itemDesc : itemDescList) {
            
            if (itemDesc == null) {
                continue;
            }
            if (BatchEditResult.checkIsContinueExecute(errorMsgList) == false) {
                break;
            }
            
            Item tbItem = tbItemMap.get(itemDesc.getNumIid());
            ItemPlay itemPlay = itemPlayMap.get(itemDesc.getNumIid());
            
            BatchEditErrorMsg errorMsg = doRemoveForOneItem(user, itemDesc, tbItem, itemPlay, 
                    deleteLinkList);
            
            if (BatchEditResStatus.Success.equals(errorMsg.getStatus())) {
                successNumIidSet.add(itemPlay.getNumIid());
            } else {
                errorMsgList.add(errorMsg);
            }
        }
        
        
        int notExecuteNum = itemDescList.size() - successNumIidSet.size() - errorMsgList.size();
        
        String prevMessage = "成功去除" + successNumIidSet.size() + "个宝贝的外链";
        
        if (CommonUtils.isEmpty(errorMsgList) == false) {
            prevMessage += "，失败了" + errorMsgList.size() + "个宝贝";
        }
        if (notExecuteNum > 0) {
            prevMessage += "，有" + notExecuteNum + "个宝贝尚未执行";
        }
        
        BatchEditResult removeRes = new BatchEditResult(prevMessage, errorMsgList);
        
        return removeRes;
        
    }
    
    
    private static BatchEditErrorMsg doRemoveForOneItem(User user, ItemDescPlay itemDesc,
            Item tbItem, ItemPlay itemPlay, List<ItemDescLinks> deleteLinkList) {
        
        if (tbItem == null || itemPlay == null) {
            BatchEditErrorMsg errorMsg = new BatchEditErrorMsg(BatchEditResStatus.OtherError,
                    "从淘宝找不到该宝贝！", itemPlay);
            return errorMsg;
        }
        
        String desc = tbItem.getDesc();
        if (StringUtils.isEmpty(desc)) {
            BatchEditErrorMsg errorMsg = new BatchEditErrorMsg(BatchEditResStatus.OtherError,
                    "宝贝详情为空！", itemPlay);
            return errorMsg;
        }
        final String originDesc = desc;
        itemDesc.setDesc(desc);
        boolean isSuccess = itemDesc.rawUpdate();
        if (isSuccess == false) {
            BatchEditErrorMsg errorMsg = new BatchEditErrorMsg(BatchEditResStatus.DBError,
                    "数据库出现异常，请联系我们！", itemPlay);
            return errorMsg;
        }
        
        Set<String> itemLinkSet = getItemLinkSet(itemDesc);
        Set<String> removedLinkSet = new HashSet<String>();
        
        for (ItemDescLinks deleteLinkObj : deleteLinkList) {
            String deleteLinkStr = deleteLinkObj.getLink();
            if (StringUtils.isEmpty(deleteLinkStr)) {
                continue;
            }
            itemLinkSet.remove(deleteLinkStr);
            if (desc.contains(deleteLinkStr)) {
                removedLinkSet.add(deleteLinkStr);
            }
            
            String formatDeleteLink = deleteLinkStr.replace("?", "\\?").replace("|", "\\|")
                    .replace("[", "\\[").replace("]", "\\]");
            
            desc = desc.replaceAll("href=('|\")?" + formatDeleteLink + "('|\")?", StringUtils.EMPTY);
            desc = desc.replaceAll(formatDeleteLink, StringUtils.EMPTY);
        }
        if (StringUtils.equals(desc, itemDesc.getDesc()) == true) {
            return new BatchEditErrorMsg(BatchEditResStatus.Success, "", itemPlay);
        }
        
        ItemApi.ItemUpdate updateApi = new ItemApi.ItemUpdate(user.getSessionKey(), 
                tbItem.getNumIid(), desc);
        Item resItem = updateApi.call();
        if (resItem == null || updateApi.isApiSuccess() == false) {
            return new BatchEditErrorMsg(BatchEditResStatus.CallApiError, updateApi.getErrorMsg(), 
                    itemPlay);
        }
        
        //数据库中插入log
        RemoveDescLinkLog removeLog = new RemoveDescLinkLog(user.getId(), tbItem.getNumIid(), originDesc, 
                StringUtils.join(removedLinkSet, ItemDescPlay.LinkSeparator), 
                RemoveDescLinkLogStatus.NewCreate);
        removeLog.rawInsert();
        
        if (CommonUtils.isEmpty(itemLinkSet)) {
            itemDesc.rawDelete();
        } else {
            //更新宝贝链接
            itemDesc.setDesc(desc);
            itemDesc.setStatus(ItemDescOpStatus.hasDeletedBefore);
            itemDesc.setLinks(StringUtils.join(itemLinkSet, ItemDescPlay.LinkSeparator));
            isSuccess = itemDesc.rawUpdate();
            
            if (isSuccess == false) {
                BatchEditErrorMsg errorMsg = new BatchEditErrorMsg(BatchEditResStatus.DBError,
                        "数据库出现异常，请联系我们！", itemPlay);
                return errorMsg;
            }
        }
        
        
        return new BatchEditErrorMsg(BatchEditResStatus.Success, "", itemPlay);
    }
    
    private static Set<String> getItemLinkSet(ItemDescPlay itemDesc) {
        String links = itemDesc.getLinks();
        if (StringUtils.isEmpty(links)) {
            return new HashSet<String>();
        }
        
        String[] linkArr = StringUtils.split(links, ItemDescPlay.LinkSeparator);
        Set<String> linkSet = new HashSet<String>();
        
        for (String link : linkArr) {
            if (StringUtils.isEmpty(link)) {
                continue;
            }
            linkSet.add(link);
        }
        
        return linkSet;
    }
    
    
    private static Map<Long, ItemPlay> findItemPlayMap(User user, Set<Long> numIidSet) {
        if (CommonUtils.isEmpty(numIidSet)) {
            return new HashMap<Long, ItemPlay>();
        }
        
        List<ItemPlay> itemList = ItemDao.findByNumIids(user.getId(), numIidSet);
        
        Map<Long, ItemPlay> itemPlayMap = new HashMap<Long, ItemPlay>();
        
        for (ItemPlay item : itemList) {
            if (item == null) {
                continue;
            }
            itemPlayMap.put(item.getNumIid(), item);
        }
        
        return itemPlayMap;
    }
    
}
