/**
 * 
 */
package models.fenxiao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

/**
 * @author navins
 * @date: Dec 2, 2013 11:15:55 AM
 */
@Entity(name = AutoCatRule.TABLE_NAME)
@JsonIgnoreProperties(value = { "dataSrc", "persistent", "entityId", "entityId", "tableHashKey", "persistent",
        "tableName", "idName", "idColumn" })
public class AutoCatRule extends Model implements PolicySQLGenerator {

    @Transient
    private static final Logger log = LoggerFactory.getLogger(AutoCatRule.class);

    @Transient
    public static final String TABLE_NAME = "autocat_rule";

    @Transient
    public static AutoCatRule EMPTY = new AutoCatRule();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

    @Index(name = "user_id")
    private Long userId;

    @Index(name = "cid")
    private Long catId;

    private String words;

    private String brand;

    private String attr;

    private String distributor;

    private String supplier;

    private Long created;

    private String reserved;

    public AutoCatRule() {

    }

    public AutoCatRule(Long userId, Long catId, String words) {
        super();
        this.userId = userId;
        this.catId = catId;
        this.words = words;
    }

    public AutoCatRule(ResultSet rs) throws SQLException {
        this.id = rs.getLong(1);
        this.userId = rs.getLong(2);
        this.catId = rs.getLong(3);
        this.words = rs.getString(4);
        this.brand = rs.getString(5);
        this.attr = rs.getString(6);
        this.distributor = rs.getString(7);
        this.supplier = rs.getString(8);
        this.created = rs.getLong(9);
        this.reserved = rs.getString(10);
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCatId() {
        return catId;
    }

    public void setCatId(Long catId) {
        this.catId = catId;
    }

    public String getWords() {
        return words;
    }

    public void setWords(String words) {
        this.words = words;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getAttr() {
        return attr;
    }

    public void setAttr(String attr) {
        this.attr = attr;
    }

    public String getDistributor() {
        return distributor;
    }

    public void setDistributor(String distributor) {
        this.distributor = distributor;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public String getReserved() {
        return reserved;
    }

    public void setReserved(String reserved) {
        this.reserved = reserved;
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
        return null;
    }

    @Override
    public void setId(Long id) {
        // TODO Auto-generated method stub
        this.id = id;
    }

    @Override
    public String toString() {
        return "AutoCatRule [userId=" + userId + ", catId=" + catId + ", words=" + words + ", brand=" + brand
                + ", attr=" + attr + ", distributor=" + distributor + ", supplier=" + supplier + ", created=" + created
                + ", reserved=" + reserved + "]";
    }

    @Transient
    static String EXIST_ID_QUERY = "select id from " + TABLE_NAME + " where userId = ? and catId = ? ";

    public static long findExistId(Long userId, Long catId) {
        return dp.singleLongQuery(EXIST_ID_QUERY, userId, catId);
    }

    @Override
    public boolean jdbcSave() {

        try {
            long existdId = findExistId(this.userId, this.catId);

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

    @Override
    public String getIdName() {
        // TODO Auto-generated method stub
        return "id";
    }

    public boolean rawInsert() {

        long id = dp
                .insert("insert into `autocat_rule`(`userId`,`catId`,`words`,`brand`,`attr`,`distributor`, `supplier`,`created`,`reserved`) values(?,?,?,?,?,?,?,?,?)",
                        this.userId, this.catId, this.words, this.brand, this.attr, this.distributor, this.supplier,
                        this.created, this.reserved);

        // log.info("[Insert Item Id:]" + id);

        if (id > 0L) {
            setId(id);
            return true;
        } else {
            log.error("Insert Fails.....");
            return false;
        }

    }

    public boolean rawUpdate() {
        long updateNum = dp
                .insert("update `autocat_rule` set `userId` = ?, `catId` = ?, `words` = ?, `brand` = ?, `attr` = ?, `distributor` = ?, `supplier` = ?, `created` = ?, `reserved` = ? where `id` = ? ",
                        this.userId, this.catId, this.words, this.brand, this.attr, this.distributor, this.supplier,
                        this.created, this.reserved, this.getId());

        if (updateNum == 1) {
            log.info("update ok for :" + this.getId());
            return true;
        } else {
            log.error("update failed...for :" + this.getId());
            return false;
        }
    }

}
