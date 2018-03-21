package job.writter;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import models.visit.IVisitLog;
import models.visit.VisitLog;
import play.jobs.Every;
import play.jobs.Job;

import com.ciaosir.client.url.URLParser;

@Every("20s")
public class VisitLogWritter extends Job {

    public static final Queue<LogBean> logListToWritten = new ConcurrentLinkedQueue<LogBean>();

    public void doJob() {

        LogBean bean = null;
        while ((bean = logListToWritten.poll()) != null) {
            VisitLog.ensureLog(bean);
        }
    }

    public static void addMsg(String cookieIdString, String ip, Long userId, long timeMillis, String url) {
        if (!URLParser.isFromSimba(url)) {
            return;
        }

        logListToWritten.add(new LogBean(userId, timeMillis, url, ip, cookieIdString));
    }

    public static class LogBean implements IVisitLog {

        public LogBean(Long userId, long ts, String url, String ip, String cookieIdString) {
            super();
            this.userId = userId;
            this.ts = ts;
            this.url = url;
            this.ip = ip;
            this.cookieIdString = cookieIdString;
        }

        Long userId;

        long ts;

        String url;

        String ip;

        String cookieIdString;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public long getTs() {
            return ts;
        }

        public void setTs(long ts) {
            this.ts = ts;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getCookieIdString() {
            return cookieIdString;
        }

        public void setCookieIdString(String cookieIdString) {
            this.cookieIdString = cookieIdString;
        }

    }
}
