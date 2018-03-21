
package actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import jdp.ApiJdpAdapter;
import models.CategoryProps;
import models.UserDiag;
import models.item.ItemCatHotProps;
import models.item.ItemPlay;
import models.user.User;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import titleDiag.DiagResult;
import titleDiag.TitleDiagnose;
import actions.task.TaskProgressAction.AutoTitleProgressAction;
import autotitle.AutoTitleOption.BatchPageOption;
import bustbapi.ItemApi.ItemTitleUpdater;
import bustbapi.TMTradeApi.ShopBaseTradeInfo;
import bustbapi.TmallItem.TmallItemTitleUpdater;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.ItemThumb;
import com.ciaosir.client.utils.ChsCharsUtil;
import com.ciaosir.client.utils.CiaoStringUtil;
import com.ciaosir.client.utils.DateUtil;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.NumberUtil;
import com.ciaosir.commons.ClientException;
import com.taobao.api.ApiException;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.ItemProp;

import configs.TMConfigs;
import controllers.Diag;
import dao.item.ItemDao;

public class DiagAction {

    private static final Logger log = LoggerFactory.getLogger(DiagAction.class);

    public static final String TAG = "DiagAction";

    public static int diagScore(Item item, int defaultValue) {
        DiagResult diag = diag(item);
        return diag == null ? defaultValue : diag.getScore();
    }

    public static abstract class TitleOperEachCaller implements Callable<List<BatchResultMsg>> {

        protected List<ItemPlay> items;

        protected User user;

        private Long taskId;

        static String MSG_TITLE_TOO_LONG = "标题长度超过限制";

        public TitleOperEachCaller(User user, List<ItemPlay> items) {
            this.items = items;
            this.user = user;
        }

        public TitleOperEachCaller(User user, List<ItemPlay> items, Long taskId) {
            this.items = items;
            this.user = user;
            this.taskId = taskId;
        }

        public List<BatchResultMsg> call() {

            if (CommonUtils.isEmpty(items)) {
                return ListUtils.EMPTY_LIST;
            }

            List<BatchResultMsg> res = new ArrayList<BatchResultMsg>();

            List<FutureTask<BatchResultMsg>> tasks = new ArrayList<FutureTask<BatchResultMsg>>();
            for (final ItemPlay item : items) {
                tasks.add(TMConfigs.getBatchResultMsgPool().submit(new Callable<BatchResultMsg>() {
                    @Override
                    public BatchResultMsg call() throws Exception {

                        return doForEach(item);
                    }
                }));
            }

            try {
                for (FutureTask<BatchResultMsg> task : tasks) {
                    BatchResultMsg batchResultMsg = task.get();
                    if (batchResultMsg != null) {
                        res.add(batchResultMsg);
                    }
                    //设置异步任务的进度
                    if (taskId != null && taskId > 0L) {
                        AutoTitleProgressAction.stepOneTaskProgress(taskId);
                    }
                }
            } catch (InterruptedException e) {
                log.warn(e.getMessage(), e);

            } catch (ExecutionException e) {
                log.warn(e.getMessage(), e);
            }

            return res;
        }

        public abstract BatchResultMsg doForEach(ItemPlay item);

    }

    @JsonAutoDetect
    public static class BatchResultMsg {

        @JsonProperty
        boolean ok = true;

        @JsonProperty
        Long numIid;

        @JsonProperty
        String title;

        @JsonProperty
        String msg;

        @JsonProperty
        String picPath;

        @JsonProperty
        String originTitle;

        @JsonProperty
        String newTitle;

        public BatchResultMsg(boolean ok, Long numIid, String title, String msg, String picPath) {
            super();
            this.ok = ok;
            this.numIid = numIid;
            this.title = title;
            this.msg = msg;
            this.picPath = picPath;
        }

        public BatchResultMsg(boolean success, String msg, Long numIid) {
            this.ok = success;
            this.msg = msg;
            this.numIid = numIid;
        }

        public BatchResultMsg(boolean success, String msg, ItemPlay item) {
            this.ok = success;
            this.msg = msg;

            if (item == null) {
                return;
            }

            this.numIid = item.getNumIid();
            this.title = item.getTitle();
            this.picPath = item.getPicURL();
            this.originTitle = item.getTitle();
        }

        public BatchResultMsg(boolean success, String msg, ItemPlay item, String newTitle) {
            this.ok = success;
            this.msg = msg;
            this.newTitle = newTitle;

            if (item == null) {
                return;
            }

            this.numIid = item.getNumIid();
            this.title = item.getTitle();
            this.picPath = item.getPicURL();
            this.originTitle = item.getTitle();
        }

        public BatchResultMsg(Long numIid, String title, String msg, String picPath) {
            super();
            this.numIid = numIid;
            this.title = title;
            this.msg = msg;
            this.picPath = picPath;
        }

