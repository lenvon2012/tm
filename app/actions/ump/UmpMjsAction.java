package actions.ump;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import job.ump.UmpMjsTmplUpdateJob;
import models.promotion.TMProActivity;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import result.TMResult;
import tbapi.ump.UMPApi.UmpMjsActivityDelete;
import tbapi.ump.UMPApi.UmpRangeAdd;
import tbapi.ump.UMPApi.UmpRangeDelete;
import actions.RelationAction;
import actions.TaobaoCDNPicAction;
import actions.TaobaoCDNPicAction.CDNPicMsg;
import actions.ump.PromotionResult.PromotionActionType;
import actions.ump.PromotionResult.PromotionErrorMsg;
import bustbapi.ItemApi;
import bustbapi.ItemApi.ItemUpdate;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.HtmlUtil;

import controllers.UmpPromotion;
import dao.item.ItemDao;

public class UmpMjsAction {

    /**
     * 满就送活动的添加宝贝和删除宝贝，以及修改活动
     */
    
    private static final Logger log = LoggerFactory.getLogger(UmpMjsAction.class);
    
    
    public static PromotionResult doAddMsjItems(User user, TMProActivity tmActivity, Set<Long> numIidSet) {
        
        if (CommonUtils.isEmpty(numIidSet)) {
            return new PromotionResult(false, "请先选择要加入满就送活动宝贝！");
        }
        
        
        Long mjsActivityId = tmActivity.getMjsActivityId();
        if (mjsActivityId == null || mjsActivityId <= 0) {
            return new PromotionResult(false, "系统出现异常，找不到满就送活动ID，请联系我们！");
        }
        
        //添加range
        UmpRangeAdd rangeAddApi = new UmpRangeAdd(user, mjsActivityId, numIidSet);
        TMResult rangeAddRes = rangeAddApi.call();
        
        Set<Long> successNumIidSet = new HashSet<Long>();
        List<PromotionErrorMsg> errorList = new ArrayList<PromotionErrorMsg>();
        
        if (rangeAddRes == null || rangeAddRes.isOk() == false || rangeAddApi.isApiSuccess() == false) {
            
            for (Long numIid : numIidSet) {
                PromotionErrorMsg promotionError = PromotionResult.createPromotionError(rangeAddApi, numIid);
                if (StringUtils.isEmpty(promotionError.getErrorMsg())) {
                    promotionError.setErrorMsg("系统异常，添加满就送宝贝失败，请联系我们！");
                }
                errorList.add(promotionError);
            }
            
        } else {
            //这里的问题是，如果同时操作同一个活动，会丢失数据。。
            for (Long numIid : numIidSet) {
                tmActivity.addMjsItemNumIid(numIid);
                successNumIidSet.add(numIid);
                // 将满就算模板添加进宝贝详情页
                // UmpMjsAction.addItemMjsTmpl(user, numIid, tmActivity.getTmplHtml());
                UmpMjsTmplUpdateJob.addTmpl(user, numIid, tmActivity.getTmplHtml(), tmActivity.getId(), false);
            }
            tmActivity.setStatus(TMProActivity.ActivityStatus.ACTIVE);
            tmActivity.jdbcSave();
        }
        
        
        int notExecuteNum = 0;
        
        PromotionResult promotionRes = new PromotionResult(user, PromotionActionType.AddMjsItems, 
                successNumIidSet, notExecuteNum, errorList);
        
        return promotionRes;
        
    }
    
    
    public static PromotionResult deleteSomeMsjItems(User user, TMProActivity tmActivity, 
            Set<Long> numIidSet) {
        
        if (CommonUtils.isEmpty(numIidSet)) {
            return new PromotionResult(false, "请先选择要取消满就送活动的宝贝！");
        }
        
        
        Long mjsActivityId = tmActivity.getMjsActivityId();
        if (mjsActivityId == null || mjsActivityId <= 0) {
            return new PromotionResult(false, "系统出现异常，找不到满就送活动ID，请联系我们！");
        }
        
        
        //删除range
        UmpRangeDelete rangeDeleteApi = new UmpRangeDelete(user, mjsActivityId, numIidSet);
        Boolean deleteRes = rangeDeleteApi.call();
        
        Set<Long> successNumIidSet = new HashSet<Long>();
        List<PromotionErrorMsg> errorList = new ArrayList<PromotionErrorMsg>();
        
        if (deleteRes == null || deleteRes.booleanValue() == false || rangeDeleteApi.isApiSuccess() == false) {
            
            for (Long numIid : numIidSet) {
                PromotionErrorMsg promotionError = PromotionResult.createPromotionError(rangeDeleteApi, 
                        numIid);
                if (StringUtils.isEmpty(promotionError.getErrorMsg())) {
                    promotionError.setErrorMsg("系统异常，删除满就送宝贝失败，请联系我们！");
                } 
                
                errorList.add(promotionError);
            }
            
        } else {
            
            //这里的问题是，如果同时操作同一个活动，会丢失数据。。
            for (Long numIid : numIidSet) {
                tmActivity.removeMjsItemNumIid(numIid);
                successNumIidSet.add(numIid);
                // 将满就算模板添加进宝贝详情页
                // UmpMjsAction.removeItemMjsTmpl(user, numIid);
                UmpMjsTmplUpdateJob.addTmpl(user, numIid, tmActivity.getTmplHtml(), tmActivity.getId(), true);
            }
            tmActivity.jdbcSave();
        }
        

        int notExecuteNum = 0;
        
        PromotionResult promotionRes = new PromotionResult(user, PromotionActionType.DeleteMjsItems, 
                successNumIidSet, notExecuteNum, errorList);
        
        return promotionRes;
    }
    
    
    /**
     * 删除淘宝的activity，但保留数据库中记录，可以重启
     * @param user
     * @param tmActivity
     * @return
     */
    public static PromotionResult cancelMjsActivity(User user, TMProActivity tmActivity) {
        
        if (tmActivity.isMjsActivity() == false) {
            return new PromotionResult(false, "系统出现异常，这不是满就送活动，请联系我们！");
        }
        
        //删除淘宝activity
        Long mjsActivityId = tmActivity.getMjsActivityId();
        if (mjsActivityId == null || mjsActivityId <= 0L) {
            return new PromotionResult(false, "系统出现异常，找不到满就送活动ID，请联系我们！");
        }
        
        UmpMjsActivityDelete activityDeleteApi = new UmpMjsActivityDelete(user, mjsActivityId);
        Boolean deleteRes = activityDeleteApi.call();
        // 活动已经被卖家手动在后台删除

        if(!StringUtils.isEmpty(activityDeleteApi.getSubErrorCode()) && 
        		activityDeleteApi.getSubErrorCode().equals("isv.activity-not-exist:activityId")) {
        	tmActivity.setUnActiveStatusAndEndTime();
            
            UmpMjsAction.removeItemsMjsTmpl(user, tmActivity.getItems(), tmActivity.getId());
            tmActivity.jdbcSave();
            return new PromotionResult(true, "满就送活动结束成功！");
        }
        //删除activity后，range不需要删除
        if (deleteRes == null || deleteRes.booleanValue() == false 
                || activityDeleteApi.isApiSuccess() == false) {
            
            String errorMsg = activityDeleteApi.getSubErrorMsg();
            if (StringUtils.isEmpty(errorMsg)) {
                errorMsg = "系统异常，结束满就送活动失败，请联系我们！";
            }
            
            return new PromotionResult(false, errorMsg);
        }
        
        //tmActivity.rawDelete();
        
        //tmActivity.setStatus(ActivityStatus.UNACTIVE);
        tmActivity.setUnActiveStatusAndEndTime();
        
        UmpMjsAction.removeItemsMjsTmpl(user, tmActivity.getItems(), tmActivity.getId());
        tmActivity.jdbcSave();
        
        return new PromotionResult(true, "满就送活动结束成功！");
    }
    
