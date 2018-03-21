//package underup.frame.industry;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.Map;
//import java.util.List;
//import java.util.ArrayList;
//import java.util.HashMap;
//
//import models.item.ItemCatPlay;
//import models.mysql.fengxiao.CatTopSaleItem;
//import models.mysql.fengxiao.CatTopSaleItem.YearMonthInfo;
//
//import play.jobs.Job;
//import play.jobs.OnApplicationStart;
//
////@OnApplicationStart
//public class ItemsCatJob extends Job {
//    private static final Logger log = LoggerFactory.getLogger(ItemsCatJob.class);
//
//    private Map<String, List<ItemCatPlay>> firstMap = new HashMap<String, List<ItemCatPlay>>();
//
//    private Map<String, List<ItemCatPlay>> secondMap = new HashMap<String, List<ItemCatPlay>>();
//
//    private List<ItemCatPlay> firstRemove = new ArrayList<ItemCatPlay>();
//
//    private List<ItemCatPlay> secondAdd = new ArrayList<ItemCatPlay>();
//
//    public void doJob() {
//        // List<YearAndMonth> yearAndMonthInfos = YearAndMonth.findAll();
//        List<YearMonthInfo> yearAndMonthInfos = CatTopSaleItem.getYearMonthInfos();
//        if (yearAndMonthInfos == null) {
//            log.error("get the year and month info from year_and_month database failed...... so your need to confirm that there are year and month information in the year_and_month");
//            return;
//        }
//        // 获取每一年中每一月的信息
//        for (YearMonthInfo yearAndMonthInfo : yearAndMonthInfos) {
//            long year = yearAndMonthInfo.getYear();
//            long month = yearAndMonthInfo.getMonth();
//            // 一级目录的信息key
//            String firstKey = "" + year + month;
//            firstRemove.clear();
//            // 获取一级类目信息
//            List<ItemCatPlay> itemCatsLevel1 = ItemCatPlay.findAllFirstLevelCats();
//            if (itemCatsLevel1 == null) {
//                log.error("there is an error when get the first level cid from database item_cat_play and confirm that there are some datas in the database item_cat_play");
//                return;
//            }
//            // 遍历一级类目
//            for (ItemCatPlay itemCatLevel1 : itemCatsLevel1) {
//                secondAdd.clear();
//                String secondKey = "" + year + month + itemCatLevel1.getCid();
//                if (itemCatLevel1.isParent()) {
//                    int len = getChildLevel(itemCatLevel1, year, month);
//                    if (len == 0) {
//                        firstRemove.add(itemCatLevel1);
//                    }
//                } else {
//                    if (CatTopSaleItem.findIfExisted(itemCatLevel1.getCid(), year, month)) {
//                        secondAdd.add(itemCatLevel1);
//                    } else {
//                        firstRemove.add(itemCatLevel1);
//                    }
//                }
//                if (secondAdd.size() != 0) {
//                    List<ItemCatPlay> itemCatTemps = new ArrayList<ItemCatPlay>();
//                    for (ItemCatPlay itemCatTemp : secondAdd) {
//                        itemCatTemps.add(itemCatTemp);
//                    }
//                    secondMap.put(secondKey, itemCatTemps);
//                }
//
//            }
//            for (ItemCatPlay itemCatLevel1 : firstRemove) {
//                itemCatsLevel1.remove(itemCatLevel1);
//            }
//            firstMap.put(firstKey, itemCatsLevel1);
//        }
//        save();
//
//    }
//
//    // 为父目录时
//    int getChildLevel(ItemCatPlay itemCat, long year, long month) {
//        int itemLevelCount = 0;
//        List<ItemCatPlay> itemCatChildren = ItemCatPlay.findByParentCid(itemCat.getCid());
//        if (itemCatChildren == null) {
//            log.info("can't get info from item_cat_play by parentcid");
//            return itemLevelCount;
//        }
//        for (ItemCatPlay itemCatChild : itemCatChildren) {
//            // 如果是还是父类，则继续遍历,如果是叶子则看是否存在此叶子节点信息
//            if (itemCatChild.isParent()) {
//                int l = getChildLevel(itemCatChild, year, month);
//                itemLevelCount += l;
//                if (l > 0)
//                    secondAdd.add(itemCatChild);
//            } else {
//                if (CatTopSaleItem.findIfExisted(itemCatChild.getCid(), year, month)) {
//                    itemLevelCount++;
//                    secondAdd.add(itemCatChild);
//                }
//            }
//        }
//        return itemLevelCount;
//    }
//
//    // 把信息存入数据库
//    void save() {
//        List<String> level1String = new ArrayList<String>(firstMap.keySet());
//        if (level1String != null) {
//            for (String s : level1String) {
//                List<ItemCatPlay> itemCatPlays = firstMap.get(s);
//                if (itemCatPlays != null) {
//                    for (ItemCatPlay itemCatPlay : itemCatPlays) {
//                        String sign = s;
//                        long cid = itemCatPlay.getCid();
//                        String name = itemCatPlay.getName();
//                        new ItemCatLevel1(sign, cid, name).jdbcSave();
//                    }
//                }
//            }
//        }
//
//        List<String> level2String = new ArrayList<String>(secondMap.keySet());
//        if (level2String == null) {
//            log.error("secondMap failed......");
//            return;
//        }
//        log.info("secondMap are " + level2String);
//
//        for (String s1 : level2String) {
//            List<ItemCatPlay> itemCatPlays = secondMap.get(s1);
//            if (itemCatPlays != null) {
//                for (ItemCatPlay itemCatPlay : itemCatPlays) {
//                    log.info("insert into second cid = " + itemCatPlay.getCid());
//                    String sign = s1;
//                    long cid = itemCatPlay.getCid();
//                    String name = itemCatPlay.getName();
//                    boolean isParent = itemCatPlay.isParent();
//                    long parentCid = itemCatPlay.getParentCid();
//                    int level = itemCatPlay.getLevel();
//                    new ItemCatLevel2(sign, cid, name, isParent, parentCid, level).jdbcSave();
//                }
//            } else
//                log.info("error occur with secondMap with s1 " + s1);
//        }
//
//    }
//
//}
