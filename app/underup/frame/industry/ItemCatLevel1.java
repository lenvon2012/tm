package underup.frame.industry;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
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

import com.ciaosir.client.CommonUtils;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

public class ItemCatLevel1 implements PolicySQLGenerator, Comparable{
    private static final Logger log = LoggerFactory.getLogger(ItemCatLevel1.class);
    
    private long id;
    
    private long cid;
    
    private String name;
    
    public static ItemCatLevel1 EMPTY = new ItemCatLevel1();
    
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    public ItemCatLevel1() {
    }
    
    public ItemCatLevel1(long cid, String name){
        this.cid = cid;
        this.name = name;
    }
    
    public long getCid(){
        return this.cid;
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
        String tableName = "item_cat_level1_" + y + "_" + m;
        return tableName;
    }
    
    public static long findExistId(long cid, String name) {
        long[] ym = CatTopSaleItemSQL.getTime();
        String findCidPropExistSQL = "select id from " + getTableName(ym[0], ym[1]) +" where cid=? and name = ?"; 
        return dp.singleLongQuery(findCidPropExistSQL,  cid, name);
    }

    public static String getInsertSQL(){
        long[] ym = CatTopSaleItemSQL.getTime();
        String insertSQL = "insert into "+getTableName(ym[0], ym[1]) + "(`cid`,`name`) values(?,?)";
        return insertSQL;
    }

    public boolean rawInsert() {
        String insertSQL = getInsertSQL();
        long id = dp.insert(false, insertSQL, this.cid, this.name);
        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails.....");
            return false;
        }

    }
    
    public String getUpdateSQL(){
        long ym[] = CatTopSaleItemSQL.getTime();
        String updateSQL = "update " + getTableName(ym[0], ym[1]) +" set `cid` = ?, `name` = ? where id = ?";
        return updateSQL;
    }
    
    public boolean rawUpdate() {
        String updateSQL = getUpdateSQL();
        long updateNum = dp.insert(false, updateSQL, this.cid, this.name, this.id);

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
    
    public static List<ItemCatLevel1> getLevel1(long year, long month) {
        String tableName = getTableName(year, month);
        String query = "select cid, name from " + tableName;
        List<ItemCatLevel1> itemCatLevel1s =  new JDBCBuilder.JDBCExecutor<List<ItemCatLevel1>>(dp, query) {
            @Override
            public List<ItemCatLevel1> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemCatLevel1> catList = new ArrayList<ItemCatLevel1>();
                while (rs.next()) {
                    ItemCatLevel1 catPlay = new ItemCatLevel1(rs.getLong(1), rs.getString(2));
                    if (catPlay != null) {
                        catList.add(catPlay);
                    }
                }
                return catList;
            }

        }.call();
        if(CommonUtils.isEmpty(itemCatLevel1s)) {//TODO
            return null;
        }
        Collections.sort(itemCatLevel1s);
        return itemCatLevel1s;
    }

    @Override
    public int compareTo(Object o) {
        ItemCatLevel1 itemCatLevel1 = (ItemCatLevel1)o;
        return  (int)(itemCatLevel1.getCid() - this.getCid());
    }
    

}
