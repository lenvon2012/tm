/**
 * 
 */

package controllers;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.carrierTask.CarrierLimitForDQ;
import models.carrierTask.CarrierTask;
import models.carrierTask.ItemArea;
import models.carrierTask.ItemCarryCustom;
import models.carrierTask.SubCarrierTask;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.dom4j.Element;

import play.db.jpa.NoTransaction;
import result.TMResult;
import utils.DateUtil;
import utils.TaobaoUtil;
import actions.carriertask.CarrierTaskAction;
import bustbapi.DeliveryApi;
import bustbapi.PicApi;
import carrier.FileCarryUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.NumberUtil;
import com.taobao.api.domain.DeliveryTemplate;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.Picture;
import com.taobao.api.domain.PictureCategory;
import com.taobao.api.domain.SellerCat;
import com.taobao.top.schema.Util.XmlUtils;
import com.taobao.top.schema.depend.DependExpress;
import com.taobao.top.schema.depend.DependGroup;
import com.taobao.top.schema.enums.FieldTypeEnum;
import com.taobao.top.schema.enums.RuleTypeEnum;
import com.taobao.top.schema.exception.TopSchemaException;
import com.taobao.top.schema.factory.SchemaReader;
import com.taobao.top.schema.field.ComplexField;
import com.taobao.top.schema.field.Field;
import com.taobao.top.schema.field.InputField;
import com.taobao.top.schema.field.MultiCheckField;
import com.taobao.top.schema.field.MultiComplexField;
import com.taobao.top.schema.field.SingleCheckField;
import com.taobao.top.schema.rule.Rule;
import com.taobao.top.schema.value.ComplexValue;
import com.taobao.top.schema.value.Value;

import configs.Subscribe.Version;
import dao.UserDao;

/**
 * @author navins
 * @date: Jan 20, 2014 8:11:21 PM
 */
public class ItemCarrier extends TMController {

	public static void index() {
		render("carrier/itemcarrier.html");
	}

	public static void itemFailLog() {
		render("Application/ItemFailLog.html");
	}

	public static void doCopyItem(String input, String wapData) {
		User user = getUser();
		boolean isValid = UserDao.doValid(user);
		if(!isValid) {
			renderJSON(JsonUtil.getJson(new TMResult(false, "用户已过期，请重新授权或续订软件！", null)));
		}
		if(user.getVersion() == Version.LL) {
			Boolean success = CarrierLimitForDQ.checkUserLimit(user.getId());
			if(!success) {
				renderJSON(JsonUtil.getJson(new TMResult(false, "已达到当月最大可复制宝贝数，如需继续复制请联系客服升级版本！", null)));
			}
		}

		Long numIid = null;
		if (StringUtils.isBlank(input)) {
			renderJSON(JsonUtil.getJson(new TMResult(false, "请先输入拷贝宝贝ID 或 宝贝网址！", null)));
		}
		input = input.trim();
		if (NumberUtils.isDigits(input)) {
			numIid = NumberUtil.parserLong(input, 0L);
		} else {
			numIid = CarrierTaskAction.getNumId(input);
		}

		if (numIid == null || numIid <= 0) {
			renderJSON(JsonUtil.getJson(new TMResult(false, "请输入有效的需要拷贝宝贝ID 或 宝贝网址！", null)));
		}

		wapData = checkWapData(wapData);

		TMResult tmResult = null;
		if(!user.isTmall()) {
			tmResult = CarrierTaskAction.doCarryForTaobao(null, numIid, wapData, null, null, user);
		} else {
			tmResult = CarrierTaskAction.doCarryForTmall(numIid, wapData, null, user);
		}

		if (tmResult.isOk()) {
			if (user.isTmall()) {
				// 天猫
				Map<String, String> itemMap = (Map<String, String>) tmResult.getRes();
				SubCarrierTask.recordSuccess(numIid.toString(), itemMap.get("title"), user.getUserNick(), tmResult.getMsg(), SubCarrierTask.SubTaskType.天猫复制, itemMap.get("item_image_0"));
			} else {
				// 淘宝
				Item item = (Item) tmResult.getRes();
				SubCarrierTask.recordSuccess(numIid.toString(), item.getTitle(), user.getUserNick(), tmResult.getMsg(), SubCarrierTask.SubTaskType.淘宝复制, item.getPicUrl());
			}
			if(user.getVersion() == Version.LL) {
				CarrierLimitForDQ.updateUseCountByUserId(user.getId());
			}
			String msg = "拷贝成功，请在仓库中查看！新宝贝地址：<br><a href='" + tmResult.getMsg() + "' target='_blank'>" + tmResult.getMsg() + "</a>";
			tmResult.setMsg(msg);
		} else if (tmResult.getMsg().contains("复制任务添加成功")) {
			ItemCarryCustom itemCarryCustom = ItemCarryCustom.findByUserId(user.getIdlong());
			Long itemCarryCustomId = 0L;
			if (itemCarryCustom != null) {
				itemCarryCustomId = itemCarryCustom.getId();
			}
			CarrierTaskAction.addBatchCarrierTask(user, numIid.toString(), user.getUserNick(), itemCarryCustomId);
		} else {
			SubCarrierTask.recordError(numIid.toString(), user.getUserNick(), tmResult.getMsg(), user.isTmall() ? SubCarrierTask.SubTaskType.天猫复制 : SubCarrierTask.SubTaskType.淘宝复制);
		}

		renderJSON(tmResult);
	}

