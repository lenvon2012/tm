
package jdp;

import static java.lang.String.format;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import job.message.DeleteItemJob;
import models.user.User;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import transaction.JDBCBuilder.JDBCLongSetExecutor;
import bustbapi.TBApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;
import com.ciaosir.client.utils.MapIterator;
import com.ciaosir.client.utils.NumberUtil;
import com.jd.open.api.sdk.internal.util.StringUtil;
import com.taobao.api.ApiException;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.Trade;

public class JdpModel {

    private static final Logger log = LoggerFactory.getLogger(JdpModel.class);

    public static final String TAG = "JdpModel";

    /**
     * | jdp_tb_item | 
    CREATE TABLE `jdp_tb_item` (
    `num_iid` bigint(20) NOT NULL,
    `nick` varchar(32) DEFAULT NULL,
    `approve_status` varchar(32) DEFAULT NULL,
    `has_showcase` varchar(32) DEFAULT NULL,
    `created` datetime DEFAULT NULL,
    `modified` datetime DEFAULT NULL,
    `cid` varchar(256) DEFAULT NULL,
    `has_discount` varchar(32) DEFAULT NULL,
    `jdp_hashcode` varchar(128) DEFAULT NULL,
    `jdp_response` mediumtext,
    `jdp_delete` int(2) DEFAULT NULL,
    `jdp_created` datetime DEFAULT NULL,
    `jdp_modified` datetime DEFAULT NULL,
    PRIMARY KEY (`num_iid`),
    KEY `ind_jdp_tb_item_nick_jdp_modified` (`nick`,`jdp_modified`),
    KEY `ind_jdp_tb_item_jdp_modified` (`jdp_modified`),
    KEY `ind_jdp_tb_item_nick_modified` (`nick`,`modified`),
    KEY `ind_jdp_tb_item_modified` (`modified`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8
       
    ******************** 1. row ***************************
       num_iid: 5192139988
          nick: yad旗舰店
    approve_status: instock
    has_showcase: true
       created: 2010-04-29 13:43:40
      modified: 2014-01-31 13:27:47
           cid: 50000436
    has_discount: false
    jdp_hashcode: 845573988
    jdp_response: {"item_get_response":{"item":{"approve_status":"instock","auction_point":5,"cid":50000436,"created":"2010-04-29 13:43:40","delist_time":"2012-02-23 11:37:11","detail_url":"http:\/\/item.taobao.com\/item.htm?id=5192139988&spm=2014.12440355.0.0","ems_fee":"20.00","express_fee":"6.00","freight_payer":"buyer","has_discount":false,"has_invoice":true,"has_showcase":true,"has_warranty":false,"input_pids":"1632501","input_str":"SZ038A","is_fenxiao":0,"is_timing":false,"is_virtual":false,"item_imgs":{"item_img":[{"id":0,"position":0,"url":"http:\/\/img03.taobaocdn.com\/bao\/uploaded\/i3\/T1d.SrXkhgXXXJq2EV_020448.jpg"},{"id":183967696,"position":1,"url":"http:\/\/img04.taobaocdn.com\/bao\/uploaded\/i4\/T1hkecXgppXXcFCmba_121930.jpg"},{"id":183967817,"position":2,"url":"http:\/\/img04.taobaocdn.com\/bao\/uploaded\/i4\/267761165\/T2yS4oXchMXXXXXXXX_!!267761165.jpg"},{"id":207566656,"position":3,"url":"http:\/\/img04.taobaocdn.com\/bao\/uploaded\/i4\/267761165\/T2rDhJXbhXXXXXXXXX_!!267761165.jpg"},{"id":207566657,"position":4,"url":"http:\/\/img03.taobaocdn.com\/bao\/uploaded\/i3\/267761165\/T2JC4oXcdMXXXXXXXX_!!267761165.jpg"}]},"list_time":"2012-02-18 22:14:47","modified":"2014-01-31 13:27:47","nick":"yad旗舰店","num":236,"num_iid":5192139988,"outer_id":"SZ038A","pic_url":"http:\/\/img03.taobaocdn.com\/bao\/uploaded\/i3\/T1d.SrXkhgXXXJq2EV_020448.jpg","post_fee":"6.00","postage_id":42912807,"price":"96.00","property_alias":"1627207:28320:白色;1627207:28341:黑色;1627207:28326:红色;1627207:28338:蓝色;20503:3267945:XL;20503:3271533:XXL;20503:3271531:L;20503:3271530:M","props_name":"1627743:3267186:袖长:短袖（袖长<35cm）;1632501:51332872:货号:SZ038A;1627746:29447:领型:圆领;1627207:28320:颜色:白色;1627207:28326:颜色:红色;1627207:28338:颜色:蓝色;1627207:28341:颜色:黑色;20503:3271530:尺码:170\/90(M);20503:3271531:尺码:175\/95(L);20503:3267945:尺码:180\/100(XL);20503:3271533:尺码:185\/105(XXL);1627766:248584391:面料分类:其他针织布;1627773:129555:款式细节:印花;20000:6274241:品牌:YAD\/亚道;1627739:3267162:版型:修身型","seller_cids":"","skus":{"sku":[{"created":"2011-02-17 23:59:13","modified":"2013-06-27 07:29:47","outer_id":"SZ205A0212","price":"96.00","properties":"1627207:28320;20503:3267945","properties_name":"1627207:28320:颜色:白色;20503:3267945:尺码:180\/100(XL)","quantity":25,"sku_id":8982730436,"with_hold_quantity":0},{"created":"2011-02-17 23:59:13","modified":"2013-06-27 07:29:44","outer_id":"SZ205A0202","price":"96.00","properties":"1627207:28320;20503:3271531","properties_name":"1627207:28320:颜色:白色;20503:3271531:尺码:175\/95(L)","quantity":12,"sku_id":8982730484,"with_hold_quantity":0},{"created":"2011-02-17 23:59:13","modified":"2013-06-27 07:29:47","outer_id":"SZ205A0222","price":"96.00","properties":"1627207:28320;20503:3271533","properties_name":"1627207:28320:颜色:白色;20503:3271533:尺码:185\/105(XXL)","quantity":11,"sku_id":8982730532,"with_hold_quantity":0},{"created":"2011-02-18 19:37:59","modified":"2013-06-27 07:29:37","outer_id":"SZ205A0201","price":"96.00","properties":"1627207:28320;20503:3271530","properties_name":"1627207:28320:颜色:白色;20503:3271530:尺码:170\/90(M)","quantity":41,"sku_id":8995626500,"with_hold_quantity":0},{"created":"2011-03-09 17:04:53","modified":"2013-06-27 07:29:49","outer_id":"SZ205A0312","price":"96.00","properties":"1627207:28338;20503:3267945","properties_name":"1627207:28338:颜色:蓝色;20503:3267945:尺码:180\/100(XL)","quantity":19,"sku_id":9444102388,"with_hold_quantity":0},{"created":"2011-03-09 17:04:53","modified":"2013-06-27 07:29:51","outer_id":"SZ205A0301","price":"96.00","properties":"1627207:28338;20503:3271530","properties_name":"1627207:28338:颜色:蓝色;20503:3271530:尺码:170\/90(M)","quantity":18,"sku_id":9444102404,"with_hold_quantity":0},{"created":"2011-03-09 17:04:53","modified":"2013-06-27 07:29:47","outer_id":"SZ205A0302","price":"96.00","properties":"1627207:28338;20503:3271531","properties_name":"1627207:28338:颜色:蓝色;20503:3271531:尺码:175\/95(L)","quantity":27,"sku_id":9444102420,"with_hold_quantity":0},{"created":"2011-03-09 17:04:53","modified":"2013-06-27 07:29:51","outer_id":"SZ205A0322","price":"96.00","properties":"1627207:28338;20503:3271533","properties_name":"1627207:28338:颜色:蓝色;20503:3271533:尺码:185\/105(XXL)","quantity":8,"sku_id":9444102436,"with_hold_quantity":0},{"created":"2011-05-05 17:11:55","modified":"2013-06-27 07:30:38","outer_id":"SZ205A0412","price":"96.00","properties":"1627207:28326;20503:3267945","properties_name":"1627207:28326:颜色:红色;20503:3267945:尺码:180\/100(XL)","quantity":23,"sku_id":10999483444,"with_hold_quantity":0},{"created":"2011-05-05 17:11:55","modified":"2013-06-27 07:30:39","outer_id":"SZ205A0112","price":"96.00","properties":"1627207:28341;20503:3267945","properties_name":"1627207:28341:颜色:黑色;20503:3267945:尺码:180\/100(XL)","quantity":0,"sku_id":10999483460,"with_hold_quantity":0},{"created":"2011-05-05 17:11:55","modified":"2013-06-27 07:30:38","outer_id":"SZ205A0401","price":"96.00","properties":"1627207:28326;20503:3271530","properties_name":"1627207:28326:颜色:红色;20503:3271530:尺码:170\/90(M)","quantity":0,"sku_id":10999483476,"with_hold_quantity":0},{"created":"2011-05-05 17:11:55","modified":"2013-06-27 07:30:39","outer_id":"SZ205A0101","price":"96.00","properties":"1627207:28341;20503:3271530","properties_name":"1627207:28341:颜色:黑色;20503:3271530:尺码:170\/90(M)","quantity":22,"sku_id":10999483492,"with_hold_quantity":0},{"created":"2011-05-05 17:11:55","modified":"2013-06-27 07:30:39","outer_id":"SZ205A0402","price":"96.00","properties":"1627207:28326;20503:3271531","properties_name":"1627207:28326:颜色:红色;20503:3271531:尺码:175\/95(L)","quantity":8,"sku_id":10999483508,"with_hold_quantity":0},{"created":"2011-05-05 17:11:55","modified":"2013-06-27 07:30:38","outer_id":"SZ205A0102","price":"96.00","properties":"1627207:28341;20503:3271531","properties_name":"1627207:28341:颜色:黑色;20503:3271531:尺码:175\/95(L)","quantity":0,"sku_id":10999483524,"with_hold_quantity":0},{"created":"2011-05-05 17:11:55","modified":"2013-06-27 07:30:39","outer_id":"SZ205A0422","price":"96.00","properties":"1627207:28326;20503:3271533","properties_name":"1627207:28326:颜色:红色;20503:3271533:尺码:185\/105(XXL)","quantity":22,"sku_id":10999483540,"with_hold_quantity":0},{"created":"2011-05-05 17:11:55","modified":"2013-06-27 07:30:38","outer_id":"SZ205A0122","price":"96.00","properties":"1627207:28341;20503:3271533","properties_name":"1627207:28341:颜色:黑色;20503:3271533:尺码:185\/105(XXL)","quantity":0,"sku_id":10999483556,"with_hold_quantity":0}]},"sub_stock":1,"template_id":"null","title":"YAD圆领男士t恤男装纯棉短袖印花修身型黑色白色骷髅头红色蓝色","type":"fixed","violation":false}}}
    jdp_delete: 0
    jdp_created: 2014-01-30 19:15:53
    jdp_modified: 2014-01-31 13:27:49
           num_iid: 15972177443
          nick: 王朝阳549386
    approve_status: instock
    has_showcase: false
       created: 2012-09-20 17:45:34
      modified: 2014-01-22 03:59:27
           cid: 162103
    has_discount: false
    jdp_hashcode: -1233194081    
           num_iid: 5192139988
          nick: yad旗舰店
    approve_status: instock
    has_showcase: true
       created: 2010-04-29 13:43:40
      modified: 2014-01-31 13:27:47
           cid: 50000436
    has_discount: false
    jdp_hashcode: 845573988
    jdp_response: {"item_get_response":{"item":{"approve_status":"instock","auction_point":5,"cid":50000436,"created":"2010-04-29 13:43:40","delist_time":"2012-02-23 11:37:11","detail_url":"http:\/\/item.taobao.com\/item.htm?id=5192139988&spm=2014.12440355.0.0","ems_fee":"20.00","express_fee":"6.00","freight_payer":"buyer","has_discount":false,"has_invoice":true,"has_showcase":true,"has_warranty":false,"input_pids":"1632501","input_str":"SZ038A","is_fenxiao":0,"is_timing":false,"is_virtual":false,"item_imgs":{"item_img":[{"id":0,"position":0,"url":"http:\/\/img03.taobaocdn.com\/bao\/uploaded\/i3\/T1d.SrXkhgXXXJq2EV_020448.jpg"},{"id":183967696,"position":1,"url":"http:\/\/img04.taobaocdn.com\/bao\/uploaded\/i4\/T1hkecXgppXXcFCmba_121930.jpg"},{"id":183967817,"position":2,"url":"http:\/\/img04.taobaocdn.com\/bao\/uploaded\/i4\/267761165\/T2yS4oXchMXXXXXXXX_!!267761165.jpg"},{"id":207566656,"position":3,"url":"http:\/\/img04.taobaocdn.com\/bao\/uploaded\/i4\/267761165\/T2rDhJXbhXXXXXXXXX_!!267761165.jpg"},{"id":207566657,"position":4,"url":"http:\/\/img03.taobaocdn.com\/bao\/uploaded\/i3\/267761165\/T2JC4oXcdMXXXXXXXX_!!267761165.jpg"}]},"list_time":"2012-02-18 22:14:47","modified":"2014-01-31 13:27:47","nick":"yad旗舰店","num":236,"num_iid":5192139988,"outer_id":"SZ038A","pic_url":"http:\/\/img03.taobaocdn.com\/bao\/uploaded\/i3\/T1d.SrXkhgXXXJq2EV_020448.jpg","post_fee":"6.00","postage_id":42912807,"price":"96.00","property_alias":"1627207:28320:白色;1627207:28341:黑色;1627207:28326:红色;1627207:28338:蓝色;20503:3267945:XL;20503:3271533:XXL;20503:3271531:L;20503:3271530:M","props_name":"1627743:3267186:袖长:短袖（袖长<35cm）;1632501:51332872:货号:SZ038A;1627746:29447:领型:圆领;1627207:28320:颜色:白色;1627207:28326:颜色:红色;1627207:28338:颜色:蓝色;1627207:28341:颜色:黑色;20503:3271530:尺码:170\/90(M);20503:3271531:尺码:175\/95(L);20503:3267945:尺码:180\/100(XL);20503:3271533:尺码:185\/105(XXL);1627766:248584391:面料分类:其他针织布;1627773:129555:款式细节:印花;20000:6274241:品牌:YAD\/亚道;1627739:3267162:版型:修身型","seller_cids":"","skus":{"sku":[{"created":"2011-02-17 23:59:13","modified":"2013-06-27 07:29:47","outer_id":"SZ205A0212","price":"96.00","properties":"1627207:28320;20503:3267945","properties_name":"1627207:28320:颜色:白色;20503:3267945:尺码:180\/100(XL)","quantity":25,"sku_id":8982730436,"with_hold_quantity":0},{"created":"2011-02-17 23:59:13","modified":"2013-06-27 07:29:44","outer_id":"SZ205A0202","price":"96.00","properties":"1627207:28320;20503:3271531","properties_name":"1627207:28320:颜色:白色;20503:3271531:尺码:175\/95(L)","quantity":12,"sku_id":8982730484,"with_hold_quantity":0},{"created":"2011-02-17 23:59:13","modified":"2013-06-27 07:29:47","outer_id":"SZ205A0222","price":"96.00","properties":"1627207:28320;20503:3271533","properties_name":"1627207:28320:颜色:白色;20503:3271533:尺码:185\/105(XXL)","quantity":11,"sku_id":8982730532,"with_hold_quantity":0},{"created":"2011-02-18 19:37:59","modified":"2013-06-27 07:29:37","outer_id":"SZ205A0201","price":"96.00","properties":"1627207:28320;20503:3271530","properties_name":"1627207:28320:颜色:白色;20503:3271530:尺码:170\/90(M)","quantity":41,"sku_id":8995626500,"with_hold_quantity":0},{"created":"2011-03-09 17:04:53","modified":"2013-06-27 07:29:49","outer_id":"SZ205A0312","price":"96.00","properties":"1627207:28338;20503:3267945","properties_name":"1627207:28338:颜色:蓝色;20503:3267945:尺码:180\/100(XL)","quantity":19,"sku_id":9444102388,"with_hold_quantity":0},{"created":"2011-03-09 17:04:53","modified":"2013-06-27 07:29:51","outer_id":"SZ205A0301","price":"96.00","properties":"1627207:28338;20503:3271530","properties_name":"1627207:28338:颜色:蓝色;20503:3271530:尺码:170\/90(M)","quantity":18,"sku_id":9444102404,"with_hold_quantity":0},{"created":"2011-03-09 17:04:53","modified":"2013-06-27 07:29:47","outer_id":"SZ205A0302","price":"96.00","properties":"1627207:28338;20503:3271531","properties_name":"1627207:28338:颜色:蓝色;20503:3271531:尺码:175\/95(L)","quantity":27,"sku_id":9444102420,"with_hold_quantity":0},{"created":"2011-03-09 17:04:53","modified":"2013-06-27 07:29:51","outer_id":"SZ205A0322","price":"96.00","properties":"1627207:28338;20503:3271533","properties_name":"1627207:28338:颜色:蓝色;20503:3271533:尺码:185\/105(XXL)","quantity":8,"sku_id":9444102436,"with_hold_quantity":0},{"created":"2011-05-05 17:11:55","modified":"2013-06-27 07:30:38","outer_id":"SZ205A0412","price":"96.00","properties":"1627207:28326;20503:3267945","properties_name":"1627207:28326:颜色:红色;20503:3267945:尺码:180\/100(XL)","quantity":23,"sku_id":10999483444,"with_hold_quantity":0},{"created":"2011-05-05 17:11:55","modified":"2013-06-27 07:30:39","outer_id":"SZ205A0112","price":"96.00","properties":"1627207:28341;20503:3267945","properties_name":"1627207:28341:颜色:黑色;20503:3267945:尺码:180\/100(XL)","quantity":0,"sku_id":10999483460,"with_hold_quantity":0},{"created":"2011-05-05 17:11:55","modified":"2013-06-27 07:30:38","outer_id":"SZ205A0401","price":"96.00","properties":"1627207:28326;20503:3271530","properties_name":"1627207:28326:颜色:红色;20503:3271530:尺码:170\/90(M)","quantity":0,"sku_id":10999483476,"with_hold_quantity":0},{"created":"2011-05-05 17:11:55","modified":"2013-06-27 07:30:39","outer_id":"SZ205A0101","price":"96.00","properties":"1627207:28341;20503:3271530","properties_name":"1627207:28341:颜色:黑色;20503:3271530:尺码:170\/90(M)","quantity":22,"sku_id":10999483492,"with_hold_quantity":0},{"created":"2011-05-05 17:11:55","modified":"2013-06-27 07:30:39","outer_id":"SZ205A0402","price":"96.00","properties":"1627207:28326;20503:3271531","properties_name":"1627207:28326:颜色:红色;20503:3271531:尺码:175\/95(L)","quantity":8,"sku_id":10999483508,"with_hold_quantity":0},{"created":"2011-05-05 17:11:55","modified":"2013-06-27 07:30:38","outer_id":"SZ205A0102","price":"96.00","properties":"1627207:28341;20503:3271531","properties_name":"1627207:28341:颜色:黑色;20503:3271531:尺码:175\/95(L)","quantity":0,"sku_id":10999483524,"with_hold_quantity":0},{"created":"2011-05-05 17:11:55","modified":"2013-06-27 07:30:39","outer_id":"SZ205A0422","price":"96.00","properties":"1627207:28326;20503:3271533","properties_name":"1627207:28326:颜色:红色;20503:3271533:尺码:185\/105(XXL)","quantity":22,"sku_id":10999483540,"with_hold_quantity":0},{"created":"2011-05-05 17:11:55","modified":"2013-06-27 07:30:38","outer_id":"SZ205A0122","price":"96.00","properties":"1627207:28341;20503:3271533","properties_name":"1627207:28341:颜色:黑色;20503:3271533:尺码:185\/105(XXL)","quantity":0,"sku_id":10999483556,"with_hold_quantity":0}]},"sub_stock":1,"template_id":"null","title":"YAD圆领男士t恤男装纯棉短袖印花修身型黑色白色骷髅头红色蓝色","type":"fixed","violation":false}}}
    jdp_delete: 0
    jdp_created: 2014-01-30 19:15:53
    jdp_modified: 2014-01-31 13:27:49
    */

