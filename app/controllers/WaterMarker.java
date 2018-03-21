
package controllers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import job.watermarker.ClearOutDateFilesJob;
import models.item.ItemPlay;
import models.user.User;
import models.watermarker.WaterMarkerOriginImage;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import result.TMResult;
import actions.WaterMarkerAction.MarkImageUtil;
import bustbapi.ItemApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.NumberUtil;
import com.taobao.api.domain.Item;

import dao.item.ItemDao;

public class WaterMarker extends TMController {
    private static final Logger log = LoggerFactory.getLogger(WaterMarker.class);

    private static final String WaterMarkerFirstFolder = "tmp/watermarker";

    public static final File WaterMarkerOutDir = new File(Play.applicationPath, WaterMarkerFirstFolder);

    private static final String WaterMarkerOutZipKey = "_wm_zip_path_";

    public static void waterMarker() {

        render();
    }

    /**
     * 选择要添加水印的宝贝
     * @param s
     * @param pn
     * @param ps
     * @param excludedIds
     */
    public static void chooseItems(String s, int pn, int ps, String excludedIds) {
        User user = getUser();
        PageOffset po = new PageOffset(pn, ps);
        //excludedIds暂时都为空
        List<Long> excludedIdList = new ArrayList<Long>();
        if (!StringUtils.isEmpty(excludedIds)) {
            String[] excludedIdArray = excludedIds.split(",");
            if (excludedIdArray != null) {
                for (int i = 0; i < excludedIdArray.length; i++) {
                    long numIid = NumberUtil.parserLong(excludedIdArray[i], 0L);
                    excludedIdList.add(numIid);
                }
            }
        }
        TMResult tmRes = ItemDao.findByUserWithExcluded(user.getId(), po, excludedIdList);
        renderJSON(JsonUtil.getJson(tmRes));
    }

    /**
     * 显示水印记录备份
     */
    /*public static void showWaterMarkerRecords() {
    	User user = getUser();
    	List<WaterMarkerRecord> recordList = WaterMarkerRecord.findByUserIdWithDesc(user.getId());
    	
    	renderJSON(recordList);
    }*/
    
    /**
     * 还原备份
     */
    /*public static void returnBackWaterMarkerRecords(Long recordId) {
    	User user = getUser();
    	WaterMarkerRecord record = WaterMarkerRecord.findByRecordId(user.getId(), recordId);
    	if (record == null) {
    		renderError("亲，找不到该备份，请联系我们");
    	}
    	List<WaterMarkerItem> markerItemList = WaterMarkerItem.findByRecordId(user.getId(), recordId);
    	if (markerItemList == null || markerItemList.isEmpty()) {
    		renderError("亲，该备份中宝贝记录为空，请联系我们");
    	}
    	
    	List<WaterMarkerItem> errorList = new ArrayList<WaterMarkerItem>();
    	List<WaterMarkerItem> successList = new ArrayList<WaterMarkerItem>();
    	//
		File targetFolder = getTargetFolder(user);
    	for (WaterMarkerItem markerItem : markerItemList) {
    		try {
    			String picUrl = markerItem.getPicUrl();
    			String format = MarkImageUtil.getFormat(picUrl, true);
                String targetPath = getTargetPath(targetFolder, markerItem.getNumIid(), format);
	    		//文件下载到本地
                downloadImg(picUrl, targetPath);
                
                File newImgFile = new File(targetPath);
	            ItemApi.ItemImageUpdater api = new ItemApi.ItemImageUpdater(user.getSessionKey(), markerItem.getNumIid(), newImgFile);
	            Item newItem = api.call();
	            if (newItem == null || api.isSuccess() == false) {
	            	errorList.add(markerItem);
	            	continue;
	            }
	            successList.add(markerItem);
    		} catch (Exception ex) {
    			log.error(ex.getMessage(), ex);
    			errorList.add(markerItem);
    		}
    	}
    	
    	if (successList.isEmpty()) {
    		renderError("亲，全部还原失败，请联系我们");
    	}
    	
    	//更新宝贝图片
    	List<Long> numIidList = new ArrayList<Long>();
    	for (WaterMarkerItem markerItem : successList) {
    		Long numIid = markerItem.getNumIid();
    		numIidList.add(numIid);
    	}
    	List<ItemPlay> successItemList = ItemDao.findByNumIids(user.getId(), numIidList);
    	if (successItemList == null) {
    		successItemList = new ArrayList<ItemPlay>();
    	}
        updateItemPics(user, successItemList);

        
        if (!CommonUtils.isEmpty(errorList)) {
            renderSuccess("亲，" + errorList.size() + "宝贝还原失败了，请联系我们", "");
        }

        renderSuccess("宝贝还原成功！", "");
    	
    }*/
    
