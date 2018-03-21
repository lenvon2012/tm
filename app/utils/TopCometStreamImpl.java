
package utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.taobao.api.internal.stream.Configuration;
import com.taobao.api.internal.stream.StreamConstants;
import com.taobao.api.internal.stream.StreamImplementation;
import com.taobao.api.internal.stream.TopCometStream;
import com.taobao.api.internal.stream.TopCometStreamRequest;
import com.taobao.api.internal.stream.TopCometSysErrorException;
import com.taobao.api.internal.stream.connect.ConnectionLifeCycleListener;
import com.taobao.api.internal.stream.connect.HttpClient;
import com.taobao.api.internal.stream.connect.HttpResponse;
import com.taobao.api.internal.stream.message.StreamMsgConsumeFactory;
import com.taobao.api.internal.stream.message.TopCometMessageListener;
import com.taobao.api.internal.util.RequestParametersHolder;
import com.taobao.api.internal.util.StringUtils;
import com.taobao.api.internal.util.TaobaoHashMap;
import com.taobao.api.internal.util.TaobaoUtils;

/**
 * 淘宝SDK复制过来
 * 
 * @author zhenzi 2011-8-9 上午09:59:31
 */
public class TopCometStreamImpl implements TopCometStream {
    private static final Logger logger = Logger.getLogger(TopCometStreamImpl.class);

    private ConnectionLifeCycleListener connectionListener;

    private TopCometMessageListener cometMessageListener;

    private Configuration conf;

    private StreamMsgConsumeFactory msgConsumeFactory = null;

    /**
     * 停掉全部连接
     */
    private boolean gloableStop = false;

    private List<ControlThread> controlThreads = new ArrayList<ControlThread>();

    protected TopCometStreamImpl(Configuration conf) {
        this.conf = conf;
    }

    public void setConnectionListener(
            ConnectionLifeCycleListener connectionLifeCycleListener) {
        this.connectionListener = connectionLifeCycleListener;
    }

    public void setMessageListener(TopCometMessageListener cometMessageListener) {
        this.cometMessageListener = cometMessageListener;
    }

