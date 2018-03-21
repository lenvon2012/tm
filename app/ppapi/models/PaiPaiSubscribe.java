/**
 * 
 */

package ppapi.models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

/**
 * @author navins
 * @date 2013-7-9 下午12:14:17
 */
@Entity(name = PaiPaiSubscribe.TABLE_NAME)
public class PaiPaiSubscribe extends Model implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(PaiPaiSubscribe.class);

    public static final String TAG = "PaiPaiSubscribe";

    public static final String TABLE_NAME = "paipai_subscribe";

    public static PaiPaiSubscribe _instance = new PaiPaiSubscribe();

    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, _instance);

    @Index(name = "sellerUin")
    private Long sellerUin;

    private int chargeItemId;

    private Long deadLine;

    public PaiPaiSubscribe() {

    }

    public PaiPaiSubscribe(Long sellerUin, int chargeItemId, Long deadLine) {
        super();
        this.sellerUin = sellerUin;
        this.chargeItemId = chargeItemId;
        this.deadLine = deadLine;
    }

    public PaiPaiSubscribe(ResultSet rs) throws SQLException {
        this.sellerUin = rs.getLong(1);
        this.chargeItemId = rs.getInt(2);
        this.deadLine = rs.getLong(3);
    }

    public Long getSellerUin() {
        return sellerUin;
    }

    public void setSellerUin(Long sellerUin) {
        this.sellerUin = sellerUin;
    }

    public int getChargeItemId() {
        return chargeItemId;
    }

    public void setChargeItemId(int chargeItemId) {
        this.chargeItemId = chargeItemId;
    }

    public Long getDeadLine() {
        return deadLine;
    }

    public void setDeadLine(Long deadLine) {
        this.deadLine = deadLine;
    }

    @Override
    public String toString() {
        return "PaiPaiSubscribe [sellerUin=" + sellerUin + ", chargeItemId=" + chargeItemId + ", deadLine=" + deadLine
                + "]";
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
    public boolean jdbcSave() {
        return this.rawInsert();
    }

    @Override
    public String getIdName() {
        // TODO Auto-generated method stub
        return "id";
    }

    public static List<PaiPaiSubscribe> findUserSubscribes(Long sellerUin) {
        return new ListFetcher("sellerUin = ?", sellerUin).call();
    }

    public boolean rawInsert() {
        long id = JDBCBuilder.insert(
                "insert into `paipai_subscribe`(`sellerUin`,`chargeItemId`,`deadLine`) values(?,?,?)", this.sellerUin,
                this.chargeItemId, this.deadLine);
        if (id > 0) {
            return true;
        }
        return false;
    }

    public boolean rawUpdate() {
        long updateNum = JDBCBuilder.insert(
                "update `paipai_subscribe` set  `sellerUin` = ?, `chargeItemId` = ?, `deadLine` = ? where `null` = ? ",
                this.sellerUin, this.chargeItemId, this.deadLine, this.getId());

        if (updateNum > 0) {
            return true;
        }
        return false;
    }

    public static class ListFetcher extends JDBCExecutor<List<PaiPaiSubscribe>> {
        public ListFetcher(String whereQuery, Object... params) {
            super(whereQuery, params);
            StringBuilder sb = new StringBuilder();
            sb.append("select sellerUin,chargeItemId,deadLine from ");
            sb.append(PaiPaiSubscribe.TABLE_NAME);
            if (!StringUtils.isBlank(whereQuery)) {
                sb.append(" where ");
                sb.append(whereQuery);
            }
            this.src = dp.getSrc();
            this.query = sb.toString();

        }

        @Override
        public List<PaiPaiSubscribe> doWithResultSet(ResultSet rs) throws SQLException {
            List<PaiPaiSubscribe> list = new ArrayList<PaiPaiSubscribe>();
            while (rs.next()) {
                list.add(new PaiPaiSubscribe(rs));
            }
            return list;
        }
    }

    public static long countQuery(String whereQuery, Object... params) {
        StringBuilder sb = new StringBuilder();
        sb.append("select count(*) from ");
        sb.append(PaiPaiSubscribe.TABLE_NAME);
        if (!StringUtils.isBlank(whereQuery)) {
            sb.append(" where ");
            sb.append(whereQuery);
        }
        return JDBCBuilder.singleLongQuery(dp.getSrc(), sb.toString(), params);
    }

}
