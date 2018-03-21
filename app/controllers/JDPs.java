
package controllers;

import static java.lang.String.format;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jdp.ApiJdpAdapter;
import jdp.ApiJdpAdapter.JdpApiImpl;
import jdp.ApiJdpAdapter.JdpUserStatusFixer;
import jdp.ApiJdpAdapter.OriginApiImpl;
import jdp.JdpCancelAllUserJob;
import jdp.JdpModel.JdpItemModel;
import jdp.JdpReRegisterAllUserJob;
import jdp.JdpRecentModifiedItemsWorker;
import job.apiget.ItemUpdateJob;
import job.diagjob.PropDiagJob;
import models.item.ItemPlay;
import models.op.RawId;
import models.order.OrderDisplay;
import models.user.User;

import org.apache.commons.lang.StringUtils;

import utils.PlayUtil;
import ats.ATSResultGetAPI;
import ats.TaskManager;
import bustbapi.ItemApi.ItemsInventoryCount;
import bustbapi.ItemApi.ItemsOnsaleCount;
import bustbapi.JDPApi;
import bustbapi.JDPApi.JdpItemStatus;
import bustbapi.JDPApi.JuDataType;
import bustbapi.JDPApi.JuShiTaCancelApi;
import bustbapi.JDPApi.JuShiTaDataDeleteApi;
import bustbapi.ShowWindowApi;
import bustbapi.TBApi;
import bustbapi.TMTradeApi;
import bustbapi.TMTradeApi.ShopBaseTradeInfo;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.Task;

import configs.TMConfigs.Rds;
import dao.UserDao;
import dao.item.ItemDao;
import dao.trade.OrderDisplayDao;

public class JDPs extends TMController {

    public static void lastItems(int num) {
        if (num <= 0) {
            num = 3;
        }

        User user = getUser();
        List<Item> items = JdpItemModel.jdpItemFetcher(" nick = ? order by order by jdp_modified limit ? ",
                user.getUserNick(), num);

        renderText(new GsonBuilder().setPrettyPrinting().create().toJson(items));
    }

    public static void cancel() {
        User user = getUser();
        Boolean res = new JuShiTaCancelApi(user).call();
        renderText("cancel result :" + res + " for user:" + user.toIdNick());

    }

    public static void reRegister() {
        User user = getUser();
        Boolean res = new JuShiTaCancelApi(user).call();
        new JDPApi.JuShiTaAddUserApi(user).call();
    }

    public static void reRegisterAll(int offset) {
        new JdpReRegisterAllUserJob(offset).now();
    }

    public static void removeJdpItems() {
        User user = getUser();
        Task deleteTask = new JuShiTaDataDeleteApi(user, JuDataType.tb_item).call();
        renderJSON(deleteTask);
    }

    public static void fixJdpModified() {
        User user = getUser();
        new PropDiagJob(user, true).now();
    }

    public static void deleteBeforeModified() {
        User user = getUser();
        new JdpUserStatusFixer(user, true).call();
    }

    public static void itemStatus() {

        StringBuilder sb = new StringBuilder();
        User user = getUser();
        Long userId = user.getId();
        Long onlineNum = ItemDao.countOnsaleItemByuserId(userId);
        Long inStockNum = ItemDao.countInStockItemByuserId(userId);

        Long temp = new ItemsOnsaleCount(user, null, null).call();
        int apiItemOnSaleNum = temp == null ? -1 : temp.intValue();
        temp = new ItemsInventoryCount(user, null, null).call();
        int apiItemInventoryNum = temp == null ? -1 : temp.intValue();

        Set<Long> apiOnWIndowNumIids = ShowWindowApi.toNumIids(OriginApiImpl.get().findCurrOnWindowItems(user));
        int apiOnWindowItemNum = apiOnWIndowNumIids.size();
        Set<Long> jdpOnWIndowNumIids = ShowWindowApi.toNumIids(JdpApiImpl.get().findCurrOnWindowItems(user));
        int jdpOnWindowItemNum = jdpOnWIndowNumIids.size();

        long jdpOnSaleCount = JdpItemModel.countOnSaleItem(user);
        long jdpInventoryCount = JdpItemModel.countInventoryItems(user);
        sb.append(" api extra on window numiids :[" + apiOnWIndowNumIids + "] -- [" + jdpOnWIndowNumIids + "]\n");
        sb.append(" rawids :" + RawId.hasId(user.getId()) + "\n");
        sb.append(" db  online [" + onlineNum + "]\tinstock num:[" + inStockNum + "]\n");
        sb.append(" api online [" + apiItemOnSaleNum + "]\tinstock num:[" + apiItemInventoryNum + "]\n");
        sb.append(" jdp online [" + jdpOnSaleCount + "]\tjdp instock num:[" + jdpInventoryCount + "]\n");
        sb.append(" api onwindow [" + apiOnWindowItemNum + "]\t   jdp on window num:[" + jdpOnWindowItemNum + "]\n");
    }

