
package actions.wireless;

import static java.lang.String.format;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.imageio.ImageIO;

import jdp.ApiJdpAdapter;
import job.ApplicationStopJob;
import job.writter.TitleOptimisedWritter;
import models.task.AutoTitleTask;
import models.task.AutoTitleTask.WireLessDetailConfig;
import models.user.User;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.tools.ant.filters.StringInputStream;
import org.jboss.netty.util.internal.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import pojo.webpage.top.ItemStatusCount;
import result.TMResult;
import utils.PlayUtil;
import actions.WaterMarkerAction.MarkImageUtil;
import actions.task.TaskProgressAction.AutoTitleProgressAction;
import actions.wireless.GraphicMagicBuilder.WidthXHeight;
import actions.wireless.WirelessItemAssistant.TBAssitantVer;
import au.com.bytecode.opencsv.CSVReader;
import autotitle.ItemPropAction;
import bustbapi.BusAPI;
import bustbapi.ItemApi;
import bustbapi.OperateItemApi.WirelessItemStatus;
import bustbapi.PicApi;
import carrier.WirelessAction;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.PYFutureTaskPool;
import com.ciaosir.client.api.ItemSaleAPIs;
import com.ciaosir.client.api.SearchAPIs.ReocommendWordsSearch;
import com.ciaosir.client.utils.MapIterator;
import com.ciaosir.client.utils.NumberUtil;
import com.google.gson.JsonParser;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.Picture;

public class WirelessItemWorker implements Callable<TMResult<List<String[]>>> {

    private static final Logger log = LoggerFactory.getLogger(WirelessItemWorker.class);

    public static final String TAG = "ItemAssitentWorker";

    static WirelessItemWorker _instantce = new WirelessItemWorker();

    User user;

    File srcFile;

    AutoTitleTask task;

    WireLessDetailConfig config;

    public static String defaultWireDesc = "<wapDesc><shortDesc></shortDesc></wapDesc>";

    public WirelessItemWorker() {
        super();
    }

    public WirelessItemWorker(User user, File srcFile, AutoTitleTask task, WireLessDetailConfig config) {
        super();
        this.user = user;
        this.srcFile = srcFile;
        this.task = task;
        this.config = config;
    }

    protected List<WirelessItemAssistant> items = new ArrayList<WirelessItemAssistant>();

    private WirelessItemField.DescriptionField descField = new WirelessItemField.DescriptionField();

    private WirelessItemField.NumIidField numIidField = new WirelessItemField.NumIidField();

    private WirelessItemField.WirelessDescField wirelessField = new WirelessItemField.WirelessDescField();

    private WirelessItemField.TitleField titleField = new WirelessItemField.TitleField();

    private WirelessItemField.UserNameField nameField = new WirelessItemField.UserNameField();

    private WirelessItemField.SubTitleField subTitlle = new WirelessItemField.SubTitleField();

    WirelessItemField[] fields = new WirelessItemField[] {
            numIidField, descField, wirelessField, new WirelessItemField.CidItemField(), titleField, nameField,
            subTitlle
    };

    private void parseEngHeader(String[] args) {
        int length = args.length;
        for (int i = 0; i < length; i++) {
            String str = args[i];
            for (WirelessItemField field : fields) {
                if (field.getFieldName().endsWith(str)) {
                    field.setIndex(i);
                }
            }
        }
    }

    private void parseData(String[] args) {
        WirelessItemAssistant item = new WirelessItemAssistant();
        JsonParser parser = new JsonParser();
        for (WirelessItemField field : fields) {
            field.fillField(item, args[field.getIndex()], parser);
        }
        this.items.add(item);
    }

    private void parseChsHeader(String[] args) {
        int length = args.length;
        for (int i = 0; i < length; i++) {
            String str = args[i];
            for (WirelessItemField field : fields) {
                if (field.getFieldChnName().endsWith(str)) {
                    field.setIndex(i);
                }
            }
        }

    }

    private boolean isVersionHeader(String[] args) {
        if (args == null) {
            return false;
        }
        return StringUtils.join(args, StringUtils.EMPTY).indexOf("version") >= 0;
    }

    private boolean isEngHeader(String[] args) {
        if (args == null || args.length < 1) {
            return false;
        }
        for (WirelessItemField field : fields) {
            for (String string : args) {
                //                    log.info("[filed name :]" + field.getFieldName() + " with string: " + PlayUtil
                //                            .trimToShow(string));
                if (field.getFieldName().equals(string)) {
                    return true;
                }
            }
            return false;
        }

        return true;
    }

    private boolean isChsHeader(String[] args) {
        if (args == null || args.length < 1) {
            return false;
        }
        for (WirelessItemField field : fields) {
            for (String string : args) {
                if (field.getFieldChnName().equals(string)) {
                    break;
                }
            }
            return false;
        }

        return true;
    }

    private boolean isDataRow(String[] args) {
        if (args == null || args.length < fields.length) {
            return false;
        }
        String text = StringUtils.join(args, "").trim();
        if (text.length() <= "version 1.00".length()) {
            return false;
        }
        return true;
    }

    boolean isEngHeaderRead = false;

    boolean isChsHeaderRead = false;

    public List<WirelessItemAssistant> parseFile(File file) {
        try {
            String src = new String(FileUtils.readFileToByteArray(file), "unicode");
            log.error("[!!!!!!!1src :]" + src + " for file :" + file);
            CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(file), "unicode"), '\t');
            List<String[]> list = reader.readAll();

            int length = list.size();
            for (int i = 0; i < length; i++) {
                log.info("[line :]" + StringUtils.join(list.get(i), "").trim());
                doForArgs(list.get(i));
            }

