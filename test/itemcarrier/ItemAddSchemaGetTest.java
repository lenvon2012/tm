package itemcarrier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import com.alibaba.fastjson.JSONObject;
import com.ciaosir.client.CommonUtils;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.TmallItemAddSchemaGetRequest;
import com.taobao.api.response.TmallItemAddSchemaGetResponse;
import com.taobao.top.schema.Util.XmlUtils;
import com.taobao.top.schema.enums.FieldTypeEnum;
import com.taobao.top.schema.exception.TopSchemaException;
import com.taobao.top.schema.factory.SchemaReader;
import com.taobao.top.schema.field.Field;
import com.taobao.top.schema.field.InputField;
import com.taobao.top.schema.field.MultiInputField;
import com.taobao.top.schema.option.Option;
import com.taobao.top.schema.rule.Rule;

public class ItemAddSchemaGetTest {

    protected static String url = "http://gw.api.taobao.com/router/rest";
    protected static String appkey = "21255586";
    protected static String appSecret = "04eb2b1fa4687fbcdeff12a795f863d4";
    protected static String sessionkey = "6102b014e17396ZZ4f8fb08a1439c700b36af7de6498071771532983";
    
    public static void ItemAddSchemaGet(){
		TaobaoClient client = new DefaultTaobaoClient(url, appkey, appSecret);
		TmallItemAddSchemaGetRequest req = new TmallItemAddSchemaGetRequest();
		req.setCategoryId(123454001L);
		req.setProductId(347078701L);
		req.setType("b");
//		req.setIsvInit(true);
		TmallItemAddSchemaGetResponse response;
		try {
		    response = client.execute(req, sessionkey);
		    String itemXmlString = response.getAddItemResult();
		    List<Field> fieldList = SchemaReader.readXmlForList(itemXmlString);
		    
		    Map<String, Field> fieldMap = SchemaReader.readXmlForMap(itemXmlString);
		    Field field = fieldMap.get("sell_points");
		    List<Field> fieldList222 = getFieldList(field);
		    System.out.println(response.getAddItemResult());
		} catch (ApiException e) {
		    e.printStackTrace();
		} catch (TopSchemaException e) {
			e.printStackTrace();
		}

    }
    
    public static String getDefaultValue(Field field) throws TopSchemaException {
    	Element fieldElm = field.toElement();
	    Element defaultValueEle = XmlUtils.getChildElement(fieldElm, "default-value");
	    String dvalue = StringUtils.EMPTY;
	    if(defaultValueEle != null){
			dvalue = defaultValueEle.getText();
			System.out.println(dvalue);
		}
		return dvalue;
    }
    
    public static Map getOptionMap(Field field) throws TopSchemaException {
    	Element fieldElm = field.toElement();
	    Element optionsEle = XmlUtils.getChildElement(fieldElm, "options");
	    List<Element> optionEleList = new ArrayList<Element>();
	    Map<String, String> optionMap = new HashMap<String, String>();
		if(optionsEle != null){
			optionEleList = XmlUtils.getChildElements(optionsEle, "option");
			if(CommonUtils.isEmpty(optionEleList)) {
				return optionMap;
			}
			for(Element optionEleEle : optionEleList){
				String displayName = XmlUtils.getAttributeValue(optionEleEle, "displayName");
				String value = XmlUtils.getAttributeValue(optionEleEle, "value");
				optionMap.put(displayName, value);
			}
		}
		return optionMap;
    }
    
    public static List<Field> getFieldList(Field field) throws TopSchemaException {
    	Element fieldElm = field.toElement();
	    Element fieldsEle = XmlUtils.getChildElement(fieldElm, "fields");
	    List<Field> fieldList = new ArrayList<Field>();
	    List<Element> fieldsEleList = new ArrayList<Element>();
	    if(fieldsEle != null){
			fieldsEleList = XmlUtils.getChildElements(fieldsEle, "field");
			for(Element a : fieldsEleList){
				Field elementToField = SchemaReader.elementToField(a);
				fieldList.add(elementToField);
			}
		}
		return fieldList;
    }

    public static void main(String[] args) {
    	ItemAddSchemaGetTest.ItemAddSchemaGet();
    }

}