    public static void matchItem() {
        User user = getUser();
        StringBuilder sb = new StringBuilder();

        JdpItemStatus iStatus = new JdpItemStatus(user);
        sb.append(iStatus.toStrBuilder().toString());
        if (!iStatus.isJdpStatusMatch()) {
            addNotSameItemCheck(user, sb);
            addJdpNotSameCheck(user, sb);
        }

        renderText(sb.toString());
    }

    private static void addJdpNotSameCheck(User user, StringBuilder sb) {
        List<Item> jdpItems = JdpItemModel.jdpItemFetcher(" nick = ?  and approve_status = 'instock' ",
                user.getUserNick());
        List<ItemPlay> localItems = new ArrayList<ItemPlay>();
        for (Item item : jdpItems) {
            localItems.add(new ItemPlay(user.getId(), item));
        }
        Map<Long, ItemPlay> remoteItems = new HashMap<Long, ItemPlay>();
        try {
            List<Item> rawItems = new ItemUpdateJob(user.getId()).getItem(user, null, null);
            for (Item item : rawItems) {
                remoteItems.put(item.getNumIid(), new ItemPlay(user.getId(), item));
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }

        Iterator<ItemPlay> it = localItems.iterator();
        while (it.hasNext()) {
            ItemPlay localItem = it.next();
            ItemPlay remote = remoteItems.get(localItem.getNumIid());

            if (remote == null) {
                continue;
            }
            if (!remote.getTitle().equals(localItem.getTitle())) {
                continue;
            }
            it.remove();
            remoteItems.remove(remote.getNumIid());
        }

        sb.append(" \n >>>>>> jdps  not match modified items:");
        sb.append(StringUtils.join(localItems, '\n'));
        sb.append(" \n <<<<<<  remotes not match items:\n");
        sb.append(StringUtils.join(remoteItems.values(), '\n'));

        return;
    }

    private static void addNotSameItemCheck(User user, StringBuilder sb) {
        // TODO Auto-generated method stub
        List<ItemPlay> localItems = ItemDao.findByUserId(user.getId());

        Map<Long, ItemPlay> remoteItems = new HashMap<Long, ItemPlay>();
        try {
            List<Item> rawItems = new ItemUpdateJob(user.getId()).getItem(user, null, null);
            for (Item item : rawItems) {
                remoteItems.put(item.getNumIid(), new ItemPlay(user.getId(), item));
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }

        Iterator<ItemPlay> it = localItems.iterator();
        while (it.hasNext()) {
            ItemPlay localItem = it.next();
            ItemPlay remote = remoteItems.get(localItem.getNumIid());

            if (remote == null) {
                continue;
            }
            if (!remote.getTitle().equals(localItem.getTitle())) {
                continue;
            }
            it.remove();
            remoteItems.remove(remote.getNumIid());
        }

        sb.append(" \n >>>>>> local not match modified items:");
        sb.append(StringUtils.join(localItems, '\n'));
        sb.append(" \n <<<<<<  remotes not match items:\n");
        sb.append(StringUtils.join(remoteItems.values(), '\n'));

        return;
    }

    public static void matchTrades() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        User user = getUser();
        Long userId = user.getId();
        try {

            Map<Long, Integer> dbMap = OrderDisplayDao.findUserRecentTrade(userId);
            ShopBaseTradeInfo apiInfo = TMTradeApi.buildNumIidSaleMap(user, 30);

            StringBuilder sb = new StringBuilder();
            sb.append("db trade info :");
            sb.append(gson.toJson(dbMap));
            sb.append('\n');
            sb.append("api trade info :");
            sb.append(gson.toJson(apiInfo.getNumIidSales()));
            renderText(sb.toString());

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            renderText(e.getMessage());
        }
    }

    public static void debugTrade(long id) {
        User user = getUser();
        HashSet<Long> set = new HashSet<Long>();
        set.add(id);
        List<OrderDisplay> orders = OrderDisplayDao.findByUserIdOids(user.getId(), set);
        renderJSON(orders);
    }

    public static void trades() {

    }

    public static void checkUserJdpStatus(String nick) {
        User user = UserDao.findByUserNick(nick);
        if (user == null) {
            renderText("no user");
        }

        Set<String> onUsers = new JDPApi.JuShiTaGetUsers(user).call();
        renderText("back on users:" + onUsers);
    }

    public static void clearUserJdpItems() {
        long endMillis = System.currentTimeMillis() - DateUtil.TRIPPLE_DAY_MILLIS_SPAN;
        long startMillis = endMillis - (40 * DateUtil.THIRTY_DAYS);
        User user = getUser();

        Task task = new JuShiTaDataDeleteApi(user, startMillis, endMillis, JuDataType.tb_item).call();
        renderText(new Gson().toJson(task));
    }

    public static void clearUserJdpAll() {
        User user = getUser();
        Task task = new JuShiTaDataDeleteApi(user, 0L + DateUtil.DAY_MILLIS, System.currentTimeMillis()
                - DateUtil.TRIPPLE_DAY_MILLIS_SPAN, JuDataType.tb_item).call();
        renderText(new Gson().toJson(task));
    }

    public static void clearAll() {
        long end = System.currentTimeMillis() - DateUtil.TRIPPLE_DAY_MILLIS_SPAN;
        long start = end - (10 * DateUtil.THIRTY_DAYS);

        Task task = null;
        JuShiTaDataDeleteApi deleteApi = null;
        deleteApi = new JDPApi.JuShiTaDataDeleteApi(null, start, end, JuDataType.tb_item);
        task = deleteApi.call();
        log.info("[task111]" + new Gson().toJson(task));

        deleteApi = new JDPApi.JuShiTaDataDeleteApi(null, start, end, JuDataType.tb_trade);
        task = deleteApi.call();
        log.info("[task112]" + new Gson().toJson(task));

        deleteApi = new JDPApi.JuShiTaDataDeleteApi(null, start, end, JuDataType.tb_item);
        deleteApi.setClient(TBApi.genClient(APIConfig.taobiaoti));
        task = deleteApi.call();
        log.info("[task taobiaoti 1 ]" + new Gson().toJson(task));

        deleteApi = new JDPApi.JuShiTaDataDeleteApi(null, start, end, JuDataType.tb_trade);
        deleteApi.setClient(TBApi.genClient(APIConfig.taobiaoti));
        task = deleteApi.call();
        log.info("[task  taobiaoti 2]" + new Gson().toJson(task));
    }

    /**
     * /jdps/clearOldItems?start=2013-01-31&end=2013-02-21
     * @param start
     * @param end
     * @throws ParseException
     */
    public static void clearOldItems(String start, String end) throws ParseException {
        long endMillis = System.currentTimeMillis() - DateUtil.TRIPPLE_DAY_MILLIS_SPAN;
        long startMillis = endMillis - (24 * DateUtil.THIRTY_DAYS);

        Task task = new JDPApi.JuShiTaDataDeleteApi(null, startMillis, endMillis, JuDataType.tb_item).call();
        log.info("[task]" + new Gson().toJson(task));
    }

    public static void clearAllRawIdUser() {

        long endMillis = System.currentTimeMillis() - DateUtil.TRIPPLE_DAY_MILLIS_SPAN;
        long startMillis = endMillis - (24 * DateUtil.THIRTY_DAYS);

        List<RawId> findAll = RawId.findAll();
        for (RawId rawId : findAll) {
            User user = UserDao.findById(rawId.getId());
            if (user == null) {
                continue;
            }

            Task task = new JDPApi.JuShiTaDataDeleteApi(user, startMillis, endMillis, JuDataType.tb_item).call();
            log.info("[task]" + new Gson().toJson(task) + "  for user:" + user);
        }
    }

    /*
     *  [task]{"created":"Feb 25, 2014 12:55:36 PM","taskId":223631481}
     *  /jdps/taskStatus?id=223631481
     */
    public static void taskStatus(long id) {
        final Task taobaoTask = ATSResultGetAPI.getTaskResult(id);
        log.info("[find taobao task:]" + new Gson().toJson(taobaoTask));
        renderJSON(taobaoTask);
    }

    public static void recentDown() {
        User user = getUser();
        List<ItemPlay> list = ItemDao.recentDownItems(user, 200);
        StringBuilder sb = new StringBuilder();
        for (ItemPlay itemPlay : list) {
            sb.append(itemPlay.toString());
            sb.append('\n');
        }
        renderText(sb.toString());
    }

    public static void useJdpApi() {
        User user = getUser();
        StringBuilder sb = new StringBuilder();
        sb.append(" enable jdp api:");
        sb.append(ApiJdpAdapter.enableJdp(user));
        sb.append('\n');
        sb.append("Rds.enableJdpApi:");
        sb.append(Rds.enableJdpApi);
        sb.append('\n');
        sb.append("Rds.checkUserWith:");
        sb.append(Rds.checkUserWithInRawId);
        sb.append('\n');
        renderText(sb.toString());
    }

    public static void recentModified(String start, String end) {

        log.info(format("recentModified:start, end".replaceAll(", ", "=%s, ") + "=%s", start, end));
        DateFormat df = DateUtil.genYMSHms(0L);
        try {
            long startMillis = df.parse(start).getTime();
            long endMillis = df.parse(end).getTime();
            new JdpRecentModifiedItemsWorker(startMillis, endMillis).doJob();
        } catch (ParseException e) {
            log.warn(e.getMessage(), e);
        }
    }

    public static void clearRecentTrade(int dayAgo) {
        long curr = System.currentTimeMillis();
        long start = curr - (2 * DateUtil.THIRTY_DAYS);
        long end = curr - DateUtil.DAY_MILLIS * dayAgo;

        Gson gson = PlayUtil.genPrettyGson();
        JuShiTaDataDeleteApi api = new JDPApi.JuShiTaDataDeleteApi(null, start, end, JuDataType.tb_trade);
        Task call = api.call();
        log.warn("back task :" + gson.toJson(call));
        Long taskId = call.getTaskId();
        int count = 100;
        while (count-- > 0) {
            Task remoteTask = ATSResultGetAPI.getTaskResult(taskId);
            if (TaskManager.Status.OVER.equals(remoteTask.getStatus())) {
                log.error(" task finish :" + gson.toJson(remoteTask));
                renderJSON(remoteTask);
            }
            log.warn(" current json status : " + gson.toJson(remoteTask));
            CommonUtils.sleepQuietly(50000L);
        }
    }

    public static void cancelAllJob() {
        new JdpCancelAllUserJob().now();
    }

    public static void reregisterForRds(String rds) {
        Set<String> name = new JDPApi.JuShiTaGetUsers(rds).call();
        int count = 1;
        for (String string : name) {
            User user = UserDao.findByUserNick(string);
            if (user == null || !user.isVaild()) {
                continue;
            }

            new JDPApi.JuShiTaCancelApi(user).call();
            new JDPApi.JuShiTaAddUserApi(user).call();
            log.info("re register rds :" + user.toIdNick() + " with offset :" + count++);
        }
    }
}
