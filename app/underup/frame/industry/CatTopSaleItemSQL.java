package underup.frame.industry;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import models.item.ItemCatPlay;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import underup.frame.industry.LevelPicShopInfo.ShopLevelPicInfos;
import autotitle.ItemPropAction;
import autotitle.ItemPropAction.PropUnit;
import bustbapi.BusAPI;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.pojo.ItemThumb;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.commons.ClientException;
import com.taobao.api.domain.Item;

public class CatTopSaleItemSQL implements PolicySQLGenerator {
    public static final Logger log = LoggerFactory.getLogger(CatTopSaleItemSQL.class);

    public static CatTopSaleItemSQL EMPTY = new CatTopSaleItemSQL();

    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

    public CatTopSaleItemSQL() {
    }
    
    private Long id;

    private Long numIid; // 得到item id

    private int tradeNum; // 得到销量

    private String picPath; // 得到图片url

    private String title; // 得到title

    private double price; // 得到价格

    private String wangwang; // 得到旺旺名

    private Long sellerId; // 卖家id
    
    private String sellerNick; // 卖家昵称

    private Long listTime; // 上架时间

    private Long delistTime; // 下架时间

    private String props; // 得到属性值

    private Long backCid; // 后台cid

    private Long ts;

    private long year; // 得到爬取商品的年份

    private long month; // 得到爬取商品的月份

    private long frontcid;
    
    public static long[] getTime() {
		Date date = new Date(); // 当前时间
		int year = date.getYear(); // 当前年份
		int month = date.getMonth(); // 当前月份
		Date dYear = new Date(year, 1, 1); // 算出今年一月一日的时间
		Date dMonth = new Date(year, month, 1); // 计算出本月一日的时间
		long[] yearMonth = new long[2];
		yearMonth[0] = dYear.getTime(); // 得到年的时间
		yearMonth[1] = dMonth.getTime() - yearMonth[0]; // 得到月的时间
//		System.out.println("year millstimes:" + yearMonth[0] + " month millstimes:" + yearMonth[1]);
		return yearMonth;
	}
    
    public static YearMonth gettime(){
    	Calendar date = Calendar.getInstance();
		int year = date.get(Calendar.YEAR);
		int month = date.get(Calendar.MONTH);
		date.set(year, 1, 1, 0, 0, 0);
		long yearMillis = date.getTimeInMillis();
		yearMillis = (yearMillis /1000) * 1000;
		date.set(year, month, 1, 0, 0, 0);
		long monthMillis = date.getTimeInMillis();
		monthMillis = (monthMillis/1000) * 1000;
		monthMillis = monthMillis - yearMillis;
		return new YearMonth(yearMillis, monthMillis);
    }
    
    public static class YearMonth{
    	public YearMonth(long year, long month){
    		this.year = year;
    		this.month = month;
    	}
    	public long year;
    	public long month;
    	
    }
    

    private static Item getItem(Long id) throws ClientException {
        Set<Long> ids = new HashSet<Long>();
        ids.add(id);
        Map<Long, Item> map;
        map = new BusAPI.MultiItemApi(ids).execute();
        return map.get(id);
    }

    public static String parseItemProps(Item tbItem) {
        String propsName = tbItem.getPropsName();
        String propAlias = tbItem.getPropertyAlias();
        List<PropUnit> splitProp = ItemPropAction.mergePropAlis(propsName, propAlias);
        String json = JsonUtil.getJson(splitProp);
        return json;
    }


    public static String getInsertSQL(long year, long month) {
        String insertSQL = "insert into "+getTableName(year, month)+"(`numIid`,`tradeNum`,`picPath`,`title`,`price`,`wangwang`," +
        		"`sellerId`,`sellerNick`,`listTime`,`delistTime`,`props`,`backCid`, `ts`, `year`, `month`, `frontcid`)"
                + " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        return insertSQL;
    }
    
