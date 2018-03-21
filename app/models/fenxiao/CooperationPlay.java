package models.fenxiao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.taobao.api.domain.Cooperation;

@Entity(name = CooperationPlay.TABLE_NAME)
@JsonIgnoreProperties(value = { "dataSrc", "persistent", "entityId", "entityId", "tableHashKey", "persistent",
        "tableName", "idName", "idColumn" })
public class CooperationPlay extends Model implements PolicySQLGenerator {

    @Transient
    private static final Logger log = LoggerFactory.getLogger(CooperationPlay.class);

    @Transient
    public static final String TABLE_NAME = "cooperation";

    @Transient
    static CooperationPlay EMPTY = new CooperationPlay();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

    private Long userId;

    /**
     * 合作关系ID
     */
    private Long cooperateId;

    /**
     * 分销商ID
     */
    private Long distributorId;

    /**
     * 分销商nick
     */
    private String distributorNick;

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 供应商NICK
     */
    private String supplierNick;

    /**
     * 分销方式： AGENT(代销) - 1 、DEALER(经销) - 2
     */
    private int tradeType;

    /**
     * 等级ID
     */
    private Long gradeId;

    /**
     * 授权产品线
     */
    private String productLine;

    /**
     * 合作起始时间
     */
    private Long startTs;

    /**
     * 合作终止时间
     */
    private Long endTs;

    /**
     * NORMAL END ENDING
     */
    private String status;

    public CooperationPlay() {
        super();
    }

    public CooperationPlay(Long userId, Cooperation cooperation) {
        this.userId = userId;
        this.cooperateId = cooperation.getCooperateId();
        this.distributorId = cooperation.getDistributorId();
        this.distributorNick = cooperation.getDistributorNick();
        this.supplierId = cooperation.getSupplierId();
        this.supplierNick = cooperation.getSupplierNick();
        this.startTs = cooperation.getStartDate().getTime();
        this.endTs = cooperation.getEndDate().getTime();
        this.status = cooperation.getStatus();
        if ("AGENT".equals(cooperation.getTradeType())) {
            this.tradeType = 1;
        } else if ("DEALER".equals(cooperation.getTradeType())) {
            this.tradeType = 2;
        }
        this.gradeId = cooperation.getGradeId();
        this.productLine = cooperation.getProductLine();
    }

    public CooperationPlay(ResultSet rs) throws SQLException {
        this.id = rs.getLong(1);
        this.cooperateId = rs.getLong(2);
        this.distributorId = rs.getLong(3);
        this.distributorNick = rs.getString(4);
        this.supplierId = rs.getLong(5);
        this.supplierNick = rs.getString(6);
        this.tradeType = rs.getInt(7);
        this.gradeId = rs.getLong(8);
        this.productLine = rs.getString(9);
        this.startTs = rs.getLong(10);
        this.endTs = rs.getLong(11);
        this.status = rs.getString(12);
        this.userId = rs.getLong(13);
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCooperateId() {
        return cooperateId;
    }

    public void setCooperateId(Long cooperateId) {
        this.cooperateId = cooperateId;
    }

    public Long getDistributorId() {
        return distributorId;
    }

    public void setDistributorId(Long distributorId) {
        this.distributorId = distributorId;
    }

    public String getDistributorNick() {
        return distributorNick;
    }

    public void setDistributorNick(String distributorNick) {
        this.distributorNick = distributorNick;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierNick() {
        return supplierNick;
    }

    public void setSupplierNick(String supplierNick) {
        this.supplierNick = supplierNick;
    }

    public int getTradeType() {
        return tradeType;
    }

    public void setTradeType(int tradeType) {
        this.tradeType = tradeType;
    }

    public Long getGradeId() {
        return gradeId;
    }

    public void setGradeId(Long gradeId) {
        this.gradeId = gradeId;
    }

    public String getProductLine() {
        return productLine;
    }

    public void setProductLine(String productLine) {
        this.productLine = productLine;
    }

    public Long getStartTs() {
        return startTs;
    }

    public void setStartTs(Long startTs) {
        this.startTs = startTs;
    }

    public Long getEndTs() {
        return endTs;
    }

    public void setEndTs(Long endTs) {
        this.endTs = endTs;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public Long getId() {
        // TODO Auto-generated method stub
        return id;
    }

    @Override
    public String getIdColumn() {
        // TODO Auto-generated method stub
        return "id";
    }

    @Override
    public String getIdName() {
        // TODO Auto-generated method stub
        return "id";
    }

    @Override
    public String getTableHashKey() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getTableName() {
        // TODO Auto-generated method stub
        return TABLE_NAME;
    }

    @Override
    public void setId(Long id) {
        // TODO Auto-generated method stub
        this.id = id;
    }

    static String EXIST_ID_QUERY = "select id from " + TABLE_NAME + " where distributorId = ? and supplierId = ?";

    private static long findExistId(Long distributorId, Long supplierId) {
        return dp.singleLongQuery(EXIST_ID_QUERY, distributorId, supplierId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#jdbcSave()
     */
    @Override
    public boolean jdbcSave() {

        try {
            long existdId = findExistId(this.distributorId, this.supplierId);
            // if (existdId != 0)
            // log.info("find existed Id: " + existdId);

            if (existdId == 0L) {
                return this.rawInsert();
            } else {
                id = existdId;
                return this.rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }

    }

    public boolean rawInsert() {
        long id = dp
                .insert("insert into `cooperation`(`cooperateId`,`distributorId`,`distributorNick`,`supplierId`,`supplierNick`,`tradeType`,`gradeId`,`productLine`,`startTs`,`endTs`,`status`,`userId`) values(?,?,?,?,?,?,?,?,?,?,?,?)",
                        this.cooperateId, this.distributorId, this.distributorNick, this.supplierId, this.supplierNick,
                        this.tradeType, this.gradeId, this.productLine, this.startTs, this.endTs, this.status,
                        this.userId);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.distributorId);
            return false;
        }
    }

    public boolean rawUpdate() {
        long updateNum = dp
                .insert("update `cooperation` set  `cooperateId` = ?, `distributorId` = ?, `distributorNick` = ?, `supplierId` = ?, `supplierNick` = ?, `tradeType` = ?, `gradeId` = ?, `productLine` = ?, `startTs` = ?, `endTs` = ?, `status` = ?, `userId` = ? where `id` = ? ",
                        this.cooperateId, this.distributorId, this.distributorNick, this.supplierId, this.supplierNick,
                        this.tradeType, this.gradeId, this.productLine, this.startTs, this.endTs, this.status,
                        this.userId, this.getId());

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for :" + this.id + "[userId : ]" + this.distributorId);
            return false;
        }
    }

    static String FENXIAO_QUERY = "select id,cooperateId,distributorId,distributorNick,supplierId,supplierNick,tradeType,gradeId,productLine,startTs,endTs,status,userId from cooperation where userId = ? ";

    public static List<CooperationPlay> findByUserId(Long userId) {
        return new JDBCBuilder.JDBCExecutor<List<CooperationPlay>>(dp, FENXIAO_QUERY, userId) {
            @Override
            public List<CooperationPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<CooperationPlay> list = new ArrayList<CooperationPlay>();
                while (rs.next()) {
                    CooperationPlay cooperation = new CooperationPlay(rs);
                    list.add(cooperation);
                }
                return list;
            }
        }.call();
    }
}
