package controllers;

import models.op.AdBanner;
import models.paipai.PaiPaiUser;
import models.user.UserOP;

public class OPPaiPaiUserInterFace extends PaiPaiController {
    public static void setShowed() {
    	PaiPaiUser user = getUser();
        UserOP userOP = UserOP.findByUserId(user.getId());
        if(userOP == null){
            boolean isSuccess = new UserOP(user.getId(),false,UserOP.Type.ONE_YUAN).jdbcSave();
            if(isSuccess){
                renderText("设置成功");
            }else {
                renderText("设置失败");
            }
        } else {
            if(userOP.is1yuanShowed()){
                renderText("已设置");
            } else {
                userOP.set1YuanShowed(true);
                userOP.jdbcSave();
                renderText("设置成功");
            }
        }
    }
	
    public static void oneyuanshowed(){
    	PaiPaiUser user=getUser();
        UserOP userOp = UserOP.findByUserId(user.getId());
        if(userOp == null){
            renderText("unshowed");
        } else if(userOp.is1yuanShowed()) {
            renderText("showed");
        } else {
            renderText("unshowed");
        }
    }
    
    public static void show1yuanhongbao(){
        AdBanner adbanner = AdBanner.find1yuanhongbao();
        if(adbanner != null && adbanner.isEnable()){
            renderText("show");
        } else {
            renderText("noshow");
        }
    }

}