    public static PromotionResult cancelShopMjsActivity(User user, TMProActivity tmActivity) {
        
        if (tmActivity.isShopMjsActivity() == false) {
            return new PromotionResult(false, "系统出现异常，这不是满就送活动，请联系我们！");
        }
        
        //删除淘宝activity
        Long mjsActivityId = tmActivity.getMjsActivityId();
        if (mjsActivityId == null || mjsActivityId <= 0L) {
            return new PromotionResult(false, "系统出现异常，找不到满就送活动ID，请联系我们！");
        }
        
        UmpMjsActivityDelete activityDeleteApi = new UmpMjsActivityDelete(user, mjsActivityId);
        Boolean deleteRes = activityDeleteApi.call();
        
        if(!StringUtils.isEmpty(activityDeleteApi.getSubErrorCode()) && 
        		activityDeleteApi.getSubErrorCode().equals("isv.activity-not-exist:activityId")) {
        	tmActivity.setUnActiveStatusAndEndTime();
            
            UmpMjsAction.removeItemsMjsTmpl(user, ItemDao.findNumIidWithUser(user.getId()), tmActivity.getId());
            tmActivity.jdbcSave();
            return new PromotionResult(true, "全店满就送活动结束成功！");
        }
        //删除activity后，range不需要删除
        if (deleteRes == null || deleteRes.booleanValue() == false 
                || activityDeleteApi.isApiSuccess() == false) {
            
            String errorMsg = activityDeleteApi.getSubErrorMsg();
            if (StringUtils.isEmpty(errorMsg)) {
                errorMsg = "系统异常，结束满就送活动失败，请联系我们！";
            }
            
            return new PromotionResult(false, errorMsg);
        }

        // 删除宝贝详情页的满就送模板
        UmpMjsAction.removeItemsMjsTmpl(user, ItemDao.findNumIidWithUser(user.getId()), tmActivity.getId());
        
        //tmActivity.setStatus(ActivityStatus.UNACTIVE);
        tmActivity.setUnActiveStatusAndEndTime();
        
        tmActivity.jdbcSave();
        
        return new PromotionResult(true, "全店满就送活动结束成功！");
    }
    /*
    public static Boolean addItemMjsTmpl(User user, Long numIid, String tmplHtml, Long activityId) {
    	if(user == null) {
    		return false;
    	}
    	if(numIid == null || numIid <= 0) {
    		return false;
    	}
    	if(StringUtils.isEmpty(tmplHtml)) {
    		return false;
    	}
    	// 更新宝贝详情页
    	String desc = RelationAction.getItemDesc(user, numIid);
    	String newDesc = StringUtils.EMPTY;
    	if(StringUtils.isEmpty(desc)) {
    		newDesc = tmplHtml;
    	} else {
    		newDesc = tmplHtml.trim() + deleteMjsTmpl(desc, activityId);
    	}
    	
    	ItemUpdate api = new ItemApi.ItemUpdate(user.getSessionKey(), numIid, newDesc);
        api.call();
        String errorMsg = api.getErrorMsg();
        // 更新详情页出错
        if(!StringUtils.isEmpty(errorMsg)) {
         	return false;
            	
        }
        return true;
    }*/
    
