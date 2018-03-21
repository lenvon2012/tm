import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import models.paipai.PaiPaiUser;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import ppapi.PaiPaiUserInfoApi;
import ppapi.models.PaiPaiUserInfo;
import smsprovider.SendInfo;
import smsprovider.QTL.QLTProvider;
import utils.ExcelUtil;
import actions.catunion.UserIdNickAction;
import actions.catunion.UserIdNickAction.BuyerIdApi;
import actions.catunion.UserRateSpiderAction;
import actions.catunion.UserRateSpiderAction.CommentType;
import actions.catunion.UserRateSpiderAction.UserRateInfo;

import com.ciaosir.client.api.SimpleHttpApi;
import com.jd.open.api.sdk.DefaultJdClient;
import com.jd.open.api.sdk.JdClient;
import com.jd.open.api.sdk.JdException;
import com.jd.open.api.sdk.domain.order.OrderResult;
import com.jd.open.api.sdk.domain.order.OrderSearchInfo;
import com.jd.open.api.sdk.request.order.OrderSearchRequest;
import com.jd.open.api.sdk.response.order.OrderSearchResponse;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.UserGetRequest;
import com.taobao.api.request.UserSellerGetRequest;
import com.taobao.api.response.UserGetResponse;
import com.taobao.api.response.UserSellerGetResponse;

public class JunitTest {

    @Before
    public void setUp() throws Exception {
    }

//     @Test
    public void genCreateTable16() {
        try {
            String sql = FileUtils.readFileToString(new File("/Users/navins/Code/taobao/tm/ts.sql"));
            for (int j = 0; j < 16; j++) {
                System.out.println(String.format(sql, j, j, j, j, j, j, j));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // @Test
    public void genSql() {
        try {
            List<String> list = FileUtils.readLines(new File("/Users/navins/Code/taobao/tm/ts.sql"));
            String words = StringUtils.join(list, "', '");
            System.out.println(words);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     @Test
    public void testUserIdSpider() {
//        Long userId = BuyerIdApi.findBuyerId("茜茜桐12");
//        System.out.println(userId);
//        UserRateInfo info = UserRateSpiderAction.spiderUserRateById(userId);
//        System.out.println(info);
         
         String userInfoUrl = UserIdNickAction.BuyerIdApi.getUserInfoUrl("幸福的豆钉妹");
         System.out.println(userInfoUrl);
    }
    
//    @Test
    public void testBadComment() {
        SimpleHttpApi.init(new String[] {
//              "py01",
//              "py02",
//              "py03",
              //              "py04",
              "subway01",
              "subway02",
//              "subway03",
//              "subway04",
              //                "subway05",
//              "op01",
              //              "op02",
//              "ss01",
//              "ss02",
//              "sp01",
//              "sp02",
              //              "bbn01:9092",
//            "bbn02:9092",
              "bbn03:9092",
              "bbn04:9092",
              //              "bbn06:9092",
//            "bbn08:9092",
//            "bbn09:9092",
//            "bbn10:9092",
//            "bbn21:9092",
//            "bbn24:9092",
              "bbn25:9092",
              "bbn25:9092",
              "bbn26:9092",
              "bbn26:9092",
              "bbn27:9092",
              "bbn27:9092",
              "bbn28:9092",
              "bbn28:9092",
              "bbn29:9092",
              "bbn29:9092",
              "bbn30:9092",
              "bbn30:9092",
              "bbn31:9092",
              "bbn31:9092",
              "bbn32:9092",
              "bbn32:9092",
              "bbn33:9092",
              "bbn33:9092",
      });
        String json = UserRateSpiderAction.doSpiderCommentList(1651420064L, StringUtils.EMPTY, 1, CommentType.Negative, true);
        System.out.println(json);
    }

//    @Test
    public void sms() {
        SendInfo sendNormalMsg = QLTProvider.getInstance().sendNormalMsg(1L, System.currentTimeMillis(), "15088682225",
                "fangkechong1: 淘宝买家(mxd1976128)给您差评，请及时登录淘宝查看。【差评防御师】");
        System.out.println(sendNormalMsg);
        // QLTProvider.getInstance().sendNormalMsg(1L, System.currentTimeMillis(), "15818629090",
        // "fangkechong1: 淘宝买家(mxd1976128)给您差评，请及时登录淘宝查看，购买宝贝[代购印度香米巴斯马蒂巴斯马蒂香米Basmati包邮5kg优泰国香米大米]。【差评防御师】");
    }

    @Test
    public void checkBalance() {
        System.out.println("剩余短信： " + QLTProvider.getInstance().checkBalance());
    }

    // @Test
    public void jd() throws JdException {
        String SERVER_URL = "";
        JdClient client = new DefaultJdClient(SERVER_URL, "c7b82199-f26d-459e-a4b3-d6f6cec4b869",
                "36AB8642CCEF66293AD391EE7E4E9515", "0da953632d0a4741beb0bbd096214605");
        OrderSearchRequest request = new OrderSearchRequest();
        request.setStartDate("2012-01-10 12:12:23");
        request.setEndDate("2012-02-20 12:13:13");
        request.setOrderState("WAIT_SELLER_STOCK_OUT");
        request.setPage("1");
        request.setPageSize("100");
        request.setOptionalFields("vender_id,order_id,pay_type");
        OrderSearchResponse response = client.execute(request);

        OrderResult result = response.getOrderInfoResult();
        List<OrderSearchInfo> list = result.getOrderInfoList();
        System.out.println(list);

    }

    // @Test
    public void user() throws ApiException {
        TaobaoClient client = new DefaultTaobaoClient("http://gw.api.taobao.com/router/rest", "21255586",
                "31d9c374ff99e6cd6d50e6b22daca68a");
        System.out.println(client);
        UserGetRequest req = new UserGetRequest();
        req.setFields("user_id,uid,nick,sex,buyer_credit,seller_credit,location,created,last_visit,birthday,type,status,alipay_no,alipay_account,alipay_account,email,consumer_protection,alipay_bind");
        req.setNick("mary199004");
        UserGetResponse response = client.execute(req, "6201100cega004929369bfb80560e2ab48b51d022e2ca641739016678");
        System.out.println(response.getBody());

        UserSellerGetRequest req1 = new UserSellerGetRequest();
        req1.setFields("user_id,nick,seller_credit,sex");
        UserSellerGetResponse response1 = client.execute(req1,
                "6201100cega004929369bfb80560e2ab48b51d022e2ca641739016678");

        System.out.println(response1.getBody());

    }

    // @Test
    public void testPaipai() {
        PaiPaiUser user = new PaiPaiUser(721719L, "cfb7eeb66e434c94f10d8c933af5ab9d");
        try {
            PaiPaiUserInfo rsp = new PaiPaiUserInfoApi(user).call();
            System.out.println(rsp);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // @Test
    public void testTbRate() {
        // tb0164233_33
        Long userId = UserIdNickAction.findWithTaobao("tb0164233_33");
        System.out.println(userId);
        UserRateInfo info = UserRateSpiderAction.spiderUserRateById(userId);
        System.out.println(info);

    }
    
//    @Test
    public void testExcel() {
        String[] ss = {"aaaa,bbb,ccc", "sss,ddd,fff"};
        List<String[]> records = new ArrayList<String[]>();
        records.add(ss);
        String fields = "111,222,333";
        String sheetName = "sheetff";
        String fileName = "test.xls";
        ExcelUtil.writeToExcel(records, fields, sheetName, fileName);
    }

}
