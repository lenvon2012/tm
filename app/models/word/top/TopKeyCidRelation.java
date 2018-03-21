
package models.word.top;

import javax.persistence.Entity;

import org.hibernate.annotations.Index;

import play.db.jpa.Model;

@Entity
public class TopKeyCidRelation extends Model {

    public static final String TABLE_NAME = "topkeycidrelation_";

    @Index(name = "topKeyId")
    public Long topKeyId;

    @Index(name = "cid")
    public Integer cid;

    public Integer score = 0;

    public static TopKeyCidRelation findOrCreate(Long id, Integer cid) {

        TopKeyCidRelation first = TopKeyCidRelation.find("topKeyId = ? and cid = ?", id, cid)
                .first();

        if (first != null) {
            return first;
        }

        return new TopKeyCidRelation(id, cid).save();
    }

    public TopKeyCidRelation(Long topKeyId, Integer cid) {
        super();
        this.topKeyId = topKeyId;
        this.cid = cid;
    }

    public Long getTopKeyId() {
        return topKeyId;
    }

    public void setTopKeyId(Long topKeyId) {
        this.topKeyId = topKeyId;
    }

    public Integer getCid() {
        return cid;
    }

    public void setCid(Integer cid) {
        this.cid = cid;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer valueOf) {
        this.score = valueOf;
    }

}