    public static Boolean removeItemMjsTmpl(User user, Long numIid, Long activityId) {
    	log.info("删除满就送详情页模板: userNick = ["+user.getUserNick()+"], 宝贝Id = ["+numIid+"]," +
    			" 活动ID=["+activityId+"]");
    	if(user == null) {
    		return false;
    	}
    	if(numIid == null || numIid <= 0) {
    		return false;
    	}
    	if (MjsModuleTemplateAction.updateModuleTemplate(user, numIid, 
                "", activityId, true) == true) {
            return true;
        }
    	
    	// 更新宝贝详情页
    	String desc = RelationAction.getItemDesc(user, numIid);
    	String newDesc = StringUtils.EMPTY;
    	if(StringUtils.isEmpty(desc)) {
    		return true;
    	} else {
    		newDesc = deleteMjsTmpl(desc, activityId);
    	}
    	
    	ItemUpdate api = new ItemApi.ItemUpdate(user.getSessionKey(), numIid, newDesc.trim());
        api.call();
        String errorMsg = api.getErrorMsg();
        // 更新详情页出错
        if(!StringUtils.isEmpty(errorMsg)) {
        	log.info("删除满就送详情页模板失败: userNick = ["+user.getUserNick()+"], 宝贝Id = ["+numIid+"]," +
        			" 活动ID=["+activityId+"]");
         	return false;
            	
        }
        return true;
    }
    
