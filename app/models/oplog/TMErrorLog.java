
package models.oplog;

import javax.persistence.Entity;

import models.CreatedUpdatedModel;
import controllers.TMController;

@Entity(name = TMErrorLog.TABLE_NAME)
public class TMErrorLog extends CreatedUpdatedModel {

    public static final String TABLE_NAME = "tm_error_log";

    public String content;

    public TMErrorLog(String content) {
        super();
        this.content = content;
        TMController.log.error(" save error content :" + content);
    }

}
