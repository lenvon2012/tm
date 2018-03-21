package underup.frame.industry;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;

import models.item.ItemPropSale;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.ItemThumb;
import com.taobao.api.domain.Item;

import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import play.Play;
import play.db.jpa.GenericModel;
import transaction.JDBCBuilder;
import transaction.DBBuilder.DataSrc;

public class ItemProps extends GenericModel implements PolicySQLGenerator {
    public static final Logger log = LoggerFactory.getLogger(ItemProps.class);


    public static ItemProps EMPTY = new ItemProps();

    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

    public ItemProps() {
        totalPrice = 0L;
        sale = 0L;
    }

    private long id;

    private long cid;

    private long pid;

    private long vid;

    private String pname;

    private String vname;

    private long sale;

    private long year;

    private long month;

    private long totalPrice;

    public long getCid() {
        return this.cid;
    }

    public void setCid(Long cid) {
        this.cid = cid;
    }

    public long getVid() {
        return vid;
    }

    public void setVid(long vid) {
        this.vid = vid;
    }

    public long getPid() {
        return this.pid;
    }

    public void setPid(long pid) {
        this.pid = pid;
    }

    public String getPName() {
        return this.pname;
    }

    public void setPName(String pname) {
        this.pname = pname;
    }

    public String getVname() {
        return this.vname;
    }

    public void setVname(String vname) {
        this.vname = vname;
    }

    public long getSale() {
        return this.sale;
    }

    public void setSale(long sale) {
        this.sale = sale;
    }

    public long getYear() {
        return this.year;
    }

    public void setYear(long year) {
        this.year = year;
    }

    public long getMonth() {
        return this.month;
    }

    public void setMonth(long month) {
        this.month = month;
    }

    public long getTotalPrice() {
        return this.totalPrice;
    }