    /**
     * 还原备份，像读数据库log一样
     */
    /*public static void returnBackWaterMarkerRecords(Long recordId) {
    	User user = getUser();
    	WaterMarkerRecord record = WaterMarkerRecord.findByRecordId(user.getId(), recordId);
    	if (record == null) {
    		renderError("亲，找不到该备份，请联系我们");
    	}
    	List<WaterMarkerItem> markerItemList = WaterMarkerItem.findByRecordId(user.getId(), recordId);
    	if (markerItemList == null || markerItemList.isEmpty()) {
    		renderError("亲，该备份中宝贝记录为空，请联系我们");
    	}
    	
    	Map<Long, String> modifyMap = new HashMap<Long, String>();
    	List<WaterMarkerItem> markerItemList = WaterMarkerItem.findWithRecordIdAsc(user.getId());
    	
    	//遍历操作日志，按照从前往后的顺序
    	for (WaterMarkerItem markerItem : markerItemList) {
    		long tempRecordId = markerItem.getRecordId();
    		Long numIid = markerItem.getNumIid();
    		//针对之前的日志，就相当于重新做一次一样
    		if (tempRecordId <= recordId) {
    			modifyMap.put(numIid, markerItem.getPicUrl());
    		} else {
    			if (modifyMap.containsKey(numIid))
    				continue;
    			else 
    				modifyMap.put(numIid, markerItem.getPicUrl());
    		}
    	}
    	
    	
    	List<Long> errorList = new ArrayList<Long>();
    	List<Long> successList = new ArrayList<Long>();
    	//
		File targetFolder = getTargetFolder(user);
    	for (Map.Entry<Long, String> entry : modifyMap.entrySet()) {
    		Long numIid = entry.getKey();
    		try {
    			String picUrl = entry.getValue();
    			String format = MarkImageUtil.getFormat(picUrl, true);
                String targetPath = getTargetPath(targetFolder, numIid, format);
	    		//文件下载到本地
                downloadImg(picUrl, targetPath);
                
                File newImgFile = new File(targetPath);
	            ItemApi.ItemImageUpdater api = new ItemApi.ItemImageUpdater(user.getSessionKey(), numIid, newImgFile);
	            Item newItem = api.call();
	            if (newItem == null || api.isSuccess() == false) {
	            	errorList.add(numIid);
	            	continue;
	            }
	            successList.add(numIid);
    		} catch (Exception ex) {
    			log.error(ex.getMessage(), ex);
    			errorList.add(numIid);
    		}
    	}
    	
    	if (successList.isEmpty()) {
    		renderError("亲，全部还原失败，请联系我们");
    	}
    	
    	//更新宝贝图片
    	List<Long> numIidList = new ArrayList<Long>();
    	for (Long numIid : successList) {
    		numIidList.add(numIid);
    	}
    	List<ItemPlay> successItemList = ItemDao.findByNumIids(user.getId(), numIidList);
    	if (successItemList == null) {
    		successItemList = new ArrayList<ItemPlay>();
    	}
        updateItemPics(user, successItemList);

        
        if (!CommonUtils.isEmpty(errorList)) {
            renderSuccess("亲，" + errorList.size() + "宝贝还原失败了，请联系我们", "");
        }

        renderSuccess("宝贝还原成功！", "");
    	
    }*/
    
    public static void returnBackSomeWaterMarker(String numIids) {
    	User user = getUser();
    	List<WaterMarkerOriginImage> originItemList = WaterMarkerOriginImage.findByNumIids(user.getId(), numIids);
    	
    	returnBackWaterMarkers(user, originItemList);
    }
    
