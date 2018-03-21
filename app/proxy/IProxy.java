
package proxy;

import java.util.concurrent.ConcurrentLinkedQueue;

import job.proxy.ProxiesUpdate;

import org.apache.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;

import com.ciaosir.client.utils.DateUtil;

import configs.TMConfigs;

public abstract class IProxy {

    private static final Logger log = LoggerFactory.getLogger(IProxy.class);

    public static final String TAG = "IProxy";

    ConcurrentLinkedQueue<HttphostWrapper> proxies = new ConcurrentLinkedQueue<HttphostWrapper>();

    public abstract ConcurrentLinkedQueue<HttphostWrapper> genProxies(int num);

    public ConcurrentLinkedQueue<HttphostWrapper> genProxies() {
        return genProxies(getFetchNum());
    }

    public ConcurrentLinkedQueue<HttphostWrapper> getProxies() {
        return proxies;
    }

    public void setProxies(ConcurrentLinkedQueue<HttphostWrapper> proxies) {
        this.proxies = proxies;
    }

    public synchronized HttpHost provide() {

        log.error(" current pool size model:" + proxies.size());
        if (proxies.size() > 0) {

            HttphostWrapper host = proxies.poll();
            if (host == null) {
                return null;
            }

            return host.getHttphost();
        }
        return null;
    }

    public synchronized void returnHost(HttpHost host) {
        proxies.add(new HttphostWrapper(host, System.currentTimeMillis()));
    }

    public synchronized HttphostWrapper provideWrapper() {
//        log.info("++checkPositive+ current pool size model:" + proxies.size());
        
        if (proxies.size() <= 0) {
            reinit();
        }
        HttphostWrapper host = proxies.poll();
        if (host == null) {
            return null;
        }

        if (host.getFailCount() <= 0) {
            proxies.add(host);
        }

        return host;
    }

    public synchronized HttphostWrapper poll() {
        return this.proxies.poll();
    }

    public synchronized void reinit() {
        reinit(getFetchNum());
    }

    public synchronized void reinit(int num) {
        if (num <= 0) {
            num = ProxiesUpdate.poolsize;
        }
        this.proxies = this.genProxies(num);
        log.info("[++checkPositive+ inittttttttttttttt this.proxies IProxy: ]" + this.proxies.size());
    }

    public static IProxy getInstance() {
        switch (TMConfigs.DAILIMODE) {
            case 1:
                return ProxyDaili999API.getInstance();
            case 2:
                return ProxyPanzhigaoAPI.getInstance();
            case 3:
                return ProxyXuehaiAPI.getInstance();
            case 4:
                return ProxyDaili71API.getInstance();
            default:
                return ProxyPanzhigaoAPI.getInstance();
        }
    }
    
    public static IProxy getInstanceByMode(int mode) {
    	if(mode != 1 && mode != 2 && mode != 3) {
    		mode = 1;
    	}
        switch (mode) {
            case 1:
                return ProxyDaili999API.getInstance();
            case 2:
                return ProxyPanzhigaoAPI.getInstance();
            case 3:
                return ProxyXuehaiAPI.getInstance();
            default:
                return ProxyPanzhigaoAPI.getInstance();
        }
    }

    public static IProxy[] allProxies = null;

    public static IProxy[] getAllProxies() {
        if (allProxies != null) {
            return allProxies;
        }

        allProxies = new IProxy[] {
                ProxyDaili999API.getInstance(), ProxyPanzhigaoAPI.getInstance(), ProxyXuehaiAPI.getInstance()
        };
        return allProxies;
    }

    public int getFetchNum() {
        return 120;
    }

    public int getInejctNum() {
        return Play.mode.isDev() ? 2 : 50;
    }

    public long getExpiredTime() {
        return DateUtil.ONE_MINUTE_MILLIS * 60;
    }

    public IProxy() {
        super();
        this.reinit(getFetchNum());
    }

}