    public void start() {
        if (cometMessageListener == null) {
            throw new RuntimeException("Comet message listener must not null");
        }

        Set<TopCometStreamRequest> cometRequests = conf.getConnectReqParam();
        for (TopCometStreamRequest cometRequest : cometRequests) {
            if (cometRequest.getConnectListener() == null) {
                cometRequest.setConnectListener(connectionListener);
            }
            if (cometRequest.getMsgListener() == null) {
                cometRequest.setMsgListener(cometMessageListener);
            }
        }
        msgConsumeFactory = new StreamMsgConsumeFactory(conf.getMinThreads(),
                conf.getMaxThreads(), conf.getQueueSize(), new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable arg0, ThreadPoolExecutor arg1) {
                    }
                });

        for (TopCometStreamRequest cometRequest : cometRequests) {
            // 这里不把线程设置成daemon线程的原因，如果设置成daemon线程后，如果主线程退出后这些线程都将自动退出
            ControlThread ct = new ControlThread(cometRequest);
            controlThreads.add(ct);
            new Thread(ct, "stream-control-thread-connectid-"
                    + cometRequest.getConnectId()).start();
        }
    }

    public class ControlThread implements Runnable {
        private static final String threadName = "top-stream-consume-thread";

        private TopCometStreamConsume currentStreamConsume;

        private boolean isReconnect = false;// 是否客户端发起重连

        private String serverRespCode = StreamConstants.CLIENT_FIRST_CONNECT;

        private ReentrantLock lock = new ReentrantLock();

        private Condition controlCondition = lock.newCondition();

        private int startConsumeThreadTimes = 0;

        private long lastStartConsumeThread = System.currentTimeMillis();

        /**
         * 停掉
         */
        private boolean stop = false;

        private TopCometStreamRequest cometReq;

        public ControlThread(TopCometStreamRequest cometReq) {
            this.cometReq = cometReq;
        }

        public ReentrantLock getLock() {
            return lock;
        }

        public Condition getControlCondition() {
            return controlCondition;
        }

        public void setServerRespCode(String serverRespCode) {
            this.serverRespCode = serverRespCode;
        }

        public void run() {
            long lastSleepTime = 0;
            while (!stop) {
                if (gloableStop) {
                    break;
                }
                try {
                    if (StreamConstants.SERVER_DEPLOY.equals(serverRespCode)) {// 服务端在发布
                        if (logger.isDebugEnabled()) {
                            logger.debug("Server is upgrade sleep "
                                    + conf.getSleepTimeOfServerInUpgrade()
                                    + " seconds");
                        }
                        try {
                            Thread.sleep(conf.getSleepTimeOfServerInUpgrade() * 1000);
                        } catch (InterruptedException e) {
                            // ignore;
                        }
                        startConsumeThread();
                    } else if (/* 客户端第一次发起连接请求 */
                    StreamConstants.CLIENT_FIRST_CONNECT.equals(serverRespCode)
                            ||
                            /* 服务端主动断开了所有的连接 */
                            StreamConstants.SERVER_REHASH
                                    .equals(serverRespCode)
                            ||
                            /* 连接到达最大时间 */
                            StreamConstants.CONNECT_REACH_MAX_TIME
                                    .equals(serverRespCode) ||
                            /* 在一些异常情况下需要重连 */
                            StreamConstants.RECONNECT.equals(serverRespCode)) {
                        startConsumeThread();
                    } else if (/* 客户端自己把自己踢开 */
                    StreamConstants.CLIENT_KICKOFF.equals(serverRespCode) ||
                            /* 服务端把客户端踢开 */
                            StreamConstants.SERVER_KICKOFF.equals(serverRespCode)) {
                        if ((StreamConstants.CLIENT_KICKOFF
                                .equals(serverRespCode) && !isReconnect)
                                || StreamConstants.SERVER_KICKOFF
                                        .equals(serverRespCode)) {
                            stop = true;
                            if (currentStreamConsume != null) {
                                currentStreamConsume.closed = true;
                            }
                            break;// 终止掉当前线程
                        }
                    } else {// 错误码设置出错，停止线程
                        stop = true;
                        break;
                    }
                    // 连接成功，开始休眠
                    try {
                        lock.lock();
                        lastSleepTime = System.currentTimeMillis();
                        controlCondition.await(conf.getHttpReconnectInterval(),
                                TimeUnit.SECONDS);
                        if (System.currentTimeMillis() - lastSleepTime >= (conf
                                .getHttpReconnectInterval() - 5 * 60) * 1000) {
                            /*
                             * 快要到达连接的最大时间了，需要重新发起连接
                             */
                            serverRespCode = StreamConstants.RECONNECT;
                            isReconnect = true;
                        }// 否则，是由于某种原因被唤醒的
                    } catch (Exception e) {
                        logger.error(e, e);
                    } finally {
                        lock.unlock();
                    }
                } catch (Throwable e) {
                    logger.error("Occur some error,stop the stream consume", e);
                    stop = true;
                    try {
                        lock.lock();
                        controlCondition.signalAll();
                    } catch (Exception ex) {
                        // ignore
                    } finally {
                        lock.unlock();
                    }
                }
            }
            // 此控制线程由于某种原因退出，从列表中移除掉
            controlThreads.remove(this);
        }

        private void startConsumeThread() {
            StreamImplementation stream = null;
            try {
                stream = getMsgStreamImpl();
                if (cometReq.getConnectListener() != null) {
                    cometReq.getConnectListener().onConnect();
                }
            } catch (TopCometSysErrorException e) {
                stop = true;
                logger.error(e, e);
                if (cometReq.getConnectListener() != null) {
                    cometReq.getConnectListener().onSysErrorException(e);
                }
            } catch (Exception ex) {
                stop = true;
                logger.error(ex, ex);
                if (cometReq.getConnectListener() != null) {
                    cometReq.getConnectListener().onConnectError(ex);
                }
            }
            currentStreamConsume = new TopCometStreamConsume(stream, this,
                    cometReq.getConnectListener());
            Thread consumeThread = new Thread(currentStreamConsume, threadName);
            consumeThread.setDaemon(true);
            consumeThread.start();
            lastStartConsumeThread = System.currentTimeMillis();
        }

        private StreamImplementation getMsgStreamImpl()
                throws TopCometSysErrorException, Exception {
            TaobaoHashMap param = new TaobaoHashMap();
            param.put(StreamConstants.PARAM_APPKEY, cometReq.getAppkey());
            if (!StringUtils.isEmpty(cometReq.getUserId())) {
                param.put(StreamConstants.PARAM_USERID, cometReq.getUserId());
            }
            if (!StringUtils.isEmpty(cometReq.getConnectId())) {
                param.put(StreamConstants.PARAM_CONNECT_ID,
                        cometReq.getConnectId());
            }
            String timestamp = String.valueOf(System.currentTimeMillis());
            param.put(StreamConstants.PARAM_TIMESTAMP, timestamp);
            RequestParametersHolder paramHolder = new RequestParametersHolder();
            paramHolder.setProtocalMustParams(param);
            String sign = null;
            try {
                sign = TaobaoUtils.signTopRequestNew(paramHolder,
                        cometReq.getSecret(), false);
                if (StringUtils.isEmpty(sign)) {
                    throw new RuntimeException("Get sign error");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            param.put(StreamConstants.PARAM_SIGN, sign);

            HttpClient httpClient = new HttpClient(conf, param);
            HttpResponse response = httpClient.post();

            return new MessageStreamImpl(msgConsumeFactory, response,
                    cometReq.getMsgListener(), this);
        }

    }

    class TopCometStreamConsume implements Runnable {
        private StreamImplementation stream;

        private boolean closed = false;

        private ControlThread ct;

        private ConnectionLifeCycleListener connectListener;

        TopCometStreamConsume(StreamImplementation stream, ControlThread ct,
                ConnectionLifeCycleListener connectListener) {
            this.stream = stream;
            this.ct = ct;
            this.connectListener = connectListener;
        }

        public void run() {
            while (!closed && stream.isAlive()) {
                try {
                    stream.nextMsg();
                } catch (IOException e) {// 出现了read time out异常
                    // 资源清理
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e1) {
                            logger.error(e1, e1);
                        }
                    }
                    stream = null;
                    closed = true;
                    // 通知
                    if (connectListener != null) {
                        try {
                            connectListener.onReadTimeout();
                        } catch (Exception ex) {
                            logger.error(ex, ex);
                        }
                    }
                    /**
                     * 30分钟内发送了10次IOException
                     */
                    if (System.currentTimeMillis() - ct.lastStartConsumeThread < 30 * 60 * 1000) {// 短时间内由于读取IOException连接了10次，则退出
                        ct.startConsumeThreadTimes++;
                        if (ct.startConsumeThreadTimes >= 10) {
                            ct.stop = true;
                            if (connectListener != null) {
                                try {
                                    connectListener.onMaxReadTimeoutException();
                                } catch (Exception maxE) {
                                    logger.error(maxE, maxE);
                                }
                            }
                            logger.error("Occure too many exception,stop the system,please check");
                            // 通知唤醒控制线程，但是不在发起重连接
                            try {
                                ct.lock.lock();
                                ct.controlCondition.signalAll();
                            } catch (Exception e2) {
                            } finally {
                                ct.lock.unlock();
                            }
                        } else {// 没有到达10次，通知重连
                            ct.startConsumeThreadTimes = 0;
                            ct.serverRespCode = StreamConstants.RECONNECT;
                            try {
                                ct.lock.lock();
                                ct.controlCondition.signalAll();
                            } catch (Exception e2) {
                            } finally {
                                ct.lock.unlock();
                            }
                        }
                    } else {
                        // 通知重连
                        ct.startConsumeThreadTimes = 0;
                        ct.serverRespCode = StreamConstants.RECONNECT;
                        try {
                            ct.lock.lock();
                            ct.controlCondition.signalAll();
                        } catch (Exception e2) {
                        } finally {
                            ct.lock.unlock();
                        }
                    }
                }
            }
            // 出现异常情况下做资源清理
            if (this.stream != null) {
                try {
                    this.stream.close();
                } catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                }
            }
        }
    }

    public void stop() {
        gloableStop = true;
        for (ControlThread ct : controlThreads) {
            try {
                ct.lock.lock();
                ct.controlCondition.signalAll();
            } catch (Exception e) {
            } finally {
                ct.lock.unlock();
            }
        }
        msgConsumeFactory.shutdown();
        logger.info("Stop stream consume");
    }

    @Override
    public void addNewStreamClient(TopCometStreamRequest newClient) {
        // No more add now...
    }

}
