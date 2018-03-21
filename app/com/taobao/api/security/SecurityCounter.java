package com.taobao.api.security;

import com.taobao.api.internal.utils.LruHashMap;

/**
 * 计数器
 * 
 * @author changchun
 * @since 2016年8月16日 下午7:45:54
 */
public class SecurityCounter implements SecurityConstants {

    private static final LruHashMap<String, Counter> APP_USER_COUNTER_MAP = new LruHashMap<String, Counter>(16, 65536);// 私有秘钥计数器

    public static LruHashMap<String, Counter> getAppUserCounterMap() {
        return APP_USER_COUNTER_MAP;
    }

    private static final Counter COUNTER = new Counter();// 公共秘钥计数器

    public static Counter getCounter() {
        return COUNTER;
    }

    private static void addEncryptCount(String type, Counter counter) {
        if (counter == null) {
            return;
        }
        
        if (PHONE.equals(type)) {
            counter.getEncryptPhoneNum().incrementAndGet();
        } else if (NICK.equals(type)) {
            counter.getEncryptNickNum().incrementAndGet();
        } else if (RECEIVER_NAME.equals(type)) {
            counter.getEncryptReceiverNameNum().incrementAndGet();
        } else if (SIMPLE.equals(type)) {
            counter.getEncryptSimpleNum().incrementAndGet();
        } else if (SEARCH.equals(type)) {
            counter.getEncryptSearchNum().incrementAndGet();
        }
    }

    private static void addDecryptCount(String type, Counter counter) {
        if (counter == null) {
            return;
        }
        
        if (PHONE.equals(type)) {
            counter.getDecryptPhoneNum().incrementAndGet();
        } else if (NICK.equals(type)) {
            counter.getDecryptNickNum().incrementAndGet();
        } else if (RECEIVER_NAME.equals(type)) {
            counter.getDecryptReceiverNameNum().incrementAndGet();
        } else if (SIMPLE.equals(type)) {
            counter.getDecryptSimpleNum().incrementAndGet();
        } else if (SEARCH.equals(type)) {
            counter.getDecryptSearchNum().incrementAndGet();
        }
    }

    public static void addSearchCount(String type, Counter counter) {
        if (counter == null) {
            return;
        }
        
        if (PHONE.equals(type)) {
            counter.getSearchPhoneNum().incrementAndGet();
        } else if (NICK.equals(type)) {
            counter.getSearchNickNum().incrementAndGet();
        } else if (RECEIVER_NAME.equals(type)) {
            counter.getSearchReceiverNameNum().incrementAndGet();
        } else if (SIMPLE.equals(type)) {
            counter.getSearchSimpleNum().incrementAndGet();
        } else if (SEARCH.equals(type)) {
            counter.getSearchSearchNum().incrementAndGet();
        }
    }

    public static void addEncryptCount(String type, String session) {
        addEncryptCount(type, getCounter(session));
    }

    public static void addDecryptCount(String type, String session) {
        addDecryptCount(type, getCounter(session));
    }

    public static void addSearchCount(String type, String session) {
        addSearchCount(type, getCounter(session));
    }

    private static Counter getCounter(String session) {
        Counter counter = null;
        if (session == null) {
            counter = SecurityCounter.COUNTER;
        } else {
            counter = APP_USER_COUNTER_MAP.get(session);
            if (counter == null) {
                counter = new Counter();
                APP_USER_COUNTER_MAP.put(session, counter);
            }
        }
        return counter;
    }
    
    public static void cleanAppUserCounter() {
        APP_USER_COUNTER_MAP.clear();
    }
}
