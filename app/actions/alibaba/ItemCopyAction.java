package actions.alibaba;

import actions.carriertask.CarrierTaskAction;
import bustbapi.ItemCopyApi;
import bustbapi.ItemCopyApiFor1688;
import bustbapi.ItemCopyApiForCustom;
import bustbapi.PicApi;

import com.alibaba.fastjson.JSON;
import com.ciaosir.client.CommonUtils;
import com.taobao.api.domain.*;

import configs.Subscribe;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import models.LimitWord;
import models.carrierTask.CarrierLimitForDQ;
import models.carrierTask.CarrierTask;
import models.carrierTask.ItemCarryCustom;
import models.carrierTask.ItemCarryCustomFor1688;
import models.carrierTask.ItemCarryCustomFor1688.PostAddressType;
import models.carrierTask.SubCarrierTask;
import models.item.ItemCatPlay;
import models.itemCopy.AliCatMapping;
import models.itemCopy.AlibabaCat;
import models.itemCopy.ItemCatProps;
import models.itemCopy.ItemExt;
import models.itemCopy.PriceUnit;
import models.itemCopy.dto.PropDto;
import models.itemCopy.dto.SalePropDto;
import models.itemCopy.dto.SkuDto;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import result.TMResult;
import utils.ApiUtil;
import utils.CommonUtil;
import utils.CopyUtil;
import utils.ToolBy1688;

/**
 * 处理复制阿里宝贝请求
 * 
 * @author oyster
 * 
 */
public class ItemCopyAction {

	public static final Logger log = LoggerFactory
			.getLogger(ItemCopyAction.class);

	/**
	 * 执行复制宝贝 
	 * 
	 * @throws Exception
	 */
	public static TMResult<Item> doCopy(String numiid, Long cid, User user,
			Boolean agian, Boolean isForDq) throws Exception {
		// 获取要复制的宝贝信息
		Item itemOrigin = getItemFromAlibaba(numiid, cid, user, agian,null);
		if (itemOrigin == null) {
			return new TMResult(false, "复制出错，获取原宝贝信息出错。", null);
		}
		TMResult<Item> result =  new ItemCopyApi(user, itemOrigin).itemCopy();
		// 宝贝属性上传
//		if (isForDq) {
//			// 宝贝复制自定义设置
//			ItemCarryCustomFor1688 itemCarryCustom = ItemCopyAction
//					.getItemCarryCustom(null, user);
//			ItemCopyApi itemCopyApi;
//			if (itemCarryCustom == null)
//				itemCopyApi = new ItemCopyApi(user, itemOrigin);
//			else
//				itemCopyApi = new ItemCopyApiForCustom(user, itemOrigin,
//						itemCarryCustom);

//			result =
//		} else {
//			result = ItemCopyApiFor1688.itemCarrier(user, itemOrigin);
//		}
		String url = CopyUtil.changeToUrl(numiid);
		if (result.isOk) {
			SubCarrierTask.recordSuccess(url, "", user.getUserNick(),
					result.getRes().getNumIid().toString(), SubCarrierTask.SubTaskType.$1688复制);
			if (user.getVersion() == Subscribe.Version.LL)
				CarrierLimitForDQ.updateUseCountByUserId(user.getId());
		
		}else {
			SubCarrierTask.recordError(url, user.getUserNick(),
					result.getMsg(), SubCarrierTask.SubTaskType.$1688复制);
		}

		return result;
	}

