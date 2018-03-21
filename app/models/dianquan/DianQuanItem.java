package models.dianquan;

import codegen.CodeGenerator;
import com.google.gson.JsonObject;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.db.jpa.GenericModel;
import transaction.DBBuilder;
import transaction.JDBCBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The type Dian quan item.
 *
 * @author lyl
 * @date 2017 /11/01
 */
@Entity(name = DianQuanItem.TABLE_NAME)
public class DianQuanItem extends GenericModel{
    private static final Logger LOGGER = LoggerFactory.getLogger(DianQuanItem.class);
    public static final String TAG = "DianQuanItem";
    public static final String TABLE_NAME = "dian_quan_item";
    private static final CodeGenerator.DBDispatcher DP = new CodeGenerator.DBDispatcher(DBBuilder.DataSrc.BASIC, null);
    private static final String ALL_PROPERTY = "id,gid,categoryId,categoryName,site,title,subTitle,isBrand,brandName,discountPrice,normalPrice,ratio,hasFreight,ratioType,thumb,url,longPic,videoUrl,recommendedReason,activity,finalSales,hasCoupon,couponMoney,couponUrl,couponTotal,couponLatest,timeline,stoptime,newUrl,createTs,updateTs";
    private static final String PARAM_HOLDERS = ALL_PROPERTY.replaceAll("\\w+", "?");
    private static final String UPDATE_PARAM_HOLDERS = ALL_PROPERTY.replaceFirst("id\\s*,", "").replaceAll(",", " = ?,") + "= ? ";
    private static final String QUERY_PREFIX = "select " + ALL_PROPERTY + " from " + TABLE_NAME + " where ";
    private static final String COUNT_PREFIX = "select count(id) from " + TABLE_NAME + " where ";
    /**
     * The Id.
     */
    @Id
    public String id;
    /**
     * 商品淘宝id
     */
    @Index(name = "gid")
    public String gid;
    /**
     * 分类id
     */
    public int categoryId;
    /**
     * 分类名称
     */
    public String categoryName;

    /**
     * 所属站点
     */
    public String site;
    /**
     * 标题
     */
    public String title;
    /**
     * 商品简称
     */
    public String subTitle;
    /**
     * 品牌状态
     * 0-未申请品牌，1-待审，2-已审）
     */
    public boolean isBrand;
    /**
     * 品牌名称
     */
    public String brandName;

    /**
     * 券后价
     */
    @Column(precision = 10, scale = 2)
    public BigDecimal discountPrice;
    /**
     * 正常售价
     */
    @Column(precision = 10, scale = 2)
    public BigDecimal normalPrice;
    /**
     * 佣金比例
     */
    @Column(precision = 10, scale = 2)
    public BigDecimal ratio;
    /**
     * 运费险
     * 0-否 1-是
     */
    public boolean hasFreight;
    /**
     * 通用, 定向, 鹊桥
     */
    public String ratioType;

    /**
     * 商品主图
     */
    public String thumb;
    /**
     * 购买链接
     */
    public String url;
    /**
     * 商品长图
     */
    public String longPic;
    /**
     * 视频地址
     */
    public String videoUrl;

    /**
     * 推荐理由
     */
    public String recommendedReason;
    /**
     * 活动
     */
    public String activity;

    /**
     * 最终销量
     */
    public int finalSales;
    /**
     * 是否有优惠券
     * 0-无 1-有）
     */
    public boolean hasCoupon;
    /**
     * 优惠券金额
     */
    @Column(precision = 10, scale = 2)
    public BigDecimal couponMoney;
    /**
     * 优惠券链接
     */
    public String couponUrl;
    /**
     * 优惠券总数
     */
    public int couponTotal;
    /**
     * 当前已领优惠券数
     */
    public int couponLatest;
    /**
     * 更新时间
     */
    public long timeline;
    /**
     * 结束时间
     */
    public long stoptime;
    /**
     * 二合一链接
     */
    public String newUrl;

    /**
     * 数据创建时间
     */
    public long createTs;
    /**
     * 数据更新时间
     */
    public long updateTs;

    public DianQuanItem() {

    }

    private void createNew() {
        this.id = UUID.randomUUID().toString();
        this.createTs = System.currentTimeMillis();
        this.updateTs = this.createTs;
    }

