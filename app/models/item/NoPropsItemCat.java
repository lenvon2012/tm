
package models.item;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;

@Entity(name = NoPropsItemCat.TABLE_NAME)
public class NoPropsItemCat extends GenericModel {

    @Transient
    public static final DataSrc src = DataSrc.BASIC;

    @Transient
    public static final String TABLE_NAME = "no_props_item_cat";

    @Transient
    public static final Logger log = LoggerFactory.getLogger(ItemCatPlay.class);

    /*
     ** 还是使用JDBC
     */

    @Id
    private Long cid;

    private String catName;

    public Long getCid() {
        return cid;
    }

    public void setCid(Long cid) {
        this.cid = cid;
    }

    public String getCatName() {
        return catName;
    }

    public void setCatName(String catName) {
        this.catName = catName;
    }

    public NoPropsItemCat(Long cid, String catName) {
        this.cid = cid;
        this.catName = catName;
    }

    @Transient
    static String EXIST_ID_QUERY = "select cid from " + NoPropsItemCat.TABLE_NAME + " where cid = ? ";

    public static long findExistId(Long cid) {
        return JDBCBuilder.singleLongQuery(EXIST_ID_QUERY, cid);
    }

    public boolean jdbcSave() {

        try {
            long existdId = findExistId(this.cid);

            if (existdId == 0L) {
                return this.rawInsert();
            } else {
                setCid(existdId);
                return this.rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }

    }

    @Transient
    static String insertSQL = "insert into `no_props_item_cat`(`cid`,`catName`) values(?,?)";

    public boolean rawInsert() {

        long id = JDBCBuilder.insert(false, insertSQL, this.cid, this.catName);

//        log.info("[Insert Item Id:]" + id);

        if (id > 0L) {
            setCid(id);
            return true;
        } else {
            log.error("Insert Fails.....");
            return false;
        }

    }

    @Transient
    static String updateSQL = "update `no_props_item_cat` set `catName` = ? where `cid` = ? ";

    public boolean rawUpdate() {
        long updateNum = JDBCBuilder.insert(false, updateSQL, this.catName, this.cid);

        if (updateNum == 1) {
            log.info("update ok for :" + this.getCid());
            return true;
        } else {
            log.error("update failed...for :" + this.getCid());
            return false;
        }
    }

    @Transient
    static String fetchCidsSQL = "select cid from `no_props_item_cat`";

    public static List<Long> fetchCids() {
        return new JDBCBuilder.JDBCExecutor<List<Long>>(fetchCidsSQL) {

            @Override
            public List<Long> doWithResultSet(ResultSet rs)
                    throws SQLException {
                List<Long> list = new ArrayList<Long>();
                while (rs.next()) {
                    list.add(rs.getLong(1));
                }
                return list;
            }

        }.call();
    }

    @Transient
    static String fetchCidsMapSQL = "select cid,catName from `no_props_item_cat`";

    public static Map<Long, String> fetchCidsMap() {
        return new JDBCBuilder.JDBCExecutor<Map<Long, String>>(fetchCidsMapSQL) {

            @Override
            public Map<Long, String> doWithResultSet(ResultSet rs)
                    throws SQLException {
                HashMap<Long, String> map = new HashMap<Long, String>();
                while (rs.next()) {
                    map.put(rs.getLong(1), rs.getString(2));
                }
                return map;
            }

        }.call();
    }
}