    static DataSrc src = DataSrc.JDP;

    public static class JdpItemModel {

        @Override
        public String toString() {
            return "JdpItemModel [modified=" + modified + ", isDeleted=" + isDeleted + ", hasShowCase=" + hasShowCase
                    + ", numIid=" + numIid + ", resp=" + resp + ", item=" + item + ", approveStatus=" + approveStatus
                    + "]";
        }

        long modified = 0L;

        boolean isDeleted = false;

        boolean hasShowCase = false;

        long numIid;

        String resp;

        String nick;

        Item item;

        String jdpModified;

        public static String getSelectFields() {
            return " num_iid,approve_status,has_showcase,jdp_delete,jdp_response,nick,jdp_modified";
        }

        public Item toTBItem() throws ApiException {
            if (resp == null) {
                return null;
            }
            if (this.item != null) {
                return this.item;
            }

            try {
                JSONObject first = new JSONObject(resp);
                if (!first.has("item_get_response")) {
                    return null;
                }
                first = first.getJSONObject("item_get_response");
                if (!first.has("item")) {
                    return null;
                }
                first = first.getJSONObject("item");
                this.item = TBApi.parseItemRespJson(first);
                return this.item;
            } catch (JSONException e) {
                log.warn("bad json :" + resp);
                throw new ApiException(e.getMessage());
            } catch (ParseException e) {
                log.warn("bad json :" + resp);
                throw new ApiException(e.getMessage());
            }
        }

