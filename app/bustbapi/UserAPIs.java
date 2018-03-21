
package bustbapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.api.domain.User;
import com.taobao.api.request.UserSellerGetRequest;
import com.taobao.api.response.UserSellerGetResponse;

public class UserAPIs {

    public static final Logger log = LoggerFactory.getLogger(UserAPIs.class);

    static String fields = "user_id,uid,nick,seller_credit,status,has_shop,type";

//    public static class UserGetApi extends TBApi<UserGetRequest, UserGetResponse, User> {
//
//        String userNick = null;
//
//        public UserGetApi(String sid, String userNick) {
//            super(sid);
//            this.userNick = userNick;
//        }
//
//        @Override
//        public UserGetRequest prepareRequest() {
//            UserGetRequest req = new UserGetRequest();
//            req.setFields(fields);
//            if (this.userNick != null) {
//                req.setNick(userNick);
//            }
//            return req;
//        }
//
//        @Override
//        public User validResponse(UserGetResponse resp) {
//            if (resp == null) {
//                log.warn("No result return!!!");
//            }
//
//            ErrorHandler.validTaoBaoResp(resp);
//
//            return resp.getUser();
//        }
//
//        @Override
//        public User applyResult(User res) {
//            return res;
//        }
//    }

    public static class UserGetApi extends TBApi<UserSellerGetRequest, UserSellerGetResponse, User> {

        String userNick = null;

        Long userId;

        public UserGetApi(String sid, String userNick) {
            super(sid);
            this.userNick = userNick;
            this.retryTime = 1;
        }

        public UserGetApi(Long userId, String sid, String userNick) {
            super(sid);
            this.userId = userId;
            this.userNick = userNick;
            this.retryTime = 1;
        }

        public UserGetApi(String sid, String userNick, String appkey, String appSecret) {
            super(sid);
            this.userNick = userNick;
            this.appKey = appkey;
            this.appSecret = appSecret;
            this.retryTime = 1;
        }

        @Override
        public UserSellerGetRequest prepareRequest() {
            UserSellerGetRequest req = new UserSellerGetRequest();
            req.setFields(fields);
            if (this.userNick != null) {
//                req.set
            }
            return req;
        }

        @Override
        public User validResponse(UserSellerGetResponse resp) {
            if (resp == null) {
                log.warn("No result return!!!");
            }

            this.subErrorCode = resp.getSubCode();
//            ErrorHandler.fuckWithTheErrorCode(userId, sid, resp.getSubCode());

            return resp.getUser();
        }

        @Override
        public User applyResult(User res) {
            return res;
        }

    }
}
