
package bustbapi;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;
import com.taobao.api.domain.User;
import com.taobao.api.request.UsersGetRequest;
import com.taobao.api.response.UsersGetResponse;

public class UsersGetApi {

    private final static Logger log = LoggerFactory.getLogger(FuwuApis.class);

    private final static String DEFAULTFIELDS = "nick,buyer_credit,seller_credit,location,created,last_visit,has_shop,promoted_type,vip_info  ";
    
    public static class UserGet extends TBApi<UsersGetRequest, UsersGetResponse, List<User>> {

        String nicks;

        String fields;

        public UserGet(String nicks) {
            super();
            this.nicks = nicks;
            this.fields = DEFAULTFIELDS;
        }
        
        public UserGet(String nicks, String fields) {
            super();
            this.nicks = nicks;
            this.fields = fields;
        }

        @Override
        public UsersGetRequest prepareRequest() {
        	UsersGetRequest req = new UsersGetRequest();

            if (StringUtils.isEmpty(nicks) == false) {
                req.setNicks(nicks);
            }
            if(StringUtils.isEmpty(fields)) {
            	fields = DEFAULTFIELDS;
            }
            req.setFields(fields);
            return req;
        }

        @Override
        public List<User> validResponse(UsersGetResponse resp) {

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
            return resp.getUsers();
            
        }

        @Override
        public List<User> applyResult(List<User> res) {
            return res;
        }

    }
}
