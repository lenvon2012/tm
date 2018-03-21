
package models.helpcenter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import models.CreatedUpdatedModel;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity(name = HelpNavLevel2.TABLE_NAME)
@JsonAutoDetect
@JsonIgnoreProperties(value = {
        "hibernateLazyInitializer", "entityId"
})
public class HelpNavLevel2 extends CreatedUpdatedModel {

    @Transient
    private static final Logger log = LoggerFactory.getLogger(HelpNavLevel2.class);

    @Transient
    public static final String TAG = "HelpNavLevel2";

    @Transient
    public static final String TABLE_NAME = "help_nav_level_2";

    @Index(name = "name")
    @JsonProperty
    String name;

    @JsonProperty
    String comment;

    boolean isDelete;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonProperty
    HelpNavLevel1 parent;

//    Long parentId;

//    String parentName;

    public HelpNavLevel2(String name, String comment, HelpNavLevel1 parent) {
        super();
        this.name = name;
        this.comment = comment;
        this.parent = parent;
    }

    public HelpNavLevel2() {
        super();
    }

    @Override
    public String toString() {
        return "HelpNavLevel2 [name=" + name + ", comment=" + comment + ", isDelete=" + isDelete + ", parent=" + (parent == null ? null
                : parent.getName()) + "]";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isDelete() {
        return isDelete;
    }

    public void setDelete(boolean isDelete) {
        this.isDelete = isDelete;
    }

    public HelpNavLevel1 getParent() {
        return parent;
    }

    public void setParent(HelpNavLevel1 parent) {
        this.parent = parent;
    }

}
