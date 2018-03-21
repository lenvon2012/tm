
package job.sync.item;

import job.apiget.ItemUpdateJob;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;

public class SyncAllItemsJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(SyncAllItemsJob.class);

    private User user;

    private String userNick;

    private boolean isSyncAdgroup = false;

    public SyncAllItemsJob(User user, String userNick) {
        super();
        this.user = user;
        this.userNick = userNick == null ? user.getUserNick() : userNick;
    }

    public SyncAllItemsJob(User user, String userNick, boolean isSyncAdgroup) {
        super();
        this.user = user;
        this.userNick = userNick == null ? user.getUserNick() : userNick;
        this.isSyncAdgroup = isSyncAdgroup;
    }

    @Override
    public void doJob() {
        try {
            syncAllItems();
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    void syncAllItems() throws Exception {

        log.error("Sync all items!!!");
        /**
         * sync item
         */

        new ItemUpdateJob(user.getId()).doJob();

    }

}
