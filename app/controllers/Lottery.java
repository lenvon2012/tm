package controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import models.lottery.LotteryPlay;
import models.lottery.LotteryPlay.PrizeGrade;
import models.sms.SmsSendCount;
import models.user.User;

import org.apache.commons.lang.StringUtils;

import com.ciaosir.client.utils.NetworkUtil;

public class Lottery extends TMController {

    public static void play() {
        render("lottery/lottery.html");
    }

    public static void single() {
        render("lottery/zhuanpan.html");
    }

    static Random rand = new Random();

    static final int DAILY_LOTTERY_TIMES = 2;

    static String ZHUANPAN_RET_JSON = "{\"error\":%d,\"remain\":%d,\"angle\":%d,\"prize\":\"%s\",\"url\":\"%s\",\"id\":\"%d\"}";

    static String[] blacklistArr = { "老顽童钱币", "急屁猴", "杨素素1", "王倩倩1861" };
    static HashSet<String> blacklist = new HashSet<String>();

    static {
        for (int i = 0; i < blacklistArr.length; i++) {
            blacklist.add(blacklistArr[i]);
        }
    }

    public static void go() {
        String json = null;
        if (APIConfig.get().getApp() == APIConfig.taoxuanci.getApp()) {
            json = lotteryAutoTitle();
        } else if (APIConfig.get().getApp() == APIConfig.taobiaoti.getApp()) {
            json = lotteryTaozhanggui();
        } else if (APIConfig.get().getApp() == APIConfig.defender.getApp()) {
            json = lotteryDefender();
        } else {
            json = lotteryDefault();
        }

        renderJSON(json);
    }

    private static String lotteryAutoTitle() {
        int error = 0;
        int angle = 0;
        int remain = 0;
        PrizeGrade prize = PrizeGrade.未中奖;
        String url = "";
        User user = getUser();
        int todayTime = LotteryPlay.countTodayLotteryTimes(user.getId());

        if (todayTime >= DAILY_LOTTERY_TIMES) {
            error = 1;
            renderJSON(String.format(ZHUANPAN_RET_JSON, error, remain, angle, prize, url, 0));
        }

        remain = DAILY_LOTTERY_TIMES - todayTime;

        boolean ver = (user.getVersion() <= 20); // 99包年version=20；500以上宝贝数：version=30

        // 自动标题特惠5元, 自动标题一个月8元, 自动标题一季度20元, 自动标题半年39元, 自动标题一年69元,
        int rdx = rand.nextInt(1000);
        if (rdx >= 900) {
            angle = 90 + rand.nextInt(45);
            prize = ver ? PrizeGrade.自动标题特惠5元 : PrizeGrade.自动标题特惠8元;
        } else if (rdx >= 700) {
            angle = 315 + rand.nextInt(45);
            prize = ver ? PrizeGrade.自动标题一个月8元 : PrizeGrade.自动标题一个月18元;
        } else if (rdx >= 500) {
            angle = 45 + rand.nextInt(45);
            prize = ver ? PrizeGrade.自动标题一季度20元 : PrizeGrade.自动标题一季度30元;
        } else if (rdx >= 400) {
            angle = 135 + rand.nextInt(45);
            prize = ver ? PrizeGrade.自动标题半年39元 : PrizeGrade.自动标题半年60元;
        } else if (rdx >= 300) {
            angle = 225 + rand.nextInt(45);
            prize = ver ? PrizeGrade.自动标题一年69元 : PrizeGrade.自动标题一年99元;
        } 
//        else if (rdx >= 290) {
//            angle = 180 + rand.nextInt(45);
//            prize = PrizeGrade.彩票1注;
//        } else if (rdx >= 280) {
//            angle = 360 + rand.nextInt(45);
//            prize = PrizeGrade.话费1元;
//        }
        else if (rdx >= 100) {
            angle = 315 + rand.nextInt(45);
            prize = ver ? PrizeGrade.自动标题一个月8元 : PrizeGrade.自动标题一个月18元;
        } else {
            angle = 270 + rand.nextInt(45);
            prize = PrizeGrade.再来一次;
        }
        
        if (blacklist.contains(user.getUserNick())) {
            prize = ver ? PrizeGrade.自动标题一年69元 : PrizeGrade.自动标题一年99元;
        }

        url = LotteryPlay.REWARD_MAP.get(prize);
        long id = 0;
        if (prize != PrizeGrade.再来一次) {
            LotteryPlay lottery = new LotteryPlay(user.getId(), user.getUserNick(), prize);
            lottery.save();
            id = lottery.getId();
        }
        return String.format(ZHUANPAN_RET_JSON, error, remain, angle, prize, url, id);
    }

