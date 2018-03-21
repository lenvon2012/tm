
package models.industry;

import javax.persistence.Entity;

import models.word.top.TopURLBase;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Index;

import play.db.jpa.Model;

import com.ciaosir.client.item.ShopInfo;

@Entity(name = TopUrlToShop.TABLE_NAME)
@JsonIgnoreProperties(value = {
        "parentBaseId", "cat", "cid", "cidInt", "level", "url", "catText", "type", "tableHashKey", "persistent",
        "entityId", "idName", "totalNum"
})
public class TopUrlToShop extends Model {
    public static final String TABLE_NAME = "topurltoshop";

    @Index(name = "topUrlId")
    Long topUrlId;

    @Index(name = "userId")
    public Long userId;

    public static void ensureRelation(TopURLBase base, ShopInfo info) {
        if (TopUrlToShop.find("topUrlId = ? and userId = ? ", base.getId(), info.getUserId()).first() != null) {
            return;
        }

        TopUrlToShop model = new TopUrlToShop(base.getId(), info.getUserId());
        model.save();
    }

    public TopUrlToShop(Long topUrlId, Long userId) {
        super();
        this.topUrlId = topUrlId;
        this.userId = userId;
    }

}