        public static boolean isItemRecentDeleted(Long numIid) {
            int res = JDBCBuilder.singleIntQuery(src, " select jdp_delete from jdp_tb_item where num_iid = ?", numIid);
            // If jdp_delete == 1, then is deleted...
            return res == 1;
        }

        public static List<JdpItemModel> recentJdpModifiedItems(int offset, int limit, long start, long end) {
            String startStr = DateUtil.formDateForLog(start);
            String endStr = DateUtil.formDateForLog(end);

            log.info(format("recentJdpModifiedItems:offset, limit, startStr, endStr".replaceAll(", ", "=%s, ") + "=%s",
                    offset, limit, startStr, endStr));
            Map<Long, JdpItemModel> res = fetchJdpItemModel("select " + getSelectFields()
                    + " from jdp_tb_item where jdp_modified >= ? and jdp_modified < ? limit ? offset ? ", startStr,
                    endStr,
                    limit, offset);

            return new ArrayList<JdpItemModel>(res.values());
        }

        public static Map<Long, JdpItemModel> queryAllJdpItems(String nick, long start, long end) {
            String sql = "select " + getSelectFields()
                    + "from jdp_tb_item where nick = ? and jdp_modified >? and jdp_modified <? ";

            Map<Long, JdpItemModel> res = fetchJdpItemModel(sql, nick, DateUtil.formDateForLog(start),
                    DateUtil.formDateForLog(end));
            return res;
        }

