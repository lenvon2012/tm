package models.lottery;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import result.TMResult;
import utils.DateUtil;

import com.ciaosir.client.pojo.PageOffset;

@Entity(name = LotteryPlay.TABLE_NAME)
@JsonIgnoreProperties(value = {})
public class LotteryPlay extends Model implements Serializable {

    @Transient
    private static final Logger log = LoggerFactory.getLogger(LotteryPlay.class);

    @Transient
    public static final String TABLE_NAME = "lottery";

    public enum PrizeGrade {
        /*增加奖项务必从在最后依次添加！不然奖项对应会出错！*/
        未开始, 未中奖, 流量套餐五折, 自动标题优化五折, 防恶意购买五折, 滞销解决大师五折, 引流王五折, 话费1元, 彩票1注, 再来一次,

        /* 自动标题 */
        自动标题特惠5元, 自动标题一个月8元, 自动标题一季度20元, 自动标题半年39元, 自动标题一年69元, 自动标题特惠8元, 自动标题一个月18元, 自动标题一季度30元, 自动标题半年60元, 自动标题一年99元,

        /* 淘掌柜 */
        淘掌柜特惠5元, 淘掌柜一个月8元, 淘掌柜一季度20元, 淘掌柜半年39元, 淘掌柜一年69元, 淘掌柜特惠15元, 淘掌柜一个月20元, 淘掌柜一季度39元, 淘掌柜半年59元, 淘掌柜一年79元,

        /* 差评防御 */
        差评防御师旗舰版18元, 赠送短信1条, 赠送短信5条
    }

    @Transient
    public static HashMap<PrizeGrade, String> REWARD_MAP = new HashMap<PrizeGrade, String>();
    static {
        // REWARD_MAP.put(PrizeGrade.流量套餐五折, "http://to.taobao.com/kodvAhy");
        // REWARD_MAP.put(PrizeGrade.自动标题优化五折, "http://to.taobao.com/7D40Bhy");
        // REWARD_MAP.put(PrizeGrade.防恶意购买五折, "http://to.taobao.com/MmaFBhy");
        // REWARD_MAP.put(PrizeGrade.滞销解决大师五折, "http://to.taobao.com/uCPGBhy");
        // REWARD_MAP.put(PrizeGrade.引流王五折, "http://to.taobao.com/2HWzAhy");
        REWARD_MAP.put(PrizeGrade.流量套餐五折, "http://t.tobti.com");
        REWARD_MAP.put(PrizeGrade.自动标题优化五折, "http://t.tobti.com");
        REWARD_MAP.put(PrizeGrade.防恶意购买五折, "http://t.tobti.com");
        REWARD_MAP.put(PrizeGrade.滞销解决大师五折, "http://t.tobti.com");
        REWARD_MAP.put(PrizeGrade.引流王五折, "http://t.tobti.com");

        REWARD_MAP.put(PrizeGrade.自动标题特惠5元, "http://to.taobao.com/Dojazgy");
        REWARD_MAP.put(PrizeGrade.自动标题特惠8元, "http://to.taobao.com/2uiazgy");

        REWARD_MAP.put(PrizeGrade.自动标题一个月8元, "http://to.taobao.com/uXBfzgy");
        REWARD_MAP.put(PrizeGrade.自动标题一季度20元, "http://to.taobao.com/NZxezgy");
        REWARD_MAP.put(PrizeGrade.自动标题半年39元, "http://to.taobao.com/GRwezgy");
        REWARD_MAP.put(PrizeGrade.自动标题一年69元, "http://to.taobao.com/zawezgy");

        REWARD_MAP.put(PrizeGrade.自动标题一个月18元, "http://to.taobao.com/4yuezgy");
        REWARD_MAP.put(PrizeGrade.自动标题一季度30元, "http://to.taobao.com/65uezgy");
        REWARD_MAP.put(PrizeGrade.自动标题半年60元, "http://to.taobao.com/DXtezgy");
        REWARD_MAP.put(PrizeGrade.自动标题一年99元, "http://to.taobao.com/r4tezgy");

        REWARD_MAP.put(PrizeGrade.淘掌柜特惠5元, "http://to.taobao.com/9oiHzgy");
        REWARD_MAP.put(PrizeGrade.淘掌柜一个月8元, "http://to.taobao.com/FV9tzgy");
        REWARD_MAP.put(PrizeGrade.淘掌柜一季度20元, "http://to.taobao.com/nIbfzgy");
        REWARD_MAP.put(PrizeGrade.淘掌柜半年39元, "http://to.taobao.com/uRbfzgy");
        REWARD_MAP.put(PrizeGrade.淘掌柜一年69元, "http://to.taobao.com/1Abfzgy");

        REWARD_MAP.put(PrizeGrade.淘掌柜特惠15元, "http://to.taobao.com/ov7Dzgy");
        REWARD_MAP.put(PrizeGrade.淘掌柜一个月20元, "http://to.taobao.com/ihCDzgy");
        REWARD_MAP.put(PrizeGrade.淘掌柜一季度39元, "http://to.taobao.com/rjBDzgy");
        REWARD_MAP.put(PrizeGrade.淘掌柜半年59元, "http://to.taobao.com/ZJ2Pzgy");
        REWARD_MAP.put(PrizeGrade.淘掌柜一年79元, "http://to.taobao.com/vwADzgy");

        REWARD_MAP.put(PrizeGrade.差评防御师旗舰版18元, "http://to.taobao.com/aJDVygy");
        REWARD_MAP.put(PrizeGrade.赠送短信1条, "");
        REWARD_MAP.put(PrizeGrade.赠送短信5条, "");
    }