    public DianQuanItem(String gid, int categoryId, String categoryName, String site, String title, String subTitle, boolean isBrand, String brandName, BigDecimal discountPrice, BigDecimal normalPrice, BigDecimal ratio, boolean hasFreight, String ratioType, String thumb, String url, String longPic, String videoUrl, String recommendedReason, String activity, int finalSales, boolean hasCoupon, BigDecimal couponMoney, String couponUrl, int couponTotal, int couponLatest, long timeline, long stoptime, String newUrl) {
        createNew();
        this.gid = gid;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.site = site;
        this.title = title;
        this.subTitle = subTitle;
        this.isBrand = isBrand;
        this.brandName = brandName;
        this.discountPrice = discountPrice;
        this.normalPrice = normalPrice;
        this.ratio = ratio;
        this.hasFreight = hasFreight;
        this.ratioType = ratioType;
        this.thumb = thumb;
        this.url = url;
        this.longPic = longPic;
        this.videoUrl = videoUrl;
        this.recommendedReason = recommendedReason;
        this.activity = activity;
        this.finalSales = finalSales;
        this.hasCoupon = hasCoupon;
        this.couponMoney = couponMoney;
        this.couponUrl = couponUrl;
        this.couponTotal = couponTotal;
        this.couponLatest = couponLatest;
        this.timeline = timeline;
        this.stoptime = stoptime;
        this.newUrl = newUrl;
    }

    /**
     * 从JsonObject初始化DianQuanItem
     *
     * @param item the item
     */
    public DianQuanItem(JsonObject item) {
        createNew();
        this.gid = item.get("gid").getAsString();
        this.categoryId = item.get("id").getAsInt();
        this.categoryName = item.get("cate").getAsString();
        this.site = item.get("site").getAsString();
        this.title = item.get("title").getAsString();
        this.subTitle = item.get("sub_title").getAsString();
        this.isBrand = item.get("is_brand").getAsBoolean();
        this.brandName = item.get("brand_name").getAsString();
        this.discountPrice = item.get("price").getAsBigDecimal();
        this.normalPrice = item.get("prime").getAsBigDecimal();
        this.ratio = item.get("ratio").getAsBigDecimal();
        this.hasFreight = item.get("freight").getAsBoolean();
        this.ratioType = item.get("ratio_type").getAsString();
        this.thumb = item.get("thumb").getAsString();
        this.url = item.get("url").getAsString();
        this.longPic = item.has("long_pic")?item.get("long_pic").getAsString():"";
        this.videoUrl = item.has("video")?item.get("video").getAsString():"";
        this.recommendedReason = item.get("intro_foot").getAsString();
        this.activity = item.get("activity").getAsString();
        this.finalSales = item.get("final_sales").getAsInt();
        this.hasCoupon = item.get("coupon").getAsBoolean();
        this.couponMoney = item.get("coupon_money").getAsBigDecimal();
        this.couponUrl = item.get("coupon_url").getAsString();
        this.couponTotal = item.get("coupon_total").getAsInt();
        this.couponLatest = item.get("coupon_latest").getAsInt();
        this.timeline = item.get("timeline").getAsLong();
        this.stoptime = item.get("stoptime").getAsLong();
        this.newUrl = item.get("new_url").getAsString();
    }

    /**
     * 插入
     *
     * @return 是否成功
     */
    public boolean rawInsert() {
        String sql = "insert into " + TABLE_NAME + "(" + ALL_PROPERTY + ")" +
                " values(" + PARAM_HOLDERS + ")";

        long result = DP.insert(sql, id, gid, categoryId, categoryName, site, title, subTitle, isBrand, brandName, discountPrice, normalPrice, ratio, hasFreight, ratioType, thumb, url, longPic, videoUrl, recommendedReason, activity, finalSales, hasCoupon, couponMoney, couponUrl, couponTotal, couponLatest, timeline, stoptime, newUrl, createTs, updateTs);
        if (result > 0L) {
            return true;
        } else {
            LOGGER.error("create failed");
            return false;
        }
    }

    /**
     * 更新
     *
     * @return 是否成功
     */
    public boolean update() {
        String sql = "update " + TABLE_NAME + " set " + UPDATE_PARAM_HOLDERS + " where id=?";
        long result = DP.update(sql,
                gid, categoryId, categoryName, site, title, subTitle, isBrand, brandName, discountPrice, normalPrice, ratio, hasFreight, ratioType, thumb, url, longPic, videoUrl, recommendedReason, activity, finalSales, hasCoupon, couponMoney, couponUrl, couponTotal, couponLatest, timeline, stoptime, newUrl, createTs, updateTs, id);
        return result > 0L;
    }