    public static String getUpdateSQL(long year, long month){
        String updateSQL = "update " + getTableName(year, month)+" set   `tradeNum` = ?, `picPath` = ?, `title` = ?, `price` = ?, `wangwang` = ?, "
                + "`sellerId` = ?,`sellerNick` = ?, `listTime` = ?, `delistTime` = ?, `props` = ?, `backCid` = ?, `ts` = ?, `frontcid` = ? , `numIid` = ? , `year` = ?"
                + ", `month` = ? where id=?";
        return updateSQL;
    }

    public static String getTableName(long year, long month) {
        Date date = new Date(year + month);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        String y = sdf.format(date);
        sdf = new SimpleDateFormat("MM");
        String m = sdf.format(date);
        return "cat_top_sale_item_" + y + "_" + m;
    }

    public static List<String> getTitleWords(Long backCid, long year, long month) {
        String tableName = getTableName(year, month);
        String query = "select title from " + tableName + " where backCid=?";
        return new JDBCBuilder.JDBCExecutor<List<String>>(dp, query, backCid) {
            @Override
            public List<String> doWithResultSet(ResultSet rs) throws SQLException {
                List<String> wordList = new ArrayList<String>();
                while (rs.next()) {
                    String temp = rs.getString(1);
                    wordList.add(temp);
                }
                return wordList;
            }

        }.call();
    }

    @JsonAutoDetect
    public static class TopSaleItem {
        @JsonProperty
        public String picPath;

        @JsonProperty
        public String title;

        @JsonProperty
        public double price;

        @JsonProperty
        public String wangwang;

        @JsonProperty
        public int tradeNum;

        @JsonProperty
        public Long numIid;
        
        @JsonProperty
        public int level;

        public TopSaleItem(String picPath, String title, double price, String wangwang, int tradeNum, Long numIid, int level) {
            this.picPath = picPath;
            this.title = title;
            this.price = price;
            this.wangwang = wangwang;
            this.tradeNum = tradeNum;
            this.numIid = numIid;
            this.level = level;
        }
    }

    // 根据后台cid取出前100宝贝
    public static List<TopSaleItem> getTopSale100(Long backCid, long year, long month, int offset, int size) {
        String tableName = getTableName(year, month);
        String query = "select picPath, title, price, wangwang, tradeNum, numIid from " + tableName
                + " where backCid=?" + " order by tradeNum desc limit ? offset ? ";
        return new JDBCBuilder.JDBCExecutor<List<TopSaleItem>>(dp, query, backCid, size, offset) {
            @Override
            public List<TopSaleItem> doWithResultSet(ResultSet rs) throws SQLException {
                List<TopSaleItem> topItem = new ArrayList<TopSaleItem>();
                while (rs.next()) {
                    String picPath = rs.getString(1);
                    String title = rs.getString(2);
                    double price = rs.getDouble(3);
                    String wangwang = rs.getString(4);
                    int tradeNum = rs.getInt(5);
                    long numIid = rs.getLong(6);
                    ShopLevelPicInfos levelInfo = LevelPicShopInfo.getShoLevelPic(wangwang);
                    int level = levelInfo.getLevel();
                    topItem.add(new TopSaleItem(picPath, title, price, wangwang, tradeNum, numIid, level));
                }
                return topItem;
            }
        }.call();
    }

