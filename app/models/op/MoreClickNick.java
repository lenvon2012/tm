
package models.op;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import play.db.jpa.GenericModel;

@Entity(name = MoreClickNick.TABLE_NAME)
public class MoreClickNick extends GenericModel {

    @Transient
    public static final String TABLE_NAME = "more_click_nick";

    @Id
    String nick;

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

}
