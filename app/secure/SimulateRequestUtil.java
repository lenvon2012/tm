package secure;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import models.order.OrderDisplay;
import models.trade.TradeDisplay;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import secure.SimulateRrequest.LogType;
import secure.SimulateRrequest.Param;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.PYFutureTaskPool;

import controllers.APIConfig;

/**
 * 御城河日志接入
 * 服务器上非用户请求触发的订单读取操作
 */
public class SimulateRequestUtil {
	
	protected static final Logger log = Logger.getLogger(SimulateRequestUtil.class);
	
	private static final int THREAD_SIZE = 32;
    
    private static PYFutureTaskPool<Boolean> threadPool = new PYFutureTaskPool<Boolean>(THREAD_SIZE);
    
    public static final String TRADE_FULLINFO_GET = "taobao.trade.fullinfo.get";
    
    public static final String TRADES_SOLD_GET = "taobao.trades.sold.get";
    
    public static final String TRADES_SOLD_INCREMENT_GET = "taobao.trades.sold.increment.get";
    
	/**
     * top调用日志
     */
    public static void sendTopLog(String operation){
    	if (!APIConfig.get().isRisk()) {
			return;
		}
    	String topAppKey = APIConfig.get().getApiKey();
    	String appName = APIConfig.get().getAppName();
    	Param param = new SimulateRrequest.Param(StringUtils.EMPTY, 0L, getHostIp(), topAppKey, appName, operation);
    	threadPool.submit(new SimulateRrequest(param, LogType.TOP));
    }
    
    /**
     * 数据库访问日志
     */
    public static void sendSqlLog(String operation, String db, String sql){
    	if (!APIConfig.get().isRisk()) {
			return;
		}
    	String topAppKey = APIConfig.get().getApiKey();
    	String appName = APIConfig.get().getAppName();
    	Param param = new SimulateRrequest.Param(StringUtils.EMPTY, 0L, getHostIp(), topAppKey, appName, operation, db, sql);
    	threadPool.submit(new SimulateRrequest(param, LogType.SQL));
    }
    
    /**
     * 订单访问日志
     */
    public static void sendOrderLog(String operation, List<Long> tradeIds){
    	if (!APIConfig.get().isRisk()) {
			return;
		}
    	if(CommonUtils.isEmpty(tradeIds)) {
    		return;
    	}
    	String topAppKey = APIConfig.get().getApiKey();
    	String appName = APIConfig.get().getAppName();
    	Param param = new SimulateRrequest.Param(StringUtils.EMPTY, 0L, getHostIp(), topAppKey, appName, operation, tradeIds, operation);
    	threadPool.submit(new SimulateRrequest(param, LogType.ORDER));
    }
    
    // get the ip address
    private static String getHostIp(){ 
    	return "120.26.193.87";
        /*try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			log.error(e.getMessage(), e);
		}
        return null;*/
    }
    
    public static List<Long> getOrderTid(List<OrderDisplay> orderDisplay){
    	List<Long> tidList = new ArrayList<Long>();
        Iterator<OrderDisplay> iterator = orderDisplay.iterator();
        while(iterator.hasNext()){
        	tidList.add(iterator.next().getTid());
        }
		return tidList;
    }
    
    public static List<Long> getTradeTid(List<TradeDisplay> tradeDisplay){
    	List<Long> tidList = new ArrayList<Long>();
        Iterator<TradeDisplay> iterator = tradeDisplay.iterator();
        while(iterator.hasNext()){
        	tidList.add(iterator.next().getTid());
        }
		return tidList;
    }
}
