package carrier;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import result.TMResult;
import utils.oyster.BufferedImageBuilder;
import utils.oyster.DateUtil;
import actions.WaterMarkerAction.MarkImageUtil;
import actions.pdd.CopyToPddAction;
import bustbapi.PicApi;

import com.taobao.api.domain.Picture;

public class FileCarryUtils {

	public final static Logger log = LoggerFactory
			.getLogger(FileCarryUtils.class);

	public static String filterDesc(User user, String desc) {
		if (user == null || StringUtils.isEmpty(desc)) {
			return StringUtils.EMPTY;
		}

		// 匹配 src="http://"
		Pattern pattern = Pattern
				.compile("src=('|\")?[\\w\\.\\-/:?!_&%=;,]+('|\")?");
		Matcher matcher = pattern.matcher(desc);
		HashSet<String> itemLinkSet = new HashSet<String>();
		while (matcher.find()) {
			String link = matcher.group();
			if (StringUtils.isBlank(link) || link.startsWith("=")) {
				continue;
			}

			if (link.startsWith("src=")) {
				link = link.replaceFirst("src=('|\")?", StringUtils.EMPTY);
				if (link.endsWith("\"") || link.endsWith("'")) {
					link = link.substring(0, link.length() - 1);
				}
			}

			itemLinkSet.add(link);
		}

		// 匹配 background:url("http://")
		pattern = Pattern
				.compile("(background:(\\s)*url\\(('|\")?){1}[\\w\\.\\-/:?!_&%=;,]+('|\")?\\)");
		matcher = pattern.matcher(desc);
		while (matcher.find()) {
			String link = matcher.group();
			if (StringUtils.isBlank(link)) {
				continue;
			}

			link = link.replaceAll("background:(\\s)*url\\(('|\")?",
					StringUtils.EMPTY);
			if (link.endsWith("\")") || link.endsWith("')")) {
				link = link.substring(0, link.length() - 1);
			}

			itemLinkSet.add(link);
		}

		// 匹配 background="http://..."
		pattern = Pattern
				.compile("background=('|\")?[\\w\\.\\-/:?!_&%=;,]+('|\")?");
		matcher = pattern.matcher(desc);
		while (matcher.find()) {
			String link = matcher.group();
			if (StringUtils.isBlank(link)) {
				continue;
			}

			link = link.replaceAll("background=('|\")?", "");
			if (link.endsWith("\"") || link.endsWith("'")) {
				link = link.substring(0, link.length() - 1);
			}

			itemLinkSet.add(link);
		}

		for (String link : itemLinkSet) {
//			String newLink = FileCarryUtils.uploadPicFromOnline(user, link);
			 TMResult<Picture> result =FileCarryUtils.newUploadPicFromOnline(user, link);
			 String newLink=result.getRes().getPicturePath();
			log.info(link + " ---> " + newLink);
			desc = desc.replace(link, newLink);
		}

		return desc;
	}

	/**
	 * 返回TMResult 追踪图片空间报错
	 */
	public static TMResult<Picture> newFilterDesc(User user, String desc, Long catId) {
		if (user == null || StringUtils.isEmpty(desc)) {
			return new TMResult<Picture>(true, desc, null);
		}

		// 匹配 src="http://"
		Pattern pattern = Pattern
				.compile("src=('|\")?[\\w\\.\\-/:?!_&%=;,\\s]+('|\")?");
		Matcher matcher = pattern.matcher(desc);
		HashSet<String> itemLinkSet = new HashSet<String>();
		while (matcher.find()) {
			String link = matcher.group();
			if (StringUtils.isBlank(link) || link.startsWith("=")) {
				continue;
			}

			if (link.startsWith("src=")) {
				link = link.replaceFirst("src=('|\")?", StringUtils.EMPTY);
				if (link.endsWith("\"") || link.endsWith("'")) {
					link = link.substring(0, link.length() - 1);
				}
			}

			if (link.indexOf("load.js") >= 0) {
				continue;
			}

			itemLinkSet.add(link);
		}

		// 匹配 background:url("http://")
		pattern = Pattern
				.compile("(background:(\\s)*url\\(('|\")?){1}[\\w\\.\\-/:?!_&%=;,]+('|\")?\\)");
		matcher = pattern.matcher(desc);
		while (matcher.find()) {
			String link = matcher.group();
			if (StringUtils.isBlank(link)) {
				continue;
			}

			link = link.replaceAll("background:(\\s)*url\\(('|\")?",
					StringUtils.EMPTY);
			if (link.endsWith("\")") || link.endsWith("')")) {
				link = link.substring(0, link.length() - 1);
			}

			itemLinkSet.add(link);
		}

		// 匹配 background="http://..."
		pattern = Pattern
				.compile("background=('|\")?[\\w\\.\\-/:?!_&%=;,]+('|\")?");
		matcher = pattern.matcher(desc);
		while (matcher.find()) {
			String link = matcher.group();
			if (StringUtils.isBlank(link)) {
				continue;
			}

			link = link.replaceAll("background=('|\")?", "");
			if (link.endsWith("\"") || link.endsWith("'")) {
				link = link.substring(0, link.length() - 1);
			}

			itemLinkSet.add(link);
		}

		for (String link : itemLinkSet) {
			TMResult<Picture> result = FileCarryUtils.newUploadPic(user, link, catId);
			if (!result.isOk) {
				return new TMResult<Picture>(false, result.msg, null);
			}
			String newLink = result.getRes() == null ? StringUtils.EMPTY
					: result.getRes().getPicturePath();
			log.info(link + " ---> " + newLink);
			desc = desc.replace(link, newLink);
		}

		return new TMResult<Picture>(true, desc, null);
	}

