package underup.frame.industry;

import java.io.IOException;

import play.jobs.Job;
import play.jobs.OnApplicationStart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

//@OnApplicationStart
public class UpdateFrontCatsJob extends Job {
    private static final Logger log = LoggerFactory.getLogger(UpdateFrontCatsJob.class);

    @Override
    public void doJob() {
        Document document = null;
        document = null;
        for (int i = 1; i <= 16; ++i) {
            try {
                document= Jsoup.connect("http://www.taobao.com/2013/ajax/tms.php?ids=cat_" + i).get();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
            if (document == null) {
                log.error("--------------------------------------------------------------------------------get the cids failes by i="
                        + i);
                return;
            }
            String context = document.toString();
            getCids(context);
        }
    }

    public void getCids(String context) {
        while (context != null && context.indexOf("cat=") > 0) {
            int sIndex = context.indexOf("cat=") + 4;
            int eIndex = sIndex;
            for (; context.charAt(eIndex) >= '0' && context.charAt(eIndex) <= '9'; eIndex++) {

            }
            if (sIndex == eIndex) {
                log.error("the \"cat=\" dosen't hava anything!");
                context = context.substring(sIndex);
                continue;
            }
            long frontCid = Long.parseLong(context.substring(sIndex, eIndex));
            new FrontCats(frontCid).jdbcSave();
            context = context.substring(eIndex);

        }
    }
}