    // 取出后台cid为父类的前100宝贝
    public static List<TopSaleItem> getTopSale100Children(long cid, long year, long month, int offset, int size) {
        String tableName = getTableName(year, month);
        List<Long> childrenCids = ItemsCatArrange.getChildrenCids(cid, year, month);
        if (childrenCids == null) {
            log.error("error can't get children cids");
            return new ArrayList<TopSaleItem>();
        }
        String query = StringUtils.EMPTY;
        String cids = StringUtils.EMPTY;
        cids = "backcid=" + childrenCids.get(0);
        query = "(select picPath, title, price, wangwang, tradeNum, numIid from " + tableName + " where " + cids
                + " order by tradeNum desc limit 100)";
        for (int i = 1; i < childrenCids.size(); ++i) {
            cids = "backcid=" + childrenCids.get(i);
            query += " union all (select picPath, title, price, wangwang, tradeNum, numIid from " + tableName
                    + " where " + cids + " order by tradeNum desc limit 100)";
        }
        query += " order by tradeNum desc limit ? offset ?";
        log.info("backcid:" + childrenCids);
        return new JDBCBuilder.JDBCExecutor<List<TopSaleItem>>(dp, query, size, offset) {
            @Override
            public List<TopSaleItem> doWithResultSet(ResultSet rs) throws SQLException {
                List<TopSaleItem> topItem = new ArrayList<TopSaleItem>();
                while (rs.next()) {
                    String picPath = rs.getString(1);
                    String title = rs.getString(2);
                    double price = rs.getDouble(3);
                    String wangwang = rs.getString(4);
                    int tradeNum = rs.getInt(5);
                    long numIid = rs.getLong(6);
                    ShopLevelPicInfos levelInfo = LevelPicShopInfo.getShoLevelPic(wangwang);
                    topItem.add(new TopSaleItem(picPath, title, price, wangwang, tradeNum, numIid, levelInfo.getLevel()));
                }
                return topItem;
            }
        }.call();
    }

    public static int getTopSizeChildren(long cid, long year, long month) {
        String tableName = getTableName(year, month);
        List<Long> childrenCids = ItemsCatArrange.getChildrenCids(cid, year, month);
        if (childrenCids == null) {
            log.error("error can't get children cids");
            return 0;
        }
        String query = StringUtils.EMPTY;
        String cids = StringUtils.EMPTY;
        int count = 0;
        for (int i = 0; i < childrenCids.size(); ++i) {
            cids = "backcid=" + childrenCids.get(i);
            query = "select count(1) from " + tableName + " where " + cids;
            count += (int) dp.singleLongQuery(query);
            if (count >= 100)
                return 100;
        }
        return count;
    }

    public static int getTopSize(Long backCid, long year, long month) {
        String tableName = getTableName(year, month);
        String query = "select count(1) from " + tableName + " where backCid=?";
        int count = (int) dp.singleLongQuery(query, backCid);
        if (count > 100)
            return 100;
        else
            return count;
    }

    public static class PriceTradenum {
        private double price;

        private int tradeNum;

        public PriceTradenum(double price, int tradeNum) {
            this.price = price;
            this.tradeNum = tradeNum;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public int getTradeNum() {
            return this.tradeNum;
        }

        public void setTradeNum(int tradeNum) {
            this.tradeNum = tradeNum;
        }
    }

    public static List<PriceTradenum> getPriceTradenum(List<Long> cids, long year, long month) {
        String tableName = getTableName(year, month);
        if (cids == null)
            return new ArrayList<PriceTradenum>();
        long cid = cids.get(0);
        String cidS = "(select price, tradeNum from " + tableName + " where  backcid=" + cid + ")";
        for (int i = 1; i < cids.size(); ++i) {
            cidS += " union all (select price, tradeNum from " + tableName + " where backcid=" + cids.get(i) + ")";
        }
        long t1, t2;
        t1 = System.currentTimeMillis();
        List<PriceTradenum> priceTradeNums = new JDBCBuilder.JDBCExecutor<List<PriceTradenum>>(dp, cidS) {
            @Override
            public List<PriceTradenum> doWithResultSet(ResultSet rs) throws SQLException {
                List<PriceTradenum> priceTradeNums = new ArrayList<PriceTradenum>();

                while (rs.next()) {
                    priceTradeNums.add(new PriceTradenum(rs.getDouble(1), rs.getInt(2)));
                }

                return priceTradeNums;

            }

        }.call();
        t2 = System.currentTimeMillis();
        log.info("----------------------------------------------------------------------get the delist need the time is "
                + (t2 - t1));
        return priceTradeNums;
    }

