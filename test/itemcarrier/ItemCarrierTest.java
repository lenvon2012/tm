package itemcarrier;

import java.util.ArrayList;
import java.util.List;

import bustbapi.request.PromotionmiscCommonItemActivityGetRequest;
import bustbapi.request.PromotionmiscCommonItemDetailListGetRequest;
import bustbapi.response.PromotionmiscCommonItemActivityGetResponse;
import bustbapi.response.PromotionmiscCommonItemDetailListGetResponse;
import bustbapi.result.CommonItemActivity;
import bustbapi.result.CommonItemDetail;

import com.ciaosir.client.CommonUtils;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.domain.PromotionRange;
import com.taobao.api.request.PromotionmiscActivityRangeListGetRequest;
import com.taobao.api.request.TmallItemIncrementUpdateSchemaGetRequest;
import com.taobao.api.request.TmallItemSchemaIncrementUpdateRequest;
import com.taobao.api.response.PromotionmiscActivityRangeListGetResponse;
import com.taobao.api.response.TmallItemIncrementUpdateSchemaGetResponse;
import com.taobao.api.response.TmallItemSchemaIncrementUpdateResponse;
import com.taobao.top.schema.exception.TopSchemaException;
import com.taobao.top.schema.factory.SchemaReader;
import com.taobao.top.schema.factory.SchemaWriter;
import com.taobao.top.schema.field.Field;

public class ItemCarrierTest {

    protected static String url = "http://gw.api.taobao.com/router/rest";
    protected static String appkey = "21255586";
    protected static String appSecret = "04eb2b1fa4687fbcdeff12a795f863d4";
    protected static String sessionkey = "620012266107f7e95b05513f2445c3ZZb31470a3b7899e81727355702";
    
    public static void ItemCarrier(){
    	Long itemId = 523258207332L;
    	TaobaoClient client=new DefaultTaobaoClient(url, appkey, appSecret);
    	
    	TmallItemIncrementUpdateSchemaGetRequest req = new TmallItemIncrementUpdateSchemaGetRequest();
    	req.setItemId(itemId);
    	
    	TmallItemIncrementUpdateSchemaGetResponse response;
    	String xmlStirng = "";
		try {
			response = client.execute(req , sessionkey);
			xmlStirng = response.getUpdateItemResult();
		} catch (ApiException e) {
			e.printStackTrace();
		}
    	
    	List<Field> fieldList = new ArrayList<Field>();
		try {
			fieldList = SchemaReader.readXmlForList(xmlStirng);
		} catch (TopSchemaException e) {
			e.printStackTrace();
		}
    	/**
         * 对fieldList进行各种修改操作数据组装
         */
    	String addXml = "";
		try {
			addXml = SchemaWriter.writeParamXmlString(fieldList);
		} catch (TopSchemaException e) {
			e.printStackTrace();
		}
    	TmallItemSchemaIncrementUpdateRequest addReq = new TmallItemSchemaIncrementUpdateRequest();
    	addReq.setItemId(itemId);
    	addReq.setXmlData(addXml);
    	TmallItemSchemaIncrementUpdateResponse updateRes = null;
		try {
			updateRes = client.execute(addReq , sessionkey);
		} catch (ApiException e) {
			e.printStackTrace();
		}
    	Long newItemId = Long.parseLong(updateRes.getUpdateItemResult());
    }
    
    public static void CommonItemDetailGetTest() {
    	TaobaoClient client = new DefaultTaobaoClient(url, appkey, appSecret);
    	PromotionmiscCommonItemDetailListGetRequest req = new PromotionmiscCommonItemDetailListGetRequest();
    	req.setActivityId(2745005259L);
    	req.setPageNo(3L);
    	req.setPageSize(50L);
    	PromotionmiscCommonItemDetailListGetResponse rsp;
		try {
			rsp = client.execute(req, sessionkey);
			List<CommonItemDetail> detailList = rsp.getDetailList();
			for (CommonItemDetail detail : detailList) {
				System.out.print(detail.getItemId() + ",");
			}
		} catch (ApiException e) {
			e.printStackTrace();
		}
    }

    public static void main(String[] args) {
//    	ItemCarrierTest.ItemCarrier();
    	CommonItemDetailGetTest();
    }

}