
package models.visit;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.persistence.Column;
import javax.persistence.Entity;

import jdbcexecutorwrapper.JDBCMapLongIntExecutor;
import job.writter.VisitLogWritter.LogBean;
import models.updatetimestamp.updates.LinezingUpdateTs;
import models.user.User;
import models.user.UserIdNick;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Table;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.db.jpa.Model;
import play.jobs.Every;
import play.jobs.Job;
import result.TMResult;
import transaction.DBBuilder;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import utils.TBIpApi;
import utils.TBIpApi.IpDataBean;
import utils.TimeUtil;
import actions.listTaoBao.TBUrlManager;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.url.URLParser;
import com.ciaosir.client.utils.DateUtil;
import com.ciaosir.client.utils.NetworkUtil;

@Entity(name = LinezingRecord.TABLE_NAME)
@Table(appliesTo = LinezingRecord.TABLE_NAME,
        indexes = {
                //                @Index(name = "uniqueUv", columnNames = {
//                        "uv", "created"
//                }),
                @Index(name = "userIdDay", columnNames = {
                        "userId", "created", "uv"
                })
        }

)
public class LinezingRecord extends Model implements Comparable<LinezingRecord>, PolicySQLGenerator {

    static LinezingRecord _instance = new LinezingRecord();

    public static LinezingRecord getInstance() {
        return _instance;
    }

    static DBDispatcher dp = new DBDispatcher(DataSrc.RDS, _instance);

    private static final Logger log = LoggerFactory.getLogger(LinezingRecord.class);

    private static final String TAG = "LinezingRecord";

    public static final String TABLE_NAME = "linezing_record";

    public static class Status {

        //        public static int isTradeRealted = 8;

        public static int isFromBus = 16;

        public static int isFromTaobaoke = 32;

        public static int isFromMainSearch = 64;

    }

    /**
     * 本次session达成交易的tid（只在购物车确认页面和订单确认页面的记录中返回）
     */
    // public Long createdTid = Long.valueOf(0L);

    @Override
    public int compareTo(LinezingRecord o) {
        // TODO Auto-generated method stub
        return 0;
    }

    private LinezingRecord(Long userId, Long created, int status, String cookieIDString, long curItemId,
            String searchKey,
            long ip, int day, int hour) {
        super();
        this.userId = userId;
        this.created = created;
        this.status = status;
        this.curItemId = curItemId;
        this.searchKey = searchKey;
        this.ip = ip;
        this.day = day;
        this.hour = hour;
    }

    public LinezingRecord() {
    }

    public static void ensureLog(LogBean bean) {
        LinezingRecord.ensureLog(bean.getUrl(), bean.getIp(), bean.getCookieIdString(), bean.getUserId(), bean.getTs());
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

        new LinezingRecord(userId, timeMillis, Status.isFromBus, cookieString, itemId, key, ipLong, day, hour).save();
    }

