package controllers;

import java.util.List;

import models.item.ItemPlay;
import models.user.User;

import com.ciaosir.client.utils.JsonUtil;

import dao.item.ItemDao;

/**
 * 宝贝的列表，并且所有操作都在这个页面
 * User: Administrator
 * Date: 12-11-17
 * Time: 下午2:35
 * To change this template use File | Settings | File Templates.
 */
public class ItemSet extends TMController {
    public static void itemSet() {
        render();
    }

    public static void queryItemCount(String s) {
    	User user = getUser();
    	long count = ItemDao.countOnlineByUser(user.getId(), s);
    	renderText(count);
    }
    
    public static void queryItems(String s, int pn, int ps) {
    	User user = getUser();
        List<ItemPlay> list = ItemDao.findOnlineByUser(user.getId(), (pn - 1) * ps, ps, s, 0);
        String json = JsonUtil.getJson(list);
        renderJSON(json);
    }
    
}
