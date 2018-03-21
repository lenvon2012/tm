
package bustbapi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jdp.ApiJdpAdapter;
import jdp.ApiJdpAdapter.JdpApiImpl;
import jdp.ApiJdpAdapter.OriginApiImpl;
import jdp.JdpModel.JdpItemModel;
import job.diagjob.PropDiagJob;
import models.item.ItemPlay;
import models.op.RawId;
import models.updatetimestamp.updates.ItemUpdateTs;
import models.user.User;

import org.apache.commons.collections.SetUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transaction.DBBuilder.DataSrc;
import bustbapi.ItemApi.ItemsInventoryCount;
import bustbapi.ItemApi.ItemsOnsaleCount;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;
import com.google.gson.Gson;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.JdpUser;
import com.taobao.api.domain.Task;
import com.taobao.api.request.JushitaJdpUserAddRequest;
import com.taobao.api.request.JushitaJdpUserDeleteRequest;
import com.taobao.api.request.JushitaJdpUsersGetRequest;
import com.taobao.api.request.TopatsJushitaJdpDatadeleteRequest;
import com.taobao.api.response.JushitaJdpUserAddResponse;
import com.taobao.api.response.JushitaJdpUserDeleteResponse;
import com.taobao.api.response.JushitaJdpUsersGetResponse;
import com.taobao.api.response.TopatsJushitaJdpDatadeleteResponse;

import configs.TMConfigs.Rds;
import controllers.APIConfig;
import dao.item.ItemDao;

/**
 * 需要另外搞一个橱窗宝贝的cache  db,结合jdp来做橱窗状态的监控。。。
 * TODO 获取所有橱窗中的宝贝
 * TODO 获取所有在售宝贝,并且还要按照下架时间排序
 * TODO Total Count需要每天更新一次缓存，然后实时去扫当前橱窗状态
 * TODO 在取消之前，需要查询宝贝是否还在推荐中
 * TODO 在推荐之前，需要确认宝贝是否已经被推荐了
 * TODO 还得准备对店铺的总数进行一个监控,这里可能需要一个缓存，一天更新一次
 * @author zrb
 *
 */
public class JDPApi {

    private static final Logger log = LoggerFactory.getLogger(JDPApi.class);

    public static final String TAG = "JDPApi";

    static JDPApi _instance = new JDPApi();

//    public static DBDispatcher dp = new DBDispatcher(DataSrc.QUOTA, );

    static DataSrc src = DataSrc.JDP;

    public static JDPApi get() {
        return _instance;
    }

    public Boolean startUserJuShiTaListener(User user) {
        return new JuShiTaAddUserApi(user).call();
    }

//
//    public Boolean stopUserJuShiTaListener(User user) {
//        return new JuShiTaDeleteUserApi(user).call();
//    }

    public enum JuDataType {
        tb_trade, tb_item;
    }

    public static class JuShiTaCancelApi extends
            TBApi<JushitaJdpUserDeleteRequest, JushitaJdpUserDeleteResponse, Boolean> {

        private User user;

        public JuShiTaCancelApi(User user) {
            super();

            this.user = user;
            this.retryTime = 3;
        }

        @Override
        public JushitaJdpUserDeleteRequest prepareRequest() {
            JushitaJdpUserDeleteRequest req = new JushitaJdpUserDeleteRequest();
            if (user == null) {
                return null;
            }
            req.setNick(user.getUserNick());
            req.setUserId(user.getId());

            return req;
        }

        @Override
        public Boolean validResponse(JushitaJdpUserDeleteResponse resp) {
            ErrorHandler.validTaoBaoResp(resp);
            if (!resp.isSuccess()) {
                return Boolean.FALSE;
            }
            return resp.getIsSuccess();
        }

        @Override
        public Boolean applyResult(Boolean res) {
            return res;
        }

    }