    public static List<Long> getDelistTime(List<Long> cids, long year, long month) {
        String tableName = getTableName(year, month);
        if (cids == null)
            return new ArrayList<Long>();
        long cid = cids.get(0);
        String cidS = "(select delistTime from " + tableName + " where  backcid=" + cid + ")";
        for (int i = 1; i < cids.size(); ++i) {
            cidS += " union all (select delistTime from " + tableName + " where backcid=" + cids.get(i) + ")";
        }
        long t1, t2;
        t1 = System.currentTimeMillis();
        List<Long> priceTradeNums = new JDBCBuilder.JDBCExecutor<List<Long>>(dp, cidS) {
            @Override
            public List<Long> doWithResultSet(ResultSet rs) throws SQLException {
                List<Long> delistTimes = new ArrayList<Long>();

                while (rs.next()) {
                    delistTimes.add(rs.getLong(1));
                }
                return delistTimes;
            }

        }.call();

        t2 = System.currentTimeMillis();
        log.info("----------------------------------------------------------------------get the delist need the time is "
                + (t2 - t1));
        return priceTradeNums;
    }

    private static final String SelectAllProperty = " numIid,tradeNum,picPath,title,price,"
            + "wangwang,sellerId, sellerNick, listTime,delistTime,props,backCid," + "ts,year, month,frontcid";

    @JsonAutoDetect
    public static class HotWordInfo {
        @JsonProperty
        Long cid;

        @JsonProperty
        Long month;

        @JsonProperty
        Long year;

        public HotWordInfo(Long cid, Long month, Long year) {
            this.cid = cid;
            this.month = month;
            this.year = year;
        }

        public long getCid() {
            return cid;
        }

        public long getMonth() {
            return month;
        }

        public long getYear() {
            return year;
        }
    }

    public static List<CatTopSaleItemSQL> findByCid(Long backCid, long year, long month) {
        String query = "select " + SelectAllProperty + " from " + getTableName(year, month)
                + " where backCid = ?";
        return findListByJDBC(query, backCid);
    }

    private static List<CatTopSaleItemSQL> findListByJDBC(String query, Object... params) {
        return new JDBCBuilder.JDBCExecutor<List<CatTopSaleItemSQL>>(dp, query, params) {
            @Override
            public List<CatTopSaleItemSQL> doWithResultSet(ResultSet rs) throws SQLException {
                List<CatTopSaleItemSQL> catItemList = new ArrayList<CatTopSaleItemSQL>();

                while (rs.next()) {
                    CatTopSaleItemSQL catItem = parseCatHotItemPlay(rs);
                    if (catItem != null) {
                        catItemList.add(catItem);
                    }
                }

                return catItemList;

            }

        }.call();

    }
    private void setNumIid(Long numIid) {
        this.numIid = numIid;
    }

    public Long getNumIid() {
        return this.numIid;
    }

    private void setTradeNum(int tradeNum) {
        this.tradeNum = tradeNum;
    }

    public int getTradeNum() {
        return this.tradeNum;
    }

    private void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    private void setTitle(String title) {
        this.title = title;
    }

    private void setPrice(double price) {
        this.price = price;
    }

    public String getWangWang() {
        return this.wangwang;
    }

    private void setWangwang(String wangwang) {
        this.wangwang = wangwang;
    }

    private void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    private void setSellerNick(String sellerNick) {
        this.sellerNick = sellerNick;
    }

    private void setListTime(Long listTime) {
        this.listTime = listTime;
    }

    public long getDelistTime() {
        return this.delistTime;
    }

    private void setDelistTime(Long delistTime) {
        this.delistTime = delistTime;
    }

    private void setProps(String props) {
        this.props = props;
    }
    
    public long getSellerId(){
        return this.sellerId;
    }
    private void setBackCid(Long backCid) {
        this.backCid = backCid;
    }

