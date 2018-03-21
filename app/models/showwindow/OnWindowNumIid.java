
package models.showwindow;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;

import models.user.User;
import play.db.jpa.GenericModel;

@Entity(name = OnWindowNumIid.TABLE_NAME)
public class OnWindowNumIid extends GenericModel {
    public static final String TABLE_NAME = "on_window_numiid_";

    @Id
    Long numIid;

    Long userId;

    public static void init(User user, Set<Long> onWindowIds) {

    }

}
