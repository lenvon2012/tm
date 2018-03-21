package underup.frame.industry;

import java.util.*;

//import models.mysql.fengxiao.CatTopSaleItem;
import underup.frame.industry.CatTopSaleItemSQL;
import models.item.ItemCatPlay;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.*;

import actions.industry.IndustryDelistResultAction.DelistItemInfo;
import actions.industry.RemoteIndustryGetAction.IndustryItemInfo;

import utils.*;

public class ListTimeRange {
    private static final Logger log = LoggerFactory.getLogger(ListTimeRange.class);
    private static final String TAG = "ListTimeRange";

    private Long cid;
    private long year;
    private long month;
    private int[] hourDelistTime = new int[7 * 24];

    public ListTimeRange(Long cid, long year, long month) {
        this.cid = cid;
        this.year = year;
        this.month = month;
    }

    public int[] getHourDelistTime() {
        return hourDelistTime;
    }

    public void exec() {
        ItemCatPlay itemCatPlay = ItemCatPlay.findByCid(cid);
        List<Long> cids = new ArrayList<Long>();
        if (!itemCatPlay.isParent) {
            cids.add(this.cid);
        } else {
            cids = ItemsCatArrange.getChildrenCids(cid, year, month);
        }
        List<Long> delistTimes = CatTopSaleItemSQL.getDelistTime(cids, year, month);
        if (delistTimes != null) {
            for (long delistTime : delistTimes) {
                long relativeTime = getDelistTime(delistTime);
                int delistHour = getItemDelistHour(relativeTime);
                if (delistHour >= 0) {
                    hourDelistTime[delistHour]++;
                }
            }
        } else {
            log.error(TAG + "can't find topItems");
        }

    }

    // 得到相对时间即delistTime所在的星期的时间毫秒数，这里的时间是从星期天开始算起的
    private static long getRelativeDelistTime(long delistTime) {
        if (delistTime <= 0) {
            return -1;
        }

        long weekStart = DateUtil.findThisWeekStart(delistTime);

        if (weekStart <= 0) {
            return -1;
        }

        long relativeTime = delistTime - weekStart;

        return relativeTime;
    }

    // 得到从星期一开始算起的时间
    private static long getDelistTime(long delistTime) {
        long relativeTime = getRelativeDelistTime(delistTime);
        if (relativeTime >= 0 && relativeTime < DateUtil.DAY_MILLIS) {
            relativeTime += 6 * DateUtil.DAY_MILLIS;
        } else {
            relativeTime -= DateUtil.DAY_MILLIS;
        }
        return relativeTime;
    }

    // 得到相对于周一0点起的小时数
    private static int getItemDelistHour(long relativeTime) {
        if (relativeTime < 0) {
            return -1;
        }
        int relativeHour = (int) (relativeTime / DateUtil.ONE_HOUR_MILLIS);
        return relativeHour;

    }
}
