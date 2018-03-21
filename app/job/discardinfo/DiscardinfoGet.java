//package job.discardinfo;
//
//import java.util.Queue;
//import java.util.concurrent.ConcurrentLinkedQueue;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.taobao.api.DefaultTaobaoClient;
//import com.taobao.api.TaobaoClient;
//
//import configs.TMConfigs;
//import configs.TMConfigs.App;
//
//import play.jobs.Job;
//
//public class DiscardinfoGet extends Job {
//
//    private static final Logger log = LoggerFactory.getLogger(DiscardinfoGet.class);
//
//    public static final String TAG = "discardinfoGet";
//
//	@Override
//	public void doJob() {
//		TaobaoClient client=new DefaultTaobaoClient(App.API_TAOBAO_URL,
//				TMConfigs.App.APP_KEY, TMConfigs.App.APP_SECRET);
//		CometDiscardinfoGetRequest req=new CometDiscardinfoGetRequest();
//		req.setTypes("item,trade");
//		req.setUserId(79827L);
//		Date dateTime = SimpleDateFormat.getDateTimeInstance().parse("2000-01-01 00:00:00");
//		req.setStart(dateTime);
//		Date dateTime = SimpleDateFormat.getDateTimeInstance().parse("2000-01-01 00:00:00");
//		req.setEnd(dateTime);
//		req.setNick("我是一个nick");
//		CometDiscardinfoGetResponse response = client.execute(req);
//
//	}	
//	
//}