package proxy;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import job.ApplicationStopJob;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.jobs.Job;
import utils.DateUtil;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.PYFutureTaskPool;

import configs.TMConfigs;

//@Every("30s")
//@OnApplicationStart(async = true)
public class CommonProxyPools extends Job {
    private static final Logger log = LoggerFactory.getLogger(CommonProxyPools.class);

    static String verifyUrl = "http://www.baidu.com/cache/global/img/gs.gif";

    public static ConcurrentLinkedQueue<HttphostWrapper> mainland = new ConcurrentLinkedQueue<HttphostWrapper>();
    
    public static Random random = new Random();
    
    public static int fullSize = 1200;
    
    public static Long sleepFor = DateUtil.ONE_HOUR_MILLIS * 3;
    
    public static Long panzhigao_shutDownAt = 0L;
    
    public static Long daili71_shutDownAt = 0L;
    
    public static Long daili666_shutDownAt = 0L;
    
    static PYFutureTaskPool<Void> diagResultPool;
    
    public void doJob() {
        if (Play.mode.isDev()) {
            return;
        }
        if (!TMConfigs.ALLOW_COMMON_AUTO_PROXY) {
            return;
        }
    	if(mainland.size() >= fullSize) {
    		log.info("CommonProxyPools mainland size = " + mainland.size() + "" +
    				" and is over fullSize " + fullSize);
    		return;
    	}
    	
    	// 目前只有999和panzhigao可以用
        int mode = (random.nextInt(100) % 3)  + 1;
        if(mode == 1) {
        	mode = 3;
        }
        log.info("CommonProxyPools random mode = " + mode);
        IProxy model = null;
        if(mode == 2){
        	if(panzhigao_shutDownAt > 0 && (System.currentTimeMillis() - panzhigao_shutDownAt < sleepFor)) {
        		log.error("daili panzhigao is shutdown in 3 hours!!!");
        		return;
        	}
        	model = ProxyPanzhigaoAPI.getInstance();
        } else if(mode == 3) {
        	if(daili666_shutDownAt > 0 && (System.currentTimeMillis() - daili666_shutDownAt < sleepFor)) {
        		log.error("daili 666 is shutdown in 3 hours!!!");
        		return;
        	}
        	model = ProxyDaili666API.getInstance();
        }
        
        if(model == null) {
        	return;
        }
        
        final ConcurrentLinkedQueue<HttphostWrapper> proxies = model.genProxies(500);

        if(proxies != null && proxies.size() > 0) {
        	log.info("CommonProxyPools job proxies size = " + proxies.size() + " with mode = " + mode);
        	for(final HttphostWrapper wrapper : proxies) {
        		getDiagResultPool().submit(new Callable<Void>() {
	                @Override
	                public Void call() throws Exception {
	                    if (wrapper == null) {
	                        return null;
	                    }
	                    if(verifyWrapper(wrapper) != null) {
	                    	mainland.add(wrapper);
	                    }
	                    return null;
	                }
	            });
        	}
        	proxies.clear();
        } else {
        	log.error("CommonProxyPools job proxies size = 0 with mode = " + mode);
        }       

        log.info("[TMProxiesUpdate new proxies pool size = " + mainland.size());
    }

    public static HttphostWrapper provideWrapper() {
//      log.info("++checkPositive+ current pool size model:" + proxies.size());
      if(mainland == null) {
    	  mainland = new ConcurrentLinkedQueue<HttphostWrapper>();
      }
      if (mainland.size() <= 0) {
    	  int mode = (random.nextInt(100) % 3)  + 1;
    	  if(mode == 1) {
    		  mode = 3;
    	  }
    	  ConcurrentLinkedQueue<HttphostWrapper> tmps = new ConcurrentLinkedQueue<HttphostWrapper>();
    	  if(mode == 2 && panzhigao_shutDownAt <= 0) {
    		  tmps = ProxyPanzhigaoAPI.getInstance().genProxiesUsingCloud(100);
    	  } else if(mode == 3 && daili71_shutDownAt <= 0) {
    		  tmps = ProxyDaili71API.getInstance().genProxies(100);
    	  }
    	  
    	  if(!CommonUtils.isEmpty(tmps)) {
    		  mainland.addAll(tmps);
    	  }
          
      }
      if(mainland.size() <= 0) {
    	  return null;
      }
      HttphostWrapper host = mainland.poll();
      if (host == null) {
          return null;
      }

      return host;
    }
    
    public static HttphostWrapper provideWrapperAddNew() {
        if(mainland == null) {
            mainland = new ConcurrentLinkedQueue<HttphostWrapper>();
        }
        if (mainland.size() <= 0) {
            int mode = (random.nextInt(100) % 4)  + 1;
            if(mode == 1) {
                mode = 4;
            }
            ConcurrentLinkedQueue<HttphostWrapper> tmps = new ConcurrentLinkedQueue<HttphostWrapper>();
            if(mode == 2 && panzhigao_shutDownAt <= 0) {
                tmps = ProxyPanzhigaoAPI.getInstance().genProxiesUsingCloud(100);
            } else if(mode == 3 && daili71_shutDownAt <= 0) {
                tmps = ProxyDaili71API.getInstance().genProxies(100);
            } else if(mode == 4 && daili666_shutDownAt <= 0) {
                tmps = ProxyDaili666API.getInstance().genProxies(100);
            }
            
            if(!CommonUtils.isEmpty(tmps)) {
                mainland.addAll(tmps);
            }
            
        }
        if(mainland.size() <= 0) {
            return null;
        }
        HttphostWrapper host = mainland.poll();
        if (host == null) {
            return null;
        }

        return host;
      }
    
