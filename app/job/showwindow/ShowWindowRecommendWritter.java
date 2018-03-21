
package job.showwindow;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import models.showwindow.ShowDelistItem;
import play.jobs.Every;
import play.jobs.Job;

@Every("20s")
public class ShowWindowRecommendWritter extends Job {

    static Queue<ShowDelistItem> queue = new ConcurrentLinkedQueue<ShowDelistItem>();

    @Override
    public void doJob() {
        ShowDelistItem item = null;
        while ((item = queue.poll()) != null) {
            item.save();
        }
    }

    public static void addRecommend(Long userId, Long numIid) {
        queue.add(new ShowDelistItem(userId, numIid));
    }
}
