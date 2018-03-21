package utils;

import bustbapi.TBApi;

import com.dbt.commons.Params.Comm;
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
import com.taobao.api.request.ItemUpdateRequest;
import com.taobao.api.request.ItemcatsAuthorizeGetRequest;
import com.taobao.api.request.ItempropsGetRequest;
import com.taobao.api.request.LogisticsAddressSearchRequest;
import com.taobao.api.request.SellercatsListGetRequest;
import com.taobao.api.response.DeliveryTemplatesGetResponse;
import com.taobao.api.response.ItemUpdateResponse;
import com.taobao.api.response.ItemcatsAuthorizeGetResponse;
import com.taobao.api.response.ItempropsGetResponse;
import com.taobao.api.response.LogisticsAddressSearchResponse;
import com.taobao.api.response.SellercatsListGetResponse;

import java.util.ArrayList;
import java.util.List;

import models.item.ItemCatPlay;
import models.itemCopy.SkuProps;
import models.itemCopy.dto.PropDto;
import models.itemCopy.taobao.api.domain.SizeMappingTemplate;
import models.itemCopy.taobao.api.request.TmallItemOuteridUpdateRequest;
import models.itemCopy.taobao.api.request.TmallItemSizemappingTemplatesListRequest;
import models.itemCopy.taobao.api.response.TmallItemOuteridUpdateResponse;
import models.itemCopy.taobao.api.response.TmallItemSizemappingTemplatesListResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by oyster on 2017/3/29.
 */
public class ApiUtil {

	protected static String url = "http://gw.api.taobao.com/router/rest";
	protected static String appkey = "21348761";
	protected static String appSecret = "74854fd22c37b749b7d86b7fafd45a96";

	protected static TaobaoClient client;

	protected static Logger log = LoggerFactory.getLogger(ApiUtil.class);

	static {
		client =TBApi.genClient();//  new DefaultTaobaoClient(url, appkey, appSecret);
	}

	/**
	 * 获取前台展示的店铺内卖家自定义商品类目
	 */
	public static List<SellerCat> getSellerCats(String nickname) {
		try {
			SellercatsListGetRequest req = new SellercatsListGetRequest();
			req.setNick(nickname);
			SellercatsListGetResponse rsp = client.execute(req);
			// log.info("自定义商品类目\n"+new Gson().toJson(rsp.getSellerCats()));
			return rsp.getSellerCats();
		} catch (ApiException e) {
			e.printStackTrace();
			log.error("调用获取店铺自定义商品类目信息API异常" + e.getErrMsg());
		}
		return null;

	}

	/**
	 * 获取运费模板
	 * 
	 * @return
	 */
	public static List<DeliveryTemplate> getShippingTemplate(String sessionKey) {
		try {
			DeliveryTemplatesGetRequest req = new DeliveryTemplatesGetRequest();
			req.setFields("template_id,template_name");
			DeliveryTemplatesGetResponse rsp = client.execute(req, sessionKey);
			// log.info("运费模板信息："+new
			// Gson().toJson(rsp.getDeliveryTemplates()));
			return rsp.getDeliveryTemplates();
		} catch (ApiException e) {
			e.printStackTrace();
			log.error("调用获取运费模板API异常" + e.getErrMsg());
		}
		return null;
	}

	/**
	 * 获取图片空间目录
	 */
	/*
	 * public static String getPicDirs(){ }
	 */

	/**
	 * 根据关键字查询符合条件的淘宝分类
	 * 
	 * @param key
	 * @return
	 */
	public static List<ItemCatPlay> getItemCats(String key) {
		List<ItemCatPlay> result = ItemCatPlay.findAllByNameLike(key);
		if (CommonUtil.isNullOrSizeZero(result)) {
			return null;
		}
		return getItemcats( result);
	}