    private HttphostWrapper verifyWrapper(HttphostWrapper hostwrapper) {
        boolean flag = false;
        try {
            String result = directGet(verifyUrl, "", null, hostwrapper.getHttphost(), null);
            if (result != null && result.length() > 0) {
                flag = true;
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
        if (flag) {
            return hostwrapper;
        } else {
        	//log.info("holy verifyWrapper failed for HttphostWrapper: " + hostwrapper.getHttphost());
            return null;
        }
    }
    
    private ConcurrentLinkedQueue<HttphostWrapper> verifyProxies(final ConcurrentLinkedQueue<HttphostWrapper> proxies, int failNum) {
        int size = proxies.size();
        int count = 0;
        while (count++ < size) {
            HttphostWrapper hostwrapper = proxies.poll();
            if (hostwrapper == null) {
                log.error("remove null host!");
                continue;
            }
            if (System.currentTimeMillis() - hostwrapper.getCreated() > IProxy.getInstance().getExpiredTime()) {
                failNum++;
                log.error("remove host over 20 mins : " + hostwrapper.getHttphost());
                continue;
            }
            boolean flag = false;
            try {
                String result = directGet(verifyUrl, "", null, hostwrapper.getHttphost(), null);
                if (result != null && result.length() > 0) {
                    flag = true;
                }
            } catch (Exception e) {
                log.warn(e.getMessage());
            }
            if (flag) {
                proxies.add(hostwrapper);
            } else {
                failNum++;
                log.error("remove host :" + hostwrapper.getHttphost());
            }
        }
       
        log.info("[original proxies pool size = " + size + "................................................]");
        log.info("[fail proxies size = " + failNum + "................................................]");
        return proxies;
    }
    
    public static String directGet(String url, String referer, String ua, HttpHost host, String cookies, int timeout) {
    	HttpClient httpclient = null;

        HttpResponse rsp = null;
        if(timeout < 3000) {
        	timeout = 3000;
        }
        try {
            httpclient = new DefaultHttpClient();
            if (host != null) {
                httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, host);
            }

            HttpConnectionParams.setSoTimeout(httpclient.getParams(), timeout);
            HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), timeout);
            httpclient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
            httpclient.getParams().setParameter(ClientPNames.MAX_REDIRECTS, 20);

            HttpGet httpGet = new HttpGet(url);
            if (referer != null) {
                httpGet.addHeader("Referer", referer);
            }
            if (cookies != null) {
                httpGet.addHeader("Cookie", cookies);
            }

            httpGet.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)");

            rsp = httpclient.execute(httpGet);
            HttpEntity entity = rsp.getEntity();
            String content = EntityUtils.toString(entity);
            EntityUtils.consume(entity);

            return content;
        } catch (Exception e) {
//            log.warn(e.getMessage(),e);
            log.warn(e.getMessage());
            if (e.getMessage().contains("refused")) {
                throw new RuntimeException("");
            }
        }
        return null;
    }
    
    public static String directGet(String url, String referer, String ua, HttpHost host, String cookies) {
        HttpClient httpclient = null;

        HttpResponse rsp = null;

        try {
            httpclient = new DefaultHttpClient();
            if (host != null) {
                httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, host);
            }

            HttpConnectionParams.setSoTimeout(httpclient.getParams(), 3000);
            HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), 3000);
            httpclient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
            httpclient.getParams().setParameter(ClientPNames.MAX_REDIRECTS, 20);

            HttpGet httpGet = new HttpGet(url);
            if (referer != null) {
                httpGet.addHeader("Referer", referer);
            }
            if (cookies != null) {
                httpGet.addHeader("Cookie", cookies);
            }
            if(ua == null) {
            	ua = NewProxyTools.DEFAULT_UA;
            }

            httpGet.addHeader("User-Agent", ua);

            rsp = httpclient.execute(httpGet);
            HttpEntity entity = rsp.getEntity();
            String content = EntityUtils.toString(entity);
            EntityUtils.consume(entity);

            return content;
        } catch (Exception e) {
//            log.warn(e.getMessage(),e);
            log.warn(e.getMessage());
            if (e.getMessage().contains("refused")) {
                throw new RuntimeException("");
            }
        }
        return null;
    }
    
    public static PYFutureTaskPool<Void> getDiagResultPool() {
        if (diagResultPool == null) {
            diagResultPool = new PYFutureTaskPool<Void>(64);
            ApplicationStopJob.addShutdownPool(diagResultPool);
        }
        return diagResultPool;
    }
}
