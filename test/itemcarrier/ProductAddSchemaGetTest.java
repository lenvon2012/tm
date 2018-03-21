package itemcarrier;

import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.TmallProductAddSchemaGetRequest;
import com.taobao.api.response.TmallProductAddSchemaGetResponse;

public class ProductAddSchemaGetTest {

    protected static String url = "http://gw.api.taobao.com/router/rest";
    protected static String appkey = "21255586";
    protected static String appSecret = "04eb2b1fa4687fbcdeff12a795f863d4";
    protected static String sessionkey = "6102b014e17396ZZ4f8fb08a1439c700b36af7de6498071771532983";
    
    public static void ItemPropsGet(){
		TaobaoClient client = new DefaultTaobaoClient(url, appkey, appSecret);
		TmallProductAddSchemaGetRequest req = new TmallProductAddSchemaGetRequest();
		req.setCategoryId(50003881L);
		req.setBrandId(27181L);
		TmallProductAddSchemaGetResponse response;
		try {
		    response = client.execute(req, sessionkey);
		    System.out.println(response.getBody());
		} catch (ApiException e) {
		    e.printStackTrace();
		}

    }

    public static void main(String[] args) {
    	ProductAddSchemaGetTest.ItemPropsGet();
    }

}