	/**
	 * 根据父类目集合获取相关子类目集合
	 * @param result
	 * @return
	 */
	private static List<ItemCatPlay> getItemcats(List<ItemCatPlay> result) {
		List<ItemCatPlay> rtnCatPlays=new ArrayList<ItemCatPlay>();
		for (ItemCatPlay icp : result) {
			ItemCatPlay addIcp = new ItemCatPlay();
			if (!icp.isParent()) {
				// 叶子类目
				StringBuffer appandStr = new StringBuffer();
				if (icp.getParentCid() != 0) {
					ItemCatPlay fatherCatPlay = icp.getParentCat();
					if (fatherCatPlay.getParentCid() != 0) {
						ItemCatPlay grandCatPlay = fatherCatPlay.getParentCat();
						appandStr.append(grandCatPlay.getName() + " > ");
					}
					appandStr.append(fatherCatPlay.getName() + " > ");
				}
				addIcp.setCid(icp.getCid());
				addIcp.setName(appandStr.toString() + icp.getName());
				rtnCatPlays.add(addIcp);
			} else {
				// 非叶子类目则往下搜索
				List<ItemCatPlay> sonCatPlays1 = ItemCatPlay
						.findByParentCid(icp.getCid());
				if (!CommonUtil.isNullOrSizeZero(sonCatPlays1)) {
					// 扫描所有该类目下发的叶子类目
					for (ItemCatPlay lv1 : sonCatPlays1) {
						if (lv1.isParent()) {
							List<ItemCatPlay> sonCatPlays2 = ItemCatPlay
									.findByParentCid(lv1.getCid());
							if (!CommonUtil.isNullOrSizeZero(sonCatPlays2)) {
								for (ItemCatPlay lv2 : sonCatPlays2) {
									if (lv2.isParent()) {
										// 最后遍历一级
										List<ItemCatPlay> sonCatPlays3 = ItemCatPlay
												.findByParentCid(lv2.getCid());
										if (!CommonUtil
												.isNullOrSizeZero(sonCatPlays3)) {
											for (ItemCatPlay lv3 : sonCatPlays3) {
												if (lv3.isParent()) {
													// 待续
												} else {
													ItemCatPlay icp3 = new ItemCatPlay();
													icp3.setName(icp.getName()
															+ ">"
															+ lv1.getName()
															+ ">"
															+ lv2.getName()
															+ ">"
															+ lv3.getName());
													icp3.setCid(lv3.getCid());
													rtnCatPlays.add(icp3);
												}
											}
										}
									} else {
										ItemCatPlay icp2 = new ItemCatPlay();
										icp2.setName(icp.getName() + ">"
												+ lv1.getName() + ">"
												+ lv2.getName());
										icp2.setCid(lv2.getCid());
										rtnCatPlays.add(icp2);
									}
								}
							}
						} else {
							ItemCatPlay icp1 = new ItemCatPlay();
							icp1.setName(icp.getName() + ">" + lv1.getName());
							icp1.setCid(lv1.getCid());
							rtnCatPlays.add(icp1);
						}
					}

				}
			}

		}
		return rtnCatPlays;
	}

