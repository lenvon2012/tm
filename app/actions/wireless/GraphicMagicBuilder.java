
package actions.wireless;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang.StringUtils;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.core.IdentifyCmd;
import org.im4java.process.ArrayListOutputConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;

import utils.PlayUtil;

import com.ciaosir.client.utils.NumberUtil;

public class GraphicMagicBuilder {

    public GraphicMagicBuilder() {

    }

    private static final Logger log = LoggerFactory.getLogger(GraphicMagicBuilder.WidthXHeight.class);

    public static final String TAG = "GraphicMagicBuilder.WidthXHeight";

    public static class WidthXHeight {
        int width;

        int height;

        public WidthXHeight() {
            super();
        }

        public WidthXHeight(int width, int height) {
            super();
            this.width = width;
            this.height = height;
        }

        @Override
        public String toString() {
            return "WidthXHeight [width=" + width + ", height=" + height + "]";
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public static WidthXHeight parse(String widthXHeight) {
            widthXHeight = widthXHeight.trim();
            String[] split = StringUtils.split(widthXHeight, 'x');
            int width = NumberUtil.parserInt(split[0], 0);
            int height = NumberUtil.parserInt(split[1], 0);

            return new WidthXHeight(width, height);
        }

    }

    public IdentifyCmd genIdCommd() {
        IdentifyCmd identifyCmd = new IdentifyCmd(true);
        if (PlayUtil.getOS().name().indexOf("win") >= 0) { //linux下不要设置此值，不然会报错
            String path = Play.configuration.getProperty("D:\\xxxxx", "???").toString();
            identifyCmd.setSearchPath(path);
        }
        return identifyCmd;
    }

    public ConvertCmd genConvertCommd() {
        ConvertCmd convert = new ConvertCmd(true);
        if (PlayUtil.getOS().name().indexOf("win") >= 0) { //linux下不要设置此值，不然会报错
            String path = Play.configuration.getProperty("D:\\xxxxx", "???").toString();
            convert.setSearchPath(path);
        }
        return convert;
    }

    public WidthXHeight getWidthAndHeight(String imagePath) {
        try {
            IMOperation op = new IMOperation();
            op.format("%wx%h"); // 设置获取宽度参数
            op.addImage(1);
            IdentifyCmd identifyCmd = genIdCommd();
            ArrayListOutputConsumer output = new ArrayListOutputConsumer();
            identifyCmd.setOutputConsumer(output);
            identifyCmd.run(op, imagePath);
            ArrayList<String> cmdOutput = output.getOutput();
            assert cmdOutput.size() == 1;
            return WidthXHeight.parse(StringUtils.join(cmdOutput, StringUtils.EMPTY));

        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        } catch (InterruptedException e) {
            log.warn(e.getMessage(), e);
        } catch (IM4JavaException e) {
            log.warn(e.getMessage(), e);
        }
        return null;
    }

    public boolean zoom(File src, File out, int width) {

        log.info(format("zoom:src, out, width".replaceAll(", ", "=%s, ") + "=%s", src, out, width));

        return zoom(src.getAbsolutePath(), out.getAbsolutePath(), width);
    }

    public boolean convert(String src, String output) {
        boolean flag = false;
        try {
            IMOperation op = new IMOperation();
            op.addImage(src);
            op.quality(90d);
            op.addImage(output);
            ConvertCmd convert = genConvertCommd();
            convert.run(op);
            flag = true;
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            flag = false;
        }
        return flag;
    }

    public boolean zoom(String imagePath, String newPath, int width) {
        log.info(format("zoom:imagePath, newPath, width".replaceAll(", ", "=%s, ") + "=%s", imagePath, newPath, width));

        boolean flag = false;
        try {
            IMOperation op = new IMOperation();
            op.addImage(imagePath);
//            op.sample(width, 480);
//            op.resize(width, null);
            op.sample(width, null);

            op.quality(90d);
            op.gravity("center");
            op.addImage(newPath);

            ConvertCmd convert = genConvertCommd();
            convert.run(op);

            flag = true;
        } catch (IOException e) {
            log.warn(e.getMessage(), e);

            log.warn("文件读取错误!"
                    + (format("zoom:imagePath, newPath, width".replaceAll(", ", "=%s, ") + "=%s", imagePath, newPath,
                            width)));
            flag = false;
        } catch (InterruptedException e) {
            log.warn(e.getMessage(), e);
            flag = false;
        } catch (IM4JavaException e) {
            log.warn(e.getMessage(), e);
            flag = false;
        } finally {

        }
        return flag;
    }

    public boolean cutImage(File input, File output, int x, int y,
            int width, int height) {

        return cutImage(input.getAbsolutePath(), output.getAbsolutePath(), x, y, width, height);
    }

    public static void main(String[] args) {
        GraphicMagicBuilder builder = new GraphicMagicBuilder();
        boolean res = builder.cutImage("/home/zrb/code/tm/tmp/_tmp_output_382/103962638224071399239991425.jpg",
                "/home/zrb/code/tm/tmp/pic_trans_382/10396263821399239991477_499.jpg", 0, 0, 100, 100);
        System.out.println(res);
    }

    /**
     * - cutImage:srcPath=/home/zrb/code/tm/tmp/_tmp_output_382/103962638224071399239991425.jpg, targetPath=/home/zrb/code/tm/tmp/pic_trans_382/10396263821399239991477_499.jpg, x=0, y=0, width=480, height=499 

    public static void main(String[] args) {
        String path = "/home/zrb/code/tm/tmp/T2XGXCXqlXXXXXXXXX_!!1662033639.jpg";
        GraphicMagicBuilder builder = new GraphicMagicBuilder();
        WidthXHeight model = builder.getWidthAndHeight(path);
        System.out.println(model);
    }
     * @param srcPath
     * @param targetPath
     * @param x
     * @param y
     * @param width
     * @param height
     * @return
     */
    public boolean cutImage(String srcPath, String targetPath, int x, int y,
            int width, int height) {

        log.info(format("cutImage:srcPath, targetPath, x, y, width, height".replaceAll(", ", "=%s, ") + "=%s", srcPath,
                targetPath, x, y, width, height));

        boolean flag = false;
        try {
            IMOperation op = new IMOperation();
            /** width：裁剪的宽度 * height：裁剪的高度 * x：裁剪的横坐标 * y：裁剪纵坐标 */
            op.crop(width, height, x, y);
            op.quality(80d);
            op.addImage(srcPath);
            op.addImage(targetPath);
            ConvertCmd convert = new ConvertCmd(true);
//            System.out.println(op);
            convert.run(op);
            flag = true;
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
            flag = false;
        } catch (InterruptedException e) {
            log.warn(e.getMessage(), e);
            flag = false;
        } catch (IM4JavaException e) {
            log.warn(e.getMessage(), e);
            flag = false;
        } finally {
        }
        return flag;
    }

}
