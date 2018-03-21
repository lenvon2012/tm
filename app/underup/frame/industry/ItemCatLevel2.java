package underup.frame.industry;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;
import play.db.jpa.GenericModel;
import transaction.JDBCBuilder;
import transaction.DBBuilder.DataSrc;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

public class ItemCatLevel2 implements PolicySQLGenerator{
    private static final Logger log = LoggerFactory.getLogger(ItemCatLevel2.class);
    
    long id;
    
    private long cid;
    
    private String name;
    
    private boolean isParent;
    
    private long parentCid;
    
    private int level;
    
    private long levelOneCid;
    
    public static ItemCatLevel2 EMPTY = new ItemCatLevel2();
    
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    public ItemCatLevel2() {
    }
    
    public ItemCatLevel2(long levelOneCid, long cid, String name, boolean isParent, long parentCid, int level){
        this.levelOneCid = levelOneCid;
        this.cid = cid;
        this.name = name;
        this.isParent = isParent;
        this.parentCid = parentCid;
        this.level =level;
    }
    
    public long getCid(){
        return this.cid;
    }
    
    public void setCid(long cid){
        this.cid = cid;
    }
    
    public String getName(){
        return this.name;
    }
    
    public void setName(String name){
        this.name = name;
    }
    
    public boolean getIsParent(){
        return isParent;
    }
    
    public void setIsParent(boolean isParent){
        this.isParent = isParent;
    }
    
    public long getParentCid(){
        return this.parentCid;
    }
    
    public void setParentCid(long parentCid){
        this.parentCid = parentCid;
    }
    
    public int getLevel(){
        return this.level;
    }
    
    public void setLevel(int level){
        this.level = level;
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
        return this.id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean jdbcSave() {

        try {
            long existdId = findExistId(this.cid, this.name);

            if (existdId == 0L) {
                return this.rawInsert();
            } else {
                setId(existdId);
                return this.rawUpdate();
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }
    }
    public static String getTableName(long year, long month){
        Date date = new Date(year + month);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        String y = sdf.format(date);
        sdf = new SimpleDateFormat("MM");
        String m = sdf.format(date);
        String tableName = "item_cat_level2_" + y + "_" + m;
        return tableName;
    }
    
    public static String getCidPropExistSQL(){
        long[] ym  =CatTopSaleItemSQL.getTime();
        String tableName = getTableName(ym[0], ym[1]);
        String findCidPropExistSQL = "select id from " + tableName + " where cid = ? and name = ?";
        return findCidPropExistSQL;
    }
    public static long findExistId(long cid, String name) {
        String findCidPropExistSQL = getCidPropExistSQL();
        return dp.singleLongQuery(findCidPropExistSQL, cid, name);
    }

    public static String getInsertSQL(){
        long[] ym = CatTopSaleItemSQL.getTime();
        String tableName = getTableName(ym[0], ym[1]);
        String insertSQL = "insert into "+tableName +"(`levelOneCid`,`cid`,`name`,`isParent`, `parentCid`,`level`) values(?,?,?,?,?,?)";
        return insertSQL;
    }
    public boolean rawInsert() {
        String insertSQL = getInsertSQL();
        long id = dp.insert(false, insertSQL, this.levelOneCid, this.cid, this.name, this.isParent, this.parentCid, this.level);
        if (id > 0L) {
            setId(id);
            return true;
        } else {
            log.error("Insert Fails.....");
            return false;
        }

    }

    public static String getUpdateSQL(){
        long[] ym = CatTopSaleItemSQL.getTime();
        String tableName = getTableName(ym[0], ym[1]);
        String updateSQL = "update " + tableName +" set `levelOneCid` = ? ,`cid` = ?, `name` = ?, isParent = ?, parentCid = ?, level = ? where id = ?";
        return updateSQL;
    }
    public boolean rawUpdate() {
        String updateSQL = getUpdateSQL();
        long updateNum = dp.insert(false, updateSQL, this.levelOneCid, this.cid, this.name, this.isParent, this.parentCid, this.level, this.id);

        if (updateNum == 1) {
            log.info("update ok for :" + this.getId());
            return true;
        } else {
            log.error("update failed...for :" + this.getId());
            return false;
        }
    }

    @Override
    public String getIdName() {
        return null;
    }
    
    public static List<ItemCatLevel2> getLevel2(long levelOneCid, long year, long month) {
        String tableName = getTableName(year, month);
        log.info(" ------------------------------tableName:" + tableName + " levelOneCid:" + levelOneCid);
        String query = "select levelOneCid,cid, name, isParent, parentCid, level from " + tableName +" where levelOneCid = ?";
        return new JDBCBuilder.JDBCExecutor<List<ItemCatLevel2>>(dp, query, levelOneCid) {
            @Override
            public List<ItemCatLevel2> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemCatLevel2> catList = new ArrayList<ItemCatLevel2>();
                while (rs.next()) {
                    ItemCatLevel2 catPlay = new ItemCatLevel2(rs.getLong(1), rs.getLong(2), rs.getString(3), rs.getBoolean(4), rs.getLong(5), rs.getInt(6));
                    if (catPlay != null) {
                        catList.add(catPlay);
                    }
                }
                return catList;
            }

        }.call();
    }
    
    public static List<ItemCatLevel2> getLevel2Parent(long levelOneCid, long year, long month) {
        String tableName = getTableName(year, month);
        String query = "select levelOneCid, cid, name, isParent, parentCid, level from "+ tableName+" where levelOneCid = ? and level=2";
        return new JDBCBuilder.JDBCExecutor<List<ItemCatLevel2>>(dp, query, levelOneCid) {

            @Override
            public List<ItemCatLevel2> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemCatLevel2> catList = new ArrayList<ItemCatLevel2>();
                while (rs.next()) {
                    ItemCatLevel2 catPlay = new ItemCatLevel2(rs.getLong(1), rs.getLong(2), rs.getString(3), rs.getBoolean(4), rs.getLong(5), rs.getInt(6));
                    if (catPlay != null) {
                        catList.add(catPlay);
                    }
                }
                return catList;
            }

        }.call();
    }
    
    //得到父目录的子目录
    public static List<ItemCatLevel2> getLevel2Children(long levelOneCid, long year, long month, long parentCid){
        String tableName = getTableName(year, month);
        String query = "select levelOneCid, cid, name, isParent, parentCid, level from " + tableName +" where levelOneCid = ? and parentCid = ?";
        return new JDBCBuilder.JDBCExecutor<List<ItemCatLevel2>>(dp, query, levelOneCid, parentCid) {
            @Override
            public List<ItemCatLevel2> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemCatLevel2> catList = new ArrayList<ItemCatLevel2>();
                while (rs.next()) {
                    ItemCatLevel2 catPlay = new ItemCatLevel2(rs.getLong(1), rs.getLong(2), rs.getString(3), rs.getBoolean(4), rs.getLong(5), rs.getInt(6));
                    if (catPlay != null) {
                        catList.add(catPlay);
                    }
                }
                return catList;
            }

        }.call();
    }
    
    public static long getLevelOneCid(long levelTwoCid, long year, long month){
        String tableName = getTableName(year, month);
        String query = "select levelOneCid from " + tableName + " where cid = ?";
        return dp.singleLongQuery(query, levelTwoCid);
    }
    
}