    /**
     * 简单的还原，还原的到最初
     */
    public static void returnBackAllWaterMarker() {
    	User user = getUser();
    	List<WaterMarkerOriginImage> originItemList = WaterMarkerOriginImage.findByUserId(user.getId());
    	
    	returnBackWaterMarkers(user, originItemList);
    }
    
    
    private static void returnBackWaterMarkers(User user, List<WaterMarkerOriginImage> originItemList) {
    	if (originItemList == null) {
    		originItemList = new ArrayList<WaterMarkerOriginImage>();
    	}
    	if (originItemList.isEmpty()) {
    		renderSuccess("宝贝还原成功！", "");
    	}

    	List<WaterMarkerOriginImage> errorList = new ArrayList<WaterMarkerOriginImage>();
    	List<WaterMarkerOriginImage> successList = new ArrayList<WaterMarkerOriginImage>();
    	//
		File targetFolder = getTargetFolder(user);
    	for (WaterMarkerOriginImage originItem : originItemList) {
    		try {
    			String picUrl = originItem.getPicUrl();
    			String format = MarkImageUtil.getFormat(picUrl, true);
                String targetPath = getTargetPath(targetFolder, originItem.getNumIid(), format);
	    		//文件下载到本地
                downloadImg(picUrl, targetPath);
                
                File newImgFile = new File(targetPath);
	            ItemApi.ItemImageUpdater api = new ItemApi.ItemImageUpdater(user.getSessionKey(), originItem.getNumIid(), newImgFile);
	            Item newItem = api.call();
	            if (newItem == null || api.isApiSuccess() == false) {
	            	errorList.add(originItem);
	            	continue;
	            }
	            successList.add(originItem);
	            originItem.setTs(0L);
	    		originItem.save();
    		} catch (Exception ex) {
    			log.error(ex.getMessage(), ex);
    			errorList.add(originItem);
    		}
    	}
    	//删除图片
        deleteFolder(targetFolder);
    	
    	if (successList.isEmpty()) {
    		renderError("亲，全部还原失败，请联系我们");
    	}
    	
    	//更新宝贝图片
    	List<Long> numIidList = new ArrayList<Long>();
    	for (WaterMarkerOriginImage originItem : successList) {
    		Long numIid = originItem.getNumIid();
    		
    		numIidList.add(numIid);
    	}
    	List<ItemPlay> successItemList = ItemDao.findByNumIids(user.getId(), numIidList);
    	if (successItemList == null) {
    		successItemList = new ArrayList<ItemPlay>();
    	}
        updateItemPics(user, successItemList);

        
        if (!CommonUtils.isEmpty(errorList)) {
            renderSuccess("亲，" + errorList.size() + "宝贝还原失败了，请联系我们", "");
        }

        renderSuccess("宝贝还原成功！", "");
    }
    
    private static void deleteFolder(File folder) {
    	log.error("delete folder: " + folder.getAbsolutePath());
    	ClearOutDateFilesJob.deleteDirectory(folder);
    }
    
    //通过url下载图片
    private static void downloadImg(String srcUrl, String targetPath) throws Exception {
    	InputStream is = null;
    	OutputStream out = null;
    	try {
    		URL url = new URL(srcUrl); 
            URLConnection uc = url.openConnection(); 
            is = uc.getInputStream(); 
            File file = new File(targetPath); 
            out = new FileOutputStream(file); 
            int i = 0; 
            while ((i = is.read()) != -1) { 
            	out.write(i); 
            } 
            
    	} catch (Exception ex) {
    		throw new Exception(ex.getMessage());
    	} finally {
    		if (is != null)
    			is.close();
    		if (out != null)
    			out.close();
    	}
    }
    
    
    /**
     * 显示已选择的宝贝
     * @param numIids
     */
    public static void showItems(String numIids) {
        if (StringUtils.isEmpty(numIids))
            renderJSON(JsonUtil.getJson(new ArrayList<ItemPlay>()));
        User user = getUser();
        List<ItemPlay> itemList = ItemDao.findByIds(user.getId(), numIids);
        renderJSON(JsonUtil.getJson(itemList));
    }

    
    private static final String WaterMarkerImgFolderPath = "/public/images/source/watermarker";