	/**
	 * 执行复制宝贝 1.获取原宝贝信息 2.封装成添加的淘宝商品对象
	 * 
	 * @throws Exception
	 */
	public static TMResult<Item> doCopyItem(String itemId, Long cid, User user,
			boolean again, boolean isForDq) {
		// 转换Url
		String url = CopyUtil.changeToUrl(itemId.toString());
		long aliCid=0l;
		try {
			if (cid<=0) {
				aliCid = ToolBy1688.getCatIdFor1688(url);
				
					AliCatMapping mapping=AliCatMapping.getMappingByAliCid(aliCid);
					//判断该类目对应的映射淘宝类目是否失效
					ItemCatPlay play=ItemCatPlay.findByCid(mapping.getTbcid());
					if (play==null) {
						mapping.rawDelete();
						mapping=null;
					}
				if (mapping!=null) {
					cid=mapping.getTbcid();
				}else {
					//无匹配记录则通过引擎匹配最优解
					//bbn29
					String pageTitle=AlibabaCat.getWholeCatName(aliCid);//ToolBy1688.getPageTitle();
					cid=ItemCopyAction.matchCidByTitle(pageTitle);
				}
			}
			TMResult<Item> tmResult = doCopy(itemId,  cid,  user, false, isForDq);
			log.warn("request url:" + url + " to cid :" + cid);
			String msg = tmResult.getMsg();
			if (!tmResult.isOk()) {
				// 如果属性出错，删除数据库中的CID数据
				if (tmResult.getMsg().contains("宝贝销售属性出错")) {
					// 部分类目下销售属性不支持自定义造成,重新请求复制，二次请求复制，排除不存在的自定义属性
					log.warn("first copyAction error,errorMsg:"
							+ tmResult.getMsg()
							+ " try copy agian without sku info");
					tmResult = doCopy(url,cid,  user, true, isForDq);
					if (msg.contains("属性出错")) {
						log.error("second copyAction error,errorMsg:"
								+ tmResult.getMsg());
						log.error("try to delete ItemCatProps");
						ItemCatProps.deleteByCid(cid);
					}
				} else if (msg.contains("自定义属性是非销售属性")) {
					log.warn("first copyAction error,errorMsg:" + msg);
					tmResult.setMsg("当前类目下部分销售属性不支持自定义，请更换类目后重新复制.");
				} else if (msg.contains("宝贝所在的类目的不存在")) {
					// 删除当前失效类目
					log.warn("first copyAction error,errorMsg:" + msg);
					log.warn("try to delete itemcat");
					int startIndex = msg.indexOf("cid:");
					int endIndex=msg.indexOf("---numIid");
					String cidStr = msg.substring(startIndex + "cid:".length(),endIndex);

					cid = Long.parseLong(cidStr);
					ItemCatPlay.delByCid(cid);
				}
			}else {
				//保存类目映射关联关系
				long numIid=tmResult.getRes().getNumIid();
				long aliItemId=Long.parseLong(CopyUtil.parseItemId(url));
				log.info(String.format("save maping relation: alicid--%s,tbcid--%s,aliItemId--%s,tbItemId--%s",aliCid,cid,aliItemId,numIid));
				boolean result=AliCatMapping.formatObj(aliCid,cid, aliItemId, numIid)
				.rawInsert();
				log.info("~~~~~~~~~~~add mapping result:"+result);
			}

			return tmResult;
		} catch (Exception e) {
			e.printStackTrace();
			return new TMResult(false, "复制出错，返回结果为空。" + e.toString(), null);
		}

	}

