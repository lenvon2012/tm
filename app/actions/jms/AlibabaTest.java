package actions.jms;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.alibaba.open.param.CompanyGetParam;
import cn.alibaba.open.param.CompanyGetResult;
import cn.alibaba.open.param.CompanyInfo;
import cn.alibaba.open.param.MemberMemberIdGetParam;
import cn.alibaba.open.param.MemberMemberIdGetResult;

import com.alibaba.ocean.rawsdk.ApiExecutor;
import com.alibaba.product.param.AlibabaCategoryCategoryInfo;
import com.alibaba.product.param.AlibabaCategorySearchByKeywordParam;
import com.alibaba.product.param.AlibabaCategorySearchByKeywordResult;

public class AlibabaTest {
	
	private static final Logger log = LoggerFactory.getLogger(AlibabaTest.class);
	
	private static String APP_KEY = "3800274";
	
	private static String SEC_KRY = "e1wBMPhGdHap";
	
	private static String ACCESS_TOKEN = "c4399ee1-ea6d-4163-9e1d-ff05603dc99e";
	
	public static void CategorySearchByKeyword() {
		//设置appkey和密钥(seckey)
		ApiExecutor apiExecutor = new ApiExecutor(APP_KEY, SEC_KRY); 

		//构造API入参和出参
		//API出入参类命名规则：API名称每个单词首字母大写，并去掉分隔符（“.”），末尾加上Param（或Result），其中Param为入参、Result为出参
		AlibabaCategorySearchByKeywordParam param = new AlibabaCategorySearchByKeywordParam(); 
		param.setKeyword("童装"); 

		//调用API并获取返回结果
		AlibabaCategorySearchByKeywordResult result = apiExecutor.execute(param, ACCESS_TOKEN); 

		//对返回结果进行操作
		AlibabaCategoryCategoryInfo[] products = result.getProducts();
		System.out.println(products.toString());
	}
	
	public static void CompanyGet() {
		//设置appkey和密钥(seckey)
		ApiExecutor apiExecutor = new ApiExecutor(APP_KEY, SEC_KRY); 

		//构造API入参和出参
		//API出入参类命名规则：API名称每个单词首字母大写，并去掉分隔符（“.”），末尾加上Param（或Result），其中Param为入参、Result为出参
		CompanyGetParam param = new CompanyGetParam(); 
		param.setMemberId("b2b-1717722482");
		String[] fields = {"memberId"};
		param.setReturnFields(fields);

		//调用API并获取返回结果
		CompanyGetResult result = apiExecutor.execute(param, ACCESS_TOKEN); 

		//对返回结果进行操作
		CompanyInfo company = result.getCompany();
		System.out.println(company);
	}
	
	public static void MemberIdGet() {
		//设置appkey和密钥(seckey)
		ApiExecutor apiExecutor = new ApiExecutor(APP_KEY, SEC_KRY); 

		//构造API入参和出参
		//API出入参类命名规则：API名称每个单词首字母大写，并去掉分隔符（“.”），末尾加上Param（或Result），其中Param为入参、Result为出参
		MemberMemberIdGetParam param = new MemberMemberIdGetParam(); 
		param.setUserId(79742176L);

		//调用API并获取返回结果
		MemberMemberIdGetResult result = apiExecutor.execute(param, ACCESS_TOKEN); 

		//对返回结果进行操作
		Map memberInfo = result.getMemberInfo();
		System.out.println(memberInfo);
	}
	
	public static void main(String[] args) {
//		CategorySearchByKeyword();
		CompanyGet();
//		MemberIdGet();
	}
	
}
