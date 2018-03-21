package controllers;

import actions.alibaba.CopyToTmallAction;
import actions.alibaba.ItemCopyAction;
import actions.carriertask.CarrierTaskAction;
import actions.carriertask.ItemCarrierForDQAction;
import actions.carriertask.ItemCarrierForDQAction.itemCarryResult;
import bustbapi.ItemCopyApiFor1688;
import carrier.FileCarryUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.product.param.AlibabaProductProductInfo;
import com.ciaosir.client.utils.JsonUtil;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.Picture;

import configs.Subscribe;
import dao.UserDao;
import dto.eslexicon.ESResult;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Transient;

import models.carrierTask.CarrierLimitForDQ;
import models.carrierTask.ItemCarryCustom;
import models.carrierTask.CarrierTaskForDQ.CarrierTaskForDQType;
import models.carrierTask.ItemCarryCustomFor1688;
import models.carrierTask.SubCarrierTask;
import models.item.ItemCatPlay;
import models.itemCopy.AliCatMapping;
import models.itemCopy.AlibabaCat;
import models.itemCopy.ItemCatProps;
import models.itemCopy.ItemExt;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.NoTransaction;
import result.TMResult;
import utils.ApiUtil;
import utils.CommonUtil;
import utils.CopyUtil;
import utils.JsoupUtil;
import utils.ToolBy1688;
import utils.oyster.Levenshtein;

/**
 * 获取用户数据初始化界面信息
 * 
 * @author oyster
 * 
 */
public class StoreData extends TMController {

	@Transient
	public static final Logger log = LoggerFactory.getLogger(StoreData.class);

	public static Set<String> colorVal = new HashSet<String>();

	static {
		// colorVal.addAll(Arrays.asList(a))
	}

	/**
	 * 店铺自定义商品类别
	 */
	public static void getSellerCats() {
		User user = getUser();
		renderJSON(ApiUtil.getSellerCats(user.userNick));
	}

	/**
	 * 运费模板
	 */
	public static void getDeliveryTemplates() {
		User user = getUser();
		renderJSON(ApiUtil.getShippingTemplate(user.sessionKey));
	}

	/**
	 * 默认发货地址
	 */
	public static void getDefAddress() {
		User user = getUser();
		renderJSON(ApiUtil.getDefAddress(user.sessionKey));
	}

	/**
	 * 取授权品牌
	 */
	public static void getBrands() {
		if (getUser().isTmall()) {
			renderJSON(ApiUtil.getAuthorizeBrands(getUser().getSessionKey()));
		} else {
			renderJSON("");
		}

	}

	/**
	 * 查询淘宝分类
	 */
	public static void getItemCats(String key) {
		// 获取被授权类目
		if (getUser().isTmall()) {
			renderJSON(ApiUtil.getAuthorizeItemCats(key, getUser()
					.getSessionKey()));
		} else {
			renderJSON(ApiUtil.getItemCats(key));
		}

	}