	/**
	 * 解析URL获取item信息
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 *             获取SKU出现异常
	 */
//	public static ItemExt getItemFromAlibaba(String numiid, Integer sellerCat, Long dtId,
//                                             String oldTitle, String newTitle, Integer priceWay, Double priceVal,
//                                             Long cid, Integer addressWay, User user, Boolean agian,Long brand){
//
//		String url= CopyUtil.changeToUrl(numiid);
//		ToolBy1688 tool=new ToolBy1688(url);
//        if (tool.getItemInfo()==null) {
//            return null;// new TMResult("数据源获取异常");
//        }
//        
//      
//		ItemExt item = new ItemExt();
//        Date curDate = new Date();
//        item.setListTime(curDate);
//        item.setModified(curDate);
//        item.setHasShowcase(true);
//        item.setIsVirtual(false);
//        item.setStuffStatus("new");
//        item.setType("fixed");
//        item.setBrand(brand);
//        //价格
//		String price=tool.getMPrice().toString();
//		item.setNum(tool.getMNums());
//		log.info("get sku price："+price);
//		//是否有价格改变的值
//		if (!CommonUtil.isNullOrEmpty(priceVal)) {
//			//价格变更方式
//			if (priceWay==1) {
//				price=String.valueOf(Double.valueOf(price)+Double.valueOf(priceVal));
//			}else if (priceWay==2) {
//				price=String.valueOf(priceVal*Double.parseDouble(price));
//			}
//		}
//		//保证精度准确
//		if (price.substring(price.indexOf(".")+1).length()>2) {
//			price=price.substring(0,price.indexOf(".")+3);
//		}
//		item.setPrice(price);
//
//        //商家编码
//        item.setOuterId(tool.getUrl());
//        item.setNumIid(Long.parseLong(CopyUtil.parseItemId(tool.getUrl())));
//		//宝贝标题
//		String title=tool.getMTitle();
//		//是否替换标题关键字
//		if (!CommonUtil.isNullOrEmpty(oldTitle)&&newTitle!=null) {
//			title=title.replace(oldTitle, newTitle);
//		}
//		//过滤淘宝不被允许的关键字（批发、代理、招商、回收、置换、求购）
//		title=title.replaceAll("求购|批发|置换|回收|代理|招商|专供|代购|养颜|预售|中药|排毒|医用|点痣|祛痣|颈椎病|腰间盘突出|定制|武警|威乐星|治疗|宫颈糜烂|炎症|霉菌性阴道炎|盆腔炎", "");
//		item.setTitle(title);
//		//发货地址
//		Location location=new Location();
//		if (addressWay==1) {
//			//（原宝贝地址）
//			location=tool.getObjLocation();
//			//存在原宝贝无发货地址的情况下用卖家默认发货地址
//			if (location==null) {
//				//获取用户默认发货地址
//				location=new Location();
//				AddressResult result=ApiUtil.getDefAddress(user.sessionKey);
//				location.setState(result.getProvince().replaceAll("省|市|地区|行政区|自治州", ""));
//				location.setCity(result.getCity().replaceAll("省|市|地区|行政区|自治州", ""));
//			}
//		}else if (addressWay==2) {
//			//获取用户默认发货地址
//			AddressResult result=ApiUtil.getDefAddress(user.sessionKey);
//			location.setState(result.getProvince().replaceAll("省|市|地区|行政区|自治州", ""));
//			location.setCity(result.getCity().replaceAll("省|市|地区|行政区|自治州", ""));
//		}
//		item.setLocation(location);
//		
////		if (cid<=0) {
////			long aliCid = tool.getCatIdFor1688();
////				AliCatMapping mapping=AliCatMapping.getMappingByAliCid(aliCid);
////			if (mapping!=null) {
////				cid=mapping.getTbcid();
////			}else {
////				//无匹配记录则通过引擎匹配最优解
////				//bbn29
////				String pageTitle=tool.getPageTitle();
////				cid=matchCidByTitle(pageTitle);
////			}
////		}
//		item.setCid(cid);
//		//设置 状态 库存 上架
//		item.setApproveStatus("instock");
//		//设置SKU
//		String props="";
//		SkuDto skuDto=null;
//		try {
//			skuDto = tool.getMSkus(cid);
//			//如果二次请求则不复制SKU
//			if (!agian) {
//				List<Sku> skus=skuDto.getSkus();
////				item.setSkus(skus);
//				//去重
//				String inputCustomCpv=skuDto.getInputCustomCpvs();
//				
//				item.setInputCustomCpv(inputCustomCpv);
//				
//				String skuPrices=StringUtils.EMPTY;
//				String skuProperties=StringUtils.EMPTY;
//				String skuQuantities=StringUtils.EMPTY;
//				String skuOuterIds=StringUtils.EMPTY;
//			
//				//设置Sku图片
//				item.setPropImgs(skuDto.getPropImgs());
//				
////				if (!CommonUtil.isNullOrEmpty(skus)) {
////					for (int i=0;i< skus.size();i++) {
////						if (i==skus.size()-1) {
////							
////							props+=skus.get(i).getProperties();
////							continue;
////						}
////						props+=skus.get(i).getProperties()+";";
////						
////					}
////				}
//				
//				if (!CommonUtils.isEmpty(skus)) {
//					List<String> sku_properties = new ArrayList<String>();
//					List<Long> sku_quantities = new ArrayList<Long>();
//					List<String> sku_prices = new ArrayList<String>();
//					List<String> sku_outer_ids = new ArrayList<String>();
//					for (Sku sku : skus) {
//						if (!StringUtils.isEmpty(sku.getProperties())) {
//							sku_properties.add(sku.getProperties());
//							sku_quantities.add(sku.getQuantity());
//							if (StringUtils.isEmpty(sku.getPrice())) {
//								sku_prices.add(item.getPrice());
//							} else {
//								sku_prices.add(sku.getPrice());
//							}
//
//							sku_outer_ids.add(sku.getOuterId());
//						}
//						props+=sku.getProperties()+";";
//					}
//					skuProperties = StringUtils.join(sku_properties, ",");
//					skuQuantities = StringUtils.join(sku_quantities, ",");
//					skuPrices = StringUtils.join(sku_prices, ",");
//					skuOuterIds = StringUtils.join(sku_outer_ids, ",");
//
//				}
//
//				
//				item.setSkuPrices(skuPrices);
//				item.setSkuQuantities(skuQuantities);
//				item.setSkuProperties(skuProperties);
//				item.setSkuOuterIds(skuOuterIds);
//				 // sku价格
//		        List<PriceUnit[]> list = new ArrayList<PriceUnit[]>();
//		        if (StringUtils.isNotEmpty(skuPrices)) {
//					String[] pricesStrings = skuPrices.split(",");
//					for (String p : pricesStrings) {
//						PriceUnit[] priceUnits = new PriceUnit[1];
//						priceUnits[0] = new PriceUnit().setPrice(p).setDisplay(p);
//						list.add(priceUnits);
//					}
//				}
//		        item.setPriceUnitsForSkuPrice(list);
//					
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		//折扣价与价格
//		  PriceUnit[] priceUnitsForPrice =tool.getDiscountPrice();
//          item.setPriceUnitsForPrice(priceUnitsForPrice);
//		
//		
//		
//		//获取类目属性
//		Map<String, String> propMap=tool.getMProps();
//		
//		item.setPropsName(propMap.get("货号"));
//		
//		PropDto dto=tool.getPropsByCid(cid,propMap);
//		
//		//设置类目属性
////		PropDto dto=tool.getPropsByCid(itemCat);
//		if (dto!=null) {
//			if (!CommonUtil.isNullOrEmpty(dto.getProps())) {
//				props+=";"+dto.getProps();
//			}
//			if (props!=null) {
//				item.setProps(props);
//			}
//			item.setInputPids(dto.getInputPids());
//			item.setInputStr(dto.getInputStr());
//		}
//		
//		if (dtId!=0) {
//			item.setPostageId(dtId);
//		}
//		if (sellerCat!=0) {
//			item.setSellerCids(sellerCat.toString());
//		}
//		item.setFreightPayer("seller");
//		item.setValidThru(14L);
//		item.setHasInvoice(true);
//		item.setHasWarranty(false);
//		item.setHasShowcase(false);
//		item.setHasDiscount(false);
//		//商品主图地址
//		List<String> imgUrls=tool.getMImgUrl();
//		item.setPicUrl(tool.getPicPath());
//		//商品次图
//		List<ItemImg> itemImgs=new ArrayList<ItemImg>();
//		//获取所有商品次图
//		
//		if (!CommonUtil.isNullOrSizeZero(imgUrls)) {
//			//由于淘宝商品图片只能5张，在此作限制
//			int size=imgUrls.size()>=5?5:imgUrls.size();
//			for (int i = 0; i <size; i++) {
//				ItemImg img=new ItemImg();
//				img.setPosition((long) (i+1));
//				img.setUrl(imgUrls.get(i));
//				itemImgs.add(img);
//			}
//		}
//		
//		  if (user.isTmall()) {
//	        	try {
//					SalePropDto salePropDto=tool.getMSaleProp();
//					item.setSalePropDto(salePropDto);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//	        	
//			}
//		
//		item.setItemImgs(itemImgs);
//		//商品描述信息
//		//部分商品返回的数据值信息不一致
//		item.setDesc(tool.getMDesc().replace("src=\"//", "src=\"https://"));
//		//item.setDesc("xxxxxxxxxxxxxxxxx");
//		return item;
//	}
	
