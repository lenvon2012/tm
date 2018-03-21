//package models.mysql.fengxiao;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.PreparedStatement;
//import java.sql.Statement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.text.SimpleDateFormat;
//import java.util.*;
//
//import javax.persistence.Column;
//import javax.persistence.Entity;
//import javax.persistence.GeneratedValue;
//import javax.persistence.Id;
//import javax.persistence.Transient;
//
//import models.hotitem.CatHotItemPlay;
//import models.item.ItemCatPlay;
//import models.item.ItemPropSale;
//
//import org.apache.commons.lang.StringUtils;
//import org.codehaus.jackson.annotate.JsonAutoDetect;
//import org.codehaus.jackson.annotate.JsonProperty;
//import org.hibernate.annotations.Index;
//import org.hibernate.annotations.Table;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import autotitle.ItemPropAction;
//import autotitle.ItemPropAction.PropUnit;
//import bustbapi.BusAPI;
//
//import com.ciaosir.client.pojo.ItemThumb;
//import com.ciaosir.client.utils.JsonUtil;
//import com.ciaosir.commons.ClientException;
//import com.dbt.commons.Params.Comm;
////import com.mysql.jdbc.Statement;
//import com.taobao.api.domain.Item;
//
//import codegen.CodeGenerator.DBDispatcher;
//import codegen.CodeGenerator.PolicySQLGenerator;
//import play.Play;
//import play.db.jpa.GenericModel;
//import play.db.jpa.Model;
//import transaction.JDBCBuilder;
//import transaction.DBBuilder.DataSrc;
//import underup.frame.industry.ItemCatLevel2;
//import underup.frame.industry.ItemsCatArrange;
//
//@Entity(name = CatTopSaleItem.TABLE_NAME)
//@Table(appliesTo = CatTopSaleItem.TABLE_NAME, indexes = {
//        @Index(name = "indexNum", columnNames = { "backcid", "year", "month", "tradeNum" }),
//        @Index(name = "numIid", columnNames = { "numIid", "year", "month" }),
//        @Index(name = "yearAndMonth", columnNames={ "year", "month"}),
//        @Index(name = "sellerNick", columnNames={ "sellerNick"})})
//public class CatTopSaleItem extends GenericModel implements PolicySQLGenerator {
//    @Transient
//    private static final Logger log = LoggerFactory.getLogger(ItemPropSale.class);
//
//    @Transient
//    public static final String t = "cat_top_sale_item";
//
//    @Transient
//    public static final String TABLE_NAME = t;
//
//    @Id
//    @GeneratedValue
//    private Long id;
//
//    private Long numIid; // 得到item id
//
//    private int tradeNum; // 得到销量
//
//    private String picPath; // 得到图片url
//
//    private String title; // 得到title
//
//    private double price; // 得到价格
//
//    private String wangwang; // 得到旺旺名
//
//    private Long sellerId; // 卖家id
//    
//    private String sellerNick; // 卖家昵称
//
//    private Long listTime; // 上架时间
//
//    private Long delistTime; // 下架时间
//
//    @Column(columnDefinition = "text")
//    private String props; // 得到属性值
//
//    private Long backCid; // 后台cid
//
//    private Long ts;
//
//    private long year; // 得到爬取商品的年份
//
//    private long month; // 得到爬取商品的月份
//
//    private long frontcid;
//
//    @Transient
//    public static CatTopSaleItem EMPTY = new CatTopSaleItem();
//
//    @Transient
//    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
//
//    public CatTopSaleItem() {
//    }
//
//    @Override
//    public String toString() {
//        return "CatTopSaleItem[" + numIid + "," + numIid + "," + tradeNum + "," + picPath + "," + title + "," + price
//                + "," + wangwang + "," + sellerId + "," + sellerNick + "," + listTime + "," + delistTime + "," + props
//                + "," + backCid + "," + year + "," + "month" + " " + frontcid + "]";
//    }
//
//    public CatTopSaleItem(long frontcid, ItemThumb itemThumb, Item item) {
//        super();
//        this.numIid = itemThumb.getId();
//        this.tradeNum = itemThumb.getTradeNum();
//        this.picPath = itemThumb.getPicPath();
//        this.title = itemThumb.getFullTitle();
//        this.price = itemThumb.getPrice() / 100.00;
//        this.wangwang = itemThumb.getWangwang();
//        this.sellerId = itemThumb.getSellerId();
//        // 从淘宝api中Item获取
//        // Item item = getItem(itemThumb.getId());
//        this.sellerNick = item.getNick();
//        this.listTime = item.getListTime().getTime();
//        this.delistTime = item.getDelistTime().getTime();
//        this.props = parseItemProps(item);
//        this.backCid = item.getCid();
//
//        this.ts = System.currentTimeMillis();
//        long[] yearMonth = getTime();
//        this.year = yearMonth[0];
//        this.month = yearMonth[1];
//        this.frontcid = frontcid;
//    }
//
//    // //for test
//    // public CatTopSaleItem(long numIid, long year, long month, long backcid){
//    // this.numIid = numIid;
//    // this.year = year;
//    // this.month = month;
//    // this.backCid = backcid;
//    // }
//
//    private static final long[] getTime() {
//        // SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM", Locale.CHINA);
//        Date date = new Date(); // 当前时间
//        int year = date.getYear(); // 当前年份
//        int month = date.getMonth(); // 当前月份
//        Date dYear = new Date(year, 1, 1); // 算出今年一月一日的时间
//        Date dMonth = new Date(year, month, 1); // 计算出本月一日的时间
//        long[] yearMonth = new long[2];
//        yearMonth[0] = dYear.getTime(); // 得到年的时间
//        yearMonth[1] = dMonth.getTime() - yearMonth[0]; // 得到月的时间
//        return yearMonth;
//    }
//
//    private static Item getItem(Long id) throws ClientException {
//        Set<Long> ids = new HashSet<Long>();
//        ids.add(id);
//        Map<Long, Item> map;
//        map = new BusAPI.MultiItemApi(ids).execute();
//        return map.get(id);
//    }
//
//    private static String parseItemProps(Item tbItem) {
//        String propsName = tbItem.getPropsName();
//        String propAlias = tbItem.getPropertyAlias();
//        List<PropUnit> splitProp = ItemPropAction.mergePropAlis(propsName, propAlias);
//        String json = JsonUtil.getJson(splitProp);
//        return json;
//    }
//
//    public String getTitle() {
//        return title;
//    }
//
//    public String setTitle() {
//        return title;
//    }
//
//    public long getSellerId() {
//        return this.sellerId;
//    }
//
//    @Override
//    public String getTableName() {
//        return CatTopSaleItem.TABLE_NAME;
//    }
//
//    @Override
//    public String getTableHashKey() {
//        return null;
//    }
//
//    @Override
//    public String getIdColumn() {
//        return "id";
//    }
//
//    @Override
//    public Long getId() {
//        return id;
//    }
//
//    @Override
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    @Override
//    public String getIdName() {
//        return "id";
//    }
//
//    @Override
//    public boolean jdbcSave() {
//        try {
//            long existdId = isThumbIdExcited(this.numIid, this.year, this.month);
//            if (existdId <= 0L) {
//                return this.rawInsert();
//            } else {
//                setId(existdId);
//                return this.rawUpdate();
//            }
//        } catch (Exception e) {
//        }
//        return true;
//    }
//
//    public long getYear() {
//        return this.year;
//    }
//
//    public long getMonth() {
//        return this.month;
//    }
//
//    public long getCid() {
//        return this.backCid;
//    }
//
//    @Transient
//    static String getExcitedId = "select id from cat_top_sale_item where numIid = ? and year = ? and month=?";
//
//    public static Long isThumbIdExcited(Long numIid, long year, long month) {
//        return dp.singleLongQuery(getExcitedId, numIid, year, month);
//    }
//
//    @Transient
//    static String insertSQL = "insert into cat_top_sale_item(`numIid`,`tradeNum`,`picPath`,`title`,`price`,`wangwang`,`sellerId`,`sellerNick`,`listTime`,`delistTime`,`props`,`backCid`, `ts`, `year`, `month`, `frontcid`)"
//            + " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
//
//    public boolean rawInsert() {
//        Long id = dp.insert(false, insertSQL, this.numIid, this.tradeNum, this.picPath, this.title, this.price,
//                this.wangwang, this.sellerId, this.sellerNick, this.listTime, this.delistTime, this.props,
//                this.backCid, this.ts, this.year, this.month, this.frontcid);
//
//        if (id > 0L) {
//            // setId(id);
//            return true;
//        } else {
//            log.error("Insert Fails.....");
//            return false;
//        }
//
//    }
//
//    @Transient
//    static String updateSQL = "update cat_top_sale_item set   `tradeNum` = ?, `picPath` = ?, `title` = ?, `price` = ?, `wangwang` = ?, "
//            + "`sellerId` = ?,`sellerNick` = ?, `listTime` = ?, `delistTime` = ?, `props` = ?, `backCid` = ?, `ts` = ?, `frontcid` = ? , `numIid` = ? , `year` = ?"
//            + ", `month` = ? where id=?";
//
//    public boolean rawUpdate() {
//        long updateNum = dp.insert(false, updateSQL, this.tradeNum, this.picPath, this.title, this.price,
//                this.wangwang, this.sellerId, this.sellerNick, this.listTime, this.delistTime, this.props,
//                this.backCid, this.ts, this.frontcid, this.numIid, this.year, this.month, this.id);
//        if (updateNum == 1) {
//            log.info("update ok for :" + this.getId());
//            return true;
//        } else {
//            log.error("update failed...for :" + this.getId());
//            return false;
//        }
//    }
//
//    public static List<String> getTitleWords(Long backCid, long year, long month) {
//        String query = "select title from cat_top_sale_item where backCid=? and year=? and month=?";
//        return new JDBCBuilder.JDBCExecutor<List<String>>(dp, query, backCid, year, month) {
//            @Override
//            public List<String> doWithResultSet(ResultSet rs) throws SQLException {
//                List<String> wordList = new ArrayList<String>();
//                while (rs.next()) {
//                    String temp = rs.getString(1);
//                    wordList.add(temp);
//                }
//                return wordList;
//            }
//
//        }.call();
//    }
//
//    @JsonAutoDetect
//    public static class TopSaleItem {
//        @JsonProperty
//        public String picPath;
//
//        @JsonProperty
//        public String title;
//
//        @JsonProperty
//        public double price;
//
//        @JsonProperty
//        public String wangwang;
//
//        @JsonProperty
//        public int tradeNum;
//
//        @JsonProperty
//        public Long numIid;
//
//        public TopSaleItem(String picPath, String title, double price, String wangwang, int tradeNum, Long numIid) {
//            this.picPath = picPath;
//            this.title = title;
//            this.price = price;
//            this.wangwang = wangwang;
//            this.tradeNum = tradeNum;
//            this.numIid = numIid;
//        }
//    }
//
//    // 根据后台cid取出前100宝贝
//    public static List<TopSaleItem> getTopSale100(Long backCid, long year, long month, int offset, int size) {
//        String query = "select picPath, title, price, wangwang, tradeNum, numIid from cat_top_sale_item where backCid=? and year=? and month=? "
//                + "order by tradeNum desc limit ? offset ? ";
//        return new JDBCBuilder.JDBCExecutor<List<TopSaleItem>>(dp, query, backCid, year, month, size, offset) {
//            @Override
//            public List<TopSaleItem> doWithResultSet(ResultSet rs) throws SQLException {
//                List<TopSaleItem> topItem = new ArrayList<TopSaleItem>();
//                while (rs.next()) {
//                    String picPath = rs.getString(1);
//                    String title = rs.getString(2);
//                    double price = rs.getDouble(3);
//                    String wangwang = rs.getString(4);
//                    int tradeNum = rs.getInt(5);
//                    long numIid = rs.getLong(6);
//                    topItem.add(new TopSaleItem(picPath, title, price, wangwang, tradeNum, numIid));
//                }
//                return topItem;
//            }
//        }.call();
//    }
//
//    // 取出后台cid为父类的前100宝贝
//    public static List<TopSaleItem> getTopSale100Children(long cid, long year, long month, int offset, int size) {
//        List<Long> childrenCids = ItemsCatArrange.getChildrenCids(cid, year, month);
//        if (childrenCids == null) {
//            log.error("error can't get children cids");
//            return new ArrayList<TopSaleItem>();
//        }
//        String query = StringUtils.EMPTY;
//        String cids = StringUtils.EMPTY;
//        cids = "backcid=" + childrenCids.get(0);
//        query = "(select picPath, title, price, wangwang, tradeNum, numIid from cat_top_sale_item where " + cids
//                + " and year=" + year + " and month=" + month + " order by tradeNum desc limit 100)";
//        for (int i = 1; i < childrenCids.size(); ++i) {
//            cids = "backcid=" + childrenCids.get(i);
//            query += " union all (select picPath, title, price, wangwang, tradeNum, numIid from cat_top_sale_item where "
//                    + cids + " and year=" + year + " and month=" + month + " order by tradeNum desc limit 100)";
//        }
//        query += " order by tradeNum desc limit ? offset ?";
//        log.info("backcid:" + childrenCids);
//        return new JDBCBuilder.JDBCExecutor<List<TopSaleItem>>(dp, query, size, offset) {
//            @Override
//            public List<TopSaleItem> doWithResultSet(ResultSet rs) throws SQLException {
//                List<TopSaleItem> topItem = new ArrayList<TopSaleItem>();
//                while (rs.next()) {
//                    String picPath = rs.getString(1);
//                    String title = rs.getString(2);
//                    double price = rs.getDouble(3);
//                    String wangwang = rs.getString(4);
//                    int tradeNum = rs.getInt(5);
//                    long numIid = rs.getLong(6);
//                    topItem.add(new TopSaleItem(picPath, title, price, wangwang, tradeNum, numIid));
//                }
//                return topItem;
//            }
//        }.call();
//    }
//
//    public static int getTopSizeChildren(long cid, long year, long month) {
//        List<Long> childrenCids = ItemsCatArrange.getChildrenCids(cid, year, month);
//        if (childrenCids == null) {
//            log.error("error can't get children cids");
//            return 0;
//        }
//        String query = StringUtils.EMPTY;
//        String cids = StringUtils.EMPTY;
//        int count = 0;
//        for (int i = 0; i < childrenCids.size(); ++i) {
//            cids = "backcid=" + childrenCids.get(i);
//            query = "select count(1) from cat_top_sale_item where " + cids + " and year=" + year + " and month="
//                    + month;
//            count += (int) dp.singleLongQuery(query);
//            if (count >= 100)
//                return 100;
//        }
//        return count;
//    }
//
//    public static int getTopSize(Long backCid, long year, long month) {
//        String query = "select count(1) from cat_top_sale_item where backCid=? and year=? and month=?";
//        int count = (int) dp.singleLongQuery(query, backCid, year, month);
//        if (count > 100)
//            return 100;
//        else
//            return count;
//    }
//
//    public static List<Long> getYearLong() {
//        String query = "select year from cat_top_sale_item group by year";
//        return new JDBCBuilder.JDBCExecutor<List<Long>>(dp, query) {
//            @Override
//            public List<Long> doWithResultSet(ResultSet rs) throws SQLException {
//                List<Long> year = new ArrayList<Long>();
//                while (rs.next()) {
//                    long t = rs.getLong(1);
//                    year.add(t);
//                }
//                return year;
//            }
//        }.call();
//    }
//
//    public static List<Long> getMonthLong(long year) {
//        String query = "select month from cat_top_sale_item where year=? group by month";
//        return new JDBCBuilder.JDBCExecutor<List<Long>>(dp, query, year) {
//            @Override
//            public List<Long> doWithResultSet(ResultSet rs) throws SQLException {
//                List<Long> month = new ArrayList<Long>();
//                while (rs.next()) {
//                    long t = rs.getLong(1);
//                    month.add(t);
//                }
//                return month;
//            }
//        }.call();
//    }
//
//    public static List<CatTopSaleItem> findByCid(Long backCid, long year, long month) {
//        String query = "select " + SelectAllProperty + " from " + getSqlTableName()
//                + " where backCid = ? and year = ? and month = ?";
//        return findListByJDBC(query, backCid, year, month);
//    }
//
//    private static List<CatTopSaleItem> findListByJDBC(String query, Object... params) {
//        return new JDBCBuilder.JDBCExecutor<List<CatTopSaleItem>>(dp, query, params) {
//            @Override
//            public List<CatTopSaleItem> doWithResultSet(ResultSet rs) throws SQLException {
//                List<CatTopSaleItem> catItemList = new ArrayList<CatTopSaleItem>();
//
//                while (rs.next()) {
//                    CatTopSaleItem catItem = parseCatHotItemPlay(rs);
//                    if (catItem != null) {
//                        catItemList.add(catItem);
//                    }
//                }
//
//                return catItemList;
//
//            }
//
//        }.call();
//
//    }
//    
//    public static class PriceTradenum {
//        private double price;
//
//        private int tradeNum;
//
//        public PriceTradenum(double price, int tradeNum) {
//            this.price = price;
//            this.tradeNum = tradeNum;
//        }
//
//        public double getPrice() {
//            return price;
//        }
//
//        public void setPrice(double price) {
//            this.price = price;
//        }
//
//        public int getTradeNum() {
//            return this.tradeNum;
//        }
//
//        public void setTradeNum(int tradeNum) {
//            this.tradeNum = tradeNum;
//        }
//    }
//
//    public static List<PriceTradenum> getPriceTradenum(List<Long> cids, long year, long month) {
//        if (cids == null)
//            return new ArrayList<PriceTradenum>();
//        long cid = cids.get(0);
//        String cidS = "(select price, tradeNum from cat_top_sale_item where  backcid=" + cid + " and year=" + year + " and month=" + month + ")";
//        for (int i = 1; i < cids.size(); ++i) {
//            cidS += " union all (select price, tradeNum from cat_top_sale_item where backcid="  + cids.get(i) + " and year=" + year + " and month=" + month + ")";
//        }
//        long t1, t2;
//        t1 = System.currentTimeMillis();
//        List<PriceTradenum> priceTradeNums =  new JDBCBuilder.JDBCExecutor<List<PriceTradenum>>(dp, cidS) {
//            @Override
//            public List<PriceTradenum> doWithResultSet(ResultSet rs) throws SQLException {
//                List<PriceTradenum> priceTradeNums = new ArrayList<PriceTradenum>();
//
//                while (rs.next()) {
//                    priceTradeNums.add(new PriceTradenum(rs.getDouble(1), rs.getInt(2)));
//                }
//
//                return priceTradeNums;
//
//            }
//
//        }.call();
//        t2 = System.currentTimeMillis();
//        log.info("----------------------------------------------------------------------get the delist need the time is " + (t2 - t1));
//        return priceTradeNums;
//    }
//    
//    public static List<Long> getDelistTime(List<Long> cids, long year, long month) {
//        if (cids == null)
//            return new ArrayList<Long>();
//        long cid = cids.get(0);
//        String cidS = "(select delistTime from cat_top_sale_item where  backcid=" + cid + " and year=" + year + " and month=" + month + ")";
//        for (int i = 1; i < cids.size(); ++i) {
//            cidS += " union all (select delistTime from cat_top_sale_item where backcid="  + cids.get(i) + " and year=" + year + " and month=" + month + ")";
//        }
//        long t1, t2;
//        t1 = System.currentTimeMillis();
//        List<Long> priceTradeNums =  new JDBCBuilder.JDBCExecutor<List<Long>>(dp, cidS) {
//            @Override
//            public List<Long> doWithResultSet(ResultSet rs) throws SQLException {
//                List<Long> delistTimes = new ArrayList<Long>();
//
//                while (rs.next()) {
//                    delistTimes.add(rs.getLong(1));
//                }
//                return delistTimes;
//            }
//
//        }.call();
//        
//        t2 = System.currentTimeMillis();
//        log.info("----------------------------------------------------------------------get the delist need the time is " + (t2 - t1));
//        return priceTradeNums;
//    }
//    
//    private static String getSqlTableName() {
//        return TABLE_NAME;
//    }
//
//    private static final String SelectAllProperty = " numIid,tradeNum,picPath,title,price,"
//            + "wangwang,sellerId, sellerNick, listTime,delistTime,props,backCid," + "ts,year, month,frontcid";
//
//    private void setNumIid(Long numIid) {
//        this.numIid = numIid;
//    }
//
//    public Long getNumIid() {
//        return this.numIid;
//    }
//
//    private void setTradeNum(int tradeNum) {
//        this.tradeNum = tradeNum;
//    }
//
//    public int getTradeNum() {
//        return this.tradeNum;
//    }
//
//    private void setPicPath(String picPath) {
//        this.picPath = picPath;
//    }
//
//    private void setTitle(String title) {
//        this.title = title;
//    }
//
//    private void setPrice(double price) {
//        this.price = price;
//    }
//
//    public String getWangWang() {
//        return this.wangwang;
//    }
//
//    private void setWangwang(String wangwang) {
//        this.wangwang = wangwang;
//    }
//
//    private void setSellerId(Long sellerId) {
//        this.sellerId = sellerId;
//    }
//
//    private void setSellerNick(String sellerNick) {
//        this.sellerNick = sellerNick;
//    }
//
//    private void setListTime(Long listTime) {
//        this.listTime = listTime;
//    }
//
//    public long getDelistTime() {
//        return this.delistTime;
//    }
//
//    private void setDelistTime(Long delistTime) {
//        this.delistTime = delistTime;
//    }
//
//    private void setProps(String props) {
//        this.props = props;
//    }
//
//    private void setBackCid(Long backCid) {
//        this.backCid = backCid;
//    }
//
//    private void setTs(Long ts) {
//        this.ts = ts;
//    }
//
//    public double getPrice() {
//        return this.price;
//    }
//
//    public void setYear(long year) {
//        this.year = year;
//    }
//
//    public void setMonth(long month) {
//        this.month = month;
//    }
//
//    public void setFrontcid(long frontCid) {
//        this.frontcid = frontCid;
//    }
//
//    public String getProps() {
//        return this.props;
//    }
//
//    private static CatTopSaleItem parseCatHotItemPlay(ResultSet rs) {
//        try {
//            CatTopSaleItem item = new CatTopSaleItem();
//
//            item.setNumIid(rs.getLong(1));
//            item.setTradeNum(rs.getInt(2));
//            item.setPicPath(rs.getString(3));
//            item.setTitle(rs.getString(4));
//            item.setPrice(rs.getDouble(5));
//            item.setWangwang(rs.getString(6));
//            item.setSellerId(rs.getLong(7));
//            item.setSellerNick(rs.getString(8));
//            item.setListTime(rs.getLong(9));
//            item.setDelistTime(rs.getLong(10));
//            item.setProps(rs.getString(11));
//            item.setBackCid(rs.getLong(12));
//            item.setTs(rs.getLong(13));
//            item.setYear(rs.getLong(14));
//            item.setMonth(rs.getLong(15));
//            item.setFrontcid(rs.getLong(16));
//            return item;
//
//        } catch (Exception ex) {
//            log.error(ex.getMessage(), ex);
//            return null;
//        }
//    }
//
//    @JsonAutoDetect
//    public static class HotWordInfo {
//        @JsonProperty
//        Long cid;
//
//        @JsonProperty
//        Long month;
//
//        @JsonProperty
//        Long year;
//
//        public HotWordInfo(Long cid, Long month, Long year) {
//            this.cid = cid;
//            this.month = month;
//            this.year = year;
//        }
//
//        public long getCid() {
//            return cid;
//        }
//
//        public long getMonth() {
//            return month;
//        }
//
//        public long getYear() {
//            return year;
//        }
//    }
//
//    public static List<HotWordInfo> getHotWordInfo() {
//        String query = "select backcid, year, month from cat_top_sale_item group by backcid, year, month";
//        return new JDBCBuilder.JDBCExecutor<List<HotWordInfo>>(dp, query) {
//            @Override
//            public List<HotWordInfo> doWithResultSet(ResultSet rs) throws SQLException {
//                List<HotWordInfo> hotWordInfo = new ArrayList<HotWordInfo>();
//                while (rs.next()) {
//                    long cid = rs.getLong(1);
//                    long year = rs.getLong(2);
//                    long month = rs.getLong(3);
//                    hotWordInfo.add(new HotWordInfo(cid, month, year));
//                }
//                return hotWordInfo;
//            }
//        }.call();
//    }
//
//    public static List<HotWordInfo> getCidInfos() {
//        long[] yearAndMonth = getTime();
//        final long year = yearAndMonth[0];
//        final long month = yearAndMonth[1];
//        String query = "select backcid from cat_top_sale_item where year=" + year + " and month=" + month
//                + " group by backcid";
//        return new JDBCBuilder.JDBCExecutor<List<HotWordInfo>>(dp, query) {
//            @Override
//            public List<HotWordInfo> doWithResultSet(ResultSet rs) throws SQLException {
//                List<HotWordInfo> hotWordInfo = new ArrayList<HotWordInfo>();
//                while (rs.next()) {
//                    long cid = rs.getLong(1);
//                    long y = year;
//                    long m = month;
//                    hotWordInfo.add(new HotWordInfo(cid, m, y));
//                }
//                return hotWordInfo;
//            }
//        }.call();
//    }
//
//    public static void insertPatch(long frontCid, List<ItemThumb> itemThumbs, Map<Long, Item> map) {
//        Properties prop = Play.configuration;
//        Connection conn = null;
//
//        String url = prop.getProperty("base.db.url");
//        if (StringUtils.isEmpty(url)) {
//            url = prop.getProperty("db.url");
//        }
//
//        String user = prop.getProperty("base.db.user");
//        if (StringUtils.isEmpty(user)) {
//            user = prop.getProperty("db.user");
//        }
//
//        String pwd = prop.getProperty("base.db.pass");
//        if (StringUtils.isEmpty(pwd)) {
//            pwd = prop.getProperty("db.pass");
//        }
//
//        try {
//            conn = DriverManager.getConnection(url, user, pwd);
//            ResultSet rs = null;
//            conn.setAutoCommit(false);
//            PreparedStatement prest = conn.prepareStatement(insertSQL);
//            PreparedStatement up = conn
//                    .prepareStatement("select id from cat_top_sale_item where numiid=? and year = ? and month = ?");
//            PreparedStatement updatePatch = conn.prepareStatement(updateSQL);
//            log.info("the num to insert is " + itemThumbs.size());
//            int insertNum = 0, updateNum = 0;
//            for (ItemThumb item : itemThumbs) {
//                long[] yearMonth = getTime();
//                up.setLong(1, item.getId());
//                up.setLong(2, yearMonth[0]);
//                up.setLong(3, yearMonth[1]);
//                rs = up.executeQuery();
//                long flag = 0L;
//                if (rs.next()) {
//                    flag = rs.getLong(1);
//                }
//                
//                if (flag == 0L) {
//                    prest.setLong(1, item.getId());
//                    prest.setInt(2, item.getTradeNum());
//                    prest.setString(3, item.getPicPath());
//                    prest.setString(4, item.getFullTitle());
//                    prest.setDouble(5, item.getPrice() / 100.0);
//                    prest.setString(6, item.getWangwang());
//                    prest.setLong(7, item.getSellerId());
//
//                    Item ite = map.get(item.getId());
//                    prest.setString(8, ite.getNick());
//                    if (ite.getListTime() == null) {
//                        log.error("get the delist time failed...");
//                        continue;
//                    }
//                    prest.setLong(9, ite.getListTime().getTime());
//                    prest.setLong(10, ite.getDelistTime().getTime());
//                    prest.setString(11, parseItemProps(ite));
//
//                    prest.setLong(12, ite.getCid());
//                    prest.setLong(13, System.currentTimeMillis());
//
//                    prest.setLong(14, yearMonth[0]);
//                    prest.setLong(15, yearMonth[1]);
//                    prest.setLong(16, frontCid);
//
//                    prest.addBatch();
//                    insertNum++;
//                } else {
//                    // 批量更新
////                  //update cat_top_sale_item set   `tradeNum` = ?, `picPath` = ?, `title` = ?, `price` = ?, `wangwang` = ?, "
////                  + "`sellerId` = ?,`sellerNick` = ?, `listTime` = ?, `delistTime` = ?, `props` = ?, `backCid` = ?, `ts` = ?, `frontcid` = ? , `numIid` = ? , `year` = ?"
////                  + ", `month` = ? where id=?";
//                    updatePatch.setLong(1, item.getTradeNum());
//                    updatePatch.setString(2, item.getPicPath());
//                    updatePatch.setString(3, item.getFullTitle());
//                    updatePatch.setDouble(4, item.getPrice() / 100.0);
//                    updatePatch.setString(5, item.getWangwang());
//                    updatePatch.setLong(6, item.getSellerId());
//
//                    Item ite = map.get(item.getId());
//                    updatePatch.setString(7, ite.getNick());
//                    if (ite.getListTime() == null) {
//                        log.error("get the delist time failed...");
//                        continue;
//                    }
//                    updatePatch.setLong(8, ite.getListTime().getTime());
//                    updatePatch.setLong(9, ite.getDelistTime().getTime());
//                    updatePatch.setString(10, parseItemProps(ite));
//
//                    updatePatch.setLong(11, ite.getCid());
//                    updatePatch.setLong(12, System.currentTimeMillis());
//                    updatePatch.setLong(13, frontCid);
//                    updatePatch.setLong(14, item.getId());
//                    updatePatch.setLong(15, yearMonth[0]);
//                    updatePatch.setLong(16, yearMonth[1]);
//                    updatePatch.setLong(17, flag);
//                    updatePatch.addBatch();
//                    updateNum++;
//                }
//            }
//            log.info("the insert num is " + insertNum + " and the update num is " + updateNum);
//            if(insertNum > 0)
//                prest.executeBatch();
//            if(updateNum > 0)
//                updatePatch.executeBatch();
//            conn.commit();
//            conn.close();
//        } catch (SQLException e) {
//            log.error("-------------------------------------------------------------------------------------------------------------------------connect to database fial........");
//        }
//
//    }
//
//    public static List<Long> getFrontCid() {
//        String query = "select distinct(frontcid) from cat_top_sale_item where year=? and month=?";
//        long year, month;
//        long[] ym = getTime();
//        year = ym[0];
//        month = ym[1];
//        return new JDBCBuilder.JDBCExecutor<List<Long>>(dp, query, year, month) {
//            @Override
//            public List<Long> doWithResultSet(ResultSet rs) throws SQLException {
//                List<Long> frontcids = new ArrayList<Long>();
//                while (rs.next()) {
//                    long frontcid = rs.getLong(1);
//                    frontcids.add(frontcid);
//                }
//                return frontcids;
//            }
//        }.call();
//    }
//
//    public static List<Long[]> getYearMonth() {
//        String query = "select year, month from cat_top_sale_item group by year, month";
//        return new JDBCBuilder.JDBCExecutor<List<Long[]>>(dp, query) {
//            @Override
//            public List<Long[]> doWithResultSet(ResultSet rs) throws SQLException {
//                List<Long[]> yearmonths = new ArrayList<Long[]>();
//                Long[] yearmonth = new Long[2];
//                while (rs.next()) {
//                    long year = rs.getLong(1);
//                    long month = rs.getLong(2);
//                    yearmonth[0] = year;
//                    yearmonth[1] = month;
//                    yearmonths.add(yearmonth);
//                }
//                return yearmonths;
//            }
//        }.call();
//    }
//
//    public static boolean findIfExisted(long cid, long year, long month) {
//        if (dp.singleLongQuery(
//                "select backcid from cat_top_sale_item where backcid = ? and year = ? and month = ?  limit 1", cid,
//                year, month) <= 0L)
//            return false;
//        else
//            return true;
//    }
//    
//    public static class ShopSimpleInfo{
//        private String wangwang;
//        
//        private Long sellerId;
//        
//        private int tradeNum;
//        
//        public ShopSimpleInfo(String wangwang, Long sellerId, int tradeNum){
//            this.wangwang = wangwang;
//            this.sellerId = sellerId;
//            this.tradeNum = tradeNum;
//        }
//        
//        public String getWangWang(){
//            return this.wangwang;
//        }
//        
//        public void setWangWang(String wangwang){
//            this.wangwang = wangwang;
//        }
//        
//        public long getSellerId(){
//            return this.sellerId;
//        }
//        
//        public void setSellerId(long sellerId){
//            this.sellerId = sellerId;
//        }
//        
//        public int getTradeNum(){
//            return tradeNum;
//        }
//        
//        public void setTradeNum(int tradeNum){
//            this.tradeNum = tradeNum;
//        }
//    }
//    
//    public static List<ShopSimpleInfo> findShopInfo(long cid, long year, long month){
//        ItemCatPlay itemCatPlay = ItemCatPlay.findByCid(cid);
//        List<Long> cids = new ArrayList<Long>();
//        if(itemCatPlay.isParent()){
//            cids = ItemsCatArrange.getChildrenCids(cid, year, month);
//        }else{
//            cids.add(cid);
//        }
//        
//        String query = "(select sellerNick, sellerId, tradeNum from cat_top_sale_item where backcid=" + cids.get(0) + " and year=" + year + " and month=" + month +
//                " order by tradeNum desc limit 100)";
//        for(int i = 1; i < cids.size(); ++i){
//            query += " union all (select sellerNick, sellerId, tradeNum from cat_top_sale_item where backcid=" + cids.get(i) + " and year=" + year + " and month=" + month +
//                    " order by tradeNum desc limit 100)";
//        }
//        return new JDBCBuilder.JDBCExecutor<List<ShopSimpleInfo>>(dp, query) {
//            @Override
//            public List<ShopSimpleInfo> doWithResultSet(ResultSet rs) throws SQLException {
//                List<ShopSimpleInfo> shopInfos = new ArrayList<ShopSimpleInfo>();
//                while (rs.next()) {
//                    String wangwang = rs.getString(1);
//                    long sellerId = rs.getLong(2);
//                    int tradeNum = rs.getInt(3);
//                    shopInfos.add(new ShopSimpleInfo(wangwang, sellerId, tradeNum));
//                }
//                return shopInfos;
//            }
//        }.call();
//        
//        
//    }
//    
//    
//    public static List<String> getAllShopWangWang(){
//        String query = "select distinct(sellerNick) from cat_top_sale_item";
//        return new JDBCBuilder.JDBCExecutor<List<String>>(dp, query) {
//            @Override
//            public List<String> doWithResultSet(ResultSet rs) throws SQLException {
//                List<String> wangwangs = new ArrayList<String>();
//                while (rs.next()) {
//                    String wangwang = rs.getString(1);
//                    wangwangs.add(wangwang);
//                }
//                return wangwangs;
//            }
//        }.call();
//    }
//    
//    public static List<Long> getYearInfos(){
//        String query = "select distinct(year) from cat_top_sale_item";
//        return new JDBCBuilder.JDBCExecutor<List<Long>>(dp, query) {
//            @Override
//            public List<Long> doWithResultSet(ResultSet rs) throws SQLException {
//                List<Long> years = new ArrayList<Long>();
//                while (rs.next()) {
//                    Long year = rs.getLong(1);
//                    years.add(year);
//                }
//                return years;
//            }
//        }.call();
//    }
//    
//    public static List<Long> getMonthInfos(long year){
//        String query = "select distinct(month) from cat_top_sale_item where year=?";
//        return new JDBCBuilder.JDBCExecutor<List<Long>>(dp, query, year){
//            @Override
//            public List<Long> doWithResultSet(ResultSet rs) throws SQLException {
//                List<Long> months = new ArrayList<Long>();
//                while (rs.next()) {
//                    Long year = rs.getLong(1);
//                    months.add(year);
//                }
//                return months;
//            }
//        }.call();
//    }
//    
//    public static class YearMonthInfo{
//        long year;
//        
//        long month;
//        
//        public YearMonthInfo(long year, long month){
//            this.year = year;
//            this.month = month;
//        }
//        
//        public long getYear(){
//            return this.year;
//        }
//        
//        public long getMonth(){
//            return this.month;
//        }
//        
//        public void setYear(long year){
//            this.year = year;
//        }
//        
//        public void setMonth(long month){
//            this.month = month;
//        }
//    }
//    
//    public static List<YearMonthInfo> getYearMonthInfos(){
//        String query = "select year, month from cat_top_sale_item group by year, month";
//        return new JDBCBuilder.JDBCExecutor<List<YearMonthInfo>>(dp, query){
//            @Override
//            public List<YearMonthInfo> doWithResultSet(ResultSet rs) throws SQLException {
//                List<YearMonthInfo> yearMonthInfos = new ArrayList<YearMonthInfo>();
//                while (rs.next()) {
//                    long year = rs.getLong(1);
//                    long month = rs.getLong(2);
//                    yearMonthInfos.add(new YearMonthInfo(year, month));
//                }
//                return yearMonthInfos;
//            }
//        }.call();
//    }
//
//}
