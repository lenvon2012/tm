package controllers;

import java.util.HashMap;

import org.elasticsearch.common.joda.time.chrono.LimitChronology;
import org.json.JSONException;
import org.json.JSONObject;

import actions.alibaba.ItemCopyAction;
import models.LimitWord;
import models.item.ItemCatPlay;
import models.itemCopy.AliCatMapping;
import models.itemCopy.AlibabaCat;
import models.itemCopy.NotCustomizableProp;
import play.db.jpa.NoTransaction;
import play.mvc.Controller;
import result.TMResult;
import utils.ApiUtil;
import utils.ApiUtilFor1688;
import utils.CommonUtil;
import utils.CopyUtil;
import utils.ToolBy1688;

/**
 * 宝贝复制相关api
 * 
 * @author oyster
 * 
 */
public class CopyApi extends Controller {

	/**
	 * 查询是否有映射记录
	 * 
	 * @param aliCat
	 */
	@NoTransaction
	public static void isExsitMapping(long aliCat) {
		AliCatMapping mapping = AliCatMapping.getMappingByAliCid(aliCat);
		//判断该类目对应的映射淘宝类目是否失效
		ItemCatPlay play=ItemCatPlay.findByCid(mapping.getTbcid());
		if (play==null) {
			mapping.rawDelete();
			mapping=null;
		}
		if (mapping == null) {
			renderText(false);
		}
		renderText(true);
	}

	public static void doRefreshAccessToken(String p) {
		if ("zhuangbility".equalsIgnoreCase(p)) {
			ApiUtilFor1688.doRefresh();
			renderText("刷新成功！");
		}
		renderText("无权操作！");
	}
	
	public static void getWholeLimitStr(String p) {
		if ("zhuangbility".equalsIgnoreCase(p)) {
			renderText(LimitWord.getWholeLimitStr());
		}
		renderText("无权操作！");
	}

	 public static void getBatchCid(String itemIds){
		 TMResult result=new TMResult(false);
		 if (CommonUtil.isNullOrEmpty(itemIds)) {
			renderJSON(result);
		}else {
			String[] itemIdArr=itemIds.split(",");
			HashMap<String, Long> map=new HashMap<String, Long>();
			for (String itemId : itemIdArr) {
				String url = "https://detail.1688.com/offer/" + itemId + ".html";
				Long cid = ToolBy1688.getCatIdFor1688(url);
				map.put(itemId, cid);
			}
			result.setRes(map);
			result.setOk(true);
			renderJSON(result);
		}
		 
	 }

	public static void getMappingInfo(Long itemId, String p) {
		if ("zhuangbility".equalsIgnoreCase(p)) {
			String url = "https://detail.1688.com/offer/" + itemId + ".html";
			Long cid = ToolBy1688.getCatIdFor1688(url);
			if (cid == -1) {
				renderText("请输入正确的商品ID");
			}
			String wholeCatName = AlibabaCat.getWholeCatName(cid);
			JSONObject result = new JSONObject();
			// 是否已有映射关系
			AliCatMapping mapping = AliCatMapping.getMappingByAliCid(cid);
			if (mapping!=null) {
				//判断该类目对应的映射淘宝类目是否失效
				ItemCatPlay play=ItemCatPlay.findByCid(mapping.getTbcid());
				if (play==null) {
					mapping.rawDelete();
					mapping=null;
				}
//				renderText("无对应映射关系");
			}
		
			// 类目匹配到的淘宝类目
			String urlPath = "http://115.29.162.138:9090/api/ItemCat/search";
			
			String data = CommonUtil.sendPost(urlPath, "itemTitle="
					+ wholeCatName);
		
			try {
				JSONObject resultObject = new JSONObject(data);
				result.put("itemId", itemId);
				result.put("url", url);
				result.put("cid", cid);
				result.put("wholeCatName", wholeCatName);
				// result.put("wholeCatName", wholeCatName);
				result.put("mappingRelation", mapping);
				result.put("matchCidByTitle", resultObject);
				if (mapping!=null) {
					String tbWholeCatName=ItemCatPlay.getWholeCatName(mapping.getTbcid());
					result.put("tbWholeCatName", tbWholeCatName);
				}
				renderJSON(result);
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
		renderText("无权操作！");
	}

	/**
	 * 更新映射关系信息
	 * 
	 * @param aliCid
	 * @param tbCid
	 * @param aliItemId
	 * @param tbItemId
	 */
	public static void saveOrUpdateMapping(long aliCid, long tbCid,
			long aliItemId, long tbItemId, String p) {
		if ("zhuangbility".equalsIgnoreCase(p)) {
			AliCatMapping mapping = new AliCatMapping();
			mapping.setAlicid(aliCid);
			mapping.setAliItemId(aliItemId);
			mapping.setTbcid(tbCid);
			mapping.setTbItemId(tbItemId);
			if (mapping.jdbcSave()) {
				renderText("更新成功！");
			} else {
				renderText("更新失败！");
			}
		}
		renderText("无权操作！");

	}
	
	/**
	 * 更新映射关系信息
	 * 
	 * @param aliCid
	 * @param tbCid
	 * @param aliItemId
	 * @param tbItemId
	 */
	public static void updateOuterId(String sessionKey,Long numIid,String p,String outerId) {
		if ("zhuangbility".equalsIgnoreCase(p)) {
			
			if (ApiUtil.updateOuterId(sessionKey, numIid,outerId)) {
				renderText("更新成功！");
			} else {
				renderText("更新失败！");
			}
		}
		renderText("无权操作！");

	}
	
	public static void saveNotCustomProp(long cid, long pid, String p) {
		if ("zhuangbility".equalsIgnoreCase(p)) {
			NotCustomizableProp prop = new NotCustomizableProp();
			prop.setCid(cid);
			prop.setPid(pid);
//			mapping.setAlicid(aliCid);
//			mapping.setAliItemId(tbItemId);
//			mapping.setAliItemId(aliItemId);
//			mapping.setTbItemId(tbItemId);
			if (prop.rawInsert()) {
				renderText("添加成功！");
			} else {
				renderText("添加失败！");
			}
		}
		renderText("无权操作！");

	}
}