    private static String checkWapData(String wapData) {
	    try {
            JSONObject parse = JSON.parseObject(wapData);

            JSONArray ret = parse.getJSONArray("ret");
            String str = ret.getString(0);
            if (str.equals("SUCCESS::调用成功")) {
                return wapData;
            }
            return null;
        } catch (Exception e) {
	        log.error(e.toString());
        }
        return null;
    }

    public static List<Field> getNewFieldList(String itemXmlString, Map<String, String> itemMap, User user) {
		List<Field> newFieldList = new ArrayList<Field>();

		try {
			List<Field> fieldList = SchemaReader.readXmlForList(itemXmlString);
			if(CommonUtils.isEmpty(fieldList)) {
				log.error("读取商品上新规则xml异常");
				renderJSON(JsonUtil.getJson(new TMResult(false, "拷贝失败，读取商品上新规则xml异常", null)));
			}
			Map<String, String> valueMap = new HashMap<String, String>();
			
			for(Field field : fieldList) {
				String id = field.getId();
				
				if(id.indexOf("locality_life") >= 0) {
					continue;
				}
				if("expired_refund".equalsIgnoreCase(id)) {
					continue;
				}
				if("auto_refund".equalsIgnoreCase(id)) {
					continue;
				}
				if("wap_desc".equalsIgnoreCase(id)) {
					continue;
				}
				
				if("wireless_desc".equalsIgnoreCase(id)) {
					continue;
				}
				
				String name = field.getName();
				FieldTypeEnum fieldEnum = field.getType();
				
				String value = StringUtils.EMPTY;
				
				switch (fieldEnum) {
					case INPUT:
						/*
						 * 商品标题-返点比例-开始时间-商品数量-商品价格-运费模板ID
						 * 供应商-厂家联系方式-厂名-厂址-保质期-产品标准号-配料表-储藏方法-食品添加剂
						 * 透明素材图-售后说明模板ID-商家外部编码-天猫系统服务版本
						 * */
						if("auction_point".equalsIgnoreCase(id)) {
							value = "0.5";
						} else if("short_title".equalsIgnoreCase(id)) {
							value = itemMap.get("title").substring(0, 10);
						} else if("vertical_image".equalsIgnoreCase(id)) {
							//商品竖图
							value = FileCarryUtils.uploadPicDealSizeOnline(user, itemMap.get("item_image_0"), 800, 1200);
						} else if("white_bg_image".equalsIgnoreCase(id)) {
							//透明素材图
							value = "https://img.alicdn.com/imgextra/i2/79742176/TB2eGOPqrlmpuFjSZFlXXbdQXXa_!!79742176.jpg";
						} else if("diaopai_pic".equalsIgnoreCase(id)) {
							//吊牌图
							value= FileCarryUtils.uploadPicFromOnline(user, itemMap.get("item_image_0"));
						} else {
							value = getValue(field, newFieldList, valueMap, itemMap);
						}
						
						if(StringUtils.isEmpty(value)) {
							break;
						}
						
						InputField field1 = new InputField();
						field1.setId(id);
						field1.setName(name);
						field1.setValue(value);
						
						newFieldList.add(field1);
						valueMap.put(id, value);
						break;
					case SINGLECHECK:
						/*
						 * 发布类型-宝贝类型-拍下减库存
						 * 产品剂型-是否礼盒装-是否进口-是否新品-商品状态
						 * 商品文字的字符集-是否支持会员折扣-页面模板
						 * 运费承担方式-买家承担运费-是否为有机食品
						 * 保修-发票-退换货服务-橱窗推荐-有效期-是否在淘宝和天猫显示-是否在外店显示-是否是3D-扫码验真服务-营养师咨询服务-十倍赔偿服务
						 * */
						if("freight_payer".equalsIgnoreCase(id)) {
							value = "2";
						} else if("valid_thru".equalsIgnoreCase(id)) {
							value = "14";
						} else if("item_status".equalsIgnoreCase(id)) {
							value = "2";
						} else if("is_xinpin".equalsIgnoreCase(id)) {
							value = "true";
						} else {
							value = getValue(field, newFieldList, valueMap, itemMap);
						}
						
						if(StringUtils.isEmpty(value)) {
							break;
						}
						
						SingleCheckField field2 = new SingleCheckField();
						field2.setId(id);
						field2.setName(name);
						field2.setValue(value);
						
						newFieldList.add(field2);
						valueMap.put(id, value);
						break;
					case MULTIINPUT:
						/*
						 * 
						 * */
//						MultiInputField field3 = new MultiInputField();
//						List<String> values1 = new ArrayList<String>();
//						values1.add("multiInput值");
//						field3.setValues(values1);
						break;
					case MULTICHECK:
						/*
						 * 商品秒杀-提取方式-商品所属的店铺类目列表
						 * */
                        List<String> values = new ArrayList<String>();
						if("delivery_way".equalsIgnoreCase(id)) {
							value = "2";
                            values.add(value);
						} else {
                            values = getMultiCheckValue(field, newFieldList, valueMap, itemMap);
						}
						
						if(CommonUtils.isEmpty(values)) {
							break;
						}
						
						MultiCheckField field4 = new MultiCheckField();
						List<Value> values2 = new ArrayList<Value>();
						for (String v : values) {
                            values2.add(new Value(v));
                        }
						field4.setId(id);
						field4.setName(name);
						field4.setValues(values2);
						
						newFieldList.add(field4);
						valueMap.put(id, value);
						break;
					case COMPLEX:
						/*
						 * 商品卖点-所在地-运费-生产日期-进货日期-商品图片-无线商品描述-商品物流重量体积-新商品无线描述-商品描述
						 * */
						ComplexField field5 = new ComplexField();
						ComplexValue complexValue = new ComplexValue();
						
						List<Field> resFieldList = getFieldList(field);
						for (Field resfield : resFieldList) {
							// 新商品无线描述之商品图片
							if("item_picture".equalsIgnoreCase(resfield.getId())) {
								// TODO
							}
							value = getValue(resfield, newFieldList, valueMap, itemMap);
							
							switch (resfield.getType()) {
								case INPUT:
									complexValue.setInputFieldValue(resfield.getId(), value);
									break;
								case SINGLECHECK:
									complexValue.setSingleCheckFieldValue(resfield.getId(), new Value(value));
									break;
								case COMPLEX:
									// 商品描述-商品参数,商品实拍,商品尺码表
									ComplexValue minComplexValue = new ComplexValue();
									if("desc_module_5_cat_mod".equalsIgnoreCase(resfield.getId())) {
										minComplexValue.setInputFieldValue("desc_module_5_cat_mod_content", "<p style=\"height:0px;margin:0px;color:#ffffff;\">~~~~~</p>");
										minComplexValue.setInputFieldValue("desc_module_5_cat_mod_order", "1");
									} else if ("desc_module_24_cat_mod".equalsIgnoreCase(resfield.getId())) {
										minComplexValue.setInputFieldValue("desc_module_24_cat_mod_content", "<p style=\"height:0px;margin:0px;color:#ffffff;\">~~~~~</p>");
										minComplexValue.setInputFieldValue("desc_module_24_cat_mod_order", "2");
									} else if ("desc_module_41_cat_mod".equalsIgnoreCase(resfield.getId())) {
										minComplexValue.setInputFieldValue("desc_module_41_cat_mod_content", "<p style=\"height:0px;margin:0px;color:#ffffff;\">~~~~~</p>");
										minComplexValue.setInputFieldValue("desc_module_41_cat_mod_order", "3");
									}
									
									complexValue.setComplexFieldValue(resfield.getId(), minComplexValue);
									break;
								case MULTICOMPLEX:
									// 商品描述-自定义模块列表
									if("desc_module_user_mods".equalsIgnoreCase(resfield.getId())) {
										List<ComplexValue> complexValueList = new ArrayList<ComplexValue>();
										ComplexValue complexValue2 = new ComplexValue();
										// 自定义模块名称
										complexValue2.setInputFieldValue("desc_module_user_mod_name", "商品描述");
										// 自定义模块内容
										complexValue2.setInputFieldValue("desc_module_user_mod_content", (String) itemMap.get("description"));
										// 自定义模块排序值
										complexValue2.setInputFieldValue("desc_module_user_mod_order", "4");
										complexValueList.add(complexValue2);
										
										complexValue.setMultiComplexFieldValues(resfield.getId(), complexValueList);
									}
									break;
							}
						}
						
						field5.setId(id);
						field5.setName(name);
						field5.setComplexValue(complexValue);
						
						newFieldList.add(field5);
						// 该类型数据不放入valueMap
//						valueMap.put(id, value);
						break;
					case MULTICOMPLEX:
						/*
						 * 材质成分-SKU
						 * */
						MultiComplexField field6 = new MultiComplexField();
						List<ComplexValue> values3 = new ArrayList<ComplexValue>();
						
						if("material_prop_149422948".equalsIgnoreCase(id)) {
							String materialStr = itemMap.get("材质成分");
							if(StringUtils.isEmpty(materialStr)) {
								break;
							}
							// 棉70% 其他30%
							String[] split = materialStr.split(" ");
							for (String s : split) {
							    // 棉70%
                                String materialPropName = null;
                                String materialPropContent = null;

								// material_prop_content匹配规则
                                Pattern regexRule = Pattern.compile("\\d+(\\.\\d{1,2})?");
                                Matcher matcher = regexRule.matcher(s);
                                if (matcher.find()) {
                                    // 棉
                                    materialPropName = s.substring(0, matcher.start());
                                    // 70
                                    materialPropContent = matcher.group();
                                } else {
                                    // 未匹配到含量
                                    s = s.trim();
                                    // 当材质为彩棉|有机棉，含量可以不填
                                    if ("彩棉".equals(s) || "有机棉".equals(s)) {
                                        materialPropName = s;
                                        materialPropContent = null;
                                    } else {
                                        log.error("材质成分匹配错误！！！materialStr=" + materialStr + ",s=" + s);
                                    }
                                }

                                ComplexValue complexValue2 = new ComplexValue();
								complexValue2.setSingleCheckFieldValue("material_prop_name", new Value(materialPropName));
								complexValue2.setInputFieldValue("material_prop_content", materialPropContent);
								values3.add(complexValue2);
							}
						} else if ("sku".equalsIgnoreCase(id)) {
							List<Field> skuFieldList = getFieldList(field);
							String skusStr = itemMap.get(id);
							String[] splits = skusStr.split(",");
							
							for (String s : splits) {
								ComplexValue complexValue2 = new ComplexValue();
								
								String[] split = s.split(";");
								for (String t : split) {
									String i = t.split(":")[0];
									String v = t.split(":")[1];
									for (Field skuField : skuFieldList) {
										if(skuField.getId().indexOf(i) >= 0) {
											switch (skuField.getType()) {
												case INPUT:
												    // 如果sku库存为0 则将库存设置为1
												    if (skuField.getId().equals("sku_quantity") && Integer.valueOf(v).equals(0)) {
												        v = "1";
                                                    }
													complexValue2.setInputFieldValue(skuField.getId(), v);
													break;
												case SINGLECHECK:
													complexValue2.setSingleCheckFieldValue(skuField.getId(), new Value(v));
													break;
											}
										}
									}
								}
								values3.add(complexValue2);
							}
						} else if ("prop_extend_1627207".equalsIgnoreCase(id)) {
							String extendStr = itemMap.get(id);
							if(StringUtils.isEmpty(extendStr)) {
								break;
							}
							String[] splits = extendStr.split(",");
							for (String s : splits) {
								ComplexValue complexValue2 = new ComplexValue();
								
								String v = s.split(";")[0];
								String n = s.split(";")[1];
								String i = s.split(";")[2];
								
								complexValue2.setSingleCheckFieldValue("prop_1627207", new Value(v));
								complexValue2.setInputFieldValue("alias_name", n);
								complexValue2.setInputFieldValue("prop_image", i);
								
								values3.add(complexValue2);
							}
						}
						
						field6.setId(id);
						field6.setName(name);
						field6.setComplexValues(values3);
						
						newFieldList.add(field6);
						// 该类型数据不放入valueMap
//						valueMap.put(id, value);
						break;
					case LABEL:
						/*
						 * 信息-条形码
						 * */
//						LabelField field7 = new LabelField();
//						LabelGroup labelGroup = new LabelGroup();
//						Label label = new Label();
//						// 传空值
//						label.setDesc(value);
//						labelGroup.add(label);
//						
//						field7.setId(id);
//						field7.setName(name);
//						field7.setLabelGroup(labelGroup);
//						
//						newFieldList.add(field7);
//						valueMap.put(id, value);
						break;
				}
				
			}
			
		} catch (TopSchemaException e) {
			e.printStackTrace();
		}
    	
		return newFieldList;
    }

