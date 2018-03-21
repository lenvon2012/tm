package models.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import utils.DateUtil;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = SellerDSR.TABLE_NAME)
@JsonIgnoreProperties(value = {
        "entityId", "tableHashKey", "persistent", "tableName", "idName", "idColumn"
})
public class SellerDSR extends Model implements PolicySQLGenerator {

    @Transient
    private static final Logger log = LoggerFactory.getLogger(SellerDSR.class);

    @Transient
    public static final String TABLE_NAME = "SellerDSR";

    @Transient
    public static SellerDSR EMPTY = new SellerDSR();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

    @Index(name = "userId")
    public Long userId;

    public double goodRate;

    @Index(name = "ts")
    public Long ts;

    public SellerDSR() {

    }

    public SellerDSR(Long userId, double goodRate) {
        this.userId = userId;
        this.goodRate = goodRate;
        this.ts = DateUtil.formCurrDate();
    }

    public SellerDSR(Long id, Long userId, double goodRate, Long ts) {
        super();
        this.id = id;
        this.userId = userId;
        this.goodRate = goodRate;
        this.ts = ts;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public double getGoodRate() {
        return goodRate;
    }

    public void setGoodRate(double goodRate) {
        this.goodRate = goodRate;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    @Override
    public String toString() {
        return "SellerDSR [userId=" + userId + ", goodRate=" + goodRate + ", ts=" + ts + "]";
    }

    @Override
    public String getTableName() {
        // TODO Auto-generated method stub
        return TABLE_NAME;
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
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getIdName() {
        return "id";
    }

    @Transient
    static String EXIST_ID_QUERY = "select userId from " + SellerDSR.TABLE_NAME + " where userId = ? and ts = ? ";

    public static long findExistId(Long userId, Long ts) {
        return dp.singleLongQuery(EXIST_ID_QUERY, userId, ts);
    }

    public boolean jdbcSave() {
        try {
            long existdId = findExistId(this.userId, this.ts);
            if (existdId > 0L) {
                return this.rawUpdate();
            } else {
                return this.rawInsert();
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }
    }

    @Transient
    static String insertSQL = "insert into `SellerDSR`(`userId`,`goodRate`, `ts`) values(?,?,?)";

    public boolean rawInsert() {

        long id = dp.insert(false, insertSQL, this.userId, this.goodRate, this.ts);

        if (id >= 0L) {
            return true;
        } else {
            log.error("Insert SellerDSR Fails....." + this);
            return false;
        }
    }

    public boolean rawUpdate() {
        long updateNum = dp.insert("update `SellerDSR` set  `goodRate` = ? where `userId` = ? and `ts` = ? ",
                this.goodRate, this.userId, this.ts);

        if (updateNum >= 0) {
            return true;
        } else {
            log.error("Update SellerDSR Fails....." + this);
            return false;
        }

    }

    public static SellerDSR findByUserId(Long userId, Long ts) {

        String query = "select id,userId,goodRate,ts from " + TABLE_NAME + " where userId = ? and ts = ? ";

        return new JDBCBuilder.JDBCExecutor<SellerDSR>(dp, query, userId, ts) {

            @Override
            public SellerDSR doWithResultSet(ResultSet rs) throws SQLException {

                if (rs.next()) {
                    return new SellerDSR(rs.getLong(1), rs.getLong(2), rs.getDouble(3), rs.getLong(4));
                } else {
                    return null;
                }
            }

        }.call();
    }

    public static List<SellerDSR> findSellerDSR(Long userId, Long start, Long end) {

        String query = "select id,userId,goodRate,ts from " + TABLE_NAME + " where userId = ? ";

        if (start != null && start.longValue() > 0) {
            query += " and ts >= " + start;
        }
        if (end != null && end.longValue() > 0) {
            query += " and ts <= " + end;
        }
        query += " order by ts asc";

        return new JDBCBuilder.JDBCExecutor<List<SellerDSR>>(dp, query, userId) {

            @Override
            public List<SellerDSR> doWithResultSet(ResultSet rs) throws SQLException {
                List<SellerDSR> list = new ArrayList<SellerDSR>();
                while (rs.next()) {
                    SellerDSR sellerDSR = new SellerDSR(rs.getLong(1), rs.getLong(2), rs.getDouble(3), rs.getLong(4));
                    list.add(sellerDSR);
                }
                return list;
            }

        }.call();
    }

}
