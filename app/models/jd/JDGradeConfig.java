package models.jd;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.DBBuilder.DataSrc;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

/**
 * 买家等级配置
 * @author Administrator
 *
 */
@Entity(name = JDGradeConfig.TABLE_NAME)
public class JDGradeConfig extends GenericModel implements PolicySQLGenerator {
    private static final Logger log = LoggerFactory.getLogger(JDGradeConfig.class);
    
    public static final String TABLE_NAME = "JDGradeConfig_";
    
    private static final JDGradeConfig EMPTY = new JDGradeConfig();
    
    public static final DBDispatcher GradeConfigDp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    
    @Id
    private long sellerId;
    
    //普通会员的设置
    private double normalTradeAmount;
    private int normalTradeCount;
    
    //高级会员的设置
    private double advanceTradeAmount;
    private int advanceTradeCount;
    
    //VIP的设置
    private double vipTradeAmount;
    private int vipTradeCount;
    
    //至尊VIP的设置
    private double godTradeAmount;
    private int godTradeCount;
    
    private long createTs;
    private long updateTs;
    
    
    public JDGradeConfig() {
        
    }
    
    
    public void updateGradeConfig(double normalTradeAmount,
            int normalTradeCount, double advanceTradeAmount,
            int advanceTradeCount, double vipTradeAmount, int vipTradeCount,
            double godTradeAmount, int godTradeCount) {

        this.normalTradeAmount = normalTradeAmount;
        this.normalTradeCount = normalTradeCount;
        this.advanceTradeAmount = advanceTradeAmount;
        this.advanceTradeCount = advanceTradeCount;
        this.vipTradeAmount = vipTradeAmount;
        this.vipTradeCount = vipTradeCount;
        this.godTradeAmount = godTradeAmount;
        this.godTradeCount = godTradeCount;
    }
    public long getSellerId() {
        return sellerId;
    }
    public void setSellerId(long sellerId) {
        this.sellerId = sellerId;
    }
    
    public double getNormalTradeAmount() {
        return normalTradeAmount;
    }
    public void setNormalTradeAmount(double normalTradeAmount) {
        this.normalTradeAmount = normalTradeAmount;
    }
    public int getNormalTradeCount() {
        return normalTradeCount;
    }
    public void setNormalTradeCount(int normalTradeCount) {
        this.normalTradeCount = normalTradeCount;
    }
    public double getAdvanceTradeAmount() {
        return advanceTradeAmount;
    }
    public void setAdvanceTradeAmount(double advanceTradeAmount) {
        this.advanceTradeAmount = advanceTradeAmount;
    }
    public int getAdvanceTradeCount() {
        return advanceTradeCount;
    }
    public void setAdvanceTradeCount(int advanceTradeCount) {
        this.advanceTradeCount = advanceTradeCount;
    }

    public double getVipTradeAmount() {
        return vipTradeAmount;
    }
    public void setVipTradeAmount(double vipTradeAmount) {
        this.vipTradeAmount = vipTradeAmount;
    }
    public int getVipTradeCount() {
        return vipTradeCount;
    }
    public void setVipTradeCount(int vipTradeCount) {
        this.vipTradeCount = vipTradeCount;
    }

    public double getGodTradeAmount() {
        return godTradeAmount;
    }
    public void setGodTradeAmount(double godTradeAmount) {
        this.godTradeAmount = godTradeAmount;
    }
    public int getGodTradeCount() {
        return godTradeCount;
    }
    public void setGodTradeCount(int godTradeCount) {
        this.godTradeCount = godTradeCount;
    }

    public long getCreateTs() {
        return createTs;
    }
    public void setCreateTs(long createTs) {
        this.createTs = createTs;
    }
    public long getUpdateTs() {
        return updateTs;
    }
    public void setUpdateTs(long updateTs) {
        this.updateTs = updateTs;
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
        return "sellerId";
    }
    @Override
    public Long getId() {
        return sellerId;
    }
    @Override
    public void setId(Long id) {
        this.sellerId = id;
    }
    
    
    
    private static String EXIST_ID_QUERY = "select sellerId from " + TABLE_NAME + " where sellerId = ? ";
    
    private static long findExistId(long sellerId) {
        
        return GradeConfigDp.singleLongQuery(EXIST_ID_QUERY, sellerId);
        
    }
    
    
    public boolean rawInsert() {
        
        this.createTs = System.currentTimeMillis();
        this.updateTs = System.currentTimeMillis();
        
        String insertSql = "insert into `" + TABLE_NAME + "`(`sellerId`,`normalTradeAmount`,`normalTradeCount`," +
                "`advanceTradeAmount`,`advanceTradeCount`," +
                "`vipTradeAmount`,`vipTradeCount`," +
                "`godTradeAmount`,`godTradeCount`," +
                "`createTs`,`updateTs`) values(?,?,?,?,?,?,?,?,?,?,?)";
        
        long id = GradeConfigDp.insert(insertSql, this.sellerId, this.normalTradeAmount, this.normalTradeCount,
                this.advanceTradeAmount, this.advanceTradeCount,
                this.vipTradeAmount, this.vipTradeCount,
                this.godTradeAmount, this.godTradeCount,
                this.createTs, this.updateTs);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[sellerId : ]" + this.sellerId);

            return false;
        }
    }
    
    
    public boolean rawUpdate() {
        
        this.updateTs = System.currentTimeMillis();
        
        String updateSql = "update `" + TABLE_NAME + "` set `normalTradeAmount`=?,`normalTradeCount`=?," +
                "`advanceTradeAmount`=?,`advanceTradeCount`=?," +
                "`vipTradeAmount`=?,`vipTradeCount`=?," +
                "`godTradeAmount`=?,`godTradeCount`=?," +
                "`createTs`=?,`updateTs`=? where `sellerId` = ?";
        
        long updateNum = GradeConfigDp.insert(updateSql, this.normalTradeAmount, this.normalTradeCount,
                this.advanceTradeAmount, this.advanceTradeCount, 
                this.vipTradeAmount, this.vipTradeCount, 
                this.godTradeAmount, this.godTradeCount, 
                this.createTs, this.updateTs, this.sellerId);

        if (updateNum == 1L) {
            return true;
        } else {
            log.error("update Fails....." + "[sellerId : ]" + this.sellerId);

            return false;
        }
    }
    
    
    @Override
    public boolean jdbcSave() {
        try {
            long existdId = findExistId(this.sellerId);
            
            boolean status = false;
            if (existdId <= 0L) {
                status = rawInsert();
            } else {
                status = rawUpdate();
            }
            
            return status;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return false;
        }
    }
    
    
    
    @Override
    public String getIdName() {
        return "sellerId";
    }
    
    
    
}