    private static List<String> getMultiCheckValue(Field field, List<Field> newFieldList, Map<String, String> valueMap, Map<String, String> itemMap) {
        String id = field.getId();
        String name = field.getName();
        List<Rule> rules = field.getRules();
        Map<String, Rule> ruleMap = new HashMap<String, Rule>();
        for(Rule rule : rules) {
            ruleMap.put(rule.getName(), rule);
        }

        String value = StringUtils.EMPTY;

        if(itemMap.containsKey(id)) {
            value = itemMap.get(id);
        }
        if(itemMap.containsKey(name)) {
            value = itemMap.get(name);
        }

	    List<String> result = new ArrayList<String>();
        String[] values = value.split(",");
        for (String v : values) {
            result.add(processValue(field, newFieldList, valueMap, ruleMap, v));
        }

	    return result;
    }

    private static String processValue(Field field, List<Field> newFieldList, Map<String, String> valueMap, Map<String, Rule> ruleMap, String value) {
        if(StringUtils.isEmpty(value)) {
            try {
                value = getDefaultValue(field);
            } catch (TopSchemaException e) {
                e.printStackTrace();
            }
        } else {
            Map<String, String> optionMap = null;
            try {
                optionMap = getOptionMap(field);
            } catch (TopSchemaException e) {
                e.printStackTrace();
            }
            if(!CommonUtils.isEmpty(optionMap) && optionMap.containsKey(value)) {
                value = optionMap.get(value).toString();
            } else if(optionMap.containsKey("其它")) {
                value = "-1";
            } else if("prop_20000".equalsIgnoreCase(field.getId()) && !CommonUtils.isEmpty(optionMap)) {
                // 复制的品牌不同时
                for (String key : optionMap.keySet()) {
                    if(!StringUtils.isEmpty(optionMap.get(key))) {
                        value = optionMap.get(key);
                        break;
                    }
                }
            }
        }

        if(StringUtils.isEmpty(value)) {
            if(ruleMap.containsKey(RuleTypeEnum.REQUIRED_RULE.value())) {
                // TODO delivery_way && valid_thru
            }
        } else {
            if(ruleMap.containsKey(RuleTypeEnum.DISABLE_RULE.value())) {
                Rule rule = ruleMap.get(RuleTypeEnum.DISABLE_RULE.value());
                DependGroup dependGroup = rule.getDependGroup();
                DependExpress dependExpress = dependGroup.getDependExpressList().get(0);
                String dependId = dependExpress.getFieldId();
                String dependValue = dependExpress.getValue();
                String dependSymbol = dependExpress.getSymbol();

                String compareValue = StringUtils.EMPTY;

                for(Field newfield : newFieldList) {
                    if(dependId.equalsIgnoreCase(newfield.getId())){
                        compareValue = valueMap.get(dependId) == null? compareValue : valueMap.get(dependId);
                    }
                }

                if(DependExpress.SYMBOL_NOT_EQUALS.equals(dependSymbol)) {
                    if(!dependValue.equalsIgnoreCase(compareValue)) {
                        value = StringUtils.EMPTY;
                    }
                }else if(DependExpress.SYMBOL_NOT_CONTAINS.equals(dependSymbol)){
                    // TODO locality_life
                }
            }
        }

        return value;
    }

