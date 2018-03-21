package job.word;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import job.writter.ElasticRawWordWritter;
import models.word.ElasticRawWord;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;

import com.ciaosir.client.api.WidAPIs;
import com.ciaosir.client.pojo.IWordBase;
import com.ciaosir.commons.ClientException;

import controllers.APIConfig;
import controllers.APIConfig.Platform;

public class Update50WWordsJob extends Job {

	private static final Logger log = LoggerFactory
			.getLogger(Update50WWordsJob.class);

	public static final String TAG = "Update50WWordsJob";

	public static int limit = 128;
	
	@Override
	public void doJob() {
		if (APIConfig.get().getPlatform() != Platform.taobao) {
            return;
        }

        if (APIConfig.get().getApp() != APIConfig.taobiaoti.getApp()) {
            return;
        }
        
		try {
			List<String> readLines = org.apache.commons.io.FileUtils.readLines(new File("tb_hot.txt"));
			List<String> words = new ArrayList<String>();
			int count = 0, num = 0;
			for (String word : readLines) {
				count++;
				num++;
				if(StringUtils.isEmpty(word)) {
					continue;
				}
				words.add(word);
				if(num >= limit) {
					Map<String, IWordBase> execute = getIWordBases(words);
					if(execute == null){
						continue;
					}
					log.info("Update50WWordsJob word count : " + count);
					
					for (String s : words) {
		                IWordBase iWordBase = execute.get(s);
		                if (iWordBase != null) {
		                	ElasticRawWordWritter.addMsg(new ElasticRawWord(iWordBase));
		                }
		            }
					num = 0;
					words.clear();
					/*if(ElasticRawWordWritter.queue.size() >= 1024) {
						CommonUtils.sleepQuietly(5000);
					}*/
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public static Map<String, IWordBase> getIWordBases(List<String> words) {
		Map<String, IWordBase> execute = new HashMap<String, IWordBase>();
		int retry =0;
		while(retry++ < 3) {
			try {
				execute = new WidAPIs.WordBaseAPI(words).execute();
				if(execute != null) {
					return execute;
				}
			} catch (ClientException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}
}