    // 替换原来的tmpl
    public static Boolean updateSingleItemMjsTmpl(User user, Long numIid, String tmplHtml,
    		Long activityId) {
    	
    	if(user == null) {
    		return false;
    	}
    	if(numIid == null || numIid <= 0) {
    		return false;
    	}
    	if(StringUtils.isEmpty(tmplHtml)) {
    		return false;
    	}
    	int index = getMjsTmplIndex(tmplHtml);
    	log.info("更新满就送详情页模板: userNick = ["+user.getUserNick()+"], 宝贝Id = ["+numIid+"]," +
    			" 活动ID=["+activityId+"], 模板序号=["+index+"]");
    	
    	if (MjsModuleTemplateAction.updateModuleTemplate(user, numIid, 
    	        tmplHtml, activityId, false) == true) {
    	    return true;
    	}
    	
    	// 更新宝贝详情页
    	String desc = RelationAction.getItemDesc(user, numIid);
    	String newDesc = StringUtils.EMPTY;
    	if(StringUtils.isEmpty(desc)) {
    		newDesc = addMjsTmpl(user, tmplHtml.trim(), activityId);
    	} else {
    		newDesc = addMjsTmpl(user, tmplHtml.trim(), activityId) + deleteMjsTmpl(desc, activityId);
    	}
    	
    	ItemUpdate api = new ItemApi.ItemUpdate(user.getSessionKey(), numIid, newDesc);
        api.call();
        String errorMsg = api.getErrorMsg();
        // 更新详情页出错
        if(!StringUtils.isEmpty(errorMsg)) {
        	log.info("更新满就送详情页模板失败: userNick = ["+user.getUserNick()+"], 宝贝Id = ["+numIid+"]," +
        			" 活动ID=["+activityId+"], 模板序号=["+index+"]");
         	return false;
            	
        }
        return true;
    }
    
	public static int getMjsTmplIndex(String tmplHtml) {
		if (StringUtils.isEmpty(tmplHtml)) {
			return 0;
		}

		if (tmplHtml.indexOf("index=\"0\"") >= 0) {
			return 0;
		} else if (tmplHtml.indexOf("index=\"1\"") >= 0) {
			return 1;
		} else if (tmplHtml.indexOf("index=\"2\"") >= 0) {
			return 2;
		} else if (tmplHtml.indexOf("index=\"3\"") >= 0) {
			return 3;
		} else {
			return 1;
		}
	}
    
    public static String mjsTmplClassNamePre = "mjsTmplClassNamePre_";
    public static String deleteMjsTmpl(String content, Long activityId){
        
        if (StringUtils.isEmpty(content)) {
            return "";
        }
        
    	String toRemoveClass = StringUtils.EMPTY;
    	if(activityId == null || activityId <= 0) {
    		toRemoveClass = "HNMjsTmplHolder";
    	} else {
    		toRemoveClass = mjsTmplClassNamePre + activityId;
    	}
    	
    	Document doc = Jsoup.parse(content);
    	doc.select("." + toRemoveClass).remove();
    	String model =doc.toString();
    	
    	model=model.replace("<html>", "");
    	model=model.replace("<head>", "");
    	model=model.replace("</head>", "");
    	model=model.replace("<body>", "");
    	model=model.replace("</body>", "");
    	model=model.replace("</html>", "");
    	
    	model = HtmlUtil.deleteBlank(model);
    	return model;
    }
    
    public static String addMjsTmpl(User user, String content, Long activityId){
    	if(user == null) {
    		return StringUtils.EMPTY;
    	}
        if (StringUtils.isEmpty(content)) {
            return "";
        }
        
    	String toAddClass = StringUtils.EMPTY;
    	if(activityId == null || activityId <= 0) {
    		toAddClass = "HNMjsTmplHolder";
    	} else {
    		toAddClass = mjsTmplClassNamePre + activityId;
    	}
    	content = getUserCdnImgPath(user, content);
    	Document doc = Jsoup.parse(content);
    	doc.select(".HNMjsTmplHolder").addClass(toAddClass);
    	String model =doc.toString();
    	model=model.replace("<html>", "");
    	model=model.replace("<head>", "");
    	model=model.replace("</head>", "");
    	model=model.replace("<body>", "");
    	model=model.replace("</body>", "");
    	model=model.replace("</html>", "");
    	
    	model = HtmlUtil.deleteBlank(model);
    	return model;
    }
    
