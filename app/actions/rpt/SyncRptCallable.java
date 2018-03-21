package actions.rpt;

import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.PlayUtil;

import com.ciaosir.client.CommonUtils;

public abstract class SyncRptCallable<W> implements Callable<Void> {

    public final static Logger log = LoggerFactory.getLogger(SyncRptCallable.class);

    Long userId;
    String sid;
    String subwayToken;
    Long startTs;
    Long endTs;
    String userNick;
    int source;

    protected int retryTime = 5;
    protected long sleepInterval = 200L;

    public SyncRptCallable(Long userId, String sid, String userNick, String subwayToken, Long startTs, Long endTs, int source) {
        this.userId = userId;
        this.sid = sid;
        this.userNick = userNick;
        this.subwayToken = subwayToken;
        this.startTs = startTs;
        this.endTs = endTs;
        this.source = source;

    }

    @Override
    public Void call() throws Exception {

        long pageNo = 1L;
        
        long startTime = System.currentTimeMillis();

        while (true) {

            int count = 0;
            while (++count < retryTime) {

                if (count > 1) {
                    log.error("[Current Retry Time]" + count + "  for class:"
                            + this.getClass());
                    PlayUtil.sleepQuietly(sleepInterval);
                }

                List<W> resList = getApiResult(pageNo);

                if (CommonUtils.isEmpty(resList)) {
//                    log.info("out of the while!!!"+pageNo);
                    afterFinished(startTime);
                    
                    return null;
                }

                if (applyResult(resList)) {
                    break;
                }
            }
            pageNo++;
        }
    }

    protected abstract List<W> getApiResult(long pageNo);

    protected abstract boolean applyResult(List<W> resList);

    protected void afterFinished(long startTime) {
        
    }
    
}
