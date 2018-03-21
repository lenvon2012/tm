
package models.user;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.JDBCBuilder;

@Entity(name = SellerMobile.TABLE_NAME)
public class SellerMobile extends GenericModel {

    @Transient
    private static final Logger log = LoggerFactory.getLogger(SellerMobile.class);

    @Transient
    public static final String TABLE_NAME = "SellerMobile";

    @Transient
    public static SellerMobile EMPTY = new SellerMobile();

    @Id
    public Long userId;

    public String userNick;

    public String mobile;

    public SellerMobile() {
    }

    public SellerMobile(Long userId, String userNick, String mobile) {
        this.userId = userId;
        this.userNick = userNick;
        this.mobile = mobile;
    }

    /*public static SellerMobile findByUserId(Long userId) {
        return SellerMobile.find("userId = ? ", userId).first();
    }*/

    public static SellerMobile findByUserId(Long userId) {

        String query = "select userId,userNick,mobile from " + TABLE_NAME
                + " where userId = ? ";

        return new JDBCBuilder.JDBCExecutor<SellerMobile>(query, userId) {

            @Override
            public SellerMobile doWithResultSet(ResultSet rs)
                    throws SQLException {

                if (rs.next()) {
                    return new SellerMobile(rs.getLong(1),rs.getString(2),rs.getString(3));
                } else {
                    return null;
                }
            }

        }.call();
    }
    
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    @Transient
    static String EXIST_ID_QUERY = "select userId from " + SellerMobile.TABLE_NAME
            + " where userId = ? ";

    public static long findExistId(Long userId) {
        return JDBCBuilder.singleLongQuery(EXIST_ID_QUERY, userId);
    }

    public boolean jdbcSave() {

        try {
            long existdId = findExistId(this.userId);
            if (existdId > 0L) {
                return false;
            } else {
                return this.rawInsert();
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }
    }

    @Transient
    static String insertSQL = "insert into `SellerMobile`(`userId`,`userNick`, `mobile`) values(?,?,?)";

    public boolean rawInsert() {

        long id = JDBCBuilder.insert(false, insertSQL, this.userId, this.userNick, this.mobile);

        log.info("[Insert SellerMobile Id:]" + id);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert SellerMobile Fails.....");
            return false;
        }
    }

    @Override
    public String toString() {
        return "SellerMobile [userId=" + userId + ", userNick=" + userNick + ", mobile=" + mobile
                + "]";
    }

    public static void ensure(final User user) {
        if (user == null) {
            return;
        }
        // Titles.getPool().submit(new Callable<DiagResult>() {
        // @Override
        // public DiagResult call() throws Exception {
        // long existed = findExistId(user.getId());
        // if (existed > 0L) {
        // return null;
        // }
        //
        // String sellerMobile = TradeApi.getSellerMobile(user.getSessionKey());
        // new SellerMobile(user.getId(), user.getUserNick(),
        // sellerMobile).jdbcSave();
        // return null;
        // }
        // });
    }
}
