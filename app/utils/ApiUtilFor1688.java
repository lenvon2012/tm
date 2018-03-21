package utils;

import bustbapi.TBApi;

import com.alibaba.ocean.rawsdk.ApiExecutor;
import com.alibaba.product.param.AlibabaCategoryCategoryInfo;
import com.alibaba.product.param.AlibabaCategoryGetParam;
import com.alibaba.product.param.AlibabaCategoryGetResult;
import com.dbt.commons.Params.Comm;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.domain.AddressResult;
import com.taobao.api.domain.Brand;
import com.taobao.api.domain.DeliveryTemplate;
import com.taobao.api.domain.Feature;
import com.taobao.api.domain.ItemCat;
import com.taobao.api.domain.ItemProp;
import com.taobao.api.domain.ItemTaoSirElDO;
import com.taobao.api.domain.ItemTaosirDO;
import com.taobao.api.domain.Product;
import com.taobao.api.domain.PropValue;
import com.taobao.api.domain.SellerAuthorize;
import com.taobao.api.domain.SellerCat;
import com.taobao.api.domain.Sku;
import com.taobao.api.request.DeliveryTemplatesGetRequest;
import com.taobao.api.request.ItemcatsAuthorizeGetRequest;
import com.taobao.api.request.ItempropsGetRequest;
import com.taobao.api.request.LogisticsAddressSearchRequest;
import com.taobao.api.request.SellercatsListGetRequest;
import com.taobao.api.response.DeliveryTemplatesGetResponse;
import com.taobao.api.response.ItemcatsAuthorizeGetResponse;
import com.taobao.api.response.ItempropsGetResponse;
import com.taobao.api.response.LogisticsAddressSearchResponse;
import com.taobao.api.response.SellercatsListGetResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import models.item.ItemCatPlay;
import models.itemCopy.APiConfig1688;
import models.itemCopy.AlibabaCat;
import models.itemCopy.SkuProps;
import models.itemCopy.dto.PropDto;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by oyster on 2017/3/29.
 */
public class ApiUtilFor1688 {

	

	protected static ApiExecutor executor;
	
	
	//换取accessToken的url
	public static String GET_TOKEN_URL="https://gw.api.alibaba.com/openapi/param2/1/system.oauth2/getToken/";
	
	protected static Logger log = LoggerFactory.getLogger(ApiUtilFor1688.class);

	// 测试应用 api调用次数5000 轮番使用
	public static ApiExecutor genApiExecutor() {
		APiConfig1688 config=APiConfig1688.getValidApp();
		if (config==null) {
			throw new RuntimeException("API可调用次数不足~~~~~~~~");
		}
		ApiExecutor executor = null;
		executor = new ApiExecutor(config.getAppkey(), config.getAppSecret());
		return executor;
	}

	// 获取阿里类目关键词
	public static AlibabaCategoryCategoryInfo getInfoByCid(long catId) {
		AlibabaCategoryGetParam getParam = new AlibabaCategoryGetParam();
		getParam.setCategoryID(catId);
		getParam.setWebSite("1688");
		AlibabaCategoryGetResult getResult = genApiExecutor().execute(getParam);

		AlibabaCategoryCategoryInfo[] infos = getResult.getCategoryInfo();
		return infos[0];
	}
	
	// 获取阿里类目关键词
		public static AlibabaCat getAliCatByCid(long catId) {

			AlibabaCategoryCategoryInfo info = getInfoByCid(catId);
			if (info==null) {
				return null;
			}
			AlibabaCat cat=new AlibabaCat();
			cat.setCatId(info.getCategoryID());
			cat.setCatName(info.getName());
			cat.setLeaf(info.getIsLeaf());
			cat.setParentId(info.getParentIDs()[0]);
			return cat;
		}
	
	/**
	 * 获取阿里宝贝所属类目信息
	 * @return
	 */
	public String getCatName(long cid) {
		StringBuffer catName = new StringBuffer();
		AlibabaCategoryCategoryInfo leafInfo = getInfoByCid(cid);
		long parentId = 0l;
		while (leafInfo.getParentIDs() != null) {
			parentId = leafInfo.getParentIDs()[0];
			catName.append(leafInfo.getName() + "<<");
			leafInfo = getInfoByCid(parentId);
		}
		return catName.toString();
	}
	
	//根据ItemId获取类目ID
	public long getCidByItemId(String itemId){
		long catId=0l;
		return catId;
	}


	//刷新access_token
	public static void doRefresh() {
		List<APiConfig1688> list=APiConfig1688.getAppList();
		
		for (APiConfig1688 config : list) {
			String appkey=config.getAppkey();
			String appSecret=config.getAppSecret();
			String refresh_token=config.getRefreshToken();
			String url=GET_TOKEN_URL+appkey;
			String param="grant_type=refresh_token&client_id="+appkey+"&client_secret="+appSecret+"&refresh_token="+refresh_token;
			String result=CommonUtil.sendPost(url, param);
			long useTs=config.getUseTs();
			try {
				JSONObject resultJo = new JSONObject(result);
				config.setAccessToken(resultJo.getString("access_token"));
				//判断是否需要重置次数
				long now=System.currentTimeMillis();
				if (now-useTs>=DateUtil.ONE_HOUR_MILLIS*24) {
					//避免其他测试调用，初始值为100
					config.setUseCount(100);
					config.setUseTs(now);
					
				}
				config.setUseCount(config.getUseCount());
				config.rawUpdate();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		
		}
		
		
		
		
	}

	
	
	

}
