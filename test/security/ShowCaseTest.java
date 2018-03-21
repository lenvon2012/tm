package security;

import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.domain.Shop;
import com.taobao.api.request.ShopRemainshowcaseGetRequest;
import com.taobao.api.response.ShopRemainshowcaseGetResponse;

public class ShowCaseTest {
	
	private static final String serverUrl = "https://eco.taobao.com/router/rest";
	
	private static final String appkey = "21255586";
	
	private static final String appSecret = "04eb2b1fa4687fbcdeff12a795f863d4";
	
	private static final String sessionkey = "6102a058eb24b1c50dZZ28e929ff9ca7de0083d7d3fb2d03169577297";
	
	public static void test() {
		TaobaoClient client = new DefaultTaobaoClient(serverUrl, appkey, appSecret);
		ShopRemainshowcaseGetRequest req = new ShopRemainshowcaseGetRequest();
		ShopRemainshowcaseGetResponse rsp;
		try {
			rsp = client.execute(req, sessionkey);
			Shop shop = rsp.getShop();
			if(shop != null) {
				System.out.println(shop.getAllCount() + "~~~" + shop.getUsedCount() + "~~~" + shop.getRemainCount());
			}
			System.out.println(rsp.getBody());
		} catch (ApiException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		test();
	}
	
}
