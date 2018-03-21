package actions.alibaba;

import actions.carriertask.CarrierTaskAction;
import bustbapi.SchemaApi.*;
import carrier.FileCarryUtils;

import com.ciaosir.client.CommonUtils;
import com.taobao.api.domain.Brand;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.ItemImg;
import com.taobao.top.schema.exception.TopSchemaException;
import com.taobao.top.schema.factory.SchemaReader;
import com.taobao.top.schema.factory.SchemaWriter;
import com.taobao.top.schema.field.*;
import com.taobao.top.schema.option.Option;
import com.taobao.top.schema.rule.Rule;
import com.taobao.top.schema.value.ComplexValue;
import com.taobao.top.schema.value.Value;

import configs.Subscribe;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.carrierTask.CarrierLimitForDQ;
import models.carrierTask.ItemCarryCustomFor1688;
import models.carrierTask.SubCarrierTask;
import models.carrierTask.SubCarrierTask.SubTaskType;
import models.itemCopy.ItemExt;
import models.itemCopy.dto.SalePropDto;
import models.itemCopy.dto.SalePropModel;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import result.TMResult;
import utils.*;
import utils.oyster.DateUtil;

public class CopyToTmallAction {

	public static final Logger log = LoggerFactory
			.getLogger(CopyToTmallAction.class);

	// public static TMResult<Item> doCopy(String numiid, Integer sellerCat,
	// Long dtId, String oldTitle, String newTitle, Integer priceWay,
	// Double priceVal, Long cid, Integer addressWay, User user,
	// boolean b, Long brand) {
	//
	// ItemExt item = ItemCopyAction.getItemFromAlibaba(numiid, cid, user,
	// false,brand);
	//
	// return doCopyItemToTmall(item, user,SubTaskType.$1688复制);
	//
	// }

	public static TMResult<Item> doCopy(String numiid, Long cid, User user,
			boolean b, Long brand) {
		ItemExt item = ItemCopyAction.getItemFromAlibaba(numiid, cid, user,
				false, brand);

		return doCopyItemToTmall(item, user, SubTaskType.$1688复制);

	}