	public static String fetchFileName(String picUrl) {
		if (StringUtils.isEmpty(picUrl)) {
			return null;
		}
		int nameStart = picUrl.lastIndexOf('/');
		if (nameStart < 0 || nameStart >= picUrl.length()) {
			return null;
		}

		String fileName = picUrl.substring(nameStart + 1);

		return fileName;
	}

	/**
	 * 从远程图片地址下载图片到本地 tmp目录下
	 * 
	 * @param picUrl
	 * @return picPath 下载到本地的绝对路径
	 */
	public static String downloadPicToLocal(String picUrl) {
		String filePath = StringUtils.EMPTY;
		String fileName = fetchFileName(picUrl);
		if (StringUtils.isEmpty(fileName)) {
			return StringUtils.EMPTY;
		}
		try {
			URL url = new URL(picUrl);
			InputStream fStream = url.openConnection().getInputStream();

			int b = 0;
			File file = new File(Play.tmpDir, fileName);
			filePath = file.getAbsolutePath();

			FileOutputStream fos = new FileOutputStream(file);
			while ((b = fStream.read()) != -1) {
				fos.write(b);
			}
			fStream.close();
			fos.close();
		} catch (Exception e) {
			log.error("download pic to local error!!");
			log.error(e.getMessage(), e);
		}
		return filePath;
	}

	/**
	 * 从远程图片地址下载图片裁剪对应的尺寸然后上传
	 * 
	 * @param picUrl
	 * @param width
	 *            宽度
	 * @param height
	 *            高度
	 * @return picPath 下载到本地的绝对路径
	 */
	public static String uploadPicDealSizeOnline(User user, String picUrl,
			int width, int height) {
		String filePath = StringUtils.EMPTY;
		String fileName = fetchFileName(picUrl);
		if (StringUtils.isEmpty(fileName)) {
			return StringUtils.EMPTY;
		}
		try {
			URL url = new URL(picUrl);
			InputStream fStream = url.openConnection().getInputStream();
			int b = 0;
			fileName = DateUtil.getNowTime("yyMMddHHmmssms") + fileName;
			File file = new File(Play.tmpDir, fileName);
			FileOutputStream fos = new FileOutputStream(file);
			while ((b = fStream.read()) != -1) {
				fos.write(b);
			}
			fStream.close();
			fos.close();
			BufferedImage originImg = ImageIO.read(file);
			BufferedImage lastPic = MarkImageUtil.zoomImage(originImg, width,
					height);
			boolean result = ImageIO.write(lastPic,
					fileName.substring(fileName.lastIndexOf(".") + 1), file);
			if (result) {
				log.warn("success! pic from " + picUrl + " deal with width:"
						+ width + ",height:" + height);
				filePath = uploadPicFromLocal(user, Play.tmpDir
						+ File.separator + fileName);
			} else {
				log.error("fiald! pic from " + picUrl + " deal with width:"
						+ width + ",height:" + height);
			}

		} catch (Exception e) {
			log.error("download pic to local error!!");
			log.error(e.getMessage(), e);
		}
		return filePath;
	}

