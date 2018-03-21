package controllers;

import javax.persistence.Transient;

import models.KefuSweety.KefuSweety;

import org.apache.commons.lang.StringUtils;

import com.ciaosir.client.utils.JsonUtil;

import controllers.Op.InviteInfo;

public class KefuSweetyUI extends CatUnionBase{

	@Transient
	private static final String PASSWORD_PATTERN = "[\\x21-\\x7e]{6,64}";
	
	public static void register (String username, String password, String email, String phone) {
		//KefuSweety user = KefuSweety.connect(username, password);
		KefuSweety user = KefuSweety.findByName(username);
		if(user != null) {
			renderText("用户名已存在");
		}
		/*if(!StringUtils.isBlank(password)){
			if (!password.matches(PASSWORD_PATTERN)){
				renderText("新密码不符合规则");
			}
		}*/
		user = new KefuSweety(username, password, email, phone);
		user._save();
		renderJSON(JsonUtil.getJson(user));
	}
	
	public static void login(String username, String password) {
		KefuSweety user = KefuSweety.connect(username, password);
		if(user == null) {
			renderText("用户不存在");
		}
		renderJSON(JsonUtil.getJson(user));
	}
	
	public static void editUser(String username, String password, String newPassword, 
			String email, String phone) {
		
		KefuSweety user = KefuSweety.connect(username, password);
		if(user == null) {
			renderText("用户不存在");
		}
		// 是否修改密码
		if(!StringUtils.isBlank(newPassword)){
			/*if (!password.matches(PASSWORD_PATTERN)){
				renderText("新密码不符合规则");
			}*/
			user.setPassword(newPassword);
		}
		if(!StringUtils.isBlank(email)){
			user.setEmail(email);
		}
		if(!StringUtils.isBlank(phone)){
			user.setPhone(phone);
		}
		user._save();
		renderJSON(JsonUtil.getJson(user));
	}
	
    public static void genSweetyInviteUrl(String name) {
        InviteInfo info = new InviteInfo();
        String url = "http://" + request.host + "/sweetyinvite/" +name;
        info.setUrl(url);
        renderJSON(JsonUtil.getJson(info));
    }
}