    private static final File WaterMarkerImgDir = new File(Play.applicationPath, WaterMarkerImgFolderPath);

    
    /**
     * 显示水印图片列表
     * @param type
     * @param pn
     * @param ps
     */
    public static void showMarkerImgs(String type, int pn, int ps) {

    	PageOffset po = new PageOffset(pn, ps);
    	List<String> resultList = new ArrayList<String>();
    	File folder = new File(WaterMarkerImgDir, type);
    	//log.error(folder.getAbsolutePath());
    	if (folder.isDirectory() == false) {
    		TMResult tmRes = new TMResult(resultList, 0, po);
    		renderJSON(JsonUtil.getJson(tmRes));
    	}
    	String[] fileArray = folder.list();
    	if (fileArray == null || fileArray.length == 0) {
    		TMResult tmRes = new TMResult(resultList, 0, po);
    		renderJSON(JsonUtil.getJson(tmRes));
    	}
    	int offset = po.getOffset();
    	for (int i = 0; i < ps && i + offset < fileArray.length; i++) {
    		String imgPath = WaterMarkerImgFolderPath + "/" + type + "/" + fileArray[i + offset];
    		resultList.add(imgPath);
    	}
    	
    	TMResult tmRes = new TMResult(resultList, fileArray.length, po);
		renderJSON(JsonUtil.getJson(tmRes));
    	
    }

    //为了给文件夹取得唯一的名字，所以同步
    private synchronized static long getSynchronizedTime() {
        return System.currentTimeMillis();
    }

    /**
     * 生成水印，生成文件夹下载
     */
    /*public static void generateWaterMarks(String numIids, String iconPath, double posX, double posY, double mainWidth,
            double mainHeight, double hoverWidth, double hoverHeight) {
        //要生成水印的宝贝
        if (StringUtils.isEmpty(numIids)) {
            renderError("亲，您尚未选择要添加水印的宝贝");
        }
        User user = getUser();
        List<ItemPlay> itemList = ItemDao.findByIds(user.getId(), numIids);
        if (CommonUtils.isEmpty(itemList)) {
            renderError("亲，您尚未选择要添加水印的宝贝");
        }

        //得到水印图片
        BufferedImage iconImg = readIconImg(iconPath);

        //生成文件目标文件夹

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dayStr = sdf.format(new Date());
        File targetFolder = new File(WaterMarkerOutDir, dayStr);
        targetFolder = new File(targetFolder, user.getId() + "");
        long curTime = getSynchronizedTime();
        targetFolder = new File(targetFolder, curTime + "");

        String secondFolder = dayStr + "/" + user.getId() + "/" + curTime;

        List<ItemPlay> errorItemList = new ArrayList<ItemPlay>();
        boolean isAllFail = true;
        for (ItemPlay itemPlay : itemList) {
            try {
                if (itemPlay == null) {
                    continue;
                }
                //得到宝贝图片
                String srcPath = itemPlay.getPicURL();
                if (StringUtils.isEmpty(srcPath)) {
                    errorItemList.add(itemPlay);
                    continue;
                }
                BufferedImage srcImg = MarkImageUtil.readImage(srcPath, true, false);
                if (srcImg == null) {
                    errorItemList.add(itemPlay);
                    continue;
                }

                //计算比例
                int srcWidth = srcImg.getWidth();
                int srcHeight = srcImg.getHeight();
                double xTimes = 1.0 * srcWidth / mainWidth;
                double yTimes = 1.0 * srcHeight / mainHeight;
                int iconWidth = (int) (xTimes * hoverWidth);
                int iconHeight = (int) (yTimes * hoverHeight);
                //放大或缩小icon
                BufferedImage newIconImg = MarkImageUtil.zoomImage(iconImg, iconWidth, iconHeight);
                int iconPosX = (int) (xTimes * posX);
                int iconPosY = (int) (yTimes * posY);

                //生成图片文件
                String format = MarkImageUtil.getFormat(srcPath, true);
                String targetPath = new File(targetFolder, itemPlay.getNumIid() + "." + format).getAbsolutePath();
                MarkImageUtil.addWaterMarker(srcImg, newIconImg, targetPath, iconPosX, iconPosY);
                isAllFail = false;
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                errorItemList.add(itemPlay);
            }
        }

        if (isAllFail == true) {
            renderError("亲，生成水印失败了，请联系我们");
        }

        //导出zip包
        sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
        String zipFileName = sdf.format(new Date()) + "WaterMarker.zip";
        File zipFile = new File(targetFolder, zipFileName);

        boolean isSuccess = ZipUtil.createZip(zipFile, targetFolder);
        if (isSuccess == false) {
            renderError("亲，系统出现了一些问题，请联系我们");
        }

        
        //把文件夹放到session
        session.put(WaterMarkerOutZipKey, zipFile.getAbsolutePath());

        if (!CommonUtils.isEmpty(errorItemList)) {
            renderSuccess("亲，" + errorItemList.size() + "宝贝生成水印失败了，请联系我们", "");
        }

        renderSuccess("水印生成成功，马上开始下载", "");

    }*/
    
    
    private static File getTargetFolder(User user) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dayStr = sdf.format(new Date());
    	File targetFolder = new File(WaterMarkerOutDir, dayStr);
        targetFolder = new File(targetFolder, user.getId() + "");
        long curTime = getSynchronizedTime();
        targetFolder = new File(targetFolder, curTime + "");
        if (targetFolder.exists() == false) {
        	targetFolder.mkdirs();
        }

