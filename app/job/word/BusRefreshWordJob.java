/**
 * 
 */

package job.word;

import java.io.File;
import java.io.IOException;
import java.util.List;

import models.word.top.BusCatPlay;
import models.word.top.BusTopKey;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.jobs.Job;

/**
 * @author navins
 * @date 2013-7-1 下午3:45:59
 */
public class BusRefreshWordJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(BusRefreshWordJob.class);

    File[] files = new File[] {
            new File(Play.applicationPath + "/conf/catwords/top20W0725.csv"),
            new File(Play.applicationPath + "/conf/catwords/top20W0815.csv"),
            new File(Play.applicationPath + "/conf/catwords/top20W0823.csv"),
            new File(Play.applicationPath + "/conf/catwords/top20W0829.csv"),

    };

    public void doJob() {
        try {

            for (File file : files) {
                log.info("[do for file:]" + file);
                doForFile(file);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doForFile(File file) throws IOException {
        List<String> lines = FileUtils.readLines(file, "UTF-8");

        for (String line : lines) {
            if (StringUtils.isBlank(line)) {
                continue;
            }
            String[] arr = line.split(",");
            if (arr.length < 4 || StringUtils.isBlank(arr[3])) {
                continue;
            }
            long[] pids = new long[5];
            for (int i = 0; i < 3; i++) {
                String name = arr[i].trim();
                if (!StringUtils.isEmpty(name) && !"0".equals(name)) {
                    pids[i + 1] = BusCatPlay.findOrCreate(name, i, pids[i]);
                }
            }

            BusTopKey.saveKey(pids[1], pids[2], pids[3], arr[3].trim());
        }
    }

}
