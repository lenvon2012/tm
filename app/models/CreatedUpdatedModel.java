
package models;

import javax.persistence.MappedSuperclass;

import play.db.jpa.Model;

@MappedSuperclass
public class CreatedUpdatedModel extends Model {

//    @Exclude
    public Long created;

//    @Exclude
    public Long updated;

    public CreatedUpdatedModel() {
        super();
        created = System.currentTimeMillis();
    }

    @Override
    public void _save() {
        updated = System.currentTimeMillis();
        super._save();
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public Long getUpdated() {
        return updated;
    }

    public void setUpdated(Long updated) {
        this.updated = updated;
    }
}
