package models.promotion;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import utils.DateUtil;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name =EveryDaySalesCount.TABLE_NAME)
public class EveryDaySalesCount extends Model implements PolicySQLGenerator  {
    
    private static final Logger log=LoggerFactory.getLogger(EveryDaySalesCount.class);

    public static final String TABLE_NAME ="everydaysalescount";
    
    @Index(name = "numIid")
    private Long numIid;
    
    private Long userId;
    
    @Index(name = "ts")
    private Long ts;
    
    private Long salesCount;
    
    public EveryDaySalesCount(){
        
    }
    
    public EveryDaySalesCount(Long numIid,Long userId,Long ts,Long salesCount){
        this.numIid = numIid;
        this.userId = userId;
        this.ts = ts;
        this.salesCount = salesCount;
    }

    public EveryDaySalesCount(Long numIid,Long userId,Long salesCount){
        this.numIid = numIid;
        this.userId = userId;
        this.ts = DateUtil.formCurrDate();;
        this.salesCount = salesCount;
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

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public Long getSalesCount() {
        return salesCount;
    }

    public void setSalesCount(Long salesCount) {
        this.salesCount = salesCount;
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
    public Long getId() {
        // TODO Auto-generated method stub
        return id;
    }

    @Override
    public void setId(Long id) {
        // TODO Auto-generated method stub
        this.id=id;
    }

    @Override
    public String getIdName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String toString() {
        return "EveryDaySalesCount [numIid=" + numIid + ", userId=" + userId
                + ", ts=" + ts + ", salesCount=" + salesCount + "]";
    }
    
    @Transient
    static String EXIST_ID_QUERY = "select id from `everydaysalescount` where numIid = ? and ts= ?";
    
    public static long findExistId(Long numIid,Long ts) {
        return JDBCBuilder.singleLongQuery(EXIST_ID_QUERY, numIid,ts);
    }
    
    @Override
    public boolean jdbcSave() {
         try {
                long existdId = findExistId(this.numIid,this.ts);

                if (existdId == 0L) {
                    return this.rawInsert();
                } else {    
                    this.setId(existdId);
                    return this.rawUpdate();
                }

            } catch (Exception e) {
                log.warn(e.getMessage(), e);
                return false;
            }
    }
    
    @Transient
    static String insertSQL = "insert into `everydaysalescount`(`numIid`,`userId`,`ts`,`salesCount`) values(?,?,?,?)";

    public boolean rawInsert() {

        long id = JDBCBuilder.insert(insertSQL, this.numIid, this.userId, this.ts,
                this.salesCount);

        if (id != 0L) {
            return true;
        } else {
            return false;
        }
    }
    
    @Transient
    static final String updateSQL = "update `everydaysalescount` set  `numIid` = ?, `userId` = ?, `ts` = ?, `salesCount` = ?  where `id` = ? ";

    public boolean rawUpdate() {
        long updateNum = JDBCBuilder.update(false,updateSQL, this.numIid, this.userId, this.ts,
                this.salesCount,
                this.getId());
        if (updateNum > 0L) {
            return true;
        } else {
            return false;
        }
    }
    
    public static List<EveryDaySalesCount> findByNumIidAndTs(Long numIid,long startTs,long endTs){
        
        String sql = " select id,numIid,userId,ts,salesCount from "+TABLE_NAME+" where numIid= ? and ts >= ? and ts <= ? ";
        
        return new JDBCExecutor<List<EveryDaySalesCount>>(sql,numIid,startTs,endTs) {
            @Override
            public List<EveryDaySalesCount> doWithResultSet(ResultSet rs) throws SQLException {
                List<EveryDaySalesCount> list = new ArrayList<EveryDaySalesCount>();
                while (rs.next()) {
                    EveryDaySalesCount scList = parseSalesCount(rs);
                    if (scList != null)
                        list.add(scList);
                }
                return list;
            }
        }.call();
    }
    
    public static EveryDaySalesCount parseSalesCount(ResultSet rs) throws SQLException{
        Long id = rs.getLong(1);
        Long numIid = rs.getLong(2);
        Long userId = rs.getLong(3);
        Long ts = rs.getLong(4);
        Long salesCount = rs.getLong(5);
        
        return new EveryDaySalesCount(numIid, userId, ts, salesCount);
    }

}
