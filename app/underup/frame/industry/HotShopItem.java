//package underup.frame.industry;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Properties;
//
//import javax.persistence.Entity;
//import javax.persistence.GeneratedValue;
//import javax.persistence.Id;
//import javax.persistence.Transient;
//
//import org.apache.commons.lang.StringUtils;
//import org.codehaus.jackson.annotate.JsonAutoDetect;
//import org.codehaus.jackson.annotate.JsonProperty;
//import org.slf4j.*;
//
//import com.ciaosir.commons.ClientException;
//
//import codegen.CodeGenerator.DBDispatcher;
//import codegen.CodeGenerator.PolicySQLGenerator;
//
//import play.Play;
//import play.db.jpa.GenericModel;
//import transaction.JDBCBuilder;
//import transaction.DBBuilder.DataSrc;
//import underup.frame.industry.HotShop.HotShopInfo;
//
//@Entity(name = HotShopItem.TABLE_NAME)
//public class HotShopItem extends GenericModel implements PolicySQLGenerator{
//    @Transient
//    private static final Logger log = LoggerFactory.getLogger(HotShopItem.class);
//    
//    @Transient
//    public static final String TABLE_NAME = "hot_shop_item";
//    
//    @Id
//    @GeneratedValue
//    long id;
//    
//    private String picPath;
//    
//    private String nick;
//    
//    private long sid;
//    
//    private long sale;
//    
//    private int level;
//    
//    private long sellerId;
//    
//    private long year;
//    
//    private long month;
//    
//    private long cid;
//    
//    @Transient
//    public static HotShopItem EMPTY = new HotShopItem();
//    
//    @Transient
//    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
//    
//    public HotShopItem() {
//    }
//    
//    public HotShopItem(String picPath, String nick, long sid, long sale, int level, long sellerId, long year, long month, long cid){
//        this.picPath = picPath;
//        this.nick =nick;
//        this.sid = sid;
//        this.sale = sale;
//        this.level=level;
//        this.sellerId = sellerId;
//        this.year=year;
//        this.month = month;
//        this.cid = cid;
//    }
//    
//    
//    public void setSale(long sale){
//        this.sale = sale;
//    }
//    
//    public long getSale(){
//        return this.sale;
//    }
//    
//    public String getPicPath(){
//        return this.picPath;
//    }
//    
//    public String getNick(){
//        return this.nick;
//    }
//    
//    public long getSid(){
//        return this.sid;
//    }
//    
//    public long getLevel(){
//        return this.level;
//    }
//    
//    public long getSellerId(){
//        return this.sellerId;
//    }
//    
//    public long getYear(){
//        return this.year;
//    }
//    
//    public long getMonth(){
//        return this.month;
//    }
//    
//    public long getCid(){
//        return this.cid;
//    }
//    @Override
//    public String getTableName() {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public String getTableHashKey() {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public String getIdColumn() {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public Long getId() {
//        // TODO Auto-generated method stub
//        return this.id;
//    }
//
//    @Override
//    public void setId(Long id) {
//        // TODO Auto-generated method stub
//        this.id = id;
//    }
//    
//    @Override
//    public boolean jdbcSave() {
//
//        try {
//            long existdId = findExistId(this.nick, this.cid, this.year, this.month);
//
//            if (existdId == 0L) {
//                return this.rawInsert();
//            } else {
//                setId(existdId);
//                return this.rawUpdate();
//            }
//        } catch (Exception e) {
//            log.warn(e.getMessage(), e);
//            return false;
//        }
//    }
//
//    @Transient
//    static String findCidPropExistSQL = "select id from `hot_shop_item` where nick = ? and cid = ? and year = ? and month=?";
//
//    public static long findExistId(String nick, long cid, long year, long month) {
//        return dp.singleLongQuery(findCidPropExistSQL, nick, cid, year, month);
//    }
//    
//    @Transient
//    static String insertSQL = "insert into `hot_shop_item`(`picPath`,`nick`,`sid`,`sale`,`level`,`sellerId`,`year`, `month`, `cid`) values(?,?,?,?,?,?,?,?,?)";
//
//    public boolean rawInsert() {
//
//        long id = dp.insert(false, insertSQL, this.picPath, this.nick, this.sid, this.sale, this.level, this.sellerId, this.year, this.month, this.cid);
//        if (id > 0L) {
//            setId(id);
//            log.info("Insert Success!");
//            return true;
//        } else {
//            log.error("Insert Fails.....");
//            return false;
//        }
//
//    }
//
//    @Transient
//    static String updateSQL = "update `hot_shop_item` set  `picPath` = ?, `nick` = ?, `sid` = ?, `sale` = ?, `level` = ?, `sellerId` = ?, `year` = ?, `month` = ?, `cid` = ? where `id` = ? ";
//
//    public boolean rawUpdate() {
//        long updateNum = dp.insert(false, updateSQL, this.picPath, this.nick, this.sid, this.sale, this.level, this.sellerId,
//                this.year, this.month, this.cid, this.getId());
//
//        if (updateNum == 1) {
//            log.info("update ok for :" + this.getId());
//            return true;
//        } else {
//            log.error("update failed...for :" + this.getId());
//            return false;
//        }
//    }
//
//    @Override
//    public String getIdName() {
//        // TODO Auto-generated method stub
//        return null;
//    }
//    
//    
//    @JsonAutoDetect
//    public static class HotShopInfo{
//        @JsonProperty
//        private String picPath;     //主图
//        
//        @JsonProperty
//        private String nick;        //店铺
//        
//        @JsonProperty
//        private Long sid;           //shop id
//        
//        @JsonProperty    
//        private long sale;           //店铺销售
//        
//        @JsonProperty
//        private int level;          //店铺分数
//        
//        public HotShopInfo(String picPath, String nick, Long sid, long sale, int level){
//            this.picPath = picPath;
//            this.nick = nick;
//            this.sid = sid;
//            this.sale = sale;
//            this.level = level;
//        }
//        public void setSale(long sale){
//            this.sale = sale;
//        }
//        public long getSale(){
//            return this.sale;
//        }
//    }
//    
//    public static List<HotShopInfo> getItemProps(long cid, long year, long month) {
//        String query = "select picPath, nick, sid, sale, level from hot_shop_item where cid = ? and year =? and month = ?";
//        return new JDBCBuilder.JDBCExecutor<List<HotShopInfo>>(dp, query, cid, year, month) {
//
//            @Override
//            public List<HotShopInfo> doWithResultSet(ResultSet rs) throws SQLException {
//                List<HotShopInfo> catList = new ArrayList<HotShopInfo>();
//                while (rs.next()) {
//                    String picPath = rs.getString(1);
//                    String nick = rs.getString(2);
//                    long sid = rs.getLong(3);
//                    long sale = rs.getLong(4);
//                    int level = rs.getInt(5);
//                    HotShopInfo catPlay = new HotShopInfo(picPath, nick, sid, sale, level);
//                    if (catPlay != null) {
//                        catList.add(catPlay);
//                    }
//                }
//                return catList;
//            }
//
//        }.call();
//    }
//    
//    public static List<HotShopInfo> getShopInfo(long cid, long year, long month, int offset, int ps) throws ClientException{
//        String query = "select picPath, nick, sid, sale, level from hot_shop_item where cid = ? and year =? and month = ? limit ? offset ?";
//        return new JDBCBuilder.JDBCExecutor<List<HotShopInfo>>(dp, query, cid, year, month, ps, offset) {
//
//            @Override
//            public List<HotShopInfo> doWithResultSet(ResultSet rs) throws SQLException {
//                List<HotShopInfo> catList = new ArrayList<HotShopInfo>();
//                while (rs.next()) {
//                    String picPath = rs.getString(1);
//                    String nick = rs.getString(2);
//                    long sid = rs.getLong(3);
//                    long sale = rs.getLong(4);
//                    int level = rs.getInt(5);
//                    HotShopInfo catPlay = new HotShopInfo(picPath, nick, sid, sale, level);
//                    if (catPlay != null) {
//                        catList.add(catPlay);
//                    }
//                }
//                return catList;
//            }
//
//        }.call();
//    }
//    
//    public static void insertPatch( Map<String, HotShopItem> hotShopItems){
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
//        try {
//            conn = DriverManager.getConnection(url, user, pwd);
//            conn.setAutoCommit(false);
//            PreparedStatement prest = conn.prepareStatement(insertSQL);
//            List<String> shopItems = new ArrayList<String>(hotShopItems.keySet());
//            for (String shopItem : shopItems) {
//                HotShopItem hotShopItem = hotShopItems.get(shopItem);
//                prest.setString(1, hotShopItem.getPicPath());
//                prest.setString(2, hotShopItem.getNick());
//                prest.setLong(3, hotShopItem.getSid());
//                prest.setLong(4, hotShopItem.getSale());
//                prest.setLong(5, hotShopItem.getLevel());
//                prest.setLong(6, hotShopItem.getSellerId());
//                prest.setLong(7, hotShopItem.getYear());
//                prest.setLong(8, hotShopItem.getMonth());
//                prest.setLong(9, hotShopItem.getCid());
//                prest.addBatch();
//            }
//            prest.executeBatch();
//            conn.commit();
//            conn.close();
//        } catch (SQLException e) {
//            // TODO Auto-generated catch block
//            log.error("connect to database fial........");
//        }
//    }
//    
//}
