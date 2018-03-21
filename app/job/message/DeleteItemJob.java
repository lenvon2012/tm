
package job.message;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import jdp.JdpModel.JdpItemModel;
import models.item.ItemPlay;
import models.showwindow.OnWindowItemCache;
import models.showwindow.ShowwindowMustDoItem;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.NoTransaction;
import play.jobs.Every;
import play.jobs.Job;
import cache.UserHasTradeItemCache;
import dao.UserDao;
import dao.item.ItemDao;

@Every("15s")
@NoTransaction
public class DeleteItemJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(DeleteItemJob.class);

    public static final String TAG = "DeleteItemJob";

    public static final int retry = 2;

    static Queue<String> atsMsgQueue = new ConcurrentLinkedQueue<String>();

    static Queue<JdpItemModel> jdpItemMsgQueue = new ConcurrentLinkedQueue<JdpItemModel>();

    @Override
    public void doJob() {
        String msg = null;
        while ((msg = atsMsgQueue.poll()) != null) {
            deleteItem(msg);
        }

        JdpItemModel jMsg = null;
        while ((jMsg = jdpItemMsgQueue.poll()) != null) {
            String nick = jMsg.getNick();
            if (StringUtils.isEmpty(nick)) {
                continue;
            }

            User user = UserDao.findByUserNick(nick);
            if (user == null) {
                log.warn("no user for :" + jMsg);
            }

            tryDeleteItem(user.getId(), jMsg.getNumIid());
        }

    }

    public static void addJdpTbItem(JdpItemModel jItem) {
        jdpItemMsgQueue.add(jItem);
    }

    public static void addAtsMsg(String msg) {
        atsMsgQueue.add(msg);
//		log.info("addMsg into DeleteItem Queue :" + msg);
    }

    public void deleteItem(String msg) {
        boolean isSuccess = false;
        int tryTime = 0;
        try {
            JSONObject notify_delete = new JSONObject(msg).getJSONObject("notify_item");
            long userId = notify_delete.getLong("user_id");
            long numIid = notify_delete.getLong("num_iid");
            while (!isSuccess && tryTime++ < retry) {
                isSuccess = tryDeleteItem(userId, numIid);
            }
        } catch (JSONException e) {
            log.warn(e.getMessage(), e);
        }
    }

    public static boolean tryDeleteItem(long userId, long numIid) {
        boolean isSuccess = false;
        ItemPlay itemPlay = ItemDao.findByNumIid(userId, numIid);
//        log.info("[find to delete item;]" + itemPlay);
        if (itemPlay != null) {
            isSuccess = itemPlay.rawDelete();
        }
        ShowwindowMustDoItem.remove(userId, numIid);

        if (isSuccess) {
            User user = UserDao.findById(userId);
            if (user != null) {
                UserHasTradeItemCache.removeForChange(user, numIid);
                UserDelistUpdateJob.addUser(user);
                if (user.isShowWindowOn()) {
                    OnWindowItemCache.get().refresh(user);
                }
            }
        }
        return isSuccess;
    }

}