	/**
	 * 从远程图片地址下载图片裁剪对应的尺寸
	 * 
	 * @param picUrl
	 * @param width
	 *            宽度
	 * @param height
	 *            高度
	 * @return picPath 下载到本地的绝对路径
	 */
	public static String downOnlineDealSize(String picUrl, int width, int height) {
		String filePath = StringUtils.EMPTY;
		String fileName = fetchFileName(picUrl);
		if (StringUtils.isEmpty(fileName)) {
			return StringUtils.EMPTY;
		}
		try {
			fileName = Play.tmpDir + File.separator
					+ DateUtil.getNowTime("yyMMddHHmmssms") + fileName;
			URL url = new URL(picUrl);
			InputStream fStream = url.openConnection().getInputStream();
			BufferedImage originImg = ImageIO.read(fStream);
			BufferedImage lastPic = MarkImageUtil.zoomImage(originImg, width,
					height);

			boolean result = ImageIO.write(lastPic,
					fileName.substring(fileName.lastIndexOf(".") + 1),
					new File(fileName));

			if (result) {
				log.warn("success! pic from " + picUrl + " deal with width:"
						+ width + ",height:" + height);
				return Play.tmpDir + File.separator + fileName;
			} else {
				log.error("fiald! pic from " + picUrl + " deal with width:"
						+ width + ",height:" + height);
			}

		} catch (Exception e) {
			log.error("download pic to local error!!");
			log.error(e.getMessage(), e);
		}
		return filePath;
	}

