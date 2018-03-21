package controllers;

import java.util.Map;

import models.op.AdBanner;
import models.user.User;
import models.user.UserOP;
import models.user.UserOPVisitCount;

import org.apache.commons.lang.StringUtils;

import com.ciaosir.client.utils.JsonUtil;

import configs.TMConfigs;

public class OPUserInterFace extends TMController{
    
    public static void xufeishowed() {
        User user = getUser();
        long id = UserOP.findExistId(user.getId());
        if(id <= 0l){
            renderText("unshowed");
        } else {
            renderText("showed");
        }
    }
    
    public static void setShowed() {
        User user = getUser();
        long id = UserOP.findExistId(user.getId());
        if(id <= 0l){
            boolean isSuccess = new UserOP(user.getId(),true).jdbcSave();
            if(isSuccess){
                renderText("设置成功");
            }else {
                renderText("设置失败");
            }
        } 
        renderText("已设置");
        /*User user = getUser();
        UserOP userOP = UserOP.findByUserId(user.getId());
        if(userOP == null){
            boolean isSuccess = new UserOP(user.getId(),false,UserOP.Type.OLD_LINK).jdbcSave();
            if(isSuccess){
                renderText("设置成功");
            }else {
                renderText("设置失败");
            }
        } else {
            if(userOP.isOldLinkShowed()){
                renderText("已设置");
            } else {
                userOP.setOldLinkShowed(true);
                renderText("设置成功");
            }
        }*/
    }
    
    public static void set5YuanXufeiShowed(){
        User user = getUser();
        UserOP userOP = UserOP.findByUserId(user.getId());
        if(userOP == null){
            boolean isSuccess = new UserOP(user.getId(),false,UserOP.Type.FIVE_YUAN_XUFEI).jdbcSave();
            if(isSuccess){
                renderText("设置成功");
            }else {
                renderText("设置失败");
            }
        } else {
            if(userOP.is5yuanShowed()){
                renderText("已设置");
            } else {
                userOP.set5YuanXufeiShowed(true);
                userOP.jdbcSave();
                renderText("设置成功");
            }
        }
    }
    
    public static void set3YuanXufeiShowed(){
        User user = getUser();
        UserOP userOP = UserOP.findByUserId(user.getId());
        if(userOP == null){
            boolean isSuccess = new UserOP(user.getId(),false,UserOP.Type.THREE_YUAN_XUFEI).jdbcSave();
            if(isSuccess){
                renderText("设置成功");
            }else {
                renderText("设置失败");
            }
        } else {
            if(userOP.is3yuanShowed()){
                renderText("已设置");
            } else {
                userOP.set3YuanXufeiShowed(true);
                userOP.jdbcSave();
                renderText("设置成功");
            }
        }
    }
    
    public static void setPeixunShowed(){
        User user = getUser();
        UserOP userOP = UserOP.findByUserId(user.getId());
        if(userOP == null){
            boolean isSuccess = new UserOP(user.getId(),false,UserOP.Type.PEIXUN).jdbcSave();
            if(isSuccess){
                renderText("设置成功");
            }else {
                renderText("设置失败");
            }
        } else {
            if(userOP.isPeixunShowed()){
                renderText("已设置");
            } else {
                userOP.setPeixunShowed(true);
                userOP.jdbcSave();
                renderText("设置成功");
            }
        }
    }
    
    public static void setDazheShowed(){
        User user = getUser();
        UserOP userOP = UserOP.findByUserId(user.getId());
        if(userOP == null){
            boolean isSuccess = new UserOP(user.getId(),false,UserOP.Type.DAZHE).jdbcSave();
            if(isSuccess){
                renderText("设置成功");
            }else {
                renderText("设置失败");
            }
        } else {
            if(userOP.isDazheShowed()){
                renderText("已设置");
            } else {
                userOP.setDazheShowed(true);
                userOP.jdbcSave();
                renderText("设置成功");
            }
        }
    }
    
    public static void setold3YuanXufeiShowed(){
        User user = getUser();
        UserOP userOP = UserOP.findByUserId(user.getId());
        if(userOP == null){
            boolean isSuccess = new UserOP(user.getId(),false,UserOP.Type.OLD_THREE_YUAN_XUFEI).jdbcSave();
            if(isSuccess){
                renderText("设置成功");
            }else {
                renderText("设置失败");
            }
        } else {
            if(userOP.isold3yuanShowed()){
                renderText("已设置");
            } else {
                userOP.setold3YuanXufeiShowed(true);
                userOP.jdbcSave();
                renderText("设置成功");
            }
        }
    }
    
