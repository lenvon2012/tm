package models.order;

/**
 * 订单结构
 *
 */

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

import models.trade.JDTradeDisplay.StatusType;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.DBBuilder;
import transaction.DBBuilder.DataSrc;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;
import com.jd.open.api.sdk.domain.order.ItemInfo;
import com.jd.open.api.sdk.domain.order.OrderSearchInfo;

@Entity(name = JDOrderDisplay.TABLE_NAME)
public class JDOrderDisplay extends GenericModel implements PolicySQLGenerator {

    public static final String TABLE_NAME = "jd_order_display_";

    private static final Logger log = LoggerFactory.getLogger(JDOrderDisplay.class);

    public static final PolicySQLGenerator EMPTY = new JDOrderDisplay();

    public static DBDispatcher dp = new DBDispatcher(DataSrc.QUOTA, EMPTY);

    public JDOrderDisplay() {
        super();
    }

    /**
     * "sku_id": "1100037898", "ware_id": "", "jd_price": "400.00", "sku_name": "大苹果", "product_no": "", "gift_point":
     * "0", "outer_sku_id": "", "item_total": "1"
     */

    /**
     * Order对应的User
     */
    @Index(name = "vender_id")
    @CodeNoUpdate
    public Long venderId;

    @Index(name = "tid")
    @CodeNoUpdate
    public Long tid;

    /**
     * 京东内部SKU的ID
     */
    @Id
    @CodeNoUpdate
    public Long skuId;

    /**
     * SKU外部ID
     */
    public Long outerSkuId;

    /**
     * 商品的名称+SKU规格（比如
     */
    public String skuName;

    /**
     * SKU的京东价
     */
    public Double jdPrice;

    /**
     * 赠送积分
     */
    public int giftPoint;

    /**
     * 京东内部商品ID
     */
    public Long wareId;

    /**
     * 数量
     */
    public int itemTotal;

    /**
     * 订单状态
     */
    @Enumerated(EnumType.STRING)
    public StatusType status;

    @CodeNoUpdate
    public Long ts;

    public JDOrderDisplay(Long venderId, Long ts, OrderSearchInfo trade, ItemInfo order) {
        this(venderId, ts, trade, order, true);
    }

    public JDOrderDisplay(Long venderId, OrderSearchInfo trade, ItemInfo order) {
        this(venderId, DateUtil.formCurrDate(), trade, order, true);
    }

    public JDOrderDisplay(Long venderId, OrderSearchInfo trade, ItemInfo order, boolean isOldCustomer) {
        this(venderId, DateUtil.formCurrDate(), trade, order, isOldCustomer);
    }

    public JDOrderDisplay(Long venderId, Long ts, OrderSearchInfo trade, ItemInfo order, boolean isOldCustomer) {

        this.venderId = venderId;
        this.ts = ts;
        
        this.skuId = CommonUtils.String2Long(order.getSkuId());
        this.outerSkuId = CommonUtils.String2Long(order.getOuterSkuId());
        this.skuName = order.getSkuName();
        this.jdPrice = CommonUtils.String2Double(order.getJdPrice());
        this.giftPoint = CommonUtils.String2Int(order.getGiftPoint());
        this.wareId = CommonUtils.String2Long(order.getWareId());
        this.itemTotal = CommonUtils.String2Int(order.getItemTotal());
        this.status = StatusType.valueOf(trade.getOrderState());
    }
    
    

    public JDOrderDisplay(Long venderId, Long tid, Long skuId, Long outerSkuId, String skuName, Double jdPrice,
            int giftPoint, Long wareId, int itemTotal, StatusType status) {
        super();
        this.venderId = venderId;
        this.tid = tid;
        this.skuId = skuId;
        this.outerSkuId = outerSkuId;
        this.skuName = skuName;
        this.jdPrice = jdPrice;
        this.giftPoint = giftPoint;
        this.wareId = wareId;
        this.itemTotal = itemTotal;
        this.status = status;
        this.ts = System.currentTimeMillis();
    }