    public static class JuShiTaDataDeleteApi extends
            TBApi<TopatsJushitaJdpDatadeleteRequest, TopatsJushitaJdpDatadeleteResponse, Task> {

        private long start;

        private long end;

        User user;

        JuDataType dType = JuDataType.tb_trade;

        /**
         * 默认删除最近七天之前的订单数据
         * @param user
         */
        public JuShiTaDataDeleteApi() {
            super();
            end = DateUtil.formCurrDate() - DateUtil.WEEK_MILLIS;
            start = end - 2 * DateUtil.THIRTY_DAYS;
        }

        public JuShiTaDataDeleteApi(User user, JuDataType dType) {
            super(user == null ? null : user.getSessionKey());
            this.start = 0L;
            this.end = System.currentTimeMillis() - DateUtil.TRIPPLE_DAY_MILLIS_SPAN;
            this.dType = dType;
            this.user = user;
        }

        public JuShiTaDataDeleteApi(User user, long start, long end, JuDataType dType) {
            super(user == null ? null : user.getSessionKey());
            this.start = start;
            this.end = end;
            this.dType = dType;
            this.user = user;
        }

        @Override
        public TopatsJushitaJdpDatadeleteRequest prepareRequest() {
            TopatsJushitaJdpDatadeleteRequest req = new TopatsJushitaJdpDatadeleteRequest();

            req.setSyncType(dType.name());
//            if (start > 0L) {
//            }
            req.setStartModified(new Date(start));
            req.setEndModified(new Date(end));
//            if (end > 0L) {
//            }
            if (user != null) {
                req.setUserNick(user.getUserNick());
            }

            log.info("[for req:]" + new Gson().toJson(req));

            return req;
        }

        @Override
        public Task validResponse(TopatsJushitaJdpDatadeleteResponse resp) {
            ErrorHandler.validTaoBaoResp(resp);
            if (!resp.isSuccess()) {
                return null;
            }
            return resp.getTask();
        }

        @Override
        public Task applyResult(Task res) {
            return res;
        }

    }

	public static final List<String> LIMIT_USER_NICK = new ArrayList<String>();
	
	static {
		LIMIT_USER_NICK.add("害你心肝跳三下");
		LIMIT_USER_NICK.add("红豆冰2008");
		LIMIT_USER_NICK.add("雷州杨家");
		LIMIT_USER_NICK.add("qiaoaiai520");
		LIMIT_USER_NICK.add("ruanjialiang123");
		LIMIT_USER_NICK.add("liuminggang151");
		LIMIT_USER_NICK.add("姚星米");
		LIMIT_USER_NICK.add("牛仔很忙0921");
		LIMIT_USER_NICK.add("tb9215704_00");
		LIMIT_USER_NICK.add("高品质低价格88");
		LIMIT_USER_NICK.add("nan88990");
		LIMIT_USER_NICK.add("0668猩猩潮牌");
		LIMIT_USER_NICK.add("栩栩52055");
	}
		
    public static class JuShiTaAddUserApi extends TBApi<JushitaJdpUserAddRequest, JushitaJdpUserAddResponse, Boolean> {

        private User user;

        public JuShiTaAddUserApi(User user) {
            super(user.getSessionKey());
            this.user = user;
        }

        @Override
        public JushitaJdpUserAddRequest prepareRequest() {
        	for (String nick : LIMIT_USER_NICK) {
				if(nick.equalsIgnoreCase(user.getUserNick())) {
					return null;
				}
			}
            JushitaJdpUserAddRequest request = new JushitaJdpUserAddRequest();
            request.setRdsName(APIConfig.get().getRdsName());
            return request;
        }

        @Override
        public Boolean validResponse(JushitaJdpUserAddResponse resp) {
            ErrorHandler.validTaoBaoResp(resp);
            if ("指定的用户不存在店铺".equals(resp.getSubMsg()) || "session-expired".equals(resp.getSubCode())) {
                user.setVaild(false);
                user.jdbcSave();
            }

            if (!resp.isSuccess()) {
                return Boolean.FALSE;
            }
            return resp.getIsSuccess();
        }

        @Override
        public Boolean applyResult(Boolean res) {
            return res;
        }
    }

