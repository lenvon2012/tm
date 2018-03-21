/*package models.autolist;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = DistributeRule.TABLE_NAME)
public class DistributeRule extends GenericModel implements PolicySQLGenerator {
    
    @Transient
    private static final Logger log = LoggerFactory.getLogger(DistributeRule.class);
    
    @Transient
    public static final String TABLE_NAME = "distribute_rule";
    
    @Transient
    public static DistributeRule EMPTY = new DistributeRule();
    
    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    @Id
    private Long userId;
    
    private int status;
    
    @Column(columnDefinition = "int default 1 ")
    private int config = DelistConfig.Smart_Distri;
    
    public static class DelistConfig {
        public static final int Smart_Distri = 1;// 更新宝贝后，分布上下架
        public static final int Remain_Old = 2;// 保持原样
    }
    
    
    
    private long createTime;
    
    private long updateTime;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getConfig() {
        return config;
    }

    public void setConfig(int config) {
        this.config = config;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public AutoListConfig() {
        super();
    }

    public AutoListConfig(Long userId, int config) {
        super();
        this.userId = userId;
        this.config = config;
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
        return "userId";
    }

    @Override
    public Long getId() {
        return userId;
    }

    @Override
    public void setId(Long id) {
        this.userId = id;
    }
    
    public static long findExistId(Long userId) {

        String query = "select userId from " + TABLE_NAME + " where userId = ? ";

        return dp.singleLongQuery(query, userId);
    }


    @Override
    public boolean jdbcSave() {
        try {
            
            long existId = findExistId(this.userId);
            
            if (existId <= 0) {
                return this.rawInsert();
            } else {
                return this.rawUpdate();
            }
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return false;
        }
    }

    @Override
    public String getIdName() {
        return "userId";
    }
    
    public boolean rawInsert() {
        try {
            String insertSQL = "insert into `"
                    + TABLE_NAME
                    + "`(`userId`,`status`,`config`,`createTime`,"
                    +
                    "`updateTime`) values(?,?,?,?,?)";

            this.createTime = System.currentTimeMillis();
            this.updateTime = System.currentTimeMillis();
            
            long id = dp.insert(insertSQL, userId, status, config, createTime, updateTime);
            
            if (id > 0L) {
                return true;
            } else {
                return false;
            }
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            
            return false;
        }
    }
    
    
    public boolean rawUpdate() {

        String updateSQL = "update `"
                + TABLE_NAME
                + "` set `status` = ?, `config` = ?, `updateTime` = ? where `userId` = ?  ";

        this.updateTime = System.currentTimeMillis();
        
        long updateNum = dp.insert(updateSQL, this.status, this.config, this.updateTime,
                this.userId);

        if (updateNum == 1) {

            return true;
        } else {

            return false;
        }
    }
    
    
    public static AutoListConfig findByUserId(Long userId) {

        String query = "select " + SelectAllProperties + " from " + TABLE_NAME
                + " where userId = ? ";

        return new JDBCBuilder.JDBCExecutor<AutoListConfig>(dp, query, userId) {

            @Override
            public AutoListConfig doWithResultSet(ResultSet rs)
                    throws SQLException {

                if (rs.next()) {

                    return parseAutoListConfig(rs);

                } else {
                    return null;
                }

            }

        }.call();
    }
    
    public static boolean isRemainDelist(Long userId) {
        AutoListConfig listCfg = AutoListConfig.findByUserId(userId);
        if (listCfg == null) {
            return false;
        } else {
            if (listCfg.getConfig() == DelistConfig.Remain_Old) {
                return true;
            } else {
                return false;
            }
        }
        
    }
    
    
    private static final String SelectAllProperties = " userId,status,config,createTime,updateTime ";

    private static AutoListConfig parseAutoListConfig(ResultSet rs) {
        try {
            AutoListConfig listCfg = new AutoListConfig();

            listCfg.setUserId(rs.getLong(1));
            listCfg.setStatus(rs.getInt(2));
            listCfg.setConfig(rs.getInt(3));
            listCfg.setCreateTime(rs.getLong(4));
            listCfg.setUpdateTime(rs.getLong(5));

            return listCfg;

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    } 
    
}
*/