    public static class LinzingBean {
        /**
         * [
        {
            "cnt": 207
        },
        [
            {
                "im": "",
                "is_baobei": 1,
                "search_key": "大童运动套装 女",
                "ip": "221.7.253.23",
                "url": "http://detail.tmall.com/item.htm?id=38655625793&ali_refid=a3_420434_1006:1106111762:6:%B4%F3%CD%AF%D4%CB%B6%AF%CC%D7%D7%B0+%C5%AE:4bf1ec176a0df09a4bbab0b587bf83b5&ali_trackid=1_4bf1ec176a0df09a4bbab0b587bf83b5",
                "uv": "vv4cMtsZ63wZmm9DP3Li4Bbx4crZ0uV+fGuTyY+INHjCrHCNtsBXgA==",
                "location_id": 265992,
                "ref": "http://s.taobao.com/search?bcoffset=1&tab=all&filter=reserve_price%5B60%2C%5D&fs=0&stats_click=search_radio_all%3A1&initiative_id=staobaoz_20140514&q=%D4%CB%B6%AF%CC%D7%D7%B0%C5%AE%CF%C4+%B4%F3%CD%AF&promote=0&s=240",
                "is_ref_p4p": "p4p",
                "location_name": "广西梧州市",
                "day": "2014-05-14",
                "ref_type": "直通车",
                "title": "童装女童中小大童夏装套装儿童短袖T恤运动套装宝宝韩版2014新款-tmall.com天猫",
                "url_baobei_id": "38655625793",
                "uv_return": 0,
                "log_time": "18:38:33",
                "uv_no": 105,
                "src_id": 11
            },
            {
                "im": "",
                "is_baobei": 1,
                "search_key": "女大童夏装新款套装",
                "ip": "112.5.234.88",
                "url": "http://detail.tmall.com/item.htm?id=38655625793&ali_refid=a3_420435_1006:1106111762:6:%C5%AE%B4%F3%CD%AF%CF%C4%D7%B0%D0%C2%BF%EE%CC%D7%D7%B0:c7e5426dd4e6270df9d2ae2423b200cb&ali_trackid=1_c7e5426dd4e6270df9d2ae2423b200cb",
                "uv": "vv4cMtsZ63wZmm9DP3Li4Bbx4crZ0uV+fGuTyY+INHjmgpH2tgoydQ==",
                "location_id": 269064,
                "ref": "http://s.taobao.com/search?&initiative_id=tbindexz_20140514&sourceId=tb.index&search_type=item&ssid=s5-e&commend=all&q=%C5%AE%B4%F3%CD%AF%CC%D7%D7%B0%CF%C4%D7%B0%D0%C2%BF%EE&suggest=0_2&wq=%C5%AE%B4%F3%CD%AF%CC%D7&suggest_query=%C5%AE%B4%F3%CD%AF%CC%D7&source=suggest",
                "is_ref_p4p": "p4p",
                "location_name": "福建福州市",
                "day": "2014-05-14",
                "ref_type": "直通车",
                "title": "童装女童中小大童夏装套装儿童短袖T恤运动套装宝宝韩版2014新款-tmall.com天猫",
                "url_baobei_id": "38655625793",
                "uv_return": 0,
                "log_time": "18:37:49",
                "uv_no": 104,
                "src_id": 11
            },
            {
                "im": "",
                "is_baobei": 1,
                "search_key": "短袖 女童 套装",
                "ip": "121.60.86.90",
                "url": "http://detail.tmall.com/item.htm?id=38655625793&ali_refid=a3_420434_1006:1106111762:6:%B6%CC%D0%E4+%C5%AE%CD%AF+%CC%D7%D7%B0:7d9b233f9e7bb55f7e0e4066dddde9bc&ali_trackid=1_7d9b233f9e7bb55f7e0e4066dddde9bc",
                "uv": "vv4cMtsZ63wZmm9DP3Li4Bbx4crZ0uV+fGuTyY+INHjcMLmZaxZeZg==",
                "location_id": 268041,
                "ref": "http://s.taobao.com/search?atab=stats_click%3Dsearch_radio_all%253A1&js=1&initiative_id=staobaoz_20140514&q=%C5%AE%CD%AF%CC%D7%D7%B0%B6%CC%D0%E4&suggest=0_5&wq=%C5%AE%CD%AF%CC%D7%D7%B0&suggest_query=%C5%AE%CD%AF%CC%D7%D7%B0&source=suggest&tab=all&promote=0&bcoffset=-4&s=800",
                "is_ref_p4p": "p4p",
                "location_name": "湖北武汉市",
                "day": "2014-05-14",
                "ref_type": "直通车",
                "title": "童装女童中小大童夏装套装儿童短袖T恤运动套装宝宝韩版2014新款-tmall.com天猫",
                "url_baobei_id": "38655625793",
                "uv_return": 0,
                "log_time": "18:31:43",
                "uv_no": 103,
                "src_id": 11
            },
            {
                "im": "",
                "is_baobei": 1,
                "search_key": "羽绒服内胆 儿童",
                "ip": "14.111.31.98",
                "url": "http://detail.tmall.com/item.htm?id=27185228209&ali_refid=a3_419096_1006:1106111762:6:%D3%F0%C8%DE%B7%FE%C4%DA%B5%A8+%B6%F9%CD%AF:d5be94d13fcc63c9e53313b4ec1062e5&ali_trackid=1_d5be94d13fcc63c9e53313b4ec1062e5&scm=1029.newlist-0.bts3.50035372",
                "uv": "vv4cMtsZ63wZmm9DP3Li4Bbx4crZ0uV+fGuTyY+INHhd7oKNNPVMkA==",
                "location_id": 270081,
                "ref": "http://list.taobao.com/itemlist/baby.htm?cat=50035372&spercent=95&user_type=0&isprepay=1&gobaby=1&random=false&as=0&viewIndex=1&commend=all&atype=b&s=1824&style=grid&same_info=1&tid=0&isnew=2&olu=yes&mSelect=false&_input_charset=utf-8",
                "is_ref_p4p": "p4p",
                "location_name": "重庆",
                "day": "2014-05-14",
                "ref_type": "直通车",
                "title": "反季促销秋冬季童装新款促销女童中小童正品加厚中长款儿童羽绒服-tmall.com天猫",
                "url_baobei_id": "27185228209",
                "uv_return": 0,
                "log_time": "18:19:27",
                "uv_no": 94,
                "src_id": 11
            },
            {
                "im": "",
                "is_baobei": 1,
                "search_key": "羽绒服内胆 儿童",
                "ip": "14.111.31.98",
                "url": "http://detail.tmall.com/item.htm?id=27185228209&ali_refid=a3_419096_1006:1106111762:6:%D3%F0%C8%DE%B7%FE%C4%DA%B5%A8+%B6%F9%CD%AF:38940698aeabb0462a350e76271948c7&ali_trackid=1_38940698aeabb0462a350e76271948c7&scm=1029.newlist-0.bts3.50035372",
                "uv": "vv4cMtsZ63wZmm9DP3Li4Bbx4crZ0uV+fGuTyY+INHhd7oKNNPVMkA==",
                "location_id": 270081,
                "ref": "http://list.taobao.com/itemlist/baby.htm?cat=50035372&spercent=95&user_type=0&isprepay=1&gobaby=1&random=false&as=0&viewIndex=1&commend=all&atype=b&s=1824&style=grid&same_info=1&tid=0&isnew=2&olu=yes&mSelect=false&_input_charset=utf-8",
                "is_ref_p4p": "p4p",
                "location_name": "重庆",
                "day": "2014-05-14",
                "ref_type": "直通车",
                "title": "反季促销秋冬季童装新款促销女童中小童正品加厚中长款儿童羽绒服-tmall.com天猫",
                "url_baobei_id": "27185228209",
                "uv_return": 0,
                "log_time": "18:17:58",
                "uv_no": 94,
                "src_id": 11
            },
            {
                "im": "",
                "is_baobei": 0,
                "search_key": "",
                "ip": "182.123.68.50",
                "url": "http://rbmy.tmall.com",
                "uv": "vv4cMtsZ63wZmm9DP3Li4Bbx4crZ0uV+fGuTyY+INHiynLCjpBcUbQ==",
                "location_id": 267274,
                "ref": "http://detail.tmall.com/item.htm?id=38655625793&ali_refid=a3_420435_1006:1106111762:6:%CD%AF%D7%B0%B4%F3%C5%AE%CD%AF%CF%C4%D7%B02014:60c8bd86ded6869fb22cb8ad3595175e&ali_trackid=1_60c8bd86ded6869fb22cb8ad3595175e",
                "is_ref_p4p": "common",
                "location_name": "河南新乡市",
                "day": "2014-05-14",
                "ref_type": "店内浏览",
                "title": "本店首页",
                "url_baobei_id": "first",
                "uv_return": 0,
                "log_time": "18:03:23",
                "uv_no": 102,
                "src_id": 100
            },
            {
                "im": "",
                "is_baobei": 1,
                "search_key": "童装大女童夏装2014",
                "ip": "182.123.68.50",
                "url": "http://detail.tmall.com/item.htm?id=38655625793&ali_refid=a3_420435_1006:1106111762:6:%CD%AF%D7%B0%B4%F3%C5%AE%CD%AF%CF%C4%D7%B02014:60c8bd86ded6869fb22cb8ad3595175e&ali_trackid=1_60c8bd86ded6869fb22cb8ad3595175e",
                "uv": "vv4cMtsZ63wZmm9DP3Li4Bbx4crZ0uV+fGuTyY+INHiynLCjpBcUbQ==",
                "location_id": 267274,
                "ref": "http://s.taobao.com/search?initiative_id=tbindexz_20140514&sourceId=tb.index&search_type=item&ssid=s5-e&commend=all&q=%B8%F6%D0%D4%C5%AE%CD%AF%D7%B0%CF%C4&suggest=history_1&source=suggest&tab=all&promote=0&bcoffset=-4&s=3640",
                "is_ref_p4p": "p4p",
                "location_name": "河南新乡市",
                "day": "2014-05-14",
                "ref_type": "直通车",
                "title": "童装女童中小大童夏装套装儿童短袖T恤运动套装宝宝韩版2014新款-tmall.com天猫",
                "url_baobei_id": "38655625793",
                "uv_return": 0,
                "log_time": "18:02:42",
                "uv_no": 102,
                "src_id": 11
            },
            {
                "im": "",
                "is_baobei": 1,
                "search_key": "",
                "ip": "111.193.79.167",
                "url": "http://detail.tmall.com/item.htm?id=27185228209&_u=e7ie9glf9a4&is_b=1&cat_id=2&q=&rn=e156bc59663814ae4cc91f8e76935362",
                "uv": "vv4cMtsZ63wZmm9DP3Li4Bbx4crZ0uV+fGuTyY+INHglj0nL6BWpTQ==",
                "location_id": 263425,
                "ref": "http://list.tmall.com/search_shopitem.htm?user_id=1752402391&cat=2&oq=%C8%CA%B2%A9%C4%B8%D3%A4%D7%A8%D3%AA%B5%EA&ds=1&stype=search",
                "is_ref_p4p": "common",
                "location_name": "北京",
                "day": "2014-05-14",
                "ref_type": "天猫类目",
                "title": "反季促销秋冬季童装新款促销女童中小童正品加厚中长款儿童羽绒服-tmall.com天猫",
                "url_baobei_id": "27185228209",
                "uv_return": 1,
                "log_time": "17:55:44",
                "uv_no": 10,
                "src_id": 35
            },
            {
                "im": "",
                "is_baobei": 1,
                "search_key": "",
                "ip": "111.193.79.167",
                "url": "http://detail.tmall.com/item.htm?id=38655625793&_u=e7ie9gl5bd9&is_b=1&cat_id=2&q=&rn=e156bc59663814ae4cc91f8e76935362",
                "uv": "vv4cMtsZ63wZmm9DP3Li4Bbx4crZ0uV+fGuTyY+INHglj0nL6BWpTQ==",
                "location_id": 263425,
                "ref": "http://list.tmall.com/search_shopitem.htm?user_id=1752402391&cat=2&oq=%C8%CA%B2%A9%C4%B8%D3%A4%D7%A8%D3%AA%B5%EA&ds=1&stype=search",
                "is_ref_p4p": "common",
                "location_name": "北京",
                "day": "2014-05-14",
                "ref_type": "天猫类目",
                "title": "童装女童中小大童夏装套装儿童短袖T恤运动套装宝宝韩版2014新款-tmall.com天猫",
                "url_baobei_id": "38655625793",
                "uv_return": 1,
                "log_time": "17:54:55",
                "uv_no": 10,
                "src_id": 35
            },
            {
                "im": "",
                "is_baobei": 1,
                "search_key": "童装女童2014潮夏装",
                "ip": "113.64.242.144",
                "url": "http://detail.tmall.com/item.htm?id=38655625793&ali_refid=a3_420434_1006:1106111762:6:%CD%AF%D7%B0%C5%AE%CD%AF2014%B3%B1%CF%C4%D7%B0:1fcc84571f8135a83c43158d52640115&ali_trackid=1_1fcc84571f8135a83c43158d52640115",
                "uv": "vv4cMtsZ63wZmm9DP3Li4Bbx4crZ0uV+fGuTyY+INHjGFQsnz8ggaA==",
                "location_id": 265734,
                "ref": "http://s.taobao.com/search?atab=stats_click%3Dsearch_radio_all%253A1&js=1&initiative_id=staobaoz_20140514&q=%CD%AF%D7%B0%C5%AE%CD%AF2014%B3%B1%CF%C4%D7%B0&suggest=0_2&wq=%CD%AF%D7%B0%C5%AE%CD%AF2014&suggest_query=%CD%AF%D7%B0%C5%AE%CD%AF2014&source=suggest&tab=all&promote=0&bcoffset=-4&s=120",
                "is_ref_p4p": "p4p",
                "location_name": "广东广州市",
                "day": "2014-05-14",
                "ref_type": "直通车",
                "title": "童装女童中小大童夏装套装儿童短袖T恤运动套装宝宝韩版2014新款-tmall.com天猫",
                "url_baobei_id": "38655625793",
                "uv_return": 0,
                "log_time": "17:52:18",
                "uv_no": 101,
                "src_id": 11
            },
            {
                "im": "",
                "is_baobei": 1,
                "search_key": "",
                "ip": "27.155.134.43",
                "url": "http://detail.tmall.com/item.htm?id=27185228209&rn=&acm=03054.1003.1.54951&uuid=iLGC6UoB&abtest=_AB-LR32-PR32&scm=1003.1.03054.ITEM_27185228209_54951&pos=1",
                "uv": "vv4cMtsZ63wZmm9DP3Li4Bbx4crZ0uV+fGuTyY+INHjADYu/8Pv9VQ==",
                "location_id": 269064,
                "ref": "http://detail.tmall.com/item.htm?id=38655625793&ali_refid=a3_420841_1006:1106111762:6:%B4%F3%CD%AF+%C5%AE%BF%EE+%CC%D7%D7%B0:930a1ece0fedd4ed8713baeb63e2d1fe&ali_trackid=1_930a1ece0fedd4ed8713baeb63e2d1fe",
                "is_ref_p4p": "common",
                "location_name": "福建福州市",
                "day": "2014-05-14",
                "ref_type": "店内浏览",
                "title": "反季促销秋冬季童装新款促销女童中小童正品加厚中长款儿童羽绒服-tmall.com天猫",
                "url_baobei_id": "27185228209",
                "uv_return": 0,
                "log_time": "17:51:28",
                "uv_no": 100,
                "src_id": 100
            },
            {
                "im": "",
                "is_baobei": 1,
                "search_key": "大童 女款 套装",
                "ip": "27.155.134.43",
                "url": "http://detail.tmall.com/item.htm?id=38655625793&ali_refid=a3_420841_1006:1106111762:6:%B4%F3%CD%AF+%C5%AE%BF%EE+%CC%D7%D7%B0:930a1ece0fedd4ed8713baeb63e2d1fe&ali_trackid=1_930a1ece0fedd4ed8713baeb63e2d1fe",
                "uv": "vv4cMtsZ63wZmm9DP3Li4Bbx4crZ0uV+fGuTyY+INHjADYu/8Pv9VQ==",
                "location_id": 269064,
                "ref": "http://re.taobao.com/search?keyword=%C5%AE%B4%F3%CD%AF&catid=50010540&refpid=430016_1007&propertyid=&refpos=&frcatid=50095672&tp=7&buckid=1&ismall=&gprice=&loc=&clk1=70f94bf3da20e79f58c4002a827b52aa&back=fp_midtop%253D0%2526firstpage_pushleft%253D0&dpback=&isinner=1&yp4p_page=2&posid=8",
                "is_ref_p4p": "p4p",
                "location_name": "福建福州市",
                "day": "2014-05-14",
                "ref_type": "直通车",
                "title": "童装女童中小大童夏装套装儿童短袖T恤运动套装宝宝韩版2014新款-tmall.com天猫",
                "url_baobei_id": "38655625793",
                "uv_return": 0,
                "log_time": "17:51:17",
                "uv_no": 100,
                "src_id": 11
            },
            {
                "im": "",
                "is_baobei": 1,
                "search_key": "",
                "ip": "5.251.22.23",
                "url": "http://detail.tmall.com/item.htm?id=27179552385",
                "uv": "vv4cMtsZ63wZmm9DP3Li4Bbx4crZ0uV+fGuTyY+INHikqUd+AbL4uw==",
                "location_id": 3277057,
                "ref": "http://s.taobao.com/search?tab=all&q=%D3%F0%C8%DE%B7%FE&cps=yes&promote=0&filterFineness=2&cat=50035372&navlog=compass&from=compass&ppath=24477:20532",
                "is_ref_p4p": "common",
                "location_name": "哈萨克斯坦",
                "day": "2014-05-14",
                "ref_type": "淘宝搜索",
                "title": "2013新款博士神童冬季童装正品儿童羽绒服男童中小码卡通外套-tmall.com天猫",
                "url_baobei_id": "27179552385",
                "uv_return": 0,
                "log_time": "17:51:09",
                "uv_no": 99,
                "src_id": 20
            },
            {
                "im": "",
                "is_baobei": 1,
                "search_key": "女 大童休闲套装",
                "ip": "113.9.110.220",
                "url": "http://detail.tmall.com/item.htm?id=38655625793&ali_refid=a3_420434_1006:1106111762:6:%C5%AE+%B4%F3%CD%AF%D0%DD%CF%D0%CC%D7%D7%B0:836f6720bb800a968d13b829be27ddbc&ali_trackid=1_836f6720bb800a968d13b829be27ddbc",
                "uv": "vv4cMtsZ63wZmm9DP3Li4Bbx4crZ0uV+fGuTyY+INHje2R62cQckKA==",
                "location_id": 271110,
                "ref": "http://s.taobao.com/search?initiative_id=staobaoz_20140514&js=1&atab=stats_click%3Dsearch_radio_all%253A1&q=%C5%AE%B4%F3%CD%AF%D0%DD%CF%D0%CC%D7%D7%B0",
                "is_ref_p4p": "p4p",
                "location_name": "黑龙江哈尔滨市",
                "day": "2014-05-14",
                "ref_type": "直通车",
                "title": "童装女童中小大童夏装套装儿童短袖T恤运动套装宝宝韩版2014新款-tmall.com天猫",
                "url_baobei_id": "38655625793",
                "uv_return": 0,
                "log_time": "17:46:37",
                "uv_no": 98,
                "src_id": 11
            },
            {
                "im": "",
                "is_baobei": 1,
                "search_key": "大童夏季休闲套装女",
                "ip": "117.29.64.66",
                "url": "http://detail.tmall.com/item.htm?id=38655625793&ali_refid=a3_420435_1006:1106111762:6:%B4%F3%CD%AF%CF%C4%BC%BE%D0%DD%CF%D0%CC%D7%D7%B0%C5%AE:29df163d29cf9690f2d13ac4bedb921b&ali_trackid=1_29df163d29cf9690f2d13ac4bedb921b",
                "uv": "vv4cMtsZ63wZmm9DP3Li4Bbx4crZ0uV+fGuTyY+INHj0or41DobF5Q==",
                "location_id": 269064,
                "ref": "http://s.taobao.com/search?q=%C5%AE%CD%AF%B8%F6%D0%D4%CF%C4%BC%BE%CC%D7%D7%B0&searcy_type=item&s_from=newHeader&ssid=s5-e&search=y&initiative_id=shopz_20140513&suggest=0_8&wq=%C5%AE%CD%AF%B8%F6%D0%D4&suggest_query=%C5%AE%CD%AF%B8%F6%D0%D4&tab=all&promote=0&bcoffset=-8&s=240",
                "is_ref_p4p": "p4p",
                "location_name": "福建福州市",
                "day": "2014-05-14",
                "ref_type": "直通车",
                "title": "童装女童中小大童夏装套装儿童短袖T恤运动套装宝宝韩版2014新款-tmall.com天猫",
                "url_baobei_id": "38655625793",
                "uv_return": 0,
                "log_time": "17:42:20",
                "uv_no": 97,
                "src_id": 11
            },
            {
                "im": "",
                "is_baobei": 1,
                "search_key": "",
                "ip": "220.181.165.134",
                "url": "http://detail.tmall.com/item.htm?id=19960361230",
                "uv": "vv4cMtsZ63wZmm9DP3Li4Bbx4crZ0uV+fGuTyY+INHgrSjxPlY3GFQ==",
                "location_id": 263425,
                "ref": "access",
                "is_ref_p4p": "common",
                "location_name": "北京",
                "day": "2014-05-14",
                "ref_type": "直接访问",
                "title": "2013新款秋冬正品反季儿童羽绒服女童中大码韩版百搭外套-tmall.com天猫",
                "url_baobei_id": "19960361230",
                "uv_return": 0,
                "log_time": "17:42:04",
                "uv_no": 96,
                "src_id": 6
            },
            {
                "im": "",
                "is_baobei": 1,
                "search_key": "羽绒服儿童装",
                "ip": "60.191.104.130",
                "url": "http://detail.tmall.com/item.htm?id=27185228209&ali_refid=a3_420434_1006:1106111762:6:%D3%F0%C8%DE%B7%FE%B6%F9%CD%AF%D7%B0:fed3d69f25bad7084b085443ac5ac680&ali_trackid=1_fed3d69f25bad7084b085443ac5ac680",
                "uv": "vv4cMtsZ63wZmm9DP3Li4Bbx4crZ0uV+fGuTyY+INHiOBd07Bu8B+g==",
                "location_id": 267526,
                "ref": "http://s.taobao.com/search?promote=0&sort=sale-desc&initiative_id=staobaoz_20140514&tab=all&q=%CD%AF%D7%B0+%D3%F0%C8%DE%B7%FE&stats_click=search_radio_all%3A1",
                "is_ref_p4p": "p4p",
                "location_name": "浙江杭州市",
                "day": "2014-05-14",
                "ref_type": "直通车",
                "title": "反季促销秋冬季童装新款促销女童中小童正品加厚中长款儿童羽绒服-tmall.com天猫",
                "url_baobei_id": "27185228209",
                "uv_return": 0,
                "log_time": "17:38:45",
                "uv_no": 95,
                "src_id": 11
            },
            {
                "im": "",
                "is_baobei": 1,
                "search_key": "羽绒服内胆 儿童",
                "ip": "14.111.31.98",
                "url": "http://detail.tmall.com/item.htm?id=27185228209&ali_refid=a3_419253_1006:1106111762:6:%D3%F0%C8%DE%B7%FE%C4%DA%B5%A8+%B6%F9%CD%AF:50cdecc0953064ae88eb3be02ebe633c&ali_trackid=1_50cdecc0953064ae88eb3be02ebe633c&scm=1029.newlist-0.bts3.50035372",
                "uv": "vv4cMtsZ63wZmm9DP3Li4Bbx4crZ0uV+fGuTyY+INHhd7oKNNPVMkA==",
                "location_id": 270081,
                "ref": "http://list.taobao.com/itemlist/baby.htm?cat=50035372&spercent=95&isprepay=1&user_type=0&gobaby=1&random=false&as=0&viewIndex=1&commend=all&atype=b&style=grid&same_info=1&olu=yes&isnew=2&mSelect=false&tid=0&_input_charset=utf-8",
                "is_ref_p4p": "p4p",
                "location_name": "重庆",
                "day": "2014-05-14",
                "ref_type": "直通车",
                "title": "反季促销秋冬季童装新款促销女童中小童正品加厚中长款儿童羽绒服-tmall.com天猫",
                "url_baobei_id": "27185228209",
                "uv_return": 0,
                "log_time": "17:37:34",
                "uv_no": 94,
                "src_id": 11
            },
            {
                "im": "",
                "is_baobei": 1,
                "search_key": "大童 女款 套装",
                "ip": "61.178.118.10",
                "url": "http://detail.tmall.com/item.htm?id=38655625793&ali_refid=a3_420841_1006:1106111762:6:%B4%F3%CD%AF+%C5%AE%BF%EE+%CC%D7%D7%B0:1ecaf5bbf54dea7817282fe66889beeb&ali_trackid=1_1ecaf5bbf54dea7817282fe66889beeb",
                "uv": "vv4cMtsZ63wZmm9DP3Li4Bbx4crZ0uV+fGuTyY+INHhofgyjy6Ah8Q==",
                "location_id": 268803,
                "ref": "http://re.taobao.com/search?keyword=%C5%AE%B4%F3%CD%AF&catid=50010540&refpid=420986_1007&propertyid=&refpos=&frcatid=50095672&tp=7&buckid=2&ismall=&gprice=&loc=&clk1=f3c5d2b8b1276bfc327ce0b05ff3fbcb&back=fp_midtop%253D0%2526firstpage_pushleft%253D0&dpback=&isinner=1&yp4p_page=2&posid=8",
                "is_ref_p4p": "p4p",
                "location_name": "甘肃兰州市",
                "day": "2014-05-14",
                "ref_type": "直通车",
                "title": "童装女童中小大童夏装套装儿童短袖T恤运动套装宝宝韩版2014新款-tmall.com天猫",
                "url_baobei_id": "38655625793",
                "uv_return": 0,
                "log_time": "17:26:39",
                "uv_no": 93,
                "src_id": 11
            },
            {
                "im": "",
                "is_baobei": 1,
                "search_key": "",
                "ip": "116.21.103.85",
                "url": "http://detail.tmall.com/item.htm?id=19579762220&rn=&acm=03054.1003.1.54951&uuid=5MnvJmAf&abtest=_AB-LR32-PR32&scm=1003.1.03054.ITEM_19579762220_54951&pos=1",
                "uv": "vv4cMtsZ63wZmm9DP3Li4Bbx4crZ0uV+fGuTyY+INHi8CP+ky97d3A==",
                "location_id": 265734,
                "ref": "http://detail.tmall.com/item.htm?id=19741397879",
                "is_ref_p4p": "common",
                "location_name": "广东广州市",
                "day": "2014-05-14",
                "ref_type": "店内浏览",
                "title": "2013新款博士神童正品反季男童中小童卡通小汽车儿童羽绒服外套-tmall.com天猫",
                "url_baobei_id": "19579762220",
                "uv_return": 0,
                "log_time": "17:23:52",
                "uv_no": 92,
                "src_id": 100
            },
            {
                "im": "",
                "is_baobei": 1,
                "search_key": "",
                "ip": "116.21.103.85",
                "url": "http://detail.tmall.com/item.htm?id=22183247551&rn=&acm=03054.1003.1.54951&uuid=5MnvJmAf&abtest=_AB-LR32-PR32&scm=1003.1.03054.ITEM_22183247551_54951&pos=3",
                "uv": "vv4cMtsZ63wZmm9DP3Li4Bbx4crZ0uV+fGuTyY+INHi8CP+ky97d3A==",
                "location_id": 265734,
                "ref": "http://detail.tmall.com/item.htm?id=19741397879",
                "is_ref_p4p": "common",
                "location_name": "广东广州市",
                "day": "2014-05-14",
                "ref_type": "店内浏览",
                "title": "2013新款博士神童正品反季儿童羽绒服女童中小童时尚韩版外套-tmall.com天猫",
                "url_baobei_id": "22183247551",
                "uv_return": 0,
                "log_time": "17:23:44",
                "uv_no": 92,
                "src_id": 100
            },
            {
                "im": "",
                "is_baobei": 1,
                "search_key": "",
                "ip": "116.21.103.85",
                "url": "http://detail.tmall.com/item.htm?id=19741397879",
                "uv": "vv4cMtsZ63wZmm9DP3Li4Bbx4crZ0uV+fGuTyY+INHi8CP+ky97d3A==",
                "location_id": 265734,
                "ref": "http://rbmy.tmall.com/index.htm",
                "is_ref_p4p": "common",
                "location_name": "广东广州市",
                "day": "2014-05-14",
                "ref_type": "店内浏览",
                "title": "冬季童装2013新款反季促销男童中大童时尚亮面儿童羽绒服秋冬外套-tmall.com天猫",
                "url_baobei_id": "19741397879",
                "uv_return": 0,
                "log_time": "17:23:21",
                "uv_no": 92,
                "src_id": 100
            },
            {
                "im": "",
                "is_baobei": 1,
                "search_key": "",
                "ip": "116.21.103.85",
                "url": "http://detail.tmall.com/item.htm?id=19796538794",
                "uv": "vv4cMtsZ63wZmm9DP3Li4Bbx4crZ0uV+fGuTyY+INHi8CP+ky97d3A==",
                "location_id": 265734,
                "ref": "http://rbmy.tmall.com/index.htm",
                "is_ref_p4p": "common",
                "location_name": "广东广州市",
                "day": "2014-05-14",
                "ref_type": "店内浏览",
                "title": "【会员购】正品小猪吧拉反季促销男童中小童卡通时尚儿童羽绒服-tmall.com天猫",
                "url_baobei_id": "19796538794",
                "uv_return": 0,
                "log_time": "17:23:08",
                "uv_no": 92,
                "src_id": 100
            },
            {
                "im": "",
                "is_baobei": 1,
                "search_key": "",
                "ip": "116.21.103.85",
                "url": "http://detail.tmall.com/item.htm?id=19796538794",
                "uv": "vv4cMtsZ63wZmm9DP3Li4Bbx4crZ0uV+fGuTyY+INHi8CP+ky97d3A==",
                "location_id": 265734,
                "ref": "http://rbmy.tmall.com/index.htm",
                "is_ref_p4p": "common",
                "location_name": "广东广州市",
                "day": "2014-05-14",
                "ref_type": "店内浏览",
                "title": "【会员购】正品小猪吧拉反季促销男童中小童卡通时尚儿童羽绒服-tmall.com天猫",
                "url_baobei_id": "19796538794",
                "uv_return": 0,
                "log_time": "17:22:36",
                "uv_no": 92,
                "src_id": 100
            },
            {
                "im": "",
                "is_baobei": 0,
                "search_key": "",
                "ip": "116.21.103.85",
                "url": "http://rbmy.tmall.com/index.htm",
                "uv": "vv4cMtsZ63wZmm9DP3Li4Bbx4crZ0uV+fGuTyY+INHi8CP+ky97d3A==",
                "location_id": 265734,
                "ref": "http://detail.tmall.com/item.htm?id=27185228209&rn=&acm=03054.1003.1.54951&uuid=Zq6P4nXk&abtest=_AB-LR32-PR32&scm=1003.1.03054.ITEM_27185228209_54951&pos=1",
                "is_ref_p4p": "common",
                "location_name": "广东广州市",
                "day": "2014-05-14",
                "ref_type": "店内浏览",
                "title": "首页",
                "url_baobei_id": "10e6e172b5a52f1e446cf47532563591",
                "uv_return": 0,
                "log_time": "17:21:36",
                "uv_no": 92,
                "src_id": 100
            }
        ]
        ]
         * @throws JSONException 
         * @throws ParseException 
         */

    }

