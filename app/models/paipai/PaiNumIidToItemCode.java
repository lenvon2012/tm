package models.paipai;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.JDBCBuilder;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.NumberUtil;

@Entity(name = PaiNumIidToItemCode.TABLE_NAME)
public class PaiNumIidToItemCode extends Model {

    @Transient
    private static final Logger log = LoggerFactory.getLogger(PaiNumIidToItemCode.class);

    @Transient
    public static final String TAG = "PaiNumIidToItemCode";

    @Transient
    public static final String TABLE_NAME = "pai_numiid_to_itemcode";

    @Index(name = "itemCode")
    private String itemCode;

    public PaiNumIidToItemCode(String itemCode) {
        super();
        this.itemCode = itemCode;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public static String fetchItemCode(Long numIid) {
        if (NumberUtil.isNullOrZero(numIid)) {
            return null;
        }
        return JDBCBuilder.singleStringQuery(" select itemCode from " + TABLE_NAME + " where id = ?", numIid);
    }

    public static long ensureItemCode(String itemCode) {
        String sql = "select id from " + TABLE_NAME + " where itemCode = ?";
        long existId = JDBCBuilder.singleLongQuery(sql, itemCode);
        if (existId > 0L) {
            return existId;
        }

        sql = "insert into `" + TABLE_NAME + "`(`itemCode`) values(?)";
        return JDBCBuilder.insert(sql, itemCode);
    }

    public static HashMap<Long, String> fetchItemCodeMap(HashSet<Long> numIids) {
        if (CommonUtils.isEmpty(numIids)) {
            return null;
        }

        String ids = StringUtils.join(numIids, ",");
        String sql = "select id,itemCode from " + TABLE_NAME + " where id in (" + ids + ")";
        return new JDBCBuilder.JDBCExecutor<HashMap<Long, String>>(sql) {

            @Override
            public HashMap<Long, String> doWithResultSet(ResultSet rs) throws SQLException {
                HashMap<Long, String> map = new HashMap<Long, String>();
                while (rs.next()) {
                    map.put(rs.getLong(1), rs.getString(2));
                }
                return map;
            }

        }.call();

    }

}
