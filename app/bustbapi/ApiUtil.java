package bustbapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.api.domain.User;

public class ApiUtil {

    public final static Logger log = LoggerFactory.getLogger(ApiUtil.class);

    public static boolean Api_Call_Limited = false;

    public static long apiCount = 0;

    public static void resetApiCount() {
        ApiUtil.apiCount = 0L;
    }

    public static long getApiCount() {
        return ApiUtil.apiCount;
    }

    public static boolean isApiCallLimited() {
        return Api_Call_Limited;
    }

    public static void setApiCallLimited(boolean apiCallLimited) {
        Api_Call_Limited = apiCallLimited;
    }

    public static boolean updateAppCallLimited() {

        if (ApiUtil.Api_Call_Limited) {
            ApiUtil.setApiCallLimited(false);
            User user = new UserAPIs.UserGetApi(null, "simpleliangy").call();
            if(user != null){
                setApiCallLimited(false);
            }
        }
        return ApiUtil.Api_Call_Limited;

    }
}