        public static Map<Long, JdpItemModel> fetchJdpItemModel(String sql, Object... objects) {
            long start = System.currentTimeMillis();
            Map<Long, JdpItemModel> res = new JDBCExecutor<Map<Long, JdpItemModel>>(false, src, sql, objects) {
                @Override
                public Map<Long, JdpItemModel> doWithResultSet(ResultSet rs) throws SQLException {
                    Map<Long, JdpItemModel> map = new HashMap<Long, JdpModel.JdpItemModel>();
                    try {
                        while (rs.next()) {
                            JdpItemModel model = new JdpItemModel(rs);
                            map.put(model.getNumIid(), model);
                        }
                    } catch (ApiException e) {
                        log.warn(e.getMessage(), e);
                    }
                    return map;
                }
            }.call();
            long end = System.currentTimeMillis();
//            log.warn(" sql :" + sql + " took " + (end - start) + "ms");

            start = end;
            for (JdpItemModel model : res.values()) {
                try {
                    model.toTBItem();
                } catch (ApiException e) {
                    log.warn(e.getMessage(), e);
                }
            }
            end = System.currentTimeMillis();
//            log.warn(" transrate item model :" + res.values().size() + " took " + (end - start) + "ms");
            return res;
        }

        public static Item findByNumIid(Long userId, Long numIid) {
            JdpItemModel model = new JDBCExecutor<JdpItemModel>(src, "select " + getSelectFields()
                    + " from  jdp_tb_item where num_iid = ?",
                    numIid) {
                @Override
                public JdpItemModel doWithResultSet(ResultSet rs) throws SQLException {
                    try {
                        while (rs.next()) {
                            JdpItemModel model = new JdpItemModel(rs);
                            return model;
                        }
                    } catch (ApiException e) {
                        log.warn(e.getMessage(), e);
                    }
                    return null;
                }
            }.call();
            if (model == null) {
                return null;
            }
            if (model.isDeleted()) {
                if (userId != null) {
                    DeleteItemJob.tryDeleteItem(userId, numIid);
                }
                return null;
            }
            try {
                return model.toTBItem();
            } catch (ApiException e) {
                log.warn(e.getMessage(), e);
            }

            return null;
        }