    public static LinezingRecord parseFromLZ(User user, JSONObject json) throws JSONException, ParseException {
        LinezingRecord model = new LinezingRecord();
        model.userId = user.getId();

        if (json.has("src_id")) {
            model.srcId = json.getInt("src_id");
        }

        if (json.has("uv_no")) {
            model.uvNo = json.getInt("uv_no");
        }
        if (json.has("is_ref_p4p")) {
            // 是否为直通车, common  or  p4p
            model.p4p = json.getString("is_ref_p4p");
        }

        if (json.has("location_name")) {
            model.locationName = json.getString("location_name");
        }
        if (json.has("ref_type")) {
            model.refType = json.getString("ref_type");
        }

        if (json.has("day") && json.has("log_time")) {
            String date = json.getString("day") + " " + json.getString("log_time");
            Date actualDate = DateUtil.genYMSHms().parse(date);

            Calendar instance = Calendar.getInstance();
            instance.setTime(actualDate);

            model.created = actualDate.getTime();
            model.day = DateUtil.formDay(actualDate.getTime());
            model.hour = instance.get(Calendar.HOUR_OF_DAY);
        } else {
            log.error("no correspond date");
        }

        if (json.has("ip")) {
            log.info("[ip:]" + json.getString("ip"));
            model.ip = NetworkUtil.getIpNum(json.getString("ip"));
        }
        if (json.has("uv")) {
            model.uv = json.getString("uv");
        }
        if (json.has("search_key")) {
            model.searchKey = json.getString("search_key");
        }
        if (json.has("url")) {
            model.toUrl = json.getString("url");
        }
        if (json.has("p4p")) {
            model.p4p = json.getString("p4p");
        }

        if (json.has("ref")) {
            model.fromUrl = json.getString("ref");
        }

        model.formalize();
        return model;
    }