	/**
	 * 复制宝贝到天猫
	 * 
	 * @param brand
	 */
	// public static TMResult<Item> doCopyItemToTmall(String numiid, Integer
	// sellerCat,
	// Long dtId, String oldTitle, String newTitle, Integer priceWay,
	// Double priceVal, Long itemCat, Integer addressWay, User user,
	// boolean b, Long brand) {
	// String url = CopyUtil.changeToUrl(numiid);
	// ToolBy1688 tool=new ToolBy1688(url);
	// // 调用tmall.product.match.schema.get接口获取产品匹配的规则
	// tmallProductMatchSchemaGet pmsg = new tmallProductMatchSchemaGet(
	// user.getSessionKey(), itemCat);
	// String productXmlString = pmsg.call();
	//
	// TMResult<Item> rtnResult=new TMResult<Item>();
	// rtnResult.isOk=false;
	// if (StringUtils.isEmpty(productXmlString)) {
	// rtnResult.setMsg(pmsg.errorMsg);
	// // rtnResult.setOk(false);
	// return rtnResult;
	// }
	// log.info("productXmlStirng:" + productXmlString);
	// // 获取宝贝信息
	// ItemExt sourcesItem = ItemCopyAction.getItemFromAlibaba(numiid,
	// sellerCat,
	// dtId, oldTitle, newTitle, priceWay, priceVal, itemCat,
	// addressWay, user, false);
	// // 默认选择用户品牌
	// if (brand==null||brand<=0) {
	// List<Brand> brands=ApiUtil.getAuthorizeBrands(user.getSessionKey());
	// brand=brands.get(0).getVid();
	// if (brand==null||brand<=0) {
	// rtnResult.setMsg("用户无天猫授权品牌。");
	// return rtnResult;
	// }
	// }
	// // 根据规则生成产品匹配xml
	// List<Field> newProductFieldList = getNewFieldList(user,productXmlString,
	// sourcesItem,null, brand);
	// String propvalues = StringUtils.EMPTY;
	// try {
	// propvalues = SchemaWriter.writeParamXmlString(newProductFieldList);
	// if ("<itemParam/>".equalsIgnoreCase(propvalues)) {
	// log.error("生成产品匹配xml异常");
	// rtnResult.setMsg("生成产品匹配xml异常");
	// return rtnResult;
	// }
	// } catch (TopSchemaException e) {
	// e.printStackTrace();
	// }
	// //生成的product_xml
	// log.info("gen_product_xml:"+propvalues);
	//
	// SalePropDto salePropDto=null;
	// try {
	// salePropDto = tool.getMSaleProp();
	// } catch (IOException e1) {
	// log.error("get salePropDto error from url:"+url);
	// e1.printStackTrace();
	// }
	//
	// // 调用tmall.product.schema.match进行产品匹配（返回product_id）
	// tmallProductSchemaMatch psm = new tmallProductSchemaMatch(
	// user.getSessionKey(), itemCat, propvalues);
	// String productIdStr = psm.call();
	// if (StringUtils.isEmpty(productIdStr)) {
	// if (StringUtils.isEmpty(psm.errorMsg)) {
	// // 新建对应的产品
	// // 调用tmall.product.add.schema.get接口获取产品发布涉及的规则
	// tmallProductAddSchemaGet pas = new tmallProductAddSchemaGet(
	// user.getSessionKey(), itemCat, brand);
	// String productAddString = pas.call();
	// if (StringUtils.isEmpty(productAddString)) {
	// rtnResult.setMsg(pmsg.getSubErrorMsg());
	// return rtnResult;
	// }
	// log.info("productXml:" + productAddString);
	// // 根据规则生成产品发布xml
	// List<Field> productAddFieldList = getNewFieldList(user,
	// productAddString, sourcesItem,salePropDto, brand);
	// String xmlData = StringUtils.EMPTY;
	// try {
	// xmlData = SchemaWriter
	// .writeParamXmlString(productAddFieldList);
	// if ("<itemParam/>".equalsIgnoreCase(xmlData)) {
	// log.error("生成产品发布xml异常");
	// rtnResult.setMsg("生成产品发布xml异常");
	// return rtnResult;
	// }
	// } catch (TopSchemaException e) {
	// e.printStackTrace();
	// }
	//
	// log.info("productAdd:"+xmlData);
	// // 调用tmall.product.schema.add发布产品
	// tmallProductSchemaAdd psa = new tmallProductSchemaAdd(
	// user.getSessionKey(), itemCat, brand, xmlData);
	// productIdStr = psa.call();
	// if (StringUtils.isEmpty(productIdStr)) {
	// rtnResult.setMsg("您可能没有发布该类目或品牌产品的权限");
	// return rtnResult;
	// }
	// // 重新调用tmall.product.schema.match进行产品匹配（返回product_id）
	// productIdStr
	// =Jsoup.parse(productIdStr).getElementById("product_id").text();
	// } else {
	// rtnResult.setMsg("您可能没有发布该类目或品牌产品的权限"+psm.getErrorMsg());
	// return rtnResult;
	// }
	// }
	// Long productId = Long.parseLong(productIdStr);
	// log.info("产品匹配的productId:" + productId
	// + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	//
	// // 调用tmall.item.add.schema.get获取商品发布的规则
	// tmallItemAddSchemaGet iasg = new tmallItemAddSchemaGet(
	// user.getSessionKey(), itemCat, productId);
	// String itemXmlString = iasg.call();
	// if (StringUtils.isEmpty(itemXmlString)) {
	// rtnResult.setMsg(iasg.errorMsg);
	// return rtnResult;
	// }
	// log.info("itemXmlStirng:" + itemXmlString);
	// if ("oyster".equals(Play.id)) {
	// FileUtil.saveFile("C:\\Users\\kohler\\Documents\\HBuilderProject\\Test\\ItemXml.xml",
	// itemXmlString);
	// }
	// // 根据规则生成商品上新xml
	// List<Field> newItemFieldList = getNewFieldList(user,itemXmlString,
	// sourcesItem,salePropDto, brand);
	// String addXml = StringUtils.EMPTY;
	// try {
	// addXml = SchemaWriter.writeParamXmlString(newItemFieldList);
	// if (StringUtils.isEmpty(addXml)) {
	// log.error("生成商品上新xml异常");
	// rtnResult.setMsg("生成商品上新xml异常");
	// return rtnResult;
	// }
	// } catch (TopSchemaException e) {
	// e.printStackTrace();
	// }
	// log.info("addXml:" + addXml);
	// if ("oyster".equals(Play.id)) {
	// FileUtil.saveFile("C:\\Users\\kohler\\Documents\\HBuilderProject\\Test\\addXml.xml",
	// addXml);
	// }
	//
	// // 调用tmall.item.schema.add进行商品上新
	// tmallItemSchemaAdd isa = new tmallItemSchemaAdd(user.getSessionKey(),
	// itemCat, productId, addXml);
	// String resule = isa.call();
	// if (StringUtils.isEmpty(resule)) {
	// rtnResult.setMsg(isa.errorMsg);
	// // rtnResult.isOk=false;
	// return rtnResult;
	// }
	// log.info("商品上新的add_item_result:" + resule);
	// String itemUrl = "https://detail.tmall.com/item.htm?id=" + resule;
	// log.warn("success copy from [url: " + url + "] -> to ["
	// + user.getUserNick() + "] id: " + resule);
	// // rtnResult.setMsg("<b></br>拷贝失败，请联系客服处理！</b><br>错误原因："+isa.errorMsg);
	// // rtnResult.setMsg(itemUrl);
	// rtnResult.isOk=true;
	// //封装返回参数 （主图，标题，宝贝id）
	// Item returnItem=new Item();
	// returnItem.setNumIid(Long.parseLong(resule));
	// returnItem.setPicUrl(sourcesItem.getPicUrl());
	// returnItem.setTitle(sourcesItem.getTitle());
	// rtnResult.setRes(returnItem);
	//
	// if (rtnResult.isOk()) {
	// SubCarrierTask.recordSuccess(url, "~", user.getUserNick(),itemUrl,
	// SubCarrierTask.SubTaskType.$1688复制);
	// // 用户复制次数限制
	// if(user.getVersion() == Subscribe.Version.LL) {
	// CarrierLimitForDQ.updateUseCountByUserId(user.getId());
	// }
	// }else {
	// SubCarrierTask.recordError(url, user.getUserNick(), rtnResult.getMsg(),
	// SubCarrierTask.SubTaskType.$1688复制);
	// }
	// return rtnResult;
	// // return rtnResult; new TMResult(true,
	// // "拷贝成功，请在仓库中查看！新宝贝地址：<br><a href='" + itemUrl
	// // + "' target='_blank'>" + itemUrl
	// // + "</a><br/><br/><a style='color:blue' href='"
	// // + updateUrl + "' target='_blank'>编辑宝贝</a>", null);
	// }