    private static String lotteryTaozhanggui() {
        int error = 0;
        int angle = 0;
        int remain = 0;
        PrizeGrade prize = PrizeGrade.未开始;
        String url = "";
        User user = getUser();
        int todayTime = LotteryPlay.countTodayLotteryTimes(user.getId());

        if (todayTime >= DAILY_LOTTERY_TIMES) {
            error = 1;
            renderJSON(String.format(ZHUANPAN_RET_JSON, error, remain, angle, prize, url, 0));
        }

        remain = DAILY_LOTTERY_TIMES - todayTime;

        boolean ver = (user.getVersion() <= 20); // 99包年version=20；500以上宝贝数：version=30

        // 自动标题特惠5元, 自动标题一个月8元, 自动标题一季度20元, 自动标题半年39元, 自动标题一年69元,
        int rdx = rand.nextInt(1000);
        if (rdx >= 900) {
            angle = 90 + rand.nextInt(45);
            prize = ver ? PrizeGrade.淘掌柜特惠5元 : PrizeGrade.淘掌柜特惠15元;
        } else if (rdx >= 700) {
            angle = 315 + rand.nextInt(45);
            prize = ver ? PrizeGrade.淘掌柜一个月8元 : PrizeGrade.淘掌柜一个月20元;
        } else if (rdx >= 500) {
            angle = 45 + rand.nextInt(45);
            prize = ver ? PrizeGrade.淘掌柜一季度20元 : PrizeGrade.淘掌柜一季度39元;
        } else if (rdx >= 400) {
            angle = 135 + rand.nextInt(45);
            prize = ver ? PrizeGrade.淘掌柜半年39元 : PrizeGrade.淘掌柜半年59元;
        } else if (rdx >= 300) {
            angle = 225 + rand.nextInt(45);
            prize = ver ? PrizeGrade.淘掌柜一年69元 : PrizeGrade.淘掌柜一年79元;
        }
//        else if (rdx >= 290) {
//            angle = 180 + rand.nextInt(45);
//            prize = PrizeGrade.彩票1注;
//        } else if (rdx >= 280) {
//            angle = 360 + rand.nextInt(45);
//            prize = PrizeGrade.话费1元;
//        }
        else if (rdx >= 100) {
            angle = 90 + rand.nextInt(45);
            prize = ver ? PrizeGrade.淘掌柜特惠5元 : PrizeGrade.淘掌柜特惠15元;
        } else {
            angle = 270 + rand.nextInt(45);
            prize = PrizeGrade.再来一次;
        }

        // 过滤 体验版 抽到啊话费或彩票
        if (user.getVersion() < 20) {
            if (prize == PrizeGrade.话费1元) {
                angle = 315 + rand.nextInt(45);
                prize = PrizeGrade.淘掌柜一个月20元;
            }
            if (prize == PrizeGrade.彩票1注) {
                angle = 90 + rand.nextInt(45);
                prize = PrizeGrade.淘掌柜特惠15元;
            }
        }
        
        if (blacklist.contains(user.getUserNick())) {
            prize = ver ? PrizeGrade.淘掌柜一年69元 : PrizeGrade.淘掌柜一年79元;
        }

        url = LotteryPlay.REWARD_MAP.get(prize);
        long id = 0;
        if (prize != PrizeGrade.再来一次) {
            LotteryPlay lottery = new LotteryPlay(user.getId(), user.getUserNick(), prize);
            lottery.save();
            id = lottery.getId();
        }
        return String.format(ZHUANPAN_RET_JSON, error, remain, angle, prize, url, id);
    }

