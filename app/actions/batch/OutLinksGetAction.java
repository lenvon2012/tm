package actions.batch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.fenxiao.ItemDescLinks;
import models.fenxiao.ItemDescPlay;
import models.fenxiao.ItemDescLinks.ActionType;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.url.URLParser;
import com.taobao.api.domain.Item;

import dao.fenxiao.ItemDescDao;
import dao.item.ItemDao;

public class OutLinksGetAction {

    private static final Logger log = LoggerFactory.getLogger(OutLinksGetAction.class);
    
    
    public static List<ItemDescLinks> doCheckItemOutLinks(User user, Collection<Item> tbItemList,
            boolean isAllItems) {
        
        try {
            
            long startTime = System.currentTimeMillis();
            
            if (CommonUtils.isEmpty(tbItemList)) {
                tbItemList = new ArrayList<Item>();
            }

            Map<Long, ItemDescPlay> itemDescMap = findUserItemDescMap(user);
            Map<String, ItemDescLinks> descLinkMap = findUserDescLinkMap(user);
            
            Set<Long> allNumIidSet = findAllUserNumIidSet(user, tbItemList);
            
            Set<String> totalItemLinkSet = new HashSet<String>();
            
            for (Item tbItem : tbItemList) {
                if (tbItem == null) {
                    continue;
                }
                
                ItemDescPlay itemDesc = itemDescMap.get(tbItem.getNumIid());
                
                Set<String> itemLinkSet = findItemLinkSet(user, allNumIidSet, tbItem);
                if (CommonUtils.isEmpty(itemLinkSet)) {
                    if (itemDesc != null) {
                        itemDesc.rawDelete();
                    }
                    continue;
                } else {
                    
                    totalItemLinkSet.addAll(itemLinkSet);
                    
                    if (itemDesc == null) {
                        itemDesc = new ItemDescPlay(user.getId(), tbItem.getNumIid());
                    } 
                    
                    itemDesc.updateByTbItem(tbItem, itemLinkSet);
                    
                    itemDesc.jdbcSave();
                }
                
            }
            
            List<ItemDescLinks> newAddDescLinkList = new ArrayList<ItemDescLinks>();
            for (String link : totalItemLinkSet) {
                if (descLinkMap.containsKey(link)) {
                    continue;
                }
                ItemDescLinks descLink = new ItemDescLinks(user.getId(), link, ActionType.REMOVE_IT);
                descLink.jdbcSave();
                newAddDescLinkList.add(descLink);
            }
            
            
            if (isAllItems == true) {
                //删除多余的ItemDescPlay
                for (ItemDescPlay itemDesc : itemDescMap.values()) {
                    if (itemDesc == null) {
                        continue;
                    }
                    if (allNumIidSet.contains(itemDesc.getNumIid()) == false) {
                        itemDesc.rawDelete();
                    }
                }
                //删除多余的ItemDescLinks
                for (ItemDescLinks descLink : descLinkMap.values()) {
                    if (descLink == null) {
                        continue;
                    }
                    if (totalItemLinkSet.contains(descLink.getLink()) == false) {
                        descLink.rawDelete();
                    }
                }
            }
            long endTime = System.currentTimeMillis();
            
            log.info("end get item out links for user: " + user.getUserNick() 
                    + ", used " + (endTime - startTime) + " ms-----------");
            
            return newAddDescLinkList;
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new ArrayList<ItemDescLinks>();
        }
        
    }
    
    
    private static Set<String> findItemLinkSet(User user, Set<Long> allNumIidSet, Item tbItem) {
    
        // 只去除href里面的，还是其他的http一起
        // Pattern pattern =
        // Pattern.compile("((href=('|\")?)|((=('|\")?)?http://)){1}.[\\w\\.\\-/:?!_&%=;,]+('|\")?");
        
        final String desc = tbItem.getDesc();
        if (StringUtils.isEmpty(desc)) {
            return new HashSet<String>();
        }
        
        Pattern pattern = Pattern.compile("(href=('|\")?){1}[\\w\\.\\-/:?!_&%=;,]+('|\")?");
        Matcher matcher = pattern.matcher(desc);
        Set<String> itemLinkSet = new HashSet<String>();
        
        while (matcher.find()) {
            String link = matcher.group();
            if (StringUtils.isEmpty(link) || link.startsWith("=")) {
                continue;
            }

            if (link.startsWith("href=")) {
                link = link.replaceFirst("href=('|\")?", StringUtils.EMPTY);
                if (link.endsWith("\"") || link.endsWith("'")) {
                    link = link.substring(0, link.length() - 1);
                }
            }

            String escapeHttpLink = link;
            if (escapeHttpLink.startsWith("http://")) {
                escapeHttpLink = escapeHttpLink.substring("http://".length());
            }

            Long numIid = URLParser.findItemId(escapeHttpLink);
            if (numIid != null && allNumIidSet.contains(numIid)) {
                continue;
            }

            itemLinkSet.add(link);
        }
        
        return itemLinkSet;
    }
    
    
    private static Set<Long> findAllUserNumIidSet(User user, Collection<Item> tbItemList) {
        
        Set<Long> allNumIidSet = ItemDao.findNumIidWithUser(user.getId());
        
        if (CommonUtils.isEmpty(tbItemList) == false) {
            for (Item tbItem : tbItemList) {
                if (tbItem == null) {
                    continue;
                }
                allNumIidSet.add(tbItem.getNumIid());
            }
        }
        
        return allNumIidSet;
    }
    
    
    private static Map<String, ItemDescLinks> findUserDescLinkMap(User user) {
     
        List<ItemDescLinks> descLinkList = ItemDescLinks.findLinksByUserId(user.getId());
        Map<String, ItemDescLinks> descLinkMap = new HashMap<String, ItemDescLinks>();
        
        for (ItemDescLinks descLink : descLinkList) {
            if (descLink == null) {
                continue;
            }
            descLinkMap.put(descLink.getLink(), descLink);
        }
        
        return descLinkMap;
        
    }
    
    private static Map<Long, ItemDescPlay> findUserItemDescMap(User user) {
        List<ItemDescPlay> itemDescList = ItemDescDao.findByUserId(user.getId());
        Map<Long, ItemDescPlay> itemDescMap = new HashMap<Long, ItemDescPlay>();
        
        for (ItemDescPlay itemDesc : itemDescList) {
            if (itemDesc == null) {
                continue;
            }
            itemDescMap.put(itemDesc.getNumIid(), itemDesc);
        }
        
        return itemDescMap;
    }
    
}
