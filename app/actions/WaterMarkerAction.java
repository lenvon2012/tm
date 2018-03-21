
package actions;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 给图片添加水印
 * @author Administrator
 *
 */
public class WaterMarkerAction {
    private static final Logger log = LoggerFactory.getLogger(WaterMarkerAction.class);

    /**
     * 图片工具类
     * @author Administrator
     *
     */
    public static class MarkImageUtil {

        public static String getFormat(String targetPath, boolean isUrl) {
            String defaultFormat = "JPG";
            if (StringUtils.isEmpty(targetPath))
                return defaultFormat;
            if (isUrl == true) {//去掉?号
                int tempIndex = targetPath.lastIndexOf("?");
                if (tempIndex >= 0) {
                    targetPath = targetPath.substring(0, tempIndex).trim();
                }
            }
            int index = targetPath.lastIndexOf(".");
            if (index < 0 || index >= (targetPath.length() - 1))
                return defaultFormat;
            return targetPath.substring(index + 1);
        }

        public static void addWaterMarker(BufferedImage srcImg, BufferedImage iconImg,
                String targetPath, int posX, int posY) throws Exception {

            BufferedImage targetImg = new BufferedImage(srcImg.getWidth(null),
                    srcImg.getHeight(null), srcImg.getType());

            // 得到画笔对象 
            // Graphics g= buffImg.getGraphics();   
            Graphics2D g = targetImg.createGraphics();

            //设置透明
            //g.getDeviceConfiguration().createCompatibleImage(srcImg.getWidth(), srcImg.getHeight(),Transparency.TRANSLUCENT);  
            //g.dispose();  
            //g = targetImg.createGraphics();   

            // 设置对线段的锯齿状边缘处理   
            //g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,   
            //        RenderingHints.VALUE_INTERPOLATION_BILINEAR);   

            g.drawImage(srcImg.getScaledInstance(srcImg.getWidth(null), srcImg
                    .getHeight(null), Image.SCALE_SMOOTH), 0, 0, null);

            //float alpha = 1f; // 透明度   
            //g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP,   
            //        alpha));   

            // 表示水印图片的位置   
            g.drawImage(iconImg, posX, posY, null);

            //g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));   

            g.dispose();

            File outFile = new File(targetPath);
            File outFolder = outFile.getParentFile();
            if (outFolder != null && outFolder.exists() == false) {
                outFolder.mkdirs();
            }

            String format = "";
            format = getFormat(targetPath, false);
            //log.error("图片格式：" + format);
            // 生成图片   
            //format = "png";
            ImageIO.write(targetImg, format, outFile);
            log.error("water marker at: " + outFile.getAbsolutePath());
            //log.error("图片完成添加Icon印章。。。。。。");   
        }

        /**
         * 缩放图片
         * @param originImg
         * @param times
         * @return
         */
        public static BufferedImage zoomImage(BufferedImage originImg, int targetWidth, int targetHeight) {

            BufferedImage newImg = new BufferedImage(targetWidth, targetHeight, originImg.getType());
            Graphics2D g = newImg.createGraphics();
            g.drawImage(originImg.getScaledInstance(targetWidth, targetHeight,
                    Image.SCALE_SMOOTH), 0, 0, targetWidth, targetHeight, null);
            g.dispose();
            return newImg;
        }

        public static BufferedImage partHeight(BufferedImage originImg, int startHeight, int endHeight) {
            int targetHeight = endHeight - startHeight;
            BufferedImage subimage = originImg.getSubimage(0, startHeight, originImg.getWidth(), targetHeight);
            return subimage;
        }

        /**
         * 读取图片，不用ImageIO.read，因为ImageIO.read会丢失ICC信息
         * @param path
         * @param isUrl
         * @param isAlpha 是否携带透明信息，如果底图也携带的话，图片数据就太大了
         * @return
         */
        public static BufferedImage readImage(String path, boolean isUrl, boolean isAlpha) {
            try {
                Image image = null;
                if (isUrl == false) {
                    image = Toolkit.getDefaultToolkit().getImage(path);
                } else {
                    URL url = new URL(path);
                    image = Toolkit.getDefaultToolkit().getImage(url);
                }
                if (image == null) {
                    log.error("the image from: " + path + "　is null!!!!!");
                    return null;
                }

                BufferedImage bImage = buildImageBuffer(isAlpha, image);
                return bImage;
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                return null;
            }
        }

        static int defaultTimeout = 30000;

        public static BufferedImage readImage(URL url, boolean isAlpha) {

            int count = 3;
            while (count-- > 0) {
                try {
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    URLConnection conn = url.openConnection();
                    conn.setConnectTimeout(defaultTimeout);
                    InputStream inputStream = conn.getInputStream();
                    IOUtils.copy(inputStream, output);

//                    ByteArrayInputStream in = new ByteArrayInputStream(output.toByteArray());
                    byte[] b = output.toByteArray();
                    Image image = Toolkit.getDefaultToolkit().createImage(b, 0, b.length);
//                    BufferedImage image = ImageIO.read(in);
                    BufferedImage bufferedImage = buildImageBuffer(isAlpha, image);

                    IOUtils.closeQuietly(inputStream);
//                    IOUtils.closeQuietly(in);
                    IOUtils.closeQuietly(output);

                    return bufferedImage;

                } catch (IOException e) {
                    log.warn(e.getMessage(), e);
                }
            }
            return null;
        }