    @Index(name = "userId")
    public Long userId;

    @Column(columnDefinition = "varchar(63) default NULL")
    public String nick;

    /**
     * 标记哪个产品 1：淘掌柜 @Drepricated
     */
    // private int pid;

    /**
     * 获奖等级
     */
    @Enumerated(EnumType.ORDINAL)
    public PrizeGrade prize;

    private long ts;

    /**
     * 标记是否领过
     */
    private boolean used;

    @Column(columnDefinition = "varchar(63) default NULL")
    public String wangwang;

    @Column(columnDefinition = "varchar(31) default NULL")
    public String mobile;

    @Transient
    @JsonProperty(value = "url")
    private String rewardUrl;

    public LotteryPlay(Long userId, String nick, PrizeGrade prize) {
        this.userId = userId;
        this.nick = nick;
        this.prize = prize;
        this.ts = System.currentTimeMillis();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public PrizeGrade getPrize() {
        return prize;
    }

    public void setPrize(PrizeGrade prize) {
        this.prize = prize;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public String getPrizeName() {
        return prize.name();
    }

    public void setRewardUrl(String rewardUrl) {
        this.rewardUrl = rewardUrl;
    }

    public String getRewardUrl() {
        return REWARD_MAP.get(prize);
    }

    public String getWangwang() {
        return wangwang;
    }

    public void setWangwang(String wangwang) {
        this.wangwang = wangwang;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    @Override
    public String toString() {
        return "LotteryPlay [userId=" + userId + ", nick=" + nick + ", prize=" + prize + ", ts=" + ts + ", used="
                + used + ", wangwang=" + wangwang + ", mobile=" + mobile + ", rewardUrl=" + rewardUrl + "]";
    }

    public static List<LotteryPlay> findLotteryById(Long userId) {
        return LotteryPlay.find("userId = ? order by ts desc", userId).fetch();
    }

    public static int countTodayLotteryTimes(Long userId) {
        long ts = DateUtil.formCurrDate();
        long count = LotteryPlay.count("userId = ? and ts > ?", userId, ts);
        return (int) count;
    }

    public static long countLotteryByUserId(Long userId) {
        long count = LotteryPlay.count("userId = ?", userId);
        return count;
    }

    public static TMResult findLotteryByUserId(Long userId, PageOffset po) {
        List<LotteryPlay> list = LotteryPlay.find("userId = ? and prize > 0 order by ts desc", userId).fetch(
                po.getPn(), po.getPs());
        int count = (int) countLotteryByUserId(userId);
        return new TMResult(list, count, po);
    }

    public static List<LotteryPlay> findLatestLottery() {
        List<LotteryPlay> list = LotteryPlay.find("prize > ? order by ts desc", PrizeGrade.未中奖).fetch(100);
        return list;
    }

    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public static List<LotteryPlay> findPrizeList(String date, String prize) {
        try {
            long ts = DateUtil.formDailyTimestamp(sdf.parse(date));
            PrizeGrade p = PrizeGrade.valueOf(prize);
            List<LotteryPlay> list = LotteryPlay.find("ts >= ? and ts < ? and prize = ? order by mobile desc", ts, ts + DateUtil.DAY_MILLIS, p).fetch();
            return list;
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    
    public static List<LotteryPlay> findByNick(String nick) {
        return LotteryPlay.find("nick = ? order by ts desc", nick).fetch();
    }

}
