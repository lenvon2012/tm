
package autotitle;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import jdp.ApiJdpAdapter;
import models.item.ItemCatPlay;
import models.user.User;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import titleDiag.DiagResult;
import titleDiag.TitleDiagnose;
import utils.PlayUtil;
import autotitle.AutoTitleOption.BatchPageOption;
import bustbapi.ShowWindowApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.NumberUtil;
import com.taobao.api.domain.Item;

import configs.TMConfigs;

public class AutoTitleAction {
    static final Logger log = LoggerFactory.getLogger(AutoTitleAction.class);

    public static final String TAG = "AutoTitleAction";

    public static AutoTitleAction _instance = new AutoTitleAction();

    public AutoTitleAction() {
    }

//    public static void updateRecommendInfo(User user, ItemPlay item) {
//        String recommend = autoRecommend(user, item.getNumIid());
//        item.setRecommendTitle(recommend);
//    }

    public static String autoRecommend(User user, Long numIid, BatchPageOption pageOpt) {

        log.info(format("autoRecommend:user, numIid, pageOpt".replaceAll(", ", "=%s, ") + "=%s", user, numIid, pageOpt));

//        ItemGet itemGet = ApiAdapter.get().findItem(user, numIid);
//        Item call = itemGet.call();

        Item call = ApiJdpAdapter.get(user).findItem(user, numIid);
//        log.error("item :"+call.getCid());
//        log.error("item :"+call.getSellerCids());

        if (call == null) {
//            ErrorHandler.fuckWithTheErrorCode(user.getId(), user.getSessionKey(), itemGet.getSubErrorCode());
            return StringUtils.EMPTY;
        }

        if (ItemCatPlay.isBaoyou(call.getCid())) {
            log.error("[item is baoyou cid..]" + call.getCid());
            return call.getTitle();
        }

        AutoTitleOption opt = new AutoTitleOption(call, pageOpt);
//        log.info("[option :]" + opt);
//        return AutoTitleEngine.autoRecommend(opt);
        try {
            return new AutoTitleEngine(opt).call();
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        return StringUtils.EMPTY;
    }

    public static String autoRecommend(User user, Long numIid) {

//        log.info(format("autoRecommend:user, numIid".replaceAll(", ", "=%s, ") + "=%s", user, numIid));

        Item call = ApiJdpAdapter.get(user).findItem(user, numIid);

//        log.error("item :"+call.getCid());
//        log.error("item :"+call.getSellerCids());
        if (call == null) {
//            ErrorHandler.fuckWithTheErrorCode(user.getId(), user.getSessionKey(), itemGet.getSubErrorCode());
            return StringUtils.EMPTY;
        }
        return autoRecommend(call);
    }

    public static String autoRecommend(Item call, BatchPageOption pageOpt) {
    	
        if (ItemCatPlay.isBaoyou(call.getCid())) {
            log.error("[item is baoyou cid..]" + call.getCid());
            return call.getTitle();
        }
        log.info("[before to send :]" + call.getTitle());
        AutoTitleOption opt = new AutoTitleOption(call, pageOpt);
        log.info("[now to send :]" + opt.getRawTitle());
//        return AutoTitleEngine.autoRecommend(opt);
        try {
            return new AutoTitleEngine(opt).call();
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        return StringUtils.EMPTY;
    }

    public static String autoRecommend(Item call) {
        return autoRecommend(call, new BatchPageOption());
    }

    public static List<DiagResult> recommendTitles(final User user, String numIids) {
        List<Long> idsList = PlayUtil.parseIdsList(numIids);
        List<FutureTask<DiagResult>> list = new ArrayList<FutureTask<DiagResult>>(idsList.size());
//        Map<Long, DiagResult> res = new HashMap<Long, DiagResult>();
        List<DiagResult> res = new ArrayList<DiagResult>(idsList.size());


        if (CommonUtils.isEmpty(idsList)) {
            return ListUtils.EMPTY_LIST;
        }

        List<Item> remoteItems = ApiJdpAdapter.get(user).tryItemList(user, idsList);

//        log.warn("remote size:" + remoteItems);
        if (remoteItems.size() != idsList.size()) {
            Set<Long> remoteIds = ShowWindowApi.toNumIids(remoteItems);
            idsList.removeAll(remoteIds);

            for (Long numIid : idsList) {
                Item item = ApiJdpAdapter.tryFetchSingleItem(user, numIid);
                if (item != null) {
                    remoteItems.add(item);
                }
            }

        }

        for (final Item itemWithProps : remoteItems) {

            list.add(TMConfigs.getDiagResultPool().submit(new Callable<DiagResult>() {

                @Override
                public DiagResult call() throws Exception {
                    if (itemWithProps == null) {
                        return null;
                    }

                    String title = autoRecommend(itemWithProps);
//                    log.error("title >>>>> : " + title);
//                    log.error("no prop >>>>>" + itemWithProps);

                    double price = NumberUtil.parserDouble(itemWithProps.getPrice(), 0.0d);
                    long delistTime = itemWithProps.getDelistTime() == null ? 0L : itemWithProps.getDelistTime()
                            .getTime();

                    DiagResult doWord = TitleDiagnose.getInstance().doWord(itemWithProps.getNumIid(), price, title,
                            itemWithProps.getPropsName(), itemWithProps.getPicUrl(), -1, delistTime,
                            itemWithProps.getCid());

                    return doWord;
                }

            }));
        }

        CommonUtils.sleepQuietly(300L);

        for (FutureTask<DiagResult> task : list) {
            try {
                DiagResult diagResult = task.get();
                if (diagResult != null) {
//                    res.put(diagResult.getNumIid(), diagResult);
                    res.add(diagResult);
                }
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }
        return res;
    }
}
