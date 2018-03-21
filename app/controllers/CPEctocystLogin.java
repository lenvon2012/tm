package controllers;

import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import play.cache.Cache;
import play.mvc.Controller;
import models.op.CPStaff;

import com.ciaosir.client.utils.JsonUtil;

import controllers.Op.InviteInfo;

public class CPEctocystLogin extends Controller{

	@Transient
	private static final String PASSWORD_PATTERN = "[\\x21-\\x7e]{6,64}";
	
	public static void register (String username, String password, String email, String phone) {
		//KefuSweety user = KefuSweety.connect(username, password);
		CPStaff user = CPStaff.findByName(username);
		if(user != null) {
			renderText("用户名已存在");
		}
		/*if(!StringUtils.isBlank(password)){
			if (!password.matches(PASSWORD_PATTERN)){
				renderText("新密码不符合规则");
			}
		}*/
		user = new CPStaff(username, password, email, phone);
		user = user.save();
		if(user == null) {
			renderText("注册出错");
		}
		Cache.set(CPStaff.CPSTaffCache + username.trim(), user, "2h");
		renderJSON(JsonUtil.getJson(user));
	}
	
	public static void login(String username, String password) {

		CPStaff user = CPStaff.connect(username, password);
		if(user == null) {

			renderText("用户不存在");
		}
		Cache.set(CPStaff.CPSTaffCache + username.trim(), user, "2h");
		renderJSON(JsonUtil.getJson(user));
	}
	
	public static void editUser(String username, String password, String newPassword, 
			String phone) {
		
		CPStaff user = CPStaff.connect(username, password);
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
		if(!StringUtils.isBlank(phone)){
			user.setPhone(phone);
		}
		user = user.save();
		if(user == null) {
			renderText("更新出错");
		}
		Cache.set(CPStaff.CPSTaffCache + username.trim(), user, "2h");
		renderJSON(JsonUtil.getJson(user));
	}
	
	public static void editPassword(String username, String newPassword) {
		
		CPStaff user = CPStaff.find("name = ?", username).first();
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
		user = user.save();
		if(user == null) {
			renderText("修改出错");
		}
		Cache.set(CPStaff.CPSTaffCache + username.trim(), user, "2h");
		renderJSON(JsonUtil.getJson(user));
	}
	
    public static void genSweetyInviteUrl(String name) {
        InviteInfo info = new InviteInfo();
        String url = "http://" + request.host + "/sweetyinvite/" +name;
        info.setUrl(url);
        renderJSON(JsonUtil.getJson(info));
    }
}
