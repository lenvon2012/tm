package job.topshop;

import models.industry.TopShop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;

public class TopSopJob extends Job {
	public final static Logger log = LoggerFactory.getLogger(TopSopJob.class);
	TopShop topShop;
	String picPath;
	String title;
	@Override
    public void doJob() throws Exception {
		//Shop shop = new ShopGet(wangwang).call(); 
	}
}
