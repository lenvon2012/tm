
package models.showwindow;

import javax.persistence.Entity;

import models.CreatedUpdatedModel;

@Entity(name = ShowDelistItem.TABLE_NAME)
public class ShowDelistItem extends CreatedUpdatedModel {

    public static final String TABLE_NAME = "show_delist_item";

    Long userId;

    Long numIid;

    public ShowDelistItem(Long userId, Long numIid) {
        super();
        this.userId = userId;
        this.numIid = numIid;
    }

}