	/**
	 * 查询淘宝分类
	 */
	public static void getItemCatRate(String catName,String goodName) {
		
		Levenshtein ls=new Levenshtein();
		List<ItemCatPlay> itemCatPlays=null;
		if (play.cache.Cache.get("a")!=null) {
			itemCatPlays=(List<ItemCatPlay>) play.cache.Cache.get("a");
		}else {
			itemCatPlays=ApiUtil.getItemCats("");
			play.cache.Cache.set("a", itemCatPlays);
		}
		
		
		//网页标题和商品名称
		float titleRatio=0f;
		ItemCatPlay titleResult=null;
		
		float nameRatio=0f;
		ItemCatPlay nameResult=null;
		for (ItemCatPlay itemCatPlay : itemCatPlays) {
			float b= ls.getSimilarityRatio(goodName, itemCatPlay.getName());
			if (b>titleRatio) {
				titleRatio=b;
				titleResult=itemCatPlay;
			}
			float c= ls.getSimilarityRatio(catName, itemCatPlay.getName());
			if (c>nameRatio) {
				nameRatio=c;
				nameResult=itemCatPlay;
			}
		}
		if (nameRatio>titleRatio) {
			renderJSON(nameResult);
		}else {
			renderJSON(titleResult);
		}
		
		
	}

//	/**
//	 * 复制商品
//	 * --复制成功：返回主图信息，
//	 * -- 
//	 */
//	public static void copyItem(String url, Integer sellerCat, Long dtId,
//			String oldTitle, String newTitle, Integer priceWay,
//			Double priceVal, Long itemCat, Integer addressWay, Long brand,boolean isAutoMatch) {
//		User user = getUser();
//		// 用户复制次数限制
//		if (user.getVersion() == Subscribe.Version.LL) {
//			Boolean success = CarrierLimitForDQ.checkUserLimit(user.getId());
//			if (!success) {
//				renderJSON(JsonUtil.getJson(new TMResult(false,
//						"已达到当月最大可复制宝贝数，如需继续复制请联系客服升级版本！", null)));
//			}
//		}
//		// 再次判断URL是否正确
//        // 解析得到ID
//		if (url.indexOf("detail.tmall.com")>1||url.indexOf("item.taobao.com")>1) {
//			renderJSON(new ESResult(false,
//					"亲，淘宝天猫宝贝复制还请在 <a href='/kits/itemCarrier' style='text-decoration:underline; color:#0088cc' target='_blank'>这里</a>  使用哦！"));
//		}
//        String numiid=StringUtils.EMPTY;
//        if(StringUtils.isNumeric(url)){
//            numiid=url;
//        }else {
//            numiid=CopyUtil.parseItemId(url);
//        }
//		url = CopyUtil.changeToUrl(String.valueOf(numiid));
//		try {
//			Jsoup.connect(url).userAgent(JsoupUtil.Default_UA).get();
//		} catch (Exception e) {
//			log.error("url from alibaba is not valid!!!" + e.getMessage());
//			renderJSON(new ESResult(false,
//					"宝贝链接不正确或者网络异常！请用浏览器访问后再尝试复制！<br><br><a href='" + url
//							+ "' target='_blank'>" + url + "</a>"));
//		}
//		// 暂时先在这里判断，完成淘宝的schema后删除
//		if (user.isTmall()) {
//			if (itemCat==0) {
//				renderJSON(new ESResult(false,
//						"亲，您还未选择类目呢！"));
//			}
//			TMResult<Item> tmResult = CopyToTmallAction.doCopy(
//					numiid, sellerCat, dtId, oldTitle, newTitle, priceWay,
//					priceVal, itemCat, addressWay, user, false, brand);
//
//			if (tmResult.isOk) {
//				Item item=tmResult.getRes();
////				String itemUrl = tmResult.getMsg();
//				String itemUrl = "https://detail.tmall.com/item.htm?id=" + item.getNumIid();
////				String itemId = tmResult.getRes();
//				String updateUrl = "https://upload.tmall.com/auction/publish/edit.htm?itemNumId="
//						+ item.getNumIid();
//				tmResult.setMsg("拷贝成功，请在仓库中查看！新宝贝地址：<br><a href='" + itemUrl
//						+ "' target='_blank'>" + itemUrl
//						+ "</a><br/><br/><a style='color:blue' href='"
//						+ updateUrl + "' target='_blank'>编辑宝贝</a>");
//			} else {
//				tmResult.setMsg("<b>拷贝失败，请重试或联系客服反馈！</b><br>失败原因："
//						+ tmResult.getMsg());
//			}
//
//			ESResult result = new ESResult(tmResult.isOk(), tmResult.getMsg());
//
//			// 更新无线描述
//			renderJSON(result);
//		}
//		long aliCid=0l;
//		try {
//			//设置商品类目
//			if (isAutoMatch||itemCat<=0) {
//				aliCid = ToolBy1688.getCatIdFor1688(url);
//					AliCatMapping mapping=AliCatMapping.getMappingByAliCid(aliCid);
//				if (mapping!=null) {
//					itemCat=mapping.getTbcid();
//				}else {
//					//无匹配记录则通过引擎匹配最优解
//					//bbn29
//					String pageTitle=AlibabaCat.getWholeCatName(aliCid);//ToolBy1688.getPageTitle();
//					itemCat=ItemCopyAction.matchCidByTitle(pageTitle);
//				}
//			}
//			TMResult<Item> tmResult = ItemCopyAction.doCopyItem(numiid, sellerCat,
//					dtId, oldTitle, newTitle, priceWay, priceVal, itemCat,
//					addressWay, user, false, false);
//
//			log.warn("request url:" + url + " to cid :" + itemCat);
//			if (!tmResult.isOk()) {
//				// 如果属性出错，删除数据库中的CID数据
//				if (tmResult.getMsg().contains("宝贝销售属性出错")) {
//					// 部分类目下销售属性不支持自定义造成,重新请求复制，二次请求复制，排除不存在的自定义属性
//					log.warn("first copyAction error,errorMsg:"
//							+ tmResult.getMsg()
//							+ " try copy agian without sku info");
//					tmResult = ItemCopyAction.doCopyItem(url, sellerCat, dtId,
//							oldTitle, newTitle, priceWay, priceVal, itemCat,
//							addressWay, user, true, false);
//					if (tmResult.getMsg().contains("属性出错")) {
//						log.error("second copyAction error,errorMsg:"
//								+ tmResult.getMsg());
//						log.error("try to delete ItemCatProps");
//						ItemCatProps.deleteByCid(itemCat);
//					}
//				} else if (tmResult.getMsg().contains("自定义属性是非销售属性")) {
//					log.warn("first copyAction error,errorMsg:"
//							+ tmResult.getMsg());
//					tmResult.setMsg("当前类目下部分销售属性不支持自定义，请更换类目后重新复制.");
//				} else if (tmResult.getMsg().contains("宝贝所在的类目的不存在")) {
//					// 删除当前失效类目
//					log.warn("first copyAction error,errorMsg:"
//							+ tmResult.getMsg());
//					log.warn("try to delete itemcat");
//					// ItemCatPlay.delByCid(itemCat);
//				}
//			}
//
//			if (tmResult.isOk) {
//				Item item = tmResult.getRes();
//				Long numIid = item.getNumIid();
//				String itemUrl = "http://item.taobao.com/item.htm?id=" + numIid;
//				String updateUrl = "https://upload.taobao.com/auction/publish/edit.htm?itemNumId="
//						+ numIid;
//				tmResult.setMsg("拷贝成功，请在仓库中查看！新宝贝地址：<br><a href='" + itemUrl
//						+ "' target='_blank'>" + itemUrl
//						+ "</a><br/><br/><a style='color:blue' href='"
//						+ updateUrl + "' target='_blank'>编辑宝贝</a>");
//				
//				//保存类目关联映射
//				long aliItemId=Long.parseLong(CopyUtil.parseItemId(url));
//				log.info(String.format("save maping relation: alicid--%s,tbcid--%s,aliItemId--%s,tbItemId--%s",aliCid,itemCat,aliItemId,numIid));
//				boolean result=AliCatMapping.formatObj(aliCid,itemCat, aliItemId, numIid).rawInsert();
//				log.info("~~~~~~~~~~~add mapping result:"+result);
//				
//			} 
//			
//
//			ESResult result = new ESResult(tmResult.isOk(), tmResult.getMsg());
//
//			renderJSON(result);
//		} catch (Exception e) {
//			log.error(e.getMessage());
//			e.printStackTrace();
//			SubCarrierTask.recordError(url, user.getUserNick(), e.toString(),
//					SubCarrierTask.SubTaskType.$1688复制);
//			renderJSON(new TMResult(false, e.toString(), null));
//		}
//
//	}
	
