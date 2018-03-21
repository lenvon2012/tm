
package models.helpcenter;

import static java.lang.String.format;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import models.CreatedUpdatedModel;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.data.validation.MaxSize;
import play.db.jpa.JPA;
import result.TMResult;

import com.ciaosir.client.pojo.PageOffset;

import controllers.CRUD.Exclude;

@Entity(name = TMHelpArticle.TABLE_NAME)
@JsonAutoDetect
@JsonIgnoreProperties(value = {
        "hibernateLazyInitializer", "entityId"
})
public class TMHelpArticle extends CreatedUpdatedModel {

    @Exclude
    @Transient
    private static final Logger log = LoggerFactory.getLogger(TMHelpArticle.class);

    @Exclude
    @Transient
    public static final String TAG = "TMHelpArticle";

    @Exclude
    @Transient
    public static final String TABLE_NAME = "help_article";

    @Index(name = "name")
    @JsonProperty
    String name;

    @Column(columnDefinition = "varchar(65535) default '' ")
    @MaxSize(value = 65535)
    String body;

    String comment;

    boolean isDelete = true;

//    @Exclude
    @ManyToOne(fetch = FetchType.EAGER)
    HelpNavLevel2 parent;

    public TMHelpArticle() {
        super();
    }

    public TMHelpArticle(String name, String body, String comment, HelpNavLevel2 parent) {
        this(name, body, comment, parent, true);
    }

    public TMHelpArticle(String name, String body, String comment, HelpNavLevel2 parent, boolean isDelete) {
        super();
        this.name = name;
        this.body = body;
        this.comment = comment;
        this.parent = parent;
        HelpNavLevel1 level1 = parent.getParent();
        if (level1 != null) {
            this.level1Id = level1.getId();
        }
        this.isDelete = isDelete;
    }

    Long level1Id;

    public static TMResult findByLevel2(Long level2Id, PageOffset po) {

        log.info(format("findByLevel2:level2Id, po".replaceAll(", ", "=%s, ") + "=%s", level2Id, po));
        HelpNavLevel2 nav2 = HelpNavLevel2.findById(level2Id);

//        List<TMHelpArticle> articles = TMHelpArticle.find(" parent = ? and isDelete = false", nav2)
        List<TMHelpArticle> articles = TMHelpArticle.find(" parent = ? ", nav2)
                .from(po.getOffset()).fetch(po.getPs());
        for (TMHelpArticle tmHelpArticle : articles) {
            tmHelpArticle.body = StringUtils.EMPTY;
        }
//        int count = (int) TMHelpArticle.count(" parent = ? and isDelete = false", nav2);
        int count = (int) TMHelpArticle.count(" parent = ? ", nav2);

        log.info("[count : ]" + count);
        return new TMResult(articles, count, po);
    }

    public static TMResult search(String word, PageOffset po) {
        StringBuilder sb = new StringBuilder("  from " + TABLE_NAME + " where  ");
        if (StringUtils.isBlank(word)) {

        } else {
            word = word.replaceAll(",", " ");
            word = word.replaceAll("  ", " ");
            word = word.replaceAll("  ", " ");

            String[] wordArr = word.split(" ");
//            sb.append(" body like '%" + StringEscapeUtils.escapeSql(wordArr[0]) + "%' ");
            for (int i = 0; i < wordArr.length; i++) {
                String piece = wordArr[i];
                log.info("[piece]:" + piece);
                sb.append(" (");
                sb.append("  body like '%" + StringEscapeUtils.escapeSql(piece) + "%' ");
                sb.append(" or  ");
                sb.append("  name like '%" + StringEscapeUtils.escapeSql(piece) + "%' ");
                sb.append(" )");
                sb.append(" and  ");
            }
        }

        sb.append(" isDelete is false ");
        String fromWhere = sb.toString();
        String selectSQL = " select * " + fromWhere + " limit " + po.getPs() + " offset " + po.getOffset();
        log.info("[sqlSQL]" + selectSQL);
        List<TMHelpArticle> resultList = JPA.em().createNativeQuery(selectSQL, TMHelpArticle.class).getResultList();
        int count = JPA.em().createNativeQuery(" select count(*) " + fromWhere).getFirstResult();

        return new TMResult(resultList, count, po);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public HelpNavLevel2 getParent() {
        return parent;
    }

    public void setParent(HelpNavLevel2 parent) {
        this.parent = parent;
    }

    public Long getLevel1Id() {
        return level1Id;
    }

    public void setLevel1Id(Long level1Id) {
        this.level1Id = level1Id;
    }

    @Override
    public String toString() {
        return "TMHelpArticle [name=" + name + ", comment=" + comment + ", isDelete=" + isDelete + ", parent="
                + (parent == null ? null : parent) + ", level1Id=" + level1Id + "]";
    }
}
