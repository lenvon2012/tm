
package onlinefix;

import java.util.HashSet;
import java.util.Set;

import models.op.RawId;
import models.showwindow.ShowwindowExcludeItem;
import models.user.User;
import play.jobs.Job;
import dao.UserDao;
import dao.item.ItemDao;

public class ClearAllDeletedExcludeItems extends Job {

    public void doJob() {
        Set<Long> ids = RawId.toIds();
        for (Long id : ids) {
            User user = UserDao.findById(id);
            if (user == null) {
                continue;
            }
            Set<Long> numIids = ItemDao.findNumIidWithUser(id);
            Set<Long> existIds = ShowwindowExcludeItem.findIdsByUser(id);
            Set<Long> toDeleteIds = new HashSet<Long>();
            for (Long exist : existIds) {
                if (numIids.contains(exist)) {
                    continue;
                }
                toDeleteIds.add(exist);
            }
            for (Long toDelete : toDeleteIds) {
                ShowwindowExcludeItem.remove(user, toDelete);
            }

        }
    }
}
