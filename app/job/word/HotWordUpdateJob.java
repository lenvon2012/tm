
package job.word;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;

import models.word.top.TopKey;
import models.word.top.TopURLBase;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import pojo.webpage.top.TopHotKeyItem;
import pojo.webpage.top.TopHotKeyParser;
import pojo.webpage.top.TopHotUpKeyPojo;
import transaction.JPATransactionManager;

import com.ciaosir.commons.ClientException;

public class HotWordUpdateJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(HotWordUpdateJob.class);

    public static final String TAG = "UpdateAllTopHotWord";

    @Override
    public void doJob() {

        List<TopURLBase> allUrl = TopURLBase.find("level = 3 ").fetch();
        log.info("Top BVase:" + allUrl);
        for (TopURLBase topURLBase : allUrl) {
            topURLBase.updateType();
        }

        int count = 0;

        for (TopURLBase urlModel : allUrl) {
//            urlModel= JPA.em().merge(urlModel);
            log.error("Url :" + urlModel);
            try {
                doForUrlModel(urlModel);
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }

            count++;
        }
    }

    private void doForUrlModel(TopURLBase urlModel) throws ClientException {
        Long topURLBaseId = urlModel.getId();

        TopKey.getDp().update(" delete from " + TopKey.TABLE_NAME + " where topUrlBaseId = ? ",
                urlModel.getId());

        List<TopKey> res = new ArrayList<TopKey>();

        res.addAll(doForURL(urlModel, appendHotRank(urlModel, true), true));
        res.addAll(doForURL(urlModel, appendHotRank(urlModel, false), false));

        TopKey.update(topURLBaseId, 0, 128);

        JPATransactionManager.clearEntities();

//                if (count > 3) {
//                    return;
//                }
        log.error(" do for top url base hot word update current for :" + urlModel);
    }

    private String appendHotRank(TopURLBase urlModel, boolean isSearchRank) {
        String rawUrl = urlModel.getUrl();
        String appendParams = isSearchRank ? "&show=focus&up=false" : "&show=focus&up=true";

        if (rawUrl.contains("up=true")) {
            rawUrl = rawUrl.replaceAll("&up=true", appendParams);
        } else {
            if (rawUrl.endsWith("&")) {
                rawUrl = rawUrl + appendParams;
            } else {
                rawUrl = rawUrl + appendParams;
            }
        }
        return rawUrl;
    }

    private List<TopKey> doForURL(TopURLBase urlModel, String rawBaseUrl, boolean isSearchRank) {

        log.info(format("doForURL:urlModel, rawBaseUrl, isSearchRank".replaceAll(", ", "=%s, ") + "=%s", urlModel,
                rawBaseUrl, isSearchRank));

        int offset = 0;
        List<TopKey> keys = new ArrayList<TopKey>();

        try {
//            while (true) {
            log.info("[do for url model :" + urlModel + "] with offset :" + offset);
            String currUrl = rawBaseUrl + "&offset=" + offset;
            TopHotUpKeyPojo pojo = new TopHotKeyParser().parse(currUrl);

            if (pojo == null) {
                log.error("No Pojo Returned .... for rawUrl:" + rawBaseUrl);
            }

            urlModel.setCatText(StringUtils.replaceChars(pojo.catogery, '\n', ' '));

            List<TopHotKeyItem> items = pojo.items;
            log.info("Item Size :" + items.size());
            int length = items.size();

            for (int i = 0; i < length; i++) {
                TopHotKeyItem item = items.get(i);
                log.info("For Item:" + item);
                TopKey key = TopKey.ensure(item, urlModel, i + 1, isSearchRank);
                keys.add(key);
            }

//                if (length <= 0) {
//                    log.info("over at offset :" + offset);
//                    break;
//                } else {
////                    offset += 30;
//                    offset += 100000000;
//                }

            JPATransactionManager.clearEntities();
//            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }

        return keys;
    }

    public static class TopEtaoWordLevel2UpdateJob extends Job {
        public void doJob() {
            List<TopURLBase> allUrl = TopURLBase.find("level = 2 ").fetch();

            for (TopURLBase topURLBase : allUrl) {
                HotWordUpdateJob caller = new HotWordUpdateJob();
                try {
                    caller.doForUrlModel(topURLBase);
                } catch (ClientException e) {
                    log.warn(e.getMessage(), e);
                }
            }

        }
    }
}