    public static class JuShiTaGetUsers extends
            TBApi<JushitaJdpUsersGetRequest, JushitaJdpUsersGetResponse, Set<String>> {

        Set<String> totalIds = new HashSet<String>();

        long pageNo = 1L;

        Long pageSize = 2048L;

        boolean hasInit = false;

        String filterRdsName = null;

        User user = null;

        public JuShiTaGetUsers() {
            super();
        }

        public JuShiTaGetUsers(User user) {
            super(user.getSessionKey());
            this.user = user;
        }

        public JuShiTaGetUsers(String filterRdsName) {
            super();
            this.filterRdsName = filterRdsName;
        }

        @Override
        public JushitaJdpUsersGetRequest prepareRequest() {
            JushitaJdpUsersGetRequest req = new JushitaJdpUsersGetRequest();
            req.setPageSize(pageSize);
            req.setPageNo(pageNo++);
            if (user != null) {
                req.setUserId(user.getId());
            }

            return req;
        }

        @Override
        public Set<String> validResponse(JushitaJdpUsersGetResponse resp) {
            ErrorHandler.validTaoBaoResp(resp);
            if (!hasInit) {
                long totalResult = resp.getTotalResults();
                this.iteratorTime = (int) CommonUtils.calculatePageCount(totalResult, pageSize) - 1;
                this.hasInit = true;
            }

            List<JdpUser> list = resp.getUsers();
            if (CommonUtils.isEmpty(list)) {
                return SetUtils.EMPTY_SET;
            }

            Set<String> ids = new HashSet<String>();
            for (JdpUser jdpUser : list) {
                if (!StringUtils.isEmpty(filterRdsName)) {
                    if (StringUtils.equals(jdpUser.getRdsName(), filterRdsName)) {
                        // I need this
                        ids.add(jdpUser.getUserNick());
                        continue;
                    }else{
                        continue;
                    }
                }

                ids.add(jdpUser.getUserNick());
            }

            return ids;
        }

        @Override
        public Set<String> applyResult(Set<String> res) {
            if (res == null) {
                return totalIds;
            }

            totalIds.addAll(res);
            return totalIds;
        }

    }

    public boolean fixUserListernerIfNeeded(User user) {
        if (!Rds.enableJdpPush) {
            return false;
        }

        Set<String> set = new JuShiTaGetUsers(user).call();
        if (CommonUtils.isEmpty(set)) {
            // TODO Why no on line item user for this ???
            Long count = new ItemsOnsaleCount(user.getSessionKey(), null, null).call();
            if (count != null && count > 0L) {
                new JuShiTaAddUserApi(user).call();
                new PropDiagJob(user, true).doJob();
                return true;
            }
        }

        return false;
    }

    public boolean isItemNumMatch(User user) {

        Long apiItemInventoryNum = new ItemsInventoryCount(user, null, null).call();
        Long apiItemOnSaleTotalNum = new ItemApi.ItemsOnsaleCount(user, null, null).call();

        int onSaleCount = JdpItemModel.countOnSaleItem(user);
        int inventoryCount = JdpItemModel.countInventoryItems(user);

        return apiItemInventoryNum != null && apiItemInventoryNum.longValue() == inventoryCount
                && apiItemOnSaleTotalNum != null && apiItemOnSaleTotalNum.longValue() == onSaleCount;
    }

    public boolean isOnSaleItemDBAPIMatch(User user) {
        Long apiItemOnSaleTotalNum = new ItemApi.ItemsOnsaleCount(user, null, null).call();
        long onSaleDbItem = ItemDao.countOnsaleItemByuserId(user.getId());
        return apiItemOnSaleTotalNum != null && onSaleDbItem == apiItemOnSaleTotalNum.longValue();
    }

    public static class JdpItemStatus implements Serializable {

        private static final long serialVersionUID = 1L;

        User user;

        long onlineNum;

        long inStockNum;

        int apiItemInventoryNum;

        int apiItemOnSaleNum;

        int jdpOnSaleCount;

        int jdpInventoryCount;

        ItemUpdateTs updateTs = null;

        long jdpStart = 0L;

        long jdpEnd = 0L;

        boolean isJdpListening = false;

        int onSaleChangeNumwithInJdp;

        int inventoryChangeNumwithInJdp;

        Set<Long> dbIds = SetUtils.EMPTY_SET;

        Set<Long> apiIds = SetUtils.EMPTY_SET;

        Set<Long> allJdpIds = SetUtils.EMPTY_SET;

        Set<Long> allApiIds = SetUtils.EMPTY_SET;

        Map<Long, Long> apiDelistTimeMap = new HashMap<Long, Long>();

