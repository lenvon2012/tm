/**
 * 
 */
package models.item;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.pojo.PageOffset;

/**
 * @author navins
 * @date: Mar 6, 2014 3:58:49 PM
 */

@Entity(name = ItemPropSale.TABLE_NAME)
@JsonIgnoreProperties(value = { "dataSrc", "persistent", "entityId", "entityId", "tableHashKey", "persistent",
        "tableName", "idName", "idColumn", })
@Table(appliesTo = ItemPropSale.TABLE_NAME, indexes = {
        @Index(name = "cidpid", columnNames = {
                "cid", "pid"
        })
})
public class ItemPropSale extends GenericModel implements PolicySQLGenerator {

    @Transient
    private static final Logger log = LoggerFactory.getLogger(ItemPropSale.class);

    @Transient
    public static final String TAG = "ItemPropSale";

    @Transient
    public static final String TABLE_NAME = "item_prop_sale";

    @Transient
    public static ItemPropSale EMPTY = new ItemPropSale();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

    @Id
    @GeneratedValue						//定义自动增长的组件的策略
    public Long id;

    private long cid;					//目录id

    private long pid;				//属性id

    private long vid;				//属性值id

    private String pname;		//属性名

    private String vname;		//属性值名

    private long sale;		//销售量

    private long ts;		//在开始定义时的时间
    
    //uttp
    private long totalPrice;
   // private long year;
    //private long month;

    public ItemPropSale() {

    }
    //modify by uttp
    public ItemPropSale(long cid, long pid, long vid, String pname, String vname, long sale, long totalPrice) {
        super();				//需要对父类进行定义
        this.cid = cid;
        this.pid = pid;
        this.vid = vid;
        this.pname = pname;
        this.vname = vname;
        this.sale = sale > 0L ? sale : 0L;
        this.ts = System.currentTimeMillis();
        this.totalPrice = totalPrice;
        //this.year = year;
        //this.month = month;
    }
    //modify by uttp
    public ItemPropSale(ResultSet rs) throws SQLException {
        this.id = rs.getLong(1);
        this.cid = rs.getLong(2);
        this.pid = rs.getLong(3);
        this.vid = rs.getLong(4);
        this.pname = rs.getString(5);
        this.vname = rs.getString(6);
        this.sale = rs.getLong(7);
        this.ts = rs.getLong(8);
        this.totalPrice = rs.getLong(9);
        //this.year = rs.getLong(10);
        //this.month = rs.getLong(11);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getCid() {
        return cid;
    }

    public void setCid(long cid) {
        this.cid = cid;
    }

    public long getPid() {
        return pid;
    }

    public void setPid(long pid) {
        this.pid = pid;
    }

    public long getVid() {
        return vid;
    }

    public void setVid(long vid) {
        this.vid = vid;
    }

    public String getPname() {
        return pname;
    }

    public void setPname(String pname) {
        this.pname = pname;
    }

    public String getVname() {
        return vname;
    }

    public void setVname(String vname) {
        this.vname = vname;
    }

    public long getSale() {
        return sale;
    }

    public void setSale(long sale) {
        this.sale = sale;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }
    
    public long getTotalPrice(){
        return this.totalPrice;
    }
    
    public void setTotalPrice(long totalPrice){
        this.totalPrice = totalPrice;
    }
    
//    public long getYear(){
//        return this.year;
//    }
//    
//    public void setYear(long year){
//        this.year = year;
//    }
//    public long getMonth(){
//        return this.month;
//    }
//    
//    public void setMonth(long month){
//        this.month = month;
//    }
    @Override
    public String toString() {
        return "ItemPropSale [id=" + id + ", cid=" + cid + ", pid=" + pid + ", vid=" + vid + ", pname=" + pname
                + ", vname=" + vname + ", sale=" + sale + ", ts=" + ts + ", totalSale=" + totalPrice + "]";
    }

    @Override
    public String getTableName() {
        // TODO Auto-generated method stub
        return TABLE_NAME;
    }

    @Override
    public String getTableHashKey() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getIdColumn() {
        // TODO Auto-generated method stub
        return "id";
    }

    @Override
    public String getIdName() {
        // TODO Auto-generated method stub
        return "id";
    }