	/**
	 * 
	 * @Description: 剪切网络图片 String
	 */
	public static String cutFromUrl(String imageUrl, int width, int height) {
		String fileName = "";
		InputStream is = null;
		ImageInputStream iis = null;
		try {
			/** 读取图片 */
			Iterator<ImageReader> it = ImageIO
					.getImageReadersByFormatName("jpg");
			ImageReader reader = it.next();
			/** 获取图片流 */
			URL url = new URL(imageUrl);
			HttpURLConnection httpConn = (HttpURLConnection) url
					.openConnection();
			/** 设置请求方式为"GET" */
			httpConn.setRequestMethod("GET");
			/** 超时响应时间为5秒 */
			httpConn.setConnectTimeout(5 * 1000);
			httpConn.connect();
			is = httpConn.getInputStream();

			iis = ImageIO.createImageInputStream(is);
			reader.setInput(iis, true);

			ImageReadParam param = reader.getDefaultReadParam();
			Rectangle rect = new Rectangle(0, 0, width, height);
			param.setSourceRegion(rect);
			BufferedImage bi = reader.read(0, param);
			fileName = Play.tmpDir + File.separator
					+ DateUtil.getNowTime("yyMMddHHmmssms")
					+ fetchFileName(imageUrl);
			ImageIO.write(bi, "jpg", new File(fileName));
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (is != null) {
					is.close();
				}
				if (iis != null) {
					iis.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return fileName;
	}

	/*
	 * 图片缩放,w，h为缩放的目标宽度和高度 src为源文件目录，dest为缩放后保存目录
	 */
	public static String zoomImage(String picUrl, int w, int h) {
		double wr = 0, hr = 0;
		try {
			URL url = new URL(picUrl);
			InputStream fStream = url.openConnection().getInputStream();
			
			Image imageTookit = Toolkit.getDefaultToolkit().createImage(CopyToPddAction.readInputStream(fStream));  
			BufferedImage bufImg = BufferedImageBuilder.toBufferedImage(imageTookit);  // 读取图片
			
			Image Itemp = bufImg.getScaledInstance(w, h, bufImg.SCALE_SMOOTH);// 设置缩放目标图片模板
			wr = w * 1.0 / bufImg.getWidth(); // 获取缩放比例
			hr = h * 1.0 / bufImg.getHeight();
			AffineTransformOp ato = new AffineTransformOp(
					AffineTransform.getScaleInstance(wr, hr), null);
			Itemp = ato.filter(bufImg, null);
			String fileName = Play.tmpDir + File.separator
					+ DateUtil.getNowTime("yyMMddHHmmssms")
					+ fetchFileName(picUrl);
			ImageIO.write((BufferedImage) Itemp, picUrl.substring(picUrl
					.lastIndexOf(".") + 1), new File(fileName)); // 写入缩减后的图片
			return fileName;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static String uploadPicFromLocal(User user, String picPath) {
		return uploadPicFromOnline(user, picPath, null, null);
	}

	public static String uploadPicFromOnline(User user, String picPath,
			String imageInputTitle, String title) {
		picPath = picPath.replaceAll("\\\\", "/");
		if (StringUtils.isEmpty(imageInputTitle)) {
			imageInputTitle = FileCarryUtils.fetchFileName(picPath);
		}
		if (StringUtils.isEmpty(title)) {
			int dotIdx = imageInputTitle.lastIndexOf('.');
			if (dotIdx > 0) {
				title = imageInputTitle.substring(0, dotIdx);
			} else {
				title = imageInputTitle;
			}
		}

		PicApi api = PicApi.get();
		Picture pic = api.uploadPcClientPic(user, imageInputTitle, new File(
				picPath), api.ensureTMCat(user));
		if (pic == null) {
			return StringUtils.EMPTY;
		}
		return pic.getPicturePath();
	}

	/**
	 * 返回TMResult 追踪图片空间报错
	 */
	public static TMResult<Picture> newUploadPicFromLocal(User user,
			String picPath) {
		return newUploadPicFromOnline(user, picPath, null, null, null);
	}

	public static TMResult<Picture> newUploadPicFromLocal(User user,
			String picPath, Long pictureCategoryId) {
		return newUploadPicFromOnline(user, picPath, null, null,
				pictureCategoryId);
	}

	/**
	 * 返回TMResult 追踪图片空间报错
	 */
	public static TMResult<Picture> newUploadPicFromOnline(User user,
			String picPath, String imageInputTitle, String title,
			Long pictureCategoryId) {
		picPath = picPath.replaceAll("\\\\", "/");
		if (StringUtils.isEmpty(imageInputTitle)) {
			imageInputTitle = FileCarryUtils.fetchFileName(picPath);
		}
		if (StringUtils.isEmpty(title)) {
			int dotIdx = imageInputTitle.lastIndexOf('.');
			if (dotIdx > 0) {
				title = imageInputTitle.substring(0, dotIdx);
			} else {
				title = imageInputTitle;
			}
		}

		PicApi api = PicApi.get();
		if (pictureCategoryId == null)
			pictureCategoryId = api.ensureTMCat(user);
		TMResult<Picture> result = api.newUploadPcClientPic(user,
				imageInputTitle, new File(picPath), pictureCategoryId);

		return result;
	}

	public static String uploadPicFromOnline(User user, String picUrl) {
		try {
			String picPath = FileCarryUtils.downloadPicToLocal(picUrl);
			if (!StringUtils.isEmpty(picPath)) {
				String url = FileCarryUtils.uploadPicFromLocal(user, picPath);
				return url;
			} else {
				return StringUtils.EMPTY;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return StringUtils.EMPTY;
	}

	/**
	 * 返回TMResult 追踪图片空间报错
	 */
	public static TMResult<Picture> newUploadPicFromOnline(User user,
			String picUrl) {
		TMResult<Picture> pictureTMResult = null;
		final int retry = 5;
		int i = 0;
		do {
			pictureTMResult = newUploadPicFromOnline(user, picUrl, null);
		} while (pictureTMResult.getRes() == null && !pictureTMResult.getMsg().contains("容量不足，请登录图片空间") && i++ < retry);

		return pictureTMResult;
	}
	
	public static TMResult<Picture> newUploadPic(User user, String picUrl, Long catId) {
		TMResult<Picture> pictureTMResult = null;
		final int retry = 5;
		int i = 0;
		do {
			pictureTMResult = newUploadPicFromOnline(user, picUrl, catId);
		} while (pictureTMResult.getRes() == null && !pictureTMResult.getMsg().contains("容量不足，请登录图片空间") && i++ < retry);

		return pictureTMResult;
	}

	public static TMResult<Picture> newUploadPicFromOnline(User user,
			String picUrl, Long pictureCategoryId) {
		try {
			String picPath = FileCarryUtils.downloadPicToLocal(picUrl);
			if (!StringUtils.isEmpty(picPath)) {
				TMResult<Picture> result = FileCarryUtils
						.newUploadPicFromLocal(user, picPath, pictureCategoryId);
				return result;
			} else {
				return new TMResult<Picture>(true, StringUtils.EMPTY, null);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return new TMResult<Picture>(true, StringUtils.EMPTY, null);
	}

	public static String format800ImgPath(String srcPath) {
		if (StringUtils.isEmpty(srcPath)) {
			return StringUtils.EMPTY;
		}
		int i = srcPath.lastIndexOf(".");
		return srcPath.substring(0, i) + "_800.jpg";
	}

	public static String resizeImage(String srcImgPath, String distImgPath,
			int width, int height) {
		try {
			File srcFile = new File(srcImgPath);
			Image srcImg = ImageIO.read(srcFile);
			if (srcImg.getHeight(null) == 800 && srcImg.getWidth(null) == 800) {
				return srcImgPath;
			}
			BufferedImage buffImg = null;
			buffImg = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_RGB);
			buffImg.getGraphics()
					.drawImage(
							srcImg.getScaledInstance(width, height,
									Image.SCALE_SMOOTH), 0, 0, null);

			ImageIO.write(buffImg, "JPEG", new File(distImgPath));
			return distImgPath;
		} catch (Exception e) {
		}
		return srcImgPath;
	}
}
