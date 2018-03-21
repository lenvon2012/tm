
package bustbapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.api.request.FuwuSaleLinkGenRequest;
import com.taobao.api.response.FuwuSaleLinkGenResponse;

public class FuwuApis {

    private final static Logger log = LoggerFactory.getLogger(FuwuApis.class);

    public static class SaleLinkGenApi extends TBApi<FuwuSaleLinkGenRequest, FuwuSaleLinkGenResponse, String> {

        String nick;

        String paramStr;

        public SaleLinkGenApi(String appKey, String appSecret, String nick, String paramStr) {
            super();
            this.appKey = appKey;
            this.appSecret = appSecret;
            this.nick = nick;
            this.paramStr = paramStr;
        }

        @Override
        public FuwuSaleLinkGenRequest prepareRequest() {
            FuwuSaleLinkGenRequest req = new FuwuSaleLinkGenRequest();

            if (nick != null) {
                req.setNick(nick);
            }
            req.setParamStr(paramStr);
            return req;
        }

        @Override
        public String validResponse(FuwuSaleLinkGenResponse resp) {

            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }

            if (!resp.isSuccess()) {
                log.error("resp submsg" + resp.getSubMsg());
                log.error("resp error code " + resp.getErrorCode());
                log.error("resp Mesg " + resp.getMsg());
                this.errorMsg = resp.getSubMsg();
                return null;
            }
            return resp.getUrl();
        }

        @Override
        public String applyResult(String res) {
            return res;
        }

    }
}
