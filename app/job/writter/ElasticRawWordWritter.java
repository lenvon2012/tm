
package job.writter;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import models.word.ElasticRawWord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import controllers.APIConfig;


//@Every("15s")
public class ElasticRawWordWritter extends Job {

    private static final Logger log = LoggerFactory.getLogger(CommentsWritter.class);

    public static Queue<ElasticRawWord> queue = new ConcurrentLinkedQueue<ElasticRawWord>();

    @Override
    public void doJob() {
    	if (APIConfig.get().getApp() != APIConfig.taobiaoti.getApp()) {
    		queue.clear();
    		return; 
        }
    	ElasticRawWord word = null;
        log.info("Current ElasticRawWord writter queue size is : " + queue.size());
        while ((word = queue.poll()) != null) {
        	word.save();
        }
    }

    public static void addMsg(ElasticRawWord elasticRawWord) {
        queue.add(elasticRawWord);
        //if(queue.size() >= 512) {
        //	CommonUtils.sleepQuietly(10000);
        //}
    }

}
