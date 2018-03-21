
package carrier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.user.User;
import net.sf.oval.guard.Pre;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import sun.text.normalizer.Replaceable;

import actions.wireless.WirelessItemWorker;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.JsoupUtil;
import com.ciaosir.client.utils.NumberUtil;

/**
 * @author navins
 * @date: Jan 21, 2014 3:01:12 PM
 */
public class WirelessAction {

    public final static Logger log = LoggerFactory.getLogger(WirelessAction.class);

    public static String genWirelessDesc(String desc) {
        if (StringUtils.isEmpty(desc)) {
            return StringUtils.EMPTY;
        }

        Pattern pattern = Pattern.compile("src=('|\")?[\\w\\.\\-/:?!_&%=;,]+('|\")?");
        Matcher matcher = pattern.matcher(desc);

        HashSet<String> itemLinkSet = new HashSet<String>();
        while (matcher.find()) {
            String link = matcher.group();
            if (StringUtils.isBlank(link)) {
                continue;
            }

            link = link.replaceFirst("src=('|\")?", StringUtils.EMPTY);
            if (link.endsWith("\"") || link.endsWith("'")) {
                link = link.substring(0, link.length() - 1);
            }

            itemLinkSet.add(link);
        }

        for (String link : itemLinkSet) {
            String newLink = WirelessAction.genWirelessPicUrl(link);
            log.info(link + " ---> " + newLink);
            desc = desc.replace(link, newLink);
        }

        return desc;
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

    public static String genWirelessPicUrl(String link) {
//        log.warn("[from]" + link);
        if (StringUtils.isEmpty(link) || !link.contains("taobaocdn.com")) {
            return link;
        }

        Pattern pattern = Pattern.compile("_[0-9]+x[0-9]+.jpg");
        Matcher matcher = pattern.matcher(link);
        String ext = StringUtils.EMPTY;
        while (matcher.find()) {
            ext = matcher.group();
        }
        if (StringUtils.isEmpty(ext)) {
            return link + "_600x600.jpg";
        }
        if (!link.endsWith(ext)) {
            return link + "_600x600.jpg";
        }

        String width = ext.substring(1, ext.indexOf('x'));
        if (!StringUtils.isEmpty(width)) {
            Integer widthVal = Integer.valueOf(width);
            if (widthVal == null) {
                return link;
            } else if (widthVal < 400) {
                return null;
            } else if (widthVal > 600) {
                return link.replaceAll(ext, "_600x600.jpg");
            }
        }

        return link;
    }

    private static int tryGetWidth(Element elem) {
        int noWith = 99999;
        try {
            String widthAttr = elem.attr("width");
            if (StringUtils.isEmpty(widthAttr)) {
                String style = elem.attr("style").toLowerCase();
                if (StringUtils.isEmpty(style)) {
                    return noWith;
                }
                log.info("[styles:]" + style);
                String[] splits = style.split(";");
                for (String string : splits) {
                    if (string.trim().startsWith("width")) {
                        widthAttr = string.replaceAll("width", "").replaceAll(":", "").replaceAll(" ", "");
                    }
                }
            }

            String trimmed = widthAttr.trim().replaceAll("px", "").trim();
            if (widthAttr.endsWith("%")) {
                double percent = NumberUtil.parserDouble(trimmed.replaceAll("%", ""), 100d);
                int width = (int) (800 * percent / 100d);
                return width;
            } else {
                int pointIndex = trimmed.indexOf('.');
                if (pointIndex >= 0) {
                    trimmed = trimmed.substring(0, pointIndex);
                }
                int width = NumberUtil.parserInt(trimmed, noWith);
                return width;
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return noWith;
        }
    }

    public static void main(String[] args) {
        List<String> imgs = getLargeImgUrls("<img src='http://img02.taobaocdn.com/bao/uploaded/i2/16382033409524251/T1gI8wXpBfXXXXXXXX_!!0-item_pic.jpg' style='width: 180.0px;height: 180.0px;' />");
        System.out.println(imgs);
    }

    public static List<String> getLargeImgUrls(String desc) {
        List<String> links = new ArrayList<String>();
        Document doc = JsoupUtil.parseJsoupDocument(desc);
        Elements elements = doc.getElementsByTag("img");

        for (Element elem : elements) {
            int width = tryGetWidth(elem);
            log.info("elem:" + elem + " fetch html width:" + width);

            if (width < 480) {
                log.warn("small width:" + elem);
                continue;
            }

            Element parent = elem.parent();
            width = tryGetWidth(parent);
            if (width < WirelessItemWorker.MIN_WIDTH) {
                log.warn("small parent width:" + parent);
                continue;
            }
            String link = elem.attr("src");
            if (StringUtils.isEmpty(link)) {
                continue;
            }
            if (link.contains("tongji") || link.contains("linezing") || link.contains("51la")
                    || link.contains("51.la") || link.contains("http://img.taobao.com/Juhuasuan_startFlag.gif")) {
                continue;
            }
            if (link.contains("_.webp")) {
                link.replace("_.webp", StringUtils.EMPTY);
            }

            links.add(link);
        }

        return links;
    }

    public static List<String> oldGetImgUrls(String desc) {
        if (StringUtils.isEmpty(desc)) {
            return null;
        }

        Pattern pattern = Pattern.compile("src=('|\")?[\\w\\.\\-/:?!_&%=;,]+('|\")?");
        Matcher matcher = pattern.matcher(desc);
        List<String> imgUrlSet = new ArrayList<String>();

        while (matcher.find()) {
            String link = matcher.group();
            if (StringUtils.isBlank(link)) {
                continue;
            }

            link = link.replaceFirst("src=('|\")?", StringUtils.EMPTY);
            if (link.endsWith("\"") || link.endsWith("'")) {
                link = link.substring(0, link.length() - 1);
            }

            log.info("[link :]" + link);
            /**
             * http://img02.taobaocdn.com/imgextra/i2/784007510/T2WiWwXKFXXXXXXXXX-784007510.jpg_.webp
             */
            if (link.contains("_.webp")) {
                link.replace("_.webp", StringUtils.EMPTY);
            }
            if (!imgUrlSet.contains(link)) {
                imgUrlSet.add(link);
            }
        }

        return imgUrlSet;
    }

    public static String downloadAndZipItem(Long userId, String title, String desc) {
        String dirPath = "/" + userId + "/" + title + "/";
        File dir = new File(Play.tmpDir, dirPath);
        if (!dir.exists() && !dir.mkdirs()) {
            return null;
        }

        List<String> imgUrls = WirelessAction.getLargeImgUrls(desc);
        if (CommonUtils.isEmpty(imgUrls)) {
            return null;
        }
        String itemPath = dir.getAbsolutePath();
        File mediaDir = new File(dir.getAbsolutePath(), "media");
        if (!mediaDir.exists() && !mediaDir.mkdir()) {
            return null;
        }
        String mediaPath = mediaDir.getAbsolutePath();
        List<String> imgNames = new ArrayList<String>();
        for (String picUrl : imgUrls) {
            String fileName = downloadPicToLocal(mediaPath, picUrl);
            imgNames.add(fileName);
        }

        writeXmlFile(itemPath, imgNames);

        String zipPath = Play.tmpDir.getAbsolutePath() + "/" + userId + "/" + title + ".zip";
        new ZipUtils(zipPath).compress(itemPath);
        return zipPath;
    }

    /**
     * 从远程图片地址下载图片到本地 tmp目录下
     * 
     * 

     * @param picUrl
     * @return picPath 下载到本地的文件名
     */
    public static String downloadPicToLocal(String dirPath, String picUrl) {
        String fileName = fetchFileName(picUrl);
        if (StringUtils.isEmpty(fileName)) {
            return StringUtils.EMPTY;
        }
        try {
            picUrl = genWirelessPicUrl(picUrl);
            URL url = new URL(picUrl);
            InputStream fStream = url.openConnection().getInputStream();

            int b = 0;
            File file = new File(dirPath, fileName);

            FileOutputStream fos = new FileOutputStream(file);
            while ((b = fStream.read()) != -1) {
                fos.write(b);
            }
            fStream.close();
            fos.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return fileName;
    }

    public static void writeXmlFile(String dirPath, List<String> imgNames) {
        File file = new File(dirPath, "mobile.xml");
        if (CommonUtils.isEmpty(imgNames)) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<wapDesc>");
        for (String name : imgNames) {
            sb.append("<img>media\\" + name + "</img>");
        }
        sb.append("</wapDesc>");

        try {
            FileUtils.write(file, sb.toString());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

//    public static String translateWirelessDesc(User user, Long numIid, String title, String desc, Map<Long, Long> numIidToPicCatId ) {
    public static String translateWirelessDesc(User user, String title, String desc) {
        String text = desc.replace("<br>", "\n").replace("</p>", "\n").replace("</table>", "\n").replace("&nbsp;", " ")
                .replaceAll("<[a-zA-Z0-9#/'\":;=&_%!宋体雅黑\\s\\-\\.\\?]*.>", "");
        StringBuilder sb = new StringBuilder();

        sb.append("<wapDesc><shortDesc>");
        sb.append(StringEscapeUtils.escapeXml(title));
        sb.append("</shortDesc>");

        List<String> imgUrls = WirelessAction.getLargeImgUrls(desc);
        for (String string : imgUrls) {
            log.info("[pic url:]" + string);
            String picUrl = genWirelessPicUrl(string);
            if (StringUtils.isEmpty(picUrl)) {
                continue;
            }
//            WS.url("");
            if (picUrl.contains("_600x600.jpg")) {
                picUrl = picUrl.replaceAll("_600x600.jpg", "");
            }
            sb.append("<img>");
            sb.append(picUrl);
            sb.append("</img>");
        }

        sb.append("</wapDesc>");

        return sb.toString().replaceAll(",", StringUtils.EMPTY).replaceAll("\n", StringUtils.EMPTY);
    }

    public static String fetchTxtXmlFile(Long userId, String title, String desc) {

        String dirPath = "/" + userId + "/" + title + "/";
        File dir = new File(Play.tmpDir, dirPath);
        if (!dir.exists() && !dir.mkdirs()) {
            return null;
        }

        String text = desc.replace("<br>", "\n").replace("</p>", "\n").replace("</table>", "\n").replace("&nbsp;", " ")
                .replaceAll("<[a-zA-Z0-9#/'\":;=&_%!宋体雅黑\\s\\-\\.\\?]*.>", "");
        StringBuilder sb = new StringBuilder();
        sb.append("<wapDesc><txt>");
        sb.append(text);
        sb.append("</txt></wapDesc>");

        File file = new File(dir.getAbsoluteFile(), "mobile.xml");
        try {
            FileUtils.write(file, sb.toString());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        String zipPath = Play.tmpDir.getAbsolutePath() + "/" + userId + "/" + title + ".zip";
        new ZipUtils(zipPath).compress(dir.getAbsolutePath());
        return zipPath;
    }

    /**
     * 
    <div style='line-height: 0.0px;'>
        <img
            src='http://img02.taobaocdn.com/imgextra/i2/696944147/T2wIWRXqRaXXXXXXXX-696944147.jpg?q=START_MEI2ZHE_MJS_ID53455a608fe78a3eaeb159bd_START'>
        <div
            style='background: #ffffff; margin: 0.0px 1.0% 2.0px; padding: 5.0px 1.0%; border: 1.0px solid #cccccc; width: 96.0%; overflow: hidden; font-size: 12.0px;'>
            <a title='该内容由美折生成#MEIZHE_MJS_53455a608fe78a3eaeb159bd'
                target='_blank'><img
                style='width: auto; float: left; display: inline;'
                src='http://img03.taobaocdn.com/bao/uploaded/i3/T1kZWZXj4jXXbaMLs2_043749.jpg'>
            </a>
            <div style='padding-left: 80.0px;'>
                <p style='margin: 0.0px; padding: 0.0px; line-height: 20.0px;'>
                    <strong><span style='color: #ff5400;'>【全店活动】</span>满减促销</strong>&nbsp;&nbsp;&nbsp;<span
                        style='color: #999999;'>活动日期：<span>2014-04-09
                            22:34:00 -- 2014-05-29 22:34:00</span>
                    </span>
                </p>
                <div>
                    <p style='margin: 0.0px; padding: 0.0px; line-height: 23.0px;'>
                        单笔订单满<strong style='color: #ff5400; font-size: 15.0px;'>2</strong>&nbsp;<span>件</span>:<span>减<strong
                            style='color: #ff5400; font-size: 15.0px;'>10</strong>元</span>上不封顶
                    </p>
                </div>
            </div>
        </div>
        <img
            src='http://img02.taobaocdn.com/imgextra/i2/696944147/T2wIWRXqRaXXXXXXXX-696944147.jpg?q=END_MEI2ZHE_MJS_ID53455a608fe78a3eaeb159bd_END'>
    </div>
    <p>
        <img
            src='http://img01.taobaocdn.com/imgextra/i1/1865319893/T2Kc6pXutaXXXXXXXX_!!1865319893.jpg'><img
            src='http://img02.taobaocdn.com/imgextra/i2/1865319893/T29fnqXzXXXXXXXXXX_!!1865319893.jpg'><img
            src='http://img04.taobaocdn.com/imgextra/i4/1865319893/T2pIDqXzJXXXXXXXXX_!!1865319893.jpg'><img
            src='http://img02.taobaocdn.com/imgextra/i2/1865319893/T2kIrqXAVXXXXXXXXX_!!1865319893.jpg'><img
            src='http://img01.taobaocdn.com/imgextra/i1/1865319893/T2BYTpXu0aXXXXXXXX_!!1865319893.jpg'><img
            src='http://img03.taobaocdn.com/imgextra/i3/1865319893/T2gbbqXBVXXXXXXXXX_!!1865319893.jpg'><img
            src='http://img02.taobaocdn.com/imgextra/i2/1865319893/T2ByjqXw8XXXXXXXXX_!!1865319893.jpg'><img
            src='http://img02.taobaocdn.com/imgextra/i2/1865319893/T2zu6pXtXaXXXXXXXX_!!1865319893.jpg'>


     */

    /**
     * <wapDesc>
     * <shortDesc>
     * 预售费雪正品婴儿玩具多功能健身架 踢踏钢琴爬行垫 游戏毯W2621 FISHER-PRICE/费雪 W2621 军绿色 500元以上 毛绒 无 3周岁以下
     * </shortDesc>
     * <img>http://img03.taobaocdn.com/imgextra/i3/1865319893/T2mWe4XidcXXXXXXXX_!!1865319893.jpg</img>
     * <img>http://img02.taobaocdn.com/imgextra/i2/1865319893/T2pW1VXmdbXXXXXXXX_!!1865319893.jpg</img>
     * <img>http://img04.taobaocdn.com/imgextra/i4/1865319893/T28yNjXnxOXXXXXXXX_!!1865319893.jpg</img>
     * <img>http://img04.taobaocdn.com/imgextra/i4/1865319893/T2UtaAXJpaXXXXXXXX_!!1865319893.jpg</img>
     * <img>http://img02.taobaocdn.com/imgextra/i2/1865319893/T2.09XXBRaXXXXXXXX_!!1865319893.jpg</img>
     * <img>http://img01.taobaocdn.com/imgextra/i1/1865319893/T2b2eSXAdaXXXXXXXX_!!1865319893.jpg</img>
     * <img>http://img02.taobaocdn.com/imgextra/i2/1865319893/T243uzXO4aXXXXXXXX_!!1865319893.jpg</img>
     * <img>http://img01.taobaocdn.com/imgextra/i1/1865319893/T2KO9GXEXaXXXXXXXX_!!1865319893.jpg</img>
     * </wapDesc>
     *
     * <wapDesc>
     * <shortDesc>元旦新娘饰品女水晶四叶草水钻项链女式首饰结婚珍珠配饰特价包邮合金镀银镀金水波链水钻植物花卉欧美现货全新网聚特色t5</shortDesc>
     * <img>http://img01.taobaocdn.com/imgextra/i1/694293805/T2LKn5XGxaXXXXXXXX_!!694293805.jpg</img>
     * <img>http://img04.taobaocdn.com/imgextra/i4/T165XpFCNXXXXXXXXX_!!0-item_pic.jpg</img>
     * <img>http://img04.taobaocdn.com/imgextra/i4/694293805/T2SSr8XTlXXXXXXXXX_!!694293805.jpg</img>
     * <img>http://img04.taobaocdn.com/imgextra/i4/694293805/T2hYj8XG0aXXXXXXXX_!!694293805.jpg</img>
     * <img>http://img03.taobaocdn.com/imgextra/i3/694293805/T2evP_XJlXXXXXXXXX_!!694293805.jpg</img>
     * <img>http://img03.taobaocdn.com/imgextra/i3/694293805/T2Yff_XHpXXXXXXXXX_!!694293805.jpg</img>
     * <img>http://img02.taobaocdn.com/imgextra/i2/694293805/T2zXr.XFNXXXXXXXXX_!!694293805.jpg</img>
     * <img>http://img04.taobaocdn.com/imgextra/i4/694293805/T24Lz9XOlXXXXXXXXX_!!694293805.jpg</img>
     * </wapDesc>
     */

}