    public static String getValue(Field field, List<Field> newFieldList, Map<String, String> valueMap, Map<String, String> itemMap) {
    	String id = field.getId();
		String name = field.getName();
		List<Rule> rules = field.getRules();
		Map<String, Rule> ruleMap = new HashMap<String, Rule>();
		for(Rule rule : rules) {
			ruleMap.put(rule.getName(), rule);
		}
		
		String value = StringUtils.EMPTY;
		
		if(itemMap.containsKey(id)) {
			value = itemMap.get(id);
		}
		if(itemMap.containsKey(name)) {
			value = itemMap.get(name);
		}
		
		if(StringUtils.isEmpty(value)) {
			try {
				value = getDefaultValue(field);
			} catch (TopSchemaException e) {
				e.printStackTrace();
			}
		} else {
			Map<String, String> optionMap = null;
			try {
				optionMap = getOptionMap(field);
			} catch (TopSchemaException e) {
				e.printStackTrace();
			}
			if(!CommonUtils.isEmpty(optionMap) && optionMap.containsKey(value)) {
				value = optionMap.get(value).toString();
			} else if(optionMap.containsKey("其它")) {
				value = "-1";
			} else if("prop_20000".equalsIgnoreCase(id) && !CommonUtils.isEmpty(optionMap)) {
				// 复制的品牌不同时
				for (String key : optionMap.keySet()) {
					if(!StringUtils.isEmpty(optionMap.get(key))) {
						value = optionMap.get(key);
						break;
					}
				}
			}
		}

		if(StringUtils.isEmpty(value)) {
			if(ruleMap.containsKey(RuleTypeEnum.REQUIRED_RULE.value())) {
				// TODO delivery_way && valid_thru
			}
		} else {
			if(ruleMap.containsKey(RuleTypeEnum.DISABLE_RULE.value())) {
				Rule rule = ruleMap.get(RuleTypeEnum.DISABLE_RULE.value());
				DependGroup dependGroup = rule.getDependGroup();
				DependExpress dependExpress = dependGroup.getDependExpressList().get(0);
				String dependId = dependExpress.getFieldId();
				String dependValue = dependExpress.getValue();
				String dependSymbol = dependExpress.getSymbol();
				
				String compareValue = StringUtils.EMPTY;
				
				for(Field newfield : newFieldList) {
					if(dependId.equalsIgnoreCase(newfield.getId())){
						compareValue = valueMap.get(dependId) == null? compareValue : valueMap.get(dependId);
					}
				}
				
				if(DependExpress.SYMBOL_NOT_EQUALS.equals(dependSymbol)) {
					if(!dependValue.equalsIgnoreCase(compareValue)) {
						value = StringUtils.EMPTY;
					}
				}else if(DependExpress.SYMBOL_NOT_CONTAINS.equals(dependSymbol)){
					// TODO locality_life
				}
			}
		}
			
		return value;
    }
    