        public static Set<Long> recentDeleted(long start, long end) {
            String endStr = DateUtil.formDateForLog(end);
            String startStr = DateUtil.formDateForLog(start);
            return new JDBCLongSetExecutor(src, " jdp_modified between  ? and ?  jdp_delete = 1", startStr, endStr)
                    .call();
        }

        public static List<Item> recentModified(long start, long end) {
            String endStr = DateUtil.formDateForLog(end);
            String startStr = DateUtil.formDateForLog(start);
            List<Item> items = jdpItemFetcher(" jdp_modified between  ? and ? ", startStr, endStr, startStr, endStr);
            return items;
        }

        public static List<Item> recentAdded(long interval) {
            long curr = System.currentTimeMillis();
            String endStr = DateUtil.formDateForLog(curr);
//            String startStr = DateUtil.formDateForLog(curr - DateUtil.ONE_MINUTE_MILLIS);
            String startStr = DateUtil.formDateForLog(curr - interval);

            List<Item> items = jdpItemFetcher(
                    " jdp_modified  between  ? and ? and created between ? and ? and jdp_delete = 0", startStr, endStr,
                    startStr, endStr);

            return items;
        }

        public static List<Item> jdpItemFetcher(String where, Object... objs) {
            String sql = "select " + getSelectFields() + " from jdp_tb_item where 1 = 1 ";

            if (StringUtil.isEmpty(where)) {
                //
            } else {
                sql += " and " + where;
            }

            List<JdpItemModel> models = new JDBCExecutor<List<JdpItemModel>>(src, sql, objs) {
                @Override
                public List<JdpItemModel> doWithResultSet(ResultSet rs) throws SQLException {
//                    List<Item> items = new ArrayList<Item>();
                    List<JdpItemModel> models = new ArrayList<JdpItemModel>();

                    try {
                        while (rs.next()) {
                            JdpItemModel model = new JdpItemModel(rs);
//                            items.add(model.getItem());
                            models.add(model);
                        }
                    } catch (ApiException e) {
                        log.warn(e.getMessage(), e);
                    }

                    return models;
                }
            }.call();
            try {
                for (JdpItemModel model : models) {
                    model.toTBItem();
                }
            } catch (ApiException e) {
                log.warn(e.getMessage(), e);

            }

            List<Item> items = new ArrayList<Item>(models.size());
            try {
                for (JdpItemModel jdpItemModel : models) {
                    items.add(jdpItemModel.toTBItem());
                }
            } catch (ApiException e) {
                log.warn(e.getMessage(), e);
            }
            return items;
        }

        public JdpItemModel(ResultSet rs) throws SQLException, ApiException {
            this.numIid = rs.getLong(1);
            this.approveStatus = rs.getString(2);
            this.hasShowCase = "true".equals(rs.getString(3));
            this.isDeleted = (1 == rs.getInt(4));
            this.resp = rs.getString(5);
            this.nick = rs.getString(6);
            this.jdpModified = rs.getString(7);

//            this.item = toTBItem();
        }

        private String approveStatus;

        public String getApproveStatus() {
            return approveStatus;
        }

        public void setApproveStatus(String approveStatus) {
            this.approveStatus = approveStatus;
        }

        public long getModified() {
            return modified;
        }

        public void setModified(long modified) {
            this.modified = modified;
        }

        public boolean isDeleted() {
            return isDeleted;
        }

        public void setDeleted(boolean isDeleted) {
            this.isDeleted = isDeleted;
        }

        public boolean isHasShowCase() {
            return hasShowCase;
        }

        public void setHasShowCase(boolean hasShowCase) {
            this.hasShowCase = hasShowCase;
        }

        public long getNumIid() {
            return numIid;
        }

        public void setNumIid(long numIid) {
            this.numIid = numIid;
        }

        public String getResp() {
            return resp;
        }

        public void setResp(String resp) {
            this.resp = resp;
        }

        public Item getItem() {
            return item;
        }

        public void setItem(Item item) {
            this.item = item;
        }

        public String getNick() {
            return nick;
        }