        public boolean isOnSaleSynced() {
            return apiItemOnSaleNum == jdpOnSaleCount;
        }

        public JdpItemStatus() {

        }

        Long userId;

        Set<Long> notMatchedDelistTime = new HashSet<Long>();

        public JdpItemStatus(User user) {
            this.user = user;
            this.userId = user.getId();
            Long temp = null;
            this.notMatchedDelistTime.clear();

            onlineNum = ItemDao.countOnsaleItemByuserId(userId);
            inStockNum = ItemDao.countInStockItemByuserId(userId);

            temp = new ItemsOnsaleCount(user, null, null).call();
            apiItemOnSaleNum = temp == null ? -1 : temp.intValue();
            temp = new ItemsInventoryCount(user, null, null).call();
            apiItemInventoryNum = temp == null ? -1 : temp.intValue();

            jdpOnSaleCount = JdpItemModel.countOnSaleItem(user);
            jdpInventoryCount = JdpItemModel.countInventoryItems(user);

            updateTs = ItemUpdateTs.fetchByUser(user.getId());

            jdpStart = JdpItemModel.minModified(user);
            jdpEnd = JdpItemModel.maxModified(user);

            isJdpListening = !CommonUtils.isEmpty(new JuShiTaGetUsers(user).call());

            temp = new ItemsOnsaleCount(user, jdpStart, jdpEnd).call();
            onSaleChangeNumwithInJdp = temp == null ? -1 : temp.intValue();

            temp = new ItemsInventoryCount(user, jdpStart, jdpEnd).call();
            inventoryChangeNumwithInJdp = temp == null ? -1 : temp.intValue();

            int maxNum = 30;
            List<Item> jdpItems = JdpApiImpl.get().findRecentDownItems(user, maxNum);
            dbIds = ShowWindowApi.toNumIids(jdpItems);
//            log.info("[back db <" + dbIds.size() + "> extrfa :]" + dbIds);
            List<Item> apiItems = OriginApiImpl.get().findRecentDownItems(user, maxNum);
            for (Item item : apiItems) {
                Date date = item.getDelistTime();
                apiDelistTimeMap.put(item.getNumIid(), date == null ? 0L : date.getTime());
            }
            apiIds = ShowWindowApi.toNumIids(apiItems);
//            log.info("[back item  extrfa :]" + apiIds);
            Iterator<Long> it = dbIds.iterator();
            while (it.hasNext()) {
                Long jId = it.next();
                if (apiIds.contains(jId)) {
                    apiIds.remove(jId);
                    it.remove();
                }
            }

            allJdpIds = JdpItemModel.allNumIids(user);
            allApiIds = ItemApi.allNumIids(user);

            Iterator<Long> jdpIter = allJdpIds.iterator();
            while (jdpIter.hasNext()) {
                Long id = jdpIter.next();
                if (allApiIds.contains(id)) {
                    jdpIter.remove();
                }
            }
            apiOnWIndowNumIids = ShowWindowApi.toNumIids(OriginApiImpl.get().findCurrOnWindowItems(user));
            apiOnWindowItemNum = apiOnWIndowNumIids.size();
            jdpOnWIndowNumIids = ShowWindowApi.toNumIids(JdpApiImpl.get().findCurrOnWindowItems(user));
            jdpOnWindowItemNum = jdpOnWIndowNumIids.size();

            apiOnWIndowNumIids.removeAll(new ArrayList(jdpOnWIndowNumIids));
            jdpOnWIndowNumIids.removeAll(new ArrayList(apiOnWIndowNumIids));

        }

        int apiOnWindowItemNum = 0;

        int jdpOnWindowItemNum = 0;

        Set<Long> apiOnWIndowNumIids;

        Set<Long> jdpOnWIndowNumIids;

        public boolean isJdpStatusMatch() {
            return apiItemOnSaleNum == jdpOnSaleCount && apiItemInventoryNum == jdpInventoryCount;
        }

