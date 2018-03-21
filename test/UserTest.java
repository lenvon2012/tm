import org.junit.Test;

import play.test.UnitTest;
import bustbapi.TBApi;

import com.google.gson.Gson;
import com.taobao.api.ApiException;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.UsersGetRequest;
import com.taobao.api.response.UsersGetResponse;

public class UserTest extends UnitTest {

    @Test
    public void testUserInfo() throws ApiException {
        TaobaoClient client = TBApi.genPYClient();
        UsersGetRequest req = new UsersGetRequest();
        req.setFields("user_id,nick,location.city");
        req.setNicks("hz0799,tb_tevonol,zhengye83");
        UsersGetResponse response = client.execute(req);
        System.out.println(new Gson().toJson(response));
    }
}
