package actions;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import models.CDNPIc.UserCDNPic;
import models.user.User;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;

import bustbapi.PictureApi;

import com.taobao.api.domain.Picture;

import controllers.newAutoTitle;

public class TaobaoCDNPicAction {

	private static final Logger log = LoggerFactory
			.getLogger(TaobaoCDNPicAction.class);

	public static Picture uploadPicForUser(User user, String imgPath,
			String imgName, String imgTitle) {
		if (user == null) {
			return null;
		}
		if (StringUtils.isEmpty(imgPath) || StringUtils.isEmpty(imgName)
				|| StringUtils.isEmpty(imgTitle)) {
			return null;
		}
		Picture p = new PictureApi.PictureCarrier(user.getSessionKey(),
				imgPath, imgName, imgTitle).call();
		if (p == null) {
			return null;
		}
		return p;
	}

	public static CDNPicMsg getCDNPicPath(User user, String title) {
		if (user == null) {
			return new CDNPicMsg(false, "传入的user为空");
		}
		if (StringUtils.isEmpty(title)) {
			return new CDNPicMsg(false, "传入的图片title为空");
		}

		String cdnPath = StringUtils.EMPTY;
		cdnPath = UserCDNPic.getCDNPicPathByTitle(user.getId(), title);
		if (!StringUtils.isEmpty(cdnPath)) {
			return new CDNPicMsg(true, cdnPath);
		}

		// 否则先上传
		LocalPic localPic = cdnPicMap.get(title);
		if (localPic == null) {
			return new CDNPicMsg(false, "找不到本地的title图片映射");
		}
		Picture p = uploadPicForUser(user, localPic.getLocalPath(),
				localPic.getPicType(), title);
		if (p == null) {
			return new CDNPicMsg(false, "上传图片到CDN出错");
		}
		new UserCDNPic(p.getPictureId(), user.getId(), title,
				p.getPicturePath()).jdbcSave();
		return new CDNPicMsg(true, p.getPicturePath());
	}

	public static Map<String, LocalPic> cdnPicMap = new HashMap<String, LocalPic>();

	static {
		cdnPicMap.put("manjiusong1", new LocalPic(Play.applicationPath
				+ "/public/images/dazhe/manjiusong1.jpg", "jpg"));

		cdnPicMap.put("manjiusong2", new LocalPic(Play.applicationPath
				+ "/public/images/dazhe/manjiusong2.jpg", "jpg"));
	}

	public static class CDNPicMsg {
		public Boolean success;

		public String msg;

		public CDNPicMsg(Boolean success, String msg) {
			super();
			this.success = success;
			this.msg = msg;
		}

		public Boolean getSuccess() {
			return success;
		}

		public void setSuccess(Boolean success) {
			this.success = success;
		}

		public String getMsg() {
			return msg;
		}

		public void setMsg(String msg) {
			this.msg = msg;
		}

	}

	public static class LocalPic {

		public String localPath;

		// jpg, png等
		public String picType;

		public LocalPic(String localPath, String picType) {
			super();
			this.localPath = localPath;
			this.picType = picType;
		}

		public String getLocalPath() {
			return localPath;
		}

		public void setLocalPath(String localPath) {
			this.localPath = localPath;
		}

		public String getPicType() {
			return picType;
		}

		public void setPicType(String picType) {
			this.picType = picType;
		}

	}
}