        public StringBuilder toStrBuilder() {
            StringBuilder sb = new StringBuilder();
            sb.append("\n");
            sb.append(" >>>>>  enable jdp api:" + ApiJdpAdapter.enableJdp(user));
            sb.append(" \t");
            sb.append(user + "\n");
            sb.append(" api extra on window numiids :[" + apiOnWIndowNumIids + "] -- [" + jdpOnWIndowNumIids + "]\n");
            sb.append(" rawids :" + RawId.hasId(user.getId()) + "\n");
            sb.append(" db  online [" + onlineNum + "]\tinstock num:[" + inStockNum + "]\n");
            sb.append(" api online [" + apiItemOnSaleNum + "]\tinstock num:[" + apiItemInventoryNum + "]\n");
            sb.append(" jdp online [" + jdpOnSaleCount + "]\tjdp instock num:[" + jdpInventoryCount + "]\n");
            sb.append(" api onwindow [" + apiOnWindowItemNum + "]\t   jdp on window num:[" + jdpOnWindowItemNum + "]\n");

            sb.append(" item update ts : " + updateTs);

            sb.append("\n current user jdp listener: " + isJdpListening);
            sb.append("\n jdp start :" + DateUtil.formDateForLog(jdpStart) + "  ---  jdp end "
                    + DateUtil.formDateForLog(jdpEnd));
            sb.append('\n');
            sb.append("online item changes: [" + onSaleChangeNumwithInJdp + "] and inventory changes :["
                    + inventoryChangeNumwithInJdp + "]");
            sb.append('\n');

            List<String> msgs = new ArrayList<String>();
            for (Long id : dbIds) {
                ItemPlay item = ItemDao.findByNumIid(user.getId(), id);
                Item remoteTemp = new ItemApi.ItemGet(user, item.getNumIid()).call();
                long remoteDelistTime = 0L;
                if (remoteTemp == null) {
                    remoteDelistTime = 0L;
                } else {
                    remoteDelistTime = remoteTemp.getDelistTime() == null ? 0L : remoteTemp.getDelistTime().getTime();
                }
                msgs.add(" [" + id + "]:" + DateUtil.formDateForLog(item.getDeListTime()) + " --> with remote :["
                        + DateUtil.formDateForLog(remoteDelistTime) + "]");
                notMatchedDelistTime.add(item.getDeListTime());

            }
            sb.append("not match recent down jdpids :" + StringUtils.join(msgs, ',') + "\n");

            msgs.clear();
            for (Long apiId : apiIds) {
                Long delistTime = apiDelistTimeMap.get(apiId);
                ItemPlay localDbItem = ItemDao.findByNumIid(userId, apiId);
                msgs.add(" [" + apiId + "]:" + DateUtil.formDateForLog(delistTime) + " -- local:"
                        + (localDbItem == null ? null : DateUtil.formDateForLog(localDbItem.getDeListTime())));
                notMatchedDelistTime.add(delistTime);
            }

            boolean isMatch = notMatchedDelistTime.size() <= 1;
            if (!isMatch) {
                tryRefixCurrentNotMatchIds(userId);
            }

            sb.append(" not match num [" + notMatchedDelistTime.size() + "] showindow on ?? :" + user.isShowWindowOn()
                    + "\n");
            sb.append("not match recent down apiids :" + StringUtils.join(apiIds, ',') + "\n");
            sb.append("not match recent down apiids with delist time:\n" + StringUtils.join(msgs, '\n') + "\n");
//            sb.append(" no where comefrom for the jdp ids:" + StringUtils.join(allJdpIds, ',') + "\n");
            return sb;
        }

        public boolean isJdpListening() {
            return isJdpListening;
        }

        public void setJdpListening(boolean isJdpListening) {
            this.isJdpListening = isJdpListening;
        }

        public boolean isRecentDownMatch() {
            return notMatchedDelistTime.size() <= 1;
        }