    private void setTs(Long ts) {
        this.ts = ts;
    }

    public double getPrice() {
        return this.price;
    }

    public void setYear(long year) {
        this.year = year;
    }

    public void setMonth(long month) {
        this.month = month;
    }

    public void setFrontcid(long frontCid) {
        this.frontcid = frontCid;
    }

    public String getProps() {
        return this.props;
    }
    
    public long getYear() {
        return this.year;
    }

    public long getMonth() {
        return this.month;
    }

    public long getCid() {
        return this.backCid;
    }
    
    private static CatTopSaleItemSQL parseCatHotItemPlay(ResultSet rs) {
        try {
            CatTopSaleItemSQL item = new CatTopSaleItemSQL();

            item.setNumIid(rs.getLong(1));
            item.setTradeNum(rs.getInt(2));
            item.setPicPath(rs.getString(3));
            item.setTitle(rs.getString(4));
            item.setPrice(rs.getDouble(5));
            item.setWangwang(rs.getString(6));
            item.setSellerId(rs.getLong(7));
            item.setSellerNick(rs.getString(8));
            item.setListTime(rs.getLong(9));
            item.setDelistTime(rs.getLong(10));
            item.setProps(rs.getString(11));
            item.setBackCid(rs.getLong(12));
            item.setTs(rs.getLong(13));
            item.setYear(rs.getLong(14));
            item.setMonth(rs.getLong(15));
            item.setFrontcid(rs.getLong(16));
            return item;

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

    public static List<HotWordInfo> getCidInfos() {
        long[] yearAndMonth = getTime();
        final long year = yearAndMonth[0];
        final long month = yearAndMonth[1];
        String tableName = getTableName(year, month);
        String query = "select backcid from " + tableName + " group by backcid";
        return new JDBCBuilder.JDBCExecutor<List<HotWordInfo>>(dp, query) {
            @Override
            public List<HotWordInfo> doWithResultSet(ResultSet rs) throws SQLException {
                List<HotWordInfo> hotWordInfo = new ArrayList<HotWordInfo>();
                while (rs.next()) {
                    long cid = rs.getLong(1);
                    long y = year;
                    long m = month;
                    hotWordInfo.add(new HotWordInfo(cid, m, y));
                }
                return hotWordInfo;
            }
        }.call();
    }
    
    //插入到以年和月分表的数据库中
    public static void insertPatch(long frontCid, List<ItemThumb> itemThumbs, Map<Long, Item> map) {
        long[] yearMonth = getTime();
        String tableName = getTableName(yearMonth[0], yearMonth[1]);
        Properties prop = Play.configuration;
        Connection conn = null;
        String url = prop.getProperty("base.db.url");
        if (StringUtils.isEmpty(url)) {
            url = prop.getProperty("db.url");
        }
        String user = prop.getProperty("base.db.user");
        if (StringUtils.isEmpty(user)) {
            user = prop.getProperty("db.user");
        }

        String pwd = prop.getProperty("base.db.pass");
        if (StringUtils.isEmpty(pwd)) {
            pwd = prop.getProperty("db.pass");
        }

        try {
            conn = DriverManager.getConnection(url, user, pwd);
            ResultSet rs = null;
            conn.setAutoCommit(false);
            PreparedStatement prest = conn.prepareStatement(getInsertSQL(yearMonth[0], yearMonth[1]));
            PreparedStatement up = conn
                    .prepareStatement("select id from "+ tableName +" where numiid=?");
            PreparedStatement updatePatch = conn.prepareStatement(getUpdateSQL(yearMonth[0], yearMonth[1]));
//            log.info("the num to insert is " + itemThumbs.size());
            int insertNum = 0, updateNum = 0;
            for (ItemThumb item : itemThumbs) {
                up.setLong(1, item.getId());
                rs = up.executeQuery();
                long flag = 0L;
                if (rs.next()) {
                    flag = rs.getLong(1);
                }

                if (flag == 0L) {
                    prest.setLong(1, item.getId());
                    prest.setInt(2, item.getTradeNum());
                    prest.setString(3, item.getPicPath());
                    prest.setString(4, item.getFullTitle());
                    prest.setDouble(5, item.getPrice() / 100.0);
                    prest.setString(6, item.getWangwang());
                    prest.setLong(7, item.getSellerId());
                    Item ite = map.get(item.getId());
                    prest.setString(8, ite.getNick());
                    if (ite.getListTime() == null) {
                        log.error("get the delist time failed...");
                        continue;
                    }
                    prest.setLong(9, ite.getListTime().getTime());
                    prest.setLong(10, ite.getDelistTime().getTime());
                    prest.setString(11, ite.getProps());

                    prest.setLong(12, ite.getCid());
                    prest.setLong(13, System.currentTimeMillis());

                    prest.setLong(14, yearMonth[0]);
                    prest.setLong(15, yearMonth[1]);
                    prest.setLong(16, frontCid);

                    prest.addBatch();
                    insertNum++;
                } else {
                    updatePatch.setLong(1, item.getTradeNum());
                    updatePatch.setString(2, item.getPicPath());
                    updatePatch.setString(3, item.getFullTitle());
                    updatePatch.setDouble(4, item.getPrice() / 100.0);
                    updatePatch.setString(5, item.getWangwang());
                    updatePatch.setLong(6, item.getSellerId());

                    Item ite = map.get(item.getId());
                    updatePatch.setString(7, ite.getNick());
                    if (ite.getListTime() == null) {
                        log.error("get the delist time failed...");
                        continue;
                    }
                    updatePatch.setLong(8, ite.getListTime().getTime());
                    updatePatch.setLong(9, ite.getDelistTime().getTime());
                    updatePatch.setString(10, ite.getProps());

                    updatePatch.setLong(11, ite.getCid());
                    updatePatch.setLong(12, System.currentTimeMillis());
                    updatePatch.setLong(13, frontCid);
                    updatePatch.setLong(14, item.getId());
                    updatePatch.setLong(15, yearMonth[0]);
                    updatePatch.setLong(16, yearMonth[1]);
                    updatePatch.setLong(17, flag);
                    updatePatch.addBatch();
                    updateNum++;
                }
            }
//            log.info("the insert num is " + insertNum + " and the update num is " + updateNum);
            if (insertNum > 0)
                prest.executeBatch();
            if (updateNum > 0)
                updatePatch.executeBatch();
            conn.commit();
            conn.close();
        } catch (SQLException e) {
            log.error("-------------------------------------------------------------------------------------------------------------------------connect to database fial........");
            log.error(e.getMessage(), e);
        }

    }

    public static List<Long> getFrontCid() {
        long[] ym = getTime();
        log.info("year:" + ym[0] + " month:" + ym[1]);
        String tableName = getTableName(ym[0], ym[1]);
        log.info("table_name:" + tableName);
        String query = "select distinct(frontCid) from "+ tableName;
        return new JDBCBuilder.JDBCExecutor<List<Long>>(dp, query) {
            @Override
            public List<Long> doWithResultSet(ResultSet rs) throws SQLException {
                List<Long> frontcids = new ArrayList<Long>();
                while (rs.next()) {
                    long frontcid = rs.getLong(1);
                    frontcids.add(frontcid);
                }
                return frontcids;
            }
        }.call();
    }

    public static boolean findIfExisted(long cid) {
        long[] ym = getTime();
        long year = ym[0];
        long month = ym[1];
        String tableName = getTableName(year, month);
        String query = "select backcid from " +tableName +" where backcid = ? limit 1";
        if (dp.singleLongQuery(query, cid) <= 0L)
            return false;
        else
            return true;
    }

    public static class ShopSimpleInfo {
        private String wangwang;

        private Long sellerId;

        private int tradeNum;

        public ShopSimpleInfo(String wangwang, Long sellerId, int tradeNum) {
            this.wangwang = wangwang;
            this.sellerId = sellerId;
            this.tradeNum = tradeNum;
        }

        public String getWangWang() {
            return this.wangwang;
        }

        public void setWangWang(String wangwang) {
            this.wangwang = wangwang;
        }

        public long getSellerId() {
            return this.sellerId;
        }

        public void setSellerId(long sellerId) {
            this.sellerId = sellerId;
        }

        public int getTradeNum() {
            return tradeNum;
        }

        public void setTradeNum(int tradeNum) {
            this.tradeNum = tradeNum;
        }
    }

    public static List<ShopSimpleInfo> findShopInfo(long cid, long year, long month) {
        String tableName = getTableName(year, month);
        ItemCatPlay itemCatPlay = ItemCatPlay.findByCid(cid);
        List<Long> cids = new ArrayList<Long>();
        if (itemCatPlay.isParent()) {
            cids = ItemsCatArrange.getChildrenCids(cid, year, month);
        } else {
            cids.add(cid);
        }

        String query = "(select sellerNick, sellerId, tradeNum from " + tableName +" where backcid=" + cids.get(0)
                + " order by tradeNum desc limit 100)";
        for (int i = 1; i < cids.size(); ++i) {
            query += " union all (select sellerNick, sellerId, tradeNum from " + tableName +" where backcid="
                    + cids.get(i) + " order by tradeNum desc limit 100)";
        }
        return new JDBCBuilder.JDBCExecutor<List<ShopSimpleInfo>>(dp, query) {
            @Override
            public List<ShopSimpleInfo> doWithResultSet(ResultSet rs) throws SQLException {
                List<ShopSimpleInfo> shopInfos = new ArrayList<ShopSimpleInfo>();
                while (rs.next()) {
                    String wangwang = rs.getString(1);
                    long sellerId = rs.getLong(2);
                    int tradeNum = rs.getInt(3);
                    shopInfos.add(new ShopSimpleInfo(wangwang, sellerId, tradeNum));
                }
                return shopInfos;
            }
        }.call();
    }

    public static List<String> getAllShopWangWang() {
        long[] ym = getTime();
        String query = "select distinct(sellerNick) from " + getTableName(ym[0], ym[1]);
        return new JDBCBuilder.JDBCExecutor<List<String>>(dp, query) {
            @Override
            public List<String> doWithResultSet(ResultSet rs) throws SQLException {
                List<String> wangwangs = new ArrayList<String>();
                while (rs.next()) {
                    String wangwang = rs.getString(1);
                    wangwangs.add(wangwang);
                }
                return wangwangs;
            }
        }.call();
    }

    public static class YearMonthInfo {
        long year;

        long month;

        public YearMonthInfo(long year, long month) {
            this.year = year;
            this.month = month;
        }

        public long getYear() {
            return this.year;
        }

        public long getMonth() {
            return this.month;
        }

        public void setYear(long year) {
            this.year = year;
        }

        public void setMonth(long month) {
            this.month = month;
        }
    }
    
    public static boolean isExistTable(String tableName) {
        String sql = "SHOW TABLES LIKE ? ";
        String table = dp.singleStringQuery(sql, tableName);
        if(!StringUtils.isEmpty(table)){
            String sqlCount = "select id from " + tableName + " limit 0, 1";
            long singleLongQuery = dp.singleLongQuery(sqlCount);
            return singleLongQuery > 0L;
        }
        return false;
    }

    @Override
    public String getTableName() {
        return null;
    }

    @Override
    public String getTableHashKey() {
        return null;
    }

    @Override
    public String getIdColumn() {
        return null;
    }

    @Override
    public Long getId() {
        return null;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean jdbcSave() {
        return false;
    }

    @Override
    public String getIdName() {
        return null;
    }

}
