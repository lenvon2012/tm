package controllers;

import models.user.User;

public class Taotuiguang extends TMController {
    
    public static void index() {
        render("taotuiguang/index.html");
    }
    
    
    public static void queryUserNick() {
        User user = getUser();
        if (user == null) {
            renderText("");
        }
        renderText(user.getUserNick());
    }
}