        public static boolean isRecentDownItemMatch(User user) {
            StringBuilder sb = new StringBuilder();
            Long userId = user.getId();

            Map<Long, Long> apiDelistTimeMap = new HashMap<Long, Long>();

            int maxNum = 199;
            List<Item> jdpItems = JdpApiImpl.get().findRecentDownItems(user, maxNum);
            Set<Long> dbIds = ShowWindowApi.toNumIids(jdpItems);
//            log.info("[back db <" + dbIds.size() + "> extrfa :]" + dbIds);
            List<Item> apiItems = OriginApiImpl.get().findRecentDownItems(user, maxNum);
            for (Item item : apiItems) {
                Date date = item.getDelistTime();
                apiDelistTimeMap.put(item.getNumIid(), date == null ? 0L : date.getTime());
            }
            Set<Long> apiIds = ShowWindowApi.toNumIids(apiItems);
//            log.info("[back item  extrfa :]" + apiIds);
            Iterator<Long> it = dbIds.iterator();
            while (it.hasNext()) {
                Long jId = it.next();
                if (apiIds.contains(jId)) {
                    apiIds.remove(jId);
                    it.remove();
                }
            }

            Set<Long> allJdpIds = JdpItemModel.allNumIids(user);
            Set<Long> allApiIds = ItemApi.allNumIids(user);

            Iterator<Long> jdpIter = allJdpIds.iterator();
            while (jdpIter.hasNext()) {
                Long id = jdpIter.next();
                if (allApiIds.contains(id)) {
                    jdpIter.remove();
                }
            }

            Set<Long> notMatchedDelistTime = new HashSet<Long>();

            List<String> msgs = new ArrayList<String>();
            for (Long id : dbIds) {
                ItemPlay item = ItemDao.findByNumIid(user.getId(), id);
                msgs.add(" [" + id + "]:" + DateUtil.formDateForLog(item.getDeListTime()));

                notMatchedDelistTime.add(item.getDeListTime());
            }

            sb.append("not match recent down jdpids :" + StringUtils.join(msgs, ',') + "\n");

            msgs.clear();
            for (Long apiId : apiIds) {
                Long delistTime = apiDelistTimeMap.get(apiId);
                ItemPlay localDbItem = ItemDao.findByNumIid(userId, apiId);
                msgs.add(" [" + apiId + "]:" + DateUtil.formDateForLog(delistTime) + " -- local:"
                        + (localDbItem == null ? null : DateUtil.formDateForLog(localDbItem.getDeListTime())));

                notMatchedDelistTime.add(delistTime);
            }

            sb.append(" not match num [" + notMatchedDelistTime.size() + "] showindow on ?? :" + user.isShowWindowOn()
                    + "\n");
            sb.append("not match recent down apiids :" + StringUtils.join(apiIds, ',') + "\n");
            sb.append("not match recent down apiids with delist time:\n" + StringUtils.join(msgs, '\n') + "\n");

            sb.append(" no where comefrom for the jdp ids:" + StringUtils.join(allJdpIds, ',') + "\n");
            log.warn(" after sync :" + sb.toString());

            /**
             * 这个东西很有意思，哈哈, 有一种情况就是排序的时候，由于时间都是一样的，所以本地和服务器有可能排序不对
             */
            boolean isMatch = notMatchedDelistTime.size() <= 1;
            if (!isMatch) {
                tryRefixCurrentNotMatchIds(userId);
            }

            return isMatch;
        }

        public static void tryRefixCurrentNotMatchIds(Long userId) {
            Set<Long> dbIds = ItemDao.allSaleItemIds(userId);
            for (Long numIid : dbIds) {
                ItemPlay itemPlay = ItemDao.findByNumIid(userId, numIid);
                if (itemPlay == null) {
                    continue;
                }

                long itemPlayDelist = itemPlay.getDeListTime();
                Item jdpItem = JdpItemModel.findByNumIid(userId, numIid);
                long jdpDelist = jdpItem.getDelistTime() == null ? 0L : jdpItem.getDelistTime().getTime();
                log.warn("  fix process item [" + numIid + "] local delist [ "
                        + DateUtil.formDateForLog(itemPlayDelist)
                        + "] while in rds :[" + DateUtil.formDateForLog(jdpDelist) + "]");

                if (itemPlayDelist != jdpDelist) {
                    itemPlay.setDeListTime(jdpDelist);
                    itemPlay.rawUpdate();
                }
            }
        }
    }

    public Task clearOldTradeData() {
        try {
            long end = System.currentTimeMillis() - DateUtil.THIRTY_DAYS - DateUtil.DAY_MILLIS;
            long start = 0L;
            return new JuShiTaDataDeleteApi(null, start, end, JuDataType.tb_trade).call();
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return null;
        }
    }

}
