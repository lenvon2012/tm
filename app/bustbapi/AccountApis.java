
package bustbapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;
import com.google.gson.Gson;
import com.taobao.api.request.SimbaAccountBalanceGetRequest;
import com.taobao.api.response.SimbaAccountBalanceGetResponse;

public class AccountApis {

    public final static Logger log = LoggerFactory.getLogger(AccountApis.class);

    public static class BalanceGetApi extends
            TBApi<SimbaAccountBalanceGetRequest, SimbaAccountBalanceGetResponse, Double> {

        public BalanceGetApi(String sid) {
            super(sid);
        }

        @Override
        public SimbaAccountBalanceGetRequest prepareRequest() {
            SimbaAccountBalanceGetRequest req = new SimbaAccountBalanceGetRequest();
            return req;
        }

        @Override
        public Double validResponse(SimbaAccountBalanceGetResponse resp) {
            if (resp == null) {
                log.warn("No result return!!!");
            }

            ErrorHandler.validTaoBaoResp(resp);

            log.info("blance :" + new Gson().toJson(resp));
            String balance = resp.getBalance();

            return CommonUtils.String2Double(balance);
        }

        @Override
        public Double applyResult(Double res) {
            return res;
        }
    }

}
