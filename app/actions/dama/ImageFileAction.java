package actions.dama;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import result.TMResult;

public class ImageFileAction {
    
    private static final Logger log = LoggerFactory.getLogger(ImageFileAction.class);
    
    private static final String ImageFolderPrefix = Play.configuration.getProperty("tmpimg.savefilepath",
            "auto/tmpimg");

    /** 
     * 根据路径 下载图片 然后 保存到对应的目录下 
     */  
    public static File download(String urlString, String codeType){  
        
        Calendar now = Calendar.getInstance();       
        now.setTimeInMillis(System.currentTimeMillis());
        int year = now.get(Calendar.YEAR);       
        int month = now.get(Calendar.MONTH);
        int day = now.get(Calendar.DAY_OF_MONTH);
        
        final String relativePath = year + "/" + (month + 1) + "-" + day + "/";
        final String absolutePath = getAbsolutePath(relativePath);
        
        File folder = new File(absolutePath);
        if (folder.exists() == false) {
            folder.mkdirs();
        }
        
       File sf = new File(folder, genFileName(codeType));
        
        while (sf.exists()) {
            sf = new File(folder, genFileName(codeType)
                    + "_" + new Random().nextInt(100000));
        }

        InputStream is = null;
        FileOutputStream os = null;
        
        try {
            URL uri = new URL(urlString);
            is = uri.openStream();       
            os = new FileOutputStream(sf);
            
            byte[] bs = new byte[1024];  
            int len;  
            while ((len = is.read(bs)) != -1) {  
              os.write(bs, 0, len);  
            }  
            
            // 刷新此缓冲的输出流
            os.flush();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }finally{
            // 完毕，关闭所有链接  
            if(os != null){
                try {
                    os.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }   
            }
               
            if(is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }   
            }

        }  
        log.info(" download image file success !!! ");
        return sf;

    }   
      
    /**
     * 截图
     * @param codeType 验证码类型
     * @param image
     * @return
     */
    public static TMResult oprateCodePic(String codeType, 
            OperateImageFile image){
        
        Calendar now = Calendar.getInstance();       
        now.setTimeInMillis(System.currentTimeMillis());
        int year = now.get(Calendar.YEAR);       
        int month = now.get(Calendar.MONTH);
        int day = now.get(Calendar.DAY_OF_MONTH);
        
        final String relativePath = year + "/" + (month + 1) + "-" + day + "/";
        final String absolutePath = getAbsolutePath(relativePath);
        
        File folder = new File(absolutePath);
        if (folder.exists() == false) {
            folder.mkdirs();
        }
        
       File targetFile = new File(folder, genFileName(codeType));
        
        while (targetFile.exists()) {
            targetFile = new File(folder, genFileName(codeType)
                    + "_" + new Random().nextInt(100000));
        }
        
        image.setSubFile(targetFile);
        File subFile = image.cutImage();
        if (subFile == null) {
            return new TMResult(false, "截图失败！", null);
        } 
        
        return new TMResult(true,"",subFile);
    }

    private static String getAbsolutePath(String relativePath) {
        String absolutePth = "";
        if (ImageFolderPrefix.trim().endsWith("/")) {
            absolutePth = ImageFolderPrefix.trim() + relativePath;
        } else {
            absolutePth = ImageFolderPrefix.trim() + "/" + relativePath;
        }
        
        return absolutePth;
    }
    
    private static  String genFileName(String codeType) {
        
        String fileName = "";
        
        fileName += codeType + "_";
        String timeStr = System.currentTimeMillis() + "";
        
        if (timeStr.length() > 5) {
            timeStr = timeStr.substring(timeStr.length() - 5);
        }
        
        fileName += timeStr;
        
        return fileName;
        
    }
    
    public static class OperateImageFile {
        private File srcFile;//源文件
        private File subFile;//输出文件地址
        private String imageType;//文件类型
        private int x;
        private int y;
        private int width;
        private int height;

        public OperateImageFile() {
        }

        public OperateImageFile(File srcFile,  int x, int y, int width, int height) {
            this.srcFile = srcFile;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }


        public File getSrcFile() {
            return srcFile;
        }

        public void setSrcFile(File srcFile) {
            this.srcFile = srcFile;
        }
        
        public File getSubFile() {
            return subFile;
        }

        public void setSubFile(File subFile) {
            this.subFile = subFile;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public String getImageType() {
            if (StringUtils.isEmpty(imageType)) {
                imageType = getFormatName(srcFile);
            }
            return imageType;
        }

        public void setImageType(String imageType) {
            this.imageType = imageType;
        }


        public  File cutImage() {
            FileInputStream is = null;
            ImageInputStream iis = null;
            try {
                    is = new FileInputStream(srcFile);

                    Iterator<ImageReader> it = ImageIO.getImageReadersByFormatName(this.getImageType());           
                    ImageReader reader = it.next();
                    
                    // 获取图片流  
                    iis = ImageIO.createImageInputStream(is);
                    reader.setInput(iis, true); 
                    ImageReadParam param = reader.getDefaultReadParam();
                    /** 
                     * 图片裁剪区域。Rectangle 指定了坐标空间中的一个区域，通过 Rectangle 对象 
                     * 的左上顶点的坐标(x，y)、宽度和高度可以定义这个区域。 
                     */
                    Rectangle rect = new Rectangle(x, y, width, height);
                    param.setSourceRegion(rect);
                    
                    /** 
                     * 使用所提供的 ImageReadParam 读取通过索引 imageIndex 指定的对象
                     */ 
                    BufferedImage bi = reader.read(0, param);
                    // 保存新图片  
                    ImageIO.write(bi, this.getImageType(), subFile);
                     } catch (Exception e) {
                       log.error(e.getMessage(), e);
                       return null;
                   } finally {
                       if (is != null){
                           try {                 
                               is.close();
                           } catch (Exception ex) {
                               log.error(ex.getMessage(), ex);
                           }
                       }
                       if (iis != null){
                           try {
                               iis.close();
                           } catch (Exception ex) {
                               log.error(ex.getMessage(), ex);
                           }
                       }
                       
                   }
            
            return subFile;
        }

        private String getFormatName(Object o) {
            try {
                
                   ImageInputStream iis = ImageIO.createImageInputStream(o);
                   Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);
                   if (!iter.hasNext()) {
                       return null;
                   }
                   ImageReader reader = iter.next();
                   iis.close();
                   return reader.getFormatName();
                 } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }

            return null;
        }
    }

    
}
