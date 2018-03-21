
package underup.frame.industry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import models.item.ItemCatPlay;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

//@OnApplicationStart
public class UpdateItemsCatJob extends Job {
    private static final Logger log = LoggerFactory.getLogger(UpdateItemsCatJob.class);

    private Map<Long, List<ItemCatPlay>> secondMap = new HashMap<Long, List<ItemCatPlay>>();

    List<ItemCatPlay> firstRemove = new ArrayList<ItemCatPlay>();

    List<ItemCatPlay> secondAdd = new ArrayList<ItemCatPlay>();

    public void doJob() {
        firstRemove.clear();
        // 获取一级类目信息
        List<ItemCatPlay> itemCatsLevel1 = ItemCatPlay.findAllFirstLevelCats();
        if (itemCatsLevel1 == null) {
            log.error("there is an error when get the first level cid from database item_cat_play and confirm that there are some datas in the database item_cat_play");
            return;
        }
        // 遍历一级类目
        for (ItemCatPlay itemCatLevel1 : itemCatsLevel1) {
            secondAdd.clear();
            long secondKey = itemCatLevel1.getCid();
            if (itemCatLevel1.isParent()) {
                int len = getChildLevel(itemCatLevel1);
                if (len == 0) {
                    firstRemove.add(itemCatLevel1);
                }
            } else {
                if (CatTopSaleItemSQL.findIfExisted(itemCatLevel1.getCid())) {
                    secondAdd.add(itemCatLevel1);
                } else {
                    firstRemove.add(itemCatLevel1);
                }
            }
            if (secondAdd.size() != 0) {
                List<ItemCatPlay> itemCatTemps = new ArrayList<ItemCatPlay>();
                for (ItemCatPlay itemCatTemp : secondAdd) {
                    itemCatTemps.add(itemCatTemp);
                }
                secondMap.put(secondKey, itemCatTemps);
            }

        }
        for (ItemCatPlay itemCatLevel1 : firstRemove) {
            itemCatsLevel1.remove(itemCatLevel1);
        }

        // 把一级目录存入数据库
        if (!CommonUtils.isEmpty(itemCatsLevel1)) {
            for (ItemCatPlay itemCatPlay : itemCatsLevel1) {
                long cid = itemCatPlay.getCid();
                String name = itemCatPlay.getName();
                new ItemCatLevel1(cid, name).jdbcSave();
            }
        }

        // 把二级目录存入数据库
        save();
        //触发属性的job
        new UpdateItemProps().doJob();
    }

    // 为父目录时
    int getChildLevel(ItemCatPlay itemCat) {
        int itemLevelCount = 0;
        List<ItemCatPlay> itemCatChildren = ItemCatPlay.findByParentCid(itemCat.getCid());
        if (CommonUtils.isEmpty(itemCatChildren)) {
            log.info("can't get info from item_cat_play by parentcid");
            return itemLevelCount;
        }
        for (ItemCatPlay itemCatChild : itemCatChildren) {
            if (itemCatChild.isParent()) {
                int l = getChildLevel(itemCatChild);
                itemLevelCount += l;
                if (l > 0)
                    secondAdd.add(itemCatChild);
            } else {
                if (CatTopSaleItemSQL.findIfExisted(itemCatChild.getCid())) {
                    itemLevelCount++;
                    secondAdd.add(itemCatChild);
                }
            }
        }
        return itemLevelCount;
    }

    // 把信息存入数据库
    void save() {
        List<Long> level2String = new ArrayList<Long>(secondMap.keySet());
        if (CommonUtils.isEmpty(level2String)) {
            log.error("secondMap failed......");
            return;
        }
        log.info("secondMap are " + level2String);
        for (Long s1 : level2String) {
            List<ItemCatPlay> itemCatPlays = secondMap.get(s1);
            if (CommonUtils.isEmpty(itemCatPlays)) {
                log.info("error occur with secondMap with s1 " + s1);
                continue;
            }
            for (ItemCatPlay itemCatPlay : itemCatPlays) {
                log.info("insert into second cid = " + itemCatPlay.getCid());
                long levelOneCid = s1;
                long cid = itemCatPlay.getCid();
                String name = itemCatPlay.getName();
                boolean isParent = itemCatPlay.isParent();
                long parentCid = itemCatPlay.getParentCid();
                int level = itemCatPlay.getLevel();
                new ItemCatLevel2(levelOneCid, cid, name, isParent, parentCid, level).jdbcSave();
            }

        }

    }

}
