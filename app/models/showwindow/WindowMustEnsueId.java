
package models.showwindow;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.jpa.GenericModel;

import com.ciaosir.client.utils.NumberUtil;

@Entity(name = WindowMustEnsueId.TABLE_NAME)
public class WindowMustEnsueId extends GenericModel {

    public static final String TABLE_NAME = "window_must_do_id";

    @Id
    Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public static void ensure(Long id) {
        if (NumberUtil.isNullOrZero(id)) {
            return;
        }
        if (WindowMustEnsueId.findById(id) == null) {
            new WindowMustEnsueId(id).save();
        }
    }

    public WindowMustEnsueId(Long id) {
        super();
        this.id = id;
    }

}
