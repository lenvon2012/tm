
package models.shop;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.taobao.api.domain.ShopCat;

@Entity(name = ShopCatPlay.TABLE_NAME)
public class ShopCatPlay extends GenericModel implements PolicySQLGenerator {

    public static final String TABLE_NAME = "shop_cat";

    public static final Logger log = LoggerFactory.getLogger(ShopCatPlay.class);

    public static ShopCatPlay EMPTY = new ShopCatPlay();

    public ShopCatPlay() {
    }

    @Id
    public Long cid;

    public Long parentCid;

    public String name;

    public boolean isParent;

    public ShopCatPlay(ShopCat shopCat) {
        this.cid = shopCat.getCid();
        this.parentCid = shopCat.getParentCid();
        this.name = shopCat.getName();
        this.isParent = shopCat.getIsParent();
    }

    @Override
    public String getTableName() {
        return this.TABLE_NAME;
    }

    @Override
    public String getIdColumn() {
        return "cid";
    }

    @Override
    public void setId(Long id) {
        this.cid = id;
    }

    @Override
    public String getIdName() {
        return "cid";
    }

    @Override
    public Long getId() {
        return cid;
    }

    static String EXIST_ID_QUERY = "select cid from " + TABLE_NAME + " where cid = ? ";

    public static long findExistId(Long cid) {
        return JDBCBuilder.singleLongQuery(EXIST_ID_QUERY, cid);
    }

    @Override
    public boolean jdbcSave() {

        try {
            long existdId = findExistId(this.cid);

            if (existdId == 0L) {
                return this.rawInsert();
            } else {
                return false;
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }

    }

    static String insertSQL = "insert into `shop_cat`(`cid`,`parentCid`,`name`,`isParent`) values(?,?,?,?)";

    public boolean rawInsert() {

        long id = JDBCBuilder.insert(false, insertSQL, this.cid, this.parentCid, this.name, this.isParent);

//        log.info("[Insert Item Id:]" + id);

        if (id > 0L) {
            setId(id);
            return true;
        } else {
            log.error("Insert Fails.....");
            return false;
        }

    }

    public Long getCid() {
        return cid;
    }

    public void setCid(Long cid) {
        this.cid = cid;
    }

    public Long getParentCid() {
        return parentCid;
    }

    public void setParentCid(Long parentCid) {
        this.parentCid = parentCid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isParent() {
        return isParent;
    }

    public void setParent(boolean isParent) {
        this.isParent = isParent;
    }

    @Override
    public String toString() {
        return "ShopCatPlay [cid=" + cid + ", parentCid=" + parentCid + ", name=" + name + ", isParent=" + isParent
                + "]";
    }

    @Override
    public String getTableHashKey() {
        // TODO Auto-generated method stub
        return null;
    }

    public static ShopCatPlay findByUserCid(Long userCid) {
    	return singleQuery("cid = ?",userCid);
    }
    
    public static String SHOP_CAT_PLAY_QUERY = "select cid,parentCid,name,isParent from " + TABLE_NAME
            + " where ";
    public static ShopCatPlay singleQuery(String query, Object...params) {
		return new JDBCExecutor<ShopCatPlay>(SHOP_CAT_PLAY_QUERY+query, params) {

            @Override
            public ShopCatPlay doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                	ShopCatPlay shopcat = new ShopCatPlay();
                	shopcat.setCid(rs.getLong(1));
                	shopcat.setParentCid(rs.getLong(2));
                	shopcat.setName(rs.getString(3));
                	shopcat.setParent(rs.getBoolean(4));
                	return shopcat;
                } else {
                	return null;
                }
            }
        }.call();
	}
}
