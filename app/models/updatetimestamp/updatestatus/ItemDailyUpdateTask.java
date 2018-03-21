
package models.updatetimestamp.updatestatus;

import javax.persistence.Entity;

import models.updatetimestamp.UserDailyUpdateStatus;

@Entity(name = ItemDailyUpdateTask.TABLE_NAME)
public class ItemDailyUpdateTask extends UserDailyUpdateStatus {

    public final static String TABLE_NAME = "item_daily_update_task";

    public ItemDailyUpdateTask(Long userId, Long ts) {
        super(userId, ts);
    }

    public static ItemDailyUpdateTask findByUserIdAndTs(Long userId, Long ts) {
        return ItemDailyUpdateTask.find("userId= ?  and ts = ? ", userId, ts).first();
    }

    public static ItemDailyUpdateTask findOrCreate(Long userId, Long ts) {
        ItemDailyUpdateTask task = findByUserIdAndTs(userId, ts);
        if (task == null) {
            task = new ItemDailyUpdateTask(userId, ts).save();
        }
        return task;
    }

    public static void deleteOne(ItemDailyUpdateTask task) {
        //JDBCBuilder.update(false, "delete from " + TABLE_NAME + " where userId= ?  and ts = ? ", task.getUserId(),
        //        task.getTs());
    	task.delete();
    }
}
