package com.taobao.api.internal.report;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.api.ApiException;
import com.taobao.api.TaobaoClient;
import com.taobao.api.internal.utils.LruHashMap;
import com.taobao.api.security.Counter;
import com.taobao.api.security.SecurityCore;
import com.taobao.api.security.SecurityCounter;

/**
 * API报表
 * 
 * @author changchun
 */
public class ApiReporter {

    private static final long SLEEP_TIME = 1000L * 60 * 1;
    private static final Log LOG = LogFactory.getLog(ApiReporter.class);
    private static final AtomicBoolean INIT_SECRET_ATOMIC = new AtomicBoolean(false);
    private static final long FLUSH_INTERVAL = 1000L * 60 * 5;// 5分钟
    private static final long MIN_FLUSH_INTERVAL = 1000L * 60 * 1;// 1分钟
    private static final String APP_SECRET_TYPE = "2";
    private static final String APP_USER_SECRET_TYPE = "3";
    private TaobaoClient taobaoClient;

    public void initSecret(TaobaoClient taobaoClient) {
        this.taobaoClient = taobaoClient;
        if (!INIT_SECRET_ATOMIC.get()) {
            initSecretThread();
        }
    }

    /**
     * 初始化一次
     */
    @Deprecated
    private void initSecretThread() {
        if (INIT_SECRET_ATOMIC.compareAndSet(false, true)) {
            new Thread("flushSecretApiReporter-thread") {
                public void run() {
                    doUploadBiz();
                }
            }.start();
        }
    }