    public static void setHaoPingShowed(){
        User user = getUser();
        UserOP userOP = UserOP.findByUserId(user.getId());
        if(userOP == null){
            boolean isSuccess = new UserOP(user.getId(),false,UserOP.Type.FIVE_XING_HAO_PING).jdbcSave();
            if(isSuccess){
                renderText("设置成功");
            }else {
                renderText("设置失败");
            }
        } else {
            if(userOP.isHaoPingShowed()){
                renderText("已设置");
            } else {
                userOP.setHaoPingShowed(true);
                userOP.jdbcSave();
                renderText("设置成功");
            }
        }
    }
    
    public static void setFreeOneMonthShowed(){
        User user = getUser();
        UserOP userOP = UserOP.findByUserId(user.getId());
        if(userOP == null){
            boolean isSuccess = new UserOP(user.getId(),false,UserOP.Type.FREE_ONE_MONTH).jdbcSave();
            if(isSuccess){
                renderText("设置成功");
            }else {
                renderText("设置失败");
            }
        } else {
            if(userOP.isFreeOneMonthShowed()){
                renderText("已设置");
            } else {
                userOP.setFreeOneMonthShowed(true);
                userOP.jdbcSave();
                renderText("设置成功");
            }
        }
    }
    
    public static void freeLink(String key){
        User user = getUser();
        if(user == null){
            renderText("找不到用户名");
        }
        if(user.userNick.isEmpty()){
            renderText("用户名为空");
        }
        String paramStr = StringUtils.EMPTY;
        Map<String, String> newPatamsMap = APIConfig.get().getNewParamStr();
        if(newPatamsMap.isEmpty()){
            renderText("找不到营销链接");
        }
        paramStr = newPatamsMap.get(key);
        String link = StringUtils.EMPTY;
        link = new bustbapi.FuwuApis.SaleLinkGenApi(APIConfig.get().getApiKey(),APIConfig.get().getSecret(),user.userNick,paramStr).call();
        if(link == null || link.isEmpty()){
            renderText("获取营销链接出错");
        }
        renderText(link);
    }
    
    public static void showXufeiOrNot(){
        AdBanner adbanner = AdBanner.findXufeiBanner();
        if(adbanner != null && adbanner.isEnable()){
            renderText("show");
        } else {
            renderText("noshow");
        }
    }
    
    public static void showAutotitleComment(){
        AdBanner adbanner = AdBanner.findAutoTitleCommentBanner();
        if(adbanner != null && adbanner.isEnable()){
            renderText("show");
        } else {
            renderText("noshow");
        }
    }
    
    public static void show5XingHaoPing(){
        AdBanner adbanner = AdBanner.find5XingBanner();
        if(adbanner != null && adbanner.isEnable()){
            renderText("show");
        } else {
            renderText("noshow");
        }
    }
    
    public static void showFreeOneMonth(){
        AdBanner adbanner = AdBanner.findFreeOneMonthBanner();
        if(adbanner != null && adbanner.isEnable()){
            renderText("show");
        } else {
            renderText("noshow");
        }
    }
    
    public static void fiveyuanshowed() {
        User user = getUser();
        UserOP userOp = UserOP.findByUserId(user.getId());
        if(userOp == null){
            renderText("unshowed");
        } else if(userOp.is5yuanShowed()) {
            renderText("showed");
        } else {
            renderText("unshowed");
        }
    }
    
    public static void ReInfiveyuanshowed() {
        User user = getUser();
        UserOPVisitCount visitCount = UserOPVisitCount.findByUserId(user.getId());
        if(visitCount == null){
        	new UserOPVisitCount(user.getId(), 1).jdbcSave();
            renderText("noshow");
        } else if(visitCount.visitCount == 1) {
        	visitCount.setVisitCount(visitCount.visitCount + 1);
        	visitCount.jdbcSave();
            renderText("toshow");
        } else {
        	visitCount.setVisitCount(visitCount.visitCount + 1);
        	visitCount.jdbcSave();
            renderText("noshow");
        }
    }
    
    public static void threeyuanshowed() {
        User user = getUser();
        UserOP userOp = UserOP.findByUserId(user.getId());
        if(userOp == null){
            renderText("unshowed");
        } else if(userOp.is3yuanShowed()) {
            renderText("showed");
        } else {
            renderText("unshowed");
        }
    }
    
