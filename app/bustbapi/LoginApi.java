package bustbapi;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.api.request.SimbaLoginAuthsignGetRequest;
import com.taobao.api.response.SimbaLoginAuthsignGetResponse;

import configs.BusConfigs;

public class LoginApi {

    public final static Logger log = LoggerFactory.getLogger(LoginApi.class);

    /**
     * 获取登陆权限签名
     * 
     * @author LY
     * 
     */
    public static class AuthsignGetApi extends
            TBApi<SimbaLoginAuthsignGetRequest, SimbaLoginAuthsignGetResponse, String> {

        public String nick;

        public AuthsignGetApi(String sid) {
            super(sid);
            this.nick = null;
        }

        public AuthsignGetApi(String sid, String nick) {
            super(sid);
            this.nick = nick;
        }

        @Override
        public SimbaLoginAuthsignGetRequest prepareRequest() {

            if (!BusConfigs.RPT_ENABLE) {
                log.error("This Appkey does not have rpt permission!!! ");
                return null;
            }

            SimbaLoginAuthsignGetRequest req = new SimbaLoginAuthsignGetRequest();
            if (nick != null) {
                req.setNick(nick);
            }
            return req;
        }

        @Override
        public String validResponse(SimbaLoginAuthsignGetResponse resp) {

            if (resp == null) {
                return StringUtils.EMPTY;
            }

            ErrorHandler.CommonTaobaoHandler.getInstance().validResp(resp);

            if (resp.isSuccess()) {
                return resp.getSubwayToken();
            } else {
                return resp.getSubMsg();
            }
        }

        @Override
        public String applyResult(String res) {
            return res;
        }
    }
}