    private String formIpFromNum(long num) {
        String d = String.valueOf(num % 256);
        num = num / 256;
        String c = String.valueOf(num % 256);
        num = num / 256;

        String b = String.valueOf(num % 256);
        num = num / 256;
        String a = String.valueOf(num % 256);
        return String.format("%s.%s.%s.%s", a, b, c, d);

    }

    public void formalize() {
        if (this.ip <= 0L) {
            log.warn("fail for no ip :" + this);
            return;
        }

        String ipStr = formIpFromNum(this.ip);
        IpDataBean bean = TBIpApi.parseIpToBean(ipStr);
        if (bean == null) {
//            return;
            this.province = TBIpApi.trimProvince(this.locationName);
            this.city = TBIpApi.trimCity(this.locationName);
            this.country = TBIpApi.trimCountry(this.locationName);
        } else {
            this.province = TBIpApi.trimProvince(bean.getProvince());
            this.city = TBIpApi.trimCity(bean.getCity());
            this.isp = TBIpApi.trimIsp(bean.getIsp());
            this.country = TBIpApi.trimCountry(bean.getCountry());
        }

        if (this.fromUrl != null) {
            if (fromUrl.contains("s.taobao.com")) {

            } else if (TBUrlManager.get().isTaobaoItemUrl(fromUrl)) {
                // TODO get the search key params..
                this.fromItemId = TBUrlManager.get().findItemId(fromUrl);
            }

        }

        if (this.toUrl != null && TBUrlManager.get().isTaobaoItemUrl(toUrl)) {
            this.curItemId = TBUrlManager.get().findItemId(toUrl);
        }

    }

