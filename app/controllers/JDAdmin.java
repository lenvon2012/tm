package controllers;

import models.jd.JDUser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.mvc.Controller;
import dao.JDUserDao;

public class JDAdmin extends Controller {

    private static final Logger log = LoggerFactory.getLogger(JDAdmin.class);

    public static final String TAG = "Admin";

    public static void index() {
        render("Application/crud.html");
    }

    public static void makeDev(Long id) {
        JDUser user = JDUserDao.findById(id);

        if (user == null) {
            notFound();
        }

        JDController.clearUser();
        setDevUser(user);
    }
    
    public static void makeDevName(String nick) {
        JDUser user = JDUserDao.findByUserNick(nick.trim());
        if (user == null) {
            notFound();
        }

        JDController.clearUser();
        setDevUser(user);
    }

    protected static void setDevUser(JDUser user) {
        JDController.putUser(user);
        JDTuiguang.index();
    }

}