    public JDOrderDisplay(ResultSet rs) throws SQLException {
        this.venderId = rs.getLong(1);
        this.tid = rs.getLong(2);
        this.skuId = rs.getLong(3);
        this.outerSkuId = rs.getLong(4);
        this.skuName = rs.getString(5);
        this.jdPrice = rs.getDouble(6);
        this.giftPoint = rs.getInt(7);
        this.wareId = rs.getLong(8);
        this.itemTotal = rs.getInt(9);
        this.status = StatusType.valueOf(rs.getString(10));
        this.ts = rs.getLong(11);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getIdColumn() {
        return "oid";
    }

    @Override
    public Long getId() {
        return skuId;
    }

    @Override
    public void setId(Long id) {
        this.skuId = id;

    }

    public static String genShardQuery(String query, String key) {
        query = query.replaceAll("%s", "~~");
        query = query.replaceAll("%", "##");
        query = query.replaceAll("~~", "%s");

        String formQuery = String.format(query, key);
        return formQuery.replaceAll("##", "%");
    }

    public static String genShardQuery(String query, Long venderId) {
        return genShardQuery(query, String.valueOf(DBBuilder.genUserIdHashKey(venderId)));
    }

    static String SKUID_QUERY = "select skuId from jd_order_display_%s where skuId = ?";

    public static long findExistId(Long venderId, long skuId) {
        return dp.singleLongQuery(genShardQuery(SKUID_QUERY, venderId), skuId);
    }

    @Override
    public boolean jdbcSave() {

        try {

            return insertOnDupKeyUpdate();
            // long exist = dp.singleLongQuery(genShardQuery(OID_QUERY, venderId), oid);
            //
            // if (exist == 0L) {
            // return rawInsert();
            // } else {
            // return rawUpdate();
            // }

        } catch (Exception e) {
            log.warn(e.getMessage());
            return false;
        }
    }

    public static String INSERT_SQL = "insert into `jd_order_display_%s`(`venderId`,`tid`,`skuId`,`outerSkuId`,`skuName`,`jdPrice`,`giftPoint`,`wareId`,`itemTotal`,`status`,`ts`) values(?,?,?,?,?,?,?,?,?,?,?)";

    public boolean rawInsert() {
        long id = dp.insert(genShardQuery(INSERT_SQL, venderId), this.venderId, this.tid, this.skuId, this.outerSkuId,
                this.skuName, this.jdPrice, this.giftPoint, this.wareId, this.itemTotal, this.status, this.ts);

        if (id != 0L) {
            // new OrderItem(this).rawInsertOnDupUpdate();
            // log.info("Raw Insert Order Display OK:" + oid);
            return true;
        } else {
            log.warn("Raw insert Fails... for :" + skuId);
            return false;
        }

    }

    public static String UPDATE_SQL = "update `jd_order_display_` set  `outerSkuId` = ?, `skuName` = ?, `jdPrice` = ?, `giftPoint` = ?, `wareId` = ?, `itemTotal` = ?, `status` = ? where `skuId` = ? ";

    public boolean rawUpdate() {
        long updateNum = dp.insert(genShardQuery(UPDATE_SQL, venderId), this.outerSkuId, this.skuName, this.jdPrice,
                this.giftPoint, this.wareId, this.itemTotal, this.status, this.getId());

        if (updateNum > 0L) {
            // log.info("Update Order Display OK:" + oid);
            return true;
        } else {
            log.warn("Update Fails... for :" + skuId);

            return false;
        }
    }

    static String insertOnDupKeyUpdateSQL = INSERT_SQL
            + " update `outerSkuId` = ?, `skuName` = ?, `jdPrice` = ?, `giftPoint` = ?, `wareId` = ?, `itemTotal` = ?, `status` = ?";

    public boolean insertOnDupKeyUpdate() {
        long id = dp.insert(genShardQuery(insertOnDupKeyUpdateSQL, this.venderId), this.venderId, this.tid, this.skuId,
                this.outerSkuId, this.skuName, this.jdPrice, this.giftPoint, this.wareId, this.itemTotal, this.status,
                this.ts, this.outerSkuId, this.skuName, this.jdPrice, this.giftPoint, this.wareId, this.itemTotal,
                this.status);

        if (id != 0L) {
            // new OrderItem(this).rawInsertOnDupUpdate();
            // log.info("Raw Insert Order Display OK:" + oid);
            return true;
        } else {
            log.warn("Raw insert Fails... for :" + skuId);

            return false;
        }
    }

    @Override
    public String getIdName() {
        return "skuId";
    }

    @Override
    public void _save() {
        throw new UnsupportedOperationException("No Save Method for this model...");
    }

    @Override
    public String getTableHashKey() {
        // TODO Auto-generated method stub
        return null;
    }
}