        return targetFolder;
    }
    
    private static String getTargetPath(File targetFolder, Long numIid, String format) {
    	String targetPath = new File(targetFolder, numIid + "." + format).getAbsolutePath();
    	return targetPath;
    }
    
    
    /**
     * 生成水印，直接修改主图
     */
    public static void generateWaterMarks(String numIids, String iconPath, double posX, double posY, double mainWidth,
            double mainHeight, double hoverWidth, double hoverHeight) {
    	
    	
        //要生成水印的宝贝
        if (StringUtils.isEmpty(numIids)) {
            renderError("亲，您尚未选择要添加水印的宝贝");
        }
        User user = getUser();
        List<ItemPlay> itemList = ItemDao.findByIds(user.getId(), numIids);
        if (CommonUtils.isEmpty(itemList)) {
            renderError("亲，您尚未选择要添加水印的宝贝");
        }
        
        /*WaterMarkerRecord record = WaterMarkerRecord.findByRecordName(user.getId(), recordName);
        if (record != null) {
        	renderError("亲，该备份名称已经存在，请换个名称！");
        }*/

        //得到水印图片
        BufferedImage iconImg = readIconImg(iconPath);

        //生成文件目标文件夹
        File targetFolder = getTargetFolder(user);

        List<ItemPlay> errorItemList = new ArrayList<ItemPlay>();
        List<ItemPlay> successItemList = new ArrayList<ItemPlay>();
        boolean isAllFail = true;
        for (ItemPlay itemPlay : itemList) {
            try {
                if (itemPlay == null) {
                    continue;
                }
                //得到宝贝图片
                String srcPath = itemPlay.getPicURL();
                if (StringUtils.isEmpty(srcPath)) {
                    errorItemList.add(itemPlay);
                    continue;
                }
                BufferedImage srcImg = MarkImageUtil.readImage(srcPath, true, false);
                if (srcImg == null) {
                    errorItemList.add(itemPlay);
                    continue;
                }

                //计算比例
                int srcWidth = srcImg.getWidth();
                int srcHeight = srcImg.getHeight();
                double xTimes = 1.0 * srcWidth / mainWidth;
                double yTimes = 1.0 * srcHeight / mainHeight;
                int iconWidth = (int) (xTimes * hoverWidth);
                int iconHeight = (int) (yTimes * hoverHeight);
                //放大或缩小icon
                BufferedImage newIconImg = MarkImageUtil.zoomImage(iconImg, iconWidth, iconHeight);
                int iconPosX = (int) (xTimes * posX);
                int iconPosY = (int) (yTimes * posY);

                //生成图片文件
                String format = MarkImageUtil.getFormat(srcPath, true);
                String targetPath = getTargetPath(targetFolder, itemPlay.getNumIid(), format);
                MarkImageUtil.addWaterMarker(srcImg, newIconImg, targetPath, iconPosX, iconPosY);
                
                //这是我原来的图片。。。。
                //targetPath = "F:/itempicbak/" + itemPlay.getNumIid() + ".jpg";
                File newImgFile = new File(targetPath);
                ItemApi.ItemImageUpdater api = new ItemApi.ItemImageUpdater(user.getSessionKey(), itemPlay.getNumIid(), newImgFile);
                Item newItem = api.call();
                if (newItem == null || api.isApiSuccess() == false) {
                	errorItemList.add(itemPlay);
                	continue;
                }
                successItemList.add(itemPlay);
                isAllFail = false;
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                errorItemList.add(itemPlay);
            }
        }
        //删除图片
        deleteFolder(targetFolder);
        
        if (isAllFail == true) {
            renderError("亲，所有宝贝的水印都失败了，请联系我们");
        }
        
        //生成记录，这要在更新宝贝图片之前
        for (ItemPlay itemPlay : successItemList) {
        	Long numIid = itemPlay.getNumIid();
        	WaterMarkerOriginImage originItem = WaterMarkerOriginImage.findByNumIid(user.getId(), numIid);
        	if (originItem == null) {
        		originItem = new WaterMarkerOriginImage(user.getId(), itemPlay.getNumIid(),
            			itemPlay.getPicURL(), null, new Date().getTime());
        		originItem.save();
        	} else {
        		originItem.setTs(new Date().getTime());
        		originItem.save();
        	}
        	
        }
        
        //更新宝贝图片
        updateItemPics(user, successItemList);

        
        if (!CommonUtils.isEmpty(errorItemList)) {
            renderSuccess("亲，" + errorItemList.size() + "宝贝生成水印失败了，请联系我们", "");
        }

        renderSuccess("水印生成成功！", "");

    }
    
    //更新宝贝图片路径
    private static void updateItemPics(User user, List<ItemPlay> successItemList) {
    	List<Long> numIidList = new ArrayList<Long>();
    	Map<Long, ItemPlay> itemPlayMap = new HashMap<Long, ItemPlay>();
    	for (ItemPlay itemPlay : successItemList) {
    		numIidList.add(itemPlay.getNumIid());
    		itemPlayMap.put(itemPlay.getNumIid(), itemPlay);
    	}
    	ItemApi.ItemsListGet getApi = new ItemApi.ItemsListGet(numIidList, false);
    	List<Item> getItemList = getApi.call();
    	if (getItemList == null)
			getItemList = new ArrayList<Item>();
    	
    	for (Item getItem : getItemList) {
    		Long numIid = getItem.getNumIid();
    		ItemPlay itemPlay = itemPlayMap.get(numIid);
    		itemPlay.setPicURL(getItem.getPicUrl());
    		itemPlay.jdbcSave();
    	}
    	
    }
    
    

    private static BufferedImage readIconImg(String iconPath) {
        if (StringUtils.isEmpty(iconPath)) {
            renderError("亲，您尚未选择水印图标");
        }
        iconPath = iconPath.trim();
        if (iconPath.startsWith("/"))
            iconPath = iconPath.substring(1);
        //iconPath = new File(Play.applicationPath, iconPath).getAbsolutePath();

        //把iconPath组装成url
        try {
            URL absoluteUrl = new URL(request.getBase());
            URL url = new URL(absoluteUrl, iconPath);
            iconPath = url.toString();
            //log.error(iconPath);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            renderError("亲，生成失败，找不到水印文件，请联系我们");
            return null;
        }

        BufferedImage iconImg = MarkImageUtil.readImage(iconPath, true, true);
        if (iconImg == null) {
            renderError("亲，生成失败，找不到水印文件，请联系我们");
        }

        return iconImg;
    }

    

    public static void downloadWaterMarker() {
        String zipPath = session.get(WaterMarkerOutZipKey);
        session.remove(WaterMarkerOutZipKey);
        if (StringUtils.isEmpty(zipPath))
            notFound();
        File file = new File(zipPath);
        if (file.exists() == false)
            notFound();
        renderBinary(file);
    }

    
    /**
     * 测试用的，查看picUrl还是不是一样的
     * @param numIid
     */
    public static void showItemPlay(Long numIid) {
    	User user = getUser();
    	ItemPlay itemPlay = ItemDao.findByNumIid(user.getId(), numIid);
    	renderJSON(itemPlay);
    }
    
    public static void showItem(Long numIid) {
    	User user = getUser();
    	ItemApi.ItemGet api = new ItemApi.ItemGet(user, numIid);
    	Item item = api.call();
    	renderJSON(item);
    }
    
    public static void clearOutDateFiles() {
    	ClearOutDateFilesJob clearJob = new ClearOutDateFilesJob();
    	clearJob.doJob();
    }
    
}