    private void doUploadBiz() {
        long uploadTime = System.currentTimeMillis() + FLUSH_INTERVAL;
        while (true) {
            try {
                Thread.sleep(SLEEP_TIME);
                LruHashMap<String, Counter> appUserCounterMap = SecurityCounter.getAppUserCounterMap();
                if (System.currentTimeMillis() >= uploadTime
                        || (appUserCounterMap.size() * 4 > appUserCounterMap.getMaxCapacity())) {
                    StringBuilder counterBuilder = new StringBuilder();
                    
                    Map<String, Counter> cloneAppUserCounter = cloneAppUserCounter(appUserCounterMap);
                    SecurityCounter.cleanAppUserCounter();
                    
                    int count = 0;
                    Iterator<Entry<String, Counter>> iterator = cloneAppUserCounter.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Entry<String, Counter> entry = iterator.next();
                        Counter counter = entry.getValue();
                        if (counterBuilder.length() > 0) {
                            counterBuilder.append(";");
                        }
                        counterBuilder.append(entry.getKey());
                        generateCounter(counterBuilder, counter);

                        if ((++count) % 100 == 0) {
                            doUpload(counterBuilder.toString(), APP_USER_SECRET_TYPE);
                            counterBuilder = new StringBuilder();
                        }
                    }
                    if (counterBuilder.length() > 0) {
                        doUpload(counterBuilder.toString(), APP_USER_SECRET_TYPE);
                        counterBuilder = new StringBuilder();
                    }

                    Counter counter = SecurityCounter.getCounter();
                    counterBuilder.append(SecurityCore.getAppUserSecretCache().size());
                    generateCounter(counterBuilder, counter);
                    counter.reset();

                    long uploadInterval = doUpload(counterBuilder.toString(), APP_SECRET_TYPE);
                    uploadTime = System.currentTimeMillis() + uploadInterval;
                }
            } catch (Throwable e) {
                LOG.error("flushSecretApiReporter error", e);
            }
        }
    }

    private Map<String, Counter> cloneAppUserCounter(Map<String, Counter> sourceMap) {
        Map<String, Counter> targetMap = new HashMap<String, Counter>();
        for (Entry<String, Counter> entry : sourceMap.entrySet()) {
            targetMap.put(entry.getKey(), entry.getValue());
        }
        return targetMap;
    }

    private long doUpload(String contentJson, String type) throws ApiException {
        long uploadInterval = FLUSH_INTERVAL;
        TopSdkFeedbackUploadRequest request = new TopSdkFeedbackUploadRequest();
        request.setType(type);
        request.setContent(contentJson);

        TopSdkFeedbackUploadResponse response = taobaoClient.execute(request, null);
        if (response.isSuccess()) {
            uploadInterval = response.getUploadInterval();
            if (uploadInterval < MIN_FLUSH_INTERVAL) {
                uploadInterval = FLUSH_INTERVAL;
            }
        }
        return uploadInterval;
    }

    private void generateCounter(StringBuilder counterBuilder, Counter counter) {

        // encrypt
        if (counterBuilder.length() > 0) {
            counterBuilder.append(",");
        }
        if (SecurityCounter.getCounter().getEncryptPhoneNum().get() != 0) {
            counterBuilder.append(counter.getEncryptPhoneNum());
        } else {
            appendZero(counterBuilder);
        }
        counterBuilder.append(",");
        if (counter.getEncryptNickNum().get() != 0) {
            counterBuilder.append(counter.getEncryptNickNum());
        } else {
            appendZero(counterBuilder);
        }
        counterBuilder.append(",");
        if (counter.getEncryptReceiverNameNum().get() != 0) {
            counterBuilder.append(counter.getEncryptReceiverNameNum());
        } else {
            appendZero(counterBuilder);
        }
        counterBuilder.append(",");
        if (counter.getEncryptSimpleNum().get() != 0) {
            counterBuilder.append(counter.getEncryptSimpleNum());
        } else {
            appendZero(counterBuilder);
        }
        counterBuilder.append(",");
        if (counter.getEncryptSearchNum().get() != 0) {
            counterBuilder.append(counter.getEncryptSearchNum());
        } else {
            appendZero(counterBuilder);
        }

        // decrypt
        counterBuilder.append(",");
        if (counter.getDecryptPhoneNum().get() != 0) {
            counterBuilder.append(counter.getDecryptPhoneNum());
        } else {
            appendZero(counterBuilder);
        }
        counterBuilder.append(",");
        if (counter.getDecryptNickNum().get() != 0) {
            counterBuilder.append(counter.getDecryptNickNum());
        } else {
            appendZero(counterBuilder);
        }
        counterBuilder.append(",");
        if (counter.getDecryptReceiverNameNum().get() != 0) {
            counterBuilder.append(counter.getDecryptReceiverNameNum());
        } else {
            appendZero(counterBuilder);
        }
        counterBuilder.append(",");
        if (counter.getDecryptSimpleNum().get() != 0) {
            counterBuilder.append(counter.getDecryptSimpleNum());
        } else {
            appendZero(counterBuilder);
        }
        counterBuilder.append(",");
        if (counter.getDecryptSearchNum().get() != 0) {
            counterBuilder.append(counter.getDecryptSearchNum());
        } else {
            appendZero(counterBuilder);
        }

        // search
        counterBuilder.append(",");
        if (counter.getSearchPhoneNum().get() != 0) {
            counterBuilder.append(counter.getSearchPhoneNum());
        } else {
            appendZero(counterBuilder);
        }
        counterBuilder.append(",");
        if (counter.getSearchNickNum().get() != 0) {
            counterBuilder.append(counter.getSearchNickNum());
        } else {
            appendZero(counterBuilder);
        }
        counterBuilder.append(",");
        if (counter.getSearchReceiverNameNum().get() != 0) {
            counterBuilder.append(counter.getSearchReceiverNameNum());
        } else {
            appendZero(counterBuilder);
        }
        counterBuilder.append(",");
        if (counter.getSearchSimpleNum().get() != 0) {
            counterBuilder.append(counter.getSearchSimpleNum());
        } else {
            appendZero(counterBuilder);
        }
        counterBuilder.append(",");
        if (counter.getSearchSearchNum().get() != 0) {
            counterBuilder.append(counter.getSearchSearchNum());
        } else {
            appendZero(counterBuilder);
        }
    }

    private void appendZero(StringBuilder counterBuilder) {
        counterBuilder.append("0");
    }
}