        public void setNick(String nick) {
            this.nick = nick;
        }

        public String getJdpModified() {
            return jdpModified;
        }

        public void setJdpModified(String jdpModified) {
            this.jdpModified = jdpModified;
        }

        public static int countOnSaleItem(User user) {
            return JDBCBuilder
                    .singleIntQuery(
                            JdpModel.src,
                            " select count(*) from jdp_tb_item where nick = ? and jdp_delete = 0 and approve_status = 'onsale'",
                            user.getUserNick());
        }

        public static long minModified(User user) {
            return parseDateStr(JDBCBuilder
                    .singleStringQuery(JdpModel.src,
                            " select min(jdp_modified) from jdp_tb_item where nick = ? and jdp_delete = 0 ",
                            user.getUserNick()));
        }

        public static long maxModified(User user) {
            return parseDateStr(JDBCBuilder
                    .singleStringQuery(JdpModel.src,
                            " select max(jdp_modified) from jdp_tb_item where nick = ? and jdp_delete = 0 ",
                            user.getUserNick()));
        }

        static long parseDateStr(String dateStr) {
            if (StringUtil.isEmpty(dateStr)) {
                return 0L;
            }
            try {
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateStr).getTime();
            } catch (ParseException e) {
                log.warn(e.getMessage(), e);
            }
            return 0L;
        }

        /**
        CREATE TABLE `jdp_tb_trade` (
        `tid` bigint(20) NOT NULL,
        `status` varchar(64) DEFAULT NULL,
        `type` varchar(64) DEFAULT NULL,
        `seller_nick` varchar(32) DEFAULT NULL,
        `buyer_nick` varchar(32) DEFAULT NULL,
        `created` datetime DEFAULT NULL,
        `modified` datetime DEFAULT NULL,
        `jdp_hashcode` varchar(128) DEFAULT NULL,
        `jdp_response` mediumtext,
        `jdp_created` datetime DEFAULT NULL,
        `jdp_modified` datetime DEFAULT NULL,
        PRIMARY KEY (`tid`),
        KEY `ind_jdp_tb_trade_seller_nick_jdp_modified` (`seller_nick`,`jdp_modified`),
        KEY `ind_jdp_tb_trade_jdp_modified` (`jdp_modified`),
        KEY `ind_jdp_tb_trade_seller_nick_modified` (`seller_nick`,`modified`),
        KEY `ind_jdp_tb_trade_modified` (`modified`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8 
        
        
         * 订单数据
        
        *************************** 1. row ***************************
             tid: 452790065331038
          status: TRADE_CLOSED_BY_TAOBAO
            type: fixed
        seller_nick: yad旗舰店
        buyer_nick: ququ617
         created: 2013-11-10 14:22:38
        modified: 2014-01-31 00:18:01
        jdp_hashcode: -1309859145
        jdp_response: {"trade_fullinfo_get_response":{"trade":{"adjust_fee":"0.00","alipay_id":2088602232459025,"alipay_no":"2013111011001001020066467481","available_confirm_fee":"13.80","buyer_alipay_no":"6240*****@qq.com","buyer_area":"陕西西安电信","buyer_cod_fee":"0.00","buyer_email":"angel_l*******@yahoo.com.cn","buyer_nick":"ququ617","buyer_obtain_point_fee":0,"buyer_rate":false,"cod_fee":"0.00","cod_status":"NEW_CREATED","commission_fee":"0.00","created":"2013-11-10 14:22:38","discount_fee":"0.00","end_time":"2013-11-13 14:24:01","has_post_fee":true,"is_3D":false,"is_brand_sale":false,"is_force_wlb":false,"is_lgtype":false,"is_part_consign":false,"is_wt":false,"modified":"2014-01-31 00:18:01","num":1,"num_iid":35223917483,"orders":{"order":[{"adjust_fee":"0.00","buyer_rate":false,"cid":50000436,"discount_fee":"74.20","end_time":"2013-11-13 14:24:01","is_daixiao":false,"is_oversold":false,"num":1,"num_iid":35223917483,"oid":452790065331038,"order_from":"TAOBAO","outer_sku_id":"TX324A0212","part_mjz_discount":"0.00","payment":"13.80","pic_path":"http:\/\/img04.taobaocdn.com\/bao\/uploaded\/i4\/267761165\/T2oKlBXxBXXXXXXXXX_!!267761165.jpg","price":"88.00","refund_status":"NO_REFUND","seller_rate":false,"seller_type":"B","sku_id":"33395009470","sku_properties_name":"颜色:白色;尺码:180\/100(XL)","snapshot_url":"e:452790065331038_1","status":"TRADE_CLOSED_BY_TAOBAO","title":"2013春秋装 男士 长袖T恤男新款 圆领长纯色 简单打底衫 休闲体恤","total_fee":"13.80"}]},"payment":"13.80","pic_path":"http:\/\/img04.taobaocdn.com\/bao\/uploaded\/i4\/267761165\/T2oKlBXxBXXXXXXXXX_!!267761165.jpg","point_fee":0,"post_fee":"0.00","price":"88.00","promotion_details":{"promotion_detail":[{"discount_fee":"74.20","id":452790065331038,"promotion_desc":"亏本清仓:省74.20元","promotion_id":"PKSNmultidz1350894312-111562788_1124882997","promotion_name":"亏本清仓"},{"discount_fee":"0.00","id":452790065331038,"promotion_desc":"满就送:省0.00元","promotion_id":"mjs-267761165_106348459","promotion_name":"满就送"}]},"real_point_fee":0,"received_payment":"0.00","receiver_address":"丈**路吉的堡绿地**园","receiver_city":"西安市","receiver_district":"雁塔区","receiver_mobile":"1389289****","receiver_name":"屈**","receiver_phone":"029-8597****","receiver_state":"陕西省","receiver_zip":"710065","seller_alipay_no":"bjm_com@163.com","seller_can_rate":false,"seller_cod_fee":"0.00","seller_email":"luxsea@163.com","seller_flag":1,"seller_memo":"已催 小强 11.11","seller_mobile":"18600500020","seller_name":"北京百珈玛制衣有限公司","seller_nick":"yad旗舰店","seller_phone":"010-81888988","seller_rate":false,"shipping_type":"express","snapshot_url":"e:452790065331038_1","status":"TRADE_CLOSED_BY_TAOBAO","tid":452790065331038,"title":"yad旗舰店","total_fee":"88.00","trade_from":"TAOBAO","type":"fixed"}}}
        jdp_created: 2014-01-31 00:18:03
        jdp_modified: 2014-01-31 00:18:03
        
         */

