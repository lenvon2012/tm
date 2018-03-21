package models;

import javax.persistence.Entity;

import models.user.User;

import org.hibernate.annotations.Index;

import play.db.jpa.Model;

@Entity(name = CRUDUser.TABLE_NAME)
public class CRUDUser extends Model {

    public static final String TABLE_NAME = "crud_user";
    
    @Index(name = "userName")
    private String userName;
    
    private String password;

    public CRUDUser() {
        super();
    }

    public CRUDUser(String userName, String password) {
        super();
        this.userName = userName;
        this.password = password;
    }
    
    public static boolean isValidUser(String userName, String password) {
        
        if (CRUDUser.count() <= 0) {
            return ("!@#bigesandai".equals(userName)) && ("bigesandai!@#".equals(password));
        } else {
            
            CRUDUser user = CRUDUser.find("userName = ? and password = ? ", userName, password).first();
            
            if (user == null) {
                return false;
            } else {
                return true;
            }
            
        }
        
    }
    
    
}