	/**
	 * 获取指定类目的必备属性集合
	 */
	public static List<ItemProp> getItemProps(Long cid) {
		try {
			ItempropsGetRequest req = new ItempropsGetRequest();
			req.setCid(cid);
			req.setFields("pid,name,is_input_prop,must,prop_values,is_taosir,taosir_do,is_sale_prop");
			ItempropsGetResponse rsp = client.execute(req);
//			log.info(rsp.getBody());
			// System.err.println(rsp.getBody());
			return rsp.getItemProps();
		} catch (ApiException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取类目下的所有必备props对
	 * 
	 * @param cid
	 * @return 无关键属性返回null
	 */
	public static PropDto getPropsByCid(Long cid) {
		PropDto pd = new PropDto();
		StringBuffer sb = new StringBuffer();
		List<ItemProp> itemProps = getItemProps(cid);
		if (CommonUtil.isNullOrSizeZero(itemProps)) {
			return null;
		}
		StringBuffer inputPids = new StringBuffer();
		// 关于度量衡属性的pid与str
		StringBuffer dlhPid = new StringBuffer();
		StringBuffer dlhStr = new StringBuffer();
		for (int i = 0; i < itemProps.size(); i++) {
			ItemProp itemProp = itemProps.get(i);
			
			if (itemProp == null ||!itemProp.getMust()||itemProp.getIsSaleProp())
				continue;

			StringBuffer tsdVal = new StringBuffer();
			// 判断是否是度量衡属性
			if (itemProp.getIsTaosir()) {
				ItemTaosirDO tsd = itemProp.getTaosirDo();
				dlhPid.append(itemProp.getPid() + ",");
				String str = "";
				// 判断是否是普通度量衡属性
				if (CommonUtil.isNullOrSizeZero(tsd.getExprElList())) {
					// 单位
					List<Feature> features = tsd.getStdUnitList();
					if (CommonUtil.isNullOrSizeZero(features)) {
						continue;
					}
					str = "1" + tsd.getStdUnitList().get(0).getAttrValue();
					// tsdVal.append("1"+tsd.getStdUnitList().get(0).getAttrValue());
				} else {
					// 表达式度量衡属性
					List<ItemTaoSirElDO> itses = tsd.getExprElList();
					for (int j = 1; j < itses.size(); j++) {
						ItemTaoSirElDO itse = itses.get(j);
						if (itse.getIsInput()) {
							str = str + "1";
							continue;
						}
						if (itse.getIsLabel()) {
							str = str + itse.getText();
						}
					}
					str = str + tsd.getStdUnitList().get(0).getAttrValue();
				}
				// 时间点度量衡暂不做判断
				log.error("cid:" + cid + "中有度量衡属性：" + itemProp.getName());
				dlhStr.append(str + ",");
				continue;
			}
			// 获取对应的PID属性值
			Long pid = itemProp.getPid();
			// 度量衡属性没有属性值
			if (tsdVal.length() > 0) {
				sb.append(pid + ":" + tsdVal.toString() + ";");
				continue;
			}
			
			// 判断是否是可输入字段
			if (itemProp.getIsInputProp()) {
				inputPids.append(pid + ",");
				continue;
			} else {
				//材质成分 
				if (pid==149422948L) {
					inputPids.append(pid + ",");
					continue;
				}
				
				if (CommonUtil.isNullOrSizeZero(itemProp.getPropValues())) {
					inputPids.append(pid + ",");
				}
//				if (itemProp.getPid()==20000||itemProp.getName().contains("品牌")) {
//					//判断是否有其他
//						//初步写死
//					sb.append(pid + ":" + 29534 + ";");
//				}
			}
			// 获取某个属性的值
			List<PropValue> propValues = itemProp.getPropValues();
			if (CommonUtil.isNullOrSizeZero(propValues)) 
				continue;
			PropValue propValue = propValues.get(0);
			Long vid = propValue.getVid();
			sb.append(pid + ":" + vid + ";");
			// 获取二级子属性
			String childPidAndVid = getSonItemProp(cid, pid + ":" + vid);
			if (childPidAndVid != null) {
				if (childPidAndVid.contains("-")) {
					inputPids.append(childPidAndVid.substring(0,
							childPidAndVid.indexOf(":"))
							+ ",");
				} else {
					sb.append(childPidAndVid + ";");
				}

			}

		}
		// 判断是否有值
		if (sb.length() > 0) {
			pd.setProps(sb.toString().substring(0, sb.toString().length() - 1)
					.replace(";;", ";"));
		}
		if (inputPids.length() > 0) {
			String inputPidWithoutDlh = inputPids.toString().substring(0,
					inputPids.toString().length() - 1);
			pd.setInputPids(dlhPid.toString() + inputPidWithoutDlh);
			// 生成inputStr
			pd.setInputStr(dlhStr.toString()
					+ CommonUtil.genratorInputStr(inputPidWithoutDlh));
		}
		return pd;
	}

	/**
	 * 获取子属性
	 */
	public static String getSonItemProp(Long cid, String child_path) {
		StringBuffer sb = new StringBuffer();
		try {
			ItempropsGetRequest req = new ItempropsGetRequest();
			req.setFields("pid,name,is_input_prop,must,prop_values");
			req.setCid(cid);
//			req.setIsKeyProp(true);
			req.setChildPath(child_path);
			ItempropsGetResponse rsp = client.execute(req);
			// System.out.println(rsp.getBody());
			List<ItemProp> itemProps = rsp.getItemProps();
			if (CommonUtil.isNullOrSizeZero(itemProps)) {
				return null;
			}
			for (int i = 0; i < itemProps.size(); i++) {
				
				ItemProp itemProp = itemProps.get(i);
				if (itemProp.getMust()==false) {
					continue;
				}
				// 判断子属性是否有值
				if (!CommonUtil.isNullOrSizeZero(itemProp.getPropValues())) {
					String result = itemProp.getPid() + ":"
							+ itemProp.getPropValues().get(0).getVid()
							+ (i == itemProps.size() - 1 ? "" : ";");
					sb.append(result);
				} else {
					// 无值判断是否可输入属性
					if (itemProp.getIsInputProp()) {
						String result = itemProp.getPid() + ":" + "-"
								+ (i == itemProps.size() - 1 ? "" : ";");
						sb.append(result);
					}
				}

			}
			return sb.toString();
		} catch (ApiException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取销售属性
	 * 
	 * @param cid
	 */
	public static List<ItemProp> getSalePropsByCid(Long cid) {
		try {
			ItempropsGetRequest req = new ItempropsGetRequest();
			req.setFields("pid,name,is_input_prop,prop_values");
			req.setCid(cid);
			req.setIsSaleProp(true);
			ItempropsGetResponse rsp = client.execute(req);
			return rsp.getItemProps();
		} catch (ApiException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取并且保存对应类目的销售属性
	 * 
	 * @param cid
	 * @return 返回新增的记录数
	 */
	public static Integer getAndSaveSkuProp(Long cid) {
		int add = 0;
		List<ItemProp> saleProps = getSalePropsByCid(cid);
		if (CommonUtil.isNullOrSizeZero(saleProps)) {
			// log.error("未从CID"+cid+"类目取到销售属性");
			return null;
		}
		for (ItemProp itemProp : saleProps) {
			List<PropValue> values = itemProp.getPropValues();
			if (CommonUtil.isNullOrSizeZero(values)) {
				continue;
			}
			for (PropValue propValue : values) {
				SkuProps sp = new SkuProps();
				sp.setCid(cid);
				sp.setPid(itemProp.getPid());
				sp.setName(propValue.getName());
				sp.setVid(propValue.getVid());
				sp.setPropName(itemProp.getName());
				if (sp.jdbcSave())
					add++;
			}
		}
		return add;
	}

	/**
	 * 获取用户默认发货地址
	 * 
	 * @param sessionKey
	 * @return
	 */
	public static AddressResult getDefAddress(String sessionKey) {
		LogisticsAddressSearchRequest req = new LogisticsAddressSearchRequest();
		req.setRdef("get_def");
		LogisticsAddressSearchResponse rsp;
		try {
			rsp = client.execute(req, sessionKey);
			List<AddressResult> results = rsp.getAddresses();
			if (CommonUtil.isNullOrSizeZero(results))
				return null;
			return results.get(0);
		} catch (ApiException e) {
			log.warn("get user default address error,msg:" + e.getMessage());
			return null;
		}

	}


	/**
	 * 根据关键字查询符合条件的卖家已获得授权的淘宝分类
	 * @param key
	 * @return
	 */
	public static List<ItemCatPlay> getAuthorizeItemCats(String key,String sessionKey) {
		List<ItemCatPlay> condition = new ArrayList<ItemCatPlay>();
		try {
			ItemcatsAuthorizeGetRequest req = new ItemcatsAuthorizeGetRequest();
			req.setFields(" item_cat.cid, item_cat.name,item_cat.parent_cid,item_cat.is_parent,item_cat.status");
			ItemcatsAuthorizeGetResponse rsp = client.execute(req, sessionKey);
			if (!CommonUtil.isNullOrEmpty(rsp.getErrorCode())) {
				log.error(rsp.getMsg());
				return null;
			}
			List<ItemCat> itemCats=rsp.getSellerAuthorize().getItemCats();
			if (!CommonUtil.isNullOrSizeZero(itemCats)) {
				for (ItemCat itemCat : itemCats) {
					if (itemCat.getStatus().equals("deleted")) {
						continue;
					}
					ItemCatPlay icp=new ItemCatPlay();
					icp.setParent(itemCat.getIsParent());
					icp.setCid(itemCat.getCid());
					icp.setParentCid(itemCat.getParentCid());
					icp.setName(itemCat.getName());
					condition.add(icp);
				}
			}
			List<ItemCatPlay> rtnCatPlays= getItemcats(condition);
			
			List<ItemCatPlay> finalCatPlays=new ArrayList<ItemCatPlay>();
			if (!CommonUtil.isNullOrSizeZero(rtnCatPlays)) {
				for (ItemCatPlay icp : rtnCatPlays) {
					if (icp.getName().contains(key)&&icp.getStatus()==0) {
						finalCatPlays.add(icp);
					}
				}
				
				return finalCatPlays;
			}
			
			
		} catch (ApiException e) {
			e.printStackTrace();
		}
		return null;
		
	}

	/**
	 * 获取卖家已被授权品牌
	 * @return 成功返回品牌，失败返回null
	 */
	public static List<Brand> getAuthorizeBrands(String sessionKey) { 
		ItemcatsAuthorizeGetRequest req = new ItemcatsAuthorizeGetRequest();
		req.setFields("brand.vid, brand.name");
		ItemcatsAuthorizeGetResponse rsp;
		try {
			rsp = client.execute(req, sessionKey);
			List<Brand> brands=rsp.getSellerAuthorize().getBrands();
			return brands;
		} catch (ApiException e) {
			e.printStackTrace();
		}
		return null;
	}
	
//	/**
//	 * 获取卖家的尺码表模板数据
//	 * @return 成功返回品牌，失败返回null
//	 */
//	public static List<SizeMappingTemplate> getSizeMappings(String sessionKey) { 
//		
//		TmallItemSizemappingTemplatesListRequest req = new TmallItemSizemappingTemplatesListRequest();
//		TmallItemSizemappingTemplatesListResponse rsp;
//		try {
//			rsp = client.execute(req, sessionKey);
//			List<SizeMappingTemplate> templates=rsp.getSizeMappingTemplates();
//			return templates;
//		} catch (ApiException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}

	
	/**
	 * 更新商家编码
	 * @param outerId 
	 */
	public static boolean updateOuterId(String sessionKey,Long numIid, String outerId){
		
		TmallItemOuteridUpdateRequest req = new TmallItemOuteridUpdateRequest();
		req.setItemId(numIid);
		req.setOuterId(outerId);
		TmallItemOuteridUpdateResponse rsp =null;// client.execute(req, sessionKey);
		try {
			rsp = client.execute(req, sessionKey);
			if (rsp.getBody().contains("error_response")) {
				log.error(rsp.getMsg());
				return false;
			}
//			System.out.println(rsp.getBody());
			return true;
		} catch (ApiException e) {
			e.printStackTrace();
		}
		return false;
		
	}
	
	
	public static void updateWi() {
		
	}
}
