
package onlinefix;

import job.topshop.UpdateAllTopTaobao;
import models.word.top.TopKey;
import models.word.top.TopKey.TopKeyWordBaseUpdateJob;
import play.jobs.Job;

public class RebuildTopKeyJob extends Job {

    public void doJob() {
        TopKey.deleteAll();
        new UpdateAllTopTaobao().doJob();
        new TopKeyWordBaseUpdateJob().doJob();
    }
}
