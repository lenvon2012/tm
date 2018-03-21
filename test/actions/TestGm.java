
package actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.core.IdentifyCmd;
import org.im4java.process.ArrayListOutputConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;

public class TestGm {

    private static final Logger log = LoggerFactory.getLogger(TestGm.class);

    public static final String TAG = "TestGm";

    /**
     * * 获得图片文件大小[小技巧来获得图片大小] * * @param filePath * 文件路径 *
     * 
     * @return 文件大小
     */

    public int getSize(String imagePath) {
        int size = 0;
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(imagePath);
            size = inputStream.available();
            inputStream.close();
            inputStream = null;
        } catch (FileNotFoundException e) {
            size = 0;
            System.out.println("文件未找到!");
        } catch (IOException e) {
            size = 0;
            System.out.println("读取文件大小错误!");
        } finally {
            // 可能异常为关闭输入流,所以需要关闭输入流
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    System.out.println("关闭文件读入流异常");
                }
                inputStream = null;

            }
        }
        return size;
    }

    /**
     * 获得图片的宽度
     * 
     * @param filePath
     *            文件路径
     * @return 图片宽度
     */
    public int getWidth(String imagePath) {
        int line = 0;
        try {
            IMOperation op = new IMOperation();
            op.format("%w"); // 设置获取宽度参数
            op.addImage(1);
            IdentifyCmd identifyCmd = new IdentifyCmd(true);
            ArrayListOutputConsumer output = new ArrayListOutputConsumer();
            identifyCmd.setOutputConsumer(output);
            identifyCmd.run(op, imagePath);
            ArrayList<String> cmdOutput = output.getOutput();
            assert cmdOutput.size() == 1;
            line = Integer.parseInt(cmdOutput.get(0));
        } catch (Exception e) {
            line = 0;
            System.out.println("运行指令出错!");
        }
        return line;
    }

    /**
     * 获得图片的高度
     * 
     * @param imagePath
     *            文件路径
     * @return 图片高度
     */
    public int getHeight(String imagePath) {
        int line = 0;
        try {
            IMOperation op = new IMOperation();

            op.format("%h"); // 设置获取高度参数
            op.addImage(1);
            IdentifyCmd identifyCmd = new IdentifyCmd(true);
            ArrayListOutputConsumer output = new ArrayListOutputConsumer();
            identifyCmd.setOutputConsumer(output);
            identifyCmd.run(op, imagePath);
            ArrayList<String> cmdOutput = output.getOutput();
            assert cmdOutput.size() == 1;
            line = Integer.parseInt(cmdOutput.get(0));
        } catch (Exception e) {
            line = 0;
            System.out.println("运行指令出错!" + e.toString());
        }
        return line;
    }

    /**
     * 图片信息
     * 
     * @param imagePath
     * @return
     */
    public static String getImageInfo(String imagePath) {
        String line = null;
        try {
            IMOperation op = new IMOperation();
            op.format("width:%w,height:%h,path:%d%f,size:%b%[EXIF:DateTimeOriginal]");
            op.addImage(1);
            IdentifyCmd identifyCmd = new IdentifyCmd(true);
            ArrayListOutputConsumer output = new ArrayListOutputConsumer();
            identifyCmd.setOutputConsumer(output);
            identifyCmd.run(op, imagePath);
            ArrayList<String> cmdOutput = output.getOutput();
            assert cmdOutput.size() == 1;
            line = cmdOutput.get(0);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return line;
    }

    /**
     * 裁剪图片
     * 
     * @param imagePath
     *            源图片路径
     * @param newPath
     *            处理后图片路径
     * @param x
     *            起始X坐标
     * @param y
     *            起始Y坐标
     * @param width
     *            裁剪宽度
     * @param height
     *            裁剪高度
     * @return 返回true说明裁剪成功,否则失败
     */
    public boolean cutImage(String imagePath, String newPath, int x, int y,
            int width, int height) {
        boolean flag = false;
        try {
            IMOperation op = new IMOperation();
            op.addImage(imagePath);
            /** width：裁剪的宽度 * height：裁剪的高度 * x：裁剪的横坐标 * y：裁剪纵坐标 */
            op.crop(width, height, x, y);
            op.addImage(newPath);
            ConvertCmd convert = new ConvertCmd(true);
            convert.run(op);
            flag = true;
        } catch (IOException e) {
            System.out.println("文件读取错误!");
            flag = false;
        } catch (InterruptedException e) {
            flag = false;
        } catch (IM4JavaException e) {
            flag = false;
        } finally {

        }
        return flag;
    }

    /**
     * 根据尺寸缩放图片[等比例缩放:参数height为null,按宽度缩放比例缩放;参数width为null,按高度缩放比例缩放]
     * 
     * @param imagePath
     *            源图片路径
     * @param newPath
     *            处理后图片路径
     * @param width
     *            缩放后的图片宽度
     * @param height
     *            缩放后的图片高度
     * @return 返回true说明缩放成功,否则失败
     */
    public boolean zoomImage(String imagePath, String newPath, Integer width,
            Integer height) {

        boolean flag = false;
        try {
            IMOperation op = new IMOperation();
            op.addImage(imagePath);
            if (width == null) {// 根据高度缩放图片
                op.resize(null, height);
            } else if (height == null) {// 根据宽度缩放图片
                op.resize(width);
            } else {
                op.resize(width, height);
            }
            op.addImage(newPath);
            ConvertCmd convert = new ConvertCmd(true);
            convert.run(op);
            flag = true;
        } catch (IOException e) {
            System.out.println("文件读取错误!");
            flag = false;
        } catch (InterruptedException e) {
            flag = false;
        } catch (IM4JavaException e) {
            flag = false;
        } finally {

        }
        return flag;
    }

    /**
     * 图片旋转
     * 
     * @param imagePath
     *            源图片路径
     * @param newPath
     *            处理后图片路径
     * @param degree
     *            旋转角度
     */
    public boolean rotate(String imagePath, String newPath, double degree) {
        boolean flag = false;
        try {
            // 1.将角度转换到0-360度之间
            degree = degree % 360;
            if (degree <= 0) {
                degree = 360 + degree;
            }
            IMOperation op = new IMOperation();
            op.addImage(imagePath);
            op.rotate(degree);
            op.addImage(newPath);
            ConvertCmd cmd = new ConvertCmd(true);
            cmd.run(op);
            flag = true;
        } catch (Exception e) {
            flag = false;
            System.out.println("图片旋转失败!");
        }
        return flag;
    }

    public static void main(String[] args) throws Exception {
        TestGm imageUtil = new TestGm();

        System.out.println("原图片大小:" + imageUtil.getSize("d://test.jpg") + "Bit");
        System.out.println("原图片宽度:" + imageUtil.getWidth("d://test.jpg"));
        System.out.println("原图片高度:" + imageUtil.getHeight("d://test.jpg"));
        if (imageUtil.zoomImage("d://test.jpg", "d://test1.jpg", 500, null)) {
            if (imageUtil.rotate("d://test.jpg", "d://test2.jpg", 15)) {
                if (imageUtil.cutImage("d://test2.jpg", "d://test3.jpg", 32,
                        105, 200, 200)) {
                    System.out.println("编辑成功");
                } else {
                    System.out.println("编辑失败03");
                }
            } else {
                System.out.println("编辑失败02");
            }
        } else {
            System.out.println("编辑失败01");
        }

    }

    /* 根据尺寸缩放图片
     * 
     * @author tanjun
     * @date 2013年9月6日  
     * @param path 
     *           源图路径
     * @param width
     *           压缩后宽度
     * @param height
     *          压缩后高度
     * @param type 
     *          1为像素，2为百分比处理，如（像素大小：1024x1024,百分比：50%x50%）
     * @return
     * @throws Exception
     */
    public static String createThumbnail(String path, int width, int height, String type) throws Exception {

        IMOperation op = new IMOperation();
        ConvertCmd cmd = new ConvertCmd(true);
        String newFileName = null;
        //文件名前缀
        String prevFileName = null;
        try {
            op.addImage();
            String raw = "";
            if ("1".equals(type)) {
                //按像素
                raw = width + "x" + height + "!";
                prevFileName = width + "x" + height + "_";
            } else {
                //按百分比
                raw = width + "%x" + height + "%";
                prevFileName = width + "%x" + height + "%_";
            }
            //压缩
            op.addRawArgs("-thumbnail", raw);
            //图片质量
            op.addRawArgs("-quality", "100");
            op.addImage();
            //系统类型
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.indexOf("win") != -1) {
                // linux下不要设置此值，不然会报错
                cmd.setSearchPath("/data/top/GraphicsMagick-1.3.19");
            }
            //读取配置文件：工程路径
            File dir = new File(Play.tmpDir, "testcreateimg");
            String filePath = dir.getAbsolutePath();
            String fpath[] = filePath.split("/");
            //原图名称
            String oldFileName = fpath[fpath.length - 1];
            //压缩图名称
            String fileName = System.currentTimeMillis() + ".jpg";
            ;
            //压缩后的新文件名
            newFileName = prevFileName + fileName;
            //新文件路径
            String newfile = filePath.replace(oldFileName, newFileName);
            //压缩
            cmd.run(op, filePath, newfile);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }

        return "";
    }
}
