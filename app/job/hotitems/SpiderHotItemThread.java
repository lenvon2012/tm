package job.hotitems;

import java.util.ArrayList;
import java.util.List;

import models.item.ItemCatPlay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import configs.TMConfigs;

public class SpiderHotItemThread extends Job {

    private static final Logger log = LoggerFactory.getLogger(SpiderHotItemThread.class);
    
    private static List<ItemCatPlay> findNeedCatList() {
        
        List<ItemCatPlay> level3CatList = ItemCatPlay.findByLevel(3);

        List<ItemCatPlay> catList = new ArrayList<ItemCatPlay>();
        catList.addAll(level3CatList);
        
        List<ItemCatPlay> level2CatList = ItemCatPlay.findByLevel(2);
        for (ItemCatPlay itemCat : level2CatList) {
            if (itemCat.isParent()) {
                continue;
            }
            catList.add(itemCat);
        }
        
        List<ItemCatPlay> level1CatList = ItemCatPlay.findByLevel(1);
        for (ItemCatPlay itemCat : level1CatList) {
            if (itemCat.isParent()) {
                continue;
            }
            catList.add(itemCat);
        }
        
        return catList;
    }
    
    @Override
    public void doJob() {
        if (!TMConfigs.Server.jobTimerEnable) {
            log.error("this is not master!!!");
            return;
        }
        if (TMConfigs.IsSpiderCatHotItems == false) {
            log.error("not switch to spider cat hot items");
            return;
        }
        
        List<ItemCatPlay> catList = findNeedCatList();
        
        log.info(catList.size() + " category need to spider cat hot items---------------------------");
        
        //catList = catList.subList(0, 5);
        
        int cidIndex = 1;
        int totalCount = catList.size();
        
        for (ItemCatPlay itemCat : catList) {
            
            try {
                if (itemCat == null) {
                    continue;
                }
                SpiderHotItemJob catSpiderJob = new SpiderHotItemJob(cidIndex, totalCount, 
                        itemCat.getCid(), itemCat.getName());
                
                catSpiderJob.call();
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
            
            cidIndex++;
        }
        
    }
    
    
}
