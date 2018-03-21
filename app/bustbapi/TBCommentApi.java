
package bustbapi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.taobao.api.ApiException;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.TraderatesGetRequest;
import com.taobao.api.response.TraderatesGetResponse;

public class TBCommentApi {

    public static void hello() throws ParseException, ApiException {
        TaobaoClient client = TBApi.genClient();
        TraderatesGetRequest req = new TraderatesGetRequest();
        req.setFields("tid,oid,role,nick,result,created,rated_nick,item_title,item_price,content,reply,num_iid");
        req.setRateType("get");
        req.setRole("seller");
        req.setResult("good");
        req.setPageNo(1L);
        req.setPageSize(100L);
        Date dateTime = SimpleDateFormat.getDateTimeInstance().parse("2011-01-01 00:00:00");
        req.setStartDate(dateTime);
        dateTime = SimpleDateFormat.getDateTimeInstance().parse("2011-01-02 00:00:00");
        req.setEndDate(dateTime);
        req.setTid(123456L);
        req.setUseHasNext(true);
        req.setNumIid(1234L);
        TraderatesGetResponse response = client.execute(req, null);
    }
}
