
package controllers;

import static java.lang.String.format;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import models.item.ItemCatPlay;
import models.item.ItemPlay;
import models.user.User;
import models.word.top.BusCatPlay;
import play.cache.Cache;
import play.cache.CacheFor;
import result.TMResult;

import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.JsonUtil;

import dao.item.ItemDao;
import dao.topbuskey.TopBusKeyListDao;

public class Buskey extends TMController {

    @CacheFor("3h")
    public static void findLevel1() {
        List<BusCatPlay> list = TopBusKeyListDao.findBusCatPlayFirstLevelList();
        renderJSON(list);
    }

    @CacheFor("3h")
    public static void findLevel2or3(int catId) {
        if (catId == 0) {
            log.info("the select is not found");
            catId = -1;
        }
        List<BusCatPlay> list = TopBusKeyListDao.findBusCatPlayListBycatId(catId);
        renderJSON(list);
    }

//    @CacheFor("3h")
    public static void buslistlevel3(int catId, int Choselevel, String sortBy, int pn, int ps) {
        if (catId == 0) {
            log.info("the select is not found");
            catId = -1;
        }

        PageOffset po = new PageOffset(pn, ps, 15);
        TMResult tmResult = TopBusKeyListDao.finBusTopkeyListBylevel3(catId, Choselevel, sortBy, po);
        renderJSON(tmResult);
    }

    public static void searchWords(Long cat1Id, Long cat2Id, Long cat3Id, String sortBy, int pn, int ps) {

        log.info(format("searchWords:cat1Id, cat2Id, cat3Id, sortBy, pn, ps".replaceAll(", ", "=%s, ") + "=%s", cat1Id,
                cat2Id, cat3Id, sortBy, pn, ps));
        PageOffset po = new PageOffset(pn, ps, ps);
        TMResult tmResult = TopBusKeyListDao.searchWords(cat1Id, cat2Id, cat3Id, sortBy, po);
        renderJSON(tmResult);
    }
    
    public static void myMostCat() {
        User user = getUser();
        if(user == null) {
            renderFailedJson("用户不存在");
        }
        Long cid = (Long) Cache.get(Home.CidTopKeyPre + user.getId());
        if(cid == null) {
            cid = ItemDao.findMaxCid(user.getId());
            Cache.set(Home.CidTopKeyPre + user.getId(), cid, "3d");
        }
        if(cid <= 0L) {
            renderFailedJson("找不到类目");
        }
        ItemCatPlay itemCatPlay = ItemCatPlay.findByCid(cid);
        if(itemCatPlay == null) {
            renderFailedJson("找不到类目");
        }
        BusCatPlay busCatPlay = BusCatPlay.findByCatName(itemCatPlay.getName());
        if(busCatPlay == null) {
            renderFailedJson("找不到类目");
        }
        List<BusCatPlay> results = new ArrayList<BusCatPlay>();
        results.add(busCatPlay);
        BusCatPlay tmpBusCatPlay = busCatPlay;
        while (tmpBusCatPlay.getLevel() != 0) {
            BusCatPlay parent = BusCatPlay.findByCid(tmpBusCatPlay.getParentId());
            if(parent == null) {
                break;
            }
            results.add(parent);
            tmpBusCatPlay = parent;
        }
        renderJSON(JsonUtil.getJson(results));
    }
    
    public static void itemBusCatPlay(Long numIid) {
        if(numIid == null) {
            renderFailedJson("宝贝ID为空");
        }
        User user = getUser();
        if(user == null) {
            renderFailedJson("用户不存在");
        }
        ItemPlay itemPlay = ItemDao.findByNumIid(user.getId(), numIid);
        if(itemPlay == null) {
            renderFailedJson("宝贝不存在");
        }
        Long cid = itemPlay.getCid();
        if(cid <= 0L) {
            renderFailedJson("找不到类目");
        }
        ItemCatPlay itemCatPlay = ItemCatPlay.findByCid(cid);
        if(itemCatPlay == null) {
            renderFailedJson("找不到类目");
        }
        BusCatPlay busCatPlay = BusCatPlay.findByCatName(itemCatPlay.getName());
        if(busCatPlay == null) {
            renderFailedJson("找不到BusCatPlay类目");
        }
        List<BusCatPlay> results = new ArrayList<BusCatPlay>();
        results.add(busCatPlay);
        BusCatPlay tmpBusCatPlay = busCatPlay;
        while (tmpBusCatPlay.getLevel() != 0) {
            BusCatPlay parent = BusCatPlay.findByCid(tmpBusCatPlay.getParentId());
            if(parent == null) {
                break;
            }
            results.add(parent);
            tmpBusCatPlay = parent;
        }
        renderJSON(JsonUtil.getJson(results));
    }
    
    public static String UserCidsMapPreTemp = "UserCidsMapPreTemp";
    public static void myAllCats() {
        User user = getUser();
        if(user == null) {
            renderFailedJson("用户不存在");
        }
        List<CatCount> catCounts = (List<CatCount>) Cache.get(UserCidsMapPreTemp + user.getId());
        if(catCounts == null) {
            catCounts = ItemDao.findAllCids(user.getId());
            if(catCounts.size() <= 0) {
                renderFailedJson("找不到类目");
            }
            // 根据宝贝数目排序
            Collections.sort(catCounts, new Comparator<CatCount>(){
                public int compare(CatCount arg0, CatCount arg1) {
                    return arg1.getCount() - arg0.getCount();
                }
            });
            Cache.set(UserCidsMapPreTemp + user.getId(), catCounts, "3d");
        }
        List<ItemCatPlay> results = new ArrayList<ItemCatPlay>();
        for(CatCount catCount : catCounts) {
            ItemCatPlay itemCatPlay = ItemCatPlay.findByCid(catCount.getCid());
            if(itemCatPlay == null) {
                continue;
            }
            BusCatPlay busCatPlay = BusCatPlay.findByCatName(itemCatPlay.getName());
            if(busCatPlay == null) {
                continue;
            }
            results.add(itemCatPlay);
        }

        ItemCatPlay.ensureParentPath(results);

        renderJSON(JsonUtil.getJson(results));
    }
    
    public static class CatCount implements Serializable{
        Long cid;
        
        Integer count;

        String name;
        
        public Long getCid() {
            return cid;
        }

        public void setCid(Long cid) {
            this.cid = cid;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public CatCount(Long cid, Integer count) {
            super();
            this.cid = cid;
            this.count = count;
        }
    }
    
    public static void getCatLevel(Long cid) {
        User user = getUser();
        if(user == null) {
            renderFailedJson("用户不存在");
        }
        if(cid == null) {
            renderFailedJson("找不到类目");
        }
        ItemCatPlay itemCatPlay = ItemCatPlay.findByCid(cid);
        if(itemCatPlay == null) {
            renderFailedJson("找不到类目");
        }
        BusCatPlay busCatPlay = BusCatPlay.findByCatName(itemCatPlay.getName());
        if(busCatPlay == null) {
            renderFailedJson("找不到类目");
        }
        List<BusCatPlay> results = new ArrayList<BusCatPlay>();
        results.add(busCatPlay);
        BusCatPlay tmpBusCatPlay = busCatPlay;
        while (tmpBusCatPlay.getLevel() != 0) {
            BusCatPlay parent = BusCatPlay.findByCid(tmpBusCatPlay.getParentId());
            if(parent == null) {
                break;
            }
            results.add(parent);
            tmpBusCatPlay = parent;
        }
        renderJSON(JsonUtil.getJson(results));
    }

}
