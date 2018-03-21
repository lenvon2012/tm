package controllers;

import java.util.ArrayList;
import java.util.List;

import models.hotitem.CatTopWordPlay;
import models.item.ItemCatPlay;
import models.item.ItemPlay;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import play.cache.CacheFor;
import result.TMResult;
import bustbapi.ItemCatApi;
import bustbapi.TMApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.JsonUtil;
import com.taobao.api.domain.ItemCat;

import dao.UserDao;
import dao.item.ItemDao;

public class CatTopWord extends TMController {

    private static final Logger log = LoggerFactory.getLogger(CatTopWord.class);
    
    @CacheFor("3h")
    public static void findLevel1() {
        List<ItemCatPlay> catList = ItemCatPlay.findAllFirstLevelCats();
        renderBusJson(catList);
    }
    
    @CacheFor("3h")
    public static void findLevel2or3(Long parentCid) {
        if (parentCid == null || parentCid <= 0L) {
            parentCid = 0L;
            //renderFailedJson("请先选择一个类目！");
        }
        List<ItemCatPlay> catList = ItemCatPlay.findByParentCid(parentCid);
        renderBusJson(catList);
    }
    
    public static void updateCidTopKeyCache(Long cid, String userNick) {
    	if(cid == null || cid <= 0) {
    		renderText("请传入正确的cid");
    	}
    	if(StringUtils.isEmpty(userNick)) {
    		renderText("请传入正确的用户昵称");
    	}
    	User user = UserDao.findByUserNick(userNick);
    	if(user == null) {
    		renderText("用户不存在");
    	}
    	Cache.set(Home.CidTopKeyPre + user.getId(), cid, "3d");
    	renderText("设置成功");
    }
    
    public static void findUserMostCat(Long numIid) {
        User user = getUser();
        
        Long cid = 0L;
        
        if (numIid == null || numIid <= 0L) {
            cid = (Long) Cache.get(Home.CidTopKeyPre + user.getId());
            if (cid == null) {
                cid = ItemDao.findMaxCid(user.getId());
                Cache.set(Home.CidTopKeyPre + user.getId(), cid, "3d");
            }
        } else {
            ItemPlay itemPlay = ItemDao.findByNumIid(user.getId(), numIid);
            if (itemPlay == null) {
                renderFailedJson("找不到指定的宝贝！");
            }
            cid = itemPlay.getCid();
        }
        
        if (cid == null || cid <= 0L) {
            renderFailedJson("系统出现异常，找不到您的宝贝类目，请联系我们！");
        }
        ItemCatPlay itemCatPlay = ItemCatPlay.findByCid(cid);
        if (itemCatPlay == null) {
        	renderFailedJson("找不到相应的类目，请联系我们！");
        	List<ItemCat> cats = new ItemCatApi.ItemcatsGet(user, cid).call();
        	if(CommonUtils.isEmpty(cats)) {
        		renderFailedJson("找不到相应的类目，请联系我们！");
        	}
            ItemCat itemCat = cats.get(0);
            Long parentCid = itemCat.getParentCid();
            if(parentCid == null) {
            	itemCatPlay = new ItemCatPlay(itemCat);
            } else {
            	ItemCatPlay parentCatPlay = ItemCatPlay.findByCid(parentCid);
	            if(parentCatPlay == null) {
	            	itemCatPlay = new ItemCatPlay(itemCat);
	            } else {
	            	itemCatPlay = new ItemCatPlay(itemCat, parentCatPlay, parentCatPlay.getLevel() + 1);
	            }
	            
            }
            itemCatPlay.jdbcSave();
        }
        
        List<ItemCatPlay> itemCatList = new ArrayList<ItemCatPlay>();
        ItemCatPlay childCat = itemCatPlay;
        while (true) {
            itemCatList.add(childCat);
            ItemCatPlay parentCat = ItemCatPlay.findByCid(childCat.getParentCid());
            if (parentCat == null) {
                break;
            }
            
            childCat = parentCat;
        }
        
        renderBusJson(itemCatList);
        
    }
    
    
    public static void findCatTopWords(long firstCid, long secondCid, long thirdCid, int pn, int ps,
            String orderBy, boolean isDesc) {
        
        PageOffset po = new PageOffset(pn, ps);
        
        try {
            List<CatTopWordPlay> topWordList = new TMApi.TMCatTopWordGetApi(firstCid, secondCid, thirdCid, 
                    pn, ps, orderBy, isDesc).execute();
            
            long count = new TMApi.TMCatTopWordCountApi(firstCid, secondCid, thirdCid).execute();
            
            TMResult tmRes = new TMResult(topWordList, (int) count, po);
            
            renderJSON(JsonUtil.getJson(tmRes));
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            
            TMResult tmRes = new TMResult(new ArrayList<CatTopWordPlay>(), 0, po);
            
            renderJSON(JsonUtil.getJson(tmRes));
        }
        
        
    }
    
}