        public static int countOnWindow(User user) {
            int onWindow = JDBCBuilder.singleIntQuery(JdpModel.src,
                    " select count(num_iid) from jdp_tb_item where nick = ? and jdp_delete = 0 "
                            + "and approve_status = 'onsale' and has_showcase = 'true'",
                    user.getUserNick());
            return onWindow;
        }

        public static int countInventoryItems(User user) {
            String sql = "select count(num_iid) from jdp_tb_item where nick = ? and approve_status = 'instock' and jdp_delete = 0";
            return JDBCBuilder.singleIntQuery(src, sql, user.getUserNick());
        }

        public static Set<Long> allNumIids(User user) {
            String sql = " select num_iid from jdp_tb_item where nick = ? and jdp_delete = 0";
            return new JDBCBuilder.JDBCLongSetExecutor(src, sql, user.getUserNick()).call();
        }

        public static Set<Long> onWindowNumIids(User user) {
            String sql = " select num_iid from jdp_tb_item where nick = ? and jdp_delete = 0" +
                    " and approve_status = 'onsale' and has_showcase = 'true'";
            return new JDBCBuilder.JDBCLongSetExecutor(src, sql, user.getUserNick()).call();
        }

        public static Set<Long> onSaleItemIds(User user) {
            String sql = " select num_iid from jdp_tb_item where nick = ? and approve_status = 'onsale' and jdp_delete = 0";
            return new JDBCBuilder.JDBCLongSetExecutor(src, sql, user.getUserNick()).call();
        }

