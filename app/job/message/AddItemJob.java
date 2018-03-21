
package job.message;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.FutureTask;

import jdp.ApiJdpAdapter;
import jdp.JdpModel.JdpItemModel;
import job.ump.UmpMjsTmplUpdateJob;
import models.item.ItemPlay;
import models.promotion.TMProActivity;
import models.user.User;

import org.elasticsearch.common.util.concurrent.jsr166y.ConcurrentLinkedDeque;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import play.db.jpa.NoTransaction;
import play.jobs.Every;
import play.jobs.Job;
import titleDiag.DiagResult;
import actions.DiagAction;
import bustbapi.OperateItemApi;

import com.ciaosir.client.CommonUtils;
import com.taobao.api.domain.Item;

import configs.TMConfigs;
import controllers.APIConfig;
import dao.UserDao;
import dao.item.ItemDao;

@Every("5s")
@NoTransaction
public class AddItemJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(AddItemJob.class);

    public static final String TAG = "AddItemJob";

    public static final int retry = 2;

    static Queue<String> atsMsgQueue = new ConcurrentLinkedQueue<String>();

//    static Queue<JdpItemModel> jdpItemQueue = new ConcurrentLinkedDeque<JdpItemModel>();
    static Queue<Item> jdpItemQueue = new ConcurrentLinkedDeque<Item>();

    @Override
    public void doJob() {
//        if (!TMConfigs.App.IS_ADD_ITEM_ALLOW) {
//            return;
//        }

        String msg = null;
//        log.error("do AddItemJob started with queue size = " + atsMsgQueue.size() + " !!!!!!!!!!!!!!!");
        while ((msg = atsMsgQueue.poll()) != null) {
            addItem(msg);
        }
        Item jdpModel = null;
        while ((jdpModel = jdpItemQueue.poll()) != null) {
            User user = UserDao.findByUserNick(jdpModel.getNick());
            if (user == null) {
                continue;
            }

            doForAdd(user, jdpModel);
        }
//
//        if (Rds.Enable_Jdp_Push) {
//            checkForRecentJdpItems();
//        }
    }

    private void checkForRecentJdpItems() {
        List<Item> items = JdpItemModel.recentAdded(10000L);
        if (CommonUtils.isEmpty(items)) {
            return;
        }

        for (Item item : items) {
            if (isItemRecentAdded(item.getNumIid())) {
                continue;
            }
            User user = UserDao.findByUserNick(item.getNick());
            if (user == null) {
                continue;
            }

            doForAdd(user, item);
        }
    }

    static String TAG_RECENT_ADD = "RecentAddItemId_";

    public static void addMsg(String msg) {
        if (!TMConfigs.App.IS_ADD_ITEM_ALLOW) {
            return;
        }
        atsMsgQueue.add(msg);
//        log.info("addMsg into AddItem Queue :" + msg);
    }

    public static void addJdpItem(Item item) {
        if (!TMConfigs.App.IS_ADD_ITEM_ALLOW) {
            return;
        }
        jdpItemQueue.add(item);
    }

    public static void markItemRecentAddTag(Long numIid) {
        Cache.set(TAG + numIid, Boolean.TRUE, "1h");
    }

    public static boolean isItemRecentAdded(Long numIid) {
        return Cache.get(TAG + numIid) != null;
    }

    public void addItem(String msg) {
//        log.info("[try get item:]" + msg);

        boolean isSuccess = false;
        int tryTime = 0;
        try {
            /*
             * {"notify_item":{"topic":"item","status":"ItemAdd","user_id":412536637,"nick":"张顺罗",
             * "modified":"2013-11-16 17:26:19","num":100,
             * "title":"思密达韩国代购正品2013冬季新款绅士兔可爱蝴蝶结针织衫",
             * "price":"340.00","num_iid":36091123181}} 
             */
            JSONObject notify_add = new JSONObject(msg);
            if (notify_add.has("notify_item")) {
                notify_add = notify_add.getJSONObject("notify_item");
            }

            while (!isSuccess && tryTime++ < retry) {
                Long userId = notify_add.getLong("user_id");
                if (userId == null) {
                    break;
                }
                final User user = UserDao.findById(userId);
                if (user == null) {
                    break;
                }
                Long numIid = notify_add.getLong("num_iid");
                if (numIid == null) {
                    break;
                }
                if (isItemRecentAdded(numIid)) {
                    isSuccess = true;
                    break;
                }

                final Item item = ApiJdpAdapter.singleItem(user, numIid);
//                final Item item = new ItemApi.SingleItemGet(numIid).call();
                if (item == null) {
                    continue;
                }

                isSuccess = doForAdd(user, item);
/*                if (isSuccess) {
                    log.info("add item success for " + numIid);
                } else {
                    log.error("add item failed for " + numIid);
                }*/
//                log.warn("[save item:]" + itemPlay);
            }
        } catch (JSONException e) {
            log.warn(e.getMessage(), e);
        }
    }

    public static boolean doForAdd(final User user, final Item item) {
        Long numIid = item.getNumIid();
        Long userId = user.getId();
        boolean isSuccess = false;

        ItemPlay itemPlay = ItemDao.findByNumIid(userId, numIid);
        if (itemPlay == null) {
            itemPlay = new ItemPlay(userId, item);
        } else {
            itemPlay.updateWrapper(item);
        }

        if (APIConfig.get().isItemScoreRelated()) {
            final Integer tradeCount = itemPlay.getSalesCount();
            // 更新宝贝标题得分
            FutureTask<DiagResult> task = TMConfigs.getDiagResultPool().submit(new Callable<DiagResult>() {
                @Override
                public DiagResult call() throws Exception {
                    return DiagAction.doDiag(user, item, null, tradeCount);
                }
            });

            DiagResult doWord;
            try {
                doWord = task.get();
                itemPlay.setScore(doWord.getScore());
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }

        }
        
        // 更新全店满就送模板
        if(APIConfig.get().isNeedToUpdateMjsTmpl()) {
        	log.info("do add Shop Mjs Tmpl Add for user : " + user.getUserNick() + " and " +
        			"numIid = " + numIid);
        	// 是否有生效中的全店满就送活动
        	List<TMProActivity> activitys = TMProActivity.findShopMjsActivitysOn(userId);
        	if(!CommonUtils.isEmpty(activitys)) {
        		for(TMProActivity tmActivity : activitys) {
        			UmpMjsTmplUpdateJob.addTmpl(user, numIid, tmActivity.getTmplHtml(), tmActivity.getId(), false);
        		}
        	}
        }
        
        isSuccess = itemPlay.jdbcSave();

        if (isSuccess == true) {
            UserDelistUpdateJob.addUser(user);
            markItemRecentAddTag(item.getNumIid());
            OperateItemApi.clearNoMoreCandidatesCached(user);
            if (itemPlay.getStatus() == ItemPlay.Status.ONSALE) {

            }
        }

        return isSuccess;
    }
}
