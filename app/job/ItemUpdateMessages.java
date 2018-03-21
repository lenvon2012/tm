
package job;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import models.item.ItemPlay;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;
import bustbapi.ItemApi;
import bustbapi.ItemApi.ItemGet;

import com.taobao.api.domain.Item;

import dao.UserDao;
import dao.item.ItemDao;

@Every("15s")
public class ItemUpdateMessages extends Job {
    private static final Logger log = LoggerFactory.getLogger(ItemUpdateMessages.class);

    public static final String TAG = "ItemUpdateMessages";

    static Queue<UpdateMsg> queue = new ConcurrentLinkedQueue<UpdateMsg>();

    @Override
    public void doJob() {

        UpdateMsg msg = null;
        while ((msg = queue.poll()) != null) {

            itemUpdate(msg);
        }
    }

    public static void addMsg(UpdateMsg msg) {
        queue.add(msg);
    }

    public void itemUpdate(UpdateMsg msg) {
        JSONObject content;
        try {
            content = new JSONObject(msg.getMsgBody());
            Long userId = msg.getUserId();
            String changeField = getChangeFields(content);
            /**
             * "desc,num,sku"
             */
            Long numIid = content.getLong("num_iid");
            String userNick = content.getString("nick");

            // 宝贝标题发生改变
            if (changeField.contains("title")) {
                String title = content.getString("title");

                if (!StringUtils.isEmpty(title) && !StringUtils.isEmpty(userNick) && numIid != null) {
                    boolean updateTitleSuccess = updateTitle(userId, numIid, title);
                    if (!updateTitleSuccess) {
                        log.info("update title for userNick[" + userNick + "] and numIid[" +
                                numIid + "] failed");
                    }
                }

            } else if (changeField.contains("price")) {
                User user = UserDao.findByUserNick(userNick);
                if (user == null) {
                    return;
                }

                Item rawItem = new ItemGet(user, numIid, true).call();
                if (rawItem == null) {
                    return;
                }
                new ItemPlay(user.getId(), rawItem).jdbcSave();
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            log.warn(e.getMessage(), e);
        }

    }

    public static String getChangeFields(JSONObject content) throws JSONException {
        return content.getString("changed_fields");
    }

    public static boolean updateTitle(Long userId, Long numIid, String title) {

        if (userId == null || StringUtils.isEmpty(title) || numIid == null) {
            return false;
        }

        User user = UserDao.findById(userId);

        if (user == null) {
            return false;
        }

        Item item = new ItemApi.ItemGet(user, numIid, true).call();
        if (item == null) {
            return false;
        }
        item.setTitle(title);

        ItemPlay exist = ItemDao.findByNumIid(user.getId(), numIid);
        if (exist == null) {
            return true;
        }

        /*
         * 
         */
        if (StringUtils.equals(exist.getTitle(), title)) {
            return true;
        }

        exist.updateWithTitleAndScore(item, title);
        return true;
    }

    public static class UpdateMsg {
        Long userId;

        String msgBody;

        public UpdateMsg() {
            super();
        }

        public UpdateMsg(Long userId, String msgBody) {
            super();
            this.userId = userId;
            this.msgBody = msgBody;
        }

        public String getMsgBody() {
            return msgBody;
        }

        public void setMsgBody(String msgBody) {
            this.msgBody = msgBody;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        @Override
        public String toString() {
            return "UpdateMsg [userId=" + userId + ", msgBody=" + msgBody + "]";
        }

    }
}
