
package controllers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import models.mysql.word.TMCWordBase.SearchConfigParms;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.jobs.Job;
import configs.TMConfigs;

//@On("0 0 1 * * ?")
public class WordCacheJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(WordCacheJob.class);

    public static final String TAG = "WordCacheJob";

//    boolean checkJobTimerEnable = false;

    public void doJob() {
        if (Play.mode.isProd()) {
            if (APIConfig.get().getApp() != APIConfig.taobiaoti.getApp()
//                    || !Server.jobTimerEnable
            ) {
                return;
            }
        }

        File srcFile = new File(TMConfigs.configDir, "queries.txt");
        int count = 0;
        try {
            List<String> lines = FileUtils.readLines(srcFile);
            for (String line : lines) {
                SearchConfigParms params = SearchConfigParms.fromLine(line);
                params.doMustRefresh();
                log.info("[do for [ " + (count++) + " ]line :]" + line);
            }
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }
    }
}