    public static String getDefaultValue(Field field) throws TopSchemaException {
    	Element fieldElm = field.toElement();
	    Element defaultValueEle = XmlUtils.getChildElement(fieldElm, "default-value");
	    String dvalue = StringUtils.EMPTY;
	    if(defaultValueEle != null){
			dvalue = defaultValueEle.getText();
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
			for(Element ele : fieldsEleList){
				Field elementToField = SchemaReader.elementToField(ele);
				fieldList.add(elementToField);
			}
		}
		return fieldList;
    }
    
    public static String getMaterialPropName(String s) {
    	Pattern p = Pattern.compile("[0-9]");
		Matcher m = p.matcher(s);
		if(m.find()) {
			return s.substring(0, m.start());
		} else {
			return StringUtils.EMPTY;
		}
    }
    
    public static String getMaterialPropContent(String s) {
    	Pattern p = Pattern.compile("[0-9]");
		Matcher m = p.matcher(s);
		if(m.find()) {
			return s.substring(m.start());
		} else {
			return StringUtils.EMPTY;
		}
    }

	@NoTransaction
	public static void doShopCarry() {
        String json = request.params.data.get("body")[0];
        JSONObject data = JSON.parseObject(json);
        String ww = data.getString("ww");
        JSONArray items = data.getJSONArray("items");
        if (items.isEmpty()) {
            renderJSON(JsonUtil.getJson(new TMResult(false, "未查询到可添加的宝贝 店铺旺旺：" + ww, null)));
        }
        User user = getUser();
		boolean isValid = UserDao.doValid(user);
		if(!isValid) {
			renderJSON(JsonUtil.getJson(new TMResult(false, "用户已过期，请重新授权或续订软件！", null)));
		}
		
		if (StringUtils.isEmpty(ww) == true) {
			renderJSON(JsonUtil.getJson(new TMResult(false, "请输入正确的旺旺号", null)));
		}
		ww = StringUtils.trim(ww);
		
		if (CarrierTask.findByWW(ww, user.getUserNick()) == true) {
			renderJSON(JsonUtil.getJson(new TMResult(false, "您在1个月内已复制过该店宝贝，请不要重复提交", null)));
		}

        ItemCarryCustom itemCarryCustom = ItemCarryCustom.findByUserId(user.getIdlong());
        Long itemCarryCustomId = 0L;
        if (itemCarryCustom != null) {
            itemCarryCustomId = itemCarryCustom.getId();
        }

        renderJSON(JsonUtil.getJson(CarrierTaskAction.addShopBatchCarrierTask(user, ww, items, user.getUserNick(), itemCarryCustomId)));
	}