    @Override
    public boolean jdbcSave() {

        try {
            long existdId = findExistId(this.cid, this.pid, this.vid);

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

    @Transient
    static String findCidPropExistSQL = "select id from `item_prop_sale` where cid = ? and pid = ? and vid = ?";

    public static long findExistId(long cid, long pid, long vid) {
        return dp.singleLongQuery(findCidPropExistSQL, cid, pid, vid);
    }

    @Transient
    static String insertSQL = "insert into `item_prop_sale`(`cid`,`pid`,`vid`,`pname`,`vname`,`sale`,`ts`, `totalPrice`) values(?,?,?,?,?,?,?,?)";

    public boolean rawInsert() {

        long id = dp.insert(false, insertSQL, this.cid, this.pid, this.vid, this.pname, this.vname, this.sale, this.ts, this.totalPrice);

        // log.info("[Insert Item Id:]" + id);

        if (id > 0L) {
            setId(id);
            return true;
        } else {
            log.error("Insert Fails.....");
            return false;
        }

    }

    @Transient
    static String updateSQL = "update `item_prop_sale` set  `cid` = ?, `pid` = ?, `vid` = ?, `pname` = ?, `vname` = ?, `sale` = ?, `ts` = ?, `totalPrice` = ? where `id` = ? ";

    public boolean rawUpdate() {
        long updateNum = dp.insert(false, updateSQL, this.cid, this.pid, this.vid, this.pname, this.vname, this.sale,
                this.ts, this.totalPrice, this.getId());

        if (updateNum == 1) {
            log.info("update ok for :" + this.getId());
            return true;
        } else {
            log.error("update failed...for :" + this.getId());
            return false;
        }
    }
    
    public static List<ItemPropSale> findCidPropGroup(long cid) {

        String query = "select id,cid,pid,vid,pname,vname,sum(sale) as saleAll,max(ts),totalPrice, from " + TABLE_NAME + " where cid = ? group by pid order by saleAll desc ";
        
        return new JDBCBuilder.JDBCExecutor<List<ItemPropSale>>(dp, query, cid) {

            @Override
            public List<ItemPropSale> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPropSale> catList = new ArrayList<ItemPropSale>();
                while (rs.next()) {
                    ItemPropSale catPlay = new ItemPropSale(rs);
                    if (catPlay != null) {
                        catList.add(catPlay);
                    }
                }
                return catList;
            }

        }.call();
    }

    public static List<ItemPropSale> findCidTopProp(long cid, long pid, PageOffset po) {

        String query = "select id,cid,pid,vid,pname,vname,sale,ts,totalPrice from " + TABLE_NAME + " where 1=1 ";
        if (cid > 0) {
            query += " and cid = " + cid;
        }
        if (pid > 0) {
            query += " and pid = " + pid;
        }

        query += " order by sale desc limit ? offset ?";

        return new JDBCBuilder.JDBCExecutor<List<ItemPropSale>>(dp, query, po.getPs(), po.getOffset()) {

            @Override
            public List<ItemPropSale> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPropSale> catList = new ArrayList<ItemPropSale>();
                while (rs.next()) {
                    ItemPropSale catPlay = new ItemPropSale(rs);
                    if (catPlay != null) {
                        catList.add(catPlay);
                    }
                }
                return catList;
            }

        }.call();
    }

    public static long countCidProp(long cid, long pid) {
        String query = "select count(*) from " + TABLE_NAME + " where 1=1 ";
        if (cid > 0) {
            query += " and cid = " + cid;
        }
        if (pid > 0) {
            query += " and pid = " + pid;
        }
        return dp.singleLongQuery(query);
    }

    public static List<ItemPropSale> findEachProp(PageOffset po) {

        String query = "select id,cid,pid,vid,pname,vname,sale,ts,totalPrice from " + TABLE_NAME
                + " order by sale desc limit ? offset ?";

        return new JDBCBuilder.JDBCExecutor<List<ItemPropSale>>(dp, query, po.getPs(), po.getOffset()) {

            @Override
            public List<ItemPropSale> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPropSale> catList = new ArrayList<ItemPropSale>();
                while (rs.next()) {
                    ItemPropSale catPlay = new ItemPropSale(rs);
                    if (catPlay != null) {
                        catList.add(catPlay);
                    }
                }
                return catList;
            }

        }.call();
    }

    public static long countPropAll() {
        String query = "select count(*) from " + TABLE_NAME;
        return dp.singleLongQuery(query);
    }

    @Transient
    static String updateSaleSQL = "update `item_prop_sale` set sale = sale + ? where cid = ? and pid = ? and vid = ?";

    public static boolean addCidPropSale(long cid, long pid, long vid, long sale) {
        long updateNum = dp.update(updateSaleSQL, sale, cid, pid, vid);
        if (updateNum > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Transient
    static String updateSaleByIdSQL = "update `item_prop_sale` set sale = sale + ? where id = ?";

    public static boolean addCidPropSaleById(long id, long sale) {
        long updateNum = dp.update(updateSaleByIdSQL, sale, id);
        if (updateNum > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean createOrAddCidPropSale(long cid, long pid, long vid, String pname, String vname, long sale, long totalPrice) {
        try {
            long id = findExistId(cid, pid, vid);
            if (id > 0) {
                return addCidPropSaleById(cid, id);
            } else {
                return new ItemPropSale(cid, pid, vid, pname, vname, sale, totalPrice).rawInsert();
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }
    }

    public static void truncateTable() {
        dp.update("truncate table " + TABLE_NAME);
    }

}
