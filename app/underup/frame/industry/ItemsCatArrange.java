package underup.frame.industry;

import models.item.ItemCatPlay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;

public class ItemsCatArrange {
    private static final Logger log = LoggerFactory.getLogger(ItemsCatArrange.class);

    public static List<ItemCatLevel2> level2Arrange(long levelOneCid, long year, long month) {
        List<ItemCatLevel2> itemCatsLevel2 = ItemCatLevel2.getLevel2(levelOneCid, year, month);
        List<ItemCatLevel2> itemCats = new ArrayList<ItemCatLevel2>();
        if (itemCatsLevel2 == null) {
            log.error("can't get the info from ItemCatLevel2");
            return null;
        }
        log.info("----levelOneCid" + levelOneCid);
        // 得到二级目录为父目录的目录
        List<ItemCatLevel2> itemCatsLevel2Parents = ItemCatLevel2.getLevel2Parent(levelOneCid, year, month);
        if (itemCatsLevel2Parents != null) {
            // 遍历二级目录为父目录的目录
            for (ItemCatLevel2 itemCatLevel2 : itemCatsLevel2Parents) {
                // 取出二级目录为父目录的子类目
                itemCats.add(itemCatLevel2);
                List<ItemCatLevel2> itemCatLevel2Children = ItemCatLevel2.getLevel2Children(levelOneCid, year, month,
                        itemCatLevel2.getCid());
                if (itemCatLevel2Children != null) {
                    // 遍历二级子目录
                    for (ItemCatLevel2 itemCatLevel2Child : itemCatLevel2Children) {
                        if (itemCatLevel2Child.getIsParent()) {
                            itemCats.add(itemCatLevel2Child);
                            // 遍历三级目录
                            List<ItemCatLevel2> itemCatLevel3Children = ItemCatLevel2.getLevel2Children(levelOneCid, year, month,
                                    itemCatLevel2Child.getCid());
                            if (itemCatLevel3Children != null) {
                                for (ItemCatLevel2 itemCatLevel3Child : itemCatLevel3Children) {
                                    itemCats.add(itemCatLevel3Child);
                                }
                            }
                        } else {
                            itemCats.add(itemCatLevel2Child);
                        }
                    }
                }
            }
        }
        log.info("-----after:" + itemCats.size());
        for (ItemCatLevel2 itemCatLevel2 : itemCatsLevel2) {
//            if (!itemCats.contains(itemCatLevel2)) {
//                itemCats.add(itemCatLevel2);
//            }
            boolean flag = true;
            for(ItemCatLevel2 itemCat: itemCats){
                if(itemCatLevel2.getCid() == itemCat.getCid()){
                    flag = false;
                    break;
                }
            }
            if(flag){
                itemCats.add(itemCatLevel2);
            }
        }
        log.info("-----after1:" + itemCats.size());
        return itemCats;
    }
    
    public static List<Long> getChildrenCids(long cid, long year, long month){
        List<Long> childrenCids = new ArrayList<Long>();
        ItemCatPlay itemCatPlay1 = ItemCatPlay.findByCid(cid);
        long parentCid = itemCatPlay1.getParentCid();
        if (itemCatPlay1.getLevel() != 2) {
            parentCid = ItemCatPlay.findByCid(parentCid).getParentCid();
        }
        long levelOneCid = parentCid;
        List<ItemCatLevel2> itemCatChildren = ItemCatLevel2.getLevel2Children(levelOneCid, year, month, cid);
        if (itemCatChildren != null) {
            for (ItemCatLevel2 itemCatChild : itemCatChildren) {
                if (itemCatChild.getIsParent()) {
                    List<ItemCatLevel2> itemCatLevel3Children = ItemCatLevel2.getLevel2Children(levelOneCid, year, month,
                            itemCatChild.getCid());
                    if (itemCatLevel3Children != null) {
                        for (ItemCatLevel2 itemCatLevel3Child : itemCatLevel3Children) {
                            childrenCids.add(itemCatLevel3Child.getCid());
                        }
                    }
                } else {
                    childrenCids.add(itemCatChild.getCid());
                }
            }
        }
        return childrenCids;
    }

}
