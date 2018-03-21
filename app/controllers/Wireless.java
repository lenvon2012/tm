/**
 * 
 */

package controllers;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import job.apiget.ItemUpdateJob;

import models.item.ItemPlay;
import models.task.AutoTitleTask;
import models.task.AutoTitleTask.UserTaskStatus;
import models.task.AutoTitleTask.UserTaskType;
import models.task.AutoTitleTask.WireLessDetailConfig;
import models.user.User;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import play.Play;
import play.mvc.Before;
import result.TMResult;
import utils.PlayUtil;
import actions.wireless.WireLessUtil;
import actions.wireless.WirelessItemField;
import actions.wireless.WirelessItemField.NumIidField;
import actions.wireless.WirelessItemField.WirelessDescField;
import bustbapi.ItemApi.ItemDescGet;
import bustbapi.ItemApi.ItemFullGet;
import bustbapi.PicApi;
import carrier.WirelessAction;

import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.MixHelpers;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.Picture;
import com.taobao.api.request.ItemUpdateDelistingRequest;

import dao.item.ItemDao;

/**
 * @author navins
 * @date: Jan 23, 2014 1:36:31 AM
 */
public class Wireless extends TMController {

    public static void onekey() {
        render("PhoneDetaileds/phonesIndex.html");
//        render("carrier/wirelessonekey.html");
    }

    public static void index() {
        render("carrier/mobiledesc.html");
    }

    public static void tasks() {
        render("carrier/wirelesstasks.html");
    }

    public static void help() {
        render("carrier/wirelesshelp.html");
    }

    public static void video() {
        render("carrier/wirelessvideo.html");
    }

    public static void items(String s, int pn, int ps, final int lowBegin, final int topEnd, int sort, int status,
            String catId, Long cid) {
        User user = getUser();
        log.info(format("params:userId, s, pn, ps, lowBegin, topEnd, sort, status, catId".replaceAll(", ", "=%s, ")
                + "=%s", user.getId(), s, pn, ps, lowBegin, topEnd, sort, status, catId));
        PageOffset po = new PageOffset(pn, ps);
        List<ItemPlay> list = ItemDao.findOnlineByUserWithArgs(user.getId(), po.getOffset(), po.getPs(), s, lowBegin,
                topEnd, sort, status, catId, cid, false);
        long count = ItemDao.countOnlineByUserWithArgs(user.getId(), lowBegin, topEnd, s, 0L, status, catId, cid);
        TMResult res = new TMResult(list, (int) count, po);
        renderJSON(JsonUtil.getJson(res));
    }

    public static void genDesc(Long numIid) {
        User user = getUser();
        Item item = new ItemDescGet(user, numIid).call();
        if (item == null) {
            renderText("");
        }
        String desc = item.getDesc();
        String newDesc = WirelessAction.genWirelessDesc(desc);
        renderText(newDesc);
    }

    public static void genAndZip(Long numIid) {
        User user = getUser();
        Item item = new ItemDescGet(user, numIid).call();
        if (item == null) {
            renderText("对不起，获取宝贝信息出错，请重试！");
        }

        String zipPath = WirelessAction.downloadAndZipItem(user.getId(), item.getTitle(), item.getDesc());
        if (StringUtils.isEmpty(zipPath)) {
            zipPath = WirelessAction.fetchTxtXmlFile(user.getId(), item.getTitle(), item.getDesc());
        }
        if (StringUtils.isEmpty(zipPath)) {
            renderText("该宝贝详情页中没有发现图片，请先编辑好电脑端详情页！");
        }
        renderBinary(new File(zipPath));
    }

    public static void batchGen(String numIids) {
        User user = getUser();
        List<Long> list = PlayUtil.parseIdsList(numIids);

        String fields = "num_id,wireless_desc";
        List<String[]> records = new ArrayList<String[]>();
        records.add(new String[] {
                WirelessItemField.NumIidField.getOne().getFieldChnName(),
                WirelessItemField.WirelessDescField.getOne().getFieldChnName()
        });
        for (Long id : list) {
            Item item = new ItemFullGet(user, id).call();
            String wirelessDesc = WirelessAction.translateWirelessDesc(user, item.getTitle(), item.getDesc());

            records.add(new String[] {
                    item.getNumIid().toString(), wirelessDesc
            });
        }

        String fileName = Play.tmpDir.getPath() + "/批量导出手机详情页面" + user.getId() + "_" + System.currentTimeMillis()
                + "_numIids" + ".xls";
//        ExcelUtil.writeToExcel(records, fields, "手机详情页模板", fileName);
//
//        File file = new File(fileName);
//        renderBinary(file);
        StringBuilder sb = new StringBuilder();
        for (String[] strings : records) {
            sb.append(StringUtils.join(strings, ','));
            sb.append('\n');
        }
        renderText(sb.toString());

    }

    @Before(only = {
            "submitCsv"
    })
    public static void doEncode() {
//        log.error(">>>>>> do before");
//        request.encoding = "gbk";
//        Request.current().encoding = "gbk";
//        new ApacheMultipartParser().parse(request.body);
//        Request.current().encoding = "utf8";
//        try {
//            List<String> readLines = IOUtils.readLines(request.body);
//            for (String string : readLines) {
//                System.out.println(string);
//            }
//        } catch (IOException e) {
//            log.warn(e.getMessage(), e);
//        }

    }