    int srcId;

    @Column(columnDefinition = "varchar(31) default ''")
    String country;

    @Column(columnDefinition = "varchar(31) default ''")
    String province;

    @Column(columnDefinition = "varchar(31) default ''")
    String city;

    @Column(columnDefinition = "varchar(31) default ''")
    String isp;

    @Column(columnDefinition = "varchar(63) default ''")
    private String locationName;

    @Column(columnDefinition = "varchar(31) default ''")
    private String p4p;

    private int uvNo;

    @Column(name = "userId", nullable = false)
//    @Index(name = "tbUserId")
    public Long userId;

    /**
     * 日志产生时间
     */
//    @Index(name = "created")
    public Long created = 0L;

//    @Index(name = "hour")
    public int hour = 0;

//    @Index(name = "day")
    public int day = 0;

//    @Index(name = "uv")
    String uv;

    String refType;

    @Column(columnDefinition = " varchar(2046) default null")
    String fromUrl;

    @Column(columnDefinition = " varchar(2046) default null")
    String toUrl;

    public int status = 0;

    // /**
    // * 当前URL
    // */
    // @Column(length = MAX_URL_LENGTH, columnDefinition = "varchar(" +
    // MAX_URL_LENGTH + ")")
//    @Index(name = "itemId")

    long curItemId;