    @NoTransaction
    public static void taskList(int pn, int ps, boolean isDesc) {

        User user = getUser();
        PageOffset po = new PageOffset(pn, ps, 10);
        List<CarrierTask> list = CarrierTask.fetchUnfinishedListByUserNick(user.getUserNick(), po, isDesc);
        renderJSON(JsonUtil.getJson(new TMResult(list, CarrierTask.countCurrentTaskByNick(user.getUserNick()), po)));
    }

	@NoTransaction
	public static void adminTaskList(String userNick, int pn, int ps, boolean isDesc) {
		PageOffset po = new PageOffset(pn, ps, 10);
		List<CarrierTask> list = CarrierTask.fetchUnfinishedListByUserNick(userNick, po, isDesc);
		renderJSON(JsonUtil.getJson(new TMResult(list, CarrierTask.countCurrentTaskByNick(userNick), po)));
	}

    @NoTransaction
    public static void historyTaskList(int pn, int ps, boolean isDesc) {
        User user = getUser();
        PageOffset po = new PageOffset(pn, ps, 10);
        List<CarrierTask> list = CarrierTask.fetchFinishedListByUserNick(user.getUserNick(), po, isDesc);
        renderJSON(JsonUtil.getJson(new TMResult(list, CarrierTask.countHistoryTaskByNick(user.getUserNick()), po)));
    }

	@NoTransaction
	public static void adminHistoryTaskList(String userNick, int pn, int ps, boolean isDesc) {
		PageOffset po = new PageOffset(pn, ps, 10);
		List<CarrierTask> list = CarrierTask.fetchFinishedListByUserNick(userNick, po, isDesc);
		renderJSON(JsonUtil.getJson(new TMResult(list, CarrierTask.countHistoryTaskByNick(userNick), po)));
	}