        public static List<Item> findByNumIids(Collection<Long> notDownId) {
            if (CommonUtils.isEmpty(notDownId)) {
                return ListUtils.EMPTY_LIST;
            }

            List<Item> items = jdpItemFetcher(" num_iid in ( " + StringUtils.join(notDownId, ',') + " ) ");
            return items;
        }

    }

    public static class JDBCJdpTradeExec extends JDBCExecutor<Map<Long, String>> {
        public JDBCJdpTradeExec(DataSrc src, String query, Object... params) {
            super(src, query, params);
//            this.debug = true;
        }

        @Override
        public Map<Long, String> doWithResultSet(ResultSet rs) throws SQLException {
            Map<Long, String> res = new HashMap<Long, String>();
            while (rs.next()) {
                Long tid = rs.getLong(1);
                String resp = rs.getString(2);
                res.put(tid, resp);
            }
            return res;
        }
    }

    public static Long tmpTid = 518073718172548L;

    public static String tmpRsp = "{\"trade_fullinfo_get_response\":{\"trade\":{\"adjust_fee\":\"0.00\",\"alipay_id\":2088012761187096,\"alipay_no\":\"2014011511001001090016485237\",\"available_confirm_fee\":\"0.00\",\"buyer_alipay_no\":\"29130*****@qq.com\",\"buyer_area\":\"未知\",\"buyer_cod_fee\":\"0.00\",\"buyer_email\":\"29130*****@qq.com\",\"buyer_nick\":\"老婆最大51521\",\"buyer_obtain_point_fee\":0,\"buyer_rate\":false,\"cod_fee\":\"0.00\",\"cod_status\":\"NEW_CREATED\",\"commission_fee\":\"0.00\",\"consign_time\":\"2014-01-17 15:31:07\",\"created\":\"2014-01-15 21:40:01\",\"discount_fee\":\"0.00\",\"end_time\":\"2014-01-27 15:32:23\",\"has_post_fee\":true,\"is_3D\":false,\"is_brand_sale\":false,\"is_force_wlb\":false,\"is_lgtype\":false,\"is_part_consign\":false,\"is_wt\":false,\"modified\":\"2014-02-06 15:35:43\",\"num\":2,\"num_iid\":37000093369,\"orders\":{\"order\":[{\"adjust_fee\":\"0.00\",\"buyer_rate\":false,\"cid\":50002816,\"consign_time\":\"2014-01-17 15:31:07\",\"discount_fee\":\"0.00\",\"end_time\":\"2014-01-27 15:32:23\",\"invoice_no\":\"550281136794\",\"is_daixiao\":false,\"is_oversold\":false,\"logistics_company\":\"顺丰速运\",\"num\":2,\"num_iid\":37000093369,\"oid\":518073718172548,\"order_from\":\"TAOBAO\",\"payment\":\"76.00\",\"pic_path\":\"http://img03.taobaocdn.com/bao/uploaded/i3/1954831548/T2GCxBXsJbXXXXXXXX_!!1954831548.jpg\",\"price\":\"38.00\",\"refund_status\":\"NO_REFUND\",\"seller_rate\":true,\"seller_type\":\"C\",\"shipping_type\":\"express\",\"sku_id\":\"56388300261\",\"sku_properties_name\":\"颜色分类:绿檀大圆把\",\"snapshot_url\":\"f:518073718172548_1\",\"status\":\"TRADE_FINISHED\",\"store_code\":\"IC\",\"title\":\"梳美人天然绿檀木梳子檀檀香静电防静电正品断发脱发大圆个人洗漱\",\"total_fee\":\"76.00\"}]},\"pay_time\":\"2014-01-15 21:40:24\",\"payment\":\"76.00\",\"pic_path\":\"http://img03.taobaocdn.com/bao/uploaded/i3/1954831548/T2GCxBXsJbXXXXXXXX_!!1954831548.jpg\",\"point_fee\":0,\"post_fee\":\"0.00\",\"price\":\"38.00\",\"real_point_fee\":0,\"received_payment\":\"76.00\",\"receiver_address\":\"南关**号北**单**楼西户\",\"receiver_city\":\"郑州市\",\"receiver_district\":\"管城回族区\",\"receiver_mobile\":\"1573672****\",\"receiver_name\":\"闫**\",\"receiver_state\":\"河南省\",\"receiver_zip\":\"450000\",\"seller_alipay_no\":\"18705580658\",\"seller_can_rate\":false,\"seller_cod_fee\":\"0.00\",\"seller_email\":\"\",\"seller_flag\":0,\"seller_mobile\":\"18705580658\",\"seller_name\":\"郭俊霞\",\"seller_nick\":\"梳美人精品木梳\",\"seller_rate\":true,\"shipping_type\":\"express\",\"snapshot_url\":\"f:518073718172548_1\",\"status\":\"TRADE_FINISHED\",\"tid\":518073718172548,\"title\":\"梳美人高档木梳\",\"total_fee\":\"76.00\",\"trade_from\":\"TAOBAO\",\"type\":\"fixed\"}}}";

    public static Map<Long, Trade> formTidRspToTidTradeMap(
            Map<Long, String> tidRspMap) {
//        tidRspMap.put(tmpTid, tmpRsp);

        if (CommonUtils.isEmpty(tidRspMap)) {
            return MapUtils.EMPTY_MAP;
        }

        final Map<Long, Trade> res = new HashMap<Long, Trade>();

        new MapIterator<Long, String>(tidRspMap) {
            @Override
            public void execute(Entry<Long, String> entry) {

                try {
                    Trade trade = TBApi.parseTradeRespJson(new JSONObject(entry.getValue()));
                    res.put(entry.getKey(), trade);
                } catch (JSONException e) {
                    log.warn(e.getMessage(), e);
                } catch (ParseException e) {
                    log.warn(e.getMessage(), e);
                }
            }
        }.call();

        return res;
    }

    public static class JdpTradeModel {
        public static Map<Long, Trade> queryModifiedTrades(String nick,
                long start, long end) {
            String sql = "select tid,jdp_response from jdp_tb_trade where "
                    + "seller_nick = ? and jdp_modified >? and jdp_modified<? order by jdp_modified";

            return formTidRspToTidTradeMap(new JDBCJdpTradeExec(src, sql, nick,
                    DateUtil.formDateForLog(start),
                    DateUtil.formDateForLog(end)).call());
        }

        public static Map<Long, Trade> recentStatus(String statusSQL, long start, long end) {

//            log.info(format("recentStatus:statusSQL, start, end".replaceAll(", ", "=%s, ") + "=%s", statusSQL,
//                    DateUtil.formDateForLog(start), DateUtil.formDateForLog(end)));

            String sql = "select tid,jdp_response from jdp_tb_trade where " + statusSQL
                    + " and jdp_modified between  '" + DateUtil.formDateForLog(start) + "' and '"
                    + DateUtil.formDateForLog(end) + "' order by jdp_modified ";
            log.info("[sql:]" + sql);

            return formTidRspToTidTradeMap(new JDBCJdpTradeExec(src, sql).call());
        }

        public static Map<Long, Trade> recentCreateOrPayed(long start, long end) {
            String sql = " (status = 'WAIT_BUYER_PAY' or status = 'TRADE_NO_CREATE_PAY' or status = 'WAIT_SELLER_SEND_GOODS' ) ";
            return recentStatus(sql, start, end);
        }

        public static Map<Long, Trade> recentFinished(long start, long end) {
            return recentStatus(" status = 'TRADE_FINISHED' ", start, end);
        }

        public static Map<Long, Trade> recentCreated(long start, long end) {
            return recentStatus(" (status = 'WAIT_BUYER_PAY' or status = 'TRADE_NO_CREATE_PAY' ) ", start, end);
        }

        public static Trade fetchTrade(long tid) {
            String sql = "select tid,jdp_response from jdp_tb_trade where tid = ? ";
            Map<Long, Trade> tradeMap = formTidRspToTidTradeMap(new JDBCJdpTradeExec(src, sql, tid).call());
            return NumberUtil.first(new ArrayList<Trade>(tradeMap.values()));
        }

        public static Trade fetchUserTrade(String nick, long tid) {
            String sql = "select tid,jdp_response from jdp_tb_trade where seller_nick = ? and tid = ? order by jdp_modified desc";

            Map<Long, Trade> tradeMap = formTidRspToTidTradeMap(new JDBCJdpTradeExec(src, sql, nick, tid).call());
            Collection<Trade> trades = tradeMap.values();
            if (CommonUtils.isEmpty(trades)) {
                return null;
            }
            return trades.iterator().next();
        }

        public static List<Trade> fetchTrades(Collection<Long> ids) {
            if (CommonUtils.isEmpty(ids)) {
                return ListUtils.EMPTY_LIST;
            }
            return fetchTrades(" tid in (" + StringUtils.join(ids, ',') + ")");
        }

        public static List<Trade> fetchTrades(String whereSQL, Object... objects) {
            String sql = "select tid,jdp_response from jdp_tb_trade where 1 = 1 ";
            if (StringUtil.isEmpty(whereSQL)) {
//                sql = sql;
            } else {
                sql = sql + " and " + whereSQL;
            }

            return new ArrayList<Trade>(formTidRspToTidTradeMap(new JDBCJdpTradeExec(src, sql, objects).call())
                    .values());
        }
        
        public static Map<Long, Trade> queryRecentModifiedTrades(String nick) {
        	long limitTime = System.currentTimeMillis() - DateUtil.DAY_MILLIS;
        	
            String sql = "select tid,jdp_response from jdp_tb_trade where "
                    + "seller_nick = ? and jdp_modified > ? order by jdp_modified limit 100";

            return formTidRspToTidTradeMap(new JDBCJdpTradeExec(src, sql, nick, limitTime).call());
        }
    }

}
