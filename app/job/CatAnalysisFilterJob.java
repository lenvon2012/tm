package job;

import java.util.List;

import job.writter.NoPropsItemCatWritter;
import models.item.ItemCatPlay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;

import com.ciaosir.client.CommonUtils;

import controllers.APIConfig;

public class CatAnalysisFilterJob extends Job {
	
	static final Logger log = LoggerFactory
			.getLogger(CatAnalysisFilterJob.class);

	public static String TAG = "CrontabJob";

	public void doJob() {
		Thread.currentThread().setName(TAG);

		// 自动标题才需要
		if (APIConfig.get().getApp() != 21348761) {
			return;
		}
		List<ItemCatPlay> level1 = ItemCatPlay.find("level = 1").fetch();
		if (CommonUtils.isEmpty(level1)) {
			return;
		}
		for (ItemCatPlay cat1 : level1) {
			if (cat1 == null) {
				continue;
			}
			if (cat1.getCid() == null) {
				continue;
			}
			List<ItemCatPlay> level2 = ItemCatPlay.find("parentCid = ?",
					cat1.getCid()).fetch();
			if (CommonUtils.isEmpty(level2)) {
				continue;
			}
			for (ItemCatPlay cat2 : level2) {
				filterCat2(cat2);
			}
		}
	}

	public static void filterCat2(ItemCatPlay cat2) {
		if (cat2 == null) {
			return;
		}
		if (cat2.getCid() == null) {
			return;
		}
		NoPropsItemCatWritter.addMsg(cat2);
	}
}