    public void setTotalPrice(long totalPrice) {
        this.totalPrice = totalPrice;
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
        return "Id";
    }

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;

    }

    public static String getTableName(long year, long month) {
        Date date = new Date(year + month);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        String y = sdf.format(date);
        sdf = new SimpleDateFormat("MM");
        String m = sdf.format(date);
        return "item_props_" + y + "_" + m;
    }
    
    @Override
    public boolean jdbcSave() {

        try {
            long existdId = findExistId(this.cid, this.pid, this.vid);

            if (existdId <= 0L) {
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
    
    public static String getCidPropExistSQL(){
        long[] ym = CatTopSaleItemSQL.getTime();
        String findCidPropExistSQL = "select id from " + getTableName(ym[0], ym[1]) + " where cid = ? and pid = ? and vid = ?";
        return findCidPropExistSQL;
    }
    public static long findExistId(long cid, long pid, long vid) {
        String findCidPropExistSQL = getCidPropExistSQL();
        return dp.singleLongQuery(findCidPropExistSQL, cid, pid, vid);
    }

    public static String getInsertSQL(){
        long[] ym = CatTopSaleItemSQL.getTime();
        String insertSQL = "insert into "+ getTableName(ym[0], ym[1]) + "(`cid`,`pid`,`vid`,`pname`,`vname`,`sale`,`year`, `month`, `totalPrice`) values(?,?,?,?,?,?,?,?,?)";
        return insertSQL;
    }
    
    public boolean rawInsert() {
        String insertSQL = getInsertSQL();
        long id = dp.insert(false, insertSQL, this.cid, this.pid, this.vid, this.pname, this.vname, this.sale,
                this.year, this.month, this.totalPrice);
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
        String updateSQL = "update "+getTableName(ym[0], ym[1])+" set  `cid` = ?, `pid` = ?, `vid` = ?, `pname` = ?, `vname` = ?, `sale` = ?, `year` = ?, " +
    		"`month` = ?, `totalPrice` = ? where `id` = ? ";
        return updateSQL;
    }

    public boolean rawUpdate() {
        String updateSQL = getUpdateSQL();
        long updateNum = dp.insert(false, updateSQL, this.cid, this.pid, this.vid, this.pname, this.vname, this.sale,
                this.year, this.month, this.totalPrice, this.getId());

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

    public ItemProps(long cid, long pid, long vid, String pname, String vname, long sale, long year, long month,
            long totalPrice) {
        this.cid = cid;
        this.pid = pid;
        this.vid = vid;
        this.pname = pname;
        this.vname = vname;
        this.sale = sale;
        this.year = year;
        this.month = month;
        this.totalPrice = totalPrice;
    }

    public ItemProps(ResultSet rs) throws SQLException {
        this.cid = rs.getLong(1);
        this.pid = rs.getLong(2);
        this.vid = rs.getLong(3);
        this.pname = rs.getString(4);
        this.vname = rs.getString(5);
        this.sale = rs.getLong(6);
        this.year = rs.getLong(7);
        this.month = rs.getLong(8);
        this.totalPrice = rs.getLong(9);
        this.id = rs.getLong(10);
    }

    public static List<ItemProps> getItemProps(List<Long> cids, long year, long month) {
        if (CommonUtils.isEmpty(cids))
            return new ArrayList<ItemProps>();
        String tableName = getTableName(year, month);
        long cid = cids.get(0);
        String query = "(select cid, pid, vid, pname, vname, sale, year, month, totalPrice, id from " + tableName + " where cid = "
                + cid + ") ";
        for (int i = 1; i < cids.size(); ++i) {
            cid = cids.get(i);
            query += "union all (select cid, pid, vid, pname, vname, sale, year, month, totalPrice, id from "+ tableName +" where cid = "
                    + cid + ")";
        }
        return new JDBCBuilder.JDBCExecutor<List<ItemProps>>(dp, query) {

            @Override
            public List<ItemProps> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemProps> catList = new ArrayList<ItemProps>();
                while (rs.next()) {
                    ItemProps catPlay = new ItemProps(rs);
                    if (catPlay != null) {
                        catList.add(catPlay);
                    }
                }
                return catList;
            }

        }.call();
    }

    public static List<Long> getBackcid(long year, long month) {
        String tableName = getTableName(year, month);
        String query = "select cid from " + tableName + " group by cid";
        return new JDBCBuilder.JDBCExecutor<List<Long>>(dp, query) {
            @Override
            public List<Long> doWithResultSet(ResultSet rs) throws SQLException {
                List<Long> catList = new ArrayList<Long>();
                while (rs.next()) {
                    long cid = rs.getLong(1);
                    catList.add(cid);
                }
                return catList;
            }

        }.call();
    }

    public static List<Long> getBackcid1() {
        //String tableName = getTableName(year, month);
        String query = "select cid from item_props group by cid";
        return new JDBCBuilder.JDBCExecutor<List<Long>>(dp, query) {
            @Override
            public List<Long> doWithResultSet(ResultSet rs) throws SQLException {
                List<Long> catList = new ArrayList<Long>();
                while (rs.next()) {
                    long cid = rs.getLong(1);
                    catList.add(cid);
                }
                return catList;
            }

        }.call();
    }

    public static void insertPatch(Map<String, ItemProps> itemPropsMap) {
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
            long[] ym = CatTopSaleItemSQL.getTime();
            String tableName = getTableName(ym[0], ym[1]);
            conn = DriverManager.getConnection(url, user, pwd);
            ResultSet rs = null;
            conn.setAutoCommit(false);
            String insertSQL = getInsertSQL();
            String updateSQL = getUpdateSQL();
            PreparedStatement prest = conn.prepareStatement(insertSQL);
            List<String> itemProps = new ArrayList(itemPropsMap.keySet());
            PreparedStatement up = conn
                    .prepareStatement("select id from " + tableName +" where cid=? and pname = ? and vname = ?");
            PreparedStatement updatePatch = conn.prepareStatement(updateSQL);
            int insert = 0, update = 0;
            if (itemProps != null) {
                for (String itemProp : itemProps) {
                    ItemProps item = itemPropsMap.get(itemProp);

                    up.setLong(1, item.getCid());
                    up.setString(2, item.getPName());
                    up.setString(3, item.getVname());
                    rs = up.executeQuery();
                    long flag = 0L;
                    if (rs.next()) {
                        flag = rs.getLong(1);
                    }
                    if (flag == 0L) {
                        prest.setLong(1, item.getCid());
                        prest.setLong(2, item.getPid());
                        prest.setLong(3, item.getVid());
                        prest.setString(4, item.getPName());
                        prest.setString(5, item.getVname());
                        prest.setLong(6, item.getSale());
                        prest.setLong(7, item.getYear());
                        prest.setLong(8, item.getMonth());
                        prest.setLong(9, item.getTotalPrice());
                        prest.addBatch();
                        insert++;
                    }else{
                        updatePatch.setLong(1, item.getCid());
                        updatePatch.setLong(2, item.getPid());
                        updatePatch.setLong(3, item.getVid());
                        updatePatch.setString(4, item.getPName());
                        updatePatch.setString(5, item.getVname());
                        updatePatch.setLong(6, item.getSale());
                        updatePatch.setLong(7, item.getYear());
                        updatePatch.setLong(8, item.getMonth());
                        updatePatch.setLong(9, item.getTotalPrice());
                        updatePatch.setLong(10, flag);
                        updatePatch.addBatch();
                        update++;
                    }
                }
                log.info("---------------------------ItemProps:the insert number is " + insert + " and the update num is " + update);
                if(insert > 0)
                    prest.executeBatch();
                if(update > 0)
                    prest.executeBatch();
                conn.commit();
                conn.close();
            }
        } catch (SQLException e) {
            log.error("connect to database fial........");
        }

    }

}