	/**
	 * 复制商品
	 * --复制成功：返回主图信息，
	 * -- 
	 */
	public static void copyItem(String url,Long cid,Long brand,boolean isAutoMatch) {
		User user = getUser();
		// 用户复制次数限制
		if (user.getVersion() == Subscribe.Version.LL) {
			Boolean success = CarrierLimitForDQ.checkUserLimit(user.getId());
			if (!success) {
				renderJSON(JsonUtil.getJson(new TMResult(false,
						"已达到当月最大可复制宝贝数，如需继续复制请联系客服升级版本！", null)));
			}
		}
		// 再次判断URL是否正确
        // 解析得到ID
		if (url.indexOf("detail.tmall.com")>1||url.indexOf("item.taobao.com")>1) {
			renderJSON(new ESResult(false,
					"亲，淘宝天猫宝贝复制还请在 <a href='/kits/itemCarrier' style='text-decoration:underline; color:#0088cc' target='_blank'>这里</a>  使用哦！"));
		}
        String numiid=StringUtils.EMPTY;
        if(StringUtils.isNumeric(url)){
            numiid=url;
        }else {
            numiid=CopyUtil.parseItemId(url);
        }
		url = CopyUtil.changeToUrl(String.valueOf(numiid));
		try {
			Jsoup.connect(url).userAgent(JsoupUtil.Default_UA).get();
		} catch (Exception e) {
			log.error("url from alibaba is not valid!!!" + e.getMessage());
			renderJSON(new ESResult(false,
					"宝贝链接不正确或者网络异常！请用浏览器访问后再尝试复制！<br><br><a href='" + url
							+ "' target='_blank'>" + url + "</a>"));
		}
		// 暂时先在这里判断，完成淘宝的schema后删除
		if (user.isTmall()) {
			if (cid==0) {
				renderJSON(new ESResult(false,
						"亲，您还未选择类目呢！"));
			}
			TMResult<Item> tmResult = CopyToTmallAction.doCopy(
					numiid, cid, user, false, brand);

			if (tmResult.isOk) {
				Item item=tmResult.getRes();
//				String itemUrl = tmResult.getMsg();
				String itemUrl = "https://detail.tmall.com/item.htm?id=" + item.getNumIid();
//				String itemId = tmResult.getRes();
				String updateUrl = "https://upload.tmall.com/auction/publish/edit.htm?itemNumId="
						+ item.getNumIid();
				tmResult.setMsg("拷贝成功，请在仓库中查看！新宝贝地址：<br><a href='" + itemUrl
						+ "' target='_blank'>" + itemUrl
						+ "</a><br/><br/><a style='color:blue' href='"
						+ updateUrl + "' target='_blank'>编辑宝贝</a>");
			} else {
				tmResult.setMsg("<b>拷贝失败，请重试或联系客服反馈！</b><br>失败原因："
						+ tmResult.getMsg());
			}

			ESResult result = new ESResult(tmResult.isOk(), tmResult.getMsg());

			// 更新无线描述
			renderJSON(result);
		}
		long aliCid=0l;
		try {
			//设置商品类目
			if (isAutoMatch||cid<=0) {
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
			TMResult<Item> tmResult = ItemCopyAction.doCopyItem(numiid, cid, user, false, false);

			log.warn("request url:" + url + " to cid :" + cid);
			if (!tmResult.isOk()) {
				// 如果属性出错，删除数据库中的CID数据
				if (tmResult.getMsg().contains("宝贝销售属性出错")) {
					// 部分类目下销售属性不支持自定义造成,重新请求复制，二次请求复制，排除不存在的自定义属性
					log.warn("first copyAction error,errorMsg:"
							+ tmResult.getMsg()
							+ " try copy agian without sku info");
					tmResult = ItemCopyAction.doCopyItem(url, cid, user, true, false);
					if (tmResult.getMsg().contains("属性出错")) {
						log.error("second copyAction error,errorMsg:"
								+ tmResult.getMsg());
						log.error("try to delete ItemCatProps");
						ItemCatProps.deleteByCid(cid);
					}
				} else if (tmResult.getMsg().contains("自定义属性是非销售属性")) {
					log.warn("first copyAction error,errorMsg:"
							+ tmResult.getMsg());
					tmResult.setMsg("当前类目下部分销售属性不支持自定义，请更换类目后重新复制.");
				} else if (tmResult.getMsg().contains("宝贝所在的类目的不存在")) {
					// 删除当前失效类目
					log.warn("first copyAction error,errorMsg:"
							+ tmResult.getMsg());
					log.warn("try to delete itemcat");
					// ItemCatPlay.delByCid(itemCat);
				}
			}

			if (tmResult.isOk) {
				Item item = tmResult.getRes();
				Long numIid = item.getNumIid();
				String itemUrl = "http://item.taobao.com/item.htm?id=" + numIid;
				String updateUrl = "https://upload.taobao.com/auction/publish/edit.htm?itemNumId="
						+ numIid;
				tmResult.setMsg("拷贝成功，请在仓库中查看！新宝贝地址：<br><a href='" + itemUrl
						+ "' target='_blank'>" + itemUrl
						+ "</a><br/><br/><a style='color:blue' href='"
						+ updateUrl + "' target='_blank'>编辑宝贝</a>");
				
				//保存类目关联映射
				long aliItemId=Long.parseLong(CopyUtil.parseItemId(url));
				log.info(String.format("save maping relation: alicid--%s,tbcid--%s,aliItemId--%s,tbItemId--%s",aliCid,cid,aliItemId,numIid));
				boolean result=AliCatMapping.formatObj(aliCid,cid, aliItemId, numIid).rawInsert();
				log.info("~~~~~~~~~~~add mapping result:"+result);
				
			} 
			

			ESResult result = new ESResult(tmResult.isOk(), tmResult.getMsg());

			renderJSON(result);
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
			SubCarrierTask.recordError(url, user.getUserNick(), e.toString(),
					SubCarrierTask.SubTaskType.$1688复制);
			renderJSON(new TMResult(false, e.toString(), null));
		}

	}
	
	
	/**
	 * 上传吊牌图文件
	 * @param file
	 */
	public static void uploadHangTagPic(File file){
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
//		TMResult result=ItemCopyAction.uploadImg(file,getUser());
//		if (result.isOk) {
//			//更新配置项
//			ItemCarryCustomFor1688 itemCarryCustom = ItemCarryCustomFor1688.findByUserId(getUser().getIdlong());
//			itemCarryCustom.setHangtagUrl(result.getMsg());
//			itemCarryCustom.rawUpdate();
//		}
//		renderJSON(result);
	}
	//处理批量复制
	@NoTransaction
	public static void doBatchCarry(String url,Long brand,Long cid) {
		User user = getUser();
		boolean isValid = UserDao.doValid(user);
		if(!isValid) {
			renderJSON(JsonUtil.getJson(new TMResult(false, "用户已过期，请重新授权或续订软件！", null)));
		}
		if (url == null) {
			renderError("传入宝贝ID或链接不能为空");
		}
		// 检验宝贝url是否正确
        List<String> urls = Arrays.asList(url.split("\n"));
        for (String itemUrl : urls) {
            String numiid ="";
            if(StringUtils.isNumeric(itemUrl)){
                numiid=itemUrl;
            }else {
                numiid=CopyUtil.parseItemId(itemUrl);
            }
            if (numiid == "") {
                renderJSON(JsonUtil.getJson(new TMResult(false, "请输入有效的需要拷贝宝贝ID 或 宝贝网址！" + url, null)));
            }
        }

		ItemCarryCustomFor1688 itemCarryCustom = ItemCarryCustomFor1688.findByUserId(user.getIdlong());
		Long itemCarryCustomId = 0L;
		if (itemCarryCustom != null) {
            itemCarryCustomId = itemCarryCustom.getId();
        }

		renderJSON(JsonUtil.getJson(CarrierTaskAction.addBatchCarrierTask1688(user, url, user.getUserNick(), itemCarryCustomId,cid,brand)));
	}
	
	@NoTransaction
	public static void testCopy(long numiId){
		AlibabaProductProductInfo productInfo=ToolBy1688.getCatIdFor1688(numiId);
		
		ItemExt item=new ItemExt();
		
		 Date curDate = new Date();
         item.setListTime(curDate);
         item.setModified(curDate);
         item.setHasShowcase(true);
         item.setIsVirtual(false);
         item.setStuffStatus("new");
         item.setType("fixed");
         //数量
         item.setNum(1000l);
         //价格
         Double price=productInfo.getSaleInfo().getPriceRanges()[0].getPrice();
         item.setPrice(price.toString());
       //宝贝标题
 		String title=productInfo.getSubject();
 		//过滤淘宝不被允许的关键字（批发、代理、招商、回收、置换、求购）
 		title=title.replaceAll("求购|批发|置换|回收|代理|招商|专供|代购|养颜|预售|中药|排毒|医用|点痣|祛痣|颈椎病|腰间盘突出|定制|武警|威乐星|宫颈糜烂|炎症|霉菌性阴道炎|盆腔炎|治疗", "");
 		item.setTitle(title);
 		//地址暂无
 		//item.setCid(cid);
		//设置 状态 库存 上架
		item.setApproveStatus("instock");
		
		TMResult<Item> result = ItemCopyApiFor1688.itemCarrier(getUser(), item);
	}

	/**
	 * 商品复制-创建复制任务(1688到淘宝)
	 */
	public static void itemCarry(long numIid, long cid, String sid) {
		if (numIid <= 0) {
			renderError("numIid入参不能为空！！！");
		}
		if (cid <= 0) {
			renderError("CID入参不可空！！！");
		}
		if (StringUtils.isEmpty(sid)) {
			renderError("SID入参不可空！！！");
		}
		itemCarryResult carryResult = ItemCarrierForDQAction.doItemCarry(sid,
				numIid, CarrierTaskForDQType.ALIBABA, cid);
		renderSuccess("", carryResult);
	}
	
	// 卖家保存宝贝复制自定义模板
		public static void saveItemCarryCustom() {
			String json = request.params.data.get("body")[0];
			ItemCarryCustomFor1688 itemCarryCustom = JSON.parseObject(json, ItemCarryCustomFor1688.class);

			User user=getUser();
			// 卖家ID
			Long userId = user.getIdlong();
			itemCarryCustom.setUserId(userId);
			
			// 吊牌图
			if(!StringUtils.isEmpty(itemCarryCustom.getHangtagUrl())) {
				TMResult<Picture> result = FileCarryUtils.newUploadPicFromOnline(user, itemCarryCustom.getHangtagUrl());
				if(result.isOk()) {
					itemCarryCustom.setHangtagUrl(result.getRes().getPicturePath());
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
			ItemCarryCustomFor1688 itemCarryCustom = ItemCarryCustomFor1688.findByUserId(getUser().getIdlong());

			renderJSON(utils.JsonUtil.toJson(itemCarryCustom));
		}

}