	@NoTransaction
    public static void showTaskDetail(long taskId, int pn, int ps) {

        PageOffset po = new PageOffset(pn, ps, 10);
        List<SubCarrierTask> tasks = SubCarrierTask.findListByTaskId(taskId, po);
        renderJSON(JsonUtil.getJson(new TMResult(tasks, SubCarrierTask.countByTaskId(taskId), po)));
    }

	@NoTransaction
	public static void doBatchCarry(String content) {
		User user = getUser();
		boolean isValid = UserDao.doValid(user);
		if(!isValid) {
			renderJSON(JsonUtil.getJson(new TMResult(false, "用户已过期，请重新授权或续订软件！", null)));
		}
		if (content == null) {
			renderError("传入宝贝ID或链接不能为空");
		}
		// 检验宝贝url是否正确
        List<String> urls = Arrays.asList(content.split("\n"));
        for (String url : urls) {
            Long numId = CarrierTaskAction.getNumId(url);
            if (numId == null || numId <= 0) {
                renderJSON(JsonUtil.getJson(new TMResult(false, "请输入有效的需要拷贝宝贝ID 或 宝贝网址！" + url, null)));
            }
        }

		ItemCarryCustom itemCarryCustom = ItemCarryCustom.findByUserId(user.getIdlong());
		Long itemCarryCustomId = 0L;
		if (itemCarryCustom != null) {
            itemCarryCustomId = itemCarryCustom.getId();
        }

		renderJSON(JsonUtil.getJson(CarrierTaskAction.addBatchCarrierTask(user, content, user.getUserNick(), itemCarryCustomId)));
	}

	public static void rebootTask(Long taskId) {
		SubCarrierTask.clearSubTaskByTaskId(taskId);
		Boolean success = CarrierTask.rebootTask(taskId);

		if (success)
			renderSuccessJson();
		else
			renderError("重启任务失败");
	}
	
	public static void cancelTask(Long taskId) {
		SubCarrierTask.cancelSubTaskByTaskId(taskId);
		Boolean success = CarrierTask.cancelTask(taskId);

		if (success)
			renderSuccessJson();
		else
			renderError("取消任务失败");
	}

	public static void deleteTask(Long taskId) {
		SubCarrierTask.clearSubTaskByTaskId(taskId);
		Boolean success = CarrierTask.deleteTask(taskId);

		if (success)
			renderSuccessJson();
		else
			renderError("删除任务失败");
	}

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	public static void failTaskList(String startTime, String endTime, int pn, String errorInfoKey, Long taskId, String goodUrl, String publisher, int taskStatus, Integer subTaskType) {
		PageOffset po = new PageOffset(pn, 10);
		Long startTs;
		Long endTs;
		try {
			startTs = sdf.parse(startTime).getTime();
		} catch (ParseException e) {
			startTs = System.currentTimeMillis() - DateUtil.DAY_MILLIS * 3;
		}
		try {
			endTs = sdf.parse(endTime).getTime() + DateUtil.DAY_MILLIS - 1;
		} catch (ParseException e) {
			endTs = new Date().getTime();
		}

		List<SubCarrierTask> list = SubCarrierTask.findBySearchRules(startTs, endTs, errorInfoKey, taskId, goodUrl, publisher, taskStatus, subTaskType, po);
		int count = SubCarrierTask.countBySearchRules(startTs, endTs, errorInfoKey, taskId, goodUrl, publisher, subTaskType, taskStatus);

		renderJSON(JSON.toJSONString(new TMResult(list, count, po), SerializerFeature.WriteEnumUsingToString));
	}

	public static void rebootById(Long... ids) {
		if (ids == null || ids.length == 0) renderJSON(new TMResult(false, "没有选中项", null));
		for (Long id : ids) {
			SubCarrierTask subCarrierTask = SubCarrierTask.findById(id);
			Long taskId = subCarrierTask.getTaskId();
			if (subCarrierTask.getStatus() == SubCarrierTask.SubCarrierTaskStatus.failure) CarrierTask.reduceFinishCnt(taskId);
			SubCarrierTask.rebootById(id);
		}

		renderJSON(new TMResult(true, "重启成功", null));
	}
	
	// 卖家保存吊牌图
	public static void saveDiaoPaiPic(File file) {
		User user = getUser();
		
		String picPath = file.getAbsolutePath();
		if (StringUtils.isEmpty(picPath)) {
			renderError("图片读取失败，请刷新重试或者联系我们！");
		}
		TMResult<Picture> result = FileCarryUtils.newUploadPicFromLocal(user, picPath, null);
		if(!result.isOk()) {
			renderError(result.getMsg());
		}
		Picture picture = result.getRes();
		renderSuccess("吊牌图上传成功！", picture);
	}