    public static void submitCsv(File csvfile, WireLessDetailConfig config) throws IOException {
//        List<Upload> uploads = (List<Upload>) request.args.get("__UPLOADS");
//        for (Upload upload : uploads) {
//            File file = upload.asFile();
//            log.info("[uploads:]" + file.getAbsolutePath());
//        }
        log.info(format("submitCsv:csvfile, config".replaceAll(", ", "=%s, ") + "=%s", csvfile, config));
        User user = getUser();

        MixHelpers.infoAll(request, response);
        if (csvfile == null) {
            renderFailedJson("亲,请先选择上传的csv文件");
        }
        if (AutoTitleTask.hasRecentSame(user, csvfile.getName(), UserTaskType.BuildPhoneDetailByTaobaoZhuli)) {
            renderFailedJson("亲,已经有相同任务存在,无需重复提交哟亲");
        }
        if (!csvfile.getName().endsWith("csv")) {
            renderFailedJson("亲, 请上传csv文件哟亲");
        }
//        String name = csvfile.getName();
//        Long userId = user.getId();
        File renameDir = new File(WireLessUtil.genWirelessInputDir(), String.valueOf(user.getId() % 1000L));
        if (!renameDir.exists()) {
            renameDir.mkdirs();
        }

        File renameFile = new File(renameDir, System.currentTimeMillis() + csvfile.getName());

        renameFile.deleteOnExit();
//        csvfile.renameTo(renameDFile);
        FileUtils.copyFile(csvfile, renameFile);
        config.setFilePath(renameFile.getAbsolutePath());

        AutoTitleTask task = new AutoTitleTask(user.getId(), JsonUtil.getJson(config),
                UserTaskStatus.New, UserTaskType.BuildPhoneDetailByTaobaoZhuli);

        boolean isSuccess = task.jdbcSave();
        if (isSuccess) {
            renderSuccessJson();
        } else {
            renderFailedJson("任务提交失败,请稍后重试或联系客服哟亲");
        }

        renderSuccessJson();
    }

    public static void batchSubmit(String numIids, WireLessDetailConfig config) {

        log.info(format("batchSubmit:numIids, config".replaceAll(", ", "=%s, ") + "=%s", numIids, config));

        User user = getUser();
        config.setNumIids(numIids);
        String configJson = JsonUtil.getJson(config);
        if (AutoTitleTask.hasRecentSame(user, configJson, UserTaskType.BuildPhoneDetailByNumIids)) {
            renderFailedJson("亲,已经有相同任务存在,无需重复提交哟亲");
        }
        AutoTitleTask task = new AutoTitleTask(user.getId(), JsonUtil.getJson(config), UserTaskStatus.New,
                UserTaskType.BuildPhoneDetailByNumIids);

        boolean isSuccess = task.jdbcSave();
        if (isSuccess) {
            renderSuccessJson();
        } else {
            renderFailedJson("任务提交失败,请稍后重试或联系客服哟亲");
        }

        renderSuccessJson();
    }

    public static void down(long taskId, String ver) {

        log.info(format("down:taskId, ver".replaceAll(", ", "=%s, ") + "=%s", taskId, ver));

        User user = getUser();
        AutoTitleTask task = AutoTitleTask.queryByTaskId(user.getId(), taskId);
        if (task == null) {
            renderText("任务不存在或者已经过期");
        }
        if (task.getUserId().longValue() != task.getUserId().longValue()) {
            renderText("这个任务好像不属于亲");
        }
        File file = null;
        if (StringUtils.isBlank(ver)) {
            String path = task.getMessage();
            file = new File(path);

        } else {
            Map map = task.genPageRes();
            Object path = map.get(ver);
            log.info("[found path]" + path);

            file = new File(path.toString());
        }

        if (file == null || !file.exists()) {
            renderText("任务文件不存在或者已经过期");
        }

        renderBinary(file);
    }

    public static void find() {
        Set<String> urls = new HashSet<String>();
        urls.add("http://img01.taobaocdn.com/imgextra/i1/1039626382/T2a4K8XHXaXXXXXXXX-1039626382.jpg");

        User user = getUser();
        List<Picture> pics = PicApi.get().ensureOriginPicture(user, urls);
        System.out.println(pics);
    }

    public static void testFind() {
        /*
         *04114693383b9c26225f8b29745a6095:1:3:|
         * ;
         * e9edd2c170d5e4bbfc7ecd1e3280efdc:1:4:|
         */
        String[] arr = new String[] {
                "",
                ""
        };

        Set<String> set = new HashSet<String>();
        set.add("http://img04.taobaocdn.com/bao/uploaded/i4/1039626382/T2SWiUXd4aXXXXXXXX_!!1039626382.jpg");
        set.add("http://img03.taobaocdn.com/bao/uploaded/i3/1039626382/T2gMCTXkxaXXXXXXXX_!!1039626382.jpg");
        User user = getUser();
        List<Picture> pictures = PicApi.get().ensureOriginPicture(user, set);
        for (Picture picture : pictures) {
//            System.out.println(picture.getTitle());
            System.out.println(picture);
        }
    }

    public static void singlePicture(Long id) {
        User user = getUser();
        Picture picture = PicApi.get().singlePicture(user, id);
        renderText(picture);
    }

    public static void testItemId() {
        User user = getUser();
        new ItemUpdateJob(user.getId()).doJob();

        List<ItemPlay> list = ItemDao.findByUserId(user.getId(), 3);
        List<Long> ids = new ArrayList<Long>();
        for (ItemPlay itemPlay : list) {
            ids.add(itemPlay.getNumIid());
        }

        WireLessDetailConfig config = new WireLessDetailConfig();
        config.setNumIids(StringUtils.join(ids, ','));

        AutoTitleTask task = new AutoTitleTask(user.getId(), JsonUtil.getJson(config), UserTaskStatus.New,
                UserTaskType.BuildPhoneDetailByNumIids);
        task.save();

        renderText("已经上传");
    }

    public static void lecture() {
        render("carrier/wirelesslecture.html");
    }
}