    public static String getUserCdnImgPath(User user, String content) {
    	// 如果title是manjiusong1
    	if(content.indexOf("http://img03.taobaocdn.com/bao/uploaded/i3/T1kZWZXj4jXXbaMLs2_043749.jpg") >= 0) {
    		CDNPicMsg msg = TaobaoCDNPicAction.getCDNPicPath(user, "manjiusong1");
    		// 如果找不到cdn图片，则不修改
    		if(msg == null || msg.getSuccess() == false) {
    			return content;
    		}
    		return content.replace("http://img03.taobaocdn.com/bao/uploaded/i3/T1kZWZXj4jXXbaMLs2_043749.jpg", msg.getMsg());
    	}
    	// 如果title是manjiusong2
    	else if(content.indexOf("http://img02.taobaocdn.com/imgextra/i2/414224286/T2E8SAXstaXXXXXXXX-414224286.jpg") >= 0){
    		CDNPicMsg msg = TaobaoCDNPicAction.getCDNPicPath(user, "manjiusong2");
    		// 如果找不到cdn图片，则不修改
    		if(msg == null || msg.getSuccess() == false) {
    			return content;
    		}
    		return content.replace("http://img02.taobaocdn.com/imgextra/i2/414224286/T2E8SAXstaXXXXXXXX-414224286.jpg", msg.getMsg());
    	}
    	return content;
    }
    
    public static boolean updateItemsMjsTmpl(User user, String numIids, String tmplHtml,
    		Long activityId) {
    	if(user == null) {
    		return false;
    	}
    	if(StringUtils.isEmpty(tmplHtml)) {
    		return false;
    	}
    	if(StringUtils.isEmpty(numIids)) {
    		return false;
    	}
    	Set<Long> numIidSet = UmpPromotion.parseIdsToSet(numIids);
    	return updateItemsMjsTmpl(user, numIidSet, tmplHtml, activityId);
    }
    
    public static boolean updateItemsMjsTmpl(User user, Set<Long> numIidSet, String tmplHtml,
    		Long activityId) {
    	if(user == null) {
    		return false;
    	}
    	if(StringUtils.isEmpty(tmplHtml)) {
    		return false;
    	}
    	if(CommonUtils.isEmpty(numIidSet)) {
    		return false;
    	}
		if(numIidSet.size() > 1000) {
			log.info("Holy Shit !!! " + user.getUserNick() + " had submit shop MJS Task " +
					"with item num = " + numIidSet.size() + " and activityId = " + activityId);	
		}
    	for(Long numIid : numIidSet) {
    		//updateSingleItemMjsTmpl(user, numIid, tmplHtml);
    		UmpMjsTmplUpdateJob.addTmpl(user, numIid, tmplHtml, activityId, false);
    	}
    	return true;
    }
    
    public static boolean removeItemsMjsTmpl(User user, String numIids, Long activityId) {
    	if(user == null) {
    		return false;
    	}

    	if(StringUtils.isEmpty(numIids)) {
    		return false;
    	}
    	Set<Long> numIidSet = UmpPromotion.parseIdsToSet(numIids);
    	removeItemsMjsTmpl(user, numIidSet, activityId);
    	return true;
    }
    
    public static boolean removeItemsMjsTmpl(User user, Set<Long> numIidSet, Long activityId) {
    	if(user == null) {
    		return false;
    	}
    	if(CommonUtils.isEmpty(numIidSet)) {
    		return false;
    	}
    	for(Long numIid : numIidSet) {
    		//removeItemMjsTmpl(user, numIid);
    		UmpMjsTmplUpdateJob.addTmpl(user, numIid, "", activityId, true);
    	}
    	return true;
    }
}
