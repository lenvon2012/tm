
package models.op;

import javax.persistence.Column;
import javax.persistence.Entity;

import models.CreatedUpdatedModel;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity(name = RecommendFeedBack.TABLE_NAME)
public class RecommendFeedBack extends CreatedUpdatedModel {

    private static final Logger log = LoggerFactory.getLogger(RecommendFeedBack.class);

    public static final String TAG = "RecommendFeedBack";

    public static final String TABLE_NAME = "recommend_feedback";

    @Index(name = "userId")
    Long userId;

    Long numIid = 0L;

    String origin = StringUtils.EMPTY;

    @Column(columnDefinition = "varchar(1022) default ''")
    String recommendTitle = StringUtils.EMPTY;

    @Column(columnDefinition = "varchar(4094) default ''")
    String content = StringUtils.EMPTY;

    boolean toVisit = false;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getNumIid() {
        return numIid;
    }

    public void setNumIid(Long numIid) {
        this.numIid = numIid;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getRecommendTitle() {
        return recommendTitle;
    }

    public void setRecommendTitle(String recommendTitle) {
        this.recommendTitle = recommendTitle;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isToVisit() {
        return toVisit;
    }

    public void setToVisit(boolean toVisit) {
        this.toVisit = toVisit;
    }

    @Override
    public String toString() {
        return "RecommendFeedBack [userId=" + userId + ", numIid=" + numIid + ", origin=" + origin
                + ", recommendTitle=" + recommendTitle + ", content=" + content + ", toVisit=" + toVisit + "]";
    }

}