	/**
	 * 解析URL获取item信息
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 *             获取SKU出现异常
	 */
	public static ItemExt getItemFromAlibaba(String numiid,Long cid,User user, Boolean agian,Long brand){

		String url= CopyUtil.changeToUrl(numiid);
		ToolBy1688 tool=new ToolBy1688(url);
        if (tool.getItemInfo()==null) {
            return null;// new TMResult("数据源获取异常");
        }
        // 宝贝复制自定义设置
        ItemCarryCustomFor1688 itemCarryCustom = getItemCarryCustom(null, user);
      
		ItemExt item = new ItemExt();
        Date curDate = new Date();
        item.setListTime(curDate);
        item.setModified(curDate);
        item.setHasShowcase(true);
        item.setIsVirtual(false);
        item.setStuffStatus("new");
        item.setType("fixed");
        item.setBrand(brand);
        //价格
		String price=tool.getMPrice().toString();
		item.setNum(tool.getMNums());
		log.info("get sku price："+price);
		//是否有价格改变的值
		
		if (itemCarryCustom!=null&&itemCarryCustom.getMultiplyNum()!=null) {
			Double priceD =tool.getMPrice()*itemCarryCustom.getMultiplyNum();
			if (itemCarryCustom.getAddNum()!=null) {
				priceD=priceD+itemCarryCustom.getAddNum();
			}
			price=String.valueOf(priceD);
		}
		
		//保证精度准确
		if (price.substring(price.indexOf(".")+1).length()>2) {
			price=price.substring(0,price.indexOf(".")+3);
		}
		
		
		if (itemCarryCustom!=null&&itemCarryCustom.getPriceTail()!=null) {
			//取小数部分
			int pointIndex=price.indexOf(".");
			String pointPart=price.substring(pointIndex+1);
			//整数部分
			String integerPrice=price.substring(0,pointIndex);
			//整数是否为一位
			if (integerPrice.length()==1) {
				price=itemCarryCustom.getPriceTail().toString()+"."+pointPart;
			}else {
				price=price.substring(0, pointIndex-1)+itemCarryCustom.getPriceTail()+"."+pointPart;
			}
//			iRntegerPrice=integerPrice
		}
		item.setPrice(price);
        //商家编码
        item.setOuterId(tool.getUrl());
        item.setNumIid(Long.parseLong(CopyUtil.parseItemId(tool.getUrl())));
		//宝贝标题
		String title=tool.getMTitle();
		//是否替换标题关键字 增加前缀
		if (itemCarryCustom!=null) {
			String titleKeywordMapper=itemCarryCustom.getTitleKeywordMapper()==null?"{}":itemCarryCustom.getTitleKeywordMapper();
			
			if (titleKeywordMapper.equals("{}")==false&&titleKeywordMapper.equals("a")==false) {
				Map<String, String> titleKeywordMap = (Map<String, String>) JSON
						.parse(titleKeywordMapper);
				for (Entry<String, String> keyword : titleKeywordMap.entrySet()) {
					title=title.replace(keyword.getKey(), keyword.getValue());
				}
			}
			//计算前后缀长度，避免超出
			int fixLength=itemCarryCustom.getPrefixTitleString().length()+itemCarryCustom.getSuffixTitleString().length();
			
			int canUseLength=30-fixLength;
			if (title.length()>canUseLength) {
				title=title.substring(0, canUseLength);
			}
			//新增前后缀
			if(StringUtils.isEmpty(itemCarryCustom.getPrefixTitleString())==false){
				title=itemCarryCustom.getPrefixTitleString()+title;
			}
			if(StringUtils.isEmpty(itemCarryCustom.getSuffixTitleString())==false){
				title=title+itemCarryCustom.getSuffixTitleString();
			}
		}
		
		//过滤淘宝不被允许的关键字（批发、代理、招商、回收、置换、求购）
		title=title.replaceAll("求购|批发|置换|回收|代理|招商|专供|代购|养颜|预售|中药|排毒|医用|点痣|祛痣|颈椎病|腰间盘突出|定制|武警|威乐星|治疗|宫颈糜烂|炎症|霉菌性阴道炎|盆腔炎", "")
				.replaceAll(LimitWord.getWholeLimitStr(),"");
		
		item.setTitle(title);
		//发货地址
		Location location=new Location();
		if (itemCarryCustom==null||itemCarryCustom.getPostAddress()==PostAddressType.DefAddress) {
			AddressResult result=ApiUtil.getDefAddress(user.sessionKey);
			location.setState(result.getProvince().replaceAll("省|市|地区|行政区|自治州", ""));
			location.setCity(result.getCity().replaceAll("省|市|地区|行政区|自治州", ""));
		}else {
			if (itemCarryCustom.getPostAddress()==PostAddressType.SourceAddress) {
				//（原宝贝地址）
				location=tool.getObjLocation();
				//存在原宝贝无发货地址的情况下用卖家默认发货地址
				if (location==null) {
					//获取用户默认发货地址
					location=new Location();
					AddressResult result=ApiUtil.getDefAddress(user.sessionKey);
					location.setState(result.getProvince().replaceAll("省|市|地区|行政区|自治州", ""));
					location.setCity(result.getCity().replaceAll("省|市|地区|行政区|自治州", ""));
				}
			}
		}
		
		item.setLocation(location);
		item.setCid(cid);
		//设置 状态 库存 上架
//		item.setApproveStatus("instock");、
		String status=itemCarryCustom==null?"instock":itemCarryCustom.getApproveStatus().name();
		item.setApproveStatus(status);
		//设置SKU
		String props="";
		SkuDto skuDto=null;
		try {
			skuDto = tool.getMSkus(cid);
			//如果二次请求则不复制SKU
			if (!agian) {
				List<Sku> skus=skuDto.getSkus();
//				item.setSkus(skus);
				//去重
				String inputCustomCpv=skuDto.getInputCustomCpvs();
				
				item.setInputCustomCpv(inputCustomCpv);
				
				String skuPrices=StringUtils.EMPTY;
				String skuProperties=StringUtils.EMPTY;
				String skuQuantities=StringUtils.EMPTY;
				String skuOuterIds=StringUtils.EMPTY;
			
				//设置Sku图片
				item.setPropImgs(skuDto.getPropImgs());
				
				if (!CommonUtils.isEmpty(skus)) {
					List<String> sku_properties = new ArrayList<String>();
					List<Long> sku_quantities = new ArrayList<Long>();
					List<String> sku_prices = new ArrayList<String>();
					List<String> sku_outer_ids = new ArrayList<String>();
					for (Sku sku : skus) {
						if (!StringUtils.isEmpty(sku.getProperties())) {
							sku_properties.add(sku.getProperties());
							sku_quantities.add(sku.getQuantity());
							if (StringUtils.isEmpty(sku.getPrice())) {
								sku_prices.add(item.getPrice());
							} else {
								sku_prices.add(sku.getPrice());
							}

							sku_outer_ids.add(sku.getOuterId());
						}
						props+=sku.getProperties()+";";
					}
					skuProperties = StringUtils.join(sku_properties, ",");
					skuQuantities = StringUtils.join(sku_quantities, ",");
//					skuPrices = StringUtils.join(sku_prices, ",");
					skuPrices=changeSkuPrice(sku_prices,itemCarryCustom);
					skuOuterIds = StringUtils.join(sku_outer_ids, ",");

				}

				
				item.setSkuPrices(skuPrices);
				item.setSkuQuantities(skuQuantities);
				item.setSkuProperties(skuProperties);
				item.setSkuOuterIds(skuOuterIds);
				 // sku价格
		        List<PriceUnit[]> list = new ArrayList<PriceUnit[]>();
		        if (StringUtils.isNotEmpty(skuPrices)) {
					String[] pricesStrings = skuPrices.split(",");
					for (String p : pricesStrings) {
						PriceUnit[] priceUnits = new PriceUnit[1];
						priceUnits[0] = new PriceUnit().setPrice(p).setDisplay(p);
						list.add(priceUnits);
					}
				}
		        item.setPriceUnitsForSkuPrice(list);
					
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//折扣价与价格  itemCarryCustom==null?"":itemCarryCustom.getPriceTail().toString()
		  PriceUnit[] priceUnitsForPrice =tool.getDiscountPrice();
          item.setPriceUnitsForPrice(priceUnitsForPrice);
		
		
		
		//获取类目属性
		Map<String, String> propMap=tool.getMProps();
		
		item.setPropsName(propMap.get("货号"));
		
		PropDto dto=tool.getPropsByCid(cid,propMap);
		
		//设置类目属性
//		PropDto dto=tool.getPropsByCid(itemCat);
		if (dto!=null) {
			if (!CommonUtil.isNullOrEmpty(dto.getProps())) {
				props+=";"+dto.getProps();
			}
			if (props!=null) {
				item.setProps(props);
			}
			item.setInputPids(dto.getInputPids());
			item.setInputStr(dto.getInputStr());
		}
		
		if (itemCarryCustom!=null&&itemCarryCustom.getPostageId()!=null) {
			item.setPostageId(itemCarryCustom.getPostageId());
		}
		if (itemCarryCustom!=null&&itemCarryCustom.getSellerCids()!=null) {
			item.setSellerCids(itemCarryCustom.getSellerCids());
		}
		item.setFreightPayer("seller");
		item.setValidThru(14L);
		item.setHasInvoice(true);
		item.setHasWarranty(false);
		item.setHasShowcase(false);
		item.setHasDiscount(false);
		//商品主图地址
		List<String> imgUrls=tool.getMImgUrl();
//		item.setPicUrl(tool.getPicPath());
		long mainPicIndex=itemCarryCustom==null?0L:itemCarryCustom.getMainPicIndex();
		if (mainPicIndex==4) {
			//随机
			mainPicIndex=new Random().nextInt(imgUrls.size());
		}
		if (mainPicIndex>imgUrls.size()) {
			mainPicIndex=0;
		}
		item.setPicUrl(imgUrls.get((int) mainPicIndex));
		//商品次图
		List<ItemImg> itemImgs=new ArrayList<ItemImg>();
		//获取所有商品次图
		if (!CommonUtil.isNullOrSizeZero(imgUrls)) {
			//由于淘宝商品图片只能5张，在此作限制
			int size=imgUrls.size()>=5?5:imgUrls.size();
			for (int i = 0; i <size; i++) {
				ItemImg img=new ItemImg();
				img.setPosition((long) (i+1));
				img.setUrl(imgUrls.get(i));
				itemImgs.add(img);
			}
		}
		  if (user.isTmall()) {
	        	try {
					SalePropDto salePropDto=tool.getMSaleProp();
					item.setSalePropDto(salePropDto);
				} catch (IOException e) {
					e.printStackTrace();
				}
	        	
			}
		
		item.setItemImgs(itemImgs);
		//商品描述信息
		//部分商品返回的数据值信息不一致
		item.setDesc(tool.getMDesc().replace("src=\"//", "src=\"https://"));
		//item.setDesc("xxxxxxxxxxxxxxxxx");
		return item;
	}
	
	public static ItemCarryCustomFor1688 getItemCarryCustom(Long subId, User user) {
		ItemCarryCustomFor1688 itemCarryCustom;
        if (subId != null) {
            SubCarrierTask subCarrierTask = SubCarrierTask.findById(subId);
            if (subCarrierTask == null) return null;
            Long taskId = subCarrierTask.getTaskId();
            if (taskId == 0) return null;
            CarrierTask carrierTask = CarrierTask.findByTaskId(taskId);
            if (carrierTask == null) return null;
            long itemCarryCustomId = carrierTask.getItemCarryCustomId();
            if (itemCarryCustomId <= 0L) return null;
            itemCarryCustom = ItemCarryCustomFor1688.findById(carrierTask.getItemCarryCustomId());
        } else {
            itemCarryCustom = ItemCarryCustomFor1688.findByUserId(user.getIdlong());
        }

        return itemCarryCustom;
    }

	public static Long matchCidByTitle(String key) {
		Long cid = null;
		try {
			// String titleType=pageTitle.substring(0,pageTitle.indexOf("_"));
			// bbn29
			String urlPath = "http://115.29.162.138:9090/api/ItemCat/search";
			String result = CommonUtil.sendPost(urlPath, "itemTitle=" + key);
			log.info(result);
			JSONObject resultObject = new JSONObject(result);

			boolean success = resultObject.getBoolean("success");
			if (success) {
				String msg = resultObject.getString("msg");
				cid = new JSONArray(msg).getJSONObject(0).getLong("cid");
			} else {
				log.error("匹配类目信息失败：key:" + key);
				return null;
			}

		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			return null;
		}
		return cid;
	}

	public static TMResult uploadImg(File file,User user) {
		TMResult result=new TMResult();
		PicApi api = PicApi.get();
		Picture pic = api.uploadPcClientPic(user, file.getName(), file, api.ensureTMCat(user));
		if (pic == null) {
			result.isOk=false;
			
		}else {
			result.isOk=true;
			result.msg=pic.getPicturePath();
		}
		return result;
	}

	
	public static String changeSkuPrice(List<String> sku_prices,ItemCarryCustomFor1688 itemCarryCustom){
		StringBuffer priceSb=new StringBuffer();
		
		for (String price : sku_prices) {
			if (itemCarryCustom!=null&&itemCarryCustom.getMultiplyNum()!=null) {
				Double priceD =Double.valueOf(price)*itemCarryCustom.getMultiplyNum();
				if (itemCarryCustom.getAddNum()!=null) {
					priceD=priceD+itemCarryCustom.getAddNum();
				}
				price=String.valueOf(priceD);
			}
			
			//保证精度准确
			if (price.substring(price.indexOf(".")+1).length()>2) {
				price=price.substring(0,price.indexOf(".")+3);
			}
			
			
			if (itemCarryCustom!=null&&itemCarryCustom.getPriceTail()!=null) {
				//取小数部分
				int pointIndex=price.indexOf(".");
				String pointPart=price.substring(pointIndex+1);
				//整数部分
				String integerPrice=price.substring(0,pointIndex);
				//整数是否为一位
				if (integerPrice.length()==1) {
					price=itemCarryCustom.getPriceTail().toString()+"."+pointPart;
				}else {
					price=price.substring(0, pointIndex-1)+itemCarryCustom.getPriceTail()+"."+pointPart;
				}
			}
			
			priceSb.append(price+",");
		}
		
		
		return priceSb.toString().substring(0,priceSb.length()-1);
		
	}
//	/**
//	 * 处理1688到淘宝的复制任务
//	 */
//	public static TMResult<Item> doCopy(String numiid,)
}
