package controllers;

import models.tblm.TBLMUser;

import com.ciaosir.client.utils.JsonUtil;

import controllers.Op.InviteInfo;

public class TBLMUserUI extends CatUnionBase{

	public static void register (String username, String password, String email, String phone,String fromname) {
		TBLMUser user = TBLMUser.connect(username, password);
		if(user != null) {
			renderText("用户名已存在");
		}
		user = new TBLMUser(username, password, email, phone, fromname);
		user._save();
		renderJSON(JsonUtil.getJson(user));
	}
	
	public static void login(String username, String password) {
		TBLMUser user = TBLMUser.connect(username, password);
		if(user == null) {
			renderText("用户不存在");
		}
		renderJSON(JsonUtil.getJson(user));
	}
	
    public static void genTBLMInviteUrl(String name) {
        InviteInfo info = new InviteInfo();
        String url = "http://" + request.host + "/tblminvite/" +name;
        info.setUrl(url);
        renderJSON(JsonUtil.getJson(info));
    }
}
