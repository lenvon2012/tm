
package models.op;

import javax.persistence.Column;
import javax.persistence.Entity;

import models.CreatedUpdatedModel;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity(name = TaozhangguiFeedBack.TABLE_NAME)
public class TaozhangguiFeedBack extends CreatedUpdatedModel {

    private static final Logger log = LoggerFactory.getLogger(TaozhangguiFeedBack.class);

    public static final String TAG = "TaozhangguiFeedBack";

    public static final String TABLE_NAME = "taozhanggui_feedback";

    @Index(name = "userId")
    Long userId;

    @Column(columnDefinition = "varchar(4094) default ''")
    String content = StringUtils.EMPTY;

    public TaozhangguiFeedBack(Long userId, String content) {
        this.userId = userId;
        this.content = content;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "RecommendFeedBack [userId=" + userId + ", content=" + content + "]";
    }

}
