package models.traderate;

import codegen.CodeGenerator;
import com.google.common.collect.Lists;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.db.jpa.GenericModel;
import transaction.DBBuilder;
import transaction.JDBCBuilder;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import codegen.CodeGenerator.DBDispatcher;


@JsonIgnoreProperties(value = { "tableHashKey", "persistent", "tableName", "idName", "idColumn", "entityId" })
@Entity(name = OrderPlay.TABLE_NAME)
public class OrderPlay extends GenericModel implements CodeGenerator.PolicySQLGenerator {
    @Transient
    private static final Logger log = LoggerFactory.getLogger(OrderPlay.class);

    @Transient
    public static final String TABLE_NAME = "order_play";

    @Transient
    public static OrderPlay EMPTY = new OrderPlay();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DBBuilder.DataSrc.QUOTA, EMPTY);

    @Id
    @GeneratedValue
    private Long id;

    private Long userId;

    private Long oid;//订单号

    private Long dispatchId;//分配专员ID

    @Transient
    private String groupName;//专员名称

    private String remark;//备注

    public OrderPlay(){}

    public OrderPlay(ResultSet rs) {
        try {
            this.userId = rs.getLong(1);
            this.oid = rs.getLong(2);
            this.dispatchId = rs.getLong(3);
            this.remark = rs.getString(4);
        } catch (SQLException e) {
            e.printStackTrace();
        }

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
        return null;
    }

    @Override
    public Long getId() {
        return null;
    }

    @Override
    public void setId(Long id) {
    }

    @Override
    public boolean jdbcSave() {
        return false;
    }

    @Override
    public String getIdName() {
        return null;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getOid() {
        return oid;
    }

    public void setOid(Long oid) {
        this.oid = oid;
    }

    public Long getDispatchId() {
        return dispatchId;
    }

    public void setDispatchId(Long dispatchId) {
        this.dispatchId = dispatchId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public static List<OrderPlay> selectByOid(Long userId, Long oid) {

        String sql = "select userId, oid, dispatchId, remark from " + TABLE_NAME + " where userId = ? and  oid = ?";

        List<OrderPlay> list = new JDBCBuilder.JDBCExecutor<List<OrderPlay>>(OrderPlay.dp, sql, userId, oid) {
            @Override
            public List<OrderPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<OrderPlay> list = new ArrayList<OrderPlay>();

                while (rs.next()) {
                    OrderPlay itemLimit = new OrderPlay(rs);
                    if (itemLimit != null) {
                        list.add(itemLimit);
                    }
                }
                return list;
            }
        }.call();

        return list != null ? list : Lists.<OrderPlay>newArrayList();
    }

    public static OrderPlay findByOid(Long userId, Long oid) {

        List<OrderPlay> orderPlays = selectByOid(userId, oid);
        if (orderPlays!= null && orderPlays.size() >0){
            return orderPlays.get(0);
        }
        return new OrderPlay();
    }

    public static List<OrderPlay> selectByDispatchId(Long userId, Integer dispatchId) {
        String sql = "select userId, oid, dispatchId, remark from " + TABLE_NAME + " where userId = ? and dispatchId = ?";
        List<OrderPlay> list = new JDBCBuilder.JDBCExecutor<List<OrderPlay>>(OrderPlay.dp, sql, userId, dispatchId) {
            @Override
            public List<OrderPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<OrderPlay> list = new ArrayList<OrderPlay>();

                while (rs.next()) {
                    OrderPlay itemLimit = new OrderPlay(rs);
                    if (itemLimit != null) {
                        list.add(itemLimit);
                    }
                }
                return list;
            }
        }.call();

        return list;
    }

    public static List<OrderPlay> selectByUserId(Long userId) {
        String sql = "select userId, oid, dispatchId, remark from " + TABLE_NAME + " where userId = ? ";

        List<OrderPlay> list =  new JDBCBuilder.JDBCExecutor<List<OrderPlay>>(OrderPlay.dp, sql, userId) {
            @Override
            public List<OrderPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<OrderPlay> list = new ArrayList<OrderPlay>();
                while (rs.next()) {
                    OrderPlay itemLimit = new OrderPlay(rs);
                    if (itemLimit != null) {
                        list.add(itemLimit);
                    }
                }
                return list;
            }
        }.call();

        return list != null ? list : Lists.<OrderPlay>newArrayList();
    }



    public Boolean update() {
        String sql = "update " + TABLE_NAME + " %s where userId = ? and oid = ?";
        List param = new ArrayList();
        if (this.userId == null || this.oid == null) return false;
        String setSql = " set ";
        if (getDispatchId() != null) {
            setSql +=  " dispatchId = ?,";
            param.add(getDispatchId());
        }
        if (getRemark() != null) {
            setSql += " remark = ?,";
            param.add(getRemark());
        }
        if (!setSql.contains("?")) {
            setSql = "";
        } else {
            setSql = setSql.substring(0, setSql.length()-1);
        }
        sql = String.format(sql, setSql);
        param.add(this.userId);
        param.add(this.oid);
        long update = dp.update(sql, param.toArray());

        return update > 0;
    }


    public Boolean insert() {
        String sql = "insert into " + TABLE_NAME + "(userId, oid, dispatchId, remark) value(?, ?, ?, ?)";
        if (this.userId == null || this.oid == null) return false;
        List params = new ArrayList();
        params.add(this.userId);
        params.add(this.oid);

        params.add(this.dispatchId == null ? 0L : this.dispatchId);
        params.add(this.remark == null ? "" : this.remark);

        long insert = dp.insert(sql, params.toArray());

        return insert > 0;
    }

    private static Boolean updateOneIfAbsentInsert(Long userId, Long oid, Long dispatchId, String remark) {
        if (userId == null || oid == null) return false;
        List<OrderPlay> orderPlays = selectByOid(userId, oid);
        OrderPlay orderPlay = new OrderPlay();
        orderPlay.setUserId(userId);
        orderPlay.setOid(oid);
        orderPlay.setDispatchId(dispatchId);
        orderPlay.setRemark(remark);
        if (orderPlays != null && orderPlays.size() == 0) {
            return orderPlay.insert();
        } else {
            return orderPlay.update();
        }
    }


    public static Integer updateDispatchIdByOidSet(Long userId, Set<Long> oids, Long dispatchId) {
        Integer update = 0;
        for (Long oid : oids) {
            Boolean success = updateOneIfAbsentInsert(userId, oid, dispatchId, null);
            if (success)
                update++;
        }

        return update;
    }

    public static Boolean updateRemarkByOid(Long userId, Long oid, String remark) {

        return updateOneIfAbsentInsert(userId, oid, null, remark);

    }



}
