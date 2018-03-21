
package models.relation;

import java.util.List;

import javax.persistence.Column;
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

//@Entity(name = ItemDetailTemplate.TABLE_NAME)
@JsonAutoDetect
@JsonIgnoreProperties(value = {
        "hibernateLazyInitializer", "entityId"
})
public class ItemDetailTemplate extends CreatedUpdatedModel {

    @Transient
    private static final Logger log = LoggerFactory.getLogger(ItemDetailTemplate.class);

    @Transient
    public static final String TAG = "ItemDetailTemplate";

    @Transient
    public static final String TABLE_NAME = "item_detail_template";

    @Index(name = "name")
    @JsonProperty
    String name;

    @Column(columnDefinition = "varchar(16380) default '' ")
    @MaxSize(value = 16382)
    String frameworkTmpl;

    @Column(columnDefinition = "varchar(16380) default '' ")
    @MaxSize(value = 16382)
    String itemDescTmpl;

    /**
     * 自定义参数,直接写成json串放到数据库里面
     * [
     *      {'name':'startTime', 'comment':'启动时间','value':'2014.02.02'},
     *      {'name':'endTime', 'comment':'结束时间','value':'2014.02.03'}
     * ]
     */
    @Column(columnDefinition = "varchar(16380) default '' ")
    @MaxSize(value = 16382)
    String customParams;

    String comment;

    boolean isDelete = true;

    Long parentCid;

    @Column(columnDefinition = "varchar(16380) default '' ")
    String tags;

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
                sb.append("  comment like '%" + StringEscapeUtils.escapeSql(piece) + "%' ");
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
        List<ItemDetailTemplate> resultList = JPA.em().createNativeQuery(selectSQL, ItemDetailTemplate.class)
                .getResultList();
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
        return frameworkTmpl;
    }

    public void setBody(String body) {
        this.frameworkTmpl = body;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}