    public static void peixunshowed() {
        User user = getUser();
        UserOP userOp = UserOP.findByUserId(user.getId());
        if(userOp == null){
            renderText("unshowed");
        } else if(userOp.isPeixunShowed()) {
            renderText("showed");
        } else {
            renderText("unshowed");
        }
    }
    
    public static void dazheshowed() {
        User user = getUser();
        UserOP userOp = UserOP.findByUserId(user.getId());
        if(userOp == null){
            renderText("unshowed");
        } else if(userOp.isDazheShowed()) {
            renderText("showed");
        } else {
            renderText("unshowed");
        }
    }
    
    public static void oldthreeyuanshowed() {
        User user = getUser();
        UserOP userOp = UserOP.findByUserId(user.getId());
        if(userOp == null){
            renderText("unshowed");
        } else if(userOp.isold3yuanShowed()) {
            renderText("showed");
        } else {
            renderText("unshowed");
        }
    }
    
    public static void haopingshowed() {
        User user = getUser();
        UserOP userOp = UserOP.findByUserId(user.getId());
        if(userOp == null){
            renderText("unshowed");
        } else if(userOp.isHaoPingShowed()) {
            renderText("showed");
        } else {
            renderText("unshowed");
        }
    }
    
    public static void freeonemonthshowed() {
        User user = getUser();
        UserOP userOp = UserOP.findByUserId(user.getId());
        if(userOp == null){
            renderText("unshowed");
        } else if(userOp.isFreeOneMonthShowed()) {
            renderText("showed");
        } else {
            renderText("unshowed");
        }
    }
    
    public static void show5yuanXufei(){
        AdBanner adbanner = AdBanner.find5yuanXufei();
        if(adbanner != null && adbanner.isEnable()){
            renderText("show");
        } else {
            renderText("noshow");
        }
    }
    
    public static void show3yuanXufei(){
        AdBanner adbanner = AdBanner.find3yuanXufei();
        if(adbanner != null && adbanner.isEnable()){
            renderText("show");
        } else {
            renderText("noshow");
        }
    }
    
    public static void showPeixun(){
        AdBanner adbanner = AdBanner.findPeixun();
        if(adbanner != null && adbanner.isEnable()){
            renderJSON(JsonUtil.getJson(adbanner));
        } else {
            renderText("noshow");
        }
    }
    
    public static void showDazhe(){
        AdBanner adbanner = AdBanner.findPeixun();
        if(adbanner != null && adbanner.isEnable()){
            renderJSON(JsonUtil.getJson(adbanner));
        } else {
            renderText("noshow");
        }
    }
    
    public static void showOld3yuanXufei(){
        AdBanner adbanner = AdBanner.findOld3yuanXufei();
        if(adbanner != null && adbanner.isEnable()){
            renderText("show");
        } else {
            renderText("noshow");
        }
    }
    
    public static void aituiguangNewFiveYuan() {
    	User user = getUser();
    	if(user == null) {
    		renderFailedJson("用户不存在，请重新授权");
    	}
    	String paramStr = StringUtils.EMPTY;
    	if(user.getVersion() == 1) {
    		paramStr = TMConfigs.YINGXIAO.AITUIGUANG_FIVE_YAUN_ONE_SHOWCASE;
    	} else if(user.getVersion() == 20) {
    		paramStr = TMConfigs.YINGXIAO.AITUIGUANG_FIVE_YAUN_THREE_SHOWCASE;
    	} else if(user.getVersion() == 30) {
    		paramStr = TMConfigs.YINGXIAO.AITUIGUANG_FIVE_YAUN_FIVE_SHOWCASE;
    	} else if(user.getVersion() == 40) {
    		paramStr = TMConfigs.YINGXIAO.AITUIGUANG_FIVE_YAUN_TEN_SHOWCASE;
    	} else if(user.getVersion() == 50) {
    		paramStr = TMConfigs.YINGXIAO.AITUIGUANG_FIVE_YAUN_TWENTY_SHOWCASE;
    	} else if(user.getVersion() == 60) {
    		paramStr = TMConfigs.YINGXIAO.AITUIGUANG_FIVE_YAUN_THIRTY_SHOWCASE;
    	} else {
    		renderFailedJson("版本不对，暂无优惠");
    	}
    	
        String link = StringUtils.EMPTY;
        link = new bustbapi.FuwuApis.SaleLinkGenApi(APIConfig.get().getApiKey(),APIConfig.get().getSecret(),user.userNick,paramStr).call();
        if(StringUtils.isEmpty(link)) {
        	renderFailedJson("获取链接出错");
        }
        renderSuccessJson(link);
    }
}
