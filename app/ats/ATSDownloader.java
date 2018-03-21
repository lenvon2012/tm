
package ats;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import models.visit.ATSLocalTask;
import models.visit.ATSLocalTask.CompressType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transaction.NoTransactionSecurity;
import utils.ATSUtils;
import ats.TaskManager.Status;

import com.ciaosir.client.CommonUtils;
import com.taobao.api.ApiException;

import configs.TMConfigs.ATS;

public class ATSDownloader implements Callable<Boolean> {

    private static final Logger log = LoggerFactory.getLogger(ATSDownloader.class);

    Long userId;

    Long ts;

    File downDir;

    File unzipDir;

    ATSLocalTask task;

    boolean isZipped = true;

    static void ensureAtsDir() {

    }

    public ATSDownloader(ATSLocalTask task) {
        this.task = task;
        this.userId = task.getUserId();
        this.ts = task.getTs();
        switch (task.getTaskType()) {
            case ATSTradeSold:
            case ATSSimpleTrade:
            default:
                downDir = CommonUtils.ensureDir(ATS.TRADE_SOLD_ZIP_DIR, ATSLocalTask.genModKey(userId));
                downDir = CommonUtils.ensureDir(downDir, String.valueOf(userId));
                unzipDir = CommonUtils.ensureDir(ATS.TRADE_SOLD_UNZIP_DIR, ATSLocalTask.genModKey(userId));
                unzipDir = CommonUtils.ensureDir(unzipDir, String.valueOf(userId));
                isZipped = true;
                break;

//            case ATSTradeHistory:
//                downDir = FileUtil.ensureDir(PolicyUtil.HISTORY_TRADE_DOWNLOAD_DIR, String.valueOf(userId));
//                unzipDir = FileUtil.ensureDir(PolicyUtil.HISTORY_TRADE_UNGZIP_DIR, String.valueOf(userId));
//                isZipped = false;
//                break;
//            case VisitLog:
//                downDir = FileUtil.ensureDir(PolicyUtil.VISITLOG_DOWNLOAD_FILEDIR, String.valueOf(userId));
//                unzipDir = FileUtil.ensureDir(PolicyUtil.VISITLOG_UNGZIP_FILEDIR, String.valueOf(userId));
//                isZipped = true;
//                break;
        }

    }

    public Boolean call() {
        if (!ATSLocalTask.isTaskDoneByTaobao(task)) {
            log.warn("Not Taobao Done Task:" + task);
            return false;
        }

        Boolean success = Boolean.FALSE;
        try {
            ATSLocalTask.updateStatusInDelay(task, TaskManager.Status.DOWNLOADING);

            String savedFile = String.valueOf(ts);

            success = obtainRawVisitLog(task, downDir, unzipDir, savedFile, userId, ts, isZipped);
        } finally {
            log.info("[Download Result]:" + success);
            ATSLocalTask.updateStatusInDelay(task, success ? Status.DOWNLOADED : Status.NEW);
        }

        return success;
    }

    public static Boolean obtainRawVisitLog(final ATSLocalTask task, final File downloadDir, final File unzipDir,
            final String saveFileName, Long userId, Long ts, boolean isZipped) {

        File target = new File(downloadDir, saveFileName);

        File dataFile = downloadFile(task, target, unzipDir, isZipped);

        if (!CommonUtils.exists(dataFile)) {
            return Boolean.FALSE;
        }

        log.info("[Set Data File]" + dataFile);
        ATSLocalTask.writeFilePathInDelay(task, dataFile);

        return Boolean.TRUE;
    }

    public static File downloadFile(final ATSLocalTask task, final File downloadTarget, final File unzipDir,
            final boolean isZipped) {

        return new NoTransactionSecurity<File>() {

            @Override
            public File noOpOnDB() {
                try {
                    return downAndUnzipUrl(task, downloadTarget, unzipDir, isZipped);
                } catch (ApiException e1) {
                    log.warn(e1.getMessage(), e1);
                    log.error("Current Task:" + task);
                    return null;
                } catch (IOException e) {
                    log.warn(e.getMessage(), e);
                    log.error("Current Task:" + task);
                    return null;
                }
            }
        }.execute();
    }

    private static File downAndUnzipUrl(final ATSLocalTask task, final File downloadTarget, final File unzipDir,
            boolean isZipped) throws ApiException, IOException {
        File unzippedFile = null;
        File downFile = null;

        if (TaskManager.isMD5Equals(task, downloadTarget)) {
            log.info("File [" + downloadTarget.getAbsolutePath() + "] exists..no needs to download...");
            downFile = downloadTarget;
        } else {

            FileUtils.deleteQuietly(downloadTarget);

            String downUrl = task.getDownloadUrl();
            log.info("start to download for:" + downloadTarget);

            downFile = ATSUtils.download(downUrl, downloadTarget);
            log.info("end to download for:" + downloadTarget + " with size:[" + downloadTarget.length() + "] with");

            // if(FileUtil.)
            boolean checksumEqual = TaskManager.isMD5Equals(task, downloadTarget);
            log.info("[check sum result]" + checksumEqual);
            if (!checksumEqual && !StringUtils.isEmpty(task.getCheckCode())) {
                FileUtils.deleteQuietly(downFile);
                log.warn("Downloaded CheckSum Not Equal...Retry later....");
                return null;
            }
        }

        CompressType zipType = task.getZipType();
        if (zipType == CompressType.NONE) {
            return downFile;
        }

        unzippedFile = new File(unzipDir, downFile.getName());
        if (unzippedFile.exists()) {
            unzippedFile.delete();
        }

        switch (zipType) {
            case ZIP:
                ATSUtils.unZip(downFile, unzippedFile);
                break;
            default:
                ATSUtils.unGzip(downFile, unzippedFile);
                break;
        }

        log.info("[Unzipped size[" + unzippedFile.length() + "] File:" + unzippedFile);

        return unzippedFile;
    }

}