    private static String lotteryDefender() {
        int error = 0;
        int angle = 0;
        int remain = 0;
        PrizeGrade prize = PrizeGrade.未开始;
        String url = "";
        User user = getUser();
        int todayTime = LotteryPlay.countTodayLotteryTimes(user.getId());

        if (todayTime >= DAILY_LOTTERY_TIMES) {
            error = 1;
            renderJSON(String.format(ZHUANPAN_RET_JSON, error, remain, angle, prize, url, 0));
        }

        remain = DAILY_LOTTERY_TIMES - todayTime;

        // 自动标题特惠5元, 自动标题一个月8元, 自动标题一季度20元, 自动标题半年39元, 自动标题一年69元,
        int rdx = rand.nextInt(1000);
        if (rdx >= 900) {
            angle = 135 + rand.nextInt(45);
            prize = PrizeGrade.淘掌柜特惠5元;
        } else if (rdx >= 700) {
            angle = 45 + rand.nextInt(45);
            prize = PrizeGrade.差评防御师旗舰版18元;
        } else if (rdx >= 500) {
            angle = 315 + rand.nextInt(45);
            prize = PrizeGrade.自动标题一个月8元;
        } else if (rdx >= 350) {
            angle = 225 + rand.nextInt(45);
            prize = PrizeGrade.赠送短信1条;
        } else if (rdx >= 300) {
            angle = 90 + rand.nextInt(45);
            prize = PrizeGrade.赠送短信5条;
        }
//        else if (rdx >= 290) {
//            angle = 180 + rand.nextInt(45);
//            prize = PrizeGrade.彩票1注;
//        } else if (rdx >= 280) {
//            angle = 360 + rand.nextInt(45);
//            prize = PrizeGrade.话费1元;
//        }
        else if (rdx >= 100) {
            angle = 225 + rand.nextInt(45);
            prize = PrizeGrade.赠送短信1条;
        } else {
            angle = 270 + rand.nextInt(45);
            prize = PrizeGrade.再来一次;
        }

        // 过滤 体验版 抽到啊话费或彩票
        if (user.getVersion() < 20) {
            if (prize == PrizeGrade.话费1元 || prize == PrizeGrade.彩票1注) {
                angle = 45 + rand.nextInt(45);
                prize = PrizeGrade.差评防御师旗舰版18元;
            }
        }
        
        // 黑名单
        if (blacklist.contains(user.getUserNick())) {
            prize = PrizeGrade.差评防御师旗舰版18元;
        }

        if (prize == PrizeGrade.赠送短信1条) {
            SmsSendCount.addTotalSmsCount(user.getId(), 1);
        } else if (prize == PrizeGrade.赠送短信5条) {
            SmsSendCount.addTotalSmsCount(user.getId(), 5);
        }

        url = LotteryPlay.REWARD_MAP.get(prize);
        long id = 0;
        if (prize != PrizeGrade.再来一次) {
            LotteryPlay lottery = new LotteryPlay(user.getId(), user.getUserNick(), prize);
            lottery.save();
            id = lottery.getId();
        }
        return String.format(ZHUANPAN_RET_JSON, error, remain, angle, prize, url, id);
    }

    private static String lotteryDefault() {
        int error = 0;
        int angle = 0;
        int remain = 0;
        PrizeGrade prize = PrizeGrade.未中奖;
        String url = "";
        User user = getUser();
        int todayTime = LotteryPlay.countTodayLotteryTimes(user.getId());

        if (todayTime >= DAILY_LOTTERY_TIMES) {
            error = 1;
            renderJSON(String.format(ZHUANPAN_RET_JSON, error, remain, angle, prize, url));
        }

        remain = DAILY_LOTTERY_TIMES - todayTime;
        int rdx = rand.nextInt(1000);
        if (rdx >= 700) {
            angle = 180 + rand.nextInt(45);
            prize = PrizeGrade.流量套餐五折;
        } else if (rdx >= 400) {
            angle = 45 + rand.nextInt(45);
            prize = PrizeGrade.自动标题优化五折;
        } else if (rdx >= 300) {
            angle = 315 + rand.nextInt(45);
            prize = PrizeGrade.防恶意购买五折;
        } else if (rdx >= 200) {
            angle = 135 + rand.nextInt(45);
            prize = PrizeGrade.滞销解决大师五折;
        } else if (rdx >= 100) {
            angle = 225 + rand.nextInt(45);
            prize = PrizeGrade.引流王五折;
        } else {
            angle = 180 + rand.nextInt(45);
            prize = PrizeGrade.流量套餐五折;
            // prize = PrizeGrade.未中奖;
        }

        url = LotteryPlay.REWARD_MAP.get(prize);
        long id = 0;
        if (prize != PrizeGrade.再来一次) {
            LotteryPlay lottery = new LotteryPlay(user.getId(), user.getUserNick(), prize);
            lottery.save();
            id = lottery.getId();
        }
        return String.format(ZHUANPAN_RET_JSON, error, remain, angle, prize, url, id);
    }