    /**
     * 通过id寻找
     *
     * @param id the id
     * @return the DianQuanItem
     */
    public static DianQuanItem findDianQuanItemById(final String id) {
        String query = QUERY_PREFIX + "id=?";
        return findOne(query, id);
    }

    /**
     * 通过gid寻找
     *
     * @param gid 淘宝宝贝id
     * @return the DianQuanItem
     */
    public static DianQuanItem findDianQuanItemByGid(final String gid) {
        String query = QUERY_PREFIX + "gid=?";
        return findOne(query, gid);
    }

    /**
     * 通过自定义query查找
     *
     * @param query  the query
     * @param params the params
     * @return the list
     */
    public static List<DianQuanItem> findDianQuanItemList(String query, Object... params) {
        query = QUERY_PREFIX + query;
        return findList(query, params);
    }

    /**
     * 清除过期点券
     *
     * @return the boolean
     */
    public static boolean clearExpiredDianquan() {
        String query = "delete from " + TABLE_NAME + " where stoptime<=? or couponTotal=couponLatest";
        return DP.update(query, System.currentTimeMillis() / 1000) != -1;
    }

    /**
     * 通过自定义query统计个数
     *
     * @param query  the query
     * @param params the params
     * @return 个数
     */
    public static long countDianquan(String query, Object... params) {
        query = COUNT_PREFIX + query;
        return DP.singleLongQuery(query, params);
    }

    private static DianQuanItem findOne(String query, Object... param) {
        return new JDBCBuilder.JDBCExecutor<DianQuanItem>(DP, query, param) {
            @Override
            public DianQuanItem doWithResultSet(ResultSet rs)
                    throws SQLException {
                while (rs.next()) {
                    DianQuanItem item = parseDianQuanItem(rs);
                    if (item != null) {
                        return item;
                    }
                }
                return null;
            }
        }.call();
    }

    private static List<DianQuanItem> findList(String query, Object... params) {
        return new JDBCBuilder.JDBCExecutor<List<DianQuanItem>>(DP, query, params) {
            @Override
            public List<DianQuanItem> doWithResultSet(ResultSet rs)
                    throws SQLException {
                List<DianQuanItem> words = new ArrayList<DianQuanItem>();
                while (rs.next()) {
                    DianQuanItem item = parseDianQuanItem(rs);
                    if (item != null) {
                        words.add(item);
                    }
                }
                return words;
            }
        }.call();
    }


    private static DianQuanItem parseDianQuanItem(ResultSet rs) {
        DianQuanItem item = new DianQuanItem();
        try {
            item.id = rs.getString("id");
            item.gid = rs.getString("gid");
            item.categoryId = rs.getInt("categoryId");
            item.categoryName = rs.getString("categoryName");
            item.site = rs.getString("site");
            item.title = rs.getString("title");
            item.subTitle = rs.getString("subTitle");
            item.isBrand = rs.getBoolean("isBrand");
            item.brandName = rs.getString("brandName");
            item.discountPrice = rs.getBigDecimal("discountPrice");
            item.normalPrice = rs.getBigDecimal("normalPrice");
            item.ratio = rs.getBigDecimal("ratio");
            item.hasFreight = rs.getBoolean("hasFreight");
            item.ratioType = rs.getString("ratioType");
            item.thumb = rs.getString("thumb");
            item.url = rs.getString("url");
            item.longPic = rs.getString("longPic");
            item.videoUrl = rs.getString("videoUrl");
            item.recommendedReason = rs.getString("recommendedReason");
            item.activity = rs.getString("activity");
            item.finalSales = rs.getInt("finalSales");
            item.hasCoupon = rs.getBoolean("hasCoupon");
            item.couponMoney = rs.getBigDecimal("couponMoney");
            item.couponUrl = rs.getString("couponUrl");
            item.couponTotal = rs.getInt("couponTotal");
            item.couponLatest = rs.getInt("couponLatest");
            item.timeline = rs.getLong("timeline");
            item.stoptime = rs.getLong("stoptime");
            item.newUrl = rs.getString("newUrl");
            item.createTs = rs.getLong("createTs");
            item.updateTs = rs.getLong("updateTs");
            return item;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