    long fromItemId;

    /**
     * 来源页面的URL
     */
    // @Column(length = MAX_URL_LENGTH, columnDefinition = "varchar(" +
    // MAX_URL_LENGTH + ")")

    /**
     * If this is an item form url, then this would be the item id or this would
     * be the url_derived hash id
     */

    /*
     *  直通车关键词,也可以是其他类型的关键词
     */
    @Index(name = "searchKey")
    public String searchKey;

    public long ip;

    public int getSrcId() {
        return srcId;
    }

    public void setSrcId(int srcId) {
        this.srcId = srcId;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getIsp() {
        return isp;
    }

    public void setIsp(String isp) {
        this.isp = isp;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getP4p() {
        return p4p;
    }

    public void setP4p(String p4p) {
        this.p4p = p4p;
    }

    public int getUvNo() {
        return uvNo;
    }

    public void setUvNo(int uvNo) {
        this.uvNo = uvNo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String getUv() {
        return uv;
    }

    public void setUv(String uv) {
        this.uv = uv;
    }

    public String getRefType() {
        return refType;
    }

    public void setRefType(String refType) {
        this.refType = refType;
    }

    public String getFromUrl() {
        return fromUrl;
    }

    public void setFromUrl(String fromUrl) {
        this.fromUrl = fromUrl;
    }

    public String getToUrl() {
        return toUrl;
    }

    public void setToUrl(String toUrl) {
        this.toUrl = toUrl;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getCurNumIid() {
        return curItemId;
    }

    public void setCurItemId(long curItemId) {
        this.curItemId = curItemId;
    }

    public long getFromItemId() {
        return fromItemId;
    }

    public void setFromItemId(long fromItemId) {
        this.fromItemId = fromItemId;
    }

    public String getSearchKey() {
        return searchKey;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    public long getIp() {
        return ip;
    }

    public void setIp(long ip) {
        this.ip = ip;
    }

    static DataSrc src = DataSrc.RDS;

    public static int findTodayRecords(User user) {
        int day = DateUtil.formDay(System.currentTimeMillis());
        String sql = " select count(*) from " + TABLE_NAME + genTableHashKey(user.getId())
                + " where userId = ? and day = ?";
        int count = JDBCBuilder.singleIntQuery(src, sql, user.getId(), day);
        return count;
    }

    /**
     * 放在别的表,效率会更高
     * @param user
     * @return
     */
    public static int findTodayUv(User user) {
//        int day = DateUtil.formDay(System.currentTimeMillis());

        String sql = " select count(distinct(uv)) from " + TABLE_NAME + genTableHashKey(user.getId())
                + " where userId = ? and created between  ? and ? ";
        int count = JDBCBuilder.singleIntQuery(src, sql, user.getId(), DateUtil.formCurrDate(),
                System.currentTimeMillis());
        return count;
    }

    public static Map<Long, Integer> findTodayItemPv(User user) {
        int day = DateUtil.formDay(System.currentTimeMillis());
        String sql = "select curItemId, count(id) from " + TABLE_NAME + genTableHashKey(user.getId())
                + "  where userId = ? and created between  ? and ? and curItemId > 0 group by curItemId";
        Map<Long, Integer> map = new JDBCMapLongIntExecutor(dp, sql, user.getId(), DateUtil.formCurrDate(),
                System.currentTimeMillis()).call();
        return map;
    }

    public static Map<Long, Integer> findTodayItemUv(User user) {
        int day = DateUtil.formDay(System.currentTimeMillis());
        String sql = "select curItemId, count(distinct(uv)) from " + TABLE_NAME + genTableHashKey(user.getId())
                + "  where userId = ? and created between  ? and ? and curItemId > 0 group by curItemId";
        Map<Long, Integer> map = new JDBCMapLongIntExecutor(dp, sql, user.getId(), DateUtil.formCurrDate(),
                System.currentTimeMillis()).call();
        return map;
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getTableHashKey() {
//        return String.valueOf(DBBuilder.genUserIdHashKey(this.id));
        if (Play.mode.isDev()) {
            return StringUtils.EMPTY;
        }
        return StringUtils.EMPTY;
    }

    public static String genTableHashKey(Long userId) {
//        return String.valueOf(DBBuilder.genUserIdHashKey(userId));
        if (Play.mode.isDev()) {
            return StringUtils.EMPTY;
        }
        return StringUtils.EMPTY;
    }

    @Override
    public String getIdColumn() {
        return "id";
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean jdbcSave() {
        if (this.exist()) {
            this.rawUpdate();
        } else {
            this.rawInsert();
        }
        return true;
    }

    @Override
    public String getIdName() {
        return "id";
    }

    static String selectSQL = "select srcId,country,province,city,isp,locationName,p4p,uvNo,userId," +
            "created,hour,day,uv,refType,fromUrl,toUrl,status,curItemId,fromItemId,searchKey,ip from  ";

    static String selectSQL_T1 = "select t1.srcId,t1.country,t1.province,t1.city,t1.isp,t1.locationName," +
            "t1.p4p,t1.uvNo,t1.userId,t1.created,t1.hour,t1.day,t1.uv,t1.refType,t1.fromUrl,t1.toUrl," +
            "t1.status,t1.curItemId,t1.fromItemId,t1.searchKey,t1.ip from  ";

    public boolean exist() {
        return dp.singleLongQuery(" select 1 from " + TABLE_NAME + genTableHashKey(userId)
                + " where uv = ? and created = ?", uv, created) > 0L;
    }

    public static boolean exist(Long userId, String uv, long created) {
        return dp.singleLongQuery(" select 1 from " + TABLE_NAME + genTableHashKey(userId)
                + " where uv = ? and created = ?", uv, created) > 0L;
    }

    public long rawInsert() {
        long id = dp
                .insert("insert into `linezing_record"
                        + genTableHashKey(this.userId)
                        + "`(`srcId`,`country`,`province`,`city`,`isp`,`locationName`,`p4p`,`uvNo`,`userId`,`created`,`hour`,`day`,`uv`,`refType`,`fromUrl`,`toUrl`,`status`,`curItemId`,`fromItemId`,`searchKey`,`ip`) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                        this.srcId, this.country, this.province, this.city, this.isp, this.locationName, this.p4p,
                        this.uvNo, this.userId, this.created, this.hour, this.day, this.uv, this.refType, this.fromUrl,
                        this.toUrl, this.status, this.curItemId, this.fromItemId, this.searchKey, this.ip);
        this.id = id;

        return id;
    }

    public long rawUpdate() {
        long updateNum = dp
                .update("update `linezing_record"
                        + genTableHashKey(this.userId)
                        + "` set  `srcId` = ?, `country` = ?, `province` = ?, `city` = ?, `isp` = ?, `locationName` = ?, `p4p` = ?, `uvNo` = ?, `userId` = ?, `created` = ?, `hour` = ?, `day` = ?, `uv` = ?, `refType` = ?, `fromUrl` = ?, `toUrl` = ?, `status` = ?, `curItemId` = ?, `fromItemId` = ?, `searchKey` = ?, `ip` = ? where `id` = ? ",
                        this.srcId, this.country, this.province, this.city, this.isp, this.locationName, this.p4p,
                        this.uvNo, this.userId, this.created, this.hour, this.day, this.uv, this.refType, this.fromUrl,
                        this.toUrl, this.status, this.curItemId, this.fromItemId, this.searchKey, this.ip, this.getId());
        return updateNum;
    }

    public LinezingRecord(ResultSet rs) throws SQLException {
        this.srcId = rs.getInt(1);
        this.country = rs.getString(2);
        this.province = rs.getString(3);
        this.city = rs.getString(4);
        this.isp = rs.getString(5);
        this.locationName = rs.getString(6);
        this.p4p = rs.getString(7);
        this.uvNo = rs.getInt(8);
        this.userId = rs.getLong(9);
        this.created = rs.getLong(10);
        this.hour = rs.getInt(11);
        this.day = rs.getInt(12);
        this.uv = rs.getString(13);
        this.refType = rs.getString(14);
        this.fromUrl = rs.getString(15);
        this.toUrl = rs.getString(16);
        this.status = rs.getInt(17);
        this.curItemId = rs.getLong(18);
        this.fromItemId = rs.getLong(19);
        this.searchKey = rs.getString(20);
        this.ip = rs.getLong(21);

    }

    public static class ListFetcher extends JDBCExecutor<List<LinezingRecord>> {
        public ListFetcher(String whereQuery, Object... params) {
            super(dp, whereQuery, params);
        }

        public ListFetcher(Long hashKeyId, String whereQuery, Object... params) {
//            super(false, true, whereQuery, params);
            super(dp, whereQuery, params);

            StringBuilder sb = new StringBuilder();
            sb.append("select srcId,country,province,city,isp,locationName,p4p,uvNo,userId,created,hour,day,uv,refType,fromUrl,toUrl,status,curItemId,fromItemId,searchKey,ip from ");
            sb.append(TABLE_NAME);
            sb.append(genTableHashKey(hashKeyId));
            sb.append(" where  true  ");
            if (!StringUtils.isBlank(whereQuery)) {
                sb.append(" and ");
                sb.append(whereQuery);
            }
            this.src = dp.getDataSrc(hashKeyId);
            this.query = sb.toString();

        }

        @Override
        public List<LinezingRecord> doWithResultSet(ResultSet rs) throws SQLException {
            List<LinezingRecord> list = new ArrayList<LinezingRecord>();
            while (rs.next()) {
                list.add(new LinezingRecord(rs));
            }
            return list;
        }
    }

    public static int count(Long hashKeyId, String whereQuery, Object... params) {
        StringBuilder sb = new StringBuilder();
        sb.append("select count(*) from ");
        sb.append(TABLE_NAME);
        sb.append(genTableHashKey(hashKeyId));
        sb.append(" where  true  ");
        if (!StringUtils.isBlank(whereQuery)) {
            sb.append(" and ");
            sb.append(whereQuery);
        }
        return (int) JDBCBuilder.singleLongQuery(dp.getDataSrc(hashKeyId), sb.toString(), params);

    }

    /**
     * 理论上来讲, uv key, + created  作为主键 是没有任何问题
     * @author depvelop
     */
    @Every("2s")
    public static class LinezingWritter extends Job {

        static Queue<LinezingWritterWrapper> queue = new ConcurrentLinkedQueue<LinezingWritterWrapper>();

        public void doJob() {
            LinezingWritterWrapper wrapper = null;

            while ((wrapper = queue.poll()) != null) {
                log.info("[poll wapper:]" + wrapper);
                JSONArray arr = wrapper.getArr();
                User user = wrapper.getUser();
                if (user == null) {
                    log.warn("no user for wrapper:");
                    continue;
                }

                long maxCreated = doForArr(user, arr);

                if (maxCreated > 0L) {
                    LinezingUpdateTs.updateLastItemModifedTime(user.getId(), maxCreated);
                }
            }
        }

        private long doForArr(User user, JSONArray arr) {
            log.info("[write user:]" + user.toIdNick() + "  with arr size:" + arr.length());

            long maxCreated = 0L;
            int length = arr.length();
            for (int i = 0; i < length; i++) {
                try {
                    JSONObject jsonObject = arr.getJSONObject(i);
                    LinezingRecord model = parseFromLZ(user, jsonObject);

                    /**
                     * 如果是一个用户同时并发访问页面的话 会有问题,不过问题也不大
                     */
                    if (model.exist()) {
                        log.warn("exist for uv:" + model.getUv() + " created :"
                                + DateUtil.formDateForLog(model.getCreated()));
//                        continue;
                    } else {
                        model.rawInsert();
                    }
                    log.info("[write mode:]" + model);

                    Long createdTime = model.getCreated();
                    if (createdTime != null && createdTime > maxCreated) {
                        maxCreated = createdTime;
                    }

                } catch (JSONException e) {
                    log.warn(e.getMessage(), e);
                } catch (ParseException e) {
                    log.warn(e.getMessage(), e);
                }
            }
            return maxCreated;
        }

        public static void addQueue(User user, JSONArray arr) {
            queue.add(new LinezingWritterWrapper(user, arr));
        }
    }

    static class LinezingWritterWrapper implements Serializable {
        private static final long serialVersionUID = -6783885164952528230L;

        User user;

        long ts;

        JSONArray arr;

        private LinezingWritterWrapper(User user, JSONArray arr) {
            super();
            this.arr = arr;
            this.ts = System.currentTimeMillis();
            this.user = user;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public long getTs() {
            return ts;
        }

        public void setTs(long ts) {
            this.ts = ts;
        }

        public JSONArray getArr() {
            return arr;
        }

        public void setArr(JSONArray arr) {
            this.arr = arr;
        }

        @Override
        public String toString() {
            return "LinezingWritterWrapper [user=" + user + ", ts=" + ts + ", arr=" + arr.length() + "]";
        }

    }

    @Override
    public String toString() {
        return "LinezingRecord [srcId=" + srcId + ", country=" + country + ", province=" + province + ", city=" + city
                + ", isp=" + isp + ", locationName=" + locationName + ", p4p=" + p4p + ", uvNo=" + uvNo + ", userId="
                + userId + ", created=" + DateUtil.formDateForLog(created) + ", hour=" + hour + ", day=" + day
                + ", uv=" + uv + ", refType=" + refType + ", fromUrl=" + fromUrl + ", toUrl=" + toUrl + ", status="
                + status + ", curItemId=" + curItemId + ", fromItemId=" + fromItemId + ", searchKey=" + searchKey
                + ", ip=" + formIpFromNum(ip) + "]";
    }

    /**
     *    String sql = "select t2.id, t2.word, t2.price, t2.click, t2.competition, t2.pv, t2.strikeFocus, t2.searchFocus,"
                + " t2.lastINWordUpdate, t2.score ,t2.status, t2.scount, t2.cid from ( select id from  "
                + TABLE_NAME
                + " where " + whereSQL + " limit " + po.getPs()
                + " offset "
                + po.getOffset()
                + " ) t1, "
                + TABLE_NAME
                + " t2 where  t1.id = t2.id";
     * @return 
     */
    public static TMResult<List<LinezingRecord>> recentCreatePager(User user, long start, long end, PageOffset po) {
        Long userId = user.getId();
        String sql = selectSQL_T1 + " ( select id from " + TABLE_NAME + genTableHashKey(userId)
                + " where userId = ? and created between ? and ? order by created desc limit ? offset ?) t2 , "
                + TABLE_NAME + genTableHashKey(userId) + " t1 where t1.id = t2.id";

        List<LinezingRecord> list = new ListFetcher(sql, user.getId(), start, end, po.getPs(), po.getPn()).call();
        sql = "select count(id) from " + TABLE_NAME + genTableHashKey(userId)
                + " where userId = ? and created between ? and ? ";

        int count = JDBCBuilder.singleIntQuery(src, sql, userId, start, end);

        TMResult<List<LinezingRecord>> res = new TMResult<List<LinezingRecord>>(list, count, po);
        return res;
    }
}