    static String ZHUANPAN_RET_JSONP = "jQuery.lotteryPlay({\"error\":%d,\"remain\":%d,\"angle\":%d,\"prize\":\"%s\",\"url\":\"%s\"})";

    public static void zzz() {
        int error = 0;
        int angle = 0;
        int remain = 0;
        PrizeGrade prize = PrizeGrade.未中奖;
        String url = "";

        // User user = getUser();
        int todayTime = 0;
        String ip = NetworkUtil.getRemoteIPForNginx(request);
        long iplong = ip2long(ip);
        if (iplong > 0) {
            todayTime = LotteryPlay.countTodayLotteryTimes(ip2long(ip));

            if (todayTime >= DAILY_LOTTERY_TIMES) {
                error = 1;
                renderJSON(String.format(ZHUANPAN_RET_JSONP, error, remain, angle, prize, url));
            }
        }

        remain = DAILY_LOTTERY_TIMES - todayTime;
        int rdx = rand.nextInt(1000);
        if (rdx >= 700) {
            angle = 180 + rand.nextInt(45);
            prize = PrizeGrade.流量套餐五折;
        } else if (rdx >= 400) {
            angle = 45 + rand.nextInt(45);
            prize = PrizeGrade.自动标题优化五折;
        } else if (rdx >= 300) {
            angle = 315 + rand.nextInt(45);
            prize = PrizeGrade.防恶意购买五折;
        } else if (rdx >= 200) {
            angle = 135 + rand.nextInt(45);
            prize = PrizeGrade.滞销解决大师五折;
        } else if (rdx >= 100) {
            angle = 225 + rand.nextInt(45);
            prize = PrizeGrade.引流王五折;
        } else {
            angle = 180 + rand.nextInt(45);
            prize = PrizeGrade.流量套餐五折;
            // prize = PrizeGrade.未中奖;
        }

        url = LotteryPlay.REWARD_MAP.get(prize);
        // new LotteryPlay(user.getId(), user.getUserNick(), prize).save();
        new LotteryPlay(iplong, "", prize).save();

        String json = String.format(ZHUANPAN_RET_JSONP, error, remain, angle, prize, url);
        renderJSON(json);
    }

    /**
     * ip地址转成整数.
     * 
     * @param ip
     * @return
     */
    public static long ip2long(String ip) {
        if (StringUtils.isEmpty(ip)) {
            return 0L;
        }
        String[] ips = ip.split("[.]");
        if (ips.length != 4) {
            return 0L;
        }
        long num = 16777216L * Long.parseLong(ips[0]) + 65536L * Long.parseLong(ips[1]) + 256 * Long.parseLong(ips[2])
                + Long.parseLong(ips[3]);
        return num;
    }

    static final int MY_PAGE_SIZE = 20;

    // public static void my(int pn) {
    // User user = getUser();
    // PageOffset po = new PageOffset(pn, MY_PAGE_SIZE);
    // TMResult res = LotteryPlay.findLotteryByUserId(user.getId(), po);
    // renderJSON(res);
    // }

    public static void latest() {
        List<LotteryPlay> list = LotteryPlay.findLatestLottery();
        for (LotteryPlay one : list) {
            one.setNick(setInvisible(one.getNick()));
        }
        renderJSON(list);
    }

    private static String setInvisible(String name) {
        if (StringUtils.isEmpty(name)) {
            return "";
        }
        name = name.replace(name.substring(1, name.length() - 1), "***");
        return name;
    }

    public static void post(Long id, String wangwang, String mobile) {
        if (id == null || id <= 0) {
            renderText("");
        }
        if (StringUtils.isBlank(wangwang) && StringUtils.isBlank(mobile)) {
            renderText("");
        }
        LotteryPlay lottery = LotteryPlay.findById(id);
        lottery.setWangwang(wangwang);
        lottery.setMobile(mobile);
        lottery.save();
        renderText("");
    }
    
}
