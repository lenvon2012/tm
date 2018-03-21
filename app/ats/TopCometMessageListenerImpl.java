
package ats;

import java.text.SimpleDateFormat;
import java.util.Date;

import onlinefix.FixAllUserListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.api.internal.stream.message.TopCometMessageListener;

/**
 * http://open.taobao.com/doc/detail.htm?spm=0.0.0.0.sn0QdH&id=101381
 *  1）105消息（服务端踢出），说明已经达到这个连接下发消息的瓶颈，当收到此消息后sdk会调用ITopCometMessageListener.onServerKickOff，并且关闭连接。建议：
A.监控此方法。
B.客户端使用多连接。
C.如果可以，客户端去掉一些不需要的消息类型。

2）如果连接在半小时里发生10次read time out，则sdk会调用IConnectionLifeCycleListener.onMaxReadTimeoutException 并且关闭连接。建议：
A.客户端查看网络原因。
B.监控器方法。

3）在初次建立连接的时候由于指定的appkey与secret不匹配或者其他原因导致的建立不成功，服务端会调用。IConnectionLifeCycleListener.onSysErrorException，建议客户端把异常信息打出来可以看到出错的原因。
 * @author zrb
 *
 */
public class TopCometMessageListenerImpl implements TopCometMessageListener {

    private static final Logger log = LoggerFactory.getLogger(TopCometMessageListenerImpl.class);

    private static final String TAG = "TopCometMessageListenerImpl";

    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 收到消息
     */
    public void onReceiveMsg(final String message) {

        /**
         * 处理消息
         * 注释掉通过Ats的消息接收处理
         */
//        Runnable cosumer = new Runnable() {
//            public void run() {
//                MessagesDealer.onReceiveMessage(message);
//            }
//        };
        
//        log.info(sdf.format(new Date()) + ":receive:" + message);
//        TMConfigs.msgConsumeFactory.consume(cosumer);
        
    }

    public void onConnectReachMaxTime() {
//        new FixAllUserListener().now();
//        if (TMConfigs.App.ENABLE_TMHttpServlet && APIConfig.get().getPlatform() == Platform.taobao) {
//            log.error(" start  tm http servlet");
//            new TMHttpServlet().start();
//        }

        log.error(getLoggerInfo("onConnectReachMaxTime", sdf.format(new Date()) + ":reach max time"));
    }

    public void onDiscardMsg(String message) {
//        new FixAllUserListener().now();
/*        if (TMConfigs.App.ENABLE_TMHttpServlet && APIConfig.get().getPlatform() == Platform.taobao) {
            log.error(" start  tm http servlet");
            new TMHttpServlet().start();
        }
*/
        log.error(getLoggerInfo("onDiscardMsg", sdf.format(new Date()) + ":discardMsg:" + message));
    }

    public void onServerUpgrade(String message) {
        log.error(getLoggerInfo("onServerUpgrade", sdf.format(new Date()) + ":DeployMsg:" + message));
    }

    public void onServerRehash() {
        log.error(getLoggerInfo("onServerRehash", sdf.format(new Date()) + ":server rehash"));
    }

    public void onServerKickOff() {
        new FixAllUserListener().now();
        log.info(getLoggerInfo("onServerKickOff", sdf.format(new Date()) + ":server kickoff"));
    }

    public void onOtherMsg(String message) {
        log.info(getLoggerInfo("onOtherMsg", sdf.format(new Date()) + ":otherMsg:" + message));
    }

    public void onException(Exception ex) {
        log.info(getLoggerInfo("onException", sdf.format(new Date()) + ":exception:" + ex));
    }

    public void onConnectMsg(String message) {
        log.info(getLoggerInfo("onConnectMsg", sdf.format(new Date()) + ":connection success message:" + message));
    }

    public void onHeartBeat() {
        log.info(getLoggerInfo("onHeartBeat", sdf.format(new Date()) + ":heart beat"));
    }

    public void onClientKickOff() {
        log.info(getLoggerInfo("onClientKickOff", sdf.format(new Date()) + ":client kick off"));
    }

    public void onPartTypeKickOff(String message) {
        log.info(getLoggerInfo("onPartTypeKickOff", sdf.format(new Date()) + message));
    }

    private String getLoggerInfo(String functionName, String loggerInfo) {
        return (new Date().toString()).concat(" TopCometMessageListenerImpl.").concat(functionName).concat(":")
                .concat(loggerInfo);
    }
}
