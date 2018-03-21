package underup.frame.industry;

import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import bustbapi.BusAPI;

import com.ciaosir.commons.ClientException;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.domain.Shop;
import com.taobao.api.request.UserGetRequest;
import com.taobao.api.response.UserGetResponse;

public class ShopInfosThread implements Callable<LevelPicShopInfo>{
    private static final Logger log = LoggerFactory.getLogger(ShopInfosThread.class);
    
    String wangwang;
    
    public ShopInfosThread(String wangwang){
        this.wangwang = wangwang;
    }
    
    @Override
    public LevelPicShopInfo call() throws Exception {
        Shop shop = null;
        try {
            shop = new BusAPI.SingleShopApi(wangwang).execute();
        } catch (ClientException e) {
            log.error("get the shop info fialed ...");
        }
        String picPath = null;
        if (shop != null) {
            if(!StringUtils.isEmpty(shop.getPicPath()))
                picPath = "http://logo.taobao.com/shop-logo" + shop.getPicPath();
            else{
                picPath = "http://img03.taobaocdn.com/tps/i3/T1DbPGXldcXXXH8R6X-140-42.png";
            }
        } else {
            picPath = "http://img03.taobaocdn.com/tps/i3/T1DbPGXldcXXXH8R6X-140-42.png";
        }
        int level = getShopLevel(this.wangwang);
        return new LevelPicShopInfo(this.wangwang, picPath, level);
    }

    public int getShopLevel(String userNick) {
        TaobaoClient client = new DefaultTaobaoClient("http://gw.api.taobao.com/router/rest", "21348761",
                "74854fd22c37b749b7d86b7fafd45a96");
        UserGetRequest req = new UserGetRequest();
        req.setFields("seller_credit");
        req.setNick(userNick);
        UserGetResponse response;
        int level = 0;
        try {
            response = client.execute(req, "6100913115977207dae2b154ce42389d699d75789182ce31039626382");
            JSONObject user;
            try {
                user = new JSONObject(response.getBody());
                JSONObject user_get_response = user.getJSONObject("user_get_response");
                JSONObject u = user_get_response.getJSONObject("user");
                JSONObject seller_credit = u.getJSONObject("seller_credit");
                level = seller_credit.getInt("level");
            } catch (JSONException e) {
                log.error("---get the JSON resolve failed ......");
            }

        } catch (ApiException e) {
            log.error("---get the shop lever from taobao api fialed .......");
        }
        return level;

    }
}
