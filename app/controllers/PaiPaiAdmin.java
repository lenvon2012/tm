package controllers;

import models.paipai.PaiPaiUser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.mvc.Controller;
import dao.paipai.PaiPaiUserDao;


/**
 * @author haoyongzh
 *
 */
public class PaiPaiAdmin extends Controller{
	
	private static final Logger log = LoggerFactory.getLogger(PaiPaiAdmin.class);
	
    public static final String TAG = "Admin";
    
    public static void index() {
        render("Application/crud.html");
    }
    
    public static void PPmakeDev(Long id) {
    	
        PaiPaiUser user = PaiPaiUserDao.findById(id);
    	
        if (user == null) {
            notFound();
        }

        PaiPaiController.clearUser();
        setDevUser(user);
    }
    
    protected static void setDevUser(PaiPaiUser user){
    	PaiPaiController.putUser(user);
    	
//    	System.out.println(user);
//    	System.out.println(PaiPaiAPIConfig.get());
    	PaiPaiAPIConfig.get().afterLogin(null, null, false, false);
    }

}
