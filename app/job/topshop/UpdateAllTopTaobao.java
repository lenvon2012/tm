
package job.topshop;

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

public class UpdateAllTopTaobao extends Job {

    private static final Logger log = LoggerFactory.getLogger(UpdateAllTopTaobao.class);

    public static final String TAG = "UpdateAllTopHotWord";

    @Override
    public void doJob() {

        List<TopURLBase> allUrl = TopURLBase.find("level = 3 ").fetch();
        log.info("Top BVase:" + allUrl);
        for (TopURLBase topURLBase : allUrl) {
            topURLBase.updateType();
        }

//        int count = 0;
        for (TopURLBase urlModel : allUrl) {
//            urlModel= JPA.em().merge(urlModel);
            try {
                log.error("Url :" + urlModel);
                doForURL(urlModel, appendHotRank(urlModel, true), true);
                doForURL(urlModel, appendHotRank(urlModel, false), false);
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }
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

    private void doForURL(TopURLBase urlModel, String rawUrl, boolean isSearchRank) {
        TopHotUpKeyPojo pojo = new TopHotKeyParser().parse(rawUrl);
//        TopHotUpKeyPojo pojo = TopSpiderService.getWordList(rawUrl);
        if (pojo == null) {
            log.error("No Pojo Returned .... for rawUrl:" + rawUrl);
        }
        urlModel.setCatText(StringUtils.replaceChars(pojo.catogery, '\n', ' '));

        List<TopHotKeyItem> items = pojo.items;
        log.info("Item Size :" + items.size());
        int length = items.size();
        for (int i = 0; i < length; i++) {
            TopHotKeyItem item = items.get(i);
            log.info("For Item:" + item);
            TopKey.ensure(item, urlModel, i + 1, isSearchRank);

        }

//        urlModel.updateType();

        JPATransactionManager.clearEntities();
    }
}
