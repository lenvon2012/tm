package controllers;

import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.api.internal.util.StringUtils;

import dao.UserDao;

public class BKUserAdmin extends AdminController {

    private static final Logger log = LoggerFactory.getLogger(BKUserAdmin.class);
    
    public static void index() {
        render("tmadmin/useradmin.html");
    }
    
    
    
    public static void searchUser(String userNick) {
        if (StringUtils.isEmpty(userNick)) {
            renderFailedJson("请先输入卖家旺旺！");
        }
        userNick = userNick.trim();
        if (StringUtils.isEmpty(userNick)) {
            renderFailedJson("请先输入卖家旺旺！");
        }
        
        User user = UserDao.findByUserNick(userNick);
        
        if (user == null) {
            renderFailedJson("找不到该卖家！");
        }
        
        renderBusJson(user);
    }
    
    
    public static void updateUser(Long userId) {
        
        if (userId == null || userId <= 0) {
            renderFailedJson("系统出现异常，卖家ID为空");
        }
        
        
        
        User user = UserDao.findById(userId);
        if (user == null) {
            renderFailedJson("找不到该卖家！");
        }
        
        
    }
    
    
}