	// 卖家保存宝贝复制自定义模板
	public static void saveItemCarryCustom() {
		String json = request.params.data.get("body")[0];
		ItemCarryCustom itemCarryCustom = JSON.parseObject(json, ItemCarryCustom.class);

		// 卖家ID
		User user = getUser();
		itemCarryCustom.setUserId(user.getId());
		
		// 吊牌图
		if(!StringUtils.isEmpty(itemCarryCustom.getDiaopaiPic())) {
			TMResult<Picture> result = FileCarryUtils.newUploadPicFromOnline(user, itemCarryCustom.getDiaopaiPic());
			if(result.isOk()) {
				itemCarryCustom.setDiaopaiPic(result.getRes().getPicturePath());
			}
		}

		// 是否存在相同的模板
		Long id = itemCarryCustom.findId();
		Boolean success;
		if (id > 0) {
			itemCarryCustom.setId(id);
			itemCarryCustom.setCreateTs(System.currentTimeMillis());
			success = itemCarryCustom.rawUpdate();
		} else {
			success = itemCarryCustom.rawInsert();
		}
		if (success) renderSuccess("保存成功", null);
		else renderError("保存失败");
	}

	// 卖家宝贝自定义模板获取
	public static void userItemCarryCustom() {
		ItemCarryCustom itemCarryCustom = ItemCarryCustom.findByUserId(getUser().getIdlong());

		renderJSON(utils.JsonUtil.toJson(itemCarryCustom));
	}

	// 自定义模板中图片分类获取
	public static void getPicCats() {
		List<PictureCategory> pictureCategoryList = new PicApi.PicCatsGet(getUser(), null).call();
		if (pictureCategoryList == null) renderError("获取图片分类失败");

		List parentList = new ArrayList();
        Map<Long, List> childListMap = new HashMap<Long, List>();
		for (PictureCategory pictureCategory : pictureCategoryList) {
            Long parentId = pictureCategory.getParentId();
            if (parentId == 0L) {
                parentList.add(pictureCategory);
            } else {
		        List childList = childListMap.get(parentId);
		        if (childList == null) {
		            childList = new ArrayList();
		            childListMap.put(parentId, childList);
                }
		        childList.add(pictureCategory);
            }
        }

		Map result = new HashMap();
		result.put("pictureCategoryParent", parentList);
		result.put("pictureCategoryChild", childListMap);

        renderSuccess("获取图片分类", utils.JsonUtil.toJson(result));
	}

	// 自定义模板中省份城市数据获取
	public static void itemArea() {
		List<ItemArea> itemAreas = ItemArea.getAll();

		List parentList = new ArrayList();
		Map<Integer, List> childListMap = new HashMap<Integer, List>();
		for (ItemArea itemArea : itemAreas) {
			Integer parentId = itemArea.getParentId();
			if (parentId == 0) {
				parentList.add(itemArea);
			} else {
				List childList = childListMap.get(parentId);
				if (childList == null) {
					childList = new ArrayList();
					childListMap.put(parentId, childList);
				}
				childList.add(itemArea);
			}
		}

		Map map = new HashMap();
		map.put("province", parentList);
		map.put("city", childListMap);

		renderJSON(map);
	}

	// 自定义模板中运费模板获取
	public static void deliveryTemplates() {
		DeliveryApi.DeliveryTemplatesGet deliveryTemplatesGet = new DeliveryApi.DeliveryTemplatesGet(getUser());
		List<DeliveryTemplate> deliveryTemplates = deliveryTemplatesGet.call();

		renderJSON(deliveryTemplates);
	}

	// 自定义模板中宝贝分类数据获取
	public static void sellerCatsList() {
		List<SellerCat> list = TaobaoUtil.getSellerCatByUserId(getUser());
		if (list == null) renderError("获取宝贝分类失败");

		List parentList = new ArrayList();
		Map<Long, List> childListMap = new HashMap<Long, List>();
		for (SellerCat sellerCat : list) {
			Long parentCid = sellerCat.getParentCid();
			if (parentCid == 0L) parentList.add(sellerCat);
			else {
				List childList = childListMap.get(parentCid);
				if (childList == null) {
					childList = new ArrayList();
					childListMap.put(parentCid, childList);
				}
				childList.add(sellerCat);
			}
		}

		Map result = new HashMap();
		result.put("sellerCatsParent", parentList);
		result.put("sellerCatsChild", childListMap);

		renderSuccess("获取宝贝分类成功", utils.JsonUtil.toJson(result));
	}

	// 清理默认图片分类缓存
	public static void cleanCatId(String nick) {
		if(StringUtils.isEmpty(nick)) {
			renderError("nick为空");
		}
		User user = UserDao.findByUserNick(nick);
		if(user == null) {
			renderError("用户【" + nick + "】不存在");
		}
		boolean success = PicApi.removePicCat(user);
		if(!success) {
			renderError("操作失败");
		}
		renderSuccess("操作成功", "");
	}

}