            return items;

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }

        return null;
    }

    public static void main(String[] args) {
        String originPath = "http://img02.taobaocdn.com/imgextra/i2/54695045/T2TA8dXBhaXXXXXXXX_!!54695045.gif";
        System.out.println(originPath.endsWith("jpg") && !originPath.endsWith("jpeg") && originPath.endsWith("png"));

//        CSVReader reader = null;
//        try {
//            String src = new String(FileUtils.readFileToByteArray(new File("/home/zrb/code/tm/11.csv")), "unicode");
//            reader = new CSVReader(new InputStreamReader(new StringInputStream(src)), '\t');
//            List<String[]> list = reader.readAll();
//            TBAssitantVer ver = WirelessItemAssistant.getVersion(list);
//            System.out.println(ver);
//        } catch (Exception e) {
//            log.warn(e.getMessage(), e);
//
//        }
    }

    public TMResult<List<String[]>> call() {
        CSVReader reader = null;
        try {
            String src = new String(FileUtils.readFileToByteArray(srcFile), "unicode");
            reader = new CSVReader(new InputStreamReader(new StringInputStream(src)), '\t');
            List<String[]> list = reader.readAll();
            if (list.size() < 2) {
                log.error("上传的不是CSV文件吧");
                return new TMResult<List<String[]>>("NotCSV", "上传的不是CSV文件吧");
            }
            if (list.size() <= 3) {
                log.error("上传的CSV文件里面木有宝贝");
                return new TMResult<List<String[]>>("NotCSV", "上传的CSV文件里面木有宝贝");
            }
            TBAssitantVer ver = WirelessItemAssistant.getVersion(list);
            if (ver == null) {
                log.error("没有对应版本");
                return new TMResult<List<String[]>>("NotCSV", "csv文件格式有误");
            }
            if (ver == TBAssitantVer.VER550) {
                return new TMResult<List<String[]>>("NotCSV", "您当前淘宝助理版本为5.5,请至http://zhuli.taobao.com下载最新版淘宝助理");
            }

            AutoTitleProgressAction.createTaskProgress(task.getTaskId(), list.size());
            this.totalNum = list.size();

            for (int i = 0; i < list.size(); i++) {
                String[] args = list.get(i);
                if (isVersionHeader(args)) {
                    continue;
                }

                log.error(" args num:" + args.length);
                if (!isEngHeaderRead && isEngHeader(args)) {
                    parseEngHeader(args);
                    isEngHeaderRead = true;
                    continue;
                }
                if (isDataRow(args)) {
                    Long numIid = parseNumIid(args);
                    if (numIid > 0L) {
                        targetNumIids.add(numIid);
                    }
                    continue;
                }
            }

            log.info("[target numiids :]" + targetNumIids);
            ensureNumIidPicDir(targetNumIids);

            List<FutureTask<TMResult>> tasks = new ArrayList<FutureTask<TMResult>>();

            for (int i = 0; i < list.size(); i++) {
                log.info("[do for index]" + i);
                final String[] args = list.get(i);
                final int index = i;
                boolean isDataRow = isDataRow(args);
                if (isDataRow) {
                    tasks.add(getGraphicPool().submit(new Callable<TMResult>() {
                        @Override
                        public TMResult call() throws Exception {
                            return doForSingleItem(index, args);
                        }
                    }));
//                    doForSingleItem(index, args);
                }
            }
            for (FutureTask<TMResult> futureTask : tasks) {
                try {
                    futureTask.get();
                } catch (ExecutionException e) {
                    log.warn(e.getMessage(), e);
                } catch (InterruptedException e) {
                    log.warn(e.getMessage(), e);
                }
            }

            log.error("finish for : " + srcFile.getAbsolutePath());

            return new TMResult<List<String[]>>(list);
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.warn(e.getMessage(), e);

                }
            }
        }

        return new TMResult<List<String[]>>("inner", "服务器出了点小问题,可以联系客服哟");
    }

    static PYFutureTaskPool<TMResult> graphicPool;

    public synchronized PYFutureTaskPool<TMResult> getGraphicPool() {
        if (graphicPool == null) {
            int coreNum = NumberUtil.parserInt(Play.configuration.getProperty("graphic.core", "0"), 0);
            if (Play.mode.isDev()) {
                coreNum = 2;
            }
            graphicPool = new PYFutureTaskPool<TMResult>(coreNum);
            ApplicationStopJob.addShutdownPool(graphicPool);
        }
        return graphicPool;
    }

    Set<Long> targetNumIids = new HashSet<Long>();

    Map<Long, Long> numIidToPicCategory = new ConcurrentHashMap<Long, Long>();

    int totalNum;

    private Long parseNumIid(String[] args) {
        Long numIid = NumberUtil.parserLong(args[numIidField.getIndex()], -1L);
        return numIid;
    }

    private TMResult<String> doForSingleItem(int index, String[] args) {
        log.warn(" start to do index :" + index + " with total[" + totalNum + "] for user:" + user.toIdNick());

        Long numIid = NumberUtil.parserLong(args[numIidField.getIndex()], -1L);
        if (numIid <= 0L) {
            String msg = StringUtils.join(args, ' ');
            log.error("parse error for : args:" + PlayUtil.trimToShow(msg));
            return TMResult.failMsg("No NumIid");
        }

        String rawDesc = args[descField.getIndex()];
        String originWirelessField = args[wirelessField.getIndex()];

        if (config.isSkipExist()
                && StringUtils.length(originWirelessField) > defaultWireDesc.length()) {
            return TMResult.failMsg("skip exist...");
        }
        String shortText = args[titleField.getIndex()];
//        if (config.isAutoProp() && Play.mode.isProd()) {
        if (config.isAutoProp()) {
            log.info("[do fetch  base prop ]" + numIid);

            try {
                Item item = null;
                if (numIid == 0 || numIid <= 0) {
                    // Do nothing...
                } else if (Play.mode.isProd()) {
                    item = ApiJdpAdapter.singleItem(user, numIid);
                } else {
                    item = new BusAPI.SingleItemApi(numIid).execute();
                }

                shortText = trimShortText(shortText, item);

            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        } else {
            shortText += StringUtils.EMPTY;
        }

//        trimShortText(shortText);

        String wirelessDesc = buildWirelessPics(numIid, shortText, rawDesc);

        args[wirelessField.getIndex()] = wirelessDesc;
        AutoTitleProgressAction.stepOneTaskProgress(task.getId());

        /**
         * Record for the optimzed record....
         */
        TitleOptimisedWritter.addMsg(user.getId(), numIid, true);

        return TMResult.OK;
    }

    private String trimShortText(String title, Item item) {
        if (item == null) {
            return title;
        }

        title += StringUtils.join(ItemPropAction.splitPropList(item.getPropsName(), item.getPropertyAlias()), "");
        title = title.replaceAll("  ", " ");
        title = title.replaceAll(",", StringUtils.EMPTY);
        title = title.replaceAll("\n", StringUtils.EMPTY);

        if (title.length() > 75) {
            title = title.substring(0, 75);
        }

        return title;
    }

    /*
     * ------------------------------
     *
     * 沒有淘寶助理我們一樣得幹
     *
     * ------------------------------
     */
    static int batNumIidSize = 5;

    private void ensureNumIidPicDir(Collection<Long> ids) {

        Set<Long> targetids = new HashSet<Long>(ids);
        targetids.removeAll(numIidToPicCategory.keySet());
        log.info("[numiid to pic category:]" + numIidToPicCategory + " target ids:" + targetids);
        if (CommonUtils.isEmpty(targetids)) {
            return;
        }
        Map<Long, Long> map = PicApi.get().ensureNumIidsToDirMap(user, targetids);
        numIidToPicCategory.putAll(map);
    }

    public OneKeyHelper genOne() {
        OneKeyHelper config = new OneKeyHelper();
        return config;
    }

    public class OneKeyHelper {

        static final int maxCount = 40;

        int count = 0;

        public boolean addCount() {
            count++;
            return count > maxCount;
        }

        public Map<WirelessFieldLoader, StringBuffer> getLoadWriteBuffer() {
            return loadWriteBuffer;
        }

        public void setLoadWriteBuffer(Map<WirelessFieldLoader, StringBuffer> loadWriteBuffer) {
            this.loadWriteBuffer = loadWriteBuffer;
        }

        Map<WirelessFieldLoader, StringBuffer> loadWriteBuffer = new ConcurrentHashMap<WirelessFieldLoader, StringBuffer>();

        private OneKeyHelper() {
            super();
//            this.loadWriteBuffer = new HashMap<WirelessFieldLoader, StringBuffer>();
            try {
                log.info("[load buffer size:]" + loadWriteBuffer.size());
//                loadWriteBuffer.put(WirelessFieldLoader.loader50, new StringBuffer());
//                loadWriteBuffer.put(WirelessFieldLoader.loader60, new StringBuffer());
                loadWriteBuffer.put(WirelessFieldLoader.loader61, new StringBuffer());
                log.info("[load buffer size:]" + loadWriteBuffer.size());
            } catch (Throwable t) {
                log.warn(t.getMessage());

            }
        }

        public void writeItemBuffer(final Item item) {
            new MapIterator<WirelessFieldLoader, StringBuffer>(loadWriteBuffer) {
                @Override
                public void execute(Entry<WirelessFieldLoader, StringBuffer> entry) {
                    WirelessFieldLoader loader = entry.getKey();
                    StringBuffer buffer = entry.getValue();
                    String line = loader.buildItemLine(item);
                    buffer.append(line);
                    buffer.append('\n');
                }
            }.call();
        }

        public Map<String, String> writeAllFile() {
            final Map<String, String> verToPath = new HashMap<String, String>();
            new MapIterator<WirelessFieldLoader, StringBuffer>(loadWriteBuffer) {
                @Override
                public void execute(Entry<WirelessFieldLoader, StringBuffer> entry) {
                    WirelessFieldLoader loader = entry.getKey();
                    StringBuffer buffer = entry.getValue();
                    loader.getVer();
                    String name = user.getUserNick() + new SimpleDateFormat("MMdd_HH点mm分ss秒").format(new Date()) + "."
                            + loader.getVer() + ".csv";
                    File file = new File(WireLessUtil.ensureUserOutputDir(user.getId()), name);
                    buffer.insert(0, loader.getHeader());
                    try {
                        FileUtils.writeStringToFile(file, buffer.toString(), "gbk");
                        buffer.delete(0, buffer.length());
                        log.info("[putver:  ]" + loader.getVer() + ":" + file.getAbsolutePath());
                        verToPath.put(loader.getVer(), file.getAbsolutePath());

                    } catch (IOException e) {
                        log.warn(e.getMessage(), e);
                    }
                }
            }.call();
            return verToPath;
        }

    }

    /**
     * 这是一键生成的最关键也是唯一的流程
     * @param ids
     * @return 
     */
    public TMResult<Map<String, String>> doForAllNumIids(List<Long> ids) {
        try {

            Thread.currentThread().setName(TAG);
            log.info(format("doForAllNumIids:ids".replaceAll(", ", "=%s, ") + "=%s", ids));

            if (CommonUtils.isEmpty(ids)) {
                log.info("[empty ids:]" + ids);
                return new TMResult<Map<String, String>>("没有找到需要生成详情页的宝贝");
            }

            log.info("[new:config]");
            final OneKeyHelper config = new OneKeyHelper();
            log.info("[end:config]");
            int size = ids.size();
            List<FutureTask<TMResult>> tasks = new ArrayList<FutureTask<TMResult>>(size);

            for (int start = 0; start < size; start += batNumIidSize) {
                int end = start + batNumIidSize;
                if (end > size) {
                    end = size;
                }

                log.info("[start ]" + start + "-end :" + end + " for task ids:" + task.getId());
                final List<Long> batchList = ids.subList(start, end);
                List<FutureTask<TMResult>> list = doForNumIidUnit(batchList, config);
                tasks.addAll(list);
            }

            for (FutureTask<TMResult> futureTask : tasks) {

                try {
                    futureTask.get();
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                }
            }

            Map<String, String> map = config.writeAllFile();
            return new TMResult<Map<String, String>>(map);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        return new TMResult<Map<String, String>>("系统异常，您可以联系客服解决");
    }

    /**
     * This is the smallest unit....
     * @param ids
     * @param helper 
     * @return 
     * @return 
     */
    private List<FutureTask<TMResult>> doForNumIidUnit(Collection<Long> ids, final OneKeyHelper helper) {

        log.info(format("doForNumIidUnit:ids, config".replaceAll(", ", "=%s, ") + "=%s", ids, helper));

        ensureNumIidPicDir(ids);
        List<FutureTask<TMResult>> list = new ArrayList<FutureTask<TMResult>>();

//        List<Item> items = new MultiItemsListGet(user, ids, field, batNumIidSize).call();
        for (final Long numIid : ids) {
            log.info("[do for numiid :]" + numIid);
//            AutoTitleProgressAction.stepOneTaskProgress(task.getId());
            final Item item = new ItemApi.ItemFullGet(user, numIid, ItemApi.WIRELESS_FIELDS).call();
            if (!CommonUtils.isEmpty(item.getVideos())) {
                log.warn("no workd for the has video mode");
                continue;
            }

            if (WirelessItemWorker.this.config.isSkipExist()
                    && StringUtils.length(item.getWirelessDesc()) > defaultWireDesc.length()) {
                log.warn("skip exist....:" + numIid + " for user;" + user.toIdNick());
                continue;
            }
            try {
                /*
                 * 对于有视频的宝贝,不能直接通过服务器导出
                 */
                list.add(getGraphicPool().submit(new Callable<TMResult>() {
                    @Override
                    public TMResult call() throws Exception {
                        doForItem(helper, item);
                        TitleOptimisedWritter.addMsg(user.getId(), item.getNumIid(), true);
                        AutoTitleProgressAction.stepOneTaskProgress(task.getId());
                        return null;
                    }
                }));

            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }

        return list;
//        Map<String, String> verToFile = config.writeAllFile();
//        return verToFile;
    }

    private void doForItem(OneKeyHelper helper, Item item) {
        String desc = item.getDesc();
        String shortText = item.getTitle();
//        List<String> raw = ItemPropAction.splitPropList(item.getPropsName(), item.getPropertyAlias());
//        String propName = StringUtils.join(raw, "");
//        shortText += propName;

        String wirelessDesc = buildWirelessPics(item.getNumIid(), shortText, desc);
        item.setWirelessDesc(wirelessDesc);
        helper.writeItemBuffer(item);

    }

    private String genUrlMappedKey(String url) {
        int index = -1;
        index = url.lastIndexOf("/");
        if (index < 0) {
            log.warn("invalid url:" + url);
            return null;
        }
        String key = url.substring(index);
        return key;
    }

    static class WirelessItemStatus {
        int sizeCount = 0;

        int addNum = 0;

        private WirelessItemStatus(int sizeCount, int addNum) {
            super();
            this.sizeCount = sizeCount;
            this.addNum = addNum;
        }

        private WirelessItemStatus() {
            super();
        }

        @Override
        public String toString() {
            return "WirelessItemStatus [sizeCount=" + sizeCount + ", addNum=" + addNum + ", isMaxReached="
                    + isMaxReached + "]";
        }

        public int getSizeCount() {
            return sizeCount;
        }

        public void setSizeCount(int sizeCount) {
            this.sizeCount = sizeCount;
        }

        public int getAddNum() {
            return addNum;
        }

        public void setAddNum(int addNum) {
            this.addNum = addNum;
        }

        public boolean maxReached() {
            return false;
        }

        boolean isMaxReached = false;

        public boolean tryAddNewFileSize(int size) {
            log.info("[try add size]" + size + " for current :" + this);
            int newSize = this.sizeCount + size;
            if (newSize > 1536 * 1000) {
                isMaxReached = true;
                return false;
            } else {
                this.sizeCount = newSize;
                this.addNum++;
                return true;
            }
        }
    }

    private String buildWirelessPics(final Long numIid, String shortText, String rawDesc) {

        log.info(format("buildWirelessPics:numIid, shortText, rawDesc".replaceAll(", ", "=%s, ") + "=%s", numIid,
                shortText, rawDesc.length()));

        final StringBuffer sb = new StringBuffer();

        WirelessItemStatus itemStatus = new WirelessItemStatus();

        sb.append("<wapDesc><shortDesc>");
        sb.append(StringEscapeUtils.escapeXml(shortText));
        sb.append("</shortDesc>");

//        log.info("[bak raw imsg urlS:]" + imgUrls);
        Long picCatId = numIidToPicCategory.get(numIid);
        if (Play.mode.isProd() && picCatId != null && picCatId.longValue() != PicApi.ensureTMCat(user).longValue()) {
            PicApi.get().clearDir(user, picCatId);
        }

        if (picCatId == null) {
            picCatId = PicApi.ensureTMCat(user);
        }
        if (picCatId == null) {
            String content = sb.toString();
            log.info("[wireless desc:]" + content + " with current status \n" + itemStatus + " Fail for no cat id");
            content = content.replaceAll("\r\n", StringUtils.EMPTY).replaceAll("\n", StringUtils.EMPTY)
                    .replaceAll(",", StringUtils.EMPTY);
            return content;
        }

//      List<Picture> targetPictures = new ArrayList<Picture>();
        Map<Long, Picture> targetPictures = new HashMap<Long, Picture>();
        Set<String> noPictureIds = new HashSet<String>();
        /*
         * http://img03.taobaocdn.com/bao/uploaded/i3/16382026890065285/T1acCpFmBcXXXXXXXX_!!0-item_pic.jpg
         * http://img03.taobaocdn.com/imgextra/i3/1039626382/T1acCpFmBcXXXXXXXX_!!0-item_pic.jpg
         * 由于很多返回的链接并不一样，只能这样过滤一手了
         */
        Map<String, String> noExistMappedKeysFromUrl = new HashMap<String, String>();

        /**
         * Already trimmed...
         */
        List<String> imgUrls = WirelessAction.getLargeImgUrls(rawDesc);
        for (int i = imgUrls.size() - 1; i >= 0; i--) {
            String url = imgUrls.get(i);

            if (!url.contains("taobaocdn.com") || url.endsWith(".gif")) {
                /*
                 * 第三方图片空间
                 */
//                String newUrl = doForOtherImages(url, numIid, picCatId, targetPictures);
                String newUrl = doForOtherImagesWithGraphicMagic(url, numIid, picCatId, targetPictures);
                log.info("[url:]" + newUrl);
                if (newUrl == null) {
                    /*
                     * Not good for the new url..
                     */
                    imgUrls.remove(i);
                } else {
                    /*
                     * replace for the current other party page..
                     */
                    imgUrls.set(i, newUrl);
                    url = newUrl;
                }
            }
//
//            if (url.endsWith("gif")) {
//
//            }

            String key = genUrlMappedKey(url);
            if (key != null) {
                log.info("[put:]" + key + " : url ;" + url);
                noExistMappedKeysFromUrl.put(key, url);
            }
        }

        List<Picture> tarPicList = new ArrayList<Picture>();

        if (CommonUtils.isEmpty(imgUrls)) {
            sb.append("</wapDesc>");
            return sb.toString();
        }

        for (String string : imgUrls) {
            log.info("[add for url:]" + string);
            /**
             * gif会导致sun jdk的bug，过滤掉
             */

//            if (!string.endsWith("jpg") && !string.endsWith("png") && !string.endsWith("jpeg")) {
//                continue;
//            }

            if (!string.contains("taobaocdn.com")) {
                continue;
            }
            Picture picture = originPicpathCache.get(string);
            if (picture != null) {
                targetPictures.put(picture.getPictureId(), picture);
            } else {
                noPictureIds.add(string);
            }
        }
//        log.info("[target pictures:]"+noPictureIds);
//        log.info("[no picture id:]"+noPictureIds);

        List<Picture> list = PicApi.get().ensureOriginPicture(user, noPictureIds);
//        log.info("[back for no pictture:id]" + list);
        for (Picture picture : list) {
            originPicpathCache.put(picture.getPicturePath(), picture);
            targetPictures.put(picture.getPictureId(), picture);
        }

        /**
         * 从页面爬出来的url和服务器返回的url并不一样，所以只能通过 mappedKey作为中介进行匹配了
         */
        Map<String, Picture> originUrlToPicture = new HashMap<String, Picture>();
        for (Picture apiPicture : targetPictures.values()) {
            String mappedKey = genUrlMappedKey(apiPicture.getPicturePath());
            if (mappedKey == null) {
                continue;
            }
            String originUrl = noExistMappedKeysFromUrl.get(mappedKey);
            if (originUrl == null) {
                continue;
            }
            originUrlToPicture.put(originUrl, apiPicture);
        }

//        log.info("[target pictures:]" + targetPictures);
        /**
         * 为了保证顺序， 不得不这样干
         */
        for (String string : imgUrls) {
            Picture picture = originUrlToPicture.get(string);
            if (picture != null) {
                tarPicList.add(picture);
            }
        }

        int targetPicNum = tarPicList.size();
        List<FutureTask<TMResult>> task = new ArrayList<FutureTask<TMResult>>(targetPicNum);

        int count = 0;

        for (Picture picture : tarPicList) {
            try {
                log.info("append [" + (count) + "] in [" + targetPicNum + "]for numiid [" + numIid + "]picture :"
                        + picture);

//                boolean translateOk = translatePic(numIid, picture, sb);
                boolean translateOk = graphicTranslate(numIid, picture, sb, itemStatus);
                log.info("[current istem stauts :]" + itemStatus);
                if (itemStatus.maxReached()) {
                    break;
                }

//                if (translateOk) {
//                    count++;
//                    if (count > 30) {
//                        log.warn("too many pictures;" + count + " for numiid :" + numIid + " for user:"
//                                + user.toIdNick());
//                        break;
//                    }
//                }
//                final Picture tarPicture = picture;
//                task.add(getGraphicPool().submit(new Callable<TMResult>() {
//                    @Override
//                    public TMResult call() throws Exception {
//                        translatePic(numIid, tarPicture, sb);
//                        return null;
//                    }
//                }));
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }

        sb.append("</wapDesc>");

        String content = sb.toString();
        log.info("[wireless desc:]" + content + " with current status \n" + itemStatus);
        content = content.replaceAll("\r\n", StringUtils.EMPTY).replaceAll("\n", StringUtils.EMPTY)
                .replaceAll(",", StringUtils.EMPTY);
        return content;
    }

    private String doForOtherImagesWithGraphicMagic(String path, Long numIid, Long picCatId,
            Map<Long, Picture> targetPictures) {
//        tempOriginInput = turnGifToJpg(user, tempOriginInput);
        log.info("[read for new path:]" + path + "for user:" + user.toIdNick() + " with numiid :" + numIid);
        if (StringUtils.isBlank(path)) {
            return null;
        }
        boolean isGif = path.endsWith(".gif");
        GraphicMagicBuilder builder = new GraphicMagicBuilder();
        try {
            File readImgFile = readImgFile(new URL(path));
            if (isGif) {
//                File origin = readImgFile;
                readImgFile = turnGifToJpg(user, readImgFile);
//                FileUtils.deleteQuietly(origin);
            }
            WidthXHeight model = builder.getWidthAndHeight(readImgFile.getAbsolutePath());
            if (model == null) {
                log.error(" read error :" + path + " for numiid :" + numIid);
                return null;
            }
            if (model.getWidth() < MIN_WIDTH) {
                log.info("[to small other pic :]" + model);
                return null;
            }

            String title = new Random().nextInt(10000) + "_" + System.currentTimeMillis() + "_" + +numIid + ".jpg";
            byte[] b = FileUtils.readFileToByteArray(readImgFile);
            Picture uploaded = PicApi.get().uploadPcClientPic(user, title, b, picCatId);

            log.info("[upload for other site img]:" + uploaded);
            if (uploaded == null) {
                return null;
            }
            targetPictures.put(uploaded.getPictureId(), uploaded);

            FileUtils.deleteQuietly(readImgFile);
            return uploaded.getPicturePath();
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }

        return null;

    }

    @Deprecated
    private String doForOtherImages(String path, Long numIid, Long picCatId, Map<Long, Picture> targetPictures) {
        log.info("[read for new path:]" + path + "for user:" + user.toIdNick() + " with numiid :" + numIid);
        if (StringUtils.isBlank(path)) {
            return null;
        }
        if (!path.endsWith("png") && !path.endsWith("jpg") && !path.endsWith("jpeg")) {
//        if (!path.endsWith("png") && !path.endsWith("jpg") && !path.endsWith("jpeg") && !path.endsWith("gif")) {
            /**
             * 只看这三种格式
             */
            return null;
        }

//        BufferedImage srcImg = MarkImageUtil.readImage(path, true, false);
        BufferedImage srcImg;
        try {
            srcImg = MarkImageUtil.readImage(new URL(path), false);
        } catch (MalformedURLException e1) {
            log.warn(e1.getMessage(), e1);
            return null;
        }

        if (srcImg == null) {
            log.warn(" can't fetch url:" + path);
            return null;
        }
        if (srcImg.getWidth() < 480) {
            log.info("[to small other pic :]" + srcImg.getWidth() + "x" + srcImg.getHeight());
            return null;
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            ImageIO.write(srcImg, "jpg", output);
            byte[] b = output.toByteArray();
            String title = new Random().nextInt(10000) + "_" + System.currentTimeMillis() + "_" + +numIid + ".jpg";
            Picture uploaded = PicApi.get().uploadPcClientPic(user, title, b, picCatId);

            log.info("[upload for other site img]:" + uploaded);
            if (uploaded == null) {
                return null;
            }
            targetPictures.put(uploaded.getPictureId(), uploaded);
            return uploaded.getPicturePath();
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }
        return null;
    }

    private Map<String, Picture> originPicpathCache = new ConcurrentHashMap<String, Picture>();

    private Map<String, Picture> uploadPicpathCache = new ConcurrentHashMap<String, Picture>();

    public static int MIN_WIDTH = 480;

    public static int MAX_WIDTH = 600;

    static int MAX_HEIGHT = 920;

    private File genTempInputFile() throws IOException {
        File dir = new File(Play.tmpDir, "_tmp_input_" + (user.getId().intValue() % 1000));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = user.getId().toString() + new Random().nextInt(10000) + System.currentTimeMillis();
        File file = new File(dir, fileName);
        file.createNewFile();
        return file;
    }

    public static File genTempOutputFile(User user, String format) throws IOException {
        File dir = new File(Play.tmpDir, "_tmp_output_" + (user.getId().intValue() % 1000));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = user.getId().toString() + new Random().nextInt(10000) + System.currentTimeMillis();
        if (format != null) {
            fileName = fileName + "." + format;
        }
        File file = new File(dir, fileName);
        file.createNewFile();
        return file;
    }

    private File readImgFile(URL url) throws IOException {

        File file = genTempInputFile();

        int count = 3;
        while (count-- > 0) {
            try {
                if (!file.exists()) {
                    file.createNewFile();
                }
                URLConnection conn = url.openConnection();
                conn.setConnectTimeout(4000);
                conn.setReadTimeout(4000);
                InputStream inputStream = conn.getInputStream();
                FileUtils.copyInputStreamToFile(inputStream, file);
                IOUtils.closeQuietly(inputStream);

                return file;
            } catch (IOException e) {
                log.warn(e.getMessage(), e);
            }
        }

        return null;

    }

    private boolean graphicTranslate(Long numIid, Picture picture, StringBuffer sb, WirelessItemStatus itemStatus) {

        String originPath = picture.getPicturePath();
        if (!originPath.endsWith("jpg") && !originPath.endsWith("jpeg") && !originPath.endsWith("png")) {
            log.warn(" not valid format new path:" + originPath + " with numiid :" + numIid + "  pic:" + picture);
            return false;
        }

//        log.info(format("graphicTranslate:numIid, picture, sb".replaceAll(", ", "=%s, ") + "=%s", numIid, picture, sb));

        File tempOriginInput = null;
        File tempTBTranlated = null;
        File tempOutput = null;
        boolean success = false;
        boolean removeTempFiles = true;

        int srcWidth = picture.getWidth();
        if (srcWidth > 0 && srcWidth < MIN_WIDTH) {
            log.warn(" not valid format numiid :" + numIid + "  pic:" + picture);
            return false;
        }
        boolean isGif = picture.getPicturePath().endsWith("gif");

//        String postFix = "jpg";
//        if (picture.getPicturePath().endsWith("png")) {
//            postFix = "png";
//        } else if (picture.getPicturePath().endsWith("gif")) {
//            postFix = "gif";
//        }
        GraphicMagicBuilder builder = new GraphicMagicBuilder();
        try {
            tempOriginInput = readImgFile(new URL(picture.getPicturePath()));
            if (isGif) {
                tempOriginInput = turnGifToJpg(user, tempOriginInput);
            }
            WidthXHeight src = builder.getWidthAndHeight(tempOriginInput.getAbsolutePath());
            srcWidth = src.getWidth();
            int srcHeight = src.getHeight();
            if (src.getWidth() < MIN_WIDTH) {
                log.warn(" too small width:" + srcWidth + " for numiid :" + numIid + " with path:" + picture);
                return false;
            }
            boolean noNeedToZoom = (srcWidth >= MIN_WIDTH && srcWidth <= MAX_WIDTH) && (srcHeight <= MAX_HEIGHT);
            if (noNeedToZoom) {
                log.info("[no need zoom for srcwith and height:]" + srcWidth + "x" + srcHeight);
                writeFile(numIid, sb, tempOriginInput, itemStatus);
                return true;
            }

            int toWidth = MIN_WIDTH;
            int toHeight = srcHeight * toWidth / srcWidth;
            WidthXHeight target = new WidthXHeight(toWidth, toHeight);

            if (toHeight > MAX_HEIGHT) {
                log.info("[do for target :]" + toWidth + "x" + toHeight);
                if (!config.isAutoSplit()) {
                    log.warn("no auto split for long pic of user;" + user.toIdNick() + " with path :" + picture);
                    return false;
                }
                log.warn(" too long height" + toHeight + " for pic url:" + picture);
                int step = STEP_HEIGHT;
//                BufferedImage formedImg = MarkImageUtil.zoomImage(srcImg, toWidth, toHeight);
                tempTBTranlated = genTempOutputFile(user, null);
                boolean zoom = builder.zoom(tempOriginInput, tempTBTranlated, toWidth);
                log.warn("is zommed : " + zoom);
                if (!zoom) {
                    return false;
                }

                for (int startHeight = 0; startHeight < toHeight; startHeight += step) {
                    int endHeight = startHeight + step - 1;
                    if (endHeight > toHeight) {
                        endHeight = toHeight;
                    }

                    File dir = new File(Play.tmpDir, "pic_trans_" + (user.getId().intValue() % 1000));
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
//                    File output = new File(dir, System.currentTimeMillis() + endHeight + ".jpg");
                    File output = new File(dir, user.getId().toString() + System.currentTimeMillis() + "_" + endHeight);
                    output.createNewFile();
                    int offsetY = endHeight - startHeight;
                    success = builder.cutImage(tempTBTranlated, output, 0, startHeight, toWidth, offsetY);
                    if (success) {
                        writeFile(numIid, sb, output, itemStatus);
                        log.warn(" write sub for numiid :" + numIid + " picture: " + picture + " with :" + tempOutput);
                    } else {
                        log.warn(" fail for numiid :" + numIid + " picture: " + picture);
                    }

                    if (removeTempFiles) {
                        output.delete();
                    }

                }
            } else {
                // TODO use taobao zoom way...
                String taobaoUrl = picture.getPicturePath() + "_480x480.jpg";
                tempTBTranlated = readImgFile(new URL(taobaoUrl));
                WidthXHeight tbTranfered = builder.getWidthAndHeight(tempOriginInput.getAbsolutePath());
                if (tbTranfered.getWidth() == MIN_WIDTH) {
                    log.info(" good from taobao for numiid :" + numIid + " picture: " + picture);
                    writeFile(numIid, sb, tempTBTranlated, itemStatus);
                    return true;
                }

                tempOutput = genTempOutputFile(user, null);
                success = builder.zoom(tempOriginInput, tempOutput, toWidth);

                if (success) {
                    writeFile(numIid, sb, tempOutput, itemStatus);
                    log.warn("write success [" + user.toIdNick() + "].:  with pixel:" + target + " for picture;"
                            + picture + "");
                } else {
                    log.warn("simple zoom fails [" + user.toIdNick() + "].:  with pixel:" + target + " for picture;"
                            + picture + "");
                }

//                srcImg = MarkImageUtil.zoomImage(srcImg, toWidth, toHeight);
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        } finally {
            if (removeTempFiles) {
                FileUtils.deleteQuietly(tempOriginInput);
                FileUtils.deleteQuietly(tempTBTranlated);
                FileUtils.deleteQuietly(tempOutput);
            }
        }
        return true;
    }

    public static File turnGifToJpg(User user, File tempOriginInput) throws IOException {
        if (!tempOriginInput.getName().endsWith("gif")) {
            File target = new File(tempOriginInput.getAbsolutePath() + ".gif");
            target.createNewFile();
            tempOriginInput.renameTo(target);
            tempOriginInput = target;
        }

        File outputFile = genTempOutputFile(user, "jpg");
        GraphicMagicBuilder builder = new GraphicMagicBuilder();
        builder.convert(tempOriginInput.getAbsolutePath(), outputFile.getAbsolutePath());
        FileUtils.deleteQuietly(tempOriginInput);
        return outputFile;
    }

    private boolean isGoodSize(int srcWidth, int srcHeight) {
        return (srcWidth >= 480 && srcWidth <= 600) && srcHeight <= 920;
    }

    /**
     * zoom for all
     * @param numIid
     * @param picture
     * @param sb
     * @return 
     * @throws IOException
     */
    private boolean translatePic(Long numIid, Picture picture, StringBuffer sb, WirelessItemStatus iStatus)
            throws IOException {

        String originPath = picture.getPicturePath();
        if (!originPath.endsWith("jpg") && !originPath.endsWith("jpeg") && originPath.endsWith("png")) {
            log.warn(" not valid format new path:" + originPath + " with numiid :" + numIid + "  pic:" + picture);
            return false;
        }
//        BufferedImage zoomedImg = null;
//        String tbZoomPath = originPath + "_480x480.jpg";
//        zoomedImg = MarkImageUtil.readImage(new URL(tbZoomPath), false);
//        if (zoomedImg != null && isGoodSize(zoomedImg.getWidth(), zoomedImg.getWidth())) {
//            log.info("good for :" + tbZoomPath + "with width:[" + zoomedImg.getWidth() + "]-height:["
//                    + zoomedImg.getHeight() + "] for user:" + user.toIdNick() + " with numiid ;" + numIid);
//            writeByByte(numIid, sb, zoomedImg);
//            return true;
//        }

        long tStart = System.currentTimeMillis();
        String newPath = originPath;
        BufferedImage srcImg = MarkImageUtil.readImage(new URL(newPath), false);
        if (srcImg == null) {
            log.warn(" fail for new path:" + newPath + " with numiid :" + numIid + "  pic:" + picture);
            return false;
        }
        long tEnd = System.currentTimeMillis();

        int srcWidth = srcImg.getWidth();
        int srcHeight = srcImg.getHeight();

        log.info("took [" + (tEnd - tStart) + "]ms [width: ]" + srcWidth + " - height:" + srcHeight + " - path :"
                + newPath);

        if (srcWidth < 480) {
            // too small size...
            log.warn(" too small width:" + srcWidth);
            return false;
        }

//        boolean isZoomNeeded  =  srcWidth > 600 || srcHeight > 920;
        boolean noNeedToZoom = isGoodSize(srcWidth, srcHeight);
        if (noNeedToZoom) {
            log.info("[no need zoom for srcwith and height:]" + srcWidth + "x" + srcHeight);
            writeByByte(numIid, sb, srcImg, iStatus);
            return true;
        }

        int toWidth = MIN_WIDTH;
        int toHeight = srcHeight * toWidth / srcWidth;

        if (toHeight > MAX_HEIGHT) {
            log.info("[do for target :]" + toWidth + "x" + toHeight);
            if (!config.isAutoSplit()) {
                log.warn("no auto split for long pic of user;" + user.toIdNick() + " with path :" + picture);
                return false;
            }
            log.warn(" too long height" + toHeight + " for pic url:" + picture);
            int step = STEP_HEIGHT;
            BufferedImage formedImg = MarkImageUtil.zoomImage(srcImg, toWidth, toHeight);
            for (int startHeight = 0; startHeight < toHeight; startHeight += step) {
                int endHeight = startHeight + step - 1;
                if (endHeight > toHeight) {
                    endHeight = toHeight;
                }

                File dir = new File(Play.tmpDir, "pic_trans_" + (user.getId()));
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File tmpFile = new File(dir, System.currentTimeMillis() + endHeight + ".jpg");
                int targetHeight = endHeight - startHeight;
                BufferedImage subimage = formedImg.getSubimage(0, startHeight, formedImg.getWidth(), targetHeight);
                writeFile(numIid, sb, tmpFile, subimage, iStatus);
            }
        } else {
            log.warn("simple zoom :  with pixel:" + toWidth + "x" + toHeight);
            srcImg = MarkImageUtil.zoomImage(srcImg, toWidth, toHeight);
            writeByByte(numIid, sb, srcImg, iStatus);
        }

        return true;
    }

    static int STEP_HEIGHT = 500;

    private void appendByPic(Long numIid, Picture picture, StringBuffer sb, String oldWay, WirelessItemStatus iStatus)
            throws IOException {

        File dir = new File(Play.tmpDir, "pic_trans_" + (user.getId()));
        if (!dir.exists()) {
            dir.mkdirs();
        }
//        File tmpFile = new File(dir, System.currentTimeMillis() + "_" + picture.getPictureId() + ".jpg");
//        FileUtils.copyInputStreamToFile(intput, tmpFile);

        String originPath = picture.getPicturePath();
        String newPath = originPath.endsWith(".jpg") ? (originPath + "_600x600.jpg") : originPath;
        log.info("[new path:]" + newPath);

        long tStart = System.currentTimeMillis();
        BufferedImage srcImg = MarkImageUtil.readImage(newPath, true, false);
        if (srcImg == null) {
            log.warn(" fail for new path:" + newPath + " with numiid :" + numIid + "  pic:" + picture);
            return;
        }
        long tEnd = System.currentTimeMillis();

        int srcWidth = srcImg.getWidth();
        int srcHeight = srcImg.getHeight();

        log.info("took [" + (tEnd - tStart) + "]ms [width: ]" + srcWidth + " - height:" + srcHeight + " - path :"
                + newPath);

//        int width = picture.getWidth();
//        int height = picture.getHeight();
        if (srcWidth < 500) {
            // No need to care for this..
            log.warn(" too small width:" + srcWidth);
            return;
        }
        if (srcHeight < 30) {
            // No need to care for this...
            log.warn(" too small height" + srcHeight);
            return;
        }

        int toWidth = 600;
        int toHeight = srcHeight * toWidth / srcWidth;
        if (toHeight > 920) {

            if (!config.isAutoSplit()) {
                log.warn("no auto split for long pic of user;" + user.toIdNick() + " with path :" + picture);
                return;
            }
            log.warn(" too long height" + toHeight + " for pic url:" + picture);
            int step = 500;
            BufferedImage formedImg = MarkImageUtil.zoomImage(srcImg, toWidth, toHeight);
            for (int startHeight = 0; startHeight < toHeight; startHeight += step) {
                int endHeight = startHeight + step - 1;
                if (endHeight > toHeight) {
                    endHeight = toHeight;
                }
                File tmpFile = new File(dir, System.currentTimeMillis() + endHeight + ".jpg");
                int targetHeight = endHeight - startHeight;
                BufferedImage subimage = formedImg.getSubimage(0, startHeight, formedImg.getWidth(), targetHeight);
                writeFile(numIid, sb, tmpFile, subimage, iStatus);
            }

        } else if ((srcWidth >= 480 && srcWidth <= 600)) {
            writeByByte(numIid, sb, srcImg, iStatus);
//            File tmpFile = new File(dir, System.currentTimeMillis() + numIid + ".jpg");
//            writeFile(numIid, sb, tmpFile, srcImg);
//            tmpFile.deleteOnExit();
        } else {
            srcImg = MarkImageUtil.zoomImage(srcImg, toWidth, toHeight);
            writeByByte(numIid, sb, srcImg, iStatus);
//            File tmpFile = new File(dir, System.currentTimeMillis() + "_" + picture.getPictureId() + ".jpg");
//            File tmpFile = new File(dir, System.currentTimeMillis() + numIid + ".jpg");
//            writeFile(numIid, sb, tmpFile, newImg);
//            tmpFile.deleteOnExit();
//            writeFile(
        }

    }

//    private void directWrite(Long numIid, StringBuffer sb, Picture picture) {
//
//        Long pictureCid = numIidToPicCategory.get(numIid);
//        if (pictureCid == null) {
//            pictureCid = PicApi.get().ensureTMCat(user);
//        }
//
//        sb.append("<img>");
//        sb.append(picture.getPicturePath());
//        sb.append("</img>");
//    }

    private void writeByByte(Long numIid, StringBuffer sb, BufferedImage srcImg, WirelessItemStatus iStatus)
            throws IOException {

        Long pictureCid = numIidToPicCategory.get(numIid);
        if (pictureCid == null) {
            pictureCid = PicApi.get().ensureTMCat(user);
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(srcImg, "jpg", output);
        byte[] b = output.toByteArray();

        if (!iStatus.tryAddNewFileSize(b.length)) {
            return;
        }

        Picture uploaded = PicApi.get().uploadPic(user,
                new Random().nextInt(10000) + "_" + System.currentTimeMillis() + "_" + numIid + ".jpg", b,
                pictureCid);

        log.info("user:" + user.toIdNick() + "[new pic:]" + uploaded);

        sb.append("<img>");
        sb.append(uploaded.getPicturePath());
        sb.append("</img>");
    }

    private void writeFile(Long numIid, StringBuffer sb, File tmpFile, BufferedImage partImg, WirelessItemStatus iStatus)
            throws IOException {
        ImageIO.write(partImg, "jpg", tmpFile);
        writeFile(numIid, sb, tmpFile, iStatus);
    }

    private void writeFile(Long numIid, StringBuffer sb, File tmpFile, WirelessItemStatus iStatus) {

        log.info(format("writeFile:numIid, sb, tmpFile".replaceAll(", ", "=%s, ") + "=%s", numIid, sb.length(), tmpFile));

        if (!iStatus.tryAddNewFileSize((int) FileUtils.sizeOf(tmpFile))) {
            return;
        }

        Long pictureCid = numIidToPicCategory.get(numIid);
        Picture uploaded = PicApi.get().uploadPic(user, tmpFile.getName(), tmpFile, pictureCid);
//        log.info("user:" + user.toIdNick() + "[new pic:]" + uploaded);
        sb.append("<img>");
        sb.append(uploaded.getPicturePath());
        sb.append("</img>");
    }

//    private void appenImgLine(Long numIid, Long picCatId, String originPicUrl, StringBuilder sb) throws IOException {
//        log.info("[pic url:]" + originPicUrl);
//        String picUrl = WirelessAction.genWirelessPicUrl(originPicUrl);
//        log.info("[gen url:]" + picUrl);
//        if (StringUtils.isEmpty(picUrl)) {
//            return;
//        }
//
//        URL url = new URL(picUrl);
//        InputStream intput = url.openStream();
//        File tmpFile = new File(Play.tmpDir, System.currentTimeMillis() + "_" + numIid);
//        FileUtils.copyInputStreamToFile(intput, tmpFile);
//
//        String name = String.valueOf(numIid) + System.currentTimeMillis();
//        Picture picture = PicApi.get().uploadPic(user, name, tmpFile, picCatId);
//        log.info("[new pic:]" + picture);
//
//        sb.append("<img>");
//        sb.append(picture.getPicturePath());
//        sb.append("</img>");
//    }

    private void doForArgs(String[] args) {

        if (!isEngHeaderRead && isEngHeader(args)) {
            parseEngHeader(args);
            isEngHeaderRead = true;
            return;
        }

        if (!isChsHeaderRead && isChsHeader(args)) {
            parseChsHeader(args);
            isChsHeaderRead = true;
            return;
        }

        if (isDataRow(args)) {
            parseData(args);
            return;
        }

    }

    public List<WirelessItemAssistant> getItems() {
        return items;
    }

    public void setItems(List<WirelessItemAssistant> items) {
        this.items = items;
    }

}
