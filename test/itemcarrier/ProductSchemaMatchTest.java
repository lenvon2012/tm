package itemcarrier;

import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.TmallProductSchemaMatchRequest;
import com.taobao.api.response.TmallProductSchemaMatchResponse;

public class ProductSchemaMatchTest {

    protected static String url = "http://gw.api.taobao.com/router/rest";
    protected static String appkey = "21255586";
    protected static String appSecret = "04eb2b1fa4687fbcdeff12a795f863d4";
    protected static String sessionkey = "6102b014e17396ZZ4f8fb08a1439c700b36af7de6498071771532983";
    
    public static void ProductSchemaMatch(){
		TaobaoClient client = new DefaultTaobaoClient(url, appkey, appSecret);
		TmallProductSchemaMatchRequest req = new TmallProductSchemaMatchRequest();
		req.setCategoryId(50003881L);
		req.setPropvalues("<itemRule> <field id=\"prop_20000\" name=\"冰箱冰柜品牌\" type=\"singleCheck\">  <value>11016</value>  </field>  </itemRule>");
		TmallProductSchemaMatchResponse response;
		try {
		    response = client.execute(req, sessionkey);
		    System.out.println(response.getBody());
		} catch (ApiException e) {
		    e.printStackTrace();
		}

    }

    public static void main(String[] args) {
    	ProductSchemaMatchTest.ProductSchemaMatch();
    }

}