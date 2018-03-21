package models.jd;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.DBBuilder;
import transaction.DBBuilder.DataSrc;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.jd.open.api.sdk.domain.ware.Ware;

@Entity(name = JDItemPlay.TABLE_NAME)
@JsonIgnoreProperties(value = {
        "entityId", "tableHashKey", "persistent", "tableName",
        "idName", "idColumn", "persistent", "entityId"
})
public class JDItemPlay extends GenericModel implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(JDItemPlay.class);

    public static final String TABLE_NAME = "jd_item_";

    private static JDItemPlay EMPTY = new JDItemPlay();

    public static final DBDispatcher dp = new DBDispatcher(DataSrc.QUOTA, EMPTY);

    @Id
    @JsonProperty(value = "numIid")
    @PolicySQLGenerator.CodeNoUpdate
    public Long numIid;

    @Index(name = "uid")
    public Long uid;

    @Index(name = "user_id")
    @PolicySQLGenerator.CodeNoUpdate
    public Long userId;

    public Long shopId;

    @Index(name = "ts")
    public Long ts;

    public Long status;

    public Long price;

    public Long stock_num;

    public Long skuId;

    public String title;

    @Transient
    public String detailURL;

    public String picURL;

    public JDItemPlay() {
        // TODO Auto-generated constructor stub
    }

    public JDItemPlay(Long uid, Ware ware) {
        this.uid = uid;
        this.numIid = ware.getWareId();
        this.userId = ware.getVenderId();
        this.shopId = ware.getShopId();

        this.price = (long) (Double.valueOf(ware.getJdPrice()) * 100);
        this.stock_num = ware.getStockNum();
        this.title = ware.getTitle();
        this.picURL = ware.getLogo();
        this.status = 0L;
    }

    public JDItemPlay(ResultSet rs) throws SQLException {
        this.numIid = rs.getLong(1);
        this.userId = rs.getLong(2);
        this.shopId = rs.getLong(3);
        this.ts = rs.getLong(4);
        this.status = rs.getLong(5);
        this.price = rs.getLong(6);
        this.stock_num = rs.getLong(7);
        this.title = rs.getString(8);
        this.picURL = rs.getString(9);
        this.uid = rs.getLong(10);
        this.skuId = rs.getLong(11);
    }

    public Long getNumIid() {
        return numIid;
    }

    public void setNumIid(Long numIid) {
        this.numIid = numIid;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    public void setPopularized(int popularized) {
        this.status = this.status | (popularized & 3);
    }

    public boolean isPopularized() {
        return (this.status & 3) > 0;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetailURL() {
        return detailURL;
    }

    public void setDetailURL(String detailURL) {
        this.detailURL = detailURL;
    }

    public String getPicURL() {
        return picURL;
    }

    public void setPicURL(String picURL) {
        this.picURL = picURL;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public Long getStock_num() {
        return stock_num;
    }

    public void setStock_num(Long stock_num) {
        this.stock_num = stock_num;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getTableHashKey() {
        return null;
    }

    @Override
    public String getIdColumn() {
        return "numIid";
    }

    @Override
    public void setId(Long id) {
        this.numIid = id;
    }

    @Override
    public Long getId() {
        // TODO Auto-generated method stub
        return numIid;
    }

    @Override
    public String toString() {
        return "JDItemPlay [numIid=" + numIid + ", userId=" + userId + ", shopId=" + shopId + ", ts=" + ts
                + ", status=" + status + ", price=" + price + ", stock_num=" + stock_num + ", title=" + title
                + ", detailURL=" + detailURL + ", picURL=" + picURL + "]";
    }

    public static String genShardQuery(String query, Long sellerId) {
        return genShardQuery(query, String.valueOf(DBBuilder.genUserIdHashKey(sellerId)));
    }

    private static String genShardQuery(String query, String key) {
        query = query.replaceAll("%s", "~~");
        query = query.replaceAll("%", "##");
        query = query.replaceAll("~~", "%s");

        String formQuery = String.format(query, key);
        return formQuery.replaceAll("##", "%");
    }

    @Transient
    private static String EXIST_ID_QUERY = "select numIid from jd_item_%s where numIid = ? ";

    public static long findExistId(long uid, long numIid) {

        String sql = genShardQuery(EXIST_ID_QUERY, uid);

        return dp.singleLongQuery(sql, numIid);

    }

    public boolean rawInsert() {
        long id = dp
                .insert(genShardQuery(
                        "insert into `jd_item_%s`(`numIid`,`userId`,`shopId`,`ts`,`status`,`price`,`stock_num`,`title`,`picURL`,`uid`,`skuId`) values(?,?,?,?,?,?,?,?,?,?,?)",
                        uid), this.numIid, this.userId, this.shopId, this.ts, this.status, this.price, this.stock_num,
                        this.title, this.picURL, this.uid, this.skuId);

        if (id > 0L) {
            return true;
        } else {
            log.error("insert crmMember error, buyerNick: ");
            return false;
        }

    }

    public boolean rawUpdate() {

        long updateNum = dp
                .insert(genShardQuery(
                        "update `jd_item_%s` set  `shopId` = ?, `ts` = ?, `price` = ?, `stock_num` = ?, `title` = ?, `picURL` = ?, `uid` = ?, `skuId` = ? where `numIid` = ? ",
                        uid), this.shopId, this.ts, this.price, this.stock_num, this.title, this.picURL,
                        this.uid, this.skuId, this.getId());

        if (updateNum > 0L) {
            return true;
        } else {
            log.error("update crmMember error, buyerNick: ");
            return false;
        }

    }

    @Override
    public boolean jdbcSave() {
        try {
            long existdId = findExistId(this.uid, this.numIid);

            if (existdId <= 0L) {
                return rawInsert();
            } else {
                return rawUpdate();
            }

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return false;
        }
    }

    @Override
    public String getIdName() {

        return "numIid";
    }

}
