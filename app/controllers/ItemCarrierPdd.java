package controllers;

import models.itemCopy.ItemCatProps;
import models.itemCopy.dto.PddItemDto;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;

import com.taobao.api.domain.Item;

import dto.eslexicon.ESResult;
import actions.alibaba.CopyToTmallAction;
import actions.alibaba.ItemCopyAction;
import actions.pdd.CopyToPddAction;
import play.db.jpa.NoTransaction;
import result.TMResult;
import utils.CommonUtil;
import utils.CopyUtil;
import utils.JsoupUtil;

public class ItemCarrierPdd extends TMController {
	
	/**
	 * 登录
	 * @param userName 用户名
	 * @param password 密码
	 * @param verificationCode 手机验证码（非必须）
	 */
	@NoTransaction
	public static void loginGetInfo(String userName,String password,String verificationCode){
		if (CommonUtil.isNullOrEmpty(userName)||CommonUtil.isNullOrEmpty(password)) {
			renderError("用户名与密码不可空！");
		}
		
		try {
			TMResult result=CopyToPddAction.login(userName, password,"");
			renderJSON(result);
		} catch (Exception e) {
			e.printStackTrace();
			renderError("系统内部异常，请联系客服处理！");
		
		}
	}
	
	
	@NoTransaction
	public static void initShipTemplate(String passId,String mallId){
		String content=CopyToPddAction.getShipTemplate(passId,mallId);
		renderJSON(content);
	}
	
	@NoTransaction
	public static void initMainCat(String mallId,String passId){
		String content=CopyToPddAction.getMainCat(mallId,passId);
		renderJSON(content);
	}
	
	//获取分级类目
	@NoTransaction
	public static void initSonCat(String mallId,String passId,int level,long parentId){
		String content=CopyToPddAction.getSonCat(mallId, passId, level, parentId);
		renderJSON(content);
	}
	
	/**
	 * 复制商品
	 * 'passId':passId,
				'payment':payment,
				'backGood':backGood,
				'goodType':goodType,
				'promisePost':promisePost
	 */
	public static void copyItem(String url, Long dtId,
			String oldTitle, String newTitle, Integer priceWay,Double priceVal,
			Integer itemCat, Integer payment, Integer backGood,Integer goodType,Integer promisePost,String passId,Integer isPublish) {
		// 再次判断URL是否正确
		if (StringUtils.isEmpty(url)) {
			renderJSON(new ESResult(false, "宝贝链接不正确！"));
		}
		// 转换Url
//		url ="http://hws.m.taobao.com/cache/wdetail/5.0/?id=" + CopyUtil.parseTbAndTmItemId(url);;
//		try {
//			Jsoup.connect(url).userAgent(JsoupUtil.Default_UA).get();
//		} catch (Exception e) {
//			log.error("url from alibaba is not valid!!!" + e.getMessage());
//			renderJSON(new ESResult(false, "宝贝链接不正确或者网络异常！请用浏览器访问后再尝试复制！<br><br><a href='" + url
//	                + "' target='_blank'>"
//	                + url + "</a>"));
//		}
		
		PddItemDto item=new PddItemDto();
		item.setCat_id(itemCat); //类目
		item.setCost_template_id(dtId.toString()); //运费模板
		item.setGoods_type(goodType.toString()); 	//商品类型 进口2|普通1
		item.setIs_refundable(backGood); //七天无理由退换货  是-1 否-0
		item.setIs_folt(payment.toString()); //假一赔十
		
		TMResult result=CopyToPddAction.doCarrier(item,url,oldTitle,newTitle,priceWay,priceVal,passId,isPublish);
		//是否预售 是-1 否-0
		
		renderJSON(result);
		
		
	}


}