	/**
	 * 复制宝贝到天猫
	 * 
	 * @param item
	 *            额外包含brand,SalePropItemDto
	 * @param user
	 * @return
	 */
	public static TMResult<Item> doCopyItemToTmall(ItemExt item, User user,
			SubTaskType type) {
		// 调用tmall.product.match.schema.get接口获取产品匹配的规则
		tmallProductMatchSchemaGet pmsg = new tmallProductMatchSchemaGet(
				user.getSessionKey(), item.getCid());
		String productXmlString = pmsg.call();
		TMResult<Item> rtnResult = new TMResult<Item>();
		rtnResult.isOk = false;
		if (StringUtils.isEmpty(productXmlString)) {
			rtnResult.setMsg(pmsg.errorMsg);
			return rtnResult;
		}

		log.info("productXmlStirng:" + productXmlString);

		Long brand = item.getBrand();
		// 默认选择用户品牌
		if (brand == null || brand <= 0) {
			List<Brand> brands = ApiUtil.getAuthorizeBrands(user
					.getSessionKey());
			brand = brands.get(0).getVid();
			if (brand == null || brand <= 0) {
				rtnResult.setMsg("用户无天猫授权品牌。");
				return rtnResult;
			}
		}
		// 根据规则生成产品匹配xml
		List<Field> newProductFieldList = getNewFieldList(user,
				productXmlString, item, null, brand);
		String propvalues = StringUtils.EMPTY;
		try {
			propvalues = SchemaWriter.writeParamXmlString(newProductFieldList);
			if ("<itemParam/>".equalsIgnoreCase(propvalues)) {
				log.error("生成产品匹配xml异常");
				rtnResult.setMsg("生成产品匹配xml异常");
				return rtnResult;
			}
		} catch (TopSchemaException e) {
			e.printStackTrace();
		}
		// 生成的product_xml
		log.info("gen_product_xml:" + propvalues);

		// 调用tmall.product.schema.match进行产品匹配（返回product_id）
		tmallProductSchemaMatch psm = new tmallProductSchemaMatch(
				user.getSessionKey(), item.getCid(), propvalues);
		String productIdStr = psm.call();
		// &&(item.getItemImgs()!=null&&!item.getItemImgs().isEmpty())
		// &&StringUtils.isEmpty(item.getPropertyAlias())
		if (item.getSalePropDto() == null) {
			List<SalePropModel> colors = new ArrayList<SalePropModel>();
			List<SalePropModel> others = new ArrayList<SalePropModel>();
			// 分割属性串
			String skuPropStr = item.getPropertyAlias();
			String[] skuProps = skuPropStr.split(";");
			// 区分颜色属性和其他销售属性
			int colorSize = item.getPropImgs().size();
			for (int i = 0; i < skuProps.length; i++) {
				SalePropModel spm = new SalePropModel();
				String props[] = skuProps[i].split(":");
				String aliasName = props[2];
				String id = props[0];
				String value = props[1];
				spm.setAliasName(aliasName);
				spm.setValue(value);
				spm.setId(id);
				if (i < colorSize) {
					spm.setImgUrl(item.getPropImgs().get(i).getUrl());
					colors.add(spm);
				} else {
					others.add(spm);
				}
			}
			item.setSalePropDto(new SalePropDto(colors, others));

		}

		SalePropDto salePropDto = item.getSalePropDto();
		if (StringUtils.isEmpty(productIdStr)) {
			if (StringUtils.isEmpty(psm.errorMsg)) {
				// 新建对应的产品
				// 调用tmall.product.add.schema.get接口获取产品发布涉及的规则
				tmallProductAddSchemaGet pas = new tmallProductAddSchemaGet(
						user.getSessionKey(), item.getCid(), brand);
				String productAddString = pas.call();
				if (StringUtils.isEmpty(productAddString)) {
					rtnResult.setMsg(pmsg.getSubErrorMsg());
					return rtnResult;
				}
				log.info("productXml:" + productAddString);
				// 根据规则生成产品发布xml
				List<Field> productAddFieldList = getNewFieldList(user,
						productAddString, item, salePropDto, brand);
				String xmlData = StringUtils.EMPTY;
				try {
					xmlData = SchemaWriter
							.writeParamXmlString(productAddFieldList);
					if ("<itemParam/>".equalsIgnoreCase(xmlData)) {
						log.error("生成产品发布xml异常");
						rtnResult.setMsg("生成产品发布xml异常");
						return rtnResult;
					}
				} catch (TopSchemaException e) {
					e.printStackTrace();
				}

				log.info("productAdd:" + xmlData);
				// 调用tmall.product.schema.add发布产品
				tmallProductSchemaAdd psa = new tmallProductSchemaAdd(
						user.getSessionKey(), item.getCid(), brand, xmlData);
				productIdStr = psa.call();
				if (StringUtils.isEmpty(productIdStr)) {
					rtnResult.setMsg("您可能没有发布该类目或品牌产品的权限");
					return rtnResult;
				}
				// 重新调用tmall.product.schema.match进行产品匹配（返回product_id）
				productIdStr = Jsoup.parse(productIdStr)
						.getElementById("product_id").text();
			} else {
				rtnResult.setMsg("您可能没有发布该类目或品牌产品的权限" + psm.getErrorMsg());
				return rtnResult;
			}
		}
		Long productId = Long.parseLong(productIdStr);
		log.info("产品匹配的productId:" + productId
				+ "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

		// 调用tmall.item.add.schema.get获取商品发布的规则
		tmallItemAddSchemaGet iasg = new tmallItemAddSchemaGet(
				user.getSessionKey(), item.getCid(), productId);
		String itemXmlString = iasg.call();
		if (StringUtils.isEmpty(itemXmlString)) {
			rtnResult.setMsg(iasg.errorMsg);
			return rtnResult;
		}
		log.info("itemXmlStirng:" + itemXmlString);
		if ("oyster".equals(Play.id)) {
			FileUtil.saveFile(
					"C:\\Users\\kohler\\Documents\\HBuilderProject\\Test\\ItemXml.xml",
					itemXmlString);
		}
		// 根据规则生成商品上新xml
		List<Field> newItemFieldList = getNewFieldList(user, itemXmlString,
				item, salePropDto, brand);
		String addXml = StringUtils.EMPTY;
		try {
			addXml = SchemaWriter.writeParamXmlString(newItemFieldList);
			if (StringUtils.isEmpty(addXml)) {
				log.error("生成商品上新xml异常");
				rtnResult.setMsg("生成商品上新xml异常");
				return rtnResult;
			}
		} catch (TopSchemaException e) {
			e.printStackTrace();
		}
		log.info("addXml:" + addXml);
		if ("oyster".equals(Play.id)) {
			FileUtil.saveFile(
					"C:\\Users\\kohler\\Documents\\HBuilderProject\\Test\\addXml.xml",
					addXml);
		}

		// 调用tmall.item.schema.add进行商品上新
		tmallItemSchemaAdd isa = new tmallItemSchemaAdd(user.getSessionKey(),
				item.getCid(), productId, addXml);
		String resule = isa.call();
		if (StringUtils.isEmpty(resule)) {
			rtnResult.setMsg(isa.errorMsg);
			return rtnResult;
		}
		log.info("商品上新的add_item_result:" + resule);
		String itemUrl = "https://detail.tmall.com/item.htm?id=" + resule;
		log.warn("success copy from [url: " + item.getNumIid() + "] -> to ["
				+ user.getUserNick() + "] id: " + resule);
		// 更新商家编码
		String outerId = "1003-a-" + item.getNumIid() + "-"
				+ item.getPropsName();
		boolean up = ApiUtil.updateOuterId(user.getSessionKey(),
				Long.valueOf(resule), outerId);
		if (up == false) {
			log.error("更新商家编码失败");
		}
		rtnResult.isOk = true;
		// 封装返回参数 （主图，标题，宝贝id）
		Item returnItem = new Item();
		returnItem.setNumIid(Long.parseLong(resule));
		returnItem.setPicUrl(item.getPicUrl());
		returnItem.setTitle(item.getTitle());
		rtnResult.setRes(returnItem);
		String recordUrl = "http://detail.1688.com/offer/" + item.getNumIid()
				+ ".html";
		if (type == SubCarrierTask.SubTaskType.天猫复制) {
			recordUrl = "https://detail.tmall.com/item.htm?id="
					+ item.getNumIid();
		}
		if (rtnResult.isOk()) {
			SubCarrierTask.recordSuccess(recordUrl, "~", user.getUserNick(),
					itemUrl, type);
			// 用户复制次数限制
			if (user.getVersion() == Subscribe.Version.LL) {
				CarrierLimitForDQ.updateUseCountByUserId(user.getId());
			}
		} else {
			SubCarrierTask.recordError(recordUrl, user.getUserNick(),
					rtnResult.getMsg(), type);
		}
		return rtnResult;
	}

	/**
	 * 复制宝贝到天猫
	 * 
	 */
	public static TMResult<Item> doCopyItemToTmall(String itemId, Long cid,
			User user) {
		// 转换Url
		// String url = CopyUtil.changeToUrl(itemId);String numiid, Long cid,
		// User user, boolean b, Long brand
		return doCopy(itemId, cid, user, false, null);
	}

	/**
	 * 复制宝贝到天猫
	 * 
	 */
	public static TMResult<Item> doCopyToTmall(String itemId, Long cid,
			User user) {
		// 转换Url
		return doCopy(itemId, cid, user, false, null);// doCopy(itemId, 0, 0L,
														// null,null, null,
														// null, cid, 1, user,
														// false, null);
	}

	/**
	 * 根据规则生成产品匹配xml
	 * 
	 * @param itemXmlString
	 * @param salePropDto
	 * @return
	 */
	public static List<Field> getNewFieldList(User user, String itemXmlString,
			ItemExt itemDto, SalePropDto salePropDto, Long brand) {
		List<Field> newFieldList = new ArrayList<Field>();
		try {
			List<Field> fieldList = SchemaReader.readXmlForList(itemXmlString);
			if (CommonUtils.isEmpty(fieldList)) {
				log.error("读取商品上新规则xml异常");
				return null;
			}
			for (Field field : fieldList) {
				// 添加属性信息
				Field addField = genField(user, field, newFieldList, itemDto,
						salePropDto, brand);
				if (addField != null) {
					newFieldList.add(addField);
				}

			}

		} catch (TopSchemaException e) {
			e.printStackTrace();
		}

		return newFieldList;
	}

	/**
	 * 根据枚举类型生成不同的Field
	 * 
	 * @return
	 */
	private static Field genField(User user, Field sourcefield,
			List<Field> newFieldList, ItemExt dto, SalePropDto salePropDto,
			Long brand) {
		Field field = null;
		// 当前fields
		String id = sourcefield.getId();
		String name = sourcefield.getName();
		String value = name.contains("品牌") ? brand.toString() : getValue(user,
				id, dto);
		if (value == null) {
			value = getDefaultValue(user, sourcefield);
		}
		switch (sourcefield.getType()) {
		case INPUT:
			/*
			 * 商品标题-返点比例-开始时间-商品数量-商品价格-运费模板ID
			 * 供应商-厂家联系方式-厂名-厂址-保质期-产品标准号-配料表-储藏方法-食品添加剂
			 * 透明素材图-售后说明模板ID-商家外部编码-天猫系统服务版本
			 */
			InputField inputField = new InputField();
			if ("description".equals(id)) {
				value = "<p style=\"height:0px;margin:0px;color:#ffffff;\">~~~~~</p>"
						+ FileCarryUtils.filterDesc(user, dto.getDesc());
			}
			// 商品数量：商品库存必须是0－1000000的数字
			if ("quantity".equals(id)) {
				long quantity = Long.parseLong(value);
				if (quantity > 1000000) {
					value = "999999";
				}
			}
			// if ("size_mapping_template_id".equals(id)) {
			// ItemCarryCustomFor1688 itemCarryCustom =
			// ItemCarryCustomFor1688.findByUserId(user.getIdlong());
			// if
			// (itemCarryCustom!=null&&itemCarryCustom.getSizeMappingTemplateId()!=null&&itemCarryCustom.getSizeMappingTemplateId()>0)
			// {
			// value= itemCarryCustom.getSizeMappingTemplateId().toString();
			// }
			// }

			inputField.setValue(value);
			field = inputField;

			break;
		case SINGLECHECK:
			/*
			 * 发布类型-宝贝类型-拍下减库存 产品剂型-是否礼盒装-是否进口-商品状态 商品文字的字符集-是否支持会员折扣-页面模板
			 * 运费承担方式-买家承担运费-是否为有机食品
			 * 保修-发票-退换货服务-橱窗推荐-有效期-是否在淘宝和天猫显示-是否在外店显示-是否是3D
			 * -扫码验真服务-营养师咨询服务-十倍赔偿服务
			 */
			SingleCheckField singleCheckField = new SingleCheckField();
			singleCheckField.setValue(new Value(value));
			field = singleCheckField;
			break;
		case MULTIINPUT:
			MultiInputField multiInputField = new MultiInputField();
			multiInputField.setValues(Arrays.asList(value));
			field = multiInputField;
			break;
		case MULTICHECK:
			/*
			 * 商品秒杀-提取方式-商品所属的店铺类目列表--适用性别
			 */
			if ("second_kill".equals(id)) {
				return null;
			}

			MultiCheckField multiCheckField = new MultiCheckField();
			MultiCheckField dataField = (MultiCheckField) sourcefield;
			value = dataField.getOptions().get(0).getValue();
			multiCheckField.setValues(Arrays.asList(new Value(value)));
			// if (CommonUtil.isNullOrEmpty(value)) {
			//
			// }else {
			// multiCheckField.setValues(Arrays.asList(new Value(value)));
			// }
			//
			field = multiCheckField;
			break;
		case COMPLEX:
			/*
			 * 商品卖点-所在地-运费-生产日期-进货日期-商品图片-无线商品描述-商品物流重量体积-新商品无线描述-商品描述
			 */
			ComplexField complexField = new ComplexField();
			// 商品描述-商品参数,商品实拍,商品尺码表
			complexField
					.setComplexValue(getComplexValue(user, dto, sourcefield));
			field = complexField;
			break;
		case MULTICOMPLEX:
			MultiComplexField multiComplexField = new MultiComplexField();
			MultiComplexField mtcf = (MultiComplexField) sourcefield;
			List<ComplexValue> mtcvs = new ArrayList<ComplexValue>();
			if ("material_prop_149422948".equalsIgnoreCase(id)
					|| "material_prop_151386995".equalsIgnoreCase(id)) {
				ComplexValue cv = new ComplexValue();
				cv.setInputFieldValue("material_prop_name", "其他");
				mtcvs.add(cv);
				cv.setInputFieldValue("material_prop_content", "100%");
				multiComplexField.setComplexValues(mtcvs);

			} else if ("sku".equalsIgnoreCase(id)) {
				// sku信息
				addSkuFieldAndSaleProp(user, newFieldList, mtcf, salePropDto,
						dto);
				return null;
			} else if (name.contains("扩展")) {
				return null;
			} else {
				if (id.contains("size_model_try")) {
					return null;
				}
				List<Field> fs = ((MultiComplexField) sourcefield)
						.getFieldList();
				ComplexValue cv = new ComplexValue();
				for (int i = 0; i < fs.size(); i++) {
					Field f = fs.get(i);
					switch (f.getType()) {
					case INPUT:
						cv.setInputFieldValue(f.getId(),
								getDefaultValue(user, f));
						break;
					case SINGLECHECK:
						cv.setInputFieldValue(f.getId(),
								getDefaultValue(user, f));
						break;
					case COMPLEX:
						cv.setComplexFieldValue(f.getId(),
								getComplexValue(user, dto, sourcefield));
						break;
					}
				}
				multiComplexField.addComplexValue(cv);
			}
			field = multiComplexField;
			break;
		case LABEL:
			field = new LabelField();
			break;
		}
		field.setId(id);
		field.setName(name);

		return field;
	}

	/**
	 * 获取默认值
	 * 
	 * @param sourcefield
	 * @return
	 */
	private static String getDefaultValue(User user, Field sourcefield) {
		Rule req = sourcefield.getRuleByName("requiredRule");
		Rule dataType = sourcefield.getRuleByName("valueTypeRule");

		if (sourcefield instanceof InputField) {
			String result = ((InputField) sourcefield).getDefaultValue();
			if (CommonUtil.isNullOrEmpty(result)) {
				if (req != null && req.getValue().equals("true")) {
					// 判断数据类型
					if (dataType != null) {
						if ("url".equalsIgnoreCase(dataType.getValue())) {
							return FileCarryUtils
									.uploadPicFromOnline(user,
											"https://cbu01.alicdn.com/img/ibank/2017/460/462/4003264064_2049781951.jpg");
						}
					} else {
						if (sourcefield.getId().equals("barcode")) {
							return "693" + DateUtil.getNowTime("MMddHHms");
						}
						return "28320";
					}
				}
			}
			return result;
		} else if (sourcefield instanceof SingleCheckField) {
			return getUnUsePropVal(sourcefield, null);
		}
		return null;
	}

	/**
	 * 生成SKU信息并且添加销售属性
	 * 
	 * @param sourceField
	 * @param salePropDto
	 * @return
	 */
	private static void addSkuFieldAndSaleProp(User user,
			List<Field> newFieldList, MultiComplexField sourceField,
			SalePropDto salePropDto, ItemExt itemDto) {
		// 已使用的颜色属性
		Set<String> colors = new HashSet<String>();
		// 已使用的其他属性
		Set<String> others = new HashSet<String>();

		List<SalePropModel> colorSps = salePropDto.getColorProps();
		// 添加颜色
		Field colorField = getColorSaleField(user, sourceField, colors,
				colorSps, itemDto.getItemImgs().get(0).getUrl());

		newFieldList.add(colorField);
		List<SalePropModel> otherSps = salePropDto.getOtherProps();
		// 添加其他
		Field otherField = getOtherSaleField(sourceField, others, otherSps);
		newFieldList.add(otherField);
		// 添加SKU]

		String otherFieldId = (otherField == null) ? "" : otherField.getId();

		MultiComplexField skuField = genSkuField(sourceField, colors, others,
				colorField.getId(), otherFieldId, itemDto.getPrice());
		newFieldList.add(skuField);

	}

	/**
	 * 生成销售属性中其他属性信息的Field
	 * 
	 * @param sourceField
	 *            原属性
	 * @param others
	 *            已使用的其他属性
	 * @param otherSps
	 *            其他销售属性集合
	 * @return 成功返回封装后的Field，失败返回Null
	 */
	private static Field getOtherSaleField(MultiComplexField sourceField,
			Set<String> others, List<SalePropModel> otherSps) {
		if (CommonUtil.isNullOrSizeZero(otherSps)) {
			return null;
		}
		MultiComplexField otherField = new MultiComplexField();
		List<ComplexValue> otherCvs = new ArrayList<ComplexValue>();
		Field otherF = getSaleFiledByName(sourceField, "其他");// .get(1);
		// Field otherF=sourceField.getFieldList().get(0);
		String otherId = otherF.getId();
		// 尺码信息
		// 获取SKU中的其他属性信息
		for (int i = 0; i < otherSps.size(); i++) {
			ComplexValue otherCv = new ComplexValue();
			SalePropModel otherSp = otherSps.get(i);

			String otherVal = "";
			String otherName = otherSp.getAliasName().replaceAll(
					"【|】|;|:|,|=|@|\\$|\\^|\\*|-", "");
			InputField otherAlias = new InputField();
			if ("inputfield"
					.equalsIgnoreCase(otherF.getClass().getSimpleName())) {
				// 尺码信息
				otherVal = otherName;
				InputField addField = new InputField();
				// 避免某些只有中文的字符过滤后为空
				String othername2 = CommonUtil.delChineseWord(otherName);
				if (otherSps.size() == 1) {
					othername2 = otherName;
				}
				addField.setValue(othername2);
				addField.setId(otherId.contains("_-1") ? otherId : otherId
						.concat("_-1"));
				otherCv.put(addField);
				// 身高
				InputField heightField = new InputField();
				heightField.setValue("" + (150 + 5 * i));
				heightField.setId("size_mapping_shengao");
				otherCv.put(heightField);
				// 体重
				InputField weightField = new InputField();
				weightField.setValue("" + (50 + 5 * i));
				weightField.setId("size_mapping_tizhong");
				otherCv.put(weightField);

				// 尺码备注
				otherAlias.setId("size_tip");
			} else {
				otherVal = getUnUsePropVal(otherF, others);
				SingleCheckField addField = new SingleCheckField();
				addField.setValue(otherVal);
				addField.setId(otherId);
				// 身高
				InputField heightField = new InputField();
				heightField.setValue("" + (150 + 5 * i));
				heightField.setId("size_mapping_shengao");
				otherCv.put(heightField);
				// 体重
				InputField weightField = new InputField();
				weightField.setValue("" + (50 + 5 * i));
				weightField.setId("size_mapping_tizhong");
				otherCv.put(weightField);
				otherCv.put(addField);
				otherAlias.setId("alias_name");
			}

			otherAlias.setValue(otherName);
			otherCv.put(otherAlias);
			others.add(otherVal);

			otherCvs.add(otherCv);
		}
		if (otherId.contains("std_size")) {
			otherId = otherId.replace("prop", "extends").replace("_-1", "");
		}
		otherField.setId(otherId);
		otherField.setComplexValues(otherCvs);
		return otherField;
	}

	/**
	 * 生成销售属性中颜色属性信息的Field
	 * 
	 * @param sourceField
	 *            原属性
	 * @param colors
	 *            已使用的颜色属性
	 * @param colorSps
	 *            颜色属性集合
	 * @param url
	 * @return 成功返回封装后的Field，失败返回Null
	 */
	private static Field getColorSaleField(User user,
			MultiComplexField sourceField, Set<String> colors,
			List<SalePropModel> colorSps, String url) {
		if (CommonUtil.isNullOrSizeZero(colorSps)) {
			return null;
		}
		MultiComplexField colorField = new MultiComplexField();
		List<ComplexValue> colorCvs = new ArrayList<ComplexValue>();
		// 不同类目的除颜色外的销售属性不定
		Field colorF = getSaleFiledByName(sourceField, "颜色");// .get(1);
		String colorId = colorF.getId();
		// 避免部分颜色对超过24个
		int size = colorSps.size() > 24 ? 24 : colorSps.size();
		// 获取SKU中的颜色属性信息
		for (int i = 0; i < size; i++) {
			ComplexValue colorCv = new ComplexValue();
			SalePropModel colorSp = colorSps.get(i);
			String colorVal = "";
			String colorName = colorSp.getAliasName();
			// 去特殊字符
			colorName = colorName.replace(",", "");
			if ("inputfield"
					.equalsIgnoreCase(colorF.getClass().getSimpleName())) {
				colorVal = colorName;
				colorCv.setInputFieldValue(colorId, colorVal);
			} else {
				colorVal = getUnUsePropVal(colorF, colors);
				colorCv.setSingleCheckFieldValue(colorId, new Value(colorVal));
			}
			colors.add(colorVal);
			InputField colorAlias = new InputField();
			colorAlias.setId("alias_name");

			colorAlias.setValue(colorName);
			colorCv.put(colorAlias);
			// 库存
			InputField quantityField = new InputField();
			quantityField.setValue("100");
			colorCv.put(quantityField);

			// baseColor
			MultiCheckField baseColor = new MultiCheckField();
			baseColor.setId("basecolor");
			baseColor.setValues(Arrays.asList(new Value("28324")));
			colorCv.put(baseColor);

			// if(colorSp.getImgUrl()!=null){
			InputField imgField = new InputField();
			imgField.setId("prop_image");
			if (StringUtils.isEmpty(colorSp.getImgUrl()) == false) {
				// 部分源宝贝小数属性无图片采用主图
				url = FileCarryUtils.uploadPicFromOnline(user,
						colorSp.getImgUrl());
			}

			imgField.setValue(url);
			colorCv.put(imgField);
			// }
			colorCvs.add(colorCv);
		}
		colorField.setId("prop_extend_1627207");
		colorField.setComplexValues(colorCvs);
		return colorField;
	}

	/**
	 * 获取未被使用的Option选项的值
	 * 
	 * @param scf
	 *            获取的Field的属性
	 * @param used
	 *            使用过的值的集合
	 * @return 有option选项则返回未被使用的值，无则返回默认值
	 */
	public static String getUnUsePropVal(Field scf, Set<String> used) {
		if (scf instanceof InputField) {
			return null;
		}

		List<Option> options = null;
		if (scf instanceof SingleCheckField) {
			options = ((SingleCheckField) scf).getOptions();
		} else if (scf instanceof MultiCheckField) {
			options = ((MultiCheckField) scf).getOptions();
		}
		String result = null;

		if (CommonUtil.isNullOrSizeZero(options)) {
			return null;
		}
		for (Option option : options) {
			String val = option.getValue();
			if (used != null && used.contains(val)) {
				continue;
			} else {
				result = val;
				// used.add(result);
				break;
			}
		}
		return result;

	}

	/**
	 * 根据销售属性生成Sku Field
	 * 
	 * @param sourceField
	 * @return
	 */
	private static MultiComplexField genSkuField(MultiComplexField sourceField,
			Set<String> colors, Set<String> others, String colorId,
			String otherId, String price) {
		MultiComplexField skuCf = new MultiComplexField();
		Collection<String> skuList = CommonUtil.mergeList(colors, others);
		colorId = colorId.replace("extend_", "");
		otherId = otherId.replace("extend_", "");
		if (otherId.contains("std_size")) {
			// 修正尺码类目的颜色，尺码属性标准化
			colorId = "in_" + colorId;
			if (StringUtils.isEmpty(otherId) == false) {
				otherId = otherId.replace("extends", "prop") + "_-1";
			}
		}
		List<Field> reqFields = new ArrayList<Field>();
		// 添加SKU信息中的其他必须field
		for (int i = 2; i < sourceField.getFieldList().size(); i++) {
			Field reqField = sourceField.getFieldList().get(i);
			if (CommonUtil
					.isNullOrEmpty(reqField.getRuleByName("requiredRule"))) {
				continue;
			}
			reqFields.add(reqField);
		}

		String maybeColorId = sourceField.getFieldList().get(0).getId();
		for (String sku : skuList) {
			ComplexValue cv = new ComplexValue();
			String props[] = sku.split(";");
			// 颜色属性分为input,SingleCheck
			if (colorId.contains("in")) {
				InputField color = new InputField();
				color.setId(colorId);
				color.setValue(props[0]);
				cv.put(color);
			} else {
				SingleCheckField color = new SingleCheckField();
				if (maybeColorId.contains("1627207")) {
					colorId = maybeColorId;
				}
				color.setId(colorId);
				color.setValue(props[0]);
				cv.put(color);
			}

			if (StringUtils.isEmpty(otherId) == false) {
				if (otherId.contains("std_size")) {
					InputField other = new InputField();
					other.setId(otherId);
					// 避免某些只有中文的字符过滤后为空
					String otherVal = CommonUtil.delChineseWord(props[1]);
					if (others.size() == 1) {
						otherVal = props[1];
					}
					other.setValue(otherVal);
					cv.put(other);
				} else {
					SingleCheckField other = new SingleCheckField();
					other.setId(otherId);
					other.setValue(props[1]);
					cv.put(other);
				}

			}
			InputField priceField = new InputField();
			priceField.setId("sku_price");
			priceField.setValue(price);
			cv.put(priceField);
			InputField quantityField = new InputField();
			quantityField.setId("sku_quantity");
			quantityField.setValue("100");
			cv.put(quantityField);

			InputField skuProductCode = new InputField();
			skuProductCode.setId("sku_ProductCode");
			skuProductCode.setValue(DateUtil.getNowTime("yyMMddHHmmssms"));
			cv.put(skuProductCode);

			if (!CommonUtil.isNullOrSizeZero(reqFields)) {
				for (Field f : reqFields) {
					String dataType = f.getRuleByName("valueTypeRule")
							.getValue();
					if (f instanceof InputField
							&& f.getRuleByName("requiredRule") != null
							&& "date".equals(dataType)) {
						// 上市时间
						InputField field = new InputField();
						field.setId(f.getId());
						field.setValue(DateUtil.getNowTime("yyyy-MM-dd"));
						cv.put(field);
					}
				}
			}
			skuCf.addComplexValue(cv);
		}
		skuCf.setId("sku");
		return skuCf;
	}

	/**
	 * 从属性集合对中获取符合对应条件的值
	 * 
	 * @return
	 */
	public static String getValue(User user, String id, ItemExt itemDto) {
		String value = null;
		if ("vertical_image".equals(id)) {
			// 商品竖图
			value = FileCarryUtils.uploadPicDealSizeOnline(user,
					itemDto.getPicUrl(), 800, 1200);
		} else if ("white_bg_image".equalsIgnoreCase(id)) {
			// 透明素材图
			value = "https://img.alicdn.com/imgextra/i2/79742176/TB2eGOPqrlmpuFjSZFlXXbdQXXa_!!79742176.jpg";
			// value = FileCarryUtils.uploadPicFromOnline(getUser(), picUrl);
		} else if ("diaopai_pic".equalsIgnoreCase(id)) {
			// 吊牌图
			ItemCarryCustomFor1688 itemCarryCustom = ItemCarryCustomFor1688
					.findByUserId(user.getIdlong());
			if (itemCarryCustom != null
					&& StringUtils.isEmpty(itemCarryCustom.getHangtagUrl()) == false) {
				value = itemCarryCustom.getHangtagUrl();
			} else {
				value = FileCarryUtils.newUploadPicFromOnline(user, itemDto.getPicUrl()).getRes().getPicturePath();
			}

		} else if ("prop_13021751".equals(id)) {
			// 货号
			if (CarrierTaskAction.VIP_USER_NICK.contains(user.getUserNick())) {
				value = "1003-a-" + itemDto.getNumIid() + "-"
						+ itemDto.getPropsName();
			} else {
				value = DateUtil.getNowTime("yyMMddHHmmssms");
			}

		} else if (id.equalsIgnoreCase("title")) {
			// 标题
			value = itemDto.getTitle();
		} else if (id.equalsIgnoreCase("short_title")) {
			// 短标题
			if (itemDto.getTitle().length()>10) {
				value = itemDto.getTitle().substring(0, 10);
			}else {
				value = itemDto.getTitle();
			}
			
		} else if ("quantity".equalsIgnoreCase(id)) {
			// 数量
			value = itemDto.getNum().toString();
		} else if ("price".equalsIgnoreCase(id)) {
			// 价格
			value = itemDto.getPrice();
		} else if (id.equalsIgnoreCase("item_status")) {
			// 商品状态 出售中-0 仓库-2 定时上架-1 默认值-0
			value = "2";
		} else if (id.equalsIgnoreCase("item_type")) {
			// 发布类型 默认值：b 一口价
			value = "b";
		} else if (id.equalsIgnoreCase("stuff_status")) {
			// 宝贝类型 默认值：5 全新
			value = "5";
		} else if ("freight_payer".equalsIgnoreCase(id)) {
			// 运费承担方式
			
			ItemCarryCustomFor1688 itemCarryCustom = ItemCarryCustomFor1688
					.findByUserId(user.getIdlong());
			
			if (itemCarryCustom!=null) {
				Long postageId=itemCarryCustom.getPostageId();
				if (postageId!=null&&postageId>0) {
					value = "1";
				}
			}
		}else if ("postage_id".equalsIgnoreCase(id)) {
			ItemCarryCustomFor1688 itemCarryCustom = ItemCarryCustomFor1688
					.findByUserId(user.getIdlong());
			
			if (itemCarryCustom!=null) {
				Long postageId=itemCarryCustom.getPostageId();
				if (postageId!=null&&postageId>0) {
					value = postageId.toString();
				}
			}
			
		} else if ("delivery_way".equalsIgnoreCase(id)) {
			// 提取方式
			value = "2";
		} else if ("valid_thru".equalsIgnoreCase(id)) {
			value = "14";
		} else if ("item_status".equalsIgnoreCase(id)) {
			value = "2";
		} else if ("auction_point".equalsIgnoreCase(id)) {
			// 返点比例： 最低返点比例为0.5%，设定的返点比例必须是0.5%的整数倍
			value = "0.5";
		}else if ("outer_id".equalsIgnoreCase(id)) {
			value= "1003-a-" + itemDto.getNumIid() + "-"
							+ itemDto.getPropsName();
		}
		return value;
	}

	/**
	 * 根据Field解析ComplexValue
	 * 
	 * @param itemDto
	 * @param sourceField
	 * @return
	 */
	public static ComplexValue getComplexValue(User user, ItemExt itemDto,
			Field sourceField) {
		ComplexValue cv = new ComplexValue();
		List<Field> fields = null;
		if (sourceField instanceof ComplexField) {
			fields = ((ComplexField) sourceField).getFieldList();
		} else if (sourceField instanceof MultiComplexField) {
			fields = ((MultiComplexField) sourceField).getFieldList();
		}
		String value = getDefaultValue(user, sourceField);
		String id = sourceField.getId();
		if (fields != null) {
			// 遍历fields
			for (int i = 0; i < fields.size(); i++) {
				Field field = fields.get(i);
				switch (field.getType()) {
				case INPUT:
					if ("item_images".equalsIgnoreCase(id)
							|| "product_images".equalsIgnoreCase(id)) {
						// 商品图片或者产品图片
						List<ItemImg> imgs = itemDto.getItemImgs();
						if (i < imgs.size()) {
							value = FileCarryUtils.newUploadPicFromOnline(user,
									imgs.get(i).getUrl()).getRes().getPicturePath();
						}

					} else if ("freight".equals(id)) {
						// 运费
						// 判断是否选择运费模板
						if (CommonUtil.isNullOrEmpty(itemDto.getPostageId())) {
							// 未选择
							value = "20";
						} else {
							value = itemDto.getPostageId().toString();
						}
					} else if ("location".equals(id)) {
						// 所在地
						if (i == 0) {
							value = itemDto.getLocation().getState();
						} else if (i == 1) {
							value = itemDto.getLocation().getCity();
						}
					} else if ("item_attach_images".equals(id)) {
						if (field.getId().equals("attach_51")) {
							ItemCarryCustomFor1688 itemCarryCustom = ItemCarryCustomFor1688
									.findByUserId(user.getIdlong());
							if (itemCarryCustom != null
									&& StringUtils.isEmpty(itemCarryCustom.getHangtagUrl()) == false) {
								value = itemCarryCustom.getHangtagUrl();
							} else {
								value = FileCarryUtils.newUploadPicFromOnline(user, itemDto.getPicUrl()).getRes().getPicturePath();
							}
						}else {
							// 商品资质图片.(user,
							value = FileCarryUtils.newUploadPicFromOnline(user,
									itemDto.getPicUrl()).getRes().getPicturePath();
						}
						
					}
					// 设置默认值
					cv.setInputFieldValue(field.getId(), value);
					break;
				case SINGLECHECK:
					value = value == null ? ((SingleCheckField) field)
							.getDefaultValue() : null;
					cv.setInputFieldValue(field.getId(), value);
					break;
				case COMPLEX:
					ComplexValue concv = new ComplexValue();

					if ("description".equalsIgnoreCase(id)) {
						// 产品描述
						List<Field> sonFields = ((ComplexField) field)
								.getFieldList();
						for (int j = 0; j < sonFields.size(); j++) {
							Field sonf = sonFields.get(j);
							if (i == 0 && j == 0) {
								value = FileCarryUtils.filterDesc(user,
										itemDto.getDesc())
										+ "<p style=\"height:0px;margin:0px;color:#ffffff;\">~~~~~</p>";
							} else {
								// 主描述位置待解决
								if (sonf.getId().contains("content")
										&& !CommonUtil
												.isNullOrEmpty(sonf
														.getRuleByName("minLengthRule"))) {
									value = "<p style=\"height:0px;margin:0px;color:#ffffff;\">~~~~~</p>";
								} else if (CommonUtil.isNullOrEmpty(sonf
										.getRuleByName("minLengthRule"))) {
									continue;
								}
							}
							concv.setInputFieldValue(sonf.getId(), value);
						}

						cv.setComplexFieldValue(field.getId(), concv);

					} else if ("wireless_desc".equals(id)) {
						break;
						// 产品描述
//						List<Field> sonFields = ((ComplexField) field)
//								.getFieldList();
//						// for (int j = 0; j < sonFields.size(); j++) {
//						// Field sonf =sonFields.get(j);
//						if (field.getId().equals("item_info")
//								|| field.getId().equals("size")
//								|| field.getId().equals("item_picture")) {
//							concv.setSingleCheckFieldValue(sonFields.get(0).getId(),new Value("true"));
//							if (field.getId().equals("item_picture")) {
//								List<Field> picSonFields = ((ComplexField) field)
//										.getFieldList();
//								// 跳过第一个
//
//								List<String> imgs = extractPicUrlForDesc(itemDto
//										.getDesc());
//								for (int k = 1; k < picSonFields.size(); k++) {
//									ComplexValue value2 = new ComplexValue();
//									Field picField = picSonFields.get(k);
//									// 避免图片不足的情况下空指针
//									if (imgs.size() <= k - 1) {
//										break;
//									}
//									String urlValue = FileCarryUtils
//											.newUploadPicFromOnline(user,
//													imgs.get(k - 1)).getRes().getPicturePath();
//									value2.setInputFieldValue(
//											"item_picture_image", urlValue);
//									concv.setComplexFieldValue(
//											picField.getId(), value2);
//
//								}
//
//							}
//							cv.setComplexFieldValue(field.getId(), concv);
//							break;
//						} else {
//							concv.setSingleCheckFieldValue(sonFields.get(0).getId(),new Value("false"));
//							cv.setComplexFieldValue(field.getId(), concv);
//							break;
//						}
					}

					
				}
			}
		}
		return cv;
	}

	/**
	 * 根据属性名称模糊获取Sku中的销售属性
	 * 
	 * @param sourceField
	 * @return
	 */
	public static Field getSaleFiledByName(MultiComplexField sourceField,
			String name) {
		if (sourceField == null || CommonUtil.isNullOrEmpty(name)) {
			return null;
		}

		List<Field> fields = sourceField.getFieldList();
		for (Field field : fields) {
			String fieldName = field.getName();
			if (name.equals("颜色") && fieldName.contains(name)) {
				return field;
			}
			if (name.equals("其他") && !fieldName.contains("颜色")) {
				return field;
			}
		}

		return null;

	}

	private static final Pattern IMG_PATTERN = Pattern
			.compile("src\\s*=\\s*[\"|'](.+?)[\"|']");

	/**
	 * 提取描述中的图片
	 * 
	 * @param desc
	 * @return
	 */
	public static List<String> extractPicUrlForDesc(String desc) {
		Matcher matcher = IMG_PATTERN.matcher(desc);
		List<String> imgs = new ArrayList<String>();
		while (matcher.find()) {
			String img = matcher.group(1);
			imgs.add(img);
		}
		return imgs;
	}

}
