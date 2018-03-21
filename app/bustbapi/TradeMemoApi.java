/**
 * 
 */
package bustbapi;

import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.api.request.TradeMemoUpdateRequest;
import com.taobao.api.response.TradeMemoUpdateResponse;

/**
 * @author navins
 * @date: Nov 12, 2013 12:36:52 PM
 */
public class TradeMemoApi {

    public final static Logger log = LoggerFactory.getLogger(TradeMemoApi.class);

    public static class TradeMemoUpdate extends TBApi<TradeMemoUpdateRequest, TradeMemoUpdateResponse, Boolean> {

        public User user;

        public Long tid;

        public String memo;

        public Long flag = 0L;

        public boolean reset = false;

        public TradeMemoUpdate(User user, Long tid, String memo) {
            super(user.getSessionKey());
            this.user = user;
            this.tid = tid;
            this.memo = memo;
        }

        public TradeMemoUpdate(User user, Long tid, Long flag, String memo) {
            super(user.getSessionKey());
            this.user = user;
            this.tid = tid;
            this.flag = flag;
            this.memo = memo;
        }

        public TradeMemoUpdate(User user, Long tid, Long flag, String memo, boolean reset) {
            super(user.getSessionKey());
            this.user = user;
            this.tid = tid;
            this.flag = flag;
            this.memo = memo;
            this.reset = reset;
        }

        @Override
        public TradeMemoUpdateRequest prepareRequest() {
            TradeMemoUpdateRequest req = new TradeMemoUpdateRequest(); 
            if (tid != null && tid > 0L) {
                req.setTid(tid);
            }
            req.setMemo(memo);
            req.setFlag(flag);
            req.setReset(reset);
            return req;
        }

        @Override
        public Boolean validResponse(TradeMemoUpdateResponse resp) {
            if (resp == null) {
                log.error("Null Resp Returned");
                return Boolean.FALSE;
            }

            if (!resp.isSuccess()) {
                log.error("resp submsg" + resp.getSubMsg());
                log.error("resp error code " + resp.getErrorCode());
                log.error("resp Mesg " + resp.getMsg());
                return Boolean.FALSE;
            }

            return Boolean.TRUE;
        }

        @Override
        public Boolean applyResult(Boolean res) {
            return res;
        }

    }
}
