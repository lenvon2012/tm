
package job.showwindow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jdp.ApiJdpAdapter;
import jdp.ApiJdpAdapter.JdpApiImpl;
import jdp.ApiJdpAdapter.OriginApiImpl;
import models.item.ItemPlay;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cache.UserHasTradeItemCache;

import com.ciaosir.client.utils.DateUtil;
import com.taobao.api.domain.Item;

import controllers.SWindows.WindowConcigs;

public class WindowValidationJob {

    private static final Logger log = LoggerFactory.getLogger(WindowValidationJob.class);

    public static final String TAG = "WindowValidationJob";

    User user;

    StringBuilder sb = new StringBuilder();

    public WindowValidationJob(User user) {
        super();
        this.user = user;
    }

    boolean useOriginApi = false;

    public WindowValidationJob(User user, boolean useOriginApi) {
        super();
        this.user = user;
        this.useOriginApi = useOriginApi;
    }

    public void doJob() {

        WindowConcigs config = WindowConcigs.build(user);
        Map<Long, Item> map = fetchRecentWithSale();
        int max = 200;

        List<Item> recentDownItems = useOriginApi ? OriginApiImpl.get().findRecentDownItems(user, max) : JdpApiImpl
                .get().findRecentDownItems(user, max);
        List<Item> onWindowItems = useOriginApi ? OriginApiImpl.get().findCurrOnWindowItems(user) : ApiJdpAdapter.get(
                user).findCurrOnWindowItems(user);
        Set<Long> onWindowIds = new HashSet<Long>();
        for (Item item : onWindowItems) {
            onWindowIds.add(item.getNumIid());
        }

        log.info("[on window :]" + onWindowIds);

        sb.append(" config :");
        sb.append(config.toString());
        sb.append('\n');

        for (Item item : recentDownItems) {
            sb.append(" item: [");
            sb.append(item.getNumIid());
            sb.append("]");

            sb.append(" delistTime :[");
            sb.append(DateUtil.formDateForLog(item.getDelistTime().getTime()));
            sb.append(" ]");

            sb.append(" has showcase :[");
            sb.append(item.getHasShowcase());
            sb.append(" ] ");

            sb.append(" on window :[");
            sb.append(onWindowIds.contains(item.getNumIid()));
            sb.append(" ] ");
//            log.info(" item numiid :" + item.getNumIid() + " sale :" + onWindowIds);

            Item saleItem = map.get(item.getNumIid());
            if (saleItem != null) {
                sb.append(" sale :[");
                sb.append(saleItem.getVolume());
                sb.append("]");
            }

            Long[] mustIds = config.getMustIds();
            for (Long long1 : mustIds) {
                if (long1.longValue() == item.getNumIid()) {
                    sb.append(" <isMust> ");
                }
            }

            Long[] excludeIds = config.getExcludeIds();
            for (Long long1 : excludeIds) {
                if (long1.longValue() == item.getNumIid()) {
                    sb.append(" <isExclude> ");
                }
            }

            sb.append(" [");
            sb.append("");
            sb.append("]");
            sb.append('\n');
        }

    }

    private Map<Long, Item> fetchRecentWithSale() {
        List<ItemPlay> items = UserHasTradeItemCache.getByUser(user, 100, false);

        Map<Long, Integer> itemSale = new HashMap<Long, Integer>();
        for (ItemPlay itemPlay : items) {
            itemSale.put(itemPlay.getId(), itemPlay.getSalesCount());
        }

        List<Item> rawItems = ApiJdpAdapter.get(user).findRecentDownItems(user, 200);
        for (Item item : rawItems) {
            Integer sale = itemSale.get(item.getNumIid());
            if (sale == null) {
                continue;
            }

            item.setVolume(sale.longValue());
        }

        Map<Long, Item> map = new HashMap<Long, Item>();
        for (Item itemPlay : rawItems) {
            map.put(itemPlay.getNumIid(), itemPlay);
        }
        return map;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public StringBuilder getSb() {
        return sb;
    }

    public void setSb(StringBuilder sb) {
        this.sb = sb;
    }

}
