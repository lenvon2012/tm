
package models.visit;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import job.writter.VisitLogWritter.LogBean;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import utils.TimeUtil;

import com.ciaosir.client.url.URLParser;
import com.ciaosir.client.utils.NetworkUtil;
import com.ciaosir.client.utils.NumberUtil;

@Entity(name = VisitLog.TABLE_NAME)
public class VisitLog extends Model implements Comparable<VisitLog> {

    private static final Logger log = LoggerFactory.getLogger(VisitLog.class);

    private static final String TAG = "ATSVisitLog";

    public static final String TABLE_NAME = "visitlog";

    @Column(name = "userId", nullable = false)
    @Index(name = "tbUserId")
    public Long userId;

    /**
     * 日志产生时间
     */
    @Index(name = "created")
    public Long created = 0L;

    @Index(name = "hour")
    public int hour = 0;

    @Index(name = "day")
    public int day = 0;

    public static class Status {

        //        public static int isTradeRealted = 8;

        public static int isFromBus = 16;

        public static int isFromTaobaoke = 32;

        public static int isFromMainSearch = 64;

    }

    public int status = 0;

    /**
     * 表示用户访问页面唯一的ID，只要保持浏览器不变，用户登录前和登录后的CookieId是一样的
     */
    @Transient
    public String cookieIDString;

    @Index(name = "cookieId")
    public Long cookieId;

    // /**
    // * 当前URL
    // */
    // @Column(length = MAX_URL_LENGTH, columnDefinition = "varchar(" +
    // MAX_URL_LENGTH + ")")
    @Index(name = "itemId")
    public long curItemId;

    /**
     * 来源页面的URL
     */
    // @Column(length = MAX_URL_LENGTH, columnDefinition = "varchar(" +
    // MAX_URL_LENGTH + ")")

    /**
     * If this is an item form url, then this would be the item id or this would
     * be the url_derived hash id
     */

    // 直通车关键词
    @Index(name = "searchKey")
    public String searchKey;

    public long ip;

    /**
     * 本次session达成交易的tid（只在购物车确认页面和订单确认页面的记录中返回）
     */
    // public Long createdTid = Long.valueOf(0L);

    @Override
    public int compareTo(VisitLog o) {
        // TODO Auto-generated method stub
        return 0;
    }

    private VisitLog(Long userId, Long created, int status, String cookieIDString, long curItemId, String searchKey,
            long ip, int day, int hour) {
        super();
        this.userId = userId;
        this.created = created;
        this.status = status;
        this.cookieId = NumberUtil.hashStringToLong(cookieIDString);
        this.curItemId = curItemId;
        this.searchKey = searchKey;
        this.ip = ip;
        this.day = day;
        this.hour = hour;
    }

    public static void ensureLog(LogBean bean) {
        VisitLog.ensureLog(bean.getUrl(), bean.getIp(), bean.getCookieIdString(), bean.getUserId(), bean.getTs());
    }

    public static void ensureLog(String url, String ip, String cookieString, Long userId, long timeMillis) {
        if (!URLParser.isFromSimba(url)) {
            return;
        }

        String key = URLParser.findBusClickKey(url);
        long itemId = URLParser.findItemId(url);
        long ipLong = NetworkUtil.getIpNum(ip);

        Date date = new Date();
        date.setTime(timeMillis);
        int day = TimeUtil.getDay(date);
        int hour = TimeUtil.getHour(date);

        new VisitLog(userId, timeMillis, Status.isFromBus, cookieString, itemId, key, ipLong, day, hour).save();
    }

}
