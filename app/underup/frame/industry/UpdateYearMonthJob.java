
package underup.frame.industry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;

//@OnApplicationStart
public class UpdateYearMonthJob extends Job {
    private static final Logger log = LoggerFactory.getLogger(UpdateYearMonthJob.class);

    public static class YearAndMonthPojo {
        long year;

        long month;
    }

    @Override
    public void doJob() {
        long[] ym = CatTopSaleItemSQL.getTime();
        // log.info("---------------------------------------------------------------------------yearmonths:");
        if (ym == null) {
            log.error("no year and month");
            return;
        }
        long year = ym[0];
        long month = ym[1];
        log.info("------------------------------------------------------------------------------------year and month :"
                + year + " " + month);
//        new YearAndMonth(year, month).jdbcSave();
    }
}