        public boolean isOk() {
            return ok;
        }

        public void setOk(boolean ok) {
            this.ok = ok;
        }

        public Long getNumIid() {
            return numIid;
        }

        public void setNumIid(Long numIid) {
            this.numIid = numIid;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getPicPath() {
            return picPath;
        }

        public void setPicPath(String picPath) {
            this.picPath = picPath;
        }

        public String getOriginTitle() {
            return originTitle;
        }

        public void setOriginTitle(String originTitle) {
            this.originTitle = originTitle;
        }

        public String getNewTitle() {
            return newTitle;
        }

        public void setNewTitle(String newTitle) {
            this.newTitle = newTitle;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

    }

    public static class BatchInserter extends TitleOperEachCaller {
        String target;

        int offset;

        /**
         * If the index is smaller than 0, we know it means append to the tail...
         * @param user
         * @param items
         * @param target
         * @param offset
         */
        public BatchInserter(User user, List<ItemPlay> items, String target, int offset) {
            super(user, items);
            this.target = target;
            this.offset = offset;
        }

        @Override
        public BatchResultMsg doForEach(ItemPlay item) {

            if (StringUtils.isEmpty(target)) {
                return null;
            }

            String currTitle = item.getTitle();
            if (StringUtils.isEmpty(currTitle)) {
                return null;
            }

            int srcLength = ChsCharsUtil.length(currTitle);
            int targetLength = ChsCharsUtil.length(target);

            if (srcLength + targetLength > 60) {
                return new BatchResultMsg(false, MSG_TITLE_TOO_LONG, item);
            }
            int tmpOffset;
            if (offset < 0) {
                tmpOffset = currTitle.length();
            }
            else {
                tmpOffset = 0;
            }
            // offset =  currTitle.length();
            StringBuilder sb = new StringBuilder(currTitle);
            sb.insert(tmpOffset, target);
            String newTitle = sb.toString();

            return executeUpdate(user, item, newTitle);
        }

    }

    public static class BatchReplacer extends TitleOperEachCaller {
        Map<String, String> newTitleMap;

        Boolean updateDB = false;
        
        BatchPageOption opt;

        /**
         * @param user
         * @param items
         * @param newTitleMap
         */
        public BatchReplacer(User user, List<ItemPlay> items, Map<String, String> newTitleMap, Long taskId) {
            super(user, items, taskId);
            this.newTitleMap = newTitleMap;

        }
        
        public BatchReplacer(User user, List<ItemPlay> items, Map<String, String> newTitleMap,
        		Long taskId, Boolean updateDB) {
            super(user, items, taskId);
            this.newTitleMap = newTitleMap;
            this.updateDB = updateDB;

        }

        public BatchReplacer(User user, List<ItemPlay> items, Map<String, String> newTitleMap2, Long taskId,
                BatchPageOption opt) {
            super(user, items, taskId);
            this.newTitleMap = newTitleMap2;
            this.opt = opt;
        }

        @Override
        public BatchResultMsg doForEach(ItemPlay item) {

            if (newTitleMap.size() == 0) {
                return null;
            }
            if (item == null) {
                return null;
            }

            String currTitle = item.getTitle();
            if (StringUtils.isBlank(currTitle)) {
                return null;
            }

            String newTitle = newTitleMap.get(item.getId().toString());
            if (StringUtils.isEmpty(newTitle)) {
                return new BatchResultMsg(true, StringUtils.EMPTY, item, currTitle);
            }

            if (newTitle.length() > 60) {
                return new BatchResultMsg(false, MSG_TITLE_TOO_LONG, item);
            }
//            log.info("new title : [" + newTitle + " ] with  current :" + currTitle);
            //error test
            //newTitle = newTitle.concat("nidshahdsja");
            BatchResultMsg msg = executeUpdate(user, item, newTitle);
            if(updateDB) {
            	if(msg.isOk()) {
            		item.setTitle(newTitle);
            		item.jdbcSave();
            	}
            }
            return msg;
        }
    }

    public static class BatchReplaceCaller extends TitleOperEachCaller {

        String target;

        String src;

        public BatchReplaceCaller(User user, List<ItemPlay> items, String src, String target) {

            super(user, items);

            this.target = target;

            this.src = src;
        }

        @Override
        public BatchResultMsg doForEach(ItemPlay item) {

            //  if (StringUtils.isEmpty(target)) {
            //      return null;
            //  }

            String currTitle = item.getTitle();
            if (StringUtils.isEmpty(currTitle)) {
                return null;
            }

            String newTitle = StringUtils.replace(currTitle, src, target);
            //   if (StringUtils.length(newTitle) == currTitle.length()) {
            //       return null;
            //   }
            if (newTitle.equals(currTitle)) {
                return null;
            }
            if (ChsCharsUtil.length(newTitle) > 60) {
                return new BatchResultMsg(false, MSG_TITLE_TOO_LONG, item);
            }

            return executeUpdate(user, item, newTitle);
        }

    }

    public static void refreshByUpdateMsgs(User user, List<BatchResultMsg> msgs) {
        if (CommonUtils.isEmpty(msgs)) {
            return;
        }

        for (BatchResultMsg msg : msgs) {
            if (msg == null) {
                continue;
            }

            if (!msg.isOk()) {
                continue;
            }

            ItemDao.setTitle(user.getId(), msg.getNumIid(), msg.getNewTitle());
        }
    }

    public static void computeTotalScore(Map<Integer, Integer> res, Map<Integer, Integer> sreadMap) {
        for (Entry<Integer, Integer> entry : res.entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();
            Integer sreadKey = 0;
            if (key >= 85) {//85以上是优秀
                sreadKey = 5;
            } else if (key >= 70) {//70以上是良好
                sreadKey = 4;
            } else if (key >= 60) {
                sreadKey = 3;
            } else {
                sreadKey = 12;
            }
            Integer sreadValue = sreadMap.get(sreadKey);
            if (sreadValue == null)
                sreadValue = 0;
            sreadValue += value;
            sreadMap.put(sreadKey, sreadValue);
        }
    }

    static TitleDiagnose instance = TitleDiagnose.getInstance();

    public static DiagResult doDiag(User user, long numIid, String title) throws ClientException {
        Item call = ApiJdpAdapter.get(user).findItem(user, numIid);
        ItemPlay itemPlay = ItemDao.findByNumIid(user.getId(), numIid);
        int tradeCount = -1;
        if (itemPlay != null) {
            tradeCount = itemPlay.getSalesCount();
        }
        String prop = call == null ? null : call.getPropsName();
        title = StringUtils.isEmpty(title) ? (call  == null ? itemPlay.getTitle() : call.getTitle()) : title;
        log.info(">>>>>>>>>> start to diag: " + numIid);
        DiagResult doWord = instance.doWord(numIid, CommonUtils.String2Double(call.getPrice()),
                title, prop, call.getPicUrl(), tradeCount, itemPlay.getDeListTime(), itemPlay.getCid());
        doWord.setCid(call.getCid());
        doWord.setItemTitleCatPv(title, call.getCid());
        log.info("<<<<<<<<<<< end to diag: " + numIid);
        return doWord;
    }

    public static DiagResult doDiag(String title) throws ClientException {

        log.info(">>>>>>>>>> start to diag: " + title);
        DiagResult doWord = instance.doWord(title);
        log.info("<<<<<<<<<<< end to diag: " + title);
        return doWord;
    }

    public static DiagResult doDiag(double price, String title, String props, String picPath) throws ClientException {

        log.info(">>>>>>>>>> start to diag: " + title);
        DiagResult doWord = instance.doWord(0L, price, title, props, picPath, -1, 0L, 0L);
        log.info("<<<<<<<<<<< end to diag: " + title);
        return doWord;
    }

    public static DiagResult doDiag(User user, ItemPlay itemPlay, String title) throws ClientException {
        title = StringUtils.isEmpty(title) ? itemPlay.getTitle() : title;
        Item call = ApiJdpAdapter.get(user).findItem(user, itemPlay.getNumIid());
        String prop = call == null ? null : call.getPropsName();
//        log.info("[single prop :]" + prop);
        long delistTime = itemPlay.getDeListTime();

        DiagResult doWord = instance.doWord(itemPlay.getNumIid(), itemPlay.getPrice(), title, prop,
                itemPlay.getPicURL(), itemPlay.getSalesCount(), delistTime, itemPlay.getCid());
        doWord.setCid(itemPlay.getCid());

        return doWord;
    }

    public static DiagResult doDiag(User user, Item item, String title, Integer tradeCount) throws ClientException {
//        if(item == null){
//            return null;
//        }
        if (item == null) {
            return null;
        }

        title = StringUtils.isEmpty(title) ? item.getTitle() : title;
//        if (item.getPropsName() == null) {
//            log.info("[get prop :]" + user);
//            item = new ItemGet(user, item.getNumIid(), true).call();
//        }

        double price = NumberUtil.parserDouble(item.getPrice(), 0.0d);
        String prop = item == null ? null : item.getPropsName();
        long delistTime = item.getDelistTime() == null ? 0L : item.getDelistTime().getTime();
        DiagResult doWord = instance.doWord(item.getNumIid(), price, title, prop, item.getPicUrl(),
                tradeCount, delistTime, item.getCid());
        doWord.setCid(item.getCid());
        doWord.setItemTitleCatPv(title, item.getCid());
        return doWord;
    }

    public static String cidPropsPre = "cidPropsPre_";
    public static String getCidProps(Long cid) {
    	if(cid == null || cid <= 0) {
    		return StringUtils.EMPTY;
    	} 
    	
    	String cidProps = (String) Cache.get(cidPropsPre + cid);
    	if(!StringUtils.isEmpty(cidProps)) {
    		return cidProps;
    	}
    	CategoryProps cat = CategoryProps.findByCid(cid);
    	if(cat != null && cat.getCreated() > (System.currentTimeMillis() - DateUtil.THIRTY_DAYS)) {
    		Cache.set(cidPropsPre + cid, cat.getProps(), "24h");
    		return cat.getProps();
    	}

    	try {
			List<ItemProp> propList = ItemCatHotProps.fetchItemProps(cid);
			if(CommonUtils.isEmpty(propList)) {
				return StringUtils.EMPTY;
			}
			List<String> propNames = new ArrayList<String>();
			for(ItemProp prop : propList) {
				propNames.add(prop.getName());
			}
			Cache.set(cidPropsPre + cid, StringUtils.join(propNames, ","), "24h");
			new CategoryProps(cid, System.currentTimeMillis(), StringUtils.join(propNames, ",")).jdbcSave();
			return propNames.toString();
		} catch (ApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return StringUtils.EMPTY;
    }
    
    public static List<String> parseNames(String propNames) {
        if (StringUtils.isEmpty(propNames)) {
            return ListUtils.EMPTY_LIST;
        }

        List<String> res = new ArrayList<String>();
        String[] segments = StringUtils.split(propNames, ';');
        if (ArrayUtils.isEmpty(segments)) {
            return ListUtils.EMPTY_LIST;
        }

        for (String segment : segments) {
            if (StringUtils.isBlank(segment)) {
                continue;
            }

//            log.info("Found Segment:" + segment);
            segment = CiaoStringUtil.lazyReplace(segment, "#scln#", ";");

            String[] split = StringUtils.split(segment, ':');
            if (split.length < 4) {
                continue;
            }

            String pText = split[2];
            String vText = split[3];

            pText = CiaoStringUtil.lazyReplace(pText, "#cln#", ":");
            vText = CiaoStringUtil.lazyReplace(vText, "#cln#", ":");
            if(!res.contains(pText)) {
            	res.add(pText);
            }
            
        }
        return res;
    }
    
    public static DiagResult diag(Item item) {

        double price = NumberUtil.parserDouble(item.getPrice(), 0.0d);
        try {
            long delistTime = item.getDelistTime() == null ? 0L : item.getDelistTime().getTime();
            DiagResult doWord = instance.doWord(item.getNumIid(), price, item.getTitle(), item.getPropsName(),
                    item.getPicUrl(), -1, delistTime, item.getCid());
            if (doWord.getScore() < 40) {
                doWord.setScore(40);
            }

            return doWord;
        } catch (ClientException e) {
            log.warn(e.getMessage(), e);
            return null;
        }
    }

    public static String buildUserShopDiag(User user, ShopBaseTradeInfo basetrade, List<ItemThumb> list) {
        String res = (String) Cache.get(Diag.TAG + user.getId());
        if (res != null) {
            return res;
        }

        UserDiag diag = UserDiag.findByUserId(user.getId());
        if (diag == null) {
            diag = new UserDiag.ComputeUserDelistTime(user).doJobWithResult();
        }

        diag.buildReport(basetrade, list);
        diag.jdbcSave();
        String diagJson = JsonUtil.getJson(diag);
        Cache.set(Diag.TAG + user.getId(), diagJson, "20min");
        return diagJson;
    }

    public static BatchResultMsg executeUpdate(User user, ItemPlay item, String newTitle) {
        String sid = user.getSessionKey();
//        log.warn("Current Class :" + this.getClass());
//        log.warn("Old Title <<:" + item.getTitle());
//        log.warn("New Title >>:" + newTitle);

        if (!TMConfigs.Operate.enableRealTitleModifier) {
            log.info("[origin title:]" + item.getTitle() + " and new title :[" + newTitle + "]");
            return new BatchResultMsg(true, null, item, newTitle);
        }
        String errorMsg;
        if(user.isTmall()){
            TmallItemTitleUpdater update = new TmallItemTitleUpdater(sid, item.getNumIid(), newTitle);
            update.call();
             errorMsg = update.getErrorMsg();
        }else {
            ItemTitleUpdater update = new ItemTitleUpdater(sid, item.getNumIid(), newTitle);
            update.call();
             errorMsg = update.getErrorMsg();
        }
        BatchResultMsg msg = null;
        if (StringUtils.isEmpty(errorMsg)) {
            msg = new BatchResultMsg(true, null, item, newTitle);
        } else {
            msg = new BatchResultMsg(false, errorMsg, item);
        }

        return msg;
    }

}
