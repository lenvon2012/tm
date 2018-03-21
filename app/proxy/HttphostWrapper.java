
package proxy;

import org.apache.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.utils.DateUtil;

public class HttphostWrapper {

    private static final Logger log = LoggerFactory.getLogger(HttphostWrapper.class);

    public static final String TAG = "HttphostWrapper";

    private HttpHost httphost;

    private Long created;

    private int failCount = 0;

    public HttphostWrapper() {
        super();
    }

    public HttphostWrapper(HttpHost httphost, Long created) {
        this.httphost = httphost;
        this.created = created;
    }

    public void setHttphost(HttpHost httphost) {
        this.httphost = httphost;
    }

    public HttpHost getHttphost() {
        return this.httphost;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public Long getCreated() {
        return this.created;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - this.getCreated() > IProxy.getInstance().getExpiredTime();
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    @Override
    public String toString() {
        return "HttphostWrapper [httphost=" + httphost + ", created=" + DateUtil.formDateForLog(created)
                + ", failCount=" + failCount + "]";
    }

    public void addFailCount() {
        this.failCount++;
        //log.info("[this add fail count:]" + this);
    }

    public boolean hasFailedTooManyTimes() {
        return failCount > 0;
    }

}
