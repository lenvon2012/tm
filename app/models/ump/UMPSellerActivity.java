
package models.ump;

import javax.persistence.Entity;

import org.hibernate.annotations.Index;

import play.db.jpa.Model;

@Entity(name = UMPSellerActivity.TABLE_NAME)
public class UMPSellerActivity extends Model {
    public static final String TABLE_NAME = "ump_seller_activity";

    @Index(name = "activityId")
    Long currActivityId;

    String name;

    @Index(name = "userId")
    Long userId;

    int type;

    long start;

    long end;

    long toolId;

    public String tag;

    public static class Type {

    }
}