        private static BufferedImage buildImageBuffer(boolean isAlpha, Image image) {
            BufferedImage bImage = null;
            if (image instanceof BufferedImage) {
                return (BufferedImage) image;
            }
            image = new ImageIcon(image).getImage();
            int width = image.getWidth(null);
            int height = image.getHeight(null);
            if (width < 0 || height < 0) {
                return null;
            }
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            try {

                int transparency = Transparency.TRANSLUCENT;
                if (isAlpha == false) {
                    transparency = Transparency.OPAQUE;
                    GraphicsDevice gs = ge.getDefaultScreenDevice();
                    GraphicsConfiguration gc = gs.getDefaultConfiguration();
                    bImage = gc.createCompatibleImage(width, height, transparency);
                }
            } catch (HeadlessException ex) {
                log.error(ex.getMessage(), ex);
                return null;
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                return null;
            }

            if (bImage == null) {
                int type = BufferedImage.TYPE_4BYTE_ABGR_PRE;
                if (isAlpha == false)
                    type = BufferedImage.TYPE_INT_RGB;
                bImage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
            }
            Graphics2D g = bImage.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
            return bImage;
        }

    }

    public static void main(String[] args) {
        try {
            String srcImgPath = "http://img02.taobaocdn.com/bao/uploaded/i2/T1pVYVXfJfXXXHY6Q8_100937.jpg";
            String iconImgPath = "C:/test/icon.png";
            String targetImgPath = "C:/test/target.jpg";

            BufferedImage iconImg = MarkImageUtil.readImage(iconImgPath, false, true);
            iconImg = MarkImageUtil.zoomImage(iconImg, 40, 20);

            BufferedImage srcImg = MarkImageUtil.readImage(srcImgPath, true, false);
            log.error(srcImg.getType() + "");
            log.error(BufferedImage.TYPE_INT_RGB + "");
            MarkImageUtil.addWaterMarker(srcImg, iconImg, targetImgPath, 10, 10);

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    public static class ZipUtil {
        public static boolean createZip(File zipFile, File folderFile) {
            try {
                executeZip(zipFile.getAbsolutePath(), folderFile.getAbsolutePath());
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                return false;
            }
            return true;
        }

        private static void executeZip(String zipPath, String sourceDir) throws Exception
        {
            File dir = new File(sourceDir);
            if (!dir.exists())
                throw new Exception("找不到打包的文件夹：" + sourceDir);
            if (dir.isDirectory() == false)
                throw new Exception("这不是打包的文件夹：" + sourceDir + "。而是文件。");

            File zipFile = new File(zipPath);
            if (zipFile.exists() == false)
                zipFile.getParentFile().mkdirs();
            OutputStream outStream = null;
            ZipOutputStream zos = null;
            String zipEntryName = zipFile.getName().replace(".zip", "");
            try
            {
                File[] files = dir.listFiles();
                outStream = new FileOutputStream(zipPath);
                zos = new ZipOutputStream(outStream);

                for (int i = 0; i < files.length; i++)
                {
                    File file = files[i];
                    //跳过zip包自己
                    if (file.getName().equals(zipFile.getName()))
                        continue;
                    if (file.isDirectory())
                        doZipOneDir(zos, file, zipEntryName + "/" + file.getName());
                    else
                        doZipOneFile(zos, file, zipEntryName);
                }
            } catch (Exception ex)
            {
                throw ex;
            } finally
            {
                if (zos != null)
                    zos.close();
                if (outStream != null)
                    outStream.close();
            }

        }

        //递归
        private static void doZipOneDir(ZipOutputStream zos, File dir, String entryName)
        {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++)
            {
                File file = files[i];
                if (file.isDirectory())
                    doZipOneDir(zos, file, entryName + "/" + file.getName());
                else
                    doZipOneFile(zos, file, entryName);
            }
        }

        private static void doZipOneFile(ZipOutputStream zos, File file, String entryName)
        {
            InputStream inStream = null;
            byte[] buffer = new byte[1024];
            try
            {
                inStream = new FileInputStream(file);
                ZipEntry entry = new ZipEntry(entryName + "/" + file.getName());
                zos.putNextEntry(entry);
                int len = 0;
                while ((len = inStream.read(buffer)) >= 0)
                {
                    //将字节数组写入当前 ZIP 条目数据
                    zos.write(buffer, 0, len);
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            } finally
            {
                try
                {
                    if (inStream != null)
                        inStream.close();
                    zos.closeEntry();
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }

        }
    }
}
